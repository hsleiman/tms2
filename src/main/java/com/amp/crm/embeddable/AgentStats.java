/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.crm.embeddable;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.DataSerializable;
import com.amp.crm.constants.AgentState;
import com.amp.crm.service.utility.DurationUtils;
import java.io.IOException;
import java.util.Objects;
import javax.persistence.Embeddable;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import org.hibernate.envers.NotAudited;
import org.joda.time.Duration;
import org.joda.time.LocalDateTime;

@Embeddable
public class AgentStats implements DataSerializable {

    @NotAudited
//    @Audited(withModifiedFlag = true)
    private boolean dialerActive;
//    private Integer extension;
    @Enumerated(EnumType.STRING)
    private AgentState state = null;

//    @Audited(withModifiedFlag = true)
    @Enumerated(EnumType.STRING)
    private AgentState previousState = null;

//    @Type(type = "com.objectbrains.orm.hibernate.usertype.joda.PersistentDurationAsInterval")
//    @Type(type = "interval")
//    private Duration previousStateDuration = null;
    @NotAudited
    private Duration utilizationMeetingTime = Duration.ZERO;
    @NotAudited
    private Duration utilizationBreakTime = Duration.ZERO;
    @NotAudited
    private Duration utilizationIdleTime = Duration.ZERO;
    @NotAudited
    private Duration utilizationWrapTime = Duration.ZERO;
    @NotAudited
    private Duration utilizationHoldTime = Duration.ZERO;
    @NotAudited
    private Duration utilizationPreviewTime = Duration.ZERO;

    @NotAudited
    private Duration totalReadyTime = Duration.ZERO;
    @NotAudited
    private Duration totalMeetingTime = Duration.ZERO;
    @NotAudited
    private Duration totalBreakTime = Duration.ZERO;
    @NotAudited
    private Duration totalIdleTime = Duration.ZERO;
    @NotAudited
    private Duration totalWrapTime = Duration.ZERO;
    @NotAudited
    private Duration totalOnCallTime = Duration.ZERO;
    @NotAudited
    private Duration totalHoldTime = Duration.ZERO;
    @NotAudited
    private Duration totalPreviewTime = Duration.ZERO;
    @NotAudited
    private Duration totalDialerActiveTime = Duration.ZERO;

    @NotAudited
    private LocalDateTime dialerActiveStartTime = null;

    @NotAudited
    private LocalDateTime readyStartTime = null;
    private LocalDateTime stateStartTime = null;
    private Duration stateTimeThreshold = Duration.ZERO;

    private LocalDateTime startTime = null;
    private LocalDateTime endTime = null;

    private String hostAddress = null;

    @Override
    public void writeData(ObjectDataOutput out) throws IOException {
//        out.writeInt(extension);
        out.writeBoolean(dialerActive);
        AgentState.write(out, state);
        AgentState.write(out, previousState);
        DurationUtils.writeDuration(out, utilizationMeetingTime);
        DurationUtils.writeDuration(out, utilizationBreakTime);
        DurationUtils.writeDuration(out, utilizationIdleTime);
        DurationUtils.writeDuration(out, utilizationWrapTime);
        DurationUtils.writeDuration(out, utilizationHoldTime);
        DurationUtils.writeDuration(out, utilizationPreviewTime);
        DurationUtils.writeDuration(out, totalReadyTime);
        DurationUtils.writeDuration(out, totalMeetingTime);
        DurationUtils.writeDuration(out, totalBreakTime);
        DurationUtils.writeDuration(out, totalIdleTime);
        DurationUtils.writeDuration(out, totalWrapTime);
        DurationUtils.writeDuration(out, totalOnCallTime);
        DurationUtils.writeDuration(out, totalHoldTime);
        DurationUtils.writeDuration(out, totalPreviewTime);
        DurationUtils.writeDuration(out, totalDialerActiveTime);
        out.writeObject(dialerActiveStartTime);
        out.writeObject(readyStartTime);
        out.writeObject(stateStartTime);
        DurationUtils.writeDuration(out, stateTimeThreshold);
        out.writeObject(startTime);
        out.writeObject(endTime);
        out.writeUTF(hostAddress);
    }

