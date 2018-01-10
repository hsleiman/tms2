/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.crm.service.dialer;

import com.amp.crm.constants.CallRoutingOption;
import com.amp.crm.constants.CallTimeCode;
import com.amp.crm.constants.CallerId;
import com.amp.crm.constants.DialerMode;
import com.amp.crm.constants.DialerQueueSourceType;
import com.amp.crm.constants.DialerQueueType;
import com.hazelcast.core.IAtomicLong;
import com.objectbrains.config.General;
import com.objectbrains.dms.iws.EmailInfo;
import com.objectbrains.hcms.annotation.ConfigContext;
import com.objectbrains.hcms.configuration.ConfigurationUtility;
import com.objectbrains.hcms.hazelcast.HazelcastService;
import com.amp.crm.db.entity.agent.AgentDialerGroup;
import com.amp.crm.db.entity.agent.DialerGroup;
import com.amp.crm.db.entity.base.account.Account;
import com.amp.crm.db.entity.base.WorkQueue;
import com.amp.crm.db.entity.base.customer.Customer;
import com.amp.crm.db.entity.base.customer.Phone;
import com.amp.crm.db.entity.disposition.CallDispositionCode;
import com.amp.crm.db.entity.disposition.CallDispositionGroup;
import com.amp.crm.db.hibernate.ThreadAttributes;
import com.amp.crm.db.repository.StiAgentRepository;
import com.amp.crm.db.repository.account.AccountRepository;
import com.amp.crm.db.repository.account.WorkLogRepository;
import com.amp.crm.db.repository.account.WorkQueueRepository;
import com.amp.crm.db.repository.customer.CustomerRepository;
import com.amp.crm.db.repository.dialer.DialerQueueRepository;
import com.amp.crm.exception.AccountNotInQueueException;
import com.amp.crm.exception.CrmException;
import com.amp.crm.ows.DocumentManagerOWS;
import com.amp.crm.service.tms.CallDispositionService;
import com.amp.crm.service.tms.DialerGroupService;
import com.amp.crm.service.tms.TMSService;
import com.amp.crm.service.utility.HttpClient;
import com.amp.crm.service.utility.PhoneUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.Hibernate;
import org.hibernate.proxy.HibernateProxy;
import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ResourceUtils;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.amp.crm.constants.DialerQueueType.OUTBOUND;
import com.amp.crm.constants.IncomingCallAgent;
import com.amp.crm.constants.PhoneNumberType;
import com.amp.crm.constants.PopupDisplayMode;
import com.amp.crm.constants.PreviewDialerType;
import com.amp.crm.constants.WorkLogTypes;
import com.amp.crm.db.entity.base.dialer.DialerQueryHistory;
import com.amp.crm.db.entity.base.dialer.DialerQueue;
import com.amp.crm.db.entity.base.dialer.DialerQueueGroupAssociation;
import com.amp.crm.db.entity.base.dialer.DialerQueueSettings;
import com.amp.crm.db.entity.base.dialer.DialerSettingsHistory;
import com.amp.crm.db.entity.base.dialer.HoldMusic;
import com.amp.crm.db.entity.base.dialer.InboundDialerQueue;
import com.amp.crm.db.entity.base.dialer.InboundDialerQueueSettings;
import com.amp.crm.db.entity.base.dialer.OutboundDialerQueue;
import com.amp.crm.db.entity.base.dialer.OutboundDialerQueueSettings;
import com.amp.crm.db.entity.base.dialer.OutboundDialerRecord;
import com.amp.crm.db.entity.base.dialer.StiCallerId;
import com.amp.crm.db.entity.base.dialer.VoiceRecording;
import com.amp.crm.embeddable.AccountData;
import com.amp.crm.embeddable.AgentCallOrder;
import com.amp.crm.embeddable.AgentWeightPriority;
import com.amp.crm.embeddable.DialerQueueDetails;
import com.amp.crm.embeddable.InboundDialerQueueRecord;
import com.amp.crm.embeddable.WeightedPriority;
import com.amp.crm.pojo.AccountCustomerName;
import com.amp.crm.pojo.BestTimeToCallPojo;
import com.amp.crm.pojo.DialerQueueGroup;
import com.amp.crm.pojo.DialerQueueRecord;
import com.amp.crm.pojo.NationalPhoneNumber;
import com.amp.crm.pojo.QueueAgentWeightPriority;
import com.amp.crm.pojo.QueuePkName;
import com.amp.crm.pojo.QueueRunningStatus;

/**
 * 
 */
@Service
@Transactional
public class DialerQueueService {

    protected static final String DEFAULT_INBOUND_QUEUE = "Customer Service";
    protected static final String DEFAULT_DIALER_GROUP = "Customer Service Group";
    private static final Logger LOG = LoggerFactory.getLogger(DialerQueueService.class);

    private QueueRunningStatusListCache queueRunningStatusListCache = new QueueRunningStatusListCache(new ArrayList<QueueRunningStatus>());

    @Autowired
    @Lazy
    private DialerQueueRepository dqRepo;
    @Autowired
    private AccountRepository accountRepo;
    @Autowired
    private CustomerRepository customerRepo;
    //    @Autowired
//    private CustomerRepository bwrRepo;
    @Autowired
    private StiAgentRepository agentRepo;
    @Autowired
    private HazelcastService hzService;
    @Autowired
    private WorkQueueRepository workQueueRepo;
    @Autowired
    private DialerGroupService dialerGroupService;
    @Autowired
    private TMSService tmsService;
    @Autowired
    private CallDispositionService callDispositionService;
    @Autowired
    private WorkLogRepository workLogRepo;
    @Autowired
    @Lazy
    private DocumentManagerOWS documentManagerOWS;
    @ConfigContext
    private General generalConfig;
    @ConfigContext
    private ConfigurationUtility config;
    @PersistenceContext
    private EntityManager entityManager;
    @Autowired
    private DialerAccountPhoneData dialerAccountPhoneData;

    public DialerQueueDetails createDialerQueue(String queueName, String sqlQuery, DialerQueueType dqType) throws CrmException {
        return instantiateDialerQueue(queueName, sqlQuery, dqType, DialerQueueSourceType.SQL).getDialerQueueDetails();
    }

    @SuppressWarnings("unchecked")
    public DialerQueueDetails createDialerQueueFromQuery(String query, DialerQueueType dqType) throws CrmException {
        validateQuery(query, null, dqType);
        //query = queryBuilderService.createOrUpdateQuery(query, Collections.EMPTY_LIST);
        //TO DO Run given input query and associate result set of loans with Dialer Queue
        //DialerQueue queue = instantiateDialerQueue(query.getName(), query.getSql(), dqType, DialerQueueSourceType.QUERY_BUILDER);
        //queue.setQuery(query);
        //queue.getDialerQueueDetails().setQueryPk(query.getPk());
        //return queue.getDialerQueueDetails();
        return null;
    }

    public DialerQueueDetails createDialerQueueFromCollectionQueue(long workQueuePk, DialerQueueType dqType) throws CrmException {
        DialerQueue queue = dqRepo.findDialerQueueWithWorkQueuePk(workQueuePk, dqType);
        if (queue != null) {
            throw new CrmException("Collection Queue is already assigned to Dialer Queue " + queue.getPk());
        }
        WorkQueue cq = workQueueRepo.getWorkQueue(workQueuePk);
        String sqlQuery = generateSqlQueryForWorkQueue(workQueuePk);
        queue = instantiateDialerQueue(cq.getWorkQueueData().getQueueName(), sqlQuery, dqType, DialerQueueSourceType.COLLECTION_QUEUE);
        queue.setWorkQueue(cq);
        queue.getDialerQueueDetails().setWorkQueuePk(cq.getPk());
        return queue.getDialerQueueDetails();
    }

    public DialerQueueDetails createDialerQueueFromDestinationNumber(String queueName, String query) throws CrmException {
        DialerQueue queue = instantiateDialerQueue(queueName, "", DialerQueueType.INBOUND, DialerQueueSourceType.DESTINATION_NUMBER);
        queue.getDialerQueueDetails().setDestinationNumbers(query);
        return queue.getDialerQueueDetails();
    }

    private DialerQueue instantiateDialerQueue(String queueName, String sqlQuery, DialerQueueType dqType, DialerQueueSourceType queueSourceType) throws CrmException {
        DialerQueueDetails dqDetails = new DialerQueueDetails();
        dqDetails.setDialerQueueType(dqType);
        dqDetails.setQueueName(queueName);
        dqDetails.setSqlQuery(sqlQuery);
        dqDetails.setDialerQueueSourceType(queueSourceType);
        findExistingDq(dqDetails);
        DialerQueue queue = instantiateDialerQueue(dqType);
        queue.setDialerQueueDetails(dqDetails);
        dqRepo.createDialerQueue(queue);
        dqDetails.setPk(queue.getPk());
        instantiateDQSettings(dqDetails);
        resetDialerQueueAccountIterator(queue.getPk());
        dqRepo.executeSqlAndAssignAccounts(queue);
        return queue;
    }

    private DialerQueue instantiateDialerQueue(DialerQueueType dqType) {
        DialerQueue queue;
        if (dqType == DialerQueueType.INBOUND) {
            queue = new InboundDialerQueue();
        } else {
            queue = new OutboundDialerQueue();
        }
        return queue;
    }

