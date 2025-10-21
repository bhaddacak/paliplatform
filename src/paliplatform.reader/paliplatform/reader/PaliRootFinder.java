/*
 * PaliRootFinder.java
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

package paliplatform.reader;

import paliplatform.base.*;

import java.util.*;
import java.util.stream.Collectors;
import java.text.Normalizer;
import java.text.Normalizer.Form;

import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.*;
import javafx.scene.image.*;
import javafx.scene.input.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/** 
 * The window showing all Pali roots with search function.
 * This is a singleton.
 * @author J.R. Bhaddacak
 * @version 3.2
 * @since 3.0
 */
public class PaliRootFinder extends SingletonWindow {
	public static final PaliRootFinder INSTANCE = new PaliRootFinder();
	private final BorderPane mainPane = new BorderPane();
	private final TableView<RootOutput> table = new TableView<>();	
	private final Label message = new Label();
	private final InfoPopup infoPopup = new InfoPopup();
	private final TextField searchTextField;
	private final ObservableList<RootOutput> outputList = FXCollections.<RootOutput>observableArrayList();
	private final List<RootDef> workingList = new ArrayList<>();
	private final VBox toolBarBox = new VBox();
	private final HBox optionToolBar = new HBox();
	private final CheckBox cbVariant = new CheckBox("Variants");
	private final CheckBox[] cbRootGroup9 = new CheckBox[9];
	private final CheckBox[] cbRootGroup8 = new CheckBox[8];
	private final Set<RootDef.RootBook> bookSelectorSet = EnumSet.allOf(RootDef.RootBook.class);
	private final ContextMenu popupMenu = new ContextMenu();											
	
