/*
 * SktDictWin.java
 *
 * Copyright (C) 2023-2026 J. R. Bhaddacak 
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

package paliplatform.sanskrit;

import paliplatform.sanskrit.SanskritUtilities.SktDictBook;
import paliplatform.base.*;
import paliplatform.base.ScriptTransliterator.EngineType;

import java.util.*;
import java.util.regex.*;
import java.util.stream.Collectors;
import java.text.Normalizer;
import java.text.Normalizer.Form;

import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.geometry.Pos;
import javafx.geometry.Insets;

/**
 * The sanskrit dictionary window's pane.
 * @author J.R. Bhaddacak
 * @version 4.1
 * @since 3.5
 */
public class SktDictWin extends DictWinBase {
	static enum OperationMode {
		NORMAL("Normal mode"), EXP_MW("Explore MW"), EXP_AP("Explore AP"),
		EXP_SHS("Explore SHS"), EXP_MD("Explore MD"), EXP_BHS("Explore BHS");
		public static final OperationMode[] values = OperationMode.values();
		private String name;
		private OperationMode(final String nam) {
			name = nam;
		}
		public String getName() {
			return name;
		}
		public String getTableName() {
			if (this == NORMAL)
				return "";
			else
				return name.substring(name.lastIndexOf(" ") + 1);
		}
		public SktDictBook getDictBook() {
			if (this == EXP_MW)
				return SktDictBook.MW;
			else if (this == EXP_AP)
				return SktDictBook.AP;
			else if (this == EXP_SHS)
				return SktDictBook.SHS;
			else if (this == EXP_MD)
				return SktDictBook.MD;
			else if (this == EXP_BHS)
				return SktDictBook.BHS;
			else
				return null;
		}
		public List<ExploringOption> getOptionList() {
			final List<ExploringOption> result;
			if (this == EXP_MW) {
				result = new ArrayList<>();
				result.add(new ExploringOption("Roots/Verbs", "<ab>cl."));
				result.add(new ExploringOption("Genuine", "<info verb=\"genuineroot"));
				result.add(new ExploringOption("Non-genuine", "<info verb=\"root"));
				result.add(new ExploringOption("Westergaard", "<info westergaard"));
				result.add(new ExploringOption("Whitney", "<info whitneyroots"));
				result.add(new ExploringOption("Masculine", "<lex>m."));
				result.add(new ExploringOption("Feminine", "<lex>f."));
				result.add(new ExploringOption("Neuter", "<lex>n."));
				result.add(new ExploringOption("M/F/N (adj.)", "<lex>mfn.", "<ab>mfn."));
				result.add(new ExploringOption("M/F*/N (adj.)", "<lex>mf("));
				result.add(new ExploringOption("Pronouns", "<ab>pron."));
				result.add(new ExploringOption("Pronominals", "<ab>pronom."));
				result.add(new ExploringOption("Indeclinables", "<lex>ind."));
				result.add(new ExploringOption("Ind. Participles", "<ab>ind.p."));
			} else if (this == EXP_AP) {
				result = new ArrayList<>();
				result.add(new ExploringOption("Roots/Verbs", "€"));
				result.add(new ExploringOption("Masculine", "{\\%m."));
				result.add(new ExploringOption("Feminine", "{\\%f."));
				result.add(new ExploringOption("Neuter", "{\\%n."));
				result.add(new ExploringOption("Pronominals", "{\\%pron. a."));
				result.add(new ExploringOption("Adjectives", "{\\%a."));
				result.add(new ExploringOption("Adverbs", "{\\%adv."));
				result.add(new ExploringOption("Numerals", "{\\%Num. a.", "{\\%num. a."));
				result.add(new ExploringOption("Past Participles", "{\\%p. p."));
				result.add(new ExploringOption("Pot. Participles", "{\\%Pot. p.", "{\\%pot. p."));
				result.add(new ExploringOption("Pres. Participles", "{\\%Pres. p.", "{\\%pres. p."));
				result.add(new ExploringOption("Indeclinables", "{\\%ind."));
			} else if (this == EXP_SHS) {
				result = new ArrayList<>();
				result.add(new ExploringOption("Roots/Verbs", " cl. "));
				result.add(new ExploringOption("Masculine", " m. "));
				result.add(new ExploringOption("Feminine", " f. "));
				result.add(new ExploringOption("Masc/Fem", " mf. "));
				result.add(new ExploringOption("Neuter", " n. "));
				result.add(new ExploringOption("Masc/Neut", " mn. "));
				result.add(new ExploringOption("M/F/N", " mfn. "));
				result.add(new ExploringOption("Pronominals", " Pron. ", " pron. "));
				result.add(new ExploringOption("Adjectives", " Adj. ", " adj. "));
				result.add(new ExploringOption("Adverbs", " Adv. ", " adv. "));
				result.add(new ExploringOption("Indeclinables", " Ind. ", " ind. "));
			} else if (this == EXP_MD) {
				result = new ArrayList<>();
				result.add(new ExploringOption("Roots/Verbs", "<cl>"));
				result.add(new ExploringOption("Masculine", "<lex>m."));
				result.add(new ExploringOption("Feminine", "<lex>f."));
				result.add(new ExploringOption("Neuter", "<lex>n."));
				result.add(new ExploringOption("Pronominals", "<lex>prn.", "<ab>prn."));
				result.add(new ExploringOption("Adjectives", "<lex>a."));
				result.add(new ExploringOption("Adverbs", "<lex>ad."));
				result.add(new ExploringOption("Perfect Participles", "<ab>pp."));
				result.add(new ExploringOption("Future Participles", "<ab>fp."));
				result.add(new ExploringOption("Indeclinables", "<ab>indec.", "<ab>indecl."));
			} else if (this == EXP_BHS) {
				result = new ArrayList<>();
				result.add(new ExploringOption("Masculine", "<lex>m."));
				result.add(new ExploringOption("Feminine", "<lex>f."));
				result.add(new ExploringOption("Neuter", "<lex>nt."));
				result.add(new ExploringOption("Adjectives", "<lex>adj."));
				result.add(new ExploringOption("Past Participles", "<lex>ppp."));
				result.add(new ExploringOption("Indeclinables", "<lex>indecl."));
			} else {
				result = Collections.emptyList();
			}
			return result;
		}
	}
	private static final String[] shsXrefMapArr = {
		"2187", "apāc", "3938", "avāc", "4913", "ā", "6939", "udaka",
		"6964", "udac", "17495", "tiryyac", "23294", "parāñc", "26826", "pratyac",
		"27762", "prāc", "37845", "viṣvac", "31847", "viṣvadryac", "43087", "samyac",
		"46719", "havā" };
	private static final Map<String, String> shsXrefMap = new HashMap<>();
	private static final Pattern tagPatt = Pattern.compile("<([^> ]+)( *[^>]*)>(.*?)</\\1>");
	private static final Pattern slp1Patt = Pattern.compile("\\{#(.*?)#\\}");
	private static final Pattern italicPatt = Pattern.compile("\\{%(.*?)%\\}");
	private static final Pattern boldPatt = Pattern.compile("\\{@(.*?)@\\}");
	private static final Pattern unreadablePatt = Pattern.compile("\\{\\?(.*?)\\?\\}");
	private static final Pattern infoPatt = Pattern.compile("<[^>]+/>");
	private static final Pattern poemPatt = Pattern.compile("<Poem>(.*?)</Poem>");
	private static final List<String> mwExpList = Arrays.asList("m.", "f.", "mfn.");
	private final ChoiceBox<ExploringOption> exploringChoice = new ChoiceBox<>();
	private final ToggleGroup operationGroup = new ToggleGroup();
	private final SimpleObjectProperty<OperationMode> operationMode = new SimpleObjectProperty<>(OperationMode.NORMAL);

