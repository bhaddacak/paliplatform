/*
 * CorpusSelectorBox.java
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

package paliplatform.reader;

import paliplatform.base.*;

import java.util.*;
import java.util.regex.*;

import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.beans.property.SimpleBooleanProperty;

/** 
 * The box having components for selecting text groups in a corpus.
 * @author J.R. Bhaddacak
 * @version 3.0
 * @since 3.0
 */
public class CorpusSelectorBox extends HBox {
	private final ChoiceBox<String> corpusChoice = new ChoiceBox<>();
	private final ChoiceBox<TextGroup> textGroupChoice = new ChoiceBox<>();
	private final SimpleBooleanProperty boldAvailable = new SimpleBooleanProperty(true);
	private final InfoPopup infoPopup = new InfoPopup();
	private Corpus currCorpus;
	private Runnable action;
	
	public CorpusSelectorBox(final Corpus corpus, final Runnable func) {
		currCorpus = corpus;
		action = func;
		updateTextGroupList(currCorpus);
		corpusChoice.itemsProperty().setValue(ReaderUtilities.corpusAbbrList);
		corpusChoice.getSelectionModel().select(0);
		corpusChoice.setTooltip(new Tooltip(currCorpus.getName()));
		corpusChoice.setOnAction(actionEvent -> {
			final String selId = corpusChoice.getSelectionModel().getSelectedItem().toLowerCase();
			currCorpus = ReaderUtilities.corpusMap.get(Corpus.Collection.idMap.get(selId));
			corpusChoice.setTooltip(new Tooltip(currCorpus.getName()));
			updateTextGroupList(currCorpus);
			updateBoldAvailable();
			if (action != null)
				action.run();
		});
		final Button infoButton = new Button("", new TextIcon("info", TextIcon.IconSet.AWESOME));
		infoButton.setTooltip(new Tooltip("Show corpus information"));
		infoButton.setOnAction(actionEvent -> {
			final String corpusId = corpusChoice.getSelectionModel().getSelectedItem().toLowerCase();
			final Corpus cp = ReaderUtilities.corpusMap.get(Corpus.Collection.idMap.get(corpusId));
			if (cp == null) return;
			infoPopup.setContentWithText(cp.getCorpusInformation());
			infoPopup.showPopup(infoButton, InfoPopup.Pos.BELOW_LEFT, true);
		});
		textGroupChoice.setPrefWidth(Utilities.getRelativeSize(20));
		getChildren().addAll(infoButton, corpusChoice, textGroupChoice);
		// other init
		infoPopup.setTextWidth(Utilities.getRelativeSize(32));
	}

	public void init() {
		corpusChoice.getSelectionModel().select(0);
		textGroupChoice.getSelectionModel().select(0);
	}

	private void updateTextGroupList(final Corpus corpus) {
		final List<TextGroup> textGroupList = new ArrayList<>();
		textGroupList.add(Corpus.tgAll);
		if (corpus.hasExtraGroup())
			textGroupList.add(Corpus.tgNoExt);
		final List<TextGroup> docClassList = corpus.getTextClassList();
		if (docClassList.isEmpty()) {
			textGroupList.addAll(corpus.getTextBasketList());
			if (corpus.hasDMSA())
				textGroupList.addAll(List.of(Corpus.tgVDMSA, Corpus.tgDMSA));
		} else {
			textGroupList.addAll(docClassList);
			textGroupList.addAll(corpus.getTextBasketList());
			textGroupList.addAll(corpus.getTextGroupCombination());
		}
		textGroupList.addAll(corpus.getTextExtraGroupList());
		textGroupChoice.setOnAction(null);
		textGroupChoice.getItems().clear();
		textGroupChoice.getItems().addAll(textGroupList);
		textGroupChoice.getSelectionModel().select(0);
		textGroupChoice.setTooltip(new Tooltip(Corpus.tgAll.getPaliName()));
		textGroupChoice.setOnAction(actionEvent -> {
			final TextGroup selected = textGroupChoice.getSelectionModel().getSelectedItem();
			textGroupChoice.setTooltip(new Tooltip(selected.getPaliName()));
			if (action != null)
				action.run();
		});
	}

	public Corpus getSelectedCorpus() {
		return currCorpus;
	}

	public TextGroup getSelectedTextGroup() {
		return textGroupChoice.getSelectionModel().getSelectedItem();
	}

	public String getSelectedIdString() {
		return currCorpus.getId() + "-" + getSelectedTextGroup().getAbbrev();
	}

	public SimpleBooleanProperty getBoldAvailable() {
		return boldAvailable;
	}

	private void updateBoldAvailable() {
		if (currCorpus != null) {
			final Corpus.Collection col = currCorpus.getCollection();
			boldAvailable.set(Corpus.hasFullStructure(col) || Corpus.hasAlmostFullButNotes(col));
		}
	}

	public Pattern getFileFilterPattern() {
		final Corpus.Collection col = currCorpus.getCollection();
		return Corpus.getFileFilterPattern(col, getSelectedTextGroup().getAbbrev());
	}

}

