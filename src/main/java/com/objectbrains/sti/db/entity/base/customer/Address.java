/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.objectbrains.sti.db.entity.base.customer;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.objectbrains.sti.common.StringUpperCaseAdapter;
import com.objectbrains.sti.constants.AddressType;
import com.objectbrains.sti.db.entity.superentity.SuperEntity;
import com.objectbrains.sti.embeddable.AddressData;
import java.util.Objects;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.hibernate.annotations.ForeignKey;

/**
 *
 * @author jaimel
 */
//@NamedQueries({
//        
//})
@Entity
@Table(schema = "sti")
@XmlAccessorType(XmlAccessType.FIELD)
public class Address extends SuperEntity {

    @Embedded
    AddressData addressData;
    
    @XmlTransient
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_pk", referencedColumnName = "pk")
    private Customer customer;

    @XmlTransient
    @JsonIgnore
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employmentdata_pk", referencedColumnName = "pk")
    @ForeignKey(name = "fk_sv_employer_address_employment_data")
    private Employment employmentAddress;

    public AddressData getAddressData() {
        return addressData;
    }

    public void setAddressData(AddressData addressData) {
        this.addressData = addressData;
    }

    
    
    public Employment getEmploymentAddress() {
        return employmentAddress;
    }

    public void setEmploymentAddress(Employment employmentAddress) {
        this.employmentAddress = employmentAddress;
    }

    
    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

}
