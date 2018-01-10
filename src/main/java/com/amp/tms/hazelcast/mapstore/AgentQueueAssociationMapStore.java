/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.tms.hazelcast.mapstore;

import com.hazelcast.core.MapLoader;
import com.hazelcast.core.MapStore;
import com.amp.tms.db.entity.AgentQueueAssociation;
import com.amp.tms.db.entity.AgentRecord;
import com.amp.tms.db.entity.DialerQueueTms;
import com.amp.tms.db.repository.AgentRepository;
import com.amp.tms.db.repository.TmsDialerQueueRepository;
import static com.amp.tms.hazelcast.Configs.AGENT_QUEUE_ASSOCIATION_MAP_STORE_BEAN_NAME;
import com.amp.tms.hazelcast.entity.AgentWeightedPriority;
import com.amp.tms.hazelcast.keys.AgentQueueKey;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * 
 */
@Repository(AGENT_QUEUE_ASSOCIATION_MAP_STORE_BEAN_NAME)
public class AgentQueueAssociationMapStore implements MapStore<AgentQueueKey, AgentWeightedPriority> {

    private static final String LOAD_QUERY = "select new " + AgentWeightedPriority.class.getName()
            + "(coalesce(assoc.priority, assoc.dialerQueue.priority), "
            + "coalesce(assoc.weight, assoc.dialerQueue.weight),"
            + "assoc.leader, assoc.allowAfterHours) "
            + "from AgentQueueAssociation assoc "
            + "where assoc.agent.extension = :ext "
            + "and assoc.dialerQueue.pk = :queuePk";

    private static final String LOAD_ALL_KEYS_QUERY
            = "select new " + AgentQueueKey.class.getName() + "(assoc.pk.extension, assoc.pk.queuePk) "
            + "from AgentQueueAssociation assoc";

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private AgentRepository agentRepository;

    @Autowired
    private TmsDialerQueueRepository queueRepository;

    @Override
    @Transactional
    public void store(AgentQueueKey key, AgentWeightedPriority value) {
        AgentQueueAssociation assoc = entityManager.find(AgentQueueAssociation.class, key);
        if (assoc != null) {
            assoc.copyFrom(value);
            return;
        }
        assoc = new AgentQueueAssociation();
        assoc.copyFrom(value);
        assoc.setPk(key);
        AgentRecord agent = agentRepository.getAgent(key.getExtension());
        DialerQueueTms queue = queueRepository.getDialerQueue(key.getQueuePk());
        
        assoc.setAgent(agent);
        assoc.setDialerQueue(queue);
        entityManager.persist(assoc);
    }

    @Override
    @Transactional
    public void storeAll(Map<AgentQueueKey, AgentWeightedPriority> map) {
        for (Map.Entry<AgentQueueKey, AgentWeightedPriority> entrySet : map.entrySet()) {
            AgentQueueKey key = entrySet.getKey();
            AgentWeightedPriority value = entrySet.getValue();
            store(key, value);
        }
//        Set<Integer> extensions = new HashSet<>();
//        Set<Long> queuePks = new HashSet<>();
//
//        for (AgentQueueKey key : map.keySet()) {
//            extensions.add(key.getExtension());
//            queuePks.add(key.getQueuePk());
//        }
//        Map<Integer, AgentRecord> agents = new HashMap<>();
//        Map<Long, DialerQueueTms> queues = new HashMap<>();
//        
//        for(AgentRecord agent : agentRepository.getAgents(extensions)){
//            
//        }
////        List<AgentagentRepository.
//        
//
    }

    @Override
    @Transactional
    public void delete(AgentQueueKey key) {
        AgentQueueAssociation assoc = entityManager.find(AgentQueueAssociation.class, key);
        if(assoc != null){
            entityManager.remove(assoc);
        }
    }

    @Override
    @Transactional
    public void deleteAll(Collection<AgentQueueKey> keys) {
        for (AgentQueueKey key : keys) {
            delete(key);
        }
    }
//entityManager.createQuery("delete AgentQueueAssociation assoc where assoc.pk.queuePk = :queuePk")
//                .setParameter("queuePk", queuePk)
//                .executeUpdate();
//
//        DialerQueueTms queue = getDialerQueue(queuePk);
//        for (int i = 0; i < extensions.size(); i++) {
//            AgentWeightedPriority agentWeightPriority = agentWeightPriorities.get(i);
//            AgentRecord agent = agentRepository.getAgent(extensions.get(i));
//            AgentQueueAssociation assoc = new AgentQueueAssociation();
//            AgentQueueKey key = new AgentQueueKey(agent.getExtension(), queuePk);
//            assoc.copyFrom(agentWeightPriority);
//            assoc.setPk(key);
//            assoc.setAgent(agent);
//            assoc.setDialerQueue(queue);
//            entityManager.persist(assoc);
//        }

    @Override
    @Transactional
    public AgentWeightedPriority load(AgentQueueKey key) {
        AgentQueueAssociation assoc = entityManager.find(AgentQueueAssociation.class, key);
        if(assoc != null){
            return new AgentWeightedPriority(assoc);
        }
        return null;
        

//        try {
//            return entityManager.createQuery(LOAD_QUERY, AgentWeightedPriority.class)
//                    .setParameter("ext", key.getExtension())
//                    .setParameter("queuePk", key.getQueuePk())
//                    .getSingleResult();
//        } catch (NoResultException ex) {
//            return null;
//        }
    }

    @Override
    @Transactional
    public Map<AgentQueueKey, AgentWeightedPriority> loadAll(Collection<AgentQueueKey> keys) {
        Map<AgentQueueKey, AgentWeightedPriority> map = new HashMap<>();
        for (AgentQueueKey key : keys) {
            AgentWeightedPriority value = load(key);
            if (value != null) {
                map.put(key, value);
            }
        }
        return map;
    }

    @Override
    public Set<AgentQueueKey> loadAllKeys() {
//        List<AgentQueueKey> results = entityManager.createQuery(LOAD_ALL_KEYS_QUERY, AgentQueueKey.class).getResultList();
//        return new HashSet<>(results);
        return null;
    }

}
