/*
 * RootWin.java
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

package paliplatform.grammar;

import paliplatform.base.*;

import java.util.*;
import java.util.stream.Collectors;
import java.text.Normalizer;
import java.text.Normalizer.Form;
import java.nio.charset.StandardCharsets;

import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.*;
import javafx.scene.image.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/** 
 * The window showing Pali roots according to Saddanīti.
 * This is a singleton.
 * @author J.R. Bhaddacak
 * @version 3.0
 * @since 2.0
 */
public class RootWin extends SingletonWindow {
	static enum RootField {
		GROUP("Group"), ROOT("Root"), PALI_MEANING("Pāli Meaning"), ENG_MEANING("Eng Meaning");
		public static final RootField[] values = values();
		final public String name;
		private RootField(final String name) {
			this.name = name;
		}
	}
	public static final RootWin INSTANCE = new RootWin();
	private static final String ALL_GROUPS = "All";
	private final BorderPane mainPane = new BorderPane();
	private final TableView<RootOutput> table = new TableView<>();	
	private final VBox detailBox = new VBox();
	private final InfoPopup infoPopup = new InfoPopup();
	private final TextField searchTextField;
	private final ObservableList<RootOutput> outputList = FXCollections.<RootOutput>observableArrayList();
	private final List<PaliRoot> rootList;
	private final List<PaliRoot> workingList = new ArrayList<>();
	private RootField currSearchField = RootField.ROOT;
	
	private RootWin() {
		windowWidth = Utilities.getRelativeSize(66);
		setTitle("Pāli Roots");
		getIcons().add(new Image(RootWin.class.getResourceAsStream("resources/images/seedling.png")));
		
		// add toolbar on the top
		final CommonWorkingToolBar toolBar = new CommonWorkingToolBar(table);
		// config some buttons
		toolBar.saveTextButton.setTooltip(new Tooltip("Save data as CSV"));
		toolBar.saveTextButton.setOnAction(actionEvent -> saveCSV());		
		toolBar.copyButton.setTooltip(new Tooltip("Copy CSV to clipboard"));
		toolBar.copyButton.setOnAction(actionEvent -> copyCSV());		
		// add new components
		final ChoiceBox<String> groupChoice = new ChoiceBox<>();
		groupChoice.setTooltip(new Tooltip("Verb group"));
		groupChoice.getItems().add(ALL_GROUPS);
		for (final PaliRoot.RootGroup rg : PaliRoot.RootGroup.values)
			groupChoice.getItems().add(rg.getName());
		groupChoice.getSelectionModel().select(0);
		groupChoice.setOnAction(actionEvent -> {
			final int selected = groupChoice.getSelectionModel().getSelectedIndex();
			selectRootGroup(selected);
		});
		final PaliTextInput searchTextInput = new PaliTextInput(PaliTextInput.InputType.FIELD);
		searchTextField = (TextField)searchTextInput.getInput();
		searchTextField.setPromptText("Search for...");
		searchTextField.textProperty().addListener((obs, oldValue, newValue) -> {
			final String text = Normalizer.normalize(newValue.trim(), Form.NFC);
			setOutputList(text);
		});
		final ChoiceBox<String> fieldChoice = new ChoiceBox<>();
		for (final RootField rf : RootField.values) {
			if (rf.ordinal() == 0) continue;
			fieldChoice.getItems().add(rf.name);
		}
		fieldChoice.getSelectionModel().select(0);
		fieldChoice.setOnAction(actionEvent -> {
			final int selectedInd = fieldChoice.getSelectionModel().getSelectedIndex();
			currSearchField = RootField.values[selectedInd + 1];
			setOutputList();
		});
		// help button
		final Button helpButton = new Button("", new TextIcon("circle-question", TextIcon.IconSet.AWESOME));
		helpButton.setOnAction(actionEvent -> infoPopup.showPopup(helpButton, InfoPopup.Pos.BELOW_RIGHT, true));
		toolBar.getItems().addAll(new Separator(), groupChoice, searchTextField, 
								searchTextInput.getClearButton(), searchTextInput.getMethodButton(), new Label("in"), fieldChoice, helpButton);
		mainPane.setTop(toolBar);

		// add main content
		final Scene scene = new Scene(mainPane, windowWidth, windowHeight);
		table.setItems(outputList);
		table.setOnMouseClicked(mouseEvent -> showDetail());
		mainPane.setCenter(table);
		
		// add detail display on the bottom
		mainPane.setBottom(detailBox);

		// some intialization
		GrammarUtilities.loadRootList();
		rootList = new ArrayList<>(GrammarUtilities.paliRoots.values());
		selectRootGroup(0);
		setupTable();

		setScene(scene);
		
		// prepare info popup
		infoPopup.setContentWithText(GrammarUtilities.getTextResource("info-paliroots.txt"));
		infoPopup.setTextWidth(Utilities.getRelativeSize(27));
	}

