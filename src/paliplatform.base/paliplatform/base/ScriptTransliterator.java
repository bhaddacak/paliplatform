/*
 * ScriptTransliterator.java
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

import paliplatform.base.Utilities.PaliScript;

import java.util.*;
import java.util.function.Function;
import java.util.regex.*;

/** 
 * This revision of script transformer is intended to cover
 * Sanskrit characters in most typical usages.
 * The class is a factory, providing static methods.
 * @author J.R. Bhaddacak
 * @version 3.4
 * @since 1.0
 */
public class ScriptTransliterator {
	public static enum EngineType {
		DEVA_ROMAN_ISO("D-R: ISO 15919", PaliScript.DEVANAGARI, PaliScript.ROMAN),
		DEVA_ROMAN_IAST("D-R: IAST", PaliScript.DEVANAGARI, PaliScript.ROMAN),
		DEVA_ROMAN_COMMON("D-R: Pali Common", PaliScript.DEVANAGARI, PaliScript.ROMAN),
		DEVA_ROMAN_LEAST("D-R: Least Contamination", PaliScript.DEVANAGARI, PaliScript.ROMAN),
		DEVA_ROMAN_UNIQUE("D-R: Roman Unique", PaliScript.DEVANAGARI, PaliScript.ROMAN),
		DEVA_THAI("D-T: Thai Common", PaliScript.DEVANAGARI, PaliScript.THAI),
		DEVA_KHMER("D-K: Khmer Common", PaliScript.DEVANAGARI, PaliScript.KHMER),
		DEVA_SINHALA("D-S: Sinhala Common", PaliScript.DEVANAGARI, PaliScript.SINHALA),
		DEVA_MYANMAR("D-M: Myanmar Common", PaliScript.DEVANAGARI, PaliScript.MYANMAR),
		ROMAN_SKT_DEVA("R-D: Devanagari Common", PaliScript.ROMAN, PaliScript.DEVANAGARI),
		ROMAN_DEVA("R-D: Devanagari Common", PaliScript.ROMAN, PaliScript.DEVANAGARI),
		THAI_DEVA("T-D: Devanagari Common", PaliScript.THAI, PaliScript.DEVANAGARI),
		KHMER_DEVA("K-D: Devanagari Common", PaliScript.KHMER, PaliScript.DEVANAGARI),
		SINHALA_DEVA("S-D: Devanagari Common", PaliScript.SINHALA, PaliScript.DEVANAGARI),
		MYANMAR_DEVA("M-D: Devanagari Common", PaliScript.MYANMAR, PaliScript.DEVANAGARI);
		public static final EngineType[] engines = EngineType.values();
		public static final String[] engineCodes = new String[] {
			"di", "da", "dr", "dl", "du", "dt", "dk", "ds", "dm",
			"cd", "rd", "td", "kd", "sd", "md" };
		private final String name;
		private final PaliScript sourceScript;
		private final PaliScript targetScript;
		private static final Map<String, EngineType> emap = new HashMap<>();
		static {
			for (int i = 0; i < engines.length; i++)
				emap.put(engineCodes[i], engines[i]);
		}
		private EngineType(final String nam, final PaliScript source, final PaliScript target) {
			name = nam;
			sourceScript = source;
			targetScript = target;
		}
		public String getName() {
			return name;
		}
		public String getNameShort() {
			return name.substring(name.indexOf(":") + 2);
		}
		public PaliScript getSourceScript() {
			return sourceScript;
		}
		public PaliScript getTargetScript() {
			return targetScript;
		}
		public static EngineType fromCode(final String code) {
			return emap.get(code);
		}
	}
	private static final String NEWLINE = "\\n";
	private static final char saveBlockStart = '\uE800';
	private static final char[] saveChars = new char[] { '\uE700', '\uE701', '\uE702', '\uE703' }; // for temporary substitutions
	private static final Pattern tagPattern = Pattern.compile("(<.*?>)");
	private static final Pattern tagShiftedPattern = Pattern.compile(
			"(" + (char)(saveBlockStart + '<') + ".*?" + (char)(saveBlockStart + '>') + ")");
	private static final Map<EngineType, Function<String, String>> translitMap = new EnumMap<>(EngineType.class);
	private static boolean alsoNumber = true;
	// Devanagari set
	// Vowels: a ā i ī u ū, ṛ ṝ ḷ ḹ, e ai o au
	private static final int[] nonPaliVowelIndice = { 6, 7, 8, 9, 11, 13 };
	private static final char[] devaVowelsInd = { 
		'\u0905', '\u0906', '\u0907', '\u0908', '\u0909', '\u090A',
		'\u090B', '\u0960', '\u090C', '\u0961',
		'\u090F', '\u0910', '\u0913', '\u0914' };
	private static final char[] devaVowelsDep = {
		'\u0905', '\u093E', '\u093F', '\u0940', '\u0941', '\u0942',
		'\u0943', '\u0944', '\u0962', '\u0963',
		'\u0947', '\u0948', '\u094B', '\u094C' };
	// prepare for binary search
	private static final char[] devaVISorted = Arrays.copyOf(devaVowelsInd, devaVowelsInd.length); // sorted in init
	private static final char[] devaVDSorted = Arrays.copyOfRange(devaVowelsDep, 1, devaVowelsDep.length); // sorted in init
	private static final char[] devaNumbers = {
		'\u0966', '\u0967', '\u0968', '\u0969', '\u096A', '\u096B', '\u096C', '\u096D', '\u096E', '\u096F' };
	private static final char devaAnusvara = '\u0902';
	private static final char devaVisarga = '\u0903';
	private static final char devaNukta = '\u093C';
	private static final char devaAvagraha = '\u093D';
	private static final char devaVirama = '\u094D';
	private static final char devaDanda = '\u0964';
	private static final char devaDoubleDanda = '\u0965';
	private static final char devaAbbrSign = '\u0970';
	// Consonants: ka kha ga gha ṅa, ca cha ja jha ña, ṭa ṭha ḍa ḍha ṇa, ta tha da dha na, pa pha ba bha ma
	//             ya ra la ḷa va śa ṣa sa ha
	public static final char[] devaConsonants = {
		'\u0915', '\u0916', '\u0917', '\u0918', '\u0919',
		'\u091A', '\u091B', '\u091C', '\u091D', '\u091E',
		'\u091F', '\u0920', '\u0921', '\u0922', '\u0923',
		'\u0924', '\u0925', '\u0926', '\u0927', '\u0928',
		'\u092A', '\u092B', '\u092C', '\u092D', '\u092E',
		'\u092F', '\u0930', '\u0932', '\u0933', '\u0935', '\u0936', '\u0937', '\u0938', '\u0939' };
	// Roman set
	private static final String romanVowelsUnique = "aāiīuūṛṝḷḹeēoō"; // ē = ai, ō = au (converted afterward)
	private static final String romanConsonantsUnique = "kgṅcjñṭḍṇtdnpbmyrlḻvśṣsh"; // note ḻ not ḷ
	private static final String romanWithHChars = "bcdgjkptḍṭ";
	private static final char[] romanNumbers = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' };
	private static final char romanAnusvara = 'ṃ';
	private static final char romanVisarga = 'ḥ';
	private static final char romanAvagraha = '\u0315';
	private static final char romanDanda = '|';
	private static final char romanDoubleDanda = '\u2016';
	private static final char romanAbbrSign = '\u00B7';
	private static final char[] romanConsonantsChrUnique = { 	
		'k', 'x', 'g', 'x', 'ṅ',
		'c', 'x', 'j', 'x', 'ñ',
		'ṭ', 'x', 'ḍ', 'x', 'ṇ',
		't', 'x', 'd', 'x', 'n',
		'p', 'x', 'b', 'x', 'm',
		'y', 'r', 'l', 'ḻ', 'v', 'ś', 'ṣ', 's', 'h' }; // note ḻ not ḷ
	private static final String[] romanConsonantsStrUnique = { 	
		"k", "kh", "g", "gh", "ṅ",
		"c", "ch", "j", "jh", "ñ",
		"ṭ", "ṭh", "ḍ", "ḍh", "ṇ",
		"t", "th", "d", "dh", "n",
		"p", "ph", "b", "bh", "m",
		"y", "r", "l", "ḻ", "v", "ś", "ṣ", "s", "h" }; // note ḻ not ḷ (converted afterward)
	// Thai set
	private static final String[] thaiVowelsInd = { 
		"\u0E2D", "\u0E2D\u0E32", "\u0E2D\u0E34", "\u0E2D\u0E35", "\u0E2D\u0E38", "\u0E2D\u0E39",
		"\u0E24", "\u0E24\u0E45", "\u0E26", "\u0E26\u0E45", 
		"\u0E2D\u0E40", "\u0E2D\u0E44", "\u0E2D\u0E42", "\u0E2D\u0E40\u0E32" };
	private static final String[] thaiVowelsDep = { 
		"\u0E2D", "\u0E32", "\u0E34", "\u0E35", "\u0E38", "\u0E39",
		"\u0E24", "\u0E24\u0E45", "\u0E26", "\u0E26\u0E45", 
		"\u0E40", "\u0E44", "\u0E42", "\u0E40\u0E32" };
	private static final char[] thaiVowelsDepSub = { 
		'\u0E2D', '\u0E32', '\u0E34', '\u0E35', '\u0E38', '\u0E39',
		'\u0E24', saveChars[0], '\u0E26', saveChars[1],
		'\u0E40', '\u0E44', '\u0E42', saveChars[2] };
	private static final char[] thaiNumbers = {
		'\u0E50', '\u0E51', '\u0E52', '\u0E53', '\u0E54', '\u0E55', '\u0E56', '\u0E57', '\u0E58', '\u0E59' };
	private static final char thaiAnusvara = '\u0E4D';
	private static final char thaiVisarga = '\u0E30';
	private static final char thaiAvagraha = '’';
	private static final char thaiVirama = '\u0E3A';
	private static final char thaiDanda = '\u0E2F';
	private static final char thaiDoubleDanda = '\u0E5A';
	private static final char thaiAbbrSign = '.';
	public static final char[] thaiConsonants = {
		'\u0E01', '\u0E02', '\u0E04', '\u0E06', '\u0E07',
		'\u0E08', '\u0E09', '\u0E0A', '\u0E0C', '\u0E0D',
		'\u0E0F', '\u0E10', '\u0E11', '\u0E12', '\u0E13',
		'\u0E15', '\u0E16', '\u0E17', '\u0E18', '\u0E19',
		'\u0E1B', '\u0E1C', '\u0E1E', '\u0E20', '\u0E21',
		'\u0E22', '\u0E23', '\u0E25', '\u0E2C', '\u0E27', '\u0E28', '\u0E29', '\u0E2A', '\u0E2B' };
	private static final String thaiAllConsonants = new String(thaiConsonants);
	// Khmer set
	private static final String[] khmerVowelsInd = {
		"\u17A2", "\u17a2\u17b6", "\u17A5", "\u17A6", "\u17A7", "\u17A9",
		"\u17AB", "\u17AC", "\u17AD", "\u17AE", 
		"\u17AF", "\u17B0", "\u17B1", "\u17B3" };
	private static final char[] khmerVowelsDep = {
		'\u17A2', '\u17B6', '\u17B7', '\u17B8', '\u17BB', '\u17BC',
		'\u17AB', '\u17AC', '\u17AD', '\u17AE', 
		'\u17C1', '\u17C3', '\u17C4', '\u17C5' };
	private static final char[] khmerVowelsDepSub = {
		'\u17A2', '\u17B6', '\u17B7', '\u17B8', '\u17BB', '\u17BC',
		saveChars[0], saveChars[1], saveChars[2], saveChars[3], 
		'\u17C1', '\u17C3', '\u17C4', '\u17C5' };
	private static final char[] khmerNumbers = {
		'\u17E0', '\u17E1', '\u17E2', '\u17E3', '\u17E4', '\u17E5', '\u17E6', '\u17E7', '\u17E8', '\u17E9' };
	private static final char khmerAnusvara = '\u17C6';
	private static final char khmerVisarga = '\u17C7';
	private static final char khmerAvagraha = '’';
	private static final char khmerKiller = '\u17D1';
	private static final char khmerCoeng = '\u17D2';
	private static final char khmerDanda = '\u17D4';
	private static final char khmerDoubleDanda = '\u17D5';
	private static final char khmerAbbrSign = '.';
	public static final char[] khmerConsonants = {
		'\u1780', '\u1781', '\u1782', '\u1783', '\u1784',
		'\u1785', '\u1786', '\u1787', '\u1788', '\u1789',
		'\u178A', '\u178B', '\u178C', '\u178D', '\u178E',
		'\u178F', '\u1790', '\u1791', '\u1792', '\u1793',
		'\u1794', '\u1795', '\u1796', '\u1797', '\u1798',
		'\u1799', '\u179A', '\u179B', '\u17A1', '\u179C', '\u179D', '\u179E', '\u179F', '\u17A0' };
	private static final String khmerAllConsonants = new String(khmerConsonants);
	private static final String khmerRuLu = "\u17AB\u17AC\u17AD\u17AE";
	// Sinhala set
	private static final char[] sinhalaVowelsInd = {
		'\u0D85', '\u0D86', '\u0D89', '\u0D8A', '\u0D8B', '\u0D8C',
		'\u0D8D', '\u0D8E', '\u0D8F', '\u0D90',
		'\u0D91', '\u0D93', '\u0D94', '\u0D96' };
	private static final char[] sinhalaVowelsDep = {
		'\u0D85', '\u0DCF', '\u0DD2', '\u0DD3', '\u0DD4', '\u0DD6',
		'\u0DD8', '\u0DF2', '\u0DDF', '\u0DF3',
		'\u0DD9', '\u0DDB', '\u0DDC', '\u0DDE' };
	// These digits are used mainly for astrological purpose.
	//private static final char[] sinhalaNumbers = {
	//	'\u0DE6', '\u0DE7', '\u0DE8', '\u0DE9', '\u0DEA', '\u0DEB', '\u0DEC', '\u0DED', '\u0DEE', '\u0DEF' };
	private static final char sinhalaAnusvara = '\u0D82';
	private static final char sinhalaVisarga = '\u0D83';
	private static final char sinhalaAvagraha = '’';
	private static final char sinhalaVirama = '\u0DCA';
	private static final char sinhalaDanda = '.';
	private static final char sinhalaDoubleDanda = '.';
	private static final char sinhalaAbbrSign = '.';
	public static final char[] sinhalaConsonants = {
		'\u0D9A', '\u0D9B', '\u0D9C', '\u0D9D', '\u0D9E',
		'\u0DA0', '\u0DA1', '\u0DA2', '\u0DA3', '\u0DA4',
		'\u0DA7', '\u0DA8', '\u0DA9', '\u0DAA', '\u0DAB',
		'\u0DAD', '\u0DAE', '\u0DAF', '\u0DB0', '\u0DB1',
		'\u0DB4', '\u0DB5', '\u0DB6', '\u0DB7', '\u0DB8',
		'\u0DBA', '\u0DBB', '\u0DBD', '\u0DC5', '\u0DC0', '\u0DC1', '\u0DC2', '\u0DC3', '\u0DC4' };
	// Myanmar set
	private static final char myanmarTallAA = '\u102B';
	private static final char myanmarShortAA = '\u102C';
	private static final char myanmarDepE = '\u1031'; // this causes problem, not used in displaying
	private static final String[] myanmarVowelsInd = {
		"\u1021", "\u1021" + myanmarShortAA, "\u1023", "\u1024", "\u1025", "\u1026",
		"\u1052", "\u1053", "\u1054", "\u1055",
		"\u1027", "\u1021\u1032", "\u1029", "\u102A" };
	private static final char[] myanmarVowelsIndSub = {
		'\u1021', saveChars[0], '\u1023', '\u1024', '\u1025', '\u1026',
		'\u1052', '\u1053', '\u1054', '\u1055',
		'\u1027', saveChars[1], '\u1029', '\u102A' };
	private static final String[] myanmarVowelsDep = {
		"\u1021", "" + myanmarShortAA, "\u102D", "\u102E", "\u102F", "\u1030",
		"\u1056", "\u1057", "\u1058", "\u1059",
		"\u1031", "\u1032", "\u1031\u102C", "\u1031\u102C\u103A" };
	private static final char[] myanmarVowelsDepSub = {
		'\u1021', myanmarShortAA, '\u102D', '\u102E', '\u102F', '\u1030',
		'\u1056', '\u1057', '\u1058', '\u1059',
		'\u1031', '\u1032', saveChars[2], saveChars[3] };
	private static final char[] myanmarNumbers = {
		'\u1040', '\u1041', '\u1042', '\u1043', '\u1044', '\u1045', '\u1046', '\u1047', '\u1048', '\u1049' };
	private static final char myanmarAnusvara = '\u1036';
	private static final char myanmarVisarga = '\u1038';
	private static final char myanmarAvagraha = '’';
	private static final char myanmarVirama = '\u1039';
	private static final char myanmarAsat = '\u103A';
	private static final char myanmarDanda = '\u104A'; // in CST4 comma is changed to danda
	private static final char myanmarDoubleDanda = '\u104B';
	private static final char myanmarAbbrSign = '.';
	public static final char[] myanmarConsonants = {
		'\u1000', '\u1001', '\u1002', '\u1003', '\u1004',
		'\u1005', '\u1006', '\u1007', '\u1008', '\u1009',
		'\u100B', '\u100C', '\u100D', '\u100E', '\u100F',
		'\u1010', '\u1011', '\u1012', '\u1013', '\u1014',
		'\u1015', '\u1016', '\u1017', '\u1018', '\u1019',
		'\u101A', '\u101B', '\u101C', '\u1020', '\u101D', '\u1050', '\u1051', '\u101E', '\u101F' };
	private static final String myanmarAllConsonants = new String(myanmarConsonants);
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
	private static final List<String> myanmarTallAAList = List.of(
			"" + '\u1004' + myanmarVirama + '\u1001' + myanmarShortAA, // ṅkhā
			"" + '\u1004' + myanmarVirama + '\u1001' + myanmarDepE + myanmarShortAA, // ṅkho
			"" + '\u1004' + myanmarVirama + '\u1002' + myanmarShortAA, // ṅgā
			"" + '\u1004' + myanmarVirama + '\u1002' + myanmarDepE + myanmarShortAA, // ṅgo
			"" + '\u1004' + myanmarVirama + '\u1004' + myanmarShortAA, // ṅṅā
			"" + '\u1004' + myanmarVirama + '\u1004' + myanmarDepE + myanmarShortAA, // ṅṅo
			"" + '\u1012' + myanmarVirama + '\u1012' + myanmarShortAA, // ddā *
			"" + '\u1012' + myanmarVirama + '\u1012' + myanmarDepE + myanmarShortAA, // ddo *
			"" + '\u1012' + myanmarVirama + '\u1013' + myanmarShortAA, // ddhā
			"" + '\u1012' + myanmarVirama + '\u1013' + myanmarDepE + myanmarShortAA, // ddho
			"" + '\u1012' + myanmarVirama + '\u1019' + myanmarShortAA, // dmā
			"" + '\u1012' + myanmarVirama + '\u1019' + myanmarDepE + myanmarShortAA, // dmo
			"" + '\u1012' + myanmarVirama + '\u101D' + myanmarShortAA, // dvā
			"" + '\u1012' + myanmarVirama + '\u101D' + myanmarDepE + myanmarShortAA, // dvo
			"" + '\u1001' + myanmarShortAA, // khā
			"" + '\u1001' + myanmarDepE + myanmarShortAA, // kho
			"" + '\u1002' + myanmarShortAA, // gā
			"" + '\u1002' + myanmarDepE + myanmarShortAA, // go
			"" + '\u1004' + myanmarShortAA, // ṅā
			"" + '\u1004' + myanmarDepE + myanmarShortAA, // ṅo
			"" + '\u1012' + myanmarShortAA, // dā *
			"" + '\u1012' + myanmarDepE + myanmarShortAA, // do *
			"" + '\u1015' + myanmarShortAA, // pā
			"" + '\u1015' + myanmarDepE + myanmarShortAA, // po
			"" + '\u101D' + myanmarShortAA, // vā
			"" + '\u101D' + myanmarDepE + myanmarShortAA // vo
			);
	private static final List<String> myanmarShortAAList = List.of(
			"" + '\u1000' + myanmarVirama + '\u1001' + myanmarTallAA, // kkh *
			"" + '\u1002' + myanmarVirama + '\u1002' + myanmarTallAA, // gg *
			"" + '\u1015' + myanmarVirama + '\u1015' + myanmarTallAA, // pp *
			"" + '\u1019' + myanmarVirama + '\u1015' + myanmarTallAA, // mp *
			"" + myanmarVirama + '\u101D' + myanmarTallAA // xv
			);

