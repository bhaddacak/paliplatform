/*
 * SktVerb.java
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

import paliplatform.base.*;
import static paliplatform.sanskrit.SktConjugation.TenseMood;
import static paliplatform.sanskrit.SktConjugation.Pada;
import static paliplatform.sanskrit.SktConjugation.Person;
import static paliplatform.sanskrit.SktConjugation.Number;
import static paliplatform.sanskrit.SktDeclension.Case;
import static paliplatform.sanskrit.NominalParadigm.Gender;

import java.util.*;
import java.util.stream.*;

/** 
 * Representation of a Sanskrit verb.
 * @author J.R. Bhaddacak
 * @version 4.2
 * @since 4.1
 */
public class SktVerb {
//~ 	private static final Map<Integer, String> romanNumMap = Map.of(
//~ 		1, "I", 2, "II", 3, "III", 4, "IV", 5, "V", 6, "VI", 7, "VII", 8, "VIII", 9, "IX", 10, "X");
	private final int numBucknell;
	private final String subRef;
	private final String rootName;
	private final String rootNameAlt;
	private final String prefix;
	private final List<Integer> rootClass;
	private final String meaning;
	private final String meaningAlt;
	private final boolean activeNormal;
	private final Map<Pada, String> citationForm;
	private final Map<Pada, String> alternativeForm;
	private final List<String> irregularMiddleForm;
	private final String presentPluralForm;
	private final List<String> presentPassiveForm;
	private final List<String> futureForm;
	private final String presentCausativeForm;
	private final Map<Pada, String> desiderativeForm;
	private final String intensiveForm;
	private final Map<Pada, List<String>> presentSubstitution;
	private final Map<Pada, String> perfectForm;
	private final Map<Pada, String> perfectParadigm;
	private final Map<Pada, List<String>> perfectSubstitution;
	private final Map<Pada, String> perfectPeriForm;
	private final Map<Pada, List<String>> aoristForm;
	private final String aoristPassiveForm;
	private final String aoristCausativeForm;
	private final List<String> infinitiveForm;
	private final List<String> absolutiveForm;
	private final List<String> prefixedAbsolutiveForm;
	private final List<String> pppForm;
	private final List<String> fppForm;
	private final String fppNiyaForm;
	private final List<String> fppYaForm;
	private final String periFutureForm;
	private final Map<Pada, String> precativeForm;
	private final boolean completed;

