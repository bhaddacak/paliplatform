/*
 * DpdRootWin.java
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

package paliplatform.dpd;

import paliplatform.base.*;

import java.util.*;
import java.util.stream.Collectors;
import java.text.Normalizer;
import java.text.Normalizer.Form;

import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.image.*;
import javafx.scene.layout.*;
import javafx.scene.text.*;
import javafx.geometry.*;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/** 
 * This window shows Pali roots as described in the DPD.
 * This is a singleton.
 * @author J.R. Bhaddacak
 * @version 3.0
 * @since 3.0
 */
public class DpdRootWin extends SingletonWindow {
	public static final DpdRootWin INSTANCE = new DpdRootWin();
	private static final String ALL_GROUPS = "All";
	private final BorderPane mainPane = new BorderPane();
	private final ScrollPane scrollPane = new ScrollPane();
	private final VBox detailBox = new VBox();
	private final Label message = new Label();
	private final InfoPopup infoPopup = new InfoPopup();
	private final TextField searchTextField;
	private final ObservableList<DpdRoot> resultList = FXCollections.<DpdRoot>observableArrayList();
	private final ListView<DpdRoot> rootListView = new ListView<>(resultList);
	private final List<DpdRoot> rootList = new ArrayList<>();
	private final List<DpdRoot> workingList = new ArrayList<>();
	
