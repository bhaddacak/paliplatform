/*
 * GramHtmlViewer.java
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
 * @version 3.2
 * @since 3.0
 */
public class GramHtmlViewer extends PaliHtmlViewer {
	private static enum LeftListType { HEADING, NUMBER }
	private static final String INDENT = "&nbsp;&nbsp;&nbsp;&nbsp;";
	private static final String BR = "<br>";
	private static final String BULLET = "&bull;";
	static final double DIVIDER_POSITION_LEFT = 0.2;
	private final SplitPane splitPane = new SplitPane();
	private final BorderPane leftPane = new BorderPane();
	private final ObservableList<String> docNavList = FXCollections.<String>observableArrayList();
	private final ListView<String> docNavListView;
	private LeftListType currLeftListType = LeftListType.HEADING;
	private final HBox contextToolBar = new HBox();
	private final ToggleButton heartButton = new ToggleButton("", new TextIcon("heart", TextIcon.IconSet.AWESOME));
	private final MenuButton optionsMenu = new MenuButton("", new TextIcon("check-double", TextIcon.IconSet.AWESOME));		
	private final List<CheckMenuItem> relatedBookMenuList = new ArrayList<>();
	private final InfoPopup helpPopup = new InfoPopup();
	private GrammarText gramText;
	private String recentJS = "";

	public GramHtmlViewer(final TocTreeNode node, final String strToLocate) {
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
		init(node, strToLocate);
	}

	public void init(final TocTreeNode node, final String strToLocate) {
		super.init(node);
		Platform.runLater(() ->	{
			contextToolBar.getChildren().clear();
			optionsMenu.getItems().clear();
			relatedBookMenuList.clear();
			heartButton.setSelected(false);
			loadContent();
			setupContextToolBar();
			initFindInput();
			setInitialStringToLocate(strToLocate);
		});
	}

	public void loadContent() {
		String filename =  thisDoc.getNodeFileName();
		final Corpus corpus = thisDoc.getCorpus();
		gramText = new GrammarText(thisDoc, ReaderUtilities.readTextFromZip(filename, corpus));
		setupContent();
		displayScript.set(Utilities.PaliScript.ROMAN);
	}

	private void setupContent() {
		final GrammarText.GrammarBook bookId = gramText.getBook();
		final String body;
		final List<GrammarText.GrammarBook> extras = new ArrayList<>();
		for (final CheckMenuItem related : relatedBookMenuList) {
			if (related.isSelected())
				extras.add((GrammarText.GrammarBook)related.getUserData());
		}
		if (heartButton.isSelected()) {
			body = formatDisplay(gramText.getFormulaListAsString(bookId, extras));
		} else {
			body = formatDisplay(gramText.getText(bookId, extras));
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
		final String descStr = ((SimpleDocumentInfo)thisDoc.getCorpus().getDocInfo(thisDoc.getNodeId())).getDescription();
		final String desc = "<blockquote><p>"
						+ formatDescription(descStr)
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

	private String formatDescription(final String text) {
		return text.replaceAll("\\\\n", BR + INDENT + BULLET) // change \n to <br> and indent and bullet
					.replaceAll("\\*(.*?)\\*", "<b>$1</b>"); // change *_* to <b>_</b>
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
		final GrammarText.GrammarBook bookId = gramText.getBook();
		if (gramText.hasSuttaFormula()) {
			heartButton.setTooltip(new Tooltip("Show only sutta heads/formulae"));
			heartButton.setOnAction(actionEvent -> setupContent());
			contextToolBar.getChildren().add(heartButton);
		}
		final List<GrammarText.GrammarBook> relatedBooks = bookId.getRelatedBooks();
		if (!relatedBooks.isEmpty()) {
			optionsMenu.setTooltip(new Tooltip("Related books"));
			final Corpus corpus = thisDoc.getCorpus();
			for (final GrammarText.GrammarBook book : relatedBooks) {
				final CheckMenuItem menuItem = new CheckMenuItem(corpus.getDocInfo(book.getBookId()).getTextName());
				menuItem.setUserData(book);
				menuItem.setOnAction(actionEvent -> setupContent());
				optionsMenu.getItems().add(menuItem);
				relatedBookMenuList.add(menuItem);
			}
			final MenuItem allMenuItem = new MenuItem("Include all");
			allMenuItem.setOnAction(actionEvent -> {relatedBookMenuList.forEach(x -> x.setSelected(true)); setupContent();});
			final MenuItem noneMenuItem = new MenuItem("Include none");
			noneMenuItem.setOnAction(actionEvent -> {relatedBookMenuList.forEach(x -> x.setSelected(false)); setupContent();});
			optionsMenu.getItems().addAll(new SeparatorMenuItem(), allMenuItem, noneMenuItem);
			contextToolBar.getChildren().add(optionsMenu);
		}
	}

	private void setDocNav(final LeftListType listType) {
		currLeftListType = listType;
		final List<String> docNav = new ArrayList<>();
		if (listType == LeftListType.HEADING) {
			final List<String[]> headList = gramText.getHeadList();
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
			final List<String> numList = gramText.getNumList();
			docNav.addAll(numList);
		}
		docNavList.setAll(docNav);
		docNavListView.scrollTo(0);
	}

}

