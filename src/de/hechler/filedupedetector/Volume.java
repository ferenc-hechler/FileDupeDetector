package de.hechler.filedupedetector;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.FileStore;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import static de.hechler.filedupedetector.Const.COLUMN_SEPERATOR;


public class Volume {

	private Path root;
	private String volName;
	private String volLetter;
	private String volType;
	private long volSize;
	private String volVSN;

	public Volume(String basePath) {
	     try {
			root = Paths.get(basePath).getRoot();
			volLetter = root.toString().substring(0,1);
			for (FileStore store: FileSystems.getDefault().getFileStores()) {
				String storeString = store.toString();
				if (storeString.indexOf("("+volLetter+":)") != -1) {
					volName = store.name();
					volType = store.type();
					volSize = store.getTotalSpace();
					volVSN = getVSN(store);
				}
			}
		} catch (Exception e) {
			throw new RuntimeException("Volume("+basePath+"): "+e.toString(), e);
		}
	}

	@SuppressWarnings("resource")
	public Volume(String vsn, String type, long size, String name, String letter) {
	     try {
			for (FileStore store: FileSystems.getDefault().getFileStores()) {
				if (vsn.equals(getVSN(store))) {
					if (type.equals(store.type())) {
						if (size == store.getTotalSpace()) {
							volLetter = store.toString().replaceFirst(".*[(]([A-Z])[:][)].*", "$1");
							root = Paths.get(volLetter+":/").getRoot();
							volName = store.name();
							volType = store.type();
							volSize = store.getTotalSpace();
							volVSN = getVSN(store);
							return;
						}
					}
				};
			}
		} catch (Exception e) {
		}
	     this.volLetter = letter;
	     this.volName = name;
	     this.volVSN = vsn;
	     this.volType = type;
	     this.volSize = size;
	     this.root = Paths.get(volLetter+":\\").getRoot();
	}
	

	@SuppressWarnings("resource")
	public Volume(String vsn, String type, long size) {
	     try {
			for (FileStore store: FileSystems.getDefault().getFileStores()) {
				if (vsn.equals(getVSN(store))) {
					if (type.equals(store.type())) {
						if (size == store.getTotalSpace()) {
							volLetter = store.toString().replaceFirst(".*[(]([A-Z])[:][)].*", "$1");
							root = Paths.get(volLetter+":/").getRoot();
							volName = store.name();
							volType = store.type();
							volSize = store.getTotalSpace();
							volVSN = getVSN(store);
							System.out.println(getUnchangeableInfo());
							System.out.println(getChangeableInfo());
						}
						else {
							throw new RuntimeException("size does not match: "+store.getTotalSpace());
						}
					}
					else {
						throw new RuntimeException("type does not match: "+store.type());
					}
				};
			}
		} catch (Exception e) {
			throw new RuntimeException("Volume("+vsn+", "+type+", "+size+"): "+e.toString(), e);
		}
	}
	
	public String getChangeableInfo() {
		return volLetter+COLUMN_SEPERATOR+volName;
	}
	
	public String getUnchangeableInfo() {
		return volVSN+COLUMN_SEPERATOR + volType+COLUMN_SEPERATOR+volSize;
	}

	private String getVSN(FileStore store) {
		try {
			return formatVSN((Integer) store.getAttribute("volume:vsn"));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}


	private static String formatVSN(int vsnInt) {
		String result = Integer.toHexString(vsnInt);
		while (result.length()<8) { result= "0"+result; }
		result= result.substring(0, 4)+"-"+result.substring(4);
		return result;
	}
	
	public void write(PrintStream out) {
		out.println("VOLUME "+getChangeableInfo()+COLUMN_SEPERATOR+getUnchangeableInfo());
	}
	
	public static Volume read(BufferedReader in) {
		try {
			String info = in.readLine();
			if (!info.startsWith("VOLUME ")) {
				throw new RuntimeException("Keyword 'VOLUME' not found");
			}
			String[] letter_name_vsn_type_size = info.replaceFirst("VOLUME ", "").trim().split("["+COLUMN_SEPERATOR+"]");
			String letter = letter_name_vsn_type_size[0];
			String name = letter_name_vsn_type_size[1];
			String vsn = letter_name_vsn_type_size[2];
			String type = letter_name_vsn_type_size[3];
			long size = Long.parseLong(letter_name_vsn_type_size[4]);
			return new Volume(vsn, type, size, name, letter);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public String getLetter() {
		return volLetter;
	}

	public Path getPath() {
		return root;
	}
	
	@Override
	public String toString() {
		return getChangeableInfo();
	}
	
}
