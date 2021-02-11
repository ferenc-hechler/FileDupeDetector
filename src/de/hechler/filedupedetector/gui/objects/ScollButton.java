package de.hechler.filedupedetector.gui.objects;

import javax.swing.ImageIcon;
import javax.swing.JButton;

public class ScollButton extends JButton {
	
	/** UID */
	private static final long serialVersionUID = -4977167227286249289L;
	
	
	
	public static final int WIDTH = 50;
	public static final int HEIGHT = 250;
	
	
	
	public ScollButton() {
	}
	
	
	public ScollButton load(int x, int y, boolean scroll, Table table) {
		setBounds(x, y, WIDTH, HEIGHT);
		
		if (scroll) {
			setIcon(new ImageIcon("./icons/scollButtonUp.png"));
		} else {
			setIcon(new ImageIcon("./icons/scollButtonDown.png"));
		}
		
		addActionListener(e -> table.scoll(scroll));
		
		return this;
	}
	
	
}