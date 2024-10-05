/*
 * PaliWord.java
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

import java.util.*;

/** 
 * Pali word class, mainly used with CPED dictionary and other grammatical purposes.
 * @author J.R. Bhaddacak
 * @version 3.0
 * @since 1.0
 */
public class PaliWord {
	public static enum Gender { 
		MAS, FAM, NEU;
		public static final Gender[] values = values();
		private final static String codes = "mfn";
		private final char code;
		private Gender() {
			code = codes.charAt(this.ordinal());
		}
		public char getCode() {
			return code;
		}
		public String getName() {
			final String name;
			if (code == 'm')
				name = "Masculine";
			else if (code == 'f')
				name = "Faminine";
			else if (code == 'n')
				name = "Neuter";
			else
				name = "";
			return name;
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
	};
	private static final String PARADIGM_DELIM = ",";
	private final String term;
	private final String stem;
	private final char lastChar;
	private final Map<Gender, String> insertion;
	private final Map<Gender, String> ending;
	private final List<String> posInfo;
	private List<Gender> gender;
	private List<String> paradigm;
	private final List<String> meaning;
	private final List<String> submeaning;
	private final List<Boolean> forCompounds;
	private int numericValue;
	private int expValue;
	private int recordCount; // count the record in db with the same term
	
	public PaliWord(final String term) {
		this.term = term;
		lastChar = term.charAt(term.length()-1);
		if (lastChar == 't')
			stem = term.substring(0, term.length()-3);
		else if (lastChar == 'r' || lastChar == 'ṃ')
			stem = term.substring(0, term.length()-2);
		else
			stem = term.substring(0, term.length()-1);
		insertion = new EnumMap<>(Gender.class);
		for (Gender g : Gender.values)
			insertion.put(g, "");
		ending = new EnumMap<>(Gender.class);
		gender = new ArrayList<>(3);
		posInfo = new ArrayList<>();
		paradigm = new ArrayList<>();
		meaning = new ArrayList<>();
		submeaning = new ArrayList<>();
		forCompounds = new ArrayList<>();
		numericValue = 0;
		expValue = 0;
		recordCount = 0;
	}
	
	@Override
	public String toString() {
		return term;
	}
		
	public String getTerm() {
		return term;
	}
	
	public String getStem() {
		return stem;
	}
	
	public char getLastChar() {
		return lastChar;
	}
	
	/**
	 * Sets ending and insertion of pronouns and numerals
	 */
	public void setEnding() {
		final String end;
		if (lastChar == 'ṃ')
			end = "a";
		else
			end = "" + lastChar;
		for (Gender g : gender) {
			ending.put(g, transformEnding(end, g));
			insertion.put(g, "");
		}
	}
	
	public Map<Gender, String> getEnding() {
		return ending;
	}
	
	public int getRecordCount() {
		return recordCount;
	}
	
	// for noun/adj this method also add gender and ending
	// for pronoun, setAllGenders() and setEnding() have to be called when creating a new word
	public void addPosInfo(final String pos) {
		recordCount++;
		if (pos == null)
			return;
		final Set<Gender> genderSet = new LinkedHashSet<>(3);
		final String[] posArr;
		if (pos.contains(";"))
			posArr = pos.split(";");
		else
			posArr = new String[] { pos };
		for (final String s : posArr) {
			final String p = s.trim();
			if (!posInfo.contains(p))
				posInfo.add(p);
		}
		// determine genders
		for (final String s : posArr) {
			final String p = s.trim();
			if (p.equals("3") || p.contains("adj.")) {
				genderSet.add(Gender.MAS);
				genderSet.add(Gender.FAM);
				genderSet.add(Gender.NEU);
			} else if (p.equals("n.")) {
				if (lastChar == 'ā') {
					genderSet.add(Gender.FAM);
				} else if (lastChar == 'a') {
					genderSet.add(Gender.MAS);
					genderSet.add(Gender.NEU);
				} else if (lastChar == 'ī' || lastChar == 'ū') {
					genderSet.add(Gender.MAS);
					genderSet.add(Gender.FAM);
				} else {
					genderSet.add(Gender.MAS);
					genderSet.add(Gender.FAM);
					genderSet.add(Gender.NEU);
				}
			} else if (p.contains("m.")) {
				genderSet.add(Gender.MAS);
			} else if (p.contains("f.")) {
				genderSet.add(Gender.FAM);
			} else if (p.contains("nt.")) {
				genderSet.add(Gender.NEU);
			}
		} // end for
		// set corresponding endings
		for (final Gender g : genderSet) {
			if (!gender.contains(g)) {
				gender.add(g);
				insertion.put(g, "");
				String end = transformEnding("" + lastChar, g);
				if (g == Gender.FAM && !isNumber()) {
					if (isAdjective() && (lastChar == 'ī' || lastChar == 'i')) {
						end = "ī";
						insertion.put(g, "in");
					}
				} // end if
				ending.put(g, end);
			}
		} // end for
	}
	
