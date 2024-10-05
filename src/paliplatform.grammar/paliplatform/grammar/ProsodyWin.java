/*
 * ProsodyWin.java
 *
 * Copyright (C) 2023 J. R. Bhaddacak 
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
import java.util.stream.*;
import java.nio.charset.StandardCharsets;

import javafx.scene.control.*;
import javafx.scene.control.cell.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.text.*;
import javafx.scene.layout.*;
import javafx.scene.input.*;
import javafx.scene.Node;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.beans.property.*;
import javafx.beans.value.ChangeListener;
import javafx.geometry.*;

/** 
 * Prosody window analyzes prosodic patterns of a selected stanza.
 * @author J.R. Bhaddacak
 * @version 3.0
 * @since 2.0
 */
public final class ProsodyWin extends BorderPane {
	private static enum IncompleteType {
		NONE(""), INCOMPELTE("incomplete"), OVERREQUIRED("over-required");
		public final String description;
		private IncompleteType(final String desc) {
			description = desc;
		}
	}
	private static final int MAX_LINE_LENGTH = 64;
	private static final String[] gathaType = { "Mattāvutti", "Vaññavutti" };
	private static final String[][] gathaSubType = { {"Ariyā", "Gīti", "Vetālīya", "Mattāsamaka"},
													 {"Samavutti", "Aḍḍhasamavutti", "Visamavutti"} };
	private static final List<Gatha> gathaList = new ArrayList<>();
	private final VBox mainPane = new VBox();
	private final TableView<ProsodyOutput> table = new TableView<>();	
	private final ObservableList<ProsodyOutput> outputList = FXCollections.<ProsodyOutput>observableArrayList();
	private final VBox resultBox = new VBox();
	private final VBox editBox = new VBox();
	private final AnchorPane statusPane = new AnchorPane();
	private final Label formulaText = new Label();
	private final Label incompleteText = new Label();
	private final InfoPopup infoPopup = new InfoPopup();
	private final ChangeListener<String> areaListener;
	private final TextArea editArea;
	private final Button inputMethodButton;
	private String[] stanza;
	private String[] glPatterns; // garu lahu pattern

	public ProsodyWin(final Object[] args) {
		final PaliTextInput textInput = new PaliTextInput(PaliTextInput.InputType.AREA);
		inputMethodButton = textInput.getMethodButton();
		final SimpleListProperty<Node> panesProp = new SimpleListProperty<>(mainPane.getChildren());
		inputMethodButton.disableProperty().bind(panesProp.sizeProperty().isNotEqualTo(3));
        
		// add toolbar on the top
		final CommonWorkingToolBar toolBar = new CommonWorkingToolBar(mainPane);
		// config some buttons
		toolBar.saveTextButton.setOnAction(actionEvent -> saveText());		
		toolBar.copyButton.setOnAction(actionEvent -> copyText());		
		// add new components
		final Button editButton = new Button("", new TextIcon("pen-fancy", TextIcon.IconSet.AWESOME));
		editButton.setTooltip(new Tooltip("Edit mode on/off"));
		editButton.setOnAction(actionEvent -> toggleEditMode());
		final Button analyzeButton = new Button("Analyze");
		analyzeButton.setOnAction(actionEvent -> analyze());
		final Button resetButton = new Button("Reset");
		resetButton.setOnAction(actionEvent -> reset());
		final Button helpButton = new Button("", new TextIcon("circle-question", TextIcon.IconSet.AWESOME));
		helpButton.setOnAction(actionEvent -> infoPopup.showPopup(helpButton, InfoPopup.Pos.BELOW_RIGHT, true));
	
		toolBar.getItems().addAll(new Separator(), editButton, inputMethodButton, new Separator(), analyzeButton, resetButton, helpButton);
		setTop(toolBar);

		// add status bar at the bottom
		final StackPane formulaStatus = new StackPane();
		formulaStatus.setPadding(new Insets(2));
		formulaText.setStyle("-fx-font-family:'" + Utilities.FONTMONO +"';");
		formulaStatus.getChildren().add(formulaText);
		final StackPane incompleteStatus = new StackPane();
		incompleteStatus.setPadding(new Insets(2));
		incompleteText.setStyle("-fx-font-family:'" + Utilities.FONTMONO +"';");
		incompleteStatus.getChildren().add(incompleteText);
		AnchorPane.setBottomAnchor(formulaStatus, 0.0);
		AnchorPane.setLeftAnchor(formulaStatus, 0.0);
		AnchorPane.setBottomAnchor(incompleteStatus, 0.0);
		AnchorPane.setRightAnchor(incompleteStatus, 0.0);
		statusPane.getChildren().addAll(formulaStatus, incompleteStatus);
		setBottom(statusPane);

		// add table and analyzed result at the center
		final MenuItem openRefMenuItem = new MenuItem("Open Ref.");
		openRefMenuItem.setOnAction(actionEvent -> openRef());		
		final ContextMenu popupMenu = new ContextMenu(openRefMenuItem);
		table.setContextMenu(popupMenu);
		table.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> {
			final ProsodyOutput selItem = newValue;
			if (selItem != null)
				showGathaRule(selItem);
		});

