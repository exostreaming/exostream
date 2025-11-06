package it.sc.services;


import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JButton;
import javax.swing.JProgressBar;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;

import org.apache.commons.text.StringEscapeUtils;

import it.sc.utility.ConfigProperties;
import it.sc.utility.Constants;
import it.sc.utility.Utils;

public class LatestMovieTvService extends SwingWorker<List<String[]>, Void> {

	private JButton searchButton;
	private DefaultTableModel tableModel;
	private JProgressBar progressBar;
	private String category;
	
	public LatestMovieTvService(DefaultTableModel tableModel, JProgressBar progressBar, JButton searchButton, String category) {
		this.tableModel = tableModel;
		this.progressBar = progressBar;
		this.searchButton = searchButton;
		this.category = category;
	}

	@Override
	protected List<String[]> doInBackground() throws Exception {
		return search();
	}

	@Override
	protected void done() {
		try {
			List<String[]> risultato = get();
			if (risultato != null && risultato.size() > 0) {
				for (String[] row : risultato) {
					tableModel.addRow(row);
				}
			} else {
				tableModel.addRow(new Object[]{"Nessun risultato trovato, verifica il link!", "", ""});
			}
		} catch (Exception e) {
			tableModel.addRow(new Object[]{"Errore: " + e.getMessage(), "", ""});
			e.printStackTrace();
		} finally {
			progressBar.setVisible(false);
			if(searchButton != null)
				searchButton.setEnabled(true);
		}
	}

	private List<String[]> search() {
		List<String[]> result = new ArrayList<>();
		
		if(category.equals(Constants.TYPE_MOVIE)) {
			result = getLatestMovie();
			
			if(result != null) {
				DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

				Collections.sort(result, (o1, o2) -> {
				    String s1 = o1[Constants.COLUMN_DATE];
				    String s2 = o2[Constants.COLUMN_DATE];

				    // Se entrambi null → considerali uguali
				    if (s1 == null && s2 == null) return 0;
				    // Se solo il primo è null → mettilo dopo (così i null stanno in fondo)
				    if (s1 == null) return 1;
				    // Se solo il secondo è null → mettilo prima
				    if (s2 == null) return -1;

				    // Entrambi non null → confrontali come date
				    LocalDate d1 = LocalDate.parse(s1, formatter);
				    LocalDate d2 = LocalDate.parse(s2, formatter);

				    // Ordine discendente
				    return d2.compareTo(d1);
				});

			}		
		}
		else if(category.equals(Constants.TYPE_TV)) {
			result = getLatestTv();
		}
		else { //all
			List<String[]> res1 = getLatestMovie();
			List<String[]> res2 = getLatestTv();
			//result.addAll(res1);
			//result.addAll(res2);

			if(res1 != null && res2 != null) {
				result.addAll(res1);
				result.addAll(res2);

				DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

				Collections.sort(result, (o1, o2) -> {
					LocalDate d1 = LocalDate.parse(o1[Constants.COLUMN_DATE], formatter); 
					LocalDate d2 = LocalDate.parse(o2[Constants.COLUMN_DATE], formatter);
					return d2.compareTo(d1); // discendente
				});
			}
		}
		
		
		
		return filterFutureMovies(result);
	}


	public static List<String[]> filterFutureMovies(List<String[]> movies) {
        LocalDate today = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        Iterator<String[]> iterator = movies.iterator();
        while (iterator.hasNext()) {
            String[] movie = iterator.next();
            if (movie[3] != null) {
	            LocalDate movieDate = LocalDate.parse(movie[3], formatter);
	            if (movieDate.isAfter(today)) {
	                iterator.remove();
	            }
            }
        }

        return movies;
    }
	private List<String[]> getLatestMovie() {
		try {
			String url = ConfigProperties.URL + "/it/archive?sort=last_air_date"; 

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
				String decoded = StringEscapeUtils.unescapeHtml4(dataPage.replaceAll("&quot;", "\""));

				return Utils.parseAndReturnTitles(decoded, false);
			} else {
				return null;
			}

		} catch (Exception e) {
			return null;
		}
	}


	private List<String[]> getLatestTv() {
		try {
			String url = ConfigProperties.URL + "/it/browse/new-episodes?type=tv"; 


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
				String decoded = StringEscapeUtils.unescapeHtml4(dataPage.replaceAll("&quot;", "\""));

				return Utils.parseAndReturnTitles(decoded, false);
			} else {
				return null;
			}

		} catch (Exception e) {
			return null;
		}
	}

}
