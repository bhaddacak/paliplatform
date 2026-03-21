/* StringTriad.java
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

package paliplatform.base;

/** 
 * The representation of a triad of string for general uses.
 * @author J.R. Bhaddacak
 * @version 4.1
 * @since 4.1
 */
public class StringTriad {
	private final String[] triad = new String[3];

	public StringTriad(final String first, final String second, final String third) {
		triad[0] = first;
		triad[1] = second;
		triad[2] = third;
	}

	public String[] getTriad() {
		return triad;
	}

	public String getFirst() {
		return triad[0];
	}

	public String getSecond() {
		return triad[1];
	}

	public String getThird() {
		return triad[2];
	}

	public String toStringFull() {
		return triad[0] + ":" + triad[1] + ":" + triad[2];
	}

	@Override
	public String toString() {
		return triad[0];
	}

}

