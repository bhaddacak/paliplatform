/*
 * VerbParadigm.java
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

/** 
 * The class handling verbal paradigms.
 * @author J.R. Bhaddacak
 * @version 3.0
 * @since 1.0
 */
public class VerbParadigm {
	private final PaliConjugation.TenseMood tense;
	private final PaliConjugation.Pada pada;
	private final Map<PaliConjugation.Person, Map<PaliConjugation.Number, List<String>>> paradigm;

	public VerbParadigm(final PaliConjugation.TenseMood tense, final PaliConjugation.Pada pada) {
		this.tense = tense;
		this.pada = pada;
		paradigm = new EnumMap<>(PaliConjugation.Person.class);
	}
	
	public void setParadigm(final String str) {
		final String[] persons = str.split("\\|");
		int ind = 0;
		for (final PaliConjugation.Person p : PaliConjugation.Person.values) {
			final Map<PaliConjugation.Number, List<String>> vacana = new EnumMap<>(PaliConjugation.Number.class);
			final String[] nums = persons[ind].split(";");
			int k = 0;
			for (final PaliConjugation.Number n : PaliConjugation.Number.values) {
				if (nums.length == 0) {
					vacana.put(n, new ArrayList<String>());
				} else {
					final String[] suffix = nums[k].split(",");
					vacana.put(n, Arrays.asList(suffix));
					k++;
				}
			}
			paradigm.put(p, vacana);
			ind++;
		}
	}
	
	public List<String> getEndings(final PaliConjugation.Person person, final PaliConjugation.Number num) {
		final Map<PaliConjugation.Number, List<String>> numMap = paradigm.get(person);
		return numMap.get(num);
	}

}
