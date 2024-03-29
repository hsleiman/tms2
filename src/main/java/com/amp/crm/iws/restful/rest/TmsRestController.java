/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.crm.iws.restful.rest;

import com.amp.crm.aop.Authorization;
import com.amp.crm.constants.DialerQueueType;
import com.amp.crm.constants.Permission;
import com.amp.crm.db.entity.agent.Agent;
import com.amp.crm.db.entity.agent.AgentDialerGroup;
import com.amp.crm.db.entity.agent.DialerGroup;
import com.amp.crm.db.entity.base.dialer.BIMessage;
import com.amp.crm.db.entity.base.dialer.DialerQueueSettings;
import com.amp.crm.db.entity.base.dialer.InboundDialerQueueSettings;
import com.amp.crm.db.entity.base.dialer.OutboundDialerQueueSettings;
import com.amp.crm.db.entity.base.dialer.CrmCallerId;
import com.amp.crm.db.entity.disposition.CallDispositionCode;
import com.amp.crm.db.entity.disposition.CallDispositionGroup;
import com.amp.crm.db.entity.qualityAssurance.QualityAssuranceForm;
import com.amp.crm.db.entity.qualityAssurance.QualityAssuranceFormQuestionRelation;
import com.amp.crm.db.entity.qualityAssurance.QualityAssuranceQuestion;
import com.amp.crm.embeddable.AgentWeightPriority;
import com.amp.crm.embeddable.BIPlaybackData;
import com.amp.crm.embeddable.DialerQueueDetails;
import com.amp.crm.embeddable.InboundDialerQueueRecord;
import com.amp.crm.embeddable.OutboundDialerQueueRecord;
import com.amp.crm.exception.CrmException;
import com.amp.crm.pojo.AccountCustomerName;
import com.amp.crm.pojo.CallDetailRecordResult;
import com.amp.crm.pojo.CallHistoryCriteria;
import com.amp.crm.pojo.DQCallDispositionGroupAssociation;
import com.amp.crm.pojo.DialerQueueGroup;
import com.amp.crm.pojo.DialerQueueRecord;
import com.amp.crm.pojo.PhoneNumberAccountData;
import com.amp.crm.pojo.QueueAgentWeightPriority;
import com.amp.crm.pojo.TMSBasicAccountInfo;
import com.amp.crm.pojo.TMSCallDetails;
import com.amp.crm.service.dialer.DialerQueueService;
import com.amp.crm.service.dialer.OutboundDialerService;
import com.amp.crm.service.dialer.PhoneNumberCallable;
import com.amp.crm.service.tms.BIMessageService;
import com.amp.crm.service.tms.CallDispositionService;
import com.amp.crm.service.tms.CallQualityManagementService;
import com.amp.crm.service.tms.DialerGroupService;
import com.amp.crm.service.tms.TMSService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.springframework.web.bind.annotation.RequestMethod.*;

/**
 * 
 */
@RestController()
@RequestMapping(value = "/tmsRestController", produces = MediaType.APPLICATION_JSON_VALUE)
public class TmsRestController {

    private static final Logger LOG = LoggerFactory.getLogger(TmsRestController.class);

    @Autowired
    private DialerGroupService dialerGroupService;
    @Autowired
    private CallDispositionService callDispositionService;
    @Autowired
    private DialerQueueService dialerQueueService;
    @Autowired
    private TMSService tmsService;
    @Autowired
    private CallQualityManagementService callQualityManagementService;
    @Autowired
    private BIMessageService bIMessageService;
    @Autowired
    private OutboundDialerService outboundDialerService;

    
    @RequestMapping(value = "/getAccountsForPhoneNumber{phoneNumber}", method = GET)
    @Authorization(permission = Permission.Authenticated, noPermissionTo = "Not Authenticated.")
    public List<PhoneNumberAccountData> getAllDialerGroups(@PathVariable long phoneNumber) throws IOException {
        return tmsService.getAccountsForPhoneNumber(phoneNumber);
    }
    