    @Override
    public void readData(ObjectDataInput in) throws IOException {
//        extension = in.readInt();
        dialerActive = in.readBoolean();
        state = AgentState.read(in);
        previousState = AgentState.read(in);
        utilizationMeetingTime = DurationUtils.readDuration(in);
        utilizationBreakTime = DurationUtils.readDuration(in);
        utilizationIdleTime = DurationUtils.readDuration(in);
        utilizationWrapTime = DurationUtils.readDuration(in);
        utilizationHoldTime = DurationUtils.readDuration(in);
        utilizationPreviewTime = DurationUtils.readDuration(in);
        totalReadyTime = DurationUtils.readDuration(in);
        totalMeetingTime = DurationUtils.readDuration(in);
        totalBreakTime = DurationUtils.readDuration(in);
        totalIdleTime = DurationUtils.readDuration(in);
        totalWrapTime = DurationUtils.readDuration(in);
        totalOnCallTime = DurationUtils.readDuration(in);
        totalHoldTime = DurationUtils.readDuration(in);
        totalPreviewTime = DurationUtils.readDuration(in);
        totalDialerActiveTime = DurationUtils.readDuration(in);
        dialerActiveStartTime = in.readObject();
        readyStartTime = in.readObject();
        stateStartTime = in.readObject();
        stateTimeThreshold = DurationUtils.readDuration(in);
        startTime = in.readObject();
        endTime = in.readObject();
        hostAddress = in.readUTF();
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 37 * hash + Objects.hashCode(this.dialerActive);
        hash = 37 * hash + Objects.hashCode(this.state);
        hash = 37 * hash + Objects.hashCode(this.previousState);
        hash = 37 * hash + Objects.hashCode(this.utilizationMeetingTime);
        hash = 37 * hash + Objects.hashCode(this.utilizationBreakTime);
        hash = 37 * hash + Objects.hashCode(this.utilizationIdleTime);
        hash = 37 * hash + Objects.hashCode(this.utilizationWrapTime);
        hash = 37 * hash + Objects.hashCode(this.utilizationHoldTime);
        hash = 37 * hash + Objects.hashCode(this.utilizationPreviewTime);
        hash = 37 * hash + Objects.hashCode(this.totalReadyTime);
        hash = 37 * hash + Objects.hashCode(this.totalMeetingTime);
        hash = 37 * hash + Objects.hashCode(this.totalBreakTime);
        hash = 37 * hash + Objects.hashCode(this.totalIdleTime);
        hash = 37 * hash + Objects.hashCode(this.totalWrapTime);
        hash = 37 * hash + Objects.hashCode(this.totalOnCallTime);
        hash = 37 * hash + Objects.hashCode(this.totalHoldTime);
        hash = 37 * hash + Objects.hashCode(this.totalPreviewTime);
        hash = 37 * hash + Objects.hashCode(this.totalDialerActiveTime);
        hash = 37 * hash + Objects.hashCode(this.dialerActiveStartTime);
        hash = 37 * hash + Objects.hashCode(this.readyStartTime);
        hash = 37 * hash + Objects.hashCode(this.stateStartTime);
        hash = 37 * hash + Objects.hashCode(this.stateTimeThreshold);
        hash = 37 * hash + Objects.hashCode(this.startTime);
        hash = 37 * hash + Objects.hashCode(this.endTime);
        hash = 37 * hash + Objects.hashCode(this.hostAddress);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final AgentStats other = (AgentStats) obj;
        if (this.dialerActive != other.dialerActive) {
            return false;
        }
        if (this.state != other.state) {
            return false;
        }
        if (this.previousState != other.previousState) {
            return false;
        }
        if (!Objects.equals(this.utilizationMeetingTime, other.utilizationMeetingTime)) {
            return false;
        }
        if (!Objects.equals(this.utilizationBreakTime, other.utilizationBreakTime)) {
            return false;
        }
        if (!Objects.equals(this.utilizationIdleTime, other.utilizationIdleTime)) {
            return false;
        }
        if (!Objects.equals(this.utilizationWrapTime, other.utilizationWrapTime)) {
            return false;
        }
        if (!Objects.equals(this.utilizationHoldTime, other.utilizationHoldTime)) {
            return false;
        }
        if (!Objects.equals(this.utilizationPreviewTime, other.utilizationPreviewTime)) {
            return false;
        }
        if (!Objects.equals(this.totalReadyTime, other.totalReadyTime)) {
            return false;
        }
        if (!Objects.equals(this.totalMeetingTime, other.totalMeetingTime)) {
            return false;
        }
        if (!Objects.equals(this.totalBreakTime, other.totalBreakTime)) {
            return false;
        }
        if (!Objects.equals(this.totalIdleTime, other.totalIdleTime)) {
            return false;
        }
        if (!Objects.equals(this.totalWrapTime, other.totalWrapTime)) {
            return false;
        }
        if (!Objects.equals(this.totalOnCallTime, other.totalOnCallTime)) {
            return false;
        }
        if (!Objects.equals(this.totalHoldTime, other.totalHoldTime)) {
            return false;
        }
        if (!Objects.equals(this.totalPreviewTime, other.totalPreviewTime)) {
            return false;
        }
        if (!Objects.equals(this.totalDialerActiveTime, other.totalDialerActiveTime)) {
            return false;
        }
        if (!Objects.equals(this.dialerActiveStartTime, other.dialerActiveStartTime)) {
            return false;
        }
        if (!Objects.equals(this.readyStartTime, other.readyStartTime)) {
            return false;
        }
        if (!Objects.equals(this.stateStartTime, other.stateStartTime)) {
            return false;
        }
        if (!Objects.equals(this.stateTimeThreshold, other.stateTimeThreshold)) {
            return false;
        }
        if (!Objects.equals(this.startTime, other.startTime)) {
            return false;
        }
        if (!Objects.equals(this.endTime, other.endTime)) {
            return false;
        }
        if (!Objects.equals(this.hostAddress, other.hostAddress)) {
            return false;
        }
        return true;
    }

