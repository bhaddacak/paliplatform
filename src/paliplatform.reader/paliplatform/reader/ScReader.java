/*
 * ScReader.java
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

package paliplatform.reader;

import paliplatform.base.*;
import static paliplatform.reader.ScDocument.Nikaya;

import java.util.*;
import java.util.stream.*;
import java.util.function.*;
import javafx.application.Platform;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/** 
 * The HTML viewer of SuttaCentral Pāli texts.
 * @author J.R. Bhaddacak
 * @version 3.0
 * @since 3.0
 */
public class ScReader extends PaliHtmlViewerBase {
	private static final double INIT_DIVIDER_POSITION = 0.2;
	private static final double EXPANDING_FACTOR = 1.3;
	private static final List<String> VINAYA = 
		List.of("Mahāvibhaṅga:pli-tv-bu-vb", "Bhikkhunivibhaṅga:pli-tv-bi-vb",
				"Khandhaka:pli-tv-kd", "Parivāra:pli-tv-pvr", "Dvemātikāpāḷi:-pm");
	private static final List<String> KHUDDAKA = 
		List.of("Khuddakapāṭha:kp", "Dhammapada:dhp", "Udāna:ud", "Itivuttaka:iti",
				"Suttanipāta:snp", "Vimānavatthu:vv", "Petavatthu:pv", "Theragāthā:thag",
				"Therīgāthā:thig", "Therāpadāna:tha-ap", "Therīapadāna:thi-ap", "Buddhavaṃsa:bv",
				"Cariyāpiṭaka:cp", "Jātaka:ja", "Mahāniddesa:mnd", "Cūḷaniddesa:cnd", 
				"Paṭisambhidāmagga:ps", "Milindapañha:mil", "Netti:ne", "Peṭakopadesa:pe");
	private static final List<String> ABHIDHAM =
		List.of("Dhammasaṅgaṇī:ds", "Vibhaṅga:vb", "Dhātukathā:dt", "Puggalapaññatti:pp",
				"Kathāvatthu:kv", "Yamaka:ya", "Paṭṭhāna:patthana");
	private static final Map<Nikaya, Integer> suttaCountMap = 
		Map.of(Nikaya.DN, 34, Nikaya.MN, 152, Nikaya.SN, 56, Nikaya.AN, 11);
	private final SplitPane splitPane = new SplitPane();
	private final BorderPane navPane = new BorderPane();
	private final ObservableList<StringPair> suttaList = FXCollections.<StringPair>observableArrayList();
	private final ListView<StringPair> suttaSelectorListView = new ListView<>(suttaList);
	private final ToolBar navToolBar = new ToolBar();
	private final ChoiceBox<Nikaya> nikayaChoice = new ChoiceBox<>();
	private final ComboBox<StringPair> groupChoice = new ComboBox<>();
	private final CheckMenuItem sentIdMenuItem = new CheckMenuItem("Show sentence ids");
	private final CheckMenuItem variantMenuItem = new CheckMenuItem("Show variants");
	private final CheckMenuItem transMenuItem = new CheckMenuItem("Show translations");
	private final CheckMenuItem commentMenuItem = new CheckMenuItem("Show comments");
	private final CheckMenuItem referenceMenuItem = new CheckMenuItem("Show references");
	private final CheckMenuItem useMDotAboveMenuItem = new CheckMenuItem("Use ṁ");
	private final ChoiceBox<String> transLangChoice = new ChoiceBox<>();
	private final ToggleGroup scriptLangGroup = new ToggleGroup();
	private ScDocument currDoc = null;

