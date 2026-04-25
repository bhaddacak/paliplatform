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
import static paliplatform.sanskrit.SktConjugationWin.VForm;
import static paliplatform.sanskrit.SktConjugation.TenseMood;
import static paliplatform.sanskrit.SktConjugation.Pada;
import static paliplatform.sanskrit.SktConjugation.Person;
import static paliplatform.sanskrit.SktConjugation.Number;

import java.util.*;
import java.util.stream.*;
import java.io.*;
import java.nio.file.*;
import java.nio.charset.StandardCharsets;

import static org.hamcrest.MatcherAssert.assertThat; 
import static org.hamcrest.Matchers.*;

/** 
 * The CLI tool for Sanskrit module.
 * This consists of static factory methods.
 * The tool can be invoked by this command line from the program's root dir:
 * $ java -p modules -m paliplatform.sanskrit/paliplatform.sanskrit.SktUtil
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
		ScriptTransliterator.initializeTransliterator();
		String opt = "";
		final String[] param = { "", "" };
		switch (args[0]) {
			case "list":
				opt = args.length > 1 ? args[1] : "";
				if (opt.equals("-d")) {
					listDeclensionParadigms();
				} else if (opt.equals("-c")) {
					listConjugationParadigms();
				} else if (opt.equals("-v")) {
					listVerbs();
				} else {
					printHelpAndExit();
				}
				break;
			case "show":
				opt = args.length > 1 ? args[1] : "";
				param[0] = args.length > 2 ? args[2] : "";
				param[1] = args.length > 3 ? args[3] : "";
				if (opt.equals("-s") && !param[0].isEmpty() && !param[1].isEmpty()) {
					showExternalSandhi(param);
				} else if (opt.equals("-si") && !param[0].isEmpty() && !param[1].isEmpty()) {
					showInternalSandhi(param);
				} else if (opt.equals("-d") && !param[0].isEmpty()) {
					showDeclension(param);
				} else {
					printHelpAndExit();
				}
				break;
			case "save":
				Utilities.initializeSktDictDB(false);
				Utilities.initializeComparator();
				opt = args.length > 1 ? args[1] : "";
				param[0] = args.length > 2 ? args[2] : "";
				if (opt.equals("-mw") && !param[0].isEmpty()) {
					saveMWTerms(param);
				} else if (opt.equals("-o") && !param[0].isEmpty()) {
					sortAndSave(param);
				} else if (opt.equals("-cf")) {
					saveConjugationProducts();
				} else {
					printHelpAndExit();
				}
				break;
			case "test":
				opt = args.length > 1 ? args[1] : "";
				if (opt.equals("-sa")) {
					testSandhiAuto();
				} else if (opt.equals("-si")) {
					testInternalSandhi();
				} else if (opt.equals("-sf")) {
					testSandhiFull();
				} else if (opt.equals("-da")) {
					testDeclensionAuto();
				} else if (opt.equals("-aa")) {
					testAugmentAuto();
				} else if (opt.equals("-x")) {
					testMisc();
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
		help.append(LINESEP).append("Pāli Platform Sanskrit Util CLI").append(LINESEP);
		help.append("  Usage:").append(LINESEP);
		help.append("    SktUtil [<command>] <option>").append(LINESEP);
		help.append("      (The abstract SktUtil can be a launcher script,").append(LINESEP);
		help.append("       such as sktutil.sh or sktutil.cmd which can be").append(LINESEP);
		help.append("       found in the program's root directory.").append(LINESEP);
		help.append("       See also Notes below.)").append(LINESEP);
		help.append("  Commands:").append(LINESEP);
		help.append("    list\tList things").append(LINESEP);
		help.append("        -d\tList all declensional paradigms").append(LINESEP);
		help.append("        -c\tList all conjugational paradigms").append(LINESEP);
		help.append("        -v\tList all verbs").append(LINESEP);
		help.append("    show\tShow things").append(LINESEP);
		help.append("        -d <paradigm> [<stem>]\tShow declensions of a paradigm [with a stem]").append(LINESEP);
		help.append("        -s <word1> <word2>\tShow external sandhi of word1 + word2").append(LINESEP);
		help.append("        -si <stem> <suffix>\tShow internal sandhi of stem + suffix").append(LINESEP);
		help.append("    save\tSave data").append(LINESEP);
		help.append("        -mw <condition>\tSave MW terms on the <condition>").append(LINESEP);
		help.append("        -o <file>\tSort and save <file>").append(LINESEP);
		help.append("        -cf\tSave all conjugational products").append(LINESEP);
		help.append("    test\tTest cases").append(LINESEP);
		help.append("        -sa\tAutomatic external sandhi test").append(LINESEP);
		help.append("        -si\tAutomatic Internal sandhi test").append(LINESEP);
		help.append("        -sf\tFull external sandhi list").append(LINESEP);
		help.append("        -da\tAutomatic declension test").append(LINESEP);
		help.append("        -aa\tAutomatic augment test").append(LINESEP);
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

	private static void listDeclensionParadigms() {
		int count = 0;
		for (final String pname : SktDeclension.paradigmMap.keySet()) {
			final NominalParadigm parad = SktDeclension.paradigmMap.get(pname);
			final String bucknell = parad.getBucknellNumber();
			final String bucknellStr = !bucknell.isEmpty() ? " [" + bucknell + "]": "";
			final String gendStr = parad.getGenderStr();
			final Map<SktDeclension.Case, Map<SktDeclension.Number, List<String>>> prod = parad.getSampleProduct();
			final Map<SktDeclension.Number, List<String>> numMap = prod.get(SktDeclension.Case.NOM);
			final List<String> decl = new ArrayList<>();
			for (final SktDeclension.Number n : SktDeclension.Number.values) {
				final List<String> tlist = numMap.get(n);
				final String term = tlist.get(0);
				decl.add(term.isEmpty() ? "-" : term);
			}
			final String result = pname + bucknellStr + " " + gendStr + " => " + decl.stream().collect(Collectors.joining(":"));
			System.out.println(result);
			count++;
		}
		System.out.println(count + " items listed");
	}

	private static void listConjugationParadigms() {
		int count = 0;
		for (final String pname : SktConjugation.paradigmMap.keySet()) {
			final VerbalParadigm parad = SktConjugation.paradigmMap.get(pname);
			final String bucknell = parad.getBucknellNumber();
			final String bucknellStr = !bucknell.isEmpty() ? " [" + bucknell + "]": "";
			final TenseMood tenseMood = parad.getTenseMood();
			final Pada pada = parad.getPada();
			final Map<Person, Map<Number, List<String>>> prod = parad.getSampleProduct();
			final Map<Number, List<String>> numMap = prod.get(Person.PRATHAMA);
			final List<String> conjug = new ArrayList<>();
			for (final Number n : Number.values) {
				final List<String> tlist = numMap.getOrDefault(n, List.of(""));
				final String term = tlist.get(0);
				conjug.add(term.isEmpty() ? "-" : term);
			}
			final String result = pname + bucknellStr + " " + tenseMood + " " + pada + " => " + conjug.stream().collect(Collectors.joining(":"));
			System.out.println(result);
			count++;
		}
		System.out.println(count + " items listed");
	}

	private static void listVerbs() {
		int count = 0;
		for (final SktVerb verb : VerbRepo.sktVerbMap.values()) {
			final String refNum = verb.getBucknellNumberStr();
			final String verbStr = verb.getVerbAndRoot();
			final Pada pad = verb.isActiveNormal() ? Pada.ACT : Pada.MID;
			final String paradName = SktConjugation.getParadigmName(verb.getCitationForm(), TenseMood.PRES, pad);
			final String result = String.format("%4s %s: %s", refNum, verbStr, paradName);
			System.out.println(result);
			count++;
		}
		System.out.println(count + " items listed");
	}

	private static void showExternalSandhi(final String[] param) {
		final Sandhi sandhi = new Sandhi(param[0], param[1]);
		System.out.println(param[0] + " + " + param[1] + " = " +
				sandhi.getProductRoman() + " (" + sandhi.getProductDeva() + ")");
	}

	private static void showInternalSandhi(final String[] param) {
		final String result = Sandhi.applyInternalSandhi(param[0], param[1]);
		final String resultDeva = ScriptTransliterator.translitQuick(result, ScriptTransliterator.EngineType.ROMAN_SKT_DEVA, false);
		System.out.println(param[0] + " + " + param[1] + " = " + result + " (" + resultDeva + ")");
	}

	private static void showDeclension(final String[] args) {
		final String pname = args[0];
		final String stem = args.length > 1 ? args[1] : "";
		final String exactName = SktDeclension.paradigmMap.keySet().stream()
									.filter(x -> x.startsWith(pname))
									.findFirst()
									.orElse(null);
		if (exactName == null) {
			System.out.println("No paradigm like '" + pname + "*' found");
			System.exit(0);
		}
		final NominalParadigm parad = SktDeclension.paradigmMap.get(exactName);
		final Map<SktDeclension.Case, Map<SktDeclension.Number, List<String>>> product;
		if (stem.isEmpty()) {
			product = parad.getSampleProduct();
			System.out.println(drawDeclensionTable(parad, product));
			System.out.println(drawDeclensionTableDeva(parad, product));
		} else {
			final SktNominal term = new SktNominal(stem, parad);
			product = SktDeclension.compute(term);
			System.out.println(drawDeclensionTable(parad, product));
			System.out.println(drawDeclensionTableDeva(parad, product));
		}
	}

	private static String drawDeclensionTable(final NominalParadigm parad, final Map<SktDeclension.Case, Map<SktDeclension.Number, List<String>>> product) {
		final StringBuilder result = new StringBuilder();
		final int colWidth = 18;
		result.append("Paradigm name: " + parad.getName()).append(LINESEP);
		final List<NominalParadigm.Gender> glist = parad.getGenderList();
		final String gendStr = glist == null || glist.isEmpty() ? ""
								: glist.stream().map(x -> x.getShortName()).collect(Collectors.joining("/"));
		result.append("Gender: " + gendStr).append(LINESEP);
		result.append(String.format("     %-" + colWidth + "s%-" + colWidth + "s%-" + colWidth + "s", 
					SktDeclension.Number.SING.getName().toUpperCase(),
					SktDeclension.Number.DUAL.getName().toUpperCase(),
					SktDeclension.Number.PLU.getName().toUpperCase()));
		result.append(LINESEP);
		for (final SktDeclension.Case cas : SktDeclension.Case.values) {
			result.append(cas + "  ");
			final Map<SktDeclension.Number, List<String>> numList = product.get(cas);
			for (final SktDeclension.Number num : SktDeclension.Number.values) {
				result.append(String.format("%-" + colWidth + "s",
						numList.get(num).stream().collect(Collectors.joining(", "))));
			}
			result.append(LINESEP);
		}
		return result.toString();
	}

	private static String drawDeclensionTableDeva(final NominalParadigm parad, final Map<SktDeclension.Case, Map<SktDeclension.Number, List<String>>> product) {
		final StringBuilder result = new StringBuilder();
		result.append("\t" + SktDeclension.Number.SING.getDevaName() + "\t"
							+ SktDeclension.Number.DUAL.getDevaName() + "\t"
							+ SktDeclension.Number.PLU.getDevaName());
		result.append(LINESEP);
		for (final SktDeclension.Case cas : SktDeclension.Case.values) {
			result.append(cas.getDevaName());
			final Map<SktDeclension.Number, List<String>> numList = product.get(cas);
			for (final SktDeclension.Number num : SktDeclension.Number.values) {
				result.append("\t");
				result.append(numList.get(num).stream()
						.map(x -> ScriptTransliterator.translitQuick(x, ScriptTransliterator.EngineType.ROMAN_SKT_DEVA, false))
						.collect(Collectors.joining(", ")));
			}
			result.append(LINESEP);
		}
		return result.toString();
	}

	private static void saveMWTerms(final String[] args) throws IOException {
		final long startTime = System.currentTimeMillis();
		final Path outputPath = Path.of(Utilities.ROOTDIR + Utilities.OUTPUTPATH);
		if (Files.notExists(outputPath))
			Files.createDirectories(outputPath);
		printLog("Retrieving data...");
		final String condition = args[0];
		final String dbQuery = "SELECT KEY1 FROM MW WHERE MEANING LIKE '%" + condition + "%';";
		final Set<String> results = Utilities.getFirstColumnFromDB(Utilities.H2DB.SKTDICT, dbQuery);
		if (!results.isEmpty()) {
			final String sortedResult = results.stream().sorted(Utilities.sktComparator).collect(Collectors.joining(LINESEP));
			final String condStr = condition.replaceAll("<.*?>", "").replaceAll("[(<>.?/\\|]+", "");
			final File outfile = new File(Utilities.OUTPUTPATH + "mw-" + condStr + ".txt");
			printLog("Writing out " + outfile.getPath());
			Utilities.saveText(sortedResult, outfile);
			final long endTime = System.currentTimeMillis();
			printTime(endTime - startTime);
		}
	}

	private static void sortAndSave(final String[] args) throws Exception {
		Utilities.initializeComparator();
		final File input = new File(args[0]);
		final List<String> lineList = new ArrayList<>();
		try (final Scanner in = new Scanner(new FileInputStream(input), StandardCharsets.UTF_8)) {
			while (in.hasNextLine()) {
				final String line = in.nextLine().trim();
				if (line.isEmpty())
					continue;
				lineList.add(line);
			}
		}
		printLog("Sorting " + args[0]);
		Collections.sort(lineList, Utilities.sktComparator);
		final File outfile = new File(Utilities.OUTPUTPATH + "sorted_" + args[0]);
		printLog("Writing out " + outfile.getPath());
		Utilities.saveText(lineList.stream().collect(Collectors.joining(LINESEP)), outfile);
	}

	private static void saveConjugationProducts() throws Exception {
		final long startTime = System.currentTimeMillis();
		final Path outputPath = Path.of(Utilities.ROOTDIR + Utilities.OUTPUTPATH);
		if (Files.notExists(outputPath))
			Files.createDirectories(outputPath);
		final StringBuilder result = new StringBuilder();
		int vcount = 0;
		for (final SktVerb verb : VerbRepo.sktVerbMap.values()) {
			vcount++;
			result.append(getConjugResult(verb.getBucknellNumberStr(),
						verb.getCommonProduct(TenseMood.PRES, Pada.ACT),
						TenseMood.PRES.getNameCut(),
						Pada.ACT,
						VForm.ACT));
			result.append(getConjugResult(verb.getBucknellNumberStr(),
						verb.getCommonProduct(TenseMood.PRES, Pada.MID),
						TenseMood.PRES.getNameCut(),
						Pada.MID,
						VForm.MID));
			for (int i = 0; i < verb.getIrregularMiddleForm().size(); i++) {
				result.append(getConjugResult(verb.getBucknellNumberStr(),
							verb.getIrrMidCommonProduct(TenseMood.PRES, i),
							TenseMood.PRES.getNameCut(),
							Pada.MID,
							VForm.MID));
			}
		}
		printLog(vcount + " verbs processed");
		final File outfile = new File(Utilities.OUTPUTPATH + "allsktverbforms.txt");
		printLog("Writing out " + outfile.getPath());
		Utilities.saveText(result.toString(), outfile);
		final long endTime = System.currentTimeMillis();
		printTime(endTime - startTime);
	}

	private static String getConjugResult(final String ref, final Map<Person, Map<Number, List<String>>> prod,
										final String tense, final Pada pad, final VForm vform) {
		final StringBuilder result = new StringBuilder();
		prod.forEach((p, m) -> {
			m.forEach((n, l) -> {
				for (final String term : l) {
					result.append(ref + ":" + term + ":" + p.getInitial()+ ":" + n  + ":" + tense + ":" + pad + ":" + vform);
					result.append(LINESEP);
				}
			});
		});
		return result.toString();
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
				assertThat(product.get(i), equalTo(expected.get(i)));
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

	private static void testInternalSandhi() {
		// general case
		assertThat(Sandhi.applyInternalSandhi("vār", ""), equalTo("vār"));
		assertThat(Sandhi.applyInternalSandhi("", "ime"), equalTo("ime"));
		assertThat(Sandhi.applyInternalSandhi("vār", "ime"), equalTo("vārime"));
		assertThat(Sandhi.applyInternalSandhi("vār", "iṇa"), equalTo("vāriṇa"));
		assertThat(Sandhi.applyInternalSandhi("phal", "eṣu"), equalTo("phaleṣu"));
		// n rules
		assertThat(Sandhi.applyInternalSandhi("vār", "in"), equalTo("vārin"));
		assertThat(Sandhi.applyInternalSandhi("vār", "ina"), equalTo("vāriṇa"));
		assertThat(Sandhi.applyInternalSandhi("vār", "inā"), equalTo("vāriṇā"));
		assertThat(Sandhi.applyInternalSandhi("vār", "ini"), equalTo("vāriṇi"));
		assertThat(Sandhi.applyInternalSandhi("vār", "inī"), equalTo("vāriṇī"));
		assertThat(Sandhi.applyInternalSandhi("vār", "inu"), equalTo("vāriṇu"));
		assertThat(Sandhi.applyInternalSandhi("vār", "inū"), equalTo("vāriṇū"));
		assertThat(Sandhi.applyInternalSandhi("vār", "inṛ"), equalTo("vāriṇṛ"));
		assertThat(Sandhi.applyInternalSandhi("vār", "inṝ"), equalTo("vāriṇṝ"));
		assertThat(Sandhi.applyInternalSandhi("vār", "ine"), equalTo("vāriṇe"));
		assertThat(Sandhi.applyInternalSandhi("vār", "ino"), equalTo("vāriṇo"));
		assertThat(Sandhi.applyInternalSandhi("vār", "inna"), equalTo("vāriṇna"));
		assertThat(Sandhi.applyInternalSandhi("vār", "inma"), equalTo("vāriṇma"));
		assertThat(Sandhi.applyInternalSandhi("vār", "inya"), equalTo("vāriṇya"));
		assertThat(Sandhi.applyInternalSandhi("vār", "inva"), equalTo("vāriṇva"));
		assertThat(Sandhi.applyInternalSandhi("vāp", "ina"), equalTo("vāpina"));
		assertThat(Sandhi.applyInternalSandhi("ṣāp", "ina"), equalTo("ṣāpiṇa"));
		assertThat(Sandhi.applyInternalSandhi("vārap", "ina"), equalTo("vārapiṇa"));
		assertThat(Sandhi.applyInternalSandhi("vākṛp", "ina"), equalTo("vākṛpiṇa"));
		assertThat(Sandhi.applyInternalSandhi("vākṝp", "ina"), equalTo("vākṝpiṇa"));
		assertThat(Sandhi.applyInternalSandhi("vāṣap", "ina"), equalTo("vāṣapiṇa"));
		assertThat(Sandhi.applyInternalSandhi("vāral", "ina"), equalTo("vāralina"));
		assertThat(Sandhi.applyInternalSandhi("vārac", "ina"), equalTo("vāracina"));
		assertThat(Sandhi.applyInternalSandhi("sarv", "ena"), equalTo("sarveṇa"));
		assertThat(Sandhi.applyInternalSandhi("sarv", "āni"), equalTo("sarvāṇi"));
		// s rules
		assertThat(Sandhi.applyInternalSandhi("phal", "esu"), equalTo("phaleṣu"));
		assertThat(Sandhi.applyInternalSandhi("phal", "esru"), equalTo("phalesru"));
		assertThat(Sandhi.applyInternalSandhi("phal", "esṛ"), equalTo("phalesṛ"));
		assertThat(Sandhi.applyInternalSandhi("phal", "esṝ"), equalTo("phalesṝ"));
		assertThat(Sandhi.applyInternalSandhi("phak", "su"), equalTo("phakṣu"));
		assertThat(Sandhi.applyInternalSandhi("phaṃ", "su"), equalTo("phaṃsu"));
		assertThat(Sandhi.applyInternalSandhi("phaḥ", "su"), equalTo("phaḥsu"));
		assertThat(Sandhi.applyInternalSandhi("phiṃ", "su"), equalTo("phiṃṣu"));
		assertThat(Sandhi.applyInternalSandhi("phiḥ", "su"), equalTo("phiḥṣu"));
	}

	private static void testDeclensionAuto() {
		final NominalParadigm parad = SktDeclension.paradigmMap.get("devaḥ");
		final SktNominal devah = new SktNominal("dev", parad);
		final DeclensionTestCase testCase = new DeclensionTestCase(SktDeclension.compute(devah));
		assertThat(testCase.getCase(SktDeclension.Case.NOM), arrayContaining("devaḥ", "devau", "devāḥ"));
		assertThat(testCase.getCase(SktDeclension.Case.ACC), arrayContaining("devam", "devau", "devān"));
		assertThat(testCase.getCase(SktDeclension.Case.INS), arrayContaining("devena", "devābhyām", "devaiḥ"));
		assertThat(testCase.getCase(SktDeclension.Case.DAT), arrayContaining("devāya", "devābhyām", "devebhyaḥ"));
		assertThat(testCase.getCase(SktDeclension.Case.ABL), arrayContaining("devāt", "devābhyām", "devebhyaḥ"));
		assertThat(testCase.getCase(SktDeclension.Case.GEN), arrayContaining("devasya", "devayoḥ", "devānām"));
		assertThat(testCase.getCase(SktDeclension.Case.LOC), arrayContaining("deve", "devayoḥ", "deveṣu"));
		assertThat(testCase.getCase(SktDeclension.Case.VOC), arrayContaining("deva", "devau", "devāḥ"));
	}

	private static void testAugmentAuto() {
		assertThat(VerbalParadigm.augment(""), equalTo("a"));
		assertThat(VerbalParadigm.augment("kat"), equalTo("akat"));
		assertThat(VerbalParadigm.augment("akat"), equalTo("ākat"));
		assertThat(VerbalParadigm.augment("ākat"), equalTo("ākat"));
		assertThat(VerbalParadigm.augment("ikat"), equalTo("aikat"));
		assertThat(VerbalParadigm.augment("īkat"), equalTo("aikat"));
		assertThat(VerbalParadigm.augment("ukat"), equalTo("aukat"));
		assertThat(VerbalParadigm.augment("ūkat"), equalTo("aukat"));
		assertThat(VerbalParadigm.augment("ṛkat"), equalTo("ārkat"));
		assertThat(VerbalParadigm.augment("ṝkat"), equalTo("ārkat"));
		assertThat(VerbalParadigm.augment("ḷkat"), equalTo("ālkat"));
		assertThat(VerbalParadigm.augment("ekat"), equalTo("aikat"));
		assertThat(VerbalParadigm.augment("okat"), equalTo("aukat"));
		assertThat(VerbalParadigm.augment("aikat"), equalTo("aikat"));
		assertThat(VerbalParadigm.augment("aukat"), equalTo("aukat"));
	}

	private static void testMisc() {
		assertThat(SanskritUtilities.endsWithDoubleConsonant("t"), equalTo(false));
		assertThat(SanskritUtilities.endsWithDoubleConsonant("tt"), equalTo(true));
		assertThat(SanskritUtilities.endsWithDoubleConsonant("it"), equalTo(false));
		assertThat(SanskritUtilities.endsWithDoubleConsonant("ith"), equalTo(false));
		assertThat(SanskritUtilities.endsWithDoubleConsonant("tth"), equalTo(true));
		assertThat(SanskritUtilities.endsWithDoubleConsonant("itth"), equalTo(true));
		assertThat(SanskritUtilities.endsWithDoubleConsonant("tith"), equalTo(false));
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

	static class DeclensionTestCase {
		private final Map<SktDeclension.Case, Map<SktDeclension.Number, List<String>>> product;
		public DeclensionTestCase(final Map<SktDeclension.Case, Map<SktDeclension.Number, List<String>>> prod) {
			product = prod;
		}
		public String[] getCase(final SktDeclension.Case cas) {
			final String[] result = new String[3];
			final Map<SktDeclension.Number, List<String>> numMap = product.get(cas);
			result[0] = numMap.get(SktDeclension.Number.SING).get(0);
			result[1] = numMap.get(SktDeclension.Number.DUAL).get(0);
			result[2] = numMap.get(SktDeclension.Number.PLU).get(0);
			return result;
		}
	}

}

