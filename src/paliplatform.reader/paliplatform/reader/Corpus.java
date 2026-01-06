/*
 * Corpus.java
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

package paliplatform.reader;

import paliplatform.base.*;
import paliplatform.base.Utilities.Encoding;
import paliplatform.base.Utilities.PaliScript;

import java.io.File;
import java.util.*;
import java.util.regex.*;
import java.util.stream.Collectors;
import javafx.scene.control.TreeItem;

/** 
 * The representation of a Pali text collection.
 * @author J.R. Bhaddacak
 * @version 3.7
 * @since 3.0
 */

public class Corpus {
	public static enum Collection {
		CSTR, CSTDEVA, CST4, BJT, PTST, SRT, GRAM, SC, SKT; // must correspond with corpus root's name
		public static final Collection[] values = values();
		public static final Map<String, Collection> idMap = Map.of("cstr", CSTR, "cstdeva", CSTDEVA, "cst4", CST4, "bjt", BJT,
															"ptst", PTST, "srt", SRT, "gram", GRAM, "sc", SC, "skt", SKT);
		public static final Set<Collection> hasDMSASet = Set.of(CSTR, CSTDEVA, CST4, BJT, PTST, SRT, SC);
		public static final Comparator<String> colComparator = new Comparator<String>() {
			private final List<String> colList = List.of("cstr", "cstdeva", "cst4", "bjt", "ptst", "srt", "gram", "sc", "skt");
			@Override
			public int compare(final String name1, final String name2) {
				return colList.indexOf(name1.toLowerCase()) - colList.indexOf(name2.toLowerCase());
			}
		};
	}
	public static final TextGroup tgAll = new SimpleTextGroup("All:Sabbe:all");
	public static final TextGroup tgNoExt = new SimpleTextGroup("All but Others (No extra):Na añña:noext");
	public static final TextGroup tgDMSA = new SimpleTextGroup("The 4 main Nikāyas (DMSA):DMSA mūla:dmsa");
	public static final TextGroup tgVDMSA = new SimpleTextGroup("The Vinaya and 4 main Nikāyas (VDMSA):Vin + DMSA mūla:vdmsa");
	private static final String NOT_WORD_CSTR = "!()+,-.:;=?[]–‘’“”…";
	private static final String NOT_WORD_CSTDEVA = "!'`()+,-.;=?[]–‘’।॥…";
	private static final String NOT_WORD_CST4 = "!()+,-.;=?[]|–‖‘’§…";
	private static final String NOT_WORD_BJT = "!\"$()*,-./:;<>?[]\\_{}–‘’“”†‡…";
	private static final String NOT_WORD_PTST = "!\"#$%&'()*+,-./:;=?[]^_{|}~§";
	private static final String NOT_WORD_SRT = "!\"#'()*,-./:;=?[]_";
	private static final String NOT_WORD_GRAM = "!()*+,-.:;=?[]–‘’“”…";
	private static final String NOT_WORD_SC = "(),-./:;<>?[]~–—‘’“”…";
	private static final String NOT_WORD_SKT = "!\"#$%&@^'()*+,-./:;=?<>[]{|}§\\~–—_─`’‘“”°«»⟨⟩·⏑…→";
	private static final String SPACES = " \t\n\f\r";
	private final String corpusName;
	private String shortName;
	private final Collection collection;
	private final String rootName; // root directory name, also used as id
	private final String infoFileName; // tree filename
	private final boolean inArchive; // true if the whole collection is in a zip file
	private Encoding encoding = Encoding.UTF_8; // typical values are UTF_8, UTF_16
	private PaliScript script = PaliScript.ROMAN; //typical values are ROMAN, DEVANAGARI
	private File zipFile; // archive file
	private boolean transformable = false; // true if the text can be transformed to other scripts
	private final List<TextGroup> basketGroupList = new ArrayList<>(); // vin or sut or abh
	private final List<TextGroup> classGroupList = new ArrayList<>(); // mul or exe (or att and tik)
	private final List<TextGroup> extraGroupList = new ArrayList<>(); // if any, e.g. anya, extra
	private final Set<String> extraGroupSet = new HashSet<>();
	private final List<TextGroup> extraSubgroupList = new ArrayList<>(); // if any, e.g. chronicles, grammar books in CST4
	private String description;
	private String copyright;
	private final List<String> urlList = new ArrayList<>();
	private final Map<String, DocumentInfo> docInfoMap; // document information map (to doc id)
	private final int size; // number of docs
	private Map<DocumentInfo.SuttaGroup, TreeItem<TocTreeNode>> suttantaGroupMap;
	private Map<String, TreeItem<TocTreeNode>> extraSubgroupMap; // for CSTDEVA and CST4 only

