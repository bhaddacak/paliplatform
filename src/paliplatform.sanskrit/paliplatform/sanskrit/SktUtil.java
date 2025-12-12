/*
 * SktUtil.java
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
import java.util.regex.*;
import java.io.*;
import java.nio.file.*;
import java.nio.charset.StandardCharsets;
import java.sql.*;

/** 
 * The CLI tool for the sanskrit module.
 * This consists of static factory methods.
 * The tool can be invoked by this command line from the program's root dir:
 * $ java -p modules -m paliplatform.sanskrit/paliplatform.sanskrit.SktUtil
 *
 * @author J.R. Bhaddacak
 * @version 3.5
 * @since 3.5
 */
final public class SktUtil {
	private static final String LINESEP = System.getProperty("line.separator");
	private static Properties urlProps = new Properties();

	private SktUtil() {
	}

	public static void main(final String[] args) throws Exception {
		if (args.length == 0) {
			printHelpAndExit();
		}
		String opt = "";
		String param = "";
		switch (args[0]) {
			case "create":
				opt = args.length > 1 ? args[1] : "";
//~ 				param = args.length > 2 ? args[2] : "";
				if (opt.equals("-d")) {
					createDictDB();
				} else {
					printHelpAndExit();
				}
				break;
			default:
				printHelpAndExit();
		}
	}

	private static void printHelpAndExit() {
		final StringBuilder help = new StringBuilder();
		help.append(LINESEP).append("Pāli Platform Sanskrit Util CLI").append(LINESEP);
		help.append("  Usage:").append(LINESEP);
		help.append("    SktUtil <command> <option>").append(LINESEP);
		help.append("      (The abstract SktUtil can be a launcher script,").append(LINESEP);
		help.append("       such as sktutil.sh or sktutil.cmd which can be").append(LINESEP);
		help.append("       found in the program's root directory.").append(LINESEP);
		help.append("       See also Notes below.)").append(LINESEP);
		help.append("  Commands:").append(LINESEP);
		help.append("    create\tCreate data/db").append(LINESEP);
		help.append("        -d\tCreate Sanskrit dict db").append(LINESEP);
		help.append("    <none>\tShow this help").append(LINESEP);
		help.append("  Notes:").append(LINESEP);
		help.append("    To invoke the program, the Java convention has to be used.").append(LINESEP);
		help.append("    At the program's root directory, if no launcher script available,").append(LINESEP);
		help.append("    type this at the console: ").append(LINESEP);
		help.append("    $ java -p modules -m paliplatform.sanskrit/paliplatform.sanskrit.SktUtil").append(LINESEP);
		printLog(help.toString());
		System.exit(0);
	}

	private static void printLog(final String mess) {
		System.out.println(mess);
	}

	private static void printTime(final long msec) {
		printLog(String.format("Done in %.3f seconds", msec/1000.0));
	}

	private static void createDictDB() throws Exception {
		final long startTime = System.currentTimeMillis();
		final Pattern entryPatt = Pattern.compile("<L>(.*?)<pc>(.*?)<k1>(.*?)<k2>(.*?)<.*");
		final Pattern homoNumPatt = Pattern.compile(".*<h>(.*?)<.*");
		for (final SanskritUtilities.SktDictBook dict : SanskritUtilities.SktDictBook.books) {
			printLog("Initializing " + dict.bookName);
			final File dataFile = new File(SanskritUtilities.DICTPATH + dict.getDataFileName());
			if (!dataFile.exists()) {
				printLog(dataFile.getPath() + " is expected but not found");
				System.exit(1);
			}
			final Utilities.H2DB dictDB = Utilities.H2DB.SKTDICT;
			final String dbName = dictDB.getNameWithExt();
			if (Utilities.isDBPresent(dictDB)) {
				printLog(dbName + " already existed, operation aborted");
				System.exit(1);
			} else {
				Utilities.initializeSktDictDB(true);
				final String table = dict.toString();
				final java.sql.Connection conn = dictDB.getConnection();
				final String create = "CREATE TABLE " + table + " (" +
					"ID INT PRIMARY KEY," +
					"LID VARCHAR(16) NOT NULL," +
					"PAGECOL VARCHAR(16) NOT NULL," +
					"KEY1 VARCHAR(255) NOT NULL," +
					"KEY2 VARCHAR(255) NOT NULL," +
					"HNUM VARCHAR(4)," +
					"MEANING CLOB);";
				Utilities.executeSQL(conn, create);
				final String insert = "INSERT INTO " + table + " VALUES(?, ?, ?, ?, ?, ?, ?);";
				final PreparedStatement pstm = conn.prepareStatement(insert);
				int id = 0;
				String lid = "";
				String pagecol = "";
				String key1 = "";
				String key2 = "";
				String homoNum = "";
				StringBuilder meaning = null;
				try (final Scanner in = new Scanner(new FileInputStream(dataFile), StandardCharsets.UTF_8)) {
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
								key1 = matcher.group(3);
								key2 = matcher.group(4);
								if (homoMatcher.matches()) {
									homoNum = homoMatcher.group(1);
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
								pstm.setString(7, meaning.toString());
								pstm.executeUpdate();
								if (id % 5000 == 0)
									printLog(id + " records inserted");
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
				}
				pstm.close();
				printLog(id + " records created");
				printLog("Creating index...");
				final String index = "CREATE INDEX IDX_" + table + " ON " + table + "(KEY1);";
				Utilities.executeSQL(conn, index);
				printLog(table + " creation done");
				conn.close();
			}

		}
		final long endTime = System.currentTimeMillis();
		printTime(endTime - startTime);
	}

}
