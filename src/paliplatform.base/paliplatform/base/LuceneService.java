/*
 * LuceneService.java
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
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see https://www.gnu.org/licenses/.
 */

package paliplatform.base;

import java.util.List;
import java.util.Set;
import javafx.scene.control.Tab;

/** 
 * The service interface used by Lucene module.
 * @author J.R. Bhaddacak
 * @version 3.0
 * @since 3.0
 */

public interface LuceneService {
	Tab getLuceneTab();
	Tab getListerTab();
	List<String> getListerTableNameList();
	List<String> getTermFreqList(String tabName, Set<String> wordSet);
	void searchTerm(String term);
}

