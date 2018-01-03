/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.objectbrains.tms.hazelcast.mapstore;

import com.hazelcast.core.MapStore;
import com.hazelcast.core.PostProcessingMapStore;
import com.objectbrains.ams.iws.User;
import com.objectbrains.tms.db.entity.AgentRecord;
import com.objectbrains.tms.db.repository.AgentRepository;
import static com.objectbrains.tms.hazelcast.Configs.AGENT_MAP_STORE_BEAN_NAME;
import com.objectbrains.tms.hazelcast.entity.Agent;
import com.objectbrains.tms.service.AmsService;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author connorpetty
 */
@Repository(AGENT_MAP_STORE_BEAN_NAME)
public class AgentMapStore implements MapStore<Integer, Agent>, PostProcessingMapStore {

    private static final Logger LOG = LoggerFactory.getLogger(AgentMapStore.class);

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private AgentRepository agentRepository;

    @Autowired
    private AmsService amsService;

    @Override
    @Transactional
    public void store(Integer key, Agent value) {
        value.setLastActivityTime(LocalDateTime.now());
        AgentRecord agent = agentRepository.getAgent(key);
        agent.setInfo(value);
    }

    @Override
    @Transactional
    public void storeAll(Map<Integer, Agent> map) {
        for (Map.Entry<Integer, Agent> entrySet : map.entrySet()) {
            Integer key = entrySet.getKey();
            Agent value = entrySet.getValue();
            store(key, value);
        }
    }

    @Override
    public void delete(Integer key) {
        //TODO
    }

    @Override
    public void deleteAll(Collection<Integer> keys) {
        //TODO
    }

    @Override
    @Transactional
    public Agent load(Integer key) {
        AgentRecord agent = agentRepository.getAgent(key);
        return agent == null ? null : agent.getInfo();
    }

    @Override
    @Transactional
    public Map<Integer, Agent> loadAll(Collection<Integer> keys) {
        List<Agent> agents = entityManager.createQuery("select agent.info from Agent agent where agent.extension in (:extensions)", Agent.class)
                .setParameter("extensions", keys)
                .getResultList();

        Map<Integer, Agent> resultMap = new HashMap<>();
        for (Agent agent : agents) {
            resultMap.put(agent.getExtension(), agent);
        }
        return resultMap;
    }

    @Override
    public Set<Integer> loadAllKeys() {
//        try {
//            Set<Integer> exts = new HashSet<>();
//            for (User user : amsService.getAllUsers()) {
//                exts.add(user.getExtension());
//            }
//            return exts;
//        } catch (RuntimeException ex) {
//            LOG.warn("Unable to eagerly load ams users.", ex);
//            return null;
//        }
//        List<Integer> extensions = entityManager.createQuery(
//                "select agent.extension from Agent agent", Integer.class)
//                .getResultList();
//        return new HashSet<>(extensions);
        return null;
    }

}
