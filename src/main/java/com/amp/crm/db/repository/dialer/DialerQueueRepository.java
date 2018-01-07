/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.crm.db.repository.dialer;

import com.objectbrains.hcms.annotation.ConfigContext;
import com.objectbrains.hcms.configuration.ConfigurationUtility;
import com.amp.crm.constants.CallerId;
import com.amp.crm.constants.DialerQueueType;
import com.amp.crm.constants.DoNotCallCodes;
import com.amp.crm.db.entity.agent.DialerGroup;
import com.amp.crm.db.entity.base.account.Account;
import com.amp.crm.db.entity.base.dialer.StiCallerId;
import com.amp.crm.db.entity.base.dialer.DialerQueue;
import com.amp.crm.db.entity.base.dialer.DialerQueueGroupAssociation;
import com.amp.crm.db.entity.base.dialer.DialerQueueSettings;
import com.amp.crm.db.entity.base.dialer.HoldMusic;
import com.amp.crm.db.entity.base.dialer.OutboundDialerRecord;
import com.amp.crm.db.entity.base.dialer.OutboundAccountDetails;
import com.amp.crm.db.entity.base.dialer.VoiceRecording;
import com.amp.crm.db.hibernate.ThreadAttributes;
import com.amp.crm.db.repository.account.AccountRepository;
import com.amp.crm.embeddable.DialerQueueDetails;
import com.amp.crm.exception.ObjectNotFoundException;
import com.amp.crm.exception.CrmException;
import com.amp.crm.pojo.CustomerCallablePojo;
import com.amp.crm.pojo.QueuePkName;
import com.amp.crm.service.dialer.PhoneNumberCallable;
import com.amp.crm.constants.CallTimeCode;
import com.amp.crm.service.utility.ZipTimeZoneService;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.Session;
import org.hibernate.type.StandardBasicTypes;
import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author Hoang, J, Bishistha
 */
