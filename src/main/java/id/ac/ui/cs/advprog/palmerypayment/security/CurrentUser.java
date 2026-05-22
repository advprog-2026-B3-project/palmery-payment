package id.ac.ui.cs.advprog.palmerypayment.security;

import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.server.ResponseStatusException;

import java.util.Locale;

public record CurrentUser(String userId, String role) {

    public static CurrentUser from(JwtAuthenticationToken authentication) {
        if (authentication == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "authentication is required");
        }

        Jwt jwt = authentication.getToken();
        String userId = jwt.getSubject();
        if (userId == null || userId.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "token subject is missing");
        }

        String role = jwt.getClaimAsString("role");
        if (role == null || role.isBlank()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "token role is missing");
        }

        return new CurrentUser(userId.trim(), role.trim().toUpperCase(Locale.ROOT));
    }

    public boolean isAdmin() {
        return "ADMIN".equals(role);
    }
}
