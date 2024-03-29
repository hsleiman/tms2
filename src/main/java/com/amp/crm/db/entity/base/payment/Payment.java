package com.amp.crm.db.entity.base.payment;
// Generated Jun 13, 2014 8:06:43 AM by Hibernate Tools 3.6.0

import com.amp.crm.db.entity.base.payment.ach.AchPayment;
import com.amp.crm.db.entity.base.account.Account;
import com.amp.crm.db.entity.superentity.SuperEntity;
import com.amp.crm.embeddable.PaymentBasicData;
import com.amp.crm.embeddable.PaymentOptionalData;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import org.hibernate.annotations.ForeignKey;
import org.joda.time.LocalDate;

/**
 * Payment generated by hbm2java
 */
@NamedQueries({
        @NamedQuery(
            name = "Payment.LocateByAccount",
            query = "SELECT s FROM Payment s WHERE s.account = :account ORDER BY s.paymentBasicData.paymentEffectiveDate, s.pk"
        ),
        @NamedQuery(
            name = "Payment.LocateByPk",
            query = "SELECT s FROM Payment s WHERE s.pk = :paymentPk"
        )
})
@Entity
@Table(name="payment", schema="crm")
public class Payment extends SuperEntity implements Comparable {
    
    @Embedded
    private PaymentBasicData paymentBasicData;
    
    
    @Embedded
    private PaymentOptionalData paymentOptionalData;
     
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="account_pk", referencedColumnName="pk")
    @ForeignKey(name="fk_crm_payment_account")
    private Account account;
     
    @OneToOne(fetch=FetchType.LAZY, mappedBy="payment")
    private AchPayment achPayment;
    
    private String poolCompanyId;
    
    public Payment() {
    }

    //=========================================================================
    // Getters and Setters
    
    public Payment(long pk) {
        this.pk = pk;
    }

    public PaymentBasicData getPaymentBasicData() {
        if (paymentBasicData == null) setPaymentBasicData(new PaymentBasicData());
        return paymentBasicData;
    }

    public void setPaymentBasicData(PaymentBasicData paymentBasicData) {
        this.paymentBasicData = paymentBasicData;
    }


    public PaymentOptionalData getPaymentOptionalData() {
        if (paymentOptionalData == null) setPaymentOptionalData(new PaymentOptionalData());
        return paymentOptionalData;
    }

    public void setPaymentOptionalData(PaymentOptionalData paymentOptionalData) {
        this.paymentOptionalData = paymentOptionalData;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public AchPayment getAchPayment() {
        return achPayment;
    }

    public void setAchPayment(AchPayment achPayment) {
        this.achPayment = achPayment;
    }

    public String getPoolCompanyId() {
        return poolCompanyId;
    }

    public void setPoolCompanyId(String poolCompanyId) {
        this.poolCompanyId = poolCompanyId;
    }
    
    

   
   
    
    @Override
    public String toString() {
        return "SvPayment[pk=" + getPk() + ", $" + getPaymentBasicData().getAmount() + 
                " on " + getPaymentBasicData().getPaymentEffectiveDate() + "]";
    }
    
    @Override
    public int compareTo(Object o) {
        if (o == null) return -1; // Nulls sort to end
        if (! (o instanceof Payment)) throw new RuntimeException("Invalid comparison target for SvPayment: " + o);
        
        Payment other = (Payment) o;
        
        PaymentBasicData pbd1 = getPaymentBasicData();
        LocalDate d1 = (pbd1 == null) ? null : pbd1.getPaymentEffectiveDate();
        
        PaymentBasicData pbd2 = other.getPaymentBasicData();
        LocalDate d2 = (pbd2 == null) ? null : pbd2.getPaymentEffectiveDate();
        
        if (d1 == null) return (d2 == null) ? 0 : 1;
        
        int ct = d1.compareTo(d2);
        if (ct != 0) return ct;
        
        return Long.compare(getPk(), other.getPk());
    }
}


