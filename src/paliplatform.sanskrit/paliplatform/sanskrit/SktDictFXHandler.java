/*
 * SktDictFXHandler.java
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

package paliplatform.sanskrit;

import paliplatform.base.*;

/** 
 * The handler class used to communicate with JavaScript in WebView.
 * @author J.R. Bhaddacak
 * @version 3.5
 * @since 3.5
 */

public class SktDictFXHandler extends FXHandler {
	final SktDictWin dictHost;
	
	public SktDictFXHandler(final HtmlViewer viewer, final SktDictWin host) {
		super(viewer);
		dictHost = host;
		textOutput = "sktdict-entry.txt";
	}
	
	public void openNewDict(final String term) {
		final String[] args = { term };
		SanskritUtilities.openWindow(Utilities.WindowType.SKTDICT, args);
	}

	public void setSearchTextFound(final boolean yn) {
		dictHost.setSearchTextFound(yn);
	}

	public void showFindMessage(final String text) {
		dictHost.showFindMessage(text);
	}

}
