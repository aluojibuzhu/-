package com.ruoyi.system.service.project.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.system.domain.project.ProjCustomer;
import com.ruoyi.system.mapper.project.ProjCustomerMapper;
import com.ruoyi.system.mapper.project.ProjInfoMapper;
import com.ruoyi.system.service.project.IProjCustomerService;

@Service
public class ProjCustomerServiceImpl implements IProjCustomerService
{
    @Autowired
    private ProjCustomerMapper customerMapper;
    @Autowired
    private ProjInfoMapper projInfoMapper;
    public ProjCustomer selectProjCustomerById(Long customerId) { return customerMapper.selectProjCustomerById(customerId); }
    public List<ProjCustomer> selectProjCustomerList(ProjCustomer customer) { return customerMapper.selectProjCustomerList(customer); }
    public int insertProjCustomer(ProjCustomer customer) { return customerMapper.insertProjCustomer(customer); }
    public int updateProjCustomer(ProjCustomer customer) {
        int rows = customerMapper.updateProjCustomer(customer);
        projInfoMapper.updateCustomerNameByCustomerId(customer.getCustomerId(), customer.getCustomerName());
        return rows;
    }
    public int deleteProjCustomerById(Long customerId) { return customerMapper.deleteProjCustomerById(customerId); }
}
