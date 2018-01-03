/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.objectbrains.sti.service.dialer;

import com.hazelcast.core.IMap;
import com.hazelcast.core.IQueue;
import com.objectbrains.hcms.annotation.ConfigContext;
import com.objectbrains.hcms.configuration.ConfigurationUtility;
import com.objectbrains.hcms.hazelcast.HazelcastService;
import com.objectbrains.sti.constants.CallTimeCode;
import com.objectbrains.sti.constants.OutboundRecordStatus;
import com.objectbrains.sti.db.entity.base.account.Account;
import com.objectbrains.sti.db.repository.account.AccountRepository;
import com.objectbrains.sti.db.repository.dialer.DialerQueueRepository;
import com.objectbrains.sti.hazelcast.Config;
import com.objectbrains.sti.pojo.BasicPhoneData;
import com.objectbrains.sti.pojo.CustomerPhoneData;
import com.objectbrains.sti.pojo.DialerQueueAccountDetails;
import com.objectbrains.sti.service.utility.PhoneUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;
import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.stereotype.Component;

/**
 *
 * @author David
 */
@Component
public class DialerAccountDetailsPlanD {
    @Autowired
    @Qualifier("sti-executor")
    private AsyncTaskExecutor executor;
    @ConfigContext
    private ConfigurationUtility config;
    @Autowired
    private HazelcastService hzService;
    @Autowired
    private AccountRepository accountRepo;
    @Autowired
    private DialerQueueRepository dqRepo;
    
    private static final Logger LOG = LoggerFactory.getLogger(DialerAccountDetailsPlanD.class);
    
