/*
 * BatchScriptTransformer.java
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

package paliplatform.main;

import paliplatform.base.*;

import java.util.*;
import java.io.*;

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
 * @version 3.0
 * @since 2.0
 */
public class BatchScriptTransformer extends SingletonWindow {
	private static final String DEFAULT_SUFFIX = "_converted";
	public static final BatchScriptTransformer INSTANCE = new BatchScriptTransformer();
	private final BorderPane mainPane = new BorderPane();
	private final ObservableList<ScriptTransformer> workingList = FXCollections.<ScriptTransformer>observableArrayList();
	private final TableView<ScriptTransformer> table = new TableView<>();
	private final CheckBox cbAutodetect = new CheckBox("Auto-detect");
	private static String suffix = DEFAULT_SUFFIX;
	private final InfoPopup infoPopup = new InfoPopup();
	
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
		cbAutodetect.setAllowIndeterminate(false);
		cbAutodetect.setTooltip(new Tooltip("Auto-detect input script"));
		cbAutodetect.setSelected(true);
		final Button helpButton = new Button("", new TextIcon("circle-question", TextIcon.IconSet.AWESOME));
		helpButton.setOnAction(actionEvent -> infoPopup.showPopup(helpButton, InfoPopup.Pos.BELOW_CENTER, true));
		commonToolBar.getItems().addAll(cbAutodetect, helpButton);
		// 2. the task specific tool bar
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
		final Button setTargetButton = new Button("Output folder");
		setTargetButton.setOnAction(actionEvent -> setTargetFolder());
		final MenuButton sourceScriptMenu = new MenuButton("Input script");
		sourceScriptMenu.disableProperty().bind(cbAutodetect.selectedProperty());
		for (Utilities.PaliScript sc : Utilities.PaliScript.scripts) {
			if (sc.ordinal() == 0 || sc.ordinal() == 4) continue; // skip Unknown and Myanmar
			final String sname = sc.toString();
			final MenuItem mitem = new MenuItem(sname.charAt(0) + sname.substring(1).toLowerCase());
			mitem.setOnAction(actionEvent -> setSourceScript(sc));
			sourceScriptMenu.getItems().add(mitem);
		}
		final MenuButton targetScriptMenu = new MenuButton("Output script");
		for (Utilities.PaliScript sc : Utilities.PaliScript.scripts) {
			if (sc.ordinal() == 0 || sc.ordinal() == 4) continue; // skip Unknown and Myanmar
			final String sname = sc.toString();
			final MenuItem mitem = new MenuItem(sname.charAt(0) + sname.substring(1).toLowerCase());
			mitem.setOnAction(actionEvent -> setTargetScript(sc));
			targetScriptMenu.getItems().add(mitem);
		}
		final Button suffixButton = new Button("Suffix");
		suffixButton.setTooltip(new Tooltip("Change output-file suffix"));
		suffixButton.setOnAction(actionEvent -> setSuffix());
		final CheckBox includeNumButton = new CheckBox("Numbers");
		includeNumButton.setAllowIndeterminate(false);
		includeNumButton.setTooltip(new Tooltip("Including numbers on/off"));
		includeNumButton.setSelected(true);
		includeNumButton.setOnAction(actionEvent -> PaliCharTransformer.setIncludingNumbers(includeNumButton.isSelected()));
		final Button convertButton = new Button("Convert", new TextIcon("gears", TextIcon.IconSet.AWESOME));
		convertButton.setOnAction(actionEvent -> startConvert());
		toolBar.getItems().addAll(removeButton, clearButton, resetButton, 
								addButton, setTargetButton, sourceScriptMenu, targetScriptMenu,
								suffixButton, includeNumButton, convertButton);
		
		mainPane.setTop(commonToolBar);
		final VBox contentBox = new VBox();
		VBox.setVgrow(table, Priority.ALWAYS);
		contentBox.getChildren().addAll(toolBar, table);
		mainPane.setCenter(contentBox);
		final Scene scene = new Scene(mainPane, windowWidth, windowHeight);
		setScene(scene);
		
		// prepare info popup
		infoPopup.setContentWithText(PaliPlatform.getTextResource("info-batch-transformer.txt"));
		infoPopup.setTextWidth(Utilities.getRelativeSize(40));		
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
	
	private void setTargetFolder() {
		final File targetDir = Utilities.selectDirectory(this);
		if (targetDir != null) {
			workingList.forEach(st -> st.setTargetDirectory(targetDir));
		}
	}
	
	private void setSourceScript(final Utilities.PaliScript script) {
		workingList.forEach(st -> st.setSourceScript(script.toString()));
	}

	private void setTargetScript(final Utilities.PaliScript script) {
		workingList.forEach(st -> st.setTargetScript(script.toString()));
	}

