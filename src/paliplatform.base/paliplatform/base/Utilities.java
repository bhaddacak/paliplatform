/*
 * Utilities.java
 *
 * Copyright (C) 2023-2025 J. R. Bhaddacak 
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at
 * your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see https://www.gnu.org/licenses/.
 */

package paliplatform.base;

import java.lang.module.*;
import java.util.*;
import java.util.zip.*;
import java.util.stream.*;
import java.util.concurrent.ExecutorService;
import java.io.*;
import java.nio.file.*;
import java.nio.charset.StandardCharsets;
import java.text.RuleBasedCollator;
import java.sql.*;
import java.security.*;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.application.Platform;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.Scene;
import javafx.scene.Parent;
import javafx.scene.Node;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.text.Font;
import javafx.geometry.*;
import javafx.util.StringConverter;
import javafx.css.Styleable;
import javafx.beans.property.SimpleBooleanProperty;

import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import javafx.stage.Stage;
import static javafx.stage.FileChooser.ExtensionFilter;

import org.apache.commons.compress.archivers.*;
import org.apache.commons.compress.archivers.tar.*;
import org.apache.commons.compress.archivers.zip.*;
import org.apache.commons.io.IOUtils;
import org.apache.commons.csv.*;

/** 
 * The main method factory for various uses, including common constants.
 * @author J.R. Bhaddacak
 * @version 3.0
 * @since 2.0
 */
final public class Utilities {
	public static final String VERSION = "3.0";
	public static Path ROOTPATH = Path.of(".");
	public static String ROOTDIR = "";
	public static final String IMGDIR = "resources/images/";
	public static final String FONTDIR = "resources/fonts/";
	public static final String CSSDIR = "resources/styles/";
	public static final String TXTDIR = "resources/text/";
	public static final String JSDIR = "resources/js/";
	public static final String MODULES = "modules";
	public static final String MODPATH = MODULES + File.separator;
	public static final String CACHEPATH = "cache" + File.separator;
	public static final String DATAPATH = "data" + File.separator;
	public static final String DBPATH = DATAPATH + "db" + File.separator;
	public static final String RULESPATH = DATAPATH + "rules" + File.separator;
	public static final String PICPATH = DATAPATH + "pic" + File.separator;
	public static final String EXFONTPATH = "fonts" + File.separator;
	public static final String DICT_CSS = CSSDIR + "dict.css";
	public static final String COMMON_JS = JSDIR + "viewer-common.js"; // used by all HtmlViewer
	public static final String TEXCONV = TXTDIR + "texconv.csv";
	public static final String FONTICON = "PaliPlatformIcons";
	public static final String FONTAWESOME = "Font Awesome 6 Free Solid";
	public static final String FONT_FALLBACK = "sans-serif";
	public static String FONTSERIF = FONT_FALLBACK;
	public static String FONTSANS = FONT_FALLBACK;
	public static String FONTMONO = FONT_FALLBACK;
	public static String FONTMONOBOLD = FONT_FALLBACK;
	public static String FONTMYAN = FONT_FALLBACK;
	public static final String PALI_ALL_CHARS = "ÑĀĪŊŚŪḌḤḶḸṀṂṄṆṚṜṢṬñāīŋśūḍḥḷḹṁṃṅṇṛṝṣṭēō";
	public static final String REX_NON_PALI = "[^A-Za-z" + PALI_ALL_CHARS + "]+";
	public static final String REX_NON_PALI_NUM = "[^A-Za-z0-9" + PALI_ALL_CHARS + "]+";
	public static final String REX_NON_PALI_PUNC = "[^A-Za-z" + PALI_ALL_CHARS + "?!–-]+";
	public static final String REX_NON_PALI_PUNC_FULL = "[^A-Za-z" + PALI_ALL_CHARS + "?!–-‖|.:;]+";
	public static final String PALI_NOUN_ENDINGS = "aāiīuū";
	public static final String PALI_VOWELS = "aāiīuūeo";
	public static final String PALI_LAHU_VOWELS = "aiu";
	public static final String PALI_LONG_VOWELS = "āīū";
	public static final String PALI_CONSONANTS = "kgṅcjñṭḍṇtdnpbmyrlvshḷ";
	public static final String WITH_H_CHARS = "bcdgjkptḍṭ";
	public static final String DASH_N = "–";
	public static final String DASH_M = "—";
	public static String csvDelimiter = CSVFormat.EXCEL.getDelimiterString();
	public static String csvRecordSeparator = CSVFormat.EXCEL.getRecordSeparator();
	public static final Map<PaliScript, Set<String>> paliFontMap = new EnumMap<>(PaliScript.class); 
	public static final Map<PaliTextInput.InputMethod, HashMap<String, String>> paliInputCharMap = new EnumMap<>(PaliTextInput.InputMethod.class);
	public static final Map<PpdpdTable, SimpleBooleanProperty> ppdpdAvailMap = new EnumMap<>(PpdpdTable.class);
	public static final Map<Character, List<String>> texConvMap = new HashMap<>();
	public static final Map<Character, String> meterPatternMap = new HashMap<>();
	// moved from main
	public static final HashSet<Stage> openedWindows = new HashSet<>();
	public static Stage mainStage;
	public static Properties settings;
	public static Properties urls;
	public static RuleBasedCollator paliCollator;
	public static Comparator<String> paliComparator;
	public static Comparator<String> alphanumComparator;
	public static StringConverter<Integer> integerStringConverter;
	public static ExecutorService threadPool;
	public static double defBaseFontSize;
	// enums
	public static enum PaliScript {
		UNKNOWN, ROMAN, DEVANAGARI, KHMER, MYANMAR, SINHALA, THAI;
		public static final PaliScript[] scripts = values();
		public String getName() {
			final String name = this.toString();
			return name.charAt(0) + name.substring(1).toLowerCase();
		}
	}
	public static enum Theme {
		LIGHT, DARK
	}
	public static enum WindowType {
		TOCTREE("TocTreeWin"), FINDER("DocumentFinder"), LUCENE("LuceneFinder"),
		LISTER("TermLister"), DICT("DictWin"), EDITOR("PaliTextEditor"),
		DECLENSION("DeclensionWin"), PROSODY("ProsodyWin"), READER("SentenceReader"),
		VIEWER("PaliHtmlViewer"), VIEWER_CSTR("CstrHtmlViewer"), VIEWER_CST4("Cst4HtmlViewer"),
		VIEWER_GRETIL("GretilHtmlViewer"), VIEWER_BJT("BjtHtmlViewer"), VIEWER_SRT("SrtHtmlViewer"), 
		VIEWER_GRAM("GramHtmlViewer"), VIEWER_SC("ScReader");
		public static final WindowType[] types = values();
		private final String windowClassName;
		private WindowType(final String className) {
			windowClassName = className;
		}
		public String getWindowClassName() {
			return windowClassName;
		}
		public static WindowType from(final String className) {
			WindowType result = null;
			for (final WindowType wt : WindowType.types) {
				if (className.endsWith(wt.windowClassName)) {
					result = wt;
					break;
				}
			}
			return result;
		}
	}
	public static enum H2DB {
		DICT("ppdict"), LISTER("pplister"), PPDPD("ppdpd");
		private final String name;
		private static final java.sql.Connection[] connection = new java.sql.Connection[3];
		private H2DB(final String name) {
			this.name = name;
		}
		public String getName() {
			return name;
		}
		public String getNameWithExt() {
			return name + ".mv.db";
		}
		public boolean isAvailable() {
			return H2DB.connection[this.ordinal()] != null;
		}
		public static void setConnection(final H2DB db, final java.sql.Connection conn) {
			H2DB.connection[db.ordinal()] = conn;
		}
		public java.sql.Connection getConnection() {
			return H2DB.connection[this.ordinal()];
		}
	}
	public static enum SQLiteDB {
		DPD("dpd");
		private final String name;
		private static final java.sql.Connection[] connection = new java.sql.Connection[1];
		private SQLiteDB(final String name) {
			this.name = name;
		}
		public String getName() {
			return name;
		}
		public String getNameWithExt() {
			return name + ".db";
		}
		public boolean isAvailable() {
			return SQLiteDB.connection[this.ordinal()] != null;
		}
		public static void setConnection(final SQLiteDB db, final java.sql.Connection conn) {
			SQLiteDB.connection[db.ordinal()] = conn;
		}
		public java.sql.Connection getConnection() {
			return SQLiteDB.connection[this.ordinal()];
		}
	}
	public static enum PpdpdTable {
		DICTIONARY, DECONSTRUCTOR, MINIDPD, SORTED_HEADWORDS;
		public static final PpdpdTable[] tables = values(); 
	}

