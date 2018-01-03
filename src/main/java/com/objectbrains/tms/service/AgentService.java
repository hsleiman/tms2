/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.objectbrains.tms.service;

import com.hazelcast.core.IExecutorService;
import com.hazelcast.core.IMap;
import com.hazelcast.mapreduce.aggregation.impl.SetAdapter;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.DataSerializable;
import com.hazelcast.query.Predicate;
import com.hazelcast.spring.context.SpringAware;
import com.objectbrains.ams.iws.User;
import com.objectbrains.hcms.annotation.ConfigContext;
import com.objectbrains.hcms.configuration.ConfigurationUtility;
import com.objectbrains.hcms.hazelcast.HazelcastService;
import com.objectbrains.sti.constants.CallRoutingOption;
import com.objectbrains.sti.embeddable.AgentWeightPriority;
import com.objectbrains.tms.db.entity.freeswitch.CDR;
import com.objectbrains.tms.db.entity.freeswitch.TMSDialplan;
import com.objectbrains.tms.db.repository.AgentRepository;
import com.objectbrains.tms.enumerated.AgentState;
import com.objectbrains.tms.enumerated.CallDirection;
import com.objectbrains.tms.enumerated.FreeswitchContext;
import com.objectbrains.tms.enumerated.SetAgentState;
import com.objectbrains.tms.hazelcast.AbstractEntryProcessor;
import com.objectbrains.tms.hazelcast.AgentCallState;
import com.objectbrains.tms.hazelcast.AgentDialerState;
import com.objectbrains.tms.hazelcast.Configs;
import com.objectbrains.tms.hazelcast.entity.Agent;
import com.objectbrains.tms.hazelcast.entity.AgentCall;
import com.objectbrains.tms.hazelcast.entity.AgentStats;
import com.objectbrains.tms.hazelcast.entity.AgentWeightedPriority;
import com.objectbrains.tms.hazelcast.entity.WeightedPriority;
import com.objectbrains.tms.pojo.AgentStatus;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import javax.annotation.PostConstruct;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.Duration;
import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 *
 * @author connorpetty
 */
@Service
public class AgentService {

    private static final Logger LOG = LoggerFactory.getLogger(AgentService.class);

    @Autowired
    private HazelcastService hazelcastService;

    @Autowired
    private AgentRepository agentRepository;

    @Autowired
    private AgentStatsService statsService;

    @Autowired
    private AgentCallService callService;

    @Autowired
    private AmsService amsService;

    @ConfigContext
    private ConfigurationUtility config;

    @Autowired
    private FreeswitchConfiguration configuration;

    @Autowired
    private FreeswitchNodeService freeswitchNodeService;

    private IMap<Integer, Agent> agentMap;
    private IExecutorService executor;

    @PostConstruct
    private void init() {
        agentMap = hazelcastService.getMap(Configs.AGENT_MAP);
        executor = hazelcastService.getExecutorService(Configs.DIALER_EXECUTOR_SERVICE);
    }

    private Agent loadAgent(String userName) {
        User user = amsService.getUser(userName);
        if (user == null) {
            return null;
        }
        int ext = user.getExtension();
        Agent agent = getAgent(ext);
        if (!userName.equals(agent.getUserName())) {
            agentRepository.changeUserName(ext, userName);
            agentMap.evict(ext);
            agent = getAgent(ext);
        }
        return agent;
//        return loadAgent(userName, 0);
    }
//
//    private Agent loadAgent(String userName, int attemptCount) {
//        Agent agent = null;
//        try {
//            agent = agentRepository.getAgent(userName).getInfo();
//            agentMap.putTransient(agent.getExtension(), agent, 0, TimeUnit.SECONDS);
//        } catch (DataIntegrityViolationException ex) {
//            //this probably means that the agent was inserted in another thread at the same time
//            //if that is the case we can try to retrieve it from hazelcast
//            if (attemptCount < config.getInteger("loadAgent.maxRetryCount", 3)) {
//                agent = getAgent(userName, attemptCount + 1);
//            }
//        }
//        return agent;
//    }