    @SuppressWarnings("unchecked")
    public DialerQueueDetails updateDialerQueue(DialerQueueDetails dqDetails) throws CrmException {
        DialerQueue queue = findExistingDq(dqDetails);
        if (queue == null) {
            queue = dqRepo.locateDialerQueueByPk(dqDetails.getPk());
        }
        EmailInfo info = new EmailInfo();
        String sender;
        String receiver;
        String oldQuery = queue.getDialerQueueDetails().getSqlQuery();
        String newQuery = dqDetails.getSqlQuery(); //Strings to populate the email
        if (generalConfig.isProduction()) {
            sender = config.getString("dialer.query.email.sender", "cs@objectbrains.com");
            receiver = config.getString("dialer.query.email.recipient.production", "hussien.sleiman@objectbrains.com");
        } else {
            sender = config.getString("dialer.query.email.sender", "cs@objectbrains.com");
            receiver = config.getString("dialer.query.email.recipient.test", "calvin.huynh@objectbrains.com");
        }
        info.setSender(sender);
        info.setReceiverList(receiver);
        info.setSubject("Dialer " + queue.getDialerQueueDetails().getPk() + " query has been updated");
        info.setCanSaveWithoutLoan(Boolean.TRUE);

        dqDetails.setDialerQueueSourceType(queue.getDialerQueueDetails().getDialerQueueSourceType());
        if (dqDetails.getQueryPk() == null && StringUtils.isBlank(dqDetails.getSqlQuery()) && dqDetails.getWorkQueuePk() == null && StringUtils.isBlank(dqDetails.getDestinationNumbers())) {
            throw new CrmException("Dialer Queue should either be based on query, custom sql, destination number or work queue.");
        }
        boolean isQueryBased = dqDetails.getQueryPk() != null || (dqDetails.getTableGroupPk() != null && !CollectionUtils.isEmpty(dqDetails.getCriteriaSetPks()));
        if (dqDetails.getWorkQueuePk() != null && isQueryBased) {
            throw new CrmException("Dialer Queue cannot be assigned to both query and work queue.");
        }
        WorkQueue workQueue = queue.getWorkQueue();
        if (workQueue != null && workQueue.getPk() != dqDetails.getWorkQueuePk()) {
            throw new CrmException("Cannot update dialer queue. It is associated with work queue [" + workQueue.getWorkQueueData().getQueueName() + "].");
        }
        /*Query query = queue.getQuery();
         if (isQueryBased) {
         if (query == null) {
         //dq is being updated as query based
         query = new Query();
         } else {
         validateQuery(query, queue.getPk(), dqDetails.getDialerQueueType());
         }
         LOG.info("update query based DialerQueue Name: {}", dqDetails.getQueueName());
         query.setName(dqDetails.getQueueName());
         query.setCriteriaSetPks(dqDetails.getCriteriaSetPks());
         query.setTableGroupPk(dqDetails.getTableGroupPk());
         query = queryBuilderService.createOrUpdateQuery(query, Collections.EMPTY_LIST);
         LOG.info("Sql: {}", query.getSql());
         dqDetails.setSqlQuery(query.getSql());
         queue.setQuery(query);
         }
         long queueCount = queue.getDialerQueueDetails().getAccountCount();
         if (dqDetails.isActive() != null && !dqDetails.isActive() && queueCount > 0) {
         throw new CrmException("Cannot deactivate Dialer Queue. " + queueCount + " account(s) currently exist/s in the queue.");
         }
         if (queue.getDialerQueueDetails().getDialerQueueSourceType() != DialerQueueSourceType.DESTINATION_NUMBER) {
         dqDetails.setAccountCount(updateSqlQueryForDQ(queue, dqDetails.getSqlQuery()));
         } else {
         oldQuery = queue.getDialerQueueDetails().getDestinationNumbers();
         dqDetails.setDestinationNumbers(dqDetails.getSqlQuery());
         dqDetails.setSqlQuery("");
         //move the information passed in as the query to the Destination numbers and replace the query with the empty string
         }
         queue.setDialerQueueDetails(dqDetails);
         dqRepo.updateDialerQueue(queue);
         if (!oldQuery.equals(newQuery)) {
         String username = ThreadAttributes.getUserData(ThreadAttributes.get("agent.username")).getUserName();
         DialerQueryHistory history = new DialerQueryHistory(oldQuery, newQuery, username);
         history.setDialer_queue_name(queue.getDialerQueueDetails().getQueueName());
         history.setDialer_queue_pk(queue.getDialerQueueDetails().getPk());
         entityManager.persist(history);
         if (config.getBoolean("dialer.query.email.enabled", Boolean.TRUE)) {
         String message = "Username: " + username
         + "\nTime: " + LocalDateTime.now().toString()
         + "\nPk: " + queue.getDialerQueueDetails().getPk()
         + "\nQueue Name: " + queue.getDialerQueueDetails().getQueueName()
         + "\n\n\nOld Query: " + oldQuery
         + "\n\n\nNew Query: " + newQuery;
         info.setLetterBodyText(message);
         documentManagerOWS.sendToEmailQueue(info, null, null, null);
         }
         }*/
        return dqDetails;
    }

    private void validateQuery(String query, Long queuePk, DialerQueueType dqType) throws CrmException {
        /*DialerQueue existing = dqRepo.findDialerQueueWithQueryPk(query.getPk(), dqType);
         if (existing != null && (queuePk == null || existing.getPk() != queuePk)) {
         throw new CrmException("Cannot update dialer queue. Query " + query.getName() + " is already referenced by dialer queue " + existing.getDialerQueueDetails().getQueueName());
         }*/
    }

    private DialerQueueSettings instantiateDQSettings(DialerQueueDetails dqDetails) throws CrmException {
        DialerQueueSettings dqSettings;
        if (dqDetails.getDialerQueueType() == DialerQueueType.INBOUND) {
            dqSettings = new InboundDialerQueueSettings();
        } else {
            dqSettings = new OutboundDialerQueueSettings();
        }
        dqSettings.setDialerQueuePk(dqDetails.getPk());
        createOrUpdateDQSettings(dqSettings);
        return dqSettings;
    }

    public String generateSqlQueryForWorkQueue(long workQueuePk) throws CrmException {
        WorkQueue queue = workQueueRepo.getWorkQueue(workQueuePk);
        return "SELECT sv_account.pk FROM svc.sv_account WHERE sv_account.work_queue_pk = " + workQueuePk;
    }

    private DialerQueue findExistingDq(DialerQueueDetails dqDetails) throws CrmException {
        if (dqDetails == null) {
            throw new CrmException("Please provide the dialer queue details");
        }
        if (StringUtils.isBlank(dqDetails.getQueueName())) {
            throw new CrmException("Please provide a name for the dialer queue.");
        }
        if (dqDetails.getDialerQueueType() == null) {
            throw new CrmException("Please provide dialer queue type");
        }
        DialerQueue existingDq = dqRepo.locateDialerQueueByNameAndType(dqDetails.getQueueName(), dqDetails.getDialerQueueType());
        if (existingDq != null && existingDq.getPk() != dqDetails.getPk()) {
            throw new CrmException("Found DialerQueue with the same name: " + dqDetails.getQueueName());
        }
        return existingDq;
    }

    public DialerQueue getDialerQueueByNameAndType(String queueName, DialerQueueType dqType) {
        return dqRepo.locateDialerQueueByNameAndType(queueName, dqType);
    }

