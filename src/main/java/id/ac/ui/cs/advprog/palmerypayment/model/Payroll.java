package id.ac.ui.cs.advprog.palmerypayment.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.PreUpdate;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "payroll")
public class Payroll {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "wallet_id", nullable = false)
    private Wallet wallet;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, length = 255)
    private String description;

    @Column(nullable = false, length = 50)
    private String type;

    @Column(nullable = false, length = 50)
    private String status;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal quantityKg;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal ratePerKg;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal deductionRate;

    @Column(nullable = false, length = 1000)
    private String calculationDetail;

    @Column(length = 500)
    private String rejectionReason;

    @Column(unique = true, length = 100)
    private String sourceEventId;

    @Column(length = 100)
    private String sourceEventType;

    @Column(nullable = false)
    private Instant createdAt;

    private Instant processedAt;

    protected Payroll() {
        // JPA constructor
    }

    public Payroll(
            Wallet wallet,
            BigDecimal amount,
            String description,
            String type,
            String status,
            BigDecimal quantityKg,
            BigDecimal ratePerKg,
            BigDecimal deductionRate,
            String calculationDetail
    ) {
        this.wallet = wallet;
        this.amount = amount;
        this.description = description;
        this.type = type;
        this.status = status;
        this.quantityKg = quantityKg;
        this.ratePerKg = ratePerKg;
        this.deductionRate = deductionRate;
        this.calculationDetail = calculationDetail;
    }

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }

    @PreUpdate
    public void preUpdate() {
        if (!"PENDING".equals(status) && processedAt == null) {
            processedAt = Instant.now();
        }
    }

    public Long getId() {
        return id;
    }

    public Wallet getWallet() {
        return wallet;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getDescription() {
        return description;
    }

    public String getType() {
        return type;
    }

    public String getStatus() {
        return status;
    }

    public BigDecimal getQuantityKg() {
        return quantityKg;
    }

    public BigDecimal getRatePerKg() {
        return ratePerKg;
    }

    public BigDecimal getDeductionRate() {
        return deductionRate;
    }

    public String getCalculationDetail() {
        return calculationDetail;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public String getSourceEventId() {
        return sourceEventId;
    }

    public String getSourceEventType() {
        return sourceEventType;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getProcessedAt() {
        return processedAt;
    }

    public void approve() {
        this.status = "ACCEPTED";
        this.rejectionReason = null;
        this.processedAt = Instant.now();
    }

    public void reject(String reason) {
        this.status = "REJECTED";
        this.rejectionReason = reason;
        this.processedAt = Instant.now();
    }

    public void attachEventSource(String sourceEventId, String sourceEventType) {
        this.sourceEventId = sourceEventId;
        this.sourceEventType = sourceEventType;
    }
}
