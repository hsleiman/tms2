
package com.amp.crm.db.entity.base.dialer;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.amp.crm.db.entity.superentity.SuperEntity;
import com.amp.crm.pojo.VoiceMailPojo;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.NamedQueries;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlTransient;
import org.hibernate.annotations.ForeignKey;
import org.joda.time.LocalDateTime;

//@NamedQueries({
// 
//})
@Entity
@Table(schema = "crm")
public class VoiceMail extends SuperEntity{
    @XmlTransient
    @JsonIgnore
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cdr_pk", referencedColumnName = "pk" , unique = true)
    @ForeignKey(name="fk_cdr_voicemail")
    private CallDetailRecord CDR;
    private LocalDateTime firstHeardTime;
    private String firstHeardByUser;
    private LocalDateTime lastHeardTime;
    private String lastHeardByUser;
    private Long accountPk;

    public CallDetailRecord getCDR() {
        return CDR;
    }

    public void setCDR(CallDetailRecord CDR) {
        this.CDR = CDR;
    }

    public LocalDateTime getFirstHeardTime() {
        return firstHeardTime;
    }

    public void setFirstHeardTime(LocalDateTime firstHeardTime) {
        this.firstHeardTime = firstHeardTime;
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

    public String getFirstHeardByUser() {
        return firstHeardByUser;
    }

    public void setFirstHeardByUser(String firstHeardByUser) {
        this.firstHeardByUser = firstHeardByUser;
    }

    public Long getAccountPk() {
        return accountPk;
    }

    public void setAccountPk(Long accountPk) {
        this.accountPk = accountPk;
    }

   

    @Override
    public String toString() {
        return "VoiceMail{" + "CDR=" + CDR + ", createTime=" + this.getCreatedTime() + ", firstHeardTime=" + firstHeardTime + ", firstHeardByUser=" + firstHeardByUser + ", lastHeardTime=" + lastHeardTime + ", lastHeardByUser=" + lastHeardByUser + ", accountPk=" + accountPk + '}';
    }

    public VoiceMailPojo toPojo() {
        VoiceMailPojo vmPojo = new VoiceMailPojo();
        CallDetailRecord CDR = getCDR();
        System.out.println("CDR for voicemail "+toString()+" is "+CDR);
        if(CDR != null){            
            vmPojo.setCustomerPhoneNumber(CDR.getBorrowerPhoneNumber());
            vmPojo.setCallRecordingUrl(CDR.getCallRecordingUrl());                
        }
        vmPojo.setAccountPk(getAccountPk());
        vmPojo.setCreateTime(this.getCreatedTime());
        vmPojo.setFirstHeardTime(getFirstHeardTime());
        vmPojo.setFirstHeardByUser(getFirstHeardByUser());
        vmPojo.setLastHeardByUser(getLastHeardByUser());
        vmPojo.setLastHeardTime(getLastHeardTime());
        vmPojo.setVoicemailPk(getPk());
        return vmPojo;
    }

}
