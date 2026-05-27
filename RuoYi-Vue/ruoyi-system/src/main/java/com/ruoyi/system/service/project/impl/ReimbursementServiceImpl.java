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
import com.ruoyi.system.domain.project.Reimbursement;
import com.ruoyi.system.domain.project.ReimbursementAttachment;
import com.ruoyi.system.domain.project.ReimbursementSequence;
import com.ruoyi.system.domain.project.ReimbursementStatus;
import com.ruoyi.system.domain.project.SysCostCategory;
import com.ruoyi.system.domain.vo.project.ReimbursementFormVO;
import com.ruoyi.system.mapper.project.ProjInfoMapper;
import com.ruoyi.system.mapper.project.ProjWbsNodeMapper;
import com.ruoyi.system.mapper.project.ReimbursementAttachmentMapper;
import com.ruoyi.system.mapper.project.ReimbursementMapper;
import com.ruoyi.system.mapper.project.SysCostCategoryMapper;
import com.ruoyi.system.service.project.ICostApprovalService;
import com.ruoyi.system.service.project.IReimbursementService;

@Service
public class ReimbursementServiceImpl implements IReimbursementService
{
    @Autowired
    private ReimbursementMapper reimbursementMapper;
    @Autowired
    private ReimbursementAttachmentMapper attachmentMapper;
    @Autowired
    private ProjInfoMapper projInfoMapper;
    @Autowired
    private ProjWbsNodeMapper wbsNodeMapper;
    @Autowired
    private SysCostCategoryMapper categoryMapper;
    @Autowired
    private ICostApprovalService costApprovalService;

    public List<Reimbursement> selectReimbursementList(Reimbursement reimbursement)
    {
        refreshProjectStatusByDate();
        return reimbursementMapper.selectReimbursementList(reimbursement);
    }

    public ReimbursementFormVO getReimbursementForm(Long reimburseId)
    {
        refreshProjectStatusByDate();
        ReimbursementFormVO vo = new ReimbursementFormVO();
        vo.setReimbursement(reimbursementMapper.selectReimbursementById(reimburseId));
        vo.setAttachments(attachmentMapper.selectByReimburseId(reimburseId));
        return vo;
    }

    public List<ProjWbsNode> selectWbsNodesByProjId(Long projId)
    {
        refreshProjectStatusByDate();
        ProjInfo info = projInfoMapper.selectProjInfoById(projId);
        ProjectStatus.require(info, ProjectStatus.APPROVED, ProjectStatus.IN_PROGRESS, ProjectStatus.COMPLETED);
        return wbsNodeMapper.selectByProjId(projId);
    }

    @Transactional
    public Reimbursement saveDraft(ReimbursementFormVO form, String username)
    {
        if (form == null || form.getReimbursement() == null)
        {
            throw new ServiceException("报销信息不能为空");
        }
        Reimbursement reimbursement = form.getReimbursement();
        if (reimbursement.getReimburseId() != null)
        {
            ReimbursementStatus.require(reimbursementMapper.selectReimbursementById(reimbursement.getReimburseId()), ReimbursementStatus.DRAFT, ReimbursementStatus.REJECTED);
        }
        fillAndValidate(reimbursement, form.getAttachments());
        reimbursement.setStatus(ReimbursementStatus.DRAFT.code());
        if (StringUtils.isEmpty(reimbursement.getReimburseNo()))
        {
            reimbursement.setReimburseNo(generateReimburseNo());
        }
        reimbursement.setUpdateBy(username);
        if (reimbursement.getReimburseId() == null)
        {
            reimbursement.setCreateBy(username);
            reimbursementMapper.insertReimbursement(reimbursement);
        }
        else
        {
            reimbursementMapper.updateReimbursement(reimbursement);
        }
        saveAttachments(reimbursement.getReimburseId(), form.getAttachments(), username);
        return reimbursement;
    }

    public int submitForApproval(Long reimburseId, String username)
    {
        Reimbursement reimbursement = reimbursementMapper.selectReimbursementById(reimburseId);
        ReimbursementStatus.require(reimbursement, ReimbursementStatus.DRAFT, ReimbursementStatus.REJECTED);
        reimbursement.setStatus(ReimbursementStatus.PENDING.code());
        reimbursement.setSubmitTime(new Date());
        reimbursement.setUpdateBy(username);
        return reimbursementMapper.updateReimbursement(reimbursement);
    }

    public int approve(Long reimburseId, String username)
    {
        Reimbursement reimbursement = reimbursementMapper.selectReimbursementById(reimburseId);
        ReimbursementStatus.require(reimbursement, ReimbursementStatus.PENDING);
        reimbursement.setStatus(ReimbursementStatus.APPROVED.code());
        reimbursement.setApproveBy(username);
        reimbursement.setApproveTime(new Date());
        reimbursement.setUpdateBy(username);
        return reimbursementMapper.updateReimbursement(reimbursement);
    }

    public int reject(Long reimburseId, String reason, String username)
    {
        Reimbursement reimbursement = reimbursementMapper.selectReimbursementById(reimburseId);
        ReimbursementStatus.require(reimbursement, ReimbursementStatus.PENDING);
        if (StringUtils.isEmpty(reason))
        {
            throw new ServiceException("请填写驳回原因");
        }
        reimbursement.setStatus(ReimbursementStatus.REJECTED.code());
        reimbursement.setRejectReason(reason);
        reimbursement.setApproveBy(username);
        reimbursement.setApproveTime(new Date());
        reimbursement.setUpdateBy(username);
        return reimbursementMapper.updateReimbursement(reimbursement);
    }

