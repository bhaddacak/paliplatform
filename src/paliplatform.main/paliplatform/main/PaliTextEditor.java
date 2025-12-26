/*
 * PaliTextEditor.java
 *
 * Copyright (C) 2023-2026 J. R. Bhaddacak 
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
import paliplatform.base.Utilities.PaliScript;
import paliplatform.base.ScriptTransliterator.EngineType;

import javafx.application.Platform;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.event.*;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;

import java.io.*;
import java.util.*;
import java.util.stream.*;
import java.util.regex.*;
import java.text.Normalizer;
import java.text.Normalizer.Form;
import javax.print.*;
import javax.print.attribute.*;

/** 
 * A general text editor for Pali.
 * @author J.R. Bhaddacak
 * @version 3.7
 * @since 2.0
 */
public class PaliTextEditor extends BorderPane {
	private static final String DEFAULT_FILENAME = "untitled.txt";
	private final PaliTextInput textInput;
	private final TextArea area;
	private String fileName = DEFAULT_FILENAME;
	private final SimpleObjectProperty<Stage> theStage = new SimpleObjectProperty<>(null);
	private final SimpleObjectProperty<File> theFile = new SimpleObjectProperty<>(null);
	private final SimpleBooleanProperty saveable = new SimpleBooleanProperty(false);
	private final CommonWorkingToolBar toolBar;
	private final CheckMenuItem saveOnCloseMenuItem = new CheckMenuItem("Autosave on close");
	private final CheckMenuItem noAskOnCloseMenuItem = new CheckMenuItem("Never ask on close");
	private final CheckMenuItem alsoNumberMenuItem = new CheckMenuItem("Convert Roman numbers");
	private final CheckMenuItem romanAsSanskritMenuItem = new CheckMenuItem("Roman as Sanskrit");
	final MenuItem findNextMenuItem = new MenuItem("Find _Next");
	final MenuItem findPrevMenuItem = new MenuItem("Find Pre_v");	
	private final ToggleGroup convertFromGroup = new ToggleGroup();
	private final ToggleGroup romanDefaultGroup = new ToggleGroup();
	private final FindReplaceBox findReplaceBox = new FindReplaceBox(this);
	private final TextField findInput = findReplaceBox.getFindTextField();
	private final TextField replaceInput = findReplaceBox.getReplaceTextField();
	private final ComboBox<String> findInputCombo = findReplaceBox.getFindComboBox();
	private final ComboBox<String> replaceInputCombo = findReplaceBox.getReplaceComboBox();
	private final InfoPopup infoPopup = new InfoPopup();
	private final List<int[]> regexFindResult = new ArrayList<>();
	private Runnable afterSaveCallback;
	private boolean caseSensitive = false;
	private boolean wholeWord = false;
	private boolean regexSearch = false;
	private boolean regexMode = false;
	private boolean replaceMode = false;
	private SimpleBooleanProperty searchTextFound = new SimpleBooleanProperty(false);
	private int currFoundIndex = 0;
	private static enum TexConvertMode { SIMPLE, BRACES, WITH_A }
	
