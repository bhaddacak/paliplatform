/*
 * DictUtilities.java
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
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see https://www.gnu.org/licenses/.
 */

package paliplatform.dict;

import paliplatform.base.*;

import java.util.*;
import java.util.stream.*;
import java.util.concurrent.*;
import java.util.function.*;
import java.util.ServiceLoader.Provider;
import java.io.*;
import java.nio.file.Path;
import java.nio.file.Files;
import java.nio.charset.StandardCharsets;
import java.sql.*;

import javafx.stage.Stage;
import javafx.scene.image.Image;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Tooltip;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.SimpleObjectProperty;

import com.google.gson.Gson;

/** 
 * The utility factory for the Dict module.
 * @author J.R. Bhaddacak
 * @version 3.6
 * @since 3.0
 */
final public class DictUtilities {
	public static final String DICTPATH = Utilities.DATAPATH + "dict" + File.separator;
	public static final String TXTDIR = "resources/text/";
	public static final String CUSTOM_DICT = "customdict.txt";
	public static final String SANDHI_LIST = "sandhi.txt";
	public static final Map<DictBook, SimpleBooleanProperty> dictAvailMap = new EnumMap<>(DictBook.class);
	public static final SimpleBooleanProperty someDictAvailable = new SimpleBooleanProperty(false);
	public static final SimpleBooleanProperty dictDBLocked = new SimpleBooleanProperty(false);
	public static final SimpleObjectProperty<Node> dictDBLockIcon = new SimpleObjectProperty<>(null);
	private static final String[] dbLockStatus = { "Dict DB unlocked", "Dict DB locked" };
	public static final SimpleStringProperty dictDBLockString = new SimpleStringProperty(dbLockStatus[0]);
	public static Map<String, SimpleService> simpleServiceMap;
	public static final List<String> cpedTerms = new ArrayList<>();
	public static final Map<String, DictEntry> customDictMap = new HashMap<>();
	public static final Map<String, List<String>> sandhiListMap = new HashMap<>();
	private static final Gson gson = new Gson();
	public static File customDictFile;
	public static File sandhiFile;
	public static enum DictBook {
		CPED("Concise Pāli-English Dictionary"), CEPD("Concise English-Pāli Dictionary"),
		NCPED("New Concise Pāli-English Dictionary"), PTSD("PTS Pāli-English Dictionary"),
		DPPN("Dictionary of Pāli Proper Names"), MDPD("Mini Digital Pāḷi Dictionary"),
		CONE("Margaret Cone's Dictionary of Pāli");;
		public static final DictBook[] books = values();
		public final String bookName;
		public final String csvName;
		private DictBook(final String name) {
			bookName = name;
			csvName = this.toString().toLowerCase() + ".csv";
		}
		public boolean needCsvImport() {
			return this != MDPD;
		}
		public static boolean isValid(final String dic) {
			boolean result = false;
			for (final DictBook db : DictBook.books) {
				if (db.toString().equals(dic)) {
					result = true;
					break;
				}
			}
			return result;
		}
	}

	static String getTextResource(final String filename) {
		String result = "";
		try {
			final InputStream in = DictUtilities.class.getResourceAsStream("resources/text/" + filename);
			if (in != null)
				result = new String(in.readAllBytes(), StandardCharsets.UTF_8);
		} catch (IOException e) {
			System.err.println(e);
		}
		return result;
	}

	public static Map<String, SimpleService> getSimpleServices() {
		return ServiceLoader.load(SimpleService.class)
				.stream()
				.map(Provider::get)
				.collect(Collectors.toMap(x -> x.getClass().getName(), Function.identity()));
	}

	public static void loadCPEDTerms() {
		if (!cpedTerms.isEmpty()) return;
		if (!DictUtilities.dictAvailMap.get(DictUtilities.DictBook.CPED).get()) return;
		final String select = "SELECT TERM FROM CPED ORDER BY ID;";
		final Set<String> terms = Utilities.getFirstColumnFromDB(Utilities.H2DB.DICT, select);
		cpedTerms.addAll(terms);
	}

