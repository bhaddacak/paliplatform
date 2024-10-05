/*
 * PaliRoot.java
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

import paliplatform.base.*;

import java.util.*;

/** 
 * The class handling Pali roots.
 * @author J.R. Bhaddacak
 * @version 3.0
 * @since 2.0
 */
public class PaliRoot {
	public static enum RootGroup {
		I("bhū"), II("rudh"), III("div"), IV("su"), V("kī"), VI("gah"), VII("tan"), VIII("cur");
		public static final RootGroup[] values = values();
		private final String name;
		private RootGroup(final String name) {
			this.name = name;
		}
		public String getName() {
			return this.toString() + " (" + name + ")";
		}
		public static RootGroup fromNumber(final int num) {
			RootGroup result = RootGroup.I;
			for (RootGroup rg : RootGroup.values) {
				if (rg.ordinal() + 1 == num) {
					result = rg;
					break;
				}
			}
			return result;
		}
	}
	private static final String[] causEnds = { "e", "ay", "āpe", "āpay" };
	private static final String[] causPassEnds = { "iy", "ayiy", "āpiy", "āpayiy" };
	private final Integer id;
	private final String root;
	private final RootGroup group;
	private Map<PaliConjugation.Voice, Map<PaliConjugation.TenseMood, List<String>>> stemMap; // map stems to tense/mood
	private Map<PaliConjugation.Voice, Map<String, Set<String>>> paradigmMap; // map paradigm to stem
	private Map<PaliConjugation.Voice, Map<PaliConjugation.DeriPaccaya, List<String>>> deriStemMap; // map stems to derivative paccaya
	private String paliMeaning;
	private String engMeaning;
	private String rootRemark;
	private String meaningRemark;
	
	public PaliRoot(final int id, final String root, final String grpStr) {
		this.id = id;
		this.root = root;
		this.group = Enum.valueOf(PaliRoot.RootGroup.class, grpStr);
		paliMeaning = "";
		engMeaning = "";
		rootRemark = "";
		meaningRemark = "";
	}
	
	public Integer getId() {
		return id;
	}
	
	public String getRoot() {
		return root;
	}

	public RootGroup getGroup() {
		return group;
	}
	
	public void setPaliMeaning(final String meaning) {
		paliMeaning = meaning;
	}
	
	public String getPaliMeaning() {
		return paliMeaning;
	}
	
	public void setEngMeaning(final String meaning) {
		engMeaning = meaning;
	}
	
	public String getEngMeaning() {
		return engMeaning;
	}

	public void setRootRemark(final String remark) {
		rootRemark = remark;
	}
	
	public String getRootRemark() {
		return rootRemark;
	}
	
	public void setMeaningRemark(final String remark) {
		meaningRemark = remark;
	}
	
	public String getMeaningRemark() {
		return meaningRemark;
	}

