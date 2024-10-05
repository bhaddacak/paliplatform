/*
 * DownloadResponse.java
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

/** 
 * The response from download workers.
 * @author J.R. Bhaddacak
 * @version 3.0
 * @since 3.0
 */

class DownloadResponse {
	final boolean isSuccess;
	final String message;
	final TaskItem taskItem;

	public DownloadResponse(final boolean yn, final String mess) {
		this(yn, mess, null);
	}

	public DownloadResponse(final boolean yn, final String mess, final TaskItem task) {
		isSuccess = yn;
		message = mess;
		taskItem = task;
	}

	public boolean isSuccess() {
		return isSuccess;
	}

	public String getMessage() {
		return message;
	}

	public TaskItem getTaskItem() {
		return taskItem;
	}

}

