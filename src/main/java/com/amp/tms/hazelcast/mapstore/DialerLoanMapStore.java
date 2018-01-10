/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.tms.hazelcast.mapstore;

import com.hazelcast.core.MapStore;
import com.hazelcast.core.PostProcessingMapStore;
import com.amp.tms.hazelcast.entity.DialerLoan;
import java.util.Collection;
import java.util.Map;

/**
 *
 * 
 */
public class DialerLoanMapStore implements MapStore<Long, DialerLoan>, PostProcessingMapStore {

    private final Long dialerPk;
    private final DialerLoanRepository repository;

    public DialerLoanMapStore(Long dialerPk, DialerLoanRepository repository) {
        this.dialerPk = dialerPk;
        this.repository = repository;
    }

    @Override
    public void store(Long loanPk, DialerLoan value) {
        repository.saveDialerLoan(dialerPk, loanPk, value);
    }

    @Override
    public void storeAll(Map<Long, DialerLoan> map) {
        repository.saveDialerLoans(dialerPk, map);
    }

    @Override
    public void delete(Long key) {
    }

    @Override
    public void deleteAll(Collection<Long> keys) {
    }

    @Override
    public DialerLoan load(Long loanPk) {
        return repository.getDialerLoan(dialerPk, loanPk);
    }

    @Override
    public Map<Long, DialerLoan> loadAll(Collection<Long> keys) {
        return repository.getDialerLoans(dialerPk, keys);
    }

    @Override
    public Iterable<Long> loadAllKeys() {
        return null;
    }

}
