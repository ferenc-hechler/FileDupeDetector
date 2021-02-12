package de.hechler.filedupedetector.gui.objects;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JTextField;


public class ErrorWindow extends JFrame {
	
	/** UID */
	private static final long serialVersionUID = -7946535256947486659L;
	
	private Boolean state;
	
	public ErrorWindow() {
	}
	
	public ErrorWindow load(Exception e) {
		return load(e, "ERROR");
	}
	
	public ErrorWindow load(Exception e, String name) {
		state = false;
		setName(name);
		setResizable(true);
		setVisible(true);
		toFront();
		setBounds(0, 0, 200, 120);
		JTextField text = new JTextField();
		text.setBounds(70, 10, 15, 100);
		text.setText(e.getMessage());
		JButton button = new JButton(new ImageIcon("./icons/errorExpand.png"));
		button.addActionListener(a -> {
			synchronized (state) {
				if (state) {
					setBounds(0, 0, 200, 120);
					text.setBounds(70, 10, 15, 100);
					text.setText(e.getMessage());
				} else {
					setBounds(0, 0, 500, 750);
					text.setBounds(70, 10, 500, 100);
					StringBuilder str = new StringBuilder(e.getMessage()).append('\n');
					for (StackTraceElement add : e.getStackTrace()) {
						str.append(add);
					}
					text.setText(str.toString());
				}
				state = !state;
			}
		});
		add(text);
		add(button);
		return this;
	}
	
}
