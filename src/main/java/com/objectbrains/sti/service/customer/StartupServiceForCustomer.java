package com.objectbrains.sti.service.customer;

import com.objectbrains.scheduler.annotation.RunOnce;
import com.objectbrains.sti.constants.CallerId;
import com.objectbrains.sti.constants.EmailAddressType;
import com.objectbrains.sti.constants.Gender;
import com.objectbrains.sti.constants.PhoneNumberType;
import com.objectbrains.sti.db.entity.base.account.Account;
import com.objectbrains.sti.embeddable.AccountData;
import com.objectbrains.sti.embeddable.EmailData;
import com.objectbrains.sti.embeddable.PersonalInformation;
import com.objectbrains.sti.embeddable.PhoneData;
import com.objectbrains.sti.exception.StiException;
import com.objectbrains.sti.service.core.AccountService;
import com.objectbrains.sti.service.customerinfo.EmailService;
import com.objectbrains.sti.service.customerinfo.PhoneService;
import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

/**
 * Generate the dummy customer on startup if not exist
 */
@Service
public class StartupServiceForCustomer {

    private static final Logger log = LoggerFactory.getLogger(StartupServiceForCustomer.class);

    @Autowired
    private CustomerService customerService;

    @Autowired
    private PhoneService phoneService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private AccountService accountService;

    @PostConstruct
    public void init() {
    }


    /**
     * runs on Start Up
     */
    @RunOnce(retryOnFailure = true, retryDelayMillis = 30000)
    public void systemStartUp() {
        try {

            /**
             * based on (Production Env):
             * CATALINA_OPTS="$CATALINA_OPTS -Dproduction=true"
             * CATALINA_OPTS="$CATALINA_OPTS -Dautostartup=true"
             */
            String production = System.getProperty("production");
            String autoStartup = System.getProperty("autostartup");

            if (production != null && production.toLowerCase().equals("true")
                    && autoStartup != null && autoStartup.toLowerCase().equals("true")) {
                //this is production env
                log.info("Production Env");
            } else {
                createCustomer();
            }


        } catch (Exception ex) {
            log.error("Exception {}", ex);
        }

    }

    private void createCustomer() throws StiException {
        log.info("Creating dummy Customer if not Exist");
        //should be max 10 names for now
        String[] customerStrings = {"Jay", "Farzad", "Tesla", "Bob"};
        int counter = 0;
        for (String s : customerStrings) {

            //create account
            Account account = accountService.getAccountByPk(counter + 1);
            if (account == null) {

                AccountData accountData = new AccountData();
                accountData.setAccountPk(0);

                accountService.createOrUpdateAccount(accountData);
                log.info("account Created:" + s + (counter + 1));
            }

            account = accountService.getAccountByPk(counter + 1);

            //creating customer
            PersonalInformation personalInformation = new PersonalInformation();
            personalInformation.setDateOfBirth(new LocalDate());
            personalInformation.setGender(Gender.MALE);
            personalInformation.setFirstName(s);
            personalInformation.setLastName(s);
            personalInformation.setMiddleInitial(s);
            personalInformation.setStatedIncome(true);
            personalInformation.setSsn("12312123" + counter);


            personalInformation.setAccountPk(account.getPk());

            long customerPk = customerService.createOrUpdateCustomer(personalInformation);
            if (customerPk > 0) {

                //creating customer phone
                PhoneData phoneData = new PhoneData();
                phoneData.setAccountPk(customerPk);
                phoneData.setAreaCode(1L);
                phoneData.setPhoneNumber(1231234567L);
                phoneData.setPhoneNumberType(PhoneNumberType.HOME_PHONE_1);
                phoneService.createOrUpdateCustomerPhone(phoneData);

                //creating customer email
                EmailData emailData = new EmailData();
                emailData.setCustomerPk(customerPk);
                emailData.setEmailAddressType(EmailAddressType.PRIMARY_EMAIL);
                emailData.setEmailAddress(s + "@gmail.com");
                emailService.createOrUpdateCustomerEmail(emailData);

            } else {
                log.info("Customer:" + s + " has not created");
            }

            counter++;
        }
    }

}
