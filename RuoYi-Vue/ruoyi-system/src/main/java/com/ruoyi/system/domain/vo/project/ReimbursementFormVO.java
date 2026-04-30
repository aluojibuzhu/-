package com.ruoyi.system.domain.vo.project;

import java.util.List;
import com.ruoyi.system.domain.project.Reimbursement;
import com.ruoyi.system.domain.project.ReimbursementAttachment;

public class ReimbursementFormVO
{
    private Reimbursement reimbursement;
    private List<ReimbursementAttachment> attachments;

    public Reimbursement getReimbursement() { return reimbursement; }
    public void setReimbursement(Reimbursement reimbursement) { this.reimbursement = reimbursement; }
    public List<ReimbursementAttachment> getAttachments() { return attachments; }
    public void setAttachments(List<ReimbursementAttachment> attachments) { this.attachments = attachments; }
}
