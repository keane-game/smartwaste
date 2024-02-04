package sonaged.collecte.master.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import sonaged.collecte.master.dto.reponse.AuthReponse;
import sonaged.collecte.master.dto.request.AuthRequest;
import sonaged.collecte.master.repository.UserRepository;
import sonaged.collecte.master.security.JwtService;
import sonaged.collecte.master.security.SecurityConfig;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements SecurityConfig.AuthenticationService {
    private final UserRepository userRepository;
    private final JwtService jwtService;
   /* @Override
    public JwtAuthenticationResponse signup(SignUpRequest request) {
        var user = User.builder().firstName(request.getFirstName()).lastName(request.getLastName())
                .email(request.getEmail()).password(passwordEncoder.encode(request.getPassword()))
                .role(Role.USER).build();
        userRepository.save(user);
        var jwt = jwtService.generateToken(user);
        return JwtAuthenticationResponse.builder().token(jwt).build();
    }
    */
    @Override
    public AuthReponse signin(AuthRequest request) throws Exception{
        log.info("coodinate_1 {}", request);
        try {
           /* this.authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail (), request.getPassword())
            );*/
        } catch (BadCredentialsException e) {
            //Here is the error
            log.info("coodinate_2221 {}", request);
            throw new Exception("Incorrect username or password", e);
        }
    log.info("coodinate_12 {}", request);
        var user = userRepository.findByUserEmail (request.getEmail())
               ;
        log.info("coodinate31 {}", request);
        var jwt = jwtService.generateToken(user.getUserEmail());
        return AuthReponse.builder().token(jwt).build();
    }
}