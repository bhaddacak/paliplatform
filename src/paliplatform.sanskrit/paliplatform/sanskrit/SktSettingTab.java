/*
 * SktSettingTab.java
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

import paliplatform.base.*;

import java.util.*;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;

/** 
 * The tab for the settings of Sanskrit module.
 * @author J.R. Bhaddacak
 * @version 3.5
 * @since 3.5
 */
public class SktSettingTab extends Tab {
	public SktSettingTab() {
		setText("Sanskrit");
		setClosable(false);
		final VBox sktMainBox = new VBox();
		sktMainBox.setSpacing(5);
		sktMainBox.setPadding(new Insets(10));
		VBox.setVgrow(sktMainBox, Priority.ALWAYS);
		// default sanskrit input method
		final HBox defMethodBox = new HBox();
		defMethodBox.setSpacing(5);
		defMethodBox.getChildren().add(new Label("Default input method: "));
		final ToggleGroup defMethodGroup = new ToggleGroup();
		final List<PaliTextInput.InputMethod> inputMethods = List.of(
			PaliTextInput.InputMethod.COMPOSITE, PaliTextInput.InputMethod.NORMAL);
		final Map<PaliTextInput.InputMethod, RadioButton> inputRadioMap = new EnumMap<>(PaliTextInput.InputMethod.class);
		for (final PaliTextInput.InputMethod im : inputMethods) {
			final RadioButton radio = new RadioButton(im.getName());
			radio.setUserData(im);
			radio.setToggleGroup(defMethodGroup);
			defMethodBox.getChildren().add(radio);
			inputRadioMap.put(im, radio);
		}
		final String sktInputMethodStr = Utilities.settings.getProperty("sanskrit-input-method", "COMPOSITE");
		final PaliTextInput.InputMethod sktInputMethod = PaliTextInput.InputMethod.valueOf(sktInputMethodStr.toUpperCase());
		defMethodGroup.selectToggle(inputRadioMap.get(sktInputMethod));
        defMethodGroup.selectedToggleProperty().addListener((observable) -> {
			if (defMethodGroup.getSelectedToggle() != null) {
				final RadioButton selected = (RadioButton)defMethodGroup.getSelectedToggle();
				final PaliTextInput.InputMethod inputMethod = (PaliTextInput.InputMethod)selected.getUserData();
				Utilities.settings.setProperty("sanskrit-input-method", inputMethod.toString());
				MainProperties.INSTANCE.saveSettings();
			}
		});
		// default skt dict selection
		final HBox dictBookBox = new HBox();
		dictBookBox.setSpacing(5);
		for (final SanskritUtilities.SktDictBook d : SanskritUtilities.SktDictBook.books) {
			final CheckBox cb = createDictCheckBox(d);
			dictBookBox.getChildren().add(cb);
		}
		sktMainBox.getChildren().addAll(defMethodBox, new Separator(), new Label("Default inclusion of dictionaries"), dictBookBox);
		setContent(sktMainBox);
	}

	private CheckBox createDictCheckBox(final SanskritUtilities.SktDictBook book) {
		final String strDictSet = Utilities.settings.getProperty("sktdictset");
		final String name = book.toString();
		final CheckBox cb = SanskritUtilities.createSktDictCheckBox(book);
		cb.setSelected(strDictSet.contains(name));
		cb.setOnAction(actionEvent -> {
			final String dset = Utilities.settings.getProperty("sktdictset").replace(name + ",", "");
			if (cb.isSelected())
				Utilities.settings.setProperty("sktdictset", dset + name + ",");
			else
				Utilities.settings.setProperty("sktdictset", dset);
			MainProperties.INSTANCE.saveSettings();			
		});
		return cb;
	}

}

