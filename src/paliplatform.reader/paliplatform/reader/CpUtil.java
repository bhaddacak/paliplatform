/*
 * CpUtil.java
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
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see https://www.gnu.org/licenses/.
 */

package paliplatform.reader;

import paliplatform.base.*;
import paliplatform.base.Utilities.PaliScript;
import paliplatform.base.ScriptTransliterator;
import paliplatform.base.ScriptTransliterator.EngineType;

import java.util.*;
import java.util.stream.*;
import java.util.zip.*;
import java.io.*;
import java.nio.file.*;
import java.nio.charset.*;

/** 
 * The CLI tool for corpus investigation.
 * This consists of static factory methods.
 * The tool can be invoked by this command line from the program's root dir:
 * $ java -p modules -m paliplatform.reader/paliplatform.reader.CpUtil
 *
 * @author J.R. Bhaddacak
 * @version 3.4
 * @since 3.3
 */
final public class CpUtil {
	private static final String LINESEP = System.getProperty("line.separator");
	private static final File scData = new File(ReaderUtilities.SCPATH + ReaderUtilities.BILARA_DATA);
	private static final String metaChars = "\\{}[]()*+?^$";
	private static final int DEF_MAX = 25;

	private CpUtil() {
	}

	public static void main(final String[] args) throws Exception {
		if (args.length == 0) {
			printHelpAndExit();
		}
		String opt = "";
		String param = "";
		switch (args[0]) {
			case "list":
				opt = args.length > 1 ? args[1] : "";
				param = args.length > 2 ? args[2] : "";
				if (opt.equals("-f")) {
					showFullInfo();
				} else if (opt.equals("-l") && !param.isEmpty()) {
					final long startTime = System.currentTimeMillis();
					if (param.equalsIgnoreCase("sc"))
						printLog("For SuttaCentral, please use ScUtil instead");
					else
						printLog(getFileList(param.toLowerCase()));
					final long endTime = System.currentTimeMillis();
					printTime(endTime - startTime);
				} else {
					showBriefInfo();
				}
				break;
			case "show":
				opt = args.length > 1 ? args[1] : "";
				param = args.length > 2 ? args[2] : "";
				if (opt.equals("-c") && !param.isEmpty()) {
					final long startTime = System.currentTimeMillis();
					printLog("Analyzing...");
					printLog(analyzeChars(param.toLowerCase()));
					final long endTime = System.currentTimeMillis();
					printTime(endTime - startTime);
				} else {
					printHelpAndExit();
				}
				break;
			case "find":
				opt = args.length > 1 ? args[1] : "";
				param = args.length > 2 ? args[2] : "";
				final String strToFind = args.length > 3 ? args[3] : "";
				if (opt.equals("-c") && !param.isEmpty() && !strToFind.isEmpty()) {
					final long startTime = System.currentTimeMillis();
					printLog("Analyzing...");
					printLog(findChar(param.toLowerCase(), strToFind));
					final long endTime = System.currentTimeMillis();
					printTime(endTime - startTime);
				} else {
					printHelpAndExit();
				}
				break;
			case "save":
				opt = args.length > 1 ? args[1] : "";
				param = args.length > 2 ? args[2] : "";
				if (opt.equals("-l") && !param.isEmpty()) {
					if (param.equalsIgnoreCase("sc"))
						printLog("For SuttaCentral, please use ScUtil instead");
					else
						saveFileList(param.toLowerCase());
				} else if (opt.equals("-c") && !param.isEmpty()) {
					analyzeCharsAndSave(param);
				} else if (opt.equals("-cf") && !param.isEmpty()) {
					final int max = args.length > 3
									? args[3].matches("\\d+") ? Integer.parseInt(args[3]) : DEF_MAX
									: DEF_MAX;
					analyzeCharsFullAndSave(param, max);
				} else if (opt.matches("-t.") && !param.isEmpty()) {
					final char langCode = opt.charAt(2);
					final String filename = args.length > 3 ? args[3] : "";
					saveTransliterated(param, filename, langCode);
				} else {
					printHelpAndExit();
				}
				break;
			case "test":
				break;
			default:
				printHelpAndExit();
		}
	}

