/*
 * DummyDocumentInfo.java
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

import java.util.List;
import java.util.Collections;
import java.util.Comparator;
import javafx.beans.property.*;

/** 
 * The dummy class for DocumentInfo.
 * @author J.R. Bhaddacak
 * @version 3.2
 * @since 3.0
 */

public class DummyDocumentInfo implements DocumentInfo {
	private StringProperty corpus;
	private StringProperty summary;
	private StringProperty ref;
	private StringProperty fileName;
	private IntegerProperty searchResultCount;

	@Override
	public String getId() {
		return "";
	}

	@Override
	public String getGroup() {
		return "";
	}

	@Override
	public String getDocClass() {
		return "";
	}

	@Override
	public String getTextName() {
		return summaryProperty().get();
	}

	@Override
	public String getFileNameWithExt() {
		return fileNameProperty().get() + ".txt";
	}

	@Override
	public StringProperty corpusProperty() {
		if (corpus == null)
			corpus = new SimpleStringProperty(this, "corpus");
		return corpus;
	}
	
	@Override
	public StringProperty summaryProperty() {
		if (summary == null)
			summary = new SimpleStringProperty(this, "summary");
		return summary;
	}
	
	@Override
	public StringProperty refProperty() {
		if (ref == null)
			ref = new SimpleStringProperty(this, "ref");
		return ref;
	}
	
	@Override
	public StringProperty fileNameProperty() {
		if (fileName == null)
			fileName = new SimpleStringProperty(this, "fileName");
		return fileName;
	}
	
	@Override
	public IntegerProperty searchResultCountProperty() {
		if (searchResultCount == null)
			searchResultCount = new SimpleIntegerProperty(this, "searchResultCount");
		return searchResultCount;
	}
	
	@Override
	public void setMatchResult(final List<String> result) {
	}

	@Override
	public List<String> getMatchResult() {
		return Collections.emptyList();
	}

	@Override
	public boolean containsInfo(final String query) {
		return true;
	}

	@Override
	public boolean isInTextGroup(final TextGroup tg) {
		return true;
	}

	@Override
	public TocTreeNode toTocTreeNode() {
		throw new UnsupportedOperationException();
	}

	@Override
	public DocumentInfo.SuttaGroup getSuttaGroup() {
		return DocumentInfo.SuttaGroup.XN;
	}

}

