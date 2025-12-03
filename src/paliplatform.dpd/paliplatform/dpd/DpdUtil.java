/*
 * DpdUtil.java
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

package paliplatform.dpd;

import paliplatform.base.*;

import java.util.*;
import java.util.stream.*;
import java.io.*;
import java.sql.*;
import com.google.gson.Gson;

/** 
 * The CLI tool for DPD db.
 * This consists of static factory methods.
 * The tool can be invoked by this command line from the program's root dir:
 * $ java -p modules -m paliplatform.dpd/paliplatform.dpd.DpdUtil
 *
 * @author J.R. Bhaddacak
 * @version 3.4
 * @since 3.0
 */
final public class DpdUtil {
	private static final String ATOMS_OUTPUT = "dpd-atoms";
	private static final String LINESEP = System.getProperty("line.separator");
	private static final Gson gson = new Gson();

	private DpdUtil() {
	}

	public static void main(final String[] args) throws Exception {
		if (args.length == 0) {
			printHelpAndExit();
		}
		switch (args[0]) {
			case "-v":
				printVersion();
				break;
			case "-t":
				testApplicability();
				break;
		}
	}

	private static void printHelpAndExit() {
		final StringBuilder help = new StringBuilder();
		help.append(LINESEP).append("Pāli Platform DPD Util CLI").append(LINESEP);
		help.append("  Usage:").append(LINESEP);
		help.append("    DpdUtil [<command>] <option>").append(LINESEP);
		help.append("      (The abstract DpdUtil can be a launcher script,").append(LINESEP);
		help.append("       such as dpdutil.sh or dpdutil.cmd which can be").append(LINESEP);
		help.append("       found in the program's root directory.").append(LINESEP);
		help.append("       See also Notes below.)").append(LINESEP);
		help.append("  Commands:").append(LINESEP);
		help.append("    <none>\tShow this help").append(LINESEP);
		help.append("        General options:").append(LINESEP);
		help.append("        -t\tTest for DPD database applicability").append(LINESEP);
		help.append("        -v\tShow DPD database version").append(LINESEP);
		help.append("  Notes:").append(LINESEP);
		help.append("    To invoke the program, the Java convention has to be used.").append(LINESEP);
		help.append("    At the program's root directory, if no launcher script available,").append(LINESEP);
		help.append("    type this at the console: ").append(LINESEP);
		help.append("    $ java -p modules -m paliplatform.dpd/paliplatform.dpd.DpdUtil").append(LINESEP);
		printLog(help.toString());
		System.exit(0);
	}

	private static void printLog(final String mess) {
		System.out.println(mess);
	}

	private static void printTime(final long msec) {
		printLog(String.format("Done in %.3f seconds", msec/1000.0));
	}

	private static boolean dpdInit() {
		final boolean dbExists = DpdUtilities.checkIfDpdAvailable();
		final boolean success;
		if (dbExists) {
			Utilities.initializeDpdDB();
			success = true;
		} else {
			printLog("DPD DB not found, download it first");
			success = false;
		}
		return success;
	}

	private static boolean ppdpdInit(final Utilities.PpdpdTable table) {
		Utilities.initializePpdpdDB();
		Utilities.initializePpdpdAvailMap();
		Utilities.updatePpdpdAvailibility();
		final boolean success = Utilities.ppdpdAvailMap.get(table).get();
		if (!success)
			printLog("PP-DPD unavailable, create it first");
		return success;
	}

	private static void finish() {
		try {
			for (final Utilities.H2DB db : Utilities.H2DB.values()) {
				final java.sql.Connection conn = db.getConnection();
				if (conn != null)
					conn.close();
			}
			for (final Utilities.SQLiteDB db : Utilities.SQLiteDB.values()) {
				final java.sql.Connection conn = db.getConnection();
				if (conn != null)
					conn.close();
			}
		} catch (SQLException e) {
			System.err.println(e);
		}
	}

	public static void printVersion() throws SQLException {
		final long startTime = System.currentTimeMillis();
		if (!dpdInit()) return;
		final String select = "SELECT value FROM db_info WHERE key = 'dpd_release_version';";
		final java.sql.Connection conn = Utilities.SQLiteDB.DPD.getConnection();
		final Statement stmt = conn.createStatement();
		final ResultSet rs = stmt.executeQuery(select);
		if (rs.next()) {
			printLog("DPD version: " + rs.getString(1));
		}
		rs.close();
		stmt.close();
		finish();
		final long endTime = System.currentTimeMillis();
		printTime(endTime - startTime);
	}

	public static void testApplicability() throws SQLException {
		final long startTime = System.currentTimeMillis();
		if (!dpdInit()) return;
		final List<String> result = DpdUtilities.checkApplicability();
		finish();
		if (result.isEmpty()) {
			printLog("OK: DPD DB is suitable enough to use");
		} else {
			for (final String err : result)
				printLog("Error: " + err);
			printLog("Error: DPD DB is not suitable to use, try it with your own risk");
		}
		final long endTime = System.currentTimeMillis();
		printTime(endTime - startTime);
	}

}