	public SktDictWin(final Object[] args) {
		// initialization
		setPrefWidth(Utilities.getRelativeSize(70));
		inMeaningButton.disableProperty().bind(useWildcards.or(incremental).or(operationMode.isNotEqualTo(OperationMode.NORMAL)));
		final ChangeListener<String> searchTextListener = (obs, oldValue, newValue) -> {
			if (incremental.get()) {
				if (operationMode.get() == OperationMode.NORMAL)
					submitSearch(newValue);
				else
					explore();
			}
		};
		searchTextField.textProperty().removeListener(defSearchTextListener);
		searchTextField.textProperty().addListener(searchTextListener);
		if (shsXrefMap.isEmpty()) {
			for (int i = 0; i < shsXrefMapArr.length; i += 2) {
				shsXrefMap.put(shsXrefMapArr[i], shsXrefMapArr[i + 1]);
			}
		}
		if (SanskritUtilities.simpleServiceMap == null) 
			SanskritUtilities.simpleServiceMap = SanskritUtilities.getSimpleServices();
		final HBox dictToolBox = new HBox();
		dictToolBox.disableProperty().bind(operationMode.isNotEqualTo(OperationMode.NORMAL));
		dictToolBox.setAlignment(Pos.BOTTOM_LEFT);
		dictToolBox.setPadding(new Insets(1, 1, 1, 1));
		dictToolBox.setSpacing(5);
		for (final SktDictBook d : SktDictBook.books) {
			final CheckBox cb =  createDictCheckBox(d);
			dictCBMap.put(d, cb);
			dictToolBox.getChildren().add(cb);
		}
		final MenuButton operationMenu = new MenuButton("", new TextIcon("check-double", TextIcon.IconSet.AWESOME));
		operationMenu.setTooltip(new Tooltip("Operation mode"));
		for (final OperationMode m : OperationMode.values) {
			final RadioMenuItem radio = new RadioMenuItem(m.getName());
			radio.setUserData(m);
			if (m != OperationMode.NORMAL)
				radio.disableProperty().bind(SanskritUtilities.sktDictAvailMap.get(m.getDictBook()).not());
			radio.setToggleGroup(operationGroup);
			operationMenu.getItems().add(radio);
		}
		operationGroup.selectedToggleProperty().addListener((observable) -> {
			if (operationGroup.getSelectedToggle() != null) {
				operationModeSelected();
			}
		});
		toolBar.getItems().addAll(dictToolBox, new Separator(), operationMenu);
		// add context tool box
		exploringChoice.disableProperty().bind(operationMode.isEqualTo(OperationMode.NORMAL));
		contextToolBox.getChildren().add(exploringChoice);
		// other setup
		searchInput.setSanskritMode(true);
		final String inputMethod = Utilities.getSetting("sanskrit-input-method");
		searchInput.setInputMethod(PaliTextInput.InputMethod.valueOf(inputMethod));
		editorLauncher = (SimpleService)SanskritUtilities.simpleServiceMap.get("paliplatform.main.EditorLauncher");
		editorButton.setDisable(editorLauncher == null);
		htmlViewer.setStyleSheet(Utilities.SKTDICT_CSS);
		fxHandler = new SktDictFXHandler(htmlViewer, this);
		// prepare info popup
		infoPopup.setContentWithText(SanskritUtilities.getTextResource("info-sanskrit-dictionaries.txt"));
		infoPopup.setTextWidth(Utilities.getRelativeSize(42));
		init(args);
	}