	public static String transformEnding(final String word, final Gender gender) {
		final String base = word.substring(0, word.length()-1);
		final char lastCh = word.charAt(word.length()-1);
		String end = "" + lastCh; 
		if (lastCh == 'r') {
			end = "u";
		} else if (lastCh == 'e') {
			end = "i";
		} else {
			if (gender == Gender.MAS) {
				if (lastCh == 'ā' || lastCh == 'o')
					end = "a";
			} else if (gender == Gender.FAM) {
				if (lastCh == 'a' || lastCh == 'o')
					end = "ā";
			} else if (gender == Gender.NEU) {
				if (lastCh == 'ā' || lastCh == 'o')
					end = "a";
				else if (lastCh == 'ī')
					end = "i";
				else if (lastCh == 'ū')
					end = "u";
			}
		}
		return base + end;
	}

	public List<String> getPosInfo() {
		return posInfo;
	}
	
	public String getPosString() {
		String pos = "";
		for (final String s : posInfo) {
			pos = pos + s + "; ";
		}
		return pos.substring(0, pos.length()-2);
	}
	
	public List<Gender> getGender() {
		return gender;
	}
	
	public static Gender getGender(char code) {
		Gender gen = null;
		for (Gender g : Gender.values) {
			if (code == g.getCode()) {
				gen = g;
				break;
			}
		}
		return gen;
	}
	
	public void setAllGenders() {
		gender = new ArrayList<>();
		gender.add(Gender.MAS);
		gender.add(Gender.FAM);
		gender.add(Gender.NEU);
	}
	
	public void setNumeralGender(final boolean isOrdinal) {
		gender = new ArrayList<>();
		if ((numericValue <= 18 && expValue == 0) || isOrdinal) {
			gender.add(Gender.MAS);
			gender.add(Gender.FAM);
			gender.add(Gender.NEU);
		} else if (numericValue >= 99 || expValue > 0) {
			if (lastChar == 'i' || lastChar == 'ī')
				gender.add(Gender.FAM);
			else
				gender.add(Gender.NEU);
		} else {
			if (lastChar == 'ṃ')
				gender.add(Gender.NEU);
			else
			gender.add(Gender.FAM);
		}
	}
	
	public void setNumeralParadigm(final boolean isOrdinal) {
		paradigm = new ArrayList<>();
		String parad = "generic";
		if (isOrdinal) {
			if (numericValue <= 3 || numericValue >= 19)
				parad = "ordinal";
			if (numericValue >= 4 && numericValue <= 10)
				parad = "ordinal4";
			if (numericValue >= 11 && numericValue <= 18)
				parad = "ordinal11";
		} else {
			if (expValue == 0) {
				if (numericValue < 99) {
					if (numericValue == 1)
						parad = "eka";
					else if (numericValue == 2)
						parad = "dvi";
					else if (numericValue == 3)
						parad = "ti";
					else if (numericValue == 4)
						parad = "catu";
					else if (numericValue <= 18)
						parad = "number18";
					else
						parad = "number98";
				} else {
					parad = "number99";
				}
			}
		}
		paradigm.add(parad);
	}
		
	public void setParadigm(final String parad) {
		if (parad.contains(PARADIGM_DELIM)) {
			final String[] para = parad.split(PARADIGM_DELIM);
			for (String s : para) {
				if (s.charAt(0) == '0')
					paradigm.add("generic");
				else
					paradigm.add(s);
			}
		} else {
			paradigm.add(parad);
		}
	}
	
