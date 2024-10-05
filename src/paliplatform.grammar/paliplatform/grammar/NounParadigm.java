/*
 * NounParadigm.java
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

import paliplatform.base.PaliWord;

import java.util.*;

/** 
 * The class handling declensional paradigms used in PaliDeclension.
 * @author J.R. Bhaddacak
 * @version 3.0
 * @since 1.0
 */
public class NounParadigm {
	private final String ending;
	private final PaliWord.Gender gender;
	private final Map<PaliDeclension.Case, Map<PaliDeclension.Number, List<String>>> paradigm;
	
	public NounParadigm(final String ending, final PaliWord.Gender gender) {
		this.ending = ending;
		this.gender = gender;
		paradigm = new EnumMap<>(PaliDeclension.Case.class);
	}
	
	public void setParadigm(final String str) {
		final String[] cases = str.split("\\|");
		int ind = 0;
		for (PaliDeclension.Case c : PaliDeclension.Case.values) {
			final Map<PaliDeclension.Number, List<String>> vacana = new EnumMap<>(PaliDeclension.Number.class);
			final String[] nums = cases[ind].split(";");
			int k = 0;
			for (PaliDeclension.Number n : PaliDeclension.Number.values) {
				if (nums.length == 0) {
					vacana.put(n, new ArrayList<String>());
				} else {
					final String[] suffix = nums[k].split(",");
					vacana.put(n, Arrays.asList(suffix));
					k++;
				}
			}
			paradigm.put(c, vacana);
			ind++;
		}
	}
	
	public List<String> getEndings(final PaliDeclension.Case cas, final PaliDeclension.Number num) {
		final Map<PaliDeclension.Number, List<String>> numMap = paradigm.get(cas);
		return numMap.get(num);
	}

}