    public DialerQueueSettings createOrUpdateDQSettings(DialerQueueSettings dqSettings) throws CrmException {
        String username = ThreadAttributes.getUserData(ThreadAttributes.get("agent.username")).getUserName();
        EmailInfo info = new EmailInfo();
        String sender;
        String receiver;
        String newSettings = "";
        String oldSettings = ""; //Strings to populate the email
        DialerQueueSettings prevDQSettings = null;
        Boolean settingsChanged = false;
        if (generalConfig.isProduction()) {
            sender = config.getString("dialer.settings.email.sender", "cs@objectbrains.com");
            receiver = config.getString("dialer.settings.email.recipient.production", "hussien.sleiman@objectbrains.com");
        } else {
            sender = config.getString("dialer.settings.email.sender", "cs@objectbrains.com");
            receiver = config.getString("dialer.settings.email.recipient.test", "calvin.huynh@objectbrains.com");
        }

        if (dqSettings == null) {
            throw new CrmException("Please provide the Dialer Queue settings.");
        }
        DialerQueue queue = dqRepo.locateDialerQueueByPk(dqSettings.getDialerQueuePk());
        if (queue.getDialerQueueSettings() == null) {
            if (queue.isOutbound()) {
                dqSettings.setAutoAnswerEnabled(Boolean.TRUE);
            }
            dqRepo.createDQSettings(dqSettings, queue);
        } else {
            settingsChanged = true;
            newSettings = dqSettings.toStringForHistory();
            oldSettings = queue.getDialerQueueSettings().toStringForHistory();
            prevDQSettings = queue.getDialerQueueSettings();
            dqSettings.setDialerQueue(queue);

        }
        if (dqSettings instanceof InboundDialerQueueSettings) {
            setDefaultInboundQueueSettings((InboundDialerQueueSettings) dqSettings);
        } else {
            setDefaultOutboundQueueSettings((OutboundDialerQueueSettings) dqSettings);
        }
        if (dqSettings.getPopupDisplayMode() == null) {
            dqSettings.setPopupDisplayMode(PopupDisplayMode.NEW_WINDOW);
        }
        if (dqSettings.getIdleMaxMinutes() == null) {
            dqSettings.setIdleMaxMinutes(5);
        }
        if (dqSettings.getWrapMaxMinutes() == null) {
            dqSettings.setWrapMaxMinutes(5);
        }
        if (dqSettings.getStartTime() == null) {
            dqSettings.setStartTime(new LocalTime(8, 0));
        }
        if (dqSettings.getEndTime() == null) {
            dqSettings.setEndTime(new LocalTime(17, 0));
        }
        setDefaultWeightPriority(dqSettings.getWeightedPriority());

        if (config.getBoolean("createOrUpdateDQSettings.save.change.history", Boolean.TRUE) && prevDQSettings != null) {
            String prevHistory = "";
            if (queue.isOutbound()) {
                prevHistory = ((OutboundDialerQueueSettings) prevDQSettings).getChangeHistory();
                dqSettings.setChangeHistory((prevHistory == null ? "" : prevHistory) + "\n\n" + LocalDateTime.now() + " Changes by user \"" + username + "\" are : " + ((OutboundDialerQueueSettings) prevDQSettings).difference(dqSettings));
            } else {
                prevHistory = ((InboundDialerQueueSettings) prevDQSettings).getChangeHistory();
                dqSettings.setChangeHistory((prevHistory == null ? "" : prevHistory) + "\n\n" + LocalDateTime.now() + " Changes by user \"" + username + "\" are : " + ((InboundDialerQueueSettings) prevDQSettings).difference(dqSettings));
            }
        }
        entityManager.merge(dqSettings);
        if (settingsChanged) {

            DialerSettingsHistory history = new DialerSettingsHistory();
            history.setNew_Settings(newSettings);
            history.setOld_Settings(oldSettings);
            history.setUsername(username);
            entityManager.persist(history);
            if (config.getBoolean("dialer.settings.email.enabled", Boolean.TRUE)) {
                String columnedChanges = formatMessageBody(oldSettings, newSettings);
                String message = "Username: " + username
                        + "\n\n\nTime: " + LocalDateTime.now().toString()
                        + "\nPk: " + queue.getDialerQueueDetails().getPk()
                        + "\nQueue Name: " + queue.getDialerQueueDetails().getQueueName()
                        + "\n\n\nOld Settings\t\t\t\t\t\tNew Settings\n" + columnedChanges;
                info.setLetterBodyText(message);
                info.setSender(sender);
                info.setReceiverList(receiver);
                info.setSubject("Dialer " + queue.getDialerQueueDetails().getPk() + " settings have been updated");
                info.setCanSaveWithoutLoan(Boolean.TRUE);
                documentManagerOWS.sendToEmailQueue(info, null, null, null);
            }
        }

        return dqSettings;
    }

    private String formatMessageBody(String oldMessage, String newMessage) {
        oldMessage = oldMessage.replace("]", "");
        oldMessage = oldMessage.replace("[", "");
        newMessage = newMessage.replace("]", "");
        newMessage = newMessage.replace("[", "");
        String[] oldSettings = oldMessage.split(", ");
        String[] newSettings = newMessage.split(", ");
        String messageBody = "";
        if (oldSettings.length == newSettings.length) {
            for (int i = 0; i < oldSettings.length; i++) {
                if (oldSettings[i].equals(newSettings[i])) {
                    messageBody = messageBody + String.format("%-60s %s\n", oldSettings[i], newSettings[i]);
                } else {
                    messageBody = messageBody + "--> " + String.format("%-60s %s\n", oldSettings[i], newSettings[i]);
                }
                if (i == 8 || i == 36) {
                    messageBody = messageBody + "\n\n";
                }
            }
        }
        return messageBody;
    }

    private void setDefaultWeightPriority(WeightedPriority weightedPriority) {
        if (weightedPriority.getPriority() == null) {
            weightedPriority.setPriority(5);
        }
        if (weightedPriority.getWeight() == null) {
            weightedPriority.setWeight(5);
        }
    }

    private void setDefaultInboundQueueSettings(InboundDialerQueueSettings dqSettings) {
        if (dqSettings.getAgentCallOrder().isEmpty()) {
            List<AgentCallOrder> agentCallOrder = new ArrayList<>();
            agentCallOrder.add(new AgentCallOrder(IncomingCallAgent.PRIMARY_AGENT));
            agentCallOrder.add(new AgentCallOrder(IncomingCallAgent.QUEUE_GROUP_AGENTS));
            agentCallOrder.add(new AgentCallOrder(IncomingCallAgent.SECONDARY_AGENTS));
            dqSettings.setAgentCallOrder(agentCallOrder);
        }
        if (dqSettings.getCallRoutingOption() == null) {
            dqSettings.setCallRoutingOption(CallRoutingOption.ROUND_ROBIN);
        }
    }

    private void setDefaultOutboundQueueSettings(OutboundDialerQueueSettings dqSettings) {
        if (dqSettings.getDialerMode() == null) {
            dqSettings.setDialerMode(DialerMode.PREVIEW);
            dqSettings.setPreviewDialerType(PreviewDialerType.REGULAR);
        }
        if (dqSettings.getCallerId() == null) {
            dqSettings.setCallerId(CallerId.ACTUAL);
        }
        if (dqSettings.getPhoneTypesAllowed() == null) {
            dqSettings.setPhoneTypesAllowed("0,2,3");
        }
    }

    public DialerQueueDetails getDialerQueueByPk(long dqPk) throws CrmException {
        DialerQueue dq = dqRepo.locateDialerQueueByPk(dqPk);
        dq.getDialerQueueDetails().setPk(dq.getPk());
        return dq.getDialerQueueDetails();
    }

    //    public void removeAccountFromDialerQueue(long accountPk) throws CrmException {
//        Account accountData = accountRepo.locateByAccountPk(accountPk);
//        DialerQueue queue = accountData.getDialerQueue();
//        if (queue != null) {
//            queue.disassociateAccountFromDQ(accountData);
//            IAtomicLong at = hzService.getAtomicLong(queue.getDialerQueueDetails().getQueueName());
//            at.decrementAndGet();
//            LOG.debug("Account [{}] has been removed to dialer queue {}", accountPk, queue.getDialerQueueDetails().getQueueName());
//        }
//    }
    public List<Long> getDialerQueueAccounts(long dqPk) throws CrmException {
        DialerQueue queue = dqRepo.locateDialerQueueByPk(dqPk);
        return DialerQueueService.this.getDialerQueueAccounts(queue);
    }

    List<Long> getDialerQueueAccounts(DialerQueue queue) throws CrmException {
        //updateDQFromQueryBuilder(queue);
        return dqRepo.getDialerQueueAccounts(queue.getPk());
    }

    //check if criteria set/query has been updated
    private long updateSqlQueryForDQ(DialerQueue queue, String newSqlQuery) throws CrmException {
        if (!newSqlQuery.equals(queue.getDialerQueueDetails().getSqlQuery())) {
            LOG.info("Query changed for DQ: {}", queue.getDialerQueueDetails().getQueueName());
            queue.getDialerQueueDetails().setSqlQuery(newSqlQuery);
            dqRepo.executeSqlAndAssignAccounts(queue);
        }
        return queue.getDialerQueueDetails().getAccountCount();
    }

    public List<DialerQueueDetails> getAllDialerQueues() {
        return getDialerQueueDetailsList(dqRepo.getAllDialerQueues());
    }

    public List<DialerQueueDetails> getAllDialerQueuesByType(DialerQueueType dqType) {
        return getDialerQueueDetailsList(dqRepo.getAllDialerQueuesByType(dqType));
    }

    public List<DialerQueueDetails> getDialerQueuesForWorkQueue(long workQueuePk) {
        List<DialerQueueDetails> list = new ArrayList<>();
        WorkQueue queue = workQueueRepo.locateByWorkQueuePk(workQueuePk);
        InboundDialerQueue inbound = queue.getInboundDialerQueue();
        if (inbound != null) {
            list.add(inbound.getDialerQueueDetails());
        }
        OutboundDialerQueue outbound = queue.getOutboundDialerQueue();
        if (outbound != null) {
            list.add(outbound.getDialerQueueDetails());
        }
        return list;
    }

    private List<DialerQueueDetails> getDialerQueueDetailsList(List<DialerQueue> dialerQueueList) {
        List<DialerQueueDetails> dqDetails = new ArrayList<>();
        for (DialerQueue dq : dialerQueueList) {
            dq.getDialerQueueDetails().setPk(dq.getPk());
            dqDetails.add(dq.getDialerQueueDetails());
            DialerQueueGroupAssociation dialerQueueGroupAssociation = dqRepo.getQueueGroupAssociationByDialerQueue(dq);
            if (dialerQueueGroupAssociation != null) {
                if (dialerQueueGroupAssociation.getSecondaryGroup() != null) {
                    dq.getDialerQueueDetails().setSecondaryGroupPk(dialerQueueGroupAssociation.getSecondaryGroup().getPk());
                }
            }
        }
        return dqDetails;
    }

