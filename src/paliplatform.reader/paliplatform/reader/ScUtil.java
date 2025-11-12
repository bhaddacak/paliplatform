/*
 * ScUtil.java
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

import java.util.*;
import java.util.stream.*;
import java.util.zip.*;
import java.io.*;
import java.nio.file.*;
import java.nio.charset.StandardCharsets;

/** 
 * The CLI tool for SuttaCentral data.
 * This consists of static factory methods.
 * The tool can be invoked by this command line from the program's root dir:
 * $ java -p modules -m paliplatform.reader/paliplatform.reader.ScUtil
 *
 * @author J.R. Bhaddacak
 * @version 3.3
 * @since 3.0
 */
final public class ScUtil {
	enum ListOption { FULL, ONLY_DIR, MS }
	private static final String LINESEP = System.getProperty("line.separator");
	private static final File scData = new File(ReaderUtilities.SCPATH + ReaderUtilities.BILARA_DATA);

	private ScUtil() {
	}

	public static void main(final String[] args) throws Exception {
		if (args.length == 0) {
			printHelpAndExit();
		}
		String opt = "";
		switch (args[0]) {
			case "show":
				opt = args.length > 1 ? args[1] : "-v";
				if (opt.equals("-v")) {
					if (verify())
						printLog("SuttaCentral Bilara data available");
				} else if (opt.equals("-c")) {
					printLog("Analyzing...");
					printLog(analyzeChars());
				} else if (opt.equals("-t")) {
					final String docId = args.length > 2 ? args[2].toLowerCase() : "mn1";
					showText(docId, false);
				} else if (opt.equals("-tm")) {
					final String docId = args.length > 2 ? args[2].toLowerCase() : "mn1";
					showText(docId, true);
				} else {
					printHelpAndExit();
				}
				break;
			case "list":
				opt = args.length > 1 ? args[1] : "-d";
				if (opt.equals("-d")) {
					printList(ListOption.ONLY_DIR);
				} else if (opt.equals("-f")) {
					printList(ListOption.FULL);
				} else if (opt.equals("-m")) {
					printList(ListOption.MS);
				} else if (opt.equals("-i")) {
					final String nikaya = args.length > 2 ? args[2].toLowerCase() : "dn";
					listId(nikaya);
				} else {
					printHelpAndExit();
				}
				break;
			case "find":
				opt = args.length > 1 ? args[1] : "";
				if (opt.equals("-c")) {
					final String toFind = args.length > 2 ? args[2] : "";
					if (!toFind.isEmpty()) {
						printLog(findChar(CpUtil.makeProperQuery(toFind)));
					}
				} else {
					printHelpAndExit();
				}
				break;
			case "save":
				opt = args.length > 1 ? args[1] : "-h";
				if (opt.equals("-h"))
					saveHead();
				else if (opt.equals("-c"))
					analyzeCharsAndSave();
				else
					printHelpAndExit();
				break;
			default:
				printHelpAndExit();
		}
	}

