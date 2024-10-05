/*
 * DictEntry.java
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

/** 
 * Representation of dictionary entry used for the custom dict.
 * @author J.R. Bhaddacak
 * @version 3.0
 * @since 3.0
 */

public class DictEntry {
	private final String term;
	private final String meaning;
	private final String explanation;

	public DictEntry(final String term, final String meaning, final String explanation) {
		this.term = term;
		this.meaning = meaning;
		this.explanation = explanation;
	}

	public String getTerm() {
		return term;
	}

	public String getMeaning() {
		return meaning;
	}

	public String getExplanation() {
		return explanation;
	}

}
