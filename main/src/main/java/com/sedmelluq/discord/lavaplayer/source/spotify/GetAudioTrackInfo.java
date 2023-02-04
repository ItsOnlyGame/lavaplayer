package com.sedmelluq.discord.lavaplayer.source.spotify;

import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import com.wrapper.spotify.model_objects.IPlaylistItem;
import com.wrapper.spotify.model_objects.specification.*;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public abstract class GetAudioTrackInfo {

	public List<AudioTrackInfo> getAudioTrackInfoPlaylist(List<PlaylistTrack> playlistTracks, String playlistUrl) {
		List<AudioTrackInfo> list = playlistTracks.stream().map(PlaylistTrack::getTrack).map(t -> this.getAudioTrackInfo(t, playlistUrl)).collect(Collectors.toList());
		list.removeIf(Objects::isNull);
		return list;
	}

	public List<AudioTrackInfo> getAudioTrackAlbum(List<TrackSimplified> trackSimplified, Album album) {
		List<AudioTrackInfo> list = trackSimplified.stream().map(t -> this.getAudioTrackInfo(t, album)).collect(Collectors.toList());
		list.removeIf(Objects::isNull);
		return list;
	}

	public List<AudioTrackInfo> getAudioTrack(List<Track> tracks) {
		List<AudioTrackInfo> list = tracks.stream().map(this::getAudioTrackInfo).collect(Collectors.toList());
		list.removeIf(Objects::isNull);
		return list;
	}

	public AudioTrackInfo getAudioTrackInfo(IPlaylistItem item, String playlistUrl) {
		Track track = (Track) item;
		if (track.getId() == null) return null;

		return new AudioTrackInfo(
				track.getName(),
				getAllArtists(track.getArtists()),
				track.getDurationMs(),
				track.getId(),
				false,
				track.getExternalUrls().get("spotify"),
				(track.getAlbum().getImages().length == 0 ? null : track.getAlbum().getImages()[0].getUrl())
		);
	}

	public AudioTrackInfo getAudioTrackInfo(Track track) {
		return new AudioTrackInfo(
				track.getName(),
				getAllArtists(track.getArtists()),
				track.getDurationMs(),
				track.getId(),
				false,
				track.getExternalUrls().get("spotify"),
				track.getAlbum().getImages()[0].getUrl()
		);
	}

	public AudioTrackInfo getAudioTrackInfo(TrackSimplified track, Album album) {
		return new AudioTrackInfo(
				track.getName(),
				getAllArtists(track.getArtists()),
				track.getDurationMs(),
				track.getId(),
				false,
				track.getExternalUrls().get("spotify"),
				album.getImages()[0].getUrl()
		);
	}

	public String getId(String url) {
		return url.split("/")[2].trim();
	}

	private String getAllArtists(ArtistSimplified[] artists) {
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < artists.length; i++) {
			String name = artists[i].getName();

			if (i == artists.length - 1) {
				builder.append(name);
			} else {
				builder.append(name).append(", ");
			}
		}

		return builder.toString();
	}

}
