/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.tms.freeswitch.pojo;

import com.amp.crm.constants.DialerMode;
import com.amp.crm.constants.PreviewDialerType;
import com.amp.crm.db.entity.base.dialer.InboundDialerQueueSettings;
import com.amp.tms.websocket.message.outbound.PhoneToType;
import java.util.ArrayList;

/**
 *
 * @author hsleiman
 */
public class InboundDialerInfoPojo {
    
    private DialerMode dialerMode;
    private PreviewDialerType previewDialerType;
    
    private InboundDialerQueueSettings settings;
    private Long agentGroupId;
    private Long loanId;
    private String borrowerFirstName;
    private String borrowerLastName;
    private Integer agentExt;
    private ArrayList<PhoneToType> phoneToTypes;
    
    private String CallUUID;

    public String getCallUUID() {
        return CallUUID;
    }

    public void setCallUUID(String CallUUID) {
        this.CallUUID = CallUUID;
    }

    public DialerMode getDialerMode() {
        return dialerMode;
    }

    public void setDialerMode(DialerMode dialerMode) {
        this.dialerMode = dialerMode;
    }

    public PreviewDialerType getPreviewDialerType() {
        return previewDialerType;
    }

    public void setPreviewDialerType(PreviewDialerType previewDialerType) {
        this.previewDialerType = previewDialerType;
    }

    public InboundDialerQueueSettings getSettings() {
        return settings;
    }

    public void setSettings(InboundDialerQueueSettings settings) {
        this.settings = settings;
    }

    public Long getLoanId() {
        return loanId;
    }

    public void setLoanId(Long loanId) {
        this.loanId = loanId;
    }

    public String getBorrowerFirstName() {
        return borrowerFirstName;
    }

    public void setBorrowerFirstName(String borrowerFirstName) {
        this.borrowerFirstName = borrowerFirstName;
    }

    public String getBorrowerLastName() {
        return borrowerLastName;
    }

    public void setBorrowerLastName(String borrowerLastName) {
        this.borrowerLastName = borrowerLastName;
    }

    public Integer getAgentExt() {
        return agentExt;
    }

    public void setAgentExt(Integer agentExt) {
        this.agentExt = agentExt;
    }

    public ArrayList<PhoneToType> getPhoneToType() {
        return phoneToTypes;
    }

    public void setPhoneToType(ArrayList<PhoneToType> phoneToType) {
        this.phoneToTypes = phoneToType;
    }
    
    public PhoneToType getPhoneToTypeSingle(){
        return phoneToTypes.get(0);
    }
    
    public void addPhoneToTypeSingle(PhoneToType phoneToType){
        phoneToTypes = new ArrayList<>();
        phoneToTypes.add(phoneToType);
    }

    public Long getAgentGroupId() {
        return agentGroupId;
    }

    public void setAgentGroupId(Long agentGroupId) {
        this.agentGroupId = agentGroupId;
    }
    
    
}
