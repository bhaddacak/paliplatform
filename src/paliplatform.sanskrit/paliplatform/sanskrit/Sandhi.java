/*
 * Sandhi.java
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

import paliplatform.base.*;
import java.util.*;
import java.util.stream.*;

/**
 * The representation of a unit of two joined words.
 * @author J.R. Bhaddacak
 * @version 4.1
 * @since 4.1
 */
class Sandhi {
	private static char UNIT_SEP = '\u001F';
	private static char AVAGRAHA = '\u0315';
	private static String EMPTY = "\u2205";
	private static String sktVowelsNotA = "āiīuūṛṝḷeēoō";
	public static String sktVowels = "a" + sktVowelsNotA;
	private static String sktBeforeVisarga = "aāiīuūeēoō";
	public static String sktHasH = "kgcjṭḍtdpb";
	public static String[] availEndings = {
		"k", "ṭ", "t", "p", "ṅ", "m", "n", "aḥ", "āḥ",
		"iḥ", "īḥ", "uḥ", "ūḥ", "eḥ", "oḥ", "ēḥ", "ōḥ",
		"a", "ā", "i", "ī", "u", "ū", "ṛ", "e", "ē", "o", "ō"
	};
	public static List<String> availBeginnings = new ArrayList<>();
	private static Map<String, SandhiRule> sandhiRuleMap = new HashMap<>();
	private static Map<String, Set<String>> startDoubleRuleMap = new HashMap<>();
	private static Map<String, Set<String>> endDoubleRuleMap = new HashMap<>();
	private static Map<String, List<String>> vowelSandhiRuleMap = new HashMap<>();
	private String first;
	private String second;
	private List<String> productListRaw;

	public Sandhi(final String one, final String two) {
		first = normalize(one);
		second = normalize(two);
		productListRaw = combineRaw(first, second);
	}

	public static String normalize(final String term) {
		return term.toLowerCase().replace("ai", "ē").replace("au", "ō");
	}

	public static String restoreChar(final String term) {
		return term.replace("ē", "ai").replace("ō", "au");
	}

	public static Map<String, SandhiRule> getSandhiRuleMap() {
		return sandhiRuleMap;
	}

	public static boolean isVowel(final char ch) {
		return sktVowels.indexOf(ch) > -1;
	}

	public static boolean isVowel(final String ch) {
		return sktVowels.indexOf(ch) > -1;
	}

	public static String removeUnitSep(final String input) {
		return input.replace("" + UNIT_SEP, "");
	}

	public static String formatAvagraha(final String input) {
		return input.replace(AVAGRAHA, '’');
	}

	public static String convertAvagraha(final String input) {
		return input.replace('’', AVAGRAHA);
	}

	@Override
	public String toString() {
		return getFirst() + " + " + getSecond();
	}

	public String getFirst() {
		return restoreChar(first);
	}

	public String getSecond() {
		return restoreChar(second);
	}

	public String getFirstEnding() {
		return restoreChar(getEnding(first));
	}

	public String getSecondBeginning() {
		return restoreChar(getBeginning(second));
	}

	public List<String> getProductListRaw() {
		return productListRaw;
	}

	public List<String> getProductList() {
		return getProductListRoman();
	}

	public List<String> getProductListRoman() {
		return formatToRoman(productListRaw);
	}

	public List<String> getProductListDeva() {
		return formatToDeva(productListRaw);
	}

	public String getProduct() {
		return getProductRoman();
	}

	public String getProductRoman() {
		final List<String> romanList = getProductListRoman();
		return romanList.stream().collect(Collectors.joining(", "));
	}

	public String getProductDeva() {
		final List<String> devaList = getProductListDeva();
		return devaList.stream().collect(Collectors.joining(", "));
	}

	private static String getBeginning(final String word) {
		if (word.isEmpty()) return "";
		final int len = word.length();
		final char start = word.charAt(0);
		final String result;
		if (len > 1) {
			final char next = word.charAt(1);
			result = sktHasH.indexOf(start) > -1
					? next == 'h'
						? "" + start + next
						: "" + start
					: "" + start;
		} else {
			result = "" + start;
		}
		return result;
	}

	private static String getEnding(final String word) {
		if (word.isEmpty()) return "";
		final int len = word.length();
		final char end = word.charAt(len - 1);
		final String result;
		if (len > 1) {
			final char preEnd = word.charAt(len - 2);
			if (end == 'h') {
				result = isVowel(preEnd)
							? "" + end
							: sktHasH.indexOf(preEnd) > -1
								? "" + preEnd + end
								: "" + end;
			} else if (end == 'ḥ') {
				result = sktBeforeVisarga.indexOf(preEnd) > -1
						? "" + preEnd + end
						: "" + end;
			} else {
				result = "" + end;
			}
		} else {
			result = "" + end;
		}
		return result;
	}

