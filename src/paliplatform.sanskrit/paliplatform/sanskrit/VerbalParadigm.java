/*
 * VerbalParadigm.java
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

import static paliplatform.sanskrit.SktConjugation.TenseMood;
import static paliplatform.sanskrit.SktConjugation.Person;
import static paliplatform.sanskrit.SktConjugation.Number;
import static paliplatform.sanskrit.SktConjugation.Pada;

import java.util.*;

/** 
 * The class handling conjugational paradigms used in SktConjugation.
 * @author J.R. Bhaddacak
 * @version 4.1
 * @since 4.1
 */
public class VerbalParadigm {
	private final String paradigmName;
	private final String sampleTerm;
	private final List<String> wordList;
	private final TenseMood tenseMood;
	private final Pada pada;
	private int stemCutFactor;
	private String numBucknell;
	private boolean hasAugment;
	private List<String> presentSubstitution;
	private List<String> perfectSubstitution;
	private final Map<Person, Map<Number, List<String>>> paradigm;
	
	public VerbalParadigm(final String name, final String sample, final TenseMood tense, final Pada pad) {
		paradigmName = name;
		sampleTerm = sample;
		wordList = new ArrayList<>(); // used for a handful of examples, if any
		tenseMood = tense;
		pada = pad;
		stemCutFactor = 3; // cut the last 3 characters by default, e.g. nayati => nay
		numBucknell = ""; // paradigm number in Bucknell's handbook
		hasAugment = false; // no preceding a- by default
		perfectSubstitution = null;
		paradigm = new EnumMap<>(Person.class);
	}

	public static VerbalParadigm generate(final String pname, final String sample, final TenseMood tense,
											final Pada pad, final String[][] data) {
		final VerbalParadigm parad = new VerbalParadigm(pname, sample, tense, pad);
		parad.addEndings(data);
		return parad;
	}

	public static VerbalParadigm generate(final String pname, final String sample, final TenseMood tense,
											final Pada pad, final String[][] data, final String num) {
		final VerbalParadigm parad = generate(pname, sample, tense, pad, data);
		parad.setBucknellNumber(num);
		return parad;
	}

	public static VerbalParadigm duplicate(final VerbalParadigm parad, final String newName, final String sample) {
		final VerbalParadigm newParad = new VerbalParadigm(newName, sample, parad.getTenseMood(), parad.getPada());
		newParad.setStemCutFactor(parad.getStemCutFactor());
		newParad.setBucknellNumber(parad.getBucknellNumber());
		newParad.setAugment(parad.hasAugment());
		final Map<Person, Map<Number, List<String>>> p = parad.getParadigm();
		for (final Person person : p.keySet()) {
			final Map<Number, List<String>> numMap = p.get(person);
			for (final Number num : numMap.keySet()) {
				final List<String> endings = numMap.get(num);
				for (final String end : endings) {
					newParad.addEndings(person, num, end);
				}
			}
		}
		return newParad;
	}

	public String getName() {
		return paradigmName;
	}

	public String getSampleTerm() {
		return sampleTerm;
	}

	public void addWordList(final List<String> list) {
		wordList.addAll(list);
	}

	public List<String> getWordList() {
		return wordList;
	}

	public boolean hasInWordList(final String term) {
		return wordList.contains(term);
	}

	public TenseMood getTenseMood() {
		return tenseMood;
	}