	public static boolean isModulePresentByFile(final String modname) {
		final String root = ROOTDIR.isEmpty() ? "." : ROOTDIR;
		final ModuleFinder finder = ModuleFinder.of(Path.of(root + File.separator + MODPATH));
		final Optional<ModuleReference> omref = finder.find(modname);
		return omref.isPresent();
	}

	public static boolean isModulePresent(final String modname) {
		return Utilities.class.getModule().getLayer().findModule(modname).isPresent();
	}

	public static Module getModule(final String modname) {
		Module result = null;
		if (isModulePresent(modname)) {
			final ModuleLayer bootLayer = ModuleLayer.boot();
			final Optional<Module> mod = bootLayer.findModule(modname);
			if (mod.isPresent())
				result = mod.get();
		}
		return result;
	}

	public static String getDBDir() {
		final String dbroot = ROOTDIR.isEmpty() ? "./" : ROOTDIR;
		return dbroot + "data/db/";
	}

    public static void initializeDictDB() {
		initializeH2DB(H2DB.DICT);
	}

    public static void initializeListerDB() {
		initializeH2DB(H2DB.LISTER);
	}

    public static void initializePpdpdDB() {
		initializeH2DB(H2DB.PPDPD);
	}

    public static void initializeH2DB(final H2DB db, final boolean... force) {
		final java.sql.Connection conn = db.getConnection();
		if (conn == null || (force.length > 0 && force[0])) {
			H2DB.setConnection(db, initializeH2DBConnection(db));
		}
	}

    private static java.sql.Connection initializeH2DBConnection(final H2DB db) {
		java.sql.Connection result = null;
		final String dbdir = getDBDir();
		final Path dbpath = Path.of(dbdir);
		try {
			if (Files.notExists(dbpath))
				Files.createDirectories(dbpath);
			final String dburl = "jdbc:h2:" + dbdir + db.getName() + ";DB_CLOSE_ON_EXIT=FALSE";
			result = DriverManager.getConnection(dburl, "sa", "");
			result.setAutoCommit(true);
		} catch (IOException e) {
			System.err.println(e);
		} catch (SQLException e) {
			System.err.println(e);
			// this can protect the running of multiple instances
			if (e instanceof org.h2.jdbc.JdbcSQLNonTransientConnectionException)
				Platform.exit();
		}
		return result;
	}

	public static boolean isDBWritable(final H2DB db) {
		final String dbdir = getDBDir();
		final File dbfile = new File(dbdir + db.getNameWithExt());
		return dbfile.exists() && dbfile.canWrite();
	}

	public static void setDBWritable(final H2DB db, final boolean yn) {
		final String dbdir = getDBDir();
		final File dbfile = new File(dbdir + db.getNameWithExt());
		if (dbfile.exists()) {
			final java.sql.Connection conn = db.getConnection();
			try {
				if (conn != null)
					conn.close();
			} catch (SQLException e) {
				System.err.println(e);
			}
			dbfile.setWritable(yn);
			initializeH2DB(db, true);
		}
	}

    public static void initializeDpdDB() {
		final java.sql.Connection conn = SQLiteDB.DPD.getConnection();
		if (conn == null) {
			SQLiteDB.setConnection(SQLiteDB.DPD, initializeSQLiteDBConnection(SQLiteDB.DPD));
		}
	}

