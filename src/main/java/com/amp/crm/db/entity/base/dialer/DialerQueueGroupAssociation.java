/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.crm.db.entity.base.dialer;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.amp.crm.db.entity.agent.DialerGroup;
import com.amp.crm.embeddable.DialerQueueGroupPk;
import com.amp.crm.embeddable.WeightedPriority;
import javax.persistence.Embedded;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MapsId;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlTransient;
import org.hibernate.annotations.ForeignKey;

/**
 *
 * @author raine.cabal
 */
@NamedQueries({
    @NamedQuery(
            name = "DialerQueueGroupAssociation.LocateByDialerQueue",
            query = "SELECT s FROM DialerQueueGroupAssociation s where s.dialerQueue = :dialerQueue"
    ),
    @NamedQuery(
            name = "DialerQueueGroupAssociation.LocateByDialerGroup",
            query = "SELECT s FROM DialerQueueGroupAssociation s where s.dialerGroup = :dialerGroup"
    ),
    @NamedQuery(
            name = "DialerQueueGroupAssociation.LocateBySecondaryDialerGroup",
            query = "SELECT s FROM DialerQueueGroupAssociation s where s.secondaryGroup = :secondaryGroup"
    ),
    @NamedQuery(
            name = "DialerQueueGroupAssociation.LocateByPrimaryAndSecondaryDialerGroup",
            query = "SELECT s FROM DialerQueueGroupAssociation s where s.dialerGroup = :dialerGroup OR s.secondaryGroup = :dialerGroup"
    )
})
@Entity
@Table(schema = "sti")
public class DialerQueueGroupAssociation {

    @EmbeddedId
    private DialerQueueGroupPk dialerQueueGroupPk = new DialerQueueGroupPk();

    @XmlTransient
    @JsonIgnore
    @OneToOne
    @MapsId("dialerQueuePk")
    @JoinColumn(name = "dialer_queue_pk", referencedColumnName = "pk")
//        @JoinColumns(
//            {@JoinColumn(name = "dialer_queue_pk", referencedColumnName = "pk", insertable = false, updatable = false),
//             @JoinColumn(name = "dialer_queue_type", referencedColumnName = "dialer_queue_type", insertable = false, updatable = false)
//            })
    @ForeignKey(name = "fk_dq_group_association_queue")
    private DialerQueue dialerQueue;
    
    @XmlTransient
    @JsonIgnore
    @ManyToOne
    @MapsId("dialerGroupPk")
    @JoinColumn(name = "dialer_group_pk", referencedColumnName = "pk")
    @ForeignKey(name = "fk_dq_group_association_group")
    private DialerGroup dialerGroup;
    
    @XmlTransient
    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "secondary_dialer_group_pk", referencedColumnName = "pk")
    @ForeignKey(name = "fk_dq_group_secondary_association_group")
    private DialerGroup secondaryGroup;
     
    @Embedded
    private WeightedPriority weightedPriority;

    public DialerQueueGroupPk getDialerQueueGroupPk() {
        return dialerQueueGroupPk;
    }

    public void setDialerQueueGroupPk(DialerQueueGroupPk dialerQueueGroupPk) {
        this.dialerQueueGroupPk = dialerQueueGroupPk;
    }

    public DialerQueue getDialerQueue() {
        return dialerQueue;
    }

    public void setDialerQueue(DialerQueue dialerQueue) {
        this.dialerQueue = dialerQueue;
    }

    public DialerGroup getDialerGroup() {
        return dialerGroup;
    }

    public void setDialerGroup(DialerGroup dialerGroup) {
        this.dialerGroup = dialerGroup;
    }

    public DialerGroup getSecondaryGroup() {
        return secondaryGroup;
    }

    public void setSecondaryGroup(DialerGroup secondaryGroup) {
        this.secondaryGroup = secondaryGroup;
    }

    public WeightedPriority getWeightedPriority() {
        return weightedPriority;
    }

    public void setWeightedPriority(WeightedPriority weightedPriority) {
        this.weightedPriority = weightedPriority;
    }

}
