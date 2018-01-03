package com.objectbrains.sti.embeddable;

import com.objectbrains.sti.db.entity.base.dialer.InboundDialerQueueSettings;
import com.objectbrains.sti.pojo.DialerQueueRecord;

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
