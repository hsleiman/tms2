/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.crm.iws.restful.rest;

import com.amp.crm.aop.Authorization;
import com.amp.crm.constants.Permission;
import com.amp.crm.db.entity.base.account.Account;
import com.amp.crm.db.entity.base.customer.Customer;
import com.amp.crm.embeddable.AccountData;
import com.amp.crm.embeddable.EmailData;
import com.amp.crm.embeddable.PersonalInformation;
import com.amp.crm.embeddable.PhoneData;
import com.amp.crm.exception.CrmException;
import com.amp.crm.pojo.CustomerContactInformationPojo;
import com.amp.crm.service.core.AccountService;
import com.amp.crm.service.customer.CustomerService;
import com.amp.crm.service.customerinfo.EmailService;
import com.amp.crm.service.customerinfo.PhoneService;
import java.util.List;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.web.bind.annotation.RequestMethod.POST;

/**
 * @author Hoang, J, Bishistha
 */
@RestController()
@RequestMapping(value = "/settleitRestController", produces = MediaType.APPLICATION_JSON_VALUE)
public class SettleitRestController {

    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(SettleitRestController.class);
    @Autowired
    private CustomerService customerService;
    @Autowired
    private AccountService accountService;
    @Autowired
    private PhoneService phoneService;
    @Autowired
    private EmailService emailService;

    @RequestMapping(value = "/createOrUpdateCustomer", method = POST)
    @Authorization(permission = Permission.Authenticated, noPermissionTo = "Not Authenticated.")
    public long createOrUpdateCustomer(@RequestBody PersonalInformation personalInformation) {
        return customerService.createOrUpdateCustomer(personalInformation);
    }

    @RequestMapping(value = "/getCustomerPersonalInfo/{customerPk}", method = GET)
    @Authorization(permission = Permission.Authenticated, noPermissionTo = "Not Authenticated.")
    public PersonalInformation getCustomerPersonalInfo(@PathVariable long customerPk) {
        Customer customer = customerService.getCustomerByPk(customerPk);
        return customer.getPersonalInfo();
    }
    
    @RequestMapping(value = "/createOrUpdateCustomerPhone", method = POST)
    @Authorization(permission = Permission.Authenticated, noPermissionTo = "Not Authenticated.")
    public long createOrUpdateCustomerPhone(@RequestBody PhoneData phoneData) throws CrmException {
        return phoneService.createOrUpdateCustomerPhone(phoneData);
    }
    
    @RequestMapping(value = "/getCustomerPhoneInfo/{customerPk}", method = GET)
    @Authorization(permission = Permission.Authenticated, noPermissionTo = "Not Authenticated.")
    public List<PhoneData> getCustomerPhoneInfo(@PathVariable long customerPk) throws CrmException{
        return phoneService.getCustomerPhoneInfo(customerPk);
    }
    
    @RequestMapping(value = "/createOrUpdateCustomerEmail", method = POST)
    @Authorization(permission = Permission.Authenticated, noPermissionTo = "Not Authenticated.")
    public long createOrUpdateCustomerEmail(@RequestBody EmailData emailData) throws CrmException {
        return emailService.createOrUpdateCustomerEmail(emailData);
    }
    
    @RequestMapping(value = "/getCustomerEmailInfo/{customerPk}", method = GET)
    @Authorization(permission = Permission.Authenticated, noPermissionTo = "Not Authenticated.")
    public List<EmailData> getCustomerEmailInfo(@PathVariable long customerPk) throws CrmException{
        return emailService.getEmailAddressForCustomer(customerPk);
    }

    @RequestMapping(value = "/createOrUpdateAccount", method = POST)
    @Authorization(permission = Permission.Authenticated, noPermissionTo = "Not Authenticated.")
    public long createOrUpdateAccount(@RequestBody AccountData accountData) {
        return accountService.createOrUpdateAccount(accountData);
    }
    
    @RequestMapping(value="/getAccountByPk/{accountPk}", method = GET)
    @Authorization(permission = Permission.Authenticated, noPermissionTo = "Not Authenticated.")
    public Account getAccountByPk(@PathVariable("accountPk") long accountPk){
        return accountService.getAccountByPk(accountPk);
    }
    
      @RequestMapping(value = "/getCustomerContactInformation/{customerPk}", method = GET)
    @Authorization(permission = Permission.Authenticated, noPermissionTo = "Not Authenticated.")
    public CustomerContactInformationPojo getCustomerContactInformation(@PathVariable long customerPk) throws CrmException{
        return customerService.getBorrowerContactInformation(customerPk);
    }

    @RequestMapping(value="/createTestData", method = POST)
    @Authorization(permission = Permission.Authenticated, noPermissionTo = "Not Authenticated.")
    public void createTestData(){
        accountService.createTestData();
    }
}
