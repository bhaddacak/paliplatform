/*
 * SktUtil.java
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
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see https://www.gnu.org/licenses/.
 */

package paliplatform.sanskrit;

import paliplatform.base.*;

import java.util.*;
import java.util.stream.*;

import static org.hamcrest.MatcherAssert.assertThat; 
import static org.hamcrest.Matchers.*;

/** 
 * The CLI tool for Sanskrit module.
 * This consists of static factory methods.
 * The tool can be invoked by this command line from the program's root dir:
 * $ java -p modules -m paliplatform.dpd/paliplatform.dpd.SktUtil
 *
 * @author J.R. Bhaddacak
 * @version 4.1
 * @since 4.1
 */
final public class SktUtil {
	private static final String LINESEP = System.getProperty("line.separator");

	private SktUtil() {
	}

	public static void main(final String[] args) throws Exception {
		if (args.length == 0) {
			printHelpAndExit();
		}
		String opt = "";
		final String[] param = { "", "" };
		switch (args[0]) {
			case "test":
				ScriptTransliterator.initializeTransliterator();
				opt = args.length > 1 ? args[1] : "";
				param[0] = args.length > 2 ? args[2] : "";
				param[1] = args.length > 3 ? args[3] : "";
				if (opt.equals("-s") && !param[0].isEmpty() && !param[1].isEmpty()) {
					testSandhi(param);
				} else if (opt.equals("-sa")) {
					testSandhiAuto();
				} else if (opt.equals("-sf")) {
					testSandhiFull();
				} else {
					printHelpAndExit();
				}
				break;
			default:
				printHelpAndExit();
		}
	}

	private static void printHelpAndExit() {
		final StringBuilder help = new StringBuilder();
		help.append(LINESEP).append("Pāli Platform DPD Util CLI").append(LINESEP);
		help.append("  Usage:").append(LINESEP);
		help.append("    SktUtil [<command>] <option>").append(LINESEP);
		help.append("      (The abstract SktUtil can be a launcher script,").append(LINESEP);
		help.append("       such as sktutil.sh or sktutil.cmd which can be").append(LINESEP);
		help.append("       found in the program's root directory.").append(LINESEP);
		help.append("       See also Notes below.)").append(LINESEP);
		help.append("  Commands:").append(LINESEP);
		help.append("    test\tTest cases").append(LINESEP);
		help.append("        -s <word1> <word2>\tSandhi test of word1 + word2").append(LINESEP);
		help.append("        -sa\tAutomatic sandhi test").append(LINESEP);
		help.append("        -sf\tFull sandhi list").append(LINESEP);
		help.append("    <none>\tShow this help").append(LINESEP);
		help.append("  Notes:").append(LINESEP);
		help.append("    To invoke the program, the Java convention has to be used.").append(LINESEP);
		help.append("    At the program's root directory, if no launcher script available,").append(LINESEP);
		help.append("    type this at the console: ").append(LINESEP);
		help.append("    $ java -p modules -m paliplatform.sanskrit/paliplatform.sanskrit.SktUtil").append(LINESEP);
		printLog(help.toString());
		System.exit(0);
	}

	private static void printLog(final String mess) {
		System.out.println(mess);
	}

	private static void printTime(final long msec) {
		printLog(String.format("Done in %.3f seconds", msec/1000.0));
	}

	private static void testSandhi(final String[] param) {
		final Sandhi sandhi = new Sandhi(param[0], param[1]);
		System.out.println(param[0] + " + " + param[1] + " = " +
				sandhi.getProductRoman() + " (" + sandhi.getProductDeva() + ")");
	}

