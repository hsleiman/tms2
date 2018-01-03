/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.objectbrains.tms.hazelcast.entity;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.DataSerializable;
import com.objectbrains.sti.constants.PreviewDialerType;
import com.objectbrains.tms.enumerated.CallDirection;
import com.objectbrains.tms.enumerated.CallState;
import com.objectbrains.tms.pojo.BorrowerInfo;
import java.io.IOException;
import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.MappedSuperclass;
import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;

/**
 *
 * @author connorpetty
 */
@Audited
@MappedSuperclass
public class AgentCall implements DataSerializable {

    @Column(name = "call_uuid")
    private String callUUID;

    @Enumerated(EnumType.STRING)
    private CallState callState;

    @Enumerated(EnumType.STRING)
    private PreviewDialerType previewType;

    @Audited(withModifiedFlag = true)
    private boolean accepted;
    @Audited(withModifiedFlag = true)
    private boolean rejected;
    @Audited(withModifiedFlag = true)
    private boolean answered;
    @Audited(withModifiedFlag = true)
    private boolean wrapped;
    @Audited(withModifiedFlag = true)
    private boolean callerHangup;
    @Audited(withModifiedFlag = true)
    private boolean agentHangup;
    @Audited(withModifiedFlag = true)
    private boolean holding;
    private Boolean terminated;
    private boolean transferring;
    private boolean transferred;
    private boolean autoDialed;

    private BorrowerInfo borrowerInfo;

    @Enumerated(EnumType.STRING)
    private CallDirection callDirection;

    @NotAudited
    private String agentFreeswitchUUID;

    private Long queuePk;

    @Column(length = 9999)
    private String badLanguage;
    private Long dispositionId;

    @NotAudited
    private Boolean queueAssigned;

    @NotAudited
    private Boolean agentReached;

    public AgentCall() {
    }

    public AgentCall(AgentCall copy) {
        copyFrom(copy);
    }

    public AgentCall(String callUUID, PreviewDialerType previewType, boolean ignoreWrap,
            BorrowerInfo borrowerInfo, CallDirection callDirection,
            Long queuePk, String agentFreeswitchUUID, boolean autoDialed) {
        this.callUUID = callUUID;
        this.previewType = previewType;
        this.accepted = previewType == null || previewType == PreviewDialerType.REGULAR;
        this.rejected = false;
        this.holding = false;
        this.borrowerInfo = borrowerInfo;
        this.callDirection = callDirection;
        this.queuePk = queuePk;
        this.answered = false;
        this.callerHangup = false;
        this.agentHangup = false;
        this.transferring = false;
        this.transferred = false;
        this.wrapped = ignoreWrap;
        this.badLanguage = null;
        this.agentFreeswitchUUID = agentFreeswitchUUID;
        this.autoDialed = autoDialed;
        this.dispositionId = null;
        this.callState = impliedCallState();
        this.queueAssigned = false;
        this.terminated = false;
        this.agentReached = false;
    }

    public final void copyFrom(AgentCall copy) {
        this.callUUID = copy.callUUID;
        this.previewType = copy.previewType;
        this.accepted = copy.accepted;
        this.rejected = copy.rejected;
        this.holding = copy.holding;
        this.callState = copy.callState;
        this.answered = copy.answered;
        this.wrapped = copy.wrapped;
        this.callerHangup = copy.callerHangup;
        this.agentHangup = copy.agentHangup;
        this.transferring = copy.transferring;
        this.transferred = copy.transferred;
        this.autoDialed = copy.autoDialed;
        this.borrowerInfo = copy.borrowerInfo;
        this.callDirection = copy.callDirection;
        this.agentFreeswitchUUID = copy.agentFreeswitchUUID;
        this.queuePk = copy.queuePk;
        this.badLanguage = copy.badLanguage;
        this.dispositionId = copy.dispositionId;
        this.queueAssigned = copy.queueAssigned;
        this.terminated = copy.terminated;
        this.agentReached = copy.agentReached;
    }

    public String getCallUUID() {
        return callUUID;
    }

