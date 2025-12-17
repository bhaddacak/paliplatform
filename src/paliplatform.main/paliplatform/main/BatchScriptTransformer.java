/*
 * BatchScriptTransformer.java
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
import paliplatform.base.Utilities.PaliScript;
import paliplatform.base.ScriptTransliterator.EngineType;

import java.util.*;
import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import javafx.collections.*;
import javafx.scene.*;
import javafx.scene.image.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.*;
import javafx.scene.layout.*;
import javafx.beans.property.*;

/** 
 * This utility converts files containing a Pali script to another script.
 * This is a singleton.
 * @author J.R. Bhaddacak
 * @version 3.6
 * @since 2.0
 */
public class BatchScriptTransformer extends SingletonWindow {
	private static final String[] charsetStr = { "UTF-8 ", "UTF-16" };
	public static final BatchScriptTransformer INSTANCE = new BatchScriptTransformer();
	private final BorderPane mainPane = new BorderPane();
	private final ObservableList<ScriptTransformer> workingList = FXCollections.<ScriptTransformer>observableArrayList();
	private final TableView<ScriptTransformer> table = new TableView<>();
	private final ToggleButton charsetButton = new ToggleButton();
	private final CheckMenuItem useSourceDirMenuItem = new CheckMenuItem("Save in source directory");
	private final CheckMenuItem autodetectMenuItem = new CheckMenuItem("Auto-detect input script");
	private final CheckMenuItem includeNumMenuItem = new CheckMenuItem("Convert Roman numbers");
	private final CheckMenuItem romanAsSanskritMenuItem = new CheckMenuItem("Roman as Sanskrit");
	private final ToggleGroup romanDefaultGroup = new ToggleGroup();
	private final InfoPopup infoPopup = new InfoPopup();
	private File outputDirectory = null;
	
