package id.ac.ui.cs.advprog.palmerypayment.controller;

import id.ac.ui.cs.advprog.palmerypayment.dto.WalletDashboardView;
import id.ac.ui.cs.advprog.palmerypayment.service.WalletDashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/wallets")
public class WalletController {

    private final WalletDashboardService walletDashboardService;

    public WalletController(WalletDashboardService walletDashboardService) {
        this.walletDashboardService = walletDashboardService;
    }

    @GetMapping("/{userId}")
    public ResponseEntity<WalletDashboardView> getWallet(@PathVariable String userId) {
        WalletDashboardView walletDashboard = walletDashboardService.getWalletDashboard(userId);
        return ResponseEntity.ok(walletDashboard);
    }
}
