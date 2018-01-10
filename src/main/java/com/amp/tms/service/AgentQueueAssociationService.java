/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.tms.service;

import com.hazelcast.core.IMap;
import com.hazelcast.mapreduce.aggregation.impl.SetAdapter;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.DataSerializable;
import com.hazelcast.query.Predicate;
import com.objectbrains.hcms.annotation.ConfigContext;
import com.objectbrains.hcms.configuration.ConfigurationUtility;
import com.objectbrains.hcms.hazelcast.HazelcastService;
import com.amp.tms.db.repository.AgentQueueAssociationRepository;
import com.amp.tms.enumerated.CallDirection;
import com.amp.tms.enumerated.DialerType;
import com.amp.tms.hazelcast.AbstractEntryProcessor;
import com.amp.tms.hazelcast.AgentDialerState;
import com.amp.tms.hazelcast.AgentExtPredicate;
import com.amp.tms.hazelcast.AgentsInQueuePredicate;
import com.amp.tms.hazelcast.Configs;
import com.amp.tms.hazelcast.entity.AgentWeightedPriority;
import com.amp.tms.hazelcast.keys.AgentQueueKey;
import com.amp.tms.service.dialer.predict.AgentQueueWeightedPriority;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 *
 * 
 */
@Service(AgentQueueAssociationService.BEAN_NAME)
public class AgentQueueAssociationService {

    public static final String BEAN_NAME = "agentQueueAssociationService";

    private static final Logger LOG = LoggerFactory.getLogger(AgentQueueAssociationService.class);

    @Autowired
    private HazelcastService hazelcastService;

    @Autowired
    private AgentQueueAssociationRepository assocRepo;

    @Autowired
    @Lazy
    private TMSAgentService agentService;

    @ConfigContext
    private ConfigurationUtility config;

    private IMap<AgentQueueKey, AgentWeightedPriority> agentQueueMap;

    @PostConstruct
    private void init() {
        agentQueueMap = hazelcastService.getMap(Configs.QUEUE_WEIGHTED_PRIORITY_MAP);
    }

    private Set<AgentQueueWeightedPriority> getParticipatingAssociations(
            Predicate<AgentQueueKey, ? super AgentWeightedPriority> predicate, boolean checkAgentReady, CallDirection callDirection, boolean autoDialed) {

        Set<Map.Entry<AgentQueueKey, AgentWeightedPriority>> entries
                = predicate == null
                        ? agentQueueMap.entrySet()
                        : agentQueueMap.entrySet(predicate);

        Set<Integer> extensions = new HashSet<>(entries.size());
        for (Map.Entry<AgentQueueKey, AgentWeightedPriority> entry : entries) {
            extensions.add(entry.getKey().getExtension());
        }

        Map<Integer, AgentDialerState> states = agentService.getAgentDialerStates(extensions, callDirection, autoDialed);
        Map<Integer, Map<Long, AgentWeightedPriority>> agentWeightedPriorityMap = new HashMap<>();

        for (Map.Entry<AgentQueueKey, AgentWeightedPriority> entry
                : predicate == null
                        ? entries
                        : agentQueueMap.entrySet(new ExtensionListPredicate(extensions))) {
            AgentQueueKey key = entry.getKey();
            long queuePk = key.getQueuePk();
            int extension = key.getExtension();

            AgentWeightedPriority value = entry.getValue();
            AgentDialerState state = states.get(extension);
            if ((value.getDialerType() == DialerType.INBOUND || state.isDialerActive())
                    && (!checkAgentReady || InboundCallService.shouldRecieveCall(false, state.getState(), state, value, null))) {

                Map<Long, AgentWeightedPriority> queuePriorityList = agentWeightedPriorityMap.get(extension);
                if (queuePriorityList == null) {
                    queuePriorityList = new HashMap<>();
                    agentWeightedPriorityMap.put(extension, queuePriorityList);
                }
                queuePriorityList.put(queuePk, value);
            }
        }

        Set<AgentQueueWeightedPriority> ret = new HashSet<>();
        //for each agent we iterate through all of their queues
        for (Map.Entry<Integer, Map<Long, AgentWeightedPriority>> entrySet2 : agentWeightedPriorityMap.entrySet()) {
            int extension = entrySet2.getKey();
            Map<Long, AgentWeightedPriority> value2 = entrySet2.getValue();

            for (Map.Entry<Long, AgentWeightedPriority> entrySet : value2.entrySet()) {
                Long queuePk = entrySet.getKey();
                AgentWeightedPriority source = entrySet.getValue();

                //is the agent participating in this queue?
                //only way to determing is if isParticipating returns true for all other queues this
                //agent is also in.
                boolean participating = true;

                for (Map.Entry<Long, AgentWeightedPriority> entrySet1 : value2.entrySet()) {
//                    Long key = entrySet1.getKey();
                    AgentWeightedPriority target = entrySet1.getValue();
                    participating &= isParticipating(source, target);
                }

                if (participating) {
                    ret.add(new AgentQueueWeightedPriority(extension, queuePk, source));
                }
            }
        }
        return ret;
    }