	// internal builder class
	public static class Builder {
		private final int numBucknell;
		private String subRef = "";
		private final String rootName;
		private String rootNameAlt = "";
		private String prefix = "";
		private final List<Integer> rootClass = new ArrayList<>(3);
		private final String meaning;
		private String meaningAlt = "";
		private boolean activeNormal = true; // otherwise normally used in middle form
		private final Map<Pada, String> citationForm = new EnumMap<>(Pada.class);
		private final Map<Pada, String> alternativeForm = new EnumMap<>(Pada.class);
		private final List<String> irregularMiddleForm = new ArrayList<>(2);
		private String presentPluralForm = "";
		private final List<String> presentPassiveForm = new ArrayList<>();
		private final List<String> futureForm = new ArrayList<>();
		private String presentCausativeForm = "";
		private final Map<Pada, String> desiderativeForm = new EnumMap<>(Pada.class);
		private String intensiveForm = "";
		private final Map<Pada, List<String>> presentSubstitution = new EnumMap<>(Pada.class);
		private final Map<Pada, String> perfectForm = new EnumMap<>(Pada.class);
		private final Map<Pada, String> perfectParadigm = new EnumMap<>(Pada.class);
		private final Map<Pada, List<String>> perfectSubstitution = new EnumMap<>(Pada.class);
		private final Map<Pada, String> perfectPeriForm = new EnumMap<>(Pada.class);
		private final Map<Pada, List<String>> aoristForm = new EnumMap<>(Pada.class);
		private String aoristPassiveForm = "";
		private String aoristCausativeForm = "";
		private final List<String> infinitiveForm = new ArrayList<>();
		private final List<String> absolutiveForm = new ArrayList<>();
		private final List<String> prefixedAbsolutiveForm = new ArrayList<>();
		private final List<String> pppForm = new ArrayList<>();
		private final List<String> fppForm = new ArrayList<>();
		private String fppNiyaForm = "";
		private final List<String> fppYaForm = new ArrayList<>();
		private String periFutureForm = "";
		private final Map<Pada, String> precativeForm = new EnumMap<>(Pada.class);
		private boolean completed = false;
		//
		public Builder(final int bucknum, final String rname, final int rclass, final String mean) {
			numBucknell = bucknum;
			rootName = rname;
			rootClass.add(rclass);
			meaning = mean;
		}
		public Builder subRef(final String name) {
			subRef = name;
			return this;
		}
		public Builder addRootClass(final Integer... num) {
			rootClass.addAll(Arrays.asList(num));
			return this;
		}
		public Builder altRootName(final String rname) {
			rootNameAlt = rname;
			return this;
		}
		public Builder prefix(final String pfix) {
			prefix = pfix;
			return this;
		}
		public Builder midNormal() {
			activeNormal = false;
			return this;
		}
		public Builder citActForm(final String word) {
			citationForm.put(Pada.ACT, word);
			return this;
		}
		public Builder citMidForm(final String word) {
			citationForm.put(Pada.MID, word);
			return this;
		}
		public Builder altMeaning(final String mean) {
			meaningAlt = mean;
			return this;
		}
		public Builder altActForm(final String word) {
			alternativeForm.put(Pada.ACT, word);
			return this;
		}
		public Builder altMidForm(final String word) {
			alternativeForm.put(Pada.MID, word);
			return this;
		}
		public Builder irrMidForm(final String... word) {
			irregularMiddleForm.addAll(Arrays.asList(word));
			return this;
		}
		public Builder presPluForm(final String word) {
			presentPluralForm = word;
			return this;
		}
		public Builder presPassForm(final String... word) {
			presentPassiveForm.addAll(Arrays.asList(word));
			return this;
		}
		public Builder futForm(final String... word) {
			futureForm.addAll(Arrays.asList(word));
			return this;
		}
		public Builder presCausForm(final String word) {
			presentCausativeForm = word;
			return this;
		}
		public Builder desForm(final Pada pad, final String word) {
			desiderativeForm.put(pad, word);
			return this;
		}
		public Builder intForm(final String word) {
			intensiveForm = word;
			return this;
		}
		public Builder presSub(final Pada pad, final String... part) {
			final List<String> plist = presentSubstitution.getOrDefault(pad, new ArrayList<>());
			plist.addAll(Arrays.asList(part));
			presentSubstitution.put(pad, plist);
			return this;
		}
		public Builder perfForm(final Pada pad, final String word) {
			perfectForm.put(pad, word);
			return this;
		}
		public Builder perfParad(final Pada pad, final String pname) {
			perfectParadigm.put(pad, pname);
			return this;
		}
		public Builder perfSub(final Pada pad, final String... part) {
			final List<String> plist = perfectSubstitution.getOrDefault(pad, new ArrayList<>());
			plist.addAll(Arrays.asList(part));
			perfectSubstitution.put(pad, plist);
			return this;
		}
		public Builder perfPeriForm(final Pada pad, final String word) {
			perfectPeriForm.put(pad, word);
			return this;
		}
		public Builder aorForm(final Pada pad, final String... word) {
			final List<String> tlist = aoristForm.getOrDefault(pad, new ArrayList<>());
			tlist.addAll(Arrays.asList(word));
			aoristForm.put(pad, tlist);
			return this;
		}
		public Builder aorPassForm(final String word) {
			aoristPassiveForm = word;
			return this;
		}
		public Builder aorCausForm(final String word) {
			aoristCausativeForm = word;
			return this;
		}
		public Builder infForm(final String... word) {
			infinitiveForm.addAll(Arrays.asList(word));
			return this;
		}
		public Builder absForm(final String... word) {
			absolutiveForm.addAll(Arrays.asList(word));
			return this;
		}
		public Builder prefAbsForm(final String... word) {
			prefixedAbsolutiveForm.addAll(Arrays.asList(word));
			return this;
		}
		public Builder pppForm(final String... word) {
			pppForm.addAll(Arrays.asList(word));
			return this;
		}
		public Builder fppForm(final String... word) {
			fppForm.addAll(Arrays.asList(word));
			return this;
		}
		public Builder fppNiyaForm(final String word) {
			fppNiyaForm = word;
			return this;
		}
		public Builder fppYaForm(final String... word) {
			fppYaForm.addAll(Arrays.asList(word));
			return this;
		}
		public Builder periFutForm(final String word) {
			periFutureForm = word;
			return this;
		}
		public Builder precForm(final Pada pad, final String word) {
			precativeForm.put(pad, word);
			return this;
		}
		public Builder completed() {
			completed = true;
			return this;
		}
		public SktVerb build() {
			return new SktVerb(this);
		}
	}

	// constructor
	private SktVerb(final Builder builder) {
		numBucknell = builder.numBucknell;
		subRef = builder.subRef;
		rootName = builder.rootName;
		rootNameAlt = builder.rootNameAlt;
		prefix = builder.prefix;
		rootClass = builder.rootClass;
		meaning = builder.meaning;
		meaningAlt = builder.meaningAlt;
		activeNormal = builder.activeNormal;
		citationForm = builder.citationForm;
		alternativeForm = builder.alternativeForm;
		irregularMiddleForm = builder.irregularMiddleForm;
		presentPluralForm = builder.presentPluralForm;
		presentPassiveForm = builder.presentPassiveForm;
		futureForm = builder.futureForm;
		presentCausativeForm = builder.presentCausativeForm;
		desiderativeForm = builder.desiderativeForm;
		intensiveForm = builder.intensiveForm;
		presentSubstitution = builder.presentSubstitution;
		perfectForm = builder.perfectForm;
		perfectParadigm = builder.perfectParadigm;
		perfectSubstitution = builder.perfectSubstitution;
		perfectPeriForm = builder.perfectPeriForm;
		aoristForm = builder.aoristForm;
		aoristPassiveForm = builder.aoristPassiveForm;
		aoristCausativeForm = builder.aoristCausativeForm;
		infinitiveForm = builder.infinitiveForm;
		absolutiveForm = builder.absolutiveForm;
		prefixedAbsolutiveForm = builder.prefixedAbsolutiveForm;
		pppForm = builder.pppForm;
		fppForm = builder.fppForm;
		fppNiyaForm = builder.fppNiyaForm;
		fppYaForm = builder.fppYaForm;
		periFutureForm = builder.periFutureForm;
		precativeForm = builder.precativeForm;
		completed = builder.completed;
	}

