package de.hechler.filedupedetector.gui;

import de.hechler.filedupedetector.GuiInterface;
import de.hechler.filedupedetector.ScanStore;
import de.hechler.filedupedetector.gui.objects.Window;

public class GuiMain {
	
	public static GuiInterface root = new ScanStore("./src");
	
	public static void main(String[] args) {
		new Window().load();
	}
	
}
