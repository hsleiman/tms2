/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.tms.db.entity.cdr;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.PrePersist;
import javax.persistence.Table;
import org.joda.time.LocalDateTime;

/**
 *
 * 
 */
@Entity
@Table(schema = "crm")
public class SpeechToTextTms implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long pk;

    @Column(updatable = false, nullable = false)
    private LocalDateTime createTimestamp;

    @PrePersist
    private void onCreate() {
        createTimestamp = LocalDateTime.now();
    }

    @Lob
    private String bothChannal;
    @Lob
    private String leftChannal;
    @Lob
    private String rightChannal;
    

    private String call_uuid;
    @Column(length = 10000)
    private String text;

    private LocalDateTime timestamp;

    private Double confidence;

    public Long getPk() {
        return pk;
    }

    public void setPk(Long pk) {
        this.pk = pk;
    }

    public LocalDateTime getCreateTimestamp() {
        return createTimestamp;
    }

    public void setCreateTimestamp(LocalDateTime createTimestamp) {
        this.createTimestamp = createTimestamp;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getCall_uuid() {
        return call_uuid;
    }

    public void setCall_uuid(String call_uuid) {
        this.call_uuid = call_uuid;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public Double getConfidence() {
        return confidence;
    }

    public void setConfidence(Double confidence) {
        this.confidence = confidence;
    }

    public String getBothChannal() {
        return bothChannal;
    }

    public void setBothChannal(String bothChannal) {
        this.bothChannal = bothChannal;
    }

    public String getLeftChannal() {
        return leftChannal;
    }

    public void setLeftChannal(String leftChannal) {
        this.leftChannal = leftChannal;
    }

    public String getRightChannal() {
        return rightChannal;
    }

    public void setRightChannal(String rightChannal) {
        this.rightChannal = rightChannal;
    }
    

}
