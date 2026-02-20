/*
 * SandhiWin.java
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

import java.util.*;
import java.util.stream.*;
import java.text.Normalizer;
import java.text.Normalizer.Form;

import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.scene.image.*;
import javafx.scene.layout.*;
import javafx.geometry.Pos;
import javafx.geometry.Insets;
/** 
 * The window showing Sanskrit external sandhi rules.
 * This is a singleton.
 * @author J.R. Bhaddacak
 * @version 4.1
 * @since 4.1
 */
public final class SandhiWin extends SingletonWindow {
	static enum OutputForm { DASH, WORD };
	public static final SandhiWin INSTANCE = new SandhiWin();
	private final BorderPane mainPane = new BorderPane();
	private final ComboBox<String> endingChoice = new ComboBox<>();
	private final ComboBox<String> beginningChoice = new ComboBox<>();
	private final ChoiceBox<OutputForm> outputFormChoice = new ChoiceBox<>();
	private final CheckMenuItem showDevaMenuItem = new CheckMenuItem("Show Devanāgarī");
	private final TextField searchTextField;
	private final GridPane outputGrid = new GridPane();
	private final InfoPopup infoPopup = new InfoPopup();
	private final List<SandhiOutput> outputList = new ArrayList<>();

	private SandhiWin() {
		windowWidth = Utilities.getRelativeSize(55);
		windowHeight = Utilities.getRelativeSize(46);
		setTitle("Sanskrit Sandhi Rules");
		getIcons().add(new Image(SandhiWin.class.getResourceAsStream("resources/images/handshake.png")));
		// add common toolbar on the top
		final CommonWorkingToolBar toolBar = new CommonWorkingToolBar(outputGrid);
		// configure some buttons first
		toolBar.saveTextButton.setTooltip(new Tooltip("Save data as text"));
		toolBar.saveTextButton.setOnAction(actionEvent -> saveText());		
		toolBar.copyButton.setTooltip(new Tooltip("Copy text data to clipboard"));
		toolBar.copyButton.setOnAction(actionEvent -> copyText());	
		// add new toolbar components
		endingChoice.setTooltip(new Tooltip("First word's ending"));
		endingChoice.getItems().add("ALL");
		for (final String end : Sandhi.availEndings) {
			endingChoice.getItems().add(Sandhi.restoreChar(end));
		}
		endingChoice.getSelectionModel().select(1);
		endingChoice.setOnAction(actionEvent -> updateOutput());
		beginningChoice.setTooltip(new Tooltip("Second word's beginning"));
		beginningChoice.getItems().add("ALL");
		for (final String start : Sandhi.availBeginnings) {
			beginningChoice.getItems().add(Sandhi.restoreChar(start));
		}
		beginningChoice.getSelectionModel().select(0);
		beginningChoice.setOnAction(actionEvent -> updateOutput());
		outputFormChoice.setTooltip(new Tooltip("Output form"));
		outputFormChoice.getItems().addAll(OutputForm.DASH, OutputForm.WORD);
		outputFormChoice.getSelectionModel().select(0);
		outputFormChoice.setOnAction(actionEvent -> updateOutput());
		final MenuButton optionsMenu = new MenuButton("", new TextIcon("check-double", TextIcon.IconSet.AWESOME));		
		optionsMenu.setTooltip(new Tooltip("Options"));
		showDevaMenuItem.setSelected(false);
		showDevaMenuItem.setOnAction(actionEvent -> updateOutput());
		optionsMenu.getItems().add(showDevaMenuItem);
		// add help button
		final Button helpButton = new Button("", new TextIcon("circle-question", TextIcon.IconSet.AWESOME));
		helpButton.setOnAction(actionEvent -> infoPopup.showPopup(helpButton, InfoPopup.Pos.BELOW_RIGHT, true));
		toolBar.getItems().addAll(new Separator(), endingChoice, new Label("+"), beginningChoice,
								new Separator(), outputFormChoice, optionsMenu, helpButton);
		mainPane.setTop(toolBar);
		// add search bar at the bottom
		final HBox searchBox = new HBox();
		searchBox.setPadding(new Insets(3, 0, 0, 0));
		searchBox.setSpacing(3);
		final PaliTextInput searchInput = new PaliTextInput(PaliTextInput.InputType.FIELD);
		searchInput.setSanskritMode(true);
		final String inputMethod = Utilities.getSetting("sanskrit-input-method");
		searchInput.setInputMethod(PaliTextInput.InputMethod.valueOf(inputMethod));
		searchTextField = (TextField)searchInput.getInput();
		searchTextField.setPromptText("Filter only...");
		searchTextField.textProperty().addListener((obs, oldValue, newValue) -> updateDisplay());
		final Button clearButton = searchInput.getClearButton();
		clearButton.setOnAction(actionEvent -> searchTextField.clear());
		searchBox.getChildren().addAll(searchTextField, clearButton, searchInput.getMethodButton());
		mainPane.setBottom(searchBox);
		// add main content
		final ScrollPane scrollPane = new ScrollPane();
		final StackPane outputPane = new StackPane();
		StackPane.setMargin(outputGrid, new Insets(5, 10, 5, 10));
		outputGrid.setAlignment(Pos.TOP_CENTER);
		outputGrid.setHgap(10);
		outputGrid.setVgap(3);
		outputPane.getChildren().add(outputGrid);
		scrollPane.setContent(outputPane);
		mainPane.setCenter(scrollPane);
		final Scene scene = new Scene(mainPane, windowWidth, windowHeight);
		setScene(scene);
		
		// prepare info popup
		infoPopup.setContentWithText(SanskritUtilities.getTextResource("info-sanskrit-sandhi.txt"));
		infoPopup.setTextWidth(Utilities.getRelativeSize(38));		

		// start display
		updateOutput();
	}

