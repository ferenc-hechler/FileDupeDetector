package de.hechler.filedupedetector.gui.objects;

import javax.swing.JFrame;
import javax.swing.JTextField;

public class ChangeSearchLogWindow extends JFrame {
	
	/** UID */
	private static final long serialVersionUID = 7728412941961086507L;
	
	private JTextField text;
	
	public ChangeSearchLogWindow() {
	}
	
	public ChangeSearchLogWindow load() {
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		setLayout(null);
		setLocationRelativeTo(null);
		
		text = new JTextField("working");
		setBounds(0, 0, 50, 100);
		text.setBounds(10, 10, 80, 15);
		add(text);
		
		setVisible(true);
		toFront();
		return this;
	}
	
	
	
	public void setText(String text) {
		this.text.setText(text);
	}
	
	public void finish() {
		text.setText("finish");
	}
	
}