	public static void createCustomDictFile() {
		final File parent = DictUtilities.customDictFile.getParentFile();
		if (!parent.exists())
			parent.mkdirs();
		final String defDict = DictUtilities.getTextResource(DictUtilities.CUSTOM_DICT);
		Utilities.saveText(defDict, DictUtilities.customDictFile);
	}

	public static void loadCustomDict() {
		if (!DictUtilities.customDictMap.isEmpty()) return;
		// load from the custom dict file
		try (final Scanner in = new Scanner(new FileInputStream(DictUtilities.customDictFile), StandardCharsets.UTF_8)) {
			String[] line;
			while (in.hasNextLine()) {
				line = in.nextLine().split("=");
				final String term = line[0] == null ? "" : line[0].trim();
				if (term.isEmpty() || term.startsWith("#"))
					continue;
				String mean = "";
				String expl = "";
				if (line[1] != null) {
					final String[] detail = line[1].split(":");
					if (detail[0] != null)
						mean = detail[0];
					if (detail[1] != null)
						expl = detail[1];
				}
				customDictMap.put(term, new DictEntry(term, mean, expl));
			}
		} catch (FileNotFoundException e) {
			System.err.println(e);
		}
	}

	public static void updateCustomDict() {
		customDictMap.clear();
		loadCustomDict();
	}

	public static void createSandhiFile() {
		final File parent = DictUtilities.sandhiFile.getParentFile();
		if (!parent.exists())
			parent.mkdirs();
		final String defSandhi = DictUtilities.getTextResource(DictUtilities.SANDHI_LIST);
		Utilities.saveText(defSandhi, DictUtilities.sandhiFile);
	}

	public static void loadSandhiList() {
		if (!sandhiListMap.isEmpty()) return;
		// load from the sandhi rules file
		try (final Scanner in = new Scanner(new FileInputStream(sandhiFile), StandardCharsets.UTF_8)) {
			String[] line;
			while (in.hasNextLine()) {
				line = in.nextLine().split("=");
				final String term = line[0] == null ? "" : line[0].trim();
				if (term.isEmpty() || term.startsWith("#"))
					continue;
				final List<String> partList = new ArrayList<>();
				if (line[1] != null) {
					final String[] detail = line[1].split("\\+");
					for (final String p : detail)
						partList.add(p.trim());
					sandhiListMap.put(term, partList);
				}
			}
		} catch (FileNotFoundException e) {
			System.err.println(e);
		}
	}

	public static void updateSandhiList() {
		sandhiListMap.clear();
		loadSandhiList();
	}

	public static List<String> cutSandhi(final String term) {
		final List<String> result = new ArrayList<>();
		final boolean isCap = Character.isUpperCase(term.charAt(0));
		final String lower = term.toLowerCase();
		if (sandhiListMap.containsKey(lower)) {
			final List<String> sMap = sandhiListMap.get(lower);
			for (int k = 0; k < sMap.size(); k++) {
				final String part = sMap.get(k);
				final String output;
				if (k == 0 && isCap)
					output = Character.toUpperCase(part.charAt(0)) + part.substring(1);
				else
					output = part;
				result.add(output);
			}
		} else {
			result.add(term);
		}
		return result;
	}

	public static void openWindow(final Utilities.WindowType win, final Object[] args) {
		final Stage stg = Utilities.getOpenedWindow(win.getWindowClassName());
		switch (win) {
			case DICT:
				if (stg == null) {
					Utilities.openNewWindow(new DictWin(args), 
							new Image(DictUtilities.class.getResourceAsStream("resources/images/book.png")), "Pāli Dictionaries");
				} else {
					final DictWin dictWin = (DictWin)stg.getScene().getRoot();
					dictWin.init(args);
					Utilities.showExistingWindow(stg);
				}
				break;
		}
	}

	public static CheckBox createDictCheckBox(final DictBook book) {
		final String name = book.toString();
		final CheckBox cb = new CheckBox(name);
		cb.setAllowIndeterminate(false);
		cb.setTooltip(new Tooltip(book.bookName));
		return cb;
	}

