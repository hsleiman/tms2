/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.tms.service.dialer;

import com.hazelcast.core.IMap;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.objectbrains.hcms.hazelcast.HazelcastService;
import com.amp.crm.service.dialer.DialerQueueService;
import com.amp.crm.service.tms.TMSService;
import com.amp.tms.enumerated.DialerType;
import com.amp.tms.hazelcast.AbstractEntryProcessor;
import com.amp.tms.hazelcast.Configs;
import com.amp.tms.hazelcast.entity.AgentTMS;
import com.amp.tms.hazelcast.entity.DialerCall;
import com.amp.tms.hazelcast.entity.DialerLoan;
import com.amp.tms.hazelcast.entity.DialerStats;
import com.amp.tms.service.AgentQueueAssociationService;
import com.amp.tms.service.TMSAgentService;
import com.amp.tms.service.dialer.Dialer.State;
import com.amp.tms.websocket.WebsocketService;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.PostConstruct;
import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

/**
 *
 * @author connorpetty
 */
@Service
public class DialerStatsService {

    private static final Logger LOG = LoggerFactory.getLogger(DialerStatsService.class);

    @Autowired
    protected TMSService tmsIws;

    @Autowired
    private HazelcastService hazelcastService;

    @Autowired
    @Lazy
    private TMSAgentService agentService;

    @Autowired
    private AgentQueueAssociationService assocService;

    @Autowired
    @Lazy
    private WebsocketService websocketService;
    
    @Autowired 
    private DialerQueueService dialerQueueService;

    private IMap<Long, DialerStats> statsMap;

    @PostConstruct
    private void initialize() {
        statsMap = hazelcastService.getMap(Configs.DIALER_STATS_MAP);
    }

    public DialerStats getStats(long queuePk) {
        return statsMap.get(queuePk);
    }

    public Map<Long, DialerStats> getStats(Set<Long> queuePks) {
        return statsMap.getAll(queuePks);
    }

    public Dialer.State getDialerState(long queuePk) {
        return (Dialer.State) statsMap.executeOnKey(queuePk, new GetDialerState());
    }

    public void startStats(long dialerPk, int loanCount, DialerType type) {
        runDialerStatsUpdater(dialerPk, new StartDialer(loanCount, type));
    }

    public void pauseStats(long dialerPk) {
        runDialerStatsUpdater(dialerPk, new PauseDialer());
    }

    public void resumeStats(long dialerPk) {
        runDialerStatsUpdater(dialerPk, new ResumeDialer());
    }

    public void stopStats(long dialerPk) {
        runDialerStatsUpdater(dialerPk, new StopDialer());
    }

    public void updateStateCount(long dialerPk, DialerLoan.State oldState, DialerLoan.State newState) {
        runDialerStatsUpdater(dialerPk, new UpdateLoanStateCount(oldState, newState));
    }

    public void updateStateCount(long dialerPk, DialerCall.State oldState, DialerCall.State newState) {
        runDialerStatsUpdater(dialerPk, new UpdateCallStateCount(oldState, newState));
    }

    public void incrementScheduledCallCount(long dialerPk) {
        runDialerStatsUpdater(dialerPk, new UpdateScheduledCallCount(true));
    }

    public void decrementScheduledCallCount(long dialerPk) {
        runDialerStatsUpdater(dialerPk, new UpdateScheduledCallCount(false));
    }

    public void incrementReadyCallCount(long dialerPk) {
        runDialerStatsUpdater(dialerPk, new UpdateReadyCallCount(true));
    }

    public void decrementReadyCallCount(long dialerPk) {
        runDialerStatsUpdater(dialerPk, new UpdateReadyCallCount(false));
    }

//    public void addCallDuration(long queuePk, long callDurationMillis) {
//        statsMap.executeOnKey(queuePk, new AddCallDuration(callDurationMillis));
//    }
    public void addResponseTime(long dialerPk, long responseTimeMillis) {
        runDialerStatsUpdater(dialerPk, new AddResponseTime(responseTimeMillis));
    }

    public void addWaitTimeMillis(long dialerPk, long waitTimeMillis) {
        runDialerStatsUpdater(dialerPk, new AddWaitTime(waitTimeMillis));
    }