	private static void printHelpAndExit() {
		final StringBuilder help = new StringBuilder();
		help.append(LINESEP).append("Pāli Platform Corpus Util CLI").append(LINESEP);
		help.append("  Usage:").append(LINESEP);
		help.append("    CpUtil <command> <option>").append(LINESEP);
		help.append("      (The abstract CpUtil can be a launcher script,").append(LINESEP);
		help.append("       such as cputil.sh or cputil.cmd which can be").append(LINESEP);
		help.append("       found in the program's root directory.").append(LINESEP);
		help.append("       See also Notes below.)").append(LINESEP);
		help.append("  Commands:").append(LINESEP);
		help.append("    list\tShow information of corpora").append(LINESEP);
		help.append("        List options:").append(LINESEP);
		help.append("        -l <corpus>\tShow file list of <corpus>").append(LINESEP);
		help.append("        -f\tShow full information of collections").append(LINESEP);
		help.append("        <none>\tShow brief information of collections").append(LINESEP);
		help.append("    show\tShow some analyses").append(LINESEP);
		help.append("        Show options:").append(LINESEP);
		help.append("        -c <corpus>\tAnalyze character stat in <corpus>").append(LINESEP);
		help.append("    find\tFind certain information").append(LINESEP);
		help.append("        Find options:").append(LINESEP);
		help.append("        -c <corpus> <ch>\tFind a character in <corpus>").append(LINESEP);
		help.append("    save\tAnalyze and save data").append(LINESEP);
		help.append("        Save options:").append(LINESEP);
		help.append("        -l <corpus>\tSave file list of <corpus>").append(LINESEP);
		help.append("        -c <corpus>\tAnalyze character stat in <corpus> and save").append(LINESEP);
		help.append("        -cf <corpus> [max]\tLike -c but with top max-freq result findings").append(LINESEP);
		help.append("        -tr <corpus> <files>\tSave <files> to Roman script (Pali Common)").append(LINESEP);
		help.append("        -ti <corpus> <files>\tSave <files> to Roman script (ISO 15919)").append(LINESEP);
		help.append("        -ta <corpus> <files>\tSave <files> to Roman script (IAST)").append(LINESEP);
		help.append("        -tl <corpus> <files>\tSave <files> to Roman script (Least)").append(LINESEP);
		help.append("        -tu <corpus> <files>\tSave <files> to Roman script (Unique)").append(LINESEP);
		help.append("        -td <corpus> <files>\tSave <files> to Devanagari script").append(LINESEP);
		help.append("        -tk <corpus> <files>\tSave <files> to Khmer script").append(LINESEP);
		help.append("        -tm <corpus> <files>\tSave <files> to Myanmar script").append(LINESEP);
		help.append("        -ts <corpus> <files>\tSave <files> to Sinhala script").append(LINESEP);
		help.append("        -tt <corpus> <files>\tSave <files> to Thai script").append(LINESEP);
		help.append("    <none>\tShow this help").append(LINESEP);
		help.append("  Examples:").append(LINESEP);
		help.append("    1. To list all corpora with full information:").append(LINESEP);
		help.append("       $ CpUtil list -f").append(LINESEP);
		help.append("    2. To show the character stat of SuttaCentra collection:").append(LINESEP);
		help.append("       $ CpUtil show -c sc").append(LINESEP);
		help.append("       (This command will show at most 20 results)").append(LINESEP);
		help.append("    3. To find whether '^' is unexpectedly present in CSTDEVA:").append(LINESEP);
		help.append("       $ CpUtil find -c cstdeva '^'").append(LINESEP);
		help.append("       (No escaping required)").append(LINESEP);
		help.append("    4. To save the character stat of CST4:").append(LINESEP);
		help.append("       $ CpUtil save -c cst4").append(LINESEP);
		help.append("    5. To save the character stat with finding results of CSTDEVA:").append(LINESEP);
		help.append("       (25 occurences maximum by default)").append(LINESEP);
		help.append("       $ CpUtil save -cf cstdeva").append(LINESEP);
		help.append("    6. To save the character stat of with finding results of GRAM:").append(LINESEP);
		help.append("       (10 occurences maximum)").append(LINESEP);
		help.append("       $ CpUtil save -cf gram 10").append(LINESEP);
		help.append("    7. To show file list in CSTDEVA:").append(LINESEP);
		help.append("       $ CpUtil list -l cstdeva").append(LINESEP);
		help.append("    8. To save Visuddhimagga 1 in CSTDEVA to common Roman:").append(LINESEP);
		help.append("       $ CpUtil save -tr cstdeva e0101n.mul").append(LINESEP);
		help.append("       (File extension can be omitted)").append(LINESEP);
		help.append("    9. To save all mūla Vinaya of CST4 to Devanagari:").append(LINESEP);
		help.append("       $ CpUtil save -td cst4 vin*mul").append(LINESEP);
		help.append("       (Wildcards, ? and *, can be used for multiple input)").append(LINESEP);
		help.append("   10. To save MN10 of SuttaCentral to Devanagari:").append(LINESEP);
		help.append("       $ CpUtil save -td sc mn10").append(LINESEP);
		help.append("       (Only SC needs an exact text ID, one file at a time)").append(LINESEP);
		help.append("  Technical notes:").append(LINESEP);
		help.append("    1. IAST here uses ṃ (m dot below) not ṁ (m dot above),").append(LINESEP);
		help.append("       but uses ḻ (l line below) as a consonant, not ḷ (l dot below),").append(LINESEP);
		help.append("       because the latter stands for a Sanskrit vowel.").append(LINESEP);
		help.append("    2. Pāli Common also uses ṃ (m dot below), also ḷ (l dot below)").append(LINESEP);
		help.append("       as a consonant. This transliteration is commonly used for Pāli.").append(LINESEP);
		help.append("       Moreover, ṛ, ṝ, ḷ, and ḹ are also used as Sanskrit vowels.").append(LINESEP);
		help.append("       Hence, the method can be ambiguous when used with Sanskrit.").append(LINESEP);
		help.append("    3. Least Contamination method is like Pāli Common but retains").append(LINESEP);
		help.append("       Devanagari symbols visually. This guarantees 100% reversibility").append(LINESEP);
		help.append("       for Pali, but possibly unsuitable for Sanskrit.").append(LINESEP);
		help.append("    4. Roman Unique is like Least Contamination, but ḻ (l line below)").append(LINESEP);
		help.append("       is used instead. This makes Sanskrit 100% reversible.").append(LINESEP);
		help.append("    5. Converting from Roman to other scripts does not suppose").append(LINESEP);
		help.append("       ISO nor IAST input. Only Pali is guaranteed.").append(LINESEP);
		help.append("    6. To invoke the program, the Java convention has to be used.").append(LINESEP);
		help.append("       At the program's root directory, if no launcher script available,").append(LINESEP);
		help.append("       type this at the console: ").append(LINESEP);
		help.append("       $ java -p modules -m paliplatform.reader/paliplatform.reader.CpUtil").append(LINESEP);
		help.append("       (For the SuttaCentral corpus, see also ScUtil.)").append(LINESEP);
		printLog(help.toString());
		System.exit(0);
	}

