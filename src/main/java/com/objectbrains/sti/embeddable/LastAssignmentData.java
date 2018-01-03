/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.objectbrains.sti.embeddable;

import javax.persistence.Embeddable;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import org.joda.time.LocalDateTime;

/**
 *
 * @author David
 */
@Embeddable
@XmlAccessorType(XmlAccessType.FIELD)
public class LastAssignmentData {
    private long queuePk;
    private LocalDateTime lastAssignmentTime;

    public LastAssignmentData() {
    }

    public LastAssignmentData(long queuePk, LocalDateTime lastAssignmentTime) {
        this.queuePk = queuePk;
        this.lastAssignmentTime = lastAssignmentTime;
    }

    public long getQueuePk() {
        return queuePk;
    }

    public void setQueuePk(long queuePk) {
        this.queuePk = queuePk;
    }

    public LocalDateTime getLastAssignmentTime() {
        return lastAssignmentTime;
    }

    public void setLastAssignmentTime(LocalDateTime lastAssignmentTime) {
        this.lastAssignmentTime = lastAssignmentTime;
    }
    
    @Override
    public String toString() {
        return "LastAssignmentData[pk=" + queuePk + ",time=" + lastAssignmentTime + "]";
    }
}