    public List<DialerQueueSettings> getAllDialerQueueSettings() {
        return dqRepo.getDialerQueueSettings();
    }

    public DialerQueueSettings getDialerQueueSettingsByDQPk(long queuePk) throws CrmException {
        DialerQueue queue = dqRepo.locateDialerQueueByPk(queuePk);
        return queue.getDialerQueueSettings();
    }

    public InboundDialerQueueSettings getInboundDQSettingsByDQPk(long queuePk) throws CrmException {
        return (InboundDialerQueueSettings) getDialerQueueSettingsByPkAndType(queuePk, DialerQueueType.INBOUND);
    }

    public OutboundDialerQueueSettings getOutboundDQSettingsByDQPk(long queuePk) throws CrmException {
        return (OutboundDialerQueueSettings) getDialerQueueSettingsByPkAndType(queuePk, OUTBOUND);
    }

    private DialerQueueSettings getDialerQueueSettingsByPkAndType(long queuePk, DialerQueueType dqType) throws CrmException {
        DialerQueue queue = dqRepo.locateDialerQueueByPk(queuePk);
        DialerQueueDetails queueDetails = queue.getDialerQueueDetails();
        if (queueDetails.getDialerQueueType() != dqType) {
            throw new CrmException("DialerQueue " + queueDetails.getQueueName() + " is not an " + dqType.name() + " queue.");
        }
        return queue.getDialerQueueSettings();
    }

    public List<Long> updateSqlQueryForDQ(long dqPk, String sqlQuery) throws CrmException {
        if (StringUtils.isBlank(sqlQuery)) {
            throw new CrmException("Please provide the sql query.");
        }
        DialerQueue dq = dqRepo.locateDialerQueueByPk(dqPk);
        updateSqlQueryForDQ(dq, sqlQuery);
        dqRepo.updateDialerQueue(dq);
        return dqRepo.getDialerQueueAccounts(dqPk);
    }

    public void assignWorkQueueAccountToDQ(Account account, WorkQueue workQ) {
        if (account == null || workQ == null) {
            return;
        }
        assignWorkQueueAccountToDQ(account, workQ.getInboundDialerQueue());
        assignWorkQueueAccountToDQ(account, workQ.getOutboundDialerQueue());
    }

    private void assignWorkQueueAccountToDQ(Account account, DialerQueue workDialerQueue) {
        if (workDialerQueue != null) {
            dqRepo.addAccountToDialerQueue(account, workDialerQueue);
        }
    }

    public void removeWorkQueueAccountFromDQ(Account account, WorkQueue workQ) {
        if (account == null) {
            return;
        }
        removeWorkQueueAccountFromDQ(account, workQ.getInboundDialerQueue(), account.getInboundDialerQueue());
        removeWorkQueueAccountFromDQ(account, workQ.getOutboundDialerQueue(), account.getOutboundDialerQueue());
    }

    private void removeWorkQueueAccountFromDQ(Account account, DialerQueue workDialerQueue, DialerQueue accountDialerQueue) {
        if (hasAssociation(workDialerQueue, accountDialerQueue)) {
            dqRepo.removeAccountFromDialerQueue(account, accountDialerQueue);
        }
    }

    private boolean hasAssociation(DialerQueue workDialerQueue, DialerQueue accountDialerQueue) {
        return workDialerQueue != null && accountDialerQueue != null && workDialerQueue.getPk() == accountDialerQueue.getPk();
    }

    public List<QueuePkName> getAllWorkQueues() {
        return dqRepo.getAllWorkQueues();
    }

    public InboundDialerQueueRecord getInboundDialerQueueRecord(long dqPk) throws CrmException {
        InboundDialerQueueRecord dqRecord = new InboundDialerQueueRecord(dqPk);
        instantiateDialerQueueRecord(dqPk, dqRecord);
        return dqRecord;
    }

    @SuppressWarnings("unchecked")
    <T extends DialerQueueSettings> DialerQueue instantiateDialerQueueRecord(long dqPk, DialerQueueRecord<T> dqRecord) throws CrmException {
        LocalDateTime start = new LocalDateTime();
        DialerQueue queue = dqRepo.locateDialerQueueByPk(dqPk);
        DialerQueueGroupAssociation assoc = dqRepo.getQueueGroupAssociationByDialerQueue(queue);
        if (assoc != null) {
            dqRecord.setAgentWeightPriorityList(getAgentWeightPriorityForDialerGroups(assoc.getDialerGroup(), assoc.getSecondaryGroup()));
        }
        dqRecord.setWeightedPriority(queue.getDialerQueueSettings().getWeightedPriority());
        dqRecord.setDialerQueueSettings((T) queue.getDialerQueueSettings());
        dqRecord.setCallDispositionGroup(getCallDispositionGroupForQueue(queue));
        LOG.info("Instantiate DialerQueueRecord took {} msec for dialerQueuePk {}", (new LocalDateTime().getMillisOfDay() - start.getMillisOfDay()), dqPk);
        return queue;
    }

    private List<AgentWeightPriority> getAgentWeightPriorityForDialerGroups(DialerGroup dialerGroup, DialerGroup svSecondaryDialerGroup) {
        List<AgentWeightPriority> agentWeightPriorityList = new ArrayList<>();
        if (dialerGroup != null) {
            agentWeightPriorityList.addAll(getAgentWeightPriorityListForGroups(dialerGroup, svSecondaryDialerGroup));
//            Set<DialerGroup> dialerGroups = dialerGroup.getSubDialerGroups();
//            for (DialerGroup dg : dialerGroups) {
//                agentWeightPriorityList.addAll(getAgentWeightPriorityForDialerGroups(dg, null));
//            }
        }
        return agentWeightPriorityList;
    }

    public List<AgentWeightPriority> getAgentWeightPriorityForDialerGroup(DialerGroup dialerGroup) {
        List<AgentWeightPriority> agentWeightPriorityList = new ArrayList<>();
        if (dialerGroup != null) {
            agentWeightPriorityList.addAll(getAgentWeightPriorityListForGroups(dialerGroup, null));
//            Set<DialerGroup> dialerGroups = dialerGroup.getSubDialerGroups();
//            for (DialerGroup dg : dialerGroups) {
//                agentWeightPriorityList.addAll(getAgentWeightPriorityForDialerGroup(dg));
//            }
        }
        return agentWeightPriorityList;
    }

    private List<AgentWeightPriority> getAgentWeightPriorityListForGroups(DialerGroup primaryGroup, DialerGroup secondaryGroup) {
        List<AgentWeightPriority> agentWeightPriorityList = new ArrayList<>();
        for (AgentDialerGroup agentDialerGroup : primaryGroup.getDialerGroupAgents()) {
            agentWeightPriorityList.add(setAgentWeightPriorityForGroup(agentDialerGroup, true));
        }
        if (secondaryGroup != null) {
            for (AgentDialerGroup agentDialerGroup : secondaryGroup.getDialerGroupAgents()) {
                boolean isAgentInList = false;
                for (AgentWeightPriority awp : agentWeightPriorityList) {
                    if (agentDialerGroup.getDialerAgent().getUserName().equals(awp.getUsername())) {
                        isAgentInList = true;
                    }
                }
                if (!isAgentInList) {
                    agentWeightPriorityList.add(setAgentWeightPriorityForGroup(agentDialerGroup, false));
                }
            }
        }
        return agentWeightPriorityList;
    }

    public List<AgentWeightPriority> getAgentWeightPriorityListForGroup(long dialerGroupPk) throws CrmException {
        DialerGroup dg = agentRepo.locateByDialerGroupPk(dialerGroupPk);
        return getAgentWeightPriorityForDialerGroup(dg);
    }

    public List<AgentWeightPriority> getAgentWeightPriorityListForDq(long dqPk) throws CrmException {
        DialerGroup primaryGroup = getPrimaryDialerGroupForDQ(dqPk);
        DialerGroup secondaryGroup = getSecondaryDialerGroupForDQ(dqPk);
        return getAgentWeightPriorityListForGroups(primaryGroup, secondaryGroup);
    }

    private AgentWeightPriority setAgentWeightPriorityForGroup(AgentDialerGroup agentDialerGroup, boolean isPrimaryGroup) {
        AgentWeightPriority agentWeightPriority = new AgentWeightPriority();
        agentWeightPriority.setUsername(agentDialerGroup.getDialerAgent().getUserName());
        agentWeightPriority.setFirstName(agentDialerGroup.getDialerAgent().getFirstName());
        agentWeightPriority.setLastName(agentDialerGroup.getDialerAgent().getLastName());
        agentWeightPriority.setLeader(agentDialerGroup.isLeader());
        agentWeightPriority.setGroupPk(agentDialerGroup.getDialerGroup().getPk());
        agentWeightPriority.setIsPrimaryGroup(isPrimaryGroup);
        if (agentDialerGroup.getAllowAfterHours() != null) {
            agentWeightPriority.setAllowAfterHours(agentDialerGroup.getAllowAfterHours());
        } else {
            agentWeightPriority.setAllowAfterHours(Boolean.FALSE);
        }
        WeightedPriority weightedPriority = agentDialerGroup.getWeightedPriority();
        setDefaultWeightPriority(weightedPriority);
        agentWeightPriority.setWeightedPriority(weightedPriority);
        return agentWeightPriority;
    }