	public boolean isCompleted() {
		return completed;
	}

	public String getBucknellNumberStr() {
		return numBucknell + subRef;
	}

	public String getRootName() {
		return rootNameAlt.isEmpty()
				? rootName
				: rootName + "/" + rootNameAlt;
	}

	public String getRootClassStr() {
		return rootClass.stream()
						.map(x -> "" + x)
						.collect(Collectors.joining("/"));
	}

	public String getMeaning() {
		return meaning;
	}

	public String getAltMeaning() {
		return meaningAlt;
	}

	public boolean isActiveNormal() {
		return activeNormal;
	}

	public String getCitationForm() {
		final String result = activeNormal
								? citationForm.getOrDefault(Pada.ACT, citationForm.get(Pada.MID))
								: citationForm.getOrDefault(Pada.MID, citationForm.get(Pada.ACT));
		return result == null ? "" : result;
	}

	public String getVerbAndRoot() {
		final String prefixStr = prefix.isEmpty() ? "" : prefix + " + ";
		return getCitationForm() + " (" + prefixStr + "√" + getRootName() + ")"; 
	}

	public String getAlternativeForm(final Pada pad) {
		return alternativeForm.getOrDefault(pad, "");
	}

	public List<String> getIrregularMiddleForm() {
		return irregularMiddleForm;
	}

	public List<String> getPresentPassiveForm() {
		return presentPassiveForm;
	}

	public String getDesiderativeForm() {
		return desiderativeForm.getOrDefault(Pada.ACT, desiderativeForm.getOrDefault(Pada.MID, ""));
	}

	public String getPerfectForm() {
		return perfectForm.getOrDefault(Pada.ACT, perfectForm.getOrDefault(Pada.MID, ""));
	}

	public List<String> getAoristForm() {
		return aoristForm.getOrDefault(Pada.ACT, aoristForm.getOrDefault(Pada.MID, Collections.emptyList()));
	}

	public List<String> getFutureForm() {
		return futureForm;
	}

	public List<String> getPppForm() {
		return pppForm;
	}

	public List<String> getFppForm() {
		return fppForm;
	}

	public String getDescription() {
		final String prefixStr = prefix.isEmpty() ? "" : prefix + " + ";
		final String desc = prefixStr + "√" + getRootName() + " " + getRootClassStr() + " " +
							meaning +" [" + numBucknell + subRef + "]";
		return desc;
	}

	public String getFullDescription() {
		final String prefixStr = prefix.isEmpty() ? "" : prefix + " + ";
		final String desc = getCitationForm() + " < " + prefixStr + "√" + getRootName() + " " + getRootClassStr() + " " +
							meaning +" [" + numBucknell + subRef + "]";
		return desc;
	}

	private String getParadigmName(final TenseMood tense, final Pada pad) {
		final String citeForm = getCitationForm();
		if (citeForm.endsWith("e") && pad == Pada.ACT) return "";
		return citeForm.isEmpty() ? "" : SktConjugation.getParadigmName(citeForm, tense, pad);
	}

	public Map<Person, Map<Number, List<String>>> getCommonProduct(final TenseMood tense, final Pada pad) {
		if (pad == Pada.MID && !citationForm.containsKey(Pada.MID))
			return Collections.emptyMap();
		final String paradName = getParadigmName(tense, pad);
		if (paradName.isEmpty())
			return Collections.emptyMap();
		final List<String> subList = presentSubstitution.get(pad);
		return subList == null
				? SktConjugation.compute(getCitationForm(), paradName)
				: SktConjugation.compute(getCitationForm(), paradName, subList);
	}

	public Map<Person, Map<Number, List<String>>> getIrrMidCommonProduct(final TenseMood tense, final int index) {
		final String midForm = irregularMiddleForm.get(index);
		if (midForm == null)
			return Collections.emptyMap();
		final String paradName = SktConjugation.getParadigmName(midForm, tense, Pada.MID);
		if (paradName.isEmpty())
			return Collections.emptyMap();
		final List<String> subList = presentSubstitution.get(Pada.MID);
		return subList == null
				? SktConjugation.compute(midForm, paradName)
				: SktConjugation.compute(midForm, paradName, subList);
	}

	public Map<Person, Map<Number, List<String>>> getCommonPassiveProduct(final TenseMood tense, final int index) {
		final String passForm = presentPassiveForm.get(index);
		if (passForm == null)
			return Collections.emptyMap();
		final String paradName = SktConjugation.getParadigmName(passForm, tense, Pada.MID);
		return paradName.isEmpty()
				? Collections.emptyMap()
				: SktConjugation.compute(passForm, paradName);
	}

	public Map<Person, Map<Number, List<String>>> getCommonCausativeProduct(final TenseMood tense) {
		final String paradName = presentCausativeForm.isEmpty()
									? ""
									: SktConjugation.getParadigmName(presentCausativeForm, tense, Pada.ACT);
		return paradName.isEmpty()
				? Collections.emptyMap()
				: SktConjugation.compute(presentCausativeForm, paradName);
	}

