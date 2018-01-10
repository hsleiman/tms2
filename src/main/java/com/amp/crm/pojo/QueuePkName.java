/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.crm.pojo;

/**
 *
 * 
 */
public class QueuePkName {
    
    private long pk;
    private String queueName;

    public QueuePkName() {
    }

    public QueuePkName(long pk, String queueName) {
        this.pk = pk;
        this.queueName = queueName;
    }

    public long getPk() {
        return pk;
    }

    public void setPk(long pk) {
        this.pk = pk;
    }

    public String getQueueName() {
        return queueName;
    }

    public void setQueueName(String queueName) {
        this.queueName = queueName;
    }
    
}

