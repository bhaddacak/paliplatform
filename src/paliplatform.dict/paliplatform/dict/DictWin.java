/*
 * DictWin.java
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

package paliplatform.dict;

import paliplatform.dict.DictUtilities.DictBook;
import paliplatform.base.*;

import java.util.*;
import java.util.stream.Collectors;

import javafx.scene.control.*;
import javafx.application.Platform;

/**
 * The main dictionary window's pane.
 * @author J.R. Bhaddacak
 * @version 4.1
 * @since 2.0
 */
public class DictWin extends DictWinBase {

	public DictWin(final Object[] args) {
		// initialization
		if (DictUtilities.simpleServiceMap == null) 
			DictUtilities.simpleServiceMap = DictUtilities.getSimpleServices();
		for (final DictBook d : DictBook.books) {
			final CheckBox cb =  createDictCheckBox(d);
			dictCBMap.put(d, cb);
			toolBar.getItems().add(cb);
		}
		editorLauncher = (SimpleService)DictUtilities.simpleServiceMap.get("paliplatform.main.EditorLauncher");
		editorButton.setDisable(editorLauncher == null);
		htmlViewer.setStyleSheet(Utilities.DICT_CSS);
		fxHandler = new DictFXHandler(htmlViewer, this);
		// prepare info popup
		infoPopup.setContentWithText(DictUtilities.getTextResource("info-dictionaries.txt"));
		infoPopup.setTextWidth(Utilities.getRelativeSize(37.5));
		init(args);
	}

