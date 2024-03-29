/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.tms.freeswitch.premaid.incoming.service;

import com.amp.crm.service.tms.TMSService;
import com.amp.tms.db.entity.freeswitch.TMSDialplan;
import com.amp.tms.enumerated.FreeswitchContext;
import com.amp.tms.enumerated.RecordedPhrases;
import com.amp.tms.enumerated.refrence.HOLDOrder;
import com.amp.tms.enumerated.refrence.IVROrder;
import com.amp.tms.freeswitch.dialplan.action.Answer;
import com.amp.tms.freeswitch.dialplan.action.BridgeToAgent;
import com.amp.tms.freeswitch.dialplan.action.BridgeToFifo;
import com.amp.tms.freeswitch.dialplan.action.Playback;
import com.amp.tms.freeswitch.dialplan.action.Sleep;
import com.amp.tms.freeswitch.dialplan.action.TMSOrder;
import com.amp.tms.freeswitch.dialplan.action.Transfer;
import com.amp.tms.freeswitch.pojo.AgentIncomingDistributionOrder;
import com.amp.tms.freeswitch.pojo.DialplanVariable;
import com.amp.tms.freeswitch.premaid.incoming.IncomingDialerOrder;
import com.amp.tms.service.TMSAgentService;
import com.amp.tms.service.CallDetailRecordService;
import com.amp.tms.service.FreeswitchConfiguration;
import com.amp.tms.service.FreeswitchService;
import com.amp.tms.service.InboundCallService;
import com.amp.tms.service.TextToSpeechService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * 
 */
@Service("IVRMain")
public class IVRMain {

    protected final static Logger log = LoggerFactory.getLogger(IVRMain.class);

    @Autowired
    private CallDetailRecordService callDetailRecordService;

    @Autowired
    private TMSService tmsIWS;

    @Autowired
    private FreeswitchConfiguration configuration;

    @Autowired
    private TMSAgentService agentService;

    @Autowired
    private InboundCallService inboundCallService;

    @Autowired
    private TextToSpeechService textToSpeechService;

    @Autowired
    private FreeswitchService freeswitchService;

    public TMSDialplan VerifyMain(DialplanVariable variable, TMSDialplan tmsDialplan) {
        tmsDialplan.addAction(new Answer());
        tmsDialplan.addAction(new Sleep(1000l));

        if (tmsDialplan.getCounter() > 6) {
            sentToCustomerServiceFiFo(variable, tmsDialplan);
        } else {
            //AgentIncomingDistributionOrder aido = inboundCallService.inboundCallOrder(tmsDialplan.getCallerLong(), tmsDialplan.getBorrowerInfo().getLoanId());
            switch (variable.getOptionSelectedId()) {
                case 0:
                    sentToCustomerServiceFiFo(variable, tmsDialplan);
                    break;
//                case 1:
//                    String fileToPlay = RecordedPhrases.TYPE_OF_PAYMENT.getAudioPath();
//                    String invalidToPlay = RecordedPhrases.INVALID_ENTRY.getAudioPath();
//                    tmsDialplan.addAction(new PlayAndGetDigits(1, 1, 3, 5000, "#*", fileToPlay, invalidToPlay, "OPTION_SELECTED", "\\d", 7000, "1000 XML " + FreeswitchContext.ivr_dp));
//                    tmsDialplan.addAction(new Set(FreeswitchVariables.option_selected_id, "${OPTION_SELECTED}"));
//                    tmsDialplan.addAction(new TMSOrder(IVROrder.SELECT_PAYMENT_TYPE));
//                    tmsDialplan.addBridge(new Transfer("1000 XML " + FreeswitchContext.ivr_dp));
//                    break;
  
                default:
                    sendToMainMenu(tmsDialplan);

            }
        }
        return tmsDialplan;
    }

    private void sentToCustomerServiceFiFo(DialplanVariable variable, TMSDialplan tmsDialplan) {
        AgentIncomingDistributionOrder aido = inboundCallService.inboundCallOrder(null, tmsDialplan.getCallerLong(), tmsDialplan.getCall_uuid());
        if (aido.getAgents().isEmpty()) {
            tmsDialplan.addAction(new Playback(RecordedPhrases.PLEASE_WAIT_FOR_NEXT_AGENT));
            tmsDialplan.addAction(new TMSOrder(HOLDOrder.PLACE_ON_HOLD_CUSTOMER_SERVICE));
            tmsDialplan.addBridge(new BridgeToFifo(FreeswitchContext.ivr_dp, freeswitchService.getFreeswitchIPNew(tmsDialplan.getCall_uuid(), FreeswitchContext.fifo_dp)));
        } else {
            IncomingDialerOrder builder = new IncomingDialerOrder(variable, aido);
            builder.setTMS_UUID(tmsDialplan.getKey().getTms_uuid());
            TMSDialplan connectToAgentDialplan = builder.buildDialplansWithoutSBC();
            tmsDialplan.addAction(new TMSOrder(connectToAgentDialplan.getKey().getOrderPower()));
            tmsDialplan.addBridge(new BridgeToAgent(agentService.getFreeswitchIPForExt(connectToAgentDialplan.getCalleeInteger()), connectToAgentDialplan.getCalleeInteger()));
        }
    }

    private void sendToMainMenu(TMSDialplan tmsDialplan) {
        tmsDialplan.addAction(new TMSOrder(IVROrder.MAIN_MENU));
        tmsDialplan.addBridge(new Transfer("1000 XML " + FreeswitchContext.ivr_dp));
    }

    public TMSDialplan SelectPaymentType(DialplanVariable variable, TMSDialplan tmsDialplan) {
        switch (variable.getOptionSelectedId()) {
            case 0:
                sentToCustomerServiceFiFo(variable, tmsDialplan);
                break;
            case 3:
                tmsDialplan.addAction(new TMSOrder(IVROrder.MAIN_MENU));
                tmsDialplan.addBridge(new Transfer("1000 XML " + FreeswitchContext.ivr_dp));
                break;
            default:
                sendToMainMenu(tmsDialplan);

        }
        return tmsDialplan;
    }

}
