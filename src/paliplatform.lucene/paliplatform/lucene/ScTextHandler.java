/*
 * ScTextHandler.java
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

package paliplatform.lucene;

import paliplatform.reader.ReaderUtilities;

import java.util.*;
import java.io.*;

/** 
 * The indexing handler of texts in the SuttaCentral collection.
 * @author J.R. Bhaddacak
 * @version 3.0
 * @since 3.0
 */
 
class ScTextHandler implements TextHandler {
	private final Map<TermInfo.Field, StringBuilder> textMap;

	public ScTextHandler(final Map<TermInfo.Field, StringBuilder> tMap) {
		textMap = tMap;
	}

	@Override
	public void processFile(final File file) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void processStream(final InputStream input) {
		try {
		final Map<String, String> scTextMap = ReaderUtilities.readJsonObject(input);
		final StringBuilder bodyText = textMap.get(TermInfo.Field.BODYTEXT);
		for (final String t : scTextMap.values()) {
			bodyText.append(" " + t + " ");
		}
		} catch (IOException e) {
			System.err.println(e);
		}
	}

}



