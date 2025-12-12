/*
 * SktDictEntry.java
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

package paliplatform.sanskrit;

/** 
 * Representation of dictionary entry used for Sanskrit dict.
 * @author J.R. Bhaddacak
 * @version 3.5
 * @since 3.5
 */

public class SktDictEntry {
	private final String lid; // Cologne record identifier
	private final String pagecol; // page/column
	private final String key1; // used for searching
	private final String key2; // original headword
	private final String hnum; // homonym number
	private String meaning;

	public SktDictEntry(final String id, final String pc, final String k1, final String k2, final String h) {
		lid = id;
		pagecol = pc;
		key1 = k1;
		key2 = k2;
		hnum = h;
		meaning = "";
	}

	public String getKey1() {
		return key1;
	}

	public String getKey2() {
		return key2;
	}

	public String getHnum() {
		return hnum;
	}

	public void setMeaning(final String text) {
		meaning = text;
	}

	public String getMeaning() {
		return meaning;
	}

}
