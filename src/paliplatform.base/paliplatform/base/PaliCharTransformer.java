/*
 * PaliCharTransformer.java
 *
 * Copyright (C) 2023-2025 J. R. Bhaddacak 
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
 * This factory class transforms Pali scripts to one another.
 * @author J.R. Bhaddacak
 * @version 3.0
 * @since 1.0
 */
public class PaliCharTransformer {
	public static final String romanVowels = "aāiīuūeo";
	public static final String romanVowelsForDeva = "aāiīuūeēoō";
	public static final String romanConsonants = "kgṅcjñṭḍṇtdnpbmyrlvshḷ"; //śṣṛṝḹ";
	private static final String romanWithHChars = "bcdgjkptḍṭ";
	public static final char[] romanNumbers = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' };
	private static final char romanBar = '|';
	private static final char romanDoubleBar = '\u2016';
	private static final char romanAbbrSign = '\u00B7';
	public static final char[] romanConsonantsChr = { 	
		'k', 'x', 'g', 'x', 'ṅ',
		'c', 'x', 'j', 'x', 'ñ',
		'ṭ', 'x', 'ḍ', 'x', 'ṇ',
		't', 'x', 'd', 'x', 'n',
		'p', 'x', 'b', 'x', 'm',
		'y', 'r', 'l', 'v', 's', 'h', 'ḷ', 'ṃ' };
//~ 		'ś', 'ṣ', 'ṛ', 'ṝ', 'ḹ' }; // these cause conversion problems
	public static final String[] romanConsonantsStr = { 	
		"k", "kh", "g", "gh", "ṅ",
		"c", "ch", "j", "jh", "ñ",
		"ṭ", "ṭh", "ḍ", "ḍh", "ṇ",
		"t", "th", "d", "dh", "n",
		"p", "ph", "b", "bh", "m",
		"y", "r", "l", "v", "s", "h", "ḷ", "ṃ" };
//~ 		"ś", "ṣ", "ṛ", "ṝ", "ḹ" };
	// Thai set
	public static final String romanVowelsForT2R = "aāiīiuūeo";
	public static final char[] thaiVowelsForT2R = { '\u0E2D', '\u0E32', '\u0E34', '\u0E35', '\u0E36', '\u0E38', '\u0E39', '\u0E40', '\u0E42' };
	public static final char[] thaiVowels = { '\u0E2D', '\u0E32', '\u0E34', '\u0E35', '\u0E38', '\u0E39', '\u0E40', '\u0E42' };
	public static final char[] thaiNumbers = { '\u0E50', '\u0E51', '\u0E52', '\u0E53', '\u0E54', '\u0E55', '\u0E56', '\u0E57', '\u0E58', '\u0E59' };
	private static final char thaiBindu = '\u0E3A';
	private static final char thaiPeriod = '\u0E2F';
	private static final char thaiDoublePeriod = '\u0E5A';
	public static final char[] thaiConsonants = {
		'\u0E01', '\u0E02', '\u0E04', '\u0E06', '\u0E07',
		'\u0E08', '\u0E09', '\u0E0A', '\u0E0C', '\u0E0D',
		'\u0E0F', '\u0E10', '\u0E11', '\u0E12', '\u0E13',
		'\u0E15', '\u0E16', '\u0E17', '\u0E18', '\u0E19',
		'\u0E1B', '\u0E1C', '\u0E1E', '\u0E20', '\u0E21',
		'\u0E22', '\u0E23', '\u0E25', '\u0E27', '\u0E2A', '\u0E2B', '\u0E2C', '\u0E4D' };
//~ 		'\u0E28', '\u0E29', '\u0E24', '\u0E24', '\u0E26' };
	public static final char[] altPaliThaiChars = { '\uF70F', '\uF700' }; // in case that 0E0D and 0E10 are not desirable
	private static final Map<Character, String> altThaiCharsMap = new HashMap<>();
	static {
		altThaiCharsMap.put('\uF70F', "ñ");
		altThaiCharsMap.put('\uF700', "ṭ");
	}
	// Khmer set
	public static final char[] khmerVowelsInd = { '\u17A2', '\u17B6', '\u17A5', '\u17A6', '\u17A7', '\u17A9', '\u17AF', '\u17B1' };
	private static final char[] khmerVowelsDep = { '\u17A2', '\u17B6', '\u17B7', '\u17B8', '\u17BB', '\u17BC', '\u17C1', '\u17C4' };
	public static final char[] khmerNumbers = { '\u17E0', '\u17E1', '\u17E2', '\u17E3', '\u17E4', '\u17E5', '\u17E6', '\u17E7', '\u17E8', '\u17E9' };
	private static final char khmerKiller = '\u17D1';
	private static final char khmerCoeng = '\u17D2';
	private static final char khmerPeriod = '\u17D4';
	private static final char khmerDoublePeriod = '\u17D5';
	public static final char[] khmerConsonants = {
		'\u1780', '\u1781', '\u1782', '\u1783', '\u1784',
		'\u1785', '\u1786', '\u1787', '\u1788', '\u1789',
		'\u178A', '\u178B', '\u178C', '\u178D', '\u178E',
		'\u178F', '\u1790', '\u1791', '\u1792', '\u1793',
		'\u1794', '\u1795', '\u1796', '\u1797', '\u1798',
		'\u1799', '\u179A', '\u179B', '\u179C', '\u179F', '\u17A0', '\u17A1', '\u17C6' };
//~ 		'\u179D', '\u179E', '\u17AB', '\u17AC', '\u17AE' };
	// Myanmar set
	private static final char myanmarTallA = '\u102B';
	private static final char myanmarShortA = '\u102C';
	private static final char myanmarDepE = '\u1031'; // this causes problem, not used in displaying
	private static final char myanmarSpecialE = '\u102A';
	public static final char[] myanmarVowelsInd = { '\u1021', myanmarShortA, '\u1023', '\u1024', '\u1025', '\u1026', '\u1027', '\u1029' };
	private static final char[] myanmarVowelsDep = { '\u1021', myanmarShortA, '\u102D', '\u102E', '\u102F', '\u1030', myanmarSpecialE, myanmarSpecialE };
	private static final char[] myanmarVowelsDepStraight = { '\u1021', myanmarShortA, '\u102D', '\u102E', '\u102F', '\u1030', myanmarDepE, myanmarDepE };
	public static final char[] myanmarNumbers = { '\u1040', '\u1041', '\u1042', '\u1043', '\u1044', '\u1045', '\u1046', '\u1047', '\u1048', '\u1049' };
	private static final char myanmarVirama = '\u1039';
	private static final char myanmarAsat = '\u103A';
	private static final char myanmarPeriod = '\u104A';
	private static final char myanmarDoublePeriod = '\u104B';
	public static final char[] myanmarConsonants = {
		'\u1000', '\u1001', '\u1002', '\u1003', '\u1004',
		'\u1005', '\u1006', '\u1007', '\u1008', '\u1009',
		'\u100B', '\u100C', '\u100D', '\u100E', '\u100F',
		'\u1010', '\u1011', '\u1012', '\u1013', '\u1014',
		'\u1015', '\u1016', '\u1017', '\u1018', '\u1019',
		'\u101A', '\u101B', '\u101C', '\u101D', '\u101E', '\u101F', '\u1020', '\u1036' };
//~ 		'\u1050', '\u1051', ' ', ' ', ' ' };
	private static final Map<Character, Character> myanmarToMedialMap = Map.of(
			'\u101A', '\u103B', // y
			'\u101B', '\u103C', // r
			'\u101D', '\u103D', // v
			'\u101F', '\u103E' ); // h
	private static final Map<Character, String> myanmarFromMedialMap = Map.of(
			'\u103B', "" + myanmarVirama + '\u101A', 
			'\u103C', "" + myanmarVirama + '\u101B', 
			'\u103D', "" + myanmarVirama + '\u101D', 
			'\u103E', "" + myanmarVirama + '\u101F' );
	private static final char myanmarNga = '\u1004';
	private static final char myanmarNya = '\u1009';
	private static final char myanmarNnya = '\u100A';
	private static final char myanmarSa = '\u101E';
	private static final char myanmarGreatSa = '\u103F';
	private static final Set<String> myanmarTallASet = Set.of(
			"" + '\u1001' + myanmarShortA, // kh
			"" + '\u1002' + myanmarShortA, // g
			"" + '\u1004' + myanmarShortA, // ṅ
			"" + '\u1012' + myanmarShortA, // d
			"" + '\u1015' + myanmarShortA, // p
			"" + '\u101D' + myanmarShortA, // v
			"" + '\u1004' + myanmarVirama + '\u1001' + myanmarShortA, // ṅkh
			"" + '\u1004' + myanmarVirama + '\u1002' + myanmarShortA, // ṅg
			"" + '\u1004' + myanmarVirama + '\u1004' + myanmarShortA, // ṅṅ
			"" + '\u1012' + myanmarVirama + '\u1012' + myanmarShortA, // dd
			"" + '\u1012' + myanmarVirama + '\u1013' + myanmarShortA, // ddh
			"" + '\u1012' + myanmarVirama + '\u1019' + myanmarShortA, // dm
			"" + '\u1012' + myanmarVirama + '\u101D' + myanmarShortA // dv
			);
	private static final Set<String> myanmarShortASet = Set.of(
			"" + '\u1000' + myanmarVirama + '\u1001' + myanmarTallA, // kkh
			"" + '\u1002' + myanmarVirama + '\u1002' + myanmarTallA, // gg
			"" + '\u1015' + myanmarVirama + '\u1015' + myanmarTallA, // pp
			"" + '\u1019' + myanmarVirama + '\u1015' + myanmarTallA, // mp
			"" + myanmarVirama + '\u101D' + myanmarTallA // xv
			);
	// Sinhala set
	public static final char[] sinhalaVowelsInd = { '\u0D85', '\u0D86', '\u0D89', '\u0D8A', '\u0D8B', '\u0D8C', '\u0D91', '\u0D94' };
	private static final char[] sinhalaVowelsDep = { '\u0D85', '\u0DCF', '\u0DD2', '\u0DD3', '\u0DD4', '\u0DD6', '\u0DD9', '\u0DDC' };
	private static final char sinhalaVirama = '\u0DCA';
	public static final char[] sinhalaConsonants = {
		'\u0D9A', '\u0D9B', '\u0D9C', '\u0D9D', '\u0D9E',
		'\u0DA0', '\u0DA1', '\u0DA2', '\u0DA3', '\u0DA4',
		'\u0DA7', '\u0DA8', '\u0DA9', '\u0DAA', '\u0DAB',
		'\u0DAD', '\u0DAE', '\u0DAF', '\u0DB0', '\u0DB1',
		'\u0DB4', '\u0DB5', '\u0DB6', '\u0DB7', '\u0DB8',
		'\u0DBA', '\u0DBB', '\u0DBD', '\u0DC0', '\u0DC3', '\u0DC4', '\u0DC5', '\u0D82' };
//~ 		'\u0DC1', '\u0DC2', '\u0D8D', '\u0D8E', '\u0D90' };
	// Devanagari set
	public static final char[] devaVowelsInd = { '\u0905', '\u0906', '\u0907', '\u0908', '\u0909', '\u090A', '\u090F', '\u0910', '\u0913', '\u0914' };
	private static final char[] devaVowelsDep = { '\u0905', '\u093E', '\u093F', '\u0940', '\u0941', '\u0942', '\u0947', '\u0948', '\u094B', '\u094C' };
	public static final char[] devaNumbers = { '\u0966', '\u0967', '\u0968', '\u0969', '\u096A', '\u096B', '\u096C', '\u096D', '\u096E', '\u096F' };
	private static final char devaVirama = '\u094D';
	private static final char devaPeriod = '\u0964';
	private static final char devaDoublePeriod = '\u0965';
	private static final char devaAbbrev = '\u0970';
	public static final char[] devaConsonants = {
		'\u0915', '\u0916', '\u0917', '\u0918', '\u0919',
		'\u091A', '\u091B', '\u091C', '\u091D', '\u091E',
		'\u091F', '\u0920', '\u0921', '\u0922', '\u0923',
		'\u0924', '\u0925', '\u0926', '\u0927', '\u0928',
		'\u092A', '\u092B', '\u092C', '\u092D', '\u092E',
		'\u092F', '\u0930', '\u0932', '\u0935', '\u0938', '\u0939', '\u0933', '\u0902' };
//~ 		'\u0936', '\u0937', '\u090B', '\u0960', '\u090C' };
	private static boolean alsoNumber = true;
	