    private static java.sql.Connection initializeSQLiteDBConnection(final SQLiteDB db) {
		final File dbfile = new File(Utilities.ROOTDIR + Utilities.DBPATH + db.getNameWithExt());
		if (!dbfile.exists()) return null;
		java.sql.Connection result = null;
		final String dbdir = getDBDir();
		try {
			Class.forName("org.sqlite.JDBC");
			final String dburl = "jdbc:sqlite:" + dbdir + db.getNameWithExt();
			result = DriverManager.getConnection(dburl);
		} catch (ClassNotFoundException | SQLException e) {
			System.err.println(e);
		}
		return result;
	}

	public static void initializePpdpdAvailMap() {
		for (final PpdpdTable t : PpdpdTable.tables) {
			ppdpdAvailMap.put(t, new SimpleBooleanProperty(false));
		}
	}

	public static void updatePpdpdAvailibility() {
		final Set<String> tableNames = getFirstColumnFromDB(H2DB.PPDPD, "SHOW TABLES;");
		tableNames.forEach(d -> {
			final SimpleBooleanProperty prop = ppdpdAvailMap.get(PpdpdTable.valueOf(d.toUpperCase()));
			if (prop != null)
				prop.set(true);
		});
	}

	public static void setTheme(final Scene scn, final Utilities.Theme thm) {
		final String stylesheet = getCustomStyleSheet(thm);
		if (stylesheet.length() > 0) {
			scn.getStylesheets().clear();
			scn.getStylesheets().add(stylesheet);		
		}
	}
	
	public static String getCustomStyleSheet() {
		String style = "";
		switch (settings.getProperty("theme")) {
			case "LIGHT":
				style = Utilities.class.getResource(CSSDIR + "custom_light.css").toExternalForm();
				break;
			case "DARK":
				style = Utilities.class.getResource(CSSDIR + "custom_dark.css").toExternalForm();
				break;
		}
		return style;
	}
	
	static String getCustomStyleSheet(final Theme theme) {
		final String strTheme = theme.toString().toLowerCase();
		return Utilities.class.getResource(CSSDIR + "custom_"+strTheme+".css").toExternalForm();
	}

	/**
	 * Calculates the size relative to default base font size.
	 * By this, using absolute sizes can be avoided.
	 * The base size is adjusted automatically according to screen size.
	 */
	public static double getRelativeSize(final double scale) {
		return defBaseFontSize*scale;
	}

	public static void initializeFontMap() throws Exception {
		for (final PaliScript sc : PaliScript.scripts)
			paliFontMap.put(sc, new HashSet<String>());
		// load embedded fonts
		defBaseFontSize = Font.getDefault().getSize();
		Font.loadFont(Utilities.class.getResourceAsStream(FONTDIR + "PaliPlatformIcons.ttf"), 0); // PaliPlatformIcons
		Font.loadFont(Utilities.class.getResourceAsStream(FONTDIR + "fa-solid-900.ttf"), 0); // Font Awesome 6 Free Solid
		final Font fontSans = Font.loadFont(Utilities.class.getResourceAsStream(FONTDIR + "DejaVuSans.ttf"), 0);
		FONTSANS = fontSans==null ? FONT_FALLBACK : fontSans.getFamily();
		Font.loadFont(Utilities.class.getResourceAsStream(FONTDIR + "DejaVuSans-Bold.ttf"), 0);
		Font.loadFont(Utilities.class.getResourceAsStream(FONTDIR + "DejaVuSans-Oblique.ttf"), 0);
		final Font fontSerif = Font.loadFont(Utilities.class.getResourceAsStream(FONTDIR + "DejaVuSerif.ttf"), 0);
		FONTSERIF = fontSerif==null ? FONT_FALLBACK : fontSerif.getFamily();
		Font.loadFont(Utilities.class.getResourceAsStream(FONTDIR + "DejaVuSerif-Bold.ttf"), 0);
		Font.loadFont(Utilities.class.getResourceAsStream(FONTDIR + "DejaVuSerif-Italic.ttf"), 0);
		final Font fontMono = Font.loadFont(Utilities.class.getResourceAsStream(FONTDIR + "DejaVuSansMono.ttf"), 0);
		FONTMONO = fontMono==null ? FONT_FALLBACK : fontMono.getFamily();
		final Font fontMonoBold = Font.loadFont(Utilities.class.getResourceAsStream(FONTDIR + "DejaVuSansMono-Bold.ttf"), 0);
		FONTMONOBOLD = fontMonoBold==null ? FONT_FALLBACK : fontMonoBold.getFamily();
		Font.loadFont(Utilities.class.getResourceAsStream(FONTDIR + "DejaVuSansMono-Oblique.ttf"), 0);
		final Font fontMyan = Font.loadFont(Utilities.class.getResourceAsStream(FONTDIR + "PadaukPP-Regular.ttf"), 0);
		FONTMYAN = fontMyan==null ? FONT_FALLBACK : fontMyan.getFamily();
		Font.loadFont(Utilities.class.getResourceAsStream(FONTDIR + "PadaukPP-Bold.ttf"), 0);
		if (fontSans != null)
			paliFontMap.get(PaliScript.ROMAN).add(fontSans.getFamily());
		if (fontSerif != null)
			paliFontMap.get(PaliScript.ROMAN).add(fontSerif.getFamily());
		if (fontMono != null)
			paliFontMap.get(PaliScript.ROMAN).add(fontMono.getFamily());
		if (fontMyan != null)
			paliFontMap.get(PaliScript.MYANMAR).add(fontMyan.getFamily());
		// read external fonts
		loadExternalFonts();
	}
	
