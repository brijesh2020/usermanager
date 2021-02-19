package com.e4d4.usermanager.domain;

import java.util.Date;

import org.springframework.http.HttpStatus;

import com.fasterxml.jackson.annotation.JsonFormat;

public class HttpResponse {
	private int httpStatusCode;
	private HttpStatus httpStatus;
	private String reason;
	private String message;
	
	@JsonFormat(shape=JsonFormat.Shape.STRING, pattern = "MM-dd-yyyy hh:mm:ss",timezone = "America/New_York")
	private Date timesStamp;
	
	public HttpResponse() {
		super();
		// TODO Auto-generated constructor stub
	}
	public HttpResponse(int httpStatusCode, HttpStatus httpStatus, String reason, String message) {
		super();
		this.httpStatusCode = httpStatusCode;
		this.httpStatus = httpStatus;
		this.reason = reason;
		this.message = message;
		this.timesStamp = new Date();
	}
	public int getHttpStatusCode() {
		return httpStatusCode;
	}
	public void setHttpStatusCode(int httpStatusCode) {
		this.httpStatusCode = httpStatusCode;
	}
	public HttpStatus getHttpStatus() {
		return httpStatus;
	}
	public void setHttpStatus(HttpStatus httpStatus) {
		this.httpStatus = httpStatus;
	}
	public String getReason() {
		return reason;
	}
	public void setReason(String reason) {
		this.reason = reason;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public Date getTimesStamp() {
		return timesStamp;
	}
	public void setTimesStamp(Date timesStamp) {
		this.timesStamp = timesStamp;
	}
	
}
