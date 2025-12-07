/*
 * GrammarUtilities.java
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
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see https://www.gnu.org/licenses/.
 */

package paliplatform.grammar;

import paliplatform.base.*;
import paliplatform.base.PaliWord;
import paliplatform.dict.*;

import java.util.*;
import java.util.stream.*;
import java.util.ServiceLoader.Provider;
import java.io.InputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import javafx.stage.Stage;
import javafx.scene.image.Image;
import javafx.scene.layout.GridPane;
import javafx.scene.control.*;
import javafx.geometry.*;

/** 
 * The utility factory for the Grammar module.
 * @author J.R. Bhaddacak
 * @version 3.0
 * @since 3.0
 */
final public class GrammarUtilities {
	private static final String TXTDIR = "resources/text/";
	public static final String PALI_ROOT_LIST = TXTDIR + "paliroots.csv";
	public static final String DECLINABLES = TXTDIR + "declinables.txt";
	public static final String PALI_PRONOUN_LIST = TXTDIR + "pronouns.csv";
	public static final String PALI_NUMERAL_LIST = TXTDIR + "numerals.csv";
	public static final String PALI_COMMON_VERB_LIST = TXTDIR + "vcommon.csv";
	public static final String PARADIGM_VERB_LIST = TXTDIR + "paradv.csv";
	public static final String PROSODY = TXTDIR + "prosody.csv";
	public static final Map<Integer, PaliRoot> paliRoots = new HashMap<>();
	public static final List<String> declinables = new ArrayList<>();
	public static final Map<String, PaliWord> paliNumerals = new LinkedHashMap<>(100);
	public static final Map<String, PaliWord> paliOrdinals = new LinkedHashMap<>(10);
	public static final Map<String, PaliWord> paliPronouns = new LinkedHashMap<>(30);
	public static final Map<String, List<String>> paliCardinalMap = new LinkedHashMap<>();
	public static final Map<String, List<String>> paliOrdinalMap = new LinkedHashMap<>();	
	public static final Map<String, PaliWord> paliIrrNouns = new HashMap<>(160);
	public static final Map<String, DeclinedWord> declPronounsMap = new HashMap<>();
	public static final Map<String, DeclinedWord> declNumbersMap = new HashMap<>();
	public static final Map<String, DeclinedWord> declIrrNounsMap = new HashMap<>();
	public static ReaderService readerServiceImp;
	public static LuceneService luceneServiceImp;
	public static PaliDeclension declension;

	static String getTextResource(final String filename) {
		String result = "";
		try {
			final InputStream in = GrammarUtilities.class.getResourceAsStream(TXTDIR + filename);
			if (in != null)
				result = new String(in.readAllBytes(), StandardCharsets.UTF_8);
		} catch (IOException e) {
			System.err.println(e);
		}
		return result;
	}

	public static void initializeServices() {
		readerServiceImp = ServiceLoader.load(ReaderService.class)
				.stream()
				.filter((Provider p) -> p.type().getName().equals("paliplatform.reader.ReaderServiceImp"))
				.map(Provider::get)
				.findFirst()
				.orElse(null);
		luceneServiceImp = ServiceLoader.load(LuceneService.class)
				.stream()
				.filter((Provider p) -> p.type().getName().equals("paliplatform.lucene.LuceneServiceImp"))
				.map(Provider::get)
				.findFirst()
				.orElse(null);
	}
    
	public static void openWindow(final Utilities.WindowType win, final Object[] args) {
		final Stage stg = Utilities.getOpenedWindow(win.getWindowClassName());
		switch (win) {
			case DECLENSION:
				if (stg == null) {
					Utilities.openNewWindow(new DeclensionWin(args), 
						new Image(GrammarUtilities.class.getResourceAsStream("resources/images/table-cells.png")), "Declension Table");
				} else {
					final DeclensionWin declWin = (DeclensionWin)stg.getScene().getRoot();
					declWin.init(DeclensionWin.Mode.NOUN, args);					
					Utilities.showExistingWindow(stg);
				}
				break;
			case PROSODY:
				if (stg == null) {
					Utilities.openNewWindow(new ProsodyWin(args), 
						new Image(GrammarUtilities.class.getResourceAsStream("resources/images/music.png")), "Prosody");
				} else {
					final ProsodyWin prosWin = (ProsodyWin)stg.getScene().getRoot();
					if (args == null) {
						prosWin.reset();
					} else {
						final String text = (String)args[0];
						prosWin.analyze(text);
					}
					Utilities.showExistingWindow(stg);
				}
				break;
		}
	}

