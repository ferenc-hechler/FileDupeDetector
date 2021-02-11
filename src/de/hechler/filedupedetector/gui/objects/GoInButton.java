package de.hechler.filedupedetector.gui.objects;

import javax.swing.JButton;

public class GoInButton extends JButton {
	
	/** UID */
	private static final long serialVersionUID = -2474422339193939902L;
	
	private static final int WIDTH = 10;
	private static final int HEIGHT = 10;
	
	
	
	public GoInButton() {
		// TODO Auto-generated constructor stub
	}
	
	public GoInButton load(int x, int y, Table goIn, int index) {
		setBounds(x, y, WIDTH, HEIGHT);
		
		addActionListener(e -> goIn.goIn(index));
		
		return this;
	}
	
	public GoInButton load(int x, int y, Table goOut) {
		setBounds(x, y, WIDTH, HEIGHT);
		
		addActionListener(e -> goOut.goOut());
		
		return this;
	}
	
}