	public Corpus(final String name, final String root, final String infoname, final String inArchiveStr) {
		corpusName = name;
		shortName = "";
		rootName = root;
		collection = Collection.idMap.get(root);
		infoFileName = infoname;
		inArchive = Boolean.parseBoolean(inArchiveStr); // if true, set the zip file also
		docInfoMap = root.equals("sc")
						? ReaderUtilities.loadScDocInfoMap(this)
						: root.equals("skt")
							? ReaderUtilities.loadSktDocInfoMap(this)
							: ReaderUtilities.loadDocInfoMap(this, infoFileName);
		size = docInfoMap.size();
	}

	public String getName() {
		return corpusName;
	}

	public String getShortName() {
		return shortName;
	}

	public void setShortName(final String name) {
		shortName = name;
	}

	public Collection getCollection() {
		return collection;
	}

	public String getRootName() {
		return rootName;
	}

	public String getId() {
		return rootName;
	}

	public String getInfoFileName() {
		return infoFileName;
	}

	public boolean isInArchive() {
		return inArchive;
	}

	public void setZipFile(final String name) {
		zipFile = new File(Utilities.ROOTDIR + ReaderUtilities.TEXTPATH + rootName + File.separator, name);
	}

	public File getZipFile() {
		return zipFile;
	}

	public boolean isAvailable() {
		return inArchive ? zipFile.exists() : true;
	}

	public void setEncoding(final String encName) {
		final String nameOK = encName.replace("-", "_");
		if (Encoding.isValid(nameOK))
			encoding = Encoding.valueOf(nameOK);
	}

	public Encoding getEncoding() {
		return encoding;
	}

	public void setScript(final String scrName) {
		script = PaliScript.fromName(scrName);
	}

	public PaliScript getScript() {
		return script;
	}

	public void setTransformable(final boolean yn) {
		transformable = yn;
	}

	public boolean isTransformable() {
		return transformable;
	}

	public void setTextBasketList(final List<String> bktStrList) {
		for (final String grp : bktStrList) {
			final TextGroup tg = new SimpleTextGroup(grp);
			basketGroupList.add(tg);
		}
	}

	public List<TextGroup> getTextBasketList() {
		return basketGroupList;
	}

	public void setTextClassList(final List<String> clsStrList) {
		for (final String grp : clsStrList) {
			final TextGroup tg = new SimpleTextGroup(grp);
			classGroupList.add(tg);
		}
	}

	public List<TextGroup> getTextClassList() {
		return classGroupList;
	}

	public void setTextExtraGroupList(final List<String> extStrList) {
		for (final String grp : extStrList) {
			final TextGroup tg = new SimpleTextGroup(grp);
			extraGroupList.add(tg);
			extraGroupSet.add(tg.getAbbrev());
		}
	}

	public List<TextGroup> getTextExtraGroupList() {
		return extraGroupList;
	}

	public boolean hasExtraGroup() {
		return !extraGroupList.isEmpty();
	}

	public boolean isInExtraGroup(final String grpAbbr) {
		return extraGroupSet.contains(grpAbbr);
	}

	public void setTextExtraSubgroupList(final List<String> subStrList) {
		for (final String grp : subStrList) {
			final TextGroup tg = new SimpleTextGroup(grp);
			extraSubgroupList.add(tg);
		}
	}

	public List<TextGroup> getTextExtraSubgroupList() {
		return extraSubgroupList;
	}

	public boolean hasDMSA() {
		return Corpus.Collection.hasDMSASet.contains(collection);
	}

	public List<TextGroup> getTextGroupCombination() {
		final List<TextGroup> result = new ArrayList<>();
		for (final TextGroup tgBasket : basketGroupList) {
			if (tgBasket.getAbbrev().equals("sut") && hasDMSA()) {
				result.add(tgVDMSA);
				result.add(tgDMSA);
			}
			for (final TextGroup tgClass : classGroupList) {
				String grpStr = tgBasket.getEngName() + " (" + tgClass.getEngName() + ")";
				grpStr = grpStr + ":" + tgBasket.getPaliName() + " " + tgClass.getPaliName();
				grpStr = grpStr + ":" + tgBasket.getAbbrev() + TextGroup.DELIM + tgClass.getAbbrev();
				final TextGroup tg = new SimpleTextGroup(grpStr);
				result.add(tg);
			}
		}
		return result;
	}

	public Map<String, DocumentInfo> getDocInfoMap() {
		return docInfoMap;
	}

	public DocumentInfo getDocInfo(final String docId) {
		return docInfoMap.get(docId);
	}

	public DocumentInfo getDocInfoByFileName(final String name) {
		final String filename = collection == Collection.PTST
								? name.replace("pu.htm", "ou.htm")
								: collection == Collection.SKT
									? name.replace("/plaintext/", "/html/").replace(".txt", ".htm")
									: name;
		final Optional<DocumentInfo> result = docInfoMap.values().stream()
												.filter(x -> x.getFileNameWithExt().equals(filename))
												.findFirst();
		return result.orElse(null);
	}

