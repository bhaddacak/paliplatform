/*
 * DpdMenu.java
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

package paliplatform.dpd;

import paliplatform.base.*;

import javafx.scene.control.*;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCode;
import javafx.application.Platform;

/** 
 * The menu items for DPD module.
 * @author J.R. Bhaddacak
 * @version 3.0
 * @since 3.0
 */
public class DpdMenu extends Menu {
	public DpdMenu() {
		super("D_PD");
		setMnemonicParsing(true);
		// init
		final boolean dbExists = DpdUtilities.checkIfDpdAvailable();
		if (dbExists)
			Utilities.initializeDpdDB();
		DpdUtilities.updatePpdpdDBLockStatus();
		// add components
		final MenuItem dictMenuItem = new MenuItem("_Dictionary", new TextIcon("dpd", TextIcon.IconSet.CUSTOM));
		dictMenuItem.setMnemonicParsing(true);
		dictMenuItem.disableProperty().bind(DpdUtilities.dpdAvailable.not());
		dictMenuItem.setOnAction(actionEvent -> DpdDictWin.INSTANCE.display());
		final MenuItem deconMenuItem = new MenuItem("De_constructor", new TextIcon("hammer", TextIcon.IconSet.AWESOME));
		deconMenuItem.setMnemonicParsing(true);
		deconMenuItem.disableProperty().bind(DpdUtilities.dpdAvailable.not());
		deconMenuItem.setOnAction(actionEvent -> DeconWin.INSTANCE.display());
		final MenuItem headwordMenuItem = new MenuItem("_Head words", new TextIcon("crown", TextIcon.IconSet.AWESOME));
		headwordMenuItem.setMnemonicParsing(true);
		Platform.runLater(() -> headwordMenuItem.disableProperty().bind(Utilities.ppdpdAvailMap.get(Utilities.PpdpdTable.SORTED_HEADWORDS).not()));
		headwordMenuItem.setOnAction(actionEvent -> DpdHeadWordWin.INSTANCE.display());
		final MenuItem dpdRootMenuItem = new MenuItem("_Roots", new TextIcon("seedling", TextIcon.IconSet.AWESOME));
		dpdRootMenuItem.setMnemonicParsing(true);
		dpdRootMenuItem.disableProperty().bind(DpdUtilities.dpdAvailable.not());
		dpdRootMenuItem.setOnAction(actionEvent -> DpdRootWin.INSTANCE.display());
		final MenuItem familyMenuItem = new MenuItem("_Families of terms", new TextIcon("boxes-stacked", TextIcon.IconSet.AWESOME));
		familyMenuItem.setMnemonicParsing(true);
		familyMenuItem.disableProperty().bind(DpdUtilities.dpdAvailable.not());
		familyMenuItem.setOnAction(actionEvent -> FamilyWin.INSTANCE.display());
		final MenuItem checkDBMenuItem = new MenuItem("Check DB Applicability", new TextIcon("stethoscope", TextIcon.IconSet.AWESOME));
		checkDBMenuItem.disableProperty().bind(DpdUtilities.dpdAvailable.not());
		checkDBMenuItem.setOnAction(actionEvent -> DpdUtilities.testDpdDb());
		final MenuItem downloadMenuItem = new MenuItem("Download DPD database", new TextIcon("cloud-arrow-down", TextIcon.IconSet.AWESOME));
		downloadMenuItem.setOnAction(actionEvent -> DpdDownloader.INSTANCE.display());
		final CheckMenuItem lockDBMenuItem = new CheckMenuItem();
		lockDBMenuItem.selectedProperty().bindBidirectional(DpdUtilities.ppdpdDBLocked);
		lockDBMenuItem.textProperty().bindBidirectional(DpdUtilities.ppdpdDBLockString);
		lockDBMenuItem.graphicProperty().bindBidirectional(DpdUtilities.ppdpdDBLockIcon);
		lockDBMenuItem.setOnAction(actionEvent -> DpdUtilities.lockPpdpdDB(lockDBMenuItem.isSelected()));
		getItems().addAll(dictMenuItem, deconMenuItem, headwordMenuItem, dpdRootMenuItem, familyMenuItem,
						new SeparatorMenuItem(), checkDBMenuItem, downloadMenuItem, lockDBMenuItem);
	}

}