	public static void initializeDictAvailMap() {
		if (!dictAvailMap.isEmpty()) return;
		for (final DictBook d : DictBook.books) {
			dictAvailMap.put(d, new SimpleBooleanProperty(false));
		}
	}

	public static void updateDictAvailibility() {
		final Set<String> dictNames = Utilities.getFirstColumnFromDB(Utilities.H2DB.DICT, "SHOW TABLES;");
		dictNames.forEach(d -> {
			final SimpleBooleanProperty prop = dictAvailMap.get(DictBook.valueOf(d));
			if (prop != null)
				prop.set(true);
		});
		dictAvailMap.get(DictBook.MDPD).bind(Utilities.ppdpdAvailMap.get(Utilities.PpdpdTable.MINIDPD));
		final boolean coneAvailable = Utilities.isDBPresent(Utilities.H2DB.CONE);
		if (Utilities.isDBWritable(Utilities.H2DB.CONE))
			Utilities.setDBWritable(Utilities.H2DB.CONE, false);
		dictAvailMap.get(DictBook.CONE).bind(new SimpleBooleanProperty(coneAvailable));
		someDictAvailable.set(dictAvailMap.values().stream().anyMatch(x -> x.get()));
	}

	private static String getDictCSV(final DictBook dic) {
		return  Utilities.ROOTDIR + DICTPATH + dic.csvName;
	}

	private static boolean isDictAvailable(final DictBook dic) {
		final String csvName = getDictCSV(dic);
		return Files.exists(Path.of(csvName));
	}

	private static Set<DictBook> getAvailableCSV() {
		final Set<DictBook> result = EnumSet.noneOf(DictBook.class);
		for (final DictBook dic : DictBook.books) {
			if (isDictAvailable(dic))
				result.add(dic);
		}
		return result;
	}

	public static boolean anyDictTableExists() {
		if (!Utilities.H2DB.DICT.isAvailable()) return false;
		boolean result = false;
		final Set<String> tableNames = Utilities.getFirstColumnFromDB(Utilities.H2DB.DICT, "SHOW TABLES;");
		for (final String t : tableNames) {
			if (DictBook.isValid(t.toUpperCase())) {
				result = true;
				break;
			}
		}
		return result;
	}

	private static boolean proceedCreateConfirm() {
		boolean output = false;
		final String message = "The existing dictionaries will be replaced, \nproceed to continue.";
		final ConfirmAlert proceedAlert = new ConfirmAlert(Utilities.mainStage, ConfirmAlert.ConfirmType.PROCEED, message);
		final Optional<ButtonType> result = proceedAlert.showAndWait();
		if (result.isPresent()) {
			if (result.get() == proceedAlert.getConfirmButtonType())
				output = true;
		}
		return output;		
	}

	public static void createDictData() {
		final Set<DictBook> availDict = getAvailableCSV();
		if (availDict.isEmpty()) {
			Utilities.displayAlert(Alert.AlertType.ERROR, "No CSV found, please install first");
			return;
		}
		Utilities.initializeDictDB();
		if (!Utilities.H2DB.DICT.isAvailable()) return;
		final boolean proceed = anyDictTableExists() ? proceedCreateConfirm() : true;
		if (!proceed) return;
		final Set<DictBook> failed = EnumSet.noneOf(DictBook.class);
		final Map<DictBook, CompletableFuture<Void>> taskMap = new EnumMap<>(DictBook.class);
		for (final DictBook dic : DictBook.books) {
			if (!dic.needCsvImport()) continue;
			if (availDict.contains(dic)) {
				Runnable work = null;
				switch (dic) {
					case NCPED:
						work = () -> createNCPEDTable();
						break;
					case CPED:
						work = () -> createCPEDTable();
						break;
					case CEPD:
					case DPPN:
					case PTSD:
						work = () -> createDictTable(dic);
						break;
				}
				if (work != null)
					taskMap.put(dic, CompletableFuture.runAsync(work, Utilities.threadPool));
			} else {
				if (dic != DictBook.CONE)
					failed.add(dic);
			}
		}
		if (failed.isEmpty()) {
			final String mess = "The creation is going in background,\nplease wait a moment";
			Utilities.displayAlert(Alert.AlertType.INFORMATION, mess);
		} else {
			final String nodata = failed.stream().map(x -> x.csvName).collect(Collectors.joining(", "));
			Utilities.displayAlert(Alert.AlertType.WARNING, nodata + " unavailable");
		}
	}

