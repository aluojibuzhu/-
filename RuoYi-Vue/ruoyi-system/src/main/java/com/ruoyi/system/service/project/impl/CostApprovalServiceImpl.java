package com.ruoyi.system.service.project.impl;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.system.domain.project.CostApprovalBill;
import com.ruoyi.system.domain.project.CostPostingRecord;
import com.ruoyi.system.domain.project.ProjCostAllocation;
import com.ruoyi.system.domain.project.Reimbursement;
import com.ruoyi.system.domain.project.ReimbursementStatus;
import com.ruoyi.system.domain.project.WorkHour;
import com.ruoyi.system.domain.project.WorkHourStatus;
import com.ruoyi.system.domain.vo.project.CostApprovalSummaryVO;
import com.ruoyi.system.mapper.project.CostApprovalMapper;
import com.ruoyi.system.mapper.project.ProjCostAllocationMapper;
import com.ruoyi.system.mapper.project.ProjInfoMapper;
import com.ruoyi.system.mapper.project.ProjWbsNodeMapper;
import com.ruoyi.system.mapper.project.ReimbursementMapper;
import com.ruoyi.system.mapper.project.WorkHourMapper;
import com.ruoyi.system.service.project.ICostApprovalService;

@Service
public class CostApprovalServiceImpl implements ICostApprovalService
{
    public static final String BILL_TYPE_WORK_HOUR = "WORK_HOUR";
    public static final String BILL_TYPE_REIMBURSEMENT = "REIMBURSEMENT";

    @Autowired
    private CostApprovalMapper costApprovalMapper;
    @Autowired
    private WorkHourMapper workHourMapper;
    @Autowired
    private ReimbursementMapper reimbursementMapper;
    @Autowired
    private ProjInfoMapper projInfoMapper;
    @Autowired
    private ProjWbsNodeMapper wbsNodeMapper;
    @Autowired
    private ProjCostAllocationMapper allocationMapper;

    public List<CostApprovalBill> selectApprovalBillList(CostApprovalBill bill)
    {
        return costApprovalMapper.selectApprovalBillList(bill);
    }

    public CostApprovalSummaryVO selectSummary()
    {
        return costApprovalMapper.selectSummary();
    }

    @Transactional
    public int approveAndPost(String billType, Long billId, String username)
    {
        if (BILL_TYPE_WORK_HOUR.equals(billType))
        {
            WorkHour workHour = workHourMapper.selectWorkHourById(billId);
            WorkHourStatus.require(workHour, WorkHourStatus.PENDING);
            workHour.setStatus(WorkHourStatus.POSTED.code());
            workHour.setApproveBy(username);
            workHour.setApproveTime(new Date());
            workHour.setUpdateBy(username);
            postCost(toRecord(workHour, username));
            return workHourMapper.updateWorkHour(workHour);
        }
        Reimbursement reimbursement = getReimbursementForType(billType, billId);
        ReimbursementStatus.require(reimbursement, ReimbursementStatus.PENDING);
        reimbursement.setStatus(ReimbursementStatus.POSTED.code());
        reimbursement.setApproveBy(username);
        reimbursement.setApproveTime(new Date());
        reimbursement.setUpdateBy(username);
        postCost(toRecord(reimbursement, username));
        return reimbursementMapper.updateReimbursement(reimbursement);
    }

    @Transactional
    public int reject(String billType, Long billId, String reason, String username)
    {
        if (StringUtils.isEmpty(reason))
        {
            throw new ServiceException("请填写驳回原因");
        }
        if (BILL_TYPE_WORK_HOUR.equals(billType))
        {
            WorkHour workHour = workHourMapper.selectWorkHourById(billId);
            WorkHourStatus.require(workHour, WorkHourStatus.PENDING);
            workHour.setStatus(WorkHourStatus.REJECTED.code());
            workHour.setRejectReason(reason);
            workHour.setApproveBy(username);
            workHour.setApproveTime(new Date());
            workHour.setUpdateBy(username);
            return workHourMapper.updateWorkHour(workHour);
        }
        Reimbursement reimbursement = getReimbursementForType(billType, billId);
        ReimbursementStatus.require(reimbursement, ReimbursementStatus.PENDING);
        reimbursement.setStatus(ReimbursementStatus.REJECTED.code());
        reimbursement.setRejectReason(reason);
        reimbursement.setApproveBy(username);
        reimbursement.setApproveTime(new Date());
        reimbursement.setUpdateBy(username);
        return reimbursementMapper.updateReimbursement(reimbursement);
    }

