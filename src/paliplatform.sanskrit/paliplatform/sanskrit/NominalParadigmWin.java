/*
 * NominalParadigmWin.java
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
import static paliplatform.sanskrit.SktDeclension.Case;
import static paliplatform.sanskrit.SktDeclension.Number;

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
 * The window showing Sanskrit nominal paradigms.
 * This is a singleton.
 * @author J.R. Bhaddacak
 * @version 4.1
 * @since 4.1
 */
public final class NominalParadigmWin extends SingletonWindow {
	public static final NominalParadigmWin INSTANCE = new NominalParadigmWin();
	private final SplitPane splitPane = new SplitPane();
	private final BorderPane mainPane = new BorderPane();
	private final GridPane declensionGrid = new GridPane();
	private final ChoiceBox<String> genderChoice = new ChoiceBox<>();
	private final ChoiceBox<String> wordTypeChoice = new ChoiceBox<>();
	private final TableView<ParadigmOutput> table = new TableView<>();	
	private final ObservableList<ParadigmOutput> outputList = FXCollections.<ParadigmOutput>observableArrayList();
	private final ObservableList<ProductOutput> productList = FXCollections.<ProductOutput>observableArrayList();
	private final Map<StringPair, Set<String>> productParadigmMap = new HashMap<>();
	private final ListView<ProductOutput> productListView = new ListView<>(productList);
	private final List<ParadigmOutput> paradigmList = new ArrayList<>();
	private final MenuItem openMenuItem = new MenuItem("Open Declension Table");
	private final TextField searchTextField;
	private final InfoPopup infoPopup = new InfoPopup();
	private double divPosition = Double.MAX_VALUE;

	private NominalParadigmWin() {
		windowWidth = Utilities.getRelativeSize(68);
		windowHeight = Utilities.getRelativeSize(46);
		setTitle("Sanskrit Nominal Paradigms");
		getIcons().add(new Image(NominalParadigmWin.class.getResourceAsStream("resources/images/stamp.png")));
		// add common toolbar on the top
		final CommonWorkingToolBar toolBar = new CommonWorkingToolBar(table, declensionGrid);
		// configure some buttons first
		toolBar.saveTextButton.setTooltip(new Tooltip("Save data as CSV"));
		toolBar.saveTextButton.setOnAction(actionEvent -> saveCSV());		
		toolBar.copyButton.setTooltip(new Tooltip("Copy CSV to clipboard"));
		toolBar.copyButton.setOnAction(actionEvent -> copyCSV());	
		// add new toolbar components
		genderChoice.setTooltip(new Tooltip("Gender selector"));
		genderChoice.getItems().add("ALL");
		for (final NominalParadigm.Gender gend : NominalParadigm.Gender.values) {
			genderChoice.getItems().add(gend.getName());
		}
		genderChoice.getSelectionModel().select(0);
		genderChoice.setOnAction(actionEvent -> updateOutput());
		wordTypeChoice.setTooltip(new Tooltip("Word type selector"));
		wordTypeChoice.getItems().add("ALL");
		for (final NominalParadigm.WordType type : NominalParadigm.WordType.values) {
			wordTypeChoice.getItems().add(type.getShortName());
		}
		wordTypeChoice.getSelectionModel().select(0);
		wordTypeChoice.setOnAction(actionEvent -> updateOutput());
		// add help button
		final Button helpButton = new Button("", new TextIcon("circle-question", TextIcon.IconSet.AWESOME));
		helpButton.setOnAction(actionEvent -> infoPopup.showPopup(helpButton, InfoPopup.Pos.BELOW_RIGHT, true));
		toolBar.getItems().addAll(new Separator(), genderChoice, wordTypeChoice, new Separator(), helpButton);
		mainPane.setTop(toolBar);

		// generate all paradigm list
		for (final String pname : SktDeclension.paradigmMap.keySet()) {
			final NominalParadigm parad = SktDeclension.paradigmMap.get(pname);
			paradigmList.add(new ParadigmOutput(parad));
		}

		// add product list on the left
		final VBox productBox = new VBox();
		final HBox searchBox = new HBox();
		searchBox.setPadding(new Insets(3));
		searchBox.setSpacing(3);
		final PaliTextInput searchInput = new PaliTextInput(PaliTextInput.InputType.FIELD);
		searchTextField = (TextField)searchInput.getInput();
		searchTextField.setPromptText("Ending filter");
		searchTextField.setAlignment(Pos.BOTTOM_RIGHT);
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
		productListView.setCellFactory((ListView<ProductOutput> lv) -> {
			return new ListCell<ProductOutput>() {
				@Override
				public void updateItem(ProductOutput item, boolean empty) {
					super.updateItem(item, empty);
					this.setGraphic(null);
					if (empty) {
						this.setText(null);
						this.setTooltip(null);
					} else {
						final ProductOutput value = this.getItem();
						this.setText(value.toString());
					}
					this.setAlignment(Pos.CENTER_RIGHT);
				}
			};
		});
		productListView.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> {
			final ProductOutput selItem = newValue;
			if (selItem != null)
				selectTableRows(selItem.getTerm());
		});
		VBox.setVgrow(productListView, Priority.ALWAYS);	
		productBox.getChildren().addAll(searchBox, productListView);
		mainPane.setRight(productBox);
		
