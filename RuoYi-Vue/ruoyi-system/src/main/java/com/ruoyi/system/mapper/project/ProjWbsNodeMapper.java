package com.ruoyi.system.mapper.project;

import java.util.List;
import java.math.BigDecimal;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.system.domain.project.ProjWbsNode;

public interface ProjWbsNodeMapper
{
    List<ProjWbsNode> selectByProjId(Long projId);
    ProjWbsNode selectByNodeId(Long nodeId);
    int insertProjWbsNode(ProjWbsNode node);
    int deleteByProjId(Long projId);
    int increaseActualCost(@Param("nodeId") Long nodeId, @Param("amount") BigDecimal amount, @Param("username") String username);
}
