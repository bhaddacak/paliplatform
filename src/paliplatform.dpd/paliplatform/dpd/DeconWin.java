/*
 * DeconWin.java
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
import javafx.scene.control.cell.*;
import javafx.scene.image.*;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.scene.text.*;
import javafx.geometry.*;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.application.Platform;

import com.google.gson.Gson;

/** 
 * The window showing Pali word Deconpositions according to DPD.
 * This is a singleton.
 * @author J.R. Bhaddacak
 * @version 3.6
 * @since 3.0
 */
public class DeconWin extends SingletonWindow {
	public static final DeconWin INSTANCE = new DeconWin();
	private final BorderPane mainPane = new BorderPane();
	private final SplitPane splitPane = new SplitPane();
	private final ScrollPane detailPane = new ScrollPane();
	private final VBox detailBox = new VBox();
	private final TableView<DeconOutput> table = new TableView<>();	
	private final ToggleGroup searchMethodGroup = new ToggleGroup();
	private final HBox statusBox = new HBox(5);
	private final ProgressBar progressBar = new ProgressBar();
	private final Label message = new Label();
	private final InfoPopup infoPopup = new InfoPopup();
	private final TextField searchTextField;
	private final ObservableList<DeconOutput> outputList = FXCollections.<DeconOutput>observableArrayList();
	private final List<StringPair> deconList = new ArrayList<>();
	private final SimpleBooleanProperty isCreating = new SimpleBooleanProperty(false);
	private final SimpleBooleanProperty isSearching = new SimpleBooleanProperty(false);
	private final Gson gson;
	private int maxRowCount = DpdUtilities.DEF_MAX_RESULT;
	