    public CallState getCallState() {
        return callState;
    }

    public PreviewDialerType getPreviewType() {
        return previewType;
    }

    public boolean isAgentReached() {
        return agentReached != null && agentReached;
    }

    public boolean isAutoDialed() {
        return autoDialed;
    }

    public boolean isRejected() {
        return rejected;
    }

    public boolean isAccepted() {
        return accepted;
    }

    public boolean isAnswered() {
        return answered;
    }

    public boolean isAcceptedOrRejected() {
        return accepted | rejected;
    }

    public boolean isAgentHangup() {
        return agentHangup;
    }

    public boolean isCallerHangup() {
        return callerHangup;
    }

    public boolean isTerminated() {
        return terminated != null && terminated;
    }

    public boolean isEnded() {
        return callerHangup | agentHangup | isTerminated();
    }

    public boolean isWrapped() {
        return wrapped;
    }

    public boolean isHolding() {
        return holding;
    }

    public boolean isTransferring() {
        return transferring;
    }

    public boolean isTransferred() {
        return transferred;
    }

    public Long getQueuePk() {
        return queuePk;
    }

    public void setQueuePk(long queuePk) {
        this.queuePk = queuePk;
        this.queueAssigned = true;
    }

    public boolean wasQueueAssigned() {
        return queueAssigned;
    }

    public String getBadLanguage() {
        return badLanguage;
    }

    public Long getDispositionId() {
        return dispositionId;
    }

    public BorrowerInfo getBorrowerInfo() {
        if (borrowerInfo == null) {
            borrowerInfo = new BorrowerInfo();
        }
        return borrowerInfo;
    }

    public CallDirection getCallDirection() {
        return callDirection;
    }

    public boolean agentReached() {
        if (agentReached == false) {
            agentReached = true;
            return true;
        }
        return false;
    }

    public boolean accepted() {
        if (!isAcceptedOrRejected()) {
            accepted = true;
            callState = impliedCallState();
            return true;
        }
        return false;
    }

    public boolean rejected() {
        if (!isAcceptedOrRejected()) {
            rejected = true;
            callState = impliedCallState();
            return true;
        }
        return false;
    }

    public boolean answered() {
        if (answered == false) {
            answered = true;
            callState = impliedCallState();
            return true;
        }
        return false;
    }

    public boolean agentHungup() {
        if (!isEnded()) {
            agentHangup = true;
            callState = impliedCallState();
            return true;
        }
        return false;
    }

    public boolean callerHungup() {
        if (!isEnded()) {
            callerHangup = true;
            callState = impliedCallState();
            return true;
        }
        return false;
    }

    public boolean onHold() {
        if (holding == false && !isEnded()) {
            holding = true;
            callState = impliedCallState();
            return true;
        }
        return false;
    }

    public boolean offHold() {
        if (holding == true) {
            holding = false;
            callState = impliedCallState();
            return true;
        }
        return false;
    }

    public boolean wrapped(Long dispositionId) {
        if (wrapped == false) {
            wrapped = true;
            this.dispositionId = dispositionId;
            callState = impliedCallState();
            return true;
        }
        return false;
    }

    public boolean terminate() {
        if (!agentReached && !isEnded()) {
//            callerHangup = true;
            terminated = true;
            callState = impliedCallState();
            return true;
        }
        return false;
    }

    public boolean transferring() {
        if (transferring == false && transferred == false) {
            transferring = true;
            callState = impliedCallState();
            return true;
        }
        return false;
    }

    public boolean transferred() {
        if (transferred == false) {
            transferring = false;
            transferred = true;
            callState = impliedCallState();
            return true;
        }
        return false;
    }

    public void setBadLanguage(String badLanguage) {
        this.badLanguage = badLanguage;
    }

    public String getAgentFreeswitchUUID() {
        return agentFreeswitchUUID;
    }

    public void setAgentFreeswitchUUID(String agentFreeswitchUUID) {
        this.agentFreeswitchUUID = agentFreeswitchUUID;
    }
    
