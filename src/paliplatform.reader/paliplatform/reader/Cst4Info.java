/*
 * Cst4Info.java
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

import java.util.*;
import java.util.stream.*;
import javafx.beans.property.*;

/** 
 * The information class for a document in the collection of CST4.
 * @author J.R. Bhaddacak
 * @version 3.2
 * @since 3.0
 */

public final class Cst4Info implements DocumentInfo {
	private static Corpus corpus;
	private String id;
	private String group; // equivalent to basket: vin, sut, abh, ann
	private String docClass; // mul, att, tik
	private String ref;
	private List<String> refList;
	private String fileName;
	private String textName;
	private List<String> textNameList;
	private String linkId;
	private StringProperty corpusProp; // 5 properties used for TableView
	private StringProperty summaryProp;
	private StringProperty refProp;
	private StringProperty fileNameProp;
	private IntegerProperty searchResultCountProp;
	private List<String> matchResults;

	public Cst4Info(final Corpus corp, final String idStr) {
		if (corpus == null)
			corpus = corp;
		id = idStr;
		refList = new ArrayList<>();
		textNameList = new ArrayList<>();
		linkId = "";
		corpusProperty().set(corpus.getCollection().toString());
	}

	@Override
	public String getId() {
		return id;
	}

	public void setGroup(final String grp) {
		group = grp;
	}

	@Override
	public String getGroup() {
		return group;
	}

	public void setDocClass(final String cls) {
		docClass = cls;
	}

	@Override
	public String getDocClass() {
		return docClass;
	}

	public String getGroupStr() {
		final String dclass = docClass.isEmpty() ? "" : " " + docClass.toUpperCase();
		return group.toUpperCase() + dclass;
	}

	public void setRefList(final List<String> list) {
		refList.addAll(list);
		ref = refList.stream().collect(Collectors.joining(", "));
		refProperty().set(ref);
	}

	public String getRef() {
		return ref;
	}

	public void setFileName(final String fname) {
		fileName = fname;
		final int sPos = fname.lastIndexOf("/");
		final int dPos = fname.lastIndexOf(".");
		fileNameProperty().set(fname.substring(sPos + 1, dPos));
	}
	
	@Override
	public String getFileNameWithExt() {
		return fileName;
	}

	public void setTextName(final List<String> list) {
		textNameList.addAll(list);
		textName = textNameList.stream().collect(Collectors.joining(", "));
	}

	@Override
	public String getTextName() {
		return textName;
	}

	public void setLinkId(final String id) {
		linkId = id;
	}

	public String getLinkId() {
		return linkId;
	}

	public void setSummary() {
		summaryProperty().set(textName);
	}

	@Override
	public StringProperty corpusProperty() {
		if (corpusProp == null)
			corpusProp = new SimpleStringProperty(this, "corpusProp");
		return corpusProp;
	}
	
	@Override
	public StringProperty summaryProperty() {
		if (summaryProp == null)
			summaryProp = new SimpleStringProperty(this, "summaryProp");
		return summaryProp;
	}
	
	@Override
	public StringProperty refProperty() {
		if (refProp == null)
			refProp = new SimpleStringProperty(this, "refProp");
		return refProp;
	}
	
	@Override
	public StringProperty fileNameProperty() {
		if (fileNameProp == null)
			fileNameProp = new SimpleStringProperty(this, "fileNameProp");
		return fileNameProp;
	}
	
	@Override
	public IntegerProperty searchResultCountProperty() {
		if (searchResultCountProp == null)
			searchResultCountProp = new SimpleIntegerProperty(this, "searchResultCountProp");
		return searchResultCountProp;
	}

	@Override
	public void setMatchResult(final List<String> result) {
		matchResults = result;
	}

	@Override
	public List<String> getMatchResult() {
		return matchResults == null ? Collections.emptyList() : matchResults;
	}

	@Override
	public boolean containsInfo(final String query) {
		boolean result = false;
		result = result || textName.toLowerCase().indexOf(query) > -1;
		return result;
	}

	private boolean isDMSA() {
		final char ch = id.charAt(0);
		return group.equals("sut") && docClass.equals("mul") && "dmsa".indexOf(ch) > -1;
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
					: tgAbbr[0].equals("noext") ? !corpus.isInExtraGroup(group)
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
	public TocTreeNode toTocTreeNode() {
		final TocTreeNode node = new SimpleTocTreeNode(corpus, id, textName + " (" + ref + ")", getFileNameWithExt());
		node.setIsText(true);
		return(node);
	}

	@Override
	public DocumentInfo.SuttaGroup getSuttaGroup() {
		DocumentInfo.SuttaGroup result = DocumentInfo.SuttaGroup.XN;
		if (group.equals("sut")) {
			if (docClass.equals("mul")) {
				if (id.matches("dn\\d+")) {
					result = DocumentInfo.SuttaGroup.DN;
				} else if (id.matches("mn\\d+")) {
					result = DocumentInfo.SuttaGroup.MN;
				} else if (id.matches("sn\\d+")) {
					result = DocumentInfo.SuttaGroup.SN;
				} else if (id.matches("an\\d+")) {
					result = DocumentInfo.SuttaGroup.AN;
				} else {
					result = DocumentInfo.SuttaGroup.KN;
				}
			} else if (docClass.equals("att") || docClass.equals("tik")) {
				if (id.matches("dn\\d+(?:a|t|nt)\\d?")) {
					result = DocumentInfo.SuttaGroup.DN;
				} else if (id.matches("mn\\d+(?:a|t)")) {
					result = DocumentInfo.SuttaGroup.MN;
				} else if (id.matches("sn\\d+(?:a|t)")) {
					result = DocumentInfo.SuttaGroup.SN;
				} else if (id.matches("an\\d+(?:a|t)")) {
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
		String result = ".*";
		if (tgAbbrs.length == 1) {
			switch (tgAbbrs[0]) {
				case "vin":
				case "sut":
				case "abh":
				case "ext": 
					result = tgAbbrs[0].charAt(0) + ".*";
					break;
				case "mul":
				case "att":
				case "tik":
					result = "[vsa]...." + tgAbbrs[0].charAt(0) + ".*";
					break;
				case "noext":
					result = "[^e].*";
					break;
				case "dmsa":
					result = "s0[1-4]0[1-5]m[1-4]?.*";
					break;
				case "vdmsa":
					result = "(?:(?:vin0[1-2])|(?:s0[1-4]0[1-5]))m[1-4]?.*";
					break;
			}
		} else if (tgAbbrs.length == 2) {
			result = tgAbbrs[0].charAt(0) + "...." + tgAbbrs[1].charAt(0) + ".*";
		}
		return result + "\\.xml";
	}

}
