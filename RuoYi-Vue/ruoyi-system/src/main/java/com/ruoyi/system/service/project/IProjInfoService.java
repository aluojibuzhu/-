package com.ruoyi.system.service.project;

import java.util.List;
import com.ruoyi.system.domain.project.ProjInfo;
import com.ruoyi.system.domain.vo.project.ProjInfoFormVO;

public interface IProjInfoService
{
    ProjInfoFormVO getProjForm(Long projId);
    List<ProjInfo> selectProjInfoList(ProjInfo projInfo);
    int saveDraft(ProjInfoFormVO form, String username);
    int submitForApproval(Long projId, String username);
    int approve(Long projId, String username);
    int reject(Long projId, String reason, String username);
    int deleteProjInfoById(Long projId);
}

