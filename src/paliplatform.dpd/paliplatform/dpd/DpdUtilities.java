/*
 * DpdUtilities.java
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

package paliplatform.dpd;

import paliplatform.base.*;

import java.util.*;
import java.util.stream.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.*;
import javafx.geometry.*;
import javafx.stage.Stage;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.SimpleObjectProperty;
import com.google.gson.stream.*;

/** 
 * The utility factory for the DPD module.
 * @author J.R. Bhaddacak
 * @version 3.0
 * @since 3.0
 */
final public class DpdUtilities {
	public static enum SearchMethod { 
		TERM_START("Term start"), TERM_WITHIN("Term within"), BOTH_WITHIN("Both within (slow)");
		public static final SearchMethod[] values = values();
		private final String name;
		private SearchMethod(final String name) {
			this.name = name;
		}
		public String getName() {
			return name;
		}
	}
	static enum TermFamily {
		COMPOUND(new String[] { "family_compound", "compound_family" }), WORD(new String[] { "family_word", "word_family"}),
		IDIOM(new String[] { "family_idiom", "idiom" }), SET(new String[] { "family_set", "\"set\"" });
		private final String tabName;
		private final String key;
		public static TermFamily[] values = values();
		private TermFamily(final String[] info) {
			tabName = info[0];
			key = info[1];
		}
		public String getTableName() {
			return tabName;
		}
		public String getKey() {
			return key;
		}
	}
	public static boolean downloaderOpened = false;
	public static final String TXTDIR = "resources/text/";
	public static final String LINESEP = System.getProperty("line.separator");
	public static final int DEF_MAX_RESULT = 500;
	public static final List<Integer> MAXLIST = Arrays.asList(100, 500, 1000, 5000, 10000, 50000);
	public static final String READY = "Ready to search";
	public static final String RETRIEVING = "Retrieving data... (please wait)";
	public static final SimpleBooleanProperty dpdAvailable = new SimpleBooleanProperty(false);
	public static final SimpleBooleanProperty ppdpdDBLocked = new SimpleBooleanProperty(false);
	public static final SimpleObjectProperty<Node> ppdpdDBLockIcon = new SimpleObjectProperty<>(null);
	private static final String[] dbLockStatus = { "PP-DPD DB unlocked", "PP-DPD DB locked" };
	public static final SimpleStringProperty ppdpdDBLockString = new SimpleStringProperty(dbLockStatus[0]);

	static String getStringResource(final String fileNameWithPath) {
		String result = "";
		try {
			final InputStream in = DpdUtilities.class.getResourceAsStream(fileNameWithPath);
			if (in != null)
				result = new String(in.readAllBytes(), StandardCharsets.UTF_8);
		} catch (IOException e) {
			System.err.println(e);
		}
		return result;
	}
    
	static String getTextResource(final String filename) {
		return getStringResource(TXTDIR + filename);
	}

	static boolean checkIfDpdAvailable() {
		final File dpdDB = new File(Utilities.ROOTDIR + Utilities.DBPATH + Utilities.SQLiteDB.DPD.getNameWithExt());
		final boolean result = dpdDB.exists();
		dpdAvailable.set(result);
		return result;
	}

	public static void updatePpdpdDBLockStatus() {
		final boolean lock = !Utilities.isDBWritable(Utilities.H2DB.PPDPD);
		final String status = lock ? dbLockStatus[1] : dbLockStatus[0];
		final Node icon = lock
							? new TextIcon("lock", TextIcon.IconSet.AWESOME)
							: new TextIcon("unlock", TextIcon.IconSet.AWESOME);
		ppdpdDBLocked.set(lock);
		ppdpdDBLockString.set(status);
		ppdpdDBLockIcon.set(icon);
	}

	public static void lockPpdpdDB(final boolean lock) {
		Utilities.setDBWritable(Utilities.H2DB.PPDPD, !lock);
		updatePpdpdDBLockStatus();
	}
	
