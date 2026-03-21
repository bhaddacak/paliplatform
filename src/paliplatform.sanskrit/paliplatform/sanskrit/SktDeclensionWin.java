/*
 * SktDeclensionWin.java
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
import static paliplatform.sanskrit.NominalParadigm.Gender;

import java.util.*;
import java.util.stream.*;
import java.util.function.Predicate;
import java.text.Normalizer;
import java.text.Normalizer.Form;

import javafx.scene.control.*;
import javafx.scene.control.cell.*;
import javafx.scene.layout.*;
import javafx.beans.property.*;
import javafx.scene.input.*;
import javafx.scene.text.Text;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.util.Callback;
import javafx.geometry.*;

/**
 * The Sanskrit declension window.
 * @author J.R. Bhaddacak
 * @version 4.1
 * @since 4.1
 */
public class SktDeclensionWin extends BorderPane {
	private static final String DEF_PRONOUN = "yuṣmad";
	public static enum Mode {
		NOUN("Nouns"), ADJ("Adj."), PRONOUN("Pronouns"), CARDINAL("Cardinals"), ORDINAL("Ordinals");
		private static String[] descriptions = { "Nouns", "Adjectives", "Pronouns and demonstratives",
												"Carninal numbers", "Ordinal numbers" };
		public static final Mode[] values = values();
		public String name;
		private Mode(final String name) {
			this.name = name;
		}
		public String getDescription() {
			return descriptions[this.ordinal()];
		}
	};
	private final ObservableList<String> wordList = FXCollections.<String>observableArrayList();
	private final ListView<String> wordListView = new ListView<>(wordList);
	private final ObservableList<DeclensionOutput> outputList = FXCollections.<DeclensionOutput>observableArrayList();
	private final TableView<DeclensionOutput> table = new TableView<>();	
	private final Callback<TableColumn<DeclensionOutput, String>, TableCell<DeclensionOutput, String>> declOutputCellFactory;
	private final Map<Mode, Toggle> toggleMap = new EnumMap<>(Mode.class);
	private final Map<Mode, HBox> toolbarMap = new EnumMap<>(Mode.class);
	private final BorderPane mainPane = new BorderPane();
	private final CheckMenuItem devaMenuItem = new CheckMenuItem("Devanāgarī");
	private final ToggleGroup toggleModeGroup = new ToggleGroup();
	private final ToggleGroup genderGroup = new ToggleGroup();
	private final PaliTextInput nounTextInput = new PaliTextInput(PaliTextInput.InputType.FIELD);
	private final TextField nounTextField;
	private final PaliTextInput adjTextInput = new PaliTextInput(PaliTextInput.InputType.FIELD);
	private final TextField adjTextField;
	private final ComboBox<String> paradigmChoice = new ComboBox<>();
	private final HBox nounToolBox = new HBox();
	private final HBox adjToolBox = new HBox();
	private final HBox pronToolBox = new HBox();
	private final HBox numbToolBox = new HBox();
	private final HBox genderToolBox = new HBox();
	private final ChoiceBox<String> numberChoice = new ChoiceBox<>();
	private final Label numberAltLabel = new Label();
	private final Map<String, Set<NominalParadigm>> pronounParadMap = new LinkedHashMap<>();
	private final InfoPopup infoPopup = new InfoPopup();
	private HBox currToolBox;

