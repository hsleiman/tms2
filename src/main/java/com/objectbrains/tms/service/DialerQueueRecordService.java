/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.objectbrains.tms.service;

import com.hazelcast.core.IMap;
import com.objectbrains.hcms.hazelcast.HazelcastService;
import com.objectbrains.svc.iws.AgentWeightPriority;
import com.objectbrains.svc.iws.DialerQueueRecord;
import com.objectbrains.svc.iws.InboundDialerQueueRecord;
import com.objectbrains.svc.iws.OutboundDialerQueueRecord;
import com.objectbrains.svc.iws.SvDialerQueueSettings;
import com.objectbrains.svc.iws.SvOutboundDialerQueueSettings;
import com.objectbrains.svc.iws.SvcException;
import com.objectbrains.svc.iws.TMSServiceIWS;
import com.objectbrains.tms.db.repository.DialerQueueRepository;
import com.objectbrains.tms.enumerated.DialerType;
import com.objectbrains.tms.hazelcast.AbstractEntryProcessor;
import com.objectbrains.tms.hazelcast.Configs;
import com.objectbrains.tms.hazelcast.entity.Agent;
import com.objectbrains.tms.hazelcast.entity.AgentWeightedPriority;
import com.objectbrains.tms.hazelcast.entity.WeightedPriority;
import com.objectbrains.tms.hazelcast.keys.AgentQueueKey;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 *
 * @author connorpetty
 */
@Service
public class DialerQueueRecordService {

    private static final Logger LOG = LoggerFactory.getLogger(DialerQueueRecordService.class);

    @Autowired
    private HazelcastService hazelcastService;

    @Autowired
    private AgentQueueAssociationService associationService;

    @Autowired
    private DialerQueueRepository dialerQueueRepository;

    @Autowired
    private TMSServiceIWS tmsIws;

    @Autowired
    @Lazy
    private AgentService agentService;

    private IMap<Long, DialerQueueRecord> recordMap;

    @PostConstruct
    private void init() {
        recordMap = hazelcastService.getMap(Configs.DIALER_QUEUE_RECORD_MAP);
    }

    private void setAgentQueueAssociations(long queuePk, DialerType type, DialerQueueRecord record) {
        List<Integer> extensions = new ArrayList<>();
        List<AgentWeightedPriority> weightPriorities = new ArrayList<>();
        Map<String, Integer> nameToExtension = new HashMap<>();
        Set<AgentQueueKey> agentQueueKeys = new HashSet<>();
        for (AgentWeightPriority agentWeightPriority : record.getAgentWeightPriorityList()) {
            String userName = agentWeightPriority.getUsername();
            Integer extension = nameToExtension.get(agentWeightPriority.getUsername());
            if (extension == null) {
                Agent agent = agentService.getAgent(userName);
                if (agent == null) {
                    //what now?
                    continue;
                }
                extension = agent.getExtension();
                nameToExtension.put(userName, extension);
            }
            agentQueueKeys.add(new AgentQueueKey(extension, queuePk));
            extensions.add(extension);
            weightPriorities.add(new AgentWeightedPriority(agentWeightPriority));
        }
        dialerQueueRepository.update(queuePk, type, record.getWeightedPriority());
        associationService.setAgentQueueAssociations(queuePk, extensions, weightPriorities);
        associationService.setAssociationDialerType(queuePk, type);
    }

    @Async
    public void storeInboundDialerQueueRecord(InboundDialerQueueRecord record) {
        long queuePk = record.getDqPk();
        if (recordMap.tryLock(queuePk)) {
            try {
                recordMap.put(queuePk, record);
                setAgentQueueAssociations(queuePk, DialerType.INBOUND, record);
            } finally {
                recordMap.unlock(queuePk);
            }
        }
    }

    public void storeOutboundDialerQueueRecord(OutboundDialerQueueRecord record) {
        long queuePk = record.getDqPk();
        if (recordMap.tryLock(queuePk)) {
            try {
                recordMap.put(queuePk, record);
                SvOutboundDialerQueueSettings settings = record.getSvDialerQueueSettings();
                setAgentQueueAssociations(queuePk, DialerType.valueFrom(settings), record);
            } finally {
                recordMap.unlock(queuePk);
            }
        }
    }

