/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.tms.db.repository;

import com.amp.crm.db.entity.base.dialer.DialerQueueSettings;
import com.amp.crm.embeddable.WeightedPriority;
import com.amp.tms.db.entity.DialerQueueTms;
import com.amp.tms.enumerated.DialerType;
import com.amp.tms.service.SvcQueueService;
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
 * @author Hoang, J, Bishistha
 */
@Repository
@Transactional
public class TmsDialerQueueRepository {

    private static final Logger LOG = LoggerFactory.getLogger(TmsDialerQueueRepository.class);

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
//    public void update(DialerQueueTms dialerQueue) {
//        entityManager.merge(dialerQueue);
//    }
    public void update(long queuePk, DialerType type, WeightedPriority wp) {
        DialerQueueTms queue = entityManager.find(DialerQueueTms.class, queuePk);
        boolean persist = queue == null;
        if (persist) {
            queue = new DialerQueueTms();
            queue.setPk(queuePk);
        }
        queue.copyFrom(wp);
        queue.setType(type);

        if (persist) {
            entityManager.persist(queue);
        }
    }

    public DialerQueueTms getDialerQueue(long queuePk) {
        DialerQueueTms queue = entityManager.find(DialerQueueTms.class, queuePk);
        if (queue != null) {
            return queue;
        }
        return loadQueue(queuePk);
    }

    private DialerQueueTms loadQueue(long queuePk) {
        DialerQueueSettings settings = queueService.getQueueSettings(queuePk);
        if (settings == null) {
            return null;
        }
        DialerQueueTms queue = new DialerQueueTms();
        queue.setPk(queuePk);
        queue.copyFrom(settings.getWeightedPriority());
        queue.setType(DialerType.valueFrom(settings));
        entityManager.persist(queue);
        return queue;
    }

    public List<DialerQueueTms> getDialerQueuesForAgent(int agentExtension) {
        return entityManager.createQuery("select assoc.dialerQueue "
                + "from AgentQueueAssociation assoc "
                + "where assoc.pk.extension = :agentExtension",
                DialerQueueTms.class)
                .setParameter("agentExtension", agentExtension)
                .getResultList();
    }

}
