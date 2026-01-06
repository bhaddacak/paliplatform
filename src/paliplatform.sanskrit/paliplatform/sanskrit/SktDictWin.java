/*
 * SktDictWin.java
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

package paliplatform.sanskrit;

import paliplatform.sanskrit.SanskritUtilities.SktDictBook;
import paliplatform.base.*;
import paliplatform.base.ScriptTransliterator.EngineType;

import java.util.*;
import java.util.regex.*;
import java.util.stream.Collectors;

import javafx.scene.control.*;
import javafx.application.Platform;

/**
 * The sanskrit dictionary window's pane.
 * @author J.R. Bhaddacak
 * @version 4.0
 * @since 3.5
 */
public class SktDictWin extends DictWinBase {
	private static final String[] shsXrefMapArr = {
		"2187", "apāc", "3938", "avāc", "4913", "ā", "6939", "udaka",
		"6964", "udac", "17495", "tiryyac", "23294", "parāñc", "26826", "pratyac",
		"27762", "prāc", "37845", "viṣvac", "31847", "viṣvadryac", "43087", "samyac",
		"46719", "havā" };
	private static final Map<String, String> shsXrefMap = new HashMap<>();
	private static final Pattern tagPatt = Pattern.compile("<([^> ]+)( *[^>]*)>(.*?)</\\1>");
	private static final Pattern slp1Patt = Pattern.compile("\\{#(.*?)#\\}");
	private static final Pattern italicPatt = Pattern.compile("\\{%(.*?)%\\}");
	private static final Pattern boldPatt = Pattern.compile("\\{@(.*?)@\\}");
	private static final Pattern unreadablePatt = Pattern.compile("\\{\\?(.*?)\\?\\}");
	private static final Pattern infoPatt = Pattern.compile("<[^>]+/>");
	private static final Pattern poemPatt = Pattern.compile("<Poem>(.*?)</Poem>");

	public SktDictWin(final Object[] args) {
		// initialization
		if (shsXrefMap.isEmpty()) {
			for (int i = 0; i < shsXrefMapArr.length; i += 2) {
				shsXrefMap.put(shsXrefMapArr[i], shsXrefMapArr[i + 1]);
			}
		}
		if (SanskritUtilities.simpleServiceMap == null) 
			SanskritUtilities.simpleServiceMap = SanskritUtilities.getSimpleServices();
		for (final SktDictBook d : SktDictBook.books) {
			final CheckBox cb =  createDictCheckBox(d);
			dictCBMap.put(d, cb);
			toolBar.getItems().add(cb);
		}
		searchInput.setSanskritMode(true);
		final String inputMethod = Utilities.getSetting("sanskrit-input-method");
		searchInput.setInputMethod(PaliTextInput.InputMethod.valueOf(inputMethod));
		editorLauncher = (SimpleService)SanskritUtilities.simpleServiceMap.get("paliplatform.main.EditorLauncher");
		editorButton.setDisable(editorLauncher == null);
		htmlViewer.setStyleSheet(Utilities.SKTDICT_CSS);
		fxHandler = new SktDictFXHandler(htmlViewer, this);
		// prepare info popup
		infoPopup.setContentWithText(SanskritUtilities.getTextResource("info-sanskrit-dictionaries.txt"));
		infoPopup.setTextWidth(Utilities.getRelativeSize(36));
		init(args);
	}

	@Override
	public final void init(final Object[] args) {
		dictSet.clear();
		final String strDictSet = Utilities.getSetting("sktdictset");
		final String[] arrDictSet = strDictSet.split(",");
		for (final SktDictBook db : SktDictBook.books)
			dictCBMap.get(db).setSelected(false);
		for (final String s : arrDictSet) {
			if (SktDictBook.isValid(s)) {
				final SktDictBook db = SktDictBook.valueOf(s);
				dictSet.add(db);
				dictCBMap.get(db).setSelected(true);
			}
		}
		htmlViewer.setContent(Utilities.makeHTML(""));
		if (args != null) {
			final String term = (String)args[0];
			if (!term.isEmpty())
				searchTextField.setText(term);
		} else {
			searchTextField.clear();
			resultList.clear();
			resultMap.clear();
		}
		searchTextField.requestFocus();
		findBox.init();
		Platform.runLater(() -> resultPane.setBottom(null));			
	}