	/**
	 * Loads external fonts provided, used for non-Roman scripts.
	 */
	public static void loadExternalFonts() throws Exception {
		final File fontdir = new File(ROOTDIR + EXFONTPATH);
		if (fontdir.exists()) {
			final File[] files = fontdir.listFiles(f -> f.getName().toLowerCase().endsWith(".ttf"));
			for (final File f : files) {
				final java.awt.Font font = java.awt.Font.createFont(java.awt.Font.TRUETYPE_FONT, f);
				boolean doLoad = false;
				if (-1 == font.canDisplayUpTo(PALI_VOWELS + PALI_CONSONANTS)) {
					paliFontMap.get(PaliScript.ROMAN).add(font.getFamily());
					doLoad = true;
				}
				if (-1 == font.canDisplayUpTo(PaliCharTransformer.thaiConsonants, 0, PaliCharTransformer.thaiConsonants.length)) {
					paliFontMap.get(PaliScript.THAI).add(font.getFamily());
					doLoad = true;
				}
				if (-1 == font.canDisplayUpTo(PaliCharTransformer.khmerConsonants, 0, PaliCharTransformer.khmerConsonants.length)) {
					paliFontMap.get(PaliScript.KHMER).add(font.getFamily());
					doLoad = true;
				}
				if (-1 == font.canDisplayUpTo(PaliCharTransformer.myanmarConsonants, 0, PaliCharTransformer.myanmarConsonants.length)) {
					paliFontMap.get(PaliScript.MYANMAR).add(font.getFamily());
					doLoad = true;
				}
				if (-1 == font.canDisplayUpTo(PaliCharTransformer.sinhalaConsonants, 0, PaliCharTransformer.sinhalaConsonants.length)) {
					paliFontMap.get(PaliScript.SINHALA).add(font.getFamily());
					doLoad = true;
				}
				if (-1 == font.canDisplayUpTo(PaliCharTransformer.devaConsonants, 0, PaliCharTransformer.devaConsonants.length)) {
					paliFontMap.get(PaliScript.DEVANAGARI).add(font.getFamily());
					doLoad = true;
				}
				if (doLoad) {
					javafx.scene.text.Font.loadFont(new FileInputStream(new File(ROOTDIR + EXFONTPATH + f.getName())), 0);
				}
			}
		}
		// if nothing available, set to the fallback font
		for (final PaliScript sc : PaliScript.scripts) {
			if (paliFontMap.get(sc).isEmpty())
				paliFontMap.get(sc).add(FONT_FALLBACK);
		}
	}
	
	/**
	 * Prepares the Pali collator to sort Pali terms, including some Sanskrit characters
	 * there is some problem with ḷ (maybe using lh is better)
	*/
	public static void initializeComparator() throws Exception {
		if (paliCollator != null) return;
		final String vowel = "< √ < A, a < Ā, ā < I, i < Ī, ī < U, u < Ū, ū < E, e < Ē, ē < O, o < Ō, ō";	
		final String consonant = "< K, k < KH, Kh, kh < G, g < GH, Gh, gh < Ṅ, ṅ" +
								"< C, c < CH, Ch, ch < J, j < JH, Jh, jh < Ñ, ñ" +
								"< Ṭ, ṭ < ṬH, Ṭh, ṭh < Ḍ, ḍ < ḌH, Ḍh, ḍh < Ṇ, ṇ" +
								"< T, t < TH, Th, th < D, d < DH, Dh, dh < N, n" +
								"< P, p < PH, Ph, ph < B, b < BH, Bh, bh < M, m" +
								"< Y, y < R, r < Ṛ, ṛ < Ṝ, ṝ < L, l < Ḹ, ḹ < V, v" +
								"< Ś, ś < Ṣ, ṣ < S, s < H, h < Ḷ, ḷ";
		final String niggahita = "< Ṁ, ṁ < Ṃ, ṃ";
		final String paliRule = vowel + consonant + niggahita;
		paliCollator = new RuleBasedCollator(paliRule);
		paliComparator = paliCollator::compare;
		alphanumComparator = new Comparator<String>() {
			@Override
			public int compare(final String aName, final String bName) {
				// this compares xxx12 and xxx123 with numerical order
				// first separate letter and number part
				int ind = aName.length() - 1;
				while (Character.isDigit(aName.charAt(ind)))
					ind--;
				final String aNameLetter = aName.substring(0, ind + 1);
				final String aNameNumStr = aName.substring(ind + 1);
				final int aNameNum = aNameNumStr.isEmpty() ? 0 : Integer.parseInt(aNameNumStr);
				ind = bName.length() - 1;
				while (Character.isDigit(bName.charAt(ind)))
					ind--;
				final String bNameLetter = bName.substring(0, ind + 1);
				final String bNameNumStr = bName.substring(ind + 1);
				final int bNameNum = bNameNumStr.isEmpty() ? 0 : Integer.parseInt(bNameNumStr);
				int result = aNameLetter.compareTo(bNameLetter);
				if (result == 0) {
					return Integer.compare(aNameNum, bNameNum);
				} else {
					return result;
				}
			}
		};
	}

	/**
	 * Prepares StringConverter used in digit-only text input
	 */
	public static void initializeStringConverter() {
		if (integerStringConverter != null) return;
		integerStringConverter = new StringConverter<Integer>() {
			@Override
			public String toString(final Integer num) {
				if (num == null) return "";
				return num.toString();
			}
			@Override
			public Integer fromString(final String str) {
				if (str != null && str.matches("-?\\d+"))
					return Integer.valueOf(str);
				else
					return 0 ;
			}
		};
	}

	public static void displayAlert(final Alert.AlertType type, final String message) {
		final Alert alert = new Alert(type, message);
		alert.setHeaderText(null);
		Platform.runLater(() ->	alert.showAndWait());
	}

	public static boolean checkFileExistence(final File file) {
		boolean result = file.exists();
		if (!result)
			displayAlert(Alert.AlertType.ERROR, file.getName() + " not found, please install");
		return result;
	}

	public static Stage getOpenedWindow(final String className) {
		// find an existing closed window
		final Stage stg = openedWindows.stream()
										.filter(s -> s.getScene().getRoot().getClass().getName().endsWith(className))
										.filter(s -> !s.isShowing())
										.findFirst()
										.orElse(null);
		return stg;
	}
		
	public static Stage openNewWindow(final Parent p, final Image icon, final String title) {
		final Stage window = new Stage();
		window.getIcons().add(icon);
		window.setTitle(title);
		final Scene sc = new Scene(p);
		final String stylesheet = getCustomStyleSheet();
		if (stylesheet.length() > 0)
			sc.getStylesheets().add(stylesheet);		
		window.setScene(sc);
		openedWindows.add(window);
		window.show();
		return window;
	}
	
