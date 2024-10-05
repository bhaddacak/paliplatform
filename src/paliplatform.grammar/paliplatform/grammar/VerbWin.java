/*
 * VerbWin.java
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
import paliplatform.dict.*;

import java.text.Normalizer;
import java.text.Normalizer.Form;
import java.util.*;
import java.util.stream.*;
import java.util.regex.*;
import java.sql.*;

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
 * The Pali-verb window displays verbs in the CPED.
 * This is a singleton.
 * @author J.R. Bhaddacak
 * @version 3.0
 * @since 2.0
 */
public class VerbWin extends SingletonWindow {
	static enum SearchField {
		VERB("Verb"), ROOT("Root/Prefix"), PACCAYA("Paccaya"), MEANING("Meaning");
		public static final SearchField[] values = values();
		private final String name;
		private SearchField(final String name) {
			this.name = name;
		}
		@Override
		public String toString() {
			return name;
		}
	};
	static enum VerbForm {
		AOR("Aorist"), CAU("Causative"), PP("Past Participle"), PRP("Present Participle"),
		POP("Potential Participle"), ABS("Absolutive"), INF("Infinitive"), DEN("Denominative"), MISC("Misc.");
		public static final VerbForm[] values = values();
		private static final String[] abbrs = { "aor.", "caus.", "pp.", "pr.p.", "pt.p.", "abs.", "inf.", "deno.", "misc." };
		private final String name;
		private VerbForm(final String name) {
			this.name = name;
		}
		public String getAbbr() {
			return abbrs[this.ordinal()];
		}
		@Override
		public String toString() {
			return name;
		}
	};
	public static final VerbWin INSTANCE = new VerbWin();
	private final Map<String, VerbOutput> cpedVerbMap = new HashMap<>();
	private final BorderPane mainPane = new BorderPane();
	private final RadioButton mainFormButton = new RadioButton("Main");
	private final RadioButton otherFormButton = new RadioButton("Other");
	private final TableView<VerbOutput> table = new TableView<>();	
	private final ObservableList<VerbOutput> outputList = FXCollections.<VerbOutput>observableArrayList();
	private final VBox detailBox = new VBox();
	private final InfoPopup infoPopup = new InfoPopup();
	private final TextField searchTextField;
	private Toggle currMode = mainFormButton;
	private SearchField currSearchField = SearchField.VERB;
	private VerbForm currOtherVerbForm = VerbForm.AOR;
	
	private VerbWin() {
		setTitle("Pāli Verbs");
		getIcons().add(new Image(VerbWin.class.getResourceAsStream("resources/images/person-walking.png")));
		windowWidth = Utilities.getRelativeSize(65);
		
		// add toolbar on the top
		final CommonWorkingToolBar toolBar = new CommonWorkingToolBar(table);
		// config some buttons
		toolBar.saveTextButton.setOnAction(actionEvent -> saveCSV());		
		toolBar.copyButton.setOnAction(actionEvent -> copyCSV());		
		// add new components
		final ToggleGroup modeGroup = new ToggleGroup();
		mainFormButton.setTooltip(new Tooltip("Canonical verb form"));
		mainFormButton.setToggleGroup(modeGroup);
		otherFormButton.setTooltip(new Tooltip("Other verb forms"));
		otherFormButton.setToggleGroup(modeGroup);
		// for main verb form
		final PaliTextInput searchTextInput = new PaliTextInput(PaliTextInput.InputType.FIELD);
		searchTextField = (TextField)searchTextInput.getInput();
		searchTextField.setPromptText("Search for...");
		searchTextField.setPrefWidth(Utilities.getRelativeSize(8));
		searchTextField.textProperty().addListener((obs, oldValue, newValue) -> {
			final String text = Normalizer.normalize(newValue.trim(), Form.NFC);
			displayDictMain(text);
		});
		final Button clearButton = searchTextInput.getClearButton();
		final Button methodButton = searchTextInput.getMethodButton();
		final Label inLabel = new Label("in");
		final ChoiceBox<SearchField> searchFieldChoice = new ChoiceBox<>();
		for (final SearchField sf : SearchField.values) 
			searchFieldChoice.getItems().add(sf);
		searchFieldChoice.getSelectionModel().select(0);
		searchFieldChoice.setOnAction(actionEvent -> {
			final int selected = searchFieldChoice.getSelectionModel().getSelectedIndex();
			currSearchField = SearchField.values[selected];
			if (currSearchField == SearchField.MEANING)
				searchTextInput.setInputMethod(PaliTextInput.InputMethod.NORMAL);
			else
				searchTextInput.resetInputMethod();
			displayDictMain();
		});
		// for other verb forms
		final ChoiceBox<VerbForm> vformChoice = new ChoiceBox<>();
		for (final VerbForm vf : VerbForm.values)
			vformChoice.getItems().add(vf);
		vformChoice.getSelectionModel().select(0);
		vformChoice.setOnAction(actionEvent -> {
			final int selected = vformChoice.getSelectionModel().getSelectedIndex();
			currOtherVerbForm = VerbForm.values[selected];
			displayDictOther();
		});

		// help button
		final Button helpButton = new Button("", new TextIcon("circle-question", TextIcon.IconSet.AWESOME));
		helpButton.setOnAction(actionEvent -> infoPopup.showPopup(helpButton, InfoPopup.Pos.BELOW_RIGHT, true));

		modeGroup.selectToggle(currMode);
		modeGroup.selectedToggleProperty().addListener((observable) -> {
			currMode = modeGroup.getSelectedToggle();
			if (currMode == mainFormButton) {
				toolBar.getItems().removeAll(vformChoice, helpButton);
				toolBar.getItems().addAll(searchTextField, clearButton, methodButton, inLabel, searchFieldChoice, helpButton);
				displayDictMain();
				setupTable();
			} else if (currMode == otherFormButton) {
				toolBar.getItems().removeAll(searchTextField, clearButton, methodButton, inLabel, searchFieldChoice, helpButton);
				toolBar.getItems().addAll(vformChoice, helpButton);
				displayDictOther();
				setupTable();
			}
		});	

		toolBar.getItems().addAll(new Separator(), mainFormButton, otherFormButton, new Separator(),
								searchTextField, clearButton, methodButton, inLabel, searchFieldChoice, helpButton);
		mainPane.setTop(toolBar);

		// add main content
		final Scene scene = new Scene(mainPane, windowWidth, windowHeight);
		table.setItems(outputList);
		table.setOnMouseClicked(mouseEvent -> showDetail());
		mainPane.setCenter(table);
		
		// add detail display on the bottom
		mainPane.setBottom(detailBox);

		// some initialization
		loadVerbsFromCPED();
		displayDictMain();
		setupTable();
		
		setScene(scene);

		// prepare info popup
		infoPopup.setContentWithText(GrammarUtilities.getTextResource("info-paliverbs.txt"));
		infoPopup.setTextWidth(Utilities.getRelativeSize(28));
	}

