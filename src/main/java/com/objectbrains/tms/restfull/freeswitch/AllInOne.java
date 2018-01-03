/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.objectbrains.tms.restfull.freeswitch;

import com.objectbrains.tms.db.entity.freeswitch.CDR;
import com.objectbrains.tms.db.entity.freeswitch.StaticConfiguration;
import com.objectbrains.tms.db.entity.freeswitch.StaticDialplan;
import com.objectbrains.tms.db.repository.ConfigurationRepository;
import com.objectbrains.tms.exception.DialplanNotFoundException;
import com.objectbrains.tms.freeswitch.pojo.ConfigurationVariable;
import com.objectbrains.tms.freeswitch.pojo.DialplanVariable;
import com.objectbrains.tms.freeswitch.pojo.DirectoryVariable;
import com.objectbrains.tms.service.DialplanService;
import com.objectbrains.tms.service.freeswitch.DQService;
import com.objectbrains.tms.service.freeswitch.FifoService;
import com.objectbrains.tms.service.freeswitch.FsAgentService;
import com.objectbrains.tms.service.freeswitch.IVRService;
import com.objectbrains.tms.service.freeswitch.RSBCService;
import com.objectbrains.tms.service.freeswitch.SBCService;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.BeanParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author hsleiman
 */
@Path("/all_in_one")
@Produces(MediaType.TEXT_PLAIN)
public class AllInOne {

    private static final Logger log = LoggerFactory.getLogger(AllInOne.class);

    @Autowired
    private FsAgentService agentService;
    @Autowired
    private DQService dQService;
    @Autowired
    private FifoService fifoService;
    @Autowired
    private IVRService iVRService;
    @Autowired
    private RSBCService rSBCService;
    @Autowired
    private SBCService sBCService;
    @Autowired
    private DialplanService dialplanRepository;
    @Autowired
    private ConfigurationRepository configurationRepository;

    public AllInOne() {
        log.info("created instance of AllInOne");
    }

    @Path("/config")
    @POST
    public String Configuration(@BeanParam ConfigurationVariable configurationVariable) {
        log.info("Configuration lookup: " + configurationVariable.getKey_value());
        try {
            StaticConfiguration staticConfiguration = configurationRepository.getStaticConfiguration(configurationVariable.getKey_value());
            if (staticConfiguration != null) {
                log.info(configurationVariable.getKey_value() + " -> File Found in DB ");
                return configurationRepository.getStaticConfiguration(configurationVariable.getKey_value()).getConfiguration();
            }
        } catch (Exception ex) {
            log.error(configurationVariable.getKey_value() + "- Error: " + ex.getMessage());
        }
        return "";
    }

    @Path("/directory")
    @POST
    public String DirectoryLookup(@BeanParam DirectoryVariable directoryVariable) {
        log.info("Directory lookup: " + directoryVariable.getUser());
        return agentService.DirectoryLookup(directoryVariable);
    }

