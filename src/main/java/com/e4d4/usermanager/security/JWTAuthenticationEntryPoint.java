package com.e4d4.usermanager.security;

import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.Http403ForbiddenEntryPoint;
import org.springframework.stereotype.Component;

import com.e4d4.usermanager.constants.SecurityConstants;
import com.e4d4.usermanager.domain.HttpResponse;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class JWTAuthenticationEntryPoint extends Http403ForbiddenEntryPoint {
	private static final Log logger = LogFactory.getLog(Http403ForbiddenEntryPoint.class);
	
	@Override
	public void commence(HttpServletRequest req, HttpServletResponse res,AuthenticationException excep) throws IOException {
		HttpResponse customResponse  = new HttpResponse(HttpStatus.FORBIDDEN.value(),HttpStatus.FORBIDDEN,HttpStatus.FORBIDDEN.getReasonPhrase().toUpperCase(),
					SecurityConstants.FORBIDDEN_MESSAGE);
		res.setContentType(MediaType.APPLICATION_JSON_VALUE);
		res.setStatus(HttpStatus.FORBIDDEN.value());
		OutputStream os = res.getOutputStream();
		ObjectMapper om = new ObjectMapper();
		om.writeValue(os, customResponse);
		os.flush();
	}

}
