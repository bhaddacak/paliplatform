/*
 * Settings.java
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

package paliplatform.main;

import paliplatform.base.*;

import java.util.*;
import java.io.*;
import java.nio.file.*;

import javafx.scene.*;
import javafx.scene.image.*;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.AnchorPane;
import javafx.geometry.*;

/** 
 * The settings dialog. This is a singleton.
 * @author J.R. Bhaddacak
 * @version 3.3
 * @since 2.0
 */
class Settings extends SingletonWindow {
	static final Settings INSTANCE = new Settings();
	static TextField[] unusedCharTextFields;
	static TextField[] compCharTextFields;
	private final Label lbExtraPath = new Label();
	
	private Settings() {
		windowWidth = Utilities.getRelativeSize(44);
		windowHeight = Utilities.getRelativeSize(32);
		setTitle("Settings");
		getIcons().add(new Image(Settings.class.getResourceAsStream("resources/images/gear.png")));
		
		final BorderPane outerPane = new BorderPane();
		outerPane.setPadding(new Insets(10, 0, 10, 0));
		// using tabpane as the main container
		final TabPane tabPane = new TabPane();
		
		// general settings
		final Tab generalTab = new Tab("General");
		generalTab.setClosable(false);
		final VBox generalBox = new VBox();
		generalBox.setSpacing(5);
		generalBox.setPadding(new Insets(10));
		generalBox.setPrefHeight(Double.MAX_VALUE);
		final CheckBox cbExitAsk = new CheckBox("Ask before exit");
		cbExitAsk.setAllowIndeterminate(false);
		cbExitAsk.setSelected(Boolean.parseBoolean(Utilities.settings.getProperty("exit-ask")));
		cbExitAsk.setOnAction(actionEvent -> Utilities.settings.setProperty("exit-ask", Boolean.toString(cbExitAsk.isSelected())));
		final CheckBox cbThaiAltChars = new CheckBox("Always use Thai's Yo Ying (" + '\uF70F' + ") and Tho Than (" + '\uF700' + ") without lower part");
		final List<String> flist = new ArrayList<>(Utilities.externalFontMap.get(Utilities.PaliScript.THAI));
		cbThaiAltChars.setStyle("-fx-font-family:'" + flist.get(0) + "'");
		cbThaiAltChars.setAllowIndeterminate(false);
		cbThaiAltChars.setSelected(Boolean.parseBoolean(Utilities.settings.getProperty("thai-alt-chars")));
		cbThaiAltChars.setOnAction(actionEvent -> Utilities.settings.setProperty("thai-alt-chars", Boolean.toString(cbThaiAltChars.isSelected())));
		final CheckBox cbDpdLookup = new CheckBox("Use DPD lookup in Pāli text readers");
		cbDpdLookup.setAllowIndeterminate(false);
		cbDpdLookup.setSelected(Boolean.parseBoolean(Utilities.settings.getProperty("dpd-lookup-enable")));
		cbDpdLookup.setOnAction(actionEvent -> Utilities.settings.setProperty("dpd-lookup-enable", Boolean.toString(cbDpdLookup.isSelected())));
		generalBox.getChildren().addAll(cbExitAsk,
										new Separator(), new Label("Script transformation"),
										cbThaiAltChars, new Label("(This can cause search problems)"),
										new Separator(), new Label("DPD integration"), cbDpdLookup);
		generalTab.setContent(generalBox);
		
		// Pali input settings
		final Tab paliInputTab = new Tab("Pāli input");
		paliInputTab.setClosable(false);
		final VBox paliInputBox = new VBox();
		paliInputBox.setSpacing(5);
		paliInputBox.setPadding(new Insets(10));
		paliInputBox.setPrefHeight(Double.MAX_VALUE);
		final HBox defMethodBox = new HBox();
		defMethodBox.setSpacing(5);
		final ToggleGroup defMethodGroup = new ToggleGroup();
		final RadioButton raUnusedChars = new RadioButton("Unused chars");
		final RadioButton raCompChars = new RadioButton("Composite chars");
		final RadioButton raNormal = new RadioButton("Normal");
		final Map<String, Toggle> textInputMap = Map.of(
				PaliTextInput.InputMethod.UNUSED_CHARS.toString(), raUnusedChars,
				PaliTextInput.InputMethod.COMPOSITE.toString(), raCompChars,
				PaliTextInput.InputMethod.NORMAL.toString(), raNormal);
		raUnusedChars.setToggleGroup(defMethodGroup);
		raCompChars.setToggleGroup(defMethodGroup);
		raNormal.setToggleGroup(defMethodGroup);
		defMethodGroup.selectToggle(textInputMap.get(Utilities.settings.getProperty("pali-input-method")));
		raUnusedChars.setOnAction(actionEvent -> {
			Utilities.settings.setProperty("pali-input-method", PaliTextInput.InputMethod.UNUSED_CHARS.toString());
			MainProperties.INSTANCE.saveSettings();
		});
		raCompChars.setOnAction(actionEvent -> {
			Utilities.settings.setProperty("pali-input-method", PaliTextInput.InputMethod.COMPOSITE.toString());
			MainProperties.INSTANCE.saveSettings();
		});
		raNormal.setOnAction(actionEvent -> {
			Utilities.settings.setProperty("pali-input-method", PaliTextInput.InputMethod.NORMAL.toString());
			MainProperties.INSTANCE.saveSettings();
		});
		defMethodBox.getChildren().addAll(new Label("Default input method: "), raUnusedChars, raCompChars, raNormal);
		paliInputBox.getChildren().add(defMethodBox);
		
		final Hashtable<String, String> defaultTable = MainProperties.PaliInputProperties.INSTANCE.getDefaultTable();
		final String[] unusedCharNames = MainProperties.PaliInputProperties.INSTANCE.getUnusedCharNames();
		final String[] unusedCharKeys = MainProperties.PaliInputProperties.INSTANCE.getUnusedCharKeys();
		final AnchorPane unusedCharHeadPane = new AnchorPane();
		final Button unusedCharResetButton = new Button("", new TextIcon("arrows-rotate", TextIcon.IconSet.AWESOME));
		unusedCharResetButton.setTooltip(new Tooltip("Reset to default values"));
		unusedCharResetButton.setOnAction(actionEvent -> {
			for (int i=0; i<unusedCharKeys.length; i++) {
				final String def = defaultTable.get(unusedCharKeys[i]);
				Utilities.settings.setProperty(unusedCharKeys[i], def);
				unusedCharTextFields[i].setText(def);
			}
			MainProperties.INSTANCE.saveSettings();
			Utilities.setupPaliInputCharMap();
		});
		final Label unusedCharHeadLabel = new Label("Key mapping of unused characters:");
		AnchorPane.setTopAnchor(unusedCharHeadLabel, 0.0);
		AnchorPane.setLeftAnchor(unusedCharHeadLabel, 0.0);
		AnchorPane.setTopAnchor(unusedCharResetButton, 0.0);
		AnchorPane.setRightAnchor(unusedCharResetButton, 0.0);
		unusedCharHeadPane.getChildren().addAll(unusedCharHeadLabel, unusedCharResetButton);
		final GridPane unusedInputGrid = new GridPane();
		unusedInputGrid.setHgap(5);
		unusedInputGrid.setVgap(2);
		unusedCharTextFields = new TextField[unusedCharNames.length];
		int row;
		int colLb;
		int colTf;
		for (int i=0; i<unusedCharNames.length; i++) {
			final Label lb = new Label(unusedCharNames[i]+":");
			unusedCharTextFields[i] = new TextField(Utilities.settings.getProperty(unusedCharKeys[i]));
			unusedCharTextFields[i].setPrefColumnCount(1);
			row = i/3;
			colLb = 2*(i - row*3);
			colTf = 2*(i - row*3) + 1;
			GridPane.setConstraints(lb, colLb, row, 1, 1, HPos.RIGHT, VPos.CENTER);
			GridPane.setConstraints(unusedCharTextFields[i], colTf, row, 1, 1, HPos.RIGHT, VPos.CENTER);
			unusedInputGrid.getChildren().addAll(lb, unusedCharTextFields[i]);
			final int iFinal = i;
			unusedCharTextFields[i].textProperty().addListener((obs, oldValue, newValue) -> {
				final String newInput;
				if (newValue.isEmpty())
					newInput = defaultTable.get(unusedCharKeys[iFinal]);
				else
					newInput = newValue.substring(0, 1);
				Utilities.settings.setProperty(unusedCharKeys[iFinal], newInput);
				MainProperties.INSTANCE.saveSettings();
				Utilities.setupPaliInputCharMap();
			});
		}
		paliInputBox.getChildren().addAll(new Separator(), unusedCharHeadPane, unusedInputGrid);

		final String[] compCharNames = MainProperties.PaliInputProperties.INSTANCE.getCompCharNames();
		final String[] compCharKeys = MainProperties.PaliInputProperties.INSTANCE.getCompCharKeys();
		final AnchorPane compCharHeadPane = new AnchorPane();
		final Button compCharResetButton = new Button("", new TextIcon("arrows-rotate", TextIcon.IconSet.AWESOME));
		compCharResetButton.setTooltip(new Tooltip("Reset to default values"));
		compCharResetButton.setOnAction(actionEvent -> {
			for (int i=0; i<compCharKeys.length; i++) {
				final String def = defaultTable.get(compCharKeys[i]);
				Utilities.settings.setProperty(compCharKeys[i], def);
				compCharTextFields[i].setText(def);
			}
			MainProperties.INSTANCE.saveSettings();
			Utilities.setupPaliInputCharMap();
		});
		final Label compCharHeadLabel = new Label("Key mapping of composite accents:");
		AnchorPane.setTopAnchor(compCharHeadLabel, 0.0);
		AnchorPane.setLeftAnchor(compCharHeadLabel, 0.0);
		AnchorPane.setTopAnchor(compCharResetButton, 0.0);
		AnchorPane.setRightAnchor(compCharResetButton, 0.0);
		compCharHeadPane.getChildren().addAll(compCharHeadLabel, compCharResetButton);		
		final GridPane compInputGrid = new GridPane();
		compInputGrid.setHgap(5);
		compInputGrid.setVgap(2);
		compCharTextFields = new TextField[compCharNames.length];
		for (int i=0; i<compCharNames.length; i++) {
			final Label lb = new Label(compCharNames[i]+":");
			compCharTextFields[i] = new TextField(Utilities.settings.getProperty(compCharKeys[i]));
			compCharTextFields[i].setPrefColumnCount(1);
			row = i/2;
			colLb = 2*(i - row*2);
			colTf = 2*(i - row*2) + 1;
			GridPane.setConstraints(lb, colLb, row, 1, 1, HPos.RIGHT, VPos.CENTER);
			GridPane.setConstraints(compCharTextFields[i], colTf, row, 1, 1, HPos.RIGHT, VPos.CENTER);
			compInputGrid.getChildren().addAll(lb, compCharTextFields[i]);
			final int iFinal = i;
			compCharTextFields[i].textProperty().addListener((obs, oldValue, newValue) -> {
				final String newInput;
				if (newValue.isEmpty())
					newInput = defaultTable.get(compCharKeys[iFinal]);
				else
					newInput = newValue.substring(0, 1);
				Utilities.settings.setProperty(compCharKeys[iFinal], newInput);
				MainProperties.INSTANCE.saveSettings();
				Utilities.setupPaliInputCharMap();
			});			
		}		
		paliInputBox.getChildren().addAll(new Separator(), compCharHeadPane, compInputGrid);
		
		paliInputTab.setContent(paliInputBox);
		
		// add tabs
		tabPane.getTabs().addAll(generalTab, paliInputTab);
		// dictionaries
		final Tab dictTab = (Tab)PaliPlatform.styleableServiceMap.get("paliplatform.dict.DictSettingTab");
		if (dictTab != null)
			tabPane.getTabs().add(dictTab);
		// sentence
		final Tab sentTab = (Tab)PaliPlatform.styleableServiceMap.get("paliplatform.sentence.SentenceSettingTab");
		if (sentTab != null)
			tabPane.getTabs().add(sentTab);
		
		// close button
		final Button close = new Button("Close");
		close.setOnAction(actionEvent -> close());
		outerPane.setCenter(tabPane);
		outerPane.setBottom(close);
		outerPane.setAlignment(close, Pos.CENTER);
		
		final Scene scene = new Scene(outerPane, windowWidth, windowHeight);
		setScene(scene);
	}

}
