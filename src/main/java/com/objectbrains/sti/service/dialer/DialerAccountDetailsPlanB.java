/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.objectbrains.sti.service.dialer;

import com.objectbrains.hcms.annotation.ConfigContext;
import com.objectbrains.hcms.configuration.ConfigurationUtility;
import com.objectbrains.sti.constants.CallTimeCode;
import com.objectbrains.sti.db.repository.dialer.DialerQueueRepository;
import com.objectbrains.sti.exception.DialerException;
import com.objectbrains.sti.pojo.BasicPhoneData;
import com.objectbrains.sti.pojo.CustomerPhoneData;
import com.objectbrains.sti.pojo.DialerQueueAccountDetails;
import com.objectbrains.sti.service.dialer.DialerAccountPhoneData;
import com.objectbrains.sti.service.dialer.PhoneNumberCallable;
import com.objectbrains.sti.service.utility.PhoneUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
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
public class DialerAccountDetailsPlanB {
    @Autowired
    @Qualifier("sti-executor")
    private AsyncTaskExecutor executor;
    @ConfigContext
    private ConfigurationUtility config;
    @Autowired
    private DialerQueueRepository dqRepo;
    
    private static final Logger LOG = LoggerFactory.getLogger(DialerAccountDetailsPlanB.class);
    
    //Optimized because 1. the executor works on batches instead of a single Account and 2. the phoneCallable is cached.
    List<DialerQueueAccountDetails> buildAccountDetailsViaPlanB(List<DialerAccountPhoneData.AccountPhoneData> AccountPhoneDataList) {
        LocalDateTime start = new LocalDateTime();
        List<Future<List<DialerQueueAccountDetails>>> futureList = new ArrayList<>();
        ConcurrentHashMap<DialerAccountPhoneData.ZipAreaCode, PhoneNumberCallable> zipPhoneCache = new ConcurrentHashMap<>();
        boolean debug = config.getBoolean("tms.log.Account.phone.data", Boolean.TRUE);
        // Divide the list into batches
        int partitionCount = config.getInteger("buildDialerQueueAccountDetails.partitionCount", 10);
        int size = AccountPhoneDataList.size();
        int minPartitionSize = size / partitionCount;
        int remainingPartitionSize = size % partitionCount;
        LOG.info("Plan B Dynamic code returned : {} phones; partitionCount : {}; MinPartitionSize : {}; remainingPartitionSize : {} ",size, partitionCount, minPartitionSize, remainingPartitionSize);
        for (int i = 0, startIndex = 0; i < partitionCount; i++) {
            //Divide the remaining entries among the batches created. 
            int partitionSize = minPartitionSize + ((i < remainingPartitionSize) ? 1 : 0);
            int endIndex = (startIndex+ partitionSize > size) ? size : startIndex+ partitionSize;//Math.max(partitionSize, startIndex + 1);
            LOG.info("Plan B Batch {} partitionSize {}  startIndex {} endIndex {} ", i, partitionSize, startIndex, endIndex);
            if (startIndex < size) {
                long pk = AccountPhoneDataList.get(endIndex-1).getAccountPk();
                long nextPk = 0;
                if(endIndex < size){
                    nextPk = AccountPhoneDataList.get(endIndex).getAccountPk();
                }
                LOG.info("Plan B Last AccountPk in batch {} is {} and the first AccountPk in next batch is {}",i, pk, nextPk);
                while(endIndex < size && pk == nextPk){
                    LOG.info("Plan B pk : {}, nextPk : {} endIndex : {}", pk, nextPk, endIndex);
                    endIndex++;
                    nextPk = endIndex < size ? AccountPhoneDataList.get(endIndex).getAccountPk() : 0;
                }
//                for(nextPk = pk; endIndex < size && nextPk == pk; endIndex++){
//                    nextPk = AccountPhoneDataList.get(endIndex).getAccountPk(); 
//                }
                int numberOfPhonesPerbatch = endIndex - startIndex;
                LOG.info("Plan B Number of phones in batch {} with startIndex {} and endIndex {} are {}", i, startIndex, endIndex, numberOfPhonesPerbatch);
                Future<List<DialerQueueAccountDetails>> future = executor.submit(
                        new DialerQueueAccountDetailsCallable(AccountPhoneDataList, startIndex, endIndex, zipPhoneCache, debug));
                startIndex = endIndex;
                futureList.add(future);  
            } else {
                break;
            }           
        }
        List<DialerQueueAccountDetails> dqAccounts = new ArrayList<>();        
        for (Future<List<DialerQueueAccountDetails>> future : futureList) {
            try {
                dqAccounts.addAll(future.get());
            } catch (InterruptedException | ExecutionException ex) {
                throw new DialerException(ex);
            }
        }       
        LOG.info("Plan B buildDialerQueueAccountDetails took {} msec to return {} Accounts", (new LocalDateTime().getMillisOfDay() - start.getMillisOfDay()), dqAccounts.size());
        return dqAccounts;
    }
    

