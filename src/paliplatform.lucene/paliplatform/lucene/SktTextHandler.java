/*
 * SktTextHandler.java
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

import java.util.*;
import java.util.zip.*;
import java.util.regex.*;
import java.util.stream.*;
import java.io.*;
import java.nio.charset.StandardCharsets;

/** 
 * The indexing handler of texts in GRETIL Sanskrit collection.
 * @author J.R. Bhaddacak
 * @version 3.7
 * @since 3.7
 */
 
class SktTextHandler implements TextHandler {
	private final Map<TermInfo.Field, StringBuilder> textMap;

	public SktTextHandler(final Map<TermInfo.Field, StringBuilder> tMap) {
		textMap = tMap;
	}

	@Override
	public void processFile(final File file) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void processStream(final InputStream input) {
		final Scanner in = new Scanner(input, StandardCharsets.UTF_8);
		final StringBuilder bodyText = new StringBuilder();
		boolean started = false;
		while (in.hasNextLine()) {
			String line = in.nextLine().trim();
			if (line.isEmpty()) continue;
			if (started) {
				bodyText.append(" " + line + " \n");
			}
			if (line.startsWith("# Text")) {
				started = true;
			}
		}
		in.close();
		textMap.get(TermInfo.Field.BODYTEXT).append(bodyText.toString()).append("\n");
	}

}


