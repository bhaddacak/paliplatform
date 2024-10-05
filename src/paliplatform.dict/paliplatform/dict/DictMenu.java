/*
 * DictMenu.java
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

import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCode;

/** 
 * The menu items for Dict module.
 * @author J.R. Bhaddacak
 * @version 3.0
 * @since 3.0
 */
public class DictMenu extends Menu {

	public DictMenu() {
		super("_Dict");
		setMnemonicParsing(true);
		// init
		Utilities.initializeDictDB();
		Utilities.initializePpdpdDB();
		Utilities.initializePpdpdAvailMap();
		Utilities.updatePpdpdAvailibility();
		DictUtilities.initializeDictAvailMap();
		DictUtilities.updateDictAvailibility();
		DictUtilities.updateDictDBLockStatus();
		// add menu items
		final MenuItem dictMenuItem = new MenuItem("_Dictionaries", new TextIcon("book", TextIcon.IconSet.AWESOME));
		dictMenuItem.setMnemonicParsing(true);
		dictMenuItem.disableProperty().bind(DictUtilities.someDictAvailable.not());
		dictMenuItem.setOnAction(actionEvent -> DictUtilities.openWindow(Utilities.WindowType.DICT, null));
		final MenuItem createDataMenuItem = new MenuItem("_Create Dict data", new TextIcon("database", TextIcon.IconSet.AWESOME));
		createDataMenuItem.setMnemonicParsing(true);
		createDataMenuItem.disableProperty().bind(DictUtilities.dictDBLocked);
		createDataMenuItem.setOnAction(actionEvent -> DictUtilities.createDictData());
		final CheckMenuItem lockDBMenuItem = new CheckMenuItem();
		lockDBMenuItem.selectedProperty().bindBidirectional(DictUtilities.dictDBLocked);
		lockDBMenuItem.textProperty().bindBidirectional(DictUtilities.dictDBLockString);
		lockDBMenuItem.graphicProperty().bindBidirectional(DictUtilities.dictDBLockIcon);
		lockDBMenuItem.setOnAction(actionEvent -> DictUtilities.lockDictDB(lockDBMenuItem.isSelected()));
		getItems().addAll(dictMenuItem, createDataMenuItem, new SeparatorMenuItem(), lockDBMenuItem);
	}

}
