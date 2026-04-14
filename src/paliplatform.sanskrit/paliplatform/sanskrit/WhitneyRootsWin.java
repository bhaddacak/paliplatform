/*
 * WhitneyRootsWin.java
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

package paliplatform.sanskrit;

import paliplatform.base.*;

import java.util.*;
import java.util.stream.*;
import java.util.zip.*;
import java.io.*;
import java.text.Normalizer;
import java.text.Normalizer.Form;

import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.image.*;
import javafx.scene.input.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.geometry.Insets;

/** 
 * The window showing William Dwight Whitney's Sanskrit roots.
 * This is a singleton.
 * @author J.R. Bhaddacak
 * @version 4.1
 * @since 4.1
 */
public final class WhitneyRootsWin extends SingletonWindow {
	public static final WhitneyRootsWin INSTANCE = new WhitneyRootsWin();
	private final BorderPane mainPane = new BorderPane();
	private final ComboBox<String> pageChoice = new ComboBox<>();
	private final List<String> pageList = new ArrayList<>();
	private final ScrollPane scrollPane = new ScrollPane();
	private final ImageView pageDisplay = new ImageView();
	private final ObservableList<StringPair> rootList = FXCollections.<StringPair>observableArrayList();
	private final ListView<StringPair> rootListView = new ListView<>(rootList);
	private final InfoPopup infoPopup = new InfoPopup();
	private final TextField searchTextField;
	private final File whitneyZip;
	private int currIndex = 0;

	private WhitneyRootsWin() {
		windowWidth = Utilities.getRelativeSize(68);
		windowHeight = Utilities.getRelativeSize(46);
		setTitle("Whitney's Sanskrit Roots");
		getIcons().add(new Image(WhitneyRootsWin.class.getResourceAsStream("resources/images/seedling.png")));
		// add tool bar
		final ToolBar toolBar = new ToolBar();	
		pageChoice.setOnAction(actionEvent -> updatePage());
		// add navigator
		final Button homeButton = new Button("", new TextIcon("house", TextIcon.IconSet.AWESOME));
		homeButton.setTooltip(new Tooltip("Go to the first page"));
		homeButton.setOnAction(actionEvent -> gotoPage(0));
		final Button fastBackwardButton = new Button("", new TextIcon("angles-left", TextIcon.IconSet.AWESOME));
		fastBackwardButton.setOnAction(actionEvent -> changePage(-10));
		final Button backwardButton = new Button("", new TextIcon("angle-left", TextIcon.IconSet.AWESOME));
		backwardButton.setOnAction(actionEvent -> changePage(-1));
		final Button forwardButton = new Button("", new TextIcon("angle-right", TextIcon.IconSet.AWESOME));
		forwardButton.setOnAction(actionEvent -> changePage(+1));
		final Button fastForwardButton = new Button("", new TextIcon("angles-right", TextIcon.IconSet.AWESOME));
		fastForwardButton.setOnAction(actionEvent -> changePage(+10));
		// add help button
		final Button helpButton = new Button("", new TextIcon("circle-question", TextIcon.IconSet.AWESOME));
		helpButton.setOnAction(actionEvent -> infoPopup.showPopup(helpButton, InfoPopup.Pos.BELOW_LEFT, true));
		toolBar.getItems().addAll(homeButton, fastBackwardButton, backwardButton, pageChoice, forwardButton, fastForwardButton,
									new Separator(), helpButton);
		toolBar.setOnKeyPressed(keyEvent -> {
			if (keyEvent.getEventType() == KeyEvent.KEY_PRESSED) {
				final KeyCode key = keyEvent.getCode();
				if (keyEvent.isControlDown()) {
					if (key == KeyCode.PAGE_UP) {
						changePage(-10);
					} else if (key == KeyCode.PAGE_DOWN) {
						changePage(+10);
					}
				} else {
					if (key == KeyCode.PAGE_UP) {
						changePage(-1);
					} else if (key == KeyCode.PAGE_DOWN) {
						changePage(+1);
					} else if (key == KeyCode.HOME) {
						gotoPage(0);
					} else if (key == KeyCode.END) {
						gotoPage(pageList.size() - 1);
					}
				}
			}
		});	
		mainPane.setTop(toolBar);
		
		// add root list on the left
		final VBox rootBox = new VBox();
		final HBox searchBox = new HBox();
		searchBox.setPadding(new Insets(3));
		searchBox.setSpacing(3);
		final PaliTextInput searchInput = new PaliTextInput(PaliTextInput.InputType.FIELD);
		searchTextField = (TextField)searchInput.getInput();
		searchTextField.setPromptText("Search for...");
		searchTextField.setPrefWidth(Utilities.getRelativeSize(12));
		searchTextField.textProperty().addListener((obs, oldValue, newValue) -> updateRootList());
		searchTextField.setOnKeyPressed(keyEvent -> {
			if (keyEvent.getEventType() == KeyEvent.KEY_PRESSED) {
				final KeyCode key = keyEvent.getCode();
				if (keyEvent.isControlDown()) {
					if (key == KeyCode.SPACE) {
						searchInput.rotateInputMethod();
					}					
				} else {
					if (key == KeyCode.ESCAPE) {
						searchTextField.clear();
					}
				}
			}
		});	
		searchInput.setSanskritMode(true);
		final String inputMethod = Utilities.getSetting("sanskrit-input-method");
		searchInput.setInputMethod(PaliTextInput.InputMethod.valueOf(inputMethod));
		searchBox.getChildren().addAll(searchTextField, searchInput.getMethodButton());
		rootListView.setPrefWidth(Utilities.getRelativeSize(15));
		rootListView.setCellFactory((ListView<StringPair> lv) -> {
			return new ListCell<StringPair>() {
				@Override
				public void updateItem(StringPair item, boolean empty) {
					super.updateItem(item, empty);
					this.setGraphic(null);
					if (empty) {
						this.setText(null);
						this.setTooltip(null);
					} else {
						final StringPair value = this.getItem();
						this.setText(value.getSecond());
					}
				}
			};
		});
		rootListView.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> {
			final StringPair selItem = newValue;
			if (selItem != null) {
				pageChoice.getSelectionModel().select(selItem.getFirst());
			}
		});
		VBox.setVgrow(rootListView, Priority.ALWAYS);	
		rootBox.getChildren().addAll(searchBox, rootListView);
		mainPane.setLeft(rootBox);

