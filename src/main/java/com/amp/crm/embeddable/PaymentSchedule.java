/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.amp.crm.embeddable;

import java.math.BigDecimal;
import javax.persistence.Embeddable;
import org.joda.time.LocalDate;

/**
 *
 * 
 */
@Embeddable
public class PaymentSchedule {
    
    private BigDecimal amountDue;
    private LocalDate dueDate;
    private BigDecimal amountReceived; 

    public BigDecimal getAmountDue() {
        return amountDue;
    }

    public void setAmountDue(BigDecimal amountDue) {
        this.amountDue = amountDue;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public BigDecimal getAmountReceived() {
        return amountReceived;
    }

    public void setAmountReceived(BigDecimal amountReceived) {
        this.amountReceived = amountReceived;
    }
    
    
}
