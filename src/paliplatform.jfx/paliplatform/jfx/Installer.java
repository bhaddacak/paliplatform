/*
 * Installer.java
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

package paliplatform.jfx;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.*;
import java.net.URLDecoder;
import java.awt.EventQueue;
import javax.swing.JFrame;

import org.apache.commons.compress.archivers.*;
import org.apache.commons.compress.archivers.tar.*;
import org.apache.commons.compress.archivers.zip.*;
import org.apache.commons.compress.compressors.xz.*;
import org.apache.commons.io.IOUtils;

/** 
 * The installer of JavaFX for Pāli Platform 3.
 * @author J.R. Bhaddacak
 * @version 3.0
 * @since 3.0
 */

public class Installer {
	private static final String PROG_NAME = "JavaFX Installer";
	public static final String LINESEP = System.getProperty("line.separator");
	public static final String JFX_FILELIST = "javafx.filelist";
	public static String ROOTDIR = "";
	private static final String MODULES = "modules";
	public static final String MODDIR = MODULES + File.separator;
	public static final String CACHEDIR = "cache" + File.separator;
	public static final String BINDIR = "bin" + File.separator;
	private static final File TEMPDIR = new File(ROOTDIR + CACHEDIR + "temp" + File.separator);

	private Installer() {
	}

	public static void main(final String[] args) throws Exception {
		ROOTDIR = getRootDir(args);
		// prepare for macOS UI
		final boolean isMacOS = System.getProperty("mrj.version") != null;
		if (isMacOS) {
			System.setProperty("apple.laf.useScreenMenuBar", "true");
			System.setProperty("com.apple.mrj.application.apple.menu.about.name", PROG_NAME);
		}
		EventQueue.invokeLater(() -> {
			final InstallerWin installerWin = new InstallerWin();
			installerWin.setTitle(PROG_NAME);
			installerWin.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			installerWin.pack();
			installerWin.setVisible(true);
		});
	}

	private static String getRootDir(final String[] args) throws Exception {
		String classPath = URLDecoder.decode(Installer.class.getProtectionDomain().getCodeSource().getLocation().getPath(), StandardCharsets.UTF_8);
		if (SystemInfo.INSTANCE.isWindows() && classPath.startsWith("/"))
			classPath = classPath.substring(1);
		String appPath = "";
		if (args.length == 0){
			if (classPath.toLowerCase().endsWith(".jar")) {
				appPath = classPath.substring(0, classPath.lastIndexOf("/") + 1);
			} else {
				appPath = classPath;
			}
			appPath = appPath.endsWith("/") ? appPath : appPath + "/";
			final String moddir = MODULES + "/";
			appPath = appPath.endsWith(moddir)
						? appPath.substring(0, appPath.lastIndexOf(moddir))
						: appPath;
		} else {
			appPath = args[0];
		}
		final Path apath = Path.of(appPath);
		final File afile = apath.toFile();
		final String rootdir = afile.isDirectory() ? afile.getPath() + File.separator : "." + File.separator;
		return rootdir;
	}

	public static String readText(final File file) {
		final StringBuilder text = new StringBuilder();
		try (final Scanner in = new Scanner(new FileInputStream(file), StandardCharsets.UTF_8)) {
			while (in.hasNextLine()) {
				final String line = in.nextLine().trim();
				text.append(line).append(LINESEP);
			}
		} catch (FileNotFoundException e) {
			System.err.println(e.toString());
		}
		return text.toString();
	}
	
	public static List<String> readTextToList(final File file) {
		final List<String> result = new ArrayList<>();
		try (final Scanner in = new Scanner(new FileInputStream(file), StandardCharsets.UTF_8)) {
			while (in.hasNextLine()) {
				final String line = in.nextLine().trim();
				result.add(line);
			}
		} catch (FileNotFoundException e) {
			System.err.println(e.toString());
		}
		return result;
	}
	
	public static String readResourceText(final String resName) {
		final StringBuilder text = new StringBuilder();
		try (final Scanner in = new Scanner(Installer.class.getResourceAsStream("resources/" + resName), StandardCharsets.UTF_8)) {
			while (in.hasNextLine()) {
				final String line = in.nextLine().trim();
				if (!line.isEmpty())
					text.append(line).append(LINESEP);
			}
		}
		return text.toString();
	}