	private DeconWin() {
		windowWidth = Utilities.getRelativeSize(58);
		setTitle("Deconstructor");
		getIcons().add(new Image(DeconWin.class.getResourceAsStream("resources/images/hammer.png")));
		
		// add toolbar on the top
		final CommonWorkingToolBar toolBar = new CommonWorkingToolBar(table);
		// config some buttons
		toolBar.saveTextButton.setTooltip(new Tooltip("Save data as CSV"));
		toolBar.saveTextButton.setOnAction(actionEvent -> saveCSV());		
		toolBar.copyButton.setTooltip(new Tooltip("Copy CSV to clipboard"));
		toolBar.copyButton.setOnAction(actionEvent -> copyCSV());		
		// add new components
		final Button genTableButton = new Button("Generate", new TextIcon("screwdriver-wrench", TextIcon.IconSet.AWESOME));
		genTableButton.disableProperty().bind(DpdUtilities.ppdpdDBLocked.or(isCreating).or(isSearching));
		genTableButton.setTooltip(new Tooltip("Generate deconstructor table"));
		genTableButton.setOnAction(actionEvent -> buildDeconTable());
		// help button
		final Button helpButton = new Button("", new TextIcon("circle-question", TextIcon.IconSet.AWESOME));
		helpButton.setOnAction(actionEvent -> infoPopup.showPopup(helpButton, InfoPopup.Pos.BELOW_CENTER, true));
		toolBar.getItems().addAll(new Separator(), genTableButton, helpButton);
		// add second tool bar
		final ToolBar searchToolBar = new ToolBar();
		final PaliTextInput searchTextInput = new PaliTextInput(PaliTextInput.InputType.FIELD);
		searchTextField = (TextField)searchTextInput.getInput();
		searchTextField.setPromptText("Search for...");
		searchTextField.disableProperty().bind(isSearching);
		searchTextField.textProperty().addListener((obs, oldValue, newValue) -> {
			final SearchMethod method = (SearchMethod)searchMethodGroup.getSelectedToggle().getUserData();
			if (method != SearchMethod.BOTH_WITHIN) {
				final String query = Normalizer.normalize(newValue.trim(), Form.NFC);
				if (method == SearchMethod.TERM_START || query.length() >= 2) {
					message.setText(DpdUtilities.RETRIEVING);
					isSearching.set(true);
					CompletableFuture.runAsync(() -> showResult(query, true), Utilities.threadPool);
				}
			}
		});
		searchTextField.setOnKeyPressed(keyEvent -> {
			if (keyEvent.getEventType() == KeyEvent.KEY_PRESSED) {
				final KeyCode key = keyEvent.getCode();
				if (key == KeyCode.ENTER) {
					showResult();
				} else if (key == KeyCode.ESCAPE) {
					searchTextField.clear();
				}
			}
		});	
		final Button searchButton = new Button("Search");
		searchButton.setOnAction(actionEvent -> {
			showResult();
		});		
		final MenuButton searchOptionsMenu = new MenuButton("", new TextIcon("check-double", TextIcon.IconSet.AWESOME));
		searchOptionsMenu.setTooltip(new Tooltip("Search method"));
		for (final SearchMethod sm : SearchMethod.values) {
			final RadioMenuItem radio = new RadioMenuItem(sm.getName());
			radio.setUserData(sm);
			radio.setToggleGroup(searchMethodGroup);
			searchOptionsMenu.getItems().add(radio);
		}
		searchMethodGroup.selectToggle(searchMethodGroup.getToggles().get(0));
        searchMethodGroup.selectedToggleProperty().addListener(observable -> showResult());
		final ChoiceBox<Integer> maxRowChoice = new ChoiceBox<>();
		maxRowChoice.setTooltip(new Tooltip("Maximum rows"));
		maxRowChoice.getItems().addAll(DpdUtilities.MAXLIST);
		maxRowChoice.getSelectionModel().select(1);
		maxRowChoice.setOnAction(actionEvent -> {
			maxRowCount = maxRowChoice.getSelectionModel().getSelectedItem();
			showResult();
		});
		searchToolBar.getItems().addAll(searchTextField, searchTextInput.getClearButton(), searchTextInput.getMethodButton(),
									searchButton, searchOptionsMenu, maxRowChoice);
		final VBox toolBarBox = new VBox();
		toolBarBox.getChildren().addAll(toolBar, searchToolBar);
		mainPane.setTop(toolBarBox);

		// add main content
		final Scene scene = new Scene(mainPane, windowWidth, windowHeight);
		table.setItems(outputList);
		table.setOnMouseClicked(mouseEvent -> showDetail());
		table.setOnDragDetected(mouseEvent -> {
			final Dragboard db = table.startDragAndDrop(TransferMode.ANY);
			final ClipboardContent content = new ClipboardContent();
			final DeconOutput dout = table.getSelectionModel().getSelectedItem();
			content.putString(dout.wordProperty().get());
			db.setContent(content);
			mouseEvent.consume();
		});
		table.setOnMouseDragged(mouseEvent -> mouseEvent.setDragDetect(true));
		splitPane.setOrientation(Orientation.VERTICAL);
		splitPane.getItems().add(table);
		mainPane.setCenter(splitPane);
		
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

		// add status bar at the bottom
		statusBox.setPadding(new Insets(3, 3, 0, 3));
		statusBox.setAlignment(Pos.BOTTOM_LEFT);
		statusBox.getChildren().add(message);
		mainPane.setBottom(statusBox);

		// some intialization
		gson = new Gson();
		detailBox.setPadding(new Insets(5, 5, 0, 5));
		detailPane.setContent(detailBox);
		showInitialResult();
		setupTable();

		setScene(scene);
		
		// prepare info popup
		infoPopup.setContentWithText(DpdUtilities.getTextResource("info-deconstructor.txt"));
		infoPopup.setTextWidth(Utilities.getRelativeSize(32));
	}

	private void showInitialResult() {
		outputList.add(new DeconOutput(new StringPair("", "[]")));
		isSearching.set(true);
		showResult("", false);
	}

	private void showResult() {
		final String query = Normalizer.normalize(searchTextField.getText().trim(), Form.NFC);
		final SearchMethod method = (SearchMethod)searchMethodGroup.getSelectedToggle().getUserData();
		if (method == SearchMethod.TERM_START || query.length() >= 2) {
			message.setText(DpdUtilities.RETRIEVING);
			isSearching.set(true);
			CompletableFuture.runAsync(() -> showResult(query, true), Utilities.threadPool);
		}
	}

