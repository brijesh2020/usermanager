package com.e4d4.usermanager.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.mail.MessagingException;
import javax.mail.internet.AddressException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.checkerframework.common.reflection.qual.GetClass;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.authentication.Http403ForbiddenEntryPoint;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.e4d4.usermanager.constants.FileConstants;
import com.e4d4.usermanager.constants.Role;
import com.e4d4.usermanager.domain.User;
import com.e4d4.usermanager.domain.UserPrincipal;
import com.e4d4.usermanager.exceptions.EmailExistException;
import com.e4d4.usermanager.exceptions.EmailNotFoundExceptions;
import com.e4d4.usermanager.exceptions.UserNameExistsException;
import com.e4d4.usermanager.repository.UserRepository;

import jdk.jshell.spi.ExecutionControl.UserException;
import net.bytebuddy.utility.RandomString;

@Service
@Transactional
@Qualifier("UserDetailsService")
public class UserService implements UserDetailsService{
	private static final Log logger = LogFactory.getLog(UserService.class);
	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private LoginAttemptService loginAttemptService;
	
	@Autowired
	private BCryptPasswordEncoder encoder ;
	
	@Autowired
	private EmailService emailService;

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		User user = userRepository.findUserByUserName(username);
		if(user==null) throw new UsernameNotFoundException("User Not Found");

