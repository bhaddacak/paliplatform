/*
 * ReferenceTable.java
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
import java.nio.charset.StandardCharsets;

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
 * The window showing text references used in the collections.
 * This is a singleton.
 * @author J.R. Bhaddacak
 * @version 3.0
 * @since 3.0
 */
public class ReferenceTable extends SingletonWindow {
	public static final ReferenceTable INSTANCE = new ReferenceTable();
	private final BorderPane mainPane = new BorderPane();
	private final TableView<RefOutput> table = new TableView<>();	
	private final Label message = new Label();
	private final InfoPopup infoPopup = new InfoPopup();
	private final TextField searchTextField;
	private final ObservableList<RefOutput> outputList = FXCollections.<RefOutput>observableArrayList();
	private final VBox toolBarBox = new VBox();
	private final ContextMenu popupMenu = new ContextMenu();											
	
	private ReferenceTable() {
		windowWidth = Utilities.getRelativeSize(72);
		setTitle("Reference Table");
		getIcons().add(new Image(ReaderUtilities.class.getResourceAsStream("resources/images/table-cells.png")));
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
		// help button
		final Button helpButton = new Button("", new TextIcon("circle-question", TextIcon.IconSet.AWESOME));
		helpButton.setOnAction(actionEvent -> infoPopup.showPopup(helpButton, InfoPopup.Pos.BELOW_RIGHT, true));
		toolBar.getItems().addAll(new Separator(), searchTextField, searchTextInput.getClearButton(),
								searchTextInput.getMethodButton(), helpButton);
		toolBarBox.getChildren().add(toolBar);
		mainPane.setTop(toolBarBox);

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
		if (ReaderUtilities.referenceList == null)
			ReaderUtilities.readReferenceList();
		setOutputList();
		setupTable();

		setScene(scene);
		
		// prepare info popup
		infoPopup.setContentWithText(ReaderUtilities.getTextResource("info-reftable.txt"));
		infoPopup.setTextWidth(Utilities.getRelativeSize(32));
	}

	private void setOutputList() {
		final String filter = Normalizer.normalize(searchTextField.getText().trim(), Form.NFC);
		setOutputList(filter);
	}

	private void setOutputList(final String filter) {
		outputList.clear();
		final List<Reference> output;
		if (filter.isEmpty()) {
			output = ReaderUtilities.referenceList;
		} else {
			final String lowerFilter = filter.toLowerCase();
			output = ReaderUtilities.referenceList.stream()
					.filter(x -> x.getFullName().toLowerCase().contains(lowerFilter)
							|| x.isInAcadRef(lowerFilter)
							|| x.isInColRef(lowerFilter))
					.collect(Collectors.toList());
		}
		for (final Reference ref : output)
			outputList.add(new RefOutput(ref));
		// show item count
		final int count = outputList.size();
		final String s = count <= 1 ? "" : "s";
		message.setText(count + " item" + s + " listed");
	}

	private void setupTable() {
		final TableColumn<RefOutput, String> nameCol = new TableColumn<>("Name");
		nameCol.setCellValueFactory(new PropertyValueFactory<>(outputList.get(0).nameProperty().getName()));
		nameCol.setReorderable(false);
		nameCol.setComparator(Utilities.paliComparator);
		nameCol.prefWidthProperty().bind(mainPane.widthProperty().divide(11).multiply(3).subtract(26));
		final TableColumn<RefOutput, String> acadCol = new TableColumn<>("Acad");
		acadCol.setCellValueFactory(new PropertyValueFactory<>(outputList.get(0).acadProperty().getName()));
		acadCol.setReorderable(false);
		acadCol.setComparator(Utilities.paliComparator);
		acadCol.prefWidthProperty().bind(mainPane.widthProperty().divide(11));
		final TableColumn<RefOutput, String> cstrCol = new TableColumn<>("CSTR");
		cstrCol.setCellValueFactory(new PropertyValueFactory<>(outputList.get(0).cstrProperty().getName()));
		cstrCol.setReorderable(false);
		cstrCol.setComparator(Utilities.paliComparator);
		cstrCol.prefWidthProperty().bind(mainPane.widthProperty().divide(11));
		final TableColumn<RefOutput, String> cst4Col = new TableColumn<>("CST4");
		cst4Col.setCellValueFactory(new PropertyValueFactory<>(outputList.get(0).cst4Property().getName()));
		cst4Col.setReorderable(false);
		cst4Col.setComparator(Utilities.paliComparator);
		cst4Col.prefWidthProperty().bind(mainPane.widthProperty().divide(11));
		final TableColumn<RefOutput, String> bjtCol = new TableColumn<>("BJT");
		bjtCol.setCellValueFactory(new PropertyValueFactory<>(outputList.get(0).bjtProperty().getName()));
		bjtCol.setReorderable(false);
		bjtCol.setComparator(Utilities.paliComparator);
		bjtCol.prefWidthProperty().bind(mainPane.widthProperty().divide(11));
		final TableColumn<RefOutput, String> ptstCol = new TableColumn<>("PTST");
		ptstCol.setCellValueFactory(new PropertyValueFactory<>(outputList.get(0).ptstProperty().getName()));
		ptstCol.setReorderable(false);
		ptstCol.setComparator(Utilities.paliComparator);
		ptstCol.prefWidthProperty().bind(mainPane.widthProperty().divide(11));
		final TableColumn<RefOutput, String> srtCol = new TableColumn<>("SRT");
		srtCol.setCellValueFactory(new PropertyValueFactory<>(outputList.get(0).srtProperty().getName()));
		srtCol.setReorderable(false);
		srtCol.setComparator(Utilities.paliComparator);
		srtCol.prefWidthProperty().bind(mainPane.widthProperty().divide(11));
		final TableColumn<RefOutput, String> gramCol = new TableColumn<>("Gram");
		gramCol.setCellValueFactory(new PropertyValueFactory<>(outputList.get(0).gramProperty().getName()));
		gramCol.setReorderable(false);
		gramCol.setComparator(Utilities.paliComparator);
		gramCol.prefWidthProperty().bind(mainPane.widthProperty().divide(11));
		final TableColumn<RefOutput, String> scCol = new TableColumn<>("SC");
		scCol.setCellValueFactory(new PropertyValueFactory<>(outputList.get(0).scProperty().getName()));
		scCol.setReorderable(false);
		scCol.setComparator(Utilities.paliComparator);
		scCol.prefWidthProperty().bind(mainPane.widthProperty().divide(11));
		table.getColumns().clear();
		table.getColumns().add(nameCol);
		table.getColumns().add(acadCol);
		table.getColumns().add(cstrCol);	
		table.getColumns().add(cst4Col);	
		table.getColumns().add(bjtCol);	
		table.getColumns().add(ptstCol);	
		table.getColumns().add(srtCol);	
		table.getColumns().add(gramCol);	
		table.getColumns().add(scCol);	
	}