    public Set<AgentQueueWeightedPriority> getAllParticipatingAgents(boolean checkAgentReady, CallDirection callDirection, boolean autoDialed) {
        return getParticipatingAssociations(null, checkAgentReady, callDirection, autoDialed);
    }

    public Map<Integer, AgentWeightedPriority> getParticipatingAgents(long queuePk) {
        return getParticipatingAgents(queuePk, false, null, false);
    }

    public Map<Integer, AgentWeightedPriority> getParticipatingAgents(
            long queuePk, CallDirection callDirection, boolean autoDialed) {
        return getParticipatingAgents(queuePk, true, callDirection, autoDialed);
    }

    /*
     given queue, find participating agents
     1. find agents in queue
     2. filter out agents that are not ready
     3. determine if given queue is among the priority queues for the agent
     */
    public Map<Integer, AgentWeightedPriority> getParticipatingAgents(
            long queuePk, boolean checkAgentReady, CallDirection callDirection, boolean autoDialed) {
        LOG.trace("Entered");
        Set<AgentQueueWeightedPriority> participants = getParticipatingAssociations(
                new AgentsInQueuePredicate(queuePk),
                checkAgentReady, callDirection, autoDialed);
        Map<Integer, AgentWeightedPriority> ret = new HashMap<>();
        for (AgentQueueWeightedPriority participant : participants) {
            if (participant.getQueuePk() == queuePk) {
                ret.put(participant.getExtension(), participant.getWeightedPriority());
            }
        }

        LOG.trace("Exited");

        return ret;
    }

    private static class ExtensionListPredicate implements Predicate<AgentQueueKey, AgentWeightedPriority>, DataSerializable {

        private SetAdapter<Integer> extensions = new SetAdapter<>();

        private ExtensionListPredicate() {
        }

        public ExtensionListPredicate(Set<Integer> extensions) {
            this.extensions.addAll(extensions);
        }

        @Override
        public boolean apply(Map.Entry<AgentQueueKey, AgentWeightedPriority> mapEntry) {
            return extensions.contains(mapEntry.getKey().getExtension());
        }

        @Override
        public void writeData(ObjectDataOutput out) throws IOException {
            extensions.writeData(out);
        }

        @Override
        public void readData(ObjectDataInput in) throws IOException {
            extensions.clear();
            extensions.readData(in);
        }

    }

    /*
     this is more or less a way to compare the priorities of dialers for a single
     agent. typcially a power dialer will always trump a progressive dialer
     of the same priority.
     So if the target has a priority of 3 and the source has a priority of 4, 
     this will return true only if the target has no waiting calls and it is not running.
    
     */
    private static boolean isParticipating(AgentWeightedPriority source, AgentWeightedPriority target) {
        switch (source.getDialerType() != null ? source.getDialerType() : DialerType.INBOUND) {
            case PROGRESSIVE:
            case PREDICTIVE:
                if (target.getDialerType() != null) {
                    switch (target.getDialerType()) {
                        case POWER:
                            if (target.getPriority() <= source.getPriority()) {
                                return !target.hasWaitingCalls() && !target.isRunning();
                            }
                        //fall through
                        default:
                            return true;
                        case PROGRESSIVE:
                        case PREDICTIVE:
                        //fall through
                    }
                }
            //fall through
            case POWER:
                if (target.getPriority() < source.getPriority()) {
                    return !target.hasWaitingCalls() && !target.isRunning();
                }
            //fall through
            default:
                return true;
        }
    }

    public Map<AgentQueueKey, AgentWeightedPriority> getAssociations(int ext) {
        Map<AgentQueueKey, AgentWeightedPriority> map = new HashMap<>();
        for (Map.Entry<AgentQueueKey, AgentWeightedPriority> entrySet : agentQueueMap.entrySet(new AgentExtPredicate(ext))) {
            AgentQueueKey key = entrySet.getKey();
            AgentWeightedPriority value = entrySet.getValue();
            map.put(key, value);
        }
        return map;
    }

