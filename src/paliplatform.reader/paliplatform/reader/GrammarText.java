/* 
 * GrammarText.java
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
import java.util.regex.*;
import java.util.stream.*;

/** 
 * The representation of a grammar textbook.
 * A dual book may be the case.
 * @author J.R. Bhaddacak
 * @version 3.2
 * @since 3.0
 */
public class GrammarText {
	public static enum GrammarBook {
		UNKNOWN("Unknown"),
		// the items correspond to text 'id' in docinfo
		KACC("Kacc"), RUPA("Rūpa"), MMD("Mmd"), KACCSN("Kacc-sn"), BLV("Blv"), DHMJS("Dhmjs"),
		MOGG("Mogg"), PAYO("Payo"), MOGGPT("Mogg-pt"), NIRU("Niru"),
		SADDPAD("Sadd-Pad"), SADDDHA("Sadd-Dhā"), SADDSUT("Sadd-Sut"),
		ABHIDHA("Abhidhā"), ABHIDHAT("Abhidhā-t"),
		SUBHO("Subho"), SUBHOT("Subho-t"), VUTT("Vutt"), DHATVA("Dhātva");
		private final String ref;
		private GrammarBook(final String ref) {
			this.ref = ref;
		}
		public String getRef() {
			return ref;
		}
		public String getFirstChar() {
			return "" + Character.toLowerCase(ref.charAt(0));
		}
		public String getBookId() {
			// this is equivalent to docid
			return toString().toLowerCase();
		}
		public List<GrammarBook> getRelatedBooks() {
			final List<GrammarBook> result;
			if (this == KACC) {
				result = List.of(RUPA, MMD, KACCSN, BLV);
			} else if (this == MOGG) {
				result = List.of(PAYO, MOGGPT, NIRU);
			} else if (this == ABHIDHA) {
				result = List.of(ABHIDHAT);
			} else {
				result = Collections.emptyList();
			}
			return result;
		}
	}
	private static final String REX_HEAD = "^<h([0-9])>(.*?)</h\\1>";
	private static final List<String> hasFormulaList = List.of("kacc", "rupa", "mmd", "kaccsn", "blv", "mogg", "payo", "moggpt", "niru", "sadddha", "saddsut");
	private static final List<GrammarBook> notHasBoldNumber = List.of(
			GrammarBook.DHMJS, GrammarBook.NIRU, GrammarBook.ABHIDHA, GrammarBook.ABHIDHAT,
			GrammarBook.SUBHO, GrammarBook.SUBHOT, GrammarBook.VUTT, GrammarBook.DHATVA
			);
	private final TocTreeNode node;
	private final GrammarBook gramBook;
	private final Map<GrammarBook, String> nameMap = new EnumMap<>(GrammarBook.class);
	private final Map<GrammarBook, String> textMap = new EnumMap<>(GrammarBook.class);
	private final Map<GrammarBook, List<String[]>> headListMap = new EnumMap<>(GrammarBook.class);
	private final Map<GrammarBook, List<String>> numListMap = new EnumMap<>(GrammarBook.class);
	private final Map<GrammarBook, Map<String, String>> formulaListMap = new EnumMap<>(GrammarBook.class);
	private final Map<GrammarBook, Map<String, String>> suttaListMap = new EnumMap<>(GrammarBook.class);
	private final Map<GrammarBook, Map<String, List<String>>> numTransMap = new EnumMap<>(GrammarBook.class);

	public GrammarText(final TocTreeNode node, final String rawText) {
		this.node = node;
		// set up key list and text
		final String id = node.getNodeId();
		gramBook = GrammarBook.valueOf(id.toUpperCase());
		if (gramBook == GrammarBook.NIRU)
			makeTranslationMap(gramBook);
		final String text = gramBook == GrammarBook.DHMJS
							? formatTextNoFormulaStrongHead(gramBook, rawText)
							: gramBook == GrammarBook.SADDPAD
								? formatTextNoFormulaNoNumber(gramBook, rawText)
								: formatTextFull(gramBook, rawText);
		textMap.put(gramBook, text);
		// set up name list
		final String textName = node.getNodeName();
		final int parenPos = textName.lastIndexOf("(");
		final String nameFull = parenPos > -1 ? textName.substring(0, parenPos).trim() : textName;
		nameMap.put(gramBook, nameFull);
	}

