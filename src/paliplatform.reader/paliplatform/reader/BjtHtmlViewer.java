/*
 * BjtHtmlViewer.java
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
import java.util.stream.Collectors;

import javafx.application.Platform;
import javafx.scene.control.*;
import javafx.scene.Node;
import javafx.scene.layout.*;
import javafx.scene.input.*;
import javafx.collections.ObservableList;
import javafx.collections.FXCollections;
import javafx.beans.property.*;
import javafx.geometry.Pos;
import javafx.geometry.Insets;

/** 
 * The viewer of BJT Pali texts. 
 * 
 * @author J.R. Bhaddacak
 * @version 3.6
 * @since 3.0
 */
public class BjtHtmlViewer extends PaliHtmlViewer {
	private static enum LeftListType { 
		HEADING_TEXT("h"), HEADING_NUM("n"), PAGE("p"), GATHA("g");
		private String abbr;
		private LeftListType(final String ch) {
			abbr = ch;
		}
		public String getAbbr() {
			return abbr;
		}
	}
	static final double DIVIDER_POSITION_LEFT = 0.2;
	static final double DIVIDER_POSITION_RIGHT = 0.8;
	private final SplitPane splitPane = new SplitPane();
	private final BorderPane rightPane = new BorderPane();
	private final BorderPane leftPane = new BorderPane();
	private final ObservableList<String> docTocList = FXCollections.<String>observableArrayList();
	private final ListView<String> docTocListView;
	private final List<String[]> headTextList = new ArrayList<>();
	private final List<String> headNumList = new ArrayList<>();
	private final List<String> pageList = new ArrayList<>();
	private final List<String> gathaList = new ArrayList<>();
	private LeftListType currLeftListType = LeftListType.HEADING_TEXT;
	private static Map<String, DocumentInfo> bjtInfoMap;
	private BjtInfo nodeInfo;
	private String recentJS = "";

