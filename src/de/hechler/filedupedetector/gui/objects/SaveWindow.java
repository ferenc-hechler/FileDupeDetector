package de.hechler.filedupedetector.gui.objects;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JTextField;

public class SaveWindow extends JFrame {
	
	/** UID */
	private static final long serialVersionUID = 6399441728955574649L;
	
	public static final int WIDTH = 160;
	public static final int HEIGHT = 75;
	
	
	public SaveWindow() {
	}
	
	public SaveWindow load(Window window) {
		setLayout(null);
		setBounds(0, 0, WIDTH, HEIGHT);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		
		JTextField text = new JTextField("save folder");
		text.setBounds(35, 10, 100, 15);
		add(text);
		
		JButton button = new JButton();
		button.setIcon(new ImageIcon("./icons/cuniform.png"));
		button.setBounds(10, 10, 15, 15);
		button.addActionListener(a -> {
			String file = text.getText();
			new Thread( () -> {
				window.saveTo(file);
			}).start();
		});
		add(button);
		
		setVisible(true);
		
		return this;
	}
	
}