	static List<DpdRoot> getRootList() {
		final List<DpdRoot> result = new ArrayList<>();
		final java.sql.Connection conn = Utilities.SQLiteDB.DPD.getConnection();
		if (conn == null) return result;
		ResultSet resultSet = null;
		Statement statement = null;
        try {
			statement = conn.createStatement();
			final String select = "SELECT root,root_group,root_sign,root_meaning," +
				  "sanskrit_root,sanskrit_root_meaning,root_example," + 
				  "dhatupatha_root,dhatupatha_pali,dhatupatha_english," +
				  "dhatumanjusa_root,dhatumanjusa_pali,dhatumanjusa_english," +
				  "dhatumala_root,dhatumala_pali,dhatumala_english," +
				  "panini_root,panini_sanskrit,panini_english," +
				  "note,root_matrix" +
				  " FROM dpd_roots;";
			resultSet = statement.executeQuery(select);
			while (resultSet.next()) {
				final String root = resultSet.getString("root");
				final int grp = resultSet.getInt("root_group");
				final String sign = resultSet.getString("root_sign");
				final String meaning = resultSet.getString("root_meaning");
				final String sanskrit = resultSet.getString("sanskrit_root") + " = " + 
										resultSet.getString("sanskrit_root_meaning");
				final String example = resultSet.getString("root_example");
				final String[] patha = { resultSet.getString("dhatupatha_root"),
										resultSet.getString("dhatupatha_pali"),
										resultSet.getString("dhatupatha_english") };
				final String[] manjusa = { resultSet.getString("dhatumanjusa_root"),
										resultSet.getString("dhatumanjusa_pali"),
										resultSet.getString("dhatumanjusa_english") };
				final String[] mala = { resultSet.getString("dhatumala_root"),
										resultSet.getString("dhatumala_pali"),
										resultSet.getString("dhatumala_english") };
				final String[] panini = { resultSet.getString("panini_root"),
										resultSet.getString("panini_sanskrit"),
										resultSet.getString("panini_english") };
				final String note = resultSet.getString("note");
				final String matrix = resultSet.getString("root_matrix");
				final DpdRoot dpdRoot = new DpdRoot(root);
				dpdRoot.setGroup(grp);
				dpdRoot.setRootSign(sign);
				dpdRoot.setMeaning(meaning);
				dpdRoot.setSanskritInfo(sanskrit);
				dpdRoot.setExample(example);
				dpdRoot.setPatha(patha);
				dpdRoot.setManjusa(manjusa);
				dpdRoot.setMala(mala);
				dpdRoot.setPanini(panini);
				dpdRoot.setNote(note);
				dpdRoot.setMatrix(matrix);
				result.add(dpdRoot);
			}
        } catch (SQLException e) {
            System.err.println(e);
        }
		return result;
	}

	static List<String> getFamilyList(final TermFamily family) {
		final String select = "SELECT " + family.getKey() + " FROM " + family.getTableName() + ";";
		final Set<String> resultSet = Utilities.getFirstColumnFromDB(Utilities.SQLiteDB.DPD.getConnection(), select);
		return family == TermFamily.SET
				? resultSet.stream().sorted().collect(Collectors.toList())
				: resultSet.stream().sorted(Utilities.paliComparator).collect(Collectors.toList());
	}

	static Map<String, List<List<String>>> getRootFamily(final String root) {
		final Map<String, List<List<String>>> result = new HashMap<>();
		final java.sql.Connection conn = Utilities.SQLiteDB.DPD.getConnection();
		if (conn == null) return result;
		ResultSet resultSet = null;
		Statement statement = null;
        try {
			statement = conn.createStatement();
			final String select = "SELECT root_family,data from family_root WHERE root_key = '" + root + "';";
			resultSet = statement.executeQuery(select);
			while (resultSet.next()) {
				final String rootFamily = resultSet.getString("root_family");
				final String data = resultSet.getString("data");
				result.put(rootFamily, readJsonArray(data));
			}
        } catch (IOException | SQLException e) {
            System.err.println(e);
        }
		return result;
	}

