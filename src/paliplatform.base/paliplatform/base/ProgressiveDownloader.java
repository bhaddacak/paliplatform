/*
 * ProgressiveDownloader.java
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

import java.util.*;
import java.util.stream.*;
import java.io.*;
import java.nio.file.*;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.net.*;

import javafx.scene.*;
import javafx.scene.image.*;
import javafx.scene.text.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.geometry.*;
import javafx.stage.Stage;
import javafx.stage.Modality;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.beans.property.SimpleBooleanProperty;

import org.apache.commons.compress.compressors.gzip.*;
import org.apache.commons.compress.compressors.bzip2.*;
import org.apache.commons.compress.compressors.xz.*;

/** 
 * The progressive downloader dialog with progress report and post-download
 * installation, suitable for downloading sizeable files.
 * @author J.R. Bhaddacak
 * @version 3.0
 * @since 3.0
 */
public class ProgressiveDownloader extends Stage {
	private static final String START = "Start";
	private static final double MEGA = 1024 * 1024;
	private static final Set<String> formatExtSet = Set.of(".tar.gz", ".tar.xz", ".tar.bz2", ".zip");
	private static final File tempDir = new File(Utilities.CACHEPATH + "temp" + File.separator);
	protected final List<DownloadTask> taskList = new ArrayList<>();
	private final LinkedList<DownloadTask> downloadQueue = new LinkedList<>();
	private final LinkedList<DownloadTask> unpackQueue = new LinkedList<>();
	protected final Button startStopButton = new Button(START);
	private final CheckBox cbSkipDownload = new CheckBox("Skip download if in cache");
	private final CheckBox cbInstall = new CheckBox("Install/unpack");
	protected final BorderPane mainPane = new BorderPane();
	private final ProgressBar progressBar = new ProgressBar();
	private final VBox optionBox = new VBox(5);
	private final Label sizeDisplay = new Label();
	protected final Label message = new Label();
	private final Map<DownloadTask.State, Integer> stateMap = new EnumMap<>(DownloadTask.State.class);
	private final SimpleBooleanProperty isRunning = new SimpleBooleanProperty(false);
	private Task<Boolean> workerTask = null;
	private DownloadTask currDownloadTask = null;
	
	public ProgressiveDownloader() {
		// set up the stage
        setTitle("Progressive Downloader");
		getIcons().add(new Image(Utilities.class.getResourceAsStream("resources/images/cloud-arrow-down.png")));
		initModality(Modality.NONE);
		initOwner(null);
		setResizable(false);
		progressBar.setPrefWidth(Utilities.getRelativeSize(24));
		progressBar.setProgress(0);
		final VBox contentBox = new VBox(5);
		contentBox.setAlignment(Pos.TOP_CENTER);
		contentBox.setPadding(new Insets(20, 20, 10, 20));
		// add option box
		optionBox.setAlignment(Pos.CENTER);
		final VBox cbBox = new VBox(5);
		cbBox.setPadding(new Insets(0, 0, 0, 60));
		cbSkipDownload.setTooltip(new Tooltip("Skip download if the file already exists in the cache"));
		cbSkipDownload.setSelected(false);
		cbInstall.setTooltip(new Tooltip("Install or unpack the file to the destination"));
		cbInstall.setSelected(true);
		cbBox.getChildren().addAll(cbSkipDownload, cbInstall);
		optionBox.getChildren().add(cbBox);
		// add button bar
		final HBox buttonBar = new HBox(5);
		buttonBar.setAlignment(Pos.CENTER);
		buttonBar.setPadding(new Insets(10, 0, 0, 0));
		startStopButton.setDisable(taskList.isEmpty());
		startStopButton.setOnAction(actionEvent -> toggleStartDownload());
		final Button closeButton = new Button("Close");
		closeButton.setOnAction(actionEvent -> close());
		buttonBar.getChildren().addAll(startStopButton, closeButton);
		contentBox.getChildren().addAll(progressBar, sizeDisplay, optionBox, buttonBar);
		// set main pane
		mainPane.setCenter(contentBox);
		// status bar
		final HBox statusBar = new HBox();
		statusBar.setPadding(new Insets(0, 0, 3, 5));
		statusBar.getChildren().add(message);
		mainPane.setBottom(statusBar);

		final Scene scene = new Scene(mainPane);
		setOnShowing(e -> refreshTheme());
		setScene(scene);

		// some init
		stateMap.put(DownloadTask.State.DOWNLOADED, 0);
		stateMap.put(DownloadTask.State.SKIPPED, 0);
		stateMap.put(DownloadTask.State.INSTALLED, 0);
	}

	public void display() {
		if (this.isShowing()) {
			this.toFront();
		} else {
			show();
		}	
	}

