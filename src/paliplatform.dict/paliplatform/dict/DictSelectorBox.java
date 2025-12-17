/*
 * DictSelectorBox.java
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

package paliplatform.dict;

import paliplatform.base.*;

import javafx.scene.control.*;
import javafx.scene.layout.HBox;

/** 
 * The box for Pāli dict selection.
 * @author J.R. Bhaddacak
 * @version 3.6
 * @since 3.6
 */
public class DictSelectorBox extends HBox {
	public DictSelectorBox() {
		setSpacing(5);
		for (final DictUtilities.DictBook d : DictUtilities.DictBook.books) {
			final CheckBox cb = createDictCheckBox(d);
			getChildren().add(cb);
		}
	}

	private CheckBox createDictCheckBox(final DictUtilities.DictBook book) {
		final String strDictSet = Utilities.getSetting("dictset");
		final String name = book.toString();
		final CheckBox cb = DictUtilities.createDictCheckBox(book);
		cb.setSelected(strDictSet.contains(name));
		cb.setOnAction(actionEvent -> {
			final String dset = Utilities.getSetting("dictset").replace(name + ",", "");
			if (cb.isSelected())
				Utilities.setSetting("dictset", dset + name + ",");
			else
				Utilities.setSetting("dictset", dset);
			MainProperties.INSTANCE.saveSettings();			
		});
		return cb;
	}

}

