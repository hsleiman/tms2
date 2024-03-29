/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.tms.service.dialer;

import com.hazelcast.spring.context.SpringAware;
import com.amp.crm.embeddable.OutboundDialerQueueRecord;
import com.amp.crm.pojo.DialerQueueAccountDetails;
import com.amp.tms.enumerated.DialerType;
import com.amp.tms.websocket.message.outbound.PhoneToType;
import org.joda.time.LocalTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * 
 */
@SpringAware
public class PowerDialer extends AbstractDialer {

    private static final Logger log = LoggerFactory.getLogger(PowerDialer.class);

    private PowerDialer() {
    }

    public PowerDialer(long dialerPk, OutboundDialerQueueRecord record, LocalTime endTime) {
        super(dialerPk, record, endTime);
    }

    @Override
    protected String makeCall(Integer ext, LoanNumber loanNumber, DialerQueueAccountDetails details) {
        PhoneToType phoneToType = Utils.getPhoneToType(loanNumber, details);
        return callService.initiateCall(getRecord().getDialerQueueSettings(),
                loanNumber.getLoanPk(),
                ext, phoneToType);
    }

    @Override
    public DialerType getDialerType() {
        return DialerType.POWER;
    }

}
