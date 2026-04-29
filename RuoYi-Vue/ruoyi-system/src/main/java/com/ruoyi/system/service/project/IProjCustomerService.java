package com.ruoyi.system.service.project;

import java.util.List;
import com.ruoyi.system.domain.project.ProjCustomer;

public interface IProjCustomerService
{
    ProjCustomer selectProjCustomerById(Long customerId);
    List<ProjCustomer> selectProjCustomerList(ProjCustomer customer);
    int insertProjCustomer(ProjCustomer customer);
    int updateProjCustomer(ProjCustomer customer);
    int deleteProjCustomerById(Long customerId);
}

