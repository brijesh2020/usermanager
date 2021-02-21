package com.e4d4.usermanager.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.stereotype.Component;

import com.e4d4.usermanager.domain.UserPrincipal;
import com.e4d4.usermanager.service.LoginAttemptService;

@Component
public class AuthenticationSuccessListener {
	private final Logger log = LoggerFactory.getLogger(getClass()); 
	private LoginAttemptService loginAttempService;

	@Autowired
	public AuthenticationSuccessListener(LoginAttemptService service) {
		super();
		this.loginAttempService = service;
	}
	
	@EventListener
	public void onAuthenticationSuccess(AuthenticationSuccessEvent event) {
	 
		Object principal = event.getAuthentication().getPrincipal();
		if(principal instanceof UserPrincipal) {
			UserPrincipal p =   (UserPrincipal) event.getAuthentication().getPrincipal();
			loginAttempService.evictUserFromLoginAttempt(p.getUsername());
		}
	}
}
