package id.ac.ui.cs.advprog.palmerypayment.repository;

import id.ac.ui.cs.advprog.palmerypayment.model.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AppUserRepository extends JpaRepository<AppUser, String> {
    List<AppUser> findByRoleKeyOrderByUserIdAsc(String roleKey);
}
