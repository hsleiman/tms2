/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.amp.crm.pojo;

import com.amp.crm.constants.CallerId;
import com.amp.crm.constants.PopupDisplayMode;
//import com.objectbrains.sti.db.entity.loan.SvCollectionQueue;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 *
 * 
 */
public class TMSCallDetails {
     
    private Long accountPk;
    private String firstName;
    private String lastName;
    private String ssn;
    private String addr1;
    private String addr2;
    private String city;
    private String state;
    private String zip;
    @XmlElement(required = true)
    private Boolean doNotCall;
    private CallerId callerId;
    @XmlElement(required = true)
//    private Boolean isPendingBK;
//    @XmlElement(required = true)
//    private Boolean isChargedOff;
//    private Integer loanStatus;
//    private Integer loanServicingStatus;
    private Long dialerQueuePk;
    @XmlElement(required = true)    
    private Boolean autoAnswerEnabled;
    private PopupDisplayMode popupDisplayMode;
    @XmlElement(required = true)
    private Boolean disableSecondaryAgentsCallRouting;
    @XmlTransient
//    private CollectionQueue collectionQueue;
    private String queueName;
    private Long portfolioType;
//    private Integer achAutoPaymentStatus;
    
    private Integer defaultExtension;
    private String primaryAgentUsername;
    private List<String> secondaryAgentUsernameList = new ArrayList<>();
    
    private boolean hasMultipleMatches;
    
    public TMSCallDetails() {
    }

    public TMSCallDetails(Long loanPk, String firstName, String lastName, String ssn, String addr1, String addr2, String city, String state, String zip, 
            Boolean doNotCall, CallerId callerId, Boolean isPendingBK, Boolean isChargedOff, Integer loanStatus, Integer loanServicingStatus, Long dialerQueuePk, 
            Boolean autoAnswerEnabled, PopupDisplayMode popupDisplayMode, Boolean disableSecondaryAgentsCallRouting, 
            String queueName, Long portfolioType, Integer achAutoPaymentStatus) {
        this.accountPk = loanPk;
        this.firstName = firstName;
        this.lastName = lastName;
        this.ssn = ssn;
        this.addr1 = addr1;
        this.addr2 = addr2;
        this.city = city;
        this.state = state;
        this.zip = zip;
        this.doNotCall = doNotCall;
        this.callerId = callerId;
//        this.isPendingBK = isPendingBK;
//        this.isChargedOff = isChargedOff;
//        this.loanStatus = loanStatus;
//        this.loanServicingStatus = loanServicingStatus;
        this.dialerQueuePk = dialerQueuePk;
        this.autoAnswerEnabled = autoAnswerEnabled;
        this.popupDisplayMode = popupDisplayMode;
        this.disableSecondaryAgentsCallRouting = disableSecondaryAgentsCallRouting;
        //this.svCollectionQueue = svCollectionQueue;
        this.queueName = queueName;
        this.portfolioType = portfolioType;
//        this.achAutoPaymentStatus = achAutoPaymentStatus;
    }

    public Long getLoanPk() {
        return accountPk;
    }

