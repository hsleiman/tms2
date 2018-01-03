/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.objectbrains.sti.service.customerinfo;

import com.objectbrains.sti.db.entity.base.account.Account;
import com.objectbrains.sti.db.entity.base.account.DebtAccount;
import com.objectbrains.sti.db.repository.account.AccountRepository;
import com.objectbrains.sti.db.repository.customer.CustomerRepository;
import com.objectbrains.sti.db.repository.customerinfo.DebtAccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author Bishistha
 */
@Service
@Transactional
public class DebtAccountService {
    
    @Autowired
    private DebtAccountRepository debtAccountRepo;
    
    @Autowired
    private AccountRepository accountRepo;
    
      @Autowired
    private CustomerRepository customerRepo;
    
    
    public void addDebtAccount(DebtAccount debtAccount, long accountPK) {
        Account account = accountRepo.findAccountByPk(accountPK);
        if (debtAccount != null) {
            if (debtAccount.getPk() <= 0) {
                debtAccountRepo.persistDebtAccount(debtAccount);
                account.getDebtAccounts().add(debtAccount);
                debtAccount.setAccount(account);
            } else {
                debtAccountRepo.mergeDebtAccount(debtAccount);
            }
        }
    }

 
}
