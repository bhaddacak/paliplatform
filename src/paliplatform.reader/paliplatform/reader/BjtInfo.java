/*
 * BjtInfo.java
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

/** 
 * The information class for a document in the collection
 * of the Buddha Jayanti Tipitaka.
 * @author J.R. Bhaddacak
 * @version 3.0
 * @since 3.0
 */

public final class BjtInfo extends SimpleDocumentInfo {
	
	public BjtInfo(final Corpus corpus, final String idStr) {
		super(corpus, idStr);
	}

	private boolean isDMSA() {
		final char ch = id.charAt(0);
		final char num = id.charAt(1);
		return group.equals("sut") && "dmsa".indexOf(ch) > -1 && Character.isDigit(num);
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
		String result = ".+";
		if (tgAbbrs.length == 1) {
			switch (tgAbbrs[0]) {
				case "vin":
					result = "(?:para|pac|biv|mahav|cullav|pari).+";
					break;
				case "sut":
					result = "(?:di|maj|sam|an|ap|bud|ca|cullan|dhamm|it|ja|kh|ni|mahan|pati|pe|su|th|ud|vim).+";
					break;
				case "abh":
					result = "(?:dhams|dhat|ka|patt|pu|vib|ya).+";
					break;
				case "dmsa":
					result = "(?:di|maj|sam|an).+";
					break;
				case "vdmsa":
					result = "(?:para|pac|biv|mahav|cullav|pari|di|maj|sam|an).+";
					break;
			}
		}
		return result + "\\.htm";
	}

}