		VBox.setVgrow(table, Priority.ALWAYS);
		mainPane.getChildren().add(table);
		setCenter(mainPane);

		// set up drop event
		this.setOnDragOver(dragEvent -> {
			if (dragEvent.getGestureSource() != this && dragEvent.getDragboard().hasString())
				dragEvent.acceptTransferModes(TransferMode.COPY_OR_MOVE);
			dragEvent.consume();
		});
		this.setOnDragDropped(dragEvent -> {
			final Dragboard db = dragEvent.getDragboard();
			if (db.hasString()) {
				final String text = db.getString();
				analyze(text.trim());
				dragEvent.setDropCompleted(true);
			} else {
				dragEvent.setDropCompleted(false);
			}
			dragEvent.consume();
		});

		// init
		setPrefWidth(Utilities.getRelativeSize(50));
		setPrefHeight(Utilities.getRelativeSize(44));
		loadGathaList();
		table.setItems(outputList);
		resultBox.getStyleClass().add("stanzabox");
		editArea = (TextArea) textInput.getInput();
		editArea.setWrapText(false);
		editArea.setPrefRowCount(2);
		editBox.getChildren().add(editArea);
		areaListener = (ob, oldV, newV) -> analyze(editArea.getText());
		if (args == null) {
			reset();
		} else {
			final String text = (String)args[0];
			analyze(text);
			editArea.setText(text);
		}
		// prepare info popup
		infoPopup.setContentWithText(GrammarUtilities.getTextResource("info-prosody.txt"));
		infoPopup.setTextWidth(Utilities.getRelativeSize(35));
	}

	private void loadGathaList() {
		if (!gathaList.isEmpty())
			return;
		try (final Scanner in = new Scanner(ProsodyWin.class.getResourceAsStream(GrammarUtilities.PROSODY), StandardCharsets.UTF_8)) {
			int ind = 0;
			while (in.hasNextLine()) {
				final String line = in.nextLine().trim();
				if (line.charAt(0) == '#')
					continue;
				final String[] items = line.split(":");
				if (items.length >= 4) {
					final String name = items[0];
					final String type = items[1];
					final int mnum = Integer.parseInt(items[2]);
					final String rule = items[3];
					final Gatha gatha = new Gatha(ind, name, type, mnum, rule);
					if (items.length == 6) {
						gatha.setRefId(items[4]);
						gatha.setRefParaNum(Integer.parseInt(items[5]));
					}
					gathaList.add(gatha);
					ind++;
				}
			}
		}
	}
	
	private void reset() {
		stanza = null;
		glPatterns = null;
		resetGathaHitScore();
		final List<Integer> shownGathaId = new ArrayList<>();
		for (final Gatha g : gathaList)
			shownGathaId.add(g.getID());
		showGathaList(shownGathaId);
		editArea.textProperty().removeListener(areaListener);
		editArea.clear();
		resultBox.getChildren().clear();
		mainPane.getChildren().clear();
		mainPane.getChildren().add(table);
		formulaText.setText("");
		incompleteText.setText("");
	}

	private void resetGathaHitScore() {
		for (int i=0; i<gathaList.size(); i++) {
			gathaList.get(i).setHitScore(0.0);
		}
	}

	private void showGathaList(final List<Integer> shownId) {
		outputList.clear();
		for (final Integer id : shownId) {
			final Gatha gatha = gathaList.get(id);
			final ProsodyOutput pout = new ProsodyOutput(gatha);
			outputList.add(pout);
		}
		setupTable();
	}

	private void setupTable() {
		final TableColumn<ProsodyOutput, Integer> idCol = new TableColumn<>("ID");
		idCol.setCellValueFactory(new PropertyValueFactory<>(outputList.get(0).idProperty().getName()));
		idCol.prefWidthProperty().bind(mainPane.widthProperty().divide(13));
		idCol.setStyle("-fx-alignment:center-right");
		final TableColumn<ProsodyOutput, String> nameCol = new TableColumn<>("Name");
		nameCol.setCellValueFactory(new PropertyValueFactory<>(outputList.get(0).nameProperty().getName()));
		nameCol.setComparator(Utilities.paliComparator);
		nameCol.prefWidthProperty().bind(mainPane.widthProperty().divide(13).multiply(4));
		final TableColumn<ProsodyOutput, String> typeCol = new TableColumn<>("Type");
		typeCol.setCellValueFactory(new PropertyValueFactory<>(outputList.get(0).typeProperty().getName()));
		typeCol.setComparator(Utilities.paliComparator);
		typeCol.prefWidthProperty().bind(mainPane.widthProperty().divide(13).multiply(4));
		final TableColumn<ProsodyOutput, String> refCol = new TableColumn<>("Ref.");
		refCol.setCellValueFactory(new PropertyValueFactory<>(outputList.get(0).refProperty().getName()));
		refCol.prefWidthProperty().bind(mainPane.widthProperty().divide(13).multiply(2).subtract(10));
		final TableColumn<ProsodyOutput, Double> scoreCol = new TableColumn<>("Score");
		scoreCol.setCellValueFactory(new PropertyValueFactory<>(outputList.get(0).scoreProperty().getName()));
		scoreCol.prefWidthProperty().bind(mainPane.widthProperty().divide(13).multiply(2).subtract(10));
		scoreCol.setStyle("-fx-alignment:center-right");
		scoreCol.setCellFactory(col -> {
			TableCell<ProsodyOutput, Double> cell = new TableCell<ProsodyOutput, Double>() {
				@Override
				public void updateItem(final Double item, final boolean empty) {
					super.updateItem(item, empty);
					this.setText(null);
					this.setGraphic(null);
					if (!empty) {
						this.setText(String.format("%6.4f", item));
					}
				}
			};
			return cell;
		});
		table.getColumns().clear();
		table.getColumns().add(idCol);
		table.getColumns().add(nameCol);
		table.getColumns().add(typeCol);
		table.getColumns().add(refCol);	
		table.getColumns().add(scoreCol);	
	}
	
	private void showGathaRule(final ProsodyOutput pout) {
		final Gatha gt = gathaList.stream().filter(x -> x.getID()==pout.idProperty().get()).findFirst().get();
		if (gt != null) {
			final String rule = gt.getRule();
			formulaText.setText(rule + " (" + gt.getMeasureNum() + ")");
			incompleteText.setText(gt.getIncomplete().description);
			if (glPatterns == null)
				return;
			final char RIGHT = '\u2713';
			final char WRONG = '\u2717';
			// show the result of analysis
			final List<List<List<Boolean>>> hits = testGatha(glPatterns, gt);
			for (int i = 0; i < glPatterns.length; i++) {
				final StringBuilder checkedPatt = new StringBuilder(glPatterns[i].length());
				for (final List<Boolean> lst : hits.get(i)) {
					for (final Boolean b : lst) {
						if (b)
							checkedPatt.append(RIGHT);
						else
							checkedPatt.append(WRONG);
					}
				} // end for
				final int linenum = i*3 + 2;
				final StringBuilder result = new StringBuilder(stanza[linenum-1].length());
				int count = 0;
				for (int j = 0; j < stanza[linenum-1].length(); j++) {
					if (stanza[linenum-1].charAt(j) == ' ') {
						result.append(' ');
					} else {
						result.append(checkedPatt.charAt(count));
						count++;
					}
					if (count >= checkedPatt.length())
						break;
				} // end for
				stanza[linenum] = result.toString();
			} //  end for
			showSpecimen();
		}
	}

	private void analyze() {
		final Clipboard cboard = Clipboard.getSystemClipboard();
		final String text = cboard.hasString() ? cboard.getString().trim() : "";
		if (text.isEmpty()) {
			final Alert alert = new Alert(AlertType.INFORMATION);
			alert.initOwner(this.getScene().getWindow());
			alert.setHeaderText(null);
			alert.setContentText("There is no text to be analyzed.\nPlease copy some text to clipboard first.");
			alert.showAndWait();
		} else {
			analyze(text);
			editArea.setText(text);
		}
	}

	public void analyze(final String text) {
		final String[] input = text.split("\\n");
		final List<String> specimens = new ArrayList<>();
		for (final String s : input) {
			if (!s.trim().isEmpty()) {
				specimens.add(s.trim());
				// use only 2 lines of input or less
				if (specimens.size() >= 2)
					break;
			}
		}
		final int lcount = specimens.size();
		stanza = new String[lcount*3];
		glPatterns = new String[lcount];
		for (int i=0; i<lcount; i++) {
			String line = specimens.get(i);
			if (line.length() > MAX_LINE_LENGTH) {
				// if the line is too long, truncate it
				// (it is not supposed to be a verse)
				line = line.substring(0, MAX_LINE_LENGTH);
			}
			// 1st line is the text
			stanza[i*3] = line;
			// 2rd line is meter pattern
			glPatterns[i] = Utilities.computeMeter(line, true);
			final StringBuilder rawPattern = new StringBuilder();
			int ind = 0;
			for (int j = 0; j < line.length(); j++) {
				if (Utilities.isVowel(line.charAt(j))) {
					rawPattern.append(glPatterns[i].charAt(ind));
					ind++;
				} else {
					rawPattern.append(' ');
				}
			} // end for
			final int meterSum = Utilities.sumMeter(glPatterns[i]);
			rawPattern.append(" (").append(glPatterns[i].length()).append("/").append(meterSum).append(")");
			stanza[i*3+1] = rawPattern.toString();
			// 3nd line is meter testing result
			// show nothing at first
			stanza[i*3 + 2] = "";
		} //  end for

		resetGathaHitScore();
		findGathaHitScore(glPatterns);
		final List<Integer> orderedListByScore = gathaList.stream()
												.sorted((x, y) -> Double.compare(y.getHitScore(), x.getHitScore()))
												.map(Gatha::getID)
												.collect(Collectors.toList());
		showGathaList(orderedListByScore);
		showSpecimen();
		formulaText.setText("");
		incompleteText.setText("");
	}

	private void showSpecimen() {
		resultBox.getChildren().clear();
		for (final String s : stanza) {
			final Label lbl = new Label(s);
			lbl.setFont(Font.font(Utilities.FONTMONO, FontWeight.MEDIUM, Utilities.getRelativeSize(1.2)));
			resultBox.getChildren().add(lbl);
		}
		if (mainPane.getChildren().size() == 1) {
			mainPane.getChildren().clear();
			mainPane.getChildren().addAll(resultBox, table);
		}
	}

	private void toggleEditMode() {
		final List<Node> boxes = new ArrayList<>(mainPane.getChildren());
		final int bcount = boxes.size();
		mainPane.getChildren().clear();
		editArea.textProperty().removeListener(areaListener);
		if (bcount == 1) {
			editArea.textProperty().addListener(areaListener);
			mainPane.getChildren().addAll(resultBox, editBox, table);
		} else if (bcount == 2) {
			editArea.textProperty().addListener(areaListener);
			mainPane.getChildren().addAll(resultBox, editBox, table);
		} else if (bcount == 3) {
			if (resultBox.getChildren().isEmpty())
				mainPane.getChildren().addAll(table);
			else
				mainPane.getChildren().addAll(resultBox, table);
		}
	}

	private void findGathaHitScore(final String[] patterns) {
		final int inputMeterSum = Arrays.stream(patterns).map(x -> Utilities.sumMeter(x)).reduce(0, Integer::sum);
		final int inputSyllableSum = Arrays.stream(patterns).map(x -> x.length()).reduce(0, Integer::sum);
		for (int i = 0; i<gathaList.size(); i++) {
			final Gatha gatha = gathaList.get(i);
			final String rule = gatha.getRule();
			final List<List<List<Boolean>>> hits = testGatha(patterns, gatha);
			final int sum = gatha.getMeasureNum();
			final int lineNeeded = rule.contains(";") ? 2 : 1;
			final int multiplier = patterns.length > lineNeeded ? patterns.length/lineNeeded : 1;
			final int totalSum = sum * multiplier;
			final int inputSum = gatha.getType().charAt(0) == 'm' ? inputMeterSum : inputSyllableSum;
			gatha.setIncomplete(IncompleteType.NONE);
			if (patterns.length < lineNeeded)
				gatha.setIncomplete(IncompleteType.INCOMPELTE);
			else if (totalSum < inputSum)
				gatha.setIncomplete(IncompleteType.OVERREQUIRED);
			final long hit;
			if (gatha.getType().charAt(0) == 'm') {
				// mattavutti type, sum the hit weight
				int wSum = 0;
				for (int j = 0; j < patterns.length; j++) {
					final String patt = patterns[j];
					final List<Boolean> hitList = hits.get(j).stream().flatMap(Collection::stream).collect(Collectors.toList());
					for (int k = 0; k < patt.length(); k++) {
						if (k < hitList.size() && hitList.get(k))
							wSum += patt.charAt(k) - '0';
					}
				}
				hit = wSum;
			} else {
				// vannavutti type, just count the hit syllables
				hit = hits.stream()
							.flatMap(Collection::stream)
							.flatMap(Collection::stream)
							.filter(x -> x)
							.count();
			}
			final double score = (double)hit / totalSum;
			gatha.setHitScore(score);
		}
	}

	private List<List<List<Boolean>>> testGatha(final String[] patterns, final Gatha gatha) {
		final String rule = gatha.getRule();
		final String[] rules;
		if (patterns.length > 1) {
			// if input has 2 lines
			if (rule.contains(";")) {
				// if rule has 2 lines also, split it
				rules = rule.split(";");
			} else {
				// if rule has only 1 line, duplicate it
				rules = new String[2];
				rules[0] = rule;
				rules[1] = rule;
			}
		} else {
			// if input has only 1 line
			rules = new String[1];
			if (rule.contains(";")) {
				// if rule has 2 lines, use only the first half (marked as incomplete)
				rules[0] = rule.split(";")[0];
			} else {
				// 1 input line per 1 rule
				rules[0] = rule;
			}
		}
		final List<List<List<Boolean>>> hitsOfPatterns = new ArrayList<>();
		for (int p = 0; p < patterns.length; p++) {
			final String[] elements = rules[p].split("-");
			final List<List<Boolean>> hitsOfRule = new ArrayList<>();
			int ind = 0;
			for (int i = 0; i < elements.length; i++) {
				final List<Boolean> hitsOfElements = testGathaRule(patterns[p], ind, elements[i], gatha);
				hitsOfRule.add(hitsOfElements);
				ind += hitsOfElements.size();
				if (ind >= patterns[p].length())
					break;
			}
			hitsOfPatterns.add(hitsOfRule);
		}
		return hitsOfPatterns;
	}

	private List<Boolean> testGathaRule(final String pattern, final int index, final String ruleElement, final Gatha gatha) {
		final List<Boolean> hits = new ArrayList<>();
		boolean hit = false;
		if (ruleElement.length() == 1) {
			// simple rule indicated by a single character
			final char r = ruleElement.charAt(0);
			if (Character.isDigit(r)) {
				// counted by meter, so sylables may vary
				final int meterCount = r - '0';
				boolean specialTestPassed = true;
				if (gatha.getRefParaNum() == 29 && meterCount == 8) {
					// special case for Vetālīya (Vut.29), no 6 successive lahus in even feet
					final String part = pattern.substring(index);
					if (part.contains("111111"))
						specialTestPassed = false;
				}
				int sum = 0;
				int count = 0;
				for (int i = index; i < pattern.length(); i++) {
					sum += pattern.charAt(i) - '0';
					count++;
					if (sum == meterCount) {
						final boolean res = specialTestPassed;
						for (int j = 0; j < count; j++)
							hits.add(res);
						break;
					} else if (sum > meterCount) {
						count--;
						break;
					}
				}
				if (sum != meterCount) {
					for (int j = 0; j < count; j++)
						hits.add(false);
				}
			} else {
				// counted by syllable, so length is fixed 
				final int sylCount = getSyllableCount(r);
				if (index <= pattern.length() - sylCount) {
					hit = testRulePattern(pattern.substring(index, index+sylCount), ruleElement);
					for (int i=0; i<sylCount; i++)
						hits.add(hit);
				} else {
					for (int i=index; i<pattern.length(); i++)
						hits.add(false);
				}
			}
		} else {
			// complex rule
			final Set<Character> ruleSet = new HashSet<>();
			if (ruleElement.charAt(0) == '!') {
				// with negation
				boolean isMatta = false;
				final Set<Character> notThese = new HashSet<>();
				for (int i=1; i<ruleElement.length(); i++) {
					isMatta = Character.isLowerCase(ruleElement.charAt(i));
					notThese.add(ruleElement.charAt(i));
				}
				if (isMatta)
					ruleSet.addAll(Arrays.asList('m', 's', 'j', 'b', 'n'));
				else
					ruleSet.addAll(Arrays.asList('N', 'S', 'J', 'Y', 'B', 'R', 'T', 'M'));
				ruleSet.removeAll(notThese);
			} else if (ruleElement.contains("|")) {
				// with OR operation
				for (int i = 0; i < ruleElement.length(); i++) {
					if (ruleElement.charAt(i) != '|')
						ruleSet.add(ruleElement.charAt(i));
				}
			}
			final Iterator<Character> it = ruleSet.iterator();
			while (it.hasNext()) {
				final Character ch = it.next();
				final int sylCount = getSyllableCount(ch);
				hit = false;
				if (index <= pattern.length() - sylCount) {
					hit = testRulePattern(pattern.substring(index, index+sylCount), ""+ch);
					if (hit) {
						for (int i = 0; i < sylCount; i++)
							hits.add(true);
						break;
					}
				}
			}
			if (!hit) {
				final int end = index <= pattern.length()-3 ? index+3 : pattern.length();
				for (int i = index; i < end; i++)
					hits.add(false);
			}
		}
		return hits;
	}

	private int getSyllableCount(final char rule) {
		final int result;
		if ("1lg".indexOf(rule) >= 0) {
			result = 1;
		} else if (rule == 'm') {
			result = 2;
		} else if ("NSJYBRTMsjb".indexOf(rule) >= 0) {
			result = 3;
		} else if (rule == 'n') {
			result = 4;
		} else if (rule == 'L') {
			result = 14;
		} else {
			result = 0;
		}
		return result;
	}

	private boolean testRulePattern(final String input, final String rulePattern) {
		boolean result = false;
		if (rulePattern.equals("l") || rulePattern.equals("1")) {
			if (input.charAt(0) == '1')
				result = true;
		} else if (rulePattern.equals("g")) {
			if (input.charAt(0) == '2')
				result = true;
		} else if (rulePattern.equals("d")) {
			// double lahu
			if (input.equals("11"))
				result = true;
		} else if (rulePattern.equals("N")) {
			if (input.equals("111"))
				result = true;
		} else if (rulePattern.toUpperCase().equals("S")) {
			if (input.equals("112"))
				result = true;
		} else if (rulePattern.toUpperCase().equals("J")) {
			if (input.equals("121"))
				result = true;
		} else if (rulePattern.equals("Y")) {
			if (input.equals("122"))
				result = true;
		} else if (rulePattern.toUpperCase().equals("B")) {
			if (input.equals("211"))
				result = true;
		} else if (rulePattern.equals("R")) {
			if (input.equals("212"))
				result = true;
		} else if (rulePattern.equals("T")) {
			if (input.equals("221"))
				result = true;
		} else if (rulePattern.equals("M")) {
			if (input.equals("222"))
				result = true;
		} else if (rulePattern.equals("n")) {
			if (input.equals("1111"))
				result = true;
		} else if (rulePattern.equals("m")) {
			if (input.equals("22"))
				result = true;
		} else if (rulePattern.equals("L")) {
			// 14 lahus
			if (input.equals("11111111111111"))
				result = true;
		}
		return result;
	}

	private void openRef() {
		final ProsodyOutput pdout = table.getSelectionModel().getSelectedItem();
		if (pdout != null) {
			final String refId = pdout.getGatha().getRefId().toLowerCase();
		if (GrammarUtilities.readerServiceImp != null)
			GrammarUtilities.readerServiceImp.openDocument("GRAM", refId);
		}
	}

	private String makeText() {
		final StringBuilder result = new StringBuilder();
		if (stanza != null) {
			for (final String s : stanza) {
				result.append(s).append(System.getProperty("line.separator"));
			}
		}
		final String rule = formulaText.getText();
		if (!rule.isEmpty()) {
			final ProsodyOutput selected = table.getSelectionModel().getSelectedItem();
			result.append(selected.idProperty().get()).append(" ").append(selected.nameProperty().get()).append(" = ");
			result.append(rule).append(System.getProperty("line.separator"));
			result.append(System.getProperty("line.separator"));
		}
		for (int i=0; i<table.getColumns().size(); i++){
			result.append(table.getColumns().get(i).getText()).append(Utilities.csvDelimiter);
		}
		result.append(System.getProperty("line.separator"));
		for (int i=0; i<table.getItems().size(); i++){
			final ProsodyOutput pout = table.getItems().get(i);
			final Integer id = pout.idProperty().get();
			result.append(id.toString()).append(Utilities.csvDelimiter);
			final String name = pout.nameProperty().get();
			result.append(name).append(Utilities.csvDelimiter);
			final String type = pout.typeProperty().get();
			result.append(type).append(Utilities.csvDelimiter);
			final String ref = pout.refProperty().get();
			result.append(ref).append(Utilities.csvDelimiter);
			final Double score = pout.scoreProperty().get();
			result.append(String.format("%6.4f", score)).append(Utilities.csvDelimiter);
			result.append(System.getProperty("line.separator"));
		}
		return result.toString();
	}
	
	private void copyText() {
		Utilities.copyText(makeText());
	}
	
	private void saveText() {
		Utilities.saveText(makeText(), "prosody.txt");
	}
	
	// inner classes
	public class Gatha {
		private final int id;
		private final String name;
		private final String type;
		private final int measureNum;
		private final String rule;
		private String refId;
		private int refParaNum;
		private double hitScore;
		private IncompleteType incompleteState;
		
		private Gatha(final int id, final String name, final String type, final int mnum, final String rule) {
			this.id = id;
			this.name = name;
			this.type = type;
			this.measureNum = mnum;
			this.rule = rule;
			refId = "";
			refParaNum = 0;
			hitScore = 0.0;
			incompleteState = IncompleteType.NONE;
		}
		
		private int getID() {
			return id;
		}
		
		private String getName() {
			return name;
		}
		
		private String getType() {
			return type;
		}
		
		private int getMeasureNum() {
			return measureNum;
		}
		
		private String getRule() {
			return rule;
		}
		
		private void setRefId(final String id) {
			refId = id;
		}
		
		private String getRefId() {
			return refId;
		}
		
		private void setRefParaNum(final int num) {
			refParaNum = num;
		}
		
		private int getRefParaNum() {
			return refParaNum;
		}
		
		private void setHitScore(final double score) {
			hitScore = score;
		}
		
		private double getHitScore() {
			return hitScore;
		}

		private void setIncomplete(final IncompleteType type) {
			incompleteState = type;
		}

		private IncompleteType getIncomplete() {
			return incompleteState;
		}
	} // end inner class
	
	public static final class ProsodyOutput {
		private IntegerProperty id;
		private StringProperty name;
		private StringProperty type;
		private StringProperty ref;
		private DoubleProperty score;
		private final Gatha gatha;
		
		public ProsodyOutput(final Gatha gatha) {
			this.gatha = gatha;
			final String typ = gatha.getType();
			final String gtype;
			if (typ.length() == 2) {
				final int itype = typ.charAt(0)=='m' ? 0 : 1;
				gtype = gathaType[itype] + ", " + gathaSubType[itype][typ.charAt(1) - '0' - 1];
			} else {
				gtype = "";
			}
			final String rf = gatha.getRefId();
			final String gref = rf.isEmpty() ? "" : rf + ". " + gatha.getRefParaNum();
			idProperty().set(gatha.getID());
			nameProperty().set(gatha.getName());
			typeProperty().set(gtype);
			refProperty().set(gref);
			scoreProperty().set(gatha.getHitScore());
		}
		
		public IntegerProperty idProperty() {
			if (id == null)
				id = new SimpleIntegerProperty(this, "id");
			return id;
		}
		
		public StringProperty nameProperty() {
			if (name == null)
				name = new SimpleStringProperty(this, "name");
			return name;
		}
		
		public StringProperty typeProperty() {
			if (type == null)
				type = new SimpleStringProperty(this, "type");
			return type;
		}
		
		public StringProperty refProperty() {
			if (ref == null)
				ref = new SimpleStringProperty(this, "ref");
			return ref;
		}
		
		public DoubleProperty scoreProperty() {
			if (score == null)
				score = new SimpleDoubleProperty(this, "score");
			return score;
		}

		public Gatha getGatha() {
			return gatha;
		}
	} // end inner class
	
}