    public List<QueueAgentWeightPriority> getQueueAgentWeightPriorityForUsername(String username) {
        List<QueueAgentWeightPriority> qawpList = new ArrayList<>();
        long agentPk = agentRepo.locateByAgentUserName(username).getPk();
        List<AgentDialerGroup> results = entityManager.createNamedQuery("AgentDialerGroup.LocateByAgentPk", AgentDialerGroup.class).
                setParameter("agentPk", agentPk).getResultList();
        for (AgentDialerGroup adg : results) {
            List<DialerQueueGroupAssociation> secondaryList = new ArrayList<>();
            List<DialerQueueGroupAssociation> primaryDqAssocList = dqRepo.getQueueGroupAssociationByDialerGroup(adg.getDialerGroup());
            List<DialerQueueGroupAssociation> secondaryDqAssocList = dqRepo.getQueueGroupAssociationBySecondaryDialerGroup(adg.getDialerGroup());

            for (DialerQueueGroupAssociation svdqa : primaryDqAssocList) {
                QueueAgentWeightPriority queueAgentWeightPriority = new QueueAgentWeightPriority(adg.getWeightedPriority(),
                        svdqa.getDialerQueue().getPk(), adg.isLeader(), adg.getAllowAfterHours(), svdqa.getDialerGroup().getPk(), true);
                qawpList.add(queueAgentWeightPriority);
            }

            for (DialerQueueGroupAssociation secondary : secondaryDqAssocList) {
                boolean isInList = false;
                for (DialerQueueGroupAssociation assoc : primaryDqAssocList) {
                    if (secondary.getDialerQueue().getPk() == assoc.getDialerQueue().getPk()) {
                        isInList = true;
                    }
                }
                if (!isInList) {
                    secondaryList.add(secondary);
                }
            }

            for (DialerQueueGroupAssociation svdqa : secondaryList) {
                QueueAgentWeightPriority queueAgentWeightPriority = new QueueAgentWeightPriority(adg.getWeightedPriority(),
                        svdqa.getDialerQueue().getPk(), adg.isLeader(), adg.getAllowAfterHours(), svdqa.getSecondaryGroup().getPk(), false);
                qawpList.add(queueAgentWeightPriority);
            }

        }

        return qawpList;
    }

    //returns all accounts assigned to queue, doesn't check valid phone numbers (for now), used to view all accounts satisfied by the sql
    @SuppressWarnings("unchecked")
    public List<AccountCustomerName> getBasicAccountDataForQueue(long queuePk, Integer pageNum, Integer pageSize) {
        List<AccountCustomerName> list = new ArrayList<>();
        // try {
        DialerQueue dq = dqRepo.locateDialerQueueByPk(queuePk);
        //updateDQFromQueryBuilder(dq);
        if (config.getBoolean("get.basic.account.data.without.dynamic.code", Boolean.FALSE)) {
            List<Long> accounts = dqRepo.getDialerQueueAccounts(queuePk, pageNum, pageSize);
            for (Long accountPk : accounts) {
                list.add(getAccountCustomerNameForAccount(accountPk));
            }
            return list;
        } else {
            int startIndex = 0;
            List<BigInteger> accounts = (List<BigInteger>) (Object) dialerAccountPhoneData.getCallableAccountsForDialerQueue(queuePk);
            if ((pageNum == null || pageNum < 0) || (pageSize == null || pageSize <= 0)) {
                pageSize = accounts.size();
                pageNum = 1;
            } else {
                pageNum = pageNum + 1; //Assuming pageNum starts from 0
                startIndex = pageNum * pageSize - pageSize;
            }
            if (startIndex < 0) {
                startIndex = 0;
            } else if (startIndex >= accounts.size()) {
                return null;
            }
            LOG.info("pageNum : " + pageNum + " pageSize : " + pageSize);
            for (int i = startIndex; i < startIndex + pageSize && i < accounts.size(); i++) {
                Long accountPk = accounts.get(i).longValue();
                list.add(getAccountCustomerNameForAccount(accountPk));
            }
        }
        /*  } catch (CrmException ex) {
         throw new EntityNotFoundException(ex.getMessage());
         }*/
        return list;
    }

    private AccountCustomerName getAccountCustomerNameForAccount(long accountPk) {
        Account account = entityManager.find(Account.class, accountPk);
        Customer bwr = account.getPrimaryCustomer();
        AccountCustomerName name = new AccountCustomerName();
        name.setFirstName(bwr.getPersonalInfo().getFirstName());
        name.setLastName(bwr.getPersonalInfo().getLastName());
        name.setAccountPk(account.getPk());
        return name;
    }

    public PhoneNumberCallable canCallNumberInQueue(long dqPk, long accountPk, long phoneNumber) throws CrmException, AccountNotInQueueException {
        int startCanCall = new LocalDateTime().getMillisOfDay();
        LOG.debug("canCallNumber start for dqPk: {} accountPk: {} and phoneNumber: {}", dqPk, accountPk, phoneNumber);
        DialerQueue queue = dqRepo.locateDialerQueueByPk(dqPk);
        Account account = accountRepo.findAccountByPk(accountPk);
        AccountData accountData = account.getAccountData();
        boolean isAccountInQueue = isAccountInQueue(queue, account);
        if (!isAccountInQueue) {
            throw new AccountNotInQueueException(accountPk, dqPk);
        }
        String sql = queue.getDialerQueueDetails().getSqlQuery();
        String tableName = " sv_account ";
        if (sql.contains("svc.sv_account")) {
            tableName = " svc.sv_account ";
        }
        sql = sql.replace(tableName, " svc.sv_account join (select * from svc.sv_account where pk = " + accountPk + ") account2 on svc.sv_account.pk = account2.pk ");
        LOG.debug("canCallNumberInQueue : sql to be executed: {}", sql);
        List<Long> n = dqRepo.executeSqlScript(sql);
        LOG.debug("Ended sql execution .. Took {} msec", (new LocalDateTime().getMillisOfDay() - startCanCall));
        if (n.isEmpty()) {
            LOG.info("canCallNumberInQueue {} for account {} is not in queue {} anymore.", phoneNumber, accountPk, dqPk);
            String note = "Account " + accountPk + " with PhoneNumber " + phoneNumber + " is not in the dialer queue " + queue.getDialerQueueDetails().getQueueName() + " anymore";
            workLogRepo.createWorkLog(account, note, WorkLogTypes.WORK_LOG_DIALER, null, false);
            throw new AccountNotInQueueException(accountPk, dqPk);
        }
        if (config.getBoolean("can.call.number.check.reviwed.time", Boolean.TRUE) && ((accountData.getLastReviewedDateTime() != null
                && accountData.getLastReviewedDateTime().isAfter(new LocalDateTime().minusHours(config.getInteger("can.call.number.hours.check", 6)))) || accountData.getLastReviewedDateTime() == null)) {
            if (accountData.isPendingBK() != null && accountData.isPendingBK()) {
                LOG.info("AccountPk {} has pendingBk ", accountPk);
                String note = "AccountPk " + accountPk + " has pendingBk";
                workLogRepo.createWorkLog(account, note, WorkLogTypes.WORK_LOG_DIALER, null, false);
                throw new CrmException("AccountPk " + accountPk + " has pendingBk or verbal c&d set");
            }
            boolean isVerbalCD = false;
            if (accountData.isVerbalCeaseAndDesist() != null && accountData.isVerbalCeaseAndDesist()) {
                isVerbalCD = true;
            }
            List<Phone> phones = new ArrayList<>();
            if (config.getBoolean("can.call.number.check.phones.new.way", Boolean.TRUE)) {
                LocalDateTime start = new LocalDateTime();
                LOG.info("Get phones new way at {}", start);
                for (Customer svBwr : account.getCustomers()) {
                    for (Phone svPhone : svBwr.getPhones()) {
                        if (svPhone.getPhoneData().getAreaCode() * 10000000 + svPhone.getPhoneData().getPhoneNumber() == phoneNumber) {
                            checkForDncAndVerbalCandD(svPhone, phoneNumber, isVerbalCD, account);
                        }
                    }
                }
                LOG.warn("Get phones for account {} and phone number {} new way took{} ms", accountData, phoneNumber, (new LocalDateTime().getMillisOfDay() - start.getMillisOfDay()));
            } else {
                LocalDateTime start = new LocalDateTime();
                LOG.info("Get phones old way at {}", start);
                phones = customerRepo.locatePhoneByPhoneNumberAndAccount(Long.toString(phoneNumber), accountPk);
                if (phones != null && !phones.isEmpty()) {
                    for (Phone svPhone : phones) {
                        checkForDncAndVerbalCandD(svPhone, phoneNumber, isVerbalCD, account);
                    }
                }
                LOG.warn("Get phones query for account {} and phone number {} old way took :{} ms", accountData, phoneNumber, (new LocalDateTime().getMillisOfDay() - start.getMillisOfDay()));
            }
        }

        LOG.debug("Dialer queue and phone check done. Calculating best time to call...");
        NationalPhoneNumber phone = PhoneUtils.parsePhoneNumber(phoneNumber);
        TypedQuery<String> q = entityManager.createQuery("SELECT addr.zip5 as zipCode"
                + " FROM Account account "
                + " join account.svCustomers bwr "
                + " join account.svAddresses addr "
                + " join bwr.customerPhone phone "
                + " WHERE account.pk = :accountPk"
                + " AND addr.svCustomer = bwr"
                + " AND phone.svPhoneCustomer = bwr"
                + " AND phone.areaCode = :areaCode"
                + " AND phone.phoneNumber = :phoneNumber"
                + " ORDER BY bwr.class, addr.class", String.class);
        q.setParameter("accountPk", accountPk);
        q.setParameter("areaCode", phone.getAreaCode());
        q.setParameter("phoneNumber", phone.getLocalNumber());
        q.setMaxResults(1);
        List<String> list = (List<String>) q.getResultList();
        String zipCode = null;
        if (!list.isEmpty()) {
            zipCode = (String) list.get(0);
        }
        PhoneNumberCallable phCallable = dqRepo.getPhoneNumberCallable(zipCode, phone.getAreaCode());
        LOG.info("canCallNumberInQueue {} calltimeCode :  {}", phoneNumber, phCallable.getCallTimeCode());
        if (phCallable.getCallTimeCode() != CallTimeCode.OK_TO_CALL) {
            String note = "PhoneNumber " + phoneNumber + " is not callable (" + phCallable.getCallTimeCode() + ") for dialer " + queue.getDialerQueueDetails().getQueueName();
            workLogRepo.createWorkLog(account, note, WorkLogTypes.WORK_LOG_DIALER, null, false);
        }
        LOG.debug("canCallNumberInQueue ended. Took {} msec", (new LocalDateTime().getMillisOfDay() - startCanCall));
        return phCallable;
    }

