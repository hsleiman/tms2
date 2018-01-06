package com.amp.crm.embeddable;

import com.objectbrains.enumerated.CallResponseCode;
import com.amp.crm.constants.OutboundRecordStatus;
import com.amp.crm.db.entity.base.CallResponseAction;
import com.amp.crm.db.entity.base.dialer.OutboundDialerQueueSettings;
import com.amp.crm.pojo.DialerQueueAccountDetails;
import com.amp.crm.pojo.DialerQueueRecord;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class OutboundDialerQueueRecord extends DialerQueueRecord<OutboundDialerQueueSettings> {
    
    private Map<CallResponseCode, CallResponseAction> callResponseMap;
    private List<DialerQueueAccountDetails> accountDetails = new ArrayList<>();
    private OutboundDialerQueueSettings dialerQueueSettings;
    private OutboundRecordStatus status;
            
    public OutboundDialerQueueRecord(long dqPk) {
        super(dqPk);
    }
    
    public OutboundDialerQueueRecord() {
    }
    
    public Map<CallResponseCode, CallResponseAction> getCallResponseMap() {
        return callResponseMap;
    }
    
    public void setCallResponseMap(Map<CallResponseCode, CallResponseAction> callResponseMap) {
        this.callResponseMap = callResponseMap;
    }

    public List<DialerQueueAccountDetails> getAccountDetails() {
        return accountDetails;
    }

    public void setAccountDetails(List<DialerQueueAccountDetails> accountDetails) {
        this.accountDetails = accountDetails;
    }

    @Override
    public OutboundDialerQueueSettings getDialerQueueSettings() {
        return dialerQueueSettings;
    }

    @Override
    public void setDialerQueueSettings(OutboundDialerQueueSettings dialerQueueSettings) {
        this.dialerQueueSettings = dialerQueueSettings;
    }

    public OutboundRecordStatus getStatus() {
        return status;
    }

    public void setStatus(OutboundRecordStatus status) {
        this.status = status;
    }
    
    
}
