/*
 * cst4-viewer.js
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

function jumpTo(point) {
	const elm = document.getElementById('jumptarget-'+point);
	if(elm !== null)
		elm.scrollIntoView();
}
function showNotes(yn) {
	const elms = document.getElementsByClassName('note');
	for(const e of elms) {
		e.style.display = yn ? 'inline' : 'none';
	}
}
function showXRef(yn) {
	const elms = document.getElementsByTagName('A');
	for(const e of elms) {
		const name = e.getAttribute('name');
		if(name.startsWith('P') || name.startsWith('V') || name.startsWith('M') || name.startsWith('T')) {
			if(yn) {
				e.innerHTML = '[' + name + ']';
				e.className = 'xref';
				e.style.fontSize = '70%';
			} else {
				e.innerHTML = '';
			}
		}
	}
}
function setXrefColor(theme, style) {
	const themeObj = theme === 'DARK' ? darkThemeObj : lightThemeObj;
	const xrefs = document.getElementsByClassName('xref');
	for(const e of xrefs) {
		e.style.color = themeObj['xrefColor' + style];
	}	
}
