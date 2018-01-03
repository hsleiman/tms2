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
@DiscriminatorValue(value = DialerQueueType.INBOUND_TYPE)
public class InboundDialerQueue extends DialerQueue {
    
    @XmlTransient
    @JsonIgnore
    @OneToOne(fetch = FetchType.LAZY, mappedBy = "dialerQueue", cascade = CascadeType.ALL)
    private InboundDialerQueueSettings dialerQueueSettings;

    @Override
    public InboundDialerQueueSettings getDialerQueueSettings() {
        return dialerQueueSettings;
    }

    public void setDialerQueueSettings(InboundDialerQueueSettings dialerQueueSettings) {
        this.dialerQueueSettings = dialerQueueSettings;
    }

    @Override
    public void setDialerQueueForAccount(Account account, DialerQueue queue) {
        account.setInboundDialerQueue((InboundDialerQueue) queue);
    }

}
