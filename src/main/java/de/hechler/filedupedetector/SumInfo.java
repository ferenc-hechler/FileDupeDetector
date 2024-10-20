package de.hechler.filedupedetector;

public class SumInfo {

	private long numFiles;
	private long numSelectedFiles;
	private long numHiddenFiles;
	private long numDuplicateFiles;
	private long numFolders;
	private long totalMemory;
	private long selecedMemory;
	private long hiddenMemory;
	private long duplicateMemory;
	private long duplicateRatioMemory;
	private long lastModified;
	
	public SumInfo() {
		this(0,0,0,0,0,0,0,0,0,0,0);
	}

	public SumInfo(
			long numFiles, long numSelectedFiles, long numHiddenFiles, long numDuplicateFiles,
			long numFolders,
			long totalMemory, long selecedMemory, long hiddenMemory, long duplicateMemory, long duplicateRatioMemory,
			long lastModified) {
		this.numFiles = numFiles;
		this.numSelectedFiles = numSelectedFiles;
		this.numHiddenFiles = numHiddenFiles;
		this.numDuplicateFiles += numDuplicateFiles;
		this.numFolders = numFolders;
		this.totalMemory = totalMemory;
		this.selecedMemory = selecedMemory;
		this.hiddenMemory = hiddenMemory;
		this.duplicateMemory = duplicateMemory;
		this.duplicateRatioMemory = duplicateRatioMemory;
		this.lastModified = lastModified;
	}
	
	public void add(SumInfo other) {
		this.numFiles += other.numFiles;
		this.numSelectedFiles += other.numSelectedFiles;
		this.numHiddenFiles += other.numHiddenFiles;
		this.numDuplicateFiles += other.numDuplicateFiles;
		this.numFolders += other.numFolders;
		this.totalMemory += other.totalMemory;
		this.selecedMemory += other.selecedMemory;  
		this.hiddenMemory += other.hiddenMemory;  
		this.duplicateMemory += other.duplicateMemory;  
		this.duplicateRatioMemory += other.duplicateRatioMemory;  
		if (lastModified < other.lastModified) {
			lastModified = other.lastModified;
		}
	}
	
	public void sub(SumInfo other) {
		this.numFiles -= other.numFiles;
		this.numSelectedFiles -= other.numSelectedFiles;
		this.numHiddenFiles -= other.numHiddenFiles;
		this.numDuplicateFiles -= other.numDuplicateFiles;
		this.numFolders -= other.numFolders;
		this.totalMemory -= other.totalMemory;
		this.selecedMemory -= other.selecedMemory;  
		this.hiddenMemory -= other.hiddenMemory;  
		this.duplicateMemory -= other.duplicateMemory;  
		this.duplicateRatioMemory -= other.duplicateRatioMemory;
		// lastModified can not be changed
	}
	
	
	
	public long getNumFiles() {
		return numFiles;
	}
	public long getNumSelectedFiles() {
		return numSelectedFiles;
	}
	public long getNumHiddenFiles() {
		return numHiddenFiles;
	}
	public long getNumDuplicateFiles() {
		return numDuplicateFiles;
	}
	public long getNumFolders() {
		return numFolders;
	}
	public long getTotalMemory() {
		return totalMemory;
	}
	public long getSelecedMemory() {
		return selecedMemory;
	}
	public long getHiddenMemory() {
		return hiddenMemory;
	}
	public long getDuplicateMemory() {
		return duplicateMemory;
	}
	public long getDuplicateRatioMemory() {
		return duplicateRatioMemory;
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

}
