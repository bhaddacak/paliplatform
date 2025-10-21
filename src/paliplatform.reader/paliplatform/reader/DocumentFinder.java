/*
 * DocumentFinder.java
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

package paliplatform.reader;

import paliplatform.base.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.*;
import java.util.zip.*;
import java.util.regex.*;
import java.text.Normalizer;
import java.text.Normalizer.Form;

import javafx.geometry.*;
import javafx.collections.*;
import javafx.scene.input.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.*;
import javafx.scene.layout.*;
import javafx.util.Callback;
import javafx.beans.property.*;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.application.Platform;

/** 
 * The tool for listing, finding and opening a specific Pali document
 * from text collections.
 * @author J.R. Bhaddacak
 * @version 3.0
 * @since 2.0
 */
public class DocumentFinder extends BorderPane {
	private static enum SearchField { INFO, REF, CONTENT }
	private final BorderPane mainPane = new BorderPane();
	private final PaliTextInput textInput = new PaliTextInput(PaliTextInput.InputType.COMBO);
	private final ObservableList<DocumentInfo> resultList = FXCollections.<DocumentInfo>observableArrayList();
	private final TableView<DocumentInfo> table = new TableView<>();
	private final RadioButton inInfoRadio = new RadioButton("Doc Info");
	private final ToggleGroup searchFieldGroup = new ToggleGroup();
	private final CorpusSelectorBox corpusSelector;
	private final ToggleButton starButton = new ToggleButton("", new TextIcon("asterisk", TextIcon.IconSet.AWESOME));
	private final HBox statusBox = new HBox(3);
	private final Label statusMessage = new Label();
	private final HBox progressBox = new HBox(3);
	private final ProgressBar progressBar = new ProgressBar();
	private final Label progressMessage = new Label();
	private final InfoPopup helpPopup = new InfoPopup();
	private final TextField searchTextField;
	private final ComboBox<String> searchComboBox;
	private SearchField searchIn = SearchField.INFO;
	private Task<Boolean> searchTask = null;
	
	public DocumentFinder() {
		// get the default corpus, suppose there is at least one
		// (if the corpus list is empty DocumentFinder should be prevented from opening)
		corpusSelector = new CorpusSelectorBox(ReaderUtilities.corpusMap.get(Corpus.Collection.CSTR), () -> search());

		resultList.add(new DummyDocumentInfo());
		table.setItems(resultList);
		table.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		table.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> {
			final DocumentInfo selItem = newValue;
			if (selItem != null) {
				// show content search result, if any
				final Set<String> findRes = new LinkedHashSet<>(selItem.getMatchResult());
				if (!findRes.isEmpty()) {
					final String res = findRes.stream().collect(Collectors.joining("; "));
					statusMessage.setText(res);
				}
			}
		});
		setupTable();
		// add context menu
		final ContextMenu popupMenu = new ContextMenu();
		final MenuItem openMenuItem = new MenuItem("Open");
		openMenuItem.setOnAction(actionEvent -> openDoc());		
		popupMenu.getItems().addAll(openMenuItem);
		table.setContextMenu(popupMenu);
		table.setOnKeyPressed(keyEvent -> {
			if (keyEvent.getEventType() == KeyEvent.KEY_PRESSED) {
				if (keyEvent.getCode() == KeyCode.ENTER)
					openDoc();
			}
		});
		mainPane.setCenter(table);
		setCenter(mainPane);
		
