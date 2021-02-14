package de.hechler.filedupedetector.gui.objects;

import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JButton;

public class SmallButton extends JButton {
	
	/** UID */
	private static final long serialVersionUID = -2474422339193939902L;
	
	public static final int WIDTH = 50;
	public static final int HEIGHT = 15;
	
	
	
	public SmallButton() {
	}
	
	public SmallButton load(int x, int y, ImageIcon icon, ActionListener action) {
		setBounds(x, y, WIDTH, HEIGHT);
		setIcon(icon);
		addActionListener(e ->action.actionPerformed(e));
		setVisible(true);
		return this;
	}
	
}
