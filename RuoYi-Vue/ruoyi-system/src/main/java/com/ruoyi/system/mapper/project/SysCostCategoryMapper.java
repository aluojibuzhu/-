package com.ruoyi.system.mapper.project;

import java.util.List;
import com.ruoyi.system.domain.project.SysCostCategory;

public interface SysCostCategoryMapper
{
    List<SysCostCategory> selectSysCostCategoryList(SysCostCategory category);

    SysCostCategory selectSysCostCategoryById(Long categoryId);

    SysCostCategory selectRootCategoryByName(String categoryName);

    int insertSysCostCategory(SysCostCategory category);

    int updateSysCostCategory(SysCostCategory category);

    int deleteSysCostCategoryById(Long categoryId);

    int hasChildByCategoryId(Long categoryId);
}
