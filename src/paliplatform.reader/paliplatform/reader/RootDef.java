/*
 * RootDef.java
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

import java.util.Map;

/** 
 * The representation of a definition of Pali roots,
 * mainly used in PaliRootFinder.
 * @author J.R. Bhaddacak
 * @version 3.0
 * @since 3.0
 */
public class RootDef {
	public static enum RootBook {
		DHAP("Dhāp", "Pāli Dhātupāṭha"), DHMJS("Dhmjs", "Kaccāyana-dhātumañjūsā"), 
		SADDDHA("Sadd-Dhā", "Saddanīti Dhātumālā"), DHATVA("Dhātva", "Dhātvatthasaṅgaha");
		private final String ref;
		private final String name;
		public static final RootBook[] values = values();
		public static final Map<String, RootBook> idMap = Map.of("dp", DHAP, "dm", DHMJS, "sd", SADDDHA, "ds", DHATVA);
		private RootBook(final String ref, final String name) {
			this.ref = ref;
			this.name = name;
		}
		public String getRef() {
			return ref;
		}
		public String getName() {
			return name;
		}
	}
	public static enum Variant {
		NO("Normal"), AS("Andersen and Smith"), CS("Chaṭṭha Saṅgāyana"), PP("Pālipage"), SM("Smith"), SY("Syāma");
		private final String name;
		private Variant(final String name) {
			this.name = name;
		}
		public String getName() {
			return name;
		}
	}
	public static final String[] rootGroupRoman = { "I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX" };
	public static final String[] rootGroupName9 = { "bhū", "rudha", "diva", "tuda", "ji", "kī", "su", "tana", "cura" };
	public static final String[] rootGroupName8 = { "bhū", "rudha", "diva", "su", "kī", "gaha", "tanu", "cura" };
	private final String rootName;
	private final RootBook book;
	private final int ref; // reference/stanza number in the book
	private String definition = "";
	private int group = 0;
	private String groupString = "";
	private Variant variant = Variant.NO;

	public RootDef(final String root, final String bookId, final String refStr) {
		rootName = root;
		book = RootBook.idMap.get(bookId);
		ref = Integer.parseInt(refStr);
	}

	public String getRoot() {
		return rootName;
	}

	public RootBook getBook() {
		return book;
	}

	public int getRefNum() {
		return ref;
	}

	public String getFullRef() {
		return book.getRef() + " " + ref;
	}

	public void setDefinition(final String def) {
		definition = def;
	}

	public String getDefinition() {
		return definition;
	}

	public void setGroup(final String grpStr) {
		group = Integer.parseInt(grpStr);
		final int ind = group - 1;
		groupString = book == RootBook.DHAP
						? rootGroupRoman[ind].toLowerCase() + " " + rootGroupName9[ind]
						: rootGroupRoman[ind] + " " + rootGroupName8[ind];
	}

	public int getGroup() {
		return group;
	}

	public String getGroupString() {
		return groupString;
	}

	public void setVariant(final String varStr) {
		variant = varStr.isEmpty() ? Variant.NO : Variant.valueOf(varStr.toUpperCase());
		// count CS and PP as normal
		if (variant == Variant.CS || variant == Variant.PP)
			variant = Variant.NO;
	}

	public Variant getVariant() {
		return variant;
	}

}

