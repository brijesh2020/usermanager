package com.e4d4.usermanager.service;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Service;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

@Service
public class LoginAttemptService {
	private static final int MAX_ATTEMPT_ALLOWED = 5;
	private static final int ATTEMPT_INCREMENT=1;
	private LoadingCache<String, Integer> loginAttemptCache ;
	
	public LoginAttemptService(){
		loginAttemptCache = CacheBuilder.newBuilder().expireAfterWrite(15,TimeUnit.MINUTES).maximumSize(100)
				.build(new CacheLoader<String,Integer>(){
					@Override
					public Integer load(String key) throws Exception {
						return 0;
					}
				});
	}
	
	public void evictUserFromLoginAttempt(String username) {
		loginAttemptCache.invalidate(username);
	}
	
	public void addUserToLoginAttemptCache(String username) throws ExecutionException {
		int attempt = 0;
		attempt = ATTEMPT_INCREMENT + loginAttemptCache.get(username);
		loginAttemptCache.put(username, attempt);
	}
	
	public boolean hasExceededMaxAttempts(String username) throws ExecutionException {
		return loginAttemptCache.get(username)>= MAX_ATTEMPT_ALLOWED;
	}
}
