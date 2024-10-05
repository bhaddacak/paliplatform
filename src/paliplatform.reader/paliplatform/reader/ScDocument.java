/*
 * ScDocument.java
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

package paliplatform.reader;

import java.util.*;
import java.util.regex.*;

/** 
 * The representation of a Pāli document in SuttaCentral collection.
 * This is used mainly in ScReader. 
 *
 * @author J.R. Bhaddacak
 * @version 3.0
 * @since 3.0
 */
public class ScDocument {
	static enum Nikaya {
		VIN("Vinaya"), DN("Dīghanikāya"), MN("Majjhimanikāya"), SN("Saṁyuttanikāya"),
		AN("Aṅguttaranikāya"), KN("Khuddakanikāya"), ABH("Abhidhamma");
		private final String name;
		public static Nikaya[] values = values();
		private Nikaya(final String name) {
			this.name  = name;
		}
		public String getName() {
			return name;
		}
		public String getDir() {
			return this == VIN ? "vinaya/" : this == ABH ? "abhidhamma/" : "sutta/";
		}
	}
	private static final String ZIPROOT = "bilara-data-published/";
	public static final String ROOT_PLI_MS_END = "_root-pli-ms.json";
	private static final Set<String> havingVagga = Set.of("iti", "ud", "snp");
	private static final int[][] itiVagga = { {1, 10}, {11, 20}, {21, 27}, {28, 37}, {38, 49},
										{50, 59}, {60, 69}, {70, 79}, {80, 89}, {90, 99}, {100, 112} };
	private final String id;
	private final Nikaya nikaya;
	private final String dirPart;
	private final Map<String, String> pali; // Pāli text
	private final Map<String, String> html; // HTML structure
	private final Map<String, String> variant;
	private final Map<String, String> reference;
	private final Set<String> translatorSet;
	private final Set<String> transLangSet;
	private final Set<String> commentatorSet;
	private final Set<String> commentLangSet;

	ScDocument(final String code, final Nikaya nk) {
		id = code;
		nikaya = nk;
		dirPart = getDirPart();
		pali = ReaderUtilities.getScData(getPaliRootName());
		html = ReaderUtilities.getScData(getHtmlName());
		variant = ReaderUtilities.getScData(getVariantName());
		reference = ReaderUtilities.getScData(getReferenceName());
		final Map<String, Set<String>> transAuthLang = ReaderUtilities.getScAuthLang(id, "translation");
		translatorSet = transAuthLang.get("auth");
		transLangSet = transAuthLang.get("lang");
		final Map<String, Set<String>> commAuthLang = ReaderUtilities.getScAuthLang(id, "comment");
		commentatorSet = commAuthLang.get("auth");
		commentLangSet = commAuthLang.get("lang");
	}

	private String getDirPart() {
		String part = nikaya.getDir();
		if (nikaya == Nikaya.SN || nikaya == Nikaya.AN) {
			final int dotPos = id.indexOf(".");
			final String grp = id.substring(0, dotPos);
			part = part + nikaya.toString().toLowerCase() + "/" + grp + "/";
		} else if (nikaya == Nikaya.KN) {
			final Pattern patt = Pattern.compile("^(\\D+)(\\d+)");
			final Matcher matcher = patt.matcher(id);
			String grpPart = "";
			if (matcher.find()) {
				final String grp = matcher.group(1);
				if (havingVagga.contains(grp)) {
					int vaggaNum = Integer.parseInt(matcher.group(2));
					if (grp.equals("iti")) {
						final int num = vaggaNum;
						for (int i = 0; i < itiVagga.length; i++) {
							if (num >= itiVagga[i][0] && num <= itiVagga[i][1]) {
								vaggaNum = i + 1;
								break;
							}
						}
					}
					grpPart = grp + "/vagga" + vaggaNum + "/";
				} else {
					grpPart = grp + "/";
				}
			}
			part = part + nikaya.toString().toLowerCase() + "/" + grpPart;
		} else if (nikaya == Nikaya.ABH) {
			final Pattern patt = Pattern.compile("^(\\D+)(\\d+)");
			final Matcher matcher = patt.matcher(id);
			String grpPart = "";
			if (matcher.find()) {
				final String grp = matcher.group(1);
				final String num = matcher.group(2);
				grpPart = grp.equals("vb")
							? grp + "/"
							: grp + "/" + grp + num + "/";
			}
			part = part + grpPart;
		} else if (nikaya == Nikaya.VIN) {
			if (id.endsWith("-pm")) {
				part = part;
			} else {
				final Pattern patt = Pattern.compile("^(\\D+)");
				final Matcher matcher = patt.matcher(id);
				String grpPart = "";
				if (matcher.find()) {
					final String grp = matcher.group(1);
					if (grp.endsWith("-kd") || grp.endsWith("-pvr")) {
						grpPart = grp + "/";
					} else {
						final String superGrp = grp.substring(0, grp.lastIndexOf("-"));
						grpPart = grp.endsWith("-as")
							? superGrp + "/"
							: superGrp + "/" + grp + "/";
					}
				}
				part = part + grpPart;
			}
		} else {
			part = part + nikaya.toString().toLowerCase() + "/";
		}
		return part;
	}

	public String getPaliRootName() {
		return ZIPROOT + "root/pli/ms/" + dirPart + id + ROOT_PLI_MS_END;
	}

	public String getHtmlName() {
		return ZIPROOT + "html/pli/ms/" + dirPart + id + "_html.json";
	}

	public String getVariantName() {
		return ZIPROOT + "variant/pli/ms/" + dirPart + id + "_variant-pli-ms.json";
	}

	public String getReferenceName() {
		return ZIPROOT + "reference/pli/ms/" + dirPart + id + "_reference.json";
	}

	public String getWithAuthorNameAndLang(final String author, final String lang, final String type) {
		return ZIPROOT + type + "/" + lang + "/" + author + "/" + dirPart +
				id + "_" + type + "-" + lang + "-" + author + ".json";
	}

	public Map<String, String> getPali() {
		return pali;
	}

	public Map<String, String> getHtml() {
		return html;
	}

	public Map<String, String> getVariant() {
		return variant;
	}

	public Map<String, String> getReference() {
		return reference;
	}

	public Set<String> getTransLangs() {
		return transLangSet;
	}

	public Set<String> getCommentLangs() {
		return commentLangSet;
	}

	public Map<String, Map<String, String>> getTranslation(final String lang) {
		final Map<String, Map<String, String>> result = new HashMap<>();
		for (final String author : translatorSet) {
			result.put(author, ReaderUtilities.getScData(getWithAuthorNameAndLang(author, lang, "translation")));
		}
		return result;
	}

	public Map<String, Map<String, String>> getComment(final String lang) {
		final Map<String, Map<String, String>> result = new HashMap<>();
		for (final String author : commentatorSet) {
			result.put(author, ReaderUtilities.getScData(getWithAuthorNameAndLang(author, lang, "comment")));
		}
		return result;
	}

	public static String getTextName(final String id, final String headText) {
		final String info = headText.endsWith(";") ? headText.substring(0, headText.length() - 1) : headText;
		String result = info;
		final String[] items = info.split(";");
		final int offset = id.startsWith("dhp") ? 2 : 1;
		for (int n = items.length - offset; n >= 0; n--) {
			if (!items[n].isEmpty()) {
				if (id.startsWith("kd") && !items[n].endsWith("khandhaka")) {
					continue;
				} else {
					result = items[n];
					break;
				}
			}
		}
		return result;
	}

}

