package com.ruoyi.system.service.project.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.system.domain.project.SysCostCategory;
import com.ruoyi.system.mapper.project.SysCostCategoryMapper;
import com.ruoyi.system.service.project.ISysCostCategoryService;

@Service
public class SysCostCategoryServiceImpl implements ISysCostCategoryService
{
    @Autowired
    private SysCostCategoryMapper mapper;

    public List<SysCostCategory> selectSysCostCategoryList(SysCostCategory category) { return mapper.selectSysCostCategoryList(category); }

    public SysCostCategory selectSysCostCategoryById(Long categoryId) { return mapper.selectSysCostCategoryById(categoryId); }

    public int insertSysCostCategory(SysCostCategory category)
    {
        prepareCategory(category);
        return mapper.insertSysCostCategory(category);
    }

    public int updateSysCostCategory(SysCostCategory category)
    {
        prepareCategory(category);
        if (category.getCategoryId() != null && category.getCategoryId().equals(category.getParentId()))
        {
            throw new ServiceException("上级科目不能选择自己");
        }
        return mapper.updateSysCostCategory(category);
    }

    public int deleteSysCostCategoryById(Long categoryId)
    {
        if (mapper.hasChildByCategoryId(categoryId) > 0)
        {
            throw new ServiceException("存在下级成本科目，不允许删除");
        }
        return mapper.deleteSysCostCategoryById(categoryId);
    }

    private void prepareCategory(SysCostCategory category)
    {
        if (category == null || StringUtils.isEmpty(category.getCategoryName()))
        {
            throw new ServiceException("成本科目名称不能为空");
        }
        if (category.getParentId() == null)
        {
            category.setParentId(0L);
        }
        if (category.getParentId() != 0L)
        {
            SysCostCategory parent = mapper.selectSysCostCategoryById(category.getParentId());
            if (parent == null || parent.getCategoryLevel() == null || parent.getCategoryLevel() != 1)
            {
                throw new ServiceException("成本科目仅支持维护到二级");
            }
        }
        category.setCategoryLevel(category.getParentId() == 0L ? 1 : 2);
        if (category.getOrderNum() == null)
        {
            category.setOrderNum(0);
        }
        if (StringUtils.isEmpty(category.getStatus()))
        {
            category.setStatus("0");
        }
    }
}
