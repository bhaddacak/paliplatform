/*
 * SimpleDocumentInfo.java
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
 * The simplest information class for a document in the collections.
 * @author J.R. Bhaddacak
 * @version 3.2
 * @since 3.0
 */

public class SimpleDocumentInfo implements DocumentInfo {
	protected final Corpus corpus;
	protected final String id;
	protected String group = ""; // vin, sut, abh, ext, ...
	protected String ref = "";
	private String fileName = "";
	private String textName = "";
	private String description = "";
	private StringProperty corpusProp; // 5 properties used for TableView
	private StringProperty summaryProp;
	private StringProperty refProp;
	private StringProperty fileNameProp;
	private IntegerProperty searchResultCountProp;
	private List<String> matchResults;

	public SimpleDocumentInfo(final Corpus corp, final String idStr) {
		corpus = corp;
		id = idStr;
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

	@Override
	public String getDocClass() {
		return "";
	}

	public String getGroupStr() {
		return group.toUpperCase();
	}

	public void setRef(final String rf) {
		ref= rf.substring(0, 1).toUpperCase() + rf.substring(1);
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

	public void setDescription(final String desc) {
		description = desc;
	}

	public String getDescription() {
		return description;
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
		result = result || description.toLowerCase().indexOf(query) > -1;
		return result;
	}
	
	@Override
	public boolean isInTextGroup(final TextGroup tg) {
		final boolean result;
		final String[] tgAbbr = tg.getAbbrev().split(TextGroup.DELIM_REX);
		if (tgAbbr.length == 1) {
			result = tgAbbr[0].equals("all") ? true 
					: tgAbbr[0].equals("noext") ? !corpus.isInExtraGroup(group)
					: group.equals(tgAbbr[0]);
		} else {
			result = false;
		}
		return result;
	}

	@Override
	public TocTreeNode toTocTreeNode() {
		// for doc in cstr nodeid is also needed,
		// using ref or id in parens following text name
		final TocTreeNode node = new SimpleTocTreeNode(corpus, id, textName + " (" + ref + ")", getFileNameWithExt());
		node.setIsText(true);
		return(node);
	}

	@Override
	public DocumentInfo.SuttaGroup getSuttaGroup() {
		DocumentInfo.SuttaGroup result = DocumentInfo.SuttaGroup.XN;
		if (group.equals("sut")) {
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
		}
		return result;
	}

	public static String getFileFilterPatternString(final String textGroupAbbr) {
		return ".*";
	}

}

