package de.hechler.filedupedetector;

public class DupeDetector {

//	private final static String DEFAULT_START_FOLDER = ".";
//	 private final static String DEFAULT_START_FOLDER = "C:\\BACKUP";
//	private final static String DEFAULT_START_FOLDER = "C:\\BACKUP\\FerisHandy";
//	 private final static String DEFAULT_START_FOLDER = "D:\\BILDER";
	 private final static String DEFAULT_START_FOLDER = "D:\\BILDER\\feri";
//	 private final static String DEFAULT_START_FOLDER = "D:\\BILDER\\ALEXA";
	
	
	private final static String DEFAULT_OUTPUT_FILE = "out/test.out";
	
	
	public static void main(String[] args) {
		try {
			
			String startFolder = DEFAULT_START_FOLDER;
			String outputFile = DEFAULT_OUTPUT_FILE;
	
			if (args.length > 0) {
				startFolder = args[0];
			}
			
			if (args.length > 1) {
				outputFile = args[1];
			}

			long startTime = System.currentTimeMillis();
			ScanStore store = new ScanStore();
			store.scanFolder(startFolder);
			long delay = System.currentTimeMillis() - startTime;

			store.scanFolder(".");

			store.write();

			System.out.println();
			System.out.println("Time: "+(0.001*delay)+"s");

			int[] cnt = new int[2];
			store.visitFiles((folder, file) -> cnt[0]++);
			store.visitFolders(folder -> cnt[1]++);
			System.out.println("Folders: "+cnt[1]);
			System.out.println("Files: "+cnt[0]);

			startTime = System.currentTimeMillis();
			store.write(outputFile);
			delay = System.currentTimeMillis() - startTime;
			System.out.println("Time write1: "+(0.001*delay)+"s");
			
			
			ScanStore store2 = new ScanStore();

			startTime = System.currentTimeMillis();
			store2.read(outputFile);
			delay = System.currentTimeMillis() - startTime;
			System.out.println("Time read: "+(0.001*delay)+"s");
			
			startTime = System.currentTimeMillis();
			store2.write(outputFile+"2");
			delay = System.currentTimeMillis() - startTime;
			System.out.println("Time write2: "+(0.001*delay)+"s");
			
			
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}


}
