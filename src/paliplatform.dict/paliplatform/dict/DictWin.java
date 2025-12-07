/*
 * DictWin.java
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

package paliplatform.dict;

import paliplatform.dict.DictUtilities.DictBook;
import paliplatform.base.*;

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
 * The main dictionary window's pane.
 * @author J.R. Bhaddacak
 * @version 3.5
 * @since 2.0
 */
public class DictWin extends BorderPane {
	private final PaliTextInput searchInput = new PaliTextInput(PaliTextInput.InputType.COMBO);
	private final ComboBox<String> searchComboBox;
	private final TextField searchTextField;
	private final Map<DictBook, CheckBox> dictCBMap = new EnumMap<>(DictBook.class);
	private final Set<DictBook> dictSet = EnumSet.noneOf(DictBook.class);
	private final ObservableList<String> resultList = FXCollections.<String>observableArrayList();
	private final ListView<String> resultListView = new ListView<>(resultList);
	private final Map<String, ArrayList<DictBook>> resultMap = new LinkedHashMap<>();
	private final SimpleBooleanProperty incremental = new SimpleBooleanProperty(false);
	private final SimpleBooleanProperty useWildcards = new SimpleBooleanProperty(false);
	private final SimpleBooleanProperty inMeaning = new SimpleBooleanProperty(false);
	private final SimpleBooleanProperty searchResultTextFound = new SimpleBooleanProperty(false);
	private final BorderPane resultPane = new BorderPane();
	private final HtmlViewer htmlViewer = new HtmlViewer();
	private final SimpleFindBox findBox = new SimpleFindBox(this);
	private final TextField findInput = findBox.getFindTextField();
	private final InfoPopup infoPopup = new InfoPopup();
	private String initialStringToLocate = "";

	public DictWin(final Object[] args) {
		if (DictUtilities.simpleServiceMap == null) 
			DictUtilities.simpleServiceMap = DictUtilities.getSimpleServices();
		// initialize dict checkboxes
		for (final DictBook d : DictBook.books) {
			dictCBMap.put(d, createDictCheckBox(d));
		}
		// initialize web engine
	 	htmlViewer.webEngine.getLoadWorker().stateProperty().addListener((prop, oldState, newState) -> {
			if (newState == Worker.State.SUCCEEDED) {
				if (!initialStringToLocate.isEmpty())
					findSingle(initialStringToLocate);
			}
		});		

		final VBox mainBox = new VBox();
		searchTextField = (TextField)searchInput.getInput();
		// add toolbar on the top
		final CommonWorkingToolBar toolBar = new CommonWorkingToolBar(this, searchTextField);
		// add a new button
		final SimpleService editorLauncher = (SimpleService)DictUtilities.simpleServiceMap.get("paliplatform.main.EditorLauncher");
		if (editorLauncher != null) {
			final Button editorButton = new Button("", new TextIcon("pencil", TextIcon.IconSet.AWESOME));
			editorButton.setTooltip(new Tooltip("Open result in editor"));
			editorButton.setOnAction(actionEvent -> {
				copyText();
				final Clipboard cboard = Clipboard.getSystemClipboard();
				final String text = cboard.hasString() ? cboard.getString() : "";
				if (text.isEmpty()) return;
				final Object[] argsEditor = new Object[2];
				argsEditor[0] = "ROMAN";
				argsEditor[1] = text;
				editorLauncher.processArray(argsEditor);
			});
			toolBar.getItems().addAll(editorButton, new Separator());
		}
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
		for (final DictBook db : DictBook.books)
			toolBar.getItems().add(dictCBMap.get(db));
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
		htmlViewer.setStyleSheet(Utilities.DICT_CSS);
		// Set the member for the browser's window object after the document loads
		final DictFXHandler fxHandler = new DictFXHandler(htmlViewer, this);
	 	htmlViewer.webEngine.getLoadWorker().stateProperty().addListener((prop, oldState, newState) -> {
			if (newState == Worker.State.SUCCEEDED) {
				final JSObject jsWindow = (JSObject)htmlViewer.webEngine.executeScript("window");
				jsWindow.setMember("fxHandler", fxHandler);
				setViewerTheme(toolBar.getTheme().toString());
				toolBar.resetFont();
			}
		});
		htmlViewer.setContent(DictUtilities.makeHTML(""));
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
				final String query = Utilities.convertToRomanPali(Normalizer.normalize(newValue, Form.NFC));
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
		
		// set initial values, used also when reopening the window
		init(args);

		// prepare info popup
		infoPopup.setContentWithText(DictUtilities.getTextResource("info-dictionaries.txt"));
		infoPopup.setTextWidth(Utilities.getRelativeSize(37.5));
	}