	private BatchScriptTransformer() {
		super();
		windowWidth = Utilities.getRelativeSize(67);
		setTitle("Batch Script Transformer");
		getIcons().add(new Image(BatchScriptTransformer.class.getResourceAsStream("resources/images/gears.png")));
		workingList.add(new ScriptTransformer(null));
		table.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		table.setItems(workingList);
		setupTable();
		// add context menu
		final MenuItem removeMenuItem = new MenuItem("Remove");
		removeMenuItem.setOnAction(actionEvent -> removeItems());		
		final ContextMenu popupMenu = new ContextMenu();
		popupMenu.getItems().add(removeMenuItem);
		table.setContextMenu(popupMenu);
		
		// add options on the top
		// 1. the common tool bar
		final CommonWorkingToolBar commonToolBar = new CommonWorkingToolBar(table);
		// use property to bind with disablility of some buttons
		SimpleListProperty<ScriptTransformer> workingListProperty = new SimpleListProperty<>(workingList);
		// configure some buttons first
		commonToolBar.saveTextButton.setTooltip(new Tooltip("Save data as CSV"));
		commonToolBar.saveTextButton.setOnAction(actionEvent -> saveCSV());		
		commonToolBar.saveTextButton.disableProperty().bind(workingListProperty.sizeProperty().isEqualTo(0));
		commonToolBar.copyButton.setTooltip(new Tooltip("Copy CSV to clipboard"));
		commonToolBar.copyButton.setOnAction(actionEvent -> copyCSV());		
		commonToolBar.copyButton.disableProperty().bind(workingListProperty.sizeProperty().isEqualTo(0));
		charsetButton.setTooltip(new Tooltip("File encoding"));
		charsetButton.setText(charsetStr[0]);
		charsetButton.setStyle("-fx-font-family:'" + Utilities.FONTMONO +"';");
		charsetButton.setOnAction(actionEvent -> charsetButton.setText((charsetButton.isSelected() ? charsetStr[1] : charsetStr[0])));
		final Button helpButton = new Button("", new TextIcon("circle-question", TextIcon.IconSet.AWESOME));
		helpButton.setOnAction(actionEvent -> infoPopup.showPopup(helpButton, InfoPopup.Pos.BELOW_CENTER, true));
		commonToolBar.getItems().addAll(charsetButton, helpButton);
		// 2. the task-specific tool bar
		final ToolBar toolBar = new ToolBar();
		final Button removeButton = new Button("", new TextIcon("trash", TextIcon.IconSet.AWESOME));
		removeButton.setTooltip(new Tooltip("Remove from the list"));
		removeButton.setOnAction(actionEvent -> removeItems());		
		removeButton.disableProperty().bind(table.getSelectionModel().selectedItemProperty().isNull());
		final Button clearButton = new Button("Clear");
		clearButton.setTooltip(new Tooltip("Clear all"));
		clearButton.setOnAction(actionEvent -> clearAll());		
		final Button resetButton = new Button("Reset");
		resetButton.setTooltip(new Tooltip("Reset done status"));
		resetButton.setOnAction(actionEvent -> reset());		
		final Button addButton = new Button("Add files");
		addButton.setOnAction(actionEvent -> addFiles());
		final MenuButton sourceScriptMenu = new MenuButton("Input script");
		sourceScriptMenu.disableProperty().bind(autodetectMenuItem.selectedProperty());
		for (PaliScript sc : PaliScript.scripts) {
			if (sc.ordinal() == 0) continue; // skip Unknown
			final MenuItem mitem = new MenuItem(sc.getName());
			mitem.setOnAction(actionEvent -> setSourceScript(sc));
			sourceScriptMenu.getItems().add(mitem);
		}
		final MenuButton targetScriptMenu = new MenuButton("Output script");
		for (PaliScript sc : PaliScript.scripts) {
			if (sc.ordinal() == 0) continue; // skip Unknown
			final MenuItem mitem = new MenuItem(sc.getName());
			mitem.setOnAction(actionEvent -> setTargetScript(sc));
			targetScriptMenu.getItems().add(mitem);
		}
		// Options
		final MenuButton optionsMenu = new MenuButton("", new TextIcon("check-double", TextIcon.IconSet.AWESOME));		
		optionsMenu.setTooltip(new Tooltip("Options"));
		useSourceDirMenuItem.setSelected(true);
		useSourceDirMenuItem.setOnAction(actionEvent -> setTargetFolderAsSource());
		final MenuItem setOutputDirMenuItem = new MenuItem("Set output directory");
		setOutputDirMenuItem.disableProperty().bind(useSourceDirMenuItem.selectedProperty());
		setOutputDirMenuItem.setOnAction(actionEvent -> setTargetFolder());
		autodetectMenuItem.setSelected(true);
		final Menu romanDefMenu = new Menu("Roman transliteration");
		final List<EngineType> enList = new ArrayList<>(EngineType.forRoman);
		enList.add(EngineType.DEVA_ROMAN_UNIQUE);
		enList.add(EngineType.DEVA_ROMAN_SLP1);
		for (final EngineType en : enList) {
			if (en.getTargetScript() == PaliScript.ROMAN) {
				final RadioMenuItem enItem = new RadioMenuItem(en.getNameShort());
				enItem.setUserData(en);
				enItem.setToggleGroup(romanDefaultGroup);
				romanDefMenu.getItems().add(enItem);
			}
		}
		romanDefaultGroup.selectToggle(romanDefaultGroup.getToggles().get(2));
		includeNumMenuItem.setSelected(true);
		optionsMenu.getItems().addAll(useSourceDirMenuItem, setOutputDirMenuItem,
						new SeparatorMenuItem(), autodetectMenuItem, romanDefMenu, includeNumMenuItem, romanAsSanskritMenuItem);
		final Button convertButton = new Button("Convert", new TextIcon("gears", TextIcon.IconSet.AWESOME));
		convertButton.setOnAction(actionEvent -> startConvert());
		toolBar.getItems().addAll(removeButton, clearButton, resetButton, 
								addButton, sourceScriptMenu, targetScriptMenu,
								optionsMenu, convertButton);
		
		mainPane.setTop(commonToolBar);
		final VBox contentBox = new VBox();
		VBox.setVgrow(table, Priority.ALWAYS);
		contentBox.getChildren().addAll(toolBar, table);
		mainPane.setCenter(contentBox);
		final Scene scene = new Scene(mainPane, windowWidth, windowHeight);
		setScene(scene);
		
		// prepare info popup
		infoPopup.setContentWithText(PaliPlatform.getTextResource("info-batch-transformer.txt"));
		infoPopup.setTextWidth(Utilities.getRelativeSize(42));		
	}

	@Override
	public void init() {
		clearAll();
	}
	
