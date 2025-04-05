/*
 * TermLister.java
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
import java.util.stream.*;
import java.util.regex.*;
import java.util.concurrent.CompletableFuture;
import java.sql.*;
import java.io.*;
import java.nio.file.*;
import java.text.Normalizer;
import java.text.Normalizer.Form;

import javafx.geometry.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.*;
import javafx.scene.layout.*;
import javafx.scene.input.*;
import javafx.beans.property.*;
import javafx.event.*;
import javafx.util.Callback;
import javafx.collections.ObservableList;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.application.Platform;
import javafx.stage.Stage;
import javafx.beans.property.SimpleBooleanProperty;

import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.LeafReader;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.index.FieldInfos;
import org.apache.lucene.index.FieldInfo;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.index.Terms;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.Directory;
import org.apache.lucene.util.BytesRef;

/** 
 * The window showing term lists of Pali collections.
 * @author J.R. Bhaddacak
 * @version 3.0
 * @since 2.0
 */
public class TermLister extends BorderPane {
	public static enum FilterMode { SIMPLE, WILDCARDS, REGEX, METER }
	private final int DEF_MAX_ROW = 500;
	private Stage theStage;
	private final BorderPane mainPane = new BorderPane();
	private final VBox contentBox = new VBox();
	private final CommonWorkingToolBar mainToolBar;
	private final AnchorPane statusPane = new AnchorPane();
	private final ToolBar buildToolBar = new ToolBar();
	private final VBox toolBarBox = new VBox();
	private final ChoiceBox<String> listerTableChoice = new ChoiceBox<>();
	private final ChoiceBox<String> indexChoice = new ChoiceBox<>();
	private final CheckBox cbIncludeNotes = new CheckBox("Notes");
	private final ToggleButton buildOptionsToggle = new ToggleButton("", new TextIcon("gears", TextIcon.IconSet.AWESOME));
	private final ChoiceBox<String> freqRangeChoice = new ChoiceBox<>();
	private final ChoiceBox<Integer> maxRowChoice = new ChoiceBox<>();
	private final SimpleBooleanProperty notesAvailable = new SimpleBooleanProperty(true);
	private final TableView<SimpleTermFreqProp> table = new TableView<>();
	private final ObservableList<SimpleTermFreqProp> shownResultList = FXCollections.<SimpleTermFreqProp>observableArrayList();
	private final PaliTextInput searchTextInput = new PaliTextInput(PaliTextInput.InputType.FIELD);
	private final TextField searchTextField;
	private final Map<Toggle, FilterMode> filterModeMap = new HashMap<>();
	private final ToggleGroup termFilterGroup = new ToggleGroup();
	private final RadioMenuItem filterSimpleMenuItem = new RadioMenuItem("Simple filter");
	private final ChoiceBox<Integer> firstCharGroupChoice = new ChoiceBox<>();
	private final ChoiceBox<Integer> lastCharGroupChoice = new ChoiceBox<>();
	private final Label fixedInfoLabel = new Label();
	private final HBox progressBox = new HBox(3);
	private final ProgressBar progressBar = new ProgressBar();
	private final Label progressMessage = new Label();
	private final InfoPopup mainHelpPopup = new InfoPopup();
	private final InfoPopup filterHelpPopup = new InfoPopup();
	private FilterMode currFilterMode = FilterMode.SIMPLE;
	private PaliTextInput.InputMethod savInputMethod = PaliTextInput.InputMethod.UNUSED_CHARS;
	private String currFreqRange = ">= 1";
	private int maxRowCount = DEF_MAX_ROW;
	private int totalTerms = 0;
	
