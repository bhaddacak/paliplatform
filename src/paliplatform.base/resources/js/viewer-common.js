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
	"color0": "black",
	"background0": "white",
	"blockBG0": "white",
	"noteColor0": "black",
	"xrefColor0": "black",
	"transColor0": "black",
	"commentColor0": "black",
	"referenceColor0": "black",
	"color1": "#1f1f1f",
	"background1": "#e0e0e0",
	"blockBG1": "#808080",
	"noteColor1": "blue",
	"xrefColor1": "#8B6914",
	"transColor1": "midnightblue",
	"commentColor1": "saddlebrown",
	"referenceColor1": "maroon",
	"color2": "#1f1f1f",
	"background2": "#fafacf",
	"blockBG2": "#808080",
	"noteColor2": "blue",
	"xrefColor2": "#8B6914",
	"transColor2": "midnightblue",
	"commentColor2": "saddlebrown",
	"referenceColor2": "maroon",
	"color3": "#1f1f1f",
	"background3": "#cff3fa",
	"blockBG3": "#808080",
	"noteColor3": "blue",
	"xrefColor3": "#8B6914",
	"transColor3": "midnightblue",
	"commentColor3": "saddlebrown",
	"referenceColor3": "maroon"
};
const darkThemeObj = {
	"color0": "white",
	"background0": "black",
	"blockBG0": "black",
	"noteColor0": "white",
	"xrefColor0": "white",
	"transColor0": "white",
	"commentColor0": "white",
	"referenceColor0": "white",
	"color1": "#dfdfdf",
	"background1": "#1f1f1f",
	"blockBG1": "#404040",
	"noteColor1": "lightblue",
	"xrefColor1": "#E9AA0E",
	"transColor1": "royalblue",
	"commentColor1": "darksalmon",
	"referenceColor1": "brown",
	"color2": "#dfdfdf",
	"background2": "#151502",
	"blockBG2": "#404040",
	"noteColor2": "lightblue",
	"xrefColor2": "#E9AA0E",
	"transColor2": "royalblue",
	"commentColor2": "darksalmon",
	"referenceColor2": "brown",
	"color3": "#dfdfdf",
	"background3": "#031519",
	"blockBG3": "#404040",
	"noteColor3": "lightblue",
	"xrefColor3": "#E9AA0E",
	"transColor3": "royalblue",
	"commentColor3": "darksalmon",
	"referenceColor3": "brown"
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
