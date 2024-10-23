/*
 * pali-viewer.js
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

// global variables
let romanBody = null;
let textNodeList = [];
// functions
function init(transformable) {
	addMouseListener();
	if (transformable)
		createTextNodeList();
}
function addMouseListener() {
	document.body.addEventListener('mouseup', event => {
		const sel = window.getSelection();
		const text = sel.toString().trim();
		if(text.length > 0) {
			window.fxHandler.showDictResult(text);
			window.fxHandler.updateClickedObject(text);
		} else {
			const text = event.target.className === 'paranum' && event.target.className === 'hangnum' ? ''
						: text = event.target.textContent;
			window.fxHandler.updateClickedObject(text);
		}
	});	
}
function createTextNodeList() {
	// the textNodeList is used for script transformation;
	// create only three levels of elements and bare text node
	textNodeList = [];
	const allNodes = document.body.childNodes;
	for(let i=0; i<allNodes.length; i++) {
		const node_i = allNodes[i];
		if(node_i.nodeType === Node.TEXT_NODE) {
			textNodeList.push([i, -1, -1]);
		} else if(node_i.nodeType === Node.ELEMENT_NODE && node_i.nodeName !== 'A') {
			for(let j=0; j<node_i.childNodes.length; j++) {
				if(node_i.childNodes[j].nodeType === Node.TEXT_NODE) {
					textNodeList.push([i, j, -1]);
				} else if(node_i.childNodes[j].nodeType === Node.ELEMENT_NODE && node_i.childNodes[j].nodeName !== 'A') {
					const node_j = node_i.childNodes[j];
					for(let k=0; k<node_j.childNodes.length; k++) {
						if(node_j.childNodes[k].nodeType === Node.TEXT_NODE)
							textNodeList.push([i, j, k]);
					}
				}
			}
		}
	}
}
function saveRomanBody() {
	if(romanBody === null)
		romanBody = document.body.cloneNode(true);
}
function toRoman() {
	const workingBody = romanBody.cloneNode(true);
	document.body = workingBody;
	addMouseListener();
}
function toNonRoman(lang, alsoNumber, useThAlt, isLinux) {
	if(useThAlt)
		useAltThai();
	const workingBody = romanBody.cloneNode(true);
	const allNodes = workingBody.childNodes;
	for(const arr of textNodeList) {
		const node = arr[1]<0 ? allNodes[arr[0]] :
					arr[2]<0 ? allNodes[arr[0]].childNodes[arr[1]] :
					allNodes[arr[0]].childNodes[arr[1]].childNodes[arr[2]];
		if(lang === "THAI") {
			node.textContent = romanToThai(node.textContent.toLowerCase(), alsoNumber);
		} else if(lang === "KHMER") {
			node.textContent = romanToKhmer(node.textContent.toLowerCase(), alsoNumber);
		} else if(lang === "MYANMAR") {
			node.textContent = romanToMyanmar(node.textContent.toLowerCase(), alsoNumber);
		} else if(lang === "SINHALA") {
			node.textContent = romanToSinhala(node.textContent.toLowerCase(), alsoNumber);
		} else if(lang === "DEVANAGARI") {	
			node.textContent = romanToDevanagari(node.textContent.toLowerCase(), alsoNumber);
		}
	}
	document.body = workingBody;
}
function setThemeBW(theme, isBW) {
	const themeObj = theme === 'DARK' ? darkThemeObj : lightThemeObj;
	const bw = isBW?'BW':'';
	document.body.style.color = themeObj['color' + bw];
	document.body.style.background = themeObj['background' + bw];
	const blocks = document.getElementsByTagName('blockquote');
	for(const e of blocks) {
		e.style.background = themeObj['blockBG' + bw];
	}
	const notes = document.getElementsByClassName('note');
	for(const e of notes) {
		e.style.color = themeObj['noteColor' + bw];
	}
	const trans = document.getElementsByClassName('sc-translation');
	for(const e of trans) {
		e.style.color = themeObj['transColor' + bw];
	}
	const comms = document.getElementsByClassName('sc-comment');
	for(const e of comms) {
		e.style.color = themeObj['commentColor' + bw];
	}
	const refs = document.getElementsByClassName('sc-reference');
	for(const e of refs) {
		e.style.color = themeObj['referenceColor' + bw];
	}
}
function setLineHeight(percent) {
	document.body.style.lineHeight = percent;
}
