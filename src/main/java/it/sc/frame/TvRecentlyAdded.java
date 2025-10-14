package it.sc.frame;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.image.BufferedImage;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;

import it.sc.model.Content;
import it.sc.services.DownloadMovieTvService;
import it.sc.services.LatestMovieTvService;
import it.sc.utility.ConfigProperties;
import it.sc.utility.Constants;

public class TvRecentlyAdded extends JFrame {

	private static final long serialVersionUID = -1035476888750220027L;

	private DefaultTableModel tableModel;
	private JTable resultTable;
	private JProgressBar progressBar;
	private final JDialog imagePreview = new JDialog();  
	private ConsoleFrame consoleFrame;

	public TvRecentlyAdded(ConsoleFrame consoleFrame) {
		this.consoleFrame = consoleFrame;
		setTitle(Constants.TITLE + " (Tv -> Episodes Recently Added)");
		setSize(600, 400);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setLocationRelativeTo(null);
		setLayout(new BorderLayout());
		ImageIcon icon = new ImageIcon("bin/icon.png");
		setIconImage(icon.getImage());


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


		//Crea il menu a tendina
		JPopupMenu popupMenu = new JPopupMenu();
		JMenuItem download = new JMenuItem("Download");
		JMenuItem info = new JMenuItem("Info");
		popupMenu.add(info);
		popupMenu.add(download);
		
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
				String date = (String) resultTable.getModel().getValueAt(selectedRow, Constants.COLUMN_DATE);
				
				Content content = new Content();
				content.setId(id);
				content.setType(type);
				content.setSlug(slug);
				content.setSeasons_count(seasons_count);
				content.setTitle(title);
				content.setDate(date);
				content.setFileNameTmp(title);
				
				consoleFrame.appendLine(content.toString());

				if(type.equalsIgnoreCase(Constants.TYPE_MOVIE))
					new DownloadMovieTvService(content, progressBar, consoleFrame).execute();
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
				if(type.equals(Constants.TYPE_MOVIE))
					download.setText("Download");
				else if(type.equals(Constants.TYPE_TV))
					download.setText("Seleziona Stagione-Episodio");  

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
						String id = (String) resultTable.getModel().getValueAt(row, Constants.COLUMN_POSTER);
						if(id != null) {
							String imagePath = ConfigProperties.URL_IMAGE + "/"+ id;

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

		progressBar.setVisible(true);
		new LatestMovieTvService(tableModel, progressBar, null, Constants.TYPE_TV).execute();
	}

	public void init() {
		progressBar.setVisible(true);
		tableModel.setRowCount(0);  // pulisci tabella
		new LatestMovieTvService(tableModel, progressBar, null, Constants.TYPE_TV).execute();
	}


}