	private static void printHelpAndExit() {
		final StringBuilder help = new StringBuilder();
		help.append(LINESEP).append("Pāli Platform SuttaCentral Util CLI").append(LINESEP);
		help.append("  Usage:").append(LINESEP);
		help.append("    ScUtil <command> <option>").append(LINESEP);
		help.append("      (The abstract ScUtil can be a launcher script,").append(LINESEP);
		help.append("       such as scutil.sh or scutil.cmd which can be").append(LINESEP);
		help.append("       found in the program's root directory.").append(LINESEP);
		help.append("       See also Notes below.)").append(LINESEP);
		help.append("  Commands:").append(LINESEP);
		help.append("    show\tShow various information").append(LINESEP);
		help.append("        Show options:").append(LINESEP);
		help.append("        -t <id>\tShow Pāli text identified by <id>").append(LINESEP);
		help.append("        -tm <id>").append(LINESEP);
		help.append("          \tShow Pāli text using ṃ in place of ṁ").append(LINESEP);
		help.append("        -c\tShow character stat in Pāli texts").append(LINESEP);
		help.append("        -v\tVerify whether the data file available").append(LINESEP);
		help.append("    list\tList things").append(LINESEP);
		help.append("        List options:").append(LINESEP);
		help.append("        -i <nikaya>").append(LINESEP);
		help.append("          \tList text ids where <nikaya> can be").append(LINESEP);
		help.append("          \tvin, dn, mn, sn, an, kn, ab, or abh").append(LINESEP);
		help.append("        -d\tList only directories").append(LINESEP);
		help.append("        -f\tList all files").append(LINESEP);
		help.append("        -m\tList only Pāli ms files").append(LINESEP);
		help.append("    find\tFind certain information").append(LINESEP);
		help.append("        Find options:").append(LINESEP);
		help.append("        -c <ch>\tFind a character in Pāli texts").append(LINESEP);
		help.append("    save\tAnalyze and save data").append(LINESEP);
		help.append("        Save options:").append(LINESEP);
		help.append("        -c\tAnalyze character stat in Pāli texts and save").append(LINESEP);
		help.append("        -h\tSave Pāli text headings").append(LINESEP);
		help.append("    <none>\tShow this help").append(LINESEP);
		help.append("  Examples:").append(LINESEP);
		help.append("    1. To find how many Pāli ms files, use this (Linux/macOS):").append(LINESEP);
		help.append("       $ ScUtil list -m | wc -l").append(LINESEP);
		help.append("       $ ScUtil list -m | grep '/abhidhamma/' | wc -l").append(LINESEP);
		help.append("    2. To save all Pāli text heads into a file, use this:").append(LINESEP);
		help.append("       $ ScUtil save -h").append(LINESEP);
		help.append("    3. To find whether 'f' is unexpectedly used in the texts, use this:").append(LINESEP);
		help.append("       $ ScUtil find -c f").append(LINESEP);
		help.append("       (This command will show at most 20 results)").append(LINESEP);
		help.append("    4. To see the text of MN1, use this:").append(LINESEP);
		help.append("       $ ScUtil show -t mn1").append(LINESEP);
		help.append("  Notes:").append(LINESEP);
		help.append("    To invoke the program, the Java convention has to be used.").append(LINESEP);
		help.append("    At the program's root directory, if no launcher script available,").append(LINESEP);
		help.append("    type this at the console: ").append(LINESEP);
		help.append("    $ java -p modules -m paliplatform.reader/paliplatform.reader.ScUtil").append(LINESEP);
		printLog(help.toString());
		System.exit(0);
	}

	private static void printLog(final String mess) {
		System.out.println(mess);
	}

	private static void printTime(final long msec) {
		printLog(String.format("Done in %.3f seconds", msec/1000.0));
	}

	private static boolean verify() {
		final boolean success = scData.exists();
		if (!success) {
			printLog("SuttaCentral Bilara data not found");
		}
		return success;
	}

	private static void printList(final ListOption opt) throws IOException {
		if (!verify()) return;
		final ZipFile zip = new ZipFile(scData);
		for (final Enumeration<? extends ZipEntry> e = zip.entries(); e.hasMoreElements();) {
			final ZipEntry entry = e.nextElement();
			final String fname = entry.getName();
			if (opt == ListOption.ONLY_DIR) {
				if (fname.endsWith("/"))
					printLog(fname);
			} else if (opt == ListOption.MS) {
				if (fname.endsWith(ScDocument.ROOT_PLI_MS_END) && fname.indexOf("playground") == -1)
					printLog(fname);
			} else {
				printLog(fname);
			}
		}
		zip.close();
	}

	private static void saveHead() {
		if (!verify()) return;
		final long startTime = System.currentTimeMillis();
		printLog("Reading data...");
		final String data = ReaderUtilities.getScHeads();
		final File outfile = new File(ReaderUtilities.SC_HEADS);
		printLog("Writing out " + outfile.getName());
		Utilities.saveText(data, outfile);
		final long endTime = System.currentTimeMillis();
		printTime(endTime - startTime);
	}

	private static String getNikayaIden(final String nikAbbr) {
		String result = "";
		switch (nikAbbr) {
			case "v":
			case "vi":
			case "vin":
				result = "/vinaya/";
				break;
			case "d":
			case "dn":
				result = "/dn/";
				break;
			case "m":
			case "mn":
				result = "/mn/";
				break;
			case "s":
			case "sn":
				result = "/sn/";
				break;
			case "a":
			case "an":
				result = "/an/";
				break;
			case "k":
			case "kn":
				result = "/kn/";
				break;
			case "ab":
			case "abh":
				result = "/abhidhamma/";
				break;
		}
		return result;
	}

