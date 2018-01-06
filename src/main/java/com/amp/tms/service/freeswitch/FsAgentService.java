/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.tms.service.freeswitch;

import com.objectbrains.ams.iws.User;
import com.amp.crm.db.entity.disposition.CallDispositionCode;
import com.amp.tms.constants.Constants;
import com.amp.tms.db.entity.cdr.CallDetailRecordTMS;
import com.amp.tms.db.entity.freeswitch.CDR;
import com.amp.tms.db.entity.freeswitch.TMSDialplan;
import com.amp.tms.db.repository.DialplanRepository;
import com.amp.tms.enumerated.CallDirection;
import com.amp.tms.enumerated.FreeswitchContext;
import com.amp.tms.enumerated.refrence.DDD;
import com.amp.tms.enumerated.refrence.HOLDOrder;
import com.amp.tms.freeswitch.FreeswitchVariables;
import com.amp.tms.freeswitch.dialplan.action.BridgeToFifo;
import com.amp.tms.freeswitch.dialplan.action.Set;
import com.amp.tms.freeswitch.dialplan.action.TMSOrder;
import com.amp.tms.freeswitch.pojo.DialplanVariable;
import com.amp.tms.freeswitch.pojo.DirectoryVariable;
import com.amp.tms.freeswitch.premaid.DialplanBuilder;
import com.amp.tms.freeswitch.premaid.incoming.HangupSimply;
import com.amp.tms.freeswitch.premaid.local.AgentToAgent;
import com.amp.tms.freeswitch.premaid.local.AgentToAgentTransfer;
import com.amp.tms.freeswitch.premaid.local.AgentToAgentTransferToP1;
import com.amp.tms.freeswitch.premaid.local.AgentToFifo;
import com.amp.tms.freeswitch.premaid.local.P2AgentToP1Agent;
import com.amp.tms.freeswitch.premaid.outbound.ManualDial;
import com.amp.tms.hazelcast.entity.AgentCall;
import com.amp.tms.service.AgentCallService;
import com.amp.tms.service.TMSAgentService;
import com.amp.tms.service.AmsService;
import com.amp.tms.service.CallDetailRecordService;
import com.amp.tms.service.CdrService;
import com.amp.tms.service.DialplanService;
import com.amp.tms.service.DispositionCodeService;
import com.amp.tms.service.FreeswitchConfiguration;
import com.amp.tms.service.FreeswitchService;
import com.amp.tms.service.InboundCallService;
import com.amp.tms.service.TransferService;
import com.amp.tms.service.freeswitch.common.CommonService;
import com.amp.tms.websocket.Websocket;
import java.util.Objects;
import javax.servlet.http.HttpServletRequest;
import org.joda.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

/**
 *
 * @author hsleiman
 */
@Service("FsAgentService")
public class FsAgentService {

    private static final Logger log = LoggerFactory.getLogger(FsAgentService.class);

    @Autowired
    private CdrService cdrRepository;

    @Autowired
    private TMSAgentService agentService;

    @Autowired
    private DialplanService dialplanService;

    @Autowired
    private FreeswitchConfiguration configuration;

    @Autowired
    private DialplanRepository dialplanRepository;

    @Autowired
    private CommonService commonService;

    @Autowired
    private AmsService amsService;

    @Autowired
    private AgentCallService agentCallService;

    @Autowired
    private CallingOutService callingOutService;

    @Autowired
    private DispositionCodeService dispositionCodeService;

    @Autowired
    private CallDetailRecordService callDetailRecordService;

    @Autowired
    private InboundCallService inboundCallService;

    @Autowired
    private FreeswitchService freeswitchService;

    @Autowired
    @Lazy
    private Websocket websocket;

    @Autowired
    private TransferService transferService;

