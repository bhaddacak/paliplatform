/*
 * BjtTextHandler.java
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

package paliplatform.lucene;

import paliplatform.reader.*;
import java.util.*;
import java.util.regex.*;
import java.io.*;

/** 
 * The indexing handler of texts in BJT collection.
 * @author J.R. Bhaddacak
 * @version 3.1
 * @since 3.0
 */
 
class BjtTextHandler implements TextHandler {
	private final Map<TermInfo.Field, StringBuilder> textMap;

	public BjtTextHandler(final Map<TermInfo.Field, StringBuilder> tMap) {
		textMap = tMap;
	}

	@Override
	public void processFile(final File file) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void processStream(final InputStream input) {
		try {
			final List<BjtPage> pages = ReaderUtilities.getBjtPages(input);
			final Pattern boldPatt = Pattern.compile("\\*\\*(.*?)\\*\\*");
			for (final BjtPage page : pages) {
				final List<BjtPage.Element> entries = page.getEntries();
				for (final BjtPage.Element elem : entries) {
					final String text = elem.getText().replace("\n", " ");
					final Matcher boldMatcher = boldPatt.matcher(text);
					while (boldMatcher.find())
						textMap.get(TermInfo.Field.BOLD).append(" " + boldMatcher.group(1) + " \n");
					if (elem.getType() == BjtPage.Type.HEADING) {
						textMap.get(TermInfo.Field.HEADING).append(" " + text + " \n");
					} else if (elem.getType() == BjtPage.Type.CENTERED) {
						textMap.get(TermInfo.Field.CENTRE).append(" " + text + " \n");
					} else if (elem.getType() == BjtPage.Type.UNINDENTED) {
						textMap.get(TermInfo.Field.UNINDENTED).append(" " + text + " \n");
					} else if (elem.getType() == BjtPage.Type.PARAGRAPH) {
						textMap.get(TermInfo.Field.BODYTEXT).append(" " + text + " \n");
					} else if (elem.getType() == BjtPage.Type.GATHA) {
						textMap.get(TermInfo.Field.GATHA).append(" " + text + " \n");
					}
				}
			}
		} catch (IOException e) {
			System.err.println(e);
		}
	}

}

