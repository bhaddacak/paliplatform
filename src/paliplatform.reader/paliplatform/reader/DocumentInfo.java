/*
 * DocumentInfo.java
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

import paliplatform.base.Utilities;

import java.util.List;
import java.util.Comparator;
import javafx.beans.property.StringProperty;
import javafx.beans.property.IntegerProperty;

/** 
 * The interface of document information used mainly in DocumentFinder.
 * @author J.R. Bhaddacak
 * @version 3.2
 * @since 3.0
 */

public interface DocumentInfo {
	public static enum SuttaGroup {
		DN("Dīghanikāya"), MN("Majjhimanikāya"), SN("Saṃyuttanikāya"),
		AN("Aṅguttaranikāya"), KN("Khuddakanikāya"), XN("Extra Group");
		public static final SuttaGroup[] groups = values();
		private String name;
		SuttaGroup(final String name) {
			this.name = name;
		}
		public String getName() {
			return name;
		}
	}

	String getId();
	String getTextName();
	String getFileNameWithExt();
	String getGroup(); // vin, sut, abh
	String getDocClass(); // mul, exe
	StringProperty corpusProperty();
	StringProperty summaryProperty();
	StringProperty refProperty();
	StringProperty fileNameProperty();
	IntegerProperty searchResultCountProperty();
	void setMatchResult(List<String> result);
	List<String> getMatchResult();
	boolean containsInfo(final String query);
	boolean isInTextGroup(final TextGroup tg);
	TocTreeNode toTocTreeNode();
	SuttaGroup getSuttaGroup();
	
}
