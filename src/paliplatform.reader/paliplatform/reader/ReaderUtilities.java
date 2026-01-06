/*
 * ReaderUtilities.java
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
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see https://www.gnu.org/licenses/.
 */

package paliplatform.reader;

import paliplatform.base.*;

import java.util.*;
import java.util.regex.*;
import java.util.zip.*;
import java.util.stream.*;
import java.util.function.Function;
import java.util.ServiceLoader.Provider;
import java.io.*;
import java.nio.file.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.RuleBasedCollator;
import java.text.ParseException;
import org.w3c.dom.*;
import org.xml.sax.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;

import javafx.stage.Stage;
import javafx.scene.image.Image;
import javafx.application.Platform;
import javafx.collections.*;
import javafx.beans.property.SimpleBooleanProperty;

import com.google.gson.stream.*;

/** 
 * The utility factory for the Reader module.
 * @author J.R. Bhaddacak
 * @version 3.7
 * @since 3.0
 */
final public class ReaderUtilities {
	private static final String LINESEP = System.getProperty("line.separator");
	public static final Map<Integer, String> bulletMap = Map.of(0, "•", 1, "‣", 2, "›", 3, "‐", 4, "»"); 
	public static final String TEXTPATH = Utilities.DATAPATH + "text" + File.separator;
	public static final String TXTDIR = "resources/text/";
	public static final String CSSDIR = "resources/styles/";
	public static final String JSDIR = "resources/js/";
	public static final String PALIHTML_CSS = CSSDIR + "palihtml.css";
	public static final String CSTR_CSS = CSSDIR + "cstr.css";
	public static final String CST4_CSS = CSSDIR + "cst4.css";
	public static final String CST4_XSL = CSSDIR + "cst4.xsl";
	public static final String BJT_CSS = CSSDIR + "bjt.css";
	public static final String SC_CSS = CSSDIR + "sc.css";
	public static final String SKTGRETIL_IND_CSS = CSSDIR + "sktgretil_ind.css";
	public static final String SKTGRETIL_DOC_CSS = CSSDIR + "sktgretil_doc.css";
	public static final String PALI_JS = JSDIR + "pali-viewer.js"; // used by all PaliHtmlViewer
	public static final String CSTR_JS = JSDIR + "cstr-viewer.js"; // used only by CSTR
	public static final String CST4_JS = JSDIR + "cst4-viewer.js"; // used only by CST4
	public static final String BJT_JS = JSDIR + "bjt-viewer.js"; // used only by BJT
	public static final String GRETIL_JS = JSDIR + "gretil-viewer.js"; // used only by PTS
	public static final String SRT_JS = JSDIR + "srt-viewer.js"; // used only by SRT
	public static final String GRAM_JS = JSDIR + "gram-viewer.js"; // used only by gram books
	public static final String SKTGRETIL_JS = JSDIR + "sktgretil-viewer.js"; // used only by Skt GRETIL
	public static final String CORPUS_INFO = TXTDIR + "corpus-info.xml";
	public static final String ROOT_DEF = TXTDIR + "rootdef.xml";
	public static final String REF_DATA = TXTDIR + "references.xml";
	public static final String GRAM_SUT = "gramsut.txt";
	public static final String GRAM_SUT_XREF = "gramsutniruxref.csv";
	public static final String NIRU_TO_MOGG = "nirumogg.csv";
	public static final String SCPATH = TEXTPATH + "sc" + File.separator;
	public static final String BILARA_DATA = "bilara-data-published.zip";
	public static final File scData = new File(Utilities.ROOTDIR + SCPATH + BILARA_DATA);
	public static final String SC_HEADS = "sc-heads.txt";
	public static final File scHeadFile = new File(Utilities.ROOTDIR + SCPATH + SC_HEADS);
	public static final String SKT_GRETIL_DATA = "1_sanskr.zip";
	public static final File sktGretilData = new File(Utilities.ROOTDIR + TEXTPATH + "skt" + File.separator + SKT_GRETIL_DATA);
	public static final String SKT_GRETIL_INDEX = "gretil_skt_index.txt";
	public static final SimpleBooleanProperty corpusAvailable = new SimpleBooleanProperty(false);
	public static final SimpleBooleanProperty suttaCentralAvailable = new SimpleBooleanProperty(false);
	public static final SimpleBooleanProperty sktGretilAvailable = new SimpleBooleanProperty(false);
	public static Map<String, SimpleService> simpleServiceMap;
	public static Map<String, SktService> sktServiceMap;
	public static Map<Corpus.Collection, Corpus> corpusMap;
	public static ObservableList<String> corpusAbbrList =  FXCollections.observableArrayList();
	public static List<RootDef> rootList;
	public static List<GrammarSutta> gramSutList;
	public static List<String> gramSutNiruXrefList;
	public static Map<String, Set<String>> gramSutXrefMap; // map to shortId
	public static List<Reference> referenceList;
	public static Map<String, String> scSuttaInfoMap = new HashMap<>();
	public static Comparator<String> gramSutRefComparator;
	public static String sktGretilIndexHtml = "";
	public static List<String> sktGretilIndexHeadList = new ArrayList<>();

	public static Comparator<String> getReferenceComparator(final Corpus corpus) {
		Comparator<String> result = Utilities.alphanumComparator;
		try {
			switch (corpus.getCollection()) {
				case GRAM:
					final String gramBookNameRule = "< k < r < m < p < n < s"; // only some having suttas
					final RuleBasedCollator gramBookCollator = new RuleBasedCollator(gramBookNameRule);
					result = new Comparator<String>() {
						@Override
						public int compare(final String aName, final String bName) {
							// available only with short form, i.e. k1, r1, m1.1, n1, s1
							// separate letters from digits
							int dpos = aName.indexOf(".");
							int ind = dpos > 0 ? dpos - 1 : aName.length() - 1;
							while (Character.isDigit(aName.charAt(ind)))
								ind--;
							final String aNameLetter = aName.substring(0, ind + 1);
							final String aNameNumStr = aName.substring(ind + 1);
							final float aNameNum = aNameNumStr.isEmpty() ? 0.0F : Float.parseFloat(aNameNumStr);
							dpos = bName.indexOf(".");
							ind = dpos > 0 ? dpos - 1 : bName.length() - 1;
							while (Character.isDigit(bName.charAt(ind)))
								ind--;
							final String bNameLetter = bName.substring(0, ind + 1);
							final String bNameNumStr = bName.substring(ind + 1);
							final float bNameNum = bNameNumStr.isEmpty() ? 0.0F : Float.parseFloat(bNameNumStr);
							// compare each part
							int result = gramBookCollator.compare(aNameLetter, bNameLetter);
							if (result == 0) {
								return Float.compare(aNameNum, bNameNum);
							} else {
								return result;
							}
						}
					};
					break;
			}
		} catch (ParseException e) {
			System.err.println(e);
		}
		return result;
	}

