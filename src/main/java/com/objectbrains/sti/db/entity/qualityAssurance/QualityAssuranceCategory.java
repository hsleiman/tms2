package com.objectbrains.sti.db.entity.qualityAssurance;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.joda.time.LocalDateTime;

import javax.persistence.*;

@Entity
@Table(schema = "sti", name = "quality_assurance_categories")
public class QualityAssuranceCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "quality_assurance_category_pk", unique = true, nullable = false)
    private long qualityAssuranceCategoryPk;

    @Column(name = "create_time", updatable = false)
    private LocalDateTime createdTime;

    @Column(name = "update_time")
    private LocalDateTime updatedTime;

    @Column(name = "active", nullable = false)
    private boolean active = true;

    @Column(name = "category_name", nullable = false)
    private String categoryName;

    @JsonIgnore
    @PrePersist
    protected void onCreate() {
        if (createdTime == null) {
            createdTime = new LocalDateTime();
        }
    }

    @JsonIgnore
    @PreUpdate
    public void preUpdate() {
        updatedTime = new LocalDateTime();
    }

    @Override
    public String toString() {
        try {
            return new ObjectMapper().writeValueAsString(this);
        } catch (JsonProcessingException e) {
            return String.valueOf(this);
        }
    }

    public long getQualityAssuranceCategoryPk() {
        return qualityAssuranceCategoryPk;
    }

    public void setQualityAssuranceCategoryPk(long qualityAssuranceCategoryPk) {
        this.qualityAssuranceCategoryPk = qualityAssuranceCategoryPk;
    }

    public LocalDateTime getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(LocalDateTime createdTime) {
        this.createdTime = createdTime;
    }

    public LocalDateTime getUpdatedTime() {
        return updatedTime;
    }

    public void setUpdatedTime(LocalDateTime updatedTime) {
        this.updatedTime = updatedTime;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }
}
