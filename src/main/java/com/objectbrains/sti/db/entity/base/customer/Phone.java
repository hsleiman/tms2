/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.objectbrains.sti.db.entity.base.customer;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.objectbrains.sti.db.entity.superentity.SuperEntity;
import com.objectbrains.sti.embeddable.PhoneData;
import java.util.Objects;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlTransient;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.annotations.ForeignKey;
import org.hibernate.envers.AuditTable;
import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;

/**
 *
 * @author sundeeptaachanta
 */
@NamedQueries({
    @NamedQuery(
            name = "Phone.getAllPhoneByCustomerPk",
            query = "SELECT p FROM Phone p WHERE p.customer.pk = :customerPk ORDER BY p.createdTime DESC "
    ),
    @NamedQuery(
            name = "Phone.LocateByPk",
            query = "SELECT s FROM Phone s WHERE s.pk = :pk"
    ),
    @NamedQuery(
            name = "Phone.LocateByPhoneNumber",
            query = "SELECT s FROM Phone s WHERE s.phoneData.phoneNumber = :phoneNumber and s.phoneData.areaCode = :areaCode"
    ),
    @NamedQuery(
            name = "Phone.LocateByPhoneType",
            query = "SELECT s FROM Phone s WHERE s.phoneData.phoneNumberType = :phoneType and s.customer = :customer"
    ),
    @NamedQuery(
            name = "Phone.LocateByPhoneTypes",
            query = "SELECT s FROM Phone s WHERE s.phoneData.phoneNumberType in :phoneType and s.customer = :customerPhone"
    ),
    @NamedQuery(
            name = "Phone.LocateByEmploymentDataPk",
            query = "SELECT s FROM Phone s WHERE s.employmentPhone.pk = :employmentDataPk"
    ),
    @NamedQuery(
            name = "Phone.LocateByPhoneNumberAndAccount",
            query = "SELECT s FROM Phone s WHERE s.phoneData.accountPk = :accountPk and s.phoneData.phoneNumber = :phoneNumber and s.phoneData.areaCode = :areaCode"
    ),
    @NamedQuery(
            name = "Phone.LocateByAccountPk",
            query = "SELECT s FROM Phone s WHERE s.phoneData.accountPk = :accountPk"
    )
})
@Entity
@Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
@AuditTable(value = "phone_history", schema = "sti")
@Table(name = "phone", schema = "sti")
@XmlAccessorType(XmlAccessType.FIELD)
public class Phone extends SuperEntity {

    @Embedded
    private PhoneData phoneData;

    @XmlTransient
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_phone_pk", referencedColumnName = "pk")
    @ForeignKey(name = "fk_customer_phone")
    private Customer customer;

    @XmlTransient
    @JsonIgnore
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employmentdata_pk", referencedColumnName = "pk")
    @ForeignKey(name = "fk_sv_employer_address_employment_data")
    private Employment employmentPhone;

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public Employment getSvEmploymentData() {
        return employmentPhone;
    }

    public void setSvEmploymentData(Employment employmentPhone) {
        this.employmentPhone = employmentPhone;
    }

    public Employment getEmploymentPhone() {
        return employmentPhone;
    }

    public void setEmploymentPhone(Employment employmentPhone) {
        this.employmentPhone = employmentPhone;
    }

    public PhoneData getPhoneData() {
        return phoneData;
    }

    public void setPhoneData(PhoneData phoneData) {
        this.phoneData = phoneData;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 17 * hash + Objects.hashCode(this.getPhoneData().getAreaCode());
        hash = 17 * hash + Objects.hashCode(this.getPhoneData().getPhoneNumber());
        hash = 17 * hash + Objects.hashCode(this.getPhoneData().getPhoneExtension());
        hash = 17 * hash + Objects.hashCode(this.getPhoneData().getDoNotCall());
        hash = 17 * hash + Objects.hashCode(this.getPhoneData().getAccountPk());
        hash = 17 * hash + Objects.hashCode(this.getPhoneData().getTimeZone());
        hash = 17 * hash + Objects.hashCode(this.getPhoneData().getLastCallDisposition());
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Phone other = (Phone) obj;
        if (!Objects.equals(this.getPhoneData().getAreaCode(), other.getPhoneData().getAreaCode())) {
            return false;
        }
        if (!Objects.equals(this.getPhoneData().getNotes(), other.getPhoneData().getNotes())) {
            return false;
        }
        if (!Objects.equals(this.getPhoneData().getPhoneNumber(), other.getPhoneData().getPhoneNumber())) {
            return false;
        }
        if (!Objects.equals(this.getPhoneData().getDoNotCall(), other.getPhoneData().getDoNotCall())) {
            return false;
        }
        if (!Objects.equals(this.getPhoneData().getPhoneExtension(), other.getPhoneData().getPhoneExtension())) {
            return false;
        }
        if (!Objects.equals(this.getPhoneData().getAccountPk(), other.getPhoneData().getAccountPk())) {
            return false;
        }
        if (!Objects.equals(this.getPhoneData().getTimeZone(), other.getPhoneData().getTimeZone())) {
            return false;
        }
        if (!Objects.equals(this.getPhoneData().getPhoneNumberType(), other.getPhoneData().getPhoneNumberType())) {
            return false;
        }
        if (!Objects.equals(this.getPhoneData().getPhoneLabel(), other.getPhoneData().getPhoneLabel())) {
            return false;
        }
        if (!Objects.equals(this.getPhoneData().getPhoneSource(), other.getPhoneData().getPhoneSource())) {
            return false;
        }
        if (!Objects.equals(this.getPhoneData().getSpecialInstr(), other.getPhoneData().getSpecialInstr())) {
            return false;
        }
        if (!Objects.equals(this.getPhoneData().getLastCallDisposition(), other.getPhoneData().getLastCallDisposition())) {
            return false;
        }
        return true;
    }

//    @Override
//    public String toString() {
//        StringBuilder builder = new StringBuilder();
//        builder.append("Phone Details: [");
//        builder.append("pk: [").append(pk);
//        builder.append("] areaCode: [").append(areaCode);
//        builder.append("] phoneNumber: [").append(phoneNumber);
//        builder.append("] phoneExtension: [").append(phoneExtension);
//        builder.append("] timeZone: [").append(timeZone);
//        builder.append("] doNotCall: [").append(doNotCall);
//        builder.append("] oldDoNotCall: [").append(oldDoNotCall);
//        builder.append("] loanPk: [").append(accountPk);
//        builder.append("] PhoneType: [").append(phoneNumberType);
//        builder.append("] PhoneLabel: [").append(phoneLabel);
//        builder.append("] PhoneSource: [").append(phoneSource);
//        builder.append("] PhoneSpecialInstr: [").append(phoneSpecialInstr);
//
//        builder.append("]]");
//        return builder.toString();
//    }
    public long getFullPhoneNumber() {
        if (this.getPhoneData().getAreaCode() > 0 && this.getPhoneData().getPhoneNumber() > 0) {
            String fullPhoneNum = (((Long) this.getPhoneData().getAreaCode()).toString()).concat(((Long) this.getPhoneData().getPhoneNumber()).toString());
            if (!StringUtils.isBlank(fullPhoneNum)) {
                return Long.valueOf(fullPhoneNum);
            }
        }
        return 0;
    }

}