    public String DirectoryLookup(DirectoryVariable variable) {

        int ext = 1000;
        try {
            ext = Integer.parseInt(variable.getUser());
        } catch (NumberFormatException ex) {
            log.error("NumberFormatException {} to [{}][{}]", variable.getUser(), variable.getDomain(), variable.getFreeSWITCH_IPv4());
        }
        User user = null;
        if (ext != 1000) {
            log.info("Setting domain for agent {} to [{}][{}]", ext, variable.getDomain(), variable.getFreeSWITCH_IPv4());
            user = amsService.getUser(ext);
            agentService.setFreeswitchIpAndDomain(ext, variable.getFreeSWITCH_IPv4(), variable.getDomain(), user, variable.getUserIP());
        }
        String xml = null;
        if (configuration.useNewFreeswitchDirectoryLookUp()) {
            log.info("Using new way to look up Directory for: {}", variable.getUser());
            StringBuilder sb = new StringBuilder();
            sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>");
            sb.append("\n");
            sb.append("<document type=\"freeswitch/xml\">");
            sb.append("\n");
            sb.append("<section name=\"directory\" description=\"Directory lookup For FreeSwitch\">");
            sb.append("\n");
            sb.append("<domain name=\"").append(variable.getDomain()).append("\">");
            sb.append("\n");
            sb.append("  <user id=\"").append(variable.getUser()).append("\">");
            sb.append("\n");
            sb.append("    <params>");
            sb.append("\n");
            if (user == null) {
                sb.append("<param name=\"password\" value=\"").append(variable.getUser()).append("\"/> <!--SIP password-->");
                sb.append("\n");
                sb.append("<param name=\"vm-password\" value=\"").append(variable.getUser()).append("\"/>");
                sb.append("\n");

            } else {
                sb.append("<param name=\"password\" value=\"").append(user.getExtensionAuthToken()).append("\"/> <!--SIP password-->");
                sb.append("\n");
                sb.append("<param name=\"vm-password\" value=\"").append(user.getVoiceMailPassword()).append("\"/>");
                sb.append("\n");
            }

            if (configuration.useNewFreeswitchDirectoryLookUpAllowEmptyPassword()) {
                sb.append("<param name=\"allow-empty-password\" value=\"false\"/>");
                sb.append("\n");
            }

            sb.append("<param name=\"vm-storage-dir\" value=\"" + Constants.FREESWITCH_RECORDING_LOCATION).append(variable.getUser()).append("\"/> <!--SIP password-->");
            sb.append("\n");
            if (user != null) {
                sb.append("<param name=\"vm-email-all-messages\" value=\"true\" />");
                sb.append("\n");
                sb.append("<param name=\"vm-mailto\" value=\"").append(user.getEmailAddress()).append("\" />");
                sb.append("\n");
                sb.append("<param name=\"vm-mailfrom\" value=\"").append(user.getEmailAddress()).append("\"/>");
                sb.append("\n");
                sb.append("<param name=\"vm-keep-local-after-email\" value=\"false\"/>");
                sb.append("\n");
                sb.append("<param name=\"vm-attach-file\" value=\"true\" />");
                sb.append("\n");
            }
            sb.append("</params>");
            sb.append("\n");
            sb.append("<variables><!--these variable are accessible in the channel-->");
            sb.append("\n");
            sb.append("<variable name=\"accountcode\" value=\"").append(variable.getUser()).append("\"/> <!--Use this in your dialplan for authorization and limits. Also, cdr_csv can use it for separate CDR files-->");
            sb.append("\n");
            sb.append("<variable name=\"user_context\" value=\"agent_dp\"/> <!--magic variable: specifies the context-->");
            sb.append("\n");
            sb.append("<variable name=\"effective_caller_id_name\" value=\"Extension ").append(variable.getUser()).append("\"/><!--magic variable: used for outbound caller ID name-->");
            sb.append("\n");
            sb.append("<variable name=\"effective_caller_id_number\" value=\"").append(variable.getUser()).append("\"/><!--magic variable: used for outbound caller ID name/number-->");
            sb.append("\n");
            sb.append("</variables>");
            sb.append("\n");
            sb.append("</user>");
            sb.append("\n");
            sb.append("</domain>");
            sb.append("\n");
            sb.append("</section>");
            sb.append("\n");
            sb.append("</document>");
            xml = sb.toString();
        } else {
            log.info("Using OLD way to look up Directory for: {}", variable.getUser());
            xml = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n"
                    + "<document type=\"freeswitch/xml\">\n"
                    + "<section name=\"directory\" description=\"RE Dial Plan For FreeSwitch\">\n"
                    + "<domain name=\"" + variable.getDomain() + "\">"
                    + "  <user id=\"" + variable.getUser() + "\">\n"
                    + "    <params>\n";
            if (user == null) {
                xml = xml + "    <param name=\"password\" value=\"" + variable.getUser() + "\"/> <!--SIP password-->\n"
                        + "      <param name=\"vm-password\" value=\"" + variable.getUser() + "\"/>\n";
            } else {
                xml = xml + "    <param name=\"password\" value=\"" + user.getExtensionAuthToken() + "\"/> <!--SIP password-->\n"
                        + "      <param name=\"vm-password\" value=\"" + user.getVoiceMailPassword() + "\"/>\n";
            }
            xml = xml + "    <param name=\"vm-storage-dir\" value=\"" + Constants.FREESWITCH_RECORDING_LOCATION + variable.getUser() + "\"/> <!--SIP password-->\n";
            if (user != null) {
                xml = xml + "      <param name=\"vm-email-all-messages\" value=\"true\" />\n"
                        + "        <param name=\"vm-mailto\" value=\"" + user.getEmailAddress() + "\" />\n"
                        + "        <param name=\"vm-mailfrom\" value=\"" + user.getEmailAddress() + "\"/>\n"
                        + "        <param name=\"vm-keep-local-after-email\" value=\"false\"/>\n"
                        + "        <param name=\"vm-attach-file\" value=\"true\" />\n";
            }

            xml = xml + "    </params>\n"
                    + "    <variables><!--these variable are accessible in the channel-->\n"
                    + "      <variable name=\"accountcode\" value=\"" + variable.getUser() + "\"/> <!--Use this in your dialplan for authorization and limits. Also, cdr_csv can use it for separate CDR files-->\n"
                    + "      <variable name=\"user_context\" value=\"agent_dp\"/> <!--magic variable: specifies the context-->\n"
                    + "      <variable name=\"effective_caller_id_name\" value=\"Extension " + variable.getUser() + "\"/><!--magic variable: used for outbound caller ID name-->\n"
                    + "      <variable name=\"effective_caller_id_number\" value=\"" + variable.getUser() + "\"/><!--magic variable: used for outbound caller ID name/number-->\n"
                    + "    </variables>\n"
                    + "  </user>\n"
                    + "</domain>"
                    + "</section>\n"
                    + "</document>";

        }
        log.debug("User XML {} - {}", ext, xml);
        return xml;
    }