	private static void printLog(final String mess) {
		System.out.println(mess);
	}

	private static void printTime(final long msec) {
		printLog(String.format("Done in %.3f seconds", msec/1000.0));
	}

	private static Corpus.Collection getCollection(final String colStr) {
		final Corpus.Collection col = Corpus.Collection.idMap.get(colStr);
		if (col == null) {
			printLog("Error: Invalid corpus name");
			System.exit(1);
		}
		return col;
	}

	private static void showBriefInfo() {
		final long startTime = System.currentTimeMillis();
		ReaderUtilities.updateCorpusList(true);
		for (final Corpus.Collection col : Corpus.Collection.values) {
			final Corpus cp = ReaderUtilities.corpusMap.get(col);
			printLog((col.ordinal() + 1) + ". " + col + ": " + cp.getName());
			if (cp.isInArchive()) {
				final File zipfile = cp.getZipFile();
				final String found = zipfile.exists() ? " (found)" : " (not found)";
				printLog("   => " + zipfile.getPath() + found);
			} else {
				final File dir = new File(ReaderUtilities.TEXTPATH + cp.getRootName());
				String[] flist = new String[0];
				if (dir.exists())
					flist = dir.list();
				printLog("   => " + dir.getPath() + " (" + flist.length + " files found)");
			}
		}
		final long endTime = System.currentTimeMillis();
		printTime(endTime - startTime);
	}


