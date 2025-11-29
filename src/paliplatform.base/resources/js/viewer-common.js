/*
 * viewer-common.js
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
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see https://www.gnu.org/licenses/.
 */

// data definition
const lightThemeObj = {
	"color": [ "black", "#191919", "#191919", "#0d000d", "#0f0f00", "#001010", "#001a00" ],
	"background": [ "white", "white", "#f1f1f1", "#ffe7ff", "#ffffe7", "#e5ffff", "#e5ffe5" ],
	"blockBG": [ "white", "#808080", "#808080", "#808080", "#808080", "#808080", "#808080" ],
	"noteColor": [ "black", "blue", "blue", "blue", "blue", "blue", "blue" ],
	"xrefColor": [ "black", "#8B6914", "#8B6914", "#8B6914", "#8B6914", "#8B6914", "#8B6914" ],
	"transColor": [ "black", "midnightblue", "midnightblue", "midnightblue", "midnightblue", "midnightblue", "midnightblue" ],
	"commentColor": [ "black", "saddlebrown", "saddlebrown", "saddlebrown", "saddlebrown", "saddlebrown", "saddlebrown" ],
	"referenceColor": [ "black", "maroon", "maroon", "maroon", "maroon", "maroon", "maroon" ]
};
const darkThemeObj = {
	"color": [ "white", "#f1f1f1", "#f1f1f1", "#ffe7ff", "#ffffe7", "#e5ffff", "#e5ffe5" ],
	"background": [ "black", "black", "#191919", "#0d000d", "#0f0f00", "#001010", "#001a00" ],
	"blockBG": [ "black", "#404040", "#404040", "#404040", "#404040", "#404040", "#404040" ],
	"noteColor": [ "white", "lightblue", "lightblue", "lightblue", "lightblue", "lightblue", "lightblue" ],
	"xrefColor": [ "white", "#E9AA0E", "#E9AA0E", "#E9AA0E", "#E9AA0E", "#E9AA0E", "#E9AA0E" ],
	"transColor": [ "white", "royalblue", "royalblue", "royalblue", "royalblue", "royalblue", "royalblue" ],
	"commentColor": [ "white", "darksalmon", "darksalmon", "darksalmon", "darksalmon", "darksalmon", "darksalmon" ],
	"referenceColor": [ "white", "brown", "brown", "brown", "brown", "brown", "brown" ]
};
// functions
function setFont(fontnameJson) {
	const fonts = JSON.parse(fontnameJson);
	let fontNames = "";
	for(const f of fonts) {
		fontNames += "'" + f + "',";
	}
 	//window.fxHandler.debugPrint("debug: " + fontNames);
	document.body.style.fontFamily = fontNames.slice(0, -1);
}
function setThemeCommon(theme) {
	const themeObj = theme === 'DARK' ? darkThemeObj : lightThemeObj;
	document.body.style.color = themeObj['color1'];
	document.body.style.background = themeObj['background1'];
}
function copySelection() {
	const sel = window.getSelection();
	const text = sel.toString();
	if(text.length > 0)
		window.fxHandler.copyText(text);
}
function saveSelection() {
	const sel = window.getSelection();
	const text = sel.toString();
	if(text.length > 0)
		window.fxHandler.saveText(text);
}
function copyBody() {
	window.getSelection().selectAllChildren(document.body);
	const sel = window.getSelection();
	const text = sel.toString();
	window.fxHandler.copyText(text);
}
function saveBody() {
	window.getSelection().selectAllChildren(document.body);
	const sel = window.getSelection();
	const text = sel.toString();
	if(text.length > 0)
		window.fxHandler.saveText(text);
}
function openDeclension(term) {
	if(term.length > 0)
		window.fxHandler.openDeclension(term);
}
function openNewDict(term) {
	if(term.length > 0)
		window.fxHandler.openNewDict(term);
}
function findNext(query, caseSensitive, direction) {
	const backWard = parseInt(direction) < 0 ? true : false;
	simpleSearch(query, caseSensitive, backWard);
}
function findSingleQuiet(query) {
	window.find(query, true, false, true, false, false, false);
}
function simpleSearch(query, caseSensitive, backWard) {
	const found = window.find(query, caseSensitive, backWard, true, false, false, false);
	const message = found ? "" : "Not found";
	window.fxHandler.setSearchTextFound(found);
	if (message.length > 0)
		window.fxHandler.showFindMessage(message);
}
