package id.ac.ui.cs.advprog.palmerypayment.controller;

import id.ac.ui.cs.advprog.palmerypayment.dto.WalletDashboardView;
import id.ac.ui.cs.advprog.palmerypayment.service.WalletDashboardService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/wallet")
public class WalletController {

    private final WalletDashboardService walletDashboardService;

    public WalletController(WalletDashboardService walletDashboardService) {
        this.walletDashboardService = walletDashboardService;
    }

    @GetMapping("/{userId}")
    public String walletPage(@PathVariable String userId, Model model) {
        WalletDashboardView walletDashboard = walletDashboardService.getWalletDashboard(userId);
        model.addAttribute("wallet", walletDashboard);
        return "wallet";
    }
}
