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
import paliplatform.base.ScriptTransliterator.EngineType;

import java.util.*;
import java.util.stream.*;
import java.io.*;
import java.nio.file.*;

import javafx.scene.*;
import javafx.scene.image.*;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.AnchorPane;
import javafx.geometry.*;

/** 
 * The settings dialog. This is a singleton.
 * @author J.R. Bhaddacak
 * @version 3.6
 * @since 2.0
 */
class Settings extends SingletonWindow {
	static final Settings INSTANCE = new Settings();
	static TextField[] unusedCharTextFields;
	static TextField[] compCharTextFields;
	private final Label lbExtraPath = new Label();
	
	private Settings() {
		windowWidth = Utilities.getRelativeSize(44);
		windowHeight = Utilities.getRelativeSize(43);
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
		cbExitAsk.setSelected(Boolean.parseBoolean(Utilities.getSetting("exit-ask")));
		cbExitAsk.setOnAction(actionEvent -> Utilities.setSetting("exit-ask", Boolean.toString(cbExitAsk.isSelected())));
		final CheckBox cbDpdLookup = new CheckBox("Use DPD lookup in Pāli text readers");
		cbDpdLookup.setAllowIndeterminate(false);
		cbDpdLookup.setSelected(Boolean.parseBoolean(Utilities.getSetting("dpd-lookup-enable")));
		cbDpdLookup.setOnAction(actionEvent -> Utilities.setSetting("dpd-lookup-enable", Boolean.toString(cbDpdLookup.isSelected())));
		generalBox.getChildren().addAll(cbExitAsk,
								new Separator(), new Label("DPD integration"), cbDpdLookup,
								new Separator(), new Label("Default transliteration to Roman in text readers"));
		final ToggleGroup defRomanGroup = new ToggleGroup();
		for (final EngineType en : EngineType.engines) {
			final int ind = en.ordinal();
			if (ind > 4) break;
			final RadioButton radio = new RadioButton(en.getNameShort());
			radio.setUserData(EngineType.engineCodes[ind]);
			radio.setToggleGroup(defRomanGroup);
			generalBox.getChildren().add(radio);
		}
		final EngineType selectedEngine = EngineType.fromCode(Utilities.getSetting("roman-translit"));
		if (selectedEngine != null)
			defRomanGroup.selectToggle(defRomanGroup.getToggles().get(selectedEngine.ordinal()));
        defRomanGroup.selectedToggleProperty().addListener(observable -> {
			final String code = (String)defRomanGroup.getSelectedToggle().getUserData();
			Utilities.setSetting("roman-translit", code);
			MainProperties.INSTANCE.saveSettings();
		});
		final HBox styleBox = new HBox();
		generalBox.getChildren().addAll(new Separator(), new Label("Default line spacing and color style in text readers"),
										styleBox);
		final ChoiceBox<String> cbLineHeight = new ChoiceBox<>();
		cbLineHeight.getItems().addAll(Arrays.asList(Utilities.lineHeights));
		cbLineHeight.getSelectionModel().select(Utilities.getSetting("lineheight"));
		cbLineHeight.setOnAction(actionEvent -> {
			final String selected = cbLineHeight.getSelectionModel().getSelectedItem();
			Utilities.setSetting("lineheight", selected);
			MainProperties.INSTANCE.saveSettings();
		});
		final ChoiceBox<String> cbBGStyle = new ChoiceBox<>();
		cbBGStyle.getItems().addAll(Arrays.stream(Utilities.Style.values).map(x -> x.getName()).collect(Collectors.toList()));
		final Utilities.Style bg = Utilities.Style.valueOf(Utilities.getSetting("bgstyle"));
		cbBGStyle.getSelectionModel().select(bg.ordinal());
		cbBGStyle.setOnAction(actionEvent -> {
			final int selected = cbBGStyle.getSelectionModel().getSelectedIndex();
			Utilities.setSetting("bgstyle", Utilities.Style.values[selected].toString());
			MainProperties.INSTANCE.saveSettings();
		});
		styleBox.getChildren().addAll(cbLineHeight, cbBGStyle);
		generalTab.setContent(generalBox);
		
		// Font settings
		final Tab fontTab = new Tab("Fonts");
		fontTab.setClosable(false);
		final VBox fontBox = new VBox();
		fontBox.setSpacing(5);
		fontBox.setPadding(new Insets(10));
		fontBox.setPrefHeight(Double.MAX_VALUE);
		fontBox.getChildren().add(new Label("Preferred font used for each script"));
		for (final Utilities.PaliScript script : Utilities.PaliScript.scripts) {
			final HBox scBox = new HBox();
			scBox.setSpacing(5);
			scBox.setAlignment(Pos.CENTER_LEFT);
			final ChoiceBox<String> cb = new ChoiceBox<>();
			cb.setPrefWidth(Utilities.getRelativeSize(15));
			cb.getItems().addAll(Utilities.availFontMap.get(script));
			final String scriptStr = script.toString().toLowerCase();
			final String fn = Utilities.getSetting("font-" + scriptStr);
			cb.getSelectionModel().select(fn);
			cb.setOnAction(actionEvent -> {
				Utilities.setSetting("font-" + scriptStr, cb.getSelectionModel().getSelectedItem());
				MainProperties.INSTANCE.saveSettings();
			});
			final String name = script == Utilities.PaliScript.UNKNOWN ? "Unspecified/Unknown" : script.getName();
			scBox.getChildren().addAll(cb, new Label(name));
			fontBox.getChildren().add(scBox);
		}
		fontTab.setContent(fontBox);

		// Keyboard input method settings
		final Tab keyInputTab = new Tab("Keyboard");
		keyInputTab.setClosable(false);
		final VBox keyInputBox = new VBox();
		keyInputBox.setSpacing(5);
		keyInputBox.setPadding(new Insets(10));
		keyInputBox.setPrefHeight(Double.MAX_VALUE);
		keyInputBox.getChildren().add(new Label("Default input method"));
		final HBox paliMethodBox = new HBox();
		paliMethodBox.setSpacing(5);
		paliMethodBox.getChildren().add(new Label("Pāli: "));
		final ToggleGroup paliMethodGroup = new ToggleGroup();
		final List<PaliTextInput.InputMethod> paliInputMethods = List.of(
			PaliTextInput.InputMethod.UNUSED_CHARS, PaliTextInput.InputMethod.COMPOSITE,
			PaliTextInput.InputMethod.SLP1, PaliTextInput.InputMethod.NORMAL);
		final Map<PaliTextInput.InputMethod, RadioButton> paliInputRadioMap = new EnumMap<>(PaliTextInput.InputMethod.class);
		for (final PaliTextInput.InputMethod im : paliInputMethods) {
			final RadioButton radio = new RadioButton(im.getName());
			radio.setUserData(im);
			radio.setToggleGroup(paliMethodGroup);
			paliMethodBox.getChildren().add(radio);
			paliInputRadioMap.put(im, radio);
		}
		final String paliInputMethodStr = Utilities.getSetting("pali-input-method");
		final PaliTextInput.InputMethod paliInputMethod = PaliTextInput.InputMethod.valueOf(paliInputMethodStr.toUpperCase());
		paliMethodGroup.selectToggle(paliInputRadioMap.get(paliInputMethod));
        paliMethodGroup.selectedToggleProperty().addListener((observable) -> {
			if (paliMethodGroup.getSelectedToggle() != null) {
				final RadioButton selected = (RadioButton)paliMethodGroup.getSelectedToggle();
				final PaliTextInput.InputMethod inputMethod = (PaliTextInput.InputMethod)selected.getUserData();
				Utilities.setSetting("pali-input-method", inputMethod.toString());
				MainProperties.INSTANCE.saveSettings();
			}
		});
		final HBox sktMethodBox = new HBox();
		sktMethodBox.setSpacing(5);
		sktMethodBox.getChildren().add(new Label("Skt.: "));
		final ToggleGroup sktMethodGroup = new ToggleGroup();
		final List<PaliTextInput.InputMethod> sktInputMethods = List.of(
			PaliTextInput.InputMethod.COMPOSITE, PaliTextInput.InputMethod.SLP1, PaliTextInput.InputMethod.NORMAL);
		final Map<PaliTextInput.InputMethod, RadioButton> sktInputRadioMap = new EnumMap<>(PaliTextInput.InputMethod.class);
		for (final PaliTextInput.InputMethod im : sktInputMethods) {
			final RadioButton radio = new RadioButton(im.getName());
			radio.setUserData(im);
			radio.setToggleGroup(sktMethodGroup);
			sktMethodBox.getChildren().add(radio);
			sktInputRadioMap.put(im, radio);
		}
		final String sktInputMethodStr = Utilities.getSetting("sanskrit-input-method");
		final PaliTextInput.InputMethod sktInputMethod = PaliTextInput.InputMethod.valueOf(sktInputMethodStr.toUpperCase());
		sktMethodGroup.selectToggle(sktInputRadioMap.get(sktInputMethod));
        sktMethodGroup.selectedToggleProperty().addListener((observable) -> {
			if (sktMethodGroup.getSelectedToggle() != null) {
				final RadioButton selected = (RadioButton)sktMethodGroup.getSelectedToggle();
				final PaliTextInput.InputMethod inputMethod = (PaliTextInput.InputMethod)selected.getUserData();
				Utilities.setSetting("sanskrit-input-method", inputMethod.toString());
				MainProperties.INSTANCE.saveSettings();
			}
		});
		keyInputBox.getChildren().addAll(paliMethodBox, sktMethodBox);
		
		final Properties settings = MainProperties.INSTANCE.getSettings();
		final Map<String, String> defaultTable = MainProperties.PaliInputProperties.INSTANCE.getDefaultTable();
		final String[] unusedCharNames = MainProperties.PaliInputProperties.INSTANCE.getUnusedCharNames();
		final String[] unusedCharKeys = MainProperties.PaliInputProperties.INSTANCE.getUnusedCharKeys();
		final AnchorPane unusedCharHeadPane = new AnchorPane();
		final Button unusedCharResetButton = new Button("", new TextIcon("arrows-rotate", TextIcon.IconSet.AWESOME));
		unusedCharResetButton.setTooltip(new Tooltip("Reset to default values"));
		unusedCharResetButton.setOnAction(actionEvent -> {
			for (int i=0; i<unusedCharKeys.length; i++) {
				final String def = defaultTable.get(unusedCharKeys[i]);
				Utilities.setSetting(unusedCharKeys[i], def);
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
			unusedCharTextFields[i] = new TextField(settings.getProperty(unusedCharKeys[i]));
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
				Utilities.setSetting(unusedCharKeys[iFinal], newInput);
				MainProperties.INSTANCE.saveSettings();
				Utilities.setupPaliInputCharMap();
			});
		}
		keyInputBox.getChildren().addAll(new Separator(), unusedCharHeadPane, unusedInputGrid);

		final String[] compCharNames = MainProperties.PaliInputProperties.INSTANCE.getCompCharNames();
		final String[] compCharKeys = MainProperties.PaliInputProperties.INSTANCE.getCompCharKeys();
		final AnchorPane compCharHeadPane = new AnchorPane();
		final Button compCharResetButton = new Button("", new TextIcon("arrows-rotate", TextIcon.IconSet.AWESOME));
		compCharResetButton.setTooltip(new Tooltip("Reset to default values"));
		compCharResetButton.setOnAction(actionEvent -> {
			for (int i=0; i<compCharKeys.length; i++) {
				final String def = defaultTable.get(compCharKeys[i]);
				Utilities.setSetting(compCharKeys[i], def);
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
			compCharTextFields[i] = new TextField(settings.getProperty(compCharKeys[i]));
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
				Utilities.setSetting(compCharKeys[iFinal], newInput);
				MainProperties.INSTANCE.saveSettings();
				Utilities.setupPaliInputCharMap();
			});			
		}		
		keyInputBox.getChildren().addAll(new Separator(), compCharHeadPane, compInputGrid);
		
		final AnchorPane slp1HeadPane = new AnchorPane();
		final Button slp1ResetButton = new Button("", new TextIcon("arrows-rotate", TextIcon.IconSet.AWESOME));
		slp1ResetButton.setTooltip(new Tooltip("Reset to default values"));
		final Label slp1HeadLabel = new Label("Key mapping used for SLP1:");
		AnchorPane.setTopAnchor(slp1HeadLabel, 0.0);
		AnchorPane.setLeftAnchor(slp1HeadLabel, 0.0);
		AnchorPane.setTopAnchor(slp1ResetButton, 0.0);
		AnchorPane.setRightAnchor(slp1ResetButton, 0.0);
		slp1HeadPane.getChildren().addAll(slp1HeadLabel, slp1ResetButton);
		final HBox viramaBox = new HBox();
		viramaBox.setAlignment(Pos.CENTER_LEFT);
		final TextField viramaField = new TextField(Utilities.getSetting("virama-key"));
		viramaField.setPrefColumnCount(1);
		viramaField.textProperty().addListener((obs, oldValue, newValue) -> {
			final String newInput;
			if (newValue.isEmpty())
				newInput = MainProperties.INSTANCE.getDefault("virama-key");
			else
				newInput = newValue.substring(0, 1);
			Utilities.setSetting("virama-key", newInput);
			MainProperties.INSTANCE.saveSettings();
		});
		viramaBox.getChildren().addAll(new Label("Virāma key: "), viramaField);
		final HBox mapToBox = new HBox();
		mapToBox.setSpacing(5);
		mapToBox.getChildren().add(new Label("Map SLP1 to: "));
		final ToggleGroup mapToGroup = new ToggleGroup();
		final Map<String, RadioButton> mapToRadioMap = new HashMap<>();
		final String[] mapToNames = { "Devanāgarī", "IAST (Roman)" };
		for (final String name : mapToNames) {
			final RadioButton radio = new RadioButton(name);
			radio.setToggleGroup(mapToGroup);
			final String mCode = name.substring(0, 4).toUpperCase();	
			radio.setSelected(mCode.equals(Utilities.getSetting("slp1-mapto")));
			mapToBox.getChildren().add(radio);
			mapToRadioMap.put(mCode, radio);
		}
        mapToGroup.selectedToggleProperty().addListener(observable -> {
			final String name = ((RadioButton)mapToGroup.getSelectedToggle()).getText();
			final String mCode = name.substring(0, 4).toUpperCase();	
			Utilities.setSetting("slp1-mapto", mCode);
			MainProperties.INSTANCE.saveSettings();
		});
		slp1ResetButton.setOnAction(actionEvent -> {
			final String defViramaKey = MainProperties.INSTANCE.getDefault("virama-key");
			viramaField.setText(defViramaKey);
			final String mapTo = MainProperties.INSTANCE.getDefault("slp1-mapto");
			mapToRadioMap.get(mapTo).setSelected(true);
			MainProperties.INSTANCE.saveSettings();
		});
		keyInputBox.getChildren().addAll(new Separator(), slp1HeadPane, viramaBox, mapToBox);

		keyInputTab.setContent(keyInputBox);
		
		// Dictionaries
		final Tab dictTab = new Tab("Dictionaries");
		dictTab.setClosable(false);
		final VBox dictBox = new VBox();
		dictBox.setSpacing(5);
		dictBox.setPadding(new Insets(10));
		dictBox.setPrefHeight(Double.MAX_VALUE);
		dictBox.getChildren().addAll(new Label("Default inclusion of dictionaries"), new Label("Pāli: "));
		final Pane paliDictBox = (Pane)PaliPlatform.styleableServiceMap.get("paliplatform.dict.DictSelectorBox");
		if (paliDictBox != null)
			dictBox.getChildren().add(paliDictBox);
		dictBox.getChildren().addAll(new Separator(), new Label("Sanskrit:  "));
		final Pane sktDictBox = (Pane)PaliPlatform.styleableServiceMap.get("paliplatform.sanskrit.DictSelectorBox");
		if (sktDictBox != null)
			dictBox.getChildren().add(sktDictBox);
		dictTab.setContent(dictBox);

		// add tabs
		tabPane.getTabs().addAll(generalTab, fontTab, keyInputTab, dictTab);
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
