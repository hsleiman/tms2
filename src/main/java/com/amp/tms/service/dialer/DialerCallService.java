/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.tms.service.dialer;

import com.hazelcast.core.IMap;
import com.hazelcast.mapreduce.aggregation.Aggregation;
import com.hazelcast.mapreduce.aggregation.Aggregations;
import com.hazelcast.mapreduce.aggregation.Supplier;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.DataSerializable;
import com.hazelcast.query.Predicate;
import com.objectbrains.hcms.hazelcast.HazelcastService;
import com.amp.tms.hazelcast.AbstractEntryProcessor;
import com.amp.tms.hazelcast.Configs;
import com.amp.tms.hazelcast.entity.DialerCall;
import com.amp.tms.websocket.message.outbound.PhoneToType;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author connorpetty
 */
@Service
public class DialerCallService {

    @Autowired
    private DialerStatsService statsService;

    @Autowired
    private HazelcastService hazelcastService;

    private IMap<String, DialerCall> dialerCallMap;

    @PostConstruct
    private void initialize() {
        dialerCallMap = hazelcastService.getMap(Configs.DIALER_CALL_MAP);
    }

    public void lock(String callUUID) {
        dialerCallMap.lock(callUUID);
    }

    public void unlock(String callUUID) {
        dialerCallMap.unlock(callUUID);
    }

    public DialerCall getDialerCall(String callUUID) {
        return dialerCallMap.get(callUUID);
    }

    public void createCall(String callUUID, Long queuePK, long dialerPk, LoanNumber loanNumber, PhoneToType callInfo) {
        DialerCall dialerCall = new DialerCall();
        dialerCall.copyFrom(loanNumber);
        dialerCall.setCallUUID(callUUID);
        dialerCall.setQueuePk(queuePK);
        dialerCall.setDialerPk(dialerPk);
        dialerCall.setState(DialerCall.State.PENDING);
        dialerCall.setCallInfo(callInfo);
        save(dialerCall);
        statsService.updateStateCount(dialerPk, null, dialerCall.getState());
    }

    public void save(DialerCall call) {
        dialerCallMap.put(call.getCallUUID(), call);
    }

    public void evictCalls(long dialerPk) {
        Set<String> callsToEvict = dialerCallMap.keySet(new CallsForDialerPredicate(dialerPk));
        for (String callToEvict : callsToEvict) {
            dialerCallMap.evict(callToEvict);
        }
    }

    public long getFailedCallCount(long dialerPk, long loanPk, long dispositionCodeId) {
        return dialerCallMap.keySet(new FailedCallPredicate(dialerPk, loanPk, dispositionCodeId)).size();
    }

    public static class CallsForDialerPredicate implements Predicate<String, DialerCall>, DataSerializable {

        private long dialerPk;

        private CallsForDialerPredicate() {
        }

        public CallsForDialerPredicate(long dialerPk) {
            this.dialerPk = dialerPk;
        }

        @Override
        public boolean apply(Map.Entry<String, DialerCall> mapEntry) {
            DialerCall call = mapEntry.getValue();
            return call.getDialerPk().equals(dialerPk);
        }

        @Override
        public void writeData(ObjectDataOutput out) throws IOException {
            out.writeLong(dialerPk);
        }

        @Override
        public void readData(ObjectDataInput in) throws IOException {
            dialerPk = in.readLong();
        }

    }

    public static class FailedCallPredicate implements Predicate<String, DialerCall>, DataSerializable {

        private long dialerPk;
        private long loanPk;
        private long dispositionCodeId;

        private FailedCallPredicate() {
        }

        public FailedCallPredicate(long dialerPk, long loanPk, long dispositionCodeId) {
            this.dialerPk = dialerPk;
            this.loanPk = loanPk;
            this.dispositionCodeId = dispositionCodeId;
        }

        @Override
        public boolean apply(Map.Entry<String, DialerCall> mapEntry) {
            DialerCall call = mapEntry.getValue();
            return call.getDialerPk() == dialerPk
                    && call.getLoanPk() == loanPk
                    && call.getDispositionCodeId() == dispositionCodeId;
        }

        @Override
        public void writeData(ObjectDataOutput out) throws IOException {
            out.writeLong(dialerPk);
            out.writeLong(loanPk);
            out.writeLong(dispositionCodeId);
        }

        @Override
        public void readData(ObjectDataInput in) throws IOException {
            dialerPk = in.readLong();
            loanPk = in.readLong();
            dispositionCodeId = in.readLong();
        }

    }

}
