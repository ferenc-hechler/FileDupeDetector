package de.hechler.filedupedetector.gui.objects;

import javax.swing.ImageIcon;
import javax.swing.JFrame;

public class Window extends JFrame {
	
	/** UID */
	private static final long serialVersionUID = 6510610980424957351L;
	
	
	
	private static final int WIDTH = 1000;
	private static final int HEIGHT = 1000;
	
	private static final int VOID = 10;
	private static final int X_1 = VOID;
	private static final int X_2 = X_1 + VOID + MenuButton.SIZE;
	private static final int X_3 = X_2 + VOID + MenuButton.SIZE;
	private static final int X_4 = X_2 + VOID + Table.WIDTH;
	private static final int Y_1 = VOID;
	private static final int Y_2 = Y_1 + VOID + MenuButton.SIZE;
	private static final int Y_3 = Y_2 + VOID + MenuButton.SIZE;
	private static final int Y_4 = Y_3 + VOID + ScollButton.HEIGHT;
	
	
	
	
	
	
	
	private Table table;
	private ScollButton up;
	private ScollButton down;
	private MenuButton changeSearchFolder;
	private MenuButton reload;
	private MenuButton deleteSelected;
	private GoInButton[] goIn;
	private GoInButton goOut;
	
	
	
	
	public Window() {
	}
	
	public Window load() {
		setBounds(0, 0, WIDTH, HEIGHT);
		setResizable(false);
		setLayout(null);
		setLocationRelativeTo(null);
		
		changeSearchFolder = new MenuButton().load(X_1, Y_1, new ImageIcon("./icons/changeSearchFolder.png"), (a) -> changeSearchFolder());
		reload = new MenuButton().load(X_2, Y_1, new ImageIcon("./icons/reload.png"), (a) -> reload());
		deleteSelected = new MenuButton().load(X_3, Y_1, new ImageIcon("./icons/delSelect.png"), a -> table.deleteSelected());
		table = new Table().load(X_2, Y_2, null); // TODO add content
		goOut = new GoInButton().load(X_1, Y_2, table);
		up = new ScollButton().load(X_4, Y_2, true, table);
		down = new ScollButton().load(X_4, Y_4, false, table);
		add(down);
		add(up);
		add(table);
		add(deleteSelected);
		add(changeSearchFolder);
		add(reload);
		
		setVisible(true);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		
		repaint();
		
		return this;
	}
	
	private void reload() {
		// TODO Auto-generated method stub
	}

	private void changeSearchFolder() {
		// TODO Auto-generated method stub
	}
	
}
