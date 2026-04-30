package com.ruoyi.system.domain.project;

import com.ruoyi.common.core.domain.BaseEntity;

public class WorkHourAttachment extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long attachmentId;
    private Long whId;
    private String fileName;
    private String originalName;
    private String filePath;
    private Long fileSize;
    private String fileType;

    public Long getAttachmentId() { return attachmentId; }
    public void setAttachmentId(Long attachmentId) { this.attachmentId = attachmentId; }
    public Long getWhId() { return whId; }
    public void setWhId(Long whId) { this.whId = whId; }
    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }
    public String getOriginalName() { return originalName; }
    public void setOriginalName(String originalName) { this.originalName = originalName; }
    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }
    public Long getFileSize() { return fileSize; }
    public void setFileSize(Long fileSize) { this.fileSize = fileSize; }
    public String getFileType() { return fileType; }
    public void setFileType(String fileType) { this.fileType = fileType; }
}
