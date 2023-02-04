package com.sedmelluq.discord.lavaplayer.source.spotify;

import com.neovisionaries.i18n.CountryCode;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.spotify.Handlers.*;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeSearchMusicProvider;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeSearchProvider;
import com.sedmelluq.discord.lavaplayer.track.*;
import com.wrapper.spotify.SpotifyApi;
import com.wrapper.spotify.exceptions.SpotifyWebApiException;
import com.wrapper.spotify.model_objects.credentials.ClientCredentials;
import com.wrapper.spotify.requests.authorization.client_credentials.ClientCredentialsRequest;
import org.apache.commons.codec.binary.StringUtils;
import org.apache.hc.core5.http.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.net.URL;
import java.util.List;

public class SpotifyAudioSourceManager implements AudioSourceManager {
	public static final String SPOTIFY_DOMAIN = "open.spotify.com";
	private static final String SEARCH_PREFIX = "spsearch:";
	private static final Logger logger = LoggerFactory.getLogger(SpotifyAudioSourceManager.class);
	private static final String SPOTIFY_TRACK_REGEX = "(?:http://|https://|)(?:www.|)open.spotify.com/track/(.*)";
	private static final String SPOTIFY_PLAYLIST_REGEX = "(?:http://|https://|)(?:www.|)open.spotify.com/playlist/(.*)";
	private static final String SPOTIFY_ARTIST_REGEX = "(?:http://|https://|)(?:www.|)open.spotify.com/artist/(.*)";
	private static final String SPOTIFY_ALBUM_REGEX = "(?:http://|https://|)(?:www.|)open.spotify.com/album/(.*)";

	private final CountryCode countryCode;

	private final SpotifyApi spotifyApi;
	private final AudioTrackFactory audioTrackFactory;

	private final SpotifyArtistLoader artistHandle;
	private final SpotifyAlbumLoader albumHandle;
	private final SpotifyPlaylistLoader playlistHandler;
	private final SpotifyTrackLoader trackHandle;
	private final SpotifySearchLoader searchHandle;

	private final SpotifySeveralTrackLoader severalTrackHandler;

	public SpotifyAudioSourceManager(String clientId, String clientSecret, CountryCode countryCode) {
		this.spotifyApi = new SpotifyApi.Builder()
				.setClientId(clientId)
				.setClientSecret(clientSecret)
				.build();

		this.countryCode = countryCode;

		this.audioTrackFactory = new AudioTrackFactory(
				new YoutubeSearchProvider(),
				new YoutubeSearchMusicProvider(),
				new YoutubeAudioSourceManager()
		);

		this.artistHandle = new SpotifyArtistLoader();
		this.albumHandle = new SpotifyAlbumLoader();
		this.playlistHandler = new SpotifyPlaylistLoader();
		this.trackHandle = new SpotifyTrackLoader();
		this.searchHandle = new SpotifySearchLoader();

		this.severalTrackHandler = new SpotifySeveralTrackLoader();
	}

	public SpotifyApi getSpotifyApi() {
		return spotifyApi;
	}

	@Override
	public String getSourceName() {
		return "Spotify";
	}

	@Override
	public AudioItem loadItem(AudioPlayerManager manager, AudioReference reference) {
		try {
			loadClientCredentials();


			if (reference.identifier.startsWith(SEARCH_PREFIX)) {
				return searchHandle.handle(reference.identifier.split(":")[1], audioTrackFactory, this, spotifyApi);
			}

			URL url = new URL(reference.identifier);

			if (!StringUtils.equals(url.getHost(), SPOTIFY_DOMAIN)) {
				return null;
			}

			if (url.toString().matches(SPOTIFY_TRACK_REGEX)) {
				return trackHandle.handle(url, audioTrackFactory, this, spotifyApi);
			} else if (url.toString().matches(SPOTIFY_PLAYLIST_REGEX)) {
				return playlistHandler.handle(url, audioTrackFactory, this, spotifyApi);
			} else if (url.toString().matches(SPOTIFY_ARTIST_REGEX)) {
				return artistHandle.handle(url, audioTrackFactory, this, spotifyApi);
			} else if (url.toString().matches(SPOTIFY_ALBUM_REGEX)) {
				return albumHandle.handle(url, audioTrackFactory, this, spotifyApi);
			}

			return null;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		} finally {
			spotifyApi.setAccessToken(null);
		}

	}

	public AudioPlaylist getSeveralTracks(List<String> ids) {
		try {
			loadClientCredentials();
			return (AudioPlaylist) severalTrackHandler.handle(ids, audioTrackFactory, this, spotifyApi);
		} catch (ParseException | SpotifyWebApiException | IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public boolean isTrackEncodable(AudioTrack track) {
		return false;
	}

	@Override
	public void encodeTrack(AudioTrack track, DataOutput output) {
		throw new UnsupportedOperationException("encodeTrack is unsupported.");
	}

	@Override
	public AudioTrack decodeTrack(AudioTrackInfo trackInfo, DataInput input) {
		throw new UnsupportedOperationException("decodeTrack is unsupported.");
	}

	@Override
	public void shutdown() {
		spotifyApi.setAccessToken(null);
	}


	private void loadClientCredentials() throws ParseException, SpotifyWebApiException, IOException {
		final ClientCredentialsRequest clientCredentialsRequest = spotifyApi.clientCredentials().build();
		final ClientCredentials clientCredentials = clientCredentialsRequest.execute();

		spotifyApi.setAccessToken(clientCredentials.getAccessToken());
		logger.info("Expires in: " + clientCredentials.getExpiresIn() + "s");
	}

	public CountryCode getCountryCode() {
		return countryCode;
	}
}

