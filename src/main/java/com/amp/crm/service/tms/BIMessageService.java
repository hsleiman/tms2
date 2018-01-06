
package com.amp.crm.service.tms;

import com.amp.crm.db.entity.base.dialer.BIMessage;
import com.amp.crm.db.entity.base.dialer.CallDetailRecord;
import com.amp.crm.db.repository.dialer.BIMessageRepository;
import com.amp.crm.db.repository.dialer.StiCallDetailRecordRepository;
import com.amp.crm.embeddable.BIPlaybackData;
import com.amp.crm.service.utility.DurationUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@Transactional
public class BIMessageService {
    
    @Autowired
    private StiCallDetailRecordRepository cdrRepo;

    @Autowired
    private BIMessageRepository biMessageRepository;

    private static final Logger LOG = LoggerFactory.getLogger(BIMessageService.class);

    public void saveBIMessage(BIMessage message) {
        biMessageRepository.persist(message);
    }

    public BIPlaybackData getBIPlaybackData(String callUUID) {
        if (StringUtils.isNotBlank(callUUID)) {
            callUUID = callUUID.trim();
            CallDetailRecord record = cdrRepo.locateCallDetailRecordByCallUUID(callUUID);
            BIPlaybackData data = new BIPlaybackData();
            if (record != null && record.getEndTime() != null) {
                data.setCallLength(DurationUtils.getDuration(record.getStartTime(), record.getEndTime()));
                data.setPlaybackElements(biMessageRepository.getPlaybackElements(record));
                data.setAudioUrl(record.getCallRecordingUrl());
            }
            return data;
        }
        return null;
    }

}