	public Map<Person, Map<Number, List<String>>> getCausativePassiveProduct() {
		if (presentCausativeForm.isEmpty())
			return Collections.emptyMap();
		final String causPassForm = presentCausativeForm.substring(0, presentCausativeForm.length()-5) + "yate";
		final String paradName = SktConjugation.getParadigmName(causPassForm, TenseMood.PRES, Pada.MID);
		if (paradName.isEmpty())
			return Collections.emptyMap();
		return SktConjugation.compute(causPassForm, paradName);
	}

	public Map<Person, Map<Number, List<String>>> getDesiderativeProduct(final Pada pad) {
		final String desForm = desiderativeForm.get(pad);
		if (desForm == null)
			return Collections.emptyMap();
		final String paradName = SktConjugation.getParadigmName(desForm, TenseMood.PRES, pad);
		if (paradName.isEmpty())
			return Collections.emptyMap();
		return SktConjugation.compute(desForm, paradName);
	}

	public Map<Person, Map<Number, List<String>>> getDesiderativePassiveProduct() {
		final String desForm = desiderativeForm.get(Pada.ACT);
		if (desForm == null)
			return Collections.emptyMap();
		final String desPassForm = desForm.substring(0, desForm.length()-3) + "yate";
		final String paradName = SktConjugation.getParadigmName(desPassForm, TenseMood.PRES, Pada.MID);
		if (paradName.isEmpty())
			return Collections.emptyMap();
		return SktConjugation.compute(desPassForm, paradName);
	}

	public Map<Person, Map<Number, List<String>>> getIntensiveProduct() {
		if (intensiveForm.isEmpty())
			return Collections.emptyMap();
		final String paradName = SktConjugation.getParadigmName(intensiveForm, TenseMood.PRES, Pada.MID);
		if (paradName.isEmpty())
			return Collections.emptyMap();
		return SktConjugation.compute(intensiveForm, paradName);
	}

	public Map<Person, Map<Number, List<String>>> getPerfectProduct(final Pada pad) {
		final String paradName = perfectParadigm.get(pad);
		if (paradName == null)
			return Collections.emptyMap();
		final List<String> subList = perfectSubstitution.get(pad);
		return subList == null
			? SktConjugation.compute(getPerfectForm(), paradName)
			: SktConjugation.computePerfect(getPerfectForm(), paradName, subList); 
	}

	public Map<Person, Map<Number, List<String>>> getPeriPerfectProduct(final Pada pad) {
		final String periPerfForm = perfectPeriForm.get(pad);
		if (periPerfForm == null)
			return Collections.emptyMap();
		final String paradName = pad == Pada.ACT ? "āsa" : "cakre";
		final String vsample = pad == Pada.ACT ? "āsa" : "cakāra";
		return SktConjugation.compute(vsample, paradName, periPerfForm.split(" ")[0]); 
	}

	public Map<Person, Map<Number, List<String>>> getPerfectCausativeProduct() {
		if (presentCausativeForm.isEmpty())
			return Collections.emptyMap();
		final String perfCausForm = presentCausativeForm.substring(0, presentCausativeForm.length()-5) + "ayām";
		return SktConjugation.compute("āsa", "āsa", perfCausForm);
	}

	public Map<Person, Map<Number, List<String>>> getPerfectDesiderativeProduct(final Pada pad) {
		final String desForm = desiderativeForm.get(pad);
		if (desForm == null)
			return Collections.emptyMap();
		final String desPerfForm = desForm.substring(0, desForm.length()-3) + "ām";
		final String paradName = pad == Pada.ACT ? "āsa" : "cakre";
		final String vsample = pad == Pada.ACT ? "āsa" : "cakāra";
		return SktConjugation.compute(vsample, paradName, desPerfForm); 
	}

	public Map<Person, Map<Number, List<String>>> getAoristProduct(final Pada pad, final int index) {
		if (pad == Pada.ACT && !citationForm.containsKey(Pada.ACT))
			return Collections.emptyMap();
		final List<String> aorTerms = getAoristForm(); // middle stem is derived from active
		if (aorTerms.isEmpty())
			return Collections.emptyMap();
		final String paradName = SktConjugation.getParadigmName(aorTerms.get(index), TenseMood.AOR, pad);
		if (paradName.isEmpty())
			return Collections.emptyMap();
		return SktConjugation.compute(aorTerms.get(index), paradName);
	}

	public Map<Person, Map<Number, List<String>>> getAoristPassiveProduct() {
		if (aoristPassiveForm.isEmpty())
			return Collections.emptyMap();
		final String paradName = "apāci";
		if (paradName.isEmpty())
			return Collections.emptyMap();
		return SktConjugation.compute(aoristPassiveForm, paradName);
	}

	public Map<Person, Map<Number, List<String>>> getAoristCausativeProduct() {
		if (aoristCausativeForm.isEmpty())
			return Collections.emptyMap();
		final String paradName = SktConjugation.getParadigmName(aoristCausativeForm, TenseMood.AOR, Pada.ACT);
		if (paradName.isEmpty())
			return Collections.emptyMap();
		return SktConjugation.compute(aoristCausativeForm, paradName);
	}