	public SktDeclensionWin(final Object[] args) {
		// prepare toolbar for nouns
		nounTextInput.setSanskritMode(true);
		final String inputMethod = Utilities.getSetting("sanskrit-input-method");
		nounTextInput.setInputMethod(PaliTextInput.InputMethod.valueOf(inputMethod));
		nounTextField = (TextField)nounTextInput.getInput();
		nounTextField.setPromptText("Filter by...");
		nounTextField.textProperty().addListener((obs, oldValue, newValue) -> {
			final String strQuery = Normalizer.normalize(newValue.trim(), Form.NFC);
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
					if (key == KeyCode.ESCAPE) {
						nounTextField.clear();
					}
				}
			}
		});	
		final Button namClearButton = nounTextInput.getClearButton();
		namClearButton.setOnAction(actionEvent -> nounTextField.clear());
		final List<String> nounParad = SktDeclension.paradigmMap.keySet().stream()
										.takeWhile(x -> !x.equals("ekaḥ"))
										.collect(Collectors.toList());
		paradigmChoice.setTooltip(new Tooltip("Paradigm selector"));
		paradigmChoice.setOnAction(actionEvent -> showNounList());
		paradigmChoice.getItems().addAll(nounParad);
		nounToolBox.setPadding(new Insets(3));
		nounToolBox.setSpacing(3);
		nounToolBox.getChildren().addAll(nounTextField, namClearButton, nounTextInput.getMethodButton(), paradigmChoice);

		// prepare toolbar for adj
		adjTextInput.setSanskritMode(true);
		adjTextInput.setInputMethod(PaliTextInput.InputMethod.valueOf(inputMethod));
		adjTextField = (TextField)adjTextInput.getInput();
		adjTextField.setPromptText("Filter by...");
		adjTextField.textProperty().addListener((obs, oldValue, newValue) -> showAdjList());
		adjTextField.setOnKeyPressed(keyEvent -> {
			if (keyEvent.getEventType() == KeyEvent.KEY_PRESSED) {
				final KeyCode key = keyEvent.getCode();
				if (keyEvent.isControlDown()) {
					if (key == KeyCode.SPACE) {
						adjTextInput.rotateInputMethod();
					}					
				} else {
					if (key == KeyCode.ESCAPE) {
						adjTextField.clear();
					}
				}
			}
		});	
		final Button adjClearButton = adjTextInput.getClearButton();
		adjClearButton.setOnAction(actionEvent -> adjTextField.clear());
		adjToolBox.setPadding(new Insets(3));
		adjToolBox.setSpacing(3);
		adjToolBox.getChildren().addAll(adjTextField, adjClearButton, adjTextInput.getMethodButton());

		// prepare toolbar for numbers
		numberChoice.setOnAction(actionEvent -> showNumberResult());
		numbToolBox.setPadding(new Insets(3));
		numbToolBox.setSpacing(3);
		numbToolBox.setAlignment(Pos.CENTER_LEFT);
		numbToolBox.getChildren().addAll(numberChoice, numberAltLabel);
		
		// add word list on the left
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
			final RadioButton radio = new RadioButton(m.name);
			radio.setTooltip(new Tooltip(m.getDescription()));
			radio.setToggleGroup(toggleModeGroup);
			radio.setUserData(m);
			toggleMap.put(m, radio);
			commonToolbar.getItems().add(radio);
		}
        toggleModeGroup.selectedToggleProperty().addListener((observable) -> {
			final Mode mode = (Mode)toggleModeGroup.getSelectedToggle().getUserData();
			init(mode, null);
        });
		final MenuButton optionsMenu = new MenuButton("", new TextIcon("check-double", TextIcon.IconSet.AWESOME));		
		optionsMenu.setTooltip(new Tooltip("Options"));
		devaMenuItem.setOnAction(actionEvent -> showResult());
		optionsMenu.getItems().addAll(devaMenuItem);
        final Button helpButton = new Button("", new TextIcon("circle-question", TextIcon.IconSet.AWESOME));
		helpButton.setOnAction(actionEvent -> infoPopup.showPopup(helpButton, InfoPopup.Pos.BELOW_RIGHT, true));
		commonToolbar.getItems().addAll(new Separator(), optionsMenu, helpButton);	
		setTop(commonToolbar);

		mainPane.setLeft(wordListView);
		setCenter(mainPane);

		// do some initilization
		declOutputCellFactory = col -> {
			TableCell<DeclensionOutput, String> cell = new TableCell<DeclensionOutput, String>() {
				@Override
				public void updateItem(String item, boolean empty) {
					super.updateItem(item, empty);
					this.setText(null);
					this.setGraphic(null);
					if (!empty) {
						final Text text = new Text(item);
						text.getStyleClass().add("shape");                      
						text.wrappingWidthProperty().bind(getTableColumn().widthProperty().subtract(5));
						this.setGraphic(text);
					}
				}
			};
			return cell;
		};
		// load the list of declinable nouns, numbers
		SanskritUtilities.loadNounList();
		SanskritUtilities.loadAdjList();
		SanskritUtilities.loadNumberList();
		// prepare info popup
		infoPopup.setContentWithText(SanskritUtilities.getTextResource("info-sktdeclension.txt"));
		infoPopup.setTextWidth(Utilities.getRelativeSize(32));
		// other inits
		table.setSelectionModel(null);
		toolbarMap.put(Mode.NOUN, nounToolBox);
		toolbarMap.put(Mode.ADJ, adjToolBox);
		toolbarMap.put(Mode.PRONOUN, pronToolBox);
		toolbarMap.put(Mode.CARDINAL, numbToolBox);
		toolbarMap.put(Mode.ORDINAL, numbToolBox);
		genderToolBox.setPadding(new Insets(3, 3, 3, 3));
		genderToolBox.setSpacing(3);
		init(Mode.NOUN, args);
	}

	public final void init(final Mode mode, final Object[] args) {
		nounTextField.clear();
		wordList.clear();
		outputList.clear();
		toggleModeGroup.selectToggle(toggleMap.get(mode));
		updateToolBar(mode);
		table.setItems(null);
		mainPane.setCenter(null);
		setPrefWidth(Utilities.getRelativeSize(63));
		setPrefHeight(Utilities.getRelativeSize(30));
		if (args != null) {
			final String paradName = (String)args[0];
			if (paradName.isEmpty())
				paradigmChoice.getSelectionModel().selectFirst();
			else
				paradigmChoice.getSelectionModel().select(paradName);
			showNounList();
		} else {
			wordListView.getSelectionModel().clearSelection();
			if (mode == Mode.PRONOUN) {
				showPronounList();
			} else if (mode == Mode.CARDINAL || mode == Mode.ORDINAL) {
				showNumberList();
			} else if (mode == Mode.ADJ) {
				showAdjList();
			} else {
				paradigmChoice.getSelectionModel().selectFirst();
				showNounList();
			}
			wordListView.scrollTo(0);
		}
	}
	
	private void updateToolBar(final Mode mode) {
		currToolBox = toolbarMap.get(mode);
		final AnchorPane toolPane = new AnchorPane();
		AnchorPane.setBottomAnchor(currToolBox, 0.0);
		AnchorPane.setLeftAnchor(currToolBox, 0.0);
		updateGenderToolBox(null);
		AnchorPane.setBottomAnchor(genderToolBox, 0.0);
		AnchorPane.setRightAnchor(genderToolBox, 0.0);
		toolPane.getChildren().addAll(currToolBox, genderToolBox);
		mainPane.setTop(toolPane);
	}
	
	private void updateGenderToolBox(final List<Gender> glist) {
		// for nouns, numerals
		genderGroup.getToggles().clear();
		genderToolBox.getChildren().clear();
		final String gendStr = glist == null || glist.isEmpty() ? ""
			: "[" + glist.stream().map(x -> x.getShortName()).collect(Collectors.joining("/")) + "]";
		final Label genderLabel = new Label();
		genderLabel.setText(gendStr);
		genderToolBox.getChildren().add(genderLabel);
	}

	private void updateGenderToolBox() {
		// for pronouns, some numerals
		genderGroup.getToggles().clear();
		genderToolBox.getChildren().clear();
		for (final Gender g : Gender.values) {
			final RadioButton radio = new RadioButton(g.getShortName());
			radio.setUserData(g);
			radio.setToggleGroup(genderGroup);
			if (g == Gender.MAS) radio.setSelected(true);
			radio.setOnAction(actionEvent -> showResult(g));
			genderToolBox.getChildren().add(radio);
		}
	}

	private void showNounList() {
		final String strQuery = Normalizer.normalize(nounTextField.getText().trim(), Form.NFC);
		showNounList(strQuery.toLowerCase());
	}

	private void showNounList(final String word) {
		wordList.clear();
		final Predicate<String> filterCond = word.isEmpty()
												? x -> true
												: x -> x.startsWith(word);
		final String paradName = paradigmChoice.getSelectionModel().getSelectedItem();
		if (paradName == null) return;
		final String wordgrpStr = SktDeclension.getWordGroupIdentifier(paradName);
		final List<String> nlist = wordgrpStr.isEmpty()
									? Collections.emptyList()
									: SanskritUtilities.sktNouns.stream()
										.filter(x -> x.startsWith(wordgrpStr))
										.map(x -> x.substring(x.lastIndexOf(":") + 1))
										.collect(Collectors.toList());
		if (!nlist.isEmpty()) {
			// found in word list, filter further
			wordList.setAll(nlist.stream().filter(filterCond).collect(Collectors.toList()));
		} else {
			// no word list in the paradigm
			final NominalParadigm parad = SktDeclension.paradigmMap.get(paradName);
			// use example word list, if any
			final List<String> wlist = parad.getWordList();
			if (!wlist.isEmpty()) {
				// word list exists, filter further
				wordList.setAll(wlist.stream().filter(filterCond).collect(Collectors.toList()));
			} else {
				// or else, add the sample term
				wordList.add(parad.getSampleTerm().getTerm());
			}
		}
		showFirstResult();
	}

	private void showAdjList() {
		final String strQuery = Normalizer.normalize(adjTextField.getText().trim(), Form.NFC);
		final List<String> alist = SanskritUtilities.sktAdjectives.stream()
									.map(x -> x.getTerms()[0])
									.filter(x -> x.startsWith(strQuery))
									.collect(Collectors.toList());
		wordList.setAll(alist);
		wordListView.getSelectionModel().selectFirst();
		showFirstResult();
	}

	private void showPronounList() {
		if (pronounParadMap.isEmpty()) {
			// generate pronoun-paradigm map, one-to-many relation
			SktDeclension.paradigmMap.forEach((n, p) -> {
				if (p.getWordType() == NominalParadigm.WordType.PRONOUN) {
					final List<String> wList = p.getWordList();
					if (wList.isEmpty())
						wList.add(p.getSampleTermStr());
					for(final String w : wList) {
						final Set<NominalParadigm> pset = pronounParadMap.getOrDefault(w, new HashSet<>());
						pset.add(p);
						pronounParadMap.put(w, pset);
					}
				}
			});
		}
		wordList.setAll(pronounParadMap.keySet());
		wordListView.getSelectionModel().select(DEF_PRONOUN);
		showResult(DEF_PRONOUN);
	}

	private void showNumberList() {
		final List<String> nlist = SanskritUtilities.sktNumerals.stream()
									.map(x -> x.getNumberStr())
									.collect(Collectors.toList());
		wordList.setAll(nlist);
		wordListView.getSelectionModel().selectFirst();
		showResult("1");
	}
	
	private void showFirstResult() {
		if (!wordList.isEmpty()) {
			wordListView.getSelectionModel().selectFirst();
			showResult(wordList.get(0));
		}
	}
	
	private void showResult() {
		final String selected = wordListView.getSelectionModel().getSelectedItem();
		if (selected != null)
			showResult(selected);
	}

	private void showResult(final String term) {
		final Mode mode = (Mode)toggleModeGroup.getSelectedToggle().getUserData();
		if (mode == Mode.NOUN) {
			final String paradName = paradigmChoice.getSelectionModel().getSelectedItem();
			if (paradName != null) {
				final NominalParadigm parad = SktDeclension.paradigmMap.get(paradName);
				updateGenderToolBox(parad.getGenderList());
				showDeclensionTable(parad, term);
			}
		} else if (mode == Mode.ADJ || mode == Mode.PRONOUN) {
			updateGenderToolBox();
			showResult(term, Gender.MAS);
		} else if (mode == Mode.CARDINAL || mode == Mode.ORDINAL) {
			final SktNominal.Numeral numeral = SanskritUtilities.sktNumerals.stream()
												.filter(x -> term.equals(x.getNumberStr()))
												.findFirst()
												.orElse(null);
			if (numeral == null) return;
			final List<String> termList = mode == Mode.CARDINAL
											? numeral.getCardinal()
											: numeral.getOrdinal();
			numberChoice.getItems().setAll(termList);
			numberChoice.getSelectionModel().selectFirst();
			final int count = numberChoice.getItems().size();
			final String endingS = count > 1 ? "s" : "";
			numberAltLabel.setText("[" + count + " alternative" + endingS + "]");
			final int num = numeral.getNumber();
			if (num <= 4 || mode == Mode.ORDINAL)
				updateGenderToolBox();
			else
				updateGenderToolBox(Collections.emptyList());
			showNumberResult();
		}
	}

	private void showResult(final String term, final Gender gender) {
		final Mode mode = (Mode)toggleModeGroup.getSelectedToggle().getUserData();
		if (mode == Mode.PRONOUN) {
			final Set<NominalParadigm> paradSet = pronounParadMap.get(term);
			final NominalParadigm parad = paradSet.stream()
											.filter(x -> x.hasGender(gender))
											.findFirst()
											.orElse(null);
			if (parad == null) return;
			showDeclensionTable(parad, term);
		} else if (mode == Mode.ADJ) {
			final SktNominal.Adjective adj = SanskritUtilities.sktAdjectives.stream()
												.filter(x -> term.equals(x.getTerms()[0]))
												.findFirst()
												.orElse(null);
			if (adj == null) return;
			final String[] terms = adj.getTerms();
			final String[] paradNames = adj.getParadigmNames();
			final NominalParadigm parad = SktDeclension.paradigmMap.get(paradNames[gender.ordinal()]);
			showDeclensionTable(parad, terms[gender.ordinal()]);
		} else if (mode == Mode.CARDINAL || mode == Mode.ORDINAL) {
			showNumberResult();
		}
	}
	
	private void showResult(final Gender gender) {
		final Mode mode = (Mode)toggleModeGroup.getSelectedToggle().getUserData();
		if (mode == Mode.ADJ || mode == Mode.PRONOUN) {
			final String term = wordListView.getSelectionModel().getSelectedItem();
			if (term != null)
				showResult(term, gender);
		} else if (mode == Mode.CARDINAL || mode == Mode.ORDINAL) {
			showNumberResult();
		}
	}
	
	private void showNumberResult() {
		final Mode mode = (Mode)toggleModeGroup.getSelectedToggle().getUserData();
		final String numStr = wordListView.getSelectionModel().getSelectedItem();
		if (numStr == null) return;
		final int num = Integer.parseInt(numStr);
		final String term = numberChoice.getSelectionModel().getSelectedItem();
		if (term == null) return;
		final String paradName;
		final NominalParadigm parad;
		if (mode == Mode.CARDINAL) {
			if (genderGroup.getToggles().isEmpty()) {
				// greater than 4
				paradName = SktNominal.Numeral.getCardinalParadigmName(num, term);
			} else {
				// 1 to 4
				final Toggle radio = genderGroup.getSelectedToggle();
				if (radio == null) return;
				final Gender gend = (Gender)radio.getUserData();
				if (gend == null) return;
				paradName = SktNominal.Numeral.getCardinalParadigmName(num, term, gend);
			}
			parad = SktDeclension.paradigmMap.get(paradName);
			if (parad == null) return;
			if (num <= 18 || (num == 19 && term.endsWith("a"))) {
				// 1 to -daśa
				showDeclensionTable(parad, term);
			} else if (num == 19 || (num > 19 && num <= 98) || (num == 99 && term.endsWith("i"))) {
				// 19 ti 99 (-ti)
				final Number[] numSelected = { Number.SING };
				showDeclensionTable(parad, term, numSelected);
			} else {
				showDeclensionTable(parad, term);
			}
		} else {
			// ordinal
			final Toggle radio = genderGroup.getSelectedToggle();
			if (radio == null) return;
			final Gender gend = (Gender)radio.getUserData();
			if (gend == null) return;
			paradName = SktNominal.Numeral.getOrdinalParadigmName(num, term, gend);
			parad = SktDeclension.paradigmMap.get(paradName);
			if (parad == null) return;
			showDeclensionTable(parad, term);
		}
	}

	private void showDeclensionTable(final NominalParadigm parad, final String term, final Number... numArr) {
		final Mode mode = (Mode)toggleModeGroup.getSelectedToggle().getUserData();
		outputList.clear();
		// for multiple-word term
		final String[] terms = term.split(" ");
		final SktNominal[] nominals = new SktNominal[terms.length];
		for (int i = 0; i < terms.length - 1; i++) {
			// from the first to the penultimate
			final String paradName;
			final NominalParadigm par;
			if (mode == Mode.CARDINAL) {
				paradName = terms[i].equals("dvi")
							? "dve"
							: terms[i].equals("tri")
								? "trīṇi"
								: "phalam";
				par = SktDeclension.paradigmMap.get(paradName);
			} else {
				par = parad.getName().equals("nadī")
						? SktDeclension.paradigmMap.get("kathā")
						: parad;
			}
			nominals[i] = new SktNominal(terms[i], par);
		}
		// add the last or the only part
		nominals[terms.length - 1] = new SktNominal(terms[terms.length - 1], parad);
		final Map<Case, Map<Number, List<String>>> caseMap = combineDeclension(nominals);
		for (Case cas : Case.values) {
			final Map<Number, List<String>> numMap = caseMap.get(cas);
			final Map<Number, List<String>> numMapSelected;
			if (numArr.length > 0) {
				numMapSelected = new EnumMap<>(Number.class);
				for (final Number n : numArr) {
					numMapSelected.put(n, numMap.get(n));
				}
			} else {
				numMapSelected = numMap;
			}
			final DeclensionOutput dout = new DeclensionOutput(cas, numMapSelected);
			outputList.add(dout);
		}
		if (!outputList.isEmpty()) {
			setupTable();
			table.setItems(outputList);
			mainPane.setCenter(table);
		}
	}

	private Map<Case, Map<Number, List<String>>> combineDeclension(final SktNominal... nominals) {
		final Map<Case, Map<Number, List<String>>> result = SktDeclension.compute(nominals[0]);
		for (int i = 1; i < nominals.length; i++) {
			final Map<Case, Map<Number, List<String>>> caseMap = SktDeclension.compute(nominals[i]);
			for (final Case c : Case.values) {
				final Map<Number, List<String>> resNumMap = result.get(c);
				final Map<Number, List<String>> curNumMap = caseMap.get(c);
				for (final Number n : Number.values) {
					final List<String> resTermList = resNumMap.get(n);
					final List<String> curTermList = curNumMap.get(n);
					final String resFirst = resTermList.isEmpty() ? "" : resTermList.get(0);
					final String curFirst = curTermList.isEmpty() ? "" : curTermList.get(0);
					final Sandhi sandhi = new Sandhi(resFirst, curFirst);
					resNumMap.put(n, List.of(sandhi.getProductRoman()));
				}
			}
		}
		return result;
	}

	private void setupTable() {
		final String caseHead, singHead, dualHead, pluHead;
		if (devaMenuItem.isSelected()) {
			caseHead = "विभक्तिः";
			singHead = Number.SING.getDevaName();
			dualHead = Number.DUAL.getDevaName();
			pluHead = Number.PLU.getDevaName();
		} else {
			caseHead = "Case";
			singHead = Number.SING.getName();
			dualHead = Number.DUAL.getName();
			pluHead = Number.PLU.getName();
		}
		final TableColumn<DeclensionOutput, String> caseNumberCol = createDeclTableColumn("", outputList.get(0).caseNumberProperty().getName()); 
		caseNumberCol.prefWidthProperty().bind(mainPane.widthProperty().subtract(Utilities.getRelativeSize(11)).divide(19));
		final TableColumn<DeclensionOutput, String> caseNameCol = createDeclTableColumn(caseHead, outputList.get(0).caseNameProperty().getName());
		caseNameCol.prefWidthProperty().bind(mainPane.widthProperty().subtract(Utilities.getRelativeSize(11)).divide(19).multiply(2));
		final TableColumn<DeclensionOutput, String> singularOutputCol = createDeclTableColumn(singHead, outputList.get(0).singularOutputProperty().getName()); 
		singularOutputCol.prefWidthProperty().bind(mainPane.widthProperty().subtract(Utilities.getRelativeSize(11)).divide(19).multiply(5));
		final TableColumn<DeclensionOutput, String> dualOutputCol = createDeclTableColumn(dualHead, outputList.get(0).dualOutputProperty().getName()); 
		dualOutputCol.prefWidthProperty().bind(mainPane.widthProperty().subtract(Utilities.getRelativeSize(11)).divide(19).multiply(5));
		final TableColumn<DeclensionOutput, String> pluralOutputCol = createDeclTableColumn(pluHead, outputList.get(0).pluralOutputProperty().getName());
		pluralOutputCol.prefWidthProperty().bind(mainPane.widthProperty().subtract(Utilities.getRelativeSize(11)).divide(19).multiply(5));
		table.getColumns().clear();
		table.getColumns().add(caseNumberCol);
		table.getColumns().add(caseNameCol);
		table.getColumns().add(singularOutputCol);
		table.getColumns().add(dualOutputCol);
		table.getColumns().add(pluralOutputCol);	
	}
	
	private TableColumn<DeclensionOutput, String> createDeclTableColumn(final String colName, final String propValue) {
		final TableColumn<DeclensionOutput, String> col = new TableColumn<>(colName);
		col.setCellValueFactory(new PropertyValueFactory<>(propValue));
		col.setCellFactory(declOutputCellFactory);
		col.setSortable(false);
		return col;	
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
			data[3] = decl.dualOutputProperty().get();
			data[4] = decl.pluralOutputProperty().get();
			result.add(data);
		}
		return result;
	}
	
	private void copyCSV() {
		Utilities.copyCSV(makeCSV());
	}
	
	private void saveCSV() {
		Utilities.saveCSV(makeCSV(), "sktdecl-table.csv");
	}
	
	// inner class
	public final class DeclensionOutput {
		private StringProperty caseNumber;
		private StringProperty caseName;
		private StringProperty singularOutput;
		private StringProperty dualOutput;
		private StringProperty pluralOutput;
		
		public DeclensionOutput(final Case cas, final Map<Number, List<String>> outputMap) {
			final String caseNumStr = devaMenuItem.isSelected() ? cas.getDevaNumAbbr() : cas.getNumAbbr();
			caseNumberProperty().set(caseNumStr);
			final String caseNameStr = devaMenuItem.isSelected() ? cas.getDevaName() : cas.getAbbr();
			caseNameProperty().set(caseNameStr);
			List<String> singList = outputMap.get(Number.SING);
			if (singList != null && !singList.isEmpty()) {
				final String singRoman = singList.stream().collect(Collectors.joining(", "));
				final String singFinal = devaMenuItem.isSelected()
										? ScriptTransliterator.translitQuick(singRoman, ScriptTransliterator.EngineType.ROMAN_SKT_DEVA, false)
										: singRoman;
				singularOutputProperty().set(singFinal);
			}
			List<String> dualList = outputMap.get(Number.DUAL);
			if (dualList != null && !dualList.isEmpty()) {
				final String dualRoman = dualList.stream().collect(Collectors.joining(", "));
				final String dualFinal = devaMenuItem.isSelected()
										? ScriptTransliterator.translitQuick(dualRoman, ScriptTransliterator.EngineType.ROMAN_SKT_DEVA, false)
										: dualRoman;
				dualOutputProperty().set(dualFinal);
			}
			List<String> pluList = outputMap.get(Number.PLU);
			if (pluList != null && !pluList.isEmpty()) {
				final String pluRoman = pluList.stream().collect(Collectors.joining(", "));
				final String pluFinal = devaMenuItem.isSelected()
										? ScriptTransliterator.translitQuick(pluRoman, ScriptTransliterator.EngineType.ROMAN_SKT_DEVA, false)
										: pluRoman;
				pluralOutputProperty().set(pluFinal);
			}
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
		
		public StringProperty dualOutputProperty() {
			if (dualOutput == null)
				dualOutput = new SimpleStringProperty(this, "dualOutput");
			return dualOutput;
		}
		
		public StringProperty pluralOutputProperty() {
			if (pluralOutput == null)
				pluralOutput = new SimpleStringProperty(this, "pluralOutput");
			return pluralOutput;
		}
	}

}

