/*
 * LetterWin.java
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
import java.text.Normalizer;
import java.text.Normalizer.Form;

import javafx.scene.*;
import javafx.scene.text.Text;
import javafx.scene.image.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.event.ActionEvent;
import javafx.geometry.Pos;
import javafx.geometry.Insets;
import javafx.beans.property.*;

/** 
 * The window showing Pali letters in various scripts.
 * This is a singleton.
 * @author J.R. Bhaddacak
 * @version 3.2
 * @since 2.0
 */
public final class LetterWin extends SingletonWindow {
	static enum TagType {
		VOWEL("vowel;sara"), CONSONANT("consonant;vyañjana"), GUTTURAL("guttural;kaṇṭhaja"), PALATAL("palatal;tāluja"),
		CEREBRAL("cerebral/retroflex;muḍḍhaja"), DENTAL("dental;dantaja"), LABIAL("labial;oṭṭhaja"), NASAL("nasal;nāsikā"),
		VOICELESS("voiceless;aghosa"), VOICED("voiced;ghosa"), UNASPIRATED("unaspirated;sithila"), ASPIRATED("aspirated;dhanita"),
		SEMIVOWEL("semivowel;antaṭṭha"), SIBILANT("sibilant;sakāra"), SPIRANT("spirant;hakāra");
		public static final TagType[] values = values();
		public final String romanName;
		public final String paliName;
		private TagType(final String strName) {
			final String[] names = strName.split(";");
			romanName = names[0];
			paliName = names[1];
		}
		public List<int[]> getLetterPosList() {
			final List<int[]> result;
			switch (this.ordinal()) {
				case 0: // vowel
					final int[][] l0 = {{0,0},{0,1},{0,2},{0,3},{0,4},{0,5},{0,6},{0,7}};
					result = Arrays.asList(l0);
					break;
				case 1: // consonant
					final int[][] l1 = {{1,0},{1,1},{1,2},{1,3},{1,4},
										{2,0},{2,1},{2,2},{2,3},{2,4},
										{3,0},{3,1},{3,2},{3,3},{3,4},
										{4,0},{4,1},{4,2},{4,3},{4,4},
										{5,0},{5,1},{5,2},{5,3},{5,4},
										{6,0},{6,1},{6,2},{6,3},{6,4},{6,5},{6,6}};
					result = Arrays.asList(l1);
					break;
				case 2: // guttural
					final int[][] l2 = {{1,0},{1,1},{1,2},{1,3},{1,4},{0,0},{0,1},{0,6},{0,7},{6,5}};
					result = Arrays.asList(l2);
					break;
				case 3: // palatal
					final int[][] l3 = {{2,0},{2,1},{2,2},{2,3},{2,4},{0,2},{0,3},{0,6},{6,0}};
					result = Arrays.asList(l3);
					break;
				case 4: // cerebral
					final int[][] l4 = {{3,0},{3,1},{3,2},{3,3},{3,4},{6,1},{6,6}};
					result = Arrays.asList(l4);
					break;
				case 5: // dental
					final int[][] l5 = {{4,0},{4,1},{4,2},{4,3},{4,4},{6,2},{6,3},{6,4}};
					result = Arrays.asList(l5);
					break;
				case 6: // labial
					final int[][] l6 = {{5,0},{5,1},{5,2},{5,3},{5,4},{0,4},{0,5},{0,7},{6,3}};
					result = Arrays.asList(l6);
					break;
				case 7: //nasal
					final int[][] l7 = {{1,4},{2,4},{3,4},{4,4},{5,4},{6,7}};
					result = Arrays.asList(l7);
					break;
				case 8: //voiceless
					final int[][] l8 = {{1,0},{2,0},{3,0},{4,0},{5,0},{1,1},{2,1},{3,1},{4,1},{5,1}};
					result = Arrays.asList(l8);
					break;
				case 9: //voiced
					final int[][] l9 = {{1,2},{2,2},{3,2},{4,2},{5,2},{1,3},{2,3},{3,3},{4,3},{5,3}};
					result = Arrays.asList(l9);
					break;
				case 10: //unaspirated
					final int[][] l10 = {{1,0},{2,0},{3,0},{4,0},{5,0},{1,2},{2,2},{3,2},{4,2},{5,2}};
					result = Arrays.asList(l10);
					break;
				case 11: //aspirated
					final int[][] l11 = {{1,1},{2,1},{3,1},{4,1},{5,1},{1,3},{2,3},{3,3},{4,3},{5,3}};
					result = Arrays.asList(l11);
					break;
				case 12: //semivowel
					final int[][] l12 = {{6,0},{6,1},{6,2},{6,3},{6,6}};
					result = Arrays.asList(l12);
					break;
				case 13: //sibilant
					final int[][] l13 = {{6,4}};
					result = Arrays.asList(l13);
					break;
				case 14: //spirant
					final int[][] l14 = {{6,5}};
					result = Arrays.asList(l14);
					break;
				default:
					result = new ArrayList<>();
			}
			return result;
		}
	}
	public static final LetterWin INSTANCE = new LetterWin();
	private static final double DEF_FONT_SCALE = 1.8;
	private static final int DEF_BIG_CHAR_SIZE = 1000;
	private final String[][] paliChars = new String[8][10];
	private final BorderPane mainPane = new BorderPane();
	private final ToggleButton epSwitchButton = new ToggleButton("E/P");
	private final Text bigChar = new Text();
	private final SimpleStringProperty selectedChar = new SimpleStringProperty("");
	private final VBox tagBox = new VBox();
	private final GridPane letterGrid = new GridPane();
	private final CommonWorkingToolBar toolBar = new CommonWorkingToolBar(letterGrid);
	private final VBox typingBox = new VBox();
	private final Label typingOutput = new Label();
	private final TextField typingTextField;
	private final int[] currSelectedPos = { -1, -1 };
	private Utilities.PaliScript currPaliScript = Utilities.PaliScript.ROMAN;
	private int currFontPercent = 100;
	private int currBigCharSize = DEF_BIG_CHAR_SIZE;
	
