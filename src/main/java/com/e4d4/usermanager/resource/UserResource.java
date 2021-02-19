package com.e4d4.usermanager.resource;

import java.util.List;

import javax.mail.MessagingException;
import javax.mail.internet.AddressException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.e4d4.usermanager.constants.SecurityConstants;
import com.e4d4.usermanager.domain.User;
import com.e4d4.usermanager.domain.UserPrincipal;
import com.e4d4.usermanager.exceptions.EmailExistException;
import com.e4d4.usermanager.exceptions.ExceptionHandling;
import com.e4d4.usermanager.exceptions.UserNameExistsException;
import com.e4d4.usermanager.security.JWTTokenProvider;
import com.e4d4.usermanager.service.UserService;

@RestController
@RequestMapping(path = {"/", "/user"})
public class UserResource extends ExceptionHandling{
	private final Logger log = LoggerFactory.getLogger(getClass()); 
	
	@Autowired
	private UserService service;
	
	@Autowired
	private AuthenticationManager authManager;
	
	@Autowired
	private JWTTokenProvider jwtTokenProvider; 
	
	
	@PostMapping("/register")
	public ResponseEntity<User> register(@RequestBody User user) throws UserNameExistsException, EmailExistException, AddressException, MessagingException {
		log.debug("User:{}",user);
		User newUser = service.register(user.getFirstName(), user.getLastName(), user.getUserName(), user.getEmail());
		return new ResponseEntity<>(newUser,HttpStatus.OK);
	}
	
	@GetMapping("/getusers")
	public ResponseEntity<List<User>> getUser( )   {
		List<User> users = service.getUsers();
		return new ResponseEntity<>(users,HttpStatus.OK);
	}
	
	@PostMapping("/login")
	public ResponseEntity<User> login(@RequestBody User user)  {
		authenticateUser(user.getUserName(),user.getPassword());
		User loginUser = service.findUserByUsername( user.getUserName());
		UserPrincipal userPrincipal = new UserPrincipal(loginUser);
		HttpHeaders jwtHeader = getJwtHeader(userPrincipal);
		return new ResponseEntity<>(loginUser,jwtHeader,HttpStatus.OK);
	}

	private HttpHeaders getJwtHeader(UserPrincipal userPrincipal) {
		HttpHeaders header = new HttpHeaders();
		header.add(SecurityConstants.TOKEN_PREFIX, jwtTokenProvider.generateToken(userPrincipal));
		return header;
	}

	private void authenticateUser(String userName, String password) {
		 authManager.authenticate(new UsernamePasswordAuthenticationToken(userName, password));
	}
	
	@PostMapping("/add")
	public  ResponseEntity<User> add(@RequestParam("firstName") String firstName, @RequestParam("lastName") String lastName,
			@RequestParam("userName") String userName,@RequestParam("email") String email ,@RequestParam("role") String role,
			@RequestParam("isActive") String isActive,@RequestParam("isNonLocked") String isNonLocked,
			@RequestParam(value = "profileImage", required=false) MultipartFile profileImage)  {
	
		User user = service.addUser(firstName, lastName, userName, email, role, Boolean.parseBoolean(isNonLocked), Boolean.parseBoolean(isActive), profileImage);

}
