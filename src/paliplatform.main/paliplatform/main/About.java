/*
 * About.java
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

package paliplatform.main;

import paliplatform.base.*;

import java.util.*;
import java.util.stream.*;
import java.util.zip.*;
import java.io.*;
import java.net.URL;
import javafx.scene.*;
import javafx.scene.image.*;
import javafx.scene.text.*;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.stage.Modality;
import javafx.util.Duration;
import javafx.animation.*;

/** 
 * The about dialog. This is a singleton.
 * @author J.R. Bhaddacak
 * @version 3.3
 * @since 2.0
 */
class About extends Stage {
	static final About INSTANCE = new About();
	private static final String SLIDEZIP = "slides.zip";
	private final TextArea miscInfo = new TextArea();
	private final Image defaultLogo = new Image(About.class.getResourceAsStream("resources/images/lotustext-240.png"));
	private final ImageView picDisplay = new ImageView(defaultLogo);
	private final File slideZip;
	private final List<String> magazine = new ArrayList<>();
	private boolean slideRunning = false;
	private Thread slideThread = null;
	
	private About() {
        setTitle("About");
		initModality(Modality.APPLICATION_MODAL);
		initOwner(PaliPlatform.stage);
		setResizable(false);
		// prepare slide magazine
		slideZip  = new File(Utilities.ROOTDIR + Utilities.PICPATH + SLIDEZIP);
		magazine.addAll(getSlideList());
		// set up components
		final VBox aboutBox = new VBox(5);
		final String aboutBoxStyle = "-fx-padding: 10;" +
									"-fx-border-width: 2;" +
									"-fx-border-insets: 5;" +
									"-fx-border-radius: 5;" +
									"-fx-border-color: #909090;";
		aboutBox.setStyle(aboutBoxStyle + "-fx-border-style: solid inside;-fx-alignment: top-center;");
		final HBox logoBox = new HBox(5);
		final VBox nameBox = new VBox();
		nameBox.setStyle(aboutBoxStyle + "-fx-border-style: dotted outside;-fx-alignment: center;");
		final Label progName = new Label("Pāli Platform");
		progName.setFont(Font.font(Utilities.FONTSERIF, FontWeight.BOLD, Utilities.getRelativeSize(1.7)));
		final Label progVersion = new Label(Utilities.VERSION);
		progVersion.setFont(Font.font(Utilities.FONTSERIF, FontWeight.MEDIUM, Utilities.getRelativeSize(1.3)));
		final Label progDesc = new Label("\n“Pāli studies made enjoyable”");
		nameBox.minWidthProperty().bind(progDesc.widthProperty());
		final VBox versionBox = new VBox();
		progDesc.setFont(Font.font(Utilities.FONTSANS, FontWeight.MEDIUM, Utilities.getRelativeSize(1.0)));
		final String sysInfoStr = 
			"\nOperating System: " + 
			"\n  " + System.getProperty("os.name") + " (" + System.getProperty("os.arch") + ") " + System.getProperty("os.version") +
			"\nJava Virtual Machine: " + 
			"\n  " + System.getProperty("java.vm.name") + 
			"\n  (" +System.getProperty("java.vm.vendor") + ")" +
			"\n  " + System.getProperty("java.vm.version") +
			"\nJavaFX: " + 
			"\n  " + System.getProperty("javafx.version");
		final Label sysInfo = new Label(sysInfoStr);
		sysInfo.setFont(Font.font(Utilities.FONTSANS, FontWeight.MEDIUM, Utilities.getRelativeSize(1)));
		versionBox.getChildren().addAll(sysInfo);
		nameBox.getChildren().addAll(progName, progVersion, progDesc, versionBox);
		picDisplay.setFitWidth(240);
		picDisplay.setPreserveRatio(true);
		picDisplay.setSmooth(true);
		picDisplay.setCache(true);
		picDisplay.setOnMouseClicked(mouseEvent -> startSlideShow());
		logoBox.getChildren().addAll(nameBox, new Label("  "), picDisplay);
		miscInfo.setFont(Font.font(Utilities.FONTMONO, FontWeight.MEDIUM, Utilities.getRelativeSize(0.85)));
		miscInfo.setStyle("-fx-border-width:2;-fx-border-radius:5;-fx-border-color:#909090;" +
							"-fx-focus-color:transparent;-fx-text-box-border:transparent;");
		miscInfo.setEditable(false);
		miscInfo.setWrapText(true);
		final Button aboutClose = new Button("Close");
		aboutClose.setOnAction(actionEvent -> hide());
		aboutBox.getChildren().addAll(logoBox, miscInfo, aboutClose);
		final Scene aboutContent = new Scene(aboutBox, Utilities.getRelativeSize(45), Utilities.getRelativeSize(37));
		setOnShowing(e -> refreshTheme());
		setScene(aboutContent);
		this.setOnHidden(windowEvent -> {
			if (slideThread != null) {
				slideThread.interrupt();
				slideThread = null;
			}
			slideRunning = false;
		});
	}
	
