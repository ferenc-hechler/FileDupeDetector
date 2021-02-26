package de.hechler.filedupedetector;

import static de.hechler.filedupedetector.Const.COLUMN_SEPERATOR;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.List;

public class FileInfo implements GuiInterface {

	private final static int HASH_BLOCK_SIZE = 8192;

	private final static byte[] buf;
	private final static MessageDigest digest;

	private static SumProgressBar progress;
	
	static {
		try {
			buf = new byte[6 * HASH_BLOCK_SIZE];
			digest = MessageDigest.getInstance("SHA-256");
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
			
		progress = new SumProgressBar();
		progress.setDisplayDelayMillis(50*1000);
		progress.setCntStepsBeforeTimeCheck(500);
	}

	private Folder parent;
	private String filename;
	private long filesize;
	private long lastModified;
	private String qHash;
	
	private boolean deactivated;
	

	public FileInfo(Folder parent, Path file) {
		try {
			this.parent = parent;
			this.filename = file.getFileName().toString();
			lastModified = Files.getLastModifiedTime(file).toMillis();
			filesize = Files.size(file);
			qHash = "-";
			deactivated = false;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public FileInfo(Folder parent, String filename, long filesize, long lastModified, String qHash) {
		this.parent = parent;
		this.filename = filename;
		this.filesize = filesize;
		this.lastModified = lastModified;
		this.qHash = qHash;
		this.deactivated = false;
	}

	public String calcQHash(Path folder) {
		if (progress.nextStep(filesize)) {
			System.out.println("Files: " + progress.getPercentage()+" current: "+folder+"\\"+filename+" ("+Utils.readableBytes(progress.getSum())+")");
		}
		if (!"-".equals(qHash)) {
			return qHash;
		}
		if (!DupeDetector.CALC_HASH) {
			return "-";
		}
		if (filesize <= 6 * HASH_BLOCK_SIZE) {
			qHash = calcFullHash(folder);
		} else {
			qHash = calcQuickHash(folder);
		}
		return qHash;
	}


	private String calcFullHash(Path folder) {
		try {
			digest.reset();
			Path file = folder.resolve(filename);
			try (FileInputStream in = new FileInputStream(file.toFile())) {
				updateHash(in, (int) filesize);
				byte[] hash = digest.digest();
				return Utils.bytesToHex(hash);
			}
		} catch (Exception e) {
			System.err.println("ERROR " + e.toString());
			return "ERROR " + e.toString().replace(Const.COLUMN_SEPERATOR, " ");
		}
	}

	private String calcQuickHash(Path folder) {
		try {
			digest.reset();
			Path file = folder.resolve(filename);
			try (RandomAccessFile in = new RandomAccessFile(file.toFile(), "r")) {
				updateHash(in, HASH_BLOCK_SIZE);
				in.seek((filesize - HASH_BLOCK_SIZE) / 2);
				updateHash(in, HASH_BLOCK_SIZE);
				in.seek(filesize - HASH_BLOCK_SIZE);
				updateHash(in, HASH_BLOCK_SIZE);
				byte[] hash = digest.digest();
				return Utils.bytesToHex(hash);
			}
		} catch (IOException e) {
			System.err.println("ERROR " + e.toString());
			return "ERROR " + e.toString().replace(Const.COLUMN_SEPERATOR, " ");
		}
	}

	private void updateHash(InputStream in, int len) {
		try {
			int missing = len;
			int cnt = in.read(buf, 0, missing);
			while (cnt > 0) {
				digest.update(buf, 0, cnt);
				missing -= cnt;
				if (missing == 0) {
					break;
				}
				cnt = in.read(buf, 0, missing);
			}
			if (missing > 0) {
				throw new RuntimeException("error calcing hash for " + filename);
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private void updateHash(RandomAccessFile in, int len) {
		try {
			int missing = len;
			int cnt = in.read(buf, 0, missing);
			while (cnt > 0) {
				digest.update(buf, 0, cnt);
				missing -= cnt;
				if (missing == 0) {
					break;
				}
				cnt = in.read(buf, 0, missing);
			}
			if (missing > 0) {
				throw new RuntimeException("error calcing hash for " + filename);
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public String getFilename() {
		return filename;
	}

	public long getFilesize() {
		return filesize;
	}

	public long getLastModified() {
		return lastModified;
	}

	public String getqHash() {
		return qHash;
	}

	@Override
	public Path getPath() {
		return parent.getPath().resolve(filename);
	}

	/**
	 * NOT THREADSAFE!
	 */
	private String lastModifiedDate() {
		return Utils.date2string(lastModified);
	}


	/**
	 * NOT THREADSAFE!
	 */
	public void write(PrintStream out) {
		out.println(qHash + COLUMN_SEPERATOR + filename + COLUMN_SEPERATOR + filesize + COLUMN_SEPERATOR + lastModifiedDate());
	}

	/**
	 * NOT THREADSAFE!
	 */
	public static FileInfo read(Folder folder, String line) {
		String[] qhash_filename_filesize_lmdate = line.split("["+COLUMN_SEPERATOR+"]");
		String qHash = qhash_filename_filesize_lmdate[0];
		String filename = qhash_filename_filesize_lmdate[1];
		long filesize = Long.parseLong(qhash_filename_filesize_lmdate[2]);
		long lmDate = Utils.string2date(qhash_filename_filesize_lmdate[3]);
		return new FileInfo(folder, filename, filesize, lmDate, qHash);
	}

	@Override
	public String toString() {
		return filename;
	}

	@Override public boolean isFolder() { return false; }
	@Override public boolean isFile() { return true; }
	@Override public boolean isVolume() { return false; }
	@Override public boolean isRoot() { return false; }	

	@Override public String getName() {
		return filename;
	}

	@Override public void refreshSumInfo() {
		// nothing to do.
	}

	@Override public SumInfo getSumInfo() {
		long dupesize = 0;
		if (QHashManager.getInstance().isDupe(qHash)) {
			dupesize = filesize;
		}
		return new SumInfo(1, 0, filesize, dupesize, lastModified);
	}

	@Override public List<GuiInterface> getChildFolders() {
		return Collections.emptyList();
	}

	@Override public List<GuiInterface> getChildFiles() {
		return Collections.emptyList();
	}

	@Override public long getVolumeSize() {
		throw new UnsupportedOperationException("FileInfo ("+toString()+") can not be queried for volumeSize");
	}

	@Override public GuiInterface getParent() {
		return parent;
	}

	@Override public void delete() {
		parent.removeChild(this);
	}

	public void setDeactivated(boolean newValue) {
		this.deactivated = newValue;
	}

	public boolean isDeactivated() {
		return deactivated;
	}

	public String getExtension() {
		int pos = filename.lastIndexOf('.');
		if (pos == -1) {
			return "";
		}
		return filename.substring(pos+1);
	}
	
}
