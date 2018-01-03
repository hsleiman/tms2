/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.objectbrains.tms.freeswitch.pojo;

import com.google.gson.Gson;
import com.google.gson.annotations.Expose;
import com.objectbrains.sti.constants.DialerMode;
import com.objectbrains.sti.constants.PreviewDialerType;
import com.objectbrains.sti.db.entity.base.dialer.OutboundDialerQueueSettings;
import com.objectbrains.tms.utility.GsonUtility;
import com.objectbrains.tms.websocket.message.outbound.PhoneToType;
import java.util.ArrayList;

/**
 *
 * @author hsleiman
 */
public class DialerInfoPojo {
    @Expose
    private DialerMode dialerMode;
    @Expose
    private PreviewDialerType previewDialerType;

    private OutboundDialerQueueSettings settings;
    @Expose
    private Long loanId;
    @Expose
    private String borrowerFirstName;
    @Expose
    private String borrowerLastName;
    @Expose
    private Integer agentExt;
    @Expose
    private ArrayList<PhoneToType> phoneToTypes;
    
    @Expose
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

    public OutboundDialerQueueSettings getSettings() {
        return settings;
    }

    public void setSettings(OutboundDialerQueueSettings settings) {
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
    
    public String toJson() {
        Gson gson = GsonUtility.getGson(true);
        return gson.toJson(this);
    }
    
    
}
