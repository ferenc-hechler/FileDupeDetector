package de.hechler.filedupedetector.gui.objects;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;

import de.hechler.filedupedetector.GuiInterface;
import de.hechler.filedupedetector.ScanStore;
import de.hechler.filedupedetector.SumInfo;
import de.hechler.filedupedetector.Utils;

public class Window extends JFrame {
	
	/** UID */
	private static final long serialVersionUID = 6510610980424957351L;
	
	
	
	private static final int WIDTH = 910;
	private static final int HEIGHT = 660;
	
	public static final int VOID = 10;
	private static final int X_1 = VOID;
	private static final int X_2 = X_1 + VOID + MenuButton.SIZE;
	private static final int X_3 = X_2 + VOID + Table.WIDTH;
	private static final int Y_1 = VOID;
	private static final int Y_2 = Y_1 + VOID + MenuButton.SIZE;
	private static final int Y_3 = Y_2 + VOID + ScollButton.HEIGHT;
	
	
	
	private Table table;
	private ScollButton up;
	private ScollButton down;
	private MenuButton changeSearchFolderButton;
	private ChangeSearchFolderWindow changeSearchFolderWindow;
	private MenuButton reload;
	private GoInButton[] goIn;
	private JButton goOut;
	private ScanStore scanStore;
	private List <GuiInterface[]> stack;
	private GuiInterface[] element;
	private int index;
	private List <String> search;
	private List <String> read;
	private volatile boolean blocked = false;
	
	
	
	public Window() {
		super("FileDupeDuplicator");
	}
	
	public Window load() {
		scanStore = new ScanStore();
		element = new GuiInterface[0];
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
		table = new Table().load(X_2, Y_2);
		up = new ScollButton().load(X_3, Y_2, true, this);
		down = new ScollButton().load(X_3, Y_3, false, this);
		goOut = new JButton();
		goOut.setIcon(new ImageIcon("./icons/goOut.png"));
		goOut.addActionListener(a -> goOut());
		goOut.setBounds(X_1, Y_2, 50, 15);
		add(goOut);
		add(down);
		add(up);
		add(table);
		add(changeSearchFolderButton);
		add(reload);
		
		goIn = new GoInButton[Table.ELEMENT_CNT];
		
		for (int i = 0; i < goIn.length; i ++ ) {
			goIn[i] = new GoInButton().load(X_1, Y_2 + GoInButton.HEIGHT * (i + 1), this, i);
			add(goIn[i]);
		}
		
		setVisible(true);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		
		changeSearchFolder();
		
		return this;
	}
	
	private void goOut() {
		element = stack.remove(stack.size() - 1);
		index = 0;
		rebuildTable();
	}
	
	private void rebuildTable() {
		if (stack.isEmpty()) {
			goOut.setVisible(false);
		} else {
			goOut.setVisible(true);
		}
		if (element.length - index > Table.ELEMENT_CNT) {
			up.setVisible(true);
		} else {
			up.setVisible(false);
		}
		if (index > 0) {
			down.setVisible(true);
		} else {
			down.setVisible(false);
		}
		for (int i = 0; i < Table.ELEMENT_CNT; i ++ ) {
			if (i + index >= element.length) {
				for (; i < goIn.length; i ++ ) {
					goIn[i].setVisible(false);
				}
				break;
			}
			GuiInterface e = element[i + index];
			table.setValueAt(e.getName(), Table.NAME, i);
			SumInfo sum = e.getSumInfo();
			if (sum == null) {
				e.refreshSumInfo();
				sum = e.getSumInfo();
			}
			long tm = sum.getTotalMemory();
			table.setValueAt(e.getName(), i, Table.NAME);
			table.setValueAt(Utils.readableBytes(tm), i, Table.SPEICHER_PLATZ);
			table.setValueAt("unknown", i, Table.SPEICHER_PLATZ_PROZENT);
			table.setValueAt((int) (sum.getDuplicateMemory() * 100.0 / tm) + "%", i, Table.DOPPELT_PROZENT);
			table.setValueAt(Utils.readableBytes(sum.getDuplicateMemory()), i, Table.DOPPELT);
			table.setValueAt(sum.getLastModifiedString(), i, Table.LAST_MODIFIED);
			if (e.isFolder() && e.getChildFiles().size() != 0 && e.getChildFolders().size() != 0) {
				goIn[i].setVisible(true);
			} else {
				goIn[i].setVisible(false);
			}
		}
		repaint();
	}
	
//	Soll anders sein
//	public void deleteSelected() {
//		int[] del = table.getSelectedRows();
//		for (int rem : del) {
//			if (rem == 0) continue; // ignore the head of the table
//			String val = table.getValueAt(rem, 0).toString();
//			Path path = Paths.get(val);
//			try {
//				Files.delete(path);
//			} catch (IOException e) {
//				new ErrorWindow().load(e, "could not delete");
//			}
//		}
//		rebuildTable();
//	}
	
	public void reload() {
		if (blocked) return;
		blocked = true;
		new Thread(() -> {
			scanStore = new ScanStore();
			for (String zw : read) {
				scanStore.read(zw);
			}
			for (String zw : search) {
				scanStore.scanFolder(zw);
			}
			scanStore.calcSumInfoFromChildren();
			rebuildTable();
			blocked = false;
		}).start();
	}
	
	public void changeSearchFolder() {
		if (blocked) return;
		blocked = true;
		scanStore = new ScanStore();
		changeSearchFolderWindow.init(() -> {
			new Thread(() -> {
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
				List <GuiInterface> zw = new ArrayList <>(scanStore.getChildFiles());
				zw.addAll(scanStore.getChildFolders());
				element = zw.toArray(new GuiInterface[zw.size()]);
				Arrays.parallelSort(element, (a, b) -> {
					SumInfo sumA = a.getSumInfo();
					if (sumA == null) {
						a.refreshSumInfo();
						sumA = a.getSumInfo();
					}
					SumInfo sumB = b.getSumInfo();
					if (sumB == null) {
						b.refreshSumInfo();
						sumB = b.getSumInfo();
					}
					return Long.compare(sumA.getTotalMemory(), sumB.getTotalMemory());
				});
				scanStore.calcSumInfoFromChildren();
				rebuildTable();
				blocked = false;
			}).start();
		}, () -> blocked = false);
	}
	
	public void scoll(boolean up) {
		index += up ? Table.ELEMENT_CNT : -Table.ELEMENT_CNT;
		rebuildTable();
	}
	
	public void goIn(int index) {
		index += this.index;
		stack.add(element);
		List <GuiInterface> zw = new ArrayList <>(element[index].getChildFiles());
		zw.addAll(element[index].getChildFolders());
		element = zw.toArray(new GuiInterface[zw.size()]);
		this.index = 0;
		rebuildTable();
	}
	
}
