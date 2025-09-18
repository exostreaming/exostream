package it.sc.services;


import java.net.URI;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JProgressBar;
import javax.swing.SwingWorker;

import org.apache.commons.text.StringEscapeUtils;

import it.sc.frame.ConsoleFrame;
import it.sc.utility.ConfigProperties;
import it.sc.utility.Constants;
import it.sc.utility.StreamInfo;
import it.sc.utility.Utils;

public class StreamingMovieTvService extends SwingWorker<String, Void> {

	private final String query;
	private String title;
	private JProgressBar progressBar;
	private ConsoleFrame consoleFrame;

	public StreamingMovieTvService(String query, String title, JProgressBar progressBar, ConsoleFrame consoleFrame) {
		this.query = query;
		this.title = title;
		this.progressBar = progressBar;
		this.consoleFrame = consoleFrame;
	}

	@Override
	protected String doInBackground() throws Exception {
		return search(query);
	}

	@Override
	protected void done() {
		try {
			get(); 
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			progressBar.setVisible(false);
		}
	}

	private String search(String query) {
		try {
			progressBar.setVisible(true);

			String encodedQuery = URLEncoder.encode(query, "UTF-8");
			String url = ConfigProperties.URL + "/it/iframe/" + URLDecoder.decode(encodedQuery, StandardCharsets.UTF_8);;

			HttpClient client = HttpClient.newHttpClient();
			HttpRequest request = HttpRequest.newBuilder()
					.uri(new URI(url))
					.header("User-Agent", Constants.USER_AGENT)
					.GET()
					.build();

			HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

			String html = response.body();

			Pattern pattern = Pattern.compile("src\\s*=\\s*\"([^\"]+)\"");
			Matcher matcher = pattern.matcher(html);

			if (matcher.find()) {
				String dataPage = matcher.group(1);
				String decodedSrc = StringEscapeUtils.unescapeHtml4(dataPage);
				consoleFrame.appendLine("decodedSrc:" + decodedSrc);


				client = HttpClient.newHttpClient();
				request = HttpRequest.newBuilder()
						.uri(new URI(decodedSrc))
						.header("User-Agent", Constants.USER_AGENT)
						.header("Referer", ConfigProperties.URL)  
						.GET()
						.build();
				response = client.send(request, HttpResponse.BodyHandlers.ofString());
				html = response.body();

				String fullUrl = Utils.extractUrlWithParams(html) + "&lang=it&h=1"; //masterPlaylist
				String fileName = Utils.extractFilename(html);
				if(fileName.equals("")) 
					fileName = title;
				consoleFrame.appendLine("fullUrl: " + fullUrl);
				consoleFrame.appendLine("fileName: " + fileName);
					
				if(!fullUrl.equals("")) {
					client = HttpClient.newHttpClient();
					request = HttpRequest.newBuilder()
							.uri(new URI(fullUrl))
							.header("User-Agent", Constants.USER_AGENT)
							.header("Referer", decodedSrc)   
							.GET()
							.build();
					response = client.send(request, HttpResponse.BodyHandlers.ofString());
					html = response.body();

					StreamInfo si = StreamInfo.getHighestResolutionInfo(html); 

					consoleFrame.appendLine("URL VIDEO: " + si.getVideoUrl());
					consoleFrame.appendLine("RESOLUTION: " + si.getResolution());
					consoleFrame.appendLine("AUDIO URL: " + si.getAudioUrl());
					consoleFrame.appendLine("SUBS URL: " + si.getSubUrl());
					
					 /*
					  * TODO
					  
					if(si.getVideoUrl() != null)
						BrowserPlayer.open(si.getVideoUrl(),si.getAudioUrl(), si.getSubUrl());
						
						*/
				}

				return decodedSrc;
			} else {
				return "Nessun risultato trovato.";
			}

		} catch (Exception e) {
			consoleFrame.appendLine("Errore nella ricerca: " + e.getMessage());
			consoleFrame.appendLine("PRESTO IN ARRIVO...");
			return "Errore nella ricerca: " + e.getMessage();
		}
		finally {
			consoleFrame.appendLine("Processo terminato");
			progressBar.setVisible(false);
		}
	}

}
