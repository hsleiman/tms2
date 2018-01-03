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
import com.objectbrains.tms.db.entity.freeswitch.CDR;
import com.objectbrains.tms.db.repository.DialplanRepository;
import com.objectbrains.tms.freeswitch.pojo.DialplanVariable;
import com.objectbrains.tms.freeswitch.premaid.DialplanBuilder;
import com.objectbrains.tms.freeswitch.premaid.DialplanBuilderFactory;
import com.objectbrains.tms.freeswitch.premaid.incoming.IncomingRSBC;
import com.objectbrains.tms.service.CdrService;
import com.objectbrains.tms.service.DialplanService;
import javax.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author hsleiman
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
