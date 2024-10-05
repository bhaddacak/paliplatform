/*
 * SrtInfo.java
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

import java.util.*;
import java.util.stream.*;

/** 
 * The information class for a document in the collection
 * of Siam Rath Tipitaka.
 * @author J.R. Bhaddacak
 * @version 3.0
 * @since 3.0
 */

public final class SrtInfo extends SimpleDocumentInfo {
	private String docClass = ""; // mul, exe (att, tik)
	private List<String> refList;
	
	public SrtInfo(final Corpus corpus, final String idStr) {
		super(corpus, idStr);
		refList = new ArrayList<>();
	}

	public void setDocClass(final String cls) {
		docClass = cls;
	}

	@Override
	public String getDocClass() {
		return docClass;
	}

	public void setRefList(final List<String> list) {
		refList.addAll(list);
		ref = refList.stream().collect(Collectors.joining(", "));
		refProperty().set(ref);
	}

	private boolean isDMSA() {
		final char ch = id.charAt(0);
		final char num = id.charAt(1);
		return group.equals("sut") && docClass.equals("mul") && "dmsa".indexOf(ch) > -1 && Character.isDigit(num);
	}
	
	private boolean isVDMSA() {
		return (docClass.equals("mul") && group.equals("vin")) || isDMSA();
	}
	
	@Override
	public boolean isInTextGroup(final TextGroup tg) {
		final boolean result;
		final String[] tgAbbr = tg.getAbbrev().split(TextGroup.DELIM_REX);
		if (tgAbbr.length == 1) {
			result = tgAbbr[0].equals("all") ? true
					: tgAbbr[0].equals("dmsa") ? isDMSA()
					: tgAbbr[0].equals("vdmsa") ? isVDMSA()
					: group.equals(tgAbbr[0]) || docClass.equals(tgAbbr[0]);
		} else if (tgAbbr.length == 2) {
			result = group.equals(tgAbbr[0]) && docClass.equals(tgAbbr[1]);
		} else {
			result = false;
		}
		return result;
	}

	@Override
	public DocumentInfo.SuttaGroup getSuttaGroup() {
		DocumentInfo.SuttaGroup result = DocumentInfo.SuttaGroup.XN;
		if (group.equals("sut")) {
			if (docClass.equals("mul")) {
				if (id.matches("d\\d+")) {
					result = DocumentInfo.SuttaGroup.DN;
				} else if (id.matches("m\\d+")) {
					result = DocumentInfo.SuttaGroup.MN;
				} else if (id.matches("s\\d+")) {
					result = DocumentInfo.SuttaGroup.SN;
				} else if (id.matches("a\\d+")) {
					result = DocumentInfo.SuttaGroup.AN;
				} else {
					result = DocumentInfo.SuttaGroup.KN;
				}
			} else if (docClass.equals("att")) {
				if (id.matches("d-a\\d+")) {
					result = DocumentInfo.SuttaGroup.DN;
				} else if (id.matches("m-a\\d+")) {
					result = DocumentInfo.SuttaGroup.MN;
				} else if (id.matches("s-a\\d+")) {
					result = DocumentInfo.SuttaGroup.SN;
				} else if (id.matches("a-a\\d+")) {
					result = DocumentInfo.SuttaGroup.AN;
				} else {
					result = DocumentInfo.SuttaGroup.KN;
				}
			}
		}
		return result;
	}

	public static String getFileFilterPatternString(final String textGroupAbbr) {
		final String[] tgAbbrs = textGroupAbbr.split(TextGroup.DELIM_REX);
		String result = ".+";
		if (tgAbbrs.length == 1) {
			switch (tgAbbrs[0]) {
				case "vin":
					result = "(?:(?:pali/0[1-8])|(?:attha/0[1-3]))";
					break;
				case "sut":
					result = "(?:(?:pali/(?:09|[1-2][0-9]|3[0-3]))|(?:attha/(?:0[4-9]|[1-3][0-9]|4[0-5])))";
					break;
				case "abh":
					result = "(?:(?:pali/(?:3[4-9]|4[0-5]))|(?:attha/4[6-8]))";
					break;
				case "dmsa":
					result = "(?:pali/(?:09|1[0-9]|2[0-4]))";
					break;
				case "vdmsa":
					result = "(?:pali/(?:0[1-9]|1[0-9]|2[0-4]))";
					break;
				case "mul":
					result = "pali.+";
					break;
				case "att":
					result = "attha.+";
					break;
			}
		} else if (tgAbbrs.length == 2) {
			if (tgAbbrs[1].equals("mul")) {
				result = "pali/";
				switch (tgAbbrs[0]) {
					case "vin":
						result = result + "0[1-8]";
						break;
					case "sut":
						result = result + "(?:09|[1-2][0-9]|3[0-3])";
						break;
					case "abh":
						result = result + "(?:3[4-9]|4[0-5])";
						break;
				}
			} else if (tgAbbrs[1].equals("att")) {
				result = "attha/";
				switch (tgAbbrs[0]) {
					case "vin":
						result = result + "0[1-3]";
						break;
					case "sut":
						result = result + "(?:0[4-9]|[1-3][0-9]|4[0-5])";
						break;
					case "abh":
						result = result + "(?:4[6-8])";
						break;
				}
			}
		}
		return result + "\\.txt";
	}

}

