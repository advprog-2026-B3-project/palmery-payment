package id.ac.ui.cs.advprog.palmerypayment.dto;

import java.math.BigDecimal;

public record WageConfigView(
        BigDecimal buruhRatePerKg,
        BigDecimal supirRatePerKg,
        BigDecimal mandorRatePerKg
) {
}
