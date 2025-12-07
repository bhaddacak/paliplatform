/*
 * CommonWorkingToolBar.java
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

/** 
 * The common toolbar used in various working components.
 * @author J.R. Bhaddacak
 * @version 3.5
 * @since 2.0
 */
public class CommonWorkingToolBar extends ToolBar {
	private static final int DEFAULT_FONTSIZE = 100;
	private final Node[] nodes;
	private Utilities.Theme theme;
	private final Map<String, RadioMenuItem> fontMenuItemsMap = new HashMap<>();
	private String currFont;
	private int currFontSizePercent = DEFAULT_FONTSIZE;
	protected final ToggleButton darkButton = new ToggleButton("", new TextIcon("moon", TextIcon.IconSet.AWESOME));
	protected final Button zoomOutButton = new Button("", new TextIcon("circle-minus", TextIcon.IconSet.AWESOME));
	protected final Button resetButton = new Button("", new TextIcon("arrows-rotate", TextIcon.IconSet.AWESOME));
	protected final Button zoomInButton = new Button("", new TextIcon("circle-plus", TextIcon.IconSet.AWESOME));
	protected final ToggleGroup fontGroup = new ToggleGroup();
	public MenuButton fontMenu = new MenuButton("", new TextIcon("font", TextIcon.IconSet.AWESOME));
	public final Button copyButton = new Button("", new TextIcon("copy", TextIcon.IconSet.AWESOME));
	public final Button saveTextButton = new Button("", new TextIcon("file-arrow-down", TextIcon.IconSet.AWESOME));
	static Map<String, SimpleService> simpleServiceMap;
	
	public CommonWorkingToolBar(final Node... nodes) {
		this.nodes = nodes;
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
		currFont = setupFontMenu(PaliScript.ROMAN);
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

	public final String setupFontMenu(final PaliScript script) {
		fontMenuItemsMap.clear();
		fontMenu.getItems().clear();
		final List<String> allFonts = new ArrayList<>();
		// add generic fonts first
		for (final String gf : Utilities.genericFonts) {
			allFonts.add(gf);
		}
		// add embedded fonts; if script UNKNOWN, add all
		if (script == PaliScript.UNKNOWN) {
			for (final Collection<String> c : Utilities.embeddedFontMap.values())
				allFonts.addAll(c);
		} else {
			allFonts.addAll(Utilities.embeddedFontMap.get(script));
		}
		// add external fonts, if any; if script UNKNOWN, add all
		final List<String> extFonts = new ArrayList<>();
		if (script == PaliScript.UNKNOWN) {
			for (final Collection<String> c : Utilities.externalFontMap.values()) {
				for (final String f : c) {
					if (!extFonts.contains(f))
						extFonts.addAll(c);
				}
			}
		} else {
			extFonts.addAll(Utilities.externalFontMap.get(script));
		}
		Collections.sort(extFonts);
		for (final String f : extFonts) {
			if (!Utilities.genericFonts.contains(f))
				allFonts.add(f);
		}
		for (final String fname : allFonts) {
			final RadioMenuItem fontMenuItem = new RadioMenuItem(fname);
			fontMenuItemsMap.put(fname, fontMenuItem);
			fontMenuItem.setToggleGroup(fontGroup);
			fontMenu.getItems().add(fontMenuItem);
		}
		if (script == PaliScript.ROMAN) {
			// this should agree with base css (custom_light/custom_dark)
			return Utilities.FONTSANS;
		} else {
			// otherwise return the first available font name
			return allFonts.get(0);
		}
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
	
	public void resetFont(final int sizePercent) {
		currFontSizePercent = sizePercent;
		setFont(currFont);
	}
	
	public void resetFont(final String fontname) {
		resetFont(fontname, DEFAULT_FONTSIZE);
	}

	public void resetFont(final String fontname, final int sizePercent) {
		currFontSizePercent = sizePercent;
		currFont = fontname;
		setFont(currFont);
	}

	public void resetFont(final PaliScript script) {
		resetFont(script, DEFAULT_FONTSIZE);
	}
	
	public void resetFont(final PaliScript script, final int sizePercent) {
		currFontSizePercent = sizePercent;
		setFont(script);
	}
	
	public void setFontMenu(final String fontname) {
		currFont = fontname;
		fontGroup.selectToggle(fontMenuItemsMap.get(fontname));
	}

	private void setFont(final String fontname) {
		final SimpleService dictFontSetter = CommonWorkingToolBar.simpleServiceMap.get("paliplatform.dict.FontSetter");
		final SimpleService readerFontSetter = CommonWorkingToolBar.simpleServiceMap.get("paliplatform.reader.FontSetter");
		for (final Node node : nodes) {
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
				final Scene scene = node.getScene();
				if (scene == null) {
					node.setStyle("-fx-font-family:'" + fontname + "';-fx-font-size:" + currFontSizePercent + "%;");
				} else {
					final Window win = scene.getWindow();
					final String winName = win.getClass().getName();
					if (winName.endsWith("SktLetterWin")) {
						final SimpleService sktFontSetter = CommonWorkingToolBar.simpleServiceMap.get("paliplatform.sanskrit.FontSetter");
						if (sktFontSetter != null)
							sktFontSetter.processArray(new Object[] { win, fontname });
					} else if (winName.endsWith("LetterWin")) {
						final SimpleService gramFontSetter = CommonWorkingToolBar.simpleServiceMap.get("paliplatform.grammar.FontSetter");
						if (gramFontSetter != null)
							gramFontSetter.processArray(new Object[] { win, fontname });
					} else {
						node.setStyle("-fx-font-family:'" + fontname + "';-fx-font-size:" + currFontSizePercent + "%;");
					}
				}
			}
		}
//~ 		final javafx.scene.text.Font f = new javafx.scene.text.Font(fontname, 12.0);
//~ 		System.out.println(f.getFamily() + " : " + f.getName());
		setFontMenu(fontname);
	}
	
	private void setFont(final PaliScript script) {
		final List<String> emList = new ArrayList<>(Utilities.embeddedFontMap.get(script));
		if (!emList.isEmpty()) {
			// use an embedded font, if any
			setFont(emList.get(0));
		} else {
			// otherwise, use an external font
			final List<String> exList = new ArrayList<>(Utilities.externalFontMap.get(script));
			Collections.sort(exList);
			setFont(exList.get(0));
		}
	}
	
	public void changeFontSize(final int percent) {
		if (percent == 0) {
			currFontSizePercent = DEFAULT_FONTSIZE;
		} else {
			currFontSizePercent += percent;
			if (currFontSizePercent < 20)
				currFontSizePercent = 20;
		}
		for (final Node node : nodes) {
			if (!(node instanceof ComboBox || node instanceof TextField)) {
				node.setStyle("-fx-font-family:'" + currFont + "';-fx-font-size:" + currFontSizePercent + "%;");
			}
		}
	}

}
