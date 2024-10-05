/*
 * ConfirmAlert.java
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

import javafx.stage.Window;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonBar.ButtonData;

/** 
 * The alert dialog used for various confirmations.
 * @author J.R. Bhaddacak
 * @version 3.0
 * @since 2.0
 */
public class ConfirmAlert extends Alert {
	public static enum ConfirmType { 
		PROCEED("Proceed"), SAVE("Save"), DELETE("Delete"),
		REPLACE("Replace"), RESTART("Restart"), QUIT("Quit");
		public String name;
		private ConfirmType(final String str) {
			name = str;
		}
	};
	private ConfirmType confirmType;
	private ButtonType confirmButtonType;
	private ButtonType discardButtonType;
	private ButtonType keepButtonType;
	
	public ConfirmAlert(final Window owner, final ConfirmType type) {
		super(AlertType.CONFIRMATION);
		setType(type);
		getButtonTypes().remove(ButtonType.OK);
		getButtonTypes().add(confirmButtonType);		
        setHeaderText(null);
        initOwner(owner);			
		setDefaultButton();
	}
	
	public ConfirmAlert(final Window owner, final ConfirmType type, final String message) {
		this(owner, type);
		setMessage(message);
	}
	
	public final void setType(final ConfirmType type) {
		confirmType = type;
		confirmButtonType = new ButtonType(confirmType.name, ButtonData.OK_DONE);
		switch (confirmType) {
			case SAVE:
				discardButtonType = new ButtonType("Discard", ButtonData.CANCEL_CLOSE);
				getButtonTypes().add(discardButtonType);	
				setContentText("The work has been modified.\nWould you like to save it?");	
				break;
			case REPLACE:
				keepButtonType = new ButtonType("Keep", ButtonData.CANCEL_CLOSE);
				getButtonTypes().add(keepButtonType);	
				setContentText("Some files already exist.\nDo you want to replace them?");
				break;
			case DELETE:
				setContentText("A file will be deleted from your computer.\nDo you really want to do this?");
				break;
			case RESTART:
				setContentText("The application will restart.");	
				break;
			case QUIT:
				setContentText("The application will be closed.\nDon't you forget something?");	
				break;
		}
	}

	private void setDefaultButton() {
		switch (confirmType) {
			case REPLACE:
				setDefaultButton(keepButtonType);
				break;
			case DELETE:
			case QUIT:
				setDefaultButton(ButtonType.CANCEL);
				break;
		}
	}

	private void setDefaultButton(final ButtonType defBtn) {
		final DialogPane pane = getDialogPane();
		for (final ButtonType bt : getButtonTypes())
			((Button)pane.lookupButton(bt)).setDefaultButton(bt == defBtn);
	}

	public ButtonType getConfirmButtonType() {
		return confirmButtonType;
	}
	
	public ButtonType getDiscardButtonType() {
		return discardButtonType;
	}
	
	public ButtonType getKeepButtonType() {
		return keepButtonType;
	}
	
	public final void setMessage(final String str) {
		setContentText(str);
	}

}