	static String getSuffix() {
		return suffix;
	}
		
	private void setSuffix() {
		final TextInputDialog inputDialog = new TextInputDialog(suffix);
		inputDialog.setTitle("Specify a suffix");
		inputDialog.setHeaderText(null);
		inputDialog.setContentText("Please enter the suffix of output files\nto avoid meddling with source files.");
		final Optional<String> result = inputDialog.showAndWait();
		if (result.isPresent()) {
			suffix = result.get();
			workingList.forEach(st -> st.setTargetFileName());
		}
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
	public static final class ScriptTransformer {
		final private File sourceFile;
		private StringProperty sourceFileName;
		private StringProperty sourceScript;
		private StringProperty targetFileName;
		private StringProperty targetScript;
		private StringProperty done;
		final private String sourceName;
		final private String sourcePath;
		final private String sourceExt;
		final private Utilities.PaliScript detectedScript;
		private String targetPath = "";
		
		public ScriptTransformer(final File file) {
			sourceFile = file;
			if (file == null) {
				sourceName = "";
				sourcePath = "";
				sourceExt = "";
				detectedScript = Utilities.PaliScript.UNKNOWN;
			} else {
				final String srcFilePath = file.getPath();
				sourcePath = srcFilePath.substring(0, srcFilePath.lastIndexOf(File.separator));
				sourceName = srcFilePath.substring(srcFilePath.lastIndexOf(File.separator), srcFilePath.lastIndexOf("."));
				sourceExt = srcFilePath.substring(srcFilePath.lastIndexOf("."));
				sourceFileNameProperty().set(srcFilePath);
				detectedScript = Utilities.getScriptLanguage(file);
				sourceScriptProperty().set(detectedScript.toString());
				if (targetPath.isEmpty())
					targetPath = sourcePath;
				final String targetFilePath = detectedScript == Utilities.PaliScript.UNKNOWN ? ""
											: targetPath + sourceName + BatchScriptTransformer.getSuffix() + sourceExt;
				targetFileNameProperty().set(targetFilePath);
				targetScriptProperty().set(detectedScript == Utilities.PaliScript.UNKNOWN ? "" : "ROMAN");
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
			if (!Utilities.PaliScript.UNKNOWN.toString().equals(sourceScriptProperty().get())) {
				targetScript.set(strScript);
			}
		}
		
		public void setTargetDirectory(final File path) {
			targetPath = path.getPath();
			setTargetFileName();
		}
		
		public void setTargetFileName() {
			if (!Utilities.PaliScript.UNKNOWN.toString().equals(sourceScriptProperty().get())) {
				targetFileNameProperty().set(targetPath + sourceName + BatchScriptTransformer.getSuffix() + sourceExt);
			}
		}
		
		public void setDone(final boolean yn) {
			final String result = yn ? "✔" : "";
			done.set(result);
		}
		
		public void convert() {
			if (!doneProperty().get().isEmpty())
				return;
			if (!(targetFileNameProperty().get().isEmpty() || sourceScriptProperty().get().equals(targetScriptProperty().get()))) {
				final File target = new File(targetFileNameProperty().get());
				if (!target.exists()) {
					String srcText = Utilities.getTextFileContent(sourceFile);
					String tgtText = "";
					if (Utilities.PaliScript.ROMAN.toString().equals(sourceScriptProperty().get())) {
						srcText = Utilities.normalizeNiggahita(srcText, true);
						switch (Utilities.PaliScript.valueOf(targetScriptProperty().get())) {
							case DEVANAGARI:
								tgtText = PaliCharTransformer.romanToDevanagari(srcText);
								break;
							case KHMER:
								tgtText = PaliCharTransformer.romanToKhmer(srcText);
								break;
							case SINHALA:
								tgtText = PaliCharTransformer.romanToSinhala(srcText);
								break;
							case THAI:
								tgtText = PaliCharTransformer.romanToThai(srcText);
								break;
						}
					} else {
						if (Utilities.PaliScript.ROMAN.toString().equals(targetScriptProperty().get())) {
							switch (Utilities.PaliScript.valueOf(sourceScriptProperty().get())) {
								case DEVANAGARI:
									tgtText = PaliCharTransformer.devanagariToRoman(srcText);
									break;
								case KHMER:
									tgtText = PaliCharTransformer.khmerToRoman(srcText);
									break;
								case SINHALA:
									tgtText = PaliCharTransformer.sinhalaToRoman(srcText);
									break;
								case THAI:
									tgtText = PaliCharTransformer.thaiToRoman(srcText);
									break;
							}
						}
					}
					if (!tgtText.isEmpty()) {
						Utilities.saveText(tgtText, target);
						setDone(true);
					}
				}
			} // end if
		} // end convert
	} // end inner class
	
}
