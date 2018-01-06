/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.tms.websocket.message.outbound;

import com.google.gson.Gson;
import com.google.gson.annotations.Expose;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.DataSerializable;
import com.amp.tms.db.entity.Chat;
import com.amp.tms.hazelcast.entity.AgentStats;
import com.amp.tms.pojo.AgentStatus;
import com.amp.tms.pojo.DialerQueueDetailPojo;
import com.amp.tms.utility.GsonUtility;
import com.amp.tms.websocket.message.FreeswitchCheck;
import com.amp.tms.websocket.message.Function;
import com.amp.tms.websocket.message.Message;
import com.amp.tms.websocket.message.inbound.CallDisposition;
import java.io.IOException;
import java.io.Serializable;
import java.util.List;

/**
 *
 * @author hsleiman
 */
public class Send extends Message implements Serializable, DataSerializable {

    @Expose
    private PreviewDialerSend previewDialer;

    @Expose
    private UpdateDirectory phoneExtensionUpdate;

    @Expose
    private AgentStatus agentStatus;

    @Expose
    private CallRecent callRecent;

    @Expose
    private CallSipHeader callSipHeader;

    @Expose
    private RefreshSVC refreshSVC;

    @Expose
    private AgentStats agentStats;

    @Expose
    private String valueCheckVersion;

    @Expose
    private Integer lockedExtension;

    @Expose
    private CheckExt checkExt;

    @Expose
    private List<DialerQueueDetailPojo> dialerQueueDetailPojos;

    @Expose
    private Integer transferToExt;

    @Expose
    private List<Long> callerIds;

    @Expose
    private String originalTransferCallUUID;

    @Expose
    private String internalTransferCallUUID;

    @Expose
    private PushNotification pushNotification;

    @Expose
    private Chat chat;

    @Expose
    private FreeswitchCheck freeswitchCheck;

    @Expose
    private List<CallDisposition> callDispositions;

    @Expose
    private String logServerIPAddress;

    @Expose
    private ChangeFreeswitchIp changeFreeswitchIp;

    private Send() {
    }

    public Send(Function function) {
        super(function);
    }

    public Send(Send copy) {
        super(copy);
        this.previewDialer = copy.previewDialer;
        this.phoneExtensionUpdate = copy.phoneExtensionUpdate;
        this.agentStatus = copy.agentStatus;
        this.callRecent = copy.callRecent;
        this.callSipHeader = copy.callSipHeader;
        this.refreshSVC = copy.refreshSVC;
        this.agentStats = copy.agentStats;
        this.valueCheckVersion = copy.valueCheckVersion;
        this.lockedExtension = copy.lockedExtension;
        this.checkExt = copy.checkExt;
        this.transferToExt = copy.transferToExt;
        this.callerIds = copy.callerIds;
        this.dialerQueueDetailPojos = copy.dialerQueueDetailPojos;
        this.originalTransferCallUUID = copy.originalTransferCallUUID;
        this.internalTransferCallUUID = copy.internalTransferCallUUID;
        this.pushNotification = copy.pushNotification;
        this.chat = copy.chat;
        this.callDispositions = copy.callDispositions;
        this.freeswitchCheck = copy.freeswitchCheck;
        this.logServerIPAddress = copy.logServerIPAddress;
        this.changeFreeswitchIp = copy.changeFreeswitchIp;
    }

    @Override
    public void writeData(ObjectDataOutput out) throws IOException {
        super.writeData(out);
        out.writeObject(previewDialer);
        out.writeObject(phoneExtensionUpdate);
        out.writeObject(agentStatus);
        out.writeObject(callRecent);
        out.writeObject(callSipHeader);
        out.writeObject(refreshSVC);
        out.writeObject(agentStats);
        out.writeUTF(valueCheckVersion);
        out.writeObject(lockedExtension);
        out.writeObject(checkExt);
        out.writeObject(transferToExt);
        out.writeObject(callerIds);
        out.writeObject(dialerQueueDetailPojos);
        out.writeObject(originalTransferCallUUID);
        out.writeObject(pushNotification);
        out.writeObject(chat);
        out.writeObject(callDispositions);
        out.writeObject(internalTransferCallUUID);
        out.writeObject(freeswitchCheck);
        out.writeObject(logServerIPAddress);
        out.writeObject(changeFreeswitchIp);
    }

    @Override
    public void readData(ObjectDataInput in) throws IOException {
        super.readData(in);
        previewDialer = in.readObject();
        phoneExtensionUpdate = in.readObject();
        agentStatus = in.readObject();
        callRecent = in.readObject();
        callSipHeader = in.readObject();
        refreshSVC = in.readObject();
        agentStats = in.readObject();
        valueCheckVersion = in.readUTF();
        lockedExtension = in.readObject();
        checkExt = in.readObject();
        transferToExt = in.readObject();
        callerIds = in.readObject();
        dialerQueueDetailPojos = in.readObject();
        originalTransferCallUUID = in.readObject();
        pushNotification = in.readObject();
        chat = in.readObject();
        callDispositions = in.readObject();
        internalTransferCallUUID = in.readObject();
        freeswitchCheck = in.readObject();
        logServerIPAddress = in.readObject();
        changeFreeswitchIp = in.readObject();
    }

