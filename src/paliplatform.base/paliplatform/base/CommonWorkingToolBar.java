/*
 * CommonWorkingToolBar.java
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

package paliplatform.base;

import paliplatform.base.Utilities.PaliScript;

import java.util.*;
import java.util.stream.Collectors;
import java.util.function.Function;
import java.util.ServiceLoader.Provider;

import javafx.scene.control.*;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.stage.Window;
import javafx.application.Platform;

/** 
 * The common toolbar used in various working components.
 * @author J.R. Bhaddacak
 * @version 3.7
 * @since 2.0
 */
public class CommonWorkingToolBar extends ToolBar {
	static enum NodeClass { VIEWER, PDICT, SDICT, PLETTER, SLETTER, SREADER, OTHER }
	private final Node[] nodes;
	private Utilities.Theme theme;
	private final Map<String, RadioMenuItem> fontMenuItemsMap = new HashMap<>();
	private String currFont;
	private int currFontSizePercent; // set in init
	protected final ToggleButton darkButton = new ToggleButton("", new TextIcon("moon", TextIcon.IconSet.AWESOME));
	protected final Button zoomOutButton = new Button("", new TextIcon("circle-minus", TextIcon.IconSet.AWESOME));
	protected final Button zoomInButton = new Button("", new TextIcon("circle-plus", TextIcon.IconSet.AWESOME));
	protected final ChoiceBox<Integer> fontSizeChoice = new ChoiceBox<>();
	protected final ToggleGroup fontGroup = new ToggleGroup();
	public MenuButton fontMenu = new MenuButton("", new TextIcon("font", TextIcon.IconSet.AWESOME));
	public final Button copyButton = new Button("", new TextIcon("copy", TextIcon.IconSet.AWESOME));
	public final Button saveTextButton = new Button("", new TextIcon("file-arrow-down", TextIcon.IconSet.AWESOME));
	private NodeClass nodeClass; // set in init
	static Map<String, SimpleService> simpleServiceMap;
	
	public CommonWorkingToolBar(final Node... nodes) {
		this.nodes = nodes;
		theme = Utilities.Theme.valueOf(Utilities.getSetting("theme"));

		darkButton.setTooltip(new Tooltip("Dark theme on/off"));
		darkButton.setSelected(theme == Utilities.Theme.DARK);
		darkButton.setOnAction(actionEvent -> resetTheme());
		
		zoomOutButton.setTooltip(new Tooltip("Decrease font size"));
		zoomOutButton.setOnAction(actionEvent -> changeFontSize(-1));
		
		fontSizeChoice.setTooltip(new Tooltip("Font size (%)"));
		for (final int s : Utilities.fontSizes)
			fontSizeChoice.getItems().add(s);

		zoomInButton.setTooltip(new Tooltip("Increase font size"));
		zoomInButton.setOnAction(actionEvent -> changeFontSize(+1));
			
		fontMenu.setTooltip(new Tooltip("Select the display font"));
		currFont = setupFontMenu(PaliScript.UNKNOWN);
		fontGroup.selectToggle(fontMenuItemsMap.get(currFont));
        fontGroup.selectedToggleProperty().addListener((observable) -> {
			if (fontGroup.getSelectedToggle() != null) {
				final RadioMenuItem selected = (RadioMenuItem)fontGroup.getSelectedToggle();
				final String fontname = selected.getText();
				if (!currFont.equals(fontname)) {
					currFont = fontname;
					setFont(fontname);
				}
			}
        });
        		
		// context-dependent buttons, needed to be set-up before use
		saveTextButton.setTooltip(new Tooltip("Save data as text"));
		copyButton.setTooltip(new Tooltip("Copy text to clipboard"));
		
		getItems().addAll(darkButton, zoomOutButton, fontSizeChoice, zoomInButton, fontMenu, saveTextButton, copyButton);		

		// init service
		simpleServiceMap = getSimpleServices();
		Platform.runLater(() -> init());
	}

