package ucg.collecte.master.exception;

import lombok.Getter;

import java.time.Instant;


@Getter
public record ErrorMessage(int statusCode, Instant timestamp, String message, String description) {


}
