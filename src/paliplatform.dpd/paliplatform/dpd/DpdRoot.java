/*
 * DpdRoot.java
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

package paliplatform.dpd;

import java.util.*;

/** 
 * The respresentation of a Pali root in DPD.
 * @author J.R. Bhaddacak
 * @version 3.0
 * @since 3.0
 */
class DpdRoot {
	public static enum RootGroup {
		I, II, III, IV, V, VI, VII, VIII;
		public static final RootGroup[] values = values();
		public static RootGroup fromNumber(final int num) {
			RootGroup result = RootGroup.I;
			for (RootGroup rg : RootGroup.values) {
				if (rg.ordinal() + 1 == num) {
					result = rg;
					break;
				}
			}
			return result;
		}
	}
	private final String root;
	private RootGroup group = RootGroup.I;
	private String rootSign = "";
	private String meaning = "";
	private String sanskritInfo = "";
	private String example = "";
	private String[] patha = new String[3];
	private String[] manjusa = new String[3];
	private String[] mala = new String[3];
	private String[] panini = new String[3];
	private String note = "";
	private String matrix = "";
	private Map<String, List<List<String>>> rootFamily = null;

	public DpdRoot(final String rname) {
		root = rname;
	}

	public String getRoot() {
		return root;
	}

	public void setGroup(final int num) {
		group = RootGroup.fromNumber(num);
	}

	public RootGroup getGroup() {
		return group;
	}

	public void setRootSign(final String text) {
		rootSign = text;
	}

	public String getRootSign() {
		return rootSign;
	}

	public void setMeaning(final String text) {
		meaning = text;
	}

	public String getMeaning() {
		return meaning;
	}

	public void setSanskritInfo(final String text) {
		if (!text.startsWith(" =") && !text.startsWith("- =")) 
			sanskritInfo = text;
	}

	public String getSanskritInfo() {
		return sanskritInfo;
	}

	public void setExample(final String text) {
		if (!text.equals("-")) 
			example = text;
	}

	public String getExample() {
		return example;
	}

	public void setPatha(final String[] texts) {
		for (int i = 0; i < 3; i++) {
			patha[i] = DpdUtilities.stripTags(texts[i]);
		}
	}

	public String[] getPatha() {
		return patha;
	}

	public void setManjusa(final String[] texts) {
		for (int i = 0; i < 3; i++) {
			manjusa[i] = DpdUtilities.stripTags(texts[i]);
		}
	}

	public String[] getManjusa() {
		return manjusa;
	}

	public void setMala(final String[] texts) {
		for (int i = 0; i < 3; i++) {
			mala[i] = DpdUtilities.stripTags(texts[i]);
		}
	}

	public String[] getMala() {
		return mala;
	}

	public void setPanini(final String[] texts) {
		for (int i = 0; i < 3; i++) {
			panini[i] = DpdUtilities.stripTags(texts[i]);
		}
	}

	public String[] getPanini() {
		return panini;
	}

	public void setNote(final String text) {
		if (!text.equals("-")) 
			note = text;
	}

	public String getNote() {
		return note;
	}

	public void setMatrix(final String text) {
		matrix = DpdUtilities.replaceTags(text, " ");
	}

	public void setRootFamily(final Map<String, List<List<String>>> rmap) {
		rootFamily = rmap;
	}

	public Map<String, List<List<String>>> getRootFamily() {
		return rootFamily;
	}

	public boolean contains(final String text) {
		boolean result = false;
		result = result || root.contains(text);
		result = result || meaning.contains(text);
		result = result || example.contains(text);
		result = result || sanskritInfo.contains(text);
		result = result || note.contains(text);
		result = result || matrix.contains(text);
		result = result || containsInArray(text, patha);
		result = result || containsInArray(text, manjusa);
		result = result || containsInArray(text, mala);
		result = result || containsInArray(text, panini);
		return result;
	}
	
	private static boolean containsInArray(final String text, final String[] arr) {
		boolean result = false;
		for (final String s : arr) {
			result = result || s.contains(text);
		}
		return result;
	}

}