	public ScReader(final String docId) {
		super();
		webEngine.setUserStyleSheetLocation(ReaderUtilities.class.getResource(ReaderUtilities.SC_CSS).toExternalForm());
		// set up tool bar
		final Button toggleLeftPaneButton = new Button("", new TextIcon("left-pane", TextIcon.IconSet.CUSTOM));
		toggleLeftPaneButton.setTooltip(new Tooltip("Left pane on/off"));
		toggleLeftPaneButton.setOnAction(actionEvent -> {
			if (splitPane.getItems().size() > 1) {
				splitPane.getItems().remove(navPane);
			} else {
				splitPane.getItems().setAll(navPane, textPane);
				final Nikaya nikaya = nikayaChoice.getSelectionModel().getSelectedItem();
				final double pos = nikaya == Nikaya.DN || nikaya == Nikaya.MN 
									? INIT_DIVIDER_POSITION
									: INIT_DIVIDER_POSITION * EXPANDING_FACTOR;
				splitPane.setDividerPositions(pos);
			}
		});
		final MenuButton optionsMenu = new MenuButton("", new TextIcon("check-double", TextIcon.IconSet.AWESOME));		
		optionsMenu.setTooltip(new Tooltip("Display options"));
		sentIdMenuItem.setOnAction(actionEvent -> updateContent());
		variantMenuItem.setOnAction(actionEvent -> updateContent());
		transMenuItem.setOnAction(actionEvent -> updateContent());
		commentMenuItem.setOnAction(actionEvent -> updateContent());
		referenceMenuItem.setOnAction(actionEvent -> updateContent());
		useMDotAboveMenuItem.setOnAction(actionEvent -> updateContent());
		final MenuItem showAllMenuItem = new MenuItem("Show all details");
		showAllMenuItem.setOnAction(actionEvent -> {
			sentIdMenuItem.setSelected(true);
			variantMenuItem.setSelected(true);
			transMenuItem.setSelected(true);
			commentMenuItem.setSelected(true);
			referenceMenuItem.setSelected(true);
			updateContent();
		});
		final MenuItem hideAllMenuItem = new MenuItem("Hide all details");
		hideAllMenuItem.setOnAction(actionEvent -> {
			sentIdMenuItem.setSelected(false);
			variantMenuItem.setSelected(false);
			transMenuItem.setSelected(false);
			commentMenuItem.setSelected(false);
			referenceMenuItem.setSelected(false);
			updateContent();
		});
		optionsMenu.getItems().addAll(showAllMenuItem, hideAllMenuItem, new SeparatorMenuItem(), sentIdMenuItem,
						variantMenuItem, transMenuItem, commentMenuItem, referenceMenuItem,
						new SeparatorMenuItem(), useMDotAboveMenuItem, new SeparatorMenuItem());
		for (final Utilities.PaliScript script : Utilities.PaliScript.scripts) {
			if (script.ordinal() == 0) continue;
			final RadioMenuItem radio = new RadioMenuItem(script.getName());
			radio.setUserData(script);
			radio.setToggleGroup(scriptLangGroup);
			optionsMenu.getItems().add(radio);
		}
		scriptLangGroup.selectToggle(scriptLangGroup.getToggles().get(0));
        scriptLangGroup.selectedToggleProperty().addListener(observable -> updateContent());
		transLangChoice.setTooltip(new Tooltip("Language of translations/comments"));
        transLangChoice.setOnAction(actionEvent -> updateContent());
		final Button helpButton = new Button("", new TextIcon("circle-question", TextIcon.IconSet.AWESOME));
		helpButton.setOnAction(actionEvent -> helpInfoPopup.showPopup(helpButton, InfoPopup.Pos.BELOW_RIGHT, true));
		toolBar.getItems().addAll(new Separator(), toggleLeftPaneButton, optionsMenu, transLangChoice, helpButton);
		// set up left nav pane
		for (final Nikaya nik : Nikaya.values) {
			nikayaChoice.getItems().add(nik);
		}
		nikayaChoice.setOnAction(actionEvent -> nikayaSelected());
		groupChoice.setOnAction(actionEvent -> groupSelected());
		navToolBar.getItems().add(nikayaChoice);
		navPane.setTop(navToolBar);
		suttaSelectorListView.setCellFactory((ListView<StringPair> lv) -> {
			return new ListCell<StringPair>() {
				@Override
				public void updateItem(StringPair item, boolean empty) {
					super.updateItem(item, empty);
					this.setGraphic(null);
					if (empty) {
						this.setText(null);
						this.setTooltip(null);
					} else {
						final StringPair pair = this.getItem();
						final String id = pair.getFirst();
						final String info = pair.getSecond();
						if (!info.isEmpty()) {
							String tip = info.replace(";", ", ");
							tip = tip.substring(0, tip.length() - 2);
							this.setTooltip(new Tooltip(tip));
						}
						final String idStr = id.startsWith("pli-tv-")
												? id.substring(7)
												: id;
						this.setText(idStr + " " + getTextName(pair));
					}
					this.setStyle("-fx-padding: 0px 0px 0px 3px");
				}
			};
		});
		suttaSelectorListView.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> {
			if (currDoc == null) return;
			final StringPair selItem = newValue;
			if (selItem != null)
				loadContent(selItem, true);
		});
		navPane.setCenter(suttaSelectorListView);

