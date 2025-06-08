package com.ax14n.soundbeat.servidor.dto;

import java.time.LocalDate;

public class UserDTO {
	private String _username;
	private LocalDate _dateJoined;

	public String getUsername() {
		return _username;
	}

	public void setUsername(String username) {
		this._username = username;
	}

	public LocalDate getDateJoined() {
		return _dateJoined;
	}

	public void setDateJoined(LocalDate dateJoined) {
		this._dateJoined = dateJoined;
	}
}