    //Optimized because 1. the executor works on batches instead of a single account and 2. the phoneCallable is cached.
    List<DialerQueueAccountDetails> saveDialerQueueAccountDetailsViaPlanD(List<DialerAccountPhoneData.AccountPhoneData> accountPhoneDataList) {//throws InterruptedException {
        LocalDateTime start = new LocalDateTime();
        List<Future<List<DialerQueueAccountDetails>>> futureList = new ArrayList<>();
        ConcurrentHashMap<DialerAccountPhoneData.ZipAreaCode, PhoneNumberCallable> zipPhoneCache = new ConcurrentHashMap<>();
        boolean debug = config.getBoolean("tms.log.account.phone.data", Boolean.TRUE);
        // Divide the list into batches
        int partitionCount = config.getInteger("buildDialerQueueAccountDetails.partitionCount", 10);
        int size = accountPhoneDataList.size();
        int minPartitionSize = size / partitionCount;
        int remainingPartitionSize = size % partitionCount;
        Account account = accountRepo.findAccountByPk(accountPhoneDataList.get(0).getAccountPk());
        long dqPk = account.getOutboundDialerQueue().getPk();
        LOG.info("Plan D Dynamic code returned : {} phones; partitionCount : {}; MinPartitionSize : {}; remainingPartitionSize : {} ",size, partitionCount, minPartitionSize, remainingPartitionSize);
        IMap<Long, OutboundRecordStatus> dialerStatusMap = Config.getOutboundDialerStatus(hzService, dqPk);
        for (int i = 0, startIndex = 0; i < partitionCount; i++) {            
            //Divide the remaining entries among the batches created. 
            int partitionSize = minPartitionSize + ((i < remainingPartitionSize) ? 1 : 0);
            int endIndex = (startIndex+ partitionSize > size) ? size : startIndex+ partitionSize;//Math.max(partitionSize, startIndex + 1);
            LOG.info("Plan D Batch {} partitionSize {}  startIndex {} endIndex {} ", i, partitionSize, startIndex, endIndex);
            if (startIndex < size) {
                long pk = accountPhoneDataList.get(endIndex-1).getAccountPk();
                long nextPk = 0;
                if(endIndex < size){
                    nextPk = accountPhoneDataList.get(endIndex).getAccountPk();
                }
                LOG.info("Plan D Last accountPk in batch {} is {} and the first accountPk in next batch is {}",i, pk, nextPk);
                while(endIndex < size && pk == nextPk){
                    LOG.info("Plan B pk : {}, nextPk : {} endIndex : {}", pk, nextPk, endIndex);
                    endIndex++;
                    nextPk = accountPhoneDataList.get(endIndex).getAccountPk();
                }
                int numberOfPhonesPerbatch = endIndex - startIndex;
                LOG.info("Plan D Number of phones in batch {} with startIndex {} and endIndex {} are {} for dqPk {}", i, startIndex, endIndex, numberOfPhonesPerbatch, dqPk);
                Future<List<DialerQueueAccountDetails>> future = executor.submit(
                        new DialerAccountDetailsPlanD.DialerQueueAccountDetailsCallable(accountPhoneDataList, startIndex, endIndex, zipPhoneCache, debug, dqPk));
                startIndex = endIndex;
                futureList.add(future);  
//                try {
//                    LOG.info("Plan D Batch {} returned {} account pojos from {} phones", i, future.get().size(), numberOfPhonesPerbatch);
//                } catch (InterruptedException | ExecutionException ex) {
//                    java.util.logging.Logger.getLogger(DialerAccountDetailsPlanD.class.getName()).log(Level.SEVERE, null, ex);
//                }
                dialerStatusMap.put(dqPk, OutboundRecordStatus.PARTIAL);
            } else {
                break;
            }           
        
        }
        LOG.info("Hazelcast map status for dqPk {} is {}. Setting it to complete", dqPk, dialerStatusMap.get(dqPk));
        dialerStatusMap.put(dqPk, OutboundRecordStatus.COMPLETE);
        List<DialerQueueAccountDetails> retList = new ArrayList<>();
        for(Future<List<DialerQueueAccountDetails>> f : futureList){
            try {
                retList.addAll(f.get());
            } catch (InterruptedException | ExecutionException ex) {
                java.util.logging.Logger.getLogger(DialerAccountDetailsPlanD.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        LOG.info("Plan D buildDialerQueueAccountDetails took {} msec to give {} pojos complete", (new LocalDateTime().getMillisOfDay() - start.getMillisOfDay()), retList.size());
        LOG.info(" Hazelcast queue contains {} account details for dqPk {} upon completion. Number of phones returned by dynamic code : {}",Config.getDialerAccountDetailQueue(hzService, dqPk).size(), dqPk, accountPhoneDataList.size());
        return retList;
    }

    private class DialerQueueAccountDetailsCallable implements Callable<List<DialerQueueAccountDetails>> {

        private List<DialerAccountPhoneData.AccountPhoneData> accountPhoneList;
        private int startIndex;
        private int endIndex;
        private boolean debug;
        private ConcurrentHashMap<DialerAccountPhoneData.ZipAreaCode, PhoneNumberCallable> zipPhoneCache;
        private Long dqPk;
        public DialerQueueAccountDetailsCallable(List<DialerAccountPhoneData.AccountPhoneData> accountPhoneList, int startIndex, int endIndex,
                ConcurrentHashMap<DialerAccountPhoneData.ZipAreaCode, PhoneNumberCallable> zipPhoneCache, boolean debug, Long dqPk) {
            this.accountPhoneList = accountPhoneList;
            this.startIndex = startIndex;
            this.endIndex = endIndex;
            this.zipPhoneCache = zipPhoneCache;
            this.debug = debug;
            this.dqPk = dqPk;
        }

        @Override
        public List<DialerQueueAccountDetails> call() throws Exception {
            List<DialerQueueAccountDetails> accountDetailList = new ArrayList<>();

            int index = startIndex;
            DialerAccountPhoneData.AccountPhoneData p = accountPhoneList.get(index);
            do {
                DialerQueueAccountDetails dqAccountDetails = new DialerQueueAccountDetails();
                long accountPk = p.getAccountPk();
                dqAccountDetails.setAccountPk(accountPk);
                dqAccountDetails.setBestTimeToCall(p.getBestTimeToCall());
                LOG.info("Plan D Parsing phone number list for account: {}, index : {}, endIndex: {}", accountPk, index, endIndex);

                List<CustomerPhoneData> bwrPhoneData = new ArrayList<>();
                do {
                    CustomerPhoneData borrowerPhoneData = new CustomerPhoneData();
                    borrowerPhoneData.setFirstName(p.getFirstName());
                    borrowerPhoneData.setLastName(p.getLastName());
                    long borrowerPk = p.getCustomerPk();
                    do {
                        BasicPhoneData basicPhoneData = new BasicPhoneData();
                        basicPhoneData.setPhoneNumber(PhoneUtils.formatPhoneNumber(p.getAreaCode(), p.getPhoneNumber()));
                        if (debug) {
                            LOG.info("Plan D AccountPk: {} ; CustomerPk: {} ; CustomerPhone: {}", p.getAccountPk(), p.getCustomerPk(), basicPhoneData.getPhoneNumber());
                        }
                        basicPhoneData.setPhoneNumberType(p.getPhoneType());
                        basicPhoneData.setCallTimeCode(CallTimeCode.OK_TO_CALL);
                        if (p.getZipCode() != null) {
                            DialerAccountPhoneData.ZipAreaCode z = new DialerAccountPhoneData.ZipAreaCode(p.getZipCode(), p.getAreaCode());
                            PhoneNumberCallable pnc = zipPhoneCache.get(z);
                            if (pnc == null) {
                                pnc = dqRepo.getPhoneNumberCallable(p.getZipCode(), p.getAreaCode());
                                zipPhoneCache.put(z, pnc);
                            }
                            basicPhoneData.setCallTimeCode(pnc.getCallTimeCode());
                            basicPhoneData.setEarliestTimeToCall(pnc.getEarliestTimeToCall());
                        }
                        borrowerPhoneData.getBasicPhoneData().add(basicPhoneData);

                        index++;
                        if (index >= endIndex) {
                            break;
                        }
                        p = accountPhoneList.get(index);
                    } while (p.getCustomerPk() == borrowerPk);
                    bwrPhoneData.add(borrowerPhoneData);
                } while (index < endIndex && p.getAccountPk() == accountPk);
                dqAccountDetails.setCustomerPhoneData(bwrPhoneData);
                accountDetailList.add(dqAccountDetails);
                IQueue<DialerQueueAccountDetails> dialerAccountDetailQueue = Config.getDialerAccountDetailQueue(hzService, dqPk);
                boolean addedAccount = dialerAccountDetailQueue.offer(dqAccountDetails);
                LOG.info("Plan D Hazelcast queue contains {} entries. Is pojo added? {}", dialerAccountDetailQueue.size(), addedAccount);               
            } while (index < endIndex);
            LOG.info("Returning {} account pojos for the given {} phones with startIndex {} and endIndex {} for dialer {}", accountDetailList.size(), endIndex-startIndex, startIndex, endIndex, dqPk);
            return accountDetailList;
        }

    }

}