    public Agent getAgent(String userName) {
        Collection<Agent> agents = agentMap.values(new AgentNamePredicate(userName));
        if (agents.size() == 1) {
            return agents.iterator().next();
        }
        Agent realAgent = loadAgent(userName);
        //need to get rid of any agents that are not the authentic one.
        for (Agent agent : agents) {
            if (!agent.equals(realAgent)) {
                agentMap.evict(agent.getExtension());
            }
        }
        return realAgent;
    }

//    private Agent getAgent(String userName, int attemptCount) {
//        Collection<Agent> agents = agentMap.values(new AgentNamePredicate(userName));
//        if (agents.isEmpty()) {
//            return loadAgent(userName, attemptCount);
//        }
//        return agents.iterator().next();
//    }
    public List<Agent> getAgents(Collection<String> agentNames) {
        List<Agent> agents = new ArrayList<>(agentMap.values(new AgentNamesPredicate(agentNames)));
        Set<String> namesNotFound = new HashSet<>(agentNames);
        List<Agent> agentsToRemove = null;
        for (Agent agent : agents) {
            if (!namesNotFound.remove(agent.getUserName())) {
                //the names was already removed, so it must be a duplicate
                String duplicateName = agent.getUserName();
                //need to find the first duplicate agent
                Agent dupAgent = findFirstAgentWithName(agents, duplicateName);
                if (agentsToRemove == null) {
                    agentsToRemove = new ArrayList<>();
                }
                Agent trueAgent = loadAgent(duplicateName);
                if (trueAgent == null) {
                    agentsToRemove.add(agent);
                    agentsToRemove.add(dupAgent);
                } else if (trueAgent.equals(dupAgent)) {
                    agentsToRemove.add(agent);
                } else {
                    agentsToRemove.add(dupAgent);
                }
            }
        }
        if (agentsToRemove != null) {
            for (Agent agent : agentsToRemove) {
                agents.remove(agent);
                agentMap.evict(agent.getExtension());
            }
        }
        for (String agentName : namesNotFound) {
            Agent agent = loadAgent(agentName);
            if (agent != null) {
                agents.add(agent);
            }
        }
        return agents;
    }

    private static Agent findFirstAgentWithName(Collection<Agent> agents, String userName) {
        for (Agent agent : agents) {
            if (agent.getUserName().equals(userName)) {
                return agent;
            }
        }
        return null;
    }

    public List<Agent> getAgents(List<AgentWeightPriority> agentWeightPriorities,
            com.objectbrains.sti.embeddable.WeightedPriority defaultWeightedPriorioty,
            CallRoutingOption callOrder) {
        return getAgents(Utils.convertToMap(agentWeightPriorities),
                defaultWeightedPriorioty == null ? null : new WeightedPriority(defaultWeightedPriorioty),
                callOrder);
    }

    public List<Agent> getAgents(Map<Integer, AgentWeightedPriority> agentWeightPriorities,
            com.objectbrains.sti.embeddable.WeightedPriority defaultWeightedPriorioty,
            CallRoutingOption callOrder) {
        if (agentWeightPriorities.isEmpty()) {
            return Collections.emptyList();
        }
        Map<Integer, Agent> agents = agentMap.getAll(agentWeightPriorities.keySet());
        Map<String, AgentWeightedPriority> weights = new HashMap<>();

        for (Map.Entry<Integer, AgentWeightedPriority> entrySet : agentWeightPriorities.entrySet()) {
            Integer key = entrySet.getKey();
            AgentWeightedPriority value = entrySet.getValue();
            weights.put(agents.get(key).getUserName(), value);
        }
        List<Agent> list = new ArrayList<>(agents.values());
        sortAgents(list, weights,
                defaultWeightedPriorioty == null ? null : new WeightedPriority(defaultWeightedPriorioty),
                callOrder);
        return list;
    }

    public List<Agent> getAgents(Map<String, AgentWeightedPriority> agentWeightPriorities,
            final WeightedPriority defaultWeightedPriorioty, CallRoutingOption callOrder) {
        List<Agent> agents = getAgents(agentWeightPriorities.keySet());
        sortAgents(agents, agentWeightPriorities, defaultWeightedPriorioty, callOrder);
        return agents;
    }