	private CheckBox createDictCheckBox(final SktDictBook book) {
		final CheckBox cb = SanskritUtilities.createSktDictCheckBox(book);
		cb.disableProperty().bind(SanskritUtilities.sktDictAvailMap.get(book).not());
		cb.setOnAction(actionEvent -> {
			if (cb.isSelected())
				dictSet.add(book);
			else
				dictSet.remove(book);
			search();
		});
		return cb;
	}
	
	@Override
	public String processInput(final String query) {
		return Utilities.convertToRomanSanskrit(query);
	}

	@Override
	public void search(final String query) {
		resultMap.clear();
		// remove single qoute causing SQL error
		final String properQuery = query.replace("'", "");
		String termQuery = properQuery;
		if (useWildcards.get()) {
			if (termQuery.indexOf('?') >= 0)
				termQuery = termQuery.replace("?", "_");
			if (termQuery.indexOf('*') >= 0)
				termQuery = termQuery.replace("*", "%");
			final int uCount = Utilities.charCount(termQuery, '_');
			final int pCount = Utilities.charCount(termQuery, '%');
			// just * or a sheer combination of ? and * is not allowed
			if (termQuery.length() == 0 || (pCount > 0 && pCount + uCount == termQuery.length()))
				return;
		}
		for (final Object book : dictSet) {
			final SktDictBook dicBook = (SktDictBook)book;
			final String orderBy = "ORDER BY ID;";
			final String tail = useWildcards.get() ? "' " + orderBy : "%' " + orderBy;
			String dbQuery = "SELECT KEY1 FROM " + dicBook.toString() + " WHERE KEY1 LIKE '" + termQuery + tail;
			if (inMeaning.get()) {
				// make 2 queries, all lowercase and title case
				final String[] inMQuery = { properQuery, Character.toUpperCase(properQuery.charAt(0)) + properQuery.substring(1) };
				dbQuery = "SELECT KEY1 FROM " + dicBook.toString() + 
					" WHERE MEANING LIKE '%" + inMQuery[0] + "%' OR MEANING LIKE '%" + inMQuery[1] +
					"%' ORDER BY ID;";
			}
			final Set<String> results = Utilities.getFirstColumnFromDB(Utilities.H2DB.SKTDICT, dbQuery);
			for (final String term : results) {
				final ArrayList<Object> dList;
				if (resultMap.containsKey(term))
					dList = resultMap.get(term);
				else
					dList = new ArrayList<>();
				dList.add(dicBook);
				resultMap.put(term, dList);
			}
		} // end for
		if (dictSet.size() > 1)
			resultList.setAll(resultMap.keySet().stream().sorted(Utilities.sktComparator).collect(Collectors.toList()));
		else
			resultList.setAll(resultMap.keySet());
		resultListView.scrollTo(0);
		if (!resultList.isEmpty()) {
			showResult(resultList.get(0));
			initialStringToLocate = inMeaning.get() ? properQuery : "";
		}
	}

	@Override
	public void showResult(final String term) {
		final StringBuilder result = new StringBuilder();
		final ArrayList<SktDictBook> dicts = resultMap.get(term).stream()
										.map(x -> (SktDictBook)x)
										.sorted(Comparator.comparing(x -> x.ordinal()))
										.collect(Collectors.toCollection(ArrayList::new));
		if (dicts == null) return;
		// show head word
		result.append("<h1>").append(term).append("</h1>");
		for (int i = 0; i < dicts.size(); i++) {
			final SktDictBook dict = dicts.get(i);
			result.append("<p class=bookname>&lt;").append(dict.bookName).append("&gt;</p>");
				final String res = getResultArticle(dict, term);
				result.append(res);
			if (i < dicts.size() - 1)
				result.append("<div class=hrule></div><p></p>");
		}
		htmlViewer.setContent(Utilities.makeHTML(result.toString()));
	}

