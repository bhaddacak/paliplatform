/*
 * FontAwesomeData.java
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

import java.util.HashMap;

/** 
 * This contains the FontAwesome symbols currently used.
 * This is a singleton.
 * @author J.R. Bhaddacak
 * @version 3.0
 * @since 2.0
 */
 class FontAwesomeData extends HashMap<String, Character> {
	 static final FontAwesomeData INSTANCE = new FontAwesomeData();
	 
	 private FontAwesomeData() {
		 super();
		 put("0", '0');
		 put("A", 'A');
		 put("B", 'B');
		 put("C", 'C');
		 put("G", 'G');
		 put("H", 'H');
		 put("L", 'L');
		 put("N", 'N');
		 put("R", 'R');
		 put("T", 'T');
		 put("W", 'W');
		 put("Z", 'Z');
		 put("music", '\uF001');
		 put("magnifying-glass", '\uF002');
		 put("table-cells", '\uF00A');
		 put("check", '\uF00C');
		 put("xmark", '\uF00D');
		 put("power-off", '\uF011');
		 put("gear", '\uF013');
		 put("file", '\uF016');
		 put("download", '\uF019');
		 //~ put("arrow-rotate-right", '\uF01E');
		 put("arrows-rotate", '\uF021');
		 put("lock", '\uF023');
		 put("tag", '\uF02B');
		 put("tags", '\uF02C');
		 put("book", '\uF02D');
		 put("bookmark", '\uF02E');
		 put("print", '\uF02F');
//~ 		 put("camera", '\uF030');
		 put("font", '\uF031');
		 put("list", '\uF03A');
		 put("circle-half-stroke", '\uF042');
		 put("backward-step", '\uF048');
		 put("forward-step", '\uF051');
		 put("circle-plus", '\uF055');
		 put("circle-minus", '\uF056');
		 put("circle-question", '\uF059');
		 put("circle-info", '\uF05A');
 		 //~ put("ban", '\uF05E');
		 put("expand", '\uF065');
		 //~ put("compress", '\uF066');
		 put("plus", '\uF067');
		 put("minus", '\uF068');
		 put("asterisk", '\uF069');
		 //~ put("eye", '\uF06E');
		 put("eye-slash", '\uF070');
		 put("folder", '\uF07B');
		 put("folder-open", '\uF07C');
		 put("arrows-up-down", '\uF07D');
//~ 		 put("key", '\uF084');
		 put("gears", '\uF085');
		 put("heart", '\uF08A');
		 put("upload", '\uF093');
		 put("unlock", '\uF09C');
		 put("list-check", '\uF0AE');
		 put("briefcase", '\uF0B1');
		 put("link", '\uF0C1');
		 put("copy", '\uF0C5');
		 put("bars", '\uF0C9');
		 //~ put("list-ul", '\uF0CA');
		 put("list-ol", '\uF0CB');
		 //~ put("table-columns", '\uF0DB');
		 //~ put("arrow-rotate-left", '\uF0E2');
		 put("paste", '\uF0EA');
		 put("lightbulb", '\uF0EB');
		 put("stethoscope", '\uF0F1');
		 put("angles-up", '\uF102');
		 put("angles-down", '\uF103');
		 put("angle-left", '\uF104');
		 put("angle-right", '\uF105');
		 put("keyboard", '\uF11C');
		 put("info", '\uF129');
//~ 		 put("puzzle-piece", '\uF12E');
		 put("rocket", '\uF135');
		 put("arrow-turn-down", '\uF149');
		 put("square-arrow-up-right", '\uF14C');
		 put("file-lines", '\uF15C');
		 put("moon", '\uF186');
		 put("box-archive", '\uF187');
		 put("square-plus", '\uF196');
		 put("language", '\uF1AB');
		 put("database", '\uF1C0');
		 put("file-code", '\uF1C9');
		 put("heading", '\uF1DC');
		 put("paragraph", '\uF1DD');
		 put("trash", '\uF1F8');
		 put("note-sticky", '\uF249');
		 put("hand", '\uF256');
		 put("hashtag", '\uF292');
		 //~ put("square-xmark", '\uF2D3');
		 put("pencil", '\uF303');
		 put("pen-clip", '\uF305');
		 put("repeat", '\uF363');
		 put("cloud-arrow-down", '\uF381');
		 put("code-merge", '\uF387');
		 //~ put("lock-open", '\uF3C1');
		 put("boxes-stacked", '\uF468');
		 //~ put("box-open", '\uF49E');
		 put("handshake-angle", '\uF4C4');
		 put("seedling", '\uF4D8');
		 put("scale-unbalanced", '\uF515');
		 put("book-open", '\uF518');
		 put("broom", '\uF51A');
		 put("crown", '\uF521');
		 put("equals", '\uF52C');
		 put("glasses", '\uF530');
		 put("not-equal", '\uF53E');
		 //~ put("robot", '\uF544');
		 put("person-walking", '\uF554');
		 put("delete-left", '\uF55A');
		 put("check-double", '\uF560');
		 put("file-arrow-down", '\uF56D');
		 put("file-arrow-up", '\uF574');
		 put("grip", '\uF58D');
		 put("pen-fancy", '\uF5AC');
		 put("book-open-reader", '\uF5DA');
		 put("person-running", '\uF70C');
		 put("hammer", '\uF6E3');
		 put("scroll", '\uF70E');
		 put("screwdriver-wrench", '\uF7D9');
		 put("folder-tree", '\uF802');
	 }

 }
