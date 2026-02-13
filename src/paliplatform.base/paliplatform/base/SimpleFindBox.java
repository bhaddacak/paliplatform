/*
 * SimpleFindBox.java
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

package paliplatform.base;

import javafx.application.Platform;
import javafx.stage.Popup;
import javafx.scene.control.*;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.geometry.*;
import javafx.collections.*;

/** 
 * The simple find box used in HTML viewers.
 * @author J.R. Bhaddacak
 * @version 3.7
 * @since 3.0
 */
public class SimpleFindBox extends VBox {
	private final Node hostNode;
	private final PaliTextInput findTextInput = new PaliTextInput(PaliTextInput.InputType.FIELD);
	private final TextField findTextField;
	private final Button prevButton = new Button("", new TextIcon("angle-left", TextIcon.IconSet.AWESOME));
	private final Button nextButton = new Button("", new TextIcon("angle-right", TextIcon.IconSet.AWESOME));
	private final Button closeButton = new Button("", new TextIcon("xmark", TextIcon.IconSet.AWESOME));
	private final Button clearFindButton;
	private final Popup messagePopup = new Popup();
	private final Label messageText = new Label("");
	private final ToggleButton caseSensitiveButton = new ToggleButton("", new TextIcon("C", TextIcon.IconSet.AWESOME));
	private final InfoPopup infoPopup = new InfoPopup();
	private boolean suspended = false;
	
	public SimpleFindBox(final Node node) {
		hostNode = node;
		// find part
		findTextInput.setInputMethod(PaliTextInput.InputMethod.NORMAL);
		findTextField = (TextField)findTextInput.getInput();
		findTextField.setPrefWidth(Utilities.getRelativeSize(20));
		findTextField.setPromptText("Search for...");
		clearFindButton = findTextInput.getClearButton();
		prevButton.setTooltip(new Tooltip("Find backward"));
		nextButton.setTooltip(new Tooltip("Find forward"));
		caseSensitiveButton.setTooltip(new Tooltip("Case sensitive"));
		caseSensitiveButton.setOnAction(actionEvent -> findTextField.requestFocus());
		closeButton.setTooltip(new Tooltip("Close"));
		final Button helpButton = new Button("", new TextIcon("circle-question", TextIcon.IconSet.AWESOME));
		helpButton.setOnAction(actionEvent -> infoPopup.showPopup(helpButton, InfoPopup.Pos.OVER_RIGHT, true));
		final HBox findBox = new HBox();
		findBox.getChildren().addAll(findTextField, clearFindButton, findTextInput.getMethodButton(),
									new Separator(), prevButton, nextButton, 
									new Separator(), caseSensitiveButton,
									new Separator(), helpButton, closeButton);
		// add components
		getChildren().add(findBox);
		setPadding(new Insets(3));
		
		// prepare popups
		messagePopup.setHideOnEscape(true);
		messagePopup.setAutoHide(true);
		final StackPane messageBox = new StackPane();
		messageBox.getStyleClass().add("infopopup");
		messageBox.getChildren().add(messageText);
		messagePopup.getContent().add(messageBox);

		infoPopup.setContent("info-find-simple.txt");
		infoPopup.setTextWidth(Utilities.getRelativeSize(32));

		init();
	}

	public void init() {
		findTextField.clear();
		caseSensitiveButton.setSelected(false);
	}
	
	public PaliTextInput getFindTextInput() {
		return findTextInput;
	}
	
	public TextField getFindTextField() {
		return findTextField;
	}
	
	public Button getPrevButton() {
		return prevButton;
	}
	
	public Button getNextButton() {
		return nextButton;
	}
	
	public Button getCloseButton() {
		return closeButton;
	}
	
	public Button getClearFindButton() {
		return clearFindButton;
	}

	public boolean isCaseSensitive() {
		return caseSensitiveButton.isSelected();
	}
	
	public void showMessage(final String text) {
		messageText.setText(text);
		final Bounds buttonBounds = findTextField.localToScreen(findTextField.getBoundsInLocal());
		final Runnable task = () -> {
			try {
				Platform.runLater(() -> messagePopup.show(findTextField, buttonBounds.getMinX(), buttonBounds.getMinY() - Utilities.getRelativeSize(3.2)));
				Thread.sleep(1000);
				Platform.runLater(() -> hideMessage());
			} catch (InterruptedException e) {
				System.err.println(e);
			}
		};
		final Thread t = new Thread(task); 
		t.start();
	}
	
	private void hideMessage() {
		messagePopup.hide();
	}

	public void setSuspended(final boolean yn) {
		suspended = yn;
	}

	public boolean isSuspended() {
		return suspended;
	}

	public void setSanskritMode(final boolean yn) {
		findTextInput.setSanskritMode(yn);
	}

}