	private PaliRootFinder() {
		windowWidth = Utilities.getRelativeSize(66);
		setTitle("Pāli Root Finder");
		getIcons().add(new Image(ReaderUtilities.class.getResourceAsStream("resources/images/magnifying-glass.png")));
		
		// add toolbar on the top
		// main toolbar
		final CommonWorkingToolBar toolBar = new CommonWorkingToolBar(table);
		// config some buttons
		toolBar.saveTextButton.setTooltip(new Tooltip("Save data as CSV"));
		toolBar.saveTextButton.setOnAction(actionEvent -> saveCSV());		
		toolBar.copyButton.setTooltip(new Tooltip("Copy CSV to clipboard"));
		toolBar.copyButton.setOnAction(actionEvent -> copyCSV());		
		// add new components
		final PaliTextInput searchTextInput = new PaliTextInput(PaliTextInput.InputType.FIELD);
		searchTextField = (TextField)searchTextInput.getInput();
		searchTextField.setPromptText("Search for...");
		searchTextField.textProperty().addListener((obs, oldValue, newValue) -> {
			final String text = Normalizer.normalize(newValue.trim(), Form.NFC);
			setOutputList(text);
		});
		// variant checkbox
		cbVariant.setSelected(true);
		cbVariant.setTooltip(new Tooltip("Show variants"));
		cbVariant.setOnAction(actionEvent -> setOutputList());
		// option button
		final Button optionButton = new Button("", new TextIcon("list-check", TextIcon.IconSet.AWESOME));
		optionButton.setTooltip(new Tooltip("Inclusion options"));
		optionButton.setOnAction(actionEvent -> toggleOption());
		// help button
		final Button helpButton = new Button("", new TextIcon("circle-question", TextIcon.IconSet.AWESOME));
		helpButton.setOnAction(actionEvent -> infoPopup.showPopup(helpButton, InfoPopup.Pos.BELOW_RIGHT, true));
		toolBar.getItems().addAll(new Separator(), searchTextField, searchTextInput.getClearButton(),
								searchTextInput.getMethodButton(), cbVariant, optionButton, helpButton);
		// prepare for option toolbar
		optionToolBar.getStyleClass().add("fontmono");
		optionToolBar.setPadding(new Insets(3));
		// root group selectors
		final VBox checkBoxGroup = new VBox();
		final HBox dhapCheckGroup = new HBox();
		final Label lblDhap = new Label(RootDef.RootBook.DHAP.getRef() + ":   ");
		dhapCheckGroup.getChildren().add(lblDhap);
		for (int i = 0; i < 9; i++) {
			final String gnum = RootDef.rootGroupRoman[i].toLowerCase() + " ";
			final CheckBox cb = new CheckBox(gnum);
			cb.setTooltip(new Tooltip(gnum + RootDef.rootGroupName9[i]));
			cb.setOnAction(actionEvent -> selectRootGroup());
			cbRootGroup9[i] = cb;	
			dhapCheckGroup.getChildren().add(cb);
		}
		final HBox otherCheckGroup = new HBox();
		final Label lblOthers = new Label("Others: ");
		otherCheckGroup.getChildren().add(lblOthers);
		for (int i = 0; i < 8; i++) {
			final String gnum = RootDef.rootGroupRoman[i] + " ";
			final CheckBox cb = new CheckBox(gnum);
			cb.setTooltip(new Tooltip(gnum + RootDef.rootGroupName8[i]));
			cb.setOnAction(actionEvent -> selectRootGroup());
			cbRootGroup8[i] = cb;	
			otherCheckGroup.getChildren().add(cb);
		}
		checkBoxGroup.getChildren().addAll(dhapCheckGroup, otherCheckGroup);
		// all and none buttons
		final VBox allNoneGroup = new VBox();
		allNoneGroup.setPadding(new Insets(0, 5, 0, 0));
		final HBox dhapButtGroup = new HBox();
		final Button dhapAllButton = new Button("All");
		dhapAllButton.setPadding(new Insets(1));
		dhapAllButton.setOnAction(actionEvent -> selectAllRootGroup(true, null));
		final Button dhapNonButton = new Button("None");
		dhapNonButton.setPadding(new Insets(1));
		dhapNonButton.setOnAction(actionEvent -> selectAllRootGroup(false, null));
		dhapButtGroup.getChildren().addAll(dhapAllButton, dhapNonButton);
		final HBox otherButtGroup = new HBox();
		final Button otherAllButton = new Button("All");
		otherAllButton.setPadding(new Insets(1));
		otherAllButton.setOnAction(actionEvent -> selectAllRootGroup(null, true));
		final Button otherNonButton = new Button("None");
		otherNonButton.setPadding(new Insets(1));
		otherNonButton.setOnAction(actionEvent -> selectAllRootGroup(null, false));
		otherButtGroup.getChildren().addAll(otherAllButton, otherNonButton);
		allNoneGroup.getChildren().addAll(dhapButtGroup, otherButtGroup);
		// book selectors
		final VBox dhapDhmjsBox = new VBox();
		dhapDhmjsBox.setPadding(new Insets(0, 0, 0, 10));
		final CheckBox cbDhap = new CheckBox(RootDef.RootBook.DHAP.getRef());
		cbDhap.setSelected(bookSelectorSet.contains(RootDef.RootBook.DHAP));
		cbDhap.setTooltip(new Tooltip(RootDef.RootBook.DHAP.getName()));
		cbDhap.setOnAction(actionEvent -> selectRootBook(RootDef.RootBook.DHAP, cbDhap.isSelected()));
		final CheckBox cbDhmjs = new CheckBox(RootDef.RootBook.DHMJS.getRef());
		cbDhmjs.setSelected(bookSelectorSet.contains(RootDef.RootBook.DHMJS));
		cbDhmjs.setTooltip(new Tooltip(RootDef.RootBook.DHMJS.getName()));
		cbDhmjs.setOnAction(actionEvent -> selectRootBook(RootDef.RootBook.DHMJS, cbDhmjs.isSelected()));
		dhapDhmjsBox.getChildren().addAll(cbDhap, cbDhmjs);
		final VBox saddDhatvaBox = new VBox();
		saddDhatvaBox.setPadding(new Insets(0, 0, 0, 10));
		final CheckBox cbSaddDha = new CheckBox(RootDef.RootBook.SADDDHA.getRef());
		cbSaddDha.setSelected(bookSelectorSet.contains(RootDef.RootBook.SADDDHA));
		cbSaddDha.setTooltip(new Tooltip(RootDef.RootBook.SADDDHA.getName()));
		cbSaddDha.setOnAction(actionEvent -> selectRootBook(RootDef.RootBook.SADDDHA, cbSaddDha.isSelected()));
		final CheckBox cbDhatva = new CheckBox(RootDef.RootBook.DHATVA.getRef());
		cbDhatva.setSelected(bookSelectorSet.contains(RootDef.RootBook.DHATVA));
		cbDhatva.setTooltip(new Tooltip(RootDef.RootBook.DHATVA.getName()));
		cbDhatva.setOnAction(actionEvent -> selectRootBook(RootDef.RootBook.DHATVA, cbDhatva.isSelected()));
		saddDhatvaBox.getChildren().addAll(cbSaddDha, cbDhatva);
		
		optionToolBar.getChildren().addAll(checkBoxGroup, allNoneGroup,
										new Separator(Orientation.VERTICAL), dhapDhmjsBox, saddDhatvaBox);
		toolBarBox.getChildren().add(toolBar);
		mainPane.setTop(toolBarBox);

		// add context menu
		final MenuItem openMenuItem = new MenuItem("Open");
		openMenuItem.setOnAction(actionEvent -> openDoc());		
		popupMenu.getItems().addAll(openMenuItem);
		table.setOnMouseClicked(mouseEvent -> clickHandler(mouseEvent));

		// add main content
		final Scene scene = new Scene(mainPane, windowWidth, windowHeight);
		table.setItems(outputList);
		mainPane.setCenter(table);
		
		// add detail display on the bottom
		final VBox detailBox = new VBox();
		detailBox.getChildren().add(message);
		mainPane.setBottom(detailBox);

		// some intialization
		if (ReaderUtilities.rootList == null)
			ReaderUtilities.updateRootList();
		selectAllRootGroup(true, true);
		setupTable();

		setScene(scene);
		
		// prepare info popup
		infoPopup.setContentWithText(ReaderUtilities.getTextResource("info-rootfinder.txt"));
		infoPopup.setTextWidth(Utilities.getRelativeSize(36));
	}

