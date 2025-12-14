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

import java.util.*;
import java.util.stream.*;
import java.util.function.Function;
import java.util.ServiceLoader.Provider;
import java.io.File;
import java.io.InputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.*;

import javafx.stage.Stage;
import javafx.scene.image.Image;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Tooltip;
import javafx.beans.property.SimpleBooleanProperty;

/** 
 * The utility factory for the Sanskrit module.
 * @author J.R. Bhaddacak
 * @version 3.5
 * @since 3.5
 */
final public class SanskritUtilities {
	public static final String DICTPATH = Utilities.DATAPATH + "dict" + File.separator;
	private static final String TXTDIR = "resources/text/";
	public static final SimpleBooleanProperty sktDictDBAvailable = new SimpleBooleanProperty(false);
	public static final Map<SktDictBook, SimpleBooleanProperty> sktDictAvailMap = new EnumMap<>(SktDictBook.class);
	public static Map<String, SimpleService> simpleServiceMap;
	public static enum SktDictBook {
		MWD("Monier-Williams' Sanskrit-English Dictionary"), APTE("V.S. Apte's Practical Sanskrit-English Dictionary (revised)");
		public static final SktDictBook[] books = values();
		private static final String[] dataFiles = { "mw_iast.txt", "ap.txt" };
		public final String bookName;
		private SktDictBook(final String name) {
			bookName = name;
		}
		public String getDataFileName() {
			return dataFiles[this.ordinal()];
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
		if (!sktAvailable) return;
		final Set<String> dictNames = Utilities.getFirstColumnFromDB(Utilities.H2DB.SKTDICT, "SHOW TABLES;");
		dictNames.forEach(d -> {
			final SimpleBooleanProperty prop = sktDictAvailMap.get(SktDictBook.valueOf(d));
			if (prop != null)
				prop.set(true);
		});
		Utilities.setDBWritable(Utilities.H2DB.SKTDICT, false);
	}

	public static void openWindow(final Utilities.WindowType win, final Object[] args) {
		final Stage stg = Utilities.getOpenedWindow(win.getWindowClassName());
		switch (win) {
			case SKTDICT:
				if (stg == null) {
					Utilities.openNewWindow(new SktDictWin(args), 
						new Image(SanskritUtilities.class.getResourceAsStream("resources/images/book.png")), "Sanskrit Dictionaries");
				} else {
					final SktDictWin sktDictWin = (SktDictWin)stg.getScene().getRoot();
					sktDictWin.init(args);
					Utilities.showExistingWindow(stg);
				}
				break;
		}
	}

	public static CheckBox createSktDictCheckBox(final SktDictBook book) {
		final String name = book.toString();
		final CheckBox cb = new CheckBox(name);
		cb.setAllowIndeterminate(false);
		cb.setTooltip(new Tooltip(book.bookName));
		return cb;
	}

	public static List<SktDictEntry> lookUpSktDictFromDB(final SktDictBook dic, final String term) {
		final List<SktDictEntry> entryList = new ArrayList<>();
		final String query = "SELECT LID,PAGECOL,KEY1,KEY2,HNUM,MEANING FROM " + dic.toString() + " WHERE KEY1 = '" + term + "';";
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

