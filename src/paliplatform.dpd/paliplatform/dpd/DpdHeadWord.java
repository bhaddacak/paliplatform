/*
 * DpdHeadWord.java
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

package paliplatform.dpd;

import paliplatform.base.DpdHeadWordBase;

import java.util.*;

/** 
 * The respresentation of a head word in DPD dictionary.
 * @author J.R. Bhaddacak
 * @version 3.0
 * @since 3.0
 */
class DpdHeadWord extends DpdHeadWordBase {
	private String notes = "";
	private String familyWord = "";
	private String familyIdiom = "";
	private String familyCompound = "";
	private String familySet = "";

	public DpdHeadWord(final String term) {
		super(term);
	}

	public void setNotes(final String text) {
		notes = text;
	}

	public String getNotes() {
		return notes;
	}

	public void setFamilyWord(final String text) {
		familyWord = text;
	}

	public String getFamilyWord() {
		return familyWord;
	}

	public void setFamilyIdiom(final String text) {
		familyIdiom = text;
	}

	public String getFamilyIdiom() {
		return familyIdiom;
	}

	public void setFamilyCompound(final String text) {
		familyCompound = text;
	}

	public String getFamilyCompound() {
		return familyCompound;
	}

	public void setFamilySet(final String text) {
		familySet = text;
	}

	public String getFamilySet() {
		return familySet;
	}

}

