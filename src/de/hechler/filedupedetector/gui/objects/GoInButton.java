package de.hechler.filedupedetector.gui.objects;

import javax.swing.ImageIcon;
import javax.swing.JButton;

public class GoInButton extends JButton {
	
	/** UID */
	private static final long serialVersionUID = -2474422339193939902L;
	
	public static final int WIDTH = 50;
	public static final int HEIGHT = 15;
	
	
	
	public GoInButton() {
	}
	
	public GoInButton load(int x, int y, Window window, int index) {
		setBounds(x, y, WIDTH, HEIGHT);
		setIcon(new ImageIcon("./icons/goIn.png"));
		addActionListener(e -> window.goIn(index));
		setVisible(true);
		return this;
	}
	
}
