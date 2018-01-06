/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.tms.service.freeswitch;

/**
 *
 * @author hsleiman
 */
import com.amp.crm.db.entity.disposition.CallDispositionCode;
import com.amp.tms.db.entity.cdr.CallDetailRecordTMS;
import com.amp.tms.db.entity.freeswitch.CDR;
import com.amp.tms.db.entity.freeswitch.TMSDialplan;
import com.amp.tms.db.repository.DialplanRepository;
import com.amp.tms.enumerated.CallDirection;
import com.amp.tms.enumerated.refrence.DDD;
import com.amp.tms.exception.DialplanNotFoundException;
import com.amp.tms.freeswitch.pojo.AgentIncomingDistributionOrder;
import com.amp.tms.freeswitch.pojo.ConfigurationVariable;
import com.amp.tms.freeswitch.pojo.DialplanVariable;
import com.amp.tms.hazelcast.entity.WaitingCall;
import com.amp.tms.pojo.BorrowerInfo;
import com.amp.tms.service.CallDetailRecordService;
import com.amp.tms.service.CdrService;
import com.amp.tms.service.DialplanService;
import com.amp.tms.service.DispositionCodeService;
import com.amp.tms.service.FreeswitchConfiguration;
import com.amp.tms.service.InboundCallService;
import com.amp.tms.service.TransferService;
import com.amp.tms.service.dialer.CallService;
import com.amp.tms.service.freeswitch.common.CommonService;
import com.amp.tms.websocket.message.outbound.PhoneToType;
import javax.servlet.http.HttpServletRequest;
import org.joda.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author hsleiman
 */
@Service("FifoService")
public class FifoService {

    private static final Logger log = LoggerFactory.getLogger(FifoService.class);

    @Autowired
    private CallService callService;

    @Autowired
    private CdrService cdrRepository;

    @Autowired
    private DialplanService dialplanService;

    @Autowired
    private DialplanRepository dialplanRepository;

    @Autowired
    private CommonService commonService;

    @Autowired
    private InboundCallService inboundCallService;

    @Autowired
    private CallingOutService callingOutService;

    @Autowired
    private DispositionCodeService dispositionCodeService;

    @Autowired
    private CallDetailRecordService callDetailRecordService;
    
    @Autowired
    private FreeswitchConfiguration configuration;
    
    @Autowired
    private TransferService transferService;

    public String Configuration(ConfigurationVariable variable) {
        log.info("-------------------------");
        log.info("-------------------------");
        log.info("Start fifo Configuration : " + variable.getKey_value() + " / ");
        log.info("user/1005@$${domain}</member>-->\n" + " You need fill in");
        log.info("-------------------------");

        String xml = ("<document type=\"freeswitch/xml\">\n"
                + " <section name=\"configuration\">\n"
                + "<configuration name=\"fifo.conf\" description=\"FIFO Configuration\">\n"
                + " <settings>\n"
                + "   <param name=\"delete-all-outbound-member-on-startup\" value=\"false\"/>\n"
                + "   <param name=\"odbc-dsn\" value=\"pgsql://hostaddr=localhost dbname=svp2 user=svc password='svc' options='-c client_min_messages=NOTICE' application_name='freeswitch'\"/>"
                + " </settings>\n"
                + " <fifos>\n"
                + "   <fifo name=\"5900@$${domain}\" importance=\"0\">\n"
                + "   </fifo>\n"
                + " </fifos>\n"
                + "</configuration>\n"
                + "\n"
                + " </section>\n"
                + "</document>");
        return xml;

    }

    public String DialLookup(DialplanVariable variable) throws DialplanNotFoundException {

        if (variable.getTmsDP()) {
            TMSDialplan dp = dialplanService.getPremaidDialplan(variable);
            if (dp != null) {
                log.info("TMS pre-defined dialplan found: " + variable.getTmsUUID() + " @ " + variable.getContext() + " with " + variable.getTmsOrder());
                dp = commonService.executeTMSFunction(dp, variable);

                return dialplanRepository.LogDialplan(variable, dp);
            }
        }

        log.info("-------------------------");
        log.info("-------------------------");
        log.info("Start fifo Dialplan : " + variable.getContext() + " / ");
        log.info("-------------------------");
        log.info("-------------------------");

        throw new DialplanNotFoundException("Fifo TMS Dialplan was not found. " + variable.toJson());

    }

