/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.crm.pojo;

import com.amp.crm.constants.ContactTimeType;
import com.amp.crm.db.entity.base.customer.Email;
import com.amp.crm.db.entity.base.customer.Phone;
import java.util.ArrayList;
import java.util.List;
import org.joda.time.LocalDateTime;

public class CustomerContactInformationPojo {
    private LocalDateTime lastContactTimestamp;
    private Long phoneNumberUsedInLastContact;
    private List<Phone> phones = new ArrayList<>(0);
    private List<Email> emailAddresses = new ArrayList<>(0);
    private LocalDateTime callbackDateTime;
    private LocalDateTime dialerLeftMessageTime;
    private ContactTimeType contactType;
    private LocalDateTime contactTime;
    private long customerPk;

    public LocalDateTime getLastContactTimestamp() {
        return lastContactTimestamp;
    }

    public void setLastContactTimestamp(LocalDateTime lastContactTimestamp) {
        this.lastContactTimestamp = lastContactTimestamp;
    }

    public Long getPhoneNumberUsedInLastContact() {
        return phoneNumberUsedInLastContact;
    }

    public void setPhoneNumberUsedInLastContact(Long phoneNumberUsedInLastContact) {
        this.phoneNumberUsedInLastContact = phoneNumberUsedInLastContact;
    }

    public List<Phone> getPhones() {
        return phones;
    }

    public void setPhones(List<Phone> phones) {
        this.phones = phones;
    }

    public List<Email> getEmailAddresses() {
        return emailAddresses;
    }

    public void setEmailAddresses(List<Email> emailAddresses) {
        this.emailAddresses = emailAddresses;
    }

    public LocalDateTime getCallbackDateTime() {
        return callbackDateTime;
    }

    public void setCallbackDateTime(LocalDateTime callbackDateTime) {
        this.callbackDateTime = callbackDateTime;
    }

    public LocalDateTime getDialerLeftMessageTime() {
        return dialerLeftMessageTime;
    }

    public void setDialerLeftMessageTime(LocalDateTime dialerLeftMessageTime) {
        this.dialerLeftMessageTime = dialerLeftMessageTime;
    }

    public ContactTimeType getContactType() {
        return contactType;
    }

    public void setContactType(ContactTimeType contactType) {
        this.contactType = contactType;
    }

    public LocalDateTime getContactTime() {
        return contactTime;
    }

    public void setContactTime(LocalDateTime contactTime) {
        this.contactTime = contactTime;
    }

    public long getCustomerPk() {
        return customerPk;
    }

    public void setCustomerPk(long customerPk) {
        this.customerPk = customerPk;
    }
    
    
}