	@Override
	public final void init(final Object[] args) {
		dictSet.clear();
		final String strDictSet = Utilities.getSetting("dictset");
		final String[] arrDictSet = strDictSet.split(",");
		for (final DictBook db : DictBook.books)
			dictCBMap.get(db).setSelected(false);
		for (final String s : arrDictSet) {
			if (DictBook.isValid(s)) {
				final DictBook db = DictBook.valueOf(s);
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
		if (!DictUtilities.someDictAvailable.get())
			Utilities.displayAlert(Alert.AlertType.ERROR, "No data to display,\nplease create dict data first");
		findBox.init();
		Platform.runLater(() -> resultPane.setBottom(null));			
	}

	private CheckBox createDictCheckBox(final DictBook book) {
		final CheckBox cb = DictUtilities.createDictCheckBox(book);
		cb.disableProperty().bind(DictUtilities.dictAvailMap.get(book).not());
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
		return Utilities.convertToRomanPali(query);
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
			final DictBook dicBook = (DictBook)book;
			final String orderBy = "ORDER BY ID;";
			final String tail = useWildcards.get() ? "' " + orderBy : "%' " + orderBy;
			String dbQuery = dicBook == DictBook.MDPD
							? "SELECT TERM FROM " + Utilities.PpdpdTable.DICTIONARY.toString() + " WHERE TERM LIKE '" + termQuery + tail
							: dicBook == DictBook.CONE
								? "SELECT TERM FROM DICT WHERE TERM LIKE '%|" + termQuery + tail
								: "SELECT TERM FROM " + dicBook.toString() + " WHERE TERM LIKE '" + termQuery + tail;
			if (inMeaning.get()) {
				if (dicBook == DictBook.CPED) {
					dbQuery = "SELECT TERM FROM CPED WHERE MEANING LIKE '%" + properQuery + "%' " +
							"OR SUBMEANING LIKE '%" + properQuery + "%' OR POS LIKE '%" + properQuery + "%' " +
							"ORDER BY ID;";
				} else if (dicBook == DictBook.MDPD) {
					dbQuery = "SELECT LEMMA_1 FROM " + Utilities.PpdpdTable.MINIDPD.toString() +
							" WHERE MEANING_1 LIKE '%" + properQuery + "%' " +
							"OR MEANING_2 LIKE '%" + properQuery + "%' OR MEANING_LIT LIKE '%" + properQuery + "%' " +
							"ORDER BY ID;";
				} else if (dicBook == DictBook.CONE) {
					dbQuery = "SELECT TERM FROM DICT WHERE MEANING LIKE '%" + properQuery + "%' ORDER BY ID;";
				} else {
					final String mfield = dicBook == DictBook.NCPED ? "DEFINITION" : "MEANING";
					// make 2 queries, all lowercase and title case
					final String[] inMQuery = { properQuery, Character.toUpperCase(properQuery.charAt(0)) + properQuery.substring(1) };
					dbQuery = "SELECT TERM FROM " + dicBook.toString() + 
							" WHERE " + mfield + " LIKE '%" + inMQuery[0] + "%' OR " + mfield + " LIKE '%" + inMQuery[1] +
							"%' ORDER BY ID;";
				}
			}
			final Set<String> results = dicBook == DictBook.MDPD
										? Utilities.getFirstColumnFromDB(Utilities.H2DB.PPDPD, dbQuery)
										: dicBook == DictBook.CONE
											? Utilities.getFirstColumnFromDB(Utilities.H2DB.CONE, dbQuery)
											: Utilities.getFirstColumnFromDB(Utilities.H2DB.DICT, dbQuery);
			for (final String term : results) {
				final String termOK = term.startsWith("|") ? term.substring(1) : term;
				final ArrayList<Object> dList;
				if (resultMap.containsKey(termOK))
					dList = resultMap.get(termOK);
				else
					dList = new ArrayList<>();
				dList.add(dicBook);
				resultMap.put(termOK, dList);
			}
		} // end for
		if (dictSet.size() > 1)
			resultList.setAll(resultMap.keySet().stream().sorted(Utilities.paliComparator).collect(Collectors.toList()));
		else
			resultList.setAll(resultMap.keySet());
		resultListView.scrollTo(0);
		if (!resultList.isEmpty()) {
			showResult(resultList.get(0));
			showMessage(resultList.size() + " found");
			initialStringToLocate = inMeaning.get() ? properQuery : "";
		}
	}

	@Override
	public void showResult(final String term) {
		final StringBuilder result = new StringBuilder();
		final ArrayList<DictBook> dicts = resultMap.get(term).stream()
										.map(x -> (DictBook)x)
										.sorted(Comparator.comparing(x -> x.ordinal()))
										.collect(Collectors.toCollection(ArrayList::new));
		if (dicts == null) return;
		// show head word
		result.append("<h1>").append(term).append("</h1>");
		for (int i = 0; i < dicts.size(); i++) {
			final DictBook db = dicts.get(i);
			result.append("<p class=bookname>&lt;").append(db.bookName).append("&gt;</p>");
			if (db == DictBook.CPED) {
				result.append(getCPEDArticle(DictUtilities.lookUpCPEDFromDB(term)));
			} else if (db == DictBook.NCPED) {
				result.append(DictUtilities.formatNCPEDMeaning(DictUtilities.lookUpNCPEDFromDB(term)));
			} else if (db == DictBook.MDPD) {
				if (inMeaning.get()) {
					// search in minidpd with lemma_1
					result.append(DictUtilities.formatMDPDMeaning(DictUtilities.lookUpMDPDFromDBWithLemma(term)));
				} else {
					// search in minidpd with a list of term ids
					result.append(DictUtilities.formatMDPDMeaning(DictUtilities.lookUpMDPDFromDBWithTerm(term)));
				}
			} else {
				final String res = db == DictBook.CONE
									? getConeResultArticle(DictUtilities.lookUpConeFromDB(term))
									: getResultArticle(DictUtilities.lookUpDictFromDB(db, term));
				if (db == DictBook.PTSD)
					result.append(res.replaceAll("href=[^>]*", ""));
				else if (db == DictBook.DPPN)
					result.append(res.replace("<hr>", ""));
				else
					result.append(res);
			}
			if (i < dicts.size() - 1)
				result.append("<div class=hrule></div><p></p>");
		}
		htmlViewer.setContent(Utilities.makeHTML(result.toString()));
	}

	private String getCPEDArticle(final PaliWord item) {
		String result = DictUtilities.formatCPEDMeaning(item, true);
		if (item.isDeclinable()) {
			result = result + "<p><a class=linkbutton onClick=openDeclension('"+item.getTerm()+"')>Show declension</a></p>";
		}
		return result;
	}

	private String getResultArticle(final List<String> items) {
		final StringBuilder text = new StringBuilder();
		for (final String s : items) {
			String sOK = s.replace("&apos;", "'");
			text.append(sOK);
			text.append("<p></p>");
		}
		return text.toString();
	}

	private String getConeResultArticle(final List<String> items) {
		final StringBuilder text = new StringBuilder();
		for (final String s : items) {
			String sOK = s.replace("\\n", "");
			sOK = sOK.replace("\\t", "\t");
			sOK = sOK.replace("—", "<br>&nbsp;&nbsp;&nbsp;&nbsp;—");
			text.append(sOK);
			text.append("<p></p>");
		}
		return text.toString();
	}

}
