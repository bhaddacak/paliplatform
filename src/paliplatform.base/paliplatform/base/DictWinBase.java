/*
 * DictWinBase.java
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

package paliplatform.base;

import java.util.*;
import java.util.stream.Collectors;
import java.util.concurrent.CompletableFuture;
import java.util.ServiceLoader.Provider;
import java.text.Normalizer;
import java.text.Normalizer.Form;

import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.concurrent.Worker;
import javafx.application.Platform;
import netscape.javascript.JSObject;

/**
 * The base class of dictionary window's pane.
 * @author J.R. Bhaddacak
 * @version 3.5
 * @since 3.5
 */
public abstract class DictWinBase extends BorderPane {
	protected final PaliTextInput searchInput = new PaliTextInput(PaliTextInput.InputType.COMBO);
	protected final ComboBox<String> searchComboBox;
	protected final TextField searchTextField;
	protected Set<Object> dictSet = new HashSet<>();
	protected Map<Object, CheckBox> dictCBMap = new HashMap<>();
	protected SimpleService editorLauncher = null; // init needed
	protected FXHandler fxHandler = null; // init needed
	protected final ObservableList<String> resultList = FXCollections.<String>observableArrayList();
	protected final ListView<String> resultListView = new ListView<>(resultList);
	protected final Map<String, ArrayList<Object>> resultMap = new LinkedHashMap<>();
	protected final SimpleBooleanProperty incremental = new SimpleBooleanProperty(false);
	protected final SimpleBooleanProperty useWildcards = new SimpleBooleanProperty(false);
	protected final SimpleBooleanProperty inMeaning = new SimpleBooleanProperty(false);
	protected final SimpleBooleanProperty searchResultTextFound = new SimpleBooleanProperty(false);
	protected final BorderPane resultPane = new BorderPane();
	protected final HtmlViewer htmlViewer = new HtmlViewer();
	protected final SimpleFindBox findBox = new SimpleFindBox(this);
	protected final TextField findInput = findBox.getFindTextField();
	protected final Button editorButton;
	protected final CommonWorkingToolBar toolBar;
	protected final InfoPopup infoPopup = new InfoPopup();
	protected String initialStringToLocate = "";

