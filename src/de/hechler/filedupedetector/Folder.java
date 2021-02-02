package de.hechler.filedupedetector;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class Folder implements FolderIF {

	protected String foldername;
	private FolderIF parent;
	private List<FileInfo> files;
	protected List<Folder> childFolders;
	
	public Folder(FolderIF parent, String foldername) {
		this.parent = parent;
		this.foldername = foldername;
		files = null;
		childFolders = null;
	}

	public void readFolderContent() {
		files = new ArrayList<>();
		childFolders = new ArrayList<>();
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
			System.err.println("error reading folder "+getPath()+": "+e.toString());
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
	
	@Override
	public Path getPath() {
		return parent.getPath().resolve(foldername);
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
			childFolders = new ArrayList<>(); 
			files = new ArrayList<>();
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

}