		if (docId.isEmpty())
			splitPane.getItems().setAll(navPane, textPane);
		else
			splitPane.getItems().setAll(textPane);
		setCenter(splitPane);

		// initialization
		helpInfoPopup.setContentWithText(ReaderUtilities.getTextResource("info-screader.txt"));
		helpInfoPopup.setTextWidth(Utilities.getRelativeSize(36));
		init(docId);
	}
	
	public void init(final String docId) {
		super.init();
		if (!ReaderUtilities.scHeadFile.exists())
			ReaderUtilities.createScHeads();
		ReaderUtilities.loadScSuttaInfo();
		Platform.runLater(() -> {
			if (docId.isEmpty() && splitPane.getItems().size() < 2)
				splitPane.getItems().setAll(navPane, textPane);
			else if (!docId.isEmpty() && splitPane.getItems().size() > 1)
				splitPane.getItems().setAll(textPane);
			sentIdMenuItem.setSelected(false);
			variantMenuItem.setSelected(false);
			transMenuItem.setSelected(false);
			commentMenuItem.setSelected(false);
			referenceMenuItem.setSelected(false);
			useMDotAboveMenuItem.setSelected(false);
			nikayaChoice.getSelectionModel().select(1);
			scriptLangGroup.selectToggle(scriptLangGroup.getToggles().get(0));
			if (splitPane.getItems().size() > 1)
				splitPane.setDividerPositions(INIT_DIVIDER_POSITION);
			if (docId.isEmpty()) {
				if (!suttaList.isEmpty())
					loadContent(suttaList.get(0), true);
			} else {
				loadContent(new StringPair(docId, ""), false);
			}
		});
	}

	private String getTextName(final StringPair pair) {
		final String id = pair.getFirst();
		String info = pair.getSecond();
		return ScDocument.getTextName(id, info);
	}

	private void loadContent(final StringPair item, final boolean useNav) {
		if (item == null) return;
		final String id = item.getFirst();
		currDoc = getScDoc(id, useNav);
		if (currDoc == null) return;
		theStage.setTitle("SuttaCentral Text Reader " + "(" + id + ")");
		final Set<String> transLangs = currDoc.getTransLangs().stream()
										.map(String::toUpperCase).collect(Collectors.toSet());
		final Set<String> commLangs = currDoc.getCommentLangs().stream()
										.map(String::toUpperCase).collect(Collectors.toSet());
		final Set<String> allLangs = new HashSet<>();
		allLangs.addAll(transLangs);
		allLangs.addAll(commLangs);
		final ObservableList<String> langList = transLangChoice.getItems();
		langList.clear();
		langList.addAll(allLangs);
		FXCollections.sort(langList, Comparator.naturalOrder());
		if (!langList.isEmpty()) {
			if (langList.contains("EN"))
				transLangChoice.getSelectionModel().select("EN");
			else
				transLangChoice.getSelectionModel().select(0);
		}
		transMenuItem.setDisable(transLangs.isEmpty());
		commentMenuItem.setDisable(commLangs.isEmpty());
		updateContent();
	}

	private ScDocument getScDoc(final String id, final boolean useNav) {
		final Nikaya nikaya = useNav 
								? nikayaChoice.getSelectionModel().getSelectedItem()
								: ScInfo.getNikaya(id);
		return nikaya == null ? null : new ScDocument(id, nikaya);
	}

	private void updateContent() {
		if (currDoc == null) return;
		pageBody = formatText(currDoc);
		final String pageContent = ReaderUtilities.makeHTML(pageBody);
		setContent(pageContent);
	}

	private String formatText(final ScDocument scDoc) {
		final StringBuilder result = new StringBuilder();
		final Map<String, String> paliMap = scDoc.getPali();
		final Map<String, String> htmlMap = scDoc.getHtml();
		final Map<String, String> variantMap = scDoc.getVariant();
		final Map<String, String> refMap = scDoc.getReference();
		final String lang = transLangChoice.getItems().isEmpty()
							? ""
							: transLangChoice.getSelectionModel().getSelectedItem().toLowerCase();
		final Map<String, Map<String, String>> transMap = lang.isEmpty() ? Collections.emptyMap() : scDoc.getTranslation(lang);
		final Map<String, Map<String, String>> commentMap = lang.isEmpty() ? Collections.emptyMap() : scDoc.getComment(lang);
		if (paliMap.isEmpty()) return "";
		paliMap.forEach((id, text) -> {
			final String sentId = sentIdMenuItem.isSelected()
									? "<span class='sc-sentid'> " + id + " </span>"
									: "";
			final String variant = variantMenuItem.isSelected() && variantMap.containsKey(id)
									? "<span class='sc-variant'> [" + variantMap.get(id).trim() + "] </span>"
									: "";
			final String reference = referenceMenuItem.isSelected() && refMap.containsKey(id)
									? "<span class='sc-reference'> (" + refMap.get(id).trim() + ") </span>"
									: "";
			final String trans;
			if (transMenuItem.isSelected() && !transMap.isEmpty()) {
				final List<String> authors = transMap.keySet().stream().sorted().collect(Collectors.toList());
				String trText = "";
				String theText = "";
				for (final String a : authors) {
					final Map<String, String> trMap = transMap.get(a);
					if (trMap.containsKey(id)) {
						theText = trMap.get(id).trim();
						final String auth = "<span class='sc-author'>" + a.toUpperCase() + "</span>";
						trText = trText + auth + ": " + theText + " ";
					}
				}
				trans = theText.isEmpty() ? "" : "<span class='sc-translation'>" + trText + "</span>";
			} else {
				trans = "";
			}
			final String comm;
			if (commentMenuItem.isSelected() && !commentMap.isEmpty()) {
				final List<String> authors = commentMap.keySet().stream().sorted().collect(Collectors.toList());
				String ctText = "";
				String theText = "";
				for (final String a : authors) {
					final Map<String, String> ctMap = commentMap.get(a);
					if (ctMap.containsKey(id)) {
						theText = ctMap.get(id).trim();
						final String auth = "<span class='sc-author'>" + a.toUpperCase() + "</span>";
						ctText = ctText + auth + ": " + theText + " ";
					}
				}
				comm = theText.isEmpty() ? "" : "<span class='sc-comment'>" + ctText + "</span>";
			} else {
				comm = "";
			}
			final Utilities.PaliScript script = (Utilities.PaliScript)scriptLangGroup.getSelectedToggle().getUserData();
			final String paliPart = script == Utilities.PaliScript.ROMAN
									? useMDotAboveMenuItem.isSelected()
										? sentId + text
										: sentId + Utilities.normalizeNiggahita(text, true)
									: sentId + convertToScript(text, script);
			final String detailPart = useMDotAboveMenuItem.isSelected()
									? variant + reference + trans + comm
									: Utilities.normalizeNiggahita(variant + reference + trans + comm, true);
			final String allText = paliPart + detailPart;
			final String formatted;
			if (htmlMap.isEmpty()) {
				formatted = "<p>" + allText + "</p>";
			} else {
				final String format = htmlMap.getOrDefault(id, "{}");
				formatted = format.replace("{}", allText);
			}
			result.append(formatted).append("\n");
		});
		return result.toString();
	}

	private String convertToScript(final String text, final Utilities.PaliScript script) {
		final String normalized = Utilities.normalizeNiggahita(text, true);
		String result = "";
		switch (script) {
			case ROMAN:
				result = normalized;
				break;
			case DEVANAGARI:
				result = PaliCharTransformer.romanToDevanagari(normalized);
				break;
			case KHMER:
				result = PaliCharTransformer.romanToKhmer(normalized);
				break;
			case MYANMAR:
				result = PaliCharTransformer.romanToMyanmar(normalized);
				break;
			case SINHALA:
				result = PaliCharTransformer.romanToSinhala(normalized);
				break;
			case THAI:
				PaliCharTransformer.setUsingAltThaiChars(Boolean.parseBoolean(Utilities.settings.getProperty("thai-alt-chars")));
				result = PaliCharTransformer.romanToThai(normalized);
				break;
			default:
				result = normalized;
		}
		return result;
	}

	private void fillGroupChoice(final ComboBox<StringPair> box, final List<String> list) {
		box.getItems().clear();
		for (final String n : list) {
			final String[] arr = n.split(":");
			final StringPair pair = arr.length > 1
									? new StringPair(arr[0], arr[1])
									: new StringPair(arr[0], arr[0].toLowerCase());
			box.getItems().add(pair);
		}
	}

	private List<StringPair> getSuttaList(final Predicate<String> pred) {
		final List<StringPair> result = new ArrayList<>();
		final List<String> keyList = ReaderUtilities.scSuttaInfoMap.keySet().stream()
										.filter(pred)
										.sorted(ScInfo.scComparator)
										.collect(Collectors.toList());
		for (final String k : keyList) {
			final StringPair pair = new StringPair(k, ReaderUtilities.scSuttaInfoMap.get(k));
			result.add(pair);
		}
		return result;
	}

	private List<StringPair> getSuttaList(final Nikaya nikaya) {
		if (nikaya != Nikaya.DN && nikaya != Nikaya.MN) return Collections.emptyList();
		final List<StringPair> result = new ArrayList<>();
		final int count = suttaCountMap.get(nikaya);
		for (int i = 1; i <= count; i++) {
			final String id = nikaya.toString().toLowerCase() + i;
			final String info = ReaderUtilities.scSuttaInfoMap.getOrDefault(id, "");
			final StringPair pair = new StringPair(id, info);
			result.add(pair);
		}
		return result;
	}

	private List<StringPair> getSuttaList(final String key) {
		final List<StringPair> result = new ArrayList<>();
		if (key.matches("(?:s|a)n\\d.*")) {
			// SN and AN
			result.addAll(getSuttaList(x -> x.startsWith(key + ".")));
		} else if (key.startsWith("pli-tv-")) {
			// Vinaya
			result.addAll(getSuttaList(x -> x.startsWith(key)));
		} else if (key.equals("-pm")) {
			// Vinaya Dvemātikāpāḷi
			result.addAll(getSuttaList(x -> x.endsWith(key)));
		} else {
			// Otherwise
			result.addAll(getSuttaList(x -> x.matches(key + "\\d+.*")));
		}
		return result;
	}

	private List<String> getGroupList(final Nikaya nikaya) {
		final List<String> result = new ArrayList<>();
		if (nikaya == Nikaya.SN || nikaya == Nikaya.AN) {
			final int count = suttaCountMap.get(nikaya);
			for (int i = 1; i <= count; i++) {
				final String id = nikaya.toString() + i;
				result.add(id + ":");
			}
		} else if (nikaya == Nikaya.KN) {
			result.addAll(KHUDDAKA);
		} else if (nikaya == Nikaya.VIN) {
			result.addAll(VINAYA);
		} else if (nikaya == Nikaya.ABH) {
			result.addAll(ABHIDHAM);
		}
		return result;
	}

	private void nikayaSelected() {
		final Nikaya nikaya = nikayaChoice.getSelectionModel().getSelectedItem();
		if (nikaya == null) return;
		nikayaChoice.setTooltip(new Tooltip(nikaya.getName()));
		switch (nikaya) {
			case DN:
			case MN:
				suttaList.clear();
				suttaList.addAll(getSuttaList(nikaya));
				if (navToolBar.getItems().size() > 1)
					navToolBar.getItems().remove(groupChoice);
				suttaSelectorListView.scrollTo(0);
				suttaSelectorListView.getSelectionModel().select(0);
				splitPane.setDividerPositions(INIT_DIVIDER_POSITION);
				break;
			case SN:
			case AN:
			case KN:
			case VIN:
			case ABH:
				fillGroupChoice(groupChoice, getGroupList(nikaya));
				if (navToolBar.getItems().size() == 1)
					navToolBar.getItems().add(groupChoice);
				groupChoice.getSelectionModel().select(0); // event does not fire!
				groupSelected(); // this causes double action in the first select
				splitPane.setDividerPositions(INIT_DIVIDER_POSITION * EXPANDING_FACTOR);
				break;
		}
	}

	private void groupSelected() {
		final StringPair group = groupChoice.getSelectionModel().getSelectedItem();
		if (group == null) return;
		final String key = group.getSecond();
		suttaList.clear();
		suttaList.addAll(getSuttaList(key));
		suttaSelectorListView.scrollTo(0);
		suttaSelectorListView.getSelectionModel().select(0);
	}

	@Override
	protected void findNext(final String query, final int direction) {
		final int caseSensitive = findBox.isCaseSensitive() ? 1 : 0;
		webEngine.executeScript("findNext('" + query + "'," + caseSensitive + "," + direction + ")");
	}
	
}
