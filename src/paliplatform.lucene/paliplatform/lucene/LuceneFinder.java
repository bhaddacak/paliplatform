/*
 * LuceneFinder.java
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

import java.util.*;
import java.util.zip.*;
import java.util.regex.*;
import java.util.function.*;
import java.util.stream.*;
import java.io.*;
import java.nio.file.*;
import java.text.Normalizer;
import java.text.Normalizer.Form;

import javax.xml.parsers.*;
import org.xml.sax.*;
import org.xml.sax.helpers.DefaultHandler;

import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.scene.image.*;
import javafx.scene.text.*;
import javafx.scene.layout.*;
import javafx.geometry.*;
import javafx.stage.Stage;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.Analyzer.TokenStreamComponents;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.util.CharTokenizer;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.highlight.*;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.Directory;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.queryparser.classic.ParseException;

/** 
 * Advanced document finder using Apache Lucene.
 * @author J.R. Bhaddacak
 * @version 3.0
 * @since 2.0
 */
public class LuceneFinder extends BorderPane {
	public static String FIELD_PATH = "path";
	private Stage theStage;
	private final BorderPane mainPane = new BorderPane();
	private final BorderPane contentPane = new BorderPane();
	private final PaliTextInput textInput = new PaliTextInput(PaliTextInput.InputType.COMBO);
	private final ComboBox<String> searchComboBox;
	private final TextField searchTextField;
	private final FieldSelectorBox fieldOptionsBox;
	private final VBox searchResultBox = new VBox();
	private final ContextMenu searchResultPopupMenu = new ContextMenu();
	private final AnchorPane statusPane = new AnchorPane();
	private final Label statusInfo = new Label();
	private final HBox progressBox = new HBox(3);
	private final ProgressBar progressBar = new ProgressBar();
	private final Label progressMessage = new Label();
	private final InfoPopup mainHelpPopup = new InfoPopup();
	private final InfoPopup searchHelpPopup = new InfoPopup();
	private final VBox toolBarBox = new VBox();
	private final CommonWorkingToolBar mainToolBar;
	private final ToolBar buildToolBar = new ToolBar();
	private final ChoiceBox<String> indexChoice = new ChoiceBox<>();
	private final ToggleButton buildOptionsToggle = new ToggleButton("Indexing options", new TextIcon("gears", TextIcon.IconSet.AWESOME));
	private final CorpusSelectorBox corpusSelector;
	private final ChoiceBox<Integer> maxResultChoice = new ChoiceBox<>();
	private final CheckMenuItem includeNumberMenuItem = new CheckMenuItem("Include numbers");
	private final CheckMenuItem includeBoldMenuItem = new CheckMenuItem("Include field 'bold'");
	private	final RadioMenuItem oneCharMenuItem = new RadioMenuItem("== 1 char long");
	private final ToggleGroup lengthExclusionGroup = new ToggleGroup();
	private final CheckMenuItem useStopwordsMenuItem = new CheckMenuItem("Use stopwords");
	private final CheckMenuItem showWholeLineMenuItem = new CheckMenuItem("Show whole lines");
	private final ToggleButton showSearchDetailButton = new ToggleButton("", new TextIcon("glasses", TextIcon.IconSet.AWESOME));
	private Corpus currCorpus = null;
	private DocumentInfo currSelectedDoc = null;
	
