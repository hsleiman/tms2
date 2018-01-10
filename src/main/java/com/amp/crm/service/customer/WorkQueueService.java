/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.crm.service.customer;

import com.amp.crm.db.entity.base.WorkQueue;
import com.amp.crm.db.entity.base.account.Account;
import com.amp.crm.db.repository.account.AccountRepository;
import com.amp.crm.exception.WorkQueueNotFoundException;
import java.util.Set;
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
public class WorkQueueService {
    
    @Autowired
    private AccountRepository accountRepo;
    
    public static final Logger LOG = LoggerFactory.getLogger(WorkQueueService.class);
    
    public Set<WorkQueue> getAccountPortfolioQueues(long accountPk) throws WorkQueueNotFoundException {
        Account account = accountRepo.findAccountByPk(accountPk);
        Set<WorkQueue> workQueues = account.getWorkQueues();
        if (workQueues == null || workQueues.isEmpty()) {
            throw new WorkQueueNotFoundException("No Queues assigned to this loan", account.getPk());
        }
        return workQueues;

    }
}
