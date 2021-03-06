package com.e4d4.usermanager.exceptions;

import java.io.IOException;
import java.util.Objects;

import javax.persistence.NoResultException;
import javax.security.auth.login.AccountLockedException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

import com.auth0.jwt.exceptions.TokenExpiredException;
import com.e4d4.usermanager.domain.HttpResponse;


@RestControllerAdvice
public class ExceptionHandling implements ErrorController{
	private final Logger log = LoggerFactory.getLogger(getClass()); 
	
	private static final String ACCOUNT_LOCKED="ACCOUNT_LOCKED";
	private static final String METHOD_IS_NOT_ALLOWED="METHOD_IS_NOT_ALLOWED";
	
	private static final String INTERNAL_SERVER_ERROR_MSG="INTERNAL_SERVER_ERROR_MSG";
	private static final String INCORRECT_CREDENTIALS="INCORRECT_CREDENTIALS";
	private static final String ACCOUNT_DISABLED="ACCOUNT_DISABLED";
	private static final String ERROR_PROCESSING_FILE="ERROR_PROCESSING_FILE";
	private static final String NOT_ENOUGH_PERMISSION="NOT_ENOUGH_PERMISSION";
	
	private static final String ERROR_PATH="/error";
	
	@ExceptionHandler(DisabledException.class)
	public ResponseEntity<HttpResponse> accountDisabledException(){
		return createHttpResponse(HttpStatus.BAD_REQUEST, ACCOUNT_LOCKED);
	} 
	
	@ExceptionHandler(BadCredentialsException.class)
	public ResponseEntity<HttpResponse> badCredentailsException(){
		return createHttpResponse(HttpStatus.BAD_REQUEST, INCORRECT_CREDENTIALS);
	}
	
	@ExceptionHandler(AccessDeniedException.class)
	public ResponseEntity<HttpResponse> accessDeniedException(){
		return createHttpResponse(HttpStatus.FORBIDDEN, NOT_ENOUGH_PERMISSION);
	}
	
	@ExceptionHandler(LockedException.class)
	public ResponseEntity<HttpResponse> lockedException(){
		return createHttpResponse(HttpStatus.UNAUTHORIZED, ACCOUNT_LOCKED);
	}
	
	@ExceptionHandler(TokenExpiredException.class)
	public ResponseEntity<HttpResponse> tokenExpiredException(TokenExpiredException exception){
		return createHttpResponse(HttpStatus.UNAUTHORIZED, exception.getMessage().toUpperCase());
	}
	
	@ExceptionHandler(EmailExistException.class)
	public ResponseEntity<HttpResponse> emailExistException(EmailExistException exception){
		return createHttpResponse(HttpStatus.BAD_REQUEST, exception.getMessage().toUpperCase());
	}
	
	@ExceptionHandler(UserNameExistsException.class)
	public ResponseEntity<HttpResponse> usernameExistException(UserNameExistsException exception){
		return createHttpResponse(HttpStatus.BAD_REQUEST, exception.getMessage().toUpperCase());
	}
	
	@ExceptionHandler(EmailNotFoundExceptions.class)
	public ResponseEntity<HttpResponse> emailNotFoundException(EmailNotFoundExceptions exception){
		return createHttpResponse(HttpStatus.BAD_REQUEST, exception.getMessage().toUpperCase());
	}
	
	
	@ExceptionHandler(HttpRequestMethodNotSupportedException.class)
	public ResponseEntity<HttpResponse> methodNotSupportedException(HttpRequestMethodNotSupportedException exception){
		HttpMethod supportedMethod=Objects.requireNonNull(exception.getSupportedHttpMethods()).iterator().next();
		return createHttpResponse(HttpStatus.METHOD_NOT_ALLOWED,String.format(METHOD_IS_NOT_ALLOWED, supportedMethod));
	}
	
	/*@ExceptionHandler(NoHandlerFoundException.class)
	public ResponseEntity<HttpResponse> noHandlerFoundException(NoHandlerFoundException exception){
		 	return createHttpResponse(HttpStatus.BAD_REQUEST, "No Page Found.");
	}*/
	
	@ExceptionHandler(Exception.class)
	public ResponseEntity<HttpResponse> internalServerErrorException(Exception exception){
		return createHttpResponse(HttpStatus.INTERNAL_SERVER_ERROR, INTERNAL_SERVER_ERROR_MSG);
	}
	
	@ExceptionHandler(NoResultException.class)
	public ResponseEntity<HttpResponse>  notFoundException(NoResultException exception){
		return createHttpResponse(HttpStatus.NOT_FOUND, exception.getMessage().toUpperCase());
	}
	
	
	private ResponseEntity<HttpResponse> createHttpResponse(HttpStatus httpStatus,String message){
		HttpResponse response = new HttpResponse(httpStatus.value(),httpStatus,httpStatus.getReasonPhrase().toUpperCase(),message);
		return new ResponseEntity<>(response,httpStatus);
	}
	
	@RequestMapping(ERROR_PATH)
	public ResponseEntity<HttpResponse> 	notFound404(IOException exception){
		return createHttpResponse(HttpStatus.NOT_FOUND, "Page Not Found.");
	}
	

	@Override
	public String getErrorPath() {
		return ERROR_PATH;
	}

}
