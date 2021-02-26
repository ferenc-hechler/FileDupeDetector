package de.hechler.filedupedetector.tools;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ReadExtensionCategories {

	public static void main(String[] args) throws IOException {
		String filename = "analysis/extensions-categories2.csv";
		BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(filename), "utf-8"));
		String line = in.readLine(); // skip header
		
		Map<String, List<String>> categories = new HashMap<>();
		
		line = in.readLine();
		while (line != null) {
			String[] ext_count_category = line.split(";");
			String ext = ext_count_category[0];
			String cat = ext_count_category[2];
			System.out.println("."+ext+" --> "+cat);
			List<String> exts = categories.get(cat);
			if (exts == null) {
				exts = new ArrayList<>();
				categories.put(cat, exts);
			}
			exts.add(ext);
			line = in.readLine();
		}
		in.close();
		
		System.out.println(categories);
		for (String cat:categories.keySet()) {
			Set<String> handledExts = new HashSet<>();
			System.out.print("+ \""+cat+"=.");
			List<String> exts = categories.get(cat);
			for (String ext:exts) {
				ext = ext.toLowerCase();
				if (handledExts.contains(ext)) {
					continue;
				}
				handledExts.add(ext);
				System.out.print(ext+".\"");
			}
			System.out.println();
		}
	}
	
}
