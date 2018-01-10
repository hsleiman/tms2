/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.tms.service;

import com.hazelcast.core.IMap;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.DataSerializable;
import com.hazelcast.query.Predicate;
import com.objectbrains.hcms.hazelcast.HazelcastService;
import com.amp.tms.db.entity.freeswitch.FreeswitchNode;
import com.amp.tms.enumerated.FreeswitchContext;
import com.amp.tms.enumerated.FreeswitchNodeStatus;
import com.amp.tms.hazelcast.AbstractEntryProcessor;
import com.amp.tms.hazelcast.Configs;
import java.io.IOException;
import java.util.Map;
import javax.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * 
 */
@Service
public class FreeswitchNodeService {

    private static final Logger log = LoggerFactory.getLogger(FreeswitchNodeService.class);

    @Autowired
    private HazelcastService hazelcastService;

    private IMap<String, FreeswitchNode> freeswitchNodesMap;
    private IMap<Integer, String> freeswitchNodesToExtMap;
    private IMap<String, String> freeswitchNodesToCallMap;

    @PostConstruct
    private void init() {
        freeswitchNodesMap = hazelcastService.getMap(Configs.FREESWITCH_NODE_MAP);
        freeswitchNodesToExtMap = hazelcastService.getMap(Configs.FREESWITCH_NODE_TO_EXT_MAP);
        freeswitchNodesToCallMap = hazelcastService.getMap(Configs.FREESWITCH_NODE_TO_CALL_MAP);
    }

    public String createNewNode(FreeswitchNode freeswitchNode) {
        freeswitchNode.setStatus(FreeswitchNodeStatus.CREATING);
        if (freeswitchNode.getContext() == null) {
            freeswitchNode.setAll_contexts(true);
        }
        freeswitchNodesMap.put(freeswitchNode.getHostname(), freeswitchNode);
        return freeswitchNode.getHostname();
    }

    public void powerOffNode(String hostname) {
        freeswitchNodesMap.executeOnKey(hostname, new SetStatus(FreeswitchNodeStatus.POWERING_OFF));
    }

    public void deactivateNode(String hostname) {
        freeswitchNodesMap.executeOnKey(hostname, new SetStatus(FreeswitchNodeStatus.INACTIVE));
    }