    private void sortAgents(List<Agent> agents,
            Map<String, AgentWeightedPriority> agentWeightPriorities,
            final WeightedPriority defaultWeightedPriorioty, CallRoutingOption callOrder) {
        if (callOrder == null) {
            callOrder = CallRoutingOption.ROUND_ROBIN;
        }
        Collections.shuffle(agents);
        Comparator<Agent> comparator = null;
        switch (callOrder) {
            case LONGEST_IDLE:
                comparator = new AgentIdleComparator(agentWeightPriorities, statsService.getAgentStats(agents), false);
                break;
            case SHORTEST_IDLE:
                comparator = new AgentIdleComparator(agentWeightPriorities, statsService.getAgentStats(agents), true);
                break;
            case SKILL_BASED:
                comparator = new SkillBasedComparator(agentWeightPriorities, defaultWeightedPriorioty);
                break;
            case ROUND_ROBIN_UTILIZATION:
                comparator = new UtilizationComparator(agentWeightPriorities, statsService.getAgentStats(agents), config.getDouble("utilizationThreshold", 0.8));
                break;
            case ROUND_ROBIN:
                comparator = new AgentGroupComparator(agentWeightPriorities);
                break;
            default:
                throw new IllegalArgumentException();
        }
        Collections.sort(agents, comparator);
    }

    public Integer getAgentExtension(String userName) {
        Set<Integer> extentions = agentMap.keySet(new AgentNamePredicate(userName));
        if (extentions.isEmpty()) {
            Agent agent = loadAgent(userName);
            if (agent != null) {
                return agent.getExtension();
            }
            return null;
        }
        return extentions.iterator().next();
    }

    public Agent getAgent(Integer extension) {
        if (extension == null) {
            LOG.warn("Attempted to look up agent with null extension. Ignoring");
            return null;
        }
        //Agents cannot have an extension larger than 9999
        if (extension > 9999) {
            return null;
        }
        return agentMap.get(extension);
    }

    public AgentStatus getAgentStatus(int ext) {
        Agent agent = getAgent(ext);
        AgentCall agentCall = callService.getActiveCall(ext);
        return new AgentStatus(agent, statsService.getAgentStats(ext), agentCall);
    }

    public boolean agentExists(int ext) {
        //Agents cannot have an extension larger than 9999
        if (ext > 9999) {
            return false;
        }
        boolean exists = agentMap.containsKey(ext);
        LOG.info("Agent Exists {}: {}", ext, exists);
        return exists;
    }

    public void updateAgentLastHangupCause(int ext, String lastHangupCause) {
        agentMap.executeOnKey(ext, new SetAgentLastHangupCauseEntryProcessor(lastHangupCause));
    }

    public void setAgentState(int ext, SetAgentState setAgentState) {
        if (setAgentState == null) {
            LOG.warn("setAgentState was null, ignoring");
            return;
        }
        LOG.info("Setting Ext {} to {}", ext, setAgentState.getAgentState());

        agentMap.executeOnKey(ext, new SetAgentStateEntryProcessor(setAgentState));

        switch (setAgentState) {
            case IDLE:
                if (statsService.isDialerActive(ext)) {
                    break;
                }
                statsService.setAgentToIdle(ext);
//            case HOLD:
//            case ONCALL:
//            case PREVIEW:
//            case WRAP:
                break;
//            //case LOGOFF:
//            //statsService.stopStats(ext);
//            //  break;
//            case OFFLINE:
//            case SESSION_TIMEOUT:
//            case FORCE_OFFLINE:
////                statsService.setDialerActive(ext, false);
//
//                break;
            default:
                if (!setAgentState.getAgentState().isReadyState()) {
                    statsService.setState(ext, setAgentState.getAgentState(), Duration.ZERO);
                    callService.clearAgentCalls(ext);
                }
                break;
        }
    }

    public void setFreeswitchIpAndDomain(int ext, String freeswitchIP, String domain, User user, String userIP) {
        freeswitchNodeService.assignNodeForAgent(ext, domain);
        agentMap.executeOnKey(ext, new SetFreeswitchIpAndDomainEntryProcessor(freeswitchIP, domain, user, userIP));
    }

    public void updateUserRawData(int ext, User user) {
        if (user != null) {
            agentMap.executeOnKey(ext, new UpdateUserRawDataEntryProcessor(user));
        }
    }

    public void updateLastHeartbeat(int ext) {
        agentMap.executeOnKey(ext, new SetLastHartbeatTimeEntryProcessor());
    }

    @Async
    public void updateLastInboundCallTimes(String ext) {
        try {
            Integer i = Integer.parseInt(ext);
            if (i != null) {
                updateLastInboundCallTimes(i);
            }
        } catch (Exception ex) {
            LOG.error("Could not parse extension form {}", ext);
        }
    }

