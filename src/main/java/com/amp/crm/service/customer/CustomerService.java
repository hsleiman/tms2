/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.crm.service.customer;

import com.amp.crm.constants.AddressType;
import com.amp.crm.constants.ContactTimeType;
import com.amp.crm.constants.DoNotCallCodes;
import com.amp.crm.db.entity.base.WorkQueue;
import com.amp.crm.db.entity.base.account.Account;
import com.amp.crm.db.entity.base.customer.Address;
import com.amp.crm.db.entity.base.customer.Customer;
import com.amp.crm.db.entity.base.customer.Email;
import com.amp.crm.db.entity.base.customer.Phone;
import com.amp.crm.db.repository.account.AccountRepository;
import com.amp.crm.db.repository.customer.CustomerRepository;
import com.amp.crm.embeddable.AddressData;
import com.amp.crm.embeddable.PersonalInformation;
import com.amp.crm.embeddable.PhoneData;
import com.amp.crm.exception.CrmException;
import com.amp.crm.exception.WorkQueueNotFoundException;
import com.amp.crm.pojo.CustomerCallablePojo;
import com.amp.crm.pojo.CustomerContactInformationPojo;
import com.amp.crm.service.customerinfo.AddressService;
import com.amp.crm.service.utility.ZipTimeZoneService;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * 
 */
@Service
@Transactional
public class CustomerService {

    @Autowired
    private CustomerRepository customerRepo;

    @Autowired
    private AccountRepository accountRepo;

    @Autowired
    private WorkQueueService workQueueService;

    @Autowired
    private AddressService addressService;

    @Autowired
    private ZipTimeZoneService zipTimeZoneService;

    private static final Logger LOG = LoggerFactory.getLogger(CustomerService.class);

    public long createOrUpdateCustomer(PersonalInformation personalInfo) {
        long accountPk = personalInfo.getAccountPk();
        Account account = accountRepo.findAccountByPk(accountPk);
        if (account != null) {
            long customerPk = personalInfo.getCustomerPk();
            if (customerPk <= 0) {
                Customer newCustomer = new Customer();
                newCustomer.setPersonalInfo(personalInfo);
                customerRepo.persistCustomer(newCustomer);
                account.getCustomers().add(newCustomer);
                newCustomer.setAccount(account);
                return newCustomer.getPk();
            } else {
                Customer oldCustomer = customerRepo.findCustomerByPk(customerPk);
                oldCustomer.setPersonalInfo(personalInfo);
                customerRepo.mergeCustomer(oldCustomer);
                return customerPk;
            }
        }
        return 0;
    }

    public Customer getCustomerByPk(long customerPk) {
        return customerRepo.findCustomerByPk(customerPk);
    }

    public CustomerContactInformationPojo getBorrowerContactInformation(long customerPk) throws CrmException {
        Customer customer = customerRepo.findCustomerByPk(customerPk);
        Set<Phone> phones = new HashSet<>(customer.getPhones());
        CustomerContactInformationPojo customerContactInfo = new CustomerContactInformationPojo();
        customerContactInfo.setCustomerPk(customerPk);
        Set<Email> emailAddresses = customer.getEmails();
        customerContactInfo.setEmailAddresses(new ArrayList<>(emailAddresses));
        Account account = customer.getAccount();
        customerContactInfo.setCallbackDateTime(account.getAccountData().getCallbackDateTime());
        HashMap<ContactTimeType, LocalDateTime> retMap = getContactTime(account);
        if (!retMap.isEmpty()) {
            customerContactInfo.setContactType(retMap.keySet().iterator().next());
            customerContactInfo.setContactTime(retMap.get(customerContactInfo.getContactType()));
        }
        LOG.debug("GetBorrowerPhones{}", customer.getPhones().size());
        for (Phone phone : phones) {
            AddressData currentAddress = addressService.getAddressForCustomerByType(customerPk, AddressType.CURRENT);
            String currentAddressZip = currentAddress.getZip();
            if (currentAddressZip.length() > 5) {
                currentAddressZip = currentAddressZip.substring(0, 5);
            }
            phone.getPhoneData().setCustomerCallable(setCallable(currentAddressZip, phone.getPhoneData().getAreaCode(), phone.getPhoneData().getDoNotCall()));
            try {
                for (WorkQueue workQueue : workQueueService.getAccountPortfolioQueues(phone.getPhoneData().getAccountPk())) {
                    Boolean queueDNC = workQueue.getWorkQueueData().isDoNotCall();
                    if (queueDNC != null && queueDNC == true && phone.getPhoneData().getCustomerCallable().getDoNotCallCode() == 0) {
                        phone.getPhoneData().getCustomerCallable().setDoNotCallCode(DoNotCallCodes.ACCOUNT_DO_NOT_CALL_TRUE);
                    }
                }
            } catch (WorkQueueNotFoundException colQueuesNotFound) {
            };
            //checks if queue has DNC checked. if there is no queue account DNC cannot be set, so do nothing
        }
        customerContactInfo.setPhones(new ArrayList<>(phones));
        return customerContactInfo;
    }

    public CustomerCallablePojo setCallable(String zip5Address, Long areaCode, Boolean doNotCallBool) {
        if (doNotCallBool == null) {
            doNotCallBool = false;
        }
        CustomerCallablePojo pojo = zipTimeZoneService.getCustomerCallable(zip5Address, areaCode);
        if (pojo.getDoNotCallCode() == DoNotCallCodes.OKAY_TO_CALL && doNotCallBool) {
            pojo.setDoNotCallCode(DoNotCallCodes.IS_DO_NOT_CALL_TRUE);
        }
        return pojo;
    }

    private HashMap<ContactTimeType, LocalDateTime> getContactTime(Account account) {
        HashMap<ContactTimeType, LocalDateTime> retMap = new HashMap<>();
        ContactTimeType contactType = null;
        LocalDateTime time = null;
        if (account.getAccountData().getLastContactTimestamp() != null && account.getAccountData().getLastContactTimestamp().toLocalDate().equals(LocalDate.now())) {
            contactType = ContactTimeType.LAST_CONTACT_TIME;
            time = account.getAccountData().getLastContactTimestamp();
        }
        if (account.getAccountData().getLastLeftMessageTime() != null && account.getAccountData().getLastLeftMessageTime().toLocalDate().equals(LocalDate.now())) {
            if (time != null && time.isBefore(account.getAccountData().getLastLeftMessageTime())) {
                contactType = ContactTimeType.LAST_LEFT_MESSAGE_TIME;
                time = account.getAccountData().getLastLeftMessageTime();
            } else if (time == null) {
                contactType = ContactTimeType.LAST_LEFT_MESSAGE_TIME;
                time = account.getAccountData().getLastLeftMessageTime();
            }
        }

        if (account.getAccountData().getDialerLeftMessageTime() != null && account.getAccountData().getDialerLeftMessageTime().toLocalDate().equals(LocalDate.now())) {
            if (time != null && time.isBefore(account.getAccountData().getDialerLeftMessageTime())) {
                contactType = ContactTimeType.DIALER_LEFT_MESSAGE_TIME;
                time = account.getAccountData().getDialerLeftMessageTime();
            } else if (time == null) {
                contactType = ContactTimeType.DIALER_LEFT_MESSAGE_TIME;
                time = account.getAccountData().getDialerLeftMessageTime();
            }
        }
        retMap.put(contactType, time);
        return retMap;
    }

}
