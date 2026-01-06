/*
 * TocTreeWin.java
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
import javafx.scene.input.*;
import javafx.scene.layout.BorderPane;

/** 
 * The TOC (Table Of Contents) tree window of the Pali collections.
 * @author J.R. Bhaddacak
 * @version 3.7
 * @since 2.0
 */
public class TocTreeWin extends BorderPane {
	private TreeView<TocTreeNode> treeView;
	private final TreeItem<TocTreeNode> treeBase;
	private final ContextMenu popupMenu = new ContextMenu();
	private final ToggleButton showFileNameButton = new ToggleButton("", new TextIcon("tags", TextIcon.IconSet.AWESOME));
	private final InfoPopup infoPopup = new InfoPopup();
	
	public TocTreeWin() {
		if (ReaderUtilities.simpleServiceMap == null) 
			ReaderUtilities.simpleServiceMap = ReaderUtilities.getSimpleServices();
		final Corpus rootCorpus = new Corpus("Pāli literature", ".", ReaderUtilities.CORPUS_INFO, "false");
		treeBase = new TreeItem<>(new SimpleTocTreeNode(rootCorpus));
		treeBase.setExpanded(true);
		if (ReaderUtilities.corpusMap != null) {
			for (final Corpus cp : ReaderUtilities.corpusMap.values()) {
				final Corpus.Collection col = cp.getCollection();
				if (col == Corpus.Collection.SC || col == Corpus.Collection.SKT) continue;
				if (!cp.isAvailable()) continue;
				final TreeItem<TocTreeNode> node = cp.createTreeNode();
				treeBase.getChildren().add(node);
			}
		}
		treeView = new TreeView<>(treeBase);
		treeView.setShowRoot(treeBase.getChildren().size() == 0);
		
		// customize tree cell
		treeView.setCellFactory((TreeView<TocTreeNode> tv) -> {
			return new TreeCell<TocTreeNode>() {
				@Override
				public void updateItem(TocTreeNode item, boolean empty) {
					super.updateItem(item, empty);
					if (empty) {
						this.setText(null);
						this.setGraphic(null);
					} else {
						final TocTreeNode node = this.getTreeItem().getValue();
						if (showFileNameButton.isSelected())
							this.setText(node.toStringFull());
						else
							this.setText(node.toString());
						final TextIcon icon;
						if (node.isTextNode())
							icon = new TextIcon("file-lines", TextIcon.IconSet.AWESOME);
						else
							icon = new TextIcon("folder", TextIcon.IconSet.AWESOME);
						this.setGraphic(icon);
					}
					this.setDisclosureNode(null);
				}
			};
		});
		
		// add context menus
		final MenuItem openMenuItem = new MenuItem("Open");
		openMenuItem.setOnAction(actionEvent -> openDoc());
		final MenuItem openAsTextMenuItem = new MenuItem("Open as text");
		popupMenu.getItems().addAll(openMenuItem);
		
		// add mouse listener
		treeView.setOnMouseClicked(mouseEvent -> selectionHandler(mouseEvent));
        
        // add key listener
        treeView.setOnKeyPressed(keyEvent -> selectionHandler(keyEvent));
        
		setCenter(treeView);
		
		// add toolbar on the top
		final CommonWorkingToolBar toolBar = new CommonWorkingToolBar(treeView);
		// config some buttons
		toolBar.saveTextButton.setOnAction(actionEvent -> saveText());		
		toolBar.copyButton.setOnAction(actionEvent -> copyText());		
		// add new components
		showFileNameButton.setTooltip(new Tooltip("File names on/off"));
		showFileNameButton.setOnAction(actionEvent -> treeView.refresh());
		final Button infoButton = new Button("", new TextIcon("info", TextIcon.IconSet.AWESOME));
		infoButton.setTooltip(new Tooltip("Show corpus information"));
		infoButton.setOnAction(actionEvent -> {
			final TreeItem<TocTreeNode> treeItem = treeView.getSelectionModel().getSelectedItem();
			if (treeItem == null) return;
			final TocTreeNode node = treeItem.getValue();
			if (node == null) return;
			infoPopup.setContentWithText(node.getCorpus().getCorpusInformation());
			infoPopup.showPopup(infoButton, InfoPopup.Pos.BELOW_LEFT, true);
		});
		toolBar.getItems().addAll(showFileNameButton, infoButton);
		setTop(toolBar);		

		// some init
		infoPopup.setTextWidth(Utilities.getRelativeSize(32));
		setPrefWidth(Utilities.getRelativeSize(38));
	}

	public TreeView<TocTreeNode> getTreeView() {
		return treeView;
	}

	private void selectionHandler(final InputEvent event) {
		final TreeItem<TocTreeNode> thisItem = treeView.getSelectionModel().getSelectedItem();
		if (thisItem == null) return;
		final TocTreeNode thisNode = thisItem.getValue();
		if (event instanceof KeyEvent) {
			// action by keyboard
			final KeyEvent keyEvent = (KeyEvent)event;
			if (keyEvent.getEventType() == KeyEvent.KEY_PRESSED) {
				popupMenu.hide();
				if (keyEvent.getCode() == KeyCode.ENTER) {
					if (thisNode.isTextNode()) {
						openDoc(thisNode);
					} else {
						if (thisItem.isLeaf()) {
							thisItem.setExpanded(true);
						} else {
							thisItem.setExpanded(!thisItem.isExpanded());
						}
					}
				} else if (keyEvent.getCode() == KeyCode.RIGHT) {
					if (!thisNode.isTextNode()) {
						thisItem.setExpanded(true);
					}
				} else if (keyEvent.getCode() == KeyCode.LEFT) {
					if (!thisNode.isTextNode())			
						thisItem.setExpanded(false);
				}
			}
		} else {
			// action by mouse
			final MouseEvent mouseEvent = (MouseEvent)event;
			if (mouseEvent != null && thisNode.isTextNode()) {
				// the lowest level is text (not necessary leaf of tree)
				popupMenu.show(treeView, mouseEvent.getScreenX(), mouseEvent.getScreenY());
			} else {
				popupMenu.hide();
				if (thisItem.isLeaf()) {
					thisItem.setExpanded(true);
				} else {
					thisItem.setExpanded(!thisItem.isExpanded());
				}
			}			
		}
	}
	
	private void openDoc() {
		final TocTreeNode selected = treeView.getSelectionModel().getSelectedItem().getValue();
		openDoc(selected);
	}

	private void openDoc(final TocTreeNode node) {
		if (Utilities.checkFileExistence(node.getNodeFile()))
			ReaderUtilities.openPaliHtmlViewer(node);
	}

	private String makeText() {
		final StringBuilder result = new StringBuilder();
		int row = 0;
		TreeItem<TocTreeNode> node = null;
		do {
			node = treeView.getTreeItem(row);
			if (node != null) {
				final int tabCount = treeView.getTreeItemLevel(node) - 1;
				final TocTreeNode ttn = node.getValue();
				final String text = showFileNameButton.isSelected() ? ttn.toStringFull() : ttn.toString();
				result.append("\t".repeat(tabCount)).append(text);
				result.append(System.getProperty("line.separator"));
			}
			row++;
		} while (node != null);
		return result.toString();
	}
	
	private void copyText() {
		Utilities.copyText(makeText());
	}
	
	private void saveText() {
		Utilities.saveText(makeText(), "toctree.txt");
	}

}
