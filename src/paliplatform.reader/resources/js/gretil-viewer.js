/*
 * gretil-viewer.js
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

function toggleFront() {
	const front = document.getElementById("frontmatter");
	const note = document.getElementById("gretilnotes");
	if(front.style.display === "none") {
		front.style.display = "block";
		note.style.display = "none";
	} else {
		front.style.display = "none";
	}
}
function toggleNote() {
	const note = document.getElementById("gretilnotes");
	const front = document.getElementById("frontmatter");
	if(note.style.display === "none") {
		note.style.display = "block";
		front.style.display = "none";
	} else {
		note.style.display = "none";
	}
}
function jumpTo(point) {
	const elm = document.getElementById('page-'+point);
	if(elm !== null)
		elm.scrollIntoView();
}
