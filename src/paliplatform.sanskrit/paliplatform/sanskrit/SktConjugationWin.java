/*
 * SktConjugationWin.java
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
import static paliplatform.sanskrit.SktConjugation.TenseMood;
import static paliplatform.sanskrit.SktConjugation.Pada;
import static paliplatform.sanskrit.SktConjugation.Person;
import static paliplatform.sanskrit.SktConjugation.Number;
import static paliplatform.sanskrit.SktDeclension.Case;
import static paliplatform.sanskrit.NominalParadigm.Gender;

import java.util.*;
import java.util.stream.*;
import java.text.Normalizer;
import java.text.Normalizer.Form;

import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.input.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.*;

/**
 * The Sanskrit conjugation window.
 * @author J.R. Bhaddacak
 * @version 4.2
 * @since 4.1
 */
public class SktConjugationWin extends BorderPane {
	static enum VerbListMode { VERB, ROOT, LAME }
	static enum VForm { 
		ACT("Active"), MID("Middle"), PAS("Passive"), CAU("Causative"), CAUPAS("Causative Passive"),
		DES("Desiderative"), DESMID("Desiderative Middle"), DESPAS("Desiderative Passive"), INT("Intensive");
		public static VForm[] values = values();
		private String name;
		private VForm(final String nam) {
			name = nam;
		}
		public String getName() {
			return name;
		}
	}
	static enum DeriVerbForm {
		PRES_ACT_PART, PRES_MID_PART, PRES_PAS_PART,
		PERF_ACT_PART, PERF_MID_PART, PERF_PAS_PART,
		FUT_ACT_PART, FUT_MID_PART, FUT_PAS_PART,
		INF, ABS;
		public static DeriVerbForm[] values = values();
		private static String[] names = {
			"Present active participle", "Present middle participle", "Present passive participle",
			"Perfect active participle", "Perfect middle participle", "Perfect passive participle",
			"Future active participle", "Future middle participle", "Future passive participle",
			"Infinitive", "Absolutive" };
		public static List<String> getNameList() {
			return Arrays.asList(names);
		}
		public String getName() {
			return names[this.ordinal()];
		}
		@Override
		public String toString() {
			return names[this.ordinal()];
		}
	}
	private static final String LINESEP = System.getProperty("line.separator");
	private static final String DELIM = ":";
	private final BorderPane mainPane = new BorderPane();
	private final ScrollPane scrollPane = new ScrollPane();
	private final FlowPane tablePane = new FlowPane();
	private final ChoiceBox<String> verbFormChoice = new ChoiceBox<>();
	private final ToggleButton mainVerbButton = new ToggleButton("", new TextIcon("M", TextIcon.IconSet.AWESOME));
	private final CheckMenuItem devaMenuItem = new CheckMenuItem("Devanāgarī");
	private final ObservableList<String> verbList = FXCollections.<String>observableArrayList();
	private final ListView<String> verbListView = new ListView<>(verbList);
	private final InfoPopup infoPopup = new InfoPopup();
	private final ToggleGroup listTypeGroup = new ToggleGroup();
	private final TextField searchTextField;
	private VerbListMode currVerbListMode = VerbListMode.VERB;
	private SktVerb currVerb = null;
	private StringBuilder exportedResult;

