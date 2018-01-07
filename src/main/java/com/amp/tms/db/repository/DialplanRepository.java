/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.tms.db.repository;

import com.amp.tms.db.entity.freeswitch.LogDialplan;
import com.amp.tms.db.entity.freeswitch.LogDialplanInfo;
import com.amp.tms.db.entity.freeswitch.TMSDialplan;
import com.amp.tms.freeswitch.pojo.DialplanVariable;
import com.amp.tms.service.FreeswitchConfiguration;
import java.util.UUID;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author Hoang, J, Bishistha
 */
@Repository
@Transactional
public class DialplanRepository {

    private static final Logger log = LoggerFactory.getLogger(DialplanRepository.class);

    @Autowired
    private FreeswitchConfiguration configuration;

    @PersistenceContext
    private EntityManager entityManager;

    public String LogDialplan(DialplanVariable variable, TMSDialplan dialplan) {
        if (true) {
            log.info("+++++++++++++++++++++++++++++++++++++++++++");
            log.info("Log Dialplan: " + "++++++++++++++++++++++++");
            log.info("+++++++++++++++++++++++++++++++++++++++++++");
            log.info(dialplan.getXml());
            log.info("+++++++++++++++++++++++++++++++++++++++++++");
            log.info("Log Dialplan: " + "++++++++++++++++++++++++");
            log.info("+++++++++++++++++++++++++++++++++++++++++++");
        }
        LogDialplan d = new LogDialplan();
        String jsonStr = variable.toJson();
        if (jsonStr.length() < 10000) {
            d.setDump(jsonStr);
        } else {
            d.setDump(jsonStr.substring(0, 9999));
        }

        d.setTms_uuid(dialplan.getKey().getTms_uuid());
        d.setOrderPower(dialplan.getKey().getOrderPower());

        d.setXml(dialplan.getXml());
        d.setCaller_ANI(variable.getCallerIdNumber());
        d.setContext(variable.getContext());
        d.setCaller_Destination_Number(variable.getCalleeIdNumber());
        entityManager.persist(d);
        return dialplan.getXml();
    }

    public String LogDialplanText(String text) throws Exception {

        LogDialplan d = new LogDialplan();
        d.setTms_uuid(UUID.randomUUID().toString());
        d.setOrderPower(UUID.randomUUID().toString());

        d.setXml(text);
        entityManager.persist(d);

        if (true) {
            throw new Exception("ha ha");
        }
        return text;
    }

    public void logDialplanInfoIntoDb(String callUUID, Object... content) {

        if (configuration.getDbLoging() == false) {
            return;
        }
        try {
            StackTraceElement[] elements = Thread.currentThread().getStackTrace();
            LogDialplanInfo dialplanInfo = new LogDialplanInfo();
            dialplanInfo.setCallUUID(callUUID);

            String replacer = "";
            for (int i = 0; i < content.length; i++) {
                Object string = content[i];
                log.debug("Rplacer Text [{}] <- {}", replacer, string);
                if (i == 0) {
                    replacer = (String) string;
                } else {
                    replacer = replacer.replaceFirst("\\{\\}", string + "");
                }
            }

            if (replacer.length() > 9999) {
                replacer = replacer.substring(0, 9999);
            }
            dialplanInfo.setContent(replacer);

            String className = "";
            String methodName = "";
            String lineNumber = "";

            String lastClassName = "";
            String lastMethodName = "";
            Integer lastLineNumber = 0;
            String spring = "org.springframework";
            String dialplanRepository = "com.objectbrains.tms.db.repository.DialplanRepository";
            String dialplanService = "com.objectbrains.tms.service.DialplanService";
            boolean ready = false;
            boolean done = false;
            for (int i = 0; i < elements.length; i++) {
                StackTraceElement element = elements[i];
                if (className.equals("")) {
                    className = element.getClassName();
                    methodName = element.getMethodName();
                    lineNumber = element.getLineNumber() + "";
                } else {
                    className = className + "," + element.getClassName();
                    methodName = methodName + "," + element.getMethodName();
                    lineNumber = lineNumber + "," + element.getLineNumber();
                }

                if (element.getClassName().startsWith(spring) || element.getClassName().startsWith(dialplanRepository) || (element.getClassName().startsWith(dialplanService) && element.getMethodName().equalsIgnoreCase("LogDialplanInfoIntoDb"))) {
                    ready = true;
                } else {
                    if (ready && done == false) {
                        lastClassName = element.getClassName();
                        lastMethodName = element.getMethodName();
                        lastLineNumber = element.getLineNumber();
                        done = true;
                    }
                }
            }
            if (className.length() > 9999) {
                className = className.substring(0, 9999);
            }
            if (methodName.length() > 9999) {
                methodName = methodName.substring(0, 9999);
            }

            dialplanInfo.setClassName(className);
            dialplanInfo.setMethodName(methodName);
            dialplanInfo.setLineNumber(lineNumber);
            dialplanInfo.setLastClassName(lastClassName);
            dialplanInfo.setLastMethodName(lastMethodName);
            dialplanInfo.setLastLineNumber(lastLineNumber);

            dialplanInfo.setThreadId(Thread.currentThread().getId());
            dialplanInfo.setThreadName(Thread.currentThread().getName());

            dialplanInfo.setServerIp(configuration.getLocalHostAddress());
            dialplanInfo.setServerName(configuration.getLocalHostName());

            entityManager.persist(dialplanInfo);
        } catch (Exception ex) {
            log.error("Could not log the error to the database - {}", ex.getMessage());
        }

    }

}
