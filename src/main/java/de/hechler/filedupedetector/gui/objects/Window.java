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
	
	private static final int WIDTH  = 910;
	private static final int HEIGHT = 660;
	
	public static final int                        VOID       = 10;
	private static final int                       X_1        = VOID;
	private static final int                       X_2        = X_1 + VOID + MenuButton.SIZE;
	private static final int                       X_3        = X_2 + VOID + Table.WIDTH;
	private static final int                       Y_1        = VOID;
	private static final int                       Y_2        = Y_1 + VOID + MenuButton.SIZE;
	private static final int                       Y_3        = Y_2 + VOID + ScollButton.HEIGHT;
	private static final Comparator <GuiInterface> COMPERATOR = (a, b) -> {
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
	
	private Table                    table;
	private ScollButton              up;
	private ScollButton              down;
	private MenuButton               changeSearchFolderButton;
	private ChangeSearchFolderWindow changeSearchFolderWindow;
	private MenuButton               reload;
	private MenuButton               saveIn;
	private MenuButton               delete;
	private SmallButton[]            goIn;
	private JButton                  goOut;
	private ScanStore                scanStore;
	private List <StackElement>      stack;
	private GuiInterface[]           element;
	private int                      index;
	private List <String>            search;
	private List <String>            read;
	private volatile boolean         blocked = false;
	private GuiInterface             parent;
	private String                   filter;
	
	
	private class StackElement {
		
		GuiInterface[] value;
		GuiInterface   parent;
		
		public StackElement(GuiInterface[] value, GuiInterface parent) {
			StackElement.this.value = value;
			StackElement.this.parent = parent;
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
		saveIn = new MenuButton().load(X_2 + VOID + MenuButton.SIZE, Y_1, new ImageIcon("./icons/save.png"), a -> saveIn());
		delete = new MenuButton().load(X_2 + VOID + MenuButton.SIZE + VOID + MenuButton.SIZE, Y_1, new ImageIcon("./icons/deleteMarked.png"), a -> delete());
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
		add(delete);
		add(saveIn);
		add(reload);
		add(changeSearchFolderButton);
		
		goOut.setVisible(false);
		up.setVisible(false);
		down.setVisible(false);
		
		goIn = new SmallButton[Table.ELEMENT_CNT];
		
		for (int i = 0; i < goIn.length; i ++ ) {
			final int index = i;
			goIn[i] = new SmallButton().load(X_1, Y_2 + SmallButton.HEIGHT * (i + 1), new ImageIcon("./icons/goIn.png"), e -> goIn(index));
			add(goIn[i]);
			goIn[i].setVisible(false);
		}
		
		changeSearchFolderWindow.initforce((filter) -> {
			new Thread(() -> {
				read = changeSearchFolderWindow.getRead();
				search = changeSearchFolderWindow.getSearch();
				for (String zw : read) {
					scanStore.read(zw);
				}
				for (String zw : search) {
					scanStore.scanFolder(zw);
				}
				this.filter = filter;
				if ( !"".equals(filter)) {
					scanStore.filterCategories(filter);
				}
				stack = new ArrayList <>();
				index = 0;
				List <GuiInterface> zw = new ArrayList <>(scanStore.getChildFiles());
				zw.addAll(scanStore.getChildFolders());
				element = zw.toArray(new GuiInterface[zw.size()]);
				Arrays.parallelSort(element, COMPERATOR);
				scanStore.calcSumInfoFromChildren();
				parent = null;
				rebuildTable();
				blocked = false;
			}).start();
		});
		
		return this;
	}
	
	private void delete() {
		if (blocked) return;
		blocked = true;
		table.forEachSelectedRow((rem) -> {
			if (rem >= element.length) return;
			rem += index - 1;
			element[rem].delete();
			GuiInterface[] neu = Arrays.copyOf(element, element.length - 1);
			System.arraycopy(element, rem + 1, neu, rem, neu.length - rem);
			element = neu;
		});
		rebuildTable();
		blocked = false;
	}
	
	private void saveIn() {
		new SaveWindow().load(this);
	}
	
	private void goOut() {
		StackElement se = stack.remove(stack.size() - 1);
		element = se.value;
		parent = se.parent;
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
						table.setValue("", i, ii);
					}
				}
				break;
			}
			GuiInterface e = element[i + index];
			table.setValue(e.getName(), i, Table.NAME);
			SumInfo sum = e.getSumInfo();
			if (sum == null) {
				e.refreshSumInfo();
				sum = e.getSumInfo();
			}
			long tm = sum.getTotalMemory();
			boolean folder = e.isFolder();
			table.setValue(e.getName(), i, Table.NAME);
			table.setValue(Utils.readableBytes(tm), i, Table.SPEICHER_PLATZ);
			SumInfo parentsum;
			if (parent == null) {
				parentsum = scanStore.getSumInfo();
				if (parentsum == null) {
					scanStore.refreshSumInfo();
					parentsum = parent.getSumInfo();
				}
			} else {
				parentsum = parent.getSumInfo();
				if (parentsum == null) {
					parent.refreshSumInfo();
					parentsum = parent.getSumInfo();
				}
			}
			table.setValue((int) (tm * 100.0 / parentsum.getTotalMemory()) + "%", i, Table.SPEICHER_PLATZ_PROZENT);
			table.setValue((int) (sum.getDuplicateMemory() * 100.0 / tm) + "%", i, Table.DOPPELT_PROZENT);
			table.setValue(Utils.readableBytes(sum.getDuplicateMemory()), i, Table.DOPPELT);
			table.setValue(sum.getLastModifiedString(), i, Table.LAST_MODIFIED);
			table.setValue(e.isVolume() ? "volume" : folder ? "folder" : e.isFile() ? "file" : "unknown", i, Table.ART);
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
			if ( !"".equals(filter)) {
				scanStore.filterCategories(filter);
			}
			scanStore.calcSumInfoFromChildren();
			index = 0;
			stack = new ArrayList <>();
			List <GuiInterface> zw = new ArrayList <>();
			zw.addAll(scanStore.getChildFiles());
			zw.addAll(scanStore.getChildFolders());
			element = zw.toArray(new GuiInterface[zw.size()]);
			rebuildTable();
			blocked = false;
			new FinishRefreshWindow().load();
		}).start();
	}
	
	public void changeSearchFolder() {
		if (blocked) return;
		blocked = true;
		scanStore = new ScanStore();
		changeSearchFolderWindow.init((filter) -> {
			new Thread(() -> {
				ChangeSearchLogWindow log = new ChangeSearchLogWindow().load();
				read = changeSearchFolderWindow.getRead();
				search = changeSearchFolderWindow.getSearch();
				for (String zw : read) {
					scanStore.read(zw);
				}
				for (String zw : search) {
					scanStore.scanFolder(zw);
				}
				this.filter = filter;
				if ( !"".equals(filter)) {
					scanStore.filterCategories(filter);
				}
				stack = new ArrayList <>();
				index = 0;
				List <GuiInterface> zw = new ArrayList <>(scanStore.getChildFiles());
				zw.addAll(scanStore.getChildFolders());
				element = zw.toArray(new GuiInterface[zw.size()]);
				Arrays.parallelSort(element, COMPERATOR);
				scanStore.calcSumInfoFromChildren();
				rebuildTable();
				log.finish();
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
		stack.add(new StackElement(element, element[index]));
		parent = element[index];
		List <GuiInterface> zw = new ArrayList <>(element[index].getChildFiles());
		zw.addAll(element[index].getChildFolders());
		element = zw.toArray(new GuiInterface[zw.size()]);
		Arrays.parallelSort(element, COMPERATOR);
		this.index = 0;
		rebuildTable();
	}
	
	public void saveTo(String file) {
		scanStore.write(file);
	}
	
}
