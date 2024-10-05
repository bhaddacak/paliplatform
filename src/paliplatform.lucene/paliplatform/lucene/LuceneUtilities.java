/*
 * LuceneUtilities.java
 *
 * Copyright (C) 2023-2024 J. R. Bhaddacak 
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

package paliplatform.lucene;

import paliplatform.base.*;

import java.util.*;
import java.util.stream.*;
import java.util.function.Function;
import java.util.ServiceLoader.Provider;
import java.io.*;
import java.nio.file.Path;
import java.nio.file.Files;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.text.RuleBasedCollator;
import java.text.ParseException;

import javafx.stage.Stage;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.SimpleObjectProperty;

/** 
 * The utility factory for the Lucene module.
 * @author J.R. Bhaddacak
 * @version 3.0
 * @since 3.0
 */
final public class LuceneUtilities {
	public static final String STOPWORDS = "stopwords.txt";
	public static final String INDEXPATH = Utilities.DATAPATH + "index" + File.separator;
	public static final String INDEXROOT = Utilities.ROOTDIR + LuceneUtilities.INDEXPATH;
	public static final String TEXTPATH = Utilities.DATAPATH + "text" + File.separator;
	public static final String TXTDIR = "resources/text/";
	public static final Map<String, Integer> predefTextGroup = Map.of("all", 1, "noext", 2, "vdmsa", 3, "dmsa", 4);
	public static final SimpleBooleanProperty listerDBLocked = new SimpleBooleanProperty(false);
	public static final SimpleObjectProperty<Node> listerDBLockIcon = new SimpleObjectProperty<>(null);
	private static final String[] dbLockStatus = { "Lister DB unlocked", "Lister DB locked" };
	public static final SimpleStringProperty listerDBLockString = new SimpleStringProperty(dbLockStatus[0]);
	public static Map<String, SimpleService> simpleServiceMap;
	public static final Map<String, LuceneIndex> indexMap = new HashMap<>();
	public static final Map<String, ListerTable> listerTableMap = new HashMap<>();
	public static final Set<String> stopwords = new HashSet<>();
	public static RuleBasedCollator textGroupCollator;
	public static Comparator<String> predefTextGroupComparator;
	public static Comparator<String> indexNameComparator;
	public static Comparator<String> listerTableNameComparator;
	public static File stopwordsFile; // set in LuceneMenu

	static String getStringResource(final String fileNameWithPath) {
		String result = "";
		try {
			final InputStream in = LuceneUtilities.class.getResourceAsStream(fileNameWithPath);
			if (in != null)
				result = new String(in.readAllBytes(), StandardCharsets.UTF_8);
		} catch (IOException e) {
			System.err.println(e);
		}
		return result;
	}
    
	static String getTextResource(final String filename) {
		return getStringResource(TXTDIR + filename);
	}

	public static Map<String, SimpleService> getSimpleServices() {
		return ServiceLoader.load(SimpleService.class)
				.stream()
				.map(Provider::get)
				.collect(Collectors.toMap(x -> x.getClass().getName(), Function.identity()));
	}

	public static void initializeComparator() {
		final String textGroupRule = "< V,v < S,s < M,m < A,a < T,t < E,e < N,n < B,b < O,o";
		try {
			textGroupCollator = new RuleBasedCollator(textGroupRule);
			predefTextGroupComparator = new Comparator<String>() {
				@Override
				public int compare(final String aName, final String bName) {
					return Integer.compare(predefTextGroup.getOrDefault(aName, 10),
											predefTextGroup.getOrDefault(bName, 10));
				}
			};
			indexNameComparator = LuceneIndex.getIndexNameComparator(false);
			listerTableNameComparator = LuceneIndex.getIndexNameComparator(true);
		} catch (ParseException e) {
			System.err.println(e);
		}
	}

	public static void updateIndexList() {
		indexMap.clear();
		final File indexRoot = new File(INDEXROOT);
		final File[] dirs = indexRoot.listFiles(f -> f.isDirectory());
		for (final File d : dirs) {
			final String[] indices = d.list((f, s) -> { return s.startsWith("segments"); });
			if (indices.length > 0) {
				final String dirName = d.getName();
				indexMap.put(dirName, new LuceneIndex(dirName));
			}
		}
	}

