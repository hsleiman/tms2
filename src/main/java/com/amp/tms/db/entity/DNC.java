/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.tms.db.entity;

import com.amp.tms.enumerated.DncStatus;
import java.io.Serializable;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.PrePersist;
import javax.persistence.Table;
import org.hibernate.annotations.Index;
import org.joda.time.LocalDateTime;

/**
 *
 * 
 */
@Entity
@Table(schema = "crm")
public class DNC implements Serializable{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long pk;

    @Index(name = "idx_phone_number")
    private String phoneNumber;
    
    @Enumerated(EnumType.STRING)
    private DncStatus status;

    private LocalDateTime createTimestamp;
    
    @Index(name = "idx_expire_timestamp")
    private LocalDateTime expireTimestamp;

    private String reason;

    public DNC() {
    }

    public DNC(String phoneNumber, String reason, LocalDateTime expireTimestamp) {
        this.phoneNumber = phoneNumber;
        this.expireTimestamp = expireTimestamp;
        this.reason = reason;
        status = DncStatus.ACTIVE;
    }

    @PrePersist
    public void onCreate() {
        createTimestamp = LocalDateTime.now();
    }

    public Long getPk() {
        return pk;
    }

    public void setPk(Long pk) {
        this.pk = pk;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public DncStatus getStatus() {
        return status;
    }

    public void setStatus(DncStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreateTimestamp() {
        return createTimestamp;
    }

    public void setCreateTimestamp(LocalDateTime createTimestamp) {
        this.createTimestamp = createTimestamp;
    }

    public LocalDateTime getExpireTimestamp() {
        return expireTimestamp;
    }

    public void setExpireTimestamp(LocalDateTime expireTimestamp) {
        this.expireTimestamp = expireTimestamp;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

}