	public LuceneFinder() {
		currCorpus = ReaderUtilities.corpusMap.get(Corpus.Collection.CSTR); // this will be changed in updateIndexInfo
		corpusSelector = new CorpusSelectorBox(currCorpus, null);
		
		// prepare field selector
		fieldOptionsBox = new FieldSelectorBox(() -> search());

		// add toolbars on the top
		mainToolBar = new CommonWorkingToolBar(searchResultBox);
		// config some buttons
		mainToolBar.saveTextButton.setOnAction(actionEvent -> saveText());		
		mainToolBar.copyButton.setOnAction(actionEvent -> copyText());		
		// add new components
		final Button refreshButton = new Button("", new TextIcon("repeat", TextIcon.IconSet.AWESOME));
		refreshButton.setTooltip(new Tooltip("Refresh"));
		refreshButton.setOnAction(actionEvent -> updateIndexChoice());
		final Button deleteButton = new Button("", new TextIcon("xmark", TextIcon.IconSet.AWESOME));
		deleteButton.disableProperty().bind(indexChoice.valueProperty().isNull());
		deleteButton.setTooltip(new Tooltip("Delete selected index"));
		deleteButton.setOnAction(actionEvent -> deleteIndex());
		indexChoice.setTooltip(new Tooltip("Index selector"));
		indexChoice.setOnAction(actionEvent -> updateIndexInfo());
		buildOptionsToggle.setOnAction(actionEvent -> initToolBars());
		final Button mainHelpButton = new Button("", new TextIcon("circle-question", TextIcon.IconSet.AWESOME));
		mainHelpButton.setOnAction(actionEvent -> mainHelpPopup.showPopup(mainHelpButton, InfoPopup.Pos.BELOW_RIGHT, true));
		mainToolBar.getItems().addAll(new Separator(), refreshButton, indexChoice, deleteButton, buildOptionsToggle, mainHelpButton);
		// add second toolbar for building indices
		final Button buildIndexButton = new Button("Build", new TextIcon("screwdriver-wrench", TextIcon.IconSet.AWESOME));
		buildIndexButton.setTooltip(new Tooltip("Build/rebuild Lucene index"));
		buildIndexButton.setOnAction(actionEvent -> buildIndex());
		final MenuButton buildOptionsMenu = new MenuButton("", new TextIcon("check-double", TextIcon.IconSet.AWESOME));		
		buildOptionsMenu.setTooltip(new Tooltip("Options for indexing"));
		includeBoldMenuItem.setSelected(false);
		includeBoldMenuItem.disableProperty().bind(corpusSelector.getBoldAvailable().not());
		final Menu lengthExcludeMenu = new Menu("Length exclusion");
		final RadioMenuItem noExcMenuItem = new RadioMenuItem("No exclusion");
		final RadioMenuItem twoCharMenuItem = new RadioMenuItem("<= 2 chars long");
		final RadioMenuItem threeCharMenuItem = new RadioMenuItem("<= 3 chars long");
		lengthExcludeMenu.getItems().addAll(noExcMenuItem, oneCharMenuItem, twoCharMenuItem, threeCharMenuItem);
		lengthExclusionGroup.getToggles().addAll(noExcMenuItem, oneCharMenuItem, twoCharMenuItem, threeCharMenuItem);
		lengthExclusionGroup.selectToggle(oneCharMenuItem);
		final MenuItem editStopwordsMenuItem = new MenuItem("Edit stopwords");
		editStopwordsMenuItem.setOnAction(actionEvent -> editStopwords());
		final MenuItem setToDefaultMenuItem = new MenuItem("Set to defaults");
		setToDefaultMenuItem.setOnAction(actionEvent -> setDefaultIndexOptions());
		buildOptionsMenu.getItems().addAll(includeNumberMenuItem, includeBoldMenuItem,
										lengthExcludeMenu, useStopwordsMenuItem, 
										new SeparatorMenuItem(), editStopwordsMenuItem, setToDefaultMenuItem);
		buildToolBar.getItems().addAll(buildIndexButton, corpusSelector, buildOptionsMenu);
		mainPane.setTop(toolBarBox);

		// add main content
		// add search toolbar
		final ToolBar searchToolBar = new ToolBar();
		searchComboBox = textInput.getComboBox();
		searchComboBox.setPromptText("Search for...");
		searchComboBox.setPrefWidth(Utilities.getRelativeSize(24));
		searchComboBox.setOnKeyPressed(keyEvent -> {
			if (keyEvent.getEventType() == KeyEvent.KEY_PRESSED) {
				if (keyEvent.getCode() == KeyCode.ENTER)
					search();
				else if (keyEvent.getCode() == KeyCode.ESCAPE)
					clearSearch();
			}
		});
		searchTextField = (TextField)textInput.getInput();
		final Button searchButton = new Button("Search");
		searchButton.disableProperty().bind(indexChoice.valueProperty().isNull());
		searchButton.setOnAction(actionEvent -> search());
		final List<Integer> maxList = Arrays.asList(10, 20, 30, 50, 100);
		maxResultChoice.setTooltip(new Tooltip("Maximum results"));
		maxResultChoice.getItems().addAll(maxList);
		maxResultChoice.getSelectionModel().select(0);
		maxResultChoice.setOnAction(actionEvent -> search());
		final Button fieldSelButton = new Button("", new TextIcon("list-check", TextIcon.IconSet.AWESOME));
		fieldSelButton.setTooltip(new Tooltip("Field selector on/off"));
		fieldSelButton.setOnAction(actionEvent -> openFieldSelector());
		showSearchDetailButton.setTooltip(new Tooltip("Show text fragments"));
		showSearchDetailButton.setOnAction(actionEvent -> search());
		final Button foldUpAllButton = new Button("", new TextIcon("angles-up", TextIcon.IconSet.AWESOME));
		foldUpAllButton.setTooltip(new Tooltip("Collapse all"));
		foldUpAllButton.disableProperty().bind(showSearchDetailButton.selectedProperty().not());
		foldUpAllButton.setOnAction(actionEvent -> foldSearchResult(false));
		final Button foldDownAllButton = new Button("", new TextIcon("angles-down", TextIcon.IconSet.AWESOME));
		foldDownAllButton.setTooltip(new Tooltip("Expand all"));
		foldDownAllButton.disableProperty().bind(showSearchDetailButton.selectedProperty().not());
		foldDownAllButton.setOnAction(actionEvent -> foldSearchResult(true));
		final MenuButton searchOptionsMenu = new MenuButton("", new TextIcon("check-double", TextIcon.IconSet.AWESOME));		
		searchOptionsMenu.setTooltip(new Tooltip("Options for search results"));
		showWholeLineMenuItem.setOnAction(actionEvent -> search());
		searchOptionsMenu.getItems().addAll(showWholeLineMenuItem);
		final Button searchHelpButton = new Button("", new TextIcon("circle-question", TextIcon.IconSet.AWESOME));
		searchHelpButton.setOnAction(actionEvent -> searchHelpPopup.showPopup(searchHelpButton, InfoPopup.Pos.BELOW_RIGHT, true));
		searchToolBar.getItems().addAll(searchComboBox, textInput.getClearButton(), textInput.getMethodButton(),
									searchButton, maxResultChoice, fieldSelButton, showSearchDetailButton, 
									foldUpAllButton, foldDownAllButton, searchOptionsMenu, searchHelpButton);
		contentPane.setTop(searchToolBar);

		// add search result box at the center
		searchResultBox.prefWidthProperty().bind(contentPane.widthProperty().subtract(16));
		final ScrollPane searchResultPane = new ScrollPane(searchResultBox);
		contentPane.setCenter(searchResultPane);
		
		mainPane.setCenter(contentPane);
		setCenter(mainPane);

		// add status pane at the bottom
		statusPane.getChildren().add(statusInfo);
		mainPane.setBottom(statusPane);

		// set drop action
		mainPane.setOnDragOver(dragEvent -> {
			if (dragEvent.getGestureSource() != mainPane && dragEvent.getDragboard().hasString()) {
				dragEvent.acceptTransferModes(TransferMode.COPY_OR_MOVE);
			}
			dragEvent.consume();
		});
		mainPane.setOnDragDropped(dragEvent -> {
			final Dragboard db = dragEvent.getDragboard();
			if (db.hasString()) {
				final String[] allLines = db.getString().split("\\n");
				final String head = allLines[0].trim();
				addTermToSearch(head);	
				dragEvent.setDropCompleted(true);
			} else {
				dragEvent.setDropCompleted(false);
			}
			dragEvent.consume();
		});

		// init
		AnchorPane.setBottomAnchor(statusInfo, 0.0);
		AnchorPane.setRightAnchor(statusInfo, 0.0);
		AnchorPane.setBottomAnchor(progressBox, 0.0);
		AnchorPane.setLeftAnchor(progressBox, 0.0);
		progressBox.setAlignment(Pos.BOTTOM_CENTER);
		progressBox.getChildren().addAll(progressBar, progressMessage);
		mainHelpPopup.setContentWithText(LuceneUtilities.getTextResource("info-lucene.txt"));
		mainHelpPopup.setTextWidth(Utilities.getRelativeSize(32));
		searchHelpPopup.setContentWithText(LuceneUtilities.getTextResource("info-lucene-search.txt"));
		searchHelpPopup.setTextWidth(Utilities.getRelativeSize(32));
		// prepare search result context menu
		final MenuItem openDocMenuItem = new MenuItem("Open");
		openDocMenuItem.setOnAction(actionEvent -> openCurrentDoc());
		searchResultPopupMenu.getItems().addAll(openDocMenuItem);
		// init
		setPrefWidth(Utilities.getRelativeSize(60));
		setPrefHeight(Utilities.getRelativeSize(32));
		if (LuceneUtilities.simpleServiceMap == null) 
			LuceneUtilities.simpleServiceMap = LuceneUtilities.getSimpleServices();
		init();
	}

