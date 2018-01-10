package com.amp.crm.embeddable;

import com.amp.crm.exception.ObjectDuplicationException;
import java.math.BigDecimal;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;

/**
 * Basic payment information
 * 
 * 
 */
@Embeddable
@XmlAccessorType(XmlAccessType.FIELD)
public class PaymentBasicData implements Cloneable {

    @Column(name="payment_type", nullable=false)
    private int paymentType;
    
    @Column(name="amount", precision=10, scale=2)
    private BigDecimal amount;
    
    @Column(name="payment_time")
    private LocalDateTime paymentTime;
    
    @Column(name="payment_effective_date")
    private LocalDate paymentEffectiveDate;
     
    
    @Override
    public PaymentBasicData clone() throws CloneNotSupportedException {
        return (PaymentBasicData) super.clone();
    }
    
    public PaymentBasicData duplicate() {
        try {
            return clone();
        } catch (CloneNotSupportedException ex) {
            throw new ObjectDuplicationException("Error duplicating PaymentBasicData", ex);
        }
    }
    
    public int getPaymentType() {
        return this.paymentType;
    }
    
    public void setPaymentType(int paymentType) {
        this.paymentType = paymentType;
    }
    
    public BigDecimal getAmount() {
        return this.amount;
    }
    
    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public LocalDateTime getPaymentTime() {
        return this.paymentTime;
    }
    
    public void setPaymentTime(LocalDateTime paymentTime) {
        this.paymentTime = paymentTime;
    }

    public LocalDate getPaymentEffectiveDate() {
        return this.paymentEffectiveDate;
    }
    
    public void setPaymentEffectiveDate(LocalDate paymentEffectiveDate) {
        this.paymentEffectiveDate = paymentEffectiveDate;
    }
    
}