    private void runDialerStatsUpdater(long dialerPk, DialerStatsUpdater entryProcessor) {
        DialerStats ret = (DialerStats) statsMap.executeOnKey(dialerPk, entryProcessor);
        if (ret == null) {
            return;
        }
        String message = null;
        switch (ret.getState()) {
            case COMPLETED:
                message = "Dialer for %s is now complete";
                break;
            case PAUSED:
                message = "Dialer for %s has been paused";
                break;
            case STOPPED:
                message = "Dialer for %s has been stopped";
                break;
            default:
                break;
        }
        assocService.setAssociationIsRunning(ret.getQueuePk(), ret.getState() == State.RUNNING);

        if (message == null) {
            return;
        }
        String queueName;
        try {
            queueName = dialerQueueService.getDialerQueueByPk(ret.getQueuePk()).getQueueName();
        } catch (Exception ex) {
            LOG.error("Unable to get queueName for queue {}", ret.getQueuePk(), ex);
            return;
        }
        List<AgentTMS> agents = agentService.getAgents(assocService.getParticipatingAgents(ret.getQueuePk()), null, null);
        websocketService.sendPushNotification(agents, String.format(message, queueName));
    }

    public static class GetDialerState extends AbstractEntryProcessor<Long, DialerStats> {

        public GetDialerState() {
            super(false);
        }

        @Override
        public Object process(Map.Entry<Long, DialerStats> entry, boolean isPrimary) {
            DialerStats stats = entry.getValue();
            if (stats != null) {
                return stats.getState();
            } else {
                return null;
            }
        }

    }

    public static abstract class DialerStatsUpdater extends AbstractEntryProcessor<Long, DialerStats> {

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

        @Override
        protected final DialerStats process(Map.Entry<Long, DialerStats> entry, boolean isPrimary) {
            DialerStats stats = entry.getValue();
            if (stats == null) {
                LOG.error("Dialer with pk " + entry.getKey() + " not found. Failed to run " + getClass().getName());
                return null;
            }
            State oldState = stats.getState();
            update(stats);
            State newState = stats.getState();
            entry.setValue(stats);
            return oldState != newState ? stats : null;
        }

        public abstract void update(DialerStats stats);

    }

    public static class StartDialer extends DialerStatsUpdater {

        private int loanCount;
        private DialerType type;

        private StartDialer() {
        }

        public StartDialer(int loanCount, DialerType type) {
            this.loanCount = loanCount;
            this.type = type;
        }

        @Override
        public void update(DialerStats stats) {
            stats.start(now, loanCount, type);
        }

        @Override
        public void writeData(ObjectDataOutput out) throws IOException {
            super.writeData(out);
            out.writeInt(loanCount);
            DialerType.write(out, type);
        }

        @Override
        public void readData(ObjectDataInput in) throws IOException {
            super.readData(in);
            loanCount = in.readInt();
            type = DialerType.read(in);
        }

    }

    public static class PauseDialer extends DialerStatsUpdater {

        @Override
        public void update(DialerStats stats) {
            stats.pause(now);
        }

    }

    public static class ResumeDialer extends DialerStatsUpdater {

        @Override
        public void update(DialerStats stats) {
            stats.resume(now);
        }
    }

    public static class StopDialer extends DialerStatsUpdater {

        @Override
        public void update(DialerStats stats) {
            stats.stop(now);
        }

    }

//    private static class AddCallDuration extends AbstractEntryProcessor<Long, DialerStats>
//            implements DataSerializable {
//
//        private long callDurationMillis;
//
//        public AddCallDuration() {
//        }
//
//        public AddCallDuration(long callDurationMillis) {
//            this.callDurationMillis = callDurationMillis;
//        }
//
//        @Override
//        public Object process(Map.Entry<Long, DialerStats> entry) {
//            DialerStats stats = entry.getValue();
//            stats.addCallDuration(callDurationMillis);
//            entry.setValue(stats);
//            return null;
//        }
//
//        @Override
//        public void writeData(ObjectDataOutput out) throws IOException {
//            out.writeLong(callDurationMillis);
//        }
//
//        @Override
//        public void readData(ObjectDataInput in) throws IOException {
//            callDurationMillis = in.readLong();
//        }
//
//    }
    public static class AddResponseTime extends DialerStatsUpdater {

        private long responseTimeMillis;

        private AddResponseTime() {
        }

        public AddResponseTime(long responseTimeMillis) {
            this.responseTimeMillis = responseTimeMillis;
        }

