/*
 * LuceneServiceImp.java
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

package paliplatform.lucene;

import paliplatform.base.*;

import java.util.*;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.scene.layout.HBox;

/** 
 * An implementation of Lucene service.
 * @author J.R. Bhaddacak
 * @version 3.0
 * @since 3.0
 */
public class LuceneServiceImp implements LuceneService {
	private Pane finderWin;
	private Tab finderTab;
	private Pane listerWin;
	private Tab listerTab;

	public LuceneServiceImp() {
	}

	@Override
	public Tab getLuceneTab() {
		finderTab = new Tab("Lucene Finder");
		finderTab.setClosable(false);
		final TextIcon luceneIcon = new TextIcon("lucene", TextIcon.IconSet.CUSTOM);
		finderTab.setGraphic(luceneIcon);
		final Button loadButton = new Button("Load");
		loadButton.setOnAction(actionEvent -> setFinder());
		final HBox box = new HBox();
		box.setPadding(new Insets(5));
		box.getChildren().add(loadButton);
		finderTab.setContent(box);
		return finderTab;
	}

	private void setFinder() {
		if (finderWin == null)
			finderWin = new LuceneFinder();
		finderTab.setContent(finderWin);
	}

	@Override
	public Tab getListerTab() {
		listerTab = new Tab("Term Lister");
		listerTab.setClosable(false);
		final TextIcon listerIcon = new TextIcon("bars", TextIcon.IconSet.AWESOME);
		listerTab.setGraphic(listerIcon);
		final Button loadButton = new Button("Load");
		loadButton.setOnAction(actionEvent -> setLister());
		final HBox box = new HBox();
		box.setPadding(new Insets(5));
		box.getChildren().add(loadButton);
		listerTab.setContent(box);
		return listerTab;
	}

	private void setLister() {
		if (listerWin == null)
			listerWin = new TermLister();
		listerTab.setContent(listerWin);
	}

	@Override
	public List<String> getListerTableNameList() {
		return LuceneUtilities.getListerTableNameList();
	}

	@Override
	public List<String> getTermFreqList(final String tabName, final Set<String> wordSet) {
		if (LuceneUtilities.listerTableMap.containsKey(tabName))
			return LuceneUtilities.getTermFreqListFromDB(tabName, wordSet);
		else
			return Collections.emptyList();
	}

	@Override
	public void searchTerm(final String term) {
		if (finderWin != null) {
			((LuceneFinder)finderWin).setSearchInput(term);
		}
	}

}

