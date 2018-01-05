/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.objectbrains.tms.service.dialer;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.objectbrains.sti.db.entity.base.dialer.OutboundDialerQueueSettings;
import com.objectbrains.sti.db.entity.disposition.CallDispositionCode;
import com.objectbrains.sti.embeddable.OutboundDialerQueueRecord;
import com.objectbrains.sti.pojo.DialerQueueAccountDetails;
import com.objectbrains.tms.enumerated.DialerType;
import com.objectbrains.tms.exception.CallNotFoundException;
import com.objectbrains.tms.hazelcast.entity.DialerCall;
import com.objectbrains.tms.hazelcast.entity.DialerLoan;
import com.objectbrains.tms.hazelcast.entity.DialerStats;
import com.objectbrains.tms.websocket.message.outbound.PhoneToType;
import java.io.IOException;
import java.util.Map;
import java.util.Queue;
import org.joda.time.DateTime;

/**
 *
 * @author connorpetty
 */
public interface Dialer {

    public boolean isRunning();

    public DialerStats getDialerStats();

    public OutboundDialerQueueRecord getRecord();

    public boolean isLoanComplete(Long loanPk);

    public Queue<DialerQueueAccountDetails> getReadyLoans();

//    public Queue<LoanNumber> getReadyRetryCalls();
    public void addReadyCall(LoanNumber loanNumber);

    public Queue<DialerQueueAccountDetails> getNotReadyLoans();

    public Map<Long, DialerLoan> getLoans();

    public long getDialerPk();
    
    public long getQueuePk();

    public DialerType getDialerType();

    public DateTime getEndTime();

    public void start() throws DialerException;

    public void pause() throws DialerException;

    public void resume() throws DialerException;

    public void stop() throws DialerException;

    public void handleReadyLoans() throws DialerException;

    public boolean handleAgentReady(int ext) throws DialerException;

    public void callInProgress(DialerCall call, Long phoneNumber) throws DialerException;

//    public void callSucceeded(DialerCall call) throws DialerException;
    public void callEnded(DialerCall call, CallDispositionCode dispositionCode) throws DialerException;

    public void callResponded(DialerCall call, long respondTimeMillis, CallRespondedCallback callback) throws DialerException;

    public void callDropped(DialerCall call, long waitTimeMillis, CallDispositionCode dispositionCode) throws DialerException;

    public State getState();

    public enum State {

        INIT,
        RUNNING,
        IDLE,
        PAUSED,
        STOPPED,
        COMPLETED;

        public static void write(ObjectDataOutput out, State state) throws IOException {
            if (state == null) {
                out.writeByte(-1);
            } else {
                out.writeByte(state.ordinal());
            }
        }

        public static State read(ObjectDataInput in) throws IOException {
            byte ordinal = in.readByte();
            if (ordinal == -1) {
                return null;
            }
            return State.values()[ordinal];
        }
    }

    public interface CallRespondedCallback {

        public boolean connectOutboundCallToAgent(int ext, String CallUUID, OutboundDialerQueueSettings settings, Long loanId, PhoneToType phoneToTypes)
                throws CallNotFoundException;

        public void putCallOnWait(long queuePk, String callUUID, Long loanId, PhoneToType phoneToTypes);
    }
}
