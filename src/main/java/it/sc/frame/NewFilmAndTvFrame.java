package it.sc.frame;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Image;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.image.BufferedImage;
import java.net.URI;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

import it.sc.services.DownloadMovieTvService;
import it.sc.services.LatestMovieTvService;
import it.sc.services.SearchService;
import it.sc.utility.ConfigProperties;
import it.sc.utility.Constants;
import it.sc.utility.Utils;

public class NewFilmAndTvFrame extends JFrame {

	private static final long serialVersionUID = -1035476888750220027L;

	private JTextField searchField;
	private JButton searchButton;
	private JButton aboutButton;
	private DefaultTableModel tableModel;
	private JTable resultTable;
	private JProgressBar progressBar;
	private ConsoleFrame consoleFrame;
	private final JDialog imagePreview = new JDialog();  
	private TvRecentlyAdded m;

	public NewFilmAndTvFrame() {
		int frameWidth = 600;
		int frameHeight = 400;
		java.awt.Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
		int screenWidth = screenSize.width;
		int screenHeight = screenSize.height;
		int totalWidth = frameWidth * 2;
		int x1 = (screenWidth - totalWidth) / 2;
		int y = (screenHeight - frameHeight) / 2;

		this.setSize(frameWidth, frameHeight);
		this.setLocation(x1, y);

		setTitle(Constants.TITLE  + " (News Film & Tv)");
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setLayout(new BorderLayout());
		ImageIcon icon = new ImageIcon("bin/icon.png");
		setIconImage(icon.getImage());

		// NORTH panel con campo testo e bottoni
		JPanel topPanel = new JPanel(new BorderLayout());
		searchField = new JTextField();
		searchButton = new JButton("Cerca");
		searchButton.setFocusPainted(false);
		searchButton.setBorder(new EmptyBorder(5,10,5,10));
		searchButton.setBackground(new Color(66, 133, 244)); // blu
		searchButton.setForeground(Color.WHITE);
		searchButton.setFont(new Font("SansSerif", Font.BOLD, 12));
		searchButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

		aboutButton = new JButton("?");
		aboutButton.setFocusPainted(false);
		aboutButton.setBorder(new EmptyBorder(5,10,5,10));
		aboutButton.setBackground(new Color(66, 133, 244)); // blu
		aboutButton.setForeground(Color.WHITE);
		aboutButton.setFont(new Font("SansSerif", Font.BOLD, 12));
		aboutButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

		// pannello a destra con più bottoni
		JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
		rightPanel.add(searchButton);
		rightPanel.add(aboutButton);

		topPanel.add(searchField, BorderLayout.CENTER);
		topPanel.add(rightPanel, BorderLayout.EAST);

		add(topPanel, BorderLayout.NORTH);


		aboutButton.addActionListener(e -> {
			JDialog aboutDialog = new JDialog((Frame) null, "About", true);
	        aboutDialog.setSize(400, 300);
	        aboutDialog.setLayout(new BorderLayout());

	        // Icona ridimensionata
	        ImageIcon icons = new ImageIcon("bin/icon.png");
	        Image scaledImage = icons.getImage().getScaledInstance(250, 200, Image.SCALE_SMOOTH);
	        ImageIcon scaledIcon = new ImageIcon(scaledImage);
	        aboutDialog.setIconImage(icon.getImage());

	        // JLabel con immagine sopra
	        JLabel label = new JLabel(scaledIcon, SwingConstants.CENTER);
	        label.setHorizontalTextPosition(SwingConstants.CENTER);
	        label.setVerticalTextPosition(SwingConstants.BOTTOM);
	        label.setVerticalAlignment(SwingConstants.CENTER);

	        // JLabel che simula un link
	        JLabel linkLabel = new JLabel("<html><div align=\"center\">Support the project<br><a href=''>Visit website</a><br><br></div></html>");
	        linkLabel.setHorizontalAlignment(SwingConstants.CENTER);

	        // Listener per aprire il link
	        linkLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
	        linkLabel.addMouseListener(new MouseAdapter() {
	            @Override
	            public void mouseClicked(MouseEvent e) {
	                try {
	                    Desktop.getDesktop().browse(new java.net.URI(Constants.URL_SITE));
	                } catch (Exception ex) {
	                    ex.printStackTrace();
	                }
	            }
	        });

	        // Pannello con immagine + link sotto
	        JPanel panel = new JPanel(new BorderLayout());
	        panel.add(label, BorderLayout.CENTER);
	        panel.add(linkLabel, BorderLayout.SOUTH);

	        aboutDialog.add(panel, BorderLayout.CENTER);

	        // Centra sullo schermo
	        aboutDialog.setLocationRelativeTo(null);
	        aboutDialog.setVisible(true);
		});

		// CENTRO tabella risultati
		String[] columnNames = {"Name", "Type", "Score", "Date", "ID", "Poster", "Slug", "Season Count"};
		tableModel = new DefaultTableModel(columnNames, 0) {
			private static final long serialVersionUID = 5020507724059423671L;

			// Rendi tutte le celle non editabili
			@Override
			public boolean isCellEditable(int row, int column) {
				return false;
			}
		};

		searchField.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				searchButton.doClick(); // Simula il click sul bottone
				String testo = searchField.getText().trim();
				if (testo.isEmpty()) 
					m.init();
			}
		});

		resultTable = new JTable(tableModel);
		resultTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		resultTable.getColumnModel().getColumn(Constants.COLUMN_NAME).setPreferredWidth(380); // colonna 0 (Name)
		resultTable.getColumnModel().getColumn(Constants.COLUMN_TYPE).setPreferredWidth(30); // colonna 1 (Type)
		resultTable.getColumnModel().getColumn(Constants.COLUMN_SCORE).setPreferredWidth(15);  // colonna 2 (Score)
		resultTable.getColumnModel().getColumn(Constants.COLUMN_DATE).setPreferredWidth(60); // colonna 3 (date)
		resultTable.getColumnModel().removeColumn(resultTable.getColumnModel().getColumn(Constants.COLUMN_SEASON_COUNT)); // colonna 7 (season_count)
		resultTable.getColumnModel().removeColumn(resultTable.getColumnModel().getColumn(Constants.COLUMN_SLUG)); // colonna 6 (slug)
		resultTable.getColumnModel().removeColumn(resultTable.getColumnModel().getColumn(Constants.COLUMN_POSTER)); // colonna 5 (poster)
		resultTable.getColumnModel().removeColumn(resultTable.getColumnModel().getColumn(Constants.COLUMN_ID)); // colonna 4 (id)

		JScrollPane scrollPane = new JScrollPane(resultTable);
		add(scrollPane, BorderLayout.CENTER);

		// SUD progress bar
		progressBar = new JProgressBar();
		progressBar.setIndeterminate(true);
		progressBar.setVisible(false);
		add(progressBar, BorderLayout.SOUTH);

		// Azione bottone
		searchButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String testo = searchField.getText().trim();
				tableModel.setRowCount(0);  // pulisci tabella
				progressBar.setVisible(true);
				searchButton.setEnabled(false);

				if (!testo.isEmpty()) 
					new SearchService(testo, tableModel, progressBar, searchButton).execute();
				else {
					new LatestMovieTvService(tableModel, progressBar, searchButton, Constants.TYPE_ALL).execute();
					m.init();
				}
			}
		});

		//Crea il menu a tendina
		JPopupMenu popupMenu = new JPopupMenu();
		JMenuItem download = new JMenuItem("Download");
		JMenuItem info = new JMenuItem("Info");
		//JMenuItem play = new JMenuItem("Play");
		popupMenu.add(info);
		popupMenu.add(download);
		//TODO... popupMenu.add(play);

		info.addActionListener(e -> {
			int selectedRow = resultTable.getSelectedRow();
			if (selectedRow >= 0) {
				String id = (String) resultTable.getModel().getValueAt(selectedRow, Constants.COLUMN_ID);
				String title = (String) resultTable.getModel().getValueAt(selectedRow, Constants.COLUMN_NAME);
				String slug = (String) resultTable.getModel().getValueAt(selectedRow, Constants.COLUMN_SLUG);
				String type = (String) resultTable.getModel().getValueAt(selectedRow, Constants.COLUMN_TYPE);

				consoleFrame.appendLine("id: " + id + " - title: " + title + " - slug: " + slug);

				int mainX = this.getX();
				int mainY = this.getY();
				int mainWidth = this.getWidth();
				int mainHeight = this.getHeight();

				PreviewFrame frame = new PreviewFrame(id, title, slug, type, consoleFrame);
				frame.setSize(mainWidth-100, mainHeight-100); // o altra altezza preferita
				frame.setLocation(mainX, mainY+20);  
				frame.setVisible(true);

			}
		});


		download.addActionListener(e -> {
			int selectedRow = resultTable.getSelectedRow();
			if (selectedRow >= 0) {
				String id = (String) resultTable.getModel().getValueAt(selectedRow, Constants.COLUMN_ID);
				String type = (String) resultTable.getModel().getValueAt(selectedRow, Constants.COLUMN_TYPE);
				String slug = (String) resultTable.getModel().getValueAt(selectedRow, Constants.COLUMN_SLUG);
				String seasons_count = (String) resultTable.getModel().getValueAt(selectedRow, Constants.COLUMN_SEASON_COUNT);
				String title = (String) resultTable.getModel().getValueAt(selectedRow, Constants.COLUMN_NAME);
				
				consoleFrame.appendLine("id: " + id + " - Type: " + type + " - slug: " + slug + " - seasons_count: " + seasons_count + " - title: " + title);

				if(type.equalsIgnoreCase(Constants.TYPE_MOVIE))
					new DownloadMovieTvService(id, title, title, progressBar, consoleFrame).execute();
				else if(type.equalsIgnoreCase(Constants.TYPE_TV)) {
					int mainX = this.getX();
					int mainY = this.getY();
					int mainWidth = this.getWidth();
					int mainHeight = this.getHeight();

					SeasonsEpisodesFrame frame = new SeasonsEpisodesFrame(consoleFrame, id, title, slug, seasons_count);
					frame.setSize(mainWidth, mainHeight-50); // o altra altezza preferita
					frame.setLocation(mainX, mainY+20);  
					frame.setVisible(Boolean.parseBoolean(ConfigProperties.get(Constants.SHOW_CONSOLE_KEY)));
				}
			}
		});