	public Map<Person, Map<Number, List<String>>> getAoristDesiderativeProduct(final Pada pad) {
		final String desForm = desiderativeForm.get(pad);
		if (desForm == null)
			return Collections.emptyMap();
		final String desFutForm = desForm.substring(0, desForm.length()-3) + "īt";
		final String paradName = pad == Pada.ACT ? "aninīṣīt" : "aninīṣiṣṭa";
		if (paradName.isEmpty())
			return Collections.emptyMap();
		return SktConjugation.compute(desFutForm, paradName);
	}

	public Map<Person, Map<Number, List<String>>> getPrecativeProduct(final Pada pad) {
		if (pad == Pada.ACT && !citationForm.containsKey(Pada.ACT))
			return Collections.emptyMap();
		final String subForm = pad == Pada.ACT
							? !presentPassiveForm.isEmpty() ? presentPassiveForm.get(0) : ""
							: !futureForm.isEmpty() ? futureForm.get(0) : "";
		final String precForm = precativeForm.containsKey(pad)
									? precativeForm.get(pad)
									: !subForm.isEmpty()
										? pad == Pada.ACT
											? subForm.substring(0, subForm.length()-3) + "āt"
											: subForm.substring(0, subForm.length()-4) + "īṣṭa"
										: "";
		if (precForm.isEmpty())
			return Collections.emptyMap();
		final String paradName = SktConjugation.getParadigmName(precForm, TenseMood.PREC, pad);
		if (paradName.isEmpty())
			return Collections.emptyMap();
		return SktConjugation.compute(precForm, paradName);
	}

	public Map<Person, Map<Number, List<String>>> getPrecativePassiveProduct() {
		if (aoristPassiveForm.isEmpty())
			return Collections.emptyMap();
		final String precPassForm = VerbalParadigm.deaugment(aoristPassiveForm) + "ṣīṣṭa";
		final String paradName = SktConjugation.getParadigmName(precPassForm, TenseMood.PREC, Pada.MID);
		if (paradName.isEmpty())
			return Collections.emptyMap();
		return SktConjugation.compute(precPassForm, paradName);
	}

	public Map<Person, Map<Number, List<String>>> getPeriFutureProduct(final Pada pad) {
		final String periFutForm = !periFutureForm.isEmpty()
										? periFutureForm
										: !infinitiveForm.isEmpty()
											? infinitiveForm.get(0).substring(0, infinitiveForm.get(0).length()-2) + "ā"
											: "";
		if (periFutForm.isEmpty())
			return Collections.emptyMap();
		final String paradName = SktConjugation.getParadigmName(periFutForm, TenseMood.PERI, pad);
		if (paradName.isEmpty())
			return Collections.emptyMap();
		return SktConjugation.compute(periFutForm, paradName);
	}

	public Map<Person, Map<Number, List<String>>> getPeriFuturePassiveProduct() {
		if (aoristPassiveForm.isEmpty())
			return Collections.emptyMap();
		final String periFutPassForm = VerbalParadigm.deaugment(aoristPassiveForm) + "tā";
		final String paradName = SktConjugation.getParadigmName(periFutPassForm, TenseMood.PERI, Pada.MID);
		if (paradName.isEmpty())
			return Collections.emptyMap();
		return SktConjugation.compute(periFutPassForm, paradName);
	}

	private Map<Person, Map<Number, List<String>>> getFutureConditionalProduct(final TenseMood tense, final Pada pad, final String term) {
		final TenseMood renderedTenseMood = tense == TenseMood.FUT ? TenseMood.PRES : TenseMood.IMPERF;
		final String paradName = SktConjugation.getParadigmName(term, renderedTenseMood, pad);
		if (paradName.isEmpty())
			return Collections.emptyMap();
		Map<Person, Map<Number, List<String>>> prod = SktConjugation.compute(term, paradName);
		return prod;
	}

	public Map<Person, Map<Number, List<String>>> getFutureProduct(final Pada pad, final int index) {
		if (futureForm.isEmpty() || index >= futureForm.size())
			return Collections.emptyMap();
		if (pad == Pada.ACT && !citationForm.containsKey(Pada.ACT))
			return Collections.emptyMap();
		return getFutureConditionalProduct(TenseMood.FUT, pad, futureForm.get(index));
	}

	public Map<Person, Map<Number, List<String>>> getFuturePassiveProduct() {
		if (aoristPassiveForm.isEmpty())
			return Collections.emptyMap();
		final String futPassForm = VerbalParadigm.deaugment(aoristPassiveForm) + "ṣyati";
		final String paradName = SktConjugation.getParadigmName(futPassForm, TenseMood.PRES, Pada.MID);
		if (paradName.isEmpty())
			return Collections.emptyMap();
		return SktConjugation.compute(futPassForm, paradName);
	}

	public Map<Person, Map<Number, List<String>>> getFutureCausativeProduct() {
		if (presentCausativeForm.isEmpty())
			return Collections.emptyMap();
		final String futCausForm = presentCausativeForm.substring(0, presentCausativeForm.length()-5) + "ayiṣyati";
		final String paradName = SktConjugation.getParadigmName(futCausForm, TenseMood.PRES, Pada.ACT);
		if (paradName.isEmpty())
			return Collections.emptyMap();
		return SktConjugation.compute(futCausForm, paradName);
	}

