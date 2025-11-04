package it.sc.utility;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CheckUrl {

	public static void search() {
		try {
			Path path = Paths.get("bin/url.dat");
			try {
				if (!Files.exists(path)) {
					Files.createFile(path);
					Files.write(Paths.get("bin/url.dat"), "https://streamingcommunityz.online".getBytes(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}

			String initialUrl = "";
			try (BufferedReader reader = new BufferedReader(new FileReader("bin/url.dat"))) {
				initialUrl = reader.readLine();  
				ConfigProperties.URL = initialUrl;
				ConfigProperties.URL_IMAGE = Utils.toCdnUrl(ConfigProperties.URL);
			} catch (IOException e) {
				e.printStackTrace();
			}

			if(initialUrl != null && !initialUrl.equals("")) {
				HttpURLConnection conn = (HttpURLConnection) new URL(initialUrl).openConnection();
				conn.setInstanceFollowRedirects(false); // Non seguire automaticamente la redirect
				conn.connect();

				int status = conn.getResponseCode();
				if (status == HttpURLConnection.HTTP_MOVED_PERM ||
						status == HttpURLConnection.HTTP_MOVED_TEMP ||
						status == HttpURLConnection.HTTP_SEE_OTHER) {

					String newUrl = conn.getHeaderField("Location");
					newUrl = removeTrailingSlash(newUrl);
					System.out.println("Nuovo URL trovato: " + newUrl);
					
					String checkUrl = isUrlValid(newUrl);
					
					ConfigProperties.URL = checkUrl;
					ConfigProperties.URL_IMAGE = Utils.toCdnUrl(ConfigProperties.URL);

					try {
						Files.write(Paths.get("bin/url.dat"), checkUrl.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
					} catch (IOException e) {
						//none
					}

				} else {
					System.out.println("Nessuna redirect trovata. Codice: " + status);
				}

				conn.disconnect();
			}


		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	
	private static String isUrlValid(String pageUrl) {
		String href = pageUrl;
		try {
			HttpURLConnection connection = (HttpURLConnection) new URL(pageUrl).openConnection();
			connection.setRequestMethod("GET");
			connection.setRequestProperty("User-Agent", "Mozilla/5.0");
			connection.setConnectTimeout(10000);
			connection.setReadTimeout(10000);

			BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			StringBuilder html = new StringBuilder();
			String line;
			while ((line = in.readLine()) != null) {
				html.append(line);
			}
			in.close();

			Pattern pattern = Pattern.compile(
					"<a\\s+[^>]*id=[\"']landing-website[\"'][^>]*href=[\"']([^\"']+)[\"']",
					Pattern.CASE_INSENSITIVE
					);
			Matcher matcher = pattern.matcher(html.toString());

			if (matcher.find()) {
				href = matcher.group(1);
				System.out.println("Link trovato: " + href);
			}  

		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return href;
	}

	public static String removeTrailingSlash(String url) {
		if (url == null || url.isEmpty()) {
			return url;
		}
		if (url.endsWith("/")) {
			return url.substring(0, url.length() - 1);
		}
		return url;
	}
}