	private ScriptTransliterator() {
	}

	public static void initializeTransliterator() {
		if (!translitMap.isEmpty()) return;
		Arrays.sort(devaVISorted);
		Arrays.sort(devaVDSorted);
		translitMap.put(EngineType.DEVA_ROMAN_ISO, text -> toISO(devanagariToRomanUnique(text)));
		translitMap.put(EngineType.DEVA_ROMAN_IAST, text -> toIAST(devanagariToRomanUnique(text)));
		translitMap.put(EngineType.DEVA_ROMAN_COMMON, text -> toPaliCommon(devanagariToRomanUnique(text)));
		translitMap.put(EngineType.DEVA_ROMAN_LEAST, text -> toLeast(devanagariToRomanUnique(text)));
		translitMap.put(EngineType.DEVA_ROMAN_UNIQUE, text -> devanagariToRomanUnique(text));
		translitMap.put(EngineType.DEVA_THAI, text -> processToThai(devanagariToThaiRaw(text)));
		translitMap.put(EngineType.DEVA_KHMER, text -> processToKhmer(devanagariToKhmerRaw(text)));
		translitMap.put(EngineType.DEVA_SINHALA, text -> devanagariToSinhala(text));
		translitMap.put(EngineType.DEVA_MYANMAR, text -> processToMyanmar(devanagariToMyanmarRaw(text)));
		translitMap.put(EngineType.ROMAN_SKT_DEVA, text -> romanToDevanagari(toUnique(text, false)));
		translitMap.put(EngineType.ROMAN_DEVA, text -> romanToDevanagari(toUnique(text, true)));
		translitMap.put(EngineType.THAI_DEVA, text -> thaiRawToDevanagari(processFromThai(text)));
		translitMap.put(EngineType.KHMER_DEVA, text -> khmerRawToDevanagari(processFromKhmer(text)));
		translitMap.put(EngineType.SINHALA_DEVA, text -> sinhalaToDevanagari(text));
		translitMap.put(EngineType.MYANMAR_DEVA, text -> myanmarRawToDevanagari(processFromMyanmar(text)));
	}

