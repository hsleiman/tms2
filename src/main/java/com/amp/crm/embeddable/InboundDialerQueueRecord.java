package com.amp.crm.embeddable;

import com.amp.crm.db.entity.base.dialer.InboundDialerQueueSettings;
import com.amp.crm.pojo.DialerQueueRecord;

public class InboundDialerQueueRecord extends DialerQueueRecord<InboundDialerQueueSettings> {

     public InboundDialerQueueRecord(long dqPk) {
        super(dqPk);
    }
    
    public InboundDialerQueueRecord() {
    }
    
    private InboundDialerQueueSettings dialerQueueSettings;
    
    @Override
    public InboundDialerQueueSettings getDialerQueueSettings() {
        return dialerQueueSettings;
    }

    @Override
    public void setDialerQueueSettings(InboundDialerQueueSettings svDialerQueueSettings) {
        this.dialerQueueSettings = svDialerQueueSettings;
    }

}
