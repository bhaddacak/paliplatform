/*
 * GramTextHandler.java
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
import java.util.regex.*;
import java.io.*;
import java.nio.charset.StandardCharsets;

/** 
 * The indexing handler of texts in the collection of grammar books.
 * @author J.R. Bhaddacak
 * @version 3.0
 * @since 3.0
 */
 
class GramTextHandler implements TextHandler {
	private final Map<TermInfo.Field, StringBuilder> textMap;

	public GramTextHandler(final Map<TermInfo.Field, StringBuilder> tMap) {
		textMap = tMap;
	}

	@Override
	public void processFile(final File file) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void processStream(final InputStream input) {
		final Scanner in = new Scanner(input, StandardCharsets.UTF_8);
		final StringBuilder bodyText = textMap.get(TermInfo.Field.BODYTEXT);
		final Pattern hPatt = Pattern.compile("<(h\\d)>(.*?)</\\1>");
		while (in.hasNextLine()) {
			String line = in.nextLine().trim();
			if (line.isEmpty()) continue;
			if (line.startsWith("<!--")) continue;
			final Matcher hMatcher = hPatt.matcher(line);
			if (hMatcher.find()) {
				// heading H2 - H5
				final TermInfo.Field fld = TermInfo.Field.valueOf(hMatcher.group(1).toUpperCase());
				if (fld != null)
					textMap.get(fld).append(" " + hMatcher.group(2).replaceAll("</?.*?>", "").trim() + " ").append("\n");
			} else {
				// otherwise bodytext
				line = line.replaceAll("</?.*?>", "").trim(); // remove all tags
				bodyText.append(" " + line + " \n");
			}
		}
		in.close();
	}

}