@Repository
public class DialerQueueRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private AccountRepository accountRepo;
    
    @Autowired
    private ZipTimeZoneService zipTimeZoneService;
    
    @ConfigContext
    private ConfigurationUtility configUtil;

    private static final Logger LOG = LoggerFactory.getLogger(DialerQueueRepository.class);

    public DialerQueue locateDialerQueueByPk(long pk) {
        DialerQueue dQ = entityManager.find(DialerQueue.class, pk);
        if (dQ == null) {
            throw new ObjectNotFoundException(pk, DialerQueue.class);
        }
        return dQ;
    }

    public DialerQueue locateDialerQueueByNameAndType(String queueName, DialerQueueType dqType) {
        TypedQuery<DialerQueue> q = entityManager.createNamedQuery("DialerQueue.LocateByNameAndType", DialerQueue.class);
        q.setParameter("queueName", queueName);
        q.setParameter("dqType", dqType);
        List<DialerQueue> list = q.getResultList();
        if (list == null || list.isEmpty()) {
            return null;
        }
        return list.get(0);
    }

    public List<DialerQueue> getAllDialerQueues() {
        TypedQuery<DialerQueue> q = entityManager.createNamedQuery("DialerQueue.LocateAll", DialerQueue.class);
        return q.getResultList();
    }

    public List<DialerQueue> getAllDialerQueuesOrderedByDate() {
        TypedQuery<DialerQueue> q = entityManager.createNamedQuery("DialerQueue.LocateAllOrderedByDate", DialerQueue.class);
        return q.getResultList();
    }

    public DialerQueueSettings locateDQSettingsByPk(long pk) {
        DialerQueueSettings dqSettings = entityManager.find(DialerQueueSettings.class, pk);
        if (dqSettings == null) {
            throw new ObjectNotFoundException(pk, DialerQueueSettings.class);
        }
        return dqSettings;
    }

    public void createDialerQueue(DialerQueue queue) {
        entityManager.persist(queue);
    }

    public void updateDialerQueue(DialerQueue queue) {
        queue.setUpdatedTime(LocalDateTime.now());
        entityManager.merge(queue);
    }

    public Long createDQSettings(DialerQueueSettings dqSettings, DialerQueue queue) {
        dqSettings.associateSettingsToQueue(queue);
        dqSettings.setChangeHistory(LocalDateTime.now()+" Created by "+ThreadAttributes.getUserData(ThreadAttributes.get("agent.username")).getUserName());
        entityManager.persist(dqSettings);
        return dqSettings.getDialerQueuePk();
    }

    public List<DialerQueueSettings> getDialerQueueSettings() {
        TypedQuery<DialerQueueSettings> query = entityManager.createNamedQuery("DialerQueueSettings.LocateAll", DialerQueueSettings.class);
        return query.getResultList();
    }

    @SuppressWarnings("unchecked")
    public List<Long> executeSqlScript(String sqlQuery) {
        Session session = entityManager.unwrap(Session.class);
        return session.createSQLQuery(sqlQuery).addScalar("pk", StandardBasicTypes.LONG).list();
    }

    public List<Number> getAccountPksInWorkQueue(long queuePk) {
        TypedQuery<Number> q = entityManager.createQuery("SELECT s.pk FROM Account s WHERE trim(lower(s.accountWorkQueue.pk)) = :queuePk", Number.class);
        q.setParameter("queuePk", queuePk);
        List<Number> list = (List<Number>) q.getResultList();
        return list;
    }

    public List<QueuePkName> getAllWorkQueues() {
        TypedQuery<QueuePkName> q = entityManager.createQuery("SELECT new " + QueuePkName.class.getName() + "(s.pk, s.workQueueData.queueName) FROM WorkQueue s WHERE s.workQueueData.active = true", QueuePkName.class);
        List<QueuePkName> queueList = q.getResultList();
        return queueList;
    }

    public boolean isAccountInQueue(DialerQueue queue, Account account) {
        TypedQuery<Account> q = entityManager.createNamedQuery("DialerQueue.LocateAccountInDialerQueue", Account.class);
        q.setParameter("accountPk", account.getPk());
        q.setParameter("dialerQueuePk", queue.getPk());
        return !q.getResultList().isEmpty();
    }

    public void addAccountToDialerQueue(Account account, DialerQueue queue) {
        addAccountsToDialerQueue(queue, Arrays.asList(account.getPk()));
        entityManager.refresh(account);
    }

    public void removeAccountFromDialerQueue(Account account, DialerQueue queue) {
        removeAccountsFromDialerQueue(queue, Arrays.asList(account.getPk()));
        entityManager.refresh(account);
    }

    private void updateCount(DialerQueue queue, int incrementBy) {
        DialerQueueDetails dqDetails = queue.getDialerQueueDetails();
        long count = dqDetails.getAccountCount();
        dqDetails.setAccountCount(count + incrementBy);
    }

    public int removeAllAccountsFromDialerQueue(DialerQueue queue) {
        return removeAccountsFromDialerQueue(queue, null);
    }

    public int removeAccountsFromDialerQueue(DialerQueue queue, List<Long> accountPks) {
        //list is null means remove all, if empty do nothing
        if (accountPks != null && accountPks.isEmpty()) {
            return 0;
        }
        Integer inClauseLimit = configUtil.getInteger("default.postgres.in.clause.limit.for.remove.accounts", 30000);
        
        if(accountPks == null || accountPks.size() <= inClauseLimit){
            return removeAccountsFromDQ(queue, accountPks);
        }
        Integer totalSize = accountPks.size();
        int totalAccountsUpdated = 0;
        int numberOfBatches = getNumberOfBatches(totalSize, inClauseLimit);        
        for(int i=0 ; i < numberOfBatches; i++){
            List<Long> accountPkSublist = new ArrayList<>();           
            int fromIndex = i*inClauseLimit;
            int toIndex = totalSize < fromIndex+inClauseLimit ? totalSize : fromIndex+inClauseLimit;
            accountPkSublist = accountPks.subList(fromIndex, toIndex);
            LOG.info("fromIndex : "+fromIndex+"; toIndex : "+toIndex+"; accountPksubList size : "+accountPkSublist.size());            
            LOG.info(" TotalAccounts : "+accountPks.size()+" to be removed from qeue: "+queue+"; Number of batches : "+numberOfBatches+" batch Size : "+inClauseLimit);
            int n = removeAccountsFromDQ(queue, accountPkSublist);
            totalAccountsUpdated = totalAccountsUpdated+n;
        }
        return totalAccountsUpdated;
    }
    
    private int removeAccountsFromDQ(DialerQueue queue, List<Long> accountPks){
        int n = entityManager.createNamedQuery(queue.isOutbound() ? "OutboundDialerQueue.RemoveAccounts" : "InboundDialerQueue.RemoveAccounts")
                .setParameter("dialerQueuePk", queue.getPk())
                .setParameter("accounts", accountPks)
                .setLockMode(LockModeType.PESSIMISTIC_WRITE)
                .executeUpdate();
        LOG.info("Removed {} accounts from queue {}", n, queue.getDialerQueueDetails().getQueueName());
        queue.getDialerQueueDetails().setAccountCount(queue.getDialerQueueDetails().getAccountCount() - n);
        return n;
    }
    
    public void addAccountsToDialerQueue(DialerQueue queue, List<Long> accountPks) {
        if (accountPks == null || accountPks.isEmpty()){
            return;
        }        
        Integer inClauseLimit = configUtil.getInteger("default.postgres.in.clause.limit", 30000);
        Integer totalSize = accountPks.size();
        if (totalSize <= inClauseLimit){
            addAccountsToDQ(queue, accountPks);
            return;
        }
        int numberOfBatches = getNumberOfBatches(accountPks.size(), inClauseLimit);        
        for(int i=0 ; i < numberOfBatches; i++){
            List<Long> accountPkSublist = new ArrayList<>();           
            int fromIndex = i*inClauseLimit;
            int toIndex = totalSize < fromIndex+inClauseLimit ? totalSize : fromIndex+inClauseLimit;
            accountPkSublist = accountPks.subList(fromIndex, toIndex);
            LOG.info("fromIndex : "+fromIndex+"; toIndex : "+toIndex+"; accountPksubList size : "+accountPkSublist.size());            
            LOG.info(" TotalAccounts : "+accountPks.size()+" to be moved to qeue: "+queue+"; Number of batches : "+numberOfBatches+" batch Size : "+inClauseLimit);
            addAccountsToDQ(queue, accountPkSublist);
        }                   
    }

    private int getNumberOfBatches(Integer totalSize, Integer limit){
        Integer numberOfBatches = 0;
        if(totalSize > limit){
            numberOfBatches = totalSize/limit;
            int remaining = totalSize-(numberOfBatches*limit);
            if(remaining > 0){
                numberOfBatches++;
            }
            return numberOfBatches;
        }
        return 1;
    }
    
    private void addAccountsToDQ(DialerQueue queue, List<Long> accountPks){
        List<Long> queuePks = entityManager.createNamedQuery(queue.isOutbound() ? "OutboundDialerQueue.LocateDQForAccounts" : "InboundDialerQueue.LocateDQForAccounts", Long.class)
                .setParameter("accounts", accountPks)
                .getResultList();

        int n = entityManager.createNamedQuery(queue.isOutbound() ? "OutboundDialerQueue.AddAccounts" : "InboundDialerQueue.AddAccounts")
                .setParameter("dialerQueuePk", queue.getPk())
                .setParameter("accounts", accountPks)
                .setLockMode(LockModeType.PESSIMISTIC_WRITE)
                .executeUpdate();

        if (n > 0) {
            queue.getDialerQueueDetails().setLastAccountAssignmentTimestamp(LocalDateTime.now());
            queuePks.add(queue.getPk());
            for (Long queuePk : queuePks) {
                updateAccountCount(queuePk);
            }
        }
        LOG.info("Assigned {} accounts to queue {}", n, queue.getDialerQueueDetails().getQueueName());
    }
    public Long getAccountCountForDialer(long queuePk) {
        Query q = entityManager.createNamedQuery("DialerQueue.AccountCount").setParameter("dialerQueuePk", queuePk);
        return ((Number) q.getSingleResult()).longValue();
    }

    // @Async("svc-antideadlock-executor")
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateAccountCount(long queuePk) {
        try {
            DialerQueue queue = locateDialerQueueByPk(queuePk);
            queue.getDialerQueueDetails().setAccountCount(getAccountCountForDialer(queue.getPk()));
        } catch (ObjectNotFoundException ex) {
            LOG.error("Error updating account count, queuePk [{}] not found.", queuePk);
        }
    }

    public List<Long> getDialerQueueAccounts(long queuePk) {
        TypedQuery<Long> q = entityManager.createNamedQuery("DialerQueue.GetAllAccounts", Long.class)
                .setParameter("dialerQueuePk", queuePk);
        return q.getResultList();
    }

    public List<Long> getDialerQueueAccounts(long queuePk, Integer pageNum, Integer pageSize) {
        if ((pageNum == null || pageNum < 0) || (pageSize == null || pageSize <= 0)) {
            return DialerQueueRepository.this.getDialerQueueAccounts(queuePk);
        }
        TypedQuery<Long> q = entityManager.createNamedQuery("DialerQueue.GetAllAccounts", Long.class)
                .setParameter("dialerQueuePk", queuePk)
                .setFirstResult(pageNum * pageSize)
                .setMaxResults(pageSize);
        return q.getResultList();
    }

    public DialerQueueGroupAssociation getQueueGroupAssociationByDialerQueue(DialerQueue dialerQueue) {
        TypedQuery<DialerQueueGroupAssociation> q = entityManager
                .createNamedQuery("DialerQueueGroupAssociation.LocateByDialerQueue",
                        DialerQueueGroupAssociation.class);
        q.setParameter("dialerQueue", dialerQueue);
        List<DialerQueueGroupAssociation> list = q.getResultList();
        return list.isEmpty() ? null : list.get(0);
    }
    

    public List<DialerQueueGroupAssociation> getQueueGroupAssociationByDialerGroup(DialerGroup dialerGroup) {
        TypedQuery<DialerQueueGroupAssociation> q = entityManager
                .createNamedQuery("DialerQueueGroupAssociation.LocateByDialerGroup",
                        DialerQueueGroupAssociation.class);
        q.setParameter("dialerGroup", dialerGroup);
        List<DialerQueueGroupAssociation> list = q.getResultList();
        return list;
    }
    
    public List<DialerQueueGroupAssociation> getQueueGroupAssociationBySecondaryDialerGroup(DialerGroup secondaryDialerGroup) {
        TypedQuery<DialerQueueGroupAssociation> q = entityManager
                .createNamedQuery("DialerQueueGroupAssociation.LocateBySecondaryDialerGroup",
                        DialerQueueGroupAssociation.class);
        q.setParameter("secondaryGroup", secondaryDialerGroup);
        List<DialerQueueGroupAssociation> list = q.getResultList();
        return list;
    }
    
    public List<DialerQueueGroupAssociation> getQueueGroupAssociationsByPrimaryAndSecondaryDialerGroup(DialerGroup dialerGroup) {
        TypedQuery<DialerQueueGroupAssociation> q = entityManager
                .createNamedQuery("DialerQueueGroupAssociation.LocateByPrimaryAndSecondaryDialerGroup",
                        DialerQueueGroupAssociation.class);
        q.setParameter("dialerGroup", dialerGroup);
        List<DialerQueueGroupAssociation> list = q.getResultList();
        return list;
    }

