package de.hechler.filedupedetector;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;

import de.hechler.filedupedetector.tools.ExtensionInfo;

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

	public FileInfo searchFirstFile(Predicate<FileInfo> check) {
		for (BaseFolder bf:baseFolders) {
			FileInfo f = bf.searchFirstFile(check);
			if (f != null) {
				return f;
			}
		}
		return null;
	}

	public void visitFiles(Consumer<FileInfo> visitor) {
		for (BaseFolder bf:baseFolders) {
			bf.visitFiles(visitor);
		}
	}

	public void visitAllFiles(Consumer<FileInfo> visitor) {
		for (BaseFolder bf:baseFolders) {
			bf.visitAllFiles(visitor);
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
		QHashManager.getInstance().clear();
		QHashManager.getInstance().collectHashDupes(this);
		QHashManager.getInstance().showStats();
		sumInfo = new SumInfo();
		for (BaseFolder bf:baseFolders) {
			sumInfo.add(bf.calcSumInfoFromChildren());
		}
		return sumInfo;
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

	public void filterCategories(String filteredCategoriesAsString) {
		List<String> categories = new ArrayList<>();
		for (int i=0; i<filteredCategoriesAsString.length(); i++) {
			categories.add(filteredCategoriesAsString.substring(i, i+1));
		}
		final Set<String> filteredExtensions = ExtensionInfo.getInstance().getExts(categories);
		filter(fileInfo -> filteredExtensions.contains(fileInfo.getExtension()));
	}
	
	public void filter(Predicate<FileInfo> check) {
		System.out.println("filtering");
		visitAllFiles(file -> file.setDeactivated(!check.test(file)));
		System.out.println("calculating");
		calcSumInfoFromChildren();
		visitFolders(folder -> folder.setDeactivated(folder.getSumInfo().getNumFiles() == 0));
		System.out.println("files " + sumInfo.getNumFiles()+" - netto "+Utils.readableBytes(sumInfo.getTotalMemory()-sumInfo.getDuplicateMemory()) + " - total "+Utils.readableBytes(sumInfo.getTotalMemory()));
	}
	
	@Override public boolean isFolder() { return true; }
	@Override public boolean isFile() { return false; }
	@Override public boolean isVolume() { return false; }
	@Override public boolean isRoot() { return true; }
	
	@Override public long getVolumeSize() {
		throw new UnsupportedOperationException("Root (ScanStore) can not be queried for volumeSize");
	}
	
	@Override public String getName() {
		return "Dieser PC";
	}

	@Override public void refreshSumInfo() {
		calcSumInfoFromChildren();
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override public List<GuiInterface> getChildFolders() {
		return (List) getBaseFolders();
	}

	@Override public List<GuiInterface> getChildFiles() {
		return Collections.emptyList();
	}

	@Override public GuiInterface getParent() {
		return null;
	}

	@Override public Path getPath() {
		return null;
	}

	@Override public void delete() {
		throw new UnsupportedOperationException("Root (ScanStore) can not be deleted.");
	}

}
