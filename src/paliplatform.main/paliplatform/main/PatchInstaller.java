/*
 * PatchInstaller.java
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

package paliplatform.main;

import paliplatform.base.*;

import java.util.*;
import java.util.stream.*;
import java.io.File;
import java.io.IOException;
import java.lang.module.ModuleDescriptor;
import javafx.scene.control.*;
import javafx.application.Platform;

/** 
 * The downloader and installer for the program's patches.
 * This is a singleton.
 * @author J.R. Bhaddacak
 * @version 3.6
 * @since 3.0
 */
class PatchInstaller extends ProgressiveDownloader {
	static final PatchInstaller INSTANCE = new PatchInstaller();
	private static final String INFO_HEAD = "Patch details";
	private final InfoPopup detailPopup = new InfoPopup();
	private final InfoPopup helpPopup = new InfoPopup();
	private final Map<String, ModuleDescriptor.Version> moduleMap = new HashMap<>();
	private final Map<String, File> fileMap = new HashMap<>();
	private final List<String> deleteList = new ArrayList<>();
	
	private PatchInstaller() {
        setTitle("Patch Installer");
		// add up UI
		final ToolBar toolBar = new ToolBar();
		final Button refreshButton = new Button("Refresh");
		refreshButton.disableProperty().bind(isRunningProperty());
		refreshButton.setOnAction(actionEvent -> init());
		final Button infoButton = new Button("", new TextIcon("circle-info", TextIcon.IconSet.AWESOME));
		infoButton.setTooltip(new Tooltip("Patch information"));
		infoButton.setOnAction(actionEvent -> detailPopup.showPopup(infoButton, InfoPopup.Pos.BELOW_LEFT, true));
		final Button helpButton = new Button("", new TextIcon("circle-question", TextIcon.IconSet.AWESOME));
		helpButton.setOnAction(actionEvent -> helpPopup.showPopup(helpButton, InfoPopup.Pos.BELOW_LEFT, true));
		toolBar.getItems().addAll(refreshButton, infoButton, helpButton);
		mainPane.setTop(toolBar);
		// initialization
		helpPopup.setContentWithText(PaliPlatform.getTextResource("info-patch-installer.txt"));
		helpPopup.setTextWidth(Utilities.getRelativeSize(28));
		detailPopup.setTextWidth(Utilities.getRelativeSize(18));
		init();
	}

	private void init() {
		moduleMap.clear();
		deleteList.clear();
		final String latestPatch = Utilities.getSetting("latest-patch");
		final List<String> patchUrlList = new ArrayList<>();
		for (int i = 1; i < 100; i++) {
			final String pkey = String.format("patch%02d_url", i);
			final String url = Utilities.urls.getProperty(pkey);
			if (url != null) {
				final String version = getPatchVersion(url);
				final boolean doAdd = latestPatch == null
										? true
										: latestPatch.compareTo(version) < 0
											? true
											: false;
				if (doAdd) {
					patchUrlList.add(url);
				}
			}
		}
		if (!patchUrlList.isEmpty()) {
			final DownloadTask[] tasks = new DownloadTask[patchUrlList.size()];
			final StringBuilder taskNames = new StringBuilder();
			for (int i = 0; i < tasks.length; i++) {
				final String url = patchUrlList.get(i);
				final String patchName = url.substring(url.lastIndexOf("/") + 1);
				final File downloadTarget = new File(Utilities.CACHEPATH + patchName);
				final File destination = new File(Utilities.ROOTDIR);
				final DownloadTask dlTask = new DownloadTask(url, downloadTarget, destination, true);
				tasks[i] = dlTask;
				taskNames.append("\n").append(patchName);
			}
			setDownloadTask(tasks);
			detailPopup.setContentWithText(INFO_HEAD + taskNames.toString());
		} else {
			message.setText("The program is up-to-date");
			detailPopup.setContentWithText(INFO_HEAD + "\nNo patch to install.");
			startStopButton.setDisable(true);
		}
	}

	private static String getPatchVersion(final String name) {
		return name.substring(name.lastIndexOf("-") + 1, name.lastIndexOf("."));
	}

	@Override
	public void onFinished() {
		final Optional<String> latelyInstalled = taskList.stream()
												.filter(x -> x.getState() == DownloadTask.State.INSTALLED)
												.map(DownloadTask::getFileName)
												.max(String::compareTo);
		if (latelyInstalled.isPresent()) {
			final String ver = getPatchVersion(latelyInstalled.get());
			Utilities.setSetting("latest-patch", ver);
			MainProperties.INSTANCE.saveSettings();
		}
		restart();
	}

	private void restart() {
		try {
			final String cmd = System.getProperty("os.name").toLowerCase().contains("windows") 
				? "launch.cmd"
				: "./launch.sh";
			final Process proc = Runtime.getRuntime().exec(cmd);
			proc.waitFor();
			System.out.println(proc);
			Platform.exit();
		} catch (InterruptedException | IOException e) {
			System.err.println(e);
		}
	}

}

