package de.hechler.filedupedetector;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class Folder implements GuiInterface {

	protected String foldername;
	private Folder parent;
	private List<FileInfo> files;
	protected List<Folder> childFolders;
	private SumInfo sumInfo;
	
	public Folder(Folder parent, String foldername) {
		this.parent = parent;
		this.foldername = foldername;
		this.files = new ArrayList<>();
		this.childFolders = new ArrayList<>();
		this.sumInfo = null;
	}

	public void readFolderContent() {
		try (DirectoryStream<Path> ds = Files.newDirectoryStream(getPath())) {
			for (Path child:ds) {
				if (Files.isDirectory(child)) {
					childFolders.add(new Folder(this, child.getFileName().toString()));
				}
				else {
					try {
						files.add(new FileInfo(child));
					}
					catch (Exception e) {
						System.err.println("error reading file "+child+": "+e.toString());
					}
				}
			}
		} catch (Exception e) {
			System.err.println("warning: skipping folder "+getPath()+": "+e.toString());
		}
	}
	
	public void readRecursiveFolderContent() {
		readFolderContent();
		Path path = getPath();
		for (FileInfo file:files) {
			file.calcQHash(path);
		}
		for (Folder folder:childFolders) {
			folder.readRecursiveFolderContent();
		}
	}
	
	public Path getPath() {
		return parent.getPath().resolve(foldername);
	}

	public Folder getParent() {
		return parent;
	}

	public void write(PrintStream out) {
		if (isBaseFolder() || (files.size() > 0)) {
			out.println("FOLDER "+getPath().toString().substring(2));
			for (FileInfo file:files) {
				file.write(out);
			}
			out.println();
		}
		for (Folder childFolder:childFolders) {
			childFolder.write(out);
		}
	}
	
	public void readFiles(BufferedReader in) {
		try {
			String line = in.readLine();
			while (!line.isEmpty()) {
				FileInfo file = FileInfo.read(line);
				files.add(file);
				line = in.readLine();
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	protected boolean isBaseFolder() {
		return false;
	}

	public void visitFiles(BiConsumer<Folder, FileInfo> visitor) {
		for (FileInfo file:files) {
			visitor.accept(this, file);
		}
		for (Folder childFolder:childFolders) {
			childFolder.visitFiles(visitor);
		}
	}

	public void visitFolders(Consumer<Folder> visitor) {
		visitor.accept(this);
		for (Folder childFolder:childFolders) {
			childFolder.visitFolders(visitor);
		}
	}

	public Folder findChildFolder(String foldername) {
		// optimization: use Map for folders with many childfolders.
		Optional<Folder> result = childFolders.stream().filter(f -> f.foldername.equals(foldername)).findAny();
		if (result.isPresent()) {
			return result.get();
		}
		return null;
	}
	

	public SumInfo calcSumInfoFromChildren() {
		sumInfo = new SumInfo();
		for (Folder childFolder:childFolders) {
			sumInfo.add(childFolder.calcSumInfoFromChildren());
		}
		for (FileInfo file:files) {
			sumInfo.add(file.getSumInfo());
		}
		sumInfo.addNumFolders(1);
		sumInfo.reduceDuplicates(QHashManager.getInstance().getDuplicationReduction(this));
		return sumInfo;
	}

	@Override
	public String toString() {
		return foldername;
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
		return foldername;
	}

	@Override
	public void refreshSumInfo() {
		calcSumInfoFromChildren();
	}

	@Override
	public SumInfo getSumInfo() {
		return sumInfo;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public List<GuiInterface> getChildFolders() {
		return (List)childFolders;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public List<GuiInterface> getChildFiles() {
		return (List)files;
	}

	public Folder getCommonParentFolder(Folder otherFolder) {
		if (otherFolder == null) {
			return null;
		}
		if (otherFolder == this) {
			return this;
		}
		Set<Folder> ownParents = new HashSet<>();
		Folder searchOwn = getParent();
		while (searchOwn != null) {
			if (searchOwn == otherFolder) {
				return otherFolder;
			}
			ownParents.add(searchOwn);
			searchOwn = searchOwn.getParent();
		}
		Folder searchOther = otherFolder.getParent();
		while (searchOther != null) {
			if (ownParents.contains(searchOther)) {
				return searchOther;
			}
			searchOther = searchOther.getParent();
		}
		return null;
	}

}
