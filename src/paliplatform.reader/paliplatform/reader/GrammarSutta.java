/* 
 * GrammarSutta.java
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

package paliplatform.reader;

import paliplatform.base.*;

import java.util.*;
import java.util.stream.*;

/** 
 * The representation of a grammatical sutta.
 * This is mainly used in GramSutFinder. 
 * @author J.R. Bhaddacak
 * @version 3.0
 * @since 3.0
 */
public class GrammarSutta {
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
	private final Set<String> xref;
	private final String xrefString;

	public GrammarSutta(final String input) {
		final int sPos = input.indexOf(" ");
		bookName = input.substring(0, sPos);
		book = bookMap.get(bookName);
		final int firstDotPos = input.indexOf(".");
		final int bodyStart;
		if (book == GrammarText.GrammarBook.KACC) {
			suttaNumber = input.substring(sPos + 1, input.indexOf("."));
			bodyStart = input.charAt(firstDotPos + 1) == '.' ? firstDotPos + 1 : firstDotPos + 2;
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
		final String shortNum = book == GrammarText.GrammarBook.KACC
								? suttaNumber.substring(0, suttaNumber.indexOf(",")) // Kacc uses only before comma
								: book == GrammarText.GrammarBook.PAYO
									? suttaNumber.substring(suttaNumber.indexOf("]") + 2) // Payo uses Mogg number
									: suttaNumber;
		shortRef = Character.toLowerCase(book.toString().charAt(0)) + shortNum;
		original = input;
		xref = processXref(shortRef);
		if (ReaderUtilities.gramSutRefComparator == null)
			ReaderUtilities.gramSutRefComparator = ReaderUtilities.getReferenceComparator(ReaderUtilities.corpusMap.get(Corpus.Collection.GRAM));
		xrefString = xref.stream().sorted(ReaderUtilities.gramSutRefComparator).collect(Collectors.joining(", "));
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

	public String getOriginal() {
		return original;
	}

	public String getXrefString() {
		return xrefString;
	}

	public boolean hasXref(final String ref) {
		return xref.contains(ref);
	}

	private static Set<String> processXref(final String ref) {
		final Set<String> result = new HashSet<>();
		final String thisRef = ref.startsWith("p") ? "m" + ref.substring(1) + "," : ref + ","; // Payo uses Mogg
		for (final String line : ReaderUtilities.gramSutXrefList) {
			if (line.contains(thisRef)) {
				result.addAll(Arrays.asList(line.split(",")));
			}
		}
		return result;
	}

}
