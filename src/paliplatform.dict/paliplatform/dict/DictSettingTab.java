/*
 * DictSettingTab.java
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

package paliplatform.dict;

import paliplatform.base.*;

import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;

/** 
 * The tab for Dict settings.
 * @author J.R. Bhaddacak
 * @version 3.0
 * @since 3.0
 */
public class DictSettingTab extends Tab {
	public DictSettingTab() {
		setText("Dictionaries");
		setClosable(false);
		final VBox dictMainBox = new VBox();
		dictMainBox.setSpacing(5);
		dictMainBox.setPadding(new Insets(10));
		VBox.setVgrow(dictMainBox, Priority.ALWAYS);
		final HBox dictBookBox = new HBox();
		dictBookBox.setSpacing(5);
		for (final DictUtilities.DictBook d : DictUtilities.DictBook.books) {
			final CheckBox cb = createDictCheckBox(d);
			dictBookBox.getChildren().add(cb);
		}
		dictMainBox.getChildren().addAll(new Label("Default inclusion of dictionaries"), dictBookBox);
		setContent(dictMainBox);
	}

	private CheckBox createDictCheckBox(final DictUtilities.DictBook book) {
		final String strDictSet = Utilities.settings.getProperty("dictset");
		final String name = book.toString();
		final CheckBox cb = DictUtilities.createDictCheckBox(book);
		cb.setSelected(strDictSet.contains(name));
		cb.setOnAction(actionEvent -> {
			final String dset = Utilities.settings.getProperty("dictset").replace(name + ",", "");
			if (cb.isSelected())
				Utilities.settings.setProperty("dictset", dset + name + ",");
			else
				Utilities.settings.setProperty("dictset", dset);
			MainProperties.INSTANCE.saveSettings();			
		});
		return cb;
	}

}

