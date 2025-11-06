/*
 * LuceneToolBarCom.java
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

import javafx.scene.control.*;
import javafx.scene.layout.HBox;

/** 
 * The tool bar component for Lucene module.
 * @author J.R. Bhaddacak
 * @version 3.3
 * @since 3.0
 */
public class LuceneToolBarCom extends HBox {
	public LuceneToolBarCom() {
		final Button luceneButton = new Button("", new TextIcon("lucene", TextIcon.IconSet.CUSTOM));
		luceneButton.setTooltip(new Tooltip("Lucene Finder"));
		luceneButton.setOnAction(actionEvent -> LuceneUtilities.openWindow(Utilities.WindowType.LUCENE, null));
		luceneButton.disableProperty().bind(ReaderUtilities.corpusAvailable.not());
		final Button listerButton = new Button("", new TextIcon("list-ul", TextIcon.IconSet.AWESOME));
		listerButton.setTooltip(new Tooltip("Term Lister"));
		listerButton.setOnAction(actionEvent -> LuceneUtilities.openWindow(Utilities.WindowType.LISTER, null));
		getChildren().addAll(luceneButton, listerButton);
	}

}

