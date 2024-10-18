package de.hechler.filedupedetector.tools;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ExtensionInfo {

	private final static String EXT_CATEGORIES = 
			  "a:audio:mp3.ogg.wav.wma.mpa.aac.wpl.mid.midi.voc.aax|"
			+ "b:binary:class.dll.bin.a.lib.dex.map.tpl.o.obj.so.hdr.cab.pak.sys.apk.pdb.pcode.pkg.odl.tpw.pyi.jad.drv.xul.pickle.profile|"
			+ "c:code:java.js.h.xml.py.hpp.json.md.cpp.c.ts.pom.prg.xsd.jsp.xsl.dtd.exsd.sql.gradle.wsdl.asm.cmake.yml.cs.vcxproj.dsd.idl.xls.npmignore.patch.yaml.classpath.groovy.xhtml.cvsignore.prolern.pas.metadata.aidl.aspx.wsdd.rsh.markdown.script.ddt.def.gitignore.mf.jspx.jmod.r.csproj.tmpl.npm.cxx.jspf.mak.asp.make.desc.m4.str.vbs.scala.inc.preprocessing.vb.src.ipynb.mk.vcproj.pro.node.vcode.fatjar.cc-tst.manifest.gitattributes.xmi.vbx.hsql|"
			+ "d:document:eml.txt.doc.htm.pdf.docx.rtf.chm.ps.ppt.ai.pf.pptx.emf.ots.xlsx.eps.odt.wri.x3d.mobi.dotx|"
			+ "e:executable:exe.bat.sh.cpl.cmd.com.msi.cab.ocx.bsh.pif.scr|"
			+ "f:font:ttf.pfb.fon.woff.ffa.otf.emf.woff2|"
			+ "h:hash:md5.sha512.fingerprint.sha|"
			+ "i:image:jpg.png.gif.svg.bmp.cdr.ico.wmf.swf.tif.tga.raw.psd.jpeg.cur.images.cmx.pcx.img.3tiff.pict.ppm.cdt.cpl.icns.8bf|"
			+ "k:key:p7x.pfx.keystore.p12.keyring.pfa.pgp|"
			+ "p:properties:properties.ini.prefs.cfg.conf.mem.config.cfx.options.props.prop.cf.settings.xcconfig|"
			+ "s:database:dat.ntx.db.dbf.idx.dbx.sqlite.dbt.storage.sav.mdb.autosave.data|"
			+ "t:text:info.log.inf.vcf.msg.txt.csv.manifest.pat.url.reg.hlp.log1.log2.wiki.cat.hex.asc.version.text.out.strings|"
			+ "v:video:mp4.avi.wmv.mpg.m4a.mov.3gp.vob.ifo.m2ts.ani.nvi.flv|"
			+ "w:web:html.php.htm.css.htaccess|"
			+ "z:zip:jar.zip.gz.xar.tib.loggz.ear.bz2.tlb.rar.war.arj.archive.pac.pack.iso.osm.egg-info.z.tgz.tar";

	private List<String> categoriesList;
	private Map<String, String> category2nameMap;
	private Map<String, String> ext2categoryMap;
	private Map<String, Set<String>> category2extsMap;

	private static ExtensionInfo instance;
	private ExtensionInfo() {
		categoriesList = new ArrayList<>();
		category2nameMap = new HashMap<>();
		ext2categoryMap = new HashMap<>();
		category2extsMap = new HashMap<>();
		String[] categoryInfos = EXT_CATEGORIES.split("[|]");
		for (String categoryInfo:categoryInfos) {
			String[] cat_name_exts = categoryInfo.split("[:]");
			String cat = cat_name_exts[0];
			String name = cat_name_exts[1];
			String[] exts = cat_name_exts[2].split("[.]");
			categoriesList.add(cat);
			for (String ext:exts) {
				category2nameMap.put(cat, name);
				ext2categoryMap.put(ext, cat);
				Set<String> extList = category2extsMap.get(cat);
				if (extList == null) {
					extList = new HashSet<>();
					category2extsMap.put(cat, extList);
				}
				extList.add(ext);
			}
		}
	}
	public static ExtensionInfo getInstance() {
		if (instance == null) {
			instance = new ExtensionInfo();
		}
		return instance;
	}
	
	
	public List<String> getCategories() {
		return categoriesList;
	}
	
	public String getName(String category) {
		return category2nameMap.get(category);
	}
	
	public String getCategory(String extension) {
		return ext2categoryMap.get(extension);
	}
	
	public Set<String> getExts(String category) {
		Set<String> result = category2extsMap.get(category);
		if (result == null) {
			result = Collections.emptySet();
		}
		return result;
	}
	
	public Set<String> getExts(List<String> categories) {
		Set<String> result = new HashSet<>();
		for (String category:categories) {
			result.addAll(getExts(category));
		}
		return result;
	}
	
	
}