//    public List<Long> getSortedDialerQueueAccounts(long queuePk) {
//        Query q = entityManager.createNamedQuery("DialerQueue.GetAllAccountsInQueue");
//        q.setParameter("queuePk", queuePk);
//        @SuppressWarnings("unchecked")
//        List<Long> list = q.getResultList();
//        return list;
//    }
    public void createDialerQueueGroupAssociation(DialerQueue queue, DialerGroup group, DialerGroup secondaryGroup) {
        DialerQueueGroupAssociation assoc = new DialerQueueGroupAssociation();
        assoc.setDialerQueue(queue);
        assoc.setDialerGroup(group);
        assoc.setSecondaryGroup(secondaryGroup);
        assoc.getDialerQueueGroupPk().setDialerQueueType(queue.getDialerQueueDetails().getDialerQueueType());
        entityManager.persist(assoc);
    }

    public void removeDialerQueueGroupAssociation(DialerQueueGroupAssociation assoc) {
        if (assoc != null) {
            entityManager.remove(assoc);
            entityManager.flush();
        }
    }

    public List<DialerQueue> getAllDialerQueuesByType(DialerQueueType dqType) {
        TypedQuery<DialerQueue> q = entityManager.createNamedQuery("DialerQueue.GetAllDialerQueuesByType", DialerQueue.class);
        q.setParameter("dqType", dqType);
        return q.getResultList();
    }

    public List<VoiceRecording> getAllVoiceRecordings() {
        TypedQuery<VoiceRecording> q = entityManager.createNamedQuery("VoiceRecording.GetAllVoiceRecordings", VoiceRecording.class);
        return q.getResultList();
    }

    public VoiceRecording getVoiceRecordingByName(String fileName) {
        TypedQuery<VoiceRecording> q = entityManager.createNamedQuery("VoiceRecording.GetVoiceRecordingByName", VoiceRecording.class);
        q.setParameter("fileName", fileName);
        List<VoiceRecording> list = q.getResultList();
        return list.isEmpty() ? null : list.get(0);
    }
    
    public List<HoldMusic> getAllHoldMusic() {
        TypedQuery<HoldMusic> q = entityManager.createNamedQuery("HoldMusic.GetAllHoldMusic", HoldMusic.class);
        return q.getResultList();
    }

    public HoldMusic getHoldMusicByName(String fileName) {
        TypedQuery<HoldMusic> q = entityManager.createNamedQuery("HoldMusic.GetHoldMusicByName", HoldMusic.class);
        q.setParameter("fileName", fileName);
        List<HoldMusic> list = q.getResultList();
        return list.isEmpty() ? null : list.get(0);
    }

    public List<StiCallerId> getAllCallerIds() {
        TypedQuery<StiCallerId> q = entityManager.createNamedQuery("CallerId.GetAllCallerIds", StiCallerId.class);
        return q.getResultList();
    }

    public StiCallerId getCallerIdByNumber(Long callerIdNumber) {
        TypedQuery<StiCallerId> q = entityManager.createNamedQuery("CallerId.GetCallerIdByNumber", StiCallerId.class);
        q.setParameter("callerIdNumber", callerIdNumber);
        List<StiCallerId> list = q.getResultList();
        return list.isEmpty() ? null : list.get(0);
    }

    public DialerQueue findDialerQueueWithQueryPk(long queryPk, DialerQueueType dqType) {
        TypedQuery<DialerQueue> q = entityManager.createNamedQuery("DialerQueue.LocateByQueryPk", DialerQueue.class);
        q.setParameter("queryPk", queryPk);
        q.setParameter("dqType", dqType);
        List<DialerQueue> list = q.getResultList();
        return list.isEmpty() ? null : list.get(0);
    }
    
    public DialerQueue findDialerQueueWithQueryPk(long queryPk) {
        TypedQuery<DialerQueue> q = entityManager.createNamedQuery("DialerQueue.LocateWithQueryPk", DialerQueue.class);
        q.setParameter("queryPk", queryPk);
        List<DialerQueue> list = q.getResultList();
        return list.isEmpty() ? null : list.get(0);
    }

    public DialerQueue findDialerQueueWithWorkQueuePk(long workQueuePk, DialerQueueType dqType) {
        TypedQuery<DialerQueue> q = entityManager.createNamedQuery("DialerQueue.LocateByWorkQueuePk", DialerQueue.class);
        q.setParameter("workQueuePk", workQueuePk);
        q.setParameter("dqType", dqType);
        List<DialerQueue> list = q.getResultList();
        return list.isEmpty() ? null : list.get(0);
    }
    
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public OutboundDialerRecord createOutboundRecord(OutboundDialerRecord dqRecord){        
        entityManager.persist(dqRecord);
        return dqRecord;
    }
    
    public OutboundDialerRecord findOutboundDialerRecord(long pk){
        return entityManager.find(OutboundDialerRecord.class, pk);
    }
    
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void createOutboundAccountDetails(OutboundAccountDetails accountDetails, OutboundDialerRecord record){
        entityManager.persist(accountDetails);
        accountDetails.setOutboundDialerRecord(record);
    }
    
    public PhoneNumberCallable getPhoneNumberCallable(String zipCode, long areaCode) {
        CustomerCallablePojo bwrCallable = zipTimeZoneService.getCustomerCallable(zipCode, areaCode);
        PhoneNumberCallable pnc;
        if (bwrCallable.getDoNotCallCode() == DoNotCallCodes.TOO_EARLY_TO_CALL) {
            pnc = new PhoneNumberCallable(CallTimeCode.TOO_EARLY, bwrCallable.getTooEarly());
        } else if (bwrCallable.getDoNotCallCode() == DoNotCallCodes.TOO_LATE_TO_CALL) {
            pnc = new PhoneNumberCallable(CallTimeCode.TOO_LATE, bwrCallable.getTooEarly().plusDays(1));
        } else {
            pnc = new PhoneNumberCallable(CallTimeCode.OK_TO_CALL, null);
        }
        return pnc;
    }
    
    //manual sync
    public List<Long> executeDialerQueueSql(long dqPk) throws CrmException {
        DialerQueue dq = locateDialerQueueByPk(dqPk);
        executeSqlAndAssignAccounts(dq);
        return DialerQueueRepository.this.getDialerQueueAccounts(dqPk);
    }

    public void executeSqlAndAssignAccounts(DialerQueue queue) throws CrmException {
        String sqlQuery = queue.getDialerQueueDetails().getSqlQuery();
        if (StringUtils.isNotBlank(sqlQuery)) {
            LOG.info("About to execute query: {}", sqlQuery);
            List<Long> newAccountPks = executeSqlScript(sqlQuery);
            LOG.info("Query executed: {}", newAccountPks);
            List<Long> oldAccountPks = DialerQueueRepository.this.getDialerQueueAccounts(queue.getPk());
            List<Long> accountToRemove = new ArrayList<>(oldAccountPks);
            int count = removeAccountsFromDialerQueue(queue, accountToRemove);
            addAccountsToDialerQueue(queue, newAccountPks);
        }
    }

}