	public static String[] getDevaPaliVowels() {
		final String[] result = new String[8];
		int index = 0;
		for (int i = 0; i < devaVowelsInd.length; i++) {
			if (Arrays.binarySearch(nonPaliVowelIndice, i) < 0)
				result[index++] = "" + devaVowelsInd[i];
		}
		return result;
	}

	public static char[] getDevaPaliConsonants() {
		final char[] result = new char[33];
		for (int i = 0; i < devaConsonants.length; i++) {
			result[i] = devaConsonants[i];
			if (i >= 28) break;
		}
		result[28] = '\u0935';
		result[29] = '\u0938';
		result[30] = '\u0939';
		result[31] = '\u0933';
		result[32] = '\u0902';
		return result;
	}

	public static char[] getDevaNumbers() {
		return devaNumbers;
	}

	public static char[] getRomanPaliVowels() {
		final char[] result = new char[8];
		int index = 0;
		for (int i = 0; i < romanVowelsUnique.length(); i++) {
			if (Arrays.binarySearch(nonPaliVowelIndice, i) < 0)
				result[index++] = romanVowelsUnique.charAt(i);
		}
		return result;
	}

	public static String[] getRomanPaliConsonants() {
		final String[] result = new String[33];
		for (int i = 0; i < romanConsonantsStrUnique.length; i++) {
			result[i] = romanConsonantsStrUnique[i];
			if (i >= 28) break;
		}
		result[28] = "v";
		result[29] = "s";
		result[30] = "h";
		result[31] = "ḷ";
		result[32] = "ṃ";
		return result;
	}

	public static char[] getRomanNumbers() {
		return romanNumbers;
	}

	public static String[] getThaiPaliVowels() {
		final String[] result = new String[8];
		int index = 0;
		for (int i = 0; i < thaiVowelsDep.length; i++) {
			if (Arrays.binarySearch(nonPaliVowelIndice, i) < 0)
				result[index++] = i > 0 
									? index > 6
										? thaiVowelsDep[i] + thaiVowelsDep[0]
										: thaiVowelsDep[0] + thaiVowelsDep[i]
									: thaiVowelsDep[i];
		}
		return result;
	}

	public static char[] getThaiPaliConsonants() {
		final char[] result = new char[33];
		for (int i = 0; i < thaiConsonants.length; i++) {
			result[i] = thaiConsonants[i];
			if (i >= 28) break;
		}
		result[28] = '\u0E27';
		result[29] = '\u0E2A';
		result[30] = '\u0E2B';
		result[31] = '\u0E2C';
		result[32] = '\u0E4D';
		return result;
	}

	public static char[] getThaiNumbers() {
		return thaiNumbers;
	}

	public static String[] getKhmerPaliVowels() {
		final String[] result = new String[8];
		int index = 0;
		for (int i = 0; i < khmerVowelsInd.length; i++) {
			if (Arrays.binarySearch(nonPaliVowelIndice, i) < 0)
				result[index++] = khmerVowelsInd[i];
		}
		return result;
	}

	public static char[] getKhmerPaliConsonants() {
		final char[] result = new char[33];
		for (int i = 0; i < khmerConsonants.length; i++) {
			result[i] = khmerConsonants[i];
			if (i >= 28) break;
		}
		result[28] = '\u179C';
		result[29] = '\u179F';
		result[30] = '\u17A0';
		result[31] = '\u17A1';
		result[32] = '\u17C6';
		return result;
	}

	public static char[] getKhmerNumbers() {
		return khmerNumbers;
	}

	public static String[] getSinhalaPaliVowels() {
		final String[] result = new String[8];
		int index = 0;
		for (int i = 0; i < sinhalaVowelsInd.length; i++) {
			if (Arrays.binarySearch(nonPaliVowelIndice, i) < 0)
				result[index++] = "" + sinhalaVowelsInd[i];
		}
		return result;
	}

	public static char[] getSinhalaPaliConsonants() {
		final char[] result = new char[33];
		for (int i = 0; i < sinhalaConsonants.length; i++) {
			result[i] = sinhalaConsonants[i];
			if (i >= 28) break;
		}
		result[28] = '\u0DC0';
		result[29] = '\u0DC3';
		result[30] = '\u0DC4';
		result[31] = '\u0DC5';
		result[32] = '\u0D82';
		return result;
	}

	public static char[] getSinhalaNumbers() {
		return romanNumbers;
	}

	public static String[] getMyanmarPaliVowels() {
		final String[] result = new String[8];
		int index = 0;
		for (int i = 0; i < myanmarVowelsInd.length; i++) {
			if (Arrays.binarySearch(nonPaliVowelIndice, i) < 0)
				result[index++] = myanmarVowelsInd[i];
		}
		return result;
	}

	public static char[] getMyanmarPaliConsonants() {
		final char[] result = new char[33];
		for (int i = 0; i < myanmarConsonants.length; i++) {
			result[i] = myanmarConsonants[i];
			if (i >= 28) break;
		}
		result[28] = '\u101D';
		result[29] = '\u101E';
		result[30] = '\u101F';
		result[31] = '\u1020';
		result[32] = '\u1036';
		return result;
	}

	public static char[] getMyanmarNumbers() {
		return myanmarNumbers;
	}

	public static String[][] getDevaSktLetterGrid() {
		// mainly used in SktLetters
		final String[][] result = new String[7][11];
		// mutes
		for (int i = 0; i < devaConsonants.length; i++) {
			if (i < 5)
				result[0][i] = "" + devaConsonants[i];
			else if (i < 10)
				result[1][i - 5] = "" + devaConsonants[i];
			else if (i < 15)
				result[2][i - 10] = "" + devaConsonants[i];
			else if (i < 20)
				result[3][i - 15] = "" + devaConsonants[i];
			else if (i < 25)
				result[4][i - 20] = "" + devaConsonants[i];
			else
				break;
		}
		// semivowels
		result[1][5] = "\u092F"; // y;
		result[2][5] = "\u0930"; // r
		result[3][5] = "\u0932"; // l
		result[4][5] = "\u0935"; // v
		// sibilant
		result[0][6] = "\u0939"; // h
		result[1][6] = "\u0936"; // ś
		result[2][6] = "\u0937"; // ṣ
		result[3][6] = "\u0938"; // s
		// vowels
		result[0][7] = "\u0905"; // a
		result[1][7] = "\u0907"; // i
		result[2][7] = "\u090B"; // ṛ
		result[3][7] = "\u090C"; // ḷ
		result[4][7] = "\u0909"; // u
		result[0][8] = "\u0906"; // ā
		result[1][8] = "\u0908"; // ī
		result[2][8] = "\u0960"; // ṝ
		result[3][8] = "\u0961"; // ḹ
		result[4][8] = "\u090A"; // ū
		result[1][9] = "\u090F"; // e
		result[4][9] = "\u0913"; // o
		result[1][10] = "\u0910"; // ai
		result[4][10] = "\u0914"; // au
		// misc and symbols
		result[5][4] = "" + devaAnusvara;
		result[5][5] = "" + devaVisarga;
		result[5][7] = "" + devaAvagraha;
		result[5][8] = "" + devaDanda;
		result[5][9] = "" + devaDoubleDanda;
		result[5][10] = "" + devaAbbrSign;
		// numbers
		for (int n = 1; n <= 9; n++)
			result[6][n-1] = "" + devaNumbers[n];
		result[6][9] = "" + devaNumbers[0];
		return result;
	}

	public static String[][] getDevaSktLetterGridX() {
		// mainly used in SktLetters
		final String[][] result = new String[8][10];
		// 1st & 2nd row = vowels + symbols
		for (int i = 0; i < devaVowelsInd.length; i++) {
			if (i < 10)
				result[0][i] = "" + devaVowelsInd[i];
			else
				result[1][i - 10] = "" + devaVowelsInd[i];
		}
		result[1][4] = "" + devaAnusvara;
		result[1][5] = "" + devaVisarga;
		result[1][6] = "" + devaAvagraha;
		result[1][7] = "" + devaDanda;
		result[1][8] = "" + devaDoubleDanda;
		result[1][9] = "" + devaAbbrSign;
		// mutes
		for (int i = 0; i < devaConsonants.length; i++) {
			if (i < 5)
				result[2][i] = "" + devaConsonants[i];
			else if (i < 10)
				result[3][i - 5] = "" + devaConsonants[i];
			else if (i < 15)
				result[4][i - 10] = "" + devaConsonants[i];
			else if (i < 20)
				result[5][i - 15] = "" + devaConsonants[i];
			else if (i < 25)
				result[6][i - 20] = "" + devaConsonants[i];
			else
				break;
		}
		// semivowels
		result[3][5] = "\u092F"; // y;
		result[4][5] = "\u0930"; // r
		result[5][5] = "\u0932"; // l
		result[6][5] = "\u0935"; // v
		// sibilant
		result[2][6] = "\u0939"; // h
		result[3][6] = "\u0936"; // ś
		result[4][6] = "\u0937"; // ṣ
		result[5][6] = "\u0938"; // s
		// vowels
		result[2][7] = "\u0905"; // a
		result[3][7] = "\u0907"; // i
		result[4][7] = "\u090B"; // ṛ
		result[5][7] = "\u090C"; // ḷ
		result[6][7] = "\u0909"; // u
		result[2][8] = "\u0906"; // ā
		result[3][8] = "\u0908"; // ī
		result[4][8] = "\u0960"; // ṝ
		result[5][8] = "\u0961"; // ḹ
		result[6][8] = "\u090A"; // ū
		// numbers
		for (int n = 1; n <= 9; n++)
			result[7][n-1] = "" + devaNumbers[n];
		result[7][9] = "" + devaNumbers[0];
		return result;
	}

