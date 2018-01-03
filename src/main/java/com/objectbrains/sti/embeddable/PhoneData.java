/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.objectbrains.sti.embeddable;

import com.objectbrains.sti.constants.CallDisposition;
import com.objectbrains.sti.constants.PhoneNumberType;
import com.objectbrains.sti.constants.PhoneSpecialInstructions;
import com.objectbrains.sti.constants.ZumigoLineType;
import com.objectbrains.sti.pojo.CustomerCallablePojo;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

/**
 *
 * @author sundeeptaachanta
 */
@Embeddable
@XmlAccessorType(XmlAccessType.FIELD)
public class PhoneData {

    @Transient
    private long customerPk;
    private long accountPk;
    @Transient
    private long phonePk;
    private Long areaCode;
    private Long phoneNumber;
    @Enumerated(EnumType.STRING)
    private PhoneNumberType phoneNumberType;
    @Column(length = 10)
    private String phoneExtension;
    private String phoneStatus;
    private String phoneLabel;
    private Boolean phoneDoNotUse;
    private String phoneSource;
    @Enumerated(EnumType.STRING)
    private PhoneSpecialInstructions specialInstr;
    @Column(length = 500)
    private String notes;
    private Boolean doNotCall = false;
    private String timeZone;
    @Enumerated(EnumType.STRING)
    private CallDisposition lastCallDisposition;

    @Transient
    private CustomerCallablePojo customerCallable = new CustomerCallablePojo();

    @Enumerated(EnumType.STRING)
    private ZumigoLineType verifiedLineType = ZumigoLineType.UNKNOWN;

    private String carrier;
    private Boolean locationSupported;
    private Boolean identitySupported;

    public Long getAreaCode() {
        return areaCode;
    }

    public void setAreaCode(Long areaCode) {
        this.areaCode = areaCode;
    }

    public Long getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(Long phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getPhoneExtension() {
        return phoneExtension;
    }

    public void setPhoneExtension(String phoneExtension) {
        this.phoneExtension = phoneExtension;
    }

    public String getPhoneStatus() {
        return phoneStatus;
    }

    public void setPhoneStatus(String phoneStatus) {
        this.phoneStatus = phoneStatus;
    }

    public String getPhoneLabel() {
        return phoneLabel;
    }

    public void setPhoneLabel(String phoneLabel) {
        this.phoneLabel = phoneLabel;
    }

    public Boolean isPhoneDoNotUse() {
        return phoneDoNotUse;
    }

    public void setPhoneDoNotUse(Boolean phoneDoNotUse) {
        this.phoneDoNotUse = phoneDoNotUse;
    }

    public String getPhoneSource() {
        return phoneSource;
    }

    public void setPhoneSource(String phoneSource) {
        this.phoneSource = phoneSource;
    }

    public PhoneSpecialInstructions getSpecialInstr() {
        return specialInstr;
    }

    public void setSpecialInstr(PhoneSpecialInstructions specialInstr) {
        this.specialInstr = specialInstr;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public long getCustomerPk() {
        return customerPk;
    }

    public void setCustomerPk(long customerPk) {
        this.customerPk = customerPk;
    }

    public long getAccountPk() {
        return accountPk;
    }

    public void setAccountPk(long accountPk) {
        this.accountPk = accountPk;
    }

    public PhoneNumberType getPhoneNumberType() {
        return phoneNumberType;
    }

    public void setPhoneNumberType(PhoneNumberType phoneNumberType) {
        this.phoneNumberType = phoneNumberType;
    }

    public Boolean getDoNotCall() {
        return doNotCall;
    }

    public void setDoNotCall(Boolean doNotCall) {
        this.doNotCall = doNotCall;
    }

    public String getTimeZone() {
        return timeZone;
    }

    public void setTimeZone(String timeZone) {
        this.timeZone = timeZone;
    }

    public CallDisposition getLastCallDisposition() {
        return lastCallDisposition;
    }

    public void setLastCallDisposition(CallDisposition lastCallDisposition) {
        this.lastCallDisposition = lastCallDisposition;
    }

    public long getPhonePk() {
        return phonePk;
    }

    public void setPhonePk(long phonePk) {
        this.phonePk = phonePk;
    }

    public CustomerCallablePojo getCustomerCallable() {
        return customerCallable;
    }

    public void setCustomerCallable(CustomerCallablePojo customerCallable) {
        this.customerCallable = customerCallable;
    }

    public ZumigoLineType getVerifiedLineType() {
        return verifiedLineType;
    }

    public void setVerifiedLineType(ZumigoLineType verifiedLineType) {
        this.verifiedLineType = verifiedLineType;
    }

    public String getCarrier() {
        return carrier;
    }

    public void setCarrier(String carrier) {
        this.carrier = carrier;
    }

    public Boolean getLocationSupported() {
        return locationSupported;
    }

    public void setLocationSupported(Boolean locationSupported) {
        this.locationSupported = locationSupported;
    }

    public Boolean getIdentitySupported() {
        return identitySupported;
    }

    public void setIdentitySupported(Boolean identitySupported) {
        this.identitySupported = identitySupported;
    }
    
    @Override
    public String toString() {
        return "PhoneData{" + "areaCode=" + areaCode + ", phoneNumber=" + phoneNumber + ", phoneExtension=" + phoneExtension + ", phoneStatus=" + phoneStatus + ", phoneLabel=" + phoneLabel + ", phoneDoNotUse=" + phoneDoNotUse + ", phoneSource=" + phoneSource + ", specialInstr=" + specialInstr + ", notes=" + notes + '}';
    }

}
