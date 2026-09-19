package br.com.chronac.auth;

import java.util.Locale;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/** Initial backend account; this service can later be backed by a user repository. */
@Service
public class AuthUsers {
    private final String email;
    private final String passwordHash;
    private final PasswordEncoder encoder;

    public AuthUsers(@Value("${chronac.auth.admin-email:}") String email,
            @Value("${chronac.auth.admin-password-hash:}") String passwordHash, PasswordEncoder encoder) {
        if (email.isBlank() || !passwordHash.matches("\\$2[aby]\\$12\\$.{53}")) {
            throw new IllegalStateException("Configure o email e um hash BCrypt de custo 12 para o administrador.");
        }
        this.email = email.trim().toLowerCase(Locale.ROOT);
        this.passwordHash = passwordHash;
        this.encoder = encoder;
    }

    public User authenticate(String candidateEmail, String password) {
        if (candidateEmail == null || password == null || candidateEmail.length() > 254
                || password.getBytes(java.nio.charset.StandardCharsets.UTF_8).length > 72) return null;
        boolean validPassword = encoder.matches(password, passwordHash);
        return validPassword && email.equals(candidateEmail.trim().toLowerCase(Locale.ROOT))
                ? new User(email, Role.ADMIN) : null;
    }

    public record User(String email, Role role) {}
}
