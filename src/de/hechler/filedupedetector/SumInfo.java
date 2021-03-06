package de.hechler.filedupedetector;

public class SumInfo {

	private long numFiles;
	private long numFolders;
	private long totalMemory;
	private long duplicateMemory;
	private long lastModified;
	
	public SumInfo() {
		this(0,0,0,0,0);
	}

	public SumInfo(long numFiles, long numFolders, long totalMemory, long duplicateMemory, long lastModified) {
		this.numFiles = numFiles;
		this.numFolders = numFolders;
		this.totalMemory = totalMemory;
		this.duplicateMemory = duplicateMemory;
		this.lastModified = lastModified;
	}
	
	public void add(SumInfo other) {
		this.numFiles += other.numFiles;
		this.numFolders += other.numFolders;
		this.totalMemory += other.totalMemory;
		this.duplicateMemory += other.duplicateMemory;  
		if (lastModified < other.lastModified) {
			lastModified = other.lastModified;
		}
	}
	
	public void sub(SumInfo other) {
		this.numFiles -= other.numFiles;
		this.numFolders -= other.numFolders;
		this.totalMemory -= other.totalMemory;
		this.duplicateMemory -= other.duplicateMemory;  
		// lastModified can not be changed
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
	
	
	public String getLastModifiedString() {
		if (lastModified == 0) {
			return "---";
		}
		return Utils.date2string(lastModified);
	}

	public void reduceDuplicates(long duplicationReduction) {
		duplicateMemory -= duplicationReduction;
	}

}
