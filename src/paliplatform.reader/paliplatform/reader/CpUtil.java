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

import java.util.*;
import java.util.stream.*;
import java.util.zip.*;
import java.util.regex.*;
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
 * @version 3.3
 * @since 3.3
 */
final public class CpUtil {
	private static final String LINESEP = System.getProperty("line.separator");
	private static final File scData = new File(ReaderUtilities.SCPATH + ReaderUtilities.BILARA_DATA);

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
				if (opt.equals("-f")) {
					showFullInfo();
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
				if (opt.equals("-c") && !param.isEmpty()) {
					analyzeCharsAndSave(param);
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
		help.append("        -f\tShow full information").append(LINESEP);
		help.append("        <none>\tShow brief information").append(LINESEP);
		help.append("    show\tShow some analyses").append(LINESEP);
		help.append("        Show options:").append(LINESEP);
		help.append("        -c <corpus>\tAnalyze character stat in <corpus>").append(LINESEP);
		help.append("    find\tFind certain information").append(LINESEP);
		help.append("        Find options:").append(LINESEP);
		help.append("        -c <corpus> <ch>\tFind a character in <corpus>").append(LINESEP);
		help.append("    save\tAnalyze and save data").append(LINESEP);
		help.append("        Save options:").append(LINESEP);
		help.append("        -c <corpus>\tAnalyze character stat in <corpus> and save").append(LINESEP);
		help.append("    <none>\tShow this help").append(LINESEP);
		help.append("  Examples:").append(LINESEP);
		help.append("    1. To list all corpora with full information:").append(LINESEP);
		help.append("       $ CpUtil list -f").append(LINESEP);
		help.append("    2. To show the character stat of SuttaCentra collection:").append(LINESEP);
		help.append("       $ CpUtil show -c sc").append(LINESEP);
		help.append("    3. To find whether 'f' is unexpectedly used in GRAM, use this:").append(LINESEP);
		help.append("       $ CpUtil find -c gram f").append(LINESEP);
		help.append("       (This command will show at most 20 results)").append(LINESEP);
		help.append("    4. To find whether '^' is unexpectedly present in CSTDEVA, use this:").append(LINESEP);
		help.append("       $ CpUtil find -c cstdeva '\\^'").append(LINESEP);
		help.append("       (Regular expression meta characters need to be escaped)").append(LINESEP);
		help.append("    5. To save the character stat of CSTDEVA collection:").append(LINESEP);
		help.append("       $ CpUtil save -c cstdeva").append(LINESEP);
		help.append("  Notes:").append(LINESEP);
		help.append("    To invoke the program, the Java convention has to be used.").append(LINESEP);
		help.append("    to do when you are at the program's root directory:").append(LINESEP);
		help.append("    Typically, if no launcher script available, this is the way ").append(LINESEP);
		help.append("    $ java -p modules -m paliplatform.reader/paliplatform.reader.CpUtil").append(LINESEP);
		help.append("    (For the SuttaCentral corpus, see also ScUtil.)").append(LINESEP);
		printLog(help.toString());
		System.exit(0);
	}

	private static void printLog(final String mess) {
		System.out.println(mess);
	}

	private static void printTime(final long msec) {
		printLog(String.format("Done in %.3f seconds", msec/1000.0));
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
		final Corpus.Collection col = Corpus.Collection.idMap.get(colStr);
		if (col == null) {
			printLog("Invalid corpus name");
			System.exit(1);
		}
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

	public static String makeProperQuery(final String toFind) {
		String ok = "";
		try {
			Pattern.compile(toFind);
		} catch (PatternSyntaxException e) {
			ok = "\\" + toFind.charAt(0);
		}
		return ok.isEmpty()
				? toFind.startsWith("\\") ? toFind.substring(0, 2) : toFind
				: ok;
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
		final Corpus.Collection col = Corpus.Collection.idMap.get(colStr);
		if (col == null) {
			printLog("Invalid corpus name");
			System.exit(1);
		}
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

}