    public void updateLastInboundCallTimes(int ext) {
        agentMap.executeOnKey(ext, new SetLastInboundTimeEntryProcessor());
    }

    @Async
    public void updateLastOutboundCallTimes(String ext) {
        try {
            Integer i = Integer.parseInt(ext);
            if (i != null) {
                updateLastOutboundCallTimes(i);
            }
        } catch (Exception ex) {
            LOG.error("Could not parse extension form {}", ext);
        }
    }

    public void updateLastOutboundCallTimes(int ext) {
        agentMap.executeOnKey(ext, new SetLastOutboundTimeEntryProcessor());
    }

    public void updateAgentOnCall(TMSDialplan tmsDialplan) {
        Set<Integer> extensions = new HashSet<>();
        addExtension(extensions, tmsDialplan.getCallee());
        addExtension(extensions, tmsDialplan.getCaller());
        for (int ext : extensions) {
            updateAgentOnCall(ext, tmsDialplan);
        }
    }

    public Boolean updateAgentOnCall(int ext, TMSDialplan tmsDialplan) {
        Boolean value = null;
        Agent agent = getAgent(ext);
        if (agent == null) {
            return value;
        }
        LOG.info("Updating Agent On Call: " + agent.getExtension());

//        agent.setOnCallStartTimestamp(LocalDateTime.now());
        Boolean ignore_disposition = tmsDialplan.getIgnore_disposition();
        Boolean activateAgentOnCall = tmsDialplan.getActivateAgentOnCall();
        LOG.info("Updating Agent ignore_disposition : " + ignore_disposition);

        if (activateAgentOnCall == null || activateAgentOnCall == true) {
            if (ignore_disposition == null) {
                Long loanId = tmsDialplan.getBorrowerInfo().getLoanId();
                LOG.info("Updating Agent ignore_disposition : {}, {}", loanId, (loanId == null));
                value = callService.callStarted(agent.getExtension(),
                        tmsDialplan.getCall_uuid(),
                        null,
                        loanId == null,
                        tmsDialplan.getBorrowerInfo(),
                        tmsDialplan.getCallDirection(),
                        tmsDialplan.getDialerQueueId(),
                        tmsDialplan.getUniqueID(),
                        tmsDialplan.getDialer(),
                        configuration.getCallWaitTimeoutBeforeConnect(tmsDialplan.getCallDirection()));
                LOG.info("1. Updating agent on call service : {}, {}, value: {}, UniqueUD: {}", agent.getExtension(), tmsDialplan.getCall_uuid(), value, tmsDialplan.getUniqueID());
            } else {
                value = callService.callStarted(agent.getExtension(),
                        tmsDialplan.getCall_uuid(),
                        null,
                        ignore_disposition,
                        tmsDialplan.getBorrowerInfo(),
                        tmsDialplan.getCallDirection(),
                        tmsDialplan.getDialerQueueId(),
                        tmsDialplan.getUniqueID(),
                        tmsDialplan.getDialer(),
                        configuration.getCallWaitTimeoutBeforeConnect(tmsDialplan.getCallDirection()));
                LOG.info("2. Updating agent on call service : {}, {}, value: {}, UniqueUD: {}", agent.getExtension(), tmsDialplan.getCall_uuid(), value, tmsDialplan.getUniqueID());
            }
        }
        AgentState state = statsService.getAgentState(agent.getExtension());
        LOG.info(agent.getExtension() + " -> Set Ready: " + state);
        agentMap.executeOnKey(ext, new SetOnCallStartTimestampEntryProcessor());
        return value;
    }

    public void updateAgentOffCall(CDR cdr) {
        //updateAgent(cdr);
        Set<Integer> extensions = new HashSet<>();
        addExtension(extensions, cdr.getCaller_id_number());
        addExtension(extensions, cdr.getCallee_id_number());

        for (int ext : extensions) {
            LOG.info("Is Dialer Call: " + cdr.getDialer());
            updateAgentOffCall(ext, cdr);
        }
    }

    private void updateAgentOffCall(int ext, CDR cdr) {
        Agent agent = getAgent(ext);
        if (agent == null) {
            return;
        }
        LOG.info("Updating Agent Off Call: " + agent.getExtension());
        agentMap.executeOnKey(ext, new SetOnCallEndTimestampEntryProcessor());
    }

    public void setScreenMonitored(int ext, boolean screenMonitored) {
        agentMap.executeOnKey(ext, new SetScreenMonitoredEntryProcessor(screenMonitored));
    }

