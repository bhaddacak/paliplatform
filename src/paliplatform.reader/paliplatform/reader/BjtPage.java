/*
 * BjtPage.java
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

package paliplatform.reader;

import java.util.*;

/** 
 * The representation of a page of BJT texts.
 *
 * @author J.R. Bhaddacak
 * @version 3.0
 * @since 3.0
 */
public class BjtPage {
	public static enum Type {
		HEADING, CENTERED, PARAGRAPH, UNINDENTED, GATHA;
		public static final Map<String, Type> typeMap = Map.of(
				"heading", HEADING, "centered", CENTERED, "paragraph",
				PARAGRAPH, "unindented", UNINDENTED, "gatha", GATHA);
		public static boolean isValid(final String tag) {
			return typeMap.containsKey(tag);
		}
	}
	private int num;
	private List<Element> entries;

	BjtPage(final int n) {
		num = n;
		entries = new ArrayList<>();
	}

	public int getPageNum() {
		return num;
	}

	public void addEntry(final String typeStr, final String text, final int level) {
		if (Type.isValid(typeStr)) {
			final Element elem = new Element(Type.typeMap.get(typeStr), text, level);
			entries.add(elem);
		}
	}

	public List<Element> getEntries() {
		return entries;
	}

	// inner class
	public static class Element {
		private final Type type;
		private String text;
		private int level;
		Element(final Type typ, final String tex, final int lev) {
			type = typ;
			text = tex;
			level = lev;
		}
		public Type getType() {
			return type;
		}
		public String getText() {
			return text;
		}
		public int getLevel() {
			return level;
		}
	}

}