	public SktConjugationWin(final Object[] args) {
		// add common toolbar on the top
		final CommonWorkingToolBar commonToolbar = new CommonWorkingToolBar(tablePane);
		// config some buttons
		commonToolbar.saveTextButton.setOnAction(actionEvent -> saveText());
		commonToolbar.copyButton.setOnAction(actionEvent -> copyText());		
		// add new buttons
		mainVerbButton.setTooltip(new Tooltip("Main/finite verb forms"));
		mainVerbButton.setOnAction(actionEvent -> updateVerbFormList());
		final ToggleButton deriVerbButton = new ToggleButton("", new TextIcon("D", TextIcon.IconSet.AWESOME));
		deriVerbButton.setTooltip(new Tooltip("Derivative verb forms"));
		deriVerbButton.setOnAction(actionEvent -> updateVerbFormList());
		final ToggleGroup verbGroup = new ToggleGroup();
		verbGroup.getToggles().addAll(mainVerbButton, deriVerbButton);
		verbFormChoice.setTooltip(new Tooltip("Verb form selector"));
		verbFormChoice.setOnAction(actionEvent -> showResult());
		final MenuButton optionsMenu = new MenuButton("", new TextIcon("check-double", TextIcon.IconSet.AWESOME));
		optionsMenu.setTooltip(new Tooltip("Options"));
		devaMenuItem.setOnAction(actionEvent -> showResult());
		optionsMenu.getItems().addAll(devaMenuItem);
        final Button helpButton = new Button("", new TextIcon("circle-question", TextIcon.IconSet.AWESOME));
		helpButton.setOnAction(actionEvent -> infoPopup.showPopup(helpButton, InfoPopup.Pos.BELOW_RIGHT, true));
		commonToolbar.getItems().addAll(new Separator(), mainVerbButton, deriVerbButton, verbFormChoice,
										optionsMenu, helpButton);	
		setTop(commonToolbar);

		// add verb list on the left
		final VBox verbListBox = new VBox();
		final ToolBar verbListToolBar = new ToolBar();
        final ToggleButton verbButton = new ToggleButton("", new TextIcon("person-walking", TextIcon.IconSet.AWESOME));
		verbButton.setTooltip(new Tooltip("Show as present verbs"));
		verbButton.setSelected(true);
		verbButton.setOnAction(actionEvent -> updateVerbList(VerbListMode.VERB));
        final ToggleButton rootButton = new ToggleButton("", new TextIcon("seedling", TextIcon.IconSet.AWESOME));
		rootButton.setTooltip(new Tooltip("Show as roots"));
		rootButton.setOnAction(actionEvent -> updateVerbList(VerbListMode.ROOT));
        final ToggleButton lameButton = new ToggleButton("", new TextIcon("wheelchair", TextIcon.IconSet.AWESOME));
		lameButton.setTooltip(new Tooltip("Incomplete roots"));
		lameButton.setOnAction(actionEvent -> updateVerbList(VerbListMode.LAME));
		listTypeGroup.getToggles().addAll(verbButton, rootButton, lameButton);
		verbListToolBar.getItems().addAll(verbButton, rootButton, lameButton);
		final HBox searchBox = new HBox();
		searchBox.setPadding(new Insets(3));
		searchBox.setSpacing(3);
		final PaliTextInput searchInput = new PaliTextInput(PaliTextInput.InputType.FIELD);
		searchTextField = (TextField)searchInput.getInput();
		searchTextField.setPromptText("Search for...");
		searchTextField.setPrefWidth(Utilities.getRelativeSize(10));
		searchTextField.textProperty().addListener((obs, oldValue, newValue) -> updateVerbList(currVerbListMode));
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
		verbListView.setPrefWidth(Utilities.getRelativeSize(13));
		verbListView.setCellFactory((ListView<String> lv) -> {
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
		verbListView.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> {
			final String selItem = newValue;
			if (selItem != null)
				showResult(selItem);
		});
		VBox.setVgrow(verbListView, Priority.ALWAYS);
		verbListBox.getChildren().addAll(verbListToolBar, searchBox, verbListView);
		setLeft(verbListBox);

		tablePane.setHgap(2.0);
		tablePane.setVgap(2.0);
		tablePane.setPadding(new Insets(3, 3, 3, 3));
		scrollPane.setContent(tablePane);
		mainPane.setCenter(scrollPane);
		setCenter(mainPane);

		// prepare info popup
		infoPopup.setContentWithText(SanskritUtilities.getTextResource("info-sktconjugation.txt"));
		infoPopup.setTextWidth(Utilities.getRelativeSize(40));

		init(args);
	}

	public final void init(final Object[] args) {
		setPrefWidth(Utilities.getRelativeSize(60));
		setPrefHeight(Utilities.getRelativeSize(36));
		mainVerbButton.setSelected(true);
		updateVerbFormList();
		currVerb = null;
		currVerbListMode = VerbListMode.VERB;
		verbListView.getSelectionModel().clearSelection();
		verbListView.scrollTo(0);
		listTypeGroup.selectToggle(listTypeGroup.getToggles().get(0));
		updateVerbList(currVerbListMode);
		scrollPane.setVvalue(scrollPane.getVmin());
	}

	private void updateVerbFormList() {
		final List<String> flist = mainVerbButton.isSelected()
									? TenseMood.getNameList()
									: DeriVerbForm.getNameList();
		verbFormChoice.getItems().setAll(flist);
		verbFormChoice.getSelectionModel().select(0);
	}

	private void updateVerbList(final VerbListMode mode) {
		final String strQuery = Normalizer.normalize(searchTextField.getText().trim(), Form.NFC).toLowerCase();
		int selectedInd = verbListView.getSelectionModel().getSelectedIndex();
		selectedInd = selectedInd < 0 ? 0 : selectedInd;
		verbList.clear();
		currVerbListMode = mode;
		if (mode == VerbListMode.VERB) {
			final List<String> rlist = VerbRepo.sktVerbMap.values().stream()
											.filter(x -> x.isCompleted())
											.map(SktVerb::getVerbAndRoot)
											.filter(x -> x.indexOf(strQuery) > -1)
											.collect(Collectors.toList());
			verbList.addAll(rlist);
			verbListView.getSelectionModel().select(selectedInd);
		} else if (mode == VerbListMode.ROOT) {
			final List<String> rlist = VerbRepo.sktVerbMap.values().stream()
											.filter(x -> x.isCompleted())
											.map(SktVerb::getDescription)
											.filter(x -> x.indexOf(strQuery) > -1)
											.collect(Collectors.toList());
			verbList.addAll(rlist);
			verbListView.getSelectionModel().select(selectedInd);
		} else {
			final List<String> rlist = VerbRepo.sktVerbMap.values().stream()
											.filter(x -> !x.isCompleted())
											.map(SktVerb::getFullDescription)
											.filter(x -> x.indexOf(strQuery) > -1)
											.collect(Collectors.toList());
			verbList.addAll(rlist);
			verbListView.getSelectionModel().select(0);
		}
	}

	private void showResult() {
		final String selectedVerb = verbListView.getSelectionModel().getSelectedItem();
		if (selectedVerb == null) return;
		showResult(selectedVerb);
	}

	private void showResult(final String term) {
		exportedResult = new StringBuilder();
		if (currVerbListMode == VerbListMode.VERB) {
			currVerb = VerbRepo.sktVerbMap.get(term);
		} else {
			final int bpos = term.indexOf("[");
			final String num = term.substring(bpos + 1, term.lastIndexOf("]"));
			currVerb = VerbRepo.sktVerbMap.values().stream()
						.filter(x -> num.equals(x.getBucknellNumberStr()))
						.findFirst()
						.orElse(null);
		}
		if (currVerb == null) return;
		exportedResult.append(currVerb.getDescription()).append(LINESEP);
		tablePane.getChildren().clear();
		final int selectedForm = verbFormChoice.getSelectionModel().getSelectedIndex();
		if (selectedForm < 0) return;
		exportedResult.append(verbFormChoice.getSelectionModel().getSelectedItem()).append(LINESEP);
		if (mainVerbButton.isSelected()) {
			final TenseMood tense = TenseMood.values[selectedForm];
			switch (selectedForm) {
				case 0: // PRES
				case 1: // IMP
				case 2: // OPT
				case 3: // IMPERF
					addMainVerbTable(currVerb.getCommonProduct(tense, Pada.ACT), VForm.ACT);
					addMainVerbTable(currVerb.getCommonProduct(tense, Pada.MID), VForm.MID);
					for (int i = 0; i < currVerb.getIrregularMiddleForm().size(); i++) {
						addMainVerbTable(currVerb.getIrrMidCommonProduct(tense, i), VForm.MID);
					}
					for (int i = 0; i < currVerb.getPresentPassiveForm().size(); i++) {
						addMainVerbTable(currVerb.getCommonPassiveProduct(tense, i), VForm.PAS);
					}
					addMainVerbTable(currVerb.getCommonCausativeProduct(tense), VForm.CAU);
					if (selectedForm == 0) {
						addMainVerbTable(currVerb.getCausativePassiveProduct(), VForm.CAUPAS);
						addMainVerbTable(currVerb.getDesiderativeProduct(Pada.ACT), VForm.DES);
						addMainVerbTable(currVerb.getDesiderativeProduct(Pada.MID), VForm.DESMID);
						addMainVerbTable(currVerb.getDesiderativePassiveProduct(), VForm.DESPAS);
						addMainVerbTable(currVerb.getIntensiveProduct(), VForm.INT);
					}
					break;
				case 4: // PERF
					addMainVerbTable(currVerb.getPerfectProduct(Pada.ACT), VForm.ACT);
					addMainVerbTable(currVerb.getPerfectProduct(Pada.MID), VForm.MID);
					addMainVerbTable(currVerb.getPeriPerfectProduct(Pada.ACT), VForm.ACT);
					addMainVerbTable(currVerb.getPeriPerfectProduct(Pada.MID), VForm.MID);
					addMainVerbTable(currVerb.getPerfectCausativeProduct(), VForm.CAU);
					addMainVerbTable(currVerb.getPerfectDesiderativeProduct(Pada.ACT), VForm.DES);
					addMainVerbTable(currVerb.getPerfectDesiderativeProduct(Pada.MID), VForm.DESMID);
					break;
				case 5: // AOR
					for (int i = 0; i < currVerb.getAoristForm().size(); i++) {
						addMainVerbTable(currVerb.getAoristProduct(Pada.ACT, i), VForm.ACT);
						addMainVerbTable(currVerb.getAoristProduct(Pada.MID, i), VForm.MID);
					}
					addMainVerbTable(currVerb.getAoristPassiveProduct(), VForm.PAS);
					addMainVerbTable(currVerb.getAoristCausativeProduct(), VForm.CAU);
					addMainVerbTable(currVerb.getAoristDesiderativeProduct(Pada.ACT), VForm.DES);
					addMainVerbTable(currVerb.getAoristDesiderativeProduct(Pada.MID), VForm.DESMID);
					break;
				case 6: // PREC
					addMainVerbTable(currVerb.getPrecativeProduct(Pada.ACT), VForm.ACT);
					addMainVerbTable(currVerb.getPrecativeProduct(Pada.MID), VForm.MID);
					addMainVerbTable(currVerb.getPrecativePassiveProduct(), VForm.PAS);
					break;
				case 7: // PERI
					addMainVerbTable(currVerb.getPeriFutureProduct(Pada.ACT), VForm.ACT);
					addMainVerbTable(currVerb.getPeriFutureProduct(Pada.MID), VForm.MID);
					addMainVerbTable(currVerb.getPeriFuturePassiveProduct(), VForm.PAS);
					break;
				case 8: // FUT
					for (int i = 0; i < currVerb.getFutureForm().size(); i++) {
						addMainVerbTable(currVerb.getFutureProduct(Pada.ACT, i), VForm.ACT);
						addMainVerbTable(currVerb.getFutureProduct(Pada.MID, i), VForm.MID);
					}
					addMainVerbTable(currVerb.getFuturePassiveProduct(), VForm.PAS);
					addMainVerbTable(currVerb.getFutureCausativeProduct(), VForm.CAU);
					addMainVerbTable(currVerb.getFutureDesiderativeProduct(Pada.ACT), VForm.DES);
					addMainVerbTable(currVerb.getFutureDesiderativeProduct(Pada.MID), VForm.DESMID);
					break;
				case 9: // COND
					for (int i = 0; i < currVerb.getFutureForm().size(); i++) {
						addMainVerbTable(currVerb.getConditionalProduct(Pada.ACT, i), VForm.ACT);
						addMainVerbTable(currVerb.getConditionalProduct(Pada.MID, i), VForm.MID);
					}
					break;
			}
		} else {
			final DeriVerbForm deriForm = DeriVerbForm.values[selectedForm];
			switch (selectedForm) {
				case 0: // PRES_ACT_PART
					addDeriVerbTable(currVerb.getPresentActiveParticipleProduct(Gender.MAS), Gender.MAS);
					addDeriVerbTable(currVerb.getPresentActiveParticipleProduct(Gender.FEM), Gender.FEM);
					addDeriVerbTable(currVerb.getPresentActiveParticipleProduct(Gender.NEU), Gender.NEU);
					break;
				case 1: // PRES_MID_PART
					addDeriVerbTable(currVerb.getPresentMiddleParticipleProduct(Gender.MAS), Gender.MAS);
					addDeriVerbTable(currVerb.getPresentMiddleParticipleProduct(Gender.FEM), Gender.FEM);
					addDeriVerbTable(currVerb.getPresentMiddleParticipleProduct(Gender.NEU), Gender.NEU);
					break;
				case 2: // PRES_PAS_PART
					for (int i = 0; i < currVerb.getPresentPassiveForm().size(); i++) {
						addDeriVerbTable(currVerb.getPresentPassiveParticipleProduct(Gender.MAS, i), Gender.MAS);
						addDeriVerbTable(currVerb.getPresentPassiveParticipleProduct(Gender.FEM, i), Gender.FEM);
						addDeriVerbTable(currVerb.getPresentPassiveParticipleProduct(Gender.NEU, i), Gender.NEU);
					}
					break;
				case 3: // PERF_ACT_PART
					addDeriVerbTable(currVerb.getPerfectActiveParticipleProduct(Gender.MAS), Gender.MAS);
					addDeriVerbTable(currVerb.getPerfectActiveParticipleProduct(Gender.FEM), Gender.FEM);
					addDeriVerbTable(currVerb.getPerfectActiveParticipleProduct(Gender.NEU), Gender.NEU);
					for (int i = 0; i < currVerb.getPppForm().size(); i++) {
						addDeriVerbTable(currVerb.getPerfectActiveParticipleFromTaProduct(Gender.MAS, i), Gender.MAS);
						addDeriVerbTable(currVerb.getPerfectActiveParticipleFromTaProduct(Gender.FEM, i), Gender.FEM);
						addDeriVerbTable(currVerb.getPerfectActiveParticipleFromTaProduct(Gender.NEU, i), Gender.NEU);
					}
					break;
				case 4: // PERF_MID_PART
					addDeriVerbTable(currVerb.getPerfectMiddleParticipleProduct(Gender.MAS), Gender.MAS);
					addDeriVerbTable(currVerb.getPerfectMiddleParticipleProduct(Gender.FEM), Gender.FEM);
					addDeriVerbTable(currVerb.getPerfectMiddleParticipleProduct(Gender.NEU), Gender.NEU);
					break;
				case 5: // PERF_PAS_PART
					for (int i = 0; i < currVerb.getPppForm().size(); i++) {
						addDeriVerbTable(currVerb.getPerfectPassiveParticipleProduct(Gender.MAS, i), Gender.MAS);
						addDeriVerbTable(currVerb.getPerfectPassiveParticipleProduct(Gender.FEM, i), Gender.FEM);
						addDeriVerbTable(currVerb.getPerfectPassiveParticipleProduct(Gender.NEU, i), Gender.NEU);
					}
					addDeriVerbTable(currVerb.getPerfectPassiveParticipleCausativeProduct(Gender.MAS), Gender.MAS, VForm.CAU);
					addDeriVerbTable(currVerb.getPerfectPassiveParticipleCausativeProduct(Gender.FEM), Gender.FEM, VForm.CAU);
					addDeriVerbTable(currVerb.getPerfectPassiveParticipleCausativeProduct(Gender.NEU), Gender.NEU, VForm.CAU);
					addDeriVerbTable(currVerb.getPerfectPassiveParticipleDesiderativeProduct(Gender.MAS), Gender.MAS, VForm.DES);
					addDeriVerbTable(currVerb.getPerfectPassiveParticipleDesiderativeProduct(Gender.FEM), Gender.FEM, VForm.DES);
					addDeriVerbTable(currVerb.getPerfectPassiveParticipleDesiderativeProduct(Gender.NEU), Gender.NEU, VForm.DES);
					break;
				case 6: // FUT_ACT_PART
					for (int i = 0; i < currVerb.getFutureForm().size(); i++) {
						addDeriVerbTable(currVerb.getFutureActiveParticipleProduct(Gender.MAS, i), Gender.MAS);
						addDeriVerbTable(currVerb.getFutureActiveParticipleProduct(Gender.FEM, i), Gender.FEM);
						addDeriVerbTable(currVerb.getFutureActiveParticipleProduct(Gender.NEU, i), Gender.NEU);
					}
					break;
				case 7: // FUT_MID_PART
					for (int i = 0; i < currVerb.getFutureForm().size(); i++) {
						addDeriVerbTable(currVerb.getFutureMiddleParticipleProduct(Gender.MAS, i), Gender.MAS);
						addDeriVerbTable(currVerb.getFutureMiddleParticipleProduct(Gender.FEM, i), Gender.FEM);
						addDeriVerbTable(currVerb.getFutureMiddleParticipleProduct(Gender.NEU, i), Gender.NEU);
					}
					break;
				case 8: // FUT_PAS_PART
					for (int i = 0; i < currVerb.getFppForm().size(); i++) {
						addDeriVerbTable(currVerb.getFuturePassiveParticipleProduct(Gender.MAS, i), Gender.MAS);
						addDeriVerbTable(currVerb.getFuturePassiveParticipleProduct(Gender.FEM, i), Gender.FEM);
						addDeriVerbTable(currVerb.getFuturePassiveParticipleProduct(Gender.NEU, i), Gender.NEU);
					}
					addDeriVerbTable(currVerb.getFuturePassiveParticipleNiyaProduct(Gender.MAS), Gender.MAS);
					addDeriVerbTable(currVerb.getFuturePassiveParticipleNiyaProduct(Gender.FEM), Gender.FEM);
					addDeriVerbTable(currVerb.getFuturePassiveParticipleNiyaProduct(Gender.NEU), Gender.NEU);
					addDeriVerbTable(currVerb.getFuturePassiveParticipleYaProduct(Gender.MAS), Gender.MAS);
					addDeriVerbTable(currVerb.getFuturePassiveParticipleYaProduct(Gender.FEM), Gender.FEM);
					addDeriVerbTable(currVerb.getFuturePassiveParticipleYaProduct(Gender.NEU), Gender.NEU);
					addDeriVerbTable(currVerb.getFuturePassiveParticipleCausativeProduct(Gender.MAS), Gender.MAS, VForm.CAU);
					addDeriVerbTable(currVerb.getFuturePassiveParticipleCausativeProduct(Gender.FEM), Gender.FEM, VForm.CAU);
					addDeriVerbTable(currVerb.getFuturePassiveParticipleCausativeProduct(Gender.NEU), Gender.NEU, VForm.CAU);
					addDeriVerbTable(currVerb.getFuturePassiveParticipleNiyaCausativeProduct(Gender.MAS), Gender.MAS, VForm.CAU);
					addDeriVerbTable(currVerb.getFuturePassiveParticipleNiyaCausativeProduct(Gender.FEM), Gender.FEM, VForm.CAU);
					addDeriVerbTable(currVerb.getFuturePassiveParticipleNiyaCausativeProduct(Gender.NEU), Gender.NEU, VForm.CAU);
					addDeriVerbTable(currVerb.getFuturePassiveParticipleYaCausativeProduct(Gender.MAS), Gender.MAS, VForm.CAU);
					addDeriVerbTable(currVerb.getFuturePassiveParticipleYaCausativeProduct(Gender.FEM), Gender.FEM, VForm.CAU);
					addDeriVerbTable(currVerb.getFuturePassiveParticipleYaCausativeProduct(Gender.NEU), Gender.NEU, VForm.CAU);
					break;
				case 9: // INF
					addIndeclinableTable(currVerb.getInfinitiveProduct());
					break;
				case 10: // ABS
					addIndeclinableTable(currVerb.getAbsolutiveProduct());
					break;
			}
		}
	}

	private void addMainVerbTable(final Map<Person, Map<Number, List<String>>> product, final VForm vform) {
		if (product.isEmpty()) return;
		exportedResult.append(LINESEP);
		final StackPane conjStack = new StackPane();
		conjStack.getStyleClass().add("tablegridbox");
		conjStack.getChildren().add(createConjugationGrid(product, vform));
		conjStack.setMinWidth(Utilities.getRelativeSize(40));
		conjStack.setMaxWidth(Utilities.getRelativeSize(40));
		tablePane.getChildren().add(conjStack);
	}

	public GridPane createConjugationGrid(final Map<Person, Map<Number, List<String>>> product, final VForm vform) {
		final GridPane resultGrid = new GridPane();
		resultGrid.setHgap(4);
		resultGrid.setVgap(2);
		resultGrid.setPadding(new Insets(2, 2, 2, 2));
		// top head showing verb form
		final Label lblTopHead = new Label(vform.getName().toUpperCase());
		lblTopHead.setStyle("-fx-font-weight:bold");
		GridPane.setConstraints(lblTopHead, 0, 0, 4, 1, HPos.CENTER, VPos.TOP);
		resultGrid.getChildren().add(lblTopHead);
		exportedResult.append(lblTopHead.getText()).append(LINESEP);
		// number head
		addNumberHeader(resultGrid);
		// product data
		for (final Person p : Person.values) {
			final String pInit = devaMenuItem.isSelected()
									? p.getDevaInitial()
									: p.getInitial();
			final Label lblPersonHead = createCell(pInit, 1.5, false);
			lblPersonHead.setTooltip(new Tooltip(p.getDescription()));
			lblPersonHead.setStyle("-fx-font-weight:bold");
			GridPane.setConstraints(lblPersonHead, 0, p.ordinal()+2, 1, 1, HPos.LEFT, VPos.TOP);
			resultGrid.getChildren().add(lblPersonHead);
			exportedResult.append(lblPersonHead.getText());
			final Map<Number, List<String>> numMap = product.get(p);
			for (final Number n : Number.values) {
				final String terms = numMap.get(n).stream().collect(Collectors.joining(", "));
				final String termsFinal = devaMenuItem.isSelected()
										? ScriptTransliterator.translitQuick(terms, ScriptTransliterator.EngineType.ROMAN_SKT_DEVA, false)
										: terms;
				final Label lblTerms = createCell(termsFinal, 12, true);
				GridPane.setConstraints(lblTerms, n.ordinal()+1, p.ordinal()+2, 1, 1, HPos.LEFT, VPos.TOP);
				resultGrid.getChildren().add(lblTerms);
				exportedResult.append(DELIM).append(lblTerms.getText());
			}
			exportedResult.append(LINESEP);
		}
		return resultGrid;
	}

	private void addDeriVerbTable(final Map<Case, Map<SktDeclension.Number, List<String>>> product, final Gender gender, final VForm... vform) {
		if (product.isEmpty()) return;
		exportedResult.append(LINESEP);
		final StackPane declStack = new StackPane();
		declStack.getStyleClass().add("tablegridbox");
		declStack.getChildren().add(createDeclensionGrid(product, gender, vform));
		declStack.setMinWidth(Utilities.getRelativeSize(40));
		declStack.setMaxWidth(Utilities.getRelativeSize(40));
		tablePane.getChildren().add(declStack);
	}

	public GridPane createDeclensionGrid(final Map<Case, Map<SktDeclension.Number, List<String>>> product, final Gender gender, final VForm... vform) {
		final GridPane resultGrid = new GridPane();
		resultGrid.setHgap(4);
		resultGrid.setVgap(2);
		resultGrid.setPadding(new Insets(2, 2, 2, 2));
		// top head showing gender form
		final String headStr = vform != null && vform.length > 0
								? vform[0].getName().toUpperCase() + " " + gender.getName().toUpperCase()
								: gender.getName().toUpperCase();
		final Label lblTopHead = new Label(headStr);
		lblTopHead.setStyle("-fx-font-weight:bold");
		GridPane.setConstraints(lblTopHead, 0, 0, 4, 1, HPos.CENTER, VPos.TOP);
		resultGrid.getChildren().add(lblTopHead);
		exportedResult.append(lblTopHead.getText()).append(LINESEP);
		// number head
		addNumberHeader(resultGrid);
		// product data
		for (final Case c : Case.values) {
			final String cInit = devaMenuItem.isSelected()
									? c.getDevaNumAbbr()
									: c.getNumAbbr();
			final Label lblCaseHead = createCell(cInit, 1.5, false);
			lblCaseHead.setTooltip(new Tooltip(c.getName()));
			lblCaseHead.setStyle("-fx-font-weight:bold");
			GridPane.setConstraints(lblCaseHead, 0, c.ordinal()+2, 1, 1, HPos.LEFT, VPos.TOP);
			resultGrid.getChildren().add(lblCaseHead);
			exportedResult.append(lblCaseHead.getText());
			final Map<SktDeclension.Number, List<String>> numMap = product.get(c);
			for (final SktDeclension.Number n : SktDeclension.Number.values) {
				final String terms = numMap.get(n).stream().collect(Collectors.joining(", "));
				final String termsFinal = devaMenuItem.isSelected()
										? ScriptTransliterator.translitQuick(terms, ScriptTransliterator.EngineType.ROMAN_SKT_DEVA, false)
										: terms;
				final Label lblTerms = createCell(termsFinal, 12, true);
				GridPane.setConstraints(lblTerms, n.ordinal()+1, c.ordinal()+2, 1, 1, HPos.LEFT, VPos.TOP);
				resultGrid.getChildren().add(lblTerms);
				exportedResult.append(DELIM).append(lblTerms.getText());
			}
			exportedResult.append(LINESEP);
		}
		return resultGrid;
	}

	private void addNumberHeader(final GridPane grid) {
		final String singStr = devaMenuItem.isSelected()
								? Number.SING.getDevaName()
								: Number.SING.getName();
		final Label lblSingHead = new Label(singStr);
		lblSingHead.setStyle("-fx-font-weight:bold");
		final String dualStr = devaMenuItem.isSelected()
								? Number.DUAL.getDevaName()
								: Number.DUAL.getName();
		final Label lblDualHead = new Label(dualStr);
		lblDualHead.setStyle("-fx-font-weight:bold");
		final String pluStr = devaMenuItem.isSelected()
								? Number.PLU.getDevaName()
								: Number.PLU.getName();
		final Label lblPluHead = new Label(pluStr);
		lblPluHead.setStyle("-fx-font-weight:bold");
		GridPane.setConstraints(lblSingHead, 1, 1);
		GridPane.setConstraints(lblDualHead, 2, 1);
		GridPane.setConstraints(lblPluHead, 3, 1);	
		grid.getChildren().addAll(lblSingHead, lblDualHead, lblPluHead);
		exportedResult.append(" " + DELIM)
						.append(lblSingHead.getText()).append(DELIM)
						.append(lblDualHead.getText()).append(DELIM)
						.append(lblPluHead.getText()).append(LINESEP);
	}

	private void addIndeclinableTable(final List<StringPair> product) {
		if (product.isEmpty()) return;
		exportedResult.append(LINESEP);
		final StackPane indStack = new StackPane();
		indStack.getStyleClass().add("tablegridbox");
		indStack.getChildren().add(createIndeclinableGrid(product));
		indStack.setMinWidth(Utilities.getRelativeSize(40));
		indStack.setMaxWidth(Utilities.getRelativeSize(40));
		tablePane.getChildren().add(indStack);
	}

	public GridPane createIndeclinableGrid(final List<StringPair> product) {
		final GridPane resultGrid = new GridPane();
		resultGrid.setHgap(4);
		resultGrid.setVgap(2);
		resultGrid.setPadding(new Insets(2, 2, 2, 2));
		// show the product line by line
		int row = 0;
		for (final StringPair pair : product) {
			final Label lblHead = createCell(pair.getFirst() + ":", 15, false);
			lblHead.setAlignment(Pos.CENTER_RIGHT);
			final String termStr = devaMenuItem.isSelected()
									? ScriptTransliterator.translitQuick(pair.getSecond(), ScriptTransliterator.EngineType.ROMAN_SKT_DEVA, false)
									: pair.getSecond();
			final Label lblTerm = createCell(termStr, 20, false);
			lblTerm.setStyle("-fx-font-weight:bold");
			GridPane.setConstraints(lblHead, 0, row, 1, 1, HPos.RIGHT, VPos.TOP);
			GridPane.setConstraints(lblTerm, 1, row, 1, 1, HPos.LEFT, VPos.TOP);
			resultGrid.getChildren().addAll(lblHead, lblTerm);
			exportedResult.append(lblHead.getText()).append(" ").append(lblTerm.getText()).append(LINESEP);
			row++;
		}
		return resultGrid;
	}

	private Label createCell(final String str, final double width, final boolean isWrapped) {
		final Label lbl = new Label(str);
		lbl.setWrapText(isWrapped);
		lbl.setMinWidth(Utilities.getRelativeSize(width));
		lbl.setMaxWidth(Utilities.getRelativeSize(width));
		return lbl;
	}

	private void copyText() {
		Utilities.copyText(exportedResult.toString());
	}
	
	private void saveText() {
		Utilities.saveText(exportedResult.toString(), "sktconjugation.txt");
	}

}

