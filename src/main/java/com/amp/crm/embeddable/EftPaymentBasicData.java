/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.crm.embeddable;

import java.math.BigDecimal;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Transient;
import org.apache.commons.lang3.Validate;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;

/**
 *
 * @author raine.cabal
 */
@Embeddable
public class EftPaymentBasicData implements Cloneable {

    private LocalDateTime paymentTime;
    @Column(precision = 10, scale = 2)
    private BigDecimal paymentAmount;
    @Column(precision = 10, scale = 2)
    private BigDecimal feeAmount;
    private LocalDate postingDate;
    @Transient
    private String statusDescription;
    private String username;
    private String comments;

    public LocalDateTime getPaymentTime() {
        return paymentTime;
    }

    public void setPaymentTime(LocalDateTime paymentTime) {
        this.paymentTime = paymentTime;
    }

    public BigDecimal getPaymentAmount() {
        return paymentAmount;
    }

    public void setPaymentAmount(BigDecimal paymentAmount) {
        this.paymentAmount = paymentAmount;
    }

    public BigDecimal getFeeAmount() {
        return feeAmount;
    }

    public void setFeeAmount(BigDecimal feeAmount) {
        this.feeAmount = feeAmount;
    }

    public LocalDate getPostingDate() {
        return postingDate;
    }

    public void setPostingDate(LocalDate postingDate) {
        this.postingDate = postingDate;
    }

    public String getStatusDescription() {
        return statusDescription;
    }

    public void setStatusDescription(String statusDescription) {
        this.statusDescription = statusDescription;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public void validate() {
        Validate.notNull(paymentAmount, "Payment amount is required.");
        Validate.notNull(postingDate, "Posting date is required.");
        //Validate.isTrue(!postingDate.isBefore(LocalDate.now()), "Posting date cannot be in the past.");
    }

    @Override
    protected EftPaymentBasicData clone() throws CloneNotSupportedException {
        return (EftPaymentBasicData) super.clone();
    }

    public EftPaymentBasicData copy() {
        try {
            return clone();
        } catch (CloneNotSupportedException ex) {
            throw new AssertionError("Error copying EFTPaymentBasicData: ", ex);
        }
    }

    @Override
    public String toString() {
        return "EftPaymentBasicData{" + "paymentTime=" + paymentTime + ", paymentAmount=" + paymentAmount + ", feeAmount=" + feeAmount + ", postingDate=" + postingDate + ", statusDescription=" + statusDescription + ", username=" + username + ", comments=" + comments + '}';
    }
    
    

}
