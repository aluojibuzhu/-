package com.ruoyi.system.service.project.impl;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
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
        ProjInfoFormVO vo = new ProjInfoFormVO();
        vo.setProjInfo(projInfoMapper.selectProjInfoById(projId));
        vo.setWbsNodes(wbsNodeMapper.selectByProjId(projId));
        vo.setAllocations(allocationMapper.selectByProjId(projId));
        return vo;
    }

    public List<ProjInfo> selectProjInfoList(ProjInfo projInfo) { return projInfoMapper.selectProjInfoList(projInfo); }

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
        BigDecimal total = BigDecimal.ZERO;
        for (int i = 0; i < nodes.size(); i++)
        {
            ProjWbsNode node = nodes.get(i);
            node.setProjId(projId);
            node.setNodeNo(generateNodeNo(i + 1));
            node.setOrderNum(i + 1);
            node.setCreateBy(username);
            node.setUpdateBy(username);
            wbsNodeMapper.insertProjWbsNode(node);
            total = total.add(node.getNodeBudget() == null ? BigDecimal.ZERO : node.getNodeBudget());
        }

        List<ProjCostAllocation> allocations = form.getAllocations() == null ? new ArrayList<>() : form.getAllocations();
        for (ProjCostAllocation item : allocations)
        {
            item.setProjId(projId);
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
        info.setStatus(ProjectStatus.APPROVED.code());
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
        if (info.getPlanStartDate() != null && info.getPlanEndDate() != null
                && info.getPlanEndDate().before(info.getPlanStartDate()))
        {
            throw new ServiceException("预计竣工日期必须晚于预计开工日期");
        }
    }
}
