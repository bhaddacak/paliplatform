/* Reference.java
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

package paliplatform.reader;

import java.util.*;
import java.util.stream.*;

/** 
 * The representation of a book reference.
 * @author J.R. Bhaddacak
 * @version 3.0
 * @since 3.0
 */
public class Reference {
	private final String refId;
	private final String refName;
	private String altName;
	private String note;
	private final List<String> acadRefList; // ref used by academics
	private final Map<Corpus.Collection, List<String>> colRefMap; // ref used in collections

	public Reference(final String id, final String name) {
		refId = id;
		refName = name;
		altName = "";
		note = "";
		acadRefList = new ArrayList<>();
		colRefMap = new EnumMap<>(Corpus.Collection.class);
	}

	public String getFullName() {
		final String alt = altName.isEmpty() ? "" : " (" + altName + ")";
		return refName + alt;
	}

	public void setAltName(final String name) {
		altName = name;
	}

	public String getAltName() {
		return altName;
	}

	public void setNote(final String text) {
		note = text;
	}

	public String getNote() {
		return note;
	}

	public boolean hasNote() {
		return !note.isEmpty();
	}

	public void setAcadRefList(final List<String> refList) {
		acadRefList.clear();
		acadRefList.addAll(refList);
	}

	public String getAcadRefStr() {
		return acadRefList.stream().collect(Collectors.joining(", "));
	}

	public boolean isInAcadRef(final String word) {
		return acadRefList.stream().anyMatch(x -> x.toLowerCase().contains(word));
	}

	public void addColRefList(final Corpus.Collection col, final List<String> refList) {
		colRefMap.put(col, refList);
	}

	public String getColRefStr(final Corpus.Collection col) {
		final List<String> refList = colRefMap.get(col);
		return refList == null ? "" : refList.stream().collect(Collectors.joining(", "));
	}

	public boolean isInColRef(final String word) {
		boolean result = false;
		for (final Corpus.Collection col : Corpus.Collection.values) {
			result = result || isInColRef(col, word);
		}
		return result;
	}

	private boolean isInColRef(final Corpus.Collection col, final String word) {
		final List<String> refList = colRefMap.get(col);
		return refList == null ? false : refList.stream().anyMatch(x -> x.toLowerCase().contains(word));
	}

	public boolean isMultiWordInColRef(final Corpus.Collection col, final String multiWord) {
		final List<String> refList = colRefMap.get(col);
		boolean result = false;
		if (refList != null) {
			final String[] words = multiWord.split(",");
			for (final String word : words) {
				if (col == Corpus.Collection.SC) {
					if (refList.stream().anyMatch(x -> word.toLowerCase().contains(x.toLowerCase()))) {
						result = true;
						break;
					}
				} else {
					if (refList.stream().anyMatch(x -> x.equalsIgnoreCase(word))) {
						result = true;
						break;
					}
				}
			}
		}
		return result;
	}

	public boolean hasCollection(final Corpus.Collection col) {
		return colRefMap.containsKey(col);
	}

}