	@Override
	public final void init(final Object[] args) {
		dictSet.clear();
		final String strDictSet = Utilities.getSetting("sktdictset");
		final String[] arrDictSet = strDictSet.split(",");
		for (final SktDictBook book : SktDictBook.books)
			dictCBMap.get(book).setSelected(false);
		for (final String s : arrDictSet) {
			if (SktDictBook.isValid(s)) {
				final SktDictBook book = SktDictBook.valueOf(s);
				if (!SanskritUtilities.sktDictAvailMap.get(book).get())
					continue;
				dictSet.add(book);
				dictCBMap.get(book).setSelected(true);
			}
		}
		htmlViewer.setContent(Utilities.makeHTML(""));
		if (args != null) {
			final String term = (String)args[0];
			if (!term.isEmpty())
				searchTextField.setText(term);
		} else {
			searchTextField.clear();
			resultList.clear();
			resultMap.clear();
		}
		searchTextField.requestFocus();
		operationGroup.selectToggle(operationGroup.getToggles().get(0));
		operationMode.set(OperationMode.NORMAL);
		exploringChoice.getItems().clear();
		findBox.init();
		Platform.runLater(() -> resultPane.setBottom(null));			
	}

	private CheckBox createDictCheckBox(final SktDictBook book) {
		final CheckBox cb = SanskritUtilities.createSktDictCheckBox(book);
		cb.disableProperty().bind(SanskritUtilities.sktDictAvailMap.get(book).not());
		cb.setOnAction(actionEvent -> {
			if (cb.isSelected())
				dictSet.add(book);
			else
				dictSet.remove(book);
			search();
		});
		return cb;
	}

