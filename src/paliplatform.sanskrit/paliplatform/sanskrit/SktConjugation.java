/*
 * SktConjugation.java
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

import static paliplatform.sanskrit.SanskritUtilities.VocalicGrade;

import java.util.*;
import java.util.stream.*;

/** 
 * The class handling Sanskrit verbal conjugation.
 * @author J.R. Bhaddacak
 * @version 4.1
 * @since 4.1
 */
public class SktConjugation {
	public static enum Number {
		SING("Singular"), DUAL("Dual"), PLU("Plural");
		public static final Number[] values = values();
		private static final String[] devaNames = { "एकवचनम्", "द्विवचनम्", "बहुवचनम्" };
		private final String name;
		private Number(final String n) {
			name = n;
		}
		public String getName() {
			return name;
		}
		public String getDevaName() {
			return devaNames[this.ordinal()];
		}
	}
	public static enum Person {
		PRATHAMA, MADHYAMA, UTTAMA;
		public static final Person[] values = values();
		private final String name;
		private final String[] abbrs = { "3rd", "2nd", "1st" };
		private final String[] desc = { "3rd person (prathama)", "2nd person (madhyama)", "1st person (uttama)" };
		private static final String[] devaNames = { "प्रथम", "मध्यम", "उत्तम" };
		private static final String devaInits = "३२१";
		private Person() {
			final String n = this.toString();
			name = n.charAt(0) + n.substring(1).toLowerCase() + " (" + getAbbr() + ")";
		}
		public String getName() {
			return name;
		}
		public String getDevaName() {
			return devaNames[this.ordinal()];
		}
		public String getInitial() {
			return getAbbr().charAt(0) + "";
		}
		public String getDevaInitial() {
			return devaInits.charAt(this.ordinal()) + "";
		}
		public String getAbbr() {
			return abbrs[this.ordinal()];
		}
		public String getDescription() {
			return desc[this.ordinal()];
		}
	}
	public static enum Pada {
		ACT("Active"), MID("Middle");
		public static final Pada[] values = values();
		private static final String[] sktNames = { "Parasmaipada", "Ātmanepada" };
		private final String name;
		private Pada(final String n) {
			name = n;
		}
		public String getName() {
			return name;
		}
		public String getSktName() {
			return sktNames[this.ordinal()];
		}
	}
	public static enum TenseMood {
		PRES, IMP, OPT, IMPERF, PERF, AOR, PREC, PERI, FUT, COND;
		public static final TenseMood[] values = values();
		private static final String[] names = {
			"Present", "Imperative", "Optative", "Imperfect", "Perfect",
			"Aorist", "Precative", "Periphrastic Future", "Future", "Conditional" };
		private static final String[] fullNames = {
			"Present Indicative", "Present Imperative", "Present Optative", "Imperfect", "Perfect",
			"Aorist", "Precative (Aorist Optative)", "Periphrastic Future", "Simple Future", "Conditional" };
		private static final String[] abbrs = { 
			"Pres. Ind.", "Pres. Imp.", "Pres. Opt.", "Imperf.", "Perf.",
			"Aor.", "Prec.", "Per. Fut.", "Fut.", "Cond." };
		private static final String[] sktNames = {
			"vartamānā", "pañcamī", "saptamī", "anadyatanī", "parokṣā",
			"adyatanī", "saptamī (aorist)", "śvastanī", "bhaviṣyantī", "kālātipatti" };
		private static final String[] lakaras = {
			"laṭ", "loṭ", "vidhiliṅ", "laṅ", "liṭ",
			"luṅ", "āśīrliṅ", "luṭ", "lṛṭ", "lṛṅ" };
		public static List<String> getNameList() {
			return Arrays.asList(fullNames);
		}
		public String getName() {
			return names[this.ordinal()];
		}
		public String getNameCut() {
			return names[this.ordinal()].substring(0, 4);
		}
		public String getFullName() {
			return fullNames[this.ordinal()];
		}
		public String getAbbr() {
			return abbrs[this.ordinal()];
		}
		public String getLakara() {
			return lakaras[this.ordinal()];
		}
		@Override
		public String toString() {
			return fullNames[this.ordinal()];
		}
	}
	public static final Map<String, VerbalParadigm> paradigmMap = new LinkedHashMap<>();

	private SktConjugation() {
	}

	public static Map<Person, Map<Number, List<String>>> compute(final String word, final String paradName) {
		final VerbalParadigm parad = paradigmMap.get(paradName);
		if (parad == null)
			return Collections.emptyMap();
		final Map<Person, Map<Number, List<String>>> result = new EnumMap<>(Person.class);
		for (final Person p : Person.values) {
			final Map<Number, List<String>> numMap = new EnumMap<>(Number.class);
			for (final Number n : Number.values) {
				final List<String> productList = parad.getConjugation(word, p, n);
				numMap.put(n, productList);
			}
			result.put(p, numMap);
		}
		return result;
	}

	public static Map<Person, Map<Number, List<String>>> compute(final String word, final String paradName, final String preTerm) {
		final VerbalParadigm parad = paradigmMap.get(paradName);
		if (parad == null)
			return Collections.emptyMap();
		final Map<Person, Map<Number, List<String>>> result = new EnumMap<>(Person.class);
		for (final Person p : Person.values) {
			final Map<Number, List<String>> numMap = new EnumMap<>(Number.class);
			for (final Number n : Number.values) {
				final List<String> productList = parad.getConjugation(word, p, n).stream()
													.map(x -> preTerm + " " + x)
													.collect(Collectors.toList());
				numMap.put(n, productList);
			}
			result.put(p, numMap);
		}
		return result;
	}

	public static Map<Person, Map<Number, List<String>>> computePerfect(final String word, final String paradName, final List<String> replacement) {
		final VerbalParadigm parad = paradigmMap.get(paradName);
		final List<String> tobeReplaced = parad.getPerfectSubstitution();
		final Map<String, String> replaceMap = new HashMap<>();
		for (int i = 0; i < tobeReplaced.size(); i++) {
			if (i < replacement.size())
				replaceMap.put(tobeReplaced.get(i), replacement.get(i));
		}
		if (parad == null)
			return Collections.emptyMap();
		final Map<Person, Map<Number, List<String>>> result = new EnumMap<>(Person.class);
		for (final Person p : Person.values) {
			final Map<Number, List<String>> numMap = new EnumMap<>(Number.class);
			for (final Number n : Number.values) {
				final List<String> productList = parad.getConjugation(word, p, n, replaceMap);
				numMap.put(n, productList);
			}
			result.put(p, numMap);
		}
		return result;
	}

	public static Map<Person, Map<Number, List<String>>> productCombine(final Map<Person, Map<Number, List<String>>> prod1,
																		final Map<Person, Map<Number, List<String>>> prod2) {
		final Map<Person, Map<Number, List<String>>> result = new EnumMap<>(Person.class);
		for (final Person p : Person.values) {
			final Map<Number, List<String>> numMap = new EnumMap<>(Number.class);
			for (final Number n : Number.values) {
				final List<String> productList = new ArrayList<>();
				productList.addAll(prod1.get(p).get(n));
				productList.addAll(prod2.get(p).get(n));
				numMap.put(n, productList);
			}
			result.put(p, numMap);
		}
		return result;
	}

	public static String getParadigmName(final String citeForm, final TenseMood tense, final Pada pad) {
		String result = "";
		if (citeForm.isEmpty()) return result;
		final String end1 = citeForm.length() >= 1 ? citeForm.substring(citeForm.length() - 1) : citeForm;
		final String end2 = citeForm.length() >= 2 ? citeForm.substring(citeForm.length() - 2) : citeForm;
		final String end3 = citeForm.length() >= 3 ? citeForm.substring(citeForm.length() - 3) : citeForm;
		final String end4 = citeForm.length() >= 4 ? citeForm.substring(citeForm.length() - 4) : citeForm;
		final String end5 = citeForm.length() >= 5 ? citeForm.substring(citeForm.length() - 5) : citeForm;
		final String end6 = citeForm.length() >= 6 ? citeForm.substring(citeForm.length() - 6) : citeForm;
		if (tense.ordinal() <= 3) {
			// PRES, IMP, OPT, IMPERF
			final List<String> irregularList = List.of("atti", "asti", "āste", "eti");
			final Map<String, String> sampleMap = new HashMap<>();
			sampleMap.put("ati", "nayati");
			sampleMap.put("āti", "bhāti");
			sampleMap.put("iti", "svapiti");
			sampleMap.put("auti", "stauti");
			sampleMap.put("nāti", "jānāti");
			sampleMap.put("noti", "sunoti");
			sampleMap.put("pnoti", "āpnoti");
			sampleMap.put("oti", "juhoti");
			sampleMap.put("kti", "yunakti");
			sampleMap.put("ate", "nayati");
			sampleMap.put("ate", "nayati");
			sampleMap.put("ute", "sunoti");
			final String sample = irregularList.contains(citeForm)
				? citeForm
				: sampleMap.getOrDefault(end5, sampleMap.getOrDefault(end4, sampleMap.get(end3)));
			if (sample != null) {
				result = paradigmMap.values().stream()
					.filter(x -> sample.equals(x.getSampleTerm()))
					.filter(x -> x.getTenseMood() == tense && x.getPada() == pad)
					.map(x -> x.getName())
					.findFirst()
					.orElse("");
			}
		} else if(tense == TenseMood.AOR) {
			final VerbalParadigm abhasitParad = paradigmMap.get("abhāsīt");
			if (citeForm.equals("akārṣīt")) {
				result = pad == Pada.ACT ? citeForm : "akṛta";
			} else if (citeForm.equals("abhūt")) {
				result = pad == Pada.ACT ? citeForm : "";
			} else if (citeForm.equals("alabdha")) {
				result = pad == Pada.MID ? citeForm : "";
			} else if (abhasitParad.hasInWordList(citeForm)) {
				result = pad == Pada.ACT ? "abhāsīt" : "";
			} else {
				final Map<String, String> sampleMap = new HashMap<>();
				sampleMap.put("at", "asicat");
				sampleMap.put("ṣat", "adikṣat");
				sampleMap.put("aiṣīt", "anaiṣīt");
				sampleMap.put("auṣīt", "asauṣīt");
				sampleMap.put("āsīt", "ajñāsīt");
				sampleMap.put("aipsīt", "akṣaipsīt");
				sampleMap.put("aukṣīt", "ayaukṣīt");
				sampleMap.put("kṣīt", "ayokṣīt");
				sampleMap.put("īt", "apāvīt");
				sampleMap.put("āt", "adāt");
				final String sample = sampleMap.getOrDefault(end6,
										sampleMap.getOrDefault(end5,
											sampleMap.getOrDefault(end4,
												sampleMap.getOrDefault(end3,
													sampleMap.getOrDefault(end2,
														sampleMap.get(end1))))));
				if (sample != null) {
					result = paradigmMap.values().stream()
						.filter(x -> sample.equals(x.getSampleTerm()))
						.filter(x -> x.getTenseMood() == tense && x.getPada() == pad)
						.map(x -> x.getName())
						.findFirst()
						.orElse("");
				}
			}
		} else if(tense == TenseMood.PREC) {
			result = pad == Pada.ACT ? "nīyāt" : "neṣīṣṭa";
		} else if(tense == TenseMood.PERI) {
			result = pad == Pada.ACT ? "netā" : "netāM";
		}
		return result;
	}