	public TermLister() {
		// essential init
		LuceneUtilities.updateIndexList();
		// add toolbar on the top
		mainToolBar = new CommonWorkingToolBar(table);
		// config some buttons
		mainToolBar.saveTextButton.setOnAction(actionEvent -> saveCSV());		
		mainToolBar.copyButton.setOnAction(actionEvent -> copyCSV());		
		// add new components to main toolbar
		final Button refreshButton = new Button("", new TextIcon("repeat", TextIcon.IconSet.AWESOME));
		refreshButton.setTooltip(new Tooltip("Refresh"));
		refreshButton.setOnAction(actionEvent -> {
			updateListerTableChoice();
			updateIndexChoice();
		});
		final Button deleteButton = new Button("", new TextIcon("xmark", TextIcon.IconSet.AWESOME));
		deleteButton.disableProperty().bind(listerTableChoice.valueProperty().isNull());
		deleteButton.setTooltip(new Tooltip("Delete selected table"));
		deleteButton.setOnAction(actionEvent -> deleteListerTable());
		listerTableChoice.setTooltip(new Tooltip("Lister table selector"));
		listerTableChoice.setOnAction(actionEvent -> updateResult());
		buildOptionsToggle.disableProperty().bind(LuceneUtilities.listerDBLocked);
		buildOptionsToggle.setTooltip(new Tooltip("Listing options"));
		buildOptionsToggle.setOnAction(actionEvent -> initToolBars());
		final List<String> freqRangeList = Arrays.asList("= 1", ">= 1", "> 1", "2 - 10", "> 10", "> 100", "> 1000", "> 10000");
		freqRangeChoice.setTooltip(new Tooltip("Frequency range"));
		freqRangeChoice.getItems().addAll(freqRangeList);
		freqRangeChoice.getSelectionModel().select(1);
		freqRangeChoice.setOnAction(actionEvent -> {
			currFreqRange = freqRangeChoice.getSelectionModel().getSelectedItem();
			updateResult();
		});
		final List<Integer> maxList = Arrays.asList(50, 100, 500, 1000, 5000, 10000, 50000, 100000, 550000, 1000000);
		maxRowChoice.setTooltip(new Tooltip("Maximum rows"));
		maxRowChoice.getItems().addAll(maxList);
		maxRowChoice.getSelectionModel().select(2);
		maxRowChoice.setOnAction(actionEvent -> {
			maxRowCount = maxRowChoice.getSelectionModel().getSelectedItem();
			updateResult();
		});
		final Button mainHelpButton = new Button("", new TextIcon("circle-question", TextIcon.IconSet.AWESOME));
		mainHelpButton.setOnAction(actionEvent -> mainHelpPopup.showPopup(mainHelpButton, InfoPopup.Pos.BELOW_RIGHT, true));
		mainToolBar.getItems().addAll(new Separator(), refreshButton, listerTableChoice, deleteButton, buildOptionsToggle, 
								new Separator(), freqRangeChoice, maxRowChoice, mainHelpButton);
		// add hidden toolbar for building lists
		final Button genTableButton = new Button("Generate", new TextIcon("screwdriver-wrench", TextIcon.IconSet.AWESOME));
		genTableButton.disableProperty().bind(LuceneUtilities.listerDBLocked.or(indexChoice.valueProperty().isNull()));
		genTableButton.setTooltip(new Tooltip("Generate lister table"));
		genTableButton.setOnAction(actionEvent -> buildListerTable());
		cbIncludeNotes.setTooltip(new Tooltip("Including notes"));
		cbIncludeNotes.disableProperty().bind(notesAvailable.not());
		cbIncludeNotes.setSelected(false);
		final Label indexInfoLabel = new Label();
		indexInfoLabel.setPadding(new Insets(0, 0, 0, 10));
		indexInfoLabel.setStyle("-fx-font-size:85%;");
		indexChoice.setTooltip(new Tooltip("Index selector"));
		indexChoice.setOnAction(actionEvent -> {
			final String indName = indexChoice.getSelectionModel().getSelectedItem();
			final LuceneIndex index = indName == null ? null : LuceneUtilities.indexMap.get(indName);
			if (index == null) {
				indexInfoLabel.setText("");
			} else {
				indexInfoLabel.setText(index.getIndexInfo());
				final Corpus.Collection col = index.getCollection();
				notesAvailable.set(Corpus.hasFullStructure(col) || Corpus.hasOnlyBodyTextAndNotes(col));
			}
		});
		buildToolBar.getItems().addAll(genTableButton, new Label(" from index:"), indexChoice, cbIncludeNotes,
										new Separator(), indexInfoLabel);
		setTop(toolBarBox);

		// set main pane at the center
		// set up search toolbar
		final ToolBar searchToolBar = new ToolBar();
		searchTextField = (TextField)searchTextInput.getInput();
		searchTextField.setPromptText("Search for...");
		searchTextField.setPrefWidth(Utilities.getRelativeSize(23));
		searchTextField.textProperty().addListener((obs, oldValue, newValue) -> {
			CompletableFuture.runAsync(() -> listTerms(), Utilities.threadPool);
		});
		final MenuButton filterOptionMenu = new MenuButton("", new TextIcon("check-double", TextIcon.IconSet.AWESOME));		
		filterOptionMenu.setTooltip(new Tooltip("Options"));
		final RadioMenuItem filterWildcardsMenuItem = new RadioMenuItem("Using ? and *");
		final RadioMenuItem filterRegexMenuItem = new RadioMenuItem("Regular expression");
		final RadioMenuItem filterMeterMenuItem = new RadioMenuItem("Filter by meter");
		filterModeMap.put(filterSimpleMenuItem, FilterMode.SIMPLE);
		filterModeMap.put(filterWildcardsMenuItem, FilterMode.WILDCARDS);
		filterModeMap.put(filterRegexMenuItem, FilterMode.REGEX);
		filterModeMap.put(filterMeterMenuItem, FilterMode.METER);
		termFilterGroup.getToggles().addAll(filterSimpleMenuItem, filterWildcardsMenuItem, filterRegexMenuItem, filterMeterMenuItem);
		termFilterGroup.selectToggle(filterSimpleMenuItem);
		termFilterGroup.selectedToggleProperty().addListener((observable) -> {
			if (termFilterGroup.getSelectedToggle() != null) {
				final Toggle selected = (Toggle)termFilterGroup.getSelectedToggle();
				currFilterMode = filterModeMap.get(selected);
				if (currFilterMode == FilterMode.METER) {
					savInputMethod = searchTextInput.getInputMethod();
					searchTextInput.setInputMethod(PaliTextInput.InputMethod.METER_GROUP);
				} else {
					searchTextInput.setInputMethod(savInputMethod);
				}
				updateResult();
			}
		});
		filterOptionMenu.getItems().addAll(filterSimpleMenuItem, filterWildcardsMenuItem, filterRegexMenuItem, filterMeterMenuItem);
		final List<Integer> charGroupList = Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
		firstCharGroupChoice.setTooltip(new Tooltip("# of first characters grouping"));
		firstCharGroupChoice.getItems().addAll(charGroupList);
		firstCharGroupChoice.getSelectionModel().select(0);
		firstCharGroupChoice.setOnAction(actionEvent -> updateResult());
		lastCharGroupChoice.setTooltip(new Tooltip("# of last characters grouping"));
		lastCharGroupChoice.getItems().addAll(charGroupList);
		lastCharGroupChoice.getSelectionModel().select(0);
		lastCharGroupChoice.setOnAction(actionEvent -> updateResult());
		final Button zeroResetButton = new Button("", new TextIcon("0", TextIcon.IconSet.AWESOME));
		zeroResetButton.setTooltip(new Tooltip("Reset all to 0"));
		zeroResetButton.setOnAction(actionEvent -> {
			firstCharGroupChoice.getSelectionModel().select(0);
			lastCharGroupChoice.getSelectionModel().select(0);
		});
		final Button filterHelpButton = new Button("", new TextIcon("circle-question", TextIcon.IconSet.AWESOME));
		filterHelpButton.setOnAction(actionEvent -> filterHelpPopup.showPopup(filterHelpButton, InfoPopup.Pos.BELOW_RIGHT, true));
		searchToolBar.getItems().addAll(searchTextField, searchTextInput.getClearButton(),
									searchTextInput.getMethodButton(), filterOptionMenu,
									new Separator(), firstCharGroupChoice, new Label(Utilities.DASH_N), lastCharGroupChoice, zeroResetButton, 
									new Separator(), filterHelpButton);
		mainPane.setTop(searchToolBar);
		// set up table at the center
		final ContextMenu tablePopupMenu = new ContextMenu();
		final MenuItem copyTermMenuItem = new MenuItem("Copy the word");
		copyTermMenuItem.setOnAction(actionEvent -> copyTerm());
		final MenuItem sendToDictMenuItem = new MenuItem("Send to Dictionaries");
		sendToDictMenuItem.setOnAction(actionEvent -> sendTermToDict());
		final MenuItem sendToDocFinderMenuItem = new MenuItem("Send to Document Finder");
		sendToDocFinderMenuItem.setOnAction(actionEvent -> sendTermToDocFinder());
		final MenuItem sendToLuceneFinderMenuItem = new MenuItem("Send to Lucene Finder");
		sendToLuceneFinderMenuItem.setOnAction(actionEvent -> sendTermToLuceneFinder());
		tablePopupMenu.getItems().addAll(copyTermMenuItem, sendToDictMenuItem, sendToDocFinderMenuItem, sendToLuceneFinderMenuItem);
		table.setContextMenu(tablePopupMenu);
		table.setOnDragDetected(mouseEvent -> {
			final SimpleTermFreqProp selected = (SimpleTermFreqProp)table.getSelectionModel().getSelectedItem();
			if (selected != null && table.getSelectionModel().getSelectedIndex() >= 0) {
				final Dragboard db = table.startDragAndDrop(TransferMode.ANY);
				final ClipboardContent content = new ClipboardContent();
				final String term = selected.termProperty().get();
				content.putString(term);
				db.setContent(content);
				mouseEvent.consume();
			}
		});
		table.setOnMouseDragged(mouseEvent -> mouseEvent.setDragDetect(true));
		updateListerTableChoice();
		updateResult();
		VBox.setVgrow(table, Priority.ALWAYS);
		contentBox.getChildren().add(table);
		mainPane.setCenter(contentBox);
		setCenter(mainPane);

		// set status bar at the bottom
		AnchorPane.setBottomAnchor(fixedInfoLabel, 0.0);
		AnchorPane.setRightAnchor(fixedInfoLabel, 0.0);
		AnchorPane.setBottomAnchor(progressBox, 0.0);
		AnchorPane.setLeftAnchor(progressBox, 0.0);
		fixedInfoLabel.setStyle("-fx-font-family:'" + Utilities.FONTMONO +"';-fx-font-size:85%;");
		progressBox.setAlignment(Pos.BOTTOM_CENTER);
		progressBox.getChildren().addAll(progressBar, progressMessage);
		statusPane.getChildren().add(fixedInfoLabel);
		setBottom(statusPane);

		// set up drop action
		this.setOnDragOver(dragEvent -> {
			if (dragEvent.getGestureSource() != this && dragEvent.getDragboard().hasString()) {
				dragEvent.acceptTransferModes(TransferMode.COPY_OR_MOVE);
			}
			dragEvent.consume();
		});
		this.setOnDragDropped(dragEvent -> {
			final Dragboard db = dragEvent.getDragboard();
			if (db.hasString()) {
				final String[] allLines = db.getString().split("\\n");
				final String head = allLines[0].trim();
				searchTextField.setText(Utilities.getUsablePaliTerm(head));
				dragEvent.setDropCompleted(true);
			} else {
				dragEvent.setDropCompleted(false);
			}
			dragEvent.consume();
		});

		// some other initialization
		if (LuceneUtilities.simpleServiceMap == null) 
			LuceneUtilities.simpleServiceMap = LuceneUtilities.getSimpleServices();
		initToolBars();
		Utilities.createMeterPatternMap();
		mainHelpPopup.setContentWithText(LuceneUtilities.getTextResource("info-lister.txt"));
		mainHelpPopup.setTextWidth(Utilities.getRelativeSize(32));
		filterHelpPopup.setContentWithText(LuceneUtilities.getTextResource("info-lister-filter.txt"));
		filterHelpPopup.setTextWidth(Utilities.getRelativeSize(42));
	}

