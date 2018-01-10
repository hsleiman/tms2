/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.crm.embeddable;

import com.amp.crm.constants.AchPaymentStatus;
import com.amp.crm.constants.AchType;
import com.amp.crm.constants.AchVendors;
import java.math.BigDecimal;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.hibernate.annotations.Index;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;

@Embeddable
public class AchPaymentData implements Cloneable {

    @Index(name="IDX_ACH_PAYMENT_DATA_ACH_STATUS")
    private String achStatus;
    private String acctDetail;
    @Column(precision = 10, scale = 2)
    private BigDecimal allocationToMonthlyReceivables;
    @Column(precision = 10, scale = 2)
    private BigDecimal allocateToScheduledPayment;
    private Boolean allocatedToScheduledPayment;
    @Enumerated(EnumType.STRING)
    private AchType batchType;
    @Enumerated(EnumType.STRING)
    private AchType batchTypeOld;
    @Column(precision = 10, scale = 2)
    private BigDecimal desiredAllocationToCurtailment;
    private String correctionCategory;
    private String correctionInfo;
    private String correctionLine;
    private String giactResponseCode;
    private Boolean lateReturn;
    private String nameEntered;
    private Integer numberOfReversal;
    private LocalDate originalSettlementRtnDate;
    private Boolean payoffAccount;
    private String poolAcctId;
    private String returnCode;
    private Boolean sendPaymentReminder;
    private String settlementId;
    private LocalDateTime settlementRtnTimestamp;
    private Integer transactionCode;
    private String transactionNumber;
    private Long uniqueSequenceId;
    private boolean useBankDataOnFile;
    private boolean userScheduled;
    private String verifyStatus;
    private AchPaymentStatus status;
    private LocalDateTime sentTimestamp;
   @Enumerated(EnumType.STRING)
    private AchVendors sentToVendor;
    private String sentInFileName;
    private String returnOrSettleFileName;
    private Long payCreateTime;
    private Long payReverseTime;
    
    public long getPayCreateTime() {
        return payCreateTime;
    }

    public void setPayCreateTime(Long payCreateTime) {
        this.payCreateTime = payCreateTime;
    }

    public long getPayReverseTime() {
        return payReverseTime;
    }

    public void setPayReverseTime(Long payReverseTime) {
        this.payReverseTime = payReverseTime;
    }
    
    public LocalDateTime getSentTimestamp() {
        return sentTimestamp;
    }

    public void setSentTimestamp(LocalDateTime sentTimestamp) {
        this.sentTimestamp = sentTimestamp;
    }

    public AchVendors getSentToVendor() {
        return sentToVendor;
    }

    public void setSentToVendor(AchVendors sentToVendor) {
        this.sentToVendor = sentToVendor;
    }

    public String getSentInFileName() {
        return sentInFileName;
    }

    public void setSentInFileName(String sentInFileName) {
        this.sentInFileName = sentInFileName;
    }
    
    public String getAchStatus() {
        return achStatus;
    }

    public void setAchStatus(String achStatus) {
        this.achStatus = achStatus;
    }

    public String getAcctDetail() {
        return acctDetail;
    }

    public void setAcctDetail(String acctDetail) {
        this.acctDetail = acctDetail;
    }

    public BigDecimal getAllocationToMonthlyReceivables() {
        return allocationToMonthlyReceivables;
    }

    public void setAllocationToMonthlyReceivables(BigDecimal allocationToMonthlyReceivables) {
        this.allocationToMonthlyReceivables = allocationToMonthlyReceivables;
    }

    public BigDecimal getAllocateToScheduledPayment() {
        return allocateToScheduledPayment;
    }

    public void setAllocateToScheduledPayment(BigDecimal allocateToScheduledPayment) {
        this.allocateToScheduledPayment = allocateToScheduledPayment;
    }

    public Boolean isAllocatedToScheduledPayment() {
        return allocatedToScheduledPayment;
    }

    public void setAllocatedToScheduledPayment(Boolean allocatedToScheduledPayment) {
        this.allocatedToScheduledPayment = allocatedToScheduledPayment;
    }

    public AchType getBatchType() {
        return batchType;
    }

    public void setBatchType(AchType batchType) {
        this.batchType = batchType;
    }

    public AchType getBatchTypeOld() {
        return batchTypeOld;
    }

    public void setBatchTypeOld(AchType batchTypeOld) {
        this.batchTypeOld = batchTypeOld;
    }
    
    
    public BigDecimal getDesiredAllocationToCurtailment() {
        return desiredAllocationToCurtailment;
    }

    public void setDesiredAllocationToCurtailment(BigDecimal desiredAllocationToCurtailment) {
        this.desiredAllocationToCurtailment = desiredAllocationToCurtailment;
    }

