/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.crm.service.dialer;

import com.amp.crm.constants.PhoneNumberType;
import com.amp.crm.exception.StiException;
import com.amp.crm.pojo.DialerQueueAccountDetails;
import com.amp.crm.service.dialer.DialerAccountDetailsPlanA;
import com.amp.crm.service.utility.DynamicCodeService;
import java.math.BigInteger;
import java.sql.Time;
import java.util.List;
import java.util.Objects;
import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author David
 */
@Component
public class DialerAccountPhoneData {

    @Autowired
    private DynamicCodeService dynamicCodeService;
    @Autowired
    private DialerAccountDetailsPlanA planAService;
    @Autowired
    private DialerAccountDetailsPlanB planBService;
    @Autowired
    private DialerAccountDetailsPlanD planDService;
    
    private static final Logger LOG = LoggerFactory.getLogger(DialerAccountPhoneData.class);

    public static interface AccountPhoneDataDynamic {

        public List<AccountPhoneData> getAccountPhoneDataForQueue(long dqPk);

        public List<AccountPhoneData> getAccountPhoneDataForaccounts(List<Long> accounts);
        
        public List<Object> getCallableAccountsForDialerQueue(long dqPk);
    }

    public static class AccountPhoneData {

        private long accountPk;
        private LocalTime bestTimeToCall;
        private long customerPk;
        private String firstName;
        private String lastName;
        private long areaCode;
        private long phoneNumber;
        private PhoneNumberType phoneType;
        private String zipCode;

        public AccountPhoneData() {
        }

        public long getAccountPk() {
            return accountPk;
        }

        public void setAccountPk(long accountPk) {
            this.accountPk = accountPk;
        }
       
        public LocalTime getBestTimeToCall() {
            return bestTimeToCall;
        }

        public void setBestTimeToCall(Time bestTimeToCall) {
            this.bestTimeToCall = bestTimeToCall == null ? null : new LocalTime(bestTimeToCall.getTime());
        }

        public long getCustomerPk() {
            return customerPk;
        }

        public void setCustomerPk(BigInteger customerPk) {
            this.customerPk = customerPk.longValue();
        }

        public String getFirstName() {
            return firstName;
        }

        public void setFirstName(String firstName) {
            this.firstName = firstName;
        }

        public String getLastName() {
            return lastName;
        }

        public void setLastName(String lastName) {
            this.lastName = lastName;
        }

        public long getAreaCode() {
            return areaCode;
        }

        public void setAreaCode(BigInteger areaCode) {
            this.areaCode = areaCode.longValue();
        }

        public long getPhoneNumber() {
            return phoneNumber;
        }

        public void setPhoneNumber(BigInteger phoneNumber) {
            this.phoneNumber = phoneNumber.longValue();
        }

        public PhoneNumberType getPhoneType() {
            return phoneType;
        }

        public void setPhoneType(Integer phoneType) {
            this.phoneType = PhoneNumberType.getPhoneNumberType(phoneType);
        }

        public String getZipCode() {
            return zipCode;
        }

        public void setZipCode(String zipCode) {
            this.zipCode = zipCode;
        }
    }

    static class ZipAreaCode {

        private String zipCode;
        private Long areaCode;

        public ZipAreaCode() {
        }

        public ZipAreaCode(String zipCode, Long areaCode) {
            this.zipCode = zipCode;
            this.areaCode = areaCode;
        }

        public String getZipCode() {
            return zipCode;
        }

        public void setZipCode(String zipCode) {
            this.zipCode = zipCode;
        }

        public Long getAreaCode() {
            return areaCode;
        }

        public void setAreaCode(Long areaCode) {
            this.areaCode = areaCode;
        }

        @Override
        public int hashCode() {
            int hash = 5;
            hash = 37 * hash + Objects.hashCode(this.zipCode);
            hash = 37 * hash + Objects.hashCode(this.areaCode);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final ZipAreaCode other = (ZipAreaCode) obj;
            if (!Objects.equals(this.zipCode, other.zipCode)) {
                return false;
            }
            if (!Objects.equals(this.areaCode, other.areaCode)) {
                return false;
            }
            return true;
        }

    }
    
