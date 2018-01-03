package com.objectbrains.sti.db.entity.qualityAssurance;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.joda.time.LocalDateTime;

import javax.persistence.*;

@Entity
@Table(schema = "sti", name = "quality_assurance_form_question_relations")
public class QualityAssuranceFormQuestionRelation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "quality_assurance_form_question_relation_pk", unique = true, nullable = false)
    private long qualityAssuranceFormQuestionRelationPk;

    @Column(name = "create_time", updatable = false)
    private LocalDateTime createdTime;

    @Column(name = "update_time")
    private LocalDateTime updatedTime;


    @ManyToOne
    @JoinColumn(name = "quality_assurance_form_pk", nullable = false)
    private QualityAssuranceForm qualityAssuranceForm;


    @ManyToOne
    @JoinColumn(name = "quality_assurance_question_pk", nullable = false)
    private QualityAssuranceQuestion qualityAssuranceQuestion;


    @Column(name = "credit_value", nullable = false)
    private int creditValue;


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

    public long getQualityAssuranceFormQuestionRelationPk() {
        return qualityAssuranceFormQuestionRelationPk;
    }

    public void setQualityAssuranceFormQuestionRelationPk(long qualityAssuranceFormQuestionRelationPk) {
        this.qualityAssuranceFormQuestionRelationPk = qualityAssuranceFormQuestionRelationPk;
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

    public QualityAssuranceForm getQualityAssuranceForm() {
        return qualityAssuranceForm;
    }

    public void setQualityAssuranceForm(QualityAssuranceForm qualityAssuranceForm) {
        this.qualityAssuranceForm = qualityAssuranceForm;
    }

    public QualityAssuranceQuestion getQualityAssuranceQuestion() {
        return qualityAssuranceQuestion;
    }

    public void setQualityAssuranceQuestion(QualityAssuranceQuestion qualityAssuranceQuestion) {
        this.qualityAssuranceQuestion = qualityAssuranceQuestion;
    }

    public int getCreditValue() {
        return creditValue;
    }

    public void setCreditValue(int creditValue) {
        this.creditValue = creditValue;
    }
}
