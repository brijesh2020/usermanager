package com.e4d4.usermanager.resource;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import javax.mail.MessagingException;
import javax.mail.internet.AddressException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.e4d4.usermanager.constants.FileConstants;
import com.e4d4.usermanager.constants.SecurityConstants;
import com.e4d4.usermanager.domain.User;
import com.e4d4.usermanager.domain.UserPrincipal;
import com.e4d4.usermanager.exceptions.EmailExistException;
import com.e4d4.usermanager.exceptions.EmailNotFoundExceptions;
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
			@RequestParam(value = "profileImage", required=false) MultipartFile profileImage) throws UserNameExistsException, EmailExistException, IOException  {
	
		User user = service.addUser(firstName, lastName, userName, email, role, Boolean.parseBoolean(isNonLocked), Boolean.parseBoolean(isActive), profileImage);
		return new ResponseEntity<>(user, HttpStatus.CREATED);
	}
	
	@PostMapping("/update")
	public  ResponseEntity<User> update(@RequestParam("currentUserName") String currentUserName,@RequestParam("firstName") String firstName, @RequestParam("lastName") String lastName,
			@RequestParam("userName") String userName,@RequestParam("email") String email ,@RequestParam("role") String role,
			@RequestParam("isActive") String isActive,@RequestParam("isNonLocked") String isNonLocked,
			@RequestParam(value = "profileImage", required=false) MultipartFile profileImage) throws UserNameExistsException, EmailExistException, IOException  {
	
		User user = service.updateUser(currentUserName,firstName, lastName, userName, email, role, Boolean.parseBoolean(isNonLocked), Boolean.parseBoolean(isActive), profileImage);
		return new ResponseEntity<>(user, HttpStatus.OK);
	}
	
	@GetMapping("/find/{username}")
	public ResponseEntity<User> find( @PathVariable("username") String username)   {
		User user = service.findUserByUsername(username);
		return new ResponseEntity<>(user,HttpStatus.OK);
	}
	
	@GetMapping("/resetpassword/{email}")
	public ResponseEntity<String> resetpassword( @PathVariable("email") String email) throws AddressException, EmailNotFoundExceptions, MessagingException   {
		service.resetPassword(email);
		return new ResponseEntity<>("Email sent to:"+email,HttpStatus.OK);
	}
	
	@DeleteMapping("/delete/{id}")
	@PreAuthorize("hasAnyAuthority('user:delete')")
	public ResponseEntity<String> delete( @PathVariable("id") long id)  {
		service.deleteUser(id);
		return new ResponseEntity<>("User Deleted:"+id,HttpStatus.OK);
	}
	
	@PostMapping("/updateprofileimage")
	public  ResponseEntity<User> updateprofileimage(@RequestParam("username") String username ,
			@RequestParam(value = "profileImage" ) MultipartFile profileImage) throws UserNameExistsException, EmailExistException, IOException  {
	
		User user = service.updateProfileImage(username, profileImage);
		return new ResponseEntity<>(user, HttpStatus.OK);
	}
	
	@GetMapping(path="/image/{username}/{fileName}", produces = MediaType.IMAGE_JPEG_VALUE)
	public byte[] getProfileImage( @PathVariable("username") String username,@PathVariable("fileName") String fileName) throws IOException   {
		User user = service.findUserByUsername(username);
		Path userFolder = Paths.get(FileConstants.USER_FOLDER+user.getUserName()+FileConstants.FORWARD_SLASH+fileName) ;
		return Files.readAllBytes(userFolder);
	}
	
	@GetMapping(path="/image/{username}/{fileName}", produces = MediaType.IMAGE_JPEG_VALUE)
	public byte[] getTempProfileImage( @PathVariable("username") String username) throws IOException   {
		URL url = new URL(FileConstants.TEMP_PROFILE_IMAGE_BASE_PATH+username);
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		try (InputStream is = url.openStream()){
			int bytesRead;
			byte[] chunk = new byte[1024];
			while ((bytesRead= is.read(chunk))>0) {
				stream.write(chunk,0,bytesRead);
			}
		}catch(Exception e) {
			log.error("Error while fetching temporary image:"+e.getLocalizedMessage());
		}
		 return  stream.toByteArray();
	}
	
}
