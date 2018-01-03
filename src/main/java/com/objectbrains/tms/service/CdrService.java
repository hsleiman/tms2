/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.objectbrains.tms.service;

import com.objectbrains.hcms.annotation.ConfigContext;
import com.objectbrains.hcms.configuration.ConfigurationUtility;
import com.objectbrains.hcms.hazelcast.HazelcastService;
import com.objectbrains.sti.service.tms.TMSService;
import com.objectbrains.tms.db.entity.freeswitch.CDR;
import com.objectbrains.tms.db.repository.CdrRepository;
import javax.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author connorpetty
 */
@Service
public class CdrService {

    private static final Logger LOG = LoggerFactory.getLogger(CdrService.class);

    @Autowired
    private DialplanService dialplanRepository;

    @Autowired
    private AgentService agentService;

    @Autowired
    private HazelcastService hazelcast;

    @Autowired
    private DispositionCodeService dispositionCodeService;

    @Autowired
    private CdrRepository cdrRepository;

    @Autowired
    private CallDetailRecordService callDetailRecordService;

    @Autowired
    private FreeswitchConfiguration freeswitchConfiguration;

    @Autowired
    private FreeswitchService freeswitchService;

    @Autowired
    private TMSService tmsIWS;

    @ConfigContext
    private ConfigurationUtility config;

    @PostConstruct
    private void init() {
    }

    private long currentTimeMillis() {
        long l = hazelcast.getCluster().getClusterTime();
        LOG.debug("HZ ClusterTime {}", l);
        return l;
    }

    public void storeCDR(CDR cdr) {
        cdrRepository.persistCDR(cdr);
    }

    public void updateCallDetailRecord(CDR cdr) {
       
    }

   

}
