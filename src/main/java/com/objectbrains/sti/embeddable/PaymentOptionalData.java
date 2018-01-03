package com.objectbrains.sti.embeddable;

import com.objectbrains.sti.exception.ObjectDuplicationException;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

/**
 * Data about a payment that is often not specified (or only specified
 * for certain types of payments)
 * 
 * @author chris
 */
@Embeddable
@XmlAccessorType(XmlAccessType.FIELD)
public class PaymentOptionalData implements Cloneable {
    
    @Column(name="is_settled")
    private Boolean settled;
    
    @Column(name="authorization_code", length=100)
    private String authorizationCode;
    
    @Column(name="serial_number", length=20)
    private String serialNumber;
    
    @Override
    public PaymentOptionalData clone() throws CloneNotSupportedException {
        return (PaymentOptionalData) super.clone();
    }
    
    public PaymentOptionalData duplicate() {
        try {
            return clone();
        } catch (CloneNotSupportedException ex) {
            throw new ObjectDuplicationException("Error duplicating PaymentOptionalData", ex);
        }
    }

    public Boolean isSettled() {
        return this.settled;
    }
    
    public void setSettled(Boolean settled) {
        this.settled = settled;
    }

    public String getAuthorizationCode() {
        return this.authorizationCode;
    }
    
    public void setAuthorizationCode(String authorizationCode) {
        this.authorizationCode = authorizationCode;
    }

    public String getSerialNumber() {
        return this.serialNumber;
    }
    
    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

}