	// static initialization of paradigms
	static {
		// present, optative, imperative, imperfect
		// -ati group
		final String[][] nayatiData = {
			{ "ati", "ataḥ", "anti" },
			{ "asi", "athaḥ", "atha" },
			{ "āmi", "āvaḥ", "āmaḥ" } };
		final VerbalParadigm nayatiParad = VerbalParadigm.generate("nayati", "nayati", TenseMood.PRES, Pada.ACT, nayatiData, "16-1");
		paradigmMap.put("nayati", nayatiParad);
		final String[][] nayetData = {
			{ "et", "etām", "eyuḥ" },
			{ "eḥ", "etam", "eta" },
			{ "eyam", "eva", "ema" } };
		final VerbalParadigm nayetParad = VerbalParadigm.generate("nayet", "nayati", TenseMood.OPT, Pada.ACT, nayetData, "16-1");
		paradigmMap.put("nayet", nayetParad);
		final String[][] nayatuData = {
			{ "atu", "atām", "antu" },
			{ "a", "atam", "ata" },
			{ "āni", "āva", "āma" } };
		final VerbalParadigm nayatuParad = VerbalParadigm.generate("nayatu", "nayati", TenseMood.IMP, Pada.ACT, nayatuData, "16-1");
		paradigmMap.put("nayatu", nayatuParad);
		final String[][] anayatData = {
			{ "at", "atām", "an" },
			{ "aḥ", "atam", "ata" },
			{ "am", "āva", "āma" } };
		final VerbalParadigm anayatParad = VerbalParadigm.generate("anayat", "nayati", TenseMood.IMPERF, Pada.ACT, anayatData, "16-1");
		anayatParad.setAugment(true);
		paradigmMap.put("anayat", anayatParad);
		final String[][] nayateData = {
			{ "ate", "ete", "ante" },
			{ "ase", "ethe", "adhve" },
			{ "e", "āvahe", "āmahe" } };
		final VerbalParadigm nayateParad = VerbalParadigm.generate("nayate", "nayati", TenseMood.PRES, Pada.MID, nayateData, "16-1");
		paradigmMap.put("nayate", nayateParad);
		final String[][] nayetaData = {
			{ "eta", "eyātām", "eran" },
			{ "ethāḥ", "eyāthām", "edhvam" },
			{ "eya", "evahi", "emahi" } };
		final VerbalParadigm nayetaParad = VerbalParadigm.generate("nayeta", "nayati", TenseMood.OPT, Pada.MID, nayetaData, "16-1");
		paradigmMap.put("nayeta", nayetaParad);
		final String[][] nayatamData = {
			{ "atām", "etām", "antām" },
			{ "asva", "ethām", "adhvam" },
			{ "ai", "āvahai", "āmahai" } };
		final VerbalParadigm nayatamParad = VerbalParadigm.generate("nayatām", "nayati", TenseMood.IMP, Pada.MID, nayatamData, "16-1");
		paradigmMap.put("nayatām", nayatamParad);
		final String[][] anayataData = {
			{ "ata", "etām", "anta" },
			{ "athāḥ", "ethām", "adhvam" },
			{ "e", "āvahi", "āmahi" } };
		final VerbalParadigm anayataParad = VerbalParadigm.generate("anayata", "nayati", TenseMood.IMPERF, Pada.MID, anayataData, "16-1");
		anayataParad.setAugment(true);
		paradigmMap.put("anayata", anayataParad);

		// -āti group
		final List<String> bhatiWordList = List.of("khyāti", "pāti", "bhāti", "yāti", "snāti");
		final String[][] bhatiData = {
			{ "ti", "taḥ", "nti" },
			{ "si", "thaḥ", "tha" },
			{ "mi", "vaḥ", "maḥ" } };
		final VerbalParadigm bhatiParad = VerbalParadigm.generate("bhāti", "bhāti", TenseMood.PRES, Pada.ACT, bhatiData, "16-2");
		bhatiParad.setStemCutFactor(2);
		bhatiParad.addWordList(bhatiWordList);
		paradigmMap.put("bhāti", bhatiParad);
		final String[][] bhayatData = {
			{ "yāt", "yātām", "yuḥ" },
			{ "yāḥ", "yātam", "yāta" },
			{ "yām", "yāva", "yāma" } };
		final VerbalParadigm bhayatParad = VerbalParadigm.generate("bhāyāt", "bhāti", TenseMood.OPT, Pada.ACT, bhayatData, "16-2");
		bhayatParad.setStemCutFactor(2);
		bhayatParad.addWordList(bhatiWordList);
		paradigmMap.put("bhāyāt", bhayatParad);
		final String[][] bhatuData = {
			{ "tu", "tām", "ntu" },
			{ "hi", "tam", "ta" },
			{ "ni", "va", "ma" } };
		final VerbalParadigm bhatuParad = VerbalParadigm.generate("bhātu", "bhāti", TenseMood.IMP, Pada.ACT, bhatuData, "16-2");
		bhatuParad.setStemCutFactor(2);
		bhatuParad.addWordList(bhatiWordList);
		paradigmMap.put("bhātu", bhatuParad);
		final String[][] abhatData = {
			{ "āt", "ātām", "ān" },
			{ "āḥ", "ātam", "āta" },
			{ "ām", "āva", "āma" } };
		final VerbalParadigm abhatParad = VerbalParadigm.generate("abhāt", "bhāti", TenseMood.IMPERF, Pada.ACT, abhatData, "16-2");
		abhatParad.setAugment(true);
		abhatParad.addWordList(bhatiWordList);
		abhatParad.addEndings(Person.PRATHAMA, Number.PLU, "uḥ");
		paradigmMap.put("abhāt", abhatParad);

		// -iti group
		final List<String> svapitiWordList = List.of("aniti", "jakṣiti", "śvasiti", "svapiti");
		final String[][] svapitiData = {
			{ "iti", "itaḥ", "anti" },
			{ "iṣi", "ithaḥ", "itha" },
			{ "imi", "ivaḥ", "imaḥ" } };
		final VerbalParadigm svapitiParad = VerbalParadigm.generate("svapiti", "svapiti", TenseMood.PRES, Pada.ACT, svapitiData, "16-3");
		svapitiParad.addWordList(svapitiWordList);
		paradigmMap.put("svapiti", svapitiParad);
		final String[][] svapyatData = {
			{ "yāt", "yātām", "yuḥ" },
			{ "yāḥ", "yātam", "yāta" },
			{ "yām", "yāva", "yāma" } };
		final VerbalParadigm svapyatParad = VerbalParadigm.generate("svapyāt", "svapiti", TenseMood.OPT, Pada.ACT, svapyatData, "16-3");
		svapyatParad.addWordList(svapitiWordList);
		paradigmMap.put("svapyāt", svapyatParad);
		final String[][] svapituData = {
			{ "itu", "itām", "antu" },
			{ "ihi", "itam", "ita" },
			{ "āni", "āva", "āma" } };
		final VerbalParadigm svapituParad = VerbalParadigm.generate("svapitu", "svapiti", TenseMood.IMP, Pada.ACT, svapituData, "16-3");
		svapituParad.addWordList(svapitiWordList);
		paradigmMap.put("svapitu", svapituParad);
		final String[][] asvapatData = {
			{ "at", "itām", "an" },
			{ "aḥ", "itam", "ita" },
			{ "am", "iva", "ima" } };
		final VerbalParadigm asvapatParad = VerbalParadigm.generate("asvapat", "svapiti", TenseMood.IMPERF, Pada.ACT, asvapatData, "16-3");
		asvapatParad.setAugment(true);
		asvapatParad.addWordList(svapitiWordList);
		asvapatParad.addEndings(Person.PRATHAMA, Number.SING, "īt");
		asvapatParad.addEndings(Person.MADHYAMA, Number.SING, "īḥ");
		paradigmMap.put("asvapat", asvapatParad);

		// -auti group
		final List<String> stautiWordList = List.of("kauti", "tauti", "rauti", "stauti");
		final String[][] stautiData = {
			{ "auti", "utaḥ", "uvanti" },
			{ "auṣi", "uthaḥ", "utha" },
			{ "aumi", "uvaḥ", "umaḥ" } };
		final VerbalParadigm stautiParad = VerbalParadigm.generate("stauti", "stauti", TenseMood.PRES, Pada.ACT, stautiData, "16-4");
		stautiParad.setStemCutFactor(4);
		stautiParad.addWordList(stautiWordList);
		stautiParad.addEndings(Person.PRATHAMA, Number.SING, "avīti");
		stautiParad.addEndings(Person.MADHYAMA, Number.SING, "avīṣi");
		stautiParad.addEndings(Person.UTTAMA, Number.SING, "avīmi");
		paradigmMap.put("stauti", stautiParad);
		final String[][] stuyatData = {
			{ "uyāt", "uyātām", "uyuḥ" },
			{ "uyāḥ", "uyātam", "uyāta" },
			{ "uyām", "uyāva", "uyāma" } };
		final VerbalParadigm stuyatParad = VerbalParadigm.generate("stuyāt", "stauti", TenseMood.OPT, Pada.ACT, stuyatData, "16-4");
		stuyatParad.setStemCutFactor(4);
		stuyatParad.addWordList(stautiWordList);
		paradigmMap.put("stuyāt", stuyatParad);
		final String[][] stautuData = {
			{ "autu", "utām", "uvantu" },
			{ "uhi", "utam", "uta" },
			{ "avāni", "avāva", "avāma" } };
		final VerbalParadigm stautuParad = VerbalParadigm.generate("stautu", "stauti", TenseMood.IMP, Pada.ACT, stautuData, "16-4");
		stautuParad.setStemCutFactor(4);
		stautuParad.addWordList(stautiWordList);
		stautuParad.addEndings(Person.PRATHAMA, Number.SING, "avītu");
		paradigmMap.put("stautu", stautuParad);
		final String[][] astautData = {
			{ "aut", "utām", "uvan" },
			{ "auḥ", "utam", "uta" },
			{ "āvam", "uva", "uma" } };
		final VerbalParadigm astautParad = VerbalParadigm.generate("astaut", "stauti", TenseMood.IMPERF, Pada.ACT, astautData, "16-4");
		astautParad.setStemCutFactor(4);
		astautParad.addWordList(stautiWordList);
		astautParad.setAugment(true);
		astautParad.addEndings(Person.PRATHAMA, Number.SING, "avīt");
		astautParad.addEndings(Person.MADHYAMA, Number.SING, "avīḥ");
		paradigmMap.put("astaut", astautParad);
		final String[][] stuteData = {
			{ "ute", "uvāte", "uvate" },
			{ "uṣe", "uvāthe", "udhve" },
			{ "uve", "uvahe", "umahe" } };
		final VerbalParadigm stuteParad = VerbalParadigm.generate("stute", "stauti", TenseMood.PRES, Pada.MID, stuteData, "16-4");
		stuteParad.setStemCutFactor(4);
		stuteParad.addWordList(stautiWordList);
		paradigmMap.put("stute", stuteParad);
		final String[][] stuvitaData = {
			{ "uvīta", "uvīyātām", "uvīran" },
			{ "uvīthāḥ", "uvīyāthām", "uvīdhvam" },
			{ "uvīya", "uvīvahi", "uvīmahi" } };
		final VerbalParadigm stuvitaParad = VerbalParadigm.generate("stuvīta", "stauti", TenseMood.OPT, Pada.MID, stuvitaData, "16-4");
		stuvitaParad.setStemCutFactor(4);
		stuvitaParad.addWordList(stautiWordList);
		paradigmMap.put("stuvīta", stuvitaParad);
		final String[][] stutamData = {
			{ "utām", "uvātām", "uvatām" },
			{ "uṣva", "uvāthām", "udhvam" },
			{ "avai", "avāvahai", "avāmahai" } };
		final VerbalParadigm stutamParad = VerbalParadigm.generate("stutām", "stauti", TenseMood.IMP, Pada.MID, stutamData, "16-4");
		stutamParad.setStemCutFactor(4);
		stutamParad.addWordList(stautiWordList);
		paradigmMap.put("stutām", stutamParad);
		final String[][] astutaData = {
			{ "uta", "uvātām", "uvata" },
			{ "uthāḥ", "uvāthām", "udhvam" },
			{ "uvi", "uvahi", "umahi" } };
		final VerbalParadigm astutaParad = VerbalParadigm.generate("astuta", "stauti", TenseMood.IMPERF, Pada.MID, astutaData, "16-4");
		astutaParad.setStemCutFactor(4);
		astutaParad.addWordList(stautiWordList);
		astutaParad.setAugment(true);
		paradigmMap.put("astuta", astutaParad);

		// -nāti group
		final String[][] janatiData = {
			{ "āti", "ītaḥ", "anti" },
			{ "āsi", "īthaḥ", "ītha" },
			{ "āmi", "īvaḥ", "īmaḥ" } };
		final VerbalParadigm janatiParad = VerbalParadigm.generate("jānāti", "jānāti", TenseMood.PRES, Pada.ACT, janatiData, "16-5");
		paradigmMap.put("jānāti", janatiParad);
		final String[][] janiyatData = {
			{ "īyāt", "īyātām", "īyuḥ" },
			{ "īyāḥ", "īyātam", "īyāta" },
			{ "īyām", "īyāva", "īyāma" } };
		final VerbalParadigm janiyatParad = VerbalParadigm.generate("jānīyāt", "jānāti", TenseMood.OPT, Pada.ACT, janiyatData, "16-5");
		paradigmMap.put("jānīyāt", janiyatParad);
		final String[][] janatuData = {
			{ "ātu", "ītām", "antu" },
			{ "īhi", "ītam", "īta" },
			{ "āni", "āva", "āma" } };
		final VerbalParadigm janatuParad = VerbalParadigm.generate("jānātu", "jānāti", TenseMood.IMP, Pada.ACT, janatuData, "16-5");
		paradigmMap.put("jānātu", janatuParad);
		// one special case -Cnāti
		final String[][] grathnatuData = {
			{ "nātu", "nītām", "nantu" },
			{ "āna", "nītam", "nīta" },
			{ "nāni", "nāva", "nāma" } };
		final VerbalParadigm grathnatuParad = VerbalParadigm.generate("grathnātu", "grathnāti", TenseMood.IMP, Pada.ACT, grathnatuData, "16-5");
		grathnatuParad.setStemCutFactor(4);
		paradigmMap.put("grathnātu", grathnatuParad);
		final String[][] ajanatData = {
			{ "āt", "ītām", "an" },
			{ "āḥ", "ītam", "īta" },
			{ "ām", "īva", "īma" } };
		final VerbalParadigm ajanatParad = VerbalParadigm.generate("ajānāt", "jānāti", TenseMood.IMPERF, Pada.ACT, ajanatData, "16-5");
		ajanatParad.setAugment(true);
		paradigmMap.put("ajānāt", ajanatParad);
		final String[][] janiteData = {
			{ "īte", "āte", "ate" },
			{ "īṣe", "āthe", "īdhve" },
			{ "e", "īvahe", "īmahe" } };
		final VerbalParadigm janiteParad = VerbalParadigm.generate("jānīte", "jānāti", TenseMood.PRES, Pada.MID, janiteData, "16-5");
		paradigmMap.put("jānīte", janiteParad);
		final String[][] janitaData = {
			{ "īta", "īyātām", "īran" },
			{ "īthāḥ", "īyāthām", "īdhvam" },
			{ "īya", "īvahi", "īmahi" } };
		final VerbalParadigm janitaParad = VerbalParadigm.generate("jānīta", "jānāti", TenseMood.OPT, Pada.MID, janitaData, "16-5");
		paradigmMap.put("jānīta", janitaParad);
		final String[][] janitamData = {
			{ "ītām", "ātām", "atām" },
			{ "īṣva", "āthām", "īdhvam" },
			{ "ai", "āvahai", "āmahai" } };
		final VerbalParadigm janitamParad = VerbalParadigm.generate("jānītām", "jānāti", TenseMood.IMP, Pada.MID, janitamData, "16-5");
		paradigmMap.put("jānītām", janitamParad);
		final String[][] ajanitaData = {
			{ "īta", "ātām", "ata" },
			{ "īthāḥ", "āthām", "īdhvam" },
			{ "i", "īvahi", "īmahi" } };
		final VerbalParadigm ajanitaParad = VerbalParadigm.generate("ajānīta", "jānāti", TenseMood.IMPERF, Pada.MID, ajanitaData, "16-5");
		ajanitaParad.setAugment(true);
		paradigmMap.put("ajānīta", ajanitaParad);

		// -noti group
		final String[][] sunotiData = {
			{ "oti", "utaḥ", "vanti" },
			{ "oṣi", "uthaḥ", "utha" },
			{ "omi", "uvaḥ", "umaḥ" } };
		final VerbalParadigm sunotiParad = VerbalParadigm.generate("sunoti", "sunoti", TenseMood.PRES, Pada.ACT, sunotiData, "16-6");
		final VerbalParadigm apnotiParad = VerbalParadigm.duplicate(sunotiParad, "āpnoti", "āpnoti");
		sunotiParad.addEndings(Person.UTTAMA, Number.DUAL, "vaḥ");
		sunotiParad.addEndings(Person.UTTAMA, Number.PLU, "maḥ");
		paradigmMap.put("sunoti", sunotiParad);
		apnotiParad.setEndings(Person.PRATHAMA, Number.PLU, List.of("uvanti"));
		paradigmMap.put("āpnoti", apnotiParad);
		final String[][] sunuyatData = {
			{ "uyāt", "uyātām", "uyuḥ" },
			{ "uyāḥ", "uyātam", "uyāta" },
			{ "uyām", "uyāva", "uyāma" } };
		final VerbalParadigm sunuyatParad = VerbalParadigm.generate("sunuyāt", "sunoti", TenseMood.OPT, Pada.ACT, sunuyatData, "16-6");
		final VerbalParadigm apnuyatParad = VerbalParadigm.duplicate(sunuyatParad, "āpnuyāt", "āpnoti");
		paradigmMap.put("sunuyāt", sunuyatParad);
		paradigmMap.put("āpnuyāt", apnuyatParad);
		final String[][] sunotuData = {
			{ "otu", "utām", "vantu" },
			{ "u", "utam", "uta" },
			{ "avāni", "avāva", "avāma" } };
		final VerbalParadigm sunotuParad = VerbalParadigm.generate("sunotu", "sunoti", TenseMood.IMP, Pada.ACT, sunotuData, "16-6");
		final VerbalParadigm apnotuParad = VerbalParadigm.duplicate(sunotuParad, "āpnotu", "āpnoti");
		paradigmMap.put("sunotu", sunotuParad);
		apnotiParad.setEndings(Person.PRATHAMA, Number.PLU, List.of("uvantu"));
		apnotuParad.setEndings(Person.MADHYAMA, Number.SING, List.of("uhi"));
		paradigmMap.put("āpnotu", apnotuParad);
		final String[][] asunotData = {
			{ "ot", "utām", "van" },
			{ "oḥ", "utam", "uta" },
			{ "avam", "uva", "uma" } };
		final VerbalParadigm asunotParad = VerbalParadigm.generate("asunot", "sunoti", TenseMood.IMPERF, Pada.ACT, asunotData, "16-6");
		asunotParad.setAugment(true);
		final VerbalParadigm apnotParad = VerbalParadigm.duplicate(asunotParad, "āpnot", "āpnoti");
		asunotParad.addEndings(Person.UTTAMA, Number.DUAL, "va");
		asunotParad.addEndings(Person.UTTAMA, Number.PLU, "ma");
		paradigmMap.put("asunot", asunotParad);
		apnotParad.setEndings(Person.PRATHAMA, Number.PLU, List.of("uvan"));
		paradigmMap.put("āpnot", apnotParad);
		final String[][] sunuteData = {
			{ "ute", "vāte", "vate" },
			{ "uṣe", "vāthe", "udhve" },
			{ "ve", "uvahe", "umahe" } };
		final VerbalParadigm sunuteParad = VerbalParadigm.generate("sunute", "sunoti", TenseMood.PRES, Pada.MID, sunuteData, "16-6");
		final VerbalParadigm apnuteParad = VerbalParadigm.duplicate(sunuteParad, "āpnute", "āpnoti");
		sunuteParad.addEndings(Person.UTTAMA, Number.DUAL, "vahe");
		sunuteParad.addEndings(Person.UTTAMA, Number.PLU, "mahe");
		paradigmMap.put("sunute", sunuteParad);
		apnuteParad.setEndings(Person.PRATHAMA, Number.DUAL, List.of("uvāte"));
		apnuteParad.setEndings(Person.PRATHAMA, Number.PLU, List.of("uvate"));
		apnuteParad.setEndings(Person.MADHYAMA, Number.DUAL, List.of("uvāthe"));
		apnuteParad.setEndings(Person.UTTAMA, Number.SING, List.of("uve"));
		paradigmMap.put("āpnute", apnuteParad);
		final String[][] sunvitaData = {
			{ "vīta", "vīyātām", "vīran" },
			{ "vīthāḥ", "vīyāthām", "vīdhvam" },
			{ "vīya", "vīvahi", "vīmahi" } };
		final VerbalParadigm sunvitaParad = VerbalParadigm.generate("sunvīta", "sunoti", TenseMood.OPT, Pada.MID, sunvitaData, "16-6");
		paradigmMap.put("sunvīta", sunvitaParad);
		final String[][] apnuvitaData = {
			{ "uvīta", "uvīyātām", "uvīran" },
			{ "uvīthāḥ", "uvīyāthām", "uvīdhvam" },
			{ "uvīya", "uvīvahi", "uvīmahi" } };
		final VerbalParadigm apnuvitaParad = VerbalParadigm.generate("āpnuvīta", "āpnoti", TenseMood.OPT, Pada.MID, apnuvitaData, "16-6");
		paradigmMap.put("āpnuvīta", apnuvitaParad);
		final String[][] sunutamData = {
			{ "utām", "vātām", "vatām" },
			{ "uṣva", "vāthām", "udhvam" },
			{ "avai", "avāvahai", "avāmahai" } };
		final VerbalParadigm sunutamParad = VerbalParadigm.generate("sunutām", "sunoti", TenseMood.IMP, Pada.MID, sunutamData, "16-6");
		final VerbalParadigm apnutamParad = VerbalParadigm.duplicate(sunutamParad, "āpnutām", "āpnoti");
		paradigmMap.put("sunutām", sunutamParad);
		apnutamParad.setEndings(Person.PRATHAMA, Number.DUAL, List.of("uvātām"));
		apnutamParad.setEndings(Person.PRATHAMA, Number.PLU, List.of("uvatām"));
		apnutamParad.setEndings(Person.MADHYAMA, Number.DUAL, List.of("uvāthām"));
		paradigmMap.put("āpnutām", apnutamParad);
		final String[][] asunutaData = {
			{ "uta", "vātām", "vata" },
			{ "uthāḥ", "vāthām", "udhvam" },
			{ "vi", "uvahi", "umahi" } };
		final VerbalParadigm asunutaParad = VerbalParadigm.generate("asunuta", "sunoti", TenseMood.IMPERF, Pada.MID, asunutaData, "16-6");
		asunutaParad.setAugment(true);
		final VerbalParadigm apnutaParad = VerbalParadigm.duplicate(asunutaParad, "āpnuta", "āpnoti");
		asunutaParad.addEndings(Person.UTTAMA, Number.DUAL, "vahi");
		asunutaParad.addEndings(Person.UTTAMA, Number.PLU, "mahi");
		paradigmMap.put("asunuta", asunutaParad);
		apnutaParad.setEndings(Person.PRATHAMA, Number.DUAL, List.of("uvātām"));
		apnutaParad.setEndings(Person.PRATHAMA, Number.PLU, List.of("uvata"));
		apnutaParad.setEndings(Person.MADHYAMA, Number.DUAL, List.of("uvāthām"));
		apnutaParad.setEndings(Person.UTTAMA, Number.SING, List.of("uvi"));
		paradigmMap.put("āpnuta", apnutaParad);

		// -Vti group
		// juhoti
		final String[][] juhotiData = {
			{ "oti", "utaḥ", "vati" },
			{ "oṣi", "uthaḥ", "utha" },
			{ "omi", "uvaḥ", "umaḥ" } };
		final VerbalParadigm juhotiParad = VerbalParadigm.generate("juhoti", "juhoti", TenseMood.PRES, Pada.ACT, juhotiData, "16-7");
		paradigmMap.put("juhoti", juhotiParad);
//~ 		final VerbalParadigm juxhotiParad = VerbalParadigm.duplicate(juhotiParad, "juxhoti", "juxhoti");
//~ 		juxhotiParad.setEndings(Person.PRATHAMA, Number.PLU, List.of("uvati"));
//~ 		paradigmMap.put("juxhoti", juxhotiParad);
		final String[][] juhuyatData = {
			{ "uyāt", "uyātām", "uyuḥ" },
			{ "uyāḥ", "uyātam", "uyāta" },
			{ "uyām", "uyāva", "uyāma" } };
		final VerbalParadigm juhuyatParad = VerbalParadigm.generate("juhuyat", "juhoti", TenseMood.OPT, Pada.ACT, juhuyatData, "16-7");
		paradigmMap.put("juhuyat", juhuyatParad);
		final String[][] juhotuData = {
			{ "otu", "utām", "vatu" },
			{ "udhi", "utam", "uta" },
			{ "avāni", "avāva", "avāma" } };
		final VerbalParadigm juhotuParad = VerbalParadigm.generate("juhotu", "juhoti", TenseMood.IMP, Pada.ACT, juhotuData, "16-7");
		paradigmMap.put("juhotu", juhotuParad);
//~ 		final VerbalParadigm juxhotuParad = VerbalParadigm.duplicate(juhotuParad, "juxhotu", "juxhoti");
//~ 		juxhotuParad.setEndings(Person.PRATHAMA, Number.PLU, List.of("uvatu"));
//~ 		paradigmMap.put("juxhotu", juxhotuParad);
		final String[][] ajuhotData = {
			{ "ot", "utām", "avuḥ" },
			{ "oḥ", "utam", "uta" },
			{ "avam", "uva", "uma" } };
		final VerbalParadigm ajuhotParad = VerbalParadigm.generate("ajuhot", "juhoti", TenseMood.IMPERF, Pada.ACT, ajuhotData, "16-7");
		ajuhotParad.setAugment(true);
		paradigmMap.put("ajuhot", ajuhotParad);
		final String[][] juhuteData = {
			{ "ute", "vāte", "vate" },
			{ "uṣe", "vāthe", "udhve" },
			{ "ve", "uvahe", "umahe" } };
		final VerbalParadigm juhuteParad = VerbalParadigm.generate("juhute", "juhoti", TenseMood.PRES, Pada.MID, juhuteData, "16-7");
		paradigmMap.put("juhute", juhuteParad);
//~ 		final VerbalParadigm juxhuteParad = VerbalParadigm.duplicate(juhuteParad, "juxhute", "juxhoti");
//~ 		juxhuteParad.setEndings(Person.PRATHAMA, Number.DUAL, List.of("uvāte"));
//~ 		juxhuteParad.setEndings(Person.PRATHAMA, Number.PLU, List.of("uvate"));
//~ 		juxhuteParad.setEndings(Person.MADHYAMA, Number.DUAL, List.of("uvāthe"));
//~ 		juxhuteParad.setEndings(Person.UTTAMA, Number.SING, List.of("uve"));
//~ 		paradigmMap.put("juxhute", juxhuteParad);
		final String[][] juhvitaData = {
			{ "vīta", "vīyātām", "vīran" },
			{ "vīthāḥ", "vīyāthām", "vīdhvam" },
			{ "vīya", "vīvahi", "vīmahi" } };
		final VerbalParadigm juhvitaParad = VerbalParadigm.generate("juhvīta", "juhoti", TenseMood.OPT, Pada.MID, juhvitaData, "16-7");
		paradigmMap.put("juhvīta", juhvitaParad);
//~ 		final String[][] juxhuvitaData = {
//~ 			{ "uvīta", "uvīyātām", "uvīran" },
//~ 			{ "uvīthāḥ", "uvīyāthām", "uvīdhvam" },
//~ 			{ "uvīya", "uvīvahi", "uvīmahi" } };
//~ 		final VerbalParadigm juxhuvitaParad = VerbalParadigm.generate("juxhuvīta", "juxhoti", TenseMood.OPT, Pada.MID, juxhuvitaData, "16-7");
//~ 		paradigmMap.put("juxhuvīta", juxhuvitaParad);
		final String[][] juhutamData = {
			{ "utām", "vātām", "vatām" },
			{ "uṣva", "vāthām", "udhvam" },
			{ "avai", "avāvahai", "avāmahai" } };
		final VerbalParadigm juhutamParad = VerbalParadigm.generate("juhutām", "juhoti", TenseMood.IMP, Pada.MID, juhutamData, "16-7");
		paradigmMap.put("juhutām", juhutamParad);
//~ 		final VerbalParadigm juxhutamParad = VerbalParadigm.duplicate(juhutamParad, "juxhutām", "juxhoti");
//~ 		juxhutamParad.setEndings(Person.PRATHAMA, Number.DUAL, List.of("uvātām"));
//~ 		juxhutamParad.setEndings(Person.PRATHAMA, Number.PLU, List.of("uvatām"));
//~ 		juxhutamParad.setEndings(Person.MADHYAMA, Number.DUAL, List.of("uvāthām"));
//~ 		paradigmMap.put("juxhutām", juxhutamParad);
		final String[][] ajuhutaData = {
			{ "uta", "vātām", "vata" },
			{ "uthāḥ", "vāthām", "udhvam" },
			{ "vi", "uvahi", "umahi" } };
		final VerbalParadigm ajuhutaParad = VerbalParadigm.generate("ajuhuta", "juhoti", TenseMood.IMPERF, Pada.MID, ajuhutaData, "16-7");
		ajuhutaParad.setAugment(true);
		paradigmMap.put("ajuhuta", ajuhutaParad);
//~ 		final VerbalParadigm ajuxhutaParad = VerbalParadigm.duplicate(ajuhutaParad, "ajuxhuta", "juxhoti");
//~ 		ajuxhutaParad.setEndings(Person.PRATHAMA, Number.DUAL, List.of("uvātām"));
//~ 		ajuxhutaParad.setEndings(Person.PRATHAMA, Number.PLU, List.of("uvata"));
//~ 		ajuxhutaParad.setEndings(Person.MADHYAMA, Number.DUAL, List.of("uvāthām"));
//~ 		ajuxhutaParad.setEndings(Person.UTTAMA, Number.SING, List.of("uvi"));
//~ 		paradigmMap.put("ajuxhuta", ajuxhutaParad);
		// bibharti
		final List<String> bibhartiWordList = List.of("jāgarti", "piparti", "bibharti");
		final String[][] bibhartiData = {
			{ "arti", "ṛtaḥ", "rti" },
			{ "arṣi", "ṛthaḥ", "ṛtha" },
			{ "armi", "ṛvaḥ", "ṛmaḥ" } };
		final VerbalParadigm bibhartiParad = VerbalParadigm.generate("bibharti", "bibharti", TenseMood.PRES, Pada.ACT, bibhartiData, "16-7");
		bibhartiParad.setStemCutFactor(4);
		bibhartiParad.addWordList(bibhartiWordList);
		paradigmMap.put("bibharti", bibhartiParad);
		final String[][] bibhryatData = {
			{ "ṛyāt", "ṛyātām", "ṛyuḥ" },
			{ "ṛyāḥ", "ṛyātam", "ṛyāta" },
			{ "ṛyām", "ṛyāva", "ṛyāma" } };
		final VerbalParadigm bibhryatParad = VerbalParadigm.generate("bibhṛyāt", "bibharti", TenseMood.OPT, Pada.ACT, bibhryatData, "16-7");
		bibhryatParad.setStemCutFactor(4);
		bibhryatParad.addWordList(bibhartiWordList);
		paradigmMap.put("bibhṛyāt", bibhryatParad);
		final String[][] bibhartuData = {
			{ "artu", "ṛtām", "rtu" },
			{ "ṛhi", "ṛtam", "ṛta" },
			{ "arāṇi", "arāva", "arāma" } };
		final VerbalParadigm bibhartuParad = VerbalParadigm.generate("bibhartu", "bibharti", TenseMood.IMP, Pada.ACT, bibhartuData, "16-7");
		bibhartuParad.setStemCutFactor(4);
		bibhartuParad.addWordList(bibhartiWordList);
		paradigmMap.put("bibhartu", bibhartuParad);
		final String[][] abibharData = {
			{ "ar", "ṛtām", "aruḥ" },
			{ "ar", "ṛtam", "ṛta" },
			{ "aram", "ṛva", "ṛma" } };
		final VerbalParadigm abibharParad = VerbalParadigm.generate("abibhar", "bibharti", TenseMood.IMPERF, Pada.ACT, abibharData, "16-7");
		abibharParad.setStemCutFactor(4);
		abibharParad.addWordList(bibhartiWordList);
		abibharParad.setAugment(true);
		paradigmMap.put("abibhar", abibharParad);
		final String[][] bibhrteData = {
			{ "ṛte", "rāte", "rate" },
			{ "ṛṣe", "rāthe", "ṛdhve" },
			{ "re", "ṛvahe", "ṛmahe" } };
		final VerbalParadigm bibhrteParad = VerbalParadigm.generate("bibhrte", "bibharti", TenseMood.PRES, Pada.MID, bibhrteData, "16-7");
		bibhrteParad.setStemCutFactor(4);
		bibhrteParad.addWordList(bibhartiWordList);
		paradigmMap.put("bibhrte", bibhrteParad);
		final String[][] bibhritaData = {
			{ "rīta", "rīyātām", "rīran" },
			{ "rīthāḥ", "rīyāthām", "rīdhvam" },
			{ "rīya", "rīvahi", "rīmahi" } };
		final VerbalParadigm bibhritaParad = VerbalParadigm.generate("bibhrīta", "bibharti", TenseMood.OPT, Pada.MID, bibhritaData, "16-7");
		bibhritaParad.setStemCutFactor(4);
		bibhritaParad.addWordList(bibhartiWordList);
		paradigmMap.put("bibhrīta", bibhritaParad);
		final String[][] bibhrtamData = {
			{ "ṛtām", "rātām", "ratām" },
			{ "ṛṣva", "rāthām", "ṛdhvam" },
			{ "arai", "arāvahai", "arāmahai" } };
		final VerbalParadigm bibhrtamParad = VerbalParadigm.generate("bibhṛtām", "bibharti", TenseMood.IMP, Pada.MID, bibhrtamData, "16-7");
		bibhrtamParad.setStemCutFactor(4);
		bibhrtamParad.addWordList(bibhartiWordList);
		paradigmMap.put("bibhṛtām", bibhrtamParad);
		final String[][] abibhrtaData = {
			{ "ṛta", "rātām", "rata" },
			{ "ṛthāḥ", "rāthām", "ṛdhvam" },
			{ "ri", "ṛvahi", "ṛmahi" } };
		final VerbalParadigm abibhrtaParad = VerbalParadigm.generate("abibhṛta", "bibharti", TenseMood.IMPERF, Pada.MID, abibhrtaData, "16-7");
		abibhrtaParad.setStemCutFactor(4);
		abibhrtaParad.addWordList(bibhartiWordList);
		abibhrtaParad.setAugment(true);
		paradigmMap.put("abibhṛta", abibhrtaParad);

		// bibheti (from Deshpande's Primer, p. 227)
		final String[][] bibhetiData = {
			{ "eti", "ītaḥ", "yati" },
			{ "eṣi", "īthaḥ", "ītha" },
			{ "emi", "īvaḥ", "īmaḥ" } };
		final String[][] bibhetiData2 = {
			{ "", "itaḥ", "iyati" },
			{ "", "ithaḥ", "itha" },
			{ "", "ivaḥ", "imaḥ" } };
		final VerbalParadigm bibhetiParad = VerbalParadigm.generate("bibheti", "bibheti", TenseMood.PRES, Pada.ACT, bibhetiData, "16-7");
		bibhetiParad.addEndings(bibhetiData2);
		paradigmMap.put("bibheti", bibhetiParad);
		final VerbalParadigm jihretiParad = VerbalParadigm.duplicate(bibhetiParad, "jihreti", "jihreti");
		jihretiParad.setEndings(Person.PRATHAMA, Number.PLU, List.of("iyati"));
		paradigmMap.put("jihreti", jihretiParad);
		final String[][] bibhiyatData = {
			{ "īyāt", "īyātām", "īyuḥ" },
			{ "īyāḥ", "īyātam", "īyāta" },
			{ "īyām", "īyāva", "īyāma" } };
		final String[][] bibhiyatData2 = {
			{ "iyāt", "iyātām", "iyuḥ" },
			{ "iyāḥ", "iyātam", "iyāta" },
			{ "iyām", "iyāva", "iyāma" } };
		final VerbalParadigm bibhiyatParad = VerbalParadigm.generate("bibhīyāt", "bibheti", TenseMood.OPT, Pada.ACT, bibhiyatData, "16-7");
		bibhiyatParad.addEndings(bibhiyatData2);
		paradigmMap.put("bibhīyāt", bibhiyatParad);
		final String[][] bibhetuData = {
			{ "etu", "ītām", "yatu" },
			{ "īhi", "ītam", "īta" },
			{ "ayāṇi", "ayāva", "ayāma" } };
		final String[][] bibhetuData2 = {
			{ "", "itām", "iyatu" },
			{ "ihi", "itam", "ita" },
			{ "", "", "" } };
		final VerbalParadigm bibhetuParad = VerbalParadigm.generate("bibhetu", "bibheti", TenseMood.IMP, Pada.ACT, bibhetuData, "16-7");
		bibhetuParad.addEndings(bibhetuData2);
		paradigmMap.put("bibhetu", bibhetuParad);
		final VerbalParadigm jihretuParad = VerbalParadigm.duplicate(bibhetuParad, "jihretu", "jihreti");
		jihretuParad.setEndings(Person.PRATHAMA, Number.PLU, List.of("iyatu"));
		paradigmMap.put("jihretu", jihretuParad);
		final String[][] abibhetData = {
			{ "et", "ītām", "ayuḥ" },
			{ "eḥ", "ītam", "īta" },
			{ "ayam", "īva", "īma" } };
		final String[][] abibhetData2 = {
			{ "", "itām", "" },
			{ "", "itam", "ita" },
			{ "", "iva", "ima" } };
		final VerbalParadigm abibhetParad = VerbalParadigm.generate("abibhet", "bibheti", TenseMood.IMPERF, Pada.ACT, abibhetData, "16-7");
		abibhetParad.addEndings(abibhetData2);
		abibhetParad.setAugment(true);
		paradigmMap.put("abibhet", abibhetParad);

		// -Cti group
		// yunakti
		final String[][] yunaktiData = {
			{ "nakti", "ṅktaḥ", "ñjanti" },
			{ "nakṣi", "ṅkthaḥ", "ṅktha" },
			{ "najmi", "ñjvaḥ", "ñjmaḥ" } };
		final VerbalParadigm yunaktiParad = VerbalParadigm.generate("yunakti", "yunakti", TenseMood.PRES, Pada.ACT, yunaktiData, "16-8");
		yunaktiParad.setStemCutFactor(5);
		paradigmMap.put("yunakti", yunaktiParad);
		final String[][] yunjyatData = {
			{ "ñjyāt", "ñjyātām", "ñjyuḥ" },
			{ "ñjyāḥ", "ñjyātam", "ñjyāta" },
			{ "ñjyām", "ñjyāva", "ñjyāma" } };
		final VerbalParadigm yunjyatParad = VerbalParadigm.generate("yuñjyāt", "yunakti", TenseMood.OPT, Pada.ACT, yunjyatData, "16-8");
		yunjyatParad.setStemCutFactor(5);
		paradigmMap.put("yuñjyāt", yunjyatParad);
		final String[][] yunaktuData = {
			{ "naktu", "ṅktām", "ñjantu" },
			{ "ṅgdhi", "ṅktam", "ṅkta" },
			{ "najāni", "najāva", "najāma" } };
		final VerbalParadigm yunaktuParad = VerbalParadigm.generate("yunaktu", "yunakti", TenseMood.IMP, Pada.ACT, yunaktuData, "16-8");
		yunaktuParad.setStemCutFactor(5);
		paradigmMap.put("yunaktu", yunaktuParad);
		final String[][] ayunakData = {
			{ "nak", "ṅktām", "ñjan" },
			{ "nak", "ṅktam", "ṅkta" },
			{ "najam", "ñjva", "ñjma" } };
		final VerbalParadigm ayunakParad = VerbalParadigm.generate("ayunak", "yunakti", TenseMood.IMPERF, Pada.ACT, ayunakData, "16-8");
		ayunakParad.setStemCutFactor(5);
		ayunakParad.setAugment(true);
		paradigmMap.put("ayunak", ayunakParad);
		final String[][] yunkteData = {
			{ "ṅkte", "ñjāte", "ñjate" },
			{ "ṅkṣe", "ñjāthe", "ṅgdhve" },
			{ "ñje", "ñjvahe", "ñjmahe" } };
		final VerbalParadigm yunkteParad = VerbalParadigm.generate("yuṅkte", "yunakti", TenseMood.PRES, Pada.MID, yunkteData, "16-8");
		yunkteParad.setStemCutFactor(5);
		paradigmMap.put("yuṅkte", yunkteParad);
		final String[][] yunjitaData = {
			{ "ñjīta", "ñjīyātām", "ñjīran" },
			{ "ñjīthāḥ", "ñjīyāthām", "ñjīdhvam" },
			{ "ñjīya", "ñjīvahi", "ñjīmahi" } };
		final VerbalParadigm yunjitaParad = VerbalParadigm.generate("yuñjīta", "yunakti", TenseMood.OPT, Pada.MID, yunjitaData, "16-8");
		yunjitaParad.setStemCutFactor(5);
		paradigmMap.put("yuñjīta", yunjitaParad);
		final String[][] yunktamData = {
			{ "ṅktām", "ñjātām", "ñjatām" },
			{ "ṅkṣva", "ñjāthām", "ṅgdhvam" },
			{ "najai", "najāvahai", "najāmahai" } };
		final VerbalParadigm yunktamParad = VerbalParadigm.generate("yuṅktām", "yunakti", TenseMood.IMP, Pada.MID, yunktamData, "16-8");
		yunktamParad.setStemCutFactor(5);
		paradigmMap.put("yuṅktām", yunktamParad);
		final String[][] ayunktaData = {
			{ "ṅkta", "ñjātām", "ñjata" },
			{ "ṅkthāḥ", "ñjāthām", "ṅgdhvam" },
			{ "ñji", "ñjvahi", "ñjmahi" } };
		final VerbalParadigm ayunktaParad = VerbalParadigm.generate("ayuṅkta", "yunakti", TenseMood.IMPERF, Pada.MID, ayunktaData, "16-8");
		ayunktaParad.setStemCutFactor(5);
		ayunktaParad.setAugment(true);
		paradigmMap.put("ayuṅkta", ayunktaParad);

		// rinakti
		final String[][] rinaktiData = {
			{ "nakti", "ṅktaḥ", "ñcanti" },
			{ "nakṣi", "ṅkthaḥ", "ṅktha" },
			{ "nacmi", "ñcvaḥ", "ñcmaḥ" } };
		final VerbalParadigm rinaktiParad = VerbalParadigm.generate("rinakti", "rinakti", TenseMood.PRES, Pada.ACT, rinaktiData, "16-8");
		rinaktiParad.setStemCutFactor(5);
		paradigmMap.put("rinakti", rinaktiParad);
		final String[][] rincyatData = {
			{ "ñcyāt", "ñcyātām", "ñcyuḥ" },
			{ "ñcyāḥ", "ñcyātam", "ñcyāta" },
			{ "ñcyām", "ñcyāva", "ñcyāma" } };
		final VerbalParadigm rincyatParad = VerbalParadigm.generate("riñcyāt", "rinakti", TenseMood.OPT, Pada.ACT, rincyatData, "16-8");
		rincyatParad.setStemCutFactor(5);
		paradigmMap.put("riñcyāt", rincyatParad);
		final String[][] rinaktuData = {
			{ "naktu", "ṅktām", "ñcantu" },
			{ "ṅgdhi", "ṅktam", "ṅkta" },
			{ "nacāni", "nacāva", "nacāma" } };
		final VerbalParadigm rinaktuParad = VerbalParadigm.generate("rinaktu", "rinakti", TenseMood.IMP, Pada.ACT, rinaktuData, "16-8");
		rinaktuParad.setStemCutFactor(5);
		paradigmMap.put("rinaktu", rinaktuParad);
		final String[][] arinakData = {
			{ "nak", "ṅktām", "ñcan" },
			{ "nak", "ṅktam", "ṅkta" },
			{ "nacam", "ñcva", "ñcma" } };
		final VerbalParadigm arinakParad = VerbalParadigm.generate("arinak", "rinakti", TenseMood.IMPERF, Pada.ACT, arinakData, "16-8");
		arinakParad.setStemCutFactor(5);
		arinakParad.setAugment(true);
		paradigmMap.put("arinak", arinakParad);
		final String[][] rinkteData = {
			{ "ṅkte", "ñcāte", "ñcate" },
			{ "ṅkṣe", "ñcāthe", "ṅgdhve" },
			{ "ñce", "ñcvahe", "ñcmahe" } };
		final VerbalParadigm rinkteParad = VerbalParadigm.generate("riṅkte", "rinakti", TenseMood.PRES, Pada.MID, rinkteData, "16-8");
		rinkteParad.setStemCutFactor(5);
		paradigmMap.put("riṅkte", rinkteParad);
		final String[][] rincitaData = {
			{ "ñcīta", "ñcīyātām", "ñcīran" },
			{ "ñcīthāḥ", "ñcīyāthām", "ñcīdhvam" },
			{ "ñcīya", "ñcīvahi", "ñcīmahi" } };
		final VerbalParadigm rincitaParad = VerbalParadigm.generate("riñcīta", "rinakti", TenseMood.OPT, Pada.MID, rincitaData, "16-8");
		rincitaParad.setStemCutFactor(5);
		paradigmMap.put("riñcīta", rincitaParad);
		final String[][] rinktamData = {
			{ "ṅktām", "ñcātām", "ñcatām" },
			{ "ṅkṣva", "ñcāthām", "ṅgdhvam" },
			{ "nacai", "nacāvahai", "nacāmahai" } };
		final VerbalParadigm rinktamParad = VerbalParadigm.generate("riṅktām", "rinakti", TenseMood.IMP, Pada.MID, rinktamData, "16-8");
		rinktamParad.setStemCutFactor(5);
		paradigmMap.put("riṅktām", rinktamParad);
		final String[][] arinktaData = {
			{ "ṅkta", "ñcātām", "ñcata" },
			{ "ṅkthāḥ", "ñcāthām", "ṅgdhvam" },
			{ "ñci", "ñcvahi", "ñcmahi" } };
		final VerbalParadigm arinktaParad = VerbalParadigm.generate("ariṅkta", "rinakti", TenseMood.IMPERF, Pada.MID, arinktaData, "16-8");
		arinktaParad.setStemCutFactor(5);
		arinktaParad.setAugment(true);
		paradigmMap.put("ariṅkta", arinktaParad);

		// vetti (from Deshpande's Primer, p. 218)
		final String[][] vettiData = {
			{ "etti", "ittaḥ", "idanti" },
			{ "etsi", "itthaḥ", "ittha" },
			{ "edmi", "idvaḥ", "idmaḥ" } };
		final VerbalParadigm vettiParad = VerbalParadigm.generate("vetti", "vetti", TenseMood.PRES, Pada.ACT, vettiData, "16-8");
		vettiParad.setStemCutFactor(4);
		paradigmMap.put("vetti", vettiParad);
		final String[][] vidyatData = {
			{ "idyāt", "idyātām", "idyuḥ" },
			{ "idyāḥ", "idyātam", "idyāta" },
			{ "idyām", "idyāva", "idyāma" } };
		final VerbalParadigm vidyatParad = VerbalParadigm.generate("vidyāt", "vetti", TenseMood.OPT, Pada.ACT, vidyatData, "16-8");
		vidyatParad.setStemCutFactor(4);
		paradigmMap.put("vidyāt", vidyatParad);
		final String[][] vettuData = {
			{ "ettu", "ittām", "idantu" },
			{ "iddhi", "ittam", "itta" },
			{ "idāni", "idāva", "idāma" } };
		final VerbalParadigm vettuParad = VerbalParadigm.generate("vettu", "vetti", TenseMood.IMP, Pada.ACT, vettuData, "16-8");
		vettuParad.setStemCutFactor(4);
		paradigmMap.put("vettu", vettuParad);
		final String[][] avetData = {
			{ "et", "ittām", "iduḥ" },
			{ "et", "ittam", "itta" },
			{ "edam", "idva", "idma" } };
		final VerbalParadigm avetParad = VerbalParadigm.generate("avet", "vetti", TenseMood.IMPERF, Pada.ACT, avetData, "16-8");
		avetParad.setStemCutFactor(4);
		avetParad.setAugment(true);
		avetParad.addEndings(Person.MADHYAMA, Number.SING, "eḥ");
		paradigmMap.put("avet", avetParad);

		// atti
		final String[][] attiData = {
			{ "tti", "ttaḥ", "danti" },
			{ "tsi", "tthaḥ", "ttha" },
			{ "dmi", "dvaḥ", "dmaḥ" } };
		final VerbalParadigm attiParad = VerbalParadigm.generate("atti", "atti", TenseMood.PRES, Pada.ACT, attiData);
		attiParad.setStemCutFactor(3);
		paradigmMap.put("atti", attiParad);
		final String[][] adyatData = {
			{ "dyāt", "dyātām", "dyuḥ" },
			{ "dyāḥ", "dyātam", "dyāta" },
			{ "dyām", "dyāva", "dyāma" } };
		final VerbalParadigm adyatParad = VerbalParadigm.generate("adyāt", "atti", TenseMood.OPT, Pada.ACT, adyatData);
		adyatParad.setStemCutFactor(3);
		paradigmMap.put("adyāt", adyatParad);
		final String[][] attuData = {
			{ "ttu", "ttām", "dantu" },
			{ "ddhi", "ttam", "tta" },
			{ "dāni", "dāva", "dāma" } };
		final VerbalParadigm attuParad = VerbalParadigm.generate("attu", "atti", TenseMood.IMP, Pada.ACT, attuData);
		attuParad.setStemCutFactor(3);
		paradigmMap.put("attu", attuParad);
		final String[][] atData = {
			{ "t", "ttām", "duḥ" },
			{ "t", "ttam", "tta" },
			{ "dam", "dva", "dma" } };
		final VerbalParadigm atParad = VerbalParadigm.generate("āt", "atti", TenseMood.IMPERF, Pada.ACT, atData);
		atParad.setStemCutFactor(3);
		atParad.setAugment(true);
		paradigmMap.put("āt", atParad);

		// dveṣṭi
		final String[][] dvestiData = {
			{ "eṣṭi", "iṣṭaḥ", "iṣanti" },
			{ "ekṣi", "iṣṭhaḥ", "iṣṭha" },
			{ "eṣmi", "iṣvaḥ", "iṣmaḥ" } };
		final VerbalParadigm dvestiParad = VerbalParadigm.generate("dveṣṭi", "dveṣṭi", TenseMood.PRES, Pada.ACT, dvestiData, "16-9");
		dvestiParad.setStemCutFactor(4);
		paradigmMap.put("dveṣṭi", dvestiParad);
		final String[][] dvisyatData = {
			{ "iṣyāt", "iṣyātām", "iṣyuḥ" },
			{ "iṣyāḥ", "iṣyātam", "iṣyāta" },
			{ "iṣyām", "iṣyāva", "iṣyāma" } };
		final VerbalParadigm dvisyatParad = VerbalParadigm.generate("dviṣyāt", "dveṣṭi", TenseMood.OPT, Pada.ACT, dvisyatData, "16-9");
		dvisyatParad.setStemCutFactor(4);
		paradigmMap.put("dviṣyāt", dvisyatParad);
		final String[][] dvestuData = {
			{ "eṣṭu", "iṣṭām", "iṣantu" },
			{ "iḍḍhi", "iṣṭam", "iṣṭa" },
			{ "eṣāṇi", "eṣāva", "eṣāma" } };
		final VerbalParadigm dvestuParad = VerbalParadigm.generate("dveṣṭu", "dveṣṭi", TenseMood.IMP, Pada.ACT, dvestuData, "16-9");
		dvestuParad.setStemCutFactor(4);
		paradigmMap.put("dveṣṭu", dvestuParad);
		final String[][] advetData = {
			{ "eṭ", "iṣṭām", "iṣan" },
			{ "eṭ", "iṣṭam", "iṣṭa" },
			{ "eṣam", "iṣva", "iṣma" } };
		final VerbalParadigm advetParad = VerbalParadigm.generate("adveṭ", "dveṣṭi", TenseMood.IMPERF, Pada.ACT, advetData, "16-9");
		advetParad.setStemCutFactor(4);
		advetParad.setAugment(true);
		paradigmMap.put("adveṭ", advetParad);
		final String[][] dvisteData = {
			{ "iṣṭe", "iṣāte", "iṣate" },
			{ "ikṣe", "iṣāthe", "iḍḍhve" },
			{ "iṣe", "iṣvahe", "iṣmahe" } };
		final VerbalParadigm dvisteParad = VerbalParadigm.generate("dviṣṭe", "dveṣṭi", TenseMood.PRES, Pada.MID, dvisteData, "16-9");
		dvisteParad.setStemCutFactor(4);
		paradigmMap.put("dviṣṭe", dvisteParad);
		final String[][] dvisitaData = {
			{ "iṣīta", "iṣīyātām", "iṣīran" },
			{ "iṣīthāḥ", "iṣīyāthām", "iṣīdhvam" },
			{ "iṣīya", "iṣīvahi", "iṣīmahi" } };
		final VerbalParadigm dvisitaParad = VerbalParadigm.generate("dviṣīta", "dveṣṭi", TenseMood.OPT, Pada.MID, dvisitaData, "16-9");
		dvisitaParad.setStemCutFactor(4);
		paradigmMap.put("dviṣīta", dvisitaParad);
		final String[][] dvistamData = {
			{ "iṣṭām", "iṣātām", "iṣatām" },
			{ "ikṣva", "iṣāthām", "iḍḍhvam" },
			{ "eṣai", "eṣāvahai", "eṣāmahai" } };
		final VerbalParadigm dvistamParad = VerbalParadigm.generate("dviṣṭām", "dveṣṭi", TenseMood.IMP, Pada.MID, dvistamData, "16-9");
		dvistamParad.setStemCutFactor(4);
		paradigmMap.put("dviṣṭām", dvistamParad);
		final String[][] advistaData = {
			{ "iṣṭa", "iṣātām", "iṣata" },
			{ "iṣthāḥ", "iṣāthām", "iḍḍhvam" },
			{ "iṣi", "iṣvahi", "iṣmahi" } };
		final VerbalParadigm advistaParad = VerbalParadigm.generate("adviṣṭa", "dveṣṭi", TenseMood.IMPERF, Pada.MID, advistaData, "16-9");
		advistaParad.setStemCutFactor(4);
		advistaParad.setAugment(true);
		paradigmMap.put("adviṣṭa", advistaParad);

		// vaṣṭi
		final String[][] vastiData = {
			{ "vaṣṭi", "uṣṭaḥ", "uśanti" },
			{ "vakṣi", "uṣṭhaḥ", "uṣṭha" },
			{ "vaśmi", "uśvaḥ", "uśmaḥ" } };
		final VerbalParadigm vaṣṭiParad = VerbalParadigm.generate("vaṣṭi", "vaṣṭi", TenseMood.PRES, Pada.ACT, vastiData, "16-10");
		vaṣṭiParad.setStemCutFactor(5);
		paradigmMap.put("vaṣṭi", vaṣṭiParad);
		final String[][] usyatData = {
			{ "uśyāt", "uśyātām", "uśyuḥ" },
			{ "uśyāḥ", "uśyātam", "uśyāta" },
			{ "uśyām", "uśyāva", "uśyāma" } };
		final VerbalParadigm usyatParad = VerbalParadigm.generate("uśyāt", "vaṣṭi", TenseMood.OPT, Pada.ACT, usyatData, "16-10");
		usyatParad.setStemCutFactor(5);
		paradigmMap.put("uśyāt", usyatParad);
		final String[][] vastuData = {
			{ "vaṣṭu", "uṣṭām", "uśantu" },
			{ "uḍḍhi", "uṣṭam", "uṣṭa" },
			{ "vaśāṇi", "vaśāva", "vaśāma" } };
		final VerbalParadigm vastuParad = VerbalParadigm.generate("vaṣṭu", "vaṣṭi", TenseMood.IMP, Pada.ACT, vastuData, "16-10");
		vastuParad.setStemCutFactor(5);
		paradigmMap.put("vaṣṭu", vastuParad);
		final String[][] avatData = {
			{ "vaṭ", "uṣṭām", "uśan" },
			{ "vaṭ", "uṣṭam", "uṣṭa" },
			{ "vaśam", "uśva", "uśma" } };
		final VerbalParadigm avatParad = VerbalParadigm.generate("avaṭ", "vaṣṭi", TenseMood.IMPERF, Pada.ACT, avatData, "16-10");
		avatParad.setStemCutFactor(5);
		avatParad.setAugment(true);
		paradigmMap.put("avaṭ", avatParad);

		// ruṇaddhi
		final String[][] runaddhiData = {
			{ "ṇaddhi", "nddhaḥ", "ndhanti" },
			{ "ṇatsi", "nddhaḥ", "nddha" },
			{ "ṇadhmi", "ndhvaḥ", "ndhmaḥ" } };
		final VerbalParadigm runaddhiParad = VerbalParadigm.generate("ruṇaddhi", "ruṇaddhi", TenseMood.PRES, Pada.ACT, runaddhiData, "16-11");
		runaddhiParad.setStemCutFactor(6);
		paradigmMap.put("ruṇaddhi", runaddhiParad);
		final String[][] rundhyatData = {
			{ "ndhyāt", "ndhyātām", "ndhyuḥ" },
			{ "ndhyāḥ", "ndhyātam", "ndhyāta" },
			{ "ndhyām", "ndhyāva", "ndhyāma" } };
		final VerbalParadigm rundhyatParad = VerbalParadigm.generate("rundhyāt", "ruṇaddhi", TenseMood.OPT, Pada.ACT, rundhyatData, "16-11");
		rundhyatParad.setStemCutFactor(6);
		paradigmMap.put("rundhyāt", rundhyatParad);
		final String[][] runaddhuData = {
			{ "ṇaddhu", "nddhām", "ndhantu" },
			{ "nddhi", "nddham", "nddha" },
			{ "ṇadhāni", "ṇadhāva", "ṇadhāma" } };
		final VerbalParadigm runaddhuParad = VerbalParadigm.generate("ruṇaddhu", "ruṇaddhi", TenseMood.IMP, Pada.ACT, runaddhuData, "16-11");
		runaddhuParad.setStemCutFactor(6);
		paradigmMap.put("ruṇaddhu", runaddhuParad);
		final String[][] arunatData = {
			{ "ṇat", "nddhām", "ndhan" },
			{ "ṇat", "nddham", "nddha" },
			{ "ṇadham", "ndhva", "ndhma" } };
		final VerbalParadigm arunatParad = VerbalParadigm.generate("aruṇat", "ruṇaddhi", TenseMood.IMPERF, Pada.ACT, arunatData, "16-11");
		arunatParad.setStemCutFactor(6);
		arunatParad.setAugment(true);
		arunatParad.addEndings(Person.MADHYAMA, Number.SING, "ṇaḥ");
		paradigmMap.put("aruṇat", arunatParad);
		final String[][] runddheData = {
			{ "nddhe", "ndhāte", "ndhate" },
			{ "ntse", "ndhāthe", "nddhve" },
			{ "ndhe", "ndhvahe", "ndhmahe" } };
		final VerbalParadigm runddheParad = VerbalParadigm.generate("runddhe", "ruṇaddhi", TenseMood.PRES, Pada.MID, runddheData, "16-11");
		runddheParad.setStemCutFactor(6);
		paradigmMap.put("runddhe", runddheParad);
		final String[][] rundhitaData = {
			{ "ndhīta", "ndhīyātām", "ndhīran" },
			{ "ndhīthāḥ", "ndhīyāthām", "ndhīdhvam" },
			{ "ndhīya", "ndhīvahi", "ndhīmahi" } };
		final VerbalParadigm rundhitaParad = VerbalParadigm.generate("rundhīta", "ruṇaddhi", TenseMood.OPT, Pada.MID, rundhitaData, "16-11");
		rundhitaParad.setStemCutFactor(6);
		paradigmMap.put("rundhīta", rundhitaParad);
		final String[][] runddhamData = {
			{ "nddhām", "ndhātām", "ndhatām" },
			{ "ntsva", "ndhāthām", "nddhvam" },
			{ "ṇadhai", "ṇadhāvahai", "ṇadhāmahai" } };
		final VerbalParadigm runddhamParad = VerbalParadigm.generate("runddhām", "ruṇaddhi", TenseMood.IMP, Pada.MID, runddhamData, "16-11");
		runddhamParad.setStemCutFactor(6);
		paradigmMap.put("runddhām", runddhamParad);
		final String[][] arunddhaData = {
			{ "nddha", "ndhātām", "ndhata" },
			{ "nddhāḥ", "ndhāthām", "nddhvam" },
			{ "ndhi", "ndhvahi", "ndhmahi" } };
		final VerbalParadigm arunddhaParad = VerbalParadigm.generate("arunddha", "ruṇaddhi", TenseMood.IMPERF, Pada.MID, arunddhaData, "16-11");
		arunddhaParad.setStemCutFactor(6);
		arunddhaParad.setAugment(true);
		paradigmMap.put("arunddha", arunddhaParad);

		// irregular
		// roditi
		final String[][] roditiData = {
			{ "oditi", "uditaḥ", "udanti" },
			{ "odiṣi", "udithaḥ", "uditha" },
			{ "odimi", "udivaḥ", "udimaḥ" } };
		final VerbalParadigm roditiParad = VerbalParadigm.generate("roditi", "roditi", TenseMood.PRES, Pada.ACT, roditiData, "17-12");
		roditiParad.setStemCutFactor(5);
		paradigmMap.put("roditi", roditiParad);
		final String[][] rudyatData = {
			{ "udyāt", "udyātām", "udyuḥ" },
			{ "udyāḥ", "udyātam", "udyāta" },
			{ "udyām", "udyāva", "udyāma" } };
		final VerbalParadigm rudyatParad = VerbalParadigm.generate("rudyāt", "roditi", TenseMood.OPT, Pada.ACT, rudyatData, "17-12");
		rudyatParad.setStemCutFactor(5);
		paradigmMap.put("rudyāt", rudyatParad);
		final String[][] rodituData = {
			{ "oditu", "uditām", "udantu" },
			{ "udihi", "uditam", "udita" },
			{ "odāni", "odāva", "odāma" } };
		final VerbalParadigm rodituParad = VerbalParadigm.generate("roditu", "roditi", TenseMood.IMP, Pada.ACT, rodituData, "17-12");
		rodituParad.setStemCutFactor(5);
		paradigmMap.put("roditu", rodituParad);
		final String[][] arodatData = {
			{ "odat", "uditām", "udan" },
			{ "odaḥ", "uditam", "udita" },
			{ "odam", "udiva", "udima" } };
		final VerbalParadigm arodatParad = VerbalParadigm.generate("arodat", "roditi", TenseMood.IMPERF, Pada.ACT, arodatData, "17-12");
		arodatParad.setStemCutFactor(5);
		arodatParad.setAugment(true);
		arodatParad.addEndings(Person.PRATHAMA, Number.SING, "odīt");
		arodatParad.addEndings(Person.MADHYAMA, Number.SING, "odīḥ");
		paradigmMap.put("arodat", arodatParad);

		// bravīti
		final String[][] bravitiData = {
			{ "avīti", "ūtaḥ", "uvanti" },
			{ "avīṣi", "ūthaḥ", "ūtha" },
			{ "avīmi", "ūvaḥ", "ūmaḥ" } };
		final VerbalParadigm bravitiParad = VerbalParadigm.generate("bravīti", "bravīti", TenseMood.PRES, Pada.ACT, bravitiData, "17-13");
		bravitiParad.setStemCutFactor(5);
		paradigmMap.put("bravīti", bravitiParad);
		final String[][] bruyatData = {
			{ "ūyāt", "ūyātām", "ūyuḥ" },
			{ "ūyāḥ", "ūyātam", "ūyāta" },
			{ "ūyām", "ūyāva", "ūyāma" } };
		final VerbalParadigm bruyatParad = VerbalParadigm.generate("brūyāt", "bravīti", TenseMood.OPT, Pada.ACT, bruyatData, "17-13");
		bruyatParad.setStemCutFactor(5);
		paradigmMap.put("brūyāt", bruyatParad);
		final String[][] bravituData = {
			{ "avītu", "ūtām", "uvantu" },
			{ "ūhi", "ūtam", "ūta" },
			{ "avāṇi", "avāva", "avāma" } };
		final VerbalParadigm bravituParad = VerbalParadigm.generate("bravītu", "bravīti", TenseMood.IMP, Pada.ACT, bravituData, "17-13");
		bravituParad.setStemCutFactor(5);
		paradigmMap.put("bravītu", bravituParad);
		final String[][] abravitData = {
			{ "avīt", "ūtām", "uvan" },
			{ "avīḥ", "ūtam", "ūta" },
			{ "avam", "ūva", "ūma" } };
		final VerbalParadigm abravitParad = VerbalParadigm.generate("abravīt", "bravīti", TenseMood.IMPERF, Pada.ACT, abravitData, "17-13");
		abravitParad.setStemCutFactor(5);
		abravitParad.setAugment(true);
		paradigmMap.put("abravīt", abravitParad);
		final String[][] bruteData = {
			{ "ūte", "uvāte", "uvate" },
			{ "ūṣe", "uvāthe", "ūdhve" },
			{ "uve", "ūvahe", "ūmahe" } };
		final VerbalParadigm bruteParad = VerbalParadigm.generate("brūte", "bravīti", TenseMood.PRES, Pada.MID, bruteData, "17-13");
		bruteParad.setStemCutFactor(5);
		paradigmMap.put("brūte", bruteParad);
		final String[][] bruvitaData = {
			{ "uvīta", "uvīyātām", "uvīran" },
			{ "uvīthāḥ", "uvīyāthām", "uvīdhvam" },
			{ "uvīya", "uvīvahi", "uvīmahi" } };
		final VerbalParadigm bruvitaParad = VerbalParadigm.generate("bruvīta", "bravīti", TenseMood.OPT, Pada.MID, bruvitaData, "17-13");
		bruvitaParad.setStemCutFactor(5);
		paradigmMap.put("bruvīta", bruvitaParad);
		final String[][] brutamData = {
			{ "ūtām", "uvātām", "uvatām" },
			{ "ūṣva", "uvāthām", "ūdhvam" },
			{ "avai", "avāvahai", "avāmahai" } };
		final VerbalParadigm brutamParad = VerbalParadigm.generate("brūtām", "bravīti", TenseMood.IMP, Pada.MID, brutamData, "17-13");
		brutamParad.setStemCutFactor(5);
		paradigmMap.put("brūtām", brutamParad);
		final String[][] abrutaData = {
			{ "ūta", "uvātām", "uvata" },
			{ "ūthāḥ", "uvāthām", "ūdhvam" },
			{ "uvi", "ūvahi", "ūmahi" } };
		final VerbalParadigm abrutaParad = VerbalParadigm.generate("abrūta", "bravīti", TenseMood.IMPERF, Pada.MID, abrutaData, "17-13");
		abrutaParad.setStemCutFactor(5);
		abrutaParad.setAugment(true);
		paradigmMap.put("abrūta", abrutaParad);

		// eti
		final String[][] etiData = {
			{ "eti", "itaḥ", "yanti" },
			{ "eṣi", "ithaḥ", "itha" },
			{ "emi", "ivaḥ", "imaḥ" } };
		final VerbalParadigm etiParad = VerbalParadigm.generate("eti", "eti", TenseMood.PRES, Pada.ACT, etiData, "17-14");
		etiParad.setStemCutFactor(3);
		paradigmMap.put("eti", etiParad);
		final String[][] iyatData = {
			{ "iyāt", "iyātām", "iyuḥ" },
			{ "iyāḥ", "iyātam", "iyāta" },
			{ "iyām", "iyāva", "iyāma" } };
		final VerbalParadigm iyatParad = VerbalParadigm.generate("iyāt", "eti", TenseMood.OPT, Pada.ACT, iyatData, "17-14");
		iyatParad.setStemCutFactor(3);
		paradigmMap.put("iyāt", iyatParad);
		final String[][] etuData = {
			{ "etu", "itām", "yantu" },
			{ "ihi", "itam", "ita" },
			{ "ayāni", "ayāva", "ayāma" } };
		final VerbalParadigm etuParad = VerbalParadigm.generate("etu", "eti", TenseMood.IMP, Pada.ACT, etuData, "17-14");
		etuParad.setStemCutFactor(3);
		paradigmMap.put("etu", etuParad);
		final String[][] aitData = {
			{ "ait", "aitām", "āyan" },
			{ "aiḥ", "aitam", "aita" },
			{ "āyam", "aiva", "aima" } };
		final VerbalParadigm aitParad = VerbalParadigm.generate("ait", "eti", TenseMood.IMPERF, Pada.ACT, aitData, "17-14");
		aitParad.setStemCutFactor(3);
		paradigmMap.put("ait", aitParad);

		// adhīte
		final String[][] adhiteData = {
			{ "īte", "īyāte", "īyate" },
			{ "īṣe", "īyāthe", "īdhve" },
			{ "īye", "īvahe", "īmahe" } };
		final VerbalParadigm adhiteParad = VerbalParadigm.generate("adhīte", "adhīte", TenseMood.PRES, Pada.MID, adhiteData, "17-14");
		adhiteParad.setStemCutFactor(3);
		paradigmMap.put("adhīte", adhiteParad);
		final String[][] adhiyitaData = {
			{ "īyīta", "īyīyātām", "īyīran" },
			{ "īyīthāḥ", "īyīyāthām", "īyīdhvam" },
			{ "īyīya", "īyīvahi", "īyīmahi" } };
		final VerbalParadigm adhiyitaParad = VerbalParadigm.generate("adhīyīta", "adhīte", TenseMood.OPT, Pada.MID, adhiyitaData, "17-14");
		adhiyitaParad.setStemCutFactor(3);
		paradigmMap.put("adhīyīta", adhiyitaParad);
		final String[][] adhitamData = {
			{ "ītām", "īyātām", "īyatām" },
			{ "īṣva", "īyāthām", "īdhvam" },
			{ "yayai", "yayāvahai", "yayāmahai" } };
		final VerbalParadigm adhitamParad = VerbalParadigm.generate("adhītām", "adhīte", TenseMood.IMP, Pada.MID, adhitamData, "17-14");
		adhitamParad.setStemCutFactor(3);
		paradigmMap.put("adhītām", adhitamParad);
		final String[][] adhyaitaData = {
			{ "yaita", "yaiyātām", "yaiyata" },
			{ "yaithāḥ", "yaiyāthām", "yaidhvam" },
			{ "yaiyi", "yaivahi", "yaimahi" } };
		final VerbalParadigm adhyaitaParad = VerbalParadigm.generate("adhyaita", "adhīte", TenseMood.IMPERF, Pada.MID, adhyaitaData, "17-14");
		adhyaitaParad.setStemCutFactor(3);
		paradigmMap.put("adhyaita", adhyaitaParad);

		// karoti
		final String[][] karotiData = {
			{ "aroti", "urutaḥ", "urvanti" },
			{ "aroṣi", "uruthaḥ", "urutha" },
			{ "aromi", "urvaḥ", "urmaḥ" } };
		final VerbalParadigm karotiParad = VerbalParadigm.generate("karoti", "karoti", TenseMood.PRES, Pada.ACT, karotiData, "17-15");
		karotiParad.setStemCutFactor(5);
		paradigmMap.put("karoti", karotiParad);
		final String[][] kuryatData = {
			{ "uryāt", "uryātām", "uryuḥ" },
			{ "uryāḥ", "uryātam", "uryāta" },
			{ "uryām", "uryāva", "uryāma" } };
		final VerbalParadigm kuryatParad = VerbalParadigm.generate("kuryāt", "karoti", TenseMood.OPT, Pada.ACT, kuryatData, "17-15");
		kuryatParad.setStemCutFactor(5);
		paradigmMap.put("kuryāt", kuryatParad);
		final String[][] karotuData = {
			{ "arotu", "urutām", "urvantu" },
			{ "uru", "urutam", "uruta" },
			{ "aravāṇi", "aravāva", "aravāma" } };
		final VerbalParadigm karotuParad = VerbalParadigm.generate("karotu", "karoti", TenseMood.IMP, Pada.ACT, karotuData, "17-15");
		karotuParad.setStemCutFactor(5);
		paradigmMap.put("karotu", karotuParad);
		final String[][] akarotData = {
			{ "arot", "urutām", "urvan" },
			{ "aroḥ", "urutam", "uruta" },
			{ "aravam", "urva", "urma" } };
		final VerbalParadigm akarotParad = VerbalParadigm.generate("akarot", "karoti", TenseMood.IMPERF, Pada.ACT, akarotData, "17-15");
		akarotParad.setStemCutFactor(5);
		akarotParad.setAugment(true);
		paradigmMap.put("akarot", akarotParad);
		final String[][] kuruteData = {
			{ "urute", "urvāte", "urvate" },
			{ "uruṣe", "urvāthe", "urudhve" },
			{ "urve", "urvahe", "urmahe" } };
		final VerbalParadigm kuruteParad = VerbalParadigm.generate("kurute", "karoti", TenseMood.PRES, Pada.MID, kuruteData, "17-15");
		kuruteParad.setStemCutFactor(5);
		paradigmMap.put("kurute", kuruteParad);
		final String[][] kurvitaData = {
			{ "urvīta", "urvīyātām", "urvīran" },
			{ "urvīthāḥ", "urvīyāthām", "urvīdhvam" },
			{ "urvīya", "urvīvahi", "urvīmahi" } };
		final VerbalParadigm kurvitaParad = VerbalParadigm.generate("kurvīta", "karoti", TenseMood.OPT, Pada.MID, kurvitaData, "17-15");
		kurvitaParad.setStemCutFactor(5);
		paradigmMap.put("kurvīta", kurvitaParad);
		final String[][] kurutamData = {
			{ "urutām", "urvātām", "urvatām" },
			{ "uruṣva", "urvāthām", "urudhvam" },
			{ "aravai", "aravāvahai", "aravāmahai" } };
		final VerbalParadigm kurutamParad = VerbalParadigm.generate("kurutām", "karoti", TenseMood.IMP, Pada.MID, kurutamData, "17-15");
		kurutamParad.setStemCutFactor(5);
		paradigmMap.put("kurutām", kurutamParad);
		final String[][] akurutaData = {
			{ "uruta", "urvātām", "urvata" },
			{ "uruthāḥ", "urvāthām", "urudhvam" },
			{ "urvi", "urvahi", "urmahi" } };
		final VerbalParadigm akurutaParad = VerbalParadigm.generate("akuruta", "karoti", TenseMood.IMPERF, Pada.MID, akurutaData, "17-15");
		akurutaParad.setStemCutFactor(5);
		akurutaParad.setAugment(true);
		paradigmMap.put("akuruta", akurutaParad);

		// dadhāti
		final String[][] dadhatiData = {
			{ "dadhāti", "dhattaḥ", "dadhati" },
			{ "dadhāsi", "dhatthaḥ", "dhattha" },
			{ "dadhāmi", "dadhvaḥ", "dadhmaḥ" } };
		final VerbalParadigm dadhatiParad = VerbalParadigm.generate("dadhāti", "dadhāti", TenseMood.PRES, Pada.ACT, dadhatiData, "17-16");
		dadhatiParad.setStemCutFactor(7);
		paradigmMap.put("dadhāti", dadhatiParad);
		final String[][] dadhyatData = {
			{ "dadhyāt", "dadhyātām", "dadhyuḥ" },
			{ "dadhyāḥ", "dadhyātam", "dadhyāta" },
			{ "dadhyām", "dadhyāva", "dadhyāma" } };
		final VerbalParadigm dadhyatParad = VerbalParadigm.generate("dadhyāt", "dadhāti", TenseMood.OPT, Pada.ACT, dadhyatData, "17-16");
		dadhyatParad.setStemCutFactor(7);
		paradigmMap.put("dadhyāt", dadhyatParad);
		final String[][] dadhatuData = {
			{ "dadhātu", "dhattām", "dadhatu" },
			{ "dhehi", "dhattam", "dhatta" },
			{ "dadhāni", "dadhāva", "dadhāma" } };
		final VerbalParadigm dadhatuParad = VerbalParadigm.generate("dadhātu", "dadhāti", TenseMood.IMP, Pada.ACT, dadhatuData, "17-16");
		dadhatuParad.setStemCutFactor(7);
		paradigmMap.put("dadhātu", dadhatuParad);
		final String[][] adadhatData = {
			{ "adadhāt", "adhattām", "adadhuḥ" },
			{ "adadhāḥ", "adhattam", "adhatta" },
			{ "adadhām", "adadhva", "adadhma" } };
		final VerbalParadigm adadhatParad = VerbalParadigm.generate("adadhāt", "dadhāti", TenseMood.IMPERF, Pada.ACT, adadhatData, "17-16");
		adadhatParad.setStemCutFactor(7);
		paradigmMap.put("adadhāt", adadhatParad);
		final String[][] dhatteData = {
			{ "dhatte", "dadhāte", "dadhate" },
			{ "dhatse", "dadhāthe", "dhaddhve" },
			{ "dadhe", "dadhvahe", "dadhmahe" } };
		final VerbalParadigm dhatteParad = VerbalParadigm.generate("dhatte", "dadhāti", TenseMood.PRES, Pada.MID, dhatteData, "17-16");
		dhatteParad.setStemCutFactor(7);
		paradigmMap.put("dhatte", dhatteParad);
		final String[][] dadhitaData = {
			{ "dadhīta", "dadhīyātām", "dadhīran" },
			{ "dadhīthāḥ", "dadhīyāthām", "dadhīdhvam" },
			{ "dadhīya", "dadhīvahi", "dadhīmahi" } };
		final VerbalParadigm dadhitaParad = VerbalParadigm.generate("dadhīta", "dadhāti", TenseMood.OPT, Pada.MID, dadhitaData, "17-16");
		dadhitaParad.setStemCutFactor(7);
		paradigmMap.put("dadhīta", dadhitaParad);
		final String[][] dhattamData = {
			{ "dhattām", "dadhātām", "dadhatām" },
			{ "dhatsva", "dadhāthām", "dhaddhvam" },
			{ "dadhai", "dadhāvahai", "dadhāmahai" } };
		final VerbalParadigm dhattamParad = VerbalParadigm.generate("dhattām", "dadhāti", TenseMood.IMP, Pada.MID, dhattamData, "17-16");
		dhattamParad.setStemCutFactor(7);
		paradigmMap.put("dhattām", dhattamParad);
		final String[][] adhattaData = {
			{ "adhatta", "adadhātām", "adadhata" },
			{ "adhatthāḥ", "adadhāthām", "adhaddhvam" },
			{ "adadhi", "adadhvahi", "adadhmahi" } };
		final VerbalParadigm adhattaParad = VerbalParadigm.generate("adhatta", "dadhāti", TenseMood.IMPERF, Pada.MID, adhattaData, "17-16");
		adhattaParad.setStemCutFactor(7);
		paradigmMap.put("adhatta", adhattaParad);

		// dadāti, from Deshpande's Primer, p. 225
		final String[][] dadatiData = {
			{ "dadāti", "dattaḥ", "dadati" },
			{ "dadāsi", "datthaḥ", "dattha" },
			{ "dadāmi", "dadvaḥ", "dadmaḥ" } };
		final VerbalParadigm dadatiParad = VerbalParadigm.generate("dadāti", "dadāti", TenseMood.PRES, Pada.ACT, dadatiData, "17-16");
		dadatiParad.setStemCutFactor(6);
		paradigmMap.put("dadāti", dadatiParad);
		final String[][] dadyatData = {
			{ "dadyāt", "dadyātām", "dadyuḥ" },
			{ "dadyāḥ", "dadyātam", "dadyāta" },
			{ "dadyām", "dadyāva", "dadyāma" } };
		final VerbalParadigm dadyatParad = VerbalParadigm.generate("dadyāt", "dadāti", TenseMood.OPT, Pada.ACT, dadyatData, "17-16");
		dadyatParad.setStemCutFactor(6);
		paradigmMap.put("dadyāt", dadyatParad);
		final String[][] dadatuData = {
			{ "dadātu", "dattām", "dadatu" },
			{ "dehi", "dattam", "datta" },
			{ "dadāni", "dadāva", "dadāma" } };
		final VerbalParadigm dadatuParad = VerbalParadigm.generate("dadātu", "dadāti", TenseMood.IMP, Pada.ACT, dadatuData, "17-16");
		dadatuParad.setStemCutFactor(6);
		paradigmMap.put("dadātu", dadatuParad);
		final String[][] adadatData = {
			{ "adadāt", "adattām", "adaduḥ" },
			{ "adadāḥ", "adattam", "adatta" },
			{ "adadām", "adadva", "adadma" } };
		final VerbalParadigm adadatParad = VerbalParadigm.generate("adadāt", "dadāti", TenseMood.IMPERF, Pada.ACT, adadatData, "17-16");
		adadatParad.setStemCutFactor(6);
		paradigmMap.put("adadāt", adadatParad);
		final String[][] datteData = {
			{ "datte", "dadāte", "dadate" },
			{ "datse", "dadāthe", "daddhve" },
			{ "dade", "dadvahe", "dadmahe" } };
		final VerbalParadigm datteParad = VerbalParadigm.generate("datte", "dadāti", TenseMood.PRES, Pada.MID, datteData, "17-16");
		datteParad.setStemCutFactor(6);
		paradigmMap.put("datte", datteParad);
		final String[][] daditaData = {
			{ "dadīta", "dadīyātām", "dadīran" },
			{ "dadīthāḥ", "dadīyāthām", "dadīdhvam" },
			{ "dadīya", "dadīvahi", "dadīmahi" } };
		final VerbalParadigm daditaParad = VerbalParadigm.generate("dadīta", "dadāti", TenseMood.OPT, Pada.MID, daditaData, "17-16");
		daditaParad.setStemCutFactor(6);
		paradigmMap.put("dadīta", daditaParad);
		final String[][] dattamData = {
			{ "dattām", "dadātām", "dadatām" },
			{ "datsva", "dadāthām", "daddhvam" },
			{ "dadai", "dadāvahai", "dadāmahai" } };
		final VerbalParadigm dattamParad = VerbalParadigm.generate("dattām", "dadāti", TenseMood.IMP, Pada.MID, dattamData, "17-16");
		dattamParad.setStemCutFactor(6);
		paradigmMap.put("dattām", dattamParad);
		final String[][] adattaData = {
			{ "adatta", "adadātām", "adadata" },
			{ "adatthāḥ", "adadāthām", "adaddhvam" },
			{ "adadi", "adadvahi", "adadmahi" } };
		final VerbalParadigm adattaParad = VerbalParadigm.generate("adatta", "dadāti", TenseMood.IMPERF, Pada.MID, adattaData, "17-16");
		adattaParad.setStemCutFactor(6);
		paradigmMap.put("adatta", adattaParad);

		// jahāti
		final String[][] jahatiData = {
			{ "āti", "ītaḥ", "ati" },
			{ "āsi", "īthaḥ", "ītha" },
			{ "āmi", "īvaḥ", "īmaḥ" } };
		final VerbalParadigm jahatiParad = VerbalParadigm.generate("jahāti", "jahāti", TenseMood.PRES, Pada.ACT, jahatiData, "17-17");
		paradigmMap.put("jahāti", jahatiParad);
		final String[][] jahyatData = {
			{ "yāt", "yātām", "yuḥ" },
			{ "yāḥ", "yātam", "yāta" },
			{ "yām", "yāva", "yāma" } };
		final VerbalParadigm jahyatParad = VerbalParadigm.generate("jahyāt", "jahāti", TenseMood.OPT, Pada.ACT, jahyatData, "17-17");
		paradigmMap.put("jahyāt", jahyatParad);
		final String[][] jahatuData = {
			{ "ātu", "ītām", "atu" },
			{ "īhi", "ītam", "īta" },
			{ "āni", "āva", "āma" } };
		final VerbalParadigm jahatuParad = VerbalParadigm.generate("jahātu", "jahāti", TenseMood.IMP, Pada.ACT, jahatuData, "17-17");
		paradigmMap.put("jahātu", jahatuParad);
		final String[][] ajahatData = {
			{ "āt", "ītām", "uḥ" },
			{ "āḥ", "ītam", "īta" },
			{ "ām", "īva", "īma" } };
		final VerbalParadigm ajahatParad = VerbalParadigm.generate("ajahāt", "jahāti", TenseMood.IMPERF, Pada.ACT, ajahatData, "17-17");
		ajahatParad.setAugment(true);
		paradigmMap.put("ajahāt", ajahatParad);

		// mimīte
		final String[][] mimiteData = {
			{ "īte", "āte", "ate" },
			{ "īṣe", "āthe", "īdhve" },
			{ "e", "īvahe", "īmahe" } };
		final VerbalParadigm mimiteParad = VerbalParadigm.generate("mimīte", "mimīte", TenseMood.PRES, Pada.MID, mimiteData, "17-18");
		paradigmMap.put("mimīte", mimiteParad);
		final String[][] mimitaData = {
			{ "īta", "īyātām", "īran" },
			{ "īthāḥ", "īyāthām", "īdhvam" },
			{ "īya", "īvahi", "īmahi" } };
		final VerbalParadigm mimitaParad = VerbalParadigm.generate("mimīta", "mimīte", TenseMood.OPT, Pada.MID, mimitaData, "17-18");
		paradigmMap.put("mimīta", mimitaParad);
		final String[][] mimitamData = {
			{ "ītām", "ātām", "atām" },
			{ "īṣva", "āthām", "īdhvam" },
			{ "ai", "āvahai", "āmahai" } };
		final VerbalParadigm mimitamParad = VerbalParadigm.generate("mimītām", "mimīte", TenseMood.IMP, Pada.MID, mimitamData, "17-18");
		paradigmMap.put("mimītām", mimitamParad);
		final String[][] amimitaData = {
			{ "īta", "ātām", "ata" },
			{ "īthāḥ", "āthām", "īdhvam" },
			{ "i", "īvahi", "īmahi" } };
		final VerbalParadigm amimitaParad = VerbalParadigm.generate("amimīta", "mimīte", TenseMood.IMPERF, Pada.MID, amimitaData, "17-18");
		amimitaParad.setAugment(true);
		paradigmMap.put("amimīta", amimitaParad);

		// śete
		final String[][] seteData = {
			{ "ete", "ayāte", "erate" },
			{ "eṣe", "ayāthe", "edhve" },
			{ "aye", "evahe", "emahe" } };
		final VerbalParadigm seteParad = VerbalParadigm.generate("śete", "śete", TenseMood.PRES, Pada.MID, seteData, "17-19");
		paradigmMap.put("śete", seteParad);
		final String[][] sayitaData = {
			{ "ayīta", "ayīyātām", "ayīran" },
			{ "ayīthāḥ", "ayīyāthām", "ayīdhvam" },
			{ "ayīya", "ayīvahi", "ayīmahi" } };
		final VerbalParadigm sayitaParad = VerbalParadigm.generate("śayīta", "śete", TenseMood.OPT, Pada.MID, sayitaData, "17-19");
		paradigmMap.put("śayīta", sayitaParad);
		final String[][] setamData = {
			{ "etām", "ayātām", "eratām" },
			{ "eṣva", "ayāthām", "edhvam" },
			{ "ayai", "ayāvahai", "ayāmahai" } };
		final VerbalParadigm setamParad = VerbalParadigm.generate("śetām", "śete", TenseMood.IMP, Pada.MID, setamData, "17-19");
		paradigmMap.put("śetām", setamParad);
		final String[][] asetaData = {
			{ "eta", "ayātām", "erata" },
			{ "ethāḥ", "ayāthām", "edhvam" },
			{ "ayi", "evahi", "emahi" } };
		final VerbalParadigm asetaParad = VerbalParadigm.generate("aśeta", "śete", TenseMood.IMPERF, Pada.MID, asetaData, "17-19");
		asetaParad.setAugment(true);
		paradigmMap.put("aśeta", asetaParad);

		// asti
		final String[][] astiData = {
			{ "asti", "staḥ", "santi" },
			{ "asi", "sthaḥ", "stha" },
			{ "asmi", "svaḥ", "smaḥ" } };
		final VerbalParadigm astiParad = VerbalParadigm.generate("asti", "asti", TenseMood.PRES, Pada.ACT, astiData, "17-20");
		astiParad.setStemCutFactor(4);
		paradigmMap.put("asti", astiParad);
		final String[][] syatData = {
			{ "syāt", "syātām", "syuḥ" },
			{ "syāḥ", "syātam", "syāta" },
			{ "syām", "syāva", "syāma" } };
		final VerbalParadigm syatParad = VerbalParadigm.generate("syāt", "asti", TenseMood.OPT, Pada.ACT, syatData, "17-20");
		syatParad.setStemCutFactor(4);
		paradigmMap.put("syāt", syatParad);
		final String[][] astuData = {
			{ "astu", "stām", "santu" },
			{ "edhi", "stam", "sta" },
			{ "asāni", "asāva", "asāma" } };
		final VerbalParadigm astuParad = VerbalParadigm.generate("astu", "asti", TenseMood.IMP, Pada.ACT, astuData, "17-20");
		astuParad.setStemCutFactor(4);
		paradigmMap.put("astu", astuParad);
		final String[][] asitData = {
			{ "āsīt", "āstām", "āsan" },
			{ "āsīḥ", "āstam", "āsta" },
			{ "āsam", "āsva", "āsma" } };
		final VerbalParadigm asitParad = VerbalParadigm.generate("āsīt", "asti", TenseMood.IMPERF, Pada.ACT, asitData, "17-20");
		asitParad.setStemCutFactor(4);
		paradigmMap.put("āsīt", asitParad);
		final String[][] steData = {
			{ "ste", "sāte", "sate" },
			{ "se", "sāthe", "dhve" },
			{ "he", "svahe", "smahe" } };
		final VerbalParadigm steParad = VerbalParadigm.generate("ste", "asti", TenseMood.PRES, Pada.MID, steData, "17-20");
		steParad.setStemCutFactor(4);
		paradigmMap.put("ste", steParad);

		// āste
		final String[][] asteData = {
			{ "ste", "sāte", "sate" },
			{ "sse", "sāthe", "dhve" },
			{ "se", "svahe", "smahe" } };
		final VerbalParadigm asteParad = VerbalParadigm.generate("āste", "āste", TenseMood.PRES, Pada.MID, asteData, "17-21");
		asteParad.addEndings(Person.MADHYAMA, Number.PLU, "ddhve");
		paradigmMap.put("āste", asteParad);
		final String[][] asitaData = {
			{ "sīta", "sīyātām", "sīran" },
			{ "sīthāḥ", "sīyāthām", "sīdhvam" },
			{ "sīya", "sīvahi", "sīmahi" } };
		final VerbalParadigm asitaParad = VerbalParadigm.generate("āsīta", "āste", TenseMood.OPT, Pada.MID, asitaData, "17-21");
		paradigmMap.put("āsīta", asitaParad);
		final String[][] astamData = {
			{ "stām", "sātām", "satām" },
			{ "ssva", "sāthām", "dhvam" },
			{ "sai", "sāvahai", "sāmahai" } };
		final VerbalParadigm astamParad = VerbalParadigm.generate("āstām", "āste", TenseMood.IMP, Pada.MID, astamData, "17-21");
		astamParad.addEndings(Person.MADHYAMA, Number.PLU, "ddhvam");
		paradigmMap.put("āstām", astamParad);
		final String[][] astaData = {
			{ "sta", "sātām", "sata" },
			{ "sthāḥ", "sāthām", "dhvam" },
			{ "si", "svahi", "smahi" } };
		final VerbalParadigm astaParad = VerbalParadigm.generate("āsta", "āste", TenseMood.IMPERF, Pada.MID, astaData, "17-21");
		astaParad.addEndings(Person.MADHYAMA, Number.PLU, "ddhvam");
		paradigmMap.put("āsta", astaParad);

		// śāsti
		final String[][] sastiData = {
			{ "āsti", "iṣṭaḥ", "āsati" },
			{ "āssi", "iṣṭhaḥ", "iṣṭha" },
			{ "āsmi", "iṣvaḥ", "iṣmaḥ" } };
		final VerbalParadigm sastiParad = VerbalParadigm.generate("śāsti", "śāsti", TenseMood.PRES, Pada.ACT, sastiData, "17-22");
		sastiParad.setStemCutFactor(4);
		paradigmMap.put("śāsti", sastiParad);
		final String[][] sisyatData = {
			{ "iṣyāt", "iṣyātām", "iṣyuḥ" },
			{ "iṣyāḥ", "iṣyātam", "iṣyāta" },
			{ "iṣyām", "iṣyāva", "iṣyāma" } };
		final VerbalParadigm sisyatParad = VerbalParadigm.generate("śiṣyāt", "śāsti", TenseMood.OPT, Pada.ACT, sisyatData, "17-22");
		sisyatParad.setStemCutFactor(4);
		paradigmMap.put("śiṣyāt", sisyatParad);
		final String[][] sastuData = {
			{ "āstu", "iṣṭām", "āsatu" },
			{ "ādhi", "iṣṭam", "iṣṭa" },
			{ "āsāni", "āsāva", "āsāma" } };
		final VerbalParadigm sastuParad = VerbalParadigm.generate("śāstu", "śāsti", TenseMood.IMP, Pada.ACT, sastuData, "17-22");
		sastuParad.setStemCutFactor(4);
		paradigmMap.put("śāstu", sastuParad);
		final String[][] asatData = {
			{ "āt", "iṣṭām", "āsuḥ" },
			{ "āt", "iṣṭam", "iṣṭa" },
			{ "āsam", "iṣva", "iṣma" } };
		final VerbalParadigm asatParad = VerbalParadigm.generate("aśāt", "śāsti", TenseMood.IMPERF, Pada.ACT, asatData, "17-22");
		asatParad.setStemCutFactor(4);
		asatParad.setAugment(true);
		asatParad.addEndings(Person.MADHYAMA, Number.SING, "āḥ");
		paradigmMap.put("aśāt", asatParad);

		// hanti
		final String[][] hantiData = {
			{ "hanti", "hataḥ", "ghnanti" },
			{ "haṃsi", "hathaḥ", "hatha" },
			{ "hanmi", "hanvaḥ", "hanmaḥ" } };
		final VerbalParadigm hantiParad = VerbalParadigm.generate("hanti", "hanti", TenseMood.PRES, Pada.ACT, hantiData, "17-23");
		hantiParad.setStemCutFactor(5);
		paradigmMap.put("hanti", hantiParad);
		final String[][] hanyatData = {
			{ "hanyāt", "hanyātām", "hanyuḥ" },
			{ "hanyāḥ", "hanyātam", "hanyāta" },
			{ "hanyām", "hanyāva", "hanyāma" } };
		final VerbalParadigm hanyatParad = VerbalParadigm.generate("hanyāt", "hanti", TenseMood.OPT, Pada.ACT, hanyatData, "17-23");
		hanyatParad.setStemCutFactor(5);
		paradigmMap.put("hanyāt", hanyatParad);
		final String[][] hantuData = {
			{ "hantu", "hatām", "ghnantu" },
			{ "jahi", "hatam", "hata" },
			{ "hanāni", "hanāva", "hanāma" } };
		final VerbalParadigm hantuParad = VerbalParadigm.generate("hantu", "hanti", TenseMood.IMP, Pada.ACT, hantuData, "17-23");
		hantuParad.setStemCutFactor(5);
		paradigmMap.put("hantu", hantuParad);
		final String[][] ahanData = {
			{ "ahan", "ahatām", "aghnan" },
			{ "ahan", "ahatam", "ahata" },
			{ "ahanam", "ahanva", "ahanma" } };
		final VerbalParadigm ahanParad = VerbalParadigm.generate("ahan", "hanti", TenseMood.IMPERF, Pada.ACT, ahanData, "17-23");
		ahanParad.setStemCutFactor(5);
		paradigmMap.put("ahan", ahanParad);

		// dogdhi
		final String[][] dogdhiData = {
			{ "dogdhi", "dugdhaḥ", "duhanti" },
			{ "dhokṣi", "dugdhaḥ", "dugdha" },
			{ "dohmi", "duhvaḥ", "duhmaḥ" } };
		final VerbalParadigm dogdhiParad = VerbalParadigm.generate("dogdhi", "dogdhi", TenseMood.PRES, Pada.ACT, dogdhiData, "17-24");
		dogdhiParad.setStemCutFactor(6);
		paradigmMap.put("dogdhi", dogdhiParad);
		final String[][] duhyatData = {
			{ "duhyāt", "duhyātām", "duhyuḥ" },
			{ "duhyāḥ", "duhyātam", "duhyāta" },
			{ "duhyām", "duhyāva", "duhyāma" } };
		final VerbalParadigm duhyatParad = VerbalParadigm.generate("duhyāt", "dogdhi", TenseMood.OPT, Pada.ACT, duhyatData, "17-24");
		duhyatParad.setStemCutFactor(6);
		paradigmMap.put("duhyāt", duhyatParad);
		final String[][] dogdhuData = {
			{ "dogdhu", "dugdhām", "duhantu" },
			{ "dugdhi", "dugdham", "dugdha" },
			{ "dohāni", "dohāva", "dohāma" } };
		final VerbalParadigm dogdhuParad = VerbalParadigm.generate("dogdhu", "dogdhi", TenseMood.IMP, Pada.ACT, dogdhuData, "17-24");
		dogdhuParad.setStemCutFactor(6);
		paradigmMap.put("dogdhu", dogdhuParad);
		final String[][] adhokData = {
			{ "adhok", "adugdhām", "aduhan" },
			{ "adhok", "adugdham", "adugdha" },
			{ "adoham", "aduhva", "aduhma" } };
		final VerbalParadigm adhokParad = VerbalParadigm.generate("adhok", "dogdhi", TenseMood.IMPERF, Pada.ACT, adhokData, "17-24");
		adhokParad.setStemCutFactor(6);
		paradigmMap.put("adhok", adhokParad);
		final String[][] dugdheData = {
			{ "dugdhe", "duhāte", "duhate" },
			{ "dhukṣe", "duhāthe", "dhugdhve" },
			{ "duhe", "duhvahe", "duhmahe" } };
		final VerbalParadigm dugdheParad = VerbalParadigm.generate("dugdhe", "dogdhi", TenseMood.PRES, Pada.MID, dugdheData, "17-24");
		dugdheParad.setStemCutFactor(6);
		paradigmMap.put("dugdhe", dugdheParad);
		final String[][] duhitaData = {
			{ "duhīta", "duhīyātām", "duhīran" },
			{ "duhīthāḥ", "duhīyāthām", "duhīdhvam" },
			{ "duhīya", "duhīvahi", "duhīmahi" } };
		final VerbalParadigm duhitaParad = VerbalParadigm.generate("duhīta", "dogdhi", TenseMood.OPT, Pada.MID, duhitaData, "17-24");
		duhitaParad.setStemCutFactor(6);
		paradigmMap.put("duhīta", duhitaParad);
		final String[][] dugdhamData = {
			{ "dugdhām", "duhātām", "duhatām" },
			{ "dhukṣva", "duhāthām", "dhugdhvam" },
			{ "dohai", "dohāvahai", "dohāmahai" } };
		final VerbalParadigm dugdhamParad = VerbalParadigm.generate("dugdhām", "dogdhi", TenseMood.IMP, Pada.MID, dugdhamData, "17-24");
		dugdhamParad.setStemCutFactor(6);
		paradigmMap.put("dugdhām", dugdhamParad);
		final String[][] adugdhaData = {
			{ "adugdha", "aduhātām", "aduhata" },
			{ "adugdhāḥ", "aduhāthām", "adhugdhvam" },
			{ "aduhi", "aduhvahi", "aduhmahi" } };
		final VerbalParadigm adugdhaParad = VerbalParadigm.generate("adugdha", "dogdhi", TenseMood.IMPERF, Pada.MID, adugdhaData, "17-24");
		adugdhaParad.setStemCutFactor(6);
		paradigmMap.put("adugdha", adugdhaParad);

		// leḍhi
		final String[][] ledhiData = {
			{ "leḍhi", "līḍhaḥ", "lihanti" },
			{ "lekṣi", "līḍhaḥ", "līḍha" },
			{ "lehmi", "lihvaḥ", "lihmaḥ" } };
		final VerbalParadigm ledhiParad = VerbalParadigm.generate("leḍhi", "leḍhi", TenseMood.PRES, Pada.ACT, ledhiData, "17-25");
		ledhiParad.setStemCutFactor(5);
		paradigmMap.put("leḍhi", ledhiParad);
		final String[][] lihyatData = {
			{ "lihyāt", "lihyātām", "lihyuḥ" },
			{ "lihyāḥ", "lihyātam", "lihyāta" },
			{ "lihyām", "lihyāva", "lihyāma" } };
		final VerbalParadigm lihyatParad = VerbalParadigm.generate("lihyāt", "leḍhi", TenseMood.OPT, Pada.ACT, lihyatData, "17-25");
		lihyatParad.setStemCutFactor(5);
		paradigmMap.put("lihyāt", lihyatParad);
		final String[][] ledhuData = {
			{ "leḍhu", "līḍhām", "lihantu" },
			{ "līḍhi", "līḍham", "līḍha" },
			{ "lehāni", "lehāva", "lehāma" } };
		final VerbalParadigm ledhuParad = VerbalParadigm.generate("leḍhu", "leḍhi", TenseMood.IMP, Pada.ACT, ledhuData, "17-25");
		ledhuParad.setStemCutFactor(5);
		paradigmMap.put("leḍhu", ledhuParad);
		final String[][] aletData = {
			{ "aleṭ", "alīḍhām", "alihan" },
			{ "aleṭ", "alīḍham", "alīḍha" },
			{ "aleham", "alihva", "alihma" } };
		final VerbalParadigm aletParad = VerbalParadigm.generate("aleṭ", "leḍhi", TenseMood.IMPERF, Pada.ACT, aletData, "17-25");
		aletParad.setStemCutFactor(5);
		paradigmMap.put("aleṭ", aletParad);
		final String[][] lidheData = {
			{ "līḍhe", "lihāte", "lihate" },
			{ "likṣe", "lihāthe", "līḍhve" },
			{ "lihe", "lihvahe", "lihmahe" } };
		final VerbalParadigm lidheParad = VerbalParadigm.generate("līḍhe", "leḍhi", TenseMood.PRES, Pada.MID, lidheData, "17-25");
		lidheParad.setStemCutFactor(5);
		paradigmMap.put("līḍhe", lidheParad);
		final String[][] lihitaData = {
			{ "lihīta", "lihīyātām", "lihīran" },
			{ "lihīthāḥ", "lihīyāthām", "lihīdhvam" },
			{ "lihīya", "lihīvahi", "lihīmahi" } };
		final VerbalParadigm lihitaParad = VerbalParadigm.generate("lihīta", "leḍhi", TenseMood.OPT, Pada.MID, lihitaData, "17-25");
		lihitaParad.setStemCutFactor(5);
		paradigmMap.put("lihīta", lihitaParad);
		final String[][] lidhamData = {
			{ "līḍhām", "lihātām", "lihatām" },
			{ "likṣva", "lihāthām", "līḍhvam" },
			{ "lehai", "lehāvahai", "lehāmahai" } };
		final VerbalParadigm lidhamParad = VerbalParadigm.generate("līḍhām", "leḍhi", TenseMood.IMP, Pada.MID, lidhamData, "17-25");
		lidhamParad.setStemCutFactor(5);
		paradigmMap.put("līḍhām", lidhamParad);
		final String[][] alidhaData = {
			{ "alīḍha", "alihātām", "alihata" },
			{ "alīḍhāḥ", "alihāthām", "alīḍhvam" },
			{ "alihi", "alihvahi", "alihmahi" } };
		final VerbalParadigm alidhaParad = VerbalParadigm.generate("alīḍha", "leḍhi", TenseMood.IMPERF, Pada.MID, alidhaData, "17-25");
		alidhaParad.setStemCutFactor(5);
		paradigmMap.put("alīḍha", alidhaParad);

		// perfect
		final String[][] jijivaData = {
			{ "īva", "īvatuḥ", "īvuḥ" },
			{ "īvitha", "īvathuḥ", "īva" },
			{ "īva", "īviva", "īvima" } };
		final VerbalParadigm jijivaParad = VerbalParadigm.generate("jijīva", "jijīva", TenseMood.PERF, Pada.ACT, jijivaData, "18-1");
		jijivaParad.setStemCutFactor(3);
		jijivaParad.addPerfectSubstitution("īv");
		paradigmMap.put("jijīva", jijivaParad);
		final String[][] jijiveData = {
			{ "īve", "īvāte", "īvire" },
			{ "īviṣe", "īvāthe", "īvidhve" },
			{ "īve", "īvivahe", "īvimahe" } };
		final VerbalParadigm jijiveParad = VerbalParadigm.generate("jijīve", "jijīva", TenseMood.PERF, Pada.MID, jijiveData, "18-1");
		jijiveParad.setStemCutFactor(3);
		jijiveParad.addPerfectSubstitution("īv");
		paradigmMap.put("jijīve", jijiveParad);

		final String[][] vivesaData = {
			{ "eśa", "iśatuḥ", "iśuḥ" },
			{ "eśitha", "iśathuḥ", "iśa" },
			{ "eśa", "iśiva", "iśima" } };
		final VerbalParadigm vivesaParad = VerbalParadigm.generate("viveśa", "viveśa", TenseMood.PERF, Pada.ACT, vivesaData, "18-2");
		vivesaParad.setStemCutFactor(3);
		vivesaParad.addPerfectSubstitution("eś", "iś");
		paradigmMap.put("viveśa", vivesaParad);
		final String[][] viviseData = {
			{ "iśe", "iśāte", "iśire" },
			{ "iśiṣe", "iśāthe", "iśidhve" },
			{ "iśe", "iśivahe", "iśimahe" } };
		final VerbalParadigm viviseParad = VerbalParadigm.generate("viviśe", "viveśa", TenseMood.PERF, Pada.MID, viviseData, "18-2");
		viviseParad.setStemCutFactor(3);
		viviseParad.addPerfectSubstitution("iś");
		paradigmMap.put("viviśe", viviseParad);

		final String[][] iyesaData = {
			{ "iyeṣa", "īṣatuḥ", "īṣuḥ" },
			{ "iyeṣitha", "īṣathuḥ", "īṣa" },
			{ "iyeṣa", "īṣiva", "īṣima" } };
		final VerbalParadigm iyesaParad = VerbalParadigm.generate("iyeṣa", "iyeṣa", TenseMood.PERF, Pada.ACT, iyesaData, "18-3");
		iyesaParad.setStemCutFactor(5);
		paradigmMap.put("iyeṣa", iyesaParad);
		final String[][] iseData = {
			{ "īṣe", "īṣāte", "īṣire" },
			{ "īṣiṣe", "īṣāthe", "īṣidhve" },
			{ "īṣe", "īṣivahe", "īṣimahe" } };
		final VerbalParadigm iseParad = VerbalParadigm.generate("īṣe", "iyeṣa", TenseMood.PERF, Pada.MID, iseData, "18-3");
		iseParad.setStemCutFactor(5);
		paradigmMap.put("īṣe", iseParad);

		final String[][] ninayaData = {
			{ "āya", "yatuḥ", "yuḥ" },
			{ "ayitha", "yathuḥ", "ya" },
			{ "āya", "yiva", "yima" } };
		final VerbalParadigm ninayaParad = VerbalParadigm.generate("nināya", "nināya", TenseMood.PERF, Pada.ACT, ninayaData, "18-4");
		ninayaParad.setStemCutFactor(3);
		ninayaParad.addPerfectSubstitution("āy", "ay", "y");
		ninayaParad.addEndings(Person.MADHYAMA, Number.SING, "etha");
		paradigmMap.put("nināya", ninayaParad);
		final String[][] ninyeData = {
			{ "ye", "yāte", "yire" },
			{ "yiṣe", "yāthe", "yidhve" },
			{ "ye", "yivahe", "yimahe" } };
		final VerbalParadigm ninyeParad = VerbalParadigm.generate("ninye", "nināya", TenseMood.PERF, Pada.MID, ninyeData, "18-4");
		ninyeParad.setStemCutFactor(3);
		ninyeParad.addPerfectSubstitution("y");
		paradigmMap.put("ninye", ninyeParad);

		final String[][] lebheData = {
			{ "e", "āte", "ire" },
			{ "iṣe", "āthe", "idhve" },
			{ "e", "ivahe", "imahe" } };
		final VerbalParadigm lebheParad = VerbalParadigm.generate("lebhe", "lebhe", TenseMood.PERF, Pada.MID, lebheData);
		lebheParad.setStemCutFactor(1);
		paradigmMap.put("lebhe", lebheParad);

		final String[][] sisrayaData = {
			{ "āya", "iyatuḥ", "iyuḥ" },
			{ "ayitha", "iyathuḥ", "iya" },
			{ "āya", "iyiva", "iyima" } };
		final VerbalParadigm sisrayaParad = VerbalParadigm.generate("śiśrāya", "śiśrāya", TenseMood.PERF, Pada.ACT, sisrayaData, "18-5");
		sisrayaParad.setStemCutFactor(3);
		sisrayaParad.addPerfectSubstitution("āy", "ay", "iy");
		paradigmMap.put("śiśrāya", sisrayaParad);
		final String[][] sisriyeData = {
			{ "iye", "iyāte", "iyire" },
			{ "iyiṣe", "iyāthe", "iyidhve" },
			{ "iye", "iyivahe", "iyimahe" } };
		final VerbalParadigm sisriyeParad = VerbalParadigm.generate("śiśriye", "śiśrāya", TenseMood.PERF, Pada.MID, sisriyeData, "18-5");
		sisriyeParad.setStemCutFactor(3);
		sisriyeParad.addPerfectSubstitution("iy");
		paradigmMap.put("śiśriye", sisriyeParad);

		// for -āva, imitate 18-5 (see Bucknell's Manual p. 142)
		final String[][] juhavaData = {
			{ "āva", "ivatuḥ", "ivuḥ" },
			{ "avitha", "ivathuḥ", "iva" },
			{ "āva", "iviva", "ivima" } };
		final VerbalParadigm juhavaParad = VerbalParadigm.generate("juhāva", "juhāva", TenseMood.PERF, Pada.ACT, juhavaData, "18-5");
		juhavaParad.setStemCutFactor(3);
		paradigmMap.put("juhāva", juhavaParad);
		final String[][] juhiveData = {
			{ "ive", "ivāte", "ivire" },
			{ "iviṣe", "ivāthe", "ividhve" },
			{ "ive", "ivivahe", "ivimahe" } };
		final VerbalParadigm juhiveParad = VerbalParadigm.generate("juhive", "juhāva", TenseMood.PERF, Pada.MID, juhiveData, "18-5");
		juhiveParad.setStemCutFactor(3);
		paradigmMap.put("juhive", juhiveParad);

		final String[][] uvacaData = {
			{ "uvāca", "ūcatuḥ", "ūcuḥ" },
			{ "uvacitha", "ūcathuḥ", "ūca" },
			{ "uvāca", "ūciva", "ūcima" } };
		final VerbalParadigm uvacaParad = VerbalParadigm.generate("uvāca", "uvāca", TenseMood.PERF, Pada.ACT, uvacaData, "18-6");
		uvacaParad.setStemCutFactor(5);
		uvacaParad.addEndings(Person.MADHYAMA, Number.SING, "uvaktha");
		paradigmMap.put("uvāca", uvacaParad);
		final String[][] uceData = {
			{ "ūce", "ūcāte", "ūcire" },
			{ "ūciṣe", "ūcāthe", "ūcidhve" },
			{ "ūce", "ūcivahe", "ūcimahe" } };
		final VerbalParadigm uceParad = VerbalParadigm.generate("ūce", "uvāca", TenseMood.PERF, Pada.MID, uceData, "18-6");
		uceParad.setStemCutFactor(5);
		paradigmMap.put("ūce", uceParad);

		final String[][] tastaraData = {
			{ "āra", "aratuḥ", "aruḥ" },
			{ "aritha", "arathuḥ", "ara" },
			{ "āra", "ariva", "arima" } };
		final VerbalParadigm tastaraParad = VerbalParadigm.generate("tastāra", "tastāra", TenseMood.PERF, Pada.ACT, tastaraData, "18-7");
		tastaraParad.setStemCutFactor(3);
		paradigmMap.put("tastāra", tastaraParad);
		final String[][] tastareData = {
			{ "are", "arāte", "arire" },
			{ "ariṣe", "arāthe", "aridhve" },
			{ "are", "arivahe", "arimahe" } };
		final VerbalParadigm tastareParad = VerbalParadigm.generate("tastare", "tastāra", TenseMood.PERF, Pada.MID, tastareData, "18-7");
		tastareParad.setStemCutFactor(3);
		paradigmMap.put("tastare", tastareParad);

		final String[][] cakaraData = {
			{ "āra", "ratuḥ", "ruḥ" },
			{ "artha", "rathuḥ", "ra" },
			{ "āra", "ṛva", "ṛma" } };
		final VerbalParadigm cakaraParad = VerbalParadigm.generate("cakāra", "cakāra", TenseMood.PERF, Pada.ACT, cakaraData, "18-8");
		cakaraParad.setStemCutFactor(3);
		paradigmMap.put("cakāra", cakaraParad);
		final String[][] cakreData = {
			{ "re", "rāte", "rire" },
			{ "ṛṣe", "rāthe", "ṛḍhve" },
			{ "re", "ṛvahe", "ṛmahe" } };
		final VerbalParadigm cakreParad = VerbalParadigm.generate("cakre", "cakāra", TenseMood.PERF, Pada.MID, cakreData, "18-8");
		cakreParad.setStemCutFactor(3);
		paradigmMap.put("cakre", cakreParad);

		final String[][] tustavaData = {
			{ "āva", "uvatuḥ", "uvuḥ" },
			{ "otha", "uvathuḥ", "uva" },
			{ "āva", "uva", "uma" } };
		final VerbalParadigm tustavaParad = VerbalParadigm.generate("tuṣṭāva", "tuṣṭāva", TenseMood.PERF, Pada.ACT, tustavaData, "18-9");
		tustavaParad.setStemCutFactor(3);
		paradigmMap.put("tuṣṭāva", tustavaParad);
		final String[][] tustuveData = {
			{ "uve", "uvāte", "uvire" },
			{ "uṣe", "uvāthe", "udhve" },
			{ "uve", "uvahe", "umahe" } };
		final VerbalParadigm tustuveParad = VerbalParadigm.generate("tuṣṭuve", "tuṣṭāva", TenseMood.PERF, Pada.MID, tustuveData, "18-9");
		tustuveParad.setStemCutFactor(3);
		paradigmMap.put("tuṣṭuve", tustuveParad);

		final String[][] tatanaData = {
			{ "atāna", "enatuḥ", "enuḥ" },
			{ "enitha", "enathuḥ", "ena" },
			{ "atāna", "eniva", "enima" } };
		final VerbalParadigm tatanaParad = VerbalParadigm.generate("tatāna", "tatāna", TenseMood.PERF, Pada.ACT, tatanaData, "18-10");
		tatanaParad.setStemCutFactor(5);
		tatanaParad.addEndings(Person.MADHYAMA, Number.SING, "atantha");
		tatanaParad.addPerfectSubstitution("tān", "tan");
		paradigmMap.put("tatāna", tatanaParad);
		final String[][] teneData = {
			{ "ene", "enāte", "enire" },
			{ "eniṣe", "enāthe", "enidhve" },
			{ "ene", "enivahe", "enimahe" } };
		final VerbalParadigm teneParad = VerbalParadigm.generate("tene", "tatāna", TenseMood.PERF, Pada.MID, teneData, "18-10");
		teneParad.setStemCutFactor(5);
		paradigmMap.put("tene", teneParad);

		final String[][] dadhauData = {
			{ "au", "atuḥ", "uḥ" },
			{ "ātha", "athuḥ", "a" },
			{ "au", "iva", "ima" } };
		final VerbalParadigm dadhauParad = VerbalParadigm.generate("dadhau", "dadhau", TenseMood.PERF, Pada.ACT, dadhauData, "18-11");
		dadhauParad.setStemCutFactor(2);
		dadhauParad.addEndings(Person.MADHYAMA, Number.SING, "ita");
		paradigmMap.put("dadhau", dadhauParad);
		final String[][] dadheData = {
			{ "e", "āte", "ire" },
			{ "iṣe", "āthe", "idhve" },
			{ "e", "ivahe", "imahe" } };
		final VerbalParadigm dadheParad = VerbalParadigm.generate("dadhe", "dadhau", TenseMood.PERF, Pada.MID, dadheData, "18-11");
		dadheParad.setStemCutFactor(2);
		paradigmMap.put("dadhe", dadheParad);

		final String[][] ahaData = {
			{ "āha", "āhatuḥ", "āhuḥ" },
			{ "āttha", "āhathuḥ", "" },
			{ "", "", "" } };
		final VerbalParadigm ahaParad = VerbalParadigm.generate("āha", "āha", TenseMood.PERF, Pada.ACT, ahaData, "18-12");
		ahaParad.setStemCutFactor(3);
		paradigmMap.put("āha", ahaParad);

		final String[][] vedaData = {
			{ "eda", "idatuḥ", "iduḥ" },
			{ "ettha", "idathuḥ", "ida" },
			{ "eda", "idva", "idma" } };
		final VerbalParadigm vedaParad = VerbalParadigm.generate("veda", "veda", TenseMood.PERF, Pada.ACT, vedaData, "18-13");
		vedaParad.setStemCutFactor(3);
		paradigmMap.put("veda", vedaParad);

		final String[][] asaData = {
			{ "āsa", "āsatuḥ", "āsuḥ" },
			{ "āsitha", "āsathuḥ", "āsa" },
			{ "āsa", "āsiva", "āsima" } };
		final VerbalParadigm asaParad = VerbalParadigm.generate("āsa", "āsa", TenseMood.PERF, Pada.ACT, asaData, "21");
		asaParad.setStemCutFactor(3);
		paradigmMap.put("āsa", asaParad);

		// aorist
		final String[][] asicatData = {
			{ "at", "atām", "an" },
			{ "aḥ", "atam", "ata" },
			{ "am", "āva", "āma" } };
		final VerbalParadigm asicatParad = VerbalParadigm.generate("asicat", "asicat", TenseMood.AOR, Pada.ACT, asicatData, "22-1");
		asicatParad.setStemCutFactor(2);
		paradigmMap.put("asicat", asicatParad);
		final String[][] asicataData = {
			{ "ata", "etām", "anta" },
			{ "athāḥ", "ethām", "adhvam" },
			{ "e", "āvahi", "āmahi" } };
		final VerbalParadigm asicataParad = VerbalParadigm.generate("asicata", "asicat", TenseMood.AOR, Pada.MID, asicataData, "22-1");
		asicataParad.setStemCutFactor(2);
		paradigmMap.put("asicata", asicataParad);

		final String[][] adiksatData = {
			{ "at", "atām", "an" },
			{ "aḥ", "atam", "ata" },
			{ "am", "āva", "āma" } };
		final VerbalParadigm adiksatParad = VerbalParadigm.generate("adikṣat", "adikṣat", TenseMood.AOR, Pada.ACT, adiksatData, "22-2");
		adiksatParad.setStemCutFactor(2);
		paradigmMap.put("adikṣat", adiksatParad);
		final String[][] adiksataData = {
			{ "ata", "ātām", "anta" },
			{ "athāḥ", "āthām", "adhvam" },
			{ "i", "āvahi", "āmahi" } };
		final VerbalParadigm adiksataParad = VerbalParadigm.generate("adikṣata", "adikṣat", TenseMood.AOR, Pada.MID, adiksataData, "22-2");
		adiksataParad.setStemCutFactor(2);
		paradigmMap.put("adikṣata", adiksataParad);

		// -aiṣīt
		final String[][] anaisitData = {
			{ "īt", "ṭām", "uḥ" },
			{ "īḥ", "ṭam", "ṭa" },
			{ "am", "va", "ma" } };
		final VerbalParadigm anaisitParad = VerbalParadigm.generate("anaiṣīt", "anaiṣīt", TenseMood.AOR, Pada.ACT, anaisitData, "22-3");
		anaisitParad.setStemCutFactor(2);
		paradigmMap.put("anaiṣīt", anaisitParad);
		final String[][] anestaData = {
			{ "eṣṭa", "eṣātām", "eṣata" },
			{ "eṣṭhāḥ", "eṣāthām", "eḍhvam" },
			{ "eṣi", "eṣvahi", "eṣmahi" } };
		final VerbalParadigm anestaParad = VerbalParadigm.generate("aneṣṭa", "anaiṣīt", TenseMood.AOR, Pada.MID, anestaData, "22-3");
		anestaParad.setStemCutFactor(5);
		paradigmMap.put("aneṣṭa", anestaParad);
		
		// -auṣīt
		final String[][] asausitData = {
			{ "īt", "ṭām", "uḥ" },
			{ "īḥ", "ṭam", "ṭa" },
			{ "am", "va", "ma" } };
		final VerbalParadigm asausitParad = VerbalParadigm.generate("asauṣīt", "asauṣīt", TenseMood.AOR, Pada.ACT, asausitData, "22-3");
		asausitParad.setStemCutFactor(2);
		paradigmMap.put("asauṣīt", asausitParad);
		final String[][] asostaData = {
			{ "oṣṭa", "oṣātām", "oṣata" },
			{ "oṣṭhāḥ", "oṣāthām", "oḍhvam" },
			{ "oṣi", "oṣvahi", "oṣmahi" } };
		final VerbalParadigm asostaParad = VerbalParadigm.generate("asoṣṭa", "asauṣīt", TenseMood.AOR, Pada.MID, asostaData, "22-3");
		asostaParad.setStemCutFactor(5);
		paradigmMap.put("asoṣṭa", asostaParad);

		// -āsīt
		final String[][] ajnasitData = {
			{ "īt", "ṭām", "uḥ" },
			{ "īḥ", "ṭam", "ṭa" },
			{ "am", "va", "ma" } };
		final VerbalParadigm ajnasitParad = VerbalParadigm.generate("ajñāsīt", "ajñāsīt", TenseMood.AOR, Pada.ACT, ajnasitData, "22-3");
		ajnasitParad.setStemCutFactor(2);
		paradigmMap.put("ajñāsīt", ajnasitParad);
		final String[][] ajnastaData = {
			{ "āsta", "āsātām", "āsata" },
			{ "āsthāḥ", "āsāthām", "ādhvam" },
			{ "āsi", "āsvahi", "āsmahi" } };
		final VerbalParadigm ajnastaParad = VerbalParadigm.generate("ajñāsta", "ajñāsīt", TenseMood.AOR, Pada.MID, ajnastaData, "22-3");
		ajnastaParad.setStemCutFactor(4);
		paradigmMap.put("ajñāsta", ajnastaParad);
		
		// -aipsīt
		final String[][] aksaipsitData = {
			{ "sīt", "tām", "suḥ" },
			{ "sīḥ", "tam", "ta" },
			{ "sam", "sva", "sma" } };
		final VerbalParadigm aksaipsitParad = VerbalParadigm.generate("akṣaipsīt", "akṣaipsīt", TenseMood.AOR, Pada.ACT, aksaipsitData, "22-4");
		aksaipsitParad.setStemCutFactor(3);
		paradigmMap.put("akṣaipsīt", aksaipsitParad);
		final String[][] aksiptaData = {
			{ "ipta", "ipsātām", "ipsata" },
			{ "ipthāḥ", "ipsāthām", "ibdhvam" },
			{ "ipsi", "ipsvahi", "ipsmahi" } };
		final VerbalParadigm aksiptaParad = VerbalParadigm.generate("akṣipta", "akṣaipsīt", TenseMood.AOR, Pada.MID, aksiptaData, "22-4");
		aksiptaParad.setStemCutFactor(6);
		paradigmMap.put("akṣipta", aksiptaParad);

		// -aukṣīt
		final String[][] ayauksitData = {
			{ "ṣīt", "tām", "ṣuḥ" },
			{ "ṣīḥ", "tam", "ta" },
			{ "ṣam", "ṣva", "ṣma" } };
		final VerbalParadigm ayauksitParad = VerbalParadigm.generate("ayaukṣīt", "ayaukṣīt", TenseMood.AOR, Pada.ACT, ayauksitData, "22-4");
		ayauksitParad.setStemCutFactor(3);
		paradigmMap.put("ayaukṣīt", ayauksitParad);
		final String[][] ayuktaData = {
			{ "ukta", "ukṣātām", "ukṣata" },
			{ "ukthāḥ", "ukṣāthām", "ugdhvam" },
			{ "ukṣi", "ukṣvahi", "ukṣmahi" } };
		final VerbalParadigm ayuktaParad = VerbalParadigm.generate("ayukta", "ayaukṣīt", TenseMood.AOR, Pada.MID, ayuktaData, "22-4");
		ayuktaParad.setStemCutFactor(6);
		paradigmMap.put("ayukta", ayuktaParad);

		// -kṣīt
		final String[][] ayoksitData = {
			{ "ṣīt", "tām", "ṣuḥ" },
			{ "ṣīḥ", "tam", "ta" },
			{ "ṣam", "ṣva", "ṣma" } };
		final VerbalParadigm ayoksitParad = VerbalParadigm.generate("ayokṣīt", "ayokṣīt", TenseMood.AOR, Pada.ACT, ayoksitData, "22-4");
		ayoksitParad.setStemCutFactor(3);
		paradigmMap.put("ayokṣīt", ayoksitParad);

		final String[][] abhasitData = {
			{ "īt", "iṣṭām", "iṣuḥ" },
			{ "īḥ", "iṣṭam", "iṣṭa" },
			{ "iṣam", "iṣva", "iṣma" } };
		final VerbalParadigm abhasitParad = VerbalParadigm.generate("abhāsīt", "abhāsīt", TenseMood.AOR, Pada.ACT, abhasitData, "22-5");
		abhasitParad.setStemCutFactor(2);
		abhasitParad.addWordList(List.of("abhāsīt"));
		paradigmMap.put("abhāsīt", abhasitParad);

		final String[][] apavitData = {
			{ "īt", "iṣṭām", "iṣuḥ" },
			{ "īḥ", "iṣṭam", "iṣṭa" },
			{ "iṣam", "iṣva", "iṣma" } };
		final VerbalParadigm apavitParad = VerbalParadigm.generate("apāvīt", "apāvīt", TenseMood.AOR, Pada.ACT, apavitData, "22-6");
		apavitParad.setStemCutFactor(2);
		paradigmMap.put("apāvīt", apavitParad);
		final String[][] apavistaData = {
			{ "iṣṭa", "iṣātām", "iṣata" },
			{ "iṣṭhāḥ", "iṣāthām", "iḍhvam" },
			{ "iṣi", "iṣvahi", "iṣmahi" } };
		final VerbalParadigm apavistaParad = VerbalParadigm.generate("apaviṣṭa", "apāvīt", TenseMood.AOR, Pada.MID, apavistaData, "22-6");
		apavistaParad.setStemCutFactor(2);
		paradigmMap.put("apaviṣṭa", apavistaParad);

		// apāci for passive aorist
		final String[][] apaciData = {
			{ "i", "iṣātām", "iṣata" },
			{ "iṣṭhāḥ", "iṣāthām", "iḍhvam" },
			{ "iṣi", "iṣvahi", "iṣmahi" } };
		final VerbalParadigm apaciParad = VerbalParadigm.generate("apāci", "apāci", TenseMood.AOR, Pada.MID, apaciData, "22-6");
		apaciParad.setStemCutFactor(1);
		paradigmMap.put("apāci", apaciParad);

		// aninīṣīt for desiderative aorist
		final String[][] aninisitData = {
			{ "īt", "iṣṭām", "iṣuḥ" },
			{ "īḥ", "iṣṭam", "iṣṭa" },
			{ "iṣam", "iṣva", "iṣma" } };
		final VerbalParadigm aninisitParad = VerbalParadigm.generate("aninīṣīt", "aninīṣīt", TenseMood.AOR, Pada.ACT, aninisitData, "22-6");
		aninisitParad.setStemCutFactor(2);
		aninisitParad.setAugment(true);
		paradigmMap.put("aninīṣīt", aninisitParad);
		final String[][] aninisistaData = {
			{ "iṣṭa", "iṣātām", "iṣata" },
			{ "iṣṭhāḥ", "iṣāthām", "iḍhvam" },
			{ "iṣi", "iṣvahi", "iṣmahi" } };
		final VerbalParadigm aninisistaParad = VerbalParadigm.generate("aninīṣiṣṭa", "aninīṣīt", TenseMood.AOR, Pada.MID, aninisistaData, "22-6");
		aninisistaParad.setStemCutFactor(2);
		aninisistaParad.setAugment(true);
		paradigmMap.put("aninīṣiṣṭa", aninisistaParad);

		final String[][] adatData = {
			{ "āt", "ātām", "uḥ" },
			{ "āḥ", "ātam", "āta" },
			{ "ām", "āva", "āma" } };
		final VerbalParadigm adatParad = VerbalParadigm.generate("adāt", "adāt", TenseMood.AOR, Pada.ACT, adatData, "22-7");
		adatParad.setStemCutFactor(2);
		paradigmMap.put("adāt", adatParad);
		final String[][] aditaData = {
			{ "ita", "iṣātām", "iṣata" },
			{ "ithāḥ", "iṣāthām", "iḍhvam" },
			{ "iṣi", "iṣvahi", "iṣmahi" } };
		final VerbalParadigm aditaParad = VerbalParadigm.generate("adita", "adāt", TenseMood.AOR, Pada.MID, aditaData, "22-7");
		aditaParad.setStemCutFactor(2);
		paradigmMap.put("adita", aditaParad);

		final String[][] akarsitData = {
			{ "īt", "ṭām", "uḥ" },
			{ "īḥ", "ṭam", "ṭa" },
			{ "am", "va", "ma" } };
		final VerbalParadigm akarsitParad = VerbalParadigm.generate("akārṣīt", "akārṣīt", TenseMood.AOR, Pada.ACT, akarsitData, "23-8");
		akarsitParad.setStemCutFactor(2);
		paradigmMap.put("akārṣīt", akarsitParad);
		final String[][] akrtaData = {
			{ "ṛta", "ṛṣātām", "ṛṣata" },
			{ "ṛthāḥ", "ṛṣāthām", "ṛḍhvam" },
			{ "ṛṣi", "ṛṣvahi", "ṛṣmahi" } };
		final VerbalParadigm akrtaParad = VerbalParadigm.generate("akṛta", "akārṣīt", TenseMood.AOR, Pada.MID, akrtaData, "23-8");
		akrtaParad.setStemCutFactor(5);
		paradigmMap.put("akṛta", akrtaParad);

		final String[][] abhutData = {
			{ "t", "tām", "van" },
			{ "ḥ", "tam", "ta" },
			{ "vam", "va", "ma" } };
		final VerbalParadigm abhutParad = VerbalParadigm.generate("abhūt", "abhūt", TenseMood.AOR, Pada.ACT, abhutData, "23-9");
		abhutParad.setStemCutFactor(1);
		paradigmMap.put("abhūt", abhutParad);

		final String[][] alabdhaData = {
			{ "bdha", "psātām", "psata" },
			{ "pthāḥ", "psāthām", "bdhvam" },
			{ "psi", "psvahi", "psmahi" } };
		final VerbalParadigm alabdhaParad = VerbalParadigm.generate("alabdha", "alabdha", TenseMood.AOR, Pada.MID, alabdhaData);
		alabdhaParad.setStemCutFactor(4);
		paradigmMap.put("alabdha", alabdhaParad);

		// precative (optative middle)
		final String[][] niyatData = {
			{ "t", "stām", "suḥ" },
			{ "ḥ", "stam", "sta" },
			{ "sam", "sva", "sma" } };
		final VerbalParadigm niyatParad = VerbalParadigm.generate("nīyāt", "nīyāt", TenseMood.PREC, Pada.ACT, niyatData, "24");
		niyatParad.setStemCutFactor(1);
		paradigmMap.put("nīyāt", niyatParad);
		final String[][] nesistaData = {
			{ "ṣṭa", "yāstām", "ran" },
			{ "ṣṭhāḥ", "yāsthām", "ḍhvam" },
			{ "ya", "vahi", "mahi" } };
		final VerbalParadigm nesistaParad = VerbalParadigm.generate("neṣīṣṭa", "neṣīṣṭa", TenseMood.PREC, Pada.MID, nesistaData, "24");
		nesistaParad.setStemCutFactor(3);
		paradigmMap.put("neṣīṣṭa", nesistaParad);

		// periphrastic future
		final String[][] netaData = {
			{ "ā", "ārau", "āraḥ" },
			{ "āsi", "āsthaḥ", "āstha" },
			{ "āsmi", "āsvaḥ", "āsmaḥ" } };
		final VerbalParadigm netaParad = VerbalParadigm.generate("netā", "netā", TenseMood.PERI, Pada.ACT, netaData, "25");
		netaParad.setStemCutFactor(1);
		paradigmMap.put("netā", netaParad);
		final String[][] netaMData = {
			{ "ā", "ārau", "āraḥ" },
			{ "āse", "āsāthe", "ādhve" },
			{ "āhe", "āsvahe", "āsmahe" } };
		final VerbalParadigm netaMParad = VerbalParadigm.generate("netāM", "netā", TenseMood.PERI, Pada.MID, netaMData, "25");
		netaMParad.setStemCutFactor(1);
		paradigmMap.put("netāM", netaMParad);
	}

}

