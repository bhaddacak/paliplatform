/*
 * PaliTextInput.java
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

import java.util.*;
import java.util.function.*;

import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.collections.*;
import javafx.beans.property.SimpleBooleanProperty;

/** 
 * General Pali text input.
 * @author J.R. Bhaddacak
 * @version 3.0
 * @since 2.0
 */
public class PaliTextInput {
	public static enum InputType { FIELD, AREA, COMBO }
	public static enum InputMethod { 
		NORMAL("no"), UNUSED_CHARS("uc"), COMPOSITE("co"), NUMBER("nu"), METER_GROUP("me"), NONE("0");
		public static final InputMethod[] values = values();
		public final String abbr;
		private InputMethod(final String name) {
			abbr = name;
		}
	}
	private static final String validMeterGroup = "124lgnsjbmNSJYBRTML";
	private TextInputControl input;
	private InputMethod inputMethod;
	private InputType inputType;
	private ComboBox<String> cbInput;
	private final Button clearButton = new Button("", new TextIcon("delete-left", TextIcon.IconSet.AWESOME));	
	private final Button methodButton = new Button("");
	private final UnaryOperator<TextFormatter.Change> paliFilter;
	private final UnaryOperator<TextFormatter.Change> regularFilter;
	private final UnaryOperator<TextFormatter.Change> numberFilter;
	private final UnaryOperator<TextFormatter.Change> meterGroupFilter;
	private TextFormatter<String> textFormatter;
	private final SimpleBooleanProperty isChanged = new SimpleBooleanProperty(false);
	private int limit = 0; // 0 means no limit
	
	public PaliTextInput(final InputType inputType) {
		this.inputType = inputType;
		if (inputType == InputType.COMBO) {
			cbInput = new ComboBox<>();
			cbInput.setEditable(true);
			input = cbInput.getEditor();
		} else {
			input = inputType == InputType.AREA ? new TextArea() : new TextField();
		}
		// load input properties
		// set up filters for TextFormatter of TextField
		paliFilter = change -> {
			final Map<String, String> inputCharMap = Utilities.paliInputCharMap.get(inputMethod);
			final String newText = change.getText();
			if (change.getControlNewText().isEmpty())
				return change;
			if (inputCharMap.containsKey(newText))
				change.setText(inputCharMap.get(newText));
			if (inputMethod == InputMethod.UNUSED_CHARS) {
				if (Utilities.settings.getProperty("uc-upper").equals(newText)
					|| Utilities.settings.getProperty("uc-lower").equals(newText))
					return null;
			}
			if (!isChanged.get())
				isChanged.set(change.isContentChange());
			return change;
		};
		regularFilter = change -> {
			if (change.getControlNewText().isEmpty())
				return change;
			if (!isChanged.get())
				isChanged.set(change.isContentChange());
			return change;
		};
		numberFilter = change -> {
			final String newText = change.getText();
			if (change.getControlNewText().isEmpty())
				return change;
			if (!isChanged.get())
				isChanged.set(change.isContentChange());
			if (newText.matches("\\d*")) {
				if (limit <= 0) {
					return change;
				} else {
					if (change.getControlNewText().length() <= limit)
						return change;
					else
						return null;
				}
			} else {
				return null;
			}
		};
		meterGroupFilter = change -> {
			final String newText = change.getText();
			if (change.getControlNewText().isEmpty())
				return change;
			if (!isChanged.get())
				isChanged.set(change.isContentChange());
			if (validMeterGroup.contains(newText))
				return change;
			else 
				return null;
		};
		// set up key listeners for enhancing key input
		input.setOnKeyPressed(keyEvent -> {
			if (keyEvent.getEventType() == KeyEvent.KEY_PRESSED) {
				if (keyEvent.isControlDown()) {
					final KeyCode key = keyEvent.getCode();
					if (key == KeyCode.SPACE) {
						rotateInputMethod();
					}
				}
			}
		});			
		// init input method
		resetInputMethod();
		// config default action for buttons
		clearButton.setOnAction(actionEvent -> input.clear());
		methodButton.setOnAction(actionEvent -> rotateInputMethod());	
	}
	
	public TextInputControl getInput() {
		return input;
	}
	
	public ComboBox<String> getComboBox() {
		return inputType == InputType.COMBO?cbInput:null;
	}
	
	public Button getClearButton() {
		return clearButton;
	}
	
	public Button getMethodButton() {
		setupMethodButton(inputMethod);
		return methodButton;
	}
	
	public InputMethod getInputMethod() {
		return inputMethod;
	}
	
