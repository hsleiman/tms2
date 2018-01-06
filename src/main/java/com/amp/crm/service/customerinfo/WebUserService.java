/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.crm.service.customerinfo;

import com.amp.crm.db.entity.base.customer.Customer;
import com.amp.crm.db.entity.base.customer.WebUser;
import com.amp.crm.db.repository.customer.CustomerRepository;
import com.amp.crm.db.repository.customerinfo.WebUserRepository;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author Bishistha
 */
@Service
@Transactional
public class WebUserService {
    
    @Autowired
    private WebUserRepository webUserRepo;
    
    @Autowired
    private CustomerRepository customerRepo;

    public void addCustomerWebUser(WebUser webUser, long customerPk) {
        Customer customer = customerRepo.findCustomerByPk(customerPk);
        if (webUser != null) {
            if (webUser.getPk() <= 0) {
                webUserRepo.persistWebUser(webUser);
                customer.getWebUsers().add(webUser);
                webUser.setCustomer(customer);
            } else {
                webUserRepo.mergeWebUser(webUser);
            }
        }
    }

//    public void removeEmailAddress(long emailPk) {
//        Email email = emailRepo.findEmailByPk(emailPk);
//        Customer customer = email.getCustomer();
//        customer.getEmails().remove(email);
//        email.setCustomer(null);
//        entityManager.remove(email);
//    }

    public Set<WebUser> getWebUsersForCustomer(Long customerPk) {
        Customer customer = customerRepo.findCustomerByPk(customerPk);
        return customer.getWebUsers();
    }
}