	public void setStemMap(final String mainGroup, final String voiceGroup, final String deriGroup) {
		stemMap = new EnumMap<>(PaliConjugation.Voice.class);
		paradigmMap = new EnumMap<>(PaliConjugation.Voice.class);
		final String mainStr = mainGroup.replace("0", "generic");
		// process main verb forms--'active voice'
		final Map<PaliConjugation.TenseMood, List<String>> stMap = new EnumMap<>(PaliConjugation.TenseMood.class);
		final Map<String, Set<String>> prMap = new HashMap<>();
		final String[] inputArrMain = mainStr.split("\\|");
		int ind = 0;
		for (PaliConjugation.TenseMood tense : PaliConjugation.TenseMood.values) {
			final List<String> stemList = new ArrayList<>();
			final String[] stm_pdm = inputArrMain[ind].split(",");
			for (final String s : stm_pdm) {
				final String[] sp = s.split("\\.");
				final String stem = sp[0];
				stemList.add(stem);
				final Set<String> paradSet;
				if (prMap.containsKey(stem)) {
					paradSet = prMap.get(stem);
				} else {
					paradSet = new LinkedHashSet<>();
				}
				for (int i=1; i<sp.length; i++) {
					final String pd = sp[i];
					paradSet.add(pd);
				}
				prMap.put(stem, paradSet);
			}
			stMap.put(tense, stemList);
			ind++;
		}
		stemMap.put(PaliConjugation.Voice.ACTI, stMap);
		paradigmMap.put(PaliConjugation.Voice.ACTI, prMap);
		// process other voices
		final String[] inputArrOther = voiceGroup.split(";");
		final Map<PaliConjugation.Voice, List<String>> vStemMap = new EnumMap<>(PaliConjugation.Voice.class);
		final List<String> vStemList = new ArrayList<>(2);
		if (inputArrOther[0].length() > 1) {
			for (final String s : inputArrOther[0].split(","))
				vStemList.add(s);
			vStemMap.put(PaliConjugation.Voice.PASS, vStemList);
		}
		// generate other voices' stems
		if (inputArrOther[1].length() > 1) {
			final String[] cauStem = inputArrOther[1].split(",");
			final List<String> vCStemList = new ArrayList<>();
			final List<String> vCPStemList = new ArrayList<>();
			for (final String ct : cauStem) {
				if (ct.length() > 1) {
					final char last = ct.charAt(ct.length()-1);
					for (int i=0; i<causEnds.length; i++) {
						if (last == 'ā' && i < 2)
							continue;
						vCStemList.add(sandhi(ct, causEnds[i]));
					}
					for (int i=0; i<causPassEnds.length; i++) {
						if (last == 'ā' && i < 2)
							continue;
						vCPStemList.add(sandhi(ct, causPassEnds[i]));
					}
				}
			}
			vStemMap.put(PaliConjugation.Voice.CAUS, vCStemList);
			vStemMap.put(PaliConjugation.Voice.CAUPAS, vCPStemList);
		}
		// add to stemMap and paradigmMap
		if (!vStemMap.isEmpty()) {
			vStemMap.forEach((voice, stemList) -> {
				if (!stemList.isEmpty()) {
					final Map<PaliConjugation.TenseMood, List<String>> stOMap = new EnumMap<>(PaliConjugation.TenseMood.class);
					final Map<String, Set<String>> prOMap = new HashMap<>();
					for (final PaliConjugation.TenseMood tense : PaliConjugation.TenseMood.values) {
						if (tense == PaliConjugation.TenseMood.PAR || tense == PaliConjugation.TenseMood.HIY)
							continue;
						final List<String> stemA = new ArrayList<>();
						for (final String stem : stemList) {
							final Set<String> paradSet = new LinkedHashSet<>();
							final String parad = stem.endsWith("e") ? "vgene" : "vgena";
							paradSet.add(parad);
							prOMap.put(stem, paradSet);
							if (tense == PaliConjugation.TenseMood.AJJ || tense == PaliConjugation.TenseMood.KAL) {
								// add only in non-active voices, for active stems with 'a' are listed in the csv file
								stemA.add("a" + stem);
								prOMap.put("a" + stem, paradSet);
							}
						}
						final List<String> list = new ArrayList<>();
						list.addAll(stemList);
						list.addAll(stemA);
						stOMap.put(tense, list);
					} // end for
					stemMap.put(voice, stOMap);
					paradigmMap.put(voice, prOMap);
				}
			});
		} // end if
		// add stem for derivation
		deriStemMap = createDeriStems(deriGroup, stemMap);
	}