	private PaliCharTransformer() {
	}
	
	/**
	 * Sets up initial condition before Thai transformation.
	 */
	public static void setUsingAltThaiChars(final boolean useAlt) {
		if (useAlt) {
			thaiConsonants[9] = '\uF70F';
			thaiConsonants[11] = '\uF700';
			altThaiCharsMap.put('\u0E0D', "ñ");
			altThaiCharsMap.put('\u0E10', "ṭ");
		} else {
			thaiConsonants[9] = '\u0E0D';
			thaiConsonants[11] = '\u0E10';
			altThaiCharsMap.put('\uF70F', "ñ");
			altThaiCharsMap.put('\uF700', "ṭ");
		}
	}
	
	public static void setIncludingNumbers(final boolean yn) {
		alsoNumber = yn;
	}

	public static String romanToThai(final String str) {
		final StringBuilder output = new StringBuilder();
		char[] input = str.toLowerCase().toCharArray();
		char rch;
		char tch;
		int vindex = -1; // for vowels
		boolean skipFlag = false;
		for (int index = 0; index<input.length; index++) {
			if (skipFlag) {
				skipFlag = false;
				continue;
			}
			rch = input[index];
			tch = rch; // in case of non-character
			// 1. find Thai representation of the character first
			if (Character.isDigit(rch)) {
				// is number
				if (alsoNumber)
					tch = thaiNumbers[Character.digit(rch, 10)];
			} else if (rch == '.') {
				// period is hard to differentiate from a normal dot, so retain as dot;
				tch = '.';
			} else if (rch == romanBar) {
				// single bar is changed to single danda
				tch = thaiPeriod;
			} else if (rch == romanDoubleBar) {
				// double bar is changed to double danda
				tch = thaiDoublePeriod;
			} else if (rch == 'x') {
				// reserved character
				tch = rch;
			} else if ((vindex = romanVowels.indexOf(rch)) >= 0) {
				// is vowels
				tch = thaiVowels[vindex];
			} else {
				// is consonants
				for (int i=0; i<romanConsonantsChr.length; i++) {
					if (rch == romanConsonantsChr[i]) {
						if (index < input.length-1) {
							// if the character has 'h'
							if (romanWithHChars.indexOf(rch) >= 0 && input[index+1] == 'h')
								skipFlag = true;
						}
						tch = skipFlag ? thaiConsonants[i+1] : thaiConsonants[i];
						break;
					}
				} // end for loop of finding pali consonant
			}
			// 2. consider how to put it
			if (vindex >= 0) {
				// vowels
				if (output.length() == 0) {
					// to prevent index out of bound
					// independent vowels
					if (rch == 'a') {
						output.append(tch);
					} else if (rch == 'e' || rch == 'o') {
						// transposition is needed
						output.append(tch);
						output.append(thaiVowels[0]);
					} else {
						output.append(thaiVowels[0]);
						output.append(tch);
					}
				} else {
					// look at the preceeding character; if it is not a consonant, 'a' is added
					if (romanConsonants.indexOf(input[index-1]) < 0) {
						// not follow a consonant
						if (rch == 'a') {
							output.append(tch);
						} else if (rch == 'e' || rch == 'o') {
							// transposition is needed
							output.append(tch);
							output.append(thaiVowels[0]);
						} else {
							output.append(thaiVowels[0]);
							output.append(tch);
						}						
					} else {
						// follow a consonant
						if (rch == 'e' || rch == 'o') {
							// transposition is needed
							output.insert(output.length()-1, tch);
						} else if (rch != 'a') {
							output.append(tch);
						}		
					}
				}
			} else {
				if (romanConsonants.indexOf(rch) >= 0) {
					// consonants
					output.append(tch);
					if (index < input.length-1) {
						if (!skipFlag) {
							if (romanConsonants.indexOf(input[index+1]) >= 0) {
								// double consonant needs bindu
								output.append(thaiBindu);
							} else if (romanVowels.indexOf(input[index+1]) == -1) {
								// if not followed by a vowel, add bindu
								output.append(thaiBindu);
							}
						} else {
							// characters with h
							if (index < input.length-2) {
								if (romanConsonants.indexOf(input[index+2]) >= 0) {
									// double consonant needs bindu
									output.append(thaiBindu);
								} else if (romanVowels.indexOf(input[index+2]) == -1) {
									// if not followed by a vowel, add bindu
									output.append(thaiBindu);
								}
							} else {
								// the last consonant, add bindu
								output.append(thaiBindu);
							}
						}
					} else {
						// the last consonant, add bindu
						output.append(thaiBindu);
					}
				} else {
					// others
					output.append(tch);
				}
			}
			vindex = -1;
		} // end for loop of each input character
		return output.toString();
	}
	
