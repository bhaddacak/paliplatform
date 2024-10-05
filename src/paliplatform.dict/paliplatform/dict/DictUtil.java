/*
 * DictUtil.java
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
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see https://www.gnu.org/licenses/.
 */

package paliplatform.dict;

import paliplatform.base.*;

import java.util.*;
import java.util.stream.*;
import java.util.zip.*;
import java.io.*;
import java.nio.file.*;
import java.nio.charset.StandardCharsets;
import java.net.*;
import com.google.gson.stream.*;

/** 
 * The CLI tool for the dictionary module.
 * This consists of static factory methods.
 * The tool can be invoked by this command line from the program's root dir:
 * $ java -p modules -m paliplatform.dict/paliplatform.dict.DictUtil
 *
 * @author J.R. Bhaddacak
 * @version 3.0
 * @since 3.0
 */
final public class DictUtil {
	private static final String LINESEP = System.getProperty("line.separator");
	private static final File propFile = new File(UrlProperties.URL_PROPS_FILE);
	private static Properties urlProps = new Properties();

	private DictUtil() {
	}

	public static void main(final String[] args) throws Exception {
		if (args.length == 0) {
			printHelpAndExit();
		}
		String opt = "";
		switch (args[0]) {
			case "list":
				printUrls();
				break;
			case "get":
				opt = args.length > 1 ? args[1] : "";
				if (opt.equals("-n")) {
					getNcped(true);
				} else if (opt.equals("-nx")) {
					getNcped(false);
				} else if (opt.equals("-p")) {
					getPtsped(true);
				} else if (opt.equals("-px")) {
					getPtsped(false);
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
		help.append(LINESEP).append("Pāli Platform Dict Util CLI").append(LINESEP);
		help.append("  Usage:").append(LINESEP);
		help.append("    DictUtil <command> <option>").append(LINESEP);
		help.append("      (The abstract DictUtil can be a launcher script,").append(LINESEP);
		help.append("       such as dictutil.sh or dictutil.cmd which can be").append(LINESEP);
		help.append("       found in the program's root directory.").append(LINESEP);
		help.append("       See also Notes below.)").append(LINESEP);
		help.append("  Commands:").append(LINESEP);
		help.append("    list\tList download URLs").append(LINESEP);
		help.append("    get\tDownload a dictionary").append(LINESEP);
		help.append("        Get options:").append(LINESEP);
		help.append("        -n\tDownload NCPED and make its CSV").append(LINESEP);
		help.append("        -nx\tMake NCPED CSV (skip download if the file exists)").append(LINESEP);
		help.append("        -p\tDownload PTSPED and make its CSV").append(LINESEP);
		help.append("        -px\tMake PTSPED CSV (skip download if the file exists)").append(LINESEP);
		help.append("    <none>\tShow this help").append(LINESEP);
		help.append("  Notes:").append(LINESEP);
		help.append("    To invoke the program, the Java convention has to be used.").append(LINESEP);
		help.append("    to do when you are at the program's root directory:").append(LINESEP);
		help.append("    Typically, if no launcher script available, this is the way ").append(LINESEP);
		help.append("    $ java -p modules -m paliplatform.dict/paliplatform.dict.DictUtil").append(LINESEP);
		printLog(help.toString());
		System.exit(0);
	}

	private static void printLog(final String mess) {
		System.out.println(mess);
	}

	private static void printTime(final long msec) {
		printLog(String.format("Done in %.3f seconds", msec/1000.0));
	}

	private static void initUrl() throws IOException {
		checkUrlProp();
		urlProps.load(Files.newInputStream(propFile.toPath()));
	}

	private static void download(final String url, final File target) throws IOException {
		try (final InputStream in = new URL(url).openStream()) {
			Files.copy(in, target.toPath(), StandardCopyOption.REPLACE_EXISTING);
		}
	}

	private static void checkUrlProp() throws IOException {
		if (!propFile.exists()) {
			download(UrlProperties.MAIN_URL + UrlProperties.URL_PROPS_FILE, propFile);
		}
	}

	private static void printUrls() throws IOException {
		initUrl();
		printLog("NCPED: " + urlProps.getProperty("ncped_url"));
		printLog("PTSPED: " + urlProps.getProperty("ptsped_url"));
	}

	private static void getNcped(final boolean forceDl) throws Exception {
		final long startTime = System.currentTimeMillis();
		Utilities.initializeComparator();
		initUrl();
		final String ncpedUrl = urlProps.getProperty("ncped_url");
		final File ncpedTarget = new File(Utilities.CACHEPATH + "ncped.json");
		if (!ncpedTarget.exists() || forceDl) {
			printLog("Downloading... (please wait)");
			download(ncpedUrl, ncpedTarget);
		}
		printLog("Converting to CSV...");
		makeNcpedCsv(ncpedTarget);
		final long endTime = System.currentTimeMillis();
		printTime(endTime - startTime);
	}

	private static void makeNcpedCsv(final File ncpedJson) throws IOException {
		if (!ncpedJson.exists()) return;
		final List<NcpedEntry> termList = new ArrayList<>();
		// read from json using stream
		final InputStream in = Files.newInputStream(ncpedJson.toPath());
		final JsonReader reader = new JsonReader(new InputStreamReader(in, StandardCharsets.UTF_8));
		try {
			reader.beginArray();
			while (reader.hasNext()) {
				NcpedEntry entry = null;
				reader.beginObject();
				while (reader.hasNext()) {
					final String name = reader.nextName();
					if (name.equals("entry")) {
						entry = new NcpedEntry(reader.nextString().trim());
					} else if (name.equals("homonyms")) {
						entry.homonyms.addAll(readHomonyms(reader));
					} else if (name.equals("grammar")) {
						entry.detail.grammar = reader.nextString().trim();
					} else if (name.equals("definition")) {
						if (reader.peek() == JsonToken.BEGIN_ARRAY) {
							entry.detail.definitions.addAll(readStringArray(reader));
						} else {
							entry.detail.definitions.add(reader.nextString().trim());
						}
					} else if (name.equals("xr")) {
						if (reader.peek() == JsonToken.BEGIN_ARRAY) {
							entry.detail.xrs.addAll(readStringArray(reader));
						} else {
							entry.detail.xrs.add(reader.nextString().trim());
						}
					} else {
						reader.skipValue();
					}
				}
				reader.endObject();
				if (entry != null)
					termList.add(entry);
			}
			reader.endArray();
		} finally {
			reader.close();
		}
		// sort term and generate CSV
		termList.sort((a, b) -> Utilities.paliCollator.compare(a.term, b.term));
		final List<String[]> csvOut = new ArrayList<>();
		csvOut.add(new String[] { "id", "term", "grammar" ,"definition", "xr" });
		int count = 0;
		for (final NcpedEntry entry : termList) {
			if (entry.term.startsWith("-")) continue;
			if (entry.homonyms.isEmpty()) {
				count++;
				final String[] line = new String[5];
				line[0] = "" + count;
				line[1] = entry.term;
				line[2] = entry.detail.grammar;
				line[3] = entry.detail.definitions.stream().collect(Collectors.joining(" | "));
				line[4] = entry.detail.xrs.stream().collect(Collectors.joining(", "));
				csvOut.add(line);
			} else {
				for (final EntryDetail d : entry.homonyms) {
					count++;
					final String[] line = new String[5];
					line[0] = "" + count;
					line[1] = entry.term;
					line[2] = d.grammar;
					line[3] = d.definitions.stream().collect(Collectors.joining(" | "));
					line[4] = d.xrs.stream().collect(Collectors.joining(", "));
					csvOut.add(line);
				}
			}
		}
		final File ncpedCsv = new File(DictUtilities.DICTPATH + "ncped.csv");
		Utilities.saveCSV(csvOut, ncpedCsv);
		printLog("Writng out " + ncpedCsv.getPath());
	}

	private static List<EntryDetail> readHomonyms(final JsonReader reader) throws IOException {
		final List<EntryDetail> result = new ArrayList<>();
		reader.beginArray();
		while (reader.hasNext()) {
			result.add(readHomoDetail(reader));
		}
		reader.endArray();
		return result;
	}

	private static EntryDetail readHomoDetail(final JsonReader reader) throws IOException {
		final EntryDetail result = new EntryDetail();
		reader.beginObject();
		while (reader.hasNext()) {
			final String name = reader.nextName();
			if (name.equals("grammar")) {
				result.grammar = reader.nextString().trim();
			} else if (name.equals("definition")) {
				if (reader.peek() == JsonToken.BEGIN_ARRAY)
					result.definitions.addAll(readStringArray(reader));
				else
					result.definitions.add(reader.nextString().trim());
			} else if (name.equals("xr")) {
				if (reader.peek() == JsonToken.BEGIN_ARRAY)
					result.xrs.addAll(readStringArray(reader));
				else
					result.xrs.add(reader.nextString().trim());
			} else {
				reader.skipValue();
			}
		}
		reader.endObject();
		return result;
	}

	private static List<String> readStringArray(final JsonReader reader) throws IOException {
		final List<String> result = new ArrayList<>();
		reader.beginArray();
		while (reader.hasNext()) {
			result.add(reader.nextString().trim());
		}
		reader.endArray();
		return result;
	}

	private static void getPtsped(final boolean forceDl) throws Exception {
		final long startTime = System.currentTimeMillis();
		Utilities.initializeComparator();
		initUrl();
		final String ptspedUrl = urlProps.getProperty("ptsped_url");
		final File ptspedTarget = new File(Utilities.CACHEPATH + "ptsped.zip");
		if (!ptspedTarget.exists() || forceDl) {
			printLog("Downloading... (please wait)");
			download(ptspedUrl, ptspedTarget);
		}
		printLog("Converting to CSV...");
		makePtspedCsv(ptspedTarget);
		final long endTime = System.currentTimeMillis();
		printTime(endTime - startTime);
	}

	private static void makePtspedCsv(final File ptspedZip) throws IOException {
		if (!ptspedZip.exists()) return;
		final ZipFile zip = new ZipFile(ptspedZip);
		final List<StringPair> termList = new ArrayList<>();
		for (final Enumeration<? extends ZipEntry> e = zip.entries(); e.hasMoreElements();) {
			final ZipEntry entry = e.nextElement();
			final String fname = entry.getName();
			if (fname.startsWith("PTSPED")) {
				try (final Scanner in = new Scanner(zip.getInputStream(entry), StandardCharsets.UTF_8)) {
					while (in.hasNextLine()) {
						final String line = in.nextLine().trim();
						if (line.isEmpty() || line.startsWith("0")) continue;
						final int tabPos = line.indexOf("\t");
						if (tabPos > -1) {
							final StringPair pair = new StringPair(line.substring(0, tabPos), line.substring(tabPos + 1));
							termList.add(pair);
						}
					}
				}
				break;
			}
		}
		zip.close();
		termList.sort((a, b) -> Utilities.paliCollator.compare(a.getFirst(), b.getFirst()));
		final List<String[]> csvOut = new ArrayList<>();
		csvOut.add(new String[] { "id", "term", "meaning" });
		int count = 0;
		for (final StringPair entry : termList) {
			count++;
			final String[] line = new String[3];
			line[0] = "" + count;
			line[1] = entry.getFirst();
			line[2] = entry.getSecond();
			csvOut.add(line);
		}
		final File ptspedCsv = new File(DictUtilities.DICTPATH + "ptsd.csv");
		Utilities.saveCSV(csvOut, ptspedCsv);
		printLog("Writng out " + ptspedCsv.getPath());
	}

	// inner classes
	static class NcpedEntry {
		final String term;
		final List<EntryDetail> homonyms = new ArrayList<>();
		final EntryDetail detail = new EntryDetail();
		NcpedEntry(final String word) {
			term = word;
		}
	}

	static class EntryDetail {
		String grammar = "";
		final List<String> definitions = new ArrayList<>();
		final List<String> xrs = new ArrayList<>();
	}

}
