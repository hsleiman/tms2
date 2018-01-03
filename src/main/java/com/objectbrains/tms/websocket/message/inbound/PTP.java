/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.objectbrains.tms.websocket.message.inbound;

import com.google.gson.annotations.Expose;
import java.io.Serializable;
import org.joda.time.LocalDateTime;

/**
 *
 * @author hsleiman
 */
public class PTP implements Serializable {
    @Expose
    private LocalDateTime date;
    @Expose
    private Double amount;

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }
    
}