	public static void createDictTable(final DictBook dic) {
		final String dname = dic.toString();
		final String delete = "DROP TABLE IF EXISTS " + dname + ";";
		final String meaning = dic == DictBook.CEPD ? "MEANING VARCHAR" : "MEANING CLOB";
		final String create = "CREATE TABLE " + dname + "(" +
			"ID INT PRIMARY KEY," +
			"TERM VARCHAR(255) NOT NULL," +
			meaning + ");";
		final String insert = "INSERT INTO " + dname + " SELECT * FROM CSVREAD('" + getDictCSV(dic) + "');";
		final String index1 = "CREATE INDEX IDX_" + dname + "TERM ON " + dname + "(TERM);";
		Utilities.executeSQL(Utilities.H2DB.DICT, delete);
		Utilities.executeSQL(Utilities.H2DB.DICT, create);
		Utilities.executeSQL(Utilities.H2DB.DICT, insert);
		Utilities.executeSQL(Utilities.H2DB.DICT, index1);
		if (dic == DictBook.CEPD) {
			final String index2 = "CREATE INDEX IDX_CEPDMEANING ON CEPD(MEANING);";
			Utilities.executeSQL(Utilities.H2DB.DICT, index2);
		}
		updateDictAvailibility();
	}
	
	public static void createNCPEDTable() {
		final String delete = "DROP TABLE IF EXISTS NCPED;";
		final String create = "CREATE TABLE NCPED(" +
			"ID INT PRIMARY KEY," +
			"TERM VARCHAR(255) NOT NULL," +
			"GRAMMAR VARCHAR," +
			"DEFINITION VARCHAR," +
			"XR VARCHAR(255));";
		final String insert = "INSERT INTO NCPED SELECT * FROM CSVREAD('" + getDictCSV(DictBook.NCPED) + "');";
		final String index1 = "CREATE INDEX IDX_NCPEDTERM ON NCPED(TERM);";
		final String index2 = "CREATE INDEX IDX_NCPEDMEANING ON NCPED(DEFINITION);";
		Utilities.executeSQL(Utilities.H2DB.DICT, delete);
		Utilities.executeSQL(Utilities.H2DB.DICT, create);
		Utilities.executeSQL(Utilities.H2DB.DICT, insert);
		Utilities.executeSQL(Utilities.H2DB.DICT, index1);
		Utilities.executeSQL(Utilities.H2DB.DICT, index2);
		updateDictAvailibility();
	}
	
	public static void createCPEDTable() {
		final String delete = "DROP TABLE IF EXISTS CPED;";
		final String create = "CREATE TABLE CPED(" +
			"ID INT PRIMARY KEY," +
			"TERM VARCHAR(255) NOT NULL," +
			"POS VARCHAR(50)," +
			"PARADIGM VARCHAR(255)," +
			"IN_COMPOUNDS BOOLEAN," +
			"MEANING VARCHAR," +
			"SUBMEANING VARCHAR);";
		final String insert = "INSERT INTO CPED SELECT * FROM CSVREAD('" + getDictCSV(DictBook.CPED) + "');";
		final String index1 = "CREATE INDEX IDX_CPEDTERM ON CPED(TERM);";
		final String index2 = "CREATE INDEX IDX_CPEDMEANING ON CPED(MEANING);";
		Utilities.executeSQL(Utilities.H2DB.DICT, delete);
		Utilities.executeSQL(Utilities.H2DB.DICT, create);
		Utilities.executeSQL(Utilities.H2DB.DICT, insert);
		Utilities.executeSQL(Utilities.H2DB.DICT, index1);
		Utilities.executeSQL(Utilities.H2DB.DICT, index2);
		updateDictAvailibility();
	}
	
