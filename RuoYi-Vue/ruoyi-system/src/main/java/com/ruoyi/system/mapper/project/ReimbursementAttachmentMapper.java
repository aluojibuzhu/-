package com.ruoyi.system.mapper.project;

import java.util.List;
import com.ruoyi.system.domain.project.ReimbursementAttachment;

public interface ReimbursementAttachmentMapper
{
    List<ReimbursementAttachment> selectByReimburseId(Long reimburseId);

    int insertReimbursementAttachment(ReimbursementAttachment attachment);

    int deleteByReimburseId(Long reimburseId);
}
