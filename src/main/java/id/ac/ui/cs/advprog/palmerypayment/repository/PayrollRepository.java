package id.ac.ui.cs.advprog.palmerypayment.repository;

import id.ac.ui.cs.advprog.palmerypayment.model.Payroll;
import id.ac.ui.cs.advprog.palmerypayment.model.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PayrollRepository extends JpaRepository<Payroll, Long> {
    List<Payroll> findByWalletOrderByPaidAtDesc(Wallet wallet);
}