	private LetterWin() {
		windowWidth = Utilities.getRelativeSize(52);
		windowHeight = Utilities.getRelativeSize(54);
		setTitle("Pāli Letters");
		getIcons().add(new Image(LetterWin.class.getResourceAsStream("resources/images/font.png")));
		fillCharacterArray(currPaliScript);
		
		// add common toolbar on the top
		// configure some buttons first
		toolBar.saveTextButton.setTooltip(new Tooltip("Save data as text"));
		toolBar.saveTextButton.setOnAction(actionEvent -> saveText());		
		toolBar.copyButton.setTooltip(new Tooltip("Copy text data to clipboard"));
		toolBar.copyButton.setOnAction(actionEvent -> copyText());	
		toolBar.getZoomInButton().setOnAction((actionEvent -> updateLetterDisplay(+10)));
		toolBar.getZoomOutButton().setOnAction((actionEvent -> updateLetterDisplay(-10)));
		toolBar.getResetButton().setOnAction((actionEvent -> updateLetterDisplay(0)));
		// add new buttons
		epSwitchButton.setTooltip(new Tooltip("English/Pāli"));
		epSwitchButton.setOnAction(actionEvent -> updateTagArray());
		final Button cleanButton = new Button("", new TextIcon("broom", TextIcon.IconSet.AWESOME));
		cleanButton.setTooltip(new Tooltip("Clear highlights"));
		cleanButton.setOnAction(actionEvent -> {
			clearBigChar();
			clearTagHighlights();
			clearGridHighlights();
		});
		final MenuButton convertMenu = new MenuButton("", new TextIcon("language", TextIcon.IconSet.AWESOME));
		final ToggleGroup scriptGroup = new ToggleGroup();
		for (final Utilities.PaliScript sc : Utilities.PaliScript.scripts){
			if (sc.ordinal() == 0) continue;
			final String n = sc.toString();
			final RadioMenuItem scriptItem = new RadioMenuItem(n.charAt(0) + n.substring(1).toLowerCase());
			scriptItem.setToggleGroup(scriptGroup);
			scriptItem.setSelected(scriptItem.getText().toUpperCase().equals(currPaliScript.toString()));
			convertMenu.getItems().add(scriptItem);
		}
        scriptGroup.selectedToggleProperty().addListener((observable) -> {
			if (scriptGroup.getSelectedToggle() != null) {
				final RadioMenuItem selected = (RadioMenuItem)scriptGroup.getSelectedToggle();
				final Utilities.PaliScript toScript = Utilities.PaliScript.valueOf(selected.getText().toUpperCase());
				if (currPaliScript != toScript) {
					currPaliScript = toScript;
					setTypingOutput();
					fillCharacterArray(currPaliScript); 
					currBigCharSize = DEF_BIG_CHAR_SIZE;
					toolBar.setupFontMenu(currPaliScript);
					toolBar.resetFont(currPaliScript);
					updateLetterDisplay();
				}
			}
        });
		final Button typetestButton = new Button("", new TextIcon("keyboard", TextIcon.IconSet.AWESOME));
		typetestButton.setTooltip(new Tooltip("Typing test"));
		typetestButton.setOnAction(actionEvent -> openTypingTest());
		toolBar.getItems().addAll(new Separator(), epSwitchButton, cleanButton, convertMenu, typetestButton);
		mainPane.setTop(toolBar);

		// add main content
		final Scene scene = new Scene(mainPane, windowWidth, windowHeight);
		// add tag list on the left
		for (final TagType tt : TagType.values) {
			final Button a = new Button(tt.romanName, new TextIcon("tag", TextIcon.IconSet.AWESOME));
			a.setPrefWidth(120);
			a.setStyle("-fx-alignment:center-left");
			a.setTooltip(new Tooltip(tt.romanName + " (" + tt.paliName + ")"));
			a.setUserData(tt);
			a.setOnAction(actionEvent ->  {
				updateTagArray(Arrays.asList(tt));
				showLetterHighlights(actionEvent);
				setBigChar("");
			}); 
			tagBox.getChildren().add(a);
		}
		mainPane.setLeft(tagBox);
		
		// add letter table at the center
		final StackPane letterPane = new StackPane();
		letterGrid.setHgap(2);
		letterGrid.setVgap(2);
		letterGrid.setPadding(new Insets(2, 2, 2, 2));
		for (int rowInd=0; rowInd<paliChars.length; rowInd++) {
			for (int colInd=0; colInd<paliChars[rowInd].length; colInd++) {
				final String ch = paliChars[rowInd][colInd];
				if (ch != null && !ch.isEmpty()) {
					final StackPane stp = new StackPane();
					stp.prefWidthProperty().bind(letterPane.widthProperty().divide(10.0));
					stp.prefHeightProperty().bind(letterPane.heightProperty().divide(8.0));
					stp.getStyleClass().add("letterbox");
					stp.setOnMouseClicked(mouseEvent -> showTagHighlight(mouseEvent));
					final Label lbLetter = new Label();
					stp.getChildren().add(lbLetter);
					GridPane.setConstraints(stp, colInd, rowInd, 1, 1);
					letterGrid.getChildren().add(stp);
				}
			}
		}
		updateLetterDisplay(0);
		// add big character display
		final StackPane bigCharPane = new StackPane();
		bigCharPane.getStyleClass().add("bigcharbox");
		bigChar.textProperty().bind(selectedChar);
		bigChar.getStyleClass().add("bigchar");
		// add - and + buttons to resize the big char
		final Label minusLabel = new Label("", new TextIcon("circle-minus", TextIcon.IconSet.AWESOME, Utilities.IconSize.BIG));
		minusLabel.getStyleClass().add("labelbutton");
		minusLabel.setOnMouseClicked(mouseEvent -> resizeBigChar(-100));
		StackPane.setAlignment(minusLabel, Pos.TOP_LEFT);
		StackPane.setMargin(minusLabel, new Insets(5, 0, 0, 5));
		final Label plusLabel = new Label("", new TextIcon("circle-plus", TextIcon.IconSet.AWESOME, Utilities.IconSize.BIG));
		plusLabel.getStyleClass().add("labelbutton");
		plusLabel.setOnMouseClicked(mouseEvent -> resizeBigChar(+100));
		StackPane.setAlignment(plusLabel, Pos.TOP_RIGHT);
		StackPane.setMargin(plusLabel, new Insets(5, 5, 0, 0));
		StackPane.setAlignment(bigChar, Pos.TOP_CENTER);
		StackPane.setMargin(bigChar, new Insets(20, 0, 0, 0));
		bigCharPane.getChildren().addAll(bigChar, minusLabel, plusLabel);
		GridPane.setConstraints(bigCharPane, 5, 1, 5, 5);

		letterGrid.getChildren().add(bigCharPane);
		letterPane.getChildren().add(letterGrid);
		mainPane.setCenter(letterPane);
		
		setScene(scene);

		// prepare typing test components
		final PaliTextInput typingTextInput = new PaliTextInput(PaliTextInput.InputType.FIELD);
		typingTextField = (TextField)typingTextInput.getInput();
		typingTextField.setPromptText("Type something...");
		typingTextField.prefWidthProperty().bind(scene.widthProperty());
		typingTextField.textProperty().addListener((obs, oldValue, newValue) -> {
			final String text = Normalizer.normalize(newValue, Form.NFC);
			setTypingOutput(text);
		});
		final Button typingClearButton = typingTextInput.getClearButton();
		typingClearButton.setOnAction(actionEvent -> {
			typingTextField.clear();
			typingOutput.setText("");
		});
		final HBox typingInputBox = new HBox();
		typingInputBox.getChildren().addAll(typingTextField, typingClearButton, typingTextInput.getMethodButton());
		typingBox.setPadding(new Insets(3));
		typingBox.getChildren().addAll(typingInputBox, typingOutput);

	} // end constructor
	
