/*
 * DeclensionWin.java
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
import paliplatform.base.PaliWord;
import paliplatform.dict.*;

import java.util.*;
import java.text.Normalizer;
import java.text.Normalizer.Form;
import java.util.stream.Collectors;

import javafx.application.Platform;
import javafx.scene.control.*;
import javafx.scene.control.cell.*;
import javafx.scene.input.*;
import javafx.scene.text.*;
import javafx.scene.layout.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.beans.property.*;
import javafx.util.Callback;
import javafx.geometry.*;

/** 
 * The declension table window.
 * @author J.R. Bhaddacak
 * @version 3.0
 * @since 2.0
 */
public class DeclensionWin extends BorderPane {
	public static enum Mode {
		NOUN("Nouns/Adj"), PRONOUN("Pronouns"), NUMBER("Numbers");
		public static final Mode[] values = values();
		public String name;
		private Mode(final String name) {
			this.name = name;
		}
		public String getDescription() {
			final String desc;
			if (this == NOUN)
				desc = "Pāli nouns and adjectives";
			else if (this == PRONOUN)
				desc = "Pāli pronouns";
			else if (this == NUMBER)
				desc = "Pāli numerals";
			else
				desc = "";
			return desc;
		}
	};
	private final SimpleObjectProperty<Toggle> currMode = new SimpleObjectProperty<>();
	private final ObservableList<String> wordList = FXCollections.<String>observableArrayList();
	private final ListView<String> wordListView = new ListView<>(wordList);
	private final ObservableList<DeclensionOutput> outputList = FXCollections.<DeclensionOutput>observableArrayList();
	private final TableView<DeclensionOutput> table = new TableView<>();	
	private final EnumMap<Mode, Toggle> toggleMap = new EnumMap<>(Mode.class);
	private final EnumMap<Mode, HBox> toolbarMap = new EnumMap<>(Mode.class);
	private final BorderPane mainPane = new BorderPane();
	private final SplitPane splitPane = new SplitPane();
	private final VBox wordFreqPane = new VBox();
	private final ToggleGroup toggleModeGroup = new ToggleGroup();
	private final CheckBox cbShowMeaning = new CheckBox("Show meaning");
	private final PaliTextInput nounTextInput = new PaliTextInput(PaliTextInput.InputType.FIELD);
	private final TextField nounTextField;
	private final PaliTextInput numbTextInput = new PaliTextInput(PaliTextInput.InputType.FIELD);
	private final TextField numbTextField;
	private final Button computeButton = new Button("Compute");
	private final CheckBox showNumValueButton = new CheckBox("123");
	private final RadioButton cardinalButton = new RadioButton("Cardinal");
	private final HBox nounToolBox = new HBox();
	private final HBox pronToolBox = new HBox();
	private final HBox numbToolBox = new HBox();
	private final HBox genderToolBox = new HBox();
	private final InfoPopup infoPopup = new InfoPopup();
	private final Callback<TableColumn<DeclensionOutput, String>, TableCell<DeclensionOutput, String>> declOutputCellFactory;
	private final ObservableList<String> wordFreqList = FXCollections.<String>observableArrayList();
	private final ListView<String> wordFreqListView = new ListView<>(wordFreqList);
	private final Set<String> allWords = new HashSet<>();
	private final ChoiceBox<String> listerTableChoice = new ChoiceBox<>();
	private HBox currToolBox;
	private Map<String, PaliWord> currNumberMap;
	private Map<String, PaliWord> calPaliNumMap;
	private PaliWord currWord;

