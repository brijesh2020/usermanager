package com.e4d4.usermanager.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.e4d4.usermanager.domain.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long>{
	User findUserByUserName(String username);
	
	User findByEmail(String email);
}
