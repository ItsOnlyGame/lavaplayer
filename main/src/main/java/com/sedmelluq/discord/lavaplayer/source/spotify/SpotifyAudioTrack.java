package com.sedmelluq.discord.lavaplayer.source.spotify;

import com.sedmelluq.discord.lavaplayer.source.AudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioTrack;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeSearchMusicProvider;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeSearchProvider;
import com.sedmelluq.discord.lavaplayer.track.*;
import com.sedmelluq.discord.lavaplayer.track.playback.LocalAudioTrackExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SpotifyAudioTrack extends DelegatedAudioTrack {

	private static final Logger log = LoggerFactory.getLogger(SpotifyAudioSourceManager.class);

	private final YoutubeAudioSourceManager sourceManager;
	private final YoutubeSearchMusicProvider ytSearchMusicProvider;
	private final YoutubeSearchProvider ytSearchProvider;
	private final AudioTrackInfo initialAudioTrackInfo;
	private final SpotifyAudioSourceManager spotifySourceManager;

	private YoutubeAudioTrack realTrack;
	private int searchIndex;

	public SpotifyAudioTrack(AudioTrackInfo initialAudioTrackInfo, YoutubeAudioSourceManager sourceManager, YoutubeSearchProvider ytSearchProvider,
	                         YoutubeSearchMusicProvider ytSearchMusicProvider, SpotifyAudioSourceManager spotifySourceManager) {
		super(initialAudioTrackInfo);
		this.initialAudioTrackInfo = initialAudioTrackInfo;
		this.sourceManager = sourceManager;
		this.ytSearchMusicProvider = ytSearchMusicProvider;
		this.ytSearchProvider = ytSearchProvider;
		this.spotifySourceManager = spotifySourceManager;
	}

	public SpotifyAudioTrack(AudioTrackInfo initialAudioTrackInfo, YoutubeAudioSourceManager sourceManager, YoutubeSearchProvider ytSearchProvider,
	                         YoutubeSearchMusicProvider ytSearchMusicProvider, SpotifyAudioSourceManager spotifySourceManager, int searchIndex) {
		super(initialAudioTrackInfo);
		this.initialAudioTrackInfo = initialAudioTrackInfo;
		this.sourceManager = sourceManager;
		this.ytSearchMusicProvider = ytSearchMusicProvider;
		this.ytSearchProvider = ytSearchProvider;
		this.spotifySourceManager = spotifySourceManager;
		this.searchIndex = searchIndex;
	}

	@Override
	public void process(LocalAudioTrackExecutor executor) throws Exception {
		setRealTrack();

		if (this.realTrack != null) {
			try {
				this.processDelegate(realTrack, executor);
			} catch (Exception err) {
				if (err.getMessage().equals("This video cannot be viewed anonymously.")) {
					nextSearch();
					process(executor);
				} else {
					throw err;
				}
			}

		} else {
			throw new Exception("Couldn't find a track on YouTube.");
		}
	}

	@Override
	public String getIdentifier() {
		return this.initialAudioTrackInfo.identifier;
	}

	public void setRealTrack() {
		if (this.realTrack != null) return;

		int i = 1;
		int max_retries = 3;
		this.realTrack = getTrack();
		while (this.realTrack == null && i <= max_retries) {
			this.realTrack = getTrack();
			log.error("Couldn't find a track on YouTube | Trying again ( Attempt " + i + " ) " + this.initialAudioTrackInfo.uri);
			i++;
		}

		if (this.realTrack == null) {
			log.error("Wasn't able to fetch the real track");
		}
	}

	@Override
	public AudioTrackInfo getInfo() {
		return new AudioTrackInfo(
				initialAudioTrackInfo.title,
				initialAudioTrackInfo.author,
				initialAudioTrackInfo.length,
				initialAudioTrackInfo.identifier,
				initialAudioTrackInfo.isStream,
				initialAudioTrackInfo.uri,
				initialAudioTrackInfo.artworkUrl
		);
	}

	public AudioTrackInfo getYoutubeInfo() {
		setRealTrack();
		if (this.realTrack == null) return null;

		return new AudioTrackInfo(
				this.realTrack.getInfo().title,
				this.realTrack.getInfo().author,
				this.realTrack.getInfo().length,
				this.realTrack.getInfo().identifier,
				this.realTrack.getInfo().isStream,
				this.realTrack.getInfo().uri,
				initialAudioTrackInfo.artworkUrl
		);
	}

	@Override
	public boolean isSeekable() {
		if (realTrack != null) {
			return realTrack.isSeekable();
		}

		return false;
	}

	@Override
	public AudioTrack makeClone() {
		SpotifyAudioTrack clone = new SpotifyAudioTrack(this.initialAudioTrackInfo, sourceManager, ytSearchProvider, ytSearchMusicProvider, spotifySourceManager, searchIndex);
		clone.setUserData(this.getUserData());
		return clone;
	}

	@Override
	public AudioSourceManager getSourceManager() {
		return spotifySourceManager;
	}

	private YoutubeAudioTrack getTrack() {
		String query = this.initialAudioTrackInfo.author + " " + this.initialAudioTrackInfo.title;
		AudioItem audioItem = ytSearchProvider.loadSearchResult(query, this::buildTrackFromInfo);

		if (audioItem == AudioReference.NO_TRACK) {
			return null;

		} else if (audioItem instanceof YoutubeAudioTrack) {
			return (YoutubeAudioTrack) audioItem;

		} else if (audioItem instanceof AudioPlaylist) {
			AudioPlaylist audioPlaylist = (AudioPlaylist) audioItem;
			return (YoutubeAudioTrack) audioPlaylist.getTracks().get(searchIndex);

		} else {
			log.warn("Unknown AudioItem '{}' returned by YoutubeSearchProvider.", audioItem);
			return null;
		}
	}

	private YoutubeAudioTrack buildTrackFromInfo(AudioTrackInfo info) {
		return new YoutubeAudioTrack(info, sourceManager);
	}

	public void nextSearch() {
		this.searchIndex++;
		this.realTrack = null;
	}

}