	public static void updateDictDBLockStatus() {
		final boolean locked = !Utilities.isDBWritable(Utilities.H2DB.DICT);
		final String status = locked ? dbLockStatus[1] : dbLockStatus[0];
		final Node icon = locked
							? new TextIcon("lock", TextIcon.IconSet.AWESOME)
							: new TextIcon("unlock", TextIcon.IconSet.AWESOME);
		dictDBLocked.set(locked);
		dictDBLockString.set(status);
		dictDBLockIcon.set(icon);
	}

	public static void lockDictDB(final boolean locked) {
		Utilities.setDBWritable(Utilities.H2DB.DICT, !locked);
		updateDictDBLockStatus();
	}
	
	public static PaliWord lookUpCPEDFromDB(final String term) {
		final PaliWord pword = new PaliWord(term);
		final String query = "SELECT POS,PARADIGM,IN_COMPOUNDS,MEANING,SUBMEANING FROM CPED WHERE TERM = '"+term+"';";
		final java.sql.Connection conn = Utilities.H2DB.DICT.getConnection();
		try {
			if (conn != null) {
				final Statement stmt = conn.createStatement();
				final ResultSet rs = stmt.executeQuery(query);
				while (rs.next()) {
					final String para = rs.getString("PARADIGM");
					final String pos = rs.getString("POS");
					if (para == null) {
						if (isGenericParadigmNeeded(pos))
							pword.setParadigm("generic");
					} else {
						pword.setParadigm(para);
					}
					pword.addPosInfo(pos);
					final boolean forCompounds = rs.getBoolean("IN_COMPOUNDS");
					pword.addForCompounds(forCompounds);
					final String meaning = rs.getString("MEANING");
					pword.addMeaning(meaning);
					final String submean = rs.getString("SUBMEANING");
					pword.addSubmeaning(submean);
				}
				rs.close();		
				stmt.close();
			}
		} catch (SQLException e) {
			System.err.println(e);
		}
		// dealing with special cases
		final List<String> prdm = pword.getParadigm();
		if (prdm.size() == 1 && prdm.get(0).equals("generic")) {
			final String tm = pword.getTerm();
			if (tm.endsWith("ant") && pword.isAdjective()) {
				pword.clearParadigm();
				pword.setParadigm("guṇavant,himavant,antcommon");
			} else if (tm.endsWith("ar")) {
				pword.clearParadigm();
				pword.setParadigm("kattu");
			}
		}
		return pword;	
	}

	public static NcpedWord lookUpNCPEDFromDB(final String term) {
		final NcpedWord nword = new NcpedWord(term);
		final String query = "SELECT GRAMMAR,DEFINITION,XR FROM NCPED WHERE TERM = '" + term + "';";
		final java.sql.Connection conn = Utilities.H2DB.DICT.getConnection();
		try {
			if (conn != null) {
				final Statement stmt = conn.createStatement();
				final ResultSet rs = stmt.executeQuery(query);
				while (rs.next()) {
					final String g = rs.getString(1) == null ? "" : rs.getString(1).trim();
					final String d = rs.getString(2) == null ? "" : rs.getString(2).trim();
					final String x = rs.getString(3) == null ? "" : rs.getString(3).trim();
					nword.addMeaning(g, d, x);
				}
				rs.close();		
				stmt.close();
			}
		} catch (SQLException e) {
			System.err.println(e);
		}
		return nword;	
	}
	
	public static List<String> lookUpDictFromDB(final DictBook dic, final String term) {
		final List<String> meanings = new ArrayList<>();
		final String query = "SELECT MEANING FROM " + dic.toString() + " WHERE TERM = '" + term + "';";
		final java.sql.Connection conn = Utilities.H2DB.DICT.getConnection();
		try {
			if (conn != null) {
				final Statement stmt = conn.createStatement();
				final ResultSet rs = stmt.executeQuery(query);
				while (rs.next()) {
					meanings.add(rs.getString(1));
				}
				rs.close();		
				stmt.close();
			}
		} catch (SQLException e) {
			System.err.println(e);
		}
		return meanings;	
	}

