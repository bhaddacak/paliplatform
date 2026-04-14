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
import java.util.function.Predicate;

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
		private static final List<Map<String, String>> paradList = new ArrayList<>();
		static {
			// masculine
			final Map<String, String> masMap = new HashMap<>();
			masMap.put("i", "muniḥ");
			masMap.put("u", "paśuḥ");
			masMap.put("ṛ", "netā");
			masMap.put("in",  "hastī");
			masMap.put("van", "ātmā");
			masMap.put("man", "rājā");
			masMap.put("mat", "dhīmān");
			masMap.put("vat", "dhīmān");
			masMap.put("at", "marut");
			masMap.put("yas", "śreyān");
			masMap.put("ivas", "tenivān");
			masMap.put("īvas", "ninīvān");
			masMap.put("uvas", "śuśruvān");
			masMap.put("ṛvas", "cakṛvān");
			masMap.put("vas", "vidvān");
			masMap.put("as", "vedhāḥ");
			paradList.add(masMap);
			// feminine
			final Map<String, String> femMap = new HashMap<>();
			femMap.put("ī", "nadī");
			femMap.put("i", "matiḥ");
			femMap.put("u", "dhenuḥ");
			femMap.put("as", "vedhāḥ");
			paradList.add(femMap);
			// neuter
			final Map<String, String> neuMap = new HashMap<>();
			neuMap.put("i", "vāri");
			neuMap.put("u", "madhu");
			neuMap.put("ṛ", "dhātṛ");
			neuMap.put("in", "bali");
			neuMap.put("van", "karma");
			neuMap.put("man", "nāma");
			neuMap.put("at", "jagat");
			neuMap.put("yas", "manaḥ");
			neuMap.put("ivas", "tenivat");
			neuMap.put("īvas", "ninīvat");
			neuMap.put("uvas", "śuśruvat");
			neuMap.put("ṛvas", "cakṛvat");
			neuMap.put("vas", "vidvat");
			neuMap.put("as", "manaḥ");
			paradList.add(neuMap);
		}
		private static final String[] fallbackParad = NominalParadigm.genericFallback;
		private String[] terms = new String[3]; // for 3 genders: m, f, n
		public Adjective(final String input) {
			final String[] parts = input.split(",");
			terms[0] = parts[0];
			terms[2] = parts[0];
			if (parts.length == 1) {
				// in uncommon set
				if (parts[0].endsWith("ṛ")) {
					terms[1] = parts[0].substring(0, parts[0].length()-1) + "rī";
				} else if (parts[0].endsWith("in")) {
					terms[1] = parts[0] + "ī";
				} else if (parts[0].endsWith("van")) {
					terms[1] = parts[0].substring(0, parts[0].length()-2) + "ṇī";
				} else if (parts[0].endsWith("man")) {
					terms[1] = parts[0].substring(0, parts[0].length()-2) + "nī";
				} else if (parts[0].endsWith("at") || parts[0].endsWith("yas")) {
					terms[1] = parts[0] + "ī";
				} else if (parts[0].endsWith("ivas")) {
					terms[1] = parts[0].substring(0, parts[0].length()-4) + "uṣī";
				} else if (parts[0].endsWith("īvas")) {
					final String front = parts[0].substring(0, parts[0].length()-4);
					terms[1] = SanskritUtilities.endsWithDoubleConsonant(front)
								? front + "iyuṣī"
								: front + "yuṣī";
				} else if (parts[0].endsWith("uvas")) {
					terms[1] = parts[0].substring(0, parts[0].length()-3) + "vuṣī";
				} else if (parts[0].endsWith("ṛvas")) {
					terms[1] = parts[0].substring(0, parts[0].length()-4) + "ruṣī";
				} else if (parts[0].endsWith("vas")) {
					terms[1] = parts[0].substring(0, parts[0].length()-3) + "uṣī";
				} else {
					terms[1] = parts[0];
				}
			} else {
				// in common set
				if ("ī".equals(parts[1])) {
					terms[1] = parts[0].substring(0, parts[0].length()-1) + "ī";
				} else if ("ikā".equals(parts[1])) {
					terms[1] = parts[0].substring(0, parts[0].length()-3) + "ikā";
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
			for (int i = 0; i < 3; i++) {
				final Map<String, String> paradMap = paradList.get(i);
				final String term = terms[i];
				final String end4 = term.length() >= 4 ? term.substring(term.length()-4) : "";
				final String end3 = term.length() >= 3 ? term.substring(term.length()-3) : "";
				final String end2 = term.length() >= 2 ? term.substring(term.length()-2) : "";
				final String end = term.substring(term.length()-1);
				if (paradMap.containsKey(end4)) {
					result[i] = paradMap.get(end4);
				} else if (paradMap.containsKey(end3)) {
					result[i] = paradMap.get(end3);
				} else if (paradMap.containsKey(end2)) {
					result[i] = paradMap.get(end2);
				} else if (paradMap.containsKey(end)) {
					result[i] = paradMap.get(end);
				} else {
					result[i] = fallbackParad[i];
				}
			}
			return result;
		}
		public static Predicate<String> getEndingPredicate(final String end) {
			final Predicate<String> result;
			if (end.equals("at")) {
				result = x -> x.matches(".*[^mv]at");
			} else if (end.equals("vas")) {
				result = x -> x.matches(".*[^i]vas");
			} else if (end.equals("as")) {
				result = x -> x.matches(".*[^yv]as");
			} else {
				result = x -> x.endsWith(end);
			}
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

