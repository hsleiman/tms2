/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.tms.service.freeswitch;

/**
 *
 * 
 */
import com.amp.tms.db.entity.freeswitch.CDR;
import com.amp.tms.db.entity.freeswitch.TMSDialplan;
import com.amp.tms.db.repository.DialplanRepository;
import com.amp.tms.exception.DialplanNotFoundException;
import com.amp.tms.freeswitch.pojo.DialplanVariable;
import com.amp.tms.service.CdrService;
import com.amp.tms.service.DialplanService;
import com.amp.tms.service.freeswitch.common.CommonService;
import javax.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * 
 */
@Service
public class IVRService {

    private static final Logger log = LoggerFactory.getLogger(IVRService.class);

    @Autowired
    private CdrService cdrRepository;

    @Autowired
    private CommonService commonService;

    @Autowired
    private DialplanService dialplanService;

    @Autowired
    private DialplanRepository dialplanRepository;

    public String DialLookup(DialplanVariable variable) throws DialplanNotFoundException {

        if (variable.getTmsDP()) {
            TMSDialplan dp = dialplanService.getPremaidDialplan(variable);
            if (dp != null) {
                log.info("TMS pre-defined dialplan found: " + variable.getTmsUUID() + " @ " + variable.getContext() + " with " + variable.getTmsOrder());
                dp = commonService.executeTMSFunction(dp, variable);
                return dialplanRepository.LogDialplan(variable, dp);
            }
        }

        throw new DialplanNotFoundException("IVR TMS Dialplan was not found. " + variable.toJson());

    }

    public void CDRDump(HttpServletRequest request, CDR cdr) {
        cdrRepository.storeCDR(cdr);
    }

}
