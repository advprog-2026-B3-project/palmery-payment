package id.ac.ui.cs.advprog.palmerypayment.repository;

import id.ac.ui.cs.advprog.palmerypayment.model.PaymentTopUp;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentTopUpRepository extends JpaRepository<PaymentTopUp, Long> {
    Optional<PaymentTopUp> findByReference(String reference);
}
