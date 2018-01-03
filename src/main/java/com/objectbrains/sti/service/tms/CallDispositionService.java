/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.objectbrains.sti.service.tms;

import com.objectbrains.sti.db.entity.disposition.CallDispositionCode;
import com.objectbrains.sti.db.entity.disposition.CallDispositionGroup;
import com.objectbrains.sti.db.entity.disposition.action.CallDispositionAction;
import com.objectbrains.sti.service.utility.CSVParser;
import com.objectbrains.sti.db.repository.disposition.CallDispositionRepository;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import javax.persistence.EntityExistsException;
import javax.persistence.EntityManager;
import javax.persistence.EntityNotFoundException;
import javax.persistence.PersistenceContext;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ResourceUtils;


@Service
@Transactional
public class CallDispositionService {

    @Autowired
    private CallDispositionRepository dispositionRepository;

    @PersistenceContext
    private EntityManager em;

    private static final String DEFAULT_OUTBOUND_GROUP = "Default Outbound Call Disposition Group";
    private static final String DEFAULT_INBOUND_GROUP = "Default Inbound Call Disposition Group";

    private static final Logger LOG = LoggerFactory.getLogger(CallDispositionService.class);

    public CallDispositionCode addOrUpdateCallDisposition(CallDispositionCode disposition) {
        Validate.notNull(disposition, "Please provide disposition details.");
        Validate.notNull(disposition.getDisposition(), "Disposition name cannot be null.");
        CallDispositionCode existing = dispositionRepository.locateDisposition(disposition.getDisposition());
        if (existing != null && !Objects.equals(existing.getDispositionId(), disposition.getDispositionId())) {
            throw new EntityExistsException("CallDispositionCode with same name already exists: dispositionId [" + disposition.getDispositionId() + "]");
        }
        existing = dispositionRepository.locateDispositionById(disposition.getDispositionId());
        if (existing != null) {
            if (existing.getIsCode() != null && existing.getIsCode()) {
                disposition.setDisposition(existing.getDisposition());
                disposition.setCode(existing.getCode());
                disposition.setStatus(existing.getStatus());
            }
            disposition.setqCode(existing.getqCode());
            disposition.setSIPCode(existing.getSIPCode());
            disposition.setCause(existing.getCause());
            disposition.setDescription(existing.getDescription());
            if (disposition.getAction() != null && existing.getAction() != null) {
                disposition.getAction().setPk(existing.getAction().getPk());
            }
            if (existing.equals(disposition)) {
                return existing;
            }
        }
        return em.merge(disposition);
    }

    public CallDispositionCode getCallDispositionCode(long dispositionId) {
        CallDispositionCode code = dispositionRepository.locateDispositionById(dispositionId);
        if (code == null) {
            throw new EntityNotFoundException("CallDispositionCode with dispositionId [" + dispositionId + "] could not be found");
        }
        return code;
    }

    public CallDispositionCode getCallDispositionCode(String disposition) {
        CallDispositionCode code = dispositionRepository.locateDisposition(disposition);
        if (code == null) {
            throw new EntityNotFoundException("CallDispositionCode with disposition [" + disposition + "] could not be found");
        }
        return code;
    }

    public CallDispositionCode getCallDispositionCodeByQCode(int qCode) {
        return dispositionRepository.locateDispositionByQCode(qCode);
    }

    public List<CallDispositionCode> getAllCallDispositionCodes() {
        return dispositionRepository.locateAllCallDispositionCodes();
    }

    public void deleteCallDispositionCode(long dispositionId) {
        CallDispositionCode disposition = getCallDispositionCode(dispositionId);
        List<CallDispositionGroup> groups = dispositionRepository.locateAllDispositionGroupsForDispositionCode(dispositionId);
        for (CallDispositionGroup group : groups) {
            removeCallDispositionCodeFromGroup(disposition, group);
        }
        em.remove(disposition);
    }

    public CallDispositionGroup createOrUpdateDispositionGroup(CallDispositionGroup group) {
        if (group == null) {
            return null;
        }
        Validate.notNull(group.getName(), "Please provide the group name.");
        CallDispositionGroup existing = dispositionRepository.locateDispositionGroupByName(group.getName());
        if (existing != null && existing.getPk() != group.getPk()) {
            throw new IllegalArgumentException("Call Disposition Group with name[" + group.getName() + "] already exists.");
        }
        group = em.merge(group);
        return group;
    }

    public CallDispositionGroup getCallDispositionGroup(long dispositionGroupPk) {
        CallDispositionGroup group = dispositionRepository.locateDispositionGroupByPk(dispositionGroupPk);
        if (group == null) {
            throw new EntityNotFoundException("CallDispositionGroup with pk [" + dispositionGroupPk + "] could not be found");
        }
        return group;
    }

    public List<CallDispositionGroup> getAllCallDispositionGroups() {
        return dispositionRepository.locateCallDispositionGroups();
    }

    public void deleteCallDispositionGroup(long dispositionGroupPk) {
        CallDispositionGroup group = getCallDispositionGroup(dispositionGroupPk);
        removeAllDispositionCodesFromGroup(group);
        em.remove(group);
    }

    public List<CallDispositionCode> getAllCallDispositionsForGroup(long dispositionGroupPk) {
        CallDispositionGroup group = getCallDispositionGroup(dispositionGroupPk);
        return new ArrayList<>(group.getCallDispositionCodes());
    }

