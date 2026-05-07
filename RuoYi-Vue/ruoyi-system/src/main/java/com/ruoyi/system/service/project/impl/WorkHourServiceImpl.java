package com.ruoyi.system.service.project.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.system.domain.project.ProjectStatus;
import com.ruoyi.system.domain.project.ProjInfo;
import com.ruoyi.system.domain.project.ProjWbsNode;
import com.ruoyi.system.domain.project.SysCostCategory;
import com.ruoyi.system.domain.project.WorkHour;
import com.ruoyi.system.domain.project.WorkHourAttachment;
import com.ruoyi.system.domain.project.WorkHourSequence;
import com.ruoyi.system.domain.project.WorkHourStatus;
import com.ruoyi.system.domain.vo.project.WorkHourFormVO;
import com.ruoyi.system.mapper.project.ProjInfoMapper;
import com.ruoyi.system.mapper.project.ProjWbsNodeMapper;
import com.ruoyi.system.mapper.project.SysCostCategoryMapper;
import com.ruoyi.system.mapper.project.WorkHourAttachmentMapper;
import com.ruoyi.system.mapper.project.WorkHourMapper;
import com.ruoyi.system.service.project.ICostApprovalService;
import com.ruoyi.system.service.project.IWorkHourService;

@Service
public class WorkHourServiceImpl implements IWorkHourService
{
    private static final BigDecimal HALF_HOUR = new BigDecimal("0.5");
    private static final String LABOR_CATEGORY_NAME = "人工费";

    @Autowired
    private WorkHourMapper workHourMapper;
    @Autowired
    private WorkHourAttachmentMapper attachmentMapper;
    @Autowired
    private ProjInfoMapper projInfoMapper;
    @Autowired
    private ProjWbsNodeMapper wbsNodeMapper;
    @Autowired
    private SysCostCategoryMapper categoryMapper;
    @Autowired
    private ICostApprovalService costApprovalService;

    public List<WorkHour> selectWorkHourList(WorkHour workHour)
    {
        return workHourMapper.selectWorkHourList(workHour);
    }

    public WorkHourFormVO getWorkHourForm(Long whId)
    {
        WorkHourFormVO vo = new WorkHourFormVO();
        vo.setWorkHour(workHourMapper.selectWorkHourById(whId));
        vo.setAttachments(attachmentMapper.selectByWhId(whId));
        return vo;
    }

    public List<ProjWbsNode> selectWbsNodesByProjId(Long projId)
    {
        ProjInfo info = projInfoMapper.selectProjInfoById(projId);
        ProjectStatus.require(info, ProjectStatus.APPROVED, ProjectStatus.IN_PROGRESS);
        return wbsNodeMapper.selectByProjId(projId);
    }

    @Transactional
    public WorkHour saveDraft(WorkHourFormVO form, String username)
    {
        if (form == null || form.getWorkHour() == null)
        {
            throw new ServiceException("工时信息不能为空");
        }
        WorkHour workHour = form.getWorkHour();
        if (workHour.getWhId() != null)
        {
            WorkHourStatus.require(workHourMapper.selectWorkHourById(workHour.getWhId()), WorkHourStatus.DRAFT, WorkHourStatus.REJECTED);
        }
        fillAndValidate(workHour);
        workHour.setStatus(WorkHourStatus.DRAFT.code());
        if (StringUtils.isEmpty(workHour.getWhNo()))
        {
            workHour.setWhNo(generateWhNo());
        }
        workHour.setUpdateBy(username);
        if (workHour.getWhId() == null)
        {
            workHour.setCreateBy(username);
            workHourMapper.insertWorkHour(workHour);
        }
        else
        {
            workHourMapper.updateWorkHour(workHour);
        }
        saveAttachments(workHour.getWhId(), form.getAttachments(), username);
        return workHour;
    }

    public int submitForApproval(Long whId, String username)
    {
        WorkHour workHour = workHourMapper.selectWorkHourById(whId);
        WorkHourStatus.require(workHour, WorkHourStatus.DRAFT, WorkHourStatus.REJECTED);
        workHour.setStatus(WorkHourStatus.PENDING.code());
        workHour.setSubmitTime(new Date());
        workHour.setUpdateBy(username);
        return workHourMapper.updateWorkHour(workHour);
    }

    public int approve(Long whId, String username)
    {
        WorkHour workHour = workHourMapper.selectWorkHourById(whId);
        WorkHourStatus.require(workHour, WorkHourStatus.PENDING);
        workHour.setStatus(WorkHourStatus.APPROVED.code());
        workHour.setApproveBy(username);
        workHour.setApproveTime(new Date());
        workHour.setUpdateBy(username);
        return workHourMapper.updateWorkHour(workHour);
    }

