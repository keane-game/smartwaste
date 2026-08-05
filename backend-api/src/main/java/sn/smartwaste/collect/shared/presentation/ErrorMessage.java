package sn.smartwaste.collect.shared.presentation;

import lombok.Getter;

import java.time.Instant;



public record ErrorMessage(int statusCode, Instant timestamp, String message, String description) {


}
