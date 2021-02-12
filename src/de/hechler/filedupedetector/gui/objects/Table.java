package de.hechler.filedupedetector.gui.objects;

import javax.swing.JTable;

public class Table extends JTable {
	
	
	
	/** UID */
	private static final long serialVersionUID = -3773804780193596904L;
	
	
	private static final int ROW_HIGH = 15;
	
	public static final int ELEMENT_CNT = 35;
	
	public static final int WIDTH = 750;
	public static final int HEIGHT = (ELEMENT_CNT + 1) * ROW_HIGH;
	
	public static final int NAME = 0;
	public static final int SPEICHER_PLATZ = 1;
	public static final int SPEICHER_PLATZ_PROZENT = 2;
	public static final int DOPPELT = 3;
	public static final int DOPPELT_PROZENT = 4;
	public static final int LAST_MODIFIED = 5;


	
	
	
	public Table() {
		super(ELEMENT_CNT + 1, 6);
	}
	
	@Override
	public void setValueAt(Object aValue, int row, int column) {
		if (row < 0) {
			throw new IllegalArgumentException("row < 0 | row=" + row);
		}
		super.setValueAt(aValue, row + 1, column);
	}
	
	public Table load(int x, int y) {// TODO disallow user to edit the table
		setCellSelectionEnabled(false);
		setRowSelectionAllowed(true);
		setEnabled(true);
		
		setBounds(x, y, WIDTH, HEIGHT);
		setRowHeight(ROW_HIGH);
		
		super.setValueAt("name", 0, NAME);
		super.setValueAt("größe", 0, SPEICHER_PLATZ);
		super.setValueAt("größe (%)", 0, SPEICHER_PLATZ_PROZENT);
		super.setValueAt("doppelt", 0, DOPPELT);
		super.setValueAt("doppelt (%)", 0, DOPPELT_PROZENT);
		super.setValueAt("letzte änderung", 0, LAST_MODIFIED);
		
		for (int i = 0; i < ELEMENT_CNT; i ++ ) {
			for (int ii = 0; ii < 5; ii ++ ) {
				setValueAt("", i, ii);
			}
		}
		
		setAutoCreateColumnsFromModel(true);
		setShowGrid(true);
		
		return this;
	}
	
	public void set() {
		
	}
	
}