	private void readExtraBook(final GrammarBook bookId) {
		final String id = bookId.toString().toLowerCase();
		final Corpus corpus = node.getCorpus();
		final DocumentInfo docInfo = corpus.getDocInfo(id);
		final String text = ReaderUtilities.readTextFromZip(docInfo.getFileNameWithExt(), corpus);
		textMap.put(bookId, formatTextFull(bookId, text));
		makeSuttaList(bookId);
		if (bookId == GrammarBook.NIRU || bookId == GrammarBook.ABHIDHAT) {
			makeTranslationMap(bookId);
		}
	}

	private String formatTextFull(final GrammarBook bookId, final String text) {
		final StringBuilder result = new StringBuilder();
		final String[] lines = text.split("\\r?\\n");
		final List<String[]> headList = new ArrayList<>();
		final List<String> numList = new ArrayList<>();
		final Map<String, String> formulaMap = new HashMap<>();
		final Pattern headPatt = Pattern.compile(REX_HEAD);
		final Pattern numPatt = Pattern.compile(getNumberPatternString(bookId));
		int headCounter = 0;
		int numCounter = 0;
		for (final String line : lines) {
			if (line.startsWith("<!--")) continue;
			if (line.trim().isEmpty()) continue;
			final Matcher headMatcher = headPatt.matcher(line);
			if (headMatcher.matches()) {
				// all h
				final String headLevelStr = headMatcher.group(1);
				final int headLevel = Integer.parseInt(headLevelStr);
				final String headText = headMatcher.group(2);
				String idStr = "";
				if (headLevel > 2 && headLevel < 6) {
					// show only h3, h4, h5 in the heading nav
					final String[] headItem = new String[] { headText, headLevelStr, "" + headCounter };
					headList.add(headItem);
					idStr = " id='jumptarget-h" + headCounter + "'";
					headCounter++;
				}
				result.append("<h" + headLevel + idStr + ">" + headText + "</h" + headLevelStr + ">").append("\n");
			} else {
				// text body
				// test for sutta num first
				final Matcher numMatcher = numPatt.matcher(line);
				String theLine = line;
				if (numMatcher.matches()) {
					final String num = getNumberFromLine(bookId, line);
					numList.add(num);
					final String modLine = bookId == GrammarBook.NIRU ? insertMoggNumber(num, line) : line;
					if (hasSuttaFormula()) {
						final String formulaLine = getFormulaLine(bookId, modLine);
						formulaMap.put(num, formulaLine);
					}
					theLine = "<a id='jumptarget-n" + numCounter + "'/>" + line;
					numCounter++;
				}
				result.append(theLine).append("\n");
			}
		}
		headListMap.put(bookId, headList);
		numListMap.put(bookId, numList);
		formulaListMap.put(bookId, formulaMap);
		return result.toString();
	}

	private String insertMoggNumber(final String niruNum, final String line) {
		if (!numTransMap.containsKey(GrammarBook.NIRU)) return line;
		final List<String> moggNumList = numTransMap.get(GrammarBook.NIRU).get(niruNum);
		if (moggNumList == null || moggNumList.isEmpty()) return line;
		final String moggNum = moggNumList.get(0);
		final String moggRef = " (Mogg <b>" + moggNum + "</b>)";
		return line + moggRef;
	}

