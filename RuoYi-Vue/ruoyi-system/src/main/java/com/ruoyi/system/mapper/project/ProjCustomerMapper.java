package com.ruoyi.system.mapper.project;

import java.util.List;
import com.ruoyi.system.domain.project.ProjCustomer;

public interface ProjCustomerMapper
{
    ProjCustomer selectProjCustomerById(Long customerId);
    List<ProjCustomer> selectProjCustomerList(ProjCustomer customer);
    int insertProjCustomer(ProjCustomer customer);
    int updateProjCustomer(ProjCustomer customer);
    int deleteProjCustomerById(Long customerId);
}