	public static void showExistingWindow(final Stage stg) {
		final Scene sc = stg.getScene();
		sc.getStylesheets().clear();
		final String stylesheet = getCustomStyleSheet();
		if (stylesheet.length() > 0)
			sc.getStylesheets().add(stylesheet);
		stg.show();		
	}

	public static String capitalize(final String str) {
		if (str.isEmpty()) return "";
		final char first = Character.toUpperCase(str.charAt(0));
		final String rest = str.length() > 1 ? str.substring(1).toLowerCase() : "";
		return first + rest;
	}

	public static String capitalizeFirstLetter(final String str, final boolean... alsoInQuote) {
		final StringBuilder result = new StringBuilder(str.length());
		final char[] chArr = str.toCharArray();
		boolean firstCapDone = false;
		boolean openBracketFound = false;
		for (int i = 0; i < chArr.length; i++) {
			openBracketFound = chArr[i] == ']' ? false : chArr[i] == '[' ? true : openBracketFound;
			if (openBracketFound) {
				// no change between [..]
				result.append(chArr[i]);
				continue;
			}
			if (firstCapDone) {
				if (alsoInQuote.length > 0 && alsoInQuote[0] && i > 1 && chArr[i-1] == '‘') {
					// captialize the first word in quotes, if enable
					result.append(Character.toUpperCase(chArr[i]));
				} else if (i > 2 && chArr[i-2] == '?') {
					// capitalize the word following a question mark
					result.append(Character.toUpperCase(chArr[i]));
				} else {
					result.append(chArr[i]);
				}
			} else {
				if (Character.isLetter(Character.codePointAt(chArr, i))) {
					result.append(Character.toUpperCase(chArr[i]));
					firstCapDone = true;
				} else {
					result.append(chArr[i]);
				}
			}
		}
		return result.toString();
	}

	/**
	 * Converts 3x3 string array to string.
	 * This funtion is used for debugging.
	 */
	public static String array3ToString(final String[][][] arr) {
		final StringBuilder result = new StringBuilder();
		for (final String[][] a : arr) {
			for (final String[] b : a) {
				for (final String s : b) {
					result.append(s).append(" ");
				}
				result.append(": ");
			}
			result.append("\n");
		}
		return result.toString();
	}

	/**
	 * Checks whether the array has all empty strings.
	 * This funtion is used mainly in conjugation table.
	 */
	public static boolean isArrayEmpty(final String[][] arr) {
		boolean result = true;
		for (final String[] a : arr) {
			for (final String s : a) {
				result = result && s.isEmpty();
			}
		}
		return result;
	}

	/**
	 * Sets up key-character mapping for Pali input.
	 */
	public static void setupPaliInputCharMap() {
		for (PaliTextInput.InputMethod method : PaliTextInput.InputMethod.values) {
			if (method != PaliTextInput.InputMethod.NORMAL) {
				final HashMap<String, String> inputMap = new HashMap<>();
				final Set<String> keySet = settings.stringPropertyNames();
				keySet.stream().filter(k -> k.startsWith(method.abbr+"-"))
								.forEach(k -> {
									final String paliChar = k.substring(3);
									if (paliChar.length()==1)
										inputMap.put(settings.getProperty(k), paliChar);
								});
				paliInputCharMap.put(method, inputMap);
			}
		}
	}
	
	/**
	 * Reads a file and determines its script.
	 */
	public static PaliScript getScriptLanguage(final File file) {
		final StringBuilder text = new StringBuilder();
		try (final Scanner in = new Scanner(new FileInputStream(file), StandardCharsets.UTF_8)) {
			while (in.hasNextLine() && text.length() < 100) {
				final String line = in.nextLine().trim();
				text.append(line);
			}
		} catch (FileNotFoundException e) {
			System.err.println(e);
		}
		return testLanguage(text.toString());
	}
	
	/**
	 * Determines the script language of a given text.
	 */
	public static PaliScript testLanguage(final String input) {
		final String text = input.trim();
		if (text.isEmpty())
			return PaliScript.ROMAN;
		final String specimen = text.length() > 100?text.substring(0, 100):text;
		final int totalLen = specimen.length();
		final PaliScript result;
		int romanCount = 0;
		int thaiCount = 0;
		int khmerCount = 0;
		int myanmarCount = 0;
		int sinhalaCount = 0;
		int devaCount = 0;
		final HashMap<Integer, PaliScript> hitCount = new HashMap<>();
		for (char ch : specimen.toCharArray()) {
			if (ch >= '\u0030' && ch <= '\u007A')
				romanCount++;
			if (ch >= '\u0900' && ch <= '\u097F')
				devaCount++;
			if (ch >= '\u0D80' && ch <= '\u0DFF')
				sinhalaCount++;
			if (ch >= '\u0E00' && ch <= '\u0E7F')
				thaiCount++;
			if (ch >= '\u1000' && ch <= '\u109F')
				myanmarCount++;
			if (ch >= '\u1780' && ch <= '\u17FF')
				khmerCount++;
		}
		hitCount.put(romanCount, PaliScript.ROMAN);
		hitCount.put(thaiCount, PaliScript.THAI);
		hitCount.put(khmerCount, PaliScript.KHMER);
		hitCount.put(myanmarCount, PaliScript.MYANMAR);
		hitCount.put(sinhalaCount, PaliScript.SINHALA);
		hitCount.put(devaCount, PaliScript.DEVANAGARI);
		final List<Integer> max = hitCount.keySet().stream().sorted((x, y)->Integer.compare(y, x)).limit(1).collect(Collectors.toList());
		final int maxScore = max != null && !max.isEmpty() ? max.get(0) : 0;
		if (maxScore > totalLen/2.0)
			result = hitCount.get(maxScore);
		else
			result = PaliScript.UNKNOWN;
		return result;
	}
	