	private void showDetail() {
		final RefOutput selected = table.getSelectionModel().getSelectedItem();
		if (selected == null) return;
		final Reference ref = selected.getReference();
		message.setText(ref.getNote());
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
			final RefOutput ref = table.getItems().get(i);
			final String[] data = new String[colCount];
			data[0] = ref.nameProperty().get();
			data[1] = ref.acadProperty().get();
			data[2] = ref.cstrProperty().get();
			data[3] = ref.cst4Property().get();
			data[4] = ref.scProperty().get();
			data[5] = ref.ptstProperty().get();
			data[6] = ref.bjtProperty().get();
			data[7] = ref.srtProperty().get();
			data[8] = ref.gramProperty().get();
			result.add(data);
		}
		return result;
	}
	
	private void copyCSV() {
		Utilities.copyCSV(makeCSV());
	}
	
	private void saveCSV() {
		Utilities.saveCSV(makeCSV(), "references.csv");
	}

	// inner class
	public static final class RefOutput {
		private StringProperty name;
		private StringProperty acad;
		private StringProperty cstr;
		private StringProperty cst4;
		private StringProperty sc;
		private StringProperty ptst;
		private StringProperty bjt;
		private StringProperty srt;
		private StringProperty gram;
		private final Reference reference;
		
		public RefOutput(final Reference ref) {
			reference = ref;
			final String star = ref.hasNote() ? "*" : "";
			nameProperty().set(star + ref.getFullName());
			acadProperty().set(ref.getAcadRefStr());
			cstrProperty().set(ref.getColRefStr(Corpus.Collection.CSTR));
			cst4Property().set(ref.getColRefStr(Corpus.Collection.CST4));
			scProperty().set(ref.getColRefStr(Corpus.Collection.SC));
			ptstProperty().set(ref.getColRefStr(Corpus.Collection.PTST));
			bjtProperty().set(ref.getColRefStr(Corpus.Collection.BJT));
			srtProperty().set(ref.getColRefStr(Corpus.Collection.SRT));
			gramProperty().set(ref.getColRefStr(Corpus.Collection.GRAM));
		}
		
		public StringProperty nameProperty() {
			if (name == null)
				name = new SimpleStringProperty(this, "name");
			return name;
		}
		
		public StringProperty acadProperty() {
			if (acad == null)
				acad = new SimpleStringProperty(this, "acad");
			return acad;
		}
		
		public StringProperty cstrProperty() {
			if (cstr == null)
				cstr = new SimpleStringProperty(this, "cstr");
			return cstr;
		}

		public StringProperty cst4Property() {
			if (cst4 == null)
				cst4 = new SimpleStringProperty(this, "cst4");
			return cst4;
		}

		public StringProperty scProperty() {
			if (sc == null)
				sc = new SimpleStringProperty(this, "sc");
			return sc;
		}

		public StringProperty ptstProperty() {
			if (ptst == null)
				ptst = new SimpleStringProperty(this, "ptst");
			return ptst;
		}

		public StringProperty bjtProperty() {
			if (bjt == null)
				bjt = new SimpleStringProperty(this, "bjt");
			return bjt;
		}

		public StringProperty srtProperty() {
			if (srt == null)
				srt = new SimpleStringProperty(this, "srt");
			return srt;
		}

		public StringProperty gramProperty() {
			if (gram == null)
				gram = new SimpleStringProperty(this, "gram");
			return gram;
		}

		public Reference getReference() {
			return reference;
		}
	}

}

