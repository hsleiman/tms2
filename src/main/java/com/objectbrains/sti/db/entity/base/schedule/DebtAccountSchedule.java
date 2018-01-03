/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.objectbrains.sti.db.entity.base.schedule;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.objectbrains.sti.db.entity.base.account.Account;
import com.objectbrains.sti.db.entity.superentity.SuperEntity;
import com.objectbrains.sti.embeddable.PaymentSchedule;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlTransient;
import org.hibernate.annotations.ForeignKey;

/**
 *
 * @author Skaligineedi
 */
@Entity
@Table(schema = "sti")
@XmlAccessorType(XmlAccessType.FIELD)
public class DebtAccountSchedule extends SuperEntity {

    @Embedded
    private PaymentSchedule schedule;

    @XmlTransient
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "debt_account_pk", referencedColumnName = "pk")
    @ForeignKey(name = "fk_sv_debt_account_schedule")
    private Account account;

    public PaymentSchedule getSchedule() {
        return schedule;
    }

    public void setSchedule(PaymentSchedule schedule) {
        this.schedule = schedule;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

}