	@Override
	public String processInput(final String query) {
		return Utilities.convertToRomanSanskrit(query);
	}

	private String processQuery(final String query) {
		String result = query;
		if (useWildcards.get()) {
			if (result.indexOf('?') >= 0)
				result = result.replace("?", "_");
			if (result.indexOf('*') >= 0)
				result = result.replace("*", "%");
		}
		return result;
	}

	private boolean isQueryValid(final String query) {
		final int uCount = Utilities.charCount(query, '_');
		final int pCount = Utilities.charCount(query, '%');
		// just * or a sheer combination of ? and * is not allowed
		if (query.length() == 0 || (pCount > 0 && pCount + uCount == query.length()))
			return false;
		else
			return true;
	}

	@Override
	protected void search() {
		final String strQuery = searchTextField.getText().trim();
		if (operationMode.get() == OperationMode.NORMAL) {
			if (strQuery.isEmpty())
				resultList.clear();
			else
				submitSearch(strQuery);
		} else {
			explore();
		}
	}

	@Override
	public void search(final String query) {
		// remove single qoute causing SQL error
		final String properQuery = query.replace("'", "");
		final String termQuery = processQuery(properQuery);
		if (!isQueryValid(termQuery))
			return;
		resultMap.clear();
		for (final Object book : dictSet) {
			final SktDictBook dicBook = (SktDictBook)book;
			final String tail = useWildcards.get() ? "';" : "%';";
			String dbQuery = "SELECT KEY1 FROM " + dicBook.toString() + " WHERE KEY1 LIKE '" + termQuery + tail;
			if (inMeaning.get()) {
				// make 2 queries, all lowercase and title case
				final String[] inMQuery = { properQuery, Character.toUpperCase(properQuery.charAt(0)) + properQuery.substring(1) };
				dbQuery = "SELECT KEY1 FROM " + dicBook.toString() + 
					" WHERE MEANING LIKE '%" + inMQuery[0] + "%' OR MEANING LIKE '%" + inMQuery[1] +
					"%' ORDER BY ID;";
			}
			final Set<String> results = Utilities.getFirstColumnFromDB(Utilities.H2DB.SKTDICT, dbQuery);
			for (final String term : results) {
				final ArrayList<Object> dList;
				if (resultMap.containsKey(term))
					dList = resultMap.get(term);
				else
					dList = new ArrayList<>();
				dList.add(dicBook);
				resultMap.put(term, dList);
			}
		} // end for
		resultList.setAll(resultMap.keySet().stream().sorted(Utilities.sktComparator).collect(Collectors.toList()));
		resultListView.scrollTo(0);
		if (!resultList.isEmpty()) {
			showResult(resultList.get(0));
			showMessage(resultList.size() + " found");
			initialStringToLocate = inMeaning.get() ? properQuery : "";
		}
	}

	@Override
	public void showResult(final String term) {
		final StringBuilder result = new StringBuilder();
		final ArrayList<SktDictBook> dicts = resultMap.get(term).stream()
										.map(x -> (SktDictBook)x)
										.sorted(Comparator.comparing(x -> x.ordinal()))
										.collect(Collectors.toCollection(ArrayList::new));
		if (dicts == null) return;
		// show head word
		result.append("<h1>").append(term).append("</h1>");
		for (int i = 0; i < dicts.size(); i++) {
			final SktDictBook dict = dicts.get(i);
			result.append("<p class=bookname>&lt;").append(dict.bookName).append("&gt;</p>");
				final String res = getResultArticle(dict, term);
				result.append(res);
			if (i < dicts.size() - 1)
				result.append("<div class=hrule></div><p></p>");
		}
		htmlViewer.setContent(Utilities.makeHTML(result.toString()));
	}

