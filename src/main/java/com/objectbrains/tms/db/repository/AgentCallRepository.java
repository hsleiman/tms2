/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.objectbrains.tms.db.repository;

import com.objectbrains.tms.db.entity.AgentCallRecord;
import com.objectbrains.tms.db.entity.AgentRecord;
import com.objectbrains.tms.enumerated.CallState;
import com.objectbrains.tms.hazelcast.entity.AgentCall;
import java.util.ArrayList;
import java.util.Collection;
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
 * @author connorpetty
 */
@Repository
@Transactional
public class AgentCallRepository {

    private static final Logger LOG = LoggerFactory.getLogger(AgentCallRepository.class);

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private AgentRepository agentRepository;

    public void reassignAssociations(AgentRecord oldAgent, AgentRecord newAgent) {
        List<AgentCallRecord> calls = entityManager.createQuery(
                "select call "
                + "from AgentCall call "
                + "where call.agent.extension = :ext", AgentCallRecord.class)
                .setParameter("ext", oldAgent.getExtension())
                .getResultList();

        for (AgentCallRecord call : calls) {
            call.setAgent(newAgent);
        }
    }

    /**
     * This updates the persisted {@code AgentCallRecord}s for the agent with
     * given extension.
     * <p>
     * If a {@code AgentCall} cannot be matched to one of the persisted
     * AgentCallRecords, a new AgentCallRecord will be created for that
     * AgentCall.
     * <br>
     * If an {@code AgentCallRecord} could not be matched to a
     * {@code AgentCall}, the pk for the {@code AgentCallRecord} will be
     * included in the returned collection do that it can be removed in a
     * separate transaction
     * <br>
     * This also removes {@code AgentCall}s which are in the 'DONE' state from
     * {@code agentCalls} as well as including the corresponding record pks in
     * the returned collection.
     *
     * @param extension the agent extension
     * @param agentCalls the agent calls to add or update
     * @return a set of AgentCallRecord pks that should be removed
     */
    public Collection<Long> addOrUpdate(Integer extension, Set<AgentCall> agentCalls) {
        //create map of UUIDs to AgentCalls
        Map<String, AgentCall> callMap = new HashMap<>();
        for (AgentCall agentCall : agentCalls) {
            callMap.put(agentCall.getCallUUID(), agentCall);
        }

        AgentRecord agent = agentRepository.getAgent(extension);
        Set<AgentCallRecord> callRecords = agent.getCallRecords();
        List<Long> recordsToRemove = new ArrayList<>(2);

        //attempt to update each of the existing call records
        if (callRecords != null) {
            for (AgentCallRecord record : callRecords) {
                AgentCall agentCall = callMap.remove(record.getCallUUID());
                if (agentCall != null) {
                    record.copyFrom(agentCall);
                    if (agentCall.getCallState() != CallState.DONE) {
                        continue;
                    }
                    agentCalls.remove(agentCall);
                }
                recordsToRemove.add(record.getPk());
            }
        }
        //any calls still in the map must be new so add them in
        for (AgentCall agentCall : callMap.values()) {
            AgentCallRecord record = new AgentCallRecord();
            record.setAgent(agent);
            record.setAgentStats(agent.getCurrentAgentStats());
            record.copyFrom(agentCall);
            entityManager.persist(record);

            if (agentCall.getCallState() == CallState.DONE) {
                agentCalls.remove(agentCall);
                recordsToRemove.add(record.getPk());
            }
        }
        return recordsToRemove;
    }

    public void delete(Collection<Long> pks) {
        entityManager.createQuery("delete AgentCall call where call.pk in (:pks)")
                .setParameter("pks", pks)
                .executeUpdate();
    }

    public void deleteForAgent(Integer extention) {
        entityManager.createQuery("delete AgentCall call where call.agent.extension = :extension")
                .setParameter("extension", extention)
                .executeUpdate();
    }

    public void deleteForAgents(Collection<Integer> extensions) {
        entityManager.createQuery("delete AgentCall call where call.agent.extension in (:extensions)")
                .setParameter("extensions", extensions)
                .executeUpdate();
    }
}