	private void init() {
		final String nodeClassName = nodes[0].getClass().getName();
		if (nodeClassName.endsWith("SktDictWin")) {
			nodeClass = NodeClass.SDICT;
		} else if(nodeClassName.endsWith("DictWin")) {
			nodeClass = NodeClass.PDICT;
		} else if (nodeClassName.endsWith("HtmlViewer") || nodeClassName.endsWith("ScReader")){
			nodeClass = NodeClass.VIEWER;
		} else {
			final Scene scene = nodes[0].getScene();
			if (scene == null) {
				nodeClass = NodeClass.OTHER;
			} else {
				final Window win = scene.getWindow();
				final String winName = win.getClass().getName();
				if (winName.endsWith("SktLetterWin")) {
					nodeClass = NodeClass.SLETTER;
				} else if (winName.endsWith("LetterWin")) {
					nodeClass = NodeClass.PLETTER;
				} else {
					final String parentName = nodes[0].getParent().getClass().getName();
					if (parentName.endsWith("SentenceReader"))
						nodeClass = NodeClass.SREADER;
					else
						nodeClass = NodeClass.OTHER;
				}
			}
		}
		currFontSizePercent = getDefaultFontSize();
		fontSizeChoice.getSelectionModel().select(Integer.valueOf(currFontSizePercent));
		if (nodeClass != NodeClass.VIEWER
				&& nodeClass != NodeClass.PLETTER
				&& nodeClass != NodeClass.SLETTER
				&& nodeClass != NodeClass.PDICT
				&& nodeClass != NodeClass.SDICT
				&& nodeClass != NodeClass.SREADER) {
			fontSizeChoice.setOnAction(actionEvent -> fontSizeSelected());
			resetFont(currFontSizePercent);
		}
	}

	private static Map<String, SimpleService> getSimpleServices() {
		return ServiceLoader.load(SimpleService.class)
				.stream()
				.map(Provider::get)
				.collect(Collectors.toMap(x -> x.getClass().getName(), Function.identity()));
	}

	private int getDefaultFontSize() {
		return nodeClass == NodeClass.VIEWER
				? Integer.valueOf(Utilities.getSetting("viewer-fontsize"))
				: nodeClass == NodeClass.PDICT || nodeClass == NodeClass.SDICT
					? Integer.valueOf(Utilities.getSetting("dict-fontsize"))
					: Integer.valueOf(Utilities.getSetting("other-fontsize"));
	}

	public final String setupFontMenu(final PaliScript script) {
		fontMenuItemsMap.clear();
		fontMenu.getItems().clear();
		for (final String fname : Utilities.availFontMap.get(script)) {
			final RadioMenuItem fontMenuItem = new RadioMenuItem(fname);
			fontMenuItemsMap.put(fname, fontMenuItem);
			fontMenuItem.setToggleGroup(fontGroup);
			fontMenu.getItems().add(fontMenuItem);
		}
		return Utilities.getSetting("font-" + script.toString().toLowerCase());
	}

	public final void reBuildFontMenu(final PaliScript script) {
		currFont = setupFontMenu(script);
	}
	
	public ToggleButton getThemeButton() {
		return darkButton;
	}
	
	public Button getZoomInButton() {
		return zoomInButton;
	}
	
	public Button getZoomOutButton() {
		return zoomOutButton;
	}
	
	public ChoiceBox<Integer> getFontSizeChoice() {
		return fontSizeChoice;
	}
	
	public Utilities.Theme getTheme() {
		return theme;
	}
	
	public Utilities.Theme resetTheme() {
		theme = darkButton.isSelected() ? Utilities.Theme.DARK : Utilities.Theme.LIGHT;
		Utilities.setTheme(this.getScene(), theme);
		return theme;
	}

	public String getCurrFont() {
		return currFont;
	}
	
	public void resetFont() {
		setFont(currFont);
	}
	
	public void resetFont(final int sizePercent) {
		currFontSizePercent = sizePercent;
		setFont(currFont);
	}
	
	public void resetFont(final String fontname) {
		resetFont(fontname, getDefaultFontSize());
	}

