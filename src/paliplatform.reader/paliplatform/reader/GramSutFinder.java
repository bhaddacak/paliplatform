/*
 * GramSutFinder.java
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

package paliplatform.reader;

import paliplatform.base.*;

import java.util.*;
import java.util.stream.Collectors;
import java.text.Normalizer;
import java.text.Normalizer.Form;

import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.*;
import javafx.scene.image.*;
import javafx.scene.input.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/** 
 * The window showing all Pali grammatical sutta with search function.
 * This is a singleton.
 * @author J.R. Bhaddacak
 * @version 3.2
 * @since 3.0
 */
public class GramSutFinder extends SingletonWindow {
	public static final GramSutFinder INSTANCE = new GramSutFinder();
	private final BorderPane mainPane = new BorderPane();
	private final TableView<SuttaOutput> table = new TableView<>();	
	private final Label message = new Label();
	private final InfoPopup infoPopup = new InfoPopup();
	private final TextField searchTextField;
	private final ObservableList<SuttaOutput> outputList = FXCollections.<SuttaOutput>observableArrayList();
	private final VBox toolBarBox = new VBox();
	private final HBox selectorToolBar = new HBox();
	private final CheckMenuItem includeNiruXrefMenuItem = new CheckMenuItem("Include Niru Xref");
	private final CheckMenuItem includeNiruNoteMenuItem = new CheckMenuItem("Include Niru Notes");
	private final CheckBox cbNotes = new CheckBox("Notes");
	private final Set<GrammarText.GrammarBook> bookSelectorSet = EnumSet.noneOf(GrammarText.GrammarBook.class);
	private final ContextMenu popupMenu = new ContextMenu();											
	private final Map<GrammarText.GrammarBook, CheckBox> bookCheckBoxMap = new EnumMap<>(GrammarText.GrammarBook.class);
	
	private GramSutFinder() {
		windowWidth = Utilities.getRelativeSize(66);
		setTitle("Grammatical Sutta Finder");
		getIcons().add(new Image(ReaderUtilities.class.getResourceAsStream("resources/images/magnifying-glass.png")));
		if (ReaderUtilities.gramSutRefComparator == null)
			ReaderUtilities.gramSutRefComparator = ReaderUtilities.getReferenceComparator(ReaderUtilities.corpusMap.get(Corpus.Collection.GRAM));
		// add toolbar on the top
		// main toolbar
		final CommonWorkingToolBar toolBar = new CommonWorkingToolBar(table);
		// config some buttons
		toolBar.saveTextButton.setTooltip(new Tooltip("Save data as CSV"));
		toolBar.saveTextButton.setOnAction(actionEvent -> saveCSV());		
		toolBar.copyButton.setTooltip(new Tooltip("Copy CSV to clipboard"));
		toolBar.copyButton.setOnAction(actionEvent -> copyCSV());		
		// add new components
		final PaliTextInput searchTextInput = new PaliTextInput(PaliTextInput.InputType.FIELD);
		searchTextField = (TextField)searchTextInput.getInput();
		searchTextField.setPromptText("Search for...");
		searchTextField.textProperty().addListener((obs, oldValue, newValue) -> {
			final String text = Normalizer.normalize(newValue.trim(), Form.NFC);
			setOutputList(text);
		});
		// book selector button
		final Button openSelectorButton = new Button("", new TextIcon("list-check", TextIcon.IconSet.AWESOME));
		openSelectorButton.setTooltip(new Tooltip("Book selectors"));
		openSelectorButton.setOnAction(actionEvent -> toggleSelector());
		// option menu button
		final MenuButton optionsMenu = new MenuButton("", new TextIcon("check-double", TextIcon.IconSet.AWESOME));		
		optionsMenu.setTooltip(new Tooltip("Options"));
		includeNiruXrefMenuItem.setOnAction(actionEvent -> setOutputList());
		includeNiruNoteMenuItem.setOnAction(actionEvent -> setOutputList());
		optionsMenu.getItems().addAll(includeNiruXrefMenuItem, includeNiruNoteMenuItem);
		// help button
		final Button helpButton = new Button("", new TextIcon("circle-question", TextIcon.IconSet.AWESOME));
		helpButton.setOnAction(actionEvent -> infoPopup.showPopup(helpButton, InfoPopup.Pos.BELOW_RIGHT, true));
		toolBar.getItems().addAll(new Separator(), searchTextField, searchTextInput.getClearButton(),
								searchTextInput.getMethodButton(), openSelectorButton, optionsMenu, helpButton);
		// prepare for option toolbar
		selectorToolBar.setPadding(new Insets(3));
		// book selectors
		bookSelectorSet.addAll(GrammarSutta.bookMap.values());
		final List<GrammarText.GrammarBook> bookList = GrammarSutta.bookMap.values().stream()
										.sorted((a, b) -> ReaderUtilities.gramSutRefComparator.compare(a.getFirstChar(), b.getFirstChar()))
										.collect(Collectors.toList());
		for (final GrammarText.GrammarBook gbook : bookList) {
			final CheckBox cb = new CheckBox(gbook.getRef() + " ");
			cb.setSelected(bookSelectorSet.contains(gbook));
			cb.setOnAction(actionEvent -> selectBook(gbook, cb.isSelected()));
			bookCheckBoxMap.put(gbook, cb);
			selectorToolBar.getChildren().add(cb);
		}
		final Button allButton = new Button("All");
		allButton.setPadding(new Insets(2));
		allButton.setOnAction(actionEvent -> selectAllBook(true));
		final Button noneButton = new Button("None");
		noneButton.setPadding(new Insets(2));
		noneButton.setOnAction(actionEvent -> selectAllBook(false));
		selectorToolBar.getChildren().addAll(allButton, noneButton);

		toolBarBox.getChildren().add(toolBar);
		mainPane.setTop(toolBarBox);

		// add context menu
		final MenuItem openMenuItem = new MenuItem("Open");
		openMenuItem.setOnAction(actionEvent -> openDoc());		
		popupMenu.getItems().addAll(openMenuItem);
		table.setContextMenu(popupMenu);

		// add main content
		final Scene scene = new Scene(mainPane, windowWidth, windowHeight);
		table.setItems(outputList);
		table.setOnMouseClicked(mouseEvent -> showDetail());
		mainPane.setCenter(table);
		
		// add detail display on the bottom
		final VBox detailBox = new VBox();
		message.setWrapText(true);
		detailBox.getChildren().add(message);
		mainPane.setBottom(detailBox);

		// some intialization
		if (ReaderUtilities.gramSutList == null)
			ReaderUtilities.updateGramSutList();
		setOutputList();
		setupTable();

		setScene(scene);
		
		// prepare info popup
		infoPopup.setContentWithText(ReaderUtilities.getTextResource("info-gramsutfinder.txt"));
		infoPopup.setTextWidth(Utilities.getRelativeSize(32));
	}

