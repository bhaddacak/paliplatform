/* 
 * GrammarSutta.java
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

import paliplatform.base.*;

import java.util.*;
import java.util.stream.*;

/** 
 * The representation of a grammatical sutta.
 * This is mainly used in GramSutFinder. 
 * @author J.R. Bhaddacak
 * @version 3.2
 * @since 3.0
 */
public class GrammarSutta {
	public static enum RefType { 
		SIMPLE, NIRU;
		public static final RefType[] values = RefType.values();
	}
	public static final Map<String, GrammarText.GrammarBook> bookMap = Map.of(
			"Kacc", GrammarText.GrammarBook.KACC,
			"Rūpa", GrammarText.GrammarBook.RUPA,
			"Mogg", GrammarText.GrammarBook.MOGG,
			"Payo", GrammarText.GrammarBook.PAYO,
			"Niru", GrammarText.GrammarBook.NIRU,
			"Sadd", GrammarText.GrammarBook.SADDSUT);
	private final GrammarText.GrammarBook book;
	private final String bookName;
	private final String suttaNumber;
	private final String suttaBody;
	private final String shortRef;
	private final String original;
	private final Map<RefType, Set<String>> xrefMap = new EnumMap<>(RefType.class);
	private final Map<RefType, String> xrefStringMap = new EnumMap<>(RefType.class);

	public GrammarSutta(final String input) {
		final int sPos = input.indexOf(" ");
		bookName = input.substring(0, sPos);
		book = bookMap.get(bookName);
		final int firstDotPos = input.indexOf(".");
		final int bodyStart;
		if (book == GrammarText.GrammarBook.KACC || book == GrammarText.GrammarBook.RUPA) {
			// Kacc num format = xxx:yyy [aaa], possibly [aaa, bbb] or [x]
			// Rūpa num format = aaa [xxx:yyy]
			suttaNumber = input.substring(sPos + 1, input.indexOf("."));
			bodyStart = firstDotPos + 2;
		} else if (book == GrammarText.GrammarBook.MOGG || book == GrammarText.GrammarBook.PAYO) {
			// these two have decimal form
			final int secondDotPos = input.indexOf(".", firstDotPos + 1);
			suttaNumber = input.substring(sPos + 1, secondDotPos);
			bodyStart = secondDotPos + 2;
		} else {
			suttaNumber = input.substring(sPos + 1, firstDotPos);
			bodyStart = firstDotPos + 2;
		}
		suttaBody = input.substring(bodyStart);
		final String shortNum = book == GrammarText.GrammarBook.KACC || book == GrammarText.GrammarBook.RUPA
								? suttaNumber.substring(0, suttaNumber.indexOf(" ")) // Kacc and Rūpa uses only the first part
								: book == GrammarText.GrammarBook.PAYO
									? suttaNumber.substring(suttaNumber.indexOf("[") + 1, suttaNumber.indexOf("]")) // Payo uses number in brackets
									: suttaNumber;
		shortRef = Character.toLowerCase(book.toString().charAt(0)) + shortNum;
		original = input;
		if (ReaderUtilities.gramSutRefComparator == null)
			ReaderUtilities.gramSutRefComparator = ReaderUtilities.getReferenceComparator(ReaderUtilities.corpusMap.get(Corpus.Collection.GRAM));
		for (final RefType rtype : RefType.values) {
			xrefMap.put(rtype, new HashSet<>());
		}
	}

	public GrammarText.GrammarBook getBook() {
		return book;
	}

	public String getSuttaBody(final boolean withNotes) {
		final String result;
		if (withNotes) {
			result = suttaBody;
		} else {
			// remove notes in [..]
			result = suttaBody.replaceAll(" \\[.*?\\]", "");
		}
		return result;
	}

	public String getFullRef() {
		return bookName + " " + suttaNumber;
	}

	public String getShortRef() {
		return shortRef;
	}

	public String getSuttaNumber() {
		return suttaNumber;
	}

	public String getOriginal() {
		return original;
	}

	public void addXref(final RefType rtype, final Set<String> refSet) {
		if (refSet == null) return;
		xrefMap.get(rtype).addAll(refSet);
		final String refStr = xrefMap.get(rtype).stream().sorted(ReaderUtilities.gramSutRefComparator).collect(Collectors.joining(", "));
		xrefStringMap.put(rtype, refStr);
	}

	public String getXrefString(final RefType rtype) {
		final String result = xrefStringMap.get(rtype);
		return result == null ? "" : result;
	}

	public boolean hasXref(final String ref) {
		return xrefMap.get(RefType.SIMPLE).contains(ref) || xrefMap.get(RefType.NIRU).contains(ref);
	}

	public boolean xrefContains(final String needle) {
		boolean result = xrefMap.get(RefType.SIMPLE).stream().filter(x -> x.indexOf(needle) > -1).findFirst().isPresent();
		return result ? result : xrefMap.get(RefType.NIRU).stream().filter(x -> x.indexOf(needle) > -1).findFirst().isPresent();
	}

}