    @RequestMapping(value = "/getDialerQueuePkForPhoneNumber{phoneNumber}", method = GET)
    @Authorization(permission = Permission.Authenticated, noPermissionTo = "Not Authenticated.")
    public Long getAllDialerGroups(@PathVariable String phoneNumber) throws IOException {
        return dialerQueueService.getDialerQueuePkForPhoneNumber(phoneNumber);
    }

    @RequestMapping(value = "/getBasicAccountInfoForTMS{accountPk}", method = GET)
    @Authorization(permission = Permission.Authenticated, noPermissionTo = "Not Authenticated.")
    public TMSBasicAccountInfo getBasicAccountInfoForTMS(@PathVariable long accountPk) throws IOException {
        return tmsService.getBasicAccountInfoForTMS(accountPk);
    }
    
//    @RequestMapping(value = "/getAgentWeightPriorityListForDq{dialerQueuePk}", method = GET)
//    @Authorization(permission = Permission.Authenticated, noPermissionTo = "Not Authenticated.")
//    public List<AgentWeightPriority> getAgentWeightPriorityListForDq (@PathVariable long dialerQueuePk) throws IOException, CrmException {
//        return dialerQueueService.getAgentWeightPriorityListForDq(dialerQueuePk);
//    }
    
    @RequestMapping(value = "/getAllDialerGroups", method = GET)
    @Authorization(permission = Permission.Authenticated, noPermissionTo = "Not Authenticated.")
    public List<DialerGroup> getAllDialerGroups() throws IOException {
        return dialerGroupService.getAllDialerGroups();
    }

    @RequestMapping(value = "/getDialerGroup/{dialerGroupPk}", method = GET)
    @Authorization(permission = Permission.Authenticated, noPermissionTo = "Not Authenticated.")
    public DialerGroup getDialerGroupByPk(@PathVariable long dialerGroupPk) throws IOException {
        return dialerGroupService.getDialerGroupByPk(dialerGroupPk);
    }

    @RequestMapping(value = "/getAllAgentsInDialerGroup/{dialerGroupPk}", method = GET)
    @Authorization(permission = Permission.Authenticated, noPermissionTo = "Not Authenticated.")
    public List<AgentWeightPriority> getAllAgentsInDialerGroup(@PathVariable long dialerGroupPk) throws IOException, CrmException {
        return dialerQueueService.getAgentWeightPriorityListForGroup(dialerGroupPk);
    }

    @RequestMapping(value = "/createOrUpdateDialerGroup", method = POST)
    @Authorization(permission = Permission.Authenticated, noPermissionTo = "Not Authenticated.")
    public DialerGroup createOrUpdateDialerGroup(@RequestBody DialerGroup dialerGroup) throws IOException, CrmException {
//        DialerGroup dialerGroup = JSONUtils.JSONToObject(dialerGroupString, DialerGroup.class);
        return dialerGroupService.createOrUpdateDialerGroup(dialerGroup);
    }
    
    @RequestMapping(value = "/updateAgentsToDialerGroup/{dialerGroupPk}", method = POST)
    @Authorization(permission = Permission.Authenticated, noPermissionTo = "Not Authenticated.")
    public boolean updateAgentsToDialerGroup(@PathVariable long dialerGroupPk, @RequestBody ArrayList<AgentWeightPriority> listOfAgents) throws IOException, CrmException {
        //Long dialerGroupLong = Long.valueOf(dialerGroupPk);
        dialerGroupService.deleteAgentsFromDialerGroup(dialerGroupPk);
        for (AgentWeightPriority agent : listOfAgents) {
            dialerGroupService.setAgentToDialerGroup(agent.getUsername(), dialerGroupPk, agent.getWeightedPriority(), agent.isLeader(), agent.getAllowAfterHours());
        }
        return true;
    }

