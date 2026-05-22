package id.ac.ui.cs.advprog.palmerypayment.repository;

import id.ac.ui.cs.advprog.palmerypayment.model.WageConfig;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WageConfigRepository extends JpaRepository<WageConfig, Long> {
    Optional<WageConfig> findByRoleKey(String roleKey);
}