	public static void saveText(final String text, final File file) {
		try (final BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {
			out.write(text, 0, text.length());
		} catch (IOException e) {
			System.err.println(e.toString());
		}			
	}

	public static File getJfxFileList(final File targetDir) {
		return new File(targetDir, JFX_FILELIST);
	}

	public static void removeFilesInList(final File targetDir, final File listFile) {
		if (!listFile.exists()) return;
		final List<String> fileList = Installer.readTextToList(listFile);
		for (final String f : fileList) {
			final File file = new File(targetDir, f);
			if (file.exists())
				file.delete();
		}
	}

	public static void removeJavaFX(final File targetDir) {
		if (targetDir.exists()) {
			final File[] files = targetDir.listFiles((f, n) -> n.startsWith("javafx"));
			for (final File f : files) {
				f.delete();
			}
		}
	}

	public static void removeAllNativeLib(final File targetDir) {
		// Linux and macOS
		if (targetDir.exists()) {
			final File[] files = targetDir.listFiles((f, n) -> n.endsWith(".so") || n.endsWith(".dylib"));
			for (final File f : files) {
				f.delete();
			}
		}
		// Windows
		removeBinNativeLib();
	}

	public static void removeBinNativeLib() {
		// for Windows
		final File binDir = new File(BINDIR);
		if (binDir.exists()) {
			final File[] files = binDir.listFiles((f, n) -> n.toLowerCase().endsWith(".dll"));
			for (final File f : files) {
				f.delete();
			}
		}
	}

	private static void removeExistingJfx(final File targetDir) {
		final File listFile = getJfxFileList(targetDir);
		removeFilesInList(targetDir, listFile);
	}

	public static void unXZ(final File src, final File target) {
		final String filename = src.getName();
		if (!TEMPDIR.exists())
			TEMPDIR.mkdir();
		final File tarOut = new File(TEMPDIR, filename.substring(0, filename.lastIndexOf("."))); 
		try (final InputStream fin = new FileInputStream(src);
				final BufferedInputStream in = new BufferedInputStream(fin);
				final XZCompressorInputStream xzIn = new XZCompressorInputStream(in);
				final OutputStream out = new FileOutputStream(tarOut)) {
			final byte[] buffer = new byte[4096];
			int n = 0;
			while (-1 != (n = xzIn.read(buffer))) {
				out.write(buffer, 0, n);
			}
		} catch (IOException e) {
			System.err.println(e);
		}
		if (tarOut.exists()) {
			removeExistingJfx(target);
			untar(tarOut, target);
		}
	}

	public static void unzip(final File source, final File targetDir) {
		try (final ArchiveInputStream in = new ZipArchiveInputStream(new FileInputStream(source))) {
			if (source.getName().endsWith("_bin-sdk.zip")) {
				// from Gluon
				unpackGluonZip(in, targetDir);
			} else {
				// general cases
				unarchive(in, targetDir);
			}
		} catch (IOException e) {
			System.err.println(e);
		}
	}

	public static void untar(final File source, final File targetDir) {
		try (final ArchiveInputStream in = new TarArchiveInputStream(new FileInputStream(source))) {
			if (source.getName().endsWith(".pkg.tar")) {
				// from ArchLinux32
				unpackPkg(in, targetDir);
			} else {
				// general cases
				unarchive(in, targetDir);
			}
			source.delete();
		} catch (IOException e) {
			System.err.println(e);
		}
	}

	private static void unarchive(final ArchiveInputStream in, final File targetDir) {
		ArchiveEntry entry = null;
		try {
			while ((entry = in.getNextEntry()) != null) {
				if (!in.canReadEntryData(entry)) {
					continue;
				}
				final File dest = new File(targetDir, entry.getName());
				if (entry.isDirectory()) {
					if (!dest.isDirectory() && !dest.mkdirs()) {
						throw new IOException("failed to create directory " + dest);
					}
				} else {
					final File parent = dest.getParentFile();
					if (!parent.isDirectory() && !parent.mkdirs()) {
						throw new IOException("failed to create directory " + parent);
					}
					try (final OutputStream out = new FileOutputStream(dest)) {
						IOUtils.copy(in, out);
					}
				}
			}
		} catch (IOException e) {
			System.err.println(e);
		}
	}

	private static void unpackPkg(final ArchiveInputStream in, final File targetDir) {
		final String pkg = "java-13-openjdk";
		ArchiveEntry entry = null;
		try {
			final StringBuilder filelist = new StringBuilder();
			while ((entry = in.getNextEntry()) != null) {
				if (!in.canReadEntryData(entry)) {
					continue;
				}
				final String name = entry.getName();
				if (name.startsWith("usr/lib/jvm/" + pkg + "/lib/") && !entry.isDirectory()) {
					final String shortName = name.substring(name.lastIndexOf("/") + 1);
					filelist.append(shortName).append(LINESEP);
					final File dest = new File(targetDir, shortName);
					if (!targetDir.exists())
						targetDir.mkdirs();
					try (final OutputStream out = new FileOutputStream(dest)) {
						IOUtils.copy(in, out);
					}
				}
			}
			filelist.append(JFX_FILELIST).append(LINESEP);
			saveText(filelist.toString(), new File(targetDir, JFX_FILELIST));
		} catch (IOException e) {
			System.err.println(e);
		}
	}

	private static void unpackGluonZip(final ArchiveInputStream in, final File targetDir) {
		ArchiveEntry entry = null;
		try {
			final StringBuilder filelist = new StringBuilder();
			while ((entry = in.getNextEntry()) != null) {
				if (!in.canReadEntryData(entry)) {
					continue;
				}
				final String name = entry.getName();
				if (!entry.isDirectory()) {
					final String shortName = name.substring(name.lastIndexOf("/") + 1);
					File dir = null;
					final File dest;
					if (name.contains("/lib/")) {
						if ("src.zip".equals(shortName)) continue;
						filelist.append(shortName).append(LINESEP);
						dir = targetDir;
					} else if (name.contains("/bin/")) {
						// only Windows places dll files in bin
						dir = new File(BINDIR);
					}
					if (dir != null) {
						if (!dir.exists())
							dir.mkdirs();
						dest = new File(dir, shortName);
						try (final OutputStream out = new FileOutputStream(dest)) {
							IOUtils.copy(in, out);
						}
					}
				}
			}
			filelist.append(JFX_FILELIST).append(LINESEP);
			saveText(filelist.toString(), new File(targetDir, JFX_FILELIST));
		} catch (IOException e) {
			System.err.println(e);
		}
	}

}