	private void fillCharacterArray(final Utilities.PaliScript script) {
		if (script == Utilities.PaliScript.ROMAN) {
			for (int i=0; i<PaliCharTransformer.romanVowels.length(); i++)
				paliChars[0][i] = "" + PaliCharTransformer.romanVowels.charAt(i);
			for (int i=1; i<=5; i++) {
				for (int j=0; j<5; j++) 
					paliChars[i][j] = PaliCharTransformer.romanConsonantsStr[(i-1)*5 + j];
			}
			for (int j=0; j<8; j++) 
				paliChars[6][j] = PaliCharTransformer.romanConsonantsStr[25+j];
			for (int j=1; j<10; j++)
				paliChars[7][j-1] = "" + PaliCharTransformer.romanNumbers[j];
			paliChars[7][9] = "" + PaliCharTransformer.romanNumbers[0];
		} else {
			final char[] arrVowels;
			final char[] arrConsonants;
			final char[] arrNumbers;
			if (script == Utilities.PaliScript.THAI) {
				arrVowels = PaliCharTransformer.thaiVowels;
				arrConsonants = PaliCharTransformer.thaiConsonants;
				arrNumbers = PaliCharTransformer.thaiNumbers;
			} else if (script == Utilities.PaliScript.KHMER) {
				arrVowels = PaliCharTransformer.khmerVowelsInd;
				arrConsonants = PaliCharTransformer.khmerConsonants;
				arrNumbers = PaliCharTransformer.khmerNumbers;
			} else if (script == Utilities.PaliScript.MYANMAR) {
				arrVowels = PaliCharTransformer.myanmarVowelsInd;
				arrConsonants = PaliCharTransformer.myanmarConsonants;
				arrNumbers = PaliCharTransformer.myanmarNumbers;
			} else if (script == Utilities.PaliScript.SINHALA) {
				arrVowels = PaliCharTransformer.sinhalaVowelsInd;
				arrConsonants = PaliCharTransformer.sinhalaConsonants;
				arrNumbers = PaliCharTransformer.romanNumbers;
			} else {
				arrVowels = PaliCharTransformer.devaVowelsInd;
				arrConsonants = PaliCharTransformer.devaConsonants;
				arrNumbers = PaliCharTransformer.devaNumbers;
			}
			for (int i=0; i<arrVowels.length; i++) {
				if ((script == Utilities.PaliScript.KHMER || script == Utilities.PaliScript.MYANMAR) && i == 1)
					paliChars[0][1] = "" + arrVowels[0] + arrVowels[1]; // special case for ā
				else
					paliChars[0][i] = "" + arrVowels[i];
			}
			for (int i=1; i<=5; i++) {
				for (int j=0; j<5; j++)
						paliChars[i][j] = "" + arrConsonants[(i-1)*5 + j];
			}
			if (script == Utilities.PaliScript.THAI && Boolean.parseBoolean(Utilities.settings.getProperty("thai-alt-chars"))) {
				// for Thai alternative chars
				paliChars[2][4] = "" + PaliCharTransformer.altPaliThaiChars[0];
				paliChars[3][1] = "" + PaliCharTransformer.altPaliThaiChars[1];
			} 
			for (int j=0; j<8; j++) 
				paliChars[6][j] = "" + arrConsonants[25+j];
			for (int j=1; j<10; j++)
				paliChars[7][j-1] = "" + arrNumbers[j];
			paliChars[7][9] = "" + arrNumbers[0];
		}
	}

