/*
 * ConjugationWin.java
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

package paliplatform.grammar;

import paliplatform.base.*;

import java.util.*;
import java.util.function.*;
import java.util.stream.*;
import java.text.Normalizer;
import java.text.Normalizer.Form;
import java.nio.charset.StandardCharsets;

import javafx.application.Platform;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.image.*;
import javafx.scene.layout.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.beans.property.*;
import javafx.geometry.*;

/** 
 * The window showing conjugation paradigms for some typical verbs.
 * This is a singleton.
 * @author J.R. Bhaddacak
 * @version 3.4
 * @since 2.0
 */
public class ConjugationWin extends SingletonWindow {
	public static final ConjugationWin INSTANCE = new ConjugationWin();
	private static final String DEFAULT_ROOT = "paca";
	private static final String DEFAULT_EXP_STEM = "pac";
	private static final String DELIM = ":";
	private static final String LINESEP = System.getProperty("line.separator");
	private final Map<String, PaliRoot> commonRootMap = new LinkedHashMap<>();
	private final ObservableList<String> rootList = FXCollections.<String>observableArrayList();
	private final ListView<String> rootListView = new ListView<>(rootList);
	private final BorderPane mainPane = new BorderPane();
	private final VBox wordFreqPane = new VBox();
	private final VBox mainBox = new VBox();
	private final FlowPane tablePane = new FlowPane();
	private final SimpleObjectProperty<Toggle> verbMode = new SimpleObjectProperty<>();																					
	private final CheckMenuItem attanopadaMenuItem = new CheckMenuItem("Show Attanopada");
	private final CheckMenuItem augmentMenuItem = new CheckMenuItem("Include Augment (a-)");
	private final CheckMenuItem experimentMenuItem = new CheckMenuItem("Experiment with a stem");
	private final ToggleGroup voiceGroup = new ToggleGroup();
	private final ToggleGroup paccOptGroup = new ToggleGroup();
	private final ToggleGroup langGroup = new ToggleGroup();
	private final ToggleGroup genderGroup = new ToggleGroup();
	private final Map<PaliConjugation.Voice, RadioButton> voiceRadiosMap = new EnumMap<>(PaliConjugation.Voice.class);
	private final Map<PaliConjugation.TenseMood, RadioButton> tenseRadiosMap = new EnumMap<>(PaliConjugation.TenseMood.class);
	private final Map<PaliConjugation.DeriPaccaya, RadioButton> deriPaccRadiosMap = new EnumMap<>(PaliConjugation.DeriPaccaya.class);
	private final ToolBar voiceToolBar = new ToolBar();
	private final AnchorPane paccOptionAnchorPane = new AnchorPane();
	private final HBox langBox = new HBox();
	private final HBox genderBox = new HBox();
	private final HBox infoBar = new HBox();
	private final Label infoLabel = new Label();
	private final InfoPopup infoPopup = new InfoPopup();
	private final Map<String, Map<PaliWord.Gender, GridPane>> deriDeclensionGridMap = new LinkedHashMap<>();
	private final ObservableList<String> wordFreqList = FXCollections.<String>observableArrayList();
	private final ListView<String> wordFreqListView = new ListView<>(wordFreqList);
	private final Set<String> allWords = new HashSet<>();
	private final ChoiceBox<String> listerTableChoice = new ChoiceBox<>();
	private final TextField searchTextField;
	private final PaliConjugation conjugation;
	private PaliConjugation.Voice currVoice = PaliConjugation.Voice.ACTI; 
	private PaliConjugation.TenseMood currTense = PaliConjugation.TenseMood.VAT; 
	private PaliConjugation.DeriPaccaya currDeriPacc = PaliConjugation.DeriPaccaya.TA; 
	private PaliWord.Gender currGender = PaliWord.Gender.MAS;
	private boolean isEngTense = true;
	private StringBuilder exportedResult;
	private PaliRoot currRoot;
	