	public void addParadigm(final String paraName) {
		final String par = "0".equals(paraName) ? "generic" : paraName;
		if (!paradigm.contains(par))
			paradigm.add(par);
	}
	
	public void clearParadigm() {
		paradigm = new ArrayList<>();
	}
	
	public List<String> getParadigm() {
		return paradigm;
	}
	
	public void addMeaning(final String text) {
		meaning.add(text);
	}
	
	public List<String> getMeaning() {
		return meaning;
	}
	
	public void addSubmeaning(final String text) {
		submeaning.add(text);
	}
	
	public List<String> getSubmeaning() {
		return submeaning;
	}
	
	public void addForCompounds(final boolean b) {
		forCompounds.add(b);
	}
	
	public List<Boolean> getForCompounds() {
		return forCompounds;
	}
	
	public void setNumericValue(final int value) {
		numericValue = value;
	}
	
	public int getNumericValue() {
		return numericValue;
	}
	
	public void setExpValue(final int value) {
		expValue = value;
	}
	
	public int getExpValue() {
		return expValue;
	}
	
	public boolean isAdjective() {
		boolean result = false;
		for (final String s : posInfo) {
			result = result || s.equals("adj.");
		}
		return result;
	}
	
	public boolean hasAdjDegree() {
		boolean result = false;
		if (isAdjective()) {
			if (lastChar != 't' && lastChar != 'r')
				result = true;
		}
		result = result && !isNumber();
		return result;
	}
		
	public boolean isNumber() {
		boolean result = false;
		for (final String s : paradigm) {
			result = result || s.equals("eka");
			result = result || s.equals("dvi");
			result = result || s.equals("ti");
			result = result || s.equals("catu");
			result = result || s.startsWith("number");
			if (result)
				break;
		}
		return result;
	}
	
	public boolean isDeclinable() {
		boolean result = false;
		for (String s : posInfo) {
			result = result || s.equals("3");
			result = result || s.equals("n.");
			result = result || s.equals("m.");
			result = result || s.equals("f.");
			result = result || s.equals("nt.");
			result = result || s.equals("adj.");
		}
		return result;
	}
	
	public String withSuffix(final String suffix, final Gender gend) {
		String base = stem + insertion.get(gend);
		String end = suffix;
		if (suffix.charAt(0) == '-') {
			// delete backward n chars
			final int n = suffix.charAt(1) - '0';
			end = suffix.substring(2);
			if (stem.length() >= n)
				base = stem.substring(0, stem.length()-n);
		}
		return base + end;
	}
	
	public String[] getComparative(final Gender gend) {
		final String[] result = new String[4];
		final String base = stem + insertion.get(gend);
		result[0] = base + ending.get(gend) + transformEnding("tara", gend);
		result[1] = base + transformEnding("iya", gend);
		result[2] = "ati" + base + ending.get(gend);
		result[3] = "adhi" + base + ending.get(gend);
		return result;
	}
	
	public String[] getSuperative(final Gender gend) {
		final String[] result = new String[3];
		final String base = stem + insertion.get(gend);
		result[0] = base + ending.get(gend) + transformEnding("tama", gend);
		result[1] = base + transformEnding("iṭṭha", gend);
		result[2] = "ativiya" + base + ending.get(gend);
		return result;
	}
	
	/**
	 * Combines two words according to (some) sandhi rules
	 */
	public static String sandhi(final String word1, final String word2) {
		final int len1 = word1.length();
		final int len2 = word2.length();
		String result = word1 + word2;
		if (len1==0 || len2==0)
			return result;
		final char ch1 = word1.charAt(len1-1);
		final char ch2 = word2.charAt(0);
		if ((ch1 == 'a' || ch1 == 'ā') && (ch2 == 'a' || ch2 == 'ā')) {
			result = word1.substring(0, len1-1) + "ā" + word2.substring(1);
		} else if (ch1 == 'u' && ch2 == 'a') {
			result = word1 + "rā" + word2.substring(1);
		} else if ((ch1 == 'a' || ch1 == 'u') && ch2 == 'u') {
			result = word1.substring(0, len1-1) + word2;
		} else if ((ch1 == 'i' || ch1 == 'ī') && ch2 == 'u') {
			result = word1.substring(0, len1-1) + "ay" + word2;
		}
		return result;
	}

}
