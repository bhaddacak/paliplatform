# Pāli Platform 3
“Pāli studies made enjoyable”

## Building the program
To understand this codebase, the knowledge of Java and Java Platform Module System (JPMS) is essential. As you can see in the file arrangement (see below), the project consists of several modules. Some are tightly coupled together, and some are loosely linked by services. You can check these by seeing `module-info.java` in each module.

### Tools
By the previous information, I suppose that the developers are familiar with this knowledge already. So, I will not tell you how to install and use Java/Ant. I will just list the tools I use as follows:

1. Working machine -- I use 32-bit Dell Inspiron 4030 (2011) with Void Linux
2. Java Development Kit (JDK) -- I use Amazon Corretto 11
3. JavaFX -- I use OpenJFX 13 from ArchLinux32 repository (the only 32-bit JavaFX available for Linux)
4. Apache Ant -- I use Ant 1.10.15
5. Editor -- I use Neovim/Neovim-Qt
6. Tools -- I use basic Unix tools like find, grep, zip, etc., also `Launch4j` for making a Windows executable

If your working environment is different from mine, you should use tools suitable to your system. For items 2-3, you may use JDK bundled with JavaFX distributed by Azul (Zulu) or BellSoft (Liberica). I mainly use a 32-bit computer for development to make sure that even lowest-range computers can run the program. I even test the program with my father's laptop from 2006.

Since all source files are encoded in UTF-8, in Windows you have to set this environment variable also:

```
JAVA_TOOL_OPTIONS = -Dfile.encoding=UTF8
```

### File structure
After you have prepared tools to use, downloaded the source from github (or using `git clone https://github.com/bhaddacak/papliplatform.git`), downloaded the final product, then you have to arrange files into this structure:

```
	ROOT-DIR/ (You name it)
	|--.git/
	|--.gitignore
    |--LICENSE
    |--README.md
	|
	|--src/ (the source code)
	|   |--paliplatform.base
	|   |   |--paliplatform/
	|   |   |--resources/
	|   |   |--module-info.java
	|   |--paliplatform.dict
	|   |--paliplatform.dpd
	|   |--paliplatform.grammar
	|   |--paliplatform.jfx
	|   |--paliplatform.launcher
	|   |--paliplatform.lucene
	|   |--paliplatform.main
	|   |--paliplatform.reader
	|   |--paliplatform.sentence
	|   |--build.xml
	|   |--...
	|
	|--dist/ (containing the final product)
	|   |--PaliPlatform
	|       |--data/
	|       |--fonts/
	|       |--modules/
	|       |--PPLauncher.exe
	|       |--PPLauncher.jar
	|       |--run.cmd
	|       |--run.sh
	|       |--some other files...
```

### Build process
I will describe my workflow and you should adapt from this.

1. I open Neovim-Qt at `src` folder and use this as the main editor.
2. I open 2 terminals: one in `src` for issuing build commands (I call this build terminal), another in `dist/PaliPlatform` for running test (I call this run terminal). Practically I use LXTerminal with 2 tabs opened.
3. To build the program I enter `ant build` in the build terminal.
4. To run the program I enter `./run.sh` in the run terminal.

Apart from this, you may need to study what else you can do with `ant`. Enter `ant` for help. Or enter `ant -p` for defined tasks. (You have to be in `src` before use `ant` because it reads `build.xml`.)

### Windows executable
To make `PPLauncher.exe` you have to use `Launch4j`.

1. Download [`Launch4j`](http://launch4j.sourceforge.net).
2. Unpack it and open a terminal there, run it by `java -jar launch4j.jar &`
3. You have to fill the fields marked with a red asterisk. The output file is `PPLauncher.exe`. The jar file is `PPLauncher.jar`. (Select the files, not just typing in.)
4. In JRE, add `jre` to the JRE paths (This makes the program read the JRE in the root folder, if it exists).
5. In Splash, add a bitmap file (BMP, 24-bit without metadata);
6. Hit the gear button (Build wrapper).

### Where is test cases and JUnit?
I tried to use JUnit in the development, but I gave up eventually. One main reason is testing GUI applications with basic tools is really difficult. If you use `IntelliJ IDEA` or `Eclipse` or `NetBeans` and build the project with `Maven` or `Gradle`, it can be easier. For me testing the GUI with seeing and clicking is reliable enough. You have to be very conscious of what are you doing. Moreover, programming GUI with bare coding need a lot of patience and discipline. I call this way of doing *mindful programming*. New developers may try to build the project with modern tools. I just love the plain-and-simple way.

## Useful links
- [Works by J.R. Bhaddacak](https://bhaddacak.github.io)
- [Pāli Platform's page](https://bhaddacak.github.io/paliplatform)
- [Page of Pāli Platform 3](https://bhaddacak.github.io/platform3)

## License
```
Copyright 2023-2024 J.R. Bhaddacak

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or (at
your option) any later version.

This program is distributed in the hope that it will be useful, but
WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see https://www.gnu.org/licenses/.
```