	private static Map<PaliConjugation.Voice, Map<PaliConjugation.DeriPaccaya, List<String>>> 
					createDeriStems(final String inputStr, final Map<PaliConjugation.Voice, Map<PaliConjugation.TenseMood, List<String>>> stemMap) {
		final String givenStems[] = inputStr.split(";");
		final Map<PaliConjugation.Voice, Map<PaliConjugation.DeriPaccaya, List<String>>> result = new EnumMap<>(PaliConjugation.Voice.class);
		Map<PaliConjugation.DeriPaccaya, List<String>> dstMap;
		List<String> base;
		Map<PaliConjugation.Voice, List<String>> baseMap = new EnumMap<>(PaliConjugation.Voice.class);
		// retrieve stem bases
		final PaliConjugation.TenseMood CLASS = PaliConjugation.TenseMood.VAT;
		for (final PaliConjugation.Voice vc : PaliConjugation.Voice.values) {
			final Map<PaliConjugation.TenseMood, List<String>> map = stemMap.get(vc);
			if (map == null)
				baseMap.put(vc, new ArrayList<String>());
			else
				baseMap.put(vc, map.get(CLASS));
		}
		//===== ACTIVE voice =====
		base = baseMap.get(PaliConjugation.Voice.ACTI);
		dstMap = new EnumMap<>(PaliConjugation.DeriPaccaya.class);
		List<String> ntaStems = new ArrayList<>();
		List<String> manaStems = new ArrayList<>();
		List<String> aniyaStems = new ArrayList<>();
		List<String> tabbaStems = new ArrayList<>();
		List<String> taStems = new ArrayList<>();
		List<String> tvaStems = new ArrayList<>();
		String given;
		// NTA & MANA use the main stems
		for (final String ss : base) {
			String stem, end;
			final char last = ss.charAt(ss.length()-1);
			// process NTA
			end = PaliConjugation.DeriPaccaya.NTA.getStemPart();
			given = givenStems[0];
			// skip 'x'
			if (given.charAt(0) == 'x')
				continue;
			if (given.length() > 1) {
				// if given, use it
				for (final String s : given.split(",")) {
					stem = s + end;
					ntaStems.add(stem);
				}
			} else {
				if (PaliCharTransformer.romanVowels.indexOf(last) >= 0) {
					// end with a vowel
					if (last == 'ā')
						stem = ss.substring(0, ss.length()-1) + "a" + end;
					else
						stem = ss + end;
				} else {
					// end with a consonant, add 'a'
					stem = ss + "a" + end;
				}
				ntaStems.add(stem);
			}
			// process MANA
			end = PaliConjugation.DeriPaccaya.MANA.getStemPart();
			given = givenStems[1];
			// skip 'x'
			if (given.charAt(0) == 'x')
				continue;
			if (given.length() > 1) {
				// if given use it
				for (String s : given.split(",")) {
					stem = s + end;
					manaStems.add(stem);
				}
			} else {
				if (PaliCharTransformer.romanVowels.indexOf(last) >= 0) {
					// end with a vowel, replace it with 'a'
					stem = ss.substring(0, ss.length()-1) + "a" + end;
				} else {
					// otherwise add 'a'
					stem = ss + "a" + end;
				}
				manaStems.add(stem);
			}
		} // end for
		// process TA, use only the given stems, no stem part added
		given = givenStems[4];
		if (given.length() > 1) {
			for (final String s : given.split(",")) {
				taStems.add(s);
			}
		}
		// process TVA, use only the given stems, no stem part added
		given = givenStems[5];
		if (given.length() > 1) {
			for (final String s : given.split(",")) {
				tvaStems.add(s);
			}
		}
		dstMap.put(PaliConjugation.DeriPaccaya.NTA, ntaStems);
		dstMap.put(PaliConjugation.DeriPaccaya.MANA, manaStems);
		dstMap.put(PaliConjugation.DeriPaccaya.TA, taStems);
		dstMap.put(PaliConjugation.DeriPaccaya.TVA, tvaStems);
		result.put(PaliConjugation.Voice.ACTI, dstMap);
		//===== PASSIVE =====
		base = baseMap.get(PaliConjugation.Voice.PASS);
		dstMap = new EnumMap<>(PaliConjugation.DeriPaccaya.class);
		if (!base.isEmpty()) {
			manaStems = new ArrayList<>();
			taStems = new ArrayList<>();
			tvaStems = new ArrayList<>();
			for (final String s : base) {
				String stem, end;
				// process MANA
				end = PaliConjugation.DeriPaccaya.MANA.getStemPart();
				stem = s + "a" + end;
				manaStems.add(stem);
				// process TA
				end = PaliConjugation.DeriPaccaya.TA.getStemPart();
				stem = s + "i" + end;
				taStems.add(stem);
				// process TVA
				end = PaliConjugation.DeriPaccaya.TVA.getStemPart();
				stem = s + "i" + end;
				tvaStems.add(stem);
			} // end for
		}
		// ANIYA & TABBA are counted as passive
		// process ANIYA, use only the given stems, no stem part added
		given = givenStems[2];
		if (given.length() > 1) {
			for (final String s : given.split(",")) {
				aniyaStems.add(s);
			}
		}		
		// process TABBA, use only the given stems
		given = givenStems[3];
		if (given.length() > 1) {
			String stem, end;
			end = PaliConjugation.DeriPaccaya.TABBA.getStemPart();
			for (final String s : given.split(",")) {
				stem = s + end;
				tabbaStems.add(stem);
			}
		}
		dstMap.put(PaliConjugation.DeriPaccaya.MANA, manaStems);
		dstMap.put(PaliConjugation.DeriPaccaya.ANIYA, aniyaStems);
		dstMap.put(PaliConjugation.DeriPaccaya.TABBA, tabbaStems);
		dstMap.put(PaliConjugation.DeriPaccaya.TA, taStems);
		dstMap.put(PaliConjugation.DeriPaccaya.TVA, tvaStems);
		result.put(PaliConjugation.Voice.PASS, dstMap);
		//===== CAUSATIVE =====
		base = baseMap.get(PaliConjugation.Voice.CAUS);
		dstMap = new EnumMap<>(PaliConjugation.DeriPaccaya.class);
		if (!base.isEmpty()) {
			ntaStems = new ArrayList<>();
			manaStems = new ArrayList<>();
			taStems = new ArrayList<>();
			tvaStems = new ArrayList<>();
			for (final String s : base) {
				String stem, end;
				char last = s.charAt(s.length()-1);
				// process NTA
				end = PaliConjugation.DeriPaccaya.NTA.getStemPart();
				if (PaliCharTransformer.romanVowels.indexOf(last) >= 0) {
					// end with a vowel
					stem = s + end;
				} else {
					// end with a consonant, add 'a'
					stem = s + "a" + end;
				}
				ntaStems.add(stem);
				// process MANA
				end = PaliConjugation.DeriPaccaya.MANA.getStemPart();
				if (PaliCharTransformer.romanVowels.indexOf(last) < 0) {
					// if not end with a vowel add 'a'
					stem = s + "a" + end;
					manaStems.add(stem);
				}
				// process TA
				end = PaliConjugation.DeriPaccaya.TA.getStemPart();
				if (PaliCharTransformer.romanVowels.indexOf(last) >= 0) {
					// end with a vowel
					if (last == 'e')
						stem = s.substring(0, s.length()-1) + "i" + end;
				} else {
					// end with a consonant, add 'i'
					stem = s + "i" + end;
				}
				taStems.add(stem);
				// process TVA
				end = PaliConjugation.DeriPaccaya.TVA.getStemPart();
				if (PaliCharTransformer.romanVowels.indexOf(last) >= 0) {
					// end with a vowel
					stem = s + end;
				} else {
					// end with a consonant, add 'i'
					stem = s + "i" + end;
				}
				tvaStems.add(stem);
			} // end for
			dstMap.put(PaliConjugation.DeriPaccaya.NTA, ntaStems);
			dstMap.put(PaliConjugation.DeriPaccaya.MANA, manaStems);
			dstMap.put(PaliConjugation.DeriPaccaya.TA, taStems);
			dstMap.put(PaliConjugation.DeriPaccaya.TVA, tvaStems);
			result.put(PaliConjugation.Voice.CAUS, dstMap);
		}
		//===== CAUSAL PASSIVE =====
		dstMap = new EnumMap<>(PaliConjugation.DeriPaccaya.class);
		base = baseMap.get(PaliConjugation.Voice.CAUPAS);
		if (!base.isEmpty()) {
			manaStems = new ArrayList<>();
			taStems = new ArrayList<>();
			tvaStems = new ArrayList<>();
			for (final String s : base) {
				String stem, end;
				char last = s.charAt(s.length()-1);
				// process MANA
				end = PaliConjugation.DeriPaccaya.MANA.getStemPart();
				if (PaliCharTransformer.romanVowels.indexOf(last) < 0) {
					// if not end with a vowel, add 'a'
					stem = s + "a" + end;
					manaStems.add(stem);
				}
				// process TA
				end = PaliConjugation.DeriPaccaya.TA.getStemPart();
				if (PaliCharTransformer.romanVowels.indexOf(last) >= 0) {
					// end with a vowel
					stem = s + end;
				} else {
					// delete 'iy' at the end, for only 'i' is added
					stem = s.substring(0, s.length()-2) + "i" + end;
				}
				taStems.add(stem);
				// process TVA
				end = PaliConjugation.DeriPaccaya.TVA.getStemPart();
				if (PaliCharTransformer.romanVowels.indexOf(last) >= 0) {
					// end with a vowel
					stem = s + end;
				} else {
					// delete 'iy' at the end, for only 'i' is added
					stem = s.substring(0, s.length()-2) + "i" + end;
				}
				tvaStems.add(stem);
			} // end for
		}
		// ANIYA & TABBA use causative stems
		base = baseMap.get(PaliConjugation.Voice.CAUS);
		if (!base.isEmpty()) {
			aniyaStems = new ArrayList<>();
			tabbaStems = new ArrayList<>();
			for (final String s : base) {
				String stem, end;
				char last = s.charAt(s.length()-1);				
				// process ANIYA
				end = PaliConjugation.DeriPaccaya.ANIYA.getStemPart();
				if (PaliCharTransformer.romanVowels.indexOf(last) < 0) {
					// if not end with a vowel
					stem = s + end;
					aniyaStems.add(stem);
				}
				// process TABBA
				end = "t" + PaliConjugation.DeriPaccaya.TABBA.getStemPart();
				if (PaliCharTransformer.romanVowels.indexOf(last) >= 0) {
					// end with a vowel
					stem = s + end;
				} else {
					// end with a consonant, add 'i'
					stem = s + "i" + end;
				}
				tabbaStems.add(stem);					
			}
		}
		dstMap.put(PaliConjugation.DeriPaccaya.MANA, manaStems);
		dstMap.put(PaliConjugation.DeriPaccaya.ANIYA, aniyaStems);
		dstMap.put(PaliConjugation.DeriPaccaya.TABBA, tabbaStems);
		dstMap.put(PaliConjugation.DeriPaccaya.TA, taStems);
		dstMap.put(PaliConjugation.DeriPaccaya.TVA, tvaStems);
		result.put(PaliConjugation.Voice.CAUPAS, dstMap);
		return result;	
	}

