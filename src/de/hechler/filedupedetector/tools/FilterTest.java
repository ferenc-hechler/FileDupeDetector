package de.hechler.filedupedetector.tools;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hechler.filedupedetector.GuiInterface;
import de.hechler.filedupedetector.ScanStore;
import de.hechler.filedupedetector.SumInfo;
import de.hechler.filedupedetector.Utils;

public class FilterTest {
	
	public static void main(String[] args) {
		ScanStore store = new ScanStore();
//		store.scanFolder("./out/testdir");
//		store.write("./out/store.out");
//		store.read("./out/SG-BKpl-10TB-abc.out");
		store.read("./out/scans/SG-BKpl-10TB-b.out");
		store.filterCategories("d");
		showTextGui(store);
	}

	private static void showTextGui(GuiInterface root) {
		showRecursive("", root);
	}

	private static void showRecursive(String indent, GuiInterface element) {
		if (element.isFile()) {
			System.out.println(indent+"FILE "+element.getName()+" "+Utils.readableBytes(element.getSumInfo().getTotalMemory())+(element.getSumInfo().getDuplicateMemory()!=0?" DUP":""));
			return;
		}
		SumInfo info = element.getSumInfo();
		System.out.println(indent+"FOLDER "+element.getName()+" "+Utils.readableBytes(info.getTotalMemory())+" "+Utils.readableBytes(info.getDuplicateMemory())+" (files:"+info.getNumFiles()+")");
		for (GuiInterface file:element.getChildFiles()) {
			showRecursive(indent+"  ", file);
		}
		for (GuiInterface folder:element.getChildFolders()) {
			showRecursive(indent+"  ", folder);
		}
	}
	
}
