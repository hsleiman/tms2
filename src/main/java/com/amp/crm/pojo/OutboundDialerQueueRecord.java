/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.crm.pojo;

import com.objectbrains.enumerated.CallResponseCode;
import com.amp.crm.constants.OutboundRecordStatus;
import com.amp.crm.db.entity.base.CallResponseAction;
import com.amp.crm.db.entity.base.dialer.OutboundDialerQueueSettings;
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

    public List<DialerQueueAccountDetails> getLoanDetails() {
        return accountDetails;
    }

    public void setLoanDetails(List<DialerQueueAccountDetails> accountDetails) {
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