	private static void listId(final String nikaya) throws IOException {
		final String nikIden = getNikayaIden(nikaya);
		if (nikIden.isEmpty()) return;
		final List<String> result = new ArrayList<>();
		final ZipFile zip = new ZipFile(scData);
		for (final Enumeration<? extends ZipEntry> e = zip.entries(); e.hasMoreElements();) {
			final ZipEntry entry = e.nextElement();
			final String fname  = entry.getName();
			if (fname.endsWith(ScDocument.ROOT_PLI_MS_END)
					&& fname.indexOf("playground") == -1 
					&& fname.indexOf(nikIden) > -1) {
				String textId = fname.substring(fname.lastIndexOf("/") + 1);
				textId = textId.substring(0, textId.lastIndexOf("_"));
				result.add(textId);
			}
		}
		zip.close();
		result.sort(ScInfo.scComparator);
		printLog(result.stream().collect(Collectors.joining(LINESEP)));
	}

	private static String analyzeChars() throws IOException {
		return analyzeChars(scData);
	}

	public static String analyzeChars(final File scFile) throws IOException {
		final Map<Character, Integer> charFreqMap = new HashMap<>();
		final ZipFile zip = new ZipFile(scFile);
		for (final Enumeration<? extends ZipEntry> e = zip.entries(); e.hasMoreElements();) {
			final ZipEntry entry = e.nextElement();
			final String fname  = entry.getName();
			if (fname.endsWith(ScDocument.ROOT_PLI_MS_END) && fname.indexOf("playground") == -1) {
				final Map<String, String> textMap = ReaderUtilities.readJsonObject(zip.getInputStream(entry));
				for (final String text : textMap.values()) {
					final char[] cArr = text.toCharArray();
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

	private static void analyzeCharsAndSave() throws IOException {
		if (!verify()) return;
		final Path outputPath = Path.of(Utilities.ROOTDIR + Utilities.OUTPUTPATH);
		if (Files.notExists(outputPath))
			Files.createDirectories(outputPath);
		final long startTime = System.currentTimeMillis();
		printLog("Analyzing data...");
		final String result = analyzeChars();
		final File outfile = new File(Utilities.OUTPUTPATH + "sc-charstat.txt");
		printLog("Writing out " + outfile.getPath());
		Utilities.saveText(result, outfile);
		final long endTime = System.currentTimeMillis();
		printTime(endTime - startTime);
	}

	private static String findChar(final String query) throws IOException {
		return findChar(scData, query);
	}

	public static String findChar(final File scFile, final String query) throws IOException {
		final Map<String, Long> fileMap = new HashMap<>();
		final ZipFile zip = new ZipFile(scData);
		int max = 20;
		for (final Enumeration<? extends ZipEntry> e = zip.entries(); e.hasMoreElements();) {
			if (max <= 0) break;
			final ZipEntry entry = e.nextElement();
			final String fname  = entry.getName();
			if (fname.endsWith(ScDocument.ROOT_PLI_MS_END) && fname.indexOf("playground") == -1) {
				long foundCount = 0;
				try (final Scanner in = new Scanner(zip.getInputStream(entry), StandardCharsets.UTF_8)) {
					foundCount = in.findAll(query).count();
					if (foundCount > 0) {
						fileMap.put(fname.substring(fname.lastIndexOf("/") + 1), foundCount);
						max--;
					}
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

	private static void showText(final String id, final boolean useMDotBelow) throws IOException {
		Map<String, String> textMap = null;
		final ZipFile zip = new ZipFile(scData);
		for (final Enumeration<? extends ZipEntry> e = zip.entries(); e.hasMoreElements();) {
			final ZipEntry entry = e.nextElement();
			final String fname  = entry.getName();
			if (fname.endsWith(id + ScDocument.ROOT_PLI_MS_END)) {
				textMap = ReaderUtilities.getScData(fname);
				break;
			}
		}
		zip.close();
		if (textMap != null) {
			final String result = textMap.entrySet().stream()
									.map(x -> x.getKey() + " " + 
											(useMDotBelow ? Utilities.normalizeNiggahita(x.getValue(), true) : x.getValue()))
									.collect(Collectors.joining(LINESEP));
			printLog(result);
		}
	}

}