	public static String thaiToRoman(final String str) {
		final StringBuilder output = new StringBuilder();
		char[] input = str.toLowerCase().toCharArray();
		// generate hash maps to ease the replacements
		final Map<Character, Character> vowelMap = new HashMap<>();
		for (int i=0; i<thaiVowelsForT2R.length; i++)
			vowelMap.put(thaiVowelsForT2R[i], romanVowelsForT2R.charAt(i));
		final Map<Character, String> consonantMap = new HashMap<>();
		for (int i=0; i<thaiConsonants.length; i++)
			consonantMap.put(thaiConsonants[i], romanConsonantsStr[i]);
		String rch;
		char tch;
		int ind; // general purpose index
		String suspendedChar = ""; // used for 'e' and 'o'
		boolean skipFlag = false;
		for (int index = 0; index<input.length; index++) {
			if (skipFlag) {
				skipFlag = false;
				continue;
			}
			tch = input[index];
			rch = Character.toString(tch); // in case of non-character
			// 1. find Roman representation
			if ((ind = Arrays.binarySearch(thaiNumbers, tch)) >= 0) {
				// numbers
				rch = Character.toString(romanNumbers[ind]);
			} else if (tch == thaiPeriod) {
				// bar
				rch = "" + romanBar;
			} else if (tch == thaiDoublePeriod) {
				// double bar
				rch = "" + romanDoubleBar;
			} else if (tch == '\u0E40' || tch == '\u0E42') {
				// vowel e and o
				rch = Character.toString(vowelMap.get(tch));
			} else if (tch == '\u0E2D'){
				// o-ang ('a')
				if (index < input.length-1) {
					if (input[index+1] == '\u0E32' || input[index+1] == '\u0E34' || input[index+1] == '\u0E35' ||
						input[index+1] == '\u0E36' || input[index+1] == '\u0E38' || input[index+1] == '\u0E39') {
						rch = Character.toString(vowelMap.get(input[index+1]));
						if (input[index+1] == '\u0E36')
							rch = rch + "ṃ";
						skipFlag = true;
					} else {
						rch = "a";
					}
				} else {
					rch = "a";
				}
			} else if (tch == '\u0E32' || tch == '\u0E34' || tch == '\u0E35' || tch == '\u0E36' ||
										 tch == '\u0E38' || tch == '\u0E39') {
				// other vowels after consonants
				rch = Character.toString(vowelMap.get(tch));
			} else {
				// consonants
				rch = altThaiCharsMap.get(tch);
				if (rch == null)
					rch = consonantMap.get(tch);
				if (rch == null)
					rch = Character.toString(tch);
			}
			// 2. consider how to put it
			if (tch == '\u0E40' || tch == '\u0E42') {
				// 'e' and 'o', consider the next letter, (transposition is needed)
				if (index < input.length-1) {
					if (input[index+1] == '\u0E2D') {
						// followed by 'a', skip one char
						skipFlag = true;
						output.append(rch);
					} else {
						// followed by a consonant
						// suspension is needed
						suspendedChar = rch;
					}
				}
			} else if (tch == '\u0E36') {
				output.append(rch + 'ṃ');
			} else {
				output.append(rch);
				if (index < input.length-1) {
					if (input[index+1] == thaiBindu) {
						// skip bindu
						skipFlag = true;
					} else if (consonantMap.get(tch) != null && tch != '\u0E4D' && input[index+1] != '\u0E32' &&
								input[index+1] != '\u0E34' && input[index+1] != '\u0E35' && input[index+1] != '\u0E36' &&
								input[index+1] != '\u0E38' && input[index+1] != '\u0E39') {
						if (suspendedChar.length() > 0) {
							// if any suspension from 'e' or 'o', put it here
							output.append(suspendedChar);
							suspendedChar = "";
						} else {
							// double Thai consonants, 'a' is added (also consider the following 'e' and 'o')
							output.append('a');
						}
					}
				} else {
					if (suspendedChar.length() > 0) {
						// if any suspension from 'e' or 'o', put it here
						output.append(suspendedChar);
						suspendedChar = "";
					} else {
						// if the last char is consonant, not a niggahita, add 'a'
						if (consonantMap.get(tch) != null && tch != '\u0E4D')
							output.append('a');
					}
				}
			}
		}
		return output.toString();
	}
	