	public static void loadRootList() {
		if (!paliRoots.isEmpty()) return;
		try (final Scanner in = new Scanner(RootWin.class.getResourceAsStream(PALI_ROOT_LIST), StandardCharsets.UTF_8)) {
			while (in.hasNextLine()) {
				final String line = in.nextLine().trim();
				if (line.charAt(0) == '#')
					continue;
				final String[] items = line.split(":");
				String term = items[1];
				final int indTR = term.indexOf("[");
				String termRmk = "";
				if (indTR >= 0) {
					// the root term has a remark
					termRmk = term.substring(indTR+1, term.indexOf("]"));
					term = term.substring(0, indTR);
				}
				String pmean = items[2];
				final int indMR = pmean.indexOf("[");
				String meanRmk = "";
				if (indMR >= 0) {
					// the Pali meaning has a remark
					meanRmk = pmean.substring(indMR+1, pmean.indexOf("]"));
					pmean = pmean.replaceFirst("\\[.*\\]", "");
				}
				final Integer id = Integer.parseInt(items[0]);
				final String group = items[4];
				final PaliRoot root = new PaliRoot(id, term, group);
				root.setRootRemark(termRmk);
				root.setPaliMeaning(pmean);
				root.setMeaningRemark(meanRmk);
				root.setEngMeaning(items[3]);
				paliRoots.put(id, root);
			}
		}
	}

	public static void loadDeclinables() {
		if (!declinables.isEmpty())
			return;
		try (final Scanner in = new Scanner(GrammarUtilities.class.getResourceAsStream(DECLINABLES), StandardCharsets.UTF_8)) {
			while (in.hasNextLine()) {
				final String line = in.nextLine().trim();
				if (!line.isEmpty())
					declinables.add(line);
			}
		}
	}

	public static void loadPronounList() {
		if (!paliPronouns.isEmpty())
			return;
		try (final Scanner in = new Scanner(GrammarUtilities.class.getResourceAsStream(PALI_PRONOUN_LIST), StandardCharsets.UTF_8)) {
			while (in.hasNextLine()) {
				final String line = in.nextLine().trim();
				if (line.charAt(0) == '#')
					continue;
				final String[] items = line.split(":");
				final String term = items[0];
				final PaliWord word = new PaliWord(term);
				word.addParadigm(items[1]);
				word.addMeaning(items[2]);
				word.addPosInfo("pron.");
				word.setAllGenders();
				word.setEnding();
				paliPronouns.put(term, word);
			}
		}
	}
	
	public static void loadNumeralList() {
		if (!paliNumerals.isEmpty())
			return;
		try (final Scanner in = new Scanner(GrammarUtilities.class.getResourceAsStream(PALI_NUMERAL_LIST), StandardCharsets.UTF_8)) {
			while (in.hasNextLine()) {
				final String line = in.nextLine().trim();
				if (line.charAt(0) == '#')
					continue;
				final String[] items = line.split("\\|");
				final String term = items[0];
				final int value, exp;
				if (items[1].contains("e")) {
					final String[] n = items[1].split("e");
					value = Integer.parseInt(n[0]);
					exp = Integer.parseInt(n[1]);
				} else {
					value = Integer.parseInt(items[1]);
					exp = 0;
				}
				final PaliWord word = createNumeralPaliWord(term, value, exp, false);
				final String expStr = exp > 0 ? "e" + exp : "";
				word.addMeaning(value + expStr);
				paliNumerals.put(term, word);
				final String key = value+"e"+exp;
				final List<String> numTermList;
				if (paliCardinalMap.containsKey(key))
					numTermList = paliCardinalMap.get(key);
				else
					numTermList = new ArrayList<>();
				numTermList.add(term);
				paliCardinalMap.put(key, numTermList);
			} // end while
		} // end try
		// set up ordinal number list, only the distinct terms, the rest use calculation
		if (paliOrdinalMap.isEmpty()) {
			// fill paliOrdinalMap used for list selection
			paliOrdinalMap.put("1e0", Arrays.asList("paṭhama"));
			paliOrdinalMap.put("2e0", Arrays.asList("dutiya"));
			paliOrdinalMap.put("3e0", Arrays.asList("tatiya"));
			paliOrdinalMap.put("4e0", Arrays.asList("catuttha"));
			paliOrdinalMap.put("5e0", Arrays.asList("pañcama"));
			paliOrdinalMap.put("6e0", Arrays.asList("chaṭṭha"));
			for (int i=7; i<=10; i++) {
				final List<String> ordTermList = paliCardinalMap.get(i+"e0");
				for (final String s : ordTermList)
					paliOrdinalMap.put(i+"e0", Arrays.asList(s+"ma"));
			} // end for
			final List<String> ordList = new ArrayList<>(paliOrdinalMap.keySet());
			for (int i=0; i<ordList.size(); i++) {
				final String key = ordList.get(i);
				final String t = paliOrdinalMap.get(key).get(0);
				final int val = Integer.parseInt(key.substring(0, key.length()-2));
				final PaliWord oword = createNumeralPaliWord(t, val, 0, true);
				paliOrdinals.put(t, oword);
			} // end for
		} // end if
	}
	
