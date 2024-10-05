/*
 * LuceneIndex.java
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

package paliplatform.lucene;

import paliplatform.base.*;
import paliplatform.reader.*;

import java.util.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.Directory;

/** 
 * The representation of a Lucene index instance.
 * @author J.R. Bhaddacak
 * @version 3.0
 * @since 3.0
 */
class LuceneIndex {
	static enum OpenMode { READ, WRITE }
	private final File indexFile;
	private final String indexPath;
	private final String indexName;
	private final Corpus.Collection collection;
	private String textGroupStr = "";
	private boolean boldAvailable;
	private boolean numberAvailable;
	private final String listerTableName;
	
	public LuceneIndex(final String pathName) {
		indexPath = LuceneUtilities.INDEXROOT + pathName;
		indexFile = new File(indexPath);
		indexName = pathName;
		final String[] names = indexName.split("-");
		if (names[0].isEmpty()) {
			collection = null;
			boldAvailable = false;
			numberAvailable = false;
			listerTableName = "";
		} else {
			collection = Corpus.Collection.valueOf(names[0].toUpperCase());
			String ltName = names[0];
			if (names.length > 1) {
				textGroupStr = names[1];
				ltName = ltName + "_" + names[1].replace(".", "");
			}
			if (names.length > 2) {
				boldAvailable = names[2].indexOf('b') > -1;
				numberAvailable = names[2].indexOf('n') > -1;
				ltName = ltName + "_" + names[2];
			}
			listerTableName = ltName.toUpperCase();
		}
	}

	public static String getIndexPath(final String dirName) {
		return LuceneUtilities.INDEXROOT + dirName;
	}

	public static void createIndexDir(final File indexDir) {
		indexDir.mkdir();
	}

	public void clearIndexDir() {
		for (final File f : indexFile.listFiles()) {
			f.delete();
		}
	}

	public void deleteIndexDir() {
		clearIndexDir();
		indexFile.delete();
	}

	public String getIndexPath() {
		return indexPath;
	}

	public String getIndexName() {
		return indexName;
	}

	public Corpus.Collection getCollection() {
		return collection;
	}

	public String getTextGroupStr() {
		return textGroupStr;
	}

	public boolean isAvailable() {
		boolean result = false;
		if (!indexDirExists()) return result;
		final String[] files = indexFile.list((f, s) -> { return s.startsWith("segments"); });
		if (files.length > 0)
			result = true;
		return result;
	}

	public boolean isBoldAvailable() {
		return boldAvailable;
	}

	public boolean isNumberAvailable() {
		return numberAvailable;
	}

	private boolean indexDirExists() {
		return indexFile.exists();
	}

	public String getListerTableName() {
		return listerTableName;
	}

	public int getNumDocs() {
		int docCount = 0;
		try {
			final Directory directory = FSDirectory.open(Path.of(indexPath));
			final DirectoryReader ireader = DirectoryReader.open(directory);
			if (ireader != null) {
				docCount = ireader.numDocs();
				ireader.close();
			}
			directory.close();
		} catch (IOException e) {
			System.err.println(e);
		}
		return docCount;
	}

	public String getIndexInfo() {
		final Corpus corpus = ReaderUtilities.corpusMap.get(collection);
		if (corpus == null) return "";
		final String corpusName = corpus.getShortName();
		final String textGroupStr = getTextGroupStr();
		final String withNum = isNumberAvailable() ? "number" : "";
		final String withBold = isBoldAvailable() ? "bold" : "";
		final String andSign = !withNum.isEmpty() && !withBold.isEmpty() ? " & " : "";
		final String numBoldStr = !withNum.isEmpty() || !withBold.isEmpty() ? " including " + withNum + andSign + withBold : "";
		final String info = corpusName + numBoldStr + " [" + textGroupStr;
		final String docNumStr = isAvailable() ? " = " + getNumDocs() + " docs]" : "]";
		return info + docNumStr;
	}

	public static LuceneIndex getIndexByListerTableName(final String tabName) {
		return LuceneUtilities.indexMap.values().stream()
					.filter(x -> x.getListerTableName().equals(tabName))
					.findFirst()
					.orElse(null);
	}

	public static Comparator<String> getIndexNameComparator(final boolean forListerTable) {
		final String sep = forListerTable ? "_" : "-";
		return new Comparator<String>() {
			@Override
			public int compare(final String aName, final String bName) {
				// typical name is corpus-group-flag (index), CORPUS_GROUP_FLAG (lister table)
				final String[] aNames = aName.toLowerCase().split(sep);
				final String aCorpus = aNames[0];
				final String aGroup = aNames.length > 1 ? aNames[1] : "";
				final String aFlag = aNames.length > 2 ? aNames[2] : "";
				final String[] bNames = bName.toLowerCase().split(sep);
				final String bCorpus = bNames[0];
				final String bGroup = bNames.length > 1 ? bNames[1] : "";
				final String bFlag = bNames.length > 2 ? bNames[2] : "";
				int result = Corpus.Collection.colComparator.compare(aCorpus, bCorpus);
				if (result == 0) {
					final boolean aPredef = LuceneUtilities.predefTextGroup.containsKey(aGroup);
					final boolean bPredef = LuceneUtilities.predefTextGroup.containsKey(bGroup);
					if (aPredef || bPredef)
						result = LuceneUtilities.predefTextGroupComparator.compare(aGroup, bGroup);
					else
						result = LuceneUtilities.textGroupCollator.compare(aGroup, bGroup);
					if (result == 0)
						result = LuceneUtilities.textGroupCollator.compare(aFlag, bFlag);
				}
				return result;
			}
		};
	}

	@Override
	public String toString() {
		return indexName;
	}

}
