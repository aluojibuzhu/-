package com.ruoyi.web.controller.project;

import java.util.List;
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
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.system.domain.project.ProjCustomer;
import com.ruoyi.system.service.project.IProjCustomerService;

@RestController
@RequestMapping("/project/customer")
public class ProjCustomerController extends BaseController
{
    @Autowired
    private IProjCustomerService customerService;

    @PreAuthorize("@ss.hasPermi('project:customer:list')")
    @GetMapping("/list")
    public TableDataInfo list(ProjCustomer customer)
    {
        startPage();
        List<ProjCustomer> list = customerService.selectProjCustomerList(customer);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('project:customer:query')")
    @GetMapping("/{customerId}")
    public AjaxResult getInfo(@PathVariable Long customerId) { return success(customerService.selectProjCustomerById(customerId)); }

    @PreAuthorize("@ss.hasPermi('project:customer:add')")
    @PostMapping
    public AjaxResult add(@RequestBody ProjCustomer customer)
    {
        customer.setCreateBy(getUsername());
        customer.setUpdateBy(getUsername());
        customerService.insertProjCustomer(customer);
        return success(customer);
    }

    @PreAuthorize("@ss.hasPermi('project:customer:edit')")
    @PutMapping
    public AjaxResult edit(@RequestBody ProjCustomer customer)
    {
        customer.setUpdateBy(getUsername());
        return toAjax(customerService.updateProjCustomer(customer));
    }

    @PreAuthorize("@ss.hasPermi('project:customer:remove')")
    @DeleteMapping("/{customerId}")
    public AjaxResult remove(@PathVariable Long customerId) { return toAjax(customerService.deleteProjCustomerById(customerId)); }
}
