package de.hechler.filedupedetector;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class ScanStore implements GuiInterface {

	private static final String OUTPUT_ENCODING = "UTF-8";
	private List<BaseFolder> baseFolders;

	private SumInfo sumInfo;
	
	public ScanStore() {
		this.baseFolders = new ArrayList<>();
		this.sumInfo = null;
	}

	public ScanStore(String scanFolder) {
		this.baseFolders = new ArrayList<>();
		this.sumInfo = null;
		scanFolder(scanFolder);
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

	/**
	 * recalculates number of files/folder/memory/duplicates. 
	 */
	public SumInfo calcSumInfoFromChildren() {
		QHashManager.getInstance().collectHashDupes(this);
		sumInfo = new SumInfo();
		for (BaseFolder bf:baseFolders) {
			sumInfo.add(bf.calcSumInfoFromChildren());
		}
		return sumInfo;
	}

	private Folder getParentFolder(Folder folder, Folder dupeRootFolder) {
		return null;
	}

	public List<BaseFolder> getBaseFolders() {
		return baseFolders;
	}

	public SumInfo getSumInfo() {
		if (sumInfo == null) {
			refreshSumInfo();
		}
		return sumInfo;
	}

	@Override
	public boolean isFolder() {
		return true;
	}

	@Override
	public boolean isFile() {
		return false;
	}

	@Override
	public String getName() {
		return "Dieser Computer";
	}

	@Override
	public void refreshSumInfo() {
		calcSumInfoFromChildren();
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public List<GuiInterface> getChildFolders() {
		return (List) getBaseFolders();
	}

	@Override
	public List<GuiInterface> getChildFiles() {
		return Collections.emptyList();
	}
	
}
