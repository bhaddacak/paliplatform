/*
 * SktDictDownloader.java
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

package paliplatform.sanskrit;

import paliplatform.base.*;
import paliplatform.sanskrit.SanskritUtilities.SktDictBook;

import java.io.File;
import java.util.*;
import java.util.stream.*;

import javafx.scene.control.*;
import javafx.scene.control.Alert;

/** 
 * The downloader dialog for Sanskrit dictionaries.
 * This is a singleton.
 * @author J.R. Bhaddacak
 * @version 3.6
 * @since 3.6
 */
class SktDictDownloader extends ProgressiveDownloader {
	static final SktDictDownloader INSTANCE = new SktDictDownloader();
	private final List<CheckBox> dictCBList = new ArrayList<>();
	private final InfoPopup helpPopup = new InfoPopup();
	private boolean allSelected = true;
	
	private SktDictDownloader() {
		setTitle("Downloader of Sanskrit Dictionaries");
		// add up UI
		final ToolBar toolBar = new ToolBar();
		final Button refreshButton = new Button("Refresh");
		refreshButton.disableProperty().bind(isRunningProperty());
		refreshButton.setOnAction(actionEvent -> init());
		final Button allButton = new Button("", new TextIcon("asterisk", TextIcon.IconSet.AWESOME));
		allButton.setTooltip(new Tooltip("Select all/none"));
		allButton.setOnAction(actionEvent -> toggleAll());
		toolBar.getItems().addAll(refreshButton, allButton);
		for (final SktDictBook book : SktDictBook.books) {
			final CheckBox cb = new CheckBox(book.toString());
			cb.setTooltip(new Tooltip(book.bookName));
			cb.setSelected(allSelected);
			cb.setUserData(book);
			cb.setOnAction(actionEvent -> init());
			dictCBList.add(cb);
		}
		toolBar.getItems().addAll(dictCBList);
		final Button helpButton = new Button("", new TextIcon("circle-question", TextIcon.IconSet.AWESOME));
		helpButton.setOnAction(actionEvent -> helpPopup.showPopup(helpButton, InfoPopup.Pos.BELOW_RIGHT, true));
		toolBar.getItems().add(helpButton);
		mainPane.setTop(toolBar);
		// initialization
		helpPopup.setContentWithText(SanskritUtilities.getTextResource("info-sktdict-downloader.txt"));
		helpPopup.setTextWidth(Utilities.getRelativeSize(36));
		init();
	}

	private void toggleAll() {
		allSelected = !allSelected;
		dictCBList.forEach(x -> x.setSelected(allSelected));	
		init();
	}

	private void init() {
		final List<SktDictBook> dictList = dictCBList.stream()
													.filter(x -> x.isSelected())
													.map(x -> (SktDictBook)x.getUserData())
													.collect(Collectors.toList());
		final DownloadTask[] dlTasks = new DownloadTask[dictList.size()];
		boolean aborted = false;
		for (int i = 0; i < dictList.size(); i++) {
			final SktDictBook dict = dictList.get(i);
			final String url = Utilities.urls.getProperty(dict.toString().toLowerCase() + "_url", "");
			if (url.isEmpty()) {
				Utilities.displayAlert(Alert.AlertType.WARNING, "No URLs available,\nplease update online data, then Refresh");
				aborted = true;
				break;
			}
			final String srcFile = url.substring(url.lastIndexOf("/") + 1);
			final File downloadTarget = new File(Utilities.CACHEPATH + srcFile);
			final File destination = new File(Utilities.ROOTDIR + SanskritUtilities.DICTPATH);
			final DownloadTask dlTask = new DownloadTask(url, downloadTarget, destination, url.endsWith(".zip"));
			// for zip files, use selective unpack mode
			dlTask.setUnpackMode(DownloadTask.UnpackMode.SELECTIVE);
			final String destFileName = dict.getDataFileName();
			dlTask.setDestinationFile(new File(destination, destFileName));
			dlTasks[i] = dlTask;
		}
		if (!aborted)
			setDownloadTask(dlTasks);
	}

	@Override
	public void onFinished() {
		SanskritUtilities.updateSktDictAvailibility();
		if (!SanskritUtilities.getAvailableSktDictData().isEmpty()) {
			if (Utilities.isDBWritable(Utilities.H2DB.SKTDICT)) {
				SanskritUtilities.createSktDictData();
			} else {
				Utilities.displayAlert(Alert.AlertType.WARNING, "Skt. Dict DB locked,\nplease create Skt. Dict data later");
			}
		}
	}

}

