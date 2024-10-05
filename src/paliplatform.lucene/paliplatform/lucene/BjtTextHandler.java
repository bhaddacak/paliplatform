/*
 * BjtTextHandler.java
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

import java.util.*;
import java.io.*;
import java.nio.charset.StandardCharsets;

/** 
 * The indexing handler of texts in BJT collection.
 * @author J.R. Bhaddacak
 * @version 3.0
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
		final Scanner in = new Scanner(input, StandardCharsets.UTF_8);
		boolean startFlag = false;
		final StringBuilder bodyText = textMap.get(TermInfo.Field.BODYTEXT);
		while (in.hasNextLine()) {
			String line = in.nextLine().trim();
			if (!startFlag) {
				startFlag = line.equals("<!--text-start-->");
				continue;
			}
			if (line.isEmpty()) continue;
			if (line.startsWith("<!--")) continue;
			line = line.replaceAll("</?.*?>", "").trim(); // remove all tags
			line = line.replaceAll("\\[ *\\\\.*?/ *\\]", ""); // remove format strings
			// The text is full of non-Pali that cannot be removed.
			// So, this collection is heavily contaminated.
			// all text is bodytext
			bodyText.append(" " + line + " ");
		}
		in.close();
	}

}

