/*
 * ViewerFXHandler.java
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

/** 
 * The handler class used to communicate with JavaScript in WebView.
 * @author J.R. Bhaddacak
 * @version 3.7
 * @since 2.0
 */

public class ViewerFXHandler extends FXHandler {
	public ViewerFXHandler(final PaliHtmlViewerBase viewer) {
		super((PaliHtmlViewerBase)viewer);
		textOutput = "document.txt";
	}
	
	public void setSearchTextFound(final boolean yn) {
		((PaliHtmlViewerBase)viewer).setSearchTextFound(yn);
	}

	public void showFindMessage(final String text) {
		((PaliHtmlViewerBase)viewer).showFindMessage(text);
	}

	public void showDictResult(final String text) {
		((PaliHtmlViewerBase)viewer).showDictResult(text);
	}

	public void updateClickedObject(final String text) {
		((PaliHtmlViewerBase)viewer).updateClickedObject(text);
	}

	public void openSktGretilDoc(final String name) {
		((SktGretilHtmlViewer)viewer).openSktDoc(name.substring(0, name.lastIndexOf(".")));
	}

}
