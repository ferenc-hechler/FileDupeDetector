package de.hechler.filedupedetector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class QHashManager {

	public static class DupeInfo {
		String hash;
		long filesize;
		List<FileInfo> dupeFiles;
		FileInfo selectedFile;
		public DupeInfo(String hash, long filesize, FileInfo fi) {
			this.hash = hash;
			this.filesize = filesize;
			this.dupeFiles = new ArrayList<>();
			this.dupeFiles.add(fi);
			this.selectedFile = null;
		}
	}
	public Map<String, DupeInfo> hash2dupeInfoMap;

	private static QHashManager instance;
	public static QHashManager getInstance() {
		if (instance == null) {
			instance = new QHashManager();
		}
		return instance;
	}
	private QHashManager() {
		hash2dupeInfoMap = new HashMap<>();
	}


	public void collectHashDupes(ScanStore store) {
		hash2dupeInfoMap = new HashMap<>();
		final Set<String> hashValues = new HashSet<>(); 
		final Set<String> duplicateHashValues = new HashSet<>();
		store.visitFiles((file) -> {
			if (!hashValues.add(file.getqHash())) {
				duplicateHashValues.add(file.getqHash());
			}
		});
		store.visitFiles((file) -> {
			if (duplicateHashValues.contains(file.getqHash())) {
				DupeInfo di = hash2dupeInfoMap.get(file.getqHash());
				if (di != null) {
					di.dupeFiles.add(file);
				}
				else {
					di = new DupeInfo(file.getqHash(), file.getFilesize(), file);
					hash2dupeInfoMap.put(file.getqHash(), di);
				}
			}
		});
	}

	public boolean isDupe(String qHash) {
		return getCountDupes(qHash)>0;
	}
	
	public int getCountDupes(String qHash) {
		DupeInfo dupeInfo = hash2dupeInfoMap.get(qHash);
		if (dupeInfo == null) {
			return 0;
		}
		return dupeInfo.dupeFiles.size()-1;
	}
	
	public void unselectFileForHash(FileInfo fi) {
		DupeInfo dupeInfo = hash2dupeInfoMap.get(fi.getqHash());
		if (dupeInfo == null) {
			return;
		}
		if (dupeInfo.selectedFile == fi) {
			dupeInfo.selectedFile = null;
		}
	}
	public void selectFileForHash(FileInfo fi) {
		DupeInfo dupeInfo = hash2dupeInfoMap.get(fi.getqHash());
		if (dupeInfo == null) {
			return;
		}
		dupeInfo.selectedFile = fi;
	}
	public void selectFileForHashIfUnselected(FileInfo fi) {
		DupeInfo dupeInfo = hash2dupeInfoMap.get(fi.getqHash());
		if (dupeInfo == null) {
			return;
		}
		if (dupeInfo.selectedFile == null) {
			dupeInfo.selectedFile = fi;
		}
	}
	public FileInfo getSelectFileForHash(String qHash) {
		DupeInfo dupeInfo = hash2dupeInfoMap.get(qHash);
		if (dupeInfo == null) {
			return null;
		}
		return dupeInfo.selectedFile;
	}
	public boolean isSelectFileForHash(FileInfo fi) {
		DupeInfo dupeInfo = hash2dupeInfoMap.get(fi.getqHash());
		if (dupeInfo == null) {
			return false;
		}
		return dupeInfo.selectedFile == fi;
	}
	public boolean isHiddenByOtherFileForHash(FileInfo fi) {
		DupeInfo dupeInfo = hash2dupeInfoMap.get(fi.getqHash());
		if (dupeInfo == null) {
			return false;
		}
		if (dupeInfo.selectedFile == null) {
			return false;
		}
		return dupeInfo.selectedFile != fi;
	}

	public void showStats() {
		long sumDifferentFiles = 0;
		long sumDuplicateFiles = 0;
		long sumDifferentMem = 0;
		long sumDuplicatesMem = 0;
		for (DupeInfo di:hash2dupeInfoMap.values()) {
			sumDifferentFiles += 1;
			sumDuplicateFiles += (di.dupeFiles.size()-1);
			sumDifferentMem += di.filesize;
			sumDuplicatesMem += (di.filesize*(di.dupeFiles.size()-1));
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