    @RequestMapping(value = "/assignAgentsToDialerGroup/{dialerGroupPk}", method = POST)
    @Authorization(permission = Permission.Authenticated, noPermissionTo = "Not Authenticated.")
    public boolean assignAgentsToDialerGroup(@PathVariable long dialerGroupPk, @RequestBody String listOfAgents) throws IOException, CrmException {
        //Long dialerGroupLong = Long.valueOf(dialerGroupPk);
        String[] agents = listOfAgents.split(",");
        for (String agentUsername : agents) {
            dialerGroupService.setAgentToDialerGroup(agentUsername, dialerGroupPk, null, false, false);
        }
        return true;
    }

    @RequestMapping(value = "/deleteAgentFromDialerGroup/{dialerGroupPk}/{username}", method = DELETE)
    @Authorization(permission = Permission.Authenticated, noPermissionTo = "Not Authenticated.")
    public boolean deleteAgentFromDialerGroup(@PathVariable Long dialerGroupPk, @PathVariable String username) throws IOException, CrmException {
        dialerGroupService.deleteAgentFromDialerGroup(username, dialerGroupPk);
        return true;
    }

    @RequestMapping(value = "/addOrUpdateCallDisposition", method = POST)
    @Authorization(permission = Permission.Authenticated, noPermissionTo = "Not Authenticated.")
    public CallDispositionCode addOrUpdateCallDisposition(@RequestBody CallDispositionCode callDispositionCode) throws IOException, CrmException {
        return callDispositionService.addOrUpdateCallDisposition(callDispositionCode);
    }

    @RequestMapping(value = "/getAllCallDispositionCodes", method = GET)
    @Authorization(permission = Permission.Authenticated, noPermissionTo = "Not Authenticated.")
    public List<CallDispositionCode> getAllCallDispositionCodes() throws IOException, CrmException {
        return callDispositionService.getAllCallDispositionCodes();
    }

    @RequestMapping(value = "/createOrUpdateDispositionGroup", method = POST)
    @Authorization(permission = Permission.Authenticated, noPermissionTo = "Not Authenticated.")
    public CallDispositionGroup addOrUpdateCallDisposition(@RequestBody CallDispositionGroup callDispositionGroup) throws IOException, CrmException {
        return callDispositionService.createOrUpdateDispositionGroup(callDispositionGroup);
    }

    @RequestMapping(value = "/getCallDispositionGroup/{dispositionGroupPk}", method = GET)
    @Authorization(permission = Permission.Authenticated, noPermissionTo = "Not Authenticated.")
    public CallDispositionGroup getCallDispositionGroup(@PathVariable long dispositionGroupPk) throws IOException, CrmException {
        return callDispositionService.getCallDispositionGroup(dispositionGroupPk);
    }

    @RequestMapping(value = "/getAllCallDispositionGroups", method = GET)
    @Authorization(permission = Permission.Authenticated, noPermissionTo = "Not Authenticated.")
    public List<CallDispositionGroup> getAllCallDispositionGroups() throws IOException, CrmException {
        return callDispositionService.getAllCallDispositionGroups();
    }

    @RequestMapping(value = "/setCallDispositionsToGroup/{callDispositionGroupPk}", method = POST)
    @Authorization(permission = Permission.Authenticated, noPermissionTo = "Not Authenticated.")
    public boolean addCallDispositionCodesToGroup(@PathVariable long callDispositionGroupPk, @RequestBody Set<Long> callDispositionIds) throws IOException, CrmException {
        callDispositionService.setCallDispositionsToGroup(callDispositionGroupPk, callDispositionIds);
        return true;
    }

    /**
     * JSON Sample:
     * {"dialerQueueType":"INBOUND",
     * "queueName":"jay",
     * "dialerQueueSourceType":"SQL",
     * "criteriaSetPks":[],
     * "tableGroupPk":null,
     * "secondaryGroupPk":1,
     * "destinationNumbers":"null",
     * "queryPk":1,
     * "createdBy":"null",
     * "workQueuePk":1,
     * "lastAccountAssignmentTimestamp":null,
     * "active":true,
     * "accountCount":0,
     * "sqlQuery":"select * from sti.sti_agent ll where ll.pk = 1"}
     */
    @RequestMapping(value = "/createDialerQueue", method = POST)
    @Authorization(permission = Permission.Authenticated, noPermissionTo = "Not Authenticated.")
    public DialerQueueDetails createDialerQueue(@RequestBody DialerQueueDetails dialerQueueDetails) throws CrmException {
        return dialerQueueService.createDialerQueue(dialerQueueDetails.getQueueName(), dialerQueueDetails.getSqlQuery(), dialerQueueDetails.getDialerQueueType());
    }