	public void setTextInfo(String text) {
		if (miscInfo.getText().isEmpty())
			miscInfo.setText(text);
		miscInfo.home();	
	}
	
    public void refreshTheme() {
		final Scene scene = getScene();
		scene.getStylesheets().clear();
		final String stylesheet = Utilities.getCustomStyleSheet();
		if (!stylesheet.isEmpty())
			scene.getStylesheets().add(stylesheet);
	}

	private List<String> getSlideList() {
		if (!slideZip.exists())
			return Collections.emptyList();
		final List<String> result = new ArrayList<>();
		try {
			final ZipFile zip = new ZipFile(slideZip);
			for (final Enumeration<? extends ZipEntry> e = zip.entries(); e.hasMoreElements();) {
				final ZipEntry entry = e.nextElement();
				final String fname = entry.getName();
				if (fname.endsWith(".png")) {
					result.add(fname);
				}
			}
			zip.close();
		} catch (IOException e) {
			System.err.println(e);
		}
		result.sort(Comparator.naturalOrder());
		return result;
	}

	private void startSlideShow() {
		if (magazine.isEmpty()) return;
		if (slideThread != null && slideThread.isAlive()) return;
		slideRunning = true;
		fadeOut(-1);
	}
	
	private void setDefaultImage() {
		picDisplay.setImage(defaultLogo);
		picDisplay.setOpacity(1);
	}

	private void fadeOut(final int num) {
		if (!slideRunning) {
			setDefaultImage();
			return;
		}
		slideThread = new Thread(() -> {
			try {
				Thread.sleep(2000);
				final FadeTransition fadePicOut = new FadeTransition(Duration.millis(1000), picDisplay);
				fadePicOut.setFromValue(1);
				fadePicOut.setToValue(0);
				fadePicOut.setOnFinished(actionEvent -> loadNextPic(num));
				fadePicOut.playFromStart();
			} catch (InterruptedException e) {
				// setDefaultImage();
				// Uncomment the above to reset to the default logo
				// when the window is closed during the sleep (showing) period,
				// otherwise the last image will hold.
			}
		});
		slideThread.start();
	}

	private void loadNextPic(final int num) {
		if (!slideRunning) {
			setDefaultImage();
			return;
		}
		final int nextNum = num + 1;
		slideThread = new Thread(() -> {
			try {
				final String imgName = magazine.get(nextNum % magazine.size());
				final ZipFile zip = new ZipFile(slideZip);
				final ZipEntry entry = zip.getEntry (imgName);
				picDisplay.setImage(new Image(zip.getInputStream(entry)));
				zip.close();
				final FadeTransition fadePicIn = new FadeTransition(Duration.millis(1000), picDisplay);
				fadePicIn.setFromValue(0);
				fadePicIn.setToValue(1);
				fadePicIn.setOnFinished(actionEvent -> fadeOut(nextNum));
				fadePicIn.playFromStart();
			} catch (IOException e) {
				System.err.println(e);
			}
		});
		slideThread.start();
	}

}
