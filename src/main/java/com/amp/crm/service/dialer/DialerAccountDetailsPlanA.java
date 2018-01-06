/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.crm.service.dialer;

import com.objectbrains.hcms.annotation.ConfigContext;
import com.objectbrains.hcms.configuration.ConfigurationUtility;
import com.amp.crm.constants.CallTimeCode;
import com.amp.crm.db.repository.dialer.DialerQueueRepository;
import com.amp.crm.exception.DialerException;
import com.amp.crm.pojo.BasicPhoneData;
import com.amp.crm.pojo.CustomerPhoneData;
import com.amp.crm.pojo.DialerQueueAccountDetails;
import com.amp.crm.service.utility.PhoneUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
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
public class DialerAccountDetailsPlanA {
    @Autowired
    @Qualifier("sti-executor")
    private AsyncTaskExecutor executor;
    @ConfigContext
    private ConfigurationUtility config;
    @Autowired
    private DialerQueueRepository dqRepo;
    
    private static final Logger LOG = LoggerFactory.getLogger(DialerAccountDetailsPlanA.class);
    
    List<DialerQueueAccountDetails> buildAccountDetailsViaPlanA(List<DialerAccountPhoneData.AccountPhoneData> accountPhoneDataList) {
        LocalDateTime start = new LocalDateTime();
        List<DialerQueueAccountDetails> dqAccounts = new ArrayList<>();
        List<Future<DialerQueueAccountDetails>> futureList = new ArrayList<>();
        for (int startIndex = 0, endIndex = 0; startIndex < accountPhoneDataList.size(); startIndex = endIndex) {
            endIndex = getAccountIndex(startIndex, endIndex, accountPhoneDataList);
            long pk = accountPhoneDataList.get(startIndex).getAccountPk();
            final List<DialerAccountPhoneData.AccountPhoneData> accountPhoneList = accountPhoneDataList.subList(startIndex, endIndex);
            LOG.info("Number of phones in accountPhoneList for account {} with startIndex {} and endIndex {} are {}", pk,startIndex, endIndex, accountPhoneList.size());
                Future<DialerQueueAccountDetails> future = executor.submit(new Callable<DialerQueueAccountDetails>() {
                @Override
                public DialerQueueAccountDetails call() throws Exception {
                    DialerQueueAccountDetails dqAccountDetails = new DialerQueueAccountDetails();
                    dqAccountDetails.setAccountPk(accountPhoneList.get(0).getAccountPk());
                    dqAccountDetails.setBestTimeToCall(accountPhoneList.get(0).getBestTimeToCall());
                    LOG.info("Parsing phone number list for account: {}", dqAccountDetails.getAccountPk());
                    dqAccountDetails.setCustomerPhoneData(getCustomerPhoneDataForAccount(accountPhoneList));
                    return dqAccountDetails;
                }
                });
                futureList.add(future);
        }
        
        for (Future<DialerQueueAccountDetails> future : futureList) {
            try {
                dqAccounts.add(future.get());
            } catch (InterruptedException | ExecutionException ex) {
                throw new DialerException(ex);
            }
        }
        
        LOG.info("buildDialerQueueaccountDetails via plan A took {} msec to return {} accounts", (new LocalDateTime().getMillisOfDay()-start.getMillisOfDay()), dqAccounts.size());
        return dqAccounts;
    }
    
    
    private List<CustomerPhoneData> getCustomerPhoneDataForAccount(List<DialerAccountPhoneData.AccountPhoneData> accountPhoneList) {
        Map<DialerAccountPhoneData.ZipAreaCode, PhoneNumberCallable> zipAreaCodeMap = new HashMap<>();
        List<CustomerPhoneData> bwrPhoneData = new ArrayList<>();
        for (int startBwrIndex = 0, endBwrIndex = 0; startBwrIndex < accountPhoneList.size(); startBwrIndex = endBwrIndex) {
            endBwrIndex = getBorrowerIndex(startBwrIndex, endBwrIndex, accountPhoneList);
            List<DialerAccountPhoneData.AccountPhoneData> bwrPhoneList = accountPhoneList.subList(startBwrIndex, endBwrIndex);
            CustomerPhoneData customerPhoneData = new CustomerPhoneData();
            customerPhoneData.setFirstName(bwrPhoneList.get(0).getFirstName());
            customerPhoneData.setLastName(bwrPhoneList.get(0).getLastName());
            LOG.info("[DialerAccountPhoneData] \n");
            for (DialerAccountPhoneData.AccountPhoneData p : bwrPhoneList) {
                BasicPhoneData basicPhoneData = new BasicPhoneData();
                basicPhoneData.setPhoneNumber(PhoneUtils.formatPhoneNumber(p.getAreaCode(), p.getPhoneNumber()));
                if(config.getBoolean("tms.log.account.phone.data", Boolean.TRUE)){
                    LOG.info("AccountPk: {} ; BorrowerPk: {} ; BorrowerPhone: {}", p.getAccountPk(), p.getCustomerPk(), basicPhoneData.getPhoneNumber());
                }
                basicPhoneData.setPhoneNumberType(p.getPhoneType());
                basicPhoneData.setCallTimeCode(CallTimeCode.OK_TO_CALL);
                if (p.getZipCode() != null) {
                    DialerAccountPhoneData.ZipAreaCode z = new DialerAccountPhoneData.ZipAreaCode(p.getZipCode(), p.getAreaCode());
                    PhoneNumberCallable pnc = zipAreaCodeMap.get(z);
                    if (pnc == null) {
                        pnc = dqRepo.getPhoneNumberCallable(p.getZipCode(), p.getAreaCode());
                        zipAreaCodeMap.put(z, pnc);
                    }
                    basicPhoneData.setCallTimeCode(pnc.getCallTimeCode());
                    basicPhoneData.setEarliestTimeToCall(pnc.getEarliestTimeToCall());
                }
                customerPhoneData.getBasicPhoneData().add(basicPhoneData);
            }
            bwrPhoneData.add(customerPhoneData);
        }
        return bwrPhoneData;
    }
 
    private int getAccountIndex(int startIndex, int endIndex, List<DialerAccountPhoneData.AccountPhoneData> accountPhoneDataList) {
        Long prevPk = accountPhoneDataList.get(startIndex).getAccountPk();
        do {
            endIndex++;
        } while (endIndex < accountPhoneDataList.size() && prevPk == accountPhoneDataList.get(endIndex).getAccountPk());
        return endIndex;
    }

    private int getBorrowerIndex(int startIndex, int endIndex, List<DialerAccountPhoneData.AccountPhoneData> accountPhoneDataList) {
        Long prevPk = accountPhoneDataList.get(startIndex).getCustomerPk();
        do {
            endIndex++;
        } while (endIndex < accountPhoneDataList.size() && prevPk == accountPhoneDataList.get(endIndex).getCustomerPk());
        return endIndex;
    }
    
    

}