    private LocalDateTime now() {
        return LocalDateTime.now();
    }

    public double getUtilizationPercent() {
        return getUtilizationPercent(now());
    }

    double getUtilizationPercent(LocalDateTime now) {
        if (!hasStarted()) {
            throw new IllegalStateException("AgentStats has not been started yet");
        }
        Duration workingTime = getTotalTime(now);

        double readyTimeMillis = getTotalReadyTime(now).getMillis();
        double meetingTimeMillis = getUtilizationMeetingTime(now).getMillis();
        double idleTimeMillis = getUtilizationIdleTime(now).getMillis();
        double wrapTimeMillis = getUtilizationWrapTime(now).getMillis();
        double workingTimeMillis = workingTime.getMillis();

        return (readyTimeMillis + meetingTimeMillis - idleTimeMillis - wrapTimeMillis) / workingTimeMillis;
    }

    public boolean hasStarted() {
        return startTime != null;
    }

    public boolean hasStopped() {
        return endTime != null;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public LocalDateTime getStateStartTime() {
        return stateStartTime;
    }

    public AgentState getState() {
        return state;
    }

    public AgentState getPreviousState() {
        return previousState;
    }

    public Duration getTotalTime() {
        return getTotalTime(now());
    }

    Duration getTotalTime(LocalDateTime now) {
        return DurationUtils.getDuration(startTime, endTime != null ? endTime : now);
    }

    public Duration getTotalReadyTime() {
        return getTotalReadyTime(now());
    }

    Duration getTotalReadyTime(LocalDateTime now) {
        if (readyStartTime != null) {
            return totalReadyTime.plus(DurationUtils.getDuration(readyStartTime, now));
        }
        return totalReadyTime;
    }

    private Duration getUtilizationStateDuration(AgentState targetState, Duration totalDuration, LocalDateTime now) {
        if (state == targetState) {
            Duration stateDuration = DurationUtils.getDuration(stateStartTime, now);
            if (stateDuration.isLongerThan(stateTimeThreshold)) {
                return totalDuration.plus(stateDuration);
            }
        }
        return totalDuration;
    }

    Duration getUtilizationMeetingTime(LocalDateTime now) {
        return getUtilizationStateDuration(AgentState.MEETING, utilizationMeetingTime, now);
    }

    Duration getUtilizationBreakTime(LocalDateTime now) {
        return getUtilizationStateDuration(AgentState.BREAK, utilizationBreakTime, now);
    }

    Duration getUtilizationIdleTime(LocalDateTime now) {
        return getUtilizationStateDuration(AgentState.IDLE, utilizationIdleTime, now);
    }

    Duration getUtilizationWrapTime(LocalDateTime now) {
        return getUtilizationStateDuration(AgentState.WRAP, utilizationWrapTime, now);
    }

    Duration getUtilizationHoldTime(LocalDateTime now) {
        return getUtilizationStateDuration(AgentState.HOLD, utilizationHoldTime, now);
    }

    Duration getUtilizationPreviewTime(LocalDateTime now) {
        return getUtilizationStateDuration(AgentState.PREVIEW, utilizationPreviewTime, now);
    }

    private Duration getTotalStateDuration(AgentState targetState, Duration totalDuration, LocalDateTime now) {
        if (state == targetState) {
            return totalDuration.plus(DurationUtils.getDuration(stateStartTime, now));
        }
        return totalDuration;
    }

    public Duration getTotalMeetingTime() {
        return getTotalMeetingTime(now());
    }

    Duration getTotalMeetingTime(LocalDateTime now) {
        return getTotalStateDuration(AgentState.MEETING, totalMeetingTime, now);
    }

    public Duration getTotalBreakTime() {
        return getTotalBreakTime(now());
    }

    Duration getTotalBreakTime(LocalDateTime now) {
        return getTotalStateDuration(AgentState.BREAK, totalBreakTime, now);
    }

    public Duration getTotalOnCallTime() {
        return getTotalOnCallTime(now());
    }

    Duration getTotalOnCallTime(LocalDateTime now) {
        return getTotalStateDuration(AgentState.ONCALL, totalOnCallTime, now);
    }

    public Duration getTotalIdleTime() {
        return getTotalIdleTime(now());
    }

    Duration getTotalIdleTime(LocalDateTime now) {
        return getTotalStateDuration(AgentState.IDLE, totalIdleTime, now);
    }

    public Duration getTotalHoldTime() {
        return getTotalHoldTime(now());
    }

    Duration getTotalHoldTime(LocalDateTime now) {
        return getTotalStateDuration(AgentState.HOLD, totalHoldTime, now);
    }

    public Duration getTotalWrapTime() {
        return getTotalWrapTime(now());
    }

    Duration getTotalWrapTime(LocalDateTime now) {
        return getTotalStateDuration(AgentState.WRAP, totalWrapTime, now);
    }

    public Duration getTotalPreviewTime() {
        return getTotalPreviewTime(now());
    }

    Duration getTotalPreviewTime(LocalDateTime now) {
        return getTotalStateDuration(AgentState.PREVIEW, totalPreviewTime, now);
    }

    public Duration getTotalDialerActiveTime() {
        return getTotalDialerActiveTime(now());
    }

    Duration getTotalDialerActiveTime(LocalDateTime now) {
        if (dialerActive) {
            return totalDialerActiveTime.plus(DurationUtils.getDuration(dialerActiveStartTime, now));
        }
        return totalDialerActiveTime;
    }

    public LocalDateTime getReadyStartTime() {
        return readyStartTime;
    }

    public Duration getStateTimeThreshold() {
        return stateTimeThreshold;
    }

    public String getHostAddress() {
        return hostAddress;
    }

    public void setHostAddress(String hostAddress) {
        this.hostAddress = hostAddress;
    }

    public boolean isIdle() {
        return state == AgentState.IDLE;
    }

    public boolean isOnCall() {
        return state == AgentState.ONCALL;
    }

    public boolean setState(AgentState newState, Duration newStateTimeThreshold) {
        return setState(newState, newStateTimeThreshold, now());
    }

    public boolean setState(AgentState newState, Duration newStateTimeThreshold, LocalDateTime now) {
        if (newState == null) {
            throw new IllegalArgumentException("newState cannot be null");
        }
        if (!hasStarted()) {
            throw new IllegalStateException("AgentStats has not been started yet");
        }
        if (hasStopped()) {
            throw new IllegalStateException("AgentStats has been stopped");
        }
        AgentState oldState = this.state;
        if (oldState == newState) {
            return false;
        }

        if (oldState != null) {
            Duration stateDuration = DurationUtils.getDuration(stateStartTime, now);
            boolean isLongerThanThreshold = stateDuration.isLongerThan(stateTimeThreshold);
            switch (oldState) {
                case ONCALL:
                    totalOnCallTime = totalOnCallTime.plus(stateDuration);
                    break;
                case PREVIEW:
                    totalPreviewTime = totalPreviewTime.plus(stateDuration);
                    if (isLongerThanThreshold) {
                        utilizationPreviewTime = utilizationPreviewTime.plus(stateDuration);
                    }
                    break;
                case HOLD:
                    totalHoldTime = totalHoldTime.plus(stateDuration);
                    if (isLongerThanThreshold) {
                        utilizationHoldTime = utilizationHoldTime.plus(stateDuration);
                    }
                    break;
                case IDLE:
                    totalIdleTime = totalIdleTime.plus(stateDuration);
                    if (isLongerThanThreshold) {
                        utilizationIdleTime = utilizationIdleTime.plus(stateDuration);
                    }
                    break;
                case WRAP:
                    totalWrapTime = totalWrapTime.plus(stateDuration);
                    if (isLongerThanThreshold) {
                        utilizationWrapTime = utilizationWrapTime.plus(stateDuration);
                    }
                    break;
                case OFFLINE:
                    break;
                case MEETING:
                    totalMeetingTime = totalMeetingTime.plus(stateDuration);
                    if (isLongerThanThreshold) {
                        utilizationMeetingTime = utilizationMeetingTime.plus(stateDuration);
                    }
                    break;
                case BREAK:
                    totalBreakTime = totalBreakTime.plus(stateDuration);
                    if (isLongerThanThreshold) {
                        utilizationBreakTime = utilizationBreakTime.plus(stateDuration);
                    }
                    break;
            }

            if (!oldState.isReadyState() && newState.isReadyState()) {
                readyStartTime = now;
            } else if (oldState.isReadyState() && !newState.isReadyState()) {
                Duration readyDuration = DurationUtils.getDuration(readyStartTime, now);
                totalReadyTime = totalReadyTime.plus(readyDuration);
                readyStartTime = null;
            }
        }
        if (!newState.isReadyState()) {
            setDialerActive0(false, now);
        }

        this.stateTimeThreshold = newStateTimeThreshold;
        this.stateStartTime = now;
        this.state = newState;
        this.previousState = oldState;
        return true;
    }

    public boolean isDialerActive() {
        return dialerActive;
    }

    public boolean setDialerActive(boolean dialerActive) {
        return setDialerActive(dialerActive, now());
    }

    public boolean setDialerActive(boolean dialerActive, LocalDateTime now) {
        if (!hasStarted()) {
            throw new IllegalStateException("AgentStats has not been started yet");
        }
        if (hasStopped()) {
            throw new IllegalStateException("AgentStats has been stopped");
        }
        if (!this.state.isReadyState()) {
            return false;
        }
        return setDialerActive0(dialerActive, now);
    }

    private boolean setDialerActive0(boolean dialerActive, LocalDateTime now) {
        if (this.dialerActive == dialerActive) {
            return false;
        }
        this.totalDialerActiveTime = getTotalDialerActiveTime(now);
        this.dialerActive = dialerActive;
        this.dialerActiveStartTime = dialerActive ? now : null;
        return true;
    }

    public void start() {
        start(now());
    }

    public void start(LocalDateTime now) {
        if (hasStarted()) {
            return;
        }
        startTime = now;
        setState(AgentState.OFFLINE, Duration.ZERO, now);
    }

    public void stopWithRedaction() {
        stopWithRedaction(now());
    }

    public void stopWithRedaction(LocalDateTime now) {
        if (isExpired(now)) {
            stop(stateStartTime);
        } else {
            stop(now);
        }
    }

    public void stop(LocalDateTime now) {
        if (!hasStarted()) {
            throw new IllegalStateException("AgentStats has not been started yet");
        }
        if (hasStopped()) {
            return;//this has already been stopped
        }
        if (startTime.isAfter(now)) {
            throw new IllegalArgumentException("stopTime cannot be before startTime");
        }
        if (state != AgentState.OFFLINE) {
            setState(AgentState.OFFLINE, Duration.ZERO, now);
        }
        stateStartTime = null;
        endTime = now;
    }

    /**
     * @return true if the stats were either stopped or are over a day old
     */
    public boolean isExpired() {
        return isExpired(now());
    }

    public boolean isExpired(LocalDateTime now) {
        if (!hasStarted()) {
            return false;
        }
        if (hasStopped()) {
            return true;
        }
        return DurationUtils.getDuration(stateStartTime, now).isLongerThan(Duration.standardHours(8));
    }

    public Report getReport(LocalDateTime now) {
        return new Report(this, now);
    }

    public static class Report {

        private final Duration totalTime;
        private final Duration readyTime;
        private final Duration breakTime;
        private final Duration idleTime;
        private final Duration talkTime;
        private final Duration holdTime;
        private final Duration wrapTime;
        private final Duration previewTime;
        private final Duration utilizationBreakTime;
        private final Duration utilizationIdleTime;
        private final Duration utilizationWrapTime;
        private final Duration utilizationHoldTime;
        private final Duration utilizationPreviewTime;

        private Report(AgentStats stats, LocalDateTime now) {
            totalTime = stats.getTotalTime(now);
            readyTime = stats.getTotalReadyTime(now);
            breakTime = stats.getTotalBreakTime(now);
            idleTime = stats.getTotalIdleTime(now);
            talkTime = stats.getTotalOnCallTime(now);
            holdTime = stats.getTotalHoldTime(now);
            wrapTime = stats.getTotalWrapTime(now);
            previewTime = stats.getTotalPreviewTime(now);
            utilizationBreakTime = stats.getUtilizationBreakTime(now);
            utilizationIdleTime = stats.getUtilizationIdleTime(now);
            utilizationWrapTime = stats.getUtilizationWrapTime(now);
            utilizationHoldTime = stats.getUtilizationHoldTime(now);
            utilizationPreviewTime = stats.getUtilizationPreviewTime(now);
        }

        public Duration getTotalTime() {
            return totalTime;
        }

        public Duration getBreakTime() {
            return breakTime;
        }

        public Duration getReadyTime() {
            return readyTime;
        }

        public Duration getIdleTime() {
            return idleTime;
        }

        public Duration getTalkTime() {
            return talkTime;
        }

        public Duration getHoldTime() {
            return holdTime;
        }

        public Duration getWrapTime() {
            return wrapTime;
        }

        public Duration getPreviewTime() {
            return previewTime;
        }

        public Duration getUtilizationBreakTime() {
            return utilizationBreakTime;
        }

        public Duration getUtilizationIdleTime() {
            return utilizationIdleTime;
        }

        public Duration getUtilizationWrapTime() {
            return utilizationWrapTime;
        }

        public Duration getUtilizationHoldTime() {
            return utilizationHoldTime;
        }

        public Duration getUtilizationPreviewTime() {
            return utilizationPreviewTime;
        }

    }
}
