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
import com.wrapper.spotify.model_objects.specification.Track;
import org.apache.commons.collections4.ListUtils;
import org.apache.hc.core5.http.ParseException;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SpotifySeveralTrackLoader extends GetAudioTrackInfo {

	public AudioItem handle(List<String> urls, AudioTrackFactory audioTrackFactory, SpotifyAudioSourceManager sourceManager, SpotifyApi api) {
		List<Track> tracks = new ArrayList<>();
		List<List<String>> partition = ListUtils.partition(urls, 50);

		try {
			for (List<String> lst : partition) {
				List<String> formatted_urls = new ArrayList<>();
				lst.forEach((str) -> {
					try {
						URL url = new URL(str);
						formatted_urls.add(url.getPath().split("/")[2]);
					} catch (MalformedURLException e) {
						e.printStackTrace();
					}
				});

				tracks.addAll(Arrays.asList(api.getSeveralTracks(formatted_urls.toArray(new String[0])).market(sourceManager.getCountryCode()).build().execute()));
			}

		} catch (IOException | SpotifyWebApiException | ParseException e) {
			e.printStackTrace();
		}

		List<AudioTrackInfo> songMetadata = getAudioTrack(tracks);
		List<AudioTrack> audioTrackList = audioTrackFactory.getAudioTracks(songMetadata, sourceManager);
		return new BasicAudioPlaylist("spotify", audioTrackList, null, false, null);
	}

}
