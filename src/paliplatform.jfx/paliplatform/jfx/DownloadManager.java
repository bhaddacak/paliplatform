/*
 * DownloadManager.java
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
import java.util.stream.*;

/** 
 * The download manager for TaskItems.
 * @author J.R. Bhaddacak
 * @version 3.0
 * @since 3.0
 */

public class DownloadManager {
	private InstallerWin win;
	private int maxJobs;
	private Set<TaskItem> taskSet = new LinkedHashSet<>();
	private final Map<TaskItem, DownloadWorker> downloadJobs = new HashMap<>(10);
	private boolean running = false;

	public DownloadManager(final InstallerWin iwin, final int max) {
		win = iwin;
		maxJobs = max;
	}

	public int getMaxJobs() {
		return maxJobs;
	}

	private void addTaskList(final List<TaskItem> tlist) {
		tlist.forEach(t -> t.setState(TaskItem.State.QUEUING));
		taskSet.addAll(tlist);
	}

	public void setTaskList(final List<TaskItem> tlist) {
		taskSet.clear();
		addTaskList(tlist);
	}

	public void resetMaxJobs(final int max) {
		maxJobs = max;
		if (running)
			start();
	}

	public boolean isRunning() {
		return running;
	}

	public void start() {
		running = true;
		if (downloadJobs.size() >= maxJobs) return;
		for (final TaskItem task : taskSet) {
			if (task.getState() == TaskItem.State.QUEUING) {
				if (task.targetFileExists() && task.isDownloadSkipped()) {
					// skip
					task.setState(TaskItem.State.SKIPPED);
				} else {
					// download
					final DownloadWorker worker = new DownloadWorker(this, task);
					downloadJobs.put(task, worker);
					task.setState(TaskItem.State.STARTED);
					worker.execute();
					if (downloadJobs.size() >= maxJobs) break;
				}
			}
		}
		if (downloadJobs.isEmpty()) {
			finish(TaskItem.State.SKIPPED);
		}
	}

	public void stopAll() {
		running = false;
		for (final DownloadWorker w : downloadJobs.values())
			w.cancel(true);
		for (final TaskItem t : taskSet)
			t.resetState();
		downloadJobs.clear();
		taskSet.clear();
	}

	public void taskFinished(final Object response) {
		final DownloadResponse res = (DownloadResponse)response;
		final TaskItem task = res.getTaskItem();
		win.addMessage(res.getMessage());
		if (task.getState() == TaskItem.State.CANCELLED || task.getState() == TaskItem.State.FAILED) {
			downloadJobs.clear();
			taskSet.clear();
			finish(task.getState());
		} else {
			updateQueue(task);
		}
	}

	public void updateMessage(final String mess) {
		win.addMessage(mess);
	}

	public void setStatus(final String mess) {
		win.setStatus(mess);
	}

	public void updateDownloadStatus(final TaskItem task, final long size) {
		win.updateStatus(task, size);
	}
	
	private void updateQueue(final TaskItem task) {
		downloadJobs.remove(task);
		final long waiting = taskSet.stream().filter(x -> x.getState() == TaskItem.State.QUEUING).count();
		if (waiting > 0) {
			start();
		} else {
			finish(task.getState());
		}
	}

	private void finish(final TaskItem.State state) {
		running = false;
		win.downloadFinished(state);
	}
	
}

