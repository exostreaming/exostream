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

import javax.swing.JLabel;
import javax.swing.SwingWorker;

import org.apache.commons.text.StringEscapeUtils;

import it.sc.frame.ConsoleFrame;
import it.sc.utility.ConfigProperties;
import it.sc.utility.Constants;
import it.sc.utility.Utils;


public class FilenameService extends SwingWorker<String, Void> {

	private final String query;
	private final JLabel quality;
	private ConsoleFrame consoleFrame;

	public FilenameService(String query, JLabel quality, ConsoleFrame consoleFrame) {
		this.query = query;
		this.quality = quality;
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
		}
	}


	private String search(String query) {
		String fileName = "";
		try {

			String encodedQuery = URLEncoder.encode(query, "UTF-8");
			String url = ConfigProperties.URL + "/it/iframe/" + URLDecoder.decode(encodedQuery, StandardCharsets.UTF_8);

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


				client = HttpClient.newHttpClient();
				request = HttpRequest.newBuilder()
						.uri(new URI(decodedSrc))
						.header("User-Agent", Constants.USER_AGENT)
						.header("Referer", ConfigProperties.URL)   
						.GET()
						.build();
				response = client.send(request, HttpResponse.BodyHandlers.ofString());
				html = response.body();

				fileName = Utils.extractFilename(html);
				consoleFrame.appendLine("fileName:" + fileName);
			}
		} catch (Exception e) {
			//none
		}
		if((fileName == null || fileName.equals("")) && quality.getText().equals("Loading...")) 
			quality.setText("Quality: none");
		
		return fileName;
	}
}