	public Map<Person, Map<Number, List<String>>> getFutureDesiderativeProduct(final Pada pad) {
		final String desForm = desiderativeForm.get(pad);
		if (desForm == null)
			return Collections.emptyMap();
		final String desFutForm = desForm.substring(0, desForm.length()-3) + "iṣyati";
		final String paradName = SktConjugation.getParadigmName(desFutForm, TenseMood.PRES, pad);
		if (paradName.isEmpty())
			return Collections.emptyMap();
		return SktConjugation.compute(desFutForm, paradName);
	}

	public Map<Person, Map<Number, List<String>>> getConditionalProduct(final Pada pad, final int index) {
		if (futureForm.isEmpty() || index >= futureForm.size())
			return Collections.emptyMap();
		if (pad == Pada.ACT && !citationForm.containsKey(Pada.ACT))
			return Collections.emptyMap();
		return getFutureConditionalProduct(TenseMood.COND, pad, futureForm.get(index));
	}

	public Map<Case, Map<SktDeclension.Number, List<String>>> getPresentActiveParticipleProduct(final Gender gend) {
		final String paradName = getParadigmName(TenseMood.PRES, Pada.ACT);
		if (paradName.isEmpty())
			return Collections.emptyMap();
		final Map<Person, Map<Number, List<String>>> presProd = SktConjugation.compute(getCitationForm(), paradName);
		if (presProd.isEmpty())
			return Collections.emptyMap();
		final String prathamaSing = presProd.get(Person.PRATHAMA).get(Number.SING).get(0);
		final String prathamaPlu = presProd.get(Person.PRATHAMA).get(Number.PLU).get(0);
		final String partStem = prathamaPlu.substring(0, prathamaPlu.length()-1);
		final Map<Case, Map<SktDeclension.Number, List<String>>> result;
		final NominalParadigm parad;
		if (gend == Gender.MAS) {
			parad = partStem.endsWith("nt")
					? SktDeclension.paradigmMap.get("nayan")
					: SktDeclension.paradigmMap.get("marut");
			result = SktDeclension.compute(new SktNominal(partStem, parad));
		} else if(gend == Gender.NEU) {
			parad = partStem.endsWith("nt")
					? SktDeclension.paradigmMap.get("nayat")
					: SktDeclension.paradigmMap.get("jagat");
			result = SktDeclension.compute(new SktNominal(partStem, parad));
		} else {
			// feminine
			final String stemAnti = partStem + "ī";
			final String stemAti = partStem.substring(0, partStem.length()-2) + "tī";
			final String[] partStemF = partStem.endsWith("nt")
										? rootClass.contains(6) || prathamaSing.endsWith("āti")
											? new String[] { stemAnti, stemAti }
											: prathamaSing.endsWith("ati")
												? new String[] { stemAnti }
												: new String[] { stemAti }
										: new String[] { partStem + "ī" };
			parad = SktDeclension.paradigmMap.get("nadī");
			if (partStemF.length == 1) {
				result = SktDeclension.compute(new SktNominal(partStemF[0], parad));
			} else {
				final SktNominal[] words = { new SktNominal(partStemF[0], parad), new SktNominal(partStemF[1], parad) };
				result = SktDeclension.productCombine(SktDeclension.compute(words[0]), SktDeclension.compute(words[1]));
			}
		}
		return result;
	}

	public Map<Case, Map<SktDeclension.Number, List<String>>> getPresentMiddleParticipleProduct(final Gender gend) {
		final String paradName = getParadigmName(TenseMood.PRES, Pada.MID);
		if (paradName.isEmpty())
			return Collections.emptyMap();
		final Map<Person, Map<Number, List<String>>> presProd = SktConjugation.compute(getCitationForm(), paradName);
		if (presProd.isEmpty())
			return Collections.emptyMap();
		final String prathamaPlu = presProd.get(Person.PRATHAMA).get(Number.PLU).get(0);
		final String partStem = prathamaPlu.endsWith("ante")
								? Sandhi.applyInternalSandhiN(prathamaPlu.substring(0, prathamaPlu.length()-4), "amāna")
								: Sandhi.applyInternalSandhiN(prathamaPlu.substring(0, prathamaPlu.length()-3), "āna");
		final NominalParadigm parad = SktDeclension.paradigmMap.get(NominalParadigm.genericFallback[gend.ordinal()]);
		return SktDeclension.compute(new SktNominal(partStem, parad));
	}

	public Map<Case, Map<SktDeclension.Number, List<String>>> getPresentPassiveParticipleProduct(final Gender gend, final int index) {
		final String passForm = presentPassiveForm.get(index);
		if (passForm == null)
			return Collections.emptyMap();
		final String partStem = Sandhi.applyInternalSandhiN(passForm.substring(0, passForm.length()-3), "amāna");
		final NominalParadigm parad = SktDeclension.paradigmMap.get(NominalParadigm.genericFallback[gend.ordinal()]);
		return SktDeclension.compute(new SktNominal(partStem, parad));
	}

	public Map<Case, Map<SktDeclension.Number, List<String>>> getPerfectActiveParticipleProduct(final Gender gend) {
		final Map<Person, Map<Number, List<String>>> perfProd = getPerfectProduct(Pada.ACT);
		if (perfProd.isEmpty())
			return Collections.emptyMap();
		final String perfStem = perfProd.get(Person.UTTAMA).get(Number.DUAL).get(0);
		final String perfPartStem = perfStem.endsWith("iyiva")
									? perfStem + "s"
									: perfStem.endsWith("yiva")
										? perfStem.substring(0, perfStem.length()-4) + "īvas"
										: perfStem + "s";
		final SktNominal.Adjective adj = new SktNominal.Adjective(perfPartStem);
		final String[] terms = adj.getTerms();
		final String[] paradNames = adj.getParadigmNames();
		final NominalParadigm parad = SktDeclension.paradigmMap.get(paradNames[gend.ordinal()]);
		return SktDeclension.compute(new SktNominal(terms[gend.ordinal()], parad));
	}

