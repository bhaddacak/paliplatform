/*
 * ReaderServiceImp.java
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

import java.util.Map;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.scene.layout.HBox;

/** 
 * An implementation of Reader service.
 * @author J.R. Bhaddacak
 * @version 3.7
 * @since 3.0
 */
public class ReaderServiceImp implements ReaderService {
	private Pane toctreeWin;
	private Tab toctreeTab;
	private Pane documentFinder;
	private Tab docFinderTab;

	public ReaderServiceImp() {
	}

	@Override
	public Tab getTocTreeTab() {
		toctreeTab = new Tab("TOC Tree");
		toctreeTab.setClosable(false);
		final TextIcon treeIcon = new TextIcon("folder-tree", TextIcon.IconSet.AWESOME);
		toctreeTab.setGraphic(treeIcon);
		final Button loadButton = new Button("Load");
		loadButton.setOnAction(actionEvent -> setTocTreeWin());
		final HBox box = new HBox();
		box.setPadding(new Insets(5));
		box.getChildren().add(loadButton);
		toctreeTab.setContent(box);
		return toctreeTab;
	}

	private void setTocTreeWin() {
		if (toctreeWin == null)
			toctreeWin = new TocTreeWin();
		toctreeTab.setContent(toctreeWin);
	}

	@Override
	public Tab getDocumentFinderTab() {
		docFinderTab = new Tab("Document Finder");
		docFinderTab.setClosable(false);
		final TextIcon findIcon = new TextIcon("binoculars", TextIcon.IconSet.AWESOME);
		docFinderTab.setGraphic(findIcon);
		final Button loadButton = new Button("Load");
		loadButton.setDisable(ReaderUtilities.corpusMap == null || ReaderUtilities.corpusMap.isEmpty());
		loadButton.setOnAction(actionEvent -> setDocumentFinder());
		final HBox box = new HBox();
		box.setPadding(new Insets(5));
		box.getChildren().add(loadButton);
		docFinderTab.setContent(box);
		return docFinderTab;
	}

	private void setDocumentFinder() {
		if (documentFinder == null)
			documentFinder = new DocumentFinder();
		docFinderTab.setContent(documentFinder);
	}

	@Override
	public void openDocument(final String colId, final String docId, final String strToLocate) {
		final Corpus.Collection col = Corpus.Collection.valueOf(colId.toUpperCase());
		final Corpus cp = ReaderUtilities.corpusMap.get(col);
		if (cp == null || !cp.isAvailable()) return;
		final Map<String, DocumentInfo> docInfoMap = cp.getDocInfoMap();
		final DocumentInfo dinfo = docInfoMap.get(docId);
		if (dinfo == null) return;
		if (col == Corpus.Collection.SC || col == Corpus.Collection.SKT) {
			if (strToLocate.isEmpty())
				ReaderUtilities.openOtherReader(col, dinfo);
			else
				ReaderUtilities.openOtherReader(col, dinfo, strToLocate);
		} else {
			final TocTreeNode node = dinfo.toTocTreeNode();
			if (Utilities.checkFileExistence(node.getNodeFile())) {
				if (strToLocate.isEmpty())
					ReaderUtilities.openPaliHtmlViewer(node);
				else
					ReaderUtilities.openPaliHtmlViewer(node, strToLocate);
			}
		}
	}

	@Override
	public void searchTerm(final String term) {
		if (documentFinder != null) {
			((DocumentFinder)documentFinder).setSearchInput(term);
		}
	}

}

