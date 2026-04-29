package com.ruoyi.system.domain.vo.project;

import java.util.List;
import com.ruoyi.system.domain.project.ProjCostAllocation;
import com.ruoyi.system.domain.project.ProjInfo;
import com.ruoyi.system.domain.project.ProjWbsNode;

public class ProjInfoFormVO
{
    private ProjInfo projInfo;
    private List<ProjWbsNode> wbsNodes;
    private List<ProjCostAllocation> allocations;
    public ProjInfo getProjInfo() { return projInfo; }
    public void setProjInfo(ProjInfo projInfo) { this.projInfo = projInfo; }
    public List<ProjWbsNode> getWbsNodes() { return wbsNodes; }
    public void setWbsNodes(List<ProjWbsNode> wbsNodes) { this.wbsNodes = wbsNodes; }
    public List<ProjCostAllocation> getAllocations() { return allocations; }
    public void setAllocations(List<ProjCostAllocation> allocations) { this.allocations = allocations; }
}