	public void init() {
		buildOptionsToggle.setSelected(false);
		Platform.runLater(() -> {
			initToolBars();
			setDefaultIndexOptions();
			clearSearch();
			searchResultBox.getChildren().clear();
			updateIndexChoice();
		});
	}

	private void initToolBars() {
		toolBarBox.getChildren().clear();
		if (buildOptionsToggle.isSelected())
			toolBarBox.getChildren().addAll(mainToolBar, buildToolBar);
		else
			toolBarBox.getChildren().addAll(mainToolBar);
		corpusSelector.init();
	}

	public void setStage(final Stage stage) {
		theStage = stage;
	}
	
	private Map<TermInfo.Field, StringBuilder> buildTextMap(final Corpus.Collection col) {
		final Map<TermInfo.Field, StringBuilder> textMap = new EnumMap<>(TermInfo.Field.class);
		for (final TermInfo.Field fld : TermInfo.Field.getFieldList(col))
			textMap.put(fld, new StringBuilder());
		return textMap;
	}

	private void buildIndex() {
		final String selectedCorpusStr = corpusSelector.getSelectedIdString();
		currCorpus = corpusSelector.getSelectedCorpus();
		final Pattern fileFilterPattern = corpusSelector.getFileFilterPattern();
		String flag = "";
		if (includeNumberMenuItem.isSelected() || includeBoldMenuItem.isSelected()) {
			final String nflag = includeNumberMenuItem.isSelected() ? "n" : "";
			final String bflag = includeBoldMenuItem.isSelected() ? "b" : "";
			flag = "-" + nflag + bflag; 
		}
		final String dirName = selectedCorpusStr + flag;
		final boolean confirm = chooseIndexPath(dirName, LuceneIndex.OpenMode.WRITE);
		if (!confirm) return;
		final LuceneIndex currIndex = new LuceneIndex(dirName);
		statusInfo.setText("Indexing " + currIndex.getIndexInfo());
		currIndex.clearIndexDir();
		progressBar.setProgress(0);
		final Task<Boolean> buildTask = new Task<Boolean>() {
			@Override
			protected Boolean call() throws Exception {
				updateMessage("Building index... (please wait)");
				try {
					final Path indexPath = Path.of(currIndex.getIndexPath());
					if (!Files.exists(indexPath)) return false;
					final Analyzer analyzer = new PaliIndexAnalyzer(currCorpus);
					final Directory directory = FSDirectory.open(indexPath);
					final IndexWriterConfig config = new IndexWriterConfig(analyzer);
					final IndexWriter iwriter = new IndexWriter(directory, config);
					final Corpus.Collection col = currCorpus.getCollection();
					final String rexNonWord = Corpus.getNotWordRex(currCorpus.getCollection());
					int total, count;
					if (col == Corpus.Collection.CSTR) {
						final Map<String, DocumentInfo> docInfoMap = currCorpus.getDocInfoMap();
						total = docInfoMap.size();
						count = 0;
						for (final DocumentInfo dinfo : docInfoMap.values()) {
							updateProgress(count++, total);
							final String filename = dinfo.getFileNameWithExt();
							final Matcher fileMatcher = fileFilterPattern.matcher(filename);
							if (!fileMatcher.matches()) continue;
							final File docFile = new File(Utilities.ROOTDIR + ReaderUtilities.TEXTPATH + currCorpus.getRootName() + File.separator, filename);
							if (!docFile.exists()) {
								System.err.println(filename + " is missing");
								continue;
							}
							final Map<TermInfo.Field, StringBuilder> textMap = buildTextMap(col);
							final TextHandler handler = new CstrTextHandler(textMap);
							handler.processFile(docFile);
							final Document doc = new Document();
							doc.add(new StringField(FIELD_PATH, filename, Field.Store.YES));
							textMap.forEach((f, sb) -> {
								final boolean doAdd = !includeBoldMenuItem.isSelected() && f == TermInfo.Field.BOLD ? false : true;
								if (doAdd) {
									final String text = tokenize(sb.toString().toLowerCase(), rexNonWord);
									doc.add(new org.apache.lucene.document.TextField(f.getTag(), text, Field.Store.NO));
								}
							});
							iwriter.addDocument(doc);
						} // end for
					} else if (col == Corpus.Collection.CST4) {
						final SAXParserFactory spf = SAXParserFactory.newInstance();
						final SAXParser saxParser = spf.newSAXParser();
						final ZipFile zip = new ZipFile(currCorpus.getZipFile());
						total = zip.size();
						count = 0;
						for (final Enumeration<? extends ZipEntry> e = zip.entries(); e.hasMoreElements();) {
							updateProgress(count++, total);
							final ZipEntry entry = e.nextElement();
							final String fullname = entry.getName();
							final String[] strName = fullname.split("/");
							final String nameToMatch = strName[strName.length - 1];
							final Matcher fileMatcher = fileFilterPattern.matcher(nameToMatch);
							if (!fileMatcher.matches()) continue;
							final Map<TermInfo.Field, StringBuilder> textMap = buildTextMap(col);
							final DefaultHandler handler = new Cst4SAXHandler(textMap);
							saxParser.parse(zip.getInputStream(entry), handler);
							final Document doc = new Document();
							doc.add(new StringField(FIELD_PATH, fullname, Field.Store.YES));
							textMap.forEach((f, sb) -> {
								final boolean doAdd = !includeBoldMenuItem.isSelected() && f == TermInfo.Field.BOLD ? false : true;
								if (doAdd) {
									final String text = tokenize(sb.toString().toLowerCase(), rexNonWord);
									doc.add(new org.apache.lucene.document.TextField(f.getTag(), text, Field.Store.NO));
								}
							});
							iwriter.addDocument(doc);
						} // end for
						zip.close();
					} else if (col == Corpus.Collection.SC || col == Corpus.Collection.PTST || col == Corpus.Collection.BJT
								|| col == Corpus.Collection.SRT || col == Corpus.Collection.GRAM) {
						final ZipFile zip = new ZipFile(currCorpus.getZipFile());
						total = zip.size();
						count = 0;
						for (final Enumeration<? extends ZipEntry> e = zip.entries(); e.hasMoreElements();) {
							updateProgress(count++, total);
							final ZipEntry entry = e.nextElement();
							final String fullname = entry.getName();
							final String[] strName = fullname.split("/");
							final String nameToMatch = col == Corpus.Collection.SC || col == Corpus.Collection.SRT 
														? fullname
														: strName[strName.length - 1];
							final Matcher fileMatcher = fileFilterPattern.matcher(nameToMatch);
							if (!fileMatcher.matches()) continue;
							final Map<TermInfo.Field, StringBuilder> textMap = buildTextMap(col);
							final TextHandler handler;
							if (col == Corpus.Collection.SC)
								handler = new ScTextHandler(textMap);
							else if (col == Corpus.Collection.PTST)
								handler = new PtstTextHandler(textMap);
							else if (col == Corpus.Collection.BJT)
								handler = new BjtTextHandler(textMap);
							else if (col == Corpus.Collection.SRT)
								handler = new SrtTextHandler(textMap);
							else
								handler = new GramTextHandler(textMap);
							handler.processStream(zip.getInputStream(entry));
							final Document doc = new Document();
							doc.add(new StringField(FIELD_PATH, fullname, Field.Store.YES));
							textMap.forEach((f, sb) -> {
								final String text = tokenize(sb.toString().toLowerCase(), rexNonWord);
								doc.add(new org.apache.lucene.document.TextField(f.getTag(), text, Field.Store.NO));
							});
							iwriter.addDocument(doc);
						} // end for
						zip.close();
					}
					iwriter.close();
				} catch (SAXException | ParserConfigurationException | IOException e) {
					System.err.println(e);
				}
				Platform.runLater(() -> {
					progressBar.progressProperty().unbind();
					statusPane.getChildren().remove(progressBox);
					updateIndexChoice(dirName);
				});
				return true;
			}
		};
		progressBar.progressProperty().bind(buildTask.progressProperty());
		buildTask.messageProperty().addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
			progressMessage.setText(newValue);
		});
		Utilities.threadPool.submit(buildTask);
		statusPane.getChildren().add(progressBox);
	}

	private boolean chooseIndexPath(final String dirName, final LuceneIndex.OpenMode mode) {
		if (dirName == null || dirName.isEmpty()) return false;
		final File indexPath = new File(LuceneIndex.getIndexPath(dirName));
		boolean proceed = false;
		if (mode == LuceneIndex.OpenMode.WRITE) {
			// open to write index
			if (indexPath.exists()) {
				// index already exists
				proceed = proceedBuildConfirm();
			} else {
				// not exist, create new dir
				LuceneIndex.createIndexDir(indexPath);
				proceed = true;
			}
		} else if (mode == LuceneIndex.OpenMode.READ) {
			// open to read
			proceed = indexPath.exists();
		} else {
			proceed = false;
		}
		return proceed;
	}

	private void deleteIndex() {
		final String indName = indexChoice.getSelectionModel().getSelectedItem();
		if (indName == null) return;
		final ConfirmAlert delAlert = new ConfirmAlert(theStage, ConfirmAlert.ConfirmType.DELETE);
		delAlert.setMessage("An index directory (" + indName + ") will be deleted.\nAre you sure to do this?");
		final Optional<ButtonType> response = delAlert.showAndWait();
		if (response.isPresent()) {
			if (response.get() == delAlert.getConfirmButtonType()) {
				final LuceneIndex selected = getSelectedLuceneIndex();
				if (selected != null) {
					selected.deleteIndexDir();
					updateIndexChoice();
					fieldOptionsBox.init();
				}
			}
		}
	}

	private LuceneIndex getSelectedLuceneIndex() {
		final String indexName = indexChoice.getSelectionModel().getSelectedItem();
		if (indexName == null || indexName.isEmpty()) {
			return null;
		} else {
			return LuceneUtilities.indexMap.get(indexName);
		}
	}

	private void fillIndexChoice() {
		LuceneUtilities.updateIndexList();
		final List<String> indList = LuceneUtilities.getIndexNameList();
		indList.sort(LuceneUtilities.indexNameComparator);
		indexChoice.getItems().clear();
		if (!indList.isEmpty())
			indexChoice.getItems().addAll(indList);
	}

	private void updateIndexChoice() {
		fillIndexChoice();
		indexChoice.getSelectionModel().select(0);
		updateIndexInfo();
	}

	private void updateIndexChoice(final String selectedItem) {
		fillIndexChoice();
		indexChoice.getSelectionModel().select(selectedItem);
		updateIndexInfo();
	}

	private void updateIndexInfo() {
		statusInfo.setText("");
		final LuceneIndex currIndex = getSelectedLuceneIndex();
		if (currIndex == null || !currIndex.isAvailable()) {
			fieldOptionsBox.setFieldAvailable(FieldSelectorBox.SimpleField.BOLD, false);
			return;
		}
		final Corpus.Collection col = currIndex.getCollection();
		currCorpus = ReaderUtilities.corpusMap.get(col);
		fieldOptionsBox.setFieldAvailable(FieldSelectorBox.SimpleField.BOLD, currIndex.isBoldAvailable());
		fieldOptionsBox.setFieldAvailable(FieldSelectorBox.SimpleField.BODY, true);
		fieldOptionsBox.setFieldAvailable(FieldSelectorBox.SimpleField.NOTE, 
				Corpus.hasOnlyBodyTextAndNotes(col) || Corpus.hasFullStructure(col));
		fieldOptionsBox.setFieldAvailable(FieldSelectorBox.SimpleField.HEAD, 
				Corpus.hasOnlyBodyTextAndHead(col) || Corpus.hasAlmostFullButNotes(col) || Corpus.hasFullStructure(col));
		fieldOptionsBox.setFieldAvailable(FieldSelectorBox.SimpleField.GATHA, 
				Corpus.hasAlmostFullButNotes(col) || Corpus.hasFullStructure(col));
		statusInfo.setText(currIndex.getIndexInfo());
		search();
	}

	private String tokenize(final String text, final String rexNonWord) {
		final String[] tokens = text.replaceAll("(\\d+)", " $1 ").split(rexNonWord); // padding digits with spaces before spliting
		final List<String> tokenList = new ArrayList<>();
		for (final String t : tokens) {
			// splitting by brackets, done separately
			tokenList.addAll(Arrays.asList(t.split("[\\[\\]]")));
		}
		final RadioMenuItem widExcRadio = (RadioMenuItem)lengthExclusionGroup.getSelectedToggle();
		final Predicate<String> widExcCond;
		if (widExcRadio.getText().contains("1"))
			widExcCond = x -> Utilities.getPaliWordLength(x) == 1; 
		else if (widExcRadio.getText().contains("2"))
			widExcCond = x -> Utilities.getPaliWordLength(x) <= 2; 
		else if (widExcRadio.getText().contains("3"))
			widExcCond = x -> Utilities.getPaliWordLength(x) <= 3; 
		else widExcCond = x -> false;
		final Predicate<String> inclNumCond;
		if (!includeNumberMenuItem.isSelected())
			inclNumCond = x -> x.matches("^\\d+\\S*");
		else
			inclNumCond = x -> false;
		final Predicate<String> stopwrdCond;
		if (useStopwordsMenuItem.isSelected())
			stopwrdCond = x -> LuceneUtilities.stopwords.contains(x);
		else
			stopwrdCond = x -> false;
		final String result = tokenList.stream()
										.filter(x -> !x.isEmpty())
										.filter(Predicate.not(stopwrdCond))
										.filter(Predicate.not(inclNumCond))
										.filter(Predicate.not(widExcCond))
										.collect(Collectors.joining(" "));
		return result;
	}

	private void setDefaultIndexOptions() {
		includeNumberMenuItem.setSelected(false);
		includeBoldMenuItem.setSelected(false);
		lengthExclusionGroup.selectToggle(oneCharMenuItem);
		useStopwordsMenuItem.setSelected(false);
	}

	private void editStopwords() {
		final SimpleService editorLauncher = (SimpleService)LuceneUtilities.simpleServiceMap.get("paliplatform.main.EditorLauncher");
		if (editorLauncher != null) {
			final Runnable callback = new Runnable() {
				@Override
				public void run() {
					LuceneUtilities.updateStopwords();
				}
			};
			final Object[] args = new Object[] { LuceneUtilities.stopwordsFile, callback };
			editorLauncher.processArray(args);
		}
	}

	public void setSearchInput(final String text) {
		searchTextField.setText(text);
	}

	private void clearSearch() {
		searchTextField.clear();
	}

	private void search() {
		final LuceneIndex currIndex = getSelectedLuceneIndex();
		if (currIndex == null) return;
		String strQuery = Normalizer.normalize(searchTextField.getText().trim(), Form.NFC);
		if (currCorpus.getCollection() == Corpus.Collection.SC)
			strQuery = Utilities.changeToScNiggahita(strQuery, true);
		if (strQuery.isEmpty()) {
			searchResultBox.getChildren().clear();
			return;
		}
		searchComboBox.commitValue();
		final int maxCount = maxResultChoice.getSelectionModel().getSelectedItem();
		try {
			final Path indexPath = Path.of(currIndex.getIndexPath());
			if (!Files.exists(indexPath)) return;
			final Analyzer analyzer = new PaliIndexAnalyzer(currCorpus);
			final Directory directory = FSDirectory.open(indexPath);
			final DirectoryReader ireader = DirectoryReader.open(directory);
			final IndexSearcher isearcher = new IndexSearcher(ireader);
			final Map<TermInfo.Field, ScoreDoc[]> scoreDocMap = new EnumMap<>(TermInfo.Field.class);
			for (final TermInfo.Field f : TermInfo.Field.getFieldList(currCorpus.getCollection())) {
				if (fieldOptionsBox.isFieldSelected(f)) {
					final QueryParser parser = new QueryParser(f.getTag(), analyzer);
					parser.setDefaultOperator(QueryParser.Operator.AND);
					final Query query = parser.parse(strQuery);
					scoreDocMap.put(f, isearcher.search(query, maxCount).scoreDocs);
				}
			}
			final List<SearchOutput> outputList = new ArrayList<>();
			for (final TermInfo.Field f : scoreDocMap.keySet()) {
				final ScoreDoc[] docs = scoreDocMap.get(f);
				final Set<Integer> idSet = new HashSet<>();
				for (final ScoreDoc sd : docs) {
					if (!idSet.contains(sd.doc)) {
						idSet.add(sd.doc);
						outputList.add(new SearchOutput(f, sd));
					}
				}
			}
			outputList.sort((x, y) -> Float.compare(y.getScore(), x.getScore()));
			updateSearchResult(outputList, ireader, strQuery);
			ireader.close();
			directory.close();
		} catch (ParseException | IOException e) {
			System.err.println(e);
		}
	}

	private void updateSearchResult(final List<SearchOutput> outputList, final DirectoryReader ireader, final String strQuery) {
		searchResultBox.getChildren().clear();
		if (!outputList.isEmpty())
			textInput.recordQuery();
		final int maxCount = maxResultChoice.getSelectionModel().getSelectedItem();
		final Map<Integer, Map<TermInfo.Field, StringBuilder>> resultTextMap = new HashMap<>();
		final Corpus.Collection col = currCorpus.getCollection();
		try {
			if (showSearchDetailButton.isSelected()) {
				// prepare text of each result
				if (col == Corpus.Collection.CSTR) {
					for (final SearchOutput so : outputList) {
						final int docID = so.getDocID();
						if (!resultTextMap.containsKey(docID)) {
							final Map<TermInfo.Field, StringBuilder> textMap = buildTextMap(col);
							final String filename = ireader.storedFields().document(docID).get(FIELD_PATH);
							final File docFile = new File(Utilities.ROOTDIR + ReaderUtilities.TEXTPATH + currCorpus.getRootName() + File.separator, filename);
							final TextHandler handler = new CstrTextHandler(textMap);
							handler.processFile(docFile);
							resultTextMap.put(docID, textMap);
						}
					}
				} else if (col == Corpus.Collection.CST4) {
					final SAXParserFactory spf = SAXParserFactory.newInstance();
					final SAXParser saxParser = spf.newSAXParser();
					final ZipFile zip = new ZipFile(currCorpus.getZipFile());
					for (final SearchOutput so : outputList) {
						final int docID = so.getDocID();
						if (!resultTextMap.containsKey(docID)) {
							final Map<TermInfo.Field, StringBuilder> textMap = buildTextMap(col);
							final DefaultHandler handler = new Cst4SAXHandler(textMap);
							final String filename = ireader.storedFields().document(docID).get(FIELD_PATH);
							final ZipEntry entry = zip.getEntry(filename);
							if (entry != null)
								saxParser.parse(zip.getInputStream(entry), handler);
							resultTextMap.put(docID, textMap);
						}
					}
					zip.close();
				} else if (col == Corpus.Collection.SC || col == Corpus.Collection.PTST || col == Corpus.Collection.BJT
							|| col == Corpus.Collection.SRT || col == Corpus.Collection.GRAM) {
					final ZipFile zip = new ZipFile(currCorpus.getZipFile());
					for (final SearchOutput so : outputList) {
						final int docID = so.getDocID();
						if (!resultTextMap.containsKey(docID)) {
							final Map<TermInfo.Field, StringBuilder> textMap = buildTextMap(col);
							final TextHandler handler;
							if (col == Corpus.Collection.SC)
								handler = new ScTextHandler(textMap);
							else if (col == Corpus.Collection.PTST)
								handler = new PtstTextHandler(textMap);
							else if (col == Corpus.Collection.BJT)
								handler = new BjtTextHandler(textMap);
							else if (col == Corpus.Collection.SRT)
								handler = new SrtTextHandler(textMap);
							else
								handler = new GramTextHandler(textMap);
							final String filename = ireader.storedFields().document(docID).get(FIELD_PATH);
							final ZipEntry entry = zip.getEntry(filename);
							if (entry != null)
								handler.processStream(zip.getInputStream(entry));
							resultTextMap.put(docID, textMap);
						}
					}
					zip.close();
				}
			}
			final Analyzer analyzer = new PaliIndexAnalyzer(currCorpus);
			for (int i = 0; i < outputList.size(); i++) {       
				if (i >= maxCount) break;
				final SearchOutput soutput = outputList.get(i);
				final String filename = ireader.storedFields().document(soutput.getDocID()).get(FIELD_PATH);
				final DocumentInfo docInfo = ReaderUtilities.corpusMap.get(col).getDocInfoByFileName(filename);
				if (docInfo == null) continue;
				final String docInfoStr = docInfo.getTextName() + 
										String.format(" [Score: %.4f] (%s)", soutput.getScore(), soutput.getField().getTag());
				final TitledPane tpane;
				if (showSearchDetailButton.isSelected()) {
					final StringBuilder resultText = new StringBuilder();
					final String text = resultTextMap.get(soutput.getDocID()).get(soutput.getField()).toString()
													.replaceAll(" {2,}", " ").replace(" .", ".").replace(" ,", ",").trim();
					if (showWholeLineMenuItem.isSelected()) {
						// use custom fragmenter
						resultText.append(getFragmentManually(text, strQuery, true));
					} else {
						// use Lucene fragmenter
						final QueryParser parser = new QueryParser(soutput.getField().getTag(), analyzer);
						parser.setDefaultOperator(QueryParser.Operator.AND);
						final Query query = parser.parse(strQuery);
						final SimpleHTMLFormatter htmlFormatter = new SimpleHTMLFormatter("{", "}");
						final Highlighter highlighter = new Highlighter(htmlFormatter, new QueryScorer(query));
						final Fragmenter fragmenter = new SimpleFragmenter(100);
						highlighter.setTextFragmenter(fragmenter);
						final TokenStream tokenStream = analyzer.tokenStream(soutput.getField().getTag(), text);
						final String[] frags = highlighter.getBestFragments(tokenStream, text, 10);
						if (frags.length > 0) {
							for (int j = 0; j < frags.length; j++) {
								final String[] tmps = frags[j].split("\n");
								for (final String s : tmps) {
									if (s.contains("{")) {
										resultText.append("‣ ").append(s).append("...\n");
									}
								}
							}
						} else {
							// highlighter fails, use custom fragmenter instead
							resultText.append(getFragmentManually(text, strQuery, false));
						}
					}
					tpane = new TitledPane(docInfoStr, createTextFlow(resultText.toString()));
					tpane.setExpanded(true);
				} else {
					tpane = new TitledPane(docInfoStr, null); 
					tpane.setCollapsible(false);
				}
				tpane.setUserData(docInfo);
				tpane.setContextMenu(searchResultPopupMenu);
				tpane.setOnContextMenuRequested(cmevent -> {
					final TitledPane tp = (TitledPane)cmevent.getSource();
					currSelectedDoc = (DocumentInfo)tp.getUserData();
				});
				searchResultBox.getChildren().add(tpane);
			} // end for
		} catch (SAXException | ParserConfigurationException | ParseException | InvalidTokenOffsetsException | IOException e) {
			System.err.println(e);
		}
	}

	private String getFragmentManually(final String text, final String strQuery, final boolean isWholeLine) {
		// (1) generate query word list (those need highlight)
		final List<String> wlist = new ArrayList<>();
		if (strQuery.charAt(0) == '/' && strQuery.charAt(strQuery.length()-1) == '/') {
			// it is regex query, take it as a whole
			final String rxStr = strQuery.substring(1, strQuery.length()-1);
			wlist.add(rxStr);
		} else {
			String strProcessed;
			strProcessed = strQuery.replaceAll("[&+|/!^~(){}\\[\\]\\-\\\\]", " "); // strip off symbols
			// break query string into words
			if (strProcessed.indexOf('?') >= 0) {
				// replace '?' with a non-whitespace
				strProcessed = strProcessed.replace("?", "\\\\S"); 
			}
			if (strProcessed.indexOf('*') >= 0) {
				// replace '*' with non-whitespace
				strProcessed = strProcessed.replace("*", "\\\\S*");
			}
			final int qInd = strProcessed.indexOf('"');
			if (qInd >= 0) {
				// if '"' is used, take the whole word
				String qStr;
				if (strProcessed.charAt(strProcessed.length()-1) == '"') {
					int lastInd = strProcessed.lastIndexOf('"');
					qStr = strProcessed.substring(qInd+1, lastInd);
				} else {
					qStr = strProcessed.substring(qInd+1);
					// in case of a mixed-up, exclude text after "
					final int qqInd = qStr.indexOf('"');
					if (qqInd >= 0)
						qStr = qStr.substring(0, qqInd).trim();
					
				}
				wlist.add(qStr);
			} else {
				for (final String qs : strProcessed.split("\\s+")) {
					String q = qs;
					final int clInd = qs.indexOf(":"); // field selector, exclude it
					if (clInd >= 0)
						q = qs.substring(clInd + 1);
					if (q.isEmpty()) continue;
					if (q.endsWith("AND") || q.endsWith("OR") || q.endsWith("NOT"))
						continue;
					wlist.add(q);
					if (!Character.isUpperCase(q.charAt(0)))
						wlist.add(Character.toUpperCase(q.charAt(0)) + q.substring(1));
				} // end for
			} // end if
		} // end if
		// (2) find the words' position line by line, then generate the string output
		final StringBuilder result = new StringBuilder();
		String lineOutput = "";
		boolean found = false;
		final String[] lines = text.split("\n");
		for (final String line : lines) {
			lineOutput = line;
			found = false;
			final String wb = "\\b";
			// enclose each word with {}
			for (final String s : wlist) {
				final Pattern patt = Pattern.compile(wb + s + wb);
				final Matcher matcher = patt.matcher(lineOutput);
				if (matcher.find()) {
					lineOutput = matcher.replaceAll("{" + matcher.group() + "}");
					found = true;
				}
			}
			if (found) {
				if (!isWholeLine) {
					// if not whole line, truncate the line
					final int padding = 20; // padding at start and end
					final int firstOpenB = lineOutput.indexOf("{"); // start of the first word
					if (firstOpenB > padding) {
						lineOutput = "..." + lineOutput.substring(firstOpenB - padding);
					}
					final int lastCloseB = lineOutput.lastIndexOf("}"); // end of the last word
					if (lastCloseB < lineOutput.length() - padding) {
						lineOutput = lineOutput.substring(0, lastCloseB + padding) + "...";
					}
					// if the result is still too long, truncate it in the middle, also compensate {}, if cut
					if (lineOutput.length() > 100) {
						final int firstCloseB = lineOutput.indexOf("}");
						final int lastOpenB = lineOutput.lastIndexOf("{");
						final String firstPart = lineOutput.substring(0, firstCloseB + padding);
						final String fpEndB = firstPart.lastIndexOf("{") > firstPart.lastIndexOf("}") ? "}" : "";
						final String lastPart = lineOutput.substring(lastOpenB - padding);
						final String lpEndB = lastPart.indexOf("{") > lastPart.indexOf("}") ? "{" : "";
						lineOutput = firstPart + fpEndB + "..." + lpEndB + lastPart;
					}
				}
				result.append("› " + lineOutput + "\n");
			}
		}
		return result.toString();
	}

	private TextFlow createTextFlow(final String text) {
		final List<Text> tlist = new ArrayList<>();
		final String[] lines = text.split("\n");
		int count = 0;
		for (final String line : lines) {
			final String[] leftB = line.split("\\{");
			for (final String part : leftB) {
				if (part.contains("}")) {
					final String[] rightB = part.split("\\}");
					final Text ht = new Text(rightB[0]);
					ht.getStyleClass().add("search-result-highlight");
					tlist.add(ht);
					if (rightB.length > 1) {
						final Text nt = new Text(rightB[1]);
						nt.getStyleClass().add("search-result-normal");
						tlist.add(nt);
					}
				} else {
					final Text txt = new Text(part);
					txt.getStyleClass().add("search-result-normal");
					tlist.add(txt);
				}
			}
			tlist.add(new Text("\n"));
			count++;
		}
		final TextFlow tflow = new TextFlow();
		tflow.getChildren().addAll(tlist);
		return tflow;
	}

	private void foldSearchResult(final boolean doExpand) {
		if (showSearchDetailButton.isSelected()) {
			for (final Node n : searchResultBox.getChildren()) {
				final TitledPane tp = (TitledPane)n;
				tp.setExpanded(doExpand);
			}
		}
	}

	private void openFieldSelector() {
		if (contentPane.getRight() == null) {
			contentPane.setRight(fieldOptionsBox);
		} else {
			contentPane.setRight(null);
		}
	}

	private void openCurrentDoc() {
		if (currSelectedDoc != null) {
			final Corpus.Collection col = currCorpus.getCollection();
			if (col == Corpus.Collection.SC) {
				ReaderUtilities.openScReader(col, currSelectedDoc);
			} else {
				final TocTreeNode ttn = currSelectedDoc.toTocTreeNode();
				ReaderUtilities.openPaliHtmlViewer(ttn);
			}
		}
	}

	private void addTermToSearch(final String term) {
		final String existing = searchTextField.getText();
		final String space = existing.isEmpty() ? "" : " ";
		searchTextField.setText(existing + space + term);
	}

	private boolean proceedBuildConfirm() {
		boolean output = false;
		final String message = "The existing index will be replaced, \nproceed to continue.";
		final ConfirmAlert proceedAlert = new ConfirmAlert(theStage, ConfirmAlert.ConfirmType.PROCEED, message);
		final Optional<ButtonType> result = proceedAlert.showAndWait();
		if (result.isPresent()) {
			if (result.get() == proceedAlert.getConfirmButtonType())
				output = true;
		}
		return output;		
	}

	private String makeText() {
		final LuceneIndex currIndex = getSelectedLuceneIndex();
		if (currIndex == null) return "";
		final StringBuilder result = new StringBuilder();
		result.append("Index path: ").append(currIndex.getIndexPath());
		result.append(System.getProperty("line.separator"));
		result.append("Query: ").append(Normalizer.normalize(searchTextField.getText().trim(), Form.NFC));
		result.append(System.getProperty("line.separator"));
		result.append("Results: ").append(System.getProperty("line.separator"));
		for (final Node n : searchResultBox.getChildren()) {
			final TitledPane tp = (TitledPane)n;
			result.append(tp.getText());
			result.append(System.getProperty("line.separator"));
			final Node content = tp.getContent();
			if (content != null) {
				final TextFlow tf = (TextFlow)content;
				for (final Node tn : tf.getChildren()) {
					final Text txt = (Text)tn;
					result.append(txt.getText());
				}
				result.append(System.getProperty("line.separator"));
			}
		}
		return result.toString();
	}
	
	private void copyText() {
		Utilities.copyText(makeText());
	}
	
	private void saveText() {
		Utilities.saveText(makeText(), "luceneout.txt");
	}

	// inner classes
	private class SearchOutput {
		private final TermInfo.Field field;
		private final int doc;
		private final float score;

		private SearchOutput(final TermInfo.Field field, final ScoreDoc sdoc) {
			this.field = field;
			doc = sdoc.doc;
			score = sdoc.score;
		}

		private TermInfo.Field getField() {
			return field;
		}

		private int getDocID() {
			return doc;
		}

		private float getScore() {
			return score;
		}
	}

	public class PaliIndexAnalyzer extends Analyzer {
		private final String notWord;

		public PaliIndexAnalyzer(final Corpus corpus) {
			notWord = Corpus.getNotWordString(corpus.getCollection());
		}

		@Override
		public TokenStreamComponents createComponents(final String fieldName) {
			return new TokenStreamComponents(new PaliTokenizer());
		}

		private class PaliTokenizer extends CharTokenizer {
			public PaliTokenizer() {
			}
			@Override
			protected boolean isTokenChar(final int c) {
				return notWord.indexOf(c) < 0;
			}
		}
	}

}
