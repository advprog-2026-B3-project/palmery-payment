package id.ac.ui.cs.advprog.palmerypayment.service;

import id.ac.ui.cs.advprog.palmerypayment.dto.UpdateWageConfigRequest;
import id.ac.ui.cs.advprog.palmerypayment.dto.WageConfigView;
import id.ac.ui.cs.advprog.palmerypayment.model.WageConfig;
import id.ac.ui.cs.advprog.palmerypayment.repository.WageConfigRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WageConfigServiceTest {

    @Mock
    private WageConfigRepository wageConfigRepository;

    @InjectMocks
    private WageConfigService wageConfigService;

    @Test
    void getCurrentConfigCreatesDefaultRatesWhenMissing() {
        when(wageConfigRepository.findByRoleKey(anyString())).thenReturn(Optional.empty());
        when(wageConfigRepository.save(any(WageConfig.class))).thenAnswer(invocation -> invocation.getArgument(0));

        WageConfigView config = wageConfigService.getCurrentConfig();

        assertEquals(new BigDecimal("12.00"), config.buruhRatePerKg());
        assertEquals(new BigDecimal("10.00"), config.supirRatePerKg());
        assertEquals(new BigDecimal("15.00"), config.mandorRatePerKg());
    }

    @Test
    void updateRejectsNonPositiveRate() {
        UpdateWageConfigRequest request = new UpdateWageConfigRequest();
        request.setBuruhRatePerKg(BigDecimal.ZERO);

        assertThrows(IllegalArgumentException.class, () -> wageConfigService.update(request));
    }
}