	private void toggleSelector() {
		if (toolBarBox.getChildren().size() > 1)
			toolBarBox.getChildren().remove(selectorToolBar);
		else
			toolBarBox.getChildren().add(selectorToolBar);
	}

	private void selectBook(final GrammarText.GrammarBook book, final boolean doAdd) {
		if (doAdd)
			bookSelectorSet.add(book);
		else
			bookSelectorSet.remove(book);
		setOutputList();
	}

	private void selectAllBook(final Boolean yn) {
		bookSelectorSet.clear();
		bookCheckBoxMap.forEach((book, cb) -> {
			cb.setSelected(yn);
			if (yn)
				bookSelectorSet.add(book);
		});
		setOutputList();
	}

	private void setOutputList() {
		final String filter = Normalizer.normalize(searchTextField.getText().trim(), Form.NFC);
		setOutputList(filter);
	}

	private void setOutputList(final String filter) {
		outputList.clear();
		final List<GrammarSutta> workingList = ReaderUtilities.gramSutList.stream()
												.filter(x -> bookSelectorSet.contains(x.getBook()))
												.collect(Collectors.toList());
		final List<GrammarSutta> output;
		if (filter.isEmpty()) {
			output = workingList;
		} else {
			output = workingList.stream()
					.filter(x -> x.getOriginal().contains(filter) || x.xrefContains(filter))
					.collect(Collectors.toList());
		}
		for (final GrammarSutta st : output)
			outputList.add(new SuttaOutput(st, includeNiruXrefMenuItem.isSelected(), includeNiruNoteMenuItem.isSelected()));
		// show item count
		final int count = outputList.size();
		final String s = count <= 1 ? "" : "s";
		message.setText(count + " item" + s + " listed");
	}

