package com.amp.crm.db.entity.qualityAssurance;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.amp.crm.constants.QualityAssuranceQuestionType;
import org.joda.time.LocalDateTime;

import javax.persistence.*;

@Entity
@Table(schema = "sti", name = "quality_assurance_questions")
public class QualityAssuranceQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "quality_assurance_question_pk", unique = true, nullable = false)
    private long qualityAssuranceQuestionPk;

    @Column(name = "create_time", updatable = false)
    private LocalDateTime createdTime;

    @Column(name = "update_time")
    private LocalDateTime updatedTime;

    @Column(nullable = false)
    private boolean active = true;

    @Column(nullable = false)
    private String question;

    @Enumerated(EnumType.STRING)
    @Column(name = "quality_assurance_question_type", nullable = false)
    private QualityAssuranceQuestionType qualityAssuranceQuestionType;

    @Column(nullable = false)
    private String category;

    @Column(name = "required_field", nullable = false)
    private boolean requiredField;

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

    public long getQualityAssuranceQuestionPk() {
        return qualityAssuranceQuestionPk;
    }

    public void setQualityAssuranceQuestionPk(long qualityAssuranceQuestionPk) {
        this.qualityAssuranceQuestionPk = qualityAssuranceQuestionPk;
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

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public QualityAssuranceQuestionType getQualityAssuranceQuestionType() {
        return qualityAssuranceQuestionType;
    }

    public void setQualityAssuranceQuestionType(QualityAssuranceQuestionType qualityAssuranceQuestionType) {
        this.qualityAssuranceQuestionType = qualityAssuranceQuestionType;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public boolean isRequiredField() {
        return requiredField;
    }

    public void setRequiredField(boolean requiredField) {
        this.requiredField = requiredField;
    }
}
