/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.objectbrains.tms.hazelcast.entity;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.DataSerializable;
import com.objectbrains.tms.enumerated.DialerType;
import com.objectbrains.tms.service.dialer.Dialer;
import com.objectbrains.tms.service.dialer.predict.QueueRates;
import java.io.IOException;
import javax.persistence.Column;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import org.joda.time.LocalDateTime;

/**
 *
 * @author connorpetty
 */
@MappedSuperclass
public class DialerStats implements DataSerializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long pk;

    @Column(name = "queue_pk", updatable = false, insertable = false)
    private Long queuePk;

    @Enumerated(EnumType.STRING)
    private Dialer.State state;
    @Enumerated(EnumType.STRING)
    private DialerType type;
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    private int totalLoanCount = 0;
    private int notReadyLoanCount = 0;
    private int neverReadyLoanCount = 0;
    private int readyLoanCount = 0;
    private int inProgressLoanCount = 0;
    private int completedLoanCount = 0;

    private int successfulCallCount = 0;
    private int failedCallCount = 0;
    private int droppedCallCount = 0;
    private int rejectedCallCount = 0;
    private int pendingCallCount = 0;
    private int inProgressCallCount = 0;
    private int readyCallCount = 0;
    private int scheduledCallCount = 0;

    private int callResponseCount = 0;
    private long totalResponseTimeMillis = 0;
    private int callWaitCount = 0;
    private long totalWaitTimeMillis = 0;
//    private int callTalkCount = 0;
//    private long totalCallLengthMillis = 0;

    public DialerStats() {
    }

    public DialerStats(DialerStats copy) {
        copyFrom(copy);
    }

    public final void copyFrom(DialerStats copy) {
        this.pk = copy.pk;
        this.queuePk = copy.queuePk;
        this.state = copy.state;
        this.type = copy.type;
        this.startTime = copy.startTime;
        this.endTime = copy.endTime;

        this.totalLoanCount = copy.totalLoanCount;
        this.notReadyLoanCount = copy.notReadyLoanCount;
        this.neverReadyLoanCount = copy.neverReadyLoanCount;
        this.readyLoanCount = copy.readyLoanCount;
        this.inProgressLoanCount = copy.inProgressLoanCount;
        this.completedLoanCount = copy.completedLoanCount;

        this.successfulCallCount = copy.successfulCallCount;
        this.failedCallCount = copy.failedCallCount;
        this.droppedCallCount = copy.droppedCallCount;
        this.rejectedCallCount = copy.rejectedCallCount;
        this.pendingCallCount = copy.pendingCallCount;
        this.inProgressCallCount = copy.inProgressCallCount;
        this.readyCallCount = copy.readyCallCount;
        this.scheduledCallCount = copy.scheduledCallCount;

        this.callResponseCount = copy.callResponseCount;
        this.totalResponseTimeMillis = copy.totalResponseTimeMillis;
        this.callWaitCount = copy.callWaitCount;
        this.totalWaitTimeMillis = copy.totalWaitTimeMillis;
//        this.callTalkCount = copy.callTalkCount;
//        this.totalCallLengthMillis = copy.totalCallLengthMillis;
    }

    @Override
    public void writeData(ObjectDataOutput out) throws IOException {
        out.writeObject(pk);
        out.writeObject(queuePk);
        Dialer.State.write(out, state);
        DialerType.write(out, type);
        out.writeObject(startTime);
        out.writeObject(endTime);

        out.writeInt(totalLoanCount);
        out.writeInt(notReadyLoanCount);
        out.writeInt(neverReadyLoanCount);
        out.writeInt(readyLoanCount);
        out.writeInt(inProgressLoanCount);
        out.writeInt(completedLoanCount);

        out.writeInt(successfulCallCount);
        out.writeInt(failedCallCount);
        out.writeInt(droppedCallCount);
        out.writeInt(rejectedCallCount);
        out.writeInt(pendingCallCount);
        out.writeInt(inProgressCallCount);
        out.writeInt(readyCallCount);
        out.writeInt(scheduledCallCount);

        out.writeInt(callResponseCount);
        out.writeLong(totalResponseTimeMillis);
        out.writeInt(callWaitCount);
        out.writeLong(totalWaitTimeMillis);
//        out.writeInt(callTalkCount);
//        out.writeLong(totalCallLengthMillis);
    }

    @Override
    public void readData(ObjectDataInput in) throws IOException {
        pk = in.readObject();
        queuePk = in.readObject();
        state = Dialer.State.read(in);
        type = DialerType.read(in);
        startTime = in.readObject();
        endTime = in.readObject();

        totalLoanCount = in.readInt();
        notReadyLoanCount = in.readInt();
        neverReadyLoanCount = in.readInt();
        readyLoanCount = in.readInt();
        inProgressLoanCount = in.readInt();
        completedLoanCount = in.readInt();

        successfulCallCount = in.readInt();
        failedCallCount = in.readInt();
        droppedCallCount = in.readInt();
        rejectedCallCount = in.readInt();
        pendingCallCount = in.readInt();
        inProgressCallCount = in.readInt();
        readyCallCount = in.readInt();
        scheduledCallCount = in.readInt();

        callResponseCount = in.readInt();
        totalResponseTimeMillis = in.readLong();
        callWaitCount = in.readInt();
        totalWaitTimeMillis = in.readLong();
//        callTalkCount = in.readInt();
//        totalCallLengthMillis = in.readLong();
    }

    public Long getPk() {
        return pk;
    }

    public void setPk(Long pk) {
        this.pk = pk;
    }

    public Long getQueuePk() {
        return queuePk;
    }

    public void setQueuePk(Long queuePk) {
        this.queuePk = queuePk;
    }

    public Dialer.State getState() {
        return state;
    }

