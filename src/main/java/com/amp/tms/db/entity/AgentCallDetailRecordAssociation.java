/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.tms.db.entity;

import com.amp.tms.db.entity.cdr.CallDetailRecordTMS;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import org.joda.time.LocalDateTime;

/**
 *
 * 
 */

@Entity
@Table(schema = "crm")
public class AgentCallDetailRecordAssociation {

    @Id
    private long pk;

    @ManyToOne
    @JoinColumn(name = "agent_extension", referencedColumnName = "extension")
    private AgentRecord agent;

    @ManyToOne
    @JoinColumn(name = "callDetailRecord_pk", referencedColumnName = "pk")
    private CallDetailRecordTMS callDetailRecord;

    private LocalDateTime startTimestamp;
    private LocalDateTime endTimestamp;
    
    public long getPk() {
        return pk;
    }

    public void setPk(long pk) {
        this.pk = pk;
    }

    public AgentRecord getAgent() {
        return agent;
    }

    public void setAgent(AgentRecord agent) {
        this.agent = agent;
    }

    public CallDetailRecordTMS getCallDetailRecord() {
        return callDetailRecord;
    }

    public void setCallDetailRecord(CallDetailRecordTMS callDetailRecord) {
        this.callDetailRecord = callDetailRecord;
    }

    public LocalDateTime getStartTimestamp() {
        return startTimestamp;
    }

    public void setStartTimestamp(LocalDateTime startTimestamp) {
        this.startTimestamp = startTimestamp;
    }

    public LocalDateTime getEndTimestamp() {
        return endTimestamp;
    }

    public void setEndTimestamp(LocalDateTime endTimestamp) {
        this.endTimestamp = endTimestamp;
    }


}
