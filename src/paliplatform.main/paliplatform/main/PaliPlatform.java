/*
 * PaliPlatform.java
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
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see https://www.gnu.org/licenses/.
 */

package paliplatform.main;

import paliplatform.base.*;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.*;
import javafx.scene.*;
import javafx.scene.image.*;
import javafx.scene.text.*;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.css.Styleable;

import java.net.URLDecoder;
import java.util.*;
import java.util.stream.*;
import java.util.concurrent.Executors;
import java.util.function.Function;
import java.util.ServiceLoader.Provider;
import java.sql.*;
import java.io.*;
import java.nio.file.*;
import java.nio.charset.StandardCharsets;

/** 
 * The entry point of the whole program.
 * @author J.R. Bhaddacak
 * @version 3.0
 * @since 2.0
 */
final public class PaliPlatform extends Application {
	public static final String PRODUCT_NAME = "Pāli Platform";
	static Scene scene;
	static Stage stage;
	static final TabPane tabPane = new TabPane();
	static String releaseNotes = "";
	static Map<String, Styleable> styleableServiceMap;
	static Map<String, SimpleService> simpleServiceMap;
	static DictService dictServiceImp;
	static ReaderService readerServiceImp;
	static LuceneService luceneServiceImp;
	public static final InfoPopup infoPopup = new InfoPopup();
	
    @Override
    public void init() throws Exception {
		// read the application's path from the class location
		final boolean isWindows = System.getProperty("os.name").startsWith("Windows");
		String classPath = URLDecoder.decode(PaliPlatform.class.getProtectionDomain().getCodeSource().getLocation().getPath(), StandardCharsets.UTF_8);
		if (isWindows && classPath.startsWith("/"))
			classPath = classPath.substring(1);
		final List<String> args = getParameters().getRaw();
		String appPath = "";
		if (args.isEmpty()){
			// if the path is not provided by the command line argument, use the value above
			if (classPath.toLowerCase().endsWith(".jar")) {
				appPath = classPath.substring(0, classPath.lastIndexOf("/") + 1);
			} else {
				appPath = classPath;
			}
			appPath = appPath.endsWith("/") ? appPath : appPath + "/";
			final String moddir = "modules/";
			appPath = appPath.endsWith(moddir)
						? appPath.substring(0, appPath.lastIndexOf(moddir))
						: appPath;
		} else {
			appPath = args.get(0);
		}
		final Path apath = Path.of(appPath);
		final File afile = apath.toFile();
		Utilities.ROOTPATH = apath;
		Utilities.ROOTDIR = afile.isDirectory() ? afile.getPath() + File.separator : "." + File.separator;
		final Path cachePath = Path.of(Utilities.ROOTDIR + Utilities.CACHEPATH);
		if (Files.notExists(cachePath))
			Files.createDirectories(cachePath);
		
		// prepare executor thread pool for concrrent tasks
		Utilities.threadPool = Executors.newFixedThreadPool(5 * Runtime.getRuntime().availableProcessors());

		// load settings
		Utilities.settings = MainProperties.INSTANCE.getSettings();
		Utilities.setupPaliInputCharMap();
		Utilities.urls = UrlProperties.INSTANCE.getUrlProps();

		// initialize font map
		Utilities.initializeFontMap();

		// intialize comparator
		Utilities.initializeComparator();
		
		// intialize StringConverter
		Utilities.initializeStringConverter();

		// prepare info popup
		infoPopup.setContentWithText(getTextResource("info-quick-starter.txt"));
		infoPopup.setTextWidth(Utilities.getRelativeSize(42));

		// prepare for macOS UI
		// this seems no need for moderm Mac OS X
		final boolean isMacOS = System.getProperty("mrj.version") != null;
		if (isMacOS) {
			System.setProperty("apple.laf.useScreenMenuBar", "true");
			System.setProperty("com.apple.mrj.application.apple.menu.about.name", PRODUCT_NAME);
		}

		// intialize services from other modules
		styleableServiceMap = getStyleableServices();
		simpleServiceMap = getSimpleServices();
		dictServiceImp = getDictService();
		readerServiceImp = getReaderService();
		luceneServiceImp = getLuceneService();
    }
    
