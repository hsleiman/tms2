package com.amp.crm.db.entity.superentity;

import org.joda.time.LocalDateTime;

/**
 *
 * @author hsleiman
 */
public interface SuperEntityInterface {
    
    public long getPk();
    public void setPk(long pk);
    @Override
    public String toString();
    
    public LocalDateTime getUpdatedTime();
    public void setUpdatedTime(LocalDateTime updatedTime);

}