/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.objectbrains.tms.db.entity.freeswitch;

import com.objectbrains.tms.enumerated.FreeswitchContext;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.PrePersist;
import javax.persistence.Table;
import org.joda.time.LocalDateTime;

/**
 *
 * @author hsleiman
 */
@Entity
@Table(schema = "tms")
public class LogDialplan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long pk;

    @Column(length = 10000)
    private String Dump;
    @Column(length = 10000)
    private String xml;
    private String tms_uuid;
    private String orderPower;
    @Enumerated(value = EnumType.STRING)
    private FreeswitchContext context;
    private String Caller_ANI;
    private String Caller_Destination_Number;

    private LocalDateTime createDateTime;

    public Long getPk() {
        return pk;
    }

    public void setPk(Long pk) {
        this.pk = pk;
    }

    public FreeswitchContext getContext() {
        return context;
    }

    public void setContext(FreeswitchContext context) {
        this.context = context;
    }

    public String getTms_uuid() {
        return tms_uuid;
    }

    public void setTms_uuid(String tms_uuid) {
        this.tms_uuid = tms_uuid;
    }

    public String getOrderPower() {
        return orderPower;
    }

    public void setOrderPower(String orderPower) {
        this.orderPower = orderPower;
    }
    
    public String getDump() {
        return Dump;
    }

    public void setDump(String Dump) {
        this.Dump = Dump;
    }

    public String getXml() {
        return xml;
    }

    public void setXml(String xml) {
        this.xml = xml;
    }

    public String getCaller_ANI() {
        return Caller_ANI;
    }

    public void setCaller_ANI(String Caller_ANI) {
        this.Caller_ANI = Caller_ANI;
    }

    public String getCaller_Destination_Number() {
        return Caller_Destination_Number;
    }

    public void setCaller_Destination_Number(String Caller_Destination_Number) {
        this.Caller_Destination_Number = Caller_Destination_Number;
    }

    public LocalDateTime getCreateDateTime() {
        return createDateTime;
    }

    public void setCreateDateTime(LocalDateTime createDateTime) {
        this.createDateTime = createDateTime;
    }

    @PrePersist
    public void createTime() {
        setCreateDateTime(LocalDateTime.now());
    }

}
