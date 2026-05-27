package com.ruoyi.system.service.project.impl;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.system.domain.project.ProjectSequence;
import com.ruoyi.system.domain.project.ProjectStatus;
import com.ruoyi.system.domain.project.ProjCostAllocation;
import com.ruoyi.system.domain.project.ProjInfo;
import com.ruoyi.system.domain.project.ProjWbsNode;
import com.ruoyi.system.domain.vo.project.ProjInfoFormVO;
import com.ruoyi.system.mapper.project.ProjCostAllocationMapper;
import com.ruoyi.system.mapper.project.ProjInfoMapper;
import com.ruoyi.system.mapper.project.ProjWbsNodeMapper;
import com.ruoyi.system.service.project.IProjInfoService;

@Service
public class ProjInfoServiceImpl implements IProjInfoService
{
    @Autowired
    private ProjInfoMapper projInfoMapper;
    @Autowired
    private ProjWbsNodeMapper wbsNodeMapper;
    @Autowired
    private ProjCostAllocationMapper allocationMapper;

    public ProjInfoFormVO getProjForm(Long projId)
    {
        refreshProjectStatusByDate();
        ProjInfoFormVO vo = new ProjInfoFormVO();
        vo.setProjInfo(projInfoMapper.selectProjInfoById(projId));
        vo.setWbsNodes(wbsNodeMapper.selectByProjId(projId));
        vo.setAllocations(allocationMapper.selectByProjId(projId));
        return vo;
    }

    public List<ProjInfo> selectProjInfoList(ProjInfo projInfo)
    {
        refreshProjectStatusByDate();
        return projInfoMapper.selectProjInfoList(projInfo);
    }

    @Transactional
    public int saveDraft(ProjInfoFormVO form, String username)
    {
        if (form == null || form.getProjInfo() == null)
        {
            throw new ServiceException("项目信息不能为空");
        }
        ProjInfo info = form.getProjInfo();
        validateDraft(info);
        if (info.getProjId() != null)
        {
            ProjectStatus.require(projInfoMapper.selectProjInfoById(info.getProjId()), ProjectStatus.DRAFT, ProjectStatus.REJECTED);
        }
        info.setStatus(ProjectStatus.DRAFT.code());
        if (StringUtils.isEmpty(info.getProjNo()))
        {
            info.setProjNo(generateProjNo());
        }
        info.setUpdateBy(username);
        if (info.getProjId() == null)
        {
            info.setCreateBy(username);
            projInfoMapper.insertProjInfo(info);
        }
        else
        {
            projInfoMapper.updateProjInfo(info);
        }
        Long projId = info.getProjId();
        wbsNodeMapper.deleteByProjId(projId);
        allocationMapper.deleteByProjId(projId);

        List<ProjWbsNode> nodes = form.getWbsNodes() == null ? new ArrayList<>() : form.getWbsNodes();
        validateNodes(nodes);
        BigDecimal total = BigDecimal.ZERO;
        Map<Long, Long> nodeIdMapping = new HashMap<>();
        for (int i = 0; i < nodes.size(); i++)
        {
            ProjWbsNode node = nodes.get(i);
            Long requestNodeId = node.getNodeId();
            node.setProjId(projId);
            node.setNodeNo(generateNodeNo(i + 1));
            node.setOrderNum(i + 1);
            node.setCreateBy(username);
            node.setUpdateBy(username);
            wbsNodeMapper.insertProjWbsNode(node);
            if (requestNodeId != null)
            {
                nodeIdMapping.put(requestNodeId, node.getNodeId());
            }
            total = total.add(node.getNodeBudget() == null ? BigDecimal.ZERO : node.getNodeBudget());
        }

        List<ProjCostAllocation> allocations = form.getAllocations() == null ? new ArrayList<>() : form.getAllocations();
        for (ProjCostAllocation item : allocations)
        {
            item.setProjId(projId);
            if (item.getNodeId() != null && nodeIdMapping.containsKey(item.getNodeId()))
            {
                item.setNodeId(nodeIdMapping.get(item.getNodeId()));
            }
            item.setCreateBy(username);
            item.setUpdateBy(username);
            allocationMapper.insertProjCostAllocation(item);
        }
        info.setTotalBudget(total);
        projInfoMapper.updateProjInfo(info);
        return 1;
    }