	private static void showFullInfo() {
		final long startTime = System.currentTimeMillis();
		ReaderUtilities.updateCorpusList(true);
		for (final Corpus.Collection col : Corpus.Collection.values) {
			final Corpus cp = ReaderUtilities.corpusMap.get(col);
			printLog((col.ordinal() + 1) + ". " + col + ": " + cp.getName());
			if (cp.isInArchive()) {
				final File zipfile = cp.getZipFile();
				final String found = zipfile.exists() ? " (found)" : " (not found)";
				printLog("   File: " + zipfile.getPath() + found);
			} else {
				final File dir = new File(ReaderUtilities.TEXTPATH + cp.getRootName());
				String[] flist = new String[0];
				if (dir.exists())
					flist = dir.list();
				printLog("   File: " + dir.getPath() + " (" + flist.length + " files found)");
			}
			printLog("   Encoding: " + cp.getEncoding());
			printLog("   Script: " + cp.getScript());
			printLog("   Transformable: " + (cp.isTransformable() ? "yes" : "no"));
			printLog("   Description: " + cp.getDescription());
			printLog("   Copyright: " + cp.getCopyright());
			printLog("   URLs: " + cp.getUrlList().stream().collect(Collectors.joining(", ")));
		}
		final long endTime = System.currentTimeMillis();
		printTime(endTime - startTime);
	}

	private static String getFileList(final String colStr) throws IOException {
		final Corpus.Collection col = getCollection(colStr);
		ReaderUtilities.updateCorpusList(true);
		final Corpus cp = ReaderUtilities.corpusMap.get(col);
		final Collection<DocumentInfo> docinfos = cp.getDocInfoMap().values();
		String result = "";
		switch (col) {
			case CSTR:
			case CSTDEVA:
			case SRT:
				result = docinfos.stream()
							.map(x -> x.getFileNameWithExt() + ": " + x.getTextName())
							.collect(Collectors.joining(LINESEP));
				break;
			case CST4:
			case BJT:
			case PTST:
			case GRAM:
				result = docinfos.stream()
							.map(x -> {
								final String fname = x.getFileNameWithExt();
								final String fnameOK = fname.substring(fname.lastIndexOf("/") + 1);
								return fnameOK + ": " + x.getTextName();
							})
							.collect(Collectors.joining(LINESEP));
				break;
		}
		result = result + LINESEP + docinfos.size() + " files listed";
		return result;
	}

	private static void saveFileList(final String colStr) throws IOException {
		final long startTime = System.currentTimeMillis();
		final String filelist = getFileList(colStr);
		final Path outputPath = Path.of(Utilities.ROOTDIR + Utilities.OUTPUTPATH);
		if (Files.notExists(outputPath))
			Files.createDirectories(outputPath);
		final File outfile = new File(Utilities.OUTPUTPATH + colStr + "-filelist.txt");
		printLog("Writing out " + outfile.getPath());
		Utilities.saveText(filelist, outfile);
		final long endTime = System.currentTimeMillis();
		printTime(endTime - startTime);

	}

	private static String analyzeChars(final File zipfile, final Charset charset) throws IOException {
		final Map<Character, Integer> charFreqMap = new HashMap<>();
		final ZipFile zip = new ZipFile(zipfile);
		for (final Enumeration<? extends ZipEntry> e = zip.entries(); e.hasMoreElements();) {
			final ZipEntry entry = e.nextElement();
			try (final Scanner in = new Scanner(zip.getInputStream(entry), charset)) {
				while (in.hasNextLine()) {
					final String line = in.nextLine().trim().replaceAll("</?.*?>", "");
					final char[] cArr = line.toCharArray();
					for (final char ch : cArr) {
						if (charFreqMap.containsKey(ch))
							charFreqMap.computeIfPresent(ch, (k, v) -> v + 1);
						else
							charFreqMap.put(ch, 1);
					}
				}
			}
		}
		zip.close();
		final String result = charFreqMap.entrySet().stream()
								.sorted(Map.Entry.comparingByValue())
								.map(x -> x.getKey() + "\t" + x.getValue())
								.collect(Collectors.joining(LINESEP));
		return result;
	}