	public final void init(final Object[] args) {
		dictSet.clear();
		final String strDictSet = Utilities.settings.getProperty("dictset");
		final String[] arrDictSet = strDictSet.split(",");
		for (final DictBook db : DictBook.books)
			dictCBMap.get(db).setSelected(false);
		for (final String s : arrDictSet) {
			if (DictBook.isValid(s)) {
				final DictBook db = DictBook.valueOf(s);
				dictSet.add(db);
				dictCBMap.get(db).setSelected(true);
			}
		}
		htmlViewer.setContent(DictUtilities.makeHTML(""));
		if (args != null) {
			final String term = (String)args[0];
			if (!term.isEmpty())
				searchTextField.setText(term);
		} else {
			searchTextField.clear();
			resultList.clear();
			resultMap.clear();
		}
		if (!DictUtilities.someDictAvailable.get())
			Utilities.displayAlert(Alert.AlertType.ERROR, "No data to display,\nplease create dict data first");
		findBox.init();
		Platform.runLater(() -> resultPane.setBottom(null));			
	}

	private CheckBox createDictCheckBox(final DictBook book) {
		final CheckBox cb = DictUtilities.createDictCheckBox(book);
		cb.disableProperty().bind(DictUtilities.dictAvailMap.get(book).not());
		cb.setOnAction(actionEvent -> {
			if (cb.isSelected())
				dictSet.add(book);
			else
				dictSet.remove(book);
			search();
		});
		return cb;
	}

	public void setSearchInput(final String query) {
		htmlViewer.setContent(DictUtilities.makeHTML(""));
		searchTextField.setText(query);
	}

	private void searchDict(final String value) {
		final String strQuery = Normalizer.normalize(value.trim(), Form.NFC);
		final String term = Utilities.convertToRomanPali(strQuery);
		Platform.runLater(() -> {
			if (!term.isEmpty())
				search(term);
			searchComboBox.commitValue();
		});
	}
	
	private void submitSearch(final String query) {
		CompletableFuture.runAsync(() -> searchDict(query), Utilities.threadPool);
	}

	private void search() {
		final String strQuery = Normalizer.normalize(searchTextField.getText().trim(), Form.NFC);
		final String term = Utilities.convertToRomanPali(strQuery);
		if (!term.isEmpty())
			submitSearch(term);
	}

	private void search(final String query) {
		resultMap.clear();
		// remove single qoute causing SQL error
		final String properQuery = query.replace("'", "");
		String termQuery = properQuery.replace("'", "");
		if (useWildcards.get()) {
			if (termQuery.indexOf('?') >= 0)
				termQuery = termQuery.replace("?", "_");
			if (termQuery.indexOf('*') >= 0)
				termQuery = termQuery.replace("*", "%");
			final int uCount = Utilities.charCount(termQuery, '_');
			final int pCount = Utilities.charCount(termQuery, '%');
			// just * or a sheer combination of ? and * is not allowed
			if (termQuery.length() == 0 || (pCount > 0 && pCount + uCount == termQuery.length()))
				return;
		}
		for (final DictBook dicBook : dictSet) {
			final String orderBy = "ORDER BY ID;";
			final String tail = useWildcards.get() ? "' " + orderBy : "%' " + orderBy;
			String dbQuery = dicBook == DictBook.MDPD
							? "SELECT TERM FROM " + Utilities.PpdpdTable.DICTIONARY.toString() + " WHERE TERM LIKE '" + termQuery + tail
							: dicBook == DictBook.CONE
								? "SELECT TERM FROM DICT WHERE TERM LIKE '%|" + termQuery + tail
								: "SELECT TERM FROM " + dicBook.toString() + " WHERE TERM LIKE '" + termQuery + tail;
			if (inMeaning.get()) {
				if (dicBook == DictBook.CPED) {
					dbQuery = "SELECT TERM FROM CPED WHERE MEANING LIKE '%" + properQuery + "%' " +
							"OR SUBMEANING LIKE '%" + properQuery + "%' OR POS LIKE '%" + properQuery + "%' " +
							"ORDER BY ID;";
				} else if (dicBook == DictBook.MDPD) {
					dbQuery = "SELECT LEMMA_1 FROM " + Utilities.PpdpdTable.MINIDPD.toString() +
							" WHERE MEANING_1 LIKE '%" + properQuery + "%' " +
							"OR MEANING_2 LIKE '%" + properQuery + "%' OR MEANING_LIT LIKE '%" + properQuery + "%' " +
							"ORDER BY ID;";
				} else if (dicBook == DictBook.CONE) {
					dbQuery = "SELECT TERM FROM DICT WHERE MEANING LIKE '%" + properQuery + "%' ORDER BY ID;";
				} else {
					final String mfield = dicBook == DictBook.NCPED ? "DEFINITION" : "MEANING";
					dbQuery = "SELECT TERM FROM " + dicBook.toString() + " WHERE " + mfield + " LIKE '%" + properQuery + "%' ORDER BY ID;";
				}
			}
			final Set<String> results = dicBook == DictBook.MDPD
										? Utilities.getFirstColumnFromDB(Utilities.H2DB.PPDPD, dbQuery)
										: dicBook == DictBook.CONE
											? Utilities.getFirstColumnFromDB(Utilities.H2DB.CONE, dbQuery)
											: Utilities.getFirstColumnFromDB(Utilities.H2DB.DICT, dbQuery);
			for (final String term : results) {
				final String termOK = term.startsWith("|") ? term.substring(1) : term;
				final ArrayList<DictBook> dList;
				if (resultMap.containsKey(termOK))
					dList = resultMap.get(termOK);
				else
					dList = new ArrayList<>();
				dList.add(dicBook);
				resultMap.put(termOK, dList);
			}
		} // end for
		if (dictSet.size() > 1)
			resultList.setAll(resultMap.keySet().stream().sorted(Utilities.paliComparator).collect(Collectors.toList()));
		else
			resultList.setAll(resultMap.keySet());
		resultListView.scrollTo(0);
		if (!resultList.isEmpty()) {
			showResult(resultList.get(0));
			initialStringToLocate = inMeaning.get() ? properQuery : "";
		}
	}