    public void setLoanPk(Long loanPk) {
        this.accountPk = loanPk;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getSsn() {
        return ssn;
    }

    public void setSsn(String ssn) {
        this.ssn = ssn;
    }

    public String getAddr1() {
        return addr1;
    }

    public void setAddr1(String addr1) {
        this.addr1 = addr1;
    }

    public String getAddr2() {
        return addr2;
    }

    public void setAddr2(String addr2) {
        this.addr2 = addr2;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }
        
    public String getZip() {
        return zip;
    }

    public void setZip(String zip) {
        this.zip = zip;
    }

    public Boolean isDoNotCall() {
        return doNotCall;
    }

    public void setDoNotCall(Boolean doNotCall) {
        this.doNotCall = doNotCall;
    }

    public CallerId getCallerId() {
        return callerId;
    }

    public void setCallerId(CallerId callerId) {
        this.callerId = callerId;
    }

//    public Boolean isIsPendingBK() {
//        return isPendingBK;
//    }
//
//    public void setIsPendingBK(Boolean isPendingBK) {
//        this.isPendingBK = isPendingBK;
//    }
//
//    public Boolean isIsChargedOff() {
//        return isChargedOff;
//    }
//
//    public void setIsChargedOff(Boolean isChargedOff) {
//        this.isChargedOff = isChargedOff;
//    }
//
//    public Integer getLoanStatus() {
//        return loanStatus;
//    }
//
//    public void setLoanStatus(Integer loanStatus) {
//        this.loanStatus = loanStatus;
//    }
//
//    public Integer getLoanServicingStatus() {
//        return loanServicingStatus;
//    }
//
//    public void setLoanServicingStatus(Integer loanServicingStatus) {
//        this.loanServicingStatus = loanServicingStatus;
//    }

    public Long getDialerQueuePk() {
        return dialerQueuePk;
    }

    public void setDialerQueuePk(Long dialerQueuePk) {
        this.dialerQueuePk = dialerQueuePk;
    }

    public Boolean isAutoAnswerEnabled() {
        return autoAnswerEnabled;
    }

    public void setAutoAnswerEnabled(Boolean autoAnswerEnabled) {
        this.autoAnswerEnabled = autoAnswerEnabled;
    }

    public PopupDisplayMode getPopupDisplayMode() {
        return popupDisplayMode;
    }

    public void setPopupDisplayMode(PopupDisplayMode popupDisplayMode) {
        this.popupDisplayMode = popupDisplayMode;
    }

    public Boolean getDisableSecondaryAgentsCallRouting() {
        return disableSecondaryAgentsCallRouting;
    }

    public void setDisableSecondaryAgentsCallRouting(Boolean disableSecondaryAgentsCallRouting) {
        this.disableSecondaryAgentsCallRouting = disableSecondaryAgentsCallRouting;
    }

//    public SvCollectionQueue getSvCollectionQueue() {
//        return svCollectionQueue;
//    }
//
//    public void setSvCollectionQueue(SvCollectionQueue svCollectionQueue) {
//        this.svCollectionQueue = svCollectionQueue;
//    }

    public String getQueueName() {
        return queueName;
    }

    public void setQueueName(String queueName) {
        this.queueName = queueName;
    }

    public Long getPortfolioType() {
        return portfolioType;
    }

    public void setPortfolioType(Long portfolioType) {
        this.portfolioType = portfolioType;
    }

    public Integer getDefaultExtension() {
        return defaultExtension;
    }

    public void setDefaultExtension(Integer defaultExtension) {
        this.defaultExtension = defaultExtension;
    }

    public String getPrimaryAgentUsername() {
        return primaryAgentUsername;
    }

    public void setPrimaryAgentUsername(String primaryAgentUsername) {
        this.primaryAgentUsername = primaryAgentUsername;
    }

    public List<String> getSecondaryAgentUsernameList() {
        return secondaryAgentUsernameList;
    }

    public void setSecondaryAgentUsernameList(List<String> secondaryAgentUsernameList) {
        this.secondaryAgentUsernameList = secondaryAgentUsernameList;
    }

//    public Integer getAchAutoPaymentStatus() {
//        return achAutoPaymentStatus;
//    }
//
//    public void setAchAutoPaymentStatus(Integer achAutoPaymentStatus) {
//        this.achAutoPaymentStatus = achAutoPaymentStatus;
//    }

    public boolean isHasMultipleMatches() {
        return hasMultipleMatches;
    }

    public void setHasMultipleMatches(boolean hasMultipleMatches) {
        this.hasMultipleMatches = hasMultipleMatches;
    }
    
    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 53 * hash + Objects.hashCode(this.accountPk);
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
        final TMSCallDetails other = (TMSCallDetails) obj;
        if (!Objects.equals(this.accountPk, other.accountPk)) {
            return false;
        }
        return true;
    }
    
}
