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
import com.wrapper.spotify.model_objects.specification.Paging;
import com.wrapper.spotify.model_objects.specification.Track;
import org.apache.hc.core5.http.ParseException;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

public class SpotifySearchLoader extends GetAudioTrackInfo {

	public AudioItem handle(String searchWord, AudioTrackFactory audioTrackFactory, SpotifyAudioSourceManager sourceManager, SpotifyApi api) throws Exception {
		Track[] tracks = null;
		try {
			Paging<Track> tracks_page = api.searchTracks(searchWord).limit(10).market(sourceManager.getCountryCode()).build().execute();
			tracks = tracks_page.getItems();
		} catch (IOException | SpotifyWebApiException | ParseException e) {
			e.printStackTrace();
		}

		if (tracks == null) {
			throw new Exception("Error occurred while searching songs from Spotify");
		}

		List<AudioTrackInfo> songMetadata = getAudioTrack(Arrays.asList(tracks));
		List<AudioTrack> audioTracks = audioTrackFactory.getAudioTracks(songMetadata, sourceManager);

		String url = null;
		try {
			url = new URL("https://open.spotify.com/search/" + searchWord).getPath();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}

		return new BasicAudioPlaylist("Search", audioTracks, audioTracks.get(0), true, url);
	}
}
