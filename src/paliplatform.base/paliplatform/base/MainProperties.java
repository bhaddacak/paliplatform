/*
 * MainProperties.java
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

package paliplatform.base;

import java.util.*;
import java.io.*;
import java.util.Properties;

/** 
 * This manages the program's properties.
 * This class is a singleton.
 * @author J.R. Bhaddacak
 * @version 3.7
 * @since 2.0
 */
public class MainProperties {
	public static final MainProperties INSTANCE = new MainProperties();
	private static final String PROPERTIES_FILE = "PaliPlatform3.properties";
	private static final double DEFAULT_WIDTH = 890;
	private static final double DEFAULT_HEIGHT = 500;
	private final Map<String, String> defPropMap = new HashMap<>();
	private final Properties settings;
	private final File propertiesFile;
	private final String[][] defProperties = {
		{ "latest-patch", "" },
		{ "width", "" + DEFAULT_WIDTH },
		{ "height", "" + DEFAULT_HEIGHT },
		{ "theme", "LIGHT" },
		{ "iconsize", "NORMAL" },
		{ "bgstyle", "GRAY" },
		{ "lineheight", "120%" },
		{ "viewer-fontsize", "100" },
		{ "dict-fontsize", "100" },
		{ "other-fontsize", "100" },
		{ "exit-ask", "true" },
		{ "editor-close-ask", "true" },
		{ "pali-input-method", PaliTextInput.InputMethod.UNUSED_CHARS.toString() },
		{ "sanskrit-input-method", PaliTextInput.InputMethod.COMPOSITE.toString() },
		{ "dictset", "CPED," },
		{ "sktdictset", "" },
		{ "dpd-decon-count", "850000" },
		{ "dpd-dict-count", "420000" },
		{ "dpd-head-count", "80000" },
		{ "dpd-lookup-enable", "true" },
		{ "sentence-normalize", "true" },
		{ "sentence-use-cap", "true" },
		{ "sentence-use-bar", "true" },
		{ "sentence-use-dot", "true" },
		{ "sentence-use-colon", "true" },
		{ "sentence-use-semicolon", "true" },
		{ "sentence-use-dash", "true" },
		{ "roman-translit", "dr" },
		{ "font-roman", Utilities.FONT_ROMAN_DEFAULT },
		{ "font-devanagari", Utilities.FONT_DEVA_DEFAULT },
		{ "font-khmer", Utilities.FONT_KHMER_DEFAULT },
		{ "font-myanmar", Utilities.FONT_MYANMAR_DEFAULT },
		{ "font-sinhala", Utilities.FONT_SINHALA_DEFAULT },
		{ "font-thai", Utilities.FONT_THAI_DEFAULT },
		{ "font-unknown", Utilities.FONT_ROMAN_DEFAULT },
		{ "slp1-mapto", "DEVA" },
		{ "virama-key", "`" }
	};
	public static enum PaliInputProperties {
		INSTANCE;
		private final Map<String, String> defPaliInputTable = new HashMap<>();
		private final String[] unusedCharNames = { "a macron (ā)", "i macron (ī)", "u macron (ū)", "n dot above (ṅ)", "n tilde (ñ)",
								"t dot below (ṭ)", "d dot below (ḍ)", "n dot below (ṇ)", "l dot below (ḷ)", "m dot below (ṃ)",
								"uppercase key", "lowercase key" };
		private final String[] unusedCharKeys = { "uc-ā", "uc-ī", "uc-ū", "uc-ṅ", "uc-ñ", "uc-ṭ",
												"uc-ḍ", "uc-ṇ", "uc-ḷ", "uc-ṃ", "uc-upper", "uc-lower" };
		
		private final String[] compCharNames = { "tilde (o"+'\u0303'+")", "macron (o"+'\u0304'+")", "dot above (o"+'\u0307'+")",
												"dot below (o"+'\u0323'+")", "acute (o"+'\u0301'+")", "avagraha (o"+'\u0315'+")" };
		private final String[] compCharKeys = { "co-" + '\u0303', "co-" + '\u0304', "co-" + '\u0307',
												"co-" + '\u0323', "co-" + '\u0301', "co-" + '\u0315' };
		private PaliInputProperties() {
			String[] unusedCharVals = { "x", "X", "W", "F", "f", "q", "Q", "z", "Z", "w", "<", ">" };
			// for unused-character input method
			for (int i = 0; i<unusedCharKeys.length; i++)
				defPaliInputTable.put(unusedCharKeys[i], unusedCharVals[i]);
			// for composite-character input method
			String[] compCharVals = { "~", "-", "\"", ".", "'", "`" };
			for (int i = 0; i<compCharKeys.length; i++)
				defPaliInputTable.put(compCharKeys[i], compCharVals[i]);		
		}
		public Map<String, String> getDefaultTable() {
			return defPaliInputTable;
		}
		public String[] getUnusedCharNames() {
			return unusedCharNames;
		}
		public String[] getUnusedCharKeys() {
			return unusedCharKeys;
		}
		public String[] getCompCharNames() {
			return compCharNames;
		}
		public String[] getCompCharKeys() {
			return compCharKeys;
		}
	}
	
	private MainProperties() {
		// init default property map
		propertiesFile = new File(Utilities.ROOTDIR + PROPERTIES_FILE);
		settings = new Properties();
		if (propertiesFile.exists()){
			try (final InputStream in = new FileInputStream(propertiesFile)) {
				settings.load(in);
			} catch (IOException e) {
				System.err.println(e);
			}
		} else {
			//compose the default settings
			for (final String[] prop : defProperties) {
				settings.setProperty(prop[0], prop[1]);
			}
			// set up default Pali input method properties
			final Map<String, String> defTable = PaliInputProperties.INSTANCE.getDefaultTable();
			for (final Map.Entry<String, String> entry : defTable.entrySet()) {
				settings.setProperty(entry.getKey(), entry.getValue());
			}
		}
	}
	
	public Properties getSettings() {
		return settings;
	}

	public String getDefault(final String key) {
		String result = "ABSENT";
		for (final String[] prop : defProperties) {
			if (prop[0].equals(key)) {
				result = prop[1];
				break;
			}
		}
		return result;
	}

	public String getProp(final String key) {
		return settings.getProperty(key, getDefault(key));
	}

	public void setProp(final String key, final String value) {
		settings.setProperty(key, value);
	}
	
	public void saveSettings(final double width, final double height) {
		settings.setProperty("width", "" + width);
		settings.setProperty("height", "" + height);
		saveSettings();
	}
	
	public void saveSettings() {
		try (final OutputStream out = new FileOutputStream(propertiesFile)) {
			settings.store(out, "Properties of Pali Platform 3");
		} catch (IOException e) {
			System.err.println(e);
		}
	}

}
