/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.crm.db.entity.base.customer;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.amp.crm.db.entity.base.account.Account;
import com.amp.crm.db.entity.base.account.DebtAccount;
import com.amp.crm.db.entity.superentity.SuperEntity;
import com.amp.crm.embeddable.PersonalInformation;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlTransient;

/**
 *
 * 
 */
//@NamedQueries({
//        
//})
@Entity
@Table(schema = "crm")
public class Customer extends SuperEntity{

    @XmlTransient
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_pk", referencedColumnName = "pk")
    private Account account;
    
    @XmlTransient
    @JsonIgnore
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "customer")
    private Set<Email> emails = new HashSet<>(0);
    
    @XmlTransient
    @JsonIgnore
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "customer")
    private Set<WebUser> webUsers = new HashSet<>(0);
    
    @XmlTransient
    @JsonIgnore
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "customer")
    private Set<Address> address = new HashSet<>(0);
    
    @XmlTransient
    @JsonIgnore
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "customer")
    private Set<Phone> phones = new HashSet<>(0);
    
    
    @Embedded
    private PersonalInformation personalInfo;
    

    public PersonalInformation getPersonalInfo() {
        if (personalInfo == null) personalInfo = new PersonalInformation();
        return personalInfo;
    }

    public void setPersonalInfo(PersonalInformation personalInfo) {
        this.personalInfo = personalInfo;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public Set<Email> getEmails() {
        return emails;
    }

    public void setEmails(Set<Email> emails) {
        this.emails = emails;
    }

    public Set<WebUser> getWebUsers() {
        return webUsers;
    }

    public void setWebUsers(Set<WebUser> webUsers) {
        this.webUsers = webUsers;
    }

    public Set<Address> getAddress() {
        return address;
    }

    public void setAddress(Set<Address> address) {
        this.address = address;
    }

    public Set<Phone> getPhones() {
        return phones;
    }

    public void setPhones(Set<Phone> phones) {
        this.phones = phones;
    }
    
    
}
