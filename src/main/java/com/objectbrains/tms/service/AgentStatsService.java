/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.objectbrains.tms.service;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.HazelcastInstanceAware;
import com.hazelcast.core.IMap;
import com.hazelcast.core.Member;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.query.Predicate;
import com.hazelcast.spring.context.SpringAware;
import com.objectbrains.hcms.hazelcast.HazelcastService;
import com.objectbrains.scheduler.annotation.QuartzJob;
import com.objectbrains.sti.db.entity.base.dialer.DialerQueueSettings;
import com.objectbrains.tms.enumerated.AgentState;
import com.objectbrains.tms.hazelcast.AbstractEntryProcessor;
import com.objectbrains.tms.hazelcast.Configs;
import com.objectbrains.tms.hazelcast.entity.Agent;
import com.objectbrains.tms.hazelcast.entity.AgentStats;
import com.objectbrains.tms.service.dialer.DialerService;
import com.objectbrains.tms.websocket.Websocket;
import com.objectbrains.tms.websocket.WebsocketService;
import com.objectbrains.tms.websocket.message.Function;
import com.objectbrains.tms.websocket.message.outbound.Send;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import javax.annotation.PostConstruct;
import org.joda.time.Duration;
import org.joda.time.LocalDateTime;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

/**
 *
 * @author connorpetty
 */
@Service(AgentStatsService.BEAN_NAME)
public class AgentStatsService {

    public static final String BEAN_NAME = "agentStatsService";

    private static final Logger LOG = LoggerFactory.getLogger(AgentStatsService.class);

    @Autowired
    private HazelcastService hazelcastService;

    @Autowired
    private DialerService dialerService;

    @Autowired
    private DialerQueueRecordService recordService;

    private IMap<Integer, AgentStats> statsMap;

    @Autowired
    private AgentQueueAssociationService associationService;

    @Autowired
    @Lazy
    private Websocket websocket;

    @Autowired
    @Lazy
    private WebsocketService websocketService;

    @PostConstruct
    private void init() {
        statsMap = hazelcastService.getMap(Configs.AGENT_STATS_MAP);
    }

    public AgentStats getAgentStats(int agentExt) {
        return statsMap.get(agentExt);
    }

    public AgentStats getAgentStats(Agent agent) {
        return statsMap.get(agent.getExtension());
    }

    public Map<Integer, AgentStats> getAgentStats(Collection<Agent> agents) {
        return getAgentStats(Utils.getExtensions(agents));
    }

    public Map<Integer, AgentStats> getAgentStats(Set<Integer> extensions) {
        return statsMap.getAll(extensions);
    }

    public Map<Integer, AgentStats> getAllAgentStats() {
        return statsMap;
    }

    public void startStats(int agentExt) {
        statsMap.executeOnKey(agentExt, new StartStatEntryProcessor());
//        AgentStats stats = getAgentStats(agentExt);
//        if (stats == null || stats.isExpired()) {
//            stats = agentStatsRepository.startStats(agentExt);
//            statsMap.putTransient(agentExt, stats, 0, TimeUnit.SECONDS);
//        }
//        return stats;
    }

    public void stopStats(int agentExt) {
        statsMap.executeOnKey(agentExt, new StopStatEntryProcessor());
    }

    public AgentState getAgentState(int agentExt) {
        return (AgentState) statsMap.executeOnKey(agentExt, new GetAgentStateEntryProcessor());
    }

    @SuppressWarnings("unchecked")
    public Map<Integer, AgentState> getAgentStates(Set<Integer> extensions) {
        if (extensions == null || extensions.isEmpty()) {
            return Collections.emptyMap();
        }
        return (Map) statsMap.executeOnKeys(extensions, new GetAgentStateEntryProcessor());
    }

    public void setAgentToIdle(int agentExt) {
        Set<Long> queuePks = associationService.getAgentQueues(agentExt);
        Map<Long, DialerQueueSettings> settings = recordService.getQueueSettings(queuePks);
        int minIdleMinutes = Integer.MAX_VALUE;
        for (DialerQueueSettings setting : settings.values()) {
            if (setting.getIdleMaxMinutes() != null
                    && setting.getIdleMaxMinutes() < minIdleMinutes) {
                minIdleMinutes = setting.getIdleMaxMinutes();
            }
        }
        setState(agentExt, AgentState.IDLE, Duration.standardMinutes(minIdleMinutes));
        dialerService.agentReady(agentExt);
    }

