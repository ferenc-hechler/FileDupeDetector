package de.hechler.filedupedetector;

import java.nio.file.Path;
import java.util.List;

public interface GuiInterface {

	/**
	 * gibt zurueck, ob es sich bei dem aktuellen Element um eine Datei oder einen Ordner handelt. 
	 * @return
	 */
	public boolean isFolder();
	
	/**
	 * gibt zurueck, ob es sich bei dem aktuellen Element um eine Datei oder einen Ordner handelt. 
	 * @return
	 */
	public boolean isFile();
	
	/**
	 * gibt zurueck, ob es sich bei dem aktuellen Element um eine Laufwerk handelt. 
	 * @return
	 */
	public boolean isVolume();
	
	/**
	 * gibt zurueck, ob es sich bei dem aktuellen Element um den ScanStore handelt. 
	 * @return
	 */
	public boolean isRoot();
	
	/**
	 * Name der Datei oder des Ordners.
	 * @return
	 */
	public String getName();
	
	/**
	 * berechnet die Summenwerte neu
	 */
	public void refreshSumInfo();
	
	/**
	 * Summeninformationen zur Anzahl Dateien / Ordner, Speicher und Duplikate
	 * @return
	 */
	public SumInfo getSumInfo();
	
	/**
	 * gibt alle direkt untergeordneten Ordner zurück. Leere liste, wenn keine vorhanden.
	 * @return
	 */
	public List<GuiInterface> getChildFolders();

	/**
	 * gibt alle direkt enthaltenen Dateien zurück. Leere liste, wenn keine vorhanden.
	 * @return
	 */
	public List<GuiInterface> getChildFiles();

	/**
	 * gibt den uebergeordneten Ordner zurueck.
	 * @return
	 */
	public GuiInterface getParent();

	/**
	 * Gibt den Pfad für dieses Element zurück (Ordner oder Datei). 
	 * @return
	 */
	public Path getPath();
	
	/**
	 * fuer Volumes (isVolume()==true) kann abgefragt werden, wie groß das Laufwerk ist.
	 * @return
	 */
	public long getVolumeSize();
	
	/**
	 * loescht die Datei/den Ordner in den Metadaten, nicht im Dateisystem! 
	 */
	public void delete();
	
}
