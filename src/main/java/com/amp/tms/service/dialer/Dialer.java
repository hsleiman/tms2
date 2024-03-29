/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.tms.service.dialer;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.amp.crm.db.entity.base.dialer.OutboundDialerQueueSettings;
import com.amp.crm.db.entity.disposition.CallDispositionCode;
import com.amp.crm.embeddable.OutboundDialerQueueRecord;
import com.amp.crm.pojo.DialerQueueAccountDetails;
import com.amp.tms.enumerated.DialerType;
import com.amp.tms.exception.CallNotFoundException;
import com.amp.tms.hazelcast.entity.DialerCall;
import com.amp.tms.hazelcast.entity.DialerLoan;
import com.amp.tms.hazelcast.entity.DialerStats;
import com.amp.tms.websocket.message.outbound.PhoneToType;
import java.io.IOException;
import java.util.Map;
import java.util.Queue;
import org.joda.time.DateTime;

/**
 *
 * 
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
