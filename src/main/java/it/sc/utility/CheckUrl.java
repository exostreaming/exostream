package it.sc.utility;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

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
					ConfigProperties.URL = newUrl;
					ConfigProperties.URL_IMAGE = Utils.toCdnUrl(ConfigProperties.URL);

					try {
						Files.write(Paths.get("bin/url.dat"), newUrl.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
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