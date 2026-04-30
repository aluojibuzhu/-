package com.ruoyi.system.mapper.project;

import java.util.List;
import com.ruoyi.system.domain.project.Reimbursement;
import com.ruoyi.system.domain.project.ReimbursementSequence;

public interface ReimbursementMapper
{
    List<Reimbursement> selectReimbursementList(Reimbursement reimbursement);

    Reimbursement selectReimbursementById(Long reimburseId);

    int insertReimbursement(Reimbursement reimbursement);

    int updateReimbursement(Reimbursement reimbursement);

    int deleteReimbursementById(Long reimburseId);

    int insertReimbursementSequence(ReimbursementSequence sequence);
}
