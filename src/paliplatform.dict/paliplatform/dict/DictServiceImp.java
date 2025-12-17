/*
 * DictServiceImp.java
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

import paliplatform.base.*;

import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.scene.layout.HBox;

/** 
 * An implementation of Dict service.
 * @author J.R. Bhaddacak
 * @version 3.6
 * @since 3.0
 */
public class DictServiceImp implements DictService {
	private Pane dictWin;
	private Tab dictTab;

	public DictServiceImp() {
	}

	@Override
	public Tab getDictTab() {
		dictTab = new Tab("Pāli Dictionaries");
		dictTab.disableProperty().bind(DictUtilities.someDictAvailable.not());
		dictTab.setClosable(false);
		final TextIcon dictIcon = new TextIcon("book", TextIcon.IconSet.AWESOME);
		dictTab.setGraphic(dictIcon);
		final Button loadButton = new Button("Load");
		loadButton.setOnAction(actionEvent -> setDict());
		final HBox box = new HBox();
		box.setPadding(new Insets(5));
		box.getChildren().add(loadButton);
		dictTab.setContent(box);
		return dictTab;
	}

	private void setDict() {
		if (dictWin == null)
			dictWin = new DictWin(null);
		dictTab.setContent(dictWin);
	}

	@Override
	public void searchTerm(final String term) {
		if (dictWin != null) {
			((DictWin)dictWin).setSearchInput(term);
		}
	}

}