	public static String romanToKhmer(final String str) {
		final StringBuilder output = new StringBuilder();
		char[] input = str.toLowerCase().toCharArray();
		char rch;
		char kch;
		int vindex = -1; // for vowels
		boolean skipFlag = false;
		for (int index = 0; index<input.length; index++) {
			if (skipFlag) {
				skipFlag = false;
				continue;
			}
			rch = input[index];
			kch = rch; // in case of non-character
			// 1. find Khmer representation of the character first
			if (Character.isDigit(rch)) {
				// is number
				if (alsoNumber)
					kch = khmerNumbers[Character.digit(rch, 10)];
			} else if (rch == '.') {
				// period is retained as dot
				kch = '.';
			} else if (rch == romanBar) {
				// single bar is changed to single danda
				kch = khmerPeriod;
			} else if (rch == romanDoubleBar) {
				// double bar is changed to double danda
				kch = khmerDoublePeriod;
			} else if (rch == 'x') {
				// reserved character
				kch = rch;
			} else if ((vindex = romanVowels.indexOf(rch)) >= 0) {
				// is vowels
				kch = khmerVowelsInd[vindex];
			} else {
				// is consonants
				for (int i=0; i<romanConsonantsChr.length; i++) {
					if (rch == romanConsonantsChr[i]) {
						if (index < input.length-1) {
							// if the character has 'h'
							if (romanWithHChars.indexOf(rch) >= 0 && input[index+1] == 'h')
								skipFlag = true;
						}
						kch = skipFlag ? khmerConsonants[i+1] : khmerConsonants[i];
						break;
					}
				} // end for loop of finding pali consonant
			}
			// 2. consider how to put it
			if (vindex >= 0) {
				// vowels
				if (output.length() == 0) {
					// to prevent index out of bound
					// independent vowels
					if (rch == 'ā') {
						// this needs 2 chars
						output.append(khmerVowelsInd[0]);
						output.append(kch);
					} else {
						output.append(kch);
					}
				} else {
					// look at the preceeding character; if it is not a consonant, independent vowels are used
					if (romanConsonants.indexOf(input[index-1]) < 0) {
						if (rch == 'ā') {
							// this needs 2 chars
							output.append(khmerVowelsInd[0]);
							output.append(kch);
						} else {
							output.append(kch);
						}						
					} else {
						// dependent vowels are used
						if (rch != 'a') {
							output.append(khmerVowelsDep[vindex]);
						}
					}
				}
			} else {
				if (romanConsonants.indexOf(rch) >= 0) {
					// consonants
					output.append(kch);
					if (index < input.length-1) {
						if (!skipFlag) {
							if (romanConsonants.indexOf(input[index+1]) >= 0) {
								// double consonant needs coeng 0x17D2
								output.append(khmerCoeng);
							} else if (romanVowels.indexOf(input[index+1]) == -1) {
								// if not followed by a vowel, add killer
								output.append(khmerKiller);
							}
						} else {
							// characters with h
							if (index < input.length-2) {
								if (romanConsonants.indexOf(input[index+2]) >= 0) {
									// double consonant needs coeng
									output.append(khmerCoeng);
								} else if (romanVowels.indexOf(input[index+2]) == -1) {
									// if not followed by a vowel, add killer
									output.append(khmerKiller);
								}
							} else {
								// the last consonant, add killer
								output.append(khmerKiller);
							}
						}
					} else {
						// the last consonant, add killer
						output.append(khmerKiller);
					}
				} else {
					// others
					output.append(kch);
				}
			}
			vindex = -1;
		} // end for loop of each input character
		return output.toString();
	}

