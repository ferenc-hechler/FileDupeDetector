package de.hechler.filedupedetector.gui.objects;

import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JButton;

public class MenuButton extends JButton {
	
	/** UID */
	private static final long serialVersionUID = -9079654012598600077L;
	
	public static final int SIZE = 50;
	private static final int WIDTH = SIZE;
	private static final int HEIGHT = SIZE;
	
	
	
	public MenuButton() {
	}
	
	public MenuButton load(int x, int y, ImageIcon imageIcon, ActionListener action) {
		setBounds(x, y, WIDTH, HEIGHT);
		setIcon(imageIcon);
		addActionListener(action);
		setVisible(true);
		return this;
	}

}