    private void addExtension(Set<Integer> extensions, String potentialExtension) {
        if (potentialExtension == null || potentialExtension.length() != 4) {
            return;
        }
        try {
            extensions.add(Integer.parseInt(potentialExtension));
        } catch (NumberFormatException ex) {
            //Do nothing
        }
    }

    public String getFreeswitchIPForExt(Integer ext) {
        LOG.info("Freeswitch loadbalancer is currently: {}", configuration.useFreeswitchLoadBalancer());
        LOG.info("Freeswitch loadbalancer is currently: {}", configuration.useFreeswitchLoadBalancer());
        if (configuration.useFreeswitchLoadBalancer()) {
            Agent agent = getAgent(ext);
            if (agent != null) {
                return agent.getFreeswitchIP();
            }
        }
        return configuration.getFreeswitchIP(FreeswitchContext.agent_dp);
    }

    public String getFreeswitchDomainForExt(Integer ext) {
        Agent agent = getAgent(ext);
        if (agent != null) {
            return agent.getFreeswitchDomain();
        }
        throw new NullPointerException("Could not find agent with ext=" + ext);
    }

    public AgentDialerState getAgentDialerState(int agentExt, CallDirection callDirection, boolean autoDialed) {
        try {
            return executor.submitToKeyOwner(new GetAgentDialerState(agentExt, callDirection, autoDialed), agentExt).get();
        } catch (InterruptedException ex) {
            LOG.error("Unable to get AgentDialerState", ex);
        } catch (ExecutionException ex) {
            LOG.error("Unable to get AgentDialerState", ex.getCause());
        }
        return null;
    }

    public Map<Integer, AgentDialerState> getAgentDialerStates(Set<Integer> extensions, CallDirection callDirection, boolean autoDialed) {
        Map<Integer, Future<AgentDialerState>> futures = new HashMap<>();
        for (Integer extension : extensions) {
            futures.put(extension, executor.submitToKeyOwner(new GetAgentDialerState(extension, callDirection, autoDialed), extension));
        }

        Map<Integer, AgentDialerState> states = new HashMap<>();

        for (Map.Entry<Integer, Future<AgentDialerState>> entrySet : futures.entrySet()) {
            Integer key = entrySet.getKey();
            Future<AgentDialerState> future = entrySet.getValue();
            try {
                states.put(key, future.get());
            } catch (InterruptedException ex) {
                LOG.error("Unable to get AgentDialerState", ex);
            } catch (ExecutionException ex) {
                LOG.error("Unable to get AgentDialerState", ex.getCause());
            }
        }
        return states;
    }

    @SpringAware
    private static class GetAgentDialerState implements Callable<AgentDialerState>, DataSerializable {

        @Autowired
        @Qualifier(AgentCallService.BEAN_NAME)
        private AgentCallService callService;

        @Autowired
        @Qualifier(AgentStatsService.BEAN_NAME)
        private AgentStatsService statsService;

        private int extension;
        private CallDirection callDirection;
        private boolean autoDialed;

        public GetAgentDialerState(int extension, CallDirection callDirection, boolean autoDialed) {
            this.extension = extension;
            this.callDirection = callDirection;
            this.autoDialed = autoDialed;
        }

        private GetAgentDialerState() {
        }

        @Override
        public AgentDialerState call() throws Exception {
            AgentDialerState state = new AgentDialerState();
            AgentStats stats = statsService.getAgentStats(extension);
            if (stats == null) {
                state.setCannotReceive(true);
                return state;
            }
            state.setDialerActive(stats.isDialerActive());
            state.setState(stats.getState());

            AgentCallState callState = callService.getAgentCallState(extension, callDirection, autoDialed);
            state.copyFrom(callState);
            return state;
        }

        @Override
        public void writeData(ObjectDataOutput out) throws IOException {
            out.writeInt(extension);
            CallDirection.write(out, callDirection);
            out.writeBoolean(autoDialed);
        }

        @Override
        public void readData(ObjectDataInput in) throws IOException {
            extension = in.readInt();
            callDirection = CallDirection.read(in);
            autoDialed = in.readBoolean();
        }

    }

    public static class AgentNamePredicate implements Predicate<Object, Agent>, DataSerializable {

        private String userName;

        private AgentNamePredicate() {
        }