	private static List<String> combineRaw(final String one, final String two) {
		final String firstEnding = getEnding(one);
		final String firstRest = one.substring(0, one.length() - firstEnding.length());
		final String secondBeginning = getBeginning(two);
		final String secondRest = two.substring(secondBeginning.length());
		final String secondBeginningOK = secondBeginning.isEmpty() ? EMPTY : secondBeginning;
		final SandhiRule rule = sandhiRuleMap.getOrDefault(secondBeginningOK, new SandhiRule(secondBeginningOK));
		final List<String> resList = rule.getProduct(firstEnding);
		// for double-rules
		final int diff = one.length() - firstEnding.length();
		final String keyEnd = diff > 0
								? one.substring(diff - 1, diff) // the letter before end
								: "";
		final String last = firstEnding.substring(firstEnding.length() - 1);
		final String keyStart = isVowel(last)
								? last
								: "";
		final List<String> immResList = applyStartDoubleRules(keyStart, applyEndDoubleRules(keyEnd, resList));
		final List<String> finResList = isVowel(firstEnding) && isVowel(secondBeginning)
											? applyVowelSandhi(immResList)
											: immResList;
		final List<String> result = new ArrayList<>();
		for (final String res : finResList) {
			final String item = firstRest + res + secondRest;
			result.add(item);
		}
		return result;
	}

	public static List<String> formatToRoman(final List<String> rawResList) {
		final List<String> result = new ArrayList<>();
		for (final String r : rawResList) {
			final String item = r.replace("" + UNIT_SEP, " ").replace("" + AVAGRAHA, "’");
			result.add(restoreChar(item));
		}
		return result;
	}

	public static List<String> formatToDeva(final List<String> rawResList) {
		final List<String> result = new ArrayList<>();
		for (final String res : rawResList) {
			final String item = ScriptTransliterator.translitQuick(restoreChar(removeUnitSep(res)),
								ScriptTransliterator.EngineType.ROMAN_SKT_DEVA, false);
			result.add(item);
		}
		return result;
	}

	private static List<String> applyEndDoubleRules(final String key, final List<String> resList) {
		final Set<String> endRuleSet = endDoubleRuleMap.get(key);
		final List<String> result = new ArrayList<>();
		for (String res : resList) {
			final String[] parts = res.split("" + UNIT_SEP);
			if (parts.length < 2 || parts[1].isEmpty()) {
				result.add(res);
				continue;
			}
			final String lastChar = parts[0].substring(parts[0].length() - 1);
			final String firstPart = endRuleSet != null && endRuleSet.contains(lastChar) && isVowel(parts[1].charAt(0))
									? parts[0] + lastChar
									: parts[0];
			result.add(firstPart + UNIT_SEP + parts[1]);
		}
		return result;
	}

	private static List<String> applyStartDoubleRules(final String key, final List<String> resList) {
		final Set<String> startRuleSet = startDoubleRuleMap.get(key);
		final List<String> result = new ArrayList<>();
		for (String res : resList) {
			final String[] parts = res.split("" + UNIT_SEP);
			if (parts.length < 2 || parts[1].isEmpty()) {
				result.add(res);
				continue;
			}
			final String secondPart = startRuleSet != null && parts[1].length() > 1 && startRuleSet.contains(parts[1].substring(0, 2))
									? parts[1].charAt(0) + parts[1]
									: parts[1];
			result.add(parts[0] + UNIT_SEP + secondPart);
		}
		return result;
	}

	private static List<String> applyVowelSandhi(final List<String> resList) {
		final List<String> result = new ArrayList<>();
		for (String res : resList) {
			final String[] parts = res.split("" + UNIT_SEP);
			if (parts.length < 2 || parts[1].isEmpty()) {
				result.add(res);
				continue;
			}
			final char firstEnd = parts[0].charAt(parts[0].length() - 1);
			final char secondStart = parts[1].charAt(0);
			if (isVowel(firstEnd) && isVowel(secondStart)) {
				final List<String> rules = vowelSandhiRuleMap.get("" + secondStart);
				if (rules != null) {
					final int index =  firstEnd == 'i' || firstEnd == 'ī' ? 1
										: firstEnd == 'u' || firstEnd == 'ū' ? 2
										: firstEnd == 'ṛ' ? 3
										: firstEnd == 'ō' ? 4
										: firstEnd == 'ē' ? 5
										: firstEnd == 'e' ? 6
										: firstEnd == 'o' ? 7
										: 0;
					final String prod = rules.get(index);
					result.add(parts[0].substring(0, parts[0].length() - 1) + prod + parts[1].substring(1));
				} else {
					result.add(res);
				}
			} else {
				result.add(res);
			}
		}
		return result;
	}

