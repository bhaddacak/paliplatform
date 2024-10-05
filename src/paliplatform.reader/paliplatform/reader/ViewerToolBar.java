/*
 * ViewerToolBar.java
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

import javafx.scene.control.*;
import javafx.scene.web.WebView;
import javafx.scene.Node;

/** 
 * The common toolbar used in HtmlViewer.
 * @author J.R. Bhaddacak
 * @version 3.0
 * @since 2.0
 */
class ViewerToolBar extends CommonWorkingToolBar {
	ViewerToolBar(final Node node, final WebView webView) {
		super(node);
		final PaliHtmlViewerBase viewer = (PaliHtmlViewerBase)node;
		darkButton.setOnAction(actionEvent -> {
			final Utilities.Theme theme = resetTheme();
			viewer.setViewerTheme(theme.toString());
		});

		final ToggleButton bwButton = new ToggleButton("", new TextIcon("circle-half-stroke", TextIcon.IconSet.AWESOME));
		bwButton.setTooltip(new Tooltip("Black and white on/off"));
		bwButton.setSelected(viewer.isBW);
		bwButton.setOnAction(actionEvent -> {
			viewer.isBW = bwButton.isSelected();
			((PaliHtmlViewerBase)node).setViewerTheme(viewer.isBW);
		});
		
		final Button printButton = new Button("", new TextIcon("print", TextIcon.IconSet.AWESOME));
		printButton.setTooltip(new Tooltip("Print"));
		printButton.setOnAction(actionEvent -> HtmlViewer.print(webView));
		
		zoomInButton.setOnAction(actionEvent -> webView.setFontScale(webView.getFontScale() + 0.10));
		zoomOutButton.setOnAction(actionEvent -> webView.setFontScale(webView.getFontScale() - 0.10));
		resetButton.setOnAction(actionEvent -> webView.setFontScale(1.0));
		
		getItems().addAll(bwButton, printButton);
	}

}
