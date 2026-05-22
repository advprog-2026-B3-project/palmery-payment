package id.ac.ui.cs.advprog.palmerypayment.service;

import id.ac.ui.cs.advprog.palmerypayment.model.Wallet;
import id.ac.ui.cs.advprog.palmerypayment.repository.WalletRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class WalletService {

    private final WalletRepository walletRepository;

    public WalletService(WalletRepository walletRepository) {
        this.walletRepository = walletRepository;
    }

    @Transactional
    public Wallet initializeWallet(String userId) {
        return getOrCreateWallet(userId);
    }

    @Transactional
    public Wallet getOrCreateWallet(String userId) {
        String normalizedUserId = normalizeUserId(userId);
        return walletRepository.findByUserId(normalizedUserId)
                .orElseGet(() -> walletRepository.save(new Wallet(normalizedUserId, BigDecimal.ZERO)));
    }

    @Transactional
    public void addBalance(Wallet wallet, BigDecimal amount) {
        wallet.setBalance(wallet.getBalance().add(amount));
        walletRepository.save(wallet);
    }

    @Transactional
    public void subtractBalance(Wallet wallet, BigDecimal amount) {
        if (wallet.getBalance().compareTo(amount) < 0) {
            throw new IllegalStateException("admin wallet balance is insufficient");
        }
        wallet.setBalance(wallet.getBalance().subtract(amount));
        walletRepository.save(wallet);
    }

    private String normalizeUserId(String userId) {
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("userId is required");
        }
        return userId.trim();
    }
}
