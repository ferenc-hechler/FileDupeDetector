package de.hechler.filedupedetector;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import static de.hechler.filedupedetector.Const.COLUMN_SEPERATOR;

public class FileInfo {

	private final static int HASH_BLOCK_SIZE = 8192;

	private final static byte[] buf;
	private final static MessageDigest digest;

	private static SumProgressBar progress = new SumProgressBar();
	
	static {
		try {
			buf = new byte[6 * HASH_BLOCK_SIZE];
			digest = MessageDigest.getInstance("SHA-256");
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
	}

	private String filename;
	private long filesize;
	private long lastModified;
	private String qHash;

	public FileInfo(Path file) {
		try {
			this.filename = file.getFileName().toString();
			lastModified = Files.getLastModifiedTime(file).toMillis();
			filesize = Files.size(file);
			qHash = "-";
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public FileInfo(String filename, long filesize, long lastModified, String qHash) {
		this.filename = filename;
		this.filesize = filesize;
		this.lastModified = lastModified;
		this.qHash = qHash;
	}

	public String calcQHash(Path folder) {
		if (progress.nextStep(filesize)) {
			System.out.println("Files: " + progress.getPercentage()+" current: "+folder+"\\"+filename+" ("+readableBytes(progress.getSum())+")");
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

	private final static long KILO = 1024L; 
	private final static long MEGA = KILO*KILO; 
	private final static long GIGA = KILO*MEGA; 
	private final static long TERA = KILO*GIGA; 
	private final static double  DIV_KILO = 1.0/KILO;
	private final static double  DIV_MEGA = DIV_KILO/KILO;
	private final static double  DIV_GIGA = DIV_MEGA/KILO;
	private final static double  DIV_TERA = DIV_GIGA/KILO;
	
	private String readableBytes(long sum) {
		if (sum < KILO) {
			return sum+" Bytes";
		}
		if (sum < MEGA) {
			return round1(DIV_KILO*sum)+"KB";
		}
		if (sum < GIGA) {
			return round1(DIV_MEGA*sum)+"MB";
		}
		if (sum < TERA) {
			return round1(DIV_GIGA*sum)+"GB";
		}
		return round1(DIV_TERA*sum)+"TB";
	}

	private String round1(double value) {
		String result = Double.toString(value);
		int dotPos = result.indexOf('.');
		if (dotPos != -1) {
			result = result.substring(0, dotPos+2);
		}
		return result;
	}

	private String calcFullHash(Path folder) {
		try {
			digest.reset();
			Path file = folder.resolve(filename);
			try (FileInputStream in = new FileInputStream(file.toFile())) {
				updateHash(in, (int) filesize);
				byte[] hash = digest.digest();
				return bytesToHex(hash);
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
				return bytesToHex(hash);
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

	private static String bytesToHex(byte[] hash) {
		StringBuilder hexString = new StringBuilder(2 * hash.length);
		for (int i = 0; i < hash.length; i++) {
			String hex = Integer.toHexString(0xff & hash[i]);
			if (hex.length() == 1) {
				hexString.append('0');
			}
			hexString.append(hex);
		}
		return hexString.toString();
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

	public Path getPath(FolderIF folder) {
		return getPath(folder.getPath());
	}

	public Path getPath(Path path) {
		return path.resolve(filename);
	}

	/**
	 * NOT THREADSAFE!
	 */
	private final static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH_mm_ss");

	/**
	 * NOT THREADSAFE!
	 */
	private String lastModifiedDate() {
		return date2string(lastModified);
	}

	/**
	 * NOT THREADSAFE!
	 */
	private static String date2string(long millis) {
		return sdf.format(new Date(millis));
	}

	private static long string2date(String date) {
		try {
			return sdf.parse(date).getTime();
		} catch (ParseException e) {
			throw new RuntimeException(e);
		}
	}

	static long string2datePat(String date) {
		int y = Integer.parseInt(date.substring(0, 4));
		int m = Integer.parseInt(date.substring(5, 7));
		int d = Integer.parseInt(date.substring(8, 10));
		int h = Integer.parseInt(date.substring(11, 13));
		int min = Integer.parseInt(date.substring(14, 16));
		int s = Integer.parseInt(date.substring(17));
		Calendar.Builder build = new Calendar.Builder();
		build.setFields(Calendar.YEAR, y, Calendar.MONTH, m - 1, Calendar.DAY_OF_MONTH, d, Calendar.HOUR_OF_DAY, h,
				Calendar.MINUTE, min, Calendar.SECOND, s);
		return build.build().getTimeInMillis();

//		try {
//			return sdf.parse(date).getTime();
//		} catch (ParseException e) {
//			e.printStackTrace();
//			throw new RuntimeException(e.getMessage() == null ? "parsingerror" : e.getMessage(), e);
//		}
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
	public static FileInfo read(String line) {
		String[] qhash_filename_filesize_lmdate = line.split("["+COLUMN_SEPERATOR+"]");
		String qHash = qhash_filename_filesize_lmdate[0];
		String filename = qhash_filename_filesize_lmdate[1];
		long filesize = Long.parseLong(qhash_filename_filesize_lmdate[2]);
		long lmDate = string2date(qhash_filename_filesize_lmdate[3]);
		return new FileInfo(filename, filesize, lmDate, qHash);
	}

	public static void main(String[] args) {
		Date nowDate = new Date();
		long nowMillis = nowDate.getTime();
		String dateFromMillis = date2string(nowMillis);
		System.out.println(dateFromMillis);
		String twice = date2string(string2date(dateFromMillis));
		System.out.println(twice);
		String trice = date2string(string2date(twice));
		System.out.println(trice);
	}

}
