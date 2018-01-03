/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.objectbrains.tms.pojo;

import java.io.Serializable;
import org.joda.time.LocalDateTime;

/**
 *
 * @author hsleiman
 */
public class CallHistory implements Serializable {

    private Long pk;
    private LocalDateTime create_timestamp;
    private String ani;
    private String destination_number;
    private String status;
    private String borrowerName;

    private CallHistory() {
    }

    public CallHistory(Long pk,
            LocalDateTime create_timestamp,
            String ani,
            String destination_number,
            String status,
            String borrowerName) {
        this.pk = pk;
        this.create_timestamp = create_timestamp;
        this.ani = ani;
        this.destination_number = destination_number;
        this.status = status;
        this.borrowerName = borrowerName;
    }

    public Long getPk() {
        return pk;
    }

    public void setPk(Long pk) {
        this.pk = pk;
    }

    public LocalDateTime getCreate_timestamp() {
        return create_timestamp;
    }

    public void setCreate_timestamp(LocalDateTime create_timestamp) {
        this.create_timestamp = create_timestamp;
    }

    public String getAni() {
        return ani;
    }

    public void setAni(String ani) {
        this.ani = ani;
    }

    public String getDestination_number() {
        return destination_number;
    }

    public void setDestination_number(String destination_number) {
        this.destination_number = destination_number;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getBorrowerName() {
        return borrowerName;
    }

    public void setBorrowerName(String borrowerName) {
        this.borrowerName = borrowerName;
    }

}
