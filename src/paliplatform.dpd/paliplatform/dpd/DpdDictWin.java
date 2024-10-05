/*
 * DpdDictWin.java
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
import static paliplatform.dpd.DpdUtilities.SearchMethod;

import java.util.*;
import java.util.stream.Collectors;
import java.util.concurrent.CompletableFuture;
import java.text.Normalizer;
import java.text.Normalizer.Form;
import java.sql.*;

import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.image.*;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.scene.text.*;
import javafx.geometry.*;
import javafx.beans.property.*;
import javafx.concurrent.Task;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.application.Platform;

import com.google.gson.Gson;

/** 
 * The main dictionary of DPD with selected information.
 * This is a singleton.
 * @author J.R. Bhaddacak
 * @version 3.0
 * @since 3.0
 */
public class DpdDictWin extends SingletonWindow {
	public static final DpdDictWin INSTANCE = new DpdDictWin();
	private final BorderPane mainPane = new BorderPane();
	private final ScrollPane scrollPane = new ScrollPane();
	private final VBox detailBox = new VBox();
	private final HBox statusBox = new HBox(5);
	private final ProgressBar progressBar = new ProgressBar();
	private final Label message = new Label();
	private final ToggleGroup searchMethodGroup = new ToggleGroup();
	private final CheckMenuItem showWordFamilyMenuItem = new CheckMenuItem("Show word family");
	private final CheckMenuItem showIdiomFamilyMenuItem = new CheckMenuItem("Show idiom family");
	private final InfoPopup infoPopup = new InfoPopup();
	private final InfoPopup helpPopup = new InfoPopup();
	private final TextField searchTextField;
	private final Map<String, List<String>> resultMap = new HashMap<>();
	private final ObservableList<String> resultList = FXCollections.<String>observableArrayList();
	private final ListView<String> termListView = new ListView<>(resultList);
	private final ToggleButton deconButton = new ToggleButton("", new TextIcon("hammer", TextIcon.IconSet.AWESOME));
	private final Button infoButton = new Button("", new TextIcon("circle-info", TextIcon.IconSet.AWESOME));
	private final SimpleBooleanProperty isCreating = new SimpleBooleanProperty(false);
	private final SimpleBooleanProperty isSearching = new SimpleBooleanProperty(false);
	private final Gson gson;
	private int maxResultCount = DpdUtilities.DEF_MAX_RESULT;
	private String dpdInfo = "";
	private String currTerm = "";
	
