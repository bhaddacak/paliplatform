/*
 * DpdServiceImp.java
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

package paliplatform.dpd;

import paliplatform.base.*;

/** 
 * The implementation of DPD service.
 * @author J.R. Bhaddacak
 * @version 3.0
 * @since 3.0
 */
public class DpdServiceImp implements DpdService {

	public DpdServiceImp() {
	}

	@Override
	public void closeDownloader() {
		if (DpdUtilities.downloaderOpened && DpdDownloader.INSTANCE.isShowing())
			DpdDownloader.INSTANCE.hide();
	}

}

