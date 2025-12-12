/*
 * SanskritMenu.java
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

package paliplatform.sanskrit;

import paliplatform.base.*;

import javafx.scene.control.*;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCode;

/** 
 * The menu items for Sanskrit module.
 * @author J.R. Bhaddacak
 * @version 3.5
 * @since 3.5
 */
public class SanskritMenu extends Menu {
	public SanskritMenu() {
		super("Sans_krit");
		setMnemonicParsing(true);
		// init
		Utilities.initializeSktDictDB(false);
		SanskritUtilities.initializeSktDictAvailMap();
		SanskritUtilities.updateSktDictAvailibility();
		// add menu items
		final MenuItem dictMenuItem = new MenuItem("_Dictionaries", new TextIcon("book", TextIcon.IconSet.AWESOME));
		dictMenuItem.setMnemonicParsing(true);
		dictMenuItem.disableProperty().bind(SanskritUtilities.sktDictDBAvailable.not());
		dictMenuItem.setOnAction(actionEvent -> SanskritUtilities.openWindow(Utilities.WindowType.SKTDICT, null));
		final MenuItem lettersMenuItem = new MenuItem("_Letters", new TextIcon("skt-letter", TextIcon.IconSet.CUSTOM));
		lettersMenuItem.setMnemonicParsing(true);
		lettersMenuItem.setOnAction(actionEvent -> SktLetterWin.INSTANCE.display());
		getItems().addAll(dictMenuItem, lettersMenuItem);
	}

}
