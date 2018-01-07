package com.amp.crm.embeddable;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import com.amp.crm.constants.BankAccountType;
import com.amp.crm.service.utility.StringUtils;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Transient;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.joda.time.LocalDate;

/**
 *
 * @author HS
 */
@Embeddable
public class BankDetails {

    @Transient
    private long bankDataPk;
    @Transient
    private long borrowerPk;
    private String bankName;
    @Enumerated(EnumType.ORDINAL)
    private BankAccountType bankAccountType;
    private Boolean businessAccount;
    private String accountNumber;
    private String routingNumber;
    private LocalDate accountOpeningDate;
    private String beneficiary;
    private String phoneNumber;
    private String intermediaryBankName;
    private String intermediaryRoutingNumber;
    @Column(length = 4000)
    private String specialWiringInstructions;

  
    public long getBankDataPk() {
        return bankDataPk;
    }

    public void setBankDataPk(long bankDataPk) {
        this.bankDataPk = bankDataPk;
    }

    public long getBorrowerPk() {
        return borrowerPk;
    }

    public void setBorrowerPk(long borrowerPk) {
        this.borrowerPk = borrowerPk;
    }

    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

    public BankAccountType getBankAccountType() {
        return bankAccountType;
    }

    public void setBankAccountType(BankAccountType bankAccountType) {
        this.bankAccountType = bankAccountType;
    }

    public Boolean isBusinessAccount() {
        return businessAccount;
    }

    public void setBusinessAccount(Boolean businessAccount) {
        this.businessAccount = businessAccount;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getRoutingNumber() {
        return routingNumber;
    }

    public void setRoutingNumber(String routingNumber) {
        this.routingNumber = routingNumber;
    }

    public LocalDate getAccountOpeningDate() {
        return accountOpeningDate;
    }

    public void setAccountOpeningDate(LocalDate accountOpeningDate) {
        this.accountOpeningDate = accountOpeningDate;
    }

    public String getBeneficiary() {
        return beneficiary;
    }

    public void setBeneficiary(String beneficiary) {
        this.beneficiary = beneficiary;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getIntermediaryBankName() {
        return intermediaryBankName;
    }

    public void setIntermediaryBankName(String intermediaryBankName) {
        this.intermediaryBankName = intermediaryBankName;
    }

    public String getIntermediaryRoutingNumber() {
        return intermediaryRoutingNumber;
    }

    public void setIntermediaryRoutingNumber(String intermediaryRoutingNumber) {
        this.intermediaryRoutingNumber = intermediaryRoutingNumber;
    }

    public String getSpecialWiringInstructions() {
        return specialWiringInstructions;
    }

    public void setSpecialWiringInstructions(String specialWiringInstructions) {
        this.specialWiringInstructions = StringUtils.getFirstNCharacters(specialWiringInstructions, 4000);
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

}
