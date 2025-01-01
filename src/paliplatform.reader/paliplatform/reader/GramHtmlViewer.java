/*
 * GramHtmlViewer.java
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

package paliplatform.reader;

import paliplatform.base.*;

import java.util.*;
import java.util.regex.*;

import javafx.application.Platform;
import javafx.scene.control.*;
import javafx.scene.Node;
import javafx.scene.layout.*;
import javafx.scene.input.*;
import javafx.collections.ObservableList;
import javafx.collections.FXCollections;
import javafx.beans.property.*;
import javafx.geometry.Orientation;

/** 
 * The viewer of Pali grammar books.
 * 
 * @author J.R. Bhaddacak
 * @version 3.0
 * @since 3.0
 */
public class GramHtmlViewer extends PaliHtmlViewer {
	private static enum LeftListType { HEADING, NUMBER }
	private static final String INDENT = "&nbsp;&nbsp;&nbsp;&nbsp;";
	private static final String BR = "<br>";
	static final double DIVIDER_POSITION_LEFT = 0.2;
	private final SplitPane splitPane = new SplitPane();
	private final BorderPane leftPane = new BorderPane();
	private final ObservableList<String> docNavList = FXCollections.<String>observableArrayList();
	private final ListView<String> docNavListView;
	private LeftListType currLeftListType = LeftListType.HEADING;
	private final HBox contextToolBar = new HBox();
	private final ToggleGroup textSelector = new ToggleGroup();
	private final ToggleButton combineButton = new ToggleButton("", new TextIcon("handshake-angle", TextIcon.IconSet.AWESOME));
	private final ToggleButton heartButton = new ToggleButton("", new TextIcon("heart", TextIcon.IconSet.AWESOME));
	private final MenuButton optionsMenu = new MenuButton("", new TextIcon("check-double", TextIcon.IconSet.AWESOME));		
	private final CheckMenuItem pancMenuItem = new CheckMenuItem("Moggallānapañcikā");
	private final CheckMenuItem niruMenuItem = new CheckMenuItem("Niruttidīpanī");
	private final InfoPopup helpPopup = new InfoPopup();
	private GrammarText gramText;
	private GrammarText.GrammarBook currBookId;
	private String recentJS = "";

