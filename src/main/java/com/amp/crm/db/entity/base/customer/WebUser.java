/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.crm.db.entity.base.customer;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.amp.crm.db.entity.base.customer.Customer;
import com.amp.crm.db.entity.superentity.SuperEntity;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlTransient;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;


@Entity
@Table(schema = "crm")
@XmlAccessorType(XmlAccessType.FIELD)
public class WebUser extends SuperEntity{
    
    private String culture;
    private String emailAddress;
    private Boolean emailAddressBad;
    private Boolean emailAddressDoNotContact;
    private Boolean tempPassword;
    private String password;
    private LocalDate passwordExpiryDate;
    private String username;
    private Integer userType;
    
    @XmlTransient
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_pk", referencedColumnName = "pk")
    private Customer customer;

    public String getCulture() {
        return culture;
    }

    public void setCulture(String culture) {
        this.culture = culture;
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

    public Boolean isEmailAddressDoNotContact() {
        return emailAddressDoNotContact;
    }

    public void setEmailAddressDoNotContact(Boolean emailAddressDoNotContact) {
        this.emailAddressDoNotContact = emailAddressDoNotContact;
    }

    public Boolean isTempPassword() {
        return tempPassword;
    }

    public void setTempPassword(Boolean isTempPassword) {
        this.tempPassword = isTempPassword;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public LocalDate getPasswordExpiryDate() {
        return passwordExpiryDate;
    }

    public void setPasswordExpiryDate(LocalDate passwordExpiryDate) {
        this.passwordExpiryDate = passwordExpiryDate;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Integer getUserType() {
        return userType;
    }

    public void setUserType(Integer userType) {
        this.userType = userType;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }
    
}
