/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.crm.db.entity.base.customer;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.amp.crm.db.entity.base.customer.Customer;
import com.amp.crm.db.entity.superentity.SuperEntity;
import com.amp.crm.embeddable.EmploymentData;
import com.amp.crm.embeddable.PhoneData;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlTransient;
import org.hibernate.annotations.ForeignKey;

/**
 *
 * @author sundeeptaachanta
 */
@NamedQueries({
    @NamedQuery(
            name = "Employment.LocateByPk",
            query = "SELECT s FROM Employment s WHERE s.pk = :pk"
    ),
    @NamedQuery(
            name = "Employment.GetLatestEmploymentDataForCustomer",
            query = "SELECT s FROM Employment s WHERE s.customer.pk = :customerPk order by s.employmentData.creationTimestamp desc"
    ),
    @NamedQuery(
            name = "Employment.GetLatestPrimaryEmploymentDataForCustomer",
            query = "SELECT s FROM Employment s WHERE s.customer.pk = :customerPk AND s.employmentData.employerType='PRIMARY' ORDER BY s.employmentData.creationTimestamp DESC"
    ),
    @NamedQuery(
            name = "Employment.GetLatestSecondaryEmploymentDataForCustomer",
            query = "SELECT s FROM Employment s WHERE s.customer.pk = :customerPk AND s.employmentData.employerType='NON_PRIMARY' ORDER BY s.employmentData.creationTimestamp DESC"
    ),
    @NamedQuery(
            name = "Employment.GetLatestCurrentSecondaryEmploymentDataForCustomer",
            query = "SELECT s FROM Employment s WHERE s.customer.pk = :customerPk AND s.employmentData.employerType='NON_PRIMARY' AND s.employmentData.isCurrent=true ORDER BY s.employmentData.creationTimestamp DESC"
    )
})
@Entity
@Table(name = "employment_data", schema = "sti")
public class Employment extends SuperEntity{
    
    @Embedded
    private EmploymentData employmentData;
    
    @Embedded
    private PhoneData employerPhoneData;
    
    private Boolean hasNameChanged;
    
    @XmlTransient
    @JsonIgnore
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="customer_pk", referencedColumnName="pk")
    @ForeignKey(name="fk_employment_data_customer")
    private Customer customer;
    
    @XmlTransient
    @JsonIgnore
    @OneToOne(fetch = FetchType.LAZY, mappedBy = "employmentAddress")
    private Address employerAddress;
    
    @XmlTransient
    @JsonIgnore
    @OneToOne(fetch = FetchType.LAZY, mappedBy = "employmentPhone")
    private Phone employerPhone;
    

    public EmploymentData getEmploymentData() {
        return employmentData;
    }

    public void setEmploymentData(EmploymentData employmentData) {
        this.employmentData = employmentData;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public Address getEmployerAddress() {
        return employerAddress;
    }

    public void setEmployerAddress(Address employerAddress) {
        this.employerAddress = employerAddress;
    }

    public PhoneData getEmployerPhoneData() {
        return employerPhoneData;
    }

    public void setEmployerPhoneData(PhoneData employerPhoneData) {
        this.employerPhoneData = employerPhoneData;
    }

    public Phone getEmployerPhone() {
        return employerPhone;
    }

    public void setEmployerPhone(Phone employerPhone) {
        this.employerPhone = employerPhone;
    }
    
    

    public Boolean isHasNameChanged() {
        return hasNameChanged;
    }

    public void setHasNameChanged(Boolean hasNameChanged) {
        this.hasNameChanged = hasNameChanged;
    }

    @Override
    public String toString() {
        return " EmploymentData "+this.getEmploymentData()+
                "\n EmploymentPhoneData "+this.getEmployerPhoneData();
    }
}