	private void toggleOption() {
		if (toolBarBox.getChildren().size() > 1)
			toolBarBox.getChildren().remove(optionToolBar);
		else
			toolBarBox.getChildren().add(optionToolBar);
	}

	private void selectAllRootGroup(final Boolean grp9, final Boolean grp8) {
		if (grp9 != null) {
			final boolean selected = grp9;
			for (final CheckBox cb : cbRootGroup9)
				cb.setSelected(selected);
		}
		if (grp8 != null) {
			final boolean selected = grp8;
			for (final CheckBox cb : cbRootGroup8)
				cb.setSelected(selected);
		}
		selectRootGroup();
	}

	private void selectRootGroup() {
		workingList.clear();
		if (bookSelectorSet.contains(RootDef.RootBook.DHAP)) {
			for (int i = 0; i < 9; i++) {
				if (!cbRootGroup9[i].isSelected()) continue;
				final int grp = i + 1;
				final List<RootDef> rlist = ReaderUtilities.rootList.stream()
					.filter(r -> r.getBook() == RootDef.RootBook.DHAP)
					.filter(r -> r.getGroup() == grp)
					.collect(Collectors.toList());
				workingList.addAll(rlist);
			}
		}
		for (int i = 0; i < 8; i++) {
			if (!cbRootGroup8[i].isSelected()) continue;
			final int grp = i + 1;
			final List<RootDef> rlist = ReaderUtilities.rootList.stream()
										.filter(r -> r.getBook() != RootDef.RootBook.DHAP && bookSelectorSet.contains(r.getBook()))
										.filter(r -> r.getGroup() == grp)
										.collect(Collectors.toList());
			workingList.addAll(rlist);
		}
		setOutputList();
	}
	
	private void selectRootBook(final RootDef.RootBook book, final boolean doAdd) {
		if (doAdd)
			bookSelectorSet.add(book);
		else
			bookSelectorSet.remove(book);
		selectRootGroup();
	}

	private void setOutputList() {
		final String filter = Normalizer.normalize(searchTextField.getText().trim(), Form.NFC);
		setOutputList(filter);
	}

	private void setOutputList(final String filter) {
		outputList.clear();
		final List<RootDef> finalList = cbVariant.isSelected()
										? workingList
										: workingList.stream()
											.filter(x -> x.getVariant() == RootDef.Variant.NO)
											.collect(Collectors.toList());
		final List<RootDef> output;
		if (filter.isEmpty()) {
			output = finalList;
		} else {
			output = finalList.stream()
					.filter(x -> x.getRoot().contains(filter) || x.getDefinition().contains(filter))
					.collect(Collectors.toList()); 
		}
		for (final RootDef rd : output)
			outputList.add(new RootOutput(rd));
		// show item count
		final int count = outputList.size();
		final String s = count <= 1 ? "" : "s";
		message.setText(count + " item" + s + " listed");
	}

