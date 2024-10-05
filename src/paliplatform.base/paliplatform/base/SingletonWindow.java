/*
 * SingletonWindow.java
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

package paliplatform.base;

import javafx.stage.Stage;

/**
 * The parent class of singleton windows.
 * @author J.R. Bhaddacak
 * @version 3.0
 * @since 2.0
 */
public class SingletonWindow extends Stage {
	protected double windowWidth = Utilities.getRelativeSize(40);
	protected double windowHeight = Utilities.getRelativeSize(35);
		
	protected void init() {
	}
	
	public void display() {
		if (this.isShowing()) {
			this.toFront();
		} else {
			getScene().getStylesheets().clear();
			final String stylesheet = Utilities.getCustomStyleSheet();
			if (stylesheet.length() > 0)
				getScene().getStylesheets().add(stylesheet);
			init();
			show();
		}	
	}

}
