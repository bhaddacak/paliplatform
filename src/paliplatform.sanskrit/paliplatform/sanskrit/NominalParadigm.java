/*
 * NominalParadigm.java
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

/** 
 * The class handling declensional paradigms used in SktDeclension.
 * @author J.R. Bhaddacak
 * @version 4.1
 * @since 4.1
 */
public class NominalParadigm {
	static enum WordType {
		NOUN, PRONOUN, NUMERAL;
		public static final WordType[] values = values();
		private final static String[] names = { "Nouns", "Pronouns and demonstratives", "Numerals" };
		private final static String[] shortNames = { "Noun", "Pronoun", "Numeral" };
		public String getName() {
			return names[this.ordinal()];
		}
		public String getShortName() {
			return shortNames[this.ordinal()];
		}
	}
	static enum Gender { 
		MAS, FEM, NEU;
		public static final Gender[] values = values();
		public static final Comparator<Gender> comparator = getComparator();
		private final static String codes = "mfn";
		private final static String[] names = { "Masculine", "Feminine", "Neuter" };
		private final char code;
		private Gender() {
			code = codes.charAt(this.ordinal());
		}
		public char getCode() {
			return code;
		}
		public String getName() {
			return names[this.ordinal()];
		}
		public String getShortName() {
			return names[this.ordinal()].substring(0, 3);
		}
		public String getAbbr() {
			final String abbr;
			if (code == 'n')
				abbr = "nt";
			else
				abbr = "" + code;
			return abbr;
		}
		public static Gender from(final char code) {
			final int ind = codes.indexOf(code);
			final Gender result = ind >= 0 ? Gender.values[ind] : MAS;
			return result;
		}
		private static Comparator<Gender> getComparator() {
			return new Comparator<Gender>() {
				@Override
				public int compare(final Gender aGen, final Gender bGen) {
					final int aOrd = aGen.ordinal();
					final int aNew = aOrd > 0 ? (aOrd % 2) + 1 : aOrd;
					final int bOrd = bGen.ordinal();
					final int bNew = bOrd > 0 ? (bOrd % 2) + 1 : bOrd;
					return Integer.compare(aNew, bNew);
				}
			};
		}
	};
	public static final String[] genericFallback =  { "devaḥ", "kathā", "phalam" };
	private static final Map<String, String> voicedMap = Map.of("k", "g", "ṭ", "ḍ", "t", "d", "p", "b", "ḥ", "r");
	private static final List<Set<String>> vaggas = List.of(Set.of("k", "kh", "g", "gh"),
															Set.of("c", "ch", "j", "jh"),
															Set.of("ṭ", "ṭh", "ḍ", "ḍh"),
															Set.of("t", "th", "d", "dh"),
															Set.of("p", "ph", "b", "bh"));
	private static final String[] nasals = { "ṅ", "ñ", "ṇ", "n", "m" };
	private final String paradigmName;
	private final SktNominal sampleTerm;
	private final List<String> wordList; // used for a handful of examples, if any
	private final List<Gender> gender;
	private String wordGroupIdentifier;
	private int stemCutFactor; // number of chars to cut a given word into stem, e.g. 1 for deva gets dev
	private String numBucknell; // table-paradigm number in Bucknell's handbook
	private WordType wordType;
	private final Map<SktDeclension.Case, Map<SktDeclension.Number, List<String>>> paradigm;
	
	public NominalParadigm(final String pname, final String term) {
		paradigmName = pname;
		sampleTerm = new SktNominal(term, this);
		wordList = new ArrayList<>();
		gender = new ArrayList<>(3);
		wordGroupIdentifier = "";
		stemCutFactor = 1; // cut the last character by default
		numBucknell = "";
		wordType = WordType.NOUN; // noun by default
		paradigm = new EnumMap<>(SktDeclension.Case.class);
	}

	private static String getNasal(final String ch) {
		int ind = -1;
		for (int i = 0; i < vaggas.size(); i++) {
			if (vaggas.get(i).contains(ch)) {
				ind = i;
				break;
			}
		}
		return ind > -1 ? nasals[ind] : "ṃ";
	}

	public static NominalParadigm generate(final String pname, final String term, final String[][] data) {
		final NominalParadigm parad = new NominalParadigm(pname, term);
		parad.addEndings(data);
		return parad;
	}

	public static NominalParadigm generate(final String pname, final String term, final String[][] data, final String ref) {
		final NominalParadigm parad = generate(pname, term, data);
		parad.setBucknellNumber(ref);
		return parad;
	}

	public static NominalParadigm generateFromPrototype(final NominalParadigm prototype, final String singEnd, final String pluEnd, final String term) {
		// from marut and jagat like paradigms
		final String pname = prototype.getName() + "-" + singEnd + "-" + pluEnd;
		final NominalParadigm parad = new NominalParadigm(pname, term);
		parad.setGender(prototype.getGenderList());
		parad.setStemCutFactor(prototype.getStemCutFactor());
		parad.setBucknellNumber(prototype.getBucknellNumber());
		final Map<SktDeclension.Case, Map<SktDeclension.Number, List<String>>> paradMarut = prototype.getParadigm();
		for (final SktDeclension.Case c : paradMarut.keySet()) {
			final Map<SktDeclension.Number, List<String>> numMap = paradMarut.get(c);
			for (final SktDeclension.Number n : numMap.keySet()) {
				final List<String> endings = numMap.get(n);
				for (final String e : endings) {
					final String end;
					if (n == SktDeclension.Number.SING && (c == SktDeclension.Case.NOM || c == SktDeclension.Case.VOC)) {
						end = e.replace("t", singEnd);
					} else if (n == SktDeclension.Number.PLU
								&& (c == SktDeclension.Case.NOM || c == SktDeclension.Case.ACC || c == SktDeclension.Case.VOC)
								&& e.indexOf('n') > -1) {
						// for jagat cases
						end = e.replace("n", getNasal(pluEnd)).replace("t", pluEnd);
					} else if (n == SktDeclension.Number.PLU && c == SktDeclension.Case.LOC) {
						end = Sandhi.applyInternalSandhiS(singEnd, "su");
					} else if ((n == SktDeclension.Number.DUAL || n == SktDeclension.Number.PLU)
								&& (c == SktDeclension.Case.INS || c == SktDeclension.Case.DAT || c == SktDeclension.Case.ABL)) {
						end = e.replace("d", voicedMap.get(singEnd));
					} else {
						end = e.replace("t", pluEnd);
					}
					parad.addEndings(c, n, end);
				}
			}

		}
		return parad;
	}