	static List<List<String>> getFamilyData(final TermFamily family, final String word) {
		final String select = "SELECT data FROM " + family.getTableName() + " WHERE " + family.getKey() + " = '" + word + "';";
		List<List<String>> result = Collections.emptyList();
		final java.sql.Connection conn = Utilities.SQLiteDB.DPD.getConnection();
		if (conn == null) return result;
		ResultSet resultSet = null;
		Statement statement = null;
        try {
			statement = conn.createStatement();
			resultSet = statement.executeQuery(select);
			if (resultSet.next()) {
				final String data = resultSet.getString("data");
				result = readJsonArray(data);
			}
        } catch (IOException | SQLException e) {
            System.err.println(e);
        }
		return result;
	}

	static List<StringPair> getDecompositionList() {
		final List<StringPair> result = new ArrayList<>();
		final java.sql.Connection conn = Utilities.SQLiteDB.DPD.getConnection();
		if (conn == null) return result;
		ResultSet resultSet = null;
		Statement statement = null;
        try {
			statement = conn.createStatement();
			final String select = "SELECT lookup_key,decompositor FROM lookup WHERE decompositor != '';";
			resultSet = statement.executeQuery(select);
			while (resultSet.next()) {
				final String key = resultSet.getString("lookup_key");
				final String decon = resultSet.getString("decompositor");
				final StringPair pair = new StringPair(key, decon);
				result.add(pair);
			}
			System.out.println(result.size());
        } catch (SQLException e) {
            System.err.println(e);
        }
		return result;
	}

	static List<List<String>> readJsonArray(final String text) throws IOException {
		final List<List<String>> result = new ArrayList<>();
		final JsonReader reader = new JsonReader(new StringReader(text));
		try {
			reader.beginArray();
			while (reader.hasNext()) {
				reader.beginArray();
				final List<String> inner = new ArrayList<>();
				while (reader.hasNext()) {
					inner.add(reader.nextString());
				}
				result.add(inner);
				reader.endArray();
			}
			reader.endArray();
		} finally {
			reader.close();
		}
		return result;
	}

	static String stripTags(final String htmlText) {
		return htmlText.replaceAll("</?.*?>", "");
	}

	static String replaceTags(final String htmlText, final String replacement) {
		return htmlText.replaceAll("</?.*?>", replacement);
	}

	static boolean proceedBuildConfirm(final Stage stage) {
		boolean output = false;
		final String message = "The existing table(s) will be replaced, \nproceed to continue.";
		final ConfirmAlert proceedAlert = new ConfirmAlert(stage, ConfirmAlert.ConfirmType.PROCEED, message);
		final Optional<ButtonType> result = proceedAlert.showAndWait();
		if (result.isPresent()) {
			if (result.get() == proceedAlert.getConfirmButtonType())
				output = true;
		}
		return output;		
	}

	static List<DpdHeadWord> getDpdHeadWords(final int[] ids) {
		if (ids.length == 0)
			return Collections.emptyList();
		final List<DpdHeadWord> result = new ArrayList<>();
		try {
			final java.sql.Connection dpdConn = Utilities.SQLiteDB.DPD.getConnection();
			String where = "WHERE ID = " + ids[0];
			for (int i = 1; i < ids.length; i++) {
				where = where + " OR ID = " + ids[i];
			}
			final String select = "SELECT " +
				"lemma_1,pos,grammar,verb,trans,plus_case,meaning_1,meaning_2,meaning_lit,sanskrit," +
				"root_key,stem,construction,notes,family_word,family_idioms,family_compound,family_set " +
				"FROM dpd_headwords " + where + ";"; 
			final Statement stmt = dpdConn.createStatement();
			final ResultSet res = stmt.executeQuery(select);
			while (res.next()) {
				final DpdHeadWord hw = new DpdHeadWord(res.getString("lemma_1"));
				hw.setGrammar(res.getString("grammar"));
				hw.setVerb(res.getString("verb"));
				hw.setTrans(res.getString("trans"));
				hw.setPlusCase(res.getString("plus_case"));
				hw.setMeaning1(res.getString("meaning_1"));
				hw.setMeaning2(res.getString("meaning_2"));
				hw.setMeaningLit(res.getString("meaning_lit"));
				hw.setSanskrit(res.getString("sanskrit"));
				hw.setRootKey(res.getString("root_key"));
				hw.setStem(res.getString("stem"));
				hw.setConstruction(res.getString("construction"));
				hw.setNotes(res.getString("notes"));
				hw.setFamilyWord(res.getString("family_word"));
				hw.setFamilyIdiom(res.getString("family_idioms"));
				hw.setFamilyCompound(res.getString("family_compound"));
				hw.setFamilySet(res.getString("family_set"));
				result.add(hw);
			}
			res.close();
			stmt.close();
		} catch (SQLException e) {
			System.err.println(e);
		}
		return result;
	}

