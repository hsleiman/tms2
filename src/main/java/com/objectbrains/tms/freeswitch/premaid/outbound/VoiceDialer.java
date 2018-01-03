/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.objectbrains.tms.freeswitch.premaid.outbound;

import com.objectbrains.tms.db.entity.freeswitch.TMSDialplan;
import com.objectbrains.tms.enumerated.CallDirection;
import com.objectbrains.tms.enumerated.FreeswitchContext;
import com.objectbrains.tms.freeswitch.FreeswitchVariables;
import com.objectbrains.tms.freeswitch.pojo.DialerInfoPojo;
import com.objectbrains.tms.freeswitch.premaid.DialplanBuilder;

/**
 *
 * @author hsleiman
 */
public class VoiceDialer extends DialplanBuilder {

    private TMSDialplan sbcDialplan;
    private DialerInfoPojo dialerInfoPojo;
    public VoiceDialer(DialerInfoPojo dialerInfoPojo) {
        super();
        this.dialerInfoPojo = dialerInfoPojo;
    }

    @Override
    public void createDialplans() {
        log.info("Create Dialplans");
        sbcDialplan = dialplanService.createTMSDialplan(TMS_UUID, FreeswitchContext.sbc_dp);
        sbcDialplan.setTms_type(this.getClass().getSimpleName());
    }

    @Override
    public void buildDialplans() {

        sbcDialplan.setCall_uuid(sbcDialplan.getKey().getTms_uuid());
        sbcDialplan.setRecord(Boolean.TRUE);
        sbcDialplan.setCallDirection(CallDirection.OUTBOUND);
        sbcDialplan.setCallee(dialerInfoPojo.getPhoneToTypeSingle().getPhoneNumber() + "");
        sbcDialplan.setCaller(dialerInfoPojo.getAgentExt() + "");
        sbcDialplan.setOutboundVendor(Boolean.TRUE);
        sbcDialplan.setCallerId(dialerInfoPojo.getSettings().getCallerId());

        StringBuilder originateBuilder = new StringBuilder();
        originateBuilder.append("{");
        originateBuilder.append(FreeswitchVariables.tms_uuid).append("=").append(TMS_UUID);
        originateBuilder.append(",");
        originateBuilder.append(FreeswitchVariables.is_tms_dp).append("=").append(Boolean.TRUE);
        originateBuilder.append(",");
        originateBuilder.append(FreeswitchVariables.is_dialer).append("=").append(Boolean.TRUE);
        originateBuilder.append(",");
        originateBuilder.append(FreeswitchVariables.dialer_queue_id).append("=").append(dialerInfoPojo.getSettings().getDialerQueuePk());
        originateBuilder.append(",");
        originateBuilder.append(FreeswitchVariables.origination_caller_id_name).append("=").append(dialerInfoPojo.getPhoneToTypeSingle().getFirstName()).append("_").append(dialerInfoPojo.getPhoneToTypeSingle().getLastName());
        originateBuilder.append(",");
        originateBuilder.append(FreeswitchVariables.origination_caller_id_number).append("=").append(dialerInfoPojo.getPhoneToTypeSingle().getPhoneNumber());
        originateBuilder.append("}");
        originateBuilder.append("sofia/sbc/sip:").append(dialerInfoPojo.getPhoneToTypeSingle().getPhoneNumber()).append("@").append(configuration.getFreeswitchIP(FreeswitchContext.sbc_dp)).append(":5046 ").append(dialerInfoPojo.getAgentExt()).append(" XML agent_dp");

        sbcDialplan.setOriginate(originateBuilder.toString());
    }

    @Override
    public void saveDialplans() {
        log.info("Saving " + sbcDialplan.getTms_type() + " Dialplan...");

        sbcDialplan.setXMLFromDialplan();
        dialplanService.updateTMSDialplan(sbcDialplan);

    }
}
