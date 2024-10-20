package de.hechler.filedupedetector.gui;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import de.hechler.filedupedetector.BaseFolder;
import de.hechler.filedupedetector.FileInfo;
import de.hechler.filedupedetector.Folder;
import de.hechler.filedupedetector.GuiInterface;
import de.hechler.filedupedetector.QHashManager;
import de.hechler.filedupedetector.ScanStore;
import de.hechler.filedupedetector.SumInfo;
import de.hechler.filedupedetector.tools.FileTools;
import de.hechler.filedupedetector.tools.StopWatch;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.SubScene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableCell;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
import javafx.scene.control.cell.CheckBoxTreeTableCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class FileDupeDetectorMain extends Application {

	static FileDupeDetectorMain instance = null;

    private static final float WIDTH = 1400;
    private static final float HEIGHT = 1000;

	private Stage primary;
	private Label lbTextID;
	private Slider slider;
	private TreeTableView<Item> tree;	

	enum FOLDER_STATUS {
			UNIQUE, DUPLICATE, PARTIAL_DUPLICATE, SELECTED, HIDDEN
	}
	
    private final Image folderImage = new Image(getClass().getResourceAsStream("/img/folder-16.png"));
    private final Image folderRedImage = new Image(getClass().getResourceAsStream("/img/folder-red-16.png"));
    private final Image folderOrangeImage = new Image(getClass().getResourceAsStream("/img/folder-orange-16.png"));
    private final Image folderGreenImage = new Image(getClass().getResourceAsStream("/img/folder-green-16.png"));
    private final Image folderLightgrayImage = new Image(getClass().getResourceAsStream("/img/folder-lightgray-16.png"));

    private final Image fileImage = new Image(getClass().getResourceAsStream("/img/file-16.png"));
    private final Image fileRedImage = new Image(getClass().getResourceAsStream("/img/file-red-16.png"));
    private final Image fileOrangeImage = new Image(getClass().getResourceAsStream("/img/file-orange-16.png"));
    private final Image fileGreenImage = new Image(getClass().getResourceAsStream("/img/file-green-16.png"));
    private final Image fileLightgrayImage = new Image(getClass().getResourceAsStream("/img/file-lightgray-16.png"));

    private Node newFolderIcon(FOLDER_STATUS status) {
    	switch (status) {
		case UNIQUE: {
	    	return new ImageView(folderImage);
		}
		case DUPLICATE: {
	    	return new ImageView(folderRedImage);
		}
		case PARTIAL_DUPLICATE: {
	    	return new ImageView(folderOrangeImage);
		}
		case SELECTED: {
	    	return new ImageView(folderGreenImage);
		}
		case HIDDEN: {
	    	return new ImageView(folderLightgrayImage);
		}
		default:
			throw new IllegalArgumentException("Unexpected value: " + status);
		}
    }

    private Node newFileIcon(FOLDER_STATUS status) {
    	switch (status) {
		case UNIQUE: {
	    	return new ImageView(fileImage);
		}
		case DUPLICATE: {
	    	return new ImageView(fileRedImage);
		}
		case PARTIAL_DUPLICATE: {
	    	return new ImageView(fileOrangeImage);
		}
		case SELECTED: {
	    	return new ImageView(fileGreenImage);
		}
		case HIDDEN: {
	    	return new ImageView(fileLightgrayImage);
		}
		default:
			throw new IllegalArgumentException("Unexpected value: " + status);
		}
    }

    private ScanStore store;
	
	public FileDupeDetectorMain() {
		StopWatch sw = new StopWatch();
		System.out.println("scanning");
		store = new ScanStore();
		
//		store.scanFolder("./in/testdir");
//		store.scanFolder("D:\\EBOOKS");
//		System.out.println("scan: "+sw.stopStr());
		
//		store.write("./in/store.out");
//		store.write("./in/store-d_ebooks.out");
//		System.out.println("write: "+sw.stopStr());
		
		store.read("./in/store.out");
//		store.read("./in/store-d_ebooks.out");
		System.out.println("read: "+sw.stopStr());
		
		System.out.println("collect hash dupes");
		QHashManager.getInstance().collectHashDupes(store);
		System.out.println("collect dups: "+sw.stopStr());
		System.out.println("calc sum info");
		store.calcSumInfoFromChildren();
		System.out.println("calc sum: "+sw.stopStr());
		System.out.println("open");
		open();
	}

	
    
    // from: https://stackoverflow.com/questions/31185441/javafx-treetableview-and-expanding-items-without-children
    public static class ItemTreeNode extends TreeItem<Item> {
        private boolean childrenLoaded = false ;

        public ItemTreeNode(Item value) {
            super(value);
        }

        @Override
        public boolean isLeaf() {
            if (childrenLoaded) {
                return getChildren().isEmpty() ;
            }
            GuiInterface guiInterface = getValue().guiInterface;
            if (guiInterface== null) {
            	return false;
            }
            if (guiInterface.isFile()) {
            	return true;
            }
            if (guiInterface.getSumInfo().getNumFiles()+guiInterface.getSumInfo().getNumFiles()==0) {
            	return true;
            }
            return false;
        }

        public boolean childrenLoaded() {
        	return childrenLoaded;
        }
        
        @Override
        public ObservableList<TreeItem<Item>> getChildren() {
            if (childrenLoaded) {
                return super.getChildren();
            }
            childrenLoaded = true ;

            List<TreeItem<Item>> children = new ArrayList<>();
            GuiInterface guiInterface = getValue().guiInterface;
            if ((guiInterface != null) && guiInterface.isFolder()) {
            	List<GuiInterface> childFolders = guiInterface.getChildFolders();
                for (GuiInterface childFolder:childFolders) {
                    children.add(new ItemTreeNode(new Item(childFolder)));
                }
            	List<GuiInterface> childFiles = guiInterface.getChildFiles();
                for (GuiInterface childFile:childFiles) {
                    children.add(new ItemTreeNode(new Item(childFile)));
                }
            }
            if (!children.isEmpty()) {
            	super.getChildren().addAll(children);
            }
            else {
                // great big hack:
                super.getChildren().add(null);
                super.getChildren().clear();
            }
            return super.getChildren() ;
        }
    }

    public static class Item {
    	static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    	
    	GuiInterface guiInterface;
        private StringProperty name = new SimpleStringProperty();
        private LongProperty size = new SimpleLongProperty();
        private LongProperty duplicateSize = new SimpleLongProperty();
        private LongProperty duplicateRatioSize = new SimpleLongProperty();
        private StringProperty lastModified = new SimpleStringProperty();
        private StringProperty hash = new SimpleStringProperty();
        private IntegerProperty duplicates = new SimpleIntegerProperty();
        private BooleanProperty mark = new SimpleBooleanProperty();

        public Item(GuiInterface guiInterface) {
        	this.guiInterface = guiInterface;
        	if (guiInterface.isFile()) {
        		FileInfo fi = (FileInfo) guiInterface;
        		char typeChar ='|';
        		int duplicates = QHashManager.getInstance().getCountDupes(fi.getqHash());
        		char statusChar;
        		if (duplicates == 0) {
        			statusChar = 'U';
        		}
        		else if (QHashManager.getInstance().isSelectFileForHash(fi)) {
        			statusChar = 'S';
        		}
        		else if (QHashManager.getInstance().isHiddenByOtherFileForHash(fi)) {
        			statusChar = 'H';
        		}
        		else {
        			statusChar = 'D';
        		}
        		
                setName(fi.getName()+typeChar+statusChar);
                setSize(fi.getFilesize());
                setDuplicateSize(fi.getSumInfo().getDuplicateMemory());
                setDuplicateRatioSize(fi.getSumInfo().getDuplicateRatioMemory());
                setLastModified(long2datetimestring(fi.getLastModified()));
                setHash(fi.getqHash());
                setDuplicates(duplicates);
                setMark(false);
        	}
        	else {
        		Folder folder = (Folder) guiInterface;
        		char typeChar ='/';
        		SumInfo sumInfo = folder.getSumInfo();
        		char statusChar = 'U';
        		if (sumInfo.getDuplicateMemory()>0) {
            		if (sumInfo.getDuplicateMemory()==sumInfo.getTotalMemory()) {
            			statusChar = 'D';
            		}
            		else {
            			statusChar = 'P';
            		}
        		}
                setName(folder.getName()+typeChar+statusChar);
                setSize(sumInfo.getTotalMemory());
                setDuplicateSize(sumInfo.getDuplicateMemory());
                setDuplicateRatioSize(sumInfo.getDuplicateRatioMemory());
                setLastModified(sumInfo.getLastModifiedString());
                setHash("folders: #"+sumInfo.getNumFolders()+", files: #"+sumInfo.getNumFiles());
                setDuplicates(0);
                setMark(false);
        	}
            mark.addListener(bProp -> markChanged(((BooleanProperty)bProp).get()));
        }

		public void update() {
			if (guiInterface==null) {
				return;
			}
        	if (guiInterface.isFile()) {
        		FileInfo fi = (FileInfo) guiInterface;
        		char typeChar ='|';
        		int duplicates = QHashManager.getInstance().getCountDupes(fi.getqHash());
        		char statusChar;
        		if (duplicates == 0) {
        			statusChar = 'U';
        		}
        		else if (QHashManager.getInstance().isSelectFileForHash(fi)) {
        			statusChar = 'S';
        		}
        		else if (QHashManager.getInstance().isHiddenByOtherFileForHash(fi)) {
        			statusChar = 'H';
        		}
        		else {
        			statusChar = 'D';
        		}
        		
                setName(fi.getName()+typeChar+statusChar);
                setSize(fi.getFilesize());
                setDuplicateSize(fi.getSumInfo().getDuplicateMemory());
                setDuplicateRatioSize(fi.getSumInfo().getDuplicateRatioMemory());
                setLastModified(long2datetimestring(fi.getLastModified()));
                setHash(fi.getqHash());
                setDuplicates(duplicates);
        	}
        	else {
        		Folder folder = (Folder) guiInterface;
        		char typeChar ='/';
        		SumInfo sumInfo = folder.getSumInfo();
        		char statusChar = 'U';
        		if (sumInfo.getDuplicateMemory()>0) {
            		if (sumInfo.getDuplicateMemory()==sumInfo.getTotalMemory()) {
            			statusChar = 'D';
            		}
            		else {
            			statusChar = 'P';
            		}
        		}
                setName(folder.getName()+typeChar+statusChar);
                setSize(sumInfo.getTotalMemory());
                setDuplicateSize(sumInfo.getDuplicateMemory());
                setDuplicateRatioSize(sumInfo.getDuplicateRatioMemory());
                setLastModified(sumInfo.getLastModifiedString());
                setHash("folders: #"+sumInfo.getNumFolders()+", files: #"+sumInfo.getNumFiles());
                setDuplicates(0);
        	}
		}


        
        private void markChanged(boolean newValue) {
        	System.out.println(getName()+" selection "+newValue);
    		if (guiInterface.isFile()) {
    			FileInfo fi = (FileInfo)guiInterface;
        		String qHash = fi.getqHash();
	        	if (newValue) {
	        		QHashManager.getInstance().selectFileForHash(fi);
	        	}
	        	else {
	        		QHashManager.getInstance().unselectFileForHash(fi);
	        	}
        	}
		}

		public Item(GuiInterface guiInterface, String name, long size, long duplicateSize, long duplicateRatioSize, String lastModified, String hash, int duplicates) {
        	this.guiInterface = guiInterface;
            setName(name);
            setSize(size);
            setDuplicateSize(duplicateSize);
            setDuplicateRatioSize(duplicateRatioSize);
            setLastModified(lastModified);
            setHash(hash);
            setDuplicates(duplicates);
            setMark(false);
        }

        static String long2datetimestring(long millis) {
        	Date date = new Date(millis);
        	return sdf.format(date);
        }
        
        public final LongProperty sizeProperty() {
            return this.size;
        }
        public final long getSize() {
            return this.sizeProperty().get();
        }
        public final void setSize(final long size) {
            this.sizeProperty().set(size);
        }

        public final LongProperty duplicateSizeProperty() {
            return this.duplicateSize;
        }
        public final long getDuplicateSize() {
            return this.duplicateSizeProperty().get();
        }
        public final void setDuplicateSize(final long duplicateSize) {
            this.duplicateSizeProperty().set(duplicateSize);
        }

        public final LongProperty duplicateRatioSizeProperty() {
            return this.duplicateRatioSize;
        }
        public final long getDuplicateRatioSize() {
            return this.duplicateRatioSizeProperty().get();
        }
        public final void setDuplicateRatioSize(final long duplicateRatioSize) {
            this.duplicateRatioSizeProperty().set(duplicateRatioSize);
        }

        public final StringProperty nameProperty() {
            return this.name;
        }
        public final java.lang.String getName() {
            return this.nameProperty().get();
        }
        public final void setName(final java.lang.String name) {
            this.nameProperty().set(name);
        }

        public final StringProperty lastModifiedProperty() {
            return this.lastModified;
        }
        public final java.lang.String getLastModified() {
            return this.lastModifiedProperty().get();
        }
        public final void setLastModified(final java.lang.String lastModified) {
            this.lastModifiedProperty().set(lastModified);
        }

        public final StringProperty hashProperty() {
            return this.hash;
        }
        public final java.lang.String getHash() {
            return this.hashProperty().get();
        }
        public final void setHash(final java.lang.String hash) {
            this.hashProperty().set(hash);
        }

        public final IntegerProperty duplicatesProperty() {
            return this.duplicates;
        }
        public final int getDuplicates() {
            return this.duplicatesProperty().get();
        }
        public final void setDuplicates(final int duplicates) {
            this.duplicatesProperty().set(duplicates);
        }

        public final BooleanProperty markProperty() {
            return this.mark;
        }
        public final boolean getMark() {
            return this.markProperty().get();
        }
        public final void setMark(final boolean mark) {
            this.markProperty().set(mark);
        }

    }


    
	@Override
	public void start(Stage primaryStage) throws Exception {
		primary = primaryStage;

		primary.setTitle("3D Output");
		Button btPrevious = new Button("<");
		btPrevious.setOnAction(event -> {
			System.out.println("<");;
		});
		Button btNext = new Button(">");
		btNext.setOnAction(event -> {
			System.out.println(">");;
		});
		
		Button btSmaller = new Button("v");
        btSmaller.setOnAction(ev -> {
			System.out.println("v");;
        });
        
        Button btBigger = new Button("^");
        btBigger.setOnAction(ev -> {
			System.out.println("^");;
        });
		
        Button btAdjustScale = new Button("Recalc Tree");
        btAdjustScale.setOnAction(ev -> {
			recalcTree();
        });
        
        Button btScaleUp = new Button("+");
        btScaleUp.setOnAction(ev -> {
			System.out.println("+");;
        });
        
        Button btScaleDown = new Button("-");
        btScaleDown.setOnAction(ev -> {
			System.out.println("-");;
        });
        
		lbTextID = new Label("0");
		HBox buttons = new HBox(btPrevious, btNext, btSmaller, btBigger, btAdjustScale, btScaleUp, btScaleDown, lbTextID);
		buttons.setSpacing(5);
//		buttons.setPadding(new Insets(5));
		
        slider = new Slider(0, 10000, 0);
        slider.setOrientation(Orientation.HORIZONTAL);
        slider.valueProperty().addListener(new ChangeListener<Number>() {
            public void changed(ObservableValue<? extends Number> ov,
                    Number old_val, Number new_val) {
            	double percent = new_val.doubleValue() * 0.0001;
            	System.out.println(percent);
            	// switchPage(page);
                }
            });
        slider.setPrefWidth(WIDTH-50);
		
		VBox container2D = new VBox(buttons, slider);
		container2D.setSpacing(15);
		container2D.setPadding(new Insets(25));
		container2D.setAlignment(Pos.CENTER);
		
		Group group2D = new Group(container2D);
		SubScene subScene2D = new SubScene(group2D, WIDTH, 80);

		
        tree = new TreeTableView<>();

        TreeTableColumn<Item, String> nameCol = new TreeTableColumn<>("Name");
        nameCol.setCellValueFactory(cellData -> cellData.getValue().getValue().nameProperty());
        nameCol.setPrefWidth(300);

        // from: https://stackoverflow.com/questions/36917220/javafx-treetableview-leaves-icons-behind-when-collapsing
        // cell factory to display graphic:
        nameCol.setCellFactory(ttc -> new TreeTableCell<Item, String>() {

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
	                setText(null);
	                setGraphic(null);
	                return;
                }
                int len = item.length();
                String text = item.substring(0, len-2);
                char typeChar = item.charAt(len-2);
                char statusChar = item.charAt(len-1);
                
                FOLDER_STATUS status = null;
                switch (statusChar) {
                case 'U':
                	status = FOLDER_STATUS.UNIQUE;
                	break;
                case 'D':
                	status = FOLDER_STATUS.DUPLICATE;
                	break;
                case 'P':
                	status = FOLDER_STATUS.PARTIAL_DUPLICATE;
                	break;
                case 'S':
                	status = FOLDER_STATUS.SELECTED;
                	break;
                case 'H':
                	status = FOLDER_STATUS.HIDDEN;
                	break;
        		default:
        			throw new IllegalArgumentException("Unexpected status char: " + statusChar);
        		}
                
                switch (typeChar) {
                case '/':
	                setText(text);
	                setGraphic(newFolderIcon(status));
                	break;
                case '|':
	                setText(text);
	                setGraphic(newFileIcon(status));
                	break;
        		default:
        			throw new IllegalArgumentException("Unexpected type char: " + typeChar);
        		}
            }
        });
        
        TreeTableColumn<Item, Number> sizeCol = new TreeTableColumn<>("Size");
        sizeCol.setCellValueFactory(cellData -> cellData.getValue().getValue().sizeProperty());
        sizeCol.setCellFactory(ttc -> new TreeTableCell<Item, Number>() {

            @Override
            protected void updateItem(Number nSize, boolean empty) {
                super.updateItem(nSize, empty);
                if (empty) {
	                setText(null);
	                return;
                }
                String text = FileTools.pretty(nSize.longValue());
                setText(text);
            }
        });
        sizeCol.setStyle("-fx-alignment: CENTER-RIGHT;");
        sizeCol.setPrefWidth(100);

        TreeTableColumn<Item, Number> duplicateSizeCol = new TreeTableColumn<>("Dup. Size");
        duplicateSizeCol.setCellValueFactory(cellData -> cellData.getValue().getValue().duplicateSizeProperty());
        duplicateSizeCol.setCellFactory(ttc -> new TreeTableCell<Item, Number>() {

            @Override
            protected void updateItem(Number nDuplicateSize, boolean empty) {
                super.updateItem(nDuplicateSize, empty);
                if (empty) {
	                setText(null);
	                return;
                }
                String text = FileTools.pretty(nDuplicateSize.longValue());
                setText(text);
            }
        });
        duplicateSizeCol.setStyle("-fx-alignment: CENTER-RIGHT;");
        duplicateSizeCol.setPrefWidth(100);

        TreeTableColumn<Item, Number> duplicateRatioSizeCol = new TreeTableColumn<>("Dup. Ratio Size");
        duplicateRatioSizeCol.setCellValueFactory(cellData -> cellData.getValue().getValue().duplicateRatioSizeProperty());
        duplicateRatioSizeCol.setCellFactory(ttc -> new TreeTableCell<Item, Number>() {

            @Override
            protected void updateItem(Number nDuplicateRatioSize, boolean empty) {
                super.updateItem(nDuplicateRatioSize, empty);
                if (empty) {
	                setText(null);
	                return;
                }
                String text = FileTools.pretty(nDuplicateRatioSize.longValue());
                setText(text);
            }
        });
        duplicateRatioSizeCol.setStyle("-fx-alignment: CENTER-RIGHT;");
        duplicateRatioSizeCol.setPrefWidth(100);

        TreeTableColumn<Item, String> lastModifiedCol = new TreeTableColumn<>("Last Modified");
        lastModifiedCol.setCellValueFactory(cellData -> cellData.getValue().getValue().lastModifiedProperty());
        lastModifiedCol.setPrefWidth(125);

        TreeTableColumn<Item, String> hashCol = new TreeTableColumn<>("SHA256");
        hashCol.setCellValueFactory(cellData -> cellData.getValue().getValue().hashProperty());
        hashCol.setPrefWidth(420);

        TreeTableColumn<Item, Number> duplicateCol = new TreeTableColumn<>("#dups");
        duplicateCol.setCellValueFactory(cellData -> cellData.getValue().getValue().duplicatesProperty());
        duplicateCol.setStyle("-fx-alignment: CENTER-RIGHT;");
        duplicateCol.setPrefWidth(50);

        
        TreeTableColumn<Item, Boolean> markCol = new TreeTableColumn<>("Mark");
        markCol.setCellValueFactory(cellData -> cellData.getValue().getValue().markProperty());
        markCol.setCellFactory(CheckBoxTreeTableCell.forTreeTableColumn(markCol));
        markCol.setStyle("-fx-alignment: CENTER;");
        
        tree.getColumns().addAll(Arrays.asList(nameCol, sizeCol, duplicateSizeCol, duplicateRatioSizeCol, lastModifiedCol, hashCol, duplicateCol, markCol));
        tree.setEditable(true);
        
        ItemTreeNode rootNode = new ItemTreeNode(new Item(null, "root", 0, 0, 0, "", "", 0)); 
        tree.setRoot(rootNode);
        tree.setShowRoot(false);
        List<BaseFolder> baseFolders = store.getBaseFolders();
        for (BaseFolder bf:baseFolders) {
        	rootNode.getChildren().add(new ItemTreeNode(new Item(bf)));
        }
		
		tree.setPrefHeight(HEIGHT-300);
        
		VBox vbox = new VBox(subScene2D, tree);
		Group groupALL = new Group(vbox);
		Scene scene = new Scene(groupALL);
		
		primary.setScene(scene);
		primaryStage.show();
	}
	
    
	private void recalcTree() {
		store.updateSumInfoFromChildren();
		recursiveRecalcTree(tree.getRoot());
	}

	private void recursiveRecalcTree(TreeItem<Item> treeItem) {
		recalcTreeItem(treeItem);
		ItemTreeNode node = (ItemTreeNode) treeItem;
		if (node.childrenLoaded) {
			for (TreeItem<Item> ti:node.getChildren()) {
				recursiveRecalcTree(ti);
			}
		}
	}

	private void recalcTreeItem(TreeItem<Item> treeItem) {
		Item item = treeItem.getValue();
		item.update();
	}

	public static void sleep(int ms) {
		try {
			Thread.sleep(ms);
		} catch (InterruptedException e) {
			throw new RuntimeException(e.toString(), e);
		}
	}

	private synchronized void open() {
		instance = null;
		try {
			Platform.runLater(()->{
				Stage prim = new Stage();
				try {
					start(prim);
				} catch (Exception e) {
					throw new RuntimeException(e.toString(), e);
				}
				instance = this; 
			});
		}
		catch (IllegalStateException tkEx) {
			Platform.startup(()->{
				Stage prim = new Stage();
				try {
					start(prim);
				} catch (Exception e) {
					throw new RuntimeException(e.toString(), e);
				}
				instance = this; 
			});
		}
		while (instance == null) {
			System.out.println("WAIT");
			sleep(500);
		}
		System.out.println("FOUND");		
	}

	public static void main(String[] args) throws Exception {
		System.out.println("START");
		new FileDupeDetectorMain();
		System.out.println("FINISHED");
	}
	
}