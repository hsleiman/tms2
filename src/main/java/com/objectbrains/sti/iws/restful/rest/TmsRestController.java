/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.objectbrains.sti.iws.restful.rest;

import com.objectbrains.sti.aop.Authorization;
import com.objectbrains.sti.constants.DialerQueueType;
import com.objectbrains.sti.constants.Permission;
import com.objectbrains.sti.db.entity.agent.Agent;
import com.objectbrains.sti.db.entity.agent.DialerGroup;
import com.objectbrains.sti.db.entity.base.dialer.DialerQueueSettings;
import com.objectbrains.sti.db.entity.base.dialer.InboundDialerQueueSettings;
import com.objectbrains.sti.db.entity.base.dialer.OutboundDialerQueueSettings;
import com.objectbrains.sti.db.entity.disposition.CallDispositionCode;
import com.objectbrains.sti.db.entity.disposition.CallDispositionGroup;
import com.objectbrains.sti.db.entity.qualityAssurance.QualityAssuranceForm;
import com.objectbrains.sti.db.entity.qualityAssurance.QualityAssuranceFormQuestionRelation;
import com.objectbrains.sti.db.entity.qualityAssurance.QualityAssuranceQuestion;
import com.objectbrains.sti.embeddable.DialerQueueDetails;
import com.objectbrains.sti.exception.StiException;
import com.objectbrains.sti.pojo.CallDetailRecordResult;
import com.objectbrains.sti.pojo.CallHistoryCriteria;
import com.objectbrains.sti.pojo.DialerQueueGroup;
import com.objectbrains.sti.service.dialer.DialerQueueService;
import com.objectbrains.sti.service.tms.CallDispositionService;
import com.objectbrains.sti.service.tms.CallQualityManagementService;
import com.objectbrains.sti.service.tms.DialerGroupService;
import com.objectbrains.sti.service.tms.TMSService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import static org.springframework.web.bind.annotation.RequestMethod.*;

