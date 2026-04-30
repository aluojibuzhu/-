package com.ruoyi.system.service.project;

import java.util.List;
import com.ruoyi.system.domain.project.ProjWbsNode;
import com.ruoyi.system.domain.project.Reimbursement;
import com.ruoyi.system.domain.vo.project.ReimbursementFormVO;

public interface IReimbursementService
{
    List<Reimbursement> selectReimbursementList(Reimbursement reimbursement);

    ReimbursementFormVO getReimbursementForm(Long reimburseId);

    List<ProjWbsNode> selectWbsNodesByProjId(Long projId);

    Reimbursement saveDraft(ReimbursementFormVO form, String username);

    int submitForApproval(Long reimburseId, String username);

    int approve(Long reimburseId, String username);

    int reject(Long reimburseId, String reason, String username);

    int postCost(Long reimburseId, String username);

    int deleteReimbursementById(Long reimburseId);
}