	private static void testSandhiAuto() {
		final Map<String, Sandhi.SandhiRule> ruleMap = Sandhi.getSandhiRuleMap();
		final String[] firstTerms = {
			"-k", "-ṭ", "-t", "-p", "-ṅ", "-m", "-n",
			"-aḥ", "-āḥ", "-iḥ", "-īḥ", "-uḥ", "-ūḥ", "-eḥ", "-oḥ", "-aiḥ", "-auḥ"
		};
		final String[] secondTerms = {
			"k-", "kh-", "p-", "ph-", "ṣ-", "s-"
		};
		// ending and starting cutting test
		for (final String f : firstTerms) {
			for (final String s : secondTerms) {
				final Sandhi sandhi = new Sandhi(f, s);
				final String firstEnding = sandhi.getFirstEnding();
				final String secondBeginning = sandhi.getSecondBeginning();
				assertThat(f.substring(1), equalTo(firstEnding));
				assertThat(s.substring(0, s.length()-1), equalTo(secondBeginning));
			}
		}
		// selected notable cases
		final List<SandhiTestCase> cases = new ArrayList<>();
		cases.add(new SandhiTestCase("-t", "ś-", List.of("-c ch-")));
		cases.add(new SandhiTestCase("-n", "ś-", List.of("-ñ ś-", "-ñ ch-")));
		cases.add(new SandhiTestCase("-t", "c-", List.of("-c c-")));
		cases.add(new SandhiTestCase("-t", "ch-", List.of("-c ch-")));
		cases.add(new SandhiTestCase("-n", "c-", List.of("-ṃś c-")));
		cases.add(new SandhiTestCase("-n", "ṭ-", List.of("-ṃṣ ṭ-")));
		cases.add(new SandhiTestCase("-n", "t-", List.of("-ṃs t-")));
		cases.add(new SandhiTestCase("-aḥ", "ś-", List.of("-aḥ ś-", "-aś ś-")));
		cases.add(new SandhiTestCase("-aḥ", "ṣ-", List.of("-aḥ ṣ-", "-aṣ ṣ-")));
		cases.add(new SandhiTestCase("-aḥ", "s-", List.of("-aḥ s-", "-as s-")));
		cases.add(new SandhiTestCase("-aḥ", "c-", List.of("-aś c-")));
		cases.add(new SandhiTestCase("-aḥ", "ṭ-", List.of("-aṣ ṭ-")));
		cases.add(new SandhiTestCase("-aḥ", "t-", List.of("-as t-")));
		cases.add(new SandhiTestCase("-k", "r-", List.of("-g r-")));
		cases.add(new SandhiTestCase("-ṭ", "r-", List.of("-ḍ r-")));
		cases.add(new SandhiTestCase("-t", "r-", List.of("-d r-")));
		cases.add(new SandhiTestCase("-p", "r-", List.of("-b r-")));
		cases.add(new SandhiTestCase("-ṅ", "r-", List.of("-ṅ r-")));
		cases.add(new SandhiTestCase("-m", "r-", List.of("-ṃ r-")));
		cases.add(new SandhiTestCase("-n", "r-", List.of("-n r-")));
		cases.add(new SandhiTestCase("-aḥ", "r-", List.of("-o r-")));
		cases.add(new SandhiTestCase("-āḥ", "r-", List.of("-ā r-")));
		cases.add(new SandhiTestCase("-iḥ", "r-", List.of("-ī r-")));
		cases.add(new SandhiTestCase("-īḥ", "r-", List.of("-ī r-")));
		cases.add(new SandhiTestCase("-iḥ", "g-", List.of("-ir g-")));
		cases.add(new SandhiTestCase("-īḥ", "g-", List.of("-īr g-")));
		cases.add(new SandhiTestCase("-t", "j-", List.of("-j j-")));
		cases.add(new SandhiTestCase("-n", "j-", List.of("-ñ j-")));
		cases.add(new SandhiTestCase("-t", "ḍ-", List.of("-ḍ ḍ-")));
		cases.add(new SandhiTestCase("-n", "ḍ-", List.of("-ṇ ḍ-")));
		cases.add(new SandhiTestCase("-t", "l-", List.of("-l l-")));
		cases.add(new SandhiTestCase("-n", "l-", List.of("-ṃ l-", "-l\u0310 l-"))); // with candrabindu
		cases.add(new SandhiTestCase("-k", "h-", List.of("-g gh-")));
		cases.add(new SandhiTestCase("-ṭ", "h-", List.of("-ḍ ḍh-")));
		cases.add(new SandhiTestCase("-t", "h-", List.of("-d dh-")));
		cases.add(new SandhiTestCase("-p", "h-", List.of("-b bh-")));
		cases.add(new SandhiTestCase("-k", "n-", List.of("-ṅ n-")));
		cases.add(new SandhiTestCase("-ṭ", "n-", List.of("-ṇ n-")));
		cases.add(new SandhiTestCase("-t", "n-", List.of("-n n-")));
		cases.add(new SandhiTestCase("-p", "n-", List.of("-m n-")));
		cases.add(new SandhiTestCase("-aṅ", "a-", List.of("-aṅṅ a-")));
		cases.add(new SandhiTestCase("-āṅ", "a-", List.of("-āṅ a-")));
		cases.add(new SandhiTestCase("-an", "a-", List.of("-ann a-")));
		cases.add(new SandhiTestCase("-ān", "a-", List.of("-ān a-")));
		cases.add(new SandhiTestCase("-aḥ", "a-", List.of("-o ’-")));
		cases.add(new SandhiTestCase("-a", "a-", List.of("-ā-")));
		cases.add(new SandhiTestCase("-i", "a-", List.of("-ya-")));
		cases.add(new SandhiTestCase("-u", "a-", List.of("-va-")));
		cases.add(new SandhiTestCase("-ṛ", "a-", List.of("-ra-")));
		cases.add(new SandhiTestCase("-au", "a-", List.of("-āva-")));
		cases.add(new SandhiTestCase("-ai", "a-", List.of("-ā a-")));
		cases.add(new SandhiTestCase("-e", "a-", List.of("-e ’-")));
		cases.add(new SandhiTestCase("-o", "a-", List.of("-o ’-")));
		cases.add(new SandhiTestCase("-a", "i-", List.of("-e-")));
		cases.add(new SandhiTestCase("-i", "i-", List.of("-ī-")));
		cases.add(new SandhiTestCase("-u", "i-", List.of("-vi-")));
		cases.add(new SandhiTestCase("-ṛ", "i-", List.of("-ri-")));
		cases.add(new SandhiTestCase("-au", "i-", List.of("-āvi-")));
		cases.add(new SandhiTestCase("-ai", "i-", List.of("-ā i-")));
		cases.add(new SandhiTestCase("-e", "i-", List.of("-a 'i-"))); // prevent collapse to ai, a'i is used
		cases.add(new SandhiTestCase("-o", "i-", List.of("-a 'i-")));
		cases.add(new SandhiTestCase("-a", "u-", List.of("-o-")));
		cases.add(new SandhiTestCase("-i", "u-", List.of("-yu-")));
		cases.add(new SandhiTestCase("-u", "u-", List.of("-ū-")));
		cases.add(new SandhiTestCase("-ṛ", "u-", List.of("-ru-")));
		cases.add(new SandhiTestCase("-au", "u-", List.of("-āvu-")));
		cases.add(new SandhiTestCase("-ai", "u-", List.of("-ā u-")));
		cases.add(new SandhiTestCase("-e", "u-", List.of("-a 'u-"))); // prevent collapse to au, a'u is used
		cases.add(new SandhiTestCase("-o", "u-", List.of("-a 'u-")));
		for (final SandhiTestCase c : cases) {
			final List<String> expected = c.getExpectedProductList();
			final List<String> product = c.getProductList();
			assertThat(expected.size(), equalTo(product.size()));
			for (int i = 0; i < expected.size(); i++) {
				assertThat(expected.get(i), equalTo(product.get(i)));
			}
		}
	}

