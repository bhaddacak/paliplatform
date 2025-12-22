/*
 * SanskritMenu.java
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

import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCode;

/** 
 * The menu items for Sanskrit module.
 * @author J.R. Bhaddacak
 * @version 3.6
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
		SanskritUtilities.updateSktDictDBLockStatus();
		// add menu items
		final MenuItem dictMenuItem = new MenuItem("_Dictionaries", new TextIcon("skt-book", TextIcon.IconSet.CUSTOM));
		dictMenuItem.setMnemonicParsing(true);
		dictMenuItem.disableProperty().bind(SanskritUtilities.sktDictDBAvailable.not());
		dictMenuItem.setOnAction(actionEvent -> SanskritUtilities.openWindow(Utilities.WindowType.SKTDICT, null));
		final MenuItem lettersMenuItem = new MenuItem("_Letters", new TextIcon("skt-letter", TextIcon.IconSet.CUSTOM));
		lettersMenuItem.setMnemonicParsing(true);
		lettersMenuItem.setOnAction(actionEvent -> SktLetterWin.INSTANCE.display());
		final MenuItem dictDownloadMenuItem = new MenuItem("Download Sanskrit dict", new TextIcon("cloud-arrow-down", TextIcon.IconSet.AWESOME));
		dictDownloadMenuItem.setOnAction(actionEvent -> SktDictDownloader.INSTANCE.display());
		final MenuItem createDataMenuItem = new MenuItem("_Create Skt. Dict data", new TextIcon("database", TextIcon.IconSet.AWESOME));
		createDataMenuItem.setMnemonicParsing(true);
		createDataMenuItem.disableProperty().bind(SanskritUtilities.someSktDictDataAvailable.not());
		createDataMenuItem.setOnAction(actionEvent -> SanskritUtilities.createSktDictData());
		final CheckMenuItem lockDBMenuItem = new CheckMenuItem();
		lockDBMenuItem.disableProperty().bind(SanskritUtilities.sktDictDBAvailable.not());
		lockDBMenuItem.selectedProperty().bindBidirectional(SanskritUtilities.sktDictDBLocked);
		lockDBMenuItem.textProperty().bindBidirectional(SanskritUtilities.sktDictDBLockString);
		lockDBMenuItem.graphicProperty().bindBidirectional(SanskritUtilities.sktDictDBLockIcon);
		lockDBMenuItem.setOnAction(actionEvent -> SanskritUtilities.lockSktDictDB(lockDBMenuItem.isSelected()));
		getItems().addAll(dictMenuItem, lettersMenuItem,
				new SeparatorMenuItem(), dictDownloadMenuItem, createDataMenuItem,
				new SeparatorMenuItem(), lockDBMenuItem);
	}

}
