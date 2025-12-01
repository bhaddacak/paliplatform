/*
 * pali-viewer.js
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

function init() {
	addMouseListener();
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
function setViewerTheme(theme, style) {
	const themeObj = theme === 'DARK' ? darkThemeObj : lightThemeObj;
	document.body.style.color = themeObj.color[style];
	document.body.style.background = themeObj.background[style];
	const blocks = document.getElementsByTagName('blockquote');
	for(const e of blocks) {
		e.style.background = themeObj.blockBG[style];
	}
	const notes = document.getElementsByClassName('note');
	for(const e of notes) {
		e.style.color = themeObj.noteColor[style];
	}
	const trans = document.getElementsByClassName('sc-translation');
	for(const e of trans) {
		e.style.color = themeObj.transColor[style];
	}
	const comms = document.getElementsByClassName('sc-comment');
	for(const e of comms) {
		e.style.color = themeObj.commentColor[style];
	}
	const refs = document.getElementsByClassName('sc-reference');
	for(const e of refs) {
		e.style.color = themeObj.referenceColor[style];
	}
}
function setLineHeight(percent) {
	document.body.style.lineHeight = percent;
}
