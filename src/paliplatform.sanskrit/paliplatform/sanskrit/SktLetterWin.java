/*
 * SktLetterWin.java
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

package paliplatform.sanskrit;

import paliplatform.base.*;
import paliplatform.base.ScriptTransliterator.EngineType;

import java.util.*;
import java.text.Normalizer;
import java.text.Normalizer.Form;

import javafx.scene.*;
import javafx.scene.text.Text;
import javafx.scene.image.*;
import javafx.scene.input.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.event.ActionEvent;
import javafx.geometry.Pos;
import javafx.geometry.Insets;
import javafx.beans.property.*;
import javafx.collections.ObservableList;

/** 
 * The window showing Sanskrit letters in various scripts.
 * This is a singleton.
 * @author J.R. Bhaddacak
 * @version 3.5
 * @since 3.5
 */
public final class SktLetterWin extends SingletonWindow {
	static enum TagType {
		VOWEL("vowel;svara"), SIMPLE("simple;śuddha"), COMPLEX("complex;saṃyukta"), SHORT("short;hrasva"), LONG("long;dīrgha"),
		CONSONANT("consonant;vyañjana"), GUTTURAL("guttural/velar;kaṇṭhya"), PALATAL("palatal;tālavya"),
		CEREBRAL("cerebral/retroflex;mūrdhanya"), DENTAL("dental;dantya"), LABIAL("labial;oṣṭhya"), NASAL("nasal;anunāsika"),
		VOICELESS("voiceless/hard;aghoṣa"), VOICED("voiced/soft;ghoṣavat"), UNASPIRATED("unaspirated;alpaprāṇa"), ASPIRATED("aspirated;mahāprāṇa"),
		SEMIVOWEL("semivowel;antaḥstha"), SIBILANT("sibilant/spirant;ūṣman"), ANUSVARA("anusvāra;anusvāra"), VISARGA("visarga;visarga"),
		AVAGRAHA("avagraha;avagraha");
		public static final TagType[] values = values();
		public final String engName;
		public final String sktName;
		private TagType(final String strName) {
			final String[] names = strName.split(";");
			engName = names[0];
			sktName = names[1];
		}
		public List<int[]> getLetterPosList() {
			int[][] array = null;
			switch (this) {
				case VOWEL:
					array = new int[][] {{0,7},{0,8},{1,7},{1,8},{1,9},{1,10},{2,7},{2,8},{3,7},{3,8},{4,7},{4,8},{4,9},{4,10}};
					break;
				case SIMPLE:
					array = new int[][] {{0,7},{0,8},{1,7},{1,8},{2,7},{2,8},{3,7},{3,8},{4,7},{4,8}};
					break;
				case COMPLEX:
					array = new int[][] {{1,9},{1,10},{4,9},{4,10}};
					break;
				case SHORT:
					array = new int[][] {{0,7},{1,7},{2,7},{3,7},{4,7}};
					break;
				case LONG:
					array = new int[][] {{0,8},{1,8},{1,9},{1,10},{2,8},{3,8},{4,8},{4,9},{4,10}};
					break;
				case CONSONANT:
					array = new int[][]{{0,0},{0,1},{0,2},{0,3},{0,4},{0,6},
										{1,0},{1,1},{1,2},{1,3},{1,4},{1,5},{1,6},
										{2,0},{2,1},{2,2},{2,3},{2,4},{2,5},{2,6},
										{3,0},{3,1},{3,2},{3,3},{3,4},{3,5},{3,6},
										{4,0},{4,1},{4,2},{4,3},{4,4},{4,5}};
					break;
				case GUTTURAL:
					array = new int[][] {{0,0},{0,1},{0,2},{0,3},{0,4},{0,6},{0,7},{0,8},{5,5}};
					break;
				case PALATAL:
					array = new int[][] {{1,0},{1,1},{1,2},{1,3},{1,4},{1,5},{1,6},{1,7},{1,8},{1,9},{1,10}};
					break;
				case CEREBRAL:
					array = new int[][] {{2,0},{2,1},{2,2},{2,3},{2,4},{2,5},{2,6},{2,7},{2,8}};
					break;
				case DENTAL:
					array = new int[][] {{3,0},{3,1},{3,2},{3,3},{3,4},{3,5},{3,6},{3,7},{3,8}};
					break;
				case LABIAL:
					array = new int[][] {{4,0},{4,1},{4,2},{4,3},{4,4},{4,5},{4,7},{4,8},{4,9},{4,10},{5,5}};
					break;
				case NASAL:
					array = new int[][] {{0,4},{1,4},{2,4},{3,4},{4,4},{5,4}};
					break;
				case VOICELESS:
					array = new int[][] {{0,0},{1,0},{2,0},{3,0},{4,0},{0,1},{1,1},{2,1},{3,1},{4,1},{1,6},{2,6},{3,6},{5,5}};
					break;
				case VOICED:
					array = new int[][]{{0,2},{0,3},{0,4},{0,6},{0,7},{0,8},
										{1,2},{1,3},{1,4},{1,5},{1,7},{1,8},{1,9},{1,10},
										{2,2},{2,3},{2,4},{2,5},{2,7},{2,8},
										{3,2},{3,3},{3,4},{3,5},{3,7},{3,8},
										{4,2},{4,3},{4,4},{4,5},{4,7},{4,8},{4,9},{4,10},
										{5,4}};
					break;
				case UNASPIRATED:
					array = new int[][]{{0,0},{0,2},{0,4},
										{1,0},{1,2},{1,4},{1,5},
										{2,0},{2,2},{2,4},{2,5},
										{3,0},{3,2},{3,4},{3,5},
										{4,0},{4,2},{4,4},{4,5}};
					break;
				case ASPIRATED:
					array = new int[][] {{0,1},{1,1},{2,1},{3,1},{4,1},{0,3},{1,3},{2,3},{3,3},{4,3}};
					break;
				case SEMIVOWEL:
					array = new int[][] {{1,5},{2,5},{3,5},{4,5}};
					break;
				case SIBILANT:
					array = new int[][] {{0,6},{1,6},{2,6},{3,6}};
					break;
				case ANUSVARA:
					array = new int[][] {{5,4}};
					break;
				case VISARGA:
					array = new int[][] {{5,5}};
					break;
				case AVAGRAHA:
					array = new int[][] {{5,7}};
					break;
				default:
					array = new int[][] {};
			}
			return array.length > 0 ? Arrays.asList(array) : Collections.emptyList();
		}
	}
	public static final SktLetterWin INSTANCE = new SktLetterWin();
	private static final double DEF_FONT_SCALE = 2.0;
	private final String[][] sktDevaChars;
	private final String[][] sktChars = new String[7][12];
	private final BorderPane mainPane = new BorderPane();
	private final SimpleStringProperty selectedChar = new SimpleStringProperty("");
	private final VBox tagBox = new VBox();
	private final GridPane letterGrid = new GridPane();
	private final CommonWorkingToolBar toolBar = new CommonWorkingToolBar(letterGrid);
	private final VBox typingBox = new VBox();
	private final Label typingOutput = new Label();
	private final ToggleGroup romanDefaultGroup = new ToggleGroup();
	private final TextField typingTextField;
	private final InfoPopup infoPopup = new InfoPopup();
	private final int[] currSelectedPos = { -1, -1 };
	private Utilities.PaliScript currPaliScript = Utilities.PaliScript.DEVANAGARI;
	private int currFontPercent = 100;
	private int currTagLang = 0; // 0 eng, 1 roman, 2 deva
	
