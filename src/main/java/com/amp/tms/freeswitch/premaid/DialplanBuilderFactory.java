/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.tms.freeswitch.premaid;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.stereotype.Service;

/**
 *
 * @author hsleiman
 */
@Service
public class DialplanBuilderFactory implements BeanFactoryAware {

    private AutowireCapableBeanFactory beanFactory;

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = (AutowireCapableBeanFactory) beanFactory;
    }
//
//    public DialplanBuilder createIncomingCustomerService(DialplanVariable variable) {
//        DialplanBuilder builder = new IncomingCustomerService(variable);
//        beanFactory.autowireBean(builder);
//        return builder;
//    }
//    
//    public DialplanBuilder createIncomingDialerOrder(DialplanVariable variable, AgentIncomingDistributionOrder aido) {
//        DialplanBuilder builder = new IncomingDialerOrder(variable, aido);
//        beanFactory.autowireBean(builder);
//        return builder;
//    }
//    
//    public DialplanBuilder createIncomingIVR(DialplanVariable variable) {
//        DialplanBuilder builder = new IncomingIVR(variable);
//        beanFactory.autowireBean(builder);
//        return builder;
//    }
//    
//    public DialplanBuilder createIncomingRSBC(DialplanVariable variable) {
//        DialplanBuilder builder = new IncomingRSBC(variable);
//        beanFactory.autowireBean(builder);
//        return builder;
//    }
//    
//    public DialplanBuilder createAgentToAgent(DialplanVariable variable) {
//        DialplanBuilder builder = new AgentToAgent(variable);
//        beanFactory.autowireBean(builder);
//        return builder;
//    }
//
//    public DialplanBuilder createAutoDial(DialerConnectCallPojo variable) {
//        DialplanBuilder builder = new AutoDial(variable);
//        beanFactory.autowireBean(builder);
//        return builder;
//    }
//    
//    public DialplanBuilder createManualDial(DialplanVariable variable) {
//        DialplanBuilder builder = new ManualDial(variable);
//        beanFactory.autowireBean(builder);
//        return builder;
//    }
//    
//    public DialplanBuilder createOutboundSBC(DialplanVariable variable) {
//        DialplanBuilder builder = new OutboundSBC(variable);
//        beanFactory.autowireBean(builder);
//        return builder;
//    }
}
