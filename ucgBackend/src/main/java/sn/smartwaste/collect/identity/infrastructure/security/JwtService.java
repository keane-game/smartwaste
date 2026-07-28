package sn.smartwaste.collect.identity.infrastructure.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import sn.smartwaste.collect.identity.infrastructure.security.SecurityConstants;
import sn.smartwaste.collect.identity.domain.model.UserEntity;
import sn.smartwaste.collect.identity.application.service.UserService;
import sn.smartwaste.collect.identity.domain.repository.UserRepository;
import sn.smartwaste.collect.shared.domain.exception.ResourceNotFoundException;

import javax.crypto.SecretKey;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.time.LocalTime.now;

@Service
public class JwtService {

    /** Nom du claim portant l'identifiant de session — le point d'accroche de la révocation. */
    public static final String SESSION_CLAIM = "sid";

    private final UserService userService;
    private final UserRepository userRepository;

    /**
     * Durée de vie du jeton d'accès.
     *
     * <p>Reste par défaut à la valeur historique (10 jours) : la réduire à quelques minutes — ce
     * que la session rend enfin possible — déconnecterait les clients qui n'appellent pas encore
     * {@code POST /auth/refresh}. C'est un changement de configuration, à faire quand les clients
     * savent rafraîchir, et non un effet de bord de l'introduction des sessions.
     */
    private final long accessTokenTtlMs;

    public JwtService(UserService userService,
                      UserRepository userRepository,
                      @Value("${sonaged.security.jwt.access-ttl-ms:" + SecurityConstants.EXPIRATION_TIME + "}")
                      long accessTokenTtlMs) {
        this.userService = userService;
        this.userRepository = userRepository;
        this.accessTokenTtlMs = accessTokenTtlMs;
    }

    /** Charge un utilisateur par identifiant — utilisé au rafraîchissement de session. */
    public UserEntity loadUser(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur inconnu"));
    }

    /** Émet un jeton d'accès rattaché à une session. */
    public String issueAccessToken(UserEntity user, UUID sessionId) {
        return this.generateJwt(user, sessionId);
    }

    /** Identifiant de session porté par le jeton, ou {@code null} pour un jeton antérieur aux sessions. */
    public UUID extractSessionId(String token) {
        String sid = this.getClaim(token, claims -> claims.get(SESSION_CLAIM, String.class));
        return sid == null ? null : UUID.fromString(sid);
    }

    public String extractUsername(String token) {
        return this.getClaim(token, Claims::getSubject);
    }

    public boolean isTokenExpired(String token) {
        Date expirationDate = getExpirationDateFromToken(token);
        return expirationDate.before(new Date());
    }

    private Date getExpirationDateFromToken(String token) {
        return this.getClaim(token, Claims::getExpiration);
    }

    private <T> T getClaim(String token, Function<Claims, T> function) {
        Claims claims = extractAllClaims(token);
        return function.apply(claims);
    }

  /*  private Claims getAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(this.getKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }*/

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey ())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private String generateJwt(UserEntity user, UUID sessionId) {
        final long currentTime = System.currentTimeMillis();
        final List<String> roles = user.getAuthorities().stream ().map (GrantedAuthority::getAuthority).collect(Collectors.toList ());
        // Garde contre un utilisateur sans autorité (évite IndexOutOfBoundsException / 500 au login).
        // Claim "role" purement informatif : l'autorisation s'appuie sur les authorities chargées en base.
        final String role = roles.isEmpty() ? "" : roles.get(0);
        final Map<String, Object> claims = Map.of(
                "firstname", user.getUserFirstname (),
                "lastname", user.getUserLastname (),
                "role", role,
                // Rattache le jeton à une session révocable : sans ce claim, aucun moyen de
                // savoir de quelle connexion il provient, donc aucun moyen de la fermer.
                SESSION_CLAIM, sessionId.toString(),
                Claims.ISSUED_AT, currentTime,
                Claims.EXPIRATION, new Date(currentTime + accessTokenTtlMs),
                Claims.SUBJECT, user.getUserEmail ()
        );

        return Jwts.builder()
                .claims().add(claims)
                .and()
                .signWith(getSigningKey(), Jwts.SIG.HS256)
                .compact();
    }

    // `getKey()` et sa constante `ENCRIPTION_KEY` ont disparu ici : c'était un SECOND secret
    // versionné, distinct de la clé de signature (`SecurityConstants.SECRET`), et entièrement
    // mort — son unique appelant était le bloc `getAllClaims()` commenté juste au-dessus. La
    // revue de sécurité automatique avait d'ailleurs signalé cette constante en croyant qu'elle
    // signait les jetons. Elle reste compromise et à roter au même titre que l'autre (P0-2).

    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(SecurityConstants.SECRET);
        return Keys.hmacShaKeyFor(keyBytes);
    }

}
