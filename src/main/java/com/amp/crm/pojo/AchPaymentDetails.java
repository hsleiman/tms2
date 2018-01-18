/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.amp.crm.pojo;

import com.amp.crm.constants.AchPaymentStatus;
import com.amp.crm.constants.AchType;
import com.amp.crm.embeddable.EftPaymentBasicData;



public class AchPaymentDetails {

    private long accountPK;
    private long achPaymentPk;
    private EftPaymentBasicData eftPaymentBasicData;
    private Long uniqueSequenceId;
    private AchType achBatchType;
    private String achStatus;
    private AchPaymentStatus paymentStatus;
    private String returnCodeDesc;
    
    public long getAccountPK() {
        return accountPK;
    }

    public void setAccountPK(long accountPK) {
        this.accountPK = accountPK;
    }

    public long getAchPaymentPk() {
        return achPaymentPk;
    }

    public void setAchPaymentPk(long achPaymentPk) {
        this.achPaymentPk = achPaymentPk;
    }
    
    public EftPaymentBasicData getEftPaymentBasicData() {
        return eftPaymentBasicData;
    }

    public void setEftPaymentBasicData(EftPaymentBasicData eftPaymentBasicData) {
        this.eftPaymentBasicData = eftPaymentBasicData;
    }

    public Long getUniqueSequenceId() {
        return uniqueSequenceId;
    }

    public void setUniqueSequenceId(Long uniqueSequenceId) {
        this.uniqueSequenceId = uniqueSequenceId;
    }

    public AchType getAchBatchType() {
        return achBatchType;
    }

    public void setAchBatchType(AchType achBatchType) {
        this.achBatchType = achBatchType;
    }

    public String getAchStatus() {
        return achStatus;
    }

    public void setAchStatus(String achStatus) {
        this.achStatus = achStatus;
    }

    public AchPaymentStatus getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(AchPaymentStatus status) {
        this.paymentStatus = status;
    }

    public String getReturnCodeDesc() {
        return returnCodeDesc;
    }

    public void setReturnCodeDesc(String returnCode) {
        this.returnCodeDesc = returnCode;
    }
    
    
   
}
