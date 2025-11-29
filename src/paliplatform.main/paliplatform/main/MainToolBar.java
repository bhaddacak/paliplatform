/*
 * MainToolBar.java
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

package paliplatform.main;

import paliplatform.base.*;

import javafx.scene.control.*;
import javafx.scene.layout.HBox;

/** 
 * The main toolbar. This is a singleton.
 * @author J.R. Bhaddacak
 * @version 3.0
 * @since 2.0
 */
class MainToolBar extends ToolBar {
	static final MainToolBar INSTANCE = new MainToolBar();
	public final Button helpButton = new Button("", new TextIcon("circle-question", TextIcon.IconSet.AWESOME));
	
	private MainToolBar() {
		final Button openTextButton = new Button("", new TextIcon("file-arrow-up", TextIcon.IconSet.AWESOME));
		openTextButton.setTooltip(new Tooltip("Open a text file"));
		openTextButton.setOnAction(actionEvent -> {
			PaliPlatform.openWindow(Utilities.WindowType.EDITOR, null);
		});
		
		final Button editorButton = new Button("", new TextIcon("pencil", TextIcon.IconSet.AWESOME));
		editorButton.setTooltip(new Tooltip("Edit a new text file"));
		editorButton.setOnAction(actionEvent -> {
			final Object[] args = { "" };
			PaliPlatform.openWindow(Utilities.WindowType.EDITOR, args);
		});
		
		final Button settingsButton = new Button("", new TextIcon("gear", TextIcon.IconSet.AWESOME));
		settingsButton.setTooltip(new Tooltip("Settings"));
		settingsButton.setOnAction(actionEvent -> Settings.INSTANCE.display());

		helpButton.setTooltip(new Tooltip("Quick starter guide"));
		helpButton.setOnAction(actionEvent -> PaliPlatform.infoPopup.showPopup(helpButton, InfoPopup.Pos.BELOW_CENTER, true));		
				
		final Button aboutButton = new Button("", new TextIcon("circle-info", TextIcon.IconSet.AWESOME));
		aboutButton.setTooltip(new Tooltip("About the program"));
		aboutButton.setOnAction(actionEvent -> PaliPlatform.about());
		
		final Button exitButton = new Button("", new TextIcon("power-off", TextIcon.IconSet.AWESOME));
		exitButton.setTooltip(new Tooltip("Exit"));
		exitButton.setOnAction(actionEvent -> PaliPlatform.exit(null));
		
		final HBox readerBox = (HBox)PaliPlatform.styleableServiceMap.get("paliplatform.reader.ReaderToolBarCom");
		if (readerBox != null)
			getItems().addAll(readerBox, new Separator());

		final HBox luceneBox = (HBox)PaliPlatform.styleableServiceMap.get("paliplatform.lucene.LuceneToolBarCom");
		if (luceneBox != null)
			getItems().addAll(luceneBox, new Separator());

		final HBox dictBox = (HBox)PaliPlatform.styleableServiceMap.get("paliplatform.dict.DictToolBarCom");
		if (dictBox != null)
			getItems().addAll(dictBox, new Separator());

		final HBox dpdBox = (HBox)PaliPlatform.styleableServiceMap.get("paliplatform.dpd.DpdToolBarCom");
		if (dpdBox != null)
			getItems().addAll(dpdBox, new Separator());

		getItems().addAll(editorButton, openTextButton, new Separator(), settingsButton,
							new Separator(), helpButton, aboutButton, exitButton);
	}

}
