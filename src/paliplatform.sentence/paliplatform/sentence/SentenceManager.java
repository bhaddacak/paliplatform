/*
 * SentenceManager.java
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

package paliplatform.sentence;

import paliplatform.base.*;

import java.util.*;
import java.util.zip.*;
import java.util.stream.*;
import java.util.function.*;
import java.io.*;
import java.nio.file.*;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.text.Normalizer.Form;

import javafx.application.Platform;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.*;
import javafx.scene.layout.*;
import javafx.scene.input.*;
import javafx.geometry.*;
import javafx.util.Callback;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import com.google.gson.stream.*;

/** 
 * The window managing sentences used by SentenceReader.
 * This is a singleton.
 * @author J.R. Bhaddacak
 * @version 4.1
 * @since 2.0
 */
public class SentenceManager extends SingletonWindow {
	public static final SentenceManager INSTANCE = new SentenceManager();
	public static final String ALL_SENT = ":ALL:";
	public static final String ONLY_TRANS = ":ONLY:";
	public static final String NO_TRANS = ":NONE:";
	public static final String NO_SEQ = ":NONE:";
	public static final String VARINFO = "varinfo.json";
	public static final String SEQ_NAME = "Sequence Name";
	private final String sentenceRoot = Utilities.ROOTDIR + SentenceUtilities.SENTENCESPATH;
	private String sentencePath = sentenceRoot + SentenceUtilities.SENTENCESMAIN;
	private final BorderPane mainPane = new BorderPane();
	private final TabPane tabPane = new TabPane();
	private final Tab sentenceTab = new Tab("Sentences");
	private final Tab variantTab = new Tab("Translation Variants");
	private final Tab mergerTab = new Tab("Merger");
	private final ObservableList<SentenceOutput> sentOutputList = FXCollections.<SentenceOutput>observableArrayList();
	private final TableView<SentenceOutput> sentTable = new TableView<>();	
	private final ObservableList<VariantOutput> variOutputList = FXCollections.<VariantOutput>observableArrayList();
	private final Set<String> hiddenVariSet = new HashSet<>();
	private final TableView<VariantOutput> variTable = new TableView<>();	
	private final Map<String, Sentence> allSentMap = new HashMap<>();
	private final Map<String, Variant> variantMap = new HashMap<>();
	private final Map<String, List<String>> sequenceMap = new HashMap<>();
	private final List<String> sequenceList = new ArrayList<>();
	private final PaliTextInput sentFilterInput = new PaliTextInput(PaliTextInput.InputType.FIELD);
	private final ChoiceBox<String> variantChoice = new ChoiceBox<>();
	private final RadioMenuItem findInTextMenuItem = new RadioMenuItem("Find in text");
	private final RadioMenuItem findInEditMenuItem = new RadioMenuItem("Find in edit");
	private final RadioMenuItem findInTranMenuItem = new RadioMenuItem("Find in translations");
	private final ToggleGroup sentFilterGroup = new ToggleGroup();
	private final MenuButton seqFilterMenuButton = new MenuButton("", new TextIcon("list-ol", TextIcon.IconSet.AWESOME));		
	private final RadioMenuItem seqNoneMenuItem = new RadioMenuItem(NO_SEQ);
	private final ToggleGroup seqFilterGroup = new ToggleGroup();
	private final List<String> currSeqFilterList = new ArrayList<>();
	private final Label sentHashLabel = new Label();
	private final Label sentTextLabel = new Label();
	private final PaliTextInput sentEditInput = new PaliTextInput(PaliTextInput.InputType.AREA);
	private final TextArea sentEditArea = (TextArea)sentEditInput.getInput();
	private final BorderPane translationPane = new BorderPane();
	private final VBox sentTransBox = new VBox(3);
	private final VBox seqInfoBox = new VBox(3);
	private final Label variNameLabel = new Label();
	private final PaliTextInput variAuthorInput = new PaliTextInput(PaliTextInput.InputType.FIELD);
	private final TextField variAuthorTextField = (TextField)variAuthorInput.getInput();
	private final PaliTextInput variNoteInput = new PaliTextInput(PaliTextInput.InputType.AREA);
	private final TextArea variNoteArea = (TextArea)variNoteInput.getInput();
	private final MergerPane leftMergerPane;
	private final MergerPane rightMergerPane;
	private final ToggleButton showAllButton = new ToggleButton("", new TextIcon("asterisk", TextIcon.IconSet.AWESOME));
	private final ToggleButton showEqualButton = new ToggleButton("", new TextIcon("equals", TextIcon.IconSet.AWESOME));
	private final ToggleButton showUnequalButton = new ToggleButton("", new TextIcon("not-equal", TextIcon.IconSet.AWESOME));
	private final ToggleGroup showOptionGroup = new ToggleGroup();
	private final CheckBox cbMergeVarInfo = new CheckBox("Merge variant info");
	private final Label leftFixedInfoLabel = new Label();
	private final Label rightFixedInfoLabel = new Label();
	private final TextField sentFilterTextField;
	private int currSelectedSentenceIndex = 0;
	private String currSelectedVariant = ALL_SENT;
	private String prevSelectedVariant = "";
	
