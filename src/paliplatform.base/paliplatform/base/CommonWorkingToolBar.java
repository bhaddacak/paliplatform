/*
 * CommonWorkingToolBar.java
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

package paliplatform.base;

import java.util.*;
import java.util.stream.Collectors;
import java.util.function.Function;
import java.util.ServiceLoader.Provider;

import javafx.scene.control.*;
import javafx.scene.Node;
import javafx.stage.Window;

/** 
 * The common toolbar used in various working components.
 * @author J.R. Bhaddacak
 * @version 3.0
 * @since 2.0
 */
public class CommonWorkingToolBar extends ToolBar {
	private final Node node;
	private Utilities.Theme theme;
	private final Map<String, RadioMenuItem> fontMenuItemsMap = new HashMap<>();
	private String currFont;
	private int currFontSizePercent = 100;
	protected final ToggleButton darkButton = new ToggleButton("", new TextIcon("moon", TextIcon.IconSet.AWESOME));
	protected final Button zoomOutButton = new Button("", new TextIcon("circle-minus", TextIcon.IconSet.AWESOME));
	protected final Button resetButton = new Button("", new TextIcon("arrows-rotate", TextIcon.IconSet.AWESOME));
	protected final Button zoomInButton = new Button("", new TextIcon("circle-plus", TextIcon.IconSet.AWESOME));
	protected final ToggleGroup fontGroup = new ToggleGroup();
	public MenuButton fontMenu = new MenuButton("", new TextIcon("font", TextIcon.IconSet.AWESOME));
	public final Button copyButton = new Button("", new TextIcon("copy", TextIcon.IconSet.AWESOME));
	public final Button saveTextButton = new Button("", new TextIcon("file-arrow-down", TextIcon.IconSet.AWESOME));
	static Map<String, SimpleService> simpleServiceMap;
	
	public CommonWorkingToolBar(final Node node) {
		this.node = node;
		theme = Utilities.Theme.valueOf(Utilities.settings.getProperty("theme"));

		darkButton.setTooltip(new Tooltip("Dark theme on/off"));
		darkButton.setSelected(theme == Utilities.Theme.DARK);
		darkButton.setOnAction(actionEvent -> resetTheme());
		
		zoomOutButton.setTooltip(new Tooltip("Decrease font size"));
		zoomOutButton.setOnAction(actionEvent -> changeFontSize(-10));
		
		resetButton.setTooltip(new Tooltip("Reset to normal size"));
		resetButton.setOnAction(actionEvent -> changeFontSize(0));
		
		zoomInButton.setTooltip(new Tooltip("Increase font size"));
		zoomInButton.setOnAction(actionEvent -> changeFontSize(+10));
			
		fontMenu.setTooltip(new Tooltip("Select the display font"));
		currFont = setupFontMenu(Utilities.PaliScript.ROMAN);
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
		
		getItems().addAll(darkButton, zoomOutButton, resetButton, zoomInButton, fontMenu, saveTextButton, copyButton);		

		// init service
		simpleServiceMap = getSimpleServices();
	}

	private static Map<String, SimpleService> getSimpleServices() {
		return ServiceLoader.load(SimpleService.class)
				.stream()
				.map(Provider::get)
				.collect(Collectors.toMap(x -> x.getClass().getName(), Function.identity()));
	}

	public final String setupFontMenu(final Utilities.PaliScript script) {
		fontMenuItemsMap.clear();
		fontMenu.getItems().clear();
		final List<String> flist = new ArrayList<>(Utilities.paliFontMap.get(script));
		if (flist.isEmpty())
			flist.add(Utilities.FONT_FALLBACK);
		else
			Collections.sort(flist);
		for (final String fname : flist) {
			final RadioMenuItem fontMenuItem = new RadioMenuItem(fname);
			fontMenuItemsMap.put(fname, fontMenuItem);
			fontMenuItem.setToggleGroup(fontGroup);
			fontMenu.getItems().add(fontMenuItem);
		}
		// return first available font name
		return flist.get(0);
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
	
	public Button getResetButton() {
		return resetButton;
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
	
	public void resetFont(final String fontname) {
		currFontSizePercent = 100;
		setFont(currFont);
	}

	public void resetFont(final Utilities.PaliScript script) {
		currFontSizePercent = 100;
		setFont(script);
	}
	
	public void setFontMenu(final String fontname) {
		currFont = fontname;
		fontGroup.selectToggle(fontMenuItemsMap.get(fontname));
	}

	public void setFont(final String fontname) {
		final SimpleService dictFontSetter = CommonWorkingToolBar.simpleServiceMap.get("paliplatform.dict.FontSetter");
		final SimpleService readerFontSetter = CommonWorkingToolBar.simpleServiceMap.get("paliplatform.reader.FontSetter");
		final String nodeClassName = node.getClass().getName();
		if (nodeClassName.endsWith("DictWin")) {
			if (dictFontSetter != null) {
				dictFontSetter.processArray(new Object[] { node, fontname });
			}
		} else if (nodeClassName.endsWith("HtmlViewer") || nodeClassName.endsWith("ScReader")){
			if (readerFontSetter != null) {
				readerFontSetter.processArray(new Object[] { node, fontname });
			}
		} else {
			final Window win = node.getScene().getWindow();
			if (win.getClass().getName().endsWith("LetterWin")) {
				final SimpleService gramFontSetter = CommonWorkingToolBar.simpleServiceMap.get("paliplatform.grammar.FontSetter");
				if (gramFontSetter != null)
					gramFontSetter.processArray(new Object[] { win, fontname });
			} else {
				node.setStyle("-fx-font-family:'" + fontname + "';-fx-font-size:" + currFontSizePercent + "%;");
			}
		}
		setFontMenu(fontname);
	}
	
	public void setFont(final Utilities.PaliScript script) {
		final List<String> flist = new ArrayList<>(Utilities.paliFontMap.get(script));
		Collections.sort(flist);
		setFont(flist.get(0));
	}
	
	public void changeFontSize(final int percent) {
		if (percent == 0) {
			currFontSizePercent = 100;
		} else {
			currFontSizePercent += percent;
			if (currFontSizePercent < 20)
				currFontSizePercent = 20;
		}
		node.setStyle("-fx-font-family:'"+ currFont +"';-fx-font-size:" + currFontSizePercent + "%;");
	}

}