	private DpdRootWin() {
		windowWidth = Utilities.getRelativeSize(58);
		setTitle("Pāli Roots in DPD");
		getIcons().add(new Image(DpdRootWin.class.getResourceAsStream("resources/images/seedling.png")));
		
		// add toolbar on the top
		final CommonWorkingToolBar toolBar = new CommonWorkingToolBar(detailBox);
		// config some buttons
		toolBar.saveTextButton.setTooltip(new Tooltip("Save data as text"));
		toolBar.saveTextButton.setOnAction(actionEvent -> saveText());		
		toolBar.copyButton.setTooltip(new Tooltip("Copy text to clipboard"));
		toolBar.copyButton.setOnAction(actionEvent -> copyText());		
		// add new components
		final ChoiceBox<String> groupChoice = new ChoiceBox<>();
		groupChoice.setTooltip(new Tooltip("Verb group"));
		groupChoice.getItems().add(ALL_GROUPS);
		for (final DpdRoot.RootGroup rg : DpdRoot.RootGroup.values)
			groupChoice.getItems().add(rg.toString());
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
			showResult(text);
		});
		// help button
		final Button helpButton = new Button("", new TextIcon("circle-question", TextIcon.IconSet.AWESOME));
		helpButton.setOnAction(actionEvent -> infoPopup.showPopup(helpButton, InfoPopup.Pos.BELOW_RIGHT, true));
		toolBar.getItems().addAll(new Separator(), groupChoice, searchTextField, 
								searchTextInput.getClearButton(), searchTextInput.getMethodButton(), helpButton);
		mainPane.setTop(toolBar);

		// add root list on the left
		final double leftPaneWidth = Utilities.getRelativeSize(10);
		rootListView.setPrefWidth(leftPaneWidth);
		rootListView.setCellFactory((ListView<DpdRoot> lv) -> {
			return new ListCell<DpdRoot>() {
				@Override
				public void updateItem(DpdRoot item, boolean empty) {
					super.updateItem(item, empty);
					this.setGraphic(null);
					if (empty) {
						this.setText(null);
						this.setTooltip(null);
					} else {
						final DpdRoot root = this.getItem();
						this.setTooltip(new Tooltip(root.getMeaning()));
						this.setText(root.getRoot() + " (" + root.getGroup().toString() + ")");
					}
					this.setStyle("-fx-padding: 0px 0px 0px 3px");
				}
			};
		});
		rootListView.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> {
			final DpdRoot selItem = newValue;
			if (selItem != null)
				showDetail(selItem);
		});
		mainPane.setLeft(rootListView);

		// add detail box at the center
		detailBox.setPadding(new Insets(10));
		detailBox.prefWidthProperty().bind(mainPane.widthProperty().subtract(leftPaneWidth + 20));
		scrollPane.setContent(detailBox);
		mainPane.setCenter(scrollPane);

		// add detail display on the bottom
		mainPane.setBottom(message);

		final Scene scene = new Scene(mainPane, windowWidth, windowHeight);
		setScene(scene);
		
		// some intialization
		rootList.addAll(DpdUtilities.getRootList());
		selectRootGroup(0);

		// prepare info popup
		infoPopup.setContentWithText(DpdUtilities.getTextResource("info-dpdroots.txt"));
		infoPopup.setTextWidth(Utilities.getRelativeSize(27));
	}

	public void display(final String root) {
		super.display();
		searchTextField.setText(root);
	}

	private void selectRootGroup(final int num) {
		workingList.clear();
		if (num == 0) {
			workingList.addAll(rootList);
		} else {
			DpdRoot.RootGroup grp = DpdRoot.RootGroup.fromNumber(num);
			final List<DpdRoot> rlist = rootList.stream().filter(r -> r.getGroup() == grp).collect(Collectors.toList());
			workingList.addAll(rlist);
		}
		showResult();
	}

	private void showResult() {
		final String query = Normalizer.normalize(searchTextField.getText().trim(), Form.NFC);
		showResult(query);
	}

	private void showResult(final String query) {
		resultList.clear();
		final List<DpdRoot> output;
		if (query.isEmpty()) {
			output = workingList;
		} else {
			output = workingList.stream().filter(x -> x.contains(query)).collect(Collectors.toList()); 
		}
		output.sort((a, b) -> Utilities.paliCollator.compare(a.getRoot(), b.getRoot()));
		resultList.addAll(output);
		// show item count
		final int count = resultList.size();
		final String s = count <= 1 ? "" : "s";
		setMessage(count + " item" + s + " listed");
		rootListView.scrollTo(0);
		if (count > 0) {
			showDetail(resultList.get(0));
		}
	}

	private void setMessage(final String text) {
		message.setText(text);
	}

	private void showDetail(final DpdRoot root) {
		scrollPane.setVvalue(scrollPane.getVmin());
		detailBox.getChildren().clear();
		// root head and meaning
		final TextFlow tfRootHead = new TextFlow();
		final String rootStr = root.getRoot();
		final Text txtRoot = new Text(rootStr);
		txtRoot.getStyleClass().add("reader-term");
		txtRoot.setStyle("-fx-font-size:1.5em;-fx-font-weight:bold;");
		final Text txtGroup = DpdUtilities.createInfoText(" (" + root.getGroup().toString() + " - " + root.getRootSign() + ")");
		tfRootHead.getChildren().addAll(txtRoot, txtGroup);
		final TextFlow tfMeaning = DpdUtilities.createInfoTextFlow("", root.getMeaning() + "\n");
		detailBox.getChildren().addAll(tfRootHead, tfMeaning);
		// root information
		final VBox rootInfoBox = new VBox();
		rootInfoBox.getStyleClass().add("bordered-box");
		final String example = root.getExample();
		if (!example.isEmpty()) {
			final TextFlow tfExample = DpdUtilities.createInfoTextFlow("Example: ", example);
			rootInfoBox.getChildren().add(tfExample);
		}
		final String sanskrit = root.getSanskritInfo();
		if (!sanskrit.isEmpty()) {
			final TextFlow tfSanskrit = DpdUtilities.createInfoTextFlow("Sanskrit: ", sanskrit);
			rootInfoBox.getChildren().add(tfSanskrit);
		}
		final String[] patha = root.getPatha();
		if (DpdHeadWordBase.hasData(patha[0])) {
			final TextFlow tfPatha = DpdUtilities.createInfoTextFlow("Dhātupāṭha: ", patha[0] + " = " + patha[1] + " (" + patha[2] + ")");
			rootInfoBox.getChildren().add(tfPatha);
		}
		final String[] manjusa = root.getManjusa();
		if (DpdHeadWordBase.hasData(manjusa[0])) {
			final TextFlow tfManjusa = DpdUtilities.createInfoTextFlow("Dhātumañjūsā: ", manjusa[0] + " = " + manjusa[1] + " (" + manjusa[2] + ")");
			rootInfoBox.getChildren().add(tfManjusa);
		}
		final String[] mala = root.getMala();
		if (DpdHeadWordBase.hasData(mala[0])) {
			final TextFlow tfMala = DpdUtilities.createInfoTextFlow("Dhātumālā: ", mala[0] + " = " + mala[1] + " (" + mala[2] + ")");
			rootInfoBox.getChildren().add(tfMala);
		}
		final String[] panini = root.getPanini();
		if (DpdHeadWordBase.hasData(panini[0])) {
			final TextFlow tfPanini = DpdUtilities.createInfoTextFlow("Pāṇini: ", panini[0] + " = " + panini[1] + " (" + panini[2] + ")");
			rootInfoBox.getChildren().add(tfPanini);
		}
		final String note = root.getNote();
		if (!note.isEmpty()) {
			final TextFlow tfNote = DpdUtilities.createInfoTextFlow("Note: ", note);
			rootInfoBox.getChildren().add(tfNote);
		}
		detailBox.getChildren().add(rootInfoBox);
		// root family
		Map<String, List<List<String>>> rootFamilyMap = root.getRootFamily();
		if (rootFamilyMap == null) {
			rootFamilyMap = DpdUtilities.getRootFamily(rootStr);
			root.setRootFamily(rootFamilyMap);
		}
		detailBox.getChildren().add(DpdUtilities.createInfoTextFlow("\nRoot Family:", ""));
		final List<String> keys = rootFamilyMap.keySet().stream().sorted(Utilities.paliComparator).collect(Collectors.toList());
		for (final String k : keys) {
			final VBox rootFamilyBox = DpdUtilities.createWordFamilyBox(k, rootFamilyMap.get(k));
			detailBox.getChildren().add(rootFamilyBox);
		}
	}

	private String makeText() {
		return DpdUtilities.getVBoxTextFlowText(detailBox);
	}
	
	private void copyText() {
		Utilities.copyText(makeText());
	}
	
	private void saveText() {
		Utilities.saveText(makeText(), "dpdroot.txt");
	}

}
