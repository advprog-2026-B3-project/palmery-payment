package id.ac.ui.cs.advprog.palmerypayment.service;

import id.ac.ui.cs.advprog.palmerypayment.dto.UpdateWageConfigRequest;
import id.ac.ui.cs.advprog.palmerypayment.dto.WageConfigView;
import id.ac.ui.cs.advprog.palmerypayment.model.WageConfig;
import id.ac.ui.cs.advprog.palmerypayment.repository.WageConfigRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Map;

@Service
public class WageConfigService {

    private static final Map<String, BigDecimal> DEFAULT_RATES = Map.of(
            "BURUH", new BigDecimal("12.00"),
            "SUPIR", new BigDecimal("10.00"),
            "MANDOR", new BigDecimal("15.00")
    );

    private final WageConfigRepository wageConfigRepository;

    public WageConfigService(WageConfigRepository wageConfigRepository) {
        this.wageConfigRepository = wageConfigRepository;
    }

    @Transactional
    public WageConfigView getCurrentConfig() {
        return new WageConfigView(
                getRateForRole("BURUH"),
                getRateForRole("SUPIR"),
                getRateForRole("MANDOR")
        );
    }

    @Transactional
    public WageConfigView update(UpdateWageConfigRequest request) {
        upsertRate("BURUH", request.getBuruhRatePerKg());
        upsertRate("SUPIR", request.getSupirRatePerKg());
        upsertRate("MANDOR", request.getMandorRatePerKg());
        return getCurrentConfig();
    }

    @Transactional
    public BigDecimal getRateForRole(String role) {
        String normalizedRole = normalizeRole(role);
        return wageConfigRepository.findByRoleKey(normalizedRole)
                .orElseGet(() -> wageConfigRepository.save(
                        new WageConfig(normalizedRole, DEFAULT_RATES.get(normalizedRole))
                ))
                .getRatePerKg();
    }

    private void upsertRate(String role, BigDecimal rate) {
        if (rate == null) {
            return;
        }
        if (rate.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("wage rate must be positive");
        }

        WageConfig config = wageConfigRepository.findByRoleKey(role)
                .orElseGet(() -> new WageConfig(role, rate));
        config.setRatePerKg(rate);
        wageConfigRepository.save(config);
    }

    private String normalizeRole(String role) {
        if (role == null || role.isBlank()) {
            throw new IllegalArgumentException("role is required");
        }
        String normalizedRole = role.trim().toUpperCase();
        if (!DEFAULT_RATES.containsKey(normalizedRole)) {
            throw new IllegalArgumentException("unsupported role: " + role);
        }
        return normalizedRole;
    }
}
