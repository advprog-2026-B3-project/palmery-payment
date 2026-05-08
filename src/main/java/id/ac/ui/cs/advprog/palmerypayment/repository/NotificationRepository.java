package id.ac.ui.cs.advprog.palmerypayment.repository;

import id.ac.ui.cs.advprog.palmerypayment.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByTargetUser_UserIdOrderByCreatedAtDesc(String userId);

    long countByTargetUser_UserIdAndStatus(String userId, String status);

    Optional<Notification> findByIdAndTargetUser_UserId(Long id, String userId);
}