	private static String shiftCharCode(final String input, final boolean isUp) {
		// up = encode, down = decode
		final char[] chars = input.toCharArray();
		for (int i = 0; i < chars.length; i++) {
			chars[i] = isUp
						? (char) (chars[i] + saveBlockStart)
						: (char) (chars[i] - saveBlockStart);
		}
		return String.valueOf(chars);
	}
	
	private static String saveTags(final String input) {
		final Matcher matcher = tagPattern.matcher(input);
		return matcher.replaceAll(m -> shiftCharCode(m.group(), true));
	}

	private static String restoreTags(final String input) {
		final Matcher matcher = tagShiftedPattern.matcher(input);
		return matcher.replaceAll(m -> shiftCharCode(m.group(), false));
	}

	public static String transliterate(final String text, final EngineType engine) {
		return transliterate(text, engine, true, false);
	}

	public static String transliterate(final String text, final EngineType engine, final boolean withNumbers) {
		return transliterate(text, engine, withNumbers, false);
	}

	public static String transliterate(final String text, final EngineType engine, final boolean withNumbers, final boolean xslFixed) {
		final String textPrepared = saveTags(text);
        final Function<String, String> processor = translitMap.get(engine);
		if (processor == null) return text;
		alsoNumber = withNumbers;
		final String textConverted = processor.apply(textPrepared);
		final String result = restoreTags(textConverted);
		return xslFixed ? fixXslName(result, engine) : result;
	}

	public static String transliterate(final String text, final EngineType engine1, final EngineType engine2, final boolean withNumbers) {
		final String textPrepared = saveTags(text);
        final Function<String, String> processor1 = translitMap.get(engine1);
        final Function<String, String> processor2 = translitMap.get(engine2);
		if (processor1 == null || processor2 == null) return text;
		alsoNumber = withNumbers;
		String textConverted = processor1.apply(textPrepared);
		textConverted = processor2.apply(textConverted);
		final String result = restoreTags(textConverted);
		return result;
	}

	public static String translitQuick(final String text, final EngineType engine, final boolean withNumbers) {
        final Function<String, String> processor = translitMap.get(engine);
		if (processor == null) return text;
		alsoNumber = withNumbers;
		final String textConverted = processor.apply(text);
		return textConverted;
	}

	public static String translitQuick(final String text, final EngineType engine1, final EngineType engine2, final boolean withNumbers) {
        final Function<String, String> processor1 = translitMap.get(engine1);
        final Function<String, String> processor2 = translitMap.get(engine2);
		if (processor1 == null || processor2 == null) return text;
		alsoNumber = withNumbers;
		String textConverted = processor1.apply(text);
		textConverted = processor2.apply(textConverted);
		return textConverted;
	}

	public static String translitBJT(final String text, final EngineType engine, final boolean withNumbers) {
		final String savedNL = shiftCharCode(NEWLINE, true);
		String textPrepared = text.replace(NEWLINE, savedNL);
		textPrepared = saveTags(textPrepared);
        final Function<String, String> processor = translitMap.get(engine);
		if (processor == null) return text;
		alsoNumber = withNumbers;
		final Pattern textPatt = Pattern.compile("\"text\": \"(.*?)\"");
		final Matcher textMatcher = textPatt.matcher(textPrepared);
		final String textConverted = textMatcher.replaceAll(m -> "\"text\": \"" + processor.apply(m.group(1)) + "\"");
		final String textProcessed = restoreTags(textConverted);
		final String result = textProcessed.replace(savedNL, NEWLINE);
		return result;
	}

	public static String translitSC(final String text, final EngineType engine, final boolean withNumbers) {
		final String textPrepared = saveTags(text);
        final Function<String, String> processor = translitMap.get(engine);
		if (processor == null) return text;
		alsoNumber = withNumbers;
		final Pattern textPatt = Pattern.compile("\"(.*?)\": \"(.*?)\"");
		final Matcher textMatcher = textPatt.matcher(textPrepared);
		final String textConverted = textMatcher.replaceAll(m -> "\"" + m.group(1) + "\": \"" + processor.apply(m.group(2)) + "\"");
		final String result = restoreTags(textConverted);
		return result;
	}

	private static String toISO(final String text) {
		final String result = text
								.replace("ai", "a'i")
								.replace("au", "a'u")
								.replace("ē", "ai")
								.replace("ō", "au")
								.replace('e', 'ē')
								.replace('o', 'ō')
								.replace('ṃ', 'ṁ')
								.replace("ṛ", "r\u0325")
								.replace("ḷ", "l\u0325")
								.replace("ṝ", "r\u0325\u0304")
								.replace("ḹ", "l\u0325\u0304")
								.replace('ḻ', 'ḷ')
								.replace(romanDanda, '.')
								.replace(romanDoubleDanda, '.')
								.replace(romanAbbrSign, '.');
		return result;
	}

	private static String toIAST(final String text) {
		final String result = text
								.replace("ai", "a'i")
								.replace("au", "a'u")
								.replace("ē", "ai")
								.replace("ō", "au")
								//.replace('ṃ', 'ṁ')
								.replace(romanDanda, '.')
								.replace(romanDoubleDanda, '.')
								.replace(romanAbbrSign, '.');
		return result;
	}

	private static String toPaliCommon(final String text) {
		final String result = text
						.replace("ḻ", "ḷ")
						.replace("ai", "a'i")
						.replace("au", "a'u")
						.replace("ē", "ai")
						.replace("ō", "au")
						.replace(romanAvagraha + "", "’")
						.replace(romanDanda, '.')
						.replace(romanDoubleDanda, '.')
						.replace(romanAbbrSign, '.');
		return result;
	}

	private static String toLeast(final String text) {
		final String result = text
						.replace("ḷ", "ŀ")
						.replace("ḻ", "ḷ");
		return result;
	}

	private static String toUnique(final String text, final boolean asPali) {
		String result = asPali 
						? text.replace("ḷ", "ḻ")
						: text.replace("ē", "e").replace("ō", "o")
							.replace("ai", "ē").replace("au", "ō");
		result = result
				.replace("ŀ", "ḷ") // Least
				.replace("a'i", "ē") // Common; ai = a + i not ai
				.replace("a'u", "ō") // Common; au = a + u not au
				.replace('ṁ', 'ṃ') // ISO
				.replace("r\u0325\u0304", "ṝ") // ISO
				.replace("l\u0325\u0304", "ḹ") // ISO
				.replace("r\u0325", "ṛ") // ISO
				.replace("l\u0325", "ḷ"); // ISO
		return result;
	}

	private static String fixXslName(final String text, final EngineType engine) {
		return fixXslName(text, engine.getSourceScript(), engine.getTargetScript());
	}

	public static String fixXslName(final String text, final PaliScript srcScript, final PaliScript tgtScript) {
		// just for CST4 and CSTDEVA XMLs
		final String oldXsl = "tipitaka-" + srcScript.getCstAbbr() + "\\.xsl";
		final String newXsl = "tipitaka-" + tgtScript.getCstAbbr() + "\\.xsl";
		return text.replaceFirst(oldXsl, newXsl);
	}

