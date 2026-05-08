package id.ac.ui.cs.advprog.palmerypayment.service;

import id.ac.ui.cs.advprog.palmerypayment.model.AppUser;
import id.ac.ui.cs.advprog.palmerypayment.repository.AppUserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
public class UserDirectoryService {

    private final AppUserRepository appUserRepository;

    public UserDirectoryService(AppUserRepository appUserRepository) {
        this.appUserRepository = appUserRepository;
    }

    @Transactional
    public AppUser upsertUser(String userId, String roleKey, String displayName) {
        String normalizedUserId = normalizeUserId(userId);
        String normalizedRole = normalizeRole(roleKey);
        String normalizedDisplayName = normalizeOptionalDisplayName(displayName);

        AppUser user = appUserRepository.findById(normalizedUserId)
                .orElseGet(() -> new AppUser(normalizedUserId, normalizedRole, normalizedDisplayName));
        user.setRoleKey(normalizedRole);
        if (normalizedDisplayName != null) {
            user.setDisplayName(normalizedDisplayName);
        }
        return appUserRepository.save(user);
    }

    @Transactional
    public AppUser ensureUser(String userId) {
        String normalizedUserId = normalizeUserId(userId);
        return appUserRepository.findById(normalizedUserId)
                .orElseGet(() -> appUserRepository.save(new AppUser(normalizedUserId, "UNKNOWN", normalizedUserId)));
    }

    @Transactional(readOnly = true)
    public List<AppUser> resolveTargets(List<String> userIds, String roleTarget, Boolean all) {
        if (Boolean.TRUE.equals(all)) {
            return appUserRepository.findAll().stream()
                    .sorted((left, right) -> left.getUserId().compareToIgnoreCase(right.getUserId()))
                    .toList();
        }

        if (roleTarget != null && !roleTarget.isBlank()) {
            return appUserRepository.findByRoleKeyOrderByUserIdAsc(normalizeRole(roleTarget));
        }

        if (userIds == null || userIds.isEmpty()) {
            throw new IllegalArgumentException("provide userIds, roleTarget, or set all=true");
        }

        Set<String> uniqueUserIds = new LinkedHashSet<>();
        for (String userId : userIds) {
            uniqueUserIds.add(normalizeUserId(userId));
        }

        List<AppUser> users = new ArrayList<>();
        for (String userId : uniqueUserIds) {
            users.add(ensureUser(userId));
        }
        return users;
    }

    private String normalizeUserId(String userId) {
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("userId is required");
        }
        return userId.trim();
    }

    private String normalizeRole(String roleKey) {
        if (roleKey == null || roleKey.isBlank()) {
            return "UNKNOWN";
        }
        return roleKey.trim().toUpperCase(Locale.ROOT);
    }

    private String normalizeOptionalDisplayName(String displayName) {
        if (displayName == null || displayName.isBlank()) {
            return null;
        }
        return displayName.trim();
    }
}
