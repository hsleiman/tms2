package com.amp.crm.db.entity.qualityAssurance;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.joda.time.LocalDateTime;

import javax.persistence.*;

@Entity
@Table(schema = "sti", name = "quality_assurance_forms",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"form_name"})
        })
public class QualityAssuranceForm {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "quality_assurance_form_pk", unique = true, nullable = false)
    private long qualityAssuranceFormPk;

    @Column(name = "create_time", updatable = false)
    private LocalDateTime createdTime;

    @Column(name = "update_time")
    private LocalDateTime updatedTime;

    @Column(name = "form_name", nullable = false)
    private String formName;

    @ManyToOne
    @JoinColumn(name = "quality_assurance_category_pk", nullable = false)
    private QualityAssuranceCategory qualityAssuranceCategory;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "active", nullable = false)
    private boolean active = true;

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

    public long getQualityAssuranceFormPk() {
        return qualityAssuranceFormPk;
    }

    public void setQualityAssuranceFormPk(long qualityAssuranceFormPk) {
        this.qualityAssuranceFormPk = qualityAssuranceFormPk;
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

    public String getFormName() {
        return formName;
    }

    public void setFormName(String formName) {
        this.formName = formName;
    }

    public QualityAssuranceCategory getQualityAssuranceCategory() {
        return qualityAssuranceCategory;
    }

    public void setQualityAssuranceCategory(QualityAssuranceCategory qualityAssuranceCategory) {
        this.qualityAssuranceCategory = qualityAssuranceCategory;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
