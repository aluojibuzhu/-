package com.ruoyi.system.mapper.project;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import java.util.Date;
import com.ruoyi.system.domain.project.CostApprovalBill;
import com.ruoyi.system.domain.project.CostPostingRecord;
import com.ruoyi.system.domain.vo.project.CostApprovalSummaryVO;

public interface CostApprovalMapper
{
    List<CostApprovalBill> selectApprovalBillList(CostApprovalBill bill);

    CostApprovalSummaryVO selectSummary();

    int insertPostingRecord(CostPostingRecord record);

    int existsPostingRecord(@Param("billType") String billType, @Param("billId") Long billId);

    Date selectLastPostingTime(Long projId);
}
