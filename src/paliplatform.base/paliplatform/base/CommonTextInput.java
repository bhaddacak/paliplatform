/*
 * CommonTextInput.java
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

import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

/** 
 * A generic text input field, normally used in a search bar.
 * @author J.R. Bhaddacak
 * @version 3.0
 * @since 2.0
 */
public class CommonTextInput extends HBox {
	protected final TextField input = new TextField();
	protected final Button clearButton = new Button("", new TextIcon("delete-left", TextIcon.IconSet.AWESOME));
	
	public CommonTextInput() {
		input.setPromptText("Search for...");
		setHgrow(input, Priority.ALWAYS);
		clearButton.setOnAction(actionEvent -> input.clear());
		
		getChildren().addAll(input, clearButton);
	}
	
	public TextField getInput() {
		return input;
	}
	
	public Button getClearButton() {
		return clearButton;
	}
}
