package de.hechler.filedupedetector;

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
	
}
