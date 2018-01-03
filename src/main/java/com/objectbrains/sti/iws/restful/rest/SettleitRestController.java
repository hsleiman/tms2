/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.objectbrains.sti.iws.restful.rest;

import com.objectbrains.sti.aop.Authorization;
import com.objectbrains.sti.constants.Permission;
import com.objectbrains.sti.db.entity.base.account.Account;
import com.objectbrains.sti.db.entity.base.customer.Customer;
import com.objectbrains.sti.embeddable.AccountData;
import com.objectbrains.sti.embeddable.EmailData;
import com.objectbrains.sti.embeddable.PersonalInformation;
import com.objectbrains.sti.embeddable.PhoneData;
import com.objectbrains.sti.exception.StiException;
import com.objectbrains.sti.pojo.CustomerContactInformationPojo;
import com.objectbrains.sti.service.core.AccountService;
import com.objectbrains.sti.service.customer.CustomerService;
import com.objectbrains.sti.service.customerinfo.EmailService;
import com.objectbrains.sti.service.customerinfo.PhoneService;
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
 * @author David
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
    public long createOrUpdateCustomerPhone(@RequestBody PhoneData phoneData) throws StiException {
        return phoneService.createOrUpdateCustomerPhone(phoneData);
    }
    
    @RequestMapping(value = "/getCustomerPhoneInfo/{customerPk}", method = GET)
    @Authorization(permission = Permission.Authenticated, noPermissionTo = "Not Authenticated.")
    public List<PhoneData> getCustomerPhoneInfo(@PathVariable long customerPk) throws StiException{
        return phoneService.getCustomerPhoneInfo(customerPk);
    }
    
    @RequestMapping(value = "/createOrUpdateCustomerEmail", method = POST)
    @Authorization(permission = Permission.Authenticated, noPermissionTo = "Not Authenticated.")
    public long createOrUpdateCustomerEmail(@RequestBody EmailData emailData) throws StiException {
        return emailService.createOrUpdateCustomerEmail(emailData);
    }
    
    @RequestMapping(value = "/getCustomerEmailInfo/{customerPk}", method = GET)
    @Authorization(permission = Permission.Authenticated, noPermissionTo = "Not Authenticated.")
    public List<EmailData> getCustomerEmailInfo(@PathVariable long customerPk) throws StiException{
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
    public CustomerContactInformationPojo getCustomerContactInformation(@PathVariable long customerPk) throws StiException{
        return customerService.getBorrowerContactInformation(customerPk);
    }

    @RequestMapping(value="/createTestData", method = POST)
    @Authorization(permission = Permission.Authenticated, noPermissionTo = "Not Authenticated.")
    public void createTestData(){
        accountService.createTestData();
    }
}
