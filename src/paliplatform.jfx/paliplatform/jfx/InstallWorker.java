/*
 * InstallWorker.java
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

package paliplatform.jfx;

import java.io.*;
import java.util.List;
import java.util.function.Consumer;
import javax.swing.*;

/** 
 * The worker for installing a TaskItem list.
 * @author J.R. Bhaddacak
 * @version 3.0
 * @since 3.0
 */

class InstallWorker extends SwingWorker<Boolean, Boolean> {
	final private List<TaskItem> taskList;
	final private Consumer<DownloadResponse> doWhenDone;

	public InstallWorker(final List<TaskItem> tlist, final Consumer<DownloadResponse> callback) {
		taskList = tlist;
		doWhenDone = callback;
	}

	@Override
	public Boolean doInBackground() throws IOException, InterruptedException {
		try {
			// delete existing JavaFX first
			final File moddir = new File(Installer.ROOTDIR + Installer.MODDIR);
			final File listFile = Installer.getJfxFileList(moddir);
			if (listFile.exists()) {
				Installer.removeFilesInList(moddir, listFile);
				if (SystemInfo.INSTANCE.getOsType() == SystemInfo.OsType.WINDOWS) {
					// in the case of Windows, the native lib resides in bin/ and not listed
					Installer.removeBinNativeLib();
				}
			} else {
				Installer.removeJavaFX(moddir);
				Installer.removeAllNativeLib(moddir);
			}
			// install
			final StringBuilder filelist = new StringBuilder();
			for (final TaskItem task : taskList) {
				if (task.isMoveNeeded())
					filelist.append(task.getFileName()).append(Installer.LINESEP);
				task.install();
			}
			// save file list in case of moving
			if (filelist.length() > 0) {
				filelist.append(Installer.JFX_FILELIST).append(Installer.LINESEP);
				Installer.saveText(filelist.toString(), new File(Installer.ROOTDIR + Installer.MODDIR + Installer.JFX_FILELIST));
			}
		} catch (IOException e) {
			System.err.println(e);
			doWhenDone.accept(new DownloadResponse(false, e.toString()));
		} 
		return true;
	}

	@Override
	public void done() {
		doWhenDone.accept(new DownloadResponse(true, "Installation completed"));
	}

}


