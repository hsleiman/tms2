/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.crm.embeddable;

import com.amp.crm.common.StringUpperCaseAdapter;
import com.amp.crm.constants.Gender;
import javax.persistence.Embeddable;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.joda.time.LocalDate;

/**
 *
 * @author hsleiman
 */
///
@Embeddable
@XmlAccessorType(XmlAccessType.FIELD)
public class PersonalInformation {

    @Transient
    private long customerPk;
    @Transient
    private long accountPk;
    private String ssn;
    private LocalDate dateOfBirth;
    @XmlJavaTypeAdapter(StringUpperCaseAdapter.class)
    private String firstName;
    @XmlJavaTypeAdapter(StringUpperCaseAdapter.class)
    private String lastName;
    @XmlJavaTypeAdapter(StringUpperCaseAdapter.class)
    private String middleInitial;
    private String lastNameSuffix;
    @Enumerated(EnumType.STRING)
    private Gender gender;
    private LocalDate driversLicenseExpirationDate;
    private String driversLicenseNumber;
    private String driversLicenseState;
    private String language;
    private String authorizedThirdParty;
    //private Boolean agreeToEft;
    //private Boolean agreeToEftOnLoanDoc;
    private Boolean statedIncome;
    private String relationshipToBorrower;
    
    private Boolean selfEmployed;
    private Boolean inMilitary;
    
    private Boolean paymentReminder;

    public long getAccountPk() {
        return accountPk;
    }

    public void setAccountPk(long accountPk) {
        this.accountPk = accountPk;
    }
    
    public Boolean getPaymentReminder() {
        return paymentReminder;
    }

    public void setPaymentReminder(Boolean paymentReminder) {
        this.paymentReminder = paymentReminder;
    }
    
    public Boolean getSelfEmployed() {
        return selfEmployed;
    }

    public void setSelfEmployed(Boolean selfEmployed) {
        this.selfEmployed = selfEmployed;
    }

    public Boolean getInMilitary() {
        return inMilitary;
    }

    public void setInMilitary(Boolean inMilitary) {
        this.inMilitary = inMilitary;
    }

    public long getCustomerPk() {
        return customerPk;
    }

    public void setCustomerPk(long customerPk) {
        this.customerPk = customerPk;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }
    
    public String getSsn() {
        return ssn;
    }

    public void setSsn(String ssn) {
        this.ssn = ssn;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
        
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
        
    }

    public String getMiddleInitial() {
        return middleInitial;
    }

    public void setMiddleInitial(String middleInitial) {
        this.middleInitial = middleInitial;
    }

    public String getLastNameSuffix() {
        return lastNameSuffix;
    }

    public void setLastNameSuffix(String lastNameSuffix) {
        this.lastNameSuffix = lastNameSuffix;
    }

    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public LocalDate getDriversLicenseExpirationDate() {
        return driversLicenseExpirationDate;
    }

    public void setDriversLicenseExpirationDate(LocalDate driversLicenseExpirationDate) {
        this.driversLicenseExpirationDate = driversLicenseExpirationDate;
    }

    public String getDriversLicenseNumber() {
        return driversLicenseNumber;
    }

    public void setDriversLicenseNumber(String driversLicenseNumber) {
        this.driversLicenseNumber = driversLicenseNumber;
    }

    public String getDriversLicenseState() {
        return driversLicenseState;
    }

    public void setDriversLicenseState(String driversLicenseState) {
        this.driversLicenseState = driversLicenseState;
    }

    public String getAuthorizedThirdParty() {
        return authorizedThirdParty;
    }

    public void setAuthorizedThirdParty(String authorizedThridParty) {
        this.authorizedThirdParty = authorizedThridParty;
    }

    public Boolean getStatedIncome() {
        return statedIncome;
    }

    public void setStatedIncome(Boolean statedIncome) {
        this.statedIncome = statedIncome;
    }

    public String getRelationshipToBorrower() {
        return relationshipToBorrower;
    }

    public void setRelationshipToBorrower(String relationshipToBorrower) {
        this.relationshipToBorrower = relationshipToBorrower;
    }
    
    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
    
}