	private void selectRootGroup(final int num) {
		workingList.clear();
		if (num == 0) {
			workingList.addAll(rootList);
		} else {
			PaliRoot.RootGroup grp = PaliRoot.RootGroup.fromNumber(num);
			final List<PaliRoot> rlist = rootList.stream().filter(r -> r.getGroup() == grp).collect(Collectors.toList());
			workingList.addAll(rlist);
		}
		setOutputList();
	}

	private void setOutputList() {
		final String query = Normalizer.normalize(searchTextField.getText().trim(), Form.NFC);
		setOutputList(currSearchField, query);
	}

	private void setOutputList(final String query) {
		setOutputList(currSearchField, query);
	}

	private void setOutputList(final RootField field, final String query) {
		outputList.clear();
		clearDetail();
		final List<PaliRoot> output;
		if (query.isEmpty()) {
			output = workingList;
		} else {
			switch (field) {
				case ROOT: output = workingList.stream()
							.filter(x -> x.getRoot().contains(query)).collect(Collectors.toList()); 
							break;
				case PALI_MEANING: output = workingList.stream()
							.filter(x -> x.getPaliMeaning().contains(query)).collect(Collectors.toList()); 
							break;
				case ENG_MEANING: output = workingList.stream()
							.filter(x -> x.getEngMeaning().contains(query)).collect(Collectors.toList()); 
							break;
				default: output = new ArrayList<>();
			}
		}
		for (final PaliRoot pr : output)
			outputList.add(new RootOutput(pr));
		// show item count
		final int count = outputList.size();
		final String s = count <= 1 ? "" : "s";
		final Label lblCount = new Label(count + " item" + s + " listed");
		detailBox.getChildren().add(lblCount);
	}

	private void setupTable() {
		final TableColumn<RootOutput, Integer> idCol = new TableColumn<>("ID");
		idCol.setCellValueFactory(new PropertyValueFactory<>(outputList.get(0).idProperty().getName()));
		idCol.prefWidthProperty().bind(mainPane.widthProperty().divide(12).subtract(10));
		final TableColumn<RootOutput, String> rootCol = new TableColumn<>(RootField.ROOT.name);
		rootCol.setCellValueFactory(new PropertyValueFactory<>(outputList.get(0).rootProperty().getName()));
		rootCol.prefWidthProperty().bind(mainPane.widthProperty().divide(12).multiply(2));
		final TableColumn<RootOutput, String> groupCol = new TableColumn<>(RootField.GROUP.name);
		groupCol.setCellValueFactory(new PropertyValueFactory<>(outputList.get(0).groupProperty().getName()));
		groupCol.prefWidthProperty().bind(mainPane.widthProperty().divide(12).subtract(10));
		final TableColumn<RootOutput, String> paliMeaningCol = new TableColumn<>(RootField.PALI_MEANING.name);
		paliMeaningCol.setCellValueFactory(new PropertyValueFactory<>(outputList.get(0).paliMeaningProperty().getName()));
		paliMeaningCol.prefWidthProperty().bind(mainPane.widthProperty().divide(12).multiply(4));
		final TableColumn<RootOutput, String> engMeaningCol = new TableColumn<>(RootField.ENG_MEANING.name);
		engMeaningCol.setCellValueFactory(new PropertyValueFactory<>(outputList.get(0).engMeaningProperty().getName()));
		engMeaningCol.prefWidthProperty().bind(mainPane.widthProperty().divide(12).multiply(4));
		table.getColumns().clear();
		table.getColumns().add(idCol);
		table.getColumns().add(rootCol);
		table.getColumns().add(groupCol);
		table.getColumns().add(paliMeaningCol);	
		table.getColumns().add(engMeaningCol);	
	}
	
