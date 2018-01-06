/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.tms.freeswitch.premaid.incoming;

import com.amp.tms.db.entity.freeswitch.TMSDialplan;
import com.amp.tms.enumerated.CallDirection;
import com.amp.tms.enumerated.FreeswitchContext;
import com.amp.tms.enumerated.RecordedPhrases;
import com.amp.tms.enumerated.refrence.BeanServices;
import com.amp.tms.enumerated.refrence.HOLDOrder;
import com.amp.tms.freeswitch.FreeswitchVariables;
import com.amp.tms.freeswitch.dialplan.action.Answer;
import com.amp.tms.freeswitch.dialplan.action.BridgeToFifo;
import com.amp.tms.freeswitch.dialplan.action.Fifo;
import com.amp.tms.freeswitch.dialplan.action.Playback;
import com.amp.tms.freeswitch.dialplan.action.Set;
import com.amp.tms.freeswitch.dialplan.action.Sleep;
import com.amp.tms.freeswitch.dialplan.action.TMSOrder;
import com.amp.tms.freeswitch.pojo.AgentIncomingDistributionOrder;
import com.amp.tms.freeswitch.pojo.DialplanVariable;
import com.amp.tms.freeswitch.premaid.DialplanBuilder;
import com.amp.tms.service.freeswitch.FifoService;

/**
 *
 * @author hsleiman
 */
public class IncomingPlaceOnHold extends DialplanBuilder {

    private AgentIncomingDistributionOrder aido;

    public IncomingPlaceOnHold(DialplanVariable variable, AgentIncomingDistributionOrder aido) {
        super();
        this.aido = aido;
        setVariable(variable);
    }

    @Override
    public void createDialplans() {

    }

    @Override
    public void buildDialplans() {
        callEnteringFifo();
        callEnteringSBC();
        biuldVoicemailOption();
    }

    @Override
    public void saveDialplans() {

    }

    private void commonVariable(TMSDialplan tMSDialplan) {
        tMSDialplan.setTms_type(this.getClass().getSimpleName());
        tMSDialplan.setCall_uuid(tMSDialplan.getKey().getTms_uuid());
        tMSDialplan.setCallDirection(CallDirection.INBOUND);
        tMSDialplan.setAutoAswer(aido.getIsAutoAnswer());
        tMSDialplan.setPopupType(aido.getPopupDisplayMode());
        tMSDialplan.setIvrAuthorized(callDetailRecordService.getIVRAuthorized(TMS_UUID));

        tMSDialplan.setCaller(inVariables.getCallerIdNumber());
        tMSDialplan.setCallee(inVariables.getCalleeIdNumber());
        tMSDialplan.setOutboundVendor(Boolean.FALSE);
        tMSDialplan.setDialer(Boolean.FALSE);

        tMSDialplan.setBorrowerInfo(aido.getBorrowerInfo());

    }

    public TMSDialplan callEnteringFifo() {
        TMSDialplan fifoDialplan = dialplanService.createTMSDialplan(TMS_UUID, FreeswitchContext.fifo_dp, HOLDOrder.PLACE_ON_HOLD);
        commonVariable(fifoDialplan);
        fifoDialplan.setRecord(Boolean.FALSE);
        fifoDialplan.addAction(new Set("fifo_music", configuration.getFiFoHoldMusic()));
        fifoDialplan.addAction(new Answer());
        fifoDialplan.addAction(new Playback(RecordedPhrases.PLEASE_WAIT_FOR_NEXT_AGENT));
        fifoDialplan.setBean(BeanServices.FifoService);
        fifoDialplan.setFunctionCall(FifoService.placeCallOnHold);

        fifoDialplan.addAction(new TMSOrder(HOLDOrder.PLACE_IN_VOICEMAIL));
        fifoDialplan.addAction(new Set("fifo_orbit_exten=1000:" + configuration.getMaxCallOnHoldAllowed()));
        fifoDialplan.addAction(new Set("fifo_chime_list=" + RecordedPhrases.HOLD_ANNOUNCE_PRESS1.getAudioPath()));
        fifoDialplan.addAction(new Set("fifo_chime_freq=" + configuration.getMaxHoldAnnounceTimeInSec()));
        fifoDialplan.addAction(new Set("fifo_caller_exit_key=1"));
        fifoDialplan.addAction(new Set("fifo_caller_exit_to_orbit=true"));

        if (aido.getSettings() == null) {
            fifoDialplan.addBridge(new Fifo("InboundDialerQueue_default" + " in"));
        } else {
            fifoDialplan.setDialerQueueId(aido.getSettings().getDialerQueuePk());
            fifoDialplan.addBridge(new Fifo("InboundDialerQueue_" + aido.getSettings().getDialerQueuePk() + " in"));
        }
        fifoDialplan.setXMLFromDialplan();
        dialplanService.updateTMSDialplan(fifoDialplan);
        return fifoDialplan;
    }

    private void callEnteringSBC() {
        TMSDialplan sbcDialplan = dialplanService.createTMSDialplan(TMS_UUID, FreeswitchContext.sbc_dp);
        commonVariable(sbcDialplan);

        sbcDialplan.setRecord(Boolean.TRUE);
        sbcDialplan.setVariables(inVariables.toJson());
        sbcDialplan.addAction(Set.create(FreeswitchVariables.ringback, "${us-ring}"));
        sbcDialplan.addAction(new Answer());
        sbcDialplan.addAction(new Playback(RecordedPhrases.WELCOME_TO_CASHCALL_AUTO, configuration.getCompanyInfo()));
        sbcDialplan.addAction(new Sleep(900l));

        sbcDialplan.addAction(new TMSOrder(HOLDOrder.PLACE_ON_HOLD));
        sbcDialplan.addBridge(new BridgeToFifo(FreeswitchContext.sbc_dp, freeswitchService.getFreeswitchIPNew(sbcDialplan.getCall_uuid(), FreeswitchContext.fifo_dp)));

        sbcDialplan.setXMLFromDialplan();
        dialplanService.updateTMSDialplan(sbcDialplan);
        setReturnDialplan(sbcDialplan);
    }

    private void biuldVoicemailOption() {
        IncomingVoicemail builder = new IncomingVoicemail(inVariables, aido);
        builder.setTMS_UUID(TMS_UUID);
        builder.buildDialplansWithoutSBC();
    }

}
