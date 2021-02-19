package com.e4d4.usermanager;

import java.io.File;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.e4d4.usermanager.constants.FileConstants;

@SpringBootApplication
public class UsermanagerApplication {

	public static void main(String[] args) {
		SpringApplication.run(UsermanagerApplication.class, args);
		new File(FileConstants.USER_FOLDER).mkdirs();
	}

}