    public CheckExt getCheckExt() {
        return checkExt;
    }

    public void setCheckExt(CheckExt checkExt) {
        this.checkExt = checkExt;
    }

    public PreviewDialerSend getPreviewDialer() {
        return previewDialer;
    }

    public void setPreviewDialer(PreviewDialerSend previewDialer) {
        this.previewDialer = previewDialer;
    }

    public String toJson() {
        Gson gson = GsonUtility.getGson(true);
        return gson.toJson(this);
    }

    public AgentStatus getAgentStatus() {
        return agentStatus;
    }

    public void setAgentStatus(AgentStatus agentStatus) {
        this.agentStatus = agentStatus;
    }

    public CallRecent getCallRecent() {
        return callRecent;
    }

    public void setCallRecent(CallRecent callRecent) {
        this.callRecent = callRecent;
    }

    public UpdateDirectory getPhoneExtensionUpdate() {
        return phoneExtensionUpdate;
    }

    public void setPhoneExtensionUpdate(UpdateDirectory phoneExtensionUpdate) {
        this.phoneExtensionUpdate = phoneExtensionUpdate;
    }

    public CallSipHeader getCallSipHeader() {
        return callSipHeader;
    }

    public void setCallSipHeader(CallSipHeader callSipHeader) {
        this.callSipHeader = callSipHeader;
    }

    public RefreshSVC getRefreshSVC() {
        return refreshSVC;
    }

    public void setRefreshSVC(RefreshSVC refreshSVC) {
        this.refreshSVC = refreshSVC;
    }

    public AgentStats getAgentStats() {
        return agentStats;
    }

    public void setAgentStats(AgentStats agentStats) {
        this.agentStats = agentStats;
    }

    public String getValueCheckVersion() {
        return valueCheckVersion;
    }

    public void setValueCheckVersion(String valueCheckVersion) {
        this.valueCheckVersion = valueCheckVersion;
    }

    public Integer getLockedExtension() {
        return lockedExtension;
    }

    public void setLockedExtension(Integer lockedExtension) {
        this.lockedExtension = lockedExtension;
    }

    public List<DialerQueueDetailPojo> getDialerQueueDetailPojos() {
        return dialerQueueDetailPojos;
    }

    public void setDialerQueueDetailPojos(List<DialerQueueDetailPojo> dialerQueueDetailPojos) {
        this.dialerQueueDetailPojos = dialerQueueDetailPojos;
    }

    public Integer getTransferToExt() {
        return transferToExt;
    }

    public void setTransferToExt(Integer transferToExt) {
        this.transferToExt = transferToExt;
    }

    public List<Long> getCallerIds() {
        return callerIds;
    }

    public void setCallerIds(List<Long> callerIds) {
        this.callerIds = callerIds;
    }

    public String getOriginalTransferCallUUID() {
        return originalTransferCallUUID;
    }

    public void setOriginalTransferCallUUID(String originalTransferCallUUID) {
        this.originalTransferCallUUID = originalTransferCallUUID;
    }

    public String getInternalTransferCallUUID() {
        return internalTransferCallUUID;
    }

    public void setInternalTransferCallUUID(String internalTransferCallUUID) {
        this.internalTransferCallUUID = internalTransferCallUUID;
    }

    public PushNotification getPushNotification() {
        return pushNotification;
    }

    public void setPushNotification(PushNotification pushNotification) {
        this.pushNotification = pushNotification;
    }

    public Chat getChat() {
        return chat;
    }

    public void setChat(Chat chat) {
        this.chat = chat;
    }

    public List<CallDisposition> getCallDispositions() {
        return callDispositions;
    }

    public void setCallDispositions(List<CallDisposition> callDispositions) {
        this.callDispositions = callDispositions;
    }

    public FreeswitchCheck getFreeswitchCheck() {
        return freeswitchCheck;
    }

    public void setFreeswitchCheck(FreeswitchCheck freeswitchCheck) {
        this.freeswitchCheck = freeswitchCheck;
    }

    public String getLogServerIPAddress() {
        return logServerIPAddress;
    }

    public void setLogServerIPAddress(String logServerIPAddress) {
        this.logServerIPAddress = logServerIPAddress;
    }

    public ChangeFreeswitchIp getChangeFreeswitchIp() {
        return changeFreeswitchIp;
    }

    public void setChangeFreeswitchIp(ChangeFreeswitchIp changeFreeswitchIp) {
        this.changeFreeswitchIp = changeFreeswitchIp;
    }

}
