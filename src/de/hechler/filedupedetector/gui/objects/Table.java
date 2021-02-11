package de.hechler.filedupedetector.gui.objects;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JTable;

public class Table extends JTable {
	
	
	
	/** UID */
	private static final long serialVersionUID = -3773804780193596904L;
	
	
	private static final int ROW_HIGH = 15;
	
	public static final int ELEMENT_CNT = 35;
	
	public static final int WIDTH = 750;
	public static final int HEIGHT = (ELEMENT_CNT + 1) * ROW_HIGH;
	
	public static final int NAME = 0;
	private static final int FREI_SPEICHER = 1;
	public static final int SPEICHER_PLATZ = 2;
	public static final int SPEICHER_PLATZ_PROZENT = 3;
	public static final int DOPPELT_PROZENT = 4;
	public static final int LAST_MODIFIED = 5;
	
	
	
	public Table() {
		super(ELEMENT_CNT + 1, 6);
	}
	
	public Table load(int x, int y, Void content) {// TODO use content + disallow user to edit the table
		setCellSelectionEnabled(false);
		setRowSelectionAllowed(true);
		setEnabled(true);
		
		setBounds(x, y, WIDTH, HEIGHT);
		setRowHeight(ROW_HIGH);
		
		setValueAt("name", 0, NAME);
		setValueAt("freier speicher", 0, FREI_SPEICHER);
		setValueAt("gr��e", 0, SPEICHER_PLATZ);
		setValueAt("gr��e (%)", 0, SPEICHER_PLATZ_PROZENT);
		setValueAt("doppelt (%)", 0, DOPPELT_PROZENT);
		setValueAt("letzte �nderung", 0, LAST_MODIFIED);
		
		setAutoCreateColumnsFromModel(true);
		setShowGrid(true);
		
		return this;
	}
	
	public void generateGoIns(List<GoInButton> goIn) {
		// TODO Auto-generated method stub
	}
	
	public void scoll(boolean scroll) {
		// TODO Auto-generated method stub
	}
	
	public void goOut() {
		// TODO Auto-generated method stub
	}
	
	public void goIn(int index) {
		// TODO Auto-generated method stub
	}

	public void deleteSelected() {
		// TODO Auto-generated method stub
	}
	
}