	private void recordQuery() {
		if (!resultMap.isEmpty())
			searchInput.recordQuery();
	}

	private void showResult(final String term) {
		final StringBuilder result = new StringBuilder();
		final ArrayList<DictBook> dicts = resultMap.get(term);
		if (dicts == null) 	
			return;
		// show head word
		result.append("<h1>").append(term).append("</h1>");
		for (int i = 0; i < dicts.size(); i++) {
			final DictBook db = dicts.get(i);
			result.append("<p class=bookname>&lt;").append(db.bookName).append("&gt;</p>");
			if (db == DictBook.CPED) {
				result.append(getCPEDArticle(DictUtilities.lookUpCPEDFromDB(term)));
			} else if (db == DictBook.NCPED) {
				result.append(DictUtilities.formatNCPEDMeaning(DictUtilities.lookUpNCPEDFromDB(term)));
			} else if (db == DictBook.MDPD) {
				if (inMeaning.get()) {
					// search in minidpd with lemma_1
					result.append(DictUtilities.formatMDPDMeaning(DictUtilities.lookUpMDPDFromDBWithLemma(term)));
				} else {
					// search in minidpd with a list of term ids
					result.append(DictUtilities.formatMDPDMeaning(DictUtilities.lookUpMDPDFromDBWithTerm(term)));
				}
			} else {
				final String res = db == DictBook.CONE
									? getConeResultArticle(DictUtilities.lookUpConeFromDB(term))
									: getResultArticle(DictUtilities.lookUpDictFromDB(db, term));
				if (db == DictBook.PTSD)
					result.append(res.replaceAll("href=[^>]*", ""));
				else if (db == DictBook.DPPN)
					result.append(res.replace("<hr>", ""));
				else
					result.append(res);
			}
			if (i < dicts.size() - 1)
				result.append("<div class=hrule></div><p></p>");
		}
		htmlViewer.setContent(DictUtilities.makeHTML(result.toString()));
	}

	private String getCPEDArticle(final PaliWord item) {
		String result = DictUtilities.formatCPEDMeaning(item, true);
		if (item.isDeclinable()) {
			result = result + "<p><a class=linkbutton onClick=openDeclension('"+item.getTerm()+"')>Show declension</a></p>";
		}
		return result;
	}

	private String getResultArticle(final List<String> items) {
		final StringBuilder text = new StringBuilder();
		for (final String s : items) {
			String sOK = s.replace("&apos;", "'");
			text.append(sOK);
			text.append("<p></p>");
		}
		return text.toString();
	}

	private String getConeResultArticle(final List<String> items) {
		final StringBuilder text = new StringBuilder();
		for (final String s : items) {
			String sOK = s.replace("\\n", "");
			sOK = sOK.replace("\\t", "\t");
			sOK = sOK.replace("—", "<br>&nbsp;&nbsp;&nbsp;&nbsp;—");
			text.append(sOK);
			text.append("<p></p>");
		}
		return text.toString();
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
			final String query = Utilities.convertToRomanPali(Normalizer.normalize(strInput, Form.NFC));
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