	public static String khmerToRoman(final String str) {
		final StringBuilder output = new StringBuilder();
		char[] input = str.toLowerCase().toCharArray();
		// generate hash maps to ease the replacements
		final Map<Character, Character> indVowelMap = new HashMap<>();
		final Map<Character, Character> depVowelMap = new HashMap<>();
		for (int i=0; i<khmerVowelsInd.length; i++)
			indVowelMap.put(khmerVowelsInd[i], romanVowels.charAt(i));
		for (int i=0; i<khmerVowelsDep.length; i++)
			depVowelMap.put(khmerVowelsDep[i], romanVowels.charAt(i));
		final Map<Character, String> consonantMap = new HashMap<>();
		for (int i=0; i<khmerConsonants.length; i++)
			consonantMap.put(khmerConsonants[i], romanConsonantsStr[i]);
		String rch;
		char kch;
		int ind; // general purpose index
		boolean skipFlag = false;
		for (int index = 0; index<input.length; index++) {
			if (skipFlag) {
				skipFlag = false;
				continue;
			}
			kch = input[index];
			rch = Character.toString(kch); // in case of non-character
			// 1. find Roman representation
			if ((ind = Arrays.binarySearch(khmerNumbers, kch)) >= 0) {
				// numbers
				rch = Character.toString(romanNumbers[ind]);
			} else if (kch == khmerPeriod) {
				// single bar
				rch = "" + romanBar;
			} else if (kch == khmerDoublePeriod) {
				// double bar
				rch = "" + romanDoubleBar;
			} else if (kch == '\u17A5' || kch == '\u17A6' || kch == '\u17A7' ||
					  kch == '\u17A9' || kch == '\u17AF' || kch == '\u17B1') {
				// independent vowel i, ī, u, ū, e, o
				rch = Character.toString(indVowelMap.get(kch));
			} else if (kch == '\u17A2'){
				// independent vowel a 
				if (index < input.length-1) {
					if (input[index+1] == '\u17B6') {
						// if it is ā
						rch = Character.toString(indVowelMap.get(input[index+1]));
						skipFlag = true;
					} else {
						rch = "a";
					}
				} else {
					rch = "a";
				}
			} else if (kch == '\u17B6' || kch == '\u17B7' || kch == '\u17B8' ||
					  kch == '\u17BB' || kch == '\u17BC' || kch == '\u17C1' || kch == '\u17C4') {
				// dependent vowels
				rch = Character.toString(depVowelMap.get(kch));
			} else {
				// consonants
				rch = consonantMap.get(kch);
				if (rch == null)
					rch = Character.toString(kch);
			}
			// 2. consider how to put it
			output.append(rch);
			if (index < input.length-1) {
				if (input[index+1] == khmerCoeng || input[index+1] == khmerKiller) {
					// skip Coeng
					skipFlag = true;
				} else if (consonantMap.get(kch) != null && kch != '\u17C6' && input[index+1] != '\u17B6' &&
							input[index+1] != '\u17B7' && input[index+1] != '\u17B8' &&
							input[index+1] != '\u17BB' && input[index+1] != '\u17BC' &&
							input[index+1] != '\u17C1' && input[index+1] != '\u17C4') {
					// double Khmer consonants, 'a' is added (not niggahita, not followed by vowels)
					output.append('a');
				}
			} else {
				// if the last char is a consonant, not a niggahita, add 'a'
				if (consonantMap.get(kch) != null && kch != '\u17C6')
					output.append('a');
			}
		} // end for
		return output.toString();
	}
	
	/**
	 * Converts text in Roman script to Myanmar.
	 * This is for internal display only.
	 */
	public static String romanToMyanmar(final String str) {
		final StringBuilder output = new StringBuilder();
		char[] input = str.toLowerCase().toCharArray();
		char rch;
		char mch;
		int vindex = -1; // for vowels
		boolean skipFlag = false;
		for (int index = 0; index < input.length; index++) {
			if (skipFlag) {
				skipFlag = false;
				continue;
			}
			rch = input[index];
			mch = rch; // in case of non-character
			// 1. find Myanmar representation of the character first
			if (Character.isDigit(rch)) {
				// is number
				if (alsoNumber)
					mch = myanmarNumbers[Character.digit(rch, 10)];
			} else if (rch == '.') {
				// period is retained as dot
				mch = '.';
			} else if (rch == romanBar) {
				// single bar is changed to single danda
				mch = myanmarPeriod;
			} else if (rch == romanDoubleBar) {
				// double bar is changed to double danda
				mch = myanmarDoublePeriod;
			} else if (rch == 'x') {
				// reserved character
				mch = rch;
			} else if ((vindex = romanVowels.indexOf(rch)) >= 0) {
				// is vowels
				mch = myanmarVowelsInd[vindex];
			} else {
				// is consonants
				for (int i=0; i<romanConsonantsChr.length; i++) {
					if (rch == romanConsonantsChr[i]) {
						if (index < input.length-1) {
							// if the character has 'h'
							if (romanWithHChars.indexOf(rch) >= 0 && input[index+1] == 'h')
								skipFlag = true;
						}
						mch = skipFlag ? myanmarConsonants[i+1] : myanmarConsonants[i];
						break;
					}
				} // end for loop of finding pali consonant
			}
			// 2. consider how to put it
			if (vindex >= 0) {
				// vowels
				if (output.length() == 0) {
					// to prevent index out of bound
					// independent vowels
					if (rch == 'ā')
						output.append(myanmarVowelsInd[0]);
					output.append(mch);
				} else {
					// look at the preceeding character; if it is not a consonant, independent vowels are used
					if (romanConsonants.indexOf(input[index-1]) < 0) {
						if (rch == 'ā')
							output.append(myanmarVowelsInd[0]);
						output.append(mch);
					} else {
						// dependent vowels are used
						if (rch != 'a') {
							if (rch == 'e' || rch == 'o') {
								// transposition is needed
								int insInd = 0;
								if (index >= 1) {
									final char lastCh = input[index-1];
									if (lastCh == 'h') {
										if (index >= 2) {
											insInd = romanWithHChars.indexOf(input[index-2]) >= 0
													? index >= 3 && romanConsonants.indexOf(input[index-3]) >= 0
														? 3 // double consonants plus virama
														: 1 // single consonant
													: romanConsonants.indexOf(input[index-2]) >= 0
														? 3 // double consonants plus virama
														: 1; // single consonant
										} else {
											insInd = index >= 2 && romanConsonants.indexOf(input[index-2]) >= 0
													? 3 // double consonants plus virama
													: 1; // single consonant
										}
									} else {
										if (index >= 3) {
											insInd = romanConsonants.indexOf(input[index-2]) >= 0
													? input[index-2] == 'h'
														? index >= 4 && romanConsonants.indexOf(input[index-4]) >= 0
															? 5 // triple consonants plus virama
															: 3 // double consonants plus virama
														: romanConsonants.indexOf(input[index-3]) >= 0
															? 5 // triple consonants plus virama
															: 3 // double consonants plus virama
													: 1; // single consonant
										} else {
											insInd = index >= 2 && romanConsonants.indexOf(input[index-2]) >= 0
													? 3 // double consonants plus virama
													: 1; // single consonant
										}
									}
								}
								output.insert(output.length() - insInd, myanmarVowelsDep[vindex]);
								if (rch == 'o')
									output.append(myanmarVowelsDep[1]);
							} else {
								output.append(myanmarVowelsDep[vindex]);
							}
						}
					}
				}
			} else {
				if (romanConsonants.indexOf(rch) >= 0) {
					// consonants
					output.append(mch);
					if (index < input.length-1) {
						if (!skipFlag) {
							if (romanConsonants.indexOf(input[index+1]) >= 0) {
								// double consonant needs virama
								if (rch == 'ṅ')
									output.append(myanmarAsat).append(myanmarVirama);
								else
									output.append(myanmarVirama);
							} else if (romanVowels.indexOf(input[index+1]) == -1) {
								// if not followed by a vowel, add asat
								output.append(myanmarAsat);
							}
						} else {
							// characters with h
							if (index < input.length-2) {
								if (romanConsonants.indexOf(input[index+2]) >= 0) {
									// double consonant needs virama
									output.append(myanmarVirama);
								} else if (romanVowels.indexOf(input[index+2]) == -1) {
									// if not followed by a vowel, add asat
									output.append(myanmarAsat);
								}
							} else {
								// the last consonant, add asat
								output.append(myanmarAsat);
							}
						}
					} else {
						// the last consonant, add asat
						output.append(myanmarAsat);
					}
				} else {
					// others
					output.append(mch);
				}
			}
			vindex = -1;
		} // end for loop of each input character
		return toMyanmarProcess(output.toString());
	}

