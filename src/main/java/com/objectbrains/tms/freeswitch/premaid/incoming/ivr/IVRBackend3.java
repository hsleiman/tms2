/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.objectbrains.tms.freeswitch.premaid.incoming.ivr;

import com.objectbrains.svc.iws.AllocationIWS;
import com.objectbrains.svc.iws.PaymentServiceIWS;
import com.objectbrains.svc.iws.TMSServiceIWS;
import com.objectbrains.tms.service.CallDetailRecordService;
import com.objectbrains.tms.service.DialerQueueRecordService;
import com.objectbrains.tms.service.FreeswitchConfiguration;
import com.objectbrains.tms.service.InboundCallService;
import com.objectbrains.tms.service.TextToSpeechService;
import com.objectbrains.tms.service.freeswitch.common.Incoming3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author hsleiman
 */
@Service("IVRBackend3")
public class IVRBackend3 {

    protected final static Logger log = LoggerFactory.getLogger(IVRBackend3.class);

    @Autowired
    private CallDetailRecordService callDetailRecordService;

    @Autowired
    private TMSServiceIWS tmsIWS;

    @Autowired
    private FreeswitchConfiguration configuration;

    @Autowired
    private InboundCallService inboundCallService;

    @Autowired
    private PaymentServiceIWS paymentServiceIWS;

    @Autowired
    private DialerQueueRecordService dialerQueueRecordRepository;

    @Autowired
    private AllocationIWS allocationIWS;

    @Autowired
    private Incoming3 incoming;

    @Autowired
    private TextToSpeechService textToSpeechService;

 

}
