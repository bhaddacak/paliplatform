/*
 * PaliConjugation.java
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

package paliplatform.grammar;

import java.util.*;
import java.util.stream.*;
import java.nio.charset.StandardCharsets;

/** 
 * The class handling verbal conjugation.
 * @author J.R. Bhaddacak
 * @version 3.0
 * @since 1.0
 */
public class PaliConjugation {
	public static enum TenseMood {
		VAT, PAN, SAT, PAR, HIY, AJJ, BHA, KAL;
		public static final TenseMood[] values = values();
		private final String code;
		private static final String[] paliNames = { 
			"Vattamānā", "Pañcamī", "Sattamī", "Parokkhā", "Hiyyattanī", "Ajjattanī", "Bhavissantī", "Kālātipatti" };
		private static final String[] engNames = { 
			"present", "imperative", "optative", "past/preterit/perfect", "past/imperfect", "past/aorist", "future", "conditional" };
		private static final String[] engAbbrs = { 
			"Pres.", "Imp.", "Opt.", "Perf.", "Imperf.", "Aor.", "Fut.", "Cond." };
		private TenseMood() {
			code = this.toString().toLowerCase();
		}
		public String getCode() {
			return code;
		}
		public String getPaliName() {
			return paliNames[this.ordinal()];
		}
		public String getEngName() {
			return engNames[this.ordinal()];
		}
		public String getAbbr() {
			return engAbbrs[this.ordinal()];
		}
		public String getPaliAbbr() {
			return getPaliName().substring(0, 3) + ".";
		}
	}
	public static enum Pada {
		PARASSA, ATTANO;
		public static final Pada[] values = values();
		private final char code;
		private static final String[] names = { "Parassapada", "Attanopada" };
		private Pada() {
			code = this.toString().toLowerCase().charAt(0);
		}
		public char getCode() {
			return code;
		}
		public String getName() {
			return names[this.ordinal()];
		}
		public String getAbbr() {
			return getName().substring(0, 3) + ".";
		}
	}
	public static enum Person {
		PATHAMA, MAJJHIMA, UTTAMA;
		public static final Person[] values = values();
		private final String name;
		private final String[] abbrs = { "3rd", "2nd", "1st" };
		private Person() {
			final String n = this.toString();
			name = n.charAt(0) + n.substring(1).toLowerCase() + " (" + getAbbr() + ")";
		}
		public String getName() {
			return name;
		}
		public String getAbbr() {
			return abbrs[this.ordinal()];
		}
	}
	public static enum Number {
		SING("Singular"), PLU("Plural");
		public static final Number[] values = values();
		private final String name;
		private Number(final String n) {
			name = n;
		}
		public String getName() {
			return name;
		}
		public String getAbbr() {
			return name.substring(0, 3) + ".";
		}
	}
	
	public static enum Voice {
		ACTI("Active"), PASS("Passive"), CAUS("Causative"), CAUPAS("Causal Passive");
		public static final Voice[] values = values();
		private final String name;
		private Voice(final String n) {
			name = n;
		}
		public String getName() {
			return name;
		}
		@Override
		public String toString() {
			return name;
		}
	}
	public static enum DeriPaccaya {
		NTA("nta"), MANA("māna"), ANIYA("anīya"), TABBA("tabba"), TA("ta"), TVA("tvā");
		public static final DeriPaccaya[] values = values();
		private final String name;
		private DeriPaccaya(final String n) {
			name = n;
		}
		public String getName() {
			return name;
		}
		public String getStemPart() {
			final String result;
			if (this == TABBA)
				result = name.substring(1, name.length()-1);
			else if (this == TVA)
				result = name;
			else
				result = name.substring(0, name.length()-1);
			return result;
		}
		public String getPos() {
			final String result;
			switch (this) {
				case NTA:
				case MANA:
					result = "Present participle";
					break;
				case ANIYA:
				case TABBA:
					result = "Potential/Future passive participle";
					break;
				case TA:
					result = "Past participle";
					break;
				case TVA:
					result = "Absolutive";
					break;
				default:
					result = "";
			}
			return result;
		}
	}
	private static final Map<String, VerbParadigm> paradigmMap = new HashMap<>();
	
	public PaliConjugation() {
		loadVerbParadigm();	
	}
	
	private static void loadVerbParadigm() {
		if (!paradigmMap.isEmpty())
			return;
		try (final Scanner in = new Scanner(GrammarUtilities.class.getResourceAsStream(GrammarUtilities.PARADIGM_VERB_LIST), StandardCharsets.UTF_8)) {
			while (in.hasNextLine()) {
				final String line = in.nextLine().trim();
				if (line.charAt(0) == '#')
					continue;
				final String[] items = line.split(":");
				final String name = items[0];
				final String class_pada = name.split(";")[1];
				final TenseMood tense = Enum.valueOf(TenseMood.class, class_pada.substring(0,3).toUpperCase());
				final Pada pada = class_pada.charAt(3) == 'a' ? Pada.ATTANO : Pada.PARASSA;
				final VerbParadigm vp = new VerbParadigm(tense, pada);
				vp.setParadigm(items[1]);
				paradigmMap.put(name, vp);
			}
		}
	}
	
	public VerbParadigm getVerbParadigm(final String paradigmName, final TenseMood tense, final Pada pada) {
		final String key = paradigmName + ";" + tense.getCode() + pada.getCode();
		return paradigmMap.get(key);
	}
	
	public Set<Pada> getPadaSet(final String paradigmName, final TenseMood tense) {
		final Set<String> clspdSet = paradigmMap.keySet().stream()
										.filter(x -> x.startsWith(paradigmName+ ";" + tense.getCode()))
										.map(x -> x.split(";")[1])
										.collect(Collectors.toSet());
		final Set<Pada> result = new HashSet<>();
		if (!clspdSet.isEmpty()) {
			for (final String pd : clspdSet) {
				if (pd.charAt(3) == Pada.PARASSA.getCode())
					result.add(Pada.PARASSA);
				else if (pd.charAt(3) == Pada.ATTANO.getCode())
					result.add(Pada.ATTANO);
			}
		}
		return result;
	}

}
