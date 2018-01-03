package com.objectbrains.sti.db.entity.superentity;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import org.joda.time.LocalDateTime;

/**
 *
 * @author hsleiman
 */
@MappedSuperclass
public abstract class SuperEntity extends AbstractSuperEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pk", unique = true, nullable = false)
    protected long pk;
    protected LocalDateTime createdTime;
    protected LocalDateTime updatedTime;

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

    public String originalToString() {
        return super.toString();
    }

    @Override
    public LocalDateTime getUpdatedTime() {
        return updatedTime;
    }

    @Override
    public void setUpdatedTime(LocalDateTime rowUpdatedTimestamp) {
        this.updatedTime = rowUpdatedTimestamp;
    }

    @PreUpdate
    private void onUpdate() {
        this.updatedTime = new LocalDateTime();
    }

    @PrePersist
    private void onCreate() {
        this.createdTime = LocalDateTime.now();
    }

    public LocalDateTime getCreatedTime() {
        return createdTime;
    }
    
}
