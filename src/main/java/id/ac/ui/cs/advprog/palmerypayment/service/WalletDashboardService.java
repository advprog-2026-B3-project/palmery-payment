package id.ac.ui.cs.advprog.palmerypayment.service;

import id.ac.ui.cs.advprog.palmerypayment.dto.WalletDashboardView;

public interface WalletDashboardService {
    WalletDashboardView getWalletDashboard(String userId);
}