	private String formatTextNoFormulaNoNumber(final GrammarBook bookId, final String text) {
		final StringBuilder result = new StringBuilder();
		final String[] lines = text.split("\\r?\\n");
		final List<String[]> headList = new ArrayList<>();
		final Pattern headPatt = Pattern.compile(REX_HEAD);
		int headCounter = 0;
		for (final String line : lines) {
			if (line.startsWith("<!--")) continue;
			if (line.trim().isEmpty()) continue;
			final Matcher headMatcher = headPatt.matcher(line);
			if (headMatcher.matches()) {
				// all h
				final String headLevelStr = headMatcher.group(1);
				final int headLevel = Integer.parseInt(headLevelStr);
				final String headText = headMatcher.group(2);
				String idStr = "";
				if (headLevel > 2 && headLevel < 6) {
					// show only h3, h4, h5 in the heading nav
					final String[] headItem = new String[] { headText, headLevelStr, "" + headCounter };
					headList.add(headItem);
					idStr = " id='jumptarget-h" + headCounter + "'";
					headCounter++;
				}
				result.append("<h" + headLevel + idStr + ">" + headText + "</h" + headLevelStr + ">").append("\n");
			} else {
				// text body
				result.append(line).append("\n");
			}
		}
		headListMap.put(bookId, headList);
		return result.toString();
	}

	private String formatTextNoFormulaStrongHead(final GrammarBook bookId, final String text) {
		final StringBuilder result = new StringBuilder();
		final String[] lines = text.split("\\r?\\n");
		final List<String[]> headList = new ArrayList<>();
		final List<String> numList = new ArrayList<>();
		final Pattern headPatt = Pattern.compile("^<strong>(.*?)</strong>");
		final Pattern numPatt = Pattern.compile(getNumberPatternString(bookId));
		int headCounter = 0;
		int numCounter = 0;
		for (final String line : lines) {
			if (line.startsWith("<!--")) continue;
			if (line.trim().isEmpty()) continue;
			final Matcher headMatcher = headPatt.matcher(line);
			if (headMatcher.matches()) {
				final String headText = headMatcher.group(1);
				final String[] headItem = new String[] { headText, "0", "" + headCounter };
				headList.add(headItem);
				final String idStr = " id='jumptarget-h" + headCounter + "'";
				headCounter++;
				result.append("<br><strong" + idStr + ">" + headText + "</strong>").append("\n");
			} else {
				final Matcher numMatcher = numPatt.matcher(line);
				String theLine = line;
				if (numMatcher.matches()) {
					final String num = getNumberFromLine(bookId, line);
					numList.add(num);
					theLine = "<a id='jumptarget-n" + numCounter + "'/>" + line;
					numCounter++;
				}
				result.append(theLine).append("\n");
			}
		}
		headListMap.put(bookId, headList);
		numListMap.put(bookId, numList);
		return result.toString();
	}

	private String getNumberPatternString(final GrammarBook bookId) {
		final String result;
		if (hasBoldNumber(bookId)) {
			if (bookId == GrammarBook.KACC || bookId == GrammarBook.MMD) {
				result = "^<b>\\d+:[0-9x].*";
			} else if (bookId == GrammarBook.RUPA || bookId == GrammarBook.BLV) {
				result = "^<b>\\d+ .*";
			} else if (bookId == GrammarBook.KACCSN) {
				result = "^<b>Kacc_\\d+\\..*";
			} else if (bookId == GrammarBook.MOGG || bookId == GrammarBook.PAYO) {
				result = "^<b>[\\[0-9]+.*";
			} else if (bookId == GrammarBook.SADDDHA) {
				result = "^<b>\\[\\d+.*";
			} else {
				result = "^<b>\\d+\\..*";
			}
		} else {
			result = "^[0-9-]+\\..*";
		}
		return result;
	}

	private String getNumberFromLine(final GrammarBook bookId, final String line) {
		final String result;
		final String theLine;
		if (line.startsWith("<a")) {
			// remove anchor first, if any
			final int gpos = line.indexOf(">");
			theLine = line.substring(gpos + 1);
		} else {
			theLine = line;
		}
		if (hasBoldNumber(bookId)) {
			final int gpos = theLine.indexOf(">");
			if (bookId == GrammarBook.MOGG || bookId == GrammarBook.PAYO || bookId == GrammarBook.MOGGPT) {
				final int firstDotPos = theLine.indexOf(".");
				result = theLine.substring(gpos + 1, theLine.indexOf(".", firstDotPos + 1));
			} else if (bookId == GrammarBook.SADDDHA) {
				final int bpos = theLine.indexOf("[");
				result = theLine.substring(bpos + 1, theLine.indexOf("]"));
			} else {
				result = theLine.substring(gpos + 1, theLine.indexOf("."));
			}
		} else {
			result = theLine.substring(0, theLine.indexOf("."));
		}
		return result;
	}