	public void init() {
		cbIncludeNotes.setSelected(false);
		buildOptionsToggle.setSelected(false);
		initToolBars();
		currFreqRange = ">= 1";
		maxRowCount = DEF_MAX_ROW;
		freqRangeChoice.getSelectionModel().select(1);
		maxRowChoice.getSelectionModel().select(2);
		firstCharGroupChoice.getSelectionModel().select(0);
		lastCharGroupChoice.getSelectionModel().select(0);
		searchTextField.clear();
		termFilterGroup.selectToggle(filterSimpleMenuItem);
		updateListerTableChoice();
		updateResult();
	}

	private void initToolBars() {
		toolBarBox.getChildren().clear();
		if (buildOptionsToggle.isSelected())
			toolBarBox.getChildren().addAll(mainToolBar, buildToolBar);
		else
			toolBarBox.getChildren().addAll(mainToolBar);
		updateIndexChoice();
	}

	public void setStage(final Stage stage) {
		theStage = stage;
	}

	private void fillListerTableChoice() {
		listerTableChoice.getItems().clear();
		final List<String> lsTableList = LuceneUtilities.getListerTableNameList();
		if (!lsTableList.isEmpty())
			listerTableChoice.getItems().addAll(lsTableList);
	}

	private void updateListerTableChoice() {
		fillListerTableChoice();
		listerTableChoice.getSelectionModel().select(0);
	}