	private void updateTagArray() {
		for (final Node nd : tagBox.getChildren()) {
			final TagType tt = (TagType)nd.getUserData();
			final Button bt = (Button)nd;
			bt.setText(epSwitchButton.isSelected() ? tt.paliName : tt.romanName);
		}
	}

	private void updateTagArray(final List<TagType> hiliteList) {
		for (final Node nd : tagBox.getChildren()) {
			final TagType tt = (TagType)nd.getUserData();
			final Button bt = (Button)nd;
			bt.getStyleClass().remove("button-highlight");
			if (hiliteList != null && hiliteList.contains(tt)) {
				bt.getStyleClass().add("button-highlight");
			}
		}
	}
	
	public void updateLetterDisplay(final int percent) {
		currFontPercent = percent==0 ? 100 : currFontPercent + percent;
		updateLetterDisplay();
	}

	private void updateLetterDisplay() {
		updateLetterDisplay(toolBar.getCurrFont());
	}

	private void updateLetterDisplay(final String fontname) {
		for (final Node stpn : letterGrid.getChildren()) {
			if (GridPane.getRowSpan(stpn) == 1) {
				for (final Node lbn : ((Pane)stpn).getChildren()) {
					((Label)lbn).setText(paliChars[GridPane.getRowIndex(stpn)][GridPane.getColumnIndex(stpn)]);
					lbn.setStyle("-fx-font-family:'"+ fontname +"';-fx-font-size:" + DEF_FONT_SCALE*currFontPercent + "%;");
				}
			} else {
				if (currSelectedPos[0] > -1)
					setBigChar(paliChars[currSelectedPos[0]][currSelectedPos[1]]);
			}
		}
	}

