/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.objectbrains.tms.service;

import com.objectbrains.scheduler.annotation.Sync;
import com.objectbrains.svc.iws.SvDialerQueueSettings;
import com.objectbrains.svc.iws.SvcException;
import com.objectbrains.svc.iws.TMSServiceIWS;
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
    
    @Autowired
    private TMSServiceIWS tmsIws;

    public List<SvDialerQueueSettings> getQueueSettings(Set<Long> queuePks) {
        List<SvDialerQueueSettings> ret = new ArrayList<>();
        for (Long queuePk : queuePks) {
            SvDialerQueueSettings settings = getQueueSettings(queuePk);
            if (settings != null) {
                ret.add(settings);
            }
        }
        return ret;
    }

    public SvDialerQueueSettings getQueueSettings(Long queuePk) {
        if (queuePk != null) {
            try {
                return tmsIws.getDialerQueueSettingsByDQPk(queuePk);
            } catch (SvcException ex) {
                LOG.error(ex.getMessage());
            }
        }
        return null;
    }

}