	public void resetFont(final String fontname, final int sizePercent) {
		currFontSizePercent = sizePercent;
		currFont = fontname;
		setFont(currFont);
	}

	public void resetFont(final PaliScript script) {
		resetFont(Utilities.getSetting("font-" + script.toString().toLowerCase()));
	}
	
	public void resetFont(final PaliScript script, final int sizePercent) {
		resetFont(Utilities.getSetting("font-" + script.toString().toLowerCase()), sizePercent);
	}
	
	public void setFontMenu(final String fontname) {
		currFont = fontname;
		fontGroup.selectToggle(fontMenuItemsMap.get(fontname));
	}

	private void setFont(final String fontname) {
		// for node window
		final Node node = nodes[0];
		if (nodeClass == NodeClass.PDICT) {
			final SimpleService dictFontSetter = CommonWorkingToolBar.simpleServiceMap.get("paliplatform.dict.FontSetter");
			if (dictFontSetter != null)
				dictFontSetter.processArray(new Object[] { node, fontname });
		} else if(nodeClass == NodeClass.SDICT) {
			final SimpleService sktDictFontSetter = CommonWorkingToolBar.simpleServiceMap.get("paliplatform.sanskrit.FontSetter");
			if (sktDictFontSetter != null)
				sktDictFontSetter.processArray(new Object[] { node, fontname });
		} else if(nodeClass == NodeClass.VIEWER) {
			final SimpleService readerFontSetter = CommonWorkingToolBar.simpleServiceMap.get("paliplatform.reader.FontSetter");
			if (readerFontSetter != null)
				readerFontSetter.processArray(new Object[] { node, fontname });
		} else if(nodeClass == NodeClass.PLETTER) {
			final Window win = node.getScene().getWindow();
			final SimpleService gramFontSetter = CommonWorkingToolBar.simpleServiceMap.get("paliplatform.grammar.FontSetter");
			if (gramFontSetter != null)
				gramFontSetter.processArray(new Object[] { win, fontname });
		} else if(nodeClass == NodeClass.SLETTER) {
			final SimpleService sktFontSetter = CommonWorkingToolBar.simpleServiceMap.get("paliplatform.sanskrit.FontSetter");
			if (sktFontSetter != null)
				sktFontSetter.processArray(new Object[] { node, fontname });
		} else {
			if (node instanceof ComboBox || node instanceof TextField)
				node.setStyle("-fx-font-family:'" + fontname + "';-fx-font-size:100%;");
			else
				node.setStyle("-fx-font-family:'" + fontname + "';-fx-font-size:" + currFontSizePercent + "%;");
		}
		// for other components, if any
		for (int i = 1; i < nodes.length; i++) {
			final Node compo = nodes[i];
			if (compo instanceof ComboBox || compo instanceof TextField)
				compo.setStyle("-fx-font-family:'" + fontname + "';-fx-font-size:100%;");
			else
				compo.setStyle("-fx-font-family:'" + fontname + "';-fx-font-size:" + currFontSizePercent + "%;");
		}
		setFontMenu(fontname);
	}
	
	private void changeFontSize() {
		for (final Node node : nodes) {
			if (!(node instanceof ComboBox || node instanceof TextField)) {
				node.setStyle("-fx-font-family:'" + currFont + "';-fx-font-size:" + currFontSizePercent + "%;");
			}
		}
	}

	public void changeFontSize(final int step) {
		final int currIndex = Arrays.binarySearch(Utilities.fontSizes, currFontSizePercent);
		final int newIndex = currIndex + step;
		if (newIndex < 0 || newIndex >= Utilities.fontSizes.length)
			return; // out of range
		currFontSizePercent = Utilities.fontSizes[newIndex];
		fontSizeChoice.getSelectionModel().select(Integer.valueOf(currFontSizePercent));
		changeFontSize();
	}

	private void fontSizeSelected() {
		currFontSizePercent = fontSizeChoice.getSelectionModel().getSelectedItem();
		changeFontSize();
	}

}