		// add options on the top
		final CommonWorkingToolBar toolBar = new CommonWorkingToolBar(table);
		// use property to bind with disablility of some buttons
		final SimpleListProperty<DocumentInfo> resultListProperty = new SimpleListProperty<>(resultList);
		// configure some buttons first
		toolBar.saveTextButton.setTooltip(new Tooltip("Save data as CSV"));
		toolBar.saveTextButton.setOnAction(actionEvent -> saveCSV());		
		toolBar.saveTextButton.disableProperty().bind(resultListProperty.sizeProperty().isEqualTo(0));
		toolBar.copyButton.setTooltip(new Tooltip("Copy CSV to clipboard"));
		toolBar.copyButton.setOnAction(actionEvent -> copyCSV());		
		toolBar.copyButton.disableProperty().bind(resultListProperty.sizeProperty().isEqualTo(0));
		// add new buttons
		final Button openButton = new Button("", new TextIcon("file-lines", TextIcon.IconSet.AWESOME));
		openButton.setTooltip(new Tooltip("Open the selected file"));
		openButton.setOnAction(actionEvent -> openDoc());
		openButton.disableProperty().bind(table.getSelectionModel().selectedItemProperty().isNull());
		starButton.setTooltip(new Tooltip("Search in all collections"));
		starButton.setOnAction(actionEvent -> search());
		corpusSelector.disableProperty().bind(starButton.selectedProperty());
		toolBar.getItems().addAll(new Separator(), openButton, new Separator(), starButton, corpusSelector);
		// help button
		final Button helpButton = new Button("", new TextIcon("circle-question", TextIcon.IconSet.AWESOME));
		helpButton.setOnAction(actionEvent -> helpPopup.showPopup(helpButton, InfoPopup.Pos.BELOW_RIGHT, true));
		toolBar.getItems().addAll(new Separator(), helpButton);
		// add second toolbar
		final ToolBar secondToolBar = new ToolBar();
		searchComboBox = textInput.getComboBox();
		searchComboBox.setPromptText("Search for...");
		searchComboBox.setPrefWidth(Utilities.getRelativeSize(22));
		searchComboBox.setOnShowing(e -> recordQuery());
		searchTextField = (TextField)textInput.getInput();
		searchTextField.textProperty().addListener((obs, oldValue, newValue) -> {
			if (searchIn != SearchField.CONTENT) {
				final String strQuery = Normalizer.normalize(newValue.trim(), Form.NFC);
				if (isValidQuery(strQuery))
					search(strQuery);
				else
					clearResult();
				searchComboBox.commitValue();
			}
		});
		final Button clearButton = textInput.getClearButton();
		clearButton.setOnAction(actionEvent -> {
			recordQuery();
			searchTextField.clear();
			searchComboBox.commitValue();
		});
		searchComboBox.setOnKeyPressed(keyEvent -> {
			if (keyEvent.getEventType() == KeyEvent.KEY_PRESSED) {
				if (keyEvent.getCode() == KeyCode.ENTER) {
					search();
				} else if (keyEvent.getCode() == KeyCode.ESCAPE) {
					searchTextField.clear();
				}
			}
		});
		final ToggleGroup searchFieldGroup = new ToggleGroup();
		final RadioButton inRefRadio = new RadioButton("Ref");
		final RadioButton inContentRadio = new RadioButton("Content");
		searchFieldGroup.getToggles().addAll(inInfoRadio, inRefRadio, inContentRadio);
		inInfoRadio.setSelected(true);
		inInfoRadio.setOnAction(actionEvent -> search(SearchField.INFO));
		inRefRadio.setOnAction(actionEvent -> search(SearchField.REF));
		inContentRadio.setOnAction(actionEvent -> search(SearchField.CONTENT));
		secondToolBar.getItems().addAll(new Separator(), searchComboBox, clearButton, textInput.getMethodButton(),
							new Separator(), new Label("Find in: "), inInfoRadio, inRefRadio, inContentRadio);
		final VBox toolBox = new VBox();
		toolBox.getChildren().addAll(toolBar, secondToolBar);
		setTop(toolBox);
		
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
				addTermToSearch(head);	
				dragEvent.setDropCompleted(true);
			} else {
				dragEvent.setDropCompleted(false);
			}
			dragEvent.consume();
		});
	
		// set up status bar
		statusBox.getChildren().addAll(statusMessage);
		statusBox.setPadding(new Insets(2, 2, 0, 2));
		setBottom(statusBox);

		// some init
		if (ReaderUtilities.simpleServiceMap == null) 
			ReaderUtilities.simpleServiceMap = ReaderUtilities.getSimpleServices();
		if (ReaderUtilities.referenceList == null)
			ReaderUtilities.readReferenceList();
		helpPopup.setContentWithText(ReaderUtilities.getTextResource("info-docfinder.txt"));
		helpPopup.setTextWidth(Utilities.getRelativeSize(38));
		progressBox.getChildren().addAll(progressBar, progressMessage);
		progressBox.setPadding(new Insets(2, 2, 0, 2));
		setPrefWidth(Utilities.getRelativeSize(67));
		search();
	}

	public void init() {
		clearResult();
		searchTextField.clear();
		searchFieldGroup.selectToggle(inInfoRadio);
		searchIn = SearchField.INFO;
		search();
	}

	private void setupTable() {
		if (resultList.isEmpty())
			return;
		final TableColumn<DocumentInfo, String> corpusCol = new TableColumn<>("Col");
		corpusCol.setCellValueFactory(new PropertyValueFactory<>(resultList.get(0).corpusProperty().getName()));
		corpusCol.setReorderable(false);
		corpusCol.setComparator(Corpus.Collection.colComparator);
		corpusCol.prefWidthProperty().bind(mainPane.widthProperty().divide(15));
		final TableColumn<DocumentInfo, String> summaryCol = new TableColumn<>("Document Information");
		summaryCol.setCellValueFactory(new PropertyValueFactory<>(resultList.get(0).summaryProperty().getName()));
		summaryCol.setReorderable(false);
		summaryCol.setComparator(Utilities.paliComparator);
		summaryCol.prefWidthProperty().bind(mainPane.widthProperty().divide(15).multiply(8.5).subtract(20));
		final TableColumn<DocumentInfo, String> refCol = new TableColumn<>("Ref");
		refCol.setCellValueFactory(new PropertyValueFactory<>(resultList.get(0).refProperty().getName()));
		refCol.setReorderable(false);
		refCol.setComparator(Utilities.alphanumComparator);
		refCol.prefWidthProperty().bind(mainPane.widthProperty().divide(15).multiply(2.5));
		final TableColumn<DocumentInfo, String> fileNameCol = new TableColumn<>("File");
		fileNameCol.setCellValueFactory(new PropertyValueFactory<>(resultList.get(0).fileNameProperty().getName()));
		fileNameCol.setReorderable(false);
		fileNameCol.prefWidthProperty().bind(mainPane.widthProperty().divide(15).multiply(2));
		final TableColumn<DocumentInfo, Integer> searchResultCol = new TableColumn<>("#");
		searchResultCol.setCellValueFactory(new PropertyValueFactory<>(resultList.get(0).searchResultCountProperty().getName()));
		searchResultCol.setReorderable(false);
		searchResultCol.prefWidthProperty().bind(mainPane.widthProperty().divide(15));
		searchResultCol.setStyle("-fx-alignment:center-right");
		searchResultCol.setCellFactory(col -> {
			TableCell<DocumentInfo, Integer> cell = new TableCell<DocumentInfo, Integer>() {
				@Override
				public void updateItem(final Integer item, final boolean empty) {
					super.updateItem(item, empty);
					this.setText(null);
					this.setGraphic(null);
					if (!empty && item > 0) {
						this.setText(String.format("%,d", item));
					}
				}
			};
			return cell;
		});
		table.getColumns().add(corpusCol);
		table.getColumns().add(summaryCol);
		table.getColumns().add(refCol);
		table.getColumns().add(fileNameCol);
		table.getColumns().add(searchResultCol);
	}

	private void clearResult() {
		resultList.clear();
		updateStatus();
	}

	private void addTermToSearch(final String term) {
		searchTextField.setText(term);
	}

	private boolean isValidQuery(final String text) {
		boolean result = true;
		final int threshold;
		if (searchIn == SearchField.CONTENT) {
			threshold = 3;
		} else {
			if (text.startsWith("*"))
				threshold = 2;
			else
				threshold = 0;
		}
		result = text.length() >= threshold;
		return result;
	}
	
	public void setSearchInput(final String text) {
		searchTextField.setText(text);
	}

	private Corpus[] getSelectedCorpora() {
		final Corpus[] result;
		if (starButton.isSelected()) {
			result = ReaderUtilities.corpusMap.values().stream()
						.filter(x -> ReaderUtilities.corpusAbbrList.contains(x.getCollection().toString()))
						.toArray(Corpus[]::new);
		} else {
			result = new Corpus[] { corpusSelector.getSelectedCorpus() };
		}
		return result;
	}

	private void search() {
		final String text = Normalizer.normalize(searchTextField.getText().trim(), Form.NFC);
		if (isValidQuery(text))
			search(text);
		else
			clearResult();
	}

	private void search(final SearchField field) {
		searchIn = field;
		final String text = Normalizer.normalize(searchTextField.getText().trim(), Form.NFC);
		if (isValidQuery(text))
			search(text);
		else
			clearResult();
	}
	
	private void search(final String inputStr) {
		clearResult();
		final Corpus[] selectedCorp = getSelectedCorpora();
		final String strToFind = inputStr.toLowerCase();
		if (searchIn == SearchField.INFO) {
			// search in document's information
			final TextGroup selTextGroup = starButton.isSelected() ? Corpus.tgAll : corpusSelector.getSelectedTextGroup();
			final List<DocumentInfo> docList = new ArrayList<>();
			for (final Corpus cp : selectedCorp) {
				docList.clear();
				for (final DocumentInfo docInfo : cp.getDocInfoMap().values()) {
					final boolean cond = strToFind.isEmpty()
										? true
										: cp.getCollection() == Corpus.Collection.SC
											? docInfo.containsInfo(Utilities.changeToScNiggahita(strToFind))
											: docInfo.containsInfo(strToFind);
					if (cond && docInfo.isInTextGroup(selTextGroup)){
						docList.add(docInfo);
					}
				}
				if (cp.getCollection() == Corpus.Collection.SC)
					docList.sort((a, b) -> ScInfo.scComparator.compare(a.getId(), b.getId()));
				resultList.addAll(docList);
			}
			updateStatus();
		} else if (searchIn == SearchField.REF) {
			// search in reference abbreviations
			final List<Reference> refList = ReaderUtilities.referenceList.stream()
											.filter(x -> x.isInAcadRef(strToFind) || x.isInColRef(strToFind))
											.collect(Collectors.toList());
			final TextGroup selTextGroup = starButton.isSelected() ? Corpus.tgAll : corpusSelector.getSelectedTextGroup();
			final List<DocumentInfo> docList = new ArrayList<>();
			for (final Corpus cp : selectedCorp) {
				docList.clear();
				final Corpus.Collection col = cp.getCollection();
				if (refList.stream().noneMatch(x -> x.hasCollection(col)))
					continue;
				for (final DocumentInfo docInfo : cp.getDocInfoMap().values()) {
					final String refStr = docInfo.refProperty().get().toLowerCase();
					final boolean cond = refList.stream().anyMatch(x -> x.isMultiWordInColRef(col, refStr));
					if (cond && docInfo.isInTextGroup(selTextGroup)){
						docList.add(docInfo);
					}
				}
				if (cp.getCollection() == Corpus.Collection.SC)
					docList.sort((a, b) -> ScInfo.scComparator.compare(a.getId(), b.getId()));
				resultList.addAll(docList);
			}
			updateStatus();
		} else {
			// brute full text search in the collection
			searchContent(inputStr);
		}
	}

	private void searchContent(final String text) {
		setBottom(null);
		if (searchTask != null) {
			searchTask.cancel(true);
			progressBar.progressProperty().unbind();
		}
		progressBar.setProgress(0);
		Pattern searchPatt;
		try {
			final String modified = text.replace("ṃ", "(?:ṃ|ṁ)").replace("Ṃ", "(?:Ṃ|Ṁ)");
			searchPatt = Pattern.compile(modified);
		} catch (PatternSyntaxException e) {
			return;
		}
		final Corpus[] selectedCorp = getSelectedCorpora();
		searchTask = new Task<Boolean>() {
			@Override
			protected Boolean call() throws Exception {
				final int total = Arrays.stream(selectedCorp).mapToInt(c -> c.getSize()).sum();
				try {
					int count = 0;
					for (final Corpus cp : selectedCorp) {
						final Map<String, DocumentInfo> docInfoMap = cp.getDocInfoMap();
						updateMessage("Searching... (please wait)");
						final TextGroup selTextGroup = corpusSelector.getSelectedTextGroup();
						long foundCount = 0;
						if (cp.isInArchive()) {
							final ZipFile zip = new ZipFile(cp.getZipFile());
							for (final DocumentInfo docInfo : docInfoMap.values()) {
								if (!docInfo.isInTextGroup(selTextGroup)) continue;
								final ZipEntry entry = zip.getEntry(docInfo.getFileNameWithExt());
								final Scanner in = new Scanner(zip.getInputStream(entry), StandardCharsets.UTF_8);
								final List<String> findResList = new ArrayList<>();
								String findRes;
								while((findRes = in.findWithinHorizon(searchPatt, 0)) != null) {
									findResList.add(findRes);
								}
								foundCount = findResList.size();
								if (foundCount > 0) {
									docInfo.searchResultCountProperty().set((int)foundCount);
									docInfo.setMatchResult(findResList);
									resultList.add(docInfo);
								}
								in.close();
								updateProgress(++count, total);
							}
							zip.close();
						} else {
							for (final DocumentInfo docInfo : docInfoMap.values()) {
								if (!docInfo.isInTextGroup(selTextGroup)) continue;
								final File docFile = new File(Utilities.ROOTDIR + ReaderUtilities.TEXTPATH + cp.getRootName() + File.separator,
										docInfo.getFileNameWithExt());
								if (!docFile.exists()) continue;
								// only gz file is supported by now
								if (!docFile.getName().toLowerCase().endsWith(".gz")) continue;
								final Scanner in = new Scanner(new GZIPInputStream(new FileInputStream(docFile)), StandardCharsets.UTF_8);
								final List<String> findResList = new ArrayList<>();
								String findRes;
								while((findRes = in.findWithinHorizon(searchPatt, 0)) != null) {
									findResList.add(findRes);
								}
								foundCount = findResList.size();
								if (foundCount > 0) {
									docInfo.searchResultCountProperty().set((int)foundCount);
									docInfo.setMatchResult(findResList);
									resultList.add(docInfo);
								}
								in.close();
								updateProgress(++count, total);
							}
						}
					}
				} catch (FileNotFoundException e) {
					System.err.println(e);
				} catch (IOException e) {
					System.err.println(e);
				}
				Platform.runLater(() -> {
					progressBar.progressProperty().unbind();
					mainPane.setBottom(statusBox);
					updateStatus();
				});
				return true;
			}
		};
		progressBar.progressProperty().bind(searchTask.progressProperty());
		searchTask.messageProperty().addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
			progressMessage.setText(newValue);
		});
		Utilities.threadPool.submit(searchTask);
		mainPane.setBottom(progressBox);
	}

	private void updateStatus() {
		final int num = resultList.size();
		final String s = num > 1 ? "s" : "";
		final String text = num == 0 ? "No item found" : num + " item" + s + " found";
		statusMessage.setText(text);
		table.scrollTo(0);
	}

	private void recordQuery() {
		if (!resultList.isEmpty())
			textInput.recordQuery();
	}

	private void openDoc() {
		final DocumentInfo dinfo = table.getSelectionModel().getSelectedItem();
		if (dinfo != null && !dinfo.getFileNameWithExt().isEmpty()) {
			final Corpus.Collection col = Corpus.Collection.valueOf(dinfo.corpusProperty().get());
			final List<String> findRes = dinfo.getMatchResult();
			if (col == Corpus.Collection.SC) {
				if (findRes.isEmpty())
					ReaderUtilities.openScReader(col, dinfo);
				else
					ReaderUtilities.openScReader(col, dinfo, findRes.get(0));
			} else {
				final TocTreeNode node = dinfo.toTocTreeNode();
				if (Utilities.checkFileExistence(node.getNodeFile())) {
					if (findRes.isEmpty())
						ReaderUtilities.openPaliHtmlViewer(node);
					else
						ReaderUtilities.openPaliHtmlViewer(node, findRes.get(0));
				}
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
			final DocumentInfo doc = table.getItems().get(i);
			final String[] data = new String[colCount];
			data[0] = doc.corpusProperty().get();
			data[1] = doc.summaryProperty().get();
			data[2] = doc.refProperty().get();
			data[3] = doc.fileNameProperty().get();
			data[4] = "" + doc.searchResultCountProperty().get();
			result.add(data);
		}
		return result;
	}
	
	private void copyCSV() {
		Utilities.copyCSV(makeCSV());
	}
	
	private void saveCSV() {
		Utilities.saveCSV(makeCSV(), "docfinder-result.csv");
	}

}
