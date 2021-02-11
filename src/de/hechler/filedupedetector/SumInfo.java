package de.hechler.filedupedetector;

public class SumInfo {

	private long numFiles;
	private long numFolders;
	private long totalMemory;
	private long duplicateMemory;
	
	public SumInfo() {
		this(0,0,0,0);
	}

	public SumInfo(long numFiles, long numFolders, long totalMemory, long duplicateMemory) {
		this.numFiles = numFiles;
		this.numFolders = numFolders;
		this.totalMemory = totalMemory;
		this.duplicateMemory = duplicateMemory;
	}
	
	public void add(SumInfo other) {
		this.numFiles += other.numFiles;
		this.numFolders += other.numFolders;
		this.totalMemory += other.totalMemory;
		// TODO: das stimmt so noch nicht. Die Duplikate addieren sich nur, wenn sie nicht identisch sind.
		this.duplicateMemory += other.duplicateMemory;  
	}
	
	public long getNumFiles() {
		return numFiles;
	}
	public long getNumFolders() {
		return numFolders;
	}
	public long getTotalMemory() {
		return totalMemory;
	}
	public long getDuplicateMemory() {
		return duplicateMemory;
	}

	public void addNumFolders(int add) {
		numFolders += add;
	}
	
}
