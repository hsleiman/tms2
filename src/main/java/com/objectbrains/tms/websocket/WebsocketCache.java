/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.objectbrains.tms.websocket;


import com.objectbrains.sti.constants.DialerQueueType;
import com.objectbrains.sti.db.entity.base.dialer.StiCallerId;
import com.objectbrains.sti.embeddable.DialerQueueDetails;
import com.objectbrains.sti.service.dialer.DialerQueueService;
import com.objectbrains.tms.pojo.DialerQueueDetailPojo;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author hsleiman
 */
@Service
public class WebsocketCache {

    private static final Logger LOG = LoggerFactory.getLogger(WebsocketCache.class);

    @Autowired
    private DialerQueueService dialerQueueService;

    private List<DialerQueueDetailPojo> dialerQueueDetailPojos;
    private long dialerQueueDetailPojosTimestamp = 0l;

    private List<Long> callerIdNumbers;
    private long callerIdNumbersTimestamp = 0l;

    public List<DialerQueueDetailPojo> getDialerQueueDetailPojos() {
        if (isDialerQueueDetailPojosExpired()) {
            return buildDialerQueueDetailPojos();
        } else {
            return dialerQueueDetailPojos;
        }
    }

    public List<Long> getCallerIdNumbers() {
        if (isCallerIdNumbersExpired()) {
            return buildCallerIdNumbers();
        } else {
            return callerIdNumbers;
        }
    }

    private boolean isDialerQueueDetailPojosExpired() {
        return System.currentTimeMillis() - dialerQueueDetailPojosTimestamp > (1000 * 60 * 60);
    }

    private boolean isCallerIdNumbersExpired() {
        return System.currentTimeMillis() - callerIdNumbersTimestamp > (1000 * 60 * 60 * 3);
    }

    public void rebuildAll() {
        callerIdNumbersTimestamp = 0l;
        LOG.info("Rebuilding Caller ID..");
        buildCallerIdNumbers();
        dialerQueueDetailPojosTimestamp = 0l;
        LOG.info("Rebuilding Agent Queue..");
        buildDialerQueueDetailPojos();
        LOG.info("Done.");
    }

    private synchronized List<DialerQueueDetailPojo> buildDialerQueueDetailPojos() {
        if (isDialerQueueDetailPojosExpired()) {
            LOG.info("Rebuilding Agent Queue..");
            List<DialerQueueDetailPojo> pojos = new ArrayList<>();
            List<DialerQueueDetails> dialerQueues = dialerQueueService.getAllDialerQueues();
            for (int i = 0; i < dialerQueues.size(); i++) {
                DialerQueueDetails get = dialerQueues.get(i);
                if (get != null && get.isActive() && get.getDialerQueueType() == DialerQueueType.INBOUND) {
                    pojos.add(new DialerQueueDetailPojo(get.getQueueName(), get.getPk()));
                } else {
                    LOG.info("Skiping Queue {}", get);
                }
            }
            dialerQueueDetailPojos = pojos;
            dialerQueueDetailPojosTimestamp = System.currentTimeMillis();
        }
        return dialerQueueDetailPojos;
    }

    private synchronized List<Long> buildCallerIdNumbers() {
        if (isCallerIdNumbersExpired()) {
            LOG.info("Rebuilding Caller ID..");
            List<StiCallerId> callerIds = dialerQueueService.getAllCallerIds();
            ArrayList<Long> numbers = new ArrayList<>();
            for (int i = 0; i < callerIds.size(); i++) {
                StiCallerId get = callerIds.get(i);
                numbers.add(get.getCallerIdNumber());
            }
            callerIdNumbers = numbers;
            callerIdNumbersTimestamp = System.currentTimeMillis();
        }
        return callerIdNumbers;
    }

}