	private void updateOutput() {
		final String endSelected = endingChoice.getSelectionModel().getSelectedItem();
		final String startSelected = beginningChoice.getSelectionModel().getSelectedItem();
		final OutputForm formSelected = outputFormChoice.getSelectionModel().getSelectedItem();
		if (endSelected == null || startSelected == null || formSelected == null) return;
		outputList.clear();
		final String end = Sandhi.normalize(endSelected);
		final String start = Sandhi.normalize(startSelected);
		// create first word
		final List<String> firstWordList = new ArrayList<>();
		if (end.equals("all")) {
			for (final String s : Sandhi.availEndings) {
				final List<String> firstWords = getFirstWord(s, formSelected, false);
				firstWordList.addAll(firstWords);
			}
		} else {
			final List<String> words = getFirstWord(end, formSelected, !start.equals("all"));
			firstWordList.addAll(words);
		}
		// create the list of second word
		final List<String> secondWordList = new ArrayList<>();
		if (start.equals("all")) {
			for (final String s : Sandhi.availBeginnings) {
				final String secondWord = getSecondWord(s, formSelected);
				secondWordList.add(secondWord);
			}
		} else {
			final String word = getSecondWord(start, formSelected);
			secondWordList.add(word);
		}
		for (final String secondWord : secondWordList) {
			for (final String firstWord : firstWordList) {
				final Sandhi sandhi = new Sandhi(firstWord, secondWord);
				final String caseRoman = sandhi.getFirst() + " + " + sandhi.getSecond();
				final String caseDeva = showDevaMenuItem.isSelected()
					? ScriptTransliterator.translitQuick(caseRoman, ScriptTransliterator.EngineType.ROMAN_SKT_DEVA, false)
					: "";
				final String prodRoman = sandhi.getProductRoman();
				final String prodDeva = showDevaMenuItem.isSelected()
					? sandhi.getProductDeva()
					: "";
				outputList.add(new SandhiOutput(caseRoman, prodRoman, caseDeva, prodDeva));
			}
		}
		updateDisplay();
	}

