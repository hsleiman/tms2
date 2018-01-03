/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.objectbrains.sti.db.entity.qaform;

import com.objectbrains.sti.embeddable.QuestionChoice;
import com.objectbrains.sti.constants.ChoicesDisplayMode;
import com.objectbrains.sti.constants.QuestionType;
import com.objectbrains.sti.db.entity.superentity.SuperEntity;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.NamedNativeQuery;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OrderColumn;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import org.hibernate.envers.AuditTable;
import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;

/**
 *
 * @author raine.cabal
 */
@NamedQueries({
    @NamedQuery(
            name = "QuestionChoiceSet.GetAll",
            query = "SELECT q FROM QuestionChoiceSet q"),
    @NamedQuery(
            name = "QuestionChoiceSet.GetAllByCategory",
            query = "SELECT q FROM QuestionChoiceSet q WHERE q.questionCategory = :questionCategory"),
    @NamedQuery(
            name = "QuestionChoiceSet.LocateByName",
            query = "SELECT q FROM QuestionChoiceSet q WHERE q.name = :name"),
    @NamedQuery(
            name = "QuestionChoiceSet.LocateQuestionChoiceByValue",
            query = "SELECT new com.objectbrains.sti.embeddable.QuestionChoice(c.value, c.credit) FROM QuestionChoiceSet q join q.choices c WHERE lower(trim(c.value)) = lower(trim(:value))"
    )
})
@Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
@AuditTable(value = "question_choice_set_history", schema = "sti")
@Entity
@Table(schema = "sti")
public class QuestionChoiceSet extends SuperEntity {

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(schema = "sti", name = "question_choices", 
            joinColumns = @JoinColumn(name = "choice_set_pk", referencedColumnName = "pk"),
            uniqueConstraints = @UniqueConstraint(columnNames = {"choice_set_pk", "value"}))
    @OrderColumn(name = "order_index")
    private List<QuestionChoice> choices = new ArrayList<>();
    
    @Enumerated(EnumType.STRING)
    private QuestionType questionType;
    private ChoicesDisplayMode choicesDisplayMode;
    private String questionCategory;
    @Column(unique = true, nullable = false)
    private String name;
    
    public List<QuestionChoice> getChoices() {
        return choices;
    }

    public void setChoices(List<QuestionChoice> choices) {
        this.choices = choices;
    }

    public QuestionType getQuestionType() {
        return questionType;
    }

    public void setQuestionType(QuestionType questionType) {
        this.questionType = questionType;
    }

    public ChoicesDisplayMode getChoicesDisplayMode() {
        return choicesDisplayMode;
    }

    public void setChoicesDisplayMode(ChoicesDisplayMode choicesDisplayMode) {
        this.choicesDisplayMode = choicesDisplayMode;
    }

    public String getQuestionCategory() {
        return questionCategory;
    }

    public void setQuestionCategory(String questionCategory) {
        this.questionCategory = questionCategory;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 43 * hash + Objects.hashCode(this.choices);
        hash = 43 * hash + Objects.hashCode(this.questionType);
        hash = 43 * hash + Objects.hashCode(this.choicesDisplayMode);
        hash = 43 * hash + Objects.hashCode(this.questionCategory);
        hash = 43 * hash + Objects.hashCode(this.name);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final QuestionChoiceSet other = (QuestionChoiceSet) obj;
        if (!Objects.equals(this.choices, other.choices)) {
            return false;
        }
        if (this.questionType != other.questionType) {
            return false;
        }
        if (this.choicesDisplayMode != other.choicesDisplayMode) {
            return false;
        }
        if (!Objects.equals(this.questionCategory, other.questionCategory)) {
            return false;
        }
        if (!Objects.equals(this.name, other.name)) {
            return false;
        }
        return true;
    }

    
    

}
