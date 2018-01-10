/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.crm.service.customerinfo;

import com.amp.crm.db.entity.base.customer.Customer;
import com.amp.crm.db.entity.base.customer.Email;
import com.amp.crm.db.repository.customer.CustomerRepository;
import com.amp.crm.db.repository.customerinfo.EmailRepository;
import com.amp.crm.embeddable.EmailData;
import java.util.List;
import java.util.Set;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * 
 */
@Service
@Transactional
public class EmailService {

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private CustomerRepository customerRepo;

    @Autowired
    private EmailRepository emailRepo;

    public long createOrUpdateCustomerEmail(EmailData newEmail) {
        Customer customer = customerRepo.findCustomerByPk(newEmail.getCustomerPk());
        if (customer != null) {
            if (newEmail.getEmailPk() <= 0) {
                Email email = new Email();
                email.setEmailData(newEmail);
                customer.getEmails().add(email);
                email.setCustomer(customer);
                entityManager.persist(email);
                return email.getPk();
            }else{
                Email email = emailRepo.findEmailByPk(newEmail.getEmailPk());
                email.setEmailData(newEmail);
                return email.getPk();
            }
        }
        return 0;
    }

    public void removeEmailAddress(long emailPk) {
        Email email = emailRepo.findEmailByPk(emailPk);
        Customer customer = email.getCustomer();
        customer.getEmails().remove(email);
        email.setCustomer(null);
        entityManager.remove(email);
    }

    public List<EmailData> getEmailAddressForCustomer(Long customerPk) {
        return emailRepo.getAllEmailsByCustomerPk(customerPk);
    }
}
