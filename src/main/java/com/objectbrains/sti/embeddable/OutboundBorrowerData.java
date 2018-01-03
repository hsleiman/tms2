/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.objectbrains.sti.embeddable;

import com.objectbrains.sti.constants.PhoneNumberType;
import com.objectbrains.sti.constants.CallTimeCode;
import javax.persistence.Embeddable;
import org.joda.time.LocalDateTime;



@Embeddable
public class OutboundBorrowerData {
    private String outboundRecordUUID;
    private String firstName;
    private String lastName;
    private Long phoneNumber1;
    private PhoneNumberType phoneType1;
    private CallTimeCode callTimeCode1;
    private LocalDateTime earliestTimeToCall1;
    private Long phoneNumber2;
    private PhoneNumberType phoneType2;
    private CallTimeCode callTimeCode2;
    private LocalDateTime earliestTimeToCall2;
    private Long phoneNumber3;
    private PhoneNumberType phoneType3;
    private CallTimeCode callTimeCode3;
    private LocalDateTime earliestTimeToCall3;
    private Long phoneNumber4;
    private PhoneNumberType phoneType4;
    private CallTimeCode callTimeCode4;
    private LocalDateTime earliestTimeToCall4;

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

    public Long getPhoneNumber1() {
        return phoneNumber1;
    }

    public void setPhoneNumber1(Long phoneNumber1) {
        this.phoneNumber1 = phoneNumber1;
    }

    public PhoneNumberType getPhoneType1() {
        return phoneType1;
    }

    public void setPhoneType1(PhoneNumberType phoneType1) {
        this.phoneType1 = phoneType1;
    }

    public Long getPhoneNumber2() {
        return phoneNumber2;
    }

    public void setPhoneNumber2(Long phoneNumber2) {
        this.phoneNumber2 = phoneNumber2;
    }

    public PhoneNumberType getPhoneType2() {
        return phoneType2;
    }

    public void setPhoneType2(PhoneNumberType phoneType2) {
        this.phoneType2 = phoneType2;
    }

    public Long getPhoneNumber3() {
        return phoneNumber3;
    }

    public void setPhoneNumber3(Long phoneNumber3) {
        this.phoneNumber3 = phoneNumber3;
    }

    public PhoneNumberType getPhoneType3() {
        return phoneType3;
    }

    public void setPhoneType3(PhoneNumberType phoneType3) {
        this.phoneType3 = phoneType3;
    }

    public Long getPhoneNumber4() {
        return phoneNumber4;
    }

    public void setPhoneNumber4(Long phoneNumber4) {
        this.phoneNumber4 = phoneNumber4;
    }

    public PhoneNumberType getPhoneType4() {
        return phoneType4;
    }

    public void setPhoneType4(PhoneNumberType phoneType4) {
        this.phoneType4 = phoneType4;
    }

    public CallTimeCode getCallTimeCode1() {
        return callTimeCode1;
    }

    public void setCallTimeCode1(CallTimeCode callTimeCode1) {
        this.callTimeCode1 = callTimeCode1;
    }

    public LocalDateTime getEarliestTimeToCall1() {
        return earliestTimeToCall1;
    }

    public void setEarliestTimeToCall1(LocalDateTime earliestTimeToCall1) {
        this.earliestTimeToCall1 = earliestTimeToCall1;
    }

    public CallTimeCode getCallTimeCode2() {
        return callTimeCode2;
    }

    public void setCallTimeCode2(CallTimeCode callTimeCode2) {
        this.callTimeCode2 = callTimeCode2;
    }

    public LocalDateTime getEarliestTimeToCall2() {
        return earliestTimeToCall2;
    }

    public void setEarliestTimeToCall2(LocalDateTime earliestTimeToCall2) {
        this.earliestTimeToCall2 = earliestTimeToCall2;
    }

    public CallTimeCode getCallTimeCode3() {
        return callTimeCode3;
    }

    public void setCallTimeCode3(CallTimeCode callTimeCode3) {
        this.callTimeCode3 = callTimeCode3;
    }

    public LocalDateTime getEarliestTimeToCall3() {
        return earliestTimeToCall3;
    }

    public void setEarliestTimeToCall3(LocalDateTime earliestTimeToCall3) {
        this.earliestTimeToCall3 = earliestTimeToCall3;
    }

    public CallTimeCode getCallTimeCode4() {
        return callTimeCode4;
    }

    public void setCallTimeCode4(CallTimeCode callTimeCode4) {
        this.callTimeCode4 = callTimeCode4;
    }

    public LocalDateTime getEarliestTimeToCall4() {
        return earliestTimeToCall4;
    }

    public void setEarliestTimeToCall4(LocalDateTime earliestTimeToCall4) {
        this.earliestTimeToCall4 = earliestTimeToCall4;
    }

    public String getOutboundRecordUUID() {
        return outboundRecordUUID;
    }

    public void setOutboundRecordUUID(String outboundRecordUUID) {
        this.outboundRecordUUID = outboundRecordUUID;
    }
    
    
}