	public static List<String> lookUpConeFromDB(final String term) {
		final List<String> meanings = new ArrayList<>();
		final String query = "SELECT MEANING FROM DICT WHERE TERM = '|" + term + "';";
		final java.sql.Connection conn = Utilities.H2DB.CONE.getConnection();
		try {
			if (conn != null) {
				final Statement stmt = conn.createStatement();
				final ResultSet rs = stmt.executeQuery(query);
				while (rs.next()) {
					meanings.add(rs.getString(1));
				}
				rs.close();		
				stmt.close();
			}
		} catch (SQLException e) {
			System.err.println(e);
		}
		return meanings;	
	}

	private static DpdHeadWordBase createDpdHeadWord(final ResultSet rs) throws SQLException {
		final DpdHeadWordBase hw = new DpdHeadWordBase(rs.getString("LEMMA_1"));
		hw.setGrammar(rs.getString("GRAMMAR"));
		hw.setVerb(rs.getString("VERB"));
		hw.setTrans(rs.getString("TRANS"));
		hw.setPlusCase(rs.getString("PLUS_CASE"));
		hw.setMeaning1(rs.getString("MEANING_1"));
		hw.setMeaning2(rs.getString("MEANING_2"));
		hw.setMeaningLit(rs.getString("MEANING_LIT"));
		hw.setSanskrit(rs.getString("SANSKRIT"));
		hw.setRootKey(rs.getString("ROOT_KEY"));
		hw.setConstruction(rs.getString("CONSTRUCTION"));
		return hw;
	}

	public static List<DpdHeadWordBase> lookUpMDPDFromDBWithTerm(final String term) {
		final List<DpdHeadWordBase> result = new ArrayList<>();
		final String selectIds = "SELECT HEADWORDS FROM " + Utilities.PpdpdTable.DICTIONARY.toString() + " WHERE TERM = '" + term + "';";
		final java.sql.Connection conn = Utilities.H2DB.PPDPD.getConnection();
		try {
			if (conn != null) {
				final Statement idStmt = conn.createStatement();
				final ResultSet idRs = idStmt.executeQuery(selectIds);
				if (idRs.next()) {
					final int[] termIds = gson.fromJson(idRs.getString(1), int[].class);
					for (final int id : termIds) {
						final String selectHW = "SELECT * FROM " + Utilities.PpdpdTable.MINIDPD.toString() + " WHERE ID = '" + id + "';";
						final Statement hwStmt = conn.createStatement();
						final ResultSet hwRs = hwStmt.executeQuery(selectHW);
						while (hwRs.next()) {
							result.add(createDpdHeadWord(hwRs));
						}
						hwRs.close();		
						hwStmt.close();
					}
				}
				idRs.close();		
				idStmt.close();
			}
		} catch (SQLException e) {
			System.err.println(e);
		}
		return result;	
	}

	public static List<DpdHeadWordBase> lookUpMDPDFromDBWithLemma(final String term) {
		final List<DpdHeadWordBase> result = new ArrayList<>();
		final String select = "SELECT * FROM " + Utilities.PpdpdTable.MINIDPD.toString() + " WHERE LEMMA_1 = '" + term + "';";
		final java.sql.Connection conn = Utilities.H2DB.PPDPD.getConnection();
		try {
			if (conn != null) {
				final Statement stmt = conn.createStatement();
				final ResultSet rs = stmt.executeQuery(select);
				while (rs.next()) {
					result.add(createDpdHeadWord(rs));
				}
				rs.close();		
				stmt.close();
			}
		} catch (SQLException e) {
			System.err.println(e);
		}
		return result;	
	}

	public static String makeDpdProper(final String word) {
		String result = word;
		if (word.endsWith("n")) {
			result = word.substring(0, word.length() - 1) + "ṃ";
		}
		return result;
	}