	public DictWinBase() {
		final VBox mainBox = new VBox();
		searchTextField = (TextField)searchInput.getInput();
		// add toolbar on the top
		toolBar = new CommonWorkingToolBar(this, searchTextField);
		// add a new button
		editorButton = new Button("", new TextIcon("pencil", TextIcon.IconSet.AWESOME));
		editorButton.setTooltip(new Tooltip("Open result in editor"));
		editorButton.setOnAction(actionEvent -> {
			copyText();
			final Clipboard cboard = Clipboard.getSystemClipboard();
			final String text = cboard.hasString() ? cboard.getString() : "";
			if (text.isEmpty()) return;
			final Object[] argsEditor = new Object[] { text };
			editorLauncher.processArray(argsEditor);
		});
		toolBar.getItems().addAll(editorButton, new Separator());
		// config some buttons
		toolBar.getThemeButton().setOnAction(actionEvent -> {
			final Utilities.Theme theme = toolBar.resetTheme();
			setViewerTheme(theme.toString());
		});
		toolBar.getZoomInButton().setOnAction(actionEvent -> {
			htmlViewer.webView.setFontScale(htmlViewer.webView.getFontScale() + 0.10);
		});
		toolBar.getZoomOutButton().setOnAction(actionEvent -> {
			htmlViewer.webView.setFontScale(htmlViewer.webView.getFontScale() - 0.10);
		});
		toolBar.getResetButton().setOnAction(actionEvent -> {
			htmlViewer.webView.setFontScale(1.0);
		});
		toolBar.saveTextButton.setOnAction(actionEvent -> saveText());
		toolBar.copyButton.setOnAction(actionEvent -> copyText());
		setTop(toolBar);

		// add main content
		// add search toolbar
		final ToolBar searchToolBar = new ToolBar();
		searchComboBox = searchInput.getComboBox();
		searchComboBox.setPromptText("Search for...");
		searchComboBox.setOnShowing(e -> recordQuery());
		searchComboBox.setOnKeyPressed(keyEvent -> {
			if (keyEvent.getEventType() == KeyEvent.KEY_PRESSED) {
				final KeyCode key = keyEvent.getCode();
				if (key == KeyCode.ENTER) {
					search();
				} else if (key == KeyCode.ESCAPE) {
					searchComboBox.getEditor().clear();
				}
			}
		});
		searchTextField.textProperty().addListener((obs, oldValue, newValue) -> {
			if (incremental.get()) {
				submitSearch(newValue);
			}
		});
		final Button clearButton = searchInput.getClearButton();
		clearButton.setOnAction(actionEvent -> {
			recordQuery();
			searchTextField.clear();
			searchComboBox.commitValue();
		});
		final Button searchButton = new Button("Search");
		searchButton.disableProperty().bind(incremental);
		searchButton.setOnAction(actionEvent -> search());
		final CheckBox incrementalButton = new CheckBox("Incremental");
		incrementalButton.setTooltip(new Tooltip("Search immediately while entering"));
		incrementalButton.selectedProperty().bindBidirectional(incremental);
		incrementalButton.disableProperty().bind(inMeaning.or(useWildcards));
		final CheckBox wildcardButton = new CheckBox("Use */?");
		wildcardButton.setTooltip(new Tooltip("Use wildcards (*/?)"));
		wildcardButton.selectedProperty().bindBidirectional(useWildcards);
		wildcardButton.disableProperty().bind(inMeaning.or(incremental));
		final CheckBox inMeaningButton = new CheckBox("In meaning");
		inMeaningButton.setTooltip(new Tooltip("Search in meaning"));
		inMeaningButton.selectedProperty().bindBidirectional(inMeaning);
		inMeaningButton.disableProperty().bind(useWildcards.or(incremental));
		// find buttons
		final MenuButton findMenu = new MenuButton("", new TextIcon("magnifying-glass", TextIcon.IconSet.AWESOME));
		findMenu.setTooltip(new Tooltip("Find in the result"));
		final MenuItem findMenuItem = new MenuItem("Find");
		findMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.F, KeyCombination.SHORTCUT_DOWN));
		findMenuItem.setOnAction(actionEvent -> openResultFind());
		final MenuItem findNextMenuItem = new MenuItem("Find Next");
		findNextMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.G, KeyCombination.SHORTCUT_DOWN));
		findNextMenuItem.disableProperty().bind(searchResultTextFound.not());		
		findNextMenuItem.setOnAction(actionEvent -> findResultNext(+1));
		final MenuItem findPrevMenuItem = new MenuItem("Find Previous");
		findPrevMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.G, KeyCombination.SHORTCUT_DOWN, KeyCombination.SHIFT_DOWN));
		findPrevMenuItem.disableProperty().bind(searchResultTextFound.not());		
		findPrevMenuItem.setOnAction(actionEvent -> findResultNext(-1));
		findMenu.getItems().addAll(findMenuItem, findNextMenuItem, findPrevMenuItem);
		// help button
		final Button helpButton = new Button("", new TextIcon("circle-question", TextIcon.IconSet.AWESOME));
		helpButton.setOnAction(actionEvent -> infoPopup.showPopup(helpButton, InfoPopup.Pos.BELOW_RIGHT, true));
		searchToolBar.getItems().addAll(searchComboBox, clearButton, searchInput.getMethodButton(), searchButton,
										new Separator(), incrementalButton, wildcardButton, inMeaningButton,
										new Separator(), findMenu, helpButton);

		// add result split pane
		final SplitPane splitPane = new SplitPane();
		splitPane.setDividerPositions(0.22);
		// add result list on the left
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
						final String value = this.getItem();
						final String strBooks = resultMap.get(value).stream().map(x -> x.toString()).collect(Collectors.joining(", "));
						this.setTooltip(new Tooltip(value + " (" + strBooks + ")"));
						this.setText(value);
					}
					this.setStyle("-fx-padding: 0px 0px 0px 3px");
				}
			};
		});
		resultListView.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> {
			final String term = newValue;
			if (term != null)
				showResult(term);
		});
		// setup HtmlViewer to show result on the right
		htmlViewer.simpleSetup();
		// Set the member for the browser's window object after the document loads
	 	htmlViewer.webEngine.getLoadWorker().stateProperty().addListener((prop, oldState, newState) -> {
			if (newState == Worker.State.SUCCEEDED) {
				final JSObject jsWindow = (JSObject)htmlViewer.webEngine.executeScript("window");
				jsWindow.setMember("fxHandler", fxHandler);
				setViewerTheme(toolBar.getTheme().toString());
				toolBar.resetFont();
				if (!initialStringToLocate.isEmpty())
					findSingle(initialStringToLocate);
			}
		});
		htmlViewer.setContent(Utilities.makeHTML(""));
		resultPane.setCenter(htmlViewer);
		// config Find Box for the result
		findBox.getFindTextInput().setInputMethod(PaliTextInput.InputMethod.NORMAL);
		findBox.getPrevButton().disableProperty().bind(searchResultTextFound.not());
		findBox.getPrevButton().setOnAction(actionEvent -> findResultNext(-1));
		findBox.getNextButton().disableProperty().bind(searchResultTextFound.not());
		findBox.getNextButton().setOnAction(actionEvent -> findResultNext(+1));
		findBox.getCloseButton().setOnAction(actionEvent -> resultPane.setBottom(null));
		findBox.getClearFindButton().setOnAction(actionEvent -> clearFindInput());
		findInput.setOnKeyPressed(keyEvent -> {
			if (keyEvent.getEventType() == KeyEvent.KEY_PRESSED) {
				final KeyCode key = keyEvent.getCode();
				if (key == KeyCode.ENTER) {
					if (keyEvent.isShiftDown())
						findResultNext(-1);
					else
						findResultNext(+1);
				} else if (key == KeyCode.ESCAPE) {
					clearFindInput();
				} else if (key == KeyCode.SPACE && keyEvent.isControlDown()) {
					findBox.getFindTextInput().rotateInputMethod();
				}
			}
		});
		findInput.textProperty().addListener((obs, oldValue, newValue) -> {
			if (!newValue.isEmpty()) {
				final String query = processInput(Normalizer.normalize(newValue, Form.NFC));
				findResultNext(query, +1);
			}
		});
		// add find box first, and remove it (in init); this makes the focus request possible		
		resultPane.setBottom(findBox);
		splitPane.getItems().addAll(resultListView, resultPane);

		mainBox.getChildren().addAll(searchToolBar, splitPane);
		setCenter(mainBox);
		setPrefWidth(Utilities.getRelativeSize(62));
		setPrefHeight(Utilities.getRelativeSize(45));

		// set up drop event
		this.setOnDragOver(dragEvent -> {
			if (dragEvent.getGestureSource() != this && dragEvent.getDragboard().hasString())
				dragEvent.acceptTransferModes(TransferMode.COPY_OR_MOVE);
			dragEvent.consume();
		});
		this.setOnDragDropped(dragEvent -> {
			final Dragboard db = dragEvent.getDragboard();
			if (db.hasString()) {
				final String[] allStr = db.getString().split("\\n");
				final String head = allStr[0];
				final String term = head.trim().split("\\s")[0];
				searchTextField.setText(term);
				dragEvent.setDropCompleted(true);
			} else {
				dragEvent.setDropCompleted(false);
			}
			dragEvent.consume();
		});
	}

	public abstract void init(final Object[] args);
	public abstract void search(final String query);
	public abstract void showResult(final String term);
	public abstract String processInput(final String query);

	public void setSearchInput(final String query) {
		htmlViewer.setContent(Utilities.makeHTML(""));
		searchTextField.setText(query);
	}

	private void searchDict(final String query) {
		final String term = processInput(query);
		Platform.runLater(() -> {
			if (!term.isEmpty())
				search(term);
			searchComboBox.commitValue();
		});
	}
	
	private void submitSearch(final String query) {
		final String term = Normalizer.normalize(query, Form.NFC);
		CompletableFuture.runAsync(() -> searchDict(term), Utilities.threadPool);
	}

	protected void search() {
		final String strQuery = searchTextField.getText().trim();
		if (!strQuery.isEmpty())
			submitSearch(strQuery);
	}

	private void recordQuery() {
		if (!resultMap.isEmpty())
			searchInput.recordQuery();
	}

	public void showFindMessage(final String text) {
		findBox.showMessage(text);
	}
	
	public void setSearchTextFound(final boolean yn) {
		searchResultTextFound.set(yn);
	}
	
	private void openResultFind() {
		resultPane.setBottom(findBox);
		if (!initialStringToLocate.isEmpty()) {
			findInput.setText(initialStringToLocate);
			findInput.selectAll();
		}
		findInput.requestFocus();		
	}

	private void findResultNext(final int direction) {
		final String strInput = findInput.getText();
		if (!strInput.isEmpty()) {
			final String query = processInput(Normalizer.normalize(strInput, Form.NFC));
			findResultNext(query, direction);
		}
	}

	private void findResultNext(final String query, final int direction) {
		final int caseSensitive = findBox.isCaseSensitive() ? 1 : 0;
		htmlViewer.webEngine.executeScript("findNext('" + query + "'," + caseSensitive + "," + direction + ")");
	}

	private void findSingle(final String query) {
		htmlViewer.webEngine.executeScript("findSingleQuiet('" + query + "'" + ")");
	}

	private void clearFindInput() {
		findInput.clear();
		searchResultTextFound.set(false);
	}	
	
	private void setViewerTheme(final String theme) {
		final String command = "setThemeCommon('"+theme+"');";
		htmlViewer.webEngine.executeScript(command);
	}

	public void setViewerFont(final String fontname) {
		htmlViewer.webEngine.executeScript("setFont('[\"" + fontname + "\"]')");
	}

	private void copyText() {
		htmlViewer.webEngine.executeScript("copyBody()");
	}

	private void saveText() {
		htmlViewer.webEngine.executeScript("saveBody()");
	}

}

