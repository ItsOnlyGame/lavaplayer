package com.sedmelluq.discord.lavaplayer.source.spotify.Handlers;

import com.sedmelluq.discord.lavaplayer.source.spotify.AudioTrackFactory;
import com.sedmelluq.discord.lavaplayer.source.spotify.GetAudioTrackInfo;
import com.sedmelluq.discord.lavaplayer.source.spotify.SpotifyAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import com.wrapper.spotify.SpotifyApi;
import com.wrapper.spotify.exceptions.SpotifyWebApiException;
import com.wrapper.spotify.model_objects.specification.Track;
import org.apache.commons.codec.binary.StringUtils;
import org.apache.hc.core5.http.ParseException;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

public class SpotifyTrackLoader extends GetAudioTrackInfo {

	public AudioTrack handle(URL url, AudioTrackFactory audioTrackFactory, SpotifyAudioSourceManager sourceManager, SpotifyApi api) {
		Path path = Paths.get(url.getPath());

		if (path.getNameCount() < 2) {
			return null;
		}

		if (!StringUtils.equals(path.getName(0).toString(), "track")) {
			return null;
		}

		if (!url.getPath().startsWith("/track/")) return null;
		String trackId = getId(url.getPath());

		Track track;
		try {
			track = api.getTrack(trackId).build().execute();
		} catch (IOException | SpotifyWebApiException | ParseException e) {
			throw new IllegalStateException("Unable to fetch track from Spotify API.", e);
		}
		AudioTrackInfo songMetadata = getAudioTrackInfo(track);
		return audioTrackFactory.getAudioTrack(songMetadata, sourceManager);
	}


}
