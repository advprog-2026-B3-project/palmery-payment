package id.ac.ui.cs.advprog.palmerypayment.dto;

import java.math.BigDecimal;

public class UpdateWageConfigRequest {

    private BigDecimal buruhRatePerKg;
    private BigDecimal supirRatePerKg;
    private BigDecimal mandorRatePerKg;

    public BigDecimal getBuruhRatePerKg() {
        return buruhRatePerKg;
    }

    public void setBuruhRatePerKg(BigDecimal buruhRatePerKg) {
        this.buruhRatePerKg = buruhRatePerKg;
    }

    public BigDecimal getSupirRatePerKg() {
        return supirRatePerKg;
    }

    public void setSupirRatePerKg(BigDecimal supirRatePerKg) {
        this.supirRatePerKg = supirRatePerKg;
    }

    public BigDecimal getMandorRatePerKg() {
        return mandorRatePerKg;
    }

    public void setMandorRatePerKg(BigDecimal mandorRatePerKg) {
        this.mandorRatePerKg = mandorRatePerKg;
    }
}