		try {
			validateLoginAttempt(user);
		} catch (ExecutionException e) {
			logger.error("Error in loadUserByUsername by name:"+e.getLocalizedMessage());
		}
		user.setLastLoginDateDisplay(user.getLastLoginDate());
		user.setLastLoginDate(new Date());
		userRepository.save(user);
		UserPrincipal up = new UserPrincipal(user);
		return up;
	}
	
	private void validateLoginAttempt(User user) throws ExecutionException {
		if(user.isNotLocked()) {
			if(loginAttemptService.hasExceededMaxAttempts(user.getUserName())) {
				user.setNotLocked(false);
			}else {
				user.setNotLocked(true);
			}
		}else {
			loginAttemptService.evictUserFromLoginAttempt(user.getUserName());
		}  
		
	}

	public User register(String fn,String ln,String username,String email) throws UserNameExistsException, EmailExistException, AddressException, MessagingException {
		validateUsernameAndEmail("",username,email);
		User u = new User();
		u.setUserId(generateID());
		String pass = generatePassword();
		String encodedPassword = encodePassword(pass);
		u.setFirstName(fn);
		u.setLastName(ln);
		u.setUserName(username);
		u.setEmail(email);
		u.setJoinDate(new Date());
		u.setPassword(encodedPassword);
		u.setActive(true);
		u.setNotLocked(true);
		u.setRoles(Role.ROLE_USER.name());
		u.setAuthorities(Role.ROLE_USER.getAuthorities());
		u.setProfileImageUrl(getTemporaryImageUrl(username));
		userRepository.save(u);
		logger.info("pass:"+pass);
		//emailService.sendNewPasswordEmail(u.getFirstName(),u.getPassword(), u.getEmail());
		return u;
	}

	private String encodePassword(String pass) {
		return encoder.encode(pass);
	}

	private String generateID() {
		return   new RandomString().nextString();
	}

	private String generatePassword() {
		return   new RandomString().nextString();
	}

	private User validateUsernameAndEmail(String currentUser,String username, String email) throws UserNameExistsException, EmailExistException {
		if(!ObjectUtils.isEmpty(currentUser)) {
			User curUser = findUserByUsername(currentUser);
			if(curUser==null) throw new UsernameNotFoundException("No User By This Name: "+currentUser);
			
			User newUser = findUserByUsername(username);
			if(newUser!=null &&  curUser.getId().equals(newUser.getId())) {
				throw new UserNameExistsException("UserName Already Exists");
			}
			
			User emailOfNewUser = findUserByEmail(email);
			if(emailOfNewUser!=null &&  curUser.getId().equals(emailOfNewUser.getId())) {
				throw new EmailExistException("Email Already Exists");
			}
			
			return curUser;
		}else {
			User newUser = findUserByUsername(username);
			if(newUser!=null) {
				throw new UserNameExistsException("UserName Already Exists");
			}
			User emailOfNewUser = findUserByEmail(email);
			if(emailOfNewUser!=null ) {
				throw new EmailExistException("Email Already Exists");
			}
			return newUser;
		}
		
		
	}

	public List<User> getUsers( ) {
		return userRepository.findAll();
	}

	public User findUserByUsername( String username ) {
		return userRepository.findUserByUserName(username);
	}
	
	public User findUserByEmail( String email ) {
		return userRepository.findByEmail(email);
	}
		
	public User addUser(String firstName, String lastName, String userName,String email, String role,boolean isNonLocked, 
			boolean isActive ,MultipartFile profileImage) throws UserNameExistsException, EmailExistException, IOException {
		validateUsernameAndEmail("",userName,email);
		User u = new User();
		String pass = generatePassword();
		String encodedPassword = encodePassword(pass);
		u.setFirstName(firstName);
		u.setLastName(lastName);
		u.setUserName(userName);
		u.setEmail(email);
		u.setJoinDate(new Date());
		u.setPassword(encodedPassword);
		u.setActive(true);
		u.setNotLocked(true);
		Role  r = getRoles(role);
		u.setRoles(r.name());
		u.setAuthorities( r.getAuthorities());
		u.setProfileImageUrl(getTemporaryImageUrl(userName));
		saveProfileImage(u,profileImage);
		userRepository.save(u);
		return u;

	}
	
	private void saveProfileImage(User user , MultipartFile profileImage) throws IOException {
		if(profileImage!=null) {
			Path userFolder = Paths.get(FileConstants.USER_FOLDER+user.getUserName()).toAbsolutePath().normalize();
			if(!Files.exists(userFolder)) {
				Files.createDirectories(userFolder);
				logger.info(FileConstants.DIRECTORY_CREATED);
			}
			Files.deleteIfExists(Paths.get(userFolder+user.getUserName()+FileConstants.DOT+FileConstants.JPG_EXTENSION ));
			Files.copy(profileImage.getInputStream(), userFolder.resolve(user.getUserName()+FileConstants.DOT+FileConstants.JPG_EXTENSION
					),StandardCopyOption.REPLACE_EXISTING);
			user.setProfileImageUrl(setProfileImageUrl(user.getUserName()));
			userRepository.save(user);
		}
	}

	private String setProfileImageUrl(String userName) {
		 	return ServletUriComponentsBuilder.fromCurrentContextPath().path(userName+FileConstants.DOT+FileConstants.JPG_EXTENSION ).toUriString();
	}

	private String getTemporaryImageUrl(String userName) {
		return ServletUriComponentsBuilder.fromCurrentContextPath().path(FileConstants.DEFAULT_USER_IMAGE_PATH).toUriString();
	}

	private  Role  getRoles(String role) {
		return Role.valueOf(role.toUpperCase());
	}

	public User updateUser(String currentUserName,String firstName, String lastName, String userName,String email, String role,boolean isNonLocked, 
			boolean isActive ,MultipartFile profileImage) throws UserNameExistsException, EmailExistException, IOException {

		User userUpdatingInfo = validateUsernameAndEmail(currentUserName,userName,email);
		userUpdatingInfo.setFirstName(firstName);
		userUpdatingInfo.setLastName(lastName);
		userUpdatingInfo.setUserName(userName);
		userUpdatingInfo.setEmail(email);
		userUpdatingInfo.setActive(true);
		userUpdatingInfo.setNotLocked(true);
		userUpdatingInfo.setRoles(getRoles(role).name());
		 
		userUpdatingInfo.setAuthorities(getRoles(role).getAuthorities());
		userUpdatingInfo.setProfileImageUrl(getTemporaryImageUrl(userName));
		userRepository.save(userUpdatingInfo);
	
		saveProfileImage(userUpdatingInfo,profileImage);
		return userUpdatingInfo;
	}
	
	public void deleteUser(long id) {
		userRepository.deleteById(id);
	}
	
	public void resetPassword(String email) throws EmailNotFoundExceptions, AddressException, MessagingException {
		User u = userRepository.findByEmail(email);
		if(u==null) {
			throw new EmailNotFoundExceptions("Email Not Found");
		}else {
			String pass = generatePassword();
			String encodedPassword = encodePassword(pass);
			u.setPassword(encodedPassword);
			userRepository.save(u);
			emailService.sendNewPasswordEmail(u.getFirstName(), encodedPassword, u.getEmail());
			
		}
	}
	
	public User updateProfileImage(String username,MultipartFile file) throws UserNameExistsException, EmailExistException, IOException {
		User user = validateUsernameAndEmail( username, null,null);
		saveProfileImage(user, file);
		return user;
	}
}
