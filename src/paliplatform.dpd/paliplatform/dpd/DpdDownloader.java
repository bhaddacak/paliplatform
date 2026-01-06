/*
 * DpdDownloader.java
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

package paliplatform.dpd;

import paliplatform.base.*;

import java.io.File;
import javafx.scene.control.*;

/** 
 * The downloader dialog for DPD database. This is a singleton.
 * @author J.R. Bhaddacak
 * @version 4.0
 * @since 3.0
 */
class DpdDownloader extends ProgressiveDownloader {
	static final DpdDownloader INSTANCE = new DpdDownloader();
	private final CheckBox cbLatest = new CheckBox("Latest");
	private final Label version = new Label();
	private final InfoPopup helpPopup = new InfoPopup();
	
	private DpdDownloader() {
        setTitle("DPD Downloader");
		// add up UI
		final ToolBar toolBar = new ToolBar();
		final Button refreshButton = new Button("Refresh");
		refreshButton.disableProperty().bind(isRunningProperty());
		refreshButton.setOnAction(actionEvent -> init());
		cbLatest.setTooltip(new Tooltip("Always download the latest version"));
		cbLatest.setOnAction(actionEvent -> init());
		final Button helpButton = new Button("", new TextIcon("circle-question", TextIcon.IconSet.AWESOME));
		helpButton.setOnAction(actionEvent -> helpPopup.showPopup(helpButton, InfoPopup.Pos.BELOW_CENTER, true));
		toolBar.getItems().addAll(refreshButton, cbLatest, helpButton, version);
		mainPane.setTop(toolBar);
		// initialization
		helpPopup.setContentWithText(DpdUtilities.getTextResource("info-downloader.txt"));
		helpPopup.setTextWidth(Utilities.getRelativeSize(32));
		init();
	}

	private void init() {
		final String dbURL;
		if (cbLatest.isSelected()) {
			dbURL = "https://github.com/digitalpalidictionary/dpd-db/releases/latest/download/dpd.db.tar.bz2";
			version.setText("[latest]");	
		} else {
			dbURL = Utilities.urls.getProperty("dpd_db_url");
			String ver = "undetected";
			final String[] chunks = dbURL.split("/");
			for (final String c : chunks) {
				if (c.matches("^v\\d.*")) {
					ver = c;
					break;
				}
			}
			version.setText("[" + ver + "]");
		}
		final String dbname = dbURL.substring(dbURL.lastIndexOf("/") + 1);
		final File downloadTarget = new File(Utilities.CACHEPATH + dbname);
		final File destination = new File(Utilities.ROOTDIR + Utilities.DBPATH);
		final DownloadTask dlTask = new DownloadTask(dbURL, downloadTarget, destination, true);
		setDownloadTask(dlTask);
	}

	@Override
	public void onFinished() {
		if (!skipInstall()) {
			DpdUtilities.checkIfDpdAvailable();
			Utilities.initializeDpdDB();
		}
	}

}

