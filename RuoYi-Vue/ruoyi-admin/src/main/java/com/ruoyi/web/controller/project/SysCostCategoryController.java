package com.ruoyi.web.controller.project;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.system.domain.project.SysCostCategory;
import com.ruoyi.system.service.project.ISysCostCategoryService;

@RestController
@RequestMapping("/system/costCategory")
public class SysCostCategoryController extends BaseController
{
    @Autowired
    private ISysCostCategoryService categoryService;

    @PreAuthorize("@ss.hasPermi('project:costCategory:list')")
    @GetMapping("/list")
    public AjaxResult list()
    {
        return success(categoryService.selectSysCostCategoryList(new SysCostCategory()));
    }
}