	private void showDetail() {
		clearDetail();
		final RootOutput output = table.getSelectionModel().getSelectedItem();
		if (output == null) return;
		final PaliRoot root = output.getPaliRoot();
		final String rRemark = root.getRootRemark();
		final String rMark = rRemark.isEmpty() ? "" : "*";
		final String mRemark = root.getMeaningRemark();
		final String mMark = mRemark.isEmpty() ? "" : "**";
		final Label head = new Label(root.getRoot() + rMark + " (" + root.getGroup().toString() + ") "
							+ root.getPaliMeaning() + mMark + "—" + root.getEngMeaning());
		head.setWrapText(true);
		detailBox.getChildren().add(head);
		if (!rRemark.isEmpty()) {
			final Label rootRemark = new Label(rMark + " " + rRemark);
			rootRemark.setWrapText(true);
			detailBox.getChildren().add(rootRemark);
		}
		if (!mRemark.isEmpty()) {
			final Label meaningRemark = new Label(mMark + " " + mRemark);
			meaningRemark.setWrapText(true);
			detailBox.getChildren().add(meaningRemark);
		}
	}

	private void clearDetail() {
		detailBox.getChildren().clear();
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
			final RootOutput root = table.getItems().get(i);
			final String[] data = new String[colCount];
			data[0] = "" + root.idProperty().get();
			data[1] = root.rootProperty().get();
			data[2] = root.groupProperty().get();
			data[3] = root.paliMeaningProperty().get();
			data[4] = root.engMeaningProperty().get();
			result.add(data);
		}
		return result;
	}
	
	private void copyCSV() {
		Utilities.copyCSV(makeCSV());
	}
	
	private void saveCSV() {
		Utilities.saveCSV(makeCSV(), "roots.csv");
	}

	// inner class
	public static final class RootOutput {
		private IntegerProperty id;
		private StringProperty root;
		private StringProperty group;
		private StringProperty paliMeaning;
		private StringProperty engMeaning;
		private final PaliRoot paliRoot;
		
		public RootOutput(final PaliRoot proot) {
			idProperty().set(proot.getId());
			final String rMark = proot.getRootRemark().isEmpty() ? "" : "*";
			rootProperty().set(proot.getRoot() + rMark);
			groupProperty().set(proot.getGroup().toString());
			final String mMark = proot.getMeaningRemark().isEmpty() ? "" : "**";
			paliMeaningProperty().set(proot.getPaliMeaning() + mMark);
			engMeaningProperty().set(proot.getEngMeaning());
			paliRoot = proot;
		}
		
		public IntegerProperty idProperty() {
			if (id == null)
				id = new SimpleIntegerProperty(this, "id");
			return id;
		}
		
		public StringProperty rootProperty() {
			if (root == null)
				root = new SimpleStringProperty(this, "root");
			return root;
		}
		
		public StringProperty groupProperty() {
			if (group == null)
				group = new SimpleStringProperty(this, "group");
			return group;
		}
		
		public StringProperty paliMeaningProperty() {
			if (paliMeaning == null)
				paliMeaning = new SimpleStringProperty(this, "paliMeaning");
			return paliMeaning;
		}
		
		public StringProperty engMeaningProperty() {
			if (engMeaning == null)
				engMeaning = new SimpleStringProperty(this, "engMeaning");
			return engMeaning;
		}

		public PaliRoot getPaliRoot() {
			return paliRoot;
		}
	} // end inner class
	
}