	private void setupTable() {
		if (workingList.isEmpty())
			return;
		final TableColumn<ScriptTransformer, String> sourceFileCol = new TableColumn<>("Source file");
		sourceFileCol.setCellValueFactory(new PropertyValueFactory<>(workingList.get(0).sourceFileNameProperty().getName()));
		sourceFileCol.setStyle("-fx-text-overrun:leading-ellipsis");
		sourceFileCol.prefWidthProperty().bind(mainPane.widthProperty().divide(11).multiply(4).subtract(7));
		final TableColumn<ScriptTransformer, String> sourceScriptCol = new TableColumn<>("From");
		sourceScriptCol.setCellValueFactory(new PropertyValueFactory<>(workingList.get(0).sourceScriptProperty().getName()));
		sourceScriptCol.prefWidthProperty().bind(mainPane.widthProperty().divide(11).multiply(1));
		final TableColumn<ScriptTransformer, String> targetFileCol = new TableColumn<>("Output file");
		targetFileCol.setCellValueFactory(new PropertyValueFactory<>(workingList.get(0).targetFileNameProperty().getName()));
		targetFileCol.setStyle("-fx-text-overrun:center-ellipsis");
		targetFileCol.prefWidthProperty().bind(mainPane.widthProperty().divide(11).multiply(4));
		final TableColumn<ScriptTransformer, String> targetScriptCol = new TableColumn<>("To");
		targetScriptCol.setCellValueFactory(new PropertyValueFactory<>(workingList.get(0).targetScriptProperty().getName()));
		targetScriptCol.prefWidthProperty().bind(mainPane.widthProperty().divide(11).multiply(1));
		final TableColumn<ScriptTransformer, String> doneCol = new TableColumn<>("Done");
		doneCol.setCellValueFactory(new PropertyValueFactory<>(workingList.get(0).doneProperty().getName()));
		doneCol.getStyleClass().add("checkok");
		doneCol.prefWidthProperty().bind(mainPane.widthProperty().divide(11).multiply(1));
		table.getColumns().add(sourceFileCol);
		table.getColumns().add(sourceScriptCol);
		table.getColumns().add(targetFileCol);
		table.getColumns().add(targetScriptCol);
		table.getColumns().add(doneCol);
	}
	
	private void addFiles() {
		final List<File> files = Utilities.selectMultipleTextFile(this);
		if (!(files==null || files.isEmpty())) {
			if (!workingList.isEmpty() && workingList.get(0).getSourceFile() == null)
				clearAll();
			files.forEach(f -> {
				if (workingList.stream().noneMatch(s -> s.getSourceFile().equals(f)))
					workingList.add(new ScriptTransformer(f));
			});
		}
	}
	
	private void setTargetFolderAsSource() {
		workingList.forEach(st -> st.setTargetDirectory(useSourceDirMenuItem.isSelected()));
	}

	private void setTargetFolder() {
		outputDirectory = Utilities.selectDirectory(this);
		if (outputDirectory != null) {
			workingList.forEach(st -> st.setTargetDirectory(false));
		}
	}
	
	private void setSourceScript(final PaliScript script) {
		workingList.forEach(st -> st.setSourceScript(script.toString()));
	}

	private void setTargetScript(final PaliScript script) {
		workingList.forEach(st -> st.setTargetScript(script.toString()));
	}

	private void removeItems() {
		Utilities.removeObservableItems(workingList, table.getSelectionModel().getSelectedIndices());
	}
	
	private void clearAll() {
		workingList.clear();
	}
	
	private void reset() {
		workingList.forEach(st -> st.setDone(false));
	}

	private void startConvert() {
		workingList.forEach(st -> st.convert());
	}

	private List<String[]> makeCSV() {
		final List<String[]> result = new ArrayList<>();
		// table columns
		final int colCount = table.getColumns().size();
		final String[] heads = new String[colCount];
		for (int i = 0; i < colCount; i++) {
			heads[i] = table.getColumns().get(i).getText();
		}
		result.add(heads);
		// table data
		for (int i = 0; i < table.getItems().size(); i++) {
			final ScriptTransformer st = table.getItems().get(i);
			final String[] data = new String[colCount];
			data[0] = st.getSourceFile().getPath();
			data[1] = st.sourceScriptProperty().get();
			data[2] = st.targetFileNameProperty().get();
			data[3] = st.targetScriptProperty().get();
			data[4] = st.doneProperty().get();
			result.add(data);
		}
		return result;
	}
	
	private void copyCSV() {
		Utilities.copyCSV(makeCSV());
	}
	
	private void saveCSV() {
		Utilities.saveCSV(makeCSV(), "batch-list.csv");
	}
	
	// inner class
	public final class ScriptTransformer {
		final private File sourceFile;
		private StringProperty sourceFileName;
		private StringProperty sourceScript;
		private StringProperty targetFileName;
		private StringProperty targetScript;
		private StringProperty done;
		final private String sourceName;
		final private String sourcePath;
		final private String sourceExt;
		final private Charset charset;
		final private PaliScript detectedScript;
		private String targetPath = "";
		
