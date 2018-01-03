/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.objectbrains.tms.db.entity;

import com.objectbrains.tms.enumerated.PhoneStatus;
import com.objectbrains.tms.websocket.message.Function;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;
import org.joda.time.LocalDateTime;

/**
 *
 * @author hsleiman
 */
@Entity
@Table(schema = "tms")
public class WebsocketLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long pk;
    
    private String callUUID;
    
    private LocalDateTime createTimestamp;
    private Long createTimestampLong;
    private Integer ext;
    
    private String ipAddress;
    
    @Enumerated(value = EnumType.STRING)
    private Function function;
    private String confirmCode;
    
    @Enumerated(value = EnumType.STRING)
    private PhoneStatus phone;
    
    @Lob
    private String message;
    
    private String direction;

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

    public Integer getExt() {
        return ext;
    }

    public void setExt(Integer ext) {
        this.ext = ext;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

    public Function getFunction() {
        return function;
    }

    public void setFunction(Function function) {
        this.function = function;
    }

    public String getConfirmCode() {
        return confirmCode;
    }

    public void setConfirmCode(String confirmCode) {
        this.confirmCode = confirmCode;
    }

    public Long getCreateTimestampLong() {
        return createTimestampLong;
    }

    public void setCreateTimestampLong(Long createTimestampLong) {
        this.createTimestampLong = createTimestampLong;
    }

    public String getCallUUID() {
        return callUUID;
    }

    public void setCallUUID(String callUUID) {
        this.callUUID = callUUID;
    }

    public PhoneStatus getPhone() {
        return phone;
    }

    public void setPhone(PhoneStatus phone) {
        this.phone = phone;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }
    
}