	private DpdDictWin() {
		windowWidth = Utilities.getRelativeSize(58);
		setTitle("DPD Dictionary");
		getIcons().add(new Image(DpdDictWin.class.getResourceAsStream("resources/images/dpd.png")));
		
		// add main toolbar
		final CommonWorkingToolBar toolBar = new CommonWorkingToolBar(detailBox);
		// config some buttons
		toolBar.saveTextButton.setTooltip(new Tooltip("Save data as text"));
		toolBar.saveTextButton.setOnAction(actionEvent -> saveText());		
		toolBar.copyButton.setTooltip(new Tooltip("Copy text to clipboard"));
		toolBar.copyButton.setOnAction(actionEvent -> copyText());		
		// add new components
		final Button genTableButton = new Button("Generate", new TextIcon("screwdriver-wrench", TextIcon.IconSet.AWESOME));
		genTableButton.disableProperty().bind(DpdUtilities.ppdpdDBLocked.or(isCreating).or(isSearching));
		genTableButton.setTooltip(new Tooltip("Generate dictionary tables"));
		genTableButton.setOnAction(actionEvent -> buildDictTable());
		// help button
		infoButton.setTooltip(new Tooltip("DPD information"));
		infoButton.setOnAction(actionEvent -> showDpdInfo());
		final Button helpButton = new Button("", new TextIcon("circle-question", TextIcon.IconSet.AWESOME));
		helpButton.setOnAction(actionEvent -> helpPopup.showPopup(helpButton, InfoPopup.Pos.BELOW_RIGHT, true));
		toolBar.getItems().addAll(new Separator(), genTableButton, infoButton, helpButton);
		// add second tool bar
		final ToolBar searchToolBar = new ToolBar();
		final PaliTextInput searchTextInput = new PaliTextInput(PaliTextInput.InputType.FIELD);
		searchTextField = (TextField)searchTextInput.getInput();
		searchTextField.setPromptText("Search for...");
		searchTextField.disableProperty().bind(isSearching);
		searchTextField.textProperty().addListener((obs, oldValue, newValue) -> {
			final SearchMethod method = (SearchMethod)searchMethodGroup.getSelectedToggle().getUserData();
			final String query = Normalizer.normalize(newValue.trim(), Form.NFC);
			if (!query.isEmpty() && (method == SearchMethod.TERM_START || query.length() >= 2)) {
				message.setText(DpdUtilities.RETRIEVING);
				isSearching.set(true);
				CompletableFuture.runAsync(() -> showResult(query), Utilities.threadPool);
			}
		});
		searchTextField.setOnKeyPressed(keyEvent -> {
			if (keyEvent.getEventType() == KeyEvent.KEY_PRESSED) {
				final KeyCode key = keyEvent.getCode();
				if (key == KeyCode.ESCAPE) {
					searchTextField.clear();
				}
			}
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
		maxResultChoice.setTooltip(new Tooltip("Maximum result"));
		maxResultChoice.getItems().addAll(DpdUtilities.MAXLIST);
		maxResultChoice.getSelectionModel().select(1);
		maxResultChoice.setOnAction(actionEvent -> {
			maxResultCount = maxResultChoice.getSelectionModel().getSelectedItem();
			showResult();
		});
		deconButton.setTooltip(new Tooltip("Also search in deconstructor on/off"));
		deconButton.disableProperty().bind(Utilities.ppdpdAvailMap.get(Utilities.PpdpdTable.DECONSTRUCTOR).not());
		deconButton.setOnAction(actionEvent -> showResult());
		searchToolBar.getItems().addAll(searchTextField, searchTextInput.getClearButton(), searchTextInput.getMethodButton(),
										searchOptionsMenu, maxResultChoice, deconButton);
		final VBox toolBarBox = new VBox();
		toolBarBox.getChildren().addAll(toolBar, searchToolBar);
		mainPane.setTop(toolBarBox);

		// add term list on the left
		termListView.setCellFactory((ListView<String> lv) -> {
			return new ListCell<String>() {
				@Override
				public void updateItem(String item, boolean empty) {
					super.updateItem(item, empty);
					this.setGraphic(null);
					if (empty) {
						this.setText(null);
						this.setTooltip(null);
					} else {
						final String term = this.getItem();
						this.setTooltip(new Tooltip(term));
						this.setText(term);
					}
					this.setStyle("-fx-padding: 0px 0px 0px 3px");
				}
			};
		});
		termListView.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> {
			final String selItem = newValue;
			if (selItem != null)
				showDetail(selItem);
		});
		termListView.setOnDragDetected(mouseEvent -> {
			final Dragboard db = termListView.startDragAndDrop(TransferMode.ANY);
			final ClipboardContent content = new ClipboardContent();
			final String term = termListView.getSelectionModel().getSelectedItem();
			content.putString(term);
			db.setContent(content);
			mouseEvent.consume();
		});

		// add detail box at the center
		scrollPane.setContent(detailBox);
		detailBox.setPadding(new Insets(10));
		detailBox.prefWidthProperty().bind(scrollPane.widthProperty().subtract(20));
	   	final SplitPane splitPane = new SplitPane();
		splitPane.setOrientation(Orientation.HORIZONTAL);
		splitPane.setDividerPositions(0.25);
		splitPane.getItems().addAll(termListView, scrollPane);
		mainPane.setCenter(splitPane);

		// add status bar at the bottom
		statusBox.setPadding(new Insets(3, 3, 0, 3));
		statusBox.setAlignment(Pos.BOTTOM_LEFT);
		statusBox.getChildren().add(message);
		mainPane.setBottom(statusBox);

		final Scene scene = new Scene(mainPane, windowWidth, windowHeight);
		setScene(scene);
		
		// set up drop event
		mainPane.setOnDragOver(dragEvent -> {
			if (dragEvent.getGestureSource() != this && dragEvent.getDragboard().hasString())
				dragEvent.acceptTransferModes(TransferMode.COPY_OR_MOVE);
			dragEvent.consume();
		});
		mainPane.setOnDragDropped(dragEvent -> {
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

		// some intialization
		gson = new Gson();
		final String initMess = Utilities.ppdpdAvailMap.get(Utilities.PpdpdTable.DICTIONARY).get()
								? DpdUtilities.READY
								: "Searchable data not ready, generate tables first";
		setMessage(initMess);
		Platform.runLater(() -> searchTextField.requestFocus());

		// prepare info popups
		helpPopup.setContentWithText(DpdUtilities.getTextResource("info-dpddict.txt"));
		helpPopup.setTextWidth(Utilities.getRelativeSize(35));
		infoPopup.setTextWidth(Utilities.getRelativeSize(30));
	}

	private void showDpdInfo() {
		if (dpdInfo.isEmpty()) {
			dpdInfo = readDpdInfo();
			infoPopup.setContentWithText(dpdInfo);
		}
		infoPopup.showPopup(infoButton, InfoPopup.Pos.BELOW_RIGHT, true);
	}

	private String readDpdInfo() {
		final StringBuilder result = new StringBuilder();
		result.append("Digital Pāḷi Dictionary (DPD)\n");
		final Map<String, String> infoMap = new HashMap<>();
		try {
			final java.sql.Connection dpdConn = Utilities.SQLiteDB.DPD.getConnection();
			if (dpdConn != null) {
				final String select = "SELECT key,value FROM db_info WHERE " +
										"key = 'author' OR " +
										"key = 'dpd_release_version' OR " +
										"key = 'email' OR " +
										"key = 'github' OR " +
										"key = 'website';";
				final Statement stmt = dpdConn.createStatement();
				final ResultSet res = stmt.executeQuery(select);
				while (res.next()) {
					infoMap.put(res.getString("key"), res.getString("value"));
				}
				res.close();
				stmt.close();
			}
		} catch (SQLException e) {
			System.err.println(e);
		}
		result.append("Version: ").append(infoMap.get("dpd_release_version")).append("\n");
		result.append("Author: ").append(infoMap.get("author")).append("\n");
		result.append("Email: ").append(infoMap.get("email")).append("\n");
		result.append("Website: ").append(infoMap.get("website")).append("\n");
		result.append("Github: ").append(infoMap.get("github")).append("\n");
		result.append("Copyright: ").append("CC-BY-NC 4.0\n");
		return result.toString();
	}

	private void buildDictTable() {
		final boolean proceed = Utilities.ppdpdAvailMap.get(Utilities.PpdpdTable.DICTIONARY).get()
								? DpdUtilities.proceedBuildConfirm(this)
								: true;
		if (!proceed) return;
		isCreating.set(true);
		progressBar.setProgress(0);
		final Task<Boolean> buildTask = new Task<Boolean>() {
			@Override
			protected Boolean call() throws Exception {
				try {
					final java.sql.Connection dpdConn = Utilities.SQLiteDB.DPD.getConnection();
					final java.sql.Connection ppdpdConn = Utilities.H2DB.PPDPD.getConnection();
					if (dpdConn != null  && ppdpdConn != null) {
						// read head words in DPD lookup
						final String lSelect = "SELECT lookup_key,headwords FROM lookup WHERE headwords != '';";
						final Statement headStmt = dpdConn.createStatement();
						final ResultSet headRes = headStmt.executeQuery(lSelect);
						int total = Integer.parseInt(Utilities.settings.getProperty("dpd-dict-count", "-1"));
						int count = 0;
						final List<StringPair> dlist = new ArrayList<>(total);
						updateMessage("Reading DPD data...");
						while (headRes.next()) {
							final StringPair pair = new StringPair(headRes.getString("lookup_key"),
																	headRes.getString("headwords"));
							dlist.add(pair);
							updateProgress(++count, total);
						}
						headRes.close();
						headStmt.close();
						// sort the list
						total = dlist.size();
						Utilities.settings.setProperty("dpd-dict-count", "" + total);
						MainProperties.INSTANCE.saveSettings();
						updateMessage(String.format("Sorting %,d records... (please wait)", total));
						updateProgress(-1, -1);
						dlist.sort((a, b) -> Utilities.paliCollator.compare(a.getFirst(), b.getFirst()));
						// create a table in PP-DPD
						String tabName = Utilities.PpdpdTable.DICTIONARY.toString();
						final String dDelete = "DROP TABLE IF EXISTS " + tabName + ";";
						Utilities.executeSQL(ppdpdConn, dDelete);
						final String dCreate = "CREATE TABLE " + tabName + " (" +
							"ID INT PRIMARY KEY," +
							"TERM VARCHAR(255) UNIQUE," +
							"HEADWORDS VARCHAR(255));";
						Utilities.executeSQL(ppdpdConn, dCreate);
						final String dInsert = "INSERT INTO " + tabName + " VALUES(?, ?, ?);";
						final PreparedStatement dPstm = ppdpdConn.prepareStatement(dInsert);
						count = 0;
						updateMessage(String.format("Creating table of %,d records...", total));
						for (final StringPair p : dlist) {
							count++;
							dPstm.setInt(1, count);
							dPstm.setString(2, p.getFirst());
							dPstm.setString(3, p.getSecond());
							dPstm.executeUpdate();
							updateProgress(count, total);
						}
						dPstm.close();
						updateMessage("Creating index... (please wait)");
						updateProgress(-1, -1);
						final String dIndex = "CREATE INDEX IDX_" + tabName + " ON " + tabName + "(TERM);";
						Utilities.executeSQL(ppdpdConn, dIndex);
						// create mini DPD
						tabName = Utilities.PpdpdTable.MINIDPD.toString();
						final String hSelect = "SELECT " +
											"id,lemma_1,grammar,verb,trans,plus_case,meaning_1,meaning_2,meaning_lit," +
											"sanskrit,root_key,construction FROM dpd_headwords;";
						final Statement mdpdStmt = dpdConn.createStatement();
						final ResultSet mdpdRes = mdpdStmt.executeQuery(hSelect);
						total = Integer.parseInt(Utilities.settings.getProperty("dpd-head-count", "-1"));
						count = 0;
						updateMessage("Creating Mini DPD...");
						final String mDelete = "DROP TABLE IF EXISTS " + tabName + ";";
						Utilities.executeSQL(ppdpdConn, mDelete);
						final String mCreate = "CREATE TABLE " + tabName + " (" +
							"ID INT PRIMARY KEY," +
							"LEMMA_1 VARCHAR(255)," +
							"GRAMMAR VARCHAR(255)," +
							"VERB VARCHAR(255)," +
							"TRANS VARCHAR(255)," +
							"PLUS_CASE VARCHAR(255)," +
							"MEANING_1 VARCHAR(255)," +
							"MEANING_2 VARCHAR(300)," +
							"MEANING_LIT VARCHAR(255)," +
							"SANSKRIT VARCHAR(255)," +
							"ROOT_KEY VARCHAR(255)," +
							"CONSTRUCTION VARCHAR(255));";
						Utilities.executeSQL(ppdpdConn, mCreate);
						final String mInsert = "INSERT INTO " + tabName + " VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";
						final PreparedStatement mPstm = ppdpdConn.prepareStatement(mInsert);
						final Map<String, Integer> headMap = new HashMap<>();
						while (mdpdRes.next()) {
							final int id = mdpdRes.getInt("id");
							final String term = mdpdRes.getString("lemma_1");
							headMap.put(term, id);
							mPstm.setInt(1, id);
							mPstm.setString(2, term);
							mPstm.setString(3, mdpdRes.getString("grammar"));
							mPstm.setString(4, mdpdRes.getString("verb"));
							mPstm.setString(5, mdpdRes.getString("trans"));
							mPstm.setString(6, mdpdRes.getString("plus_case"));
							mPstm.setString(7, mdpdRes.getString("meaning_1"));
							mPstm.setString(8, mdpdRes.getString("meaning_2"));
							mPstm.setString(9, mdpdRes.getString("meaning_lit"));
							mPstm.setString(10, mdpdRes.getString("sanskrit"));
							mPstm.setString(11, mdpdRes.getString("root_key"));
							mPstm.setString(12, mdpdRes.getString("construction"));
							mPstm.executeUpdate();
							updateProgress(++count, total);
						}
						Utilities.settings.setProperty("dpd-head-count", "" + count);
						MainProperties.INSTANCE.saveSettings();
						mPstm.close();
						mdpdRes.close();
						mdpdStmt.close();
						// create sorted head words
						tabName = Utilities.PpdpdTable.SORTED_HEADWORDS.toString();
						updateMessage("Creating sorted head words... (please wait)");
						updateProgress(-1, -1);
						final List<String> headList = headMap.keySet().stream()
														.sorted(Utilities.paliComparator)
														.collect(Collectors.toList());
						final String sDelete = "DROP TABLE IF EXISTS " + tabName + ";";
						Utilities.executeSQL(ppdpdConn, sDelete);
						final String sCreate = "CREATE TABLE " + tabName + " (" +
							"ID INT PRIMARY KEY," +
							"TERM VARCHAR(255) UNIQUE," +
							"TERMID INT);";
						Utilities.executeSQL(ppdpdConn, sCreate);
						final String sInsert = "INSERT INTO " + tabName + " VALUES(?, ?, ?);";
						final PreparedStatement sPstm = ppdpdConn.prepareStatement(sInsert);
						total = headMap.size();
						count = 0;
						for (final String hw : headList) {
							count++;
							sPstm.setInt(1, count);
							sPstm.setString(2, hw);
							sPstm.setInt(3, headMap.get(hw));
							sPstm.executeUpdate();
							updateProgress(count, total);
						}
						sPstm.close();
					}
				} catch (SQLException e) {
					System.err.println(e);
				}
				Platform.runLater(() -> {
					progressBar.progressProperty().unbind();
					statusBox.getChildren().remove(progressBar);
					isCreating.set(false);
					Utilities.updatePpdpdAvailibility();
					setMessage(DpdUtilities.READY);
				});
				return true;
			}
		};
		progressBar.progressProperty().bind(buildTask.progressProperty());
		buildTask.messageProperty().addListener((observable, oldValue, newValue) -> {
			message.setText(newValue);
		});
		Utilities.threadPool.submit(buildTask);
		statusBox.getChildren().setAll(progressBar, message);
	}

	private List<StringPair> getTermFromDict(final String query) {
		final SearchMethod method = (SearchMethod)searchMethodGroup.getSelectedToggle().getUserData();
		final List<StringPair> result = new ArrayList<>();
		try {
			final java.sql.Connection ppdpdConn = Utilities.H2DB.PPDPD.getConnection();
			final String where = query.isEmpty()
									? ""
									: method == SearchMethod.TERM_START
										? " WHERE TERM LIKE '" + query + "%'"
										: " WHERE TERM LIKE '%" + query + "%'";
			final String tabName = Utilities.PpdpdTable.DICTIONARY.toString();
			final String select = "SELECT TERM,HEADWORDS FROM " + tabName + where + " ORDER BY ID LIMIT " + maxResultCount + ";"; 
			final Statement stmt = ppdpdConn.createStatement();
			final ResultSet res = stmt.executeQuery(select);
			while (res.next()) {
				final StringPair p = new StringPair(res.getString("TERM"), res.getString("HEADWORDS"));
				result.add(p);
			}
			res.close();
			stmt.close();
		} catch (SQLException e) {
			System.err.println(e);
		}
		return result;
	}

	private List<StringPair> getTermFromDecon(final String query) {
		final SearchMethod method = (SearchMethod)searchMethodGroup.getSelectedToggle().getUserData();
		final List<StringPair> result = new ArrayList<>();
		try {
			final java.sql.Connection ppdpdConn = Utilities.H2DB.PPDPD.getConnection();
			final String where = query.isEmpty()
									? ""
									: method == SearchMethod.TERM_START
										? " WHERE TERM LIKE '" + query + "%'"
										: " WHERE TERM LIKE '%" + query + "%'";
			final String tabName = Utilities.PpdpdTable.DECONSTRUCTOR.toString();
			final String select = "SELECT TERM,DECON FROM " + tabName + where + " ORDER BY ID LIMIT " + maxResultCount + ";"; 
			final Statement stmt = ppdpdConn.createStatement();
			final ResultSet res = stmt.executeQuery(select);
			while (res.next()) {
				final StringPair p = new StringPair(res.getString("TERM"), res.getString("DECON"));
				result.add(p);
			}
			res.close();
			stmt.close();
		} catch (SQLException e) {
			System.err.println(e);
		}
		return result;
	}

	private void showResult() {
		final String query = Normalizer.normalize(searchTextField.getText().trim(), Form.NFC);
		final SearchMethod method = (SearchMethod)searchMethodGroup.getSelectedToggle().getUserData();
		if (!query.isEmpty() && (method == SearchMethod.TERM_START || query.length() >= 2)) {
			message.setText(DpdUtilities.RETRIEVING);
			isSearching.set(true);
			CompletableFuture.runAsync(() -> showResult(query), Utilities.threadPool);
		}
	}

	private void showResult(final String query) {
		final Runnable initCheck = isCreating.get()
									? () -> setMessage("Please wait until done")
									: !Utilities.ppdpdAvailMap.get(Utilities.PpdpdTable.DICTIONARY).get()
										? () -> setMessage("Nothing to display, please generate tables first")
										: null;
		if (initCheck != null) {
			Platform.runLater(initCheck);
		} else if (!query.isEmpty()) {
			final List<String> finalList = new ArrayList<>();
			final Set<String> termList = new LinkedHashSet<>();
			final List<StringPair> termPairDBList = getTermFromDict(query);
			for (final StringPair pair : termPairDBList) {
				final String term = pair.getFirst();
				termList.add(term);
				final List<String> detList = new ArrayList<>();
				detList.add(pair.getSecond());
				resultMap.put(term, detList);
			}
			if (deconButton.isSelected()) {
				final List<StringPair> deconPairDBList = getTermFromDecon(query);
				for (final StringPair pair : deconPairDBList) {
					final String term = pair.getFirst();
					termList.add(term);
					final List<String> detList = resultMap.getOrDefault(term, new ArrayList<>());
					detList.add(pair.getSecond());
					resultMap.put(term, detList);
				}
				final int max = maxResultCount;
				final List<String> bothList = termList.stream()
										.sorted(Utilities.paliComparator)
										.limit(max)
										.collect(Collectors.toList());
				finalList.addAll(bothList);
			} else {
				finalList.addAll(termList);
			}
			Platform.runLater(() -> {
				resultList.clear();
				resultList.addAll(finalList);
				// show item count
				final int dictTotal = Integer.parseInt(Utilities.settings.getProperty("dpd-dict-count", "0"));
				final int total = deconButton.isSelected()
									? dictTotal + Integer.parseInt(Utilities.settings.getProperty("dpd-decon-count", "0"))
									: dictTotal;
				final int count = resultList.size();
				final String s = count <= 1 ? "" : "s";
				setMessage(String.format("%,d of %,d item%s listed", count, total, s));
				termListView.scrollTo(0);
				if (count > 0) {
					showDetail(resultList.get(0));
				}
				searchTextField.requestFocus();
				searchTextField.appendText(""); // make it deselected
			});
		}
		isSearching.set(false);
	}

	private void setMessage(final String text) {
		message.setText(text);
	}

	private void showDetail(final String term) {
		if (term.isEmpty()) return;
		currTerm = term;
		scrollPane.setVvalue(scrollPane.getVmin());
		detailBox.getChildren().clear();
		detailBox.getChildren().add(DpdUtilities.createHeadTextFlow(term, "1.5em"));
		final List<String> detailList = resultMap.get(term);
		if (detailList.isEmpty()) return;
		for (final String detail : detailList) {
			if (detail.trim().matches("^\\[ *\\d+.*")) {
				// head words
				final int[] items = gson.fromJson(detail, int[].class);
				final List<DpdHeadWord> hwList = DpdUtilities.getDpdHeadWords(items);
				for (final DpdHeadWord hw : hwList) {
					final VBox resultBox = DpdUtilities.getDpdHeadWordBox(hw,
											showWordFamilyMenuItem.isSelected(), showIdiomFamilyMenuItem.isSelected());
					resultBox.setUserData("term-box");
					detailBox.getChildren().add(resultBox);
				}
			} else {
				// deconstructor
				final String[] items = gson.fromJson(detail, String[].class);
				detailBox.getChildren().add(DpdUtilities.createInfoTextFlow("\nDeconstructor: ", ""));
				for (final String decon : items) {
					final TextFlow tfDecon = new TextFlow();
					tfDecon.getChildren().add(DpdUtilities.createInfoText(decon));
					detailBox.getChildren().add(tfDecon);
				}
			}
		}
	}

	private String makeText() {
		return DpdUtilities.getVBoxTextFlowText(detailBox);
	}
	
	private void copyText() {
		Utilities.copyText(makeText());
	}
	
	private void saveText() {
		Utilities.saveText(makeText(), "dpddict.txt");
	}

}
