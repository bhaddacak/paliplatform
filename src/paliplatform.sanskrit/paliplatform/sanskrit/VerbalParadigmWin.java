/*
 * VerbalParadigmWin.java
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
import static paliplatform.sanskrit.SktConjugation.Person;
import static paliplatform.sanskrit.SktConjugation.Number;
import static paliplatform.sanskrit.SktConjugation.Pada;
import static paliplatform.sanskrit.SktConjugation.TenseMood;

import java.util.*;
import java.util.stream.*;
import java.util.function.Predicate;
import java.text.Normalizer;
import java.text.Normalizer.Form;

import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.*;
import javafx.scene.layout.*;
import javafx.beans.property.*;
import javafx.scene.input.*;
import javafx.scene.image.*;
import javafx.scene.text.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;

/** 
 * The window showing Sanskrit verbal paradigms.
 * This is a singleton.
 * @author J.R. Bhaddacak
 * @version 4.1
 * @since 4.1
 */
public final class VerbalParadigmWin extends SingletonWindow {
	public static final VerbalParadigmWin INSTANCE = new VerbalParadigmWin();
	private final SplitPane splitPane = new SplitPane();
	private final BorderPane mainPane = new BorderPane();
	private final GridPane conjugationGrid = new GridPane();
	private final ChoiceBox<String> tenseMoodChoice = new ChoiceBox<>();
	private final ChoiceBox<String> padaChoice = new ChoiceBox<>();
	private final TableView<ParadigmOutput> table = new TableView<>();	
	private final ObservableList<ParadigmOutput> outputList = FXCollections.<ParadigmOutput>observableArrayList();
	private final ObservableList<String> productList = FXCollections.<String>observableArrayList();
	private final Map<String, Set<String>> productParadigmMap = new HashMap<>();
	private final ListView<String> productListView = new ListView<>(productList);
	private final List<ParadigmOutput> paradigmList = new ArrayList<>();
	private final TextField searchTextField;
	private final InfoPopup infoPopup = new InfoPopup();
	private double divPosition = Double.MAX_VALUE;

