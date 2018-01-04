/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.objectbrains.tms.service.dialer;

import com.hazelcast.spring.context.SpringAware;
import com.objectbrains.sti.pojo.DialerQueueAccountDetails;
import com.objectbrains.sti.pojo.OutboundDialerQueueRecord;
import com.objectbrains.tms.enumerated.DialerType;
import org.joda.time.LocalTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author hsleiman
 */
@SpringAware
public class PreviewDialer extends AbstractDialer {

    private static final Logger LOG = LoggerFactory.getLogger(PreviewDialer.class);

    private PreviewDialer() {
    }

    public PreviewDialer(long dialerPk, OutboundDialerQueueRecord record, LocalTime endTime) {
        super(dialerPk, record, endTime);
    }

    @Override
    protected String makeCall(Integer ext, LoanNumber loanNumber, DialerQueueAccountDetails details) {
        return callService.initiateCallPreviewSelect(getRecord().getDialerQueueSettings(), loanNumber.getLoanPk(), ext,
                Utils.getPhoneToTypes(details));
    }

    @Override
    protected LoanNumber getNextRetryNumber(LoanNumber call, DialerQueueAccountDetails details) {
        //for preview strategy we just reuse the same phone number over and over
        //until it has been dialed.
        return call;
    }
    
    @Override
    public DialerType getDialerType() {
        return DialerType.POWER;
    }

}