    public String DialLookup(DialplanVariable variable) {
        boolean bypass = true;

        DialplanBuilder dialplan = null;
        if (variable.getCallerDestinationNumber().startsWith("1010_")) {
            log.info("TMS P2 Agent to Fifo.");
            dialplan = new AgentToFifo(variable);
            dialplan.setTMS_UUID(variable.getCall_uuid());
            return dialplanRepository.LogDialplan(variable, dialplan.getDialplan());
        }

        if (variable.getTmsOrder().equalsIgnoreCase(DDD.CONNECT_TO_AGENT.name())) {
            bypass = false;
        }
        if (variable.getTmsOrder().equalsIgnoreCase(DDD.P1_TO_P2_CONNECT_TO_AGENT.name())) {
            bypass = false;
        } else if (variable.getTmsTransfer() != null && Objects.equals(variable.getTmsTransfer(), Boolean.FALSE)) {
            bypass = false;
        } else if (variable.getTmsTransfer() != null && Objects.equals(variable.getTmsTransfer(), Boolean.TRUE)) {
            bypass = true;
        } else if (variable.getCallerTransferSource() == null) {
            bypass = false;
        }
        if (bypass == false) {
            if (variable.getTmsDP()) {
                TMSDialplan dp = dialplanService.getPremaidDialplan(variable);
                if (dp != null) {
                    log.info("dp.getFunctionCall(): {}", dp.getFunctionCall());
                    if (dp.getFunctionCall() == null || dp.getFunctionCall().equalsIgnoreCase(startCallForAgentCallee) == false) {
                        for (int i = 0; i < 100; i++) {
                            log.info("Skipping agentService.updateAgentOnCall(dp)");
                        }
                        agentService.updateAgentOnCall(dp);
                    }
                    log.info("TMS pre-defined dialplan found: {} @ {} with {}", variable.getTmsUUID(), variable.getContext(), variable.getTmsOrder());
                    dp = commonService.executeTMSFunction(dp, variable);
                    return dialplanRepository.LogDialplan(variable, dp);
                }
            }
        }
        log.info("TMS pre-defined dialplan not found: " + variable.getTmsDP());

        String destinationNumber = variable.getCalleeIdNumber();
        if (variable.getCallerTransferSource() != null && destinationNumber != null && destinationNumber.length() == "221711".length() && destinationNumber.startsWith("21")) {
            log.info("TMS P2 Agent to P1 Agent Transfer.");
            dialplan = new AgentToAgentTransferToP1(variable);
        } else if (destinationNumber != null && destinationNumber.length() == "221711".length() && destinationNumber.startsWith("21")) {
            log.info("TMS P2 Agent to P1 Agent.");
            dialplan = new P2AgentToP1Agent(variable);
        } else if (dialplanService.isAgentToAgentCall(variable)) {
            log.info("TMS Agent to Agent.");
            dialplan = new AgentToAgent(variable);
            String calluuid = transferService.getAgentTransferMapCallUUID(variable.getCallerIdNumber() + "+" + variable.getCalleeIdNumber());
            log.info("TMS Agent to Agent. Found call uuid to use {}", calluuid);
            if (calluuid != null) {
                log.info("TMS Agent to Agent. Setting TMS_UUID to {}", calluuid);
                dialplan.setTMS_UUID(calluuid);
                return dialplanRepository.LogDialplan(variable, dialplan.getDialplan());
            }
            calluuid = variable.getCall_uuid();
            if (calluuid != null) {
                log.info("TMS Agent to Agent. Setting TMS_UUID to xxxxxxxxxxxxxxxxxxxxx{}", calluuid);
                dialplan.setTMS_UUID(calluuid);
                return dialplanRepository.LogDialplan(variable, dialplan.getDialplan());
            }

        } else if (variable.getCallerTransferSource() != null) {

            log.info("TMS Agent to Agent Transfer.");
            dialplan = new AgentToAgentTransfer(variable);
            startCallForAgentCalleeTransfer(variable, dialplan.getDialplan());

        } else if (dialplanService.isOutbound(variable)) {
            log.info("TMS Agent Manual.");
            dialplan = new ManualDial(variable);
            String calluuid = variable.getCall_uuid();
            if (calluuid != null) {
                dialplan.setTMS_UUID(calluuid);
            }
        } else {

            for (int i = 0; i < 1000; i++) {
                log.warn("Should never happen ");
            }
            dialplan = new AgentToAgent(variable);
        }
        TMSDialplan tmsDialplan = dialplan.getDialplan();
        if (configuration.enableFreeswitchSwitchFSAgentServiceAgentCallUpdate()) {
            log.info("Infomation {}, {}", tmsDialplan.getCalleeInteger(), tmsDialplan);
            Integer calleeExt = getFormatedExt(tmsDialplan.getCallee());
            if (calleeExt != null) {
                Boolean callee = agentService.updateAgentOnCall(calleeExt, tmsDialplan);
                if (callee != null && callee == false) {
                    log.info("Callee Should hangup the call {}, {}", calleeExt, tmsDialplan.getCall_uuid());
                    if (configuration.enableFreeswitchSwitchFSAgentServiceAgentCallUpdate1()) {
                        tmsDialplan = (new HangupSimply(variable)).getDialplan();
                    } else {
                        AgentCall agentCall = agentCallService.getAgentCall(calleeExt, tmsDialplan.getCall_uuid());
                        if (agentCall != null) {
                            log.info("Adding Freeswicth Channal UUID to Agent Call Ext: {}, CallUUID: {}, FSUUID: {}", tmsDialplan.getCalleeInteger(), tmsDialplan.getCall_uuid(), variable.getUniqueID());
                            agentCallService.setAgentFreeswitchUUID(calleeExt, tmsDialplan.getCall_uuid(), variable.getUniqueID());
                        }
                    }

                    log.info("Information tmsdialplan is null {}", tmsDialplan == null);
                }
            }
            log.info("Information Caller {} Callee {}, callerExt {}", tmsDialplan.getCaller(), tmsDialplan.getCallee(), getFormatedExt(tmsDialplan.getCaller()));
            Integer callerExt = getFormatedExt(tmsDialplan.getCaller());
            if (callerExt != null) {
                Boolean caller = agentService.updateAgentOnCall(callerExt, tmsDialplan);
                if (caller != null && caller == false) {
                    log.info("Caller Should hangup the call {}, {}", callerExt, tmsDialplan.getCall_uuid());
                    if (configuration.enableFreeswitchSwitchFSAgentServiceAgentCallUpdate2()) {
                        tmsDialplan = (new HangupSimply(variable)).getDialplan();
                    } else {
                        AgentCall agentCall = agentCallService.getAgentCall(callerExt, tmsDialplan.getCall_uuid());
                        if (agentCall != null) {
                            log.info("Adding Freeswicth Channal UUID to Agent Call Ext: {}, CallUUID: {}, FSUUID: {}", tmsDialplan.getCalleeInteger(), tmsDialplan.getCall_uuid(), variable.getUniqueID());
                            agentCallService.setAgentFreeswitchUUID(callerExt, tmsDialplan.getCall_uuid(), variable.getUniqueID());
                        }
                    }
                }
            }
        }

//        agentService.updateAgentOnCall(tmsDialplan);
        return dialplanRepository.LogDialplan(variable, tmsDialplan);
    }

