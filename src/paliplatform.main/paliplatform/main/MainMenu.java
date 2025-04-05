/*
 * MainMenu.java
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
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCode;

/** 
 * The main menu bar including some action controllers.
 * This is a singleton.
 * @author J.R. Bhaddacak
 * @version 3.0
 * @since 2.0
 */
class MainMenu extends MenuBar {
	static final MainMenu INSTANCE = new MainMenu();
	
	private MainMenu() {
		// File
		final Menu fileMenu = new Menu("_File");
		fileMenu.setMnemonicParsing(true);
		final MenuItem openTextMenuItem = new MenuItem("_Open a text file", new TextIcon("file-arrow-up", TextIcon.IconSet.AWESOME));
		openTextMenuItem.setMnemonicParsing(true);
		openTextMenuItem.setOnAction(actionEvent -> {
			final Object[] args = {""};
			PaliPlatform.openWindow(Utilities.WindowType.EDITOR, args);
		});
		final MenuItem editorMenuItem = new MenuItem("Edit a _new text file", new TextIcon("pencil", TextIcon.IconSet.AWESOME));
		editorMenuItem.setMnemonicParsing(true);
		editorMenuItem.setOnAction(actionEvent -> {
			final Object[] args = {"ROMAN"};
			PaliPlatform.openWindow(Utilities.WindowType.EDITOR, args);
		});
		final MenuItem batchMenuItem = new MenuItem("_Batch Script Transformer", new TextIcon("gears", TextIcon.IconSet.AWESOME));
		batchMenuItem.setMnemonicParsing(true);
		batchMenuItem.setOnAction(actionEvent -> BatchScriptTransformer.INSTANCE.display());
		final MenuItem updateInfoMenuItem = new MenuItem("Update online info", new TextIcon("arrows-rotate", TextIcon.IconSet.AWESOME));
		updateInfoMenuItem.setOnAction(actionEvent -> UrlProperties.INSTANCE.update());
		final MenuItem patcherMenuItem = new MenuItem("_Patch Installer", new TextIcon("cloud-arrow-down", TextIcon.IconSet.AWESOME));
		patcherMenuItem.setMnemonicParsing(true);
		patcherMenuItem.setOnAction(actionEvent -> PatchInstaller.INSTANCE.display());
		final MenuItem exitMenuItem = new MenuItem("E_xit", new TextIcon("power-off", TextIcon.IconSet.AWESOME));
		exitMenuItem.setMnemonicParsing(true);
		exitMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.Q, KeyCombination.SHORTCUT_DOWN));
		exitMenuItem.setOnAction(actionEvent -> PaliPlatform.exit(null));
		final MenuItem desktopMenuItem = new MenuItem("Create desktop launcher", new TextIcon("rocket", TextIcon.IconSet.AWESOME));
		desktopMenuItem.setOnAction(actionEvent -> PaliPlatform.createDesktopLauncher());
		fileMenu.getItems().addAll(editorMenuItem, openTextMenuItem, 
								new SeparatorMenuItem(), batchMenuItem, updateInfoMenuItem, patcherMenuItem, desktopMenuItem, 
								new SeparatorMenuItem(), exitMenuItem);
		
		// Option
		Menu optionsMenu = new Menu("_Options");
		final Menu themeMenu = new Menu("Global _theme");
		themeMenu.setMnemonicParsing(true);
		final ToggleGroup themeGroup = new ToggleGroup();
		for (Utilities.Theme t : Utilities.Theme.values()){
			final String tName = t.toString();
			final RadioMenuItem themeItem = new RadioMenuItem(tName.charAt(0) + tName.substring(1).toLowerCase());
			themeItem.setToggleGroup(themeGroup);
			themeItem.setSelected(themeItem.getText().toUpperCase().equals(Utilities.settings.getProperty("theme")));
			themeMenu.getItems().add(themeItem);
		}
        themeGroup.selectedToggleProperty().addListener((observable) -> {
			if (themeGroup.getSelectedToggle() != null) {
				final RadioMenuItem selected = (RadioMenuItem)themeGroup.getSelectedToggle();
				final String t = selected.getText().toUpperCase();
				Utilities.settings.setProperty("theme", "" + t);
				PaliPlatform.refreshTheme();
			}
        });		
		optionsMenu.setMnemonicParsing(true);
		final MenuItem settingsMenuItem = new MenuItem("Settings", new TextIcon("gear", TextIcon.IconSet.AWESOME));
		settingsMenuItem.setOnAction(actionEvent -> Settings.INSTANCE.display());
		optionsMenu.getItems().addAll(themeMenu, settingsMenuItem);
		
		// Help
		final Menu helpMenu = new Menu("_Help");
		helpMenu.setMnemonicParsing(true);
		final MenuItem helpMenuItem = new MenuItem("Quick starter", new TextIcon("circle-question", TextIcon.IconSet.AWESOME));
		helpMenuItem.setOnAction(actionEvent -> PaliPlatform.infoPopup.showPopup(MainToolBar.INSTANCE.helpButton, InfoPopup.Pos.BELOW_CENTER, true));				
		final MenuItem aboutMenuItem = new MenuItem("About", new TextIcon("circle-info", TextIcon.IconSet.AWESOME));
		aboutMenuItem.setOnAction(actionEvent -> PaliPlatform.about());
		helpMenu.getItems().addAll(helpMenuItem, aboutMenuItem);
						
		getMenus().add(fileMenu);
		final Menu dictMenu = (Menu)PaliPlatform.styleableServiceMap.get("paliplatform.dict.DictMenu");
		if (dictMenu != null)
			getMenus().add(dictMenu);
		final Menu dpdMenu = (Menu)PaliPlatform.styleableServiceMap.get("paliplatform.dpd.DpdMenu");
		if (dpdMenu != null) {
			getMenus().add(dpdMenu);
		}
		final Menu grammarMenu = (Menu)PaliPlatform.styleableServiceMap.get("paliplatform.grammar.GrammarMenu");
		if (grammarMenu != null) {
			final MenuItem rootFinderMenuItem = (MenuItem)PaliPlatform.styleableServiceMap.get("paliplatform.reader.RootFinderMenuItem");
			final MenuItem gramSutFinderMenuItem = (MenuItem)PaliPlatform.styleableServiceMap.get("paliplatform.reader.GramSutFinderMenuItem");
			if (rootFinderMenuItem != null || gramSutFinderMenuItem != null)
				grammarMenu.getItems().add(new SeparatorMenuItem());
			if (rootFinderMenuItem != null)
				grammarMenu.getItems().add(rootFinderMenuItem);
			if (gramSutFinderMenuItem != null)
				grammarMenu.getItems().add(gramSutFinderMenuItem);
			getMenus().add(grammarMenu);
		}
		final Menu readerMenu = (Menu)PaliPlatform.styleableServiceMap.get("paliplatform.reader.ReaderMenu");
		if (readerMenu != null) {
			getMenus().add(readerMenu);
		}
		final Menu luceneMenu = (Menu)PaliPlatform.styleableServiceMap.get("paliplatform.lucene.LuceneMenu");
		if (luceneMenu != null)
			getMenus().add(luceneMenu);
		final Menu sentenceMenu = (Menu)PaliPlatform.styleableServiceMap.get("paliplatform.sentence.SentenceMenu");
		if (sentenceMenu != null) {
			getMenus().add(sentenceMenu);
		}
		getMenus().addAll(optionsMenu, helpMenu);
	}

}
