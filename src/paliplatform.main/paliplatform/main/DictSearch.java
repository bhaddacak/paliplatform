/*
 * DictSearch.java
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

package paliplatform.main;

import paliplatform.base.*;

/** 
 * The service used for searching a term in Dict tab, if available. 
 * This service in turn uses DictService.
 * @author J.R. Bhaddacak
 * @version 3.0
 * @since 3.0
 */
public class DictSearch implements SimpleService {
	public DictSearch() {
	}
	
	@Override
	public boolean process(final Object arg) {
		final String term = (String)arg;
		PaliPlatform.showDict(term);
		return true;
	}

	@Override
	public boolean processArray(final Object[] args) {
		return true;
	}

}

