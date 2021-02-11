package de.hechler.filedupedetector;

public class DummyGui {
	
	public static void main(String[] args) {
		ScanStore store = new ScanStore();
		store.scanFolder("./src");
		// store.write("./out/store.out");
		// store.read("./out/store.out");
		showTextGui(store);
	}

	private static void showTextGui(GuiInterface root) {
		showRecursive("", root);
	}

	private static void showRecursive(String indent, GuiInterface element) {
		if (element.isFile()) {
			System.out.println(indent+"FILE "+element.getName()+" "+Utils.readableBytes(element.getSumInfo().getTotalMemory()));
			return;
		}
		SumInfo info = element.getSumInfo();
		System.out.println(indent+"FOLDER "+element.getName()+" "+Utils.readableBytes(info.getTotalMemory())+" (files:"+info.getNumFiles()+")");
		for (GuiInterface file:element.getChildFiles()) {
			showRecursive(indent+"  ", file);
		}
		for (GuiInterface folder:element.getChildFolders()) {
			showRecursive(indent+"  ", folder);
		}
	}
	
}
