package com.amp.crm.db.entity.base.account;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.amp.crm.db.entity.superentity.SuperEntity;
import com.amp.crm.embeddable.BankDetails;
import java.util.Objects;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlTransient;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.hibernate.annotations.ForeignKey;

/**
 *
 * @author HS
 */
@NamedQueries({
    @NamedQuery(
            name = "BankAccount.LocateByPk",
            query = "SELECT s FROM BankAccount s WHERE s.pk = :pk"
    ),
    @NamedQuery(
            name = "BankAccount.GetCurrentBankAccount",
            query = "SELECT s FROM BankAccount s WHERE s.account.pk = :accountPK and s.current = TRUE"
    )
})
@Entity
@Table(schema = "crm")
@XmlAccessorType(XmlAccessType.FIELD)
public class BankAccount extends SuperEntity {

    @Embedded
    private BankDetails bankDetails;

    @XmlTransient
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_pk", referencedColumnName = "pk")
    @ForeignKey(name = "fk_sv_account_bank")
    private Account account;

    private Boolean current = Boolean.FALSE;

    public BankDetails getBankDetails() {
        if (bankDetails == null) {
            bankDetails = new BankDetails();
        }
        return bankDetails;
    }

    public void setBankDetails(BankDetails bankDetails) {
        this.bankDetails = bankDetails;
    }

    public Boolean isCurrent() {
        return current;
    }

    public void setCurrent(Boolean current) {
        this.current = current;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }
    
    

    public String dump() {
        return ToStringBuilder.reflectionToString(this);
    }
    
    

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 53 * hash + Objects.hashCode(this.bankDetails);
        hash = 53 * hash + Objects.hashCode(this.account);
        hash = 53 * hash + Objects.hashCode(this.current);
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
        final BankAccount other = (BankAccount) obj;
        if (!Objects.equals(this.bankDetails, other.bankDetails)) {
            return false;
        }
        if (!Objects.equals(this.account, other.account)) {
            return false;
        }
        if (!Objects.equals(this.current, other.current)) {
            return false;
        }
        return true;
    }

}
