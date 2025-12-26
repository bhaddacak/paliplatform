/*
 * CstXmlDownloader.java
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
import paliplatform.base.ProgressiveDownloader.WindowType;

import java.io.File;
import java.util.*;
import java.util.stream.*;
import javafx.scene.control.*;

/** 
 * The downloader dialog for CST Devanagari XML data.
 * This is a singleton.
 * @author J.R. Bhaddacak
 * @version 3.7
 * @since 3.3
 */
class CstXmlDownloader extends ProgressiveDownloader {
	static final CstXmlDownloader INSTANCE = new CstXmlDownloader(WindowType.NO_OPTION_BOX);
	private final List<String> textGroupList = List.of("All", "-", "Vin", "Sut", "Abh", "-", "Mūl", "Att", "Ṭīk", "Aññ");
	private final List<Toggle> textGroupItemList = new ArrayList<>();
	private final ToggleGroup textGroup = new ToggleGroup();
	private final Map<String, DocumentInfo> docInfoMap;
	private final InfoPopup helpPopup = new InfoPopup();
	
	private CstXmlDownloader(final WindowType wintype) {
		super(wintype);
		setTitle("CST Devanāgarī XML Downloader");
		// add up UI
		final ToolBar toolBar = new ToolBar();
		final Button refreshButton = new Button("Refresh");
		refreshButton.disableProperty().bind(isRunningProperty());
		refreshButton.setOnAction(actionEvent -> {
			final Toggle selected = (Toggle)textGroup.getSelectedToggle();
			init(((RadioMenuItem)selected).getText());
		});
		final MenuButton optionMenu = new MenuButton("", new TextIcon("check-double", TextIcon.IconSet.AWESOME));
		optionMenu.setTooltip(new Tooltip("Text group selection"));
		for (final String tg : textGroupList) {
			if (!tg.equals("-")) {
				final RadioMenuItem tgMenuItem = new RadioMenuItem(tg);
				textGroupItemList.add(tgMenuItem);
				tgMenuItem.setToggleGroup(textGroup);
				optionMenu.getItems().add(tgMenuItem);
			} else {
				optionMenu.getItems().add(new SeparatorMenuItem());
			}
		}
		textGroup.selectToggle(textGroupItemList.get(0));
        textGroup.selectedToggleProperty().addListener((observable) -> {
			if (textGroup.getSelectedToggle() != null) {
				final Toggle selected = (Toggle)textGroup.getSelectedToggle();
				init(((RadioMenuItem)selected).getText());
//~ 				init(selected.getText());
			}
        });
		final Button helpButton = new Button("", new TextIcon("circle-question", TextIcon.IconSet.AWESOME));
		helpButton.setOnAction(actionEvent -> helpPopup.showPopup(helpButton, InfoPopup.Pos.BELOW_LEFT, true));
		toolBar.getItems().addAll(refreshButton, optionMenu, helpButton);
		mainPane.setTop(toolBar);
		// initialization
		helpPopup.setContentWithText(ReaderUtilities.getTextResource("info-cstxmldownloader.txt"));
		helpPopup.setTextWidth(Utilities.getRelativeSize(26));
		docInfoMap = ReaderUtilities.corpusMap.get(Corpus.Collection.CSTDEVA).getDocInfoMap();
		init(textGroupList.get(0));
	}

	private void init(final String textGroup) {
		final String cstxmlURL = Utilities.urls.getProperty("cst_xml_url");
		final List<String> fileList = getFileList(textGroup);
		final DownloadTask[] dlTasks = new DownloadTask[fileList.size()];
		for (int i = 0; i < fileList.size(); i++) {
			final String file = fileList.get(i);
			final String url = cstxmlURL + "/deva/" + file;
			final File downloadTarget = new File(Utilities.CACHEPATH + file);
			final String cstxmlRoot = ReaderUtilities.corpusMap.get(Corpus.Collection.CSTDEVA).getRootName();
			final File destination = new File(Utilities.ROOTDIR + ReaderUtilities.TEXTPATH + cstxmlRoot);
			final DownloadTask dlTask = new DownloadTask(url, downloadTarget, destination, false);
			dlTasks[i] = dlTask;
		}
		setDownloadTask(dlTasks);
	}

	private List<String> getFileList(final String textGroup) {
		List<String> result = null;
		switch (textGroup) {
			case "All":
				result = docInfoMap.values().stream()
						.map(DocumentInfo::getFileNameWithExt)
						.collect(Collectors.toList());
				break;
			case "Vin":
			case "Sut":
			case "Abh":
			case "Aññ":
				final String grp = textGroup.equals("Aññ") ? "ext" : textGroup.toLowerCase();
				result = docInfoMap.values().stream()
						.filter(x -> x.getGroup().equals(grp))
						.map(DocumentInfo::getFileNameWithExt)
						.collect(Collectors.toList());
				break;
			case "Mūl":
			case "Att":
			case "Ṭīk":
				final Map<String, String> classMap = Map.of("Mūl", "mul", "Att", "att", "Ṭīk", "tik");
				final String cls = classMap.get(textGroup);
				result = docInfoMap.values().stream()
						.filter(x -> x.getDocClass().equals(cls))
						.map(DocumentInfo::getFileNameWithExt)
						.collect(Collectors.toList());
				break;
		}
		return result == null ? Collections.emptyList() : result;
	}

	@Override
	public void onFinished() {
		ReaderUtilities.checkIfSuttaCentralAvailable();
		if (ReaderUtilities.suttaCentralAvailable.get()) {
			ReaderUtilities.createScHeads();
			ReaderUtilities.scSuttaInfoMap.clear();
			ReaderUtilities.loadScSuttaInfo();
			ReaderUtilities.updateCorpusList();
		}
	}

}