	private static String devanagariToRomanUnique(final String text) {
		final StringBuilder output = new StringBuilder();
		char[] input = text.toCharArray();
		// generate hash maps to ease the replacements
		final Map<Character, Character> indVowelMap = new HashMap<>();
		for (int i = 0; i < devaVowelsInd.length; i++)
			indVowelMap.put(devaVowelsInd[i], romanVowelsUnique.charAt(i));
		final Map<Character, Character> depVowelMap = new HashMap<>();
		for (int i = 0; i < devaVowelsDep.length; i++)
			depVowelMap.put(devaVowelsDep[i], romanVowelsUnique.charAt(i));
		final Map<Character, String> consonantMap = new HashMap<>();
		for (int i = 0; i < devaConsonants.length; i++)
			consonantMap.put(devaConsonants[i], romanConsonantsStrUnique[i]);
		String rch; // for roman char
		char dch; // for deva char
		int ind; // general purpose index
		boolean skipFlag = false;
		for (int index = 0; index < input.length; index++) {
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
			} else if (dch == devaAnusvara) {
				// niggahita
				rch = "" + romanAnusvara;
			} else if (dch == devaVisarga) {
				// visarga
				rch = "" + romanVisarga;
			} else if (dch == devaAvagraha) {
				// avagraha
				rch = "" + romanAvagraha;
			} else if (dch == devaDanda) {
				// single danda
				rch = "" + romanDanda;
			} else if (dch == devaDoubleDanda) {
				// double danda
				rch = "" + romanDoubleDanda;
			} else if (dch == devaAbbrSign) {
				// abbreviation sign
				rch = "" + romanAbbrSign;
			} else if (Arrays.binarySearch(devaVISorted, dch) >= 0) {
				// independent vowels
				rch = Character.toString(indVowelMap.get(dch));
			} else if (Arrays.binarySearch(devaVDSorted, dch) >= 0) {
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
				} else if (consonantMap.get(dch) != null && dch != devaAnusvara && 
						Arrays.binarySearch(devaVDSorted, input[index+1]) < 0) {
					// double Devanagari consonants, 'a' is added (not anusvara, not followed by dependent vowels)
					output.append('a');
				}
			} else {
				// if the last char is a consonant, not a niggahita/anusvara, add 'a'
				if (consonantMap.get(dch) != null && dch != devaAnusvara)
					output.append('a');
			}
		} // end for
		// also remove nukta and zero width joiner, if present
		return output.toString().replace(Character.toString(devaNukta), "").replace("\u200D", "");
	}

	private static String devanagariToThaiRaw(final String text) {
		final StringBuilder output = new StringBuilder();
		char[] input = text.toCharArray();
		// generate hash maps to ease the replacements
		final Map<Character, String> indVowelMap = new HashMap<>();
		for (int i = 0; i < devaVowelsInd.length; i++)
			indVowelMap.put(devaVowelsInd[i], thaiVowelsInd[i]);
		final Map<Character, String> depVowelMap = new HashMap<>();
		for (int i = 1; i < devaVowelsDep.length; i++)
			depVowelMap.put(devaVowelsDep[i], thaiVowelsDep[i]);
		final Map<Character, Character> consonantMap = new HashMap<>();
		for (int i = 0; i < devaConsonants.length; i++)
			consonantMap.put(devaConsonants[i], thaiConsonants[i]);
		String tch; // for thai char
		char dch; // for deva char
		int ind; // general purpose index
		for (int index = 0; index < input.length; index++) {
			dch = input[index];
			tch = Character.toString(dch); // in case of non-character
			if ((ind = Arrays.binarySearch(devaNumbers, dch)) >= 0) {
				// numbers
				tch = Character.toString(thaiNumbers[ind]);
			} else if (dch == devaAnusvara) {
				// niggahita
				tch = "" + thaiAnusvara;
			} else if (dch == devaVisarga) {
				// visarga
				tch = "" + thaiVisarga;
			} else if (dch == devaAvagraha) {
				// avagraha
				tch = "" + thaiAvagraha;
			} else if (dch == devaVirama) {
				// virama
				tch = "" + thaiVirama;
			} else if (dch == devaDanda) {
				// single danda
				tch = "" + thaiDanda;
			} else if (dch == devaDoubleDanda) {
				// double danda
				tch = "" + thaiDoubleDanda;
			} else if (dch == devaAbbrSign) {
				// abbreviation sign
				tch = "" + thaiAbbrSign;
			} else if (Arrays.binarySearch(devaVISorted, dch) >= 0) {
				// independent vowels
				tch = indVowelMap.get(dch);
			} else if (Arrays.binarySearch(devaVDSorted, dch) >= 0) {
				// dependent vowels
				tch = depVowelMap.get(dch);
			} else {
				// consonants
				final Character ch = consonantMap.get(dch);
				tch = ch == null ? Character.toString(dch) : Character.toString(ch);
			}
			output.append(tch);
		} // end for
		// also remove nukta and zero width joiner, if present
		return output.toString().replace(Character.toString(devaNukta), "").replace("\u200D", "");
	}

	private static String processToThai(final String text) {
		// reposition some vowels
		String result = text;
		final Pattern ePatt = Pattern.compile("(.)(\u0E40)");
		final Pattern oPatt = Pattern.compile("(.)(\u0E42)");
		final Pattern aiPatt = Pattern.compile("(.)(\u0E44)");
		result = ePatt.matcher(result).replaceAll("$2$1");
		result = oPatt.matcher(result).replaceAll("$2$1");
		result = aiPatt.matcher(result).replaceAll("$2$1");
		return result;
	}

	private static String processFromThai(final String text) {
		// reverse position some vowels
		String result = text;
		final Pattern ePatt = Pattern.compile("(\u0E40)(.)");
		final Pattern oPatt = Pattern.compile("(\u0E42)(.)");
		final Pattern aiPatt = Pattern.compile("(\u0E44)(.)");
		result = ePatt.matcher(result).replaceAll("$2$1");
		result = oPatt.matcher(result).replaceAll("$2$1");
		result = aiPatt.matcher(result).replaceAll("$2$1");
		// change UE to I + Niggahita
		result = result.replace("\u0E36", "\u0E34\u0E4D");
		// change alt ñ and ṭ to normal
		result = result.replace('\uF70F', '\u0E0D').replace('\uF700', '\u0E10');
		// substitute two-letter dependent vowels: RUU, LUU, AU
		result = result.replace("\u0E24\u0E45", "" + saveChars[0]);
		result = result.replace("\u0E26\u0E45", "" + saveChars[1]);
		result = result.replace("\u0E40\u0E32", "" + saveChars[2]);
		return result;
	}

	private static String devanagariToKhmerRaw(final String text) {
		final StringBuilder output = new StringBuilder();
		char[] input = text.toCharArray();
		// generate hash maps to ease the replacements
		final Map<Character, String> indVowelMap = new HashMap<>();
		for (int i = 0; i < devaVowelsInd.length; i++)
			indVowelMap.put(devaVowelsInd[i], khmerVowelsInd[i]);
		final Map<Character, Character> depVowelMap = new HashMap<>();
		for (int i = 1; i < devaVowelsDep.length; i++)
			depVowelMap.put(devaVowelsDep[i], khmerVowelsDep[i]);
		final Map<Character, Character> consonantMap = new HashMap<>();
		for (int i = 0; i < devaConsonants.length; i++)
			consonantMap.put(devaConsonants[i], khmerConsonants[i]);
		String kch; // for khmer char
		char dch; // for deva char
		int ind; // general purpose index
		for (int index = 0; index < input.length; index++) {
			dch = input[index];
			kch = Character.toString(dch); // in case of non-character
			if ((ind = Arrays.binarySearch(devaNumbers, dch)) >= 0) {
				// numbers
				kch = Character.toString(khmerNumbers[ind]);
			} else if (dch == devaAnusvara) {
				// niggahita
				kch = "" + khmerAnusvara;
			} else if (dch == devaVisarga) {
				// visarga
				kch = "" + khmerVisarga;
			} else if (dch == devaAvagraha) {
				// avagraha
				kch = "" + khmerAvagraha;
			} else if (dch == devaVirama) {
				// virama
				kch = "" + khmerCoeng;
			} else if (dch == devaDanda) {
				// single danda
				kch = "" + khmerDanda;
			} else if (dch == devaDoubleDanda) {
				// double danda
				kch = "" + khmerDoubleDanda;
			} else if (dch == devaAbbrSign) {
				// abbreviation sign
				kch = "" + khmerAbbrSign;
			} else if (Arrays.binarySearch(devaVISorted, dch) >= 0) {
				// independent vowels
				kch = indVowelMap.get(dch);
			} else if (Arrays.binarySearch(devaVDSorted, dch) >= 0) {
				// dependent vowels
				// check for RU, RUU, LU, LUU first, if any add Choeng
				if (dch == '\u0943' || dch == '\u0944' || dch == '\u0962' || dch == '\u0963')
					kch = "" + khmerCoeng + depVowelMap.get(dch);
				else
					kch = "" + depVowelMap.get(dch);
			} else {
				// consonants
				final Character ch = consonantMap.get(dch);
				kch = ch == null ? Character.toString(dch) : Character.toString(ch);
			}
			output.append(kch);
		} // end for
		// also remove nukta and zero width joiner, if present
		return output.toString().replace(Character.toString(devaNukta), "").replace("\u200D", "");
	}

	private static String processToKhmer(final String text) {
		String result = text;
		// change Coeng to Killer when not followed by a consonant
		final Pattern kPatt = Pattern.compile(khmerCoeng + "([^" + khmerAllConsonants + khmerRuLu + "])");
		result = kPatt.matcher(result).replaceAll(khmerKiller + "$1");
		// the last character is Choeng, change to Killer
		if (!text.isEmpty() && text.charAt(text.length() - 1) == khmerCoeng)
			result = result.substring(0, result.length() - 1) + khmerKiller;
		return result;
	}

	private static String processFromKhmer(final String text) {
		String result = text;
		// substitute Choeng + Ru and so on
		result = result.replace(khmerCoeng + "\u17AB", "" + saveChars[0]);
		result = result.replace(khmerCoeng + "\u17AC", "" + saveChars[1]);
		result = result.replace(khmerCoeng + "\u17AD", "" + saveChars[2]);
		result = result.replace(khmerCoeng + "\u17AE", "" + saveChars[3]);
		// change Killer to Coeng
		result = result.replace(khmerKiller, khmerCoeng);
		return result;
	}

	private static String devanagariToSinhala(final String text) {
		final StringBuilder output = new StringBuilder();
		char[] input = text.toCharArray();
		// generate hash maps to ease the replacements
		final Map<Character, Character> indVowelMap = new HashMap<>();
		for (int i = 0; i < devaVowelsInd.length; i++)
			indVowelMap.put(devaVowelsInd[i], sinhalaVowelsInd[i]);
		final Map<Character, Character> depVowelMap = new HashMap<>();
		for (int i = 1; i < devaVowelsDep.length; i++)
			depVowelMap.put(devaVowelsDep[i], sinhalaVowelsDep[i]);
		final Map<Character, Character> consonantMap = new HashMap<>();
		for (int i = 0; i < devaConsonants.length; i++)
			consonantMap.put(devaConsonants[i], sinhalaConsonants[i]);
		String sch; // for sinhala char
		char dch; // for deva char
		int ind; // general purpose index
		for (int index = 0; index < input.length; index++) {
			dch = input[index];
			sch = Character.toString(dch); // in case of non-character
			if ((ind = Arrays.binarySearch(devaNumbers, dch)) >= 0) {
				// numbers, sinhala uses roman
				sch = Character.toString(romanNumbers[ind]);
			} else if (dch == devaAnusvara) {
				// niggahita
				sch = "" + sinhalaAnusvara;
			} else if (dch == devaVisarga) {
				// visarga
				sch = "" + sinhalaVisarga;
			} else if (dch == devaAvagraha) {
				// avagraha
				sch = "" + sinhalaAvagraha;
			} else if (dch == devaVirama) {
				// virama
				sch = "" + sinhalaVirama;
			} else if (dch == devaDanda) {
				// single danda
				sch = "" + sinhalaDanda;
			} else if (dch == devaDoubleDanda) {
				// double danda
				sch = "" + sinhalaDoubleDanda;
			} else if (dch == devaAbbrSign) {
				// abbreviation sign
				sch = "" + sinhalaAbbrSign;
			} else if (Arrays.binarySearch(devaVISorted, dch) >= 0) {
				// independent vowels
				sch = "" + indVowelMap.get(dch);
			} else if (Arrays.binarySearch(devaVDSorted, dch) >= 0) {
				// dependent vowels
				sch = "" + depVowelMap.get(dch);
			} else {
				// consonants
				final Character ch = consonantMap.get(dch);
				sch = ch == null ? Character.toString(dch) : Character.toString(ch);
			}
			output.append(sch);
		} // end for
		// also remove nukta and zero width joiner, if present
		return output.toString().replace(Character.toString(devaNukta), "").replace("\u200D", "");
	}

	private static String devanagariToMyanmarRaw(final String text) {
		final StringBuilder output = new StringBuilder();
		char[] input = text.toCharArray();
		// generate hash maps to ease the replacements
		final Map<Character, String> indVowelMap = new HashMap<>();
		for (int i = 0; i < devaVowelsInd.length; i++)
			indVowelMap.put(devaVowelsInd[i], myanmarVowelsInd[i]);
		final Map<Character, String> depVowelMap = new HashMap<>();
		for (int i = 1; i < devaVowelsDep.length; i++)
			depVowelMap.put(devaVowelsDep[i], myanmarVowelsDep[i]);
		final Map<Character, Character> consonantMap = new HashMap<>();
		for (int i = 0; i < devaConsonants.length; i++)
			consonantMap.put(devaConsonants[i], myanmarConsonants[i]);
		String mch; // for myanmar char
		char dch; // for deva char
		int ind; // general purpose index
		for (int index = 0; index < input.length; index++) {
			dch = input[index];
			mch = Character.toString(dch); // in case of non-character
			if ((ind = Arrays.binarySearch(devaNumbers, dch)) >= 0) {
				// numbers
				mch = Character.toString(myanmarNumbers[ind]);
			} else if (dch == devaAnusvara) {
				// niggahita
				mch = "" + myanmarAnusvara;
			} else if (dch == devaVisarga) {
				// visarga
				mch = "" + myanmarVisarga;
			} else if (dch == devaAvagraha) {
				// avagraha
				mch = "" + myanmarAvagraha;
			} else if (dch == devaVirama) {
				// virama
				mch = "" + myanmarVirama;
			} else if (dch == devaDanda) {
				// single danda, in CST4 double danda is used
//~ 				mch = "" + myanmarDoubleDanda;
				mch = "" + myanmarDanda;
			} else if (dch == devaDoubleDanda) {
				// double danda
				mch = "" + myanmarDoubleDanda;
			} else if (dch == devaAbbrSign) {
				// abbreviation sign
				mch = "" + myanmarAbbrSign;
			} else if (Arrays.binarySearch(devaVISorted, dch) >= 0) {
				// independent vowels
				mch = indVowelMap.get(dch);
			} else if (Arrays.binarySearch(devaVDSorted, dch) >= 0) {
				// dependent vowels
				mch = depVowelMap.get(dch);
			} else {
				// consonants
				final Character ch = consonantMap.get(dch);
				mch = ch == null ? Character.toString(dch) : Character.toString(ch);
			}
			output.append(mch);
		} // end for
		// also remove nukta and zero width joiner, if present
		return output.toString().replace(Character.toString(devaNukta), "").replace("\u200D", "");
	}

	private static String processToMyanmar(final String text) {
		String result = text;
		// change Virama to Asat Killer when not followed by a consonant
		final Pattern kPatt = Pattern.compile(myanmarVirama + "([^" + myanmarAllConsonants + "])");
		result = kPatt.matcher(result).replaceAll(myanmarAsat + "$1");
		// change to tall AA
		for (final String chs : myanmarTallAAList) {
			result = result.replace(chs, chs.substring(0, chs.length() - 1) + myanmarTallAA);
		}
		// fix some back to short
		for (final String chs : myanmarShortAAList) {
			result = result.replace(chs, chs.substring(0, chs.length() - 1) + myanmarShortAA);
		}
		// fix some conjuncts
		final String ngaVirama = "" + myanmarNga + myanmarVirama;
		result = result.replace(ngaVirama, "" + myanmarNga + myanmarAsat + myanmarVirama);
		final String doubleNya = "" + myanmarNya + myanmarVirama + myanmarNya;
		result = result.replace(doubleNya, "" + myanmarNnya);
		final String doubleSa = "" + myanmarSa + myanmarVirama + myanmarSa;
		result = result.replace(doubleSa, "" + myanmarGreatSa);
		for (final char ch : myanmarToMedialMap.keySet()) {
			final String medCh = "" + myanmarVirama + ch;
			result = result.replace(medCh, "" + myanmarToMedialMap.get(ch));
		}
		// the last character is Virama, change to Asat
		if (!text.isEmpty() && text.charAt(text.length() - 1) == myanmarVirama)
			result = result.substring(0, result.length() - 1) + myanmarAsat;
		// change comma to danda, imitating CST4 conversion
//~ 		result = result.replace(',', myanmarDanda);
		// for test
//~ 		result = result.replace('…', myanmarDoubleDanda);
		return result;
	}

	private static String processFromMyanmar(final String text) {
		String result = text;
		// restore some conjuncts
		final String ngaVirama = "" + myanmarNga + myanmarVirama;
		result = result.replace("" + myanmarNga + myanmarAsat + myanmarVirama, ngaVirama);
		final String doubleNya = "" + myanmarNya + myanmarVirama + myanmarNya;
		result = result.replace("" + myanmarNnya, doubleNya);
		final String doubleSa = "" + myanmarSa + myanmarVirama + myanmarSa;
		result = result.replace("" + myanmarGreatSa, doubleSa);
		for (final char ch : myanmarFromMedialMap.keySet()) {
			result = result.replace("" + ch, "" + myanmarFromMedialMap.get(ch));
		}
		// change tall A to short A
		result = result.replace(myanmarTallAA, myanmarShortAA);
		// change Asat to Virama
		result = result.replace(myanmarAsat, myanmarVirama);
		// substitutions for multiple letters
		result = result.replace("\u1021" + myanmarShortAA, "" + saveChars[0]); // ind AA
		result = result.replace("\u1021\u1032", "" + saveChars[1]); // ind AI
		result = result.replace("\u1031\u102C", "" + saveChars[2]); // dep O
		result = result.replace("\u1031\u102C\u103A", "" + saveChars[3]); // dep AU
		return result;
	}

	private static String romanToDevanagari(final String text) {
		final StringBuilder output = new StringBuilder();
		char[] input = text.toLowerCase().toCharArray();
		char rch; // for roman
		char dch; // for deva
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
				// if numbers included
				if (alsoNumber)
					dch = devaNumbers[Character.digit(rch, 10)];
			} else if (rch == romanAnusvara) {
				// niggahita
				dch = devaAnusvara;
			} else if (rch == romanVisarga) {
				// visarga
				dch = devaVisarga;
			} else if (rch == romanAvagraha) {
				// avagraha
				dch = devaAvagraha;
			} else if (rch == romanDanda) {
				// single danda
				dch = devaDanda;
			} else if (rch == romanDoubleDanda) {
				// double danda
				dch = devaDoubleDanda;
			} else if (rch == romanAbbrSign) {
				// abbreviation sign
				dch = devaAbbrSign;
			} else if (rch == '.') {
				// dot retained
				dch = '.';
			} else if (rch == 'x') {
				// reserved character
				dch = rch;
			} else if ((vindex = romanVowelsUnique.indexOf(rch)) >= 0) {
				// vowels, set it to stand-alone/independent first
				dch = devaVowelsInd[vindex];
			} else {
				// consonants
				for (int i = 0; i < romanConsonantsChrUnique.length; i++) {
					if (rch == romanConsonantsChrUnique[i]) {
						if (index < input.length-1) {
							// if the character has 'h'
							if (romanWithHChars.indexOf(rch) >= 0 && input[index + 1] == 'h')
								skipFlag = true;
						}
						dch = skipFlag ? devaConsonants[i + 1] : devaConsonants[i];
						break;
					}
				} // end for loop of finding consonants
			}
			// 2. consider how to put it
			if (vindex >= 0) {
				// vowels
				if (output.length() == 0) {
					// to prevent index out of bound
					// independent vowels
					output.append(dch);
				} else {
					// look at the preceeding character;
					// if it is not a consonant, independent vowels are used
					if (romanConsonantsUnique.indexOf(input[index - 1]) < 0) {
						output.append(dch);
					} else {
						// dependent vowels are used
						if (rch != 'a') {
							output.append(devaVowelsDep[vindex]);
						}
					}
				}
			} else {
				if (romanConsonantsUnique.indexOf(rch) >= 0) {
					// consonants
					output.append(dch);
					if (index < input.length-1) {
						if (!skipFlag) {
							if (romanConsonantsUnique.indexOf(input[index + 1]) >= 0) {
								// double consonant needs virama
								output.append(devaVirama);
							} else if (romanVowelsUnique.indexOf(input[index + 1]) == -1) {
								// if not followed by a vowel, add virama
								output.append(devaVirama);
							}
						} else {
							// characters with h
							if (index < input.length-2) {
								if (romanConsonantsUnique.indexOf(input[index + 2]) >= 0) {
									// double consonant needs virama
									output.append(devaVirama);
								} else if (romanVowelsUnique.indexOf(input[index + 2]) == -1) {
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
			vindex = -1; // reset vowel index
		} // end for loop of each input character
		return output.toString();
	}

	private static String thaiRawToDevanagari(final String text) {
		final StringBuilder output = new StringBuilder();
		char[] input = text.toCharArray();
		// generate hash maps to ease the replacements
		final Map<String, Character> indVowelMapWithA = new HashMap<>();
		for (int i = 1; i < devaVowelsInd.length; i++) {
			if ((i >= 1 && i <= 5) || (i >= 10 && i <= 12)) {
				indVowelMapWithA.put(thaiVowelsInd[i], devaVowelsInd[i]);
			} else if (i == 13) {
				// independent AU needs substitution
				indVowelMapWithA.put("\u0E2D" + saveChars[2], devaVowelsInd[i]);
			}
		}
		final Map<Character, Character> depVowelMap = new HashMap<>();
		for (int i = 1; i < thaiVowelsDepSub.length; i++) {
			if ((i >= 1 && i <= 5) || (i >= 10 && i <= 13))
				depVowelMap.put(thaiVowelsDepSub[i], devaVowelsDep[i]);
		}
		final String[] indVSorted = indVowelMapWithA.keySet().toArray(new String[0]);
		final char[] depVSorted = Arrays.copyOfRange(thaiVowelsDepSub, 1, thaiVowelsDepSub.length);
		Arrays.sort(indVSorted);
		Arrays.sort(depVSorted);
		final Map<Character, Character> consonantMap = new HashMap<>();
		for (int i = 0; i < thaiConsonants.length; i++)
			consonantMap.put(thaiConsonants[i], devaConsonants[i]);
		char dch; // for deva char
		char tch; // for thai char
		int ind; // general purpose index
		boolean skipFlag = false;
		for (int index = 0; index < input.length; index++) {
			if (skipFlag) {
				skipFlag = false;
				continue;
			}
			tch = input[index];
			dch = tch; // in case of non-character
			if ((ind = Arrays.binarySearch(thaiNumbers, tch)) >= 0) {
				// numbers
				dch = devaNumbers[ind];
			} else if (tch == thaiAnusvara) {
				// niggahita
				dch = devaAnusvara;
			} else if (tch == thaiVisarga) {
				// visarga
				dch = devaVisarga;
			} else if (tch == thaiVirama) {
				// virama
				dch = devaVirama;
			} else if (tch == thaiDanda) {
				// single danda
				dch = devaDanda;
			} else if (tch == thaiDoubleDanda) {
				// double danda
				dch = devaDoubleDanda;
			} else if (tch == '\u0E2D') {
				// if A is found, it is an independent vowel
				final String v = index < input.length - 1 ? "" + tch + input[index + 1] : "" + tch;
				if (Arrays.binarySearch(indVSorted, v) >= 0) {
					// two-letter vowels
					dch = indVowelMapWithA.get(v);
					skipFlag = true;
				} else {
					// one-letter, only A
					dch = devaVowelsInd[0];
				}
			} else if (tch == '\u0E24') {
				// RU is found
				final String vb = index == 0 ? "" + tch : "" + input[index - 1] + tch;
				final boolean isDep = vb.matches("[" + thaiAllConsonants + "]" + tch);
				dch = isDep ? '\u0943' : '\u090B';
			} else if (tch == saveChars[0]) {
				// RUU is found
				final String vb = index == 0 ? "" + tch : "" + input[index - 1] + tch;
				final boolean isDep = vb.matches("[" + thaiAllConsonants + "]" + tch);
				dch = isDep ? '\u0944' : '\u0960';
			} else if (tch == '\u0E26') {
				// LU is found
				final String vb = index == 0 ? "" + tch : "" + input[index - 1] + tch;
				final boolean isDep = vb.matches("[" + thaiAllConsonants + "]" + tch);
				dch = isDep ? '\u0962' : '\u090C';
			} else if (tch == saveChars[1]) {
				// LUU is found
				final String vb = index == 0 ? "" + tch : "" + input[index - 1] + tch;
				final boolean isDep = vb.matches("[" + thaiAllConsonants + "]" + tch);
				dch = isDep ? '\u0963' : '\u0961';
			} else if (Arrays.binarySearch(depVSorted, tch) >= 0) {
				// dependent vowels
				dch = depVowelMap.get(tch);
			} else {
				// consonants
				final Character ch = consonantMap.get(tch);
				dch = ch == null ? tch : ch;
			}
			output.append(dch);
		} // end for
		return output.toString();
	}

	private static String khmerRawToDevanagari(final String text) {
		final StringBuilder output = new StringBuilder();
		char[] input = text.toCharArray();
		// generate hash maps to ease the replacements
		final Map<Character, Character> indVowelMap = new HashMap<>();
		for (int i = 2; i < devaVowelsInd.length; i++) {
			// from I onward
			indVowelMap.put(khmerVowelsInd[i].charAt(0), devaVowelsInd[i]);
		}
		final Map<Character, Character> depVowelMap = new HashMap<>();
		for (int i = 1; i < khmerVowelsDepSub.length; i++) {
			// from AA onward
			depVowelMap.put(khmerVowelsDepSub[i], devaVowelsDep[i]);
		}
		final Character[] indVSorted = indVowelMap.keySet().toArray(new Character[0]);
		final char[] depVSorted = Arrays.copyOfRange(khmerVowelsDepSub, 1, khmerVowelsDepSub.length);
		Arrays.sort(indVSorted);
		Arrays.sort(depVSorted);
		final Map<Character, Character> consonantMap = new HashMap<>();
		for (int i = 0; i < khmerConsonants.length; i++)
			consonantMap.put(khmerConsonants[i], devaConsonants[i]);
		char dch; // for deva char
		char kch; // for khmer char
		int ind; // general purpose index
		boolean skipFlag = false;
		for (int index = 0; index < input.length; index++) {
			if (skipFlag) {
				skipFlag = false;
				continue;
			}
			kch = input[index];
			dch = kch; // in case of non-character
			if ((ind = Arrays.binarySearch(khmerNumbers, kch)) >= 0) {
				// numbers
				dch = devaNumbers[ind];
			} else if (kch == khmerAnusvara) {
				// niggahita
				dch = devaAnusvara;
			} else if (kch == khmerVisarga) {
				// visarga
				dch = devaVisarga;
			} else if (kch == khmerCoeng) {
				// virama
				dch = devaVirama;
			} else if (kch == khmerDanda) {
				// single danda
				dch = devaDanda;
			} else if (kch == khmerDoubleDanda) {
				// double danda
				dch = devaDoubleDanda;
			} else if (kch == '\u17A2') {
				// if A is found, it can be independent A or AA
				final String v = index < input.length - 1 ? "" + kch + input[index + 1] : "" + kch;
				if (v.equals("\u17a2\u17b6")) {
					// two-letter vowels
					dch = '\u0906';
					skipFlag = true;
				} else {
					// one-letter, only A
					dch = '\u0905';
				}
			} else if (Arrays.binarySearch(indVSorted, kch) >= 0) {
				// other indepedent vowels
				dch = indVowelMap.get(kch);
			} else if (Arrays.binarySearch(depVSorted, kch) >= 0) {
				// dependent vowels
				dch = depVowelMap.get(kch);
			} else {
				// consonants
				final Character ch = consonantMap.get(kch);
				dch = ch == null ? kch : ch;
			}
			output.append(dch);
		} // end for
		return output.toString();
	}

	private static String sinhalaToDevanagari(final String text) {
		final StringBuilder output = new StringBuilder();
		char[] input = text.toCharArray();
		// generate hash maps to ease the replacements
		final Map<Character, Character> indVowelMap = new HashMap<>();
		for (int i = 0; i < devaVowelsInd.length; i++) {
			indVowelMap.put(sinhalaVowelsInd[i], devaVowelsInd[i]);
		}
		final Map<Character, Character> depVowelMap = new HashMap<>();
		for (int i = 1; i < sinhalaVowelsDep.length; i++) {
			// from AA onward
			depVowelMap.put(sinhalaVowelsDep[i], devaVowelsDep[i]);
		}
		final char[] indVSorted = Arrays.copyOf(sinhalaVowelsInd, sinhalaVowelsInd.length);
		final char[] depVSorted = Arrays.copyOfRange(sinhalaVowelsDep, 1, sinhalaVowelsDep.length);
		Arrays.sort(indVSorted);
		Arrays.sort(depVSorted);
		final Map<Character, Character> consonantMap = new HashMap<>();
		for (int i = 0; i < sinhalaConsonants.length; i++)
			consonantMap.put(sinhalaConsonants[i], devaConsonants[i]);
		char dch; // for deva char
		char sch; // for sinhala char
		int ind; // general purpose index
		boolean skipFlag = false;
		for (int index = 0; index < input.length; index++) {
			if (skipFlag) {
				skipFlag = false;
				continue;
			}
			sch = input[index];
			dch = sch; // in case of non-character
			if ((ind = Arrays.binarySearch(romanNumbers, sch)) >= 0) {
				// numbers, use Roman
				dch = devaNumbers[ind];
			} else if (sch == sinhalaAnusvara) {
				// niggahita
				dch = devaAnusvara;
			} else if (sch == sinhalaVisarga) {
				// visarga
				dch = devaVisarga;
			} else if (sch == sinhalaVirama) {
				// virama
				dch = devaVirama;
			} else if (Arrays.binarySearch(indVSorted, sch) >= 0) {
				// indepedent vowels
				dch = indVowelMap.get(sch);
			} else if (Arrays.binarySearch(depVSorted, sch) >= 0) {
				// dependent vowels
				dch = depVowelMap.get(sch);
			} else {
				// consonants
				final Character ch = consonantMap.get(sch);
				dch = ch == null ? sch : ch;
			}
			output.append(dch);
		} // end for
		return output.toString();
	}

	private static String myanmarRawToDevanagari(final String text) {
		final StringBuilder output = new StringBuilder();
		char[] input = text.toCharArray();
		// generate hash maps to ease the replacements
		final Map<Character, Character> indVowelMap = new HashMap<>();
		for (int i = 0; i < devaVowelsInd.length; i++) {
			indVowelMap.put(myanmarVowelsIndSub[i], devaVowelsInd[i]);
		}
		final Map<Character, Character> depVowelMap = new HashMap<>();
		for (int i = 1; i < myanmarVowelsDepSub.length; i++) {
			// from AA onward
			depVowelMap.put(myanmarVowelsDepSub[i], devaVowelsDep[i]);
		}
		final Character[] indVSorted = indVowelMap.keySet().toArray(new Character[0]);
		final Character[] depVSorted = depVowelMap.keySet().toArray(new Character[0]);
		Arrays.sort(indVSorted);
		Arrays.sort(depVSorted);
		final Map<Character, Character> consonantMap = new HashMap<>();
		for (int i = 0; i < myanmarConsonants.length; i++)
			consonantMap.put(myanmarConsonants[i], devaConsonants[i]);
		char dch; // for deva char
		char mch; // for myanmar char
		int ind; // general purpose index
		boolean skipFlag = false;
		for (int index = 0; index < input.length; index++) {
			if (skipFlag) {
				skipFlag = false;
				continue;
			}
			mch = input[index];
			dch = mch; // in case of non-character
			if ((ind = Arrays.binarySearch(myanmarNumbers, mch)) >= 0) {
				// numbers
				dch = devaNumbers[ind];
			} else if (mch == myanmarAnusvara) {
				// niggahita
				dch = devaAnusvara;
			} else if (mch == myanmarVisarga) {
				// visarga
				dch = devaVisarga;
			} else if (mch == myanmarVirama) {
				// virama
				dch = devaVirama;
			} else if (mch == myanmarDanda) {
				// single danda
				dch = devaDanda;
			} else if (mch == myanmarDoubleDanda) {
				// double danda
				dch = devaDoubleDanda;
			} else if (Arrays.binarySearch(indVSorted, mch) >= 0) {
				// other indepedent vowels
				dch = indVowelMap.get(mch);
			} else if (Arrays.binarySearch(depVSorted, mch) >= 0) {
				// dependent vowels
				dch = depVowelMap.get(mch);
			} else {
				// consonants
				final Character ch = consonantMap.get(mch);
				dch = ch == null ? mch : ch;
			}
			output.append(dch);
		} // end for
		return output.toString();
	}

	public static String translitPaliScript(final String text,
			final PaliScript fromScript, final PaliScript toScript,
			final EngineType romanDef, final boolean withNumbers, final boolean asSanskrit) {
		String result = text;
		switch (toScript) {
			case ROMAN:
				switch(fromScript) {
					case ROMAN:
						result = asSanskrit
									? transliterate(text, EngineType.ROMAN_SKT_DEVA, romanDef, withNumbers)
									: transliterate(text, EngineType.ROMAN_DEVA, romanDef, withNumbers);
						break;
					case DEVANAGARI:
						result = transliterate(text, romanDef, withNumbers);
						break;
					case KHMER:
						result = transliterate(text, EngineType.KHMER_DEVA, romanDef, withNumbers);
						break;
					case MYANMAR:
						result = transliterate(text, EngineType.MYANMAR_DEVA, romanDef, withNumbers);
						break;
					case SINHALA:
						result = transliterate(text, EngineType.SINHALA_DEVA, romanDef, withNumbers);
						break;
					case THAI:
						result = transliterate(text, EngineType.THAI_DEVA, romanDef, withNumbers);
						break;
				}
				break;
			case DEVANAGARI:
				switch(fromScript) {
					case ROMAN:
						result = asSanskrit
									? transliterate(text, EngineType.ROMAN_SKT_DEVA, withNumbers)
									: transliterate(text, EngineType.ROMAN_DEVA, withNumbers);
						break;
					case KHMER:
						result = transliterate(text, EngineType.KHMER_DEVA, withNumbers);
						break;
					case MYANMAR:
						result = transliterate(text, EngineType.MYANMAR_DEVA, withNumbers);
						break;
					case SINHALA:
						result = transliterate(text, EngineType.SINHALA_DEVA, withNumbers);
						break;
					case THAI:
						result = transliterate(text, EngineType.THAI_DEVA, withNumbers);
						break;
				}
				break;
			case KHMER:
				switch(fromScript) {
					case ROMAN:
						result = asSanskrit
									? transliterate(text, EngineType.ROMAN_SKT_DEVA, EngineType.DEVA_KHMER, withNumbers)
									: transliterate(text, EngineType.ROMAN_DEVA, EngineType.DEVA_KHMER, withNumbers);
						break;
					case DEVANAGARI:
						result = transliterate(text, EngineType.DEVA_KHMER, withNumbers);
						break;
					case MYANMAR:
						result = transliterate(text, EngineType.MYANMAR_DEVA, EngineType.DEVA_KHMER, withNumbers);
						break;
					case SINHALA:
						result = transliterate(text, EngineType.SINHALA_DEVA, EngineType.DEVA_KHMER, withNumbers);
						break;
					case THAI:
						result = transliterate(text, EngineType.THAI_DEVA, EngineType.DEVA_KHMER, withNumbers);
						break;
				}
				break;
			case MYANMAR:
				switch(fromScript) {
					case ROMAN:
						result = asSanskrit
									? transliterate(text, EngineType.ROMAN_SKT_DEVA, EngineType.DEVA_MYANMAR, withNumbers)
									: transliterate(text, EngineType.ROMAN_DEVA, EngineType.DEVA_MYANMAR, withNumbers);
						break;
					case DEVANAGARI:
						result = transliterate(text, EngineType.DEVA_MYANMAR, withNumbers);
						break;
					case KHMER:
						result = transliterate(text, EngineType.KHMER_DEVA, EngineType.DEVA_MYANMAR, withNumbers);
						break;
					case SINHALA:
						result = transliterate(text, EngineType.SINHALA_DEVA, EngineType.DEVA_MYANMAR, withNumbers);
						break;
					case THAI:
						result = transliterate(text, EngineType.THAI_DEVA, EngineType.DEVA_MYANMAR, withNumbers);
						break;
				}
				break;
			case SINHALA:
				switch(fromScript) {
					case ROMAN:
						result = asSanskrit
									? transliterate(text, EngineType.ROMAN_SKT_DEVA, EngineType.DEVA_SINHALA, withNumbers)
									: transliterate(text, EngineType.ROMAN_DEVA, EngineType.DEVA_SINHALA, withNumbers);
						break;
					case DEVANAGARI:
						result = transliterate(text, EngineType.DEVA_SINHALA, withNumbers);
						break;
					case KHMER:
						result = transliterate(text, EngineType.KHMER_DEVA, EngineType.DEVA_SINHALA, withNumbers);
						break;
					case MYANMAR:
						result = transliterate(text, EngineType.MYANMAR_DEVA, EngineType.DEVA_SINHALA, withNumbers);
						break;
					case THAI:
						result = transliterate(text, EngineType.THAI_DEVA, EngineType.DEVA_SINHALA, withNumbers);
						break;
				}
				break;
			case THAI:
				switch(fromScript) {
					case ROMAN:
						result = asSanskrit
									? transliterate(text, EngineType.ROMAN_SKT_DEVA, EngineType.DEVA_THAI, withNumbers)
									: transliterate(text, EngineType.ROMAN_DEVA, EngineType.DEVA_THAI, withNumbers);
						break;
					case DEVANAGARI:
						result = transliterate(text, EngineType.DEVA_THAI, withNumbers);
						break;
					case KHMER:
						result = transliterate(text, EngineType.KHMER_DEVA, EngineType.DEVA_THAI, withNumbers);
						break;
					case MYANMAR:
						result = transliterate(text, EngineType.MYANMAR_DEVA, EngineType.DEVA_THAI, withNumbers);
						break;
					case SINHALA:
						result = transliterate(text, EngineType.SINHALA_DEVA, EngineType.DEVA_THAI, withNumbers);
						break;
				}
				break;
		}
		return result;
	}

	public static String translitQuickPali(final String text,
			final PaliScript fromScript, final PaliScript toScript,
			final EngineType romanDef, final boolean withNumbers) {
		return translitQuickPaliScript(text, fromScript, toScript, romanDef, withNumbers, false);
	}

	public static String translitQuickSanskrit(final String text,
			final PaliScript fromScript, final PaliScript toScript,
			final EngineType romanDef, final boolean withNumbers) {
		return translitQuickPaliScript(text, fromScript, toScript, romanDef, withNumbers, true);
	}

	public static String translitQuickPaliScript(final String text,
			final PaliScript fromScript, final PaliScript toScript,
			final EngineType romanDef, final boolean withNumbers, final boolean asSanskrit) {
		String result = text;
		switch (toScript) {
			case ROMAN:
				switch(fromScript) {
					case ROMAN:
						result = asSanskrit
								? transliterate(text, EngineType.ROMAN_SKT_DEVA, romanDef, withNumbers)
								: transliterate(text, EngineType.ROMAN_DEVA, romanDef, withNumbers);
						break;
					case DEVANAGARI:
						result = translitQuick(text, romanDef, withNumbers);
						break;
					case KHMER:
						result = translitQuick(text, EngineType.KHMER_DEVA, romanDef, withNumbers);
						break;
					case MYANMAR:
						result = translitQuick(text, EngineType.MYANMAR_DEVA, romanDef, withNumbers);
						break;
					case SINHALA:
						result = translitQuick(text, EngineType.SINHALA_DEVA, romanDef, withNumbers);
						break;
					case THAI:
						result = translitQuick(text, EngineType.THAI_DEVA, romanDef, withNumbers);
						break;
				}
				break;
			case DEVANAGARI:
				switch(fromScript) {
					case ROMAN:
						result = asSanskrit
								? translitQuick(text, EngineType.ROMAN_SKT_DEVA, withNumbers)
								: translitQuick(text, EngineType.ROMAN_DEVA, withNumbers);
						break;
					case KHMER:
						result = translitQuick(text, EngineType.KHMER_DEVA, withNumbers);
						break;
					case MYANMAR:
						result = translitQuick(text, EngineType.MYANMAR_DEVA, withNumbers);
						break;
					case SINHALA:
						result = translitQuick(text, EngineType.SINHALA_DEVA, withNumbers);
						break;
					case THAI:
						result = translitQuick(text, EngineType.THAI_DEVA, withNumbers);
						break;
				}
				break;
			case KHMER:
				switch(fromScript) {
					case ROMAN:
						result = asSanskrit
								? translitQuick(text, EngineType.ROMAN_SKT_DEVA, EngineType.DEVA_KHMER, withNumbers)
								: translitQuick(text, EngineType.ROMAN_DEVA, EngineType.DEVA_KHMER, withNumbers);
						break;
					case DEVANAGARI:
						result = translitQuick(text, EngineType.DEVA_KHMER, withNumbers);
						break;
					case MYANMAR:
						result = translitQuick(text, EngineType.MYANMAR_DEVA, EngineType.DEVA_KHMER, withNumbers);
						break;
					case SINHALA:
						result = translitQuick(text, EngineType.SINHALA_DEVA, EngineType.DEVA_KHMER, withNumbers);
						break;
					case THAI:
						result = translitQuick(text, EngineType.THAI_DEVA, EngineType.DEVA_KHMER, withNumbers);
						break;
				}
				break;
			case MYANMAR:
				switch(fromScript) {
					case ROMAN:
						result = asSanskrit
								? translitQuick(text, EngineType.ROMAN_SKT_DEVA, EngineType.DEVA_MYANMAR, withNumbers)
								: translitQuick(text, EngineType.ROMAN_DEVA, EngineType.DEVA_MYANMAR, withNumbers);
						break;
					case DEVANAGARI:
						result = translitQuick(text, EngineType.DEVA_MYANMAR, withNumbers);
						break;
					case KHMER:
						result = translitQuick(text, EngineType.KHMER_DEVA, EngineType.DEVA_MYANMAR, withNumbers);
						break;
					case SINHALA:
						result = translitQuick(text, EngineType.SINHALA_DEVA, EngineType.DEVA_MYANMAR, withNumbers);
						break;
					case THAI:
						result = translitQuick(text, EngineType.THAI_DEVA, EngineType.DEVA_MYANMAR, withNumbers);
						break;
				}
				break;
			case SINHALA:
				switch(fromScript) {
					case ROMAN:
						result = asSanskrit
								? translitQuick(text, EngineType.ROMAN_SKT_DEVA, EngineType.DEVA_SINHALA, withNumbers)
								: translitQuick(text, EngineType.ROMAN_DEVA, EngineType.DEVA_SINHALA, withNumbers);
						break;
					case DEVANAGARI:
						result = translitQuick(text, EngineType.DEVA_SINHALA, withNumbers);
						break;
					case KHMER:
						result = translitQuick(text, EngineType.KHMER_DEVA, EngineType.DEVA_SINHALA, withNumbers);
						break;
					case MYANMAR:
						result = translitQuick(text, EngineType.MYANMAR_DEVA, EngineType.DEVA_SINHALA, withNumbers);
						break;
					case THAI:
						result = translitQuick(text, EngineType.THAI_DEVA, EngineType.DEVA_SINHALA, withNumbers);
						break;
				}
				break;
			case THAI:
				switch(fromScript) {
					case ROMAN:
						result = asSanskrit
								? translitQuick(text, EngineType.ROMAN_SKT_DEVA, EngineType.DEVA_THAI, withNumbers)
								: translitQuick(text, EngineType.ROMAN_DEVA, EngineType.DEVA_THAI, withNumbers);
						break;
					case DEVANAGARI:
						result = translitQuick(text, EngineType.DEVA_THAI, withNumbers);
						break;
					case KHMER:
						result = translitQuick(text, EngineType.KHMER_DEVA, EngineType.DEVA_THAI, withNumbers);
						break;
					case MYANMAR:
						result = translitQuick(text, EngineType.MYANMAR_DEVA, EngineType.DEVA_THAI, withNumbers);
						break;
					case SINHALA:
						result = translitQuick(text, EngineType.SINHALA_DEVA, EngineType.DEVA_THAI, withNumbers);
						break;
				}
				break;
		}
		return result;
	}

}

