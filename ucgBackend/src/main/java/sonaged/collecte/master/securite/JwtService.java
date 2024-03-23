package sonaged.collecte.master.securite;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import sonaged.collecte.master.constant.SecurityConstants;
import sonaged.collecte.master.model.User;
import sonaged.collecte.master.model.Utilisateur;
import sonaged.collecte.master.service.UserService;

import javax.crypto.SecretKey;
import java.security.Key;
import java.util.Date;
import java.util.Map;
import java.util.function.Function;

import static java.time.LocalTime.now;

@AllArgsConstructor
@Service
public class JwtService {
    private final String ENCRIPTION_KEY = "608f36e92dc66d97d5933f0e6371493cb4fc05b1aa8f8de64014732472303a7c";
    private UserService utilisateurService;
    
    public Map<String, String> generate(String username) {
        User utilisateur = this.utilisateurService.loadUserByUsername(username);
        return this.generateJwt(utilisateur);
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

    private Map<String, String> generateJwt(User user) {
        final long currentTime = System.currentTimeMillis();
        final long expirationTime = currentTime + SecurityConstants.EXPIRATION_TIME;

        final Map<String, Object> claims = Map.of(
                "nom", user.getUserFirstname (),
                "role", user.getAuthorities(),
                Claims.ISSUED_AT, currentTime,

                Claims.EXPIRATION, new Date(System.currentTimeMillis()+ SecurityConstants.EXPIRATION_TIME),
                Claims.SUBJECT, user.getUserEmail ()
        );

        final String bearer =  Jwts.builder()
                .claims().add(claims)
                .and()
                .signWith(getSigningKey(), Jwts.SIG.HS256)
                .compact();



        return Map.of("bearer", bearer);
    }

    private Key getKey() {
        final byte[] decoder = Decoders.BASE64.decode(ENCRIPTION_KEY);
        return Keys.hmacShaKeyFor(decoder);
    }

    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(SecurityConstants.SECRET);
        return Keys.hmacShaKeyFor(keyBytes);
    }

}