	private String getResultArticle(final SktDictBook dict, final String term) {
		final List<SktDictEntry> items = SanskritUtilities.lookUpSktDictFromDB(dict, term);
		final StringBuilder text = new StringBuilder();
		for (final SktDictEntry entry : items) {
			final String hNum = entry.getHnum();
			final String hNumStr = hNum.isEmpty() ? "" : "<sup>" + hNum + "</sup>";
			final String k2 = entry.getKey2();
			final String k2Str = dict.keySLP1DecodeNeeded()
								? ScriptTransliterator.translitQuick(k2, EngineType.SLP1_IAST, true)
								: k2;
			text.append("<h3>" + k2Str + hNumStr + "</h3>");
			text.append("<p>");
			String meaning = entry.getMeaning().replace("&", "&amp;");
			text.append(formatMeaning(dict, meaning));
			text.append("</p>");
			text.append("<p></p>");
		}
		return text.toString();
	}

	private String formatMeaning(final SktDictBook dict, final String input) {
		String result = input;
		switch (dict) {
			case MW:
				result = formatMWMeaning(input);
				break;
			case AP:
				result = formatAPMeaning(input);
				break;
			case SHS:
				result = formatSHSMeaning(input);
				break;
			case MD:
				result = formatMDMeaning(input);
				break;
			case BHS:
				result = formatBHSMeaning(input);
				break;
			case MWE:
				result = formatMWEMeaning(input);
				break;
			case AE:
				result = formatAEMeaning(input);
				break;
			case BOR:
				result = formatBORMeaning(input);
				break;
		}
		return result;
	}

	public static String formatMWMeaning(final String input) {
		String result = input;
		// bullet
		result = result.replace("¦", " •");
		// meaning break for verbs
		final Pattern breakPatt = Pattern.compile("<div n=\"(?:to|vp|p)\"/>");
		result = breakPatt.matcher(result).replaceAll("<br> - ");
		// root
		final Pattern rootPatt = Pattern.compile("<info verb=\"root\" cp=\"(.*?)\"/>");
		result = rootPatt.matcher(result).replaceAll("<br> - Root: $1.");
		// genuine root
		final Pattern genuineRootPatt = Pattern.compile("<info verb=\"genuineroot\" cp=\"(.*?)\"/>");
		result = genuineRootPatt.matcher(result).replaceAll("<br> - Genuine root: $1.");
		// Whitney root
		final Pattern whitneyRootPatt = Pattern.compile("<info whitneyroots=\"(.*?)\"/>");
		result = whitneyRootPatt.matcher(result).replaceAll(m ->
				"<br> - Whitney's root: " + ScriptTransliterator.translitQuick(m.group(1), EngineType.SLP1_IAST, false) + ".");
		// Westergaard root
		final Pattern westergaardRootPatt = Pattern.compile("<info westergaard=\"(.*?)\"/>");
		result = westergaardRootPatt.matcher(result).replaceAll(m ->
				"<br> - Westergaard's root: " + ScriptTransliterator.translitQuick(m.group(1), EngineType.SLP1_IAST, false) + ".");
		// remove mere info tags
		result = infoPatt.matcher(result).replaceAll("");
		// change other tags to span
		// 1st round, save span to restore later
		result = tagPatt.matcher(result).replaceAll("<::span:: class='$1'$2>$3</::span::>");
		// 2nd round for nested tags
		final Pattern tagPatt2 = Pattern.compile("<(.{1,4})( *[^>]*)>(.*?)</\\1>");
		result = tagPatt2.matcher(result).replaceAll("<::span:: class='$1'$2>$3</::span::>");
		// restore span
		result = result.replace("::span::", "span");
		return result;
	}

