package br.com.chronac.auth;

import java.time.Instant;
import javax.crypto.SecretKey;
import com.nimbusds.jose.jwk.source.ImmutableSecret;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = "timefold.solver.termination.spent-limit=2s")
@AutoConfigureMockMvc
@ActiveProfiles("dev")
class AuthIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;
    @Autowired SecretKey key;
    @Autowired AuthUsers users;

    @Test
    void protectsApiAndRequiresCsrfForLogin() throws Exception {
        mvc.perform(get("/api/timetable")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/auth/me")).andExpect(status().isUnauthorized());
        mvc.perform(post("/api/auth/login").contentType("application/json")
                .content(credentials("admin1234"))).andExpect(status().isForbidden());
        mvc.perform(get("/login")).andExpect(status().isOk()).andExpect(forwardedUrl("/index.html"));
    }

    @Test
    void publicLandingAndExistingSpaRoutesRemainAvailableWithoutSession() throws Exception {
        for (String path : new String[]{"/chronac", "/chronac/", "/login", "/calendar"}) {
            mvc.perform(get(path)).andExpect(status().isOk()).andExpect(forwardedUrl("/index.html"));
        }
        mvc.perform(get("/api/timetable")).andExpect(status().isUnauthorized());
    }

    @Test
    void rejectsWrongCredentialsAndMalformedInputs() throws Exception {
        mvc.perform(post("/api/auth/login").with(csrf()).contentType("application/json")
                .content(credentials("wrong"))).andExpect(status().isUnauthorized());
        mvc.perform(post("/api/auth/login").with(csrf()).contentType("application/json")
                .content("{}")).andExpect(status().isUnauthorized());
        assertThat(users.authenticate("unknown@example.com", "admin1234")).isNull();
        assertThat(users.authenticate("admin123@senac.com", "x".repeat(73))).isNull();
    }

    @Test
    void loginPersistsViaCookieAndLogoutRevokesJwt() throws Exception {
        // Exercise the actual cookie + header CSRF flow used by the browser.
        var csrfResponse = mvc.perform(get("/api/auth/csrf")).andExpect(status().isOk()).andReturn().getResponse();
        var csrfJson = mapper.readTree(csrfResponse.getContentAsString());
        Cookie csrfCookie = csrfResponse.getCookie("XSRF-TOKEN");
        var login = mvc.perform(post("/api/auth/login").cookie(csrfCookie)
                .header(csrfJson.get("headerName").asText(), csrfJson.get("token").asText())
                .contentType("application/json").content(credentials("admin1234")))
                .andExpect(status().isOk()).andExpect(jsonPath("role").value("ADMIN"))
                .andExpect(jsonPath("password").doesNotExist()).andReturn().getResponse();
        Cookie cookie = login.getCookie(AuthConfig.COOKIE);
        assertThat(cookie).isNotNull();
        assertThat(cookie.isHttpOnly()).isTrue();
        assertThat(cookie.getMaxAge()).isEqualTo(8 * 3600);
        assertThat(login.getHeader(HttpHeaders.SET_COOKIE)).contains("SameSite=Strict", "Path=/api");
        mvc.perform(get("/api/auth/me").cookie(cookie)).andExpect(status().isOk())
                .andExpect(jsonPath("email").value("admin123@senac.com"));
        mvc.perform(get("/api/auth/me").cookie(cookie)).andExpect(status().isOk());
        mvc.perform(get("/api/timetable").cookie(cookie)).andExpect(status().isOk());
        mvc.perform(post("/api/auth/logout").cookie(cookie)).andExpect(status().isForbidden());
        mvc.perform(post("/api/auth/logout").cookie(cookie).with(csrf())).andExpect(status().isNoContent())
                .andExpect(cookie().maxAge(AuthConfig.COOKIE, 0));
        mvc.perform(get("/api/auth/me").cookie(cookie)).andExpect(status().isUnauthorized());
    }

    @Test
    void rejectsExpiredAndTamperedTokens() throws Exception {
        var encoder = new NimbusJwtEncoder(new ImmutableSecret<>(key));
        var claims = JwtClaimsSet.builder().issuer("chronac").subject("admin123@senac.com")
                .id("expired").issuedAt(Instant.now().minusSeconds(120)).expiresAt(Instant.now().minusSeconds(90)).build();
        String expired = encoder.encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), claims)).getTokenValue();
        mvc.perform(get("/api/auth/me").cookie(new Cookie(AuthConfig.COOKIE, expired)))
                .andExpect(status().isUnauthorized());
        mvc.perform(get("/api/auth/me").cookie(new Cookie(AuthConfig.COOKIE, expired + "tampered")))
                .andExpect(status().isUnauthorized());
    }

    private String credentials(String password) {
        return "{\"email\":\"admin123@senac.com\",\"password\":\"" + password + "\"}";
    }
}
