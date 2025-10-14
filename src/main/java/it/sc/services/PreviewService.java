package it.sc.services;


import java.awt.Desktop;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.JTextArea;
import javax.swing.SwingWorker;

import org.apache.commons.text.StringEscapeUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import it.sc.utility.ConfigProperties;
import it.sc.utility.Constants;

public class PreviewService extends SwingWorker<String, Void> {

	private JProgressBar progressBar;
	private String id;
	private String slug;
	private JLabel quality;
	private JTextArea plot;
	private JButton trailerButton;
	private JLabel genre;

	public PreviewService(String id, String slug, JProgressBar progressBar, JLabel quality, JTextArea plot, JButton trailerButton, JLabel genre) {
		this.id = id;
		this.slug = slug;
		this.progressBar = progressBar;
		this.quality = quality;
		this.plot = plot;
		this.trailerButton = trailerButton;
		this.genre = genre;
	}

	@Override
	protected String doInBackground() throws Exception {
		return search();
	}

	@Override
	protected void done() {
		try {
			get();
		} catch (InterruptedException e) {
			plot.setText("Errore nel recupero dei dati");
			e.printStackTrace();
		} catch (ExecutionException e) {
			plot.setText("Errore nel recupero dei dati");
			e.printStackTrace();
		}
	}

	private String search() {
		try {
			String url = ConfigProperties.URL + "/it/titles/"+id+"-"+slug;
			HttpClient client = HttpClient.newHttpClient();
			HttpRequest request = HttpRequest.newBuilder()
					.uri(new URI(url))
					.header("User-Agent", Constants.USER_AGENT)
					.GET()
					.build();

			HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
			StringBuffer html = new StringBuffer();
			html.append(response.body());
			//Pattern pattern = Pattern.compile("<div id=\"app\"[^>]*data-page=\"(.*?)\"");
			Pattern pattern = Pattern.compile("<div\\s*id=\"app\"[^>]*data-page=\"([^\"]+?)\"");
			Matcher matcher = pattern.matcher(html);

			if (matcher.find()) {
				String dataPage = matcher.group(1);
				String json = StringEscapeUtils.unescapeHtml4(dataPage.replaceAll("&quot;", "\""));

				JSONObject root = new JSONObject(json);
				JSONObject props = root.getJSONObject("props");
				JSONObject title = props.getJSONObject("title");

				if(quality.getText().equals("Loading...") || !title.getString("quality").equalsIgnoreCase("none"))
					quality.setText("Quality: "+ title.getString("quality"));
				plot.setText(title.getString("plot"));

				//trailer
				try {
					JSONArray trailers = title.getJSONArray("trailers");
					if(trailers.length() > 0) {
						JSONObject firstTrailer = trailers.getJSONObject(0);
						String youtube = firstTrailer.getString("youtube_id");

						if(!youtube.isEmpty()) {
							trailerButton.setVisible(true);
							trailerButton.addActionListener(e -> {
								try {
									URI uri = new URI("https://www.youtube.com/watch?v="+youtube);
									if (Desktop.isDesktopSupported()) {
										Desktop.getDesktop().browse(uri);
									}
								} catch (Exception ex) {
									ex.printStackTrace();
								}
							});
						}
					}
				}catch (Exception e) {
					e.printStackTrace();
				}

				//genre
				try {
					JSONArray genres = title.getJSONArray("genres");
					if(genres.length() > 0) {
						StringBuilder genreNames = new StringBuilder();
						for (int i = 0; i < genres.length(); i++) {
							JSONObject genre = genres.getJSONObject(i);
							String name = genre.getString("name");
							genreNames.append(name);
							if (i < genres.length() - 1) {
								genreNames.append(", ");
							}
						}
						
						genre.setText("Genre: " + genreNames.toString());
					}
				}catch (Exception e) {
					e.printStackTrace();
				}

			}  
			else {
				quality.setText("");
				plot.setText("Non presente.");
			}

		} catch (Exception e) {
			plot.setText("Errore nel recupero dei dati");
		}

		progressBar.setVisible(false);

		return "OK";
	}



}
