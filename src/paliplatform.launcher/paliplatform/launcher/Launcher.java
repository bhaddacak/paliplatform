/*
 * Launcher.java
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

package paliplatform.launcher;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.net.*;
import java.lang.module.*;
import javax.swing.*;

/** 
 * The launcher for Pāli Platform 3-4,
 * suitable for making an exe launcher for Windows
 * and doing a clean-up start after patches installed.
 * @author J.R. Bhaddacak
 * @version 4.0
 * @since 3.0
 */

public class Launcher {
	private static final String MODULES = "modules";
	private static final String WIN_EXE = "PPLauncher.exe";
	private static final String WIN_NEW_EXE = "PPLauncher_new.exe";
	private static final boolean isWindows = System.getProperty("os.name").toLowerCase().contains("windows");

	private Launcher() {
	}
	
	public static void main(final String[] args) throws Exception {
		final String rootdir = getRootDir();
		removeDuplicatedModules(rootdir);
		if (isWindows) {
			if (!newLauncherExists(rootdir)) {
				launch(rootdir);
			} else {
				final String mess = "A newer launcher is found.\n" +
					"Please remove the existing " + WIN_EXE + ".\n" +
					"Rename " + WIN_NEW_EXE + " to " + WIN_EXE + ".\n" +
					"And start again.";
				JOptionPane.showMessageDialog(null, mess, "New launcher found", JOptionPane.INFORMATION_MESSAGE);
				System.exit(1);
			}
		} else {
			launch(rootdir);
		}
	}

	private static String getRootDir() throws Exception {
		String classPath = URLDecoder.decode(Launcher.class.getProtectionDomain().getCodeSource().getLocation().getPath(), "UTF-8");
		if (isWindows && classPath.startsWith("/"))
			classPath = classPath.substring(1);
		String appPath = "";
		if (classPath.toLowerCase().endsWith(".jar")) {
			appPath = classPath.substring(0, classPath.lastIndexOf("/") + 1);
		} else {
			appPath = classPath;
		}
		final String moddir = MODULES + "/";
		appPath = appPath.endsWith("/") ? appPath : appPath + "/";
		appPath = appPath.endsWith(moddir)
					? appPath.substring(0, appPath.lastIndexOf(moddir))
					: appPath;
		final Path apath = Path.of(appPath);
		final File afile = apath.toFile();
		final String rootdir = afile.isDirectory() ? afile.getPath() + File.separator : "." + File.separator;
		return rootdir;
	}

	private static boolean newLauncherExists(final String rootdir) {
		final File launcher = new File(rootdir + WIN_NEW_EXE);
		return launcher.exists();
	}

	private static void removeDuplicatedModules(final String rootdir) {
		final File moddir = new File(rootdir + MODULES);
		if (!moddir.exists()) return;
		final Map<String, ModuleDescriptor.Version> moduleMap = new HashMap<>();
		final Map<String, File> fileMap = new HashMap<>();
		final List<String> deleteList = new ArrayList<>();
		final File[] modFiles = moddir.listFiles((f, n) -> n.toLowerCase().endsWith(".jar"));
		for (final File f : modFiles) {
			final String name = f.getName();
			final String modname = getModuleNameFromFileName(name);
			final ModuleDescriptor.Version modver = ModuleDescriptor.Version.parse(getModuleVersionFromFileName(name));
			final String fileKey = modname + "-" + modver.toString();
			fileMap.put(fileKey, f);
			if (moduleMap.containsKey(modname)) {
				final ModuleDescriptor.Version existing = moduleMap.get(modname);
				if (existing.compareTo(modver) < 0) {
					deleteList.add(modname + "-" + existing.toString());
					moduleMap.put(modname, modver);
				} else {
					deleteList.add(fileKey);
				}
			} else {
				moduleMap.put(modname, modver);
			}
		}
		try {
			for (final String k : deleteList) {
				final File f = fileMap.get(k);
				Files.delete(f.toPath());
			}
		} catch (IOException e) {
			System.err.println(e);
		}
	}

	private static int getDelimPos(final String filename) {
		int hpos = filename.indexOf("-");
		while (hpos > -1 && hpos < filename.length() - 1 && !Character.isDigit(filename.charAt(hpos + 1))) {
			hpos = filename.indexOf("-", hpos + 1);
		}
		return hpos;
	}

	private static String getModuleNameFromFileName(final String filename) {
		final int hpos = getDelimPos(filename);
		final String result = hpos < 0
								? filename.substring(0, filename.lastIndexOf("."))
								: filename.substring(0, hpos);
		return result;
	}

	private static String getModuleVersionFromFileName(final String filename) {
		final int hpos = getDelimPos(filename);
		final String result = hpos < 0
								? "0"
								: filename.substring(hpos + 1, filename.lastIndexOf("."));
		return result;
	}

	private static void launch(final String rootdir) throws Exception {
		// find JavaFX in the system and local modules
		final ModuleFinder sysMFinder = ModuleFinder.ofSystem();
		final Optional<ModuleReference> sysOMRef = sysMFinder.find("javafx.base");
		final ModuleFinder locMFinder = ModuleFinder.of(Path.of(rootdir + File.separator + MODULES));
		final Optional<ModuleReference> locOMRef = locMFinder.find("javafx.base");
		if (sysOMRef.isPresent() || locOMRef.isPresent()) {
			final String cmd = isWindows ? "run.cmd" : "./run.sh";
			final Process proc = Runtime.getRuntime().exec(cmd);
			proc.waitFor();
			System.out.println(proc);
		} else {
			final Object[] options = {"JavaFX Installer", "Exit"};
			final int ans = JOptionPane.showOptionDialog(null, "No JavaFX found, please install it first",
					"Error: JavaFX not found", JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE, null, options, options[1]);
			if (ans == 0) {
				final String cmd = isWindows ? "jfxinstall.cmd" : "./jfxinstall.sh";
				final Process proc = Runtime.getRuntime().exec(cmd);
				proc.waitFor();
				System.out.println(proc);
			} else {
				System.exit(0);
			}
		}
	}

}
