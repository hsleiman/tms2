/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.amp.crm.hazelcast;

import com.hazelcast.core.IMap;
import com.hazelcast.core.IQueue;
import com.objectbrains.hcms.hazelcast.HazelcastService;
import com.amp.crm.constants.OutboundRecordStatus;
import com.amp.crm.pojo.DialerQueueAccountDetails;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.context.annotation.Configuration;

/**
 *
 * @author priyankanamburu
 */
@Configuration
public class Config implements BeanFactoryAware  {
    private BeanFactory beanFactory;
    public static final HazelcastService.QueueKey<Long> REFINANCE_INITIATED_QUEUE= new HazelcastService.QueueKey<>("refinancePkQueue");
    public static final HazelcastService.QueueKey<Long> REFINANCE_CANCELLED_QUEUE= new HazelcastService.QueueKey<>("refinanceCancelledQueue");
    
    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }
    
    // Hazelcast map to hold account details for a given outbound dialer queue record. 
    public static IQueue<DialerQueueAccountDetails> getDialerAccountDetailQueue(HazelcastService hazelcastService, long dqPk) {
        return hazelcastService.getQueue("dialerQueueAccountDetails:" + dqPk);
    }
    
    // Hazelcast map to keep track of outbound dialer record status (whether or not it's done adding accountDetails to "dialerQueueAccountDetails" queue).
    public static IMap<Long, OutboundRecordStatus> getOutboundDialerStatus(HazelcastService hazelcastService, long dqPk) {
        return hazelcastService.getMap("outboundDialerStatus:" + dqPk);
    }
    
}
