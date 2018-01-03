/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.objectbrains.tms.freeswitch.premaid.incoming.ivr;

import com.objectbrains.svc.iws.CallerId;
import com.objectbrains.tms.db.entity.freeswitch.TMSDialplan;
import com.objectbrains.tms.enumerated.CallDirection;
import com.objectbrains.tms.enumerated.FreeswitchContext;
import com.objectbrains.tms.freeswitch.dialplan.action.TMSOrder;
import com.objectbrains.tms.freeswitch.dialplan.action.Transfer;
import com.objectbrains.tms.freeswitch.pojo.AgentIncomingDistributionOrder;
import com.objectbrains.tms.freeswitch.pojo.DialplanVariable;
import com.objectbrains.tms.freeswitch.premaid.DialplanBuilder;

/**
 *
 * @author hsleiman
 */
public class IVRIdentMod extends DialplanBuilder {

    private AgentIncomingDistributionOrder aido;
    private boolean isBorrowerKnown = true;

    private static String path = "";

    public IVRIdentMod(DialplanVariable variable, AgentIncomingDistributionOrder aido, boolean isBorrowerKnown) {
        log.info(aido.toJson());
        setVariable(variable);
        this.isBorrowerKnown = isBorrowerKnown;
        this.aido = aido;
    }

    @Override
    public void createDialplans() {
//        agentDialplan = dialplanRepository.createTMSDialplan(TMS_UUID, FreeswitchContext.AGENT_DIALPLAN);
//        agentDialplan.setTms_type(this.getClass().getSimpleName());

    }

    @Override
    public void buildDialplans() {
        entryPointForIVRIdentMod();
        iSeeYourPhoneNumber();
        forSecurityPurposesEnterLast4SSN();
        last4SSNCheck();
        checkSecurityForLoan();
    }

    public TMSDialplan buildDialplansAndReturn() {

        iSeeYourPhoneNumber();
        forSecurityPurposesEnterLast4SSN();
        last4SSNCheck();
        checkSecurityForLoan();

        return entryPointForIVRIdentMod();
    }

    @Override
    public void saveDialplans() {
    }

    private void commonVariable(TMSDialplan tMSDialplan) {
        tMSDialplan.setTms_type(this.getClass().getSimpleName());
        tMSDialplan.setDebugOn(getDebugOn());
        tMSDialplan.setTms_type(this.getClass().getSimpleName());
        tMSDialplan.setCallerId(CallerId.ACTUAL);
        tMSDialplan.setCall_uuid(tMSDialplan.getKey().getTms_uuid());
        tMSDialplan.setCallDirection(CallDirection.INBOUND);
        tMSDialplan.setDialer(Boolean.FALSE);
        tMSDialplan.setCallee(inVariables.getCalleeIdNumber());
        tMSDialplan.setCaller(inVariables.getCallerIdNumber());
        tMSDialplan.setBorrowerInfo(aido.getBorrowerInfo());

    }

    public static String entryPointForIVRIdentMod = "entryPointForIVRIdentMod";

    private TMSDialplan entryPointForIVRIdentMod() {
        TMSDialplan tmsDialplan;
        tmsDialplan = dialplanService.createTMSDialplan(TMS_UUID, FreeswitchContext.ivr_dp, entryPointForIVRIdentMod);

        if (isBorrowerKnown) {
            tmsDialplan.addAction(new TMSOrder(iSeeYourPhoneNumber));
        }
        else{
            tmsDialplan.addAction(new TMSOrder(iSeeYourPhoneNumber));
        }

        tmsDialplan.addBridge(new Transfer("1000 XML " + FreeswitchContext.ivr_dp));
        tmsDialplan.setXMLFromDialplan();
        dialplanService.updateTMSDialplan(tmsDialplan);

        return tmsDialplan;
    }

    public static String iSeeYourPhoneNumber = "iSeeYourPhoneNumber";

    private void iSeeYourPhoneNumber() {

    }

    public static String forSecurityPurposesEnterLast4SSN = "forSecurityPurposesEnterLast4SSN";

    private void forSecurityPurposesEnterLast4SSN() {

    }

    public static String last4SSNCheck = "last4SSNCheck";

    private void last4SSNCheck() {

    }

    public static String checkSecurityForLoan = "checkSecurityForLoan";

    private void checkSecurityForLoan() {

    }

}
