package br.com.chronac.auth;

import java.util.Base64;
import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.mock.web.MockHttpServletResponse;
import static org.assertj.core.api.Assertions.*;

class AuthConfigTest {
    @Test
    void productionRequiresSecretAndAccountConfiguration() {
        var config = new AuthConfig();
        var environment = new MockEnvironment();
        assertThatThrownBy(() -> config.jwtKey("", environment)).isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> config.jwtKey(Base64.getEncoder().encodeToString(new byte[16]), environment))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> new AuthUsers("", "", config.passwordEncoder()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(config.jwtKey(Base64.getEncoder().encodeToString(new byte[32]), environment).getEncoded())
                .hasSize(32);
    }

    @Test
    void productionSessionCookieIsSecureAndHashAuthenticates() {
        var config = new AuthConfig();
        var key = config.jwtKey(Base64.getEncoder().encodeToString(new byte[32]), new MockEnvironment());
        var users = new AuthUsers("production@example.com", config.passwordEncoder().encode("different-password"),
                config.passwordEncoder());
        var controller = new AuthController(users, new AuthTokens(key), true);
        var response = new MockHttpServletResponse();
        var user = controller.login(new AuthController.LoginRequest("production@example.com", "different-password"), response);
        assertThat(user.role()).isEqualTo(Role.ADMIN);
        assertThat(response.getHeader("Set-Cookie")).contains("Secure", "HttpOnly", "SameSite=Strict");
        assertThat(users.authenticate("production@example.com", "admin1234")).isNull();
    }
}
