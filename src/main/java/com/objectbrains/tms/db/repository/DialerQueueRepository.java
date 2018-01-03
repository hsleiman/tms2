/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.objectbrains.tms.db.repository;

import com.objectbrains.sti.db.entity.base.dialer.DialerQueueSettings;
import com.objectbrains.sti.embeddable.WeightedPriority;
import com.objectbrains.tms.db.entity.DialerQueue;
import com.objectbrains.tms.enumerated.DialerType;
import com.objectbrains.tms.service.SvcQueueService;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author connorpetty
 */
@Repository
@Transactional
public class DialerQueueRepository {

    private static final Logger LOG = LoggerFactory.getLogger(DialerQueueRepository.class);

    @PersistenceContext
    private EntityManager entityManager;

//    @Autowired
//    private AgentRepository agentRepository;
    @Autowired
    private SvcQueueService queueService;

//    public Set<AgentQueueKey> setAgentQueueAssociations(long queuePk, List<AgentWeightPriority> agentWeightPriorities) {
//    public void setAgentQueueAssociations(long queuePk, List<Integer> extensions, List<AgentWeightedPriority> agentWeightPriorities) {
//        entityManager.createQuery("delete AgentQueueAssociation assoc where assoc.pk.queuePk = :queuePk")
//                .setParameter("queuePk", queuePk)
//                .executeUpdate();
//
//        DialerQueue queue = getDialerQueue(queuePk);
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
//
////        Set<AgentQueueKey> agentQueueKeys = new HashSet<>();
////        for (AgentWeightPriority agentWeightPriority : agentWeightPriorities) {
////            AgentRecord agent = agentRepository.getAgent(agentWeightPriority.getUsername());
////            AgentQueueAssociation assoc = new AgentQueueAssociation();
////            AgentQueueKey key = new AgentQueueKey(agent.getExtension(), queuePk);
////            agentQueueKeys.add(key);
////            assoc.setPk(key);
////            assoc.setAgent(agent);
////            assoc.setDialerQueue(getDialerQueue(queuePk));
////            assoc.setWeightedPriority(new WeightedPriority(agentWeightPriority.getWeightedPriority()));
////            entityManager.persist(assoc);
////        }
////        return agentQueueKeys;
//    }
//    public void update(DialerQueue dialerQueue) {
//        entityManager.merge(dialerQueue);
//    }
    public void update(long queuePk, DialerType type, WeightedPriority wp) {
        DialerQueue queue = entityManager.find(DialerQueue.class, queuePk);
        boolean persist = queue == null;
        if (persist) {
            queue = new DialerQueue();
            queue.setPk(queuePk);
        }
        queue.copyFrom(wp);
        queue.setType(type);

        if (persist) {
            entityManager.persist(queue);
        }
    }

    public DialerQueue getDialerQueue(long queuePk) {
        DialerQueue queue = entityManager.find(DialerQueue.class, queuePk);
        if (queue != null) {
            return queue;
        }
        return loadQueue(queuePk);
    }

    private DialerQueue loadQueue(long queuePk) {
        DialerQueueSettings settings = queueService.getQueueSettings(queuePk);
        if (settings == null) {
            return null;
        }
        DialerQueue queue = new DialerQueue();
        queue.setPk(queuePk);
        queue.copyFrom(settings.getWeightedPriority());
        queue.setType(DialerType.valueFrom(settings));
        entityManager.persist(queue);
        return queue;
    }

    public List<DialerQueue> getDialerQueuesForAgent(int agentExtension) {
        return entityManager.createQuery("select assoc.dialerQueue "
                + "from AgentQueueAssociation assoc "
                + "where assoc.pk.extension = :agentExtension",
                DialerQueue.class)
                .setParameter("agentExtension", agentExtension)
                .getResultList();
    }

}
