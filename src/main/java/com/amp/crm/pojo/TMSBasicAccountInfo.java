/*
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.amp.crm.pojo;

import java.math.BigDecimal;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import org.joda.time.LocalDate;

@XmlAccessorType(XmlAccessType.FIELD)
public class TMSBasicAccountInfo {
    private long accountPk;
    private LocalDate nextDueDate;
    private BigDecimal principalBalance;
    private Boolean paidOff;

    public long getAccountPk() {
        return accountPk;
    }

    public void setAccountPk(long accountPk) {
        this.accountPk = accountPk;
    }
    
    public LocalDate getNextDueDate() {
        return nextDueDate;
    }

    public void setNextDueDate(LocalDate nextDueDate) {
        this.nextDueDate = nextDueDate;
    }

    public BigDecimal getPrincipalBalance() {
        return principalBalance;
    }

    public void setPrincipalBalance(BigDecimal principalBalance) {
        this.principalBalance = principalBalance;
    }

    public Boolean getPaidOff() {
        return paidOff;
    }

    public void setPaidOff(Boolean paidOff) {
        this.paidOff = paidOff;
    }
}