	public GramHtmlViewer(final TocTreeNode node) {
		super(node);
		// prepare the left pane's content
		docNavListView = new ListView<>(docNavList);
		docNavListView.setCellFactory((ListView<String> lv) -> {
			return new ListCell<String>() {
				@Override
				public void updateItem(String item, boolean empty) {
					super.updateItem(item, empty);
					final int index = this.getIndex();
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
		docNavListView.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> {
			final int ind = docNavListView.getSelectionModel().getSelectedIndex();
			if (ind >= 0) {
				final String prefix = "" + currLeftListType.toString().toLowerCase().charAt(0);
				final String command = "jumpTo('" + prefix + ind + "')";
				webEngine.executeScript(command);
				recentJS = command;
			}
		});
		// add left pane's toolbar
		final ToolBar leftpaneToolbar = new ToolBar();
		final ToggleButton leftHeadingButton = new ToggleButton("", new TextIcon("heading", TextIcon.IconSet.AWESOME));
		leftHeadingButton.setTooltip(new Tooltip("Heading list"));
		leftHeadingButton.setOnAction(actionEvent -> setDocNav(LeftListType.HEADING));
		final ToggleButton leftNumButton = new ToggleButton("", new TextIcon("hashtag", TextIcon.IconSet.AWESOME));
		leftNumButton.setTooltip(new Tooltip("Sutta number list"));
		leftNumButton.setOnAction(actionEvent -> setDocNav(LeftListType.NUMBER));
		final ToggleGroup leftListTypeGroup = new ToggleGroup();
		leftListTypeGroup.getToggles().addAll(leftHeadingButton, leftNumButton);
		leftListTypeGroup.selectToggle(currLeftListType == LeftListType.HEADING ? leftHeadingButton : leftNumButton);
		leftpaneToolbar.getItems().addAll(leftHeadingButton, leftNumButton);
		// set left components
		leftPane.setTop(leftpaneToolbar);
		leftPane.setCenter(docNavListView);
		// add new buttons to tool bar
		final Button toggleLeftPaneButton = new Button("", new TextIcon("left-pane", TextIcon.IconSet.CUSTOM));
		toggleLeftPaneButton.setTooltip(new Tooltip("Left pane on/off"));
		toggleLeftPaneButton.setOnAction(actionEvent -> {
			final ObservableList<Node> ol = splitPane.getItems();
			if (ol.contains(leftPane)) {
				ol.remove(leftPane);
			} else {
				ol.setAll(leftPane, textPane);
				splitPane.setDividerPositions(DIVIDER_POSITION_LEFT);
			}
		});
		final Button recentJumpButton = new Button("", new TextIcon("arrow-turn-down", TextIcon.IconSet.AWESOME));
		recentJumpButton.setTooltip(new Tooltip("The last jump"));
		recentJumpButton.setOnAction(actionEvent -> {
			if (recentJS.length() > 0)
				webEngine.executeScript(recentJS);
		});
		toolBar.getItems().addAll(new Separator(), toggleLeftPaneButton, recentJumpButton, contextToolBar);
		final Button helpButton = new Button("", new TextIcon("circle-question", TextIcon.IconSet.AWESOME));
		helpButton.setOnAction(actionEvent -> helpPopup.showPopup(helpButton, InfoPopup.Pos.BELOW_RIGHT, true));
		toolBar.getItems().add(helpButton);
		
		// some init
		helpPopup.setContentWithText(ReaderUtilities.getTextResource("info-gramviewer.txt"));
		helpPopup.setTextWidth(Utilities.getRelativeSize(30));
		
		// compose SplitPane, the left pane is not shown at start
		splitPane.getItems().addAll(textPane);	
		setCenter(splitPane);
		init(node);
	}

	public void init(final TocTreeNode node) {
		super.init(node);
		Platform.runLater(() ->	{
			contextToolBar.getChildren().clear();
			textSelector.getToggles().clear();
			optionsMenu.getItems().clear();
			combineButton.setSelected(false);
			heartButton.setSelected(false);
			pancMenuItem.setSelected(false);
			niruMenuItem.setSelected(false);
			loadContent();
			setupContextToolBar();
			initFindInput();
		});
	}

	public void loadContent() {
		String filename =  thisDoc.getNodeFileName();
		final Corpus corpus = thisDoc.getCorpus();
		gramText = new GrammarText(thisDoc, ReaderUtilities.readTextFromZip(filename, corpus));
		if (textSelector.getToggles().size() > 0)
			loadContent((GrammarText.GrammarBook)textSelector.getSelectedToggle().getUserData());
		else
			setupContent(gramText.getFirstBook());
	}

	private void loadContent(final GrammarText.GrammarBook bookId) {
		setupContent(bookId);
		displayScript.set(Utilities.PaliScript.ROMAN);
	}

	private void setupContent() {
		setupContent(currBookId);
	}

	private void setupContent(final GrammarText.GrammarBook bookId) {
		currBookId = bookId;
		final String body;
		final List<GrammarText.GrammarBook> extras = new ArrayList<>();
		if (bookId == GrammarText.GrammarBook.MOGG) {
			if (pancMenuItem.isSelected())
				extras.add(GrammarText.GrammarBook.PANCT);
			if (niruMenuItem.isSelected())
				extras.add(GrammarText.GrammarBook.NIRU);
		}
		if (heartButton.isSelected()) {
			body = formatDisplay(gramText.getFormulaListAsString(bookId, combineButton.isSelected(), extras));
		} else {
			body = formatDisplay(gramText.getText(bookId, combineButton.isSelected(), extras));
		}
		pageBody = "<body>\n" + body + "\n</body>";
		final String gramJS = ReaderUtilities.getStringResource(ReaderUtilities.GRAM_JS);
		final String pageContent = ReaderUtilities.makeHTML(pageBody, gramJS);
		setContent(pageContent);
		setDocNav(currLeftListType);
	}

	private String formatDisplay(final String text) {
		final StringBuilder result = new StringBuilder();
		final String head = "<h3 id='texthead' style='text-align:center;'>" + thisDoc.getNodeName() + "</h3>\n";
		result.append(head);
		final String desc = "<blockquote><p>"
						+ ((SimpleDocumentInfo)thisDoc.getCorpus().getDocInfo(thisDoc.getNodeId())).getDescription()
						+ "</p></blockquote>\n";
		result.append(desc);
		final String[] lines = text.split("\\r?\\n");
		final Pattern noBrPatt = Pattern.compile("^</?(?:h|p|div|blockquote).*");
		for (final String line : lines) {
			if (line.trim().isEmpty()) continue;
			final Matcher noBrMatcher = noBrPatt.matcher(line);
			if (noBrMatcher.matches()) {
				result.append(line);
			} else {
				result.append(INDENT).append(line).append(BR);
			}
		}
		result.append(BR).append(BR);
		return result.toString();
	}

	public void clearContent() {
		docNavListView.scrollTo(0);
		final ObservableList<Node> ol = splitPane.getItems();
		if (ol.contains(leftPane))
			ol.remove(leftPane);
		super.clearContent();
	}

	private void setupContextToolBar() {
		if (gramText == null) return;
		if (gramText.isDual()) {
			final List<GrammarText.GrammarBook> bookList = gramText.getBookList();
			final List<RadioButton> textRadioList = new ArrayList<>();
			for (final GrammarText.GrammarBook book : bookList) {
				final RadioButton refRadio = new RadioButton(book.getRef());
				refRadio.setTooltip(new Tooltip(gramText.getBookName(book)));
				refRadio.setUserData(book);
				refRadio.setOnAction(actionEvent -> loadContent(book));
				textRadioList.add(refRadio);
			}
			if (!textRadioList.isEmpty()) {
				textSelector.getToggles().addAll(textRadioList);
				textRadioList.get(0).setSelected(true);
				contextToolBar.getChildren().add(new Separator(Orientation.VERTICAL));
				contextToolBar.getChildren().addAll(textRadioList);
			}
			combineButton.setTooltip(new Tooltip("Show two books together"));
			combineButton.setOnAction(actionEvent -> setupContent((GrammarText.GrammarBook)textSelector.getSelectedToggle().getUserData()));
			contextToolBar.getChildren().add(combineButton);
			if (thisDoc.getNodeId().equals("moggpayo")) {
				optionsMenu.setTooltip(new Tooltip("Additional books"));
				pancMenuItem.setSelected(false);
				pancMenuItem.setOnAction(actionEvent -> setupContent((GrammarText.GrammarBook)textSelector.getSelectedToggle().getUserData()));
				niruMenuItem.setSelected(false);
				niruMenuItem.setOnAction(actionEvent -> setupContent((GrammarText.GrammarBook)textSelector.getSelectedToggle().getUserData()));
				optionsMenu.getItems().addAll(pancMenuItem, niruMenuItem);
				contextToolBar.getChildren().add(optionsMenu);
			}
		}
		if (gramText.hasSuttaFormula()) {
			heartButton.setTooltip(new Tooltip("Show only sutta heads/formulae"));
			final GrammarText.GrammarBook bookId = gramText.isDual()
													? (GrammarText.GrammarBook)textSelector.getSelectedToggle().getUserData()
													: gramText.getFirstBook();
			heartButton.setOnAction(actionEvent -> setupContent());
			contextToolBar.getChildren().add(heartButton);
		}
	}

	private void setDocNav(final LeftListType listType) {
		currLeftListType = listType;
		final List<String> docNav = new ArrayList<>();
		if (listType == LeftListType.HEADING) {
			final List<String[]> headList = textSelector.getToggles().size() > 0
											? gramText.getHeadList((GrammarText.GrammarBook)textSelector.getSelectedToggle().getUserData())
											: gramText.getHeadList();
			for (final String[] headItem : headList) {
				final int level = Integer.parseInt(headItem[1]);
				String spaces = " ";
				int start = 3;
				while (level - start > 0) {
					spaces = spaces + " ";
					start++;
				}
				final String indent = level > 3 ? spaces : "";
				docNav.add(indent + headItem[0]);
			}
		} else if (listType == LeftListType.NUMBER) {
			final List<String> numList = textSelector.getToggles().size() > 0
											? gramText.getNumList((GrammarText.GrammarBook)textSelector.getSelectedToggle().getUserData())
											: gramText.getNumList();
			docNav.addAll(numList);
		}
		docNavList.setAll(docNav);
		docNavListView.scrollTo(0);
	}

}

