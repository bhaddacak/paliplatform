/*
 * TaskItem.java
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

import java.util.*;
import java.io.*;
import java.nio.file.*;

/** 
 * The representation of a download task used with DownloadManager.
 * @author J.R. Bhaddacak
 * @version 3.0
 * @since 3.0
 */

public class TaskItem {
	static enum State { IDLE, QUEUING, STARTED, SKIPPED, DOWNLOADED, INSTALLED, FAILED, CANCELLED }
	static enum SupportedFileExt {
		JAR, ZIP, XZ;
		private final String ext;
		public static final SupportedFileExt[] values = SupportedFileExt.values();
		private SupportedFileExt() {
			ext = this.toString().toLowerCase();
		}
		public String getExt() {
			return ext;
		}
		public static boolean isValid(final String extStr) {
			return Arrays.stream(values).anyMatch(x -> x.getExt().equalsIgnoreCase(extStr));
		}
	}
	private final String taskName;
	private final String fileURL;
	private final File targetFile; // file saved in the cache
	private final File destinationDir; // the final installed location
	private long totalSize = -1; // indeterminate at first
	private boolean skipIfExists = false; // skip download if the file exists in the cache
	private long loadedSize = 0;
	private State state = State.IDLE;

	public TaskItem(final String url, final File target, final File destination) {
		fileURL = url;
		targetFile = target;
		destinationDir = destination;
		taskName = getFileName();
	}

	public String getName() {
		return taskName;
	}

	public String getFileURL() {
		return fileURL;
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

	public boolean targetFileExists() {
		return targetFile != null && targetFile.exists();
	}

	public void setTotalSize(final long size) {
		totalSize = size;
	}

	public long getTotalSize() {
		return totalSize;
	}

	public void setDownloadSkipped(final boolean yn) {
		skipIfExists = yn;
	}

	public boolean isDownloadSkipped() {
		return skipIfExists;
	}

	public boolean isMoveNeeded() {
		return getFileExt().equalsIgnoreCase(SupportedFileExt.JAR.getExt());
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

	public void addLoadedSize(final int size) {
		loadedSize += size;
	}

	public long getLoadedSize() {
		return loadedSize;
	}

	public boolean install() throws IOException {
		boolean success = false;
		if (targetFile.exists()) {
			if (!destinationDir.exists())
				destinationDir.mkdirs();
			final String ext = getFileExt();
			if (SupportedFileExt.isValid(ext)) {
				if (SupportedFileExt.XZ.getExt().equals(ext)) {
					Installer.unXZ(targetFile, destinationDir);
				} else if (SupportedFileExt.ZIP.getExt().equals(ext)) {
					Installer.unzip(targetFile, destinationDir);
				} else if (SupportedFileExt.JAR.getExt().equals(ext)) {
					Files.move(targetFile.toPath(), Path.of(destinationDir.getName(), targetFile.getName()), StandardCopyOption.REPLACE_EXISTING);
				}
				state = State.INSTALLED;
				success = true;
			}
		}
		return success;
	}

	public void resetState() {
		if (state != State.INSTALLED && state != State.FAILED) {
			state = State.IDLE;
			resetLoadedSize();
		}
	}

	public void resetLoadedSize() {
		loadedSize = 0L;
	}

	public void setIdle() {
		state = State.IDLE;
		resetLoadedSize();
	}

	@Override
	public String toString() {
		return taskName;
	}

}
