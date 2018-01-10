/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.tms.db.repository;

import com.amp.tms.db.entity.AgentRecord;
import com.amp.tms.db.entity.AgentStatsRecord;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * 
 */
@Repository
@Transactional
public class AgentStatsRepository {
    
    private static final Logger LOG = LoggerFactory.getLogger(AgentStatsRepository.class);
    
    @PersistenceContext
    private EntityManager entityManager;
    
//    @Autowired
//    private AgentRepository agentRepository;
    
    public void reassignAssociations(AgentRecord oldAgent, AgentRecord newAgent) {
        List<AgentStatsRecord> statList = entityManager.createQuery(
                "select stats "
                + "from AgentStats stats "
                + "where stats.agent.extension = :ext", AgentStatsRecord.class)
                .setParameter("ext", oldAgent.getExtension())
                .getResultList();
        
        for (AgentStatsRecord stats : statList) {
            stats.setAgent(newAgent);
        }
        newAgent.setCurrentAgentStats(oldAgent.getCurrentAgentStats());
        oldAgent.setCurrentAgentStats(null);
    }
    
//    private void associate(AgentRecord agent, AgentStatsRecord stats) {
//        agent.setCurrentAgentStats(stats);
//        stats.setAgent(agent);
//    }
//    
//    public AgentStats startStats(Integer agentExt) {
//        return startStats(agentExt, LocalDateTime.now());
//    }
//    
//    public AgentStats startStats(Integer agentExt, LocalDateTime startTime) {
//        return startStats(agentExt, startTime, null, null);
//    }
//    
//    public AgentStats startStats(Integer agentExt, LocalDateTime startTime, AgentState state, Duration stateThresholdDuration) {
//        AgentRecord agent = agentRepository.getAgent(agentExt);
//        AgentStatsRecord currentStats = agent.getCurrentAgentStats();
//        if (currentStats != null) {
//            AgentStats stats = currentStats.getInfo();
//            if (stats.hasStarted() && !stats.hasStopped()) {
//                return stats;
//            }
//            stats.stopWithRedaction();
//        }
//        
//        AgentStatsRecord stats = new AgentStatsRecord();
//        AgentStats hzStats = new AgentStats();
//        hzStats.start(startTime);
//        if (state != null) {
//            hzStats.setState(state, stateThresholdDuration);
//        }
//        stats.setInfo(hzStats);
//        associate(agent, stats);
//        entityManager.persist(stats);
//        return hzStats;
//    }
    
}