    /**
     * Sets agent state to Wrap using the given queuePks to determine the
     * stateThresholdDuration.
     *
     * @param agentExt
     * @param queuePks
     */
    public void setAgentToWrap(int agentExt, Set<Long> queuePks) {
        Map<Long, DialerQueueSettings> settings = recordService.getQueueSettings(queuePks);
        int minWrapMinutes = Integer.MAX_VALUE;
        for (DialerQueueSettings setting : settings.values()) {
            if (setting.getWrapMaxMinutes() != null
                    && setting.getWrapMaxMinutes() < minWrapMinutes) {
                minWrapMinutes = setting.getWrapMaxMinutes();
            }
        }
        setState(agentExt, AgentState.WRAP, Duration.standardMinutes(minWrapMinutes));
    }

    public boolean setState(int agentExt, AgentState newState, Duration stateThresholdDuration) {
        SetAgentStateEntryProcessor processor = new SetAgentStateEntryProcessor(newState, stateThresholdDuration);
        AgentStats stats = (AgentStats) statsMap.executeOnKey(agentExt, processor);
        if (stats != null) {
            websocketService.sendUpdateOfDirectory();
            Send send = new Send(Function.AGENT_STATS);
            send.setAgentStats(stats);
            websocket.sendWithRetry(agentExt, send);
            return true;
        }
        return false;
    }

    public boolean isDialerActive(int agentExt) {
        return (Boolean) statsMap.executeOnKey(agentExt, new GetAgentDialerActive());
    }

    public boolean setDialerActive(int agentExt, boolean dialerActive) {
        SetAgentDialerActive processor = new SetAgentDialerActive(dialerActive);
        AgentStats stats = (AgentStats) statsMap.executeOnKey(agentExt, processor);
        if (stats != null) {
            websocketService.sendUpdateOfDirectory();
            Send send = new Send(Function.AGENT_STATS);
            send.setAgentStats(stats);
            websocket.sendWithRetry(agentExt, send);

            AgentState state = stats.getState();
            if (state == AgentState.IDLE && dialerActive) {
                dialerService.agentReady(agentExt);
            }
            return true;
        }
        return false;
    }

    @Bean
    public static Trigger stopExpiredStatsSweepTrigger() {
        return TriggerBuilder.newTrigger()
                .withIdentity("stopExpiredStatsSweep")
                .forJob("stopExpiredStatsSweep")
                .withSchedule(SimpleScheduleBuilder.repeatSecondlyForever(30)
                        .withMisfireHandlingInstructionIgnoreMisfires())
                .startNow()
                .build();
    }

    @QuartzJob(name = "stopExpiredStatsSweep", disallowConcurrentExecution = true)
    public void stopExpiredStatsSweep() {
        statsMap.executeOnEntries(new StopStatEntryProcessor(), new ExpiredStatsPredicate());
    }

    public static class ExpiredStatsPredicate implements Predicate<Integer, AgentStats> {

        @Override
        public boolean apply(Map.Entry<Integer, AgentStats> mapEntry) {
            AgentStats stats = mapEntry.getValue();
            return stats != null && stats.isExpired();
        }

    }

    public static abstract class UpdatingStatEntryProcessor extends AbstractEntryProcessor<Integer, AgentStats> {

        protected LocalDateTime now = LocalDateTime.now();

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

    public static class StartStatEntryProcessor extends UpdatingStatEntryProcessor implements HazelcastInstanceAware {

        private HazelcastInstance hazelcastInstance;

        @Override
        public Object process(Map.Entry<Integer, AgentStats> entry, boolean isPrimary) {
            AgentStats stats = entry.getValue();
//            //only start if there are no previous stats or the old state is expired
            if (stats == null || stats.isExpired(now)) {
                stats = new AgentStats();
            }
            boolean modified = false;
            if (!stats.hasStarted()) {
                stats.start(now);
                modified = true;
            }

            Member localMember = hazelcastInstance.getCluster().getLocalMember();
            String address = localMember.getSocketAddress().getAddress().getHostName();
            if (!address.equals(stats.getHostAddress())) {
                stats.setHostAddress(address);
                modified = true;
            }

            if (modified) {
                entry.setValue(stats);
            }
//            if (stats == null || stats.hasStarted()) {
//                stats = new AgentStats();
//            }
//            stats.start(now);
//            entry.setValue(stats);
            return null;
        }

        @Override
        public void setHazelcastInstance(HazelcastInstance hazelcastInstance) {
            this.hazelcastInstance = hazelcastInstance;
        }

    }