    public int reject(Long whId, String reason, String username)
    {
        WorkHour workHour = workHourMapper.selectWorkHourById(whId);
        WorkHourStatus.require(workHour, WorkHourStatus.PENDING);
        if (StringUtils.isEmpty(reason))
        {
            throw new ServiceException("请填写驳回原因");
        }
        workHour.setStatus(WorkHourStatus.REJECTED.code());
        workHour.setRejectReason(reason);
        workHour.setApproveBy(username);
        workHour.setApproveTime(new Date());
        workHour.setUpdateBy(username);
        return workHourMapper.updateWorkHour(workHour);
    }

    public int postCost(Long whId, String username)
    {
        return costApprovalService.postApproved(CostApprovalServiceImpl.BILL_TYPE_WORK_HOUR, whId, username);
    }

    public int deleteWorkHourById(Long whId)
    {
        WorkHourStatus.require(workHourMapper.selectWorkHourById(whId), WorkHourStatus.DRAFT, WorkHourStatus.REJECTED);
        attachmentMapper.deleteByWhId(whId);
        return workHourMapper.deleteWorkHourById(whId);
    }

    private void fillAndValidate(WorkHour workHour)
    {
        ProjInfo info = projInfoMapper.selectProjInfoById(workHour.getProjId());
        ProjectStatus.require(info, ProjectStatus.APPROVED, ProjectStatus.IN_PROGRESS);
        ProjWbsNode node = wbsNodeMapper.selectByNodeId(workHour.getNodeId());
        if (node == null || !workHour.getProjId().equals(node.getProjId()))
        {
            throw new ServiceException("WBS节点与项目不匹配");
        }
        SysCostCategory category = categoryMapper.selectSysCostCategoryById(workHour.getCategoryId());
        if (category == null || !"0".equals(category.getStatus()))
        {
            throw new ServiceException("请选择有效成本科目");
        }
        SysCostCategory laborCategory = categoryMapper.selectRootCategoryByName(LABOR_CATEGORY_NAME);
        if (laborCategory == null || category.getParentId() == null || !category.getParentId().equals(laborCategory.getCategoryId()))
        {
            throw new ServiceException("请选择人工费类科目");
        }
        if (StringUtils.isEmpty(workHour.getWorkType()))
        {
            throw new ServiceException("工作类型不能为空");
        }
        if (workHour.getWorkDate() == null)
        {
            throw new ServiceException("填报日期不能为空");
        }
        if (workHour.getWorkHours() == null || workHour.getWorkHours().compareTo(BigDecimal.ZERO) <= 0)
        {
            throw new ServiceException("工时数必须大于0");
        }
        BigDecimal remainder = workHour.getWorkHours().remainder(HALF_HOUR);
        if (remainder.compareTo(BigDecimal.ZERO) != 0)
        {
            throw new ServiceException("工时数必须为0.5的整数倍");
        }
        if (StringUtils.isEmpty(workHour.getWorkDesc()))
        {
            throw new ServiceException("工作内容不能为空");
        }
        if (workHour.getWorkDesc().length() > 200)
        {
            throw new ServiceException("工作描述不能超过200字");
        }
        BigDecimal unitPrice = category.getUnitPrice() == null ? BigDecimal.ZERO : category.getUnitPrice();
        workHour.setProjName(info.getProjName());
        workHour.setNodeName(node.getNodeName());
        workHour.setCategoryName(category.getCategoryName());
        workHour.setUnitPrice(unitPrice);
        workHour.setWorkCost(workHour.getWorkHours().multiply(unitPrice).setScale(2, RoundingMode.HALF_UP));
    }

    private void saveAttachments(Long whId, List<WorkHourAttachment> attachments, String username)
    {
        attachmentMapper.deleteByWhId(whId);
        List<WorkHourAttachment> list = attachments == null ? new ArrayList<>() : attachments;
        for (WorkHourAttachment attachment : list)
        {
            if (attachment == null || StringUtils.isEmpty(attachment.getFilePath()))
            {
                continue;
            }
            attachment.setWhId(whId);
            attachment.setFileName(StringUtils.isEmpty(attachment.getFileName()) ? attachment.getFilePath() : attachment.getFileName());
            attachment.setOriginalName(StringUtils.isEmpty(attachment.getOriginalName()) ? attachment.getFileName() : attachment.getOriginalName());
            attachment.setCreateBy(username);
            attachmentMapper.insertWorkHourAttachment(attachment);
        }
    }

    private String generateWhNo()
    {
        WorkHourSequence sequence = new WorkHourSequence();
        workHourMapper.insertWorkHourSequence(sequence);
        String date = new SimpleDateFormat("yyyyMMdd").format(new Date());
        return "WH-" + date + "-" + String.format("%03d", sequence.getId());
    }
}