    public DialerQueueRecord getDialerQueueRecord(long queuePk) {
//        if(queuePk == Constants.TRANSFER_QUEUE_PK) return null;
        DialerQueueRecord record = recordMap.get(queuePk);
        if (record == null) {
            try {
                record = tmsIws.getDialerQueueRecord(queuePk);
            } catch (SvcException | RuntimeException ex) {
                LOG.error(ex.getMessage(), ex);
            }
            if (record != null) {
                recordMap.putAsync(queuePk, record);
            }
        }
        return record;
    }

    @SuppressWarnings("unchecked")
    public Map<Long, SvDialerQueueSettings> getQueueSettings(Set<Long> queuePks) {
        if (queuePks == null || queuePks.isEmpty()) {
            return Collections.emptyMap();
        }
        Set<Long> missingKeys = new HashSet<>(queuePks);
        Map<Long, SvDialerQueueSettings> resp = (Map) recordMap.executeOnKeys(queuePks, new GetQueueSettingsEntryProcessor());
        Map<Long, SvDialerQueueSettings> ret = new HashMap<>(resp);
        missingKeys.removeAll(resp.keySet());
        for (Long missingKey : missingKeys) {
            SvDialerQueueSettings settings = getSettings(getDialerQueueRecord(missingKey));
            if (settings != null) {
                ret.put(missingKey, settings);
            }
        }
        return ret;
//        Set<SvDialerQueueSettings> settings = new HashSet<>();
//        for (Long queuePk : queuePks) {
//            if (queuePk != null) {
//                DialerQueueRecord record = recordMap.get(queuePk);
//                if (record instanceof InboundDialerQueueRecord) {
//                    settings.add(((InboundDialerQueueRecord) record).getSvDialerQueueSettings());
//                } else if (record instanceof OutboundDialerQueueRecord) {
//                    settings.add(((OutboundDialerQueueRecord) record).getSvDialerQueueSettings());
//                }
//            }
//        }
//        return settings;
    }

    public SvDialerQueueSettings getQueueSettings(Long queuePk) {
        if (queuePk == null) {
            return null;
        }
        SvDialerQueueSettings settings = (SvDialerQueueSettings) recordMap.executeOnKey(queuePk, new GetQueueSettingsEntryProcessor());
        if (settings == null) {
            settings = getSettings(getDialerQueueRecord(queuePk));
        }
        return settings;
    }

    private static SvDialerQueueSettings getSettings(DialerQueueRecord record) {
        if (record instanceof InboundDialerQueueRecord) {
            return ((InboundDialerQueueRecord) record).getSvDialerQueueSettings();
        } else if (record instanceof OutboundDialerQueueRecord) {
            return ((OutboundDialerQueueRecord) record).getSvDialerQueueSettings();
        }
        return null;
    }

    public static class DeleteAgentQueueAssociations extends
            AbstractEntryProcessor<AgentQueueKey, WeightedPriority> {

        private long queuePk;

        public DeleteAgentQueueAssociations() {
        }

        public DeleteAgentQueueAssociations(long queuePk) {
            this.queuePk = queuePk;
        }

        @Override
        public Object process(Map.Entry<AgentQueueKey, WeightedPriority> entry, boolean isPrimary) {
            if (entry.getKey().getQueuePk() == queuePk) {
                //delete this entry
                entry.setValue(null);
            }
            return null;
        }

    }

    public static class DeleteEntryProcessor extends
            AbstractEntryProcessor<AgentQueueKey, WeightedPriority> {

        @Override
        public Object process(Map.Entry<AgentQueueKey, WeightedPriority> entry, boolean isPrimary) {
            entry.setValue(null);
            return null;
        }

    }

    private static class GetQueueSettingsEntryProcessor extends AbstractEntryProcessor<Long, DialerQueueRecord> {

        public GetQueueSettingsEntryProcessor() {
            super(false);
        }

        @Override
        public SvDialerQueueSettings process(Map.Entry<Long, DialerQueueRecord> entry, boolean isPrimary) {
            return getSettings(entry.getValue());
        }

    }

}
