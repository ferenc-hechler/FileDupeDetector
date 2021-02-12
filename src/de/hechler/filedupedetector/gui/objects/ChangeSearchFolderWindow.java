package de.hechler.filedupedetector.gui.objects;

import java.util.ArrayList;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JTable;

public class ChangeSearchFolderWindow extends JFrame {
	
	private static final int TABLE_LEN = 6;
	
	/** UID */
	private static final long serialVersionUID = 1610025833505675074L;
	
	private static final int WIDTH = 420;
	private static final int HEIGHT = 90;
	
	
	
	private JTable table;
	private JButton finish;
	private JButton exit;
	private volatile Runnable run;
	private volatile Runnable kill;
	
	
	
	public ChangeSearchFolderWindow() {
		super("ChangeSearchFolder");
	}
	
	public ChangeSearchFolderWindow load() {
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);// if it hides, the blocked variable in Window would block for ever
		setBounds(0, 0, WIDTH, HEIGHT);
		setResizable(false);
		setLayout(null);
		setVisible(false);
		setLocationRelativeTo(null);
		
		table = new JTable(2, TABLE_LEN);
		table.setValueAt("search", 0, 0);
		table.setValueAt("read old", 1, 0);
		table.setShowGrid(true);
		table.setColumnSelectionAllowed(true);
		table.setRowHeight(15);
		table.setBounds(90, 10, 300, 30);
		add(table);
		
		for (int i = 1; i < TABLE_LEN; i ++ ) {
			for (int ii = 0; ii < 2; ii ++ ) {
				table.setValueAt("", ii, i);
			}
		}
		
		finish = new JButton();
		finish.setIcon(new ImageIcon("./icons/changeFinish.png"));
		finish.addActionListener(a -> {
			setVisible(false);
			run.run();
		});
		finish.setBounds(50, 10, 30, 30);
		add(finish);
		exit = new JButton();
		exit.setIcon(new ImageIcon("./icons/back.png"));
		exit.addActionListener(a -> {
			setVisible(false);
			kill.run();
		});
		exit.setBounds(10, 10, 30, 30);
		add(exit);
		
		return this;
	}
	
	public void initforce(Runnable run) {
		this.run = run;
		
		exit.setEnabled(false);
		
		setVisible(true);
		toFront();
		
		repaint();
	}
	
	public void init(Runnable run, Runnable back) {
		this.run = run;
		this.kill = back;
		
		exit.setEnabled(true);
		
		setVisible(true);
		toFront();
		
		repaint();
	}
	
	public List <String> getSearch() {
		List <String> erg = new ArrayList <String>();
		String zw;
		for (int i = 1; i < TABLE_LEN; i ++ ) {
			zw = table.getValueAt(0, i).toString();
			if ( !zw.trim().isEmpty()) {
				erg.add(zw);
			}
		}
		return erg;
	}
	
	public List <String> getRead() {
		List <String> erg = new ArrayList <String>();
		String zw;
		for (int i = 1; i < TABLE_LEN; i ++ ) {
			zw = table.getValueAt(1, i).toString();
			if ( !zw.trim().isEmpty()) {
				erg.add(zw);
			}
		}
		return erg;
	}
	
}
