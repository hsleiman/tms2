/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.crm.db.entity.log;

import com.amp.crm.db.entity.superentity.SuperEntity;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import org.joda.time.LocalDateTime;

/**
 *
 * @author hsleiman
 */
@Entity
@Table(schema = "sti")
public class DataChangeLog extends SuperEntity {

    private long pkReference;

    private String agentUsername;
    
    private long groupCode;
    
    private int type;
    private String className;
    private String attributeName;
    
    @Column(length = 3999)
    private String oldValue;
    
    @Column(length = 3999)
    private String newValue;
    
    private int idx;
    
    @Column(length=4000)
    private String valueType;
    
    private long appPk;
    
    private long transactionId;
    private long threadId;
    
    private boolean hidden;
    private long logType;
    
    private String logDesc;
    
    public String getAgentUsername() {
        return agentUsername;
    }

    public void setAgentUsername(String agentUsername) {
        this.agentUsername = agentUsername;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getAttributeName() {
        return attributeName;
    }

    public void setAttributeName(String attributeName) {
        this.attributeName = attributeName;
    }

    public String getOldValue() {
        return oldValue;
    }

    public void setOldValue(String oldValue) {
        this.oldValue = oldValue;
    }

    public String getNewValue() {
        return newValue;
    }

    public void setNewValue(String newValue) {
        this.newValue = newValue;
    }

    public long getAppPk() {
        return appPk;
    }

    public void setAppPk(long appPk) {
        this.appPk = appPk;
    }

    public long getPkReference() {
        return pkReference;
    }

    public void setPkReference(long pkReference) {
        this.pkReference = pkReference;
    }

    public long getGroupCode() {
        return groupCode;
    }

    public void setGroupCode(long groupCode) {
        this.groupCode = groupCode;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getValueType() {
        return valueType;
    }

    public void setValueType(String valueType) {
        this.valueType = valueType;
    }

    public int getIdx() {
        return idx;
    }

    public void setIdx(int idx) {
        this.idx = idx;
    }

    public boolean isHidden() {
        return hidden;
    }

    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }

    public long getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(long transactionId) {
        this.transactionId = transactionId;
    }

    public long getThreadId() {
        return threadId;
    }

    public void setThreadId(long threadId) {
        this.threadId = threadId;
    }

    public long getLogType() {
        return logType;
    }

    public void setLogType(long logType) {
        this.logType = logType;
    }

    public String getLogDesc() {
        return logDesc;
    }

    public void setLogDesc(String logDesc) {
        this.logDesc = logDesc;
    }

    @Override
    public String toString() {
        return "LosDataChangeLog{" + "pkReference=" + pkReference + ", createTimestamp=" + this.getCreatedTime() + ", agentUsername=" + agentUsername + ", groupCode=" + groupCode + ", type=" + type + ", className=" + className + ", attributeName=" + attributeName + ", oldValue=" + oldValue + ", newValue=" + newValue + ", idx=" + idx + ", valueType=" + valueType + ", appPk=" + appPk + ", transactionId=" + transactionId + ", threadId=" + threadId + ", hidden=" + hidden + ", logType=" + logType + ", logDesc=" + logDesc + '}';
    }
    
    
    
    
}