    public boolean hasNumberBeenCalled(String phoneNumber, long timeInSeconds) {
        List resultList = entityManager.createNativeQuery("select customer_phone_number from svc.sv_call_detail_record where customer_phone_number = :phoneNumber "
                + "and start_time > now() - interval '" + timeInSeconds + " seconds' and customer_phone_number is not null limit 1").setParameter("phoneNumber", phoneNumber).getResultList();
        return resultList != null && !resultList.isEmpty();
    }

    public boolean isAccountInQueue(long dqPk, long accountPk) throws CrmException {
        DialerQueue queue = dqRepo.locateDialerQueueByPk(dqPk);
        Account account = accountRepo.findAccountByPk(accountPk);
//        if (accountData.getLastContactTimestamp().equals(LocalDate.now())) {
//            return false;
//        }
        return dqRepo.isAccountInQueue(queue, account);
    }

    private boolean isAccountInQueue(DialerQueue queue, Account account) throws CrmException {
//        updateDQFromQueryBuilder(queue);
        return dqRepo.isAccountInQueue(queue, account);
    }

    public DialerQueueSettings getDialerQueueSettingsForAccount(long accountPk, boolean isIncoming) throws CrmException {
        DialerQueue dq = getDialerQueueForAccount(accountPk, isIncoming);
        if (dq == null) {
            return null;
        }
        return dq.getDialerQueueSettings();
    }

    public DialerQueue getDialerQueueForAccount(long accountPk, boolean isIncoming) throws CrmException {
        Account account = accountRepo.findAccountByPk(accountPk);
        if (isIncoming) {
            return account.getInboundDialerQueue();
        } else {
            return account.getOutboundDialerQueue();
        }
    }

    public void deleteDialerQueue(long dqPk) throws CrmException {
        DialerQueue queue = dqRepo.locateDialerQueueByPk(dqPk);
        DialerQueueSettings dqSettings = getDialerQueueSettingsByDQPk(dqPk);
        String query;
        if (queue.getDialerQueueDetails().getDialerQueueSourceType() != DialerQueueSourceType.DESTINATION_NUMBER) {
            query = queue.getDialerQueueDetails().getSqlQuery();
        } else {
            query = queue.getDialerQueueDetails().getDestinationNumbers();
        }
        DialerQueryHistory history = new DialerQueryHistory();
        history.setNew_Query("Dialer queue " + queue.getDialerQueueDetails().getPk() + " with name " + queue.getDialerQueueDetails().getQueueName() + " has been deleted");
        history.setOld_Query(query);
        history.setUsername(ThreadAttributes.getUserData(ThreadAttributes.get("agent.username")).getUserName());

        dqRepo.removeAllAccountsFromDialerQueue(queue);
        DialerQueueGroupAssociation assoc = dqRepo.getQueueGroupAssociationByDialerQueue(queue);
        dqRepo.removeDialerQueueGroupAssociation(assoc);
        /* if (queue.getQuery() != null) {
         //check if another dq with different type is using the same query
         DialerQueue existing = dqRepo.findDialerQueueWithQueryPk(queue.getQuery().getPk(), queue.isOutbound() ? DialerQueueType.INBOUND : DialerQueueType.OUTBOUND);
         if (existing == null) {
         queryBuilderService.deleteQuery(queue.getQuery().getPk());
         }
         }*/
        entityManager.remove(dqSettings);
        entityManager.remove(queue);
        entityManager.persist(history);
    }

    public void setDialerQueueGroupAssociation(long dqPk, long groupPk, Long secondaryGroupPk) throws CrmException {
        LOG.warn("setDialerQueueGroupAssociation dqPk :" + dqPk + " groupPk : " + groupPk);
        DialerQueue queue = dqRepo.locateDialerQueueByPk(dqPk);
        DialerGroup group = agentRepo.locateByDialerGroupPk(groupPk);
        DialerGroup secondaryGroup;
        if (secondaryGroupPk != null) {
            secondaryGroup = agentRepo.locateByDialerGroupPk(secondaryGroupPk);
        } else {
            secondaryGroup = null;
        }
        DialerQueueGroupAssociation assoc = dqRepo.getQueueGroupAssociationByDialerQueue(queue);
        String oldGroup = (assoc != null && assoc.getDialerGroup() != null) ? assoc.getDialerGroup().getGroupName() : "";
        String newGroup = group != null ? group.getGroupName() : "";
        StringBuilder log = new StringBuilder();
        String oldSecGroup = (assoc != null && assoc.getSecondaryGroup() != null) ? assoc.getSecondaryGroup().getGroupName() : "";
        String newSecGroup = (secondaryGroup != null) ? secondaryGroup.getGroupName() : "";
        LOG.info("setDialerQueueGroupAssociation queuePk {}, groupPk {} oldGroup {} new Group {} oldSecGroup {} newSecGroup {}", dqPk, groupPk, oldGroup, newGroup, oldSecGroup, newSecGroup);
        if (!oldGroup.equalsIgnoreCase(newGroup)) {
            log.append("\nPrimaryGroup : [oldValue : ").append(oldGroup).append("; newValue : ").append(newGroup).append("]");
        }
        if (!oldSecGroup.equalsIgnoreCase(newSecGroup)) {
            log.append("\nSecondaryGroup : [oldValue : ").append(oldSecGroup).append("; newValue : ").append(newSecGroup).append("]");
        }
        dqRepo.removeDialerQueueGroupAssociation(assoc);
        dqRepo.createDialerQueueGroupAssociation(queue, group, secondaryGroup);
        DialerQueueSettings dqSettings = getDialerQueueSettingsByDQPk(dqPk);
        LOG.info(" changeHistory : {} log {}", dqSettings.getChangeHistory(), log);
        dqSettings.setChangeHistory(dqSettings.getChangeHistory() + (StringUtils.isBlank(log) ? "" : "\n" + LocalDateTime.now() + log));
        LOG.info(" changeHistory : {} log {}", dqSettings.getChangeHistory(), log);
        entityManager.merge(dqSettings);
    }

    public DialerGroup getPrimaryDialerGroupForDQ(long queuePk) throws CrmException {
        DialerQueue queue = dqRepo.locateDialerQueueByPk(queuePk);
        DialerQueueGroupAssociation assoc = dqRepo.getQueueGroupAssociationByDialerQueue(queue);
        if (assoc != null) {
            return agentRepo.locateByDialerGroupPk(assoc.getDialerGroup().getPk());
        }
        return null;
    }

    public DialerGroup getSecondaryDialerGroupForDQ(long queuePk) throws CrmException {
        DialerQueue queue = dqRepo.locateDialerQueueByPk(queuePk);
        DialerQueueGroupAssociation assoc = dqRepo.getQueueGroupAssociationByDialerQueue(queue);
        if (assoc != null) {
            if (assoc.getSecondaryGroup() != null) {
                return agentRepo.locateByDialerGroupPk(assoc.getSecondaryGroup().getPk());
            }
        }
        return null;
    }

    public void removeDialerQueueGroupAssociation(long queuePk) throws CrmException {
        DialerQueue queue = dqRepo.locateDialerQueueByPk(queuePk);
        DialerQueueGroupAssociation assoc = dqRepo.getQueueGroupAssociationByDialerQueue(queue);
        DialerQueueSettings dqSettings = getDialerQueueSettingsByDQPk(queuePk);
        String log = "\n" + LocalDateTime.now() + "\nPrimaryGroup : [oldValue : " + assoc != null && assoc.getDialerGroup() != null ? assoc.getDialerGroup().getGroupName() : "" + "; newValue : null]";
        log = log + "\nSecondaryGroup : [oldValue : " + assoc != null && assoc.getSecondaryGroup() != null ? assoc.getSecondaryGroup().getGroupName() : "" + "; newValue : null]";
        dqSettings.setChangeHistory(dqSettings.getChangeHistory() + log);
        dqRepo.removeDialerQueueGroupAssociation(assoc);
    }

