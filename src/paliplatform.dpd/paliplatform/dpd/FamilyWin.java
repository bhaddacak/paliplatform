/*
 * FamilyWin.java
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
import java.nio.charset.StandardCharsets;

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
 * The window showing families of terms as described in the DPD.
 * This is a singleton.
 * @author J.R. Bhaddacak
 * @version 3.0
 * @since 3.0
 */
public class FamilyWin extends SingletonWindow {
	public static final FamilyWin INSTANCE = new FamilyWin();
	private final BorderPane mainPane = new BorderPane();
	private final ScrollPane scrollPane = new ScrollPane();
	private final VBox detailBox = new VBox();
	private final Label message = new Label();
	private final ChoiceBox<String> familyChoice = new ChoiceBox<>();
	private final InfoPopup infoPopup = new InfoPopup();
	private final TextField searchTextField;
	private final ObservableList<String> resultList = FXCollections.<String>observableArrayList();
	private final ListView<String> resultListView = new ListView<>(resultList);
	private final List<String> familyList = new ArrayList<>();
	
	private FamilyWin() {
		windowWidth = Utilities.getRelativeSize(58);
		setTitle("Families of Terms");
		getIcons().add(new Image(FamilyWin.class.getResourceAsStream("resources/images/boxes-stacked.png")));
		
		// add toolbar on the top
		final CommonWorkingToolBar toolBar = new CommonWorkingToolBar(detailBox);
		// config some buttons
		toolBar.saveTextButton.setTooltip(new Tooltip("Save data as text"));
		toolBar.saveTextButton.setOnAction(actionEvent -> saveText());		
		toolBar.copyButton.setTooltip(new Tooltip("Copy text to clipboard"));
		toolBar.copyButton.setOnAction(actionEvent -> copyText());		
		// add new components
		final PaliTextInput searchTextInput = new PaliTextInput(PaliTextInput.InputType.FIELD);
		searchTextField = (TextField)searchTextInput.getInput();
		searchTextField.setPromptText("Search for...");
		searchTextField.textProperty().addListener((obs, oldValue, newValue) -> {
			final String text = Normalizer.normalize(newValue.trim(), Form.NFC);
			showResult(text);
		});
		// family choices
		familyChoice.setTooltip(new Tooltip("Family selector"));
		for (final DpdUtilities.TermFamily f : DpdUtilities.TermFamily.values) {
			familyChoice.getItems().add(Utilities.capitalize(f.toString()));
		}
		familyChoice.getSelectionModel().select(0);
		familyChoice.setOnAction(actionEvent -> showResult());
		// help button
		final Button helpButton = new Button("", new TextIcon("circle-question", TextIcon.IconSet.AWESOME));
		helpButton.setOnAction(actionEvent -> infoPopup.showPopup(helpButton, InfoPopup.Pos.BELOW_RIGHT, true));
		toolBar.getItems().addAll(new Separator(), searchTextField, searchTextInput.getClearButton(),
									searchTextInput.getMethodButton(), familyChoice, helpButton);
		mainPane.setTop(toolBar);

		// add root list on the left
		resultListView.setCellFactory((ListView<String> lv) -> {
			return new ListCell<String>() {
				@Override
				public void updateItem(String item, boolean empty) {
					super.updateItem(item, empty);
					this.setGraphic(null);
					if (empty) {
						this.setText(null);
						this.setTooltip(null);
					} else {
						this.setTooltip(new Tooltip(item));
						this.setText(item);
					}
					this.setStyle("-fx-padding: 0px 0px 0px 3px");
				}
			};
		});
		resultListView.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> {
			final String selItem = newValue;
			if (selItem != null)
				showDetail(selItem);
		});

		// add detail box at the center
		scrollPane.setContent(detailBox);
		detailBox.setPadding(new Insets(10));
		detailBox.prefWidthProperty().bind(scrollPane.widthProperty().subtract(20));
	   	final SplitPane splitPane = new SplitPane();
		splitPane.setOrientation(Orientation.HORIZONTAL);
		splitPane.setDividerPositions(0.2);
		splitPane.getItems().addAll(resultListView, scrollPane);
		mainPane.setCenter(splitPane);

		// add detail display on the bottom
		mainPane.setBottom(message);

		final Scene scene = new Scene(mainPane, windowWidth, windowHeight);
		setScene(scene);
		
		// some intialization
		showResult();

		// prepare info popup
		infoPopup.setContentWithText(DpdUtilities.getTextResource("info-family.txt"));
		infoPopup.setTextWidth(Utilities.getRelativeSize(26));
	}

	public void display(final String term) {
		super.display();
		searchTextField.setText(term);
	}

	private void showResult() {
		final String query = Normalizer.normalize(searchTextField.getText().trim(), Form.NFC);
		showResult(query);
	}

	private void showResult(final String query) {
		final int num = familyChoice.getSelectionModel().getSelectedIndex();
		familyList.clear();
		familyList.addAll(DpdUtilities.getFamilyList(DpdUtilities.TermFamily.values[num]));
		resultList.clear();
		final List<String> output;
		if (query.isEmpty()) {
			output = familyList;
		} else {
			output = familyList.stream().filter(x -> x.contains(query)).collect(Collectors.toList()); 
		}
		resultList.addAll(output);
		// show item count
		final int count = resultList.size();
		final String s = count <= 1 ? "" : "s";
		setMessage(count + " item" + s + " listed");
		if (count > 0) {
			showDetail(resultList.get(0));
		}
		resultListView.scrollTo(0);
	}

	private void setMessage(final String text) {
		message.setText(text);
	}

	private void showDetail(final String term) {
		if (term.isEmpty()) return;
		final int num = familyChoice.getSelectionModel().getSelectedIndex();
		final DpdUtilities.TermFamily family = DpdUtilities.TermFamily.values[num];
		scrollPane.setVvalue(scrollPane.getVmin());
		detailBox.getChildren().clear();
		detailBox.getChildren().add(DpdUtilities.createHeadTextFlow(term, "1.5em"));
		DpdUtilities.addWordFamilyData(detailBox, DpdUtilities.getFamilyData(family, term));
	}

	private String makeText() {
		return DpdUtilities.getVBoxTextFlowText(detailBox);
	}
	
	private void copyText() {
		Utilities.copyText(makeText());
	}
	
	private void saveText() {
		Utilities.saveText(makeText(), "dpdfamily.txt");
	}

}