	private static String getBjtCharStat(final Corpus cp) throws IOException {
		final File zipfile = cp.getZipFile();
		if (!zipfile.exists()) return "";
		final Map<Character, Integer> charFreqMap = new HashMap<>();
		final ZipFile zip = new ZipFile(zipfile);
		for (final Enumeration<? extends ZipEntry> e = zip.entries(); e.hasMoreElements();) {
			final ZipEntry entry = e.nextElement();
			List<BjtPage> pageList = null;
			if (entry != null && entry.getName().endsWith(".json")) {
				pageList = ReaderUtilities.getBjtPages(zip.getInputStream(entry));
				if (pageList == null) continue;
				for (final BjtPage bp : pageList) {
					final String text = bp.getAllText().replaceAll("</?.*?>", "");
					final char[] cArr = text.toCharArray();
					for (final char ch : cArr) {
						if (charFreqMap.containsKey(ch))
							charFreqMap.computeIfPresent(ch, (k, v) -> v + 1);
						else
							charFreqMap.put(ch, 1);
					} // end for
				} // end for
			} // end if
		} // end for
		zip.close();
		final String result = charFreqMap.entrySet().stream()
								.sorted(Map.Entry.comparingByValue())
								.map(x -> x.getKey() + "\t" + x.getValue())
								.collect(Collectors.joining(LINESEP));
		return result;
	}

	private static String getCstrCharStat(final Corpus cp) throws IOException {
		final File dir = new File(ReaderUtilities.TEXTPATH + cp.getRootName());
		if (!dir.exists()) return "";
		final Map<Character, Integer> charFreqMap = new HashMap<>();
		final File[] files = dir.listFiles((d, f) -> f.endsWith(".gz"));
		for (final File gz : files) {
			final String text = ReaderUtilities.readGz(gz, cp.getEncoding().getCharset()).replaceAll("</?.*?>", "");
			final char[] cArr = text.toCharArray();
			for (final char ch : cArr) {
				if (charFreqMap.containsKey(ch))
					charFreqMap.computeIfPresent(ch, (k, v) -> v + 1);
				else
					charFreqMap.put(ch, 1);
			}
		}
		final String result = charFreqMap.entrySet().stream()
								.sorted(Map.Entry.comparingByValue())
								.map(x -> x.getKey() + "\t" + x.getValue())
								.collect(Collectors.joining(LINESEP));
		return result;
	}

	private static String getCstdevaCharStat(final Corpus cp) throws IOException {
		final File dir = new File(ReaderUtilities.TEXTPATH + cp.getRootName());
		if (!dir.exists()) return "";
		final Map<Character, Integer> charFreqMap = new HashMap<>();
		final File[] files = dir.listFiles((d, f) -> f.endsWith(".xml"));
		for (final File xml : files) {
			final String text = Files.readString(xml.toPath(), cp.getEncoding().getCharset()).replaceAll("</?.*?>", "");
			final char[] cArr = text.toCharArray();
			for (final char ch : cArr) {
				if (charFreqMap.containsKey(ch))
					charFreqMap.computeIfPresent(ch, (k, v) -> v + 1);
				else
					charFreqMap.put(ch, 1);
			}
		}
		final String result = charFreqMap.entrySet().stream()
								.sorted(Map.Entry.comparingByValue())
								.map(x -> x.getKey() + "\t" + x.getValue())
								.collect(Collectors.joining(LINESEP));
		return result;
	}

	private static String analyzeChars(final String colStr) throws IOException {
		final Corpus.Collection col = getCollection(colStr);
		ReaderUtilities.updateCorpusList(true);
		final Corpus cp = ReaderUtilities.corpusMap.get(col);
		String result = "";
		switch (col) {
			case CSTR:
				result = getCstrCharStat(cp);
				break;
			case CSTDEVA:
				result = getCstdevaCharStat(cp);
				break;
			case CST4:
			case PTST:
			case SRT:
			case GRAM:
				final File zipfile = cp.getZipFile();
				if (zipfile.exists())
					result = analyzeChars(zipfile, cp.getEncoding().getCharset());
				break;
			case BJT:
				result = getBjtCharStat(cp);
				break;
			case SC:
				result = ScUtil.analyzeChars(scData);
				break;
		}
		return result;
	}

