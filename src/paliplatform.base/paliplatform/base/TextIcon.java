/*
 * TextIcon.java
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

import javafx.scene.text.*;

/** 
 * TextIcon utilizes FontAwesome and custom icons displayed in menus and buttons.
 * @author J.R. Bhaddacak
 * @version 3.0
 * @since 2.0
 */
public final class TextIcon extends Text {
	private String iconName;
	private IconSet iconSet;
	public static enum IconSet {
		AWESOME(Utilities.FONTAWESOME), CUSTOM(Utilities.FONTICON), 
		SANS(Utilities.FONTSANS), SERIF(Utilities.FONTSERIF), MONO(Utilities.FONTMONOBOLD);
		public final String fontName;
		private IconSet(final String name) {
			fontName = name;
		}
	}
	
	public TextIcon(final String key, final IconSet set) {
		iconName = key;
		iconSet = set;
		String icon = null;
		switch (set) {
			case AWESOME:
				icon = Character.toString(FontAwesomeData.INSTANCE.get(key));
				break;
			case CUSTOM:
				icon = Character.toString(PaliPlatformIconsData.INSTANCE.get(key));
				break;
			default:
				icon = key;
		}
		if (icon != null) {
			setText(icon);
			setFont(Font.font(iconSet.fontName));
		} else {
			setText("");
		}
		getStyleClass().add("shape");
	}
	
	public TextIcon(final String key, final IconSet set, final double scale) {
		this(key, set);
		setScale(scale);
	}

	public String getIconName() {
		return iconName;
	}

	public void setScale(final double factor) {
		final double currSize = getFont().getSize();
		setSize(currSize*factor);
	}

	public void setSize(final double size) {
		setFont(Font.font(iconSet.fontName, size));
	}

	public void setColor(final String color) {
		setStyle("-fx-fill:" + color);
	}
}