	private void updateListerTableChoice(final String tabName) {
		fillListerTableChoice();
		listerTableChoice.getSelectionModel().select(tabName);
	}

	private void updateIndexChoice() {
		LuceneUtilities.updateIndexList();
		final List<String> indList = LuceneUtilities.getIndexNameList();
		indexChoice.getItems().clear();
		if (!indList.isEmpty())
			indexChoice.getItems().addAll(indList);
		indexChoice.getSelectionModel().select(0);
	}

	private void buildListerTable() {
		final String indName = indexChoice.getSelectionModel().getSelectedItem();
		if (indName == null || indName.isEmpty()) return;
		final LuceneIndex index = LuceneUtilities.indexMap.get(indName);
		final String tabName = ListerTable.modName(index.getListerTableName(), cbIncludeNotes.isSelected());
		final boolean proceed = ListerTable.exists(tabName) ? proceedBuildConfirm() : true;
		if (!proceed) return;
		progressBar.setProgress(0);
		final Task<Boolean> buildTask = new Task<Boolean>() {
			@Override
			protected Boolean call() throws Exception {
				try {
					final Directory directory = FSDirectory.open(Path.of(index.getIndexPath()));
					final DirectoryReader iReader = DirectoryReader.open(directory);
					if (iReader != null) {
						// get leaf readers
						final Map<String, TermInfo> termInfoMap = new HashMap<>();
						final List<LeafReaderContext> leafReaders = iReader.leaves();
						updateMessage("Processing term-frequency");
						final int lrTotal = leafReaders.size();
						for (int r = 0; r < lrTotal; r++) {
							updateProgress(r + 1, lrTotal);
							final LeafReader lReader = leafReaders.get(r).reader();
							// read all fields
							final FieldInfos finfos = lReader.getFieldInfos();
							final Set<String> fieldSet = new HashSet<>();
							for (final Iterator<FieldInfo> it = finfos.iterator(); it.hasNext();) {
								final String fname = it.next().name;
								// exluding PATH, BOLD, and NOTE (if unselected)
								if (fname.equals(LuceneFinder.FIELD_PATH)) continue;
								if (fname.equals(TermInfo.Field.BOLD.getTag())) continue;
								if (!cbIncludeNotes.isSelected() && fname.equals(TermInfo.Field.NOTE.getTag())) continue;
								fieldSet.add(fname);
							}
							// read all terms for each field
							int n = 0;
							BytesRef t;
							for (final String fld : fieldSet) {
								long tf = 0;
								final Terms terms = lReader.terms(fld);
								if (terms == null) continue;
								for (final TermsEnum termsEnum = terms.iterator(); (t = termsEnum.next()) != null; ) {
									final String term = t.utf8ToString();
									final TermInfo tinfo = termInfoMap.getOrDefault(term, new TermInfo(term));
									final int freq = (int)termsEnum.totalTermFreq();
									tinfo.addUpTotalFreq(freq);
									if (TermInfo.Field.isGatha(fld))
										tinfo.addUpGathaFreq(freq);
									termInfoMap.put(term, tinfo);
								}
							}
						}
						iReader.close();
						final java.sql.Connection conn = Utilities.H2DB.LISTER.getConnection();
						if (conn != null) {
							// remove numbers and sort by frequency desc first
							final List<TermInfo> sorted = termInfoMap.values().stream()
														.filter(x -> !x.getTerm().matches("^\\d+\\S*"))
														.sorted((a, b) -> Integer.compare(b.getTotalFreq(), a.getTotalFreq()))
														.collect(Collectors.toList());
							final String delete = "DROP TABLE IF EXISTS " + tabName + ";";
							Utilities.executeSQL(conn, delete);
							final String create = "CREATE TABLE " + tabName + "(" +
								"TERM VARCHAR(255) PRIMARY KEY, " +
								"TOTFREQ INT, " +
								"GATFREQ INT);";
							Utilities.executeSQL(conn, create);
							updateMessage("Creating table... (please wait)");
							final String insert = "INSERT INTO " + tabName + " VALUES(?, ?, ?);";
							final PreparedStatement pstm = conn.prepareStatement(insert);
							final int total = sorted.size();
							TermInfo tinfo;
							for (int i = 0; i < total; i++) {
								tinfo = sorted.get(i);
								pstm.setString(1, tinfo.getTerm());
								pstm.setInt(2, tinfo.getTotalFreq());
								pstm.setInt(3, tinfo.getGathaFreq());
								pstm.executeUpdate();
								updateProgress(i, total);
							}
							pstm.close();
							final String index = "CREATE INDEX IDX_" + tabName + " ON " + tabName + "(TERM);";
							Utilities.executeSQL(conn, index);
						}
					}
					directory.close();
				} catch (IOException | SQLException e) {
					System.err.println(e);
				}
				Platform.runLater(() -> {
					progressBar.progressProperty().unbind();
					statusPane.getChildren().remove(progressBox);
					updateListerTableChoice(tabName);
					updateResult();
				});
				return true;
			}
		};
		progressBar.progressProperty().bind(buildTask.progressProperty());
		buildTask.messageProperty().addListener((observable, oldValue, newValue) -> {
			progressMessage.setText(newValue);
		});
		Utilities.threadPool.submit(buildTask);
		statusPane.getChildren().add(progressBox);
	}