    /**
     * JSON Sample:
     * {"dialerQueueType":"INBOUND",
     * "queueName":"jay",
     * "dialerQueueSourceType":"SQL",
     * "criteriaSetPks":[],
     * "tableGroupPk":null,
     * "secondaryGroupPk":1,
     * "destinationNumbers":"null",
     * "queryPk":1,
     * "createdBy":"null",
     * "workQueuePk":1,
     * "lastAccountAssignmentTimestamp":null,
     * "active":true,
     * "accountCount":0,
     * "sqlQuery":"select * from sti.sti_agent ll where ll.pk = 1"}
     */
    @RequestMapping(value = "/updateDialerQueue", method = POST)
    @Authorization(permission = Permission.Authenticated, noPermissionTo = "Not Authenticated.")
    public DialerQueueDetails updateDialerQueue(@RequestBody DialerQueueDetails dialerQueueDetails) throws CrmException {
        return dialerQueueService.updateDialerQueue(dialerQueueDetails);
    }

    
    @RequestMapping(value = "/setCallDispositionGroupForDialerQueue", method = POST)
    @Authorization(permission = Permission.Authenticated, noPermissionTo = "Not Authenticated.")
    public void setCallDispositionGroupForDialerQueue(@RequestBody DQCallDispositionGroupAssociation data) throws CrmException{
        dialerQueueService.setCallDispositionGroupForDialerQueue(data.getDqpk(), data.getCallDispositionGroupPk());
    }
    
    
    /**
     * @param dqPK
     * @return json to the front-end
     * @throws CrmException
     */
    @RequestMapping(value = "/deleteDialerQueue/{dqPK}", method = DELETE)
    @Authorization(permission = Permission.Authenticated, noPermissionTo = "no permission")
    public void deleteDialerQueue(@PathVariable("dqPK") long dqPK) throws CrmException {
        dialerQueueService.deleteDialerQueue(dqPK);
    }

    @RequestMapping(value = "/getAllCallDispositionsForGroup/{callDispositionGroupPk}", method = GET)
    @Authorization(permission = Permission.Authenticated, noPermissionTo = "Not Authenticated.")
    public List<CallDispositionCode> getAllCallDispositionsForGroup(@PathVariable("callDispositionGroupPk") long callDispositionGroupPk) {
        return callDispositionService.getAllCallDispositionsForGroup(callDispositionGroupPk);
    }

    @RequestMapping(value = "/getAllDialerQueues/{queueIOType}", method = GET)
    @Authorization(permission = Permission.Authenticated, noPermissionTo = "Not Authenticated.")
    public List<DialerQueueGroup> getAllDialerQueues(@PathVariable("queueIOType") DialerQueueType dialerQueueType) throws IOException, CrmException {
        return dialerQueueService.getDialerQueueGroups(dialerQueueType);
    }

    /*
     * JASON Sample:
      {"dialerQueuePk":75,"dialerQueue":null,"creationTime":null,"popupDisplayMode":null,"autoAnswerEnabled":false,"weightedPriority":{"priority":null,"weight":null},"idleMaxMinutes":null,"wrapMaxMinutes":null,"startTime":null,"endTime":null,"changeHistory":"","dialerSchedule":[],"maxDelayBeforeAgentAnswer":null,"agentCallOrder":[],"callRoutingOption":null,"roundRobinCutoffPercent":null,"disableSecondaryAgentsCallRouting":null,"forceVoicemail":null}
     */
    @RequestMapping(value = "/createOrUpdateInboundDQSettings", method = POST)
    @Authorization(permission = Permission.Authenticated, noPermissionTo = "Not Authenticated.")
    public DialerQueueSettings createOrUpdateInboundDQSettings(@RequestBody InboundDialerQueueSettings inboundDialerQueueSettings) throws CrmException {
        return dialerQueueService.createOrUpdateDQSettings(inboundDialerQueueSettings);
    }

