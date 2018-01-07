/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.tms.hazelcast.mapstore;

import com.hazelcast.core.MapStore;
import com.hazelcast.core.MapStoreAdapter;
import com.hazelcast.core.PostProcessingMapStore;
import com.hazelcast.mapreduce.aggregation.impl.SetAdapter;
import com.amp.tms.db.entity.AgentRecord;
import com.amp.tms.db.entity.AgentCallRecord;
import com.amp.tms.db.repository.AgentCallRepository;
import com.amp.tms.db.repository.AgentRepository;
import static com.amp.tms.hazelcast.Configs.AGENT_CALL_MAP_STORE_BEAN_NAME;
import com.amp.tms.hazelcast.entity.AgentCall;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author Hoang, J, Bishistha
 */
@Repository(AGENT_CALL_MAP_STORE_BEAN_NAME)
public class AgentCallMapStore implements MapStore<Integer, SetAdapter<AgentCall>>, PostProcessingMapStore {

    @Autowired
    private AgentRepository agentRepository;

    @Autowired
    private AgentCallRepository callRepository;

    @Override
    public void store(Integer key, SetAdapter<AgentCall> agentCalls) {
        //we want to commit the transaction so that envers has a chance
        //to record the changes before removing any records
        Collection<Long> recordsToRemove = callRepository.addOrUpdate(key, agentCalls);

        //now remove call records that need to be removed
        if (!recordsToRemove.isEmpty()) {
            callRepository.delete(recordsToRemove);
        }
    }

    @Override
    public void storeAll(final Map<Integer, SetAdapter<AgentCall>> map) {
        for (Map.Entry<Integer, SetAdapter<AgentCall>> entry : map.entrySet()) {
            store(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public void delete(Integer key) {
        callRepository.deleteForAgent(key);
    }

    @Override
    public void deleteAll(Collection<Integer> keys) {
        callRepository.deleteForAgents(keys);
    }

    private SetAdapter<AgentCall> load(AgentRecord agent) {
        if (agent == null) {
            return null;
        }
        Set<AgentCallRecord> callRecords = agent.getCallRecords();
        if (callRecords == null || callRecords.isEmpty()) {
            return null;
        }
        SetAdapter<AgentCall> agentCalls = new SetAdapter<>();
        for (AgentCallRecord callRecord : callRecords) {
            agentCalls.add(new AgentCall(callRecord));
        }
        return agentCalls;
    }

    @Override
    @Transactional
    public SetAdapter<AgentCall> load(Integer key) {
        return load(agentRepository.getAgent(key));
    }

    @Override
    @Transactional
    public Map<Integer, SetAdapter<AgentCall>> loadAll(Collection<Integer> keys) {
        List<AgentRecord> agents = agentRepository.getAgents(keys);
        Map<Integer, SetAdapter<AgentCall>> resultMap = new HashMap<>();
        for (AgentRecord agent : agents) {
            SetAdapter<AgentCall> set = load(agent);
            if (set != null) {
                resultMap.put(agent.getExtension(), set);
            }
        }
        return resultMap;
    }

    @Override
    public Set<Integer> loadAllKeys() {
//        List<Integer> extensions
//                = entityManager.createQuery(
//                        "select distinct call.agent.extension from AgentCall call",
//                        Integer.class)
//                .getResultList();
//        return new HashSet<>(extensions);
        return null;
    }

}