	private static String processMyanmarA(final String input) {
		String result = input;
		// change to tall ā
		for (final String chs : myanmarTallASet) {
			result = result.replace(chs, chs.substring(0, chs.length() - 1) + myanmarTallA);
		}
		// fix some back to short
		for (final String chs : myanmarShortASet) {
			result = result.replace(chs, chs.substring(0, chs.length() - 1) + myanmarShortA);
		}
		return result;
	}

	private static String toMyanmarProcess(final String input) {
		String result = processMyanmarA(input);
		final String doubleNya = "" + myanmarNya + myanmarVirama + myanmarNya;
		result = result.replace(doubleNya, "" + myanmarNnya);
		final String doubleSa = "" + myanmarSa + myanmarVirama + myanmarSa;
		result = result.replace(doubleSa, "" + myanmarGreatSa);
		for (final char ch : myanmarToMedialMap.keySet()) {
			final String medCh = "" + myanmarVirama + ch;
			result = result.replace(medCh, "" + myanmarToMedialMap.get(ch));
		}
		return result;
	}
	
	/**
	 * Converts text in Roman script to Myanmar in a straight way.
	 * This is for external script transformation, like in BatchScriptTransformer.
	 * The conversion is not identical to that by CST4, especially the use of
	 * short and tall ā, but close enough.
	 */
	public static String romanToMyanmarStraight(final String str) {
		final StringBuilder output = new StringBuilder();
		char[] input = str.toLowerCase().toCharArray();
		char rch;
		char mch;
		int vindex = -1; // for vowels
		boolean skipFlag = false;
		for(int index = 0; index<input.length; index++) {
			if(skipFlag) {
				skipFlag = false;
				continue;
			}
			rch = input[index];
			mch = rch; // in case of non-character
			// 1. find Myanmar representation of the character first
			if(Character.isDigit(rch)) {
				// is number
				if(alsoNumber)
					mch = myanmarNumbers[Character.digit(rch, 10)];
			} else if(rch == '.') {
				// period is retained as dot
				mch = '.';
			} else if (rch == romanBar) {
				// single bar is changed to single danda
				mch = myanmarPeriod;
			} else if (rch == romanDoubleBar) {
				// double bar is changed to double danda
				mch = myanmarDoublePeriod;
			} else if(rch == 'x') {
				// reserved character
				mch = rch;
			} else if((vindex = romanVowels.indexOf(rch)) >= 0) {
				// is vowels
				mch = myanmarVowelsInd[vindex];
			} else {
				// is consonants
				for(int i=0; i<romanConsonantsChr.length; i++) {
					if(rch == romanConsonantsChr[i]) {
						if(index < input.length-2) {
							// if the character has 'h'
							if(romanWithHChars.indexOf(rch) >= 0 && input[index+1] == 'h')
								skipFlag = true;
						}
						mch = skipFlag? myanmarConsonants[i+1]: myanmarConsonants[i];
						break;
					}
				} // end for loop of finding pali consonant
			}
			// 2. consider how to put it
			if(vindex >= 0) {
				// vowels
				if(output.length() == 0) {
					// to prevent index out of bound
					// independent vowels
					if(rch == 'ā')
						output.append(myanmarVowelsInd[0]);
					output.append(mch);
				} else {
					// look at the preceeding character; if it is not a consonant, independent vowels are used
					if(romanConsonants.indexOf(input[index-1]) < 0) {
						if(rch == 'ā')
							output.append(myanmarVowelsInd[0]);
						output.append(mch);
					} else {
						// dependent vowels are used
						if(rch != 'a') {
							output.append(myanmarVowelsDepStraight[vindex]);
							if(rch == 'o')
								output.append(myanmarVowelsDepStraight[1]);
						}
					}
				}
			} else {
				if(romanConsonants.indexOf(rch) >= 0) {
					// consonants
					output.append(mch);
					if(index < input.length-1) {
						if (!skipFlag) {
							if (romanConsonants.indexOf(input[index+1]) >= 0) {
								// double consonant needs virama or asat (in case of ṅ)
								if (rch == 'ṅ')
									output.append(myanmarAsat).append(myanmarVirama);
								else
									output.append(myanmarVirama);
							} else if (romanVowels.indexOf(input[index+1]) == -1) {
								// if not followed by a vowel, add asat
								output.append(myanmarAsat);
							}
						} else {
							// characters with h
							if (index < input.length-2) {
								if (romanConsonants.indexOf(input[index+2]) >= 0) {
									// double consonant needs virama
									output.append(myanmarVirama);
								} else if (romanVowels.indexOf(input[index+2]) == -1) {
									// if not followed by a vowel, add asat
									output.append(myanmarAsat);
								}
							} else {
								// the last consonant, add asat
								output.append(myanmarAsat);
							}
						}
					} else {
						// the last consonant, add asat
						output.append(myanmarAsat);
					}
				} else {
					// others
					output.append(mch);
				}
			}
			vindex = -1;
		} // end for loop of each input character
		return toMyanmarProcess(output.toString());
	}
	
