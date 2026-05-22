package id.ac.ui.cs.advprog.palmerypayment.repository;

import id.ac.ui.cs.advprog.palmerypayment.model.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WalletRepository extends JpaRepository<Wallet, Long> {
    Optional<Wallet> findByUserId(String userId);
}