        @Override
        public void update(DialerStats stats) {
            stats.addRespondTime(now, responseTimeMillis);
        }

        @Override
        public void writeData(ObjectDataOutput out) throws IOException {
            super.writeData(out);
            out.writeLong(responseTimeMillis);
        }

        @Override
        public void readData(ObjectDataInput in) throws IOException {
            super.readData(in);
            responseTimeMillis = in.readLong();
        }

    }

    public static class AddWaitTime extends DialerStatsUpdater {

        private long waitTimeMillis;

        private AddWaitTime() {
        }

        public AddWaitTime(long waitTimeMillis) {
            this.waitTimeMillis = waitTimeMillis;
        }

        @Override
        public void update(DialerStats stats) {
            stats.addWaitTime(now, waitTimeMillis);
        }

        @Override
        public void writeData(ObjectDataOutput out) throws IOException {
            super.writeData(out);
            out.writeLong(waitTimeMillis);
        }

        @Override
        public void readData(ObjectDataInput in) throws IOException {
            super.readData(in);
            waitTimeMillis = in.readLong();
        }

    }

    public static class UpdateCallStateCount extends DialerStatsUpdater {

        private DialerCall.State oldState;
        private DialerCall.State newState;

        private UpdateCallStateCount() {
        }

        public UpdateCallStateCount(DialerCall.State oldState, DialerCall.State newState) {
            this.oldState = oldState;
            this.newState = newState;
        }

        @Override
        public void update(DialerStats stats) {
            stats.decrementCount(now, oldState);
            stats.incrementCount(now, newState);
        }

        @Override
        public void writeData(ObjectDataOutput out) throws IOException {
            super.writeData(out);
            DialerCall.State.write(out, oldState);
            DialerCall.State.write(out, newState);
        }

        @Override
        public void readData(ObjectDataInput in) throws IOException {
            super.readData(in);
            oldState = DialerCall.State.read(in);
            newState = DialerCall.State.read(in);
        }

    }

    public static class UpdateLoanStateCount extends DialerStatsUpdater {

        private DialerLoan.State oldState;
        private DialerLoan.State newState;

        private UpdateLoanStateCount() {
        }

        public UpdateLoanStateCount(DialerLoan.State oldState, DialerLoan.State newState) {
            this.oldState = oldState;
            this.newState = newState;
        }

        @Override
        public void update(DialerStats stats) {
            stats.decrementCount(now, oldState);
            stats.incrementCount(now, newState);
        }

        @Override
        public void writeData(ObjectDataOutput out) throws IOException {
            super.writeData(out);
            DialerLoan.State.write(out, oldState);
            DialerLoan.State.write(out, newState);
        }

        @Override
        public void readData(ObjectDataInput in) throws IOException {
            super.readData(in);
            oldState = DialerLoan.State.read(in);
            newState = DialerLoan.State.read(in);
        }

    }

    public static class UpdateReadyCallCount extends DialerStatsUpdater {

        private boolean increment;

        private UpdateReadyCallCount() {
        }

        public UpdateReadyCallCount(boolean increment) {
            this.increment = increment;
        }

        @Override
        public void update(DialerStats stats) {
            if (increment) {
                stats.incrementReadyCallCount(now);
            } else {
                stats.decrementReadyCallCount(now);
            }
        }

        @Override
        public void writeData(ObjectDataOutput out) throws IOException {
            super.writeData(out);
            out.writeBoolean(increment);
        }

        @Override
        public void readData(ObjectDataInput in) throws IOException {
            super.readData(in);
            increment = in.readBoolean();
        }

    }

    public static class UpdateScheduledCallCount extends DialerStatsUpdater {

        private boolean increment;

        private UpdateScheduledCallCount() {
        }

        public UpdateScheduledCallCount(boolean increment) {
            this.increment = increment;
        }

        @Override
        public void update(DialerStats stats) {
            if (increment) {
                stats.incrementScheduledCallCount(now);
            } else {
                stats.decrementScheduledCallCount(now);
            }
        }

        @Override
        public void writeData(ObjectDataOutput out) throws IOException {
            super.writeData(out);
            out.writeBoolean(increment);
        }

        @Override
        public void readData(ObjectDataInput in) throws IOException {
            super.readData(in);
            increment = in.readBoolean();
        }

    }
}
