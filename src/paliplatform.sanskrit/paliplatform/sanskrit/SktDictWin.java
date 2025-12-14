/*
 * SktDictWin.java
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

import paliplatform.sanskrit.SanskritUtilities.SktDictBook;
import paliplatform.base.*;

import java.util.*;
import java.util.regex.*;
import java.util.stream.Collectors;

import javafx.scene.control.*;
import javafx.application.Platform;

/**
 * The sanskrit dictionary window's pane.
 * @author J.R. Bhaddacak
 * @version 3.6
 * @since 3.5
 */
public class SktDictWin extends DictWinBase {

	public SktDictWin(final Object[] args) {
		// initialization
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
		infoPopup.setTextWidth(Utilities.getRelativeSize(32));
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
				dbQuery = "SELECT KEY1 FROM " + dicBook.toString() + " WHERE MEANING LIKE '%" + properQuery + "%' ORDER BY ID;";
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
			final SktDictBook db = dicts.get(i);
			result.append("<p class=bookname>&lt;").append(db.bookName).append("&gt;</p>");
				final String res = getResultArticle(SanskritUtilities.lookUpSktDictFromDB(db, term));
				result.append(res);
			if (i < dicts.size() - 1)
				result.append("<div class=hrule></div><p></p>");
		}
		htmlViewer.setContent(Utilities.makeHTML(result.toString()));
	}

	private String getResultArticle(final List<SktDictEntry> items) {
		final StringBuilder text = new StringBuilder();
		for (final SktDictEntry entry : items) {
			final String hNum = entry.getHnum();
			final String hNumStr = hNum.isEmpty() ? "" : "<sup>" + hNum + "</sup>";
			text.append("<h3>" + entry.getKey2() + hNumStr + "</h3>");
			text.append("<p>");
			String meaning = entry.getMeaning().replace("&", "&amp;");
			meaning = meaning.replace("¦", "•");
			text.append(formatMeaning(meaning));
			text.append("</p>");
			text.append("<p></p>");
		}
		return text.toString();
	}

	private String formatMeaning(final String input) {
		String result = input;
		// remove mere info tags
		final Pattern infoPatt = Pattern.compile("<[^>]+/>");
		result = infoPatt.matcher(result).replaceAll("");
		// change other tags to span
		// 1st round, save span to restore later
		final Pattern tagPatt = Pattern.compile("<([^> ]+)( *[^>]*)>(.*?)</\\1>");
		result = tagPatt.matcher(result).replaceAll("<::span:: class='$1'$2>$3</::span::>");
		// 2nd round for nested tags
		final Pattern tagPatt2 = Pattern.compile("<(.{1,4})( *[^>]*)>(.*?)</\\1>");
		result = tagPatt2.matcher(result).replaceAll("<::span:: class='$1'$2>$3</::span::>");
		// restore span
		result = result.replace("::span::", "span");
		return result;
	}

}