	public DeclensionWin(final Object[] args) {
		// add main content
		// prepare toolbar for nouns/adj
		nounTextField = (TextField)nounTextInput.getInput();
		nounTextField.setPromptText("Enter some word...");
		nounTextField.textProperty().addListener((obs, oldValue, newValue) -> {
			final String strQuery = Normalizer.normalize(newValue.trim(), Form.NFC);
			if (!strQuery.isEmpty())
				showNounList(strQuery.toLowerCase());
		});
		nounTextField.setOnKeyPressed(keyEvent -> {
			if (keyEvent.getEventType() == KeyEvent.KEY_PRESSED) {
				final KeyCode key = keyEvent.getCode();
				if (keyEvent.isControlDown()) {
					if (key == KeyCode.SPACE) {
						nounTextInput.rotateInputMethod();
					}					
				} else {
					if (key == KeyCode.ENTER) {
						if (!computeButton.isDisable())
							computeNoun();
					} else if (key == KeyCode.ESCAPE) {
						nounTextField.clear();
					}
				}
			}
		});	
		final Button namClearButton = nounTextInput.getClearButton();
		namClearButton.setOnAction(actionEvent -> nounTextField.clear());
		computeButton.setOnAction(actionEvent -> computeNoun());
		final Button resetNounButton = new Button("", new TextIcon("repeat", TextIcon.IconSet.AWESOME));
		resetNounButton.setTooltip(new Tooltip("Reset/Uncompute"));
		resetNounButton.setOnAction(actionEvent -> showResult());
		nounToolBox.setPadding(new Insets(3));
		nounToolBox.setSpacing(3);
		nounToolBox.getChildren().addAll(nounTextField, namClearButton, nounTextInput.getMethodButton(), computeButton, resetNounButton);
		// prepare toolbar for pronouns
		cbShowMeaning.setAllowIndeterminate(false);
		cbShowMeaning.setOnAction(actionEvent -> showPronounList());
		pronToolBox.setPadding(new Insets(3, 3, 3, 3));
		pronToolBox.setSpacing(3);
		pronToolBox.getChildren().addAll(cbShowMeaning);
		// prepare toolbar for numbers
		numbTextField = (TextField)numbTextInput.getInput();
		numbTextField.setPromptText("Enter some number...");
		numbTextInput.setInputMethod(PaliTextInput.InputMethod.NUMBER);
		numbTextInput.setLimit(6);
		numbTextField.textProperty().addListener((obs, oldValue, newValue) -> computeNumber());
		showNumValueButton.setTooltip(new Tooltip("Show numbers"));
		showNumValueButton.setSelected(false);
		showNumValueButton.setOnAction(actionEvent -> showNumberList());
		final Button resetNumberButton = new Button("", new TextIcon("repeat", TextIcon.IconSet.AWESOME));
		resetNumberButton.setTooltip(new Tooltip("Reset"));
		resetNumberButton.setOnAction(actionEvent -> { 
			numbTextField.clear();
			if (cardinalButton.isSelected())
				currNumberMap = GrammarUtilities.paliNumerals;
			else
				currNumberMap = GrammarUtilities.paliOrdinals;
			showNumberList();
		});
		final ToggleGroup toggleNumberGroup = new ToggleGroup();
		cardinalButton.setOnAction(actionEvent -> numModeSelected());
		final RadioButton ordinalButton = new RadioButton("Ordinal");
		ordinalButton.setOnAction(actionEvent -> numModeSelected());
		ordinalButton.setToggleGroup(toggleNumberGroup);
		cardinalButton.setToggleGroup(toggleNumberGroup);
		cardinalButton.setSelected(true);
		numbToolBox.setAlignment(Pos.BOTTOM_LEFT);
		numbToolBox.setPadding(new Insets(3));
		numbToolBox.setSpacing(3);
		numbToolBox.getChildren().addAll(numbTextField, numbTextInput.getClearButton(), resetNumberButton, showNumValueButton,
										new Separator(Orientation.VERTICAL), cardinalButton, ordinalButton);
				
		// add result list on the left
		wordListView.setPrefWidth(Utilities.getRelativeSize(12.5));
		wordListView.setCellFactory((ListView<String> lv) -> {
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
						this.setTooltip(new Tooltip(value));
						this.setText(value);
					}
					this.setStyle("-fx-padding: 0px 0px 0px 3px");
				}
			};
		});
		wordListView.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> {
			final String selItem = newValue;
			if (selItem != null)
				showResult(selItem);
		});
		
		// add common toolbar on the top
		final CommonWorkingToolBar commonToolbar = new CommonWorkingToolBar(table);
		// config some buttons
		final SimpleListProperty<DeclensionOutput> outputListProperty = new SimpleListProperty<>(outputList);
		commonToolbar.saveTextButton.setOnAction(actionEvent -> saveCSV());
		commonToolbar.saveTextButton.disableProperty().bind(outputListProperty.sizeProperty().isEqualTo(0));
		commonToolbar.saveTextButton.setTooltip(new Tooltip("Save data as CSV"));
		commonToolbar.copyButton.setOnAction(actionEvent -> copyCSV());		
		commonToolbar.copyButton.disableProperty().bind(outputListProperty.sizeProperty().isEqualTo(0));
		commonToolbar.copyButton.setTooltip(new Tooltip("Copy CSV to clipboard"));
		// add new buttons
		commonToolbar.getItems().add(new Separator());
		for (final Mode m : Mode.values) {
			final RadioButton tb = new RadioButton(m.name);
			tb.setTooltip(new Tooltip(m.getDescription()));
			tb.setToggleGroup(toggleModeGroup);
			tb.setUserData(m);
			toggleMap.put(m, tb);
			commonToolbar.getItems().add(tb);
		}
		currMode.bind(toggleModeGroup.selectedToggleProperty());
        toggleModeGroup.selectedToggleProperty().addListener((observable) -> {
			final Mode mode = (Mode)currMode.get().getUserData();
			init(mode, null);
			if (mode == Mode.PRONOUN) {
				showPronounList();
			} else if (mode == Mode.NUMBER) {
				showNumberList();
			}
			wordListView.scrollTo(0);
        });
		final Button wordFreqListButton = new Button("", new TextIcon("list", TextIcon.IconSet.AWESOME));
		wordFreqListButton.setTooltip(new Tooltip("Word frequency list on/off"));
		wordFreqListButton.setOnAction(actionEvent -> toggleRightList());
        final Button helpButton = new Button("", new TextIcon("circle-question", TextIcon.IconSet.AWESOME));
		helpButton.setOnAction(actionEvent -> infoPopup.showPopup(helpButton, InfoPopup.Pos.BELOW_RIGHT, true));
		commonToolbar.getItems().addAll(new Separator(), wordFreqListButton, helpButton);	
		setTop(commonToolbar);
		
		splitPane.getItems().add(mainPane);
		setCenter(splitPane);
		
		// prepare word frequency pane
		final ToolBar wordFreqToolBar = new ToolBar();
		final Button refreshButton = new Button("", new TextIcon("repeat", TextIcon.IconSet.AWESOME));
		refreshButton.setTooltip(new Tooltip("Refresh"));
		refreshButton.setOnAction(actionEvent -> updateListerTableChoice());
		listerTableChoice.setTooltip(new Tooltip("Lister table selector"));
		listerTableChoice.setOnAction(actionEvent -> updateWordFreqList());
		wordFreqToolBar.getItems().addAll(refreshButton, listerTableChoice);
		VBox.setVgrow(wordFreqListView, Priority.ALWAYS);
		wordFreqPane.getChildren().addAll(wordFreqToolBar, wordFreqListView);

		// do some initilization
		declOutputCellFactory = col -> {
			TableCell<DeclensionOutput, String> cell = new TableCell<DeclensionOutput, String>() {
				@Override
				public void updateItem(String item, boolean empty) {
					super.updateItem(item, empty);
					this.setText(null);
					this.setGraphic(null);
					if (!empty) {
						this.setText(null);
						Text text = new Text(item);
						text.getStyleClass().add("shape");                      
						text.wrappingWidthProperty().bind(getTableColumn().widthProperty().subtract(5));
						this.setGraphic(text);
					}
				}
			};
			return cell;
		};
		// load the list of declinable nouns/adj.
		GrammarUtilities.loadDeclinables();
		// load declension paradigms
		if (GrammarUtilities.declension == null)
			GrammarUtilities.declension = new PaliDeclension();
		// prepare info popup
		infoPopup.setContentWithText(GrammarUtilities.getTextResource("info-declension.txt"));
		infoPopup.setTextWidth(Utilities.getRelativeSize(34));
		// other inits
		table.setSelectionModel(null);
		toolbarMap.put(Mode.NOUN, nounToolBox);
		toolbarMap.put(Mode.PRONOUN, pronToolBox);
		toolbarMap.put(Mode.NUMBER, numbToolBox);
		genderToolBox.setPadding(new Insets(3, 3, 3, 3));
		genderToolBox.setSpacing(3);
		init(Mode.NOUN, args);
	}
	
	public final void init(final Mode mode, final Object[] args) {
		nounTextField.clear();
		numbTextField.clear();
		wordList.clear();
		outputList.clear();
		wordFreqList.clear();
		computeButton.setDisable(true);
		cbShowMeaning.setSelected(false);
		showNumValueButton.setSelected(false);
		cardinalButton.setSelected(true);
		currNumberMap = GrammarUtilities.paliNumerals;
		toggleModeGroup.selectToggle(toggleMap.get(mode));
		updateToolBar(mode);
		table.setItems(null);
		mainPane.setLeft(null);
		mainPane.setCenter(null);
		if (splitPane.getItems().size() > 1)
			splitPane.getItems().remove(wordFreqPane);
		setPrefWidth(Utilities.getRelativeSize(53));
		setPrefHeight(Utilities.getRelativeSize(30));
		if (args != null) {
			final String starterTerm = (String)args[0];
			if (!starterTerm.isEmpty())
				nounTextField.setText(starterTerm);
		}
		Platform.runLater(() -> updateListerTableChoice());
	}
	
	private void updateToolBar(final Mode mode) {
		currToolBox = toolbarMap.get(mode);
		final AnchorPane toolPane = new AnchorPane();
		AnchorPane.setBottomAnchor(currToolBox, 0.0);
		AnchorPane.setLeftAnchor(currToolBox, 0.0);
		updateGenderToolBox(null, 0);
		AnchorPane.setBottomAnchor(genderToolBox, 0.0);
		AnchorPane.setRightAnchor(genderToolBox, 0.0);
		toolPane.getChildren().addAll(currToolBox, genderToolBox);
		mainPane.setTop(toolPane);
	}
	
	private void updateGenderToolBox(final PaliWord pword, final int index) {
		genderToolBox.getChildren().clear();
		if (pword != null) {
			final ToggleGroup tg = new ToggleGroup();
			final List<PaliWord.Gender> glist = pword.getGender();
			for (int i=0; i<glist.size(); i++) {
				final PaliWord.Gender g = glist.get(i);
				final RadioButton rb = new RadioButton(g.getAbbr());
				rb.setUserData(i);
				rb.setToggleGroup(tg);
				rb.setSelected(i == index);
				genderToolBox.getChildren().add(rb);
			}
			tg.selectedToggleProperty().addListener((observable) -> {
				final int ind = (Integer)tg.getSelectedToggle().getUserData();
				showDeclensionTable(ind);
			});		
		}
	}

	private void updateListerTableChoice() {
		listerTableChoice.getItems().clear();
		if (GrammarUtilities.luceneServiceImp != null) {
			final List<String> lstTabList = GrammarUtilities.luceneServiceImp.getListerTableNameList();
			if (!lstTabList.isEmpty()) {
				listerTableChoice.getItems().addAll(lstTabList);
				listerTableChoice.getSelectionModel().select(0);
			}
		}
	}

	private void showNounList(final String word) {
		wordList.clear();
		// if the last character is a word ending, enable submit
		computeButton.setDisable(Utilities.PALI_NOUN_ENDINGS.indexOf(word.charAt(word.length()-1)) < 0
								&& !word.endsWith("ant") && !word.endsWith("ar"));
		// reading from db directly is slow, so use prebuilt list instead
		final Set<String> results = GrammarUtilities.declinables.stream().filter(x -> x.startsWith(word)).collect(Collectors.toSet());
		if (!results.isEmpty()) {
			final List<String> resList = results.stream().filter(x->!x.endsWith("ṃ")).collect(Collectors.toList());
			resList.sort(Utilities.paliComparator);
			wordList.setAll(resList);
			showFirstResult();
		}
		mainPane.setLeft(wordListView);
	}
	
	private void showPronounList() {
		GrammarUtilities.loadPronounList();
		wordList.clear();
		if (cbShowMeaning.isSelected()) {
			for (final PaliWord pw : GrammarUtilities.paliPronouns.values()) {
				final String item = pw.getTerm() + " (" + pw.getMeaning().get(0) + ")";
				wordList.add(item);
			}
		} else {
			wordList.setAll(GrammarUtilities.paliPronouns.keySet());
		}
		mainPane.setLeft(wordListView);
		showFirstResult();
	}	
	
	private void showNumberList() {
		GrammarUtilities.loadNumeralList();
		wordList.clear();
		if (showNumValueButton.isSelected()) {
			for (PaliWord pw : currNumberMap.values()) {
				final int val = pw.getNumericValue();
				final int exp = pw.getExpValue();
				final String valStr;
				if (exp == 0)
					valStr = "" + val;
				else
					valStr = val + "^" + exp;
				wordList.add(pw.getTerm() + " (" + valStr + ")");
			}
		} else {
			wordList.setAll(currNumberMap.keySet());
		}
		mainPane.setLeft(wordListView);
		showFirstResult();
	}

	private void numModeSelected() {
		if (numbTextField.getText().length() > 0) {
			computeNumber();
		} else {
			if (cardinalButton.isSelected())
				currNumberMap = GrammarUtilities.paliNumerals;
			else
				currNumberMap = GrammarUtilities.paliOrdinals;
			showNumberList();
		}
	}

	private void showFirstResult() {
		wordListView.getSelectionModel().selectFirst();
		showResult(wordList.get(0));
	}
	
	private void showResult() {
		final String selected = wordListView.getSelectionModel().getSelectedItem();
		if (selected != null)
			showResult(selected);
	}

	private void showResult(final String item) {
		String term = item;
		final Mode mode = (Mode)currMode.get().getUserData();
		if ((mode == Mode.PRONOUN && cbShowMeaning.isSelected()) || 
			(mode == Mode.NUMBER && showNumValueButton.isSelected())) {
			term = item.substring(0, item.indexOf(" ("));
		}		
		if (currMode.get().equals(toggleMap.get(Mode.NOUN))) {
			// noun/adj declension
			if (DictUtilities.dictAvailMap.get(DictUtilities.DictBook.CPED).get()) {
				showDeclensionTable(DictUtilities.lookUpCPEDFromDB(term), 0);
			}
		} else if (currMode.get().equals(toggleMap.get(Mode.PRONOUN))) {
			// pronoun declension
			showDeclensionTable(GrammarUtilities.paliPronouns.get(term), 0);
		} else if (currMode.get().equals(toggleMap.get(Mode.NUMBER))) {
			// numeral declension
			showDeclensionTable(currNumberMap.get(term), 0);
		}
	}
	
	private void computeNoun() {
		final String word = Normalizer.normalize(nounTextField.getText().toLowerCase().trim(), Form.NFC);
		if (word.length() == 0)
			return;
		currWord = new PaliWord(word);
		if (word.endsWith("ant")) {
			currWord.setParadigm("guṇavant,himavant,antcommon");
			currWord.addPosInfo("adj.");
		} else if (word.endsWith("ar")) {
			currWord.addParadigm("kattu");
			currWord.addPosInfo("m.");
		} else {
			currWord.addParadigm("generic");
			currWord.addPosInfo("n.");
		}
		currWord.addMeaning("");
		currWord.addForCompounds(false);
		showDeclensionTable(0);		
	}
	
	private void computeNumber() {
		final String numStr = cleanNumberString(numbTextField.getText().trim());
		final int num = Integer.parseInt(numStr);
		if (num == 0)
			return;
		final List<String> terms;
		if (cardinalButton.isSelected()) {
			terms = GrammarUtilities.getPaliCardinal(numStr);
		} else {
			// consider for ordinal
			if (num <= 10) {
				terms = GrammarUtilities.paliOrdinalMap.get(num+"e0");
			} else {
				final List<String> tmp = new ArrayList<>();
				for (final String t : GrammarUtilities.getPaliCardinal(numStr)) {
					if (!t.endsWith("ṃ"))
						tmp.add(t + "ma");
				}
				terms = tmp;
			}
		} // end if
		// create PaliWord and show terms
		if (!terms.isEmpty()) {
			calPaliNumMap = new LinkedHashMap<>();
			for (final String t : terms) {
				final PaliWord pword = GrammarUtilities.createNumeralPaliWord(t, num, 0, !cardinalButton.isSelected());
				calPaliNumMap.put(t, pword);
			}
			currWord = calPaliNumMap.get(terms.get(0));
			currNumberMap = calPaliNumMap;
			showNumberList();
		}		
	}
	
	private String cleanNumberString(final String strInput) {
		String strNum = "";
		for (final char ch : strInput.toCharArray()) {
			if (Character.isDigit(ch))
				strNum += ch;
		}
		return strNum.isEmpty() ? "0" : strNum;
	}
	
	private void showDeclensionTable(final int genderIndex) {
		showDeclensionTable(currWord, genderIndex);
	}
	
	private void showDeclensionTable(final PaliWord pword, final int genderIndex) {
		currWord = pword;
		updateGenderToolBox(pword, genderIndex);
		outputList.clear();
		allWords.clear();
		final Map<PaliDeclension.Case, Map<PaliDeclension.Number, List<String>>> termMap = GrammarUtilities.computeDeclension(pword, genderIndex);
		for (PaliDeclension.Case cas : PaliDeclension.Case.values) {
			final Map<PaliDeclension.Number, List<String>> dmap = termMap.get(cas);
			final DeclensionOutput dout = new DeclensionOutput(cas, dmap);
			outputList.add(dout);
			dmap.values().forEach(lst -> allWords.addAll(lst));
		}
		if (!outputList.isEmpty()) {
			setupTable();
			table.setItems(outputList);
			mainPane.setCenter(table);
			updateWordFreqList();
		}
	}

	private void updateWordFreqList() {
		wordFreqList.clear();
		if (GrammarUtilities.luceneServiceImp != null && splitPane.getItems().size() > 1) {
			final String lstName = listerTableChoice.getSelectionModel().getSelectedItem();
			if (lstName != null && !lstName.isEmpty()) {
				wordFreqList.addAll(GrammarUtilities.luceneServiceImp.getTermFreqList(lstName, allWords));
			}
		}
	}

	private void setupTable() {
		final TableColumn<DeclensionOutput, String> caseNumberCol = createDeclTableColumn("", outputList.get(0).caseNumberProperty().getName()); 
		caseNumberCol.setPrefWidth(Utilities.getRelativeSize(1.5));	
		final TableColumn<DeclensionOutput, String> caseNameCol = createDeclTableColumn("Case", outputList.get(0).caseNameProperty().getName());
		caseNameCol.setPrefWidth(Utilities.getRelativeSize(3.5));	
		final TableColumn<DeclensionOutput, String> singularOutputCol = createDeclTableColumn("Singular", outputList.get(0).singularOutputProperty().getName()); 
		singularOutputCol.prefWidthProperty().bind(mainPane.widthProperty().subtract(Utilities.getRelativeSize(18)).divide(2));
		final TableColumn<DeclensionOutput, String> pluralOutputCol = createDeclTableColumn("Plural", outputList.get(0).pluralOutputProperty().getName());
		pluralOutputCol.prefWidthProperty().bind(mainPane.widthProperty().subtract(Utilities.getRelativeSize(18)).divide(2));
		table.getColumns().clear();
		table.getColumns().add(caseNumberCol);
		table.getColumns().add(caseNameCol);
		table.getColumns().add(singularOutputCol);
		table.getColumns().add(pluralOutputCol);	
	}
	
	private TableColumn<DeclensionOutput, String> createDeclTableColumn(final String colName, final String propValue) {
		final TableColumn<DeclensionOutput, String> col = new TableColumn<>(colName);
		col.setCellValueFactory(new PropertyValueFactory<>(propValue));
		col.setCellFactory(declOutputCellFactory);
		col.setSortable(false);
		return col;	
	}

	private void toggleRightList() {
		if (splitPane.getItems().size() == 1) {
			splitPane.getItems().add(wordFreqPane);
			splitPane.setDividerPositions(0.7);
			updateWordFreqList();
		} else {
			splitPane.getItems().remove(wordFreqPane);
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
			final DeclensionOutput decl = table.getItems().get(i);
			final String[] data = new String[colCount];
			data[0] = decl.caseNumberProperty().get();
			data[1] = decl.caseNameProperty().get();
			data[2] = decl.singularOutputProperty().get();
			data[3] = decl.pluralOutputProperty().get();
			result.add(data);
		}
		return result;
	}
	
	private void copyCSV() {
		Utilities.copyCSV(makeCSV());
	}
	
	private void saveCSV() {
		Utilities.saveCSV(makeCSV(), "decl-table.csv");
	}
	
	// inner class
	public static final class DeclensionOutput {
		private StringProperty caseNumber;
		private StringProperty caseName;
		private StringProperty singularOutput;
		private StringProperty pluralOutput;
		
		public DeclensionOutput(final PaliDeclension.Case cas, final Map<PaliDeclension.Number, List<String>> outputMap) {
			caseNumberProperty().set(cas.getNumAbbr());
			caseNameProperty().set(cas.getAbbr());
			List<String> singList = outputMap.get(PaliDeclension.Number.SING);
			if (singList != null && !singList.isEmpty())
				singularOutputProperty().set(singList.stream().collect(Collectors.joining(", ")));
			List<String> pluList = outputMap.get(PaliDeclension.Number.PLU);
			if (pluList != null && !pluList.isEmpty())
				pluralOutputProperty().set(pluList.stream().collect(Collectors.joining(", ")));
		}
		
		public StringProperty caseNumberProperty() {
			if (caseNumber == null)
				caseNumber = new SimpleStringProperty(this, "caseNumber");
			return caseNumber;
		}
		
		public StringProperty caseNameProperty() {
			if (caseName == null)
				caseName = new SimpleStringProperty(this, "caseName");
			return caseName;
		}
		
		public StringProperty singularOutputProperty() {
			if (singularOutput == null)
				singularOutput = new SimpleStringProperty(this, "singularOutput");
			return singularOutput;
		}
		
		public StringProperty pluralOutputProperty() {
			if (pluralOutput == null)
				pluralOutput = new SimpleStringProperty(this, "pluralOutput");
			return pluralOutput;
		}
	} // end inner class
	
}