	private void showResult(final String query, final boolean async) {
		final Runnable initCheck = isCreating.get()
									? () -> setMessage("Please wait until done")
									: !Utilities.ppdpdAvailMap.get(Utilities.PpdpdTable.DECONSTRUCTOR).get()
										? () -> setMessage("Nothing to display, please generate the table first")
										: null;
		if (initCheck != null) {
			Platform.runLater(initCheck);
			isSearching.set(false);
			return;
		}
		outputList.clear();
		deconList.clear();
		deconList.addAll(getDeconFromDB(query));
		final String mess;
		if (!deconList.isEmpty()) {
			if (!deconList.get(0).getFirst().isEmpty()) {
				final int total = Integer.parseInt(Utilities.getSetting("dpd-decon-count"));
				final int count = deconList.size();
				final String s = count <= 1 ? "" : "s";
				mess = String.format("%,d of %,d item%s listed", count, total, s);
			} else {
				mess = "";
			}
		} else {
			deconList.add(new StringPair("", "[]"));
			mess = "Nothing found";

		}
		if (async) {
			Platform.runLater(() -> {
				for (final StringPair pr : deconList)
					outputList.add(new DeconOutput(pr));
				setMessage(mess);
				searchTextField.requestFocus();
				searchTextField.appendText(""); // make it deselected
				if (splitPane.getItems().size() > 1)
					splitPane.getItems().remove(detailPane);
			});
		} else {
			for (final StringPair pr : deconList)
				outputList.add(new DeconOutput(pr));
			setMessage(mess);
		}
		isSearching.set(false);
	}

	private void setMessage(final String text) {
		message.setText(text);
	}

	private void showDetail() {
		final DeconOutput selected = table.getSelectionModel().getSelectedItem();
		if (selected == null) return;
		if (splitPane.getItems().size() == 1) {
			splitPane.getItems().add(detailPane);
			splitPane.setDividerPositions(0.8);
		}
		final StringPair pair = selected.getPair();
		detailBox.getChildren().clear();
		detailBox.getChildren().add(DpdUtilities.createHeadTextFlow(pair.getFirst(), "1.2em"));
		final String[] decons = gson.fromJson(pair.getSecond(), String[].class);
		for (final String decon : decons) {
			final TextFlow tfDecon = new TextFlow();
			final Text txtDecon = new Text(decon);
			txtDecon.getStyleClass().add("reader-info");
			tfDecon.getChildren().add(txtDecon);
			detailBox.getChildren().add(tfDecon);
		}
	}

	private void setupTable() {
		final TableColumn<DeconOutput, String> wordCol = new TableColumn<>("Term");
		wordCol.setCellValueFactory(new PropertyValueFactory<>(outputList.get(0).wordProperty().getName()));
		wordCol.prefWidthProperty().bind(mainPane.widthProperty().divide(6).multiply(2).subtract(20));
		wordCol.setSortable(false);
		wordCol.setReorderable(false);
		final TableColumn<DeconOutput, String> deconCol = new TableColumn<>("Deconstruction");
		deconCol.setCellValueFactory(new PropertyValueFactory<>(outputList.get(0).deconProperty().getName()));
		deconCol.prefWidthProperty().bind(mainPane.widthProperty().divide(6).multiply(4));
		deconCol.setSortable(false);
		deconCol.setReorderable(false);
		table.getColumns().clear();
		table.getColumns().add(wordCol);
		table.getColumns().add(deconCol);
	}
	