	static {
		for (final String start : new String[] { EMPTY, "k", "kh", "p", "ph", "ṣ", "s", "ś" }) {
			if (!start.equals(EMPTY))
				availBeginnings.add(start);
			final SandhiRule rule = new SandhiRule(start);
			for (final String end : availEndings) {
				final List<String> prodList = new ArrayList<>();
				final String endChanged = end.equals("t") ? "c"
										: end.equals("m") ? "ṃ"
										: end.equals("n") && start.equals("ś") ? "ñ"
										: end;
				final String startChanged = start.equals("ś") && end.equals("t") ? "ch" : start;
				prodList.add(endChanged + UNIT_SEP + startChanged);
				if ((start.equals("ṣ") || start.equals("s") || start.equals("ś")) && end.endsWith("ḥ"))
					prodList.add(end.substring(0, end.length()-1) + start + UNIT_SEP + start);
				if (start.equals("ś") && end.equals("n")) {
					prodList.add("ñ" + UNIT_SEP + "ch");
				}
				rule.addRule(end, prodList);
			}
			sandhiRuleMap.put(start, rule);
		}
		for (final String start : new String[] { "c", "ch", "ṭ", "ṭh", "t", "th" }) {
			availBeginnings.add(start);
			final SandhiRule rule = new SandhiRule(start);
			for (final String end : availEndings) {
				final List<String> prodList = new ArrayList<>();
				final String sub = start.replace("c", "ś").replace("ṭ", "ṣ").replace("t", "s").charAt(0) + "";
				final String endChanged = end.equals("t") && start.startsWith("c") ? "c"
										: end.equals("t") && start.startsWith("ṭ") ? "ṭ"
										: end.equals("m") ? "ṃ"
										: end.equals("n") ? "ṃ" + sub
										: end.endsWith("ḥ") ? end.replace("ḥ", sub)
										: end;
				prodList.add(endChanged + UNIT_SEP + start);
				rule.addRule(end, prodList);
			}
			sandhiRuleMap.put(start, rule);
		}
		for (final String start : new String[] { "r", "g", "gh", "d", "dh", "b", "bh", "y", "v", 
												"j", "jh", "ḍ", "ḍh", "l", "h", "n", "m", 
												"a", "ā", "i", "ī", "u", "ū", "ṛ", "e", "ē", "o", "ō" }) {
			availBeginnings.add(start);
			final SandhiRule rule = new SandhiRule(start);
			for (final String end : availEndings) {
				final List<String> prodList = new ArrayList<>();
				final String endChanged = end.equals("k") && (start.equals("n") || start.equals("m")) ? "ṅ"
										: end.equals("k") ? "g"
										: end.equals("ṭ") && (start.equals("n") || start.equals("m")) ? "ṇ"
										: end.equals("ṭ") ? "ḍ"
										: end.equals("t") && start.startsWith("j") ? "j"
										: end.equals("t") && start.startsWith("ḍ") ? "ḍ"
										: end.equals("t") && start.equals("l") ? "l"
										: end.equals("t") &&  (start.equals("n") || start.equals("m")) ? "n"
										: end.equals("t") ? "d"
										: end.equals("p") &&  (start.equals("n") || start.equals("m")) ? "m"
										: end.equals("p") ? "b"
										: end.equals("m") && !isVowel(start) ? "ṃ"
										: end.equals("n") && start.startsWith("j") ? "ñ"
										: end.equals("n") && start.startsWith("ḍ") ? "ṇ"
										: end.equals("n") && start.equals("l") ? "ṃ"
										: end.equals("aḥ") && sktVowelsNotA.indexOf(start) > -1 ? "a"
										: end.equals("aḥ") ? "o"
										: end.equals("āḥ") ? "ā"
										: end.endsWith("ḥ") && start.equals("r") ? end.replace("ḥ", "").replace("i", "ī").replace("u", "ū")
										: end.endsWith("ḥ") ? end.replace("ḥ", "r")
										: end;
				final String startChanged = start.equals("h") && "gḍdb".indexOf(endChanged) > -1 ? endChanged + "h"
											:start.equals("a") && "o".equals(endChanged) ? "" + AVAGRAHA
											: start;
				prodList.add(endChanged + UNIT_SEP + startChanged);
				if (end.equals("n") && start.equals("l"))
					prodList.add("l\u0310" + UNIT_SEP + "l");
				rule.addRule(end, prodList);
			}
			sandhiRuleMap.put(start, rule);
		}
		// 6 Word-final -n or -ṅ, if preceded by a short vowel, is doubled before a following vowel; e.g. -in e- -> -inn e-. 
		// 8 C denotes any consonant. When the preceding word ends in a short vowel, ch- -> cch-.
		final Set<String> nSet = Set.of("ṅ", "n");
		final Set<String> kSet = Set.of("kh", "gh", "ch", "jh", "ṭh", "ḍh", "th", "dh", "ph", "bh");
		endDoubleRuleMap.put("a", nSet);
		endDoubleRuleMap.put("i", nSet);
		endDoubleRuleMap.put("u", nSet);
		endDoubleRuleMap.put("ṛ", nSet);
		endDoubleRuleMap.put("ḷ", nSet);
		startDoubleRuleMap.put("a", kSet);
		startDoubleRuleMap.put("i", kSet);
		startDoubleRuleMap.put("u", kSet);
		startDoubleRuleMap.put("ṛ", kSet);
		startDoubleRuleMap.put("ḷ", kSet);
		// vowel sandhi rules for, 0:-a/-ā, 1:-i/-ī, 2:-u/-ū, 3:-ṛ, 4:-au, 5:-ai, 6:-e, 7:-o
		// in -e/-o + i-/u-, to prevent collapsing to ai and au, a'i and a'u are used
		vowelSandhiRuleMap.put("a", List.of("ā", "ya", "va", "ra", "āva", "ā" + UNIT_SEP + "a", "e" + UNIT_SEP + AVAGRAHA, "o" + UNIT_SEP + AVAGRAHA));
		vowelSandhiRuleMap.put("ā", List.of("ā", "yā", "vā", "rā", "āvā", "ā" + UNIT_SEP + "ā", "a" + UNIT_SEP + "ā", "a" + UNIT_SEP + "ā"));
		vowelSandhiRuleMap.put("i", List.of("e", "ī", "vi", "ri", "āvi", "ā" + UNIT_SEP + "i", "a" + UNIT_SEP + "'i", "a" + UNIT_SEP + "'i"));
		vowelSandhiRuleMap.put("ī", List.of("e", "ī", "vī", "rī", "āvī", "ā" + UNIT_SEP + "ī", "a" + UNIT_SEP + "ī", "a" + UNIT_SEP + "ī"));
		vowelSandhiRuleMap.put("u", List.of("o", "yu", "ū", "ru", "āvu", "ā" + UNIT_SEP + "u", "a" + UNIT_SEP + "'u", "a" + UNIT_SEP + "'u"));
		vowelSandhiRuleMap.put("ū", List.of("o", "yū", "ū", "rū", "āvū", "ā" + UNIT_SEP + "ū", "a" + UNIT_SEP + "ū", "a" + UNIT_SEP + "ū"));
		vowelSandhiRuleMap.put("ṛ", List.of("ar", "yṛ", "vṛ", "ṝ", "āvṛ", "ā" + UNIT_SEP + "ṛ", "a" + UNIT_SEP + "ṛ", "a" + UNIT_SEP + "ṛ"));
		vowelSandhiRuleMap.put("e", List.of("ai", "ye", "ve", "re", "āve", "ā" + UNIT_SEP + "e", "a" + UNIT_SEP + "e", "a" + UNIT_SEP + "e"));
		vowelSandhiRuleMap.put("ē", List.of("ē", "yē", "vē", "rē", "āvē", "ā" + UNIT_SEP + "ē", "a" + UNIT_SEP + "ē", "a" + UNIT_SEP + "ē"));
		vowelSandhiRuleMap.put("o", List.of("ō", "yo", "vo", "ro", "āvo", "ā" + UNIT_SEP + "o", "a" + UNIT_SEP + "o", "a" + UNIT_SEP + "o"));
		vowelSandhiRuleMap.put("ō", List.of("ō", "yō", "vō", "rō", "āvō", "ā" + UNIT_SEP + "ō", "a" + UNIT_SEP + "ō", "a" + UNIT_SEP + "ō"));
		Collections.sort(availBeginnings, Utilities.sktComparator);
	}

	// inner class
	static class SandhiRule {
		final String secondStart;
		final Map<String, List<String>> ruleMap = new HashMap<>();

		public SandhiRule(final String start) {
			secondStart = start;
		}

		public void addRule(final String end, final List<String> rules) {
			ruleMap.put(end, rules);
		}

		public List<String> getProduct(final String end) {
			final List<String> result = new ArrayList<>();
			final List<String> rules = ruleMap.getOrDefault(end, new ArrayList<>());
			if (rules.isEmpty())
				rules.add(end + UNIT_SEP + secondStart);
			for (final String r : rules) {
				result.add(r.replace(EMPTY, ""));
			}
			return result;
		}

	}

}