		// prepare declension table grid
		declensionGrid.setAlignment(Pos.TOP_CENTER);
		declensionGrid.setHgap(20);
		declensionGrid.setVgap(3);

		// add main table at the center
		splitPane.setOrientation(Orientation.VERTICAL);
		table.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		table.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> {
			final ParadigmOutput selItem = newValue;
			if (selItem != null) {
				openMenuItem.setDisable(!"Noun".equals(selItem.wordTypeProperty().get()));
				showDeclension(selItem);
			}
		});
		openMenuItem.setOnAction(actionEvent -> openDeclension());		
		final ContextMenu popupMenu = new ContextMenu(openMenuItem);
		table.setContextMenu(popupMenu);
		outputList.addAll(paradigmList);
		setupTable();
		table.setItems(outputList);
		splitPane.getItems().addAll(table);
		mainPane.setCenter(splitPane);
		final Scene scene = new Scene(mainPane, windowWidth, windowHeight);
		setScene(scene);

		// prepare info popup
		infoPopup.setContentWithText(SanskritUtilities.getTextResource("info-nominal-paradigms.txt"));
		infoPopup.setTextWidth(Utilities.getRelativeSize(38));		

		updateProduct();
	}

	private void setupTable() {
		final TableColumn<ParadigmOutput, String> nameCol = createParadigmTableColumn("Name", outputList.get(0).paradNameProperty().getName()); 
		nameCol.prefWidthProperty().bind(mainPane.widthProperty().subtract(Utilities.getRelativeSize(8)).divide(15).multiply(2.5));
		nameCol.setComparator(Utilities.sktComparator);
		final TableColumn<ParadigmOutput, String> genderCol = createParadigmTableColumn("Gender", outputList.get(0).genderProperty().getName());
		genderCol.prefWidthProperty().bind(mainPane.widthProperty().subtract(Utilities.getRelativeSize(6)).divide(15).multiply(2));
		final TableColumn<ParadigmOutput, String> bucknellCol = createParadigmTableColumn("#", outputList.get(0).bucknellNumberProperty().getName()); 
		bucknellCol.prefWidthProperty().bind(mainPane.widthProperty().subtract(Utilities.getRelativeSize(6)).divide(15).multiply(0.6));
		bucknellCol.setSortable(false);
		final TableColumn<ParadigmOutput, String> sampleCol = createParadigmTableColumn("Nominative Samples", outputList.get(0).nominativeSampleProperty().getName()); 
		sampleCol.prefWidthProperty().bind(mainPane.widthProperty().subtract(Utilities.getRelativeSize(8)).divide(15).multiply(7));
		sampleCol.setComparator(Utilities.sktComparator);
		final TableColumn<ParadigmOutput, String> wordTypeCol = createParadigmTableColumn("Type", outputList.get(0).wordTypeProperty().getName());
		wordTypeCol.prefWidthProperty().bind(mainPane.widthProperty().subtract(Utilities.getRelativeSize(6)).divide(15).multiply(1.5));
		table.getColumns().clear();
		table.getColumns().add(nameCol);
		table.getColumns().add(genderCol);
		table.getColumns().add(bucknellCol);
		table.getColumns().add(sampleCol);
		table.getColumns().add(wordTypeCol);	
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
		declensionGrid.getChildren().clear();
		if (splitPane.getItems().size() > 1) {
			divPosition = splitPane.getDividerPositions()[0];
			splitPane.getItems().remove(1, 2);
		}
		final String genderSelected = genderChoice.getSelectionModel().getSelectedItem();
		final String typeSelected = wordTypeChoice.getSelectionModel().getSelectedItem();
		if (genderSelected == null || typeSelected == null) return;
		final String gend = genderSelected.substring(0, 3);
		final Predicate<ParadigmOutput> gendCondition = gend.equals("ALL")
										? x -> true
										: x -> x.genderProperty().get().contains(gend);
		final Predicate<ParadigmOutput> typeCondition = typeSelected.equals("ALL")
										? x -> true
										: x -> x.wordTypeProperty().get().contains(typeSelected);
		final List<ParadigmOutput> result = paradigmList.stream()
											.filter(gendCondition)
											.filter(typeCondition)
											.collect(Collectors.toList());
		outputList.addAll(result);
		table.scrollTo(0);
		table.getSelectionModel().clearSelection();
		updateProduct();
	}

	private void updateProduct() {
		final String strQuery = Normalizer.normalize(searchTextField.getText().trim(), Form.NFC).toLowerCase();
		final String revQuery = SanskritUtilities.reverseString(strQuery);
		productList.clear();
		productParadigmMap.clear();
		for (final ParadigmOutput pout : outputList) {
			final Set<StringPair> allTerms = pout.getAllProducts();
			for (final StringPair term : allTerms) {
				if (revQuery.isEmpty() || term.getSecond().startsWith(revQuery)) {
					final Set<String> paradList = productParadigmMap.getOrDefault(term, new HashSet<>());
					paradList.add(pout.paradNameProperty().get());
					productParadigmMap.put(term, paradList);
				}
			}
		}
		final List<ProductOutput> sorted = productParadigmMap.keySet().stream()
										.sorted((x, y) -> Utilities.sktComparator.compare(x.getSecond(), y.getSecond()))
										.map(ProductOutput::new)
										.collect(Collectors.toList());
		productList.addAll(sorted);
		productListView.scrollTo(0);
		productListView.getSelectionModel().clearSelection();
	}

	private void selectTableRows(final StringPair term) {
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

	private void showDeclension(final ParadigmOutput parad) {
		declensionGrid.getChildren().clear();
		final Map<Case, Map<Number, List<String>>> product = parad.getProduct();
		final String[] strHead = { "Case", Number.SING.getName(), Number.DUAL.getName(), Number.PLU.getName() };
		final String[] strResult = { "", "", "", "" };
		product.forEach((k, v) -> {
			strResult[0] = strResult[0] + k.getNumAbbr() + " "  + k.getAbbr() + "\n";
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
			declensionGrid.getChildren().add(lbHead);
		}
		for (int i = 0; i < strResult.length; i++) {
			final Label lbResult = new Label(strResult[i]);
			if (i == 0)
				lbResult.setStyle("-fx-font-weight: bold;");
			GridPane.setConstraints(lbResult, i, 1);
			declensionGrid.getChildren().add(lbResult);
		}
		if (splitPane.getItems().size() == 1) {
			splitPane.setDividerPositions(divPosition == Double.MAX_VALUE ? 0.7 : divPosition);
			splitPane.getItems().add(declensionGrid);
		}
	}

	private void openDeclension() {
		final ObservableList<Integer> selected = table.getSelectionModel().getSelectedIndices();
		if (selected == null || selected.isEmpty()) return;
		final ParadigmOutput parad = outputList.get(selected.get(0));
		final String pname = parad.paradNameProperty().get();
		final Object[] args = new Object[] { pname };
		SanskritUtilities.openWindow(Utilities.WindowType.SKTDECLENSION, args);
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
			data[1] = parad.genderProperty().get();
			data[2] = parad.bucknellNumberProperty().get();
			data[3] = parad.nominativeSampleProperty().get();
			data[4] = parad.wordTypeProperty().get();
			result.add(data);
		}
		return result;
	}

	private void copyCSV() {
		Utilities.copyCSV(makeCSV());
	}
	
	private void saveCSV() {
		Utilities.saveCSV(makeCSV(), "nominal-paradigms.csv");
	}
	
	// inner classes
	public static final class ParadigmOutput {
		private StringProperty paradName;
		private StringProperty gender;
		private StringProperty bucknellNumber;
		private StringProperty nominativeSample;
		private StringProperty wordType;
		private final Map<Case, Map<Number, List<String>>> product;
		
		public ParadigmOutput(final NominalParadigm parad) {
			paradNameProperty().set(parad.getName());
			genderProperty().set(parad.getGenderStr());
			bucknellNumberProperty().set(parad.getBucknellNumberStr());
			wordTypeProperty().set(parad.getWordType().getShortName());
			product = parad.getSampleProduct();
			final Map<Number, List<String>> numMap = product.get(Case.NOM);
			final List<String> decl = new ArrayList<>();
			for (final Number n : Number.values) {
				final List<String> tlist = numMap.get(n);
				final String term = tlist.get(0);
				decl.add(term.isEmpty() ? "-" : term);
			}
			nominativeSampleProperty().set(decl.stream().collect(Collectors.joining(" : ")));
		} 

		public Map<Case, Map<Number, List<String>>> getProduct() {
			return product;
		}

		public Set<StringPair> getAllProducts() {
			final Set<StringPair> result = new HashSet<>();
			for (final Map<Number, List<String>> numMap : product.values()) {
				for (final List<String> plist : numMap.values()) {
					for (final String term : plist) {
						if (term.isEmpty()) continue;
						final String reverse = SanskritUtilities.reverseString(term);
						final StringPair pair = new StringPair(term, reverse);
						result.add(pair);
					}
				}
			}
			return result;
		}
		
		public StringProperty paradNameProperty() {
			if (paradName == null)
				paradName = new SimpleStringProperty(this, "paradName");
			return paradName;
		}
		
		public StringProperty genderProperty() {
			if (gender == null)
				gender = new SimpleStringProperty(this, "gender");
			return gender;
		}
		
		public StringProperty bucknellNumberProperty() {
			if (bucknellNumber == null)
				bucknellNumber = new SimpleStringProperty(this, "bucknellNumber");
			return bucknellNumber;
		}
		
		public StringProperty nominativeSampleProperty() {
			if (nominativeSample == null)
				nominativeSample = new SimpleStringProperty(this, "nominativeSample");
			return nominativeSample;
		}
		
		public StringProperty wordTypeProperty() {
			if (wordType == null)
				wordType = new SimpleStringProperty(this, "wordType");
			return wordType;
		}
	}
		
	static class ProductOutput {
		private final StringPair term;
		public ProductOutput(final StringPair t) {
			term = t;
		}
		public StringPair getTerm() {
			return term;
		}
		@Override
		public String toString() {
			return term.getFirst();
		}
	}

}
