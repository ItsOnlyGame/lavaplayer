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
import com.wrapper.spotify.model_objects.specification.Album;
import com.wrapper.spotify.model_objects.specification.Paging;
import com.wrapper.spotify.model_objects.specification.TrackSimplified;
import org.apache.hc.core5.http.ParseException;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SpotifyAlbumLoader extends GetAudioTrackInfo {

	public AudioItem handle(URL url, AudioTrackFactory audioTrackFactory, SpotifyAudioSourceManager sourceManager, SpotifyApi api) {
		Album album = null;
		try {
			album = api.getAlbum(getId(url.getPath())).build().execute();
		} catch (IOException | SpotifyWebApiException | ParseException e) {
			e.printStackTrace();
		}

		if (album == null) return null;

		List<TrackSimplified> albumTracks = getAllAlbumTracks(album, api);
		List<AudioTrackInfo> songMetadata = getAudioTrackAlbum(albumTracks, album);
		List<AudioTrack> audioTracks = audioTrackFactory.getAudioTracks(songMetadata, sourceManager);

		return new BasicAudioPlaylist(album.getName(), audioTracks, audioTracks.get(0), false, url.getPath());
	}

	private List<TrackSimplified> getAllAlbumTracks(Album album, SpotifyApi api) {
		List<TrackSimplified> playlistTracks = new ArrayList<>();
		Paging<TrackSimplified> currentPage = album.getTracks();

		int iteration = 1;
		do {
			playlistTracks.addAll(Arrays.asList(currentPage.getItems()));
			try {
				if (currentPage.getNext() == null) {
					currentPage = null;
				} else {
					currentPage = api.getAlbumsTracks(album.getId()).offset(100 * iteration).limit(100).build().execute();
					iteration++;
				}
			} catch (IOException | SpotifyWebApiException | ParseException e) {
				e.printStackTrace();
			}
		} while (currentPage != null);


		return playlistTracks;
	}

}
