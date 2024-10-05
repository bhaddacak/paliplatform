/*
 * PaliDeclension.java
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

import java.util.*;
import java.nio.charset.StandardCharsets;

/** 
 * The class handling Pali declensions.
 * @author J.R. Bhaddacak
 * @version 3.0
 * @since 1.0
 */
public class PaliDeclension {
	private static final String PARADIGM_NOUN_LIST = "resources/text/paradn.csv";
	public static enum Case {
		NOM("Nominative"), ACC("Accusative"), INS("Instrumental"), DAT("Dative"),
		ABL("Ablative"), GEN("Genitive"), LOC("Locative"), VOC("Vocative");
		public static final Case[] values = values();
		private final String name;
		Case(final String n) {
			name = n;
		}
		public String getName() {
			return name;
		}
		public String getAbbr() {
			return name.substring(0, 3).toLowerCase() + ".";
		}
		public String getNumAbbr() {
			final int num = this.ordinal() + 1;
			final String strNum;
			if (num < 8)
				strNum = "" + num;
			else
				strNum = "ā";
			return strNum;
		}
		public String getSimpleMeaning() {
			final String result;
			if (this == ACC)
				result = "towards";
			else if (this == INS)
				result = "by/with";
			else if (this == DAT)
				result = "for/to";
			else if (this == ABL)
				result = "from";
			else if (this == GEN)
				result = "of";
			else if (this == LOC)
				result = "in/at";
			else
				result = "";
			return result;
		}
	}
	public static enum Number {
		SING("Singular"), PLU("Plural");
		public static final Number[] values = values();
		private final String name;
		Number(final String n) {
			name = n;
		}
		public String getName() {
			return name;
		}
		public String getAbbr() {
			return name.substring(0, 3) + ".";
		}
	}
	private static final Map<String, NounParadigm> paradigmMap = new HashMap<>();
	
	public PaliDeclension() {
		loadNounParadigm();	
	}
	
	private void loadNounParadigm() {
		if (!paradigmMap.isEmpty())
			return;
		try (final Scanner in = new Scanner(PaliDeclension.class.getResourceAsStream(PARADIGM_NOUN_LIST), StandardCharsets.UTF_8)) {
			while (in.hasNextLine()) {
				final String line = in.nextLine().trim();
				if (line.charAt(0) == '#')
					continue;
				final String[] items = line.split(":");
				final String name = items[0];
				final String[] endgen = name.split(";")[1].split(",");
				final String ending = endgen[0];
				final PaliWord.Gender gender = PaliWord.getGender(endgen[1].charAt(0));
				final NounParadigm np = new NounParadigm(ending, gender);
				np.setParadigm(items[1]);
				paradigmMap.put(name, np);
			}
		}
	}
	
	public NounParadigm getNounParadigm(final String paradigmName, final String ending, final PaliWord.Gender gender) {
		final String key = paradigmName + ";" + ending + "," + gender.getCode();
		return paradigmMap.get(key);
	}
	
	// decline a word, return as a string (only the first form found)
	public String decline(final PaliWord pword, final PaliWord.Gender gender, final Case cas, final Number num) {
		String suffix = "";
		for (final String p : pword.getParadigm()) {
			final NounParadigm np = getNounParadigm(p, pword.getEnding().get(gender), gender);
			final List<String> ends = np.getEndings(cas, num);
			if (!ends.isEmpty()) {
				suffix = ends.get(0);
				break;
			}
		}
		return pword.withSuffix(suffix, gender);
	}
	
	// decline a word, return as a map of string (only the first form found)
	public Map<PaliWord.Gender, String> decline(final PaliWord pword, final Case cas, final Number num) {
		final Map<PaliWord.Gender, String> result = new EnumMap<>(PaliWord.Gender.class);
		for (final PaliWord.Gender gender : pword.getGender()) {
			String suffix = "";
			for (final String p : pword.getParadigm()) {
				final NounParadigm np = getNounParadigm(p, pword.getEnding().get(gender), gender);
				final List<String> ends = np.getEndings(cas, num);
				if (!ends.isEmpty()) {
					suffix = ends.get(0);
					break;
				}
			}
			result.put(gender, pword.withSuffix(suffix, gender));
		}
		return result;
	}
	
	// decline a numeral term
	public String declineNumeral(final PaliWord pword, final PaliWord.Gender gender, final Case cas) {
		final boolean isUttara = pword.getTerm().endsWith("uttara");
		final int value = pword.getNumericValue();
		if (value == 0)
			return "";
		PaliWord.Gender gen = gender;
		Number number = Number.PLU;
		// for number > 18, gender is dependent on the number's ending
		if (value > 18)
			gen = pword.getGender().get(0);
		if (isUttara)
			gen = PaliWord.Gender.NEU;
		if (value == 1) {
			if (isUttara)
				number = Number.PLU;
			else
				number = Number.SING;
		} else if (value < 18) {
			number = Number.PLU;
		} else if (value > 18 && value < 99) {
			if (isUttara)
				number = Number.PLU;
			else
				number = Number.SING;
		} else {
			number = Number.PLU;
		}
		return decline(pword, gen, cas, number);
	}

}
