/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.objectbrains.tms.pojo;

import com.google.gson.Gson;
import com.google.gson.annotations.Expose;
import com.objectbrains.tms.db.entity.Chat;
import com.objectbrains.tms.utility.GsonUtility;

/**
 *
 * @author farzadaziminia
 */
public class PostChatBody {

    @Expose
    private String message;

    @Expose
    private String uuid;

    @Expose
    private Integer loanPk;

    @Expose
    private String fromName;

    @Expose
    private String ext;

    @Expose
    private String callBackIP;

    @Expose
    private Boolean read;

    @Expose
    private Boolean typing;
    
    @Expose
    private Boolean ended;

    @Expose
    private String borrowerUsername;

    public String getBorrowerUsername() {
        return borrowerUsername;
    }

    public void setBorrowerUsername(String borrowerUsername) {
        this.borrowerUsername = borrowerUsername;
    }

    public Boolean getEnded() {
        return ended;
    }

    public void setEnded(Boolean ended) {
        this.ended = ended;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public Integer getLoanPk() {
        return loanPk;
    }

    public void setLoanPk(Integer loanPk) {
        this.loanPk = loanPk;
    }

    public String getFromName() {
        return fromName;
    }

    public void setFromName(String fromName) {
        this.fromName = fromName;
    }

    public String getExt() {
        return ext;
    }

    public void setExt(String ext) {
        this.ext = ext;
    }

    public String getCallBackIP() {
        return callBackIP;
    }

    public void setCallBackIP(String callBackIP) {
        this.callBackIP = callBackIP;
    }

    public Boolean getRead() {
        return read;
    }

    public void setRead(Boolean read) {
        this.read = read;
    }

    public Boolean getTyping() {
        return typing;
    }

    public void setTyping(Boolean typing) {
        this.typing = typing;
    }

    public String toJson() {
        Gson gson = GsonUtility.getGson(true);
        return gson.toJson(this);
    }

    public Chat borrowerToagentChatBuilder() {
        Chat chat = new Chat();
        chat.setMsg(this.getMessage());
        chat.setFrom_name(this.getFromName());
        chat.setUuid(this.getUuid());
        chat.setLoanPk(this.getLoanPk());
        chat.setIsBorrower(true);
        chat.setTo_ext(this.getExt() + "");
        chat.setRead(this.getRead());
        chat.setEnded(this.getEnded());
        chat.setTyping(this.getTyping());
        chat.setCallBackIP(this.getCallBackIP());
        chat.setBorrowerUsername(this.getBorrowerUsername());
        return chat;
    }

}
