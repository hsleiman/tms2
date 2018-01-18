/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.crm.pojo;

import com.amp.crm.constants.CallDirection;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.xml.bind.annotation.XmlElement;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.joda.time.LocalDateTime;

public class WorkCallLogPojo  implements Comparable<WorkCallLogPojo>{
    private Long accountPk;
    
    private String UUID;
    
    private LocalDateTime firstCallTime;
    
    private String callRecordURL;
    
    private CallDirection callDirection;
    
    @XmlElement(required = true)
    private Boolean badLanguage = Boolean.FALSE;
    
    @XmlElement(required = true)
    private Boolean badBehavior = Boolean.FALSE;
    
    private String badLanguageText;
    
    private List<CallLogLeg> LegList = new ArrayList<>();

    private String callDisposition;
    
    private String userDisposition;
    
    private boolean dialer = false;
    
    private Long fullCallDuration;
    
    private int countOfQMForms;
    
    private Boolean isSpeechToTextRequested;
    
    private Boolean isSpeechToTextCompleted;
    
    private Boolean keywordDetected;
    
    private String keyword;
    
    private Long keywordPriority;
    
    private Long badLanguagePriority;
    
    private String agentUserName;

    public String getAgentUserName() {
        return agentUserName;
    }

    public void setAgentUserName(String agentUserName) {
        this.agentUserName = agentUserName;
    }
    
    public Long getAccountPk() {
        return accountPk;
    }

    public void setAccountPk(Long accountPk) {
        this.accountPk = accountPk;
    }

    public List<CallLogLeg> getLegList() {
        return LegList;
    }

    public void setLegList(List<CallLogLeg> LegList) {
        this.LegList = LegList;
    }

    public String getUUID() {
        return UUID;
    }

    public void setUUID(String UUID) {
        this.UUID = UUID;
    }

    public LocalDateTime getFirstCallTime() {
        return firstCallTime;
    }

    public void setFirstCallTime(LocalDateTime firstCallTime) {
        this.firstCallTime = firstCallTime;
    }

    public String getCallRecordURL() {
        return callRecordURL;
    }

    public void setCallRecordURL(String callRecordURL) {
        this.callRecordURL = callRecordURL;
    }

    public CallDirection getCallDirection() {
        return callDirection;
    }

    public void setCallDirection(CallDirection callDirection) {
        this.callDirection = callDirection;
    }

    public Boolean isBadLanguage() {
        return badLanguage;
    }

    public void setBadLanguage(Boolean badLanguage) {
        this.badLanguage = badLanguage;
    }

    public Boolean isBadBehavior() {
        return badBehavior;
    }

    public void setBadBehavior(Boolean badBehavior) {
        this.badBehavior = badBehavior;
    }

    public String getBadLanguageText() {
        return badLanguageText;
    }

    public void setBadLanguageText(String badLanguageText) {
        this.badLanguageText = badLanguageText;
    }

    public String getCallDisposition() {
        return callDisposition;
    }

    public void setCallDisposition(String callDisposition) {
        this.callDisposition = callDisposition;
    }

    public String getUserDisposition() {
        return userDisposition;
    }

    public void setUserDisposition(String userDisposition) {
        this.userDisposition = userDisposition;
    }

    public boolean isDialer() {
        return dialer;
    }

    public void setDialer(boolean dialer) {
        this.dialer = dialer;
    }

    public Long getFullCallDuration() {
        return fullCallDuration;
    }

    public void setFullCallDuration(Long fullCallDuration) {
        this.fullCallDuration = fullCallDuration;
    }

    public int getCountOfQMForms() {
        return countOfQMForms;
    }

    public void setCountOfQMForms(int countOfQMForms) {
        this.countOfQMForms = countOfQMForms;
    } 

    public Boolean isIsSpeechToTextRequested() {
        return isSpeechToTextRequested;
    }

    public void setIsSpeechToTextRequested(Boolean isSpeechToTextRequested) {
        this.isSpeechToTextRequested = isSpeechToTextRequested;
    }

    public Boolean isIsSpeechToTextCompleted() {
        return isSpeechToTextCompleted;
    }

    public void setIsSpeechToTextCompleted(Boolean isSpeechToTextCompleted) {
        this.isSpeechToTextCompleted = isSpeechToTextCompleted;
    }
    
    public Boolean isKeywordDetected(){
        return keywordDetected;
    }
    
    public void setKeywordDetected(Boolean keywordDetected){
        this.keywordDetected = keywordDetected;
    }
    
    public String getKeyword(){
        return keyword;
    }
    
    public void setKeyword(String keyword){
        this.keyword = keyword;
    }
    
//    public String getBadLanguage(){
//        return keyword;
//    }
//    
//    public void setBadLanguage(String keyword){
//        this.keyword = keyword;
//    }
    
    public Long getKeywordPriority(){
        return keywordPriority;
    } 
    
    public void setKeywordPriority(Long keywordPriority){
        this.keywordPriority = keywordPriority;
    }
    
    public Long getBadLanguagePriority(){
        return badLanguagePriority;
    } 
    
    public void setBadLanguagePriority(Long badLanguagePriority){
        this.badLanguagePriority = badLanguagePriority;
    } 
    
    @Override
    public int compareTo(WorkCallLogPojo otherCallLogPojo) {
        LocalDateTime otherCallTime = otherCallLogPojo.getFirstCallTime();
        if(this.firstCallTime == null && otherCallTime == null){
            return 0;
        }
        if(this.firstCallTime == null){
            return -1;
        }
        if(otherCallTime == null){
            return 1;
        }
        return this.firstCallTime.compareTo(otherCallTime);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + Objects.hashCode(this.accountPk);
        hash = 97 * hash + Objects.hashCode(this.UUID);
        hash = 97 * hash + Objects.hashCode(this.firstCallTime);
        hash = 97 * hash + Objects.hashCode(this.callRecordURL);
        hash = 97 * hash + Objects.hashCode(this.callDirection);
        hash = 97 * hash + Objects.hashCode(this.badLanguage);
        hash = 97 * hash + Objects.hashCode(this.badBehavior);
        hash = 97 * hash + Objects.hashCode(this.badLanguageText);
        hash = 97 * hash + Objects.hashCode(this.LegList);
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
        final WorkCallLogPojo other = (WorkCallLogPojo) obj;
        if (!Objects.equals(this.accountPk, other.accountPk)) {
            return false;
        }
        if (!Objects.equals(this.UUID, other.UUID)) {
            return false;
        }
        if (!Objects.equals(this.firstCallTime, other.firstCallTime)) {
            return false;
        }
        if (!Objects.equals(this.callRecordURL, other.callRecordURL)) {
            return false;
        }
        if (!Objects.equals(this.callDirection, other.callDirection)) {
            return false;
        }
        if (!Objects.equals(this.badLanguage, other.badLanguage)) {
            return false;
        }
        if (!Objects.equals(this.badBehavior, other.badBehavior)) {
            return false;
        }
        if (!Objects.equals(this.badLanguageText, other.badLanguageText)) {
            return false;
        }
        if (!Objects.equals(this.LegList, other.LegList)) {
            return false;
        }
        return true;
    }
    
    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
    
    
}
