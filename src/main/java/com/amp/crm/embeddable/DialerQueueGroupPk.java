/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.amp.crm.embeddable;

import com.amp.crm.constants.DialerQueueType;
import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;

/**
 *
 * @author HS
 */
@Embeddable
public class DialerQueueGroupPk implements Serializable {
    
    private long dialerQueuePk;
    private long dialerGroupPk;
    @Enumerated(EnumType.STRING)
    @Column(name = "dialer_queue_type", updatable = false, insertable = false)
    private DialerQueueType dialerQueueType;

    public DialerQueueGroupPk() {    
    }
    
    public DialerQueueGroupPk(long dialerQueuePk, long dialerGroupPk, DialerQueueType dialerQueueType) {
        this.dialerQueuePk = dialerQueuePk;
        this.dialerGroupPk = dialerGroupPk;
        this.dialerQueueType = dialerQueueType;
    }
    
    public long getDialerQueuePk() {
        return dialerQueuePk;
    }

    public void setDialerQueuePk(long dialerQueuePk) {
        this.dialerQueuePk = dialerQueuePk;
    }

    public long getDialerGroupPk() {
        return dialerGroupPk;
    }

    public void setDialerGroupPk(long dialerGroupPk) {
        this.dialerGroupPk = dialerGroupPk;
    }

    public DialerQueueType getDialerQueueType() {
        return dialerQueueType;
    }

    public void setDialerQueueType(DialerQueueType dialerQueueType) {
        this.dialerQueueType = dialerQueueType;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 53 * hash + (int) (this.dialerQueuePk ^ (this.dialerQueuePk >>> 32));
        hash = 53 * hash + (int) (this.dialerGroupPk ^ (this.dialerGroupPk >>> 32));
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final DialerQueueGroupPk other = (DialerQueueGroupPk) obj;
        if (this.dialerQueuePk != other.dialerQueuePk) {
            return false;
        }
        if (this.dialerGroupPk != other.dialerGroupPk) {
            return false;
        }
        return true;
    }
    
    
}
