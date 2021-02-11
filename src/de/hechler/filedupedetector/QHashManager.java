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
	}

	public boolean isDupe(String qHash) {
		return hash2dupeInfoMap.containsKey(qHash);
	}
	
}
