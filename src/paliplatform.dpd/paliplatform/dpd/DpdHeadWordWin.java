/*
 * DpdHeadWordWin.java
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

package paliplatform.dpd;

import paliplatform.base.*;
import static paliplatform.dpd.DpdUtilities.SearchMethod;

import java.util.*;
import java.text.Normalizer;
import java.text.Normalizer.Form;
import java.sql.*;

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
 * This window shows compound families as described in the DPD.
 * This is a singleton.
 * @author J.R. Bhaddacak
 * @version 3.0
 * @since 3.0
 */
public class DpdHeadWordWin extends SingletonWindow {
	public static final DpdHeadWordWin INSTANCE = new DpdHeadWordWin();
	private final BorderPane mainPane = new BorderPane();
	private final ScrollPane scrollPane = new ScrollPane();
	private final VBox detailBox = new VBox();
	private final Label message = new Label();
	private final ToggleGroup searchMethodGroup = new ToggleGroup();
	private final CheckMenuItem showWordFamilyMenuItem = new CheckMenuItem("Show word family");
	private final CheckMenuItem showIdiomFamilyMenuItem = new CheckMenuItem("Show idiom family");
	private final InfoPopup infoPopup = new InfoPopup();
	private final TextField searchTextField;
	private final ObservableList<String> resultList = FXCollections.<String>observableArrayList();
	private final ListView<String> resultListView = new ListView<>(resultList);
	private final List<String> headWordList = new ArrayList<>();
	private int maxResultCount = DpdUtilities.DEF_MAX_RESULT;
	private String currTerm = "";
	
	private DpdHeadWordWin() {
		windowWidth = Utilities.getRelativeSize(58);
		setTitle("DPD Head Words");
		getIcons().add(new Image(DpdHeadWordWin.class.getResourceAsStream("resources/images/crown.png")));
		
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
		final MenuButton searchOptionsMenu = new MenuButton("", new TextIcon("check-double", TextIcon.IconSet.AWESOME));
		searchOptionsMenu.setTooltip(new Tooltip("Search options"));
		for (final SearchMethod sm : SearchMethod.values) {
			if (sm.ordinal() > 1) break;
			final RadioMenuItem radio = new RadioMenuItem(sm.getName());
			radio.setUserData(sm);
			radio.setToggleGroup(searchMethodGroup);
			searchOptionsMenu.getItems().add(radio);
		}
		showWordFamilyMenuItem.setSelected(true);
		showWordFamilyMenuItem.setOnAction(actionEvent -> showDetail(currTerm));
		showIdiomFamilyMenuItem.setSelected(true);
		showIdiomFamilyMenuItem.setOnAction(actionEvent -> showDetail(currTerm));
		searchOptionsMenu.getItems().addAll(new SeparatorMenuItem(), showWordFamilyMenuItem, showIdiomFamilyMenuItem);
		searchMethodGroup.selectToggle(searchMethodGroup.getToggles().get(0));
        searchMethodGroup.selectedToggleProperty().addListener(observable -> showResult());
		final ChoiceBox<Integer> maxResultChoice = new ChoiceBox<>();
		maxResultChoice.setTooltip(new Tooltip("Maximum results"));
		maxResultChoice.getItems().addAll(DpdUtilities.MAXLIST);
		maxResultChoice.getSelectionModel().select(1);
		maxResultChoice.setOnAction(actionEvent -> {
			maxResultCount = maxResultChoice.getSelectionModel().getSelectedItem();
			showResult();
		});
		// help button
		final Button helpButton = new Button("", new TextIcon("circle-question", TextIcon.IconSet.AWESOME));
		helpButton.setOnAction(actionEvent -> infoPopup.showPopup(helpButton, InfoPopup.Pos.BELOW_RIGHT, true));
		toolBar.getItems().addAll(new Separator(), searchTextField, searchTextInput.getClearButton(),
									searchTextInput.getMethodButton(), searchOptionsMenu, maxResultChoice, helpButton);
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
		
		showResult();

		// prepare info popup
		infoPopup.setContentWithText(DpdUtilities.getTextResource("info-dpdheadwords.txt"));
		infoPopup.setTextWidth(Utilities.getRelativeSize(26));
	}