	public static void createDeclPronounsMap() {
		if (!declPronounsMap.isEmpty()) return;
		loadPronounList();
		computeDeclension(declPronounsMap, paliPronouns);
	}

	public static void createDeclNumbersMap() {
		if (!declNumbersMap.isEmpty()) return;
		loadNumeralList();
		computeDeclension(declNumbersMap, paliNumerals);
	}

	public static void createDeclIrrNounsMap() {
		if (!declIrrNounsMap.isEmpty()) return;
		if (!DictUtilities.dictAvailMap.get(DictUtilities.DictBook.CPED).get()) return;
		if (paliIrrNouns.isEmpty()) {
			// load irregular nouns/adj from the database
			final String query = "SELECT TERM,POS,PARADIGM,IN_COMPOUNDS,MEANING,SUBMEANING FROM CPED WHERE PARADIGM!='' " + 
								"AND PARADIGM!='eka' AND PARADIGM!='dvi' AND PARADIGM!='ti' AND PARADIGM!='catu' " +
								"AND PARADIGM!='sabba' AND PARADIGM!='pubba' AND PARADIGM!='asuka' " +
								"AND PARADIGM NOT LIKE 'number%';";
			final java.sql.Connection conn = Utilities.H2DB.DICT.getConnection();
			try {
				if (conn != null) {
					final Statement stmt = conn.createStatement();
					final ResultSet rs = stmt.executeQuery(query);
					while (rs.next()) {
						final String term = rs.getString(1);
						final String pos = rs.getString(2);
						final String para = rs.getString(3);
						final boolean forCompounds = rs.getBoolean(4);
						final String meaning = rs.getString(5);
						final String submean = rs.getString(6);
						final PaliWord pword = new PaliWord(term);
						pword.setParadigm(para);
						pword.addPosInfo(pos);
						pword.addForCompounds(forCompounds);
						pword.addMeaning(meaning);
						pword.addSubmeaning(submean);
						paliIrrNouns.put(term, pword);
					}
					rs.close();		
					stmt.close();
				}
			} catch (SQLException e) {
				System.err.println(e);
			}
		}
		computeDeclension(declIrrNounsMap, paliIrrNouns);
	}

	private static void computeDeclension(final Map<String, DeclinedWord> outputMap, final Map<String, PaliWord> inputMap) {
		if (inputMap.isEmpty())
			return;
		outputMap.clear();
		if (declension == null)
			declension = new PaliDeclension();
		for (final PaliWord pword : inputMap.values()) {
			final List<PaliWord.Gender> glist = pword.getGender();
			for (int i = 0; i < glist.size(); i++) {
				final Map<PaliDeclension.Case, Map<PaliDeclension.Number, List<String>>> declResult = computeDeclension(pword, i);
				final PaliWord.Gender gen = glist.get(i); 
				declResult.forEach((cas, nmap) -> {
					nmap.forEach((num, lst) -> {
						for (final String t : lst) {
							final DeclinedWord dword;
							if (outputMap.containsKey(t))
								dword = outputMap.get(t);
							else
								dword = new DeclinedWord(t);
							dword.setMeaning(pword.getMeaning().get(0));
							dword.setGender(gen);
							dword.setNumber(num);
							dword.setCase(cas);
							outputMap.put(t, dword);
						}
					});
				});
			}
		}
	}