    private Integer getFormatedExt(String potentialExtension) {
        if (potentialExtension == null || potentialExtension.length() != 4) {
            return null;
        }
        try {
            return Integer.parseInt(potentialExtension);
        } catch (NumberFormatException ex) {
            //Do nothing
        }
        return null;
    }

    public void CDRDump(HttpServletRequest request, CDR cdr) {
        cdrRepository.storeCDR(cdr);
        agentService.updateAgentOffCall(cdr);

        TMSDialplan tmsDialplan = null;
        if (cdr.getTms_uuid() != null && cdr.getTms_uuid().equalsIgnoreCase("") == false) {
            tmsDialplan = dialplanService.getTMSDialplanForCDR(cdr.getTms_uuid(), cdr.getContext(), cdr.getOrderPower());
        }
        if (tmsDialplan == null) {
            return;
        }

        CallDetailRecordTMS mcdr = callDetailRecordService.getCDR(tmsDialplan.getCall_uuid());
        callDetailRecordService.uploadRecording(cdr.getCall_uuid(), cdr.getSip_local_network_addr(), cdr.getRecrodingUploadTms(), -1l);

        if (mcdr.getDialer() && (cdr.getBridge_hangup_cause() == null || (cdr.getBridge_hangup_cause() != null && cdr.getBridge_hangup_cause().equalsIgnoreCase("NORMAL_CLEARING") == false))) {
            CallDispositionCode code = dispositionCodeService.callDroppedFromDialer();
//            mcdr.setAmdDroppedBeforeAgentAnswerTimestamp(cdr.getEndTime());
//            mcdr.setAmdDroppedBeforeAgentAnswer(true);
            Duration duration = new Duration(cdr.getStartTime().toDateTime(), cdr.getEndTime().toDateTime());
            if (mcdr.getAmdStartTime() != null) {
                duration = new Duration(mcdr.getAmdStartTime().toDateTime(), cdr.getEndTime().toDateTime());
            }
            callingOutService.callDropped(mcdr.getCall_uuid(), duration.getMillis(), code);
        }

        if (tmsDialplan.getCallDirection() == CallDirection.OUTBOUND && tmsDialplan.getDialer()) {
            Integer destination_number = 0;
            try {
                destination_number = Integer.parseInt(cdr.getDestination_number());
            } catch (Exception ex) {
                destination_number = 0;
            }
            if (destination_number != 0) {
                agentService.updateAgentLastHangupCause(destination_number, cdr.getLast_bridge_hangup_cause());
            }
        } else if (tmsDialplan.getCallDirection() == CallDirection.OUTBOUND) {
            Integer destination_number = 0;
            try {
                destination_number = Integer.parseInt(cdr.getCaller_id_number());
            } catch (Exception ex) {
                destination_number = 0;
            }
            if (destination_number != 0) {
                agentService.updateAgentLastHangupCause(destination_number, cdr.getLast_bridge_hangup_cause());
            }
        } else if (tmsDialplan.getCallDirection() == CallDirection.INBOUND) {
            Integer destination_number = 0;
            try {
                destination_number = Integer.parseInt(cdr.getCallee_id_number());
            } catch (Exception ex) {
                destination_number = 0;
            }
            if (destination_number != 0) {
                agentService.updateAgentLastHangupCause(destination_number, cdr.getLast_bridge_hangup_cause());
            }
        }

    }