    @Transactional
    public int postApproved(String billType, Long billId, String username)
    {
        if (BILL_TYPE_WORK_HOUR.equals(billType))
        {
            WorkHour workHour = workHourMapper.selectWorkHourById(billId);
            WorkHourStatus.require(workHour, WorkHourStatus.APPROVED);
            workHour.setStatus(WorkHourStatus.POSTED.code());
            workHour.setUpdateBy(username);
            postCost(toRecord(workHour, username));
            return workHourMapper.updateWorkHour(workHour);
        }
        Reimbursement reimbursement = getReimbursementForType(billType, billId);
        ReimbursementStatus.require(reimbursement, ReimbursementStatus.APPROVED);
        reimbursement.setStatus(ReimbursementStatus.POSTED.code());
        reimbursement.setUpdateBy(username);
        postCost(toRecord(reimbursement, username));
        return reimbursementMapper.updateReimbursement(reimbursement);
    }

    private Reimbursement getReimbursementForType(String billType, Long billId)
    {
        if (!BILL_TYPE_REIMBURSEMENT.equals(billType))
        {
            throw new ServiceException("不支持的单据类型");
        }
        return reimbursementMapper.selectReimbursementById(billId);
    }

    private void postCost(CostPostingRecord record)
    {
        if (costApprovalMapper.existsPostingRecord(record.getBillType(), record.getBillId()) > 0)
        {
            throw new ServiceException("该单据已入账");
        }
        BigDecimal amount = record.getAmount() == null ? BigDecimal.ZERO : record.getAmount();
        projInfoMapper.increaseActualCost(record.getProjId(), amount, record.getPostBy());
        wbsNodeMapper.increaseActualCost(record.getNodeId(), amount, record.getPostBy());
        allocationMapper.increaseActualCost(record.getProjId(), record.getNodeId(), record.getCategoryId(), amount, record.getPostBy());
        costApprovalMapper.insertPostingRecord(record);
    }

    private CostPostingRecord toRecord(WorkHour workHour, String username)
    {
        CostPostingRecord record = new CostPostingRecord();
        record.setBillType(BILL_TYPE_WORK_HOUR);
        record.setBillId(workHour.getWhId());
        record.setBillNo(workHour.getWhNo());
        record.setProjId(workHour.getProjId());
        record.setProjName(workHour.getProjName());
        record.setNodeId(workHour.getNodeId());
        record.setNodeName(workHour.getNodeName());
        record.setCategoryId(workHour.getCategoryId());
        record.setCategoryName(workHour.getCategoryName());
        record.setAmount(workHour.getWorkCost());
        record.setPostBy(username);
        record.setPostTime(new Date());
        record.setCreateBy(username);
        return record;
    }

    private CostPostingRecord toRecord(Reimbursement reimbursement, String username)
    {
        CostPostingRecord record = new CostPostingRecord();
        record.setBillType(BILL_TYPE_REIMBURSEMENT);
        record.setBillId(reimbursement.getReimburseId());
        record.setBillNo(reimbursement.getReimburseNo());
        record.setProjId(reimbursement.getProjId());
        record.setProjName(reimbursement.getProjName());
        record.setNodeId(reimbursement.getNodeId());
        record.setNodeName(reimbursement.getNodeName());
        record.setCategoryId(reimbursement.getCategoryId());
        record.setCategoryName(reimbursement.getCategoryName());
        record.setAmount(reimbursement.getAmount());
        record.setPostBy(username);
        record.setPostTime(new Date());
        record.setCreateBy(username);
        return record;
    }
}
