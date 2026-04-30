package com.ruoyi.system.service.project;

import java.util.List;
import com.ruoyi.system.domain.project.SysCostCategory;

public interface ISysCostCategoryService
{
    List<SysCostCategory> selectSysCostCategoryList(SysCostCategory category);

    SysCostCategory selectSysCostCategoryById(Long categoryId);

    int insertSysCostCategory(SysCostCategory category);

    int updateSysCostCategory(SysCostCategory category);

    int deleteSysCostCategoryById(Long categoryId);
}
