package com.sedmelluq.discord.lavaplayer.track.lyrics;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;

public class LyricsManager {
	private static final Logger log = LoggerFactory.getLogger(LyricsManager.class);

	public static LyricsInfo getLyrics(String title) throws IOException {
		String link = getGeniusUrl(title + " genius lyrics");
		return handleLyrics(link);
	}

	private static String getGeniusUrl(String searchWord) throws IOException {
		String url = "https://www.google.com/search?q=" + searchWord.trim().replace(" ", "+");
		log.info("Fetching lyrics with keyword: " + searchWord + "\n url: " + url);

		Document doc;
		doc = Jsoup.connect(url).timeout(10000).get();
		Element link = doc.select("#search").first().select("a[href*=https://genius.com]").first();
		if (link != null) {
			return link.attr("href");
		}
		return null;
	}

	private static LyricsInfo handleLyrics(String url) throws IOException {
		Document doc = Jsoup.connect(url).userAgent("chrome").get();

		boolean legacy = false;

		StringBuilder builder = new StringBuilder();
		Elements link = doc.getElementsByClass("lyrics");
		if (!link.isEmpty()) {
			for (Element e : link) {
				builder.append(e.wholeText().trim()).append("\n\n");
			}
		} else {
			legacy = true;

			ArrayList<Element> elements = new ArrayList<>();
			for (Element el : doc.getAllElements()) {
				if (el.className().contains("Lyrics__Container")) {
					elements.add(el);
				}
			}

			if (elements.size() == 0) {
				return null;
			}

			for (Element e : elements) {
				for (int i = 0; i < e.childNodes().size(); i++) {
					Node node = e.childNode(i);

					switch (node.nodeName()) {
						case "#text":
							builder.append(node.toString());
							break;
						case "br":
						case "div":
							if ((i + 1) != e.childNodes().size()) {
								builder.append("\n");
							}
							break;
						case "a":
							for (Node child : node.childNodes().get(0).childNodes()) {
								if (child.nodeName().equals("#text")) {
									builder.append(child);
								} else if (child.nodeName().equals("br")) {
									builder.append("\n");
								}
							}
							break;
						case "i":
							for (Node child : node.childNodes().get(0).childNodes()) {
								if (child.nodeName().equals("#text")) {
									builder.append(child.toString());
								} else if (child.nodeName().equals("i")) {
									builder.append("\n");
								}
							}
							break;
					}
				}
				builder.append("\n");
			}
		}

		return new LyricsInfo(builder.toString(), url, legacy);
	}
}