	public SimpleBooleanProperty changeProperty() {
		return isChanged;
	}

	public void setLimit(final int num) {
		limit = num;
	}
	
	public void requestFocus() {
		input.requestFocus();
	}
	
	public void setInputMethod(final InputMethod method) {
		changeInputMethod(method);
		setupMethodButton(method);
	}
	
	public final void resetInputMethod() {
		final String methodStr = Utilities.settings.getProperty("pali-input-method");
		inputMethod = methodStr == null ? InputMethod.UNUSED_CHARS : InputMethod.valueOf(methodStr);
		setInputMethod(inputMethod);
	}

	private void setupMethodButton(final InputMethod method) {
		switch (method) {
			case NORMAL:
				methodButton.setTooltip(new Tooltip("Regular input mode"));
				methodButton.setGraphic(new TextIcon("a➤a", TextIcon.IconSet.MONO));
				break;
			case UNUSED_CHARS:
				methodButton.setTooltip(new Tooltip("Using unused characters"));
				methodButton.setGraphic(new TextIcon("x➤ā", TextIcon.IconSet.MONO));
				break;
			case COMPOSITE:
				methodButton.setTooltip(new Tooltip("Using composite characters"));
				methodButton.setGraphic(new TextIcon("ā➤ā", TextIcon.IconSet.MONO));
				break;
			case NUMBER:
				methodButton.setTooltip(new Tooltip("Number mode"));
				methodButton.setGraphic(new TextIcon("0-9", TextIcon.IconSet.MONO));
				break;
			case METER_GROUP:
				methodButton.setTooltip(new Tooltip("Meter group mode"));
				methodButton.setGraphic(new TextIcon("♩♩♪", TextIcon.IconSet.MONO));
				break;
		}
	}
	
	public void changeInputMethod(final InputMethod method) {
		inputMethod = method;
		switch (inputMethod) {
			case NORMAL:
				textFormatter = new TextFormatter<>(regularFilter);
				input.setTextFormatter(textFormatter);
				break;
			case UNUSED_CHARS:
				Utilities.setupPaliInputCharMap();
				setUpperLowerEvent();
				textFormatter = new TextFormatter<>(paliFilter);
				input.setTextFormatter(textFormatter);
				break;
			case COMPOSITE:
				Utilities.setupPaliInputCharMap();
				input.setOnKeyTyped(null);
				textFormatter = new TextFormatter<>(paliFilter);
				input.setTextFormatter(textFormatter);
				break;
			case NUMBER:
				textFormatter = new TextFormatter<>(numberFilter);
				input.setTextFormatter(textFormatter);
				break;
			case METER_GROUP:
				textFormatter = new TextFormatter<>(meterGroupFilter);
				input.setTextFormatter(textFormatter);
				break;
			default:
				input.setTextFormatter(null);
		}
	}
	
	public void rotateInputMethod() {	
		if (inputMethod == InputMethod.NUMBER || inputMethod == InputMethod.METER_GROUP)
			return;
		if (inputMethod == InputMethod.NORMAL) {
			inputMethod = InputMethod.UNUSED_CHARS;
		} else if (inputMethod == InputMethod.UNUSED_CHARS) {
			inputMethod = InputMethod.COMPOSITE;
		} else {
			inputMethod = InputMethod.NORMAL;
		}
		changeInputMethod(inputMethod);
		setupMethodButton(inputMethod);
	}
	
	private void setUpperLowerEvent() {
		input.setOnKeyTyped(keyEvent -> {
			if (keyEvent.getEventType() == KeyEvent.KEY_TYPED) {
				final String keychar = keyEvent.getCharacter();
				if (Utilities.settings.getProperty("uc-upper").equals(keychar))
					upperCase(true);
				else if (Utilities.settings.getProperty("uc-lower").equals(keychar))
					upperCase(false);
			}
		});		
	}
	
	private void upperCase(final boolean yn) {
		final int currPos = input.getCaretPosition();
		if (currPos == input.getLength())
			return;
		final String currChar = input.getText(currPos, currPos+1);
		input.deleteNextChar();
		input.insertText(currPos, yn ? currChar.toUpperCase() : currChar.toLowerCase());
	}
	
	public void recordQuery() {
		if (inputType == InputType.COMBO) {
			final String str = cbInput.getValue();
			final ObservableList<String> list = cbInput.getItems();
			if (str != null && !str.isEmpty() && !list.contains(str))
				list.add(str);				
		}
	}
	
	public void clearRecord() {
		cbInput.getItems().clear();
	}

}
