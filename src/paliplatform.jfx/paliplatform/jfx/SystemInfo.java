/*
 * SystemInfo.java
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

import java.util.*;

/** 
 * The representation of system information. This is a singleton.
 * @author J.R. Bhaddacak
 * @version 3.0
 * @since 3.0
 */

class SystemInfo {
	public static final SystemInfo INSTANCE = new SystemInfo();
	static enum OsType { 
		LINUX("Linux/Unix-like"), WINDOWS("Windows"), MAC("macOS");
		private final String name;
		private OsType(final String name) {
			this.name = name;
		}
		public String getName() {
			return name;
		}
	}
	static enum Processor { X86, AARCH64, UNCERTAIN }
	static enum ArchBit { 
		BIT32("32-bit"), BIT64("64-bit");
		private final String name;
		private ArchBit(final String name) {
			this.name = name;
		}
		public String getName() {
			return name;
		}
	}
	static enum Source { 
		GLUON, MAVEN, OTHER;
		public static Source[] values = values();
	}
	private static final Runtime.Version JAVAFX_13 = Runtime.Version.parse("13");
	private static final Runtime.Version JAVAFX_17A = Runtime.Version.parse("17.0.8");
	private static final Runtime.Version JAVAFX_17B = Runtime.Version.parse("17.0.12");
	public static final Runtime.Version JAVAFX_21 = Runtime.Version.parse("21.0.4");
//~ 	private static final String TEST_URL = "http://localhost:8000/jfx/";
	private static final String ARCHLINUX32_URL = "http://pool.mirror.archlinux32.org/i686/extra/";
	private static final String GLUON_URL = "https://download2.gluonhq.com/openjfx/";
	private static final String MAVEN_URL = "https://repo1.maven.org/maven2/org/openjfx/";
	private static final String[] JFX_MODS = { "javafx-base", "javafx-controls", "javafx-graphics", "javafx-web", "javafx-media" };
	private final Runtime.Version javaVersion;
	private final OsType osType;
	private final Processor processor;
	private final ArchBit archBit;
	
	private SystemInfo() {
		javaVersion = Runtime.Version.parse(System.getProperty("java.version"));
		final String osArch = System.getProperty("os.arch").toLowerCase();
		if (System.getProperty("mrj.version") != null) {
			// macOS
			osType = OsType.MAC;
			archBit = ArchBit.BIT64;
			processor = osArch.contains("aarch64") ? Processor.AARCH64 : Processor.X86;
		} else {
			archBit = System.getProperty("sun.arch.data.model").contains("32") ? ArchBit.BIT32 : ArchBit.BIT64;
			if (System.getProperty("os.name").toLowerCase().contains("windows")) {
				// Windows
				osType = OsType.WINDOWS;
				processor = Processor.X86;
			} else {
				// Unix-like
				osType = OsType.LINUX;
				processor = osArch.contains("aarch64")
							? Processor.AARCH64
							: osArch.contains("i386") || osArch.contains("amd64")
								? Processor.X86
								: Processor.UNCERTAIN;
			}
		}
	}

	public Runtime.Version getJavaVersion() {
		return javaVersion;
	}

	public Processor getProcessor() {
		return processor;
	}

	public String getArchString() {
		final String aarch64 = processor == Processor.AARCH64 ? "AArch64" : "";
		final String uncertain = processor == Processor.UNCERTAIN ? "?" : "";
		final String bit = osType == OsType.MAC
							? ""
							: aarch64.isEmpty()
								? " (" + archBit.getName() + ")"
								: "";
		return osType.getName() + " " + aarch64 + uncertain + bit;
	}

	public Map<Source, List<Runtime.Version>> getAvailableJavaFX() {
		final Map<Source, List<Runtime.Version>> result = new EnumMap<>(Source.class);
		final Runtime.Version java17 = Runtime.Version.parse("17");
		if (archBit == ArchBit.BIT32) {
			if (processor != Processor.AARCH64) {
				switch (osType) {
					case LINUX:
						result.put(Source.OTHER, List.of(JAVAFX_13));
						break;
					case WINDOWS:
						result.put(Source.MAVEN, List.of(JAVAFX_17A));
						break;
				}
			}
		} else {
			List<Runtime.Version> verList;
			switch (osType) {
				case MAC:
				case WINDOWS:
					if (javaVersion.compareTo(java17) < 0) {
						verList = List.of(JAVAFX_17B);
						result.put(Source.GLUON, verList);
						result.put(Source.MAVEN, verList);
					} else {
						verList = List.of(JAVAFX_17B, JAVAFX_21);
						result.put(Source.GLUON, verList);
						result.put(Source.MAVEN, verList);
					}
					break;
				case LINUX:
					if (javaVersion.compareTo(java17) < 0) {
						if (processor == Processor.AARCH64) {
							result.put(Source.MAVEN, List.of(JAVAFX_17A));
						} else {
							verList = List.of(JAVAFX_17B);
							result.put(Source.GLUON, verList);
							result.put(Source.MAVEN, verList);
						}
					} else {
						verList = List.of(JAVAFX_17B, JAVAFX_21);
						result.put(Source.GLUON, verList);
						if (processor == Processor.AARCH64)
							verList = List.of(JAVAFX_17A);
						else
							verList = List.of(JAVAFX_17B, JAVAFX_21);
						result.put(Source.MAVEN, verList);
					}
					break;
			}
		}
		return result;
	}

	public List<String> getJfxUrlList(final String src, final String ver) {
		List<String> result = new ArrayList<>();
		final Source source = Source.valueOf(src);
		if (source == Source.OTHER) {
			if (JAVAFX_13.toString().equals(ver)) {
				result.add(ARCHLINUX32_URL + "java-openjfx-13.u14-1.1-i686.pkg.tar.xz");
			}
		} else if (source == Source.GLUON) {
			final String os = osType == OsType.MAC
								? "osx"
								: osType.toString().toLowerCase();
			final String proc = processor == Processor.AARCH64
								? "-aarch64"
								: "-x64";
			final String url;
			if (archBit == ArchBit.BIT64) {
				url = GLUON_URL + ver + "/openjfx-" + ver + "_" + os + proc + "_bin-sdk.zip";
			} else {
				url = GLUON_URL + ver + "/openjfx-" + ver + "_" + os + "-x86" + "_bin-sdk.zip";
			}
			result.add(url);
		} else if (source == Source.MAVEN) {
			final String os = osType == OsType.WINDOWS
								? "win"
								: osType.toString().toLowerCase();
			final String proc = processor == Processor.AARCH64
								? "-aarch64"
								: archBit == ArchBit.BIT32
									? "-x86"
									: "";
			for (final String mod : JFX_MODS) {
				final String url = MAVEN_URL + mod + "/" + ver + "/" + mod + "-" + ver + "-" + os + proc + ".jar";
				result.add(url);
			}
		}
		return result;
	}

}