    public int postCost(Long reimburseId, String username)
    {
        return costApprovalService.postApproved(CostApprovalServiceImpl.BILL_TYPE_REIMBURSEMENT, reimburseId, username);
    }

    public int deleteReimbursementById(Long reimburseId)
    {
        ReimbursementStatus.require(reimbursementMapper.selectReimbursementById(reimburseId), ReimbursementStatus.DRAFT, ReimbursementStatus.REJECTED);
        attachmentMapper.deleteByReimburseId(reimburseId);
        return reimbursementMapper.deleteReimbursementById(reimburseId);
    }

    private void fillAndValidate(Reimbursement reimbursement, List<ReimbursementAttachment> attachments)
    {
        refreshProjectStatusByDate();
        ProjInfo info = projInfoMapper.selectProjInfoById(reimbursement.getProjId());
        ProjectStatus.require(info, ProjectStatus.APPROVED, ProjectStatus.IN_PROGRESS, ProjectStatus.COMPLETED);
        ProjWbsNode node = wbsNodeMapper.selectByNodeId(reimbursement.getNodeId());
        if (node == null || !reimbursement.getProjId().equals(node.getProjId()))
        {
            throw new ServiceException("WBS节点与项目不匹配");
        }
        if (StringUtils.isEmpty(reimbursement.getExpenseType()))
        {
            throw new ServiceException("成本科目不能为空");
        }
        SysCostCategory rootCategory = categoryMapper.selectRootCategoryByName(reimbursement.getExpenseType());
        if (rootCategory == null || !"0".equals(rootCategory.getStatus()))
        {
            throw new ServiceException("请选择有效一级成本科目");
        }
        SysCostCategory childCategory = null;
        if (reimbursement.getCategoryId() != null)
        {
            childCategory = categoryMapper.selectSysCostCategoryById(reimbursement.getCategoryId());
            if (childCategory == null || !"0".equals(childCategory.getStatus()) || childCategory.getCategoryLevel() == null || childCategory.getCategoryLevel() != 2)
            {
                throw new ServiceException("请选择有效二级成本科目");
            }
            if (childCategory.getParentId() == null || !childCategory.getParentId().equals(rootCategory.getCategoryId()))
            {
                throw new ServiceException("二级成本科目需归属于所选成本科目");
            }
        }
        if (reimbursement.getExpenseDate() == null)
        {
            throw new ServiceException("发生日期不能为空");
        }
        if (reimbursement.getAmount() == null || reimbursement.getAmount().compareTo(BigDecimal.ZERO) <= 0)
        {
            throw new ServiceException("报销金额必须大于0");
        }
        if (StringUtils.isEmpty(reimbursement.getExpenseDesc()))
        {
            throw new ServiceException("费用说明不能为空");
        }
        if (reimbursement.getExpenseDesc().length() > 200)
        {
            throw new ServiceException("费用说明不能超过200字");
        }
        if (reimbursement.getInvoiceCount() == null || reimbursement.getInvoiceCount() < 1)
        {
            throw new ServiceException("发票张数至少为1");
        }
        if (attachments == null || attachments.stream().noneMatch(item -> item != null && StringUtils.isNotEmpty(item.getFilePath())))
        {
            throw new ServiceException("请上传发票附件");
        }
        reimbursement.setAmount(reimbursement.getAmount().setScale(2, RoundingMode.HALF_UP));
        reimbursement.setProjName(info.getProjName());
        reimbursement.setNodeName(node.getNodeName());
        reimbursement.setExpenseType(rootCategory.getCategoryName());
        reimbursement.setCategoryName(childCategory == null ? null : childCategory.getCategoryName());
    }

    private void refreshProjectStatusByDate()
    {
        projInfoMapper.refreshProjectStatusByDate("system");
    }

    private void saveAttachments(Long reimburseId, List<ReimbursementAttachment> attachments, String username)
    {
        attachmentMapper.deleteByReimburseId(reimburseId);
        List<ReimbursementAttachment> list = attachments == null ? new ArrayList<>() : attachments;
        for (ReimbursementAttachment attachment : list)
        {
            if (attachment == null || StringUtils.isEmpty(attachment.getFilePath()))
            {
                continue;
            }
            attachment.setReimburseId(reimburseId);
            attachment.setFileName(StringUtils.isEmpty(attachment.getFileName()) ? attachment.getFilePath() : attachment.getFileName());
            attachment.setOriginalName(StringUtils.isEmpty(attachment.getOriginalName()) ? attachment.getFileName() : attachment.getOriginalName());
            attachment.setCreateBy(username);
            attachmentMapper.insertReimbursementAttachment(attachment);
        }
    }

    private String generateReimburseNo()
    {
        ReimbursementSequence sequence = new ReimbursementSequence();
        reimbursementMapper.insertReimbursementSequence(sequence);
        String date = new SimpleDateFormat("yyyyMMdd").format(new Date());
        return "EXP-" + date + "-" + String.format("%03d", sequence.getId());
    }
}
