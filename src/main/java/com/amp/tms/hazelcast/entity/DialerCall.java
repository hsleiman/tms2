/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.tms.hazelcast.entity;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.amp.tms.service.dialer.LoanNumber;
import com.amp.tms.websocket.message.outbound.PhoneToType;
import java.io.IOException;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

/**
 *
 * @author Hoang, J, Bishistha
 */
@MappedSuperclass
public class DialerCall extends LoanNumber {

    @Id
    @Column(name = "call_uuid")
    private String callUUID;

    @Enumerated(EnumType.STRING)
    private State state;
    
    @Column(name = "stats_pk", insertable = false, updatable = false)
    private Long dialerPk;

    @Column(name = "queue_pk", insertable = false, updatable = false)
    private Long queuePk;

    private Long waitTimeMillis;

    private Long responseTimeMillis;

    private Long dispositionCodeId;

    @Embedded
    private PhoneToType callInfo;

    public DialerCall() {
    }

    public DialerCall(DialerCall copy) {
        copyFrom(copy);
    }

    public final void copyFrom(DialerCall copy) {
        super.copyFrom(copy);
        this.callUUID = copy.callUUID;
        this.state = copy.state;
        this.dialerPk = copy.dialerPk;
        this.queuePk = copy.getQueuePk();
        this.waitTimeMillis = copy.waitTimeMillis;
        this.responseTimeMillis = copy.responseTimeMillis;
        this.dispositionCodeId = copy.dispositionCodeId;
        this.callInfo = copy.callInfo;
    }

    @Override
    public void writeData(ObjectDataOutput out) throws IOException {
        super.writeData(out);
        out.writeUTF(callUUID);
        out.writeObject(state);
        out.writeObject(dialerPk);
        out.writeObject(queuePk);
        out.writeObject(waitTimeMillis);
        out.writeObject(responseTimeMillis);
        out.writeObject(dispositionCodeId);
        getCallInfo().writeData(out);
    }

    @Override
    public void readData(ObjectDataInput in) throws IOException {
        super.readData(in);
        callUUID = in.readUTF();
        state = in.readObject();
        dialerPk = in.readObject();
        queuePk = in.readObject();
        waitTimeMillis = in.readObject();
        responseTimeMillis = in.readObject();
        dispositionCodeId = in.readObject();
        getCallInfo().readData(in);
    }

    public String getCallUUID() {
        return callUUID;
    }

    public void setCallUUID(String callUUID) {
        this.callUUID = callUUID;
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public Long getDialerPk() {
        return dialerPk;
    }

    public void setDialerPk(Long dialerPk) {
        this.dialerPk = dialerPk;
    }

    public Long getQueuePk() {
        return queuePk;
    }

    public void setQueuePk(Long queuePk) {
        this.queuePk = queuePk;
    }

    public Long getWaitTimeMillis() {
        return waitTimeMillis;
    }

    public void setWaitTimeMillis(Long waitTimeMillis) {
        this.waitTimeMillis = waitTimeMillis;
    }

    public Long getResponseTimeMillis() {
        return responseTimeMillis;
    }

    public void setResponseTimeMillis(Long responseTimeMillis) {
        this.responseTimeMillis = responseTimeMillis;
    }

    public Long getDispositionCodeId() {
        return dispositionCodeId;
    }

    public void setDispositionCodeId(Long dispositionCodeId) {
        this.dispositionCodeId = dispositionCodeId;
    }

    public PhoneToType getCallInfo() {
        if (callInfo == null) {
            callInfo = new PhoneToType();
        }
        return callInfo;
    }

    public void setCallInfo(PhoneToType phoneToType) {
        this.callInfo = phoneToType;
    }

    @Override
    public String toString() {
        return "DialerCall{" + "callUUID=" + callUUID + ", queuePk=" + queuePk + '}';
    }

    public enum State {

        PENDING,
        READY,
        SCHEDULED,
        IN_PROGRESS,
        REJECTED,
        DROPPED,
        FAILED,
        SUCCESSFUL;

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
}
