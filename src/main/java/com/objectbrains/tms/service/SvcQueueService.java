/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.objectbrains.tms.service;

import com.objectbrains.scheduler.annotation.Sync;
import com.objectbrains.sti.db.entity.base.dialer.DialerQueueSettings;
import com.objectbrains.sti.service.dialer.DialerQueueService;
import com.objectbrains.sti.service.tms.TMSService;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author Connor Petty <cpmeister@users.sourceforge.net>
 */
@Service
@Sync
public class SvcQueueService {

    private static final Logger LOG = LoggerFactory.getLogger(SvcQueueService.class);

//    @Autowired
//    private TMSService tmsIws;

    @Autowired
    private DialerQueueService dialerQueueService;

    public List<DialerQueueSettings> getQueueSettings(Set<Long> queuePks) {
        List<DialerQueueSettings> ret = new ArrayList<>();
        for (Long queuePk : queuePks) {
            DialerQueueSettings settings = getQueueSettings(queuePk);
            if (settings != null) {
                ret.add(settings);
            }
        }
        return ret;
    }

    public DialerQueueSettings getQueueSettings(Long queuePk) {
        if (queuePk != null) {
            try {
                return dialerQueueService.getDialerQueueSettingsByDQPk(queuePk);
            } catch (Exception ex) {
                LOG.error(ex.getMessage());
            }
        }
        return null;
    }

}