	public static String romanToSinhala(final String str) {
		final StringBuilder output = new StringBuilder();
		char[] input = str.toLowerCase().toCharArray();
		char rch;
		char sch;
		int vindex = -1; // for vowels
		boolean skipFlag = false;
		for (int index = 0; index<input.length; index++) {
			if (skipFlag) {
				skipFlag = false;
				continue;
			}
			rch = input[index];
			sch = rch; // in case of non-character
			// 1. find Sinhala representation of the character first
			if (rch == 'x') {
				// reserved character
				sch = rch;
			} else if ((vindex = romanVowels.indexOf(rch)) >= 0) {
				// is vowels
				sch = sinhalaVowelsInd[vindex];
			} else {
				// is consonants
				for (int i=0; i<romanConsonantsChr.length; i++) {
					if (rch == romanConsonantsChr[i]) {
						if (index < input.length-1) {
							// if the character has 'h'
							if (romanWithHChars.indexOf(rch) >= 0 && input[index+1] == 'h')
								skipFlag = true;
						}
						sch = skipFlag ? sinhalaConsonants[i+1] : sinhalaConsonants[i];
						break;
					}
				} // end for loop of finding pali consonant
			}
			// 2. consider how to put it
			if (vindex >= 0) {
				// vowels
				if (output.length() == 0) {
					// to prevent index out of bound
					// independent vowels
					output.append(sch);
				} else {
					// look at the preceeding character; if it is not a consonant, independent vowels are used
					if (romanConsonants.indexOf(input[index-1]) < 0) {
						output.append(sch);
					} else {
						// dependent vowels are used
						if (rch != 'a') {
							output.append(sinhalaVowelsDep[vindex]);
						}
					}
				}
			} else {
				if (romanConsonants.indexOf(rch) >= 0) {
					// consonants
					output.append(sch);
					if (index < input.length-1) {
						if (!skipFlag) {
							if (romanConsonants.indexOf(input[index+1]) >= 0) {
								// double consonant needs virama
								output.append(sinhalaVirama);
							} else if (romanVowels.indexOf(input[index+1]) == -1) {
								// if not followed by a vowel, add virama
								output.append(sinhalaVirama);
							}
						} else {
							// characters with h
							if (index < input.length-2) {
								if (romanConsonants.indexOf(input[index+2]) >= 0) {
									// double consonant needs virama
									output.append(sinhalaVirama);
								} else if (romanVowels.indexOf(input[index+2]) == -1) {
									// if not followed by a vowel, add virama
									output.append(sinhalaVirama);
								}
							} else {
								// the last consonant, add virama
								output.append(sinhalaVirama);
							}
						}
					} else {
						// the last consonant, add virama
						output.append(sinhalaVirama);
					}
				} else {
					// others
					output.append(sch);
				}
			}
			vindex = -1;
		} // end for loop of each input character
		return output.toString();
	}

	public static String sinhalaToRoman(final String str) {
		final StringBuilder output = new StringBuilder();
		char[] input = str.toLowerCase().toCharArray();
		// generate hash maps to ease the replacements
		final Map<Character, Character> indVowelMap = new HashMap<>();
		final Map<Character, Character> depVowelMap = new HashMap<>();
		for (int i=0; i<sinhalaVowelsInd.length; i++)
			indVowelMap.put(sinhalaVowelsInd[i], romanVowels.charAt(i));
		for (int i=0; i<sinhalaVowelsDep.length; i++)
			depVowelMap.put(sinhalaVowelsDep[i], romanVowels.charAt(i));
		final Map<Character, String> consonantMap = new HashMap<>();
		for (int i=0; i<sinhalaConsonants.length; i++)
			consonantMap.put(sinhalaConsonants[i], romanConsonantsStr[i]);
		String rch;
		char sch;
		boolean skipFlag = false;
		for (int index = 0; index<input.length; index++) {
			if (skipFlag) {
				skipFlag = false;
				continue;
			}
			sch = input[index];
			rch = Character.toString(sch); // in case of non-character
			// 1. find Roman representation
			if (sch == '\u0D85' || sch == '\u0D86' ||
					sch == '\u0D89' || sch == '\u0D8A' ||
					sch == '\u0D8B' || sch == '\u0D8C' ||
					sch == '\u0D91' || sch == '\u0D94') {
				// independent vowel a, ā, i, ī, u, ū, e, o
				rch = Character.toString(indVowelMap.get(sch));
			} else if (sch == '\u0DCF' || sch == '\u0DD2' || sch == '\u0DD3' ||
					  sch == '\u0DD4' || sch == '\u0DD6' || sch == '\u0DD9' ||
					  sch == '\u0DDC') {
				// dependent vowels
				rch = Character.toString(depVowelMap.get(sch));
			} else {
				// consonants
				rch = consonantMap.get(sch);
				if (rch == null)
					rch = Character.toString(sch);
			}
			// 2. consider how to put it
			output.append(rch);
			if (index < input.length-1) {
				if (input[index+1] == sinhalaVirama) {
					// skip Virama
					skipFlag = true;
				} else if (consonantMap.get(sch) != null && sch != '\u0D82' && input[index+1] != '\u0DCF' &&
							input[index+1] != '\u0DD2' && input[index+1] != '\u0DD3' &&
							input[index+1] != '\u0DD4' && input[index+1] != '\u0DD6' &&
							input[index+1] != '\u0DD9' && input[index+1] != '\u0DDC') {
					// double Sinhala consonants, 'a' is added (not anusvara, not followed by vowels)
					output.append('a');
				}
			} else {
				// if the last char is a consonant, not a niggahita, add 'a'
				if (consonantMap.get(sch) != null && sch != '\u0D82')
					output.append('a');
			}
		} // end for
		return output.toString();
	}
	