	public static List<String> getIndexNameList() {
		final List<String> result = indexMap.keySet().stream().sorted(indexNameComparator).collect(Collectors.toList());
		return result;
	}

	private static void updateListerTableList() {
		listerTableMap.clear();
		final Set<String> nameList = Utilities.getFirstColumnFromDB(Utilities.H2DB.LISTER, "SHOW TABLES;");
		for (final String t : nameList) {
			listerTableMap.put(t, new ListerTable(t));
		}
	}

	public static List<String> getListerTableNameList() {
		updateListerTableList();
		final List<String> result = listerTableMap.keySet().stream()
												.sorted(listerTableNameComparator)
												.collect(Collectors.toList());
		return result;
	}

	public static void loadStopwords() {
		if (!stopwords.isEmpty())
			return;
		try (final Scanner in = new Scanner(new FileInputStream(stopwordsFile), StandardCharsets.UTF_8)) {
			while (in.hasNextLine()) {
				final String line = in.nextLine().trim();
				if (line.charAt(0) == '#')
					continue;
				stopwords.add(line);
			}
		} catch (FileNotFoundException e) {
			System.err.println(e);
		}
	}

	public static void updateStopwords() {
		stopwords.clear();
		loadStopwords();
	}
	
	public static void updateListerDBLockStatus() {
		final boolean lock = !Utilities.isDBWritable(Utilities.H2DB.LISTER);
		final String status = lock ? dbLockStatus[1] : dbLockStatus[0];
		final Node icon = lock
							? new TextIcon("lock", TextIcon.IconSet.AWESOME)
							: new TextIcon("unlock", TextIcon.IconSet.AWESOME);
		listerDBLocked.set(lock);
		listerDBLockString.set(status);
		listerDBLockIcon.set(icon);
	}

	public static void lockListerDB(final boolean lock) {
		Utilities.setDBWritable(Utilities.H2DB.LISTER, !lock);
		updateListerDBLockStatus();
	}
	
	public static List<String> getTermFreqListFromDB(final String tabName, final Set<String> terms) {
		final Map<String, Integer> tfMap = new HashMap<>();
		final Function<String, String> normalize = tabName.toUpperCase().startsWith("SC_")
													? x -> Utilities.changeToScNiggahita(x)
													: Function.identity();
		final String strWhere = terms.stream()
									.map(normalize)
									.map(x -> "TERM='" + x + "'")
									.collect(Collectors.joining(" OR "));
		final java.sql.Connection conn = Utilities.H2DB.LISTER.getConnection();
		try {
			if (conn != null) {
				final String query = "SELECT TERM,TOTFREQ FROM " + tabName + " WHERE " + strWhere + ";";
				final Statement stmt = conn.createStatement();
				final ResultSet rs = stmt.executeQuery(query);
				while (rs.next())
					tfMap.put(rs.getString(1), rs.getInt(2));
				rs.close();
				stmt.close();
			}
		} catch (SQLException e) {
			System.err.println(e);
		}
		final List<String> result = tfMap.keySet().stream()
										.sorted((x, y) -> Integer.compare(tfMap.get(y), tfMap.get(x)))
										.map(x -> x + " (" + tfMap.get(x) + ")")
										.collect(Collectors.toList());
		return result;	
	}
	
	public static Stage openWindow(final Utilities.WindowType win, final Object[] args) {
		Stage stg = Utilities.getOpenedWindow(win.getWindowClassName());
		switch (win) {
			case LUCENE:
				if (stg == null) {
					final LuceneFinder lucene = new LuceneFinder(); 
					stg = Utilities.openNewWindow(lucene,
							new Image(LuceneUtilities.class.getResourceAsStream("resources/images/lucene.png")), "Lucene Finder");
					lucene.setStage(stg);
				} else {
					Utilities.showExistingWindow(stg);
					final LuceneFinder lucene = (LuceneFinder)stg.getScene().getRoot();
					lucene.init();
				}
				break;
			case LISTER:
				if (stg == null) {
					final TermLister lister = new TermLister(); 
					stg = Utilities.openNewWindow(lister,
							new Image(LuceneUtilities.class.getResourceAsStream("resources/images/bars.png")), "Term Lister");
					lister.setStage(stg);
				} else {
					Utilities.showExistingWindow(stg);
					final TermLister lister = (TermLister)stg.getScene().getRoot();
					lister.init();
				}
				break;
		}
		return stg;
	}

}