    public List<DialerQueueAccountDetails> getDialerQueueAccountDetailsForQueue(long dqPk) throws StiException {
        List<AccountPhoneData> accountPhoneDataList = dynamicCodeService.getDynamicBean(AccountPhoneDataDynamic.class).getAccountPhoneDataForQueue(dqPk);
        return planAService.buildAccountDetailsViaPlanA(accountPhoneDataList);
    }
    
    public List<DialerQueueAccountDetails> getDialerQueueAccountDetailsViaPlanA(List<Long> accounts) {
        LocalDateTime start = new LocalDateTime();
        List<AccountPhoneData> accountPhoneDataList = dynamicCodeService.getDynamicBean(AccountPhoneDataDynamic.class).getAccountPhoneDataForaccounts(accounts);
        LOG.info("dynamic code for list of accounts {} via plan A took {} msec  and returned {} phones ", accounts.size(), (new LocalDateTime().getMillisOfDay()-start.getMillisOfDay()),accountPhoneDataList.size());
        return planAService.buildAccountDetailsViaPlanA(accountPhoneDataList);
    }
    
    public List<DialerQueueAccountDetails> getDialerQueueAccountDetailsViaPlanA(long dialerQueuePk) {
        LocalDateTime start = new LocalDateTime();
        List<AccountPhoneData> accountPhoneDataList = dynamicCodeService.getDynamicBean(AccountPhoneDataDynamic.class).getAccountPhoneDataForQueue(dialerQueuePk);
        LOG.info("dynamic code for dialerQueuePk {} via plan A  took {} msec  and returned {} phones ", dialerQueuePk, (new LocalDateTime().getMillisOfDay()-start.getMillisOfDay()),accountPhoneDataList.size());
        return planAService.buildAccountDetailsViaPlanA(accountPhoneDataList);
    }
    
    public List<DialerQueueAccountDetails> getDialerQueueAccountDetailsViaPlanB(List<Long> accounts) {
        LocalDateTime start = new LocalDateTime();
        List<AccountPhoneData> accountPhoneDataList = dynamicCodeService.getDynamicBean(AccountPhoneDataDynamic.class).getAccountPhoneDataForaccounts(accounts);
        LOG.info("dynamic code for list of accounts {} via plan B took {} msec  and returned {} phones ", accounts.size(), (new LocalDateTime().getMillisOfDay()-start.getMillisOfDay()),accountPhoneDataList.size());        
        return planBService.buildAccountDetailsViaPlanB(accountPhoneDataList);
    }
    
    public List<DialerQueueAccountDetails> getDialerQueueAccountDetailsViaPlanB(long dialerQueuePk) {
        LocalDateTime start = new LocalDateTime();
        List<AccountPhoneData> accountPhoneDataList = dynamicCodeService.getDynamicBean(AccountPhoneDataDynamic.class).getAccountPhoneDataForQueue(dialerQueuePk);
        LOG.info("dynamic code for dialerQueuePk {} via plan B took {} msec  and returned {} phones ", dialerQueuePk, (new LocalDateTime().getMillisOfDay()-start.getMillisOfDay()),accountPhoneDataList.size());        
        return planBService.buildAccountDetailsViaPlanB(accountPhoneDataList);
    }
    
    @Async
    @Transactional
    public void saveDialerQueueAccountDetailsToHazelcast(List<Long> accounts) {
        LocalDateTime start = new LocalDateTime();
        List<AccountPhoneData> accountPhoneDataList = dynamicCodeService.getDynamicBean(AccountPhoneDataDynamic.class).getAccountPhoneDataForaccounts(accounts);
        LOG.info("dynamic code for plan D took {} msec  and returned {} phones ", (new LocalDateTime().getMillisOfDay()-start.getMillisOfDay()),accountPhoneDataList.size());
        planDService.saveDialerQueueAccountDetailsViaPlanD(accountPhoneDataList);
    }
    
    public List<Object> getCallableAccountsForDialerQueue(long dqPk){
        return dynamicCodeService.getDynamicBean(AccountPhoneDataDynamic.class).getCallableAccountsForDialerQueue(dqPk);
    }
}
