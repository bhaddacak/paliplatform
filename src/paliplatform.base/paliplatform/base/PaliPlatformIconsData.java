/*
 * PaliPlatformIconsData.java
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

import java.util.HashMap;

/** 
 * This contains the PaliPlatform Icons list used.
 * This is a singleton.
 * @author J.R. Bhaddacak
 * @version 3.7
 * @since 2.0
 */
 class PaliPlatformIconsData extends HashMap<String, Character> {
	 static final PaliPlatformIconsData INSTANCE = new PaliPlatformIconsData();
	 
	 private PaliPlatformIconsData() {
		 super();
		 put("left-pane", 'A');
		 put("right-pane", 'B');
		 put("full-view", 'C');
		 put("dpd", 'D');
		 put("bookmark-plus", 'E');
		 put("skt-letter", 'F');
		 put("skt-book", 'G');
		 put("skt-scroll", 'H');
		 put("book-update", 'I');
		 put("lucene", 'L');
		 put("sc", 'S');
	 }
 }
