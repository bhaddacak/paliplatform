/* 
 * GrammarText.java
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
import java.util.regex.*;

/** 
 * The representation of a grammar textbook.
 * A dual book may be the case.
 * @author J.R. Bhaddacak
 * @version 3.0
 * @since 3.0
 */
public class GrammarText {
	public static enum GrammarBook {
		UNKNOWN("Unknown"), KACC("Kacc"), RUPA("Rūpa"), DHMJS("Dhmjs"),
		MOGG("Mogg"), PAYO("Payo"), PANCT("Pañc-t"), NIRU("Niru"),
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
		public String getBundleId() {
			final String result;
			if (this == KACC || this == RUPA)
				result = "kaccrupa";
			else if (this == MOGG || this == PAYO)
				result = "moggpayo";
			else
				result = toString().toLowerCase();
			return result;
		}
	}
	private static final String REX_HEAD = "^<h([0-9])>(.*?)</h\\1>";
	private static final List<String> dualTextList = List.of("kaccrupa", "moggpayo", "abhidha");
	private static final List<String> hasFormulaList = List.of("kaccrupa", "moggpayo", "panct", "niru", "sadddha", "saddsut");
	private static final List<GrammarBook> notHasBoldNumber = List.of(
			GrammarBook.DHMJS, GrammarBook.NIRU, GrammarBook.ABHIDHA, GrammarBook.ABHIDHAT,
			GrammarBook.SUBHO, GrammarBook.SUBHOT, GrammarBook.VUTT, GrammarBook.DHATVA
			);
	private final TocTreeNode node;
	private final List<GrammarBook> bookList;
	private final Map<GrammarBook, String> nameMap = new EnumMap<>(GrammarBook.class);
	private final Map<GrammarBook, String> textMap = new EnumMap<>(GrammarBook.class);
	private final Map<GrammarBook, List<String[]>> headListMap = new EnumMap<>(GrammarBook.class);
	private final Map<GrammarBook, List<String>> numListMap = new EnumMap<>(GrammarBook.class);
	private final Map<GrammarBook, Map<String, String>> formulaListMap = new EnumMap<>(GrammarBook.class);
	private final Map<GrammarBook, Map<String, String>> suttaListMap = new EnumMap<>(GrammarBook.class);
	private final Map<GrammarBook, Map<String, List<String>>> translationMap = new EnumMap<>(GrammarBook.class);
	private final Map<GrammarBook, Map<String, List<String>>> extraTransMap = new EnumMap<>(GrammarBook.class);

	public GrammarText(final TocTreeNode node, final String rawText) {
		this.node = node;
		// set up key list and text
		final String id = node.getNodeId();
		if (dualTextList.indexOf(id) > -1) {
			final GrammarBook firstId, secondId;
			if (id.equals("abhidha")) {
				firstId = GrammarBook.ABHIDHA;
				secondId = GrammarBook.ABHIDHAT;
			} else {
				firstId = GrammarBook.valueOf(id.substring(0, 4).toUpperCase());
				secondId = GrammarBook.valueOf(id.substring(4).toUpperCase());
			}
			bookList = Arrays.asList(firstId, secondId);
			final int secondPos = rawText.indexOf(getDelimString(id));
			textMap.put(firstId, formatTextFull(firstId, rawText.substring(0, secondPos)));
			textMap.put(secondId, formatTextFull(secondId, rawText.substring(secondPos)));
			makeSuttaList(firstId);
			makeSuttaList(secondId);
			makeTranslationMap(id);
		} else {
			if (id.equals("niru"))
				makeTranslationMap(id);
			final GrammarBook bookId = GrammarBook.valueOf(id.toUpperCase());
			bookList = Arrays.asList(bookId);
			final String text = bookId == GrammarBook.DHMJS
								? formatTextNoFormulaStrongHead(bookId, rawText)
								: bookId == GrammarBook.SADDPAD
									? formatTextNoFormulaNoNumber(bookId, rawText)
									: formatTextFull(bookId, rawText);
			textMap.put(bookId, text);

		}
		// set up name list
		final String textName = node.getNodeName();
		final int parenPos = textName.lastIndexOf("(");
		final String nameFull = parenPos > -1 ? textName.substring(0, parenPos).trim() : textName;
		if (bookList.size() == 1) {
			nameMap.put(bookList.get(0), nameFull);
		} else {
			final String[] names = nameFull.split(" ");
			nameMap.put(bookList.get(0), names.length > 0 ? names[0] : "");
			nameMap.put(bookList.get(1), names.length > 1 ? names[1] : "");
		}
	}

