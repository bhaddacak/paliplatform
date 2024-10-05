/*
 * Cst4SAXHandler.java
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

import paliplatform.reader.*;

import java.util.*;
import org.xml.sax.*;
import org.xml.sax.helpers.DefaultHandler;

/** 
 * This handler reads XML data in CST collections for tokenizing.
 * @author J.R. Bhaddacak
 * @version 3.0
 * @since 2.0
 */
 
class Cst4SAXHandler extends DefaultHandler {
	private final Map<TermInfo.Field, StringBuilder> textMap;
	private final Deque<String> openedTags = new ArrayDeque<>();
	private StringBuilder textBufferP = new StringBuilder();
	private String textBufferB = "";
	private String textBufferN = "";

	public Cst4SAXHandler(final Map<TermInfo.Field, StringBuilder> textMap) {
		this.textMap = textMap;
	}

	@Override
	public void startDocument() throws SAXException {
	}

	@Override
	public void endDocument() throws SAXException {
	}

	@Override
	public void startElement(final String uri, final String localName, final String qName, final Attributes attributes) throws SAXException {
		final String thisTag = qName;
		if (thisTag.equals("p") || thisTag.equals("head") || thisTag.equals("trailer")) {
			if (attributes.getQName(0).equals("rend")) {
				openedTags.push(attributes.getValue(0));
				textBufferP = new StringBuilder();
			}
		} else if (thisTag.equals("hi")) {
			if (attributes.getQName(0).equals("rend") && attributes.getValue(0).equals("bold")) {
				openedTags.push("bold");
				textBufferB = "";
			}
		} else if (thisTag.equals("note")) {
			openedTags.push("note");
			textBufferN = "";
		}
	}

	@Override
	public void endElement(final String uri, final String localName, final String qName) throws SAXException {
		final String thisTag = qName;
		String thisField = "";
		String strPortion = "";
		if (thisTag.equals("p") || thisTag.equals("head") || thisTag.equals("trailer")) {
			thisField = openedTags.pop();
			strPortion = textBufferP.toString();
		} else if (thisTag.equals("hi") && openedTags.peek().equals("bold")) {
			thisField = openedTags.pop();
			strPortion = textBufferB;
		} else if (thisTag.equals("note")) {
			thisField = openedTags.pop();
			strPortion = textBufferN;
		}
		if (thisField.length() > 0) {
			final String tag = thisField.toUpperCase();
			if (TermInfo.Field.isValid(Corpus.Collection.CST4, tag)) {
				textMap.get(TermInfo.Field.valueOf(tag)).append(strPortion);
				if (qName.equals("p") || qName.equals("head") || qName.equals("trailer") || qName.equals("hi"))
					textMap.get(TermInfo.Field.valueOf(tag)).append("\n");
			}
		}
	}

	@Override
	public void characters(final char[] ch, final int start, final int length) throws SAXException {
		if (!openedTags.isEmpty()) {
			final String currTag = openedTags.peek();
			final String str = " " + new String(ch, start, length) + " ";
			if (currTag.equals("note")) {
				textBufferN = str;
			} else if (currTag.equals("bold")) {
				textBufferB = str;
				textBufferP.append(str);
			} else {
				textBufferP.append(str);
			}
		}
	}

	@Override
	public void error(final SAXParseException e) throws SAXException{
		System.err.println("SAX Error: " + e.getMessage());
	}

	@Override
	public void fatalError(final SAXParseException e) throws SAXException{
		System.err.println("SAX Fatal Error: " + e.getMessage());
	}

	@Override
	public void warning(final SAXParseException e) throws SAXException{
		System.err.println("SAX Warning: " + e.getMessage());
	}

}
