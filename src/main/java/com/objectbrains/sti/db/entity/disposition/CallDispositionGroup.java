/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.objectbrains.sti.db.entity.disposition;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.objectbrains.sti.db.entity.base.dialer.DialerQueue;
import com.objectbrains.sti.db.entity.superentity.SuperEntity;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.OrderColumn;
import javax.persistence.PrePersist;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlTransient;
import org.joda.time.LocalDateTime;


@NamedQueries({
  @NamedQuery(
            name = "CallDispositionGroup.GetAllCallDispositionGroups",
            query = "SELECT c FROM CallDispositionGroup c"),
    @NamedQuery(
            name = "CallDispositionGroup.GetCallDispositionGroupByName",
        query = "SELECT c FROM CallDispositionGroup c WHERE c.name = :name"),
    @NamedQuery(
        name = "CallDispositionGroup.GetAllGroupsForDispositionCode",
        query = "SELECT g FROM CallDispositionGroup g join g.callDispositionCodes c WHERE c.dispositionId = :dispositionId"    
    )
})
@Entity
@Table(schema = "sti")
public class CallDispositionGroup extends SuperEntity {

    @Column(nullable = false, unique = true)
    private String name;
    private String description;
    private String createdBy;

//    @XmlTransient
//    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "callDispositionGroup", orphanRemoval = true)
//    @OrderColumn(name = "order_index")
//    private List<CallDispositionGroupCode> callDispositionGroupCodes = new ArrayList<>();

    @XmlTransient
    @JsonIgnore
    @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	@JoinTable(name = "call_disposition_group_codes", schema = "sti",
		joinColumns = { @JoinColumn(name = "disposition_group_pk", referencedColumnName = "pk") }, 
		inverseJoinColumns = { @JoinColumn(name = "disposition_code_id", referencedColumnName = "dispositionId") }
            // removed unique constraints because it is restricting hibernate update on ordered list 
       // uniqueConstraints = @UniqueConstraint(name = "uk_disposition_group_code", columnNames = {"disposition_group_pk", "disposition_code_id"})
    )
    @OrderColumn(name = "order_index")
	private List<CallDispositionCode> callDispositionCodes = new ArrayList<>();
    
    @XmlTransient
    @JsonIgnore
    @OneToMany(fetch = FetchType.EAGER, mappedBy = "callDispositionGroup")
    private Set<DialerQueue> dialerQueues = new HashSet<>();

    public List<CallDispositionCode> getCallDispositionCodes() {
        return callDispositionCodes;
    }

    public void setCallDispositionCodes(List<CallDispositionCode> dispositions) {
        this.callDispositionCodes = dispositions;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public Set<DialerQueue> getDialerQueues() {
        return dialerQueues;
    }

    public void setDialerQueues(Set<DialerQueue> dialerQueues) {
        this.dialerQueues = dialerQueues;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 79 * hash + Objects.hashCode(this.name);
        hash = 79 * hash + Objects.hashCode(this.description);
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
        final CallDispositionGroup other = (CallDispositionGroup) obj;
        if (!Objects.equals(this.name, other.name)) {
            return false;
        }
        if (!Objects.equals(this.description, other.description)) {
            return false;
        }
        return true;
    }

}
