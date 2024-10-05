/*
 * SimpleTocTreeNode.java
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

import paliplatform.base.Utilities;

import java.io.File;

/** 
 * A simple implementation of TOC tree node.
 * @author J.R. Bhaddacak
 * @version 3.0
 * @since 3.0
 */
 
public class SimpleTocTreeNode implements TocTreeNode {
	private Corpus corpus;
	private String nodeId;
	private String nodeName;
	private String nodeFileName;
	private File nodeFile; // if in archive, this is the zip file
	private NodeType nodeType = NodeType.BRANCH;

	private SimpleTocTreeNode() {
	}

	/**
	 * Used for the root of corpus nodes.
	 */
	public SimpleTocTreeNode(final Corpus corpus) {
		this(corpus, corpus.getRootName(), corpus.getName(), corpus.getInfoFileName());
		nodeType = NodeType.CORPUS;
	}

	/**
	 * Used for general nodes.
	 */
	public SimpleTocTreeNode(final Corpus corpus, final String id, final String name, final String filename) {
		this.corpus = corpus;
		nodeId = id;
		nodeName = name;
		nodeFileName = filename;
		if (corpus.isInArchive())
			nodeFile = corpus.getZipFile(); 
		else
			nodeFile = new File(Utilities.ROOTDIR + ReaderUtilities.TEXTPATH + corpus.getRootName() + File.separator, filename);
		nodeType = filename.isEmpty() ? NodeType.BRANCH : NodeType.TEXT;
	}

	@Override
	public Corpus getCorpus() {
		return corpus;
	}

	@Override
	public String getNodeId() {
		return nodeId;
	}

	@Override
	public String getNodeName() {
		return nodeName;
	}

	@Override
	public String getNodeFileName() {
		return nodeFileName;
	}

	@Override
	public File getNodeFile() {
		return nodeFile;
	}

	@Override
	public void setNodeType(final NodeType type) {
		nodeType = type;
	}

	@Override
	public NodeType getNodeType() {
		return nodeType;
	}

	@Override
	public boolean isCorpusNode() {
		return nodeType == NodeType.CORPUS;
	}

	@Override
	public void setIsText(final boolean yn) {
		nodeType = yn == true ? NodeType.TEXT : NodeType.BRANCH;
	}

	@Override
	public boolean isTextNode() {
		return nodeType == NodeType.TEXT;
	}

	@Override
	public boolean isInArchive() {
		return corpus.isInArchive();
	}

	@Override
	public String toString() {
		return nodeName;
	}

	@Override
	public String toStringFull() {
		final String str = nodeFileName.isEmpty() ? nodeName : nodeName + " [" + nodeFileName +"]";
		return str;
	}
	
}

