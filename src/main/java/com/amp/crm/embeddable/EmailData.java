/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.crm.embeddable;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.amp.crm.constants.EmailAddressType;
import com.amp.crm.db.entity.base.customer.Customer;
import com.amp.crm.db.entity.base.customer.Email;
import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlTransient;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 *
 * 
 */
@Embeddable
@XmlAccessorType(XmlAccessType.FIELD)
public class EmailData {
    
    @Transient
    private long customerPk;  
    @Transient
    private long emailPk;
    @Column(nullable = false)
    private String emailAddress;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EmailAddressType emailAddressType;
    private Boolean emailAddressBad;
    private Boolean doNotContact;
    private String userName;
    @XmlTransient
    @JsonIgnore
    private Boolean oldDoNotContact = false;

    public long getEmailPk() {
        return emailPk;
    }

    public void setEmailPk(long emailPk) {
        this.emailPk = emailPk;
    }

    public long getCustomerPk() {
        return customerPk;
    }

    public void setCustomerPk(long customerPk) {
        this.customerPk = customerPk;
    }

    public EmailAddressType getType() {
        return emailAddressType;
    }

    public void setType(EmailAddressType type) {
        this.emailAddressType = type;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    public Boolean isEmailAddressBad() {
        return emailAddressBad;
    }

    public void setEmailAddressBad(Boolean emailAddressBad) {
        this.emailAddressBad = emailAddressBad;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public EmailAddressType getEmailAddressType() {
        return emailAddressType;
    }

    public void setEmailAddressType(EmailAddressType emailAddressType) {
        this.emailAddressType = emailAddressType;
    }

    public Boolean isDoNotContact() {
        if(doNotContact == null){
            doNotContact = false;
        }
        return doNotContact;
    }

    public void setDoNotContact(Boolean doNotContact) {
        if(doNotContact == null){
            doNotContact = false;
        }
        setOldDoNotContact(this.doNotContact);
        this.doNotContact = doNotContact;
    }
    
    @Override
    public int hashCode() {
        int hash = 5;
        hash = 29 * hash + Objects.hashCode(this.emailAddress);
        hash = 29 * hash + Objects.hashCode(this.emailAddressType);
        hash = 29 * hash + Objects.hashCode(this.emailAddressBad);
        hash = 29 * hash + Objects.hashCode(this.doNotContact);
        return hash;
    }

//    @Override
//    public boolean equals(Object obj) {
//        if (obj == null) {
//            return false;
//        }
//        if (getClass() != obj.getClass()) {
//            return false;
//        }
//        final Email other = (Email) obj;
//        if (!Objects.equals(this.emailAddress, other.emailAddress)) {
//            return false;
//        }
//        if (this.emailAddressType != other.emailAddressType) {
//            return false;
//        }
//        if (!Objects.equals(this.emailAddressBad, other.emailAddressBad)) {
//            return false;
//        }
//        if (!Objects.equals(this.doNotContact, other.doNotContact)) {
//            return false;
//        }
//        return true;
//    }

    public String dump() {
        return ToStringBuilder.reflectionToString(this);
    }

    public Boolean isOldDoNotContact() {
        return oldDoNotContact;
    }

    public void setOldDoNotContact(Boolean oldDoNotContact) {
        this.oldDoNotContact = oldDoNotContact;
    }
}
