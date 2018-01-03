/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.objectbrains.sti.db.entity.base.dialer;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.objectbrains.sti.constants.DialerQueueType;
import com.objectbrains.sti.db.entity.base.account.Account;
import javax.persistence.CascadeType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToOne;
import javax.xml.bind.annotation.XmlTransient;

/**
 *
 * @author David
 */
@Entity
@DiscriminatorValue(value = DialerQueueType.OUTBOUND_TYPE)
public class OutboundDialerQueue extends DialerQueue {

    @XmlTransient
    @JsonIgnore
    @OneToOne(fetch = FetchType.LAZY, mappedBy = "dialerQueue", cascade = CascadeType.ALL)
    private OutboundDialerQueueSettings dialerQueueSettings;
    
    @Override
    public void setDialerQueueForAccount(Account account, DialerQueue queue) {
        account.setOutboundDialerQueue((OutboundDialerQueue) queue);
    }

    @Override
    public OutboundDialerQueueSettings getDialerQueueSettings() {
        return dialerQueueSettings;
    }

    public void setDialerQueueSettings(OutboundDialerQueueSettings DialerQueueSettings) {
        this.dialerQueueSettings = DialerQueueSettings;
    }
    
}