	private String getResultArticle(final SktDictBook dict, final String term) {
		final List<SktDictEntry> items = SanskritUtilities.lookUpSktDictFromDB(dict, term);
		final StringBuilder text = new StringBuilder();
		for (final SktDictEntry entry : items) {
			final String hNum = entry.getHnum();
			final String hNumStr = hNum.isEmpty() ? "" : "<sup>" + hNum + "</sup>";
			final String k2 = entry.getKey2();
			final String k2Str = dict.keySLP1DecodeNeeded()
								? ScriptTransliterator.translitQuick(k2, EngineType.SLP1_IAST, true)
								: k2;
			text.append("<h3>" + k2Str + hNumStr + "</h3>");
			text.append("<p>");
			String meaning = entry.getMeaning().replace("&", "&amp;");
			text.append(formatMeaning(dict, meaning));
			text.append("</p>");
			text.append("<p></p>");
		}
		return text.toString();
	}

	private String formatMeaning(final SktDictBook dict, final String input) {
		String result = input;
		switch (dict) {
			case MW:
				result = formatMWMeaning(input);
				break;
			case AP:
				result = formatAPMeaning(input);
				break;
			case SHS:
				result = formatSHSMeaning(input);
				break;
			case MD:
				result = formatMDMeaning(input);
				break;
			case BHS:
				result = formatBHSMeaning(input);
				break;
			case MWE:
				result = formatMWEMeaning(input);
				break;
			case AE:
				result = formatAEMeaning(input);
				break;
			case BOR:
				result = formatBORMeaning(input);
				break;
		}
		return result;
	}

	public static String formatMWMeaning(final String input) {
		String result = input;
		// bullet
		result = result.replace("¦", " •");
		// remove mere info tags
		result = infoPatt.matcher(result).replaceAll("");
		// change other tags to span
		// 1st round, save span to restore later
		result = tagPatt.matcher(result).replaceAll("<::span:: class='$1'$2>$3</::span::>");
		// 2nd round for nested tags
		final Pattern tagPatt2 = Pattern.compile("<(.{1,4})( *[^>]*)>(.*?)</\\1>");
		result = tagPatt2.matcher(result).replaceAll("<::span:: class='$1'$2>$3</::span::>");
		// restore span
		result = result.replace("::span::", "span");
		return result;
	}

	public static String formatAPMeaning(final String input) {
		String result = input;
		final Map<String, String> xrefMap = Map.of( "5008", "avadhātavya", "6060", "asamāvṛttaḥ" );
		// dash
		result = result.replace("¦", "—");
		// decode SLP1
		result = slp1Patt.matcher(result).replaceAll(m -> 
				"<i>" + ScriptTransliterator.translitQuick(m.group(1), EngineType.SLP1_IAST, true) + "</i>");
		// italic
		result = italicPatt.matcher(result).replaceAll("<i>$1</i>");
		// bold bullet
		final Pattern boldBullPatt = Pattern.compile("[.-]\\{@(.*?)@\\}");
		result = boldBullPatt.matcher(result).replaceAll("<br> • <b>$1</b>");
		// bold bullet paren
		final Pattern boldBullParPatt = Pattern.compile("[.-]\\(\\{@(.*?)@\\}\\)");
		result = boldBullParPatt.matcher(result).replaceAll("<br> • (<b>$1</b>)");
		// bold inline
		final Pattern boldInlinePatt = Pattern.compile("\\{@(.*?)@\\}");
		result = boldInlinePatt.matcher(result).replaceAll("<b>$1</b>");
		// ² and ³ bullet
		result = result.replace(".²", "<br> • ²").replace(".³", "<br> • ³");
		// <Poem>
		result = poemPatt.matcher(result).replaceAll("<p class='poem'>$1</p>");
		// LBody
		final Pattern lbodyPatt = Pattern.compile("\\{\\{Lbody=(.*?)\\}\\}");
		result = lbodyPatt.matcher(result).replaceAll(m -> {
			final String xref = xrefMap.getOrDefault(m.group(1), "");
			return xref.isEmpty() ? "" : "See <i>" + xref + ".</i>";
		});
		return result;
	}

