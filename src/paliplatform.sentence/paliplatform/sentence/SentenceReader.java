/*
 * SentenceReader.java
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

package paliplatform.sentence;

import paliplatform.base.*;
import paliplatform.dict.*;
import paliplatform.grammar.*;

import java.util.*;
import java.util.stream.*;
import java.util.function.Function;
import java.io.*;
import java.nio.file.*;
import java.nio.charset.StandardCharsets;

import javafx.application.Platform;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.*;
import javafx.scene.input.*;
import javafx.scene.text.*;
import javafx.event.*;
import javafx.geometry.*;
import javafx.beans.property.SimpleBooleanProperty;

/** 
 * The tool facilitating Pali text reading.
 * Formerly this class is named PaliTextReader.
 * @author J.R. Bhaddacak
 * @version 3.0
 * @since 2.0
 */
public class SentenceReader extends BorderPane {
	public static final String TITLE = "Pāli Sentence Reader";
	private static final int TEXT_BASE_SIZE = 150;
	private static final int TRANS_BASE_SIZE = 100;
	private Stage theStage;
	private final String sentenceRoot = Utilities.ROOTDIR + SentenceUtilities.SENTENCESPATH;
	private String sentencePath = sentenceRoot + SentenceUtilities.SENTENCESMAIN;
	private String sequencePath = "";
	private final VBox contentBox = new VBox();
	private final VBox editBox = new VBox();
	private final VBox transBox = new VBox();
	private final SplitPane splitPane = new SplitPane();
	private final Map<String, Variant> variantMap = new HashMap<>();
	private final List<Sentence> sentenceList = new ArrayList<>();
	private final Map<Integer, Boolean> isSenEditedMap = new HashMap<>();
	private final PaliTextInput textInput = new PaliTextInput(PaliTextInput.InputType.AREA);
	private final SimpleBooleanProperty saveableThis = new SimpleBooleanProperty(false);
	private final SimpleBooleanProperty saveableAll = new SimpleBooleanProperty(false);
	private final SimpleBooleanProperty seqEdited = new SimpleBooleanProperty(false);
	private final CommonWorkingToolBar toolBar = new CommonWorkingToolBar(splitPane);
	private final Spinner<Integer> sentenceSpinner = new Spinner<>();
	private final ToggleButton showDetailButton = new ToggleButton("", new TextIcon("glasses", TextIcon.IconSet.AWESOME));		
	private final CheckMenuItem itiReconstructMenuItem = new CheckMenuItem("Reconstruct iti");
	private final CheckMenuItem preItiShortenMenuItem = new CheckMenuItem("Shorten vowel before iti");
	private final CheckMenuItem sandhiCutMenuItem = new CheckMenuItem("Cut sandhi words");
	private final CheckMenuItem usePronMenuItem = new CheckMenuItem("Use pronoun list");
	private final CheckMenuItem useNumberMenuItem = new CheckMenuItem("Use numeral list");
	private final CheckMenuItem useIrrNounMenuItem = new CheckMenuItem("Use irregular noun/adj list");
	private final ToggleButton showAllTransButton = new ToggleButton("", new TextIcon("asterisk", TextIcon.IconSet.AWESOME));
	private final TextIcon lightbulb = new TextIcon("lightbulb", TextIcon.IconSet.AWESOME);
	private final Label translationText = new Label();
	private final ChoiceBox<String> variantChoice = new ChoiceBox<>();
	private final ContextMenu termContextMenu = new ContextMenu();
	private final Label fixedInfoLabel = new Label();
	private final InfoPopup infoPopup = new InfoPopup();
	private final TextArea editArea;
	private Text currSelectedText = null;
	private int currTextSize = TEXT_BASE_SIZE;
	private int currTransSize = TRANS_BASE_SIZE;
	private EditMode currEditMode = EditMode.TEXT;
	private String currSelectedVariant = "";
	private static enum EditMode { TEXT, TRANS }
	