	public static Map<PaliDeclension.Case, Map<PaliDeclension.Number, List<String>>> computeDeclension(final PaliWord pword, final int genderIndex) {
		final Map<PaliDeclension.Case, Map<PaliDeclension.Number, List<String>>> result = new EnumMap<>(PaliDeclension.Case.class);
		final List<String> paraNames = pword.getParadigm();
		final NounParadigm[] paradigms = new NounParadigm[paraNames.size()];
		final PaliWord.Gender gender = pword.getGender().get(genderIndex);
		for (int i=0; i<paraNames.size(); i++) {
			paradigms[i] = declension.getNounParadigm(paraNames.get(i), pword.getEnding().get(gender), gender);
			if (paradigms[i] == null)
				paradigms[i] = declension.getNounParadigm("generic", pword.getEnding().get(gender), gender);
		}
		// loop for each case
		for (final PaliDeclension.Case cas : PaliDeclension.Case.values) {
			final Map<PaliDeclension.Number, List<String>> termMap = new EnumMap<>(PaliDeclension.Number.class);
			// loop for singular and plural
			for (final PaliDeclension.Number nu : PaliDeclension.Number.values) {
				final List<String> terms = new ArrayList<>();
				final Set<String> endingSet = new LinkedHashSet<>();
				for (final NounParadigm np : paradigms) {
					if (np != null)
						endingSet.addAll(np.getEndings(cas, nu));
				}
				final List<String> endings = new ArrayList<>(endingSet);
				if (!endings.isEmpty()) {
					for (int ind=0; ind<endings.size(); ind++) {
						terms.add(pword.withSuffix(endings.get(ind), gender));
					}
				}
				termMap.put(nu, terms);
			} // end for
			result.put(cas, termMap);
		} // end for
		return result;
	}

	public static GridPane createDeclensionGrid(final Map<PaliDeclension.Case, Map<PaliDeclension.Number, List<String>>> decMap) {
		final GridPane resultGrid = new GridPane();
		resultGrid.setHgap(2);
		resultGrid.setVgap(2);
		resultGrid.setPadding(new Insets(2, 2, 2, 2));
		final Label lblCaseHead = new Label("Case");
		lblCaseHead.setStyle("-fx-font-weight:bold");
		final Label lblSingHead = new Label(PaliDeclension.Number.SING.getName());
		lblSingHead.setStyle("-fx-font-weight:bold");
		final Label lblPluHead = new Label(PaliDeclension.Number.PLU.getName());
		lblPluHead.setStyle("-fx-font-weight:bold");
		GridPane.setConstraints(lblCaseHead, 0, 0, 2, 1);
		GridPane.setConstraints(lblSingHead, 2, 0);
		GridPane.setConstraints(lblPluHead, 3, 0);
		resultGrid.getChildren().addAll(lblCaseHead, lblSingHead, lblPluHead);
		decMap.forEach((cas, numMap) -> {
			final Label lblNum = new Label(cas.getNumAbbr());
			final Label lblCase = new Label(cas.getAbbr());
			lblCase.setMinWidth(Utilities.getRelativeSize(3));
			lblCase.setMaxWidth(Utilities.getRelativeSize(3));
			final Label lblSing = new Label(numMap.get(PaliDeclension.Number.SING).stream().collect(Collectors.joining(", ")));
			lblSing.setWrapText(true);
			lblSing.setMaxWidth(Utilities.getRelativeSize(11));
			final Label lblPlu = new Label(numMap.get(PaliDeclension.Number.PLU).stream().collect(Collectors.joining(", ")));
			lblPlu.setWrapText(true);
			lblPlu.setMaxWidth(Utilities.getRelativeSize(11));
			GridPane.setConstraints(lblNum, 0, cas.ordinal()+1, 1, 1, HPos.LEFT, VPos.TOP);
			GridPane.setConstraints(lblCase, 1, cas.ordinal()+1, 1, 1, HPos.LEFT, VPos.TOP);
			GridPane.setConstraints(lblSing, 2, cas.ordinal()+1, 1, 1, HPos.LEFT, VPos.TOP);
			GridPane.setConstraints(lblPlu, 3, cas.ordinal()+1, 1, 1, HPos.LEFT, VPos.TOP);
			resultGrid.getChildren().addAll(lblNum, lblCase, lblSing, lblPlu);
		});
		return resultGrid;
	}