	private String getFormulaLine(final GrammarBook bookId, final String line) {
		final String result;
		if (bookId == GrammarBook.SADDDHA) {
			result = line.substring(0, line.indexOf("</b>") + 4);
		} else {
			result = line;
		}
		return result;
	}

	private boolean hasBoldNumber(final GrammarBook bookId) {
		return notHasBoldNumber.indexOf(bookId) < 0;
	}

	private void makeSuttaList(final GrammarBook bookId) {
		final List<String> slist = getHtmlPList(textMap.get(bookId));
		final Map<String, String> suttaMap = new HashMap<>();
		final List<String> numList = numListMap.get(bookId);
		for (int i = 0; i < slist.size(); i++) {
			final String sid = numList.get(i);
			suttaMap.put(sid, slist.get(i));
		}
		suttaListMap.put(bookId, suttaMap);
	}

	private List<String> getHtmlPList(final String text) {
		final List<String> result = new ArrayList<>();
		StringBuilder buffer = new StringBuilder();
		final String[] lines = text.split("\\r?\\n");
		for (final String line : lines) {
			if (line.startsWith("<p ") || line.startsWith("<p>")) {
				buffer = new StringBuilder();
				buffer.append(line).append("\n");
			} else if (line.startsWith("</p>")) {
				buffer.append(line).append("\n");
				result.add(buffer.toString());
			} else {
				final String theLine = line.replaceFirst(" id='jumptarget-\\w\\d+'", "");
				buffer.append(theLine).append("\n");
			}
		}
		return result;
	}

	private void makeTranslationMap(final GrammarBook bookId) {
		if (bookId == GrammarBook.NIRU) {
			// for Niru to Mogg, read from csv file
			final String nmText = ReaderUtilities.getTextResource(ReaderUtilities.NIRU_TO_MOGG);
			final String[] lines = nmText.split("\\r?\\n");
			final Map<String, List<String>> nMap = new HashMap<>();
			final Map<String, List<String>> mMap = new HashMap<>();
			for (final String line : lines) {
				final String theLine = line.trim();
				if (theLine.isEmpty()) continue;
				final int delimPos = theLine.indexOf(":");
				if (delimPos < 0) continue;
				final String nNum = theLine.substring(0, delimPos);
				final String mNum = theLine.substring(delimPos + 1);
				final List<String> n2mList = new ArrayList<>();
				n2mList.add(mNum);
				nMap.put(nNum, n2mList);
				final List<String> m2nList = new ArrayList<>();
				m2nList.add(nNum);
				mMap.put(mNum, m2nList);
			}
			numTransMap.put(GrammarBook.MOGG, mMap);
			numTransMap.put(GrammarBook.NIRU, nMap);
		} else if (bookId == GrammarBook.ABHIDHAT) {
			// Abhidhā-t has some numbers in range, so create from this
			final List<String> numList = numListMap.get(GrammarBook.ABHIDHAT);
			final Map<String, List<String>> aMap = new HashMap<>();
			final Map<String, List<String>> tMap = new HashMap<>();
			for (final String num : numList) {
				final List<String> t2aList = new ArrayList<>();
				if (num.indexOf("-") > -1) {
					// numbers in a range
					final List<String> nList = Utilities.rangeToList(num);
					for (final String n : nList) {
						final List<String> a2tList = new ArrayList<>();
						a2tList.add(num);
						aMap.put(n, a2tList);
					}
					t2aList.addAll(nList);
					tMap.put(num, t2aList);
				} else {
					// single number
					final List<String> a2tList = new ArrayList<>();
					a2tList.add(num);
					t2aList.add(num);
					aMap.put(num, a2tList);
					tMap.put(num, t2aList);
				}
			}
			numTransMap.put(GrammarBook.ABHIDHA, aMap);
			numTransMap.put(GrammarBook.ABHIDHAT, tMap);
		}
	}

