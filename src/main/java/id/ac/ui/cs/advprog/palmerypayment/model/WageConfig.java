package id.ac.ui.cs.advprog.palmerypayment.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;

@Entity
@Table(name = "wage_config")
public class WageConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String roleKey;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal ratePerKg;

    protected WageConfig() {
        // JPA constructor
    }

    public WageConfig(String roleKey, BigDecimal ratePerKg) {
        this.roleKey = roleKey;
        this.ratePerKg = ratePerKg;
    }

    public Long getId() {
        return id;
    }

    public String getRoleKey() {
        return roleKey;
    }

    public BigDecimal getRatePerKg() {
        return ratePerKg;
    }

    public void setRatePerKg(BigDecimal ratePerKg) {
        this.ratePerKg = ratePerKg;
    }
}