	public static String formatAPMeaning(final String input) {
		String result = input;
		final Map<String, String> xrefMap = Map.of( "5008", "avadhātavya", "6060", "asamāvṛttaḥ" );
		// dash
		result = result.replace("¦", "—");
		// decode SLP1
		result = slp1Patt.matcher(result).replaceAll(m -> 
				"<i>" + ScriptTransliterator.translitQuick(m.group(1), EngineType.SLP1_IAST, true) + "</i>");
		// italic
		result = italicPatt.matcher(result).replaceAll("<i>$1</i>");
		// bold bullet
		final Pattern boldBullPatt = Pattern.compile("[.-]\\{@(.*?)@\\}");
		result = boldBullPatt.matcher(result).replaceAll("<br> • <b>$1</b>");
		// bold bullet paren
		final Pattern boldBullParPatt = Pattern.compile("[.-]\\(\\{@(.*?)@\\}\\)");
		result = boldBullParPatt.matcher(result).replaceAll("<br> • (<b>$1</b>)");
		// bold inline
		final Pattern boldInlinePatt = Pattern.compile("\\{@(.*?)@\\}");
		result = boldInlinePatt.matcher(result).replaceAll("<b>$1</b>");
		// ² and ³ bullet
		result = result.replace(".²", "<br> • ²").replace(".³", "<br> • ³");
		// <Poem>
		result = poemPatt.matcher(result).replaceAll("<p class='poem'>$1</p>");
		// LBody
		final Pattern lbodyPatt = Pattern.compile("\\{\\{Lbody=(.*?)\\}\\}");
		result = lbodyPatt.matcher(result).replaceAll(m -> {
			final String xref = xrefMap.getOrDefault(m.group(1), "");
			return xref.isEmpty() ? "" : "See <i>" + xref + ".</i>";
		});
		return result;
	}

	public static String formatSHSMeaning(final String input) {
		String result = input;
		// dash
		result = result.replace("¦", "—");
		// decode SLP1
		result = slp1Patt.matcher(result).replaceAll(m -> 
				"<i>" + ScriptTransliterator.translitQuick(m.group(1), EngineType.SLP1_IAST, true) + "</i>");
		// italic
		result = italicPatt.matcher(result).replaceAll("<i>$1</i>");
		// unreadable
		result = unreadablePatt.matcher(result).replaceAll("($1?)");
		// <Poem>
		result = poemPatt.matcher(result).replaceAll("<p class='poem'>$1</p>");
		// LBody
		final Pattern lbodyPatt = Pattern.compile("\\{\\{Lbody=(.*?)\\}\\}");
		result = lbodyPatt.matcher(result).replaceAll(m -> {
			final String xref = shsXrefMap.getOrDefault(m.group(1), "");
			return xref.isEmpty() ? "" : "See <i>" + xref + ".</i>";
		});
		return result;
	}

	public static String formatMDMeaning(final String input) {
		String result = input;
		// dash
		result = result.replace("¦", "—");
		// tags
		result = tagPatt.matcher(result).replaceAll("<span class='$1'$2>$3</span>");
		// decode SLP1
		result = slp1Patt.matcher(result).replaceAll(m -> 
				"<i>" + ScriptTransliterator.translitQuick(m.group(1), EngineType.SLP1_IAST, true) + "</i>");
		// italic
		result = italicPatt.matcher(result).replaceAll("<i>$1</i>");
		// bold
		result = boldPatt.matcher(result).replaceAll("<b>$1</b>");
		// unwanted char
		result = result.replace("🞄", "");
		return result;
	}

	public static String formatBHSMeaning(final String input) {
		String result = input;
		// dash
		result = result.replace("¦", "—");
		// tags
		result = tagPatt.matcher(result).replaceAll("<span class='$1'$2>$3</span>");
		// italic
		result = italicPatt.matcher(result).replaceAll("<i>$1</i>");
		// bold
		result = boldPatt.matcher(result).replaceAll("<b>$1</b>");
		return result;
	}

	private static String formatMWEMeaning(final String input) {
		String result = input;
		// dash bullet
		result = result.replace("—", "<br> —");
		// dash
		result = result.replace("¦", "—");
		// decode SLP1
		result = slp1Patt.matcher(result).replaceAll(m -> 
				"<i>" + ScriptTransliterator.translitQuick(m.group(1), EngineType.SLP1_IAST, true) + "</i>");
		// italic
		result = italicPatt.matcher(result).replaceAll("<i>$1</i>");
		return result;
	}