	public boolean isAvailable() {
		return !textMap.isEmpty();
	}

	public boolean hasSuttaFormula() {
		final String id = node.getNodeId();
		return hasFormulaList.indexOf(id) > -1;
	}

	public GrammarBook getBook() {
		return gramBook;
	}

	public String getBookName(final GrammarBook bookId) {
		return nameMap.get(bookId);
	}

	public int getTextCount() {
		return textMap.size();
	}

	public String getText() {
		return getText(gramBook);
	}

	public String getText(final GrammarBook bookId) {
		return textMap.containsKey(bookId) ? textMap.get(bookId) : "";
	}

	public String getText(final GrammarBook bookId, final List<GrammarBook> extraBooks) {
		final String result;
		if (!extraBooks.isEmpty()) {
			for (final GrammarBook book : extraBooks) {
				if (!textMap.containsKey(book)) {
					readExtraBook(book);
				}
			}
			result = combineBooks(bookId, extraBooks);
		} else {
			result = textMap.containsKey(bookId) ? textMap.get(bookId) : "";
		}
		return result;
	}

	private String combineBooks(final GrammarBook bookId, final List<GrammarBook> extraBooks) {
		final String text = textMap.get(bookId);
		final StringBuilder result = new StringBuilder();
		final String[] lines = text.split("\\r?\\n");
		final String pattStr = "^<a.*?/>" + getNumberPatternString(bookId).substring(1);
		final Pattern snumPatt = Pattern.compile(pattStr);
		boolean numFound = false;
		String sutNum = "";
		for (final String line : lines) {
			final Matcher numMatcher = snumPatt.matcher(line);
			if (numMatcher.matches()) {
				numFound = true;
				sutNum = getNumberFromLine(bookId, line);
			}
			if (line.startsWith("</p>") && numFound) {
				if (!extraBooks.isEmpty()) {
					final String extraBooksText = getExtraBookSutta(bookId, sutNum, extraBooks);
					if (!extraBooksText.isEmpty()) {
						result.append("\n<blockquote>\n");
						result.append(extraBooksText);
						result.append("\n</blockquote>\n");
					}
				}
				numFound = false;
			} else {
				result.append(line).append("\n");
			}
		}
		return result.toString();
	}

	public List<String[]> getHeadList() {
		return headListMap.get(gramBook);
	}

	public List<String[]> getHeadList(final GrammarBook bookId) {
		return headListMap.containsKey(bookId) ? headListMap.get(bookId) : Collections.emptyList();
	}

	public List<String> getNumList() {
		return getFirstNumList();
	}

	public List<String> getNumList(final GrammarBook bookId) {
		return numListMap.containsKey(bookId) ? numListMap.get(bookId) : Collections.emptyList();
	}

	public List<String> getFirstNumList() {
		return numListMap.isEmpty() ? Collections.emptyList() : numListMap.get(gramBook);
	}

	public String getFormulaListAsString(final GrammarBook bookId, final List<GrammarBook> extraBooks) {
		final StringBuilder result = new StringBuilder();
		final List<String> numList = numListMap.get(bookId);
		final Map<String, String> formulaMap = formulaListMap.get(bookId);
		if (!extraBooks.isEmpty()) {
			for (final GrammarBook book : extraBooks) {
				if (!textMap.containsKey(book)) {
					readExtraBook(book);
				}
			}
			for (final String snum : numList) {
				final String formula = formulaMap.get(snum);
				result.append("\n<div>\n");
				result.append(formula).append("\n");
				final String extraBooksFormula = getExtraBookFormula(bookId, snum, extraBooks);
				if (!extraBooksFormula.isEmpty()) {
					result.append("\n<blockquote>\n");
					result.append(extraBooksFormula);
					result.append("\n</blockquote>\n");
				}
				result.append("\n</div>\n");
			}
		} else {
			for (final String snum : numList)
				result.append(formulaMap.get(snum)).append("\n");
		}
		return result.toString();
	}

