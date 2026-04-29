package com.ruoyi.system.service.project.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.system.domain.project.SysCostCategory;
import com.ruoyi.system.mapper.project.SysCostCategoryMapper;
import com.ruoyi.system.service.project.ISysCostCategoryService;

@Service
public class SysCostCategoryServiceImpl implements ISysCostCategoryService
{
    @Autowired
    private SysCostCategoryMapper mapper;
    public List<SysCostCategory> selectSysCostCategoryList(SysCostCategory category) { return mapper.selectSysCostCategoryList(category); }
}