	private void buildDeconTable() {
		final boolean proceed = Utilities.ppdpdAvailMap.get(Utilities.PpdpdTable.DECONSTRUCTOR).get()
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
						// read deconstruction data from DPD DB
						final String select = "SELECT lookup_key,deconstructor FROM lookup WHERE deconstructor != '';";
						final Statement dpdStmt = dpdConn.createStatement();
						final ResultSet deconRes = dpdStmt.executeQuery(select);
						int total = Integer.parseInt(Utilities.getSetting("dpd-decon-count"));
						int count = 0;
						final List<StringPair> dlist = new ArrayList<>(total);
						updateMessage("Reading DPD data...");
						while (deconRes.next()) {
							final StringPair pair = new StringPair(deconRes.getString("lookup_key"),
																	deconRes.getString("deconstructor"));
							dlist.add(pair);
							updateProgress(++count, total);
						}
						// sort the list
						total = dlist.size();
						Utilities.setSetting("dpd-decon-count", "" + total);
						MainProperties.INSTANCE.saveSettings();
						updateMessage(String.format("Sorting %,d records... (please wait)", total));
						updateProgress(-1, -1);
						dlist.sort((a, b) -> Utilities.paliCollator.compare(a.getFirst(), b.getFirst()));
						// create a table in PP-DPD
						final String tabName = Utilities.PpdpdTable.DECONSTRUCTOR.toString();
						final String delete = "DROP TABLE IF EXISTS " + tabName + ";";
						Utilities.executeSQL(ppdpdConn, delete);
						final String create = "CREATE TABLE " + tabName + " (" +
							"ID INT PRIMARY KEY," +
							"TERM VARCHAR(255) UNIQUE," +
							"DECON VARCHAR);";
						Utilities.executeSQL(ppdpdConn, create);
						final String insert = "INSERT INTO " + tabName + " VALUES(?, ?, ?);";
						final PreparedStatement pstm = ppdpdConn.prepareStatement(insert);
						count = 0;
						updateMessage(String.format("Creating table of %,d records...", total));
						for (final StringPair p : dlist) {
							count++;
							pstm.setInt(1, count);
							pstm.setString(2, p.getFirst());
							pstm.setString(3, p.getSecond());
							pstm.executeUpdate();
							updateProgress(count, total);
						}
						pstm.close();
						deconRes.close();
						dpdStmt.close();
						updateMessage("Creating index... (please wait)");
						updateProgress(-1, -1);
						final String index = "CREATE INDEX IDX_" + tabName + " ON " + tabName + "(TERM);";
						Utilities.executeSQL(ppdpdConn, index);
					}
				} catch (SQLException e) {
					System.err.println(e);
				}
				Platform.runLater(() -> {
					progressBar.progressProperty().unbind();
					statusBox.getChildren().remove(progressBar);
					isCreating.set(false);
					Utilities.updatePpdpdAvailibility();
					showResult();
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

	private List<StringPair> getDeconFromDB(final String query) {
		final SearchMethod method = (SearchMethod)searchMethodGroup.getSelectedToggle().getUserData();
		final List<StringPair> result = new ArrayList<>();
		try {
			final java.sql.Connection ppdpdConn = Utilities.H2DB.PPDPD.getConnection();
			final String where;
			if (query.isEmpty()) {
				where = "";
			} else {
				if (method == SearchMethod.TERM_START)
					where = "WHERE TERM LIKE '" + query + "%'";
				else if (method == SearchMethod.TERM_WITHIN)
					where = "WHERE TERM LIKE '%" + query + "%'";
				else
					where = "WHERE TERM LIKE '%" + query + "%' OR DECON LIKE '%" + query + "%'";
			}
			final String tabName = Utilities.PpdpdTable.DECONSTRUCTOR.toString();
			final String select = "SELECT TERM,DECON FROM " + tabName + " " + where + " ORDER BY ID LIMIT " + maxRowCount + ";"; 
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
			final DeconOutput out = table.getItems().get(i);
			final String[] data = new String[colCount];
			data[0] = out.wordProperty().get();
			data[1] = out.deconProperty().get();
			result.add(data);
		}
		return result;
	}
	
	private void copyCSV() {
		Utilities.copyCSV(makeCSV());
	}
	
	private void saveCSV() {
		Utilities.saveCSV(makeCSV(), "deconstructor.csv");
	}

	// inner class
	public static final class DeconOutput {
		private StringProperty word;
		private StringProperty decon;
		private final StringPair pair;
		private final Gson gson = new Gson();
		
		public DeconOutput(final StringPair pair) {
			wordProperty().set(pair.getFirst());
			final String[] arr = gson.fromJson(pair.getSecond(), String[].class);
			deconProperty().set(Arrays.stream(arr).collect(Collectors.joining(" | ")));
			this.pair = pair;
		}
		
		public StringProperty wordProperty() {
			if (word == null)
				word = new SimpleStringProperty(this, "word");
			return word;
		}
		
		public StringProperty deconProperty() {
			if (decon == null)
				decon = new SimpleStringProperty(this, "decon");
			return decon;
		}

		public StringPair getPair() {
			return pair;
		}
		
	} // end inner class
	
}
