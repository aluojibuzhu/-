package com.ruoyi.web.controller.project;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
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
    public AjaxResult list(SysCostCategory category)
    {
        return success(categoryService.selectSysCostCategoryList(category));
    }

    @PreAuthorize("@ss.hasPermi('project:costCategory:query')")
    @GetMapping("/{categoryId}")
    public AjaxResult getInfo(@PathVariable Long categoryId)
    {
        return success(categoryService.selectSysCostCategoryById(categoryId));
    }

    @PreAuthorize("@ss.hasPermi('project:costCategory:add')")
    @PostMapping
    public AjaxResult add(@RequestBody SysCostCategory category)
    {
        category.setCreateBy(getUsername());
        category.setUpdateBy(getUsername());
        return toAjax(categoryService.insertSysCostCategory(category));
    }

    @PreAuthorize("@ss.hasPermi('project:costCategory:edit')")
    @PutMapping
    public AjaxResult edit(@RequestBody SysCostCategory category)
    {
        category.setUpdateBy(getUsername());
        return toAjax(categoryService.updateSysCostCategory(category));
    }

    @PreAuthorize("@ss.hasPermi('project:costCategory:remove')")
    @DeleteMapping("/{categoryId}")
    public AjaxResult remove(@PathVariable Long categoryId)
    {
        return toAjax(categoryService.deleteSysCostCategoryById(categoryId));
    }
}
