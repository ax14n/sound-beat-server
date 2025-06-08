package com.ax14n.soundbeat.servidor.dto;

import java.util.List;

public class SongDTO {
	private int songId;
	private String title;
	private String artist;
	private String url;
	private Integer duration;
	private List<String> genres;

	public String getAuthor() {
		return artist;
	}

	public List<String> getGenres() {
		return genres;
	}

	public String getName() {
		return title;
	}

	public int getSongId() {
		return songId;
	}

	public String getUrl() {
		return url;
	}

	public Integer getDuration() {
		return duration;
	}

	public void setAuthor(String author) {
		this.artist = author;
	}

	public void setGenres(List<String> genres) {
		this.genres = genres;
	}

	public void setName(String name) {
		this.title = name;
	}

	public void setSongId(int songId) {
		this.songId = songId;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public void setDuration(Integer duration) {
		this.duration = duration;
	}
}
