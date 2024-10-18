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
import de.hechler.filedupedetector.ScanStore;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.LongProperty;
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
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class FileDupeDetectorMain extends Application {

	static FileDupeDetectorMain instance = null;

    private static final float WIDTH = 1400;
    private static final float HEIGHT = 1000;

	private Stage primary;
	private Label lbTextID;
	private Slider slider;

    private final Image folderImage = new Image(getClass().getResourceAsStream("/img/folder-16.png"));

    private final Image fileImage = new Image(getClass().getResourceAsStream("/img/file-16.png"));

    private Node newFolderIcon() {
    	return new ImageView(folderImage);
    }

    private Node newFileIcon() {
    	return new ImageView(fileImage);
    }

    
	private String title;
	
	double scale;
	double offsetX;
	double offsetY;
	double offsetZ;
	double radiusScale;

    Color white; 
    Color red; 
    Color green; 
    Color yellow; 
    Color blue; 
    Color black; 
    
    private ScanStore store;
	
	public FileDupeDetectorMain(String title) {
		this.title = title;
		this.scale = 1.0;
		this.offsetX = 0.0;
		this.offsetY = 0.0;
		this.offsetZ = 0.0;
		this.radiusScale = 2.0;
		initColors();
		store = new ScanStore();
		store.scanFolder("./in/testdir");
//		store.write("./in/store.out");
//		store.read("./in/store.out");
		open();
	}

	
    
	public void initColors() {
        white  = new Color(1.0, 1.0, 1.0, 1.0); 
        red    = new Color(1.0, 0.1, 0.1, 1.0); 
        green  = new Color(0.1, 1.0, 0.1, 1.0); 
        blue   = new Color(0.1, 0.1, 1.0, 1.0); 
        yellow = new Color(1.0, 1.0, 0.1, 1.0); 
        black  = new Color(0.0, 0.0, 0.0, 1.0);
        
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
            return (getValue().guiInterface != null) && getValue().guiInterface.isFile();
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

        public Item(GuiInterface guiInterface) {
        	this.guiInterface = guiInterface;
        	if (guiInterface.isFile()) {
        		FileInfo fi = (FileInfo) guiInterface;
                setName(fi.getName());
                setSize(fi.getFilesize());
                setLastModified(long2datetimestring(fi.getLastModified()));
                setHash(fi.getqHash());
        	}
        	else {
        		Folder folder = (Folder) guiInterface;
                setName(folder.getName()+"/");
                setSize(0);
                setLastModified("");
                setHash("");
        	}
        }

        static String long2datetimestring(long millis) {
        	Date date = new Date(millis);
        	return sdf.format(date);
        }
        
        public Item(GuiInterface guiInterface, String name, long size, String lastModified, String hash) {
        	this.guiInterface = guiInterface;
            setName(name);
            setSize(size);
            setLastModified(lastModified);
            setHash(hash);
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
                if ((item!=null) && item.endsWith("/")) {
	                setText(empty ? null : item.substring(0, item.length()-1));
	                setGraphic(empty ? null : newFolderIcon());
                }
                else {
	                setText(empty ? null : item);
	                setGraphic(empty ? null : newFileIcon());
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

        tree.getColumns().addAll(Arrays.asList(nameCol, sizeCol, lastModifiedCol, hashCol));

        ItemTreeNode rootNode = new ItemTreeNode(new Item(null, "root", 0, "", "")); 
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
		FileDupeDetectorMain output = new FileDupeDetectorMain("FileDupeDetectorMain");
		System.out.println("FINISHED");
	}
	
}