	/**
	 * Replace Ŋ and ŋ with Ṃ and ṃ respectively.
	 * 
	 */
	public static String replaceOldNiggahitaWithNew(final String input) {
		final String output = input.replace("Ŋ", "Ṃ").replace("ŋ", "ṃ");
		return output;
	}
	
	/**
	 * Replace Ṃ and ṃ with Ŋ and ŋ respectively.
	 * 
	 */
	public static String replaceNewNiggahitaWithOld(final String input) {
		final String output = input.replace("Ṃ", "Ŋ").replace("ṃ", "ŋ");
		return output;
	}

	public static void removeObservableItems(final ObservableList<?> list, final List<Integer> selected) {
		// In case of ObservableList, we have to delete from back to front, so inversed sort is needed.
		final Integer[] inds = selected.stream().sorted((x,y) -> y-x).toArray(Integer[]::new);
		for (final Integer i : inds) {
			if (i >= 0)
				list.remove(i.intValue());
		}
	}
	
	public static String cleanXmlTags(final String text) {
		final String patt = "<\\??/?!?[a-zA-Z-](?:[^>\"\']|\"[^\"]*\"|\'[^\']*\'\\?)*>";
		String result = text.replaceAll(patt, " ");
		result = result.replaceAll("&nbsp;", " ").replaceAll("&amp;", "&").replaceAll("&gt;", ">").replaceAll("&lt;", "<")
						.replaceAll(" {2,}", " ").replace(" .", ".").replace(" ,", ",").trim();
		return result;
	}

	public static String getTextResource(final String fileNameWithPath) {
		String result = "";
		try {
			final InputStream in = Utilities.class.getResourceAsStream(fileNameWithPath);
			if (in != null)
				result = new String(in.readAllBytes(), StandardCharsets.UTF_8);
		} catch (IOException e) {
			System.err.println(e);
		}
		return result;
	}

	public static void copyText(final String text) {
		final Clipboard cboard = Clipboard.getSystemClipboard();
		final ClipboardContent content = new ClipboardContent();
		content.putString(text);
		cboard.setContent(content);
	}
	
	public static void copyCSV(final List<String[]> data) {
		final StringBuilder result = new StringBuilder();
		for (final String[] row : data) {
			for (final String text : row) {
				result.append(text).append(csvDelimiter);
			}
			result.append(csvRecordSeparator);
		}
		copyText(result.toString());
	}
	
	public static File selectDirectory(final String init) {
		return selectDirectory(init, mainStage);
	}

	public static File selectDirectory(final Window owner) {
		return selectDirectory(".", owner);
	}
	
	public static File selectDirectory(final String init, final Window owner) {
		return selectDirectory(init, owner, "Select a directory");
	}

	public static File selectDirectory(final String init, final Window owner, final String title) {
		final File initDir = new File(init);
		final DirectoryChooser dirChooser = new DirectoryChooser();
		dirChooser.setTitle(title);
		dirChooser.setInitialDirectory(initDir);
		return dirChooser.showDialog(owner);
	}
	
	public static File selectFile(final String ext, final String initPath, final Window owner) {
		final File initDir = new File(initPath);
		final FileChooser fileChooser = new FileChooser();
		fileChooser.setInitialDirectory(initDir);
		final String capExt = ext.toUpperCase();
		fileChooser.setTitle("Select a " + capExt + " file");
		fileChooser.getExtensionFilters().add(new ExtensionFilter(capExt + " Files", "*." + ext));
		return fileChooser.showOpenDialog(owner);
	}
	
	public static File selectTextFile(final Window owner) {
		final FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Select a file");
		fileChooser.getExtensionFilters().addAll(
			new ExtensionFilter("Text Files", "*.txt"),
			new ExtensionFilter("All Files", "*.*"));
		return fileChooser.showOpenDialog(owner);
	}
	
	public static List<File> selectMultipleTextFile(final Window owner) {
		final FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Select files");
		fileChooser.getExtensionFilters().addAll(
			new ExtensionFilter("Text Files", "*.txt"),
			new ExtensionFilter("All Files", "*.*"));
		return fileChooser.showOpenMultipleDialog(owner);
	}
	
	public static String getTextFileContent(final File theFile) {
		String content = "";
		try {
			content = Files.readString(theFile.toPath());
		} catch (IOException e) {
			System.err.println(e);
		}
		return content == null ? "" : content;
	}
	
	private static File getOutputFile(final String nameAndExt) {
		return getOutputFile(nameAndExt, ".", mainStage);
	}

	public static File getOutputFile(final String nameAndExt, final String initPath, final Window owner) {
		final File initDir = new File(initPath);
		final String[] filename = nameAndExt.split("\\.");
		final FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Select a file name");
		fileChooser.setInitialDirectory(initDir);
		fileChooser.setInitialFileName(nameAndExt);
		final String fileExt = filename[filename.length-1];
		final String fileEXT = fileExt.toUpperCase();
		final String fileDesc = fileExt.equals("TXT") ? "Text" : fileEXT;
		final ExtensionFilter extFilter = new ExtensionFilter(fileDesc + " Files", "*." + fileExt);
		fileChooser.getExtensionFilters().add(extFilter);
		return fileChooser.showSaveDialog(owner);
	}

	public static File saveList(final List<String> list, final String filename) {
		final File outfile = getOutputFile(filename);
		if (outfile != null) {
			final String text = list.stream().collect(Collectors.joining("\n"));
			saveText(text, outfile);
		}
		return outfile;
	}
	
	public static File saveText(final String text, final String filename) {
		return saveText(text, filename, mainStage);
	}
	
	public static File saveText(final String text, final String filename, final Window owner) {
		final File outfile = getOutputFile(filename, ".", owner);
		if (outfile != null) {
			saveText(text, outfile);
		}
		return outfile;
	}