/*
		play.addActionListener(e -> {
			int selectedRow = resultTable.getSelectedRow();
			if (selectedRow >= 0) {
				String id = (String) resultTable.getModel().getValueAt(selectedRow, Constants.COLUMN_ID);
				String type = (String) resultTable.getModel().getValueAt(selectedRow, Constants.COLUMN_TYPE);
				String slug = (String) resultTable.getModel().getValueAt(selectedRow, Constants.COLUMN_SLUG);
				String seasons_count = (String) resultTable.getModel().getValueAt(selectedRow, Constants.COLUMN_SEASON_COUNT);

				consoleFrame.appendLine("id: " + id + " - Type: " + type + " - slug: " + slug + " - seasons_count: " + seasons_count );

				if(type.equalsIgnoreCase(Constants.TYPE_MOVIE))
					new StreamingMovieTvService(id, progressBar, consoleFrame).execute();
			}
		});
*/
		resultTable.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				if (e.isPopupTrigger()) showMenu(e);
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				if (e.isPopupTrigger()) showMenu(e);
			}

			@Override
			public void mouseExited(MouseEvent e) {
				imagePreview.setVisible(false);
			}

			private void showMenu(MouseEvent e) {
				int row = resultTable.rowAtPoint(e.getPoint());

				String type = (String) resultTable.getModel().getValueAt(row, Constants.COLUMN_TYPE);
				if(type.equals(Constants.TYPE_MOVIE)) {
					download.setText("Download");
					//play.setVisible(true);
				}
				else if(type.equals(Constants.TYPE_TV)) {
					download.setText("Seleziona Stagione-Episodio");
					//play.setVisible(false);
				}

				if (row >= 0 && row < resultTable.getRowCount()) {
					resultTable.setRowSelectionInterval(row, row); // seleziona riga
				} else {
					resultTable.clearSelection();
				}
				popupMenu.show(e.getComponent(), e.getX(), e.getY());
			}
		});


		resultTable.addMouseMotionListener(new MouseMotionAdapter() {
			final JLabel imageLabel = new JLabel();
			int lastRow = -1;
			{
				imagePreview.setUndecorated(true);
				imagePreview.setAlwaysOnTop(true);
				imageLabel.setPreferredSize(new Dimension(200, 300));
				imagePreview.getContentPane().add(imageLabel);
				imagePreview.pack();
			}
			@Override
			public void mouseMoved(MouseEvent e) {
				int row = resultTable.rowAtPoint(e.getPoint());

				if (row != -1 && row != lastRow) {
					lastRow = row;
					try {
						String poster = (String) resultTable.getModel().getValueAt(row, Constants.COLUMN_POSTER);
						String id = (String) resultTable.getModel().getValueAt(row, Constants.COLUMN_ID);
						if(poster != null) {
							consoleFrame.appendLine("id: " + id);
							String imagePath = ConfigProperties.URL_IMAGE + "/"+ poster;

							BufferedImage img = ImageIO.read(new URL(imagePath));
							if (img != null) {
								ImageIcon icon = new ImageIcon(img);
								imageLabel.setIcon(icon);
								imagePreview.pack();
								Point mouseLocation = e.getLocationOnScreen();
								int x = mouseLocation.x + 15;
								int y = mouseLocation.y + 15;

								Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
								if (x + imagePreview.getWidth() > screenSize.width) {
									x = screenSize.width - imagePreview.getWidth();
								}
								if (y + imagePreview.getHeight() > screenSize.height) {
									y = screenSize.height - imagePreview.getHeight();
								}
								imagePreview.setLocation(x, y);

								imagePreview.setVisible(true);
							} else {
								imagePreview.setVisible(false);
							}
						}

					} catch (Exception ex) {
						ex.printStackTrace();
						imagePreview.setVisible(false);
					}

				} else if (row == -1) {
					lastRow = -1;
					imagePreview.setVisible(false);
				}
			}
		});

		int mainX = this.getX();
		int mainY = this.getY();
		int mainWidth = this.getWidth();
		int mainHeight = this.getHeight();

		consoleFrame = new ConsoleFrame();
		consoleFrame.setSize(frameWidth*2, 200); // o altra altezza preferita
		consoleFrame.setLocation(mainX, mainY + mainHeight);  
		consoleFrame.setVisible(Boolean.parseBoolean(ConfigProperties.get(Constants.SHOW_CONSOLE_KEY)));

		m = new TvRecentlyAdded(consoleFrame);
		m.setSize(600, 400);
		m.setLocation(mainX+mainWidth, mainY);  
		m.setVisible(true);

		progressBar.setVisible(true);
		searchButton.setEnabled(false);
		new LatestMovieTvService(tableModel, progressBar, searchButton, Constants.TYPE_MOVIE).execute();

		consoleFrame.appendLine("Start - " + Constants.TITLE + "  " + Constants.VERSION);

		checkNewVersion();
	}


	private void checkNewVersion() {
		String localVersion = ConfigProperties.get("app.version");
		String remoteVersion = Utils.getRemoteVersion();

		consoleFrame.appendLine("Versione locale: " + localVersion);
		consoleFrame.appendLine("Versione remota: " + remoteVersion);

		if (localVersion != null && remoteVersion != null) {
			int cmp = Utils.compareVersions(localVersion, remoteVersion);
			if (cmp < 0) {
				showUpdateDialogAndOpenURL(remoteVersion);
			} else if (cmp == 0) {
				// opzionale, puoi non mostrare nulla se è aggiornata
				consoleFrame.appendLine("Sei già aggiornato all'ultima versione.");
			} else {
				consoleFrame.appendLine("La versione locale è più recente di quella remota.");
			}
		} else 
			consoleFrame.appendLine("Impossibile verificare aggiornamenti.");

	}


	private void showUpdateDialogAndOpenURL(String remoteVersion) {
		JOptionPane.showMessageDialog(null,
				"È disponibile una nuova versione: " + remoteVersion,
				"Aggiornamento disponibile",
				JOptionPane.INFORMATION_MESSAGE);

		if (Desktop.isDesktopSupported()) {
			try {
				Desktop.getDesktop().browse(new URI(Constants.URL_SITE));
			} catch (Exception e) {
				e.printStackTrace();
				JOptionPane.showMessageDialog(null,
						"Impossibile aprire il browser.",
						"Errore",
						JOptionPane.ERROR_MESSAGE);
			}
		}
	}

}
