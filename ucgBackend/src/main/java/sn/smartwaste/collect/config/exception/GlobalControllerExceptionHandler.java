package sn.smartwaste.collect.config.exception;

import sn.smartwaste.collect.shared.domain.exception.MediaTypeNotSupportedException;
import sn.smartwaste.collect.shared.domain.exception.ResourceAlreadyExistException;
import sn.smartwaste.collect.shared.domain.exception.ResourceNotFoundException;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;




@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalControllerExceptionHandler {

    private final ObjectMapper objectMapper;

    private static final Logger logger = LoggerFactory.getLogger(GlobalControllerExceptionHandler.class);

    @ResponseStatus(value = HttpStatus.UNPROCESSABLE_ENTITY)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Error invalidInput(MethodArgumentNotValidException e) {


        logger.error("MethodArgumentNotValidException {}", e.getMessage());
        Error error = new Error();
        error.setCode(HttpStatus.UNPROCESSABLE_ENTITY.value());
        error.setMessage("unable to process the contained instructions");
        error.setErrors(e.getBindingResult().getFieldErrors().stream().map(objectError -> objectError.getField() + " " + objectError.getDefaultMessage()).collect(Collectors.toList()));

        return error;
    }

    @ResponseStatus(value = HttpStatus.NOT_FOUND)
    @ExceptionHandler(ResourceNotFoundException.class)
    public Error notFound(ResourceNotFoundException e) {

        logger.error("ResourceNotFoundException {}", e.getMessage());
        Error error = new Error();
        error.setCode(HttpStatus.NOT_FOUND.value());
        error.setMessage(e.getMessage());

        return error;
    }



    @ResponseStatus(value = HttpStatus.CONFLICT)
    @ExceptionHandler(ResourceAlreadyExistException.class)
    public Error conflict(ResourceAlreadyExistException e) {

        logger.error("ResourceAlreadyExistException {}",e.getMessage());

        Error error = new Error();
        error.setCode(HttpStatus.CONFLICT.value());
        error.setMessage(e.getMessage());

        return error;
    }



    @ResponseStatus(value = HttpStatus.UNPROCESSABLE_ENTITY)
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public Error invalidInput(final HttpMessageNotReadableException e) {

        logger.error("HttpMessageNotReadableException: {}", e.getMessage());

        Throwable rootCause = e;
        while(rootCause.getCause() != null) {
            rootCause = rootCause.getCause();
        }

        final Error error = new Error();
        error.setCode(HttpStatus.UNPROCESSABLE_ENTITY.value());
        error.setMessage(rootCause.getMessage());

        return error;
    }

    @ResponseStatus(value = HttpStatus.UNSUPPORTED_MEDIA_TYPE)
    @ExceptionHandler(MediaTypeNotSupportedException.class)
    public Error globalExceptionHandler(MediaTypeNotSupportedException e ) {
        logger.error("HttpMediaTypeNotSupportedException {}", e.getMessage());

        Error error = new Error();
        error.setCode(HttpStatus.UNSUPPORTED_MEDIA_TYPE.value());
        error.setMessage("Erreur : " + e.getMessage());

        return error;

    }

    @ExceptionHandler(BadCredentialsException.class)
    @ResponseStatus(value = HttpStatus.UNAUTHORIZED)
    public Error handleBadCredentialsException(BadCredentialsException e) {
        // Create a custom error response object if necessary
        String errorMessage = "Email ou mot de passe incorrect!";
        logger.error("BadCredentialsException {}", e.getMessage());

        Error error = new Error();
        error.setCode(HttpStatus.UNAUTHORIZED.value());
        error.setMessage(errorMessage);

        return error;
    }


    /**
     * Refus d'autorisation — <b>403</b>, et non 500.
     *
     * <p>Le fourre-tout {@code @ExceptionHandler(Exception.class)} ci-dessous capturait aussi les
     * {@code AccessDeniedException} : toute defaillance d'autorisation de l'application etait donc
     * rapportee comme une erreur serveur. Deux consequences, l'une pour le client et l'autre pour
     * l'exploitant : le client ne pouvait pas distinguer « interdit » de « le serveur est casse »,
     * et les refus legitimes polluaient les journaux d'erreur au meme titre que de vrais incidents.
     *
     * <p>Meme famille de defaut que le jeton expire qui rendait 500 au lieu de 401 (ADR-0003).
     *
     * <p>Le message est volontairement generique : detailler l'autorite manquante renseignerait
     * l'appelant sur la structure des roles.
     */
    @ResponseStatus(value = HttpStatus.FORBIDDEN)
    @ExceptionHandler(org.springframework.security.access.AccessDeniedException.class)
    public Error accessDenied(org.springframework.security.access.AccessDeniedException e) {
        logger.warn("Acces refuse : {}", e.getMessage());

        Error error = new Error();
        error.setCode(HttpStatus.FORBIDDEN.value());
        error.setMessage("Acces refuse");

        return error;
    }

    /**
     * Statut choisi delibrement par un controleur — <b>respecte</b>, et non aplati en 500.
     *
     * <p>{@code ResponseStatusException} est l'idiome par lequel un controleur dit « je sais
     * exactement quel statut rendre ». Le fourre-tout ci-dessous le capturait aussi : les
     * <b>29</b> statuts deliberes du projet, repartis dans 11 classes, etaient tous rendus en 500
     * — requete malformee, conflit, cle de capteur invalide, ressource absente.
     *
     * <p>Un capteur dont la cle est refusee recevait donc 500 : il ne pouvait pas distinguer « ma
     * cle est mauvaise » de « le serveur est tombe », et reessayait indefiniment. Cote
     * exploitation, chaque requete client malformee apparaissait comme un incident serveur.
     *
     * <p>C'est la troisieme fois que ce fourre-tout masque un statut — jeton expire (ADR-0003),
     * refus d'autorisation ci-dessus — chacun corrige separement. Celui-ci ferme la famille.
     */
    @ExceptionHandler(org.springframework.web.server.ResponseStatusException.class)
    public org.springframework.http.ResponseEntity<Error> deliberateStatus(
            org.springframework.web.server.ResponseStatusException e) {
        // `warn` et non `error` : une requete malformee n'est pas un incident.
        logger.warn("Requete refusee ({}) : {}", e.getStatusCode(), e.getReason());

        Error error = new Error();
        error.setCode(e.getStatusCode().value());
        error.setMessage(e.getReason() == null ? e.getMessage() : e.getReason());

        return org.springframework.http.ResponseEntity.status(e.getStatusCode()).body(error);
    }

    @ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception.class)
    public Error internalError(Exception e) {

        logger.error("InternalError {}", e.getMessage());

        Error error = new Error();
        error.setCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
        error.setMessage("Erreur : " + e.getMessage());

        return error;
    }



    private Error decode(final String data) {

        try {
            return objectMapper.readValue(data, Error.class);
        } catch (final IOException e) {
            logger.warn("Cannot decode error received from distant server.", e);
            return null;
        }
    }

    static class Error {

        private Integer code;
        private String message;
        private List<String> errors;
        private Instant timestamp;
        private  String description;

        Error() {
        }

        public Integer getCode() {
            return code;
        }

        public void setCode(Integer code) {
            this.code = code;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public List<String> getErrors() {
            return errors;
        }

        public void setErrors(List<String> errors) {
            this.errors = errors;
        }

        public Instant getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(Instant timestamp) {
            this.timestamp = timestamp;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }
    }
}