    @Override
    public void start(Stage stage) throws Exception {
		this.stage = stage;
		Utilities.mainStage = stage;
		final double width = Double.parseDouble(Utilities.settings.getProperty("width"));
		final double height = Double.parseDouble(Utilities.settings.getProperty("height"));
        final BorderPane root = new BorderPane();
        final VBox topPart = new VBox();
        topPart.getChildren().addAll(MainMenu.INSTANCE, MainToolBar.INSTANCE);
        root.setTop(topPart);
        
        // load preliminary data
        releaseNotes = "Root directory: " + Utilities.ROOTDIR + "\n\n" + loadNotesInfo();
        
        // add persistent tabs
        final EnumMap<Utilities.WindowType, Tab> persisTabs = new EnumMap<>(Utilities.WindowType.class);
		
        // Editor tab, always present
		final Tab editorTab = new Tab("Editor");
		editorTab.setClosable(false);
		final TextIcon editorIcon = new TextIcon("pencil", TextIcon.IconSet.AWESOME);
		editorTab.setGraphic(editorIcon);
		final Object[] editorArgs = {"ROMAN"};
		editorTab.setContent(new PaliTextEditor(editorArgs));
		persisTabs.put(Utilities.WindowType.EDITOR, editorTab);

		// TOC Tree tab, present when available
		if (readerServiceImp != null) {
			final Tab toctreeTab = readerServiceImp.getTocTreeTab();
			persisTabs.put(Utilities.WindowType.TOCTREE, toctreeTab);
		}
		// Document Finder tab, present when available
		if (readerServiceImp != null) {
			final Tab docFinderTab = readerServiceImp.getDocumentFinderTab();
			persisTabs.put(Utilities.WindowType.FINDER, docFinderTab);
		}
		// Lucene Finder tab, present when available
		if (luceneServiceImp != null) {
			final Tab luceneFinderTab = luceneServiceImp.getLuceneTab();
			persisTabs.put(Utilities.WindowType.LUCENE, luceneFinderTab);
		}
		// Term Lister tab, present when available
		if (luceneServiceImp != null) {
			final Tab luceneListerTab = luceneServiceImp.getListerTab();
			persisTabs.put(Utilities.WindowType.LISTER, luceneListerTab);
		}
		// Dict tab, present when available
		if (dictServiceImp != null) {
			final Tab dictTab = dictServiceImp.getDictTab();
			persisTabs.put(Utilities.WindowType.DICT, dictTab);
		}
		// add all tabs, ordered by Utilities.WindowType
		//tabPane.setTabDragPolicy(TabPane.TabDragPolicy.REORDER);
		tabPane.setTabDragPolicy(TabPane.TabDragPolicy.FIXED);
		for (final Utilities.WindowType wt : Utilities.WindowType.types) {
			final Tab tab = persisTabs.get(wt);
			if (tab != null) {
				tabPane.getTabs().add(tab);
			}
		}

        root.setCenter(tabPane);
        
        // scene start up
        scene = new Scene(root, width, height);
		stage.getIcons().add(new Image(PaliPlatform.class.getResourceAsStream("resources/images/lotusicon.png")));
        stage.setTitle(PRODUCT_NAME);
        stage.setScene(scene);
        setUserAgentStylesheet(STYLESHEET_MODENA);
        refreshTheme();
        
        // intercept the close request by pressing the window close button
		stage.setOnCloseRequest(new EventHandler<WindowEvent>() {  
			@Override
			public void handle(WindowEvent event) {
				exit(event);
			}
		});
        
        stage.show();
    }
    
    @Override
    public void stop() {
		final Scene s = stage.getScene();
        MainProperties.INSTANCE.saveSettings(s.getWidth(), s.getHeight());
		try {
			for (final Utilities.H2DB db : Utilities.H2DB.values()) {
				final java.sql.Connection conn = db.getConnection();
				if (conn != null)
					conn.close();
			}
			for (final Utilities.SQLiteDB db : Utilities.SQLiteDB.values()) {
				final java.sql.Connection conn = db.getConnection();
				if (conn != null)
					conn.close();
			}
		} catch (SQLException e) {
			System.err.println(e);
		}
		Utilities.threadPool.shutdown();
    }

	static String getTextResource(final String filename) {
		String result = "";
		try {
			final InputStream in = PaliPlatform.class.getResourceAsStream("resources/text/" + filename);
			if (in != null)
				result = new String(in.readAllBytes(), StandardCharsets.UTF_8);
		} catch (IOException e) {
			System.err.println(e);
		}
		return result;
	}
    
	/**
	 * Reads all services in Styleble type into Map,
	 * mostly Menu or ToolBar's component.
	 * So some modules may not be present at runtime.
	 */
	private static Map<String, Styleable> getStyleableServices() {
		return ServiceLoader.load(Styleable.class)
				.stream()
				.map(Provider::get)
				.collect(Collectors.toMap(x -> x.getClass().getName(), Function.identity()));
	}

	/**
	 * Reads all services in SimpleService type into Map,
	 */
	private static Map<String, SimpleService> getSimpleServices() {
		return ServiceLoader.load(SimpleService.class)
				.stream()
				.map(Provider::get)
				.collect(Collectors.toMap(x -> x.getClass().getName(), Function.identity()));
	}

	private static DictService getDictService() {
		return ServiceLoader.load(DictService.class)
				.stream()
				.filter((Provider p) -> p.type().getName().equals("paliplatform.dict.DictServiceImp"))
				.map(Provider::get)
				.findFirst()
				.orElse(null);
	}

	private static ReaderService getReaderService() {
		return ServiceLoader.load(ReaderService.class)
				.stream()
				.filter((Provider p) -> p.type().getName().equals("paliplatform.reader.ReaderServiceImp"))
				.map(Provider::get)
				.findFirst()
				.orElse(null);
	}

	private static LuceneService getLuceneService() {
		return ServiceLoader.load(LuceneService.class)
				.stream()
				.filter((Provider p) -> p.type().getName().equals("paliplatform.lucene.LuceneServiceImp"))
				.map(Provider::get)
				.findFirst()
				.orElse(null);
	}

