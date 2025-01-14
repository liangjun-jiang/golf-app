package com.greg.golf.controller;

import java.util.NoSuchElementException;

import com.greg.golf.error.*;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolationException;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {

	@ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
	@ExceptionHandler(TooManyPlayersException.class)
	public ResponseEntity<ApiErrorResponse> handleApiException(TooManyPlayersException ex) {
		var response = new ApiErrorResponse("1", "Round cannot have more than 4 players");
		return new ResponseEntity<>(response, HttpStatus.METHOD_NOT_ALLOWED);
	}

	@ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
	@ExceptionHandler(PlayerAlreadyHasThatRoundException.class)
	public ResponseEntity<ApiErrorResponse> handleApiException(PlayerAlreadyHasThatRoundException ex) {
		var response = new ApiErrorResponse("2", "Cannot save the same round for a player");
		return new ResponseEntity<>(response, HttpStatus.METHOD_NOT_ALLOWED);
	}

	@ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
	@ExceptionHandler(PlayerNickInUseException.class)
	public ResponseEntity<ApiErrorResponse> handleApiException(PlayerNickInUseException ex) {
		var response = new ApiErrorResponse("3", "Player nick already used");
		return new ResponseEntity<>(response, HttpStatus.METHOD_NOT_ALLOWED);
	}

	@ResponseStatus(HttpStatus.BAD_REQUEST)
	@ExceptionHandler(ConstraintViolationException.class)
	public ResponseEntity<ApiErrorResponse> handleApiException(ConstraintViolationException ex) {
		
		var response = new ApiErrorResponse("4", "Bad request: constraint violation");
		return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
	}

	@ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
	@ExceptionHandler(SendingMailFailureException.class)
	public ResponseEntity<ApiErrorResponse> handleApiException(SendingMailFailureException ex) {
		
		var response = new ApiErrorResponse("5", "Error sending email");
		return new ResponseEntity<>(response, HttpStatus.METHOD_NOT_ALLOWED);
	}
	
	@ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
	@ExceptionHandler(NoSuchElementException.class)
	public ResponseEntity<ApiErrorResponse> handleApiException(NoSuchElementException ex) {
		
		var response = new ApiErrorResponse("6", "Data not found");
		return new ResponseEntity<>(response, HttpStatus.METHOD_NOT_ALLOWED);
	}

	@ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
	@ExceptionHandler(TooFewHolesForTournamentException.class)
	public ResponseEntity<ApiErrorResponse> handleApiException(TooFewHolesForTournamentException ex) {
		
		var response = new ApiErrorResponse("8", "18 holes must be played to be addded to tournament");
		return new ResponseEntity<>(response, HttpStatus.METHOD_NOT_ALLOWED);
	}
	
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	@ExceptionHandler(ReCaptchaInvalidException.class)
	public ResponseEntity<ApiErrorResponse> handleApiException(ReCaptchaInvalidException ex) {
		
		var response = new ApiErrorResponse("9", ex.getMessage());
		return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
	}
	
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	@ExceptionHandler(ReCaptchaUnavailableException.class)
	public ResponseEntity<ApiErrorResponse> handleApiException(ReCaptchaUnavailableException ex) {
		
		var response = new ApiErrorResponse("10", ex.getMessage());
		return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
	}
	
	@ResponseStatus(HttpStatus.UNAUTHORIZED)
	@ExceptionHandler(UnauthorizedException.class)
	public ResponseEntity<ApiErrorResponse> handleApiException(UnauthorizedException ex) {
		
		var response = new ApiErrorResponse("11", "Authorization required to perform that operation");
		return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
	}
	
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	@ExceptionHandler(EntityNotFoundException.class)
	public ResponseEntity<ApiErrorResponse> handleApiException(EntityNotFoundException ex) {
		
		var response = new ApiErrorResponse("12", "Data not found");
		return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
	}
	
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ApiErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex) {
		
		var errors = new StringBuilder();
	    ex.getBindingResult().getAllErrors().forEach(error -> {
			String fieldName = ((FieldError) error).getField();
	        String errorMessage = error.getDefaultMessage();
			errors.append(fieldName);
			errors.append(" - ");
	        errors.append(errorMessage);
	        errors.append(" ");
	    });
		
		var response = new ApiErrorResponse("13", "Data validation failed: " + errors);
		return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
	}
	
	@ResponseStatus(HttpStatus.UNAUTHORIZED)
	@ExceptionHandler(BadCredentialsException.class)
	public ResponseEntity<ApiErrorResponse> handleApiException(BadCredentialsException ex) {
		
		var response = new ApiErrorResponse("14", "Incorrect user name or password");
		return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
	}
	
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	@ExceptionHandler(TooShortStringForSearchException.class)
	public ResponseEntity<ApiErrorResponse> handleApiException(TooShortStringForSearchException ex) {
		
		var response = new ApiErrorResponse("15", "Too short string for search");
		return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
	}
	
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	@ExceptionHandler(IllegalArgumentException.class)
	public ResponseEntity<ApiErrorResponse> handleApiException(IllegalArgumentException ex) {
		
		var response = new ApiErrorResponse("16", "Incorrect parameter");
		return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
	}
	
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	@ExceptionHandler(InvalidDataAccessApiUsageException.class)
	public ResponseEntity<ApiErrorResponse> handleApiException(InvalidDataAccessApiUsageException ex) {
		
		var response = new ApiErrorResponse("17", "Incorrect parameter");
		return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
	}
	
	@ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
	@ExceptionHandler(ScoreCardUpdateException.class)
	public ResponseEntity<ApiErrorResponse> handleApiException(ScoreCardUpdateException ex) {
		
		var response = new ApiErrorResponse("18", "Unable to update scorecard");
		return new ResponseEntity<>(response, HttpStatus.METHOD_NOT_ALLOWED);
	}

	@ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
	@ExceptionHandler(DeleteTournamentPlayerException.class)
	public ResponseEntity<ApiErrorResponse> handleApiException(DeleteTournamentPlayerException ex) {

		var response = new ApiErrorResponse("19", "Unable to delete player from tournament. Remove results first.");
		return new ResponseEntity<>(response, HttpStatus.METHOD_NOT_ALLOWED);
	}

	@ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
	@ExceptionHandler(DuplicatePlayerInTournamentException.class)
	public ResponseEntity<ApiErrorResponse> handleApiException(DuplicatePlayerInTournamentException ex) {

		var response = new ApiErrorResponse("20", "Player already added to the tournament.");
		return new ResponseEntity<>(response, HttpStatus.METHOD_NOT_ALLOWED);
	}

	@ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
	@ExceptionHandler(DuplicatePlayerInLeagueException.class)
	public ResponseEntity<ApiErrorResponse> handleApiException(DuplicatePlayerInLeagueException ex) {

		var response = new ApiErrorResponse("21", "Player already added to the league.");
		return new ResponseEntity<>(response, HttpStatus.METHOD_NOT_ALLOWED);
	}

	@ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
	@ExceptionHandler(DuplicateMatchInLeagueException.class)
	public ResponseEntity<ApiErrorResponse> handleApiException(DuplicateMatchInLeagueException ex) {

		var response = new ApiErrorResponse("22", "Match result already exists in the league.");
		return new ResponseEntity<>(response, HttpStatus.METHOD_NOT_ALLOWED);
	}

	@ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
	@ExceptionHandler( MatchResultForNotLeaguePleayerException.class)
	public ResponseEntity<ApiErrorResponse> handleApiException(MatchResultForNotLeaguePleayerException ex) {

		var response = new ApiErrorResponse("23", "Match result cannot be added for player who is not part of the league.");
		return new ResponseEntity<>(response, HttpStatus.METHOD_NOT_ALLOWED);
	}

	@ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
	@ExceptionHandler( PlayerHasMatchException.class)
	public ResponseEntity<ApiErrorResponse> handleApiException(PlayerHasMatchException ex) {

		var response = new ApiErrorResponse("24", "Player who played at least one match cannot be deleted.");
		return new ResponseEntity<>(response, HttpStatus.METHOD_NOT_ALLOWED);
	}

	@ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
	@ExceptionHandler( LeagueClosedException.class)
	public ResponseEntity<ApiErrorResponse> handleApiException(LeagueClosedException ex) {

		var response = new ApiErrorResponse("24", "League must be opened.");
		return new ResponseEntity<>(response, HttpStatus.METHOD_NOT_ALLOWED);
	}

	@ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
	@ExceptionHandler( TeeAlreadyExistsException.class)
	public ResponseEntity<ApiErrorResponse> handleApiException(TeeAlreadyExistsException ex) {

		var response = new ApiErrorResponse("25", "Tee must be unique for a course.");
		return new ResponseEntity<>(response, HttpStatus.METHOD_NOT_ALLOWED);
	}
}
