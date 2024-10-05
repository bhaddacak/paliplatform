/*
 * DeclinedWord.java
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

package paliplatform.grammar;

import paliplatform.base.PaliWord;

/** 
 * Representation of a Pali declined word.
 * @author J.R. Bhaddacak
 * @version 3.0
 * @since 2.0
 */
public class DeclinedWord {
	final String term;
	String meaning = "";
	final boolean[] genders = new boolean[] { false, false, false }; // m, f, nt
	final boolean[] cases = new boolean[] { false, false, false, false, false, false, false, false }; // nom, acc, ins, dat, abl, gen, loc, voc
	final boolean[] numbers = new boolean[] { false, false }; // sin, plu

	public DeclinedWord(final String term) {
		this.term = term;
	}

	public String getTerm() {
		return term;
	}

	public void setMeaning(final String meaning) {
		this.meaning = meaning;
	}

	public String getMeaning() {
		return meaning;
	}

	public void setGender(final PaliWord.Gender gender) {
		genders[gender.ordinal()] = true;
	}

	public void setCase(final PaliDeclension.Case cas) {
		cases[cas.ordinal()] = true;
	}

	public void setNumber(final PaliDeclension.Number number) {
		numbers[number.ordinal()] = true;
	}

	public String getGenderString() {
		final StringBuilder result = new StringBuilder();
		for (int i = 0; i < genders.length; i++) {
			if (genders[i])
				result.append(PaliWord.Gender.values[i].getAbbr()).append("., ");
		}
		final int len = result.length();
		result.delete(len - 2, len);
		return result.toString();
	}

	public String getCaseString() {
		final StringBuilder result = new StringBuilder();
		for (int i = 0; i < cases.length; i++) {
			if (cases[i])
				result.append(PaliDeclension.Case.values[i].getAbbr()).append(", ");
		}
		final int len = result.length();
		result.delete(len - 2, len);
		return result.toString();
	}

	public String getCaseMeaningString() {
		final StringBuilder result = new StringBuilder();
		for (int i = 0; i < cases.length; i++) {
			if (cases[i]) {
				final String meanin = PaliDeclension.Case.values[i].getSimpleMeaning();
				if (!meanin.isEmpty())
					result.append(meanin).append(", ");
			}
		}
		final int len = result.length();
		if (len > 2)
			result.delete(len - 2, len);
		return result.toString();
	}

	public String getNumberString() {
		final StringBuilder result = new StringBuilder();
		for (int i = 0; i < numbers.length; i++) {
			if (numbers[i])
				result.append(PaliDeclension.Number.values[i].getAbbr().toLowerCase()).append(", ");
		}
		final int len = result.length();
		result.delete(len - 2, len);
		return result.toString();
	}

}
