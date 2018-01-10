package com.amp.crm.db.entity.base.account;
// Generated Jun 13, 2014 8:06:43 AM by Hibernate Tools 3.6.0

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.amp.crm.db.entity.agent.Agent;
import com.amp.crm.db.entity.base.CallResponseAction;
import com.amp.crm.db.entity.base.WorkQueue;
import com.amp.crm.db.entity.base.customer.Customer;
import com.amp.crm.db.entity.base.dialer.InboundDialerQueue;
import com.amp.crm.db.entity.base.dialer.OutboundDialerQueue;
import com.amp.crm.db.entity.base.payment.Payment;
import com.amp.crm.db.entity.base.payment.ach.AchPayment;
import com.amp.crm.db.entity.base.schedule.AccountSchedule;
import com.amp.crm.db.entity.superentity.SuperEntityCustom;
import com.amp.crm.embeddable.AccountData;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import javax.persistence.CascadeType;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlTransient;
import org.hibernate.annotations.ForeignKey;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Sort;
import org.hibernate.annotations.SortType;
import org.hibernate.id.enhanced.SequenceStyleGenerator;
import org.joda.time.LocalDateTime;

/**
 * @author hsleiman
 */
@NamedQueries({})
@Entity
@GenericGenerator(name = "customIdGenerator", strategy = "com.objectbrains.sti.db.hibernate.CustomIdGenerator",
        parameters = {
            @Parameter(name = SequenceStyleGenerator.SEQUENCE_PARAM, value = "account_pk_seq"),
            @Parameter(name = SequenceStyleGenerator.SCHEMA, value = "crm"),
            @Parameter(name = SequenceStyleGenerator.INITIAL_PARAM, value = "1")})
@Table(schema = "crm")
@Inheritance(strategy = InheritanceType.JOINED)
@XmlAccessorType(XmlAccessType.FIELD)
public class Account extends SuperEntityCustom {

    @Embedded
    private AccountData accountData;

    @JsonIgnore
    @XmlTransient
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "account")
    private Set<Customer> customers = new HashSet<>(0);

    @JsonIgnore
    @XmlTransient
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "account")
    private Set<DebtAccount> debtAccounts = new HashSet<>(0);

    @JsonIgnore
    @XmlTransient
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "account")
    private Set<BankAccount> bankAccounts = new HashSet<>(0);

    @JsonIgnore
    @XmlTransient
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "account")
    private Set<AchPayment> achPayments = new HashSet<>(0);

    @JsonIgnore
    @XmlTransient
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "account")
    @Sort(type = SortType.NATURAL)
    private SortedSet<Payment> payments = new TreeSet<>();

    @JsonIgnore
    @XmlTransient
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "account")
    private Set<AccountSchedule> accountSchedule = new HashSet<>(0);

    @JsonIgnore
    @XmlTransient
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "agent_pk", referencedColumnName = "pk")
    @ForeignKey(name = "fk_account_agent")
    private Agent agentAssignedToAccount;

    @JsonIgnore
    @XmlTransient
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dialer_queue_inbound_pk", referencedColumnName = "pk")
    @ForeignKey(name = "fk_loan_inbound_dialer_queue")
    private InboundDialerQueue inboundDialerQueue;

    @JsonIgnore
    @XmlTransient
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dialer_queue_outbound_pk", referencedColumnName = "pk")
    @ForeignKey(name = "fk_loan_outbound_dialer_queue")
    private OutboundDialerQueue outboundDialerQueue;

    @JsonIgnore
    @XmlTransient
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "account", cascade = CascadeType.ALL)
    private Set<CallResponseAction> callResponseActions = new HashSet<>(0);

    @ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinTable(name = "Account_Work_Queue", joinColumns = {
        @JoinColumn(name = "accountPk")},
            inverseJoinColumns = {
                @JoinColumn(name = "workQueuePk")})
    private Set<WorkQueue> workQueues;

    public Agent getAgentAssignedToAccount() {
        return agentAssignedToAccount;
    }

    public void setAgentAssignedToAccount(Agent agentAssignedToAccount) {
        this.agentAssignedToAccount = agentAssignedToAccount;
    }

    public Customer getPrimaryCustomer() {
        Customer firstCustomer = null;
        Set<Customer> customersForAccount = getCustomers();
        for (Customer customer : customersForAccount) {
            if (firstCustomer == null || firstCustomer.getCreatedTime().isAfter(customer.getCreatedTime())) {
                firstCustomer = customer;
            }
        }
        return firstCustomer;
    }

    public Set<Customer> getCustomers() {
        return customers;
    }

    public void setCustomers(Set<Customer> customers) {
        this.customers = customers;
    }

    public InboundDialerQueue getInboundDialerQueue() {
        return inboundDialerQueue;
    }

    public void setInboundDialerQueue(InboundDialerQueue inboundDialerQueue) {
        this.inboundDialerQueue = inboundDialerQueue;
    }

    public OutboundDialerQueue getOutboundDialerQueue() {
        return outboundDialerQueue;
    }

    public void setOutboundDialerQueue(OutboundDialerQueue outboundDialerQueue) {
        this.outboundDialerQueue = outboundDialerQueue;
    }

    public Set<CallResponseAction> getCallResponseActions() {
        return callResponseActions;
    }

    public void setCallResponseActions(Set<CallResponseAction> callResponseActions) {
        this.callResponseActions = callResponseActions;
    }

    public AccountData getAccountData() {
        return accountData;
    }

    public void setAccountData(AccountData accountData) {
        this.accountData = accountData;
    }

    public Set<WorkQueue> getWorkQueues() {
        return workQueues;
    }

    public void setWorkQueues(Set<WorkQueue> workQueues) {
        this.workQueues = workQueues;
    }

    public Set<DebtAccount> getDebtAccounts() {
        return debtAccounts;
    }

    public void setDebtAccounts(Set<DebtAccount> debtAccounts) {
        this.debtAccounts = debtAccounts;
    }

    public Set<AccountSchedule> getAccountSchedule() {
        return accountSchedule;
    }

    public void setAccountSchedule(Set<AccountSchedule> accountSchedule) {
        this.accountSchedule = accountSchedule;
    }

    public Set<BankAccount> getBankAccounts() {
        return bankAccounts;
    }

    public void setBankAccounts(Set<BankAccount> bankAccounts) {
        this.bankAccounts = bankAccounts;
    }

    public Set<AchPayment> getAchPayments() {
        return achPayments;
    }

    public void setAchPayments(Set<AchPayment> achPayments) {
        this.achPayments = achPayments;
    }

    public SortedSet<Payment> getPayments() {
        return payments;
    }

    public void setPayments(SortedSet<Payment> payments) {
        this.payments = payments;
    }
        
}
