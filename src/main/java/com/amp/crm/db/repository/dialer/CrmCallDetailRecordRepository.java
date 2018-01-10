/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.crm.db.repository.dialer;

import com.amp.crm.constants.CallDirection;
import com.amp.crm.db.entity.base.account.Account;
import com.amp.crm.db.entity.base.WorkQueue;
import com.amp.crm.db.entity.base.dialer.CallDetailRecord;
import com.amp.crm.db.entity.base.dialer.SpeechToText;
import com.amp.crm.db.entity.base.dialer.VoiceMail;
import com.amp.crm.db.entity.log.WorkCallLog;
import com.amp.crm.db.repository.account.AccountRepository;
import com.amp.crm.db.repository.account.WorkQueueRepository;
import com.amp.crm.pojo.VoiceMailPojo;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * 
 */
@Repository
public class CrmCallDetailRecordRepository {

    @PersistenceContext
    private EntityManager em;
    @Autowired
    private WorkQueueRepository workQRepo;
    @Autowired
    private AccountRepository accountRepo;

    public CallDetailRecord locateCallDetailRecordByPk(long pk) {
        return em.find(CallDetailRecord.class, pk);
    }

    public CallDetailRecord locateCallDetailRecordByCallUUID(String callUUID) {
        TypedQuery<CallDetailRecord> q = em.createNamedQuery("CallDetailRecord.LocateByCallUUID", CallDetailRecord.class);
        q.setParameter("callUUID", callUUID);
        List<CallDetailRecord> list = q.getResultList();
        return list.isEmpty() ? null : list.get(0);
    }

    public List<CallDetailRecord> locateAllCDRByAccountPk(long accountPk) {
        TypedQuery<CallDetailRecord> q = em.createNamedQuery("CallDetailRecord.LocateAllByAccountPk", CallDetailRecord.class);
        q.setParameter("accountPk", accountPk);
        return q.getResultList();
    }

    public List<CallDetailRecord> getAllCDRByAccountPkAndDateRange(Long accountPk, LocalDate fromDate, LocalDate toDate, Integer pageNum, Integer pageSize) {
        if (fromDate == null || toDate == null) {
            return locateAllCDRByAccountPk(accountPk);
        }
        return locateByAccountPkAndDateRange(accountPk, fromDate, toDate, pageNum, pageSize);
    }

    public List<CallDetailRecord> getAllCDR(LocalDate fromDate, LocalDate toDate, Integer pageNum, Integer pageSize) {
        if (fromDate == null || toDate == null) {
            TypedQuery<CallDetailRecord> q = em.createNamedQuery("CallDetailRecord.LocateAll", CallDetailRecord.class);
            if (pageNum != null && pageSize != null) {
                q.setFirstResult(pageNum * pageSize);
                q.setMaxResults(pageSize);
            }
            return q.getResultList();
        } else {
            return locateByAccountPkAndDateRange(null, fromDate, toDate, pageNum, pageSize);
        }
    }

    @SuppressWarnings("unchecked")
    public List<CallDetailRecord> getAllCDR(LocalDate fromDate, LocalDate toDate, String callerPhoneNumber, String calleePhoneNumber, Integer callType, Long accountPk,
            Boolean dialerCall, String userDisposition, Integer pageNum, Integer pageSize) {
        if (fromDate != null && toDate != null && callerPhoneNumber == null && calleePhoneNumber == null
                && callType == null && accountPk == null && dialerCall == null && userDisposition == null) {
            return locateByAccountPkAndDateRange(null, fromDate, toDate, pageNum, pageSize);
        } else {
            Criteria criteria = createCriteriaForCallHistory(fromDate, toDate, callerPhoneNumber, calleePhoneNumber, callType, accountPk, dialerCall, userDisposition);
            if (pageNum != null && pageSize != null) {
                criteria.setFirstResult(pageNum * pageSize);
                criteria.setMaxResults(pageSize);
            }
            return (List<CallDetailRecord>) criteria.list();
        }
    }