	public Map<Case, Map<SktDeclension.Number, List<String>>> getPerfectActiveParticipleFromTaProduct(final Gender gend, final int index) {
		final String vform = pppForm.get(index);
		if (vform == null)
			return Collections.emptyMap();
		final String perfPartStem = vform + "vat";
		final SktNominal.Adjective adj = new SktNominal.Adjective(perfPartStem);
		final String[] terms = adj.getTerms();
		final String[] paradNames = adj.getParadigmNames();
		final NominalParadigm parad = SktDeclension.paradigmMap.get(paradNames[gend.ordinal()]);
		return SktDeclension.compute(new SktNominal(terms[gend.ordinal()], parad));
	}

	public Map<Case, Map<SktDeclension.Number, List<String>>> getPerfectMiddleParticipleProduct(final Gender gend) {
		final Map<Person, Map<Number, List<String>>> perfProd = getPerfectProduct(Pada.MID);
		if (perfProd.isEmpty())
			return Collections.emptyMap();
		final String perfStem = perfProd.get(Person.PRATHAMA).get(Number.SING).get(0);
		final String perfPartStem = Sandhi.applyInternalSandhiN(perfStem.substring(0, perfStem.length()-1), "āna");
		final NominalParadigm parad = SktDeclension.paradigmMap.get(NominalParadigm.genericFallback[gend.ordinal()]);
		return SktDeclension.compute(new SktNominal(perfPartStem, parad));
	}

	public Map<Case, Map<SktDeclension.Number, List<String>>> getPerfectPassiveParticipleProduct(final Gender gend, final int index) {
		final String vform = pppForm.get(index);
		if (vform == null)
			return Collections.emptyMap();
		final NominalParadigm parad = SktDeclension.paradigmMap.get(NominalParadigm.genericFallback[gend.ordinal()]);
		return SktDeclension.compute(new SktNominal(vform, parad));
	}

	public Map<Case, Map<SktDeclension.Number, List<String>>> getPerfectPassiveParticipleCausativeProduct(final Gender gend) {
		if (presentCausativeForm.isEmpty())
			return Collections.emptyMap();
		final String pppCausForm = presentCausativeForm.substring(0, presentCausativeForm.length()-5) + "ita";
		final NominalParadigm parad = SktDeclension.paradigmMap.get(NominalParadigm.genericFallback[gend.ordinal()]);
		return SktDeclension.compute(new SktNominal(pppCausForm, parad));
	}

	public Map<Case, Map<SktDeclension.Number, List<String>>> getPerfectPassiveParticipleDesiderativeProduct(final Gender gend) {
		final String desForm = getDesiderativeForm();
		if (desForm.isEmpty())
			return Collections.emptyMap();
		final String pppDesForm = desForm.substring(0, presentCausativeForm.length()-3) + "ita";
		final NominalParadigm parad = SktDeclension.paradigmMap.get(NominalParadigm.genericFallback[gend.ordinal()]);
		return SktDeclension.compute(new SktNominal(pppDesForm, parad));
	}

	public Map<Case, Map<SktDeclension.Number, List<String>>> getFutureActiveParticipleProduct(final Gender gend, final int index) {
		if (futureForm.isEmpty() || index >= futureForm.size())
			return Collections.emptyMap();
		final String partStem = futureForm.get(index).substring(0, futureForm.get(index).length()-3) + "ant";
		final String partStemFinal = gend == Gender.FEM
										? partStem + "ī"
										: partStem;
		final String[] paradNames = { "nayan", "nadī", "nayat" };
		final NominalParadigm parad = SktDeclension.paradigmMap.get(paradNames[gend.ordinal()]);
		return SktDeclension.compute(new SktNominal(partStemFinal, parad));
	}

	public Map<Case, Map<SktDeclension.Number, List<String>>> getFutureMiddleParticipleProduct(final Gender gend, final int index) {
		if (futureForm.isEmpty() || index >= futureForm.size())
			return Collections.emptyMap();
		final String partStem = Sandhi.applyInternalSandhiN(futureForm.get(index).substring(0, futureForm.get(index).length()-3), "amāna");
		final NominalParadigm parad = SktDeclension.paradigmMap.get(NominalParadigm.genericFallback[gend.ordinal()]);
		return SktDeclension.compute(new SktNominal(partStem, parad));
	}

	public Map<Case, Map<SktDeclension.Number, List<String>>> getFuturePassiveParticipleProduct(final Gender gend, final int index) {
		if (fppForm.isEmpty() || index >= fppForm.size())
			return Collections.emptyMap();
		final NominalParadigm parad = SktDeclension.paradigmMap.get(NominalParadigm.genericFallback[gend.ordinal()]);
		return SktDeclension.compute(new SktNominal(fppForm.get(index), parad));
	}

