/*
 * SandhiAnalyzer.java
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

import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.input.*;
import javafx.scene.text.*;
import javafx.scene.Node;
import javafx.stage.Popup;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.*;

/**
 * The analyzer window of Sanskrit sandhi.
 * @author J.R. Bhaddacak
 * @version 4.1
 * @since 4.1
 */
public class SandhiAnalyzer extends BorderPane {
	private static final String LINESEP = System.getProperty("line.separator");
	private static final int DEF_INPUT_LIMIT = 100;
	private static final int DEF_TEXT_SIZE = 170;
	private final TextFlow analyzedText = new TextFlow();
	private final ObservableList<SandhiChunk> sandhiChunkList = FXCollections.<SandhiChunk>observableArrayList();
	private final ListView<SandhiChunk> sandhiChunkListView = new ListView<>(sandhiChunkList);
	private final TextArea resultArea = new TextArea();;
	private final InfoPopup infoPopup = new InfoPopup();
	private final CheckMenuItem includeVCMenuItem = new CheckMenuItem("Include vowel-consonant rules");
	private final CheckMenuItem includeVVMenuItem = new CheckMenuItem("Include vowel-vowel rules");
	private static final List<Sandhi> cxSandhiList = new ArrayList<>();
	private static final List<Sandhi> vcSandhiList = new ArrayList<>();
	private static final List<Sandhi> vvSandhiList = new ArrayList<>();
	private static final Map<String, List<Sandhi>> reverseSandhiMap = new HashMap<>();
	private int currAnalyzedTextSize = DEF_TEXT_SIZE;
	private final List<SandhiChunk> currSandhiChunkList = new ArrayList<>();;
	private String currText = "";

	public SandhiAnalyzer(final Object[] args) {
		// add common toolbar on the top
		final BorderPane resultPane = new BorderPane();
		final CommonWorkingToolBar toolBar = new CommonWorkingToolBar(resultPane);
		// configure some buttons first
		toolBar.saveTextButton.setTooltip(new Tooltip("Save data as text"));
		toolBar.saveTextButton.setOnAction(actionEvent -> saveText());		
		toolBar.copyButton.setTooltip(new Tooltip("Copy text data to clipboard"));
		toolBar.copyButton.setOnAction(actionEvent -> copyText());	
		// add new toolbar components
		final Button pasteButton = new Button("", new TextIcon("paste", TextIcon.IconSet.AWESOME));
		pasteButton.setTooltip(new Tooltip("Paste text to analyze"));
		pasteButton.setOnAction(actionEvent -> pasteText());
		final MenuButton optionsMenu = new MenuButton("", new TextIcon("check-double", TextIcon.IconSet.AWESOME));		
		optionsMenu.setTooltip(new Tooltip("Options"));
		includeVCMenuItem.setOnAction(actionEvent -> includeRulesSelected());
		includeVVMenuItem.setOnAction(actionEvent -> includeRulesSelected());
		final MenuItem includeAllMenuItem = new MenuItem("Include all");
		includeAllMenuItem.setOnAction(actionEvent -> includeAllRules(true));
		final MenuItem includeNoneMenuItem = new MenuItem("Include none");
		includeNoneMenuItem.setOnAction(actionEvent -> includeAllRules(false));
		optionsMenu.getItems().addAll(includeVCMenuItem, includeVVMenuItem, new SeparatorMenuItem(),
									includeAllMenuItem, includeNoneMenuItem);
		// add help button
		final Button helpButton = new Button("", new TextIcon("circle-question", TextIcon.IconSet.AWESOME));
		helpButton.setOnAction(actionEvent -> infoPopup.showPopup(helpButton, InfoPopup.Pos.BELOW_CENTER, true));
		toolBar.getItems().addAll(pasteButton, new Separator(), optionsMenu, helpButton);
		setTop(toolBar);

		// prepare text display pane
		final VBox textDisplayBox = new VBox();
		final ToolBar anaTextToolBar = new ToolBar();
		anaTextToolBar.setPadding(new Insets(2, 2, 2, 5));
		final Button minusButton = new Button("", new TextIcon("minus", TextIcon.IconSet.AWESOME));
		minusButton.setPadding(new Insets(2));
		minusButton.setOnAction(actionEvent -> {
			if (currAnalyzedTextSize > 120) {
				currAnalyzedTextSize -= 10;
				updateAnalyzedText();
			}
		});
		final Button resetButton = new Button("", new TextIcon("arrows-rotate", TextIcon.IconSet.AWESOME));
		resetButton.setPadding(new Insets(2));
		resetButton.setOnAction(actionEvent -> {
				currAnalyzedTextSize = DEF_TEXT_SIZE;
				updateAnalyzedText();
		});
		final Button plusButton = new Button("", new TextIcon("plus", TextIcon.IconSet.AWESOME));
		plusButton.setPadding(new Insets(2));
		plusButton.setOnAction(actionEvent -> {
			if (currAnalyzedTextSize < 300) {
				currAnalyzedTextSize += 10;
				updateAnalyzedText();
			}
		});
		final Button cleanButton = new Button("", new TextIcon("broom", TextIcon.IconSet.AWESOME));
		cleanButton.setTooltip(new Tooltip("Clear"));
		cleanButton.setPadding(new Insets(2));
		cleanButton.setOnAction(actionEvent -> cleanUp());
		final Button pasteButtonSmall = new Button("", new TextIcon("paste", TextIcon.IconSet.AWESOME));
		pasteButtonSmall.setTooltip(new Tooltip("Paste text"));
		pasteButtonSmall.setPadding(new Insets(2));
		pasteButtonSmall.setOnAction(actionEvent -> pasteText());
		anaTextToolBar.getItems().addAll(minusButton, resetButton, plusButton, cleanButton, pasteButtonSmall);
		final StackPane anaTextBox = new StackPane();
		anaTextBox.setPadding(new Insets(3));
		anaTextBox.getChildren().add(analyzedText);
		textDisplayBox.getChildren().addAll(anaTextToolBar, anaTextBox);
		// prepare result pane
		// chunk list on the left
		sandhiChunkListView.setPrefWidth(Utilities.getRelativeSize(8));
		sandhiChunkListView.setCellFactory((ListView<SandhiChunk> lv) -> {
			return new ListCell<SandhiChunk>() {
				@Override
				public void updateItem(SandhiChunk item, boolean empty) {
					super.updateItem(item, empty);
					this.setGraphic(null);
					if (empty) {
						this.setText(null);
						this.setTooltip(null);
					} else {
						final SandhiChunk value = this.getItem();
						this.setTooltip(new Tooltip(value.getCases()));
						this.setText(value.toString());
					}
					this.setStyle("-fx-padding: 0px 0px 0px 3px");
				}
			};
		});
		sandhiChunkListView.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> {
			final SandhiChunk selItem = newValue;
			if (selItem != null) {
				updateAnalyzedText();
				updateDecomposition(selItem);
			}
		});
		resultPane.setLeft(sandhiChunkListView);
		// result area on the center
		resultPane.setCenter(resultArea);

