package com.example.research.exception;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

	private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ApiErrorResponse> handleValidationException(
			MethodArgumentNotValidException exception) {
		Map<String, String> validationErrors = new LinkedHashMap<>();
		exception.getBindingResult().getFieldErrors().forEach(fieldError ->
				validationErrors.putIfAbsent(
						fieldError.getField(),
						fieldError.getDefaultMessage() == null
								? "Invalid value"
								: fieldError.getDefaultMessage()));

		ApiErrorResponse response = new ApiErrorResponse(
				Instant.now(),
				HttpStatus.BAD_REQUEST.value(),
				HttpStatus.BAD_REQUEST.getReasonPhrase(),
				"Request validation failed",
				validationErrors);

		return ResponseEntity.badRequest().body(response);
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ApiErrorResponse> handleUnexpectedException(Exception exception) {
		LOGGER.error("Unexpected error while processing request", exception);

		ApiErrorResponse response = new ApiErrorResponse(
				Instant.now(),
				HttpStatus.INTERNAL_SERVER_ERROR.value(),
				HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
				"An unexpected error occurred",
				Map.of());

		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
	}
}