	static TextFlow createInfoTextFlow(final String head, final String text) {
		final TextFlow textFlow = new TextFlow();
		if (!head.isEmpty())
			textFlow.getChildren().add(createInfoHead(head));
		if (!text.isEmpty())
			textFlow.getChildren().add(createInfoText(text));
		return textFlow;
	}

	static TextFlow createInfoTextFlowWithButton(final String head, final String text, final Button button) {
		final TextFlow textFlow = createInfoTextFlow(head, text);
		textFlow.getChildren().addAll(createInfoText(" "), button);
		return textFlow;
	}

	static Text createInfoHead(final String text) {
		final Text txtHead = new Text(text);
		txtHead.getStyleClass().add("reader-info");
		txtHead.setStyle("-fx-font-weight:bold;");
		return txtHead;
	}

	static Text createInfoText(final String text) {
		final Text txtText = new Text(text);
		txtText.getStyleClass().add("reader-info");
		return txtText;
	}

	static TextFlow createHeadTextFlow(final String text, final String size) {
		final TextFlow tfResult = new TextFlow();
		final Text txtHead = new Text(text);
		txtHead.getStyleClass().add("reader-term");
		txtHead.setStyle("-fx-font-size:" + size + ";-fx-font-weight:bold;");
		tfResult.getChildren().add(txtHead);
		return tfResult;
	}

