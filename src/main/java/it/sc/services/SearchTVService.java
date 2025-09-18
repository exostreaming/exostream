package it.sc.services;


import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JProgressBar;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;

import org.apache.commons.text.StringEscapeUtils;

import it.sc.frame.ConsoleFrame;
import it.sc.utility.ConfigProperties;
import it.sc.utility.Constants;
import it.sc.utility.Utils;

public class SearchTVService extends SwingWorker<String, Void> {

	private final String query;
	private JProgressBar progressBar;
	private ConsoleFrame consoleFrame;
	private DefaultTableModel tableModel;

	public SearchTVService(String query, JProgressBar progressBar, ConsoleFrame consoleFrame, DefaultTableModel tableModel) {
		this.query = query;
		this.progressBar = progressBar;
		this.consoleFrame = consoleFrame;
		this.tableModel = tableModel;
	}

	@Override
	protected String doInBackground() throws Exception {
		return search(query);
	}

	@Override
	protected void done() {
		try {
			String risultato = get();
			if (risultato != null) {
				String decoded = StringEscapeUtils.unescapeHtml4(risultato);

				List<String[]> formatted = Utils.parseAndReturnTitlesTV(decoded, true);

				for (String[] row : formatted) {
					tableModel.addRow(row);
				}
			} else {
				tableModel.addRow(new Object[]{"Nessun risultato trovato.", "", ""});
			}
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
			String url = ConfigProperties.URL + "/it/titles/" + encodedQuery;
 
			HttpClient client = HttpClient.newHttpClient();
			HttpRequest request = HttpRequest.newBuilder()
					.uri(new URI(url))
					.header("User-Agent", Constants.USER_AGENT)
					.GET()
					.build();

			HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
			String html = response.body();

			Pattern pattern = Pattern.compile("<div id=\"app\"[^>]*data-page=\"(.*?)\"");
			Matcher matcher = pattern.matcher(html);

			if (matcher.find()) {
				String dataPage = matcher.group(1);
				
				return  dataPage.replaceAll("&quot;", "\"");
			} else {
				return "Nessun risultato trovato.";
			}

		} catch (Exception e) {
			return "Errore nella ricerca: " + e.getMessage();
		}
		finally {
			progressBar.setVisible(false);
		}
	}

}
