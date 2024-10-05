/*
 * StatusBar.java
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

package paliplatform.jfx;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.*;

/** 
 * A simple status bar.
 * @author J.R. Bhaddacak
 * @version 3.0
 * @since 1.0
 */

class StatusBar extends JPanel {
	private final JLabel statusText = new JLabel(" ");
	
	public StatusBar() {
		setLayout(new FlowLayout(FlowLayout.LEADING));
		Font mono = new Font(Font.MONOSPACED, Font.PLAIN, 11);
		add(statusText);
		setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
		setPreferredSize(new Dimension(0, 25));
	}

	public void setText(String s) {
		statusText.setText(s);
	}	

	public void clearText() {
		statusText.setText(" ");
	}
	
	public void setToolTipText(String s) {
		statusText.setToolTipText(s);
	}

}