    static void refreshTheme() {
		scene.getStylesheets().clear();
		final String stylesheet = Utilities.getCustomStyleSheet();
		if (!stylesheet.isEmpty())
			scene.getStylesheets().add(stylesheet);		
	}

	public static void openWindow(final Utilities.WindowType win, final Object[] args) {
		final Stage stg = Utilities.getOpenedWindow(win.getWindowClassName());
		switch (win) {
			case EDITOR:
				if (stg == null) {
					final PaliTextEditor editor = new PaliTextEditor(args);
					final Stage window = Utilities.openNewWindow(editor,
										new Image(PaliPlatform.class.getResourceAsStream("resources/images/pencil.png")), "");
					Platform.runLater(() -> editor.setStage(window));
				} else {
					final PaliTextEditor editor = (PaliTextEditor)stg.getScene().getRoot();
					if (args == null) {
						// open a file
						if (editor.openFile()) {
							editor.resetFont();
							Utilities.showExistingWindow(stg);
						}
					} else {
						if (args[0] instanceof File) {
							// open with specified file
							if (editor.openFile((File)args[0])) {
								editor.resetFont();
								Utilities.showExistingWindow(stg);
							}
						} else if (args[0] instanceof String) {
							// open with specified content
							final String strScript = (String)args[0];
							if (strScript.isEmpty()) {
								// open a file
								if (editor.openFile()) {
									editor.resetFont();
									Utilities.showExistingWindow(stg);
								}
							} else {
								final Utilities.PaliScript script = Utilities.PaliScript.valueOf(strScript);
								editor.clearEditor(script);
								final String content = args.length<2 ? "" : (String)args[1];
								editor.setContent(content);
								Utilities.showExistingWindow(stg);
							}
						}
					}
				}
				break;
		}
	}

	public static void showDict(final String term) {
		if (dictServiceImp != null) {
			dictServiceImp.searchTerm(term);
		}
	}
	
	/** 
	 * Loads the program's release notes from a provided text file. 
	 * The load is done only once when the program starts, 
	 * and the information is retained.
	 * The information is shown in About dialog.
	 */
	private String loadNotesInfo() throws Exception {
		final StringBuilder text = new StringBuilder();
		try (final Scanner in = new Scanner(PaliPlatform.class.getResourceAsStream("resources/text/relnotes.txt"), StandardCharsets.UTF_8)) {
			while (in.hasNextLine())
				text.append(in.nextLine()).append("\n");
		}
		return text.toString();	
	}

	static void createDesktopLauncher() {
		final String osName = System.getProperty("os.name").toLowerCase();
		if (osName.contains("linux")) {
			final String LINESEP = System.getProperty("line.separator");
			final String HOME = System.getProperty("user.home");
			final File desktop = new File(HOME + "/Desktop/");
			if (!desktop.exists()) return;
			final File launcher = new File(desktop, "PaliPlatform3.desktop");
			final StringBuilder content = new StringBuilder();
			content.append("[Desktop Entry]").append(LINESEP);
			content.append("Encoding=UTF-8").append(LINESEP);
			content.append("Version=1.0").append(LINESEP);
			content.append("Name=Pāli Platform 3").append(LINESEP);
			content.append("GenericName=Pāli Platform").append(LINESEP);
			content.append("Exec=\"" + Utilities.ROOTDIR + "run.sh\"").append(LINESEP);
			content.append("Terminal=false").append(LINESEP);
			content.append("Icon=" + Utilities.ROOTDIR + "data/pic/lotustext.ico").append(LINESEP);
			content.append("Type=Application").append(LINESEP);
			content.append("Categories=Application;Education;").append(LINESEP);
			content.append("Comment=Pali studies made enjoyable").append(LINESEP);
			Utilities.saveText(content.toString(), launcher);
			launcher.setExecutable(true);
		} else {
			final String scriptName = osName.contains("windows")
										? "winshort.cmd"
										: osName.contains("mac")
											? "./macshort.sh"
											: "";
			if (!scriptName.isEmpty()) {
				try {
					final Process proc = Runtime.getRuntime().exec(scriptName);
					proc.waitFor();
					System.out.println(proc);
				} catch (InterruptedException | IOException e) {
					System.err.println(e);
				}
			}
		}
	}

    static void about() {
		About.INSTANCE.setTextInfo(releaseNotes);
		About.INSTANCE.showAndWait();
	}
	
    static void exit(final WindowEvent event) {
		final ConfirmAlert quitAlert = new ConfirmAlert(stage, ConfirmAlert.ConfirmType.QUIT);
		if (Boolean.parseBoolean(Utilities.settings.getProperty("exit-ask"))) {
			final Optional<ButtonType> result = quitAlert.showAndWait();
			if (result.isPresent()) {
				if (result.get() == quitAlert.getConfirmButtonType()) {
					Platform.exit();
				} else {
					if (event != null)
						event.consume();
				}
			}	
		} else {
			Platform.exit();
		}
	}

}