    /*
     * JSON Sample:
    {"dialerQueuePk":82,"dialerQueue":null,"creationTime":null,"popupDisplayMode":null,"autoAnswerEnabled":false,"weightedPriority":{"priority":null,"weight":null},"idleMaxMinutes":null,"wrapMaxMinutes":null,"startTime":null,"endTime":null,"changeHistory":"","dialerSchedule":[],"dialerMode":null,"previewDialerType":null,"callerId":null,"callerIdNumber":null,"voiceMailName":null,"holdMusicName":null,"bestTimeToCall":null,"maxDelayCallTime":null,"dispositionPlanName":null,"ignoreRecallTimesForSwitchedNumbers":null,"maxWaitForResult":null,"predictiveMaxCallDropPercent":null,"progressiveCallsPerAgent":null,"answeringMachineDetection":null,"interactiveVoiceResponse":null,"voiceMailOption":null,"spillOverActive":null,"scheduledRunTime":null,"oneTimeUse":null,"phoneTypesAllowed":null,"dialOrder":"1,0,3,2","orderByList":"1","leaveVoiceMailAt":null}
     */
    @RequestMapping(value = "/createOrUpdateOutboundDQSettings", method = POST)
    @Authorization(permission = Permission.Authenticated, noPermissionTo = "Not Authenticated.")
    public DialerQueueSettings createOrUpdateOutboundDQSettings(@RequestBody OutboundDialerQueueSettings outboundDialerQueueSettings) throws CrmException {
        return dialerQueueService.createOrUpdateDQSettings(outboundDialerQueueSettings);
    }

    @RequestMapping(value = "/getOutboundDQSettingsByDQPk/{queuePk}", method = GET)
    @Authorization(permission = Permission.Authenticated, noPermissionTo = "Not Authenticated.")
    public OutboundDialerQueueSettings getOutboundDQSettingsByDQPk(
            @PathVariable("queuePk") long queuePk) throws CrmException {
        return dialerQueueService.getOutboundDQSettingsByDQPk(queuePk);
    }

    @RequestMapping(value = "/getInboundDQSettingsByDQPk/{queuePk}", method = GET)
    @Authorization(permission = Permission.Authenticated, noPermissionTo = "Not Authenticated.")
    public InboundDialerQueueSettings getInboundDQSettingsByDQPk(
            @PathVariable("queuePk") long queuePk) throws CrmException {
        return dialerQueueService.getInboundDQSettingsByDQPk(queuePk);
    }

    /**
     * pageNumber: page number [0,1,2,...]
     * pageSize:   number of records per page and it should be grater than 0
     * fromDate:   start date should be in timestamp format and its numeric (no millisecond: 10 digits) fromDate <= toDate
     * toDate:     end date should be in timestamp format and its numeric (no millisecond: 10 digits)
     * callType:   its call_direction [null,0,1,2]
     * accountPk:  loan Id: [null,1,2,3,4,...]
     * <p>
     * JASON Sample:
     * {
     * "fromDate" : 1512086400,
     * "toDate" : 1514678400,
     * "callerPhoneNumber" : "",
     * "calleePhoneNumber" : "",
     * "callType" : null,
     * "accountPk" : null,
     * "dialerCall" : null,
     * "userDisposition" : null,
     * "pageNumber" : 0,
     * "pageSize" : 100
     * }
     */
    @RequestMapping(value = "/getAllCallHistoryWithCriteria", method = POST)
    @Authorization(permission = Permission.Authenticated, noPermissionTo = "Not Authenticated.")
    public CallDetailRecordResult getAllCallHistoryWithCriteria(@RequestBody CallHistoryCriteria callHistoryCriteria) throws RuntimeException {
        //return tmsService.getAllCallHistory(callHistoryCriteria);
        return tmsService.getCallLogLegs(callHistoryCriteria);
    }