    public TMSDialplan printstuff(DialplanVariable variable, TMSDialplan dp) {
        log.info("YES IT wORKS.");
        return dp;
    }

    public static final String startCallForAgentCalleeTransfer = "startCallForAgentCalleeTransfer";

    public TMSDialplan startCallForAgentCalleeTransfer(DialplanVariable variable, TMSDialplan tmsDialplan) {
        String internalCallUUID = null;

        try {
            internalCallUUID = transferService.getTransferCallUUIDForOriginalCallUUID(tmsDialplan.getCall_uuid());
            if (internalCallUUID != null) {
                log.info("Starting call for Agent for Transfer {} - {} - {}", tmsDialplan.getCalleeInteger(), tmsDialplan.getCall_uuid(), internalCallUUID);

                agentCallService.callStarted(tmsDialplan.getCalleeInteger(), tmsDialplan.getCall_uuid(), null, false, tmsDialplan.getBorrowerInfo(), tmsDialplan.getCallDirection(), tmsDialplan.getDialerQueueId(), variable.getChannelCallUUID(), variable.getDialer(), true, 30);
                agentCallService.callTransfered(tmsDialplan.getCalleeInteger(), internalCallUUID);
            } else {
                log.info("Starting call for Agent for Transfer could not find internal call uuid{} - {} - {}", tmsDialplan.getCalleeInteger(), tmsDialplan.getCall_uuid(), internalCallUUID);
            }
        } catch (Exception ex) {
            log.info("Starting call for Agent for Transfer could not find internal call uuid{} - {} - {}", tmsDialplan.getCalleeInteger(), tmsDialplan.getCall_uuid(), internalCallUUID);
        }

        return tmsDialplan;
    }

