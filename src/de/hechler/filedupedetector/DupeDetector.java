package de.hechler.filedupedetector;

public class DupeDetector {

//	private final static String DEFAULT_START_FOLDER = ".";
//	private final static String DEFAULT_START_FOLDER = "C:\\BACKUP";
//	private final static String DEFAULT_START_FOLDER = "C:\\BACKUP\\FerisHandy";
//	private final static String DEFAULT_START_FOLDER = "D:\\BILDER";
//	private final static String DEFAULT_START_FOLDER = "D:\\BILDER\\feri";
//	private final static String DEFAULT_START_FOLDER = "D:\\BILDER\\ALEXA";
//	private final static String DEFAULT_START_FOLDER = "G:\\ALTE_BACKUP_HDDs\\PB_Store_and-Save_3500_500GB\\alt\\backup_ferissystem_13.03.2006\\Dokumente und Einstellungen\\feri\\Lokale Einstellungen\\Temporary Internet Files\\Content.IE5\\096RSPQ7";
//	private final static String DEFAULT_START_FOLDER = "G:\\DTP\\TSG";
//	private final static String DEFAULT_START_FOLDER = "G:\\";
//	private final static String DEFAULT_START_FOLDER = ".\\out\\testdir";
//	private final static String DEFAULT_START_FOLDER = ".\\out";
//	private final static String DEFAULT_START_FOLDER = "G:\\test";
//	private final static String DEFAULT_START_FOLDER = "H:\\";
//	private final static String DEFAULT_START_FOLDER = "H:\\99\\WD-Elements-2";

  private final static String DEFAULT_START_FOLDER = null;
	
	private final static String DEFAULT_OUTPUT_FILE = "out/scans/SG-BKpl-10TB-abc.out";
//	private final static String DEFAULT_OUTPUT_FILE = "out/SG-BKpl-10TB.out";
	
	public static boolean CALC_HASH = true;
	
	
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

			ScanStore store = new ScanStore();

			long startTime = System.currentTimeMillis();
			long delay = 0;
			if (startFolder != null) {
				store.scanFolder(startFolder);
				delay = System.currentTimeMillis() - startTime;
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
			}			
			
			ScanStore store2 = new ScanStore();

			startTime = System.currentTimeMillis();
			store2.read(outputFile);
			delay = System.currentTimeMillis() - startTime;
			System.out.println("Time read: "+(0.001*delay)+"s");

//			int[] cnt = new int[2];
//			store2.visitFiles((folder, file) -> cnt[0]++);
//			store2.visitFolders(folder -> cnt[1]++);
//			System.out.println("Folders: "+cnt[1]);
//			System.out.println("Files: "+cnt[0]);

			store2.calcSumInfoFromChildren();
			System.out.println("Folders: "+store2.getSumInfo().getNumFolders());
			System.out.println("Files: "+store2.getSumInfo().getNumFiles());
			System.out.println("Memory: "+Utils.readableBytes(store2.getSumInfo().getTotalMemory()));
			System.out.println("Duplicates: "+Utils.readableBytes(store2.getSumInfo().getDuplicateMemory()));

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