	private static String getDelimString(final String id) {
		final String result;
		if (id.equals("kaccrupa"))
			result = "<!--rupasiddhi-->";
		else if (id.equals("moggpayo"))
			result = "<!--payogasiddhi-->";
		else if (id.equals("abhidha"))
			result = "<!--abhidhanatika-->";
		else
			result = "";
		return result;
	}

	private void readExtraBook(final GrammarBook bookId) {
		if (node.getNodeId().equals("moggpayo")) {
			final String id = bookId.toString().toLowerCase();
			final Corpus corpus = node.getCorpus();
			final DocumentInfo docInfo = corpus.getDocInfo(id);
			final String text = ReaderUtilities.readTextFromZip(docInfo.getFileNameWithExt(), corpus);
			textMap.put(bookId, formatTextFull(bookId, text));
			makeSuttaList(bookId);
			if (bookId == GrammarBook.NIRU)
				makeTranslationMap(id);
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
		if (!extraTransMap.containsKey(GrammarBook.NIRU)) return line;
		final List<String> moggNumList = extraTransMap.get(GrammarBook.NIRU).get(niruNum);
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
			if (bookId == GrammarBook.KACC || bookId == GrammarBook.RUPA) {
				result = "^<b>\\d+[.,].*";
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
			if (bookId == GrammarBook.MOGG || bookId == GrammarBook.PAYO || bookId == GrammarBook.PANCT) {
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

	private void makeTranslationMap(final String id) {
		if (id.equals("kaccrupa")) {
			// Kacc contains Rupa sutta numbers, so use Kacc
			final List<String> numList = numListMap.get(GrammarBook.KACC);
			final Map<String, List<String>> kMap = new HashMap<>();
			final Map<String, List<String>> rMap = new HashMap<>();
			for (final String snum : numList) {
				final List<String> k2rList = new ArrayList<>();
				final String ksut = snum;
				final String[] rsuts = snum.split(",");
				for (int i = 1; i < rsuts.length; i++) {
					// start from the second number in Kacc sutta number, indicating Rupa sutta number
					final String rsut = rsuts[i].trim();
					if (rsut.matches("\\d+")) {
						k2rList.add(rsut);
					}
					final List<String> r2kList = rMap.containsKey(rsut) ? rMap.get(rsut) : new ArrayList<>();
					r2kList.add(ksut);
					rMap.put(rsut, r2kList);
				}
				kMap.put(ksut, k2rList);
			}
			translationMap.put(GrammarBook.KACC, kMap);
			translationMap.put(GrammarBook.RUPA, rMap);
		} else if (id.equals("moggpayo")) {
			// Payo contains Mogg sutta numbers, so use Payo
			final List<String> numList = numListMap.get(GrammarBook.PAYO);
			final Map<String, List<String>> mMap = new HashMap<>();
			final Map<String, List<String>> pMap = new HashMap<>();
			for (final String snum : numList) {
				// use the whole as Payo number
				final String psut = snum;
				// extract Mogg number after [xxx]
				final String msut = snum.substring(snum.indexOf("]") + 2);
				final List<String> m2pList = new ArrayList<>();
				m2pList.add(psut);
				mMap.put(msut, m2pList);
				final List<String> p2mList = new ArrayList<>();
				p2mList.add(msut);
				pMap.put(psut, p2mList);
			}
			translationMap.put(GrammarBook.MOGG, mMap);
			translationMap.put(GrammarBook.PAYO, pMap);
		} else if (id.equals("niru")) {
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
			extraTransMap.put(GrammarBook.MOGG, mMap);
			extraTransMap.put(GrammarBook.NIRU, nMap);
		} else if (id.equals("abhidha")) {
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
			translationMap.put(GrammarBook.ABHIDHA, aMap);
			translationMap.put(GrammarBook.ABHIDHAT, tMap);
		}
	}

	public boolean isAvailable() {
		return !textMap.isEmpty();
	}

	public boolean isDual() {
		return textMap.size() >= 2;
	}

	public boolean hasSuttaFormula() {
		final String id = node.getNodeId();
		return hasFormulaList.indexOf(id) > -1;
	}

	public List<GrammarBook> getBookList() {
		return bookList;
	}

	public GrammarBook getFirstBook() {
		return bookList == null ? GrammarBook.UNKNOWN : bookList.get(0);
	}

	private GrammarBook getOtherBook(final GrammarBook bookId) {
		final GrammarBook result;
		if (bookList == null) {
			result = GrammarBook.UNKNOWN;
		} else {
			final int ind = bookList.indexOf(bookId);
			result = ind == -1 ? GrammarBook.UNKNOWN : bookList.get((ind + 1) % 2);
		}
		return result;
	}

	public String getBookName(final GrammarBook bookId) {
		return nameMap.get(bookId);
	}

	public int getTextCount() {
		return textMap.size();
	}

	public String getText() {
		return getFirstText();
	}

	public String getText(final GrammarBook bookId, final boolean combineTwoBooks, final List<GrammarBook> extraBooks) {
		final String result;
		if (combineTwoBooks) {
			if (!extraBooks.isEmpty()) {
				for (final GrammarBook book : extraBooks) {
					if (!textMap.containsKey(book))
						readExtraBook(book);
				}
			}
			result = combineTextOfTwoBooks(bookId, extraBooks);
		} else {
			result = textMap.containsKey(bookId) ? textMap.get(bookId) : "";
		}
		return result;
	}

	private String combineTextOfTwoBooks(final GrammarBook bookId, final List<GrammarBook> extraBooks) {
		final String text = textMap.get(bookId);
		final GrammarBook otherBook = getOtherBook(bookId);
		if (otherBook == GrammarBook.UNKNOWN)
			return text;
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
				final Map<String, String> otherSuttaMap = suttaListMap.get(getOtherBook(bookId));
				final Map<String, List<String>> otherMap = translationMap.get(bookId);
				result.append(line).append("\n");
				if (otherMap.containsKey(sutNum) && !otherMap.get(sutNum).isEmpty()) {
					result.append("\n<blockquote>\n");
					final String otherLabel = extraBooks.isEmpty() ? "" : "<div>[" + otherBook.getRef() + "]</div>\n";
					for (final String osnum : otherMap.get(sutNum)) {
						result.append(otherLabel + otherSuttaMap.get(osnum)).append("\n");
					}
					if (!extraBooks.isEmpty()) {
						// only in Mogg case, panc-t and niru can be added
						result.append(getExtraBookSutta(sutNum, extraBooks));
					}
					result.append("\n</blockquote>\n");
				}
				numFound = false;
			} else {
				result.append(line).append("\n");
			}
		}
		return result.toString();
	}

	public String getFirstText() {
		final String result;
		if (!bookList.isEmpty())
			result = textMap.get(bookList.get(0));
		else
			result = "";
		return result;
	}

	public List<String[]> getHeadList() {
		return getFirstHeadList();
	}

	public List<String[]> getHeadList(final GrammarBook bookId) {
		return headListMap.containsKey(bookId) ? headListMap.get(bookId) : Collections.emptyList();
	}

	public List<String[]> getFirstHeadList() {
		return headListMap.get(bookList.get(0));
	}

	public List<String> getNumList() {
		return getFirstNumList();
	}

	public List<String> getNumList(final GrammarBook bookId) {
		return numListMap.containsKey(bookId) ? numListMap.get(bookId) : Collections.emptyList();
	}

	public List<String> getFirstNumList() {
		return numListMap.isEmpty() ? Collections.emptyList() : numListMap.get(bookList.get(0));
	}

	public String getFormulaListAsString(final GrammarBook bookId, final boolean combineTwoBooks, final List<GrammarBook> extraBooks) {
		final StringBuilder result = new StringBuilder();
		final List<String> numList = numListMap.get(bookId);
		final Map<String, String> formulaMap = formulaListMap.get(bookId);
		if (combineTwoBooks) {
			if (!extraBooks.isEmpty()) {
				for (final GrammarBook book : extraBooks) {
					if (!textMap.containsKey(book))
						readExtraBook(book);
				}
			}
			final GrammarBook otherBook = getOtherBook(bookId);
			final Map<String, String> otherFormulaMap = formulaListMap.get(otherBook);
			final Map<String, List<String>> otherMap = translationMap.get(bookId);
			for (final String snum : numList) {
				final String formula = formulaMap.get(snum);
				result.append("\n<div>\n");
				result.append(formula).append("\n");
				if (otherMap.containsKey(snum) && !otherMap.get(snum).isEmpty()) {
					result.append("\n<blockquote>\n");
					final String otherLabel = extraBooks.isEmpty() ? "" : otherBook.getRef() + ": ";
					for (final String osnum : otherMap.get(snum)) {
						result.append(otherLabel + otherFormulaMap.get(osnum)).append("\n");
					}
					if (!extraBooks.isEmpty()) {
						// only in Mogg case, panc-t and niru can be added
						result.append(getExtraBookFormula(snum, extraBooks));
					}
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

	private String getExtraBookFormula(final String suttaNum, final List<GrammarBook> extraBooks) {
		final StringBuilder result = new StringBuilder();
		final Map<String, List<String>> moggNiruMap;
		if (extraBooks.indexOf(GrammarBook.NIRU) > -1)
			moggNiruMap = extraTransMap.get(GrammarBook.MOGG);
		else
			moggNiruMap = Collections.emptyMap();
		for (final GrammarBook book : extraBooks) {
			final Map<String, String> formulaMap = formulaListMap.get(book);
			final String sNum;
			if (book == GrammarBook.NIRU && moggNiruMap.containsKey(suttaNum)) {
				sNum = moggNiruMap.get(suttaNum).get(0);
			} else {
				sNum = suttaNum;
			}
			final String formula = formulaMap.get(sNum);
			if (formula != null && !formula.isEmpty()) {
				final String label = book.getRef() + ": ";
				result.append(label).append(formula).append("\n");
			}
		}
		return result.toString();
	}

	private String getExtraBookSutta(final String suttaNum, final List<GrammarBook> extraBooks) {
		final StringBuilder result = new StringBuilder();
		final Map<String, List<String>> moggNiruMap;
		if (extraBooks.indexOf(GrammarBook.NIRU) > -1)
			moggNiruMap = extraTransMap.get(GrammarBook.MOGG);
		else
			moggNiruMap = Collections.emptyMap();
		for (final GrammarBook book : extraBooks) {
			final Map<String, String> suttaMap = suttaListMap.get(book);
			final String sNum;
			if (book == GrammarBook.NIRU && moggNiruMap.containsKey(suttaNum)) {
				sNum = moggNiruMap.get(suttaNum).get(0);
			} else {
				sNum = suttaNum;
			}
			final String sutta = suttaMap.get(sNum);
			if (sutta != null && !sutta.isEmpty()) {
				final String label = "<div>[" + book.getRef() + "]</div>\n";
				result.append("<hr>\n").append(label).append(sutta).append("\n");
			}
		}
		return result.toString();
	}

}

