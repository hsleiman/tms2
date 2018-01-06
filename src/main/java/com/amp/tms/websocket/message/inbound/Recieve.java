/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.tms.websocket.message.inbound;

import com.google.gson.Gson;
import com.google.gson.annotations.Expose;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.amp.tms.db.entity.Chat;
import com.amp.tms.enumerated.DialerActiveStatus;
import com.amp.tms.enumerated.SetAgentState;
import com.amp.tms.utility.GsonUtility;
import com.amp.tms.websocket.message.FreeswitchCheck;
import com.amp.tms.websocket.message.Message;
import com.amp.tms.websocket.message.PlayPrompt;
import com.amp.tms.websocket.message.outbound.CheckExt;
import java.io.IOException;

/**
 *
 * @author hsleiman
 */
public class Recieve extends Message {

    @Expose
    private String ipAddress;

    @Expose
    private PreviewDialerRecieve previewDialer;

    @Expose
    private String call_uuid;

    @Expose
    private Payment payment;

    @Expose
    private PTP ptp;

    @Expose
    private Phone phone;

    @Expose
    private SpeechToText speechToText;

    @Expose
    private DialerActiveStatus dialerActiveStatus;

    @Expose
    private SetAgentState agentState;

    @Expose
    private Operator operator;

    @Expose
    private AttachLoanToCallUUID attachLoanToCallUUID;

    @Expose
    private CallDisposition callDisposition;

    @Expose
    private String valueCheckVersion;

    @Expose
    private LockNextAvailable lockNextAvailable;

    @Expose
    private CheckExt checkExt;

    @Expose
    private Chat chat;
    
    @Expose 
    private String logServerIPAddress;
    
    @Expose
    private FreeswitchCheck freeswitchCheck;

    @Expose
    private PlayPrompt playPrompt;

    @Override
    public void writeData(ObjectDataOutput out) throws IOException {
        super.writeData(out);
        out.writeObject(previewDialer);
        out.writeUTF(call_uuid);
        out.writeObject(payment);
        out.writeObject(ptp);
        out.writeObject(phone);
        out.writeObject(speechToText);
        out.writeObject(dialerActiveStatus);
        out.writeObject(agentState);
        out.writeObject(operator);
        out.writeObject(attachLoanToCallUUID);
        out.writeObject(callDisposition);
        out.writeUTF(valueCheckVersion);
        out.writeObject(lockNextAvailable);
        out.writeObject(checkExt);
        out.writeObject(ipAddress);
        out.writeObject(chat);
        out.writeObject(logServerIPAddress);
        out.writeObject(freeswitchCheck);
        out.writeObject(playPrompt);
    }

    @Override
    public void readData(ObjectDataInput in) throws IOException {
        super.readData(in);
        previewDialer = in.readObject();
        call_uuid = in.readUTF();
        payment = in.readObject();
        ptp = in.readObject();
        phone = in.readObject();
        speechToText = in.readObject();
        dialerActiveStatus = in.readObject();
        agentState = in.readObject();
        operator = in.readObject();
        attachLoanToCallUUID = in.readObject();
        callDisposition = in.readObject();
        valueCheckVersion = in.readUTF();
        lockNextAvailable = in.readObject();
        checkExt = in.readObject();
        ipAddress = in.readObject();
        chat = in.readObject();
        logServerIPAddress = in.readObject();
        freeswitchCheck = in.readObject();
        playPrompt = in.readObject();
    }

    public String getCall_uuid() {
        return call_uuid;
    }

    public void setCall_uuid(String call_uuid) {
        this.call_uuid = call_uuid;
    }

    public PreviewDialerRecieve getPreviewDialer() {
        return previewDialer;
    }

    public void setPreviewDialer(PreviewDialerRecieve previewDialer) {
        this.previewDialer = previewDialer;
    }

    public String toJson() {
        Gson gson = GsonUtility.getGson(true);
        return gson.toJson(this);
    }

    public SpeechToText getSpeechToText() {
        return speechToText;
    }

    public void setSpeechToText(SpeechToText speechToText) {
        this.speechToText = speechToText;
    }

    public Phone getPhone() {
        return phone;
    }

    public void setPhone(Phone phone) {
        this.phone = phone;
    }

    public Payment getPayment() {
        return payment;
    }

    public void setPayment(Payment payment) {
        this.payment = payment;
    }

    public PTP getPtp() {
        return ptp;
    }

    public void setPtp(PTP ptp) {
        this.ptp = ptp;
    }

    public SetAgentState getAgentState() {
        return agentState;
    }

    public void setAgentState(SetAgentState agentState) {
        this.agentState = agentState;
    }

    public DialerActiveStatus getDialerActiveStatus() {
        return dialerActiveStatus;
    }

    public void setDialerActiveStatus(DialerActiveStatus dialerActiveStatus) {
        this.dialerActiveStatus = dialerActiveStatus;
    }

    public Operator getOperator() {
        return operator;
    }

    public void setOperator(Operator operator) {
        this.operator = operator;
    }

    public AttachLoanToCallUUID getAttachLoanToCallUUID() {
        return attachLoanToCallUUID;
    }

    public void setAttachLoanToCallUUID(AttachLoanToCallUUID attachLoanToCallUUID) {
        this.attachLoanToCallUUID = attachLoanToCallUUID;
    }

    public CallDisposition getCallDisposition() {
        return callDisposition;
    }

    public void setCallDisposition(CallDisposition callDisposition) {
        this.callDisposition = callDisposition;
    }

    public String getValueCheckVersion() {
        return valueCheckVersion;
    }

    public void setValueCheckVersion(String valueCheckVersion) {
        this.valueCheckVersion = valueCheckVersion;
    }

    public LockNextAvailable getLockNextAvailable() {
        return lockNextAvailable;
    }

    public void setLockNextAvailable(LockNextAvailable lockNextAvailable) {
        this.lockNextAvailable = lockNextAvailable;
    }

    public CheckExt getCheckExt() {
        return checkExt;
    }

    public void setCheckExt(CheckExt checkExt) {
        this.checkExt = checkExt;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public Chat getChat() {
        return chat;
    }

    public void setChat(Chat chat) {
        this.chat = chat;
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

    public PlayPrompt getPlayPrompt() {
        return playPrompt;
    }

    public void setPlayPrompt(PlayPrompt playPrompt) {
        this.playPrompt = playPrompt;
    }

    @Override
    public String toString() {
        return toJson();
    }

}
