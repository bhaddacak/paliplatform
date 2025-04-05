/*
 * CstrTextHandler.java
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
import java.io.*;
import java.nio.charset.StandardCharsets;

/** 
 * The indexing handler of texts in CSTR collection.
 * @author J.R. Bhaddacak
 * @version 3.0
 * @since 3.0
 */
 
class CstrTextHandler implements TextHandler {
	private final Map<TermInfo.Field, StringBuilder> textMap;

	public CstrTextHandler(final Map<TermInfo.Field, StringBuilder> tMap) {
		textMap = tMap;
	}

	@Override
	public void processFile(final File file) {
		final Pattern notePatt = Pattern.compile("\\[(.*?)\\]");
		final Pattern boldPatt = Pattern.compile("<b>(.*?)</b>");
		final Pattern hPatt = Pattern.compile("<(h\\d)>(.*?)</\\1>");
		final Pattern classPatt = Pattern.compile("<div class=\"(.*?)\">(.*?)</div>");
		try {
			final Scanner in = new Scanner(new GZIPInputStream(new FileInputStream(file)), StandardCharsets.UTF_8);
			while (in.hasNextLine()) {
				String line = in.nextLine();
				if (line.trim().isEmpty()) continue;
				if (line.startsWith("<!--")) continue;
				line = line.replaceAll("</?strong>", ""); // remove strong tags (no related field)
				final Matcher noteMatcher = notePatt.matcher(line);
				while (noteMatcher.find())
					textMap.get(TermInfo.Field.NOTE).append(" " + noteMatcher.group(1) + " \n");
				line = noteMatcher.replaceAll(" "); // remove notes
				final Matcher boldMatcher = boldPatt.matcher(line);
				while (boldMatcher.find())
					textMap.get(TermInfo.Field.BOLD).append(" " + boldMatcher.group(1) + " \n");
				line = line.replaceAll("</?b>", ""); // remove bold tags
				if (line.startsWith("<")) {
					// headings
					final Matcher hMatcher = hPatt.matcher(line);
					if (hMatcher.find()) {
						final TermInfo.Field fld = TermInfo.Field.valueOf(hMatcher.group(1).toUpperCase());
						if (fld != null)
							textMap.get(fld).append(" " + hMatcher.group(2) + " \n");
					}
					final Matcher classMatcher = classPatt.matcher(line);
					if (classMatcher.find()) {
						final TermInfo.Field fld = TermInfo.Field.valueOf(classMatcher.group(1).toUpperCase());
						if (fld != null)
							textMap.get(fld).append(" " + classMatcher.group(2) + " \n");
					}
				} else {
					// text body
					textMap.get(TermInfo.Field.BODYTEXT).append(" " + line + " \n");
				}
			}
			in.close();
		} catch (IOException e) {
			System.err.println(e);
		}
	}

	@Override
	public void processStream(final InputStream input) {
		throw new UnsupportedOperationException();
	}

}