    /**
     * @return List<QualityAssuranceForm> List of QualityAssuranceForm
     */
    @RequestMapping(value = "/getQualityAssuranceForms", method = GET)
    @Authorization(permission = Permission.Authenticated, noPermissionTo = "Not Authenticated.")
    public List<QualityAssuranceForm> getQualityAssuranceForms() {
        return callQualityManagementService.getQualityAssuranceForms();
    }

    /**
     * JASON Sample: INSERT
     * {
     * "formName":"My First Form 2",
     * "qualityAssuranceCategory":{
     * "qualityAssuranceCategoryPk" : 1
     * },
     * "title":"Good Title"
     * }
     * <p>
     * <p>
     * JASON Sample: UPDATE
     * {
     * "qualityAssuranceFormPk":2,
     * "formName":"My First Form 1",
     * "qualityAssuranceCategory":{
     * "qualityAssuranceCategoryPk" : 1
     * },
     * "title":"Good Title"
     * }
     */
    @RequestMapping(value = "/createOrUpdateQualityAssuranceForm", method = POST)
    @Authorization(permission = Permission.Authenticated, noPermissionTo = "Not Authenticated.")
    public boolean createOrUpdateQualityAssuranceForm(@RequestBody QualityAssuranceForm qualityAssuranceForm) {
        return callQualityManagementService.createOrUpdateQualityAssuranceForm(qualityAssuranceForm);
    }

    /**
     * get all QualityAssuranceQuestion
     *
     * @return List<QualityAssuranceQuestion> List of QualityAssuranceQuestion
     */
    @RequestMapping(value = "/getQualityAssuranceQuestions", method = GET)
    @Authorization(permission = Permission.Authenticated, noPermissionTo = "Not Authenticated.")
    public List<QualityAssuranceQuestion> getQualityAssuranceQuestions() {
        return callQualityManagementService.getQualityAssuranceQuestions();
    }

    /**
     * JASON Sample: INSERT
     * {
     * "question":"My Question 1",
     * "qualityAssuranceQuestionType":"TEXT",
     * "category":"Good category"
     * }
     * <p>
     * <p>
     * JASON Sample: UPDATE (Disabled)
     * {
     * "qualityAssuranceQuestionPk":2,
     * "question":"My Question 1",
     * "qualityAssuranceQuestionType":"TEXT",
     * "category":"Good category"
     * }
     */
    @RequestMapping(value = "/createOrUpdateQualityAssuranceQuestion", method = POST)
    @Authorization(permission = Permission.Authenticated, noPermissionTo = "Not Authenticated.")
    public boolean createOrUpdateQualityAssuranceQuestion(@RequestBody QualityAssuranceQuestion qualityAssuranceQuestion) {
        return callQualityManagementService.createOrUpdateQualityAssuranceQuestion(qualityAssuranceQuestion);
    }

    @RequestMapping(value = "/getQualityAssuranceFormQuestionRelations/{qualityAssuranceFormPk}", method = GET)
    @Authorization(permission = Permission.Authenticated, noPermissionTo = "Not Authenticated.")
    public List<QualityAssuranceFormQuestionRelation> getQualityAssuranceFormQuestionRelations(@PathVariable("qualityAssuranceFormPk") long qualityAssuranceFormPk) {
        return callQualityManagementService.getQualityAssuranceFormQuestionRelations(qualityAssuranceFormPk);
    }

    @RequestMapping(value = "/saveQualityAssuranceFormQuestionRelations", method = POST)
    @Authorization(permission = Permission.Authenticated, noPermissionTo = "Not Authenticated.")
    public boolean saveQualityAssuranceFormQuestionRelations(@RequestBody List<QualityAssuranceFormQuestionRelation> qualityAssuranceFormQuestionRelationList) throws Exception {
        return callQualityManagementService.saveQualityAssuranceFormQuestionRelations(qualityAssuranceFormQuestionRelationList);
    }
    
