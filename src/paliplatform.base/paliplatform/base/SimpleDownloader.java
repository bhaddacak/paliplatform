/*
 * SimpleDownloader.java
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

package paliplatform.base;

import java.io.*;
import java.nio.file.*;
import java.net.URL;
import java.util.function.Consumer;
import javafx.concurrent.Task;

/** 
 * A simple downloader without dialog and progress report,
 * suitable for downloading a small file.
 * @author J.R. Bhaddacak
 * @version 3.0
 * @since 3.0
 */
public class SimpleDownloader {
	final private String fileURL;
	final private File targetFile;
	final private Consumer<String> successCallback;
	final private Consumer<String> failureCallback;
	private Task<Boolean> dlTask;

	public SimpleDownloader(final String url, final File target, final Consumer<String> cbGood, final Consumer<String> cbBad) {
		fileURL = url;
		targetFile = target;
		successCallback = cbGood;
		failureCallback = cbBad;
		dlTask = download();
	}

	public void start() {
		Utilities.threadPool.submit(dlTask);
	}

	public void restart() {
		dlTask = download();
		start();
	}

	public void stop() {
		if (dlTask != null)
			dlTask.cancel(true);
	}

    private Task<Boolean> download() {
        return new Task<Boolean>() {
            @Override
            protected Boolean call() throws Exception {
				try (final InputStream in = new URL(fileURL).openStream()) {
					final long loadedSize = Files.copy(in, targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
					if (successCallback != null)
						successCallback.accept("" + loadedSize);
				} catch (IOException e) {
					if (failureCallback != null)
						failureCallback.accept(e.toString());
					return false;
				} 
				return true;
            }
        };
    }

}