	private void updateDisplay() {
		outputGrid.getChildren().clear();
		String query = Normalizer.normalize(searchTextField.getText(), Form.NFC);
		query = query.indexOf("+") > -1 ? query.replace("+", " + ") : query;
		final String finQuery = query.replaceAll("\\s+", " ");
		final Utilities.PaliScript script = Utilities.testLanguage(query);
		final List<SandhiOutput> workingList = new ArrayList<>();
		if (finQuery.isEmpty()) {
			workingList.addAll(outputList);
		} else {
			final List<SandhiOutput> searchOutput;
			if (script == Utilities.PaliScript.DEVANAGARI) {
				if (query.indexOf("+") > -1) {
					searchOutput = outputList.stream()
									.filter(x -> x.caseDeva.indexOf(finQuery) > -1)
									.collect(Collectors.toList());
				} else {
					searchOutput = outputList.stream()
									.filter(x -> x.productDeva.indexOf(finQuery) > -1)
									.collect(Collectors.toList());
				}
			} else {
				if (query.indexOf("+") > -1) {
					searchOutput = outputList.stream()
									.filter(x -> x.caseRoman.indexOf(finQuery) > -1)
									.collect(Collectors.toList());
				} else {
					searchOutput = outputList.stream()
									.filter(x -> x.productRoman.indexOf(finQuery) > -1)
									.collect(Collectors.toList());
				}
			}
			workingList.addAll(searchOutput);
		}
		final String[] strResult = { "", "", "", "", "", "", "" };
		for (final SandhiOutput out : workingList) {
			strResult[0] = strResult[0] + out.caseRoman + "\n";
			strResult[1] = strResult[1] + "=\n";
			strResult[2] = strResult[2] + out.productRoman + "\n";
			strResult[3] = strResult[3] + "│\n";
			strResult[4] = strResult[4] + out.caseDeva + "\n";
			strResult[5] = strResult[5] + "=\n";
			strResult[6] = strResult[6] + out.productDeva + "\n";
		}
		final Label lbCaseRoman = new Label(strResult[0]);
		final Label lbEqual1 = new Label(strResult[1]);
		final Label lbProductRoman = new Label(strResult[2]);
		final Label lbBar = new Label(strResult[3]);
		final Label lbCaseDeva = new Label(strResult[4]);
		final Label lbEqual2 = new Label(strResult[5]);
		final Label lbProductDeva = new Label(strResult[6]);
		GridPane.setConstraints(lbCaseRoman, 0, 0);
		GridPane.setConstraints(lbEqual1, 1, 0);
		GridPane.setConstraints(lbProductRoman, 2, 0);
		GridPane.setConstraints(lbBar, 3, 0);
		GridPane.setConstraints(lbCaseDeva, 4, 0);
		GridPane.setConstraints(lbEqual2, 5, 0);
		GridPane.setConstraints(lbProductDeva, 6, 0);
		if (showDevaMenuItem.isSelected())
			outputGrid.getChildren().addAll(lbCaseRoman, lbEqual1, lbProductRoman, lbBar, lbCaseDeva, lbEqual2, lbProductDeva);
		else
			outputGrid.getChildren().addAll(lbCaseRoman, lbEqual1, lbProductRoman);
	}

	private List<String> getFirstWord(final String end, final OutputForm form, final boolean isFull) {
		final List<String> wordEndList = new ArrayList<>();
		if (isFull) {
			for (final char v : Sandhi.sktVowels.toCharArray()) {
				wordEndList.add("-" + v + end);
			}
		} else {
			wordEndList.addAll(Arrays.asList("-a" + end, "-ā" + end));
		}
		final List<String> result = new ArrayList<>();
			if (form == OutputForm.DASH) {
				if (Sandhi.sktVowels.indexOf(end.charAt(0)) > -1)
					result.add("-" + end);
				else
					result.addAll(wordEndList);
			} else {
				final List<String> endList = wordEndList.stream()
											.map(x -> x.replace("-", "kat"))
											.collect(Collectors.toList());
				if (Sandhi.sktVowels.indexOf(end.charAt(0)) > -1)
					result.add("kat" + end);
				else
					result.addAll(endList);
			}
		return result;
	}

	private String getSecondWord(final String start, final OutputForm form) {
		final String result = form == OutputForm.DASH
								? start + "-"
								: Sandhi.sktVowels.indexOf(start) > -1
									? start + "taḥ"
									: start + "ataḥ";
		return result;
	}

	private String makeText() {
		final String result = outputList.stream()
								.map(x -> x.toString())
								.collect(Collectors.joining("\n"));
		return result;
	}

	private void copyText() {
		Utilities.copyText(makeText());
	}
	
	private void saveText() {
		Utilities.saveText(makeText(), "sktsandhi.txt");
	}

	// inner class
	class SandhiOutput {
		public final String caseRoman;
		public final String productRoman;
		public final String caseDeva;
		public final String productDeva;
		public SandhiOutput(final String cr, final String pr, final String cd, final String pd) {
			caseRoman = cr;
			productRoman = pr;
			caseDeva = cd;
			productDeva = pd;
		}
		@Override
		public String toString() {
			String result = caseRoman + " = " + productRoman;
			result = caseDeva.isEmpty() ? result : result + " | " + caseDeva + " = " + productDeva;
			return result;
		}
	}

}