    // *********** BI Message 
    
    @RequestMapping(value = "/saveBIMessage", method = POST)
    @Authorization(permission = Permission.Authenticated, noPermissionTo = "Not Authenticated.")
    public void saveBIMessage(BIMessage biMessage) {
        bIMessageService.saveBIMessage(biMessage);
    }
    
    @RequestMapping(value = "/getBIPlaybackData/{callUUID}", method = GET)
    @Authorization(permission = Permission.Authenticated, noPermissionTo = "Not Authenticated.")
    public BIPlaybackData getBIPlaybackData(@PathVariable("callUUID") String callUUID) {
        return bIMessageService.getBIPlaybackData(callUUID);
    }
    
    // *********** Dialer Queue
    
    @RequestMapping(value = "/getAllDialerQueues", method = GET)
    @Authorization(permission = Permission.Authenticated, noPermissionTo = "Not Authenticated.")
    public List<DialerQueueDetails> getAllDialerQueues() {
        return dialerQueueService.getAllDialerQueues();
    }
    
    @RequestMapping(value = "/getDialerQueueByPk/{queuePk}", method = GET)
    @Authorization(permission = Permission.Authenticated, noPermissionTo = "Not Authenticated.")
    public DialerQueueDetails getDialerQueueByPk(@PathVariable("queuePk") long queuePk) throws CrmException {
         return dialerQueueService.getDialerQueueByPk(queuePk);
    }
    
    @RequestMapping(value = "/getBasicLoanDataForQueue/{queuePk}", method = GET)
    @Authorization(permission = Permission.Authenticated, noPermissionTo = "Not Authenticated.")
    public List<AccountCustomerName> getBasicLoanDataForQueue(@PathVariable("queuePk") long queuePk) throws CrmException {
         return dialerQueueService.getBasicLoanDataForQueue(queuePk, null, null);
    }
    
    @RequestMapping(value = "/canCallNumber/{dqPk}/{accountPk}/{phoneNumber}", method = GET)
    @Authorization(permission = Permission.Authenticated, noPermissionTo = "Not Authenticated.")
    public PhoneNumberCallable canCallNumber(@PathVariable("dqPk") long dqPk,@PathVariable("accountPk") long accountPk,@PathVariable("phoneNumber") long phoneNumber) throws CrmException {
         return dialerQueueService.canCallNumberInQueue(dqPk,accountPk,phoneNumber);
    }
    
    @RequestMapping(value = "/getCallDispositionCodesForQueue/{dqPk}", method = GET)
    @Authorization(permission = Permission.Authenticated, noPermissionTo = "Not Authenticated.")
    public List<CallDispositionCode> getCallDispositionCodesForQueue(@PathVariable("dqPk") long dqPk) throws CrmException {
         return dialerQueueService.getCallDispositionCodesForQueue(dqPk);
    }
    
    // *********** DIALER QUEUE SETTINGS
    
    @RequestMapping(value = "/getOutboundDialerQueueRecord/{queuePk}", method = GET)
    @Authorization(permission = Permission.Authenticated, noPermissionTo = "Not Authenticated.")
    public OutboundDialerQueueRecord getOutboundDialerQueueRecord(@PathVariable("queuePk") long queuePk) throws CrmException {
        return outboundDialerService.getOutboundDialerQueueRecord(queuePk);
    }
    
    // ************ Queue Group Association
    
    @RequestMapping(value = "/getQueueAgentWeightPriorityForUsername/{username}", method = GET)
    @Authorization(permission = Permission.Authenticated, noPermissionTo = "Not Authenticated.")
    public List<QueueAgentWeightPriority> getQueueAgentWeightPriorityForUsername(@PathVariable("username") String username) {
        return dialerQueueService.getQueueAgentWeightPriorityForUsername(username);
    }
    
