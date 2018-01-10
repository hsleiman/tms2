/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.tms.service;

import com.hazelcast.core.IMap;
import com.hazelcast.query.Predicate;
import com.objectbrains.hcms.hazelcast.HazelcastService;
import com.amp.tms.hazelcast.Configs;
import com.amp.tms.websocket.Websocket;
import com.amp.tms.websocket.message.Function;
import com.amp.tms.websocket.message.outbound.CallSipHeader;
import com.amp.tms.websocket.message.outbound.Send;
import java.util.Map;
import java.util.Set;
import javax.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

/**
 *
 * 
 */
@Service
public class TransferService {

    private static final Logger LOG = LoggerFactory.getLogger(TransferService.class);

    @Autowired
    private HazelcastService hazelcastService;

    @Autowired
    @Lazy
    private Websocket websocket;

    private IMap<String, String> agentTransferMap;
    private IMap<String, String> transferCallUUIDToCallUUID;
    private IMap<String, Integer> transferCallUUIDToExt;

    @PostConstruct
    private void init() {
        agentTransferMap = hazelcastService.getMap(Configs.AGENT_TRANSFER_MAP);
        transferCallUUIDToCallUUID = hazelcastService.getMap(Configs.TRANSFER_CALLUUID_TO_CALLUUID);
        transferCallUUIDToExt = hazelcastService.getMap(Configs.TRANSFER_CALLUUID_TO_EXT);
    }

    public void setTransferCallUUIDToExt(String internalCallUUID, Integer ext) {
        transferCallUUIDToExt.put(internalCallUUID, ext);
    }

    public Integer getTransferCallUUIDToExt(String internalCallUUID) {
        return transferCallUUIDToExt.get(internalCallUUID);
    }

    public void setTransferCallUUIDToCallUUID(String internalCallUUID, String originalCallUUId) {
        transferCallUUIDToCallUUID.put(internalCallUUID, originalCallUUId);
    }

    public String getTransferCallUUIDForInternalCallUUID(String internalCallUUID) {
        return transferCallUUIDToCallUUID.get(internalCallUUID);
    }

    public String getTransferCallUUIDForOriginalCallUUID(String originalCallUUId) {
        Set<String> keys = transferCallUUIDToCallUUID.keySet(new SavedValuePredict(originalCallUUId));
        if (keys.isEmpty()) {
            return null;
        }
        return keys.iterator().next();
    }

    public void sendToAgentTheExtToTransferTo(int orginalAgent, int transferExt, String call_uuid) {
        LOG.info("Sending Transfer Call to {} from {} for Call UUID {}", transferExt, orginalAgent, call_uuid);
        agentTransferMap.put(orginalAgent + "+" + transferExt, call_uuid);

        Send send = new Send(Function.LOCK_NEXT_AVAILABLE_TRANSFER_TO_AGENT);
        send.setOriginalTransferCallUUID(transferCallUUIDToCallUUID.get(call_uuid));
        send.setTransferToExt(transferExt);
        send.setLockedExtension(transferExt);
        CallSipHeader callSipHeader = new CallSipHeader();
        callSipHeader.setCall_uuid(call_uuid);
        send.setCallSipHeader(callSipHeader);
        websocket.sendWithRetry(orginalAgent, send);
    }

    public String getAgentTransferMapCallUUID(String value) {
        String calluuid = agentTransferMap.get(value);
        return calluuid;
    }

    private static class SavedValuePredict implements Predicate<String, String> {

        String searchValue;

        public SavedValuePredict() {

        }

        public SavedValuePredict(String searchValue) {
            this.searchValue = searchValue;
        }

        @Override
        public boolean apply(Map.Entry<String, String> entry) {
            return entry.getValue().equals(this.searchValue);
        }

    }

}
