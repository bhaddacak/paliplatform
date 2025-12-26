/*
 * PaliHtmlViewerBase.java
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
import paliplatform.dict.*;
import paliplatform.grammar.*;

import java.util.*;
import java.util.stream.*;
import java.util.regex.*;
import java.text.Normalizer;
import java.text.Normalizer.Form;
import java.io.File;

import javafx.application.Platform;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.input.*;
import javafx.beans.property.*;
import javafx.event.*;

import javafx.concurrent.Worker;
import netscape.javascript.JSObject;

/** 
 * The base of generic HTML viewer for Pali texts.
 * 
 * @author J.R. Bhaddacak
 * @version 3.7
 * @since 3.0
 */
public class PaliHtmlViewerBase extends HtmlViewer {
	protected final BorderPane textPane = new BorderPane();
	protected final ViewerToolBar toolBar;
	protected Stage theStage;
	protected final SimpleFindBox findBox = new SimpleFindBox(this);
	private final TextField findInput = findBox.getFindTextField();
	protected String viewerTheme;
	protected Utilities.Style bgStyle; // init needed
	private String lineHeight; // init needed
	protected final SimpleObjectProperty<Utilities.PaliScript> displayScript = new SimpleObjectProperty<>(Utilities.PaliScript.ROMAN);
	private final SimpleBooleanProperty searchTextFound = new SimpleBooleanProperty(false);
	protected final SimpleStringProperty clickedText = new SimpleStringProperty("");
	private final InfoPopup dictInfoPopup = new InfoPopup();
	protected final InfoPopup helpInfoPopup = new InfoPopup();
	protected final ContextMenu contextMenu;
	public int currFontSize;