	public static String formatSHSMeaning(final String input) {
		String result = input;
		// dash
		result = result.replace("¦", "—");
		// decode SLP1
		result = slp1Patt.matcher(result).replaceAll(m -> 
				"<i>" + ScriptTransliterator.translitQuick(m.group(1), EngineType.SLP1_IAST, true) + "</i>");
		// italic
		result = italicPatt.matcher(result).replaceAll("<i>$1</i>");
		// unreadable
		result = unreadablePatt.matcher(result).replaceAll("($1?)");
		// <Poem>
		result = poemPatt.matcher(result).replaceAll("<p class='poem'>$1</p>");
		// LBody
		final Pattern lbodyPatt = Pattern.compile("\\{\\{Lbody=(.*?)\\}\\}");
		result = lbodyPatt.matcher(result).replaceAll(m -> {
			final String xref = shsXrefMap.getOrDefault(m.group(1), "");
			return xref.isEmpty() ? "" : "See <i>" + xref + ".</i>";
		});
		return result;
	}

	public static String formatMDMeaning(final String input) {
		String result = input;
		// dash
		result = result.replace("¦", "—");
		// tags
		result = tagPatt.matcher(result).replaceAll("<span class='$1'$2>$3</span>");
		// decode SLP1
		result = slp1Patt.matcher(result).replaceAll(m -> 
				"<i>" + ScriptTransliterator.translitQuick(m.group(1), EngineType.SLP1_IAST, true) + "</i>");
		// italic
		result = italicPatt.matcher(result).replaceAll("<i>$1</i>");
		// bold
		result = boldPatt.matcher(result).replaceAll("<b>$1</b>");
		// unwanted char
		result = result.replace("🞄", "");
		return result;
	}

	public static String formatBHSMeaning(final String input) {
		String result = input;
		// dash
		result = result.replace("¦", "—");
		// tags
		result = tagPatt.matcher(result).replaceAll("<span class='$1'$2>$3</span>");
		// italic
		result = italicPatt.matcher(result).replaceAll("<i>$1</i>");
		// bold
		result = boldPatt.matcher(result).replaceAll("<b>$1</b>");
		return result;
	}

	private static String formatMWEMeaning(final String input) {
		String result = input;
		// dash bullet
		result = result.replace("—", "<br> —");
		// dash
		result = result.replace("¦", "—");
		// decode SLP1
		result = slp1Patt.matcher(result).replaceAll(m -> 
				"<i>" + ScriptTransliterator.translitQuick(m.group(1), EngineType.SLP1_IAST, true) + "</i>");
		// italic
		result = italicPatt.matcher(result).replaceAll("<i>$1</i>");
		return result;
	}

	private static String formatAEMeaning(final String input) {
		String result = input;
		// dash
		result = result.replace("¦", "—");
		// remove mere info tags
		result = infoPatt.matcher(result).replaceAll("");
		// decode SLP1
		result = slp1Patt.matcher(result).replaceAll(m -> 
				"<i>" + ScriptTransliterator.translitQuick(m.group(1), EngineType.SLP1_IAST, true) + "</i>");
		// italic
		result = italicPatt.matcher(result).replaceAll("<i>$1</i>");
		// bold
		result = boldPatt.matcher(result).replaceAll("<b>$1</b>");
		return result;
	}

	private static String formatBORMeaning(final String input) {
		String result = input;
		// dash
		result = result.replace("¦", "—");
		// decode SLP1
		result = slp1Patt.matcher(result).replaceAll(m -> 
				"<i>" + ScriptTransliterator.translitQuick(m.group(1), EngineType.SLP1_IAST, true) + "</i>");
		// italic
		result = italicPatt.matcher(result).replaceAll("<i>$1</i>");
		// bold
		result = boldPatt.matcher(result).replaceAll("<b>$1</b>");
		// <ls>
		final Pattern lsPatt = Pattern.compile("<ls>(.*?)</ls>");
		result = lsPatt.matcher(result).replaceAll("<span class='ls'>$1</span>");
		return result;
	}

}
