/* StringPair.java
 *
 * Copyright (C) 2023-2026 J. R. Bhaddacak 
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

package paliplatform.base;

/** 
 * The representation of a pair of string for general uses.
 * @author J.R. Bhaddacak
 * @version 4.1
 * @since 3.0
 */
public class StringPair {
	private final String[] pair = new String[2];
	private int hashCode;

	public StringPair(final String first, final String second) {
		pair[0] = first;
		pair[1] = second;
	}

	public String[] getPair() {
		return pair;
	}

	public String getFirst() {
		return pair[0];
	}

	public String getSecond() {
		return pair[1];
	}

	public String toStringFull() {
		return pair[0] + ":" + pair[1];
	}

	@Override
	public boolean equals(final Object other) {
		if (other == null) return false;
		final StringPair otherPair = (StringPair)other;
		return pair[0].equals(otherPair.getFirst()) && pair[1].equals(otherPair.getSecond());
	}

	@Override
	public int hashCode() {
		int result = hashCode;
		if (result == 0) {
			result = pair[0].hashCode();
			result = 31 * result + pair[1].hashCode();
			hashCode = result;
		}
		return result;
	}
	@Override
	public String toString() {
		return pair[0];
	}

}
