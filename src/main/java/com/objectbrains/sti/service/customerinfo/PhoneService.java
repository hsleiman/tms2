/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.objectbrains.sti.service.customerinfo;

import com.objectbrains.sti.constants.AddressType;
import com.objectbrains.sti.constants.DoNotCallCodes;
import com.objectbrains.sti.db.entity.base.WorkQueue;
import com.objectbrains.sti.db.entity.base.account.Account;
import com.objectbrains.sti.db.entity.base.customer.Address;
import com.objectbrains.sti.db.entity.base.customer.Customer;
import com.objectbrains.sti.db.entity.base.customer.Phone;
import com.objectbrains.sti.db.repository.account.AccountRepository;
import com.objectbrains.sti.db.repository.customer.CustomerRepository;
import com.objectbrains.sti.db.repository.customerinfo.PhoneRepository;
import com.objectbrains.sti.embeddable.AddressData;
import com.objectbrains.sti.embeddable.PersonalInformation;
import com.objectbrains.sti.embeddable.PhoneData;
import com.objectbrains.sti.exception.StiException;
import com.objectbrains.sti.exception.WorkQueueNotFoundException;
import com.objectbrains.sti.pojo.CustomerCallablePojo;
import com.objectbrains.sti.service.customer.WorkQueueService;
import com.objectbrains.sti.service.utility.ZipTimeZoneService;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author Bishistha
 */
@Service
@Transactional
public class PhoneService {

    @Autowired
    private PhoneRepository phoneRepo;

    @Autowired
    private CustomerRepository customerRepo;

    @Autowired
    private ZipTimeZoneService zipTimeZoneService;

    @Autowired
    private WorkQueueService workQueueService;

    @Autowired
    private AddressService addressService;

    public long createOrUpdateCustomerPhone(PhoneData phoneData) throws StiException {
        long customerPk = phoneData.getCustomerPk();
        Customer customer = customerRepo.findCustomerByPk(customerPk);
        if (customer != null) {
            long phonePk = phoneData.getPhonePk();
            if (phonePk <= 0) {
                if(phoneData.getAccountPk() <= 0){
                    throw new StiException("##### Account Pk cannot be null when creating Phone for a customer.");
                }
                Phone phone = new Phone();
                phone.setPhoneData(phoneData);
                phoneRepo.persistPhone(phone);
                customer.getPhones().add(phone);
                phone.setCustomer(customer);
                return phone.getPk();
            } else {
                Phone oldPhone = phoneRepo.findPhoneByPk(phonePk);
                oldPhone.setPhoneData(phoneData);
                phoneRepo.mergePhone(oldPhone);
                return phonePk;
            }
        }
        return 0;
    }

    public List<PhoneData> getCustomerPhoneInfo(long customerPk) throws StiException {
        List<Phone> phones = phoneRepo.getAllPhoneByCustomerPk(customerPk);
        List<PhoneData> phoneDatas = new ArrayList<>();
        if (phones != null) {
            for (Phone phone : phones) {
                AddressData currentAddressData = addressService.getAddressForCustomerByType(customerPk, AddressType.CURRENT);
                if (currentAddressData != null) {
                    String currentAddressZip = currentAddressData.getZip();
                    if (currentAddressZip.length() > 5) {
                        currentAddressZip = currentAddressZip.substring(0, 5);
                    }
                    phone.getPhoneData().setCustomerCallable(setCallable(currentAddressZip, phone.getPhoneData().getAreaCode(), phone.getPhoneData().getDoNotCall()));
                }
                
                //checks if queue has DNC checked. if there is no queue account DNC cannot be set, so do nothing
                try {
                    for (WorkQueue workQueue : workQueueService.getAccountPortfolioQueues(phone.getPhoneData().getAccountPk())) {
                        Boolean queueDNC = workQueue.getWorkQueueData().isDoNotCall();
                        if (queueDNC != null && queueDNC == true && phone.getPhoneData().getCustomerCallable().getDoNotCallCode() == 0) {
                            phone.getPhoneData().getCustomerCallable().setDoNotCallCode(DoNotCallCodes.ACCOUNT_DO_NOT_CALL_TRUE);
                        }
                    }
                } catch (WorkQueueNotFoundException colQueuesNotFound) {
                };
                
                phoneDatas.add(phone.getPhoneData());
            }
        }
        return phoneDatas;
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
}