	public static PaliWord createNumeralPaliWord(final String term, final int num, final int exp, final boolean isOrdinal) {
		final PaliWord pword = new PaliWord(term);
		pword.setNumericValue(num);
		pword.setExpValue(exp);
		pword.addPosInfo("numerals");
		if (term.endsWith("uttara") || term.endsWith("dhika")) {
			pword.setParadigm("generic");
			pword.addPosInfo("nt.");
		} else {
			pword.setNumeralParadigm(isOrdinal);
			pword.setNumeralGender(isOrdinal);
		}
		pword.setEnding();
		return pword;
	}	
	
	public static List<String> getPaliCardinal(final String inNum) {
		final int leng = inNum.length();
		if (leng == 0)
			return Collections.emptyList();
		final int value = Integer.parseInt(inNum);
		if (value == 0)
			return Collections.emptyList();
		// eliminate leading zeroes
		int zNum = 0;
		for (final char ch : inNum.toCharArray()) {
			if (ch == '0')
				zNum++;
			else
				break;
		}
		final String numStr = inNum.substring(zNum);
		final int len = numStr.length();
		final List<String> lowerGroup = new ArrayList<>();
		final List<String> upperGroup = new ArrayList<>();
		List<List<String>> wholeList;
		if (len >= 3) {
			wholeList = get3DigitNumeral(Integer.parseInt(numStr.substring(len-3)), false);
			for (final List<String> l : wholeList)
				lowerGroup.add(l.get(0));
			if (len > 3) {
				wholeList = get3DigitNumeral(Integer.parseInt(numStr.substring(0, len-3)), false);
				for (final List<String> l : wholeList)
					upperGroup.add(l.get(0));
			}
		} else {
			wholeList = get3DigitNumeral(Integer.parseInt(numStr), false);
			for (final List<String> l : wholeList)
				lowerGroup.add(l.get(0));
		}
		final List<String> terms;
		if (!upperGroup.isEmpty()) {
			terms = new ArrayList<>();
			for (final String s : upperGroup) {
				String w = "sahassa";
				if (!lowerGroup.isEmpty()) {
					for (final String f : lowerGroup) {
						if (!s.endsWith("ā") && !s.endsWith("ṃ")) {
							if (!s.equals("eka"))
								w = s + w;
							terms.add(PaliWord.sandhi(f, "adhika") + w);
						}
					}
				} else {
					if (!s.endsWith("ā") && !s.endsWith("ṃ")) {
						if (!s.equals("eka"))
							w = s + w;
						terms.add(w);
					}
				}
			} // end for
		} else {
			terms = lowerGroup;
		} // end if
		return terms;
	}
	