	public static boolean isFormatValid(final String filename) {
		return formatExtSet.stream().anyMatch(x -> filename.endsWith(x));
	}

    public void refreshTheme() {
		final Scene scene = getScene();
		scene.getStylesheets().clear();
		final String stylesheet = Utilities.getCustomStyleSheet();
		if (!stylesheet.isEmpty())
			scene.getStylesheets().add(stylesheet);
	}

	private void init() {
		final int count = taskList.size();
		final String s = count > 1 ? "s" : "";
		final String mess = count + " task" + s + " to download";
		init(mess);
	}

	private void init(final String mess) {
		taskList.forEach(x -> x.setState(DownloadTask.State.IDLE));
		downloadQueue.clear();
		unpackQueue.clear();
		progressBar.setProgress(0);
		sizeDisplay.setText("");
		startStopButton.setText(START);
		startStopButton.setDisable(taskList.isEmpty());
		message.setText(mess);
	}

	public void setDownloadTask(final DownloadTask... tasks) {
		taskList.clear();
		taskList.addAll(Arrays.asList(tasks));
		init();
	}

	protected SimpleBooleanProperty isRunningProperty() {
		return isRunning;
	}
	
	private void finished() {
		isRunning.set(false);
		final String mess = "Finished: " + stateMap.get(DownloadTask.State.DOWNLOADED) + " downloaded, "
				+ stateMap.get(DownloadTask.State.SKIPPED) + " skipped, " 
				+ stateMap.get(DownloadTask.State.INSTALLED) + " installed";
		onFinished();
		init(mess);
	}

	protected void onFinished() {
		// implement this
	}

	private void toggleStartDownload() {
		if (workerTask == null || !workerTask.isRunning()) {
			// start
			startStopButton.setText("STOP");
			startDownload();
		} else {
			// stop
			startStopButton.setText(START);
			stopDownload();
		}
	}

	private void startDownload() {
		if (taskList.isEmpty()) return;
		if (cbSkipDownload.isSelected()) {
			// find existing for skipping if not overwrite
			for (final DownloadTask t : taskList) {
				if (t.targetFileExists()) {
					t.setState(DownloadTask.State.SKIPPED);
					stateMap.computeIfPresent(DownloadTask.State.SKIPPED, (k, v) -> v + 1);
				} else {
					downloadQueue.add(t);
				}
			}
		} else {
			downloadQueue.addAll(taskList);
		}
		stateMap.put(DownloadTask.State.DOWNLOADED, 0);
		stateMap.put(DownloadTask.State.SKIPPED, 0);
		stateMap.put(DownloadTask.State.INSTALLED, 0);
		nextDownload();
	}

	private void nextDownload() {
		isRunning.set(true);
		if (!downloadQueue.isEmpty()) {
			currDownloadTask = downloadQueue.pop();
			submitDownloadWorker(currDownloadTask);
		} else {
			// all download complete
			if (cbInstall.isSelected()) {
				startUnpack();
			} else {
				finished();
			}
		}
	}

	private void stopDownload() {
		if (workerTask != null) {
			workerTask.cancel(true);
			progressBar.progressProperty().unbind();
			currDownloadTask.setState(DownloadTask.State.CANCELLED);
			currDownloadTask.deleteTargetFile();
			init("Download cancelled");
			isRunning.set(false);
		}
	}

	private void submitDownloadWorker(final DownloadTask item) {
		workerTask = download(item);
		progressBar.progressProperty().bind(workerTask.progressProperty());
		workerTask.messageProperty().addListener((observable, oldValue, newValue) -> sizeDisplay.setText(newValue));
		Utilities.threadPool.submit(workerTask);
	}

