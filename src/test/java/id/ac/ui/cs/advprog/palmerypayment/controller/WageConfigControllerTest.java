package id.ac.ui.cs.advprog.palmerypayment.controller;

import id.ac.ui.cs.advprog.palmerypayment.dto.WageConfigView;
import id.ac.ui.cs.advprog.palmerypayment.service.WageConfigService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WageConfigControllerTest {

    @Mock
    private WageConfigService wageConfigService;

    @InjectMocks
    private WageConfigController wageConfigController;

    @Test
    void getWagesReturnsCurrentConfig() {
        WageConfigView config = new WageConfigView(
                new BigDecimal("12.00"),
                new BigDecimal("10.00"),
                new BigDecimal("15.00")
        );
        when(wageConfigService.getCurrentConfig()).thenReturn(config);

        ResponseEntity<WageConfigView> response = wageConfigController.getWages();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(config, response.getBody());
    }
}
