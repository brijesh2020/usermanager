package com.e4d4.usermanager.listener;

import java.util.concurrent.ExecutionException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.stereotype.Component;

import com.e4d4.usermanager.service.LoginAttemptService;

@Component
public class AuthenticationFailureListener {
	
	private LoginAttemptService loginAttempService;

	@Autowired
	public AuthenticationFailureListener(LoginAttemptService loginAttempService) {
		super();
		this.loginAttempService = loginAttempService;
	}
	
	@EventListener
	public void onAuthenticationFailure(AuthenticationFailureBadCredentialsEvent event) throws ExecutionException {
		Object principal = event.getAuthentication().getPrincipal();
		if(principal instanceof String) {
			String userName = (String) event.getAuthentication().getPrincipal();
			loginAttempService.addUserToLoginAttemptCache(userName);
		}
	}
	
	
}
