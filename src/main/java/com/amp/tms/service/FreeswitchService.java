/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.tms.service;

import com.amp.tms.db.entity.freeswitch.FreeswitchNode;
import com.amp.tms.db.repository.FreeswitchRepository;
import com.amp.tms.enumerated.FreeswitchContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author HS
 */
@Service
public class FreeswitchService {

    private static final Logger LOG = LoggerFactory.getLogger(FreeswitchService.class);

    @Autowired
    private FreeswitchNodeService freeswitchNodeService;

    @Autowired
    private FreeswitchConfiguration configuration;

    @Autowired
    private FreeswitchRepository freeswitchRepository;

    public boolean isOnCallFreeswitch(int ext) {
        return freeswitchRepository.isOnCallFreeswitch(ext);
    }

    public boolean callUUIDExistsOnFreeswitch(String uuid) {
        return freeswitchRepository.callUUIDExistsOnFreeswitch(uuid);
    }

    public String getOtherChannalBForChannalA(String uuid) {
        return freeswitchRepository.getOtherChannalBForChannalA(uuid);
    }

    public boolean isRegisteredOnFreeswitch(int ext) {
        if (configuration.useSipTable()) {
            return freeswitchRepository.isRegisteredOnFreeswitchSip(ext);
        }
        return freeswitchRepository.isRegisteredOnFreeswitch(ext);
    }

    public FreeswitchNode getFreeswitchNodeForCallUUID(String call_uuid) {
        return freeswitchNodeService.getNodeForCallUUID(call_uuid);
    }

    public void releaseFreeswitchNode(String call_uuid) {
        freeswitchNodeService.releaseNode(call_uuid);
    }

    public String getFreeswitchIPNew(String call_uuid, FreeswitchContext context) {
        boolean useLoadBalancer = configuration.useFreeswitchLoadBalancer();
        boolean useLoadBalancerPerContext = configuration.useFreeswitchLoadBalancerPerContext();
        LOG.info("Freeswitch loadbalancer is currently: {}", useLoadBalancer);
        LOG.info("Freeswitch loadbalancer context is currently: {}", useLoadBalancerPerContext);
        if (useLoadBalancerPerContext) {
            return configuration.getFreeswitchLoadBalancerPerContextIP(context);
        } else if (useLoadBalancer) {
            try {
                return freeswitchNodeService.getNextNodeForContext(call_uuid, context).getFreeSWITCH_IPv4();
            } catch (Exception ex) {
                LOG.error("Error getting next freeswitch node: " + ex.getMessage(), ex);
            }
        }
        return configuration.getFreeswitchIP(context);
    }

}
