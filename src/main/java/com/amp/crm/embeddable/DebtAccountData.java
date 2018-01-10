/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.amp.crm.embeddable;

import java.math.BigDecimal;
import javax.persistence.Embeddable;

/**
 *
 * 
 */
@Embeddable
public class DebtAccountData {
    
    private String debtAccountName;
    
    private BigDecimal originalDebtAmount;
    
    private BigDecimal currentDebtAmount;
    
    private BigDecimal settlementDebtAmount;
    
    private Integer numberOfPayments;
    
    private BigDecimal paymentAmount;
    
    private BigDecimal amountReceived;
    
    private BigDecimal amountRemaining;
    
    private String customerName;
    
    private Boolean enrolled = Boolean.FALSE;
    
    
    public String getDebtAccountName() {
        return debtAccountName;
    }

    public void setDebtAccountName(String debtAccountName) {
        this.debtAccountName = debtAccountName;
    }

    public BigDecimal getOriginalDebtAmount() {
        return originalDebtAmount;
    }

    public void setOriginalDebtAmount(BigDecimal originalDebtAmount) {
        this.originalDebtAmount = originalDebtAmount;
    }

    public BigDecimal getCurrentDebtAmount() {
        return currentDebtAmount;
    }

    public void setCurrentDebtAmount(BigDecimal currentDebtAmount) {
        this.currentDebtAmount = currentDebtAmount;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public BigDecimal getSettlementDebtAmount() {
        return settlementDebtAmount;
    }

    public void setSettlementDebtAmount(BigDecimal settlementDebtAmount) {
        this.settlementDebtAmount = settlementDebtAmount;
    }

    public Boolean isEnrolled() {
        return enrolled;
    }

    public void setEnrolled(Boolean enrolled) {
        this.enrolled = enrolled;
    }

    public Integer getNumberOfPayments() {
        return numberOfPayments;
    }

    public void setNumberOfPayments(Integer numberOfPayments) {
        this.numberOfPayments = numberOfPayments;
    }

    public BigDecimal getPaymentAmount() {
        return paymentAmount;
    }

    public void setPaymentAmount(BigDecimal paymentAmount) {
        this.paymentAmount = paymentAmount;
    }

    public BigDecimal getAmountReceived() {
        return amountReceived;
    }

    public void setAmountReceived(BigDecimal amountReceived) {
        this.amountReceived = amountReceived;
    }

    public BigDecimal getAmountRemaining() {
        return amountRemaining;
    }

    public void setAmountRemaining(BigDecimal amountRemaining) {
        this.amountRemaining = amountRemaining;
    }
    
    
    
    
    
}
