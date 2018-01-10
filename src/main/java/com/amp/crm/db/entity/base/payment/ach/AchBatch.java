/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.crm.db.entity.base.payment.ach;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.amp.crm.constants.AchVendors;
import com.amp.crm.constants.BatchItemStatus;
import com.amp.crm.db.entity.superentity.SuperEntity;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.PrePersist;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlTransient;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;

/**
 *
 * 
 */
@NamedQueries({
    @NamedQuery(
        name="AchBatch.LocateAllByBatchDate",
        query="SELECT s FROM AchBatch s where s.batchDate=:batchDate"),
    @NamedQuery(
        name="AchBatch.LocateAllByBatchDateAndType",
        query="SELECT s FROM AchBatch s where s.batchDate=:batchDate and s.batchType=:batchType")
})
@Entity
@Table(schema = "crm")
public class AchBatch extends SuperEntity {


    @XmlTransient
    @JsonIgnore
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "achBatch")
    private Set<AchPayment> achPayments = new HashSet<>(0);
 
    private LocalDateTime creationTimestamp;
    private LocalDate batchDate;
    private Integer batchType;
    private LocalDateTime lastEntryTimestamp;
    private Long numberForDate;
    private LocalDate postingDate;
    private Long runId;
    private LocalDateTime sentTimestamp;
    private String servicingCompanyId;
    private Integer status;
    private BigDecimal totalAchBatchAmount;
    private Integer totalNumberOfPayments;
    @Enumerated(EnumType.STRING)
    private AchVendors achVendor;
    
    @Enumerated(EnumType.STRING)
    private BatchItemStatus batchingStatus = BatchItemStatus.CREATED;

    public void removeAchPayment(AchPayment achPayment) {
        Set<AchPayment> apList = this.getAchPayments();
        if (apList.remove(achPayment)) {
            this.setTotalAchBatchAmount(this.getTotalAchBatchAmount().subtract(achPayment.getEftPaymentBasicData().getPaymentAmount()));
            this.setTotalNumberOfPayments(this.getTotalNumberOfPayments() - 1);
            this.setAchPayments(apList);
        }
    }

    public BatchItemStatus getBatchingStatus() {
        return batchingStatus;
    }

    public void setBatchingStatus(BatchItemStatus batchingStatus) {
        this.batchingStatus = batchingStatus;
    }

    public Set<AchPayment> getAchPayments() {
        return achPayments;
    }

    public void setAchPayments(Set<AchPayment> achPayments) {
        this.achPayments = achPayments;
    }

    public LocalDateTime getCreationTimestamp() {
        return creationTimestamp;
    }

    public void setCreationTimestamp(LocalDateTime creationTimestamp) {
        this.creationTimestamp = creationTimestamp;
    }

    public LocalDate getBatchDate() {
        return batchDate;
    }

    public void setBatchDate(LocalDate batchDate) {
        this.batchDate = batchDate;
    }

    public Integer getBatchType() {
        return batchType;
    }

    public void setBatchType(Integer batchType) {
        this.batchType = batchType;
    }

    public LocalDateTime getLastEntryTimestamp() {
        return lastEntryTimestamp;
    }

    public void setLastEntryTimestamp(LocalDateTime lastEntryTimestamp) {
        this.lastEntryTimestamp = lastEntryTimestamp;
    }

    public Long getNumberForDate() {
        return numberForDate;
    }

    public void setNumberForDate(Long numberForDate) {
        this.numberForDate = numberForDate;
    }

    public LocalDate getPostingDate() {
        return postingDate;
    }

    public void setPostingDate(LocalDate postingDate) {
        this.postingDate = postingDate;
    }

    public Long getRunId() {
        return runId;
    }

    public void setRunId(Long runId) {
        this.runId = runId;
    }

    public LocalDateTime getSentTimestamp() {
        return sentTimestamp;
    }

    public void setSentTimestamp(LocalDateTime sentTimestamp) {
        this.sentTimestamp = sentTimestamp;
    }

    public String getServicingCompanyId() {
        return servicingCompanyId;
    }

    public void setServicingCompanyId(String servicingCompanyId) {
        this.servicingCompanyId = servicingCompanyId;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public BigDecimal getTotalAchBatchAmount() {
        return totalAchBatchAmount;
    }

    public void setTotalAchBatchAmount(BigDecimal totalAchBatchAmount) {
        this.totalAchBatchAmount = totalAchBatchAmount;
    }

    public Integer getTotalNumberOfPayments() {
        return totalNumberOfPayments;
    }

    public void setTotalNumberOfPayments(Integer totalNumberOfPayments) {
        this.totalNumberOfPayments = totalNumberOfPayments;
    }

    public AchVendors getVendorId() {
        return achVendor;
    }

    public void setVendorId(AchVendors vendorId) {
        this.achVendor = vendorId;
    }

 
 
    public String dump() {
        return ToStringBuilder.reflectionToString(this);
    }
    
    @PrePersist
    private void onCreate(){
        this.setCreationTimestamp(LocalDateTime.now());
    }

}