	public static List<String> getMeaningFromDPD(final String term) {
		final List<String> result = new ArrayList<>();
		final String dictTab = Utilities.PpdpdTable.DICTIONARY.toString();
		final String mdpdTab = Utilities.PpdpdTable.MINIDPD.toString();
		final String idSelect = "SELECT HEADWORDS FROM " + dictTab + " WHERE TERM = '" + term + "';";
		final java.sql.Connection conn = Utilities.H2DB.PPDPD.getConnection();
		try {
			if (conn != null) {
				final Statement idStmt = conn.createStatement();
				final ResultSet idRs = idStmt.executeQuery(idSelect);
				if (idRs.next()) {
					final int[] termIds = gson.fromJson(idRs.getString(1), int[].class);
					if (termIds.length > 0) {
						String where = " WHERE ID = " + termIds[0];
						for (int i = 1; i < termIds.length; i++) {
							where = where + " OR ID = " + termIds[i];
						}
						final String mSelect = "SELECT MEANING_1,MEANING_2 FROM " + mdpdTab + where + ";";
						final Statement mStmt = conn.createStatement();
						final ResultSet mRs = mStmt.executeQuery(mSelect);
						while (mRs.next()) {
							final String mean1 = mRs.getString("MEANING_1");
							final String mean2 = mRs.getString("MEANING_2");
							if (DpdHeadWordBase.hasData(mean1))
								result.add("• " + mean1);
							else if (DpdHeadWordBase.hasData(mean2))
								result.add("‣ " + mean2);
						}
						mRs.close();		
						mStmt.close();
					}
				}
				idRs.close();		
				idStmt.close();
			}
		} catch (SQLException e) {
			System.err.println(e);
		}
		return result;	
	}

	public static List<String> getDeconFromDPD(final String term) {
		final List<String> result = new ArrayList<>();
		final String deconTab = Utilities.PpdpdTable.DECONSTRUCTOR.toString();
		final String select = "SELECT DECON FROM " + deconTab + " WHERE TERM = '" + term + "';";
		final java.sql.Connection conn = Utilities.H2DB.PPDPD.getConnection();
		try {
			if (conn != null) {
				final Statement stmt = conn.createStatement();
				final ResultSet rs = stmt.executeQuery(select);
				if (rs.next()) {
					final String[] arr = gson.fromJson(rs.getString(1), String[].class);
					if (arr.length > 0) {
						for (final String dc : arr)
							result.add(dc);
					}
				}
				rs.close();		
				stmt.close();
			}
		} catch (SQLException e) {
			System.err.println(e);
		}
		return result;	
	}

	private static boolean isGenericParadigmNeeded(final String pos) {
		boolean result = false;
		if (pos == null)
			return result;
		result = result || pos.equals("3");
		result = result || pos.equals("n.");
		result = result || pos.contains("m.");
		result = result || pos.contains("f.");
		result = result || pos.contains("nt.");
		result = result || pos.contains("adj.");
		return result;
	}
	
	public static String formatCPEDMeaning(final PaliWord pword, final boolean withTag) {
		final StringBuilder text = new StringBuilder();
		final List<String> posInfo = pword.getPosInfo();
		final List<String> meaning = pword.getMeaning();
		final List<String> submeaning = pword.getSubmeaning();
		final List<Boolean> forCompounds = pword.getForCompounds();
		String inCpds;
		String pos;
		String mean;
		String submean;
		final int all = pword.getRecordCount() - 1;
		for (int i = 0; i <= all; i++) {
			pos = "";
			if (posInfo != null && posInfo.size() > i)
				pos = posInfo.get(i)==null ? "" : "("+posInfo.get(i)+") ";
			if (pos.contains("+") && !pos.contains("of"))
				pos = "(v.)" + pos;
			inCpds = "";
			if (forCompounds.size() > i)
				inCpds = forCompounds.get(i) ? "[In Compounds] " : "";
			mean = "";
			if (meaning.size() > i)
				mean = meaning.get(i)==null ? "" : meaning.get(i);
			submean = "";
			if (submeaning.size() > i)
				submean = submeaning.get(i)==null ? "" :
					withTag ? "<div>"+submeaning.get(i)+"</div>" : submeaning.get(i);
			if (withTag)
				text.append("<p>"+pos+inCpds+mean+submean+"</p>");
			else
				text.append(pos+inCpds+mean+"\n"+submean);
		}
		return text.toString();
	}
	
