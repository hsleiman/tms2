/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.tms.service.freeswitch.common;

import com.amp.tms.db.entity.freeswitch.TMSDialplan;
import com.amp.tms.db.hibernate.ApplicationContextProvider;
import com.amp.tms.freeswitch.pojo.DialplanVariable;
import com.amp.tms.service.DialplanService;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author hsleiman
 */
@Service
public class CommonService {

    private static final Logger log = LoggerFactory.getLogger(CommonService.class);

    @Autowired
    private DialplanService dialplanRepository;

    public TMSDialplan executeTMSFunction(TMSDialplan dp, DialplanVariable variable) {
        if (dp.getBean() != null && dp.getFunctionCall() != null) {
            log.info("Executing TMS pre-defined dialplan function: " + dp.getBean() + "." + dp.getFunctionCall());
            Method method = null;
            Object ruleRun = (Object) ApplicationContextProvider.getApplicationContext().getBean(dp.getBean().name());
            Method[] methods = ruleRun.getClass().getMethods();
            for (Method m : methods) {
                if (m.getName().equalsIgnoreCase(dp.getFunctionCall())) {
                    method = m;
                }
            }
            try {
                log.info("Checking Actions/Bridge for TMSDialplan [{}],[{}],[{}] it is set to one time use {}", dp.getKey().getTms_uuid(), dp.getKey().getContext(), dp.getKey().getOrderPower(), dp.getOnce());
                if (Objects.equals(dp.getOnce(), Boolean.FALSE)) {
                    log.info("Clearing Actions/Bridge for TMSDialplan [{}],[{}],[{}] because it was use once was set to FALSE.", dp.getKey().getTms_uuid(), dp.getKey().getContext(), dp.getKey().getOrderPower());
                    dp.setActions("");
                    dp.setBridges("");
                }
                dp = (TMSDialplan) method.invoke(ruleRun, variable, dp);
                dp.setXMLFromDialplan();
                dialplanRepository.updateTMSDialplan(dp);
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                log.info("Executed TMS pre-defined dialplan function: : " + dp.getBean() + "." + dp.getFunctionCall() + " -> " + ex.getMessage(), ex);
            }
        }
        return dp;
    }

}
