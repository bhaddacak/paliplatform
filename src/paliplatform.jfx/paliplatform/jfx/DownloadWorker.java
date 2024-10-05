/*
 * DownloadWorker.java
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
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.*;
import java.util.concurrent.*;
import java.net.*;
import javax.swing.*;

/** 
 * The worker for downloading a TaskItem.
 * @author J.R. Bhaddacak
 * @version 3.0
 * @since 3.0
 */

class DownloadWorker extends SwingWorker<Long, Long> {
	final private DownloadManager dlManager;
	final private TaskItem taskItem;

	public DownloadWorker(final DownloadManager man, final TaskItem task) {
		dlManager = man;
		taskItem = task;
	}

	@Override
	public Long doInBackground() throws IOException, InterruptedException {
		URL url = null;
		ReadableByteChannel rchannel = null;
		try {
			// get file total size first
			url = new URL(taskItem.getFileURL());
			final HttpURLConnection httpConn = (HttpURLConnection)url.openConnection(); 
			httpConn.setRequestMethod("HEAD"); 
			final long size = httpConn.getContentLengthLong();
			taskItem.setTotalSize(size);
			rchannel = Channels.newChannel(url.openStream()); 
		} catch (IOException e) {
			taskItem.setState(TaskItem.State.FAILED);
			dlManager.taskFinished(new DownloadResponse(false, "[" + taskItem.getName() + "] " + e.toString(), taskItem));
			return 0L;
		}
		final File cacheFile = taskItem.getTargetFile();
		final File cachePath = cacheFile.getParentFile();
		if (cachePath != null && !cachePath.exists())
			cachePath.mkdirs();
		dlManager.updateMessage("[" + taskItem.getName() + "] Started");
		try (final FileOutputStream fos = new FileOutputStream(cacheFile)) { 
			final FileChannel wchannel = fos.getChannel();
			final ByteBuffer buffer = ByteBuffer.allocateDirect(1024);
			while (rchannel.read(buffer) != -1) {
				buffer.flip();
				while (buffer.hasRemaining()) {
					taskItem.addLoadedSize(wchannel.write(buffer));
					publish(taskItem.getLoadedSize());
				}
				buffer.clear();
			}
		} catch (IOException e) {
			if (isCancelled()) {
				taskItem.setState(TaskItem.State.CANCELLED);
				dlManager.taskFinished(new DownloadResponse(false, "[" + taskItem.getName() + "] Download cancelled", taskItem));
			} else {
				taskItem.setState(TaskItem.State.FAILED);
				dlManager.taskFinished(new DownloadResponse(false, "[" + taskItem.getName() + "] " + e.toString(), taskItem));
			}
		}
		return taskItem.getLoadedSize();
	}

	@Override
	public void process(List<Long> sizes) {
		if (isCancelled()) return;
		for (final long s : sizes)
			dlManager.updateDownloadStatus(taskItem, s);
	}

	@Override
	public void done() {
		if (taskItem.getLoadedSize() == 0 && !isCancelled()) {
			taskItem.setState(TaskItem.State.FAILED);
			dlManager.taskFinished(new DownloadResponse(false, "[" + taskItem.getName() + "] Download failed", taskItem));
		} else {
			if (isCancelled()) {
				taskItem.setState(TaskItem.State.CANCELLED);
				dlManager.taskFinished(new DownloadResponse(false, "[" + taskItem.getName() + "] Download cancelled", taskItem));
				taskItem.deleteTargetFile();
			} else {
				taskItem.setState(TaskItem.State.DOWNLOADED);
				dlManager.taskFinished(new DownloadResponse(true, "[" + taskItem.getName() + "] Download completed", taskItem));
			}
		}
	}

}