    @RequestMapping(value = "/getAgentWeightPriorityListForGroup/{dialerGroupPk}", method = GET)
    @Authorization(permission = Permission.Authenticated, noPermissionTo = "Not Authenticated.")
    public List<AgentWeightPriority> getAgentWeightPriorityListForGroup(@PathVariable("username") long dialerGroupPk) throws CrmException {
        return dialerQueueService.getAgentWeightPriorityListForGroup(dialerGroupPk);
    }
    
    @RequestMapping(value = "/getAgentWeightPriorityListForDq/{dialerQueuePk}", method = GET)
    @Authorization(permission = Permission.Authenticated, noPermissionTo = "Not Authenticated.")
    public List<AgentWeightPriority> getAgentWeightPriorityListForDq (@PathVariable("dialerQueuePk") long dialerQueuePk) throws IOException, CrmException {
        return dialerQueueService.getAgentWeightPriorityListForDq(dialerQueuePk);
    }
    
    // ************ Inbound Call Service
    
    
    
    @RequestMapping(value = "/getLoanInfoByPhoneNumber/{phoneNumber}", method = GET)
    @Authorization(permission = Permission.Authenticated, noPermissionTo = "Not Authenticated.")
    public TMSCallDetails getLoanInfoByPhoneNumber(@PathVariable("phoneNumber") long phoneNumber) throws CrmException {
        return tmsService.getLoanInfoByPhoneNumber(phoneNumber);
    }
    
    
    // ************ Dialer Queue Setting
    
    @RequestMapping(value = "/getInboundDialerQueueRecord/{queuePk}", method = GET)
    @Authorization(permission = Permission.Authenticated, noPermissionTo = "Not Authenticated.")
    public InboundDialerQueueRecord getInboundDialerQueueRecord(@PathVariable("queuePk") long queuePk) throws CrmException {
        return dialerQueueService.getInboundDialerQueueRecord(queuePk);
    }

    // ************ Call Disposition
    
    @RequestMapping(value = "/getCallDispositionCode/{dispositionCodeId}", method = GET)
    @Authorization(permission = Permission.Authenticated, noPermissionTo = "Not Authenticated.")
    public CallDispositionCode getCallDispositionCode(@PathVariable("dispositionCodeId") long dispositionCodeId) throws IOException, CrmException {
        return callDispositionService.getCallDispositionCode(dispositionCodeId);
    }
    
    @RequestMapping(value = "/getCallDispositionCodeByQCode/{qCode}", method = GET)
    @Authorization(permission = Permission.Authenticated, noPermissionTo = "Not Authenticated.")
    public CallDispositionCode getCallDispositionCodeByQCode(@PathVariable("qCode") int qCode) {
        return callDispositionService.getCallDispositionCodeByQCode(qCode);
    }
    
    @RequestMapping(value = "/getCallDispositionCodeByDispositionName/{dispositionName}", method = GET)
    @Authorization(permission = Permission.Authenticated, noPermissionTo = "Not Authenticated.")
    public CallDispositionCode getCallDispositionCodeByDispositionName(@PathVariable("dispositionName") String dispositionName) {
        return callDispositionService.getCallDispositionCodeByDispositionName(dispositionName);
    }
    
    @RequestMapping(value = "/getCallDispositionCodesForLoan/{accountPk}", method = GET)
    @Authorization(permission = Permission.Authenticated, noPermissionTo = "Not Authenticated.")
    public List<CallDispositionCode> getCallDispositionCodesForLoan(@PathVariable("accountPk") long accountPk) throws CrmException {
        return callDispositionService.getCallDispositionCodesForLoan(accountPk, true);
    }
    
    // ************ MISC
    
    @RequestMapping(value = "/getAllCallerIds", method = GET)
    @Authorization(permission = Permission.Authenticated, noPermissionTo = "Not Authenticated.")
    public List<CrmCallerId> getAllCallerIds() {
        return dialerQueueService.getAllCallerIds();
    }
    
}
