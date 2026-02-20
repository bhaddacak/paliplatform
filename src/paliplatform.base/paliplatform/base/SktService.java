/*
 * SktService.java
 *
 * Copyright (C) 2023-2026 J. R. Bhaddacak 
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
import java.util.Map;
import javafx.scene.control.Tab;

/** 
 * The service interface used by Sanskrit module.
 * @author J.R. Bhaddacak
 * @version 4.1
 * @since 3.6
 */

public interface SktService {
	Tab getSktDictTab();
	void openSktDict(String term);
	void searchTerm(String term);
	boolean isSktDictAvailable(String dictCode);
	List<String> getSktDictTerms(String dictCode);
	List<String> getSktDictMeaning(String dictCode, String term);
	Map<Utilities.PaliScript, List<String>> getSandhiProduct(String text);
}

