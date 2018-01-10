/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.tms.service;

import com.hazelcast.core.IMap;
import com.objectbrains.hcms.hazelcast.HazelcastService;
import com.amp.crm.db.entity.base.dialer.DialerQueueSettings;
import com.amp.crm.db.entity.base.dialer.OutboundDialerQueueSettings;
import com.amp.crm.embeddable.AgentWeightPriority;
import com.amp.crm.embeddable.InboundDialerQueueRecord;
import com.amp.crm.embeddable.OutboundDialerQueueRecord;
import com.amp.crm.pojo.DialerQueueRecord;
import com.amp.crm.service.dialer.OutboundDialerService;
import com.amp.crm.service.tms.TMSService;
import com.amp.tms.db.repository.TmsDialerQueueRepository;
import com.amp.tms.enumerated.DialerType;
import com.amp.tms.hazelcast.AbstractEntryProcessor;
import com.amp.tms.hazelcast.Configs;
import com.amp.tms.hazelcast.entity.AgentTMS;
import com.amp.tms.hazelcast.entity.AgentWeightedPriority;
import com.amp.tms.hazelcast.entity.WeightedPriority;
import com.amp.tms.hazelcast.keys.AgentQueueKey;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
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
 * 
 */
@Service
public class DialerQueueRecordService {

    private static final Logger LOG = LoggerFactory.getLogger(DialerQueueRecordService.class);

    @Autowired
    private HazelcastService hazelcastService;

    @Autowired
    private AgentQueueAssociationService associationService;

    @Autowired
    private TmsDialerQueueRepository dialerQueueRepository;
   

    @Autowired
    private TMSService tmsIws;
    
    @Autowired 
    private OutboundDialerService outboundDialerService;

    @Autowired
    @Lazy
    private TMSAgentService agentService;

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
        for (Iterator it = record.getAgentWeightPriorityList().iterator(); it.hasNext();) {
            AgentWeightPriority agentWeightPriority = (AgentWeightPriority) it.next();
            String userName = agentWeightPriority.getUsername();
            Integer extension = nameToExtension.get(agentWeightPriority.getUsername());
            if (extension == null) {
                AgentTMS agent = agentService.getAgent(userName);
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
                OutboundDialerQueueSettings settings = record.getDialerQueueSettings();
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
                record = outboundDialerService.getDialerQueueRecord(queuePk);
            } catch (Exception ex) {
                LOG.error(ex.getMessage(), ex);
            }
            if (record != null) {
                recordMap.putAsync(queuePk, record);
            }
        }
        return record;
    }

    @SuppressWarnings("unchecked")
    public Map<Long, DialerQueueSettings> getQueueSettings(Set<Long> queuePks) {
        if (queuePks == null || queuePks.isEmpty()) {
            return Collections.emptyMap();
        }
        Set<Long> missingKeys = new HashSet<>(queuePks);
        Map<Long, DialerQueueSettings> resp = (Map) recordMap.executeOnKeys(queuePks, new GetQueueSettingsEntryProcessor());
        Map<Long, DialerQueueSettings> ret = new HashMap<>(resp);
        missingKeys.removeAll(resp.keySet());
        for (Long missingKey : missingKeys) {
            DialerQueueSettings settings = getSettings(getDialerQueueRecord(missingKey));
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

    public DialerQueueSettings getQueueSettings(Long queuePk) {
        if (queuePk == null) {
            return null;
        }
        DialerQueueSettings settings = (DialerQueueSettings) recordMap.executeOnKey(queuePk, new GetQueueSettingsEntryProcessor());
        if (settings == null) {
            settings = getSettings(getDialerQueueRecord(queuePk));
        }
        return settings;
    }

    private static DialerQueueSettings getSettings(DialerQueueRecord record) {
        if (record instanceof InboundDialerQueueRecord) {
            return ((InboundDialerQueueRecord) record).getDialerQueueSettings();
        } else if (record instanceof OutboundDialerQueueRecord) {
            return ((OutboundDialerQueueRecord) record).getDialerQueueSettings();
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
        public DialerQueueSettings process(Map.Entry<Long, DialerQueueRecord> entry, boolean isPrimary) {
            return getSettings(entry.getValue());
        }

    }

}
