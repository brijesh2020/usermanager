package com.e4d4.usermanager.constants;

public class SecurityConstants {
	public static final long EXPIRATION_TIME=432_000_000;//5 DAYS
	public static final String TOKEN_PREFIX="Bearer ";
	public static final String JWT_TOKEN_HEADER="Jwt-Token";
	public static final String TOKEN_CANNOT_BE_VERIFIED = "token cannot be verified";
	
	public static final String GET_ARRAYS_LLC="Get Arrays, LLC ";
	public static final String GET_ARRAYS_ADMINISTRATION="User Management Portal";
	public static final String AUTHORITIES = "Authorities";
	
	public static final String FORBIDDEN_MESSAGE="You need to Login to access this page. ";
	public static final String ACCESS_DENIED_MESSAGE="You do not have access to this page";
	public static final String OPTIONS_HTTP_METHOD = "OPTIONS";
	
	public static final String[] PUBLIC_URLS = {"/user/login","/user/register","user/resetpassword/**","/user/images/**"};
	//public static final String[] PUBLIC_URLS = { "/**" };
	
}