	public PaliTextEditor(final Object[] args) {
		// add text area
		textInput = new PaliTextInput(PaliTextInput.InputType.AREA);
		textInput.setInputMethod(PaliTextInput.InputMethod.NORMAL);
		saveable.bindBidirectional(textInput.changeProperty());
		area = (TextArea) textInput.getInput();
		area.setWrapText(true);
		area.setPrefWidth(Utilities.getRelativeSize(44));
		area.setPrefHeight(Utilities.getRelativeSize(27));
		setCenter(area);
		area.setOnDragOver(new EventHandler<DragEvent>() {
			@Override
			public void handle(final DragEvent event) {
				final Dragboard db = event.getDragboard();
				if (event.getGestureSource() != area && (db.hasFiles() || db.hasString()))
					event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
				event.consume();
			}
		});
		area.setOnDragDropped(new EventHandler<DragEvent>() {
			@Override
			public void handle(final DragEvent event) {
				final Dragboard db = event.getDragboard();
				if (db.hasFiles()) {
					final File file = db.getFiles().get(0);
					if (theFile.get() == null && area.getText().isEmpty())
						Platform.runLater(() ->	openFile(file));
					else
						PaliPlatform.openWindow(Utilities.WindowType.EDITOR, new Object[] { file });
				} else if (db.hasString()) {
					final String text = db.getString();
					if (theFile.get() == null)
						area.appendText(text + "\n");
					else
						PaliPlatform.openWindow(Utilities.WindowType.EDITOR, new Object[] { text });
				}
				event.consume();
			}
		});
		toolBar = new CommonWorkingToolBar(area, findInputCombo, replaceInputCombo);
		if (args == null) {
			Platform.runLater(() ->	openFile());
		} else if (args[0] instanceof File) {
			if (args.length > 1)
				afterSaveCallback = (Runnable)args[1];
			Platform.runLater(() ->	openFile((File)args[0]));
		} else {
			final String content = (String)args[0];
			setContent(content);
			setupFontMenu();
			final PaliScript script = Utilities.testLanguage(content);
			toolBar.resetFont(script);
		}
		// add menu and toolbar on the top
		final MenuBar menuBar = new MenuBar();
		// file menu
		final Menu fileMenu = new Menu("_File");
		fileMenu.setMnemonicParsing(true);
		final MenuItem newMenuItem = new MenuItem("_New");
		newMenuItem.setMnemonicParsing(true);
		newMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.N, KeyCombination.SHORTCUT_DOWN));
		newMenuItem.setOnAction(actionEvent -> newFile());
		final MenuItem openMenuItem = new MenuItem("_Open");
		openMenuItem.setMnemonicParsing(true);
		openMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.O, KeyCombination.SHORTCUT_DOWN));
		openMenuItem.setOnAction(actionEvent -> openAFile());
		final MenuItem saveMenuItem = new MenuItem("_Save");
		saveMenuItem.setMnemonicParsing(true);
		saveMenuItem.disableProperty().bind(saveable.not());
		saveMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.S, KeyCombination.SHORTCUT_DOWN));
		saveMenuItem.setOnAction(actionEvent -> saveText());
		final MenuItem saveAsMenuItem = new MenuItem("Save _As...");
		saveAsMenuItem.setMnemonicParsing(true);
		saveAsMenuItem.disableProperty().bind(theFile.isNull());
		saveAsMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.S, KeyCombination.SHORTCUT_DOWN, KeyCombination.SHIFT_DOWN));
		saveAsMenuItem.setOnAction(actionEvent -> saveTextAs(fileName.substring(0, fileName.lastIndexOf(".")) + "_copy.txt"));
		final MenuItem printMenuItem = new MenuItem("_Print");
		printMenuItem.setMnemonicParsing(true);
		printMenuItem.disableProperty().bind(theFile.isNull());
		printMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.P, KeyCombination.SHORTCUT_DOWN));
		printMenuItem.setOnAction(actionEvent -> print());
		final MenuItem closeMenuItem = new MenuItem("_Close");
		closeMenuItem.disableProperty().bind(theStage.isNull());
		closeMenuItem.setMnemonicParsing(true);
		closeMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.W, KeyCombination.SHORTCUT_DOWN));
		closeMenuItem.setOnAction(actionEvent -> closeWindow(null));
		fileMenu.getItems().addAll(newMenuItem, openMenuItem, saveMenuItem, saveAsMenuItem, 
									new SeparatorMenuItem(), printMenuItem, 
									new SeparatorMenuItem(), closeMenuItem);
		// edit menu
		final Menu editMenu = new Menu("_Edit");
		editMenu.setMnemonicParsing(true);
		final MenuItem undoMenuItem = new MenuItem("_Undo");
		undoMenuItem.setMnemonicParsing(true);
		undoMenuItem.disableProperty().bind(area.undoableProperty().not());
		undoMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.Z, KeyCombination.SHORTCUT_DOWN));
		undoMenuItem.setOnAction(actionEvent -> {
			area.undo();
			saveable.set(true);
		});	
		final MenuItem redoMenuItem = new MenuItem("_Redo");
		redoMenuItem.setMnemonicParsing(true);
		redoMenuItem.disableProperty().bind(area.redoableProperty().not());
		redoMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.Y, KeyCombination.SHORTCUT_DOWN));
		redoMenuItem.setOnAction(actionEvent -> {
			area.redo();
			saveable.set(true);
		});	
		final MenuItem cutMenuItem = new MenuItem("Cu_t");
		cutMenuItem.setMnemonicParsing(true);
		cutMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.X, KeyCombination.SHORTCUT_DOWN));
		cutMenuItem.setOnAction(actionEvent -> area.cut());	
		final MenuItem copyMenuItem = new MenuItem("_Copy");
		copyMenuItem.setMnemonicParsing(true);
		copyMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.C, KeyCombination.SHORTCUT_DOWN));
		copyMenuItem.setOnAction(actionEvent -> area.copy());	
		final MenuItem pasteMenuItem = new MenuItem("_Paste");
		pasteMenuItem.setMnemonicParsing(true);
		pasteMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.V, KeyCombination.SHORTCUT_DOWN));
		pasteMenuItem.setOnAction(actionEvent -> area.paste());
		final MenuItem findMenuItem = new MenuItem("_Find");
		findMenuItem.setMnemonicParsing(true);
		findMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.F, KeyCombination.SHORTCUT_DOWN));
		findMenuItem.setOnAction(actionEvent -> openFind(false));
		findNextMenuItem.setMnemonicParsing(true);
		findNextMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.G, KeyCombination.SHORTCUT_DOWN));
		findNextMenuItem.setOnAction(actionEvent -> findNext(+1));
		findPrevMenuItem.setMnemonicParsing(true);
		findPrevMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.G, KeyCombination.SHORTCUT_DOWN, KeyCombination.SHIFT_DOWN));
		findPrevMenuItem.setOnAction(actionEvent -> findNext(-1));
		final MenuItem replaceMenuItem = new MenuItem("Rep_lace");
		replaceMenuItem.setMnemonicParsing(true);
		replaceMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.F, KeyCombination.SHORTCUT_DOWN, KeyCombination.SHIFT_DOWN));
		replaceMenuItem.setOnAction(actionEvent -> openFind(true));
		
		editMenu.getItems().addAll(undoMenuItem, redoMenuItem, 
									new SeparatorMenuItem(), cutMenuItem, copyMenuItem, pasteMenuItem,
									new SeparatorMenuItem(), findMenuItem, findNextMenuItem, findPrevMenuItem, replaceMenuItem);
		// convert menu
		final Menu convertFromMenu = new Menu("F_rom");
		convertFromMenu.setMnemonicParsing(true);
		final Menu convertToMenu = new Menu("_Convert to");
		convertToMenu.setMnemonicParsing(true);
		for (final PaliScript sc : PaliScript.scripts) {
			final String scName = sc.ordinal() == 0 ? "Auto-detected" : sc.getName();
			final RadioMenuItem fromItem = new RadioMenuItem(scName);
			fromItem.setUserData(sc);
			fromItem.setToggleGroup(convertFromGroup);
			convertFromMenu.getItems().add(fromItem);
			if (sc.ordinal() == 0) continue;
			final MenuItem toItem = new MenuItem(scName);
			toItem.setOnAction(actionEvent -> convertTo(sc));
			convertToMenu.getItems().add(toItem);
		}
		convertFromGroup.selectToggle(convertFromGroup.getToggles().get(0));
		// tools menu
		final Menu toolsMenu = new Menu("_Tools");
		toolsMenu.setMnemonicParsing(true);
		final MenuItem slp1EncodeMenuItem = new MenuItem("_Encode SLP1");
		slp1EncodeMenuItem.setMnemonicParsing(true);
		slp1EncodeMenuItem.setOnAction(actionEvent -> encodeSlp1(true));	
		final MenuItem slp1DecodeMenuItem = new MenuItem("_Decode SLP1 (to IAST)");
		slp1DecodeMenuItem.setMnemonicParsing(true);
		slp1DecodeMenuItem.setOnAction(actionEvent -> encodeSlp1(false));	
		final MenuItem composeMenuItem = new MenuItem("_Compose characters");
		composeMenuItem.setMnemonicParsing(true);
		composeMenuItem.setOnAction(actionEvent -> composeChars(true));	
		final MenuItem decomposeMenuItem = new MenuItem("_Decompose characters");
		decomposeMenuItem.setMnemonicParsing(true);
		decomposeMenuItem.setOnAction(actionEvent -> composeChars(false));	
		final MenuItem removeAccentsMenuItem = new MenuItem("_Remove diacritics");
		removeAccentsMenuItem.setMnemonicParsing(true);
		removeAccentsMenuItem.setOnAction(actionEvent -> removeAccents());	
		final MenuItem reformatMenuItem = new MenuItem("Re_format CST4 text");
		reformatMenuItem.setMnemonicParsing(true);
		reformatMenuItem.setOnAction(actionEvent -> reformatCST4());	
		final MenuItem calMetersMenuItem = new MenuItem("Calculate _meters");
		calMetersMenuItem.setMnemonicParsing(true);
		calMetersMenuItem.setOnAction(actionEvent -> calculateMeters());
		toolsMenu.getItems().addAll(slp1EncodeMenuItem, slp1DecodeMenuItem,
									new SeparatorMenuItem(), composeMenuItem, decomposeMenuItem, removeAccentsMenuItem, reformatMenuItem,
									new SeparatorMenuItem(), calMetersMenuItem);

		final SimpleService verseAnalyzer = (SimpleService)PaliPlatform.simpleServiceMap.get("paliplatform.grammar.ProsodyLauncher");
		if (verseAnalyzer != null) {
			final MenuItem analyzeMenuItem = new MenuItem("_Analyze the stanza/text");
			analyzeMenuItem.setMnemonicParsing(true);
			analyzeMenuItem.setOnAction(actionEvent -> openAnalyzer(verseAnalyzer));
			toolsMenu.getItems().add(analyzeMenuItem);
		}
		final SimpleService textReader = (SimpleService)PaliPlatform.simpleServiceMap.get("paliplatform.sentence.ReaderLauncher");
		if (textReader != null) {
			final MenuItem readTextMenuItem = new MenuItem("Read _text");
			readTextMenuItem.setMnemonicParsing(true);
			readTextMenuItem.setOnAction(actionEvent -> openReader(textReader));
			toolsMenu.getItems().add(readTextMenuItem);
		}
		final MenuItem changeMdotAboveMenuItem = new MenuItem("Change ṃ to ṁ");
		changeMdotAboveMenuItem.setOnAction(actionEvent -> replaceChar('ṃ', 'ṁ'));
		final MenuItem changeMdotBelowMenuItem = new MenuItem("Change ṁ to ṃ");
		changeMdotBelowMenuItem.setOnAction(actionEvent -> replaceChar('ṁ', 'ṃ'));
		final MenuItem toUpperCaseMenuItem = new MenuItem("Change to _uppercase");
		toUpperCaseMenuItem.setMnemonicParsing(true);
		toUpperCaseMenuItem.setOnAction(actionEvent -> changeCase(true));
		final MenuItem toLowerCaseMenuItem = new MenuItem("Change to _lowercase");
		toLowerCaseMenuItem.setMnemonicParsing(true);
		toLowerCaseMenuItem.setOnAction(actionEvent -> changeCase(false));
		final MenuItem toSentCaseMenuItem = new MenuItem("Change to _sentence case");
		toSentCaseMenuItem.setMnemonicParsing(true);
		toSentCaseMenuItem.setOnAction(actionEvent -> changeToSentCase());
		final Menu sortMenu = new Menu("Sort");
		final MenuItem sortPaliAscMenuItem = new MenuItem("Pāli sort ascendingly");
		sortPaliAscMenuItem.setOnAction(actionEvent -> paliSort(true));
		final MenuItem sortPaliDesMenuItem = new MenuItem("Pāli sort descendingly");
		sortPaliDesMenuItem.setOnAction(actionEvent -> paliSort(false));
		final MenuItem sortSktAscMenuItem = new MenuItem("Sanskrit sort ascendingly");
		sortSktAscMenuItem.setOnAction(actionEvent -> sktSort(true));
		final MenuItem sortSktDesMenuItem = new MenuItem("Sanskrit sort descendingly");
		sortSktDesMenuItem.setOnAction(actionEvent -> sktSort(false));
		sortMenu.getItems().addAll(sortPaliAscMenuItem, sortPaliDesMenuItem, sortSktAscMenuItem, sortSktDesMenuItem);
		final Menu paliToTexMenu = new Menu("Pāli to TeX");
		final MenuItem simpleP2TMenuItem = new MenuItem("Style 1: \\~n");
		simpleP2TMenuItem.setOnAction(actionEvent -> paliToTex(TexConvertMode.SIMPLE));
		final MenuItem bracesP2TMenuItem = new MenuItem("Style 2: \\~{n}");
		bracesP2TMenuItem.setOnAction(actionEvent -> paliToTex(TexConvertMode.BRACES));
		final MenuItem withaP2TMenuItem = new MenuItem("Style 3: \\a{~}n");
		withaP2TMenuItem.setOnAction(actionEvent -> paliToTex(TexConvertMode.WITH_A));
		paliToTexMenu.getItems().addAll(simpleP2TMenuItem, bracesP2TMenuItem, withaP2TMenuItem);
		final MenuItem texToPaliMenuItem = new MenuItem("TeX to Pāli");
		texToPaliMenuItem.setOnAction(actionEvent -> texToPali());
		toolsMenu.getItems().addAll(new SeparatorMenuItem(), changeMdotAboveMenuItem, changeMdotBelowMenuItem,
									toUpperCaseMenuItem, toLowerCaseMenuItem, toSentCaseMenuItem, sortMenu,
									new SeparatorMenuItem(), paliToTexMenu, texToPaliMenuItem);
		// options menu
		final Menu optionsMenu = new Menu("_Options");
		final CheckMenuItem wrapTextMenuItem = new CheckMenuItem("Wrap text");
		wrapTextMenuItem.selectedProperty().bindBidirectional(area.wrapTextProperty());
		saveOnCloseMenuItem.setSelected(false);
		noAskOnCloseMenuItem.setSelected(!Boolean.parseBoolean(Utilities.getSetting("editor-close-ask")));
		noAskOnCloseMenuItem.setOnAction(actionEvent -> 
				Utilities.setSetting("editor-close-ask", Boolean.toString(!noAskOnCloseMenuItem.isSelected())));
		final Menu romanDefMenu = new Menu("Roman transliteration");
		for (final EngineType en : EngineType.forRoman) {
			final RadioMenuItem enItem = new RadioMenuItem(en.getNameShort());
			enItem.setUserData(en);
			enItem.setToggleGroup(romanDefaultGroup);
			romanDefMenu.getItems().add(enItem);
		}
		romanDefaultGroup.selectToggle(romanDefaultGroup.getToggles().get(2));
		alsoNumberMenuItem.setSelected(true);
		romanAsSanskritMenuItem.setSelected(false);
		final MenuItem loadTestDataMenuItem = new MenuItem("Load test data");
		loadTestDataMenuItem.setOnAction(actionEvent -> loadTestData());
		optionsMenu.getItems().addAll(wrapTextMenuItem, saveOnCloseMenuItem, noAskOnCloseMenuItem,
									new SeparatorMenuItem(), romanDefMenu, alsoNumberMenuItem, romanAsSanskritMenuItem,
									new SeparatorMenuItem(), loadTestDataMenuItem);
		
		menuBar.getMenus().addAll(fileMenu, editMenu, convertFromMenu, convertToMenu, toolsMenu, optionsMenu);
		
		// tool bar
		Platform.runLater(() -> toolBar.resetFont(Integer.valueOf(Utilities.getSetting("other-fontsize"))));
		// config some buttons and add new ones
		toolBar.saveTextButton.setTooltip(new Tooltip("Save"));
		toolBar.saveTextButton.disableProperty().bind(saveable.not());
		toolBar.saveTextButton.setOnAction(actionEvent -> saveText());		
		toolBar.copyButton.setOnAction(actionEvent -> copyText());
		final Button helpButton = new Button("", new TextIcon("circle-question", TextIcon.IconSet.AWESOME));
		helpButton.setOnAction(actionEvent -> infoPopup.showPopup(helpButton, InfoPopup.Pos.BELOW_CENTER, true));
		
		toolBar.getItems().addAll(new Separator(), textInput.getMethodButton(), helpButton);
		
		// config Find & Replace Box
		findReplaceBox.getFindButton().setOnAction(actionEvent -> doRegExFind());
		findReplaceBox.getReplaceButton().setOnAction(actionEvent -> replaceFirst());
		findReplaceBox.getReplaceAllButton().setOnAction(actionEvent -> replaceAll());
		findReplaceBox.getPrevButton().setOnAction(actionEvent -> findNext(-1));
		findReplaceBox.getNextButton().setOnAction(actionEvent -> findNext(+1));
		findReplaceBox.getCaseSensitiveMenuItem().setOnAction(actionEvent -> startSearch());
		findReplaceBox.getWholeWordMenuItem().setOnAction(actionEvent -> startSearch());
		findReplaceBox.getRegexMenuItem().setOnAction(actionEvent -> {
			findReplaceBox.clearOptionsForRegex();
			startSearch();
		});
		findReplaceBox.getCloseButton().setOnAction(actionEvent -> setBottom(null));
		findInputCombo.setOnShowing(e -> recordQuery(findReplaceBox.getFindTextInput()));
		findInput.textProperty().addListener((obs, oldValue, newValue) -> {
			startSearch();
		});
		findInputCombo.setOnKeyPressed(keyEvent -> {
			if (keyEvent.getEventType() == KeyEvent.KEY_PRESSED) {
				final KeyCode key = keyEvent.getCode();
				if (key == KeyCode.ENTER) {
					startSearch();
				} else if (key == KeyCode.ESCAPE) {
					clearFindInput();
				}
			}
		});
		replaceInputCombo.setOnKeyPressed(keyEvent -> {
			if (keyEvent.getEventType() == KeyEvent.KEY_PRESSED) {
				final KeyCode key = keyEvent.getCode();
				if (key == KeyCode.ESCAPE) {
					clearReplaceInput();
				}
			}
		});
		findReplaceBox.getClearFindButton().setOnAction(actionEvent -> clearFindInput());
		findReplaceBox.getClearReplaceButton().setOnAction(actionEvent -> clearReplaceInput());
		// add components
		final VBox topBox = new VBox();
		topBox.getChildren().addAll(menuBar, toolBar);
		setTop(topBox);
		// add find & replace box first, and remove it; this makes the focus request possible
		setBottom(findReplaceBox);
		Platform.runLater(() -> setBottom(null));
		// prepare info popup
		infoPopup.setContentWithText(PaliPlatform.getTextResource("info-text-editor.txt"));
		infoPopup.setTextWidth(Utilities.getRelativeSize(40));		
		// one-time init
		if (Utilities.texConvMap.isEmpty())
			Utilities.loadTexConv();
	}

	public void setStage(final Stage stage) {
		theStage.set(stage);
		stage.setTitle(fileName);
		stage.setOnCloseRequest(new EventHandler<WindowEvent>() {  
			@Override
			public void handle(final WindowEvent event) {
				closeWindow(event);
			}
		});
	}
	
	public final void setContent(final String text) {
		area.setText(text);
	}

	public void setupFontMenu() {
		toolBar.setupFontMenu(PaliScript.UNKNOWN);
		resetFont();
	}

	private void openAFile() {
		if (saveable.get()) {
			if (saveOnCloseMenuItem.isSelected()) {
				saveText();
				openFile();
			} else {
				final ConfirmAlert saveAlert = new ConfirmAlert(theStage.get(), ConfirmAlert.ConfirmType.SAVE);
				final Optional<ButtonType> result = saveAlert.showAndWait();
				if (result.isPresent()) {
					if (result.get() == saveAlert.getConfirmButtonType()) {
						saveText();
						openFile();
					} else {
						if (result.get() == saveAlert.getDiscardButtonType())
							openFile();
					}
				}
			}
		} else {
			openFile();
		}
		toolBar.resetFont();
	}
		
	public final boolean openFile() {
		final File file = Utilities.selectTextFile(theStage.get());
		return openFile(file);
	}

	public final boolean openFile(final File file) {
		boolean success = false;
		if (file != null) {
			fileName = file.getName();
			final Stage stg = theStage.get();
			if (stg != null) stg.setTitle(fileName);
			final PaliTextInput.InputMethod saveInputMethod = textInput.getInputMethod();
			textInput.changeInputMethod(PaliTextInput.InputMethod.NONE);
			final String content = Utilities.getTextFileContent(file);
			setupFontMenu();
			final PaliScript script = Utilities.testLanguage(content);
			toolBar.resetFont(script);
			area.setText(content);
			textInput.changeInputMethod(saveInputMethod);
			success = true;
			theFile.set(file);
			setInitialValues();
		}
		return success;
	}
	
	private void setInitialValues() {
		saveable.set(false);
		area.setWrapText(true);
		saveOnCloseMenuItem.setSelected(false);
		noAskOnCloseMenuItem.setSelected(!Boolean.parseBoolean(Utilities.getSetting("editor-close-ask")));
		searchTextFound.set(false);
		regexMode = false;
		replaceMode = false;
		findReplaceBox.clearInputs();
		findReplaceBox.clearOptions();
		convertFromGroup.selectToggle(convertFromGroup.getToggles().get(0));
		alsoNumberMenuItem.setSelected(true);
		romanAsSanskritMenuItem.setSelected(false);
		romanDefaultGroup.selectToggle(romanDefaultGroup.getToggles().get(2));
		setBottom(null);
	}
		
	private void newFile() {
		if (saveable.get()) {
			if (saveOnCloseMenuItem.isSelected()) {
				saveText();
				clearEditor();
			} else {
				final ConfirmAlert saveAlert = new ConfirmAlert(theStage.get(), ConfirmAlert.ConfirmType.SAVE);
				final Optional<ButtonType> result = saveAlert.showAndWait();
				if (result.isPresent()) {
					if (result.get() == saveAlert.getConfirmButtonType()) {
						saveText();
						clearEditor();
					} else {
						if (result.get() == saveAlert.getDiscardButtonType())
							clearEditor();
					}
				}
			}
		} else {
			clearEditor();
		}
	}
	
	public void clearEditor() {
		fileName = DEFAULT_FILENAME;
		final Stage stg = theStage.get();
		if (stg != null) stg.setTitle(fileName);
		theFile.set(null);
		area.clear();
		setInitialValues();
		resetFont();
	}
	
	public void resetFont(final PaliScript script) {
		toolBar.resetFont(script);
	}

	public void resetFont(final String fontname) {
		toolBar.resetFont(fontname);
	}
	
	public void resetFont() {
		toolBar.resetFont(Integer.valueOf(Utilities.getSetting("other-fontsize")));
	}
	
	private void copyText() {
		if (area.getSelection().getLength() > 0)
			area.copy();
		else
			Utilities.copyText(area.getText());
	}
	
	private void saveText() {
		if (theFile.get() == null) {
			saveTextAs(fileName);
		} else {
			final File file = theFile.get();
			Utilities.saveText(area.getText(), file);
			if (afterSaveCallback != null)
				afterSaveCallback.run();
			saveable.set(false);
		}
	}
	
	private void saveTextAs(final String newname) {
		final File file = Utilities.saveText(area.getText(), newname, theStage.get());
		if (file != null) {
			fileName = file.getName();
			final Stage stg = theStage.get();
			if (stg != null) stg.setTitle(fileName);
			theFile.set(file);
			saveable.set(false);
		}
	}
	
	private void print() {
        PrintRequestAttributeSet pras = new HashPrintRequestAttributeSet();
        DocFlavor flavor = DocFlavor.INPUT_STREAM.AUTOSENSE;
        PrintService printService[] = PrintServiceLookup.lookupPrintServices(flavor, pras);
		if (printService == null || printService.length == 0) {
			final Alert noPrintAlert = new Alert(Alert.AlertType.ERROR, "No print service found");
			noPrintAlert.setHeaderText(null);
			noPrintAlert.showAndWait();
		} else {
			PrintService defaultService = PrintServiceLookup.lookupDefaultPrintService();
			PrintService service = ServiceUI.printDialog(null, 200, 200, printService, defaultService, flavor, pras);
			if (service != null) {
				try {
					DocPrintJob job = service.createPrintJob();
					FileInputStream fis = new FileInputStream(theFile.get());
					DocAttributeSet das = new HashDocAttributeSet();
					Doc doc = new SimpleDoc(fis, flavor, das);
					job.print(doc, pras);
				} catch (FileNotFoundException | PrintException e) {
					System.err.println(e);
				}
			}
		}
	}
	
	public SimpleBooleanProperty searchTextFoundProperty() {
		return searchTextFound;
	}
	
	private void clearFindInput() {
		recordQuery(findReplaceBox.getFindTextInput());
		findInput.clear();
		findInputCombo.setValue(null);
		searchTextFound.set(false);
		regexFindResult.clear();
		setDisableNextPrev(wholeWord || regexSearch);
		area.deselect();		
	}
	
	private void clearReplaceInput() {
		recordQuery(findReplaceBox.getReplaceTextInput());
		replaceInput.clear();		
	}
	
	private void openFind(final boolean withReplace) {
		findReplaceBox.showReplace(withReplace);
		setBottom(findReplaceBox);
		findReplaceBox.getFindTextField().requestFocus();
	}
	
	public void startSearch() {
		caseSensitive = findReplaceBox.caseSensitiveProperty().get();
		wholeWord = findReplaceBox.wholeWordProperty().get();
		regexSearch = findReplaceBox.regexSearchProperty().get();
		setDisableNextPrev(wholeWord || regexSearch);
		replaceMode = false;
		if (wholeWord || regexSearch) {
			doRegExFind();
		} else {
			final String strInput = findInput.getText();
			if (!strInput.isEmpty()) {
				final String strQuery = Normalizer.normalize(strInput, Form.NFC);
				startSimpleSearch(strQuery);
			}
		}
	}
	
	private void startSimpleSearch(final String query) {
		regexMode = false;
		searchTextFound.set(false);
		currFoundIndex = 0;
		search(query, +1);
		findInputCombo.commitValue();		
	}
	
	private void search(final String strToFind, final int direction) {
		int foundPos = -1;
		if (caseSensitive) {
			// case sensitive search
			foundPos = direction > 0 ? area.getText().indexOf(strToFind, currFoundIndex) : area.getText().lastIndexOf(strToFind, currFoundIndex);
		} else {
			// case insentitive search
			final String query = strToFind.toLowerCase();
			final String lowerCaseText = area.getText().toLowerCase();
			foundPos = direction > 0 ? lowerCaseText.indexOf(query, currFoundIndex) : lowerCaseText.lastIndexOf(query, currFoundIndex);
		}
		if (foundPos > -1) {
			searchTextFound.set(true);
			area.selectRange(foundPos, foundPos+strToFind.length());
			currFoundIndex = foundPos;
		} else {
			if (replaceMode) {
				searchTextFound.set(false);
				replaceMode = false;
			} else {
				if (searchTextFound.get()) {
					// circle the search, when at the last return to the first
					currFoundIndex = direction > 0 ? 0 : area.getLength();
					search(strToFind, direction);
				} else {
					area.deselect();
					showMessage("Not found");
				}
			}
		}
	}
	
	private void doRegExFind() {
		currFoundIndex = 0;
		regexMode = true;
		regexFindResult.clear();
		final String strInput = findInput.getText();
		if (strInput.isEmpty())
			return;
		final String strToFind;
		final String textBody;
		if (regexSearch || caseSensitive) {
			strToFind = Normalizer.normalize(strInput, Form.NFC);
			textBody = area.getText();
		} else {
			strToFind = Normalizer.normalize(strInput.toLowerCase(), Form.NFC);
			textBody = area.getText().toLowerCase();
		}
		String patt = "";
		if (wholeWord) {
			patt = "\\b" + strToFind + "\\b";
		} else {
			patt = strToFind;
		}
		try {
			final Pattern regPat = Pattern.compile(patt);
			final Matcher matcher = regPat.matcher(textBody);
			while (matcher.find()) {
				final int[] res = new int[2];
				res[0] = matcher.start(0);
				res[1] = matcher.end(0);
				regexFindResult.add(res);
			}
			if (regexFindResult.isEmpty()) {
				searchTextFound.set(false);
				area.deselect();
				showMessage("Not found");
			} else {
				searchTextFound.set(true);
				findRegExFirst();
			}
		} catch (PatternSyntaxException e) {
			searchTextFound.set(false);
			showMessage("Invalid input pattern");
			setDisableNextPrev(true);
		}
	}
	
	private void findRegExFirst() {
		final int[] pos = regexFindResult.get(currFoundIndex);
		area.selectRange(pos[0], pos[1]);
		setDisableNextPrev(false);		
		showMessage((currFoundIndex+1) + " of " + regexFindResult.size());
	}
	
	private void findNext(final int direction) {
		currFoundIndex += direction;		
		if (regexMode) {
			if (regexFindResult.isEmpty())
				return;
			if (currFoundIndex >= regexFindResult.size())
				currFoundIndex = 0;
			else if (currFoundIndex < 0)
				currFoundIndex = regexFindResult.size() - 1;
			final int[] pos = regexFindResult.get(currFoundIndex);
			area.selectRange(pos[0], pos[1]);
			showMessage((currFoundIndex+1) + " of " + regexFindResult.size());
		} else {
			search(Normalizer.normalize(findInput.getText(), Form.NFC), direction);
		}
	}
	
	public void replaceFirst() {
		if (!area.getSelectedText().isEmpty()) {
			replaceMode = true;
			final String replacement = Normalizer.normalize(replaceInput.getText(), Form.NFC);
			area.replaceSelection(replacement);
			if (regexMode)
				doRegExFind();
			else
				findNext(+1);
			recordQuery(findReplaceBox.getFindTextInput());
			recordQuery(findReplaceBox.getReplaceTextInput());
		}
	}
	
	public void replaceAll() {
		if (!area.getSelectedText().isEmpty()) {
			if (proceedConfirm()) {
				final String replacement = Normalizer.normalize(replaceInput.getText(), Form.NFC);
				if (regexMode) {
						final String text = area.getText();
						final StringBuilder sb = new StringBuilder();
						int start = 0;
						for (int[] range : regexFindResult) {
							sb.append(text.substring(start, range[0]));
							sb.append(replacement);
							start = range[1];
						}
						sb.append(text.substring(start, text.length()));
						area.setText(sb.toString());
						regexFindResult.clear();
				} else {
					final String newText = area.getText().replace(Normalizer.normalize(findInput.getText(), Form.NFC), replacement);
					area.setText(newText);
				}
				recordQuery(findReplaceBox.getFindTextInput());
				recordQuery(findReplaceBox.getReplaceTextInput());				
				searchTextFound.set(false);
				setDisableNextPrev(true);
			}
		}
	}
	
	private void recordQuery(final PaliTextInput ptInput) {
		if (searchTextFound.get())
			ptInput.recordQuery();
	}
	
	private void showMessage(final String text) {
		findReplaceBox.showMessage(text);
	}
	
	private void setDisableNextPrev(final boolean yn) {
		findNextMenuItem.setDisable(yn);
		findPrevMenuItem.setDisable(yn);
		findReplaceBox.getPrevButton().setDisable(yn);
		findReplaceBox.getNextButton().setDisable(yn);			
	}

	private void openNewEditor(final String content) {
		final Object[] args = { content };
		PaliPlatform.openWindow(Utilities.WindowType.EDITOR, args);
	}
	
	private void convertTo(final PaliScript toScript) {
		final String selText = area.getSelectedText();
		final String inputText = selText.isEmpty() ? area.getText() : selText;
		PaliScript fromScript = (PaliScript)convertFromGroup.getSelectedToggle().getUserData();
		final EngineType romanDef = (EngineType)romanDefaultGroup.getSelectedToggle().getUserData();
		if (fromScript == PaliScript.UNKNOWN)
			fromScript = Utilities.testLanguage(inputText);
		// if unknown script eventually, force roman
		if (fromScript == PaliScript.UNKNOWN)
			fromScript = PaliScript.ROMAN;
		final boolean alsoNumber = alsoNumberMenuItem.isSelected();
		final String result = ScriptTransliterator.translitPaliScript(inputText,
				fromScript, toScript, romanDef, alsoNumber, romanAsSanskritMenuItem.isSelected());
		openNewEditor(result);
	}
	
	private void encodeSlp1(final boolean isEncode) {
		final String selText = area.getSelectedText();
		final String inputText = selText.isEmpty() ? area.getText() : selText;
		String result = "";
		if (isEncode) {
			switch (Utilities.testLanguage(inputText)) {
				case ROMAN:
					result = ScriptTransliterator.translitQuick(inputText, EngineType.ROMAN_SKT_DEVA, EngineType.DEVA_ROMAN_SLP1, true);
					break;
				case DEVANAGARI:
					result = ScriptTransliterator.translitQuick(inputText, EngineType.DEVA_ROMAN_SLP1, true);
					break;
				case KHMER:
					result = ScriptTransliterator.translitQuick(inputText, EngineType.KHMER_DEVA, EngineType.DEVA_ROMAN_SLP1, true);
					break;
				case MYANMAR:
					result = ScriptTransliterator.translitQuick(inputText, EngineType.MYANMAR_DEVA, EngineType.DEVA_ROMAN_SLP1, true);
					break;
				case SINHALA:
					result = ScriptTransliterator.translitQuick(inputText, EngineType.SINHALA_DEVA, EngineType.DEVA_ROMAN_SLP1, true);
					break;
				case THAI:
					result = ScriptTransliterator.translitQuick(inputText, EngineType.THAI_DEVA, EngineType.DEVA_ROMAN_SLP1, true);
					break;
			}
		} else {
			result = ScriptTransliterator.translitQuick(inputText, EngineType.SLP1_IAST, true);
		}
		openNewEditor(result);
	}
	
	private void composeChars(final boolean isCompose) {
		final String selText = area.getSelectedText();
		final String inputText = selText.isEmpty() ? area.getText() : selText;
		final String romanText = Utilities.convertToRomanPali(inputText);
		final String result = isCompose ? Normalizer.normalize(romanText, Form.NFC) : Normalizer.normalize(romanText, Form.NFD);
		openNewEditor(result);
	}
	
	private void removeAccents() {
		final String selText = area.getSelectedText();
		final String inputText = selText.isEmpty() ? area.getText() : selText;
		final String romanText = Utilities.convertToRomanPali(inputText);
		final String result = Utilities.removeAccents(romanText);
		openNewEditor(result);
	}
	
	private void reformatCST4() {
		final String selText = area.getSelectedText();
		final String inputText = selText.isEmpty() ? area.getText() : selText;
		final String romanText = Utilities.convertToRomanPali(inputText);
		final String result = reformatCST4(romanText);
		openNewEditor(result);
	}

	private String reformatCST4(final String input) {
		String text = Normalizer.normalize(input, Form.NFD);
		final int len = text.length();
		text = text.replace("" + '\u2016', "|"); // change double bar to '|' first
		text = text.replace("" + '\u00B7', "."); // change one dot leader to period
		final StringBuilder output = new StringBuilder(len);
		text.lines().forEach(line -> {
			if (line.indexOf("|") > -1 ) {
				// replace '|' with '.' and capitalize each sentence
				final String[] sentences = line.stripTrailing().split("\\|");
				for (final String s : sentences) {
					output.append(Utilities.capitalizeFirstLetter(s, true)).append(".");
				}
			} else {
				output.append(Utilities.capitalizeFirstLetter(line, true));
			}
			output.append(System.getProperty("line.separator"));
		});
		return output.toString();
	}

	private void calculateMeters() {
		final String selText = area.getSelectedText();
		final String inputText = selText.isEmpty() ? area.getText() : selText;
		final String romanText = Utilities.convertToRomanPali(inputText);
		openNewEditor(Utilities.addComputedMeters(romanText));
	}

	private void openAnalyzer(final SimpleService service) {
		final String selText = area.getSelectedText();
		final String inputText = selText.isEmpty() ? area.getText() : selText;
		final String romanText = Utilities.convertToRomanPali(inputText);
		service.process(romanText);
	}

	private void openReader(final SimpleService service) {
		final String selText = area.getSelectedText();
		final String inputText = selText.isEmpty() ? area.getText() : selText;
		final String romanText = Utilities.convertToRomanPali(inputText);
		final Object[] args = { romanText };
		service.processArray(args);
	}

	private void changeCase(final boolean isUpper) {
		final String selText = area.getSelectedText();
		final boolean isAll = selText.isEmpty();
		final String inputText = isAll ? area.getText() : selText;
		final String romanText = Utilities.convertToRomanPali(inputText);
		if (isAll) {
			if (proceedConfirm())
				area.setText(toCase(romanText, isUpper));
		} else {
			final IndexRange indRange = area.getSelection();
			final int start = indRange.getStart();
			final StringBuilder text = new StringBuilder(area.getText());
			text.delete(start, indRange.getEnd());
			text.insert(start, toCase(romanText, isUpper));
			area.setText(text.toString());
		}
	}

	private void changeToSentCase() {
		final String selText = area.getSelectedText();
		final boolean isAll = selText.isEmpty();
		final String inputText = isAll ? area.getText() : selText;
		final String romanText = Utilities.convertToRomanPali(inputText);
		if (isAll) {
			if (proceedConfirm())
				area.setText(toSentCase(romanText));
		} else {
			final IndexRange indRange = area.getSelection();
			final int start = indRange.getStart();
			final StringBuilder text = new StringBuilder(area.getText());
			text.delete(start, indRange.getEnd());
			text.insert(start, toSentCase(romanText));
			area.setText(text.toString());
		}
	}

	private String toCase(final String input, final boolean isUpper) {
		final String result = isUpper ? input.toUpperCase() : input.toLowerCase();
		return result;
	}

	private String toSentCase(final String input) {
		// detect only period and question mark (. ?)
		// as ending markers (all abbreviations are affected)
		final char[] chars = input.toCharArray();
		boolean changed = false;
		for (int i = 0; i < chars.length; i++) {
			if (Character.isLetter(chars[i])) {
				if (!changed) {
					chars[i] = Character.toUpperCase(chars[i]);
					changed = true;
				} else {
					continue;
				}
			} else if (chars[i] == '.' || chars[i] == '?') {
				changed = false;
			}
		}
		return new String(chars);
	}

	private void paliSort(final boolean isAsc) {
		if (proceedConfirm()) {
			final Stream<String> allLines = Utilities.convertToRomanPali(area.getText()).lines().map(s -> s.trim());
			final Stream<String> result;
			if (isAsc)
				result = allLines.sorted(Utilities.paliComparator);
			else
				result = allLines.sorted(Utilities.paliComparator.reversed());
			area.setText(result.collect(Collectors.joining((System.getProperty("line.separator")))));
		}
	}

	private void sktSort(final boolean isAsc) {
		if (proceedConfirm()) {
			final Stream<String> allLines = Utilities.convertToRomanSanskrit(area.getText()).lines().map(s -> s.trim());
			final Stream<String> result;
			if (isAsc)
				result = allLines.sorted(Utilities.sktComparator);
			else
				result = allLines.sorted(Utilities.sktComparator.reversed());
			area.setText(result.collect(Collectors.joining((System.getProperty("line.separator")))));
		}
	}

	private void replaceChar(final char fromCh, final char toCh) {
		final String oldText = area.getText();
		String newText = oldText.replace(fromCh, toCh);
		newText = newText.replace(Character.toUpperCase(fromCh), Character.toUpperCase(toCh));
		area.setText(newText);
	}

	private void paliToTex(final TexConvertMode mode) {
		final String selText = Normalizer.normalize(area.getSelectedText(), Form.NFC);
		final boolean isAll = selText.isEmpty();
		final String inputText = isAll ? Normalizer.normalize(area.getText(), Form.NFC) : selText;
		final String romanText = Utilities.convertToRomanPali(inputText);
		if (isAll) {
			if (proceedConfirm())
				area.setText(toTex(romanText, mode));
		} else {
			final IndexRange indRange = area.getSelection();
			final int start = indRange.getStart();
			final StringBuilder text = new StringBuilder(area.getText());
			text.delete(start, indRange.getEnd());
			text.insert(start, toTex(romanText, mode));
			area.setText(text.toString());
		}
	}

	private void texToPali() {
		final String selText = area.getSelectedText();
		final boolean isAll = selText.isEmpty();
		final String inputText = isAll ? area.getText() : selText;
		final String romanText = Utilities.convertToRomanPali(inputText);
		if (isAll) {
			if (proceedConfirm())
				area.setText(fromTex(romanText));
		} else {
			final IndexRange indRange = area.getSelection();
			final int start = indRange.getStart();
			final StringBuilder text = new StringBuilder(area.getText());
			text.delete(start, indRange.getEnd());
			text.insert(start, fromTex(romanText));
			area.setText(text.toString());
		}
	}

	private String toTex(final String text, final TexConvertMode mode) {
		final StringBuilder result = new StringBuilder();
		for (final char ch : text.toCharArray()) {
			if (Utilities.texConvMap.containsKey(ch))
				result.append(Utilities.texConvMap.get(ch).get(mode.ordinal()));
			else
				result.append(ch);
		}
		return result.toString();
	}

	private String fromTex(final String text) {
		String result = text;
		for (final char ch : Utilities.texConvMap.keySet()) {
			final List<String> pattList = Utilities.texConvMap.get(ch);
			result = result.replace(pattList.get(0), "" + ch)
							.replace(pattList.get(1), "" + ch)
							.replace(pattList.get(2), "" + ch);
		}
		return result;
	}

	private void loadTestData() {
		final String testData = PaliPlatform.getTextResource("devatestdata.txt");
		area.setText(testData);
	}

	private boolean proceedConfirm() {
		boolean output = false;
		final String message = "This whole-file operation cannot be undone.\nMake sure the file is well saved."
								+ "\nIf you are aware what you are doing, then proceed.";
		final ConfirmAlert proceedAlert = new ConfirmAlert(theStage.get(), ConfirmAlert.ConfirmType.PROCEED, message);
		final Optional<ButtonType> result = proceedAlert.showAndWait();
		if (result.isPresent()) {
			if (result.get() == proceedAlert.getConfirmButtonType())
				output = true;
		}
		return output;		
	}
	
	private void closeWindow(final WindowEvent event) {
		final Stage stg = theStage.get();
		if (saveable.get()) {
			if (saveOnCloseMenuItem.isSelected()) {
				saveText();
				if (stg != null) stg.hide();
			} else {
				if (noAskOnCloseMenuItem.isSelected()) {
					if (stg != null) stg.hide();
				} else {
					final ConfirmAlert saveAlert = new ConfirmAlert(theStage.get(), ConfirmAlert.ConfirmType.SAVE);
					final Optional<ButtonType> result = saveAlert.showAndWait();
					if (result.isPresent()) {
						if (result.get() == saveAlert.getConfirmButtonType()) {
							saveText();
							if (stg != null) stg.hide();
						} else {
							if (result.get() != saveAlert.getDiscardButtonType()) {
								if (event != null) {
									event.consume();
								}
							} else {
								if (stg != null) stg.hide();
							}
						}
					}
				}
			}
		} else {
			if (stg != null) stg.hide();
		}
	}

}
