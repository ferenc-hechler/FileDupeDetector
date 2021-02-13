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
	public Map<Folder, Long> folder2allDupesCompensationBytes; 

	private static QHashManager instance;
	public static QHashManager getInstance() {
		if (instance == null) {
			instance = new QHashManager();
		}
		return instance;
	}
	private QHashManager() {
		hash2dupeInfoMap = new HashMap<>();
		folder2allDupesCompensationBytes = new HashMap<>();
	}


	public void collectHashDupes(ScanStore store) {
		hash2dupeInfoMap = new HashMap<>();
		folder2allDupesCompensationBytes = new HashMap<>();
		final Set<String> hashValues = new HashSet<>(); 
		final Set<String> duplicateHashValues = new HashSet<>();
		store.visitFiles((folder, file) -> {
			if (!hashValues.add(file.getqHash())) {
				duplicateHashValues.add(file.getqHash());
			}
		});
		store.visitFiles((folder, file) -> {
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
		for (DupeInfo di:hash2dupeInfoMap.values()) {
			Folder f = di.dupeRootFolder;
			folder2allDupesCompensationBytes.put(f, folder2allDupesCompensationBytes.getOrDefault(f, 0L) + di.filesize);
		}
	}

	public boolean isDupe(String qHash) {
		return hash2dupeInfoMap.containsKey(qHash);
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
	public long getDuplicationReduction(Folder folder) {
		return folder2allDupesCompensationBytes.getOrDefault(folder, 0L);
	}
	
}
