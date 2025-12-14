/*
 * SrtHtmlViewer.java
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

import org.w3c.dom.html.HTMLDocument;
import org.w3c.dom.html.HTMLElement;

import javafx.application.Platform;
import javafx.scene.control.*;
import javafx.scene.Node;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Priority;
import javafx.scene.input.*;
import javafx.collections.ObservableList;
import javafx.collections.FXCollections;
import javafx.beans.property.*;
import javafx.beans.value.ChangeListener;

import javafx.concurrent.Worker;
import netscape.javascript.JSObject;

/** 
 * The viewer of Siam Rath Tipitaka.
 * @author J.R. Bhaddacak
 * @version 3.6
 * @since 3.0
 */
public class SrtHtmlViewer extends PaliHtmlViewer {
	static final double DIVIDER_POSITION_LEFT = 0.12;
	private final SplitPane splitPane = new SplitPane();
	private final BorderPane leftPane = new BorderPane();
	private final ObservableList<String> docPageList = FXCollections.<String>observableArrayList();
	private final ListView<String> docPageListView = new ListView<>(docPageList);
	private String originalText = "";
	private String dehyphenatedText = "";
	private String recentJS = "";
	private boolean ptsLayout = true;
	private final ComboBox<String> cbTextLayout = new ComboBox<>();
	private final ToggleButton dehyphenButton = new ToggleButton("", new TextIcon("screwdriver-wrench", TextIcon.IconSet.AWESOME));
	private final InfoPopup helpPopup = new InfoPopup();

	public SrtHtmlViewer(final TocTreeNode node, final String strToLocate) {
		super(node);
		final ViewerFXHandler fxHandler = new ViewerFXHandler(this);
		webEngine.getLoadWorker().stateProperty().addListener((prop, oldState, newState) -> {
			if (newState == Worker.State.SUCCEEDED) {
				JSObject jsWindow = (JSObject)webEngine.executeScript("window");
				jsWindow.setMember("fxHandler", fxHandler);
				webEngine.executeScript("init()");
				setViewerTheme(Utilities.getSetting("theme"));
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
		dehyphenButton.setTooltip(new Tooltip("Dehyphenated on/off"));
		dehyphenButton.setOnAction(actionEvent -> {
			loadContent(displayScript.get());
		});
		toolBar.getItems().addAll(new Separator(), toggleLeftPaneButton, recentJumpButton, dehyphenButton);
		// help button
		final Button helpButton = new Button("", new TextIcon("circle-question", TextIcon.IconSet.AWESOME));
		helpButton.setOnAction(actionEvent -> helpPopup.showPopup(helpButton, InfoPopup.Pos.BELOW_RIGHT, true));
		toolBar.getItems().add(helpButton);

		// some init
		helpPopup.setContentWithText(ReaderUtilities.getTextResource("info-srtviewer.txt"));
		helpPopup.setTextWidth(Utilities.getRelativeSize(50));

		// compose SplitPane, the left pane is not shown at start
		splitPane.getItems().addAll(textPane);	
		setCenter(splitPane);
		init(node, strToLocate);
	}

	public void init(final TocTreeNode node, final String strToLocate) {
		super.init(node);
		final Corpus corpus = node.getCorpus();
		Platform.runLater(() ->	{
			dehyphenButton.setSelected(false);
			loadContent(corpus.getScript());
			initFindInput();
			setInitialStringToLocate(strToLocate);
		});
	}

	@Override
	public void convertScript() {
		loadContent(displayScript.get());
	}

	public void loadContent(final Utilities.PaliScript script) {
		String filename =  thisDoc.getNodeFileName();
		final Corpus corpus = thisDoc.getCorpus();
		final Utilities.PaliScript srcScript = corpus.getScript();
		final ScriptTransliterator.EngineType romanDef = 
						ScriptTransliterator.EngineType.fromCode(Utilities.getSetting("roman-translit"));
		if (originalText.isEmpty())
			originalText = ReaderUtilities.readTextFromZip(filename, corpus);
		if (dehyphenatedText.isEmpty())
			dehyphenatedText = dehyphenate(originalText);
		final String displayText = dehyphenButton.isSelected() ? dehyphenatedText : originalText;
		pageBody = "<body>\n" + formatSrtDoc(displayText) + "\n</body>";
		pageBody = script == srcScript
					? pageBody
					: ScriptTransliterator.translitPaliScript(pageBody, srcScript, script, romanDef, alsoConvertNumber, false);
		final String srtJS = ReaderUtilities.getStringResource(ReaderUtilities.SRT_JS);
		final String pageContent = ReaderUtilities.makeHTML(pageBody, srtJS);
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

	private String formatSrtDoc(final String text) {
		final StringBuilder result = new StringBuilder();
		result.append(addPTag(text));
		return result.toString();
	}

	private String addPTag(final String text) {
		final StringBuilder result = new StringBuilder();
		final String[] lines = text.split("\\r?\\n");
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

	private String dehyphenate(final String text) {
		final List<int[]> hyphenedList = getHyphenedList(text);
		final String[] lines = text.split("\\r?\\n");
		for (int i = 0; i < hyphenedList.size(); i++) {
			final String firstLine = lines[hyphenedList.get(i)[0]].stripTrailing();
			final String secondLine = lines[hyphenedList.get(i)[1]].trim();
			final int spacePos = secondLine.indexOf(" ");
			final String secondFirstWord = spacePos > -1 ? secondLine.substring(0, spacePos) : secondLine;
			final String theRest = spacePos > -1 ? secondLine.substring(spacePos) : "";
			if (!secondFirstWord.isEmpty() && !firstLine.isEmpty()) {
				lines[hyphenedList.get(i)[0]] = firstLine.substring(0, firstLine.length() - 1) + secondFirstWord;
				lines[hyphenedList.get(i)[1]] = theRest;
			}

		}
		return String.join("\n", lines);
	}

	private List<int[]> getHyphenedList(final String text) {
		final List<int[]> result = new ArrayList<>();
		final String[] lines = text.split("\\r?\\n");
		final Pattern hyphenPatt = Pattern.compile("^.+[^ -;:)]-$");
		for (int i = 0; i < lines.length; i++) {
			final String line = lines[i].trim();
			final Matcher hyphenMatcher = hyphenPatt.matcher(line);
			if (hyphenMatcher.matches()) {
				for (int j = i + 1; j < lines.length; j++) {
					final String next = lines[j].trim();
					if (next.isEmpty() || next.startsWith("[page") || next.startsWith("#") || next.startsWith("*")) {
						continue;
					} else {
						final int[] arr = { i, j }; 
						result.add(arr);
						break;
					}
				}
			}
		}
		return result;
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

