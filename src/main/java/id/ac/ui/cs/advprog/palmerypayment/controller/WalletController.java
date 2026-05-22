package id.ac.ui.cs.advprog.palmerypayment.controller;

import id.ac.ui.cs.advprog.palmerypayment.dto.WalletDashboardView;
import id.ac.ui.cs.advprog.palmerypayment.security.CurrentUser;
import id.ac.ui.cs.advprog.palmerypayment.service.WalletDashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/wallets")
public class WalletController {

    private final WalletDashboardService walletDashboardService;

    public WalletController(WalletDashboardService walletDashboardService) {
        this.walletDashboardService = walletDashboardService;
    }

    @GetMapping("/{userId}")
    public ResponseEntity<WalletDashboardView> getWallet(
            @PathVariable String userId,
            JwtAuthenticationToken authentication,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate
    ) {
        CurrentUser currentUser = CurrentUser.from(authentication);
        if (!currentUser.isAdmin() && !currentUser.userId().equalsIgnoreCase(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "cannot view another user's wallet");
        }
        WalletDashboardView walletDashboard = walletDashboardService.getWalletDashboard(userId, status, fromDate, toDate);
        return ResponseEntity.ok(walletDashboard);
    }
}
