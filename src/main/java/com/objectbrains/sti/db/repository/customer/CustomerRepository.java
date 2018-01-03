/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.objectbrains.sti.db.repository.customer;

import com.objectbrains.hcms.annotation.ConfigContext;
import com.objectbrains.hcms.configuration.ConfigurationUtility;
import com.objectbrains.sti.db.entity.base.customer.Customer;
import com.objectbrains.sti.db.entity.base.customer.Phone;
import com.objectbrains.sti.pojo.NationalPhoneNumber;
import com.objectbrains.sti.service.utility.PhoneUtils;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

/**
 *
 * @author Bishistha
 */
@Repository
public class CustomerRepository {
    
    private static final Logger LOG = LoggerFactory.getLogger(CustomerRepository.class);
    
    @ConfigContext
    private ConfigurationUtility configUtil;
    
    @PersistenceContext
    private EntityManager entityManager;
    
    public void persistCustomer(Customer c) {
        entityManager.persist(c);
    }
    
    public void mergeCustomer(Customer c) {
        entityManager.merge(c);
    }

    public Customer findCustomerByPk(long pk) {
        return entityManager.find(Customer.class, pk);
    }
    
    public List<Phone> locatePhoneByPhoneNumberAndAccount(String phoneNumber, long loanPk) {
        int start = new LocalDateTime().getMillisOfDay();
        NationalPhoneNumber phone = PhoneUtils.parseValidPhoneNumber(phoneNumber);
        int end = new LocalDateTime().getMillisOfDay();
        if((end-start) > configUtil.getInteger("can.call.number.parsephonenumber.time", 10)){
            LOG.warn("PhoneUtils.parseValidPhoneNumber(phoneNumber) took {} msec", end-start);
        }
        LOG.warn("PhoneNumber: {} loanPk: {} and phone LocalNumber: {} and areacode : {}", phoneNumber, loanPk, phone.getLocalNumber(), phone.getAreaCode());
        List<Phone> res = entityManager.createNamedQuery("SvPhone.LocateByPhoneNumberAndLoan", Phone.class)
                .setParameter("phoneNumber", phone.getLocalNumber())
                .setParameter("areaCode", phone.getAreaCode())
                .setParameter("loanPk", loanPk)
                .getResultList();
        return res;    
    }
    
}
