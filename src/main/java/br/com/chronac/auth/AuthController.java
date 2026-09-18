package br.com.chronac.auth;

import java.time.Duration;
import java.util.Map;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthUsers users;
    private final AuthTokens tokens;
    private final boolean secureCookie;

    public AuthController(AuthUsers users, AuthTokens tokens,
            @Value("${chronac.auth.secure-cookie:true}") boolean secureCookie) {
        this.users = users;
        this.tokens = tokens;
        this.secureCookie = secureCookie;
    }

    @GetMapping("/csrf")
    public Map<String, String> csrf(CsrfToken csrf) {
        return Map.of("token", csrf.getToken(), "headerName", csrf.getHeaderName());
    }

    @PostMapping("/login")
    public AuthUsers.User login(@RequestBody LoginRequest request, HttpServletResponse response) {
        var user = users.authenticate(request.email(), request.password());
        if (user == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Email ou senha inválidos");
        response.addHeader(HttpHeaders.SET_COOKIE, cookie(tokens.issue(user.email(), user.role()), AuthTokens.LIFETIME));
        return user;
    }

    @GetMapping("/me")
    public AuthUsers.User me(@AuthenticationPrincipal Jwt jwt) {
        return new AuthUsers.User(jwt.getSubject(), Role.valueOf(jwt.getClaimAsStringList("roles").get(0)));
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(@AuthenticationPrincipal Jwt jwt, HttpServletResponse response) {
        tokens.revoke(jwt);
        response.addHeader(HttpHeaders.SET_COOKIE, cookie("", Duration.ZERO));
    }

    private String cookie(String value, Duration lifetime) {
        return ResponseCookie.from(AuthConfig.COOKIE, value).httpOnly(true).secure(secureCookie)
                .sameSite("Strict").path("/api").maxAge(lifetime).build().toString();
    }

    public record LoginRequest(String email, String password) {}
}
