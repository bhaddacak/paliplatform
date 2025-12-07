/*
 * SanskritUtilities.java
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

package paliplatform.sanskrit;

import paliplatform.base.*;

import java.util.*;
//~ import java.util.stream.*;
//~ import java.util.ServiceLoader.Provider;
import java.io.InputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/** 
 * The utility factory for the Sanskrit module.
 * @author J.R. Bhaddacak
 * @version 3.5
 * @since 3.5
 */
final public class SanskritUtilities {
	private static final String TXTDIR = "resources/text/";

	static String getTextResource(final String filename) {
		String result = "";
		try {
			final InputStream in = SanskritUtilities.class.getResourceAsStream(TXTDIR + filename);
			if (in != null)
				result = new String(in.readAllBytes(), StandardCharsets.UTF_8);
		} catch (IOException e) {
			System.err.println(e);
		}
		return result;
	}

}

