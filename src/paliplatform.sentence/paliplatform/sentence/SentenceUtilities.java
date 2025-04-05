/*
 * SentenceUtilities.java
 *
 * Copyright (C) 2023-2025 J. R. Bhaddacak 
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

package paliplatform.sentence;

import paliplatform.base.*;

import java.util.*;
import java.util.stream.*;
import java.util.function.Function;
import java.util.ServiceLoader.Provider;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import javafx.stage.Stage;
import javafx.scene.image.Image;

/** 
 * The utility factory for the Sentence module.
 * @author J.R. Bhaddacak
 * @version 3.0
 * @since 3.0
 */
final public class SentenceUtilities {
	public static final String SENTENCESPATH = Utilities.DATAPATH + "sentences" + File.separator;
	public static final String SENTENCESMAIN = "main" + File.separator;
	public static Map<String, SimpleService> simpleServiceMap;

	static String getTextResource(final String filename) {
		String result = "";
		try {
			final InputStream in = SentenceUtilities.class.getResourceAsStream("resources/text/" + filename);
			if (in != null)
				result = new String(in.readAllBytes(), StandardCharsets.UTF_8);
		} catch (IOException e) {
			System.err.println(e);
		}
		return result;
	}
    
	public static Map<String, SimpleService> getSimpleServices() {
		return ServiceLoader.load(SimpleService.class)
				.stream()
				.map(Provider::get)
				.collect(Collectors.toMap(x -> x.getClass().getName(), Function.identity()));
	}

	public static void openWindow(final Utilities.WindowType win, final Object[] args) {
		Stage stg = Utilities.getOpenedWindow(win.getWindowClassName());
		switch (win) {
			case READER:
				if (stg == null) {
					final SentenceReader reader = new SentenceReader(args);
					stg = Utilities.openNewWindow(reader,
							new Image(SentenceUtilities.class.getResourceAsStream("resources/images/book-open.png")), SentenceReader.TITLE);
					reader.setStage(stg);
				} else {
					final SentenceReader reader = (SentenceReader)stg.getScene().getRoot();
					reader.init(args);
					Utilities.showExistingWindow(stg);
				}
				break;
		}
	}

	public static String MD5Sum(final String text) {
		final StringBuilder result = new StringBuilder();
		try {
			final MessageDigest md = MessageDigest.getInstance("MD5");
			final byte[] dg = md.digest(text.getBytes("UTF-8"));
			for (final byte b : dg)
				result.append(String.format("%x", b));
		} catch (UnsupportedEncodingException | NoSuchAlgorithmException e) {
			System.err.println(e);
		}
		return result.toString();
	}

}

