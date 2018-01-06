/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.tms.hazelcast.entity;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.DataSerializable;
import com.amp.tms.websocket.message.outbound.PhoneToType;
import java.io.IOException;
import java.util.Objects;
import javax.persistence.Embeddable;

/**
 *
 * @author connorpetty
 */
@Embeddable
public class WaitingCall implements DataSerializable {

    private String callUUID;
    private Long loanPk;
    private PhoneToType phoneToType;
    
    private Integer orginalAgentForTransfer;

    protected WaitingCall() {
    }
    
    public WaitingCall(String callUUID, Long loanPk, PhoneToType phoneToType, Integer orginalAgentForTransfer) {
        this.callUUID = callUUID;
        this.loanPk = loanPk;
        this.phoneToType = phoneToType;
        this.orginalAgentForTransfer = orginalAgentForTransfer;
    }

    @Override
    public String toString() {
        return "WaitingCall{" + "callUUID=" + callUUID + ", loanPk=" + loanPk + ", phoneToType=" + phoneToType + '}';
    }

    public final void copyFrom(WaitingCall call){
        this.callUUID = call.callUUID;
        this.loanPk = call.loanPk;
        this.phoneToType = call.phoneToType;
        this.orginalAgentForTransfer = call.orginalAgentForTransfer;
    }
    
    public String getCallUUID() {
        return callUUID;
    }

    public Long getLoanPk() {
        return loanPk;
    }

    public PhoneToType getPhoneToType() {
        return phoneToType;
    }

    public Integer getOrginalAgentForTransfer() {
        return orginalAgentForTransfer;
    }

    public void setOrginalAgentForTransfer(Integer orginalAgentForTransfer) {
        this.orginalAgentForTransfer = orginalAgentForTransfer;
    }
    
    

    @Override
    public void writeData(ObjectDataOutput out) throws IOException {
        out.writeUTF(callUUID);
        out.writeObject(loanPk);
        out.writeObject(phoneToType);
        out.writeObject(orginalAgentForTransfer);
    }

    @Override
    public void readData(ObjectDataInput in) throws IOException {
        callUUID = in.readUTF();
        loanPk = in.readObject();
        phoneToType = in.readObject();
        orginalAgentForTransfer = in.readObject();
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 59 * hash + Objects.hashCode(this.callUUID);
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
        final WaitingCall other = (WaitingCall) obj;
        if (!Objects.equals(this.callUUID, other.callUUID)) {
            return false;
        }
        return true;
    }
    

}