    public void CDRDump(HttpServletRequest request, CDR cdr) {
        Long queuePk = cdr.getDialerQueueId();
        if (queuePk != null) {
            WaitingCall call = callService.removeWaitingCall(queuePk, cdr.getCall_uuid());
            if (call == null) {
                log.warn("Could not find waiting call associated with CDR pk: {}", cdr.getPk());
                dialplanRepository.logDialplanInfoIntoDb(cdr.getCall_uuid(), "Could not find waiting call associated with CDR pk: {} callUUID: {}", cdr.getPk(), cdr.getCall_uuid());
            }
        }
        cdrRepository.storeCDR(cdr);

        TMSDialplan tmsDialplan = null;
        if (cdr.getTms_uuid() != null && cdr.getTms_uuid().equalsIgnoreCase("") == false) {
            tmsDialplan = dialplanService.getTMSDialplanForCDR(cdr.getTms_uuid(), cdr.getContext(), cdr.getOrderPower());
        }
        if (tmsDialplan == null) {
            return;
        }

        CallDetailRecordTMS mcdr = callDetailRecordService.getCDR(tmsDialplan.getCall_uuid());
        if (mcdr.getDialer()) {
            if (cdr.getOrderPower().equals(DDD.PLACE_CALL_IN_FIFO.name())) {
                if (cdr.getBridge_hangup_cause() == null) {
                    CallDispositionCode code = dispositionCodeService.callDroppedFromDialer();
                    //mcdr.setAmdEndFifoTime(cdr.getEndTime());

                    Duration duration = new Duration(mcdr.getAmdStartTime().toDateTime(), mcdr.getAmdEndFifoTime().toDateTime());
                    //mcdr.setAmdFifoDropped(Boolean.TRUE);

                    callingOutService.callDropped(mcdr.getCall_uuid(), duration.getMillis(), code);
                }
            }
        }
        //callDetailRecordService.saveCDR(mcdr);
    }

    public static String placeCallOnHoldForTransferCall = "placeCallOnHoldForTransferCall";

    public TMSDialplan placeCallOnHoldForTransferCall(DialplanVariable variable, TMSDialplan tmsDialplan) {
        Long queuePK = tmsDialplan.getQueuePkForTransfer();
        log.info("Placing call on hold for agent queue pk {} this is transfer call for callUUID {}", queuePK, tmsDialplan.getKey().toString());

        if (queuePK == null) {
            queuePK = callService.getAgentTransferQueueMap(tmsDialplan.getCall_uuid());
        }
        if (queuePK != null) {
            String internalCallUUID = transferService.getTransferCallUUIDForOriginalCallUUID(tmsDialplan.getCall_uuid());
            callService.removeWaitingCall(queuePK, internalCallUUID);
            log.info("Canceling Transfer call to Agent for call uuid {}", internalCallUUID);
            dialplanRepository.logDialplanInfoIntoDb(tmsDialplan.getCall_uuid(), "Canceling Transfer call to Agent for call uuid {}", internalCallUUID);
        }

        PhoneToType phone = new PhoneToType();
        phone.setFirstName(tmsDialplan.getBorrowerInfo().getBorrowerFirstName());
        phone.setLastName(tmsDialplan.getBorrowerInfo().getBorrowerLastName());
        try {
            phone.setPhoneNumber(Long.parseLong(tmsDialplan.getBorrowerInfo().getBorrowerPhoneNumber()));
        } catch (Exception ex) {
            phone.setPhoneNumber(null);
        }

        tmsDialplan.setUniqueID(variable.getUniqueID());
        tmsDialplan.setFifoUniqueID(variable.getUniqueID());
        tmsDialplan.setChannelCallUUID(variable.getChannelCallUUID());

        callingOutService.putCallOnWaitForTransferCallAsync(queuePK, tmsDialplan.getCall_uuid(), phone, tmsDialplan.getBorrowerInfo().getLoanId(), tmsDialplan.getOriginalTransferFromExt());
        return tmsDialplan;
    }

    public static String placeCallOnHold = "placeCallOnHold";

