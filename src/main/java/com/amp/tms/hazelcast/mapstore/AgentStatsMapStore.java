/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.tms.hazelcast.mapstore;

import com.hazelcast.core.MapStore;
import com.hazelcast.core.PostProcessingMapStore;
import com.amp.tms.db.entity.AgentRecord;
import com.amp.tms.db.entity.AgentStatsRecord;
import com.amp.tms.db.repository.AgentRepository;
import static com.amp.tms.hazelcast.Configs.AGENT_STATS_MAP_STORE_BEAN_NAME;
import com.amp.tms.hazelcast.entity.AgentStats;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * 
 */
@Repository(AGENT_STATS_MAP_STORE_BEAN_NAME)
public class AgentStatsMapStore implements MapStore<Integer, AgentStats>, PostProcessingMapStore {

    private static final Logger LOG = LoggerFactory.getLogger(AgentStatsMapStore.class);

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private AgentRepository agentRepository;

    private void associate(AgentRecord agent, AgentStatsRecord stats) {
        agent.setCurrentAgentStats(stats);
        stats.setAgent(agent);
    }

    private AgentStats createAgentStats(AgentRecord agent, AgentStats value) {
        AgentStatsRecord stats = new AgentStatsRecord();
        associate(agent, stats);
        stats.setInfo(value);
        entityManager.persist(stats);
        return value;
    }

    private void updateCurrentAgentStats(AgentRecord agent, AgentStats value) {
        AgentStatsRecord currentStats = agent.getCurrentAgentStats();
        if (!value.hasStarted()) {
            value.start();
        }
        if (currentStats != null) {
            AgentStats oldStats = currentStats.getInfo();
            if (!oldStats.hasStarted() || oldStats.getStartTime().equals(value.getStartTime())) {
                currentStats.setInfo(value);
                return;
            }
            //this means oldStats.hasStarted() && !oldStats.getStartTime().equals(value.getStartTime())
            if (!oldStats.hasStopped()) {
                oldStats.stopWithRedaction();
            }
        }
        createAgentStats(agent, value);
                
//        if (!value.hasStarted()) {
//            if (currentStats != null) {
//                AgentStats oldStats = currentStats.getInfo();
//                if (oldStats.hasStarted() && !oldStats.hasStopped()) {
//                    oldStats.stopWithRedaction();
//                }
//            }
//
//            currentStats = new AgentStatsRecord();
//            associate(agent, currentStats);
//            value.start();
//            currentStats.setInfo(value);
//            entityManager.persist(currentStats);
//        } else if (currentStats == null) {
//            createAgentStats(agent, value);
//        } else {
//            currentStats.setInfo(value);
//        }
    }

    @Override
    @Transactional
    public void store(Integer agentExt, AgentStats value) {
        AgentRecord agent = agentRepository.getAgent(agentExt);
        updateCurrentAgentStats(agent, value);
    }

    @Override
    @Transactional
    public void storeAll(Map<Integer, AgentStats> map) {
        List<AgentRecord> agents = agentRepository.getAgents(map.keySet());
        for (AgentRecord agent : agents) {
            updateCurrentAgentStats(agent, map.get(agent.getExtension()));
        }
    }

    @Override
    public void delete(Integer key) {
    }

    @Override
    public void deleteAll(Collection<Integer> keys) {
    }

//    @Override
//    @Transactional
//    public void delete(Integer agentExt) {
//        AgentRecord agent = agentRepository.getAgent(agentExt);
//        agent.setCurrentAgentStats(null);
//    }
//
//    @Override
//    @Transactional
//    public void deleteAll(Collection<Integer> keys) {
//        List<AgentRecord> agents = agentRepository.getAgents(keys);
//        for (AgentRecord agent : agents) {
//            agent.setCurrentAgentStats(null);
//        }
//    }
    public AgentStats getAgentStats(AgentRecord agent) {
        if (agent == null) {
            return null;
        }
        AgentStatsRecord stats = agent.getCurrentAgentStats();
        if (stats == null) {
//            return createAgentStats(agent, new AgentStats());
            return new AgentStats();
        }
//        if (stats == null || stats.getInfo().isExpired()) {
//            Future<LoginActivity> future = amsService.getLastLoginActivity(agent.getUserName());
//            LoginActivity activity;
//            try {
//                activity = future.get();
//            } catch (InterruptedException ex) {
//                LOG.error(ex.getMessage(), ex);
//                return null;
//            } catch (ExecutionException ex) {
//                LOG.error(ex.getMessage(), ex.getCause());
//                return null;
//            }
//            if (activity == null || activity.getEventDate().toLocalDate().isBefore(LocalDate.now())) {
//                LOG.info("User Activity was not found or too old, ext {}", agent.getExtension());
//                return null;
//            }
//            if (stats != null) {
//                HzAgentStats hzStats = stats.getInfo();
//                if (!hzStats.hasStarted()) {
//                    hzStats.start(activity.getEventDate());
//                    return hzStats;
//                }
//            }
//            return agentStatsRepository.startStats(agent.getExtension(), activity.getEventDate());
//        }
//        return stats.getInfo();
        return stats.getInfo();
    }

    @Override
    @Transactional
    public AgentStats load(Integer agentExt) {
        LOG.info("loading agent {}", agentExt);
        return getAgentStats(agentRepository.getAgent(agentExt));
    }

    @Override
    @Transactional
    public Map<Integer, AgentStats> loadAll(Collection<Integer> keys) {
        if (keys.size() == 1) {
            Integer key = keys.iterator().next();
            AgentStats stats = load(key);
            if (stats != null) {
                return Collections.singletonMap(key, stats);
            } else {
                return Collections.emptyMap();
            }
        }

        Map<Integer, AgentStats> resultMap = new HashMap<>();
        for (AgentRecord agent : agentRepository.getAgents(keys)) {
            resultMap.put(agent.getExtension(), getAgentStats(agent));
        }
        return resultMap;
    }

    @Override
    public Set<Integer> loadAllKeys() {
//        List<Integer> extensions = entityManager.createQuery("select agent.extension"
//                + " from Agent agent inner join agent.currentAgentStats", Integer.class)
//                .getResultList();
//        return new HashSet<>(extensions);
        return null;
    }

}