	private void deleteListerTable() {
		final String tabName = listerTableChoice.getSelectionModel().getSelectedItem();
		if (tabName == null || tabName.isEmpty()) return;
		final ConfirmAlert delAlert = new ConfirmAlert(theStage, ConfirmAlert.ConfirmType.DELETE);
		delAlert.setMessage("A lister table (" + tabName + ") will be deleted.\nAre you sure to do this?");
		final Optional<ButtonType> response = delAlert.showAndWait();
		if (response.isPresent()) {
			if (response.get() == delAlert.getConfirmButtonType()) {
				final ListerTable lsTable = LuceneUtilities.listerTableMap.get(tabName);
				if (lsTable != null) {
					lsTable.deleteTable();
					updateListerTableChoice();
					updateResult();
				}
			}
		}
	}
	
	private boolean proceedBuildConfirm() {
		boolean output = false;
		final String message = "The existing table will be replaced, \nproceed to continue.";
		final ConfirmAlert proceedAlert = new ConfirmAlert(theStage, ConfirmAlert.ConfirmType.PROCEED, message);
		final Optional<ButtonType> result = proceedAlert.showAndWait();
		if (result.isPresent()) {
			if (result.get() == proceedAlert.getConfirmButtonType())
				output = true;
		}
		return output;		
	}

    private void listTerms() {
		Platform.runLater(() -> updateResult());
	}