	private static void analyzeCharsAndSave(final String colStr) throws IOException {
		final long startTime = System.currentTimeMillis();
		final Path outputPath = Path.of(Utilities.ROOTDIR + Utilities.OUTPUTPATH);
		if (Files.notExists(outputPath))
			Files.createDirectories(outputPath);
		printLog("Analyzing data...");
		final String result = analyzeChars(colStr);
		final File outfile = new File(Utilities.OUTPUTPATH + colStr + "-charstat.txt");
		printLog("Writing out " + outfile.getPath());
		Utilities.saveText(result, outfile);
		final long endTime = System.currentTimeMillis();
		printTime(endTime - startTime);
	}

	private static void analyzeCharsFullAndSave(final String colStr, final int maxFreq) throws IOException {
		final long startTime = System.currentTimeMillis();
		final Path outputPath = Path.of(Utilities.ROOTDIR + Utilities.OUTPUTPATH);
		if (Files.notExists(outputPath))
			Files.createDirectories(outputPath);
		final StringBuilder result = new StringBuilder();
		printLog("Analyzing data...");
		final String statRes = analyzeChars(colStr);
		result.append("Character Statistics").append(LINESEP);
		result.append(statRes).append(LINESEP);
		printLog("Finding characters...");
		final String[] lines = statRes.split("\\r?\\n");
		final List<String> charList = new ArrayList<>();
		for (final String line : lines) {
			final String[] res = line.split("\t");
			if (res.length < 2) continue;
			final int freq = Integer.parseInt(res[1]);
			if (freq <= maxFreq) {
				charList.add(res[0]);
			} else {
				break;
			}
		}
		if (!charList.isEmpty()) {
			result.append(LINESEP);
			result.append("Top Result Findings (maximum = " + maxFreq + ")").append(LINESEP);
			printLog("Top characters found (maximum = " + maxFreq + "):");
			printLog(charList.stream().collect(Collectors.joining(", ")));
		}
		for (final String ch : charList) {
			printLog("Finding " + ch);
			result.append("Occurences of '" + ch + "'").append(LINESEP);
			final String findRes = findChar(colStr, ch);
			result.append(findRes).append(LINESEP).append(LINESEP);
		}
		final File outfile = new File(Utilities.OUTPUTPATH + colStr + "-charstatfull.txt");
		printLog("Writing out " + outfile.getPath());
		Utilities.saveText(result.toString(), outfile);
		final long endTime = System.currentTimeMillis();
		printTime(endTime - startTime);
	}

	public static String makeProperQuery(final String toFind) {
		final char first = toFind.charAt(0);
		return metaChars.indexOf(first) > -1 ? "\\" + first : "" + first;
	}

	private static String findChar(final File zipfile, final Charset charset, final String query) throws IOException {
		final Map<String, Long> fileMap = new HashMap<>();
		final ZipFile zip = new ZipFile(zipfile);
		int max = 20;
		for (final Enumeration<? extends ZipEntry> e = zip.entries(); e.hasMoreElements();) {
			if (max <= 0) break;
			final ZipEntry entry = e.nextElement();
			final String fname  = entry.getName();
			long foundCount = 0;
			try (final Scanner in = new Scanner(zip.getInputStream(entry), charset)) {
				foundCount = in.findAll(query).count();
				if (foundCount > 0) {
					fileMap.put(fname.substring(fname.lastIndexOf("/") + 1), foundCount);
					max--;
				}
			}
		}
		zip.close();
		final String result = fileMap.entrySet().stream()
								.sorted(Map.Entry.comparingByKey())
								.map(x -> x.getKey() + "\t" + x.getValue())
								.collect(Collectors.joining(LINESEP));
		return result;
	}

	private static String findBjtChar(final Corpus cp, final String query) throws IOException {
		final File zipfile = cp.getZipFile();
		if (!zipfile.exists()) return "";
		final Map<String, Long> fileMap = new HashMap<>();
		final ZipFile zip = new ZipFile(zipfile);
		int max = 20;
		for (final Enumeration<? extends ZipEntry> e = zip.entries(); e.hasMoreElements();) {
			if (max <= 0) break;
			final ZipEntry entry = e.nextElement();
			final String fname  = entry.getName();
			List<BjtPage> pageList = null;
			long foundCount = 0;
			if (entry != null && fname.endsWith(".json")) {
				pageList = ReaderUtilities.getBjtPages(zip.getInputStream(entry));
				if (pageList == null) continue;
				for (final BjtPage bp : pageList) {
					final String text = bp.getAllText().replaceAll("</?.*?>", "");
					try (final Scanner in = new Scanner(text)) {
						foundCount = in.findAll(query).count();
						if (foundCount > 0) {
							fileMap.put(fname.substring(fname.lastIndexOf("/") + 1), foundCount);
							max--;
						}
					}
				} // end for
			} // end if
		} // end for
		zip.close();
		final String result = fileMap.entrySet().stream()
								.sorted(Map.Entry.comparingByKey())
								.map(x -> x.getKey() + "\t" + x.getValue())
								.collect(Collectors.joining(LINESEP));
		return result;
	}