	public Map<PaliConjugation.Voice, Map<PaliConjugation.TenseMood, List<String>>> getStemMap() {
		return stemMap;
	}
	
	public Map<PaliConjugation.TenseMood, List<String>> getStemMap(final PaliConjugation.Voice voice) {
		return stemMap.get(voice);
	}	
		
	public String getStem() {
		return stemMap.get(PaliConjugation.Voice.ACTI).get(PaliConjugation.TenseMood.VAT).get(0);
	}
	
	public List<String> getStem(final PaliConjugation.Voice voice, final PaliConjugation.TenseMood tense) {
		final Map<PaliConjugation.TenseMood, List<String>> map = stemMap.get(voice);
		final List<String> result;
		if (map != null) {
			result = map.get(tense);
		} else {
			result = new ArrayList<>();
		}
		return result;
	}
	
	public List<String> getStems(final PaliConjugation.TenseMood tense) {
		return stemMap.get(PaliConjugation.Voice.ACTI).get(tense);
	}
	
	public boolean hasStems(final PaliConjugation.Voice voice) {
		final boolean result;
		final Map<PaliConjugation.TenseMood, List<String>> stMap = stemMap.get(voice);
		if (stMap == null)
			result = false;
		else
			result = stMap.values().stream().anyMatch(x -> !x.isEmpty());
		return result;
	}
	