/**
 * @author David
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
    public List<Agent> getAllAgentsInDialerGroup(@PathVariable long dialerGroupPk) throws IOException, StiException {
        return dialerGroupService.getAllAgentsInDialerGroup(dialerGroupPk);
    }

    @RequestMapping(value = "/createDialerGroup", method = POST)
    @Authorization(permission = Permission.Authenticated, noPermissionTo = "Not Authenticated.")
    public DialerGroup createDialerGroup(@RequestBody DialerGroup dialerGroup) throws IOException, StiException {
//        DialerGroup dialerGroup = JSONUtils.JSONToObject(dialerGroupString, DialerGroup.class);
        return dialerGroupService.createOrUpdateDialerGroup(dialerGroup);
    }

    @RequestMapping(value = "/assignAgentsToDialerGroup/{dialerGroupPk}", method = POST)
    @Authorization(permission = Permission.Authenticated, noPermissionTo = "Not Authenticated.")
    public boolean assignAgentsToDialerGroup(@PathVariable String dialerGroupPk, @RequestBody String listOfAgents) throws IOException, StiException {
        Long dialerGroupLong = Long.valueOf(dialerGroupPk);
        String[] agents = listOfAgents.split(",");
        for (String agentUsername : agents) {
            dialerGroupService.setAgentToDialerGroup(agentUsername, dialerGroupLong, null, false, false);
        }
        return true;
    }

    @RequestMapping(value = "/deleteAgentFromDialerGroup/{dialerGroupPk}/{username}", method = DELETE)
    @Authorization(permission = Permission.Authenticated, noPermissionTo = "Not Authenticated.")
    public boolean deleteAgentFromDialerGroup(@PathVariable Long dialerGroupPk, @PathVariable String username) throws IOException, StiException {
        dialerGroupService.deleteAgentFromDialerGroup(username, dialerGroupPk);
        return true;
    }

    @RequestMapping(value = "/addOrUpdateCallDisposition", method = POST)
    @Authorization(permission = Permission.Authenticated, noPermissionTo = "Not Authenticated.")
    public CallDispositionCode addOrUpdateCallDisposition(@RequestBody CallDispositionCode callDispositionCode) throws IOException, StiException {
        return callDispositionService.addOrUpdateCallDisposition(callDispositionCode);
    }

    @RequestMapping(value = "/getCallDispositionCode/{dispositionCodeId}", method = GET)
    @Authorization(permission = Permission.Authenticated, noPermissionTo = "Not Authenticated.")
    public CallDispositionCode getCallDispositionCode(@PathVariable long dispositionCodeId) throws IOException, StiException {
        return callDispositionService.getCallDispositionCode(dispositionCodeId);
    }

    @RequestMapping(value = "/getAllCallDispositionCodes", method = GET)
    @Authorization(permission = Permission.Authenticated, noPermissionTo = "Not Authenticated.")
    public List<CallDispositionCode> getAllCallDispositionCodes() throws IOException, StiException {
        return callDispositionService.getAllCallDispositionCodes();
    }

    @RequestMapping(value = "/createOrUpdateDispositionGroup", method = POST)
    @Authorization(permission = Permission.Authenticated, noPermissionTo = "Not Authenticated.")
    public CallDispositionGroup addOrUpdateCallDisposition(@RequestBody CallDispositionGroup callDispositionGroup) throws IOException, StiException {
        return callDispositionService.createOrUpdateDispositionGroup(callDispositionGroup);
    }

    @RequestMapping(value = "/getCallDispositionGroup/{dispositionGroupPk}", method = GET)
    @Authorization(permission = Permission.Authenticated, noPermissionTo = "Not Authenticated.")
    public CallDispositionGroup getCallDispositionGroup(@PathVariable long dispositionGroupPk) throws IOException, StiException {
        return callDispositionService.getCallDispositionGroup(dispositionGroupPk);
    }

    @RequestMapping(value = "/getAllCallDispositionGroups", method = GET)
    @Authorization(permission = Permission.Authenticated, noPermissionTo = "Not Authenticated.")
    public List<CallDispositionGroup> getAllCallDispositionGroups() throws IOException, StiException {
        return callDispositionService.getAllCallDispositionGroups();
    }

    @RequestMapping(value = "/setCallDispositionsToGroup/{callDispositionGroupPk}", method = POST)
    @Authorization(permission = Permission.Authenticated, noPermissionTo = "Not Authenticated.")
    public boolean addCallDispositionCodesToGroup(@PathVariable long callDispositionGroupPk, @RequestBody Set<Long> callDispositionIds) throws IOException, StiException {
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
    public DialerQueueDetails createDialerQueue(@RequestBody DialerQueueDetails dialerQueueDetails) throws StiException {
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
    public DialerQueueDetails updateDialerQueue(@RequestBody DialerQueueDetails dialerQueueDetails) throws StiException {
        return dialerQueueService.updateDialerQueue(dialerQueueDetails);
    }


    /**
     * @param dqPK
     * @return json to the front-end
     * @throws StiException
     */
    @RequestMapping(value = "/deleteDialerQueue/{dqPK}", method = DELETE)
    @Authorization(permission = Permission.Authenticated, noPermissionTo = "no permission")
    public void deleteDialerQueue(@PathVariable("dqPK") long dqPK) throws StiException {
        dialerQueueService.deleteDialerQueue(dqPK);
    }

    @RequestMapping(value = "/getAllCallDispositionsForGroup/{callDispositionGroupPk}", method = GET)
    @Authorization(permission = Permission.Authenticated, noPermissionTo = "Not Authenticated.")
    public List<CallDispositionCode> getAllCallDispositionsForGroup(@PathVariable("callDispositionGroupPk") long callDispositionGroupPk) {
        return callDispositionService.getAllCallDispositionsForGroup(callDispositionGroupPk);
    }

    @RequestMapping(value = "/getAllDialerQueues/{queueIOType}", method = GET)
    @Authorization(permission = Permission.Authenticated, noPermissionTo = "Not Authenticated.")
    public List<DialerQueueGroup> getAllDialerQueues(@PathVariable("queueIOType") DialerQueueType dialerQueueType) throws IOException, StiException {
        return dialerQueueService.getDialerQueueGroups(dialerQueueType);
    }

    /*
     * JASON Sample:
      {"dialerQueuePk":75,"dialerQueue":null,"creationTime":null,"popupDisplayMode":null,"autoAnswerEnabled":false,"weightedPriority":{"priority":null,"weight":null},"idleMaxMinutes":null,"wrapMaxMinutes":null,"startTime":null,"endTime":null,"changeHistory":"","dialerSchedule":[],"maxDelayBeforeAgentAnswer":null,"agentCallOrder":[],"callRoutingOption":null,"roundRobinCutoffPercent":null,"disableSecondaryAgentsCallRouting":null,"forceVoicemail":null}
     */
    @RequestMapping(value = "/createOrUpdateInboundDQSettings", method = POST)
    @Authorization(permission = Permission.Authenticated, noPermissionTo = "Not Authenticated.")
    public DialerQueueSettings createOrUpdateInboundDQSettings(@RequestBody InboundDialerQueueSettings inboundDialerQueueSettings) throws StiException {
        return dialerQueueService.createOrUpdateDQSettings(inboundDialerQueueSettings);
    }

    /*
     * JSON Sample:
    {"dialerQueuePk":82,"dialerQueue":null,"creationTime":null,"popupDisplayMode":null,"autoAnswerEnabled":false,"weightedPriority":{"priority":null,"weight":null},"idleMaxMinutes":null,"wrapMaxMinutes":null,"startTime":null,"endTime":null,"changeHistory":"","dialerSchedule":[],"dialerMode":null,"previewDialerType":null,"callerId":null,"callerIdNumber":null,"voiceMailName":null,"holdMusicName":null,"bestTimeToCall":null,"maxDelayCallTime":null,"dispositionPlanName":null,"ignoreRecallTimesForSwitchedNumbers":null,"maxWaitForResult":null,"predictiveMaxCallDropPercent":null,"progressiveCallsPerAgent":null,"answeringMachineDetection":null,"interactiveVoiceResponse":null,"voiceMailOption":null,"spillOverActive":null,"scheduledRunTime":null,"oneTimeUse":null,"phoneTypesAllowed":null,"dialOrder":"1,0,3,2","orderByList":"1","leaveVoiceMailAt":null}
     */
    @RequestMapping(value = "/createOrUpdateOutboundDQSettings", method = POST)
    @Authorization(permission = Permission.Authenticated, noPermissionTo = "Not Authenticated.")
    public DialerQueueSettings createOrUpdateOutboundDQSettings(@RequestBody OutboundDialerQueueSettings outboundDialerQueueSettings) throws StiException {
        return dialerQueueService.createOrUpdateDQSettings(outboundDialerQueueSettings);
    }

    @RequestMapping(value = "/getOutboundDQSettingsByDQPk/{queuePk}", method = GET)
    @Authorization(permission = Permission.Authenticated, noPermissionTo = "Not Authenticated.")
    public OutboundDialerQueueSettings getOutboundDQSettingsByDQPk(
            @PathVariable("queuePk") long queuePk) throws StiException {
        return dialerQueueService.getOutboundDQSettingsByDQPk(queuePk);
    }

    @RequestMapping(value = "/getInboundDQSettingsByDQPk/{queuePk}", method = GET)
    @Authorization(permission = Permission.Authenticated, noPermissionTo = "Not Authenticated.")
    public InboundDialerQueueSettings getInboundDQSettingsByDQPk(
            @PathVariable("queuePk") long queuePk) throws StiException {
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
}
