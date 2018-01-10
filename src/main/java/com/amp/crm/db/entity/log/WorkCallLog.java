/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.crm.db.entity.log;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.amp.crm.db.entity.base.dialer.CallDetailRecord;
import com.amp.crm.db.entity.base.dialer.CallDetailRecordAbstract;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlTransient;
import org.hibernate.annotations.ForeignKey;

/**
 *
 * 
 */

//@NamedQueries({
//    
//})


@Entity
@Table(schema = "crm")
public class WorkCallLog extends CallDetailRecordAbstract {
    
    @XmlTransient
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "call_detail_record_pk", referencedColumnName = "pk")
    @ForeignKey(name="fk_work_call_log_cdr")
    private CallDetailRecord CallDetailRecord;

    private String switchName;
    private Boolean agentHangup;
    private Boolean wrapped;
    
    public void associateCallDetailRecord(CallDetailRecord cdr){
        this.setCallDetailRecord(cdr);
        cdr.getWorkCallLogs().add(this);
    }

    public CallDetailRecord getCallDetailRecord() {
        return CallDetailRecord;
    }

    public void setCallDetailRecord(CallDetailRecord CallDetailRecord) {
        this.CallDetailRecord = CallDetailRecord;
    }

    public String getSwitchName() {
        return switchName;
    }

    public void setSwitchName(String switchName) {
        this.switchName = switchName;
    }
    
    public Boolean isAgentHangup() {
        return agentHangup;
    }

    public void setAgentHangup(Boolean agentHangup) {
        this.agentHangup = agentHangup;
    }

    public Boolean isWrapped() {
        return wrapped;
    }

    public void setWrapped(Boolean wrapped) {
        this.wrapped = wrapped;
    }
    
}