    public CallDispositionCode addCallDispositionToGroup(long dispositionGroupPk, long dispositionId) {
        CallDispositionGroup group = getCallDispositionGroup(dispositionGroupPk);
        CallDispositionCode disposition = getCallDispositionCode(dispositionId);
        addCallDispositionCodeToGroup(disposition, group);
        return disposition;
    }

    public void removeCallDispositionFromGroup(long dispositionGroupPk, long dispositionId) {
        CallDispositionGroup group = getCallDispositionGroup(dispositionGroupPk);
        CallDispositionCode disposition = getCallDispositionCode(dispositionId);
        removeCallDispositionCodeFromGroup(disposition, group);
    }

    public List<CallDispositionCode> setCallDispositionsToGroup(long dispositionGroupPk, Set<Long> dispositionIds) {
        CallDispositionGroup group = getCallDispositionGroup(dispositionGroupPk);
//        removeAllDispositionCodesFromGroup(group);
        List<CallDispositionCode> codes = new ArrayList<>();
        for (Long id : dispositionIds) {
            if (id != null) {
                CallDispositionCode disposition = getCallDispositionCode(id);
                codes.add(disposition);
//                addCallDispositionCodeToGroup(disposition, group);
            }
        }
        group.setCallDispositionCodes(codes);
        return codes;
    }

    public List<CallDispositionGroup> getAllDispositionGroupsForDispositionId(long dispositionId) {
        return dispositionRepository.locateAllDispositionGroupsForDispositionCode(dispositionId);
    }

    private void removeAllDispositionCodesFromGroup(CallDispositionGroup group) {
        group.getCallDispositionCodes().clear();
//        List<CallDispositionCode> codes = new ArrayList<>(group.getCallDispositionCodes());
//        for (CallDispositionCode code : codes) {
//            removeCallDispositionCodeFromGroup(code, group);
//                    LOG.info("Removing disposition code [{}] {} to group {}", code.getDispositionId(), code.getDisposition(), group.getPk());
//
//        em.flush();
//        }
    }

    private void addCallDispositionCodeToGroup(CallDispositionCode code, CallDispositionGroup group) {
        if (!group.getCallDispositionCodes().contains(code)) {
            group.getCallDispositionCodes().add(code);
        }
    }

    private void removeCallDispositionCodeFromGroup(CallDispositionCode code, CallDispositionGroup group) {
        group.getCallDispositionCodes().remove(code);
    }

    public CallDispositionGroup getCallDispositionGroupByName(String name) {
        return dispositionRepository.locateDispositionGroupByName(name);
    }

    public CallDispositionGroup getDefaultOutboundCallDispositionGroup() {
        return dispositionRepository.locateDispositionGroupByName(DEFAULT_OUTBOUND_GROUP);
    }

    public CallDispositionGroup getDefaultInboundCallDispositionGroup() {
        return dispositionRepository.locateDispositionGroupByName(DEFAULT_INBOUND_GROUP);
    }

    public List<CallDispositionCode> getAllDefaultInboundDispositionCodes() {
        CallDispositionGroup group = getDefaultInboundCallDispositionGroup();
        return getAllCallDispositionsForGroup(group.getPk());
    }

    public List<CallDispositionCode> getAllDefaultOutboundDispositionCodes() {
        CallDispositionGroup group = getDefaultOutboundCallDispositionGroup();
        return getAllCallDispositionsForGroup(group.getPk());
    }

    public CallDispositionGroup createDefaultOutboundCallDispositionGroup() {
        return createDefaultCallDispositionGroup(DEFAULT_OUTBOUND_GROUP);
    }

    public CallDispositionGroup createDefaultInboundCallDispositionGroup() {
        return createDefaultCallDispositionGroup(DEFAULT_INBOUND_GROUP);
    }

    private CallDispositionGroup createDefaultCallDispositionGroup(String name) {
        CallDispositionGroup group = getCallDispositionGroupByName(name);
        if (group == null) {
            group = new CallDispositionGroup();
            group.setName(name);
            group = createOrUpdateDispositionGroup(group);
        }
        return group;
    }

    public List<CallDispositionCode> loadAllCallDispositionCodes() throws IOException {
        List<CallDispositionCode> codes = new ArrayList<>();
        File file = ResourceUtils.getFile("classpath:com/objectbrains/svc/dialer/CallDispositionCodes.csv");
        String s = IOUtils.toString(ResourceUtils.getURL("classpath:com/objectbrains/svc/dialer/DefaultInboundCallDispositionCodes.properties"));
        List<String> inboundCodes = Arrays.asList(s.split("\n"));
        s = IOUtils.toString(ResourceUtils.getURL("classpath:com/objectbrains/svc/dialer/DefaultOutboundCallDispositionCodes.properties"));
        List<String> outboundCodes = Arrays.asList(s.split("\n"));
        List<CallDispositionCode> list = CSVParser.buildParser(file).toBean(CallDispositionCode.class);
        for (CallDispositionCode code : list) {
            if (dispositionRepository.locateDisposition(code.getDisposition()) == null) {
                code = addOrUpdateCallDisposition(code);
                codes.add(code);
                String id = String.valueOf(code.getDispositionId());
                if (inboundCodes.contains(id)) {
                    addCallDispositionCodeToGroup(code, getDefaultInboundCallDispositionGroup());
                }
                if (outboundCodes.contains(id)) {
                    addCallDispositionCodeToGroup(code, getDefaultOutboundCallDispositionGroup());
                }
            }
        }
        return codes;
    }

    public CallDispositionAction getActionForCallDispositionId(long dispositionId) {
        CallDispositionCode code = getCallDispositionCode(dispositionId);
        return code.getAction();
    }

}