	public static String romanToDevanagari(final String str) {
		final StringBuilder output = new StringBuilder();
		char[] input = str.toLowerCase().toCharArray();
		char rch;
		char dch;
		int vindex = -1; // for vowels
		boolean skipFlag = false;
		for (int index = 0; index<input.length; index++) {
			if (skipFlag) {
				skipFlag = false;
				continue;
			}
			rch = input[index];
			dch = rch; // in case of non-character
			// 1. find Devanagari representation of the character first
			if (Character.isDigit(rch)) {
				// is number
				if (alsoNumber)
					dch = devaNumbers[Character.digit(rch, 10)];
			} else if (rch == '.') {
				// period is retained as dot
				dch = '.';
			} else if (rch == romanBar) {
				// single bar is changed to single danda
				dch = devaPeriod;
			} else if (rch == romanDoubleBar) {
				// double bar is changed to double danda
				dch = devaDoublePeriod;
			} else if (rch == romanAbbrSign) {
				// one dot leader is changed to abbreviation sign
				dch = devaAbbrev;
			} else if (rch == 'x') {
				// reserved character
				dch = rch;
			} else if ((vindex = romanVowelsForDeva.indexOf(rch)) >= 0) {
				// is vowels
				dch = devaVowelsInd[vindex];
			} else {
				// is consonants
				for (int i=0; i<romanConsonantsChr.length; i++) {
					if (rch == romanConsonantsChr[i]) {
						if (index < input.length-1) {
							// if the character has 'h'
							if (romanWithHChars.indexOf(rch) >= 0 && input[index+1] == 'h')
								skipFlag = true;
						}
						dch = skipFlag ? devaConsonants[i+1] : devaConsonants[i];
						break;
					}
				} // end for loop of finding pali consonant
			}
			// 2. consider how to put it
			if (vindex >= 0) {
				// vowels
				if (output.length() == 0) {
					// to prevent index out of bound
					// independent vowels
					output.append(dch);
				} else {
					// look at the preceeding character; if it is not a consonant, independent vowels are used
					if (romanConsonants.indexOf(input[index-1]) < 0) {
						output.append(dch);
					} else {
						// dependent vowels are used
						if (rch != 'a') {
							output.append(devaVowelsDep[vindex]);
						}
					}
				}
			} else {
				if (romanConsonants.indexOf(rch) >= 0) {
					// consonants
					output.append(dch);
					if (index < input.length-1) {
						if (!skipFlag) {
							if (romanConsonants.indexOf(input[index+1]) >= 0) {
								// double consonant needs virama
								output.append(devaVirama);
							} else if (romanVowelsForDeva.indexOf(input[index+1]) == -1) {
								// if not followed by a vowel, add virama
								output.append(devaVirama);
							}
						} else {
							// characters with h
							if (index < input.length-2) {
								if (romanConsonants.indexOf(input[index+2]) >= 0) {
									// double consonant needs virama
									output.append(devaVirama);
								} else if (romanVowels.indexOf(input[index+2]) == -1) {
									// if not followed by a vowel, add virama
									output.append(devaVirama);
								}
							} else {
								// the last consonant, add virama
								output.append(devaVirama);
							}
						}
					} else {
						// the last consonant, add virama
						output.append(devaVirama);
					}
				} else {
					// others
					output.append(dch);
				}
			}
			vindex = -1;
		} // end for loop of each input character
		return output.toString();
	}

	public static String devanagariToRoman(final String str) {
		final StringBuilder output = new StringBuilder();
		char[] input = str.toLowerCase().toCharArray();
		// generate hash maps to ease the replacements
		final Map<Character, Character> indVowelMap = new HashMap<>();
		final Map<Character, Character> depVowelMap = new HashMap<>();
		for (int i=0; i<devaVowelsInd.length; i++)
			indVowelMap.put(devaVowelsInd[i], romanVowelsForDeva.charAt(i));
		for (int i=0; i<devaVowelsDep.length; i++)
			depVowelMap.put(devaVowelsDep[i], romanVowelsForDeva.charAt(i));
		final Map<Character, String> consonantMap = new HashMap<>();
		for (int i=0; i<devaConsonants.length; i++)
			consonantMap.put(devaConsonants[i], romanConsonantsStr[i]);
		String rch;
		char dch;
		int ind; // general purpose index
		boolean skipFlag = false;
		for (int index = 0; index<input.length; index++) {
			if (skipFlag) {
				skipFlag = false;
				continue;
			}
			dch = input[index];
			rch = Character.toString(dch); // in case of non-character
			// 1. find Roman representation
			if ((ind = Arrays.binarySearch(devaNumbers, dch)) >= 0) {
				// numbers
				rch = Character.toString(romanNumbers[ind]);
			} else if (dch == devaPeriod) {
				// single bar
				rch = "" + romanBar;
			} else if (dch == devaDoublePeriod) {
				// double bar
				rch = "" + romanDoubleBar;
			} else if (dch == devaAbbrev) {
				// abbreviation sign
				rch = "" + romanAbbrSign;
			} else if (dch == '\u0905' || dch == '\u0906' ||
					dch == '\u0907' || dch == '\u0908' ||
					dch == '\u0909' || dch == '\u090A' ||
					dch == '\u090F' || dch == '\u0910' ||
					dch == '\u0913' || dch == '\u0914') {
				// independent vowel a, ā, i, ī, u, ū, e, ē, o, ō
				rch = Character.toString(indVowelMap.get(dch));
			} else if (dch == '\u093E' || dch == '\u093F' || dch == '\u0940' ||
					  dch == '\u0941' || dch == '\u0942' || dch == '\u0947' ||
					  dch == '\u0948' || dch == '\u094B' || dch == '\u094C') {
				// dependent vowels
				rch = Character.toString(depVowelMap.get(dch));
			} else {
				// consonants
				rch = consonantMap.get(dch);
				if (rch == null)
					rch = Character.toString(dch);
			}
			// 2. consider how to put it
			output.append(rch);
			if (index < input.length-1) {
				if (input[index+1] == devaVirama) {
					// skip Virama
					skipFlag = true;
				} else if (consonantMap.get(dch) != null && dch != '\u0902' && input[index+1] != '\u093E' &&
							input[index+1] != '\u093F' && input[index+1] != '\u0940' &&
							input[index+1] != '\u0941' && input[index+1] != '\u0942' &&
							input[index+1] != '\u0947' && input[index+1] != '\u0948' &&
							input[index+1] != '\u094B' && input[index+1] != '\u094C') {
					// double Devanagari consonants, 'a' is added (not anusvara, not followed by vowels)
					output.append('a');
				}
			} else {
				// if the last char is a consonant, not a niggahita, add 'a'
				if (consonantMap.get(dch) != null && dch != '\u0902')
					output.append('a');
			}
		} // end for
		return output.toString();
	}

}