    private Criteria createCriteriaForCallHistory(LocalDate fromDate, LocalDate toDate, String callerPhoneNumber, String calleePhoneNumber, Integer callType, Long accountPk,
            Boolean dialerCall, String userDisposition) {
        Session session = em.unwrap(Session.class);
        Criteria criteria = session.createCriteria(CallDetailRecord.class).addOrder(Order.desc("startTime"));
        if (fromDate != null && toDate != null) {
            criteria.add(Restrictions.between("startTime", fromDate.toLocalDateTime(LocalTime.MIDNIGHT), toDate.toLocalDateTime(LocalTime.MIDNIGHT).plusDays(1).minusMinutes(1)));
        }
        if (callerPhoneNumber != null) {
            criteria.add(Restrictions.ilike("callerIdNumber", callerPhoneNumber));
        }
        if (calleePhoneNumber != null) {
            criteria.add(Restrictions.ilike("calleeIdNumber", calleePhoneNumber));
        }
        if (callType != null) {
            if (callType == 1) {
                criteria.add(Restrictions.eq("callDirection", CallDirection.INBOUND));
            } else if (callType == 2) {
                criteria.add(Restrictions.eq("callDirection", CallDirection.OUTBOUND));
            }
        } else {
            criteria.add(Restrictions.or(Restrictions.eq("callDirection", CallDirection.OUTBOUND), Restrictions.eq("callDirection", CallDirection.INBOUND)));
        }
        if (accountPk != null) {
            criteria.add(Restrictions.eq("accountPk", accountPk));
        }
        if (dialerCall != null) {
            if (dialerCall == true) {
                criteria.add(Restrictions.eq("dialer", true));
            } else {
                criteria.add(Restrictions.eq("dialer", false));
            }
        }
        if (userDisposition != null) {
            criteria.add(Restrictions.eq("userDisposition", userDisposition));
        }
        return criteria;
    }

    public Long getCDRCount(LocalDate fromDate, LocalDate toDate) {
        Query q;
        if (fromDate == null || toDate == null) {
            q = em.createNamedQuery("CallDetailRecord.CDRCount");
        } else {
            q = em.createNamedQuery("CallDetailRecord.CDRByAccountPkAndDateRangeCount")
                    .setParameter("fromDate", fromDate.toDate())
                    .setParameter("toDate", toDate.toDate());
        }
        return ((Number) q.getSingleResult()).longValue();
    }

    public Long getCDRCount(LocalDate fromDate, LocalDate toDate, String callerPhoneNumber, String calleePhoneNumber, Integer callType, Long accountPk,
            Boolean dialerCall, String userDisposition) {
        if (callerPhoneNumber == null && calleePhoneNumber == null && callType == null && accountPk == null && dialerCall == null && userDisposition == null) {
            Query q;
            if (fromDate == null || toDate == null) {
                q = em.createNamedQuery("CallDetailRecord.CDRCount");
            } else {
                q = em.createNamedQuery("CallDetailRecord.CDRByAccountPkAndDateRangeCount")
                        .setParameter("fromDate", fromDate.toDate())
                        .setParameter("toDate", toDate.toDate());
            }
            return ((Number) q.getSingleResult()).longValue();
        } else {
            return Long.valueOf(createCriteriaForCallHistory(fromDate, toDate, callerPhoneNumber, calleePhoneNumber, callType, accountPk, dialerCall, userDisposition).list().size());
        }
    }

    @Transactional
    public List<CallDetailRecord> getAllCDRForAutomatedWorkCallLogSweep(LocalDateTime startDate, LocalDateTime endDate) {
        TypedQuery<CallDetailRecord> query;
        if (startDate == null || endDate == null) {
            startDate = LocalDateTime.now().minusHours(24);
            endDate = LocalDateTime.now().minusHours(1);
        }
        query = em.createNamedQuery("CallDetailRecord.GetAllCDRForPhoneRecordingSweep", CallDetailRecord.class);
        query.setParameter("startDate", startDate);
        query.setParameter("endDate", endDate);

        return query.getResultList();
    }

    @Transactional
    public List<CallDetailRecord> getAllCDRForBestTimeToCallSweep(LocalDateTime startTime, LocalDateTime endTime) {
        if (startTime == null || endTime == null) {
            startTime = LocalDateTime.now().minusDays(1).minusHours(1);
            endTime = LocalDateTime.now().minusHours(1);
        }
        TypedQuery<CallDetailRecord> query = em.createNamedQuery("CallDetailRecord.GetCDRForBestTimeToCallSweep", CallDetailRecord.class);
        query.setParameter("startDate", startTime);
        query.setParameter("endDate", endTime);
        return query.getResultList();
    }

    private List<CallDetailRecord> locateByAccountPkAndDateRange(Long accountPk, LocalDate fromDate, LocalDate toDate, Integer pageNum, Integer pageSize) {
        TypedQuery<CallDetailRecord> q = em.createNamedQuery("CallDetailRecord.GetAllCDRByAccountPkAndDateRange", CallDetailRecord.class);
        q.setParameter("accountPk", accountPk);
        q.setParameter("fromDate", fromDate.toDate());
        q.setParameter("toDate", toDate.toDate());
        if (pageNum != null && pageSize != null) {
            q.setFirstResult(pageNum * pageSize);
            q.setMaxResults(pageSize);
        }
        return q.getResultList();
    }

    public List<WorkCallLog> locateWorkCallLogByCallUUID(String callUUID) {
        TypedQuery<WorkCallLog> q = em.createNamedQuery("WorkCallLog.LocateByCallUUID", WorkCallLog.class);
        q.setParameter("callUUID", callUUID);
        return q.getResultList();
    }