		public ScriptTransformer(final File file) {
			sourceFile = file;
			charset = charsetButton.isSelected() ? StandardCharsets.UTF_16LE : StandardCharsets.UTF_8;
			if (file == null) {
				sourceName = "";
				sourcePath = "";
				sourceExt = "";
				detectedScript = PaliScript.UNKNOWN;
			} else {
				final String srcFilePath = file.getPath();
				final int sepPos = srcFilePath.lastIndexOf(File.separator);
				sourcePath = sepPos >= 0
								? srcFilePath.substring(0, sepPos + 1)
								: File.separator;
				sourceName = sepPos >= 0
								? srcFilePath.substring(sepPos + 1, srcFilePath.lastIndexOf("."))
								: srcFilePath;
				sourceExt = srcFilePath.substring(srcFilePath.lastIndexOf("."));
				sourceFileNameProperty().set(srcFilePath);
				detectedScript = Utilities.getScriptLanguage(file, charset);
				sourceScriptProperty().set(detectedScript.toString());
				targetScriptProperty().set(detectedScript == PaliScript.UNKNOWN ? "" : "ROMAN");
				targetPath = useSourceDirMenuItem.isSelected()
								? sourcePath
								: outputDirectory == null ? "" : outputDirectory.getPath();
				final String tPath = targetPath.endsWith(File.separator) ? targetPath : targetPath + File.separator;
				final String targetFilePath = detectedScript == PaliScript.UNKNOWN
												? ""
												: tPath + targetScriptProperty().get().toLowerCase() + "_" + sourceName + sourceExt;
				targetFileNameProperty().set(targetFilePath);
			}
		}
		
		public File getSourceFile() {
			return sourceFile;
		}
		
		public StringProperty sourceFileNameProperty() {
			if (sourceFileName == null)
				sourceFileName = new SimpleStringProperty(this, "sourceFileName");
			return sourceFileName;
		}
		
		public StringProperty targetFileNameProperty() {
			if (targetFileName == null)
				targetFileName = new SimpleStringProperty(this, "targetFileName");
			return targetFileName;
		}
		
		public StringProperty sourceScriptProperty() {
			if (sourceScript == null)
				sourceScript = new SimpleStringProperty(this, "sourceScript");
			return sourceScript;
		}
		
		public StringProperty targetScriptProperty() {
			if (targetScript == null)
				targetScript = new SimpleStringProperty(this, "targetScript");
			return targetScript;
		}
		
		
		public StringProperty doneProperty() {
			if (done == null)
				done = new SimpleStringProperty(this, "done", "");
			return done;
		}
		
		public void setSourceScript(final String strScript) {
			sourceScript.set(strScript);
		}
		
		public void setTargetScript(final String strScript) {
			if (!PaliScript.UNKNOWN.toString().equals(sourceScriptProperty().get())) {
				targetScript.set(strScript);
				setTargetFileName();
			}
		}
		
		public void setTargetDirectory(final boolean asSource) {
			targetPath = asSource 
							? sourcePath
							: outputDirectory == null
								? sourcePath
								: outputDirectory.getPath();
			setTargetFileName();
		}
		
		public void setTargetFileName() {
			if (!PaliScript.UNKNOWN.toString().equals(sourceScriptProperty().get())) {
				final String prefix = targetScriptProperty().get().toLowerCase();
				final String tPath = targetPath.endsWith(File.separator) ? targetPath : targetPath + File.separator;
				final String filename = tPath + prefix + "_" + sourceName + sourceExt;
				targetFileNameProperty().set(filename);
			}
		}
		
		public void setDone(final boolean yn) {
			final String result = yn ? "✔" : "";
			done.set(result);
		}
		
		public void convert() {
			if (!doneProperty().get().isEmpty())
				return;
			if (targetFileNameProperty().get().isEmpty())
				return;
			if (sourceScriptProperty().get().equals(targetScriptProperty().get()))
				return;
			final File target = new File(targetFileNameProperty().get());
			final EngineType romanDef = (EngineType)romanDefaultGroup.getSelectedToggle().getUserData();
			final boolean alsoNumber = includeNumMenuItem.isSelected();
			final String srcText = Utilities.getTextFileContent(sourceFile, charset);
			final PaliScript fromScript = PaliScript.valueOf(sourceScriptProperty().get());
			final PaliScript toScript = PaliScript.valueOf(targetScriptProperty().get());
			String tgtText = ScriptTransliterator.translitPaliScript(srcText,
								fromScript, toScript, romanDef, alsoNumber, romanAsSanskritMenuItem.isSelected());
			if (sourceExt.equalsIgnoreCase(".xml"))
				tgtText = ScriptTransliterator.fixXslName(tgtText, fromScript, toScript);
			Utilities.saveText(tgtText, target, charset);
			setDone(true);
		} // end convert
	} // end inner class
	
}