	private void highlightLetterDisplay(final List<int[]> posList) {
		for (final Node stpn : letterGrid.getChildren()) {
			final int[] posData = { GridPane.getRowIndex(stpn), GridPane.getColumnIndex(stpn) };
			stpn.getStyleClass().remove("letterbox-highlight");
			posList.forEach(x -> {
				if (Arrays.equals(posData, x))
					stpn.getStyleClass().add("letterbox-highlight");
			});
		}
	}

	private void showLetterHighlights(final ActionEvent event) {
		final Button bt = (Button)event.getSource();
		final TagType tt = (TagType)bt.getUserData();
		final List<int[]> posList = tt.getLetterPosList();
		highlightLetterDisplay(posList);
	}

	private void showTagHighlight(final MouseEvent event) {
		final StackPane stp = (StackPane)event.getSource();
		Integer row = GridPane.getRowIndex(stp);
		Integer col = GridPane.getColumnIndex(stp);
		currSelectedPos[0] = row;
		currSelectedPos[1] = col;
		final int[] posData = { row, col };
		final String letter = ((Label)stp.getChildren().get(0)).getText();
		if ((row==0 || row==6) && col<8 || (row>=1 && row<=5 && col<5)) {
			highlightLetterDisplay(List.of(posData));
			// show big char
			setBigChar(letter);
			// find corresponding tags
			final List<TagType> hiliteList = new ArrayList<>();
			for (final TagType tt : TagType.values) {
				final List<int[]> posList = tt.getLetterPosList();
				for (final int[] pos : posList) {
					if (Arrays.equals(pos, posData)) {
						hiliteList.add(tt);
						break;
					}
				}
			}
			updateTagArray(hiliteList);
		} else if (row==7) {
			// numbers
			setBigChar(letter);
			highlightLetterDisplay(List.of(posData));
			clearTagHighlights();
		}
	}