    public TMSDialplan placeCallOnHold(DialplanVariable variable, TMSDialplan tmsDialplan) {
        log.info("Placing call on hold " + tmsDialplan.getKey().toString());
        BorrowerInfo borrowerInfo = tmsDialplan.getBorrowerInfo();
        if (((tmsDialplan.getDialer() == false && tmsDialplan.getCallDirection() == CallDirection.OUTBOUND) || tmsDialplan.getCallDirection() == CallDirection.INBOUND) && borrowerInfo.getLoanId() != null) {
            AgentIncomingDistributionOrder ado = inboundCallService.inboundCallOrder(null, variable.getCallerIdLong(), tmsDialplan.getCall_uuid(), borrowerInfo.getLoanId());
            PhoneToType phoneToType = new PhoneToType();
            phoneToType.setFirstName(borrowerInfo.getBorrowerFirstName());
            phoneToType.setLastName(borrowerInfo.getBorrowerLastName());

            switch (tmsDialplan.getCallDirection()) {
                case INBOUND:
                    phoneToType.setPhoneNumber(variable.getCallerIdLong());
                    break;
                case OUTBOUND:
                    phoneToType.setPhoneNumber(variable.getCalleeIdLong());
                    break;
            }

            long queue = 1;
            if (tmsDialplan.getDialerQueueId() != null) {
                queue = tmsDialplan.getDialerQueueId();
            } else if (ado.getSettings() != null) {
                queue = ado.getSettings().getDialerQueuePk();
            } else if (tmsDialplan.getDialerQueueId() != null) {
                queue = tmsDialplan.getDialerQueueId();
            }

            log.info(variable.getCallDirection() + " putting call on wait: " + phoneToType.getPhoneNumber() + " QueuePK: " + queue);
            log.info(variable.getCallDirection() + " putting call on wait: " + phoneToType.getPhoneNumber() + " QueuePK: " + queue);
            callingOutService.putCallOnWaitAsync(queue, tmsDialplan.getKey().getTms_uuid(), borrowerInfo.getLoanId(),
                    tmsDialplan.getCallDirection(), tmsDialplan.getDialer(), phoneToType, configuration.getPutCallOnWaitAsyncDelaySetting1());
            log.info(variable.getCallDirection() + " placed call on wait: " + phoneToType.getPhoneNumber() + " QueuePK: " + queue);
        } else if (tmsDialplan.getCallDirection() == CallDirection.OUTBOUND && tmsDialplan.getDialer()) {

            log.info(tmsDialplan.getCallDirection() + " putting call on wait: " + variable.getTmsUUID());
            log.info(tmsDialplan.getCallDirection() + " putting call on wait: " + variable.getCallerIdLong());
            log.info(tmsDialplan.getCallDirection() + " putting call on wait: " + variable.getTmsUUID() + " -+> " + tmsDialplan.getKey().getTms_uuid());
            log.info(tmsDialplan.getCallDirection() + " putting call on wait: " + variable.getTmsUUID() + " -+> " + tmsDialplan.getCalleeLong());

            callingOutService.callRespondedAsync(tmsDialplan.getCall_uuid(), System.currentTimeMillis() - tmsDialplan.getCreateLife(), callService);
        } else if (borrowerInfo.getLoanId() == null) {

            log.info(tmsDialplan.getCallDirection() + " putting call on wait Loan is null: " + variable.getTmsUUID());
            Long number = variable.getCallerIdLong();
            if (number == null) {
                number = 0l;
            }
            AgentIncomingDistributionOrder ado = inboundCallService.inboundCallOrder(null, number, tmsDialplan.getCall_uuid());

            PhoneToType phoneToType = new PhoneToType();
            phoneToType.setFirstName(borrowerInfo.getBorrowerFirstName());
            phoneToType.setLastName(borrowerInfo.getBorrowerLastName());

            switch (tmsDialplan.getCallDirection()) {
                case INBOUND:
                    phoneToType.setPhoneNumber(variable.getCallerIdLong());
                    break;
                case OUTBOUND:
                    phoneToType.setPhoneNumber(variable.getCalleeIdLong());
                    break;
            }
            long queue = 1;
            if (tmsDialplan.getDialerQueueId() != null) {
                queue = tmsDialplan.getDialerQueueId();
            } else if (ado.getSettings() != null) {
                queue = ado.getSettings().getDialerQueuePk();
            } else if (tmsDialplan.getDialerQueueId() != null) {
                queue = tmsDialplan.getDialerQueueId();
            }
            log.info(variable.getCallDirection() + " putting call on wait Loan is null: " + phoneToType.getPhoneNumber() + " QueuePK: (Q) {} tmsDialplan: {}", queue, tmsDialplan.getDialerQueueId());
            log.info(variable.getCallDirection() + " putting call on wait Loan is null: " + phoneToType.getPhoneNumber() + " QueuePK: (Q) {} tmsDialplan: {}", queue, tmsDialplan.getDialerQueueId());
            callingOutService.putCallOnWaitAsync(queue, tmsDialplan.getKey().getTms_uuid(), null,
                    tmsDialplan.getCallDirection(), tmsDialplan.getDialer(), phoneToType, configuration.getPutCallOnWaitAsyncDelaySetting2());

        }
        return tmsDialplan;
    }
}
