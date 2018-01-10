package com.amp.crm.db.entity.superentity;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.hibernate.id.enhanced.SequenceStyleGenerator;
import org.joda.time.LocalDateTime;

@MappedSuperclass
public abstract class SuperEntitySequence extends AbstractSuperEntity {
 
    @Id
    @GeneratedValue(generator = "seqGenerator")
    @GenericGenerator(name = "seqGenerator", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
            parameters = {
                @Parameter(name = SequenceStyleGenerator.CONFIG_PREFER_SEQUENCE_PER_ENTITY, value = "true"),
                @Parameter(name = SequenceStyleGenerator.CONFIG_SEQUENCE_PER_ENTITY_SUFFIX, value = "_pk_seq"),
                @Parameter(name = SequenceStyleGenerator.SCHEMA, value = "crm")
            })
    @Column(name = "pk", unique = true, nullable = false)
    private long pk;
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
