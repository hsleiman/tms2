/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.objectbrains.tms.service.freeswitch;

/**
 *
 * @author hsleiman
 */
import com.objectbrains.sti.db.entity.disposition.CallDispositionCode;
import com.objectbrains.tms.db.entity.cdr.CallDetailRecordTMS;
import com.objectbrains.tms.db.entity.freeswitch.CDR;
import com.objectbrains.tms.db.entity.freeswitch.TMSDialplan;
import com.objectbrains.tms.db.repository.DialplanRepository;
import com.objectbrains.tms.enumerated.CallDirection;
import com.objectbrains.tms.enumerated.FreeswitchContext;
import com.objectbrains.tms.freeswitch.pojo.DialplanVariable;
import com.objectbrains.tms.freeswitch.premaid.DialplanBuilder;
import com.objectbrains.tms.service.CallDetailRecordService;
import com.objectbrains.tms.service.CdrService;
import com.objectbrains.tms.service.DialplanService;
import com.objectbrains.tms.service.DispositionCodeService;
import com.objectbrains.tms.service.DncService;
import com.objectbrains.tms.service.FreeswitchConfiguration;
import com.objectbrains.tms.service.dialer.DialerService;
import com.objectbrains.tms.service.freeswitch.common.CommonService;
import com.objectbrains.tms.service.freeswitch.common.Incoming2;
import javax.servlet.http.HttpServletRequest;
import org.joda.time.Duration;
import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author hsleiman
 */
@Service
public class SBCService {

    private static final Logger log = LoggerFactory.getLogger(SBCService.class);

    @Autowired
    private DispositionCodeService dispositionCodeService;

    @Autowired
    private DialerService dialerService;

    @Autowired
    private CallingOutService callingOutService;

    @Autowired
    private CdrService cdrRepository;

    @Autowired
    private FreeswitchConfiguration configuration;

    @Autowired
    private Incoming2 incomingRoute;

    @Autowired
    private DialplanService dialplanService;

    @Autowired
    private DialplanRepository dialplanRepository;

    @Autowired
    private CommonService commonService;

    @Autowired
    private CallDetailRecordService callDetailRecordService;

    @Autowired
    private DncService dnc;

    public String DialLookup(DialplanVariable variable) {

        if (variable.getTmsDP()) {
            TMSDialplan dp = dialplanService.getPremaidDialplan(variable);
            if (dp != null) {
                log.info("TMS pre-defined dialplan found: " + variable.getTmsUUID() + " @ " + variable.getContext() + " with " + variable.getTmsOrder());
                dp = commonService.executeTMSFunction(dp, variable);
                return dialplanRepository.LogDialplan(variable, dp);
            }
        }
        log.info("TMS pre-defined dialplan not found: " + variable.getTmsUUID());

        DialplanBuilder dialplanBuilder = null;
        log.info("Checking to see if its outbound call: " + variable.getCallDirection());
        if (dialplanService.isOutbound(variable)) {
            log.error("This should not happen but just in case we are calling the manual dial since its a: " + variable.getCallDirection());
            dialplanBuilder = incomingRoute.getDialplanBuilderSBC(variable);
            log.error("This should not happen but just in case we are calling the manual dial since its a: " + dialplanBuilder.getTMS_UUID());
        } else {
            dialplanBuilder = incomingRoute.getDialplanBuilderSBC(variable);

        }
        return dialplanRepository.LogDialplan(variable, dialplanBuilder.getDialplan());
    }

