/*
 * Cst4HtmlViewer.java
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
import java.util.stream.*;

import org.w3c.dom.html.HTMLDocument;
import org.w3c.dom.html.HTMLElement;

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
 * The viewer of Pali texts in CST4 collection.
 * The format is XML agreeable to VRI's CSCD compilation format.
 * Transformation from XML to HTML is done by XSLT.
 * @author J.R. Bhaddacak
 * @version 3.4
 * @since 3.0
 */
public class Cst4HtmlViewer extends PaliHtmlViewer {
	static enum LeftListType { HEADING, PARANUM, GATHA }
	static final double DIVIDER_POSITION_LEFT = 0.2;
	static final double DIVIDER_POSITION_RIGHT = 0.8;
	private final SplitPane splitPane = new SplitPane();
	private final BorderPane rightPane = new BorderPane();
	private final BorderPane leftPane = new BorderPane();
	private final ObservableList<String> docTocList = FXCollections.<String>observableArrayList();
	private final ListView<String> docTocListView;
	private final HashSet<Integer> titleIdSet = new HashSet<>();
	private final HashSet<Integer> subheadIdSet = new HashSet<>();
	private final ToggleButton showNoteButton;
	private final ToggleButton showXRefButton;
	private LeftListType currLeftListType = LeftListType.HEADING;
	private static Map<String, DocumentInfo> cst4InfoMap;
	private boolean showNotes = true;
	private boolean showXref = false;
	private String recentJS = "";

	public Cst4HtmlViewer(final TocTreeNode node, final String strToLocate) {
		super(node);
		webEngine.setUserStyleSheetLocation(ReaderUtilities.class.getResource(ReaderUtilities.CST4_CSS).toExternalForm());
		
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
						if (currLeftListType==LeftListType.HEADING && subheadIdSet.contains(index) && !titleIdSet.isEmpty())
							this.setText("  - " + value);
						else
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
		// add context menus to the main pane
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
		showXRefButton = new ToggleButton("", new TextIcon("hashtag", TextIcon.IconSet.AWESOME));
		showXRefButton.setTooltip(new Tooltip("Show/hide publication references"));
		showXRefButton.setSelected(showXref);
		showXRefButton.setOnAction(actionEvent -> {
			final boolean selected = showXRefButton.isSelected();
			showXref = selected;
			setShowXref();
		});
		final Button helpButton = new Button("", new TextIcon("circle-question", TextIcon.IconSet.AWESOME));
		helpButton.setOnAction(actionEvent -> helpInfoPopup.showPopup(helpButton, InfoPopup.Pos.BELOW_RIGHT, true));
		toolBar.getItems().addAll(new Separator(), toggleLeftPaneButton, toggleFullViewButton, toggleRightPaneButton
								, new Separator(), recentJumpButton, showNoteButton, showXRefButton, helpButton
								);

		setCenter(splitPane);
		helpInfoPopup.setContentWithText(ReaderUtilities.getTextResource("info-cst4viewer.txt"));
		helpInfoPopup.setTextWidth(Utilities.getRelativeSize(38));
		init(node, strToLocate);
	}

	public void init(final TocTreeNode node, final String strToLocate) {
		super.init(node);
		Platform.runLater(() ->	{
			cst4InfoMap = node.getCorpus().getDocInfoMap();
			showNoteButton.setSelected(true);
			showXRefButton.setSelected(false);
			rightPane.setCenter(createInfoBox());
			loadContent();
			initFindInput();
			setInitialStringToLocate(strToLocate);
		});
	}

	private VBox createInfoBox() {
		final StringBuilder infoContent = new StringBuilder(); // for clipboard copy
		final VBox selfInfoBox = new VBox();
		final String nodeId = thisDoc.getNodeId();
		final Corpus corpus = thisDoc.getCorpus();
		final Cst4Info nodeInfo = (Cst4Info)cst4InfoMap.get(nodeId);
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
		final String fname = nodeInfo.getFileNameWithExt();
		final Label fileNameLbl = new Label("  File: " + fname.substring(fname.lastIndexOf("/") + 1));
		fileNameLbl.setStyle("-fx-font-size:0.8em;");
		infoContent.append(fileNameLbl.getText()).append("\n");
		selfInfoBox.getChildren().add(fileNameLbl);
		// add relative links information
		final String currLinkId = nodeInfo.getLinkId();
		final ObservableList<DocumentInfo> relLinkInfoList = FXCollections.<DocumentInfo>observableArrayList();
		final ListView<DocumentInfo> relLinkInfoListView = new ListView<>(relLinkInfoList);
		if (!currLinkId.isEmpty()) {
			// if the doc has related docs, list them
			final List<DocumentInfo> relDocs = cst4InfoMap.values().stream()
											.filter(x -> ((Cst4Info)x).getLinkId().equals(currLinkId))
											.collect(Collectors.toList());
			relLinkInfoList.setAll(relDocs);
			relLinkInfoListView.setCellFactory((ListView<DocumentInfo> lv) -> {
				return new ListCell<DocumentInfo>() {
					@Override
					public void updateItem(DocumentInfo item, boolean empty) {
						super.updateItem(item, empty);
						final int index = this.getIndex();
						this.setGraphic(null);
						if (empty) {
							this.setText(null);
							this.setTooltip(null);
						} else {
							final String textName = item.getTextName();
							this.setText(textName);
							this.setTooltip(new Tooltip(textName));
						}
						if (!empty && item.getId().equals(nodeInfo.getId()))
							this.getStyleClass().add("emphasized");
						this.setStyle("-fx-padding: 0px 0px 0px 3px");
					}
				};
			});
		}
		// use VBox as the container of the right pane
		final VBox resultBox = new VBox();
		VBox.setVgrow(relLinkInfoListView, Priority.ALWAYS);
		final ObservableList<Node> boxChildren = resultBox.getChildren();
		selfInfoBox.getStyleClass().add("bordered-box");
		boxChildren.add(selfInfoBox);
		// add relative links
		if (!currLinkId.isEmpty()) {
			final StackPane reldocHead = new StackPane();
			reldocHead.getChildren().add(new Label("Related documents"));
			reldocHead.getStyleClass().add("head-inverted");
			boxChildren.add(reldocHead);
			boxChildren.add(relLinkInfoListView);
			// add context menus to the list
			final ContextMenu relLinkPopupMenu = new ContextMenu();
			final MenuItem openMenuItem = new MenuItem("Open");
			openMenuItem.setOnAction(actionEvent -> {
				int ind = relLinkInfoListView.getSelectionModel().getSelectedIndex();
				final DocumentInfo selected = relLinkInfoListView.getSelectionModel().getSelectedItem();
				ReaderUtilities.openPaliHtmlViewer(selected.toTocTreeNode());
			});
			relLinkPopupMenu.getItems().addAll(openMenuItem);
			// add mouse listener
			relLinkInfoListView.setOnMouseClicked(mouseEvent -> {
				final int ind = relLinkInfoListView.getSelectionModel().getSelectedIndex();
				final DocumentInfo selected = relLinkInfoListView.getSelectionModel().getSelectedItem();
				if (selected != null) {
					if (selected.getId().equals(nodeInfo.getId()))
						relLinkPopupMenu.hide();
					else
						relLinkPopupMenu.show(relLinkInfoListView, mouseEvent.getScreenX(), mouseEvent.getScreenY());
				} else {
					relLinkPopupMenu.hide();
				}
			});
		}
		return resultBox;
	}

