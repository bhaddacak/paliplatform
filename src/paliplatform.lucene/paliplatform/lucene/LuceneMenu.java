/*
 * LuceneMenu.java
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

package paliplatform.lucene;

import paliplatform.base.*;
import paliplatform.reader.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Files;

import javafx.scene.control.*;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCode;

/** 
 * The menu items for Lucene module.
 * @author J.R. Bhaddacak
 * @version 3.3
 * @since 3.0
 */
public class LuceneMenu extends Menu {
	public LuceneMenu() {
		super("_Lucene");
		setMnemonicParsing(true);
		// initialization
		final Path indexPath = Path.of(Utilities.ROOTDIR + LuceneUtilities.INDEXPATH);
		try {
			if (Files.notExists(indexPath))
				Files.createDirectories(indexPath);
		} catch (IOException e) {
			System.err.println(e);
		}
		LuceneUtilities.stopwordsFile = new File(Utilities.ROOTDIR + Utilities.RULESPATH + LuceneUtilities.STOPWORDS);
		LuceneUtilities.updateStopwords();
		LuceneUtilities.initializeComparator();
		Utilities.initializeH2DB(Utilities.H2DB.LISTER);
		LuceneUtilities.updateListerDBLockStatus();
		// add menu items
		final MenuItem luceneMenuItem = new MenuItem("_Lucene Finder", new TextIcon("lucene", TextIcon.IconSet.CUSTOM));
		luceneMenuItem.setMnemonicParsing(true);
		luceneMenuItem.setOnAction(actionEvent -> LuceneUtilities.openWindow(Utilities.WindowType.LUCENE, null));
		luceneMenuItem.disableProperty().bind(ReaderUtilities.corpusAvailable.not());
		final MenuItem listerMenuItem = new MenuItem("Term _Lister", new TextIcon("list-ul", TextIcon.IconSet.AWESOME));
		listerMenuItem.setMnemonicParsing(true);
		listerMenuItem.setOnAction(actionEvent -> LuceneUtilities.openWindow(Utilities.WindowType.LISTER, null));
		final CheckMenuItem lockDBMenuItem = new CheckMenuItem();
		lockDBMenuItem.selectedProperty().bindBidirectional(LuceneUtilities.listerDBLocked);
		lockDBMenuItem.textProperty().bindBidirectional(LuceneUtilities.listerDBLockString);
		lockDBMenuItem.graphicProperty().bindBidirectional(LuceneUtilities.listerDBLockIcon);
		lockDBMenuItem.setOnAction(actionEvent -> LuceneUtilities.lockListerDB(lockDBMenuItem.isSelected()));
		getItems().addAll(luceneMenuItem, listerMenuItem, new SeparatorMenuItem(), lockDBMenuItem);
	}

}
