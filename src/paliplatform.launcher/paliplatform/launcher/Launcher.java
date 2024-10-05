/*
 * Launcher.java
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

package paliplatform.launcher;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.net.*;
import java.lang.module.*;
import javax.swing.*;

/** 
 * The launcher for Pāli Platform 3,
 * suitable for making an exe launcher for Windows.
 * @author J.R. Bhaddacak
 * @version 3.0
 * @since 3.0
 */

public class Launcher {
	private static final String MODULES_DIR = "modules";

	private Launcher() {
	}
	
	public static void main(final String[] args) throws Exception {
		final String rootdir = getRootDir();
		launch(rootdir);
	}

	private static String getRootDir() throws Exception {
		final boolean isWindows = System.getProperty("os.name").startsWith("Windows");
		String classPath = URLDecoder.decode(Launcher.class.getProtectionDomain().getCodeSource().getLocation().getPath(), "UTF-8");
		if (isWindows && classPath.startsWith("/"))
			classPath = classPath.substring(1);
		String appPath = "";
		if (classPath.toLowerCase().endsWith(".jar")) {
			appPath = classPath.substring(0, classPath.lastIndexOf("/") + 1);
		} else {
			appPath = classPath;
		}
		final String moddir = "modules/";
		appPath = appPath.endsWith("/") ? appPath : appPath + "/";
		appPath = appPath.endsWith(moddir)
					? appPath.substring(0, appPath.lastIndexOf(moddir))
					: appPath;
		final Path apath = Path.of(appPath);
		final File afile = apath.toFile();
		final String rootdir = afile.isDirectory() ? afile.getPath() + File.separator : "." + File.separator;
		return rootdir;
	}

	private static void launch(final String rootdir) throws Exception {
		// find JavaFX in the system and local modules
		final ModuleFinder sysMFinder = ModuleFinder.ofSystem();
		final Optional<ModuleReference> sysOMRef = sysMFinder.find("javafx.base");
		final ModuleFinder locMFinder = ModuleFinder.of(Path.of(rootdir + File.separator + MODULES_DIR));
		final Optional<ModuleReference> locOMRef = locMFinder.find("javafx.base");
		if (sysOMRef.isPresent() || locOMRef.isPresent()) {
			final String cmd = System.getProperty("os.name").toLowerCase().contains("windows") ? "run.cmd" : "./run.sh";
			final Process proc = Runtime.getRuntime().exec(cmd);
			proc.waitFor();
			System.out.println(proc);
		} else {
			final Object[] options = {"JavaFX Installer", "Exit"};
			final int ans = JOptionPane.showOptionDialog(null, "No JavaFX found, please install it first",
					"Error: JavaFX not found", JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE, null, options, options[1]);
			if (ans == 0) {
				final String cmd = System.getProperty("os.name").toLowerCase().contains("windows") ? "jfxinstall.cmd" : "./jfxinstall.sh";
				final Process proc = Runtime.getRuntime().exec(cmd);
				proc.waitFor();
				System.out.println(proc);
			} else {
				System.exit(0);
			}
		}
	}

}
