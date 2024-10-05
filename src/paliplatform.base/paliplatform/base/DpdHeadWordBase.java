/*
 * DpdHeadWordBase.java
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

package paliplatform.base;

/** 
 * The core respresentation of a head word in DPD dictionary.
 * @author J.R. Bhaddacak
 * @version 3.0
 * @since 3.0
 */
public class DpdHeadWordBase {
	private final String lemma1;
	private String grammar = "";
	private String verb = "";
	private String trans = "";
	private String plusCase = "";
	private String meaning1 = "";
	private String meaning2 = "";
	private String meaningLit = "";
	private String sanskrit = "";
	private String rootKey = "";
	private String construction = "";

	public DpdHeadWordBase(final String term) {
		lemma1 = term;
	}

	public String getTerm() {
		return lemma1;
	}

	public void setGrammar(final String text) {
		grammar = text;
	}

	public String getGrammar() {
		return grammar;
	}

	public void setVerb(final String text) {
		verb = text;
	}

	public String getVerb() {
		return verb;
	}

	public void setTrans(final String text) {
		trans = text;
	}

	public String getTrans() {
		return trans;
	}

	public void setPlusCase(final String text) {
		plusCase = text;
	}

	public String getPlusCase() {
		return plusCase;
	}

	public void setMeaning1(final String text) {
		meaning1 = text;
	}

	public String getMeaning1() {
		return meaning1;
	}

	public void setMeaning2(final String text) {
		meaning2 = text;
	}

	public String getMeaning2() {
		return meaning2;
	}

	public void setMeaningLit(final String text) {
		meaningLit = text;
	}

	public String getMeaningLit() {
		return meaningLit;
	}

	public void setSanskrit(final String text) {
		sanskrit = text;
	}

	public String getSanskrit() {
		return sanskrit;
	}

	public void setRootKey(final String text) {
		rootKey = text;
	}

	public String getRootKey() {
		return rootKey;
	}

	public void setConstruction(final String text) {
		construction = text.replaceAll("\n", " | ");
	}

	public String getConstruction() {
		return construction;
	}

	public static boolean hasData(final String data) {
		return !(data.isEmpty() || data.equals("-") || data.equals("*"));
	}

}