	public int getSize() {
		return size;
	}

	public void setDescription(final String desc) {
		description = desc;
	}

	public String getDescription() {
		return description;
	}

	public void setCopyright(final String copyInfo) {
		copyright = copyInfo;
	}

	public String getCopyright() {
		return copyright;
	}

	public void setUrlList(final List<String> list) {
		urlList.clear();
		urlList.addAll(list);
	}

	public List<String> getUrlList() {
		return urlList;
	}

	public String getCorpusInformation() {
		final StringBuilder result = new StringBuilder();
		// the first line is header
		result.append(corpusName).append("\n");
		result.append(description).append("\n\n");
		if (!copyright.isEmpty())
			result.append("Copyright: " + copyright).append("\n\n");
		if (!urlList.isEmpty())
			result.append("URL(s):\n" + urlList.stream().collect(Collectors.joining("\n"))).append("\n");
		return result.toString();
	}

	public TreeItem<TocTreeNode> createTreeNode() {
		final TreeItem<TocTreeNode> treeNode = new TreeItem<>();
		treeNode.setGraphic(new TextIcon("folder", TextIcon.IconSet.AWESOME));
		treeNode.setValue(new SimpleTocTreeNode(this));
		suttantaGroupMap = new EnumMap<>(DocumentInfo.SuttaGroup.class);
		extraSubgroupMap = new HashMap<>();
		if (basketGroupList.isEmpty()) {
			addDocsToNode(treeNode, null, null);
		} else {
			for (final TextGroup bktTG : basketGroupList) {
				final boolean isSuttanta = bktTG.getAbbrev().equals("sut");
				// add branches of basket groups
				final TreeItem<TocTreeNode> pNode = new TreeItem<>(new SimpleTocTreeNode(this, bktTG.getAbbrev(), bktTG.getEngName(), ""));
				if (classGroupList.isEmpty()) {
					// for PTS
					if (isSuttanta) 
						addSuttaGroups(pNode);
					addDocsToNode(pNode, bktTG, null);
				} else {
					for (final TextGroup clsTG : classGroupList) {
						// add branches of text class groups
						final TreeItem<TocTreeNode> sNode = new TreeItem<>(new SimpleTocTreeNode(this, clsTG.getAbbrev(), clsTG.getEngName(), ""));
						if (isSuttanta) 
							addSuttaGroups(sNode);
						addDocsToNode(sNode, bktTG, clsTG);
						pNode.getChildren().add(sNode);
					}
				}
				treeNode.getChildren().add(pNode);
			}
		}
		for (final TextGroup extTG : extraGroupList) {
			// add branches of extra groups
			final TreeItem<TocTreeNode> eNode = new TreeItem<>(new SimpleTocTreeNode(this, extTG.getAbbrev(), extTG.getEngName(), ""));
			final boolean hasAnya = !extraSubgroupList.isEmpty();
			if (hasAnya)
				addAnyaGroups(eNode); // only CSTDEVA and CST4
			for (final DocumentInfo dinfo : docInfoMap.values()) {
				if (dinfo.getGroup().equals(extTG.getAbbrev())) {
					final TreeItem<TocTreeNode> tNode = new TreeItem<>(
							new SimpleTocTreeNode(this, dinfo.getId(), dinfo.getTextName() + " (" + dinfo.refProperty().get() + ")", dinfo.getFileNameWithExt()));
					if (hasAnya)
						extraSubgroupMap.get(dinfo.getDocClass()).getChildren().add(tNode);
					else
						eNode.getChildren().add(tNode);
				}
			}
			treeNode.getChildren().add(eNode);
		}
		return treeNode;
	}

	private void addSuttaGroups(final TreeItem<TocTreeNode> node) {
		// in the suttanta, add subdivisions
		for (final DocumentInfo.SuttaGroup sg : DocumentInfo.SuttaGroup.groups) {
			final TreeItem<TocTreeNode> gNode = new TreeItem<>(new SimpleTocTreeNode(this, sg.toString(), sg.getName(script), ""));
			suttantaGroupMap.put(sg, gNode);
			node.getChildren().add(gNode);
		}
	}

	private void addAnyaGroups(final TreeItem<TocTreeNode> node) {
		// for CSTDEVA and CST4's anya, add subdivisions
		for (final TextGroup tg : extraSubgroupList) {
			final TreeItem<TocTreeNode> sgNode = new TreeItem<>(new SimpleTocTreeNode(this, tg.getAbbrev(), tg.getEngName(), ""));
			extraSubgroupMap.put(tg.getAbbrev(), sgNode);
			node.getChildren().add(sgNode);
		}
	}