    public void activateNode(String hostname) {
        log.info("Freeswitch node {} - Activating...", hostname);
        freeswitchNodesMap.executeOnKey(hostname, new SetStatus(FreeswitchNodeStatus.ACTIVE));
    }

//    public void releaseNodeForAgent(Integer ext) {
//        String hostname = freeswitchNodesToExtMap.remove(ext);
//        if (hostname != null) {
//            freeswitchNodesMap.executeOnKey(hostname, new DecrementUserCount());
//        }
//    }
    public void assignNodeForAgent(Integer ext, String hostname) {
        boolean doAssign = false;
        if (hostname != null) {
            doAssign = (Boolean) freeswitchNodesMap.executeOnKey(hostname, new IncrementUserCount());
        }
        String oldHostName = doAssign
                ? freeswitchNodesToExtMap.put(ext, hostname)
                : freeswitchNodesToExtMap.remove(ext);
        if (oldHostName != null) {
            freeswitchNodesMap.executeOnKey(oldHostName, new DecrementUserCount());
        }
    }

//    public FreeswitchNode getNextAvialableNodeForAgent(Integer ext) {
//        FreeswitchNode selectedNode = null;
//
//        String hostname = freeswitchNodesToExtMap.get(ext);
//        if (hostname != null) {
//            selectedNode = freeswitchNodesMap.get(hostname);
//            return selectedNode;
//        }
//
//        for (Map.Entry<String, FreeswitchNode> entrySet : freeswitchNodesMap.entrySet()) {
//
//            FreeswitchNode freeswitchNode = entrySet.getValue();
//            if (freeswitchNode.getContext() == FreeswitchContext.agent_dp) {
//                if (selectedNode == null) {
//                    selectedNode = freeswitchNode;
//                } else if (selectedNode.getActive_users() > freeswitchNode.getActive_users()) {
//                    selectedNode = freeswitchNode;
//                }
//            }
//        }
//
//        return selectedNode;
//
//    }
    public String releaseNode(String call_uuid) {
        if (call_uuid == null) {
            return null;
        }
        String hostname = freeswitchNodesToCallMap.remove(call_uuid);
        if (hostname != null) {
            freeswitchNodesMap.submitToKey(hostname, new DecrementCallCount());
        }

        return hostname;
    }

//    public FreeswitchNode getNodeForAgent(Integer ext) {
//        String hostname = freeswitchNodesToExtMap.get(ext);
//        return freeswitchNodesMap.get(hostname);
//    }
    public FreeswitchNode getNextNodeForContext(String call_uuid, FreeswitchContext context) {
        try {
            freeswitchNodesToCallMap.lock(call_uuid);

            FreeswitchNode selectedNode = getNodeForCallUUID(call_uuid);
            if (selectedNode == null) {
                selectedNode = findNextNode(context);
                if (selectedNode != null) {
                    freeswitchNodesMap.submitToKey(selectedNode.getHostname(), new IncrementCallCount());
                    freeswitchNodesToCallMap.set(call_uuid, selectedNode.getHostname());
                    log.info("Retruning Freeswitch node {} - {}", call_uuid, selectedNode.toJson());
                }
            }
            return selectedNode;
        } finally {
            freeswitchNodesToCallMap.unlock(call_uuid);
        }
    }

    private FreeswitchNode findNextNode(FreeswitchContext context) {
        FreeswitchNode selectedNode = null;
        for (FreeswitchNode freeswitchNode : freeswitchNodesMap.values(new NextNodePredicate(context))) {
            if (selectedNode == null
                    || selectedNode.getPriority() > freeswitchNode.getPriority()
                    || selectedNode.getActive_calls() > freeswitchNode.getActive_calls()) {
                selectedNode = freeswitchNode;
            }
        }

        if (selectedNode == null) {
            //try to find a backup node in the local entries
            for (String key : freeswitchNodesMap.localKeySet(new ActiveNodePredicate())) {
                selectedNode = freeswitchNodesMap.get(key);
                if (selectedNode != null) {
                    return selectedNode;
                }
            }
            //now try to find a backup node on the external entries
            for (FreeswitchNode freeswitchNode : freeswitchNodesMap.values(new ActiveNodePredicate())) {
                return freeswitchNode;
            }
        }

        return selectedNode;
    }