	public boolean hasStems(final PaliConjugation.Voice voice, final PaliConjugation.TenseMood tense) {
		final boolean result;
		final Map<PaliConjugation.TenseMood, List<String>> stMap = stemMap.get(voice);
		final List<String> stList = stMap.get(tense);
		if (stList == null)
			result = false;
		else
			result = !stList.isEmpty();
		return result;
	}
	
	public Map<PaliConjugation.Voice, Map<PaliConjugation.DeriPaccaya, List<String>>> getDeriStemMap() {
		return deriStemMap;
	}
	
	public Map<PaliConjugation.DeriPaccaya, List<String>> getDeriStemMap(final PaliConjugation.Voice voice) {
		return deriStemMap.get(voice);
	}	
		
	public List<String> getStems(final PaliConjugation.DeriPaccaya paccaya) {
		return deriStemMap.get(PaliConjugation.Voice.ACTI).get(paccaya);
	}
		
	public boolean hasDeriStems(final PaliConjugation.Voice voice) {
		final boolean result;
		final Map<PaliConjugation.DeriPaccaya, List<String>> stMap = deriStemMap.get(voice);
		if (stMap == null)
			result = false;
		else
			result = stMap.values().stream().anyMatch(x -> !x.isEmpty());
		return result;
	}
	
