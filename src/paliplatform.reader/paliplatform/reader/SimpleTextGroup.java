/*
 * SimpleTextGroup.java
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

/** 
 * The representation of a simple text group divided in a corpus.
 * @author J.R. Bhaddacak
 * @version 3.0
 * @since 3.0
 */
 
public class SimpleTextGroup implements TextGroup {
	private final String engName;
	private final String paliName;
	private final String abbrev;

	public SimpleTextGroup(final String grpStr) {
		final String[] grpArr = grpStr.split(":");
		engName = grpArr[0];
		paliName = grpArr.length > 1 ? grpArr[1] : "";
		abbrev = grpArr.length > 2 ? grpArr[2] : "";
	}

	@Override
	public String getName() {
		return engName;
	}

	@Override
	public String getEngName() {
		return engName;
	}

	@Override
	public String getPaliName() {
		return paliName;
	}

	@Override
	public String getAbbrev() {
		return abbrev;
	}

	@Override
	public String toString() {
		return engName;
	}

}