    public String dumpNodes() {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, FreeswitchNode> entrySet : freeswitchNodesMap.entrySet()) {
            String hostname = entrySet.getKey();
            FreeswitchNode freeswitchNode = entrySet.getValue();
            log.info("--------------------------- {} -------------------", hostname);
            sb.append(freeswitchNode.toJson());

            log.info(freeswitchNode.toJson());
        }
        return sb.toString();
    }

    public FreeswitchNode getNode(String hostname) {
        return freeswitchNodesMap.get(hostname);
    }

    public FreeswitchNode getNodeForCallUUID(String call_uuid) {
        String hostname = freeswitchNodesToCallMap.get(call_uuid);
        if (hostname != null) {
            FreeswitchNode freeswitchNode = freeswitchNodesMap.get(hostname);
            return freeswitchNode;
        }
        return null;
    }

    private static class ActiveNodePredicate implements Predicate<String, FreeswitchNode>, DataSerializable {

        @Override
        public boolean apply(Map.Entry<String, FreeswitchNode> mapEntry) {
            return mapEntry.getValue().getStatus() == FreeswitchNodeStatus.ACTIVE;
        }

        @Override
        public void writeData(ObjectDataOutput out) throws IOException {
        }

        @Override
        public void readData(ObjectDataInput in) throws IOException {
        }

    }

    private static class NextNodePredicate implements Predicate<String, FreeswitchNode>, DataSerializable {

        private FreeswitchContext context;

        private NextNodePredicate() {
        }

        public NextNodePredicate(FreeswitchContext context) {
            this.context = context;
        }

        @Override
        public boolean apply(Map.Entry<String, FreeswitchNode> mapEntry) {
            FreeswitchNode freeswitchNode = mapEntry.getValue();
            return freeswitchNode.getStatus() == FreeswitchNodeStatus.ACTIVE
                    && (freeswitchNode.getContext() == context || freeswitchNode.isAll_contexts())
                    && freeswitchNode.getActive_calls() <= freeswitchNode.getMax_calls_threshold()
                    && freeswitchNode.getActive_calls() < freeswitchNode.getMax_calls_allowed();
        }

        @Override
        public void writeData(ObjectDataOutput out) throws IOException {
            out.writeObject(context);
        }

        @Override
        public void readData(ObjectDataInput in) throws IOException {
            context = in.readObject();
        }

    }

    private static class IncrementUserCount extends AbstractEntryProcessor<String, FreeswitchNode> {

        public IncrementUserCount() {
        }

        @Override
        public Object process(Map.Entry<String, FreeswitchNode> entry, boolean isPrimary) {
            FreeswitchNode freeswitchNode = entry.getValue();
            if (freeswitchNode != null) {
                freeswitchNode.increaseUserCount();
                entry.setValue(freeswitchNode);
                return true;
            }
            return false;
        }

    }

    private static class DecrementUserCount extends AbstractEntryProcessor<String, FreeswitchNode> {

        public DecrementUserCount() {
        }

        @Override
        public Object process(Map.Entry<String, FreeswitchNode> entry, boolean isPrimary) {
            FreeswitchNode freeswitchNode = entry.getValue();
            if (freeswitchNode != null) {
                freeswitchNode.decreaseUserCount();
                entry.setValue(freeswitchNode);
                return true;
            }
            return false;
        }

    }

    private static class IncrementCallCount extends AbstractEntryProcessor<String, FreeswitchNode> {

        public IncrementCallCount() {
        }

        @Override
        public Object process(Map.Entry<String, FreeswitchNode> entry, boolean isPrimary) {
            FreeswitchNode freeswitchNode = entry.getValue();
            if (freeswitchNode != null) {
                freeswitchNode.increaseCallCount();
                entry.setValue(freeswitchNode);
                return true;
            }
            return false;
        }

    }

    private static class DecrementCallCount extends AbstractEntryProcessor<String, FreeswitchNode> {

        public DecrementCallCount() {
        }

        @Override
        public Object process(Map.Entry<String, FreeswitchNode> entry, boolean isPrimary) {
            FreeswitchNode freeswitchNode = entry.getValue();
            if (freeswitchNode != null) {
                freeswitchNode.decreaseCallCount();
                entry.setValue(freeswitchNode);
                return true;
            }
            return false;
        }

    }

    private static class SetStatus extends AbstractEntryProcessor<String, FreeswitchNode> {

        private FreeswitchNodeStatus status;

        public SetStatus() {
        }

        public SetStatus(FreeswitchNodeStatus status) {
            this.status = status;
        }

        @Override
        public Object process(Map.Entry<String, FreeswitchNode> entry, boolean isPrimary) {
            FreeswitchNode freeswitchNode = entry.getValue();
            freeswitchNode.setStatus(status);
            entry.setValue(freeswitchNode);
            return null;
        }

        @Override
        public void writeData(ObjectDataOutput out) throws IOException {
            super.writeData(out);
            out.writeObject(status);
        }

        @Override
        public void readData(ObjectDataInput in) throws IOException {
            super.readData(in);
            status = in.readObject();
        }

    }

}
