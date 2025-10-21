/*
 * BjtInfo.java
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
 * The information class for a document in the collection
 * of the Buddha Jayanthi Tripitaka (tipitaka.lk).
 * @author J.R. Bhaddacak
 * @version 3.2
 * @since 3.0
 */

public final class BjtInfo implements DocumentInfo {
	private static Set<String> dmsaSet = Set.of("dn-", "mn-", "sn-", "an-");
	private static Corpus corpus;
	private String id;
	private String group; // equivalent to basket: vin, sut, abh
	private String docClass; // mul, att
	private String ref;
	private String fileName;
	private String textName;
	private final List<String> altNames;
	private String description;
	private final List<String> commentaries;
	private StringProperty corpusProp; // 5 properties used for TableView
	private StringProperty summaryProp;
	private StringProperty refProp;
	private StringProperty fileNameProp;
	private IntegerProperty searchResultCountProp;
	private List<String> matchResults;
	
	public BjtInfo(final Corpus corp, final String idStr) {
		if (corpus == null)
			corpus = corp;
		id = idStr;
		altNames = new ArrayList<>();
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
		final int sPos = fname.lastIndexOf("/");
		final int dPos = fname.lastIndexOf(".");
		fileNameProperty().set(fname.substring(sPos + 1, dPos));
	}
	
	@Override
	public String getFileNameWithExt() {
		return fileName;
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
		for (final String alt : altNames)
			result = result || alt.toLowerCase().indexOf(query) > -1;
		result = result || description.toLowerCase().indexOf(query) > -1;
		return result;
	}
	

	private boolean isDMSA() {
		final String prefix = id.substring(0, 3);
		return group.equals("sut") && dmsaSet.contains(prefix);
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
				if (id.startsWith("dn-")) {
					result = DocumentInfo.SuttaGroup.DN;
				} else if (id.startsWith("mn-")) {
					result = DocumentInfo.SuttaGroup.MN;
				} else if (id.startsWith("sn-")) {
					result = DocumentInfo.SuttaGroup.SN;
				} else if (id.startsWith("an-")) {
					result = DocumentInfo.SuttaGroup.AN;
				} else {
					result = DocumentInfo.SuttaGroup.KN;
				}
			} else if (docClass.equals("att")) {
				if (id.startsWith("atta-dn-")) {
					result = DocumentInfo.SuttaGroup.DN;
				} else if (id.startsWith("atta-mn-")) {
					result = DocumentInfo.SuttaGroup.MN;
				} else if (id.startsWith("atta-sn-")) {
					result = DocumentInfo.SuttaGroup.SN;
				} else if (id.startsWith("atta-an-")) {
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
					result = "(?:vp|atta-vp)-.+";
					break;
				case "sut":
					result = "(?:dn|mn|sn|an|kn|atta-dn|atta-mn|atta-sn|atta-an|atta-kn)-.+";
					break;
				case "abh":
					result = "(?:ap|atta-ap)-.+";
					break;
				case "mul":
					result = "(?:vp|dn|mn|sn|an|kn|ap)-.+";
					break;
				case "att":
					result = "atta-.+";
					break;
				case "ext":
					result = "anya-.+";
					break;
				case "dmsa":
					result = "(?:dn|mn|sn|an)-.+";
					break;
				case "vdmsa":
					result = "(?:vp|dn|mn|sn|an)-.+";
					break;
			}
		} else if (tgAbbrs.length == 2) {
			if (tgAbbrs[1].equals("mul")) {
				switch (tgAbbrs[0]) {
					case "vin":
						result = "vp-.+";
						break;
					case "sut":
						result = "(?:dn|mn|sn|an|kn)-.+";
						break;
					case "abh":
						result = "ap-.+";
						break;
				}
			} else if (tgAbbrs[1].equals("att")) {
				switch (tgAbbrs[0]) {
					case "vin":
						result = "atta-vp-.+";
						break;
					case "sut":
						result = "atta-(?:dn|mn|sn|an|kn)-.+";
						break;
					case "abh":
						result = "atta-ap-.+";
						break;
				}
			}
		}
		return result + "\\.json";
	}

}