    private Task<Boolean> download(final DownloadTask item) {
        return new Task<Boolean>() {
            @Override
            protected Boolean call() throws Exception {
				ReadableByteChannel rchannel = null;
				boolean isTotalSizeAvailable = false;
				long totalSize = -1;
				String totalSizeReported = "";
				Platform.runLater(() -> message.setText("Downloading " + item.getFileName()));
				try {
					// check for file size first
					final URL url = new URL(item.getFileURL());
					final HttpURLConnection httpConn = (HttpURLConnection)url.openConnection(); 
					httpConn.setRequestMethod("HEAD"); 
					totalSize = httpConn.getContentLengthLong();
					item.setState(DownloadTask.State.STARTED);
					item.setTotalSize(totalSize);
					if (totalSize >= 0) {
						isTotalSizeAvailable = true;
						totalSizeReported = String.format("%.2f MB", (totalSize / MEGA));
					}
					rchannel = Channels.newChannel(url.openStream()); 
				} catch (IOException e) {
					System.err.println(e);
					item.setState(DownloadTask.State.FAILED);
					Platform.runLater(() -> {
						progressBar.progressProperty().unbind();
						init("Network error");
					});
					return false;
				}
				if (!isTotalSizeAvailable)
					updateProgress(-1, -1);
				try (final FileOutputStream fos = new FileOutputStream(item.getTargetFile())) { 
					final FileChannel wchannel = fos.getChannel();
					final ByteBuffer buffer = ByteBuffer.allocateDirect(1024);
					long sizeSoFar = 0;
					String sizeReported;
					while (rchannel.read(buffer) != -1) {
						buffer.flip();
						while (buffer.hasRemaining()) {
							sizeSoFar += wchannel.write(buffer);
							sizeReported = String.format("%.2f", (sizeSoFar / MEGA));
							if (isTotalSizeAvailable) {
								updateProgress(sizeSoFar, totalSize);
								final long percent = 100 * sizeSoFar / totalSize;
								updateMessage(sizeReported + " MB / " + totalSizeReported + " (" + percent + "%)");
							} else {
								updateMessage(sizeReported + " MB");
							}
						}
						buffer.clear();
					}
				} catch (IOException e) {
					Platform.runLater(() -> {
						final String mess;
						if (this.isCancelled()) {
							mess = "Download cancelled";
							item.setState(DownloadTask.State.CANCELLED);
						} else {
							mess = "Download error";
							item.setState(DownloadTask.State.FAILED);
						}
						progressBar.progressProperty().unbind();
						init(mess);
					});
					return false;
				}
				Platform.runLater(() -> {
					// in case of success start the next item, if any
					item.setState(DownloadTask.State.DOWNLOADED);
					stateMap.computeIfPresent(DownloadTask.State.DOWNLOADED, (k, v) -> v + 1);
					message.setText(item.getFileName() + " completed");
					progressBar.progressProperty().unbind();
					nextDownload();
				});
				return true;
            }
        };
    }

	private void startUnpack() {
		final List<DownloadTask> toUnpack = taskList.stream()
													.filter(DownloadTask::isInstallable)
													.collect(Collectors.toList());
		unpackQueue.addAll(toUnpack);
		if (unpackQueue.isEmpty()) {
			finished();
		} else {
			startStopButton.setDisable(true);
			nextUnpack();
		}
	}

	private void nextUnpack() {
		// create temp dir first
		if (!tempDir.exists())
			tempDir.mkdir();
		if (!unpackQueue.isEmpty()) {
			submitUnpackWorker(unpackQueue.pop());
		} else {
			// all unpack complete
			finished();
		}
	}

	private void submitUnpackWorker(final DownloadTask item) {
		if (item.isUnpackNeeded()) {
			if (ProgressiveDownloader.isFormatValid(item.getFileName().toLowerCase())) {
				boolean doSubmit = true;
				if (item.getFileExt().equals("gz")) {
					workerTask = unpackGzip(item);
					progressBar.progressProperty().bind(workerTask.progressProperty());
				} else if (item.getFileExt().equals("bz2")) {
					workerTask = unpackBZip2(item);
					progressBar.progressProperty().bind(workerTask.progressProperty());
				} else if (item.getFileExt().equals("xz")) {
					workerTask = unpackXZ(item);
					progressBar.progressProperty().bind(workerTask.progressProperty());
				} else if (item.getFileExt().equals("zip")) {
					progressBar.setProgress(-1);
					workerTask = unpackZip(item);
				} else {
					doSubmit = false;
				}
				if (doSubmit) {
					workerTask.messageProperty().addListener((observable, oldValue, newValue) -> sizeDisplay.setText(newValue));
					Utilities.threadPool.submit(workerTask);
				} else {
					nextUnpack();
				}
			} else {
				nextUnpack();
			}
		} else {
			item.copyTargetToDest();
			item.setState(DownloadTask.State.INSTALLED);
			stateMap.computeIfPresent(DownloadTask.State.INSTALLED, (k, v) -> v + 1);
			nextUnpack();
		}
	}

    private Task<Boolean> unpackZip(final DownloadTask item) {
        return new Task<Boolean>() {
            @Override
            protected Boolean call() throws Exception {
				Platform.runLater(() -> message.setText("Unpacking/installing " + item.getFileName()));
				if (item.targetFileExists()) {
					Utilities.unzip(item.getTargetFile(), item.getDestination());
				}
				Platform.runLater(() -> {
					item.setState(DownloadTask.State.INSTALLED);
					stateMap.computeIfPresent(DownloadTask.State.INSTALLED, (k, v) -> v + 1);
					message.setText(item.getFileName() + " installed");
					nextUnpack();
				});
				return true;
            }
        };
    }

