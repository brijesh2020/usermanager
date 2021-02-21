package com.e4d4.usermanager.security;

import java.io.IOException;
import java.util.List;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import static com.e4d4.usermanager.constants.SecurityConstants.*;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpStatus.OK;

@Component
public class JWTAuthorizationFilter extends OncePerRequestFilter{
	private final Logger log = LoggerFactory.getLogger(getClass()); 
	   private JWTTokenProvider jwtTokenProvider;

	    public JWTAuthorizationFilter(JWTTokenProvider jwtTokenProvider) {
	        this.jwtTokenProvider = jwtTokenProvider;
	    }

	    @Override
	    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
	        if (request.getMethod().equalsIgnoreCase(OPTIONS_HTTP_METHOD)) {
	            response.setStatus(OK.value());
	        } else {
	            String authorizationHeader = request.getHeader(AUTHORIZATION);
	            if (authorizationHeader == null || !authorizationHeader.startsWith(TOKEN_PREFIX)) {
	                filterChain.doFilter(request, response);
	                return;
	            }
	            String token = authorizationHeader.substring(TOKEN_PREFIX.length());
	            String username = jwtTokenProvider.getSubject(token);
	            if (jwtTokenProvider.isTokenValid(username, token) && SecurityContextHolder.getContext().getAuthentication() == null) {
	                List<GrantedAuthority> authorities = jwtTokenProvider.getGrantedAuthorities(token);
	                Authentication authentication = jwtTokenProvider.getAuthentication(username, authorities, request);
	                SecurityContextHolder.getContext().setAuthentication(authentication);
	            } else {
	                SecurityContextHolder.clearContext();
	            }
	        }
	        filterChain.doFilter(request, response);
	    }

}