	static VBox getDpdHeadWordBox(final DpdHeadWord hw, final boolean showWordFamily, final boolean showIdiomFamily) {
		final VBox result = new VBox();
		result.getStyleClass().add("bordered-box");
		result.getChildren().add(createHeadTextFlow(hw.getTerm(), "1.2em"));
		// detail
		final String grammar = hw.getGrammar();
		final String verb = hw.getVerb();
		final String trans = hw.getTrans();
		final String plusCase = hw.getPlusCase();
		final List<String> gramList = List.of(grammar, verb, trans, plusCase);
		final String gramStr = gramList.stream().filter(x -> DpdHeadWordBase.hasData(x))
										.collect(Collectors.joining("; "));
		if (!gramStr.isEmpty())
			result.getChildren().add(DpdUtilities.createInfoTextFlow("", "(" + gramStr + ")"));
		final String meaning1 = hw.getMeaning1();
		if (DpdHeadWordBase.hasData(meaning1))
			result.getChildren().add(DpdUtilities.createInfoTextFlow("• ", meaning1));
		final String meaning2 = hw.getMeaning2();
		if (DpdHeadWordBase.hasData(meaning2))
			result.getChildren().add(DpdUtilities.createInfoTextFlow("‣ ", meaning2));
		final String meaningLit = hw.getMeaningLit();
		if (DpdHeadWordBase.hasData(meaningLit))
			result.getChildren().add(DpdUtilities.createInfoTextFlow("Lit.: ", meaningLit));
		final String sanskrit = hw.getSanskrit();
		if (DpdHeadWordBase.hasData(sanskrit))
			result.getChildren().add(DpdUtilities.createInfoTextFlow("Skt.: ", sanskrit));
		final String rootKey = hw.getRootKey();
		if (DpdHeadWordBase.hasData(rootKey)) {
			final Button rootKeyButton = new Button("", new TextIcon("square-arrow-up-right", TextIcon.IconSet.AWESOME));
			rootKeyButton.setPadding(new Insets(0, 2, 0, 2));
			rootKeyButton.setOnAction(actionEvent -> DpdRootWin.INSTANCE.display(rootKey));		
			result.getChildren().addAll(DpdUtilities.createInfoTextFlowWithButton("Root: ", rootKey, rootKeyButton));
		}
		final String stem = hw.getStem();
		if (DpdHeadWordBase.hasData(stem))
			result.getChildren().add(DpdUtilities.createInfoTextFlow("Stem: ", stem));
		final String construction = hw.getConstruction();
		if (DpdHeadWordBase.hasData(construction))
			result.getChildren().add(DpdUtilities.createInfoTextFlow("Con.: ", construction));
		final String compFamily = hw.getFamilyCompound();
		if (DpdHeadWordBase.hasData(compFamily)) {
			result.getChildren().addAll(DpdUtilities.createInfoTextFlow("Comp.: ", compFamily));
		}
		final String setFamily = hw.getFamilySet();
		if (DpdHeadWordBase.hasData(setFamily)) {
			result.getChildren().addAll(DpdUtilities.createInfoTextFlow("Set: ", setFamily));
		}
		final String notes = hw.getNotes();
		if (DpdHeadWordBase.hasData(notes))
			result.getChildren().add(DpdUtilities.createInfoTextFlow("Notes: ", notes));
		final String familyWord = hw.getFamilyWord();
		if (DpdHeadWordBase.hasData(familyWord) && showWordFamily) {
			final List<List<String>> wordFamilyList = DpdUtilities.getFamilyData(DpdUtilities.TermFamily.WORD, familyWord);
			if (!wordFamilyList.isEmpty()) {
				result.getChildren().add(DpdUtilities.createInfoTextFlow("\nWord Family:", ""));
				final VBox wordFamilyBox = DpdUtilities.createWordFamilyBox(familyWord, wordFamilyList);
				result.getChildren().add(wordFamilyBox);
			}
		}
		final String familyIdiom = hw.getFamilyIdiom();
		if (DpdHeadWordBase.hasData(familyIdiom) && showIdiomFamily) {
			final List<List<String>> idiomFamilyList = DpdUtilities.getFamilyData(DpdUtilities.TermFamily.IDIOM, familyIdiom);
			if (!idiomFamilyList.isEmpty()) {
				result.getChildren().add(DpdUtilities.createInfoTextFlow("\nIdiom Family:", ""));
				final VBox idiomFamilyBox = DpdUtilities.createWordFamilyBox(familyIdiom, idiomFamilyList);
				result.getChildren().add(idiomFamilyBox);
			}
		}
		return result;
	}

	static VBox createWordFamilyBox(final String family, final List<List<String>> famList) {
		final VBox resultBox = new VBox();
		resultBox.getChildren().add(createHeadTextFlow("\n" + family, "1.2em"));
		addWordFamilyData(resultBox, famList);
		return resultBox;
	}

	static void addWordFamilyData(final VBox box, final List<List<String>> data) {
		for (final List<String> list : data) {
			if (list.size() < 4) continue;
			final TextFlow tfFamText = new TextFlow();
			final Text txtWord = createInfoHead(list.get(0));
			final Text txtGram = createInfoText(" (" + list.get(1) + ") ");
			final Text txtMean = createInfoText(list.get(2));
			final Text txtMark = createInfoText(" " + list.get(3));
			tfFamText.getChildren().addAll(txtWord, txtGram, txtMean, txtMark);
			box.getChildren().add(tfFamText);
		}
	}

	static String getVBoxTextFlowText(final VBox box) {
		final StringBuilder result = new StringBuilder();
		for (final Node node : box.getChildren()) {
			if (node instanceof TextFlow) {
				result.append(getTextFlowText((TextFlow)node));
			} else if (node instanceof VBox) {
				final Object boxData = node.getUserData();
				if (boxData != null && ((String)boxData).equals("term-box"))
					result.append(LINESEP).append(LINESEP);
				result.append(getVBoxTextFlowText((VBox)node));
			}
		}
		return result.toString();
	}

	private static String getTextFlowText(final TextFlow textflow) {
		final StringBuilder result = new StringBuilder();
		for (final Node tnode : ((Pane)textflow).getChildren()) {
			if (tnode instanceof Text) {
				final Text text = (Text)tnode;
				result.append(text.getText());
			}
		}
		result.append(LINESEP);
		return result.toString();
	}

}