	public void display(final String term) {
		super.display();
		searchTextField.setText(term);
	}

	private List<String> getHeadWordsFromDB(final String query) {
		final SearchMethod method = (SearchMethod)searchMethodGroup.getSelectedToggle().getUserData();
		final List<String> result = new ArrayList<>();
		try {
			final java.sql.Connection ppdpdConn = Utilities.H2DB.PPDPD.getConnection();
			final String where = query.isEmpty()
									? ""
									: method == SearchMethod.TERM_START
										? " WHERE TERM LIKE '" + query + "%'"
										: " WHERE TERM LIKE '%" + query + "%'";
			final String tabName = Utilities.PpdpdTable.SORTED_HEADWORDS.toString();
			final String select = "SELECT TERM FROM " + tabName + where + " ORDER BY ID LIMIT " + maxResultCount + ";"; 
			final Statement stmt = ppdpdConn.createStatement();
			final ResultSet res = stmt.executeQuery(select);
			while (res.next()) {
				result.add(res.getString("TERM"));
			}
			res.close();
			stmt.close();
		} catch (SQLException e) {
			System.err.println(e);
		}
		return result;
	}

	private DpdHeadWord getDpdHeadWord(final String term) {
		DpdHeadWord result = null;
		try {
			final java.sql.Connection ppdpdConn = Utilities.H2DB.PPDPD.getConnection();
			final String tabName = Utilities.PpdpdTable.SORTED_HEADWORDS.toString();
			final String idSelect = "SELECT TERMID FROM " + tabName + " WHERE TERM = '" + term + "';";
			final Statement idStmt = ppdpdConn.createStatement();
			final ResultSet idRes = idStmt.executeQuery(idSelect);
			if (idRes.next()) {
				final int[] termIds = { idRes.getInt("TERMID") };
				final List<DpdHeadWord> hwList = DpdUtilities.getDpdHeadWords(termIds);
				if (!hwList.isEmpty())
					result = hwList.get(0);
			}
			idRes.close();
			idStmt.close();
		} catch (SQLException e) {
			System.err.println(e);
		}
		return result;
	}

	private void showResult() {
		final String query = Normalizer.normalize(searchTextField.getText().trim(), Form.NFC);
		showResult(query);
	}

	private void showResult(final String query) {
		resultList.clear();
		resultList.addAll(getHeadWordsFromDB(query));
		// show item count
		final int total = Integer.parseInt(Utilities.settings.getProperty("dpd-head-count"));
		final int count = resultList.size();
		final String s = count <= 1 ? "" : "s";
		setMessage(String.format("%,d item%s of %,d listed", count, s, total));
		resultListView.scrollTo(0);
		if (count > 0) {
			showDetail(resultList.get(0));
		}
	}

	private void setMessage(final String text) {
		message.setText(text);
	}

	private void showDetail(final String term) {
		if (term.isEmpty()) return;
		currTerm = term;
		scrollPane.setVvalue(scrollPane.getVmin());
		detailBox.getChildren().clear();
		final DpdHeadWord hw = getDpdHeadWord(term);
		if (hw != null) {
			final VBox resultBox = DpdUtilities.getDpdHeadWordBox(hw,
									showWordFamilyMenuItem.isSelected(), showIdiomFamilyMenuItem.isSelected());
			detailBox.getChildren().add(resultBox);
		}
	}

	private String makeText() {
		return DpdUtilities.getVBoxTextFlowText(detailBox);
	}
	
	private void copyText() {
		Utilities.copyText(makeText());
	}
	
	private void saveText() {
		Utilities.saveText(makeText(), "dpdheadwords.txt");
	}

}
