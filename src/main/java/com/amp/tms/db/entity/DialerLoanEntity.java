/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.tms.db.entity;

import com.hazelcast.core.PartitionAware;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.DataSerializable;
import com.amp.tms.hazelcast.entity.DialerLoan;
import java.io.IOException;
import java.io.Serializable;
import java.util.Objects;
import java.util.Set;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MapsId;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.PrePersist;
import javax.persistence.Table;
import org.joda.time.LocalDateTime;

/**
 *
 * @author connorpetty
 */
@Entity(name = "DialerLoan")
@Table(schema = "sti")
public class DialerLoanEntity extends DialerLoan {

    @EmbeddedId
    private Pk pk;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("statsPk")
    @JoinColumn(name = "stats_pk", referencedColumnName = "pk")
    private DialerStatsEntity dialerStats;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "queue_pk", referencedColumnName = "pk")
    private DialerQueueTms dialerQueue;

    @OneToMany(mappedBy = "dialerLoan")
    private Set<DialerCallEntity> dialerCalls;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "current_call_uuid", referencedColumnName = "call_uuid")
    private DialerCallEntity currentCall;

    private LocalDateTime createdDate;

    @PrePersist
    protected void onCreate() {
        createdDate = new LocalDateTime();
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public DialerStatsEntity getDialerStats() {
        return dialerStats;
    }

    public void setDialerStats(DialerStatsEntity dialerStats) {
        this.dialerStats = dialerStats;
    }

    public DialerQueueTms getDialerQueue() {
        return dialerQueue;
    }

    public void setDialerQueue(DialerQueueTms dialerQueue) {
        this.dialerQueue = dialerQueue;
    }

    public Pk getPk() {
        return pk;
    }

    public void setPk(Pk pk) {
        this.pk = pk;
    }

    public Set<DialerCallEntity> getDialerCalls() {
        return dialerCalls;
    }

    public void setDialerCalls(Set<DialerCallEntity> dialerCalls) {
        this.dialerCalls = dialerCalls;
    }

    @Override
    public Long getStatsPk() {
        return pk.getStatsPk();
    }

    @Override
    public Long getLoanPk() {
        return pk.getLoanPk();
    }

    public DialerCallEntity getCurrentCall() {
        return currentCall;
    }

    public void setCurrentCall(DialerCallEntity currentCall) {
        this.currentCall = currentCall;
    }

    @Embeddable
    public static class Pk implements DataSerializable, PartitionAware, Serializable {

        @Column(name = "stats_pk")
        private Long statsPk;

        @Column(name = "loan_pk")
        private Long loanPk;

        public Pk() {
        }

        public Pk(Long statsPk, Long loanPk) {
            this.statsPk = statsPk;
            this.loanPk = loanPk;
        }

        public Long getStatsPk() {
            return statsPk;
        }

        public void setStatsPk(Long statsPk) {
            this.statsPk = statsPk;
        }

        public Long getLoanPk() {
            return loanPk;
        }

        public void setLoanPk(Long loanPk) {
            this.loanPk = loanPk;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 11 * hash + Objects.hashCode(this.statsPk);
            hash = 11 * hash + Objects.hashCode(this.loanPk);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final Pk other = (Pk) obj;
            if (!Objects.equals(this.statsPk, other.statsPk)) {
                return false;
            }
            if (!Objects.equals(this.loanPk, other.loanPk)) {
                return false;
            }
            return true;
        }

        @Override
        public void writeData(ObjectDataOutput out) throws IOException {
            out.writeObject(statsPk);
            out.writeObject(loanPk);
        }

        @Override
        public void readData(ObjectDataInput in) throws IOException {
            statsPk = in.readObject();
            loanPk = in.readObject();
        }

        @Override
        public Object getPartitionKey() {
            return statsPk;
        }

    }

}
