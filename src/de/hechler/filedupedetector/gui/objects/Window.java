package de.hechler.filedupedetector.gui.objects;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
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
	
	private static final Comparator<GuiInterface> COMPERATOR = (a, b) -> {
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
		return Long.compare(sumB.getTotalMemory(), sumA.getTotalMemory());
	};
	
	
	
	private Table table;
	private ScollButton up;
	private ScollButton down;
	private MenuButton changeSearchFolderButton;
	private ChangeSearchFolderWindow changeSearchFolderWindow;
	private MenuButton reload;
	private GoInButton[] goIn;
	private JButton goOut;
	private ScanStore scanStore;
	private List <StackElement> stack;
	private GuiInterface[] element;
	private int index;
	private List <String> search;
	private List <String> read;
	private volatile boolean blocked = false;
	private long parentMemory;
	
	
	private class StackElement {
		GuiInterface[] value;
		long parentMemory;
		
		public StackElement(GuiInterface[] value, long parentMemory) {
			StackElement.this.value = value;
			StackElement.this.parentMemory = parentMemory;
		}
	}
	
	
	
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
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setVisible(true);
		
		changeSearchFolderWindow = new ChangeSearchFolderWindow().load();
		
		changeSearchFolderButton = new MenuButton().load(X_1, Y_1, new ImageIcon("./icons/changeSearchFolder.png"), a -> changeSearchFolder());
		reload = new MenuButton().load(X_2, Y_1, new ImageIcon("./icons/reload.png"), a -> reload());
		table = new Table().load(X_2, Y_2);
		up = new ScollButton().load(X_3, Y_3, true, this);
		down = new ScollButton().load(X_3, Y_2, false, this);
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
		
		goOut.setVisible(false);
		up.setVisible(false);
		down.setVisible(false);
		
		goIn = new GoInButton[Table.ELEMENT_CNT];
		
		for (int i = 0; i < goIn.length; i ++ ) {
			goIn[i] = new GoInButton().load(X_1, Y_2 + GoInButton.HEIGHT * (i + 1), this, i);
			add(goIn[i]);
			goIn[i].setVisible(false);
		}
		
		changeSearchFolderWindow.initforce(() -> {
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
				Arrays.parallelSort(element, COMPERATOR);
				scanStore.calcSumInfoFromChildren();
				parentMemory = scanStore.getSumInfo().getTotalMemory();
				rebuildTable();
				blocked = false;
			}).start();
		});
		
		return this;
	}
	
	
	
	private void goOut() {
		StackElement se = stack.remove(stack.size() - 1);
		element = se.value;
		parentMemory = se.parentMemory;
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
					for (int ii = 0; ii < Table.COLUMS; ii ++ ) {
						table.setValueAt("", i, ii);
					}
				}
				break;
			}
			GuiInterface e = element[i + index];
			table.setValueAt(e.getName(), i, Table.NAME);
			SumInfo sum = e.getSumInfo();
			if (sum == null) {
				e.refreshSumInfo();
				sum = e.getSumInfo();
			}
			long tm = sum.getTotalMemory();
			boolean folder = e.isFolder();
			table.setValueAt(e.getName(), i, Table.NAME);
			table.setValueAt(Utils.readableBytes(tm), i, Table.SPEICHER_PLATZ);
			table.setValueAt((int) (tm * 100.0 / parentMemory) + "%", i, Table.SPEICHER_PLATZ_PROZENT);
			table.setValueAt((int) (sum.getDuplicateMemory() * 100.0 / tm) + "%", i, Table.DOPPELT_PROZENT);
			table.setValueAt(Utils.readableBytes(sum.getDuplicateMemory()), i, Table.DOPPELT);
			table.setValueAt(sum.getLastModifiedString(), i, Table.LAST_MODIFIED);
			table.setValueAt(folder ? "folder" : e.isFile() ? "file" : "unknown", i, Table.ART);
			if (folder && (e.getChildFiles().size() != 0 || e.getChildFolders().size() != 0)) {
				goIn[i].setVisible(true);
			} else {
				goIn[i].setVisible(false);
			}
		}
		repaint();
	}
	
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
			new FinishRefresh().load();
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
				Arrays.parallelSort(element, COMPERATOR);
				scanStore.calcSumInfoFromChildren();
				rebuildTable();
				blocked = false;
			}).start();
		}, () -> blocked = false);
	}
	
	public void scoll(boolean down) {
		index += down ? Table.ELEMENT_CNT : -Table.ELEMENT_CNT;
		rebuildTable();
	}
	
	public void goIn(int index) {
		index += this.index;
		stack.add(new StackElement(element, parentMemory));
		parentMemory = element[index].getSumInfo().getTotalMemory();
		List <GuiInterface> zw = new ArrayList <>(element[index].getChildFiles());
		zw.addAll(element[index].getChildFolders());
		element = zw.toArray(new GuiInterface[zw.size()]);
		Arrays.parallelSort(element, COMPERATOR);
		this.index = 0;
		rebuildTable();
	}
	
}
