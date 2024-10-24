/*
 * transformer.js
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

// data definition
const romanVowels = "aāiīuūeo";
const romanVowelsForDeva = "aāiīuūeēoō";
const romanConsonants = "kgṅcjñṭḍṇtdnpbmyrlvshḷśṣṛṝḹ";
const romanWithHChars = "bcdgjkptḍṭ";
const romanNumbers = [ '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' ];
const romanBar = "|";
const romanDoubleBar = '\u{2016}';
const romanAbbrSign = '\u{2024}';
const romanConsonantsChr = [
	'k', 'x', 'g', 'x', 'ṅ',
	'c', 'x', 'j', 'x', 'ñ',
	'ṭ', 'x', 'ḍ', 'x', 'ṇ',
	't', 'x', 'd', 'x', 'n',
	'p', 'x', 'b', 'x', 'm',
	'y', 'r', 'l', 'v', 's', 'h', 'ḷ', 'ṃ' ];
const romanConsonantsStr = [
	"k", "kh", "g", "gh", "ṅ",
	"c", "ch", "j", "jh", "ñ",
	"ṭ", "ṭh", "ḍ", "ḍh", "ṇ",
	"t", "th", "d", "dh", "n",
	"p", "ph", "b", "bh", "m",
	"y", "r", "l", "v", "s", "h", "ḷ", "ṃ" ];
// Thai set
const thaiVowels = [ '\u{0E2D}', '\u{0E32}', '\u{0E34}', '\u{0E35}', '\u{0E38}', '\u{0E39}', '\u{0E40}', '\u{0E42}' ];
const thaiNumbers = [ '\u{0E50}', '\u{0E51}', '\u{0E52}', '\u{0E53}', '\u{0E54}', '\u{0E55}', '\u{0E56}', '\u{0E57}', '\u{0E58}', '\u{0E59}' ];
const thaiBindu = '\u{0E3A}';
const thaiPeriod = '\u{0E2F}';
const thaiDoublePeriod = '\u{0E5A}';
const thaiConsonants = [
	'\u{0E01}', '\u{0E02}', '\u{0E04}', '\u{0E06}', '\u{0E07}',
	'\u{0E08}', '\u{0E09}', '\u{0E0A}', '\u{0E0C}', '\u{0E0D}',
	'\u{0E0F}', '\u{0E10}', '\u{0E11}', '\u{0E12}', '\u{0E13}',
	'\u{0E15}', '\u{0E16}', '\u{0E17}', '\u{0E18}', '\u{0E19}',
	'\u{0E1B}', '\u{0E1C}', '\u{0E1E}', '\u{0E20}', '\u{0E21}',
	'\u{0E22}', '\u{0E23}', '\u{0E25}', '\u{0E27}', '\u{0E2A}', '\u{0E2B}', '\u{0E2C}', '\u{0E4D}' ];
// Khmer set
const khmerVowelsInd = [ '\u{17A2}', '\u{17B6}', '\u{17A5}', '\u{17A6}', '\u{17A7}', '\u{17A9}', '\u{17AF}', '\u{17B1}' ];
const khmerVowelsDep = [ '\u{17A2}', '\u{17B6}', '\u{17B7}', '\u{17B8}', '\u{17BB}', '\u{17BC}', '\u{17C1}', '\u{17C4}' ];
const khmerNumbers = [ '\u{17E0}', '\u{17E1}', '\u{17E2}', '\u{17E3}', '\u{17E4}', '\u{17E5}', '\u{17E6}', '\u{17E7}', '\u{17E8}', '\u{17E9}' ];
const khmerKiller = '\u{17D1}';
const khmerCoeng = '\u{17D2}';
const khmerPeriod = '\u{17D4}';
const khmerDoublePeriod = '\u{17D5}';
const khmerConsonants = [
	'\u{1780}', '\u{1781}', '\u{1782}', '\u{1783}', '\u{1784}',
	'\u{1785}', '\u{1786}', '\u{1787}', '\u{1788}', '\u{1789}',
	'\u{178A}', '\u{178B}', '\u{178C}', '\u{178D}', '\u{178E}',
	'\u{178F}', '\u{1790}', '\u{1791}', '\u{1792}', '\u{1793}',
	'\u{1794}', '\u{1795}', '\u{1796}', '\u{1797}', '\u{1798}',
	'\u{1799}', '\u{179A}', '\u{179B}', '\u{179C}', '\u{179F}', '\u{17A0}', '\u{17A1}', '\u{17C6}' ];
// Myanmar set
const myanmarTallA = '\u{102B}';
const myanmarShortA = '\u{102C}';
const myanmarSpecialE = '\u{102A}';
//const myanmarDepE = '\u{1031}'; thsi causes problem, not used
const myanmarVowelsInd = [ '\u{1021}', '\u{102C}', '\u{1023}', '\u{1024}', '\u{1025}', '\u{1026}', '\u{1027}', '\u{1029}' ];
const myanmarVowelsDep = [ '\u{1021}', myanmarShortA, '\u{102D}', '\u{102E}', '\u{102F}', '\u{1030}', myanmarSpecialE, myanmarSpecialE ];
const myanmarNumbers = [ '\u{1040}', '\u{1041}', '\u{1042}', '\u{1043}', '\u{1044}', '\u{1045}', '\u{1046}', '\u{1047}', '\u{1048}', '\u{1049}' ];
const myanmarVirama = '\u{1039}';
const myanmarAsat = '\u{103A}';
const myanmarPeriod = '\u{104A}';
const myanmarDoublePeriod = '\u{104B}';
const myanmarConsonants = [
	'\u{1000}', '\u{1001}', '\u{1002}', '\u{1003}', '\u{1004}',
	'\u{1005}', '\u{1006}', '\u{1007}', '\u{1008}', '\u{1009}',
	'\u{100B}', '\u{100C}', '\u{100D}', '\u{100E}', '\u{100F}',
	'\u{1010}', '\u{1011}', '\u{1012}', '\u{1013}', '\u{1014}',
	'\u{1015}', '\u{1016}', '\u{1017}', '\u{1018}', '\u{1019}',
	'\u{101A}', '\u{101B}', '\u{101C}', '\u{101D}', '\u{101E}', '\u{101F}', '\u{1020}', '\u{1036}' ];
const myanmarToMedialMap = {
	'\u{101A}': '\u{103B}',
	'\u{101B}': '\u{103C}',
	'\u{101D}': '\u{103D}',
	'\u{101F}': '\u{103E}' };
const myanmarNya = '\u{1009}';
const myanmarNnya = '\u{100A}';
const myanmarSa = '\u{101E}';
const myanmarGreatSa = '\u{103F}';
const myanmarTallASet = [
	'\u{1001}' + myanmarShortA,
	'\u{1002}' + myanmarShortA,
	'\u{1004}' + myanmarShortA,
	'\u{1012}' + myanmarShortA,
	'\u{1015}' + myanmarShortA,
	'\u{101D}' + myanmarShortA,
	'\u{1004}' + myanmarVirama + '\u{1001}' + myanmarShortA,
	'\u{1004}' + myanmarVirama + '\u{1002}' + myanmarShortA,
	'\u{1004}' + myanmarVirama + '\u{1004}' + myanmarShortA,
	'\u{1012}' + myanmarVirama + '\u{1012}' + myanmarShortA,
	'\u{1012}' + myanmarVirama + '\u{1013}' + myanmarShortA,
	'\u{1012}' + myanmarVirama + '\u{1019}' + myanmarShortA,
	'\u{1012}' + myanmarVirama + '\u{101D}' + myanmarShortA ];
const myanmarShortASet = [
	'\u{1000}' + myanmarVirama + '\u{1001}' + myanmarTallA,
	'\u{1002}' + myanmarVirama + '\u{1002}' + myanmarTallA,
	'\u{1015}' + myanmarVirama + '\u{1015}' + myanmarTallA,
	'\u{1019}' + myanmarVirama + '\u{1015}' + myanmarTallA,
	myanmarVirama + '\u{101D}' + myanmarTallA ];
// Sinhala set
const sinhalaVowelsInd = [ '\u{0D85}', '\u{0D86}', '\u{0D89}', '\u{0D8A}', '\u{0D8B}', '\u{0D8C}', '\u{0D91}', '\u{0D94}' ];
const sinhalaVowelsDep = [ '\u{0D85}', '\u{0DCF}', '\u{0DD2}', '\u{0DD3}', '\u{0DD4}', '\u{0DD6}', '\u{0DD9}', '\u{0DDC}' ];
const sinhalaVirama = '\u{0DCA}';
const sinhalaConsonants = [
	'\u{0D9A}', '\u{0D9B}', '\u{0D9C}', '\u{0D9D}', '\u{0D9E}',
	'\u{0DA0}', '\u{0DA1}', '\u{0DA2}', '\u{0DA3}', '\u{0DA4}',
	'\u{0DA7}', '\u{0DA8}', '\u{0DA9}', '\u{0DAA}', '\u{0DAB}',
	'\u{0DAD}', '\u{0DAE}', '\u{0DAF}', '\u{0DB0}', '\u{0DB1}',
	'\u{0DB4}', '\u{0DB5}', '\u{0DB6}', '\u{0DB7}', '\u{0DB8}',
	'\u{0DBA}', '\u{0DBB}', '\u{0DBD}', '\u{0DC0}', '\u{0DC3}', '\u{0DC4}', '\u{0DC5}', '\u{0D82}' ];
// Devanagari set
const devaVowelsInd = [ '\u{0905}', '\u{0906}', '\u{0907}', '\u{0908}', '\u{0909}', '\u{090A}', '\u{090F}', '\u{0910}', '\u{0913}', '\u{0914}' ];
const devaVowelsDep = [ '\u{0905}', '\u{093E}', '\u{093F}', '\u{0940}', '\u{0941}', '\u{0942}', '\u{0947}', '\u{0948}', '\u{094B}', '\u{094C}' ];
const devaNumbers = [ '\u{0966}', '\u{0967}', '\u{0968}', '\u{0969}', '\u{096A}', '\u{096B}', '\u{096C}', '\u{096D}', '\u{096E}', '\u{096F}' ];
const devaVirama = '\u{094D}';
const devaPeriod = '\u{0964}';
const devaDoublePeriod = '\u{0965}';
const devaAbbrev = '\u{0970}';
const devaConsonants = [
	'\u{0915}', '\u{0916}', '\u{0917}', '\u{0918}', '\u{0919}',
	'\u{091A}', '\u{091B}', '\u{091C}', '\u{091D}', '\u{091E}',
	'\u{091F}', '\u{0920}', '\u{0921}', '\u{0922}', '\u{0923}',
	'\u{0924}', '\u{0925}', '\u{0926}', '\u{0927}', '\u{0928}',
	'\u{092A}', '\u{092B}', '\u{092C}', '\u{092D}', '\u{092E}',
	'\u{092F}', '\u{0930}', '\u{0932}', '\u{0935}', '\u{0938}', '\u{0939}', '\u{0933}', '\u{0902}' ];

// functions
function useAltThai() {
	// the replacement of 0E0D (Yo-ying) and 0E10 (Tho-than)
	const altPaliThaiChars = [ '\u{F70F}', '\u{F700}'];
	thaiConsonants[9] = altPaliThaiChars[0];
	thaiConsonants[11] = altPaliThaiChars[1];	
}
function romanToThai(input, alsoNumber) {
	let output = '';
	let rch = null;
	let tch = null;
	let vindex = -1; // for vowels
	let skipFlag = false;
	for(let index = 0; index<input.length; index++) {
		if(skipFlag) {
			skipFlag = false;
			continue;
		}
		rch = input[index];
		tch = rch; // in case of non-character
		// 1. find Thai representation of the character first
		if(rch >= '0' && rch <= '9') {
			// is number
			if(alsoNumber)
				tch = thaiNumbers[parseInt(rch, 10)];
		} else if(rch === '.') {
			// period is hard to differentiate from a normal dot, so retain as dot;
			tch = '.';
		} else if (rch === romanBar) {
			// single bar is changed to single danda
			tch = thaiPeriod;
		} else if (rch === romanDoubleBar) {
			// double bar is changed to double danda
			tch = thaiDoublePeriod;
		} else if(rch === 'x') {
			// reserved character
			tch = rch;
		} else if((vindex = romanVowels.indexOf(rch)) >= 0) {
			// is vowels
			tch = thaiVowels[vindex];
		} else {
			// is consonants
			for(let i=0; i<romanConsonantsChr.length; i++) {
				if(rch === romanConsonantsChr[i]) {
					if(index < input.length-1) {
						// if the character has 'h'
						if(romanWithHChars.indexOf(rch) >= 0 && input[index+1] === 'h')
							skipFlag = true;
					}
					tch = skipFlag? thaiConsonants[i+1]: thaiConsonants[i];
					break;
				}
			} // end for loop of finding pali consonant
		}
		// 2. consider how to put it
		if(vindex >= 0) {
			// vowels
			if(output.length === 0) {
				// to prevent index out of bound
				// independent vowels
				if(rch === 'a') {
					output += tch;
				} else if(rch === 'e' || rch === 'o') {
					// transposition is needed
					output += tch;
					output += thaiVowels[0];
				} else {
					output += thaiVowels[0];
					output += tch;
				}
			} else {
				// look at the preceeding character; if it is not a consonant, 'a' is added
				if(romanConsonants.indexOf(input[index-1]) < 0) {
					// not follow a consonant
					if(rch === 'a') {
						output += tch;
					} else if(rch === 'e' || rch === 'o') {
						// transposition is needed
						output += tch;
						output += thaiVowels[0];
					} else {
						output += thaiVowels[0];
						output += tch;
					}						
				} else {
					// follow a consonant
					if(rch === 'e' || rch === 'o') {
						// insert it before the last character
						output = output.substring(0, output.length-1) + tch + output.substring(output.length-1);
					} else if(rch != 'a') {
						output += tch;
					}
				}
			}
		} else {
			if(romanConsonants.indexOf(rch) >= 0) {
				// consonants
				output += tch;
				if(index < input.length-1) {
					if(!skipFlag) {
						if (romanConsonants.indexOf(input[index+1]) >= 0) {
							// double consonant needs bindu
							output += thaiBindu;
						} else if (romanVowels.indexOf(input[index+1]) == -1) {
							// if not followed by a vowel, add bindu
							output += thaiBindu;
						}
					} else {
						// characters with h
						if (index < input.length-2) {
							if (romanConsonants.indexOf(input[index+2]) >= 0) {
								// double consonant needs bindu
								output += thaiBindu;
							} else if (romanVowels.indexOf(input[index+2]) == -1) {
								// if not followed by a vowel, add bindu
								output += thaiBindu;
							}
						} else {
							// the last consonant, add bindu
							output += thaiBindu;
						}
					}
				} else {
					// the last consonant, add bindu
					output += thaiBindu;
				}
			} else {
				// others
				output += tch;
			}
		}
		vindex = -1;
	} // end for loop of each input character
	return output;
}
function romanToKhmer(input, alsoNumber) {
	let output = '';
	let rch = null;
	let kch = null;
	let vindex = -1; // for vowels
	let skipFlag = false;
	for(let index = 0; index<input.length; index++) {
		if(skipFlag) {
			skipFlag = false;
			continue;
		}
		rch = input[index];
		kch = rch; // in case of non-character
		// 1. find Khmer representation of the character first
		if(rch >= '0' && rch <= '9') {
			// is number
			if(alsoNumber)
				kch = khmerNumbers[parseInt(rch, 10)];
		} else if(rch === '.') {
			// period is retained as dot
			kch = '.';
		} else if (rch === romanBar) {
			// single bar is changed to single danda
			kch = khmerPeriod;
		} else if (rch === romanDoubleBar) {
			// double bar is changed to double danda
			kch = khmerDoublePeriod;
		} else if(rch === 'x') {
			// reserved character
			kch = rch;
		} else if((vindex = romanVowels.indexOf(rch)) >= 0) {
			// is vowels
			kch = khmerVowelsInd[vindex];
		} else {
			// is consonants
			for(let i=0; i<romanConsonantsChr.length; i++) {
				if(rch === romanConsonantsChr[i]) {
					if(index < input.length-1) {
						// if the character has 'h'
						if(romanWithHChars.indexOf(rch) >= 0 && input[index+1] === 'h')
							skipFlag = true;
					}
					kch = skipFlag? khmerConsonants[i+1]: khmerConsonants[i];
					break;
				}
			} // end for loop of finding pali consonant
		}
		// 2. consider how to put it
		if(vindex >= 0) {
			// vowels
			if(output.length === 0) {
				// to prevent index out of bound
				// independent vowels
				if(rch === 'ā') {
					// this needs 2 chars
					output += khmerVowelsInd[0];
					output += kch;
				} else {
					output += kch;
				}
			} else {
				// look at the preceeding character; if it is not a consonant, independent vowels are used
				if(romanConsonants.indexOf(input[index-1]) < 0) {
					if(rch === 'ā') {
						// this needs 2 chars
						output += khmerVowelsInd[0];
						output += kch;
					} else {
						output += kch;
					}						
				} else {
					// dependent vowels are used
					if(rch != 'a') {
						output += khmerVowelsDep[vindex];
					}
				}
			}
		} else {
			if(romanConsonants.indexOf(rch) >= 0) {
				// consonants
				output += kch;
				if(index < input.length-1) {
					if(!skipFlag) {
						if (romanConsonants.indexOf(input[index+1]) >= 0) {
							// double consonant needs coeng 0x17D2
							output += khmerCoeng;
						} else if (romanVowels.indexOf(input[index+1]) == -1) {
							// if not followed by a vowel, add killer
							output += khmerKiller;
						}
					} else {
						// characters with h
						if (index < input.length-2) {
							if (romanConsonants.indexOf(input[index+2]) >= 0) {
								// double consonant needs coeng
								output += khmerCoeng;
							} else if (romanVowels.indexOf(input[index+2]) == -1) {
								// if not followed by a vowel, add killer
								output += khmerKiller;
							}
						} else {
							// the last consonant, add killer
							output += khmerKiller;
						}
					}
				} else {
					// the last consonant, add killer
					output += khmerKiller;
				}
			} else {
				// others
				output += kch;
			}
		}
		vindex = -1;
	} // end for loop of each input character
	return output;
}
function romanToMyanmar(input, alsoNumber) {
	let output = '';
	let rch = null;
	let mch = null;
	let vindex = -1; // for vowels
	let skipFlag = false;
	for(let index = 0; index<input.length; index++) {
		if(skipFlag) {
			skipFlag = false;
			continue;
		}
		rch = input[index];
		mch = rch; // in case of non-character
		// 1. find Myanmar representation of the character first
		if(rch >= '0' && rch <= '9') {
			// is number
			if(alsoNumber)
				mch = myanmarNumbers[parseInt(rch, 10)];
		} else if(rch === '.') {
			// period is retained as dot
			mch = '.';
		} else if (rch === romanBar) {
			// single bar is changed to single danda
			mch = myanmarPeriod;
		} else if (rch === romanDoubleBar) {
			// double bar is changed to double danda
			mch = myanmarDoublePeriod;
		} else if(rch === 'x') {
			// reserved character
			mch = rch;
		} else if((vindex = romanVowels.indexOf(rch)) >= 0) {
			// is vowels
			mch = myanmarVowelsInd[vindex];
		} else {
			// is consonants
			for(let i=0; i<romanConsonantsChr.length; i++) {
				if(rch === romanConsonantsChr[i]) {
					if(index < input.length-1) {
						// if the character has 'h'
						if(romanWithHChars.indexOf(rch) >= 0 && input[index+1] === 'h')
							skipFlag = true;
					}
					mch = skipFlag? myanmarConsonants[i+1]: myanmarConsonants[i];
					break;
				}
			} // end for loop of finding pali consonant
		}
		// 2. consider how to put it
		if(vindex >= 0) {
			// vowels
			if(output.length === 0) {
				// to prevent index out of bound
				// independent vowels
				if(rch === 'ā')
					output += myanmarVowelsInd[0];
				output += mch;
			} else {
				// look at the preceeding character; if it is not a consonant, independent vowels are used
				if(romanConsonants.indexOf(input[index-1]) < 0) {
					if(rch === 'ā')
						output += myanmarVowelsInd[0];
					output += mch;
				} else {
					// dependent vowels are used
					if(rch != 'a') {
						if(rch === 'e' || rch === 'o') {
							// transposition is needed
							let insInd = 0;
							if(index >= 1) {
								const lastCh = input[index-1];
								if(lastCh == 'h') {
									if(index >= 2) {
										insInd = romanWithHChars.indexOf(input[index-2]) >= 0
												? index >= 3 && romanConsonants.indexOf(input[index-3]) >= 0
													? 3 // double consonants plus virama
													: 1 // single character with h
												: romanConsonants.indexOf(input[index-2]) >= 0
													? 3 // double consonants plus virama
													: 1; // single consonant
									} else {
										insInd = index >= 2 && romanConsonants.indexOf(input[index-2]) >= 0
												? 3 // double consonants plus virama
												: 1; // single consonant
									}
								} else {
									if(index >= 3) {
										insInd = romanConsonants.indexOf(input[index-2]) >= 0
												? input[index-2] == 'h'
													? index >= 4 && romanConsonants.indexOf(input[index-4]) >= 0
														? 5 // triple consonants plus virama
														: 3 // double consonants plus virama
													: romanConsonants.indexOf(input[index-3]) >= 0
														? 5 // triple consonants plus virama
														: 3 // double consonants plus virama
												: 1; // single consonant
									} else {
										insInd = index >= 2 && romanConsonants.indexOf(input[index-2]) >= 0
												? 3 // double consonants plus virama
												: 1; // single consonant
									}
								}
							}
							output = output.substring(0, output.length-insInd) + myanmarVowelsDep[vindex] + output.substring(output.length-insInd);
							if(rch === 'o')
								output += myanmarVowelsDep[1];
						} else {
							output += myanmarVowelsDep[vindex];
						}
					}
				}
			}
		} else {
			if(romanConsonants.indexOf(rch) >= 0) {
				// consonants
				output += mch;
				if(index < input.length-1) {
					if(!skipFlag) {
						if (romanConsonants.indexOf(input[index+1]) >= 0) {
							// double consonant needs virama
							output += myanmarVirama;
						} else if (romanVowels.indexOf(input[index+1]) == -1) {
							// if not followed by a vowel, add asat
							output += myanmarAsat;
						}
					} else {
						// characters with h
						if (index < input.length-2) {
							if (romanConsonants.indexOf(input[index+2]) >= 0) {
								// double consonant needs virama
								output += myanmarVirama;
							} else if (romanVowels.indexOf(input[index+2]) == -1) {
								// if not followed by a vowel, add asat
								output += myanmarAsat;
							}
						} else {
							// the last consonant, add asat
							output += myanmarAsat;
						}
					}
				} else {
					// the last consonant, add asat
					output += myanmarAsat;
				}
			} else {
				// others
				output += mch;
			}
		}
		vindex = -1;
	} // end for loop of each input character
	return toMyanmarProcess(output);
}
function processMyanmarA(input){
	let output = input;
	// change to tall ā
	for (const chs of myanmarTallASet) {
		const shortARe = new RegExp(chs, 'g');
		output = output.replace(shortARe, chs.slice(0, -1) + myanmarTallA);
	}
	// fix some back to short ā
	for (const chs of myanmarShortASet) {
		const tallARe = new RegExp(chs, 'g');
		output = output.replace(tallARe, chs.slice(0, -1) + myanmarShortA);
	}
	return output;
}
function toMyanmarProcess(input) {
	if (input === undefined) return '';
	let output = processMyanmarA(input);
	const nynyRe = new RegExp(myanmarNya + myanmarVirama + myanmarNya, 'g');
	output = output.replace(nynyRe, myanmarNnya);
	const sRe = new RegExp(myanmarSa + myanmarVirama + myanmarSa, 'g');
	output = output.replace(sRe, myanmarGreatSa);
	for (const key in myanmarToMedialMap) {
		let mRe = new RegExp(myanmarVirama + key, 'g');
		output = output.replace(mRe, myanmarToMedialMap[key]);
	}
	return output;
}
function romanToSinhala(input, alsoNumber) {
	let output = '';
	let rch = null;
	let sch = null;
	let vindex = -1; // for vowels
	let skipFlag = false;
	for(let index = 0; index<input.length; index++) {
		if(skipFlag) {
			skipFlag = false;
			continue;
		}
		rch = input[index];
		sch = rch; // in case of non-character
		// 1. find Sinhala representation of the character first
		if(rch === 'x') {
			// reserved character
			sch = rch;
		} else if((vindex = romanVowels.indexOf(rch)) >= 0) {
			// is vowels
			sch = sinhalaVowelsInd[vindex];
		} else {
			// is consonants
			for(let i=0; i<romanConsonantsChr.length; i++) {
				if(rch === romanConsonantsChr[i]) {
					if(index < input.length-1) {
						// if the character has 'h'
						if(romanWithHChars.indexOf(rch) >= 0 && input[index+1] === 'h')
							skipFlag = true;
					}
					sch = skipFlag? sinhalaConsonants[i+1]: sinhalaConsonants[i];
					break;
				}
			} // end for loop of finding pali consonant
		}
		// 2. consider how to put it
		if(vindex >= 0) {
			// vowels
			if(output.length === 0) {
				// to prevent index out of bound
				// independent vowels
				output += sch;
			} else {
				// look at the preceeding character; if it is not a consonant, independent vowels are used
				if(romanConsonants.indexOf(input[index-1]) < 0) {
					output += sch;
				} else {
					// dependent vowels are used
					if(rch != 'a') {
						output += sinhalaVowelsDep[vindex];
					}
				}
			}
		} else {
			if(romanConsonants.indexOf(rch) >= 0) {
				// consonants
				output += sch;
				if(index < input.length-1) {
					if(!skipFlag) {
						if (romanConsonants.indexOf(input[index+1]) >= 0) {
							// double consonant needs virama
							output += sinhalaVirama;
						} else if (romanVowels.indexOf(input[index+1]) == -1) {
							// if not followed by a vowel, add virama
							output += sinhalaVirama;
						}
					} else {
						// characters with h
						if (index < input.length-2) {
							if (romanConsonants.indexOf(input[index+2]) >= 0) {
								// double consonant needs virama
								output += sinhalaVirama;
							} else if (romanVowels.indexOf(input[index+2]) == -1) {
								// if not followed by a vowel, add virama
								output += sinhalaVirama;
							}
						} else {
							// the last consonant, add virama
							output += sinhalaVirama;
						}
					}
				} else {
					// the last consonant, add virama
					output += sinhalaVirama;
				}
			} else {
				// others
				output += sch;
			}
		}
		vindex = -1;
	} // end for loop of each input character
	return output;
}
function romanToDevanagari(input, alsoNumber) {
	let output = '';
	let rch;
	let dch;
	let vindex = -1; // for vowels
	let skipFlag = false;
	for(let index = 0; index<input.length; index++) {
		if(skipFlag) {
			skipFlag = false;
			continue;
		}
		rch = input[index];
		dch = rch; // in case of non-character
		// 1. find Devanagari representation of the character first
		if(rch >= '0' && rch <= '9') {
			// is number
			if(alsoNumber)
				dch = devaNumbers[parseInt(rch, 10)];
		} else if(rch === '.') {
			// period is retained as dot
			dch = '.';
		} else if(rch === romanBar) {
			// single bar is changed to single danda
			dch = devaPeriod;
		} else if(rch === romanDoubleBar) {
			// double bar is changed to double danda
			dch = devaDoublePeriod;
		} else if(rch === romanAbbrSign) {
			// one dot leader is changed to abbreviation sign
			dch = devaAbbrev;
		} else if(rch === 'x') {
			// reserved character
			dch = rch;
		} else if((vindex = romanVowelsForDeva.indexOf(rch)) >= 0) {
			// is vowels
			dch = devaVowelsInd[vindex];
		} else {
			// is consonants
			for(let i=0; i<romanConsonantsChr.length; i++) {
				if(rch === romanConsonantsChr[i]) {
					if(index < input.length-1) {
						// if the character has 'h'
						if(romanWithHChars.indexOf(rch) >= 0 && input[index+1] === 'h')
							skipFlag = true;
					}
					dch = skipFlag? devaConsonants[i+1]: devaConsonants[i];
					break;
				}
			} // end for loop of finding pali consonant
		}
		// 2. consider how to put it
		if(vindex >= 0) {
			// vowels
			if(output.length === 0) {
				// to prevent index out of bound
				// independent vowels
				output += dch;
			} else {
				// look at the preceeding character; if it is not a consonant, independent vowels are used
				if(romanConsonants.indexOf(input[index-1]) < 0) {
					output += dch;
				} else {
					// dependent vowels are used
					if(rch != 'a') {
						output += devaVowelsDep[vindex];
					}
				}
			}
		} else {
			if(romanConsonants.indexOf(rch) >= 0) {
				// consonants
				output += dch;
				if(index < input.length-1) {
					if(!skipFlag) {
						if (romanConsonants.indexOf(input[index+1]) >= 0) {
							// double consonant needs virama
							output += devaVirama;
						} else if (romanVowelsForDeva.indexOf(input[index+1]) == -1) {
							// if not followed by a vowel, add virama
							output += devaVirama;
						}
					} else {
						// characters with h
						if (index < input.length-2) {
							if (romanConsonants.indexOf(input[index+2]) >= 0) {
								// double consonant needs virama
								output += devaVirama;
							} else if (romanVowels.indexOf(input[index+2]) == -1) {
								// if not followed by a vowel, add virama
								output += devaVirama;
							}
						} else {
							// the last consonant, add virama
							output += devaVirama;
						}
					}
				} else {
					// the last consonant, add virama
					output += devaVirama;
				}
			} else {
				// others
				output += dch;
			}
		}
		vindex = -1;
	} // end for loop of each input character
	return output;
}

