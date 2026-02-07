/*
 * Variant.java
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

package paliplatform.sentence;

/** 
 * The representation of a translation variant.
 * This is used in SentenceManager.
 * @author J.R. Bhaddacak
 * @version 4.1
 * @since 2.0
 */
public class Variant {
	private String name;
	private String author;
	private String note;

	public Variant(final String variant) {
		name = variant;
		author = "";
		note = "";
	}

	public void setName(final String newName) {
		name = newName;
	}

	public String getName() {
		return name;
	}

	public void setAuthor(final String someone) {
		author = someone;
	}

	public String getAuthor() {
		return author;
	}

	public void setNote(final String text) {
		note = text;
	}

	public String getNote() {
		return note;
	}

	@Override
	public String toString() {
		return name;
	}

	public String toStringFull() {
		return name + " (" + author + ") [" + note + "]";
	}

}
