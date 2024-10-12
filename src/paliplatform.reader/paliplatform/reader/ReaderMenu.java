/*
 * ReaderMenu.java
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

package paliplatform.reader;

import paliplatform.base.*;

import javafx.scene.control.*;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCode;

/** 
 * The menu items for Reader module.
 * @author J.R. Bhaddacak
 * @version 3.0
 * @since 3.0
 */
public class ReaderMenu extends Menu {
	public ReaderMenu() {
		super("_Reader");
		setMnemonicParsing(true);
		// init
		ReaderUtilities.checkIfSuttaCentralAvailable();
		ReaderUtilities.loadScSuttaInfo();
		ReaderUtilities.updateCorpusList();
		// add menu items
		final MenuItem toctreeMenuItem = new MenuItem("_TOC Tree", new TextIcon("folder-tree", TextIcon.IconSet.AWESOME));
		toctreeMenuItem.setMnemonicParsing(true);
		toctreeMenuItem.setOnAction(actionEvent -> ReaderUtilities.openWindow(Utilities.WindowType.TOCTREE, null));
		final MenuItem docfinderMenuItem = new MenuItem("_Document Finder", new TextIcon("magnifying-glass", TextIcon.IconSet.AWESOME));
		docfinderMenuItem.setMnemonicParsing(true);
		docfinderMenuItem.disableProperty().bind(ReaderUtilities.corpusAvailable.not());
		docfinderMenuItem.setOnAction(actionEvent -> ReaderUtilities.openWindow(Utilities.WindowType.FINDER, null));
		final MenuItem refTableMenuItem = new MenuItem("_Reference Table", new TextIcon("table-cells", TextIcon.IconSet.AWESOME));
		refTableMenuItem.setMnemonicParsing(true);
		refTableMenuItem.setOnAction(actionEvent -> ReferenceTable.INSTANCE.display());
		final MenuItem scReaderMenuItem = new MenuItem("_SuttaCentral Text Reader", new TextIcon("sc", TextIcon.IconSet.CUSTOM));
		scReaderMenuItem.setMnemonicParsing(true);
		scReaderMenuItem.disableProperty().bind(ReaderUtilities.suttaCentralAvailable.not());
		scReaderMenuItem.setOnAction(actionEvent -> ReaderUtilities.openWindow(Utilities.WindowType.VIEWER_SC, null));
		final MenuItem downloadMenuItem = new MenuItem("Download SuttaCentral data", new TextIcon("cloud-arrow-down", TextIcon.IconSet.AWESOME));
		downloadMenuItem.setOnAction(actionEvent -> {
			// This prevent exit error when the window stays opened.
			// See also in ReaderServiceImp and MainMenu.
			ReaderUtilities.scDownloaderOpened = true;
			ScDownloader.INSTANCE.display();
		});
		getItems().addAll(toctreeMenuItem, docfinderMenuItem, refTableMenuItem,
						new SeparatorMenuItem(), scReaderMenuItem, downloadMenuItem);
	}

}