    public void CDRDump(HttpServletRequest request, CDR cdr) {
        try {
            if (cdr.getContext() == FreeswitchContext.sbc_dp) {
                TMSDialplan tmsDialplan = null;
                if (cdr.getTms_uuid() != null && cdr.getTms_uuid().equalsIgnoreCase("") == false) {
                    tmsDialplan = dialplanService.getTMSDialplanForCDR(cdr.getTms_uuid(), cdr.getContext(), cdr.getOrderPower());
                }
                if (tmsDialplan == null) {
                    for (int i = 0; i < 10; i++) {
                        log.warn("tmsDialplan was null for {} - {} - {}", cdr.getTms_uuid(), cdr.getContext(), cdr.getOrderPower());
                    }
                    
                } else {
                    CallDetailRecordTMS mcdr = callDetailRecordService.getCDR(tmsDialplan.getCall_uuid());
                    callDetailRecordService.uploadRecording(cdr.getCall_uuid(), cdr.getSip_local_network_addr(), cdr.getRecrodingUploadTms(), cdr.getDuration());

                    int hangupCauseCodeRaw = 0;
                    if (cdr.getHangup_cause_q850() == null) {
                        log.info("cdr hangup cause: {} for {}", hangupCauseCodeRaw, tmsDialplan.getCall_uuid());
                    } else {
                        hangupCauseCodeRaw = cdr.getHangup_cause_q850();
                    }

                    CallDispositionCode systemDispositionCode = dispositionCodeService.getDispositionCodeForQCode(hangupCauseCodeRaw);
                    if (cdr.getBridge_hangup_cause() != null && cdr.getBridge_hangup_cause().equalsIgnoreCase("NO_ANSWER")) {
                        systemDispositionCode = dispositionCodeService.callNoAnswer();
                    } else if (mcdr.getSystemDispostionCode() != null && mcdr.getSystemDispostionCode() == 7) {
                        systemDispositionCode = dispositionCodeService.answeringMachineCode();

                    } else if (mcdr.getSystemDispostionCode() != null && mcdr.getSystemDispostionCode() == 168) {
                        systemDispositionCode = dispositionCodeService.answeringMachineDialerLeftMessageCode();
                    }

                    boolean hangupCause = cdr.getHangup_cause() != null && cdr.getHangup_cause().equalsIgnoreCase("NORMAL_CLEARING") == false;
                    boolean bridgeHangupCause = cdr.getBridge_hangup_cause() != null && cdr.getBridge_hangup_cause().equalsIgnoreCase("NORMAL_CLEARING") == false;
                    mcdr.setBridgeHangupCauseBoolValue(bridgeHangupCause);
                    mcdr.setHangupCauseBoolValue(hangupCause);
                    mcdr.setComplete(Boolean.TRUE);
                    callDetailRecordService.saveSBCComplet(mcdr.getCall_uuid(), hangupCause, bridgeHangupCause, systemDispositionCode);

                    if (cdr.getDialer()) {
                        if (mcdr.getAmdExtTransferTo() != null) {
                            log.info("This is a dialer call and the amd ext transfer to was to {} - {}", mcdr.getAmdExtTransferTo(), mcdr.getCall_uuid());
                            
                            if (systemDispositionCode.getDispositionId() != 110 || bridgeHangupCause || hangupCause) {
                                log.info("The Disposition Code is being passed as {} to callEndedForAgent - {}", systemDispositionCode.getDisposition(), mcdr.getCall_uuid());
                                callingOutService.callEndedForAgent(mcdr.getAmdExtTransferTo(), mcdr.getCall_uuid(), systemDispositionCode, tmsDialplan.getDialerQueueId());

                            } else if (mcdr.isAnswered() == false && mcdr.getRinged() == false) {
                                log.info("The Disposition Code is being passed as null to callEndedForAgent - {}", mcdr.getCall_uuid());
                                callingOutService.callEndedForAgent(mcdr.getAmdExtTransferTo(), mcdr.getCall_uuid(), null, tmsDialplan.getDialerQueueId());

                                LocalDateTime amdStartTime = cdr.getStartTime();
                                if (mcdr.getAmdStartTime() != null) {
                                    amdStartTime = mcdr.getAmdStartTime();
                                }
                                LocalDateTime cdrEndTime = cdr.getEndTime();
                                Duration duration = new Duration(amdStartTime.toDateTime(), cdrEndTime.toDateTime());
                                callingOutService.callDropped(mcdr.getCall_uuid(), duration.getMillis(), systemDispositionCode);

                            }else if( mcdr.getRinged() && mcdr.isAnswered() == false ){
                                log.info("The Disposition Code is being passed as null to callEndedForAgent - {}", mcdr.getCall_uuid());
                                callingOutService.callEndedForAgent(mcdr.getAmdExtTransferTo(), mcdr.getCall_uuid(), null, tmsDialplan.getDialerQueueId());
                            } 
                            
                            else {
                                if (mcdr.isWrapped()) {
                                    log.info("The Disposition Code is being passed as {} to callEndedAsync - {}", systemDispositionCode.getDescription(), mcdr.getCall_uuid());
                                    callingOutService.callEndedAsync(mcdr.getCall_uuid(), dispositionCodeService.getDispositionCodeFromId(mcdr.getUserDispostionCode()), tmsDialplan.getDialerQueueId());
                                }
                            }
                        } else {
                            log.info("This is a dialer call and the amd ext transfer to was NULL - systemDispositionCode {}, bridgeHangupCause {}, hangupCause {}", mcdr.getCall_uuid(), systemDispositionCode.getDispositionId(), bridgeHangupCause, hangupCause);
                            if (systemDispositionCode.getDispositionId() != 110 || bridgeHangupCause || hangupCause) {
                                log.info("The Disposition Code is being passed as {} to callEndedAsync - {}", systemDispositionCode.getDisposition(), mcdr.getCall_uuid());
                                callingOutService.callEndedAsync(mcdr.getCall_uuid(), systemDispositionCode, tmsDialplan.getDialerQueueId());
                            }
                        }
                    } else {
                        if (cdr.getHangup_cause_q850() != 16) {
                            log.info("2 Seting Call Not 16 " + tmsDialplan.getCall_uuid());
                            callingOutService.callEndedAsync(tmsDialplan.getCall_uuid(), systemDispositionCode, tmsDialplan.getDialerQueueId());

                        } else if (tmsDialplan.getCaller() == null) {
                            log.info("2 Seting Call Dropped " + tmsDialplan.getCall_uuid());
                            callingOutService.callDropped(tmsDialplan.getCall_uuid(), tmsDialplan.getCreateLife() - System.currentTimeMillis(), dispositionCodeService.callerUnknownCode());

                        } else if (mcdr.isAnswered() == false && mcdr.isCallerHangup() == true && mcdr.getCallDirection() == CallDirection.INBOUND) {
                            log.info("2 INBOUND Seting Call Failed " + tmsDialplan.getCall_uuid());
                            callingOutService.callEndedAsync(tmsDialplan.getCall_uuid(), dispositionCodeService.noResponseCode(), tmsDialplan.getDialerQueueId());

                        } else {
                            log.info("2 Seting Call Success Not WRAPED" + tmsDialplan.getCall_uuid());
                            if (mcdr.isWrapped()) {
                                log.info("2 Seting Call Success WRAPED" + tmsDialplan.getCall_uuid());
                                callingOutService.callEndedAsync(cdr.getTms_uuid(), dispositionCodeService.getDispositionCodeFromId(mcdr.getUserDispostionCode()), tmsDialplan.getDialerQueueId());
                            }
                        }
                    }
                }
            }
        } catch (Exception ex) {
            log.error("Error: " + ex.getMessage(), ex);
        }
        cdrRepository.storeCDR(cdr);
    }

    public TMSDialplan printstuff(DialplanVariable variable, TMSDialplan dp) {
        log.info("YES IT wORKS.");
        return dp;

    }

}
