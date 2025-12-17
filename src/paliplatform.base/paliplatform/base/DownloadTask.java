/*
 * DownloadTask.java
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

package paliplatform.base;

import java.io.*;
import java.nio.file.*;

/** 
 * The representation of a download task used with ProgressiveDownloader.
 * @author J.R. Bhaddacak
 * @version 3.6
 * @since 3.0
 */
public class DownloadTask {
	public static enum State { IDLE, STARTED, SKIPPED, DOWNLOADED, FAILED, CANCELLED, INSTALLED }
	public static enum UnpackMode { ALL, SELECTIVE }
	private final String fileURL;
	private final File targetFile; // file saved in the cache
	private final File destinationDir; // the final installed location
	private final boolean unpackNeeded;
	private UnpackMode unpackMode;
	private File destinationFile; // used only when selective unpack
	private long totalSize = -1; // indeterminate at first
	private State state = State.IDLE;
	
	public DownloadTask(final String url, final File target, final File destination, final boolean unpack) {
		fileURL = url;
		targetFile = getValidTargetFile(target);
		destinationDir = destination;
		unpackNeeded = unpack;
		unpackMode = UnpackMode.ALL;
		destinationFile = null; // set this when used
	}

	public String getFileURL() {
		return fileURL;
	}

	public void setUnpackMode(final UnpackMode mode) {
		unpackMode = mode;
	}

	public UnpackMode getUnpackMode() {
		return unpackMode;
	}

	private static File getValidTargetFile(final File file) {
		final String name = file.getName().toLowerCase();
		if (name.endsWith("tgz")) {
			final String newName = name.substring(0, name.lastIndexOf(".")) + ".tar.gz";
			return new File(file.getParentFile(), newName);
		} else {
			return file;
		}
	}

	public String getFileName() {
		return targetFile.getName();
	}

	public String getFileExt() {
		final String name = getFileName();
		return name.substring(name.lastIndexOf(".") + 1).toLowerCase();
	}

	public File getTargetFile() {
		return targetFile;
	}

	public File getDestination() {
		return destinationDir;
	}

	public void setDestinationFile(final File destFile) {
		destinationFile = destFile;
	}

	public File getDestinationFile() {
		return destinationFile;
	}

	public boolean targetFileExists() {
		return targetFile != null && targetFile.exists();
	}

	public void setTotalSize(final long size) {
		totalSize = size;
	}

	public boolean isUnpackNeeded() {
		return unpackNeeded;
	}

	public void setState(final State state) {
		this.state = state;
	}

	public State getState() {
		return state;
	}

	public boolean isInstallable() {
		return state == State.DOWNLOADED || state == State.SKIPPED;
	}

	public void copyTargetToDest() {
		if (!targetFileExists()) return;
		try {
			if (!destinationDir.exists())
				destinationDir.mkdirs();
			final File dest = new File(destinationDir, getFileName());
			Files.copy(targetFile.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			System.err.println(e);
		}
	}

	public void deleteTargetFile() {
		if (targetFileExists())
			targetFile.delete();
	}

}
