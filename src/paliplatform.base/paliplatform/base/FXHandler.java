/*
 * FXHandler.java
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

/** 
 * The handler class used to communicate with JavaScript in WebView.
 * @author J.R. Bhaddacak
 * @version 3.0
 * @since 2.0
 */

public class FXHandler {
	protected final HtmlViewer viewer;
	protected String textOutput = "output.txt";
	
	public FXHandler(final HtmlViewer viewer) {
		this.viewer = viewer;
	}
	
	public void copyText(final String text) {
		Utilities.copyText(text);
	}
	
	public void saveText(final String text) {
		Utilities.saveText(text, textOutput);
	}
	
	public void debugPrint(final String text) {
		System.out.println(text);
	}

}