	private static void testSandhiFull() {
		final String[] firstTerms = {
			"katak", "kataṭ", "katat", "katap", "kataṅ", "katāṅ", "katam", "katin", "katīn",
			"kataḥ", "katāḥ", "katiḥ", "katīḥ", "katuḥ", "katūḥ", "kateḥ", "katoḥ", "kataiḥ", "katauḥ",
			"kata", "katā", "kati", "katī", "katu", "katū", "katṛ", "kate", "kato", "katai", "katau",
			"katag", // odd endings
		};
		final String[] secondTerms = {
			"kataḥ", "khataḥ", "pataḥ", "phataḥ", "ṣataḥ", "sataḥ", "śataḥ",
			"cataḥ", "chataḥ", "ṭataḥ", "ṭhataḥ", "tataḥ", "thataḥ",
			"rataḥ", "gataḥ", "ghataḥ", "dataḥ", "dhataḥ", "bataḥ", "bhataḥ", "yataḥ", "vataḥ", 
			"jataḥ", "jhataḥ", "ḍataḥ", "ḍhataḥ", "lataḥ", "hataḥ", "nataḥ", "mataḥ", 
			"ataḥ", "ātaḥ", "itaḥ", "ītaḥ", "utaḥ", "ūtaḥ", "ṛtaḥ", "etaḥ", "aitaḥ", "otaḥ", "autaḥ",
			"ḥtaḥ", "ṃtaḥ", "ṅtaḥ", "ñtaḥ", "ṇtaḥ", "ṝtaḥ", "ḷtaḥ", "ḹtaḥ", // odd starts
			"" // empty start
		};
		for (final String f : firstTerms) {
			for (final String s : secondTerms) {
				final Sandhi sandhi = new Sandhi(f, s);
				final String firstEnding = sandhi.getFirstEnding();
				final String secondBeginning = sandhi.getSecondBeginning();
				System.out.println(f + " + " + s + " (" +
						firstEnding + " + " + secondBeginning + ") = " +
						sandhi.getProductRoman() + " (" + sandhi.getProductDeva() + ")");
			}
		}
	}

	// inner classes
	static class SandhiTestCase {
		private String first;
		private String second;
		private List<String> expectedProduct;
		public SandhiTestCase(final String f, final String s, final List<String> p) {
			first = f;
			second = s;
			expectedProduct = p;
		}
		public List<String> getExpectedProductList() {
			return expectedProduct;
		}
		public List<String> getProductList() {
			final Sandhi res = new Sandhi(first, second);
			return res.getProductList();
		}

	}

}