    public String getCorrectionCategory() {
        return correctionCategory;
    }

    public void setCorrectionCategory(String correctionCategory) {
        this.correctionCategory = correctionCategory;
    }

    public String getCorrectionInfo() {
        return correctionInfo;
    }

    public void setCorrectionInfo(String correctionInfo) {
        this.correctionInfo = correctionInfo;
    }

    public String getCorrectionLine() {
        return correctionLine;
    }

    public void setCorrectionLine(String correctionLine) {
        this.correctionLine = correctionLine;
    }

    public String getGiactResponseCode() {
        return giactResponseCode;
    }

    public void setGiactResponseCode(String giactResponseCode) {
        this.giactResponseCode = giactResponseCode;
    }

    public Boolean isLateReturn() {
        return lateReturn;
    }

    public void setLateReturn(Boolean lateReturn) {
        this.lateReturn = lateReturn;
    }

    public String getNameEntered() {
        return nameEntered;
    }

    public void setNameEntered(String nameEntered) {
        this.nameEntered = nameEntered;
    }

    public Integer getNumberOfReversal() {
        return numberOfReversal;
    }

    public void setNumberOfReversal(Integer numberOfReversal) {
        this.numberOfReversal = numberOfReversal;
    }

    public LocalDate getOriginalSettlementRtnDate() {
        return originalSettlementRtnDate;
    }

    public void setOriginalSettlementRtnDate(LocalDate originalSettlementRtnDate) {
        this.originalSettlementRtnDate = originalSettlementRtnDate;
    }

    public Boolean isPayoffAccount() {
        return payoffAccount;
    }

    public void setPayoffAccount(Boolean payoffAccount) {
        this.payoffAccount = payoffAccount;
    }

    public String getPoolAcctId() {
        return poolAcctId;
    }

    public void setPoolAcctId(String poolAcctId) {
        this.poolAcctId = poolAcctId;
    }

    public String getReturnCode() {
        return returnCode;
    }

    public void setReturnCode(String returnCode) {
        this.returnCode = returnCode;
    }

    public Boolean isSendPaymentReminder() {
        return sendPaymentReminder;
    }

    public void setSendPaymentReminder(Boolean sendPaymentReminder) {
        this.sendPaymentReminder = sendPaymentReminder;
    }

    public String getSettlementId() {
        return settlementId;
    }

    public void setSettlementId(String settlementId) {
        this.settlementId = settlementId;
    }

    public LocalDateTime getSettlementRtnTimestamp() {
        return settlementRtnTimestamp;
    }

    public void setSettlementRtnTimestamp(LocalDateTime settlementRtnTimestamp) {
        this.settlementRtnTimestamp = settlementRtnTimestamp;
    }

    public Integer getTransactionCode() {
        return transactionCode;
    }

    public void setTransactionCode(Integer transactionCode) {
        this.transactionCode = transactionCode;
    }

    public String getTransactionNumber() {
        return transactionNumber;
    }

    public void setTransactionNumber(String transactionNumber) {
        this.transactionNumber = transactionNumber;
    }

    public Long getUniqueSequenceId() {
        return uniqueSequenceId;
    }

    public void setUniqueSequenceId(Long uniqueSequenceId) {
        this.uniqueSequenceId = uniqueSequenceId;
    }

    public boolean isUseBankDataOnFile() {
        return useBankDataOnFile;
    }

    public void setUseBankDataOnFile(boolean useBankDataOnFile) {
        this.useBankDataOnFile = useBankDataOnFile;
    }

    public boolean isUserScheduled() {
        return userScheduled;
    }

    public void setUserScheduled(boolean userScheduled) {
        this.userScheduled = userScheduled;
    }

    public String getVerifyStatus() {
        return verifyStatus;
    }

    public void setVerifyStatus(String verifyStatus) {
        this.verifyStatus = verifyStatus;
    }

    public AchPaymentStatus getStatus() {
        return status;
    }

    public void setStatus(AchPaymentStatus status) {
        this.status = status;
    }


    public String getReturnOrSettleFileName() {
        return returnOrSettleFileName;
    }

    public void setReturnOrSettleFileName(String returnOrSettleFileName) {
        this.returnOrSettleFileName = returnOrSettleFileName;
    }
    
         
    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
    

    @Override
    protected AchPaymentData clone() throws CloneNotSupportedException {
        return (AchPaymentData) super.clone(); 
    }
    
    public AchPaymentData copy() {
        try {
            return clone();
        } catch (CloneNotSupportedException ex) {
            throw new AssertionError("Error copying AchPaymentData: " , ex);
        }
    }
    
}