    public void resetDialerQueueAccountIterator(long queuePk) throws CrmException {
        DialerQueue queue = dqRepo.locateDialerQueueByPk(queuePk);
        resetDialerQueueAccountIterator(queue);
    }

    private void resetDialerQueueAccountIterator(DialerQueue queue) {
        IAtomicLong at = hzService.getAtomicLong(queue.getDialerQueueDetails().getQueueName());
        at.set(0);
    }

    public LocalTime getBestTimeToCallForAccount(long accountPk) throws CrmException {
        //Query q = entityManager.createQuery("SELECT callTime FROM (SELECT cast(s.callTimeStamp as time) callTime, count(s.callTimeStamp) maximum FROM SvCallDetailRecord s where s.accountPk = :accountPk GROUP BY cast(s.callTimeStamp as time) ORDER BY maximum DESC LIMIT 1) as c");
//        Query q = entityManager.createNativeQuery("SELECT callTime FROM (SELECT cast(s.start_time as time) callTime, count(s.start_time) maximum FROM svc.sv_call_detail_record s where s.account_pk = :accountPk GROUP BY cast(s.start_time as time) ORDER BY maximum DESC LIMIT 1) as c");
//        q.setParameter("accountPk", accountPk);
//        @SuppressWarnings("unchecked")
//        List<Object> list = q.getResultList();
//        return list.isEmpty() ? null : DateUtils.sqlTimetoLocalTime((Time) list.get(0));
        Account account = accountRepo.findAccountByPk(accountPk);
        LocalTime bestTime;
        if (account.getAccountData().getBestTimeToCall() != null) {
            bestTime = account.getAccountData().getBestTimeToCall();
        } else {
            BestTimeToCallPojo bttcPojo = tmsService.getBestTimeToCall(accountPk, 60.0);
            bestTime = bttcPojo == null ? null : bttcPojo.getBestHitRatioTime();
        }
        return bestTime;
    }

    public VoiceRecording createOrUpdateVoiceRecording(VoiceRecording voiceRecording) throws CrmException {
        if (voiceRecording == null) {
            throw new CrmException("Please provide voice recording details.");
        }
        if (StringUtils.isBlank(voiceRecording.getFileName())) {
            throw new CrmException("Please provide the voice recording file name.");
        }
        if (StringUtils.isBlank(voiceRecording.getFilePath())) {
            throw new CrmException("Please provide the voice recording file path.");
        }
        VoiceRecording existingVR = dqRepo.getVoiceRecordingByName(voiceRecording.getFileName());
        if (existingVR != null) {
            if (existingVR.equals(voiceRecording)) {
                return existingVR;
            } else {
                existingVR.setDescription(voiceRecording.getDescription());
                existingVR.setFilePath(voiceRecording.getFilePath());
                voiceRecording = existingVR;
            }
        }
        voiceRecording = entityManager.merge(voiceRecording);
        return voiceRecording;
    }

    public List<VoiceRecording> getAllVoiceRecordings() {
        return dqRepo.getAllVoiceRecordings();
    }

    public HoldMusic createOrUpdateHoldMusic(HoldMusic holdMusic) throws CrmException {
        if (holdMusic == null) {
            throw new CrmException("Please provide hold music details.");
        }
        if (StringUtils.isBlank(holdMusic.getFileName())) {
            throw new CrmException("Please provide the hold music file name.");
        }
        if (StringUtils.isBlank(holdMusic.getFilePath())) {
            throw new CrmException("Please provide the hold music file path.");
        }
        HoldMusic existingVR = dqRepo.getHoldMusicByName(holdMusic.getFileName());
        if (existingVR != null) {
            if (existingVR.equals(holdMusic)) {
                return existingVR;
            } else {
                existingVR.setDescription(holdMusic.getDescription());
                existingVR.setFilePath(holdMusic.getFilePath());
                holdMusic = existingVR;
            }
        }
        holdMusic = entityManager.merge(holdMusic);
        return holdMusic;
    }

    public List<HoldMusic> getAllHoldMusic() {
        return dqRepo.getAllHoldMusic();
    }

    public StiCallerId createOrUpdateCallerId(StiCallerId callerId) throws CrmException {
        if (callerId == null) {
            throw new CrmException("Please provide caller ID details.");
        }
        if (callerId.getCallerIdNumber() == null || callerId.getCallerIdNumber() <= 0) {
            throw new CrmException("Please provide the caller ID number.");
        }
        StiCallerId existingCI = dqRepo.getCallerIdByNumber(callerId.getCallerIdNumber());
        if (existingCI != null) {
            if (existingCI.equals(callerId)) {
                return existingCI;
            } else {
                existingCI.setCallerIdName(callerId.getCallerIdName());
                existingCI.setDescription(callerId.getDescription());
                callerId = existingCI;
            }
        }
        callerId = entityManager.merge(callerId);
        return callerId;
    }

    public List<StiCallerId> getAllCallerIds() {
        return dqRepo.getAllCallerIds();
    }

    public DialerQueueDetails createDefaultInboundQueue() throws CrmException {
        DialerQueue queue = dqRepo.locateDialerQueueByNameAndType(DEFAULT_INBOUND_QUEUE, DialerQueueType.INBOUND);
        if (queue != null) {
            return queue.getDialerQueueDetails();
        }
        DialerQueueDetails dq = createDialerQueue(DEFAULT_INBOUND_QUEUE, null, DialerQueueType.INBOUND);
        DialerGroup dialerGroup = new DialerGroup();
        dialerGroup.setGroupName(DEFAULT_DIALER_GROUP);
        dialerGroup.setIsActive(Boolean.TRUE);
        dialerGroup = dialerGroupService.createOrUpdateDialerGroup(dialerGroup);
        setDialerQueueGroupAssociation(dq.getPk(), dialerGroup.getPk(), null);
        InboundDialerQueueSettings dqSettings = getInboundDQSettingsByDQPk(dq.getPk());
        List<AgentCallOrder> agentCallOrder = new ArrayList<>();
        agentCallOrder.add(new AgentCallOrder(IncomingCallAgent.QUEUE_GROUP_AGENTS));
        agentCallOrder.add(new AgentCallOrder(IncomingCallAgent.PRIMARY_AGENT));
        agentCallOrder.add(new AgentCallOrder(IncomingCallAgent.SECONDARY_AGENTS));
        dqSettings.setAgentCallOrder(agentCallOrder);
        createOrUpdateDQSettings(dqSettings);
        return dq;
    }

    public DialerQueueDetails getDefaultInboundQueue() throws CrmException {
        DialerQueue queue = dqRepo.locateDialerQueueByNameAndType(DEFAULT_INBOUND_QUEUE, DialerQueueType.INBOUND);
        if (queue == null) {
            return createDefaultInboundQueue();
        }
        return queue.getDialerQueueDetails();
    }

    public InboundDialerQueueRecord getDefaultInboundQueueRecord() throws CrmException {
        DialerQueue queue = getDialerQueueByNameAndType(DEFAULT_INBOUND_QUEUE, DialerQueueType.INBOUND);
        DialerQueueDetails dqDetails;
        if (queue == null) {
            dqDetails = createDefaultInboundQueue();
        } else {
            dqDetails = queue.getDialerQueueDetails();
        }
        return getInboundDialerQueueRecord(dqDetails.getPk());
    }

    public List<StiCallerId> loadCallerIdNumbers() {
        List<StiCallerId> svCallerIds = new ArrayList<>();
        try {
            String callerIdNumbers = IOUtils.toString(ResourceUtils.getURL("classpath:com/objectbrains/svc/dialer/CallerIDNumbers.properties"));
            List<String> callerIdList = Arrays.asList(callerIdNumbers.split("[\\s*,;]+"));
            for (String callerId : callerIdList) {
                Long callerIdNumber = Long.valueOf(callerId);
                try {
                    if (StringUtils.isNotBlank(callerId)) {
                        StiCallerId svCallerId = new StiCallerId();
                        svCallerId.setCallerIdNumber(callerIdNumber);
                        svCallerId = createOrUpdateCallerId(svCallerId);
                        svCallerIds.add(svCallerId);
                    }
                } catch (CrmException | NumberFormatException ex) {
                    LOG.error("Error adding caller ID [{}]: {}", callerId, ex);
                }
            }
        } catch (IOException ex) {
            LOG.error("Error loading all caller IDs: {}", ex);
        }
        return svCallerIds;
    }

