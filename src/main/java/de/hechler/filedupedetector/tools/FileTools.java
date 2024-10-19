package de.hechler.filedupedetector.tools;

import java.text.DecimalFormat;

public class FileTools {

	public static String r1(float r) {
	    DecimalFormat df = new DecimalFormat("#.0");
	    return df.format(r);
	}
	
	public static String pretty(long bytes) {
		if (bytes < 1L<<10) {
			return bytes+" Bytes";
		}
		if (bytes < 1L<<20) {
			return r1((float)bytes/1024)+" KB";
		}
		if (bytes < 1L<<30) {
			return r1((float)bytes/1024/1024)+"MB";
		}
		if (bytes < 1L<<40) {
			return r1((float)bytes/1024/1024/1024)+" GB";
		}
		return r1((float)bytes/1024/1024/1024/1024)+" TB";
	}

}
