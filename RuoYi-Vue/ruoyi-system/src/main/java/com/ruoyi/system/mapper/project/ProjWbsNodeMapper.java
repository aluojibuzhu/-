package com.ruoyi.system.mapper.project;

import java.util.List;
import com.ruoyi.system.domain.project.ProjWbsNode;

public interface ProjWbsNodeMapper
{
    List<ProjWbsNode> selectByProjId(Long projId);
    ProjWbsNode selectByNodeId(Long nodeId);
    int insertProjWbsNode(ProjWbsNode node);
    int deleteByProjId(Long projId);
}
