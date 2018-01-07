package com.amp.crm.db.entity.superentity;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import org.joda.time.LocalDateTime;

/**
 *
 * @author HS
 *
 * Add @GenericGenerator on the entity level to specify params such as sequence name, initial value
 */
@MappedSuperclass
public abstract class SuperEntityCustom extends AbstractSuperEntity {

    @Id
    @GeneratedValue(generator = "customIdGenerator")
//    @GenericGenerator(name = "customIdGenerator", strategy = "com.objectbrains.svc.db.hibernate.CustomIdGenerator",
//            parameters = {
//                @Parameter(name = SequenceStyleGenerator.SEQUENCE_PARAM, value = "sv_custom_pk_seq"),
//                @Parameter(name = SequenceStyleGenerator.CONFIG_PREFER_SEQUENCE_PER_ENTITY, value = "true"),
//                @Parameter(name = SequenceStyleGenerator.CONFIG_SEQUENCE_PER_ENTITY_SUFFIX, value = "_pk_seq"),
//                @Parameter(name = SequenceStyleGenerator.SCHEMA, value = "svc")
//            })
    @Column(name = "pk", unique = true, nullable = false)
    private long pk;
    
    private LocalDateTime createdTime;
    private LocalDateTime updatedTime;
    
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
    private void onUpdate(){
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