//    public void setState(Dialer.State state) {
//        this.state = state;
//    }
    public boolean hasStarted() {
        return startTime != null;
    }

    public boolean hasEnded() {
        return endTime != null;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

//    public void setStartTime(LocalDateTime startTime) {
//        this.startTime = startTime;
//    }
//    public void setEndTime(LocalDateTime endTime) {
//        this.endTime = endTime;
//    }
    public int getTotalLoanCount() {
        return totalLoanCount;
    }

    public int getNotReadyLoanCount() {
        return notReadyLoanCount;
    }

    public int getReadyLoanCount() {
        return readyLoanCount;
    }

    public int getInProgressLoanCount() {
        return inProgressLoanCount;
    }

    public int getCompletedLoanCount() {
        return completedLoanCount;
    }

    public int getSuccessfulCallCount() {
        return successfulCallCount;
    }

    public int getFailedCallCount() {
        return failedCallCount;
    }

    public int getDroppedCallCount() {
        return droppedCallCount;
    }

    public int getRejectedCallCount() {
        return rejectedCallCount;
    }

    public int getPendingCallCount() {
        return pendingCallCount;
    }

    public int getInProgressCallCount() {
        return inProgressCallCount;
    }

    public int getReadyCallCount() {
        return readyCallCount;
    }

    public int getScheduledCallCount() {
        return scheduledCallCount;
    }

    public int getCallResponseCount() {
        return callResponseCount;
    }

    public long getTotalResponseTimeMillis() {
        return totalResponseTimeMillis;
    }

    public int getCallWaitCount() {
        return callWaitCount;
    }

    public long getTotalWaitTimeMillis() {
        return totalWaitTimeMillis;
    }

//    public int getCallTalkCount() {
//        return callTalkCount;
//    }
//
//    public long getTotalCallLengthMillis() {
//        return totalCallLengthMillis;
//    }
    public void incrementCount(LocalDateTime now, DialerLoan.State state) {
        modifyCount(now, state, 1);
    }

    public void decrementCount(LocalDateTime now, DialerLoan.State state) {
        modifyCount(now, state, -1);
    }

    private void modifyCount(LocalDateTime now, DialerLoan.State state, int mod) {
        if (state == null) {
            return;
        }
        switch (state) {
            case NOT_READY:
                notReadyLoanCount += mod;
                break;
            case NEVER_READY:
                neverReadyLoanCount += mod;
                break;
            case READY:
                readyLoanCount += mod;
                break;
            case IN_PROGRESS:
                inProgressLoanCount += mod;
                break;
            case COMPLETE:
                completedLoanCount += mod;
                break;
        }
        updateState(now);
    }

    public void incrementCount(LocalDateTime now, DialerCall.State state) {
        modifyCount(now, state, 1);
    }

    public void decrementCount(LocalDateTime now, DialerCall.State state) {
        modifyCount(now, state, -1);
    }

    private void modifyCount(LocalDateTime now, DialerCall.State state, int mod) {
        if (state == null) {
            return;
        }
        switch (state) {
            case PENDING:
                pendingCallCount += mod;
                break;
            case IN_PROGRESS:
                inProgressCallCount += mod;
                break;
            case REJECTED:
                rejectedCallCount += mod;
                break;
            case DROPPED:
                droppedCallCount += mod;
                break;
            case FAILED:
                failedCallCount += mod;
                break;
            case SUCCESSFUL:
                successfulCallCount += mod;
                break;
        }
        updateState(now);
    }

    public void addRespondTime(LocalDateTime now, long respondTimeMillis) {
        callResponseCount++;
        totalResponseTimeMillis += respondTimeMillis;
        updateState(now);
    }

    public void addWaitTime(LocalDateTime now, long waitTimeMillis) {
        callWaitCount++;
        totalWaitTimeMillis += waitTimeMillis;
        updateState(now);
    }

    public void incrementScheduledCallCount(LocalDateTime now) {
        scheduledCallCount++;
        updateState(now);
    }

    public void decrementScheduledCallCount(LocalDateTime now) {
        scheduledCallCount--;
        updateState(now);
    }

    public void incrementReadyCallCount(LocalDateTime now) {
        readyCallCount++;
        updateState(now);
    }

    public void decrementReadyCallCount(LocalDateTime now) {
        readyCallCount--;
        updateState(now);
    }

    private void end(LocalDateTime now) {
        endTime = now;
    }

    public void init() {
        state = Dialer.State.INIT;
    }
    
    public void start(LocalDateTime now, int loanCount, DialerType type) {
        this.type = type;
        startTime = now;
        totalLoanCount = loanCount;
        notReadyLoanCount = loanCount;
        resume(now);
    }

    public void pause(LocalDateTime now) {
        state = Dialer.State.PAUSED;
        updateState(now);
    }

    public void resume(LocalDateTime now) {
        state = Dialer.State.RUNNING;
        updateState(now);
    }

    public void stop(LocalDateTime now) {
        if (!hasEnded()) {
            state = Dialer.State.STOPPED;
            end(now);
        }
    }

    private void updateState(LocalDateTime now) {
        state = impliedState();
        if (!hasEnded() && state == Dialer.State.COMPLETED) {
            end(now);
        }
    }

    private Dialer.State impliedState() {

        switch (state) {
            case INIT:
            case PAUSED:
            case STOPPED:
                return state;
        }

        if (completedLoanCount >= totalLoanCount) {
            return Dialer.State.COMPLETED;
        }

        if (inProgressCallCount == 0
                && readyCallCount == 0
                && readyLoanCount == 0) {
            return Dialer.State.IDLE;
        }

        return Dialer.State.RUNNING;
    }

//    public void addCallDuration(long callLengthMillis) {
//        callTalkCount++;
//        totalCallLengthMillis += callLengthMillis;
//    }
    /**
     *
     * @param queuePk queuePk
     * @param defaultACL defaultAverageCallLength in milliseconds
     * @param defaultATBCA defaultAverageTimeBetweenCallArrivals in milliseconds
     * @param defaultACDT defaultAverageCustomerDropTime in milliseconds
     * @param defaultACRT defualtAverageCustomerResponseTime in milliseconds
     * @return
     */
    public QueueRates getQueueRates(long defaultACDT, long defaultACRT) {
        double averageCustomerDropTime;
        double averageCustomerResponseTime;
        double callResponseProbability;
//        double droppedCount = stats.getDroppedCallCount();
//        double responseCount = stats.getRespondedCallCount();
//        double failedCount = stats.getFailedLoanCount();
        if (callWaitCount != 0.0) {
            averageCustomerDropTime = totalWaitTimeMillis / callWaitCount;
        } else {
            averageCustomerDropTime = defaultACDT;
        }

        if (callResponseCount != 0.0) {
            averageCustomerResponseTime = totalResponseTimeMillis / callResponseCount;
        } else {
            averageCustomerResponseTime = defaultACRT;
        }

        double totalCount = droppedCallCount + failedCallCount + successfulCallCount;
        if (totalCount != 0.0) {
            callResponseProbability = callResponseCount / totalCount;
        } else {
            callResponseProbability = 1.0;
        }
//getAllQueueAverages(defaultACL, defaultATBCA)
        return new QueueRates(averageCustomerDropTime, averageCustomerResponseTime, callResponseProbability);
    }

}
