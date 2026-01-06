/*
 * SktGretilHtmlViewer.java
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

package paliplatform.reader;

import paliplatform.base.*;

import java.util.*;

import org.w3c.dom.html.HTMLDocument;
import org.w3c.dom.html.HTMLElement;

import javafx.application.Platform;
import javafx.scene.control.*;
import javafx.scene.Node;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.input.*;
import javafx.collections.ObservableList;
import javafx.collections.FXCollections;
import javafx.beans.property.*;
import javafx.beans.value.ChangeListener;

import javafx.concurrent.Worker;
import netscape.javascript.JSObject;

/** 
 * The viewer of Sanskrit texts from GRETIL.
 * @author J.R. Bhaddacak
 * @version 3.7
 * @since 3.7
 */
public class SktGretilHtmlViewer extends PaliHtmlViewerBase {
	static final double DIVIDER_POSITION_LEFT = 0.22;
	private final SplitPane splitPane = new SplitPane();
	private final BorderPane leftPane = new BorderPane();
	private final ObservableList<String> docHeadList = FXCollections.<String>observableArrayList();
	private final ListView<String> docHeadListView = new ListView<>(docHeadList);
	private final HBox extraButtonBox = new HBox();
	private final InfoPopup helpPopup = new InfoPopup();
	private String recentJS = "";
	private String initialStringToLocate = "";

	public SktGretilHtmlViewer(final String docId, final String strToLocate) {
		final ViewerFXHandler fxHandler = new ViewerFXHandler(this);
	 	webEngine.getLoadWorker().stateProperty().addListener((prop, oldState, newState) -> {
			if (newState == Worker.State.SUCCEEDED) {
				JSObject jsWindow = (JSObject)webEngine.executeScript("window");
				jsWindow.setMember("fxHandler", fxHandler);
				webEngine.executeScript("init()");
				if (!initialStringToLocate.isEmpty())
					findSingle(initialStringToLocate);
				setViewerTheme(Utilities.getSetting("theme"));
				setViewerFont();
			}
		});	
		extraButtonBox.setSpacing(5);
		// prepare the left pane's content (used for index display)
		docHeadListView.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> {
			final int ind = docHeadListView.getSelectionModel().getSelectedIndex();
			final String command = "jumpTo('" + ind + "')";
			webEngine.executeScript(command);
			recentJS = command;
		});
		// add left pane's toolbar
		final ToolBar leftpaneToolbar = new ToolBar();
		leftpaneToolbar.getItems().add(new Label("Go to section:"));
		// set left components
		leftPane.setTop(leftpaneToolbar);
		leftPane.setCenter(docHeadListView);
		final Button helpButton = new Button("", new TextIcon("circle-question", TextIcon.IconSet.AWESOME));
		helpButton.setOnAction(actionEvent -> helpPopup.showPopup(helpButton, InfoPopup.Pos.BELOW_RIGHT, true));
		toolBar.getItems().addAll(new Separator(), extraButtonBox, helpButton);
		
		// some init
		sanskritMode = true;
		findBox.setSanskritMode(true);
		final String inputMethod = Utilities.getSetting("sanskrit-input-method");
		findBox.getFindTextInput().setInputMethod(PaliTextInput.InputMethod.valueOf(inputMethod));
		helpPopup.setContentWithText(ReaderUtilities.getTextResource("info-sktgretilviewer.txt"));
		helpPopup.setTextWidth(Utilities.getRelativeSize(32));
		
