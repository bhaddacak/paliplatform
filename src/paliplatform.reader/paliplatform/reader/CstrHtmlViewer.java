/*
 * CstrHtmlViewer.java
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
import javafx.stage.Stage;
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
 * The viewer of CST-restructured Pali texts. 
 * 
 * @author J.R. Bhaddacak
 * @version 3.3
 * @since 3.0
 */
public class CstrHtmlViewer extends PaliHtmlViewer {
	private static enum LeftListType { HEADING, PARANUM, GATHA }
	static final double DIVIDER_POSITION_LEFT = 0.2;
	static final double DIVIDER_POSITION_RIGHT = 0.8;
	private final SplitPane splitPane = new SplitPane();
	private final BorderPane rightPane = new BorderPane();
	private final BorderPane leftPane = new BorderPane();
	private final ObservableList<String> docTocList = FXCollections.<String>observableArrayList();
	private final ListView<String> docTocListView;
	private final List<String[]> headList = new ArrayList<>();
	private final List<String> pnumList = new ArrayList<>();
	private final List<String> gathaList = new ArrayList<>();
	private final Map<String, Stage> childViewerMap = new HashMap<>();
	private final ToggleButton showNoteButton;
	private final ToggleButton syncButton = new ToggleButton("", new TextIcon("link", TextIcon.IconSet.AWESOME));
	private LeftListType currLeftListType = LeftListType.HEADING;
	private static Map<String, DocumentInfo> cstrInfoMap;
	private CstrInfo nodeInfo;
	private boolean showNotes = true;
	private String recentJS = "";

