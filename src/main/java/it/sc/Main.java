package it.sc;

import javax.swing.SwingUtilities;

import it.sc.frame.NewFilmAndTvFrame;
import it.sc.utility.CheckUrl;

public class Main {
	public static void main(String[] args) {
		CheckUrl.search();
		SwingUtilities.invokeLater(() -> new NewFilmAndTvFrame().setVisible(true));
	}
}
