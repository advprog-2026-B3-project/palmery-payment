package id.ac.ui.cs.advprog.palmerypayment.controller;

import id.ac.ui.cs.advprog.palmerypayment.dto.UpdateWageConfigRequest;
import id.ac.ui.cs.advprog.palmerypayment.dto.WageConfigView;
import id.ac.ui.cs.advprog.palmerypayment.service.WageConfigService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping({"/api/admin/config/wages", "/api/admin/config/upah"})
public class WageConfigController {

    private final WageConfigService wageConfigService;

    public WageConfigController(WageConfigService wageConfigService) {
        this.wageConfigService = wageConfigService;
    }

    @GetMapping
    public ResponseEntity<WageConfigView> getWages() {
        return ResponseEntity.ok(wageConfigService.getCurrentConfig());
    }

    @PatchMapping
    public ResponseEntity<?> updateWages(@RequestBody UpdateWageConfigRequest request) {
        try {
            return ResponseEntity.ok(wageConfigService.update(request));
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.badRequest().body(Map.of("message", exception.getMessage()));
        }
    }
}