	private void setupTable() {
		final String termHead;
		final String formHead;
		final String meaningHead = "Meaning";
		if (currMode == mainFormButton) {
			termHead = "Verb";
			formHead = "Composition";
		} else {
			termHead = "Term";
			formHead = "Form";
		}
		final TableColumn<VerbOutput, String> termCol = new TableColumn<>(termHead);
		termCol.setCellValueFactory(new PropertyValueFactory<>(outputList.get(0).termProperty().getName()));
		termCol.prefWidthProperty().bind(mainPane.widthProperty().divide(11).multiply(3).subtract(10));
		final TableColumn<VerbOutput, String> formCol = new TableColumn<>(formHead);
		formCol.setCellValueFactory(new PropertyValueFactory<>(outputList.get(0).formProperty().getName()));
		formCol.prefWidthProperty().bind(mainPane.widthProperty().divide(11).multiply(4).subtract(5));
		final TableColumn<VerbOutput, String> meaningCol = new TableColumn<>(meaningHead);
		meaningCol.setCellValueFactory(new PropertyValueFactory<>(outputList.get(0).meaningProperty().getName()));
		meaningCol.prefWidthProperty().bind(mainPane.widthProperty().divide(11).multiply(4).subtract(5));
		table.getColumns().clear();
		table.getColumns().add(termCol);
		table.getColumns().add(formCol);
		table.getColumns().add(meaningCol);
	}

	private void loadVerbsFromCPED() {
		if (!cpedVerbMap.isEmpty()) return;
		// retrieve all verbs from CPED
		final String query = "SELECT TERM,POS,MEANING FROM CPED WHERE POS LIKE '%+%' AND POS NOT LIKE '%of%'";
		final java.sql.Connection conn = Utilities.H2DB.DICT.getConnection();
		try {
			if (conn != null) {
				final Statement stmt = conn.createStatement();
				final ResultSet rs = stmt.executeQuery(query);
				while (rs.next()) {
					final String term = rs.getString(1);
					final VerbOutput entry = new VerbOutput(term, rs.getString(2), rs.getString(3));
					cpedVerbMap.put(term, entry);
				}
				rs.close();		
				stmt.close();
			}
		} catch (SQLException e) {
			System.err.println(e);
		}
	}

	private void displayDictMain() {
		final String searchText = Normalizer.normalize(searchTextField.getText().toLowerCase().trim(), Form.NFC);
		displayDictMain(searchText);
	}

	private void displayDictMain(final String query) {
		outputList.clear();
		clearDetail();
		final List<VerbOutput> entryList;
		if (query.isEmpty()) {
			entryList = cpedVerbMap.values().stream()
						.sorted(VerbOutput::compareTo)
						.collect(Collectors.toList());
		} else {
			if (currSearchField == SearchField.VERB) {
				entryList = cpedVerbMap.values().stream()
							.filter(x->x.termProperty().get().startsWith(query))
							.sorted(VerbOutput::compareTo)
							.collect(Collectors.toList());
			} else if (currSearchField == SearchField.ROOT) {
				final Pattern p = Pattern.compile(".*(\\+\\s)*"+query+".+");
				entryList = cpedVerbMap.values().stream()
							.filter(x -> p.matcher(x.formProperty().get()).matches())
							.sorted(VerbOutput::compareTo)
							.collect(Collectors.toList());
			} else if (currSearchField == SearchField.PACCAYA) {
				entryList = cpedVerbMap.values().stream()
							.filter(x -> x.formProperty().get().endsWith(query))
							.sorted(VerbOutput::compareTo)
							.collect(Collectors.toList());
			} else {
				entryList = cpedVerbMap.values().stream()
							.filter(x -> x.meaningProperty().get().contains(query))
							.sorted(VerbOutput::compareTo)
							.collect(Collectors.toList());
			}
		}
		outputList.addAll(entryList);
		showItemCount();
	}

