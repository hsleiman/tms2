/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.crm.db.repository.dialer;

import com.objectbrains.hcms.annotation.ConfigContext;
import com.objectbrains.hcms.configuration.ConfigurationUtility;
import com.amp.crm.db.entity.base.dialer.BIMessage;
import com.amp.crm.db.entity.base.dialer.CallDetailRecord;
import com.amp.crm.embeddable.BIPlaybackData.PlaybackElement;
import com.amp.crm.service.utility.DurationUtils;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.joda.time.Duration;
import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

@Repository
public class BIMessageRepository {
    
     @PersistenceContext
    private EntityManager entityManager;
    @ConfigContext
    private ConfigurationUtility config;

    private static final Logger LOG = LoggerFactory.getLogger(BIMessageRepository.class);

    public void persist(BIMessage message) {
        entityManager.persist(message);
    }
    
     public List<PlaybackElement> getPlaybackElements(CallDetailRecord record) {
        List<Object[]> results = entityManager.createQuery(
                "select bi.extension, bi.imgUrl, bi.createTimestamp"
                + " from BIMessage bi where bi.callUUID = :callUUID and bi.imgUrl is not null order by bi.createTimestamp", Object[].class)
                .setParameter("callUUID", record.getCallUUID())
                .getResultList();
        List<PlaybackElement> elements = new ArrayList<>();
        Long callLengthMillis = DurationUtils.getDuration(record.getStartTime(), record.getEndTime()).getMillis();
        Long recMillis = 0L;
        LOG.info("Total playback elements: {} call duration: {}", results.size(), callLengthMillis);
        for (Object[] result : results) {
            //2mins after the call duration.
            LocalDateTime timeStamp = (LocalDateTime) result[2];
            Duration callTime = DurationUtils.getDuration(record.getStartTime(), timeStamp);
            if(recMillis >= callLengthMillis && callTime.getMillis() > (callLengthMillis+config.getLong("playback.data.duration.in.minutes", 120000L))){
                break;
            }
            recMillis = callTime.getMillis();
            PlaybackElement element = new PlaybackElement();
            element.setExt((Integer) result[0]);
            element.setImgUrl((String) result[1]);
            element.setCallTime(callTime);
            elements.add(element);
        }
        LOG.info("Final elements: {} recMillis: {}", elements.size(), recMillis);
        return elements;
    }
}
