/*
 * UrlProperties.java
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

package paliplatform.base;

import java.util.*;
import java.io.*;
import java.util.Properties;
import javafx.application.Platform;

/** 
 * This manages URL properties mainly used for downloading materials.
 * This class is a singleton.
 * @author J.R. Bhaddacak
 * @version 3.0
 * @since 3.0
 */
public class UrlProperties {
	public static final UrlProperties INSTANCE = new UrlProperties();
	public static final String URL_PROPS_FILE = "pp3urls.properties";
	public static final String MAIN_URL = "https://raw.githubusercontent.com/bhaddacak/paliplatform/main/"; // hardcode
//~ 	public static final String MAIN_URL = "http://localhost:8000/"; // for test
	private static final String DEF_DPD_DB_URL = "https://github.com/digitalpalidictionary/digitalpalidictionary/releases/download/v0.1.20240720/dpd.db.tar.bz2";
	private static final String DEF_SC_DATA_URL = "https://github.com/suttacentral/bilara-data/archive/refs/heads/published.zip";
	private static final String NCPED_URL = "https://raw.githubusercontent.com/suttacentral/sc-data/refs/heads/main/dictionaries/simple/en/pli2en_ncped.json";
	private static final String PTSPED_URL = "https://github.com/vpnry/ptsped/raw/refs/heads/main/tabfiles/Tabfile_PTSPED-2021.zip";
	private final File urlPropsFile;
	private final Properties urlProps;
	private final SimpleDownloader downloader;
	
	private UrlProperties() {
		urlPropsFile = new File(Utilities.ROOTDIR + URL_PROPS_FILE);
		urlProps = new Properties();
		downloader = new SimpleDownloader(
				MAIN_URL + URL_PROPS_FILE,
				urlPropsFile,
				res -> load(), // successful
				res -> {
					// fail, then compose the default urlProps
					urlProps.setProperty("main_url", MAIN_URL);
					urlProps.setProperty("dpd_db_url", DEF_DPD_DB_URL);
					urlProps.setProperty("sc_data_url", DEF_SC_DATA_URL);
					urlProps.setProperty("ncped_url", NCPED_URL);
					urlProps.setProperty("ptsped_url", PTSPED_URL);
				});
		if (propsFileExists()){
			load();
		} else {
			// download from web
			Platform.runLater(() ->	downloader.start());
		}
	}

	public void load() {
		try (final InputStream in = new FileInputStream(urlPropsFile)) {
			urlProps.load(in);
		} catch (IOException e) {
			System.err.println(e);
		}
	}

	public void update() {
		downloader.restart();
	}

	public boolean propsFileExists() {
		return urlPropsFile.exists();
	}
	
	public Properties getUrlProps() {
		return urlProps;
	}
	
	public void saveUrlProps() {
		try (final OutputStream out = new FileOutputStream(urlPropsFile)) {
			urlProps.store(out, "URL Properties of Pali Platform 3");
		} catch (IOException e) {
			System.err.println(e);
		}
	}

}

