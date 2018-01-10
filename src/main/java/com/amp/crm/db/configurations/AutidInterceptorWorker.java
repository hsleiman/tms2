/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.crm.db.configurations;


import com.amp.crm.constants.WorkLogConstants;
import com.amp.crm.constants.Language;
import com.amp.crm.db.entity.log.DataChangeLog;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.apache.commons.beanutils.BeanMap;
import org.apache.commons.beanutils.PropertyUtilsBean;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public class AutidInterceptorWorker implements Runnable {

    @PersistenceContext
    private EntityManager entityManager;

    private static Logger LOG = LoggerFactory.getLogger(AutidInterceptorWorker.class);

    private ArrayList<ChangedEntity> changedEntities = null;
    private AuditEntityManager auditEntityManager = null;

    private String sessionIdLogNote = "[Uknown session]";

    @Override
    public void run() {

        for (int i = 0; i < changedEntities.size(); i++) {
            ChangedEntityExpander ce = new ChangedEntityExpander(changedEntities.get(i));

            for (int index = 0; index < ce.getPropertyNames().length; index++) {
                boolean changed = addDatachangeLog(ce, index);
                if (changed) {
                    String propertyName = ce.getPropertyNames()[index];
                }
            }
        }

    }

    public AutidInterceptorWorker(ArrayList<ChangedEntity> changedEntities) {
        this.changedEntities = changedEntities;
        auditEntityManager = (AuditEntityManager) ApplicationContextProvider.getApplicationContext().getBean(AuditEntityManager.class);
        sessionIdLogNote = ThreadAttributes.getTranstionAndThreadInfo();
    }

    private boolean isActive(String className, String attributeName) {
        return auditEntityManager.isActive(className, attributeName);
    }

    private boolean isHidden(String className, String attributeName) {
        return auditEntityManager.isHidden(className, attributeName);
    }

    private long getLogType(String className, String attributeName) {
        return auditEntityManager.getLogType(className, attributeName);
    }

    private String getLogDesc(String className, String attributeName) {
        return auditEntityManager.getLogDesc(className, attributeName);
    }

    private boolean isActiveClass(String className) {
        return auditEntityManager.isActive(className);
    }

    private boolean isHiddenClass(String className) {
        return auditEntityManager.isHidden(className);
    }

    private boolean compareObjectsIsEqual(Object oldObject, Object newObject) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        BeanMap map = new BeanMap(oldObject);

        PropertyUtilsBean propUtils = new PropertyUtilsBean();

        for (Object propNameObject : map.keySet()) {
            String propertyName = (String) propNameObject;
            Object property1 = propUtils.getProperty(oldObject, propertyName);
            Object property2 = propUtils.getProperty(newObject, propertyName);
            if (property1 == null && property2 != null) {
                return true;
            } else if (property1 == null && property2 == null) {
                return false;
            }
            return !property1.equals(property2);
        }
        return false;

    }

    private boolean addDatachangeLog(ChangedEntityExpander ce, int index) {
        if(ce.getAppPk() == null){
            LOG.info("Cannot determine appPk for entity "+ce);
            return false;
        }
        DataChangeLog changeLog = null;
        if (ce.getChangeType() == WorkLogConstants.DATA_CHANGE_LOG_CREATE) {
            changeLog = new DataChangeLog();
            if (ce.getCurrentState()[index] != null) {
                changeLog.setNewValue(getDataChangeValueOfPropertyName(ce.getPropertyNames()[index], ce.getCurrentState()[index], ce.getClassName()));
            }
        } else if (ce.getChangeType() == WorkLogConstants.DATA_CHANGE_LOG_UPDATE) {

            boolean isEqual = true;

            try {
                isEqual = compareObjectsIsEqual(ce.getPreviousState()[index], ce.getCurrentState()[index]);
            } catch (Exception ex) {
                LOG.error("[Error Compare]inputs: propertyName: {} current: {}   previous: {}", ce.getPropertyNames()[index], ce.getCurrentState()[index], ce.getPreviousState()[index]);
            }

            if (isEqual) {
                return false;
            }
            changeLog = new DataChangeLog();
            if (ce.getCurrentState()[index] != null) {
                changeLog.setNewValue(getDataChangeValueOfPropertyName(ce.getPropertyNames()[index], ce.getCurrentState()[index], ce.getClassName()));
            }
            if (ce.getPreviousState()[index] != null) {
                changeLog.setOldValue(getDataChangeValueOfPropertyName(ce.getPropertyNames()[index], ce.getPreviousState()[index], ce.getClassName()));
            }

        } else if (ce.getChangeType() == WorkLogConstants.DATA_CHANGE_LOG_DELETE) {
            changeLog = new DataChangeLog();
            if (ce.getCurrentState()[index] != null) {
                changeLog.setOldValue(getDataChangeValueOfPropertyName(ce.getPropertyNames()[index], ce.getCurrentState()[index], ce.getClassName()));
            }
            if (ce.getPreviousState()[index] != null) {
                changeLog.setOldValue(getDataChangeValueOfPropertyName(ce.getPropertyNames()[index], ce.getPreviousState()[index], ce.getClassName()));
            }
        }
        if (changeLog != null) {
            switch (ce.getPropertyNames()[index]) {
                case "claimDelivery":
                    changeLog.setAttributeName("litigationType");
                    break;
                case "withdrawalDate":
                    changeLog.setAttributeName("motionForReliefDate");
                    break;
                case "markedBadTimeStamp":
                    changeLog.setAttributeName("dateMailReturned");
                    break;
                default:
                    changeLog.setAttributeName(ce.getPropertyNames()[index]);
                    break;
            }
            changeLog.setClassName(ce.getClassName());
            changeLog.setPkReference(ce.getEntityPK());
            changeLog.setAppPk(ce.getAppPk());    
            changeLog.setValueType(ce.getTypes()[index]);
            changeLog.setType(ce.getChangeType());
            //TODO Something
            //addChangeLog(changeLog, changedObject);
            entityManager.persist(changeLog);
            LOG.info("[Adding Data Change Log] inputs: propertyName: {} current: {}   previous: {} new Value: {} Old Value: {} Change Type: {}", ce.getPropertyNames()[index], ce.getCurrentState()[index], ce.getPreviousState()[index], changeLog.getNewValue(), changeLog.getOldValue(), ce.getChangeType());
            return true;
        }
        return false;
    }

    // Below are helper method
    private String getDataChangeValueOfPropertyName(String propertyName, Object obj, String className) {
        String newValue = obj.toString();

        if (StringUtils.isNotBlank(propertyName)) {
            if (propertyName.contains("repoType")) {
                Integer repoType = (Integer) obj;
                if (repoType == 0) {
                    newValue = "Involuntary";
                } else if (repoType == 1) {
                    newValue = "Voluntary";
                } else if (repoType == 2) {
                    newValue = "Impound";
                }
            } else if (propertyName.contains("bkType")) {
                Integer bkType = (Integer) obj;
                if (bkType == 0) {
                    newValue = "Chapter 7";
                } else if (bkType == 2) {
                    newValue = "Chapter 11";
                } else if (bkType == 3) {
                    newValue = "Chapter 12";
                } else if (bkType == 1) {
                    newValue = "Chapter 13";
                }
            } else if (propertyName.contains("value") && className.equals("SvOptOutCodes")) {
                Integer value = (Integer) obj;
                if (value == 1) {
                    newValue = "Yes";
                } else if (value == 0) {
                    newValue = "No";
                }
            } else if (propertyName.contains("payDay1")) {
                Integer payDay1 = (Integer) obj;
                if (payDay1 == 1) {
                    newValue = "Sunday";
                } else if (payDay1 == 2) {
                    newValue = "Monday";
                } else if (payDay1 == 3) {
                    newValue = "Tuesday";
                } else if (payDay1 == 4) {
                    newValue = "Wednesday";
                } else if (payDay1 == 5) {
                    newValue = "Thursday";
                } else if (payDay1 == 6) {
                    newValue = "Friday";
                } else if (payDay1 == 7) {
                    newValue = "Saturday";
                }
            } else if (propertyName.contains("litigationCourtType")) {
                Integer value = Integer.parseInt((String) obj);
                if (value == 1) {
                    newValue = "Small Claims";
                } else if (value == 0) {
                    newValue = "Superior";
                }
            } else if (propertyName.contains("litigationServiceType")) {
                Integer value = (Integer) obj;
                if (value == 2) {
                    newValue = "Publication";
                } else if (value == 1) {
                    newValue = "Sub-service";
                } else if (value == 0) {
                    newValue = "Personal";
                }

            } else if (propertyName.equalsIgnoreCase("callbackdatetime")) {
                LOG.info("CallBackDateTime value: {} to string: {}", obj, newValue);
                if (StringUtils.isNotBlank(newValue)) {
                    //LocalDateTime dt2 = (LocalDateTime)obj;
                    LocalDateTime dt1 = LocalDateTime.parse(newValue);
                    LOG.info("callbackdatetime new value {} to LocalDateTime: {} to String is: {}", newValue, dt1, dt1.toString("yyyy-MM-dd hh:mm aa"));
                    //LOG.info("callbackdatetime new value {} to LocalDateTime: {} to String is: {}", obj, dt2, dt2.toString("yyyy-MM-dd hh:mm aa"));
                    newValue = dt1.toString("yyyy-MM-dd hh:mm aa");
                }

            } else if (propertyName.equalsIgnoreCase("language") && (className.equals("SvPrimaryBorrower") || className.equals("SvCoBorrower") || className.equals("SvGuarantor") || className.equals("SvBusinessBorrower"))) {
                String value = (String) obj;
                LOG.info("value:{}", value);
                if (value.equals("en-MX")) {
                    newValue = Language.SPANISH.getLanguage();
                } else if (value.equals("en-US")) {
                    newValue = Language.ENGLISH.getLanguage();
                }
            } else if (propertyName.equals("markedBadTimeStamp")) {
                newValue = trimDateTimeToDate(obj).toString("MM/dd/yyyy");
            }
        }

        return newValue;
    }

    private LocalDate trimDateTimeToDate(Object obj) {
        LocalDate dateValue = new LocalDate();
        LocalDateTime value = (LocalDateTime) obj;
        dateValue = value.toLocalDate();
        return dateValue;
    }

}