	public static NominalParadigm generateFromPrototype(final NominalParadigm prototype, final String singEnd, final String pluEnd,
			final String term, final String ref) {
		final NominalParadigm parad = generateFromPrototype(prototype, singEnd, pluEnd, term);
		parad.setBucknellNumber(ref);
		return parad;
	}

	public static NominalParadigm duplicate(final NominalParadigm parad, final String newName, final String sample) {
		final NominalParadigm newParad = new NominalParadigm(newName, sample);
		newParad.setGender(parad.getGenderList());
		newParad.setStemCutFactor(parad.getStemCutFactor());
		newParad.setBucknellNumber(parad.getBucknellNumber());
		final Map<SktDeclension.Case, Map<SktDeclension.Number, List<String>>> p = parad.getParadigm();
		for (final SktDeclension.Case c : p.keySet()) {
			final Map<SktDeclension.Number, List<String>> numMap = p.get(c);
			for (final SktDeclension.Number n : numMap.keySet()) {
				final List<String> endings = numMap.get(n);
				for (final String e : endings) {
					newParad.addEndings(c, n, e);
				}
			}
		}
		return newParad;
	}

	public String getName() {
		return paradigmName;
	}

	public SktNominal getSampleTerm() {
		return sampleTerm;
	}

	public String getSampleTermStr() {
		return sampleTerm.getTerm();
	}

	public void addWordList(final List<String> list) {
		wordList.addAll(list);
	}

	public List<String> getWordList() {
		return wordList;
	}

	public void setGender(final List<Gender> gends) {
		gender.clear();
		gender.addAll(gends);
	}

	public void addGender(final Gender gend, final Gender... gends) {
		gender.add(gend);
		for (final Gender g : gends) {
			gender.add(g);
		}
	}

	public boolean hasGender(final Gender gend) {
		return gender.contains(gend);
	}

	public List<Gender> getGenderList() {
		return gender;
	}

	public String getGenderStr() {
		final String result = gender.isEmpty() ? ""
							: gender.stream().map(x -> x.getShortName()).collect(Collectors.joining("/"));
		return result;
	}

	public Gender getGender() {
		return gender.isEmpty() ? Gender.MAS : gender.get(0);
	}

	public Gender getGender(final int index) {
		return gender.isEmpty() ? Gender.MAS : gender.get(index);
	}

	public void setWordGroupIdentifier(final String id) {
		wordGroupIdentifier = id;
	}

	public String getWordGroupIdentifier() {
		return wordGroupIdentifier;
	}

	public void setStemCutFactor(final int num) {
		stemCutFactor = num;
	}

	public int getStemCutFactor() {
		return stemCutFactor;
	}

	public String getStemFromWord(final String word) {
		return word.length() < stemCutFactor
				? ""
				: word.substring(0, word.length() - stemCutFactor);
	}

	public void setBucknellNumber(final String ref) {
		numBucknell = ref;
	}

	public String getBucknellNumber() {
		return numBucknell;
	}

	public void setWordType(final WordType type) {
		wordType = type;
	}

	public WordType getWordType() {
		return wordType;
	}

	public Map<SktDeclension.Case, Map<SktDeclension.Number, List<String>>> getParadigm() {
		return paradigm;
	}

	public void addEndings(final SktDeclension.Case cas, final SktDeclension.Number num, final String ending, final String... endings) {
		final Map<SktDeclension.Number, List<String>> numberMap = paradigm.getOrDefault(cas, new EnumMap<>(SktDeclension.Number.class));
		final List<String> endingList = numberMap.getOrDefault(num, new ArrayList<>());
		endingList.add(ending);
		if (endings.length > 0)
			endingList.addAll(Arrays.asList(endings));
		numberMap.put(num, endingList);
		paradigm.put(cas, numberMap);
	}

	public void addEndings(final String[][] data) {
		for (int c = 0; c < data.length; c++) {
			final SktDeclension.Case cas = SktDeclension.Case.values[c];
			final String[] endings = data[c];
			for (int n = 0; n < endings.length; n++) {
				final SktDeclension.Number num = SktDeclension.Number.values[n];
				addEndings(cas, num, endings[n]);
			}
		}
	}
	
	public List<String> getEndings(final SktDeclension.Case cas, final SktDeclension.Number num) {
		final Map<SktDeclension.Number, List<String>> numberMap = paradigm.get(cas);
		return numberMap.get(num);
	}

	public void setEndings(final SktDeclension.Case cas, final SktDeclension.Number num, final List<String> endings) {
		final Map<SktDeclension.Number, List<String>> numMap = this.getParadigm().get(cas);
		numMap.put(num, new ArrayList<String>(endings));
	}

	public Map<SktDeclension.Case, Map<SktDeclension.Number, List<String>>> getSampleProduct() {
		return SktDeclension.compute(sampleTerm);
	}

}

