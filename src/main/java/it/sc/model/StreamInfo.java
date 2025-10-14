package it.sc.model;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StreamInfo {
	
	private String resolution;
	private String videoUrl;
	private String audioUrl;
	private String subUrl;
	 
	public StreamInfo(String resolution, String videoUrl, String audioUrl, String subUrl) {
		this.resolution = resolution;
		this.videoUrl = videoUrl;
		this.audioUrl = audioUrl;
		this.subUrl = subUrl;
	}

	public String getResolution() {
		return resolution;
	}

	public String getVideoUrl() {
		return videoUrl;
	}

	public String getAudioUrl() {
		return audioUrl;
	}
	
	public String getSubUrl() {
		return subUrl;
	}

	public static StreamInfo getHighestResolutionInfo(String m3uContent) {
		String[] lines = m3uContent.split("\\R");
		int maxPixels = 0;
		String bestUrl = null;
		String bestResolution = null;
		String audioItaUrl = null;
		String subItaUrl = null;

		for (int i = 0; i < lines.length - 1; i++) {
			String line = lines[i];

			// Trova audio italiano
			if (line.startsWith("#EXT-X-MEDIA") && line.contains("LANGUAGE=\"ita\"") && line.contains("TYPE=AUDIO")) {
				audioItaUrl = extractUri(line);
			}
		 
			// Trova sub italiano
			if (line.startsWith("#EXT-X-MEDIA") && line.contains("LANGUAGE=\"forced-ita\"") && line.contains("TYPE=SUBTITLES")) {
				subItaUrl = extractUri(line);
			}

			// Trova miglior risoluzione
			if (line.startsWith("#EXT-X-STREAM-INF")) {
				String resolution = extractResolution(line);
				if (resolution != null) {
					String[] parts = resolution.split("x");
					try {
						int width = Integer.parseInt(parts[0]);
						int height = Integer.parseInt(parts[1]);
						int pixels = width * height;
						if (pixels > maxPixels) {
							maxPixels = pixels;
							bestUrl = lines[i + 1];
							bestResolution = resolution;
						}
					} catch (NumberFormatException e) {
						// ignora
					}
				}
			}
		}

		if (bestUrl == null || bestResolution == null) {
			throw new IllegalArgumentException("Nessuna risoluzione valida trovata nel contenuto M3U8.");
		}

		return new StreamInfo(bestResolution, bestUrl, audioItaUrl, subItaUrl); 
	}


	public static String extractResolution(String line) {
		Pattern pattern = Pattern.compile("RESOLUTION=(\\d+x\\d+)");
		Matcher matcher = pattern.matcher(line);
		if (matcher.find()) {
			return matcher.group(1);
		}
		return null;
	}


	public static String extractUri(String line) {
		int index = line.indexOf("URI=\"");
		if (index == -1) return null;
		int end = line.indexOf("\"", index + 5);
		if (end == -1) return null;
		return line.substring(index + 5, end);
	}

	@Override
	public String toString() {
		return "StreamInfo [resolution=" + resolution + ", videoUrl=" + videoUrl + ", audioUrl=" + audioUrl
				+ ", subUrl=" + subUrl + "]";
	}
	 

	public static String extractResolutionForFilename(String input) {
	    if (input == null || !input.contains("x")) {
	        return "";
	    }

	    String[] parti = input.split("x");
	    if (parti.length != 2) {
	    	return "";
	    }

	    String altezza = parti[1].trim();
	    return altezza + "p";
	}

	public static String cleanFilename(String nome) {
	    if (nome == null) return null;

	    // Elimina i caratteri non validi per file/directory (cross-platform)
	    String pulito = nome.replaceAll("[\\\\/:*?\"<>|]", "");

	    // Rimuove eventuali spazi o punti finali
	    pulito = pulito.replaceAll("[\\s.]+$", "");

	    // Opzionalmente, puoi troncare il nome se troppo lungo (es. max 255 caratteri)
	    if (pulito.length() > 255) {
	        pulito = pulito.substring(0, 255);
	    }

	    return pulito;
	}

	public static String getYear(String date) {
		if(date == null || date.equals(""))
			return "";
		else 
			return "." + date.substring(0, 4);

	}

}