	public static void saveText(final String text, final File file) {
		try (final BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {
			out.write(text, 0, text.length());
		} catch (IOException e) {
			System.err.println(e);
		}			
	}
	
	public static File saveCSV(final List<String[]> text, final String filename) {
		return saveCSV(text, filename, mainStage);
	}
	
	public static File saveCSV(final List<String[]> text, final String filename, final Window owner) {
		final File outfile = getOutputFile(filename, ".", owner);
		if (outfile != null) {
			saveCSV(text, outfile);
		}
		return outfile;
	}

	public static void saveCSV(final List<String[]> text, final File file) {
		try (final CSVPrinter printer = new CSVPrinter(new BufferedWriter(
								new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)),
								CSVFormat.EXCEL)){
			printer.printRecords(text);
		} catch (IOException e) {
			System.err.println(e);
		}			
	}
	
	public static void executeSQL(final Connection conn, final String sql) {
		try {
			if (conn != null)
				conn.createStatement().execute(sql);
		} catch (SQLException e) {
			System.err.println(e);
		}
	}
	
	public static void executeSQL(final Utilities.H2DB db, final String sql) {
		final java.sql.Connection conn = db.getConnection();
		executeSQL(conn, sql);
	}
	
	public static Set<String> getFirstColumnFromDB(final Connection conn, final String query) {
		final Set<String> result = new LinkedHashSet<>();
		try {
			if (conn != null) {
				final Statement stmt = conn.createStatement();
				final ResultSet rs = stmt.executeQuery(query);
				while (rs.next()) {
					result.add(rs.getString(1));
				}
				rs.close();		
				stmt.close();
			}
		} catch (SQLException e) {
			System.err.println(e);
		}
		return result;	
	}
	
	public static Set<String> getFirstColumnFromDB(final Utilities.H2DB db, final String query) {
		final java.sql.Connection conn = db.getConnection();
		return getFirstColumnFromDB(conn, query);
	}
	
	// count a character in a string
	public static int charCount(final String text, final char ch) {
		int result = 0;
		char[] arr = text.toCharArray();
		for (char c : arr)
			if (c == ch)
				result++;
		return result;
	}

	public static String getUsablePaliTerm(final String text) {
		String result = "";
		if (text.isEmpty())
			return result;
		final String[] tokens = text.split(REX_NON_PALI);
		result = Arrays.stream(tokens).filter(x -> x.length() > 0).findFirst().get().toLowerCase();
		return normalizeNiggahita(result);
	}
	
	public static String normalizeNiggahita(final String text, final boolean... allCases) {
		String result = text.replace("ṁ", "ṃ");
		if (allCases.length > 0 && allCases[0])
			result = result.replace("Ṁ", "Ṃ");
		return result;
	}

	public static String changeToScNiggahita(final String text, final boolean... allCases) {
		String result = text.replace("ṃ", "ṁ");
		if (allCases.length > 0 && allCases[0])
			result = result.replace("Ṃ", "Ṁ");
		return result;
	}

	public static int getFirstDigitPos(final String text) {
		int result = 0;
		for (int i=0; i<text.length(); i++) {
			if (Character.isDigit(text.charAt(i))) {
				result = i;
				break;
			}
		}
		return result;
	}

	public static String getFirstDigit(final String text) {
		final StringBuilder result = new StringBuilder();
		final char[] chars = text.trim().toCharArray();
		for (final char ch : chars) {
			if (Character.isDigit(ch))
				result.append(ch);
			else
				break;
		}
		return result.toString();
	}

	public static String removeLeadingZero(final String numStr) {
		int zNum = 0;
		for (final char ch : numStr.toCharArray()) {
			if (ch == '0')
				zNum++;
			else
				break;
		}
		return numStr.substring(zNum);
	}

	public static boolean isVowel(final char ch) {
		return (PALI_VOWELS.indexOf(Character.toLowerCase(ch)) >= 0);
	}

	public static boolean isConsonant(final char ch) {
		return (PALI_CONSONANTS.indexOf(Character.toLowerCase(ch)) >= 0);
	}

	public static void createMeterPatternMap() {
		if (!meterPatternMap.isEmpty())
			return;
		// mattāvutti set
		meterPatternMap.put('n', "llll");
		meterPatternMap.put('s', "llg");
		meterPatternMap.put('j', "lgl");
		meterPatternMap.put('b', "gll");
		meterPatternMap.put('m', "gg");
		// vaññavutti set
		meterPatternMap.put('N', "lll");
		meterPatternMap.put('S', "llg");
		meterPatternMap.put('J', "lgl");
		meterPatternMap.put('B', "gll");
		meterPatternMap.put('R', "glg");
		meterPatternMap.put('T', "ggl");
		meterPatternMap.put('M', "ggg");
		meterPatternMap.put('L', "llllllllllllll"); // 14 lahus
	}

	public static String changeToLahuGaru(final String text) {
		final StringBuilder result = new StringBuilder();
		for (final char ch : text.toCharArray()) {
			if (Character.isDigit(ch)) {
				if (ch == '1')
					result.append("l");
				else if (ch == '2')
					result.append("(g|ll)");
				else if (ch == '4')
					result.append("(llll|llg|lgl|gll|gg)");
			} else {
				if (meterPatternMap.containsKey(ch))
					result.append(meterPatternMap.get(ch));
				else
					result.append(ch);
			}
		}
		return result.toString();
	}

	public static int sumMeter(final String pattern) {
		int sum = 0;
		for (int i=0; i<pattern.length(); i++) {
			char ch = pattern.charAt(i);
			if (Character.isDigit(ch)) {
				sum += ch - '0';
			}
		}
		return sum;
	}

	public static String addComputedMeters(final String text) {
		final String[] paragraphs = text.split("\\n");
		final StringBuilder result = new StringBuilder();
		for (final String p : paragraphs) {
			if (!p.trim().isEmpty()) {
				final String[] tokens = p.split(REX_NON_PALI);
				for (final String s : tokens) {
					final String meters = computeMeter(s, true);
					if (!meters.isEmpty())
						result.append(s).append(" (").append(meters).append(") ");
				}
				result.append(System.getProperty("line.separator"));
			} else {
				result.append(System.getProperty("line.separator"));
			}
		}
		return result.toString();
	}
	