	public BjtHtmlViewer(final TocTreeNode node, final String strToLocate) {
		super(node);
		webEngine.setUserStyleSheetLocation(ReaderUtilities.class.getResource(ReaderUtilities.BJT_CSS).toExternalForm());

		if (bjtInfoMap == null)
			bjtInfoMap = node.getCorpus().getDocInfoMap();
		nodeInfo = getNodeInfo();
		
		// prepare the left pane's content (for the right pane see init())
		docTocListView = new ListView<>(docTocList);
		// customize list cells
		docTocListView.setCellFactory((ListView<String> lv) -> {
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
		// add mouse listener
		docTocListView.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> {
			final int ind = docTocListView.getSelectionModel().getSelectedIndex();
			if (ind >= 0) {
				final String prefix = "" + currLeftListType.getAbbr();
				final String command = "jumpTo('" + prefix + ind + "')";
				webEngine.executeScript(command);
				recentJS = command;
			}
		});
		// add left pane's toolbar
		final ToolBar leftpaneToolbar = new ToolBar();
		final ToggleButton leftTextHeadingButton = new ToggleButton("", new TextIcon("H", TextIcon.IconSet.AWESOME));
		leftTextHeadingButton.setTooltip(new Tooltip("Text heading list"));
		leftTextHeadingButton.setOnAction(actionEvent -> setDocToc(LeftListType.HEADING_TEXT));
		final ToggleButton leftNumHeadingButton = new ToggleButton("", new TextIcon("N", TextIcon.IconSet.AWESOME));
		leftNumHeadingButton.setTooltip(new Tooltip("Number heading list"));
		leftNumHeadingButton.setOnAction(actionEvent -> setDocToc(LeftListType.HEADING_NUM));
		final ToggleButton leftPageButton = new ToggleButton("", new TextIcon("hashtag", TextIcon.IconSet.AWESOME));
		leftPageButton.setTooltip(new Tooltip("Page number list"));
		leftPageButton.setOnAction(actionEvent -> setDocToc(LeftListType.PAGE));
		final ToggleButton leftGathaButton = new ToggleButton("", new TextIcon("music", TextIcon.IconSet.AWESOME));
		leftGathaButton.setTooltip(new Tooltip("Stanza list"));
		leftGathaButton.setOnAction(actionEvent -> setDocToc(LeftListType.GATHA));
		final ToggleGroup leftListTypeGroup = new ToggleGroup();
		leftListTypeGroup.getToggles().addAll(leftTextHeadingButton, leftNumHeadingButton, leftPageButton, leftGathaButton);
		leftListTypeGroup.selectToggle(
			currLeftListType==LeftListType.HEADING_TEXT ? leftTextHeadingButton :
			currLeftListType==LeftListType.HEADING_NUM ? leftNumHeadingButton :
			currLeftListType==LeftListType.PAGE ? leftPageButton :
			leftGathaButton);
		leftpaneToolbar.getItems().addAll(leftTextHeadingButton, leftNumHeadingButton, leftPageButton, leftGathaButton);
		// set left components
		leftPane.setTop(leftpaneToolbar);
		leftPane.setCenter(docTocListView);
		
		// compose SplitPane, the left pane is not shown at start
		splitPane.setDividerPositions(DIVIDER_POSITION_RIGHT);
		splitPane.getItems().addAll(textPane, rightPane);	
		
		// add main components
		// add new buttons to tool bar
		final Button toggleRightPaneButton = new Button("", new TextIcon("right-pane", TextIcon.IconSet.CUSTOM));
		toggleRightPaneButton.setTooltip(new Tooltip("Right pane on/off"));
		toggleRightPaneButton.setOnAction(actionEvent -> {
			final ObservableList<Node> ol = splitPane.getItems();
			boolean removed = false;
			if (ol.contains(rightPane)) {
				ol.remove(rightPane);
				removed = true;
			} else {
				ol.add(rightPane);
			}
			final double[] divPos = splitPane.getDividerPositions();
			if (divPos.length == 1) {
				if (removed)
					splitPane.setDividerPositions(DIVIDER_POSITION_LEFT);
				else
					splitPane.setDividerPositions(DIVIDER_POSITION_RIGHT);
			} else if (divPos.length == 2) {
				splitPane.setDividerPositions(DIVIDER_POSITION_LEFT, DIVIDER_POSITION_RIGHT);
			}
		});
		final Button toggleLeftPaneButton = new Button("", new TextIcon("left-pane", TextIcon.IconSet.CUSTOM));
		toggleLeftPaneButton.setTooltip(new Tooltip("Left pane on/off"));
		toggleLeftPaneButton.setOnAction(actionEvent -> {
			final ObservableList<Node> ol = splitPane.getItems();
			boolean removed = false;
			if (ol.contains(leftPane)) {
				ol.remove(leftPane);
				removed = true;
			} else {
				if (ol.size() == 1)
					ol.setAll(leftPane, textPane);
				else if (ol.size() == 2)
					ol.setAll(leftPane, textPane, rightPane);
			}
			final double[] divPos = splitPane.getDividerPositions();
			if (divPos.length == 1) {
				if (removed)
					splitPane.setDividerPositions(DIVIDER_POSITION_RIGHT);
				else
					splitPane.setDividerPositions(DIVIDER_POSITION_LEFT);
			} else if (divPos.length == 2) {
				splitPane.setDividerPositions(DIVIDER_POSITION_LEFT, DIVIDER_POSITION_RIGHT);
			}
			if (!removed) {
				setDocToc(currLeftListType);
			}
		});
		final Button toggleFullViewButton = new Button("", new TextIcon("full-view", TextIcon.IconSet.CUSTOM));
		toggleFullViewButton.setTooltip(new Tooltip("Full view on/off"));
		toggleFullViewButton.setOnAction(actionEvent -> {
			final ObservableList<Node> ol = splitPane.getItems();
			if (ol.size() > 1)
				ol.setAll(textPane);
			else
				ol.setAll(leftPane, textPane, rightPane);
			final double[] divPos = splitPane.getDividerPositions();
			if (divPos.length == 2) {
				splitPane.setDividerPositions(DIVIDER_POSITION_LEFT, DIVIDER_POSITION_RIGHT);
			}
		});
		final Button recentJumpButton = new Button("", new TextIcon("arrow-turn-down", TextIcon.IconSet.AWESOME));
		recentJumpButton.setTooltip(new Tooltip("The last jump"));
		recentJumpButton.setOnAction(actionEvent -> {
			if (recentJS.length() > 0)
				webEngine.executeScript(recentJS);
		});
		final Button helpButton = new Button("", new TextIcon("circle-question", TextIcon.IconSet.AWESOME));
		helpButton.setOnAction(actionEvent -> helpInfoPopup.showPopup(helpButton, InfoPopup.Pos.BELOW_RIGHT, true));
		toolBar.getItems().addAll(new Separator(), toggleLeftPaneButton, toggleFullViewButton, toggleRightPaneButton
								, new Separator(), recentJumpButton
								, new Separator(), helpButton
								);

		setCenter(splitPane);
		helpInfoPopup.setContentWithText(ReaderUtilities.getTextResource("info-bjtviewer.txt"));
		helpInfoPopup.setTextWidth(Utilities.getRelativeSize(28));
		init(node, strToLocate);
	}

	public void init(final TocTreeNode node, final String strToLocate) {
		super.init(node);
		final Corpus corpus = node.getCorpus();
		Platform.runLater(() ->	{
			rightPane.setCenter(createInfoBox());
			loadContent(corpus.getScript());
			initFindInput();
			setInitialStringToLocate(strToLocate);
		});
	}

	private BjtInfo getNodeInfo() {
		final String nodeId = thisDoc.getNodeId();
		return (BjtInfo)bjtInfoMap.get(nodeId);
	}

	private VBox createInfoBox() {
		final StringBuilder infoContent = new StringBuilder(); // for clipboard copy
		final VBox selfInfoBox = new VBox();
		final String nodeId = thisDoc.getNodeId();
		final Corpus corpus = thisDoc.getCorpus();
		nodeInfo = getNodeInfo();
		final AnchorPane groupClassBar = new AnchorPane();
		final Label groupClassLbl = new Label(nodeInfo.getGroupStr() + " (" + corpus.getRootName().toUpperCase() + ")");
		groupClassLbl.setStyle("-fx-font-size:0.8em;");
		infoContent.append(groupClassLbl.getText()).append("\n");
		final Button copyButton = new Button("", new TextIcon("copy", TextIcon.IconSet.AWESOME, Utilities.IconSize.SMALL));
		copyButton.setPadding(new Insets(2));
		copyButton.setTooltip(new Tooltip("Copy information"));
		copyButton.setOnAction(actionEvent -> {
			final Clipboard clipboard = Clipboard.getSystemClipboard();
			final ClipboardContent content = new ClipboardContent();
			content.putString(infoContent.toString());
			clipboard.setContent(content);
		});
		AnchorPane.setTopAnchor(groupClassLbl, 0.0);
		AnchorPane.setLeftAnchor(groupClassLbl, 0.0);
		AnchorPane.setTopAnchor(copyButton, 0.0);
		AnchorPane.setRightAnchor(copyButton, 0.0);
		groupClassBar.getChildren().addAll(groupClassLbl, copyButton);
		final String nodeName = thisDoc.getNodeName();
		final Label textNameLbl = new Label(nodeName);
		textNameLbl.getStyleClass().add("emphasized");
		infoContent.append(textNameLbl.getText()).append("\n");
		selfInfoBox.getChildren().addAll(groupClassBar, textNameLbl);
		final List<String> altNames = nodeInfo.getAltNames();
		if (!altNames.isEmpty()) {
			final String altNameStr = altNames.stream().collect(Collectors.joining(", "));
			final Label altNameLbl = new Label("  [" + altNameStr + "]");
			altNameLbl.setStyle("-fx-font-size:0.9em;");
			infoContent.append(altNameLbl.getText()).append("\n");
			selfInfoBox.getChildren().add(altNameLbl);
		}
		final String fname = nodeInfo.getFileNameWithExt();
		final Label fileNameLbl = new Label("  File: " + fname.substring(fname.lastIndexOf("/") + 1));
		fileNameLbl.setStyle("-fx-font-size:0.8em;");
		infoContent.append(fileNameLbl.getText()).append("\n");
		selfInfoBox.getChildren().add(fileNameLbl);
		final String descStr = nodeInfo.getDescription();
		if (!descStr.isEmpty()) {
			final Label descLbl = new Label("‣ " + descStr);
			descLbl.setWrapText(true);
			descLbl.setStyle("-fx-font-size:0.9em;");
			infoContent.append(descLbl.getText()).append("\n");
			selfInfoBox.getChildren().add(descLbl);
		}
		// add relative links information
		int baseNum = 0;
		final ObservableList<StringPair> relLinkInfoList = FXCollections.<StringPair>observableArrayList();
		final ListView<StringPair> relLinkInfoListView = new ListView<>(relLinkInfoList);
		final List<String> upperLinks = getUpperRelativeDocChain(nodeId);
		final CommentaryTreeNode lowerLinkTreeNode = new CommentaryTreeNode(nodeId, 0);
		final List<StringPair> links = new ArrayList<>();
		String title;
		if (!upperLinks.isEmpty()) {
			for (final String id : upperLinks) {
				final BjtInfo binfo = (BjtInfo)bjtInfoMap.get(id);
				final String textName = binfo.getTextName();
				final String ref = binfo.getRef();
				title = textName + " (" + ref + ")";
				links.add(new StringPair(title, id));
			}
		}
		baseNum = links.size();
		final List<StringPair> lowerLinkList = lowerLinkTreeNode.toStringPairList();
		links.addAll(lowerLinkList);
		relLinkInfoList.setAll(links);
		// customize list cells
		final int baseIndex = baseNum;
		relLinkInfoListView.setCellFactory((ListView<StringPair> lv) -> {
			return new ListCell<StringPair>() {
				@Override
				public void updateItem(StringPair item, boolean empty) {
					super.updateItem(item, empty);
					final int index = this.getIndex();
					this.setGraphic(null);
					if (empty) {
						this.setText(null);
						this.setTooltip(null);
					} else {
						String value = this.getItem().toString();
						this.setTooltip(new Tooltip(value));
						this.setText(value);
					}
					if (index == baseIndex)
						this.getStyleClass().add("emphasized");
					this.setStyle("-fx-padding: 0px 0px 0px 3px");
				}
			};
		});
		// use VBox as the container of the box
		final VBox resultBox = new VBox();
		VBox.setVgrow(relLinkInfoListView, Priority.ALWAYS);
		final ObservableList<Node> boxChildren = resultBox.getChildren();
		selfInfoBox.getStyleClass().add("bordered-box");
		boxChildren.add(selfInfoBox);
		// add relative links
		final StackPane reldocHead = new StackPane();
		reldocHead.getChildren().add(new Label("Related documents"));
		reldocHead.getStyleClass().add("head-inverted");
		boxChildren.add(reldocHead);
		boxChildren.add(relLinkInfoListView);
		// add context menus to the right pane
		final ContextMenu relLinkPopupMenu = new ContextMenu();
		final MenuItem openMenuItem = new MenuItem("Open");
		openMenuItem.setOnAction(actionEvent -> {
			final StringPair selected = relLinkInfoListView.getSelectionModel().getSelectedItem();
			final String id = selected.getSecond();
			final BjtInfo binfo = (BjtInfo)bjtInfoMap.get(id);
			String textName = selected.getFirst();
			if (textName.indexOf(" ") == 1)
				textName = textName.substring(2);
			final TocTreeNode ttn = new SimpleTocTreeNode(corpus, id, textName, binfo.getFileNameWithExt());
			if (Utilities.checkFileExistence(ttn.getNodeFile()))
				ReaderUtilities.openPaliHtmlViewer(ttn);
		});
		relLinkPopupMenu.getItems().addAll(openMenuItem);
		// add mouse listener
		relLinkInfoListView.setOnMouseClicked(mouseEvent -> {
			final int ind = relLinkInfoListView.getSelectionModel().getSelectedIndex();
			final StringPair selected = relLinkInfoListView.getSelectionModel().getSelectedItem();
			if (selected != null && selected.toString().length() > 0) {
				if (ind != baseIndex)
					relLinkPopupMenu.show(relLinkInfoListView, mouseEvent.getScreenX(), mouseEvent.getScreenY());
				else
					relLinkPopupMenu.hide();
			} else {
				relLinkPopupMenu.hide();
			}
		});
		return resultBox;
	}

	@Override
	public void convertScript() {
		loadContent(displayScript.get());
	}

	public void loadContent(final Utilities.PaliScript script) {
		final List<BjtPage> pages = ReaderUtilities.getBjtPages(thisDoc.getNodeFileName());
		final Utilities.PaliScript srcScript = thisDoc.getCorpus().getScript();
		final ScriptTransliterator.EngineType romanDef = 
						ScriptTransliterator.EngineType.fromCode(Utilities.getSetting("roman-translit"));
		pageBody = formatText(pages);
		pageBody = script == srcScript
					? pageBody
					: ScriptTransliterator.translitPaliScript(pageBody, srcScript, script, romanDef, alsoConvertNumber, false);
		final String bjtJS = ReaderUtilities.getStringResource(ReaderUtilities.BJT_JS);
		final String pageContent = ReaderUtilities.makeHTML(pageBody, bjtJS);
		setContent(pageContent);
	}

	private static String processText(final String text) {
		final Pattern boldPatt = Pattern.compile("\\*\\*(.*?)\\*\\*");
		final Matcher boldMatcher = boldPatt.matcher(text);
		String result = boldMatcher.replaceAll("<b>$1</b>");
		result = removeMarkDown(result, false);
		result = result.replace("\n", "<br>");
		return result;
	}

	private static String removeMarkDown(final String text, boolean isAll) {
		String result = text.replace("__", "");
		result = result.replace("$$", "");
		if (isAll)
			result = result.replace("**", "");
		return result;
	}

	private String formatText(final List<BjtPage> pages) {
		headTextList.clear();
		headNumList.clear();
		pageList.clear();
		gathaList.clear();
		final StringBuilder result = new StringBuilder();
		final Pattern hnumPatt = Pattern.compile("[0-9. -]+");
		String idStr = "";
		String classStr = "";
		String text = "";
		int level = 0;
		String[] headArray; // [text, level]
		int pageCounter = 0;
		int headCounter = 0;
		int hnumCounter = 0;
		int gathaCounter = 0;
		for (final BjtPage page : pages) {
			final int pnum = page.getPageNum();
			pageList.add("" + pnum);
			classStr = " class='bjt-page'";
			idStr = " id='jumptarget-p" + pageCounter + "'";
			pageCounter++;
			result.append("<p" + classStr + idStr + ">[" + pnum + "]</p>").append("\n");
			final List<BjtPage.Element> entries = page.getEntries();
			for (final BjtPage.Element elem : entries) {
				if (elem.getType() == BjtPage.Type.HEADING) {
					text = removeMarkDown(elem.getText(), true);
					level = elem.getLevel();
					final Matcher hnumMatcher = hnumPatt.matcher(text);
					if (hnumMatcher.matches()) {
						headNumList.add(text);
						idStr = " id='jumptarget-n" + hnumCounter + "'";
						hnumCounter++;
					} else {
						headArray = new String[] { text, "" + level };
						headTextList.add(headArray);
						idStr = " id='jumptarget-h" + headCounter + "'";
						headCounter++;
					}
					classStr = " class='bjt-heading" + level + "'";
					result.append("<p" + classStr + idStr + ">" + text + "</p>").append("\n");
				} else if (elem.getType() == BjtPage.Type.CENTERED) {
					text = processText(elem.getText());
					final Matcher hnumMatcher = hnumPatt.matcher(text);
					if (hnumMatcher.matches()) {
						headNumList.add(text);
						idStr = " id='jumptarget-n" + hnumCounter + "'";
						hnumCounter++;
					} else {
						idStr = "";
					}
					classStr = " class='bjt-centered'";
					result.append("<p" + classStr + idStr + ">" + text + "</p>").append("\n");
				} else if (elem.getType() == BjtPage.Type.UNINDENTED) {
					text = processText(elem.getText());
					classStr = " class='bjt-unindented'";
					result.append("<p" + classStr + ">" + text + "</p>").append("\n");
				} else if (elem.getType() == BjtPage.Type.PARAGRAPH) {
					text = processText(elem.getText());
					classStr = " class='bjt-paragraph'";
					result.append("<p" + classStr + ">" + text + "</p>").append("\n");
				} else if (elem.getType() == BjtPage.Type.GATHA) {
					text = elem.getText();
					final String first = text.split("\\n")[0];
					gathaList.add(removeMarkDown(first, true));
					text = processText(text);
					classStr = " class='bjt-gatha'";
					idStr = " id='jumptarget-g" + gathaCounter + "'";
					gathaCounter++;
					result.append("<p" + classStr + idStr + ">" + text + "</p>").append("\n");
				}
			}
		}
		return result.toString();
	}
	
	public void clearContent() {
		docTocListView.scrollTo(0);
		final ObservableList<Node> ol = splitPane.getItems();
		if (!ol.contains(rightPane))
			ol.add(rightPane);
		if (ol.contains(leftPane))
			ol.remove(leftPane);
		splitPane.setDividerPositions(DIVIDER_POSITION_RIGHT);
		super.clearContent();
	}

	private static List<String> getUpperRelativeDocChain(final String id) {
		return bjtInfoMap.values().stream()
						.filter(x -> ((BjtInfo)x).hasCommentary(id))
						.map(x -> ((BjtInfo)x).getId())
						.collect(Collectors.toList());
	}

	private void setDocToc(final LeftListType listType) {
		currLeftListType = listType;
		final List<String> docToc = new ArrayList<>();
		if (listType == LeftListType.HEADING_TEXT) {
			for (final String[] headItem : headTextList) {
				final int level = Integer.parseInt(headItem[1]);
				String spaces = " ";
				int start = 5;
				while (start - level > 0) {
					spaces = spaces + " ";
					start--;
				}
				final String indent = level < 5 ? spaces : "";
				docToc.add(indent + headItem[0]);
			}
		} else if (listType == LeftListType.HEADING_NUM) {
			docToc.addAll(headNumList);
		} else if (listType == LeftListType.PAGE) {
			docToc.addAll(pageList);
		} else if (listType == LeftListType.GATHA) {
			docToc.addAll(gathaList);
		}
		docTocList.setAll(docToc);
		docTocListView.scrollTo(0);
	}

	public void updateClickedObject(final String text) {
		clickedText.set(text.trim());
	}

	// inner classes
	static class CommentaryTreeNode {
		final String nodeId;
		final int level;
		final List<CommentaryTreeNode> branches;
		public CommentaryTreeNode(final String id, final int lev) {
			nodeId = id;
			level = lev;
			branches = new ArrayList<>();
			addBranches();
		}
		private void addBranches() {
			final BjtInfo nodeInfo = (BjtInfo)bjtInfoMap.get(nodeId);
			final List<String> comList = nodeInfo.getCommentaries();
			final int childLevel = level + 1;
			for (final String comId : comList) {
				final CommentaryTreeNode comNode = new CommentaryTreeNode(comId, childLevel);
				branches.add(comNode);
			}
		}
		public String getNodeId() {
			return nodeId;
		}
		public int getLevel() {
			return level;
		}
		public List<CommentaryTreeNode> getBranches() {
			return branches;
		}
		private List<Object[]> toList() {
			final List<Object[]> result = new ArrayList<>();
			final Object[] current = new Object[2];
			current[0] = nodeId;
			current[1] = Integer.valueOf(level);
			result.add(current);
			for (final CommentaryTreeNode child : branches) {
				result.addAll(child.toList());
			}
			return result;
		}
		public List<StringPair> toStringPairList() {
			final List<StringPair> result = new ArrayList<>();
			final List<Object[]> nodeList = this.toList();
			for (final Object[] objs : nodeList) {
				// objs[0] = id, objs[1] = level
				final String id = (String)objs[0];
				final int level = (Integer)objs[1];
				int start = 1;
				String indent = level > 0 ? "   " : "";
				while (start < level) {
					indent += "   ";
					start++;
				}
				final BjtInfo binfo = (BjtInfo)bjtInfoMap.get(id);
				final String textName = binfo.getTextName();
				final String ref = binfo.getRef();
				final String fullName = textName + " (" + ref + ")";
				result.add(new StringPair(indent + ReaderUtilities.bulletMap.get(level) + " " + fullName, id));
			}
			return result;
		}
	}

}

