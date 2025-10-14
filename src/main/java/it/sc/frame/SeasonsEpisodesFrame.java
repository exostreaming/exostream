package it.sc.frame;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;

import it.sc.model.Content;
import it.sc.services.DownloadMovieTvService;
import it.sc.services.SearchTVService;
import it.sc.utility.Constants;
import it.sc.utility.Utils;

public class SeasonsEpisodesFrame extends JFrame {

	private static final long serialVersionUID = -1035476888750220027L;

	private DefaultTableModel tableModelTv;
	private JTable resultTableTv;
	private JProgressBar progressBarTv;
	private final JDialog imagePreview = new JDialog();  
	private ConsoleFrame consoleFrame;
	private String seasonsCount;
	private String title;

	public SeasonsEpisodesFrame(ConsoleFrame consoleFrame, String id, String title, String slug, String seasons_count) {
		this.consoleFrame = consoleFrame;
		this.title = title;
		this.seasonsCount = seasons_count;
		
		setTitle(Constants.TITLE);
		setSize(600, 400);
		setLocationRelativeTo(null);
		setLayout(new BorderLayout());
		ImageIcon icon = new ImageIcon("bin/icon.png");
		setIconImage(icon.getImage());

		// NORTH panel con campo testo e bottone
		int numSeason = Integer.parseInt(seasons_count);
		Integer[] numeri = new Integer[numSeason];
		for (int i = 0; i < numSeason; i++) {
			numeri[i] = numSeason - i;
		}
		JComboBox<Integer> comboBox = new JComboBox<>(numeri);
		comboBox.setSelectedIndex(0);
		comboBox.setBackground(new Color(66, 133, 244));   // blu sfondo
		comboBox.setForeground(Color.WHITE);               // testo bianco
		comboBox.setFont(new Font("SansSerif", Font.BOLD, 14));
		comboBox.setCursor(new Cursor(Cursor.HAND_CURSOR));
		comboBox.setFocusable(false);
 

		JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
		JLabel label = new JLabel("Season:");
		topPanel.add(label);
		topPanel.add(comboBox);
		add(topPanel, BorderLayout.NORTH);

		comboBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Integer sel = (Integer) comboBox.getSelectedItem();
				seasonsCount = sel + "";
				progressBarTv.setVisible(true);
				tableModelTv.setRowCount(0);  // pulisci tabella
				new SearchTVService(id+"-"+slug+"/season-"+sel, progressBarTv, consoleFrame, tableModelTv).execute();
			}
		});


		// CENTRO tabella risultati
		String[] columnNames = {"#", "Name", "Duration", "Quality", "Date", "ID", "Plot"};
		tableModelTv = new DefaultTableModel(columnNames, 0) {
			private static final long serialVersionUID = 5020507724059423671L;
			// Rendi tutte le celle non editabili
			@Override
			public boolean isCellEditable(int row, int column) {
				return false;
			}
		};

		resultTableTv = new JTable(tableModelTv);
		resultTableTv.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		resultTableTv.getColumnModel().getColumn(Constants.COLUMN_NUMBER_TV).setPreferredWidth(30); // colonna 0 (Number)
		resultTableTv.getColumnModel().getColumn(Constants.COLUMN_NAME_TV).setPreferredWidth(380); // colonna 1 (Name)
		resultTableTv.getColumnModel().getColumn(Constants.COLUMN_DURATION_TV).setPreferredWidth(30); // colonna 2 (Duration)
		resultTableTv.getColumnModel().getColumn(Constants.COLUMN_QUALITY_TV).setPreferredWidth(15);  // colonna 3 (Quality)
		resultTableTv.getColumnModel().getColumn(Constants.COLUMN_DATE_TV).setPreferredWidth(60); // colonna 4 (Date)
		resultTableTv.getColumnModel().removeColumn(resultTableTv.getColumnModel().getColumn(Constants.COLUMN_PLOT_TV)); // colonna 6 (plot)
		resultTableTv.getColumnModel().removeColumn(resultTableTv.getColumnModel().getColumn(Constants.COLUMN_ID_TV)); // colonna 5 (id)

		JScrollPane scrollPane = new JScrollPane(resultTableTv);
		add(scrollPane, BorderLayout.CENTER);

		// SUD progress bar
		progressBarTv = new JProgressBar();
		progressBarTv.setIndeterminate(true);
		progressBarTv.setVisible(false);
		add(progressBarTv, BorderLayout.SOUTH);


		//Crea il menu a tendina
		JPopupMenu popupMenu = new JPopupMenu();
		JMenuItem downloadTv = new JMenuItem("Download");
		popupMenu.add(downloadTv);

		downloadTv.addActionListener(e -> {
			int selectedRow = resultTableTv.getSelectedRow();
			consoleFrame.appendLine(selectedRow + "");
			if (selectedRow >= 0) {
				String idTv = (String) resultTableTv.getModel().getValueAt(selectedRow, Constants.COLUMN_ID_TV);
				String titleEp = (String) resultTableTv.getModel().getValueAt(selectedRow, Constants.COLUMN_NAME_TV);
				String episode = (String) resultTableTv.getModel().getValueAt(selectedRow, Constants.COLUMN_NUMBER_TV);
				String date = (String) resultTableTv.getModel().getValueAt(selectedRow, Constants.COLUMN_DATE_TV);
				String fileNameTmp = title + ".S" + Utils.formatNumber(seasonsCount) + ".E" + Utils.formatNumber(episode) + "." + titleEp;
				
				Content content = new Content();
				content.setId(id+"?episode_id="+idTv+"&next_episode=1");
				content.setTitle(titleEp);
				content.setEpisode(episode);
				content.setDate(date);
				content.setFileNameTmp(fileNameTmp);
				
				consoleFrame.appendLine(content.toString());
				new DownloadMovieTvService(content, progressBarTv, consoleFrame).execute();

			}
		});

		resultTableTv.addMouseListener(new MouseAdapter() {
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
				int row = resultTableTv.rowAtPoint(e.getPoint());

				if (row >= 0 && row < resultTableTv.getRowCount()) {
					resultTableTv.setRowSelectionInterval(row, row); // seleziona riga
				} else {
					resultTableTv.clearSelection();
				}
				popupMenu.show(e.getComponent(), e.getX(), e.getY());
			}
		});

		progressBarTv.setVisible(true);
		new SearchTVService(id+"-"+slug+"/season-"+seasons_count, progressBarTv, consoleFrame, tableModelTv).execute();

	}

}
