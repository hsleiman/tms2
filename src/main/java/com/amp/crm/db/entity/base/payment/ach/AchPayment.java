/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.crm.db.entity.base.payment.ach;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.amp.crm.constants.AchPaymentStatus;
import com.amp.crm.constants.AchType;
import com.amp.crm.db.entity.base.account.Account;
import com.amp.crm.db.entity.base.payment.EFTPayment;
import com.amp.crm.db.entity.base.payment.Payment;
import com.amp.crm.embeddable.AchPaymentData;
import com.amp.crm.embeddable.BasicBankData;
import com.amp.crm.pojo.AchPaymentDetails;
import com.amp.crm.pojo.AchPaymentResponse;
import java.util.Set;
import javax.persistence.Column;
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
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.hibernate.annotations.ForeignKey;

/**
 *
 * 
 */
@NamedQueries({
    @NamedQuery(
            name = "AchPayment.LocateByPk",
            query = "SELECT s FROM AchPayment s WHERE s.pk = :pk"
    ),
    @NamedQuery(
            name = "AchPayment.LocateByUniqueSequenceId",
            query = "SELECT s FROM AchPayment s WHERE s.achPaymentData.uniqueSequenceId = :uniqueSequenceId"
    ),
    @NamedQuery(
            name = "AchPayment.LocateAllByLoanPkAndStatus",
            query = "SELECT s FROM AchPayment s WHERE s.account.pk = :accountPK AND s.achPaymentData.status = :status ORDER BY s.eftPaymentBasicData.postingDate desc, s.eftPaymentBasicData.paymentTime desc"
    ),
    @NamedQuery(
            name = "AchPayment.LocateAllPendingAchsForLoan",
            query = "SELECT s FROM AchPayment s WHERE s.account.pk = :accountPK AND ( (s.achPaymentData.status = 0 OR s.achPaymentData.status = 10 OR s.achPaymentData.status = 11) AND s.achPaymentData.achStatus is NULL) ORDER BY s.eftPaymentBasicData.postingDate ASC, s.eftPaymentBasicData.paymentTime ASC"
    ),
    @NamedQuery(
            name = "AchPayment.LocatePendingAchsForLoan",
            query = "SELECT s FROM AchPayment s WHERE s.account.pk = :accountPK AND ( s.achPaymentData.status = 0  AND s.achPaymentData.achStatus is NULL) ORDER BY s.eftPaymentBasicData.postingDate ASC, s.eftPaymentBasicData.paymentTime ASC"
    ),
    @NamedQuery(
            name = "AchPayment.GetPendingAchPaymentsForEarlySettle",
            query = "SELECT s FROM AchPayment s WHERE (s.achPaymentData.status = 10) AND (s.achPaymentData.achStatus is NULL) AND (s.achPaymentData.numberOfReversal is NULL OR s.achPaymentData.numberOfReversal = 0) AND s.achPaymentData.returnCode is NULL AND s.achBatch = :achbatch"
    ),
    @NamedQuery(
            name = "AchPayment.GetPendingAchForLoanBetweenDates",
            query = "SELECT s FROM AchPayment s WHERE s.account = :account  "
                    + "AND ((s.achPaymentData.achStatus is NULL) AND (s.achPaymentData.status = 0 OR s.achPaymentData.status = 10 OR s.achPaymentData.status = 11))"
                    + " AND s.achBatch.postingDate  BETWEEN :startDate AND :endDate"
    ),
    @NamedQuery(
            name = "AchPayment.LocateAllNonCanceledAchsForLoan",
            query = "SELECT s FROM AchPayment s WHERE s.account.pk = :loanPk AND s.achPaymentData.status<>-2 ORDER BY s.eftPaymentBasicData.postingDate DESC"
    )
})
@Entity
@Table(schema = "crm")
@XmlAccessorType(XmlAccessType.FIELD)
public class AchPayment extends EFTPayment {

    @XmlTransient
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_pk", referencedColumnName = "pk")
    @ForeignKey(name = "fk_ach_payment_account")
    private Account account;

    @XmlTransient
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ach_batch_pk", referencedColumnName = "pk")
    @ForeignKey(name = "fk_ach_payment_batch")
    private AchBatch achBatch;
    
    @Column(name = "ach_batch_pk", insertable = false, updatable = false)
    private Long achBatchPk;

    @XmlTransient
    @JsonIgnore
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_pk", referencedColumnName = "pk")
    @ForeignKey(name = "fk_ach_payment_allocation_payment")
    private Payment payment;

    @Embedded
    private BasicBankData bankData = new BasicBankData();

    @Embedded
    private AchPaymentData achPaymentData = new AchPaymentData();
  
    
    public void associateAchPaymentToAccount(Account account) {
        this.setAccount(account);
        account.getAchPayments().add(this);
    }

    public void disAssociateAchPaymentFromLoan() {
        Account account = this.getAccount();
        if (account != null) {
            Set<AchPayment> achPayments = account.getAchPayments();
            if (achPayments.remove(this)) {
                account.setAchPayments(achPayments);
            }
            this.setAccount(null);
        }
    }

