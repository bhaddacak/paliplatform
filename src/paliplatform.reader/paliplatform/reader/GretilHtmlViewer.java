/*
 * GretilHtmlViewer.java
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

import org.w3c.dom.html.HTMLDocument;
import org.w3c.dom.html.HTMLElement;

import javafx.application.Platform;
import javafx.scene.control.*;
import javafx.scene.Node;
import javafx.scene.layout.BorderPane;
import javafx.scene.input.*;
import javafx.collections.ObservableList;
import javafx.collections.FXCollections;
import javafx.beans.property.*;
import javafx.beans.value.ChangeListener;

import javafx.concurrent.Worker;
import netscape.javascript.JSObject;

/** 
 * The viewer of Pali texts from GRETIL, i.e., for PTS and BJT edition.
 * @author J.R. Bhaddacak
 * @version 3.0
 * @since 3.0
 */
public class GretilHtmlViewer extends PaliHtmlViewer {
	static final double DIVIDER_POSITION_LEFT = 0.12;
	private final SplitPane splitPane = new SplitPane();
	private final BorderPane leftPane = new BorderPane();
	private final ObservableList<String> docPageList = FXCollections.<String>observableArrayList();
	private final ListView<String> docPageListView = new ListView<>(docPageList);
	private String recentJS = "";
	private boolean ptsLayout = true;
	private final ComboBox<String> cbTextLayout = new ComboBox<>();
	private final InfoPopup helpPopup = new InfoPopup();

	public GretilHtmlViewer(final TocTreeNode node) {
		super(node);
		final ViewerFXHandler fxHandler = new ViewerFXHandler(this);
	 	webEngine.getLoadWorker().stateProperty().addListener((prop, oldState, newState) -> {
			if (newState == Worker.State.SUCCEEDED) {
				final int transformable = node.getCorpus().isTransformable() ? 1 : 0;
				JSObject jsWindow = (JSObject)webEngine.executeScript("window");
				jsWindow.setMember("fxHandler", fxHandler);
				webEngine.executeScript("init(" + transformable + ")");
				setViewerTheme(Utilities.settings.getProperty("theme"));
				setViewerFont();
				setDocPages();
			}
		});		
		// prepare the left pane's content
		docPageListView.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> {
			final String page = docPageListView.getSelectionModel().getSelectedItem();
			final String command = "jumpTo('" + page + "')";
			webEngine.executeScript(command);
			recentJS = command;
		});
		// add left pane's toolbar
		final ToolBar leftpaneToolbar = new ToolBar();
		leftpaneToolbar.getItems().add(new Label("Go to page:"));
		// set left components
		leftPane.setTop(leftpaneToolbar);
		leftPane.setCenter(docPageListView);
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
		toolBar.getItems().addAll(new Separator(), toggleLeftPaneButton, recentJumpButton);
		// for PTS layout
		if (thisDoc.getCorpus().getRootName().equals("ptst")) {
			final ChangeListener<String> textLayoutListener = (obs, oldValue, newValue) -> {
					final int ind = cbTextLayout.getSelectionModel().getSelectedIndex();
					ptsLayout = ind == 0;
					loadContent();
			};
			cbTextLayout.getItems().addAll(List.of("PTS layout", "Floating text"));
			cbTextLayout.getSelectionModel().select(0);
			cbTextLayout.getSelectionModel().selectedItemProperty().addListener(textLayoutListener);
			toolBar.getItems().add(cbTextLayout);
		}
		final Button helpButton = new Button("", new TextIcon("circle-question", TextIcon.IconSet.AWESOME));
		helpButton.setOnAction(actionEvent -> helpPopup.showPopup(helpButton, InfoPopup.Pos.BELOW_RIGHT, true));
		toolBar.getItems().add(helpButton);
		
		// some init
		helpPopup.setContentWithText(ReaderUtilities.getTextResource("info-gretilviewer.txt"));
		helpPopup.setTextWidth(Utilities.getRelativeSize(31));
		
