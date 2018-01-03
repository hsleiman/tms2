/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.objectbrains.tms.db.repository;

import com.objectbrains.tms.db.entity.AgentQueueAssociation;
import com.objectbrains.tms.db.entity.AgentRecord;
import com.objectbrains.tms.hazelcast.keys.AgentQueueKey;
import java.util.Collection;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author connorpetty
 */
@Repository
@Transactional
public class AgentQueueAssociationRepository {

    @PersistenceContext
    private EntityManager entityManager;

    public void reassignAssociations(AgentRecord oldRecord, AgentRecord newRecord) {
        List<AgentQueueAssociation> results = getAgentQueueAssociations(oldRecord.getExtension());
        for (AgentQueueAssociation oldAssoc : results) {
            AgentQueueAssociation newAssoc = new AgentQueueAssociation();
            long queuePk = oldAssoc.getPk().getQueuePk();
            newAssoc.copyFrom(oldAssoc);
            newAssoc.setPk(new AgentQueueKey(newRecord.getExtension(), queuePk));
            newAssoc.setAgent(newRecord);
            newAssoc.setDialerQueue(oldAssoc.getDialerQueue());
            entityManager.persist(newAssoc);
            entityManager.remove(oldAssoc);
        }
    }

//    public AgentQueueAssociation getAssociation(int ext, long queuePk){
//        
//    }
    public void retainAssociations(long queuePk, Collection<Integer> extensions) {
        if (extensions == null || extensions.isEmpty()) {
            entityManager.createQuery("delete AgentQueueAssociation assoc where assoc.pk.queuePk = :queuePk")
                    .setParameter("queuePk", queuePk)
                    .executeUpdate();
        } else {
            entityManager.createQuery("delete AgentQueueAssociation assoc where assoc.pk.queuePk = :queuePk"
                    + " and assoc.pk.extension not in (:extensions)")
                    .setParameter("queuePk", queuePk)
                    .setParameter("extensions", extensions)
                    .executeUpdate();
        }
    }

    public void retainAssociations(int ext, Collection<Long> queuePks) {
        if (queuePks == null || queuePks.isEmpty()) {
            entityManager.createQuery("delete AgentQueueAssociation assoc where assoc.pk.extension = :ext")
                    .setParameter("ext", ext)
                    .executeUpdate();
        } else {
            entityManager.createQuery("delete AgentQueueAssociation assoc where assoc.pk.extension = :ext"
                    + " and assoc.pk.queuePk not in (:queuePks)")
                    .setParameter("ext", ext)
                    .setParameter("queuePks", queuePks)
                    .executeUpdate();
        }
    }

    public List<AgentQueueAssociation> getAgentQueueAssociations(int ext) {
//        List<Object[]> results = entityManager.createQuery("select assoc.dialerQueue, "
//                + "new " + AgentWeightedPriority.class.getName()
//                + "(coalesce(assoc.priority, assoc.dialerQueue.priority), "
//                + "coalesce(assoc.weight, assoc.dialerQueue.weight),"
//                + "assoc.leader, assoc.allowAfterHours) "
//                + "from AgentQueueAssociation assoc "
//                + "where assoc.agent.extension = :ext", Object[].class)
//                .setParameter("ext", ext)
//                .getResultList();
//        Map<DialerQueue, AgentWeightedPriority> queuePriorities = new HashMap<>();
//        for (Object[] result : results) {
//            DialerQueue queue = (DialerQueue) result[0];
//            AgentWeightedPriority awp = (AgentWeightedPriority) result[1];
//            queuePriorities.put(queue, awp);
////            Integer priority = ((Number) result[1]).intValue();
////            Double weight = ((Number) result[2]).doubleValue();
////            queuePriorities.put(queue, new WeightedPriority(priority, weight));
//        }
//        return queuePriorities;
        List<AgentQueueAssociation> results = entityManager.createQuery("select assoc "
                + "from AgentQueueAssociation assoc where assoc.agent.extension = :ext", AgentQueueAssociation.class)
                .setParameter("ext", ext)
                .getResultList();
        return results;
    }
}