	private static String findCstrChar(final Corpus cp, final String query) throws IOException {
		final File dir = new File(ReaderUtilities.TEXTPATH + cp.getRootName());
		if (!dir.exists()) return "";
		final Map<String, Long> fileMap = new HashMap<>();
		final File[] files = dir.listFiles((d, f) -> f.endsWith(".gz"));
		int max = 20;
		long foundCount = 0;
		for (final File gz : files) {
			if (max <= 0) break;
			final String fname = gz.getName();
			final String text = ReaderUtilities.readGz(gz, cp.getEncoding().getCharset()).replaceAll("</?.*?>", "");
			try (final Scanner in = new Scanner(text)) {
				foundCount = in.findAll(query).count();
				if (foundCount > 0) {
					fileMap.put(fname.substring(fname.lastIndexOf("/") + 1), foundCount);
					max--;
				}
			}
		}
		final String result = fileMap.entrySet().stream()
								.sorted(Map.Entry.comparingByKey())
								.map(x -> x.getKey() + "\t" + x.getValue())
								.collect(Collectors.joining(LINESEP));
		return result;
	}

	private static String findCstdevaChar(final Corpus cp, final String query) throws IOException {
		final File dir = new File(ReaderUtilities.TEXTPATH + cp.getRootName());
		if (!dir.exists()) return "";
		final Map<String, Long> fileMap = new HashMap<>();
		final File[] files = dir.listFiles((d, f) -> f.endsWith(".xml"));
		int max = 20;
		long foundCount = 0;
		for (final File xml : files) {
			if (max <= 0) break;
			final String fname = xml.getName();
			final String text = Files.readString(xml.toPath(), cp.getEncoding().getCharset()).replaceAll("</?.*?>", "");
			try (final Scanner in = new Scanner(text)) {
				foundCount = in.findAll(query).count();
				if (foundCount > 0) {
					fileMap.put(fname.substring(fname.lastIndexOf("/") + 1), foundCount);
					max--;
				}
			}
		}
		final String result = fileMap.entrySet().stream()
								.sorted(Map.Entry.comparingByKey())
								.map(x -> x.getKey() + "\t" + x.getValue())
								.collect(Collectors.joining(LINESEP));
		return result;
	}

	private static String findChar(final String colStr, final String toFind) throws IOException {
		final Corpus.Collection col = getCollection(colStr);
		if (ReaderUtilities.corpusMap == null || ReaderUtilities.corpusMap.isEmpty())
			ReaderUtilities.updateCorpusList(true);
		final Corpus cp = ReaderUtilities.corpusMap.get(col);
		final String query = makeProperQuery(toFind);
		String result = "";
		switch (col) {
			case CSTR:
				result = findCstrChar(cp, query);
				break;
			case CSTDEVA:
				result = findCstdevaChar(cp, query);
				break;
			case CST4:
			case PTST:
			case SRT:
			case GRAM:
				final File zipfile = cp.getZipFile();
				if (zipfile.exists())
					result = findChar(zipfile, cp.getEncoding().getCharset(), query);
				break;
			case BJT:
				result = findBjtChar(cp, query);
				break;
			case SC:
				result = ScUtil.findChar(scData, query);
				break;
		}
		return result;
	}

