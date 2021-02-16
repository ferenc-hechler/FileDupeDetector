package de.hechler.filedupedetector.gui.objects;

import java.util.Arrays;
import java.util.function.Consumer;

import javax.swing.JTable;

public class Table extends JTable {
	
	private static final String ART_VAL = "art";
	
	
	private static final String LAST_MODIFIED_VAL = "letzte änderung";
	
	
	private static final String DOPPELT_PROZENT_VAL = "doppelt (%)";
	
	
	private static final String DOPPELT_VAL = "doppelt";
	
	
	private static final String SPEICHER_PLATZ_PROZENT_VAL = "größe (%)";
	
	
	private static final String SPEICHER_PLATZ_VAL = "größe";
	
	
	private static final String NAME_VAL = "name";
	
	
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
	public static final int ART = 6;
	public static final int COLUMS = 7;
	
	
	private static final boolean USER_ALLOWED_TO_EDIT_TABLE = false;
	private volatile String[][] table;
	
	public Table() {
		super(ELEMENT_CNT + 1, COLUMS);
	}
	
	/**
	 * This will be ignored, when {@link #USER_ALLOWED_TO_EDIT_TABLE} is <code>false</code> (default: <code>false</code>)
	 */
	@Override
	@Deprecated
	public void setValueAt(Object aValue, int row, int column) {
		if (USER_ALLOWED_TO_EDIT_TABLE) {
			super.setValueAt(aValue, row, column);
		}
	}
	
	public synchronized void setValue(String aValue, int row, int column) {
		if (row < 0) {
			throw new IllegalArgumentException("row < 0 | row=" + row);
		}
		table[row][column] = aValue;
		super.setValueAt(aValue, row + 1, column);
	}
	
	public String valueOf(int row, int column) {
		return table[row][column];
	}
	
	public synchronized Table load(int x, int y) {
		table = new String[ELEMENT_CNT][COLUMS];
		
		setCellSelectionEnabled(false);
		setRowSelectionAllowed(true);
		setEnabled(true); // allows user to select rows (and to modify the table)
		
		setBounds(x, y, WIDTH, HEIGHT);
		setRowHeight(ROW_HIGH);
		
		super.setValueAt(NAME_VAL, 0, NAME);
		super.setValueAt(SPEICHER_PLATZ_VAL, 0, SPEICHER_PLATZ);
		super.setValueAt(SPEICHER_PLATZ_PROZENT_VAL, 0, SPEICHER_PLATZ_PROZENT);
		super.setValueAt(DOPPELT_VAL, 0, DOPPELT);
		super.setValueAt(DOPPELT_PROZENT_VAL, 0, DOPPELT_PROZENT);
		super.setValueAt(LAST_MODIFIED_VAL, 0, LAST_MODIFIED);
		super.setValueAt(ART_VAL, 0, ART);
		
		for (int i = 0; i < ELEMENT_CNT; i ++ ) {
			for (int ii = 0; ii < COLUMS; ii ++ ) {
				setValue("", i, ii);
			}
		}
		
		setAutoCreateColumnsFromModel(true);
		setShowGrid(true);
		
		return this;
	}
	
	public void set() {
		
	}
	
	@Override
	public int getSelectedRow() {
		int select = super.getSelectedRow();
		if (select == -1) {
			return select;
		} else {
			return select - 1;
		}
	}
	
	@Override
	public int[] getSelectedColumns() {
		int[] selected = super.getSelectedColumns();
		for (int i = 0; i < selected.length; i ++ ) {
			if (selected[i] == 0) {
				int[] zw = Arrays.copyOf(selected, selected.length - 1);
				System.arraycopy(selected, i + 1, zw, i, zw.length - i);
				break;
			}
		}
		return selected;
	}
	
	public void forEachSelectedRow(Consumer <? super Integer> action) {
		int[] sel = super.getSelectedRows();
		for (int i = 0; i < sel.length; i ++ ) {
			if (sel[i] == 0) continue;
			action.accept(sel[i]);
		}
	}
	
}
