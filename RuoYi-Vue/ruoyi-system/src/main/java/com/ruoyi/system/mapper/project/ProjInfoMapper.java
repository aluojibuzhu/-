package com.ruoyi.system.mapper.project;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.system.domain.project.ProjectSequence;
import com.ruoyi.system.domain.project.ProjInfo;

public interface ProjInfoMapper
{
    ProjInfo selectProjInfoById(Long projId);
    List<ProjInfo> selectProjInfoList(ProjInfo projInfo);
    int insertProjInfo(ProjInfo projInfo);
    int updateProjInfo(ProjInfo projInfo);
    int deleteProjInfoById(Long projId);
    int insertProjNoSequence(ProjectSequence sequence);
    int updateCustomerNameByCustomerId(@Param("customerId") Long customerId, @Param("customerName") String customerName);
}