		// compose SplitPane, the left pane is not shown at start
		splitPane.getItems().addAll(textPane);	
		setCenter(splitPane);
		init(docId, strToLocate);
	}

	public void init(final String docId, final String strToLocate) {
		if (theStage != null)
			theStage.setTitle("");
		updateLookupDict();
		Platform.runLater(() ->	{
			extraButtonBox.getChildren().clear();
			if (docId.isEmpty()) {
				webEngine.setUserStyleSheetLocation(ReaderUtilities.class.getResource(ReaderUtilities.SKTGRETIL_IND_CSS).toExternalForm());
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
				extraButtonBox.getChildren().addAll(toggleLeftPaneButton, recentJumpButton);
				loadIndex();
			} else {
				webEngine.setUserStyleSheetLocation(ReaderUtilities.class.getResource(ReaderUtilities.SKTGRETIL_DOC_CSS).toExternalForm());
				// add new buttons to tool bar
				final Button homeButton = new Button("", new TextIcon("house", TextIcon.IconSet.AWESOME));
				homeButton.setTooltip(new Tooltip("Go to the header section"));
				homeButton.setOnAction(actionEvent -> webEngine.executeScript("jumpTo('0')"));
				final Button textButton = new Button("", new TextIcon("scroll", TextIcon.IconSet.AWESOME));
				textButton.setTooltip(new Tooltip("Go to the text section"));
				textButton.setOnAction(actionEvent -> webEngine.executeScript("jumpTo('1')"));
				final Button dictUpdateButton = new Button("", new TextIcon("book-update", TextIcon.IconSet.CUSTOM));
				dictUpdateButton.setTooltip(new Tooltip("Update lookup dict"));
				dictUpdateButton.setOnAction(actionEvent -> updateLookupDict());
				extraButtonBox.getChildren().addAll(homeButton, textButton, dictUpdateButton);
				final Corpus corpus = ReaderUtilities.corpusMap.get(Corpus.Collection.SKT);
				final DocumentInfo docInfo = corpus.getDocInfo(docId);
				loadContent(corpus, docInfo);
				final List<TextGroup> groupList = corpus.getTextBasketList();
				final TextGroup grp = groupList.stream().filter(x -> x.getAbbrev().equals(docInfo.getGroup())).findFirst().orElse(null);
				final String grpStr = grp == null ? "" : " (" + grp.getName() + ")";
				theStage.setTitle(docId + grpStr);
			}
			initFindInput();
			setInitialStringToLocate(strToLocate);
		});
	}

	public void setInitialStringToLocate(final String token) {
		initialStringToLocate = token;
		if (!token.isEmpty())
			setFindInputText(token);
	}

	private void loadIndex() {
		pageBody = "\n<body>\n" + ReaderUtilities.sktGretilIndexHtml + "\n</body>\n";
		final String sktGretilJS = ReaderUtilities.getStringResource(ReaderUtilities.SKTGRETIL_JS);
		final String pageContent = ReaderUtilities.makeHTML(pageBody, sktGretilJS);
		setContent(pageContent);
		docHeadList.addAll(ReaderUtilities.sktGretilIndexHeadList);
	}

	public void loadContent(final Corpus corpus, final DocumentInfo docInfo) {
		final String filename =  docInfo.getFileNameWithExt();
		final String sktGretilJS = ReaderUtilities.getStringResource(ReaderUtilities.SKTGRETIL_JS);
		final String wholeText = ReaderUtilities.readTextFromZip(filename, corpus);
		pageBody = formatSktGretilDoc(wholeText);
		final String pageContent = ReaderUtilities.makeHTML(pageBody, sktGretilJS);
		setContent(pageContent);
	}

	public void clearContent() {
		docHeadListView.scrollTo(0);
		final ObservableList<Node> ol = splitPane.getItems();
		if (ol.contains(leftPane))
			ol.remove(leftPane);
		super.clearContent();
	}

	private String formatSktGretilDoc(final String text) {
		final StringBuilder result = new StringBuilder();
		result.append("<body>\n");
		final String[] lines = text.split("\\r?\\n");
		boolean start = false;
		for (int i = 0; i < lines.length; i++) {
			String line = lines[i].trim();
			if (start && !line.isEmpty()) {
				line = line.replaceAll("<a .*?>", "").replaceAll("</a>", "");
				line = line.replace("<h2>Header", "<h2 id='jumptarget-h0'>Header");
				line = line.replace("<h2>Text", "<h2 id='jumptarget-h1'>Text");
				result.append(line).append("<br>").append("\n");
			}
			if (line.contains("<body>"))
				start = true;
			else if (line.contains("</body>"))
				start = false;
		}
		return result.toString();
	}

	public void openSktDoc(final String docId) {
		ReaderUtilities.openWindow(Utilities.WindowType.VIEWER_SKTGRETIL, new Object[] { docId });
	}

	private void updateLookupDict() {
		final String currSktDict = Utilities.getSetting("skt-lookup-dict");
		if (!sktDictTermsMap.containsKey(currSktDict)) {
			final SktService sktServ = (SktService)ReaderUtilities.sktServiceMap.get("paliplatform.sanskrit.SktServiceImp");
			if (sktServ != null) {
				final List<String> termList = new ArrayList<>();
				termList.addAll(sktServ.getSktDictTerms(currSktDict));
				sktDictTermsMap.put(currSktDict, termList);
			}
		}
	}

}


