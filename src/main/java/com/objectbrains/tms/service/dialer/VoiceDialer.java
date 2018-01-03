    /*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.objectbrains.tms.service.dialer;

import com.hazelcast.spring.context.SpringAware;
import com.objectbrains.svc.iws.CallDispositionCode;
import com.objectbrains.svc.iws.DialerQueueLoanDetails;
import com.objectbrains.svc.iws.OutboundDialerQueueRecord;
import com.objectbrains.tms.enumerated.DialerType;
import com.objectbrains.tms.hazelcast.entity.DialerCall;
import com.objectbrains.tms.websocket.message.outbound.PhoneToType;
import org.joda.time.LocalTime;

/**
 *
 * @author connorpetty
 */
@SpringAware
public class VoiceDialer extends AbstractDialer {

    private VoiceDialer() {
    }

    public VoiceDialer(long dialerPk, OutboundDialerQueueRecord record, LocalTime endTime) {
        super(dialerPk, record, endTime);
    }

    @Override
    protected void handleReadyLoansInternal() throws DialerException {
        while (makeNextCall(null)) {
            //do nothing
        }
    }

    @Override
    protected String makeCall(Integer ext, LoanNumber loanNumber, DialerQueueLoanDetails details) {
        PhoneToType phoneToType = Utils.getPhoneToType(loanNumber, details);
        callService.initiateBroadcast(record.getSvDialerQueueSettings(), details.getLoanPk(), phoneToType);
        return "";
    }

    @Override
    protected boolean shouldNumberCheck() {
        return false;
    }

    @Override
    public void callEnded(DialerCall call, CallDispositionCode dispositionCode) throws DialerException {
        super.callEnded(call, dispositionCode);
        //TODO makeNextCall(null);
    }

//    @Override
//    public void callFailed(DialerCall call, CallDispositionCode dispositionCode) throws DialerException {
//        super.callFailed(call, dispositionCode);
//        makeNextCall(null);
//    }
    @Override
    public DialerType getDialerType() {
        return DialerType.BROADCAST;
    }

}
