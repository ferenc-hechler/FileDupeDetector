package de.hechler.filedupedetector.gui.objects;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JFrame;

import de.hechler.filedupedetector.GuiInterface;
import de.hechler.filedupedetector.ScanStore;
import de.hechler.filedupedetector.SumInfo;

public class Window extends JFrame {
	
	/** UID */
	private static final long serialVersionUID = 6510610980424957351L;
	
	
	
	private static final int WIDTH = 1000;
	private static final int HEIGHT = 1000;
	
	public static final int VOID = 10;
	private static final int X_1 = VOID;
	private static final int X_2 = X_1 + VOID + MenuButton.SIZE;
	private static final int X_3 = X_2 + VOID + MenuButton.SIZE;
	private static final int X_4 = X_2 + VOID + Table.WIDTH;
	private static final int Y_1 = VOID;
	private static final int Y_2 = Y_1 + VOID + MenuButton.SIZE;
	private static final int Y_3 = Y_2 + VOID + ScollButton.HEIGHT;
	
	
	
	private Table table;
	private ScollButton up;
	private ScollButton down;
	private MenuButton changeSearchFolderButton;
	private ChangeSearchFolderWindow changeSearchFolderWindow;
	private MenuButton reload;
	private MenuButton deleteSelected;
	private GoInButton[] goIn;
	private MenuButton goOut;
	private ScanStore scanStore;
	private List <GuiInterface[]> stack;
	private GuiInterface[] element;
	private int index;
	private List <String> search;
	private List <String> read;
	
	
	
	public Window() {
	}
	
	public Window load() {
		scanStore = new ScanStore();
		element = null;
		stack = new ArrayList <>();
		read = new ArrayList <String>();
		search = new ArrayList <String>();
		
		setBounds(0, 0, WIDTH, HEIGHT);
		setResizable(false);
		setLayout(null);
		setLocationRelativeTo(null);
		
		changeSearchFolderWindow = new ChangeSearchFolderWindow().load();
		
		changeSearchFolderButton = new MenuButton().load(X_1, Y_1, new ImageIcon("./icons/changeSearchFolder.png"), a -> changeSearchFolder());
		reload = new MenuButton().load(X_2, Y_1, new ImageIcon("./icons/reload.png"), a -> reload());
		deleteSelected = new MenuButton().load(X_3, Y_1, new ImageIcon("./icons/delSelect.png"), a -> deleteSelected());
		table = new Table().load(X_2, Y_2);
		goOut = new MenuButton().load(X_1, Y_2, new ImageIcon("./icons/goOut.png"), a -> goOut());
		up = new ScollButton().load(X_4, Y_2, true, this);
		down = new ScollButton().load(X_4, Y_3, false, this);
		add(down);
		add(up);
		add(table);
		add(deleteSelected);
		add(changeSearchFolderButton);
		add(reload);
		
		setVisible(true);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		
		repaint();
		
		changeSearchFolder();
		
		return this;
	}
	
	private void goOut() {
		element = stack.remove(stack.size() - 1);
		index = 0;
		rebuildTable();
	}
	
	private void rebuildTable() {
		for (int i = 0; i < Table.ELEMENT_CNT; i ++ ) {
			GuiInterface e = element[i + index];
			table.setValueAt(e.getName(), Table.NAME, i);
			SumInfo sum = e.getSumInfo();
			long tm = sum.getTotalMemory();
			table.setValueAt(tm, Table.SPEICHER_PLATZ, i);
			table.setValueAt("unknown", Table.SPEICHER_PLATZ_PROZENT, i);
			table.setValueAt(sum.getDuplicateMemory() * 100.0 / tm, Table.DOPPELT_PROZENT, i);
			table.setValueAt("unknown", Table.LAST_MODIFIED, i);
			table.setValueAt("unknown", Table.FREI_SPEICHER, i);
		}
	}
	
	public void deleteSelected() {
		int[] del = table.getSelectedRows();
		for (int rem : del) {
			if (rem == 0) continue; // ignore the head of the table
			String val = table.getValueAt(rem, 0).toString();
			Path path = Paths.get(val);
			try {
				Files.delete(path);
			} catch (IOException e) {
				new ErrorWindow().load(e, "could not delete");
			}
		}
		rebuildTable();
	}
	
	public void reload() {
		new Thread(() -> {
			scanStore = new ScanStore();
			for (String zw : read) {
				scanStore.read(zw);
			}
			for (String zw : search) {
				scanStore.scanFolder(zw);
			}
			repaint();
		}).start();
	}
	
	public void changeSearchFolder() {
		scanStore = new ScanStore();
		changeSearchFolderWindow.init(() -> {
			read = changeSearchFolderWindow.getRead();
			search = changeSearchFolderWindow.getSearch();
			for (String zw : read) {
				scanStore.read(zw);
			}
			for (String zw : search) {
				scanStore.scanFolder(zw);
			}
			stack = new ArrayList <>();
			index = 0;
			List <GuiInterface> zw = scanStore.getChildFiles();
			zw.addAll(scanStore.getChildFolders());
			element = zw.toArray(new GuiInterface[zw.size()]);
			Arrays.parallelSort(element, (a,b) -> Long.compare(a.getSumInfo().getTotalMemory(), b.getSumInfo().getTotalMemory()));
			rebuildTable();
		});
	}
	
	public void scoll(boolean up) {
		index += up ? Table.ELEMENT_CNT : -Table.ELEMENT_CNT;
		rebuildTable();
	}
	
	public void goIn(int index) {
		index += this.index;
		stack.add(element);
		List <GuiInterface> zw = element[index].getChildFiles();
		zw.addAll(element[index].getChildFolders());
		element = zw.toArray(new GuiInterface[zw.size()]);
		this.index = 0;
		rebuildTable();
	}
	
}
