package com.amp.crm.db.entity.qualityAssurance;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.joda.time.LocalDateTime;

import javax.persistence.*;

@Entity
@Table(schema = "crm", name = "quality_assurance_log_histories")
public class QualityAssuranceLogHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "quality_assurance_log_history_pk", unique = true, nullable = false)
    private long qualityAssuranceLogHistoryPk;

    @Column(name = "create_time", updatable = false)
    private LocalDateTime createdTime;

    @Column(name = "update_time")
    private LocalDateTime updatedTime;

    @Column(name = "table_name", nullable = false)
    private String tableName;

    @Column(name = "column_name", nullable = false)
    private String columnName;

    @Column(name = "table_pk", nullable = false)
    private long tablePk;

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

    public long getQualityAssuranceLogHistoryPk() {
        return qualityAssuranceLogHistoryPk;
    }

    public void setQualityAssuranceLogHistoryPk(long qualityAssuranceLogHistoryPk) {
        this.qualityAssuranceLogHistoryPk = qualityAssuranceLogHistoryPk;
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

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    public long getTablePk() {
        return tablePk;
    }

    public void setTablePk(long tablePk) {
        this.tablePk = tablePk;
    }
}