	public CstrHtmlViewer(final TocTreeNode node, final String strToLocate) {
		super(node);
		webEngine.setUserStyleSheetLocation(ReaderUtilities.class.getResource(ReaderUtilities.CSTR_CSS).toExternalForm());

		if (cstrInfoMap == null)
			cstrInfoMap = node.getCorpus().getDocInfoMap();
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
				final String prefix = "" + currLeftListType.toString().toLowerCase().charAt(0);
				final String command = "jumpTo('" + prefix + ind + "')";
				webEngine.executeScript(command);
				recentJS = command;
				if (isSyncSelected()) {
					// also sync child windows (only for paranum)
					synchronizeChildViewers();
				}
			}
		});
		// add left pane's toolbar
		final ToolBar leftpaneToolbar = new ToolBar();
		final ToggleButton leftHeadingButton = new ToggleButton("", new TextIcon("heading", TextIcon.IconSet.AWESOME));
		leftHeadingButton.setTooltip(new Tooltip("Heading list"));
		leftHeadingButton.setOnAction(actionEvent -> setDocToc(LeftListType.HEADING));
		final ToggleButton leftParnumButton = new ToggleButton("", new TextIcon("paragraph", TextIcon.IconSet.AWESOME));
		leftParnumButton.setTooltip(new Tooltip("Paragraph number list"));
		leftParnumButton.setOnAction(actionEvent -> setDocToc(LeftListType.PARANUM));
		final ToggleButton leftGathaButton = new ToggleButton("", new TextIcon("music", TextIcon.IconSet.AWESOME));
		leftGathaButton.setTooltip(new Tooltip("Stanza list"));
		leftGathaButton.setOnAction(actionEvent -> setDocToc(LeftListType.GATHA));
		final ToggleGroup leftListTypeGroup = new ToggleGroup();
		leftListTypeGroup.getToggles().addAll(leftHeadingButton, leftParnumButton, leftGathaButton);
		leftListTypeGroup.selectToggle(
			currLeftListType==LeftListType.HEADING ? leftHeadingButton :
			currLeftListType==LeftListType.PARANUM ? leftParnumButton :
			leftGathaButton);
		leftpaneToolbar.getItems().addAll(leftHeadingButton, leftParnumButton, leftGathaButton);
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
		showNoteButton = new ToggleButton("", new TextIcon("note-sticky", TextIcon.IconSet.AWESOME));
		showNoteButton.setTooltip(new Tooltip("Show/hide redactional notes"));
		showNoteButton.setSelected(showNotes);
		showNoteButton.setOnAction(actionEvent -> {
			final boolean selected = showNoteButton.isSelected();
			showNotes = selected;
			setShowNotes();
		});
		syncButton.setTooltip(new Tooltip("Make child viewers synchronized on/off"));
		syncButton.setSelected(true);
		syncButton.setDisable(!nodeInfo.isLinkable());
		syncButton.setOnAction(actionEvent -> {
			if (isSyncSelected())
				synchronizeChildViewers();
		});
		final Button helpButton = new Button("", new TextIcon("circle-question", TextIcon.IconSet.AWESOME));
		helpButton.setOnAction(actionEvent -> helpInfoPopup.showPopup(helpButton, InfoPopup.Pos.BELOW_RIGHT, true));
		toolBar.getItems().addAll(new Separator(), toggleLeftPaneButton, toggleFullViewButton, toggleRightPaneButton
								, new Separator(), recentJumpButton, showNoteButton, syncButton
								, new Separator(), helpButton
								);

		setCenter(splitPane);
		helpInfoPopup.setContentWithText(ReaderUtilities.getTextResource("info-cstrviewer.txt"));
		helpInfoPopup.setTextWidth(Utilities.getRelativeSize(34));
		init(node, strToLocate);
	}

	public void init(final TocTreeNode node, final String strToLocate) {
		super.init(node);
		Platform.runLater(() ->	{
			showNoteButton.setSelected(true);
			rightPane.setCenter(createInfoBox());
			loadContent();
			initFindInput();
			setInitialStringToLocate(strToLocate);
		});
	}

	private CstrInfo getNodeInfo() {
		final String nodeId = thisDoc.getNodeId();
		return (CstrInfo)cstrInfoMap.get(nodeId);
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
		final Label fileNameLbl = new Label("  File: " + nodeInfo.getFileNameWithExt());
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
				final CstrInfo cinfo = (CstrInfo)cstrInfoMap.get(id);
				final String textName = cinfo.getTextName();
				final String ref = cinfo.getRef();
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
			// check for the already opened first
			final String id = selected.getSecond();
			final Stage existing = childViewerMap.get(id);
			boolean openNewOne = false;
			if (existing != null) {
				// check whether it is the right doc
				final CstrHtmlViewer viewer = (CstrHtmlViewer)existing.getScene().getRoot();
				final TocTreeNode docNode = viewer.getDocNode();
				if (id.equals(docNode.getNodeId())) {
					// if there it is, just show the existing
					existing.show();
					existing.toFront();
				} else {
					childViewerMap.remove(existing);
					openNewOne = true;
				}
			} else {
				openNewOne = true;
			}
			if (openNewOne) {
				final CstrInfo cinfo = (CstrInfo)cstrInfoMap.get(id);
				String textName = selected.getFirst();
				if (textName.indexOf(" ") == 1)
					textName = textName.substring(2);
				final TocTreeNode ttn = new SimpleTocTreeNode(corpus, id, textName, cinfo.getFileNameWithExt());
				if (Utilities.checkFileExistence(ttn.getNodeFile())) {
					final Stage childViewer = ReaderUtilities.openPaliHtmlViewer(ttn);
					childViewerMap.put(id, childViewer);
				}
			}
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

	public boolean isSyncSelected() {
		return syncButton.isSelected();
	}

	public void loadContent() {
		final String bodyText = ReaderUtilities.readGzHTMLBody(thisDoc.getNodeFile(), thisDoc.getCorpus().getEncoding().getCharset());
		pageBody = formatText(bodyText);
		final String transformerJS = ReaderUtilities.getStringResource(ReaderUtilities.TRANSFORMER_JS);
		final String cstrJS = ReaderUtilities.getStringResource(ReaderUtilities.CSTR_JS);
		final String pageContent = ReaderUtilities.makeHTML(pageBody, transformerJS + cstrJS);
		setContent(pageContent);
	}

	private String formatText(final String text) {
		headList.clear();
		pnumList.clear();
		gathaList.clear();
		final StringBuilder result = new StringBuilder();
		final String[] lines = text.split("\\n");
		final Pattern headPatt = Pattern.compile("^<h([0-9])>(.*?)</h\\1>");
		final Pattern pnumPatt = Pattern.compile("^([0-9:-]+)\\. *(.*)$");
		final Pattern notePatt = Pattern.compile("(\\[.*?\\])");
		int headCounter = 0;
		int pnumCounter = 0;
		int gathaCounter = 0;
		for (final String line : lines) {
			String thisLine = "";
			if (line.startsWith("<!--")) continue;
			if (line.trim().isEmpty()) continue;
			// note in []
			thisLine = notePatt.matcher(line).replaceAll("<span class='note'>$1</span>");
			final Matcher headMatcher = headPatt.matcher(thisLine);
			if (headMatcher.matches()) {
				// all h
				final String headLevelStr = headMatcher.group(1);
				final int headLevel = Integer.parseInt(headLevelStr);
				final String headText = headMatcher.group(2);
				String idStr = "";
				if (headLevel > 2 && headLevel < 6) {
					// show only h3, h4, h5 in the heading nav
					final String[] headItem = new String[] { headText, headLevelStr, "" + headCounter };
					headList.add(headItem);
					idStr = " id='jumptarget-h" + headCounter + "'";
					headCounter++;
				}
				result.append("<h" + headLevel + idStr + ">" + headText + "</h" + headLevelStr + ">").append("\n");
			} else if (thisLine.startsWith("<div")) {
				// all div
				if (thisLine.indexOf("class=\"gatha1\"") > -1) {
					// for gatha nav, use only gatha1
					// remove notes first
					String gathaStr = thisLine.replaceAll("<span class='note'>.*?</span>", "");
					gathaStr = gathaStr.replaceAll("</?b>", "").replaceAll("</?div.*?>", "").trim();
					gathaList.add(gathaStr);
					final String theLine = thisLine.replaceFirst("^<div ", "<div id='jumptarget-g" + gathaCounter + "' ");
					gathaCounter++;
					result.append(theLine).append("\n");
				} else {
					result.append(thisLine).append("\n");
				}
			} else {
				// text body
				// test for paranum first
				final Matcher pnumMatcher = pnumPatt.matcher(thisLine);
				String theLine = thisLine;
				if (pnumMatcher.matches()) {
					final String pnumStr = pnumMatcher.group(1);
					final String paraText = pnumMatcher.group(2);
					pnumList.add(pnumStr);
					theLine = "<span class='paranum' id='jumptarget-p" + pnumCounter + "'>" + pnumStr + "</span>. " + paraText;
					pnumCounter++;
				}
				result.append("<p class='bodytext'>").append(theLine).append("</p>").append("\n");
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

	private static String getUpperRelativeDoc(final String id) {
		return cstrInfoMap.values().stream()
						.filter(x -> ((CstrInfo)x).hasCommentary(id))
						.map(x -> ((CstrInfo)x).getId())
						.findFirst()
						.orElse("");
	}

	private static List<String> getUpperRelativeDocChain(final String id) {
		final LinkedList<String> result = new LinkedList<>();
		String upperId = getUpperRelativeDoc(id);
		while (!upperId.isEmpty()) {
			result.addFirst(upperId);
			upperId = getUpperRelativeDoc(upperId);
		}
		return result;
	}

	private void setDocToc(final LeftListType listType) {
		currLeftListType = listType;
		final List<String> docToc = new ArrayList<>();
		if (listType == LeftListType.HEADING) {
			for (final String[] headItem : headList) {
				final int level = Integer.parseInt(headItem[1]);
				String spaces = " ";
				int start = 3;
				while (level - start > 0) {
					spaces = spaces + " ";
					start++;
				}
				final String indent = level > 3 ? spaces : "";
				docToc.add(indent + headItem[0]);
			}
		} else if (listType == LeftListType.PARANUM) {
			docToc.addAll(pnumList);
		} else if (listType == LeftListType.GATHA) {
			docToc.addAll(gathaList);
		}
		docTocList.setAll(docToc);
		docTocListView.scrollTo(0);
	}

	public void updateClickedObject(final String text) {
		String result = text.trim();
		result = result.length() > 1
					? showNotes
						? result
						: result.replaceAll("\\[.*?\\]", "")
					: "";
		clickedText.set(result);
	}

	private void setShowNotes() {
		webEngine.executeScript("showNotes("+(showNotes?1:0)+")");
	}
	
	private void synchronizeChildViewers() {
		if (currLeftListType != LeftListType.PARANUM) return;
		int ind = docTocListView.getSelectionModel().getSelectedIndex();
		if (ind < 0)
			ind = 0;
		final String pnumStr = docTocList.get(ind);
		synchronizeChildViewers(pnumStr);
	}

	public void synchronizeChildViewers(final String pnumStr) {
		if (!nodeInfo.isLinkable()) return;
		for (final Stage win : childViewerMap.values()) {
			final CstrHtmlViewer viewer = (CstrHtmlViewer)win.getScene().getRoot();
			viewer.gotoParaNum(pnumStr);
			if (viewer.isSyncSelected())
				viewer.synchronizeChildViewers(pnumStr);
		}
	}

	public void gotoParaNum(final String targetPnum) {
		final ParaNum target = new ParaNum(targetPnum);
		int idToGo = -1;
		for (int i = 0; i < pnumList.size(); i++) {
			final ParaNum pnum = new ParaNum(pnumList.get(i));
			if (pnum.hasCoverageOf(target)) {
				idToGo = i;
				break;
			}
		}
		if (idToGo > 0) {
			final String command = "jumpTo('p" + idToGo + "')";
			webEngine.executeScript(command);
		}
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
			final CstrInfo nodeInfo = (CstrInfo)cstrInfoMap.get(nodeId);
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
				final CstrInfo cinfo = (CstrInfo)cstrInfoMap.get(id);
				final String textName = cinfo.getTextName();
				final String ref = cinfo.getRef();
				final String fullName = textName + " (" + ref + ")";
				result.add(new StringPair(indent + ReaderUtilities.bulletMap.get(level) + " " + fullName, id));
			}
			return result;
		}
	}

	static class ParaNum {
		private final int start;
		private final int end;
		private final int group;
		public ParaNum (final String input) {
			// input might be in range with group number,
			// for example, 20-99:1 or just a single number
			final int colonPos = input.indexOf(":");
			group = colonPos > -1 ? Integer.parseInt(input.substring(colonPos + 1)) : 0;
			final String numBare = colonPos > -1 ? input.substring(0, colonPos) : input;
			final int hyPos = numBare.indexOf("-");
			final String[] numArr = hyPos > -1 ? numBare.split("-") : new String[] { numBare, numBare };
			start = Integer.parseInt(numArr[0]);
			end = Integer.parseInt(numArr[1]);
		}
		public int getStart() {
			return start;
		}
		public int getEnd() {
			return end;
		}
		public boolean isAtomic() {
			return start == end;
		}
		public boolean hasGroup() {
			return group != 0;
		}
		public boolean hasCoverageOf(final ParaNum pnum) {
			boolean result = false;
			if (pnum.isAtomic()) {
				result = hasCoverageOf(pnum.getStart());
			} else {
				final int pStart = pnum.getStart();
				final int pEnd = pnum.getEnd();
				if (isAtomic())
					result = hasCoverageOf(pStart) || hasCoverageOf(pEnd);
				else
					result = hasCoverageOf(pStart) && hasCoverageOf(pEnd);
			}
			return result;
		}
		public boolean hasCoverageOf(final int num) {
			return num >= start && num <= end;
		}
		@Override
		public String toString() {
			final String grpStr = group == 0 ? "" : ":" + group;
			final String numStr = start == end ? "" + start : start + "-" + end;
			return numStr + grpStr;
		}
	}

}

