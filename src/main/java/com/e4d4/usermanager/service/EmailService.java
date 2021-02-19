package com.e4d4.usermanager.service;

import static com.e4d4.usermanager.constants.EmailConstants.DEFAULT_PORT;
import static com.e4d4.usermanager.constants.EmailConstants.GMAIL_SMTP_SERVER;
import static com.e4d4.usermanager.constants.EmailConstants.SMTP_AUTH;
import static com.e4d4.usermanager.constants.EmailConstants.SMTP_HOST;
import static com.e4d4.usermanager.constants.EmailConstants.SMTP_PORT;
import static com.e4d4.usermanager.constants.EmailConstants.SMTP_STARTTLS_ENABLED;
import static com.e4d4.usermanager.constants.EmailConstants.SMTP_STARTTLS_REQUIRED;

import java.util.Date;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.springframework.stereotype.Service;

import com.e4d4.usermanager.constants.EmailConstants;
import com.sun.mail.smtp.SMTPTransport;

@Service
public class EmailService {
	
	public void sendNewPasswordEmail(String fn, String pass, String email) throws AddressException, MessagingException {
		Message msg = createMessage(fn, pass, email);
		SMTPTransport smtpTransport = (SMTPTransport) getEmailSession().getTransport(EmailConstants.SIMPLE_MAIL_TRANSFER_PROTOCOL);
		smtpTransport.connect(EmailConstants.GMAIL_SMTP_SERVER, EmailConstants.USERNAME, EmailConstants.PASSWORD);
		smtpTransport.sendMessage(msg, msg.getAllRecipients());
		smtpTransport.close();
	}
	
	private Message createMessage(String fn, String pass, String email) throws AddressException, MessagingException {
		Message msg = new MimeMessage(getEmailSession());
		msg.setFrom(new InternetAddress(EmailConstants.FROM_EMAIL));
		msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(email,false));
		msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(EmailConstants.CC_EMAIL,false));
		msg.setSubject(EmailConstants.EMAIL_SUBJECT);
		msg.setText("Hello "+fn+ "\n \n Your new account password is :"+pass+" \n\n The Support Team");
		msg.setSentDate(new Date());
		msg.saveChanges();
		return msg;
	}
	
	private Session getEmailSession() {
		Properties prop = System.getProperties();
		prop.put(SMTP_HOST,GMAIL_SMTP_SERVER);
		prop.put(SMTP_AUTH,true);
		prop.put(SMTP_PORT,DEFAULT_PORT);
		prop.put(SMTP_STARTTLS_ENABLED, true);
		prop.put(SMTP_STARTTLS_REQUIRED,true);
		return Session.getInstance(prop);
	}
}