	public static String computeMeter(final String text, final boolean... useNumber) {
		final char[] munit = useNumber.length > 0 && useNumber[0] ? new char[] { '1', '2' } : new char[] { 'l', 'g' };
		final String input = text.contains("\n") ? text.toLowerCase().trim().split("\\n")[0] : text.toLowerCase().trim();
		final StringBuilder meterPattern = new StringBuilder();
		final char[] chars = input.toCharArray();
		// consider each character
		for (int i=0; i<chars.length; i++) {
			final char thisCh = chars[i];
			char meter = '0';
			if (PALI_VOWELS.indexOf(thisCh) >= 0) {
				// only consider when it is a vowel
				// check what follows
				if (i < chars.length-1) {
					if (i < chars.length-2) {
						if (chars[i+1] == 'ṃ' || chars[i+1] == 'ṁ') {
							// followed by a niggahita, garu is assured
							meter = munit[1];
						} else if (PALI_CONSONANTS.indexOf(chars[i+1]) >= 0 && PALI_CONSONANTS.indexOf(chars[i+2]) >= 0) {
							// followed by a double consonants, garu is assured, with some exceptions
							if (WITH_H_CHARS.indexOf(chars[i+1]) >= 0 && chars[i+2] == 'h') {
								if (PALI_LAHU_VOWELS.indexOf(thisCh) >= 0)
									meter = munit[0];
								else
									meter = munit[1];
							} else {
								meter = munit[1];
							}
						} else {
							if (PALI_LAHU_VOWELS.indexOf(thisCh) >= 0)
								meter = munit[0];
							else
								meter = munit[1];
						}
					} else {
						if (chars[i+1] == 'ṃ' || chars[i+1] == 'ṁ') {
							// followed by a niggahita, garu is assured
							meter = munit[1];
						} else {
							if (PALI_LAHU_VOWELS.indexOf(thisCh) >= 0)
								meter = munit[0];
							else
								meter = munit[1];
						}
					}
				} else {
					if (PALI_LAHU_VOWELS.indexOf(thisCh) >= 0)
						meter = munit[0];
					else
						meter = munit[1];
				}
				meterPattern.append(meter);
			}
		} // end for
		return meterPattern.toString();
	}

	public static int getPaliWordLength(final String word) {
		int hfound = 0;
		final char [] chars = word.toCharArray();
		for (int i=0; i <= chars.length - 2; i++){
			if (WITH_H_CHARS.indexOf(chars[i]) >= 0) {
				if (chars[i+1] == 'h') {
					hfound++;
					i++;
				}
			}
		}
		return chars.length - hfound;
	}

	public static char shortenVowel(final char vowel) {
		char result = vowel;
		if (PALI_LONG_VOWELS.indexOf(vowel) > -1) {
			if (vowel == 'ā')
				result = 'a';
			else if (vowel == 'ī')
				result = 'i';
			else if (vowel == 'ū')
				result = 'u';
		}
		return result;
	}

	public static void loadTexConv() {
		if (!texConvMap.isEmpty())
			return;
		try (final Scanner in = new Scanner(Utilities.class.getResourceAsStream(TEXCONV), StandardCharsets.UTF_8)) {
			while (in.hasNextLine()) {
				final String line = in.nextLine().trim();
				if (line.charAt(0) == '#')
					continue;
				final String[] chunks = line.split(":");
				final char ch = chunks[0].charAt(0);
				final String[] texRules = chunks[1].split(",");
				final List<String> ruleList = Arrays.asList(texRules);
				texConvMap.put(ch, ruleList);
			}
		}
	}

	/**
	 * Solves the problem of File.separator as delimiter in Windows platform.
	 */
	public static String[] safeSplit(final String input, final String delim) {
		final String safeDelim = delim.contains("\\") ? delim.replace("\\", "\\\\") : delim;
		return input.split(safeDelim);
	}

	public static String getLastPathPart(final File path) {
		if (path == null) return "";
		final String[] strTmp = safeSplit(path.getPath(), File.separator);
		return strTmp[strTmp.length - 1];
	}

	public static String getLastPathPart(final String path) {
		if (path == null || path.isEmpty()) return "";
		final String[] strTmp = safeSplit(path, File.separator);
		return strTmp[strTmp.length - 1];
	}

	/**
	 * Converts a number range as string to list.
	 */
	public static List<String> rangeToList(final String range) {
		final List<String> result = new ArrayList<>();
		final int dpos = range.indexOf("-");
		if (dpos > -1) {
			final int start = Integer.parseInt(range.substring(0, dpos));
			final int end = Integer.parseInt(range.substring(dpos + 1));
			for (int i = start; i <= end; i++)
				result.add("" + i);
		} else {
			result.add(range);
		}
		return result;
	}

	private static void unarchive(final ArchiveInputStream in, final File targetDir) {
		ArchiveEntry entry = null;
		try {
			while ((entry = in.getNextEntry ()) != null) {
				if (!in.canReadEntryData(entry)) {
					continue;
				}
				final File dest = new File(targetDir, entry.getName());
				if (entry.isDirectory()) {
					if (!dest.isDirectory() && !dest.mkdirs()) {
						throw new IOException("failed to create directory " + dest);
					}
				} else {
					final File parent = dest.getParentFile();
					if (!parent.isDirectory() && !parent.mkdirs()) {
						throw new IOException("failed to create directory " + parent);
					}
					try (final OutputStream out = new FileOutputStream(dest)) {
						IOUtils.copy(in, out);
					}
				}
			}
		} catch (IOException e) {
			System.err.println(e);
		}
	}

	public static void untar(final File source, final File targetDir) {
		try (final ArchiveInputStream in = new TarArchiveInputStream(new FileInputStream(source))) {
			unarchive(in, targetDir);
			source.delete();
		} catch (IOException e) {
			System.err.println(e);
		}
	}

	public static void unzip(final File source, final File targetDir) {
		try (final ArchiveInputStream in = new ZipArchiveInputStream(new FileInputStream(source))) {
			unarchive(in, targetDir);
		} catch (IOException e) {
			System.err.println(e);
		}
	}

}
