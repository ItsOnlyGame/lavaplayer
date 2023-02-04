package com.sedmelluq.discord.lavaplayer.source.spotify.Handlers;

import com.sedmelluq.discord.lavaplayer.source.spotify.AudioTrackFactory;
import com.sedmelluq.discord.lavaplayer.source.spotify.GetAudioTrackInfo;
import com.sedmelluq.discord.lavaplayer.source.spotify.SpotifyAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.track.AudioItem;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import com.sedmelluq.discord.lavaplayer.track.BasicAudioPlaylist;
import com.wrapper.spotify.SpotifyApi;
import com.wrapper.spotify.exceptions.SpotifyWebApiException;
import com.wrapper.spotify.model_objects.specification.Artist;
import com.wrapper.spotify.model_objects.specification.Track;
import org.apache.hc.core5.http.ParseException;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

public class SpotifyArtistLoader extends GetAudioTrackInfo {

	public AudioItem handle(URL url, AudioTrackFactory audioTrackFactory, SpotifyAudioSourceManager sourceManager, SpotifyApi api) throws Exception {
		if (!url.getPath().startsWith("/artist/")) return null;

		Artist artist = api.getArtist(getId(url.getPath())).build().execute();

		Track[] artistTopTracks = null;
		try {
			artistTopTracks = api.getArtistsTopTracks(getId(url.getPath()), sourceManager.getCountryCode()).build().execute();
		} catch (IOException | SpotifyWebApiException | ParseException e) {
			e.printStackTrace();
		}

		if (artistTopTracks == null) return null;

		List<AudioTrackInfo> songMetadata = getAudioTrack(Arrays.asList(artistTopTracks));
		List<AudioTrack> audioTracks = audioTrackFactory.getAudioTracks(songMetadata, sourceManager);

		return new BasicAudioPlaylist(artist.getName(), audioTracks, audioTracks.get(0), false, url.getPath());
	}

}
