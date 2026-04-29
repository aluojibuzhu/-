package com.ruoyi.system.domain.project;

import com.ruoyi.common.core.domain.BaseEntity;

public class ProjCustomer extends BaseEntity
{
    private static final long serialVersionUID = 1L;
    private Long customerId;
    private String customerName;
    private String contactPerson;
    private String contactPhone;
    private String address;
    private String remark;
    private String status;
    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }
    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    public String getContactPerson() { return contactPerson; }
    public void setContactPerson(String contactPerson) { this.contactPerson = contactPerson; }
    public String getContactPhone() { return contactPhone; }
    public void setContactPhone(String contactPhone) { this.contactPhone = contactPhone; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}