        public AgentNamePredicate(String userName) {
            this.userName = userName;
            if (userName == null) {
                throw new NullPointerException("userName is null");
            }
        }

        @Override
        public boolean apply(Map.Entry<Object, Agent> mapEntry) {
            return userName.equals(mapEntry.getValue().getUserName());
        }

        @Override
        public void writeData(ObjectDataOutput out) throws IOException {
            out.writeUTF(userName);
        }

        @Override
        public void readData(ObjectDataInput in) throws IOException {
            userName = in.readUTF();
        }

    }

    public static class AgentNamesPredicate implements Predicate<Object, Agent>, DataSerializable {

        private final SetAdapter<String> userNames = new SetAdapter<>();

        private AgentNamesPredicate() {
        }

        public AgentNamesPredicate(Collection<String> userNames) {
            this.userNames.addAll(userNames);
        }

        @Override
        public boolean apply(Map.Entry<Object, Agent> mapEntry) {
            return userNames.contains(mapEntry.getValue().getUserName());
        }

        @Override
        public void writeData(ObjectDataOutput out) throws IOException {
            userNames.writeData(out);
        }

        @Override
        public void readData(ObjectDataInput in) throws IOException {
            userNames.readData(in);
        }

    }

    private static abstract class AgentComparator implements Comparator<Agent> {

        protected final Map<String, AgentWeightedPriority> agentWeightPriorities;

        public AgentComparator(Map<String, AgentWeightedPriority> agentWeightPriorities) {
            this.agentWeightPriorities = agentWeightPriorities;
        }

        @Override
        public final int compare(Agent o1, Agent o2) {
            AgentWeightedPriority awp1 = agentWeightPriorities.get(o1.getUserName());
            AgentWeightedPriority awp2 = agentWeightPriorities.get(o2.getUserName());

            Boolean isPrimary1 = (awp1 != null) ? awp1.getPrimaryGroup() : null;
            Boolean isPrimary2 = (awp2 != null) ? awp2.getPrimaryGroup() : null;

            // order: {primary, secondary, null}
            if (Objects.equals(isPrimary1, isPrimary2)) {
                return compareInternal(o1, o2);
            }
            if (isPrimary1 == null) {
                return 1;
            }
            if (isPrimary2 == null) {
                return -1;
            }
            return isPrimary1 ? -1 : 1;
        }

        protected abstract int compareInternal(Agent o1, Agent o2);
    }

    public static class AgentGroupComparator extends AgentComparator {

        public AgentGroupComparator(Map<String, AgentWeightedPriority> agentWeightPriorities) {
            super(agentWeightPriorities);
        }

        @Override
        protected int compareInternal(Agent o1, Agent o2) {
            return 0;
        }

    }

    public static class AgentIdleComparator extends AgentComparator {

        private final Map<Integer, AgentStats> statsMap;
        private final boolean useShortestIdle;

        public AgentIdleComparator(Map<String, AgentWeightedPriority> agentWeightPriorities,
                Map<Integer, AgentStats> statsMap, boolean useShortestIdle) {
            super(agentWeightPriorities);
            this.statsMap = statsMap;
            this.useShortestIdle = useShortestIdle;
        }

        @Override
        public int compareInternal(Agent o1, Agent o2) {
            AgentStats stat1 = statsMap.get(o1.getExtension());
            AgentStats stat2 = statsMap.get(o2.getExtension());
            AgentState state1 = (stat1 != null) ? stat1.getState() : null;
            AgentState state2 = (stat2 != null) ? stat2.getState() : null;
            if (state1 == AgentState.IDLE && state2 == AgentState.IDLE) {
                //this will be negative is agent 1 has been idle longer
                int comp = stat1.getStateStartTime().compareTo(stat2.getStateStartTime());
                if (useShortestIdle) {
                    comp = -comp;
                }
                return comp;
            } else if (state1 == AgentState.IDLE) {
                return -1;
            } else if (state2 == AgentState.IDLE) {
                return 1;
            }
            return 0;
        }

    }

    public static class UtilizationComparator extends AgentComparator {

        private final Map<Integer, AgentStats> statsMap;
        private final double utilizationThresold;

        public UtilizationComparator(Map<String, AgentWeightedPriority> agentWeightPriorities,
                Map<Integer, AgentStats> statsMap, double utilizationThresold) {
            super(agentWeightPriorities);
            this.statsMap = statsMap;
            this.utilizationThresold = utilizationThresold;
        }