	private void updateResult() {
		shownResultList.clear();
		final String tabName = listerTableChoice.getSelectionModel().getSelectedItem();
		if (tabName == null || tabName.isEmpty()) {
			shownResultList.add(new SimpleTermFreqProp("", 0, 0));
			fixedInfoLabel.setText("");
		} else {
			String searchText = Normalizer.normalize(searchTextField.getText().trim(), Form.NFC);
			searchText = searchText.replace("'", "");
			final Corpus.Collection col = Corpus.Collection.valueOf(tabName.substring(0, tabName.indexOf("_")).toUpperCase());
			if (col == Corpus.Collection.SC)
				searchText = Utilities.changeToScNiggahita(searchText);
			final List<SimpleTermFreqProp> result = currFilterMode == FilterMode.METER
													? filterByMeter(getTermListFromDB(tabName), searchText)
													: getTermListFromDB(tabName, searchText);
			if (result == null || result.isEmpty())
				shownResultList.add(new SimpleTermFreqProp("", 0, 0));
			else
				shownResultList.addAll(filterByCharGroup(result));
			updateFixedInfo(tabName);
		}
		if (shownResultList.isEmpty())
			shownResultList.add(new SimpleTermFreqProp("", 0, 0));
		table.setItems(shownResultList);
		setupTable(tabName);
	}

	private void updateFixedInfo(final String tabName) {
		final String tinfo = ListerTable.getTableInfo(tabName);
		final String info;
		final int resultSize = shownResultList.size();
		final int shownTerms = resultSize == 1
								? shownResultList.get(0).isZeroFreq()
									? 0
									: 1
								: resultSize;
		info = String.format("%s | Terms: %,9d of %,9d",
				tinfo, shownTerms, totalTerms);
		fixedInfoLabel.setText(info);
	}

	private List<SimpleTermFreqProp> filterByCharGroup(final Collection<SimpleTermFreqProp> terms) {
		final int nFirst = firstCharGroupChoice.getSelectionModel().getSelectedItem();
		final int nLast = lastCharGroupChoice.getSelectionModel().getSelectedItem();
		final List<SimpleTermFreqProp> result = new ArrayList<>();
		if (nFirst > 0) {
			final Map<String, List<SimpleTermFreqProp>> headingMap = terms.stream()
																	.filter(x -> x.termProperty().get().length() >= nFirst)
																	.collect(Collectors.groupingBy(x -> {
																		final String term = x.termProperty().get();
																		return term.substring(0, nFirst);
																	}));
			if (nLast > 0) {
				headingMap.forEach((tf, lf) -> {
					final Map<String, List<SimpleTermFreqProp>> endingMap = lf.stream()
																			.filter(x -> x.termProperty().get().length() >= nLast)
																			.collect(Collectors.groupingBy(x -> {
																				final String term = x.termProperty().get();
																				final int len = term.length();
																				return term.substring(len - nLast, len);
																			}));
					endingMap.forEach((tl, ll) -> {
						final long totFreq = ll.stream().map(x -> x.totFreqProperty().get()).reduce(0, Integer::sum);
						final long gatFreq = ll.stream().map(x -> x.gatFreqProperty().get()).reduce(0, Integer::sum);
						final SimpleTermFreqProp tfp = new SimpleTermFreqProp(tf + "-" + tl, (int)totFreq, (int)gatFreq);
						result.add(tfp);
					});
				});
			} else {
				headingMap.forEach((t, lst) -> {
					final long totFreq = lst.stream().map(x -> x.totFreqProperty().get()).reduce(0, Integer::sum);
					final long gatFreq = lst.stream().map(x -> x.gatFreqProperty().get()).reduce(0, Integer::sum);
					final SimpleTermFreqProp tfp = new SimpleTermFreqProp(t + "-", (int)totFreq, (int)gatFreq);
					result.add(tfp);
				});
			}
		} else {
			if (nLast > 0) {
				final Map<String, List<SimpleTermFreqProp>> endingMap = terms.stream()
																		.filter(x -> x.termProperty().get().length() >= nLast)
																		.collect(Collectors.groupingBy(x -> {
																			final String term = x.termProperty().get();
																			final int len = term.length();
																			return term.substring(len - nLast, len);
																		}));
				endingMap.forEach((t, lst) -> {
					final long totFreq = lst.stream().map(x -> x.totFreqProperty().get()).reduce(0, Integer::sum);
					final long gatFreq = lst.stream().map(x -> x.gatFreqProperty().get()).reduce(0, Integer::sum);
					final SimpleTermFreqProp tfp = new SimpleTermFreqProp("-" + t, (int)totFreq, (int)gatFreq);
					result.add(tfp);
				});
			} else {
				result.addAll(terms);
				return result;
			}
		}
		result.sort((x, y) -> Integer.compare(y.totFreqProperty().get(), x.totFreqProperty().get()));
		return result;
	}