		// init page view
		pageDisplay.setFitWidth(622);
		pageDisplay.setPreserveRatio(true);
		pageDisplay.setSmooth(true);
		pageDisplay.setCache(true);

		scrollPane.setContent(pageDisplay);
		mainPane.setCenter(scrollPane);
		final Scene scene = new Scene(mainPane, windowWidth, windowHeight);
		setScene(scene);

		// intit page list
		for (int i = 3; i <= 14; i++) {
			pageList.add(String.format("f%03d", i));
		}
		for (int i = 1; i <= 250; i++) {
			pageList.add(String.format("p%03d", i));
		}
		pageChoice.getItems().addAll(pageList);
		pageChoice.getSelectionModel().select(0);

		// prepare info popup
		infoPopup.setContentWithText(SanskritUtilities.getTextResource("info-whitney-roots.txt"));
		infoPopup.setTextWidth(Utilities.getRelativeSize(32));
	
		// some init
		SanskritUtilities.loadWhitneyRootsIndex();
		updateRootList();
		whitneyZip = new File(SanskritUtilities.WHITNEY_ROOTS_ZIP);
		gotoPage(0);
	}

	private void updateRootList() {
		final String strQuery = Normalizer.normalize(searchTextField.getText().trim(), Form.NFC).toLowerCase();
		rootList.clear();
		final List<StringPair> workList = strQuery.isEmpty()
											? SanskritUtilities.whitneyRootsList
											: SanskritUtilities.whitneyRootsList.stream()
												.filter(x -> x.getSecond().indexOf(strQuery) > -1)
												.collect(Collectors.toList());
		rootList.addAll(workList);
		rootListView.scrollTo(0);
		rootListView.getSelectionModel().clearSelection();
	}

	private void updatePage() {
		final String pname = pageChoice.getSelectionModel().getSelectedItem();
		if (pname == null) return;
		updatePage(pname);
	}

	private void updatePage(final String pname) {
		currIndex = pageList.indexOf(pname);
		loadPage(pname);
	}

	private void gotoPage(final int index) {
		currIndex = index;
		final String pname = pageList.get(currIndex);
		pageChoice.getSelectionModel().select(pname);
		loadPage(pname);
	}

	private void changePage(final int step) {
		if (step < 0) {
			if (currIndex + step >= 0)
				gotoPage(currIndex += step);
			else
				gotoPage(0);
		} else {
			if (currIndex + step < pageList.size())
				gotoPage(currIndex += step);
			else
				gotoPage(pageList.size() - 1);
		}
	}

	private void loadPage(final String pname) {
		try {
			final ZipFile zip = new ZipFile(whitneyZip);
			final ZipEntry entry = zip.getEntry("whitney_roots/" + pname + ".png");
			pageDisplay.setImage(new Image(zip.getInputStream(entry)));
			zip.close();
			scrollPane.setVvalue(scrollPane.getVmin());
		} catch (IOException e) {
			System.err.println(e);
		}
	}

}
