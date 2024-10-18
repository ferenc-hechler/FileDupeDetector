package de.hechler.filedupedetector;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class Utils {

	private final static long KILO = 1024L; 
	private final static long MEGA = KILO*KILO; 
	private final static long GIGA = KILO*MEGA; 
	private final static long TERA = KILO*GIGA; 
	private final static double  DIV_KILO = 1.0/KILO;
	private final static double  DIV_MEGA = DIV_KILO/KILO;
	private final static double  DIV_GIGA = DIV_MEGA/KILO;
	private final static double  DIV_TERA = DIV_GIGA/KILO;
	
	public static String readableBytes(long sum) {
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

	public static String round1(double value) {
		String result = Double.toString(value);
		int dotPos = result.indexOf('.');
		if (dotPos != -1) {
			result = result.substring(0, dotPos+2);
		}
		return result;
	}


	public static String bytesToHex(byte[] hash) {
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

	/**
	 * NOT THREADSAFE!
	 */
	public final static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH_mm_ss");

	/**
	 * NOT THREADSAFE!
	 */
	public static String date2string(long millis) {
		return sdf.format(new Date(millis));
	}

	public static long string2date(String date) {
		try {
			return sdf.parse(date).getTime();
		} catch (ParseException e) {
			throw new RuntimeException(e);
		}
	}

	public static long string2datePat(String date) {
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
	}

}