	static String getStringResource(final String fileNameWithPath) {
		String result = "";
		try {
			final InputStream in = ReaderUtilities.class.getResourceAsStream(fileNameWithPath);
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

	static List<String> getTextResourceAsList(final String filename) {
		final List<String> result = new ArrayList<>();
		final String text = getStringResource(TXTDIR + filename);
		final String[] lines = text.split("\\r?\\n");
		for (final String line : lines) {
			final String theLine = line.trim();
			if (theLine.isEmpty()) continue;
			result.add(theLine);
		}
		return result;
	}

	static InputStream getTextResourceAsStream(final String fileName) {
		return ReaderUtilities.class.getResourceAsStream(TXTDIR + fileName);
	}

	public static Map<String, SimpleService> getSimpleServices() {
		return ServiceLoader.load(SimpleService.class)
				.stream()
				.map(Provider::get)
				.collect(Collectors.toMap(x -> x.getClass().getName(), Function.identity()));
	}

	public static Map<String, SktService> getSktServices() {
		return ServiceLoader.load(SktService.class)
				.stream()
				.map(Provider::get)
				.collect(Collectors.toMap(x -> x.getClass().getName(), Function.identity()));
	}

	public static void loadScSuttaInfo() {
		if (!scSuttaInfoMap.isEmpty()) return;
		if (!scHeadFile.exists()) return;
		try (final Scanner in = new Scanner(new FileInputStream(scHeadFile), StandardCharsets.UTF_8)) {
			while (in.hasNextLine()) {
				final String line = in.nextLine().trim();
				if (line.isEmpty()) continue;
				final String[] pair = line.split(":");
				if (pair.length > 1)
					scSuttaInfoMap.put(pair[0], pair[1]);
			}
		} catch (FileNotFoundException e) {
			System.err.println(e);
		}
	}

	public static void updateCorpusList() {
		updateCorpusList(false);
	}

	public static void updateCorpusList(final boolean cliMode) {
		try {
			final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			final DocumentBuilder db = dbf.newDocumentBuilder();
			final InputStream in = ReaderUtilities.class.getResourceAsStream(CORPUS_INFO);
			if (in == null) return;
			final Document doc = db.parse(in);
			final NodeList corpora = doc.getElementsByTagName("corpus");
			corpusMap = new EnumMap<>(Corpus.Collection.class);
			for (int i = 0; i < corpora.getLength(); i++) {
				final Element corpus = (Element) corpora.item(i);
				final NodeList names = corpus.getElementsByTagName("name");
				final String corpusName = names.getLength() > 0 ? getTextNodeContent((Element) names.item(0)).trim() : "";
				final NodeList shortNames = corpus.getElementsByTagName("shortname");
				final String shortName = shortNames.getLength() > 0 ? getTextNodeContent((Element) shortNames.item(0)).trim() : "";
				final NodeList roots = corpus.getElementsByTagName("root");
				final String rootName = roots.getLength() > 0 ? getTextNodeContent((Element) roots.item(0)).trim() : "";
				final NodeList infoFiles = corpus.getElementsByTagName("infofile");
				final String infoFile = infoFiles.getLength() > 0 ? getTextNodeContent((Element) infoFiles.item(0)).trim() : "";
				final NodeList inArchives = corpus.getElementsByTagName("inarchive");
				final String inArchive = inArchives.getLength() > 0 ? getTextNodeContent((Element) inArchives.item(0)).trim() : "";
				if (corpusName.isEmpty() || rootName.isEmpty() || inArchive.isEmpty())
					continue;
				final NodeList zipFiles = corpus.getElementsByTagName("zipfile");
				final String zipFile = zipFiles.getLength() > 0 ? getTextNodeContent((Element) zipFiles.item(0)).trim() : "";
				final NodeList encodings = corpus.getElementsByTagName("encoding");
				final String encoding = encodings.getLength() > 0 ? getTextNodeContent((Element) encodings.item(0)).trim() : "";
				final NodeList scripts = corpus.getElementsByTagName("script");
				final String script = scripts.getLength() > 0 ? getTextNodeContent((Element) scripts.item(0)).trim() : "";
				final NodeList transformables = corpus.getElementsByTagName("transformable");
				final String transformable = transformables.getLength() > 0 ? getTextNodeContent((Element) transformables.item(0)).trim() : "";
				final NodeList bktGrps = corpus.getElementsByTagName("textbasket");
				final List<String> bktStrList = new ArrayList<>();
				for (int g = 0; g < bktGrps.getLength(); g++) {
					bktStrList.add(bktGrps.item(g).getTextContent().trim());
				}
				final NodeList clsGrps = corpus.getElementsByTagName("textclass");
				final List<String> clsStrList = new ArrayList<>();
				for (int g = 0; g < clsGrps.getLength(); g++) {
					clsStrList.add(clsGrps.item(g).getTextContent().trim());
				}
				final NodeList extGrps = corpus.getElementsByTagName("extragroup");
				final List<String> extStrList = new ArrayList<>();
				for (int g = 0; g < extGrps.getLength(); g++) {
					extStrList.add(extGrps.item(g).getTextContent().trim());
				}
				final NodeList subextGrps = corpus.getElementsByTagName("extrasubgroup");
				final List<String> subextStrList = new ArrayList<>();
				for (int g = 0; g < subextGrps.getLength(); g++) {
					subextStrList.add(subextGrps.item(g).getTextContent().trim());
				}
				final NodeList descs = corpus.getElementsByTagName("description");
				final String description = descs.getLength() > 0 ? getTextNodeContent((Element) descs.item(0)).trim() : "";
				final NodeList copys = corpus.getElementsByTagName("copyright");
				final String copyright = copys.getLength() > 0 ? getTextNodeContent((Element) copys.item(0)).trim() : "";
				final NodeList urls = corpus.getElementsByTagName("url");
				final List<String> urlList = new ArrayList<>();
				for (int g = 0; g < urls.getLength(); g++) {
					urlList.add(urls.item(g).getTextContent().trim());
				}
				final Corpus cp = new Corpus(corpusName, rootName, infoFile, inArchive);
				cp.setShortName(shortName);
				if (Boolean.parseBoolean(inArchive))
					cp.setZipFile(zipFile);
				cp.setEncoding(encoding);
				cp.setScript(script);
				cp.setTransformable(Boolean.parseBoolean(transformable));
				cp.setTextBasketList(bktStrList);
				cp.setTextClassList(clsStrList);
				cp.setTextExtraGroupList(extStrList);
				cp.setTextExtraSubgroupList(subextStrList);
				cp.setDescription(description);
				cp.setCopyright(copyright);
				cp.setUrlList(urlList);
				corpusMap.put(Corpus.Collection.idMap.get(rootName), cp);
			}
			corpusAvailable.set(!corpusMap.isEmpty());
			if (!cliMode)
				Platform.runLater(() -> updateCorpusAbbrList());
		} catch (ParserConfigurationException | SAXException | IOException e) {
			System.err.println(e);
		}
	}

	public static void updateCorpusAbbrList() {
		final List<String> cList = corpusMap.values().stream()
								.filter(x -> x.isAvailable() && !corpusAbbrList.contains(x.getCollection().toString()))
								.map(x -> x.getRootName().toUpperCase())
								.collect(Collectors.toList());
		corpusAbbrList.addAll(cList);
		FXCollections.sort(corpusAbbrList, Corpus.Collection.colComparator);
	}

	public static String getContentSearchSource(final Corpus corpus, final String fname) {
		final String result = corpus.getCollection() == Corpus.Collection.PTST
								? fname.replace("ou.htm", "pu.htm")
								: corpus.getCollection() == Corpus.Collection.SKT
									? fname.replace("/html/", "/plaintext/").replace(".htm", ".txt")
									: fname;
		return result;
	}

	public static Map<String, DocumentInfo> loadScDocInfoMap(final Corpus corpus) {
		Map<String, DocumentInfo> result = Collections.emptyMap();
		if (!suttaCentralAvailable.get()) return result;
		try {
			result = new HashMap<>();
			final ZipFile zip = new ZipFile(scData);
			for (final Enumeration<? extends ZipEntry> e = zip.entries(); e.hasMoreElements();) {
				final ZipEntry entry = e.nextElement();
				final String fname = entry.getName();
				if (fname.endsWith(ScDocument.ROOT_PLI_MS_END) && fname.indexOf("playground") == -1) {
					final String id = fname.substring(fname.lastIndexOf("/") + 1, fname.lastIndexOf(ScDocument.ROOT_PLI_MS_END));
					final ScInfo scInfo = new ScInfo(corpus, id);
					final String grp = fname.contains("/vinaya/") ? "vin"
										: fname.contains("/sutta/") ? "sut"
										: fname.contains("/abhidhamma/") ? "abh"
										: "";
					scInfo.setGroup(grp);
					scInfo.setRef(id);
					scInfo.setFileName(fname);
					final String head = scSuttaInfoMap.getOrDefault(id, "");
					final String info = head.endsWith(";") ? head.substring(0, head.length() - 1) : head;
					final String tname = info.isEmpty() ? "" : info.replace(";", ", ");
					scInfo.setTextName(tname);
					scInfo.setSummary();
					result.put(id, scInfo);
				}
			}
			zip.close();
		} catch (IOException e) {
			System.err.println(e);
		}
		return result;
	}

	public static Map<String, DocumentInfo> loadSktDocInfoMap(final Corpus corpus) {
		final Map<String, DocumentInfo> result = new HashMap<>();
		final String indStr = getTextResource(SKT_GRETIL_INDEX);
		final StringBuilder indexHtml = new StringBuilder();
		indexHtml.append("<h2>GRETIL Sanskrit Collection</h2>\n");
		final String[] lines = indStr.split("\\r?\\n");
		String currGroup = "";
		int headCounter = 0;
		for (int i = 0; i < lines.length; i += 2) {
			final String desc = lines[i];
			final String link = lines[i + 1];
			if (link.startsWith("#")) {
				final String[] heads = desc.substring(1).split(",");
				final String level = heads[0].trim();
				final String head = heads[1].trim();
				final String grp = heads.length > 2 ? heads[2].trim() : "";
				indexHtml.append("<h" + level + " id='jumptarget-h" + headCounter + "'>" + head + "</h" + level + ">\n");
				final int lv = Integer.parseInt(level);
				String spaces = " ";
				int start = 3;
				while (lv - start > 0) {
					spaces = spaces + " ";
					start++;
				}
				final String indent = lv > 3 ? spaces : "";
				sktGretilIndexHeadList.add(indent + head);
				if (lv == 3)
					currGroup = grp;
				headCounter++;
				continue;
			} else {
				indexHtml.append("<div style='margin-top:0.5em;'>&nbsp;&nbsp;&nbsp;&nbsp;" + desc + "</div>\n");
				if (link.startsWith("-") || !link.contains("/transformations/")) {
					indexHtml.append("<div style='margin-left:2em;'>• (Unavailable)</div>\n");
					continue;
				} else {
					final String id = link.substring(link.lastIndexOf("/") + 1, link.lastIndexOf("."));
					final SimpleDocumentInfo sktInfo = new SimpleDocumentInfo(corpus, id);
					final String fileName = link.replace("gretil/corpustei/", "1_sanskr/tei/");
					sktInfo.setFileName(fileName);
					final String textName = id.replace("sa_", "");
					sktInfo.setTextName(textName);
					sktInfo.setDescription(desc);
					sktInfo.setSummary();
					sktInfo.setGroup(currGroup);
					result.put(id, sktInfo);
					final String bareName = fileName.substring(fileName.lastIndexOf("/") + 1);
					indexHtml.append("<div class='doclink' onClick=openDoc('" + bareName + "')>• " + bareName + "</div>\n");
				}
			}
		} // end for
		indexHtml.append("\n<p></p>\n");
		sktGretilIndexHtml = indexHtml.toString();
		return result;
	}

	public static Map<String, DocumentInfo> loadDocInfoMap(final Corpus corpus, final String infoName) {
		Map<String, DocumentInfo> result = Collections.emptyMap();
		try {
			final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			final DocumentBuilder db = dbf.newDocumentBuilder();
			final InputStream in = ReaderUtilities.class.getResourceAsStream(TXTDIR + infoName);
			if (in == null) return result;
			final Document doc = db.parse(in);
			final NodeList texts = doc.getElementsByTagName("text");
			result = new LinkedHashMap<>();
			int len;
			for (int i = 0; i < texts.getLength(); i++) {
				final Element elm = (Element) texts.item(i);
				final String id = elm.getAttribute("id");
				DocumentInfo docInfo = null;
				switch (corpus.getCollection()) {
					case CSTR:
						docInfo = readCstrInfo(elm, corpus);
						break;
					case CSTDEVA:
					case CST4:
						docInfo = readCst4Info(elm, corpus);
						break;
					case PTST:
						docInfo = readPtstInfo(elm, corpus);
						break;
					case BJT:
						docInfo = readBjtInfo(elm, corpus);
						break;
					case SRT:
						docInfo = readSrtInfo(elm, corpus);
						break;
					case GRAM:
						docInfo = readGramInfo(elm, corpus);
						break;
				}
				if (docInfo != null)
					result.put(id, docInfo);
			}
		} catch (ParserConfigurationException | SAXException | IOException e) {
			System.err.println(e);
		}
		return result;
	}

	private static DocumentInfo readCstrInfo(final Element elm, final Corpus corpus) {
		final String id = elm.getAttribute("id");
		final CstrInfo cstrInfo = new CstrInfo(corpus, id);
		final NodeList groups = elm.getElementsByTagName("group");
		final String group = groups.getLength() > 0 ? getTextNodeContent((Element) groups.item(0)) : "";
		cstrInfo.setGroup(group);
		final NodeList docClasses = elm.getElementsByTagName("docclass");
		final String docClass = docClasses.getLength() > 0 ? getTextNodeContent((Element) docClasses.item(0)) : "";
		cstrInfo.setDocClass(docClass);
		final NodeList refs = elm.getElementsByTagName("ref");
		final String ref = refs.getLength() > 0 ? getTextNodeContent((Element) refs.item(0)) : "";
		cstrInfo.setRef(ref);
		final NodeList fileNames = elm.getElementsByTagName("file");
		final String fileName = fileNames.getLength() > 0 ? getTextNodeContent((Element) fileNames.item(0)) : "";
		cstrInfo.setFileName(fileName);
		final NodeList textNames = elm.getElementsByTagName("name");
		final String textName = textNames.getLength() > 0 ? getTextNodeContent((Element) textNames.item(0)) : "";
		cstrInfo.setTextName(textName);
		final NodeList altNames = elm.getElementsByTagName("altname");
		cstrInfo.addAllAltNames(getTextNodeContentList(altNames));
		final NodeList cscdClasses = elm.getElementsByTagName("cscdclass");
		final String cscdClass = cscdClasses.getLength() > 0 ? getTextNodeContent((Element) cscdClasses.item(0)) : "";
		cstrInfo.setCscdClass(cscdClass);
		final NodeList cscdFiles = elm.getElementsByTagName("cscdfile");
		cstrInfo.addAllCscdFileNames(getTextNodeContentList(cscdFiles));
		final NodeList descriptions = elm.getElementsByTagName("description");
		final String description = descriptions.getLength() > 0 ? getTextNodeContent((Element) descriptions.item(0)) : "";
		cstrInfo.setDescription(description);
		final NodeList commentaries = elm.getElementsByTagName("commentary");
		cstrInfo.addAllCommentaries(getTextNodeContentList(commentaries));
		final NodeList linkables = elm.getElementsByTagName("linkable");
		final boolean linkable = linkables.getLength() > 0 ? Boolean.parseBoolean(getTextNodeContent((Element) linkables.item(0))) : false;
		cstrInfo.setLinkable(linkable);
		cstrInfo.setSummary();
		return cstrInfo;
	}

	private static DocumentInfo readCst4Info(final Element elm, final Corpus corpus) {
		final String id = elm.getAttribute("id");
		final Cst4Info cst4Info = new Cst4Info(corpus, id);
		final NodeList groups = elm.getElementsByTagName("group");
		final String group = groups.getLength() > 0 ? getTextNodeContent((Element) groups.item(0)) : "";
		cst4Info.setGroup(group);
		final NodeList docClasses = elm.getElementsByTagName("docclass");
		final String docClass = docClasses.getLength() > 0 ? getTextNodeContent((Element) docClasses.item(0)) : "";
		cst4Info.setDocClass(docClass);
		final NodeList refs = elm.getElementsByTagName("ref");
		cst4Info.setRefList(getTextNodeContentList(refs));
		final NodeList fileNames = elm.getElementsByTagName("file");
		final String fileName = fileNames.getLength() > 0 ? getTextNodeContent((Element) fileNames.item(0)) : "";
		cst4Info.setFileName(fileName);
		final NodeList textNames = elm.getElementsByTagName("name");
		cst4Info.setTextName(getTextNodeContentList(textNames));
		final NodeList linkIds = elm.getElementsByTagName("linkid");
		final String linkId = linkIds.getLength() > 0 ? getTextNodeContent((Element) linkIds.item(0)) : "";
		cst4Info.setLinkId(linkId);
		cst4Info.setSummary();
		return cst4Info;
	}

	private static DocumentInfo readPtstInfo(final Element elm, final Corpus corpus) {
		final String id = elm.getAttribute("id");
		final SimpleDocumentInfo ptstInfo = new PtstInfo(corpus, id);
		final NodeList groups = elm.getElementsByTagName("group");
		final String group = groups.getLength() > 0 ? getTextNodeContent((Element) groups.item(0)) : "";
		ptstInfo.setGroup(group);
		final NodeList refs = elm.getElementsByTagName("ref");
		final String ref = refs.getLength() > 0 ? getTextNodeContent((Element) refs.item(0)) : "";
		ptstInfo.setRef(ref);
		final NodeList fileNames = elm.getElementsByTagName("file");
		final String fileName = fileNames.getLength() > 0 ? getTextNodeContent((Element) fileNames.item(0)) : "";
		ptstInfo.setFileName(fileName);
		final NodeList textNames = elm.getElementsByTagName("name");
		final String textName = textNames.getLength() > 0 ? getTextNodeContent((Element) textNames.item(0)) : "";
		ptstInfo.setTextName(textName);
		final NodeList descriptions = elm.getElementsByTagName("description");
		final String description = descriptions.getLength() > 0 ? getTextNodeContent((Element) descriptions.item(0)) : "";
		ptstInfo.setDescription(description);
		ptstInfo.setSummary();
		return ptstInfo;
	}

	private static DocumentInfo readBjtInfo(final Element elm, final Corpus corpus) {
		final String id = elm.getAttribute("id");
		final BjtInfo bjtInfo = new BjtInfo(corpus, id);
		final NodeList groups = elm.getElementsByTagName("group");
		final String group = groups.getLength() > 0 ? getTextNodeContent((Element) groups.item(0)) : "";
		bjtInfo.setGroup(group);
		final NodeList docClasses = elm.getElementsByTagName("docclass");
		final String docClass = docClasses.getLength() > 0 ? getTextNodeContent((Element) docClasses.item(0)) : "";
		bjtInfo.setDocClass(docClass);
		final NodeList refs = elm.getElementsByTagName("ref");
		final String ref = refs.getLength() > 0 ? getTextNodeContent((Element) refs.item(0)) : "";
		bjtInfo.setRef(ref);
		final NodeList fileNames = elm.getElementsByTagName("file");
		final String fileName = fileNames.getLength() > 0 ? getTextNodeContent((Element) fileNames.item(0)) : "";
		bjtInfo.setFileName(fileName);
		final NodeList textNames = elm.getElementsByTagName("name");
		final String textName = textNames.getLength() > 0 ? getTextNodeContent((Element) textNames.item(0)) : "";
		bjtInfo.setTextName(textName);
		final NodeList altNames = elm.getElementsByTagName("altname");
		bjtInfo.addAllAltNames(getTextNodeContentList(altNames));
		final NodeList descriptions = elm.getElementsByTagName("description");
		final String description = descriptions.getLength() > 0 ? getTextNodeContent((Element) descriptions.item(0)) : "";
		bjtInfo.setDescription(description);
		final NodeList commentaries = elm.getElementsByTagName("commentary");
		bjtInfo.addAllCommentaries(getTextNodeContentList(commentaries));
		bjtInfo.setSummary();
		return bjtInfo;
	}

	private static DocumentInfo readSrtInfo(final Element elm, final Corpus corpus) {
		final String id = elm.getAttribute("id");
		final SimpleDocumentInfo srtInfo = new SrtInfo(corpus, id);
		final NodeList groups = elm.getElementsByTagName("group");
		final String group = groups.getLength() > 0 ? getTextNodeContent((Element) groups.item(0)) : "";
		srtInfo.setGroup(group);
		final NodeList docClasses = elm.getElementsByTagName("docclass");
		final String docClass = docClasses.getLength() > 0 ? getTextNodeContent((Element) docClasses.item(0)) : "";
		((SrtInfo)srtInfo).setDocClass(docClass);
		final NodeList refs = elm.getElementsByTagName("ref");
		((SrtInfo)srtInfo).setRefList(getTextNodeContentList(refs));
		final NodeList fileNames = elm.getElementsByTagName("file");
		final String fileName = fileNames.getLength() > 0 ? getTextNodeContent((Element) fileNames.item(0)) : "";
		srtInfo.setFileName(fileName);
		final NodeList textNames = elm.getElementsByTagName("name");
		final String textName = textNames.getLength() > 0 ? getTextNodeContent((Element) textNames.item(0)) : "";
		srtInfo.setTextName(textName);
		srtInfo.setSummary();
		return srtInfo;
	}

	private static DocumentInfo readGramInfo(final Element elm, final Corpus corpus) {
		final String id = elm.getAttribute("id");
		final SimpleDocumentInfo gramInfo = new SimpleDocumentInfo(corpus, id);
		final NodeList refs = elm.getElementsByTagName("ref");
		final String ref = refs.getLength() > 0 ? getTextNodeContent((Element) refs.item(0)) : "";
		gramInfo.setRef(ref);
		final NodeList fileNames = elm.getElementsByTagName("file");
		final String fileName = fileNames.getLength() > 0 ? getTextNodeContent((Element) fileNames.item(0)) : "";
		gramInfo.setFileName(fileName);
		final NodeList textNames = elm.getElementsByTagName("name");
		final String textName = textNames.getLength() > 0 ? getTextNodeContent((Element) textNames.item(0)) : "";
		gramInfo.setTextName(textName);
		final NodeList descriptions = elm.getElementsByTagName("description");
		final String description = descriptions.getLength() > 0 ? getTextNodeContent((Element) descriptions.item(0)) : "";
		gramInfo.setDescription(description);
		gramInfo.setSummary();
		return gramInfo;
	}

	public static void updateRootList() {
		try {
			final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			final DocumentBuilder db = dbf.newDocumentBuilder();
			final InputStream in = ReaderUtilities.class.getResourceAsStream(ROOT_DEF);
			if (in == null) return;
			final Document doc = db.parse(in);
			final NodeList roots = doc.getElementsByTagName("item");
			rootList = new ArrayList<>();
			for (int i = 0; i < roots.getLength(); i++) {
				final Element root = (Element) roots.item(i);
				final NodeList names = root.getElementsByTagName("root");
				final String rootName = names.getLength() > 0 ? getTextNodeContent((Element) names.item(0)).trim() : "";
				final NodeList books = root.getElementsByTagName("book");
				final String bookId = books.getLength() > 0 ? getTextNodeContent((Element) books.item(0)).trim() : "";
				final NodeList refs = root.getElementsByTagName("ref");
				final String refName = refs.getLength() > 0 ? getTextNodeContent((Element) refs.item(0)).trim() : "0";
				final RootDef rootDef = new RootDef(rootName, bookId, refName);
				final NodeList defs = root.getElementsByTagName("def");
				final String def = defs.getLength() > 0 ? getTextNodeContent((Element) defs.item(0)).trim() : "";
				rootDef.setDefinition(def);
				final NodeList grps = root.getElementsByTagName("grp");
				final String group = grps.getLength() > 0 ? getTextNodeContent((Element) grps.item(0)).trim() : "0";
				rootDef.setGroup(group);
				final NodeList vars = root.getElementsByTagName("var");
				final String variant = vars.getLength() > 0 ? getTextNodeContent((Element) vars.item(0)).trim() : "";
				rootDef.setVariant(variant);
				rootList.add(rootDef);
			}
		} catch (ParserConfigurationException | SAXException | IOException e) {
			System.err.println(e);
		}
	}

	public static void updateGramSutList() {
		// read Niru Xref first
		gramSutNiruXrefList = ReaderUtilities.getTextResourceAsList(ReaderUtilities.GRAM_SUT_XREF);
		final String gsText = ReaderUtilities.getTextResource(ReaderUtilities.GRAM_SUT);
		final String[] lines = gsText.split("\\r?\\n");
		gramSutList = new ArrayList<>();
		gramSutXrefMap = new HashMap<>();
		for (final String line : lines) {
			final String theLine = line.trim();
			if (theLine.isEmpty()) continue;
			final GrammarSutta gsut = new GrammarSutta(theLine);
		   gramSutList.add(gsut);
		}
		// process Xref data
		// process Mogg <--> Niru map first
		for (final String line : gramSutNiruXrefList) {
			final int npos = line.indexOf("n");
			final int mpos = line.indexOf("m");
			if ( npos > -1 && mpos > -1) {
				final String nRef = line.substring(npos, line.indexOf(",", npos));
				final String mRef = line.substring(mpos, line.indexOf(",", mpos));
				gramSutXrefMap.put(mRef, Set.of(nRef));
				gramSutXrefMap.put(nRef, Set.of(mRef));
			}
		}
		// process Kacc <--> Rupa and Mogg <--> Payo <--> Niru
		for (final GrammarSutta gsut : gramSutList) {
			final String sutLine = gsut.getOriginal();
			final String shortRef = gsut.getShortRef();
			final int dpos = sutLine.indexOf(".");
			final char refCh = shortRef.charAt(0);
			if (refCh == 'k') {
				// Kacc num format = xxx:yyy [aaa], possibly [aaa, bbb] or [x]
				final String kRef = shortRef;
				final int bpos1 = sutLine.indexOf("[");
				final int bpos2 = sutLine.indexOf("]");
				final String[] rRefs = sutLine.substring(bpos1 + 1, bpos2).split(",");
				final Set<String> rRefSet = new HashSet<>();
				for (final String rRef : rRefs) {
					if (rRef.indexOf('x') < 0)
						rRefSet.add("r" + rRef.trim());
				}
				gramSutXrefMap.put(kRef, rRefSet);
			} else if (refCh == 'r') {
				// Rūpa num format = aaa [xxx:yyy]
				final String rRef = shortRef;
				final int bpos1 = sutLine.indexOf("[");
				final int bpos2 = sutLine.indexOf("]");
				final String kRef = sutLine.substring(bpos1 + 1, bpos2);
				final Set<String> kRefSet = Set.of("k" + kRef);
				gramSutXrefMap.put(rRef, kRefSet);
			} else if (refCh == 'm') {
				// Mogg case, fill up missing cases, not in Mogg-Niru relations
				final String mRef = shortRef;
				gramSutXrefMap.putIfAbsent(mRef, new HashSet<>());
			} else if (refCh == 'p') {
				// Payo case, also add to Mogg
				final String pRef = shortRef;
				final int secondDotPos = sutLine.indexOf(".", dpos + 1);
				final String mRef = "m" + sutLine.substring(sutLine.indexOf("]") + 1, secondDotPos).trim();
				final Set<String> mRefSet = new HashSet<>(gramSutXrefMap.getOrDefault(mRef, new HashSet<>()));
				mRefSet.add(pRef);
				gramSutXrefMap.put(mRef, mRefSet);
				final Set<String> pRefSet = new HashSet<>();
				mRefSet.forEach(x -> {
					if (x.startsWith("n"))
						pRefSet.add(x);
				});
				pRefSet.add(mRef);
				gramSutXrefMap.put(pRef, pRefSet);
			} else if (refCh == 'n') {
				// Niru case, add Payo ref
				final String nRef = shortRef;
				final Set<String> nRefSet = new HashSet<>(gramSutXrefMap.getOrDefault(nRef, new HashSet<>()));
				final Set<String> payoSet = new HashSet<>();
				for (final String ref2Mogg : nRefSet) {
					final Set<String> mRefSet = gramSutXrefMap.getOrDefault(ref2Mogg, new HashSet<>());
					for (final String mRef : mRefSet) {
						if (mRef.startsWith("p"))
							payoSet.add(mRef);
					}
				}
				nRefSet.addAll(payoSet);
				gramSutXrefMap.put(nRef, nRefSet);
			}
		}
		// add Simple Xref for each grammar sutta	
		for (final GrammarSutta gsut : gramSutList) {
			gsut.addXref(GrammarSutta.RefType.SIMPLE, gramSutXrefMap.get(gsut.getShortRef()));
		}
		// add NiruXref for Niru's grammar suttas
		final Set<String> nXrefSet = new HashSet<>();
		for (final String line : gramSutNiruXrefList) {
			nXrefSet.clear();
			for (final GrammarSutta gsut : gramSutList) {
				final String ref = gsut.getShortRef();
				if (!ref.startsWith("n")) continue;
				if (line.startsWith(ref + ",")) {
					final String[] lineRefs = line.split(",");
					for (final String lref : lineRefs) {
						if (lref.startsWith("n") || lref.startsWith("m")) continue; // not include Niru and Mogg
						nXrefSet.add(lref);
					}
					gsut.addXref(GrammarSutta.RefType.NIRU, nXrefSet);
				}
			}
		}
	}

	public static void readReferenceList() {
		try {
			final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			final DocumentBuilder db = dbf.newDocumentBuilder();
			final InputStream in = ReaderUtilities.class.getResourceAsStream(REF_DATA);
			if (in == null) return;
			final Document doc = db.parse(in);
			final NodeList refs = doc.getElementsByTagName("ref");
			referenceList = new ArrayList<>();
			for (int i = 0; i < refs.getLength(); i++) {
				final Element ref = (Element) refs.item(i);
				final NodeList ids = ref.getElementsByTagName("id");
				final String refId = ids.getLength() > 0 ? getTextNodeContent((Element) ids.item(0)).trim() : "";
				final NodeList names = ref.getElementsByTagName("name");
				final String refName = names.getLength() > 0 ? getTextNodeContent((Element) names.item(0)).trim() : "";
				final Reference reference = new Reference(refId, refName);
				final NodeList altNames = ref.getElementsByTagName("altname");
				final String altName = altNames.getLength() > 0 ? getTextNodeContent((Element) altNames.item(0)).trim() : "";
				reference.setAltName(altName);
				final NodeList notes = ref.getElementsByTagName("note");
				final String note = notes.getLength() > 0 ? getTextNodeContent((Element) notes.item(0)).trim() : "";
				reference.setNote(note);
				final NodeList acads = ref.getElementsByTagName("acad");
				reference.setAcadRefList(getTextNodeContentList(acads));
				for (final Corpus.Collection col : Corpus.Collection.values) {
					final String tag = col.toString().toLowerCase();
					final NodeList colRefs = ref.getElementsByTagName(tag);
					final List<String> refList = getTextNodeContentList(colRefs);
					if (!refList.isEmpty()) {
						reference.addColRefList(col, refList);
					}
				}
				referenceList.add(reference);
			}
		} catch (ParserConfigurationException | SAXException | IOException e) {
			System.err.println(e);
		}
	}

	public static String getTextNodeContent(final Element elm) {
		final NodeList children = elm.getChildNodes();
		final StringBuilder sb = new StringBuilder();
		for (int i = 0; i < children.getLength(); i++) {
			final Node child = children.item(i);
			if (child.getNodeType() == Node.TEXT_NODE)
				sb.append(child.getNodeValue());
		}
		return sb.toString().trim();
	}

	public static List<String> getTextNodeContentList(final NodeList nlist) {
		final List<String> result = new ArrayList<>();
		final int len = nlist.getLength();
		if (len > 0) {
			for (int i = 0; i < len; i++) {
				final String content = getTextNodeContent((Element) nlist.item(i));
				if (!content.isEmpty())
					result.add(content);
			}
		}
		return result;
	}

	public static Stage openWindow(final Utilities.WindowType win, final Object[] args) {
		Stage stg = Utilities.getOpenedWindow(win.getWindowClassName());
		final String strToLocate = args != null && args.length > 1 ? (String)args[1] : "";
		switch (win) {
			case TOCTREE:
				if (stg == null) {
					stg = Utilities.openNewWindow(new TocTreeWin(), 
							new Image(ReaderUtilities.class.getResourceAsStream("resources/images/folder-tree.png")), "TOC Tree");
				} else {
					Utilities.showExistingWindow(stg);
				}
				break;
			case FINDER:
				if (stg == null) {
					stg = Utilities.openNewWindow(new DocumentFinder(), 
							new Image(ReaderUtilities.class.getResourceAsStream("resources/images/binoculars.png")), "Document Finder");
				} else {
					Utilities.showExistingWindow(stg);
				}
				break;
			case VIEWER:
			case VIEWER_CSTR:
			case VIEWER_CST4:
			case VIEWER_BJT:
			case VIEWER_GRETIL:
			case VIEWER_SRT:
			case VIEWER_GRAM:
				if (args == null) break;
				final TocTreeNode node = (TocTreeNode)args[0];
				final PaliHtmlViewer viewer;
				final Corpus.Collection nodeColl = node.getCorpus().getCollection();
				if (stg == null) {
						if (nodeColl == Corpus.Collection.CSTR)
							viewer = new CstrHtmlViewer(node, strToLocate);
						else if (nodeColl == Corpus.Collection.CSTDEVA || nodeColl == Corpus.Collection.CST4)
							viewer = new Cst4HtmlViewer(node, strToLocate);
						else if (nodeColl == Corpus.Collection.BJT)
							viewer = new BjtHtmlViewer(node, strToLocate);
						else if (nodeColl == Corpus.Collection.PTST)
							viewer = new GretilHtmlViewer(node, strToLocate);
						else if (nodeColl == Corpus.Collection.SRT)
							viewer = new SrtHtmlViewer(node, strToLocate);
						else if (nodeColl == Corpus.Collection.GRAM)
							viewer = new GramHtmlViewer(node, strToLocate);
						else
							viewer = new PaliHtmlViewer(node);
						stg = Utilities.openNewWindow(viewer, 
							new Image(ReaderUtilities.class.getResourceAsStream("resources/images/file-lines.png")),
							node.getNodeName());
						viewer.setStage(stg);
				} else {
						if (nodeColl == Corpus.Collection.CSTR)
							viewer = (CstrHtmlViewer)stg.getScene().getRoot();
						else if (nodeColl == Corpus.Collection.CSTDEVA || nodeColl == Corpus.Collection.CST4)
							viewer = (Cst4HtmlViewer)stg.getScene().getRoot();
						else if (nodeColl == Corpus.Collection.BJT)
							viewer = (BjtHtmlViewer)stg.getScene().getRoot();
						else if (nodeColl == Corpus.Collection.PTST)
							viewer = (GretilHtmlViewer)stg.getScene().getRoot();
						else if (nodeColl == Corpus.Collection.SRT)
							viewer = (SrtHtmlViewer)stg.getScene().getRoot();
						else if (nodeColl == Corpus.Collection.GRAM)
							viewer = (GramHtmlViewer)stg.getScene().getRoot();
						else
							viewer = (PaliHtmlViewer)stg.getScene().getRoot();
						if (nodeColl == Corpus.Collection.CSTR)
							((CstrHtmlViewer)viewer).init(node, strToLocate);
						else if (nodeColl == Corpus.Collection.CST4)
							((Cst4HtmlViewer)viewer).init(node, strToLocate);
						else
							viewer.init(node, strToLocate);
					Utilities.showExistingWindow(stg);
				}
				break;
			case VIEWER_SC:
				final String docId = args == null ? "" : (String)args[0];
				if (stg == null) {
					final ScReader reader = new ScReader(docId, strToLocate); 
					stg = Utilities.openNewWindow(reader,
							new Image(ReaderUtilities.class.getResourceAsStream("resources/images/sc.png")), "SuttaCentral Text Reader");
					reader.setStage(stg);
				} else {
					final ScReader reader = (ScReader)stg.getScene().getRoot();
					reader.init(docId, strToLocate);
					Utilities.showExistingWindow(stg);
				}
				break;
			case VIEWER_SKTGRETIL:
				final String sktDocId = args == null ? "" : (String)args[0];
				if (stg == null) {
					final SktGretilHtmlViewer sgViewer = new SktGretilHtmlViewer(sktDocId, strToLocate); 
					stg = Utilities.openNewWindow(sgViewer,
							new Image(ReaderUtilities.class.getResourceAsStream("resources/images/skt-scroll.png")), "GRETIL Sanskrit Documents");
					sgViewer.setStage(stg);
				} else {
					final SktGretilHtmlViewer sgViewer = (SktGretilHtmlViewer)stg.getScene().getRoot();
					sgViewer.init(sktDocId, strToLocate);
					Utilities.showExistingWindow(stg);
				}
				break;
		}
		return stg;
	}

	public static Stage openPaliHtmlViewer(final TocTreeNode node) {
		final Object[] args = { node };
		return openViewer(node.getCorpus().getCollection(), args);
	}

	public static Stage openPaliHtmlViewer(final TocTreeNode node, final String strToLocate) {
		final Object[] args = { node, strToLocate };
		return openViewer(node.getCorpus().getCollection(), args);
	}

	public static Stage openOtherReader(final Corpus.Collection col, final DocumentInfo dinfo) {
		final String docId = dinfo.getId();
		final Object[] args = { docId };
		return openViewer(col, args);
	}

	public static Stage openOtherReader(final Corpus.Collection col, final DocumentInfo dinfo, final String strToLocate) {
		final String docId = dinfo.getId();
		final Object[] args = { docId, strToLocate };
		return openViewer(col, args);
	}

	private static Stage openViewer(final Corpus.Collection nodeColl, final Object[] args) {
		Utilities.WindowType type = Utilities.WindowType.VIEWER;
		switch (nodeColl) {
			case CSTR: 
				type = Utilities.WindowType.VIEWER_CSTR; break;
			case CSTDEVA: 
				type = Utilities.WindowType.VIEWER_CST4; break;
			case CST4: 
				type = Utilities.WindowType.VIEWER_CST4; break;
			case BJT:
				type = Utilities.WindowType.VIEWER_BJT; break;
			case PTST: 
				type = Utilities.WindowType.VIEWER_GRETIL; break;
			case SRT:
				type = Utilities.WindowType.VIEWER_SRT; break;
			case GRAM:
				type = Utilities.WindowType.VIEWER_GRAM; break;
			case SC:
				type = Utilities.WindowType.VIEWER_SC; break;
			case SKT:
				type = Utilities.WindowType.VIEWER_SKTGRETIL; break;
		}
		return openWindow(type, args);
	}

	public static String readGz(final File gzfile, final Charset charset) {
		final StringBuilder result = new StringBuilder();
		try (final Scanner in = new Scanner(new GZIPInputStream(new FileInputStream(gzfile)), charset)) {
			while (in.hasNextLine()) {
				result.append(in.nextLine()).append("\n");
			}
		} catch (IOException e) {
			System.err.println(e);
		}
		return result.toString();
	}

	public static String readGzHTMLBody(final File gzfile, final Charset charset) {
		return "<body>" + readGz(gzfile, charset) + "</body>";
	}

	/**
	 * Reads an HTML file from a collection's zip.
	 * The output is just the body part ({@literal<body> ... </body>}), hence, not complete HTML.
	 */
	public static String readHTMLBodyFromZip(final String filename, final Corpus corpus) {
		final StringBuilder result = new StringBuilder();
		try {
			final ZipFile zip = new ZipFile(corpus.getZipFile());
			final ZipEntry entry = zip.getEntry(filename);
			if (entry != null) {
				result.append("<body>").append("\n");
				final Scanner in = new Scanner(zip.getInputStream(entry), corpus.getEncoding().getCharset());
				boolean isBody = false;
				while (in.hasNextLine()) {
					final String line = in.nextLine();
					if (isBody) {
						if (line.toLowerCase().contains("</body>"))
							break;
						result.append(line);
					} else {
						isBody = line.toLowerCase().contains("<body>");
					}
				}
				result.append("\n").append("</body>");
				in.close();
			} else {
				zip.close();
				return "";
			}
			zip.close();
		} catch (IOException e) {
			System.err.println(e);
		}
		return result.toString();
	}
	
	public static String readTextFromZip(final String filename, final Corpus corpus) {
		final StringBuilder result = new StringBuilder();
		try {
			final ZipFile zip = new ZipFile(corpus.getZipFile());
			final ZipEntry entry = zip.getEntry(filename);
			if (entry != null) {
				final Scanner in = new Scanner(zip.getInputStream(entry), corpus.getEncoding().getCharset());
				while (in.hasNextLine()) {
					final String line = in.nextLine();
					result.append(line).append("\n");
				}
				in.close();
			} else {
				zip.close();
				return "";
			}
			zip.close();
		} catch (IOException e) {
			System.err.println(e);
		}
		return result.toString();
	}

	/**
	 * Reads an XML file from CST Devanagari collection, then transforms the content into HTML by XSLT.
	 * According to the stylesheet given, the output is wrapped with {@literal<body> ... </body>},
	 * hence, not complete HTML.
	 */
	public static String readCstDevaXML(final TocTreeNode node) {
		final Corpus corpus = node.getCorpus();
		org.w3c.dom.Document domDoc = null;
		final DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		final StringWriter writer = new StringWriter();
		try {
			final DocumentBuilder builder = docFactory.newDocumentBuilder();
			final File xmlFile = new File(Utilities.ROOTDIR + ReaderUtilities.TEXTPATH + corpus.getRootName() + File.separator + node.getNodeFileName());
			if (xmlFile.exists()) {
				domDoc = builder.parse(Files.newInputStream(xmlFile.toPath(), StandardOpenOption.READ));
			} else {
				return "";
			}
			if (domDoc == null) return "";
			// read DOM and transform with XSLT
			final InputStream stylesheet = ReaderUtilities.class.getResourceAsStream(CST4_XSL);
			final TransformerFactory tFactory = TransformerFactory.newInstance();
			final StreamSource stylesource = new StreamSource(stylesheet);
			final Transformer transformer = tFactory.newTransformer(stylesource);
			final DOMSource source = new DOMSource(domDoc);
			final StreamResult result = new StreamResult(writer);
			transformer.transform(source, result);
			writer.flush();
			writer.close();
		} catch (TransformerConfigurationException tce) {
			// Error generated by the parser
			System.err.println("\n** Transformer Factory error");
			System.err.println("   " + tce.getMessage());
			// Use the contained exception, if any
			if (tce.getException() != null) {
				final Throwable x = tce.getException();
				System.err.println(x);
			}
			System.err.println(tce);
		} catch (TransformerException te) {
			// Error generated by the parser
			System.err.println("\n** Transformation error");
			System.err.println("   " + te.getMessage());
			// Use the contained exception, if any
			if (te.getException() != null) {
				final Throwable x = te.getException();
				System.err.println(x);
			}
			System.err.println(te);
		} catch (SAXException sxe) {
			// Error generated by this application
			// (or a parser-initialization error)
			if (sxe.getException() != null) {
				final Exception x = sxe.getException();
				System.err.println(x);
			}
			System.err.println(sxe);
		} catch (ParserConfigurationException | IOException e) {
			// Parser with specified options can't be built
			System.err.println(e);
		}
		return writer.toString();
	}

	/**
	 * Reads an XML file from CST4 collection zip, then transforms the content into HTML by XSLT.
	 * According to the stylesheet given, the output is wrapped with {@literal<body> ... </body>},
	 * hence, not complete HTML.
	 */
	public static String readCst4XML(final TocTreeNode node) {
		final Corpus corpus = node.getCorpus();
		org.w3c.dom.Document domDoc = null;
		final DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		final StringWriter writer = new StringWriter();
		try {
			final DocumentBuilder builder = docFactory.newDocumentBuilder();
			final File zipfile = corpus.getZipFile();
			final ZipFile zip = new ZipFile(zipfile);
			final String zipfilename = zipfile.getName();
			final ZipEntry entry = zip.getEntry(node.getNodeFileName());
			if (entry != null) {
				domDoc = builder.parse(zip.getInputStream(entry));
			} else {
				zip.close();
				return "";
			}
			zip.close();
			if (domDoc == null) return "";
			// read DOM and transform with XSLT
			final InputStream stylesheet = ReaderUtilities.class.getResourceAsStream(CST4_XSL);
			final TransformerFactory tFactory = TransformerFactory.newInstance();
			final StreamSource stylesource = new StreamSource(stylesheet);
			final Transformer transformer = tFactory.newTransformer(stylesource);
			final DOMSource source = new DOMSource(domDoc);
			final StreamResult result = new StreamResult(writer);
			transformer.transform(source, result);
			writer.flush();
			writer.close();
		} catch (TransformerConfigurationException tce) {
			// Error generated by the parser
			System.err.println("\n** Transformer Factory error");
			System.err.println("   " + tce.getMessage());
			// Use the contained exception, if any
			if (tce.getException() != null) {
				final Throwable x = tce.getException();
				System.err.println(x);
			}
			System.err.println(tce);
		} catch (TransformerException te) {
			// Error generated by the parser
			System.err.println("\n** Transformation error");
			System.err.println("   " + te.getMessage());
			// Use the contained exception, if any
			if (te.getException() != null) {
				final Throwable x = te.getException();
				System.err.println(x);
			}
			System.err.println(te);
		} catch (SAXException sxe) {
			// Error generated by this application
			// (or a parser-initialization error)
			if (sxe.getException() != null) {
				final Exception x = sxe.getException();
				System.err.println(x);
			}
			System.err.println(sxe);
		} catch (ParserConfigurationException | IOException e) {
			// Parser with specified options can't be built
			System.err.println(e);
		}
		return writer.toString();
	}

	static boolean checkIfSuttaCentralAvailable() {
		final boolean result = scData.exists();
		suttaCentralAvailable.set(result);
		return result;
	}
	
	static boolean checkIfSktGretilAvailable() {
		final boolean result = sktGretilData.exists();
		sktGretilAvailable.set(result);
		return result;
	}
	
	public static String getScHeads() {
		final StringBuilder result = new StringBuilder();
		final Pattern headPatt = Pattern.compile(": \"(.*?)\",");
		try {
			final ZipFile zip = new ZipFile(scData);
			for (final Enumeration<? extends ZipEntry> e = zip.entries(); e.hasMoreElements();) {
				final ZipEntry entry = e.nextElement();
				final String fname = entry.getName();
				if (fname.endsWith(ScDocument.ROOT_PLI_MS_END) && fname.indexOf("playground") == -1) {
					final String id = fname.substring(fname.lastIndexOf("/") + 1, fname.lastIndexOf(ScDocument.ROOT_PLI_MS_END));
					final Scanner in = new Scanner(zip.getInputStream(entry), StandardCharsets.UTF_8);
					String text = "";
					while (in.hasNextLine()) {
						final String line = in.nextLine().trim();
						if (line.indexOf(":0.") > -1) {
							final Matcher headMatcher = headPatt.matcher(line);
							if (headMatcher.find()) {
								final String head = headMatcher.group(1).trim();
								if (!head.startsWith("Namo tassa") && !head.startsWith("Ime kho") && !head.equals("~"))
									text = text + head + ";";
							}
						}
					}
					in.close();
					result.append(id).append(":").append(text).append(LINESEP);
				}
			}
			zip.close();
		} catch (IOException e) {
			System.err.println(e);
		}
		return result.toString();
	}

	public static void createScHeads() {
		Utilities.saveText(getScHeads(), scHeadFile);
	}

	public static Map<String, String> readJsonObject(final InputStream in) throws IOException {
		final Map<String, String> result = new LinkedHashMap<>();
		final JsonReader reader = new JsonReader(new InputStreamReader(in, StandardCharsets.UTF_8));
		try {
			reader.beginObject();
			while (reader.hasNext()) {
				final String name = reader.nextName();
				final String text = reader.nextString().replaceAll("</?a.*?>", "");
				result.put(name, text);
			}
			reader.endObject();
		} finally {
			reader.close();
		}
		return result;
	}

	public static Map<String, String> getScData(final String name) {
		Map<String, String> result = null;
		try {
			final ZipFile zip = new ZipFile(scData);
			final ZipEntry entry = zip.getEntry(name);
			if (entry != null)
				result = readJsonObject(zip.getInputStream(entry));
			zip.close();
		} catch (IOException e) {
			System.err.println(e);
		}
		return result == null ? Collections.emptyMap() : result;
	}

	public static Map<String, Set<String>> getScAuthLang(final String id, final String type) {
		final Map<String, Set<String>> result = new HashMap<>();
		final Set<String> langs = new HashSet<>();
		final Set<String> authors = new HashSet<>();
		final Pattern patt = Pattern.compile(id + "_" + type + "-(\\w{2,3})-(.*?)\\.json$");
		try {
			final ZipFile zip = new ZipFile(scData);
			for (final Enumeration<? extends ZipEntry> e = zip.entries(); e.hasMoreElements();) {
				final ZipEntry entry = e.nextElement();
				final String fname = entry.getName();
				final Matcher matcher = patt.matcher(fname);
				if (matcher.find() && !fname.contains("/bilara-comments/")) {
					langs.add(matcher.group(1));
					authors.add(matcher.group(2));
				}
			}
			zip.close();
		} catch (IOException e) {
			System.err.println(e);
		}
		result.put("lang", langs);
		result.put("auth", authors);
		return result;
	}

	private static List<BjtPage> readBjtPages(final JsonReader reader) throws IOException {
		final List<BjtPage> result = new ArrayList<>();
		reader.beginArray();
		while (reader.hasNext()) {
			reader.beginObject();
			BjtPage page = null;
			while (reader.hasNext()) {
				final String pageProp = reader.nextName();
				if (pageProp.equalsIgnoreCase("pagenum")) {
					final int num = reader.nextInt();
					page = new BjtPage(num);
				} else if (pageProp.equals("pali") && page != null) {
					reader.beginObject();
					while (reader.hasNext()) {
						final String paliProp = reader.nextName();
						if (paliProp.equals("entries")) {
							readBjtPageEntries(reader, page);
						} else {
							reader.skipValue();
						}
					}
					reader.endObject();
				} else {
					reader.skipValue();
				}
			}
			if (page != null)
				result.add(page);
			reader.endObject();
		}
		reader.endArray();
		return result;
	}

	private static void readBjtPageEntries(final JsonReader reader, final BjtPage page) throws IOException {
		reader.beginArray();
		while (reader.hasNext()) {
			reader.beginObject();
			String type = "";
			String text = "";
			int level = 0;
			while (reader.hasNext()) {
				final String name = reader.nextName();
				if (name.equals("type")) {
					type = reader.nextString().toLowerCase();
				} else if (name.equals("text")) {
					text = reader.nextString();
				} else if (name.equals("level")) {
					level = reader.nextInt();
				} else {
					reader.skipValue();
				}
			}
			if (!type.isEmpty())
				page.addEntry(type, text, level);
			reader.endObject();
		}
		reader.endArray();
	}

	public static List<BjtPage> getBjtPages(final InputStream input) throws IOException {
		List<BjtPage> result = null;
		final JsonReader reader = new JsonReader(new InputStreamReader(input, StandardCharsets.UTF_8));
		try {
			reader.beginObject();
			while (reader.hasNext()) {
				final String name = reader.nextName();
				if (name.equals("pages")) {
					result = readBjtPages(reader);
				} else {
					reader.skipValue();
				}
			}
			reader.endObject();
		} finally {
			reader.close();
		}
		return result == null ? Collections.emptyList() : result;
	}
	
	public static List<BjtPage> getBjtPages(final String fname) {
		List<BjtPage> result = null;
		try {
			final ZipFile zip = new ZipFile(corpusMap.get(Corpus.Collection.BJT).getZipFile());
			final ZipEntry entry = zip.getEntry(fname);
			if (entry != null)
				result = getBjtPages(zip.getInputStream(entry));
			zip.close();
		} catch (IOException e) {
			System.err.println(e);
		}
		return result == null ? Collections.emptyList() : result;
	}

	public static String makeHTML(final String textBody, final String jsBody) {
		final StringBuilder scriptBody = new StringBuilder();
		scriptBody.append(Utilities.getTextResource(Utilities.COMMON_JS));
		scriptBody.append(getStringResource(PALI_JS));
		scriptBody.append(jsBody);
		final String jsScript = "<script type='text/javascript'>" + scriptBody.toString() + "</script>";
		final StringBuilder htmlText = new StringBuilder(textBody);
		htmlText.insert(0, "<!doctype html><html><head><meta charset='utf-8'>" + jsScript + "</head>");
		htmlText.append("</html>");
		return htmlText.toString();
	}

	public static String makeHTML(final String body) {
		final StringBuilder scriptBody = new StringBuilder();
		scriptBody.append(Utilities.getTextResource(Utilities.COMMON_JS));
		scriptBody.append(getStringResource(PALI_JS));
		final String jsScript = "<script type='text/javascript'>" + scriptBody.toString() + "</script>";
		final StringBuilder htmlText = new StringBuilder(body);
		htmlText.insert(0, "<!doctype html><html><head><meta charset='utf-8'>" + jsScript + "</head>");
		htmlText.append("</html>");
		return htmlText.toString();
	}

}