    public int submitForApproval(Long projId, String username)
    {
        ProjInfo info = projInfoMapper.selectProjInfoById(projId);
        ProjectStatus.require(info, ProjectStatus.DRAFT, ProjectStatus.REJECTED);
        if (wbsNodeMapper.selectByProjId(projId).isEmpty())
        {
            throw new ServiceException("请至少维护一个WBS节点后再提交审批");
        }
        info.setStatus(ProjectStatus.PENDING.code());
        info.setUpdateBy(username);
        return projInfoMapper.updateProjInfo(info);
    }

    public int approve(Long projId, String username)
    {
        ProjInfo info = projInfoMapper.selectProjInfoById(projId);
        ProjectStatus.require(info, ProjectStatus.PENDING);
        info.setStatus(resolveApprovedStatusByDate(info));
        info.setUpdateBy(username);
        return projInfoMapper.updateProjInfo(info);
    }

    public int reject(Long projId, String reason, String username)
    {
        ProjInfo info = projInfoMapper.selectProjInfoById(projId);
        ProjectStatus.require(info, ProjectStatus.PENDING);
        if (StringUtils.isEmpty(reason))
        {
            throw new ServiceException("请填写驳回原因");
        }
        info.setStatus(ProjectStatus.REJECTED.code());
        info.setRejectReason(reason);
        info.setUpdateBy(username);
        return projInfoMapper.updateProjInfo(info);
    }

    @Transactional
    public int deleteProjInfoById(Long projId)
    {
        ProjectStatus.require(projInfoMapper.selectProjInfoById(projId), ProjectStatus.DRAFT, ProjectStatus.REJECTED);
        wbsNodeMapper.deleteByProjId(projId);
        allocationMapper.deleteByProjId(projId);
        return projInfoMapper.deleteProjInfoById(projId);
    }

    private String generateProjNo()
    {
        ProjectSequence sequence = new ProjectSequence();
        projInfoMapper.insertProjNoSequence(sequence);
        String date = new SimpleDateFormat("yyyyMMdd").format(new Date());
        return "PRJ-" + date + "-" + String.format("%03d", sequence.getId());
    }

    private String generateNodeNo(int seq)
    {
        return "ND-" + String.format("%03d", seq);
    }

    private void validateDraft(ProjInfo info)
    {
        if (StringUtils.isEmpty(info.getProjName()))
        {
            throw new ServiceException("项目名称不能为空");
        }
        if (info.getCustomerId() == null)
        {
            throw new ServiceException("关联客户不能为空");
        }
        if (info.getContractAmount() == null || info.getContractAmount().compareTo(BigDecimal.ZERO) <= 0)
        {
            throw new ServiceException("预计合同金额必须大于0");
        }
        if (info.getPlanStartDate() != null && info.getPlanEndDate() != null
                && info.getPlanEndDate().before(info.getPlanStartDate()))
        {
            throw new ServiceException("预计竣工日期必须晚于预计开工日期");
        }
    }

    private void refreshProjectStatusByDate()
    {
        projInfoMapper.refreshProjectStatusByDate("system");
    }

    private String resolveApprovedStatusByDate(ProjInfo info)
    {
        Date today = truncateDate(new Date());
        if (info.getPlanEndDate() != null && !truncateDate(info.getPlanEndDate()).after(today))
        {
            return ProjectStatus.COMPLETED.code();
        }
        if (info.getPlanStartDate() != null && !truncateDate(info.getPlanStartDate()).after(today))
        {
            return ProjectStatus.IN_PROGRESS.code();
        }
        return ProjectStatus.APPROVED.code();
    }

    private Date truncateDate(Date date)
    {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

    private void validateNodes(List<ProjWbsNode> nodes)
    {
        if (nodes.isEmpty())
        {
            throw new ServiceException("请至少维护一个WBS节点");
        }
        for (ProjWbsNode node : nodes)
        {
            if (StringUtils.isEmpty(node.getNodeName()))
            {
                throw new ServiceException("WBS节点名称不能为空");
            }
            if (node.getPlanFinishDate() == null)
            {
                throw new ServiceException("WBS节点预计完成日期不能为空");
            }
            if (node.getNodeBudget() == null || node.getNodeBudget().compareTo(BigDecimal.ZERO) < 0)
            {
                throw new ServiceException("WBS节点预算不能小于0");
            }
        }
    }
}