	private static List<List<String>> get3DigitNumeral(final int inNum, final boolean split) {
		final List<List<String>> result = new ArrayList<>();
		final int value;
		if (inNum == 0)
			return result;
		else if (inNum >= 1000)
			value = inNum % 1000;
		else
			value = inNum;
		int lowNum = value;
		int hunNum = 0;
		if (value >= 100) {
			hunNum = value/100;
			lowNum = value%100;
		}
		final List<String> lowerList;
		if (lowNum > 0)
			lowerList = get2DigitNumeral(lowNum);
		else
			lowerList = new ArrayList<>();
		if (hunNum > 0) {
			final List<String> part1;
			final List<String> part2 = paliCardinalMap.get("100e0");
			final List<String> upperList = new ArrayList<>();		
			if (hunNum > 1) {
				part1 = paliCardinalMap.get(hunNum+"e0");
			} else {
				part1 = new ArrayList<>();
			}
			if (!lowerList.isEmpty()) {
				// there are all digits
				for (final String s : part2) {
					if (!part1.isEmpty()) {
						for (final String f : part1) {
							if (!f.endsWith("ā") && !f.endsWith("ṃ"))
								upperList.add(PaliWord.sandhi(f, s));
						}
					} else {
						upperList.add(s);
					}
				} // end for
				for (final String s : upperList) {
					if (!lowerList.isEmpty()) {
						for (final String f : lowerList) {
							if (!f.endsWith("ā") && !f.endsWith("ṃ")) {
								final List<String> composition = new ArrayList<>(2);
								if (split) {
									composition.add(f);
									composition.add(s);
								} else {
									composition.add(PaliWord.sandhi(f, "uttara") + s);
								}
								result.add(composition);
							}
						}
					}
				} // endfor
				// consider x5x the addha
				final List<List<String>> adhhaList = get3DigitAddha(value, false);
				for (final List<String> ls : adhhaList) {
					final List<String> composition = new ArrayList<>(1);
					composition.add(ls.get(0));
					result.add(composition);
				}
			} else {
				// only the highest digit, the rest are zeros
				for (final String s : part2) {
					if (!part1.isEmpty()) {
						for (final String f : part1) {
							final List<String> composition = new ArrayList<>(2);
							if (split) {
								composition.add(f);
								composition.add(s);
								result.add(composition);
							} else {
								if (!f.endsWith("ā") && !f.endsWith("ṃ")) {
									composition.add(PaliWord.sandhi(f, s));
									result.add(composition);
								}
							}
						} // end for
					} else {
						final List<String> composition = new ArrayList<>(2);
						composition.add(s);
						result.add(composition);
					}
				} // end for
			}
		} else {
			// only 2 digits
			for (final String s : lowerList) {
				final List<String> composition = new ArrayList<>(2);
				composition.add(s);
				result.add(composition);
			}
		}
		return result;
	}
	
	private static List<String> get2DigitNumeral(final int value) {
		final List<String> result;
		final List<String> termList = paliCardinalMap.get(value+"e0");
		if (termList == null) {
			// not found in the list, calculate it
			result = new ArrayList<>();
			final List<String> part1;
			final List<String> part2;
			String key;
			if (value % 10 == 9) {
				// end with nine
				part1 = new ArrayList<>();
				part1.add("ekūna");
				part1.add("ūna");
				key = (value+1) + "e0";
				part2 = paliCardinalMap.get(key);
			} else {
				key = (value%10) + "e0";
				part1 = paliCardinalMap.get(key);
				key = ((value/10)*10) + "e0";
				part2 = paliCardinalMap.get(key);
			} // end if
			for (final String f : part1) {
				for (final String s : part2)
					result.add(PaliWord.sandhi(f, s));
			}// end for	
		} else {
			result = termList;
		} // end if
		return result;
	}
	
	private static List<List<String>> get3DigitAddha(final int value, final boolean split) {
		final List<List<String>> result = new ArrayList<>();
		if (value > 860)
			return result;
		final int hunNum = value/100;
		if (hunNum == 0)
			return result;
		final String[] addhas = { "diyaḍḍha", "aḍḍhateyya", "aḍḍhuḍḍha" };
		final int num = value-hunNum*100;
		if (num/10 == 5) {
			final List<String> part1 = paliCardinalMap.get((num%10)+"e0");
			final List<String> part2;		
			if (hunNum >= 1 && hunNum <=3) {
				part2 = Arrays.asList(addhas[hunNum-1]);
			} else {
				part2 = new ArrayList<>();
				for (final String t : paliOrdinalMap.get((hunNum+1)+"e0"))
					part2.add("aḍḍha" + t);
			} // end if
			for (final String s : part2) {
				if (part1 == null) {
					final List<String> tmpList = new ArrayList<>(1);
					tmpList.add(s + "sata");
					result.add(tmpList);
				} else {
					for (final String f : part1) {
						if (f.endsWith("e") || f.endsWith("ā") || f.endsWith("ṃ"))
							continue;
						final List<String> tmpList = new ArrayList<>(2);
						final String ut = PaliWord.sandhi(f, "uttara");
						if (split) {
							tmpList.add(ut);
							tmpList.add(s + "sata");
						} else {
							tmpList.add(PaliWord.sandhi(ut, s) + "sata");
						}
						result.add(tmpList);
					}
				}
			} // end for
		} // end if
		return result;
	}

}

