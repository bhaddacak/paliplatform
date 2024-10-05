/*
 * NcpedWord.java
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

import java.util.*;

/** 
 * The representation of an NCPED dict entry.
 * @author J.R. Bhaddacak
 * @version 3.0
 * @since 2.2
 */
public class NcpedWord {
	private final String term;
	private final List<String> grammar;
	private final List<List<String>> definition;
	private final List<List<String>> xr;

	public NcpedWord(final String t) {
		term = t;
		grammar = new ArrayList<>(5);
		definition = new ArrayList<>(5);
		xr = new ArrayList<>(5);
	}

	public void addMeaning(final String g, final String d, final String x) {
		grammar.add(g);
		if (!d.isEmpty())
			definition.add(List.of(d.split(" \\| ")));
		if (!x.isEmpty())
		xr.add(List.of(x.split(", ")));
	}

	public String getTerm() {
		return term;
	}

	// number of homonyms, mostly 1
	public int getHomoCount() {
		return grammar.size();
	}

	public String getGrammar(final int ind) {
		final String result = ind < grammar.size() ? grammar.get(ind) : "";
		return result;
	}

	public List<String> getDefinition(final int ind) {
		final List<String> result = ind < definition.size() ? definition.get(ind) : Collections.emptyList();
		return result;
	}

	public List<String> getXR(final int ind) {
		final List<String> result = ind < xr.size() ? xr.get(ind) : Collections.emptyList();
		return result;
	}

}