    public void associateAchPaymentToBatch(AchBatch svAchBatch) {
        this.setAchBatch(achBatch);
        achBatch.getAchPayments().add(this);
    }

    public void disAssociateAchPaymentFromBatch() {
        AchBatch achBatch = this.getAchBatch();
        if (achBatch != null) {
            achBatch.removeAchPayment(this);
            this.setAchBatch(null);
        }
    }

    public void associateToPayment(Payment payment) {
        this.setPayment(payment);
        payment.setAchPayment(this);
    }

    public void disAssociateFromSvPayment() {
        Payment payment = this.getPayment();
        if (payment != null) {
            payment.setAchPayment(null);
            this.setPayment(null);
        }
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public AchBatch getAchBatch() {
        return achBatch;
    }

    public void setAchBatch(AchBatch achBatch) {
        this.achBatch = achBatch;
    }

    public Payment getPayment() {
        return payment;
    }

    public void setPayment(Payment payment) {
        this.payment = payment;
    }
    
    


    public AchPaymentData getAchPaymentData() {
        return achPaymentData;
    }

    public void setAchPaymentData(AchPaymentData achPaymentData) {
        this.achPaymentData = achPaymentData;
    }

    public BasicBankData getBankData() {
        return bankData;
    }

    public void setBankData(BasicBankData bankData) {
        this.bankData = bankData;
    }

    public Long getAchBatchPk() {
        return achBatchPk;
    }

    public void setAchBatchPk(Long achBatchPk) {
        this.achBatchPk = achBatchPk;
    }


    public String dump() {
        return ToStringBuilder.reflectionToString(this);
    }

    public AchPaymentDetails toAchPaymentDetails() {
        AchPaymentDetails achPaymentDetails = new AchPaymentDetails();
        AchPaymentData achData = this.getAchPaymentData();
        achPaymentDetails.setAchPaymentPk(this.getPk());
        if (account != null) {
            achPaymentDetails.setAccountPK(this.getAccount().getPk());
        }
        achPaymentDetails.setEftPaymentBasicData(this.getEftPaymentBasicData());
        achPaymentDetails.setUniqueSequenceId(achData.getUniqueSequenceId());
        AchPaymentStatus status = this.getAchPaymentData().getStatus();
        achPaymentDetails.setPaymentStatus(status);
        if (status != null) {
            achPaymentDetails.getEftPaymentBasicData().setStatusDescription(
                    status.getDescription());
        }
        if(achData.getBatchType() != null) {
            achPaymentDetails.setAchBatchType(achData.getBatchType());
        }
        achPaymentDetails.setAchStatus(achData.getAchStatus());
        if(achData.getAchStatus()!=null && achData.getAchStatus().equals("3RET"))
            achPaymentDetails.setReturnCodeDesc(getDescriptionForReturnCode(achData.getReturnCode()));
        return achPaymentDetails;
    }

    public AchPaymentResponse toAchPaymentResponse() {
        AchPaymentResponse achPaymentResponse = new AchPaymentResponse();
        achPaymentResponse.setAchPaymentPk(this.getPk());
        achPaymentResponse.setUniqueSequenceId(this.getAchPaymentData().getUniqueSequenceId());
        return achPaymentResponse;
    }
    
    public AchPayment newInstance() {
        AchPayment achPayment = new AchPayment();
        achPayment.setAchPaymentData(this.getAchPaymentData().copy());
        achPayment.setBankData(this.getBankData().copy());
        achPayment.setEftPaymentBasicData(this.getEftPaymentBasicData().copy());
        return achPayment;
    } 
    
    public static String getDescriptionForReturnCode(String returnCode){
        switch (returnCode){
            case "R01":
                return "ACH returned; NSF";
            case "R09":
                return "ACH returned; uncollected funds";
            case "R16":
                return "ACH returned; frozen account";
            case "R20":
                return "ACH returned; nontransaction account";
            case "R14":
                return "ACH returned DECEASED; please investigate";
            case "R15":
                return "ACH returned DECEASED; please investigate";
            case "R02":
                return "ACH returned due to closed account; please obtain new banking information";
            case "R03":
                return "ACH returned due to UTL; please correct banking information";
            case "R04":
                return "ACH returned due to UTL; please correct banking information";
            case "R07":
                return "ACH returned due to R07 (permanent stop payment); customer is revoking our authorization to ACH account";
            case "R08":
                return "ACH returned due to R08 (stop payment); customer is placing a stop on ACH payment plan";
            case "R10":
                return "ACH returned due to R10 (unauthorized); customer claims we do not have ACH authorization";
            case "R05":
                return "ACH returned due to denied authorization";
            case "R29":
                return "ACH returned due to denied authorization";
            case "R51":
                return "ACH returned due to denied authorization";
            default:
                return "ACH returned";
         
        }
    }
}