	public PaliHtmlViewerBase() {
		currFontSize = Integer.valueOf(Utilities.getSetting("viewer-fontsize"));
		webEngine.setUserStyleSheetLocation(ReaderUtilities.class.getResource(ReaderUtilities.PALIHTML_CSS).toExternalForm());
		// Set the member for the browser's window object after the document loads
		final ViewerFXHandler fxHandler = new ViewerFXHandler(this);
	 	webEngine.getLoadWorker().stateProperty().addListener((prop, oldState, newState) -> {
			if (newState == Worker.State.SUCCEEDED) {
				JSObject jsWindow = (JSObject)webEngine.executeScript("window");
				jsWindow.setMember("fxHandler", fxHandler);
				webEngine.executeScript("init()");
				setViewerTheme(Utilities.getSetting("theme"));
				setViewerFont();
				webView.setFontScale(currFontSize/100.0);
			}
		});		
		textPane.setCenter(webView);
		// config Find Box
		final String inputMethod = Utilities.getSetting("pali-input-method");
		findBox.getFindTextInput().setInputMethod(PaliTextInput.InputMethod.valueOf(inputMethod));
		findBox.getPrevButton().disableProperty().bind(searchTextFound.not());
		findBox.getPrevButton().setOnAction(actionEvent -> findNext(-1));
		findBox.getNextButton().disableProperty().bind(searchTextFound.not());
		findBox.getNextButton().setOnAction(actionEvent -> findNext(+1));
		findBox.getCloseButton().setOnAction(actionEvent -> textPane.setBottom(null));
		findBox.getClearFindButton().setOnAction(actionEvent -> clearFindInput());
		findInput.setOnKeyPressed(keyEvent -> {
			if (keyEvent.getEventType() == KeyEvent.KEY_PRESSED) {
				final KeyCode key = keyEvent.getCode();
				if (key == KeyCode.ENTER) {
					if (keyEvent.isShiftDown())
						findNext(-1);
					else
						findNext(+1);
				} else if (key == KeyCode.ESCAPE) {
					clearFindInput();
				} else if (key == KeyCode.SPACE && keyEvent.isControlDown()) {
					findBox.getFindTextInput().rotateInputMethod();
				}
			}
		});
		findInput.textProperty().addListener((obs, oldValue, newValue) -> {
			if (!newValue.isEmpty() && !findBox.isSuspended())
				findNext(Normalizer.normalize(newValue, Form.NFC), +1);
		});
		// add find box first, and remove it (in init); this makes the focus request possible		
		textPane.setBottom(findBox);
		
		// add main components
		// add context menus to the main pane
		contextMenu = new ContextMenu();
		final MenuItem editMenuItem = new MenuItem("Open in text editor");
		editMenuItem.disableProperty().bind(clickedText.isEmpty());
		editMenuItem.setOnAction(actionEvent -> openTextEditor());
		final MenuItem readMenuItem = new MenuItem("Read this portion");
		readMenuItem.disableProperty().bind(clickedText.isEmpty());
		readMenuItem.setOnAction(actionEvent -> openSentenceReader());
		final MenuItem openDictMenuItem = new MenuItem("Open Dictionaries");
		openDictMenuItem.setOnAction(actionEvent -> openDict());
		final MenuItem sendToDictMenuItem = new MenuItem("Send to Pāli Dictionaries");
		sendToDictMenuItem.setOnAction(actionEvent -> sendToDict(false));
		final MenuItem sendToSktDictMenuItem = new MenuItem("Send to Sanskrit Dictionaries");
		sendToSktDictMenuItem.setOnAction(actionEvent -> sendToDict(true));
		final MenuItem calMetersMenuItem = new MenuItem("Calculate meters");
		calMetersMenuItem.disableProperty().bind(clickedText.isEmpty());
		calMetersMenuItem.setOnAction(actionEvent -> calculateMeters());
		final MenuItem analyzeMenuItem = new MenuItem("Analyze this stanza/portion");
		analyzeMenuItem.disableProperty().bind(clickedText.isEmpty());
		analyzeMenuItem.setOnAction(actionEvent -> openAnalyzer());
		contextMenu.getItems().addAll(editMenuItem, readMenuItem, openDictMenuItem, sendToDictMenuItem, sendToSktDictMenuItem,
										calMetersMenuItem, analyzeMenuItem);
		webView.setOnMousePressed(mouseEvent -> {
			if (mouseEvent.getButton() == MouseButton.SECONDARY) {
				contextMenu.show(webView, mouseEvent.getScreenX(), mouseEvent.getScreenY());
			} else {
				contextMenu.hide();
			}
		});		
		// add main toolbar
		toolBar = new ViewerToolBar(this, findInput);
		// configure some buttons first
		toolBar.saveTextButton.setTooltip(new Tooltip("Save selection as text"));
		toolBar.saveTextButton.setOnAction(actionEvent -> saveSelection());
		toolBar.copyButton.setTooltip(new Tooltip("Copy selection to clipboard"));
		toolBar.copyButton.setOnAction(actionEvent -> copySelection());
		// add new buttons
		// find buttons
		final MenuButton findMenu = new MenuButton("", new TextIcon("magnifying-glass", TextIcon.IconSet.AWESOME));
		findMenu.setTooltip(new Tooltip("Find"));
		final MenuItem findMenuItem = new MenuItem("Find");
		findMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.F, KeyCombination.SHORTCUT_DOWN));
		findMenuItem.setOnAction(actionEvent -> openFind());
		final MenuItem findNextMenuItem = new MenuItem("Find Next");
		findNextMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.G, KeyCombination.SHORTCUT_DOWN));
		findNextMenuItem.disableProperty().bind(searchTextFound.not());		
		findNextMenuItem.setOnAction(actionEvent -> findNext(+1));
		final MenuItem findPrevMenuItem = new MenuItem("Find Previous");
		findPrevMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.G, KeyCombination.SHORTCUT_DOWN, KeyCombination.SHIFT_DOWN));
		findPrevMenuItem.disableProperty().bind(searchTextFound.not());		
		findPrevMenuItem.setOnAction(actionEvent -> findNext(-1));
		findMenu.getItems().addAll(findMenuItem, findNextMenuItem, findPrevMenuItem);
		toolBar.getItems().addAll(new Separator(), findMenu);
		setTop(toolBar);
		setCenter(textPane);
		
		// prepare info popup used for dictionary display
		dictInfoPopup.setTextWidth(Utilities.getRelativeSize(25));

		// some other initialization
		bgStyle = Utilities.Style.valueOf(Utilities.getSetting("bgstyle"));
		lineHeight = Utilities.getSetting("lineheight");
		Utilities.initializeDictDB();
		if (ReaderUtilities.simpleServiceMap == null) 
			ReaderUtilities.simpleServiceMap = ReaderUtilities.getSimpleServices();
		if (DictUtilities.sandhiFile == null)
			DictUtilities.sandhiFile = new File(Utilities.ROOTDIR + Utilities.RULESPATH + DictUtilities.SANDHI_LIST);
		if (!DictUtilities.sandhiFile.exists())
			DictUtilities.createSandhiFile();
		DictUtilities.loadSandhiList();
		DictUtilities.loadCPEDTerms(); // used in dict look up		
		Platform.runLater(() -> {
			GrammarUtilities.createDeclPronounsMap();
			GrammarUtilities.createDeclNumbersMap();
		});

		init();
	}
	
	public void init() {
		findBox.init();
		if (theStage != null)
			theStage.setTitle("Pāli Text Viewer");
		Platform.runLater(() -> {
			textPane.setBottom(null);
			GrammarUtilities.createDeclIrrNounsMap(); // this needs CPED to be present
		});			
	}

	// for generic viewer, this has to be called
	public void loadContent() {
		throw new UnsupportedOperationException();
	}

	public void clearContent() {
		setContent(ReaderUtilities.makeHTML(""));
	}

	public void setStage(final Stage stage) {
		theStage = stage;
		theStage.setOnCloseRequest(new EventHandler<WindowEvent>() {  
			@Override
			public void handle(final WindowEvent event) {
				clearContent();
			}
		});
	}

	public void setViewerTheme(final String theme) {
		viewerTheme = theme;
		setTheme();
	}
	
	public void setViewerTheme(final Utilities.Style stl) {
		bgStyle = stl;
		setTheme();
	}
	
	private void setTheme() {
		final String command = "setViewerTheme('" + viewerTheme + "'," + bgStyle.ordinal() + ");";
		webEngine.executeScript(command);
	}

	public Utilities.Style getBGStyle() {
		return bgStyle;
	}
	
	public void setViewerFont() {
		toolBar.resetFont(displayScript.get());
	}
	
	public void setViewerFont(final String fontname) {
		toolBar.setFontMenu(fontname);
		webEngine.executeScript("setFont('{\"name\":\"" + fontname + "\"}')");
	}

	public void setViewerFont(final Utilities.PaliScript script) {
		toolBar.resetFont(script);
	}
	
	public void setLineHeight(final String percent) {
		lineHeight = percent;
		final String command = "setLineHeight('" + lineHeight + "');";
		webEngine.executeScript(command);
	}

	public String getLineHeight() {
		return lineHeight;
	}
	
	public void updateClickedObject(final String text) {
		String result = text.trim();
		result = result.length() > 1 ? result : "";
		clickedText.set(result);
	}

	private void openDict() {
		copySelection();
		final Clipboard cboard = Clipboard.getSystemClipboard();
		final String text = cboard.hasString() ? cboard.getString().trim() : "";
		final String term = Utilities.getUsablePaliTerm(Utilities.convertToRomanPali(text));
		final Object[] args = { term };
		DictUtilities.openWindow(Utilities.WindowType.DICT, args);
	}
	
	private void sendToDict(final boolean toSktDict) {
		copySelection();
		final Clipboard cboard = Clipboard.getSystemClipboard();
		final String text = cboard.hasString() ? cboard.getString().trim() : "";
		if (!text.isEmpty()) {
			final String term = Utilities.getUsablePaliTerm(Utilities.convertToRomanPali(text));
			final SimpleService dictSearch = (SimpleService)ReaderUtilities.simpleServiceMap.get("paliplatform.main.DictSearch");
			if (dictSearch != null) {
				if (toSktDict)
					dictSearch.processArray(new Object[] { Utilities.WindowType.SKTDICT, term});
				else
					dictSearch.process(term);
			}
		}
	}
	
	public void showDictResult(final String text) {
		final String word = Utilities.getUsablePaliTerm(Utilities.convertToRomanPali(text));
		final String dpdWord = DictUtilities.makeDpdProper(word);
		final boolean useDPD = Boolean.parseBoolean(Utilities.getSetting("dpd-lookup-enable"));
		// if DPD dict available, find the word
		if (Utilities.ppdpdAvailMap.get(Utilities.PpdpdTable.DICTIONARY).get() && useDPD) {
			final List<String> meaning = DictUtilities.getMeaningFromDPD(dpdWord);
			if (!meaning.isEmpty()) {
				dictInfoPopup.setTitle(dpdWord);
				dictInfoPopup.setBody(meaning.stream().collect(Collectors.joining("\n")));
				dictInfoPopup.showPopup(webView, InfoPopup.Pos.BELOW_RIGHT, false);
				return;
			}
		}
		// if not found in DPD dict, find in deconstructor
		if (Utilities.ppdpdAvailMap.get(Utilities.PpdpdTable.DECONSTRUCTOR).get() && useDPD) {
			final List<String> decon = DictUtilities.getDeconFromDPD(dpdWord);
			if (!decon.isEmpty()) {
				dictInfoPopup.setTitle(dpdWord);
				dictInfoPopup.setBody(decon.stream().collect(Collectors.joining("\n")));
				dictInfoPopup.showPopup(webView, InfoPopup.Pos.BELOW_RIGHT, false);
				return;
			}
		}
		// if the term is a sandhi word, cut it
		final List<String> parts = DictUtilities.cutSandhi(word);
		if (parts.size() > 1) {
			final String res = parts.stream().collect(Collectors.joining(" + "));
			dictInfoPopup.setTitle(res);
			dictInfoPopup.setBody("");
			dictInfoPopup.showPopup(webView, InfoPopup.Pos.BELOW_RIGHT, false);
			return;
		}
		// if the term is in declined pronoun list, show it
		if (GrammarUtilities.declPronounsMap.containsKey(word)) {
			final DeclinedWord dword = GrammarUtilities.declPronounsMap.get(word);
			final String meaning = dword.getMeaning();
			final String caseMeaning = dword.getCaseMeaningString();
			final String head = caseMeaning.isEmpty() ? "" : "(" + caseMeaning + ") ";
			dictInfoPopup.setTitle(word);
			dictInfoPopup.setBody(head + meaning + "\n(" + dword.getCaseString() + ") (" + dword.getNumberString() + ") (" + dword.getGenderString() + ")");
			dictInfoPopup.showPopup(webView, InfoPopup.Pos.BELOW_RIGHT, false);
			return;
		}
		// if the term is in declined numeral list, show it
		if (GrammarUtilities.declNumbersMap.containsKey(word)) {
			final DeclinedWord dword = GrammarUtilities.declNumbersMap.get(word);
			final String meaning = dword.getMeaning();
			final String caseMeaning = dword.getCaseMeaningString();
			final String head = caseMeaning.isEmpty() ? "" : "(" + caseMeaning + ") ";
			dictInfoPopup.setTitle(word);
			dictInfoPopup.setBody(head + meaning + "\n(" + dword.getCaseString() + ") (" + dword.getNumberString() + ") (" + dword.getGenderString() + ")");
			dictInfoPopup.showPopup(webView, InfoPopup.Pos.BELOW_RIGHT, false);
			return;
		}
		// if the term is in declined noun/adj list, show it
		if (GrammarUtilities.declIrrNounsMap.containsKey(word)) {
			final DeclinedWord dword = GrammarUtilities.declIrrNounsMap.get(word);
			final String meaning = dword.getMeaning();
			final String caseMeaning = dword.getCaseMeaningString();
			final String head = caseMeaning.isEmpty() ? "" : "(" + caseMeaning + ") ";
			dictInfoPopup.setTitle(word);
			dictInfoPopup.setBody(head + meaning + "\n(" + dword.getCaseString() + ") (" + dword.getNumberString() + ") (" + dword.getGenderString() + ")");
			dictInfoPopup.showPopup(webView, InfoPopup.Pos.BELOW_RIGHT, false);
			return;
		}
		// or else find in the concise dict until something matched
		if (!DictUtilities.dictAvailMap.get(DictUtilities.DictBook.CPED).get()) return;
		String term = word;
		PaliWord pword = new PaliWord(term);
		while (pword.getMeaning().isEmpty() && term.length() > 1) {
			final String tfilter = term;
			Set<String> terms = DictUtilities.cpedTerms.stream().filter(x -> x.startsWith(tfilter)).collect(Collectors.toSet());
			if (!terms.isEmpty()) {
				pword = DictUtilities.getFirstCPEDWord(terms);
			} else {
				// replace the ending first
				final String rterm = DictUtilities.replaceTermEnding(term);
				terms = DictUtilities.cpedTerms.stream().filter(x -> x.startsWith(rterm)).collect(Collectors.toSet());
				if (!terms.isEmpty()) {
					pword = DictUtilities.getFirstCPEDWord(terms);
					term = rterm;
				} else {
					// if failed, cut the ending and go on
					term = term.substring(0, term.length() - 1);
				}
			}
		}
		if (!pword.getMeaning().isEmpty()) {
			final String tail = word.equals(term) ? "" : "*";
			dictInfoPopup.setTitle(pword.getTerm() + tail);
			dictInfoPopup.setBody(DictUtilities.formatCPEDMeaning(pword, false));
			dictInfoPopup.showPopup(webView, InfoPopup.Pos.BELOW_RIGHT, false);
		}
	}

	private void openTextEditor() {
		final String text = clickedText.get();
		if (!text.isEmpty()) {
			final SimpleService editor = (SimpleService)ReaderUtilities.simpleServiceMap.get("paliplatform.main.EditorLauncher");
			if (editor != null) {
				final Object[] args = { text };
				editor.processArray(args);
			}
		}
	}

	private void calculateMeters() {
		final String text = clickedText.get();
		final String romanText = Utilities.convertToRomanPali(text);
		if (!text.isEmpty()) {
			final SimpleService editor = (SimpleService)ReaderUtilities.simpleServiceMap.get("paliplatform.main.EditorLauncher");
			if (editor != null) {
				final Object[] args = { Utilities.addComputedMeters(romanText) };
				editor.processArray(args);
			}
		}
	}

	private void openAnalyzer() {
		final String text = clickedText.get();
		final String romanText = Utilities.convertToRomanPali(text);
		if (!text.isEmpty()) {
			final SimpleService prosody = (SimpleService)ReaderUtilities.simpleServiceMap.get("paliplatform.grammar.ProsodyLauncher");
			if (prosody != null) {
				prosody.process(romanText);
			}
		}
	}

	private void openSentenceReader() {
		final String text = clickedText.get();
		final String romanText = Utilities.convertToRomanPali(text);
		if (!text.isEmpty()) {
			final SimpleService reader = (SimpleService)ReaderUtilities.simpleServiceMap.get("paliplatform.sentence.ReaderLauncher");
			if (reader != null) {
				final Object[] args = { romanText };
				reader.processArray(args);
			}
		}
	}

	private void copySelection() {
		webEngine.executeScript("copySelection()");
	}
	
	private void saveSelection() {
		webEngine.executeScript("saveSelection()");
	}
	
	public void showFindMessage(final String text) {
		findBox.showMessage(text);
	}
	
	public void setSearchTextFound(final boolean yn) {
		searchTextFound.set(yn);
	}
	
	private void recordQuery(final PaliTextInput ptInput) {
		if (searchTextFound.get())
			ptInput.recordQuery();
	}

	protected void initFindInput() {
		findBox.init();
	}
	
	protected void setFindInputText(final String text) {
		findBox.setSuspended(true);
		findInput.setText(text);
		findBox.setSuspended(false);
		searchTextFound.set(false);
	}	
	
	private void clearFindInput() {
		findInput.clear();
		searchTextFound.set(false);
	}	
	
	private void openFind() {
		textPane.setBottom(findBox);
		findInput.requestFocus();		
	}

	private void findNext(final int direction) {
		final String strInput = findInput.getText();
		if (!strInput.isEmpty()) {
			final String strQuery = Normalizer.normalize(strInput, Form.NFC);
			findNext(strQuery, direction);
		}
	}

	protected void findNext(final String query, final int direction) {
		final int caseSensitive = findBox.isCaseSensitive() ? 1 : 0;
		final String properQuery = query.replace("'", "\\u{0027}");
		webEngine.executeScript("findNext('" + properQuery + "'," + caseSensitive + "," + direction + ")");
	}
	
	protected void findSingle(final String query) {
		final String properQuery = query.replace("'", "\\u{0027}");
		webEngine.executeScript("findSingleQuiet('" + properQuery + "'" + ")");
	}
	
}
