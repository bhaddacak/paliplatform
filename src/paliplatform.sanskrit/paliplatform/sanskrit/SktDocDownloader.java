/*
 * SktDocDownloader.java
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

package paliplatform.sanskrit;

import paliplatform.base.*;

import java.io.File;

import javafx.scene.control.*;
import javafx.scene.control.Alert;

/** 
 * The downloader dialog for Sanskrit documents.
 * This is a singleton.
 * @author J.R. Bhaddacak
 * @version 4.1
 * @since 3.7
 */
class SktDocDownloader extends ProgressiveDownloader {
	static final SktDocDownloader INSTANCE = new SktDocDownloader();
	private final InfoPopup helpPopup = new InfoPopup();
	
	private SktDocDownloader() {
		setTitle("Downloader of Sanskrit Documents");
		// add up UI
		final ToolBar toolBar = new ToolBar();
		final Button refreshButton = new Button("Refresh");
		refreshButton.disableProperty().bind(isRunningProperty());
		refreshButton.setOnAction(actionEvent -> init());
		final Button helpButton = new Button("", new TextIcon("circle-question", TextIcon.IconSet.AWESOME));
		helpButton.setOnAction(actionEvent -> helpPopup.showPopup(helpButton, InfoPopup.Pos.BELOW_LEFT, true));
		toolBar.getItems().addAll(refreshButton, helpButton);
		mainPane.setTop(toolBar);
		// initialization
		helpPopup.setContentWithText(SanskritUtilities.getTextResource("info-sktdoc-downloader.txt"));
		helpPopup.setTextWidth(Utilities.getRelativeSize(26));
		init();
	}

	private void init() {
		final String url = Utilities.urls.getProperty("gretil_skt_url", "");
		if (url.isEmpty()) {
			Utilities.displayAlert(Alert.AlertType.WARNING, "No URL available,\nplease update online info, then Refresh");
		} else {
			final String srcFile = url.substring(url.lastIndexOf("/") + 1);
			final File downloadTarget = new File(Utilities.ROOTDIR + Utilities.CACHEPATH + srcFile);
			final File destination = new File(Utilities.ROOTDIR + SanskritUtilities.TEXTPATH);
			final DownloadTask dlTask = new DownloadTask(url, downloadTarget, destination, false);
			setDownloadTask(dlTask);
		}
	}

	@Override
	public void onFinished() {
		// no-op
	}

}

