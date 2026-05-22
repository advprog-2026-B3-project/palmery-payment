package id.ac.ui.cs.advprog.palmerypayment.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "payment_topup")
public class PaymentTopUp {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String reference;

    @Column(nullable = false, length = 100)
    private String adminUserId;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amountRupiah;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal creditedAmount;

    @Column(nullable = false, length = 50)
    private String status;

    @Column(nullable = false, length = 255)
    private String gatewayUrl;

    @Column(length = 100)
    private String gatewayToken;

    @Column(length = 50)
    private String gatewayProvider;

    @Column(length = 50)
    private String gatewayStatus;

    @Column(nullable = false)
    private Instant createdAt;

    private Instant paidAt;

    protected PaymentTopUp() {
        // JPA constructor
    }

    public PaymentTopUp(
            String reference,
            String adminUserId,
            BigDecimal amountRupiah,
            BigDecimal creditedAmount,
            String status,
            String gatewayUrl,
            String gatewayToken,
            String gatewayProvider
    ) {
        this.reference = reference;
        this.adminUserId = adminUserId;
        this.amountRupiah = amountRupiah;
        this.creditedAmount = creditedAmount;
        this.status = status;
        this.gatewayUrl = gatewayUrl;
        this.gatewayToken = gatewayToken;
        this.gatewayProvider = gatewayProvider;
        this.gatewayStatus = status;
    }

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }

    public Long getId() {
        return id;
    }

    public String getReference() {
        return reference;
    }

    public String getAdminUserId() {
        return adminUserId;
    }

    public BigDecimal getAmountRupiah() {
        return amountRupiah;
    }

    public BigDecimal getCreditedAmount() {
        return creditedAmount;
    }

    public String getStatus() {
        return status;
    }

    public String getGatewayUrl() {
        return gatewayUrl;
    }

    public String getGatewayToken() {
        return gatewayToken;
    }

    public String getGatewayProvider() {
        return gatewayProvider;
    }

    public String getGatewayStatus() {
        return gatewayStatus;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getPaidAt() {
        return paidAt;
    }

    public void markPaid() {
        this.status = "PAID";
        this.gatewayStatus = "settlement";
        this.paidAt = Instant.now();
    }

    public void markPaid(String gatewayStatus) {
        this.status = "PAID";
        this.gatewayStatus = gatewayStatus;
        this.paidAt = Instant.now();
    }

    public void updateGatewayStatus(String gatewayStatus) {
        if (gatewayStatus != null && !gatewayStatus.isBlank()) {
            this.gatewayStatus = gatewayStatus.trim();
        }
    }

    public void markFailed(String gatewayStatus) {
        this.status = "FAILED";
        this.gatewayStatus = gatewayStatus;
    }
}