	private static String formatAEMeaning(final String input) {
		String result = input;
		// dash
		result = result.replace("¦", "—");
		// remove mere info tags
		result = infoPatt.matcher(result).replaceAll("");
		// decode SLP1
		result = slp1Patt.matcher(result).replaceAll(m -> 
				"<i>" + ScriptTransliterator.translitQuick(m.group(1), EngineType.SLP1_IAST, true) + "</i>");
		// italic
		result = italicPatt.matcher(result).replaceAll("<i>$1</i>");
		// bold
		result = boldPatt.matcher(result).replaceAll("<b>$1</b>");
		return result;
	}

	private static String formatBORMeaning(final String input) {
		String result = input;
		// dash
		result = result.replace("¦", "—");
		// decode SLP1
		result = slp1Patt.matcher(result).replaceAll(m -> 
				"<i>" + ScriptTransliterator.translitQuick(m.group(1), EngineType.SLP1_IAST, true) + "</i>");
		// italic
		result = italicPatt.matcher(result).replaceAll("<i>$1</i>");
		// bold
		result = boldPatt.matcher(result).replaceAll("<b>$1</b>");
		// <ls>
		final Pattern lsPatt = Pattern.compile("<ls>(.*?)</ls>");
		result = lsPatt.matcher(result).replaceAll("<span class='ls'>$1</span>");
		return result;
	}

	private void operationModeSelected() {
		final Toggle selected = operationGroup.getSelectedToggle();
		final OperationMode mode = (OperationMode)selected.getUserData();
		operationMode.set(mode);
		initialStringToLocate = "";
		if (mode == OperationMode.NORMAL) {
			search();
		} else {
			exploringChoice.getItems().clear();
			exploringChoice.getItems().addAll(mode.getOptionList());
			exploringChoice.setOnAction(actionEvent -> explore());
			exploringChoice.getSelectionModel().select(0);
		}
	}

	private void explore() {
		resultMap.clear();
		final ExploringOption option = exploringChoice.getSelectionModel().getSelectedItem();
		if (option == null) return;
		final String strQuery = processQuery(Normalizer.normalize(searchTextField.getText().trim(), Form.NFC).replace("'", ""));
		final String termQuery = isQueryValid(strQuery) ? strQuery : "";
		String keyLikeStr = "";
		if (!termQuery.isEmpty()) {
			final String tail = useWildcards.get() ? "' AND " : "%' AND ";
			keyLikeStr = " KEY1 LIKE '" + termQuery + tail;
		}
		final List<String> ruleList = option.getRuleList();
		String meaningLikeStr = "";
		if (ruleList.size() > 1) {
			meaningLikeStr += "(";
			for (final String r : ruleList) {
				meaningLikeStr += " MEANING LIKE '%" + r + "%' OR ";
			}
			meaningLikeStr = meaningLikeStr.substring(0, meaningLikeStr.length() - 3) + ");";
		} else {
			meaningLikeStr = " MEANING LIKE '%" + ruleList.get(0) + "%';";
		}
		final OperationMode mode = operationMode.get();
		final String tabName = mode.getTableName();
		final String dbQuery = "SELECT KEY1 FROM " + tabName + " WHERE" + keyLikeStr + meaningLikeStr;
		final Set<String> results = Utilities.getFirstColumnFromDB(Utilities.H2DB.SKTDICT, dbQuery);
		if (!results.isEmpty()) {
			final ArrayList<Object> dList = new ArrayList<>();
			dList.add(mode.getDictBook());
			for (final String term : results) {
				resultMap.put(term, dList);
			}
		}
		resultList.setAll(resultMap.keySet().stream().sorted(Utilities.sktComparator).collect(Collectors.toList()));
		resultListView.scrollTo(0);
		if (!resultList.isEmpty()) {
			showResult(resultList.get(0));
			showMessage(resultList.size() + " found");
		}
	}

	// inner class
	static class ExploringOption {
		private String name;
		private List<String> ruleList;
		public ExploringOption(final String nam, final String... rules) {
			name = nam;
			ruleList = new ArrayList<>();
			for (final String r : rules) {
				ruleList.add(r);
			}
		}
		public String getName() {
			return name;
		}
		public List<String> getRuleList() {
			return ruleList;
		}
		@Override
		public String toString() {
			return name;
		}
	}

}
