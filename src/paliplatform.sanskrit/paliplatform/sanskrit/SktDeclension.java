/*
 * SktDeclension.java
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

import static paliplatform.sanskrit.NominalParadigm.Gender;
import static paliplatform.sanskrit.NominalParadigm.WordType;

import java.util.*;

/** 
 * The class handling Sanskrit declensions.
 * @author J.R. Bhaddacak
 * @version 4.1
 * @since 4.1
 */
public class SktDeclension {
	public static enum Case {
		NOM("Nominative"), ACC("Accusative"), INS("Instrumental"), DAT("Dative"),
		ABL("Ablative"), GEN("Genitive"), LOC("Locative"), VOC("Vocative");
		public static final Case[] values = values();
		private static final String[] devaNames = { "प्रथमा", "द्वितीया", "तृतीया", "चतुर्थी", "पञ्चमी", "षष्ठी", "सप्तमी", "सम्बोधनम्" };
		private static final String devaNumbers = "१२३४५६७स";
		private final String name;
		private Case(final String n) {
			name = n;
		}
		public String getName() {
			return name;
		}
		public String getDevaName() {
			return devaNames[this.ordinal()];
		}
		public String getAbbr() {
			return name.substring(0, 3).toLowerCase() + ".";
		}
		public String getNumAbbr() {
			final int num = this.ordinal() + 1;
			return num < 8 ? "" + num : "s";
		}
		public String getDevaNumAbbr() {
			return "" + devaNumbers.charAt(this.ordinal());
		}
	}
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
		public String getAbbr() {
			return name.substring(0, 3) + ".";
		}
	}
	public static final Map<String, NominalParadigm> paradigmMap = new LinkedHashMap<>();

	private SktDeclension() {
	}

	public static Map<Case, Map<Number, List<String>>> compute(final SktNominal word) {
		final NominalParadigm parad = word.getParadigm();
		if (parad == null)
			return Collections.emptyMap();
		final Map<Case, Map<Number, List<String>>> result = new EnumMap<>(Case.class);
		for (final Case c : Case.values) {
			final Map<Number, List<String>> numMap = new EnumMap<>(Number.class);
			for (final Number n : Number.values) {
				final List<String> productList = word.getDeclension(parad.getEndings(c, n));
				numMap.put(n, productList);
			}
			result.put(c, numMap);
		}
		return result;
	}

	public static String getWordGroupIdentifier(final String paradName) {
		final NominalParadigm parad = paradigmMap.get(paradName);
		return parad.getWordGroupIdentifier();
	}

	// static initialization of paradigms
	static {
		// masculine set
		final String[][] devahData = {
			{ "aḥ", "au", "āḥ" },
			{ "am", "au", "ān" },
			{ "ena", "ābhyām", "aiḥ" },
			{ "āya", "ābhyām", "ebhyaḥ" },
			{ "āt", "ābhyām", "ebhyaḥ" },
			{ "asya", "ayoḥ", "ānām" },
			{ "e", "ayoḥ", "eṣu" },
			{ "a", "au", "āḥ" } };
		final NominalParadigm devahParad = NominalParadigm.generate("devaḥ", "deva", devahData, 1);
		devahParad.addGender(Gender.MAS);
		devahParad.setWordGroupIdentifier("m:a:");
		paradigmMap.put("devaḥ", devahParad);
		final String[][] munihData = {
			{ "iḥ", "ī", "ayaḥ" },
			{ "im", "ī", "īn" },
			{ "inā", "ibhyām", "ibhiḥ" },
			{ "aye", "ibhyām", "ibhyaḥ" },
			{ "eḥ", "ibhyām", "ibhyaḥ" },
			{ "eḥ", "yoḥ", "īnām" },
			{ "au", "yoḥ", "iṣu" },
			{ "e", "ī", "ayaḥ" } };
		final NominalParadigm munihParad = NominalParadigm.generate("muniḥ", "muni", munihData, 2);
		munihParad.addGender(Gender.MAS);
		munihParad.setWordGroupIdentifier("m:i:");
		paradigmMap.put("muniḥ", munihParad);
		final String[][] pasuhData = {
			{ "uḥ", "ū", "avaḥ" },
			{ "um", "ū", "ūn" },
			{ "unā", "ubhyām", "ubhiḥ" },
			{ "ave", "ubhyām", "ubhyaḥ" },
			{ "oḥ", "ubhyām", "ubhyaḥ" },
			{ "oḥ", "voḥ", "ūnām" },
			{ "au", "voḥ", "uṣu" },
			{ "o", "ū", "avaḥ" } };
		final NominalParadigm pasuhParad = NominalParadigm.generate("paśuḥ", "paśu", pasuhData, 3);
		pasuhParad.addGender(Gender.MAS);
		pasuhParad.setWordGroupIdentifier("m:u:");
		paradigmMap.put("paśuḥ", pasuhParad);
		final String[][] netaData = {
			{ "ā", "ārau", "āraḥ" },
			{ "āram", "ārau", "ṝn" },
			{ "rā", "ṛbhyām", "ṛbhiḥ" },
			{ "re", "ṛbhyām", "ṛbhyaḥ" },
			{ "uḥ", "ṛbhyām", "ṛbhyaḥ" },
			{ "uḥ", "roḥ", "ṝṇām" },
			{ "ari", "roḥ", "ṛṣu" },
			{ "aḥ", "ārau", "āraḥ" } };
		final NominalParadigm netaParad = NominalParadigm.generate("netā", "netṛ", netaData, 4);
		netaParad.addGender(Gender.MAS);
		netaParad.setWordGroupIdentifier("m:ṛ:");
		paradigmMap.put("netā", netaParad);
		final String[][] pitaData = {
			{ "ā", "arau", "araḥ" },
			{ "aram", "arau", "ṝn" },
			{ "rā", "ṛbhyām", "ṛbhiḥ" },
			{ "re", "ṛbhyām", "ṛbhyaḥ" },
			{ "uḥ", "ṛbhyām", "ṛbhyaḥ" },
			{ "uḥ", "roḥ", "ṝṇām" },
			{ "ari", "roḥ", "ṛṣu" },
			{ "aḥ", "arau", "araḥ" } };
		final NominalParadigm pitaParad = NominalParadigm.generate("pitā", "pitṛ", pitaData, 5);
		pitaParad.addGender(Gender.MAS);
		pitaParad.setWordGroupIdentifier("m:ṛk:");
		paradigmMap.put("pitā", pitaParad);
		final String[][] marutData = {
			{ "t", "tau", "taḥ" },
			{ "tam", "tau", "taḥ" },
			{ "tā", "dbhyām", "dbhiḥ" },
			{ "te", "dbhyām", "dbhyaḥ" },
			{ "taḥ", "dbhyām", "dbhyaḥ" },
			{ "taḥ", "toḥ", "tām" },
			{ "ti", "toḥ", "tsu" },
			{ "t", "tau", "taḥ" } };
		final NominalParadigm marutParad = NominalParadigm.generate("marut", "marut", marutData, 6);
		marutParad.addGender(Gender.MAS, Gender.FEM);
		paradigmMap.put("marut", marutParad);
		paradigmMap.put("marut-k-k", NominalParadigm.generateFromPrototype(marutParad, "k", "k", "sarvaśak"));
		paradigmMap.put("marut-k-c", NominalParadigm.generateFromPrototype(marutParad, "k", "c", "vāk", 7));
		paradigmMap.put("marut-k-j", NominalParadigm.generateFromPrototype(marutParad, "k", "j", "vaṇik", 7));
		paradigmMap.put("marut-k-ś", NominalParadigm.generateFromPrototype(marutParad, "k", "ś", "dik", 7));
		paradigmMap.put("marut-k-ṣ", NominalParadigm.generateFromPrototype(marutParad, "k", "ṣ", "dadhṛk", 7));
		paradigmMap.put("marut-k-h", NominalParadigm.generateFromPrototype(marutParad, "k", "h", "kāmadhuk", 7));
		paradigmMap.put("marut-ṭ-j", NominalParadigm.generateFromPrototype(marutParad, "ṭ", "j", "samrāṭ", 7));
		paradigmMap.put("marut-ṭ-ś", NominalParadigm.generateFromPrototype(marutParad, "ṭ", "ś", "viṭ", 7));
		paradigmMap.put("marut-ṭ-ṣ", NominalParadigm.generateFromPrototype(marutParad, "ṭ", "ṣ", "dviṭ", 7));
		paradigmMap.put("marut-ṭ-h", NominalParadigm.generateFromPrototype(marutParad, "ṭ", "h", "madhuliṭ", 7));
		paradigmMap.put("marut-t-t", NominalParadigm.generateFromPrototype(marutParad, "t", "t", "marut"));
		paradigmMap.put("marut-t-d", NominalParadigm.generateFromPrototype(marutParad, "t", "d", "āpat", 7));
		paradigmMap.put("marut-t-dh", NominalParadigm.generateFromPrototype(marutParad, "t", "dh", "samit", 7));
		paradigmMap.put("marut-t-h", NominalParadigm.generateFromPrototype(marutParad, "t", "h", "upānat", 7));
		paradigmMap.put("marut-p-p", NominalParadigm.generateFromPrototype(marutParad, "p", "p", "dharmagup"));
		paradigmMap.put("marut-p-bh", NominalParadigm.generateFromPrototype(marutParad, "p", "bh", "triṣṭup", 7));
		final NominalParadigm maruthrParad = NominalParadigm.generateFromPrototype(marutParad, "ḥ", "r", "dvāḥ", 7);
		paradigmMap.put("marut-ḥ-r", maruthrParad);
		paradigmMap.put("marut-ḥ-ṣ", NominalParadigm.generateFromPrototype(marutParad, "ḥ", "ṣ", "doḥ"));
		final NominalParadigm dvahParad = NominalParadigm.duplicate(maruthrParad, "dvāḥ", "dvāḥ");
		dvahParad.setGender(List.of(Gender.FEM));
		dvahParad.setEndings(Case.LOC, Number.PLU, List.of("rṣu"));
		paradigmMap.put("dvāḥ", dvahParad);
		final String[][] vedhahData = {
			{ "āḥ", "asau", "asaḥ" },
			{ "asam", "asau", "asaḥ" },
			{ "asā", "obhyām", "obhiḥ" },
			{ "ase", "obhyām", "obhyaḥ" },
			{ "asaḥ", "obhyām", "obhyaḥ" },
			{ "asaḥ", "asoḥ", "asām" },
			{ "asi", "asoḥ", "aḥsu" },
			{ "aḥ", "asau", "asaḥ" } };
		final NominalParadigm vedhahParad = NominalParadigm.generate("vedhāḥ", "vedhas", vedhahData, 8);
		vedhahParad.setStemCutFactor(2);
		vedhahParad.addGender(Gender.MAS, Gender.FEM);
		vedhahParad.setWordGroupIdentifier("m:as:");
		paradigmMap.put("vedhāḥ", vedhahParad);
		final String[][] atmaData = {
			{ "ā", "ānau", "ānaḥ" },
			{ "ānam", "ānau", "anaḥ" },
			{ "anā", "abhyām", "abhiḥ" },
			{ "ane", "abhyām", "abhyaḥ" },
			{ "anaḥ", "abhyām", "abhyaḥ" },
			{ "anaḥ", "anoḥ", "anām" },
			{ "ani", "anoḥ", "asu" },
			{ "an", "ānau", "ānaḥ" } };
		final NominalParadigm atmaParad = NominalParadigm.generate("ātmā", "ātman", atmaData, 9);
		atmaParad.setStemCutFactor(2);
		atmaParad.addGender(Gender.MAS, Gender.FEM);
		atmaParad.setWordGroupIdentifier("m:an2:");
		paradigmMap.put("ātmā", atmaParad);
		final String[][] rajaData = {
			{ "ā", "ānau", "ānaḥ" },
			{ "ānam", "ānau", "ñaḥ" },
			{ "ñā", "abhyām", "abhiḥ" },
			{ "ñe", "abhyām", "abhyaḥ" },
			{ "ñaḥ", "abhyām", "abhyaḥ" },
			{ "ñaḥ", "ñoḥ", "ñām" },
			{ "ñi", "ñoḥ", "asu" },
			{ "an", "ānau", "ānaḥ" } };
		final NominalParadigm rajaParad = NominalParadigm.generate("rājā", "rājan", rajaData, 10);
		rajaParad.setStemCutFactor(2);
		rajaParad.addGender(Gender.MAS, Gender.FEM);
		rajaParad.setWordGroupIdentifier("m:an1:");
		rajaParad.addEndings(Case.LOC, Number.SING, "ani");
		paradigmMap.put("rājā", rajaParad);
		final String[][] hastiData = {
			{ "ī", "inau", "inaḥ" },
			{ "inam", "inau", "inaḥ" },
			{ "inā", "ibhyām", "ibhiḥ" },
			{ "ine", "ibhyām", "ibhyaḥ" },
			{ "inaḥ", "ibhyām", "ibhyaḥ" },
			{ "inaḥ", "inoḥ", "inām" },
			{ "ini", "inoḥ", "iṣu" },
			{ "in", "inau", "inaḥ" } };
		final NominalParadigm hastiParad = NominalParadigm.generate("hastī", "hastin", hastiData, 11);
		hastiParad.setStemCutFactor(2);
		hastiParad.addGender(Gender.MAS);
		hastiParad.setWordGroupIdentifier("m:in:");
		paradigmMap.put("hastī", hastiParad);
		final String[][] nayanData = {
			{ "n", "ntau", "ntaḥ" },
			{ "ntam", "ntau", "taḥ" },
			{ "tā", "dbhyām", "dbhiḥ" },
			{ "te", "dbhyām", "dbhyaḥ" },
			{ "taḥ", "dbhyām", "dbhyaḥ" },
			{ "taḥ", "toḥ", "tām" },
			{ "ti", "toḥ", "tsu" },
			{ "n", "ntau", "ntaḥ" } };
		final NominalParadigm nayanParad = NominalParadigm.generate("nayan", "nayant", nayanData, 12);
		nayanParad.setStemCutFactor(2);
		nayanParad.addGender(Gender.MAS);
		nayanParad.addWordList(List.of("nayant", "neṣyant", "bhānt", "rakṣant", "sunvant"));
		paradigmMap.put("nayan", nayanParad);
		final String[][] dhimanData = {
			{ "ān", "antau", "antaḥ" },
			{ "antam", "antau", "ataḥ" },
			{ "atā", "adbhyām", "adbhiḥ" },
			{ "ate", "adbhyām", "adbhyaḥ" },
			{ "ataḥ", "adbhyām", "adbhyaḥ" },
			{ "ataḥ", "atoḥ", "atām" },
			{ "ati", "atoḥ", "atsu" },
			{ "an", "antau", "antaḥ" } };
		final NominalParadigm dhimanParad = NominalParadigm.generate("dhīmān", "dhīmat", dhimanData, 13);
		dhimanParad.setStemCutFactor(2);
		dhimanParad.addGender(Gender.MAS);
		dhimanParad.setWordGroupIdentifier("m:mat:");
		paradigmMap.put("dhīmān", dhimanParad);
		final String[][] sreyanData = {
			{ "ān", "āṃsau", "āṃsaḥ" },
			{ "āṃsam", "āṃsau", "asaḥ" },
			{ "asā", "obhyām", "obhiḥ" },
			{ "ase", "obhyām", "obhyaḥ" },
			{ "asaḥ", "obhyām", "obhyaḥ" },
			{ "asaḥ", "asoḥ", "asām" },
			{ "asi", "asoḥ", "aḥsu" },
			{ "an", "āṃsau", "āṃsaḥ" } };
		final NominalParadigm sreyanParad = NominalParadigm.generate("śreyān", "śreyas", sreyanData, 14);
		sreyanParad.setStemCutFactor(2);
		sreyanParad.addGender(Gender.MAS);
		sreyanParad.addWordList(Arrays.asList("aṇīyas", "alpīyas", "kanīyas", "kṣepīyas", "kṣodīyas", "garīyas",
					"jyāyas", "davīyas", "drāghīyas", "nedīyas", "paṭīyas", "preyas", "balīyas", "bhūyas",
					"baṃhīyas", "mahīyas", "mradīyas", "yavīyas", "laghīyas", "varīyas", "varṣīyas",
					"śreyas", "stheyas", "hrasīyas"));
		paradigmMap.put("śreyān", sreyanParad);
		final String[][] tenivanData = {
			{ "ivān", "ivāṃsau", "ivāṃsaḥ" },
			{ "ivāṃsam", "ivāṃsau", "uṣaḥ" },
			{ "uṣā", "ivadbhyām", "ivadbhiḥ" },
			{ "uṣe", "ivadbhyām", "ivadbhyaḥ" },
			{ "uṣaḥ", "ivadbhyām", "ivadbhyaḥ" },
			{ "uṣaḥ", "uṣoḥ", "uṣām" },
			{ "uṣi", "uṣoḥ", "ivatsu" },
			{ "ivan", "ivāṃsau", "ivāṃsaḥ" } };
		final NominalParadigm tenivanParad = NominalParadigm.generate("tenivān", "tenivas", tenivanData, 15);
		tenivanParad.setStemCutFactor(4);
		tenivanParad.addGender(Gender.MAS);
		tenivanParad.addWordList(List.of("tenivas", "tutudivas", "rarakṣivas"));
		paradigmMap.put("tenivān", tenivanParad);
		final String[][] cakrvanData = {
			{ "ṛvān", "ṛvāṃsau", "ṛvāṃsaḥ" },
			{ "ṛvāṃsam", "ṛvāṃsau", "ruṣaḥ" },
			{ "ruṣā", "ṛvadbhyām", "ṛvadbhiḥ" },
			{ "ruṣe", "ṛvadbhyām", "ṛvadbhyaḥ" },
			{ "ruṣaḥ", "ṛvadbhyām", "ṛvadbhyaḥ" },
			{ "ruṣaḥ", "ruṣoḥ", "ruṣām" },
			{ "ruṣi", "ruṣoḥ", "ṛvatsu" },
			{ "ṛvan", "ṛvāṃsau", "ṛvāṃsaḥ" } };
		final NominalParadigm cakrvanParad = NominalParadigm.generate("cakṛvān", "cakṛvas", cakrvanData, 16);
		cakrvanParad.setStemCutFactor(4);
		cakrvanParad.addGender(Gender.MAS);
		paradigmMap.put("cakṛvān", cakrvanParad);
		final String[][] vidvanData = {
			{ "vān", "vāṃsau", "vāṃsaḥ" },
			{ "vāṃsam", "vāṃsau", "uṣaḥ" },
			{ "uṣā", "vadbhyām", "vadbhiḥ" },
			{ "uṣe", "vadbhyām", "vadbhyaḥ" },
			{ "uṣaḥ", "vadbhyām", "vadbhyaḥ" },
			{ "uṣaḥ", "uṣoḥ", "uṣām" },
			{ "uṣi", "uṣoḥ", "vatsu" },
			{ "van", "vāṃsau", "vāṃsaḥ" } };
		final NominalParadigm vidvanParad = NominalParadigm.generate("vidvān", "vidvas", vidvanData, 16);
		vidvanParad.setStemCutFactor(3);
		vidvanParad.addGender(Gender.MAS);
		paradigmMap.put("vidvān", vidvanParad);
		final String[][] susruvanData = {
			{ "vān", "vāṃsau", "vāṃsaḥ" },
			{ "vāṃsam", "vāṃsau", "vuṣaḥ" },
			{ "vuṣā", "vadbhyām", "vadbhiḥ" },
			{ "vuṣe", "vadbhyām", "vadbhyaḥ" },
			{ "vuṣaḥ", "vadbhyām", "vadbhyaḥ" },
			{ "vuṣaḥ", "vuṣoḥ", "vuṣām" },
			{ "vuṣi", "vuṣoḥ", "vatsu" },
			{ "van", "vāṃsau", "vāṃsaḥ" } };
		final NominalParadigm susruvanParad = NominalParadigm.generate("śuśruvān", "śuśruvas", susruvanData, 16);
		susruvanParad.setStemCutFactor(3);
		susruvanParad.addGender(Gender.MAS);
		paradigmMap.put("śuśruvān", susruvanParad);
		final String[][] ninivanData = {
			{ "īvān", "īvāṃsau", "īvāṃsaḥ" },
			{ "īvāṃsam", "īvāṃsau", "yuṣaḥ" },
			{ "yuṣā", "īvadbhyām", "īvadbhiḥ" },
			{ "yuṣe", "īvadbhyām", "īvadbhyaḥ" },
			{ "yuṣaḥ", "īvadbhyām", "īvadbhyaḥ" },
			{ "yuṣaḥ", "yuṣoḥ", "yuṣām" },
			{ "yuṣi", "yuṣoḥ", "īvatsu" },
			{ "īvan", "īvāṃsau", "īvāṃsaḥ" } };
		final NominalParadigm ninivanParad = NominalParadigm.generate("ninīvān", "ninīvas", ninivanData, 16);
		ninivanParad.setStemCutFactor(4);
		ninivanParad.addGender(Gender.MAS);
		paradigmMap.put("ninīvān", ninivanParad);
		// neuter set
		final String[][] phalamData = {
			{ "am", "e", "āni" },
			{ "am", "e", "āni" },
			{ "ena", "ābhyām", "aiḥ" },
			{ "āya", "ābhyām", "ebhyaḥ" },
			{ "āt", "ābhyām", "ebhyaḥ" },
			{ "asya", "ayoḥ", "ānām" },
			{ "e", "ayoḥ", "eṣu" },
			{ "a", "e", "āni" } };
		final NominalParadigm phalamParad = NominalParadigm.generate("phalam", "phala", phalamData, 17);
		phalamParad.addGender(Gender.NEU);
		phalamParad.setWordGroupIdentifier("n:a:");
		paradigmMap.put("phalam", phalamParad);
		final String[][] variData = {
			{ "i", "inī", "īni" },
			{ "i", "inī", "īni" },
			{ "inā", "ibhyām", "ibhiḥ" },
			{ "ine", "ibhyām", "ibhyaḥ" },
			{ "inaḥ", "ibhyām", "ibhyaḥ" },
			{ "inaḥ", "inoḥ", "īnām" },
			{ "ini", "inoḥ", "iṣu" },
			{ "i", "inī", "īni" } };
		final NominalParadigm variParad = NominalParadigm.generate("vāri", "vāri", variData, 18);
		variParad.addGender(Gender.NEU);
		variParad.setWordGroupIdentifier("n:i:");
		variParad.addEndings(Case.VOC, Number.SING, "e");
		paradigmMap.put("vāri", variParad);
		final String[][] suciData = {
			{ "i", "inī", "īni" },
			{ "i", "inī", "īni" },
			{ "inā", "ibhyām", "ibhiḥ" },
			{ "ine", "ibhyām", "ibhyaḥ" },
			{ "inaḥ", "ibhyām", "ibhyaḥ" },
			{ "inaḥ", "inoḥ", "īnām" },
			{ "ini", "inoḥ", "iṣu" },
			{ "i", "inī", "īni" } };
		final NominalParadigm suciParad = NominalParadigm.generate("śuci", "śuci", suciData, 18);
		suciParad.addGender(Gender.NEU);
		suciParad.addEndings(Case.DAT, Number.SING, "aye");
		suciParad.addEndings(Case.ABL, Number.SING, "eḥ");
		suciParad.addEndings(Case.GEN, Number.SING, "eḥ");
		suciParad.addEndings(Case.LOC, Number.SING, "au");
		suciParad.addEndings(Case.GEN, Number.DUAL, "yoḥ");
		suciParad.addEndings(Case.LOC, Number.DUAL, "yoḥ");
		suciParad.addEndings(Case.VOC, Number.SING, "e");
		paradigmMap.put("śuci", suciParad);
		final String[][] madhuData = {
			{ "u", "unī", "ūni" },
			{ "u", "unī", "ūni" },
			{ "unā", "ubhyām", "ubhiḥ" },
			{ "une", "ubhyām", "ubhyaḥ" },
			{ "unaḥ", "ubhyām", "ubhyaḥ" },
			{ "unaḥ", "unoḥ", "ūnām" },
			{ "uni", "unoḥ", "uṣu" },
			{ "u", "unī", "ūni" } };
		final NominalParadigm madhuParad = NominalParadigm.generate("madhu", "madhu", madhuData, 19);
		madhuParad.addGender(Gender.NEU);
		madhuParad.setWordGroupIdentifier("n:u:");
		madhuParad.addEndings(Case.VOC, Number.SING, "o");
		paradigmMap.put("madhu", madhuParad);
		final String[][] laghuData = {
			{ "u", "unī", "ūni" },
			{ "u", "unī", "ūni" },
			{ "unā", "ubhyām", "ubhiḥ" },
			{ "une", "ubhyām", "ubhyaḥ" },
			{ "unaḥ", "ubhyām", "ubhyaḥ" },
			{ "unaḥ", "unoḥ", "ūnām" },
			{ "uni", "unoḥ", "uṣu" },
			{ "u", "unī", "ūni" } };
		final NominalParadigm laghuParad = NominalParadigm.generate("laghu", "laghu", laghuData, 19);
		laghuParad.addGender(Gender.NEU);
		laghuParad.addEndings(Case.DAT, Number.SING, "ave");
		laghuParad.addEndings(Case.ABL, Number.SING, "oḥ");
		laghuParad.addEndings(Case.GEN, Number.SING, "oḥ");
		laghuParad.addEndings(Case.LOC, Number.SING, "au");
		laghuParad.addEndings(Case.GEN, Number.DUAL, "voḥ");
		laghuParad.addEndings(Case.LOC, Number.DUAL, "voḥ");
		laghuParad.addEndings(Case.VOC, Number.SING, "o");
		paradigmMap.put("laghu", laghuParad);
		final String[][] dhatrData = {
			{ "ṛ", "ṛṇī", "ṝṇi" },
			{ "ṛ", "ṛṇī", "ṝṇi" },
			{ "ṛṇā", "ṛbhyām", "ṛbhiḥ" },
			{ "ṛṇe", "ṛbhyām", "ṛbhyaḥ" },
			{ "ṛṇaḥ", "ṛbhyām", "ṛbhyaḥ" },
			{ "ṛṇaḥ", "ṛṇoḥ", "ṝṇām" },
			{ "ṛṇi", "ṛṇoḥ", "ṛṣu" },
			{ "ṛ", "ṛṇī", "ṝṇi" } };
		final NominalParadigm dhatrParad = NominalParadigm.generate("dhātṛ", "dhātṛ", dhatrData, 20);
		dhatrParad.addGender(Gender.NEU);
		dhatrParad.addEndings(Case.VOC, Number.SING, "aḥ");
		dhatrParad.addWordList(List.of("dātṛ", "dhātṛ", "rakṣitṛ", "sumātṛ"));
		paradigmMap.put("dhātṛ", dhatrParad);
		final String[][] jagatData = {
			{ "t", "tī", "nti" },
			{ "t", "tī", "nti" },
			{ "tā", "dbhyām", "dbhiḥ" },
			{ "te", "dbhyām", "dbhyaḥ" },
			{ "taḥ", "dbhyām", "dbhyaḥ" },
			{ "taḥ", "toḥ", "tām" },
			{ "ti", "toḥ", "tsu" },
			{ "t", "tī", "nti" } };
		final NominalParadigm jagatParad = NominalParadigm.generate("jagat", "jagat", jagatData, 21);
		jagatParad.addGender(Gender.NEU);
		paradigmMap.put("jagat", jagatParad);
		paradigmMap.put("jagat-k-k", NominalParadigm.generateFromPrototype(jagatParad, "k", "k", "sarvaśak"));
		paradigmMap.put("jagat-k-c", NominalParadigm.generateFromPrototype(jagatParad, "k", "j", "priyavāk", 22));
		paradigmMap.put("jagat-k-j", NominalParadigm.generateFromPrototype(jagatParad, "k", "j", "asṛk", 22));
		final NominalParadigm jagatttParad = NominalParadigm.generateFromPrototype(jagatParad, "t", "t", "jagat");
		jagatttParad.addWordList(Arrays.asList("jagat", "jānat", "juhvat", "tudat", "trivṛt", "dadhat", "dāsyat", "dhīmat", "neṣyat", "bibhrat", "bhagavat", "bhāt"));
		paradigmMap.put("jagat-t-t", jagatttParad);
		paradigmMap.put("jagat-p-p", NominalParadigm.generateFromPrototype(jagatParad, "p", "p", "gup"));
		final String[][] manahData = {
			{ "aḥ", "asī", "āṃsi" },
			{ "aḥ", "asī", "āṃsi" },
			{ "asā", "obhyām", "obhiḥ" },
			{ "ase", "obhyām", "obhyaḥ" },
			{ "asaḥ", "obhyām", "obhyaḥ" },
			{ "asaḥ", "asoḥ", "asām" },
			{ "asi", "asoḥ", "aḥsu" },
			{ "aḥ", "asī", "āṃsi" } };
		final NominalParadigm manahParad = NominalParadigm.generate("manaḥ", "manas", manahData, 23);
		manahParad.setStemCutFactor(2);
		manahParad.addGender(Gender.NEU);
		manahParad.setWordGroupIdentifier("n:as:");
		paradigmMap.put("manaḥ", manahParad);
		final String[][] havihData = {
			{ "iḥ", "iṣī", "īṃṣi" },
			{ "iḥ", "iṣī", "īṃṣi" },
			{ "iṣā", "irbhyām", "irbhiḥ" },
			{ "iṣe", "irbhyām", "irbhyiḥ" },
			{ "iṣaḥ", "irbhyām", "irbhyiḥ" },
			{ "iṣaḥ", "iṣoḥ", "iṣām" },
			{ "iṣi", "iṣoḥ", "iḥṣu" },
			{ "iḥ", "iṣī", "īṃṣi" } };
		final NominalParadigm havihParad = NominalParadigm.generate("haviḥ", "havis", havihData, 24);
		havihParad.setStemCutFactor(2);
		havihParad.addGender(Gender.NEU);
		havihParad.setWordGroupIdentifier("n:is:");
		paradigmMap.put("haviḥ", havihParad);
		final String[][] ayuhData = {
			{ "uḥ", "uṣī", "ūṃṣi" },
			{ "uḥ", "uṣī", "ūṃṣi" },
			{ "uṣā", "urbhyām", "urbhuḥ" },
			{ "uṣe", "urbhyām", "urbhyuḥ" },
			{ "uṣaḥ", "urbhyām", "urbhyuḥ" },
			{ "uṣaḥ", "uṣoḥ", "uṣām" },
			{ "uṣi", "uṣoḥ", "uḥṣu" },
			{ "uḥ", "uṣī", "ūṃṣi" } };
		final NominalParadigm ayuhParad = NominalParadigm.generate("āyuḥ", "āyus", ayuhData, 25);
		ayuhParad.setStemCutFactor(2);
		ayuhParad.addGender(Gender.NEU);
		ayuhParad.setWordGroupIdentifier("n:us:");
		paradigmMap.put("āyuḥ", ayuhParad);
		final String[][] karmaData = {
			{ "a", "anī", "āni" },
			{ "a", "anī", "āni" },
			{ "anā", "abhyām", "abhiḥ" },
			{ "ane", "abhyām", "abhyaḥ" },
			{ "anaḥ", "abhyām", "abhyaḥ" },
			{ "anaḥ", "anoḥ", "anām" },
			{ "ani", "anoḥ", "asu" },
			{ "a", "anī", "āni" } };
		final NominalParadigm karmaParad = NominalParadigm.generate("karma", "karman", karmaData, 26);
		karmaParad.setStemCutFactor(2);
		karmaParad.addGender(Gender.NEU);
		karmaParad.setWordGroupIdentifier("n:an2:");
		karmaParad.addEndings(Case.VOC, Number.SING, "an");
		paradigmMap.put("karma", karmaParad);
		final String[][] namaData = {
			{ "a", "nī", "āni" },
			{ "a", "nī", "āni" },
			{ "nā", "abhyām", "abhiḥ" },
			{ "ne", "abhyām", "abhyaḥ" },
			{ "naḥ", "abhyām", "abhyaḥ" },
			{ "naḥ", "noḥ", "nām" },
			{ "ni", "noḥ", "asu" },
			{ "a", "nī", "āni" } };
		final NominalParadigm namaParad = NominalParadigm.generate("nāma", "nāman", namaData, 27);
		namaParad.setStemCutFactor(2);
		namaParad.addGender(Gender.NEU);
		namaParad.setWordGroupIdentifier("n:an1:");
		namaParad.addEndings(Case.NOM, Number.DUAL, "anī");
		namaParad.addEndings(Case.ACC, Number.DUAL, "anī");
		namaParad.addEndings(Case.LOC, Number.SING, "ani");
		namaParad.addEndings(Case.VOC, Number.SING, "an");
		namaParad.addEndings(Case.VOC, Number.DUAL, "anī");
		paradigmMap.put("nāma", namaParad);
		final String[][] baliData = {
			{ "i", "inī", "īni" },
			{ "i", "inī", "īni" },
			{ "inā", "ibhyām", "ibhiḥ" },
			{ "ine", "ibhyām", "ibhyaḥ" },
			{ "inaḥ", "ibhyām", "ibhyaḥ" },
			{ "inaḥ", "inoḥ", "inām" },
			{ "ini", "inoḥ", "iṣu" },
			{ "i", "inī", "īni" } };
		final NominalParadigm baliParad = NominalParadigm.generate("bali", "balin", baliData, 28);
		baliParad.setStemCutFactor(2);
		baliParad.addGender(Gender.NEU);
		baliParad.setWordGroupIdentifier("n:in:");
		baliParad.addEndings(Case.VOC, Number.SING, "in");
		paradigmMap.put("bali", baliParad);
		final String[][] nayatData = {
			{ "t", "ntī", "nti" },
			{ "t", "ntī", "nti" },
			{ "tā", "dbhyām", "dbhiḥ" },
			{ "te", "dbhyām", "dbhyaḥ" },
			{ "taḥ", "dbhyām", "dbhyaḥ" },
			{ "taḥ", "toḥ", "tām" },
			{ "ti", "toḥ", "tsu" },
			{ "t", "ntī", "nti" } };
		final NominalParadigm nayatParad = NominalParadigm.generate("nayat", "nayant", nayatData, 29);
		nayatParad.setStemCutFactor(2);
		nayatParad.addGender(Gender.NEU);
		nayatParad.addWordList(List.of("tudant", "dāsyant", "nāyayant", "ninīṣant", "neṣyant", "bhānt", "rakṣant"));
		paradigmMap.put("nayat", nayatParad);
		final String[][] tenivatData = {
			{ "ivat", "uṣī", "ivāṃsi" },
			{ "ivat", "uṣī", "ivāṃsi" },
			{ "uṣā", "ivadbhyām", "ivadbhiḥ" },
			{ "uṣe", "ivadbhyām", "ivadbhyaḥ" },
			{ "uṣaḥ", "ivadbhyām", "ivadbhyaḥ" },
			{ "uṣaḥ", "uṣoḥ", "uṣām" },
			{ "uṣi", "uṣoḥ", "ivatsu" },
			{ "ivat", "uṣī", "ivāṃsi" } };
		final NominalParadigm tenivatParad = NominalParadigm.generate("tenivat", "tenivas", tenivatData, 30);
		tenivatParad.setStemCutFactor(4);
		tenivatParad.addGender(Gender.NEU);
		tenivatParad.addWordList(List.of("tenivas", "tutudivas", "rarakṣivas"));
		paradigmMap.put("tenivat", tenivatParad);
		final String[][] cakrvatData = {
			{ "ṛvat", "ruṣī", "ṛvāṃsi" },
			{ "ṛvat", "ruṣī", "ṛvāṃsi" },
			{ "ruṣā", "ṛvadbhyām", "ṛvadbhiḥ" },
			{ "ruṣe", "ṛvadbhyām", "ṛvadbhyaḥ" },
			{ "ruṣaḥ", "ṛvadbhyām", "ṛvadbhyaḥ" },
			{ "ruṣaḥ", "ruṣoḥ", "ruṣām" },
			{ "ruṣi", "ruṣoḥ", "ṛvatsu" },
			{ "ṛvat", "ruṣī", "ṛvāṃsi" } };
		final NominalParadigm cakrvatParad = NominalParadigm.generate("cakṛvat", "cakṛvas", cakrvatData, 31);
		cakrvatParad.setStemCutFactor(4);
		cakrvatParad.addGender(Gender.NEU);
		paradigmMap.put("cakṛvat", cakrvatParad);
		final String[][] vidvatData = {
			{ "vat", "uṣī", "vāṃsi" },
			{ "vat", "uṣī", "vāṃsi" },
			{ "uṣā", "vadbhyām", "vadbhiḥ" },
			{ "uṣe", "vadbhyām", "vadbhyaḥ" },
			{ "uṣaḥ", "vadbhyām", "vadbhyaḥ" },
			{ "uṣaḥ", "uṣoḥ", "uṣām" },
			{ "uṣi", "uṣoḥ", "vatsu" },
			{ "vat", "uṣī", "vāṃsi" } };
		final NominalParadigm vidvatParad = NominalParadigm.generate("vidvat", "vidvas", vidvatData, 31);
		vidvatParad.setStemCutFactor(3);
		vidvatParad.addGender(Gender.NEU);
		paradigmMap.put("vidvat", vidvatParad);
		final String[][] susruvatData = {
			{ "vat", "vuṣī", "vāṃsi" },
			{ "vat", "vuṣī", "vāṃsi" },
			{ "vuṣā", "vadbhyām", "vadbhiḥ" },
			{ "vuṣe", "vadbhyām", "vadbhyaḥ" },
			{ "vuṣaḥ", "vadbhyām", "vadbhyaḥ" },
			{ "vuṣaḥ", "vuṣoḥ", "vuṣām" },
			{ "vuṣi", "vuṣoḥ", "vatsu" },
			{ "vat", "vuṣī", "vāṃsi" } };
		final NominalParadigm susruvatParad = NominalParadigm.generate("śuśruvat", "śuśruvas", susruvatData, 31);
		susruvatParad.setStemCutFactor(3);
		susruvatParad.addGender(Gender.NEU);
		paradigmMap.put("śuśruvat", susruvatParad);
		final String[][] ninivatData = {
			{ "īvat", "yuṣī", "īvāṃsi" },
			{ "īvat", "yuṣī", "īvāṃsi" },
			{ "yuṣā", "īvadbhyām", "īvadbhiḥ" },
			{ "yuṣe", "īvadbhyām", "īvadbhyaḥ" },
			{ "yuṣaḥ", "īvadbhyām", "īvadbhyaḥ" },
			{ "yuṣaḥ", "yuṣoḥ", "yuṣām" },
			{ "yuṣi", "yuṣoḥ", "īvatsu" },
			{ "īvat", "vuṣī", "īvāṃsi" } };
		final NominalParadigm ninivatParad = NominalParadigm.generate("ninīvat", "ninīvas", ninivatData, 31);
		ninivatParad.setStemCutFactor(4);
		ninivatParad.addGender(Gender.NEU);
		paradigmMap.put("ninīvat", ninivatParad);
		// feminine set
		final String[][] kathaData = {
			{ "ā", "e", "āḥ" },
			{ "ām", "e", "āḥ" },
			{ "ayā", "ābhyām", "ābhiḥ" },
			{ "āyai", "ābhyām", "ābhyaḥ" },
			{ "āyāḥ", "ābhyām", "ābhyaḥ" },
			{ "āyāḥ", "ayoḥ", "ānām" },
			{ "āyām", "ayoḥ", "āsu" },
			{ "e", "e", "āḥ" } };
		final NominalParadigm kathaParad = NominalParadigm.generate("kathā", "kathā", kathaData, 32);
		kathaParad.addGender(Gender.FEM);
		kathaParad.setWordGroupIdentifier("f:ā:");
		paradigmMap.put("kathā", kathaParad);
		final String[][] nadiData = {
			{ "ī", "yau", "yaḥ" },
			{ "īm", "yau", "īḥ" },
			{ "yā", "ībhyām", "ībhiḥ" },
			{ "yai", "ībhyām", "ībhyaḥ" },
			{ "yāḥ", "ībhyām", "ībhyaḥ" },
			{ "yāḥ", "yoḥ", "īnām" },
			{ "yām", "yoḥ", "īṣu" },
			{ "i", "yau", "yaḥ" } };
		final NominalParadigm nadiParad = NominalParadigm.generate("nadī", "nadī", nadiData, 33);
		nadiParad.addGender(Gender.FEM);
		nadiParad.setWordGroupIdentifier("f:ī:");
		paradigmMap.put("nadī", nadiParad);
		final String[][] dhihData = {
			{ "īḥ", "iyau", "iyaḥ" },
			{ "iyam", "iyau", "iyaḥ" },
			{ "iyā", "ībhyām", "ībhiḥ" },
			{ "iyai", "ībhyām", "ībhyaḥ" },
			{ "iyāḥ", "ībhyām", "ībhyaḥ" },
			{ "iyāḥ", "iyoḥ", "īnām" },
			{ "iyām", "iyoḥ", "īṣu" },
			{ "īḥ", "iyau", "iyaḥ" } };
		final NominalParadigm dhihParad = NominalParadigm.generate("dhīḥ", "dhī", dhihData, 34);
		dhihParad.addGender(Gender.FEM);
		dhihParad.setWordGroupIdentifier("f:ī1:");
		dhihParad.addEndings(Case.DAT, Number.SING, "iye");
		dhihParad.addEndings(Case.GEN, Number.PLU, "iyām");
		dhihParad.addEndings(Case.LOC, Number.SING, "iyi");
		paradigmMap.put("dhīḥ", dhihParad);
		final String[][] matihData = {
			{ "iḥ", "ī", "ayaḥ" },
			{ "im", "ī", "īḥ" },
			{ "yā", "ibhyām", "ibhiḥ" },
			{ "yai", "ibhyām", "ibhyaḥ" },
			{ "yāḥ", "ibhyām", "ibhyaḥ" },
			{ "yāḥ", "yoḥ", "īnām" },
			{ "yām", "yoḥ", "iṣu" },
			{ "e", "ī", "ayaḥ" } };
		final NominalParadigm matihParad = NominalParadigm.generate("matiḥ", "mati", matihData, 35);
		matihParad.addGender(Gender.FEM);
		matihParad.setWordGroupIdentifier("f:i:");
		matihParad.addEndings(Case.DAT, Number.SING, "aye");
		matihParad.addEndings(Case.ABL, Number.SING, "eḥ");
		matihParad.addEndings(Case.GEN, Number.SING, "eḥ");
		paradigmMap.put("matiḥ", matihParad);
		final String[][] vadhuhData = {
			{ "ūḥ", "vau", "vaḥ" },
			{ "ūm", "vau", "ūḥ" },
			{ "vā", "ūbhyām", "ūbhiḥ" },
			{ "vai", "ūbhyām", "ūbhyaḥ" },
			{ "vāḥ", "ūbhyām", "ūbhyaḥ" },
			{ "vāḥ", "voḥ", "ūnām" },
			{ "vām", "voḥ", "ūṣu" },
			{ "u", "vau", "vaḥ" } };
		final NominalParadigm vadhuParad = NominalParadigm.generate("vadhūḥ", "vadhū", vadhuhData, 36);
		vadhuParad.addGender(Gender.FEM);
		vadhuParad.setWordGroupIdentifier("f:ū:");
		paradigmMap.put("vadhūḥ", vadhuParad);
		final String[][] bhuhData = {
			{ "ūḥ", "uvau", "uvaḥ" },
			{ "uvam", "uvau", "uvaḥ" },
			{ "uvā", "ūbhyām", "ūbhiḥ" },
			{ "uvai", "ūbhyām", "ūbhyaḥ" },
			{ "uvāḥ", "ūbhyām", "ūbhyaḥ" },
			{ "uvāḥ", "uvoḥ", "ūnām" },
			{ "uvām", "uvoḥ", "ūṣu" },
			{ "ūḥ", "uvau", "uvaḥ" } };
		final NominalParadigm bhuhParad = NominalParadigm.generate("bhūḥ", "bhū", bhuhData, 37);
		bhuhParad.addGender(Gender.FEM);
		bhuhParad.setWordGroupIdentifier("f:ū1:");
		bhuhParad.addEndings(Case.DAT, Number.SING, "uve");
		bhuhParad.addEndings(Case.GEN, Number.PLU, "uvām");
		bhuhParad.addEndings(Case.LOC, Number.SING, "uvi");
		paradigmMap.put("bhūḥ", bhuhParad);
		final String[][] dhenuhData = {
			{ "uḥ", "ū", "avaḥ" },
			{ "um", "ū", "ūḥ" },
			{ "vā", "ubhyām", "ubhiḥ" },
			{ "vai", "ubhyām", "ubhyaḥ" },
			{ "vāḥ", "ubhyām", "ubhyaḥ" },
			{ "vāḥ", "voḥ", "ūnām" },
			{ "vām", "voḥ", "uṣu" },
			{ "o", "ū", "avaḥ" } };
		final NominalParadigm dhenuhParad = NominalParadigm.generate("dhenuḥ", "dhenu", dhenuhData, 38);
		dhenuhParad.addGender(Gender.FEM);
		dhenuhParad.setWordGroupIdentifier("f:u:");
		dhenuhParad.addEndings(Case.DAT, Number.SING, "ave");
		dhenuhParad.addEndings(Case.ABL, Number.SING, "oḥ");
		dhenuhParad.addEndings(Case.GEN, Number.SING, "oḥ");
		dhenuhParad.addEndings(Case.LOC, Number.SING, "au");
		paradigmMap.put("dhenuḥ", dhenuhParad);
		final String[][] nauhData = {
			{ "auḥ", "āvau", "āvaḥ" },
			{ "āvam", "āvau", "āvaḥ" },
			{ "āvā", "aubhyām", "aubhiḥ" },
			{ "āve", "aubhyām", "aubhyaḥ" },
			{ "āvaḥ", "aubhyām", "aubhyaḥ" },
			{ "āvaḥ", "āvoḥ", "āvām" },
			{ "āvi", "āvoḥ", "auṣu" },
			{ "auḥ", "āvau", "āvaḥ" } };
		final NominalParadigm nauhParad = NominalParadigm.generate("nauḥ", "nau", nauhData, 39);
		nauhParad.setStemCutFactor(2);
		nauhParad.addGender(Gender.FEM);
		nauhParad.setWordGroupIdentifier("f:au:");
		paradigmMap.put("nauḥ", nauhParad);
		final String[][] mataData = {
			{ "ā", "arau", "araḥ" },
			{ "aram", "arau", "ṝḥ" },
			{ "rā", "ṛbhyām", "ṛbhiḥ" },
			{ "re", "ṛbhyām", "ṛbhyaḥ" },
			{ "uḥ", "ṛbhyām", "ṛbhyaḥ" },
			{ "uḥ", "roḥ", "ṝṇām" },
			{ "ari", "roḥ", "ṛṣu" },
			{ "aḥ", "arau", "araḥ" } };
		final NominalParadigm mataParad = NominalParadigm.generate("mātā", "mātṛ", mataData, 40);
		mataParad.addGender(Gender.FEM);
		mataParad.setWordGroupIdentifier("f:ṛ:");
		paradigmMap.put("mātā", mataParad);
		// svasṛ, an irregular case of mātā, see Bucknell p. 26
		final String[][] svasaData = {
			{ "ā", "ārau", "āraḥ" },
			{ "āram", "ārau", "ṝḥ" },
			{ "rā", "ṛbhyām", "ṛbhiḥ" },
			{ "re", "ṛbhyām", "ṛbhyaḥ" },
			{ "uḥ", "ṛbhyām", "ṛbhyaḥ" },
			{ "uḥ", "roḥ", "ṝṇām" },
			{ "ari", "roḥ", "ṛṣu" },
			{ "aḥ", "ārau", "āraḥ" } };
		final NominalParadigm svasaParad = NominalParadigm.generate("svasā", "svasā", svasaData, 40);
		svasaParad.addGender(Gender.FEM);
		paradigmMap.put("svasā", svasaParad);
		// irregular masculine set
		final String[][] visvapahData = {
			{ "āḥ", "au", "āḥ" },
			{ "ām", "au", "aḥ" },
			{ "ā", "ābhyām", "ābhiḥ" },
			{ "e", "ābhyām", "ābhyaḥ" },
			{ "aḥ", "ābhyām", "ābhyaḥ" },
			{ "aḥ", "oḥ", "ām" },
			{ "i", "oḥ", "āsu" },
			{ "āḥ", "au", "āḥ" } };
		final NominalParadigm visvapahParad = NominalParadigm.generate("viśvapāḥ", "viśvapā", visvapahData, 41);
		visvapahParad.addGender(Gender.MAS);
		visvapahParad.setWordGroupIdentifier("m:ā:");
		paradigmMap.put("viśvapāḥ", visvapahParad);
		final String[][] yavakrihData = {
			{ "īḥ", "iyau", "iyaḥ" },
			{ "iyam", "iyau", "iyaḥ" },
			{ "iyā", "ībhyām", "ībhiḥ" },
			{ "iye", "ībhyām", "ībhyaḥ" },
			{ "iyaḥ", "ībhyām", "ībhyaḥ" },
			{ "iyaḥ", "iyoḥ", "iyām" },
			{ "iyi", "iyoḥ", "īṣu" },
			{ "īḥ", "iyau", "iyaḥ" } };
		final NominalParadigm yavakrihParad = NominalParadigm.generate("yavakrīḥ", "yavakrī", yavakrihData, 42);
		yavakrihParad.addGender(Gender.MAS);
		yavakrihParad.setWordGroupIdentifier("m:ī2:");
		paradigmMap.put("yavakrīḥ", yavakrihParad);
		final String[][] senanihData = {
			{ "īḥ", "yau", "yaḥ" },
			{ "yam", "yau", "yaḥ" },
			{ "yā", "ībhyām", "ībhiḥ" },
			{ "ye", "ībhyām", "ībhyaḥ" },
			{ "yaḥ", "ībhyām", "ībhyaḥ" },
			{ "yaḥ", "yoḥ", "yām" },
			{ "yām", "yoḥ", "īṣu" },
			{ "īḥ", "yau", "yaḥ" } };
		final NominalParadigm senanihParad = NominalParadigm.generate("senānīḥ", "senānī", senanihData, 43);
		senanihParad.addGender(Gender.MAS);
		senanihParad.setWordGroupIdentifier("m:ī1:");
		paradigmMap.put("senānīḥ", senanihParad);
		final String[][] rahData = {
			{ "āḥ", "āyau", "āyaḥ" },
			{ "āyam", "āyau", "āyaḥ" },
			{ "āyā", "ābhyām", "ābhiḥ" },
			{ "āye", "ābhyām", "ābhyaḥ" },
			{ "āyaḥ", "ābhyām", "ābhyaḥ" },
			{ "āyaḥ", "āyoḥ", "āyām" },
			{ "āyi", "āyoḥ", "āsu" },
			{ "āḥ", "āyau", "āyaḥ" } };
		final NominalParadigm rahParad = NominalParadigm.generate("rāḥ", "rai", rahData, 44);
		rahParad.setStemCutFactor(2);
		rahParad.addGender(Gender.MAS);
		paradigmMap.put("rāḥ", rahParad);
		final String[][] sakhaData = {
			{ "ā", "āyau", "āyaḥ" },
			{ "āyam", "āyau", "īn" },
			{ "yā", "ibhyām", "ibhiḥ" },
			{ "ye", "ibhyām", "ibhyaḥ" },
			{ "yuḥ", "ibhyām", "ibhyaḥ" },
			{ "yuḥ", "yoḥ", "īnām" },
			{ "yau", "yoḥ", "iṣu" },
			{ "e", "āyau", "āyaḥ" } };
		final NominalParadigm sakhaParad = NominalParadigm.generate("sakhā", "sakhi", sakhaData, 45);
		sakhaParad.addGender(Gender.MAS);
		paradigmMap.put("sakhā", sakhaParad);
		final String[][] patihData = {
			{ "iḥ", "ī", "ayaḥ" },
			{ "im", "ī", "īn" },
			{ "yā", "ibhyām", "ibhiḥ" },
			{ "ye", "ibhyām", "ibhyaḥ" },
			{ "yuḥ", "ibhyām", "ibhyaḥ" },
			{ "yuḥ", "yoḥ", "īnām" },
			{ "yau", "yoḥ", "iṣu" },
			{ "e", "ī", "ayaḥ" } };
		final NominalParadigm patihParad = NominalParadigm.generate("patiḥ", "pati", patihData, 46);
		patihParad.addGender(Gender.MAS);
		paradigmMap.put("patiḥ", patihParad);
		final String[][] patData = {
			{ "āt", "ādau", "ādaḥ" },
			{ "ādam", "ādau", "adaḥ" },
			{ "adā", "adbhyām", "adbhiḥ" },
			{ "ade", "adbhyām", "adbhyaḥ" },
			{ "adaḥ", "adbhyām", "adbhyaḥ" },
			{ "adaḥ", "adoḥ", "adām" },
			{ "adi", "adoḥ", "atsu" },
			{ "āt", "ādau", "ādaḥ" } };
		final NominalParadigm patParad = NominalParadigm.generate("pāt", "pad", patData, 47);
		patParad.setStemCutFactor(2);
		patParad.addGender(Gender.MAS);
		paradigmMap.put("pāt", patParad);
		final String[][] dvipatData = {
			{ "āt", "ādau", "ādaḥ" },
			{ "ādam", "ādau", "adaḥ" },
			{ "adā", "ādbhyām", "ādbhiḥ" },
			{ "ade", "ādbhyām", "ādbhyaḥ" },
			{ "adaḥ", "ādbhyām", "ādbhyaḥ" },
			{ "adaḥ", "adoḥ", "adām" },
			{ "adi", "adoḥ", "atsu" },
			{ "āt", "ādau", "ādaḥ" } };
		final NominalParadigm dvipatParad = NominalParadigm.generate("dvipāt", "dvipād", dvipatData, 48);
		dvipatParad.setStemCutFactor(2);
		dvipatParad.addGender(Gender.MAS);
		dvipatParad.addWordList(List.of("catuṣpād", "dvipād", "supād"));
		paradigmMap.put("dvipāt", dvipatParad);
		final String[][] anadvanData = {
			{ "vān", "vāhau", "vāhaḥ" },
			{ "vāham", "vāhau", "uhaḥ" },
			{ "uhā", "udbhyām", "udbhiḥ" },
			{ "uhe", "udbhyām", "udbhyaḥ" },
			{ "uhaḥ", "udbhyām", "udbhyaḥ" },
			{ "uhaḥ", "uhoḥ", "uhām" },
			{ "uhi", "uhoḥ", "utsu" },
			{ "van", "vāhau", "vāhaḥ" } };
		final NominalParadigm anadvanParad = NominalParadigm.generate("anaḍvān", "anaḍuḥ", anadvanData, 49);
		anadvanParad.setStemCutFactor(2);
		anadvanParad.addGender(Gender.MAS);
		paradigmMap.put("anaḍvān", anadvanParad);
		final String[][] pranData = {
			{ "ṅ", "ñcau", "ñcaḥ" },
			{ "ñcam", "ñcau", "caḥ" },
			{ "cā", "gbhyām", "gbhiḥ" },
			{ "ce", "gbhyām", "gbhyaḥ" },
			{ "caḥ", "gbhyām", "gbhyaḥ" },
			{ "caḥ", "coḥ", "cām" },
			{ "ci", "coḥ", "kṣu" },
			{ "ṅ", "ñcau", "ñcaḥ" } };
		final NominalParadigm pranParad = NominalParadigm.generate("prāṅ", "prāñc", pranData, 50);
		pranParad.setStemCutFactor(2);
		pranParad.addGender(Gender.MAS);
		pranParad.addWordList(List.of("avāñc", "prāñc"));
		paradigmMap.put("prāṅ", pranParad);
		final String[][] prakData = {
			{ "k", "cī", "ñci" },
			{ "ñcam", "ñcau", "caḥ" },
			{ "cā", "gbhyām", "gbhiḥ" },
			{ "ce", "gbhyām", "gbhyaḥ" },
			{ "caḥ", "gbhyām", "gbhyaḥ" },
			{ "caḥ", "coḥ", "cām" },
			{ "ci", "coḥ", "kṣu" },
			{ "ṅ", "ñcau", "ñcaḥ" } };
		final NominalParadigm prakParad = NominalParadigm.generate("prāk", "prāñc", prakData, 50);
		prakParad.setStemCutFactor(2);
		prakParad.addGender(Gender.NEU);
		prakParad.addWordList(List.of("avāñc", "prāñc"));
		paradigmMap.put("prāk", pranParad);
		final String[][] pratyanData = {
			{ "yaṅ", "yañcau", "yañcaḥ" },
			{ "yañcam", "yañcau", "īcaḥ" },
			{ "īcā", "yagbhyām", "yagbhiḥ" },
			{ "īce", "yagbhyām", "yagbhyaḥ" },
			{ "īcaḥ", "yagbhyām", "yagbhyaḥ" },
			{ "īcaḥ", "īcoḥ", "īcām" },
			{ "īci", "īcoḥ", "yakṣu" },
			{ "yaṅ", "yañcau", "yañcaḥ" } };
		final NominalParadigm pratyanParad = NominalParadigm.generate("pratyaṅ", "pratyañc", pratyanData, 51);
		pratyanParad.setStemCutFactor(4);
		pratyanParad.addGender(Gender.MAS);
		pratyanParad.addWordList(List.of("nyañc", "pratyañc"));
		paradigmMap.put("pratyaṅ", pratyanParad);
		final String[][] pratyakData = {
			{ "yak", "īcī", "yañci" },
			{ "yañcam", "yañcau", "īcaḥ" },
			{ "īcā", "yagbhyām", "yagbhiḥ" },
			{ "īce", "yagbhyām", "yagbhyaḥ" },
			{ "īcaḥ", "yagbhyām", "yagbhyaḥ" },
			{ "īcaḥ", "īcoḥ", "īcām" },
			{ "īci", "īcoḥ", "yakṣu" },
			{ "yaṅ", "yañcau", "yañcaḥ" } };
		final NominalParadigm pratyakParad = NominalParadigm.generate("pratyak", "pratyañc", pratyakData, 51);
		pratyakParad.setStemCutFactor(4);
		pratyakParad.addGender(Gender.NEU);
		pratyakParad.addWordList(List.of("nyañc", "pratyañc"));
		paradigmMap.put("pratyak", pratyakParad);
		final String[][] udanData = {
			{ "aṅ", "añcau", "añcaḥ" },
			{ "añcam", "añcau", "īcaḥ" },
			{ "īcā", "agbhyām", "agbhiḥ" },
			{ "īce", "agbhyām", "agbhyaḥ" },
			{ "īcaḥ", "agbhyām", "agbhyaḥ" },
			{ "īcaḥ", "īcoḥ", "īcām" },
			{ "īci", "īcoḥ", "akṣu" },
			{ "aṅ", "añcau", "añcaḥ" } };
		final NominalParadigm udanParad = NominalParadigm.generate("udaṅ", "udañc", udanData, 52);
		udanParad.setStemCutFactor(3);
		udanParad.addGender(Gender.MAS);
		paradigmMap.put("udaṅ", udanParad);
		final String[][] udakData = {
			{ "ak", "īcī", "añci" },
			{ "añcam", "añcau", "īcaḥ" },
			{ "īcā", "agbhyām", "agbhiḥ" },
			{ "īce", "agbhyām", "agbhyaḥ" },
			{ "īcaḥ", "agbhyām", "agbhyaḥ" },
			{ "īcaḥ", "īcoḥ", "īcām" },
			{ "īci", "īcoḥ", "akṣu" },
			{ "aṅ", "añcau", "añcaḥ" } };
		final NominalParadigm udakParad = NominalParadigm.generate("udak", "udañc", udakData, 52);
		udakParad.setStemCutFactor(3);
		udakParad.addGender(Gender.NEU);
		paradigmMap.put("udak", udakParad);
		final String[][] anvanData = {
			{ "vaṅ", "vañcau", "vañcaḥ" },
			{ "vañcam", "vañcau", "ūcaḥ" },
			{ "ūcā", "vagbhyām", "vagbhiḥ" },
			{ "ūce", "vagbhyām", "vagbhyaḥ" },
			{ "ūcaḥ", "vagbhyām", "vagbhyaḥ" },
			{ "ūcaḥ", "ūcoḥ", "ūcām" },
			{ "ūci", "ūcoḥ", "vakṣu" },
			{ "vaṅ", "vañcau", "vañcaḥ" } };
		final NominalParadigm anvanParad = NominalParadigm.generate("anvaṅ", "anvañc", anvanData, 53);
		anvanParad.setStemCutFactor(4);
		anvanParad.addGender(Gender.MAS);
		anvanParad.addWordList(List.of("anvañc", "viśvañc"));
		paradigmMap.put("anvaṅ", anvanParad);
		final String[][] anvakData = {
			{ "vak", "ūcī", "vañci" },
			{ "vañcam", "vañcau", "ūcaḥ" },
			{ "ūcā", "vagbhyām", "vagbhiḥ" },
			{ "ūce", "vagbhyām", "vagbhyaḥ" },
			{ "ūcaḥ", "vagbhyām", "vagbhyaḥ" },
			{ "ūcaḥ", "ūcoḥ", "ūcām" },
			{ "ūci", "ūcoḥ", "vakṣu" },
			{ "vaṅ", "vañcau", "vañcaḥ" } };
		final NominalParadigm anvakParad = NominalParadigm.generate("anvak", "anvañc", anvakData, 53);
		anvakParad.setStemCutFactor(4);
		anvakParad.addGender(Gender.NEU);
		anvakParad.addWordList(List.of("anvañc", "viśvañc"));
		paradigmMap.put("anvak", anvakParad);
		final String[][] triyanData = {
			{ "yaṅ", "yañcau", "yañcaḥ" },
			{ "yañcam", "yañcau", "aścaḥ" },
			{ "aścā", "yagbhyām", "yagbhiḥ" },
			{ "aśce", "yagbhyām", "yagbhyaḥ" },
			{ "aścaḥ", "yagbhyām", "yagbhyaḥ" },
			{ "aścaḥ", "aścoḥ", "aścām" },
			{ "aści", "aścoḥ", "yakṣu" },
			{ "yaṅ", "yañcau", "yañcaḥ" } };
		final NominalParadigm triyanParad = NominalParadigm.generate("tiryaṅ", "tiryañc", triyanData, 54);
		triyanParad.setStemCutFactor(4);
		triyanParad.addGender(Gender.MAS);
		paradigmMap.put("tiryaṅ", triyanParad);
		final String[][] triyakData = {
			{ "yak", "aścī", "yañcaḥ" },
			{ "yañcam", "yañcau", "aścaḥ" },
			{ "aścā", "yagbhyām", "yagbhiḥ" },
			{ "aśce", "yagbhyām", "yagbhyaḥ" },
			{ "aścaḥ", "yagbhyām", "yagbhyaḥ" },
			{ "aścaḥ", "aścoḥ", "aścām" },
			{ "aści", "aścoḥ", "yakṣu" },
			{ "yaṅ", "yañcau", "yañcaḥ" } };
		final NominalParadigm triyakParad = NominalParadigm.generate("tiryak", "tiryañc", triyakData, 54);
		triyakParad.setStemCutFactor(4);
		triyakParad.addGender(Gender.NEU);
		paradigmMap.put("tiryak", triyakParad);
		final String[][] pumanData = {
			{ "mān", "māṃsau", "māṃsaḥ" },
			{ "māṃsam", "māṃsau", "ṃsaḥ" },
			{ "ṃsā", "mbhyām", "mbhiḥ" },
			{ "ṃse", "mbhyām", "mbhyaḥ" },
			{ "ṃsaḥ", "mbhyām", "mbhyaḥ" },
			{ "ṃsaḥ", "ṃsoḥ", "ṃsām" },
			{ "ṃsi", "ṃsoḥ", "ṃsu" },
			{ "man", "māṃsau", "māṃsaḥ" } };
		final NominalParadigm pumanParad = NominalParadigm.generate("pumān", "puṃs", pumanData, 55);
		pumanParad.setStemCutFactor(2);
		pumanParad.addGender(Gender.MAS);
		paradigmMap.put("pumān", pumanParad);
		final String[][] panthahData = {
			{ "nthāḥ", "nthānau", "nthānaḥ" },
			{ "mthānam", "nthānau", "thaḥ" },
			{ "thā", "thibhyām", "thibhiḥ" },
			{ "the", "thibhyām", "thibhyaḥ" },
			{ "thaḥ", "thibhyām", "thibhyaḥ" },
			{ "thaḥ", "thoḥ", "thām" },
			{ "thi", "thoḥ", "thiṣu" },
			{ "nthāḥ", "nthānau", "nthānaḥ" } };
		final NominalParadigm panthahParad = NominalParadigm.generate("panthāḥ", "pathin", panthahData, 56);
		panthahParad.setStemCutFactor(4);
		panthahParad.addGender(Gender.MAS);
		panthahParad.addWordList(List.of("ṛbhukhin", "manthin", "panthin"));
		paradigmMap.put("panthāḥ", panthahParad);
		final String[][] pusaData = {
			{ "ā", "aṇau", "aṇaḥ" },
			{ "aṇam", "aṇau", "ṇaḥ" },
			{ "ṇā", "abhyām", "abhiḥ" },
			{ "ṇe", "abhyām", "abhyaḥ" },
			{ "ṇaḥ", "abhyām", "abhyaḥ" },
			{ "ṇaḥ", "ṇoḥ", "ṇām" },
			{ "ṇi", "ṇoḥ", "asu" },
			{ "an", "aṇau", "aṇaḥ" } };
		final NominalParadigm pusaParad = NominalParadigm.generate("pūṣā", "pūṣan", pusaData, 57);
		pusaParad.setStemCutFactor(2);
		pusaParad.addGender(Gender.MAS);
		pusaParad.addWordList(List.of("aryaman", "pūṣan"));
		paradigmMap.put("pūṣā", pusaParad);
		final String[][] gohaData = {
			{ "hā", "hanau", "hanaḥ" },
			{ "hanam", "hanau", "ghnaḥ" },
			{ "ghnā", "habhyām", "habhiḥ" },
			{ "ghne", "habhyām", "habhyaḥ" },
			{ "ghnaḥ", "habhyām", "habhyaḥ" },
			{ "ghnaḥ", "ghnoḥ", "ghnām" },
			{ "ghni", "ghnoḥ", "hasu" },
			{ "han", "hanau", "hanaḥ" } };
		final NominalParadigm gohaParad = NominalParadigm.generate("gohā", "gohan", gohaData, 58);
		gohaParad.setStemCutFactor(3);
		gohaParad.addGender(Gender.MAS);
		gohaParad.addWordList(List.of("goham", "brahmahan"));
		gohaParad.addEndings(Case.LOC, Number.SING, "hani");
		paradigmMap.put("gohā", gohaParad);
		final String[][] svaData = {
			{ "vā", "vānau", "vānaḥ" },
			{ "vānam", "vānau", "unaḥ" },
			{ "unā", "vabhyām", "vabhiḥ" },
			{ "une", "vabhyām", "vabhyaḥ" },
			{ "unaḥ", "vabhyām", "vabhyaḥ" },
			{ "unaḥ", "unoḥ", "unām" },
			{ "uni", "unoḥ", "vasu" },
			{ "van", "vānau", "vānaḥ" } };
		final NominalParadigm svaParad = NominalParadigm.generate("śvā", "śvan", svaData, 59);
		svaParad.setStemCutFactor(3);
		svaParad.addGender(Gender.MAS);
		paradigmMap.put("śvā", svaParad);
		final String[][] yuvaData = {
			{ "uvā", "uvānau", "uvānaḥ" },
			{ "uvānam", "uvānau", "ūnaḥ" },
			{ "ūnā", "uvabhyām", "uvabhiḥ" },
			{ "ūne", "uvabhyām", "uvabhyaḥ" },
			{ "ūnaḥ", "uvabhyām", "uvabhyaḥ" },
			{ "ūnaḥ", "ūnoḥ", "ūnām" },
			{ "ūni", "ūnoḥ", "uvasu" },
			{ "uvan", "uvānau", "uvānaḥ" } };
		final NominalParadigm yuvaParad = NominalParadigm.generate("yuvā", "yuvan", yuvaData, 60);
		yuvaParad.setStemCutFactor(4);
		yuvaParad.addGender(Gender.MAS);
		paradigmMap.put("yuvā", yuvaParad);
		final String[][] maghavaData = {
			{ "avā", "avānau", "avānaḥ" },
			{ "avānam", "avānau", "onaḥ" },
			{ "onā", "avabhyām", "avabhiḥ" },
			{ "one", "avabhyām", "avabhyaḥ" },
			{ "onaḥ", "avabhyām", "avabhyaḥ" },
			{ "onaḥ", "onoḥ", "onām" },
			{ "oni", "onoḥ", "avasu" },
			{ "avan", "avānau", "avānaḥ" } };
		final NominalParadigm maghavaParad = NominalParadigm.generate("maghavā", "maghavan", maghavaData, 61);
		maghavaParad.setStemCutFactor(4);
		maghavaParad.addGender(Gender.MAS);
		paradigmMap.put("maghavā", maghavaParad);
		final String[][] mahanData = {
			{ "ān", "āntau", "āntaḥ" },
			{ "āntam", "āntau", "ataḥ" },
			{ "atā", "adbhyām", "adbhiḥ" },
			{ "ate", "adbhyām", "adbhyaḥ" },
			{ "ataḥ", "adbhyām", "adbhyaḥ" },
			{ "ataḥ", "atoḥ", "atām" },
			{ "ati", "atoḥ", "atsu" },
			{ "an", "āntau", "āntaḥ" } };
		final NominalParadigm mahanParad = NominalParadigm.generate("mahān", "mahat", mahanData, 62);
		mahanParad.setStemCutFactor(2);
		mahanParad.addGender(Gender.MAS);
		paradigmMap.put("mahān", mahanParad);
		// irregular neuter set
		final String[][] mahatData = {
			{ "at", "atī", "ānti" },
			{ "at", "atī", "ānti" },
			{ "atā", "adbhyām", "adbhiḥ" },
			{ "ate", "adbhyām", "adbhyaḥ" },
			{ "ataḥ", "adbhyām", "adbhyaḥ" },
			{ "ataḥ", "atoḥ", "atām" },
			{ "ati", "atoḥ", "atsu" },
			{ "at", "atī", "ānti" } };
		final NominalParadigm mahatParad = NominalParadigm.generate("mahat", "mahat", mahatData, 63);
		mahatParad.setStemCutFactor(2);
		mahatParad.addGender(Gender.NEU);
		paradigmMap.put("mahat", mahatParad);
		final String[][] dadhiData = {
			{ "i", "inī", "īni" },
			{ "i", "inī", "īni" },
			{ "nā", "ibhyām", "ibhiḥ" },
			{ "ne", "ibhyām", "ibhyaḥ" },
			{ "naḥ", "ibhyām", "ibhyaḥ" },
			{ "naḥ", "noḥ", "nām" },
			{ "ni", "noḥ", "iṣu" },
			{ "e", "inī", "īni" } };
		final NominalParadigm dadhiParad = NominalParadigm.generate("dadhi", "dadhi", dadhiData, 64);
		dadhiParad.addGender(Gender.NEU);
		dadhiParad.addWordList(List.of("akśi", "asthi", "dadhi", "sakthi"));
		dadhiParad.addEndings(Case.LOC, Number.SING, "ani");
		dadhiParad.addEndings(Case.VOC, Number.SING, "i");
		paradigmMap.put("dadhi", dadhiParad);
		final String[][] ahahData = {
			{ "aḥ", "nī", "āni" },
			{ "aḥ", "nī", "āni" },
			{ "nā", "obhyām", "obhiḥ" },
			{ "ne", "obhyām", "obhyaḥ" },
			{ "naḥ", "obhyām", "obhyaḥ" },
			{ "naḥ", "noḥ", "nām" },
			{ "ni", "noḥ", "aḥsu" },
			{ "aḥ", "nī", "āni" } };
		final NominalParadigm ahahParad = NominalParadigm.generate("ahaḥ", "ahan", ahahData, 65);
		ahahParad.setStemCutFactor(2);
		ahahParad.addGender(Gender.NEU);
		ahahParad.addEndings(Case.NOM, Number.DUAL, "anī");
		ahahParad.addEndings(Case.ACC, Number.DUAL, "anī");
		ahahParad.addEndings(Case.LOC, Number.SING, "ani");
		ahahParad.addEndings(Case.VOC, Number.DUAL, "anī");
		paradigmMap.put("ahaḥ", ahahParad);
		// irregular feminine set
		final String[][] jaraData = {
			{ "ā", "asau", "asaḥ" },
			{ "asam", "asau", "asaḥ" },
			{ "asā", "ābhyām", "ābhiḥ" },
			{ "ase", "ābhyām", "ābhyaḥ" },
			{ "asaḥ", "ābhyām", "ābhyaḥ" },
			{ "asaḥ", "asoḥ", "asām" },
			{ "asi", "asoḥ", "āsu" },
			{ "e", "asau", "asaḥ" } };
		final NominalParadigm jaraParad = NominalParadigm.generate("jarā", "jarā", jaraData, 66);
		jaraParad.addGender(Gender.FEM);
		paradigmMap.put("jarā", jaraParad);
		final String[][] striData = {
			{ "ī", "iyau", "iyaḥ" },
			{ "iyam", "iyau", "iyaḥ" },
			{ "iyā", "ībhyām", "ībhiḥ" },
			{ "iyai", "ībhyām", "ībhyaḥ" },
			{ "iyāḥ", "ībhyām", "ībhyaḥ" },
			{ "iyāḥ", "iyoḥ", "īṇām" },
			{ "iyām", "iyoḥ", "īṣu" },
			{ "i", "iyau", "iyaḥ" } };
		final NominalParadigm striParad = NominalParadigm.generate("strī", "strī", striData, 67);
		striParad.addGender(Gender.FEM);
		striParad.addEndings(Case.ACC, Number.SING, "īm");
		striParad.addEndings(Case.ACC, Number.PLU, "īḥ");
		paradigmMap.put("strī", striParad);
		final String[][] laksmihData = {
			{ "īḥ", "yau", "yaḥ" },
			{ "īm", "yau", "īḥ" },
			{ "yā", "ībhyām", "ībhiḥ" },
			{ "yai", "ībhyām", "ībhyaḥ" },
			{ "yāḥ", "ībhyām", "ībhyaḥ" },
			{ "yāḥ", "yoḥ", "īṇām" },
			{ "yām", "yoḥ", "īṣu" },
			{ "i", "yau", "yaḥ" } };
		final NominalParadigm laksmihParad = NominalParadigm.generate("lakṣmīḥ", "lakṣmī", laksmihData, 68);
		laksmihParad.addGender(Gender.FEM);
		laksmihParad.addWordList(List.of("tantrī", "tarī", "lakṣmī"));
		paradigmMap.put("lakṣmīḥ", laksmihParad);
		final String[][] dyauhData = {
			{ "yauḥ", "ivau", "ivaḥ" },
			{ "ivam", "ivau", "ivaḥ" },
			{ "ivā", "yubhyām", "yubhiḥ" },
			{ "ive", "yubhyām", "yubhyaḥ" },
			{ "ivaḥ", "yubhyām", "yubhyaḥ" },
			{ "ivaḥ", "ivoḥ", "ivām" },
			{ "ivi", "ivoḥ", "yuṣu" },
			{ "yauḥ", "ivau", "ivaḥ" } };
		final NominalParadigm dyauhParad = NominalParadigm.generate("dyauḥ", "dyo", dyauhData, 69);
		dyauhParad.setStemCutFactor(2);
		dyauhParad.addGender(Gender.FEM);
		paradigmMap.put("dyauḥ", dyauhParad);
		final String[][] gauhData = {
			{ "auḥ", "āvau", "āvaḥ" },
			{ "ām", "āvau", "āḥ" },
			{ "avā", "obhyām", "obhiḥ" },
			{ "ave", "obhyām", "obhyaḥ" },
			{ "oḥ", "obhyām", "obhyaḥ" },
			{ "oḥ", "avoḥ", "avām" },
			{ "avi", "avoḥ", "oṣu" },
			{ "auḥ", "āvau", "āvaḥ" } };
		final NominalParadigm gauhParad = NominalParadigm.generate("gauḥ", "go", gauhData, 70);
		gauhParad.addGender(Gender.FEM);
		paradigmMap.put("gauḥ", gauhParad);
		final String[][] apahData = {
			{ "", "", "āpaḥ" },
			{ "", "", "apaḥ" },
			{ "", "", "adbhiḥ" },
			{ "", "", "adbhyaḥ" },
			{ "", "", "adbhyaḥ" },
			{ "", "", "apām" },
			{ "", "", "apsu" },
			{ "", "", "āpaḥ" } };
		final NominalParadigm apahParad = NominalParadigm.generate("āpaḥ", "ap", apahData, 71);
		apahParad.setStemCutFactor(2);
		apahParad.addGender(Gender.FEM);
		paradigmMap.put("āpaḥ", apahParad);
		final String[][] gihData = {
			{ "īḥ", "irau", "iraḥ" },
			{ "iram", "irau", "iraḥ" },
			{ "irā", "īrbhyām", "īrbhiḥ" },
			{ "ire", "īrbhyām", "īrbhyaḥ" },
			{ "iraḥ", "īrbhyām", "īrbhyaḥ" },
			{ "iraḥ", "iroḥ", "irām" },
			{ "iri", "iroḥ", "īrṣu" },
			{ "īḥ", "irau", "iraḥ" } };
		final NominalParadigm gihParad = NominalParadigm.generate("gīḥ", "gir", gihData, 72);
		gihParad.setStemCutFactor(2);
		gihParad.addGender(Gender.FEM);
		paradigmMap.put("gīḥ", gihParad);
		final String[][] asihData = {
			{ "īḥ", "iṣau", "iṣaḥ" },
			{ "iṣam", "iṣau", "iṣaḥ" },
			{ "iṣā", "īrbhyām", "īrbhiḥ" },
			{ "iṣe", "īrbhyām", "īrbhyaḥ" },
			{ "iṣaḥ", "īrbhyām", "īrbhyaḥ" },
			{ "iṣaḥ", "iṣoḥ", "iṣām" },
			{ "iṣi", "iṣoḥ", "īḥṣu" },
			{ "īḥ", "iṣau", "iṣaḥ" } };
		final NominalParadigm asihParad = NominalParadigm.generate("āśīḥ", "āśir", asihData, 72);
		asihParad.setStemCutFactor(2);
		asihParad.addGender(Gender.FEM);
		paradigmMap.put("āśīḥ", asihParad);
		final String[][] puhData = {
			{ "ūḥ", "urau", "uraḥ" },
			{ "uram", "urau", "uraḥ" },
			{ "urā", "ūrbhyām", "ūrbhiḥ" },
			{ "ure", "ūrbhyām", "ūrbhyaḥ" },
			{ "uraḥ", "ūrbhyām", "ūrbhyaḥ" },
			{ "uraḥ", "uroḥ", "urām" },
			{ "uri", "uroḥ", "ūrṣu" },
			{ "ūḥ", "urau", "uraḥ" } };
		final NominalParadigm puhParad = NominalParadigm.generate("pūḥ", "pur", puhData, 73);
		puhParad.setStemCutFactor(2);
		puhParad.addGender(Gender.FEM);
		puhParad.addWordList(List.of("dhur", "pur"));
		paradigmMap.put("pūḥ", puhParad);
		// numerals
		// cardinals
		final String[][] ekahData = {
			{ "aḥ", "", "" },
			{ "am", "", "" },
			{ "ena", "", "" },
			{ "asmai", "", "" },
			{ "asmāt", "", "" },
			{ "asya", "", "" },
			{ "asmin", "", "" },
			{ "", "", "" } };
		final NominalParadigm ekahParad = NominalParadigm.generate("ekaḥ", "eka", ekahData);
		ekahParad.addGender(Gender.MAS);
		ekahParad.setWordType(WordType.NUMERAL);
		paradigmMap.put("ekaḥ", ekahParad);
		final String[][] ekamData = {
			{ "am", "", "" },
			{ "am", "", "" },
			{ "ena", "", "" },
			{ "asmai", "", "" },
			{ "asmāt", "", "" },
			{ "asya", "", "" },
			{ "asmin", "", "" },
			{ "", "", "" } };
		final NominalParadigm ekamParad = NominalParadigm.generate("ekam", "eka", ekamData);
		ekamParad.addGender(Gender.NEU);
		ekamParad.setWordType(WordType.NUMERAL);
		paradigmMap.put("ekam", ekamParad);
		final String[][] ekaData = {
			{ "ā", "", "" },
			{ "ām", "", "" },
			{ "ayā", "", "" },
			{ "asyai", "", "" },
			{ "asyāḥ", "", "" },
			{ "asyāḥ", "", "" },
			{ "asyām", "", "" },
			{ "", "", "" } };
		final NominalParadigm ekaParad = NominalParadigm.generate("ekā", "eka", ekaData);
		ekaParad.addGender(Gender.FEM);
		ekaParad.setWordType(WordType.NUMERAL);
		paradigmMap.put("ekā", ekaParad);
		final String[][] dvauData = {
			{ "", "au", "" },
			{ "", "au", "" },
			{ "", "ābhyām", "" },
			{ "", "ābhyām", "" },
			{ "", "ābhyām", "" },
			{ "", "ayoḥ", "" },
			{ "", "ayoḥ", "" },
			{ "", "", "" } };
		final NominalParadigm dvauParad = NominalParadigm.generate("dvau", "dvi", dvauData);
		dvauParad.addGender(Gender.MAS);
		dvauParad.setWordType(WordType.NUMERAL);
		paradigmMap.put("dvau", dvauParad);
		final String[][] dveData = {
			{ "", "e", "" },
			{ "", "e", "" },
			{ "", "ābhyām", "" },
			{ "", "ābhyām", "" },
			{ "", "ābhyām", "" },
			{ "", "ayoḥ", "" },
			{ "", "ayoḥ", "" },
			{ "", "", "" } };
		final NominalParadigm dveParad = NominalParadigm.generate("dve", "dvi", dveData);
		dveParad.addGender(Gender.FEM, Gender.NEU);
		dveParad.setWordType(WordType.NUMERAL);
		paradigmMap.put("dve", dveParad);
		final String[][] trayahData = {
			{ "", "", "ayaḥ" },
			{ "", "", "īn" },
			{ "", "", "ibhiḥ" },
			{ "", "", "ibhyaḥ" },
			{ "", "", "ibhyaḥ" },
			{ "", "", "ayāṇām" },
			{ "", "", "iṣu" },
			{ "", "", "" } };
		final NominalParadigm trayahParad = NominalParadigm.generate("trayaḥ", "tri", trayahData);
		trayahParad.addGender(Gender.MAS);
		trayahParad.setWordType(WordType.NUMERAL);
		paradigmMap.put("trayaḥ", trayahParad);
		final String[][] triniData = {
			{ "", "", "īṇi" },
			{ "", "", "īṇi" },
			{ "", "", "ibhiḥ" },
			{ "", "", "ibhyaḥ" },
			{ "", "", "ibhyaḥ" },
			{ "", "", "ayāṇām" },
			{ "", "", "iṣu" },
			{ "", "", "" } };
		final NominalParadigm triniParad = NominalParadigm.generate("trīṇi", "tri", triniData);
		triniParad.addGender(Gender.NEU);
		triniParad.setWordType(WordType.NUMERAL);
		paradigmMap.put("trīṇi", triniParad);
		final String[][] tisrahData = {
			{ "", "", "israḥ" },
			{ "", "", "israḥ" },
			{ "", "", "isṛbhiḥ" },
			{ "", "", "isṛbhyaḥ" },
			{ "", "", "isṛbhyaḥ" },
			{ "", "", "isṛṇām" },
			{ "", "", "isṛṣu" },
			{ "", "", "" } };
		final NominalParadigm tisrahParad = NominalParadigm.generate("tisraḥ", "tri", tisrahData);
		tisrahParad.setStemCutFactor(2);
		tisrahParad.addGender(Gender.FEM);
		tisrahParad.setWordType(WordType.NUMERAL);
		paradigmMap.put("tisraḥ", tisrahParad);
		final String[][] catvarahData = {
			{ "", "", "vāraḥ" },
			{ "", "", "uraḥ" },
			{ "", "", "urbhiḥ" },
			{ "", "", "urbhyaḥ" },
			{ "", "", "urbhyaḥ" },
			{ "", "", "urṇām" },
			{ "", "", "urṣu" },
			{ "", "", "" } };
		final NominalParadigm catvarahParad = NominalParadigm.generate("catvāraḥ", "catur", catvarahData);
		catvarahParad.setStemCutFactor(2);
		catvarahParad.addGender(Gender.MAS);
		catvarahParad.setWordType(WordType.NUMERAL);
		paradigmMap.put("catvāraḥ", catvarahParad);
		final String[][] catvariData = {
			{ "", "", "vāri" },
			{ "", "", "vāri" },
			{ "", "", "urbhiḥ" },
			{ "", "", "urbhyaḥ" },
			{ "", "", "urbhyaḥ" },
			{ "", "", "urṇām" },
			{ "", "", "urṣu" },
			{ "", "", "" } };
		final NominalParadigm catvariParad = NominalParadigm.generate("catvāri", "catur", catvariData);
		catvariParad.setStemCutFactor(2);
		catvariParad.addGender(Gender.NEU);
		catvariParad.setWordType(WordType.NUMERAL);
		paradigmMap.put("catvāri", catvariParad);
		final String[][] catasrahData = {
			{ "", "", "asraḥ" },
			{ "", "", "asraḥ" },
			{ "", "", "asṛbhiḥ" },
			{ "", "", "asṛbhyaḥ" },
			{ "", "", "asṛbhyaḥ" },
			{ "", "", "asṛṇām" },
			{ "", "", "asṛṣu" },
			{ "", "", "" } };
		final NominalParadigm catasrahParad = NominalParadigm.generate("catasraḥ", "catur", catasrahData);
		catasrahParad.setStemCutFactor(2);
		catasrahParad.addGender(Gender.FEM);
		catasrahParad.setWordType(WordType.NUMERAL);
		paradigmMap.put("catasraḥ", catasrahParad);
		final String[][] pancaData = {
			{ "", "", "a" },
			{ "", "", "a" },
			{ "", "", "abhiḥ" },
			{ "", "", "abhyaḥ" },
			{ "", "", "abhyaḥ" },
			{ "", "", "ānām" },
			{ "", "", "asu" },
			{ "", "", "" } };
		final NominalParadigm pancaParad = NominalParadigm.generate("pañca", "pañca", pancaData);
		pancaParad.addGender(Gender.MAS, Gender.FEM, Gender.NEU);
		pancaParad.setWordType(WordType.NUMERAL);
		paradigmMap.put("pañca", pancaParad);
		final String[][] satData = {
			{ "", "", "ṭ" },
			{ "", "", "ṭ" },
			{ "", "", "ḍbhiḥ" },
			{ "", "", "ḍbhyaḥ" },
			{ "", "", "ḍbhyaḥ" },
			{ "", "", "ṇṇām" },
			{ "", "", "ṭsu" },
			{ "", "", "" } };
		final NominalParadigm satParad = NominalParadigm.generate("ṣaṭ", "ṣaṭ", satData);
		satParad.addGender(Gender.MAS, Gender.FEM, Gender.NEU);
		satParad.setWordType(WordType.NUMERAL);
		paradigmMap.put("ṣaṭ", satParad);
		final String[][] saptaData = {
			{ "", "", "a" },
			{ "", "", "a" },
			{ "", "", "abhiḥ" },
			{ "", "", "abhyaḥ" },
			{ "", "", "abhyaḥ" },
			{ "", "", "ānām" },
			{ "", "", "asu" },
			{ "", "", "" } };
		final NominalParadigm saptaParad = NominalParadigm.generate("sapta", "sapta", saptaData);
		saptaParad.addGender(Gender.MAS, Gender.FEM, Gender.NEU);
		saptaParad.setWordType(WordType.NUMERAL);
		paradigmMap.put("sapta", saptaParad);
		final String[][] astaData = {
			{ "", "", "a" },
			{ "", "", "a" },
			{ "", "", "ābhiḥ" },
			{ "", "", "ābhyaḥ" },
			{ "", "", "ābhyaḥ" },
			{ "", "", "ānām" },
			{ "", "", "āsu" },
			{ "", "", "" } };
		final NominalParadigm astaParad = NominalParadigm.generate("aṣṭa", "aṣṭa", astaData);
		astaParad.addGender(Gender.MAS, Gender.FEM, Gender.NEU);
		astaParad.setWordType(WordType.NUMERAL);
		astaParad.addEndings(Case.NOM, Number.PLU, "u");
		astaParad.addEndings(Case.ACC, Number.PLU, "u");
		paradigmMap.put("aṣṭa", astaParad);
		final String[][] navaData = {
			{ "", "", "a" },
			{ "", "", "a" },
			{ "", "", "abhiḥ" },
			{ "", "", "abhyaḥ" },
			{ "", "", "abhyaḥ" },
			{ "", "", "ānām" },
			{ "", "", "asu" },
			{ "", "", "" } };
		final NominalParadigm navaParad = NominalParadigm.generate("nava", "nava", navaData);
		navaParad.addGender(Gender.MAS, Gender.FEM, Gender.NEU);
		navaParad.setWordType(WordType.NUMERAL);
		paradigmMap.put("nava", navaParad);
		final String[][] dasaData = {
			{ "", "", "a" },
			{ "", "", "a" },
			{ "", "", "abhiḥ" },
			{ "", "", "abhyaḥ" },
			{ "", "", "abhyaḥ" },
			{ "", "", "ānām" },
			{ "", "", "asu" },
			{ "", "", "" } };
		final NominalParadigm dasaParad = NominalParadigm.generate("daśa", "daśa", dasaData);
		dasaParad.addGender(Gender.MAS, Gender.FEM, Gender.NEU);
		dasaParad.setWordType(WordType.NUMERAL);
		paradigmMap.put("daśa", dasaParad);
		// ordinals
		final NominalParadigm prathamahParad = NominalParadigm.duplicate(devahParad, "prathamaḥ", "prathama");
		prathamahParad.clearBucknellNumber();
		prathamahParad.setWordType(WordType.NUMERAL);
		prathamahParad.addEndings(Case.DAT, Number.SING, "asmai");
		prathamahParad.addEndings(Case.ABL, Number.SING, "asmāt");
		prathamahParad.addEndings(Case.GEN, Number.SING, "asya");
		prathamahParad.addEndings(Case.LOC, Number.SING, "asmin");
		paradigmMap.put("prathamaḥ", prathamahParad);
		final NominalParadigm prathamamParad = NominalParadigm.duplicate(phalamParad, "prathamam", "prathama");
		prathamamParad.clearBucknellNumber();
		prathamamParad.setWordType(WordType.NUMERAL);
		prathamamParad.addEndings(Case.DAT, Number.SING, "asmai");
		prathamamParad.addEndings(Case.ABL, Number.SING, "asmāt");
		prathamamParad.addEndings(Case.GEN, Number.SING, "asya");
		prathamamParad.addEndings(Case.LOC, Number.SING, "asmin");
		paradigmMap.put("prathamam", prathamamParad);
		final NominalParadigm prathamaParad = NominalParadigm.duplicate(kathaParad, "prathamā", "prathama");
		prathamaParad.clearBucknellNumber();
		prathamaParad.setWordType(WordType.NUMERAL);
		prathamaParad.addEndings(Case.DAT, Number.SING, "asyai");
		prathamaParad.addEndings(Case.ABL, Number.SING, "asyāḥ");
		prathamaParad.addEndings(Case.GEN, Number.SING, "asyāḥ");
		prathamaParad.addEndings(Case.LOC, Number.SING, "asyām");
		paradigmMap.put("prathamā", prathamaParad);
		// demonstratives
		// idam
		final String[][] ayamData = {
			{ "ayam", "imau", "ime" },
			{ "imam", "imau", "imān" },
			{ "anena", "ābhyām", "ebhiḥ" },
			{ "asmai", "ābhyām", "ebhyaḥ" },
			{ "asmāt", "ābhyām", "ebhyaḥ" },
			{ "asya", "anayoḥ", "eṣām" },
			{ "asmin", "anayoḥ", "eṣu" },
			{ "", "", "" } };
		final NominalParadigm ayamParad = NominalParadigm.generate("ayam", "idam", ayamData);
		ayamParad.setStemCutFactor(4);
		ayamParad.addGender(Gender.MAS);
		ayamParad.setWordType(WordType.PRONOUN);
		ayamParad.addEndings(Case.ACC, Number.SING, "enam");
		ayamParad.addEndings(Case.ACC, Number.DUAL, "enau");
		ayamParad.addEndings(Case.ACC, Number.PLU, "enān");
		ayamParad.addEndings(Case.INS, Number.SING, "enena");
		ayamParad.addEndings(Case.GEN, Number.DUAL, "enayoḥ");
		ayamParad.addEndings(Case.LOC, Number.DUAL, "enayoḥ");
		paradigmMap.put("ayam", ayamParad);
		final String[][] idamData = {
			{ "idam", "ime", "imāni" },
			{ "idam", "ime", "imāni" },
			{ "anena", "ābhyām", "ebhiḥ" },
			{ "asmai", "ābhyām", "ebhyaḥ" },
			{ "asmāt", "ābhyām", "ebhyaḥ" },
			{ "asya", "anayoḥ", "eṣām" },
			{ "asmin", "anayoḥ", "eṣu" },
			{ "", "", "" } };
		final NominalParadigm idamParad = NominalParadigm.generate("idam", "idam", idamData);
		idamParad.setStemCutFactor(4);
		idamParad.addGender(Gender.NEU);
		idamParad.setWordType(WordType.PRONOUN);
		idamParad.addEndings(Case.ACC, Number.SING, "enat");
		idamParad.addEndings(Case.ACC, Number.DUAL, "ene");
		idamParad.addEndings(Case.ACC, Number.PLU, "enāni");
		idamParad.addEndings(Case.INS, Number.SING, "enena");
		idamParad.addEndings(Case.GEN, Number.DUAL, "enayoḥ");
		idamParad.addEndings(Case.LOC, Number.DUAL, "enayoḥ");
		paradigmMap.put("idam", idamParad);
		final String[][] iyamData = {
			{ "iyam", "ime", "imāḥ" },
			{ "imām", "ime", "imāḥ" },
			{ "anayā", "ābhyām", "ābhiḥ" },
			{ "asyai", "ābhyām", "ābhyaḥ" },
			{ "asyāḥ", "ābhyām", "ābhyaḥ" },
			{ "asyāḥ", "anayoḥ", "āsām" },
			{ "asyām", "anayoḥ", "āsu" },
			{ "", "", "" } };
		final NominalParadigm iyamParad = NominalParadigm.generate("iyam", "idam", iyamData);
		iyamParad.setStemCutFactor(4);
		iyamParad.addGender(Gender.FEM);
		iyamParad.setWordType(WordType.PRONOUN);
		iyamParad.addEndings(Case.ACC, Number.SING, "enām");
		iyamParad.addEndings(Case.ACC, Number.DUAL, "ene");
		iyamParad.addEndings(Case.ACC, Number.PLU, "enāḥ");
		iyamParad.addEndings(Case.INS, Number.SING, "enayā");
		iyamParad.addEndings(Case.GEN, Number.DUAL, "enayoḥ");
		iyamParad.addEndings(Case.LOC, Number.DUAL, "enayoḥ");
		paradigmMap.put("iyam", iyamParad);
		final String[][] asauData = {
			{ "asau", "amū", "amī" },
			{ "amum", "amū", "amūn" },
			{ "amunā", "amūbhyām", "amībhiḥ" },
			{ "amuṣmai", "amūbhyām", "amībhyaḥ" },
			{ "amuṣmāt", "amūbhyām", "amībhyaḥ" },
			{ "amuṣya", "amuyoḥ", "amīṣām" },
			{ "amuṣmin", "amuyoḥ", "amīṣu" },
			{ "", "", "" } };
		// adas
		final NominalParadigm asauParad = NominalParadigm.generate("asau", "adas", asauData);
		asauParad.setStemCutFactor(4);
		asauParad.addGender(Gender.MAS);
		asauParad.setWordType(WordType.PRONOUN);
		paradigmMap.put("asau", asauParad);
		final String[][] adahData = {
			{ "adaḥ", "amū", "amūni" },
			{ "adaḥ", "amū", "amūni" },
			{ "amunā", "amūbhyām", "amībhiḥ" },
			{ "amuṣmai", "amūbhyām", "amībhyaḥ" },
			{ "amuṣmāt", "amūbhyām", "amībhyaḥ" },
			{ "amuṣya", "amuyoḥ", "amīṣām" },
			{ "amuṣmin", "amuyoḥ", "amīṣu" },
			{ "", "", "" } };
		final NominalParadigm adahParad = NominalParadigm.generate("adaḥ", "adas", adahData);
		adahParad.setStemCutFactor(4);
		adahParad.addGender(Gender.NEU);
		adahParad.setWordType(WordType.PRONOUN);
		paradigmMap.put("adaḥ", adahParad);
		final String[][] amuhData = {
			{ "asau", "amū", "amūḥ" },
			{ "amūm", "amū", "amūḥ" },
			{ "amuyā", "amūbhyām", "amūbhiḥ" },
			{ "amuṣyai", "amūbhyām", "amūbhyaḥ" },
			{ "amuṣyāḥ", "amūbhyām", "amūbhyaḥ" },
			{ "amuṣyāḥ", "amuyoḥ", "amūṣām" },
			{ "amuṣyām", "amuyoḥ", "amūṣu" },
			{ "", "", "" } };
		final NominalParadigm amuhParad = NominalParadigm.generate("amuḥ", "adas", amuhData);
		amuhParad.setStemCutFactor(4);
		amuhParad.addGender(Gender.FEM);
		amuhParad.setWordType(WordType.PRONOUN);
		paradigmMap.put("amuḥ", amuhParad);
		// etad
		final String[][] esahData = {
			{ "saḥ", "tau", "te" },
			{ "tam", "tau", "tān" },
			{ "tena", "tābhyām", "taiḥ" },
			{ "tasmai", "tābhyām", "tebhyaḥ" },
			{ "tasmāt", "tābhyām", "tebhyaḥ" },
			{ "tasya", "tayoḥ", "teṣām" },
			{ "tasmin", "tayoḥ", "teṣu" },
			{ "", "", "" } };
		final NominalParadigm esahParad = NominalParadigm.generate("esaḥ", "etad", esahData);
		esahParad.setStemCutFactor(3);
		esahParad.addGender(Gender.MAS);
		esahParad.setWordType(WordType.PRONOUN);
		esahParad.addEndings(Case.ACC, Number.SING, "nam");
		esahParad.addEndings(Case.ACC, Number.DUAL, "nau");
		esahParad.addEndings(Case.ACC, Number.PLU, "nān");
		esahParad.addEndings(Case.INS, Number.SING, "nena");
		esahParad.addEndings(Case.GEN, Number.DUAL, "nayoḥ");
		esahParad.addEndings(Case.LOC, Number.DUAL, "nayoḥ");
		paradigmMap.put("esaḥ", esahParad);
		final String[][] etatData = {
			{ "tat", "te", "tāni" },
			{ "tat", "te", "tāni" },
			{ "tena", "tābhyām", "taiḥ" },
			{ "tasmai", "tābhyām", "tebhyaḥ" },
			{ "tasmāt", "tābhyām", "tebhyaḥ" },
			{ "tasya", "tayoḥ", "teṣām" },
			{ "tasmin", "tayoḥ", "teṣu" },
			{ "", "", "" } };
		final NominalParadigm etatParad = NominalParadigm.generate("etat", "etad", etatData);
		etatParad.setStemCutFactor(3);
		etatParad.addGender(Gender.NEU);
		etatParad.setWordType(WordType.PRONOUN);
		etatParad.addEndings(Case.ACC, Number.SING, "nat");
		etatParad.addEndings(Case.ACC, Number.DUAL, "ne");
		etatParad.addEndings(Case.ACC, Number.PLU, "nāni");
		etatParad.addEndings(Case.INS, Number.SING, "nena");
		etatParad.addEndings(Case.GEN, Number.DUAL, "nayoḥ");
		etatParad.addEndings(Case.LOC, Number.DUAL, "nayoḥ");
		paradigmMap.put("etat", etatParad);
		final String[][] esaData = {
			{ "sā", "te", "tāḥ" },
			{ "tām", "te", "tāḥ" },
			{ "tayā", "tābhyām", "tābhiḥ" },
			{ "tasyai", "tābhyām", "tābhyaḥ" },
			{ "tasyāḥ", "tābhyām", "tābhyaḥ" },
			{ "tasyāḥ", "tayoḥ", "tāsām" },
			{ "tasyām", "tayoḥ", "tāsu" },
			{ "", "", "" } };
		final NominalParadigm esaParad = NominalParadigm.generate("esā", "etad", esaData);
		esaParad.setStemCutFactor(3);
		esaParad.addGender(Gender.FEM);
		esaParad.setWordType(WordType.PRONOUN);
		esaParad.addEndings(Case.ACC, Number.SING, "nām");
		esaParad.addEndings(Case.ACC, Number.DUAL, "ne");
		esaParad.addEndings(Case.ACC, Number.PLU, "nāḥ");
		esaParad.addEndings(Case.INS, Number.SING, "nayā");
		esaParad.addEndings(Case.GEN, Number.DUAL, "nayoḥ");
		esaParad.addEndings(Case.LOC, Number.DUAL, "nayoḥ");
		paradigmMap.put("esā", esaParad);
		// pronouns
		// yad
		final List<String> yaLikeList = List.of("anya", "itara", "katama", "katara", "ya");
		final String[][] yahData = {
			{ "aḥ", "au", "e" },
			{ "am", "au", "ān" },
			{ "ena", "ābhyām", "aiḥ" },
			{ "asmai", "ābhyām", "ebhyaḥ" },
			{ "asmāt", "ābhyām", "ebhyaḥ" },
			{ "asya", "ayoḥ", "eṣām" },
			{ "asmin", "ayoḥ", "eṣu" },
			{ "", "", "" } };
		final NominalParadigm yahParad = NominalParadigm.generate("yaḥ", "ya", yahData);
		yahParad.addGender(Gender.MAS);
		yahParad.setWordType(WordType.PRONOUN);
		yahParad.addWordList(yaLikeList);
		paradigmMap.put("yaḥ", yahParad);
		final String[][] yatData = {
			{ "at", "e", "āni" },
			{ "at", "e", "āni" },
			{ "ena", "ābhyām", "aiḥ" },
			{ "asmai", "ābhyām", "ebhyaḥ" },
			{ "asmāt", "ābhyām", "ebhyaḥ" },
			{ "asya", "ayoḥ", "eṣām" },
			{ "asmin", "ayoḥ", "eṣu" },
			{ "", "", "" } };
		final NominalParadigm yatParad = NominalParadigm.generate("yat", "ya", yatData);
		yatParad.addGender(Gender.NEU);
		yatParad.setWordType(WordType.PRONOUN);
		yatParad.addWordList(yaLikeList);
		paradigmMap.put("yat", yatParad);
		final String[][] yaData = {
			{ "ā", "e", "āḥ" },
			{ "ām", "e", "āḥ" },
			{ "ayā", "ābhyām", "ābhiḥ" },
			{ "asyai", "ābhyām", "ābhyaḥ" },
			{ "asyāḥ", "ābhyām", "ābhyaḥ" },
			{ "asyāḥ", "ayoḥ", "āsām" },
			{ "asyām", "ayoḥ", "āsu" },
			{ "", "", "" } };
		final NominalParadigm yaParad = NominalParadigm.generate("yā", "ya", yaData);
		yaParad.addGender(Gender.FEM);
		yaParad.setWordType(WordType.PRONOUN);
		yaParad.addWordList(yaLikeList);
		paradigmMap.put("yā", yaParad);
		// tad
		final String[][] sahData = {
			{ "saḥ", "tau", "te" },
			{ "tam", "tau", "tān" },
			{ "tena", "tābhyām", "taiḥ" },
			{ "tasmai", "tābhyām", "tebhyaḥ" },
			{ "tasmāt", "tābhyām", "tebhyaḥ" },
			{ "tasya", "tayoḥ", "teṣām" },
			{ "tasmin", "tayoḥ", "teṣu" },
			{ "", "", "" } };
		final NominalParadigm sahParad = NominalParadigm.generate("saḥ", "tad", sahData);
		sahParad.setStemCutFactor(3);
		sahParad.addGender(Gender.MAS);
		sahParad.setWordType(WordType.PRONOUN);
		paradigmMap.put("saḥ", sahParad);
		final String[][] tatData = {
			{ "tat", "te", "tāni" },
			{ "tat", "te", "tāni" },
			{ "tena", "tābhyām", "taiḥ" },
			{ "tasmai", "tābhyām", "tebhyaḥ" },
			{ "tasmāt", "tābhyām", "tebhyaḥ" },
			{ "tasya", "tayoḥ", "teṣām" },
			{ "tasmin", "tayoḥ", "teṣu" },
			{ "", "", "" } };
		final NominalParadigm tatParad = NominalParadigm.generate("tat", "tad", tatData);
		tatParad.setStemCutFactor(3);
		tatParad.addGender(Gender.NEU);
		tatParad.setWordType(WordType.PRONOUN);
		paradigmMap.put("tat", tatParad);
		final String[][] saData = {
			{ "sā", "te", "tāḥ" },
			{ "tām", "te", "tāḥ" },
			{ "tayā", "tābhyām", "tābhiḥ" },
			{ "tasyai", "tābhyām", "tābhyaḥ" },
			{ "tasyāḥ", "tābhyām", "tābhyaḥ" },
			{ "tasyāḥ", "tayoḥ", "tāsām" },
			{ "tasyām", "tayoḥ", "tāsu" },
			{ "", "", "" } };
		final NominalParadigm saParad = NominalParadigm.generate("sā", "tad", saData);
		saParad.setStemCutFactor(3);
		saParad.addGender(Gender.FEM);
		saParad.setWordType(WordType.PRONOUN);
		paradigmMap.put("sā", saParad);
		// yuṣmad
		final String[][] tvamData = {
			{ "tvam", "yuvām", "yūyam" },
			{ "tvām", "yuvām", "yuṣmān" },
			{ "tvayā", "yuvābhyām", "yuṣmābhiḥ" },
			{ "tubhyām", "yuvābhyām", "yuṣmabhyam" },
			{ "tvat", "yuvābhyām", "yuṣmat" },
			{ "tava", "yuvayoḥ", "yuṣmākam" },
			{ "tvayi", "yuvayoḥ", "yuṣmāsu" },
			{ "", "", "" } };
		final NominalParadigm tvamParad = NominalParadigm.generate("tvam", "yuṣmad", tvamData);
		tvamParad.setStemCutFactor(6);
		tvamParad.addGender(Gender.MAS, Gender.FEM, Gender.NEU);
		tvamParad.setWordType(WordType.PRONOUN);
		tvamParad.addEndings(Case.ACC, Number.SING, "tvā");
		tvamParad.addEndings(Case.ACC, Number.DUAL, "vām");
		tvamParad.addEndings(Case.ACC, Number.PLU, "vaḥ");
		tvamParad.addEndings(Case.DAT, Number.SING, "te");
		tvamParad.addEndings(Case.DAT, Number.DUAL, "vām");
		tvamParad.addEndings(Case.DAT, Number.PLU, "vaḥ");
		tvamParad.addEndings(Case.GEN, Number.SING, "te");
		tvamParad.addEndings(Case.GEN, Number.DUAL, "vām");
		tvamParad.addEndings(Case.GEN, Number.PLU, "vaḥ");
		paradigmMap.put("tvam", tvamParad);
		final String[][] ahamData = {
			{ "aham", "āvām", "vayam" },
			{ "mām", "āvām", "asmān" },
			{ "mayā", "āvābhyām", "asmābhiḥ" },
			{ "mahyam", "āvābhyām", "asmabhyam" },
			{ "mat", "āvābhyām", "asmat" },
			{ "mama", "āvayoḥ", "asmākam" },
			{ "mayi", "āvayoḥ", "asmāsu" },
			{ "", "", "" } };
		// asmad
		final NominalParadigm ahamParad = NominalParadigm.generate("aham", "asmad", ahamData);
		ahamParad.setStemCutFactor(5);
		ahamParad.addGender(Gender.MAS, Gender.FEM, Gender.NEU);
		ahamParad.setWordType(WordType.PRONOUN);
		ahamParad.addEndings(Case.ACC, Number.SING, "mā");
		ahamParad.addEndings(Case.ACC, Number.DUAL, "nau");
		ahamParad.addEndings(Case.ACC, Number.PLU, "naḥ");
		ahamParad.addEndings(Case.DAT, Number.SING, "me");
		ahamParad.addEndings(Case.DAT, Number.DUAL, "nau");
		ahamParad.addEndings(Case.DAT, Number.PLU, "naḥ");
		ahamParad.addEndings(Case.GEN, Number.SING, "me");
		ahamParad.addEndings(Case.GEN, Number.DUAL, "nau");
		ahamParad.addEndings(Case.GEN, Number.PLU, "naḥ");
		paradigmMap.put("aham", ahamParad);
		// sarva
		final List<String> sarvaLikeList = Arrays.asList("adhara", "antara", "apara", "avara",
														"uttara", "ubhaya", "eka", "ekatara",
														"dakṣiṇa", "para", "pūrva", "viśva", "sva");
		final String[][] sarvahData = {
			{ "aḥ", "au", "e" },
			{ "am", "au", "ān" },
			{ "ena", "ābhyām", "aiḥ" },
			{ "asmai", "ābhyām", "ebhyaḥ" },
			{ "asmāt", "ābhyām", "ebhyaḥ" },
			{ "asya", "ayoḥ", "eṣām" },
			{ "asmin", "ayoḥ", "eṣu" },
			{ "a", "au", "e" } };
		final NominalParadigm sarvahParad = NominalParadigm.generate("sarvaḥ", "sarva", sarvahData);
		sarvahParad.addGender(Gender.MAS);
		sarvahParad.setWordType(WordType.PRONOUN);
		sarvahParad.addWordList(sarvaLikeList);
		paradigmMap.put("sarvaḥ", sarvahParad);
		final String[][] sarvamData = {
			{ "am", "e", "āni" },
			{ "am", "e", "āni" },
			{ "ena", "ābhyām", "aiḥ" },
			{ "asmai", "ābhyām", "ebhyaḥ" },
			{ "asmāt", "ābhyām", "ebhyaḥ" },
			{ "asya", "ayoḥ", "eṣām" },
			{ "asmin", "ayoḥ", "eṣu" },
			{ "a", "e", "āni" } };
		final NominalParadigm sarvamParad = NominalParadigm.generate("sarvam", "sarva", sarvamData);
		sarvamParad.addGender(Gender.NEU);
		sarvamParad.setWordType(WordType.PRONOUN);
		sarvamParad.addWordList(sarvaLikeList);
		paradigmMap.put("sarvam", sarvamParad);
		final String[][] sarvaData = {
			{ "ā", "e", "āḥ" },
			{ "ām", "e", "āḥ" },
			{ "ayā", "ābhyām", "ābhiḥ" },
			{ "asyai", "ābhyām", "ābhyaḥ" },
			{ "asyāḥ", "ābhyām", "ābhyaḥ" },
			{ "asyāḥ", "ayoḥ", "āsām" },
			{ "asyām", "ayoḥ", "āsu" },
			{ "e", "e", "āḥ" } };
		final NominalParadigm sarvaParad = NominalParadigm.generate("sarvā", "sarva", sarvaData);
		sarvaParad.addGender(Gender.FEM);
		sarvaParad.setWordType(WordType.PRONOUN);
		sarvaParad.addWordList(sarvaLikeList);
		paradigmMap.put("sarvā", sarvaParad);
		// kim
		final String[][] kahData = {
			{ "aḥ", "au", "e" },
			{ "am", "au", "ān" },
			{ "ena", "ābhyām", "aiḥ" },
			{ "asmai", "ābhyām", "ebhyaḥ" },
			{ "asmāt", "ābhyām", "ebhyaḥ" },
			{ "asya", "ayoḥ", "eṣām" },
			{ "asmin", "ayoḥ", "eṣu" },
			{ "", "", "" } };
		final NominalParadigm kahParad = NominalParadigm.generate("kaḥ", "kim", kahData);
		kahParad.setStemCutFactor(2);
		kahParad.addGender(Gender.MAS);
		kahParad.setWordType(WordType.PRONOUN);
		paradigmMap.put("kaḥ", kahParad);
		final String[][] kimData = {
			{ "im", "e", "āni" },
			{ "im", "e", "āni" },
			{ "ena", "ābhyām", "aiḥ" },
			{ "asmai", "ābhyām", "ebhyaḥ" },
			{ "asmāt", "ābhyām", "ebhyaḥ" },
			{ "asya", "ayoḥ", "eṣām" },
			{ "asmin", "ayoḥ", "eṣu" },
			{ "", "", "" } };
		final NominalParadigm kimParad = NominalParadigm.generate("kim", "kim", kimData);
		kimParad.setStemCutFactor(2);
		kimParad.addGender(Gender.NEU);
		kimParad.setWordType(WordType.PRONOUN);
		paradigmMap.put("kim", kimParad);
		final String[][] kaData = {
			{ "ā", "e", "āḥ" },
			{ "ām", "e", "āḥ" },
			{ "ayā", "ābhyām", "ābhiḥ" },
			{ "asyai", "ābhyām", "ābhyaḥ" },
			{ "asyāḥ", "ābhyām", "ābhyaḥ" },
			{ "asyāḥ", "ayoḥ", "āsām" },
			{ "asyām", "ayoḥ", "āsu" },
			{ "", "", "" } };
		final NominalParadigm kaParad = NominalParadigm.generate("kā", "kim", kaData);
		kaParad.setStemCutFactor(2);
		kaParad.addGender(Gender.FEM);
		kaParad.setWordType(WordType.PRONOUN);
		paradigmMap.put("kā", kaParad);
	}

}

