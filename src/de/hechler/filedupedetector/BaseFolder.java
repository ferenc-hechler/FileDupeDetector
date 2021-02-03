package de.hechler.filedupedetector;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Path;

public class BaseFolder extends Folder {

	private Volume volume;

	public BaseFolder(String baseFolder) {
		super(null, null);
		try {
			File currentFolder = new File(baseFolder);
			String path = currentFolder.getCanonicalPath();
			if (path.endsWith(File.separator) && (path.length()>3)) {
				path = path.substring(0, path.length()-File.separator.length());
			}
			volume = new Volume(path);
			if (!path.startsWith(volume.getLetter()+":")) {
				throw new RuntimeException("could not find volume letter '"+volume.getLetter()+"' in basepath '"+path+"'");
			}
			path = path.substring(2);
			foldername = path;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	public BaseFolder(Volume vol, String baseFolderPath) {
		super(null, null);
		this.volume = vol;
		this.foldername = baseFolderPath;
	}
	
	public Path getPath() {
		return volume.getPath().resolve(foldername);
	}

	public void write(PrintStream out) {
		volume.write(out);
		super.write(out);
	}
	
	@Override
	protected boolean isBaseFolder() {
		return true;
	}

	public static BaseFolder read(BufferedReader in) {
		try {
			Volume vol = Volume.read(in);
			String baseFolderPath = in.readLine();
			if (!baseFolderPath.startsWith("FOLDER ")) {
				throw new RuntimeException("missing tag 'FOLDER '");
			}
			baseFolderPath = baseFolderPath.replaceFirst("FOLDER ", "");
			BaseFolder result = new BaseFolder(vol, baseFolderPath);
			result.readFiles(in);
			String line = in.readLine();
			while (!line.isEmpty()) {
				if (!line.startsWith("FOLDER ")) {
					throw new RuntimeException("missing tag 'FOLDER '");
				}
				String childFolderName = line.replaceFirst("FOLDER ", "");
				if (!baseFolderPath.equals("\\")) {
					if (!childFolderName.startsWith(baseFolderPath+"\\")) {
						throw new RuntimeException("unexpected child folder '"+childFolderName+"' of '"+baseFolderPath+"'");
					}
					childFolderName = childFolderName.substring(baseFolderPath.length()+1);
				}
				Folder childFolder = new Folder(result, childFolderName);
				childFolder.readFiles(in);
				result.childFolders.add(childFolder);
				line = in.readLine();
			}

			return result;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}


}