    @Path("/dialplan")
    @POST
    public String DialLookup(MultivaluedMap<String, String> request, @BeanParam DialplanVariable dialplanVariable) throws DialplanNotFoundException {
        if (request == null) {
            log.info("Request is null.....");
        } else {
            log.info("Request is NOT null..... Thats Goood");
//            for (Map.Entry<String, List<String>> entrySet : request.entrySet()) {
//                String key = entrySet.getKey();
//                List<String> value = entrySet.getValue();
//                if (key.contains("TMS") || key.contains("tms")) {
//                    log.info("**************************** - " + key + " = " + value);
//                } else {
//                    log.info(key + " = " + value);
//                }
//            }
        }

        log.info("Dialpan lookup: " + dialplanVariable.getContext());
        log.info("------------------------");
        log.info(dialplanVariable.toJson());
        log.info("------------------------");
        if (dialplanVariable.getDialer() == null) {
            try {
                log.info("Static Dialplan Search: " + dialplanVariable.getCallerIdNumber() + " -> " + dialplanVariable.getCalleeIdNumber() + " @ " + dialplanVariable.getContext());
                StaticDialplan sd = dialplanRepository.getStaticDialplan(dialplanVariable.getCallerIdNumber(), dialplanVariable.getCalleeIdNumber(), dialplanVariable.getContext());
                if (sd != null & sd.isActive()) {

                    log.info("------------------------------------------------------------------");
                    log.info("------------------------------------------------------------------");
                    log.info("------------------------------------------------------------------");
                    log.info("------------------------------------------------------------------");
                    log.info("Static Dialplan found: " + dialplanVariable.getCallerIdNumber() + " -> " + dialplanVariable.getCalleeIdNumber() + " @ " + dialplanVariable.getContext());
                    log.info("------------------------------------------------------------------");
                    log.info(sd.getDialplan());
                    log.info("------------------------------------------------------------------");
                    log.info("------------------------------------------------------------------");
                    log.info("------------------------------------------------------------------");
                    log.info("------------------------------------------------------------------");
                    return sd.getDialplan();
                }
            } catch (Throwable e) {
            }
            log.info("Static Dialplan not found.");
        } else {
            log.info("Static Dialplan was skipped.");
        }

        switch (dialplanVariable.getContext()) {
            case agent_dp:
                return agentService.DialLookup(dialplanVariable);
            case dq_dp:
                return dQService.DialLookup(dialplanVariable);
            case fifo_dp:
                return fifoService.DialLookup(dialplanVariable);
            case ivr_dp:
                return iVRService.DialLookup(dialplanVariable);
            case rsbc_dp:
                return rSBCService.DialLookup(dialplanVariable);
            case sbc_dp:
                return sBCService.DialLookup(dialplanVariable);

        }

        log.error("DialLookup");
        log.error("Context did not match so TMS is missing context.");
        log.error("Context did not match so TMS is missing context.");
        log.error("Context did not match so TMS is missing context.");
        log.error("Context did not match so TMS is missing context.");
        log.error("Context did not match so TMS is missing context.");
        log.error("Context did not match so TMS is missing context.");
        log.error("Context did not match so TMS is missing context.");
        log.error("Context did not match so TMS is missing context.");
        log.error("Context did not match so TMS is missing context.");
        log.error("Context did not match so TMS is missing context.");
        log.error("Context did not match so TMS is missing context.");
        log.error("Context did not match so TMS is missing context.");
        log.error("Context did not match so TMS is missing context.");
        log.error("DialLookup");
        return "Context did not match so TMS is missing context.";
    }

    @Path("/cdr")
    @POST
    public void CDRDump(CDR cdr, @Context HttpServletRequest request) {
        try {
            log.info("CDR Context: " + cdr.getContext());

            switch (cdr.getContext()) {
                case agent_dp:
                    agentService.CDRDump(request, cdr);
                    return;
                case dq_dp:
                    dQService.CDRDump(request, cdr);
                    return;
                case fifo_dp:
                    fifoService.CDRDump(request, cdr);
                    return;
                case ivr_dp:
                    iVRService.CDRDump(request, cdr);
                    return;
                case rsbc_dp:
                    rSBCService.CDRDump(request, cdr);
                    return;
                case sbc_dp:
                    sBCService.CDRDump(request, cdr);
                    return;
                default:
                    log.error("CDR");
                    log.error("Context did not match so TMS is missing context.");
                    log.error("Context did not match so TMS is missing context.");
                    log.error("Context did not match so TMS is missing context.");
                    log.error("Context did not match so TMS is missing context.");
                    log.error("Context did not match so TMS is missing context.");
                    log.error("Context did not match so TMS is missing context.");
                    log.error("Context did not match so TMS is missing context.");
                    log.error("Context did not match so TMS is missing context.");
            }
        } catch (Throwable ex) {
            log.error(ex.getMessage(), ex);
        }
    }

    @Path("/test")
    @POST
    public String test(@Context HttpServletRequest request) {
        log.info("Good test... Freeswitch connected....");
        return "{good}";
    }

}
