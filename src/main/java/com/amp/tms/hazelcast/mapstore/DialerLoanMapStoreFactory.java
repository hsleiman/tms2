/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.tms.hazelcast.mapstore;

import com.hazelcast.core.MapLoader;
import com.hazelcast.core.MapStoreFactory;
import static com.amp.tms.hazelcast.Configs.DIALER_LOAN_MAP_STORE_FACTORY_BEAN_NAME;
import com.amp.tms.hazelcast.entity.DialerLoan;
import java.util.Properties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * @author connorpetty
 */
@Component(DIALER_LOAN_MAP_STORE_FACTORY_BEAN_NAME)
public class DialerLoanMapStoreFactory implements MapStoreFactory<Long, DialerLoan> {

    @Autowired
    private DialerLoanRepository repository;

    @Override
    public MapLoader<Long, DialerLoan> newMapStore(String mapName, Properties properties) {
        String[] split = mapName.split(":");
        Long dialerPk = Long.valueOf(split[1]);
        return new DialerLoanMapStore(dialerPk, repository);
    }

}