	private void setupTable() {
		final TableColumn<SuttaOutput, String> refCol = new TableColumn<>("Ref");
		refCol.setCellValueFactory(new PropertyValueFactory<>(outputList.get(0).refProperty().getName()));
		refCol.setReorderable(false);
		refCol.setComparator(Utilities.alphanumComparator);
		refCol.prefWidthProperty().bind(mainPane.widthProperty().divide(9).multiply(2).subtract(20));
		final TableColumn<SuttaOutput, String> suttaCol = new TableColumn<>("Sutta");
		suttaCol.setCellValueFactory(new PropertyValueFactory<>(outputList.get(0).suttaProperty().getName()));
		suttaCol.setReorderable(false);
		suttaCol.setComparator(Utilities.paliComparator);
		suttaCol.prefWidthProperty().bind(mainPane.widthProperty().divide(9).multiply(4));
		final TableColumn<SuttaOutput, String> xrefCol = new TableColumn<>("Xref");
		xrefCol.setCellValueFactory(new PropertyValueFactory<>(outputList.get(0).xrefProperty().getName()));
		xrefCol.setReorderable(false);
		xrefCol.setComparator(Utilities.paliComparator);
		xrefCol.prefWidthProperty().bind(mainPane.widthProperty().divide(9).multiply(3));
		table.getColumns().clear();
		table.getColumns().add(refCol);
		table.getColumns().add(suttaCol);
		table.getColumns().add(xrefCol);	
	}

	private void showDetail() {
		final SuttaOutput selected = table.getSelectionModel().getSelectedItem();
		if (selected == null) return;
		final GrammarSutta sutta = selected.getGramSut();
		final String xrefSimpleStr = sutta.getXrefString(GrammarSutta.RefType.SIMPLE);
		final String xrefNiruStr = sutta.getXrefString(GrammarSutta.RefType.NIRU);
		final String andStr = xrefNiruStr.isEmpty() ? "" : " and ";
		final String seeAlso = xrefSimpleStr.isEmpty() && xrefNiruStr.isEmpty()
								? ""
								: " See also: " + xrefSimpleStr + andStr + xrefNiruStr;
		message.setText(sutta.getOriginal() + seeAlso);
	}

	private void openDoc() {
		final SuttaOutput selected = table.getSelectionModel().getSelectedItem();
		if (selected == null) return;
		final GrammarSutta gramSut = selected.getGramSut();
		final String bookId = gramSut.getBook().getBookId();
		final String sutNum = gramSut.getSuttaNumber() + ". ";
		final Corpus cp = ReaderUtilities.corpusMap.get(Corpus.Collection.GRAM);
		final DocumentInfo dinfo = cp.getDocInfo(bookId);
		if (dinfo != null && !dinfo.getFileNameWithExt().isEmpty()) {
			final TocTreeNode node = dinfo.toTocTreeNode();
			if (Utilities.checkFileExistence(node.getNodeFile()))
				ReaderUtilities.openPaliHtmlViewer(node, sutNum);
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
			final SuttaOutput sut = table.getItems().get(i);
			final String[] data = new String[colCount];
			data[0] = sut.refProperty().get();
			data[1] = sut.suttaProperty().get();
			data[2] = sut.xrefProperty().get();
			result.add(data);
		}
		return result;
	}
	
	private void copyCSV() {
		Utilities.copyCSV(makeCSV());
	}
	
	private void saveCSV() {
		Utilities.saveCSV(makeCSV(), "gramsuts.csv");
	}

	// inner class
	public static final class SuttaOutput {
		private StringProperty ref;
		private StringProperty sutta;
		private StringProperty xref;
		private final GrammarSutta gramSut;
		
		public SuttaOutput(final GrammarSutta gsut, final boolean withNiruXref, final boolean withNotes) {
			gramSut = gsut;
			refProperty().set(gsut.getFullRef());
			suttaProperty().set(gsut.getSuttaBody(withNotes));
			final String simpleXref = gsut.getXrefString(GrammarSutta.RefType.SIMPLE);
			final String niruXref = withNiruXref ? gsut.getXrefString(GrammarSutta.RefType.NIRU) : "";
			final String commaStr = simpleXref.isEmpty() ? "" : ", ";
			final String xrefStr = simpleXref.isEmpty() && niruXref.isEmpty()
									? ""
									: niruXref.isEmpty()
										? simpleXref
										: simpleXref + commaStr + niruXref;
			xrefProperty().set(xrefStr);
		}
		
		public StringProperty refProperty() {
			if (ref == null)
				ref = new SimpleStringProperty(this, "ref");
			return ref;
		}
		
		public StringProperty suttaProperty() {
			if (sutta == null)
				sutta = new SimpleStringProperty(this, "sutta");
			return sutta;
		}
		
		public StringProperty xrefProperty() {
			if (xref == null)
				xref = new SimpleStringProperty(this, "xref");
			return xref;
		}

		public GrammarSutta getGramSut() {
			return gramSut;
		}
	}

}