	public Pada getPada() {
		return pada;
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

	public void setBucknellNumber(final String num) {
		numBucknell = num;
	}

	public String getBucknellNumber() {
		return numBucknell;
	}

	public void setAugment(final boolean yn) {
		hasAugment = yn;
	}

	public boolean hasAugment() {
		return hasAugment;
	}

	public Map<Person, Map<Number, List<String>>> getParadigm() {
		return paradigm;
	}

	public void addEndings(final Person person, final Number number, final String ending, final String... endings) {
		final Map<Number, List<String>> numberMap = paradigm.getOrDefault(person, new EnumMap<>(Number.class));
		final List<String> endingList = numberMap.getOrDefault(number, new ArrayList<>());
		endingList.add(ending);
		if (endings.length > 0)
			endingList.addAll(Arrays.asList(endings));
		numberMap.put(number, endingList);
		paradigm.put(person, numberMap);
	}

	public void addEndings(final String[][] data) {
		for (int p = 0; p < data.length; p++) {
			final Person person = Person.values[p];
			final String[] endings = data[p];
			for (int n = 0; n < endings.length; n++) {
				final String end = endings[n] == null || endings[n].isEmpty()
									? ""
									: endings[n];
				if (!end.equals("-")) {
					final Number number = Number.values[n];
					addEndings(person, number, end);
				}
			}
		}
	}
	
	public void setEndings(final Person person, final Number num, final List<String> endings) {
		final Map<Number, List<String>> numMap = this.getParadigm().get(person);
		numMap.put(num, new ArrayList<String>(endings));
	}

	public void addPresentSubstitution(final String... part) {
		presentSubstitution = new ArrayList<>();
		presentSubstitution.addAll(Arrays.asList(part));
	}

	public List<String> getPresentSubstitution() {
		return presentSubstitution == null ? Collections.emptyList() : presentSubstitution;
	}
	
	public void addPerfectSubstitution(final String... part) {
		perfectSubstitution = new ArrayList<>();
		perfectSubstitution.addAll(Arrays.asList(part));
	}

	public List<String> getPerfectSubstitution() {
		return perfectSubstitution == null ? Collections.emptyList() : perfectSubstitution;
	}
	
	public List<String> getConjugation(final String word, final Person pers, final Number numb) {
		final List<String> result = new ArrayList<>();
		final Map<Number, List<String>> numMap = paradigm.get(pers);
		if (numMap == null) return result;
		final List<String> endingList = numMap.get(numb);
		if (endingList == null) return result;
		final String stem = word.substring(0, word.length() - stemCutFactor);
		final String stemFinal = hasAugment ? augment(stem) : stem;
		for (final String end : endingList) {
			result.add(stemFinal + end);
		}
		return result;
	}

	public List<String> getConjugation(final String word, final Person pers, final Number numb, final Map<String, String> replaceMap) {
		final List<String> result = new ArrayList<>();
		final Map<Number, List<String>> numMap = paradigm.get(pers);
		if (numMap == null) return result;
		final List<String> endingList = numMap.get(numb);
		if (endingList == null) return result;
		final String stem = word.substring(0, word.length() - stemCutFactor);
		final String stemFinal = hasAugment ? augment(stem) : stem;
		for (final String end : endingList) {
			String endOK = end;
			for (final String k : replaceMap.keySet()) {
				if (end.indexOf(k) > -1) {
					endOK = end.replace(k, replaceMap.get(k));
					break;
				}
			}
			result.add(stemFinal + endOK);
		}
		return result;
	}

	public static String augment(final String term) {
		final String vowels = "aāiīuūṛṝḷeo";
		String result = "a";
		if (!term.isEmpty()) {
			final String firstTwo = term.length() > 1 ? term.substring(0, 2) : term.substring(0, 1);
			final String first = firstTwo.substring(0, 1);
			if (firstTwo.equals("ai") || firstTwo.equals("au")) {
				result = term;
			} else {
				if (vowels.indexOf(first) > -1) {
					// vowel initial, change to vṛddhi, see Deshpande's Primer, p. 94
					result = SanskritUtilities.getGradationOf(SanskritUtilities.VocalicGrade.VRDDHI, first) + term.substring(1);
				} else {
					// consonant, prefixed with 'a'
					result = "a" + term;
				}
			}
		}
		return result;
	}

	public static String deaugment(final String term) {
		if (term.isEmpty()) return "";
		// simple adhoc treatment
		final char first1 = term.charAt(0);
		final String first2 = term.length() > 1 ? term.substring(0, 2) : "";
		return !first2.isEmpty() && first2.equals("ai") || first2.equals("au")
					? term.substring(2)
					: first1 == 'a' || first1 == 'ā'
						? term.substring(1)
						: term;
	}

	public Map<Person, Map<Number, List<String>>> getSampleProduct() {
		return SktConjugation.compute(sampleTerm, paradigmName);
	}

}

