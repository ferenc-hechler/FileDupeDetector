package de.hechler.filedupedetector.gui.objects;

import java.util.ArrayList;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JTable;

public class ChangeSearchFolderWindow extends JFrame {
	
	private static final int TABLE_LEN = 6;

	/** UID */
	private static final long serialVersionUID = 1610025833505675074L;
	
	private static final int WIDTH = 1500;
	private static final int HEIGHT = 150;
	
	
	
	private JTable table;
	private MenuButton finish;
	private volatile Runnable run;
	
	
	
	public ChangeSearchFolderWindow() {
	}
	
	public ChangeSearchFolderWindow load() {
		setDefaultCloseOperation(HIDE_ON_CLOSE);// Do not kill main window
		setBounds(100, 100, WIDTH, HEIGHT);
		
		setVisible(false);
		
		table = new JTable(2, TABLE_LEN);
		table.setValueAt("search", 0, 0);
		table.setValueAt("read old", 1, 0);
		table.setShowGrid(true);
		table.setColumnSelectionAllowed(true);
		table.setRowHeight(15);
		table.setBounds(70, 10, 300, 30);
		add(table);
		
		for (int i = 1; i < TABLE_LEN; i ++ ) {
			for (int ii = 0; ii < 2; ii ++ ) {
				table.setValueAt("", ii, i);
			}
		}

		finish = new MenuButton().load(10, 10, new ImageIcon("./changeFinish.png"), a -> {
			setVisible(false);
			run.run();
		});
		
		add(finish);
		return this;
	}
	
	public void init(Runnable run) {
		setVisible(true);
		toFront();
		
		repaint();
		
		this.run = run;
	}

	public List<String> getSearch() {
		List<String> erg = new ArrayList <String>();
		String zw;
		for (int i = 1; i < TABLE_LEN; i ++ ) {
			zw = table.getValueAt(0, i).toString();
			if (!zw.isBlank()) {
				erg.add(zw);
			}
		}
		return erg;
	}
	public List<String> getRead() {
		List<String> erg = new ArrayList <String>();
		String zw;
		for (int i = 1; i < TABLE_LEN; i ++ ) {
			zw = table.getValueAt(1, i).toString();
			if (!zw.isBlank()) {
				erg.add(zw);
			}
		}
		return erg;
	}
	
}
