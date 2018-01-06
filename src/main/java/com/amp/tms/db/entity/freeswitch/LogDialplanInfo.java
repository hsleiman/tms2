/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.tms.db.entity.freeswitch;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.PrePersist;
import javax.persistence.Table;
import org.joda.time.LocalDateTime;

/**
 *
 * @author hsleiman
 */
@Entity
@Table(schema = "sti")
public class LogDialplanInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long pk;
    
    @Column(length = 10000)
    private String content;
    @Column(length = 10000)
    private String className;
@Column(length = 10000)
    private String methodName;
    @Column(length = 4096)
    private String lineNumber;
    private Long threadId;
    private String threadName;
    private String serverName;
    private String callUUID;
    private String serverIp;
    
    private String lastClassName;
    private String lastMethodName;
    private Integer lastLineNumber;
    
    

    private LocalDateTime createDateTime;

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

    @PrePersist
    public void createTime() {
        setCreateDateTime(LocalDateTime.now());
    }

    public String getLineNumber() {
        return lineNumber;
    }

    public void setLineNumber(String lineNumber) {
        this.lineNumber = lineNumber;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public Long getThreadId() {
        return threadId;
    }

    public void setThreadId(Long threadId) {
        this.threadId = threadId;
    }

    public String getThreadName() {
        return threadName;
    }

    public void setThreadName(String threadName) {
        this.threadName = threadName;
    }

    public String getServerName() {
        return serverName;
    }

    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    public String getCallUUID() {
        return callUUID;
    }

    public void setCallUUID(String callUUID) {
        this.callUUID = callUUID;
    }

    public String getServerIp() {
        return serverIp;
    }

    public void setServerIp(String serverIp) {
        this.serverIp = serverIp;
    }

    public String getLastClassName() {
        return lastClassName;
    }

    public void setLastClassName(String lastClassName) {
        this.lastClassName = lastClassName;
    }

    public String getLastMethodName() {
        return lastMethodName;
    }

    public void setLastMethodName(String lastMethodName) {
        this.lastMethodName = lastMethodName;
    }

    public Integer getLastLineNumber() {
        return lastLineNumber;
    }

    public void setLastLineNumber(Integer lastLineNumber) {
        this.lastLineNumber = lastLineNumber;
    }
    
    

}