    public Set<Long> getAgentQueues(int ext) {
        Set<Long> queuePks = new HashSet<>();
        for (AgentQueueKey key : agentQueueMap.keySet(new AgentExtPredicate(ext))) {
            queuePks.add(key.getQueuePk());
        }
        return queuePks;
    }

    public AgentWeightedPriority getAssociation(int ext, long queuePk) {
        return agentQueueMap.get(new AgentQueueKey(ext, queuePk));
    }

    public void setAgentQueueAssociations(long queuePk, List<Integer> extensions, List<AgentWeightedPriority> weightPriorities) {

        //2 step processes
        //step 1: remove or replace existings keys
        //step 2: add missing keys
        Map<AgentQueueKey, AgentWeightedPriority> replaceMap = new HashMap<>();
        for (int i = 0; i < extensions.size(); i++) {
            int extension = extensions.get(i);
            AgentWeightedPriority awp = weightPriorities.get(i);
            replaceMap.put(new AgentQueueKey(extension, queuePk), awp);
        }

        assocRepo.retainAssociations(queuePk, extensions);
        Map<AgentQueueKey, Object> modified = agentQueueMap.executeOnEntries(
                new ReplaceAssociationEntryProcessor(replaceMap),
                new AgentsInQueuePredicate(queuePk));

        //remove modified keys so that only new ones are left
        replaceMap.keySet().removeAll(modified.keySet());
        LOG.debug("Replacing keys: {}", replaceMap.keySet());
        agentQueueMap.putAll(replaceMap);
    }

    public void setAgentQueueAssociations(int extension, List<Long> queuePks, List<AgentWeightedPriority> weightPriorities) {
        Map<AgentQueueKey, AgentWeightedPriority> replaceMap = new HashMap<>();
        for (int i = 0; i < queuePks.size(); i++) {
            long queuePk = queuePks.get(i);
            AgentWeightedPriority awp = weightPriorities.get(i);
            replaceMap.put(new AgentQueueKey(extension, queuePk), awp);
        }

        assocRepo.retainAssociations(extension, queuePks);
        Map<AgentQueueKey, Object> modified = agentQueueMap.executeOnEntries(
                new ReplaceAssociationEntryProcessor(replaceMap),
                new AgentExtPredicate(extension));

        //remove modified keys so that only new ones are left
        replaceMap.keySet().removeAll(modified.keySet());
        LOG.debug("Replacing keys: {}", replaceMap.keySet());
        agentQueueMap.putAll(replaceMap);
    }

    public static class ReplaceAssociationEntryProcessor extends AbstractEntryProcessor<AgentQueueKey, AgentWeightedPriority> {

        private Map<AgentQueueKey, AgentWeightedPriority> replaceMap;

        public ReplaceAssociationEntryProcessor() {
            this.replaceMap = new HashMap<>();
        }

        public ReplaceAssociationEntryProcessor(Map<AgentQueueKey, AgentWeightedPriority> replaceMap) {
            this.replaceMap = replaceMap;
        }

        @Override
        public Boolean process(Map.Entry<AgentQueueKey, AgentWeightedPriority> entry, boolean isPrimary) {
            LOG.debug("Executed on key: {}", entry.getKey());
            AgentWeightedPriority oldValue = entry.getValue();
            AgentWeightedPriority replacement = replaceMap.get(entry.getKey());
            if (replacement != null) {
                if (replacement.getDialerType() == null) {
                    replacement.setDialerType(oldValue.getDialerType());
                }
                if (replacement.getIsRunning() == null) {
                    replacement.setIsRunning(oldValue.getIsRunning());
                }
                if (replacement.getHasWaitingCalls() == null) {
                    replacement.setHasWaitingCalls(oldValue.getHasWaitingCalls());
                }
            }
            if (!equals(oldValue, replacement)) {
                entry.setValue(replacement);
                LOG.debug("Replaced value for key: {}", entry.getKey());
                return true;
            }
            return null;
        }

        private static boolean equals(AgentWeightedPriority v1, AgentWeightedPriority v2) {
            return (v1 == v2) || (v1 != null && v1.valueEquals(v2));
        }

        @Override
        public void writeData(ObjectDataOutput out) throws IOException {
            super.writeData(out);
            out.writeInt(replaceMap.size());
            for (Map.Entry<AgentQueueKey, AgentWeightedPriority> entrySet : replaceMap.entrySet()) {
                AgentQueueKey key = entrySet.getKey();
                AgentWeightedPriority value = entrySet.getValue();
                key.writeData(out);
                value.writeData(out);
            }
        }

