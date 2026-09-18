package br.com.chronac.auth;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import javax.crypto.SecretKey;
import com.nimbusds.jose.jwk.source.ImmutableSecret;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;

@Service
public class AuthTokens {
    public static final Duration LIFETIME = Duration.ofHours(8);
    private final NimbusJwtEncoder encoder;
    private final NimbusJwtDecoder decoder;
    // Single-server revocation. Replace with a shared store when deploying multiple instances.
    private final ConcurrentHashMap<String, Instant> revoked = new ConcurrentHashMap<>();

    public AuthTokens(SecretKey key) {
        encoder = new NimbusJwtEncoder(new ImmutableSecret<>(key));
        decoder = NimbusJwtDecoder.withSecretKey(key).macAlgorithm(MacAlgorithm.HS256).build();
        decoder.setJwtValidator(JwtValidators.createDefaultWithIssuer("chronac"));
    }

    public String issue(String email, Role role) {
        Instant now = Instant.now();
        var claims = JwtClaimsSet.builder().issuer("chronac").subject(email)
                .id(UUID.randomUUID().toString()).issuedAt(now).expiresAt(now.plus(LIFETIME))
                .claim("roles", List.of(role.name())).build();
        return encoder.encode(JwtEncoderParameters.from(
                JwsHeader.with(MacAlgorithm.HS256).build(), claims)).getTokenValue();
    }

    public Jwt decode(String token) {
        Jwt jwt = decoder.decode(token);
        if (jwt.getExpiresAt() == null || !jwt.getExpiresAt().isAfter(Instant.now())
                || jwt.getId() == null || revoked.containsKey(jwt.getId())) {
            throw new BadJwtException("Sessão inválida ou expirada");
        }
        return jwt;
    }

    public void revoke(Jwt jwt) {
        revoked.entrySet().removeIf(entry -> entry.getValue().isBefore(Instant.now()));
        revoked.put(jwt.getId(), jwt.getExpiresAt());
    }
}
