/*
 * SktNominal.java
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

package paliplatform.sanskrit;

import java.util.*;
import java.util.stream.*;
import java.util.function.Function;

/** 
 * Representation of a Sanskrit nominal, i.e., noun, adjective, or pronoun.
 * @author J.R. Bhaddacak
 * @version 4.1
 * @since 4.1
 */
public class SktNominal {
	private final String term;
	private NominalParadigm paradigm;

	public SktNominal(final String word) {
		term = word;
		final int len = term.length();
	}

	public SktNominal(final String word, final NominalParadigm parad) {
		this(word);
		setParadigm(parad);
	}

	@Override
	public String toString() {
		return term;
	}
		
	public String getTerm() {
		return term;
	}
	
	public void setParadigm(final NominalParadigm parad) {
		paradigm = parad;
	}

	public NominalParadigm getParadigm() {
		return paradigm;
	}

	public List<String> getDeclension(final List<String> endings) {
		if (paradigm == null)
			return Collections.emptyList();
		final String stem = paradigm.getStemFromWord(term);
		final Function<String, String> combiner = paradigm.getName().equals("pumān") // pumān does not undergo internal sandhi
								? x -> x.isEmpty() ? "" : stem + x
								: x -> x.isEmpty() ? "" : Sandhi.applyInternalSandhi(stem, x);
		final List<String> result = endings.stream()
									.map(combiner)
									.collect(Collectors.toList());
		return result;
	}

	// inner classes
	static class Adjective {
		private static final String II = "ī";
		private static final String IKAA = "ikā";
		private String[] terms = new String[3];
		public Adjective(final String input) {
			final String[] parts = input.split(",");
			terms[0] = parts[0];
			terms[2] = parts[0];
			if (parts.length == 1) {
				terms[1] = parts[0];
			} else {
				if (II.equals(parts[1])) {
					terms[1] = parts[0].endsWith("n")
								? parts[0] + II
								: parts[0].substring(0, parts[0].length()-1) + II;
				} else if (IKAA.equals(parts[1])) {
					terms[1] = parts[0].substring(0, parts[0].length()-3) + IKAA;
				} else {
					terms[1] = parts[0];
				}
			}
		}
		public String[] getTerms() {
			return terms;
		}
		public String[] getParadigmNames() {
			final String[] result = new String[3];
			result[0] = terms[0].endsWith("n") ? "hastī" : "devaḥ";
			result[1] = terms[1].endsWith("ī") ? "nadī" : "kathā";
			result[2] = terms[2].endsWith("n") ? "bali" : "phalam";
			return result;
		}
	}

	static class Numeral {
		private static String[][] carGendParad14 = {
			{"ekaḥ", "ekā", "ekam"},
			{"dvau", "dve", "dve"},
			{"trayaḥ", "tisraḥ", "trīṇi"},
			{"catvāraḥ", "catasraḥ", "catvāri"} };
		private static String[] carGendParad510 = { "pañca", "ṣaṭ", "sapta", "aṣṭa", "nava", "daśa" };
		private static String[] ordGendParad13 = { "prathamaḥ", "prathamā", "prathamam" };
		private static String[] ordGendParad = { "devaḥ", "nadī", "phalam" };
		private String numberStr;
		private List<String> cardinal = new ArrayList<>();
		private List<String> ordinal = new ArrayList<>();
		public Numeral(final String numStr, final String card, final String ordi) {
			numberStr = numStr;
			cardinal.addAll(Arrays.asList(card.split(";")));	
			ordinal.addAll(Arrays.asList(ordi.split(";")));	
		}
		public int getNumber() {
			return Integer.parseInt(numberStr);
		}
		public String getNumberStr() {
			return numberStr;
		}
		public List<String> getCardinal() {
			return cardinal;
		}
		public List<String> getOrdinal() {
			return ordinal;
		}
		public static String getCardinalParadigmName(final int num, final String numStr, final NominalParadigm.Gender gender) {
			final String result;
			if (num <= 4) {
				final int gIndex = gender.ordinal();
				result = carGendParad14[num - 1][gIndex];
			} else {
				result = getCardinalParadigmName(num, numStr);
			}
			return result;
		}
		public static String getCardinalParadigmName(final int num, final String numStr) {
			final String result;
			if (num <= 4) {
				result = getCardinalParadigmName(num, numStr, NominalParadigm.Gender.MAS);
			} else if (num >= 5 && num <= 10){
				result = carGendParad510[num - 5];
			} else if (num > 10 && num <= 18){
				result = "daśa";
			} else {
				if (numStr.endsWith("daśa"))
					result = "daśa";
				else if (numStr.endsWith("i"))
					result = "matiḥ";
				else if(numStr.endsWith("t"))
					result = "marut";
				else
					result = "phalam";
			}
			return result;
		}
		public static String getOrdinalParadigmName(final int num, final String numStr, final NominalParadigm.Gender gender) {
			final String result;
			final int gIndex = gender.ordinal();
			if (num <= 3) {
				result = ordGendParad13[gIndex];
			} else {
				if (num == 4) {
					result = gender == NominalParadigm.Gender.FEM
								? numStr.endsWith("ya") ? "kathā" : "nadī"
								: ordGendParad[gIndex];
				} else {
					result = ordGendParad[gIndex];
				}
			}
			return result;
		}
	}

}

