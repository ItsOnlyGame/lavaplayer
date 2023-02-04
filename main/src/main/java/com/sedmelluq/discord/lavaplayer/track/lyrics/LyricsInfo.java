package com.sedmelluq.discord.lavaplayer.track.lyrics;

public class LyricsInfo {

	private final String lyrics;
	private final String url;
	private final boolean legacy;

	public LyricsInfo(String lyrics, String url, boolean legacy) {
		this.lyrics = lyrics;
		this.url = url;
		this.legacy = legacy;
	}

	public String getLyrics() {
		return lyrics;
	}

	public String getUrl() {
		return url;
	}

	public boolean isLegacy() {
		return legacy;
	}

	@Override
	public String toString() {
		return super.toString() + "\n" + this.lyrics + "\n" + this.url + "\n";
	}
}