	private SentenceManager() {
		windowWidth = Utilities.getRelativeSize(62);
		windowHeight = Utilities.getRelativeSize(54);
		setTitle("Sentence Manager");
		getIcons().add(new Image(SentenceManager.class.getResourceAsStream("resources/images/briefcase.png")));

		// add main area at the center
		tabPane.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> updateFixedInfo());
		// (1) sentence tab
		sentenceTab.setClosable(false);
		final SplitPane sentSplitPane = new SplitPane();
		sentSplitPane.setOrientation(Orientation.VERTICAL);
		sentSplitPane.setDividerPositions(0.5);
		// sentence list above
		final VBox sentListBox = new VBox();
		final ToolBar sentToolBar = new ToolBar();
		sentFilterTextField = (TextField) sentFilterInput.getInput();
		sentFilterTextField.setPrefWidth(Utilities.getRelativeSize(20));
		sentFilterTextField.textProperty().addListener((obs, oldValue, newValue) -> {
			final String strQuery = Normalizer.normalize(newValue.trim(), Form.NFC);
			updateSentList(strQuery);
		});
		variantChoice.setTooltip(new Tooltip("Translation/variant inclusion"));
		variantChoice.setOnAction(actionEvent -> variantChoiceSelected());		
		final MenuButton sentOptionsMenu = new MenuButton("", new TextIcon("check-double", TextIcon.IconSet.AWESOME));		
		sentOptionsMenu.setTooltip(new Tooltip("Filter options"));
		sentOptionsMenu.getItems().addAll(findInTextMenuItem, findInEditMenuItem, findInTranMenuItem);
		sentFilterGroup.getToggles().addAll(findInTextMenuItem, findInEditMenuItem, findInTranMenuItem);
		sentFilterGroup.selectToggle(findInTextMenuItem);
        sentFilterGroup.selectedToggleProperty().addListener(observable -> {
			final Toggle toggle = sentFilterGroup.getSelectedToggle();
			if (toggle == findInTranMenuItem)
				sentFilterInput.setInputMethod(PaliTextInput.InputMethod.NORMAL);
			else
				sentFilterInput.resetInputMethod();
			updateSentList();
		});
		seqFilterMenuButton.setTooltip(new Tooltip("Ordered/filtered by a sequence"));
        seqFilterGroup.selectedToggleProperty().addListener(observable -> {
			final RadioMenuItem selected = (RadioMenuItem)seqFilterGroup.getSelectedToggle();
			if (selected != null) {
				updateSeqFilter(selected.getText());
				updateSentList();
			}
		});
		final Button resortButton = new Button("", new TextIcon("repeat", TextIcon.IconSet.AWESOME));
		resortButton.setTooltip(new Tooltip("Reset sorting order"));
		resortButton.setOnAction(actionEvent -> updateSentList());		
		final Button saveSeqAsButton = new Button("", new TextIcon("download", TextIcon.IconSet.AWESOME));
		saveSeqAsButton.setTooltip(new Tooltip("Save sequence as..."));
		saveSeqAsButton.disableProperty().bind(seqNoneMenuItem.selectedProperty());
		saveSeqAsButton.setOnAction(actionEvent -> saveSequenceAs());		
		final Button openReaderSeqButton = new Button("", new TextIcon("book-open-reader", TextIcon.IconSet.AWESOME));
		openReaderSeqButton.setTooltip(new Tooltip("Open this sequence in a new Reader"));
		openReaderSeqButton.disableProperty().bind(seqNoneMenuItem.selectedProperty());
		openReaderSeqButton.setOnAction(actionEvent -> openReader());		
		final Button openReaderOneButton = new Button("", new TextIcon("book-open", TextIcon.IconSet.AWESOME));
		openReaderOneButton.setTooltip(new Tooltip("Open this sentence in a new Reader"));
		openReaderOneButton.setOnAction(actionEvent -> openReader(sentTable));		
		final Button delSentButton = new Button("", new TextIcon("trash", TextIcon.IconSet.AWESOME));
		delSentButton.setTooltip(new Tooltip("Delete this sentence"));
		delSentButton.setOnAction(actionEvent -> deleteSentence());		
		sentToolBar.getItems().addAll(sentFilterTextField, sentFilterInput.getClearButton(), sentFilterInput.getMethodButton(),
										sentOptionsMenu, variantChoice, seqFilterMenuButton, resortButton,
										saveSeqAsButton, openReaderSeqButton, openReaderOneButton, delSentButton);
		VBox.setVgrow(sentTable, Priority.ALWAYS);
		sentTable.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> {
			if (newValue == null) {
				clearSentDetail();
			} else {
				final Sentence sent = newValue.getSentence();
				currSelectedSentenceIndex = sentTable.getSelectionModel().getSelectedIndex();
				if (sent == null)
					clearSentDetail();
				else
					updateSentDetail(sent);
			}
			translationPane.setTop(null);
		});
		sentTable.setOnDragDetected(mouseEvent -> {
			final Dragboard db = sentTable.startDragAndDrop(TransferMode.ANY);
			final ClipboardContent content = new ClipboardContent();
			final SentenceOutput sp = sentTable.getSelectionModel().getSelectedItem();
			if (sp == null) return;
			final Sentence sent = sp.getSentence();
			if (sent == null) return;
			final File sentFile = sent.getFile();
			content.putFiles(Arrays.asList(sentFile));
			db.setContent(content);
			mouseEvent.consume();
		});
		sentTable.setOnMouseDragged(mouseEvent -> mouseEvent.setDragDetect(true));
		sentListBox.getChildren().addAll(sentToolBar, sentTable);
		// detail box below, use tab pane in border pane
		final BorderPane sentDetailPane = new BorderPane();
		final TabPane sentDetailTabPane = new TabPane();
		final Tab textEditTab = new Tab("Text/Edit");
		final Tab transTab = new Tab("Translations");
		final Tab seqInfoTab = new Tab("In sequences");
		// set toolbar on the top
		final ToolBar sentDetailToolBar = new ToolBar();
		final MenuButton sentDetailMenu = new MenuButton("", new TextIcon("bars", TextIcon.IconSet.AWESOME));		
		final MenuItem sentAddTransMenuItem = new MenuItem("Add translation", new TextIcon("square-plus", TextIcon.IconSet.AWESOME));
		sentAddTransMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.N, KeyCombination.SHORTCUT_DOWN, KeyCombination.SHIFT_DOWN));
		sentAddTransMenuItem.disableProperty().bind(sentDetailTabPane.getSelectionModel().selectedItemProperty().isNotEqualTo(transTab));
		sentAddTransMenuItem.setOnAction(actionEvent -> addTranslation(true));		
		final MenuItem sentAddRecentTransMenuItem = new MenuItem("Add recent variant", new TextIcon("plus", TextIcon.IconSet.AWESOME));
		sentAddRecentTransMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.N, KeyCombination.SHORTCUT_DOWN));
		sentAddRecentTransMenuItem.disableProperty().bind(sentDetailTabPane.getSelectionModel().selectedItemProperty().isNotEqualTo(transTab));
		sentAddRecentTransMenuItem.setOnAction(actionEvent -> addTranslation(false));		
		final MenuItem sentSaveMenuItem = new MenuItem("Save", new TextIcon("download", TextIcon.IconSet.AWESOME));
		sentSaveMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.S, KeyCombination.SHORTCUT_DOWN));
		sentSaveMenuItem.setOnAction(actionEvent -> saveSentence());		
		final MenuItem restoreEditMenuItem = new MenuItem("Restore edit");
		restoreEditMenuItem.disableProperty().bind(sentDetailTabPane.getSelectionModel().selectedItemProperty().isNotEqualTo(textEditTab));
		restoreEditMenuItem.setOnAction(actionEvent -> restoreEdit());		
		sentDetailMenu.getItems().addAll(sentAddTransMenuItem, sentAddRecentTransMenuItem, 
										new SeparatorMenuItem(), sentSaveMenuItem, 
										new SeparatorMenuItem(), restoreEditMenuItem);
		final Button sentSaveButton = new Button("", new TextIcon("download", TextIcon.IconSet.AWESOME));
		sentSaveButton.setTooltip(new Tooltip("Save"));
		sentSaveButton.setOnAction(actionEvent -> saveSentence());		
		final Button sentAddTransButton = new Button("", new TextIcon("square-plus", TextIcon.IconSet.AWESOME));
		sentAddTransButton.setTooltip(new Tooltip("Add new translation"));
		sentAddTransButton.disableProperty().bind(sentDetailTabPane.getSelectionModel().selectedItemProperty().isNotEqualTo(transTab));
		sentAddTransButton.setOnAction(actionEvent -> addTranslation(true));		
		final Button sentAddRecentTransButton = new Button("", new TextIcon("plus", TextIcon.IconSet.AWESOME));
		sentAddRecentTransButton.setTooltip(new Tooltip("Add new translation (recent variant)"));
		sentAddRecentTransButton.disableProperty().bind(sentDetailTabPane.getSelectionModel().selectedItemProperty().isNotEqualTo(transTab));
		sentAddRecentTransButton.setOnAction(actionEvent -> addTranslation(false));		
		final Button showTextForTransButton = new Button("", new TextIcon("scroll", TextIcon.IconSet.AWESOME));
		showTextForTransButton.disableProperty().bind(sentDetailTabPane.getSelectionModel().selectedItemProperty().isNotEqualTo(transTab));
		showTextForTransButton.setTooltip(new Tooltip("Show text on/off"));
		showTextForTransButton.setOnAction(actionEvent -> showTextForTrans());		
		sentDetailToolBar.getItems().addAll(sentDetailMenu, sentSaveButton, 
											sentAddTransButton, sentAddRecentTransButton, showTextForTransButton);
		sentDetailPane.setTop(sentDetailToolBar);
		// set tabpane at the center
		// (1.1) text and edit
		textEditTab.setClosable(false);
		// prepare edit area box
		final VBox textEditBox = new VBox(3);
		textEditBox.setPadding(new Insets(3));
		final HBox editAreaToolBar = new HBox();
		editAreaToolBar.getChildren().addAll(sentEditInput.getMethodButton());
		VBox.setVgrow(sentEditArea, Priority.ALWAYS);
		sentTextLabel.setWrapText(true);
		sentEditArea.setWrapText(true);
		sentEditArea.setPrefRowCount(3);
		textEditBox.getChildren().addAll(sentHashLabel, sentTextLabel, editAreaToolBar, sentEditArea);
		textEditTab.setContent(textEditBox);
		// (1.2) translation tab
		transTab.setClosable(false);
		final ScrollPane transBoxScrollPane = new ScrollPane();
		sentTransBox.setPadding(new Insets(3));
		sentTransBox.prefWidthProperty().bind(mainPane.widthProperty().subtract(20));
		transBoxScrollPane.setContent(sentTransBox);
		translationPane.setCenter(transBoxScrollPane);
		transTab.setContent(translationPane);
		// (1.3) sequence info (sentence stat) tab
		seqInfoTab.setClosable(false);
		seqInfoBox.setPadding(new Insets(10));
		final ScrollPane seqInfoScrollPane = new ScrollPane();
		seqInfoScrollPane.setContent(seqInfoBox);
		seqInfoTab.setContent(seqInfoScrollPane);
		// compose all tabs
		sentDetailTabPane.getTabs().addAll(textEditTab, transTab, seqInfoTab);
		sentDetailPane.setCenter(sentDetailTabPane);
		sentSplitPane.getItems().addAll(sentListBox, sentDetailPane);
		sentenceTab.setContent(sentSplitPane);
		// (2) variant tab
		variantTab.setClosable(false);
		final SplitPane variSplitPane = new SplitPane();
		variSplitPane.setOrientation(Orientation.VERTICAL);
		variSplitPane.setDividerPositions(0.65);
		// variant list above
		final VBox variListBox = new VBox();
		final ToolBar variToolBar = new ToolBar();
		final Button variHideButton = new Button("", new TextIcon("eye-slash", TextIcon.IconSet.AWESOME));
		variHideButton.setTooltip(new Tooltip("Visibility on/off"));
		variHideButton.setOnAction(actionEvent -> toggleHideVariant());		
		final Button variAddButton = new Button("", new TextIcon("plus", TextIcon.IconSet.AWESOME));
		variAddButton.setTooltip(new Tooltip("Add new variant"));
		variAddButton.setOnAction(actionEvent -> addVariant());		
		final Button variRenameButton = new Button("", new TextIcon("tag", TextIcon.IconSet.AWESOME));
		variRenameButton.setTooltip(new Tooltip("Rename variant"));
		variRenameButton.setOnAction(actionEvent -> renameVariant());		
		final Button variDeleteButton = new Button("", new TextIcon("trash", TextIcon.IconSet.AWESOME));
		variDeleteButton.setTooltip(new Tooltip("Delete variant"));
		variDeleteButton.setOnAction(actionEvent -> deleteVariant());		
		variToolBar.getItems().addAll(variHideButton, variAddButton, variRenameButton, variDeleteButton);
		variTable.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> {
			if (newValue == null) {
				clearVariDetail();
			} else {
				final Variant vari = newValue.getVariant();
				if (vari == null)
					clearVariDetail();
				else
					updateVariDetail(vari);
			}
		});
		VBox.setVgrow(variTable, Priority.ALWAYS);
		variListBox.getChildren().addAll(variToolBar, variTable);
		// detail box below
		final VBox variDetailBox = new VBox();
		final ToolBar variDetailToolBar = new ToolBar();
		final ExpandButton expandVariNoteButton = new ExpandButton(variNoteArea);
		final Button variSaveButton = new Button("", new TextIcon("download", TextIcon.IconSet.AWESOME));
		variSaveButton.setTooltip(new Tooltip("Save"));
		variSaveButton.setOnAction(actionEvent -> saveVariant());		
		variDetailToolBar.getItems().addAll(expandVariNoteButton, variSaveButton);
		final GridPane variDetailGrid = new GridPane();
		final ColumnConstraints vcol1 = new ColumnConstraints();
		vcol1.setPercentWidth(10);
		final ColumnConstraints vcol2 = new ColumnConstraints();
		vcol2.setPercentWidth(90);
		variDetailGrid.getColumnConstraints().addAll(vcol1, vcol2);
		variDetailGrid.prefWidthProperty().bind(mainPane.widthProperty().subtract(15));
		variDetailGrid.setHgap(5);
		variDetailGrid.setVgap(3);
		final Label lbVariant = new Label("Variant:");
		final Label lbAuthor = new Label("Author:");
		final Label lbNote = new Label("Notes:");
		variNoteArea.setWrapText(true);
		GridPane.setConstraints(lbVariant, 0, 0, 1, 1, HPos.RIGHT, VPos.TOP);
		GridPane.setConstraints(lbAuthor, 0, 1, 1, 1, HPos.RIGHT, VPos.TOP);
		GridPane.setConstraints(lbNote, 0, 2, 1, 1, HPos.RIGHT, VPos.TOP);
		GridPane.setConstraints(variNameLabel, 1, 0, 1, 1, HPos.LEFT, VPos.TOP);
		final HBox variAuthorBox = new HBox(3);
		variAuthorInput.setInputMethod(PaliTextInput.InputMethod.NORMAL);
		variAuthorTextField.setPrefWidth(Utilities.getRelativeSize(60));
		variAuthorBox.getChildren().addAll(variAuthorTextField, variAuthorInput.getMethodButton());
		GridPane.setConstraints(variAuthorBox, 1, 1, 1, 1, HPos.LEFT, VPos.TOP);
		final HBox variNoteBox = new HBox(3);
		variNoteInput.setInputMethod(PaliTextInput.InputMethod.NORMAL);
		variNoteArea.setPrefWidth(Utilities.getRelativeSize(60));
		variNoteBox.getChildren().addAll(variNoteArea, variNoteInput.getMethodButton());
		GridPane.setConstraints(variNoteBox, 1, 2, 1, 1, HPos.LEFT, VPos.TOP);
		variDetailGrid.getChildren().addAll(lbVariant, lbAuthor, lbNote, variNameLabel, variAuthorBox, variNoteBox);
		VBox.setVgrow(variDetailGrid, Priority.ALWAYS);
		variDetailBox.getChildren().addAll(variDetailToolBar, variDetailGrid);
		variSplitPane.getItems().addAll(variListBox, variDetailBox);
		variantTab.setContent(variSplitPane);
		// (3) merger tab
		mergerTab.setClosable(false);
		final BorderPane mergerPane = new BorderPane();
		final ToolBar mergerToolBar = new ToolBar();
		final Button mergeButton = new Button("Merge", new TextIcon("code-merge", TextIcon.IconSet.AWESOME));
		mergeButton.setOnAction(actionEvent -> merge());		
		showAllButton.setSelected(true);
		showAllButton.setTooltip(new Tooltip("Show all"));
		showAllButton.setOnAction(actionEvent -> updateMergerTables());		
		showEqualButton.setTooltip(new Tooltip("Show equals"));
		showEqualButton.setOnAction(actionEvent -> updateMergerTables());		
		showUnequalButton.setTooltip(new Tooltip("Show unequals"));
		showUnequalButton.setOnAction(actionEvent -> updateMergerTables());		
		showOptionGroup.getToggles().addAll(showAllButton, showEqualButton, showUnequalButton);
		cbMergeVarInfo.setSelected(true);
		mergerToolBar.getItems().addAll(mergeButton, showAllButton, showEqualButton, showUnequalButton, cbMergeVarInfo);
		mergerPane.setTop(mergerToolBar);
		final SplitPane mergerSplitPane = new SplitPane();
		mergerSplitPane.setOrientation(Orientation.HORIZONTAL);
		mergerSplitPane.setDividerPositions(0.5);
		leftMergerPane = new MergerPane();
		rightMergerPane = new MergerPane();
		mergeButton.disableProperty().bind(leftMergerPane.listAvailableProperty().and(rightMergerPane.listAvailableProperty()).not());
		showAllButton.disableProperty().bind(mergeButton.disableProperty());
		showEqualButton.disableProperty().bind(mergeButton.disableProperty());
		showUnequalButton.disableProperty().bind(mergeButton.disableProperty());
		leftMergerPane.keepEditButton.setSelected(true);
		leftMergerPane.keepTransButton.setSelected(true);
		leftMergerPane.keepInfoButton.setSelected(true);
		leftMergerPane.keepSeqButton.setSelected(true);
		final ToggleGroup keepEditGroup = new ToggleGroup();
		keepEditGroup.getToggles().addAll(leftMergerPane.keepEditButton, rightMergerPane.keepEditButton);
		final ToggleGroup keepTransGroup = new ToggleGroup();
		keepTransGroup.getToggles().addAll(leftMergerPane.keepTransButton, rightMergerPane.keepTransButton);
		final ToggleGroup keepVarInfoGroup = new ToggleGroup();
		keepVarInfoGroup.getToggles().addAll(leftMergerPane.keepInfoButton, rightMergerPane.keepInfoButton);
		final ToggleGroup keepSeqFilesGroup = new ToggleGroup();
		keepSeqFilesGroup.getToggles().addAll(leftMergerPane.keepSeqButton, rightMergerPane.keepSeqButton);
		mergerSplitPane.getItems().addAll(leftMergerPane, rightMergerPane);
		mergerPane.setCenter(mergerSplitPane);
		mergerTab.setContent(mergerPane);

		tabPane.getTabs().addAll(sentenceTab, variantTab, mergerTab);
		mainPane.setCenter(tabPane);
		
		// add toolbar on the top
		final CommonWorkingToolBar toolBar = new CommonWorkingToolBar(tabPane);
		// config some buttons
		toolBar.saveTextButton.setOnAction(actionEvent -> saveText());		
		toolBar.copyButton.setOnAction(actionEvent -> copyText());		
		// add new components
		final Button reloadSentButton = new Button("", new TextIcon("upload", TextIcon.IconSet.AWESOME));
		reloadSentButton.setTooltip(new Tooltip("Reload sentences"));
		reloadSentButton.setOnAction(actionEvent -> {
			loadSentenceList();
			loadSequences();
			hiddenVariSet.clear();
			updateVariOutput();
			updateVariantChoice();
		});		
		final Button changeDirButton = new Button("", new TextIcon("folder", TextIcon.IconSet.AWESOME));
		changeDirButton.setTooltip(new Tooltip("Change sentence directory"));
		changeDirButton.setOnAction(actionEvent -> changeSentPath());		
		final Button archiveButton = new Button("", new TextIcon("box-archive", TextIcon.IconSet.AWESOME));
		archiveButton.setTooltip(new Tooltip("Archive this directory"));
		archiveButton.setOnAction(actionEvent -> archiveSentences());
		toolBar.getItems().addAll(new Separator(), reloadSentButton, changeDirButton, archiveButton);
		mainPane.setTop(toolBar);

		// set status bar at the bottom
		final AnchorPane statusPane = new AnchorPane();
		AnchorPane.setBottomAnchor(leftFixedInfoLabel, 0.0);
		AnchorPane.setLeftAnchor(leftFixedInfoLabel, 0.0);
		AnchorPane.setBottomAnchor(rightFixedInfoLabel, 0.0);
		AnchorPane.setRightAnchor(rightFixedInfoLabel, 0.0);
		leftFixedInfoLabel.setStyle("-fx-font-family:'" + Utilities.FONTMONO +"';-fx-font-size:85%;");
		rightFixedInfoLabel.setStyle("-fx-font-family:'" + Utilities.FONTMONO +"';-fx-font-size:85%;");
		statusPane.getChildren().addAll(leftFixedInfoLabel, rightFixedInfoLabel);
		mainPane.setBottom(statusPane);

		// add main content
		final Scene scene = new Scene(mainPane, windowWidth, windowHeight);
		setScene(scene);

		// init
		final Path sentPath = Path.of(sentencePath);
		try {
			if (Files.notExists(sentPath))
				Files.createDirectories(sentPath);
		} catch (IOException e) {
			System.err.println(e);
		}
		loadSentenceList();
		loadSequences();
		sentTable.setItems(sentOutputList);
		if (sentOutputList.isEmpty())
			sentOutputList.add(new SentenceOutput());
		setupSentTable();
		variTable.setItems(variOutputList);
		updateVariOutput();
		updateVariantChoice();
		if (variOutputList.isEmpty())
			variOutputList.add(new VariantOutput());
		setupVariTable();
		Platform.runLater(() -> {
			if (sentOutputList.size() == 1 && sentOutputList.get(0).textProperty().get().isEmpty())
				sentOutputList.clear();
			else
				sentTable.getSelectionModel().select(0);
			if (variOutputList.size() == 1 && variOutputList.get(0).nameProperty().get().isEmpty())
				variOutputList.clear();
		});
	}

	public void refresh() {
		loadSentenceList();
		loadSequences();
		updateVariOutput();
		updateVariantChoice();
	}
	
	private void loadSentenceList() {
		loadSentenceList(sentencePath, allSentMap, variantMap);
		loadVariantInfo();
	}

	private void loadSentenceList(final String path, final Map<String, Sentence> sMap, final Map<String, Variant> vMap) {
		sMap.clear();
		vMap.clear();
		final List<String> sentFileNames = new ArrayList<>();
		try (final Stream<Path> entries = Files.list(Path.of(path))) {
			entries.forEach(p -> sentFileNames.add(p.getFileName().toString()));
		} catch (IOException e) {
			System.err.println(e);
		}
		if (!sentFileNames.isEmpty()) {
			for (final String fname : sentFileNames) {
				if (fname.endsWith(".json")) {
					if (fname.equals(VARINFO)) continue;
					final String hash = fname.substring(0, fname.lastIndexOf(".json"));
					final Sentence sent = new Sentence(hash);
					if (sent.setSentenceDirAndLoad(path)) {
						if (sent.isValid()) {
							sMap.put(hash, sent);
							for (final String varName : sent.getVariantSet()) {
								final Variant variant = new Variant(varName);
								vMap.put(varName, variant);
							}
						}
					}
				}
			}
		}
	}

	private void loadSequences() {
		sequenceMap.clear();
		sequenceList.clear();
		final File sentDir = new File(sentencePath);
		final File[] seqFiles = sentDir.listFiles(x -> x.getName().endsWith(".seq"));
		for (final File f : seqFiles) {
			final String seq = Utilities.getTextFileContent(f);
			final List<String> seqList = new ArrayList<>();
			seq.lines().forEach(x -> {
				final int ind = x.lastIndexOf(".json");
				if (ind > -1) {
					final String hash = x.substring(0, ind);
					seqList.add(hash);
				}
			});
			final String seqFile = f.getName();
			final String seqName = seqFile.substring(0, seqFile.lastIndexOf(".seq"));
			sequenceMap.put(seqName, seqList);
			sequenceList.add(seqName);
		}
		sequenceList.sort(String::compareTo);
		updateSeqFilterMenu();
	}

	private void loadVariantInfo() {
		final File variFile =  new File(sentencePath + VARINFO);
		if (!variFile.exists())
			saveVariantInfo(variantMap, variFile, false);
		loadVariantInfo(variantMap, variFile);
	}

	private void loadVariantInfo(final Map<String, Variant> varMap, final File varFile) {
		varMap.putAll(loadVariantInfo(varFile));
	}

	public static Map<String, Variant> loadVariantInfo(final File variFile) {
		final Map<String, Variant> varMap = new HashMap<>();
		try (final InputStream in = new BufferedInputStream(new FileInputStream(variFile))) {
			readVariants(in, varMap);
		} catch (IOException e) {
			System.err.println(e);
		}
		return varMap;
	}

	private static void readVariants(final InputStream in, final Map<String, Variant> varMap) throws IOException {
		final JsonReader reader = new JsonReader(new InputStreamReader(in, StandardCharsets.UTF_8));
		try {
			reader.beginObject();
			while (reader.hasNext()) {
				final String name = reader.nextName();
				if (name.equals("variants") && reader.peek() != JsonToken.NULL) {
					reader.beginArray();
					while (reader.hasNext()) {
						readVariant(reader, varMap);
					}
					reader.endArray();
				} else {
					reader.skipValue();
				}
			}
			reader.endObject();
		} finally {
			reader.close();
		}
	}

	private static void readVariant(final JsonReader reader, final Map<String, Variant> varMap) throws IOException {
		reader.beginObject();
		String name = "";
		String author = "";
		String note = "";
		while (reader.hasNext()) {
			final String item = reader.nextName();
			if (item.equals("name")) {
				name = reader.nextString();
				varMap.putIfAbsent(name, new Variant(name));
			} else if (item.equals("author")) {
				author = reader.nextString();
			} else if (item.equals("note")) {
				note = reader.nextString();
			} else {
				reader.skipValue();
			}
			if (!name.isEmpty() && (!author.isEmpty() || !note.isEmpty())) {
				final Variant vari = varMap.get(name);
				vari.setAuthor(author);
				vari.setNote(note);
			}
		}
		reader.endObject();
	}

	private void saveVariantInfo() {
		saveVariantInfo(variantMap, new File(sentencePath + VARINFO), false);
	}

	public static void saveVariantInfo(final Map<String, Variant> varMap, final File variFile, final boolean mergeInfo) {
		if (varMap == null) return;
		final Map<String, Variant> finalMap;
		if (variFile.exists()) {
			// read the existing, if any, then merge with the new ones
			final Map<String, Variant> oldVarMap = loadVariantInfo(variFile);
			varMap.forEach((k, v) -> {
				if (mergeInfo && oldVarMap.containsKey(k)) {
					// update information with the new one, if any
					final Variant oldVar = oldVarMap.get(k);
					if (oldVar.getAuthor().isEmpty() && !v.getAuthor().isEmpty())
						oldVar.setAuthor(v.getAuthor());
					if (oldVar.getNote().isEmpty() && !v.getNote().isEmpty())
						oldVar.setNote(v.getNote());
				} else {
					// add new variant, if not existed
					oldVarMap.put(k, v);
				}
			});
			finalMap = oldVarMap;
		} else {
			// if empty, the default variant is created
			final String defName = "Default";
			varMap.put(defName, new Variant(defName));
			finalMap = varMap;
		}
		try (final OutputStream out = new BufferedOutputStream(new FileOutputStream(variFile))) {
			writeVariants(out, finalMap);
		} catch (IOException e) {
			System.err.println(e);
		}
	}

	private static void writeVariants(final OutputStream out, final Map<String, Variant> varMap) throws IOException {
		final JsonWriter writer = new JsonWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8));
		writer.setIndent(" ");
		writer.beginObject();
		writer.name("variants");
		writer.beginArray();
		for (final Variant v : varMap.values()) {
			writer.beginObject();
			writer.name("name").value(v.getName());
			writer.name("author").value(v.getAuthor());
			writer.name("note").value(v.getNote());
			writer.endObject();
		}
		writer.endArray();
		writer.endObject();
		writer.close();
	}

	private void updateSeqFilterMenu() {
		seqFilterMenuButton.getItems().setAll(seqNoneMenuItem);
		seqFilterGroup.getToggles().setAll(seqNoneMenuItem);
		for (final String s : sequenceList) {
			final RadioMenuItem item = new RadioMenuItem(s);
			seqFilterMenuButton.getItems().add(item);
			seqFilterGroup.getToggles().add(item);
		}
		seqFilterGroup.selectToggle(seqNoneMenuItem);
	}

	private void updateSeqFilter(final String seqName) {
		currSeqFilterList.clear();
		if (NO_SEQ.equals(seqName)) return;
		currSeqFilterList.addAll(sequenceMap.get(seqName));
	}

	private Callback<TableColumn<SentenceOutput, Integer>, TableCell<SentenceOutput, Integer>> getIntegerCellFactory() {
		return col -> {
			TableCell<SentenceOutput, Integer> cell = new TableCell<SentenceOutput, Integer>() {
				@Override
				public void updateItem(final Integer item, final boolean empty) {
					super.updateItem(item, empty);
					this.setText(null);
					this.setGraphic(null);
					if (!empty && item > 0)
						this.setText("" + item);
				}
			};
			return cell;
		};
	}

	private void setupSentTable() {
		final TableColumn<SentenceOutput, String> textOutputCol = new TableColumn<>("Sentence");
		textOutputCol.setCellValueFactory(new PropertyValueFactory<>(sentOutputList.get(0).textProperty().getName()));
		textOutputCol.prefWidthProperty().bind(mainPane.widthProperty().divide(10).multiply(9).subtract(18));
		textOutputCol.setComparator(Utilities.paliComparator);
		final TableColumn<SentenceOutput, Integer> transCountOutputCol = new TableColumn<>("#Trans.");
		transCountOutputCol.setCellValueFactory(new PropertyValueFactory<>(sentOutputList.get(0).transCountProperty().getName()));
		transCountOutputCol.prefWidthProperty().bind(mainPane.widthProperty().divide(10));
		transCountOutputCol.setStyle("-fx-alignment:center");
		transCountOutputCol.setCellFactory(getIntegerCellFactory());
		sentTable.getColumns().add(textOutputCol);
		sentTable.getColumns().add(transCountOutputCol);
	}

	private void setupVariTable() {
		final TableColumn<VariantOutput, String> nameOutputCol = new TableColumn<>("Variant");
		nameOutputCol.setCellValueFactory(new PropertyValueFactory<>(variOutputList.get(0).nameProperty().getName()));
		nameOutputCol.prefWidthProperty().bind(mainPane.widthProperty().divide(9).multiply(3).subtract(12));
		final TableColumn<VariantOutput, String> authorOutputCol = new TableColumn<>("Author");
		authorOutputCol.setCellValueFactory(new PropertyValueFactory<>(variOutputList.get(0).authorProperty().getName()));
		authorOutputCol.prefWidthProperty().bind(mainPane.widthProperty().divide(9).multiply(4));
		final TableColumn<VariantOutput, Integer> transCountOutputCol = new TableColumn<>("#Trans.");
		transCountOutputCol.setCellValueFactory(new PropertyValueFactory<>(variOutputList.get(0).transCountProperty().getName()));
		transCountOutputCol.prefWidthProperty().bind(mainPane.widthProperty().divide(9));
		transCountOutputCol.setStyle("-fx-alignment:center");
		transCountOutputCol.setCellFactory(col -> {
			TableCell<VariantOutput, Integer> cell = new TableCell<VariantOutput, Integer>() {
				@Override
				public void updateItem(final Integer item, final boolean empty) {
					super.updateItem(item, empty);
					this.setText(null);
					this.setGraphic(null);
					if (!empty && item > 0)
						this.setText("" + item);
				}
			};
			return cell;
		});
		final TableColumn<VariantOutput, Boolean> visibleOutputCol = new TableColumn<>("Visibility");
		visibleOutputCol.setCellValueFactory(new PropertyValueFactory<>(variOutputList.get(0).visibleProperty().getName()));
		visibleOutputCol.prefWidthProperty().bind(mainPane.widthProperty().divide(9));
		visibleOutputCol.setStyle("-fx-alignment:center");
		visibleOutputCol.setCellFactory(col -> {
			TableCell<VariantOutput, Boolean> cell = new TableCell<VariantOutput, Boolean>() {
				@Override
				public void updateItem(final Boolean item, final boolean empty) {
					super.updateItem(item, empty);
					this.setText(null);
					this.setGraphic(null);
					if (!empty && !item)
						this.setGraphic(new TextIcon("eye-slash", TextIcon.IconSet.AWESOME));
				}
			};
			return cell;
		});
		variTable.getColumns().add(nameOutputCol);
		variTable.getColumns().add(authorOutputCol);
		variTable.getColumns().add(transCountOutputCol);
		variTable.getColumns().add(visibleOutputCol);
	}

	private void changeSentPath() {
		final File sentDir = Utilities.selectDirectory(sentenceRoot, this);
		changeSentPath(sentDir);
	}

	public void changeSentPath(final File sentDir) {
		if (sentDir == null) return;
		sentencePath = sentDir.getPath() + File.separator;
		loadSentenceList();
		loadSequences();
		hiddenVariSet.clear();
		updateVariOutput();
		updateVariantChoice();
		updateFixedInfo();
		prevSelectedVariant = "";
	}

	private void updateVariantChoice() {
		final String savVar = currSelectedVariant;
		variantChoice.getItems().clear();
		variantChoice.getItems().addAll(ALL_SENT, ONLY_TRANS, NO_TRANS);
		for (final VariantOutput vo : variOutputList) {
			if (vo.visibleProperty().get())
				variantChoice.getItems().add(vo.nameProperty().get());
		}
		currSelectedVariant = savVar;
		selectVariantChoice();
	}

	private void selectVariantChoice() {
		selectVariantChoice(currSelectedVariant);
	}

	private void selectVariantChoice(final String choice) {
		if (variantChoice.getItems().contains(choice)) {
			variantChoice.getSelectionModel().select(choice);
		} else {
			variantChoice.getSelectionModel().select(0);
		}
	}

	private void variantChoiceSelected() {
		if (variantChoice.getItems().isEmpty()) return;
		final String selected = variantChoice.getSelectionModel().getSelectedItem();
		currSelectedVariant = selected == null || selected.isEmpty() ? "" : selected;
		if (!currSelectedVariant.isEmpty())
			updateSentList();
	}

	private void updateSentList() {
		final String filter = Normalizer.normalize(sentFilterTextField.getText().trim(), Form.NFC);
		updateSentList(filter);
	}

	private void updateSentList(final String filter) {
		final List<Sentence> slist;
		final List<Sentence> sentList = new ArrayList<>();
		if (currSeqFilterList.isEmpty()) {
			sentList.addAll(allSentMap.values());
			sentList.sort((x, y) -> Utilities.paliCollator.compare(x.getText(), y.getText()));
		} else {
			currSeqFilterList.forEach(x -> {
				if (allSentMap.containsKey(x))
					sentList.add(allSentMap.get(x));
			});
		}
		final Predicate<Sentence> sentFilter;
		final Toggle filterMode = sentFilterGroup.getSelectedToggle();
		if (filterMode == findInTextMenuItem)
			sentFilter = x -> x.getText().contains(filter);
		else if (filterMode == findInEditMenuItem)
			sentFilter = x -> x.getEditText().contains(filter);
		else
			sentFilter = x -> x.getAllTranslations().contains(filter);
		if (currSelectedVariant.equals(ALL_SENT)) {
			slist = sentList.stream()
								.filter(sentFilter)
								.collect(Collectors.toList());
		} else if (currSelectedVariant.equals(ONLY_TRANS)) {
			slist = sentList.stream()
								.filter(x -> x.hasTranslation())
								.filter(sentFilter)
								.collect(Collectors.toList());
		} else if (currSelectedVariant.equals(NO_TRANS)) {
			slist = sentList.stream()
								.filter(x -> x.isTranslationEmpty())
								.filter(sentFilter)
								.collect(Collectors.toList());
		} else {
			final String vari = currSelectedVariant;
			slist = sentList.stream()
								.filter(x -> x.getVariantSet().contains(vari))
								.filter(sentFilter)
								.collect(Collectors.toList());
		}
		final List<SentenceOutput> soutList = new ArrayList<>();
		for (final Sentence s : slist)
			soutList.add(new SentenceOutput(s));
		markIdentical(soutList);
		sentOutputList.clear();
		sentOutputList.addAll(soutList);
		updateFixedInfo();
		if (!sentOutputList.isEmpty()) {
			if (currSelectedSentenceIndex >= sentOutputList.size())
				currSelectedSentenceIndex = 0;
			sentTable.getSelectionModel().select(currSelectedSentenceIndex);
		}
	}

	private void markIdentical(final List<SentenceOutput> list) {
		final Map<String, List<SentenceOutput>> outHashMap = list.stream().collect(Collectors.groupingBy(x -> { return x.getHash(); }));
		final Set<String> dupSet = outHashMap.keySet().stream().filter(x -> outHashMap.get(x).size() > 1).collect(Collectors.toSet());
		for (final SentenceOutput so : list) {
			if (dupSet.contains(so.getHash())) {
				final String text = so.textProperty().get();
				so.textProperty().set("*" + text);
			}
		}
	}

	private void updateFixedInfo() {
		final Tab selTab = tabPane.getSelectionModel().getSelectedItem();
		final String leftInfo;
		final String rightInfo;
		if (selTab == mergerTab) {
			final String lPath = leftMergerPane.getSentencePathName();
			final int lSentCount = leftMergerPane.getSentenceCount();
			final int lTransCount = leftMergerPane.getTranslationCount();
			final int lVariCount = leftMergerPane.getVariantCount();
			final int lSeqCount = leftMergerPane.getSequenceCount();
			final String rPath = rightMergerPane.getSentencePathName();
			final int rSentCount = rightMergerPane.getSentenceCount();
			final int rTransCount = rightMergerPane.getTranslationCount();
			final int rVariCount = rightMergerPane.getVariantCount();
			final int rSeqCount = rightMergerPane.getSequenceCount();
			leftInfo = String.format("[%s] S: %,5d | T: %,5d | V: %3d | Sq: %3d", lPath, lSentCount, lTransCount, lVariCount, lSeqCount);
			rightInfo = String.format("[%s] S: %,5d | T: %,5d | V: %3d | Sq: %3d", rPath, rSentCount, rTransCount, rVariCount, rSeqCount);
		} else {
			final String sentPath = Utilities.getLastPathPart(sentencePath);
			String seqFile = "";
			final RadioMenuItem selSeq = (RadioMenuItem)seqFilterGroup.getSelectedToggle();
			final String currSentNum;
			if (selSeq != null) {
				final String seqName = selSeq.getText();
				if (!NO_SEQ.equals(seqName)) {
					seqFile = ":" + seqName + ".seq";
					final int selected = sentTable.getSelectionModel().getSelectedIndex();
					currSentNum = String.format("%,5d/", selected + 1);
				} else {
					currSentNum = "";
				}
			} else {
				currSentNum = "";
			}
			final int shownSens = sentOutputList.size();
			final int totalSens = allSentMap.size();
			final long withTransCount = sentOutputList.stream().filter(x -> x.getSentence().hasTranslation()).count();
			final long totalTrans = sentOutputList.stream()
												.filter(x -> x.getSentence().hasTranslation())
												.map(x -> x.getSentence().getVariantSet())
												.flatMap(Collection::stream)
												.count();
			final int totalVari = variantMap.size();
			final File sentenceDir = new File(sentencePath);
			final int totalSeq = sentenceDir.exists() ? sentenceDir.listFiles(x -> x.getName().endsWith(".seq")).length : 0;
			leftInfo = "";
			rightInfo = String.format("[%s] Sent.: %s%,6d of %,6d (%,5d) | Trans.: %,5d | Var.: %3d | Seq.: %3d",
									sentPath + seqFile, currSentNum, shownSens, totalSens, withTransCount, totalTrans, totalVari, totalSeq);
		}
		leftFixedInfoLabel.setText(leftInfo);
		rightFixedInfoLabel.setText(rightInfo);
	}

	private void updateSentDetail(final Sentence sent) {
		sentHashLabel.setText("[" + sent.getHash() + "]");
		sentTextLabel.setText(sent.getText());
		sentEditArea.setText(sent.getEditText());
		sentTransBox.getChildren().setAll(getTransBoxes(sent));
		updateSeqInfoBox(sent);
		updateFixedInfo();
	}

	private void clearSentDetail() {
		sentHashLabel.setText("");
		sentTextLabel.setText("");
		sentEditArea.setText("");
		sentTransBox.getChildren().clear();
		seqInfoBox.getChildren().clear();
	}

	private List<TransBox> getTransBoxes(final Sentence sent) {
		final List<TransBox> result = new ArrayList<>();
		final List<String> varList = sent.getVariantSet().stream().sorted(String::compareTo).collect(Collectors.toList());
		for (final VariantOutput vo : variOutputList) {
			final String vari = vo.nameProperty().get();
			if (varList.contains(vari) && vo.visibleProperty().get()) {
				final TransBox tb = new TransBox(vari, sent.getTranslation(vari), sent);
				result.add(tb);
			}
		}
		return result;
	}

	private void updateSeqInfoBox(final Sentence sent) {
		final String hash = sent.getHash();
		int maxLen = sequenceList.stream().map(x -> x.length()).max((x, y) -> Integer.compare(x, y)).orElse(0);
		if (maxLen < SEQ_NAME.length())
			maxLen = SEQ_NAME.length();
		final String head = String.format("%-" + (maxLen + 5) + "s  Sent. Freq.", SEQ_NAME);
		final Label lblHead = new Label(head);
		lblHead.setWrapText(false);
		lblHead.setStyle("-fx-font-family:'" + Utilities.FONTMONO +"';-fx-font-weight:bold;");
		final StringBuilder result = new StringBuilder();
		for (final String seq : sequenceList) {
			final long seqCount = sequenceMap.get(seq).stream().filter(x -> x.startsWith(hash)).count();
			final String line = String.format("%-" + (maxLen + 5) + "s %,7d", seq, seqCount);
			result.append(line).append("\n");
		}
		final Label lblResult = new Label(result.toString());
		lblResult.setWrapText(false);
		lblResult.setStyle("-fx-font-family:'" + Utilities.FONTMONO +"';");
		seqInfoBox.getChildren().setAll(lblHead, new Separator(), lblResult);
	}

	private void restoreEdit() {
		final Sentence sent = sentTable.getSelectionModel().getSelectedItem().getSentence();
		sentEditArea.setText(sent.getText());
	}

	private void addTranslation(final boolean doAsk) {
		if (variantMap.isEmpty()) {
			final Alert alert = new Alert(AlertType.INFORMATION);
			alert.initOwner(this);
			alert.setHeaderText(null);
			alert.setContentText("No variant to select, create one first.");
			alert.showAndWait();
			return;
		}
		final String varName;
		if (doAsk)
			varName = getVariantFromChoices();
		else
			varName = prevSelectedVariant.isEmpty() ? getVariantFromChoices() : prevSelectedVariant;
		final Sentence sent = sentTable.getSelectionModel().getSelectedItem().getSentence();
		if (!varName.isEmpty() && !sent.hasVariant(varName)) {
			prevSelectedVariant = varName;
			sent.addTranslation(varName, "");
			final TransBox tbx = new TransBox(varName, "", sent);
			sentTransBox.getChildren().add(tbx);
			tbx.setFocus();
		}
	}

	private void showTextForTrans() {
		final Sentence sent = sentTable.getSelectionModel().getSelectedItem().getSentence();
		if (translationPane.getTop() == null) {
			final TextArea area = new TextArea();
			area.setEditable(false);
			area.setWrapText(true);
			area.setPrefRowCount(3);
			area.setText(sent.getText());
			translationPane.setTop(area);
		} else {
			translationPane.setTop(null);
		}
	}

	private void saveSentence() {
		final Sentence sent = sentTable.getSelectionModel().getSelectedItem().getSentence();
		final String editText = Normalizer.normalize(sentEditArea.getText(), Form.NFC);
		sent.setEditText(editText);
		final Map<String, String> transMap = sent.getTranslationMap();
		for (final Node node : sentTransBox.getChildren()) {
			final TransBox tb = (TransBox)node;
			transMap.put(tb.getVariant(), tb.getText());
		}
		sent.save(true);
		updateIdenticalSentence(sent);
		updateSentList();
		updateVariOutput();
		updateFixedInfo();
	}

	private void updateIdenticalSentence(final Sentence sent) {
		sentOutputList.forEach(x -> {
			if (x.getHash().equals(sent.getHash()))
				x.transCountProperty().set(sent.getVariantSet().size());
		});
	}

	/**
	 * Opens the selected sequence.
	 */
	private void openReader() {
		final RadioMenuItem selSeq = (RadioMenuItem)seqFilterGroup.getSelectedToggle();
		if (selSeq != null) {
			final String seqName = selSeq.getText();
			if (!NO_SEQ.equals(seqName)) {
				final File seqFile = new File(sentencePath + seqName + ".seq");
				final Object[] args = new Object[] { "", seqFile };
				SentenceUtilities.openWindow(Utilities.WindowType.READER, args);
			}
		}
	}

	/**
	 * Opens the selected sentence.
	 */
	private void openReader(final TableView<SentenceOutput> table) {
		final SentenceOutput sentOut = table.getSelectionModel().getSelectedItem();
		if (sentOut == null) return;
		final Sentence sent = sentOut.getSentence();
		if (sent == null) return;
		final Object[] args = new Object[] { "", sent.getFile() };
		SentenceUtilities.openWindow(Utilities.WindowType.READER, args);
	}

	private void deleteSentence() {
		final SentenceOutput sentOut = sentTable.getSelectionModel().getSelectedItem();
		if (sentOut == null) return;
		final ConfirmAlert delAlert = new ConfirmAlert(this, ConfirmAlert.ConfirmType.DELETE);
		delAlert.setMessage("A sentence file will be deleted. This causes related sequence files to be updated. "
							+ "Do it cautiously.");
		final Optional<ButtonType> response = delAlert.showAndWait();
		if (response.isPresent()) {
			if (response.get() == delAlert.getConfirmButtonType()) {
				final Sentence sent = sentOut.getSentence();
				sent.getFile().delete();
				loadSentenceList();
				updateSequenceAfterDelete(sent);
				updateVariOutput();
				updateVariantChoice();
				updateFixedInfo();
			}
		}
	}

	private void updateSequenceAfterDelete(final Sentence sent) {
		final String hash = sent.getHash();
		for (final String seq : sequenceList) {
			final List<String> seqList = sequenceMap.get(seq);
			if (seqList.contains(hash)) {
				final List<String> updatedList = seqList.stream().filter(x -> !x.startsWith(hash)).collect(Collectors.toList());
				sequenceMap.put(seq, updatedList);
				saveSequence(seq, updatedList);
			}
		}
	}

	private void saveSequence(final String seqName, final List<String> seqList) {
		final StringBuilder output = new StringBuilder();
		final File seqFile = new File(sentencePath + seqName + ".seq");
		seqList.forEach(x -> {
			output.append(x).append(".json");
			output.append(System.getProperty("line.separator"));
		});
		Utilities.saveText(output.toString(), seqFile);
	}

	private void saveSequenceAs() {
		final RadioMenuItem selSeq = (RadioMenuItem)seqFilterGroup.getSelectedToggle();
		if (selSeq == null) return;
		final String seqName = selSeq.getText();
		if (NO_SEQ.equals(seqName)) return;
		final File targetSeqFile = Utilities.getOutputFile(seqName + ".seq", sentencePath, SentenceManager.INSTANCE);
		if (targetSeqFile == null || targetSeqFile.getParent() == null) return;
		final String targetPath = targetSeqFile.getParent() + File.separator;
		final List<String> orgSeqList = sequenceMap.get(seqName);
		// clean up the list, remove non-existing items
		final List<String> seqList = orgSeqList.stream()
											.filter(x -> Files.exists(Path.of(sentencePath + x + ".json")))
											.collect(Collectors.toList());
		// check whether the target sentence files already exist
		boolean needReplace = false;
		for (final String s : seqList) {
			if (Files.exists(Path.of(targetPath + s + ".json"))) {
				needReplace = true;
				break;
			}
		}
		// copy sentence files in the list
		if (needReplace) {
			final ConfirmAlert replaceAlert = new ConfirmAlert(SentenceManager.INSTANCE, ConfirmAlert.ConfirmType.REPLACE);
			final Optional<ButtonType> response = replaceAlert.showAndWait();
			if (response.isPresent()) {
				if (response.get() == replaceAlert.getConfirmButtonType())
					copySentenceFiles(seqList, targetPath, true);
				else if (response.get() == replaceAlert.getKeepButtonType())
					copySentenceFiles(seqList, targetPath, false);
				else
					return;
			} else {
				return;
			}
		} else {
			copySentenceFiles(seqList, targetPath, false);
		}
		// generate sequence file, and save it
		final String LINESEP = System.getProperty("line.separator");
		final StringBuilder seqStr = new StringBuilder();
		for (final String s : seqList)
			seqStr.append(s).append(".json").append(LINESEP);
		Utilities.saveText(seqStr.toString(), targetSeqFile);
		// extract only variants used and save them in the variant info
		final Set<String> varSet = seqList.stream()
										.filter(x -> allSentMap.get(x).hasTranslation())
										.map(x -> allSentMap.get(x).getVariantSet())
										.flatMap(Collection::stream)
										.collect(Collectors.toSet());
		final Map<String, Variant> varMap = new HashMap<>();
		for (final String v : varSet)
			varMap.put(v, variantMap.get(v));
		saveVariantInfo(varMap, new File(targetPath + VARINFO), true);
	}

	private void copySentenceFiles(final List<String> seqList, final String targetDir, final boolean overWrite) {
		for (final String s : seqList) {
			final Path src = Path.of(sentencePath + s + ".json");
			final Path tgt = Path.of(targetDir + s + ".json");
			try {
			if (Files.exists(tgt)) {
				if (overWrite)
					Files.copy(src, tgt, StandardCopyOption.REPLACE_EXISTING);
			} else {
				Files.copy(src, tgt);
			}
			} catch (IOException e) {
				System.err.println(e);
			}
		}
	}

	private void updateVariOutput() {
		variOutputList.clear();
		final List<String> varList = variantMap.keySet().stream().sorted(String::compareTo).collect(Collectors.toList());
		for (final String v : varList) {
			final VariantOutput variOut = new VariantOutput(variantMap.get(v));
			final long transCount = allSentMap.values().stream().filter(x -> x.hasVariant(v)).count();
			variOut.transCountProperty().set((int)transCount);
			variOut.visibleProperty().set(!hiddenVariSet.contains(v));
			variOutputList.add(variOut);
		}
	}

	private void toggleHideVariant() {
		final VariantOutput variOut = variTable.getSelectionModel().getSelectedItem();
		if (variOut == null) return;
		final boolean visible = !variOut.visibleProperty().get();
		variOut.visibleProperty().set(visible);
		final String vari = variOut.nameProperty().get();
		if (visible)
			hiddenVariSet.remove(vari);
		else
			hiddenVariSet.add(vari);
		updateVariantChoice();
	}

	private void addVariant() {
		final String newVar = getNewVariantFromTextInput();
		if (newVar.isEmpty()) return;
		final Variant vari = new Variant(newVar);
		variantMap.put(newVar, vari);
		prevSelectedVariant = newVar;
		saveVariantInfo();
		updateVariOutput();
		updateFixedInfo();
	}

	private void renameVariant() {
		final VariantOutput variOut = variTable.getSelectionModel().getSelectedItem();
		if (variOut == null) return;
		final Variant vari = variOut.getVariant();
		if (vari == null) return;
		final String variName = vari.getName();
		final String newName = getNewVariantFromTextInput(variName);
		if (newName.isEmpty() || variantMap.containsKey(newName)) return;
		final Variant variant = variantMap.get(variName);
		variant.setName(newName);
		for (final Sentence sen : allSentMap.values()) {
			if (sen.hasVariant(variName)) {
				sen.renameVariant(variName, newName);
				sen.save(true);
			}
		}
		saveVariantInfo();
		loadSentenceList();
		updateVariOutput();
		updateVariantChoice();
	}

	private void deleteVariant() {
		final VariantOutput variOut = variTable.getSelectionModel().getSelectedItem();
		if (variOut == null) return;
		final Variant vari = variOut.getVariant();
		if (vari == null) return;
		final String variName = vari.getName();
		final boolean doAsk = variOut.transCountProperty().get() > 0;
		if (doAsk) {
			final ConfirmAlert delAlert = new ConfirmAlert(this, ConfirmAlert.ConfirmType.DELETE);
			delAlert.setContentText("All translations under '" + variName + "' will be deleted. "
									+ "Related files in this directory will be updated. Sure?");
			final Optional<ButtonType> response = delAlert.showAndWait();
			if (response.isPresent()) {
				if (response.get() == delAlert.getConfirmButtonType())
					deleteVariant(variName);
			}
		} else {
			deleteVariant(variName);
		}
	}

	private void deleteVariant(final String variName) {
		variantMap.remove(variName);
		saveVariantInfo();
		for (final Sentence sen : allSentMap.values()) {
			if (sen.hasVariant(variName)) {
				sen.removeVariant(variName);
				sen.save(true);
			}
		}
		updateVariOutput();
		updateFixedInfo();
	}

	private void saveVariant() {
		final VariantOutput variOut = variTable.getSelectionModel().getSelectedItem();
		if (variOut == null) return;
		final String author = Normalizer.normalize(variAuthorTextField.getText().trim(), Form.NFC);
		final String note = Normalizer.normalize(variNoteArea.getText().trim(), Form.NFC);
		final Variant vari = variantMap.get(variOut.getVariant().getName());
		vari.setAuthor(author);
		vari.setNote(note);
		saveVariantInfo();
		updateVariOutput();
	}

	private void updateVariDetail(final Variant vari) {
		variNameLabel.setText(vari.getName());
		variAuthorTextField.setText(vari.getAuthor());
		variNoteArea.setText(vari.getNote());
	}

	private void clearVariDetail() {
		variNameLabel.setText("");
		variAuthorTextField.clear();
		variNoteArea.clear();
	}

	private String getNewVariantFromTextInput() {
		return getNewVariantFromTextInput("");
	}

	private String getNewVariantFromTextInput(final String defStr) {
		final TextInputDialog dialog = new TextInputDialog(defStr);
		dialog.initOwner(this);
		dialog.setTitle("Enter variant name");
		dialog.setHeaderText("A variant name must be unique, and should relate to its author.");
		dialog.setContentText("Variant name:");
		final Optional<String> result = dialog.showAndWait();
		if (!result.isPresent() || result.get().isEmpty()) {
			return "";
		} else {
			final String vari = result.get().trim();
			if (vari.equalsIgnoreCase(ALL_SENT)
				|| vari.equalsIgnoreCase(ONLY_TRANS)
				|| vari.equalsIgnoreCase(NO_TRANS))
				return "";
			else
				return result.get().trim().replaceAll("['\"`]", "");
		}
	}

	private String getVariantFromChoices() {
		final List<String> varList = variantMap.keySet().stream().sorted(String::compareTo).collect(Collectors.toList());
		if (!varList.contains(prevSelectedVariant))
			prevSelectedVariant = varList.get(0);
		final ChoiceDialog<String> dialog = new ChoiceDialog<>(prevSelectedVariant, varList);
		dialog.initOwner(this);
		dialog.setTitle("Select a variant");
		dialog.setHeaderText("Select a variant from the list, or else create a new one first.");
		dialog.setContentText("Variant name:");
		final Optional<String> result = dialog.showAndWait();
		if (result.isPresent())
			return result.get();
		else
			return "";
	}

	private void archiveSentences() {
		if (sentencePath.isEmpty()) return;
		final String zname = Utilities.getLastPathPart(sentencePath);
		final String dir = sentencePath.substring(0, sentencePath.lastIndexOf(zname));
		final File zipFile = Utilities.getOutputFile(zname + ".zip", dir, this);
		if (zipFile == null) return;
		try (final ZipOutputStream zout = new ZipOutputStream(new FileOutputStream(zipFile))) {
			try (final Stream<Path> entries = Files.list(Path.of(sentencePath))) {
				entries.forEach(p -> {
					try {
						final ZipEntry entry = new ZipEntry(zname + File.separator + p.getFileName().toString());
						zout.putNextEntry(entry);
						zout.write(Files.readAllBytes(p), 0, (int)Files.size(p));
						zout.closeEntry();
					} catch (IOException e) {
						System.err.println(e);
					}
				});
			} catch (IOException e) {
				System.err.println(e);
			}
		} catch (IOException e) {
			System.err.println(e);
		}
	}

	private void merge() {
		final String title = "Select output directory (empty)";
		final File targetDir = Utilities.selectDirectory(sentenceRoot, SentenceManager.INSTANCE, title);
		if (targetDir == null) return;
		if (targetDir.list().length > 0) {
			final Alert alert = new Alert(AlertType.INFORMATION);
			alert.initOwner(this);
			alert.setHeaderText(null);
			alert.setContentText("Please select an empty directory.");
			alert.showAndWait();
			return;
		}
		// (1) merge sentences
		final Map<String, Sentence> lSentMap = leftMergerPane.getSentList().stream().collect(Collectors.toMap(x -> x.getHash(), Function.identity()));
		final Map<String, Sentence> rSentMap = rightMergerPane.getSentList().stream().collect(Collectors.toMap(x -> x.getHash(), Function.identity()));
		final List<Sentence> outputList = new ArrayList<>();
		// add the uncommons
		for (final Sentence sent : lSentMap.values()) {
			if (!rSentMap.containsKey(sent.getHash()))
				outputList.add(sent);
		}
		for (final Sentence sent : rSentMap.values()) {
			if (!lSentMap.containsKey(sent.getHash()))
				outputList.add(sent);
		}
		// add the commons, check options
		for (final Sentence sent : lSentMap.values()) {
			if (rSentMap.containsKey(sent.getHash())) {
				final Sentence rSent = rSentMap.get(sent.getHash());
				if (!leftMergerPane.keepEditButton.isSelected())
					sent.setEditText(rSent.getEditText());
				// merge translation from the other side
				for (final String v : rSent.getVariantSet()) {
					if (sent.hasVariant(v)) {
						if (!leftMergerPane.keepTransButton.isSelected())
							sent.setTranslation(v, rSent.getTranslation(v));
					} else {
						sent.addTranslation(v, rSent.getTranslation(v));
					}
				}
			}
			outputList.add(sent);
		}
		for (final Sentence sent : outputList) {
			sent.setSentenceDir(targetDir.getPath() + File.separator);
			sent.save(false);
		}
		// (2) merge variant info
		if (cbMergeVarInfo.isSelected()) {
			final Map<String, Variant> lVarMap = leftMergerPane.getVarMap();
			final Map<String, Variant> rVarMap = rightMergerPane.getVarMap();
			final Map<String, Variant> outputVarMap = new HashMap<>();
			// add the uncommons
			lVarMap.forEach((k, v) -> {
				if (!rVarMap.containsKey(k))
					outputVarMap.put(k, v);
			});
			rVarMap.forEach((k, v) -> {
				if (!lVarMap.containsKey(k))
					outputVarMap.put(k, v);
			});
			// add the commons, check the option
			lVarMap.forEach((k, v) -> {
				if (rVarMap.containsKey(k))
					if (leftMergerPane.keepInfoButton.isSelected())
						outputVarMap.put(k, v);
					else
						outputVarMap.put(k, rVarMap.get(k));
			});
			saveVariantInfo(outputVarMap, new File(targetDir.getPath() + File.separator + VARINFO) , false);
		}
		// (3) merger sequence files
		final File[] lFileList = leftMergerPane.getSentenceDir().listFiles(x -> x.getName().endsWith(".seq"));
		final Map<String, File> lFileMap = Arrays.asList(lFileList).stream().collect(Collectors.toMap(x -> x.getName(), Function.identity()));
		final File[] rFileList = rightMergerPane.getSentenceDir().listFiles(x -> x.getName().endsWith(".seq"));
		final Map<String, File> rFileMap = Arrays.asList(rFileList).stream().collect(Collectors.toMap(x -> x.getName(), Function.identity()));
		try {
			// add the uncommons
			for (final File f : lFileList) {
				final String fname = f.getName();
				if (!rFileMap.containsKey(fname))
					Files.copy(f.toPath(), Path.of(targetDir.getPath() + File.separator + fname));
			}
			for (final File f : rFileList) {
				final String fname = f.getName();
				if (!lFileMap.containsKey(fname))
					Files.copy(f.toPath(), Path.of(targetDir.getPath() + File.separator + fname));
			}
			// add the commons, check the option
			for (final File f : lFileList) {
				final String fname = f.getName();
				if (rFileMap.containsKey(fname)) {
					if (leftMergerPane.keepSeqButton.isSelected())
						Files.copy(f.toPath(), Path.of(targetDir.getPath() + File.separator + fname));
					else
						Files.copy(rFileMap.get(fname).toPath(), Path.of(targetDir.getPath() + File.separator + fname));
				}
			}
		} catch (IOException e) {
			System.err.println(e);
		}
	}

	private void updateMergerTables() {
		leftMergerPane.updateResult(rightMergerPane.getSentList());
		rightMergerPane.updateResult(leftMergerPane.getSentList());
	}

	private String makeText() {
		final String LINESEP = System.getProperty("line.separator");
		final String FIELDSEP = "|";
		final StringBuilder result = new StringBuilder();
		final Tab selected = tabPane.getSelectionModel().getSelectedItem();
		if (selected == variantTab) {
			result.append("[").append(Utilities.getLastPathPart(sentencePath)).append("]").append(LINESEP);
			for (int i=0; i < variTable.getColumns().size(); i++)
				result.append(variTable.getColumns().get(i).getText()).append(FIELDSEP);
			result.append(LINESEP);
			for (int i=0; i < variTable.getItems().size(); i++){
				final VariantOutput variOut = variTable.getItems().get(i);
				result.append(variOut.nameProperty().get()).append(FIELDSEP);
				result.append(variOut.authorProperty().get()).append(FIELDSEP);
				result.append(variOut.transCountProperty().get()).append(FIELDSEP);
				result.append(LINESEP);
			}
		} else {
			final TableView<SentenceOutput> table1;
			final TableView<SentenceOutput> table2;
			final String head1;
			final String head2;
			if (selected == mergerTab) {
				table1 = leftMergerPane.getTable();
				table2 = rightMergerPane.getTable();
				head1 = "[" + leftMergerPane.getSentencePathName() + "]" + LINESEP;
				head2 = "[" + rightMergerPane.getSentencePathName() + "]" + LINESEP;
			} else {
				table1 = sentTable;
				table2 = null;
				head1 = "[" + Utilities.getLastPathPart(sentencePath) + "]" + LINESEP;
				head2 = "";
			}
			result.append(head1);
			for (int i = 0; i < table1.getColumns().size(); i++)
				result.append(table1.getColumns().get(i).getText()).append(FIELDSEP);
			result.append(LINESEP);
			for (int i = 0; i < table1.getItems().size(); i++){
				final SentenceOutput sentOut = table1.getItems().get(i);
				result.append(sentOut.textProperty().get()).append(FIELDSEP);
				result.append(sentOut.transCountProperty().get()).append(FIELDSEP);
				result.append(LINESEP);
			}
			if (table2 != null) {
				result.append(LINESEP);
				result.append(head2);
				for (int i = 0; i < table2.getColumns().size(); i++)
					result.append(table2.getColumns().get(i).getText()).append(FIELDSEP);
				result.append(LINESEP);
				for (int i = 0; i < table2.getItems().size(); i++){
					final SentenceOutput sentOut = table2.getItems().get(i);
					result.append(sentOut.textProperty().get()).append(FIELDSEP);
					result.append(sentOut.transCountProperty().get()).append(FIELDSEP);
					result.append(LINESEP);
				}
			}
		}
		return result.toString();
	}
	
	private void copyText() {
		Utilities.copyText(makeText());
	}
	
	private void saveText() {
		Utilities.saveText(makeText(), "senmanout.txt");
	}

	// inner classes
	private static class ExpandButton extends ToggleButton {
		private TextArea area;
		private static final int min = 2;
		private static final int max = 10;

		public ExpandButton(final TextArea textArea) {
			super("", new TextIcon("expand", TextIcon.IconSet.AWESOME));
			setTooltip(new Tooltip("Expand edit area"));
			area = textArea;
			area.setPrefRowCount(min);
			setSelected(false);
			setOnAction(actionEvent -> expand());
		}

		public void expand() {
			final int row = isSelected() ? max : min;
			area.setPrefRowCount(row);
		}
	}

	private class TransBox extends VBox {
		private String variant;
		private SimpleStringProperty text;
		private final PaliTextInput transInput = new PaliTextInput(PaliTextInput.InputType.AREA);
		final TextArea txtArea = (TextArea)transInput.getInput();

		public TransBox(final String vari, final String txt, final Sentence sent) {
			super(3);
			variant = vari;
			transInput.setInputMethod(PaliTextInput.InputMethod.NORMAL);
			text = new SimpleStringProperty(txt);
			txtArea.textProperty().bindBidirectional(text);
			txtArea.setWrapText(true);
			final HBox hBox = new HBox(5);
			hBox.setAlignment(Pos.CENTER_LEFT);
			final Label varName = new Label(variant);
			final ExpandButton expandButton = new ExpandButton(txtArea);
			final Button deleteButton = new Button("", new TextIcon("trash", TextIcon.IconSet.AWESOME));
			deleteButton.setTooltip(new Tooltip("Delete this translation"));
			deleteButton.setOnAction(actionEvent -> deleteTranslation(sent));		
			hBox.getChildren().addAll(expandButton, transInput.getMethodButton(), deleteButton, varName);
			getChildren().addAll(hBox, txtArea);
		}

		public void setVariant(final String vari) {
			variant = vari;
		}

		public String getVariant() {
			return variant;
		}

		public void setText(final String txt) {
			text.set(txt);
		}

		public String getText() {
			return text.get();
		}

		public void setFocus() {
			txtArea.requestFocus();
		}

		private void deleteTranslation(final Sentence sent) {
			sent.removeTranslation(variant);
			sentTransBox.getChildren().remove(this);
		}
	}

	private class MergerPane extends VBox {
		private final TableView<SentenceOutput> table = new TableView<>();
		private final ObservableList<SentenceOutput> outputList = FXCollections.<SentenceOutput>observableArrayList();
		private final RadioButton keepEditButton = new RadioButton("Keep edit"); 
		private final RadioButton keepTransButton = new RadioButton("Keep translations"); 
		private final RadioButton keepInfoButton = new RadioButton("Keep variant info");
		private final RadioButton keepSeqButton = new RadioButton("Keep sequence files");
		private final List<Sentence> senList = new ArrayList<>();
		private final SimpleBooleanProperty listAvailable = new SimpleBooleanProperty(false);
		private final Map<String, Variant> varMap = new HashMap<>();
		private File sentenceDir = null;

		private MergerPane() {
			// add toolbar on the top
			final ToolBar mtoolbar = new ToolBar();
			final Button openDirButton = new Button("", new TextIcon("folder-open", TextIcon.IconSet.AWESOME));
			openDirButton.setTooltip(new Tooltip("Select a directory"));
			openDirButton.setOnAction(actionEvent -> selectDirectory());
			final Button openReaderButton = new Button("", new TextIcon("book-open", TextIcon.IconSet.AWESOME));
			openReaderButton.setTooltip(new Tooltip("Open in a new Reader"));
			openReaderButton.setOnAction(actionEvent -> openReader(table));		
			final Button clearButton = new Button("", new TextIcon("trash", TextIcon.IconSet.AWESOME));
			clearButton.setTooltip(new Tooltip("Clear"));
			clearButton.setOnAction(actionEvent -> clear());		
			mtoolbar.getItems().addAll(openDirButton, openReaderButton, clearButton);
			final VBox optionBox = new VBox(3);
			optionBox.setPadding(new Insets(5));
			VBox.setVgrow(table, Priority.ALWAYS);
			table.setItems(outputList);
			keepInfoButton.disableProperty().bind(cbMergeVarInfo.selectedProperty().not());
			optionBox.getChildren().addAll(keepEditButton, keepTransButton, keepInfoButton, keepSeqButton);
			getChildren().addAll(mtoolbar, table, optionBox);
		}

		private List<Sentence> getSentList() {
			return senList;
		}

		private File getSentenceDir() {
			return sentenceDir;
		}

		private TableView<SentenceOutput> getTable() {
			return table;
		}

		private int getSentenceCount() {
			return senList.size();
		}

		private int getTranslationCount() {
			final long result = senList.stream()
										.filter(x -> x.hasTranslation())
										.map(x -> x.getVariantSet())
										.flatMap(Collection::stream)
										.count();
			return (int)result;
		}

		private Map<String, Variant> getVarMap() {
			return varMap;
		}

		private int getVariantCount() {
			return varMap.size();
		}

		private int getSequenceCount() {
			return sentenceDir == null ? 0 : sentenceDir.listFiles(x -> x.getName().endsWith(".seq")).length;
		}

		private SimpleBooleanProperty listAvailableProperty() {
			return listAvailable;
		}

		private String getSentencePathName() {
			return Utilities.getLastPathPart(sentenceDir);
		}

		private void selectDirectory() {
			final File dir = Utilities.selectDirectory(sentenceRoot, SentenceManager.INSTANCE);
			if (dir != null) {
				sentenceDir = dir;
				final Map<String, Sentence> senMap = new HashMap<>();
				loadSentenceList(dir.getPath() + File.separator, senMap, varMap);
				currSeqFilterList.clear();
				loadVariantInfo(varMap, new File(dir.getPath() + File.separator + VARINFO));
				senList.addAll(senMap.values());
				senList.sort((x, y) -> Utilities.paliCollator.compare(x.getText(), y.getText()));
				updateFixedInfo();
				updateResult();
				setupTable();
				listAvailable.set(true);
			}
		}

		private void clear() {
			sentenceDir = null;
			listAvailable.set(false);
			senList.clear();
			outputList.clear();
			updateFixedInfo();
		}

		private void updateResult() {
			updateResult(Collections.emptyList());
		}

		private void updateResult(final List<Sentence> otherList) {
			outputList.clear();
			final List<SentenceOutput> outList;
			if (showOptionGroup.getSelectedToggle() == showAllButton) {
				outList = senList.stream().map(x -> new SentenceOutput(x)).collect(Collectors.toList());
			} else {
				final Set<String> otherHashSet = otherList.stream().map(x -> x.getHash()).collect(Collectors.toSet());
				if (showOptionGroup.getSelectedToggle() == showEqualButton) {
					outList = senList.stream()
									.filter(x -> otherHashSet.contains(x.getHash()))
									.map(x -> new SentenceOutput(x))
									.collect(Collectors.toList());
				} else {
					outList = senList.stream()
									.filter(x -> !otherHashSet.contains(x.getHash()))
									.map(x -> new SentenceOutput(x))
									.collect(Collectors.toList());
				}
			}
			if (outList.isEmpty())
				outputList.add(new SentenceOutput());
			else
				outputList.addAll(outList);
		}

		private void setupTable() {
			table.getColumns().clear();
			final TableColumn<SentenceOutput, String> textOutputCol = new TableColumn<>("Sentence");
			textOutputCol.setCellValueFactory(new PropertyValueFactory<>(outputList.get(0).textProperty().getName()));
			textOutputCol.prefWidthProperty().bind(this.widthProperty().divide(5).multiply(4).subtract(6));
			textOutputCol.setComparator(Utilities.paliComparator);
			final TableColumn<SentenceOutput, Integer> transCountOutputCol = new TableColumn<>("#Trans.");
			transCountOutputCol.setCellValueFactory(new PropertyValueFactory<>(outputList.get(0).transCountProperty().getName()));
			transCountOutputCol.prefWidthProperty().bind(this.widthProperty().divide(5));
			transCountOutputCol.setStyle("-fx-alignment:center");
			transCountOutputCol.setCellFactory(getIntegerCellFactory());
			table.getColumns().add(textOutputCol);
			table.getColumns().add(transCountOutputCol);
		}
	}

	public static final class SentenceOutput {
		private StringProperty text;
		private IntegerProperty transCount;
		private final Sentence sent;
		private final String hash;

		public SentenceOutput() {
			textProperty().set("");
			transCountProperty().set(0);
			sent = null;
			hash = "";
		}

		public SentenceOutput(final Sentence sent) {
			textProperty().set(sent.toString());
			transCountProperty().set(sent.getVariantSet().size());
			this.sent = sent;
			hash = sent.getHash();
		}

		public StringProperty textProperty() {
			if (text == null)
				text = new SimpleStringProperty(this, "text");
			return text;
		}

		public IntegerProperty transCountProperty() {
			if (transCount == null)
				transCount = new SimpleIntegerProperty(this, "transCount");
			return transCount;
		}
		
		public Sentence getSentence() {
			return sent;
		}

		public String getHash() {
			return hash;
		}
	}

	public static final class VariantOutput {
		private StringProperty name;
		private StringProperty author;
		private IntegerProperty transCount;
		private BooleanProperty visible;
		private final Variant variant;

		public VariantOutput() {
			nameProperty().set("");
			authorProperty().set("");
			transCountProperty().set(0);
			variant = null;
		}

		public VariantOutput(final Variant vari) {
			nameProperty().set(vari.getName());
			authorProperty().set(vari.getAuthor());
			transCountProperty().set(0);
			visibleProperty().set(true);
			variant = vari;
		}

		public StringProperty nameProperty() {
			if (name == null)
				name = new SimpleStringProperty(this, "name");
			return name;
		}

		public StringProperty authorProperty() {
			if (author == null)
				author = new SimpleStringProperty(this, "author");
			return author;
		}

		public IntegerProperty transCountProperty() {
			if (transCount == null)
				transCount = new SimpleIntegerProperty(this, "transCount");
			return transCount;
		}

		public BooleanProperty visibleProperty() {
			if (visible == null)
				visible = new SimpleBooleanProperty(this, "visible");
			return visible;
		}
		
		public Variant getVariant() {
			return variant;
		}
	}

}
