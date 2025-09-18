package it.sc.utility;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.text.StringEscapeUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import it.sc.frame.ConsoleFrame;

public class Utils {

	public static String getFileExtension(String filename) {
		if (filename == null || filename.lastIndexOf('.') == -1) {
			return ""; // Nessuna estensione
		}
		return filename.substring(filename.lastIndexOf('.') + 1);
	}

	public static String removeFileExtension(String filename) {
		if (filename == null || filename.lastIndexOf('.') == -1) {
			return filename; // Nessuna estensione da rimuovere
		}
		return filename.substring(0, filename.lastIndexOf('.'));
	}

	public static boolean deleteFile(String path) {
		File file = new File(path);
		if (file.exists()) {
			return file.delete();
		} else {
			System.out.println("File non trovato: " + path);
			return false;
		}
	}

	public static void deleteFolder(String path) {
		Path directory = Paths.get(path);
		try {
			deleteDirectory(directory);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void deleteDirectory(Path path) throws IOException {
		Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
				Files.delete(file);
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
				Files.delete(dir);
				return FileVisitResult.CONTINUE;
			}
		});
	}

	public static String extractFilename(String html) {
		try {
			Pattern pattern = Pattern.compile("window\\.video\\s*=\\s*(\\{.*?\\});", Pattern.DOTALL);
			Matcher matcher = pattern.matcher(html);

			if (matcher.find()) {
				String jsonText = matcher.group(1);
				jsonText = org.apache.commons.text.StringEscapeUtils.unescapeJava(jsonText);
				JSONObject obj = new JSONObject(jsonText);

				if(obj.isNull("filename")) {
					return obj.isNull("quality") ? sanitizeFileName(obj.getString("name")).replaceAll(" ", ".") : sanitizeFileName(obj.getString("name")).replaceAll(" ", ".") + "." + obj.getInt("quality");
				}
				return sanitizeFileName(obj.getString("filename"));
			} else {
				return null;
			}
		} catch (Exception e) {
			return "Errore: " + e.getMessage();
		}
	}


	public static String sanitizeFileName(String fileName) { 
		return fileName.replaceAll("[\\\\/:*?\"<>|]", "_");
	}


	public static String extractUrlWithParams(String js) throws Exception {
		// Step 1: estrai la parte JSON-like di window.masterPlaylist
		Pattern pattern = Pattern.compile("window\\.masterPlaylist\\s*=\\s*(\\{.*?\\});", Pattern.DOTALL);
		Matcher matcher = pattern.matcher(js);
		if (!matcher.find()) {
			throw new Exception("window.masterPlaylist non trovato");
		}
		String jsonLike = matcher.group(1);

		// Step 2: pulisci la stringa per farla diventare JSON valido
		// - virgolette singole a doppie
		// - rimuove virgole finali negli oggetti
		// - mette virgolette alle chiavi
		String jsonStr = jsonLike
				.replace("'", "\"")
				.replaceAll(",\\s*}", "}")
				.replaceAll(",\\s*]", "]")
				.replaceAll("([\\{,\\s])(\\w+)\\s*:", "$1\"$2\":");

		// Step 3: parse JSON
		JSONObject masterPlaylist = new JSONObject(jsonStr);
		JSONObject params = masterPlaylist.getJSONObject("params");
		String url = masterPlaylist.getString("url");

		// Step 4: aggiungi params come query string all’url
		StringBuilder fullUrl = new StringBuilder(url);

		boolean hasQuery = url.contains("?");
		Iterator<String> keys = params.keys();
		while (keys.hasNext()) {
			String key = keys.next();
			String value = params.getString(key);
			if (value == null || value.isEmpty()) continue;

			fullUrl.append(hasQuery ? "&" : "?");
			fullUrl.append(key).append("=").append(value);
			hasQuery = true;
		}

		return fullUrl.toString();
	}


	public static void runExe(String url, String fileName, ConsoleFrame consoleFrame) {
		try {
			// Path del file exe (relativo alla cartella di lavoro corrente)
			File exeFile = new File("bin/N_m3u8DL-RE.exe");

			if (!exeFile.exists()) {
				consoleFrame.appendLine("File exe non trovato: " + exeFile.getAbsolutePath());
				return;
			}

			// Comando con argomenti
			ProcessBuilder pb = new ProcessBuilder(
					exeFile.getAbsolutePath(),
					url,
					"--save-name="+ fileName,
					"--del-after-done",
					"--download-retry-count="+ConfigProperties.get(Constants.DOWNLOAD_RETRY_COUNT_KEY),
					"--thread-count="+ ConfigProperties.get(Constants.NUM_THREAD_KEY)
					);

			// Imposta cartella di lavoro (opzionale)
			pb.directory(new File("bin"));

			// Unisci stdout e stderr (opzionale)
			pb.redirectErrorStream(true);

			// Avvia processo
			Process process = pb.start();

			// Leggi output del processo
			try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
				String line;
				while ((line = reader.readLine()) != null) {
					consoleFrame.appendLine(line);
				}
			}

			// Aspetta che termini
			process.waitFor();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void mergeAudioVideo(String videoPath, String audioPath, String outputPath, ConsoleFrame consoleFrame) {
		outputPath = outputPath.trim();
		while (outputPath.endsWith(".")) {
			outputPath = outputPath.substring(0, outputPath.length() - 1);
		}
		if (!outputPath.matches(".*\\.[a-zA-Z]{1,5}$")) {
			outputPath += ".mkv";
		}


		ProcessBuilder builder = new ProcessBuilder(
				"bin/ffmpeg.exe", "-i", "bin/"+videoPath, "-i", "bin/"+audioPath,
				"-c:v", "copy", "-c:a", "aac", "-strict", "experimental", "out/"+outputPath
				);

		builder.redirectErrorStream(true); // Unisce stdout e stderr

		try {
			Process process = builder.start();

			// Legge l'output della console (utile per debug)
			BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
			String line;
			while ((line = reader.readLine()) != null) {
				consoleFrame.appendLine(line);
			}

			int exitCode = process.waitFor();
			if (exitCode == 0) {
				consoleFrame.appendLine("File creato con successo: " + outputPath);
			} else {
				consoleFrame.appendLine("Errore durante la fusione. Codice uscita: " + exitCode);
			}

		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
	}

	public static List<String[]> parseAndReturnTitles(String rawJson, Boolean sort) {
		List<String[]> risultati = new ArrayList<>();

		try {
			JSONObject data = new JSONObject(rawJson);
			JSONArray titles = data.getJSONObject("props").getJSONArray("titles");

			for (int i = 0; i < titles.length(); i++) {
				JSONObject title = titles.getJSONObject(i);

				String name = title.optString("name", "N/A");
				String type = title.optString("type", "N/A");
				String score = title.optString("score", "N/A");
				String date = title.optString("last_air_date", "N/A");
				String id = title.optString("id", "N/A");
				String slug = title.optString("slug", "N/A");
				String seasons_count = title.optString("seasons_count", "N/A");
				String poster = "";

				try {
					String imagesString = title.optString("images");
					JSONArray imagesArray = new JSONArray(imagesString);
					for (int j = 0; j < imagesArray.length(); j++) {
						JSONObject image = imagesArray.getJSONObject(j);
						if ("poster".equals(image.getString("type"))) {
							poster = image.getString("filename");
							break;  
						}
					}
				}catch (Exception e) {}

				risultati.add(new String[]{name, type, score, date, id, poster, slug, seasons_count});
			}

		} catch (Exception e) {
			risultati.add(new String[]{"Nessun risultato trovato.", "", "", "", "", "", "", ""});
		}

		if(sort) {
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

			Collections.sort(risultati, (o1, o2) -> {
				LocalDate d1 = LocalDate.parse(o1[Constants.COLUMN_DATE], formatter); 
				LocalDate d2 = LocalDate.parse(o2[Constants.COLUMN_DATE], formatter);
				return d2.compareTo(d1); // discendente
			});
		}

		return risultati;
	}


	public static List<String[]> parseAndReturnTitlesTV(String rawJson, Boolean sort) {
		List<String[]> risultati = new ArrayList<>();

		try {
			JSONObject data = new JSONObject(rawJson);
			JSONArray titles = data.getJSONObject("props").getJSONObject("loadedSeason").getJSONArray("episodes");

			for (int i = 0; i < titles.length(); i++) {
				JSONObject title = titles.getJSONObject(i);

				String id = title.optString("id", "N/A");
				String number = title.optString("number", "N/A");
				String name = title.optString("name", "N/A");
				String plot = title.optString("plot", "N/A");
				String duration = title.optString("duration", "N/A");
				String created_at = title.optString("created_at", "N/A").split("T")[0];
				String quality = title.optString("quality", "N/A");

				risultati.add(new String[]{number, name,  duration, quality, created_at, id, plot});
			}

		} catch (Exception e) {
			risultati.add(new String[]{"Nessun risultato trovato.", "", "", "", "", "", ""});
		}

		if(sort) {
			Collections.sort(risultati, new Comparator<String[]>() {
				@Override
				public int compare(String[] a, String[] b) {
					int numA = Integer.parseInt(a[0]); // a[0] è "number"
					int numB = Integer.parseInt(b[0]);
					return Integer.compare(numB, numA); // decrescente
				}
			});

		}

		return risultati;
	}

	public static int compareVersions(String v1, String v2) {
		if (v1 == null || v2 == null) return 0;
		String[] parts1 = v1.split("\\.");
		String[] parts2 = v2.split("\\.");
		int length = Math.max(parts1.length, parts2.length);
		for (int i = 0; i < length; i++) {
			int p1 = i < parts1.length ? Integer.parseInt(parts1[i]) : 0;
			int p2 = i < parts2.length ? Integer.parseInt(parts2[i]) : 0;
			if (p1 < p2) return -1;
			if (p1 > p2) return 1;
		}
		return 0;
	}

	// Scarica il config.properties da GitHub e ne legge la versione
	public static String getRemoteVersion() {
		Properties prop = new Properties();
		String urlString = "https://raw.githubusercontent.com/exostreaming/exostream/refs/heads/main/bin/config.properties";

		try {
			URL url = new URL(urlString);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");

			int responseCode = conn.getResponseCode();
			if (responseCode == 200) {
				try (InputStream input = conn.getInputStream()) {
					prop.load(input);
					return prop.getProperty("app.version").trim();
				}
			} else {
				System.err.println("Errore HTTP: " + responseCode);
				return null;
			}
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static void getSub(String subUrl, String fileName) {
		try {
			HttpClient client = HttpClient.newHttpClient();
			HttpRequest request = HttpRequest.newBuilder()
					.uri(new URI(subUrl))
					.header("User-Agent", Constants.USER_AGENT)
					.GET()
					.build();

			HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

			String html = response.body();

			Pattern pattern = Pattern.compile("https?://\\S+");
			Matcher matcher = pattern.matcher(html);

			if (matcher.find()) {
				String url = matcher.group();
				String decodedSrc = StringEscapeUtils.unescapeHtml4(url);


				client = HttpClient.newHttpClient();
				request = HttpRequest.newBuilder()
						.uri(new URI(decodedSrc))
						.header("User-Agent", Constants.USER_AGENT)
						.header("Referer", ConfigProperties.URL)   
						.GET()
						.build();
				response = client.send(request, HttpResponse.BodyHandlers.ofString());
				html = response.body();

				try {
					Files.write(Paths.get("out/" + fileName + ".vtt"), html.getBytes(), StandardOpenOption.CREATE);

				} catch (IOException e) {
					//none
				}

			}
		} catch (Exception e) {
			//none
		}

	}

	public static String toCdnUrl(String url) {
		if (url == null || url.isEmpty()) {
			return url;
		}

		try {
			java.net.URL u = new java.net.URL(url);
			String protocol = u.getProtocol(); 
			String host = u.getHost();         
			return protocol + "://cdn." + host + "/images";
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static String formatNumber(String numeroStr) {
	    int numero = Integer.parseInt(numeroStr); // converte la stringa in numero
	    if (numero < 10) {
	        return "0" + numero; // aggiunge lo 0 davanti
	    } else {
	        return String.valueOf(numero); // ritorna il numero così com’è
	    }
	}
}