		final SplitPane mainSplitPane = new SplitPane();
		mainSplitPane.setOrientation(Orientation.VERTICAL);
		mainSplitPane.setDividerPositions(0.22);
		mainSplitPane.getItems().addAll(textDisplayBox, resultPane);
		setCenter(mainSplitPane);

		// prepare info popup
		infoPopup.setContentWithText(SanskritUtilities.getTextResource("info-sandhi-analyzer.txt"));
		infoPopup.setTextWidth(Utilities.getRelativeSize(32));		

		// some one-time initialization
		setPrefWidth(Utilities.getRelativeSize(48));
		setPrefHeight(Utilities.getRelativeSize(30));

		init(args);
	}

	public final void init(final Object[] args) {
		sandhiChunkListView.getSelectionModel().clearSelection();
		includeVCMenuItem.setSelected(false);
		includeVVMenuItem.setSelected(false);
		generateReverseSandhiMap();
		cleanUp();
		if (args != null) {
			final String starter = ((String)args[0]).trim();
			if (!starter.isEmpty())
				analyze(starter);
		}
	}

	private void generateAllSandhi() {
		if (!cxSandhiList.isEmpty() && !vcSandhiList.isEmpty() && !vvSandhiList.isEmpty())
			return;
		final List<String> firstWordList = new ArrayList<>();
		for (final String end : Sandhi.availEndings) {
			final List<String> firstWords = SandhiWin.getFirstWord(end, SandhiWin.OutputForm.BARE, SandhiWin.PrecedingVowel.ALL);
			firstWordList.addAll(firstWords);
		}
		final List<String> secondWordList = new ArrayList<>();
		for (final String beg : Sandhi.availBeginnings) {
			final String secondWord = SandhiWin.getSecondWord(beg, SandhiWin.OutputForm.BARE);
			secondWordList.add(secondWord);
		}
		for (final String secondWord : secondWordList) {
			for (final String firstWord : firstWordList) {
				final Sandhi sandhi = new Sandhi(firstWord, secondWord);
				if (!Sandhi.isVowel(firstWord)) {
					cxSandhiList.add(sandhi);
				} else {
					if (Sandhi.isVowel(secondWord))
						vvSandhiList.add(sandhi);
					else
						vcSandhiList.add(sandhi);
				}
			}
		}
	}

	private void generateReverseSandhiMap() {
		if (cxSandhiList.isEmpty() || vcSandhiList.isEmpty() || vvSandhiList.isEmpty()) 
			generateAllSandhi();
		reverseSandhiMap.clear();
		addSandhiRules(cxSandhiList);
		if (includeVCMenuItem.isSelected())
			addSandhiRules(vcSandhiList);
		if (includeVVMenuItem.isSelected())
			addSandhiRules(vvSandhiList);
	}

	private void addSandhiRules(final List<Sandhi> ruleList) {
		for (final Sandhi sandhi : ruleList) {
			final List<String> prodList = sandhi.getProductListRaw();
			for (final String prod : prodList) {
				final String prodOK = Sandhi.removeUnitSep(prod);
				final List<Sandhi> sandhiList = reverseSandhiMap.getOrDefault(prodOK, new ArrayList<>());
				sandhiList.add(sandhi);
				reverseSandhiMap.put(prodOK, sandhiList);
			}
		}
	}

	private void includeAllRules(final boolean isAll) {
		includeVCMenuItem.setSelected(isAll);
		includeVVMenuItem.setSelected(isAll);
		includeRulesSelected();
	}

	private void includeRulesSelected() {
		sandhiChunkListView.getSelectionModel().clearSelection();
		generateReverseSandhiMap();
		analyze();
	}

	private void cleanUp() {
		currText = "";
		resultArea.setText("");
		sandhiChunkList.clear();
		analyzedText.getChildren().clear();
	}

	private void pasteText() {
		final Clipboard cboard = Clipboard.getSystemClipboard();
		final String text = cboard.hasString() ? cboard.getString().trim() : "";
		sandhiChunkListView.getSelectionModel().clearSelection();
		resultArea.setText("");
		if (!text.isEmpty())
			analyze(text);
	}

	private void analyze(final String input) {
		final String text = input.length() < DEF_INPUT_LIMIT ? input : input.substring(0, DEF_INPUT_LIMIT);
		currText = Utilities.convertToRomanSanskrit(text);
		currText = currText.split("\\r?\\n")[0]; // use only the first line
		currText = currText.replaceAll("\\s", ""); // collapse spaces
		currText = Sandhi.convertAvagraha(currText); // convert ’ to avagraha
		analyze();
	}

	private void analyze() {
		currSandhiChunkList.clear();
		currSandhiChunkList.addAll(new SandhiUnit(currText).getChunkList());
		showAnalyzedText(currText);
		showSandhiChunks();
		sandhiChunkListView.getSelectionModel().select(0);
		sandhiChunkListView.scrollTo(0);
	}

	private void showAnalyzedText(final String text) {
		analyzedText.getChildren().clear();
		final char[] chArr = text.toCharArray();
		for (int i = 0; i < chArr.length; i++) {
			final String ch = chArr[i] + "";
			final TextUnit chUnit = new TextUnit(i, ch);
			analyzedText.getChildren().add(chUnit);
		}
		updateAnalyzedText();
	}

	private void updateAnalyzedText() {
		for (final Node node : analyzedText.getChildren()) {
			final TextUnit unit = (TextUnit)node;
			unit.updateDisplay();
		}
	}

	private boolean isInChunks(final int pos) {
		final long count = currSandhiChunkList.stream()
							.filter(x -> x.getRange().includes(pos))
							.count();
		return count > 0;
	}

	private void showSandhiChunks() {
		sandhiChunkList.clear();
		for (int i = 0; i < currSandhiChunkList.size(); i++) {
			final SandhiChunk chunk = currSandhiChunkList.get(i);
			sandhiChunkList.add(chunk);
		}
	}

	private void updateDecomposition(final SandhiChunk chunk) {
		final StringBuilder result = new StringBuilder();
		final String chunkText = chunk.getText();
		final List<Sandhi> sandhiList = reverseSandhiMap.get(chunkText);
		final Range chunkRange = chunk.getRange();
		final char[] chArr = currText.toCharArray();
		boolean replaced = false;
		int count = 0;
		for (final Sandhi sandhi : sandhiList) {
			count++;
			if (sandhiList.size() > 1)
				result.append("(" + count + ") ");
			for (int i = 0; i < chArr.length; i++) {
				if (chunkRange.includes(i) && replaced)
					continue;
				if (chunkRange.includes(i)) {
					result.append(sandhi.getFirst() + " " + sandhi.getSecond());
					replaced = true;
				} else {
					result.append(chArr[i]);
					replaced = false;
				}
			}
			result.append("\n");
		}
		resultArea.setText(Sandhi.formatAvagraha(result.toString()));
	}

	private String makeText() {
		final StringBuilder result = new StringBuilder();
		result.append(currText).append(LINESEP);
		final SandhiChunk chunk = sandhiChunkListView.getSelectionModel().getSelectedItem();
		if (chunk != null) {
			result.append("[" + chunk.toString() + "]").append(LINESEP);
			final String[] lines = resultArea.getText().split("\\r?\\n");
			for (final String line : lines)
				result.append(line).append(LINESEP);
		}
		return result.toString();
	}

	private void copyText() {
		Utilities.copyText(makeText());
	}
	
	private void saveText() {
		Utilities.saveText(makeText(), "sandhi_output.txt");
	}

	// inner classes
	static class Range {
		private final int start;
		private final int end;
		private int hashCode;
		public Range(final int s, final int e) {
			start = s;
			end = e;
		}
		public int getStart() {
			return start;
		}
		public int getEnd() {
			return end;
		}
		public boolean includes(final int pos) {
			return pos >= start && pos <= end;
		}
		public boolean isOverlappedWith(final Range other) {
			final int oStart = other.getStart();
			final int oEnd = other.getEnd();
			final boolean result = start <= oStart && end >= oEnd
									|| oStart <= start && oEnd >= end
									|| start >= oStart && start <= oEnd
									|| end >= oStart && end <= oEnd;
			return result;
		}
		@Override
		public boolean equals(final Object other) {
			final Range oRange = (Range)other;
			return start == oRange.getStart() && end == oRange.getEnd();
		}
		@Override
		public int hashCode() {
			int result = hashCode;
			if (result == 0) {
				result = Integer.hashCode(start);
				result = 31 * result + Integer.hashCode(end);
				hashCode = result;
			}
			return result;
		}
		@Override
		public String toString() {
			return "[" + start + "," + end + "]";
		}
	}

	class TextUnit extends Text {
		final int position;
		public TextUnit(final int pos, final String text) {
			super(text);
			position = pos;
		}
		public int getPosition() {
			return position;
		}
		public void updateDisplay() {
			getStyleClass().clear();
			setStyle("-fx-font-size:" + currAnalyzedTextSize + "%");
			final int selectedInd = sandhiChunkListView.getSelectionModel().getSelectedIndex();
			if (selectedInd > -1) {
				final SandhiChunk selected = currSandhiChunkList.get(selectedInd);
				if (selected != null && selected.includes(position)) {
					getStyleClass().add("search-result-highlight");
				} else {
					getStyleClass().add("reader-term");
				}
			} else {
				getStyleClass().add("reader-term");
			}
		}
	}

	static class SandhiChunk {
		private String text;
		private Range range;
		public SandhiChunk(final String txt, final Range ran) {
			text = txt;
			range = ran;
		}
		public String getText() {
			return text;
		}
		public Range getRange() {
			return range;
		}
		public boolean includes(final int pos) {
			return range.includes(pos);
		}
		public String getCases() {
			final List<Sandhi> sandhiList = reverseSandhiMap.getOrDefault(text, Collections.emptyList());
			return sandhiList.stream().map(Sandhi::toString).collect(Collectors.joining(", "));

		}
		@Override
		public String toString() {
			return Sandhi.formatAvagraha(text);
		}
		public String toStringFull() {
			return text + " " + range;
		}
	}

	static class SandhiUnit {
		final List<SandhiChunk> chunkList;
		public SandhiUnit(final String text) {
			final Map<Range, String> chunkMap = analyzeUnit(text);
			chunkList = chunkMap.keySet().stream()
										.sorted((x, y) -> Integer.compare(x.getStart(), y.getStart()))
										.map(r -> new SandhiChunk(chunkMap.get(r), r))
										.collect(Collectors.toList());

		}
		public List<SandhiChunk> getChunkList() {
			return chunkList;
		}
		private static Map<Range, String> analyzeUnit(final String text) {
			if (reverseSandhiMap.isEmpty())
				return Collections.emptyMap();
			final Map<Range, String> result = new HashMap<>();
			// find chunks' position
			int sPos;
			for_loop:
			for (final String k : reverseSandhiMap.keySet()) {
				sPos = text.indexOf(k);
				if (sPos == 0)
					sPos = text.indexOf(k, sPos + 1); // from the second position onward
				while (sPos > -1) {
					if (sPos + k.length() < text.length()) { // not at the end
						final String lastCh = k.substring(k.length() - 1);
						final String nextCh = sPos + k.length() < text.length() - 1 ? text.substring(sPos + k.length()) : "";
						if (Sandhi.sktHasH.indexOf(lastCh) > -1 && !nextCh.equals("h"))
							continue for_loop;
						final Range r = new Range(sPos, sPos + k.length() - 1);
						result.put(r, k);
					}
					sPos = text.indexOf(k, sPos + 1); //’
				}
			}
			return result;
		}
	}

}

