/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.tms.db.entity;

import com.google.gson.Gson;
import com.google.gson.annotations.Expose;
import com.amp.tms.pojo.PostChatBody;
import com.amp.tms.utility.GsonUtility;
import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import org.joda.time.LocalDateTime;

/**
 *
 * @author hsleiman
 */
@Entity
@Table(schema = "sti")
public class Chat implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long pk;

    @Expose
    private LocalDateTime createDateTime;

    @Expose
    private String uuid;

    @Expose
    private Integer loanPk;

    @Expose
    private Integer from_ext;

    @Expose
    private String to_ext;

    @Expose
    private String from_name;

    @Expose
    private Boolean read;

    @Expose
    private Boolean typing;

    @Expose
    private Boolean isBorrower;

    @Expose
    private String callBackIP;

    @Expose
    private String borrowerUsername;
    
    @Expose
    private Boolean ended;

    @Expose
    @Column(length = 1024)
    private String msg;

    public Long getPk() {
        return pk;
    }

    public void setPk(Long pk) {
        this.pk = pk;
    }

    public LocalDateTime getCreateDateTime() {
        return createDateTime;
    }

    public void setCreateDateTime(LocalDateTime createDateTime) {
        this.createDateTime = createDateTime;
    }

    public Boolean getEnded() {
        return ended;
    }

    public void setEnded(Boolean ended) {
        this.ended = ended;
    }

    public String getBorrowerUsername() {
        return borrowerUsername;
    }

    public void setBorrowerUsername(String borrowerUsername) {
        this.borrowerUsername = borrowerUsername;
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

    public Integer getFrom_ext() {
        return from_ext;
    }

    public void setFrom_ext(Integer from_ext) {
        this.from_ext = from_ext;
    }

    public String getTo_ext() {
        return to_ext;
    }

    public void setTo_ext(String to_ext) {
        this.to_ext = to_ext;
    }

    public String getFrom_name() {
        return from_name;
    }

    public void setFrom_name(String from_name) {
        this.from_name = from_name;
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

    public Boolean getIsBorrower() {
        return isBorrower;
    }

    public void setIsBorrower(Boolean isBorrower) {
        this.isBorrower = isBorrower;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getCallBackIP() {
        return callBackIP;
    }

    public void setCallBackIP(String callBackIP) {
        this.callBackIP = callBackIP;
    }

    public PostChatBody agentToBorrowerChatBuilder() {
        PostChatBody postChatBody = new PostChatBody();

        postChatBody.setCallBackIP(this.getCallBackIP());
        postChatBody.setLoanPk(this.getLoanPk());
        postChatBody.setMessage(this.getMsg());
        postChatBody.setRead(this.getRead());
        postChatBody.setTyping(this.getTyping());
        postChatBody.setUuid(this.getUuid());
        postChatBody.setFromName(this.getFrom_name());
        postChatBody.setBorrowerUsername(this.getBorrowerUsername());

        return postChatBody;
    }

    public String toJson() {
        Gson gson = GsonUtility.getGson(true);
        return gson.toJson(this);
    }

}
