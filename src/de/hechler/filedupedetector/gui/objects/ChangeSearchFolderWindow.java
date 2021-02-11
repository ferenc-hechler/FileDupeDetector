package de.hechler.filedupedetector.gui.objects;

import javax.swing.JFrame;

public class ChangeSearchFolderWindow extends JFrame {
	
	/** UID */
	private static final long serialVersionUID = 1610025833505675074L;
	
	
	public ChangeSearchFolderWindow() {
	}
	
	public ChangeSearchFolderWindow load() {
		// TODO Auto-generated method stub
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);//Do not kill main window
		setVisible(true);
		return this;
	}
	
}
