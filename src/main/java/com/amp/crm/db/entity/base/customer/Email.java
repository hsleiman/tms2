/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.crm.db.entity.base.customer;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.amp.crm.db.entity.superentity.SuperEntity;
import com.amp.crm.embeddable.EmailData;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlTransient;

/**
 *
 * @author jaimel
 */
@NamedQueries({
    @NamedQuery(
            name = "Email.LocateAllByCustomerPk",
            query = "SELECT s.emailData FROM Email s WHERE s.customer.pk = :customerPk"
    ),
    @NamedQuery(
            name = "Email.LocateByEmailAddress",
            query = "SELECT s FROM Email s WHERE lower(s.emailData.emailAddress) = lower(:emailAddress)"
    ),
    @NamedQuery(
            name = "Email.LocateByEmailAddresses",
            query = "SELECT s FROM Email s WHERE lower(s.emailData.emailAddress) IN (:emailAddresses)"
    ),
    @NamedQuery(
            name = "Email.LocateByType",
            query = "SELECT s FROM Email s WHERE customer = :customer and s.emailData.emailAddressType = :type"
    ),
    @NamedQuery(
            name = "Email.LocateExistingCustomerEmailAddress",
            query = "SELECT s FROM Email s WHERE s.customer.pk = :customerPk and lower(s.emailData.emailAddress) = lower(:emailAddress) "
    ),
    @NamedQuery(
            name = "Email.LocateByCustomerPkAndEmailAddress",
            query = "SELECT s FROM Email s WHERE s.customer.pk = :customerPk and lower(s.emailData.emailAddress) like lower(:emailAddress)"
    )
})
@Entity
@Table(schema = "crm")
@XmlAccessorType(XmlAccessType.FIELD)
public class Email extends SuperEntity {
    
    @Embedded
    private EmailData emailData;
    
    @XmlTransient
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_pk", referencedColumnName = "pk")
    private Customer customer;

    public EmailData getEmailData() {
        return emailData;
    }

    public void setEmailData(EmailData emailData) {
        this.emailData = emailData;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

}