    private Task<Boolean> unpackGzip(final DownloadTask item) {
        return new Task<Boolean>() {
            @Override
            protected Boolean call() throws Exception {
				final String filename = item.getFileName();
				Platform.runLater(() -> message.setText("Unpacking/installing " + filename));
				final File tarOut = new File(tempDir, filename.substring(0, filename.lastIndexOf("."))); 
				try (final InputStream fin = new FileInputStream(item.getTargetFile());
						final BufferedInputStream in = new BufferedInputStream(fin);
						final GzipCompressorInputStream gzIn = new GzipCompressorInputStream(in);
						final OutputStream out = new FileOutputStream(tarOut)) {
					final byte[] buffer = new byte[4096];
					int n = 0;
					long sizeSoFar = 0;
					String sizeReported;
					updateProgress(-1, -1);
					while (-1 != (n = gzIn.read(buffer))) {
						out.write(buffer, 0, n);
						sizeSoFar += n;
						sizeReported = String.format("%.2f", (sizeSoFar / MEGA));
						updateMessage(sizeReported + " MB");
					}
				} catch (IOException e) {
					System.err.println(e);
				}
				if (tarOut.exists()) {
					Utilities.untar(tarOut, item.getDestination());
				}
				Platform.runLater(() -> {
					item.setState(DownloadTask.State.INSTALLED);
					stateMap.computeIfPresent(DownloadTask.State.INSTALLED, (k, v) -> v + 1);
					message.setText(item.getFileName() + " installed");
					progressBar.progressProperty().unbind();
					nextUnpack();
				});
				return true;
            }
        };
    }
	
    private Task<Boolean> unpackXZ(final DownloadTask item) {
        return new Task<Boolean>() {
            @Override
            protected Boolean call() throws Exception {
				final String filename = item.getFileName();
				Platform.runLater(() -> message.setText("Unpacking/installing " + filename));
				final File tarOut = new File(tempDir, filename.substring(0, filename.lastIndexOf("."))); 
				try (final InputStream fin = new FileInputStream(item.getTargetFile());
						final BufferedInputStream in = new BufferedInputStream(fin);
						final XZCompressorInputStream xzIn = new XZCompressorInputStream(in);
						final OutputStream out = new FileOutputStream(tarOut)) {
					final byte[] buffer = new byte[4096];
					int n = 0;
					long sizeSoFar = 0;
					String sizeReported;
					updateProgress(-1, -1);
					while (-1 != (n = xzIn.read(buffer))) {
						out.write(buffer, 0, n);
						sizeSoFar += n;
						sizeReported = String.format("%.2f", (sizeSoFar / MEGA));
						updateMessage(sizeReported + " MB");
					}
				} catch (IOException e) {
					System.err.println(e);
				}
				if (tarOut.exists()) {
					Utilities.untar(tarOut, item.getDestination());
				}
				Platform.runLater(() -> {
					item.setState(DownloadTask.State.INSTALLED);
					stateMap.computeIfPresent(DownloadTask.State.INSTALLED, (k, v) -> v + 1);
					message.setText(item.getFileName() + " installed");
					progressBar.progressProperty().unbind();
					nextUnpack();
				});
				return true;
            }
        };
    }
	
    private Task<Boolean> unpackBZip2(final DownloadTask item) {
        return new Task<Boolean>() {
            @Override
            protected Boolean call() throws Exception {
				final String filename = item.getFileName();
				Platform.runLater(() -> message.setText("Unpacking/installing " + filename));
				final File tarOut = new File(tempDir, filename.substring(0, filename.lastIndexOf("."))); 
				try (final InputStream fin = new FileInputStream(item.getTargetFile());
						final BufferedInputStream in = new BufferedInputStream(fin);
						final BZip2CompressorInputStream bzIn = new BZip2CompressorInputStream(in);
						final OutputStream out = new FileOutputStream(tarOut)) {
					final byte[] buffer = new byte[4096];
					int n = 0;
					long sizeSoFar = 0;
					String sizeReported;
					updateProgress(-1, -1);
					while (-1 != (n = bzIn.read(buffer))) {
						out.write(buffer, 0, n);
						sizeSoFar += n;
						sizeReported = String.format("%.2f", (sizeSoFar / MEGA));
						updateMessage(sizeReported + " MB");
					}
				} catch (IOException e) {
					System.err.println(e);
				}
				if (tarOut.exists()) {
					Utilities.untar(tarOut, item.getDestination());
				}
				Platform.runLater(() -> {
					item.setState(DownloadTask.State.INSTALLED);
					stateMap.computeIfPresent(DownloadTask.State.INSTALLED, (k, v) -> v + 1);
					message.setText(item.getFileName() + " installed");
					progressBar.progressProperty().unbind();
					nextUnpack();
				});
				return true;
            }
        };
    }

}

