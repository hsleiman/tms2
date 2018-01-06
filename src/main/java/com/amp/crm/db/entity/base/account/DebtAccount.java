/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.crm.db.entity.base.account;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.amp.crm.db.entity.base.schedule.DebtAccountSchedule;
import com.amp.crm.db.entity.superentity.SuperEntity;
import com.amp.crm.embeddable.DebtAccountData;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlTransient;

/**
 *
 * @author jaimel
 */

//@NamedQueries({
//        
//})
@Entity
@Table(schema = "sti")
@XmlAccessorType(XmlAccessType.FIELD)
public class DebtAccount extends SuperEntity{
    
    @Embedded
    private DebtAccountData debtAccountdata;
    
    @JsonIgnore
    @XmlTransient
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_pk", referencedColumnName = "pk")
    private Account account;
    
    
    @JsonIgnore
    @XmlTransient
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "account")
    private Set<DebtAccountSchedule> debtAccountSchedule = new HashSet<>(0);

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public DebtAccountData getDebtAccountdata() {
        return debtAccountdata;
    }

    public void setDebtAccountdata(DebtAccountData debtAccountdata) {
        this.debtAccountdata = debtAccountdata;
    }

    public Set<DebtAccountSchedule> getDebtAccountSchedule() {
        return debtAccountSchedule;
    }

    public void setDebtAccountSchedule(Set<DebtAccountSchedule> debtAccountSchedule) {
        this.debtAccountSchedule = debtAccountSchedule;
    }
    
    
   
   
}