		// compose SplitPane, the left pane is not shown at start
		splitPane.getItems().addAll(textPane);	
		setCenter(splitPane);
		init(node);
	}

	public void init(final TocTreeNode node) {
		super.init(node);
		Platform.runLater(() ->	{
			loadContent();
			initFindInput();
		});
	}

	public void loadContent() {
		String filename =  thisDoc.getNodeFileName();
		final Corpus corpus = thisDoc.getCorpus();
		if (corpus.getRootName().equals("ptst") && !ptsLayout)
			filename = toPTSFloatingTextFileName(filename);
		pageBody = ReaderUtilities.readHTMLBodyFromZip(filename, corpus);
		pageBody = formatGretilDoc(pageBody);
		final String gretilJS = ReaderUtilities.getStringResource(ReaderUtilities.GRETIL_JS);
		final String pageContent = ReaderUtilities.makeHTML(pageBody, gretilJS);
		setContent(pageContent);
	}

	public void clearContent() {
		docPageListView.scrollTo(0);
		cbTextLayout.getSelectionModel().select(0);
		final ObservableList<Node> ol = splitPane.getItems();
		if (ol.contains(leftPane))
			ol.remove(leftPane);
		super.clearContent();
	}

	private String toPTSFloatingTextFileName(final String ptsFileName) {
		return ptsFileName.substring(0, ptsFileName.length() - 6) + "pu.htm";
	}

	private String formatGretilDoc(final String text) {
		final StringBuilder result = new StringBuilder();
		final int frontStart = text.indexOf("<!--front-start-->");
		final int frontEnd = text.indexOf("<!--front-end-->");
		final int noteStart = text.indexOf("<!--note-start-->");
		final int noteEnd = text.indexOf("<!--note-end-->");
		final int textStart = text.indexOf("<!--text-start-->");
		final int textEnd = text.indexOf("<!--text-end-->");
		if (frontStart > -1 && frontEnd > -1)
			result.append("<button onClick='toggleFront();'>Front Matter</button>");
		if (noteStart > -1 && noteEnd > -1)
			result.append("<button onClick='toggleNote();'>GRETIL's Notes</button>");
		if (frontStart > -1 && frontEnd > -1) {
			result.append("<br><blockquote id='frontmatter' style='display:none;'>");
			result.append(text.substring(frontStart, frontEnd));
			result.append("</blockquote>");
		}
		if (noteStart > -1 && noteEnd > -1) {
			result.append("<blockquote id='gretilnotes' style='display:none;'>");
			final String note = text.substring(noteStart, noteEnd);
			final String hr = note.indexOf("<HR>") > -1 ? "<HR>" : "<hr>";
			result.append(note.substring(note.indexOf(hr) + hr.length(), note.lastIndexOf(hr)));
			result.append("</blockquote>");
		}
		if (textStart > -1 && textEnd > -1) {
			result.append(addPTag(text.substring(textStart, textEnd)));
		}
		return result.toString();
	}

	private String addPTag(final String text) {
		final StringBuilder result = new StringBuilder();
		final String[] lines = text.split("<br>|<BR>");
		boolean isFirst = true;
		for (final String line : lines) {
			if (line.toLowerCase().contains(thisDoc.getCorpus().getPageString())) {
				if (isFirst) {
					isFirst = false;
				} else {
					result.append("</p>");
				}
				result.append("<p>");
			}
			result.append(line).append("<br>");
		}
		result.append("</p>");
		return result.toString();
	}

	private void setDocPages() {
		final List<String> docPages = new ArrayList<>();
		final HTMLDocument hdoc = (HTMLDocument)webEngine.getDocument();
		final HTMLElement body = hdoc.getBody();
		final org.w3c.dom.NodeList pnlist = body.getElementsByTagName("P");
		for (int i = 0; i < pnlist.getLength(); i++) {
			final org.w3c.dom.Node pnode = pnlist.item(i);
			final org.w3c.dom.NodeList nlist = pnode.getChildNodes();
			for (int n=0; n<nlist.getLength(); n++) {
				final org.w3c.dom.Node nd = nlist.item(n);
				final String text = nd.getTextContent().toLowerCase();
				final String pageStr = thisDoc.getCorpus().getPageString();
				if (text.startsWith(pageStr)) {
					final String pnum = Utilities.getFirstDigit(text.substring(Utilities.getFirstDigitPos(text)));
					final String num = Utilities.removeLeadingZero(pnum);
					docPages.add(num);
					final org.w3c.dom.Attr attrId = hdoc.createAttribute("id");
					attrId.setValue("page-" + num);
					final org.w3c.dom.NamedNodeMap nnm = pnode.getAttributes();
					nnm.setNamedItem(attrId);
				}
			}
		}
		docPageList.setAll(docPages);
	}

}