        @Override
        public int compareInternal(Agent o1, Agent o2) {
            AgentStats stat1 = statsMap.get(o1.getExtension());
            AgentStats stat2 = statsMap.get(o2.getExtension());

            if (stat1 != null && stat2 != null) {
                double u1 = stat1.getUtilizationPercent();
                double u2 = stat2.getUtilizationPercent();
                if (u1 >= utilizationThresold && u2 >= utilizationThresold) {
                    return -Double.compare(u1, u2);
                } else if (u1 >= utilizationThresold) {
                    return -1;
                } else if (u2 >= utilizationThresold) {
                    return 1;
                }
            }
            return 0;
        }

    }

    public static class SkillBasedComparator extends AgentComparator {

        private final WeightedPriority defaultWeightedPriorioty;

        public SkillBasedComparator(Map<String, AgentWeightedPriority> agentWeightPriorities,
                WeightedPriority defaultWeightedPriorioty) {
            super(agentWeightPriorities);
            this.defaultWeightedPriorioty = defaultWeightedPriorioty;
        }

        @Override
        public int compareInternal(Agent o1, Agent o2) {
            WeightedPriority a1 = agentWeightPriorities.get(o1.getUserName());
            WeightedPriority a2 = agentWeightPriorities.get(o2.getUserName());
            Integer p1 = a1.getPriority();
            Integer p2 = a2.getPriority();
            if (p1 == null) {
                p1 = defaultWeightedPriorioty.getPriority();
            }
            if (p2 == null) {
                p2 = defaultWeightedPriorioty.getPriority();
            }
            return Integer.compare(p1, p2);
        }
    }
}

abstract class AgentEntryProcessor extends AbstractEntryProcessor<Integer, Agent> {

    protected LocalDateTime now = LocalDateTime.now();

    @Override
    public Object process(Map.Entry<Integer, Agent> entry, boolean isPrimary) {
        Agent agent = entry.getValue();
        if (agent != null) {
            process(agent);
            agent.setLastActivityTime(now);
            entry.setValue(agent);
        }
        return null;
    }

    protected abstract void process(Agent agent);

    @Override
    public void writeData(ObjectDataOutput out) throws IOException {
        super.writeData(out);
        out.writeObject(now);
    }

    @Override
    public void readData(ObjectDataInput in) throws IOException {
        super.readData(in);
        now = in.readObject();
    }

}

class SetScreenMonitoredEntryProcessor extends AgentEntryProcessor {

    private boolean screenMonitored;

    private SetScreenMonitoredEntryProcessor() {
    }

    public SetScreenMonitoredEntryProcessor(boolean screenMonitored) {
        this.screenMonitored = screenMonitored;
    }

    @Override
    protected void process(Agent agent) {
        agent.setScreenMonitored(screenMonitored);
    }

    @Override
    public void writeData(ObjectDataOutput out) throws IOException {
        super.writeData(out);
        out.writeBoolean(screenMonitored);
    }

    @Override
    public void readData(ObjectDataInput in) throws IOException {
        super.readData(in);
        screenMonitored = in.readBoolean();
    }

}

class SetOnCallStartTimestampEntryProcessor extends AgentEntryProcessor {

    @Override
    protected void process(Agent agent) {
        agent.setOnCallStartTimestamp(now);
    }

}

class SetOnCallEndTimestampEntryProcessor extends AgentEntryProcessor {

    @Override
    protected void process(Agent agent) {
        agent.setOnCallEndTimestamp(now);
    }

}

class SetLastOutboundTimeEntryProcessor extends AgentEntryProcessor {

    @Override
    protected void process(Agent agent) {
        agent.setLastOutboundTime(now);
    }

}

class SetLastInboundTimeEntryProcessor extends AgentEntryProcessor {

    @Override
    protected void process(Agent agent) {
        agent.setLastInboundTime(now);
    }

}

class SetLastHartbeatTimeEntryProcessor extends AgentEntryProcessor {

    @Override
    protected void process(Agent agent) {
        agent.setLastHartbeatTime(now);
    }

}

class UpdateUserRawDataEntryProcessor extends AgentEntryProcessor {

    private static final Logger LOG = LoggerFactory.getLogger(UpdateUserRawDataEntryProcessor.class);

    protected User user;

    protected UpdateUserRawDataEntryProcessor() {
    }

