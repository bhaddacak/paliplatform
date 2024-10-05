/*
 * SentenceMenu.java
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

package paliplatform.sentence;

import paliplatform.base.*;

import javafx.scene.control.*;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCode;

/** 
 * The menu items for Sentence module.
 * @author J.R. Bhaddacak
 * @version 3.0
 * @since 3.0
 */
public class SentenceMenu extends Menu {
	public SentenceMenu() {
		super("_Sentence");
		setMnemonicParsing(true);
		final MenuItem readerMenuItem = new MenuItem("Sentence _Reader", new TextIcon("book-open", TextIcon.IconSet.AWESOME));
		readerMenuItem.setMnemonicParsing(true);
		readerMenuItem.setOnAction(actionEvent -> SentenceUtilities.openWindow(Utilities.WindowType.READER, null));
		final MenuItem sentManMenuItem = new MenuItem("Sentence _Manager", new TextIcon("briefcase", TextIcon.IconSet.AWESOME));
		sentManMenuItem.setMnemonicParsing(true);
		sentManMenuItem.setOnAction(actionEvent -> SentenceManager.INSTANCE.display());
		getItems().addAll(readerMenuItem, sentManMenuItem);
	}

}
