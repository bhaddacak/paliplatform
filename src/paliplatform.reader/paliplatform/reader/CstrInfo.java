/*
 * CstrInfo.java
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
import javafx.beans.property.*;

/** 
 * The information class for a document in the collection
 * of CST restructured.
 * @author J.R. Bhaddacak
 * @version 3.0
 * @since 3.0
 */

public final class CstrInfo implements DocumentInfo {
	private static Corpus corpus;
	private String id;
	private String group; // equivalent to basket: vin, sut, abh
	private String docClass; // mul, exe
	private String ref;
	private String fileName;
	private String textName;
	private final List<String> altNames;
	private String cscdClass;
	private final List<String> cscdFileNames;
	private String description;
	private final List<String> commentaries;
	private boolean linkable;
	private StringProperty corpusProp; // 5 properties used for TableView
	private StringProperty summaryProp;
	private StringProperty refProp;
	private StringProperty fileNameProp;
	private IntegerProperty searchResultCountProp;

	public CstrInfo(final Corpus corp, final String idStr) {
		if (corpus == null)
			corpus = corp;
		id = idStr;
		altNames = new ArrayList<>();
		cscdFileNames = new ArrayList<>();
		commentaries = new ArrayList<>();
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
		return group.toUpperCase() + " " + docClass.toUpperCase();
	}

	public void setRef(final String rf) {
		ref = rf;
		refProperty().set(ref);
	}

	public String getRef() {
		return ref;
	}

	public void setFileName(final String fname) {
		fileName = fname;
		fileNameProperty().set(fname);
	}
	
	@Override
	public String getFileNameWithExt() {
		return fileName + ".gz";
	}

	public void setTextName(final String tname) {
		textName = tname;
	}

	@Override
	public String getTextName() {
		return textName;
	}

	public void addAltName(final String aname) {
		altNames.add(aname);
	}

	public void addAllAltNames(final List<String> anameList) {
		altNames.addAll(anameList);
	}

	public List<String> getAltNames() {
		return altNames;
	}

	public void setCscdClass(final String cls) {
		cscdClass = cls;
	}

	public String getCscdClass() {
		return cscdClass;
	}

	public void addCscdFileName(final String fname) {
		cscdFileNames.add(fname);
	}

	public void addAllCscdFileNames(final List<String> fnameList) {
		cscdFileNames.addAll(fnameList);
	}

	public List<String> getCscdFileNames() {
		return cscdFileNames;
	}

	public void setDescription(final String desc) {
		description = desc;
	}

	public String getDescription() {
		return description;
	}

	public void addCommentary(final String cname) {
		commentaries.add(cname);
	}

	public void addAllCommentaries(final List<String> cnameList) {
		commentaries.addAll(cnameList);
	}

	public List<String> getCommentaries() {
		return commentaries;
	}

	public boolean hasCommentary(final String comId) {
		return commentaries.contains(comId);
	}

	public void setLinkable(final boolean yn) {
		linkable = yn;
	}

	public boolean isLinkable() {
		return linkable;
	}

	public void setSummary() {
		final String altNam = altNames.isEmpty() ? "" : " [" + altNames.stream().collect(Collectors.joining(", ")) + "]";
		summaryProperty().set(textName + altNam);
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
	public boolean containsInfo(final String query) {
		boolean result = false;
		result = result || textName.toLowerCase().indexOf(query) > -1;
		for (final String alt : altNames)
			result = result || alt.toLowerCase().indexOf(query) > -1;
		result = result || description.toLowerCase().indexOf(query) > -1;
		return result;
	}
	
	private boolean isDMSA() {
		final char ch = id.charAt(0);
		final char num = id.charAt(1);
		return getGroup().equals("sut") && docClass.equals("mul") && "dmsa".indexOf(ch) > -1 && Character.isDigit(num);
	}
	
	private boolean isVDMSA() {
		return (docClass.equals("mul") && group.equals("vin") && !id.equals("bup") && !id.equals("bip"))
				 || isDMSA();
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
			} else if (docClass.equals("exe")) {
				if (id.matches("(?:smv\\d+|lpd\\d+|sv-nt)")) {
					result = DocumentInfo.SuttaGroup.DN;
				} else if (id.matches("(?:pps\\d+|lpm\\d+)")) {
					result = DocumentInfo.SuttaGroup.MN;
				} else if (id.matches("(?:srp\\d+|lps\\d+)")) {
					result = DocumentInfo.SuttaGroup.SN;
				} else if (id.matches("(?:mnp\\d+|srm\\d+)")) {
					result = DocumentInfo.SuttaGroup.AN;
				} else if (id.startsWith("vism")) {
					// only Visuddhimagga is in Extra group
					result = DocumentInfo.SuttaGroup.XN;
				} else {
					result = DocumentInfo.SuttaGroup.KN;
				}
			}
		}
		return result;
	}

	public static String getFileFilterPatternString(final String textGroupAbbr) {
		final String[] tgAbbrs = textGroupAbbr.split(TextGroup.DELIM_REX);
		final String prefix = "cst-";
		String result = ".*";
		if (tgAbbrs.length == 1) {
			switch (tgAbbrs[0]) {
				case "vin":
				case "sut":
				case "abh":
					result = prefix + tgAbbrs[0].charAt(0) + ".-.*";
					break;
				case "mul":
				case "exe":
					result = prefix + "[vsa]" + tgAbbrs[0].charAt(0) + "-.*";
					break;
				case "dmsa":
					result = prefix + "sm-[dmsa]\\d\\d?.*";
					break;
				case "vdmsa":
					result = prefix + "(?:(?:vm-(?:buv1|buv2|biv|mv|cv|pvr))|(?:sm-[dmsa]\\d\\d?)).*";
					break;
			}
		} else if (tgAbbrs.length == 2) {
			result = prefix + tgAbbrs[0].charAt(0) + tgAbbrs[1].charAt(0) + "-.*";
		}
		return result + "\\.gz";
	}

}
