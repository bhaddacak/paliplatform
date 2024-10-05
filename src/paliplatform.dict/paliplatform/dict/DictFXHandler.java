/*
 * DictFXHandler.java
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

package paliplatform.dict;

import paliplatform.base.*;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.function.Function;
import java.util.ServiceLoader;
import java.util.ServiceLoader.Provider;

/** 
 * The handler class used to communicate with JavaScript in WebView.
 * Do not forget to add functions in viewer-common.js.
 * Also add "opens paliplatform.dict to javafx.web;" to module-info.java.
 * @author J.R. Bhaddacak
 * @version 3.0
 * @since 2.0
 */

public class DictFXHandler extends FXHandler {
	final DictWin dictHost;
	static Map<String, SimpleService> simpleServiceMap;
	
	public DictFXHandler(final HtmlViewer viewer, final DictWin host) {
		super(viewer);
		dictHost = host;
		textOutput = "dict-entry.txt";
		simpleServiceMap = getSimpleServices();
	}
	
	private static Map<String, SimpleService> getSimpleServices() {
		return ServiceLoader.load(SimpleService.class)
				.stream()
				.map(Provider::get)
				.collect(Collectors.toMap(x -> x.getClass().getName(), Function.identity()));
	}

	public void openDeclension(final String term) {
		final SimpleService declWin = DictFXHandler.simpleServiceMap.get("paliplatform.grammar.DeclWinLauncher");
		if (declWin != null) {
			final String[] args = { term };
			declWin.processArray(args);
		}
	}

	public void openNewDict(final String term) {
		final String[] args = { term };
		DictUtilities.openWindow(Utilities.WindowType.DICT, args);
	}

	public void setSearchTextFound(final boolean yn) {
		dictHost.setSearchTextFound(yn);
	}

	public void showFindMessage(final String text) {
		dictHost.showFindMessage(text);
	}

}
