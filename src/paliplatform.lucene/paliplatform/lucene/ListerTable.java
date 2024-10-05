/*
 * ListerTable.java
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
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see https://www.gnu.org/licenses/.
 */

package paliplatform.lucene;

import paliplatform.base.*;

/** 
 * The representation of a table used in TermLister.
 * @author J.R. Bhaddacak
 * @version 3.0
 * @since 3.0
 */
class ListerTable {
	private static final String noteFlag = "O";
	private final String tableName;
	private final LuceneIndex index;

	public ListerTable(final String name) {
		tableName = name;
		// remove ending noteFlag, if any, to get index
		final String forIndex = name.endsWith(noteFlag)
								? name.endsWith("_" + noteFlag)
									? name.substring(0, name.length() - 2)
									: name.substring(0, name.length() - 1)
								 : name;
		index = LuceneIndex.getIndexByListerTableName(forIndex);
	}

	public String getTableName() {
		return tableName;
	}

	public int getDocCount() {
		return index.getNumDocs();
	}

	public LuceneIndex getIndex() {
		return index;
	}

	public void deleteTable() {
		final String delete = "DROP TABLE " + tableName + ";";
		Utilities.executeSQL(Utilities.H2DB.LISTER, delete);
	}

	public static String getTableInfo(final String tabName) {
		final ListerTable table = LuceneUtilities.listerTableMap.get(tabName);
		if (table == null) return "";
		final String result;
		final LuceneIndex index = table.getIndex();
		if (index == null) return "";
		final String indInfo = index.getIndexInfo();
		if (tabName.endsWith(noteFlag))
			result = indInfo.contains("including")
						? indInfo.replaceFirst("including .*? \\[", "including notes [")
						: indInfo.replaceFirst("\\[", "including notes [");
		else
			result = indInfo.contains("including")
						? indInfo.replaceFirst("including .*? \\[", "[")
						: indInfo;
		return result;
	}

	public static boolean exists(final String name) {
		return LuceneUtilities.listerTableMap.containsKey(name);
	}

	public static String modName(final String tabName, final boolean withNotes) {
		if (tabName.endsWith(noteFlag)) return tabName;
		final String result;
		final String[] names = tabName.split("_");
		if (withNotes) {
			if (names.length >= 3)
				result = tabName + noteFlag;
			else if (names.length == 2)
				result = tabName + "_" + noteFlag;
			else
				result = tabName;
		} else {
			result = tabName;
		}
		return result;
	}

}