    @SpringAware
    public static class StopStatEntryProcessor extends UpdatingStatEntryProcessor {

        @Autowired
        @Qualifier(Websocket.BEAN_NAME)
        private Websocket websocket;

        @Override
        public Object process(Map.Entry<Integer, AgentStats> entry, boolean isPrimary) {
            AgentStats stats = entry.getValue();
            if (stats == null) {
                LOG.error("Unable to stop stats for agent {}, stats is null", entry.getKey());
            } else if (!stats.hasStarted()) {
                LOG.error("Unable to stop stats for agent {}, stats were not started", entry.getKey());
            } else if (!stats.hasStopped()) {
                stats.stopWithRedaction(now);
                entry.setValue(stats);
                Send send = new Send(Function.AGENT_STATS_EXPIRED);
                if (isPrimary) {
                    websocket.sendWithRetry(entry.getKey(), send);
                }
            } else {
//                entry.setValue(null);
            }
            return null;
        }

    }

    public static class GetAgentStateEntryProcessor extends AbstractEntryProcessor<Integer, AgentStats> {

        public GetAgentStateEntryProcessor() {
            super(false);
        }

        @Override
        public AgentState process(Map.Entry<Integer, AgentStats> entry, boolean isPrimary) {
            AgentStats stats = entry.getValue();
            if (stats == null) {
                return null;
            }
            return stats.getState();
        }

    }

    public static class SetAgentStateEntryProcessor extends UpdatingStatEntryProcessor {

        private AgentState newState;
        private Duration stateThresholdDuration;

        private SetAgentStateEntryProcessor() {
        }

        public SetAgentStateEntryProcessor(AgentState state, Duration stateThresholdDuration) {
            this.newState = state;
            this.stateThresholdDuration = stateThresholdDuration;
        }

        @Override
        public AgentStats process(Map.Entry<Integer, AgentStats> entry, boolean isPrimary) {
            AgentStats stats = entry.getValue();
            if (stats == null) {
                LOG.error("Unable to update stats for agent {}, stats is null", entry.getKey());
            } else if (stats.isExpired(now)) {
                LOG.error("Unable to update stats for agent {}, stats were expired", entry.getKey());
            } else if (!stats.hasStarted()) {
                LOG.error("Unable to update stats for agent {}, stats were not started", entry.getKey());
            } else if (stats.setState(newState, stateThresholdDuration, now)) {
                entry.setValue(stats);
                return stats;
            }
            return null;
        }

        @Override
        public void writeData(ObjectDataOutput out) throws IOException {
            super.writeData(out);
            AgentState.write(out, newState);
            out.writeObject(stateThresholdDuration);
        }

        @Override
        public void readData(ObjectDataInput in) throws IOException {
            super.readData(in);
            newState = AgentState.read(in);
            stateThresholdDuration = in.readObject();
        }
    }

    public static class GetAgentDialerActive extends AbstractEntryProcessor<Integer, AgentStats> {

        public GetAgentDialerActive() {
            super(false);
        }

        @Override
        public Boolean process(Map.Entry<Integer, AgentStats> entry, boolean isPrimary) {
            AgentStats stats = entry.getValue();
            return stats != null && !stats.isExpired() && stats.isDialerActive();
        }

    }

    public static class SetAgentDialerActive extends UpdatingStatEntryProcessor {

        private boolean dialerActive;

        public SetAgentDialerActive() {
        }

        public SetAgentDialerActive(boolean dialerActive) {
            this.dialerActive = dialerActive;
        }

        @Override
        public AgentStats process(Map.Entry<Integer, AgentStats> entry, boolean isPrimary) {
            AgentStats stats = entry.getValue();
            if (stats == null) {
                LOG.error("Unable to update stats for agent {}, stats is null", entry.getKey());
            } else if (stats.isExpired(now)) {
                LOG.error("Unable to update stats for agent {}, stats were expired", entry.getKey());
            } else if (!stats.hasStarted()) {
                LOG.error("Unable to update stats for agent {}, stats were not started", entry.getKey());
            } else if (stats.setDialerActive(dialerActive, now)) {
                entry.setValue(stats);
                return stats;
            }
            return null;
        }

        @Override
        public void writeData(ObjectDataOutput out) throws IOException {
            super.writeData(out);
            out.writeBoolean(dialerActive);
        }

        @Override
        public void readData(ObjectDataInput in) throws IOException {
            super.readData(in);
            dialerActive = in.readBoolean();
        }

    }
}