	public Map<Case, Map<SktDeclension.Number, List<String>>> getFuturePassiveParticipleCausativeProduct(final Gender gend) {
		if (presentCausativeForm.isEmpty())
			return Collections.emptyMap();
		final String fppCauForm = presentCausativeForm.substring(0, presentCausativeForm.length()-4) + "vya";
		final NominalParadigm parad = SktDeclension.paradigmMap.get(NominalParadigm.genericFallback[gend.ordinal()]);
		return SktDeclension.compute(new SktNominal(fppCauForm, parad));
	}

	public Map<Case, Map<SktDeclension.Number, List<String>>> getFuturePassiveParticipleNiyaProduct(final Gender gend) {
		if (fppNiyaForm.isEmpty())
			return Collections.emptyMap();
		final NominalParadigm parad = SktDeclension.paradigmMap.get(NominalParadigm.genericFallback[gend.ordinal()]);
		return SktDeclension.compute(new SktNominal(fppNiyaForm, parad));
	}

	public Map<Case, Map<SktDeclension.Number, List<String>>> getFuturePassiveParticipleNiyaCausativeProduct(final Gender gend) {
		if (presentCausativeForm.isEmpty())
			return Collections.emptyMap();
		final String fppCauForm = presentCausativeForm.substring(0, presentCausativeForm.length()-4) + "nīya";
		final NominalParadigm parad = SktDeclension.paradigmMap.get(NominalParadigm.genericFallback[gend.ordinal()]);
		return SktDeclension.compute(new SktNominal(fppCauForm, parad));
	}

	public Map<Case, Map<SktDeclension.Number, List<String>>> getFuturePassiveParticipleYaProduct(final Gender gend) {
		if (fppYaForm.isEmpty())
			return Collections.emptyMap();
		final NominalParadigm parad = SktDeclension.paradigmMap.get(NominalParadigm.genericFallback[gend.ordinal()]);
		Map<Case, Map<SktDeclension.Number, List<String>>> result = SktDeclension.compute(new SktNominal(fppYaForm.get(0), parad));
		for (int i = 1; i < fppYaForm.size(); i++) {
			result = SktDeclension.productCombine(result, SktDeclension.compute(new SktNominal(fppYaForm.get(i), parad)));
		}
		return result;
	}

	public Map<Case, Map<SktDeclension.Number, List<String>>> getFuturePassiveParticipleYaCausativeProduct(final Gender gend) {
		if (presentCausativeForm.isEmpty())
			return Collections.emptyMap();
		final String fppCauForm = presentCausativeForm.substring(0, presentCausativeForm.length()-5) + "ya";
		final NominalParadigm parad = SktDeclension.paradigmMap.get(NominalParadigm.genericFallback[gend.ordinal()]);
		return SktDeclension.compute(new SktNominal(fppCauForm, parad));
	}

	public List<StringPair> getInfinitiveProduct() {
		if (infinitiveForm.isEmpty())
			return Collections.emptyList();
		final List<StringPair> result = new ArrayList<>();
		final StringPair infProd = new StringPair("Infinitive", infinitiveForm.stream().collect(Collectors.joining(", ")));
		result.add(infProd);
		if (!presentCausativeForm.isEmpty()) {
			final String infCauForm = presentCausativeForm.substring(0, presentCausativeForm.length()-3) + "itum";
			final StringPair infCauProd = new StringPair("Causative Infinitive", infCauForm);
			result.add(infCauProd);
		}
		if (desiderativeForm.containsKey(Pada.ACT)) {
			final String desForm = desiderativeForm.get(Pada.ACT);
			final String infDesForm = desForm.substring(0, desForm.length()-3) + "itum";
			final StringPair infDesProd = new StringPair("Desiderative Infinitive", infDesForm);
			result.add(infDesProd);
		}
		if (!intensiveForm.isEmpty()) {
			final String infIntForm = intensiveForm.substring(0, intensiveForm.length()-3) + "itum";
			final StringPair infIntProd = new StringPair("Intensive Infinitive", infIntForm);
			result.add(infIntProd);
		}
		return result;
	}

	public List<StringPair> getAbsolutiveProduct() {
		if (absolutiveForm.isEmpty())
			return Collections.emptyList();
		final List<StringPair> result = new ArrayList<>();
		final StringPair absProd = new StringPair("Absolutive", absolutiveForm.stream().collect(Collectors.joining(", ")));
		result.add(absProd);
		if (!presentCausativeForm.isEmpty()) {
			final String absCauForm = presentCausativeForm.substring(0, presentCausativeForm.length()-3) + "itvā";
			final StringPair absCauProd = new StringPair("Causative Absolutive", absCauForm);
			result.add(absCauProd);
		}
		final String prefAbs = !prefixedAbsolutiveForm.isEmpty()
								? prefixedAbsolutiveForm.stream().map(x -> "-" + x).collect(Collectors.joining(", "))
								: !presentPassiveForm.isEmpty()
									? presentPassiveForm.stream().map(x -> "-" + x.substring(0, x.length()-2)).collect(Collectors.joining(", "))
									: "";
		if (!prefAbs.isEmpty()) {
			final StringPair prefAbsProd = new StringPair("Prefixed Absolutive", prefAbs);
			result.add(prefAbsProd);
		}
		return result;
	}

	@Override
	public String toString() {
		return getDescription();
	}

}

