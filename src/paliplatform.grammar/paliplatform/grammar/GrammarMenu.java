/*
 * GrammarMenu.java
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

package paliplatform.grammar;

import paliplatform.base.*;
import paliplatform.dict.*;

import javafx.scene.control.*;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCode;

/** 
 * The menu items for Grammar module.
 * @author J.R. Bhaddacak
 * @version 3.0
 * @since 3.0
 */
public class GrammarMenu extends Menu {
	public GrammarMenu() {
		super("_Grammar");
		// initialization
		GrammarUtilities.initializeServices();
		DictUtilities.initializeDictAvailMap();
		// setup menu
		setMnemonicParsing(true);
		final MenuItem lettersMenuItem = new MenuItem("Letters", new TextIcon("font", TextIcon.IconSet.AWESOME));
		lettersMenuItem.setOnAction(actionEvent -> LetterWin.INSTANCE.display());
		final MenuItem declMenuItem = new MenuItem("Declension table", new TextIcon("table-cells", TextIcon.IconSet.AWESOME));
		declMenuItem.setOnAction(actionEvent -> GrammarUtilities.openWindow(Utilities.WindowType.DECLENSION, null));
		final MenuItem verbsMenuItem = new MenuItem("Verbs", new TextIcon("person-walking", TextIcon.IconSet.AWESOME));
		verbsMenuItem.disableProperty().bind(DictUtilities.dictAvailMap.get(DictUtilities.DictBook.CPED).not());
		verbsMenuItem.setOnAction(actionEvent -> VerbWin.INSTANCE.display());
		final MenuItem conjugMenuItem = new MenuItem("Conjugation table", new TextIcon("table-cells", TextIcon.IconSet.AWESOME));
		conjugMenuItem.setOnAction(actionEvent -> ConjugationWin.INSTANCE.display());
		final MenuItem rootsMenuItem = new MenuItem("Roots", new TextIcon("seedling", TextIcon.IconSet.AWESOME));
		rootsMenuItem.setOnAction(actionEvent -> RootWin.INSTANCE.display());
		final MenuItem prosodyMenuItem = new MenuItem("Prosody", new TextIcon("music", TextIcon.IconSet.AWESOME));
		prosodyMenuItem.setOnAction(actionEvent -> GrammarUtilities.openWindow(Utilities.WindowType.PROSODY, null));
		getItems().addAll(lettersMenuItem, declMenuItem, verbsMenuItem, conjugMenuItem, rootsMenuItem, prosodyMenuItem);
	}

}
