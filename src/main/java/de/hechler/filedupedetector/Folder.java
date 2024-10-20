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
import java.util.function.Consumer;
import java.util.function.Predicate;

public class Folder implements GuiInterface {

	protected String foldername;
	private Folder parent;
	private List<FileInfo> files;
	protected List<Folder> childFolders;
	private SumInfo sumInfo;
	
	private boolean deactivated;
	
	
	public Folder(Folder parent, String foldername) {
		this.parent = parent;
		this.foldername = foldername;
		this.files = new ArrayList<>();
		this.childFolders = new ArrayList<>();
		this.sumInfo = null;
		this.deactivated = false;
	}

	public void readFolderContent() {
		try (DirectoryStream<Path> ds = Files.newDirectoryStream(getPath())) {
			for (Path child:ds) {
				if (Files.isDirectory(child)) {
					childFolders.add(new Folder(this, child.getFileName().toString()));
				}
				else {
					try {
						files.add(new FileInfo(this, child));
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
			if (file.isDeactivated()) {
				continue;
			}
			file.calcQHash(path);
		}
		for (Folder folder:childFolders) {
			if (folder.isDeactivated()) {
				continue;
			}
			folder.readRecursiveFolderContent();
		}
	}
	
	@Override public Path getPath() {
		return parent.getPath().resolve(foldername);
	}

	@Override public Folder getParent() {
		return parent;
	}

	public void write(PrintStream out) {
		if (isVolume() || (files.size() > 0)) {
			out.println("FOLDER "+getPath().toString().substring(2));
			for (FileInfo file:files) {
				if (file.isDeactivated()) {
					continue;
				}
				file.write(out);
			}
			out.println();
		}
		for (Folder childFolder:childFolders) {
			if (childFolder.isDeactivated()) {
				continue;
			}
			childFolder.write(out);
		}
	}
	
	public void readFiles(BufferedReader in) {
		try {
			String line = in.readLine();
			while (!line.isEmpty()) {
				FileInfo file = FileInfo.read(this, line);
				files.add(file);
				line = in.readLine();
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	public FileInfo searchFirstFile(Predicate<FileInfo> check) {
		for (FileInfo file:files) {
			if (file.isDeactivated()) {
				continue;
			}
			if (check.test(file)) {
				return file;
			}
		}
		for (Folder childFolder:childFolders) {
			if (childFolder.isDeactivated()) {
				continue;
			}
			FileInfo result = childFolder.searchFirstFile(check);
			if (result != null) {
				return result;
			}
		}
		return null;
	}

	public void visitFiles(Consumer<FileInfo> visitor) {
		for (FileInfo file:files) {
			if (file.isDeactivated()) {
				continue;
			}
			visitor.accept(file);
		}
		for (Folder childFolder:childFolders) {
			if (childFolder.isDeactivated()) {
				continue;
			}
			childFolder.visitFiles(visitor);
		}
	}

	public void visitAllFiles(Consumer<FileInfo> visitor) {
		for (FileInfo file:files) {
			visitor.accept(file);
		}
		for (Folder childFolder:childFolders) {
			childFolder.visitAllFiles(visitor);
		}
	}

	public void visitFolders(Consumer<Folder> visitor) {
		visitor.accept(this);
		for (Folder childFolder:childFolders) {
			if (childFolder.isDeactivated()) {
				continue;
			}
			childFolder.visitFolders(visitor);
		}
	}

	public Folder findChildFolder(String foldername) {
		// optimization: use Map for folders with many childfolders.
		Optional<Folder> result = childFolders.stream().filter(f -> (!f.isDeactivated()) && f.foldername.equals(foldername)).findAny();
		if (result.isPresent()) {
			return result.get();
		}
		return null;
	}
	

	public SumInfo calcSumInfoFromChildren() {
		sumInfo = new SumInfo();
		for (Folder childFolder:childFolders) {
			// do not check for deactivated folders, this is calculated afterwards.
			// folders with 0 files (recursive) are deactivated.
			sumInfo.add(childFolder.calcSumInfoFromChildren());
		}
		for (FileInfo file:files) {
			if (file.isDeactivated()) {
				continue;
			}
			sumInfo.add(file.getSumInfo());
		}
		sumInfo.addNumFolders(1);
		return sumInfo;
	}

	@Override public String toString() {
		return foldername;
	}

	@Override public boolean isFolder() { return true; }
	@Override public boolean isFile() { return false; }
	@Override public boolean isVolume() { return false; }
	@Override public boolean isRoot() { return false; }	
	
	@Override public long getVolumeSize() {
		throw new UnsupportedOperationException("Folder ("+toString()+") can not be queried for volumeSize");
	}

	@Override public String getName() {
		return foldername;
	}

	@Override public void refreshSumInfo() {
		calcSumInfoFromChildren();
	}

	@Override public SumInfo getSumInfo() {
		return sumInfo;
	}

	@Override public List<GuiInterface> getChildFolders() {
		List<GuiInterface> result = new ArrayList<>();
		for (Folder childFolder:childFolders) {
			if (childFolder.isDeactivated()) {
				continue;
			}
			result.add(childFolder);
		}
		return result;
	}

	public void addChild(GuiInterface child) {
		if (child.isFolder()) {
			childFolders.add((Folder) child);
		}
		else {
			files.add((FileInfo) child);
		}
	}

	@Override public List<GuiInterface> getChildFiles() {
		List<GuiInterface> result = new ArrayList<>();
		for (FileInfo file:files) {
			if (file.isDeactivated()) {
				continue;
			}
			result.add(file);
		}
		return result;
	}

	@Override public void delete() {
		// TODO: implement for folder.
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

	public void removeChild(FileInfo file) {
		if (files.remove(file)) {
			// TODO: QHashManager anpassen und beruecksichtigem ab welchen Ordner keine Doublette mehr.
			Folder updateFolder = this;
			SumInfo subInfo = file.getSumInfo();
			while (updateFolder != null) {
				if (updateFolder.sumInfo != null) {
					updateFolder.sumInfo.sub(subInfo);
				}
				updateFolder = updateFolder.getParent();
			}
		}
	}

	public void setDeactivated(boolean newValue) {
		this.deactivated = newValue;
	}
	public boolean isDeactivated() {
		return deactivated;
	}

}
