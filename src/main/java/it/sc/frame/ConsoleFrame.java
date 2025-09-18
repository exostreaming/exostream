package it.sc.frame;

import java.awt.BorderLayout;
import java.awt.Font;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class ConsoleFrame extends JFrame {

	private static final long serialVersionUID = -4403038911734615274L;
	private final JTextArea consoleArea;

    public ConsoleFrame() {
        setTitle("Console Output");
        setSize(500, 300);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);  
		ImageIcon icon = new ImageIcon("bin/icon.png");
		setIconImage(icon.getImage());
		
        consoleArea = new JTextArea();
        consoleArea.setEditable(false);
        consoleArea.setFont(new Font("Monospaced", Font.PLAIN, 11));

        JScrollPane scrollPane = new JScrollPane(consoleArea);
        add(scrollPane, BorderLayout.CENTER);
    }

    public void appendLine(String text) {
        consoleArea.append(text + "\n");
        consoleArea.setCaretPosition(consoleArea.getDocument().getLength());  // Scroll automatico
    }

    public void clear() {
        consoleArea.setText("");
    }
}
