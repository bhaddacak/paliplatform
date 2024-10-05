/*
 * ScInfo.java
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

package paliplatform.reader;

import paliplatform.base.*;

import java.util.*;
import java.util.regex.*;

/** 
 * The information class for a document in the collection
 * of SuttaCentral.
 * @author J.R. Bhaddacak
 * @version 3.0
 * @since 3.0
 */

public final class ScInfo extends SimpleDocumentInfo {
	private static final List<String> abbrList = List.of(
	"pli-tv-bu-vb-pj", "pli-tv-bu-vb-ss", "pli-tv-bu-vb-ay", "pli-tv-bu-vb-np",
	"pli-tv-bu-vb-pc", "pli-tv-bu-vb-pd", "pli-tv-bu-vb-sk", "pli-tv-bu-vb-as",
	"pli-tv-bi-vb-pj", "pli-tv-bi-vb-ss", "pli-tv-bi-vb-np", "pli-tv-bi-vb-pc",
	"pli-tv-bi-vb-pd", "pli-tv-bi-vb-sk", "pli-tv-bi-vb-as",
	"pli-tv-kd", "pli-tv-pvr", "pli-tv-bu-pm", "pli-tv-bi-pm",
	"dn", "mn", "sn", "an", "kp", "dhp", "ud", "iti", "snp", "vv", "pv", "thag", "thig",
	"tha-ap", "thi-ap", "bv", "cp", "ja", "mnd", "cnd", "ps", "mil", "ne", "pe",
	"ds", "vb", "dt", "pp", "kv", "ya", "patthana");
	public static final Comparator<String> scComparator = new Comparator<String>() {
		private final Pattern patt = Pattern.compile("(\\D+)([0-9.]*)");
		@Override
		public int compare(final String id1, final String id2) {
			String idA = id1.matches(".*-\\d+$") ? id1.substring(0, id1.lastIndexOf("-")) : id1;
			final Matcher aMatcher = patt.matcher(idA);
			String idB = id2.matches(".*-\\d+$") ? id2.substring(0, id2.lastIndexOf("-")) : id2;
			final Matcher bMatcher = patt.matcher(idB);
			int result = 0;
			if (aMatcher.find() && bMatcher.find()) {
				result = abbrList.indexOf(aMatcher.group(1)) - abbrList.indexOf(bMatcher.group(1));
				if (result == 0) {
					final String[] aNums = aMatcher.group(2).split("\\.");
					final String[] bNums = bMatcher.group(2).split("\\.");
					result = aNums.length - bNums.length;
					if (aNums.length > 0) {
						for (int i = 0; i < aNums.length; i++) {
							if (bNums.length > i) {
								result = Integer.compare(Integer.parseInt(aNums[i]), Integer.parseInt(bNums[i]));
								if (result == 0) {
									continue;
								} else {
									return result;
								}
							} else {
								result = 1;
								break;
							}
						}
					}
				}
			} else {
				return result;
			}
			return result;
		}
	};
	private static final List<String> dmsaList = List.of("dn", "mn", "sn", "an");

	public ScInfo(final Corpus corpus, final String idStr) {
		super(corpus, idStr);
	}

	public static ScDocument.Nikaya getNikaya(final String id) {
		final String abbr = id.substring(0, Utilities.getFirstDigitPos(id));
		final int pos = abbrList.indexOf(abbr);
		final ScDocument.Nikaya result;
		if (pos >= 0 && pos <= 18) {
			result = ScDocument.Nikaya.VIN;
		} else if (pos >= 19 && pos <= 22) {
			result = ScDocument.Nikaya.valueOf(abbr.toUpperCase());
		} else if (pos >= 23 && pos <= 42) {
			result = ScDocument.Nikaya.KN;
		} else {
			result = ScDocument.Nikaya.ABH;
		}
		return result;
	}

	private boolean isDMSA() {
		final String twoCh = id.substring(0, 2);
		final char num = id.charAt(2);
		return group.equals("sut") && dmsaList.indexOf(twoCh) > -1 && Character.isDigit(num);
	}
	
	private boolean isVDMSA() {
		return (group.equals("vin") || isDMSA());
	}
	
	@Override
	public boolean isInTextGroup(final TextGroup tg) {
		final boolean result;
		final String[] tgAbbr = tg.getAbbrev().split(TextGroup.DELIM_REX);
		if (tgAbbr.length == 1) {
			result = tgAbbr[0].equals("all") ? true 
					: tgAbbr[0].equals("dmsa") ? isDMSA()
					: tgAbbr[0].equals("vdmsa") ? isVDMSA()
					: group.equals(tgAbbr[0]);
		} else {
			result = false;
		}
		return result;
	}

	public static String getFileFilterPatternString(final String textGroupAbbr) {
		final String[] tgAbbrs = textGroupAbbr.split(TextGroup.DELIM_REX);
		String result = ".+/(?:vinaya|sutta|abhidhamma)/.+";
		if (tgAbbrs.length == 1) {
			switch (tgAbbrs[0]) {
				case "vin":
					result = ".+/vinaya/.+";
					break;
				case "sut":
					result = ".+/sutta/.+";
					break;
				case "abh":
					result = ".+/abhidhamma/.+";
					break;
				case "dmsa":
					result = ".+/sutta/(?:dn|mn|sn|an).+";
					break;
				case "vdmsa":
					result = ".+(?:/vinaya/|(?:/sutta/(?:dn|mn|sn|an)/)).+";
					break;
			}
		}
		return result + ScDocument.ROOT_PLI_MS_END;
	}

	@Override
	public TocTreeNode toTocTreeNode() {
		throw new UnsupportedOperationException();
	}

}