	private void addDocsToNode(final TreeItem<TocTreeNode> node, final TextGroup basket, final TextGroup docClass) {
		final boolean isSuttanta = basket == null ? false : basket.getAbbrev().equals("sut");
		for (final DocumentInfo dinfo : docInfoMap.values()) {
			// add text nodes
			boolean cond = true;
			if (basket != null)
				cond = cond && dinfo.getGroup().equals(basket.getAbbrev());
			if (docClass != null)
				cond = cond && dinfo.getDocClass().equals(docClass.getAbbrev());
			if (cond) {
				final TreeItem<TocTreeNode> tNode = new TreeItem<>(
						new SimpleTocTreeNode(this, dinfo.getId(), dinfo.getTextName() + " (" + dinfo.refProperty().get() + ")", dinfo.getFileNameWithExt()));
				if (isSuttanta) {
					suttantaGroupMap.get(dinfo.getSuttaGroup()).getChildren().add(tNode);
				} else {
					node.getChildren().add(tNode);
				}
			}
		}
		if (isSuttanta) {
			// remove empty group, if any
			for (final TreeItem<TocTreeNode> item : suttantaGroupMap.values()) {
				if (item.getChildren().isEmpty())
					node.getChildren().remove(item);
			}
		}
	}

	public String getPageString() {
		// used for PTS edition from GRETIL, BJT, and SRT
		final String result;
		if (collection == Collection.PTST || collection == Collection.SRT)
			result = "[page";
		else
			result = "[";
		return result;
	}

	public static boolean hasFullStructure(final Collection col) {
		return col == Collection.CSTR || col == Collection.CSTDEVA || col == Collection.CST4;
	}

	public static boolean hasAlmostFullButNotes(final Collection col) {
		return col == Collection.BJT;
	}

	public static boolean hasOnlyBodyTextAndHead(final Collection col) {
		return col == Collection.GRAM;
	}

	public static boolean hasOnlyBodyTextAndNotes(final Collection col) {
		return col == Collection.SRT;
	}

	public static String getNotWordString(final Collection col) {
		String result = SPACES;
		switch (col) {
			case CSTR: result = NOT_WORD_CSTR + result; break;
			case CSTDEVA: result = NOT_WORD_CSTDEVA + result; break;
			case CST4: result = NOT_WORD_CST4 + result; break;
			case BJT: result = NOT_WORD_BJT + result; break;
			case PTST: result = NOT_WORD_PTST + result; break;
			case SRT: result = NOT_WORD_SRT + result; break;
			case GRAM: result = NOT_WORD_GRAM + result; break;
			case SC: result = NOT_WORD_SC + result; break;
			case SKT: result = NOT_WORD_SKT + result; break;
		}
		return result;
	}

	public static String getNotWordRex(final Collection col) {
		String result = "";
		switch (col) {
			case CSTR: result = NOT_WORD_CSTR; break;
			case CSTDEVA: result = NOT_WORD_CSTDEVA; break;
			case CST4: result = NOT_WORD_CST4; break;
			case BJT: result = NOT_WORD_BJT; break;
			case PTST: result = NOT_WORD_PTST; break;
			case SRT: result = NOT_WORD_SRT; break;
			case GRAM: result = NOT_WORD_GRAM; break;
			case SC: result = NOT_WORD_SC; break;
			case SKT: result = NOT_WORD_SKT; break;
		}
		// Brackets are problematic when mixed with other tokens,
		// so we have to remove them first, and use them separately.
		// See tokenize() in LuceneFinder.
		result = "[" + result.replace("[", "").replace("]", "").replace("\\", "\\\\") + "\\s]+";
		return result;
	}

	public static Pattern getFileFilterPattern(final Collection col, final String textGroupAbbr) {
		Pattern result;
		switch (col) {
			case CSTR:
				result = Pattern.compile(CstrInfo.getFileFilterPatternString(textGroupAbbr));
				break;
			case CSTDEVA:
			case CST4:
				result = Pattern.compile(Cst4Info.getFileFilterPatternString(textGroupAbbr));
				break;
			case PTST:
				result = Pattern.compile(PtstInfo.getFileFilterPatternString(textGroupAbbr));
				break;
			case BJT:
				result = Pattern.compile(BjtInfo.getFileFilterPatternString(textGroupAbbr));
				break;
			case SRT:
				result = Pattern.compile(SrtInfo.getFileFilterPatternString(textGroupAbbr));
				break;
			case SC:
				result = Pattern.compile(ScInfo.getFileFilterPatternString(textGroupAbbr));
				break;
			case SKT:
				result = Pattern.compile(".*/transformations/plaintext/.*\\.txt");
				break;
			default:
				result = Pattern.compile(".*");
		}
		return result;
	}

	public String toStringFull() {
		return corpusName + ", " + rootName + ", " + infoFileName;
	}

	@Override
	public String toString() {
		return corpusName;
	}

}
