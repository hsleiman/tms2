
package com.amp.crm.pojo;

import org.joda.time.LocalDateTime;

public class VoiceMailPojo {
    private LocalDateTime createTime;
    private LocalDateTime firstHeardTime;
    private String firstHeardByUser;
    private LocalDateTime lastHeardTime;
    private String lastHeardByUser;
    private String callRecordingUrl;
    private Long accountPk;
    private String customerPhoneNumber;
    private long voicemailPk;
    private Long portfolioType;
    private String portfolioDesc;
    private String queueName;

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public LocalDateTime getFirstHeardTime() {
        return firstHeardTime;
    }

    public void setFirstHeardTime(LocalDateTime firstHeardTime) {
        this.firstHeardTime = firstHeardTime;
    }

    public String getFirstHeardByUser() {
        return firstHeardByUser;
    }

    public void setFirstHeardByUser(String firstHeardByUser) {
        this.firstHeardByUser = firstHeardByUser;
    }

    public LocalDateTime getLastHeardTime() {
        return lastHeardTime;
    }

    public void setLastHeardTime(LocalDateTime lastHeardTime) {
        this.lastHeardTime = lastHeardTime;
    }

    public String getLastHeardByUser() {
        return lastHeardByUser;
    }

    public void setLastHeardByUser(String lastHeardByUser) {
        this.lastHeardByUser = lastHeardByUser;
    }

    public String getCallRecordingUrl() {
        return callRecordingUrl;
    }

    public void setCallRecordingUrl(String callRecordingUrl) {
        this.callRecordingUrl = callRecordingUrl;
    }

    public Long getAccountPk() {
        return accountPk;
    }

    public void setAccountPk(Long accountPk) {
        this.accountPk = accountPk;
    }

    public String getCustomerPhoneNumber() {
        return customerPhoneNumber;
    }

    public void setCustomerPhoneNumber(String customerPhoneNumber) {
        this.customerPhoneNumber = customerPhoneNumber;
    }

    public long getVoicemailPk() {
        return voicemailPk;
    }

    public void setVoicemailPk(long voicemailPk) {
        this.voicemailPk = voicemailPk;
    }

    public Long getPortfolioType() {
        return portfolioType;
    }

    public void setPortfolioType(Long portfolioType) {
        this.portfolioType = portfolioType;
    }

    public String getPortfolioDesc() {
        return portfolioDesc;
    }

    public void setPortfolioDesc(String portfolioDesc) {
        this.portfolioDesc = portfolioDesc;
    }

    public String getQueueName() {
        return queueName;
    }

    public void setQueueName(String queueName) {
        this.queueName = queueName;
    }
    
    
}