    public UpdateUserRawDataEntryProcessor(User user) {
        this.user = user;
    }

    @Override
    protected void process(Agent agent) {
        if (StringUtils.isNotBlank(user.getEffectiveCallerId())) {
            String number = user.getEffectiveCallerId().replaceAll("-", "");
            if (StringUtils.isNumeric(number)) {
                agent.setEffectiveCallerId(number);
                LOG.info("Set effective caller id for {} to {} --> {}", agent.getExtension(), user.getEffectiveCallerId(), agent.getEffectiveCallerId());
            } else {
                agent.setEffectiveCallerId(null);
            }
        } else {
            agent.setEffectiveCallerId(null);
        }
        agent.setPhoneNumber(user.getPhoneNumber());
    }

    @Override
    public void writeData(ObjectDataOutput out) throws IOException {
        super.writeData(out);
        out.writeObject(user);
    }

    @Override
    public void readData(ObjectDataInput in) throws IOException {
        super.readData(in);
        user = in.readObject();
    }

}

class SetFreeswitchIpAndDomainEntryProcessor extends UpdateUserRawDataEntryProcessor {

    private static final Logger LOG = LoggerFactory.getLogger(SetFreeswitchIpAndDomainEntryProcessor.class);

    private String freeswitchIP;
    private String domain;
    private String userIP;
    private String username;
    private String firstName;
    private String lastName;

    protected SetFreeswitchIpAndDomainEntryProcessor() {
        super();
    }

    public SetFreeswitchIpAndDomainEntryProcessor(String freeswitchIP, String domain, User user, String userIP) {
        super(user);
        this.freeswitchIP = freeswitchIP;
        this.domain = domain;
        this.userIP = userIP;
        this.username = user.getUserName();
        this.firstName = user.getFirstName();
        this.lastName = user.getLastName();

    }

    @Override
    protected void process(Agent agent) {
        agent.setFreeswitchIP(freeswitchIP);
        agent.setFreeswitchDomain(domain);
        agent.setUserIP(userIP);
        agent.setUserName(username);
        agent.setFirstName(firstName);
        agent.setLastName(lastName);
        int ext = agent.getExtension();
        LOG.info("Set domain for agent {} to [{}][{}]", ext, domain, freeswitchIP);
        if (user != null) {
            super.process(agent);
        }
    }

    @Override
    public void writeData(ObjectDataOutput out) throws IOException {
        super.writeData(out);
        out.writeUTF(freeswitchIP);
        out.writeUTF(domain);
        out.writeUTF(userIP);
        out.writeUTF(username);
        out.writeUTF(firstName);
        out.writeUTF(lastName);
    }

    @Override
    public void readData(ObjectDataInput in) throws IOException {
        super.readData(in);
        freeswitchIP = in.readUTF();
        domain = in.readUTF();
        userIP = in.readUTF();
        username = in.readUTF();
        firstName = in.readUTF();
        lastName = in.readUTF();
    }

}

class SetAgentStateEntryProcessor extends AgentEntryProcessor {

    private SetAgentState agentState;

    private SetAgentStateEntryProcessor() {
    }

    public SetAgentStateEntryProcessor(SetAgentState agentState) {
        this.agentState = agentState;
    }

    @Override
    protected void process(Agent agent) {
        agent.setStatusExt(agentState);
        agent.setStatusExtUpdated(now);
    }

    @Override
    public void writeData(ObjectDataOutput out) throws IOException {
        super.writeData(out);
        out.writeObject(agentState);
    }

    @Override
    public void readData(ObjectDataInput in) throws IOException {
        super.readData(in);
        agentState = in.readObject();
    }

}

class SetAgentLastHangupCauseEntryProcessor extends AgentEntryProcessor {

    private String lastHangupCause;

    private SetAgentLastHangupCauseEntryProcessor() {
    }

    public SetAgentLastHangupCauseEntryProcessor(String lastHangupCause) {
        this.lastHangupCause = lastHangupCause;
    }

    @Override
    protected void process(Agent agent) {
        agent.setLastHangupCause(lastHangupCause);
    }

    @Override
    public void writeData(ObjectDataOutput out) throws IOException {
        super.writeData(out);
        out.writeObject(lastHangupCause);
    }

    @Override
    public void readData(ObjectDataInput in) throws IOException {
        super.readData(in);
        lastHangupCause = in.readObject();
    }

}