	private String getExtraBookFormula(final GrammarBook bookId, final String suttaNum, final List<GrammarBook> extraBooks) {
		final StringBuilder result = new StringBuilder();
		for (final GrammarBook book : extraBooks) {
			final Map<String, String> formulaMap = formulaListMap.get(book);
			final String sNum = bookId == GrammarBook.KACC
								? suttaNum.substring(0, suttaNum.indexOf(" ")) // for Kacc get only xxx:yyy part
								: suttaNum;
			final List<String> formulaList = new ArrayList<>();
			final List<String> numList = getRelatedSuttaNumbers(bookId, book, sNum);
			for (final String num : numList) {
				formulaList.add(formulaMap.get(num));
			}
			if (!formulaList.isEmpty()) {
				for (final String formula : formulaList) {
					final String label = book.getRef() + ": ";
					result.append(label).append(formula).append("\n");
				}
			}
		}
		return result.toString();
	}

	private String getExtraBookSutta(final GrammarBook bookId, final String suttaNum, final List<GrammarBook> extraBooks) {
		final StringBuilder result = new StringBuilder();
		for (final GrammarBook book : extraBooks) {
			final Map<String, String> suttaMap = suttaListMap.get(book);
			final String sNum = bookId == GrammarBook.KACC
								? suttaNum.substring(0, suttaNum.indexOf(" ")) // for Kacc get only xxx:yyy part
								: suttaNum;
			final List<String> suttaList = new ArrayList<>();
			final List<String> numList = getRelatedSuttaNumbers(bookId, book, sNum);
			for (final String num : numList) {
				suttaList.add(suttaMap.get(num));
			}
			if (!suttaList.isEmpty()) {
				for (final String sutta : suttaList) {
					final String label = "<span>[" + book.getRef() + "]</span>\n";
					result.append("<hr>\n").append(label).append(sutta).append("\n");
				}
			}
		}
		if (result.indexOf("<hr>") == 0)
			result.delete(0, 5); // remove leading hr
		return result.toString();
	}

	private List<String> getRelatedSuttaNumbers(final GrammarBook srcBook, final GrammarBook tgtBook, final String numStr) {
		final List<String> result = new ArrayList<>();
		if (srcBook == GrammarBook.KACC) {
			if (tgtBook == GrammarBook.RUPA || tgtBook == GrammarBook.BLV) {
				final String tgtNum = "[" + numStr + "]";
				result.addAll(numListMap.get(tgtBook).stream()
									.filter(x -> x.contains(tgtNum))
									.collect(Collectors.toList()));
			} else if (tgtBook == GrammarBook.MMD) {
				result.addAll(numListMap.get(tgtBook).stream()
									.filter(x -> x.equals(numStr))
									.collect(Collectors.toList()));
			} else if (tgtBook == GrammarBook.KACCSN) {
				// for Kacc-sn use only the first number
				final String knum = numStr.substring(0, numStr.indexOf(":"));
				result.addAll(numListMap.get(tgtBook).stream()
									.filter(x -> x.equals("Kacc_" + knum))
									.collect(Collectors.toList()));
			}
		} else if (srcBook == GrammarBook.MOGG) {
			if (tgtBook == GrammarBook.PAYO) {
				result.addAll(numListMap.get(tgtBook).stream()
									.filter(x -> x.endsWith(" " + numStr))
									.collect(Collectors.toList()));
			} else if (tgtBook == GrammarBook.MOGGPT) {
				result.addAll(numListMap.get(tgtBook).stream()
									.filter(x -> x.equals(numStr))
									.collect(Collectors.toList()));
			} else if (tgtBook == GrammarBook.NIRU) {
				final Map<String, List<String>> moggNiruMap = numTransMap.get(GrammarBook.MOGG);
				if (moggNiruMap.containsKey(numStr))
				result.addAll(moggNiruMap.get(numStr));
			}
		} else if (srcBook == GrammarBook.ABHIDHA) {
			final Map<String, List<String>> abhMap = numTransMap.get(GrammarBook.ABHIDHA);
			if (abhMap.containsKey(numStr))
			result.addAll(abhMap.get(numStr));
		}
		return result;
	}

}

