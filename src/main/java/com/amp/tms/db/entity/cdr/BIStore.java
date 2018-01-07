/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.tms.db.entity.cdr;

import com.amp.tms.websocket.message.BiMessage;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.PrePersist;
import javax.persistence.Table;
import org.joda.time.LocalDateTime;

/**
 *
 * @author Hoang, J, Bishistha
 */
@Entity
@Table(schema = "sti")
public class BIStore {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long pk;
    
    private Integer extension;
    private String call_uuid;
    private Long loanId;
    private Long agentRev;
    
    @Embedded
    private BiMessage message;
    
    @Column(updatable = false, nullable = false)
    private LocalDateTime createTimestamp;

    @PrePersist
    private void onCreate() {
        createTimestamp = LocalDateTime.now();
    }

    public long getPk() {
        return pk;
    }

    public void setPk(long pk) {
        this.pk = pk;
    }

    public Integer getExtension() {
        return extension;
    }

    public void setExtension(Integer extension) {
        this.extension = extension;
    }

    public BiMessage getMessage() {
        return message;
    }

    public void setMessage(BiMessage message) {
        this.message = message;
    }

    public LocalDateTime getCreateTimestamp() {
        return createTimestamp;
    }

    public void setCreateTimestamp(LocalDateTime createTimestamp) {
        this.createTimestamp = createTimestamp;
    }

    public String getCall_uuid() {
        return call_uuid;
    }

    public void setCall_uuid(String call_uuid) {
        this.call_uuid = call_uuid;
    }

    public Long getLoanId() {
        return loanId;
    }

    public void setLoanId(Long loanId) {
        this.loanId = loanId;
    }

    public Long getAgentRev() {
        return agentRev;
    }

    public void setAgentRev(Long agentRev) {
        this.agentRev = agentRev;
    }
    

}