	private List<SimpleTermFreqProp> filterByMeter(final Collection<SimpleTermFreqProp> terms, final String strInput) {
		final String inputPatt = Utilities.changeToLahuGaru(strInput);
		final List<SimpleTermFreqProp> result = inputPatt.length() == 0
												? new ArrayList<>(terms)
												: terms.stream()
													.filter(x -> Utilities.computeMeter(x.termProperty().get()).matches(inputPatt))
													.collect(Collectors.toList());
		return result;
	}

	private Callback<TableColumn<SimpleTermFreqProp, Integer>, TableCell<SimpleTermFreqProp, Integer>> getIntegerCellFactory() {
		return col -> {
			TableCell<SimpleTermFreqProp, Integer> cell = new TableCell<SimpleTermFreqProp, Integer>() {
				@Override
				public void updateItem(final Integer item, final boolean empty) {
					super.updateItem(item, empty);
					this.setText(null);
					this.setGraphic(null);
					if (!empty) {
						if (item > 0)
							this.setText(String.format("%,d", item));
					}
				}
			};
			return cell;
		};
	}

	private void setupTable(final String tabName) {
		final ListerTable lsTable = LuceneUtilities.listerTableMap.get(tabName);
		if (lsTable == null) return;
		final LuceneIndex index = lsTable.getIndex();
		if (index == null) return;
		final Corpus.Collection col = index.getCollection();
		final boolean showGathaFreq = Corpus.hasAlmostFullButNotes(col) || Corpus.hasFullStructure(col);
		final double totalCW = showGathaFreq ? 11 : 9;
		final double termCW = showGathaFreq ? 7 : 6.5;
		table.getColumns().clear();
		final TableColumn<SimpleTermFreqProp, String> termCol = new TableColumn<>("Term");
		termCol.setCellValueFactory(new PropertyValueFactory<>(shownResultList.get(0).termProperty().getName()));
		termCol.setComparator(Utilities.paliComparator);
		termCol.prefWidthProperty().bind(mainPane.widthProperty().divide(totalCW).multiply(termCW).subtract(Utilities.getRelativeSize(2)));
		table.getColumns().add(termCol);
		final TableColumn<SimpleTermFreqProp, Integer> totFreqCol = new TableColumn<>("Total Freq");
		totFreqCol.setCellValueFactory(new PropertyValueFactory<>(shownResultList.get(0).totFreqProperty().getName()));
		totFreqCol.prefWidthProperty().bind(mainPane.widthProperty().divide(totalCW).multiply(1.5));
		totFreqCol.setStyle("-fx-alignment:center-right");
		totFreqCol.setCellFactory(getIntegerCellFactory());
		table.getColumns().add(totFreqCol);
		if (showGathaFreq) {
			final TableColumn<SimpleTermFreqProp, Integer> gatFreqCol = new TableColumn<>("Gāthā Freq");
			gatFreqCol.setCellValueFactory(new PropertyValueFactory<>(shownResultList.get(0).gatFreqProperty().getName()));
			gatFreqCol.prefWidthProperty().bind(mainPane.widthProperty().divide(totalCW).multiply(1.5));
			gatFreqCol.setStyle("-fx-alignment:center-right");
			gatFreqCol.setCellFactory(getIntegerCellFactory());
			table.getColumns().add(gatFreqCol);
		}
		final TableColumn<SimpleTermFreqProp, Integer> lengthCol = new TableColumn<>("Length");
		lengthCol.setCellValueFactory(new PropertyValueFactory<>(shownResultList.get(0).lengthProperty().getName()));
		lengthCol.prefWidthProperty().bind(mainPane.widthProperty().divide(totalCW));
		lengthCol.setStyle("-fx-alignment:center-right");
		lengthCol.setCellFactory(getIntegerCellFactory());
		table.getColumns().add(lengthCol);
	}

	private List<SimpleTermFreqProp> getTermListFromDB(final String tabName) {
		return getTermListFromDB(tabName, "");
	}

	private List<SimpleTermFreqProp> getTermListFromDB(final String tabName, final String text) {
		final List<SimpleTermFreqProp> result = new ArrayList<>();
		final String tCondition;
		if (text.isEmpty()) {
			tCondition = "";
		} else {
			if (currFilterMode == FilterMode.WILDCARDS) {
				final String query = text.replace("*", "%").replace("?", "_");
				tCondition = " AND TERM LIKE '" + query + "' ";
			} else if (currFilterMode == FilterMode.REGEX) {
				try {
					Pattern.compile(text);
				} catch (PatternSyntaxException e) {
					return Collections.emptyList();
				}
				tCondition = " AND TERM REGEXP '" + text + "' ";
			} else if (currFilterMode == FilterMode.SIMPLE) {
				tCondition = " AND TERM LIKE '" + text + "%'";
			} else {
				tCondition = "";
			}
		}
		final String fCondition = "TOTFREQ" + (currFreqRange.equals("2 - 10") ? " >= 2 AND TOTFREQ <= 10 " : currFreqRange);
		final String orderBy = currFreqRange.equals("= 1") ? " ORDER BY TERM " : "";
		final String query = "SELECT TERM,TOTFREQ,GATFREQ FROM " + tabName +
							" WHERE " + fCondition + tCondition + orderBy +
							" LIMIT " + maxRowCount + ";";
		final String totQuery = "SELECT COUNT(*) FROM " + tabName + " WHERE " + fCondition + ";";
		if (!query.isEmpty()) {
			final java.sql.Connection conn = Utilities.H2DB.LISTER.getConnection();
			try {
				if (conn != null) {
					final Statement stmt = conn.createStatement();
					final ResultSet rsData = stmt.executeQuery(query);
					while (rsData.next())
						result.add(new SimpleTermFreqProp(rsData.getString(1), rsData.getInt(2), rsData.getInt(3)));
					final ResultSet rsTot = stmt.executeQuery(totQuery);
					if (rsTot.next())
						totalTerms = rsTot.getInt(1);
					rsData.close();
					rsTot.close();
					stmt.close();
				}
			} catch (SQLException e) {
				System.err.println(e);
			}
		} else {
			totalTerms = 0;
		}
		return result;
	}