	private VerbalParadigmWin() {
		windowWidth = Utilities.getRelativeSize(68);
		windowHeight = Utilities.getRelativeSize(46);
		setTitle("Sanskrit Verbal Paradigms");
		getIcons().add(new Image(VerbalParadigmWin.class.getResourceAsStream("resources/images/stamp.png")));
		// add common toolbar on the top
		final CommonWorkingToolBar toolBar = new CommonWorkingToolBar(table, conjugationGrid);
		// configure some buttons first
		toolBar.saveTextButton.setTooltip(new Tooltip("Save data as CSV"));
		toolBar.saveTextButton.setOnAction(actionEvent -> saveCSV());		
		toolBar.copyButton.setTooltip(new Tooltip("Copy CSV to clipboard"));
		toolBar.copyButton.setOnAction(actionEvent -> copyCSV());	
		// add new toolbar components
		tenseMoodChoice.setTooltip(new Tooltip("Tense/Mood selector"));
		tenseMoodChoice.getItems().add("ALL");
		for (final TenseMood tense : TenseMood.values) {
			if (tense.ordinal() > 7) break; // not future and conditional
			tenseMoodChoice.getItems().add(tense.getFullName());
		}
		tenseMoodChoice.getSelectionModel().select(0);
		tenseMoodChoice.setOnAction(actionEvent -> updateOutput());
		padaChoice.setTooltip(new Tooltip("Pada selector"));
		padaChoice.getItems().add("ALL");
		for (final Pada pada : Pada.values) {
			padaChoice.getItems().add(pada.getSktName());
		}
		padaChoice.getSelectionModel().select(0);
		padaChoice.setOnAction(actionEvent -> updateOutput());
		// add help button
		final Button helpButton = new Button("", new TextIcon("circle-question", TextIcon.IconSet.AWESOME));
		helpButton.setOnAction(actionEvent -> infoPopup.showPopup(helpButton, InfoPopup.Pos.BELOW_RIGHT, true));
		toolBar.getItems().addAll(new Separator(), tenseMoodChoice, padaChoice, new Separator(), helpButton);
		mainPane.setTop(toolBar);

		// generate all paradigm list
		for (final String pname : SktConjugation.paradigmMap.keySet()) {
			final VerbalParadigm parad = SktConjugation.paradigmMap.get(pname);
			paradigmList.add(new ParadigmOutput(parad));
		}

		// add product list on the left
		final VBox productBox = new VBox();
		final HBox searchBox = new HBox();
		searchBox.setPadding(new Insets(3));
		searchBox.setSpacing(3);
		final PaliTextInput searchInput = new PaliTextInput(PaliTextInput.InputType.FIELD);
		searchTextField = (TextField)searchInput.getInput();
		searchTextField.setPromptText("Filter for...");
		searchTextField.setPrefWidth(Utilities.getRelativeSize(8));
		searchTextField.textProperty().addListener((obs, oldValue, newValue) -> updateProduct());
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
		productListView.setPrefWidth(Utilities.getRelativeSize(10));
		productListView.setCellFactory((ListView<String> lv) -> {
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
						this.setText(value.toString());
					}
				}
			};
		});
		productListView.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> {
			final String selItem = newValue;
			if (selItem != null)
				selectTableRows(selItem);
		});
		VBox.setVgrow(productListView, Priority.ALWAYS);	
		productBox.getChildren().addAll(searchBox, productListView);
		mainPane.setRight(productBox);
		
		// prepare declension table grid
		conjugationGrid.setAlignment(Pos.TOP_CENTER);
		conjugationGrid.setHgap(20);
		conjugationGrid.setVgap(3);

		// add main table at the center
		splitPane.setOrientation(Orientation.VERTICAL);
		table.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		table.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> {
			final ParadigmOutput selItem = newValue;
			if (selItem != null) {
				showConjugation(selItem);
			}
		});
		outputList.addAll(paradigmList);
		setupTable();
		table.setItems(outputList);
		splitPane.getItems().addAll(table);
		mainPane.setCenter(splitPane);
		final Scene scene = new Scene(mainPane, windowWidth, windowHeight);
		setScene(scene);

		// prepare info popup
		infoPopup.setContentWithText(SanskritUtilities.getTextResource("info-verbal-paradigms.txt"));
		infoPopup.setTextWidth(Utilities.getRelativeSize(38));

		updateProduct();
	}

	private void setupTable() {
		final TableColumn<ParadigmOutput, String> nameCol = createParadigmTableColumn("Name", outputList.get(0).paradNameProperty().getName()); 
		nameCol.prefWidthProperty().bind(mainPane.widthProperty().subtract(Utilities.getRelativeSize(8)).divide(15).multiply(2));
		nameCol.setComparator(Utilities.sktComparator);
		final TableColumn<ParadigmOutput, String> bucknellCol = createParadigmTableColumn("Ref", outputList.get(0).bucknellNumberProperty().getName()); 
		bucknellCol.prefWidthProperty().bind(mainPane.widthProperty().subtract(Utilities.getRelativeSize(6)).divide(15).multiply(1));
		bucknellCol.setSortable(false);
		final TableColumn<ParadigmOutput, String> tenseMoodCol = createParadigmTableColumn("Tense/Mood", outputList.get(0).tenseMoodProperty().getName());
		tenseMoodCol.prefWidthProperty().bind(mainPane.widthProperty().subtract(Utilities.getRelativeSize(6)).divide(15).multiply(2.5));
		final TableColumn<ParadigmOutput, String> padaCol = createParadigmTableColumn("Pada", outputList.get(0).padaProperty().getName());
		padaCol.prefWidthProperty().bind(mainPane.widthProperty().subtract(Utilities.getRelativeSize(6)).divide(15).multiply(1.5));
		final TableColumn<ParadigmOutput, String> sampleCol = createParadigmTableColumn("Prathama Samples", outputList.get(0).prathamaSampleProperty().getName()); 
		sampleCol.prefWidthProperty().bind(mainPane.widthProperty().subtract(Utilities.getRelativeSize(12)).divide(15).multiply(7));
		sampleCol.setComparator(Utilities.sktComparator);
		table.getColumns().clear();
		table.getColumns().add(nameCol);
		table.getColumns().add(bucknellCol);
		table.getColumns().add(tenseMoodCol);
		table.getColumns().add(padaCol);	
		table.getColumns().add(sampleCol);
	}
	
	private TableColumn<ParadigmOutput, String> createParadigmTableColumn(final String colName, final String propValue) {
		final TableColumn<ParadigmOutput, String> col = new TableColumn<>(colName);
		col.setCellValueFactory(new PropertyValueFactory<>(propValue));
		col.setCellFactory(c -> {
			TableCell<ParadigmOutput, String> cell = new TableCell<ParadigmOutput, String>() {
				@Override
				public void updateItem(String item, boolean empty) {
					super.updateItem(item, empty);
					this.setText(null);
					this.setGraphic(null);
					if (!empty) {
						final Text text = new Text(item);
						text.getStyleClass().add("shape");                      
						this.setGraphic(text);
					}
				}
			};
			return cell;
		});
		col.setReorderable(false);
		return col;	
	}

	private void updateOutput() {
		outputList.clear();
		conjugationGrid.getChildren().clear();
		if (splitPane.getItems().size() > 1) {
			divPosition = splitPane.getDividerPositions()[0];
			splitPane.getItems().remove(1, 2);
		}
		final int tenseMoodIndex = tenseMoodChoice.getSelectionModel().getSelectedIndex();
		final int padaIndex = padaChoice.getSelectionModel().getSelectedIndex();
		if (tenseMoodIndex < 0 || padaIndex < 0) return;
		final boolean allTenseMood = tenseMoodIndex == 0;
		final TenseMood selectedTenseMood = allTenseMood ? null : TenseMood.values[tenseMoodIndex-1];
		final boolean allPada = padaIndex == 0;
		final Pada selectedPada = allPada ? null : Pada.values[padaIndex-1];
		final Predicate<ParadigmOutput> tenseMoodCondition = allTenseMood
										? x -> true
										: x -> x.getTenseMood() == selectedTenseMood;
		final Predicate<ParadigmOutput> padaCondition = allPada
										? x -> true
										: x -> x.getPada() == selectedPada;
		final List<ParadigmOutput> result = paradigmList.stream()
											.filter(tenseMoodCondition)
											.filter(padaCondition)
											.collect(Collectors.toList());
		outputList.addAll(result);
		table.scrollTo(0);
		table.getSelectionModel().clearSelection();
		updateProduct();
	}

	private void updateProduct() {
		final String strQuery = Normalizer.normalize(searchTextField.getText().trim(), Form.NFC).toLowerCase();
		final String queryFinal = strQuery.replace("?", ".").replace("*", ".*");
		productList.clear();
		productParadigmMap.clear();
		for (final ParadigmOutput pout : outputList) {
			final Set<String> allTerms = pout.getAllProducts();
			for (final String term : allTerms) {
				final boolean condition = queryFinal.contains(".")
										? term.matches(queryFinal)
										: term.startsWith(queryFinal);
				if (condition) {
					final Set<String> paradList = productParadigmMap.getOrDefault(term, new HashSet<>());
					paradList.add(pout.paradNameProperty().get());
					productParadigmMap.put(term, paradList);
				}
			}
		}
		final List<String> sorted = productParadigmMap.keySet().stream()
										.sorted(Utilities.sktComparator)
										.collect(Collectors.toList());
		productList.addAll(sorted);
		productListView.scrollTo(0);
		productListView.getSelectionModel().clearSelection();
	}

	private void selectTableRows(final String term) {
		table.getSelectionModel().clearSelection();
		final Set<String> paradSet = productParadigmMap.get(term);
		if (paradSet == null) return;
		int min = Integer.MAX_VALUE;
		for (int i = outputList.size() - 1; i >= 0 ; i--) {
			final ParadigmOutput parad = outputList.get(i);
			if (paradSet.contains(parad.paradNameProperty().get())) {
				table.getSelectionModel().select(i);
				if (i < min)
					min = i;
			}
		}
		if (min < Integer.MAX_VALUE)
			table.scrollTo(min);
	}

	private void showConjugation(final ParadigmOutput parad) {
		conjugationGrid.getChildren().clear();
		final Map<Person, Map<Number, List<String>>> product = parad.getProduct();
		final String[] strHead = { "Person", Number.SING.getName(), Number.DUAL.getName(), Number.PLU.getName() };
		final String[] strResult = { "", "", "", "" };
		product.forEach((k, v) -> {
			strResult[0] = strResult[0] + k.getAbbr() + "\n";
			final String singStr = v.get(Number.SING).stream().collect(Collectors.joining(", "));
			final String dualStr = v.get(Number.DUAL).stream().collect(Collectors.joining(", "));
			final String pluralStr = v.get(Number.PLU).stream().collect(Collectors.joining(", "));
			strResult[1] = strResult[1] + (singStr.isEmpty() ? "-" : singStr) + "\n";
			strResult[2] = strResult[2] + (dualStr.isEmpty() ? "-" : dualStr) + "\n";
			strResult[3] = strResult[3] + (pluralStr.isEmpty() ? "-" : pluralStr) + "\n";
		});
		for (int i = 0; i < strHead.length; i++) {
			final Label lbHead = new Label(strHead[i]);
			lbHead.setStyle("-fx-font-weight: bold;");
			GridPane.setConstraints(lbHead, i, 0);
			conjugationGrid.getChildren().add(lbHead);
		}
		for (int i = 0; i < strResult.length; i++) {
			final Label lbResult = new Label(strResult[i]);
			if (i == 0)
				lbResult.setStyle("-fx-font-weight: bold;");
			GridPane.setConstraints(lbResult, i, 1);
			conjugationGrid.getChildren().add(lbResult);
		}
		if (splitPane.getItems().size() == 1) {
			splitPane.setDividerPositions(divPosition == Double.MAX_VALUE ? 0.8 : divPosition);
			splitPane.getItems().add(conjugationGrid);
		}
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
			final ParadigmOutput parad = table.getItems().get(i);
			final String[] data = new String[colCount];
			data[0] = parad.paradNameProperty().get();
			data[1] = parad.bucknellNumberProperty().get();
			data[2] = parad.tenseMoodProperty().get();
			data[3] = parad.padaProperty().get();
			data[4] = parad.prathamaSampleProperty().get();
			result.add(data);
		}
		return result;
	}

	private void copyCSV() {
		Utilities.copyCSV(makeCSV());
	}
	
	private void saveCSV() {
		Utilities.saveCSV(makeCSV(), "verbal-paradigms.csv");
	}
	
	// inner classes
	public static final class ParadigmOutput {
		private StringProperty paradName;
		private StringProperty bucknellNumber;
		private StringProperty tenseMood;
		private StringProperty pada;
		private StringProperty prathamaSample;
		private final TenseMood tense;
		private final Pada pad;
		private final Map<Person, Map<Number, List<String>>> product;
		
		public ParadigmOutput(final VerbalParadigm parad) {
			paradNameProperty().set(parad.getName());
			bucknellNumberProperty().set(parad.getBucknellNumber());
			tense = parad.getTenseMood();
			tenseMoodProperty().set(tense.getName());
			pad = parad.getPada();
			padaProperty().set(pad.getName());
			product = parad.getSampleProduct();
			final Map<Number, List<String>> numMap = product.get(Person.PRATHAMA);
			final List<String> conjug = new ArrayList<>();
			for (final Number n : Number.values) {
				final List<String> tlist = numMap.get(n);
				final String term = tlist.get(0);
				conjug.add(term.isEmpty() ? "-" : term);
			}
			prathamaSampleProperty().set(conjug.stream().collect(Collectors.joining(" : ")));
		} 

		public Map<Person, Map<Number, List<String>>> getProduct() {
			return product;
		}

		public Set<String> getAllProducts() {
			final Set<String> result = new HashSet<>();
			for (final Map<Number, List<String>> numMap : product.values()) {
				for (final List<String> plist : numMap.values()) {
					for (final String term : plist) {
						if (term.isEmpty()) continue;
						result.add(term);
					}
				}
			}
			return result;
		}
	
		public TenseMood getTenseMood() {
			return tense;
		}

		public Pada getPada() {
			return pad;
		}

		public StringProperty paradNameProperty() {
			if (paradName == null)
				paradName = new SimpleStringProperty(this, "paradName");
			return paradName;
		}
		
		public StringProperty bucknellNumberProperty() {
			if (bucknellNumber == null)
				bucknellNumber = new SimpleStringProperty(this, "bucknellNumber");
			return bucknellNumber;
		}
		
		public StringProperty tenseMoodProperty() {
			if (tenseMood == null)
				tenseMood = new SimpleStringProperty(this, "tenseMood");
			return tenseMood;
		}
		
		public StringProperty padaProperty() {
			if (pada == null)
				pada = new SimpleStringProperty(this, "pada");
			return pada;
		}
		
		public StringProperty prathamaSampleProperty() {
			if (prathamaSample == null)
				prathamaSample = new SimpleStringProperty(this, "prathamaSample");
			return prathamaSample;
		}
	}
		
}

