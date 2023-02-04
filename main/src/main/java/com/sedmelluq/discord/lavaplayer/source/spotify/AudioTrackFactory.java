package com.sedmelluq.discord.lavaplayer.source.spotify;

import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeSearchMusicProvider;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeSearchProvider;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;

import java.util.List;
import java.util.stream.Collectors;

public class AudioTrackFactory {

	private final YoutubeSearchProvider ytSearchProvider;
	private final YoutubeSearchMusicProvider ytSearchMusicProvider;
	private final YoutubeAudioSourceManager ytAudioSourceManager;

	public AudioTrackFactory(YoutubeSearchProvider ytSearchProvider, YoutubeSearchMusicProvider ytSearchMusicProvider, YoutubeAudioSourceManager ytAudioSourceManager) {
		this.ytSearchProvider = ytSearchProvider;
		this.ytSearchMusicProvider = ytSearchMusicProvider;
		this.ytAudioSourceManager = ytAudioSourceManager;
	}

	public List<AudioTrack> getAudioTracks(List<AudioTrackInfo> songMetadata, SpotifyAudioSourceManager sourceManager) {
		return songMetadata.stream().map(t -> getAudioTrack(t, sourceManager)).collect(Collectors.toList());
	}

	public AudioTrack getAudioTrack(AudioTrackInfo songMetadata, SpotifyAudioSourceManager sourceManager) {
		return new SpotifyAudioTrack(songMetadata, ytAudioSourceManager, ytSearchProvider, ytSearchMusicProvider, sourceManager);
	}
}