	private void copyTerm() {
		final SimpleTermFreqProp tf = table.getSelectionModel().getSelectedItem();
		final String term = tf.termProperty().get();
		if (!term.isEmpty()) {
			Utilities.copyText(term);
		}
	}

	private void sendTermToDict() {
		final SimpleTermFreqProp tf = table.getSelectionModel().getSelectedItem();
		final String term = tf.termProperty().get();
		if (!term.isEmpty()) {
			final SimpleService dictSearch = (SimpleService)LuceneUtilities.simpleServiceMap.get("paliplatform.main.DictSearch");
			if (dictSearch != null) {
				dictSearch.process(term);
			}
		}
	}

	private void sendTermToDocFinder() {
		final SimpleTermFreqProp tf = table.getSelectionModel().getSelectedItem();
		final String term = tf.termProperty().get();
		if (!term.isEmpty()) {
			final SimpleService docFinderSearch = (SimpleService)LuceneUtilities.simpleServiceMap.get("paliplatform.main.DocFinderSearch");
			if (docFinderSearch != null) {
				docFinderSearch.process(term);
			}
		}
	}

	private void sendTermToLuceneFinder() {
		final SimpleTermFreqProp tf = table.getSelectionModel().getSelectedItem();
		final String term = tf.termProperty().get();
		if (!term.isEmpty()) {
			final SimpleService luceneFinderSearch = (SimpleService)LuceneUtilities.simpleServiceMap.get("paliplatform.main.LuceneFinderSearch");
			if (luceneFinderSearch != null) {
				luceneFinderSearch.process(term);
			}
		}
	}

	private List<String[]> makeCSV() {
		final List<String[]> result = new ArrayList<>();
		// table columns
		final int colCount = table.getColumns().size();
		final String[] heads = new String[colCount];
		for (int i = 0; i < colCount; i++) {
			heads[i] = table.getColumns().get(i).getText();
		}
		result.add(heads);
		// table data
		for (int i = 0; i < table.getItems().size(); i++) {
			final SimpleTermFreqProp tf = table.getItems().get(i);
			final String[] data = new String[colCount];
			data[0] = tf.termProperty().get();
			data[1] = "" + tf.totFreqProperty().get();
			data[2] = "" + tf.gatFreqProperty().get();
			data[3] = "" + tf.lengthProperty().get();
			result.add(data);
		}
		return result;
	}
	
	private void copyCSV() {
		Utilities.copyCSV(makeCSV());
	}
	
	private void saveCSV() {
		Utilities.saveCSV(makeCSV(), "termlist.csv");
	}

	// inner classes
	public final class SimpleTermFreqProp {
		private StringProperty term;
		private IntegerProperty totFreq;
		private IntegerProperty gatFreq;
		private IntegerProperty length;

		public SimpleTermFreqProp(final String t, final int tf, final int gf) {
			termProperty().set(t);
			totFreqProperty().set(tf);
			gatFreqProperty().set(gf);
			final int len = t.contains("-") ? 0 : Utilities.getPaliWordLength(t);
			lengthProperty().set(len);
		}

		public boolean isZeroFreq() {
			return totFreqProperty().get() == 0;
		}

		public StringProperty termProperty() {
			if (term == null)
				term = new SimpleStringProperty(this, "term");
			return term;
		}

		public IntegerProperty totFreqProperty() {
			if (totFreq == null)
				totFreq = new SimpleIntegerProperty(this, "totFreq");
			return totFreq;
		}

		public IntegerProperty gatFreqProperty() {
			if (gatFreq == null)
				gatFreq = new SimpleIntegerProperty(this, "gatFreq");
			return gatFreq;
		}

		public IntegerProperty lengthProperty() {
			if (length == null)
				length = new SimpleIntegerProperty(this, "length");
			return length;
		}
	}

}
