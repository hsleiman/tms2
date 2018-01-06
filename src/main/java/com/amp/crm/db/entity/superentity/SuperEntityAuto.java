package com.amp.crm.db.entity.superentity;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.PreUpdate;
import org.joda.time.LocalDateTime;

/**
 *
 * @author hsleiman
 */
@MappedSuperclass
public abstract class SuperEntityAuto implements SuperEntityInterface{
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "pk", unique = true, nullable = false)
    protected long pk;

    protected LocalDateTime rowUpdatedTimestamp;
    @Override
    public long getPk() {
        return pk;
    }

    @Override
    public void setPk(long pk) {
        this.pk = pk;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + " [ pk = " + pk + " ]";
    }

    @Override
    public LocalDateTime getUpdatedTime() {
        return rowUpdatedTimestamp;
    }

    @Override
    public void setUpdatedTime(LocalDateTime rowUpdatedTimestamp) {
        this.rowUpdatedTimestamp = rowUpdatedTimestamp;
    }

    @PreUpdate
    private void onUpdate(){
        this.rowUpdatedTimestamp = new LocalDateTime();
    }
    
}
