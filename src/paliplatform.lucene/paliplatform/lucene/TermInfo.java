/*
 * TermInfo.java
 *
 * Copyright (C) 2023-2026 J. R. Bhaddacak 
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

import paliplatform.reader.*;

import java.util.*;
import java.util.stream.*;

/** 
 * The representation of term's information.
 * @author J.R. Bhaddacak
 * @version 3.7
 * @since 2.0
 */
 
public class TermInfo {
	public static enum Field {
		BODYTEXT, CENTRE, INDENT, UNINDENTED,
		NIKAYA, BOOK, CHAPTER, TITLE, SUBHEAD, SUBSUBHEAD,
		GATHA1, GATHA2, GATHA3, GATHALAST,
		H2, H3, H4, H5, H6, // used in CSTR
		PART, GROUP, SUBGROUP, ENDPART, ENDGROUP, ENDSUBGROUP, STRONG, // used in CSTR
		HEADING, GATHA, // used in BJT
		NOTE, BOLD; // excluding PARANUM, HANGNUM, PB, and DOT in CST4
		public static final Field[] values = Field.values();
		private static final Map<Corpus.Collection, List<Field>> fieldListMap = new EnumMap<>(Corpus.Collection.class);
		private static final Map<Corpus.Collection, Set<String>> fieldSetMap = new EnumMap<>(Corpus.Collection.class);
		static {
			// for CSTR
			fieldListMap.put(Corpus.Collection.CSTR, List.of(
						BODYTEXT, CENTRE, INDENT, UNINDENTED,
						H2, H3, H4, H5, H6,
						PART, GROUP, SUBGROUP, ENDPART, ENDGROUP, ENDSUBGROUP, STRONG,
						GATHA1, GATHA2, GATHA3, GATHALAST,
						NOTE, BOLD
						));
			final Set<String> cstrSet = fieldListMap.get(Corpus.Collection.CSTR).stream()
										.map(Field::toString).collect(Collectors.toSet());
			fieldSetMap.put(Corpus.Collection.CSTR, cstrSet);
			// for CSTDEVA and CST4 (identical structure)
			final List<Field> fieldList = List.of(
						BODYTEXT, CENTRE, INDENT, UNINDENTED,
						NIKAYA, BOOK, CHAPTER, TITLE, SUBHEAD, SUBSUBHEAD,
						GATHA1, GATHA2, GATHA3, GATHALAST,
						NOTE, BOLD);
			fieldListMap.put(Corpus.Collection.CSTDEVA, fieldList);
			final Set<String> cstDevaSet = fieldListMap.get(Corpus.Collection.CSTDEVA).stream()
										.map(Field::toString).collect(Collectors.toSet());
			fieldSetMap.put(Corpus.Collection.CSTDEVA, cstDevaSet);
			fieldListMap.put(Corpus.Collection.CST4, fieldList);
			final Set<String> cst4Set = fieldListMap.get(Corpus.Collection.CST4).stream()
										.map(Field::toString).collect(Collectors.toSet());
			fieldSetMap.put(Corpus.Collection.CST4, cst4Set);
			// for BJT (bodytext = paragraph)
			fieldListMap.put(Corpus.Collection.BJT, List.of(BODYTEXT, HEADING, CENTRE, UNINDENTED, GATHA, BOLD));
			final Set<String> bjtSet = fieldListMap.get(Corpus.Collection.BJT).stream()
										.map(Field::toString).collect(Collectors.toSet());
			fieldSetMap.put(Corpus.Collection.BJT, bjtSet);
			// for SRT
			fieldListMap.put(Corpus.Collection.SRT, List.of(BODYTEXT, NOTE));
			final Set<String> srtSet = fieldListMap.get(Corpus.Collection.SRT).stream()
										.map(Field::toString).collect(Collectors.toSet());
			fieldSetMap.put(Corpus.Collection.SRT, srtSet);
			// for GRAM
			fieldListMap.put(Corpus.Collection.GRAM, List.of(BODYTEXT, H2, H3, H4, H5));
			final Set<String> gramSet = fieldListMap.get(Corpus.Collection.GRAM).stream()
										.map(Field::toString).collect(Collectors.toSet());
			fieldSetMap.put(Corpus.Collection.GRAM, gramSet);
		}
		public String getTag() {
			return this.toString().toLowerCase();
		}
		public static List<Field> getFieldList(final Corpus.Collection col) {
			final List<Field> result = fieldListMap.getOrDefault(col, new ArrayList<>());
			if (result.isEmpty()) {
				result.add(BODYTEXT);
			}
			return result;
		}
		public static boolean hasGatha(final Corpus.Collection col) {
			return col == Corpus.Collection.CSTR || col == Corpus.Collection.CSTDEVA || col == Corpus.Collection.CST4
					? true
					: false;
		}
		public static boolean isGatha(final String tag) {
			return tag.startsWith("gatha");
		}
		public static boolean isValid(final Corpus.Collection col, final String tag) {
			final Set<String> fieldSet = fieldSetMap.get(col);
			return fieldSet == null ? false : fieldSet.contains(tag.toUpperCase());
		}
	}
	private final String term;
	private int totalFreq = 0;
	private int gathaFreq = 0;

	public TermInfo(final String term) {
		this.term = term;
	}

	public String getTerm() {
		return term;
	}

	public void addUpTotalFreq(final int freq) {
		totalFreq += freq;
	}

	public void addUpGathaFreq(final int freq) {
		gathaFreq += freq;
	}

	public int getTotalFreq() {
		return totalFreq;
	}

	public int getGathaFreq() {
		return gathaFreq;
	}

}