	public void loadContent() {
		final Corpus corpus = thisDoc.getCorpus();
		if (corpus.getCollection() == Corpus.Collection.CSTDEVA) {
			pageBody = ReaderUtilities.readCstDevaXML(thisDoc);
		} else {
			pageBody = ReaderUtilities.readCst4XML(thisDoc);
		}
		final String transformerJS = ReaderUtilities.getStringResource(ReaderUtilities.TRANSFORMER_JS);
		final String cst4JS = ReaderUtilities.getStringResource(ReaderUtilities.CST4_JS);
		final String pageContent = ReaderUtilities.makeHTML(pageBody, transformerJS + cst4JS);
		setContent(pageContent);
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

	private void setDocToc(final LeftListType listType) {
		currLeftListType = listType;
		final HTMLDocument hdoc = (HTMLDocument)webEngine.getDocument();
		final HTMLElement body = hdoc.getBody();
		final List<String> docToc = new ArrayList<>();
		org.w3c.dom.NodeList nlist;
		org.w3c.dom.Node nd = null;
		int ind = 0;
		if (listType == LeftListType.HEADING) {
			nlist = body.getElementsByTagName("P");
			for (int i=0; i<nlist.getLength(); i++) {
				nd = nlist.item(i);
				if (nd.hasAttributes()) {
					final org.w3c.dom.NamedNodeMap nnm = nd.getAttributes();
					final String clss = nnm.getNamedItem("class").getTextContent();
					if (clss.equals("title") || clss.equals("subhead")) {
						if (clss.equals("title"))
							titleIdSet.add(ind);
						else if (clss.equals("subhead"))
							subheadIdSet.add(ind);
						docToc.add(nd.getTextContent());
						final org.w3c.dom.Attr attrId = hdoc.createAttribute("id");
						attrId.setValue("jumptarget-h" + ind);
						nnm.setNamedItem(attrId);
						ind++;
					}
				}
			}
		} else if (listType == LeftListType.PARANUM) {
			nlist = body.getElementsByTagName("SPAN");
			for (int i=0; i<nlist.getLength(); i++) {
				nd = nlist.item(i);
				if (nd.hasAttributes()) {
					final org.w3c.dom.NamedNodeMap nnm = nd.getAttributes();
					final String clss = nnm.getNamedItem("class").getTextContent();
					if (clss.equals("paranum")) {
						docToc.add(nd.getTextContent());
						final org.w3c.dom.Attr attrId = hdoc.createAttribute("id");
						attrId.setValue("jumptarget-p" + ind);
						nnm.setNamedItem(attrId);
						ind++;
					}
				}
			}
		} else if (listType == LeftListType.GATHA) {
			nlist = body.getElementsByTagName("P");
			for (int i=0; i<nlist.getLength(); i++) {
				nd = nlist.item(i);
				if (nd.hasAttributes()) {
					final org.w3c.dom.NamedNodeMap nnm = nd.getAttributes();
					final String clss = nnm.getNamedItem("class").getTextContent();
					if (clss.equals("gatha1")) {
						String text = nd.getTextContent().trim();
						text = text.replaceAll("\\[.*?\\]", "").trim();
						int cutPos = text.indexOf(",");
						if (cutPos == -1)
							cutPos = text.indexOf(";");
						if (cutPos == -1)
							text = text.substring(0);
						else
							text = text.substring(0, cutPos);
						text = text.replaceAll("[ ]{2,}", " ");
						text = text.replace("‘", "");
						docToc.add(text);
						final org.w3c.dom.Attr attrId = hdoc.createAttribute("id");
						attrId.setValue("jumptarget-g" + ind);
						nnm.setNamedItem(attrId);
						ind++;
					}
				}
			}
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
		webEngine.executeScript("showNotes(" + (showNotes ? 1 : 0) + ")");
	}
	
	private void setShowXref() {
		final String command = "showXRef(" + (showXref ? 1 : 0) + ");setXrefColor('" + viewerTheme+"'," + style.ordinal() + ");";
		webEngine.executeScript(command);		
	}

}

