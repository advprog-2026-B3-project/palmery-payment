package id.ac.ui.cs.advprog.palmerypayment.service;

import id.ac.ui.cs.advprog.palmerypayment.dto.WalletDashboardView;

import java.time.LocalDate;

public interface WalletDashboardService {
    WalletDashboardView getWalletDashboard(String userId, String status, LocalDate fromDate, LocalDate toDate);
}
