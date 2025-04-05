/*
 * SentenceSettingTab.java
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

package paliplatform.sentence;

import paliplatform.base.*;

import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;

/** 
 * The tab for Sentence settings.
 * @author J.R. Bhaddacak
 * @version 3.0
 * @since 3.0
 */
public class SentenceSettingTab extends Tab {
	public SentenceSettingTab() {
		setText("Sentence");
		setClosable(false);
		final VBox sentMainBox = new VBox();
		sentMainBox.setSpacing(5);
		sentMainBox.setPadding(new Insets(10));
		VBox.setVgrow(sentMainBox, Priority.ALWAYS);
		final CheckBox cbNormalize = new CheckBox("Normalize text (make it lowercase, etc.)");
		cbNormalize.setSelected(Boolean.parseBoolean(Utilities.settings.getProperty("sentence-normalize")));
		cbNormalize.setOnAction(actionEvent -> {
			Utilities.settings.setProperty("sentence-normalize", Boolean.toString(cbNormalize.isSelected()));
			MainProperties.INSTANCE.saveSettings();			
		});
		final VBox decomBox = new VBox();
		decomBox.setSpacing(5);
		final CheckBox cbUseCap = createDecomCheckBox("Capitalization", "sentence-use-cap");
		final CheckBox cbUseBar = createDecomCheckBox("Bars (|‖)", "sentence-use-bar");
		final CheckBox cbUseDot = createDecomCheckBox("Dot (.)", "sentence-use-dot");
		final CheckBox cbUseColon = createDecomCheckBox("Colon (:)", "sentence-use-colon");
		final CheckBox cbUseSemicolon = createDecomCheckBox("Semicolon (;)", "sentence-use-semicolon");
		final CheckBox cbUseDash = createDecomCheckBox("Dashes (–—)", "sentence-use-dash");
		decomBox.getChildren().addAll(cbUseCap, cbUseBar, cbUseDot, cbUseColon, cbUseSemicolon, cbUseDash);
		sentMainBox.getChildren().addAll(new Label("Options for sentence decomposition"), cbNormalize, new Separator(),
							new Label("Treat these tokens as sentence delimiters"), decomBox);
		setContent(sentMainBox);
	}

	private CheckBox createDecomCheckBox(final String text, final String prop) {
		final CheckBox cb = new CheckBox(text);
		cb.setSelected(Boolean.parseBoolean(Utilities.settings.getProperty(prop)));
		cb.setOnAction(actionEvent -> {
			Utilities.settings.setProperty(prop, Boolean.toString(cb.isSelected()));
			MainProperties.INSTANCE.saveSettings();			
		});
		return cb;
	}

}

