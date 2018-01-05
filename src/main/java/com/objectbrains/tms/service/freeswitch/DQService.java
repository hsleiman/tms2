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
import com.objectbrains.tms.enumerated.refrence.DDD;
import com.objectbrains.tms.exception.DialplanNotFoundException;
import com.objectbrains.tms.freeswitch.pojo.DialplanVariable;
import com.objectbrains.tms.service.CallDetailRecordService;
import com.objectbrains.tms.service.CdrService;
import com.objectbrains.tms.service.DialplanService;
import com.objectbrains.tms.service.DispositionCodeService;
import com.objectbrains.tms.service.freeswitch.common.CommonService;
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
public class DQService {

    private static final Logger log = LoggerFactory.getLogger(DQService.class);

    @Autowired
    private CdrService cdrRepository;

    @Autowired
    private DialplanService dialplanService;

    @Autowired
    private DialplanRepository dialplanRepository;

    @Autowired
    private CallingOutService callingOutService;

    @Autowired
    private CallDetailRecordService callDetailRecordService;
    
    @Autowired
    private DispositionCodeService dispositionCodeService;

    @Autowired
    private CommonService commonService;

    public String DialLookup(DialplanVariable variable) throws DialplanNotFoundException {

        if (variable.getTmsDP()) {
            TMSDialplan dp = dialplanService.getPremaidDialplan(variable);
            if (dp != null) {
                log.info("TMS pre-defined dialplan found: " + variable.getTmsUUID() + " @ " + variable.getContext() + " with " + variable.getTmsOrder());
                dp = commonService.executeTMSFunction(dp, variable);
                return dialplanRepository.LogDialplan(variable, dp);
            }
        }

        throw new DialplanNotFoundException("DQ TMS Dialplan was not found. " + variable.toJson());
    }

    public void CDRDump(HttpServletRequest request, CDR cdr) {
        cdrRepository.storeCDR(cdr);

        TMSDialplan tmsDialplan = null;
        if (cdr.getTms_uuid() != null && cdr.getTms_uuid().equalsIgnoreCase("") == false) {
            tmsDialplan = dialplanService.getTMSDialplanForCDR(cdr.getTms_uuid(), cdr.getContext(), cdr.getOrderPower());
        }
        if(tmsDialplan == null){
            return;
        }

        CallDetailRecordTMS mcdr = callDetailRecordService.getCDR(tmsDialplan.getCall_uuid());
        if (cdr.getOrderPower().equals(DDD.START_AMD.name()) || cdr.getOrderPower().equals(DDD.WAIT_FOR_MEDIA.name()) || cdr.getOrderPower().equals(DDD.VERIFY_AMD.name())) {
            CallDispositionCode code = dispositionCodeService.callNoAnswer();

            LocalDateTime amdStartTime = cdr.getStartTime();
            if (mcdr.getAmdStartTime() != null) {
                amdStartTime = mcdr.getAmdStartTime();
            }

            LocalDateTime cdrEndTime = cdr.getEndTime();

            Duration duration = new Duration(amdStartTime.toDateTime(), cdrEndTime.toDateTime());
            //mcdr.setAmdBeforeFifoDropped(Boolean.TRUE);
            callingOutService.callDropped(mcdr.getCall_uuid(), duration.getMillis(), code);
        }
    }
}