    public static final String startCallForAgentCallee = "startCallForAgentCallee";

    public TMSDialplan startCallForAgentCallee(DialplanVariable variable, TMSDialplan tmsDialplan) {
        boolean ignoreWrap = tmsDialplan.getIgnore_disposition() != null && tmsDialplan.getIgnore_disposition();

        if (configuration.enableFreeswitchSwitchFSAgentServiceCanRecieveCallCheck()) {
            boolean shouldRecieveCall = inboundCallService.shouldRecieveCall(tmsDialplan.getCalleeInteger(), tmsDialplan.getAgentInline(), tmsDialplan.getCallDirection(), tmsDialplan.getDialer());
            log.info("Should Recieve Call for agent Callee {}, {}, shouldRecieveCall: {}", tmsDialplan.getCalleeInteger(), tmsDialplan.getCall_uuid(), shouldRecieveCall);
            if (shouldRecieveCall == false) {
                log.info("Should Recieve Call for agent Callee {}, {}, shouldRecieveCall: {}", tmsDialplan.getCalleeInteger(), tmsDialplan.getCall_uuid(), false);
                try {
                    Integer order = Integer.parseInt(tmsDialplan.getKey().getOrderPower());
                    order++;
                    TMSDialplan tmsDialplanNew = dialplanService.findTMSDialplan(tmsDialplan.getCall_uuid(), tmsDialplan.getKey().getContext(), order + "");

                    log.info("Should Recieve Call for agent Callee {}, {}, tmsDialplanNew: {}", tmsDialplan.getCalleeInteger(), tmsDialplan.getCall_uuid(), tmsDialplanNew);

                    if (tmsDialplanNew != null) {
                        log.info("Should Recieve Call for agent Callee {}, {}, tmsDialplanNew: {} calling [startCallForAgentCallee] once more", tmsDialplan.getCalleeInteger(), tmsDialplan.getCall_uuid(), tmsDialplanNew);
                        return startCallForAgentCallee(variable, tmsDialplanNew);
                    } else {
                        tmsDialplan.setActions("");
                        tmsDialplan.setBridges("");
                        tmsDialplan.addAction(new TMSOrder(HOLDOrder.PLACE_ON_HOLD));
                        tmsDialplan.addAction(Set.create(FreeswitchVariables.ringback, "${us-ring}"));
                        tmsDialplan.addBridge(new BridgeToFifo(FreeswitchContext.agent_dp, freeswitchService.getFreeswitchIPNew(tmsDialplan.getCall_uuid(), FreeswitchContext.fifo_dp)));
                        return tmsDialplan;
                    }
                } catch (NumberFormatException ex) {
                    log.error("Could not start call for {} this was due to number format exception {}", tmsDialplan.getCall_uuid(), ex.getMessage());
                }
            }
        }

        boolean value = agentCallService.callStarted(tmsDialplan.getCalleeInteger(), tmsDialplan.getCall_uuid(), null, ignoreWrap, tmsDialplan.getBorrowerInfo(), tmsDialplan.getCallDirection(), tmsDialplan.getDialerQueueId(), variable.getUniqueID(), tmsDialplan.getDialer(), tmsDialplan.getMaxDelayBeforeAgentAnswer());
        log.info("Started Call for agent Callee {}, {}, {}", tmsDialplan.getCalleeInteger(), tmsDialplan.getCall_uuid(), value);
        AgentCall agentCall = agentCallService.getAgentCall(tmsDialplan.getCalleeInteger(), tmsDialplan.getCall_uuid());

        if (value == false) {
            if (agentCall == null) {
                log.info("Started Call for agent Callee {}, {}, agentCallService.getAgentCall: null", tmsDialplan.getCalleeInteger(), tmsDialplan.getCall_uuid());
                try {
                    Integer order = Integer.parseInt(tmsDialplan.getKey().getOrderPower());
                    order++;
                    TMSDialplan tmsDialplanNew = dialplanService.findTMSDialplan(tmsDialplan.getCall_uuid(), tmsDialplan.getKey().getContext(), order + "");

                    log.info("Started Call for agent Callee {}, {}, tmsDialplanNew: {}", tmsDialplan.getCalleeInteger(), tmsDialplan.getCall_uuid(), tmsDialplanNew);

                    if (tmsDialplanNew != null) {
                        log.info("Started Call for agent Callee {}, {}, tmsDialplanNew: {} calling [startCallForAgentCallee] once more", tmsDialplan.getCalleeInteger(), tmsDialplan.getCall_uuid(), tmsDialplanNew);
                        return startCallForAgentCallee(variable, tmsDialplanNew);
                    } else {
                        tmsDialplan.setActions("");
                        tmsDialplan.setBridges("");
                        tmsDialplan.addAction(new TMSOrder(HOLDOrder.PLACE_ON_HOLD));
                        tmsDialplan.addAction(Set.create(FreeswitchVariables.ringback, "${us-ring}"));
                        tmsDialplan.addBridge(new BridgeToFifo(FreeswitchContext.agent_dp, freeswitchService.getFreeswitchIPNew(tmsDialplan.getCall_uuid(), FreeswitchContext.fifo_dp)));
                        return tmsDialplan;
                    }
                } catch (NumberFormatException ex) {
                    log.error("Could not start call for {} this was due to number format exception {}", tmsDialplan.getCall_uuid(), ex.getMessage());
                }

            } else {
                log.info("Adding Freeswicth Channal UUID to Agent Call Ext: {}, CallUUID: {}, FSUUID: {}", tmsDialplan.getCalleeInteger(), tmsDialplan.getCall_uuid(), variable.getUniqueID());
                agentCallService.setAgentFreeswitchUUID(tmsDialplan.getCalleeInteger(), tmsDialplan.getCall_uuid(), variable.getUniqueID());
            }
        }

        if (value == true && websocket.checkAgentExt(tmsDialplan.getCalleeInteger()) == false) {
            //end call because earler we started teh calls 
            agentCallService.callEnded(tmsDialplan.getCalleeInteger(), tmsDialplan.getCall_uuid());
            //end call because earler we started teh calls 

            log.info("Ended Call for agent Callee {}, {}, websocket.checkAgentExt: false", tmsDialplan.getCalleeInteger(), tmsDialplan.getCall_uuid());

            try {
                Integer order = Integer.parseInt(tmsDialplan.getKey().getOrderPower());
                order++;
                TMSDialplan tmsDialplanNew = dialplanService.findTMSDialplan(tmsDialplan.getCall_uuid(), tmsDialplan.getKey().getContext(), order + "");
                log.info("Started Call for agent Callee {}, {}, tmsDialplanNew: {}", tmsDialplan.getCalleeInteger(), tmsDialplan.getCall_uuid(), tmsDialplanNew);

                if (tmsDialplanNew != null) {
                    log.info("Started Call for agent Callee {}, {}, tmsDialplanNew: {} calling [startCallForAgentCallee] once more", tmsDialplan.getCalleeInteger(), tmsDialplan.getCall_uuid(), tmsDialplanNew);
                    return startCallForAgentCallee(variable, tmsDialplanNew);
                } else {
                    tmsDialplan.setActions("");
                    tmsDialplan.setBridges("");
                    tmsDialplan.addAction(new TMSOrder(HOLDOrder.PLACE_ON_HOLD));
                    tmsDialplan.addAction(Set.create(FreeswitchVariables.ringback, "${us-ring}"));
                    tmsDialplan.addBridge(new BridgeToFifo(FreeswitchContext.agent_dp, freeswitchService.getFreeswitchIPNew(tmsDialplan.getCall_uuid(), FreeswitchContext.fifo_dp)));
                    return tmsDialplan;
                }
            } catch (NumberFormatException ex) {
                log.error("Could not start call for {} this was due to number format exception {}", tmsDialplan.getCall_uuid(), ex.getMessage());
            }
        }

        return tmsDialplan;
    }

    public static final String startCallForAgentCaller = "startCallForAgentCaller";

    public TMSDialplan startCallForAgentCaller(DialplanVariable variable, TMSDialplan tmsDialplan) {
        boolean ignoreWrap = tmsDialplan.getIgnore_disposition() != null && tmsDialplan.getIgnore_disposition();
        agentCallService.callStarted(tmsDialplan.getCallerInteger(), tmsDialplan.getCall_uuid(), null, ignoreWrap, tmsDialplan.getBorrowerInfo(), tmsDialplan.getCallDirection(), tmsDialplan.getDialerQueueId(), variable.getUniqueID(), tmsDialplan.getDialer(), tmsDialplan.getMaxDelayBeforeAgentAnswer());
        return tmsDialplan;
    }

    public static final String startCallForAgentBoth = "startCallForAgentBoth";

    public TMSDialplan startCallForAgentBoth(DialplanVariable variable, TMSDialplan tmsDialplan) {
        startCallForAgentCaller(variable, tmsDialplan);
        startCallForAgentCallee(variable, tmsDialplan);
        return tmsDialplan;
    }

}