	private void setBigChar(final String ch) {
		selectedChar.set(ch);
		formatBigChar();
	}

	private void resizeBigChar(final int amount) {
		currBigCharSize += amount;
		if (currBigCharSize < 0)
			currBigCharSize = DEF_BIG_CHAR_SIZE;
		formatBigChar();
	}

	private void formatBigChar() {
		formatBigChar(toolBar.getCurrFont());
	}

	private void formatBigChar(final String fontname) {
		bigChar.setStyle("-fx-font-family:'"+ fontname +"';-fx-font-size:"+currBigCharSize+"%;");
	}

	private void clearBigChar() {
		currSelectedPos[0] = -1;
		currSelectedPos[1] = -1;
		selectedChar.set("");
	}

	private void clearTagHighlights() {
		// clear tag button array
		for (final Node nd : tagBox.getChildren()) {
			final Button bt = (Button)nd;
			bt.getStyleClass().remove("button-highlight");
		}
	}

	private void clearGridHighlights() {
		// clear letter array
		for (final Node stpn : letterGrid.getChildren()) {
			stpn.getStyleClass().remove("letterbox-highlight");
		}
	}

	private void openTypingTest() {
		if (mainPane.getBottom() == null)
			mainPane.setBottom(typingBox);
		else
			mainPane.setBottom(null);
	}

	private void setTypingOutput(final String text) {
		final String outText;
		switch (currPaliScript) {
			case DEVANAGARI: outText = PaliCharTransformer.romanToDevanagari(text); break;
			case KHMER: outText = PaliCharTransformer.romanToKhmer(text); break;
			case MYANMAR: outText = PaliCharTransformer.romanToMyanmar(text); break;
			case SINHALA: outText = PaliCharTransformer.romanToSinhala(text); break;
			case THAI: outText = PaliCharTransformer.romanToThai(text); break;
			default: outText = text;
		}
		typingOutput.setText(outText);
		formatTypingOutput();
	}

	private void setTypingOutput() {
		setTypingOutput(Normalizer.normalize(typingTextField.getText(), Form.NFC));
	}

	private void formatTypingOutput() {
		formatTypingOutput(toolBar.getCurrFont());
	}

	private void formatTypingOutput(final String fontname) {
		typingOutput.setStyle("-fx-font-family:'"+ fontname +"';-fx-font-size:200%;");
	}

	public void setFont(final String fontname) {
		updateLetterDisplay(fontname);
		formatBigChar(fontname);
		formatTypingOutput(fontname);
	}

	private String makeText() {
		final StringBuilder result = new StringBuilder();
		for (final TagType tt : TagType.values) {
			result.append(tt.romanName).append(" (").append(tt.paliName).append("): ");
			for (final int[] pos : tt.getLetterPosList())
				result.append(paliChars[pos[0]][pos[1]]).append(" ");
			result.append(System.getProperty("line.separator"));
		}
		return result.toString();
	}
	
	private void copyText() {
		Utilities.copyText(makeText());
	}
	
	private void saveText() {
		Utilities.saveText(makeText(), "letters.txt");
	}

}