	private String getRelatedWords(final String term) {
		final StringBuilder result = new StringBuilder();
		// retrieve related verb forms from CPED
		final String query = "SELECT TERM,POS FROM CPED WHERE POS LIKE '%of "+term+"'";
		final java.sql.Connection conn = Utilities.H2DB.DICT.getConnection();
		try {
			if (conn != null) {
				final Statement stmt = conn.createStatement();
				final ResultSet rs = stmt.executeQuery(query);
				while (rs.next()) {
					final String item = rs.getString(1);
					result.append(item);
					result.append(" (");
					result.append(rs.getString(2).split(" ")[0]);
					result.append("), ");
				}
				rs.close();		
				stmt.close();
			}
		} catch (SQLException e) {
			System.err.println(e);
		}
		return result.toString();
	}

	private void showDetail() {
		clearDetail();
		final VerbOutput selected = table.getSelectionModel().getSelectedItem();
		if (selected == null) return;
		final Label lbHead = new Label(selected.termProperty().get() + " (" + selected.formProperty().get() + ")");
		final Label lbMeaning = new Label(" ‣ " + selected.meaningProperty().get());
		final String related = getRelatedWords(selected.termProperty().get());
		final Label lbRelated = new Label();
		if (!related.isEmpty()) {
			lbRelated.setText("Related terms: " + related.substring(0, related.lastIndexOf(",")));
		}
		detailBox.getChildren().addAll(lbHead, lbMeaning, lbRelated);
	}
	
	private void clearDetail() {
		detailBox.getChildren().clear();
	}

	private void displayDictOther() {
		outputList.clear();
		clearDetail();
		final String where = currOtherVerbForm == VerbForm.MISC ? 
							"WHERE POS = 'v.' OR POS LIKE 'pret.%'" : 
							"WHERE POS LIKE '"+ currOtherVerbForm.getAbbr() +"%'";
		final String query = "SELECT TERM,POS,MEANING FROM CPED " + where;
		final List<VerbOutput> entryList = new ArrayList<>();
		final java.sql.Connection conn = Utilities.H2DB.DICT.getConnection();
		try {
			if (conn != null) {
				final Statement stmt = conn.createStatement();
				final ResultSet rs = stmt.executeQuery(query);
				while (rs.next()) {
					final String term = rs.getString(1);
					final VerbOutput entry = new VerbOutput(term, rs.getString(2), rs.getString(3));
					entryList.add(entry);
				}
				rs.close();		
				stmt.close();
			}
		} catch (SQLException e) {
			System.err.println(e);
		}
		final List<VerbOutput> sortedList = entryList.stream()
											.sorted(VerbOutput::compareTo)
											.collect(Collectors.toList());
		outputList.addAll(sortedList);
		showItemCount();
	}

	private void showItemCount() {
		final int count = outputList.size();
		final String s = count <= 1 ? "" : "s";
		final Label lblCount = new Label(count + " item" + s + " listed");
		detailBox.getChildren().add(lblCount);
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
			final VerbOutput verb = table.getItems().get(i);
			final String[] data = new String[colCount];
			data[0] = verb.termProperty().get();
			data[1] = verb.formProperty().get();
			data[2] = verb.meaningProperty().get();
			result.add(data);
		}
		return result;
	}
	
	private void copyCSV() {
		Utilities.copyCSV(makeCSV());
	}
	
	private void saveCSV() {
		Utilities.saveCSV(makeCSV(), "paliverbs.csv");
	}
	
	// inner class
	public static final class VerbOutput {
		private StringProperty term;
		private StringProperty form;
		private StringProperty meaning;
		
		public VerbOutput(final String term, final String form, final String meaning) {
			termProperty().set(term);
			formProperty().set(form);
			meaningProperty().set(meaning);
		}
		
		public StringProperty termProperty() {
			if (term == null)
				term = new SimpleStringProperty(this, "term");
			return term;
		}
		
		public StringProperty formProperty() {
			if (form == null)
				form = new SimpleStringProperty(this, "form");
			return form;
		}
		
		public StringProperty meaningProperty() {
			if (meaning == null)
				meaning = new SimpleStringProperty(this, "meaning");
			return meaning;
		}
		public int compareTo(final VerbOutput other) {
			return Utilities.paliCollator.compare(this.termProperty().get(), other.termProperty().get());
		}
	} // end inner class
	
}
