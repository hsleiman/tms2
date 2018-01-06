/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.crm.embeddable;

import com.amp.crm.constants.BankAccountType;
import javax.persistence.Embeddable;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import org.springframework.beans.BeanUtils;

/**
 *
 * @author raine.cabal
 */
@Embeddable
public class BasicBankData implements Cloneable {

    private String bankName;
    @Enumerated(EnumType.ORDINAL)
    private BankAccountType bankAccountType;
    private Boolean businessAccount;
    private String accountNumber;
    private String routingNumber;

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

    @Override
    protected BasicBankData clone() throws CloneNotSupportedException {
        return (BasicBankData) super.clone();
    }

    public BasicBankData copy() {
        try {
            return clone();
        } catch (CloneNotSupportedException ex) {
            throw new AssertionError("Error copying BasicBankData: ", ex);
        }
    }

    public BankDetails toBankDetails() {
        BankDetails bankDetails = new BankDetails();
        BeanUtils.copyProperties(this, bankDetails);
        bankDetails.setBusinessAccount(this.isBusinessAccount());
        return bankDetails;

    }

}
