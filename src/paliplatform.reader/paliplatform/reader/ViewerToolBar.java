/*
 * ViewerToolBar.java
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

import java.util.Map;
import java.util.EnumMap;
import java.util.HashMap;
import javafx.scene.control.*;
import javafx.scene.web.WebView;
import javafx.scene.Node;

/** 
 * The common toolbar used in HtmlViewer.
 * @author J.R. Bhaddacak
 * @version 3.6
 * @since 2.0
 */
class ViewerToolBar extends CommonWorkingToolBar {
	private final ToggleGroup lineSpacingGroup = new ToggleGroup();
	private final ToggleGroup styleGroup = new ToggleGroup();
	private final Map<Utilities.Style, Toggle> styleToggleMap = new EnumMap<>(Utilities.Style.class);
	private final Map<String, Toggle> lineHeightToggleMap = new HashMap<>();
	private PaliHtmlViewerBase viewer;

	ViewerToolBar(final WebView webView, final Node... nodes) {
		super(nodes);
		viewer = (PaliHtmlViewerBase)nodes[0];
		darkButton.setOnAction(actionEvent -> {
			final Utilities.Theme theme = resetTheme();
			viewer.setViewerTheme(theme.toString());
		});

		final MenuButton lineSpacingMenu = new MenuButton("", new TextIcon("arrows-up-down", TextIcon.IconSet.AWESOME));
		for (final String h : Utilities.lineHeights) {
			final RadioMenuItem radio = new RadioMenuItem(h);
			radio.setUserData(h);
			radio.setToggleGroup(lineSpacingGroup);
			lineSpacingMenu.getItems().add(radio);
			lineHeightToggleMap.put(h, radio);
		}
        lineSpacingGroup.selectedToggleProperty().addListener(observable -> 
				viewer.setLineHeight((String)lineSpacingGroup.getSelectedToggle().getUserData()));
		
		final MenuButton styleMenu = new MenuButton("", new TextIcon("paint-brush", TextIcon.IconSet.AWESOME));
		styleMenu.setTooltip(new Tooltip("Style"));
		for (final Utilities.Style s : Utilities.Style.values) {
			final RadioMenuItem radio = new RadioMenuItem(s.getName());
			radio.setUserData(s);
			radio.setToggleGroup(styleGroup);
			styleMenu.getItems().add(radio);
			styleToggleMap.put(s, radio);
		}
        styleGroup.selectedToggleProperty().addListener(observable -> 
				viewer.setViewerTheme((Utilities.Style)styleGroup.getSelectedToggle().getUserData()));

		final Button printButton = new Button("", new TextIcon("print", TextIcon.IconSet.AWESOME));
		printButton.setTooltip(new Tooltip("Print"));
		printButton.setOnAction(actionEvent -> viewer.print());
		
		zoomInButton.setOnAction(actionEvent -> webView.setFontScale(webView.getFontScale() + 0.10));
		zoomOutButton.setOnAction(actionEvent -> webView.setFontScale(webView.getFontScale() - 0.10));
		resetButton.setOnAction(actionEvent -> webView.setFontScale(1.0));
		
		getItems().addAll(lineSpacingMenu, styleMenu, printButton);
	}

	@Override
	public void resetFont(final Utilities.PaliScript script) {
		super.resetFont(script);
		// set default line space
		if (viewer != null) {
			styleGroup.selectToggle(styleToggleMap.get(viewer.getBGStyle()));
			final String lh = viewer.getLineHeight();
			lineSpacingGroup.selectToggle(lineHeightToggleMap.get(lh));
			viewer.setLineHeight(lh);
		}
	}
	
}