	public static String formatNCPEDMeaning(final NcpedWord nword) {
		final StringBuilder text = new StringBuilder();
		for (int i = 0; i < nword.getHomoCount(); i++) {
			text.append("<p>");
			final String gram = nword.getGrammar(i);
			if (!gram.isEmpty())
				text.append("<div style='font-size:0.8em;padding-bottom:5px;'>"+gram+"</div>");
			final List<String> def = nword.getDefinition(i);
			if (!def.isEmpty()) {
				def.forEach(d -> text.append("<div>• "+d+"</div>"));
			}
			final List<String> xr = nword.getXR(i);
			if (!xr.isEmpty()) {
				String xrStr = "";
				for (int x = 0; x < xr.size(); x++)
					xrStr += "<a style='cursor:pointer' onClick=openNewDict('"+xr.get(x)+"')><em>" + xr.get(x) + "</em></a>, ";
				xrStr = xrStr.substring(0, xrStr.length()-2);
				text.append("<div style='font-size:0.9em;padding-top:5px;'>See also: "+xrStr+"</div>");
			}
			text.append("</p>");
		}
		return text.toString();
	}

	public static String formatMDPDMeaning(final List<DpdHeadWordBase> hwList) {
		final StringBuilder text = new StringBuilder();
		for (final DpdHeadWordBase hw : hwList) {
			text.append("<p>");
			text.append("<div style='font-size:1.2em;font-weight:bold;'>" + hw.getTerm() + "</div>");
			final String grammar = hw.getGrammar();
			final String verb = hw.getVerb();
			final String trans = hw.getTrans();
			final String plusCase = hw.getPlusCase();
			final List<String> gramList = List.of(grammar, verb, trans, plusCase);
			final String gramStr = gramList.stream().filter(x -> DpdHeadWordBase.hasData(x))
											.collect(Collectors.joining("; "));
			if (!gramStr.isEmpty())
 				text.append("<div style='font-size:0.8em;padding-bottom:5px;'>" + gramStr + "</div>");
			final String meaning1 = hw.getMeaning1();
			if (DpdHeadWordBase.hasData(meaning1))
				text.append("<div>• " + meaning1 + "</div>");
			final String meaning2 = hw.getMeaning2();
			if (DpdHeadWordBase.hasData(meaning2))
				text.append("<div>‣ " + meaning2 + "</div>");
			final String meaningLit = hw.getMeaningLit();
			if (DpdHeadWordBase.hasData(meaningLit))
				text.append("<div><strong>Lit.: </strong>" + meaningLit + "</div>");
			final String sanskrit = hw.getSanskrit();
			if (DpdHeadWordBase.hasData(sanskrit))
				text.append("<div><strong>Skt.: </strong>" + sanskrit + "</div>");
			final String rootKey = hw.getRootKey();
			if (DpdHeadWordBase.hasData(rootKey))
				text.append("<div><strong>Root: </strong>" + rootKey + "</div>");
			final String construction = hw.getConstruction();
			if (DpdHeadWordBase.hasData(construction))
				text.append("<div><strong>Con.: </strong>" + construction + "</div>");
			text.append("</p>");
		}
		return text.toString();
	}

	public static String replaceTermEnding(final String term) {
		final char end = term.charAt(term.length() - 1);
		final char replacement = Set.of('ā', 'e', 'o').contains(end)
									? 'a'
									: end == 'ī'
										? 'i'
										: end == 'ū' ? 'u' : end;
		return term.substring(0, term.length() - 1) + replacement;
	}

	public static PaliWord getFirstCPEDWord(final Set<String> terms) {
		final List<String> rlist = new ArrayList<>(terms);
		rlist.sort(Utilities.paliComparator);
		return lookUpCPEDFromDB(rlist.get(0));
	}

}

