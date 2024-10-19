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
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.LongProperty;
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

	enum FOLDER_STATUS {
			UNIQUE, DUPLICATE, PARTIAL_DUPLICATE
	}
	
    private final Image folderImage = new Image(getClass().getResourceAsStream("/img/folder-16.png"));
    private final Image folderRedImage = new Image(getClass().getResourceAsStream("/img/folder-red-16.png"));
    private final Image folderOrangeImage = new Image(getClass().getResourceAsStream("/img/folder-orange-16.png"));

    private final Image fileImage = new Image(getClass().getResourceAsStream("/img/file-16.png"));
    private final Image fileRedImage = new Image(getClass().getResourceAsStream("/img/file-red-16.png"));
    private final Image fileOrangeImage = new Image(getClass().getResourceAsStream("/img/file-orange-16.png"));

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
		default:
			throw new IllegalArgumentException("Unexpected value: " + status);
		}
    }

    private ScanStore store;
	
	public FileDupeDetectorMain() {
		System.out.println("scanning");
		store = new ScanStore();
		store.scanFolder("./in/testdir");
//		store.write("./in/store.out");
//		store.read("./in/store.out");
		System.out.println("collect hash dupes");
		QHashManager.getInstance().collectHashDupes(store);
		System.out.println("collect sum info");
		store.calcSumInfoFromChildren();
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
        private StringProperty lastModified = new SimpleStringProperty();
        private StringProperty hash = new SimpleStringProperty();
        private IntegerProperty duplicates = new SimpleIntegerProperty();

        public Item(GuiInterface guiInterface) {
        	this.guiInterface = guiInterface;
        	if (guiInterface.isFile()) {
        		FileInfo fi = (FileInfo) guiInterface;
        		char typeChar ='|';
        		int duplicates = QHashManager.getInstance().getCountDupes(fi.getqHash());
        		char statusChar = duplicates == 0 ? 'U' : 'D';
                setName(fi.getName()+typeChar+statusChar);
                setSize(fi.getFilesize());
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
                setSize(0);
                setLastModified("");
                setHash("");
                setDuplicates(0);
        	}
        }

        public Item(GuiInterface guiInterface, String name, long size, String lastModified, String hash, int duplicates) {
        	this.guiInterface = guiInterface;
            setName(name);
            setSize(size);
            setLastModified(lastModified);
            setHash(hash);
            setDuplicates(duplicates);
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
		
        Button btAdjustScale = new Button("Adjust Scale");
        btAdjustScale.setOnAction(ev -> {
			System.out.println("Adjust Scale");;
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

		
        TreeTableView<Item> tree = new TreeTableView<>();

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
        sizeCol.setStyle("-fx-alignment: CENTER-RIGHT;");
        sizeCol.setPrefWidth(100);

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

        tree.getColumns().addAll(Arrays.asList(nameCol, sizeCol, lastModifiedCol, hashCol, duplicateCol));

        ItemTreeNode rootNode = new ItemTreeNode(new Item(null, "root", 0, "", "", 0)); 
        tree.setRoot(rootNode);
        tree.setShowRoot(false);
        List<BaseFolder> baseFolders = store.getBaseFolders();
        for (BaseFolder bf:baseFolders) {
        	rootNode.getChildren().add(new ItemTreeNode(new Item(bf)));
        }
		
//		TreeItem<String> treeItem = new TreeItem<String> ("Inbox", newFolderIcon());
//        treeItem.setExpanded(true);
//        for (int i = 1; i < 6; i++) {
//            TreeItem<String> item = new TreeItem<String> ("Message" + i, newFileIcon());            
//            treeItem.getChildren().add(item);
//        }        
//        TreeView<String> treeView = new TreeView<String> (treeItem);        
//        StackPane tree = new StackPane();
//        tree.getChildren().add(treeView);
		tree.setPrefHeight(HEIGHT-300);
        
		VBox vbox = new VBox(subScene2D, tree);
		Group groupALL = new Group(vbox);
		Scene scene = new Scene(groupALL);
		
		primary.setScene(scene);
		primaryStage.show();
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