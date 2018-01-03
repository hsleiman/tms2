/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.objectbrains.sti.db.auditInterceptor;

import com.objectbrains.hcms.annotation.ConfigContext;
import com.objectbrains.hcms.configuration.ConfigurationUtility;
import com.objectbrains.sti.db.entity.log.DataChangeLog;
import java.lang.reflect.Method;
import java.util.ArrayList;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author hsleiman
 */
@Repository("auditEntityManager")
public class AuditEntityManager {

    @PersistenceContext
    private EntityManager entityManager;

    @ConfigContext
    private ConfigurationUtility config;

    private static final Logger LOG = LoggerFactory.getLogger(AuditEntityManager.class);

    public EntityManager getEntityManager() {
        return entityManager;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveChangeLog(ArrayList<DataChangeLog> changeLogs) {
        for (DataChangeLog changeLog : changeLogs) {
            entityManager.persist(changeLog);
        }
    }

    public long getAccountPkFromClientPk(long borrowerPk) {
        //TODO
//        long appPk = 0;
//        if(borrowerPk!=0){
//            try {
//                LosBorrower bwr = bwrRepo.locateByBorrowerPk(borrowerPk);
//                
//                appPk = bwr.getLosApplication() != null ? bwr.getLosApplication().getPk() : 0;
//            } catch (LosObjectNotFoundException | LosTooManyObjectFoundException ex) {
//                LOG.info(ex.getMessage());
//            }
//        }
//        return appPk;
        return 0;
    }

    public boolean isActive(String className, String attributeName) {
        boolean isActive = config.getBoolean(className + ".*", Boolean.FALSE);
        isActive = config.getBoolean(className + "." + attributeName, isActive);
        return isActive;
    }

    public boolean isActive(String key) {
        return config.getBoolean(key, Boolean.FALSE);
    }

    public boolean isHidden(String className, String attributeName) {
        boolean isHidden = config.getBoolean(className + ".*.hidden", isHidden(className));
        isHidden = config.getBoolean(className + "." + attributeName + ".hidden", isHidden);
        return isHidden;
    }

    public boolean isHidden(String classname) {
        return config.getBoolean(classname + ".hidden", Boolean.FALSE);
    }

    public long getLogType(String className, String attributeName) {
        long logType = config.getLong(className + ".*.logtype", getLogType(className));
        logType = config.getLong(className + "." + attributeName + ".logtype", logType);
        return logType;
    }

    public long getLogType(String className) {
        return config.getLong(className + ".logtype", 0L);
    }

    public String getLogDesc(String className, String attributeName) {
        String logDesc = config.getString(className + ".*.desc", getLogDesc(className));
        logDesc = config.getString(className + "." + attributeName + ".desc", logDesc);
        return logDesc;
    }

    public String getLogDesc(String className) {
        return config.getString(className + ".desc", "");
    }

    public static boolean isSetter(Method method) {
        return method.getName().startsWith("set");
    }

}