	public SentenceReader(final Object[] args) {
		// add menu toolbar on the top
		final MenuBar menuBar = new MenuBar();
		// Sentence
		final Menu sentenceMenu = new Menu("_Sentence");
		sentenceMenu.setMnemonicParsing(true);
		final MenuItem pasteMenuItem = new MenuItem("_Paste text", new TextIcon("paste", TextIcon.IconSet.AWESOME));
		pasteMenuItem.setMnemonicParsing(true);
		pasteMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.V, KeyCombination.SHORTCUT_DOWN));
		pasteMenuItem.setOnAction(actionEvent -> pasteText());
		final MenuItem storeMenuItem = new MenuItem("_Store this sentence", new TextIcon("download", TextIcon.IconSet.AWESOME));
		storeMenuItem.setMnemonicParsing(true);
		storeMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.S, KeyCombination.SHORTCUT_DOWN));
		storeMenuItem.disableProperty().bind(saveableThis.not());
		storeMenuItem.setOnAction(actionEvent -> storeSentence());
		final MenuItem storeAllEditMenuItem = new MenuItem("Store all edited");
		storeAllEditMenuItem.disableProperty().bind(saveableAll.not());
		storeAllEditMenuItem.setOnAction(actionEvent -> {
			storeAllEdited();
			updateResult();
		});
		final MenuItem removeSentMenuItem = new MenuItem("_Remove this sentence", new TextIcon("trash", TextIcon.IconSet.AWESOME));
		removeSentMenuItem.setMnemonicParsing(true);
		removeSentMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.X, KeyCombination.SHORTCUT_DOWN));
		removeSentMenuItem.setOnAction(actionEvent -> removeSentence());
		final MenuItem openSeqMenuItem = new MenuItem("_Open a sequence", new TextIcon("folder-open", TextIcon.IconSet.AWESOME));
		openSeqMenuItem.setMnemonicParsing(true);
		openSeqMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.O, KeyCombination.SHORTCUT_DOWN));
		openSeqMenuItem.setOnAction(actionEvent -> openSequence());
		final MenuItem saveSeqMenuItem = new MenuItem("S_ave this sequence");
		saveSeqMenuItem.setMnemonicParsing(true);
		saveSeqMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.S, KeyCombination.SHORTCUT_DOWN, KeyCombination.SHIFT_DOWN));
		saveSeqMenuItem.disableProperty().bind(seqEdited.not());
		saveSeqMenuItem.setOnAction(actionEvent -> saveSequence());
		final MenuItem saveSeqAsMenuItem = new MenuItem("Save this sequence as...");
		saveSeqAsMenuItem.disableProperty().bind(saveableAll);
		saveSeqAsMenuItem.setOnAction(actionEvent -> saveSequenceAs());
		final MenuItem updateVarListMenuItem = new MenuItem("Update variant list");
		updateVarListMenuItem.setOnAction(actionEvent -> loadVariantList());
		final MenuItem openSenManagerMenuItem = new MenuItem("Open Sentence Manager", new TextIcon("briefcase", TextIcon.IconSet.AWESOME));
		openSenManagerMenuItem.setOnAction(actionEvent -> {
			SentenceManager.INSTANCE.display();
			SentenceManager.INSTANCE.changeSentPath(new File(sentencePath));
		});
		sentenceMenu.getItems().addAll(pasteMenuItem, storeMenuItem, storeAllEditMenuItem, removeSentMenuItem, 
									new SeparatorMenuItem(), openSeqMenuItem, saveSeqMenuItem, saveSeqAsMenuItem,
									new SeparatorMenuItem(), updateVarListMenuItem, openSenManagerMenuItem);
		// Edit
		final Menu editMenu = new Menu("_Edit");
		editMenu.setMnemonicParsing(true);
		final MenuItem editThisMenuItem = new MenuItem("_Edit this sentence", new TextIcon("pen-fancy", TextIcon.IconSet.AWESOME));
		editThisMenuItem.setMnemonicParsing(true);
		editThisMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.E, KeyCombination.SHORTCUT_DOWN));
		editThisMenuItem.setOnAction(actionEvent -> openEdit(EditMode.TEXT));
		final MenuItem editDictMenuItem1 = new MenuItem("Edit custom dictionary");
		editDictMenuItem1.setOnAction(actionEvent -> editDict());
		final MenuItem editSandhiMenuItem1 = new MenuItem("Edit sandhi list");
		editSandhiMenuItem1.setOnAction(actionEvent -> editSandhi());
		editMenu.getItems().addAll(editThisMenuItem, new SeparatorMenuItem(), editDictMenuItem1, editSandhiMenuItem1);
		// View
		final Menu viewMenu = new Menu("_View");
		viewMenu.setMnemonicParsing(true);
		final MenuItem showSimpleMenuItem = new MenuItem("_Simple");
		showSimpleMenuItem.setMnemonicParsing(true);
		showSimpleMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.U, KeyCombination.SHORTCUT_DOWN));
		showSimpleMenuItem.setOnAction(actionEvent -> {
			showDetailButton.setSelected(false);
			updateResult();
		});
		final MenuItem showDetailMenuItem = new MenuItem("_Detail", new TextIcon("glasses", TextIcon.IconSet.AWESOME));
		showDetailMenuItem.setMnemonicParsing(true);
		showDetailMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.I, KeyCombination.SHORTCUT_DOWN));
		showDetailMenuItem.setOnAction(actionEvent -> {
			showDetailButton.setSelected(true);
			updateResult();
		});
		final MenuItem showTransMenuItem = new MenuItem("Show _translation", new TextIcon("language", TextIcon.IconSet.AWESOME));
		showTransMenuItem.setMnemonicParsing(true);
		showTransMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.T, KeyCombination.SHORTCUT_DOWN));
		showTransMenuItem.setOnAction(actionEvent -> openTrans());
		viewMenu.getItems().addAll(showSimpleMenuItem, showDetailMenuItem, new SeparatorMenuItem(), showTransMenuItem);
		// Go to
		final Menu gotoMenu = new Menu("_Go to");
		gotoMenu.setMnemonicParsing(true);
		final MenuItem goNextTransMenuItem = new MenuItem("_Next sent. with trans.");
		goNextTransMenuItem.setMnemonicParsing(true);
		goNextTransMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.N, KeyCombination.SHORTCUT_DOWN));
		goNextTransMenuItem.setOnAction(actionEvent -> goNext(true));
		final MenuItem goPrevTransMenuItem = new MenuItem("_Prev sent. with trans.");
		goPrevTransMenuItem.setMnemonicParsing(true);
		goPrevTransMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.B, KeyCombination.SHORTCUT_DOWN));
		goPrevTransMenuItem.setOnAction(actionEvent -> goPrev(true));
		final MenuItem goNextNoTransMenuItem = new MenuItem("Next sent. w/o trans.");
		goNextNoTransMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.N, KeyCombination.SHORTCUT_DOWN, KeyCombination.SHIFT_DOWN));
		goNextNoTransMenuItem.setOnAction(actionEvent -> goNext(false));
		final MenuItem goPrevNoTransMenuItem = new MenuItem("Prev sent. w/o trans.");
		goPrevNoTransMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.B, KeyCombination.SHORTCUT_DOWN, KeyCombination.SHIFT_DOWN));
		goPrevNoTransMenuItem.setOnAction(actionEvent -> goPrev(false));
		gotoMenu.getItems().addAll(goNextTransMenuItem, goPrevTransMenuItem, new SeparatorMenuItem(), goNextNoTransMenuItem, goPrevNoTransMenuItem);
		menuBar.getMenus().addAll(sentenceMenu, editMenu, viewMenu, gotoMenu);
		// config some toolbar's buttons
		toolBar.getZoomInButton().setOnAction(actionEvent -> zoom(+10));
		toolBar.getZoomOutButton().setOnAction(actionEvent -> zoom(-10));
		toolBar.getResetButton().setOnAction(actionEvent -> zoom(0));
		toolBar.saveTextButton.setOnAction(actionEvent -> saveText());		
		toolBar.copyButton.setOnAction(actionEvent -> copyText());		
		// add new components
		final Button pasteButton = new Button("", new TextIcon("paste", TextIcon.IconSet.AWESOME));
		pasteButton.setTooltip(new Tooltip("Paste text"));
		pasteButton.setOnAction(actionEvent -> pasteText());
		final Button openSeqButton = new Button("", new TextIcon("folder-open", TextIcon.IconSet.AWESOME));
		openSeqButton.setTooltip(new Tooltip("Open a sequence"));
		openSeqButton.setOnAction(actionEvent -> openSequence());
		final Button saveSenButton = new Button("", new TextIcon("download", TextIcon.IconSet.AWESOME));
		saveSenButton.setTooltip(new Tooltip("Store this sentence"));
		saveSenButton.disableProperty().bind(saveableThis.not());
		saveSenButton.setOnAction(actionEvent -> storeSentence());
		final Button firstSenButton = new Button("", new TextIcon("backward-step", TextIcon.IconSet.AWESOME));
		firstSenButton.setTooltip(new Tooltip("First sentence"));
		firstSenButton.setOnAction(actionEvent -> goToSentence(1));
		final Button lastSenButton = new Button("", new TextIcon("forward-step", TextIcon.IconSet.AWESOME));
		lastSenButton.setTooltip(new Tooltip("Last sentence"));
		lastSenButton.setOnAction(actionEvent -> goToSentence(sentenceList.size()));
		sentenceSpinner.setTooltip(new Tooltip("Go to sentence #"));
		sentenceSpinner.getStyleClass().add(Spinner.STYLE_CLASS_SPLIT_ARROWS_HORIZONTAL);
		sentenceSpinner.setPrefWidth(Utilities.getRelativeSize(7));
		sentenceSpinner.setEditable(true);
		sentenceSpinner.getEditor().setTextFormatter(new TextFormatter<Integer>(Utilities.integerStringConverter, 1));
		sentenceSpinner.valueProperty().addListener((ob, oldv, newv) -> loadSentence(newv - 1));
        sentenceSpinner.getEditor().setOnKeyPressed(keyEvent -> spinnerKeyHandler(keyEvent));
		final Button editButton = new Button("", new TextIcon("pen-fancy", TextIcon.IconSet.AWESOME));
		editButton.setTooltip(new Tooltip("Edit this sentence"));
		editButton.setOnAction(actionEvent -> openEdit(EditMode.TEXT));
		showDetailButton.setTooltip(new Tooltip("Show detail"));
		showDetailButton.setOnAction(actionEvent -> updateResult());
		final Label lbBulb = new Label("", lightbulb);
		final Button transButton = new Button("", new TextIcon("language", TextIcon.IconSet.AWESOME));
		transButton.setTooltip(new Tooltip("Open translation"));
		transButton.setOnAction(actionEvent -> openTrans());
		final MenuButton analyOptionMenu = new MenuButton("", new TextIcon("check-double", TextIcon.IconSet.AWESOME));		
		analyOptionMenu.setTooltip(new Tooltip("Options for analysis"));
		final MenuItem allOptionMenuItem = new MenuItem("Turn on all options");
		allOptionMenuItem.setOnAction(actionEvent -> setAnalysisOptions(true));
		final MenuItem noOptionMenuItem = new MenuItem("Turn off all options");
		noOptionMenuItem.setOnAction(actionEvent -> setAnalysisOptions(false));
		itiReconstructMenuItem.setOnAction(actionEvent -> updateResult());
		preItiShortenMenuItem.disableProperty().bind(itiReconstructMenuItem.selectedProperty().not());
		preItiShortenMenuItem.setOnAction(actionEvent -> updateResult());
		sandhiCutMenuItem.setOnAction(actionEvent -> updateResult());
		usePronMenuItem.setOnAction(actionEvent -> updateResult());
		useNumberMenuItem.setOnAction(actionEvent -> updateResult());
		useIrrNounMenuItem.setOnAction(actionEvent -> updateResult());
		analyOptionMenu.getItems().addAll(allOptionMenuItem, noOptionMenuItem, new SeparatorMenuItem(), 
											itiReconstructMenuItem, preItiShortenMenuItem, sandhiCutMenuItem,
											usePronMenuItem, useNumberMenuItem, useIrrNounMenuItem);
		final Button openSentManButton = new Button("", new TextIcon("briefcase", TextIcon.IconSet.AWESOME));
		openSentManButton.setTooltip(new Tooltip("Open Sentence Manager"));
		openSentManButton.setOnAction(actionEvent -> {
			SentenceManager.INSTANCE.display();
			SentenceManager.INSTANCE.refresh();
		});
		final Button helpButton = new Button("", new TextIcon("circle-question", TextIcon.IconSet.AWESOME));
		helpButton.setOnAction(actionEvent -> infoPopup.showPopup(helpButton, InfoPopup.Pos.BELOW_RIGHT, true));
		toolBar.getItems().addAll(pasteButton, openSeqButton, saveSenButton,
									new Separator(), firstSenButton, sentenceSpinner, lastSenButton,
									editButton, showDetailButton, lbBulb, transButton, analyOptionMenu,
									openSentManButton, helpButton);
		final VBox topBox = new VBox();
		topBox.getChildren().addAll(menuBar, toolBar);
		setTop(topBox);
		
		// set status bar at the bottom
		final AnchorPane statusPane = new AnchorPane();
		AnchorPane.setBottomAnchor(fixedInfoLabel, 0.0);
		AnchorPane.setRightAnchor(fixedInfoLabel, 0.0);
		fixedInfoLabel.setStyle("-fx-font-family:'" + Utilities.FONTMONO +"';-fx-font-size:85%;");
		statusPane.getChildren().add(fixedInfoLabel);
		setBottom(statusPane);
		
		// add split pane at the center
		splitPane.setOrientation(Orientation.VERTICAL);
		contentBox.setPadding(new Insets(3, 0, 0, 3));
		final ScrollPane scrollPane = new ScrollPane(contentBox);
		splitPane.getItems().add(scrollPane);
		setCenter(splitPane);
		
		// one time init
		final Path sentPath = Path.of(sentencePath);
		try {
			if (Files.notExists(sentPath))
				Files.createDirectories(sentPath);
		} catch (IOException e) {
			System.err.println(e);
		}
		Utilities.initializeDictDB();
		if (SentenceUtilities.simpleServiceMap == null) 
			SentenceUtilities.simpleServiceMap = SentenceUtilities.getSimpleServices();
		if (DictUtilities.customDictFile == null)
			DictUtilities.customDictFile = new File(Utilities.ROOTDIR + Utilities.RULESPATH + DictUtilities.CUSTOM_DICT);
		if (!DictUtilities.customDictFile.exists())
			DictUtilities.createCustomDictFile();
		if (DictUtilities.sandhiFile == null)
			DictUtilities.sandhiFile = new File(Utilities.ROOTDIR + Utilities.RULESPATH + DictUtilities.SANDHI_LIST);
		if (!DictUtilities.sandhiFile.exists())
			DictUtilities.createSandhiFile();
		DictUtilities.loadCustomDict();
		DictUtilities.loadSandhiList();
		DictUtilities.loadCPEDTerms(); // used in dict look up		
		Platform.runLater(() -> {
			GrammarUtilities.createDeclPronounsMap();
			GrammarUtilities.createDeclNumbersMap();
		});
		// prepare edit box
		editArea = (TextArea) textInput.getInput();
		editArea.setWrapText(true);
		VBox.setVgrow(editArea, Priority.ALWAYS);
		final AnchorPane editTBPane = new AnchorPane();
		final HBox leftEditButtGroup = new HBox();
		final Button inputMethodButton = textInput.getMethodButton();
		final Button submitButton = new Button("Submit");
		submitButton.setOnAction(actionEvent -> submitEdit());
		leftEditButtGroup.getChildren().addAll(inputMethodButton, submitButton);
		final Button closeEditButton = new Button("", new TextIcon("xmark", TextIcon.IconSet.AWESOME));
		closeEditButton.setOnAction(actionEvent -> closeEditPane());
		AnchorPane.setTopAnchor(leftEditButtGroup, 0.0);
		AnchorPane.setLeftAnchor(leftEditButtGroup, 0.0);
		AnchorPane.setTopAnchor(closeEditButton, 0.0);
		AnchorPane.setRightAnchor(closeEditButton, 0.0);
		editTBPane.getChildren().addAll(leftEditButtGroup, closeEditButton);
		editBox.getChildren().addAll(editTBPane, editArea);
		// prepare translation box
		final AnchorPane transTBPane = new AnchorPane();
		final HBox leftTransButtGroup = new HBox();
		showAllTransButton.setTooltip(new Tooltip("Show all variants"));
		showAllTransButton.setOnAction(actionEvent -> updateTrans());
		variantChoice.disableProperty().bind(showAllTransButton.selectedProperty());
		variantChoice.setOnAction(actionEvent -> variantChoiceSelected());
		final Button editTransButton = new Button("", new TextIcon("pen-fancy", TextIcon.IconSet.AWESOME));
		editTransButton.setTooltip(new Tooltip("Edit this translation"));
		editTransButton.disableProperty().bind(showAllTransButton.selectedProperty());
		editTransButton.setOnAction(actionEvent -> openEdit(EditMode.TRANS));
		final Button delSentButton = new Button("", new TextIcon("trash", TextIcon.IconSet.AWESOME));
		delSentButton.setTooltip(new Tooltip("Delete this translation"));
		delSentButton.disableProperty().bind(showAllTransButton.selectedProperty());
		delSentButton.setOnAction(actionEvent -> deleteTrans());
		leftTransButtGroup.getChildren().addAll(showAllTransButton, variantChoice, editTransButton, delSentButton);
		final Button closeTransButton = new Button("", new TextIcon("xmark", TextIcon.IconSet.AWESOME));
		closeTransButton.setOnAction(actionEvent -> closeTransPane());
		AnchorPane.setTopAnchor(leftTransButtGroup, 0.0);
		AnchorPane.setLeftAnchor(leftTransButtGroup, 0.0);
		AnchorPane.setTopAnchor(closeTransButton, 0.0);
		AnchorPane.setRightAnchor(closeTransButton, 0.0);
		transTBPane.getChildren().addAll(leftTransButtGroup, closeTransButton);
		translationText.setWrapText(true);
		final VBox tbox = new VBox();
		tbox.setPadding(new Insets(3, 0, 0, 3));
		tbox.prefWidthProperty().bind(this.widthProperty().subtract(15));
		VBox.setVgrow(translationText, Priority.ALWAYS);
		tbox.getChildren().add(translationText);
		final ScrollPane transScroll = new ScrollPane();
		transScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
		VBox.setVgrow(transScroll, Priority.ALWAYS);
		transScroll.setContent(tbox);
		transBox.getChildren().addAll(transTBPane, transScroll);
		// prepare context menu of terms
		final MenuItem sendToDictMenuItem = new MenuItem("Send to Dictionaries");
		sendToDictMenuItem.setOnAction(actionEvent -> sendToDict());
		final MenuItem cutItiMenuItem = new MenuItem("Cut iti");
		cutItiMenuItem.setOnAction(actionEvent -> cutIti());
		final MenuItem editMenuItem = new MenuItem("Edit this sentence");
		editMenuItem.setOnAction(actionEvent -> openEdit(EditMode.TEXT));
		final MenuItem restoreSenMenuItem = new MenuItem("Restore this sentence");
		restoreSenMenuItem.setOnAction(actionEvent -> restoreSentence());
		termContextMenu.getItems().addAll(sendToDictMenuItem, cutItiMenuItem, editMenuItem,
										new SeparatorMenuItem(), restoreSenMenuItem);

		this.setOnDragOver(new EventHandler<DragEvent>() {
			@Override
			public void handle(final DragEvent event) {
				final Dragboard db = event.getDragboard();
				if (db.hasFiles() || db.hasString())
					event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
				event.consume();
			}
		});
		this.setOnDragDropped(new EventHandler<DragEvent>() {
			@Override
			public void handle(final DragEvent event) {
				final Dragboard db = event.getDragboard();
				if (db.hasFiles()) {
					final List<File> files = db.getFiles();
					if (files.isEmpty()) return;
					final boolean doAdd = isAllSenEdited() ? askBeforeSaveAllEdited() : true;
					if (doAdd) {
						final File seqFile = files.stream().filter(x -> x.getName().endsWith(".seq")).findFirst().orElse(null);
						if (seqFile != null) {
							loadSequence(seqFile);
						} else {
							final int savSentCount = sentenceList.size();
							for (final File f : files) {
								final String fname = f.getName();
								if (fname.endsWith(".json") && !fname.equals(SentenceManager.VARINFO)) {
									addSentence(f);
								}
							}
							if (sentenceList.size() > savSentCount) {
								setupSpinner();
								goToSentence(sentenceList.size());
							}
						}
						closeEditPane();
						closeTransPane();
					}
				} else if (db.hasString()) {
					final String text = db.getString();
					final boolean doAdd = isAllSenEdited() ? askBeforeSaveAllEdited() : true;
					if (doAdd) {
						sequencePath = "";
						addText(text);
						closeEditPane();
						closeTransPane();
					}
				}
				event.consume();
			}
		});

		// some other initialization
		infoPopup.setContentWithText(SentenceUtilities.getTextResource("info-reader.txt"));
		infoPopup.setTextWidth(Utilities.getRelativeSize(34));
		setPrefWidth(Utilities.getRelativeSize(64));
		setPrefHeight(Utilities.getRelativeSize(40));
		init(args);
	}

	public final void init(final Object[] args) {
		initAnalysisOptions();
		sentencePath = sentenceRoot + SentenceUtilities.SENTENCESMAIN;
		sequencePath = "";
		closeEditPane();
		closeTransPane();
		editArea.clear();
		currTextSize = TEXT_BASE_SIZE;
		currTransSize = TRANS_BASE_SIZE;
		saveableThis.set(false);
		saveableAll.set(false);
		isSenEditedMap.clear();
		translationText.setText("");
		variantChoice.getItems().clear();
		showAllTransButton.setSelected(false);
		lightbulb.setColor("grey");
		if (args != null) {
			// 1st item for text to load
			final String text = (String)args[0];
			if (!text.isEmpty())
				addText(text);
			// 2nd item for loading a sentence file, if any
			if (args.length > 1) {
				final File file = (File)args[1];
				final String fname = file.getName();
				if (fname.endsWith(".seq")) {
					// sequence file
					loadSequence(file);
				} else if (fname.endsWith(".json")) {
					// sentence file
					final Sentence sent = new Sentence(file);
					sent.load();
					sentenceList.clear();
					sentenceList.add(sent);
					sentencePath = sent.getSentenceDir();
					setupSpinner();
					goToSentence(sentenceList.size());
				}
			}
		} else {
			sentenceList.clear();
			sentenceSpinner.getEditor().setText("0");
			sentenceSpinner.setValueFactory(null);
			contentBox.getChildren().clear();
		}
		loadVariantList();
		updateFixedInfo();
		Platform.runLater(() -> GrammarUtilities.createDeclIrrNounsMap()); // this needs CPED to be present
	}

	private void initAnalysisOptions() {
		showDetailButton.setSelected(false);
		itiReconstructMenuItem.setSelected(true);
		preItiShortenMenuItem.setSelected(true);
		sandhiCutMenuItem.setSelected(true);
		usePronMenuItem.setSelected(true);
		useNumberMenuItem.setSelected(true);
		useIrrNounMenuItem.setSelected(true);
	}

	private void setAnalysisOptions(final boolean isAll) {
		itiReconstructMenuItem.setSelected(isAll);
		preItiShortenMenuItem.setSelected(isAll);
		sandhiCutMenuItem.setSelected(isAll);
		usePronMenuItem.setSelected(isAll);
		useNumberMenuItem.setSelected(isAll);
		useIrrNounMenuItem.setSelected(isAll);
		updateResult();
	}

	private void loadVariantList() {
		variantMap.clear();
		final File variFile =  new File(sentencePath + SentenceManager.VARINFO);
		if (!variFile.exists())
			SentenceManager.saveVariantInfo(variantMap, variFile, false);
		else
			variantMap.putAll(SentenceManager.loadVariantInfo(variFile));
		updateVariantChoice();
	}

	private void pasteText() {
		final Clipboard cboard = Clipboard.getSystemClipboard();
		final String text = cboard.hasString() ? cboard.getString().trim() : "";
		if (text.isEmpty()) return;
		final boolean doAdd = isAllSenEdited() ? askBeforeSaveAllEdited() : true;
		if (doAdd) {
			sequencePath = "";
			addText(text);
		}
	}

	private boolean askBeforeSaveAllEdited() {
		boolean result = true;
		final ConfirmAlert saveAlert = new ConfirmAlert(theStage, ConfirmAlert.ConfirmType.SAVE);
		final Optional<ButtonType> response = saveAlert.showAndWait();
		if (response.isPresent()) {
			if (response.get() == saveAlert.getConfirmButtonType()) {
				storeAllEdited();
			} else if (response.get() != saveAlert.getDiscardButtonType()) {
				result = false;
			}
		} else {
			result = false;
		}
		return result;
	}
	
	private void addText(final String text) {
		sentenceList.clear();
		isSenEditedMap.clear();
		seqEdited.set(true);
		final String[] tokens = text.split(Utilities.REX_NON_PALI_PUNC);
		// separate sentences by detecting capitalized terms
		final List<Integer> capIndList = new ArrayList<>();
		for (int i = 0; i < tokens.length; i++) {
			final String str = tokens[i].trim();
			if (!str.isEmpty() && Character.isUpperCase(str.charAt(0)))
				capIndList.add(i);
		}
		int savInd = 0;
		for (final Integer ind : capIndList) {
			final List<String> wlist = new ArrayList<>();
			for (int k = savInd; k < ind; k++)
				if (!tokens[k].isEmpty())
					wlist.add(tokens[k]);
			if (!wlist.isEmpty())
				sentenceList.add(createSentence(wlist));
			savInd = ind;
		}
		// the last sentence toward the end
		final List<String> wlistl = new ArrayList<>();
		for (int k = savInd; k < tokens.length; k++)
			if (!tokens[k].isEmpty())
				wlistl.add(tokens[k]);
		if (!wlistl.isEmpty())
			sentenceList.add(createSentence(wlistl));
		setupSpinner();
	}

	private void addSentence(final File sentFile) {
		if (!sentencePath.equals(sentFile.getParent() + File.separator)) return;
		final Sentence sent = new Sentence(sentFile);
		if (sent.isValid()) {
			sentenceList.add(sent);
			seqEdited.set(true);
		}
	}

	private void setupSpinner() {
		if (!sentenceList.isEmpty()) {
			for (int i = 0; i < sentenceList.size(); i++)
				isSenEditedMap.put(i, false);
			final SpinnerValueFactory<Integer> svf = new SpinnerValueFactory.IntegerSpinnerValueFactory(1, sentenceList.size());
			sentenceSpinner.setValueFactory(svf);
			loadSentence(0);
		}
	}

	private Sentence createSentence(final List<String> wordList) {
		final String bareText = wordList.stream().filter(x -> !x.equals(Utilities.DASH))
								.map(x -> x.replace(Utilities.DASH, "").replaceAll("-+", "").replaceAll("\\?+", "").replaceAll("\\!+", ""))
								.collect(Collectors.joining(" "));
		final String editText = wordList.stream().collect(Collectors.joining(" "));
		final String hash = SentenceUtilities.MD5Sum(bareText);
		final Sentence sent = new Sentence(hash, bareText, editText);
		sent.load();
		return sent;
	}

	private void goToSentence(final int num) {
		sentenceSpinner.getEditor().setText("" + num);
		sentenceSpinner.commitValue();
	}

	private void goNext(final boolean withTrans) {
		final int sentCount = sentenceList.size();
		if (sentCount == 0) return;
		final int senInd = sentenceSpinner.getValue() - 1;
		int found = 0;
		for (int i = senInd + 1; i < sentCount; i++) {
			if (withTrans) {
				if (sentenceList.get(i).hasTranslation()) {
					found = i;
					break;
				}
			} else {
				if (sentenceList.get(i).isTranslationEmpty()) {
					found = i;
					break;
				}
			}
		}
		if (found > 0)
			goToSentence(found + 1);
	}

	private void goPrev(final boolean withTrans) {
		if (sentenceList.isEmpty()) return;
		final int senInd = sentenceSpinner.getValue() - 1;
		int found = senInd;
		for (int i = senInd - 1; i >= 0; i--) {
			if (withTrans) {
				if (sentenceList.get(i).hasTranslation()) {
					found = i;
					break;
				}
			} else {
				if (sentenceList.get(i).isTranslationEmpty()) {
					found = i;
					break;
				}
			}
		}
		if (found < senInd)
			goToSentence(found + 1);
	}

	private void spinnerKeyHandler(final InputEvent event) {
		if (event instanceof KeyEvent) {
			final KeyEvent keyEvent = (KeyEvent)event;
			if (keyEvent.getEventType() == KeyEvent.KEY_PRESSED) {
				final int currInd = sentenceSpinner.getValue();
				if (keyEvent.getCode() == KeyCode.UP) {
					if (currInd > 1) {
						sentenceSpinner.getEditor().setText("" + (currInd - 1));
						sentenceSpinner.commitValue();
					}
				} else if (keyEvent.getCode() == KeyCode.DOWN) {
					if (currInd < sentenceList.size()) {
						sentenceSpinner.getEditor().setText("" + (currInd + 1));
						sentenceSpinner.commitValue();
					}
				} else if (keyEvent.getCode() == KeyCode.HOME) {
					goToSentence(1);
				} else if (keyEvent.getCode() == KeyCode.END) {
					goToSentence(sentenceList.size());
				}
			}
		}
	}

	private void updateResult() {
		if (!sentenceList.isEmpty()) {
			loadSentence(sentenceSpinner.getValue() - 1);
		}
	}

	private void updateLightbulb(final Sentence sent) {
		if (sent.hasTranslation())
			lightbulb.setColor("green");
		else
			lightbulb.setColor("grey");
	}

	private void updateFixedInfo() {
		final String[] sentPathTokens = Utilities.safeSplit(sentencePath, File.separator);
		final String sentPath = sentPathTokens[sentPathTokens.length-1];
		final String[] seqPathTokens = Utilities.safeSplit(sequencePath, File.separator);
		final String seqPath = seqPathTokens.length == 0 ? "" : seqPathTokens[seqPathTokens.length-1];
		if (theStage != null)
			theStage.setTitle(TITLE + " [" + sentPath + ":" + seqPath + "]");
		final int totalSens = sentenceList.size();
		final long totalDistSens = sentenceList.stream().map(x -> x.getHash()).distinct().count();
		final long withTransCount = sentenceList.stream().filter(x -> x.hasTranslation()).count();
		final long withTransDistCount = sentenceList.stream().filter(x -> x.hasTranslation()).map(x -> x.getHash()).distinct().count();
		final Set<String> allVars = sentenceList.stream()
											.filter(x -> x.hasTranslation())
											.map(x -> x.getVariantSet())
											.flatMap(Collection::stream)
											.collect(Collectors.toSet());
		final String info = String.format("Sent.: %,4d/%,4d | With trans.: %,4d/%,4d | Variants: %3d", 
											totalDistSens, totalSens, withTransDistCount, withTransCount, allVars.size());
		fixedInfoLabel.setText(info);
	}

	private void loadSentence(final int num) {
		if (num < 0 || num >= sentenceList.size())
			return;
		saveableThis.set(isSenEditedMap.get(num));
		saveableAll.set(isAllSenEdited());
		contentBox.getChildren().clear();
		closeEditPane();
		if (sentenceList.isEmpty())
			return;
		final Sentence thisSent = sentenceList.get(num);
		final List<String> sen = processTerms(thisSent.getEditTokens());
		final TextFlow tfSingle = new TextFlow();
		tfSingle.prefWidthProperty().bind(this.widthProperty().subtract(10));
		final List<TextFlow> tfList = new ArrayList<>();
		for (final String term : sen) {
			if (showDetailButton.isSelected()) {
				// split terms having hyphen
				final String[] subterms = term.split("-");
				for (final String subt : subterms) {
					if (subt.isEmpty()) continue;
					final TextFlow tf = new TextFlow();
					final Text txtTerm = new Text(subt + " ");
					txtTerm.getStyleClass().add("reader-term");
					txtTerm.setStyle("-fx-font-size:" + currTextSize + "%");
					final Text txtInfo = new Text(getInfo(subt.toLowerCase()));
					txtInfo.getStyleClass().add("reader-info");
					tf.getChildren().addAll(txtTerm, txtInfo);
					tfList.add(tf);
				}
			} else {
				final Text txtTerm = new Text(term + " ");
				txtTerm.getStyleClass().add("reader-term");
				txtTerm.setStyle("-fx-font-size:" + currTextSize + "%");
				txtTerm.setOnMouseClicked(mouseEvent -> termClickHandler(mouseEvent));
				tfSingle.getChildren().add(txtTerm);
			}
		}
		if (!showDetailButton.isSelected())
			tfList.add(tfSingle);
		contentBox.getChildren().addAll(tfList);
		if (splitPane.getItems().contains(transBox))
			updateTrans();
		updateLightbulb(thisSent);
		updateFixedInfo();
	}

	private List<String> processTerms(final List<String> input) {
		final LinkedList<String> result = new LinkedList<>();
		final String[] terms = input.stream().collect(Collectors.toList()).toArray(new String[0]);
		boolean skip = false;
		for (int i = 0; i < terms.length; i++) {
			final boolean isCap = Character.isUpperCase(terms[i].charAt(0));
			final String term = terms[i].toLowerCase();
			if (terms[i].equals("pe")) {
				terms[i] = "…pe…";
			}
			if (itiReconstructMenuItem.isSelected()) {
				if (term.equals("ti")) {
					if (preItiShortenMenuItem.isSelected()) {
						if (i > 0) {
							final String previous = terms[i-1];
							final int lastInd = previous.length() - 1;
							final char lastCh = previous.charAt(lastInd);
							terms[i-1] = previous.substring(0, lastInd) + Utilities.shortenVowel(lastCh);
							result.removeLast();
							result.add(terms[i-1]);
						}
					}
					terms[i] = isCap ? "Iti" : "iti";
				} else if (term.equals("nti")) {
					if (i > 0) {
						terms[i-1] = terms[i-1] + "ṃ";
						result.removeLast();
						result.add(terms[i-1]);
					}
					terms[i] = "iti";
				}
			}
			if (sandhiCutMenuItem.isSelected()) {
				final List<String> parts = DictUtilities.cutSandhi(terms[i]);
				result.addAll(parts);
				skip = true;
			}
			if (!skip)
				result.add(terms[i]);
			skip = false;
		}
		return result;
	}

	private String getInfo(final String word) {
		final String result;
		final boolean useDPD = Boolean.parseBoolean(Utilities.settings.getProperty("dpd-lookup-enable"));
		// if DPD dict available, find the word
		if (Utilities.ppdpdAvailMap.get(Utilities.PpdpdTable.DICTIONARY).get() && useDPD) {
			final List<String> meaning = DictUtilities.getMeaningFromDPD(DictUtilities.makeDpdProper(word));
			if (!meaning.isEmpty()) {
				final String meaningStr = meaning.stream().collect(Collectors.joining(" "));
				return "[DPD] " + meaningStr;
			}
		}
		// look into the custom dict first
		if (DictUtilities.customDictMap.containsKey(word)) {
			final DictEntry dic = DictUtilities.customDictMap.get(word);
			final String meaning = dic.getMeaning();
			final String space = meaning.isEmpty() ? "" : " ";
			return meaning + space + "(" + dic.getExplanation() + ")";
		}
		// if use-pronoun-list option is set
		if (usePronMenuItem.isSelected()) {
			if (GrammarUtilities.declPronounsMap.containsKey(word)) {
				final DeclinedWord dword = GrammarUtilities.declPronounsMap.get(word);
				final String meaning = dword.getMeaning();
				final String caseMeaning = dword.getCaseMeaningString();
				final String head = caseMeaning.isEmpty() ? "" : "(" + caseMeaning + ") ";
				return head + meaning + " (" + dword.getCaseString() + ") (" + dword.getNumberString() + ") (" + dword.getGenderString() + ")"; 
			}
		}
		// if use-numeral-list option is set
		if (useNumberMenuItem.isSelected()) {
			if (GrammarUtilities.declNumbersMap.containsKey(word)) {
				final DeclinedWord dword = GrammarUtilities.declNumbersMap.get(word);
				final String meaning = dword.getMeaning();
				final String caseMeaning = dword.getCaseMeaningString();
				final String head = caseMeaning.isEmpty() ? "" : "(" + caseMeaning + ") ";
				return head + meaning + " (" + dword.getCaseString() + ") (" + dword.getNumberString() + ") (" + dword.getGenderString() + ")"; 
			}
		}
		// if use-irregular-noun/adj-list option is set
		if (useIrrNounMenuItem.isSelected()) {
			if (GrammarUtilities.declIrrNounsMap.containsKey(word)) {
				final DeclinedWord dword = GrammarUtilities.declIrrNounsMap.get(word);
				final String meaning = dword.getMeaning();
				final String caseMeaning = dword.getCaseMeaningString();
				final String head = caseMeaning.isEmpty() ? "" : "(" + caseMeaning + ") ";
				return head + meaning + " (" + dword.getCaseString() + ") (" + dword.getNumberString() + ") (" + dword.getGenderString() + ")"; 
			}
		}
		// or else look into CPED dict
		if (!DictUtilities.dictAvailMap.get(DictUtilities.DictBook.CPED).get()) return "";
		String term = word;
		PaliWord pword = new PaliWord(term);
		while (pword.getMeaning().isEmpty() && term.length() > 1) {
			final String tfilter = term;
			Set<String> terms = DictUtilities.cpedTerms.stream().filter(x -> x.startsWith(tfilter)).collect(Collectors.toSet());
			if (!terms.isEmpty()) {
				pword = DictUtilities.getFirstCPEDWord(terms);
			} else {
				// replace the ending first
				final String rterm = DictUtilities.replaceTermEnding(term);
				terms = DictUtilities.cpedTerms.stream().filter(x -> x.startsWith(rterm)).collect(Collectors.toSet());
				if (!terms.isEmpty()) {
					pword = DictUtilities.getFirstCPEDWord(terms);
					term = rterm;
				} else {
					// if failed, cut the ending and go on
					term = term.substring(0, term.length() - 1);
				}
			}
		}
		if (!pword.getMeaning().isEmpty()) {
			final String remark = word.equals(term) ? "[CPED] " : "[CPED*] " + pword.getTerm() + ": ";
			final String[] infos = DictUtilities.formatCPEDMeaning(pword, false).split("\\n");
			final StringBuilder sbuilder = new StringBuilder();
			sbuilder.append(remark);
			for (final String s : infos)
				sbuilder.append(s).append(" ");
			result = sbuilder.toString();
		} else {
			result = "";
		}
		return result;
	}

	private void editDict() {
		final SimpleService editorLauncher = (SimpleService)SentenceUtilities.simpleServiceMap.get("paliplatform.main.EditorLauncher");
		if (editorLauncher != null) {
			final Runnable callback = new Runnable() {
				@Override
				public void run() {
					DictUtilities.updateCustomDict();
				}
			};
			final Object[] args = new Object[] { DictUtilities.customDictFile, callback };
			editorLauncher.processArray(args);
		}
	}

	private void editSandhi() {
		final SimpleService editorLauncher = (SimpleService)SentenceUtilities.simpleServiceMap.get("paliplatform.main.EditorLauncher");
		if (editorLauncher != null) {
			final Runnable callback = new Runnable() {
				@Override
				public void run() {
					DictUtilities.updateSandhiList();
				}
			};
			final Object[] args = new Object[] { DictUtilities.sandhiFile, callback };
			editorLauncher.processArray(args);
		}
	}

	private void restoreSentence() {
		final int senInd = sentenceSpinner.getValue() - 1;
		final Sentence sent = sentenceList.get(senInd);
		sent.restoreEdit();
		isSenEditedMap.put(senInd, true);
		updateResult();
		updateEditTextArea();
	}

	private void cutIti() {
		if (currSelectedText != null) {
			final int senInd = sentenceSpinner.getValue() - 1;
			final Sentence sent = sentenceList.get(senInd);
			final StringBuilder result = new StringBuilder();
			final List<String> tokens = sent.getEditTokens();
			final String term = currSelectedText.getText().trim();
			for (final String t : tokens) {
				if (t.equals(term)) {
					if (t.endsWith("ti")) {
						String newt = t.substring(0, t.lastIndexOf("ti"));
						if (newt.endsWith("n"))
							newt = newt.substring(0, newt.length() - 1) + "ṃ";
						/* delete, not a good idea
						if (preItiShortenMenuItem.isSelected()) {
							final int lastInd = newt.length() - 1;
							final char lastCh = newt.charAt(lastInd);
							newt = newt.substring(0, lastInd) + Utilities.shortenVowel(lastCh);
						}
						*/
						result.append(newt);
						result.append(" iti ");
						isSenEditedMap.put(senInd, true);
					}
				} else {
					result.append(t).append(" ");
				}
			}
			sent.setEditText(result.toString().trim());
			currSelectedText = null;
		}
		updateResult();
		updateEditTextArea();
	}

	private void sendToDict() {
		if (currSelectedText != null) {
			final String term = Utilities.getUsablePaliTerm(currSelectedText.getText());
			final SimpleService dictSearch = (SimpleService)SentenceUtilities.simpleServiceMap.get("paliplatform.main.DictSearch");
			if (dictSearch != null) {
				dictSearch.process(term);
				currSelectedText = null;
			}
		}
	}
	
	private void termClickHandler(final InputEvent event) {
		if (event instanceof MouseEvent) {
			final MouseEvent mouseEvent = (MouseEvent)event;
			currSelectedText = (Text)mouseEvent.getSource();
			termContextMenu.show(contentBox, mouseEvent.getScreenX(), mouseEvent.getScreenY());
		}
	}

	private void openEdit(final EditMode mode) {
		if (sentenceList.isEmpty()) return;
		currEditMode = mode;
		if (currEditMode == EditMode.TEXT) {
			if (!splitPane.getItems().contains(editBox))
				splitPane.getItems().add(editBox);
			updateEditTextArea();
			textInput.resetInputMethod();
		} else {
			if (!splitPane.getItems().contains(editBox))
				splitPane.getItems().add(editBox);
			updateEditTransArea();
			textInput.setInputMethod(PaliTextInput.InputMethod.NORMAL);
		}
		editArea.setStyle("-fx-font-family:'" + Utilities.FONTSANS + ",sans-serif';-fx-font-size:" + currTransSize + "%;");
		updateDividerPosition();
	}

	private void openTrans() {
		if (sentenceList.isEmpty()) return;
		if (variantMap.isEmpty()) {
			final Alert alert = new Alert(AlertType.INFORMATION);
			alert.initOwner(theStage);
			alert.setHeaderText(null);
			alert.setContentText("There is no translation variant in this directory.\n"
								+ "To add a new one, create it in Sentence Manager.\n"
								+ "Then select 'Update variant list' from menu.");
			alert.showAndWait();
			return;
		}
		if (!splitPane.getItems().contains(transBox)) {
			if (splitPane.getItems().contains(editBox)) {
				splitPane.getItems().remove(editBox);
				splitPane.getItems().addAll(transBox, editBox);
			} else {
				splitPane.getItems().add(transBox);
			}
		}
		updateDividerPosition();
		final int senInd = sentenceSpinner.getValue() - 1;
		final Sentence sent = sentenceList.get(senInd);
		currSelectedVariant = sent.getFirstVariant();
		if (currSelectedVariant.isEmpty())
			currSelectedVariant = variantChoice.getItems().get(0);
		variantChoice.getSelectionModel().select(currSelectedVariant);
		updateTrans();
	}

	private void updateVariantChoice() {
		final List<String> varList = variantMap.keySet().stream().sorted(String::compareTo).collect(Collectors.toList());
		final String savVar = currSelectedVariant;
		variantChoice.getItems().setAll(varList);
		currSelectedVariant = savVar;
	}

	private void variantChoiceSelected() {
		if (variantChoice.getItems().isEmpty()) return;
		final String selected = variantChoice.getSelectionModel().getSelectedItem();
		currSelectedVariant = selected == null || selected.isEmpty() ? "" : selected;
		if (!currSelectedVariant.isEmpty())
			updateTrans();
	}

	private void updateTrans() {
		final int senInd = sentenceSpinner.getValue() - 1;
		final Sentence sent = sentenceList.get(senInd);
		final String transText;
		if (showAllTransButton.isSelected()) {
			final String LINSEP = System.getProperty("line.separator");
			if (sent.hasTranslation()) {
				final StringBuilder trans = new StringBuilder();
				final List<String> varList = new ArrayList<>(sent.getVariantSet());
				varList.sort(String::compareTo);
				for (final String v : varList) {
					trans.append("[").append(v).append("]").append(LINSEP);
					trans.append(sent.getTranslation(v)).append(LINSEP).append(LINSEP);
				}
				transText = trans.toString();
			} else {
				transText = "";
			}
		} else {
			transText = sent.hasTranslation()
						? sent.getTranslation(currSelectedVariant) == null
							? ""
							: sent.getTranslation(currSelectedVariant)
						: "";
		}
		translationText.setText(transText);
		translationText.setStyle("-fx-font-family:'" + Utilities.FONTSANS + ",sans-serif';-fx-font-size:" + currTransSize + "%;");
		saveableThis.set(isSenEditedMap.get(senInd));
		saveableAll.set(isAllSenEdited());
		closeEditPane();
	}

	private void deleteTrans() {
		if (sentenceList.isEmpty()) return;
		final int senInd = sentenceSpinner.getValue() - 1;
		final Sentence sent = sentenceList.get(senInd);
		sent.removeTranslation(currSelectedVariant);
		updateLightbulb(sent);
		isSenEditedMap.put(senInd, true);
		saveableThis.set(true);
		saveableAll.set(isAllSenEdited());
		updateTrans();
	}

	private void closeEditPane() {
		if (splitPane.getItems().contains(editBox))
			splitPane.getItems().remove(editBox);
	}

	private void closeTransPane() {
		if (splitPane.getItems().contains(transBox))
			splitPane.getItems().remove(transBox);
	}

	private void updateDividerPosition() {
		final int paneCount = splitPane.getItems().size();
		if (paneCount == 2)
			splitPane.setDividerPositions(0.6);
		else if (paneCount == 3)
			splitPane.setDividerPositions(0.4, 0.6);
	}

	private void updateEditTextArea() {
		if (splitPane.getItems().size() == 1 || sentenceList.isEmpty())
			return;
		final int senInd = sentenceSpinner.getValue() - 1;
		final Sentence sent = sentenceList.get(senInd);
		editArea.setText(sent.getEditText().replace(Utilities.DASH, "--"));
	}

	private void updateEditTransArea() {
		if (splitPane.getItems().size() == 1 || sentenceList.isEmpty())
			return;
		final int senInd = sentenceSpinner.getValue() - 1;
		final Sentence sent = sentenceList.get(senInd);
		final String selVar = variantChoice.getSelectionModel().getSelectedItem();
		final String transText = sent.hasTranslation()
									? sent.getTranslation(selVar) == null ? "" : sent.getTranslation(selVar)
									: "";
		editArea.setText(transText);
	}

	private void submitEdit() {
		if (sentenceSpinner.getValue() == null || sentenceSpinner.getValue() == 0)
			return;
		final int senInd = sentenceSpinner.getValue() - 1;
		final Sentence sent = sentenceList.get(senInd);
		isSenEditedMap.put(senInd, true);
		if (currEditMode == EditMode.TEXT)
			sent.setEditText(editArea.getText());
		else
			sent.addTranslation(currSelectedVariant, editArea.getText());
		updateIdenticalSentence(sent);
		updateResult();
	}

	private void updateIdenticalSentence(final Sentence sent) {
		sentenceList.forEach(x -> {
			if (x.equals(sent)) {
				x.setEditText(sent.getEditText());
				x.addAllTranslations(sent.getTranslationMap());
			}
		});
	}

	private String getSentenceSequence() {
		if (sentenceList.isEmpty()) return "";
		final StringBuilder result = new StringBuilder();
		for (final Sentence sent : sentenceList) {
			result.append(sent.getHash()).append(".json");
			result.append(System.getProperty("line.separator"));
		}
		return result.toString();
	}

	private void openSequence() {
		final boolean doOpen = isAllSenEdited() || seqEdited.get() ? askBeforeSaveAllEdited() : true;
		if (doOpen) {
			final File seqFile = Utilities.selectFile("seq", sentencePath, theStage);
			if (seqFile == null) return;
			loadSequence(seqFile);
			seqEdited.set(false);
			closeTransPane();
		}
	}

	private void loadSequence(final File seqFile) {
		sequencePath = seqFile.getPath();
		sentencePath = seqFile.getParent() + File.separator;
		sentenceList.clear();
		final String seq = Utilities.getTextFileContent(seqFile);
		seq.lines().forEach(x -> {
			final String hash = x.split("\\.")[0];
			final Sentence sent = new Sentence(hash);
			if (sent.setSentenceDirAndLoad(sentencePath))
				sentenceList.add(sent);
		});
		loadVariantList();
		setupSpinner();
	}

	private void saveSequence() {
		if (sequencePath.isEmpty())
			sequencePath = getOutputSequencePath();
		if (!sequencePath.isEmpty()) {
			storeEverySentence();
			Utilities.saveText(getSentenceSequence(), new File(sequencePath));
			updateFixedInfo();
			seqEdited.set(false);
		}
	}

	private void saveSequenceAs() {
		final String seqPath = getOutputSequencePath();
		if (!seqPath.isEmpty()) {
			final String sentPath = seqPath.substring(0, seqPath.lastIndexOf(File.separator) + 1);
			if (storeEverySentenceAs(sentPath)) {
				sentencePath = sentPath;
				sequencePath = seqPath;
				// save only variants that have translation
				final Set<String> allVars = sentenceList.stream()
														.filter(x -> x.hasTranslation())
														.map(x -> x.getVariantSet())
														.flatMap(Collection::stream)
														.collect(Collectors.toSet());
				final Map<String, Variant> varMap = variantMap.keySet().stream()
														.filter(x -> allVars.contains(x))
														.collect(Collectors.toMap(Function.identity(), x -> variantMap.get(x)));
				SentenceManager.saveVariantInfo(varMap, new File(sentencePath + SentenceManager.VARINFO), true);
				Utilities.saveText(getSentenceSequence(), new File(sequencePath));
				updateFixedInfo();
				seqEdited.set(false);
			}
		}
	}

	private String getOutputSequencePath() {
		final File seqFile = Utilities.getOutputFile("mytext.seq", sentencePath, theStage);
		final String result;
		if (seqFile == null) {
			result = "";
		} else {
			final String spath = seqFile.getPath();
			result = spath.endsWith(".seq") ? spath : spath + ".seq";
		}
		return result;
	}

	private void storeSentence() {
		final Integer num = sentenceSpinner.getValue();
		if (num == null || num == 0) return;
		storeSentence(num - 1, true);
		updateResult();
	}

	private void storeSentence(final int senInd, final boolean forceOverWrite) {
		final Sentence sent = sentenceList.get(senInd);
		sent.save(forceOverWrite);
		isSenEditedMap.put(senInd, false);
	}

	private void storeSentenceAs(final int senInd, final boolean forceOverWrite, final String path) {
		final Sentence sent = sentenceList.get(senInd);
		sent.saveAs(path, forceOverWrite);
		isSenEditedMap.put(senInd, false);
	}

	private void storeEverySentence() {
		if (sentenceList.isEmpty()) return;
		for (int i = 0; i < sentenceList.size(); i++)
			storeSentence(i, false);
	}

	private boolean storeEverySentenceAs(final String path) {
		if (sentenceList.isEmpty()) return false;
		boolean existed = false;
		boolean success = true;
		for (final Sentence s : sentenceList) {
			final File f = new File(path + s.getHash() + ".json");
			if (f.exists()) {
				existed = true;
				break;
			}
		}
		if (existed) {
			final ConfirmAlert replaceAlert = new ConfirmAlert(theStage, ConfirmAlert.ConfirmType.REPLACE);
			final Optional<ButtonType> response = replaceAlert.showAndWait();
			if (response.isPresent()) {
				if (response.get() == replaceAlert.getConfirmButtonType())
					storeEverySentenceAs(path, true);
				else if (response.get() == replaceAlert.getKeepButtonType())
					storeEverySentenceAs(path, false);
				else
					success = false;
			}
		} else {
			storeEverySentenceAs(path, false);
		}
		return success;
	}

	private void storeEverySentenceAs(final String path, final boolean forceOverWrite) {
		for (int i = 0; i < sentenceList.size(); i++)
			storeSentenceAs(i, forceOverWrite, path);
	}

	private boolean storeAllEdited() {
		final boolean success;
		for (int i = 0; i < sentenceList.size(); i++) {
			if (isSenEditedMap.get(i))
				storeSentence(i, true);
		}
		if (seqEdited.get())
			saveSequence();
		success = true;
		return success;
	}

	private void removeSentence() {
		if (sentenceList.isEmpty()) return;
		final int senInd = sentenceSpinner.getValue() - 1;
		sentenceList.remove(senInd);
		if (sentenceList.isEmpty()) {
			sequencePath = "";
			sentenceSpinner.setValueFactory(null);
			contentBox.getChildren().clear();
			updateFixedInfo();
			seqEdited.set(false);
		} else {
			final int sz = sentenceList.size();
			final int nextSen = senInd < sz ? senInd + 1 : sz;
			setupSpinner();
			goToSentence(nextSen);
			seqEdited.set(true);
		}
		closeTransPane();
		closeEditPane();
	}

	private boolean isAllSenEdited() {
		return isSenEditedMap.values().stream().reduce(false, (x, y) -> x || y);
	}

	private void zoom(final int factor) {
		toolBar.changeFontSize(factor);
		if (factor == 0) {
			currTextSize = TEXT_BASE_SIZE;
			currTransSize = TRANS_BASE_SIZE;
		} else {
			currTextSize += factor;
			currTransSize += factor;
		}
		if (currTextSize < 100)
			currTextSize = 100;
		if (currTransSize < 50)
			currTransSize = 50;
		updateResult();
	}

	public void setStage(final Stage stage) {
		theStage = stage;
		theStage.setOnCloseRequest(new EventHandler<WindowEvent>() {  
			@Override
			public void handle(final WindowEvent event) {
				closeWindow(event);
			}
		});
	}

	private void closeWindow(final WindowEvent event) {
		final boolean needSave = isAllSenEdited();
		if (needSave) {
			final ConfirmAlert saveAlert = new ConfirmAlert(theStage, ConfirmAlert.ConfirmType.SAVE);
			final Optional<ButtonType> result = saveAlert.showAndWait();
			if (result.isPresent()) {
				if (result.get() == saveAlert.getConfirmButtonType()) {
					if (storeAllEdited())
						theStage.hide();
					else
						event.consume();
				} else {
					if (result.get() != saveAlert.getDiscardButtonType())
						if (event != null) event.consume();
					else
						theStage.hide();
				}
			}
		} else {
			theStage.hide();
		}
	}

	private String makeText() {
		final StringBuilder result = new StringBuilder();
		final String LINSEP = System.getProperty("line.separator");
		for (final Node tfNode : contentBox.getChildren()) {
			final TextFlow tf = (TextFlow)tfNode;
			for (final Node tNode : tf.getChildren()) {
				final String t = ((Text)tNode).getText();
				result.append(t);
			}
			result.append(LINSEP);
		}
		if (splitPane.getItems().contains(transBox)) {
			result.append(LINSEP);
			result.append("Translation(s):");
			result.append(LINSEP);
			if (!showAllTransButton.isSelected()) {
				result.append("[").append(variantChoice.getSelectionModel().getSelectedItem()).append("]");
				result.append(LINSEP);
			}
			result.append(translationText.getText());
		}
		return result.toString();
	}
	
	private void copyText() {
		Utilities.copyText(makeText());
	}
	
	private void saveText() {
		Utilities.saveText(makeText(), "readerout.txt");
	}

}
