package de.hechler.filedupedetector;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class ScanStore {

	private static final String OUTPUT_ENCODING = "UTF-8";
	private List<BaseFolder> baseFolders;

	public ScanStore() {
		this.baseFolders = new ArrayList<>();
	}

	public BaseFolder scanFolder(String scanFolder) {
		BaseFolder base = new BaseFolder(scanFolder);
		base.readRecursiveFolderContent();
		baseFolders.add(base);
		return base;
		
	}

	public void visitFiles(BiConsumer<Folder, FileInfo> visitor) {
		for (BaseFolder bf:baseFolders) {
			bf.visitFiles(visitor);
		}
	}

	public void visitFolders(Consumer<Folder> visitor) {
		for (BaseFolder bf:baseFolders) {
			bf.visitFolders(visitor);
		}
	}

	public void write() {
		write(System.out);
	}
	
	public void write(String filename) {
		if (filename == null) {
			write(System.out);
			return;
		}
		try (PrintStream out = new PrintStream(filename, OUTPUT_ENCODING)) {
			write(out);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public void write(PrintStream out) {
		for (BaseFolder baseFolder:baseFolders) {
			out.println("----- BASEFOLDER "+baseFolder.getPath()+" -----");
			baseFolder.write(out);
			out.println();
		}
	}
	
	public void read(String filename) {
		
		try (BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(filename), OUTPUT_ENCODING))) {
			String line = in.readLine();
			while (line != null) {
				if (!line.isEmpty()) {
					if (!line.trim().startsWith("----- BASEFOLDER ")) {
						throw new RuntimeException("missing intro '----- BASEFOLDER '");
					}
					BaseFolder baseFolder = BaseFolder.read(in);
					baseFolders.add(baseFolder);
				}
				line = in.readLine();
			}
			
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	
}
