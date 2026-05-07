package com.ruoyi.system.service.project;

import java.util.List;
import com.ruoyi.system.domain.project.CostApprovalBill;
import com.ruoyi.system.domain.vo.project.CostApprovalSummaryVO;

public interface ICostApprovalService
{
    List<CostApprovalBill> selectApprovalBillList(CostApprovalBill bill);

    CostApprovalSummaryVO selectSummary();

    int approveAndPost(String billType, Long billId, String username);

    int reject(String billType, Long billId, String reason, String username);

    int postApproved(String billType, Long billId, String username);
}
