/*
 * ReaderToolBarCom.java
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
import javafx.scene.layout.HBox;

/** 
 * The tool bar component for Reader module.
 * @author J.R. Bhaddacak
 * @version 3.0
 * @since 3.0
 */
public class ReaderToolBarCom extends HBox {
	public ReaderToolBarCom() {
		final Button toctreeButton = new Button("", new TextIcon("folder-tree", TextIcon.IconSet.AWESOME));
		toctreeButton.setTooltip(new Tooltip("TOC Tree"));
		toctreeButton.setOnAction(actionEvent -> ReaderUtilities.openWindow(Utilities.WindowType.TOCTREE, null));
		final Button docfinderButton = new Button("", new TextIcon("magnifying-glass", TextIcon.IconSet.AWESOME));
		docfinderButton.setTooltip(new Tooltip("Document Finder"));
		docfinderButton.disableProperty().bind(ReaderUtilities.corpusAvailable.not());
		docfinderButton.setOnAction(actionEvent -> ReaderUtilities.openWindow(Utilities.WindowType.FINDER, null));
		final Button scReaderButton = new Button("", new TextIcon("sc", TextIcon.IconSet.CUSTOM));
		scReaderButton.setTooltip(new Tooltip("SuttaCentral Text Reader"));
		scReaderButton.disableProperty().bind(ReaderUtilities.suttaCentralAvailable.not());
		scReaderButton.setOnAction(actionEvent -> ReaderUtilities.openWindow(Utilities.WindowType.VIEWER_SC, null));
		getChildren().addAll(toctreeButton, docfinderButton, scReaderButton);
	}

}

