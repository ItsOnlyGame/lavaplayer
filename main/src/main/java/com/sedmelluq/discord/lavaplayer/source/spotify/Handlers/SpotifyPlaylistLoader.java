package com.sedmelluq.discord.lavaplayer.source.spotify.Handlers;

import com.sedmelluq.discord.lavaplayer.source.spotify.AudioTrackFactory;
import com.sedmelluq.discord.lavaplayer.source.spotify.GetAudioTrackInfo;
import com.sedmelluq.discord.lavaplayer.source.spotify.SpotifyAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import com.sedmelluq.discord.lavaplayer.track.BasicAudioPlaylist;
import com.wrapper.spotify.SpotifyApi;
import com.wrapper.spotify.exceptions.SpotifyWebApiException;
import com.wrapper.spotify.model_objects.specification.Paging;
import com.wrapper.spotify.model_objects.specification.Playlist;
import com.wrapper.spotify.model_objects.specification.PlaylistTrack;
import org.apache.hc.core5.http.ParseException;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SpotifyPlaylistLoader extends GetAudioTrackInfo {

	public BasicAudioPlaylist handle(URL url, AudioTrackFactory audioTrackFactory, SpotifyAudioSourceManager sourceManager, SpotifyApi api) {
		if (!url.getPath().startsWith("/playlist/")) return null;
		Playlist playlist;
		try {
			playlist = api.getPlaylist(getId(url.getPath())).build().execute();
		} catch (IOException | SpotifyWebApiException | ParseException e) {
			throw new IllegalStateException("Unable to fetch playlist from Spotify API.");
		}

		if (playlist == null) return null;

		List<PlaylistTrack> playlistTracks = getAllPlaylistTracks(playlist, api);
		List<AudioTrackInfo> songMetadata = getAudioTrackInfoPlaylist(playlistTracks, url.toString());
		List<AudioTrack> audioTracks = audioTrackFactory.getAudioTracks(songMetadata, sourceManager);

		return new BasicAudioPlaylist(playlist.getName(), audioTracks, audioTracks.get(0), false, "https://open.spotify.com" + url.getPath());
	}

	private List<PlaylistTrack> getAllPlaylistTracks(Playlist playlist, SpotifyApi api) {
		List<PlaylistTrack> playlistTracks = new ArrayList<>();
		Paging<PlaylistTrack> currentPage = playlist.getTracks();

		int iteration = 1;
		do {
			playlistTracks.addAll(Arrays.asList(currentPage.getItems()));
			try {
				if (currentPage.getNext() == null) {
					currentPage = null;
				} else {
					currentPage = api.getPlaylistsItems(playlist.getId()).offset(100 * iteration).limit(100).build().execute();
					iteration++;
				}
			} catch (IOException | SpotifyWebApiException | ParseException e) {
				e.printStackTrace();
			}
		} while (currentPage != null);

		return playlistTracks;
	}

}