	private static void saveTransliterated(final String colStr, final String filename, final char lang) throws Exception {
		final Corpus.Collection col = getCollection(colStr);
		if (col == Corpus.Collection.PTST) {
			printLog("Error: The operation has not been implemented");
			System.exit(1);
		}
		if (filename.isEmpty()) {
			printLog("Error: File not given");
			System.exit(1);
		}
		final long startTime = System.currentTimeMillis();
		ReaderUtilities.updateCorpusList(true);
		final Corpus cp = ReaderUtilities.corpusMap.get(col);
		final Collection<DocumentInfo> docinfos = cp.getDocInfoMap().values();
		final String bareFilename = filename.matches(".*(\\.xml|\\.gz|\\.htm|\\.txt|\\.json)$")
									? filename.substring(0, filename.lastIndexOf("."))
									: filename;
		final List<String> fileList;
		if (col == Corpus.Collection.SC) {
			fileList = List.of(filename + "_root-pli-ms.json");
		} else {
			final String regexFilename = ".*" + bareFilename.replace(".", "\\.").replace('?', '.').replace("*", ".*") + "$";
			fileList = docinfos.stream()
						.map(DocumentInfo::getFileNameWithExt)
						.filter(x -> {
							final String bare = x.substring(0, x.lastIndexOf("."));
							return bare.matches(regexFilename);
						})
						.collect(Collectors.toList());
		}
		if (fileList.isEmpty()) {
			printLog("Error: File not found");
			System.exit(1);
		} else {
			final PaliScript sourceScript = cp.getScript();
			final char srcInit = Character.toLowerCase(sourceScript.toString().charAt(0));
			final String engineCode = Character.toString(srcInit) + Character.toString(lang);
			final EngineType engine = EngineType.fromCode(engineCode);
			if (engine == null) {
				printLog("Error: Unsupported transliteration method");
				System.exit(1);
			}
			final String targetScript = engine.getTargetScript().getName();
			ScriptTransliterator.initializeTransliterator();
			printLog("Converting " + sourceScript.getName() + " to " + targetScript + " (" + engine.getName() + ")");
			for (final String file : fileList) {
				String outputFilename = file;
				boolean doProceed = false;
				boolean xslFixed = false;
				int slashPos = file.lastIndexOf("/");
				switch (col) {
					case CSTR:
						outputFilename = outputFilename.replaceFirst("\\.gz$", ".txt");
						doProceed = true;
						break;
					case CST4:
						outputFilename = slashPos > -1 ? file.substring(slashPos + 1) : file;
					case CSTDEVA:
						xslFixed = true;
						doProceed = true;
						break;
					case SRT:
						outputFilename = file.replace("/", "_");
						doProceed = true;
						break;
					case BJT:
					case SC:
					case GRAM:
						outputFilename = slashPos > -1 ? file.substring(slashPos + 1) : file;
						doProceed = true;
						break;
				}
				if (doProceed) {
					final File outfile = new File(Utilities.OUTPUTPATH + targetScript.toLowerCase().substring(0, 3) + "_" + outputFilename);
					final String text = readFileContent(cp, file);
					if (!text.isEmpty()) {
						final String result = col == Corpus.Collection.BJT
												? ScriptTransliterator.translitBJT(text, engine, true)
												: col == Corpus.Collection.SC
													? ScriptTransliterator.translitSC(text, engine, true)
													: ScriptTransliterator.transliterate(text, engine, true, xslFixed);
						printLog("Writing out " + outfile.getPath());
						Utilities.saveText(result, outfile, cp.getEncoding().getCharset());
					} else {
						printLog("Error: No data to be written");
					}
				} // end if
			} // end for
		} // end if
		final long endTime = System.currentTimeMillis();
		printTime(endTime - startTime);
	}

	private static String readFileContent(final Corpus corpus, final String filename) throws IOException {
		String result = "";
		final Corpus.Collection col = corpus.getCollection();
		switch (col) {
			case CSTR:
				final File targetFile = new File(Utilities.ROOTDIR + ReaderUtilities.TEXTPATH + corpus.getRootName() + File.separator + filename);
				result = ReaderUtilities.readGz(targetFile, corpus.getEncoding().getCharset());
				break;
			case CSTDEVA:
				final Path targetPath = Path.of(Utilities.ROOTDIR + ReaderUtilities.TEXTPATH + corpus.getRootName() + File.separator + filename);
				result = Files.readString(targetPath, corpus.getEncoding().getCharset());
				break;
			case CST4:
			case BJT:
			case SRT:
			case SC:
			case GRAM:
				final String entryName = Utilities.findZipEntryName(corpus.getZipFile(), filename);
				result = ReaderUtilities.readTextFromZip(entryName, corpus);
				break;
		}
		return result;
	}

}