    private CallState impliedCallState() {
        if (isTerminated()) {
            return CallState.DONE;
        }
        if (transferring) {
            return CallState.TRANSFERRING;
        }
        if (rejected) {
            return CallState.DONE;
        }
        if (!accepted) {
            return CallState.PREVIEW;
        }
//        if (!answered && callerHangup) {
//            return CallState.DONE;
//        }
        if (callerHangup | agentHangup) {
            return wrapped ? CallState.DONE : CallState.WRAP;
        }
        if (!answered) {
            return CallState.RINGING;
        }
        if (holding) {
            return CallState.HOLD;
        } else {
            return CallState.ACTIVE;
        }
    }

    @Override
    public void writeData(ObjectDataOutput out) throws IOException {
        out.writeUTF(callUUID);
//        CallState.write(out, callState);
        out.writeObject(previewType);

        boolean queuePkNotNull = queuePk != null;
        boolean dispositionIdNotNull = dispositionId != null;
        short booleans = 0;
        booleans |= answered ? (1 << 0) : 0;
        booleans |= wrapped ? (1 << 1) : 0;
        booleans |= callerHangup ? (1 << 2) : 0;
        booleans |= agentHangup ? (1 << 3) : 0;
        booleans |= transferring ? (1 << 4) : 0;
        booleans |= transferred ? (1 << 5) : 0;
        booleans |= autoDialed ? (1 << 6) : 0;
        booleans |= queuePkNotNull ? (1 << 7) : 0;
        booleans |= dispositionIdNotNull ? (1 << 8) : 0;
        booleans |= accepted ? (1 << 9) : 0;
        booleans |= rejected ? (1 << 10) : 0;
        booleans |= holding ? (1 << 11) : 0;
        booleans |= queueAssigned != null && queueAssigned ? (1 << 12) : 0;
        booleans |= isTerminated() ? (1 << 13) : 0;
        booleans |= isAgentReached() ? (1 << 14) : 0;
        out.writeShort(booleans);

        if (queuePkNotNull) {
            out.writeLong(queuePk);
        }
        if (dispositionIdNotNull) {
            out.writeLong(dispositionId);
        }

        getBorrowerInfo().writeData(out);
        CallDirection.write(out, callDirection);
        out.writeUTF(badLanguage);
        out.writeUTF(agentFreeswitchUUID);
    }

    @Override
    public void readData(ObjectDataInput in) throws IOException {
        callUUID = in.readUTF();
//        callState = CallState.read(in);
        previewType = in.readObject();

        short booleans = in.readShort();
        answered = (booleans & (1 << 0)) > 0;
        wrapped = (booleans & (1 << 1)) > 0;
        callerHangup = (booleans & (1 << 2)) > 0;
        agentHangup = (booleans & (1 << 3)) > 0;
        transferring = (booleans & (1 << 4)) > 0;
        transferred = (booleans & (1 << 5)) > 0;
        autoDialed = (booleans & (1 << 6)) > 0;
        boolean queuePkNotNull = (booleans & (1 << 7)) > 0;
        boolean dispositionIdNotNull = (booleans & (1 << 8)) > 0;
        accepted = (booleans & (1 << 9)) > 0;
        rejected = (booleans & (1 << 10)) > 0;
        holding = (booleans & (1 << 11)) > 0;
        queueAssigned = (booleans & (1 << 12)) > 0;
        terminated = (booleans & (1 << 13)) > 0;
        agentReached = (booleans & (1 << 14)) > 0;

        queuePk = queuePkNotNull ? in.readLong() : null;
        dispositionId = dispositionIdNotNull ? in.readLong() : null;

        getBorrowerInfo().readData(in);
        callDirection = CallDirection.read(in);
        badLanguage = in.readUTF();
        agentFreeswitchUUID = in.readUTF();
        callState = impliedCallState();
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 97 * hash + Objects.hashCode(this.callUUID);
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
        final AgentCall other = (AgentCall) obj;
        if (!Objects.equals(this.callUUID, other.callUUID)) {
            return false;
        }
        return true;
    }

}
