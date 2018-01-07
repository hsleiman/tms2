    /*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.tms.service.dialer;

import com.hazelcast.spring.context.SpringAware;
import com.amp.crm.db.entity.disposition.CallDispositionCode;
import com.amp.crm.embeddable.OutboundDialerQueueRecord;
import com.amp.crm.pojo.DialerQueueAccountDetails;
import com.amp.tms.enumerated.DialerType;
import com.amp.tms.hazelcast.entity.DialerCall;
import com.amp.tms.websocket.message.outbound.PhoneToType;
import org.joda.time.LocalTime;

/**
 *
 * @author Hoang, J, Bishistha
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
    protected String makeCall(Integer ext, LoanNumber loanNumber, DialerQueueAccountDetails details) {
        PhoneToType phoneToType = Utils.getPhoneToType(loanNumber, details);
        callService.initiateBroadcast(record.getDialerQueueSettings(), details.getAccountPk(), phoneToType);
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
