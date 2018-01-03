package com.objectbrains.sti.service.dialer;

import com.objectbrains.sti.service.dialer.DialerQueueService;
import com.objectbrains.sti.service.dialer.DialerAccountPhoneData;
import com.hazelcast.core.IQueue;
import com.objectbrains.hcms.annotation.ConfigContext;
import com.objectbrains.hcms.configuration.ConfigurationUtility;
import com.objectbrains.hcms.hazelcast.HazelcastService;
import com.objectbrains.sti.constants.OutboundRecordStatus;
import com.objectbrains.sti.db.entity.base.dialer.DialerQueue;
import com.objectbrains.sti.db.repository.dialer.DialerQueueRepository;
import com.objectbrains.sti.embeddable.InboundDialerQueueRecord;
import com.objectbrains.sti.embeddable.OutboundDialerQueueRecord;
import com.objectbrains.sti.exception.StiException;
import com.objectbrains.sti.hazelcast.Config;
import com.objectbrains.sti.pojo.DialerQueueAccountDetails;
import com.objectbrains.sti.pojo.DialerQueueRecord;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class OutboundDialerService {
    @Autowired
    private DialerQueueRepository dqRepo;
    @Autowired
    private DialerQueueService dqService;
    @Autowired
    private HazelcastService hzService;
    @ConfigContext
    private ConfigurationUtility configUtil;
    @Autowired
    private DialerAccountPhoneData dialerAccountPhoneData;
    
    private static final Logger LOG = LoggerFactory.getLogger(OutboundDialerService.class);
    
    public DialerQueueRecord getDialerQueueRecord(long dqPk) throws StiException {
        DialerQueue queue = dqRepo.locateDialerQueueByPk(dqPk);
        if (queue.isOutbound()) {
            return (OutboundDialerQueueRecord) getOutboundDialerQueueRecord(dqPk);
        } else {
            return (InboundDialerQueueRecord) dqService.getInboundDialerQueueRecord(dqPk);
        }
    }
        
    public OutboundDialerQueueRecord getOutboundDialerQueueRecord(long dqPk) throws StiException {
        if(isLegacyCodeOn()){
            return getRecordViaPlanA(dqPk);
        }
        if(isOptimizedWayOn()){
            return getRecordViaOptimizedWayPlanB(dqPk);
        }
        if(isHazelcastOn()){
            return getRecordViaHazelcastPlanD(dqPk);
        }           
        return null;
    }
    
    public DialerQueueAccountDetails getDialerQueueAccountDetails(long accountPk) throws StiException {
        List<DialerQueueAccountDetails> list = dialerAccountPhoneData.getDialerQueueAccountDetailsViaPlanA(Arrays.asList(accountPk));
        return list.isEmpty() ? null : list.get(0);
    }
   
    public boolean isHazelcastOn(){
        return configUtil.getBoolean("get.outbound.record.via.hazelcast.planD", Boolean.FALSE);
    }
    
    public boolean isLegacyCodeOn(){
        return configUtil.getBoolean("get.outbound.record.via.legacy.code.planA", Boolean.TRUE);
    }
    
    public boolean isOptimizedWayOn(){
        return configUtil.getBoolean("get.outbound.record.via.optimized.code.planB", Boolean.FALSE);
    }
    
    public boolean useDialerQueueInDynamicCodeInPlanAOn(){
        return configUtil.getBoolean("get.outbound.record.planA.via.dialerQueuePk.in.dynamic.code", Boolean.TRUE);
    }
    
    public boolean useDialerQueueInDynamicCodeInPlanBOn(){
        return configUtil.getBoolean("get.outbound.record.planB.via.dialerQueuePk.in.dynamic.code", Boolean.TRUE);
    }

    private OutboundDialerQueueRecord getRecordViaPlanA(long dqPk) throws StiException {
        LocalDateTime start = new LocalDateTime();
        OutboundDialerQueueRecord dqRecord = new OutboundDialerQueueRecord(dqPk);
        DialerQueue queue = dqService.instantiateDialerQueueRecord(dqPk, dqRecord);
        LOG.info("PlanA Instantiate DialerQueueRecord took {} msec for dialerQueuePk {}", (new LocalDateTime().getMillisOfDay() - start.getMillisOfDay()), dqPk);
        if(useDialerQueueInDynamicCodeInPlanAOn()){
            dqService.getDialerQueueAccounts(queue);
            dqRecord.setAccountDetails(dialerAccountPhoneData.getDialerQueueAccountDetailsViaPlanA(queue.getPk()));
        }else{
            dqRecord.setAccountDetails(dialerAccountPhoneData.getDialerQueueAccountDetailsViaPlanA(dqService.getDialerQueueAccounts(queue)));
        }
        LOG.info("getOutboundDialerQueueRecord for queue {} via plan A took {} msec", dqPk, (new LocalDateTime().getMillisOfDay() - start.getMillisOfDay()));
        return dqRecord;
    }

    private OutboundDialerQueueRecord getRecordViaOptimizedWayPlanB(long dqPk) throws StiException {
        LocalDateTime start = new LocalDateTime();
        OutboundDialerQueueRecord dqRecord = new OutboundDialerQueueRecord(dqPk);
        DialerQueue queue = dqService.instantiateDialerQueueRecord(dqPk, dqRecord);
        LOG.info("PlanB Instantiate DialerQueueRecord took {} msec for dialerQueuePk {}", (new LocalDateTime().getMillisOfDay() - start.getMillisOfDay()), dqPk);
        if(useDialerQueueInDynamicCodeInPlanBOn()){
            dqService.getDialerQueueAccounts(queue);
            dqRecord.setAccountDetails(dialerAccountPhoneData.getDialerQueueAccountDetailsViaPlanB(queue.getPk()));
        }else{
            dqRecord.setAccountDetails(dialerAccountPhoneData.getDialerQueueAccountDetailsViaPlanB(dqService.getDialerQueueAccounts(queue)));
        }
        LOG.info("getOutboundDialerQueueRecord for queue {} via plan B took {} msec", dqPk, (new LocalDateTime().getMillisOfDay() - start.getMillisOfDay()));
        return dqRecord;
    }

    private OutboundDialerQueueRecord getRecordViaHazelcastPlanD(long dqPk) throws StiException {
        OutboundDialerQueueRecord dqRecord = new OutboundDialerQueueRecord(dqPk);
        DialerQueue queue = dqService.instantiateDialerQueueRecord(dqPk, dqRecord);
        dqRecord.setStatus(OutboundRecordStatus.PENDING);
        Config.getOutboundDialerStatus(hzService, queue.getPk()).clear();
        Config.getDialerAccountDetailQueue(hzService, queue.getPk()).clear();
        dialerAccountPhoneData.saveDialerQueueAccountDetailsToHazelcast(dqService.getDialerQueueAccounts(queue));
        return dqRecord;
    }
    
    public OutboundDialerQueueRecord getHazelcastOutboundRecord(long dqPk) throws StiException {
        LocalDateTime start = new LocalDateTime();
        OutboundDialerQueueRecord dqRecord = new OutboundDialerQueueRecord(dqPk);
        LOG.info("Instantiate DialerQueueRecord took {} msec for dialerQueuePk {}", (new LocalDateTime().getMillisOfDay() - start.getMillisOfDay()), dqPk);
        //returns Accounts from Hazelcast queue
        List<DialerQueueAccountDetails> dqDetails = new ArrayList<>();
        IQueue<DialerQueueAccountDetails> dialerAccountDetailQueue = Config.getDialerAccountDetailQueue(hzService, dqPk);
        LOG.info("Hazelcast queue for dialerQueuePk {} contains {} entries", dqPk, dialerAccountDetailQueue.size());
        dialerAccountDetailQueue.drainTo(dqDetails);
        dqRecord.setAccountDetails(dqDetails);
        dqRecord.setStatus(Config.getOutboundDialerStatus(hzService, dqPk).get(dqPk));
        LOG.info("getHazelcastOutboundRecord for queue {} took {} msec", dqPk, (new LocalDateTime().getMillisOfDay() - start.getMillisOfDay()));
        return dqRecord;
    }
}
