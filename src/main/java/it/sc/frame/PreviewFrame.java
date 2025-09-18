package it.sc.frame;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;

import it.sc.services.FilenameService;
import it.sc.services.PreviewService;
import it.sc.utility.Constants;

public class PreviewFrame extends JFrame {

	private static final long serialVersionUID = -1035476888750220027L;

	private JProgressBar progressBarPreview;

	public PreviewFrame(String id, String title, String slug, String type, ConsoleFrame consoleFrame) {
		setTitle(Constants.TITLE);
		setSize(600, 400);
		setLocationRelativeTo(null);
		setLayout(new BorderLayout());

		ImageIcon icon = new ImageIcon("bin/icon.png");
		setIconImage(icon.getImage());

		progressBarPreview = new JProgressBar();
		progressBarPreview.setIndeterminate(true);
		progressBarPreview.setVisible(false);
		add(progressBarPreview, BorderLayout.SOUTH);

		JPanel centerPanel = new JPanel();
		centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
		centerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

		JLabel titleLaBel = new JLabel("Title: " + title);
		JLabel quality = new JLabel("Loading...");
		JLabel genre = new JLabel();

		titleLaBel.setAlignmentX(Component.LEFT_ALIGNMENT);
		quality.setAlignmentX(Component.LEFT_ALIGNMENT);
		genre.setAlignmentX(Component.LEFT_ALIGNMENT);
 
		JButton trailerButton  = new JButton("Guarda il Trailer");
		trailerButton.setFocusPainted(false);
		trailerButton.setBorder(new EmptyBorder(5,10,5,10));
		trailerButton.setBackground(new Color(66, 133, 244)); // blu
		trailerButton.setForeground(Color.WHITE);
		trailerButton.setFont(new Font("SansSerif", Font.BOLD, 12));
		trailerButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
		trailerButton.setAlignmentX(Component.LEFT_ALIGNMENT);
		trailerButton.setVisible(false);

		JTextArea plot = new JTextArea("Loading...");
		plot.setEditable(false);
		plot.setLineWrap(true);
		plot.setWrapStyleWord(true);
		plot.setBackground(centerPanel.getBackground());
		plot.setAlignmentX(Component.LEFT_ALIGNMENT);

		centerPanel.add(titleLaBel);
		centerPanel.add(quality);
		centerPanel.add(genre);
		centerPanel.add(Box.createRigidArea(new Dimension(0, 10))); // Spazio
		JScrollPane scrollPane = new JScrollPane(plot);
		scrollPane.setAlignmentX(Component.LEFT_ALIGNMENT);
		scrollPane.setPreferredSize(new Dimension(560, 100)); // Regola l'altezza visibile
		scrollPane.setBorder(BorderFactory.createEmptyBorder()); // Nessun bordo extra

		centerPanel.add(scrollPane);
		centerPanel.add(trailerButton);

		add(centerPanel, BorderLayout.CENTER);

		// Avvio del servizio
		progressBarPreview.setVisible(true);
		if(type.equals(Constants.TYPE_MOVIE)) 
			new FilenameService(id, quality, consoleFrame).execute();
		
		new PreviewService(id, slug, progressBarPreview, quality, plot, trailerButton, genre).execute();
	}


}
