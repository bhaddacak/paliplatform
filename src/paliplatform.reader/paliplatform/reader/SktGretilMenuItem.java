/*
 * SktGretilMenuItem.java
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

package paliplatform.reader;

import paliplatform.base.*;

import javafx.scene.control.*;

/** 
 * The menu item for GRETIL Sanskrit documents.
 * @author J.R. Bhaddacak
 * @version 3.7
 * @since 3.7
 */
public class SktGretilMenuItem extends MenuItem {
	public SktGretilMenuItem() {
		super("GRETIL Sanskrit documents");
		setGraphic(new TextIcon("skt-scroll", TextIcon.IconSet.CUSTOM));
		disableProperty().bind(ReaderUtilities.sktGretilAvailable.not());
		setOnAction(actionEvent -> ReaderUtilities.openWindow(Utilities.WindowType.VIEWER_SKTGRETIL, null));
	}

}
