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
import com.amp.tms.db.repository.DialplanRepository;
import com.amp.tms.freeswitch.pojo.DialplanVariable;
import com.amp.tms.freeswitch.premaid.DialplanBuilder;
import com.amp.tms.freeswitch.premaid.DialplanBuilderFactory;
import com.amp.tms.freeswitch.premaid.incoming.IncomingRSBC;
import com.amp.tms.service.CdrService;
import com.amp.tms.service.DialplanService;
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
public class RSBCService {

    private static final Logger log = LoggerFactory.getLogger(RSBCService.class);

    @Autowired
    private CdrService cdrRepository;

    @Autowired
    private DialplanService dialplanService;
    
    @Autowired
    private DialplanRepository dialplanRepository;

    @Autowired
    private DialplanBuilderFactory dialplanBuilderFactory;

    public String DialLookup(DialplanVariable variable) {

        log.info("-------------------------");
        log.info("-------------------------");
        log.info("Start R-SBC Dialplan : " + variable.getContext() + " / ");
        log.info("-------------------------");
        log.info("-------------------------");
        log.info("-------------------------");

        DialplanBuilder premaid = new IncomingRSBC(variable);
        return dialplanRepository.LogDialplan(variable, premaid.getDialplan());
    }

    public void CDRDump(HttpServletRequest request, CDR cdr) {
        cdrRepository.storeCDR(cdr);
    }

}
