module de.hechler.filedupedetector {
    requires transitive javafx.controls;
    requires java.desktop;
	requires org.fxyz3d.core;
	requires poly2tri.core;
    exports de.hechler.filedupedetector;
    exports de.hechler.filedupedetector.gui;
    exports de.hechler.filedupedetector.gui.objects;
}