	public boolean hasDeriStems(final PaliConjugation.Voice voice, final PaliConjugation.DeriPaccaya paccaya) {
		final boolean result;
		final Map<PaliConjugation.DeriPaccaya, List<String>> stMap = deriStemMap.get(voice);
		if (stMap == null) {
			result = false;
		} else {
			final List<String> stList = stMap.get(paccaya);
			if (stList == null)
				result = false;
			else
				result = !stList.isEmpty();
		}
		return result;
	}

	public Map<PaliConjugation.Voice, Map<String, Set<String>>> getParadigmMap() {
		return paradigmMap;
	}
	
	public Map<String, Set<String>> getParadigmMap(final PaliConjugation.Voice voice) {
		return paradigmMap.get(voice);
	}
	
	public Set<String> getParadigm(final String stem, final PaliConjugation.Voice voice) {
		final Map<String, Set<String>> prMap = paradigmMap.get(voice);
		return prMap.get(stem);
	}

	public List<String> withSuffix(final String stem, final String suffix, final int minLength) {
		final List<String> result = new ArrayList<>();
		String end = suffix.charAt(0)=='0'? "": suffix;
		String base = stem;
		if (suffix.charAt(0) == '-') {
			// delete backward n chars
			final int n = suffix.charAt(1) - '0';
			end = suffix.substring(2);
			if (stem.length() >= n)
				base = stem.substring(0, stem.length()-n);
		}
		final String fin = sandhi(base, end);
		if (fin.length() >= minLength)
			result.add(fin);
		return result;
	}

	public static String sandhi(final String word1, final String word2) {
		final int len1 = word1.length();
		final int len2 = word2.length();
		String result = word1 + word2;
		if (len1 == 0 || len2 == 0)
			return result;
		final char ch1 = word1.charAt(len1-1);
		final char ch2 = word2.charAt(0);
		if ((ch1 == 'a' || ch1 == 'ā') && (ch2 == 'a' || ch2 == 'ā')) {
			result = word1.substring(0, len1-1) + "ā" + word2.substring(1);
		} else if ((ch1 == 'a' || ch1 == 'e') && ch2 == 'e') {
			result = word1.substring(0, len1-1) + word2;
		} else if (ch1 == 'a' && ch2 == 'ī') {
			result = word1.substring(0, len1-1) + word2;
		} else if (ch1 == 'e' && (ch2 == 'a' || ch2 == 'ā')) {
			result = word1 + word2.substring(1);
		} else if (ch1 == 'e' && (ch2 == 'i' || ch2 == 'ī')) {
			result = word1.substring(0, len1-1) + word2;
		} else if (ch1 == 'o' && (ch2 == 'a' || ch2 == 'ā')) {
			result = word1 + word2.substring(1);
		}
		return result;
	}

}