        @Override
        public void readData(ObjectDataInput in) throws IOException {
            super.readData(in);
            replaceMap.clear();
            int size = in.readInt();
            for (int i = 0; i < size; i++) {
                AgentQueueKey key = new AgentQueueKey();
                AgentWeightedPriority value = new AgentWeightedPriority();
                key.readData(in);
                value.readData(in);
                replaceMap.put(key, value);
            }
        }

    }

    @Async
    public void setAssociationDialerType(long queuePk, DialerType dialerType) {
        LOG.trace("Entered {} - {}", queuePk, dialerType);
        agentQueueMap.executeOnEntries(new SetDialerTypeEntryProcessor(dialerType), new AgentsInQueuePredicate(queuePk));
        LOG.trace("Exited {} - {}", queuePk, dialerType);
    }

    @Async
    public void setAssociationHasWaitingCall(long queuePk, boolean hasWaitingCall) {
        LOG.trace("Entered {} - {}", queuePk, hasWaitingCall);
        agentQueueMap.executeOnEntries(new SetHasWaitingCallEntryProcessor(hasWaitingCall), new AgentsInQueuePredicate(queuePk));
        LOG.trace("Exited {} - {}", queuePk, hasWaitingCall);
    }

    @Async
    public void setAssociationIsRunning(long queuePk, boolean isRunning) {
        LOG.trace("Entered {} - {}", queuePk, isRunning);
        agentQueueMap.executeOnEntries(new SetIsRunningEntryProcessor(isRunning), new AgentsInQueuePredicate(queuePk));
        LOG.trace("Exited {} - {}", queuePk, isRunning);
    }

    public static class SetDialerTypeEntryProcessor extends AbstractEntryProcessor<AgentQueueKey, AgentWeightedPriority> {

        private DialerType dialerType;

        private SetDialerTypeEntryProcessor() {
        }

        public SetDialerTypeEntryProcessor(DialerType dialerType) {
            this.dialerType = dialerType;
        }

        @Override
        protected Object process(Map.Entry<AgentQueueKey, AgentWeightedPriority> entry, boolean isPrimary) {
            AgentWeightedPriority value = entry.getValue();
            value.setDialerType(dialerType);
            entry.setValue(value);
            return null;
        }

        @Override
        public void writeData(ObjectDataOutput out) throws IOException {
            super.writeData(out);
            DialerType.write(out, dialerType);
        }

        @Override
        public void readData(ObjectDataInput in) throws IOException {
            super.readData(in);
            dialerType = DialerType.read(in);
        }

    }

    public static class SetHasWaitingCallEntryProcessor extends AbstractEntryProcessor<AgentQueueKey, AgentWeightedPriority> {

        private boolean hasWaitingCall;

        private SetHasWaitingCallEntryProcessor() {
        }

        public SetHasWaitingCallEntryProcessor(boolean hasWaitingCall) {
            this.hasWaitingCall = hasWaitingCall;
        }

        @Override
        protected Object process(Map.Entry<AgentQueueKey, AgentWeightedPriority> entry, boolean isPrimary) {
            AgentWeightedPriority value = entry.getValue();
            value.setHasWaitingCalls(hasWaitingCall);
            entry.setValue(value);
            return null;
        }

        @Override
        public void writeData(ObjectDataOutput out) throws IOException {
            super.writeData(out);
            out.writeBoolean(hasWaitingCall);
        }

        @Override
        public void readData(ObjectDataInput in) throws IOException {
            super.readData(in);
            hasWaitingCall = in.readBoolean();
        }

    }

    public static class SetIsRunningEntryProcessor extends AbstractEntryProcessor<AgentQueueKey, AgentWeightedPriority> {

        private boolean isRunning;

        private SetIsRunningEntryProcessor() {
        }

        public SetIsRunningEntryProcessor(boolean isRunning) {
            this.isRunning = isRunning;
        }

        @Override
        protected Object process(Map.Entry<AgentQueueKey, AgentWeightedPriority> entry, boolean isPrimary) {
            AgentWeightedPriority value = entry.getValue();
            value.setIsRunning(isRunning);
            entry.setValue(value);
            return null;
        }

        @Override
        public void writeData(ObjectDataOutput out) throws IOException {
            super.writeData(out);
            out.writeBoolean(isRunning);
        }

        @Override
        public void readData(ObjectDataInput in) throws IOException {
            super.readData(in);
            isRunning = in.readBoolean();
        }

    }

}
