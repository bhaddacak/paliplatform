/*
 * PaliHtmlViewer.java
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
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.geometry.Orientation;
import javafx.concurrent.Worker;
import netscape.javascript.JSObject;

/** 
 * The generic viewer of HTML Pali texts.
 * @author J.R. Bhaddacak
 * @version 3.6
 * @since 2.1
 */
public class PaliHtmlViewer extends PaliHtmlViewerBase {
	protected String docFilename;
	protected TocTreeNode thisDoc;
	private final HBox scriptContextBox = new HBox();
	private final List<RadioMenuItem> scriptRadioMenu = new ArrayList<>();
	private ToggleButton toggleNumberButton;
	protected boolean alsoConvertNumber = true;
	private String initialStringToLocate = "";

	public PaliHtmlViewer(final TocTreeNode node) {
		super();
		webEngine.setUserStyleSheetLocation(ReaderUtilities.class.getResource(ReaderUtilities.PALIHTML_CSS).toExternalForm());
		// Set the member for the browser's window object after the document loads
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
		toolBar.getItems().add(scriptContextBox);
		init(node);
	}
	
	public void init(final TocTreeNode node) {
		super.init();
		thisDoc = node;
		initScriptContext();
		docFilename = node.getNodeFileName();
		if (theStage != null)
			theStage.setTitle(node.getNodeName());
	}

	public void init(final TocTreeNode node, final String strToLocate) {
		init(node);
		setInitialStringToLocate(strToLocate);
	}

	private void initScriptContext () {
		scriptContextBox.getChildren().clear();
		// set display script according to corpus
		final Utilities.PaliScript script = thisDoc.getCorpus().getScript();
		displayScript.set(script);
		toolBar.reBuildFontMenu(script);
		if (script != Utilities.PaliScript.ROMAN)
			findBox.getFindTextInput().setInputMethod(PaliTextInput.InputMethod.NORMAL);
		// script conversion buttons
		if (thisDoc.getCorpus().isTransformable()) {
			final MenuButton convertMenu = new MenuButton("", new TextIcon("language", TextIcon.IconSet.AWESOME));
			convertMenu.setTooltip(new Tooltip("Convert the script to"));
			final ToggleGroup scriptGroup = new ToggleGroup();
			for (final Utilities.PaliScript sc : Utilities.PaliScript.scripts){
				if (sc.ordinal() == 0) continue;
				final String n = sc.toString();
				final RadioMenuItem scriptItem = new RadioMenuItem(n.charAt(0) + n.substring(1).toLowerCase());
				scriptItem.setToggleGroup(scriptGroup);
				scriptItem.setSelected(scriptItem.getText().toUpperCase().equals(displayScript.get().toString()));
				convertMenu.getItems().add(scriptItem);
				scriptRadioMenu.add(scriptItem);
			}
			scriptGroup.selectedToggleProperty().addListener((observable) -> {
				if (scriptGroup.getSelectedToggle() != null) {
					final RadioMenuItem selected = (RadioMenuItem)scriptGroup.getSelectedToggle();
					final Utilities.PaliScript toScript = Utilities.PaliScript.valueOf(selected.getText().toUpperCase());
					displayScript.set(toScript);
					toolBar.setupFontMenu(toScript);
					convertScript();
				}
			});
			toggleNumberButton = new ToggleButton("", new TextIcon("0-9", TextIcon.IconSet.SANS));
			toggleNumberButton.setTooltip(new Tooltip("Converting Roman numbers on/off"));
			toggleNumberButton.setSelected(alsoConvertNumber);
			toggleNumberButton.setOnAction(actionEvent -> {
				alsoConvertNumber = toggleNumberButton.isSelected();
				if (displayScript.get() != Utilities.PaliScript.ROMAN || displayScript.get() != Utilities.PaliScript.SINHALA) {
					convertScript();
				}
			});
			scriptContextBox.getChildren().addAll(new Separator(Orientation.VERTICAL), convertMenu, toggleNumberButton);
		}
	}

	public TocTreeNode getDocNode() {
		return thisDoc;
	}

	public void setInitialStringToLocate(final String token) {
		initialStringToLocate = token;
		if (!token.isEmpty())
			setFindInputText(token);
	}

	public void clearContent() {
		if (thisDoc.getCorpus().isTransformable())
			resetScriptMenu();
		setContent(ReaderUtilities.makeHTML(""));
	}

	protected void resetScriptMenu() {
		if (!scriptRadioMenu.isEmpty())
			scriptRadioMenu.get(0).setSelected(true);
		if (toggleNumberButton != null)
			toggleNumberButton.setSelected(false);
	}

	protected void convertScript() {
		// override this
	}
	
}