    private class DialerQueueAccountDetailsCallable implements Callable<List<DialerQueueAccountDetails>> {

        private List<DialerAccountPhoneData.AccountPhoneData> AccountPhoneList;
        private int startIndex;
        private int endIndex;
        private boolean debug;
        private ConcurrentHashMap<DialerAccountPhoneData.ZipAreaCode, PhoneNumberCallable> zipPhoneCache;
        
        public DialerQueueAccountDetailsCallable(List<DialerAccountPhoneData.AccountPhoneData> AccountPhoneList, int startIndex, int endIndex,
                ConcurrentHashMap<DialerAccountPhoneData.ZipAreaCode, PhoneNumberCallable> zipPhoneCache, boolean debug) {
            this.AccountPhoneList = AccountPhoneList;
            this.startIndex = startIndex;
            this.endIndex = endIndex;
            this.zipPhoneCache = zipPhoneCache;
            this.debug = debug;
        }

        @Override
        public List<DialerQueueAccountDetails> call() throws Exception {
            List<DialerQueueAccountDetails> AccountDetailList = new ArrayList<>();

            int index = startIndex;
            DialerAccountPhoneData.AccountPhoneData p = AccountPhoneList.get(index);
            LOG.info("Plan B Given AccountPhoneList size : "+AccountPhoneList.size()+"; startIndex : "+startIndex+"; endIndex : "+endIndex);
            do {
                DialerQueueAccountDetails dqAccountDetails = new DialerQueueAccountDetails();
                long AccountPk = p.getAccountPk();
                dqAccountDetails.setAccountPk(AccountPk);
                dqAccountDetails.setBestTimeToCall(p.getBestTimeToCall());
                LOG.info("Plan B Parsing phone number list for Account: {}, index : {}, endIndex: {}", AccountPk, index, endIndex);

                List<CustomerPhoneData> bwrPhoneData = new ArrayList<>();
                do {
                    CustomerPhoneData CustomerPhoneData = new CustomerPhoneData();
                    CustomerPhoneData.setFirstName(p.getFirstName());
                    CustomerPhoneData.setLastName(p.getLastName());
                    long CustomerPk = p.getCustomerPk();
                    do {
                        BasicPhoneData basicPhoneData = new BasicPhoneData();
                        basicPhoneData.setPhoneNumber(PhoneUtils.formatPhoneNumber(p.getAreaCode(), p.getPhoneNumber()));
                        if (debug) {
                            LOG.info("Plan B AccountPk: {} ; CustomerPk: {} ; CustomerPhone: {}", p.getAccountPk(), p.getCustomerPk(), basicPhoneData.getPhoneNumber());
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
                        CustomerPhoneData.getBasicPhoneData().add(basicPhoneData);

                        index++;
                        if (index >= endIndex) {
                            break;
                        }
                        p = AccountPhoneList.get(index);
                    } while (p.getCustomerPk() == CustomerPk);
                    bwrPhoneData.add(CustomerPhoneData);
                } while (index < endIndex && p.getAccountPk() == AccountPk);
                dqAccountDetails.setCustomerPhoneData(bwrPhoneData);
                AccountDetailList.add(dqAccountDetails);
            } while (index < endIndex);
            LOG.info("Plan B Returning {} pojos for given {} phones for startIndex {} and endIndex {}", AccountDetailList.size(), endIndex-startIndex, startIndex, endIndex);
            return AccountDetailList;
        }

    }
    
}