    public void setCallDispositionGroupForDialerQueue(long queuePk, long dispositionGroupPk) throws CrmException {
        DialerQueue queue = dqRepo.locateDialerQueueByPk(queuePk);
        CallDispositionGroup group = callDispositionService.getCallDispositionGroup(dispositionGroupPk);
        DialerQueueSettings dqSettings = getDialerQueueSettingsByDQPk(queuePk);
        CallDispositionGroup oldGroup = queue.getCallDispositionGroup();
        Boolean changeHistory = Boolean.TRUE;
        String log = "\n" + LocalDateTime.now() + " CallDispositionGroup : [oldValue : ";
        if (oldGroup != null) {
            queue.disassociateFromDispositionGroup();
            if (oldGroup.getName().equalsIgnoreCase(group.getName())) {
                changeHistory = false;
            }
            log = log + oldGroup.getName();
        }
        log = log + "; newValue : " + group.getName() + "]";
        if (changeHistory) {
            dqSettings.setChangeHistory(dqSettings.getChangeHistory() + log);
        }
        queue.associateToDispositionGroup(group);
    }

    public List<CallDispositionCode> getCallDispositionCodesForAccount(long accountPk, boolean isIncoming) throws CrmException {
        DialerQueue dq = getDialerQueueForAccount(accountPk, isIncoming);
        List<CallDispositionCode> codes = getCallDispositionCodesForQueue(dq);
        if (codes.isEmpty()) {
            if (isIncoming) {
                codes = getCallDispositionCodesForQueue(getDefaultInboundQueue().getPk());
                return codes.isEmpty() ? callDispositionService.getAllDefaultInboundDispositionCodes() : codes;
            } else {
                return callDispositionService.getAllDefaultOutboundDispositionCodes();
            }
        }
        return codes;
    }

    @SuppressWarnings("unchecked")
    public List<CallDispositionCode> getCallDispositionCodesForQueue(DialerQueue dq) {
        if (dq != null && dq.getCallDispositionGroup() != null) {
            return callDispositionService.getAllCallDispositionsForGroup(dq.getCallDispositionGroup().getPk());
        }
        return Collections.EMPTY_LIST;
    }

    public List<CallDispositionCode> getCallDispositionCodesForQueue(long dqPk) throws CrmException {
        DialerQueue dq = dqRepo.locateDialerQueueByPk(dqPk);
        return getCallDispositionCodesForQueue(dq);
    }

    public CallDispositionGroup getCallDispositionGroupForQueue(long dqPk) throws CrmException {
        DialerQueue queue = dqRepo.locateDialerQueueByPk(dqPk);
        return getCallDispositionGroupForQueue(queue);
    }

    private CallDispositionGroup getCallDispositionGroupForQueue(DialerQueue queue) {
        CallDispositionGroup group = queue.getCallDispositionGroup();
        if (group == null) {
            if (queue.isOutbound()) {
                group = callDispositionService.getDefaultOutboundCallDispositionGroup();
            } else {
                group = callDispositionService.getDefaultInboundCallDispositionGroup();
            }
        }
        if (group instanceof HibernateProxy) {
            Hibernate.initialize(group);
            group = (CallDispositionGroup) ((HibernateProxy) group).getHibernateLazyInitializer().getImplementation();
        }
        return group;
    }

    public long getDialerQueuePkForPhoneNumber(String phoneNumber) {
        List<DialerQueue> dialerQueues = dqRepo.getAllDialerQueues();
        for (DialerQueue dq : dialerQueues) {
            if (dq.getDialerQueueDetails().getDialerQueueSourceType() != null) {
                if (dq.getDialerQueueDetails().getDialerQueueSourceType() == DialerQueueSourceType.DESTINATION_NUMBER) {
                    if (dq.getDialerQueueDetails().getDestinationNumbers().contains(phoneNumber)) {
                        return dq.getPk();
                    }
                }
            }
        }
        return -1;
    }

    private void checkForDncAndVerbalCandD(Phone svPhone, long phoneNumber, boolean verbalCD, Account account) throws CrmException {
        if (svPhone.getPhoneData().getDoNotCall() != null && svPhone.getPhoneData().getDoNotCall()) {
            LOG.info("PhoneNumber {} has dnc set", phoneNumber);
            String note = "PhoneNumber " + phoneNumber + " has dnc set";
            workLogRepo.createWorkLog(account, note, WorkLogTypes.WORK_LOG_DIALER, null, false);
            LOG.info("Throwing an exception because dnc is set");
            throw new CrmException("Phone " + phoneNumber + " has do not call as true");
        }
        if (verbalCD && ((svPhone.getPhoneData().getPhoneNumberType() == PhoneNumberType.MOBILE_PHONE) || account.getAccountData().getCallbackDateTime() != null)) {
            LOG.info("PhoneNumber {} is mobile and has verbalc&d set", phoneNumber);
            String note = "PhoneNumber " + phoneNumber + " is mobile and has verbalc&d set";
            workLogRepo.createWorkLog(account, note, WorkLogTypes.WORK_LOG_DIALER, null, false);
            LOG.info("Throwing an exception because verbalc&d is set and mobile phone");
            throw new CrmException("Phone " + phoneNumber + " has is mobile and has verbalc&d set as true");
        }
    }

    public OutboundDialerRecord saveOutboundDialerQueueRecord(long dqPk) throws CrmException {
        LOG.info("Saving outbound dialer record to db for dialerQueue: " + dqPk);
        DialerQueue queue = dqRepo.locateDialerQueueByPk(dqPk);
        DialerQueueGroupAssociation assoc = dqRepo.getQueueGroupAssociationByDialerQueue(queue);
        OutboundDialerRecord svRecord = new OutboundDialerRecord(dqPk);
        if (assoc != null) {
            svRecord.setAgentWeightPriority(getAgentWeightPriorityForDialerGroups(assoc.getDialerGroup(), assoc.getSecondaryGroup()));
        }
        dqRepo.createOutboundRecord(svRecord);
        //returns all the accounts assigned to queue with valid phone numbers
        //dialerAccountPhoneData.getDialerQueueAccountDetailsForAccounts(getDialerQueueAccounts(queue), svRecord.getPk());
        return svRecord;
    }

    //manual sync
    public List<Long> executeDialerQueueSql(long dqPk) throws CrmException {
        return dqRepo.executeDialerQueueSql(dqPk);
    }

    public Integer getTotalCountForDialerQueue(long queuePk) {
        return dialerAccountPhoneData.getCallableAccountsForDialerQueue(queuePk).size();
    }

    public List<DialerQueueGroup> getDialerQueueGroups(final DialerQueueType dialerQueueType) throws IOException, CrmException {
        //needs to be un-hard coded
        //String url = "http://appx.objectbrains.com:7070/tms/rest/tms-commands/dialer-outbound-control/get-all-dialer-queue-status/";

        //Cache for every 20sec to prevent rapid rest call
        final List<QueueRunningStatus> statuses = new ArrayList<>();
        if (queueRunningStatusListCache.getCreateTime() + 20_000 > System.currentTimeMillis()) {
            statuses.addAll(queueRunningStatusListCache.getQueueRunningStatusList());
        } else {
            //todo: I need advice to make OR use an existing configuration structure to replace hard codded addressing
            String url = "http://127.0.0.1:7070/tms/rest/tms-commands/dialer-outbound-control/get-all-dialer-queue-status/";
            LOG.info("Call queueRunningStatusListCache on:" + System.currentTimeMillis());
            statuses.addAll(HttpClient.sendGetRequestAsJSONList(url, QueueRunningStatus.class));
            queueRunningStatusListCache = new QueueRunningStatusListCache(statuses);
        }

        return new ArrayList<DialerQueueGroup>() {
            {
                for (DialerQueueDetails dialerQueueDetails : getAllDialerQueuesByType(dialerQueueType)) {
                    DialerGroup dialerGroup = getPrimaryDialerGroupForDQ(dialerQueueDetails.getPk());
                    dialerQueueDetails.setGroupName(dialerGroup != null ? dialerGroup.getGroupName() : null);
                    DialerQueueGroup newQueueGroup = new DialerQueueGroup();
                    newQueueGroup.setDialerQueueDetails(dialerQueueDetails);

                    QueueRunningStatus queueRunningStatus = getQueueRunningStatusFromCollection(dialerQueueDetails.getPk(), statuses);

                    if (dialerQueueType == OUTBOUND && queueRunningStatus != null) {
                        newQueueGroup.setQueueRunningStatus(queueRunningStatus);
                    }

                    add(newQueueGroup);
                }
            }
        };
    }

    private QueueRunningStatus getQueueRunningStatusFromCollection(long queueStatusId, List<QueueRunningStatus> statuses) {

        for (QueueRunningStatus queueStatus : statuses) {
            if (queueStatusId == queueStatus.getQueueRunningStatusId()) {
                return queueStatus;
            }
        }
        return null;
    }

    //to keep the cached object and expire time
    private static class QueueRunningStatusListCache {

        private final long createTime = System.currentTimeMillis();
        private final List<QueueRunningStatus> queueRunningStatusList;

        QueueRunningStatusListCache(List<QueueRunningStatus> queueRunningStatusList) {
            this.queueRunningStatusList = queueRunningStatusList;
        }

        public long getCreateTime() {
            return createTime;
        }

        List<QueueRunningStatus> getQueueRunningStatusList() {
            return queueRunningStatusList;
        }
    }

    public List<AccountCustomerName> getBasicLoanDataForQueue(long queuePk, Integer pageNum, Integer pageSize) {
        List<AccountCustomerName> list = new ArrayList<>();

        return list;
    }
    
  

}
