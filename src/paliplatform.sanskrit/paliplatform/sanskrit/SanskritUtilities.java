/*
 * SanskritUtilities.java
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

package paliplatform.sanskrit;

import paliplatform.base.*;
import paliplatform.base.ScriptTransliterator.EngineType;

import java.util.*;
import java.util.regex.*;
import java.util.stream.*;
import java.util.concurrent.*;
import java.util.function.Function;
import java.util.ServiceLoader.Provider;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.sql.*;

import javafx.stage.Stage;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Tooltip;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.SimpleObjectProperty;

/** 
 * The utility factory for the Sanskrit module.
 * @author J.R. Bhaddacak
 * @version 3.6
 * @since 3.5
 */
final public class SanskritUtilities {
	public static final String DICTPATH = Utilities.DATAPATH + "dict" + File.separator;
	private static final String TXTDIR = "resources/text/";
	public static final SimpleBooleanProperty sktDictDBAvailable = new SimpleBooleanProperty(false);
	public static final Map<SktDictBook, SimpleBooleanProperty> sktDictAvailMap = new EnumMap<>(SktDictBook.class);
	public static final SimpleBooleanProperty someSktDictDataAvailable = new SimpleBooleanProperty(false);
	public static final SimpleBooleanProperty sktDictDBLocked = new SimpleBooleanProperty(false);
	public static final SimpleObjectProperty<Node> sktDictDBLockIcon = new SimpleObjectProperty<>(null);
	private static final String[] dbLockStatus = { "Skt. Dict DB unlocked", "Skt. Dict DB locked" };
	public static final SimpleStringProperty sktDictDBLockString = new SimpleStringProperty(dbLockStatus[0]);
	public static Map<String, SimpleService> simpleServiceMap;
	public static enum SktDictBook {
		MW("Monier-Williams' Sanskrit-English Dictionary (1899)"), AP("Apte's Practical Sanskrit-English Dictionary (revised, 1957)"),
		SHS("Jīvānanda Vidyāsāgara Bhaṭṭācāryya's Śabda-sāgara (1900)"), MD("Macdonell's Sanskrit-English Dictionary (1893)"),
		BHS("Edgerton's Buddhist Hybrid Sanskrit Dictionary (1953)"), MWE("Monier-Williams's English-Sanskrit Dictionary (1851)"),
		AE("Apte's Student's English-Sanskrit Dictionary (1964)"), BOR("Borooah's English-Sanskrit Dictionary (1877-1881)");
		public static final SktDictBook[] books = values();
		private static final String[] dataFiles = { "mw_iast.txt", "ap.txt", "shs.txt", "md.txt", "bhs.txt", "mwe.txt", "ae.txt", "bor.txt" };
		public final String bookName;
		private SktDictBook(final String name) {
			bookName = name;
		}
		public String getDataFileName() {
			return dataFiles[this.ordinal()];
		}
		public boolean keySLP1DecodeNeeded() {
			return Set.of(AP, SHS, MD, BHS).contains(this);
		}
		public static boolean isValid(final String dic) {
			boolean result = false;
			for (final SktDictBook db : SktDictBook.books) {
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
			final InputStream in = SanskritUtilities.class.getResourceAsStream(TXTDIR + filename);
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

	public static void initializeSktDictAvailMap() {
		if (!sktDictAvailMap.isEmpty()) return;
		for (final SktDictBook d : SktDictBook.books) {
			sktDictAvailMap.put(d, new SimpleBooleanProperty(false));
		}
	}

	public static void updateSktDictAvailibility() {
		final boolean sktAvailable = Utilities.isDBPresent(Utilities.H2DB.SKTDICT);
		sktDictDBAvailable.set(sktAvailable);
		someSktDictDataAvailable.set(!getAvailableSktDictData().isEmpty());
		if (!sktAvailable) return;
		final Set<String> dictNames = Utilities.getFirstColumnFromDB(Utilities.H2DB.SKTDICT, "SHOW TABLES;");
		dictNames.forEach(d -> {
			final SimpleBooleanProperty prop = sktDictAvailMap.get(SktDictBook.valueOf(d));
			if (prop != null)
				prop.set(true);
		});
	}

	public static void openWindow(final Utilities.WindowType win, final Object[] args) {
		final Stage stg = Utilities.getOpenedWindow(win.getWindowClassName());
		switch (win) {
			case SKTDICT:
				if (stg == null) {
					Utilities.openNewWindow(new SktDictWin(args), 
						new Image(SanskritUtilities.class.getResourceAsStream("resources/images/skt-book.png")), "Sanskrit Dictionaries");
				} else {
					final SktDictWin sktDictWin = (SktDictWin)stg.getScene().getRoot();
					sktDictWin.init(args);
					Utilities.showExistingWindow(stg);
				}
				break;
		}
	}

	public static void updateSktDictDBLockStatus() {
		final boolean locked = !Utilities.isDBWritable(Utilities.H2DB.SKTDICT);
		final String status = locked ? dbLockStatus[1] : dbLockStatus[0];
		final Node icon = locked
							? new TextIcon("lock", TextIcon.IconSet.AWESOME)
							: new TextIcon("unlock", TextIcon.IconSet.AWESOME);
		sktDictDBLocked.set(locked);
		sktDictDBLockString.set(status);
		sktDictDBLockIcon.set(icon);
	}

	public static void lockSktDictDB(final boolean locked) {
		Utilities.setDBWritable(Utilities.H2DB.SKTDICT, !locked);
		updateSktDictDBLockStatus();
	}
	
	public static Set<SktDictBook> getAvailableSktDictData() {
		final Set<SktDictBook> result = EnumSet.noneOf(SktDictBook.class);
		for (final SktDictBook dic : SktDictBook.books) {
			final File dataFile = new File(SanskritUtilities.DICTPATH + dic.getDataFileName());
			if (dataFile.exists())
				result.add(dic);
		}
		return result;
	}

	public static boolean anySktDictTableExists() {
		if (!Utilities.H2DB.SKTDICT.isAvailable()) return false;
		boolean result = false;
		final Set<String> tableNames = Utilities.getFirstColumnFromDB(Utilities.H2DB.SKTDICT, "SHOW TABLES;");
		for (final String t : tableNames) {
			if (SktDictBook.isValid(t.toUpperCase())) {
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

	public static void createSktDictData() {
		final Set<SktDictBook> availDict = getAvailableSktDictData();
		if (availDict.isEmpty()) {
			Utilities.displayAlert(Alert.AlertType.ERROR, "No Sanskrit dict found, please download first");
			return;
		}
		Utilities.initializeSktDictDB(true);
		if (!Utilities.H2DB.SKTDICT.isAvailable()) return;
		final boolean proceed = anySktDictTableExists() ? proceedCreateConfirm() : true;
		if (!proceed) return;
		final Map<SktDictBook, CompletableFuture<Void>> taskMap = new EnumMap<>(SktDictBook.class);
		for (final SktDictBook dict : SktDictBook.books) {
			if (availDict.contains(dict)) {
				final Runnable work = () -> createSktDictTable(dict);
				taskMap.put(dict, CompletableFuture.runAsync(work, Utilities.threadPool));
			}
		}
		final String mess = "The preparation is going in background,\nplease wait a minute, don't quit!";
		Utilities.displayAlert(Alert.AlertType.INFORMATION, mess);
	}

	public static void createSktDictTable(final SktDictBook dict) {
		final File dataFile = new File(SanskritUtilities.DICTPATH + dict.getDataFileName());
		final Pattern entryPatt = Pattern.compile("<L>(.*?)<pc>(.*?)<k1>(.*?)<k2>(.*)");
		final Pattern homoNumPatt = Pattern.compile(".*<h>(.*)");
		final java.sql.Connection conn = Utilities.H2DB.SKTDICT.getConnection();
		final String tableName = dict.toString();
		final String delete = "DROP TABLE IF EXISTS " + tableName + ";";
		Utilities.executeSQL(conn, delete);
		final String create = "CREATE TABLE " + tableName + " (" +
			"ID INT PRIMARY KEY," +
			"LID VARCHAR(16) NOT NULL," +
			"PAGECOL VARCHAR(16) NOT NULL," +
			"KEY1 VARCHAR(255) NOT NULL," +
			"KEY2 VARCHAR(255) NOT NULL," +
			"HNUM VARCHAR(4)," +
			"MEANING CLOB);";
		Utilities.executeSQL(conn, create);
		final String insert = "INSERT INTO " + tableName + " VALUES (?, ?, ?, ?, ?, ?, ?);";
		int id = 0;
		String lid = "";
		String pagecol = "";
		String key1 = "";
		String key2 = "";
		String homoNum = "";
		StringBuilder meaning = null;
		try (final Scanner in = new Scanner(new FileInputStream(dataFile), StandardCharsets.UTF_8)) {
			final PreparedStatement pstm = conn.prepareStatement(insert);
			while (in.hasNextLine()) {
				final String line = in.nextLine().trim();
				if (line.startsWith("<L>")) {
					// record started
					final Matcher matcher = entryPatt.matcher(line);
					final Matcher homoMatcher = homoNumPatt.matcher(line);
					if (matcher.matches()) {
						id++;
						lid = matcher.group(1);
						pagecol = matcher.group(2);
						final String k1 = matcher.group(3);
						key1 = dict.keySLP1DecodeNeeded() 
								? ScriptTransliterator.translitQuick(k1, EngineType.SLP1_IAST, true)
								: k1;
						final String k2 = matcher.group(4);
						int apos = k2.indexOf("<");
						key2 = apos > -1 ? k2.substring(0, apos) : k2;
						if (homoMatcher.matches()) {
							final String hStr = homoMatcher.group(1);
							apos = hStr.indexOf("<");
							homoNum = apos > -1 ? hStr.substring(0, apos) : hStr;
						}
						meaning = new StringBuilder();
					}
				} else if (line.startsWith("<LEND>")) {
					// record ended
					if (!lid.isEmpty()) {
						pstm.setInt(1, id);
						pstm.setString(2, lid);
						pstm.setString(3, pagecol);
						pstm.setString(4, key1);
						pstm.setString(5, key2);
						pstm.setString(6, homoNum);
						String mStr = meaning.toString();
						if (dict == SktDictBook.BOR) {
							// broken hyphenation fix for BOR
							mStr = mStr.replace("- -", "");
						}
						pstm.setString(7, mStr);
						pstm.executeUpdate();
						lid = "";
						pagecol = "";
						key1 = "";
						key2 = "";
						homoNum = "";
						meaning = null;
					}
				} else {
					// meaning in between
					if (meaning != null)
						meaning.append(line).append(" ");
				}
			}
			pstm.close();
		} catch (FileNotFoundException e) {
			System.err.println(e);
		} catch (SQLException e) {
			System.err.println(e);
		}
		final String index = "CREATE INDEX IDX_" + tableName + "_KEY1 ON " + tableName + "(KEY1);";
		Utilities.executeSQL(conn, index);
		updateSktDictAvailibility();
	}

	public static CheckBox createSktDictCheckBox(final SktDictBook book) {
		final String name = book.toString();
		final CheckBox cb = new CheckBox(name);
		cb.setAllowIndeterminate(false);
		cb.setTooltip(new Tooltip(book.bookName));
		return cb;
	}

	public static List<SktDictEntry> lookUpSktDictFromDB(final SktDictBook dict, final String term) {
		final List<SktDictEntry> entryList = new ArrayList<>();
		final String query = "SELECT LID,PAGECOL,KEY1,KEY2,HNUM,MEANING FROM " + dict.toString() + " WHERE KEY1 = '" + term + "';";
		final java.sql.Connection conn = Utilities.H2DB.SKTDICT.getConnection();
		try {
			if (conn != null) {
				final Statement stmt = conn.createStatement();
				final ResultSet rs = stmt.executeQuery(query);
				while (rs.next()) {
					final SktDictEntry entry = new SktDictEntry(
							rs.getString("LID"), rs.getString("PAGECOL"),
							rs.getString("KEY1"), rs.getString("KEY2"), rs.getString("HNUM"));
					entry.setMeaning(rs.getString("MEANING"));
					entryList.add(entry);
				}
				rs.close();		
				stmt.close();
			}
		} catch (SQLException e) {
			System.err.println(e);
		}
		return entryList;	
	}

}

