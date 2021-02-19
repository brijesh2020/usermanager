package com.e4d4.usermanager.security;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.e4d4.usermanager.constants.SecurityConstants;
import com.e4d4.usermanager.domain.UserPrincipal;

@Component
public class JWTTokenProvider {
	@Value("${jwt.secret}")
	private String secret;
	
	public String generateToken(UserPrincipal up) {
		String[] claims = getClaims(up);
		return JWT.create().withIssuer(SecurityConstants.GET_ARRAYS_LLC).withAudience(SecurityConstants.GET_ARRAYS_ADMINISTRATION)
				.withIssuedAt(new Date()).withSubject(up.getUsername()).withArrayClaim(SecurityConstants.AUTHORITIES, claims)
				.withExpiresAt(new Date(System.currentTimeMillis()+SecurityConstants.EXPIRATION_TIME)).sign(Algorithm.HMAC512(secret.getBytes()));
	}

	private String[] getClaims(UserPrincipal up) {
		List<String> result = new ArrayList<>();
		for(GrantedAuthority ga : up.getAuthorities()) {
			result.add(ga.getAuthority());
		}
		return result.toArray(new String[0]);
	}
	
	public List<GrantedAuthority> getGrantedAuthorities(String token) {
		String[] claims = getClaimsFromToken(token);
		return Arrays.stream(claims).map(SimpleGrantedAuthority::new).collect(Collectors.toList());
	}

	private String[] getClaimsFromToken(String token) {
		JWTVerifier verifier = getJWTVerifier();
		return verifier.verify(token).getClaim(SecurityConstants.AUTHORITIES).asArray(String.class);
	}
	
	public Authentication getAuthentication(String userName,List<GrantedAuthority> authorities,HttpServletRequest request ) {
		UsernamePasswordAuthenticationToken usPassToken = new UsernamePasswordAuthenticationToken(userName, authorities);
		usPassToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
		return usPassToken;
	}
	
	public boolean isTokenValid(String username,String token) {
		JWTVerifier verifier = getJWTVerifier();
		return ObjectUtils.isEmpty(username) && !isTokenExpired(verifier, token); 
	}

	private JWTVerifier getJWTVerifier() {
		JWTVerifier verifier = null;
		try {
			Algorithm algo = Algorithm.HMAC512(secret.getBytes());
			verifier = JWT.require(algo).withIssuer(SecurityConstants.GET_ARRAYS_LLC).build();
		}catch(Exception e) { 
			e.printStackTrace();
		}
		return verifier;
	}
	
	private boolean isTokenExpired(JWTVerifier verifier,String token) {
		Date expirationDate = verifier.verify(token).getExpiresAt();
		return expirationDate.before(new Date());
	}
	
	public String getSubject(String token) {
		JWTVerifier verifier = getJWTVerifier();
		return verifier.verify(token).getSubject();
	}
}
