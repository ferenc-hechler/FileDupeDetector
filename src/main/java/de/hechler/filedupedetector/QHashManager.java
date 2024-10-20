package de.hechler.filedupedetector;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class QHashManager {

	public static class DupeInfo {
		String hash;
		long filesize;
		int numDupes;
		Folder dupeRootFolder;
		public DupeInfo(String hash, long filesize, Folder dupeRootFolder, int numDupes) {
			this.hash = hash;
			this.filesize = filesize;
			this.numDupes = numDupes;
			this.dupeRootFolder = dupeRootFolder;
		}
	}
	public Map<String, DupeInfo> hash2dupeInfoMap;
	public Map<String, FileInfo> hash2selectedFile;

	private static QHashManager instance;
	public static QHashManager getInstance() {
		if (instance == null) {
			instance = new QHashManager();
		}
		return instance;
	}
	private QHashManager() {
		hash2dupeInfoMap = new HashMap<>();
		hash2selectedFile = new HashMap<>();
	}


	public void collectHashDupes(ScanStore store) {
		hash2dupeInfoMap = new HashMap<>();
		hash2selectedFile = new HashMap<>();
		final Set<String> hashValues = new HashSet<>(); 
		final Set<String> duplicateHashValues = new HashSet<>();
		store.visitFiles((file) -> {
			if (!hashValues.add(file.getqHash())) {
				duplicateHashValues.add(file.getqHash());
			}
		});
		store.visitFiles((file) -> {
			Folder folder = ((Folder) file.getParent());
			if (duplicateHashValues.contains(file.getqHash())) {
				DupeInfo di = hash2dupeInfoMap.get(file.getqHash());
				if (di != null) {
					di.dupeRootFolder = folder.getCommonParentFolder(di.dupeRootFolder);
					di.numDupes += 1;
				}
				else {
					di = new DupeInfo(file.getqHash(), file.getFilesize(), folder, 1);
					hash2dupeInfoMap.put(file.getqHash(), di);
				}
			}
		});
	}

	public boolean isDupe(String qHash) {
		return hash2dupeInfoMap.containsKey(qHash);
	}
	
	public int getCountDupes(String qHash) {
		DupeInfo dupeInfo = hash2dupeInfoMap.get(qHash);
		if (dupeInfo == null) {
			return 0;
		}
		return dupeInfo.numDupes-1; 
	}
	
	public void unselectFileForHash(FileInfo fi) {
		hash2selectedFile.remove(fi.getqHash(), fi);
	}
	public void selectFileForHash(FileInfo fi) {
		hash2selectedFile.put(fi.getqHash(), fi);
	}
	public void selectFileForHashIfUnselected(FileInfo fi) {
		if (!hash2selectedFile.containsKey(fi.getqHash())) {
			hash2selectedFile.put(fi.getqHash(), fi);
		}
	}
	public FileInfo getSelectFileForHash(String qHash) {
		return hash2selectedFile.get(qHash);
	}
	public boolean isSelectFileForHash(FileInfo fi) {
		return hash2selectedFile.get(fi.getqHash()) == fi;
	}
	public boolean isHiddenByOtherFileForHash(FileInfo fi) {
		FileInfo other = getSelectFileForHash(fi.getqHash());
		if (other == null) {
			return false;
		}
		return other != fi;
	}

	public void showStats() {
		long sumDifferentFiles = 0;
		long sumDuplicateFiles = 0;
		long sumDifferentMem = 0;
		long sumDuplicatesMem = 0;
		for (DupeInfo di:hash2dupeInfoMap.values()) {
			sumDifferentFiles += 1;
			sumDuplicateFiles += (di.numDupes-1);
			sumDifferentMem += di.filesize;
			sumDuplicatesMem += (di.filesize*(di.numDupes-1));
		}
		System.out.println("sum different files: "+sumDifferentFiles);
		System.out.println("sum duplicate files: "+sumDuplicateFiles);
		System.out.println("sum different memory: "+Utils.readableBytes(sumDifferentMem));
		System.out.println("sum duplicates memory: "+Utils.readableBytes(sumDuplicatesMem));
	}
	public void clear() {
		instance = null;
	}
	
}
