/*
 * ScDownloader.java
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

package paliplatform.reader;

import paliplatform.base.*;

import java.io.File;
import javafx.scene.control.*;

/** 
 * The downloader dialog for SuttaCentral bilara data.
 * This is a singleton.
 * @author J.R. Bhaddacak
 * @version 3.0
 * @since 3.0
 */
class ScDownloader extends ProgressiveDownloader {
	static final ScDownloader INSTANCE = new ScDownloader();
	private final InfoPopup helpPopup = new InfoPopup();
	
	private ScDownloader() {
        setTitle("SuttaCentral Data Downloader");
		// add up UI
		final ToolBar toolBar = new ToolBar();
		final Button helpButton = new Button("", new TextIcon("circle-question", TextIcon.IconSet.AWESOME));
		helpButton.setOnAction(actionEvent -> helpPopup.showPopup(helpButton, InfoPopup.Pos.BELOW_LEFT, true));
		toolBar.getItems().addAll(helpButton);
		mainPane.setTop(toolBar);
		// initialization
		helpPopup.setContentWithText(ReaderUtilities.getTextResource("info-scdownloader.txt"));
		helpPopup.setTextWidth(Utilities.getRelativeSize(26));
		init();
	}

	private void init() {
		final String scURL = Utilities.urls.getProperty("sc_data_url");
		final File downloadTarget = new File(Utilities.CACHEPATH + ReaderUtilities.BILARA_DATA);
		final File destination = new File(Utilities.ROOTDIR + ReaderUtilities.SCPATH);
		final DownloadTask dlTask = new DownloadTask(scURL, downloadTarget, destination, false);
		setDownloadTask(dlTask);
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

