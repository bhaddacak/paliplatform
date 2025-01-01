/*
 * FieldSelectorBox.java
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

package paliplatform.lucene;

import paliplatform.base.*;
import paliplatform.reader.*;

import java.util.*;

import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.geometry.*;
import javafx.beans.property.SimpleBooleanProperty;

/** 
 * This widget of field selector is used in LuceneFinder.
 * In version 3, to make it simpler, the full selectors are removed.
 * @author J.R. Bhaddacak
 * @version 3.0
 * @since 2.0
 */
public class FieldSelectorBox extends VBox {
	public static enum SimpleField {
		BODY("body"), HEAD("headings"), GATHA("gāthā"), NOTE("note"), BOLD("bold");
		private final String name;
		public static final SimpleField[] values = SimpleField.values();
		private SimpleField(final String name) {
			this.name = name;
		}
		public String getName() {
			return name;
		}
	}
	private final Map<TermInfo.Field, Boolean> selectorMap = new EnumMap<>(TermInfo.Field.class);
	private final Map<SimpleField, CheckBox> simpleFieldMap = new EnumMap<>(SimpleField.class);
	private final Map<SimpleField, SimpleBooleanProperty> simpleFieldAvailMap = new EnumMap<>(SimpleField.class);
	private final Runnable updateFunc;

	public FieldSelectorBox(final Runnable func) {
		updateFunc = func;
		final VBox simpleFieldsBox = new VBox();
		for (final SimpleField f : SimpleField.values) {
			final CheckBox cbSimple = new CheckBox(f.getName());
			cbSimple.setOnAction(actionEvent -> updateFields(actionEvent.getSource()));
			final SimpleBooleanProperty prop = new SimpleBooleanProperty(true);
			cbSimple.disableProperty().bind(prop.not());
			cbSimple.setUserData(f);
			simpleFieldMap.put(f, cbSimple);
			simpleFieldAvailMap.put(f, prop);
			simpleFieldsBox.getChildren().add(cbSimple);
		}
		getChildren().add(simpleFieldsBox);
		setPrefWidth(Utilities.getRelativeSize(8.2));
		setPadding(new Insets(0, 0, 0, 3));
		init();
	}

	public void init() {
		setDefaultFieldSelection();
	}

	public boolean isFieldSelected(final TermInfo.Field fld) {
		return selectorMap.getOrDefault(fld, Boolean.valueOf(false));
	}

	public void setFieldAvailable(final FieldSelectorBox.SimpleField field, final boolean yn) {
		simpleFieldAvailMap.get(field).set(yn);
	}

	private void updateFields(final Object source) {
		final CheckBox cbSrc = (CheckBox)source;
		final SimpleField field = (SimpleField)cbSrc.getUserData();
		final boolean sel = cbSrc.isSelected();
		if (sel && field != SimpleField.BOLD) {
			simpleFieldMap.get(SimpleField.BOLD).setSelected(false);
			selectorMap.put(TermInfo.Field.BOLD, false);
		}
		switch (field) {
			case BODY:
				selectorMap.put(TermInfo.Field.BODYTEXT, sel);
				selectorMap.put(TermInfo.Field.CENTRE, sel);
				selectorMap.put(TermInfo.Field.INDENT, sel);
				selectorMap.put(TermInfo.Field.UNINDENTED, sel);
				break;
			case HEAD:
				selectorMap.put(TermInfo.Field.NIKAYA, sel);
				selectorMap.put(TermInfo.Field.BOOK, sel);
				selectorMap.put(TermInfo.Field.CHAPTER, sel);
				selectorMap.put(TermInfo.Field.TITLE, sel);
				selectorMap.put(TermInfo.Field.SUBHEAD, sel);
				selectorMap.put(TermInfo.Field.SUBSUBHEAD, sel);
				selectorMap.put(TermInfo.Field.PART, sel);
				selectorMap.put(TermInfo.Field.GROUP, sel);
				selectorMap.put(TermInfo.Field.SUBGROUP, sel);
				selectorMap.put(TermInfo.Field.ENDPART, sel);
				selectorMap.put(TermInfo.Field.ENDGROUP, sel);
				selectorMap.put(TermInfo.Field.ENDSUBGROUP, sel);
				selectorMap.put(TermInfo.Field.STRONG, sel);
				selectorMap.put(TermInfo.Field.HEADING, sel);
				break;
			case GATHA:
				selectorMap.put(TermInfo.Field.GATHA1, sel);
				selectorMap.put(TermInfo.Field.GATHA2, sel);
				selectorMap.put(TermInfo.Field.GATHA3, sel);
				selectorMap.put(TermInfo.Field.GATHALAST, sel);
				selectorMap.put(TermInfo.Field.GATHA, sel);
				break;
			case NOTE:
				selectorMap.put(TermInfo.Field.NOTE, sel);
				break;
			case BOLD:
				if (sel) {
					setNonBoldFieldCheckBoxes(false);
				}
				selectorMap.put(TermInfo.Field.BOLD, sel);
				break;
		}
		preventNoFieldSelection();
		updateFunc.run();
	}

	private void setDefaultFieldSelection() {
		for (final CheckBox cb : simpleFieldMap.values()) {
			final SimpleField field = (SimpleField)cb.getUserData();
			final boolean sel = field != SimpleField.NOTE && field != SimpleField.BOLD;
			cb.setIndeterminate(false);
			cb.setSelected(sel);
		}
		for (final TermInfo.Field f : TermInfo.Field.values) {
			final boolean val = f == TermInfo.Field.NOTE || f == TermInfo.Field.BOLD ? false : true; 
			selectorMap.put(f, val);
		}
	}

	private void setNonBoldFieldCheckBoxes(final boolean val) {
		for (final CheckBox cb : simpleFieldMap.values()) {
			final SimpleField field = (SimpleField)cb.getUserData();
			if (field != SimpleField.BOLD) {
				cb.setIndeterminate(false);
				cb.setSelected(val);
			}
		}
		for (final TermInfo.Field f : TermInfo.Field.values) {
			if (f != TermInfo.Field.BOLD)
				selectorMap.put(f, val);
		}
	}

	private void preventNoFieldSelection() {
		boolean sel = false;
		for (final CheckBox cb : simpleFieldMap.values())
			sel = sel || cb.isSelected();
		if (!sel)
			setDefaultFieldSelection();
	}

}