	private void setupTable() {
		final TableColumn<RootOutput, String> refCol = new TableColumn<>("Ref");
		refCol.setCellValueFactory(new PropertyValueFactory<>(outputList.get(0).refProperty().getName()));
		refCol.setReorderable(false);
		refCol.setComparator(Utilities.alphanumComparator);
		refCol.prefWidthProperty().bind(mainPane.widthProperty().divide(12).multiply(2));
		final TableColumn<RootOutput, String> rootCol = new TableColumn<>("Root");
		rootCol.setCellValueFactory(new PropertyValueFactory<>(outputList.get(0).rootProperty().getName()));
		rootCol.setReorderable(false);
		rootCol.setComparator(Utilities.paliComparator);
		rootCol.prefWidthProperty().bind(mainPane.widthProperty().divide(12).multiply(2).subtract(10));
		final TableColumn<RootOutput, String> defCol = new TableColumn<>("Definition");
		defCol.setCellValueFactory(new PropertyValueFactory<>(outputList.get(0).defProperty().getName()));
		defCol.setReorderable(false);
		defCol.setComparator(Utilities.paliComparator);
		defCol.prefWidthProperty().bind(mainPane.widthProperty().divide(12).multiply(6));
		final TableColumn<RootOutput, String> groupCol = new TableColumn<>("Group");
		groupCol.setCellValueFactory(new PropertyValueFactory<>(outputList.get(0).groupProperty().getName()));
		groupCol.setReorderable(false);
		groupCol.prefWidthProperty().bind(mainPane.widthProperty().divide(12).multiply(2).subtract(10));
		table.getColumns().clear();
		table.getColumns().add(refCol);
		table.getColumns().add(rootCol);
		table.getColumns().add(defCol);	
		table.getColumns().add(groupCol);
	}

	private void clickHandler(final MouseEvent event) {
		final RootOutput thisRoot = table.getSelectionModel().getSelectedItem();
		if (event == null || thisRoot == null) return;
		if (event.getButton() == MouseButton.SECONDARY) {
			if (thisRoot.getRootDef().getBook() != RootDef.RootBook.DHAP) {
				popupMenu.setUserData(thisRoot.getRootDef());
				popupMenu.show(table, event.getScreenX(), event.getScreenY());
			}
		} else {
			popupMenu.hide();
		}
	}
	
	private void openDoc() {
		final RootDef rootDef = (RootDef)popupMenu.getUserData();
		final String bookId = rootDef.getBook().toString().toLowerCase();
		final int refNum = rootDef.getRefNum();
		final String strRefNum = rootDef.getBook() == RootDef.RootBook.SADDDHA
									? "[" + refNum + "]"
									: refNum + ".";
		final Corpus cp = ReaderUtilities.corpusMap.get(Corpus.Collection.GRAM);
		final DocumentInfo dinfo = cp.getDocInfo(bookId);
		if (dinfo != null && !dinfo.getFileNameWithExt().isEmpty()) {
			final TocTreeNode node = dinfo.toTocTreeNode();
			if (Utilities.checkFileExistence(node.getNodeFile()))
				ReaderUtilities.openPaliHtmlViewer(node, strRefNum);
		}
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
			final RootOutput root = table.getItems().get(i);
			final String[] data = new String[colCount];
			data[0] = root.refProperty().get();
			data[1] = root.rootProperty().get();
			data[2] = root.defProperty().get();
			data[4] = root.groupProperty().get();
			result.add(data);
		}
		return result;
	}
	
	private void copyCSV() {
		Utilities.copyCSV(makeCSV());
	}
	
	private void saveCSV() {
		Utilities.saveCSV(makeCSV(), "paliroots.csv");
	}

	// inner class
	public static final class RootOutput {
		private StringProperty ref;
		private StringProperty root;
		private StringProperty group;
		private StringProperty def;
		private final RootDef rootDef;
		
		public RootOutput(final RootDef rdef) {
			rootDef = rdef;
			final RootDef.Variant variant = rootDef.getVariant();
			final String varStr = variant == RootDef.Variant.NO ? "" : " [" + variant.toString().toLowerCase() + ".]";
			refProperty().set(rdef.getFullRef());
			rootProperty().set(rdef.getRoot() + varStr);
			groupProperty().set(rdef.getGroupString());
			defProperty().set(rdef.getDefinition());
		}
		
		public StringProperty refProperty() {
			if (ref == null)
				ref = new SimpleStringProperty(this, "ref");
			return ref;
		}
		
		public StringProperty rootProperty() {
			if (root == null)
				root = new SimpleStringProperty(this, "root");
			return root;
		}
		
		public StringProperty groupProperty() {
			if (group == null)
				group = new SimpleStringProperty(this, "group");
			return group;
		}
		
		public StringProperty defProperty() {
			if (def == null)
				def = new SimpleStringProperty(this, "def");
			return def;
		}
		
		public RootDef getRootDef() {
			return rootDef;
		}
	}

}
