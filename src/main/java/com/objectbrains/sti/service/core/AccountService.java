/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.objectbrains.sti.service.core;

import com.objectbrains.sti.constants.ContactTimeType;
import com.objectbrains.sti.db.entity.agent.Agent;
import com.objectbrains.sti.db.entity.base.account.Account;
import com.objectbrains.sti.db.entity.base.WorkQueue;
import com.objectbrains.sti.db.entity.base.customer.Customer;
import com.objectbrains.sti.db.repository.AgentRepository;
import com.objectbrains.sti.db.repository.account.AccountRepository;
import com.objectbrains.sti.db.repository.account.WorkQueueRepository;
import com.objectbrains.sti.db.repository.customer.CustomerRepository;
import com.objectbrains.sti.embeddable.AccountData;
import com.objectbrains.sti.embeddable.PersonalInformation;
import com.objectbrains.sti.exception.StiException;
import java.util.HashMap;
import java.util.List;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author Bishistha
 */
@Service
@Transactional
public class AccountService {

    @Autowired
    private AccountRepository accountRepo;

    @Autowired
    private WorkQueueRepository workQueueRepo;

    @Autowired
    private CustomerRepository customerRepo;

    @Autowired
    private AgentRepository agentRepo;

    public long createOrUpdateAccount(AccountData accountdata) {
        long accountPk = accountdata.getAccountPk();
        if (accountPk <= 0) {
            Account newAccount = new Account();
            newAccount.setAccountData(accountdata);
            accountRepo.persistAccount(newAccount);
            accountPk = newAccount.getPk();
        } else {
            Account account = accountRepo.findAccountByPk(accountPk);
            account.setAccountData(accountdata);
        }
        return accountPk;
    }

    public Account getAccountByPk(long accountPk) {
        Account account = accountRepo.findAccountByPk(accountPk);
        if (account != null) {
            HashMap<ContactTimeType, LocalDateTime> retMap = getContactTime(account);
            if (!retMap.isEmpty()) {
                account.getAccountData().setContactType(retMap.keySet().iterator().next());
                account.getAccountData().setContactTime(retMap.get(account.getAccountData().getContactType()));
            }
        }
        return account;
    }

    public List<Account> getAllAccounts() {
        return null;
    }

    public void addAccountToWorkQueue(long accountPk, long queuePk) throws StiException {
        Account account = accountRepo.findAccountByPk(accountPk);
        if (account == null) {
            throw new StiException("No Account found for PK: " + accountPk);
        }
        WorkQueue workQueue = workQueueRepo.getWorkQueue(queuePk);
        if (workQueue == null) {
            throw new StiException("No WorkQueue found for PK: " + queuePk);
        }
        workQueue.getAccounts().add(account);
        account.getWorkQueues().add(workQueue);

    }

    public void removeAccountFromWorkQueue(long accountPk, long queuePk) throws StiException {
        Account account = accountRepo.findAccountByPk(accountPk);
        if (account == null) {
            throw new StiException("No Account found for PK: " + accountPk);
        }
        WorkQueue workQueue = workQueueRepo.getWorkQueue(queuePk);
        if (workQueue == null) {
            throw new StiException("No WorkQueue found for PK: " + queuePk);
        }
        if (!account.getWorkQueues().remove(workQueue)) {
            throw new StiException("Could not remove WorkQueue from Account because AccountPk:" + accountPk + " did not contain workQueuePk:" + queuePk);
        }
        if (!workQueue.getAccounts().remove(account)) {
            throw new StiException("Could not remove Account from WorkQueue because WorkQueuePk:" + queuePk + " did not contain accountPk:" + accountPk);
        }

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

    public void createTestData() {
        Account newAccount = new Account();
        accountRepo.persistAccount(newAccount);
        newAccount.getPk();
        for (int i = 0; i < 3; i++) {
            Customer newCustomer = new Customer();
            PersonalInformation info = new PersonalInformation();
            info.setFirstName("TestCustomer-FirstName" + i);
            info.setLastName("TestCustomer-LastName" + i);
            newCustomer.setPersonalInfo(info);
            customerRepo.persistCustomer(newCustomer);
            newAccount.getCustomers().add(newCustomer);
            newCustomer.setAccount(newAccount);
        }
    }
}
