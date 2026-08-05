package sn.smartwaste.collect.identity.infrastructure.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.web.filter.OncePerRequestFilter;
import sn.smartwaste.collect.identity.application.service.SessionService;
import sn.smartwaste.collect.identity.application.service.UserService;

import io.jsonwebtoken.JwtException;

import java.io.IOException;
import java.util.UUID;

@Service
public class JwtFilter extends OncePerRequestFilter {

    private final UserService userService;
    private final JwtService jwtService;
    private final SessionService sessionService;

    public JwtFilter(UserService userService, JwtService jwtService, SessionService sessionService) {
        this.userService = userService;
        this.jwtService = jwtService;
        this.sessionService = sessionService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String token = null;
        String username = null;
        boolean isTokenExpired = true;
        UUID sessionId = null;

        // Bearer eyJhbGciOiJIUzI1NiJ9.eyJub20iOiJBY2hpbGxlIE1CT1VHVUVORyIsImVtYWlsIjoiYWNoaWxsZS5tYm91Z3VlbmdAY2hpbGxvLnRlY2gifQ.zDuRKmkonHdUez-CLWKIk5Jdq9vFSUgxtgdU1H2216U
        final String authorization = request.getHeader("Authorization");
        if(authorization != null && authorization.startsWith("Bearer ")){
            token = authorization.substring(7);
            try {
                isTokenExpired = jwtService.isTokenExpired(token);
                username = jwtService.extractUsername(token);
                sessionId = jwtService.extractSessionId(token);
            } catch (JwtException | IllegalArgumentException e) {
                // Token invalide/expiré/malformé — `IllegalArgumentException` couvre en plus un
                // claim `sid` qui ne serait pas un UUID. Requête laissée non authentifiée
                // (la chaîne de sécurité répondra 401), plutôt qu'une erreur 500.
                isTokenExpired = true;
                username = null;
            }
        }

        // Signature valide et non expiré ne suffisent plus : la session doit être ouverte.
        // C'est ce qui rend la déconnexion réelle — auparavant, vider le `localStorage` côté
        // client laissait le jeton pleinement utilisable pendant ses 10 jours.
        // Coût assumé : une lecture indexée par requête authentifiée. C'est le prix de la
        // révocation ; un JWT purement autoportant ne peut pas être révoqué, par construction.
        boolean sessionOpen = sessionId != null && sessionService.isActive(sessionId);

        if(!isTokenExpired && username != null && sessionOpen
                && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = userService.loadUserByUsername(username);
            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(authenticationToken);
        }

        filterChain.doFilter(request, response);
    }
}
