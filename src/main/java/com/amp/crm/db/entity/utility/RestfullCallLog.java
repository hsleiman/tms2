/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.crm.db.entity.utility;

import com.amp.crm.db.entity.superentity.SuperEntity;
import javax.persistence.Entity;
import javax.persistence.Table;
import org.joda.time.LocalDateTime;

/**
 *
 * 
 */
@Entity
@Table(schema = "crm")
public class RestfullCallLog extends SuperEntity{
    
    private String url;
    private String username;
    private Long elapseTime;
    private LocalDateTime createTimestamp;
    private String remoteIpAddress;
    private String httpMethod;
    private Integer httpReponseCode;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Long getElapseTime() {
        return elapseTime;
    }

    public void setElapseTime(Long elapseTime) {
        this.elapseTime = elapseTime;
    }

    public LocalDateTime getCreateTimestamp() {
        return createTimestamp;
    }

    public void setCreateTimestamp(LocalDateTime createTimestamp) {
        this.createTimestamp = createTimestamp;
    }

    public String getRemoteIpAddress() {
        return remoteIpAddress;
    }

    public void setRemoteIpAddress(String remoteIpAddress) {
        this.remoteIpAddress = remoteIpAddress;
    }

    public String getHttpMethod() {
        return httpMethod;
    }

    public void setHttpMethod(String httpMethod) {
        this.httpMethod = httpMethod;
    }

    public Integer getHttpReponseCode() {
        return httpReponseCode;
    }

    public void setHttpReponseCode(Integer httpReponseCode) {
        this.httpReponseCode = httpReponseCode;
    }
    
    
    
}
