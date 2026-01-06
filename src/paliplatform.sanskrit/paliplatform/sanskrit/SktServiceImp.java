/*
 * SktServiceImp.java
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

import paliplatform.base.*;

import java.util.*;
import java.util.stream.*;
import java.util.function.UnaryOperator;

import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.scene.layout.HBox;

/** 
 * An implementation of Sanskrit service.
 * @author J.R. Bhaddacak
 * @version 3.7
 * @since 3.0
 */
public class SktServiceImp implements SktService {
	private Pane sktDictWin;
	private Tab sktDictTab;

	public SktServiceImp() {
	}

	@Override
	public Tab getSktDictTab() {
		sktDictTab = new Tab("Sanskrit Dictionaries");
		sktDictTab.disableProperty().bind(SanskritUtilities.sktDictDBAvailable.not());
		sktDictTab.setClosable(false);
		final TextIcon sktDictIcon = new TextIcon("skt-book", TextIcon.IconSet.CUSTOM);
		sktDictTab.setGraphic(sktDictIcon);
		final Button loadButton = new Button("Load");
		loadButton.setOnAction(actionEvent -> setSktDict());
		final HBox box = new HBox();
		box.setPadding(new Insets(5));
		box.getChildren().add(loadButton);
		sktDictTab.setContent(box);
		return sktDictTab;
	}

	private void setSktDict() {
		if (sktDictWin == null)
			sktDictWin = new SktDictWin(null);
		sktDictTab.setContent(sktDictWin);
	}

	@Override
	public void openSktDict(final String term) {
		final Object[] args = { term };
		SanskritUtilities.openWindow(Utilities.WindowType.SKTDICT, args);
	}

	@Override
	public void searchTerm(final String term) {
		if (sktDictWin != null) {
			((SktDictWin)sktDictWin).setSearchInput(term);
		}
	}

	@Override
	public boolean isSktDictAvailable(final String dictCode) {
		final SanskritUtilities.SktDictBook dict = SanskritUtilities.SktDictBook.valueOf(dictCode);
		return dict == null
				? false
				: SanskritUtilities.sktDictAvailMap.get(dict).get();
	}

	@Override
	public List<String> getSktDictTerms(final String dictCode) {
		final List<String> result = new ArrayList<>();
		if (isSktDictAvailable(dictCode)) {
			final String select = "SELECT KEY1 FROM " + dictCode + " ORDER BY ID;";
			final Set<String> terms = Utilities.getFirstColumnFromDB(Utilities.H2DB.SKTDICT, select);
			result.addAll(terms);
		}
		return result;
	}

	@Override
	public List<String> getSktDictMeaning(final String dictCode, final String term) {
		final List<String> result;
		if (isSktDictAvailable(dictCode)) {
			final SanskritUtilities.SktDictBook dict = SanskritUtilities.SktDictBook.valueOf(dictCode);
			if (dict != null) {
				final UnaryOperator<String> format = dict == SanskritUtilities.SktDictBook.MW
														? x -> SktDictWin.formatMWMeaning(x)
														: dict == SanskritUtilities.SktDictBook.AP
															? x -> SktDictWin.formatAPMeaning(x)
															: dict == SanskritUtilities.SktDictBook.SHS
																? x -> SktDictWin.formatSHSMeaning(x)
																: dict == SanskritUtilities.SktDictBook.MD
																	? x -> SktDictWin.formatMDMeaning(x)
																	: x -> SktDictWin.formatBHSMeaning(x);
				final List<SktDictEntry> resList = SanskritUtilities.lookUpSktDictFromDB(dict, term);
				result = resList.stream()
								.map(x -> Utilities.removeTags(extraFormatFix(format.apply(x.getMeaning()))))
								.collect(Collectors.toList());
			} else {
				result = Collections.emptyList();
			}
		} else {
			result = Collections.emptyList();
		}
		return result;
	}

	private String extraFormatFix(final String input) {
		return input.replace("<br>", "\n");
	}

}