	private ConjugationWin() {
		setTitle("Conjugation Table");
		getIcons().add(new Image(ConjugationWin.class.getResourceAsStream("resources/images/table-cells.png")));
		windowWidth = Utilities.getRelativeSize(65);
		GrammarUtilities.loadRootList();
		loadCommonRoots();
		currRoot = commonRootMap.get(DEFAULT_ROOT);
		
		// add toolbar on the top
		final CommonWorkingToolBar toolBar = new CommonWorkingToolBar(mainBox);
		// config some buttons
		toolBar.saveTextButton.setOnAction(actionEvent -> saveText());		
		toolBar.copyButton.setOnAction(actionEvent -> copyText());		
		// add new components
		final PaliTextInput searchTextInput = new PaliTextInput(PaliTextInput.InputType.FIELD);
		searchTextField = (TextField)searchTextInput.getInput();
		searchTextField.setPromptText("Search for...");
		searchTextField.setPrefWidth(Utilities.getRelativeSize(10));
		searchTextField.textProperty().addListener((obs, oldValue, newValue) -> {
			final String text = Normalizer.normalize(newValue.toLowerCase().trim(), Form.NFC);
			if (experimentMenuItem.isSelected())
				experiment(text);
			else
				updateRootList(text);
		});
		final ToggleGroup modeGroup = new ToggleGroup();
		final RadioButton mainVerbButton = new RadioButton("Main");
		mainVerbButton.setTooltip(new Tooltip("Ākhyāta"));
		mainVerbButton.setToggleGroup(modeGroup);
		final RadioButton deriVerbButton = new RadioButton("Derivation");
		deriVerbButton.setTooltip(new Tooltip("Kita (Primary Derivation)"));
		deriVerbButton.setToggleGroup(modeGroup);
		verbMode.bind(modeGroup.selectedToggleProperty());
		modeGroup.selectToggle(mainVerbButton);
        modeGroup.selectedToggleProperty().addListener((observable) -> {
			updateVoiceToolBar();
			updatePaccOptionToolBar();
			updateMinorOptions();
			showResult();
        });	
		final MenuButton optionsMenu = new MenuButton("", new TextIcon("check-double", TextIcon.IconSet.AWESOME));		
		optionsMenu.setTooltip(new Tooltip("Options"));
		attanopadaMenuItem.disableProperty().bind(deriVerbButton.selectedProperty());
		attanopadaMenuItem.setOnAction(actionEvent -> showResult());
		augmentMenuItem.disableProperty().bind(deriVerbButton.selectedProperty());
		augmentMenuItem.setSelected(true);
		augmentMenuItem.setOnAction(actionEvent -> {
			if (experimentMenuItem.isSelected())
				experiment();
			showResult();
		});
		experimentMenuItem.setOnAction(actionEvent -> {
			if (experimentMenuItem.isSelected()) {
				searchTextField.setPromptText("Enter a stem...");
				mainPane.setLeft(null);
				experiment();
				mainPane.setBottom(null);
			} else {
				searchTextField.setPromptText("Search for...");
				searchTextField.clear();
				updateRootList();
				mainPane.setLeft(rootListView);
				currRoot = commonRootMap.get(DEFAULT_ROOT);
				selectRootList(currRoot.getRoot());
				updateVoiceToolBar();
				updatePaccOptionToolBar();
				showResult();
				mainPane.setBottom(infoBar);
			}
		});
		optionsMenu.getItems().addAll(attanopadaMenuItem, augmentMenuItem, experimentMenuItem);

		final Button wordFreqListButton = new Button("", new TextIcon("list", TextIcon.IconSet.AWESOME));
		wordFreqListButton.setTooltip(new Tooltip("Word frequency list on/off"));
		wordFreqListButton.setOnAction(actionEvent -> toggleRightList());
		// help button
		final Button helpButton = new Button("", new TextIcon("circle-question", TextIcon.IconSet.AWESOME));
		helpButton.setOnAction(actionEvent -> infoPopup.showPopup(helpButton, InfoPopup.Pos.BELOW_RIGHT, true));

		toolBar.getItems().addAll(new Separator(), searchTextField, searchTextInput.getClearButton(), searchTextInput.getMethodButton(),
								mainVerbButton, deriVerbButton, optionsMenu, wordFreqListButton, helpButton);
		mainPane.setTop(toolBar);
		
		// set up content pane
		// add root list on the left
		updateRootList();
		rootListView.setPrefWidth(Utilities.getRelativeSize(9));
		rootListView.setCellFactory((ListView<String> lv) -> {
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
						this.setText(value);
					}
					this.setStyle("-fx-padding: 0px 0px 0px 3px");
				}
			};
		});
		selectRootList(currRoot.getRoot());
		rootListView.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> {
			final String selItem = newValue;
			if (selItem != null) {
				currRoot = commonRootMap.get(selItem.substring(0, selItem.indexOf(" (")));
				currTense = PaliConjugation.TenseMood.VAT;
				updateVoiceToolBar();
				updatePaccOptionToolBar();
				updateMinorOptions();
				showResult();
			}
		});
		mainPane.setLeft(rootListView);

		// add information bar to the bottom
		infoBar.setPadding(new Insets(2, 2, 2, 2));
		infoBar.getChildren().add(infoLabel);
		mainPane.setBottom(infoBar);

		// add main content
		conjugation = new PaliConjugation();
		// add voice selection on the top and paccaya option toolbar below that
		for (final PaliConjugation.Voice voice : PaliConjugation.Voice.values) {
			final RadioButton vButt = new RadioButton(voice.toString());
			vButt.setUserData(voice);
			vButt.setToggleGroup(voiceGroup);
			voiceRadiosMap.put(voice, vButt);
		}
		for (final PaliConjugation.TenseMood tense : PaliConjugation.TenseMood.values) {
			final RadioButton tButt = new RadioButton();
			tButt.setTooltip(new Tooltip(tense.getPaliName() + " (" + tense.getEngName() + ")"));
			tButt.setUserData(tense);
			tButt.setToggleGroup(paccOptGroup);
			tenseRadiosMap.put(tense, tButt);
		}
		for (final PaliConjugation.DeriPaccaya deriPacc : PaliConjugation.DeriPaccaya.values) {
			final RadioButton pButt = new RadioButton(deriPacc.getName());
			pButt.setTooltip(new Tooltip(deriPacc.getName() + " (" + deriPacc.getPos() + ")"));
			pButt.setUserData(deriPacc);
			pButt.setToggleGroup(paccOptGroup);
			deriPaccRadiosMap.put(deriPacc, pButt);
		}
		// tense language selection
		AnchorPane.setTopAnchor(langBox, 0.0);
		AnchorPane.setRightAnchor(langBox, 0.0);
		langBox.setPadding(new Insets(3, 3, 3, 3));
		langBox.setSpacing(3);
		final RadioButton lButtEng = new RadioButton("Eng");
		lButtEng.setSelected(isEngTense);
		lButtEng.setToggleGroup(langGroup);
		final RadioButton lButtPali = new RadioButton("Pāli");
		lButtPali.setSelected(!isEngTense);
		lButtPali.setToggleGroup(langGroup);
		langBox.getChildren().addAll(lButtEng, lButtPali);
		// gender selection
		AnchorPane.setTopAnchor(genderBox, 0.0);
		AnchorPane.setRightAnchor(genderBox, 0.0);
		genderBox.setPadding(new Insets(3, 3, 3, 3));
		genderBox.setSpacing(3);
		for (final PaliWord.Gender gender : PaliWord.Gender.values) {
			final RadioButton gButt = new RadioButton(gender.getAbbr());
			gButt.setTooltip(new Tooltip(gender.getName()));
			gButt.setSelected(gender == PaliWord.Gender.MAS);
			gButt.setUserData(gender);
			gButt.setToggleGroup(genderGroup);
			genderBox.getChildren().add(gButt);
		}
		voiceGroup.selectedToggleProperty().addListener(observable -> {
			currVoice = (PaliConjugation.Voice)voiceGroup.getSelectedToggle().getUserData();
			updatePaccOptionToolBar();
			updateMinorOptions();
			showResult();
		});
		paccOptGroup.selectedToggleProperty().addListener(observable -> {
			if (((RadioButton)verbMode.get()).getText().equals("Main"))
				currTense = (PaliConjugation.TenseMood)paccOptGroup.getSelectedToggle().getUserData();
			else
				currDeriPacc = (PaliConjugation.DeriPaccaya)paccOptGroup.getSelectedToggle().getUserData();
			updateMinorOptions();
			showResult();
		});
		langGroup.selectedToggleProperty().addListener(observable -> {
			isEngTense = langGroup.getSelectedToggle() == lButtEng;
			updatePaccOptionToolBar();
			updateMinorOptions();
		});
		genderGroup.selectedToggleProperty().addListener(observable -> {
			currGender = (PaliWord.Gender)genderGroup.getSelectedToggle().getUserData();
			showDeriDeclension(currGender);
		});
		updateVoiceToolBar();
		updatePaccOptionToolBar();
		updateMinorOptions();
		showResult();

		tablePane.setHgap(2.0);
		tablePane.setVgap(2.0);
		tablePane.setPadding(new Insets(3, 3, 3, 3));
		mainBox.getChildren().addAll(voiceToolBar, paccOptionAnchorPane, tablePane);
		mainPane.setCenter(mainBox);
		final Scene scene = new Scene(mainPane, windowWidth, windowHeight);
		
		setScene(scene);
		
		// prepare word frequency pane
		updateListerTableChoice();
		final ToolBar wordFreqToolBar = new ToolBar();
		final Button refreshButton = new Button("", new TextIcon("repeat", TextIcon.IconSet.AWESOME));
		refreshButton.setTooltip(new Tooltip("Refresh"));
		refreshButton.setOnAction(actionEvent -> updateListerTableChoice());
		listerTableChoice.setTooltip(new Tooltip("Lister table selector"));
		listerTableChoice.setOnAction(actionEvent -> updateWordFreqList());
		wordFreqToolBar.getItems().addAll(refreshButton, listerTableChoice);
		wordFreqListView.setPrefWidth(Utilities.getRelativeSize(10));
		VBox.setVgrow(wordFreqListView, Priority.ALWAYS);
		wordFreqPane.getChildren().addAll(wordFreqToolBar, wordFreqListView);

		// prepare info popup
		infoPopup.setContentWithText(GrammarUtilities.getTextResource("info-conjugation.txt"));
		infoPopup.setTextWidth(Utilities.getRelativeSize(28));
	}

	private void loadCommonRoots() {
		if (!commonRootMap.isEmpty())
			return;
		try (final Scanner in = new Scanner(GrammarUtilities.class.getResourceAsStream(GrammarUtilities.PALI_COMMON_VERB_LIST), StandardCharsets.UTF_8)) {
			while (in.hasNextLine()) {
				final String line = in.nextLine().trim();
				if (line.charAt(0) == '#')
					continue;
				final String[] items = line.split(":");
				final String term = items[1];
				final String group = items[2];
				final PaliRoot root = new PaliRoot(Integer.valueOf(items[0]), term, group);
				// 3: active ākhyāta; 4: passive ākhyāta; 5: kita (nta, māna, anīya, tabba, ta, tvā)
				root.setStemMap(items[3], items[4], items[5]);
				commonRootMap.put(term, root);
			}
		} // end try		
	}

	private void updateRootList() {
		String text = Normalizer.normalize(searchTextField.getText().toLowerCase().trim(), Form.NFC);
		updateRootList(text);
	}

	private void updateRootList(final String filter) {
		rootList.clear();
		final Predicate<PaliRoot> withPrefix = x -> x.getRoot().contains("+"+filter);
		final List<String> sideList = commonRootMap.values().stream()
													.filter(withPrefix.or(x->x.getRoot().startsWith(filter)))
													.sorted((x,y) -> Integer.compare(x.getId(), y.getId()))
													.map(x -> x.getRoot()+" ("+x.getGroup()+")")
													.collect(Collectors.toList());
		rootList.addAll(sideList);
	}

	private void selectRootList(final String str) {
		int selectedInd = 0;
		for (int i=0; i<rootList.size(); i++) {
			if (rootList.get(i).startsWith(str)) {
				selectedInd = i;
				break;
			}
		}
		rootListView.getSelectionModel().select(selectedInd);
		final int jump = selectedInd;
		Platform.runLater(() -> rootListView.scrollTo(jump));
	}

	private void updateVoiceToolBar() {
		final List<RadioButton> voiceRadioList = new ArrayList<>(voiceRadiosMap.size());
		for (final PaliConjugation.Voice voice : PaliConjugation.Voice.values) {
			if (currRoot.hasStems(voice)) {
				voiceRadioList.add(voiceRadiosMap.get(voice));
			}
		}
		final PaliConjugation.Voice selectedVoice;
		if (voiceGroup.getSelectedToggle() == null) {
			selectedVoice = PaliConjugation.Voice.values[0];
		} else {
			final PaliConjugation.Voice selV = (PaliConjugation.Voice)voiceGroup.getSelectedToggle().getUserData();
			selectedVoice = currRoot.hasStems(selV) ? selV : PaliConjugation.Voice.values[0];
		}
		currVoice = selectedVoice;
		voiceToolBar.getItems().clear();
		voiceToolBar.getItems().addAll(voiceRadioList);
		voiceGroup.selectToggle(voiceRadiosMap.get(selectedVoice));
	}

	private void updatePaccOptionToolBar() {
		updatePaccOptionToolBar(currRoot);
	}

	private void updatePaccOptionToolBar(final PaliRoot root) {
		paccOptionAnchorPane.getChildren().clear();
		final HBox paccBox = new HBox();
		AnchorPane.setTopAnchor(paccBox, 0.0);
		AnchorPane.setLeftAnchor(paccBox, 0.0);
		paccBox.setPadding(new Insets(3, 3, 3, 6));
		paccBox.setSpacing(3);
		if (((RadioButton)verbMode.get()).getText().equals("Main")) {
			final List<RadioButton> tenseRadioList = new ArrayList<>(tenseRadiosMap.size());
			for (final PaliConjugation.TenseMood tense : PaliConjugation.TenseMood.values) {
				if (root.hasStems(currVoice, tense)) {
					final RadioButton rb = tenseRadiosMap.get(tense);
					final String rbText = isEngTense ? tense.getAbbr() : tense.getPaliAbbr();
					rb.setText(rbText);
					tenseRadioList.add(rb);
				}
			}
			final PaliConjugation.TenseMood selectedTense;
			final Toggle tgl = paccOptGroup.getSelectedToggle();
			if (tgl == null || tgl.getUserData() instanceof PaliConjugation.DeriPaccaya) {
				selectedTense = PaliConjugation.TenseMood.values[0];
			} else {
				final PaliConjugation.TenseMood selT = (PaliConjugation.TenseMood)paccOptGroup.getSelectedToggle().getUserData();
				selectedTense = root.hasStems(currVoice, selT) ? selT : PaliConjugation.TenseMood.values[0];
			}
			currTense = selectedTense;
			paccBox.getChildren().addAll(tenseRadioList);
			paccOptionAnchorPane.getChildren().addAll(paccBox);
			paccOptGroup.selectToggle(tenseRadiosMap.get(selectedTense));
		} else {
			// add paccaya radios on the left
			final List<RadioButton> deriPaccRadioList = new ArrayList<>(deriPaccRadiosMap.size());
			final List<PaliConjugation.DeriPaccaya> paccList = new ArrayList<>(deriPaccRadiosMap.size());
			for (final PaliConjugation.DeriPaccaya deriPacc : PaliConjugation.DeriPaccaya.values) {
				if (root.hasDeriStems(currVoice, deriPacc)) {
					deriPaccRadioList.add(deriPaccRadiosMap.get(deriPacc));
					paccList.add(deriPacc);
				}
			}
			if (paccList.isEmpty()) return;
			final PaliConjugation.DeriPaccaya selectedDeriPacc;
			final Toggle tgl = paccOptGroup.getSelectedToggle();
			if (tgl == null || tgl.getUserData() instanceof PaliConjugation.TenseMood) {
				selectedDeriPacc = paccList.get(0);
			} else {
				final PaliConjugation.DeriPaccaya selP = (PaliConjugation.DeriPaccaya)tgl.getUserData();
				selectedDeriPacc = root.hasDeriStems(currVoice, selP) ? selP : paccList.get(0);
			}
			currDeriPacc = selectedDeriPacc;
			paccBox.getChildren().addAll(deriPaccRadioList);
			paccOptionAnchorPane.getChildren().addAll(paccBox);
			paccOptGroup.selectToggle(deriPaccRadiosMap.get(selectedDeriPacc));
		}
	}

	private void updateMinorOptions() {
		if (((RadioButton)verbMode.get()).getText().equals("Main")) {
			if (paccOptionAnchorPane.getChildren().contains(genderBox))
				paccOptionAnchorPane.getChildren().remove(genderBox);
			if (!paccOptionAnchorPane.getChildren().contains(langBox))
				paccOptionAnchorPane.getChildren().add(langBox);
		} else {
			if (paccOptionAnchorPane.getChildren().contains(langBox))
				paccOptionAnchorPane.getChildren().remove(langBox);
			if (currDeriPacc != PaliConjugation.DeriPaccaya.TVA && currRoot.hasDeriStems(currVoice, currDeriPacc)) {
				if (!paccOptionAnchorPane.getChildren().contains(genderBox))
					paccOptionAnchorPane.getChildren().add(genderBox);
			} else {
				if (paccOptionAnchorPane.getChildren().contains(genderBox))
					paccOptionAnchorPane.getChildren().remove(genderBox);
			}
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

	private PaliRoot createExpVerb(final String stem) {
		final PaliRoot root = new PaliRoot(0, stem, "I");
		final String augStem = augmentMenuItem.isSelected() ? ",a"+stem+".0" : "";
		final String actStem = stem+".0|"+stem+".0|"+stem+".0|"+stem+".0|"+stem+".0"+augStem+"|"+stem+".0"+augStem+"|"+stem+".0|"+stem+".0"+augStem;
		final String pasStem = stem+"iy;"+stem;
		final String deriStem = "-;-;"+stem+"anīy;"+stem+"it;"+stem+"it;"+stem+"itvā";
		root.setStemMap(actStem, pasStem, deriStem);
		return root;
	}
	
	private void experiment() {
		final String text = Normalizer.normalize(searchTextField.getText().toLowerCase().trim(), Form.NFC);
		experiment(text);
	}

	private void experiment(final String text) {
		final String stem = text.isEmpty() ? DEFAULT_EXP_STEM : text;
		currRoot = createExpVerb(stem);
		updateVoiceToolBar();
		updatePaccOptionToolBar();
		updateMinorOptions();
		showResult();
	}

	private void showResult() {
		tablePane.getChildren().clear();
		exportedResult = new StringBuilder();
		allWords.clear();
		final Map<PaliWord.Gender, Set<String>> allDeriWords = new EnumMap<>(PaliWord.Gender.class);
		if (((RadioButton)verbMode.get()).getText().equals("Main")) {
			final Map<String, String[][][]> conjMap = computeConjugation();
			conjMap.forEach((stem, conjDat) -> {
				boolean doShow = true;
				if (!augmentMenuItem.isSelected()) {
					if ((currTense == PaliConjugation.TenseMood.HIY || 
						currTense == PaliConjugation.TenseMood.AJJ || 
						currTense == PaliConjugation.TenseMood.KAL) && stem.charAt(0)=='a')
						doShow = false;
				}
				if (doShow) {
					final StackPane conjStack = new StackPane();
					conjStack.getStyleClass().add("tablegridbox");
					double boxWid = 24;
					if (attanopadaMenuItem.isSelected()) {
						conjStack.getChildren().add(createConjugationGrid(conjDat, true));
						boxWid = 45;
					} else {
						conjStack.getChildren().add(createConjugationGrid(conjDat, false));
					}
					allWords.addAll(getAllWords(conjDat, attanopadaMenuItem.isSelected()));
					exportedResult.append(createConjExportedResult(conjDat, attanopadaMenuItem.isSelected()));
					conjStack.setMinWidth(Utilities.getRelativeSize(boxWid));
					conjStack.setMaxWidth(Utilities.getRelativeSize(boxWid));
					tablePane.getChildren().add(conjStack);
				}
			});
		} else {
			final Map<PaliConjugation.DeriPaccaya, List<String>> stemMap = currRoot.getDeriStemMap(currVoice);
			if (stemMap == null) return;
			final List<String> stemList = stemMap.get(currDeriPacc);
			if (stemList.isEmpty()) return;
			exportedResult.append(getExportedHeader());
			exportedResult.append(currDeriPacc.getName()).append(" (").append(currDeriPacc.getPos()).append(")");
			exportedResult.append(LINESEP);
			if (currDeriPacc == PaliConjugation.DeriPaccaya.TVA) {
				// for tvā case, no declension to show
				final List<String> tvaList = new ArrayList<>();
				for (final String term : stemList) {
					tvaList.add(term);
					if (term.endsWith("tvā"))
						tvaList.add(term + "na");
				}
				final StackPane tvaPane = new StackPane();
				tvaPane.getStyleClass().add("tablegridbox");
				tvaPane.setMinWidth(Utilities.getRelativeSize(24));
				tvaPane.setMaxWidth(Utilities.getRelativeSize(24));
				final VBox tvaBox = new VBox();
				final Label lblTva = new Label(tvaList.stream().collect(Collectors.joining(", ")));
				allWords.addAll(tvaList);
				exportedResult.append(lblTva.getText()).append(LINESEP);
				lblTva.setWrapText(true);
				final Label lblIndRmk = new Label("\n(Used as indeclinables)");
				exportedResult.append(lblIndRmk.getText());
				tvaBox.getChildren().addAll(lblTva, lblIndRmk);
				tvaPane.getChildren().add(tvaBox);
				tablePane.getChildren().add(tvaPane);
			} else {
				// for the rest, show declension tables
				deriDeclensionGridMap.clear();
				// load declension paradigms first if not yet
				if (GrammarUtilities.declension == null)
					GrammarUtilities.declension = new PaliDeclension();
				// generate three genders for each stem
				final String[] genders = { "m.", "f.", "nt." };
				for (final String stem : stemList) {
					final String[] paradigms;
					final String[] stems;
					if (currDeriPacc == PaliConjugation.DeriPaccaya.NTA) {
						// for NTA
						stems = new String[] { stem+"a", stem+"ī", stem+"a" };
						if (stem.equals("gacchant") || stem.equals("sant"))
							paradigms = new String[] { "gacchanta", "generic", "gacchanta" };
						else
							paradigms = new String[] { "ntgen", "ntgen", "ntgen" };
					} else {
						// for the rest
						stems = new String[] { stem+"a", stem+"ā", stem+"a" };
						paradigms = new String[] { "generic", "generic", "generic" };
					}
					final Map<PaliWord.Gender, GridPane> genMap = new EnumMap<>(PaliWord.Gender.class);
					for (int i=0; i<stems.length; i++) {
						// loop for three genders
						final PaliWord word = new PaliWord(stems[i]);
						word.addPosInfo(genders[i]);
						word.addParadigm(paradigms[i]);
						final Map<PaliDeclension.Case, Map<PaliDeclension.Number, List<String>>> decWordMap = GrammarUtilities.computeDeclension(word, 0);
						final PaliWord.Gender gend = PaliWord.Gender.from(genders[i].charAt(0));
						genMap.put(gend, GrammarUtilities.createDeclensionGrid(decWordMap));
						final Set<String> allw = new HashSet<>(getAllDeclWords(decWordMap));
						allDeriWords.put(gend, allw);
						exportedResult.append(gend.getName()).append(LINESEP);
						exportedResult.append(createDeclExportedResult(decWordMap));
					} // end for
					deriDeclensionGridMap.put(stem, genMap);
				} // end for
				showDeriDeclension(currGender);
				allWords.addAll(allDeriWords.get(currGender));
			}
		}
		updateWordFreqList();
		if (!experimentMenuItem.isSelected()) {
			final Integer id = currRoot.getId();
			final PaliRoot theRoot = GrammarUtilities.paliRoots.get(id);
			infoLabel.setText(currRoot.getRoot() + " (" + theRoot.getGroup().toString() + ")" + " - " + theRoot.getEngMeaning());
		}
	}

	private void updateWordFreqList() {
		wordFreqList.clear();
		if (GrammarUtilities.luceneServiceImp != null && mainPane.getRight() != null) {
			final String lstName = listerTableChoice.getSelectionModel().getSelectedItem();
			if (lstName != null && !lstName.isEmpty()) {
				wordFreqList.addAll(GrammarUtilities.luceneServiceImp.getTermFreqList(lstName, allWords));
			}
		}
	}

	private String getExportedHeader() {
		final StringBuilder result = new StringBuilder();
		result.append(infoLabel.getText()).append(LINESEP);
		result.append(currVoice.getName()).append(LINESEP);
		return result.toString();
	}

	private Set<String> getAllWords(final String[][][] data, final boolean withAttanopada) {
		final Set<String> result = new HashSet<>();
		for (final String[] n : data[0])
			for (final String t : n)
				for (final String s : t.split(","))
					result.add(s.trim());
		if (withAttanopada)
			for (final String[] n : data[1])
				for (final String t : n)
					for (final String s : t.split(","))
						result.add(s.trim());
		return result;
	}

	private String createConjExportedResult(final String[][][] data, final boolean withAttanopada) {
		final StringBuilder result = new StringBuilder();
		result.append(getExportedHeader());
		result.append(currTense.getPaliName()).append(" (").append(currTense.getEngName()).append(")");
		result.append(LINESEP);
		if (withAttanopada) {
			result.append(DELIM).append(PaliConjugation.Pada.PARASSA.getName()).append(DELIM);
			result.append(DELIM).append(PaliConjugation.Pada.ATTANO.getName());
			result.append(LINESEP);
			result.append(DELIM).append(PaliConjugation.Number.SING.getName());
			result.append(DELIM).append(PaliConjugation.Number.PLU.getName());
		}
		result.append(DELIM).append(PaliConjugation.Number.SING.getName());
		result.append(DELIM).append(PaliConjugation.Number.PLU.getName());
		result.append(LINESEP);
		result.append(PaliConjugation.Person.PATHAMA.getAbbr());
		result.append(DELIM).append(data[0][0][0]);
		result.append(DELIM).append(data[0][0][1]);
		if (withAttanopada)
			result.append(DELIM).append(data[1][0][0]).append(DELIM).append(data[1][0][1]);
		result.append(LINESEP);
		result.append(PaliConjugation.Person.MAJJHIMA.getAbbr());
		result.append(DELIM).append(data[0][1][0]);
		result.append(DELIM).append(data[0][1][1]);
		if (withAttanopada)
			result.append(DELIM).append(data[1][1][0]).append(DELIM).append(data[1][1][1]);
		result.append(LINESEP);
		result.append(PaliConjugation.Person.UTTAMA.getAbbr());
		result.append(DELIM).append(data[0][2][0]);
		result.append(DELIM).append(data[0][2][1]);
		if (withAttanopada)
			result.append(DELIM).append(data[1][2][0]).append(DELIM).append(data[1][2][1]);
		result.append(LINESEP);
		return result.toString();
	}

	private Set<String> getAllDeclWords(final Map<PaliDeclension.Case, Map<PaliDeclension.Number, List<String>>> decWordMap) {
		final Set<String> result = new HashSet<>();
		decWordMap.forEach((cas, numMap) -> {
			numMap.forEach((num, lst) -> result.addAll(lst));
		});
		return result;
	}

	private String createDeclExportedResult(final Map<PaliDeclension.Case, Map<PaliDeclension.Number, List<String>>> decWordMap) {
		final StringBuilder result = new StringBuilder();
		result.append(DELIM + "Case");
		result.append(DELIM).append(PaliConjugation.Number.SING.getName());
		result.append(DELIM).append(PaliConjugation.Number.PLU.getName());
		result.append(LINESEP);
		decWordMap.forEach((cas, numMap) -> {
			result.append(cas.getNumAbbr()).append(DELIM).append(cas.getAbbr());
			result.append(DELIM).append(numMap.get(PaliDeclension.Number.SING).stream().collect(Collectors.joining(", ")));
			result.append(DELIM).append(numMap.get(PaliDeclension.Number.PLU).stream().collect(Collectors.joining(", ")));
			result.append(LINESEP);
		});
		return result.toString();
	}

	private void showDeriDeclension(final PaliWord.Gender gender) {
		tablePane.getChildren().clear();
		deriDeclensionGridMap.forEach((stem, gmap) -> {
			final StackPane decStack = new StackPane();
			decStack.getStyleClass().add("tablegridbox");
			decStack.setMinWidth(Utilities.getRelativeSize(26));
			decStack.setMaxWidth(Utilities.getRelativeSize(26));
			decStack.getChildren().add(gmap.get(gender));
			tablePane.getChildren().add(decStack);
		});
	}

	private Map<String, String[][][]> computeConjugation() {
		final Map<String, String[][][]> result = new LinkedHashMap<>();
		final Map<String, Set<String>> paradMap = currRoot.getParadigmMap(currVoice);
		final Map<PaliConjugation.TenseMood, List<String>> stemMap = currRoot.getStemMap(currVoice);
		if (stemMap == null)
			return result;
		final List<String> stemList = stemMap.get(currTense);
		int padInd, perInd, numInd;
		for (final String stem : stemList) {
			final List<String> paradList = new ArrayList<>(paradMap.get(stem));
			final String[][][] dat =  new String[2][3][2];
			padInd = 0;
			for (final PaliConjugation.Pada pada : PaliConjugation.Pada.values) {
				final List<VerbParadigm> vp = new ArrayList<>(paradList.size());
				for (final String p : paradList) {
					final VerbParadigm vpd = conjugation.getVerbParadigm(p, currTense, pada);
					if (vpd != null)
						vp.add(vpd);
				}
				perInd = 0;
				for (final PaliConjugation.Person person : PaliConjugation.Person.values) {
					numInd = 0;
					for (final PaliConjugation.Number number : PaliConjugation.Number.values) {
						final Set<String> endingSet = new LinkedHashSet<>();
						for (final VerbParadigm v : vp)
							endingSet.addAll(v.getEndings(person, number));
						final List<String> endings = new ArrayList<>(endingSet);
						final List<String> wds = new ArrayList<>();
						if (!endings.isEmpty()) {
							for (int ind=0; ind<endings.size(); ind++)
								wds.addAll(currRoot.withSuffix(stem, endings.get(ind), 3));
						}
						final String words = wds.stream().collect(Collectors.joining(", "));
						dat[padInd][perInd][numInd] = words;
						numInd++;
					} // end for
					perInd++;
				} // end for
				padInd++;
			} // end for
			result.put(stem, dat);
		} // end for
		return result;
	}

	public GridPane createConjugationGrid(final String[][][] data, final boolean withAttanopada) {
		final GridPane resultGrid = new GridPane();
		resultGrid.setHgap(4);
		resultGrid.setVgap(2);
		resultGrid.setPadding(new Insets(2, 2, 2, 2));
		final int rowStart = withAttanopada ? 1 : 0;
		final Label lblSingHead = new Label(PaliDeclension.Number.SING.getName());
		lblSingHead.setStyle("-fx-font-weight:bold");
		final Label lblPluHead = new Label(PaliDeclension.Number.PLU.getName());
		lblPluHead.setStyle("-fx-font-weight:bold");
		GridPane.setConstraints(lblSingHead, 1, rowStart);
		GridPane.setConstraints(lblPluHead, 2, rowStart);
		resultGrid.getChildren().addAll(lblSingHead, lblPluHead);
		final Label lbl3rd = createConjugCell(PaliConjugation.Person.PATHAMA.getInitial(), 2.0, false);
		lbl3rd.setTooltip(new Tooltip(PaliConjugation.Person.PATHAMA.getName()));
		final Label lbl2nd = createConjugCell(PaliConjugation.Person.MAJJHIMA.getInitial(), 2.0, false);
		lbl2nd.setTooltip(new Tooltip(PaliConjugation.Person.MAJJHIMA.getName()));
		final Label lbl1st = createConjugCell(PaliConjugation.Person.UTTAMA.getInitial(), 2.0, false);
		lbl1st.setTooltip(new Tooltip(PaliConjugation.Person.UTTAMA.getName()));
		GridPane.setConstraints(lbl3rd, 0, rowStart+1, 1, 1, HPos.LEFT, VPos.TOP);
		GridPane.setConstraints(lbl2nd, 0, rowStart+2, 1, 1, HPos.LEFT, VPos.TOP);
		GridPane.setConstraints(lbl1st, 0, rowStart+3, 1, 1, HPos.LEFT, VPos.TOP);
		resultGrid.getChildren().addAll(lbl3rd, lbl2nd, lbl1st);
		final Label lbl3rdSing = createConjugCell(data[0][0][0], 10, true);
		final Label lbl3rdPlu = createConjugCell(data[0][0][1], 10, true);
		final Label lbl2ndSing = createConjugCell(data[0][1][0], 10, true);
		final Label lbl2ndPlu = createConjugCell(data[0][1][1], 10, true);
		final Label lbl1stSing = createConjugCell(data[0][2][0], 10, true);
		final Label lbl1stPlu = createConjugCell(data[0][2][1], 10, true);
		GridPane.setConstraints(lbl3rdSing, 1, rowStart+1, 1, 1, HPos.LEFT, VPos.TOP);
		GridPane.setConstraints(lbl3rdPlu, 2, rowStart+1, 1, 1, HPos.LEFT, VPos.TOP);
		GridPane.setConstraints(lbl2ndSing, 1, rowStart+2, 1, 1, HPos.LEFT, VPos.TOP);
		GridPane.setConstraints(lbl2ndPlu, 2, rowStart+2, 1, 1, HPos.LEFT, VPos.TOP);
		GridPane.setConstraints(lbl1stSing, 1, rowStart+3, 1, 1, HPos.LEFT, VPos.TOP);
		GridPane.setConstraints(lbl1stPlu, 2, rowStart+3, 1, 1, HPos.LEFT, VPos.TOP);
		resultGrid.getChildren().addAll(lbl3rdSing, lbl3rdPlu, lbl2ndSing, lbl2ndPlu, lbl1stSing, lbl1stPlu);
		if (withAttanopada) {
			final Label lblParHead = new Label("          " + PaliConjugation.Pada.PARASSA.getName());
			lblParHead.setStyle("-fx-font-weight:bold");
			final Label lblAttHead = new Label("          " + PaliConjugation.Pada.ATTANO.getName());
			lblAttHead.setStyle("-fx-font-weight:bold");
			GridPane.setConstraints(lblParHead, 1, 0, 2, 1, HPos.LEFT, VPos.TOP);
			GridPane.setConstraints(lblAttHead, 3, 0, 2, 1, HPos.LEFT, VPos.TOP);
			final Label lblSingHeadA = new Label(PaliDeclension.Number.SING.getName());
			lblSingHeadA.setStyle("-fx-font-weight:bold");
			final Label lblPluHeadA = new Label(PaliDeclension.Number.PLU.getName());
			lblPluHeadA.setStyle("-fx-font-weight:bold");
			GridPane.setConstraints(lblSingHeadA, 3, rowStart);
			GridPane.setConstraints(lblPluHeadA, 4, rowStart);
			resultGrid.getChildren().addAll(lblParHead, lblAttHead, lblSingHeadA, lblPluHeadA);
			final Label lbl3rdSingA = createConjugCell(data[1][0][0], 10, true);
			final Label lbl3rdPluA = createConjugCell(data[1][0][1], 10, true);
			final Label lbl2ndSingA = createConjugCell(data[1][1][0], 10, true);
			final Label lbl2ndPluA = createConjugCell(data[1][1][1], 10, true);
			final Label lbl1stSingA = createConjugCell(data[1][2][0], 10, true);
			final Label lbl1stPluA = createConjugCell(data[1][2][1], 10, true);
			GridPane.setConstraints(lbl3rdSingA, 3, rowStart+1, 1, 1, HPos.LEFT, VPos.TOP);
			GridPane.setConstraints(lbl3rdPluA, 4, rowStart+1, 1, 1, HPos.LEFT, VPos.TOP);
			GridPane.setConstraints(lbl2ndSingA, 3, rowStart+2, 1, 1, HPos.LEFT, VPos.TOP);
			GridPane.setConstraints(lbl2ndPluA, 4, rowStart+2, 1, 1, HPos.LEFT, VPos.TOP);
			GridPane.setConstraints(lbl1stSingA, 3, rowStart+3, 1, 1, HPos.LEFT, VPos.TOP);
			GridPane.setConstraints(lbl1stPluA, 4, rowStart+3, 1, 1, HPos.LEFT, VPos.TOP);
			resultGrid.getChildren().addAll(lbl3rdSingA, lbl3rdPluA, lbl2ndSingA, lbl2ndPluA, lbl1stSingA, lbl1stPluA);
		}
		return resultGrid;
	}

	private Label createConjugCell(final String str, final double width, final boolean isWrapped) {
		final Label lbl = new Label(str);
		lbl.setWrapText(isWrapped);
		lbl.setMinWidth(Utilities.getRelativeSize(width));
		lbl.setMaxWidth(Utilities.getRelativeSize(width));
		return lbl;
	}

	private void toggleRightList() {
		if (mainPane.getRight() == null) {
			mainPane.setRight(wordFreqPane);
			updateWordFreqList();
		} else {
			mainPane.setRight(null);
		}
	}

	private void copyText() {
		Utilities.copyText(exportedResult.toString());
	}
	
	private void saveText() {
		Utilities.saveText(exportedResult.toString(), "conjugation.txt");
	}

}
