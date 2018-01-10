/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.tms.service;

import com.hazelcast.core.IMap;
import com.hazelcast.query.Predicate;
import com.objectbrains.hcms.hazelcast.HazelcastService;
import com.amp.tms.db.entity.DNC;
import com.amp.tms.hazelcast.Configs;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import javax.annotation.PostConstruct;
import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 *
 * 
 */
@Service
public class DncService {

    private static final Logger LOG = LoggerFactory.getLogger(DncService.class);

    @Autowired
    private HazelcastService hazelcastService;

    private IMap<String, DNC> dncMap;

    @PostConstruct
    private void init() {
        dncMap = hazelcastService.getMap(Configs.DNC_MAP);
    }

    public DNC createDNC(String phoneNumber, String reason, LocalDateTime expireTime) {
        DNC dnc = new DNC(phoneNumber, reason, expireTime);
        DNC old = dncMap.putIfAbsent(phoneNumber, dnc);
        if (old == null) {
            return dnc;
        } else {
            return null;
        }
    }

    public boolean isInDNC(String phoneNumber) {
        return dncMap.containsKey(phoneNumber);
    }

    public Collection<DNC> getAllDNC() {
        return dncMap.values();
    }

    @Scheduled(initialDelay = 10000, fixedRate = 1000)
    public void runSweep() {
        LOG.debug("Removing Expired DNC..");
        if (hazelcastService.getLifecycleService().isRunning()) {
            Set<String> keysToEvict = dncMap.localKeySet(new ExpiredDncPredicate());
            for (String key : keysToEvict) {
                LOG.info("DNC for Phone number [{}] has expired, evicting from hazelcast", key);
                dncMap.delete(key);
            }
        }
    }

    public static class ExpiredDncPredicate implements Predicate<String, DNC> {

        private transient final LocalDateTime now = LocalDateTime.now();

        @Override
        public boolean apply(Map.Entry<String, DNC> mapEntry) {
            DNC dnc = mapEntry.getValue();
            LocalDateTime expireTime = dnc.getExpireTimestamp();
            return expireTime != null && now.isAfter(expireTime);
        }

    }
}