	private SktLetterWin() {
		windowWidth = Utilities.getRelativeSize(52);
		windowHeight = Utilities.getRelativeSize(54);
		setTitle("Sanskrit Letters");
		getIcons().add(new Image(SktLetterWin.class.getResourceAsStream("resources/images/skt-letter.png")));
		sktDevaChars = ScriptTransliterator.getDevaSktLetterGrid();
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
		final Button tagLangSwitchButton = new Button("E/R/D");
		tagLangSwitchButton.setTooltip(new Tooltip("English/Roman Skt./Devanāgarī"));
		tagLangSwitchButton.setOnAction(actionEvent -> rotateTagLang());
		final Button cleanButton = new Button("", new TextIcon("broom", TextIcon.IconSet.AWESOME));
		cleanButton.setTooltip(new Tooltip("Clear highlights"));
		cleanButton.setOnAction(actionEvent -> {
			clearTagHighlights();
			clearGridHighlights();
		});
		final MenuButton convertMenu = new MenuButton("", new TextIcon("language", TextIcon.IconSet.AWESOME));
		final ToggleGroup scriptGroup = new ToggleGroup();
		for (final Utilities.PaliScript sc : Utilities.PaliScript.scripts){
			if (sc.ordinal() == 0) continue;
			final String n = sc.toString();
			final RadioMenuItem scriptItem = new RadioMenuItem(n.charAt(0) + n.substring(1).toLowerCase());
			scriptItem.setUserData(sc);
			scriptItem.setToggleGroup(scriptGroup);
			scriptItem.setSelected(sc == currPaliScript);
			convertMenu.getItems().add(scriptItem);
		}
        scriptGroup.selectedToggleProperty().addListener((observable) -> {
			if (scriptGroup.getSelectedToggle() != null) {
				final RadioMenuItem selected = (RadioMenuItem)scriptGroup.getSelectedToggle();
				final Utilities.PaliScript toScript = (Utilities.PaliScript)selected.getUserData();
				if (currPaliScript != toScript) {
					currPaliScript = toScript;
					setTypingOutput();
					fillCharacterArray(currPaliScript); 
					toolBar.setupFontMenu(currPaliScript);
					toolBar.resetFont(currPaliScript);
					updateLetterDisplay();
				}
			}
        });
		final Button typetestButton = new Button("", new TextIcon("keyboard", TextIcon.IconSet.AWESOME));
		typetestButton.setTooltip(new Tooltip("Typing test"));
		typetestButton.setOnAction(actionEvent -> openTypingTest());
		final MenuButton optionsMenu = new MenuButton("", new TextIcon("check-double", TextIcon.IconSet.AWESOME));		
		optionsMenu.setTooltip(new Tooltip("Options"));
		final Menu romanDefMenu = new Menu("Roman transliteration");
		for (final EngineType en : EngineType.engines) {
			if (en.getTargetScript() == Utilities.PaliScript.ROMAN) {
				final RadioMenuItem enItem = new RadioMenuItem(en.getNameShort());
				enItem.setUserData(en);
				enItem.setToggleGroup(romanDefaultGroup);
				romanDefMenu.getItems().add(enItem);
			}
		}
		romanDefaultGroup.selectToggle(romanDefaultGroup.getToggles().get(1));
		romanDefaultGroup.selectedToggleProperty().addListener(observable -> {
			if (currPaliScript == Utilities.PaliScript.ROMAN) {
				fillCharacterArray(currPaliScript); 
				updateLetterDisplay();
			}
		});
		optionsMenu.getItems().addAll(romanDefMenu);
		final Button helpButton = new Button("", new TextIcon("circle-question", TextIcon.IconSet.AWESOME));
		helpButton.setOnAction(actionEvent -> infoPopup.showPopup(helpButton, InfoPopup.Pos.BELOW_RIGHT, true));
		toolBar.getItems().addAll(new Separator(), tagLangSwitchButton, cleanButton, convertMenu,
									typetestButton, optionsMenu, helpButton);
		toolBar.setupFontMenu(currPaliScript);
		toolBar.resetFont(currPaliScript);
		mainPane.setTop(toolBar);

		// add main content
		final Scene scene = new Scene(mainPane, windowWidth, windowHeight);
		// add tag list on the left
		for (final TagType tt : TagType.values) {
			final Button a = new Button(tt.engName, new TextIcon("tag", TextIcon.IconSet.AWESOME));
			a.setPrefWidth(120);
			a.setStyle("-fx-alignment:center-left;");
			a.setTooltip(new Tooltip(tt.engName + " (" + tt.sktName + ")"));
			a.setUserData(tt);
			a.setOnAction(actionEvent ->  {
				updateTagArray(Arrays.asList(tt));
				showLetterHighlights(actionEvent);
			}); 
			tagBox.getChildren().add(a);
		}
		mainPane.setLeft(tagBox);
		
		// add letter table at the center
		final StackPane letterPane = new StackPane();
		letterGrid.setHgap(2);
		letterGrid.setVgap(2);
		letterGrid.setPadding(new Insets(2, 2, 2, 2));
		for (int rowInd=0; rowInd<sktChars.length; rowInd++) {
			for (int colInd=0; colInd<sktChars[rowInd].length; colInd++) {
				final String ch = sktChars[rowInd][colInd];
				if (ch != null && !ch.isEmpty()) {
					final StackPane stp = new StackPane();
					stp.prefWidthProperty().bind(letterPane.widthProperty().divide(11.0));
					stp.prefHeightProperty().bind(letterPane.heightProperty().divide(7.0));
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
		final AnchorPane typingOutputAnchorPane = new AnchorPane();
		final Button copyTypingButton = new Button("", new TextIcon("copy", TextIcon.IconSet.AWESOME));
		copyTypingButton.setTooltip(new Tooltip("Copy converted text"));
		copyTypingButton.setOnAction(actionEvent -> copyConvertedText());
		AnchorPane.setTopAnchor(typingOutput, 0.0);
		AnchorPane.setLeftAnchor(typingOutput, 0.0);
		AnchorPane.setTopAnchor(copyTypingButton, 0.0);
		AnchorPane.setRightAnchor(copyTypingButton, 0.0);
		typingOutputAnchorPane.getChildren().addAll(typingOutput, copyTypingButton);
		typingBox.setPadding(new Insets(3));
		typingBox.getChildren().addAll(typingInputBox, typingOutputAnchorPane);
		
		// prepare info popup
		infoPopup.setContentWithText(SanskritUtilities.getTextResource("info-sanskrit-letters.txt"));
		infoPopup.setTextWidth(Utilities.getRelativeSize(32));		

	} // end constructor
	
	private void fillCharacterArray(final Utilities.PaliScript script) {
		if (script == Utilities.PaliScript.DEVANAGARI) {
			for (int i = 0; i < sktDevaChars.length; i++) {
				for (int j = 0; j < sktDevaChars[i].length; j++) {
					sktChars[i][j] = sktDevaChars[i][j];
				}
			}
		} else {
			final EngineType romanDef = (EngineType)romanDefaultGroup.getSelectedToggle().getUserData();
			for (int i = 0; i < sktDevaChars.length; i++) {
				for (int j = 0; j < sktDevaChars[i].length; j++) {
					if (sktDevaChars[i][j] == null) continue;
					sktChars[i][j] = ScriptTransliterator.translitQuickSanskrit(sktDevaChars[i][j],
									Utilities.PaliScript.DEVANAGARI, script, romanDef, true);
				}
			}
		}
	}

	private void rotateTagLang() {
		currTagLang = (currTagLang + 1) % 3;
		for (final Node nd : tagBox.getChildren()) {
			final TagType tt = (TagType)nd.getUserData();
			final Button bt = (Button)nd;
			if (currTagLang == 0) {
				// eng
				bt.setText(tt.engName);
			} else if (currTagLang == 1) {
				// roman skt
				bt.setText(tt.sktName);
			} else {
				// deva
				final ObservableList<String> stlClass = bt.getStyleClass();
				bt.setText(ScriptTransliterator.translitQuickSanskrit(tt.sktName,
						Utilities.PaliScript.ROMAN, Utilities.PaliScript.DEVANAGARI, EngineType.DEVA_ROMAN_COMMON, true));
			}
		}
	}

	private void updateTagArray(final List<TagType> hiliteList) {
		for (final Node nd : tagBox.getChildren()) {
			final TagType tt = (TagType)nd.getUserData();
			final Button bt = (Button)nd;
			bt.getStyleClass().remove("button-highlight-slim");
			if (hiliteList != null && hiliteList.contains(tt)) {
				bt.getStyleClass().add("button-highlight-slim");
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
			for (final Node lbn : ((Pane)stpn).getChildren()) {
				((Label)lbn).setText(sktChars[GridPane.getRowIndex(stpn)][GridPane.getColumnIndex(stpn)]);
				lbn.setStyle("-fx-font-family:'"+ fontname +"';-fx-font-size:" + DEF_FONT_SCALE*currFontPercent + "%;");
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
		if (row < 7) {
			highlightLetterDisplay(List.of(posData));
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
		} else {
			// numbers
			highlightLetterDisplay(List.of(posData));
			clearTagHighlights();
		}
	}

	private void clearTagHighlights() {
		// clear tag button array
		for (final Node nd : tagBox.getChildren()) {
			final Button bt = (Button)nd;
			bt.getStyleClass().remove("button-highlight-slim");
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
			case DEVANAGARI:
				outText = ScriptTransliterator.transliterate(text, EngineType.ROMAN_SKT_DEVA, true);
				break;
			case KHMER:
				outText = ScriptTransliterator.transliterate(text, EngineType.ROMAN_SKT_DEVA, EngineType.DEVA_KHMER, true);
				break;
			case MYANMAR:
				outText = ScriptTransliterator.transliterate(text, EngineType.ROMAN_SKT_DEVA, EngineType.DEVA_MYANMAR, true);
				break;
			case SINHALA:
				outText = ScriptTransliterator.transliterate(text, EngineType.ROMAN_SKT_DEVA, EngineType.DEVA_SINHALA, true);
				break;
			case THAI:
				outText = ScriptTransliterator.transliterate(text, EngineType.ROMAN_SKT_DEVA, EngineType.DEVA_THAI, true);
				break;
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
		formatTypingOutput(fontname);
	}

	private String makeText() {
		final StringBuilder result = new StringBuilder();
		for (final TagType tt : TagType.values) {
			result.append(tt.engName).append(" (").append(tt.sktName).append("): ");
			for (final int[] pos : tt.getLetterPosList())
				result.append(sktChars[pos[0]][pos[1]]).append(" ");
			result.append(System.getProperty("line.separator"));
		}
		return result.toString();
	}
	
	private void copyText() {
		Utilities.copyText(makeText());
	}
	
	private void saveText() {
		Utilities.saveText(makeText(), "sktletters.txt");
	}

	private void copyConvertedText() {
		final Clipboard clipboard = Clipboard.getSystemClipboard();
		final ClipboardContent content = new ClipboardContent();
		content.putString(typingOutput.getText());
		clipboard.setContent(content);
	}
	
}