    public List<VoiceMail> getVoiceMailsForQueueAndDates(String queueName, LocalDate startDate, LocalDate endDate, Long portfolioType, Integer pageNumber, Integer pageSize) {
        List<Long> workQueuePks = new ArrayList<>();
        if (StringUtils.isNotBlank(queueName)) {
            WorkQueue colQ = workQRepo.getWorkQueueByQueueName(queueName);
            workQueuePks.add(colQ.getPk());
        }
        if (portfolioType != null) {
            List<WorkQueue> queues = workQRepo.getAllQueuesByPortfolioType(portfolioType);
            if (queues != null && !queues.isEmpty()) {
                for (WorkQueue workQ : queues) {
                    workQueuePks.add(workQ.getPk());
                }
            }
        }
        if (!workQueuePks.isEmpty() && startDate != null && endDate != null) {
            TypedQuery<VoiceMail> q = em.createNamedQuery("VoiceMail.LocateByQueueAndDateRange", VoiceMail.class);
            q.setParameter("queuePks", workQueuePks)
                    .setParameter("startDate", startDate.toDate())
                    .setParameter("endDate", endDate.toDate());
            if (pageNumber != null && pageSize != null) {
                q.setFirstResult(pageNumber * pageSize);
                q.setMaxResults(pageSize);
            }
            return q.getResultList();

        } else if (!workQueuePks.isEmpty()) {
            TypedQuery<VoiceMail> q = em.createNamedQuery("VoiceMail.LocateByQueuePk", VoiceMail.class);
            q.setParameter("queuePks", workQueuePks);
            if (pageNumber != null && pageSize != null) {
                q.setFirstResult(pageNumber * pageSize);
                q.setMaxResults(pageSize);
            }
            return q.getResultList();
        } else if (startDate != null && endDate != null) {
            TypedQuery<VoiceMail> q = em.createNamedQuery("VoiceMail.LocateByDateRange", VoiceMail.class);
            q.setParameter("startDate", startDate.toDate())
                    .setParameter("endDate", endDate.toDate());
            if (pageNumber != null && pageSize != null) {
                q.setFirstResult(pageNumber * pageSize);
                q.setMaxResults(pageSize);
            }
            return q.getResultList();
        }
        TypedQuery<VoiceMail> q = em.createNamedQuery("VoiceMail.LocateAll", VoiceMail.class);
        return q.getResultList();
    }

    public VoiceMail findVoiceMailByPk(long pk) {
        return em.find(VoiceMail.class, pk);
    }

    public List<VoiceMail> getVoiceMailsForAccountPkandDates(LocalDate startDate, LocalDate endDate, long accountPk, Integer pageNumber, Integer pageSize) {
        if (accountPk > 0 && startDate != null && endDate != null) {
            TypedQuery<VoiceMail> q = em.createNamedQuery("VoiceMail.LocateByAccountPkAndDateRange", VoiceMail.class);
            q.setParameter("accountPk", accountPk)
                    .setParameter("startDate", startDate.toDate())
                    .setParameter("endDate", endDate.toDate());
            if (pageNumber != null && pageSize != null) {
                q.setFirstResult(pageNumber * pageSize);
                q.setMaxResults(pageSize);
            }
            return q.getResultList();
        } else if (accountPk > 0) {
            TypedQuery<VoiceMail> q = em.createNamedQuery("VoiceMail.LocateByAccountPk", VoiceMail.class);
            q.setParameter("accountPk", accountPk);
            if (pageNumber != null && pageSize != null) {
                q.setFirstResult(pageNumber * pageSize);
                q.setMaxResults(pageSize);
            }
            return q.getResultList();
        }
        return null;
    }

    public VoiceMailPojo getReturnPojoFromVoicemail(VoiceMail voicemail) {
        if (voicemail == null) {
            return null;
        }
        VoiceMailPojo vmPojo = voicemail.toPojo();
        Long accountPk = voicemail.getAccountPk();
        if (accountPk != null && accountPk > 0) {
            Account account = accountRepo.findAccountByPk(accountPk);
            Set<WorkQueue> workQs = account.getWorkQueues();
            for (WorkQueue workQ : workQs) {
                if (workQ != null) {
                    vmPojo.setPortfolioType(workQ.getWorkQueueData().getPortfolioType());
                    vmPojo.setPortfolioDesc(workQ.getWorkQueueData().getPortfolioDesc());
                    vmPojo.setQueueName(workQ.getWorkQueueData().getQueueName());
                }
            }
        }
        return vmPojo;
    }

    public SpeechToText locateSpeeechToTextForUuid(String callUUID) {
        List<SpeechToText> resultList = em.createQuery("select s from SpeechToText s where s.callUUID = :callUUID", SpeechToText.class).setParameter("callUUID", callUUID).getResultList();
        if (resultList == null || resultList.isEmpty()) {
            return null;
        }
        return resultList.get(0);
    }
}
